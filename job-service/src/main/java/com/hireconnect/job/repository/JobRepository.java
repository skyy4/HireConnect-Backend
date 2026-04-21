package com.hireconnect.job.repository;

import com.hireconnect.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {

    List<Job> findByTitleContainingIgnoreCase(String title);

    List<Job> findByCategory(String category);

    List<Job> findByLocationContainingIgnoreCase(String location);

    List<Job> findByPostedBy(int recruiterId);

    List<Job> findByStatus(String status);

    List<Job> findByType(String type);

    List<Job> findByExperienceLevel(String level);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' " +
           "AND (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:category IS NULL OR j.category = :category) " +
           "AND (:type IS NULL OR j.type = :type) " +
           "AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel) " +
           "AND (:minSalary IS NULL OR j.salaryMin >= :minSalary) " +
           "AND (:maxSalary IS NULL OR j.salaryMax <= :maxSalary)")
    List<Job> searchJobs(
            @Param("title") String title,
            @Param("location") String location,
            @Param("category") String category,
            @Param("type") String type,
            @Param("experienceLevel") String experienceLevel,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary);

    List<Job> findBySkillsContaining(String skill);

    long countByPostedBy(int recruiterId);

    long countByPostedByAndStatus(int recruiterId, String status);

    long countByStatus(String status);
}
