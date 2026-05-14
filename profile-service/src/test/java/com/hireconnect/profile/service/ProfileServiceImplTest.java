package com.hireconnect.profile.service;

import com.hireconnect.profile.pojo.CandidateProfile;
import com.hireconnect.profile.pojo.RecruiterProfile;
import com.hireconnect.profile.repository.CandidateProfileRepository;
import com.hireconnect.profile.repository.ParsedResumeRepository;
import com.hireconnect.profile.repository.RecruiterProfileRepository;
import com.hireconnect.profile.repository.TeamMemberRepository;
import com.hireconnect.profile.util.ResumeParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileServiceImpl Unit Tests")
class ProfileServiceImplTest {

    @Mock private CandidateProfileRepository candidateRepo;
    @Mock private RecruiterProfileRepository recruiterRepo;
    @Mock private ParsedResumeRepository parsedResumeRepo;
    @Mock private TeamMemberRepository teamMemberRepo;
    @Mock private ResumeParser resumeParser;

    @InjectMocks
    private ProfileServiceImpl profileService;

    // ── Candidate Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("addCandidateProfile — saves profile when userId not duplicate")
    void addCandidateProfile_success() {
        CandidateProfile profile = CandidateProfile.builder()
                .userId(101)
                .fullName("Alice Smith")
                .email("alice@example.com")
                .build();

        when(candidateRepo.existsByUserId(101)).thenReturn(false);
        when(candidateRepo.save(any())).thenReturn(profile);

        CandidateProfile result = profileService.addCandidateProfile(profile);

        assertThat(result.getFullName()).isEqualTo("Alice Smith");
        verify(candidateRepo).save(profile);
    }

    @Test
    @DisplayName("addCandidateProfile — throws when duplicate userId")
    void addCandidateProfile_duplicateUserId_throws() {
        CandidateProfile profile = CandidateProfile.builder().userId(101).fullName("Bob").email("bob@x.com").build();
        when(candidateRepo.existsByUserId(101)).thenReturn(true);

        assertThatThrownBy(() -> profileService.addCandidateProfile(profile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Profile already exists");
    }

    @Test
    @DisplayName("getCandidateByUserId — returns profile when exists")
    void getCandidateByUserId_found() {
        CandidateProfile profile = CandidateProfile.builder().userId(5).fullName("Carol").email("carol@x.com").build();
        when(candidateRepo.findByUserId(5)).thenReturn(Optional.of(profile));

        CandidateProfile result = profileService.getCandidateByUserId(5);
        assertThat(result.getFullName()).isEqualTo("Carol");
    }

    @Test
    @DisplayName("getCandidateByUserId — throws when not found")
    void getCandidateByUserId_notFound_throws() {
        when(candidateRepo.findByUserId(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getCandidateByUserId(999))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getAllCandidates — returns all from repository")
    void getAllCandidates_returnsList() {
        List<CandidateProfile> list = List.of(
                CandidateProfile.builder().userId(1).fullName("A").email("a@x.com").build(),
                CandidateProfile.builder().userId(2).fullName("B").email("b@x.com").build()
        );
        when(candidateRepo.findAll()).thenReturn(list);

        List<CandidateProfile> result = profileService.getAllCandidates();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("deleteCandidateProfile — calls deleteById")
    void deleteCandidateProfile_callsRepo() {
        doNothing().when(candidateRepo).deleteById(anyInt());
        profileService.deleteCandidateProfile(1);
        verify(candidateRepo).deleteById(1);
    }

    // ── Recruiter Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("addRecruiterProfile — saves when userId not duplicate")
    void addRecruiterProfile_success() {
        RecruiterProfile profile = RecruiterProfile.builder()
                .userId(200)
                .fullName("Dave Recruiter")
                .email("dave@corp.com")
                .build();

        when(recruiterRepo.existsByUserId(200)).thenReturn(false);
        when(recruiterRepo.save(any())).thenReturn(profile);

        RecruiterProfile result = profileService.addRecruiterProfile(profile);
        assertThat(result.getFullName()).isEqualTo("Dave Recruiter");
    }

    @Test
    @DisplayName("addRecruiterProfile — throws on duplicate userId")
    void addRecruiterProfile_duplicate_throws() {
        RecruiterProfile profile = RecruiterProfile.builder().userId(200).fullName("X").email("x@x.com").build();
        when(recruiterRepo.existsByUserId(200)).thenReturn(true);

        assertThatThrownBy(() -> profileService.addRecruiterProfile(profile))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("getRecruiterByUserId — throws when not found")
    void getRecruiterByUserId_notFound_throws() {
        when(recruiterRepo.findByUserId(anyInt())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> profileService.getRecruiterByUserId(1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("isTeamMember — delegates to repository")
    void isTeamMember_delegatesToRepo() {
        when(teamMemberRepo.existsByRecruiterIdAndMemberUserId(10, 20)).thenReturn(true);
        assertThat(profileService.isTeamMember(10, 20)).isTrue();
    }
}
