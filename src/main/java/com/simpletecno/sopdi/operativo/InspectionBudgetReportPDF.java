/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.simpletecno.sopdi.HeaderFooterPageEvent;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
public class InspectionBudgetReportPDF extends Window {

    public static Statement stQuery;
    public static PreparedStatement stPreparedQuery;
    public static ResultSet rsRecords;    
    
    static final DecimalFormat df1= new DecimalFormat("###,##0.00");
    
    String fileName;
    String queryString;
           
    public String codigoVisita = "";
    public String fechaVisita = "";
    public String clienteNombre = "";
    public String clienteEmail = "";
    public String centroCosto = "";
    public String idVisitaInspeccion = "";
    public int diasHabiles = 0;
    public int totalDiasHabiles = 0;
    long linea = 0;
    public InspectionBudgetReportPDF(String idVisitaInspeccion) {
           
        this.idVisitaInspeccion = idVisitaInspeccion;
        
        try {
            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("1100");
            setHeight("700");
            center();

            setVisitaInformation(idVisitaInspeccion);
            
            clienteNombre = clienteNombre.replaceAll(" ","_");
            
            fileName = "PresupuestoTareasVisita_" + codigoVisita + "_" + clienteNombre  + "_" + new Utileria().getFechaHoraSinFormato() + ".pdf";
            
            StreamResource pdfResource;
            pdfResource = new StreamResource(new Pdf(fileName), fileName);
                pdfResource.setMIMEType("application/pdf");
                
            e.setSource(pdfResource); 
            setContent(e);
                // despues de llamar Pdf()  fileName y tiene la ruta completa del archivo
            File file = new File(fileName);
            file.delete();
                
        }
        catch(Exception allEx) {
            Notification.show("Error al generar el reporte de la venta.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }
 
    }
    
    private void setVisitaInformation(String idVisitaInspeccion) {
        queryString = "Select Vis.*, Cli.*  ";
        queryString += " From visita_inspeccion Vis";
        queryString += " Inner Join proveedor Cli On Cli.IdProveedor = Vis.IdCliente";
        queryString += " Where Vis.IdVisitaInspeccion = " + idVisitaInspeccion;

//System.out.println("QUERY TAREAS POR INSPECCION: " +  queryString);        

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado  
                codigoVisita = rsRecords.getString("CodigoVisita");
                fechaVisita = rsRecords.getString("FechaYHoraInicio");
                clienteNombre =  rsRecords.getString("Nombre");
                clienteEmail = rsRecords.getString("Email");
                centroCosto = rsRecords.getString("IdCentroCosto");
            }
        }
        catch(Exception ex1) {
            ex1.printStackTrace();
        }
        
    }
    
    /**
     * This class creates a PDF with the iText library. This class implements
     * the StreamSource interface which defines the getStream method.
     */
    public class Pdf implements StreamResource.StreamSource, Serializable {
        private final String RECEIPTFILE = 
                VaadinService.getCurrent()
                              .getBaseDirectory().getAbsolutePath() + "/pdfreceipts/";
        private String TDLOGO = VaadinService.getCurrent()
                              .getBaseDirectory().getAbsolutePath() + "/VAADIN/themes/tests-valo-flat/img/logo_simpletecno.png";

