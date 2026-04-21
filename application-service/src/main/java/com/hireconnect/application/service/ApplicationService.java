package com.hireconnect.application.service;

import com.hireconnect.application.entity.Application;
import java.util.List;

public interface ApplicationService {

    Application submitApplication(Application application);

    Application getApplicationById(int applicationId);

    List<Application> getByCandidate(int candidateId);

    List<Application> getByJob(int jobId);

    List<Application> getByJobAndStatus(int jobId, String status);

    Application updateStatus(int applicationId, String status, String note);

    void withdrawApplication(int applicationId, int candidateId);

    boolean hasApplied(int jobId, int candidateId);

    long countByJob(int jobId);
}
