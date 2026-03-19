/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class HeaderFooterPageEventOrdenCompra extends PdfPageEventHelper {

    private final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private final Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
    private final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD);
    private final Font smallBold14 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
    private final Font smallBold12 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private final Font smallBold10 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private final Font fuenteMagenta36 = new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.BOLD, BaseColor.MAGENTA);
    private final Font fuenteNegra12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);
    private final Font fuenteVerde12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.GREEN);
    private final Font fuenteAzul25 = new Font(Font.FontFamily.TIMES_ROMAN, 25, Font.BOLD, BaseColor.BLUE);
    private final Font fuenteRoja12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
    private final Font fuenteAzul16 = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, BaseColor.BLUE);
    private final Font fuenteAzul12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
    private final Font fuenteAzul10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.BLUE);
    private final Font small10 = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private final Font small12 = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private final Font small10Red = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL, BaseColor.RED);

    private String LOGOCASTANOS = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath() + "/WEB-INF/logos/logocastanos.jpeg";
    private String LOGONISA = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath() + "/WEB-INF/logos/logonisa.jpg";
    private String LOGONACSA = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath() + "/WEB-INF/logos/logonacsa.jpg";
    private String LOGOSIENA = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath() + "/WEB-INF/logos/logosiena.png";
    private String LOGOFUNDAMEX = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath() + "/WEB-INF/logos/logofundamex.jpeg";

    private String companyName = "";
    private String companyTaxid = "";
    private String firstLineReportTitle = "";
    private String secondLineReportTitle = "";
    private String thirdLineReportTitle = "";
    private String leftFooterText = "";
    private String printTime = "";
    private String printBy = "";
    private String creadoUsuario = "";
    private String responsable = "";
    private String direccionEntrega = "";
    private String referenciaEntrega = "";
    private String contactoEnObra = "";
    private String cotizacionReferencia = "";
    private String razon = "";

    private String idOrdenCompra = "";
    private String NOC = "";
    private double anticipo = 0.00;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords;

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        try {
            mainUI = UI.getCurrent();

            //PRIMERA TABLA
            PdfPTable firstTable = new PdfPTable(3);

            firstTable.setSplitRows(false);
            firstTable.setHeaderRows(0);
            firstTable.setWidthPercentage(95);

            float[] columnWidths = {1.5f, 3.0f, 1.5f};

            firstTable.setWidths(columnWidths);
//LINE 1
            PdfPCell logoCel;
            Image image1;
            switch(((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyId()) {
                case "210":
                    image1 = Image.getInstance (LOGOCASTANOS);
                    break;
                case "220":
                    image1 = Image.getInstance (LOGONACSA);
                    break;
                case "230":
                    image1 = Image.getInstance (LOGONISA);
                    break;
                case "110": //FUNDAMEX
                case "120": //FUNGUAMEX
                    image1 = Image.getInstance (LOGOFUNDAMEX);
                    break;
                default:
                    image1 = Image.getInstance (LOGOSIENA);
                    break;
            }
            image1.setAlignment(Image.ALIGN_LEFT);
            image1.scaleAbsolute(30f, 30f);
            image1.setSpacingAfter(0f);

            logoCel = new PdfPCell();
            logoCel.addElement(image1);
            logoCel.setBorder(0);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            table.addCell(logoCel);

            PdfPCell c1;

            c1 = new PdfPCell(new Paragraph(companyName, smallBold12));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);

            table.addCell(c1);

            firstTable.addCell(table);

            c1 = new PdfPCell(new Paragraph(firstLineReportTitle, smallBold12));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_CENTER);
            c1.setBorderWidth(0);
            firstTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("Fecha : " + printTime, smallBold12));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            firstTable.addCell(c1);
