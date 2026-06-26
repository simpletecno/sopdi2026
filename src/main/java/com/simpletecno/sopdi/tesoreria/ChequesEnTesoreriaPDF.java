package com.simpletecno.sopdi.tesoreria;

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

import static com.simpletecno.sopdi.tesoreria.ChequesEnTesoreriaView.ANTIGUEDAD_PROPERTY;
import static com.simpletecno.sopdi.tesoreria.ChequesEnTesoreriaView.CHEQUE_PROPERTY;
import static com.simpletecno.sopdi.tesoreria.ChequesEnTesoreriaView.ENTIDAD_PROPERTY;
import static com.simpletecno.sopdi.tesoreria.ChequesEnTesoreriaView.FECHA_PROPERTY;
import static com.simpletecno.sopdi.tesoreria.ChequesEnTesoreriaView.MONEDA_PROPERTY;
import static com.simpletecno.sopdi.tesoreria.ChequesEnTesoreriaView.MONTO_PROPERTY;
import static com.simpletecno.sopdi.tesoreria.ChequesEnTesoreriaView.MONTOSF_PROPERTY;

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
import java.text.DecimalFormat;

/**
 * Genera el reporte PDF de Cheques en Tesorería con encabezado de empresa y el
 * detalle de los dos grids (registrados y pendientes). Sigue el patrón de
 * {@code LibroDiarioPDF} (iText 5.5.6 + {@code HeaderFooterPageEvent}).
 *
 * @author Sopdi
 */
public class ChequesEnTesoreriaPDF extends Window {

    String fileName;

    String idEmpresa;
    String empresaNombre;
    String empresaNit;
    String fechaReporte;
    IndexedContainer registradosContainer;
    IndexedContainer pendientesContainer;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    public ChequesEnTesoreriaPDF(
            String idEmpresa,
            String empresaNombre,
            String empresaNit,
            String fechaReporte,
            IndexedContainer registradosContainer,
            IndexedContainer pendientesContainer
    ) {
        this.idEmpresa = idEmpresa;
        this.empresaNombre = empresaNombre;
        this.empresaNit = empresaNit;
        this.fechaReporte = fechaReporte;
        this.registradosContainer = registradosContainer;
        this.pendientesContainer = pendientesContainer;

        try {
            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("1100");
            setHeight("700");
            center();

            fileName = "ChequesEnTesoreria_"
                    + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "")
                    + "_" + new Utileria().getHora() + ".pdf";

            StreamResource pdfResource = new StreamResource(new Pdf(fileName), fileName);
            pdfResource.setMIMEType("application/pdf");

            e.setSource(pdfResource);
            setContent(e);

            File file = new File(fileName);
            file.delete();

        } catch (Exception allEx) {
            Notification.show("Error al generar el reporte de Cheques en Tesorería.", Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }
    }

    /**
     * Crea el PDF con iText. Implementa StreamSource (método getStream).
     */
    public class Pdf implements StreamResource.StreamSource, Serializable {

        private final String RECEIPTFILE
                = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/pdfreceipts/";

