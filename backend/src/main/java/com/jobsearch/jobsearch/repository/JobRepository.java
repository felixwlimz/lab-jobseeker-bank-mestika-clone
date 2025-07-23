package com.jobsearch.repository;

import com.jobsearch.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    List<Job> findByCompanyId(Long companyId);
    
    @Query(value = "SELECT * FROM Job j WHERE j.title LIKE CONCAT('%', :keyword, '%') OR j.description LIKE CONCAT('%', :keyword, '%')")
    List<Job> searchJobs(@Param("keyword") String keyword);
    
    List<Job> findByStatus(String status);

    List<Job> findByTitleContainingIgnoreCase(String keyword);
}
