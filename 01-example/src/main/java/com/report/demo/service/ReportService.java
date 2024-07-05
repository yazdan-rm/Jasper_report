package com.report.demo.service;

import com.report.demo.model.Employee;
import com.report.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EmployeeRepository employeeRepository;

    public String exportReport(String reportFormat) throws FileNotFoundException, JRException {
        String path = "C:\\Users\\Asus\\Desktop\\spring-boot-3-spring-6-hibernate\\Japser Report tutorial\\01-example\\src\\main\\resources\\static";
        List<Employee> employees = employeeRepository.findAll();

        // Load JRXML file and compile it
        File file = ResourceUtils.getFile("classpath:employees.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

        // Create data source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);

        // Set parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "Java Techie");

        // Fill the report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Export report
        if (reportFormat.equalsIgnoreCase("html")) {
            JasperExportManager.exportReportToHtmlFile(jasperPrint, path + "\\employees.html");
        } else if (reportFormat.equalsIgnoreCase("pdf")) {
            JasperExportManager.exportReportToPdfFile(jasperPrint, path + "\\employees.pdf");
        }

        return "Report generated in path: " + path;
    }
}
