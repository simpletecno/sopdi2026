/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.simpletecno.sopdi.HeaderFooterPageEvent;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
public class ConsultaRecursoDiarioPDF extends Window {

    public static Statement stQuery;
    public static PreparedStatement stPreparedQuery;
    public static ResultSet rsRecords;

    static final DecimalFormat df1 = new DecimalFormat("###,##0.00");

    String fileName;
    public IndexedContainer asistenciaContainer;
    String fechaInicio;
    String fechaFin;

    public ConsultaRecursoDiarioPDF(
            String fechaInicio,
            String fechaFin,
            String totalRh,
            IndexedContainer asistenciaContainer
    ) {

        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.asistenciaContainer = asistenciaContainer;

        try {

            setWidth("95%");
            setHeight("95%");
            center();

            BrowserFrame browser = new BrowserFrame();
            browser.setSizeFull();

            VerticalLayout mainLayout = new VerticalLayout();
            mainLayout.setSpacing(true);
            mainLayout.addStyleName("rcorners3");
            mainLayout.setMargin(true);
            mainLayout.setWidth("100%");
            mainLayout.setHeight("100%");

            VerticalLayout browserLayout = new VerticalLayout();
            browserLayout.addStyleName("rcorners2");
            browserLayout.setSizeUndefined();
            browserLayout.setHeight("90%");
            browserLayout.setWidth("100%");

            fileName = "Disponibilidad_RH_" + new Utileria().getFechaHoraSinFormato() + ".pdf";

            Pdf pdf = new Pdf(fileName);

            StreamResource pdfResource;
            pdfResource = new StreamResource(new Pdf(fileName), fileName);
            pdfResource.setMIMEType("application/pdf");

            fileName = VaadinService.getCurrent()
                    .getBaseDirectory().getAbsolutePath() + "/pdfreceipts/" + fileName;

            //File file = new File(fileName);
            // file.delete();
            FileUtils.writeByteArrayToFile(new File(fileName), pdf.os.toByteArray());

            browser.setSource(pdfResource);

            browserLayout.addComponent(browser);
            browserLayout.setComponentAlignment(browser, Alignment.MIDDLE_CENTER);

            mainLayout.addComponent(browserLayout);
            mainLayout.setComponentAlignment(browserLayout, Alignment.TOP_CENTER);

            setContent(mainLayout);

        } catch (Exception allEx) {

            Notification.show("Error al generar el reporte de disponibilidad de RH diaria.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }

    /**
     * This class creates a PDF with the iText library. This class implements
     * the StreamSource interface which defines the getStream method.
     */
    public class Pdf implements StreamResource.StreamSource, Serializable {

        private final String RECEIPTFILE
                = VaadinService.getCurrent()
                        .getBaseDirectory().getAbsolutePath() + "/pdfreceipts/";
        private String TDLOGO = VaadinService.getCurrent()
                .getBaseDirectory().getAbsolutePath() + "/VAADIN/themes/tests-valo-flat/img/logo_simpletecno.png";

        private final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        private final Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
        private final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
        private final Font smallBold14 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        private final Font smallBold10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        private final Font fuenteMagenta36 = new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.BOLD, BaseColor.MAGENTA);
        private final Font fuenteNegra12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);
        private final Font fuenteVerde12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.GREEN);
        private final Font fuenteAzul25 = new Font(Font.FontFamily.TIMES_ROMAN, 25, Font.BOLD, BaseColor.BLUE);
        private final Font fuenteRoja12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
        private final Font fuenteAzul16 = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, BaseColor.BLUE);
        private final Font fuenteAzul12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
        private final Font fuenteAzul10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.BLUE);
        private final Font small8 = new Font(Font.FontFamily.COURIER, 8, Font.NORMAL);
        private final Font small10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
        private final Font small12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
        private final Font smallBold12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);

        private FileOutputStream fost;
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        private PdfWriter writer;

        public Pdf(String fileName) {

            try {

                new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/pdfreceipts").mkdirs();

                fileName = RECEIPTFILE + fileName;

//                Document document = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);
//                Document document2 = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);
                Document document = new Document(PageSize.A4, 0, 0, 0, 0);
                Document document2 = new Document(PageSize.A4, 0, 0, 0, 0);
                fost = new FileOutputStream(fileName);

                document.setMargins(2, 2, 2, 2);
                document2.setMargins(2, 2, 2, 2);

                PdfWriter.getInstance(document, fost);
//                PdfWriter.getInstance(document2, os);

                writer = PdfWriter.getInstance(document2, os);
                HeaderFooterPageEvent event = new HeaderFooterPageEvent();
                event.setFirstLineReportTitle(" DISPONIBILIDAD DIARIA DE EMPLEADOS");
                event.setCompanyName(((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));
                event.setSecondLineReportTitle("DEL : " + fechaInicio + " AL : " + fechaFin);
                
                writer.setPageEvent(event);

                document.open();
                document2.open();

                addMetaData(document);
//                addTitlePage(document);
                addEmployeeTable(document);
                addFooterPage(document);
                document.close();

                addMetaData(document2);
//                addTitlePage(document2);
                addEmployeeTable(document2);
                addFooterPage(document2);
                document2.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public InputStream getStream() {
            // Here we return the pdf contents as a byte-array
            return new ByteArrayInputStream(os.toByteArray());
        }

        // iText allows to add metadata to the PDF which can be viewed in your Adobe
        // Reader
        // under File -> Properties
        private void addMetaData(Document document) {
            document.addTitle("DISPONIBILIDAD DIARIA DEL : " + fechaInicio + " AL " + fechaFin);
            document.addSubject("Using iText");
            document.addKeywords("Java, PDF, iText");
            document.addAuthor("www.sopdi.com");
            document.addCreator("www.sopdi.com");
        }

        private void addTitlePage(Document document) {

            try {
                PdfPTable tableEnzabezado = new PdfPTable(7);

                tableEnzabezado.setSplitRows(false);
                tableEnzabezado.setHeaderRows(1);
                tableEnzabezado.setWidthPercentage(70);

                float[] columnWidths = {1.0f, 2.0f, 2.0f, 2.5f, 2.5f, 2.0f, 3.0f};

                tableEnzabezado.setWidths(columnWidths);

                PdfPCell c1 = new PdfPCell(new Paragraph("", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("FECHA DEL : ", fuenteAzul10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(fechaInicio, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("FECHA AL :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(fechaFin, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("SUPERVISOR :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName(), small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                document.add(tableEnzabezado);
                
            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }

        private void addEmployeeTable(Document document) {
             
            try {
                PdfPTable table = new PdfPTable(7);

                table.setSplitRows(false);
                table.setHeaderRows(1);
                table.setWidthPercentage(90);
                
                float[] columnWidth = {1.5f, 2.0f, 2.0f, 2.5f, 2.5f, 2.0f, 2.0f};

                table.setWidths(columnWidth);

                PdfPCell
                c2 = new PdfPCell(new Paragraph("FECHA", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setVerticalAlignment(Element.ALIGN_CENTER);
                c2.setBorderWidth(2);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("RH1 PROJECT", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setVerticalAlignment(Element.ALIGN_CENTER);
                c2.setBorderWidth(2);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("RH2 PROJECT", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setVerticalAlignment(Element.ALIGN_CENTER);
                c2.setBorderWidth(2);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("RH1 CONTRATADO", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setVerticalAlignment(Element.ALIGN_CENTER);
                c2.setBorderWidth(2);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("RH2 CONTRATADO", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setVerticalAlignment(Element.ALIGN_CENTER);
                c2.setBorderWidth(2);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("RH1 DIFERENCIA", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setVerticalAlignment(Element.ALIGN_CENTER);
                c2.setBorderWidth(2);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("RH2 DIFERENCIA", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setVerticalAlignment(Element.ALIGN_CENTER);
                c2.setBorderWidth(2);
                table.addCell(c2);

                for(Object itemId : asistenciaContainer.getItemIds()) {
                    c2 = new PdfPCell(new Paragraph(String.valueOf(asistenciaContainer.getItem(itemId).getItemProperty(ConsultaReursoDiarioView.FECHA_PROPERTY).getValue()), small8));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    table.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(String.valueOf(asistenciaContainer.getItem(itemId).getItemProperty(ConsultaReursoDiarioView.RH1_PROPERTY).getValue()), small8));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    table.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(String.valueOf(asistenciaContainer.getItem(itemId).getItemProperty(ConsultaReursoDiarioView.RH2_PROPERTY).getValue()), small8));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    table.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(String.valueOf(asistenciaContainer.getItem(itemId).getItemProperty(ConsultaReursoDiarioView.RH1_C_PROPERTY).getValue()), small8));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    table.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(String.valueOf(asistenciaContainer.getItem(itemId).getItemProperty(ConsultaReursoDiarioView.RH2_C_PROPERTY).getValue()), small8));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    table.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(String.valueOf(asistenciaContainer.getItem(itemId).getItemProperty(ConsultaReursoDiarioView.RH1_D_PROPERTY).getValue()), small8));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    table.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(String.valueOf(asistenciaContainer.getItem(itemId).getItemProperty(ConsultaReursoDiarioView.RH2_D_PROPERTY).getValue()), small8));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    table.addCell(c2);
                }//ENDFOR

                c2 = new PdfPCell(new Paragraph("", small10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(0);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("", small10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(0);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("----------ULTIMA LINEA----------", small10));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setBorderWidth(0);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("", small10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(0);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("", small10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(0);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("", small10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(0);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("", small10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(0);
                table.addCell(c2);

                document.add(table);

            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

        private void addEmptyLine(Paragraph paragraph, int number) {
            for (int i = 0; i < number; i++) {
                paragraph.add(new Paragraph(" "));
            }
        }

        private void addFooterPage(Document document) {
            try {

                Paragraph prefaceLineas = new Paragraph();
                Paragraph prefaceFirmas = new Paragraph();

                addEmptyLine(prefaceLineas, 2);
                document.add(prefaceLineas);

                Paragraph firmas = new Paragraph("f ____________________                           "
                        + "                    f_____________________                           "
                        + "          f ____________________                           ", smallBold10);
                firmas.setAlignment(Element.ALIGN_LEFT);
                prefaceFirmas.add(firmas);

//                addEmptyLine(preface, 1);
//
//                Paragraph firmas2 = new Paragraph("f ____________________                           "
//                        + "                    f_____________________                           "
//                        + "          f ____________________                           ", smallBold10);
//                firmas.setAlignment(Element.ALIGN_LEFT);
//                preface.add(firmas2);
//
//                addEmptyLine(preface, 1);

                document.add(prefaceFirmas);

            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }

    }
    
}
