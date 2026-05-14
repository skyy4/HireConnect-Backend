package com.hireconnect.job.service;

import com.hireconnect.job.entity.Bookmark;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.entity.JobView;

import java.util.List;

public interface JobService {

    // ── Job CRUD ──────────────────────────────────────────────────────────
    Job addJob(Job job);

    Job getJobById(int jobId);

    List<Job> getAllJobs();

    List<Job> getJobsByRecruiter(int recruiterId);

    List<Job> searchJobs(String title, String location, String category,
                         String type, String experienceLevel,
                         Double minSalary, Double maxSalary);

    Job updateJob(int jobId, Job job);

    void deleteJob(int jobId);

    Job updateJobStatus(int jobId, String status);

    Job incrementViewCount(int jobId);

    List<Job> getJobsByStatus(String status);

    long countJobsByRecruiter(int recruiterId);

    long countJobsByStatus(String status);

    long countAllJobs();

    // ── Bookmark (Saved Jobs) ─────────────────────────────────────────────
    Bookmark addBookmark(int candidateId, int jobId, String note);

    void removeBookmark(int candidateId, int jobId);

    List<Bookmark> getBookmarksByCandidate(int candidateId);

    boolean isBookmarked(int candidateId, int jobId);

    long countBookmarksByJob(int jobId);

    // ── Job View Tracking ─────────────────────────────────────────────────
    JobView recordView(int jobId, Integer viewerId, String ipAddress, String source);

    long getViewCount(int jobId);

    List<JobView> getViewsByJob(int jobId);

    List<Object[]> getTopViewedJobs();
}
