package com.report.demo.Repository;

import com.report.demo.entity.StudentScoreDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentScoreDetailRepository extends JpaRepository<StudentScoreDetail, Long> {
}
