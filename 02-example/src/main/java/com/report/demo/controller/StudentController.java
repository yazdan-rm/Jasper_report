package com.report.demo.controller;


import com.report.demo.Repository.StudentDetailsRepository;
import com.report.demo.Repository.StudentScoreDetailRepository;
import com.report.demo.entity.StudentDetails;
import com.report.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/rest")
@RequiredArgsConstructor
public class StudentController {

    private final ReportService reportService;
    private final StudentDetailsRepository studentDetailsRepository;
    private final StudentScoreDetailRepository studentScoreDetailRepository;

    @GetMapping("/student")
    public ResponseEntity<List<StudentDetails>> getStudentDetails(){
        return ResponseEntity.ok(studentDetailsRepository.findAll());
    }


    @GetMapping("/report/{format}")
    public ResponseEntity<String> exportReport(@PathVariable String format) throws JRException, FileNotFoundException {
        return ResponseEntity.ok(reportService.exportReport(format));
    }

}
