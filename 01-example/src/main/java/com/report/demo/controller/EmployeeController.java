package com.report.demo.controller;


import com.report.demo.model.Employee;
import com.report.demo.repository.EmployeeRepository;
import com.report.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final ReportService reportService;

    @GetMapping("/report/{format}")
    public String generateReport(@PathVariable String format) throws IOException, JRException {
        return reportService.exportReport(format);
    }

    @GetMapping("/employee")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }
}
