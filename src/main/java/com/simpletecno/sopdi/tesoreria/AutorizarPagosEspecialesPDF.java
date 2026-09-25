package com.simpletecno.sopdi.tesoreria;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.simpletecno.sopdi.HeaderFooterPageEvent;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import java.io.*;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reporte PDF – Pagos Especiales Autorizados.
 * Usa el container propio de AutorizarPagosEspecialesView.
 */
public class AutorizarPagosEspecialesPDF extends Window {

    String fileName;
    private Pdf pdfContent;

    public AutorizarPagosEspecialesPDF(IndexedContainer porPagarContainer) {
        try {
            BrowserFrame browser = new BrowserFrame();
            browser.setSizeFull();
            setWidth("95%");
            setHeight("90%");
            center();

            String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

            fileName = "PagosEspeciales_"
                    + empresaNombre.replaceAll("[ ,().]+", "_")
                    + "_" + new Utileria().getFechaHoraSinFormato()
                    + ".pdf";

            this.pdfContent = new Pdf(fileName, porPagarContainer);
            StreamResource pdfResource = new StreamResource(this.pdfContent, fileName);
            pdfResource.setMIMEType("application/pdf");

            browser.setSource(pdfResource);
            setContent(browser);

            new File(fileName).delete();

        } catch (Exception ex) {
            Notification.show("Error al generar el reporte PDF.", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(AutorizarPagosEspecialesPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** Retorna los bytes del PDF generado (para guardarlo en BD o enviarlo). */
    public byte[] getPdfBytes() {
        return pdfContent != null ? pdfContent.getBytes() : new byte[0];
    }

    // =========================================================================

    public class Pdf implements StreamResource.StreamSource, Serializable {

        private final String RECEIPTFILE =
                VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/pdfreceipts/";

        private static final int HDR_R = 26,  HDR_G = 82,  HDR_B = 118;
        private static final int ODD_R = 235, ODD_G = 245, ODD_B = 251;
        private static final int TOT_R = 21,  TOT_G = 67,  TOT_B = 96;
        private static final int TXT_R = 44,  TXT_G = 62,  TXT_B = 80;

        private final BaseColor colorEncabezado = new BaseColor(HDR_R, HDR_G, HDR_B);
        private final BaseColor colorFilaImpar  = new BaseColor(ODD_R, ODD_G, ODD_B);
        private final BaseColor colorTotal      = new BaseColor(TOT_R, TOT_G, TOT_B);
        private final BaseColor colorTextoDatos = new BaseColor(TXT_R, TXT_G, TXT_B);

        private final Font fHdrTexto  = new Font(Font.FontFamily.HELVETICA,  9f, Font.BOLD,   BaseColor.WHITE);
        private final Font fDatos     = new Font(Font.FontFamily.HELVETICA,  8.5f, Font.NORMAL, colorTextoDatos);
        private final Font fDatosBold = new Font(Font.FontFamily.HELVETICA,  8.5f, Font.BOLD,   colorTextoDatos);
        private final Font fMonto     = new Font(Font.FontFamily.HELVETICA,  8.5f, Font.BOLD,   colorTextoDatos);
        private final Font fTotal     = new Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD,   BaseColor.WHITE);
        private final Font fFirma     = new Font(Font.FontFamily.HELVETICA,  9f, Font.NORMAL, colorTextoDatos);

        private static final float RADIO = 4f;
        private static final float PAD_H = 6f;
        private static final float PAD_V = 5f;

        private final DecimalFormat df = new DecimalFormat("###,##0.00");

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();
        private final IndexedContainer container;

        public Pdf(String pFileName, IndexedContainer porPagarContainer) {
            this.container = porPagarContainer;
            try {
                new File(RECEIPTFILE).mkdirs();
                String fullPath = RECEIPTFILE + pFileName;

                Document doc     = new Document(PageSize.A4.rotate(), 15, 15, 15, 15);
                Document docFile = new Document(PageSize.A4.rotate(), 15, 15, 15, 15);
                PdfWriter.getInstance(docFile, new FileOutputStream(fullPath));

                PdfWriter writer = PdfWriter.getInstance(doc, os);

                HeaderFooterPageEvent event = new HeaderFooterPageEvent();
                event.setFirstLineReportTitle("PAGOS ESPECIALES AUTORIZADOS");
                event.setCompanyName(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName());
                event.setCompanyTaxid("NIT : " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId());
                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));
                writer.setPageEvent(event);

                doc.open();
                docFile.open();

                addMetaData(doc);
                addContent(doc);
                addFooter(doc);
                doc.close();

                addMetaData(docFile);
                addContent(docFile);
                addFooter(docFile);
                docFile.close();

            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(os.toByteArray());
        }

        public byte[] getBytes() {
            return os.toByteArray();
        }

        private void addMetaData(Document document) {
            document.addTitle("Pagos Especiales Autorizados");
            document.addAuthor("www.sopdi.com");
            document.addCreator("www.sopdi.com");
        }

        private void addContent(Document document) throws DocumentException {
            document.add(new Paragraph(" "));
            escribirTabla(document);
            document.add(new Paragraph(" "));
        }

        private void escribirTabla(Document document) throws DocumentException {

            // No. | Tipo | Proveedor | Fecha | Moneda | Monto Cheque | # Cheque | Cód. Partida
            PdfPTable table = new PdfPTable(8);
            float[] colWidths = {0.35f, 2.20f, 2.80f, 1.00f, 1.00f, 1.30f, 1.00f, 2.20f};
            table.setWidths(colWidths);
            table.setWidthPercentage(100);
            table.setSplitRows(true);
            table.setHeaderRows(1);
            table.setSpacingBefore(4f);

            agregarEncabezado(table, "No.",           Element.ALIGN_CENTER);
            agregarEncabezado(table, "TIPO",          Element.ALIGN_LEFT);
            agregarEncabezado(table, "PROVEEDOR",     Element.ALIGN_LEFT);
            agregarEncabezado(table, "FECHA",         Element.ALIGN_CENTER);
            agregarEncabezado(table, "MONEDA",        Element.ALIGN_CENTER);
            agregarEncabezado(table, "MONTO",         Element.ALIGN_RIGHT);
            agregarEncabezado(table, "#CHEQUE",       Element.ALIGN_CENTER);
            agregarEncabezado(table, "CÓD. PARTIDA",  Element.ALIGN_LEFT);

            int correlativo = 1;
            int filaIndex   = 0;
            double totalMonto = 0.00;

            for (Object itemId : container.getItemIds()) {

                double monto = parseDouble(container.getContainerProperty(itemId,
                        AutorizarPagosEspecialesView.A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue());
                if (monto <= 0.00) continue;

                String tipo     = nvl(container.getContainerProperty(itemId, AutorizarPagosEspecialesView.TIPO_DOCUMENTO_PROPERTY).getValue());
                String proveedor= nvl(container.getContainerProperty(itemId, AutorizarPagosEspecialesView.PROVEEDOR_PROPERTY).getValue());
                String fecha    = nvl(container.getContainerProperty(itemId, AutorizarPagosEspecialesView.FECHA_PROPERTY).getValue());
                String moneda   = nvl(container.getContainerProperty(itemId, AutorizarPagosEspecialesView.MONEDA_PROPERTY).getValue());
                String montoCheque = nvl(container.getContainerProperty(itemId, AutorizarPagosEspecialesView.A_LIQUIDAR_MONTO_CHEQUE_PROPERTY).getValue());
                String noCheque = nvl(container.getContainerProperty(itemId, AutorizarPagosEspecialesView.CHEQUE_PROPERTY).getValue());
                // CodigoPartida — puede existir o no según si ya se procesó
                com.vaadin.data.Property<?> partProp = container.getContainerProperty(itemId, AutorizarPagosEspecialesView.CODIGO_PARTIDA_PROPERTY);
                String partida = partProp != null ? nvl(partProp.getValue()) : "";

                boolean filaImpar = (filaIndex % 2 == 0);
                BaseColor fondoFila = filaImpar ? colorFilaImpar : BaseColor.WHITE;

                agregarDato(table, String.valueOf(correlativo++), Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, tipo,       Element.ALIGN_LEFT,   fondoFila, fDatos);
                agregarDato(table, proveedor,  Element.ALIGN_LEFT,   fondoFila, fDatos);
                agregarDato(table, fecha,      Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, moneda,     Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, montoCheque,Element.ALIGN_RIGHT,  fondoFila, fMonto);
                agregarDato(table, noCheque,   Element.ALIGN_CENTER, fondoFila, fDatosBold);
                agregarDato(table, partida,    Element.ALIGN_LEFT,   fondoFila, fDatos);

                totalMonto += monto;
                filaIndex++;
            }

            agregarCeldaTotal(table, "TOTAL",                  Element.ALIGN_RIGHT, 5);
            agregarCeldaTotal(table, df.format(totalMonto),    Element.ALIGN_RIGHT, 1);
            agregarCeldaTotal(table, "",                       Element.ALIGN_LEFT,  2);

            document.add(table);
        }

        private void addFooter(Document document) {
            try {
                Paragraph espacio = new Paragraph(" ");
                espacio.setSpacingBefore(20f);
                document.add(espacio);

                PdfPTable firmasTable = new PdfPTable(3);
                firmasTable.setWidthPercentage(90);
                firmasTable.setSpacingBefore(30f);

                String[] puestos = {"Contador", "Depto. Financiero", "Gerencia General"};
                for (String puesto : puestos) {
                    PdfPCell cell = new PdfPCell();
                    cell.setBorderWidth(0);
                    cell.setBorderWidthTop(1);
                    cell.setBorderColor(colorTextoDatos);
                    cell.setPaddingTop(6f);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    Paragraph p = new Paragraph(puesto, fFirma);
                    p.setAlignment(Element.ALIGN_CENTER);
                    cell.addElement(p);
                    firmasTable.addCell(cell);
                }
                document.add(firmasTable);
            } catch (Exception ex) {
                Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void agregarEncabezado(PdfPTable table, String texto, int alignment) {
            PdfPCell cell = new PdfPCell(new Phrase(texto, fHdrTexto));
            cell.setHorizontalAlignment(alignment);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingLeft(PAD_H);
            cell.setPaddingRight(PAD_H);
            cell.setPaddingTop(PAD_V + 1);
            cell.setPaddingBottom(PAD_V + 1);
            cell.setCellEvent(new CeldaRedondeada(colorEncabezado, RADIO));
            table.addCell(cell);
        }

        private void agregarDato(PdfPTable table, String texto, int alignment, BaseColor fondo, Font font) {
            PdfPCell cell = new PdfPCell(new Phrase(texto, font));
            cell.setHorizontalAlignment(alignment);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingLeft(PAD_H);
            cell.setPaddingRight(PAD_H);
            cell.setPaddingTop(PAD_V - 1);
            cell.setPaddingBottom(PAD_V - 1);
            cell.setCellEvent(new CeldaRedondeada(fondo, 2f));
            table.addCell(cell);
        }

        private void agregarCeldaTotal(PdfPTable table, String texto, int alignment, int colspan) {
            PdfPCell cell = new PdfPCell(new Phrase(texto, fTotal));
            cell.setColspan(colspan);
            cell.setHorizontalAlignment(alignment);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingLeft(PAD_H);
            cell.setPaddingRight(PAD_H);
            cell.setPaddingTop(PAD_V + 2);
            cell.setPaddingBottom(PAD_V + 2);
            cell.setCellEvent(new CeldaRedondeada(colorTotal, RADIO));
            table.addCell(cell);
        }

        private double parseDouble(Object value) {
            try {
                if (value == null) return 0.00;
                String s = String.valueOf(value).replaceAll("[^0-9.]", "");
                return s.isEmpty() ? 0.00 : Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                return 0.00;
            }
        }

        private String nvl(Object value) {
            return value == null ? "" : String.valueOf(value);
        }

        private class CeldaRedondeada implements PdfPCellEvent {
            private final BaseColor fondo;
            private final float radio;

            CeldaRedondeada(BaseColor fondo, float radio) {
                this.fondo = fondo;
                this.radio = radio;
            }

            @Override
            public void cellLayout(PdfPCell cell, Rectangle pos, PdfContentByte[] canvas) {
                PdfContentByte cb = canvas[PdfPTable.BACKGROUNDCANVAS];
                cb.saveState();
                cb.setColorFill(fondo);
                cb.roundRectangle(pos.getLeft() + 0.5f, pos.getBottom() + 0.5f,
                        pos.getWidth() - 1f, pos.getHeight() - 1f, radio);
                cb.fill();
                cb.restoreState();
            }
        }
    }
}
