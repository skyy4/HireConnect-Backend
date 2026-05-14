package com.hireconnect.application.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationResource Controller Tests")
class ApplicationResourceTest {

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Mock private ApplicationService applicationService;
    @InjectMocks private ApplicationResource applicationResource;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(applicationResource).build();
    }

    private Application buildApp(int id, String status) {
        Application a = new Application();
        a.setApplicationId(id);
        a.setJobId(10);
        a.setCandidateId(5);
        a.setStatus(status);
        a.setAppliedAt(LocalDateTime.now());
        return a;
    }

    // ── POST /api/v1/applications ───────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/applications — creates application and returns 201")
    void submit_validPayload_returns201() throws Exception {
        Application saved = buildApp(1, "APPLIED");
        when(applicationService.submitApplication(any(Application.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(buildApp(0, "APPLIED"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId", is(1)))
                .andExpect(jsonPath("$.status", is("APPLIED")));
    }

    // ── GET /api/v1/applications/{id} ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/applications/{id} — returns application with 200")
    void getById_found_returns200() throws Exception {
        when(applicationService.getApplicationById(3)).thenReturn(buildApp(3, "SHORTLISTED"));

        mockMvc.perform(get("/api/v1/applications/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId", is(3)))
                .andExpect(jsonPath("$.status", is("SHORTLISTED")));
    }

    // ── GET /api/v1/applications/candidate/{candidateId} ───────────────────

    @Test
    @DisplayName("GET /api/v1/applications/candidate/{id} — returns all applications for candidate")
    void getByCandidate_returns200WithList() throws Exception {
        when(applicationService.getByCandidate(5))
                .thenReturn(List.of(buildApp(1, "APPLIED"), buildApp(2, "SHORTLISTED")));

        mockMvc.perform(get("/api/v1/applications/candidate/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/applications/candidate/{id} — returns empty list")
    void getByCandidate_empty_returns200() throws Exception {
        when(applicationService.getByCandidate(99)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/applications/candidate/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/v1/applications/job/{jobId} ───────────────────────────────

    @Test
    @DisplayName("GET /api/v1/applications/job/{id} — returns all applications for job")
    void getByJob_returns200() throws Exception {
        when(applicationService.getByJob(10))
                .thenReturn(List.of(buildApp(1, "APPLIED"), buildApp(2, "APPLIED")));

        mockMvc.perform(get("/api/v1/applications/job/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/applications/job/{id}?status=SHORTLISTED — filters by status")
    void getByJobAndStatus_returns200() throws Exception {
        when(applicationService.getByJobAndStatus(10, "SHORTLISTED"))
                .thenReturn(List.of(buildApp(1, "SHORTLISTED")));

        mockMvc.perform(get("/api/v1/applications/job/10").param("status", "SHORTLISTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("SHORTLISTED")));
    }

    // ── PATCH /api/v1/applications/{id}/status ──────────────────────────────

    @Test
    @DisplayName("PATCH /api/v1/applications/{id}/status — updates to SHORTLISTED with note")
    void updateStatus_returns200() throws Exception {
        Application updated = buildApp(1, "SHORTLISTED");
        // Interface: updateStatus(int applicationId, String status, String note)
        when(applicationService.updateStatus(eq(1), eq("SHORTLISTED"), anyString())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\",\"note\":\"Good profile\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHORTLISTED")));
    }

    @Test
    @DisplayName("PATCH /api/v1/applications/{id}/status — updates to REJECTED without note")
    void updateStatus_rejected_returns200() throws Exception {
        Application updated = buildApp(2, "REJECTED");
        when(applicationService.updateStatus(eq(2), eq("REJECTED"), isNull())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/applications/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    // ── PATCH /api/v1/applications/{id}/withdraw ────────────────────────────

    @Test
    @DisplayName("PATCH /api/v1/applications/{id}/withdraw — withdraws application")
    void withdraw_returns200WithMessage() throws Exception {
        // Interface: withdrawApplication(int applicationId, int candidateId)
        doNothing().when(applicationService).withdrawApplication(eq(1), eq(5));

        mockMvc.perform(patch("/api/v1/applications/1/withdraw")
                        .param("candidateId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("withdrawn")));
    }

    // ── GET /api/v1/applications/check ─────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/applications/check — returns true when already applied")
    void hasApplied_true_returns200() throws Exception {
        when(applicationService.hasApplied(10, 5)).thenReturn(true);

        mockMvc.perform(get("/api/v1/applications/check")
                        .param("jobId", "10")
                        .param("candidateId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applied", is(true)));
    }

    @Test
    @DisplayName("GET /api/v1/applications/check — returns false when not applied")
    void hasApplied_false_returns200() throws Exception {
        when(applicationService.hasApplied(10, 99)).thenReturn(false);

        mockMvc.perform(get("/api/v1/applications/check")
                        .param("jobId", "10")
                        .param("candidateId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applied", is(false)));
    }

    // ── GET /api/v1/applications/job/{id}/count ─────────────────────────────

    @Test
    @DisplayName("GET /api/v1/applications/job/{id}/count — returns application count")
    void countByJob_returns200() throws Exception {
        when(applicationService.countByJob(10)).thenReturn(7L);

        mockMvc.perform(get("/api/v1/applications/job/10/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(7)));
    }

    // ── GET /api/v1/applications/count ─────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/applications/count — returns total count")
    void countAll_returns200() throws Exception {
        when(applicationService.countAll()).thenReturn(120L);

        mockMvc.perform(get("/api/v1/applications/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(120)));
    }
}
