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
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.MultiSelectionModel;
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

/**
 *
 * @author JAguirre
 */
public class EstimacionPDF extends Window {
    String fileName;
    String queryString;

    UI mainUI;
    String idEmpresa;
    String empresaNombre;
    String empresaNit;
    String estimacionId;
    String estimacionEstatus;
    String proveedorId;
    String proveedorNombre;
    IndexedContainer estimacionContainer;
    MultiSelectionModel selection;
    FooterRow footerRow;
    
    public final String RECEIPTFILE
            = VaadinService.getCurrent()
                    .getBaseDirectory().getAbsolutePath() + "/pdfreceipts/";


    public EstimacionPDF(
            String idEmpresa, 
            String empresaNombre, 
            String empresaNit,
            String estimacionId,
            String estimacionEstatus,
            String proveedorId,
            String proveedorNombre,
            IndexedContainer estimacionContainer,
            MultiSelectionModel selection,
            FooterRow footerRow) {
        this.idEmpresa = idEmpresa;
        this.empresaNombre = empresaNombre;
        this.empresaNit = empresaNit;
        this.estimacionId = estimacionId;
        this.estimacionEstatus = estimacionEstatus;
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
        this.estimacionContainer = estimacionContainer;
        this.selection = selection;
        this.footerRow = footerRow;
        
        try {
            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("1100");
            setHeight("700");
            center();

            fileName = "Estimacion_" + estimacionId + "_";
            fileName += estimacionEstatus + "_";
            fileName += empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ó", "o").replaceAll("é","") + "_" + proveedorNombre.replaceAll(" ", "_").replaceAll("ó", "o").replaceAll("é","") + "_" + new Utileria().getFechaHoraSinFormato() + ".pdf";

            StreamResource pdfResource;
            pdfResource = new StreamResource(new Pdf(fileName), fileName);
            pdfResource.setMIMEType("application/pdf");

            e.setSource(pdfResource);
            setContent(e);
            // despues de llamar Pdf()  fileName y tiene la ruta completa del archivo
            File file = new File(RECEIPTFILE + fileName);
            file.delete();

        } catch (Exception allEx) {
            Notification.show("Error al generar el documento de estimación.", Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }

    /**
     * This class creates a PDF with the iText library. This class implements
     * the StreamSource interface which defines the getStream method.
     */
    public class Pdf implements StreamResource.StreamSource, Serializable {

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
                
                writer = PdfWriter.getInstance(document2, os);
                HeaderFooterPageEvent event = new HeaderFooterPageEvent();
                
                event.setFirstLineReportTitle("PROFORMA DE ESTIMACION");
                event.setCompanyName(empresaNombre);
                event.setCompanyTaxid(empresaNit);
                event.setSecondLineReportTitle("ESTIMACION # " + estimacionId + " " + proveedorId + " " + proveedorNombre);
                event.setThirdLineReportTitle(estimacionEstatus);
                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));
                writer.setPageEvent(event);

                document.open();
                document2.open();

                addMetaData(document);
                //addTitlePage(document);
                addContent(document);
                addFooterPage(document);
                document.close();

                addMetaData(document2);
                //addTitlePage(document2);
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
            document.addTitle("Reporte de estimacion " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
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

            PdfPTable reportTable = new PdfPTable(8);
            PdfPCell c1 = null;

            float[] columnWidths = {1.0f, 1.0f, 3.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};

            reportTable.setWidths(columnWidths);
            reportTable.setSplitRows(false);
            reportTable.setHeaderRows(1);
            reportTable.setWidthPercentage(95);

            c1 = new PdfPCell(new Paragraph("IDEX", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("C.COSTO", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("DESCRIPCION", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("CANT.", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_CENTER);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("PRECIO", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("TOTAL", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("MONEDA", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("PROJECT", smallBold10));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_CENTER);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            reportTable.addCell(c1);

            try {

                for (Object itemId: selection.getSelectedRows()) {

                    c1 = new PdfPCell(new Paragraph(String.valueOf(estimacionContainer.getContainerProperty(itemId, EstimacionesWindow.IDEX_PROPERTY).getValue()), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    reportTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(String.valueOf(estimacionContainer.getContainerProperty(itemId, EstimacionesWindow.CCOSTO_PROPERTY).getValue()), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    reportTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(String.valueOf(estimacionContainer.getContainerProperty(itemId, EstimacionesWindow.DESCRIPCION_PROPERTY).getValue()), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    reportTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(String.valueOf(estimacionContainer.getContainerProperty(itemId, EstimacionesWindow.CANTIDAD_PROPERTY).getValue()), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c1.setVerticalAlignment(Element.ALIGN_CENTER);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    c1.setFixedHeight(10f);                    
                    reportTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(String.valueOf(estimacionContainer.getContainerProperty(itemId, EstimacionesWindow.PRECIO_PROPERTY).getValue()), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    reportTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(String.valueOf(estimacionContainer.getContainerProperty(itemId, EstimacionesWindow.TOTAL_PROPERTY).getValue()), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    reportTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(String.valueOf(estimacionContainer.getContainerProperty(itemId, EstimacionesWindow.MONEDA_PROPERTY).getValue()), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    reportTable.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(String.valueOf(estimacionContainer.getContainerProperty(itemId, EstimacionesWindow.PROJECT_PROPERTY).getValue()), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c1.setVerticalAlignment(Element.ALIGN_CENTER);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    reportTable.addCell(c1);

                }// end for
                
                c1 = new PdfPCell(new Paragraph("--------", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("--------", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("--------", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("--------", small10));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                c1.setFixedHeight(10f);                    
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("--------", small10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("--------", small10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("--------", small10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("--------", small10));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);
                
                Object itemId = estimacionContainer.lastItemId();

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

                c1 = new PdfPCell(new Paragraph("TOTALES", this.smallBold10));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setVerticalAlignment(Element.ALIGN_LEFT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("", small10));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                c1.setFixedHeight(10f);                    
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph("", small10));
                c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
                reportTable.addCell(c1);

                c1 = new PdfPCell(new Paragraph(footerRow.getCell(EstimacionesWindow.TOTAL_PROPERTY).getText(), smallBold10));
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
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setVerticalAlignment(Element.ALIGN_CENTER);
                c1.setBorderWidth(0);
                c1.setBorderWidthBottom(0);
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
