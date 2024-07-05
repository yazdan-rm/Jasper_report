package com.report.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "STUDENT_DETAILS")
@Setter
@Getter
@RequiredArgsConstructor
public class StudentDetails {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "YEAR")
    private String year;

    @Column(name = "ADVISOR")
    private String advisor;

    @Column(name = "GRADING_PERIOD")
    private String gradingPeriod;
}