        private final Font smallBold10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        private final Font smallBold12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        private final Font small10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
        private final Font small10Red = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL, BaseColor.RED);
        private final Font fuenteAzul12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);

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
                event.setFirstLineReportTitle("CHEQUES EN TESORERIA");
                event.setCompanyName(empresaNombre);
                event.setCompanyTaxid(empresaNit);
                event.setSecondLineReportTitle("Reporte al " + fechaReporte);
                event.setPrintBy(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserName());
                event.setPrintTime(Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()));
                event.setFolioInicial(0);

                writer.setPageEvent(event);

                document.open();
                document2.open();

                addMetaData(document);
                addContent(document);
                document.close();

                addMetaData(document2);
                addContent(document2);
                document2.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(os.toByteArray());
        }

        private void addMetaData(Document document) {
            document.addTitle("Cheques en Tesoreria " + empresaNombre);
            document.addSubject("Cheques en Tesoreria");
            document.addKeywords("Java, PDF, iText");
            document.addAuthor("www.sopdi.com");
            document.addCreator("www.sopdi.com");
        }

        private void addContent(Document document) throws DocumentException {

            Paragraph preface = new Paragraph();
            addEmptyLine(preface, 1);
            document.add(preface);

            // Detalle 1: cheques ya registrados en tesorería.
            agregarSeccion(document, "CHEQUES REGISTRADOS EN TESORERIA", registradosContainer);

            preface = new Paragraph();
            addEmptyLine(preface, 1);
            document.add(preface);

            // Detalle 2: cheques pendientes de registrar.
            agregarSeccion(document, "CHEQUES PENDIENTES DE REGISTRAR (NO COBRADOS)", pendientesContainer);

            preface = new Paragraph();
            addEmptyLine(preface, 1);
            document.add(preface);

            LineSeparator linea = new LineSeparator();
            document.add(linea);
        }

        /**
         * Renderiza un subtítulo y la tabla con el detalle de un grid.
         */
        private void agregarSeccion(Document document, String titulo, IndexedContainer container)
                throws DocumentException {

            Paragraph subtitulo = new Paragraph(titulo, fuenteAzul12);
            subtitulo.setAlignment(Element.ALIGN_LEFT);
            document.add(subtitulo);

            Paragraph espacio = new Paragraph();
            addEmptyLine(espacio, 1);
            document.add(espacio);

            PdfPTable reportTable = new PdfPTable(6);
            float[] columnWidths = {1.2f, 3.0f, 1.2f, 1.5f, 1.0f, 1.3f};
            reportTable.setWidths(columnWidths);
            reportTable.setSplitRows(false);
            reportTable.setHeaderRows(1);
            reportTable.setWidthPercentage(95);

            // Encabezado de columnas.
            agregarCeldaEncabezado(reportTable, "No. CHEQUE", Element.ALIGN_LEFT);
            agregarCeldaEncabezado(reportTable, "ENTIDAD (PROVEEDOR/CLIENTE)", Element.ALIGN_LEFT);
            agregarCeldaEncabezado(reportTable, "FECHA", Element.ALIGN_LEFT);
            agregarCeldaEncabezado(reportTable, "ANTIGUEDAD", Element.ALIGN_LEFT);
            agregarCeldaEncabezado(reportTable, "MONEDA", Element.ALIGN_LEFT);
            agregarCeldaEncabezado(reportTable, "MONTO", Element.ALIGN_RIGHT);

            double total = 0.00;
            try {
                for (Object itemId : container.getItemIds()) {
                    boolean vencido = esVencido(String.valueOf(container.getContainerProperty(itemId, FECHA_PROPERTY).getValue()));
                    Font fuente = vencido ? small10Red : small10;

                    agregarCeldaDato(reportTable, String.valueOf(container.getContainerProperty(itemId, CHEQUE_PROPERTY).getValue()), Element.ALIGN_LEFT, fuente);
                    agregarCeldaDato(reportTable, String.valueOf(container.getContainerProperty(itemId, ENTIDAD_PROPERTY).getValue()), Element.ALIGN_LEFT, fuente);
                    agregarCeldaDato(reportTable, String.valueOf(container.getContainerProperty(itemId, FECHA_PROPERTY).getValue()), Element.ALIGN_LEFT, fuente);
                    agregarCeldaDato(reportTable, String.valueOf(container.getContainerProperty(itemId, ANTIGUEDAD_PROPERTY).getValue()), Element.ALIGN_LEFT, fuente);
                    agregarCeldaDato(reportTable, String.valueOf(container.getContainerProperty(itemId, MONEDA_PROPERTY).getValue()), Element.ALIGN_LEFT, fuente);
                    agregarCeldaDato(reportTable, String.valueOf(container.getContainerProperty(itemId, MONTO_PROPERTY).getValue()), Element.ALIGN_RIGHT, fuente);

                    try {
                        total += Double.parseDouble(String.valueOf(container.getContainerProperty(itemId, MONTOSF_PROPERTY).getValue()));
                    } catch (NumberFormatException ignored) {
                    }
                }

                // Fila de totales.
                PdfPCell tituloTotal = new PdfPCell(new Paragraph("TOTAL (" + container.size() + " CHEQUES)", smallBold10));
                tituloTotal.setColspan(5);
                tituloTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tituloTotal.setBorderWidth(0);
                tituloTotal.setBorderWidthTop(1);
                reportTable.addCell(tituloTotal);

                PdfPCell celdaTotal = new PdfPCell(new Paragraph(numberFormat.format(total), smallBold10));
                celdaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaTotal.setBorderWidth(0);
                celdaTotal.setBorderWidthTop(1);
                reportTable.addCell(celdaTotal);

                document.add(reportTable);
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

        private void agregarCeldaEncabezado(PdfPTable table, String texto, int alineacion) {
            PdfPCell c1 = new PdfPCell(new Paragraph(texto, smallBold10));
            c1.setHorizontalAlignment(alineacion);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(1);
            table.addCell(c1);
        }

        private void agregarCeldaDato(PdfPTable table, String texto, int alineacion, Font fuente) {
            PdfPCell c1 = new PdfPCell(new Paragraph(texto, fuente));
            c1.setHorizontalAlignment(alineacion);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            c1.setBorderWidthBottom(0);
            table.addCell(c1);
        }

        /**
         * Determina si una fecha (dd/MM/yyyy) tiene 1 o más años de antigüedad.
         */
        private boolean esVencido(String fechaDDMMYYYY) {
            try {
                String[] partes = fechaDDMMYYYY.split("/");
                java.util.Calendar desde = java.util.Calendar.getInstance();
                desde.set(Integer.parseInt(partes[2]), Integer.parseInt(partes[1]) - 1, Integer.parseInt(partes[0]));
                java.util.Calendar limite = java.util.Calendar.getInstance();
                limite.add(java.util.Calendar.YEAR, -1);
                return !desde.after(limite);
            } catch (Exception ex) {
                return false;
            }
        }

        private void addEmptyLine(Paragraph paragraph, int number) {
            for (int i = 0; i < number; i++) {
                paragraph.add(new Paragraph(" "));
            }
        }
    }
}
