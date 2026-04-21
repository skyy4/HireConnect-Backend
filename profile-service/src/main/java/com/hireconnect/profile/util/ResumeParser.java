package com.hireconnect.profile.util;

import com.hireconnect.profile.pojo.CandidateProfile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResumeParser {

    /**
     * Mocks the parsing of a PDF resume to extract skills and profile hints.
     * In a real system, this would use Apache Tika, PDFBox, or an AI Model.
     */
    public void parseAndFill(CandidateProfile profile, String resumeUrl) {
        if (resumeUrl == null || resumeUrl.isEmpty()) return;

        // Mock: Extract some skills based on the "filename" or just simulated extraction
        String fileName = resumeUrl.toLowerCase();
        
        List<String> commonSkills = Arrays.asList(
            "Java", "Spring", "React", "Node", "Python", "SQL", "Docker", "AWS", "Kubernetes", "DevOps"
        );

        // Simulate extraction: If any common skills are in the "filename", add them
        List<String> extractedSkills = commonSkills.stream()
                .filter(skill -> fileName.contains(skill.toLowerCase()))
                .collect(Collectors.toList());

        if (!extractedSkills.isEmpty()) {
            List<String> currentSkills = profile.getSkills();
            extractedSkills.forEach(s -> {
                if (!currentSkills.contains(s)) {
                    currentSkills.add(s);
                }
            });
            profile.setSkills(currentSkills);
        }

        // Mock extraction of Experience or Job Title
        if (fileName.contains("senior")) {
            profile.setExperienceYears(Math.max(profile.getExperienceYears(), 5));
        } else if (fileName.contains("lead")) {
            profile.setExperienceYears(Math.max(profile.getExperienceYears(), 8));
        }

        if (profile.getBio() == null || profile.getBio().isEmpty()) {
            profile.setBio("Auto-extracted from resume: " + resumeUrl);
        }
    }
}
