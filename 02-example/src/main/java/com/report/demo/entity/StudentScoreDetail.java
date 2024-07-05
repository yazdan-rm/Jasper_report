package com.report.demo.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name= "STUDENT_SCORE_DETAIL")
@Setter
@Getter
@RequiredArgsConstructor
public class StudentScoreDetail {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "SUBJECT_NAME")
    private String subjectName;

    @Column(name = "TOTAL_MARKS")
    private Double totalMarks;

    @Column(name = "MARKS_OBTAINED")
    private Double marksObtained;
}
