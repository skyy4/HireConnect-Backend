package com.hireconnect.job.service;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.repository.BookmarkRepository;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.repository.JobViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private JobViewRepository jobViewRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void getJobById_Success() {
        Job job = new Job();
        job.setJobId(1);
        job.setTitle("Test Job");

        when(jobRepository.findById(1)).thenReturn(Optional.of(job));

        Job res = jobService.getJobById(1);
        assertNotNull(res);
        assertEquals("Test Job", res.getTitle());
    }

    @Test
    void getJobById_NotFound_ThrowsException() {
        when(jobRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> jobService.getJobById(1));
    }

    @Test
    void updateJobStatus_ValidStatus_Success() {
        Job job = new Job();
        job.setJobId(1);
        job.setStatus("ACTIVE");

        when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        Job res = jobService.updateJobStatus(1, "PAUSED");
        assertEquals("PAUSED", res.getStatus());
    }

    @Test
    void updateJobStatus_InvalidStatus_ThrowsException() {
        Job job = new Job();
        job.setJobId(1);
        job.setStatus("ACTIVE");

        when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        assertThrows(IllegalArgumentException.class, () -> jobService.updateJobStatus(1, "INVALID"));
    }
}
