package com.hireconnect.profile.util;

import com.hireconnect.profile.pojo.ParsedResume;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ResumeParser extracts structured data from a candidate's resume.
 *
 * Strategy:
 *  1. If the resumeUrl is a local file path (starts with /uploads/**), read
 *     the actual PDF bytes using Apache PDFBox and extract full text.
 *  2. For external URLs (http/https) or when the file is unavailable, fall back
 *     to keyword-heuristic matching on the URL string itself.
 */
@Component
public class ResumeParser {

    private static final List<String> TECH_SKILLS = Arrays.asList(
            "Java", "Spring", "Spring Boot", "React", "Angular", "Vue",
            "Node.js", "Python", "Django", "FastAPI", "SQL", "MySQL",
            "PostgreSQL", "MongoDB", "Redis", "Kafka", "RabbitMQ",
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "DevOps",
            "CI/CD", "Git", "Linux", "REST", "GraphQL", "Microservices",
            "Hibernate", "JPA", "TypeScript", "JavaScript", "HTML", "CSS",
            "Android", "iOS", "Swift", "Kotlin", "Flutter", "Dart",
            "Machine Learning", "TensorFlow", "PyTorch", "Data Science",
            "Spark", "Hadoop", "Tableau", "Power BI", "Excel"
    );

    private static final List<String> SOFT_SKILLS = Arrays.asList(
            "Leadership", "Communication", "Teamwork", "Problem Solving",
            "Agile", "Scrum", "Project Management", "Mentoring", "Collaboration"
    );

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?:\\+91[\\-\\s]?)?[6-9]\\d{9}|\\d{10}");

    private static final Pattern EXPERIENCE_PATTERN =
            Pattern.compile("(\\d+)\\+?\\s*years?\\s*(?:of\\s*)?(?:experience|exp)", Pattern.CASE_INSENSITIVE);

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^([A-Z][a-z]+(?:\\s+[A-Z][a-z]+){1,3})", Pattern.MULTILINE);

    // ── Public entry point ────────────────────────────────────────────────────

    public void parse(ParsedResume parsed, String resumeUrl) {
        if (resumeUrl == null || resumeUrl.isBlank()) {
            parsed.setParseStatus("FAILED");
            return;
        }

        String text = extractText(resumeUrl);
        String textLower = text.toLowerCase();

        // ── Skills ───────────────────────────────────────────────────────────
        List<String> skills = new ArrayList<>();
        for (String skill : TECH_SKILLS) {
            if (textLower.contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }
        for (String skill : SOFT_SKILLS) {
            if (textLower.contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }
        parsed.setExtractedSkills(skills);

        // ── Experience ───────────────────────────────────────────────────────
        Matcher expMatcher = EXPERIENCE_PATTERN.matcher(textLower);
        if (expMatcher.find()) {
            parsed.setInferredExperienceYears(Integer.parseInt(expMatcher.group(1)));
        } else {
            if (textLower.contains("lead") || textLower.contains("principal") || textLower.contains("architect")) {
                parsed.setInferredExperienceYears(8);
            } else if (textLower.contains("senior") || textLower.contains("sr.")) {
                parsed.setInferredExperienceYears(5);
            } else if (textLower.contains("junior") || textLower.contains("jr.") || textLower.contains("fresher") || textLower.contains("entry level")) {
                parsed.setInferredExperienceYears(0);
            } else if (textLower.contains("mid") || textLower.contains("associate")) {
                parsed.setInferredExperienceYears(3);
            } else {
                parsed.setInferredExperienceYears(2);
            }
        }

        // ── Email ────────────────────────────────────────────────────────────
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            parsed.setExtractedEmail(emailMatcher.group());
        }

        // ── Phone ────────────────────────────────────────────────────────────
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            parsed.setExtractedPhone(phoneMatcher.group());
        }

        // ── Name (first capitalized full name on the page) ───────────────────
        Matcher nameMatcher = NAME_PATTERN.matcher(text);
        if (nameMatcher.find() && parsed.getExtractedName() == null) {
            parsed.setExtractedName(nameMatcher.group(1));
        }

        // ── Summary ──────────────────────────────────────────────────────────
        String skillSummary = skills.isEmpty() ? "No specific skills detected" : String.join(", ", skills);
        parsed.setSummary(String.format(
                "Parsed from resume: %s. Experience: ~%d years. Skills identified: %s.",
                resumeUrl, parsed.getInferredExperienceYears(), skillSummary));
        parsed.setRawText(text.length() > 5000 ? text.substring(0, 5000) : text);
        parsed.setParsedAt(LocalDateTime.now());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Attempts to extract full text from the PDF at the given path/URL.
     * Returns the URL string itself as fallback text so heuristics still work.
     */
    private String extractText(String resumeUrl) {
        // Only try disk extraction for local file paths saved by uploadCandidateResume()
        if (resumeUrl.startsWith("/uploads/")) {
            // Strip leading slash — path is relative to JVM working directory
            File pdfFile = new File(resumeUrl.substring(1));
            if (!pdfFile.exists()) {
                // Try relative to CWD with full path
                pdfFile = new File("." + resumeUrl);
            }
            if (pdfFile.exists() && pdfFile.isFile()) {
                try (PDDocument doc = Loader.loadPDF(pdfFile)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String extracted = stripper.getText(doc);
                    if (extracted != null && !extracted.isBlank()) {
                        return extracted;
                    }
                } catch (IOException e) {
                    // Silently fall through to heuristic extraction
                }
            }
        }
        // Fallback: use the URL string (works for external CDN URLs when no disk access)
        return resumeUrl;
    }
}
