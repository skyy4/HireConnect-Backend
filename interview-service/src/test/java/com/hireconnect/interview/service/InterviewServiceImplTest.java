package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.repository.InterviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewServiceImpl Unit Tests")
class InterviewServiceImplTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private Interview buildInterview(int id, String status) {
        return Interview.builder()
                .interviewId(id)
                .applicationId(10)
                .candidateId(101)
                .recruiterId(201)
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("scheduleInterview — sets status SCHEDULED and saves")
    void scheduleInterview_setsStatusAndSaves() {
        Interview interview = buildInterview(0, "DRAFT");
        Interview saved = buildInterview(1, "SCHEDULED");

        when(interviewRepository.save(any())).thenReturn(saved);

        Interview result = interviewService.scheduleInterview(interview);

        assertThat(result.getStatus()).isEqualTo("SCHEDULED");
        verify(interviewRepository).save(interview);
    }

    @Test
    @DisplayName("getInterviewById — returns interview when found")
    void getInterviewById_found() {
        Interview interview = buildInterview(5, "SCHEDULED");
        when(interviewRepository.findById(5)).thenReturn(Optional.of(interview));

        Interview result = interviewService.getInterviewById(5);
        assertThat(result.getInterviewId()).isEqualTo(5);
    }

    @Test
    @DisplayName("getInterviewById — throws when not found")
    void getInterviewById_notFound_throws() {
        when(interviewRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.getInterviewById(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Interview not found");
    }

    @Test
    @DisplayName("confirmInterview — sets status to CONFIRMED")
    void confirmInterview_setsStatusConfirmed() {
        Interview interview = buildInterview(2, "SCHEDULED");
        Interview confirmed = buildInterview(2, "CONFIRMED");

        when(interviewRepository.findById(2)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any())).thenReturn(confirmed);

        Interview result = interviewService.confirmInterview(2);
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("cancelInterview — sets status to CANCELLED with reason")
    void cancelInterview_setsStatusCancelled() {
        Interview interview = buildInterview(3, "SCHEDULED");
        Interview cancelled = buildInterview(3, "CANCELLED");

        when(interviewRepository.findById(3)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any())).thenReturn(cancelled);

        Interview result = interviewService.cancelInterview(3, "Candidate unavailable");
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("rescheduleInterview — updates time and status to RESCHEDULED")
    void rescheduleInterview_updatesTime() {
        Interview interview = buildInterview(4, "SCHEDULED");
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);
        Interview rescheduled = buildInterview(4, "RESCHEDULED");
        rescheduled.setScheduledAt(newTime);

        when(interviewRepository.findById(4)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any())).thenReturn(rescheduled);

        Interview result = interviewService.rescheduleInterview(4, newTime, "Notes");
        assertThat(result.getStatus()).isEqualTo("RESCHEDULED");
    }

    @Test
    @DisplayName("getByCandidate — returns list from repository")
    void getByCandidate_returnsList() {
        List<Interview> list = List.of(buildInterview(1, "SCHEDULED"), buildInterview(2, "CONFIRMED"));
        when(interviewRepository.findByCandidateId(101)).thenReturn(list);

        assertThat(interviewService.getByCandidate(101)).hasSize(2);
    }

    @Test
    @DisplayName("getByStatus — filters by status")
    void getByStatus_filters() {
        when(interviewRepository.findByStatus("SCHEDULED"))
                .thenReturn(List.of(buildInterview(1, "SCHEDULED")));

        assertThat(interviewService.getByStatus("SCHEDULED")).hasSize(1);
    }

    @Test
    @DisplayName("scheduleInterview — publishes RabbitMQ event")
    void scheduleInterview_publishesEvent() {
        Interview interview = buildInterview(0, "DRAFT");
        Interview saved = buildInterview(1, "SCHEDULED");
        when(interviewRepository.save(any())).thenReturn(saved);

        interviewService.scheduleInterview(interview);

        verify(rabbitTemplate, atLeastOnce()).convertAndSend(any(), any(), any(String.class));
    }
}
