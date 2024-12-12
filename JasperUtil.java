package ir.rbp.nabreport.common.util;

import ir.rbp.nabcore.controller.exception.CustomParameterizeException;
import ir.rbp.nabcore.controller.exception.ErrorConstantsCore;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

/**
 * @author Mohammad taghavi.
 */
public class JasperUtil {

    public enum DocumentType {
        PDF("pdf"), XLSX("xlsx"), CSV("csv"),
        HTML("html"), XML("xml"), PNG("png"),
        JPG("jpg"), DOCX("docx");
        private String name;

        DocumentType(String name) {
            this.name = name;
        }

        public static DocumentType getByName(String name) {
            Optional<DocumentType> enumeration = Arrays.stream(DocumentType.values())
                    .filter(i -> i.getName().equals(name))
                    .findFirst();

            return enumeration.orElse(DocumentType.XLSX);

        }

        public String getName() {
            return name;
        }
    }
    
    /**
     * @param jasperXMLFileInputStream
     * @param parameters
     * @param dataList
     * @param documentType
     * @return
     * @throws JRException
     * @throws IOException
     */
    public static List<byte[]> export(ClassPathResource jasperXMLFileInputStream, Map<String, Object> parameters, Collection dataList, DocumentType documentType) throws JRException, IOException {

        JasperPrint print = getJasperPrint(jasperXMLFileInputStream, parameters, Optional.of(dataList), Optional.empty(), documentType);
        return generateBytesData(print, documentType);

    }
    public static List<byte[]> export(ClassPathResource jasperXMLFileInputStream, Map<String, Object> parameters, Collection dataList, DocumentType documentType,String passwordPdf) throws JRException, IOException {

        JasperPrint print = getJasperPrint(jasperXMLFileInputStream, parameters, Optional.of(dataList), Optional.empty(), documentType);
        if(documentType.equals(DocumentType.PDF) && !passwordPdf.equals("") ) {
            print.setProperty("net.sf.jasperreports.export.pdf.encrypted", "true");
            print.setProperty("net.sf.jasperreports.export.pdf.128.bit.key", "true");
            print.setProperty("net.sf.jasperreports.export.pdf.owner.password", passwordPdf);
            print.setProperty("net.sf.jasperreports.export.pdf.user.password", passwordPdf);
            /*print.setProperty("net.sf.jasperreports.export.pdf.permissions.allowed" ,"PRINTING");*/
        }
        return generateBytesData(print, documentType);

    }

    public static List<byte[]> exportForMultipleDatasets(ClassPathResource jasperXMLFileInputStream,
                                                         Map<String, Object> parameters,
                                                         Collection<List<?>> dataList,
                                                         DocumentType documentType,String passwordPdf) throws JRException, IOException {

        JasperPrint print =
                getJasperPrintForMultipleDatasets(jasperXMLFileInputStream, parameters, Optional.of(dataList), Optional.empty());
        if(documentType.equals(DocumentType.PDF) && !passwordPdf.equals("") ) {
            print.setProperty("net.sf.jasperreports.export.pdf.encrypted", "true");
            print.setProperty("net.sf.jasperreports.export.pdf.128.bit.key", "true");
            print.setProperty("net.sf.jasperreports.export.pdf.owner.password", passwordPdf);
            print.setProperty("net.sf.jasperreports.export.pdf.user.password", passwordPdf);
            /*print.setProperty("net.sf.jasperreports.export.pdf.permissions.allowed" ,"PRINTING");*/
        }
        return generateBytesData(print, documentType);

    }

    /**
     * @param jasperXMLFileInputStream
     * @param parameters
     * @param documentType
     * @return
     * @throws JRException
     * @throws IOException
     */
    public static List<byte[]> export(ClassPathResource jasperXMLFileInputStream, Map<String, Object> parameters, DocumentType documentType) throws JRException, IOException {
        JasperPrint print = getJasperPrint(jasperXMLFileInputStream, parameters);
        return generateBytesData(print, documentType);

    }

    /**
     * @param jasperXMLFileInputStream
     * @param parameters
     * @param connection
     * @param documentType
     * @return
     * @throws JRException
     * @throws IOException
     */
    public static List<byte[]> export(ClassPathResource jasperXMLFileInputStream, Map<String, Object> parameters, Connection connection, DocumentType documentType) throws JRException, IOException {
        JasperPrint print = getJasperPrint(jasperXMLFileInputStream, parameters, Optional.empty(), Optional.of(connection), documentType);
        return generateBytesData(print, documentType);
    }

