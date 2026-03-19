/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.compras;

import com.itextpdf.text.BaseColor;
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
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
public class ReporteLiquidacionPDF extends Window {

    public static Statement stQuery, stQuery2;
    public static PreparedStatement stPreparedQuery;
    public static ResultSet rsRecords, rsRecords2;

    static final DecimalFormat df1 = new DecimalFormat("###,##0.00");

    String fileName;
    String queryString;

    UI mainUI;
    String idEmpresa = "";
    String empresaNombre;
    String empresaNit;
    String idLiquidacion;
    String liquidador;

    public ReporteLiquidacionPDF(
            String idEmpresa, 
            String empresaNombre, 
            String empresaNit,
            String idLiquidacion, 
            String liquidador) {

        this.idEmpresa = idEmpresa;
        this.empresaNombre = empresaNombre;
        this.empresaNombre = empresaNit;
        this.idLiquidacion = idLiquidacion;
        this.liquidador = liquidador;

        try {
            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("1100");
            setHeight("700");
            center();

            fileName = "ReporteLiquidacion_" + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "") + "_" + liquidador + "_" + idLiquidacion + "_" + new Utileria().getFechaHoraSinFormato() + ".pdf";

            StreamResource pdfResource;
            pdfResource = new StreamResource(new Pdf(fileName), fileName);
            pdfResource.setMIMEType("application/pdf");

            e.setSource(pdfResource);
            setContent(e);
            // despues de llamar Pdf()  fileName y tiene la ruta completa del archivo
            File file = new File(fileName);
            file.delete();

        } catch (Exception allEx) {
            Notification.show("Error al generar el reporte de liquidaciones.", Notification.Type.ERROR_MESSAGE);
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

        private final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        private final Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
        private final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
        private final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD);
        private final Font smallBold10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        private final Font smallBold14 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
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

                document.setMargins(15, 15, 15, 15);
                document2.setMargins(15, 15, 15, 15);

                PdfWriter.getInstance(document, fost);

                String tipo = "REPORTE DE LIQUIDACION [" + idLiquidacion + "]";               
                String generado = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName();

                writer = PdfWriter.getInstance(document, os);
                HeaderFooterPageEvent event = new HeaderFooterPageEvent();

                event.setFirstLineReportTitle(tipo);
                event.setCompanyName(empresaNombre);
                event.setCompanyTaxid(empresaNit);
                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));
                event.setSecondLineReportTitle(liquidador + " " + Utileria.getFechaDDMMYYYY(new java.util.Date()));

                writer.setPageEvent(event);

                writer = PdfWriter.getInstance(document2, os);
                writer.setPageEvent(event);

                document.open();
                document2.open();

                addMetaData(document);
                //   addTitlePage(document);
                addContent(document);
                addFooterPage(document);
                document.close();

                addMetaData(document2);
                //  addTitlePage(document2);
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
            document.addTitle("Reporte de liquidación " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
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

            PdfPTable reportTable = new PdfPTable(5);
            PdfPCell c1 = null;

            float[] columnWidths = {1.5f, 1.5f, 2.5f, 1.5f, 2.0f};

            reportTable.setWidths(columnWidths);
            reportTable.setSplitRows(false);
            reportTable.setHeaderRows(1);
            reportTable.setWidthPercentage(95);

            c1 = new PdfPCell(new Paragraph("FACTURA", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("FECHA", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("PROVEEDOR", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("NIT", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("MONTO", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            String queryString = "";
            queryString += " Select contabilidad_partida.Fecha, contabilidad_partida.IdLiquidacion, contabilidad_partida.IdLiquidador,";
            queryString += " contabilidad_partida.Haber, contabilidad_partida.HaberQuetzales, contabilidad_partida.MonedaDocumento,";
            queryString += " proveedor.Nombre as NombreLiquidador, contabilidad_partida.Archivo, ";
            queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
            queryString += " contabilidad_partida.NitProveedor, contabilidad_partida.NombreProveedor";
            queryString += " From contabilidad_partida, proveedor ";
            queryString += " Where contabilidad_partida.IdEmpresa = " + idEmpresa;
            queryString += " And UPPER(contabilidad_partida.TipoDocumento) IN ('FACTURA', 'RECIBO', 'RECIBO CONTABLE', 'FORMULARIO','NOTA DE CREDITO')";
            queryString += " And contabilidad_partida.IdLiquidacion = " + idLiquidacion;
            queryString += " And proveedor.IdProveedor = contabilidad_partida.IdLiquidador";
            queryString += " And contabilidad_partida.Haber > 0.00";
//            queryString += " Group by contabilidad_partida.IdLiquidacion, contabilidad_partida.IdLiquidador";

            System.out.println("query liquidaciones" + queryString);

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                double total = 0.00;
                String monedaSimbolo = "";

                if (rsRecords.next()) { //  encontrado  

                    if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                        monedaSimbolo = "Q.";
                    }
                    else {
                        monedaSimbolo = "$.";                        
                    }
                    
                    do {
                        c1 = new PdfPCell(new Paragraph(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(rsRecords.getString("NombreProveedor"), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(rsRecords.getString("NitProveedor"), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(monedaSimbolo + df1.format(rsRecords.getDouble("Haber")), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        total += rsRecords.getDouble("Haber");

                    } while (rsRecords.next());

                }

                c1 = new PdfPCell(new Paragraph(""));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                c1.setBorderWidthTop(1);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(""));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthTop(1);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(""));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                c1.setBorderWidthTop(1);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("TOTAL"));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                c1.setBorderWidthTop(1);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(monedaSimbolo + df1.format(total), smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(1);
                reportTable.addCell(c1);

                document.add(reportTable);
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

        private void addFooterPage(Document document) {
            try {

                Paragraph preface = new Paragraph();

                // We add one empty line
                addEmptyLine(preface, 1);
                document.add(preface);

                Paragraph firmas = new Paragraph("f ____________________                           "
                        + "                      f_____________________                           "
                        + "           f ____________________                     ", smallBold10);
                firmas.setAlignment(Element.ALIGN_LEFT);
                preface.add(firmas);

                Paragraph puestos = new Paragraph("      Digitador                              "
                        + "                                                 Contador                               "
                        + "                                           Liquidador                                ", smallBold10);
                puestos.setAlignment(Element.ALIGN_LEFT);
                preface.add(puestos);

                addEmptyLine(preface, 1);

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
