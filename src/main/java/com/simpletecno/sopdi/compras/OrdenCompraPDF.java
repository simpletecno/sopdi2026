/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.compras;

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

import com.simpletecno.sopdi.HeaderFooterPageEventOrdenCompra;
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
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
public class OrdenCompraPDF extends Window {

    String idOrdenCompra = "";
    String tipoOrdenCompra;
    String nombreTipoOrdenCompra;
    String proveedorNombre;
    String fileName;

    String queryString;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords;

    String moneda = "Q.";
    double total = 0.00;
    double anticipo = 0.00;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    HeaderFooterPageEventOrdenCompra event;

    public OrdenCompraPDF(
            String idOrdenCompra,
            String tipoOrdenCompra,
            String nombreTipoOrdenCompra,
            String proveedorNombre,
            double anticipo
    ) {
        this.idOrdenCompra = idOrdenCompra;
        this.tipoOrdenCompra = tipoOrdenCompra;
        this.nombreTipoOrdenCompra = nombreTipoOrdenCompra;
        this.proveedorNombre = proveedorNombre;
        this.anticipo = anticipo;

        mainUI = UI.getCurrent();

        try {
            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("95%");
            setHeight("90%");
            center();

            fileName = "OrdenCompra" + idOrdenCompra + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "") + "_" + new Utileria().getHora() + ".pdf";

            StreamResource pdfResource;
            pdfResource = new StreamResource(new Pdf(fileName), fileName);
            pdfResource.setMIMEType("application/pdf");

            e.setSource(pdfResource);
            setContent(e);
            // despues de llamar Pdf()  fileName y tiene la ruta completa del archivo
            File file = new File(fileName);
            file.delete();

        } catch (Exception allEx) {
            Notification.show("Error al generar PDF orden compra.", Type.ERROR_MESSAGE);
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

        private final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD);
        private final Font smallBold10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        private final Font smallBold14 = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.NORMAL);
        private final Font small10 = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.NORMAL);
        private final Font small12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

        private FileOutputStream fost;
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        private PdfWriter writer;

        public Pdf(String fileName) {

            try {

                event = new HeaderFooterPageEventOrdenCompra();

                queryString = " SELECT *";
                queryString += " FROM orden_compra ";
                queryString += " WHERE Id = " + idOrdenCompra;

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {
                    event.setContactoEnObra(rsRecords.getString("ContactoEnObra"));
                    event.setResponsable(rsRecords.getString("Responsable"));
                    event.setCotizacionReferencia(rsRecords.getString("CotizacionReferencia"));
                    event.setRazon(rsRecords.getString("Razon"));
                    total = rsRecords.getDouble("Total");
                    anticipo = rsRecords.getDouble("Anticipo");
                    if(rsRecords.getString("Moneda").equals("QUETZALES")) {
                        moneda = "Q.";
                    } else {
                        moneda = "$.";
                    }
                }

                event.setFirstLineReportTitle("ORDEN DE COMPRA " + nombreTipoOrdenCompra);
                event.setSecondLineReportTitle("PROYECTO : " + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrProjectName());
                event.setThirdLineReportTitle("PROVEEDOR : " + proveedorNombre);
                event.setIdOrdenCompra(idOrdenCompra);
                event.setCompanyName(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName());
                event.setCompanyTaxid("NIT : " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId());
                event.setCreadoUsuario(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY(new java.util.Date()));

                new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/pdfreceipts").mkdirs();

                fileName = RECEIPTFILE + fileName;

                Document document = new Document(PageSize.A4, 0, 0, 0, 0);
                Document document2 = new Document(PageSize.A4, 0, 0, 0, 0);
                fost = new FileOutputStream(fileName);

                document.setMargins(15, 15, 15, 15);
                document2.setMargins(15, 15, 15, 15);

                PdfWriter.getInstance(document, fost);

                writer = PdfWriter.getInstance(document2, os);

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

        private void addMetaData(Document document) {
            document.addTitle("Orden de compra " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
            document.addSubject("Using iText");
            document.addKeywords("Java, PDF, iText");
            document.addAuthor("www.sopdi.com");
            document.addCreator("www.sopdi.com");
        }

        private void addContent(Document document) throws DocumentException {

            Paragraph preface = new Paragraph();

            leerBaseDatosYEscribirLineas(document);

            preface = new Paragraph();
            addEmptyLine(preface, 1);
            document.add(preface);

            LineSeparator objectName = new LineSeparator();
            document.add(objectName);

        }

        private void leerBaseDatosYEscribirLineas(Document document)
                throws DocumentException {

            if (!tipoOrdenCompra.equals("1") && !tipoOrdenCompra.equals("2")) {
                addContentOrdenCompra(document);
            } else {
                addContentEstimacion(document);
            }
        }

        private void addContentOrdenCompra(Document document) throws DocumentException {
            PdfPTable reportTable2;
            PdfPCell c2 = null;

            reportTable2 = new PdfPTable(1);
            //idcc, idex, descripcion idex, monto
            float[] columnWidths2 = {1.5f};
            reportTable2.setWidths(columnWidths2);

            reportTable2.setSplitRows(false);
            reportTable2.setHeaderRows(1);
            reportTable2.setWidthPercentage(20);

            c2 = new PdfPCell(new Paragraph("Centro de Costo",smallBold14));
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setVerticalAlignment(Element.ALIGN_LEFT);
            c2.setBorderWidth(1);
//            c2.setBorderWidthTop(1);
//            c2.setBorderWidthBottom(1);
//            c2.setBorderWidthLeft(1);
//            c2.setBorderWidthRight(1);
            reportTable2.addCell(c2);

            queryString = " SELECT *";
            queryString += " FROM orden_compra_detalle ";
            queryString += " WHERE IdOrdenCompra = " + idOrdenCompra;

            try {

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {

                    do {

                        c2 = new PdfPCell(new Paragraph(rsRecords.getString("IDCC"), small10));
                        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c2.setVerticalAlignment(Element.ALIGN_CENTER);
                        c2.setBorderWidth(1);
//                        c2.setFixedHeight(10f);
                        reportTable2.addCell(c2);

                    } while (rsRecords.next());

                    c2 = new PdfPCell(new Paragraph("-------------", small10));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setVerticalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("Total : " + moneda + numberFormat.format(total) , smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("Anticipo : " +  moneda + numberFormat.format(anticipo), smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("Por pagar : " +  moneda + numberFormat.format(total-anticipo), smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("-------------", small10));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setVerticalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);
                }
            } catch (Exception e) {
                System.out.println("Error al buscar orden de compra detalle" + e);
                e.printStackTrace();
            }

            document.add(reportTable2);
        }

        private void addContentEstimacion(Document document) throws DocumentException {
            PdfPTable reportTable2;
            PdfPCell c2 = null;

            reportTable2 = new PdfPTable(4);
            //idcc, idex, descripcion idex, monto
            float[] columnWidths2 = {1.5f, 1.5f, 3.0f, 2.0f};
            reportTable2.setWidths(columnWidths2);

            reportTable2.setSplitRows(false);
            reportTable2.setHeaderRows(1);
            reportTable2.setWidthPercentage(90);

            c2 = new PdfPCell(new Paragraph("Centro de Costo",smallBold10));
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setVerticalAlignment(Element.ALIGN_LEFT);
            c2.setBorderWidth(1);
            c2.setBorderWidthTop(1);
            c2.setBorderWidthBottom(1);
            c2.setBorderWidthLeft(1);
            c2.setBorderWidthRight(1);
            reportTable2.addCell(c2);

            c2 = new PdfPCell(new Paragraph("IDEX", smallBold10));
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setVerticalAlignment(Element.ALIGN_LEFT);
            c2.setBorderWidth(1);
            c2.setBorderWidthTop(1);
            c2.setBorderWidthBottom(1);
            c2.setBorderWidthLeft(1);
            c2.setBorderWidthRight(1);
            reportTable2.addCell(c2);

            c2 = new PdfPCell(new Paragraph("Descripción", smallBold10));
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setVerticalAlignment(Element.ALIGN_LEFT);
            c2.setBorderWidth(1);
            c2.setBorderWidthTop(1);
            c2.setBorderWidthBottom(1);
            c2.setBorderWidthLeft(1);
            c2.setBorderWidthRight(1);
            reportTable2.addCell(c2);

            c2 = new PdfPCell(new Paragraph("MONTO", smallBold10));
            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c2.setVerticalAlignment(Element.ALIGN_RIGHT);
            c2.setBorderWidth(1);
            c2.setBorderWidthTop(1);
            c2.setBorderWidthBottom(1);
            c2.setBorderWidthLeft(1);
            c2.setBorderWidthRight(1);
            c2.setFixedHeight(10f);
            reportTable2.addCell(c2);

            queryString = " SELECT ODD.*, PIDX.Descripcion";
            queryString += " FROM orden_compra_detalle ODD";
            queryString += " LEFT JOIN project_tarea PIDX ON PIDX.IDEX = ODD.IDEX ";
            queryString += " WHERE ODD.IdOrdenCompra = " + idOrdenCompra;

            try {

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {

                    total = 0.00;

                    do {

                        c2 = new PdfPCell(new Paragraph(rsRecords.getString("IDCC"), small10));
                        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c2.setVerticalAlignment(Element.ALIGN_LEFT);
                        c2.setBorderWidth(0);
                        c2.setBorderWidthBottom(1);
                        c2.setBorderWidthLeft(1);
                        c2.setFixedHeight(10f);
                        reportTable2.addCell(c2);

                        c2 = new PdfPCell(new Paragraph(rsRecords.getString("IDEX"), small10));
                        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c2.setVerticalAlignment(Element.ALIGN_LEFT);
                        c2.setBorderWidth(0);
                        c2.setBorderWidthBottom(1);
                        c2.setBorderWidthLeft(1);
                        reportTable2.addCell(c2);

                        c2 = new PdfPCell(new Paragraph(rsRecords.getString("DESCRIPCION"), small10));
                        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c2.setVerticalAlignment(Element.ALIGN_LEFT);
                        c2.setBorderWidth(0);
                        c2.setBorderWidthBottom(1);
                        c2.setBorderWidthLeft(1);
                        reportTable2.addCell(c2);

                        c2 = new PdfPCell(new Paragraph(numberFormat.format(rsRecords.getDouble("Total")), small10));
                        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c2.setBorderWidth(0);
                        c2.setBorderWidthBottom(1);
                        c2.setBorderWidthLeft(1);
                        c2.setBorderWidthRight(1);
                        reportTable2.addCell(c2);

                        total += rsRecords.getDouble("Total");

                    } while (rsRecords.next());

                    c2 = new PdfPCell(new Paragraph(" ", small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("Total ", smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setVerticalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(numberFormat.format(total), smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(1);
                    c2.setBorderWidthRight(1);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("Anticipo", smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setVerticalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(numberFormat.format(anticipo), smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(1);
                    c2.setBorderWidthRight(1);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("Por pagar", smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setVerticalAlignment(Element.ALIGN_CENTER);
                    c2.setBorderWidth(0);
                    reportTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(numberFormat.format(total-anticipo), smallBold10));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(1);
                    c2.setBorderWidthRight(1);
                    reportTable2.addCell(c2);

                }
            } catch (Exception e) {
                System.out.println("Error al buscar orden de compra detalle" + e);
                e.printStackTrace();
            }

            document.add(reportTable2);

        }

        private void addFooterPage(Document document) {
            try {

                Paragraph preface = new Paragraph();

                // We add one empty line
                addEmptyLine(preface, 2);
                document.add(preface);

                PdfPTable reportTable2 = new PdfPTable(3);
                PdfPCell c2 = null;

                float[] columnWidths2 = {2.0f, 2.0f, 2.0f};

                reportTable2.setWidths(columnWidths2);
                reportTable2.setSplitRows(false);
                reportTable2.setHeaderRows(1);
                reportTable2.setWidthPercentage(90);

                c2 = new PdfPCell(new Paragraph("f ____________________", small10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setVerticalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(0);
                c2.setBorderWidthBottom(0);
                reportTable2.addCell(c2);

                c2 = new PdfPCell(new Paragraph("Responsable : " + event.getResponsable(), small10));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                c2.setVerticalAlignment(Element.ALIGN_LEFT);
                c2.setBorderWidth(0);
                c2.setBorderWidthBottom(0);
                reportTable2.addCell(c2);

                document.add(reportTable2);

            } catch (Exception ex) {
                Logger.getLogger(OrdenCompraEstimacionPDF.Pdf.class.getName()).log(Level.SEVERE, null, ex);
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
