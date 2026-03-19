/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import com.simpletecno.sopdi.utilerias.ConvertirNumerosALetras;
import com.simpletecno.sopdi.configuracion.EnvironmentVars;
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
import java.math.BigDecimal;
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
public class PagoChequesPDF extends Window {

    String fileName;
    String queryString;

    Statement stQuery;
    ResultSet rsRecords;

    UI mainUI;
    String idEmpresa;
    String empresaNombre;
    String fecha;
    String nuevaFecha;
    String monedaDocumento;
    String tipoDocumento;
    double totalDebe = 0.00;
    double totalHaber = 0.00;
    double montoTotal = 0.00;

    String codigoPartida;

    String liquidacion, proveedorNombre, cheque, razon, montoCheque;

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");

    public PagoChequesPDF(
            String idEmpresa,
            String empresaNombre,
            String codigoPartida,
            String liquidacion,
            String proveedorNombre,
            String cheque, 
            String razon,
            String montoCheque
    ) {

        this.idEmpresa = idEmpresa;
        this.empresaNombre = empresaNombre;
        this.codigoPartida = codigoPartida;
        this.proveedorNombre = proveedorNombre;
        this.cheque = cheque;
        this.razon = razon;
        this.montoCheque = montoCheque;

        try {
            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("1100");
            setHeight("700");
            center();
            
            System.out.println("valor de cheque " + cheque +  " cheque " + this.cheque);

            fileName = "CH_" + cheque + "_" + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_" + codigoPartida + ".pdf";

            StreamResource pdfResource;
            pdfResource = new StreamResource(new Pdf(fileName), fileName+System.currentTimeMillis());
            pdfResource.setMIMEType("application/pdf");

            e.setSource(pdfResource);
            setContent(e);
            // despues de llamar Pdf()  fileName y tiene la ruta completa del archivo
            File file = new File(fileName);
            System.out.println("file name "  + fileName);
            file.delete();

        } catch (Exception allEx) {
            Notification.show("Error al generar el balance de saldos.", Type.ERROR_MESSAGE);
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
        private final Font small8 = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
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
        private final Font small12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
        private final Font small10Red = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL, BaseColor.RED);

        private FileOutputStream fost;
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        private PdfWriter writer;
        
        public Pdf(String fileName) {

            try {
                EnvironmentVars enviromentsVars = new EnvironmentVars();

                String filePath = enviromentsVars.getDtePath();
                System.out.println("get pache " + enviromentsVars.getDtePath());
                        
                fileName = filePath + fileName;

                Document document = new Document(PageSize.A4, 0, 0, 0, 0);
                Document document2 = new Document(PageSize.A4, 0, 0, 0, 0);
                fost = new FileOutputStream(fileName);
                
                                System.out.println("ltm asgnad " + enviromentsVars.getDtePath());


//                document.setMargins(15, 15, 15, 15);
//                document2.setMargins(15, 15, 15, 15);
                document.setMargins(20, 20, 1, 20);
                document2.setMargins(20, 20, 1, 20);

                PdfWriter.getInstance(document, fost);

                writer = PdfWriter.getInstance(document2, os);

                document.open();
                document2.open();

                addMetaData(document);
                //addTitlePage(document);
                addContent(document);
                addFooterPage(document);
                addDocaDetail(document);
                document.close();

                addMetaData(document2);
                //addTitlePage(document2);
                addContent(document2);
                addFooterPage(document2);
                addDocaDetail(document2);
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
            document.addTitle(tipoDocumento + " " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
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

            PdfPTable titleTable = new PdfPTable(2);

            titleTable.setSplitRows(false);
            titleTable.setHeaderRows(0);
            titleTable.setWidthPercentage(100);

            float[] columnWidhts0 = {50.0f, 50.0f};

            titleTable.setWidths(columnWidhts0);
            
            PdfPCell c0;

            c0 = new PdfPCell(new Paragraph(empresaNombre, small12));
            c0.setHorizontalAlignment(Element.ALIGN_LEFT);
            c0.setVerticalAlignment(Element.ALIGN_LEFT);
            c0.setBorderWidth(0);
            titleTable.addCell(c0);

            PdfPCell c3;
            PdfPTable secondTable = new PdfPTable(4);

            queryString = " SELECT contabilidad_partida.NombreProveedor,contabilidad_partida.NombreProveedor, ";
            queryString += " contabilidad_partida.DebeQuetzales,contabilidad_partida.HaberQuetzales,contabilidad_partida.Fecha, ";
            queryString += " contabilidad_nomenclatura_empresa.NoCuenta,contabilidad_nomenclatura_empresa.N5,contabilidad_partida.NumeroDocumento,";
            queryString += " contabilidad_nomenclatura_empresa.IdNomenclatura,contabilidad_partida.IdNomenclatura,contabilidad_partida.MonedaDocumento, ";
            queryString += " contabilidad_partida.TipoDocumento, contabilidad_partida.Debe, contabilidad_partida.Haber";
            queryString += " FROM contabilidad_partida";
            queryString += " INNER JOIN contabilidad_nomenclatura_empresa ON contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
            queryString += " WHERE contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
            queryString += " AND contabilidad_partida.TipoDocumento In ('CHEQUE', 'TRANSFERENCIA', 'NOTA DE DEBITO')";
//            queryString += " And (contabilidad_partida.Debe > 0 OR contabilidad_partida.Haber > 0)";
            queryString += " AND contabilidad_partida.NumeroDocumento = '" + cheque + "'";
            queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + idEmpresa;

            System.out.println("query busqueda pdf" + queryString);

            int contador = 1;
            totalDebe = 0.00;
            totalHaber = 0.00;

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                //  encontrado
                if (rsRecords.next()) {
                    tipoDocumento = rsRecords.getString("TIpoDocumento");

                    if(tipoDocumento.endsWith("CHEQUE")){
                        c0 = new PdfPCell(new Paragraph("Cheque No. " + cheque, small12));
                    }
                    else{
                        c0 = new PdfPCell(new Paragraph("TRANS. No. " + cheque, small12));
                    }

                    fecha = Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha"));
                    monedaDocumento = rsRecords.getString("MonedaDocumento");

                    c0.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c0.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c0.setBorderWidth(0);
                    titleTable.addCell(c0);

                    document.add(titleTable);

                    PdfPTable firstTable1 = new PdfPTable(3);

                    firstTable1.setSplitRows(false);
                    firstTable1.setHeaderRows(0);
                    firstTable1.setWidthPercentage(100);

                    float[] columnWidhts1 = {1.0f, 4.0f, 1.5f};

                    firstTable1.setWidths(columnWidhts1);

                    PdfPCell c1;

                    firstTable1.setWidths(columnWidhts1);
                    firstTable1.setSplitRows(false);
                    firstTable1.setHeaderRows(1);
                    firstTable1.setWidthPercentage(95);

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

        //            System.out.println("fecha" + fecha);
        //            System.out.println("fecha " + fecha.substring(3, 5));
        //            System.out.println("fecha " + fecha.substring(0, 2));

                    switch (fecha.substring(3, 5)) {
                        case "01":
                            nuevaFecha = fecha.substring(0, 2) + " ENERO ";
                            break;
                        case "02":
                            nuevaFecha = fecha.substring(0, 2) + " FEBRERO ";
                            break;
                        case "03":
                            nuevaFecha = fecha.substring(0, 2) + " MARZO ";
                            break;
                        case "04":
                            nuevaFecha = fecha.substring(0, 2) + " ABRIL ";
                            break;
                        case "05":
                            nuevaFecha = fecha.substring(0, 2) + " MAYO ";
                            break;
                        case "06":
                            nuevaFecha = fecha.substring(0, 2) + " JUNIO ";
                            break;
                        case "07":
                            nuevaFecha = fecha.substring(0, 2) + " JULIO ";
                            break;
                        case "08":
                            nuevaFecha = fecha.substring(0, 2) + " AGOSTO ";
                            break;
                        case "09":
                            nuevaFecha = fecha.substring(0, 2) + " SEPTIEMBRE ";
                            break;
                        case "10":
                            nuevaFecha = fecha.substring(0, 2) + " OCTUBRE ";
                            break;
                        case "11":
                            nuevaFecha = fecha.substring(0, 2) + " NOVIEMBRE ";
                            break;
                        case "12":
                            nuevaFecha = fecha.substring(0, 2) + " DICIEMBRE ";
                            break;
                        default:
                            nuevaFecha = "NO APLICA";
                            break;
                    }

                    nuevaFecha+= " " + fecha.substring(6, 10);

                    c1 = new PdfPCell(new Paragraph("GUATEMALA. " + nuevaFecha, small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    firstTable1.addCell(c1);

                    if (monedaDocumento.equals("QUETZALES")) {
                        c1 = new PdfPCell(new Paragraph("** Q." + String.valueOf(numberFormat.format(Double.valueOf(montoCheque))) + "**            ", small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT);
                        c1.setBorderWidth(0);
                        firstTable1.addCell(c1);
                    } else {
                        c1 = new PdfPCell(new Paragraph("** $." + String.valueOf(numberFormat.format(Double.valueOf(montoCheque))) + "**            ", small10));
                        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c1.setVerticalAlignment(Element.ALIGN_LEFT  );
                        c1.setBorderWidth(0);
                        firstTable1.addCell(c1);
                    }

                    c1 = new PdfPCell(new Paragraph("", small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph(proveedorNombre.toUpperCase(), small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setBorderWidthBottom(0);
                    c1.setPaddingTop(5);
                    firstTable1.addCell(c1);

                    c1 = new PdfPCell(new Paragraph("", small10));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    BigDecimal montoBigDecimal =  new BigDecimal(Double.valueOf(montoCheque));
                    montoBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);

                    c1 = new PdfPCell(new Paragraph("", small10));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    firstTable1.addCell(c1);

                    ConvertirNumerosALetras convertir = new ConvertirNumerosALetras();
                    convertir.setNombreMoneda(monedaDocumento);
                    String cantidadesLetras = convertir.getStringOfNumber(montoBigDecimal.floatValue(), montoCheque);

                    c1 = new PdfPCell(new Paragraph("**"+cantidadesLetras+"**", small8));
                    c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c1.setVerticalAlignment(Element.ALIGN_LEFT);
                    c1.setBorderWidth(0);
                    c1.setPaddingTop(5);
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

                    PdfPTable firstTable2 = new PdfPTable(3);

                    firstTable2.setSplitRows(false);
                    firstTable2.setHeaderRows(0);
                    firstTable2.setWidthPercentage(95);

                    float[] columnWidths2 = {1.0f, 4.0f, 1.5f};

                    firstTable2.setWidths(columnWidths2);

                    PdfPCell c2;

                    firstTable2.setWidths(columnWidths2);
                    firstTable2.setSplitRows(false);
                    firstTable2.setHeaderRows(0);
                    firstTable2.setWidthPercentage(95);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthTop(1);
                    c2.setBorderWidthLeft(1);
                    c2.setBorderWidthBottom(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthTop(1);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthTop(1);
                    c2.setBorderWidthRight(1);
                    firstTable2.addCell(c2);


                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthTop(0);
                    c2.setBorderWidthLeft(1);
                    c2.setBorderWidthBottom(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthTop(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthTop(0);
                    c2.setBorderWidthRight(1);
                    firstTable2.addCell(c2);


                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthLeft(1);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("GUATEMALA " + nuevaFecha + Utileria.getFechaYYYYMMDD_1(new java.util.Date()).substring(0,4), small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    firstTable2.addCell(c2);
        //System.out.println("PagoChequesPDF->montoCheque = " + montoCheque);
                    if (monedaDocumento.equals("QUETZALES")) {
                        c2 = new PdfPCell(new Paragraph("** Q." + String.valueOf(numberFormat.format(Double.valueOf(montoCheque))) + "**", small10));
                        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c2.setVerticalAlignment(Element.ALIGN_LEFT);
                        c2.setBorderWidth(0);
                        c2.setBorderWidthRight(1);
                        firstTable2.addCell(c2);
                    } else {
                        c2 = new PdfPCell(new Paragraph("** $." + String.valueOf(numberFormat.format(Double.valueOf(montoCheque))) + "**", small10));
                        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c2.setVerticalAlignment(Element.ALIGN_LEFT);
                        c2.setBorderWidth(0);
                        c2.setBorderWidthRight(1);
                        firstTable2.addCell(c2);
                    }


                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthLeft(1);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(proveedorNombre.toUpperCase(), small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthRight(1);
                    firstTable2.addCell(c2);


                    c2 = new PdfPCell(new Paragraph("", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    c2.setBorderWidthLeft(1);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph("**" + cantidadesLetras + "**", small8));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthRight(1);
                    firstTable2.addCell(c2);


                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    c2.setBorderWidthLeft(1);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthRight(1);
                    firstTable2.addCell(c2);


                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    c2.setBorderWidthLeft(1);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(razon, small10));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_RIGHT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(0);
                    c2.setBorderWidthRight(1);
                    firstTable2.addCell(c2);


                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthLeft(1);
                    c2.setBorderWidthBottom(1);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(1);
                    firstTable2.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(1);
                    c2.setBorderWidthRight(1);
                    firstTable2.addCell(c2);


                    Paragraph preface1 = new Paragraph("");
                    addEmptyLine(preface1, 3);
                    document.add(preface1);

                    document.add(firstTable1);

                    Paragraph preface2 = new Paragraph("");
                    addEmptyLine(preface2, 2);
                    document.add(preface2);

                    document.add(firstTable2);

                    Paragraph preface3 = new Paragraph("");
                    addEmptyLine(preface3, 2);
                    document.add(preface3);


                    secondTable.setSplitRows(false);
                    secondTable.setHeaderRows(1);
                    secondTable.setWidthPercentage(95);

                    float[] columnWidths3 = {1.5f, 3.5f, 1.5f, 1.5f};

                    secondTable.setWidths(columnWidths3);

                    //"PARTIDA No. " + codigoPartida

                    PdfPCell c30;
                    c30 = new PdfPCell(new Paragraph("", small12));
                    c30.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c30.setVerticalAlignment(Element.ALIGN_LEFT);
                    c30.setBorderWidth(0);
                    secondTable.addCell(c30);

                    c30 = new PdfPCell(new Paragraph("PARTIDA No. " + codigoPartida, small10));
                    c30.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c30.setVerticalAlignment(Element.ALIGN_CENTER);
                    c30.setBorderWidth(0);
                    secondTable.addCell(c30);

                    c30 = new PdfPCell(new Paragraph("", small12));
                    c30.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c30.setVerticalAlignment(Element.ALIGN_LEFT);
                    c30.setBorderWidth(0);
                    secondTable.addCell(c30);

                    c30 = new PdfPCell(new Paragraph("", small12));
                    c30.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c30.setVerticalAlignment(Element.ALIGN_LEFT);
                    c30.setBorderWidth(0);
                    secondTable.addCell(c30);

                    do {

                        c3 = new PdfPCell(new Paragraph(rsRecords.getString("contabilidad_nomenclatura.NoCuenta"), small12));
                        c3.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c3.setVerticalAlignment(Element.ALIGN_LEFT);
                        c3.setBorderWidth(0);
                        secondTable.addCell(c3);

                        c3 = new PdfPCell(new Paragraph(rsRecords.getString("contabilidad_nomenclatura.N5"), small12));
                        c3.setHorizontalAlignment(Element.ALIGN_LEFT);
                        c3.setVerticalAlignment(Element.ALIGN_LEFT);
                        c3.setBorderWidth(0);
                        secondTable.addCell(c3);

                        c3 = new PdfPCell(new Paragraph("Q." + numberFormat.format(rsRecords.getDouble("DebeQuetzales")), small12));
                        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c3.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c3.setBorderWidth(0);
                        secondTable.addCell(c3);

                        c3 = new PdfPCell(new Paragraph("Q." + numberFormat.format(rsRecords.getDouble("HaberQuetzales")), small12));
                        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c3.setVerticalAlignment(Element.ALIGN_RIGHT);
                        c3.setBorderWidth(0);
                        secondTable.addCell(c3);

                        totalDebe = totalDebe + rsRecords.getDouble("DebeQuetzales");
                        totalHaber = totalHaber + rsRecords.getDouble("HaberQuetzales");

                        contador += 1;

                    } while (rsRecords.next());
                }

            } catch (SQLException ex) {
                Logger.getLogger(PagoChequesPDF.class.getName()).log(Level.SEVERE, null, ex);
            }

            c3 = new PdfPCell(new Paragraph(" ", small12));
            c3.setHorizontalAlignment(Element.ALIGN_LEFT);
            c3.setVerticalAlignment(Element.ALIGN_LEFT);
            c3.setBorderWidth(0);
            secondTable.addCell(c3);

            c3 = new PdfPCell(new Paragraph("Totales ", small12));
            c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c3.setVerticalAlignment(Element.ALIGN_RIGHT);
            c3.setBorderWidth(0);
            secondTable.addCell(c3);

            c3 = new PdfPCell(new Paragraph("Q." + String.valueOf(numberFormat.format(totalDebe)), small12));
            c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c3.setVerticalAlignment(Element.ALIGN_RIGHT);
            c3.setBorderWidth(0);
            c3.setBorderWidthBottom(2);
            secondTable.addCell(c3);

            c3 = new PdfPCell(new Paragraph("Q." + String.valueOf(numberFormat.format(totalHaber)), small12));
            c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c3.setVerticalAlignment(Element.ALIGN_RIGHT);
            c3.setBorderWidth(0);
            c3.setBorderWidthBottom(2);
            secondTable.addCell(c3);

            document.add(secondTable);

        }

        private void addDocaDetail(Document document) {
            
            if(razon.contains("FACTURA") || razon.contains("LIQUIDACION")) {

                try {
                
                    document.newPage();

                    PdfPTable docaTable = new PdfPTable(5);
                    docaTable.setSplitRows(false);
                    docaTable.setHeaderRows(0);
                    docaTable.setWidthPercentage(95);

                    float[] docaCW = {1.5f, 1.5f, 3.5f, 2f, 1.5f};

                    docaTable.setWidths(docaCW);

                    queryString = "SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.Fecha, ";
                    queryString += " contabilidad_partida.IdProveedor, contabilidad_partida.NombreProveedor ,";
                    queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
                    queryString += " contabilidad_partida.MonedaDocumento, contabilidad_partida.TipoCambio,";
                    queryString += " contabilidad_partida.Haber, contabilidad_partida.HaberQuetzales,";
                    queryString += " contabilidad_partida.TipoDocumento ";
                    queryString += " FROM contabilidad_partida ";
                    queryString += " WHERE contabilidad_partida.IdEmpresa =" + idEmpresa;
                    queryString += " AND UPPER(contabilidad_partida.TipoDocumento) IN ('FACTURA', 'RECIBO', 'RECIBO CORRIENTE', 'RECIBO CONTABLE', 'FORMULARIO')";
                    queryString += " AND contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
                    queryString += " GROUP BY CodigoPartida";

System.out.println("query busqueda documentos afectados : " + queryString);

                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords = stQuery.executeQuery(queryString);

                    montoTotal = 0.00;
                    
                    //  encontrado  
                    if (rsRecords.next()) {

                        PdfPCell celda;

                        celda = new PdfPCell(new Paragraph("Fecha", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("Documento", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("Proveedor", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("CodigoPartida", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("Monto Q.", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        celda.setVerticalAlignment(Element.ALIGN_RIGHT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);
                        
                        celda = new PdfPCell(new Paragraph("----------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("----------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("-----------------------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("----------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("--------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        celda.setVerticalAlignment(Element.ALIGN_RIGHT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);
                        
                        do {
                                                        
                            celda = new PdfPCell(new Paragraph(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")), small12));
                            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                            celda.setVerticalAlignment(Element.ALIGN_LEFT);
                            celda.setBorderWidth(0);
                            docaTable.addCell(celda);
                            
                            celda = new PdfPCell(new Paragraph(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"), small12));
                            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                            celda.setVerticalAlignment(Element.ALIGN_LEFT);
                            celda.setBorderWidth(0);
                            docaTable.addCell(celda);

                            celda = new PdfPCell(new Paragraph(rsRecords.getString("NombreProveedor"), small12));
                            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                            celda.setVerticalAlignment(Element.ALIGN_LEFT);
                            celda.setBorderWidth(0);
                            docaTable.addCell(celda);

                            celda = new PdfPCell(new Paragraph(rsRecords.getString("CodigoPartida"), small12));
                            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                            celda.setVerticalAlignment(Element.ALIGN_LEFT);
                            celda.setBorderWidth(0);
                            docaTable.addCell(celda);

                            celda = new PdfPCell(new Paragraph(numberFormat.format(rsRecords.getDouble("HaberQuetzales")), small12));
                            celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            celda.setVerticalAlignment(Element.ALIGN_RIGHT);
                            celda.setBorderWidth(0);
                            docaTable.addCell(celda);
                            
                            montoTotal+= rsRecords.getDouble("HaberQuetzales");

                        } while (rsRecords.next());

                        celda = new PdfPCell(new Paragraph("----------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("----------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("-----------------------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("----------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("--------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        celda.setVerticalAlignment(Element.ALIGN_RIGHT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph(" ", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph(" ", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph(" ", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("TOTAL ", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph(numberFormat.format(montoTotal), small12));
                        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        celda.setVerticalAlignment(Element.ALIGN_RIGHT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("----------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("----------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("-----------------------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("----------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celda.setVerticalAlignment(Element.ALIGN_LEFT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        celda = new PdfPCell(new Paragraph("--------------", small12));
                        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        celda.setVerticalAlignment(Element.ALIGN_RIGHT);
                        celda.setBorderWidth(0);
                        docaTable.addCell(celda);

                        document.add(docaTable);
                        
                        Paragraph ultimaLinea = new Paragraph("****************************** ULTIMA LINEA ******************************", small10);
                        document.add(ultimaLinea);
                    }

                } catch (Exception ex) {
                    Logger.getLogger(PagoChequesPDF.class.getName()).log(Level.SEVERE, null, ex);
                }                                
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