    /**
     *
     * @param jasperXMLFileInputStream
     * @param parameters
     * @param beanDataSource
     * @param documentType
     * @return
     * @throws JRException
     * @throws IOException
     */
    public static byte[] export(ClassPathResource jasperXMLFileInputStream, Map<String, Object> parameters,JRBeanCollectionDataSource beanDataSource, DocumentType documentType) throws JRException, IOException {

        JasperReport report = JasperCompileManager.compileReport(jasperXMLFileInputStream.getInputStream());
        JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanDataSource);
        return JasperUtil.generateBytesData(jasperPrint, documentType).iterator().next();
    }


    /**
     * @param print
     * @param documentType
     * @return
     * @throws IOException
     * @throws JRException
     */
    private static List<byte[]> generateBytesData(JasperPrint print, DocumentType documentType) throws IOException, JRException {
        if (documentType.equals(DocumentType.PNG) || documentType.equals(DocumentType.JPG)) {
            return imagePages(print, documentType);
        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Exporter exporter;

        switch (documentType) {
            case HTML:
                exporter = new HtmlExporter();
                exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
                break;

            case CSV:
                exporter = new JRCsvExporter();
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                break;

            case XML:
                exporter = new JRXmlExporter();
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                break;

            case XLSX:
                exporter = new JRXlsxExporter();
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                break;

            case PDF:
                exporter = new JRPdfExporter();
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                break;

            case DOCX:
                exporter = new JRDocxExporter();
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                break;

            default:
                throw new JRException("UnSupport DocumentType");
        }

        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.exportReport();

        return Arrays.asList(outputStream.toByteArray());
    }

    /**
     * @param jasperXMLFileInputStream
     * @param parameters
     * @param collection
     * @param connection
     * @return
     * @throws JRException
     */
    private static JasperPrint getJasperPrint(ClassPathResource jasperXMLFileInputStream, Map<String, Object> parameters, Optional<Collection> collection, Optional<Connection> connection, DocumentType documentType) throws JRException, IOException {
        JasperPrint jasperPrint = null;
        JasperReport report = null;

        report = JasperCompileManager.compileReport(jasperXMLFileInputStream.getInputStream());

        if (collection.isPresent()) {
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(collection.get());
            parameters.put("DS1", beanColDataSource);
            jasperPrint = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
        } else if (connection.isPresent()) {
            jasperPrint = JasperFillManager.fillReport(report, parameters, connection.get());
        }
        if (jasperPrint == null) {
            throw new RuntimeException("data source is not present");
        }
        return jasperPrint;
    }

    private static JasperPrint getJasperPrint(ClassPathResource jasperXMLFileInputStream, Map<String, Object> parameters) throws JRException, IOException {
        JasperReport report = JasperCompileManager.compileReport(jasperXMLFileInputStream.getInputStream());
        JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

        if (jasperPrint == null) {
            throw new CustomParameterizeException(ErrorConstantsCore.ERR_INTERNAL_SERVER);
        }

        return jasperPrint;
    }

    private static JasperPrint getJasperPrintForMultipleDatasets(ClassPathResource jasperXMLFileInputStream,
                                                                 Map<String, Object> parameters,
                                                                 Optional<Collection<List<?>>> collection,
                                                                 Optional<Connection> connection) throws JRException, IOException {
        JasperPrint jasperPrint = null;
        JasperReport report = JasperCompileManager.compileReport(jasperXMLFileInputStream.getInputStream());

        if (collection.isPresent()) {
            List<List<?>> listCollection = (List<List<?>>) collection.get();
            for (int i = 0; i < listCollection.size(); i++) {
                JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(listCollection.get(i));
                parameters.put("DS"+(i+1), beanColDataSource);
            }
            jasperPrint = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
        } else if (connection.isPresent()) {
            jasperPrint = JasperFillManager.fillReport(report, parameters, connection.get());
        }
        if (jasperPrint == null) {
            throw new RuntimeException("data sources are not present");
        }
        return jasperPrint;
    }


    /**
     * @param jasperPrint
     * @param documentType
     * @return
     * @throws JRException
     * @throws IOException
     */
    private static List<byte[]> imagePages(JasperPrint jasperPrint, DocumentType documentType) throws JRException, IOException {
        List<byte[]> images = new ArrayList<>();

        final float zoom = 2f;

        String extension = documentType.equals(DocumentType.PNG) ? "png" : "jpg";

        // get count of pages
        int pages = jasperPrint.getPages().size();

        // convert each page into image
        for (int i = 0; i < pages; i++) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                BufferedImage image = (BufferedImage) JasperPrintManager.printPageToImage(jasperPrint, i, zoom);
                ImageIO.write(image, extension, outputStream);
                images.add(outputStream.toByteArray());
            }
        }

        return images;
    }

}
