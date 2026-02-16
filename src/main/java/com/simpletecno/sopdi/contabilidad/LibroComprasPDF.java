/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

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
import com.vaadin.data.util.IndexedContainer;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.simpletecno.sopdi.contabilidad.LibroComprasView.*;

/**
 *
 * @author JAguirre
 */
public class LibroComprasPDF extends Window {
    String fileName;
    String idEmpresa;
    String empresaNombre;
    String empresaNit;
    IndexedContainer libroComprasContainer;
    String fecha;
    String folioInicial;

    public LibroComprasPDF(
            String idEmpresa, 
            String empresaNombre, 
            String empresaNit,
            IndexedContainer librodiario,
            String fecha,
            String folioInicial
            ) {
        this.idEmpresa = idEmpresa;
        this.empresaNombre = empresaNombre;
        this.empresaNit = empresaNit;
        this.libroComprasContainer = librodiario;
        this.fecha= fecha;
        this.folioInicial = folioInicial;

        try {
            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("95%");
            setHeight("90%");
            center();

            fileName = "LibroCompras_" + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "") + "_" + fecha + "_" + new Utileria().getHora() + ".pdf";

            StreamResource pdfResource;
            pdfResource = new StreamResource(new Pdf(fileName), fileName);
            pdfResource.setMIMEType("application/pdf");

            e.setSource(pdfResource);
            setContent(e);
            // despues de llamar Pdf()  fileName y tiene la ruta completa del archivo
            File file = new File(fileName);
            file.delete();

        } catch (Exception allEx) {
            Notification.show("Error al generar el libro compras.", Type.ERROR_MESSAGE);
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

                Document document = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);
                Document document2 = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);
                fost = new FileOutputStream(fileName);

                document.setMargins(15, 15, 15, 15);
                document2.setMargins(15, 15, 15, 15);

                PdfWriter.getInstance(document, fost);
                
                writer = PdfWriter.getInstance(document2, os);
                HeaderFooterPageEvent event = new HeaderFooterPageEvent();

                event.setFirstLineReportTitle("LIBRO DE COMPRAS");
                event.setCompanyName(empresaNombre);
                event.setCompanyTaxid(empresaNit);
//                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserName());
//                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));
                event.setSecondLineReportTitle(fecha.substring(0,2) + "/" + fecha.substring(2,6));
                event.setFolioInicial(Double.valueOf(folioInicial).intValue());

                writer.setPageEvent(event);

                document.open();
                document2.open();

                addMetaData(document);
                //addTitlePage(document);
                addContent(document);
//                addFooterPage(document);
                document.close();

                addMetaData(document2);
                //addTitlePage(document2);
                addContent(document2);
//                addFooterPage(document2);
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
            document.addTitle("Reporte de documentos " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
            document.addSubject("Using iText");
            document.addKeywords("Java, PDF, iText");
            document.addAuthor("www.sopdi.com");
            document.addCreator("www.sopdi.com");
        }

        private void addContent(Document document) throws DocumentException {

            Paragraph preface = new Paragraph();

//            addEmptyLine(preface, 1);
//            document.add(preface);

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

            PdfPTable reportTable = new PdfPTable(14);
            PdfPCell c1 = null;

            float[] columnWidths = {1.5f, 1.5f, 2.0f, 1.5f, 4.0f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f};

            reportTable.setWidths(columnWidths);
            reportTable.setSplitRows(false);
            reportTable.setHeaderRows(1);
            reportTable.setWidthPercentage(98);

            c1 = new PdfPCell(new Paragraph("FECHA", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("SERIE", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("FACTURA", smallBold10));
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

            c1 = new PdfPCell(new Paragraph("PROVEEDOR", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("IVA REM.", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("PEQ.CONT.", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("NAFECTO", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("COMBUST", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("COMPRA", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("SERVICIO", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("IMPORT.", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("IVA", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("MONTO", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            try {

                for (Object itemId : libroComprasContainer.getItemIds()) {

//System.out.println(libroComprasContainer.getContainerProperty(itemId, FECHA_PROPERTY).getValue());

                    if(String.valueOf(libroComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).getValue()).toUpperCase().equals("TOTALES")) {
                        if(String.valueOf(libroComprasContainer.getContainerProperty(itemId, FECHA_PROPERTY).getValue()).trim().isEmpty()) {
                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            c1.setFixedHeight(10f);                    
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            c1.setFixedHeight(10f);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //IVA REM.
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //peq.cont.
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //no afecto
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //combustibles
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //compra
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //servicio
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //importaciones
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //iva
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("----------", small10)); //monto
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);                            
                                                        
                            
                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            c1.setFixedHeight(10f);                    
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            c1.setFixedHeight(10f);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, IVAREM_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, IMPORTACION_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, IVA_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue()), smallBold));
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            
                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            c1.setFixedHeight(10f);                    
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("", small10));
                            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            c1.setVerticalAlignment(Element.ALIGN_LEFT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            c1.setFixedHeight(10f);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("==========", small10)); //PEQ CONT
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("==========", small10));// NO AFECTO
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("==========", small10)); //COMBUSTIBLES
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("==========", small10)); //COMPRAS
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("==========", small10)); //SERVICIOS
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("==========", small10)); //IMPORTACIONES
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("==========", small10)); //IVA
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);

                            c1 = new PdfPCell(new Paragraph("==========", small10)); //MONTO
                            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                            c1.setBorderWidth(0);
                            c1.setBorderWidthBottom(0);
                            reportTable.addCell(c1);
                        }
                        
                    }
                    else {
                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, FECHA_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, SERIE_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, FACTURA_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, NIT_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        c1.setFixedHeight(10f);                    
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        c1.setFixedHeight(10f);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, IVAREM_PROPERTY).getValue()), smallBold));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, IMPORTACION_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, IVA_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);

                        c1 = new PdfPCell(new Paragraph(String.valueOf(libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue()), small10));
                        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c1.setBorderWidth(0);
                        c1.setBorderWidthBottom(0);
                        reportTable.addCell(c1);
                    }

                }

                document.add(reportTable);
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

        private void addFooterPage(Document document) {
            try {

                Paragraph preface = new Paragraph();

                // We add one empty line
                addEmptyLine(preface, 2);
                document.add(preface);

                Paragraph firmas = new Paragraph("f ____________________                           "
                        + "                    f_____________________                           "
                        + "          f ____________________                           ", smallBold10);
                firmas.setAlignment(Element.ALIGN_LEFT);
                preface.add(firmas);

                Paragraph puestos = new Paragraph("        Contador                                       "
                        + "                              Depto Financiero                               "
                        + "                         Gerencia General                                ", smallBold10);
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
