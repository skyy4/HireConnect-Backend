package com.hireconnect.application.service;

import com.hireconnect.application.client.JobServiceClient;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.repository.ApplicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationServiceImpl Unit Tests")
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private JobServiceClient jobServiceClient;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Test
    @DisplayName("getApplicationById — returns application when found")
    void getApplicationById_Success() {
        Application app = new Application();
        app.setApplicationId(1);
        app.setStatus("APPLIED");

        when(applicationRepository.findById(1)).thenReturn(Optional.of(app));

        Application res = applicationService.getApplicationById(1);
        assertNotNull(res);
        assertEquals("APPLIED", res.getStatus());
    }

    @Test
    @DisplayName("submitApplication — calls job-service Feign client and saves")
    void submitApplication_Success() {
        Application app = new Application();
        app.setJobId(1);
        app.setCandidateId(2);

        // Stub synchronous Feign call to job-service
        when(jobServiceClient.getJobById(1)).thenReturn(Map.of("jobId", 1, "title", "Engineer"));
        when(applicationRepository.existsByJobIdAndCandidateId(1, 2)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        Application res = applicationService.submitApplication(app);
        assertNotNull(res);
        verify(jobServiceClient).getJobById(1);
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("submitApplication — throws when Feign call fails (job not found)")
    void submitApplication_JobNotFound_ThrowsException() {
        Application app = new Application();
        app.setJobId(99);
        app.setCandidateId(2);

        when(jobServiceClient.getJobById(99)).thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(IllegalArgumentException.class, () -> applicationService.submitApplication(app));
    }

    @Test
    @DisplayName("submitApplication — throws on duplicate application")
    void submitApplication_DuplicateApplication_ThrowsException() {
        Application app = new Application();
        app.setJobId(1);
        app.setCandidateId(2);

        when(jobServiceClient.getJobById(1)).thenReturn(Map.of("jobId", 1));
        when(applicationRepository.existsByJobIdAndCandidateId(1, 2)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> applicationService.submitApplication(app));
    }

    @Test
    @DisplayName("getApplicationById — throws when not found")
    void getApplicationById_NotFound_Throws() {
        when(applicationRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> applicationService.getApplicationById(999));
    }

    @Test
    @DisplayName("hasApplied — delegates to repository")
    void hasApplied_DelegatesToRepo() {
        when(applicationRepository.existsByJobIdAndCandidateId(1, 5)).thenReturn(true);
        assertTrue(applicationService.hasApplied(1, 5));
    }

    @Test
    @DisplayName("countByJob — returns count from repository")
    void countByJob_ReturnsCount() {
        when(applicationRepository.countByJobId(1)).thenReturn(10L);
        assertEquals(10L, applicationService.countByJob(1));
    }
}
