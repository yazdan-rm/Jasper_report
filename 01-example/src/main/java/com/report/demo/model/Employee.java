package com.report.demo.model;

import jakarta.persistence.*;
import lombok.*;


@RequiredArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "employee_tbl")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "designation")
    private String designation;

    @Column(name = "salary")
    private double salary;

    @Column(name = "doj")
    private String doj;
}
