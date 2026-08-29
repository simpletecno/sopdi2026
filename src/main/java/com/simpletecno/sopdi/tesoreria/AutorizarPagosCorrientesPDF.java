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
 * Reporte PDF – Pagos Corrientes Autorizados.
 *
 * Fuente de datos : porPagarContainer (Tab 1) + anticiposOCContainer (Tab 2)
 * Filtro Tab 1    : A_LIQUIDAR_ANTICIPOS + A_LIQUIDAR_MONTO_CHEQUE > 0
 * Filtro Tab 2    : OC_CHEQUE_OC_PROPERTY no vacío
 */
public class AutorizarPagosCorrientesPDF extends Window {

    String fileName;

    /** Constructor de compatibilidad para vistas que no tienen anticipos OC. */
    public AutorizarPagosCorrientesPDF(IndexedContainer porPagarContainer) {
        this(porPagarContainer, new IndexedContainer());
    }

    public AutorizarPagosCorrientesPDF(IndexedContainer porPagarContainer,
                                       IndexedContainer anticiposOCContainer) {
        try {
            BrowserFrame browser = new BrowserFrame();
            browser.setSizeFull();
            setWidth("95%");
            setHeight("90%");
            center();

            String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

            fileName = "PagosCorrientes_"
                    + empresaNombre.replaceAll("[ ,().]+", "_")
                    + "_" + new Utileria().getFechaHoraSinFormato()
                    + ".pdf";

            StreamResource pdfResource = new StreamResource(
                    new Pdf(fileName, porPagarContainer, anticiposOCContainer), fileName);
            pdfResource.setMIMEType("application/pdf");

            browser.setSource(pdfResource);
            setContent(browser);

            new File(fileName).delete();

        } catch (Exception ex) {
            Notification.show("Error al generar el reporte PDF.", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(AutorizarPagosCorrientesPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // =========================================================================

    public class Pdf implements StreamResource.StreamSource, Serializable {

        private final String RECEIPTFILE =
                VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/pdfreceipts/";

        // ── Paleta de colores ────────────────────────────────────────────────
        // Encabezado: azul corporativo oscuro
        private static final int HDR_R = 26,  HDR_G = 82,  HDR_B = 118;
        // Fila impar: azul muy claro
        private static final int ODD_R = 235, ODD_G = 245, ODD_B = 251;
        // Fila total: azul más oscuro que el encabezado
        private static final int TOT_R = 21,  TOT_G = 67,  TOT_B = 96;
        // Texto oscuro para filas de datos
        private static final int TXT_R = 44,  TXT_G = 62,  TXT_B = 80;

        private final BaseColor colorEncabezado = new BaseColor(HDR_R, HDR_G, HDR_B);
        private final BaseColor colorFilaImpar  = new BaseColor(ODD_R, ODD_G, ODD_B);
        private final BaseColor colorTotal      = new BaseColor(TOT_R, TOT_G, TOT_B);
        private final BaseColor colorTextoDatos = new BaseColor(TXT_R, TXT_G, TXT_B);

        // ── Fuentes (Helvetica = moderna, legible, profesional) ──────────────
        private final Font fHdrTexto  = new Font(Font.FontFamily.HELVETICA,  9f, Font.BOLD,   BaseColor.WHITE);
        private final Font fDatos     = new Font(Font.FontFamily.HELVETICA,  8.5f, Font.NORMAL, colorTextoDatos);
        private final Font fDatosBold = new Font(Font.FontFamily.HELVETICA,  8.5f, Font.BOLD,   colorTextoDatos);
        private final Font fMonto     = new Font(Font.FontFamily.HELVETICA,  8.5f, Font.BOLD,   colorTextoDatos);
        private final Font fTotal     = new Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD,   BaseColor.WHITE);
        private final Font fFirma     = new Font(Font.FontFamily.HELVETICA,  9f, Font.NORMAL, colorTextoDatos);

        private static final float RADIO   = 4f;   // radio de esquinas redondeadas
        private static final float PAD_H   = 6f;   // padding horizontal celdas
        private static final float PAD_V   = 5f;   // padding vertical celdas

        private final DecimalFormat df = new DecimalFormat("###,##0.00");

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();
        private final IndexedContainer container;
        private final IndexedContainer containerOC;

        // ── Constructor ──────────────────────────────────────────────────────

        public Pdf(String pFileName, IndexedContainer porPagarContainer,
                   IndexedContainer anticiposOCContainer) {
            this.container   = porPagarContainer;
            this.containerOC = anticiposOCContainer;
            try {
                new File(RECEIPTFILE).mkdirs();
                String fullPath = RECEIPTFILE + pFileName;

                // Documento en pantalla (stream)
                Document doc = new Document(PageSize.A4.rotate(), 15, 15, 15, 15);
                // Copia en disco (se borra después)
                Document docFile = new Document(PageSize.A4.rotate(), 15, 15, 15, 15);
                PdfWriter.getInstance(docFile, new FileOutputStream(fullPath));

                PdfWriter writer = PdfWriter.getInstance(doc, os);

                HeaderFooterPageEvent event = new HeaderFooterPageEvent();
                event.setFirstLineReportTitle("PAGOS CORRIENTES AUTORIZADOS");
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

        // ── Estructura del documento ─────────────────────────────────────────

        private void addMetaData(Document document) {
            document.addTitle("Pagos Corrientes Autorizados");
            document.addAuthor("www.sopdi.com");
            document.addCreator("www.sopdi.com");
        }

        private void addContent(Document document) throws DocumentException {
            document.add(new Paragraph(" "));
            escribirTabla(document);
            document.add(new Paragraph(" "));

            // Sección de anticipos OC (sólo si hay registros con cheque asignado)
            boolean hayAnticipOC = false;
            for (Object id : containerOC.getItemIds()) {
                if (!nvl(containerOC.getContainerProperty(id,
                        AutorizarPagosCorrientesView.OC_CHEQUE_OC_PROPERTY).getValue()).isEmpty()) {
                    hayAnticipOC = true;
                    break;
                }
            }
            if (hayAnticipOC) {
                Paragraph titulo = new Paragraph("SOLICITUDES DE ANTICIPOS – ÓRDENES DE COMPRA",
                        new Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, colorEncabezado));
                titulo.setSpacingBefore(12f);
                document.add(titulo);
                document.add(new Paragraph(" "));
                escribirTablaOC(document);
                document.add(new Paragraph(" "));
            }
        }

        // ── Tabla principal ──────────────────────────────────────────────────

        private void escribirTabla(Document document) throws DocumentException {

            // Columnas: No. | Proveedor | Fecha | No.Doc | A Liquidar | Anticipos | Cheque | #Cheque | Cód. Partida
            int COLS = 9;
            PdfPTable table = new PdfPTable(COLS);
            float[] colWidths = {0.35f, 2.80f, 1.00f, 1.70f, 1.10f, 1.10f, 1.10f, 1.00f, 2.20f};
            table.setWidths(colWidths);
            table.setWidthPercentage(100);
            table.setSplitRows(true);
            table.setHeaderRows(1);
            table.setSpacingBefore(4f);

            // ── Fila de encabezado ───────────────────────────────────────────
            agregarEncabezado(table, "No.",           Element.ALIGN_CENTER);
            agregarEncabezado(table, "PROVEEDOR",     Element.ALIGN_LEFT);
            agregarEncabezado(table, "FECHA",         Element.ALIGN_CENTER);
            agregarEncabezado(table, "NO. DOCUMENTO", Element.ALIGN_LEFT);
            agregarEncabezado(table, "A LIQUIDAR",    Element.ALIGN_CENTER);
            agregarEncabezado(table, "ANTICIPOS",     Element.ALIGN_CENTER);
            agregarEncabezado(table, "CHEQUE",        Element.ALIGN_CENTER);
            agregarEncabezado(table, "#CHEQUE",       Element.ALIGN_CENTER);
            agregarEncabezado(table, "CÓD. PARTIDA",  Element.ALIGN_LEFT);

            // ── Filas de datos (filtradas) ────────────────────────────────────
            int correlativo  = 1;
            int filaIndex    = 0;
            double totalLiquidar = 0.00;

            for (Object itemId : container.getItemIds()) {

                double anticipo = parseDouble(container.getContainerProperty(itemId,
                        AutorizarPagosCorrientesView.A_LIQUIDAR_ANTICIPOS_PROPERTY).getValue());
                double montoCheque = parseDouble(container.getContainerProperty(itemId,
                        AutorizarPagosCorrientesView.A_LIQUIDAR_MONTO_CHEQUE_PROPERTY).getValue());

                if ((anticipo + montoCheque) <= 0.00) continue;

//                String idProveedor = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.ID_PROVEEDOR_PROPERTY).getValue());
                String proveedor   = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.PROVEEDOR_PROPERTY).getValue());
                String fecha       = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.FECHA_PROPERTY).getValue());
                String numeroDoc   = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.NUMERO_FACTURA_PROPERTY).getValue());
                String aLiquidar   = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.A_LIQUIDAR_PROPERTY).getValue());
                String anticipos   = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.A_LIQUIDAR_ANTICIPOS_PROPERTY).getValue());
                String cheque      = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.A_LIQUIDAR_MONTO_CHEQUE_PROPERTY).getValue());
                String noCheque    = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.CHEQUE_PROPERTY).getValue());
                String partida     = nvl(container.getContainerProperty(itemId, AutorizarPagosCorrientesView.CODIGO_PARTIDA_PAGO_PROPERTY).getValue());

                boolean filaImpar = (filaIndex % 2 == 0);
                BaseColor fondoFila = filaImpar ? colorFilaImpar : BaseColor.WHITE;

                agregarDato(table, String.valueOf(correlativo++), Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, proveedor,    Element.ALIGN_LEFT,   fondoFila, fDatos);
                agregarDato(table, fecha,        Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, numeroDoc,    Element.ALIGN_LEFT,   fondoFila, fDatos);
                agregarDato(table, aLiquidar,    Element.ALIGN_RIGHT,  fondoFila, fMonto);
                agregarDato(table, anticipos,    Element.ALIGN_RIGHT,  fondoFila, fMonto);
                agregarDato(table, cheque,       Element.ALIGN_RIGHT,  fondoFila, fMonto);
                agregarDato(table, noCheque,     Element.ALIGN_CENTER, fondoFila, fDatosBold);
                agregarDato(table, partida,      Element.ALIGN_LEFT,   fondoFila, fDatos);

                totalLiquidar += (anticipo + montoCheque);
                filaIndex++;
            }

            // ── Fila de total ─────────────────────────────────────────────────
            agregarCeldaTotal(table, "TOTAL", Element.ALIGN_RIGHT, 6);
            agregarCeldaTotal(table, df.format(totalLiquidar), Element.ALIGN_RIGHT, 1);
            agregarCeldaTotal(table, "",      Element.ALIGN_LEFT,  2);

            document.add(table);
        }

        // ── Tabla de anticipos OC ────────────────────────────────────────────

        private void escribirTablaOC(Document document) throws DocumentException {

            // Columnas: No. | NOC | Proveedor | Fecha | Moneda | Anticipo | #Cheque | C.Costos | Cód. Partida
            PdfPTable table = new PdfPTable(9);
            float[] colWidths = {0.35f, 1.20f, 2.80f, 1.00f, 1.00f, 1.30f, 1.00f, 1.20f, 2.20f};
            table.setWidths(colWidths);
            table.setWidthPercentage(100);
            table.setSplitRows(true);
            table.setHeaderRows(1);
            table.setSpacingBefore(4f);

            agregarEncabezado(table, "No.",          Element.ALIGN_CENTER);
            agregarEncabezado(table, "NOC",          Element.ALIGN_LEFT);
            agregarEncabezado(table, "PROVEEDOR",    Element.ALIGN_LEFT);
            agregarEncabezado(table, "FECHA",        Element.ALIGN_CENTER);
            agregarEncabezado(table, "MONEDA",       Element.ALIGN_CENTER);
            agregarEncabezado(table, "ANTICIPO",     Element.ALIGN_RIGHT);
            agregarEncabezado(table, "#CHEQUE",      Element.ALIGN_CENTER);
            agregarEncabezado(table, "C.COSTOS",     Element.ALIGN_CENTER);
            agregarEncabezado(table, "CÓD. PARTIDA", Element.ALIGN_LEFT);

            int correlativo = 1;
            int filaIndex   = 0;
            double totalAnticipo = 0.00;

            for (Object itemId : containerOC.getItemIds()) {
                String noCheque = nvl(containerOC.getContainerProperty(itemId,
                        AutorizarPagosCorrientesView.OC_CHEQUE_OC_PROPERTY).getValue());
                if (noCheque.isEmpty()) continue;

                String noc        = nvl(containerOC.getContainerProperty(itemId, AutorizarPagosCorrientesView.OC_NOC_PROPERTY).getValue());
                String proveedor  = nvl(containerOC.getContainerProperty(itemId, AutorizarPagosCorrientesView.OC_PROVEEDOR_OC_PROPERTY).getValue());
                String fecha      = nvl(containerOC.getContainerProperty(itemId, AutorizarPagosCorrientesView.OC_FECHA_OC_PROPERTY).getValue());
                String moneda     = nvl(containerOC.getContainerProperty(itemId, AutorizarPagosCorrientesView.OC_MONEDA_OC_PROPERTY).getValue());
                String anticipo   = nvl(containerOC.getContainerProperty(itemId, AutorizarPagosCorrientesView.OC_ANTICIPO_OC_PROPERTY).getValue());
                double montoNum   = parseDouble(containerOC.getContainerProperty(itemId, AutorizarPagosCorrientesView.OC_ANTICIPO_SF_OC_PROPERTY).getValue());
                String ccostos  = nvl(containerOC.getContainerProperty(itemId, AutorizarPagosCorrientesView.OC_CENTROS_COSTO_PROPERTY).getValue());
                String partida  = nvl(containerOC.getContainerProperty(itemId, AutorizarPagosCorrientesView.OC_CODIGO_PARTIDA_PAGO_PROPERTY).getValue());

                boolean filaImpar = (filaIndex % 2 == 0);
                BaseColor fondoFila = filaImpar ? colorFilaImpar : BaseColor.WHITE;

                agregarDato(table, String.valueOf(correlativo++), Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, noc,      Element.ALIGN_LEFT,   fondoFila, fDatos);
                agregarDato(table, proveedor, Element.ALIGN_LEFT,  fondoFila, fDatos);
                agregarDato(table, fecha,    Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, moneda,   Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, anticipo, Element.ALIGN_RIGHT,  fondoFila, fMonto);
                agregarDato(table, noCheque, Element.ALIGN_CENTER, fondoFila, fDatosBold);
                agregarDato(table, ccostos,  Element.ALIGN_CENTER, fondoFila, fDatos);
                agregarDato(table, partida,  Element.ALIGN_LEFT,   fondoFila, fDatos);

                totalAnticipo += montoNum;
                filaIndex++;
            }

            agregarCeldaTotal(table, "TOTAL",                     Element.ALIGN_RIGHT, 5);
            agregarCeldaTotal(table, df.format(totalAnticipo),    Element.ALIGN_RIGHT, 1);
            agregarCeldaTotal(table, "",                          Element.ALIGN_LEFT,  3);

            document.add(table);
        }

        // ── Firmas al pie ────────────────────────────────────────────────────

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

        // ── Helpers de construcción de celdas ────────────────────────────────

        /** Celda de encabezado: fondo azul, texto blanco, esquinas redondeadas. */
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

        /** Celda de datos con color de fondo alternado y esquinas suavemente redondeadas. */
        private void agregarDato(PdfPTable table, String texto, int alignment,
                                 BaseColor fondo, Font font) {
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

        /** Celda de la fila de total: fondo azul oscuro, texto blanco. */
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

        // ── Evento: celda con esquinas redondeadas ────────────────────────────

        /**
         * PdfPCellEvent que dibuja el fondo de la celda como un rectángulo
         * con esquinas redondeadas, reemplazando el fondo plano predeterminado.
         */
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
                cb.roundRectangle(
                        pos.getLeft()   + 0.5f,
                        pos.getBottom() + 0.5f,
                        pos.getWidth()  - 1f,
                        pos.getHeight() - 1f,
                        radio
                );
                cb.fill();
                cb.restoreState();
            }
        }
    }
}
