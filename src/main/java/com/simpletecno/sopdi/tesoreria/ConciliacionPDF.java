/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.tesoreria;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
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
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
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
public class ConciliacionPDF extends Window {

    String fileName;
    String queryString;

    Statement stQuery, stQuery2, stQuery3;
    ResultSet rsRecords;
    ResultSet rsRecords2;
    ResultSet rsRecords3;

    UI mainUI;
    String empresaNit;
    String idConciliacion;
    String fecha;
    String bancoNombre;

    Utileria utilidadesFecha = new Utileria();

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");

    public ConciliacionPDF(String idConciliacion, String empresaNit, String fecha, String bancoNombre) {

        this.idConciliacion = idConciliacion;
        this.empresaNit = empresaNit;
        this.fecha = fecha;
        this.bancoNombre = bancoNombre;

        try {

            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("95%");
            setHeight("90%");
            center();

            fileName = "CONCILIACION_CORRESPONDIETE_AL_" + fecha.replaceAll("/", "_") + "_DE_" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "") + idConciliacion + ".pdf";

            StreamResource pdfResourceConcinciliacion;
            pdfResourceConcinciliacion = new StreamResource(new Pdf(fileName), fileName);
            pdfResourceConcinciliacion.setMIMEType("application/pdf");

            e.setSource(pdfResourceConcinciliacion);
            setContent(e);
            // despues de llamar Pdf()  fileName y tiene la ruta completa del archivo
            File file = new File(fileName);
            file.delete();

        } catch (Exception allEx) {
            Notification.show("Error al generar el balance de saldos.", Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }

    public class Pdf implements StreamResource.StreamSource, Serializable {

        private final String RECEIPTFILE
                = VaadinService.getCurrent()
                        .getBaseDirectory().getAbsolutePath() + "/pdfreceipts/";

        private final Font small8 = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
        private final Font smallBold10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        private final Font smallBold12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        private final Font small10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
        private final Font small12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

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

                document.setMargins(20, 20, 1, 20);
                document2.setMargins(20, 20, 1, 20);

                PdfWriter.getInstance(document, fost);

                writer = PdfWriter.getInstance(document2, os);

                HeaderFooterPageEvent event = new HeaderFooterPageEvent();
                event.setFirstLineReportTitle("CONCILIACION BANCARIA");
                event.setCompanyName(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName());
                event.setCompanyTaxid(empresaNit);
                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));
                event.setSecondLineReportTitle("CORRESPONDIENTE A : " + fecha);
                event.setThirdLineReportTitle(bancoNombre.toUpperCase());
                event.setFolioInicial(0);

                writer.setPageEvent(event);

                document.open();
                document2.open();

                addMetaData(document);
                addContent(document);
                addFooterPage(document);
                document.close();

