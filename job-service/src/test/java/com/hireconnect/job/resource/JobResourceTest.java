package com.hireconnect.job.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.job.entity.Bookmark;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.service.JobService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobResource Controller Tests")
class JobResourceTest {

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Mock private JobService jobService;
    @InjectMocks private JobResource jobResource;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobResource).build();
    }

    private Job buildJob(int id) {
        Job j = new Job();
        j.setJobId(id);
        j.setTitle("Software Engineer");
        j.setCompanyName("TechCorp");  // correct field from entity
        j.setLocation("Bangalore");
        j.setStatus("ACTIVE");
        j.setPostedBy(1);
        j.setPostedAt(LocalDateTime.now());
        return j;
    }

    // ── GET /api/v1/jobs ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/jobs — returns list of active jobs with 200")
    void getAllJobs_returns200WithList() throws Exception {
        when(jobService.getAllJobs()).thenReturn(List.of(buildJob(1), buildJob(2)));

        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Software Engineer")));
    }

    @Test
    @DisplayName("GET /api/v1/jobs — returns empty list when no jobs")
    void getAllJobs_empty_returns200() throws Exception {
        when(jobService.getAllJobs()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/v1/jobs/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/jobs/{id} — returns job with 200")
    void getJobById_found_returns200() throws Exception {
        Job job = buildJob(5);
        when(jobService.recordView(eq(5), any(), any(), any())).thenReturn(null);
        when(jobService.getJobById(5)).thenReturn(job);

        mockMvc.perform(get("/api/v1/jobs/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId", is(5)))
                .andExpect(jsonPath("$.title", is("Software Engineer")));
    }

    // ── POST /api/v1/jobs ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/jobs — creates job and returns 201")
    void createJob_validPayload_returns201() throws Exception {
        Job saved = buildJob(1);
        when(jobService.addJob(any(Job.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(buildJob(0))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId", is(1)));
    }

    // ── PUT /api/v1/jobs/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/v1/jobs/{id} — updates job and returns 200")
    void updateJob_validPayload_returns200() throws Exception {
        Job updated = buildJob(3);
        updated.setTitle("Senior Engineer");
        when(jobService.updateJob(eq(3), any(Job.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/jobs/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Senior Engineer")));
    }

    // ── DELETE /api/v1/jobs/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/v1/jobs/{id} — deletes job and returns message")
    void deleteJob_returns200WithMessage() throws Exception {
        doNothing().when(jobService).deleteJob(1);

        mockMvc.perform(delete("/api/v1/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Job deleted successfully")));
    }

    // ── PATCH /api/v1/jobs/{id}/status ─────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/v1/jobs/{id}/status — updates status to CLOSED")
    void updateStatus_returns200() throws Exception {
        Job job = buildJob(1);
        job.setStatus("CLOSED");
        when(jobService.updateJobStatus(eq(1), eq("CLOSED"))).thenReturn(job);

        mockMvc.perform(patch("/api/v1/jobs/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    // ── GET /api/v1/jobs/search ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/jobs/search — returns filtered results")
    void searchJobs_withTitle_returns200() throws Exception {
        when(jobService.searchJobs(eq("Engineer"), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(buildJob(1)));

        mockMvc.perform(get("/api/v1/jobs/search").param("title", "Engineer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ── GET /api/v1/jobs/count ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/jobs/count — returns total job count")
    void countJobs_returns200WithCount() throws Exception {
        when(jobService.countAllJobs()).thenReturn(42L);

        mockMvc.perform(get("/api/v1/jobs/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(42)));
    }

    @Test
    @DisplayName("GET /api/v1/jobs/count?status=ACTIVE — returns filtered count")
    void countJobsByStatus_returns200() throws Exception {
        when(jobService.countJobsByStatus("ACTIVE")).thenReturn(10L);

        mockMvc.perform(get("/api/v1/jobs/count").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(10)));
    }

    // ── Bookmarks ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/jobs/bookmarks/candidate/{id} — returns bookmarks")
    void getBookmarks_returns200() throws Exception {
        Bookmark bm = Bookmark.builder().candidateId(1).jobId(5).savedAt(LocalDateTime.now()).build();
        when(jobService.getBookmarksByCandidate(1)).thenReturn(List.of(bm));

        mockMvc.perform(get("/api/v1/jobs/bookmarks/candidate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/jobs/bookmarks/check — returns true when bookmarked")
    void isBookmarked_returns200() throws Exception {
        when(jobService.isBookmarked(1, 5)).thenReturn(true);

        mockMvc.perform(get("/api/v1/jobs/bookmarks/check")
                        .param("candidateId", "1")
                        .param("jobId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked", is(true)));
    }

    @Test
    @DisplayName("DELETE /api/v1/jobs/bookmarks/{cId}/{jId} — removes bookmark")
    void removeBookmark_returns200() throws Exception {
        doNothing().when(jobService).removeBookmark(1, 5);

        mockMvc.perform(delete("/api/v1/jobs/bookmarks/1/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Bookmark removed successfully")));
    }

    // ── View counts ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/jobs/{id}/views/count — returns view count")
    void getViewCount_returns200() throws Exception {
        when(jobService.getViewCount(1)).thenReturn(150L);

        mockMvc.perform(get("/api/v1/jobs/1/views/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount", is(150)));
    }
}