        private final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        private final Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
        private final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
        private final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD);   
        private final Font smallBold10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        private final Font smallBold20 = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
        private final Font fuenteMagenta36 = new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.BOLD, BaseColor.MAGENTA);
        private final Font fuenteNegra12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);
        private final Font fuenteVerde12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.GREEN);
        private final Font fuenteAzul25 = new Font(Font.FontFamily.TIMES_ROMAN, 25, Font.BOLD, BaseColor.BLUE);
        private final Font fuenteRoja12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
        private final Font fuenteAzul16 = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, BaseColor.BLUE);
        private final Font fuenteAzul12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
        private final Font fuenteAzul10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.BLUE);
        private final Font small10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
        private final Font small10Red = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL, BaseColor.RED);

        private FileOutputStream fost; 
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        private PdfWriter writer;

        public Pdf(String fileName) {
            
            try {           

                new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/pdfreceipts").mkdirs();
                
                fileName = RECEIPTFILE + fileName;

                Document document = new Document(PageSize.A4, 0, 0, 0, 0);
                Document document2 = new Document(PageSize.A4, 0, 0, 0, 0);
                fost = new FileOutputStream(fileName);

                document.setMargins(20,2,2,25);
                document2.setMargins(20,2,2,25);
                
                PdfWriter.getInstance(document, fost);
                
                writer = PdfWriter.getInstance(document2, os);
                HeaderFooterPageEvent event = new HeaderFooterPageEvent();
                event.setFirstLineReportTitle("PRESUPUESTO DE LA VISITA " + codigoVisita + " " + clienteNombre);
                event.setCompanyName(((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));

                writer.setPageEvent(event);
                writer.setFullCompression();
                writer.setLinearPageMode();
                writer.setCloseStream(true);
                
                document.open();
                document2.open();
                
                addMetaData(document);
                addTitlePage(document);
                addContent(document);
                addFooterPage(document);
                document.close();

                addMetaData(document2);
                addTitlePage(document2);
                addContent(document2);
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
        private  void addMetaData(Document document) {
                document.addTitle("PRESUPUESTO DE LA VISITA " + codigoVisita + "  " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
                document.addSubject("Using iText");
                document.addKeywords("Java, PDF, iText");
                document.addAuthor("www.sopdi.com");
                document.addCreator("www.sopdi.com");
        }

        private void addTitlePage(Document document) {
            try {
//PRIMERA TABLA
                PdfPTable firstTable = new PdfPTable(3);

                firstTable.setSplitRows(false);
                firstTable.setHeaderRows(0);
                firstTable.setWidthPercentage(95);

                float[] columnWidths = {3.0f, 1.5f, 1.5f};

                firstTable.setWidths(columnWidths);
                
//LINE 1
                PdfPCell c1 = new PdfPCell(new Paragraph("Castaños, S.A.", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                firstTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("Fecha Hora Generación :", small10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                firstTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()), small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(1);
                firstTable.addCell(c1);
//LINE 2

                c1 = new PdfPCell(new Paragraph("", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                firstTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("Generado por :", small10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                firstTable.addCell(c1);
                
                c1 = new PdfPCell(new Paragraph(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName(), small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(1);
                firstTable.addCell(c1);
                
                document.add(firstTable);
                
//SEGUNDA TABLA                
                PdfPTable secondTable = new PdfPTable(2);

                secondTable.setSplitRows(false);
                secondTable.setHeaderRows(0);
                secondTable.setWidthPercentage(95);

                float[] columnWidths2 = {3.0f, 1.5f};

                secondTable.setWidths(columnWidths2);                
//TERCERA TABLA        
                PdfPTable thirdTable = new PdfPTable(2);

                thirdTable.setSplitRows(false);
                thirdTable.setHeaderRows(0);
                thirdTable.setWidthPercentage(100);

                float[] columnWidths3 = {1.5f, 3.0f};

                thirdTable.setWidths(columnWidths3);                
                                
//LINE 1
                c1 = new PdfPCell(new Paragraph("No. Visita :", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                thirdTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(codigoVisita, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(1);
                thirdTable.addCell(c1);
                
//LINE 2
                c1 = new PdfPCell(new Paragraph("Fecha Visita :", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                thirdTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(fechaVisita, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(1);
                thirdTable.addCell(c1);

//LINE 3
                c1 = new PdfPCell(new Paragraph("Unidad CC :", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                thirdTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(centroCosto, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(1);
                thirdTable.addCell(c1);

//LINE 4
                c1 = new PdfPCell(new Paragraph("Nombre :", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                thirdTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(clienteNombre, small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(1);
                thirdTable.addCell(c1);

//LINE 5
                c1 = new PdfPCell(new Paragraph("Email :", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                thirdTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(clienteEmail, fuenteAzul10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(1);
                thirdTable.addCell(c1);

                c1 = new PdfPCell(thirdTable);
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                
                secondTable.addCell(c1);
                
                byte[] docBytes = new byte[((SopdiUI) UI.getCurrent()).sessionInformation.getProjectStreamResource().getStreamSource().getStream().available()];

                ((SopdiUI) UI.getCurrent()).sessionInformation.getProjectStreamResource().getStreamSource().getStream().read(docBytes, 0, docBytes.length);

                Image image1 = Image.getInstance(docBytes);

    //                image1.setAlignment(Image.ALIGN_RIGHT);
                image1.scaleAbsolute(100f, 100f);
                image1.setSpacingAfter(0f);
                image1.setAlignment(Image.ALIGN_RIGHT);
                
                c1 = new PdfPCell();
                c1.addElement(image1);
                c1.setBorder(0);
                c1.setFixedHeight(100);
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setUseAscender(true);

                secondTable.addCell(c1);
                                
                document.add(secondTable);
                
                Paragraph preface = new Paragraph();

                // We add one empty line
//                addEmptyLine(preface, 1);
//                document.add(preface);
                
                Paragraph line1 = new Paragraph("Instrucciones:", this.smallBold10);
                line1.setAlignment(Element.ALIGN_LEFT);
                preface.add(line1);
                Paragraph line2 = new Paragraph("1. Revisar Taras de Presupuesto.", small10);
                line2.setAlignment(Element.ALIGN_LEFT);
                preface.add(line2);
                Paragraph line3 = new Paragraph("2. Cada Tarea Autorizada proceder a poner inciales en Area \" Autorizado \".", small10);
                line3.setAlignment(Element.ALIGN_LEFT);
                preface.add(line3);
                Paragraph line4 = new Paragraph("3. Rellenar Pie de Formulario con Nombre y Firma.", small10);
                line4.setAlignment(Element.ALIGN_LEFT);
                preface.add(line4);
//                Paragraph line5 = new Paragraph("4. Scanear y enviar de regreso el formulario a : aprobacion@nisa.com.gt ", small10);
//                line5.setAlignment(Element.ALIGN_LEFT);
//                preface.add(line5);
                
                document.add(preface);

                preface = new Paragraph();
                addEmptyLine(preface, 1);
                document.add(preface);
                
                LineSeparator objectName = new LineSeparator();
                document.add(objectName);

                
            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }

        private void addContent(Document document) throws DocumentException {
            
            Paragraph preface = new Paragraph();

            addEmptyLine(preface, 1);
            document.add(preface);

            // Add a table
            createInspectionsTasksTable(document);
            
            preface = new Paragraph();
            addEmptyLine(preface, 1);
            document.add(preface);
            
            LineSeparator objectName = new LineSeparator();              
            document.add(objectName);
            
        }

        private void createInspectionsTasksTable(Document document)
                         throws DocumentException {

            PdfPTable firstTable = null;
            PdfPTable secondTable = null;
            PdfPCell c1 = null;
                    
            queryString = "Select * ";
            queryString += " From visita_inspeccion_tarea VisT";
            queryString += " Inner Join visita_inspeccion_tarea_presupuesto VisP On VisP.IdVisitaInspeccionTarea = VisT.IdVisitaInspeccionTarea";
            queryString += " Inner Join centro_costo_cuenta CC On CC.IdCuentaCentroCosto = VisP.IdCuentaCentroCosto";
            queryString += " Where VisT.IdVisitaInspeccion = " + idVisitaInspeccion;
            queryString += " Order By VisT.CodigoTarea";

//System.out.println("query presupuesto tareas : " +  queryString);

            double granTotal = 0.00;

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();            
                rsRecords = stQuery.executeQuery (queryString);

                if(rsRecords.next()) { //  encontrado  

                    double taskTotal = 0.00;
                    
                    String previusTask = "";
                    
                    do {
                        if(!previusTask.equals(rsRecords.getString("CodigoTarea"))) {
//System.out.println("\npreviusTask="+previusTask);
                            if(!previusTask.isEmpty()) {
//System.out.println("entro a print total...");
                                c1 = new PdfPCell(new Paragraph("", smallBold10));
                                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                                c1.setBorderWidth(0);
                                secondTable.addCell(c1);

                                c1 = new PdfPCell(new Paragraph("Dias Habiles de Trabajo:", smallBold10));
                                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                                c1.setBorderWidth(0);
                                secondTable.addCell(c1);

                                c1 = new PdfPCell(new Paragraph("" + diasHabiles, smallBold10));
                                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                                c1.setBorderWidth(0);
                                secondTable.addCell(c1);

                                c1 = new PdfPCell(new Paragraph("", smallBold10));
                                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                                c1.setBorderWidth(0);
                                secondTable.addCell(c1);

                                c1 = new PdfPCell(new Paragraph("$ " + df1.format(taskTotal), smallBold10));
                                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                                c1.setBorderWidth(1);
                                secondTable.addCell(c1);

                                totalDiasHabiles += diasHabiles;

                                document.add(secondTable);

                            //// ------------ Chequeo de Autorizacion

                                document.add(addWhiteSpace());

                                document.add(addAthorizationCheck());

                                document.add(addWhiteSpace());

                                taskTotal = 0.00;
                                
                            }
                            
                            firstTable = new PdfPTable(4);

                            firstTable.setSplitRows(false);
                            firstTable.setHeaderRows(0);
                            firstTable.setWidthPercentage(95);

                            float[] columnWidths = {1.0f, 1.0f, 1.0f, 5.0f};
                            firstTable.setWidths(columnWidths);

                            c1 = new PdfPCell(new Paragraph("IdTarea :", smallBold10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            firstTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(rsRecords.getString("CodigoTarea"), small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(1);
                            firstTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("Descripción :", smallBold10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            firstTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(rsRecords.getString("Instruccion"), small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(1);
                            firstTable.addCell(c1);

                            document.add(firstTable);

                            Paragraph preface = new Paragraph();

                            addEmptyLine(preface, 1);
                            document.add(preface);
                            
                            secondTable = new PdfPTable(5);

                            secondTable.setSplitRows(false);
                            secondTable.setHeaderRows(0);
                            secondTable.setWidthPercentage(95);

                            float[] columnWidths2 = {.5f, 4.0f, 1.5f, 1.5f, 1.5f}; // .5f, 4.0f, 1.0f, 1.0f, 1.0f, 1.5f
                            secondTable.setWidths(columnWidths2);

                            c1 = new PdfPCell(new Paragraph("Id", smallBold10));
                            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                            c1.setVerticalAlignment(Element.ALIGN_CENTER);
                            c1.setBorderWidth(1);
                            secondTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("Descripción", smallBold10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(1);
                            secondTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("Cantidad", smallBold10));
                            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                            c1.setVerticalAlignment(Element.ALIGN_CENTER);
                            c1.setBorderWidth(1);
                            secondTable.addCell(c1);
                            
                            c1 = new PdfPCell(new Paragraph("Sub Total", smallBold10));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(1);
                            secondTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("Total", smallBold10));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(1);
                            secondTable.addCell(c1);


                            previusTask = rsRecords.getString("CodigoTarea");
//System.out.println("\n...previusTask="+previusTask);

                            linea += 24;
                            
                        }
                        
                        c1 = new PdfPCell(new Paragraph(rsRecords.getString("CodigoPresupuesto").substring(10,12), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c1.setVerticalAlignment(Element.ALIGN_CENTER);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(1);
                        secondTable.addCell(c1);
                        
                        c1 = new PdfPCell(new Paragraph(rsRecords.getString("VisP.Descripcion"), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(1);
                        secondTable.addCell(c1);

                        if(rsRecords.getString("VisibleParaCliente").equals("SI")) {

                            c1 = new PdfPCell(new Paragraph(rsRecords.getString("Cantidad"), small10));
                            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                            c1.setVerticalAlignment(Element.ALIGN_CENTER);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(1);
                            secondTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("$ " + df1.format(rsRecords.getDouble("Precio")), small10));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(1);
                            secondTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("$ " + df1.format(rsRecords.getDouble("Total")), small10));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(1);
                            secondTable.addCell(c1);


                            taskTotal += rsRecords.getDouble("Total");
                            granTotal += rsRecords.getDouble("Total");
                        }
                        else {
                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                            c1.setVerticalAlignment(Element.ALIGN_CENTER);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(1);
                            secondTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("" , small10));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(1);
                            secondTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("" , small10));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(1);
                            secondTable.addCell(c1);


                        }

//System.out.println("taskTotal=" + taskTotal);
                        linea += 10;
                        diasHabiles = rsRecords.getInt("DiasHabiles");
                    } while(rsRecords.next());

                    c1 = new PdfPCell(new Paragraph("", smallBold10));
                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c1.setVerticalAlignment(Element.ALIGN_CENTER);
                    c1.setBorderWidth(0);
                    secondTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("Dias Habiles de Trabajo:", smallBold10));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    secondTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("" + diasHabiles, smallBold10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    secondTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("", smallBold10));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    secondTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("$ " + df1.format(taskTotal), smallBold10));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(1);
                    secondTable.addCell(c1);

                    totalDiasHabiles += diasHabiles;

                    document.add(secondTable);

                /// --- Autorizacion
                    document.add(addWhiteSpace());

                    document.add(addAthorizationCheck());

                    document.add(addWhiteSpace());

                    taskTotal = 0.00;

                }

                document.add(addWhiteSpace());

                PdfPTable totalTable = new PdfPTable(5);

                totalTable.setSplitRows(false);
                totalTable.setHeaderRows(0);
                totalTable.setWidthPercentage(95);

                float[] columnWidths2 = {.5f, 4.0f, 1.5f, 1.5f, 1.5f}; // .5f, 4.0f, 1.0f, 1.0f, 1.0f, 1.5f
                totalTable.setWidths(columnWidths2);

                c1 = new PdfPCell(new Paragraph("", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                totalTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("Total Dias Habiles de Trabajo:", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                totalTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("" + totalDiasHabiles/2, smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                totalTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("**TOTAL**", smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                totalTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("$ " + df1.format(granTotal), smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(1);
                totalTable.addCell(c1);


                document.add(totalTable);

            //// ------------ Chequeo de Autorizacion
                document.add(addWhiteSpace());

                document.add(addAthorizationCheck());

                document.add(addWhiteSpace());

            }
            catch(Exception ex1) {
                ex1.printStackTrace();
            }            
        }

        private void addFooterPage(Document document) {
            try {

                Paragraph preface = new Paragraph();

                // We add one empty line
                addEmptyLine(preface, 1);
                document.add(preface);
                
                Paragraph line1 = new Paragraph("Condiciones:", this.smallBold10);
                line1.setAlignment(Element.ALIGN_LEFT);
                preface.add(line1);
                Paragraph line2 = new Paragraph("*Presupuesto debe ser aprobado antes de la ejecución de los trabajos.", small10);
                line2.setAlignment(Element.ALIGN_LEFT);
                preface.add(line2);
                Paragraph line3 = new Paragraph("*Presupuesto tiene validéz de 7 días y podrá variar luego de este período.", small10);
                line3.setAlignment(Element.ALIGN_LEFT);
                preface.add(line3);
                Paragraph line4 = new Paragraph("*Al ser aprobado el presupuesto, el cliente debe proceder al pago de los trabajos en las oficinas de Castaños, S.A.", small10);
                line4.setAlignment(Element.ALIGN_LEFT);
                preface.add(line4);
                Paragraph line5 = new Paragraph(" en los siguientes 15 días calendario luego de ser aprogado el presupuesto.", small10);
                line5.setAlignment(Element.ALIGN_LEFT);
                preface.add(line5);
                Paragraph line6 = new Paragraph("*Todos los montos NO incluyen el IVA.", small10);
                line6.setAlignment(Element.ALIGN_LEFT);
                preface.add(line6);

                 addEmptyLine(preface, 1);

                Paragraph line7 = new Paragraph("Nombre Autoriza    :  ________________________________________________________", smallBold10);
                line7.setAlignment(Element.ALIGN_LEFT);
                preface.add(line7);

                addEmptyLine(preface, 1);

                Paragraph line8 = new Paragraph("Firma Autorización :  ________________________________________________________", smallBold10);
                line8.setAlignment(Element.ALIGN_LEFT);
                preface.add(line8);

//                addEmptyLine(preface, 1);

                document.add(preface);
                
                

            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }

        private void addEmptyLine(Paragraph paragraph, int number) {
            for (int i = 0; i < number; i++) {
                linea += 12;
                paragraph.add(new Paragraph(" "));
            }
        }

        private Paragraph addWhiteSpace(){
            Paragraph whitespace = new Paragraph();
            int blancas = 1;
            if((linea + 250)%842 > 800){
                linea += 90;
                blancas += 2;
            }
            addEmptyLine(whitespace, blancas);
            return whitespace;
        }

        private PdfPTable addAthorizationCheck() throws DocumentException, SQLException {
            PdfPTable chequeo = new PdfPTable(5);
            PdfPCell c1 = null;

            chequeo.setSplitRows(false);
            chequeo.setHeaderRows(0);
            chequeo.setWidthPercentage(95);
            chequeo.setKeepTogether(true);

            float[] columnWidths = {0.5f, 4.0f, 1.5f, 1.5f, 1.5f};
            chequeo.setWidths(columnWidths);

            c1 = new PdfPCell(new Paragraph("\n"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            chequeo.addCell(c1);

            c1 = new PdfPCell(new Paragraph("\n"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            chequeo.addCell(c1);

            c1 = new PdfPCell(new Paragraph("\n"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            chequeo.addCell(c1);

            c1 = new PdfPCell(new Paragraph("\n Autorizado: ", fuenteNegra12));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            chequeo.addCell(c1);

            c1 = new PdfPCell(new Paragraph("\n"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(2);

            chequeo.addCell(c1);


            linea += 24;

            return chequeo;
        }
        
    }
    
}