//LINE 2
            c1 = new PdfPCell(new Paragraph(companyTaxid, small10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            firstTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph(secondLineReportTitle, smallBold12));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_CENTER);
            c1.setBorderWidth(0);
            firstTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("Creado por : " + creadoUsuario, smallBold12));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setVerticalAlignment(Element.ALIGN_RIGHT);
            c1.setBorderWidth(0);
            firstTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph(((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen(), small10));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            firstTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph(thirdLineReportTitle, smallBold12));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_CENTER);
            c1.setBorderWidth(0);
            firstTable.addCell(c1);

            c1 = new PdfPCell(new Paragraph("", smallBold12));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorderWidth(0);
            firstTable.addCell(c1);

            document.add(firstTable);

            Paragraph preface2 = new Paragraph("");
            addEmptyLine(preface2, 1);
            document.add(preface2);

            PdfPTable reportTable = new PdfPTable(2);
            PdfPCell c2 = null;

            float[] columnWidths2 = {2.5f, 2.5f};

            reportTable.setWidths(columnWidths2);
            reportTable.setSplitRows(false);
            reportTable.setHeaderRows(1);
            reportTable.setWidthPercentage(90);

            Paragraph preface;

            String queryString = " SELECT *, proveedor.Nombre, proveedor.NIT, proveedor.Email";
            queryString += " FROM orden_compra";
            queryString += " INNER JOIN proveedor";
            queryString += " ON orden_compra.IdProveedor = proveedor.IDProveedor";
            queryString += " WHERE orden_compra.Id = " + idOrdenCompra;

            try {

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {

                    setAnticipo(rsRecords.getDouble("Anticipo"));
                    setNOC(rsRecords.getString("NOC"));

                    c2 = new PdfPCell(new Paragraph( "ORDEN DE COMPRA : " + NOC, smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(1);
                    //c2.setBorderWidthRight(1);
                    //c2.setBorderWidthLeft(1);
                    //c2.setBorderWidthTop(1);
                    reportTable.addCell(c2);

                    c2 = new PdfPCell(new Paragraph( "RAZON : " + getRazon(), smallBold12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    c2.setBorderWidthBottom(1);
                    //c2.setBorderWidthRight(1);
                    //c2.setBorderWidthLeft(1);
                    //c2.setBorderWidthTop(1);
                    reportTable.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable.addCell(c2);

//                    c2 = new PdfPCell(new Paragraph("Anticipo : " + numberFormat.format(anticipo), small12));
//                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
//                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
//                    c2.setBorderWidth(0);
                    //c2.setBorderWidthBottom(1);
                    //c2.setBorderWidthRight(1);
                    //c2.setBorderWidthLeft(1);
                    //c2.setBorderWidthTop(1);
                    reportTable.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable.addCell(c2);

                    c2 = new PdfPCell(new Paragraph(" ", small12));
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2.setVerticalAlignment(Element.ALIGN_LEFT);
                    c2.setBorderWidth(0);
                    reportTable.addCell(c2);

                }
            } catch (Exception e) {
                System.out.println("Error al intentar buscar orden de compra " + e);
                e.printStackTrace();
            }

            preface = new Paragraph();
            addEmptyLine(preface, 1);

            document.add(reportTable);

        } catch (Exception ex) {
            Logger.getLogger(HeaderFooterPageEventOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error " + ex);
            ex.printStackTrace();
        }
    }

    private void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    public void onEndPage(PdfWriter writer, Document document) {
        Utileria utilData = new Utileria(1);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(leftFooterText, smallBold10), 20, 20, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Página : " + document.getPageNumber(), smallBold10), 550, 20, 0);
    }

    /**
     * @param leftFooterText the leftFootherText to set
     */
    public void setLeftFooterText(String leftFooterText) {
        this.leftFooterText = leftFooterText;
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * @return the companyTaxid
     */
    public String getCompanyTaxid() {
        return companyTaxid;
    }

    /**
     * @param companyTaxid the companyTaxid to set
     */
    public void setCompanyTaxid(String companyTaxid) {
        this.companyTaxid = companyTaxid;
    }

    /**
     * @return the firstLineReportTitle
     */
    public String getFirstLineReportTitle() {
        return firstLineReportTitle;
    }

    /**
     * @param firstLineReportTitle the firstLineReportTitle to set
     */
    public void setFirstLineReportTitle(String firstLineReportTitle) {
        this.firstLineReportTitle = firstLineReportTitle;
    }

    /**
     * @return the secondLineReportTitle
     */
    public String getSecondLineReportTitle() {
        return secondLineReportTitle;
    }

    /**
     * @param secondLineReportTitle the secondLineReportTitle to set
     */
    public void setSecondLineReportTitle(String secondLineReportTitle) {
        this.secondLineReportTitle = secondLineReportTitle;
    }

    /**
     * @return the thirdLineReportTitle
     */
    public String getThirdLineReportTitle() {
        return thirdLineReportTitle;
    }

    /**
     * @param thirdLineReportTitle the thirdLineReportTitle to set
     */
    public void setThirdLineReportTitle(String thirdLineReportTitle) {
        this.thirdLineReportTitle = thirdLineReportTitle;
    }

    /**
     * @return the leftFooterText
     */
    public String getLeftFooterText() {
        return leftFooterText;
    }

    /**
     * @return the printBy
     */
    public String getPrintBy() {
        return printBy;
    }

    /**
     * @param printBy the printBy to set
     */
    public void setPrintBy(String printBy) {
        this.printBy = printBy;
    }

    /**
     * @return the printTime
     */
    public String getPrintTime() {
        return printTime;
    }

    /**
     * @param printTime the printTime to set
     */
    public void setPrintTime(String printTime) {
        this.printTime = printTime;
    }

    /**
     * @return the folioInicial
     */
    public String getCreadoUsuario() {
        return creadoUsuario;
    }

    public void setCreadoUsuario(String creadoUsuario) {
        this.creadoUsuario = creadoUsuario;
    }

    public String getNOC() {
        return NOC;
    }

    public void setNOC(String NOC) {
        this.NOC = NOC;
    }

    public String getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(String idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public double getAnticipo() {
        return anticipo;
    }

    public void setAnticipo(double anticipo) {
        this.anticipo = anticipo;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getReferenciaEntrega() {
        return referenciaEntrega;
    }

    public void setReferenciaEntrega(String referenciaEntrega) {
        this.referenciaEntrega = referenciaEntrega;
    }

    public String getContactoEnObra() {
        return contactoEnObra;
    }

    public void setContactoEnObra(String contactoEnObra) {
        this.contactoEnObra = contactoEnObra;
    }

    public String getCotizacionReferencia() {
        return cotizacionReferencia;
    }

    public void setCotizacionReferencia(String cotizacionReferencia) {
        this.cotizacionReferencia = cotizacionReferencia;
    }

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }
}
