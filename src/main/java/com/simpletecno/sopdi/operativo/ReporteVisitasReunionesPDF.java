/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.simpletecno.sopdi.HeaderFooterPageEvent;
import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author JAguirre
 */
public class ReporteVisitasReunionesPDF extends Window {

    public static Statement stQuery;
    public static PreparedStatement stPreparedQuery;
    public static ResultSet rsRecords;

    static final DecimalFormat df1 = new DecimalFormat("###,##0.00");

    String fileName;
    String queryString;
    String idvisitaInspeccion;
    String codigoVisita;
    public IndexedContainer agendaContainer;
    IndexedContainer participantesContainer;
    String fechaInicio, fechaFin, motivo, medio, centroCosto, cliente, referencia, lugar;
    Button enviarReporteBtn;
    String rubro = "";
    int contador = 1;
    long linea = 0;

    public ReporteVisitasReunionesPDF(
            String idVisita, 
            String codigoVisitaInspeccion, 
            String fechaInicio, 
            String fechaFin, 
            String motivo,
            String medio,
            String cCOSTO, 
            String cliente, 
            String referencia, 
            String lugar, 
            IndexedContainer agendaContainer, 
            IndexedContainer participantesContainer
    ) {

        this.idvisitaInspeccion = idVisita;
        this.codigoVisita = codigoVisitaInspeccion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = motivo;
        this.medio = medio;
        this.centroCosto = cCOSTO;
        this.cliente = cliente;
        this.referencia = referencia;
        this.lugar = lugar;
        this.agendaContainer = agendaContainer;
        this.participantesContainer = participantesContainer;

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

            VerticalLayout buttonLayout = new VerticalLayout();
            buttonLayout.addStyleName("rcorners3");
            buttonLayout.setSizeUndefined();

            fileName = "ReporteVisitaReunion_" + String.valueOf(codigoVisitaInspeccion) + "_" + new Utileria().getFechaHoraSinFormato() + ".pdf";

            Pdf pdf = new Pdf(fileName);

            StreamResource pdfResource;
            pdfResource = new StreamResource(pdf, fileName);
            pdfResource.setMIMEType("application/pdf");

            fileName = VaadinService.getCurrent()
                    .getBaseDirectory().getAbsolutePath() + "/pdfreceipts/" + fileName;

            FileUtils.writeByteArrayToFile(new File(fileName), pdf.os.toByteArray());

            enviarReporteBtn = new Button("Enviar reporte");
            enviarReporteBtn.setIcon(FontAwesome.ENVELOPE_SQUARE);
            enviarReporteBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
            enviarReporteBtn.addClickListener((event) -> {

                try {

                    Thread.sleep(5000);
                    Thread.sleep(5000);
                    List<String> emailsTo = new ArrayList<>();
                    for(Object item : participantesContainer.getItemIds()){
                        if (!participantesContainer.getItem(item).getItemProperty("Email").getValue().equals("")) {
                            emailsTo.add(String.valueOf(participantesContainer.getItem(item).getItemProperty("Email").getValue()));
                        }
                    }
                    MyEmailMessanger eMail = new MyEmailMessanger();

                    eMail.postMail(emailsTo.toArray(new String[0]), "Sistema SOPDI-NISA", "\nEstimados, adjunto el reporte de visita/reunión.\n\nGenerado automáticamente por Sistema SOPDI-NISA\n", fileName);

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(ReporteVisitasReunionesPDF.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(ReporteVisitasReunionesPDF.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            //file.delete();

            browser.setSource(pdfResource);

            browserLayout.addComponent(browser);
            browserLayout.setComponentAlignment(browser, Alignment.MIDDLE_CENTER);

            buttonLayout.addComponent(enviarReporteBtn);
            buttonLayout.setComponentAlignment(enviarReporteBtn, Alignment.BOTTOM_CENTER);

            mainLayout.addComponent(browserLayout);
            mainLayout.setComponentAlignment(browserLayout, Alignment.TOP_CENTER);
            mainLayout.addComponent(buttonLayout);
            mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);

            mainLayout.setExpandRatio(browserLayout, 5);
            mainLayout.setExpandRatio(buttonLayout, 1);

            setContent(mainLayout);

        } catch (Exception allEx) {

            Notification.show("Error al generar el reporte de la visita reunion.", Notification.Type.ERROR_MESSAGE);
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
        private final Font small10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
        private final Font small12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
        private final Font smallBold12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);

        private FileOutputStream fost;
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        private PdfWriter writer;

        public Pdf(String fileName) {

            try {

                new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/pdfreceipts").mkdirs();

                fileName = "ReporteVisitaReunion_" + String.valueOf(codigoVisita) + "_" + new Utileria().getFechaHoraSinFormato() + ".pdf";
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
                event.setFirstLineReportTitle("REPORTE DE VISITA / REUNION #. " + String.valueOf(codigoVisita));
                event.setCompanyName(((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));
                event.setSecondLineReportTitle(referencia);
                
                writer.setPageEvent(event);

                document.open();
                document2.open();

                addMetaData(document);
                addTitlePage(document);
                addParticipantesTable(document);
                addAgendaTable(document);
                addTasksTable(document);
                addFooterPage(document);
                document.close();

                linea = 0;

                addMetaData(document2);
                addTitlePage(document2);
                addParticipantesTable(document2);
                addAgendaTable(document2);
                addTasksTable(document2);
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
            document.addTitle("REPORTE DE VISITA REUNION " + String.valueOf(codigoVisita) + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName() + " AL : " + Utileria.getFechaDDMMYYYY(new java.util.Date()));
            document.addSubject("Using iText");
            document.addKeywords("Java, PDF, iText");
            document.addAuthor("www.sopdi.com");
            document.addCreator("www.sopdi.com");
        }

        private void addTitlePage(Document document) {

            try {
                PdfPTable tableEnzabezado = new PdfPTable(4);

                tableEnzabezado.setSplitRows(false);
                tableEnzabezado.setHeaderRows(1);
                tableEnzabezado.setWidthPercentage(70);

                float[] columnWidths = {2.0f, 3.0f, 2.0f, 3.0f};

                tableEnzabezado.setWidths(columnWidths);

                PdfPCell c1 = new PdfPCell(new Paragraph("", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("", fuenteAzul10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("", fuenteAzul10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("MOTIVO :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(motivo, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("MEDIO :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(medio, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("FECHA INICIO :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(fechaInicio, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("FECHA FIN :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(fechaFin, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("CLIENTE :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                if (cliente.equals("<<ELIJA>>") || cliente.trim().equals("")) {
                    c1 = new PdfPCell(new Paragraph("", small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    tableEnzabezado.addCell(c1);
                } else {
//                    c1 = new PdfPCell(new Paragraph(String.valueOf(participantesContainer.getItem(1).getItemProperty("Nombre").getValue()), small10));
                    c1 = new PdfPCell(new Paragraph(cliente, small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    tableEnzabezado.addCell(c1);
                }

                c1 = new PdfPCell(new Paragraph("C. COSTO:", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                if (centroCosto.contains("ELIJA")) {

                    c1 = new PdfPCell(new Paragraph("", small10));
                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c1.setBorderWidth(0);
                    tableEnzabezado.addCell(c1);

                } else {

                    c1 = new PdfPCell(new Paragraph(centroCosto, small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    tableEnzabezado.addCell(c1);

                }

                c1 = new PdfPCell(new Paragraph("REFERENCIA :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(referencia, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph("LUGAR :", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);

                c1 = new PdfPCell(new Paragraph(lugar, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                tableEnzabezado.addCell(c1);
                
                document.add(tableEnzabezado);

                Paragraph preface2 = new Paragraph();
                addEmptyLine(preface2, 1);

                document.add(preface2);
                
            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }

        private void addParticipantesTable(Document document) {
             
            try {
                PdfPTable table = new PdfPTable(3);

                table.setSplitRows(false);
                table.setHeaderRows(1);
                table.setWidthPercentage(90);
                
                float[] columnWidth = {3.5f, 1.5f, 4.0f};

                table.setWidths(columnWidth);

                PdfPCell
                c2 = new PdfPCell(new Paragraph("PARTICIPANTE", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setVerticalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(2);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("DPI", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setVerticalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(2);
                table.addCell(c2);

                c2 = new PdfPCell(new Paragraph("EMAIL", smallBold10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setVerticalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(2);
                table.addCell(c2);

                for(Object item : participantesContainer.getItemIds()){
                    c2 = new PdfPCell(new Paragraph(String.valueOf(participantesContainer.getItem(item).getItemProperty("Nombre").getValue()), small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    table.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(String.valueOf(participantesContainer.getItem(item).getItemProperty("DPI").getValue()), small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    table.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(String.valueOf(participantesContainer.getItem(item).getItemProperty("Email").getValue()), small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    table.addCell(c2);
                }

                document.add(table);
                

            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
            
        private void addAgendaTable(Document document) {

            queryString = "SELECT *";
            queryString += " FROM visita_inspeccion_agenda";
            queryString += " WHERE IdVisitaInspeccion = " + idvisitaInspeccion;
            
            try {

                Paragraph preface1 = new Paragraph("");
                addEmptyLine(preface1, 1);
                document.add(preface1);

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {
                    
                    PdfPTable agendaTable = new PdfPTable(3);

                    agendaTable.setSplitRows(false);
                    agendaTable.setHeaderRows(0);
                    agendaTable.setWidthPercentage(85);

                    float[] columnWidths5 = {0.3f, 2.0f, 3.5f};

                    agendaTable.setWidths(columnWidths5);

                    PdfPCell cell = new PdfPCell(new Paragraph("#", smallBold10));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_CENTER);
                    cell.setBorderWidth(2);
                    agendaTable.addCell(cell);

                    cell = new PdfPCell(new Paragraph("ANGENDA/ASUNTO", smallBold10));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_LEFT);
                    cell.setBorderWidth(2);
                    agendaTable.addCell(cell);

                    cell = new PdfPCell(new Paragraph("RESOLUCION", smallBold10));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_LEFT);
                    cell.setBorderWidth(2);
                    agendaTable.addCell(cell);

                    int i = 1;
                    do{
                        cell = new PdfPCell(new Paragraph(i + "", small12));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setVerticalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(1);
                        agendaTable.addCell(cell);

                        cell = new PdfPCell(new Paragraph(rsRecords.getString("PuntoAgenda"), small12));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setVerticalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(1);
                        agendaTable.addCell(cell);

                        cell = new PdfPCell(new Paragraph(rsRecords.getString("Resolucion"), small12));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setVerticalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(1);
                        agendaTable.addCell(cell);
                        i++;
                        linea = linea + (rsRecords.getString("Resolucion").length() * 12);
                    }while (rsRecords.next());


                    cell = new PdfPCell(new Paragraph("***", small12));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_LEFT);
                    cell.setBorderWidth(1);
                    agendaTable.addCell(cell);

                    cell = new PdfPCell(new Paragraph("***ULTIMA LINEA***", small12));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_LEFT);
                    cell.setBorderWidth(1);
                    agendaTable.addCell(cell);

                    cell = new PdfPCell(new Paragraph("***ULTIMA LINEA***", small12));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_LEFT);
                    cell.setBorderWidth(1);
                    agendaTable.addCell(cell);

                    document.add(agendaTable);
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("error" + ex);
            }
        }

        private void addTasksTable(Document document) {

            try {

                PdfPTable titleTable = new PdfPTable(3);

                titleTable.setSplitRows(false);
                titleTable.setHeaderRows(0);
                titleTable.setWidthPercentage(90);

                float[] columnWidths3 = {1.5f, 4.0f, 1.5f};

                titleTable.setWidths(columnWidths3);

                PdfPCell c3 = new PdfPCell(new Paragraph("", smallBold10));
                c3.setHorizontalAlignment(Element.ALIGN_LEFT);
                c3.setVerticalAlignment(Element.ALIGN_LEFT);
                c3.setBorderWidth(0);
                titleTable.addCell(c3);

                c3 = new PdfPCell(new Paragraph("TAREAS", smallBold10));
                c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                c3.setVerticalAlignment(Element.ALIGN_CENTER);
                c3.setBorderWidth(3);
                titleTable.addCell(c3);

                c3 = new PdfPCell(new Paragraph("", smallBold10));
                c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                c3.setVerticalAlignment(Element.ALIGN_CENTER);
                c3.setBorderWidth(0);
                titleTable.addCell(c3);

                Paragraph preface1 = new Paragraph("");
                addEmptyLine(preface1, 1);
                document.add(preface1);

                document.add(titleTable);

                PdfPTable tareaTable = new PdfPTable(3);

                tareaTable.setSplitRows(false);
                tareaTable.setHeaderRows(0);
                tareaTable.setWidthPercentage(90);

                float[] columnWidths4 = {1.5f, 1.5f, 4.0f};

                tareaTable.setWidths(columnWidths4);

                PdfPCell cell = new PdfPCell(new Paragraph("RUBRO", smallBold10));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(2);
                tareaTable.addCell(cell);

                cell = new PdfPCell(new Paragraph("DESCRIPCION", smallBold10));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(2);
                tareaTable.addCell(cell);

                cell = new PdfPCell(new Paragraph("INSTRUCCION", smallBold10));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(2);
                tareaTable.addCell(cell);

                queryString = "SELECT *";
                queryString += " FROM visita_inspeccion_tarea";
                queryString += " WHERE IdVisitaInspeccion = " + idvisitaInspeccion;
                queryString += " ORDER BY Rubro";

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                while (rsRecords.next()) {

                    if (rubro.equals(rsRecords.getString("Rubro"))) {

                        cell = new PdfPCell(new Paragraph(rsRecords.getString("Rubro"), small10));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(0);
                        tareaTable.addCell(cell);

                        cell = new PdfPCell(new Paragraph(rsRecords.getString("Descripcion"), small10));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(0);
                        tareaTable.addCell(cell);

                        cell = new PdfPCell(new Paragraph(rsRecords.getString("Instruccion"), small10));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(0);
                        tareaTable.addCell(cell);
                        linea = linea + (rsRecords.getString("Instruccion").length()*10);

                    } else {

                        if (rsRecords.isFirst() == false) {
                            cell = new PdfPCell(new Paragraph("——————————", small10));
                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cell.setVerticalAlignment(Element.ALIGN_LEFT);
                            cell.setBorderWidth(0);
                            tareaTable.addCell(cell);

                            cell = new PdfPCell(new Paragraph("——————————", small10));
                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cell.setVerticalAlignment(Element.ALIGN_LEFT);
                            cell.setBorderWidth(0);
                            tareaTable.addCell(cell);

                            cell = new PdfPCell(new Paragraph("—————————————————————————————", small10));
                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cell.setVerticalAlignment(Element.ALIGN_LEFT);
                            cell.setBorderWidth(0);
                            tareaTable.addCell(cell);
                            linea = linea + 12;

                        }
                        
                        cell = new PdfPCell(new Paragraph(rsRecords.getString("Rubro"), smallBold12));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(0);
                        tareaTable.addCell(cell);

                        cell = new PdfPCell(new Paragraph(rsRecords.getString("Descripcion"), small10));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(0);
                        tareaTable.addCell(cell);

                        cell = new PdfPCell(new Paragraph(rsRecords.getString("Instruccion"), small10));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidth(0);
                        tareaTable.addCell(cell);

                    }

                    rubro = rsRecords.getString("Rubro");
                    contador = contador + 1;

                }
                document.add(tareaTable);

                Paragraph preface2 = new Paragraph();
                addEmptyLine(preface2, 1);

                document.add(preface2);
                
            } catch (Exception e) {

                e.printStackTrace();
                System.out.println("Error al intentar general tabla de tareas en el PDF" + e);
            }

        }
        
        private void addEmptyLine(Paragraph paragraph, int number) {
            for (int i = 0; i < number; i++) {
                paragraph.add(new Paragraph(" "));
            }
        }

        private void addFooterPage(Document document) {
            try {

                int blancas = 1;
                if((linea + 290)%842 > 800){
                    blancas += 2;
                }
                Paragraph prefaceLineas = new Paragraph();

                addEmptyLine(prefaceLineas, blancas);
                document.add(prefaceLineas);

                PdfPTable firmaTable = new PdfPTable(5);

                firmaTable.setSplitRows(false);
                firmaTable.setHeaderRows(0);
                firmaTable.setWidthPercentage(90);

                float[] columnWidths4 = {2.0f, 1.0f, 2.0f, 1.0f, 2.0f};

                firmaTable.setWidths(columnWidths4);

                PdfPCell cell = new PdfPCell(new Paragraph("f_________________________", small10));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0);
                firmaTable.addCell(cell);

                cell = new PdfPCell(new Paragraph(" ", small10));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0);
                firmaTable.addCell(cell);

                cell = new PdfPCell(new Paragraph("f_________________________", small10));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0);
                firmaTable.addCell(cell);

                cell = new PdfPCell(new Paragraph(" ", small10));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0);
                firmaTable.addCell(cell);

                cell = new PdfPCell(new Paragraph("f_________________________", small10));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0);
                firmaTable.addCell(cell);
                document.add(firmaTable);

            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }

    }
    
}
