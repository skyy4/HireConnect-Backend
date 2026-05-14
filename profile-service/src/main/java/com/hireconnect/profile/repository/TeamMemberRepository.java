package com.hireconnect.profile.repository;

import com.hireconnect.profile.pojo.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {

    List<TeamMember> findByRecruiterId(int recruiterId);

    List<TeamMember> findByRecruiterIdAndStatus(int recruiterId, String status);

    Optional<TeamMember> findByRecruiterIdAndMemberUserId(int recruiterId, int memberUserId);

    boolean existsByRecruiterIdAndMemberUserId(int recruiterId, int memberUserId);

    List<TeamMember> findByMemberUserId(int memberUserId);

    void deleteByRecruiterIdAndMemberUserId(int recruiterId, int memberUserId);

    long countByRecruiterIdAndStatus(int recruiterId, String status);
}
