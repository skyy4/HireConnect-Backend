package com.hireconnect.profile.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.profile.pojo.CandidateProfile;
import com.hireconnect.profile.pojo.RecruiterProfile;
import com.hireconnect.profile.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileResource.class)
@DisplayName("ProfileResource Controller Tests")
class ProfileResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ProfileService profileService;

    private CandidateProfile buildCandidate(int id, int userId) {
        return CandidateProfile.builder()
                .profileId(id)
                .userId(userId)
                .fullName("Alice Smith")
                .email("alice@test.com")
                .build();
    }

    private RecruiterProfile buildRecruiter(int id, int userId) {
        return RecruiterProfile.builder()
                .profileId(id)
                .userId(userId)
                .fullName("Bob Corp")
                .email("bob@corp.com")
                .companyName("BobCo")
                .build();
    }

    // ── POST /api/v1/profiles/candidates ─────────────────────────────────

    @Test
    @DisplayName("POST /candidates — creates candidate profile and returns 201")
    void createCandidate_validPayload_returns201() throws Exception {
        CandidateProfile profile = buildCandidate(0, 10);
        CandidateProfile saved   = buildCandidate(1, 10);

        when(profileService.addCandidateProfile(any(CandidateProfile.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/profiles/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(profile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId", is(1)))
                .andExpect(jsonPath("$.fullName", is("Alice Smith")));
    }

    @Test
    @DisplayName("POST /candidates — returns 409 when duplicate userId")
    void createCandidate_duplicate_returns409() throws Exception {
        CandidateProfile profile = buildCandidate(0, 10);
        when(profileService.addCandidateProfile(any()))
                .thenThrow(new IllegalStateException("Profile already exists"));

        mockMvc.perform(post("/api/v1/profiles/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(profile)))
                .andExpect(status().is4xxClientError());
    }

    // ── GET /api/v1/profiles/candidates/{profileId} ───────────────────────

    @Test
    @DisplayName("GET /candidates/{profileId} — returns candidate profile")
    void getCandidate_found_returns200() throws Exception {
        when(profileService.getCandidateById(1)).thenReturn(buildCandidate(1, 10));

        mockMvc.perform(get("/api/v1/profiles/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId", is(1)))
                .andExpect(jsonPath("$.email", is("alice@test.com")));
    }

    @Test
    @DisplayName("GET /candidates/{profileId} — returns 404 when not found")
    void getCandidate_notFound_returns404() throws Exception {
        when(profileService.getCandidateById(999))
                .thenThrow(new IllegalArgumentException("Not found"));

        mockMvc.perform(get("/api/v1/profiles/candidates/999"))
                .andExpect(status().is4xxClientError());
    }

    // ── GET /api/v1/profiles/candidates ───────────────────────────────────

    @Test
    @DisplayName("GET /candidates — returns list of all candidates")
    void getAllCandidates_returns200() throws Exception {
        when(profileService.getAllCandidates()).thenReturn(List.of(
                buildCandidate(1, 10),
                buildCandidate(2, 11)
        ));

        mockMvc.perform(get("/api/v1/profiles/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ── DELETE /api/v1/profiles/candidates/{profileId} ────────────────────

    @Test
    @DisplayName("DELETE /candidates/{profileId} — returns 200 with message")
    void deleteCandidate_returns200() throws Exception {
        doNothing().when(profileService).deleteCandidateProfile(1);

        mockMvc.perform(delete("/api/v1/profiles/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Candidate profile deleted")));
    }

    // ── POST /api/v1/profiles/recruiters ──────────────────────────────────

    @Test
    @DisplayName("POST /recruiters — creates recruiter profile and returns 201")
    void createRecruiter_validPayload_returns201() throws Exception {
        RecruiterProfile profile = buildRecruiter(0, 20);
        RecruiterProfile saved   = buildRecruiter(5, 20);

        when(profileService.addRecruiterProfile(any(RecruiterProfile.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/profiles/recruiters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(profile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId", is(5)))
                .andExpect(jsonPath("$.companyName", is("BobCo")));
    }

    // ── GET /api/v1/profiles/recruiters/user/{userId} ─────────────────────

    @Test
    @DisplayName("GET /recruiters/user/{userId} — returns recruiter by userId")
    void getRecruiterByUser_found_returns200() throws Exception {
        when(profileService.getRecruiterByUserId(20)).thenReturn(buildRecruiter(5, 20));

        mockMvc.perform(get("/api/v1/profiles/recruiters/user/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(20)));
    }
}
