package com.hireconnect.job.service;

import com.hireconnect.job.entity.Job;
import java.util.List;

public interface JobService {

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
}
