/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 *
 * @author joseaguirre
 */
public class MostrarPartidaContable extends Window {

    VerticalLayout contentLayout = new VerticalLayout();

    Grid partidaDocumentosGrid;
    public IndexedContainer documentsContainerPartida = new IndexedContainer();
    static final String TIPO_PROPERTY = "Tipo";
    static final String ID_PARTIDA_PROPERTY = "Partida";
    static final String FECHA_PARTIDA_PROPERTY = "Fecha";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PARTIDA_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String TASA_PROPERTY = "Tasa";
    static final String DEBE_Q_PROPERTY = "Debe Q.";
    static final String HABER_Q_PROPERTY = "Haber Q.";
    static final String CENTRO_COSTO_PROPERTY = "Centro Costo";
    Grid.FooterRow footer;

    Grid docaGrid;
    public IndexedContainer docaContainer = new IndexedContainer();
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String MONTO_PROPERTY = "Monto";
    static final String MONEDA_PROPERTY = "Moneda";
    Grid.FooterRow footerDoca;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    Statement stQuery;
    ResultSet rsRecords;

    String codigoPartida;
    String descripcion;
    String nombre;
    String documento;

    public MostrarPartidaContable(
            String codigoPartida,
            String descripcion,
            String nombre,
            String documento
    ) {

        this.codigoPartida = codigoPartida;
        this.descripcion = descripcion;
        this.nombre = nombre;
        this.documento = documento;

        setResponsive(true);
        setModal(true);
        setClosable(true);

        crearGridPartida();

        llenarGridPartida();

        setContent(contentLayout);

        setWidth("90%");
       //setHeight("90%");

    }

    public final void crearGridPartida() {

        contentLayout.setWidth("100%");
        contentLayout.addStyleName("rcorners3");

        HorizontalLayout camposPartidaLayout = new HorizontalLayout();
        camposPartidaLayout.setSpacing(true);

        documentsContainerPartida.addContainerProperty(TIPO_PROPERTY, String.class, "");
        documentsContainerPartida.addContainerProperty(FECHA_PARTIDA_PROPERTY, String.class, "");
        documentsContainerPartida.addContainerProperty(CUENTA_PROPERTY, String.class, "");
        documentsContainerPartida.addContainerProperty(DESCRIPCION_PARTIDA_PROPERTY, String.class, "");
        documentsContainerPartida.addContainerProperty(DEBE_PROPERTY, String.class, "");
        documentsContainerPartida.addContainerProperty(HABER_PROPERTY, String.class, "");
        documentsContainerPartida.addContainerProperty(TASA_PROPERTY, String.class, "1");
        documentsContainerPartida.addContainerProperty(DEBE_Q_PROPERTY, String.class, "");
        documentsContainerPartida.addContainerProperty(HABER_Q_PROPERTY, String.class, "");
        documentsContainerPartida.addContainerProperty(CENTRO_COSTO_PROPERTY, String.class, "");

        partidaDocumentosGrid = new Grid("Partida contable", documentsContainerPartida);
        partidaDocumentosGrid.setImmediate(true);
        partidaDocumentosGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidaDocumentosGrid.setHeightMode(HeightMode.ROW);
        partidaDocumentosGrid.setHeightByRows(5);
        partidaDocumentosGrid.setWidth("100%");
        partidaDocumentosGrid.setResponsive(true);
        partidaDocumentosGrid.setEditorBuffered(false);

        partidaDocumentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

//            System.out.println("celda=" + cellReference.getProperty());
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBE_Q_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_Q_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TASA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centertalign";
            } else {
                return null;
            }

        });

        footer = partidaDocumentosGrid.appendFooterRow();
        footer.getCell(DESCRIPCION_PARTIDA_PROPERTY).setText("SUMAS IGUALES");
        footer.getCell(DEBE_PROPERTY).setText("0.00");
        footer.getCell(HABER_PROPERTY).setText("0.00");
        footer.getCell(DEBE_Q_PROPERTY).setText("0.00");
        footer.getCell(HABER_Q_PROPERTY).setText("0.00");
        footer.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footer.getCell(HABER_PROPERTY).setStyleName("rightalign");
        footer.getCell(DEBE_Q_PROPERTY).setStyleName("rightalign");
        footer.getCell(HABER_Q_PROPERTY).setStyleName("rightalign");

        contentLayout.addComponent(partidaDocumentosGrid);
        contentLayout.setComponentAlignment(partidaDocumentosGrid, Alignment.MIDDLE_CENTER);

        docaContainer.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, null);
        docaContainer.addContainerProperty(FECHA_PARTIDA_PROPERTY, String.class, null);
        docaContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        docaContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        docaContainer.addContainerProperty(DESCRIPCION_PARTIDA_PROPERTY, String.class, null);
        docaContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        docaContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);

        docaGrid = new Grid("Documentos afectados", docaContainer);
        docaGrid.setImmediate(true);
        docaGrid.setSelectionMode(Grid.SelectionMode.NONE);
        docaGrid.setHeightMode(HeightMode.ROW);
        docaGrid.setHeightByRows(8);
        docaGrid.setWidth("100%");
        docaGrid.setResponsive(true);
        docaGrid.setEditorBuffered(false);

        docaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

