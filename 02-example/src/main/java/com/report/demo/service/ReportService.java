package com.report.demo.service;


import com.report.demo.Repository.StudentDetailsRepository;
import com.report.demo.Repository.StudentScoreDetailRepository;
import com.report.demo.entity.StudentDetails;
import com.report.demo.entity.StudentScoreDetail;
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

    private final StudentDetailsRepository studentDetailsRepository;
    private final StudentScoreDetailRepository studentScoreDetailRepository;

    public String exportReport(String reportFormat) throws FileNotFoundException, JRException {
        String path = "C:\\Users\\Asus\\Desktop\\spring-boot-3-spring-6-hibernate\\Japser Report tutorial\\02-example\\src\\main\\resources\\static\\export.pdf";
        List<StudentDetails> studentDetailsList = studentDetailsRepository.findAll();
        List<StudentScoreDetail> studentScoreDetailList = studentScoreDetailRepository.findAll();

        // Load JRXML file and compile it
        File file = ResourceUtils.getFile("classpath:02-sample.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

        // Create data source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(studentDetailsList);
        JRBeanCollectionDataSource tableDataSource = new JRBeanCollectionDataSource(studentScoreDetailList);

        // put data for table
        Map<String, Object> parameter = new HashMap<>();

        parameter.put("TABLE_DATA_SOURCE", tableDataSource);

        // Create Jasper Report Object
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameter, dataSource);

        // Export our report
        JasperExportManager.exportReportToPdfFile(jasperPrint, path);

        return "Report generated successfully!";

    }
}