                addMetaData(document2);
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
        private void addMetaData(Document document) {
            document.addTitle("Conciliacion bancaria " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
            document.addSubject("Using iText");
            document.addKeywords("Java, PDF, iText");
            document.addAuthor("www.sopdi.com");
            document.addCreator("www.sopdi.com");
        }

        private void addContent(Document document) throws DocumentException {

            Paragraph preface = new Paragraph();

            addEmptyLine(preface, 1);
            document.add(preface);

            // Add a table
            leerBaseDatosYEscribirLineas(document);

            preface = new Paragraph();
            addEmptyLine(preface, 1);
            document.add(preface);

            LineSeparator objectName = new LineSeparator();
            document.add(objectName);

        }

        private void leerBaseDatosYEscribirLineas(Document document)
                throws DocumentException {

             queryString = " SELECT * FROM contabilidad_conciliacion_bancaria ";
            queryString += " WHERE IdConciliacionBancaria =  " + idConciliacion;

            System.out.println("query busqueda conciliacion pdf" + queryString);

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery3 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                //  encontrado
                if (rsRecords.next()) {

                    float[] columnWidhts1 = {2.0f, 0.5f, 1.5f, 1.5f, 1.5f};

                    PdfPTable firstTable1 = new PdfPTable(5);
                    firstTable1.setSplitRows(false);
                    firstTable1.setHeaderRows(0);
                    firstTable1.setWidthPercentage(90);
                    firstTable1.setWidths(columnWidhts1);
                    firstTable1.setSplitRows(false);
                    firstTable1.setHeaderRows(1);
                    firstTable1.setWidthPercentage(95);

                    PdfPCell c1;

                    c1 = new PdfPCell(new Paragraph("Saldo inicial contable ", smallBold12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("", small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("", small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("", small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    c1.setPaddingTop(5);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(numberFormat.format(rsRecords.getDouble("SaldoInicialContable")), small12));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("(+)Total ingresos", smallBold12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("", small8));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setPaddingTop(5);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(numberFormat.format(rsRecords.getDouble("TotalIngresos")), small12));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("(-)Total egresos", smallBold12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(numberFormat.format(rsRecords.getDouble("TotalEgresos")), small12));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("Saldo final contable", smallBold12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);
                   

                    c1 = new PdfPCell(new Paragraph(numberFormat.format(rsRecords.getDouble("SaldoFinalContable")), small12));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("Saldo final bancario", smallBold12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(numberFormat.format(rsRecords.getDouble("SaldoFinalBanco")), small12));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("(+)Ingresos en tránsito", smallBold12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(" ", small12));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);
///// creo tabla numero 2 
                    float[] columnWidhts2 = {2.5f, 1.5f, 2.5f, 1.0f, 1.5f};

                    PdfPTable firstTable2 = new PdfPTable(5);
                    firstTable2.setSplitRows(false);
                    firstTable2.setHeaderRows(0);
                    firstTable2.setWidthPercentage(90);
                    firstTable2.setWidths(columnWidhts2);
                    firstTable2.setSplitRows(false);
                    firstTable2.setHeaderRows(1);
                    firstTable2.setWidthPercentage(95);

                    PdfPCell c2;

                    c2 = new PdfPCell(new Paragraph("FECHA", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("DOCUMENTO", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("VALOR", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    //// esto repetir mediante un cilo flor ya que aqi van los detalles de los no conciliados
                    queryString = "  SELECT * FROM conciliacion_bancaria_noconciliados";
                    queryString += " WHERE IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND Fecha >= '2017-01-01'";
                    queryString += " AND AnioMes <= " + rsRecords.getString("AnioMes");
                    queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                    queryString += " and IdConciliacion = "+ idConciliacion;
                    queryString += " and Tipo = 'INGRESOS' ";
                    
                    System.out.println("query para los ingresos en circulacion " + queryString);

                    rsRecords2 = stQuery2.executeQuery(queryString);

                    double totalDebe = 0.00;

                    if (rsRecords2.next()) {
                        do {
                            totalDebe += rsRecords2.getDouble("Monto");

                            c2 = new PdfPCell(new Paragraph(Utileria.getFechaDDMMYYYY(rsRecords2.getDate("Fecha")), small12));
                            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c2.setVerticalAlignment(Element.ALIGN_LEFT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                            c2 = new PdfPCell(new Paragraph(rsRecords2.getString("NumeroDocumento"), small12));
                            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c2.setVerticalAlignment(Element.ALIGN_LEFT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                            c2 = new PdfPCell(new Paragraph(numberFormat.format(rsRecords2.getDouble("Monto")), small12));
                            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                            c2 = new PdfPCell(new Paragraph(" ", small12));
                            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c2.setVerticalAlignment(Element.ALIGN_LEFT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                            c2 = new PdfPCell(new Paragraph(" ", small12));
                            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c2.setVerticalAlignment(Element.ALIGN_LEFT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                        } while (rsRecords2.next());
                    }

                    //// terminar ciclo de lo que no esta conciliado en el debe 
                    c2 = new PdfPCell(new Paragraph("Total Ingresos en Circulación", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(numberFormat.format(totalDebe), small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("(-)Egresos en circulación", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("FECHA", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("DOCUMENTO", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("NOMBRE", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("VALOR", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    /// EMPEZAR CILCO DE HABER PARA EL DETALLE DE NO CONCILIADOS
                    queryString = "  SELECT * FROM conciliacion_bancaria_noconciliados ";
                    queryString += " WHERE IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND Fecha >= '2020-01-01'";
                    queryString += " AND AnioMes <= " + rsRecords.getString("AnioMes");
                    queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                    queryString += " and IdConciliacion = "+ idConciliacion;
                    queryString += " and Tipo = 'EGRESOS' ";

                    System.out.println("query de egresos" + queryString);

                    rsRecords3 = stQuery3.executeQuery(queryString);

                    double totalHaber = 0.00;

                    if (rsRecords3.next()) {

                        do {
                            totalHaber += rsRecords3.getDouble("Monto");

                            c2 = new PdfPCell(new Paragraph(Utileria.getFechaDDMMYYYY(rsRecords3.getDate("Fecha")), small12));
                            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c2.setVerticalAlignment(Element.ALIGN_LEFT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                            c2 = new PdfPCell(new Paragraph(rsRecords3.getString("NumeroDocumento"), small12));
                            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c2.setVerticalAlignment(Element.ALIGN_LEFT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                            c2 = new PdfPCell(new Paragraph(rsRecords3.getString("Descripcion"), small12));
                            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c2.setVerticalAlignment(Element.ALIGN_LEFT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                            c2 = new PdfPCell(new Paragraph(numberFormat.format(rsRecords3.getDouble("Monto")), small12));
                            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                            c2 = new PdfPCell(new Paragraph(" ", small12));
                            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c2.setVerticalAlignment(Element.ALIGN_LEFT);
                            c2.setBorderWidth(0);
                            firstTable2.addCell(c2);

                        } while (rsRecords3.next());

                    }

                    c2 = new PdfPCell(new Paragraph("Total Egresos en circulación", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(numberFormat.format(totalHaber), small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);                    

                    c2 = new PdfPCell(new Paragraph("Saldo final bancario conciliado", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    double preOperacionParaSaldoFinal = (rsRecords.getDouble("SaldoFinalBanco") + totalDebe) - totalHaber;
                    
                    double saldoFinalBancarioConciliado = rsRecords.getDouble("SaldoFinalContable") - preOperacionParaSaldoFinal;
                    
                    c2 = new PdfPCell(new Paragraph(numberFormat.format(totalHaber - saldoFinalBancarioConciliado), small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);                                                            

                    c2 = new PdfPCell(new Paragraph("Diferencia a conciliar", smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);
                    
                    c2 = new PdfPCell(new Paragraph(numberFormat.format(saldoFinalBancarioConciliado), small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    firstTable2.addCell(c2);                                                           

                    Paragraph preface1 = new Paragraph("");
                    addEmptyLine(preface1, 1);

                    document.add(firstTable1);
                    document.add(firstTable2);

                    Paragraph preface2 = new Paragraph("");
                    addEmptyLine(preface2, 2);
                }

            } catch (SQLException ex) {
                Logger.getLogger(ConciliacionPDF.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private void addFooterPage(Document document) {
            try {

                Paragraph preface = new Paragraph();

                // We add one empty line
                addEmptyLine(preface, 2);
                document.add(preface);

                Paragraph firmas = new Paragraph("f ____________________                           "
                        + "                                                                     "
                        + "                                     f ____________________              ", smallBold10);
                firmas.setAlignment(Element.ALIGN_LEFT);
                preface.add(firmas);

                Paragraph puestos = new Paragraph("          HECHO POR:                                     "
                        + "                                                                               "
                        + "                                 REVISADO POR:                                ", small10);
                puestos.setAlignment(Element.ALIGN_LEFT);
                preface.add(puestos);

                addEmptyLine(preface, 2);

                Paragraph firmas2 = new Paragraph("f ____________________                           "
                        + "                   f____________________                                       "
                        + "       f ____________________              ", smallBold10);
                firmas2.setAlignment(Element.ALIGN_LEFT);
                preface.add(firmas2);

                Paragraph puestos2 = new Paragraph("          FECHA:                                     "
                        + "                       NOMBRE DE QUIEN RECIBIO                                    "
                        + "                      FIRMA                            ", small10);
                puestos2.setAlignment(Element.ALIGN_LEFT);
                preface.add(puestos2);

                document.add(preface);

            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }

        private void addEmptyLine(Paragraph paragraph, int number) {
            for (int i = 0; i < number; i++) {
                paragraph.add(new Paragraph(" "));
            }
        }

    }

}