//            System.out.println("celda=" + cellReference.getProperty());
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        footerDoca = docaGrid.appendFooterRow();
        footerDoca.getCell(MONTO_PROPERTY).setText("0.00");
        footerDoca.getCell(MONTO_PROPERTY).setStyleName("rightalign");

//        contentLayout.addComponent(docaGrid);
//        contentLayout.setComponentAlignment(docaGrid, Alignment.MIDDLE_CENTER);

    }

    public final void llenarGridPartida() {
        documentsContainerPartida.removeAllItems();
        docaContainer.removeAllItems();

        if (partidaDocumentosGrid != null) {
            partidaDocumentosGrid.setCaption("Partida contable : " + codigoPartida + " " + descripcion + " Docto. " + documento);
        }

        double totalDebe = 0.00;
        double totalHaber = 0.00;
        double totalDebeQ = 0.00;
        double totalHaberQ = 0.00;

        String queryString = " select contabilidad_partida.TipoDocumento, contabilidad_partida.Fecha,";
        queryString += " contabilidad_partida.MonedaDocumento,";
        queryString += " contabilidad_partida.Debe, contabilidad_partida.Haber,";
        queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales,";
        queryString += " contabilidad_nomenclatura.N5, contabilidad_nomenclatura.NoCuenta,";
        queryString += " contabilidad_partida.TipoCambio, contabilidad_partida.CodigoCentroCosto";
        queryString += " from contabilidad_partida,contabilidad_nomenclatura";
        queryString += " where contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
        queryString += " and contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {

                    Object itemId = documentsContainerPartida.addItem();

                    documentsContainerPartida.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    documentsContainerPartida.getContainerProperty(itemId, FECHA_PARTIDA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    documentsContainerPartida.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    documentsContainerPartida.getContainerProperty(itemId, DESCRIPCION_PARTIDA_PROPERTY).setValue(rsRecords.getString("N5"));
                    if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                        documentsContainerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Debe")));
                    } else {
                        documentsContainerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Debe")));
                    }
                    documentsContainerPartida.getContainerProperty(itemId, DEBE_Q_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("DebeQuetzales")));
                    if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                        documentsContainerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Haber")));
                    } else {
                        documentsContainerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Haber")));
                    }
                    documentsContainerPartida.getContainerProperty(itemId, HABER_Q_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("HaberQuetzales")));
                    documentsContainerPartida.getContainerProperty(itemId, TASA_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                    documentsContainerPartida.getContainerProperty(itemId, CENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("CodigoCentroCosto"));

                    totalDebe = totalDebe + rsRecords.getDouble("Debe");
                    totalHaber = totalHaber + rsRecords.getDouble("Haber");

                    totalDebeQ = totalDebeQ + rsRecords.getDouble("DebeQuetzales");
                    totalHaberQ = totalHaberQ + rsRecords.getDouble("HaberQuetzales");

                } while (rsRecords.next());

                footer.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                footer.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));
                footer.getCell(DEBE_Q_PROPERTY).setText(numberFormat.format(totalDebeQ));
                footer.getCell(HABER_Q_PROPERTY).setText(numberFormat.format(totalHaberQ));

                queryString = " Select *  ";
                queryString += " From contabilidad_partida";
                queryString += " Where CodigoCC = '" + codigoPartida + "'"; //porque solamente quiero el monto del documento

                System.out.println(queryString);

                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado                                                
                    do {

                        Object itemId = docaContainer.addItem();

                        docaContainer.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        docaContainer.getContainerProperty(itemId, FECHA_PARTIDA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        docaContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                        docaContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("NombreProveedor"));
                        docaContainer.getContainerProperty(itemId, DESCRIPCION_PARTIDA_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                        docaContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("MontoDocumento")));
                        docaContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                        totalDebe = totalDebe + rsRecords.getDouble("MontoDocumento");

                    } while (rsRecords.next());

                    footerDoca.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalDebe));
                }
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla PARTIDAS AFECTADAS:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close(); //To change body of generated methods, choose Tools | Templates.
        if (stQuery != null) {
            try {
                stQuery.close();
            } catch (Exception ex) {
                System.out.println("Error al cerrar statement de consulta de partida contable: " + ex.getMessage());
            }
        }
    }

}
