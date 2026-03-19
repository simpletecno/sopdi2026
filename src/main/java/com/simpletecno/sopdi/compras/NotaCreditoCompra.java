/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class NotaCreditoCompra extends Window {

    VerticalLayout mainLayout;

    Grid productosGrid;

    static final String ID_PROPERTY = "Id";
    static final String ID_PRODUCTO_PROPERTY = "Cod producto.";
    static final String NOMBRE_PROPERTY = "ProductoNota";
    static final String CANTIDAD_PROPERTY = "Cantidad";
    static final String CANTIDAD_QUITADA_PROPERTY = "Descontar";
    static final String BODEGA_PROPERTY = "Bodega";

    String queryString;

    Label titleLbl;
    DateField fechaDt;
    private TextField serieTxt;
    TextField numeroTxt;
    NumberField montoTxt;
    NumberField tasaCambioTxt;

    Button guardarBtn;
    Statement stQuery, stQuery2;
    ResultSet rsRecords, rsRecords2;

    UI mainUI;

    String empresa;
    IndexedContainer container;
    Object itemId;
    String codigoPartida;
    String serieDocumento;
    String numeroDocumento;

    CheckBox rebajarInventarioCheck;
    String ordenCompra = "";

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public NotaCreditoCompra(
            String empresa,
            IndexedContainer container,
            Object itemId,
            String codigoPartida,
            String serieDocumento,
            String numeroDocumento) {

        this.empresa = empresa;
        this.container = container;
        this.itemId = itemId;
        this.codigoPartida = codigoPartida;
        this.serieDocumento = serieDocumento;
        this.numeroDocumento = numeroDocumento;
        this.mainUI = UI.getCurrent();
        setResponsive(true);

        MarginInfo marginInfo = new MarginInfo(false, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setResponsive(true);
// ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()), 0);
        if((mainUI.getNavigator().getCurrentView()).getClass().getSimpleName().equals("IngresoDocumentosView")) {
            titleLbl = new Label("NOTA DE CREDITO PARA : "
                    + container.getContainerProperty(itemId, IngresoDocumentosView.DOCUMENTO_PROPERTY).getValue()
                    + " " + container.getContainerProperty(itemId, IngresoDocumentosView.PROVEEDOR_PROPERTY).getValue());
        }
        else {
            titleLbl = new Label("NOTA DE CREDITO PARA : "
                    + container.getContainerProperty(itemId, IngresoLiquidacionGastoView.NUMERO_PROPERTY).getValue()
                    + " " + container.getContainerProperty(itemId, IngresoLiquidacionGastoView.PROVEEDOR_PROPERTY).getValue());
        }
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        mainLayout.addComponent(titleLbl);

        serieTxt = new TextField("Serie : ");
        serieTxt.setWidth("100%");
        serieTxt.addStyleName("mayusculas");
        serieTxt.focus();

        numeroTxt = new TextField("Número : ");
        numeroTxt.setWidth("100%");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        montoTxt = new NumberField("Monto : ");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("100%");
//        montoTxt.addValueChangeListener(event -> {
//            if (cuentaContable1Cbx != null) {
//                verificarProveedor();
//            }
//        });

        tasaCambioTxt = new NumberField("Tipo de Cambio : ");
        tasaCambioTxt.setDecimalAllowed(true);
        tasaCambioTxt.setDecimalPrecision(5);
        tasaCambioTxt.setMinimumFractionDigits(5);
        tasaCambioTxt.setDecimalSeparator('.');
        tasaCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tasaCambioTxt.setGroupingUsed(true);
        tasaCambioTxt.setGroupingSeparator(',');
        tasaCambioTxt.setGroupingSize(3);
        tasaCambioTxt.setImmediate(true);
        tasaCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tasaCambioTxt.setWidth("100%");
        tasaCambioTxt.setValue(1.00);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addStyleName("rcorners3");
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);
        horizontalLayout.setWidth("100%");

        horizontalLayout.addComponents(serieTxt, numeroTxt, fechaDt, montoTxt, tasaCambioTxt);

        mainLayout.addComponent(horizontalLayout);

        guardarBtn = new Button("Grabar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CREAR LA NOTA DE CREDITO?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            insertarPartida();
                        }
                    }
                }
                );
            }
        });

        Button salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        rebajarInventarioCheck = new CheckBox("Rebajar Inventario");
        rebajarInventarioCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        rebajarInventarioCheck.setValue(false);
        rebajarInventarioCheck.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (rebajarInventarioCheck.equals(true)) {
                    if (!ordenCompra.trim().isEmpty()) {
                        productosGrid.setVisible(true);
                        llenarGridProducto(ordenCompra);
                    }
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.setWidth("100%");

        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(guardarBtn);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);

        setContent(mainLayout);

        encontrarPartidaDocumento();

        crearGridProducto();

    }

    private void encontrarPartidaDocumento() {
        queryString = " SELECT  *";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";
        queryString += " AND IdEmpresa = " + empresa;
        queryString += " AND SerieDocumento = '" + serieDocumento + "'";
        queryString += " AND NumeroDocumento = '" + numeroDocumento + "'";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                if (rsRecords.getString("MonedaDocumento").toUpperCase().equals("DOLARES")) {
                    tasaCambioTxt.setReadOnly(false);
                    tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
                } else {
                    tasaCambioTxt.setReadOnly(false);
                    tasaCambioTxt.setValue(1.00);
                    tasaCambioTxt.setReadOnly(true);
                }
            }
        } catch (Exception ex1) {
            Notification notif = new Notification("ERROR AL BUSCAR CODIGO DE PARTIDA DE DOCUMENTO FACTURA.",
                    Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

        }

    }

    private void insertarPartida() {
        /*
        if (((SopdiUI) UI.getCurrent()).esMesCerrado(empresa, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(empresa, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresa)), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }

         */
        if (this.serieTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese la serie del documento.", Notification.Type.WARNING_MESSAGE);
            serieTxt.focus();
            return;
        }
        if (this.numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese el número de documento.", Notification.Type.WARNING_MESSAGE);
            numeroTxt.focus();
            return;
        }
        if (this.montoTxt.getDoubleValueDoNotThrow() == 0) {
            Notification.show("Por favor ingrese el monto de la NOTA DE CREDITO.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return;
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartidaNC = String.valueOf(empresa) + año + mes + dia + "6";

        queryString = " SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE codigoPartida LIKE '" + codigoPartidaNC + "%'";
        queryString += " ORDER BY codigoPartida DESC ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                codigoPartidaNC += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartidaNC += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        double montoDocumento = 0.00;

        queryString = " SELECT  *";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";
        queryString += " AND IdEmpresa = " + empresa;
        queryString += " AND SerieDocumento = '" + serieDocumento + "'";
        queryString += " AND NumeroDocumento = '" + numeroDocumento + "'";
        queryString += " ORDER BY Haber DESC";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                if (rsRecords.getObject("IdOrdenCompra") != null) {
                    ordenCompra = rsRecords.getString("IdOrdenCompra");
                }
                do {

                    if (rsRecords.getString("IdNomenclatura").trim().equals( ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores())) { // PROVEEDORES LOCALES
                        if (rsRecords.getDouble("Haber") < montoTxt.getDoubleValueDoNotThrow()) {
                            Notification notif = new Notification("EL MONTO DE LA NOTA DE CREDITO, NO PUEDE SER MAYOR AL MONTO DE LA FACTURA. POR FAVOR REVISE!",
                                    Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());
                            montoTxt.focus();
                            return;
                        }
                    }
                    montoDocumento += rsRecords.getDouble("Haber");

                } while (rsRecords.next());

Logger.getLogger(this.getClass().getName()).log(Level.INFO, "montoDocumento=" + montoDocumento);

                rsRecords.first();

                queryString = " INSERT INTO contabilidad_partida (IdEmpresa, CodigoPartida, CodigoCC,";
                queryString += " TipoDocumento, Fecha, IdOrdenCompra, IdProveedor, NITProveedor, NombreProveedor,";
                queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, ";
                queryString += " Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Estatus, ";
                queryString += " IdLiquidacion, IdLiquidador, Descripcion, Referencia,";
                queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre";
                queryString += ")";
                queryString += " VALUES ";

                do {
                    queryString += " (";
                    queryString += empresa;
                    queryString += ",'" + codigoPartidaNC + "'";
                    queryString += ",'" + rsRecords.getString("CodigoCC") + "'";
                    queryString += ",'NOTA DE CREDITO COMPRA'";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                    queryString += ",0";
                    queryString += "," + rsRecords.getString("IdProveedor");
                    queryString += ",'" + rsRecords.getString("NitProveedor") + "'";
                    queryString += ",'" + rsRecords.getString("NombreProveedor") + "'";
                    queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                    queryString += ",'" + numeroTxt.getValue().trim() + "'";
                    queryString += "," + rsRecords.getString("IdNomenclatura");
                    queryString += ",'" + rsRecords.getString("MonedaDocumento") + "'";
                    queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
                    if(rsRecords.getDouble("Debe") > 0) { //reversar debe
                        queryString += ",0.00"; //DEBE
                        queryString += "," + ((rsRecords.getDouble("Debe") / montoDocumento) * montoTxt.getDoubleValueDoNotThrow()); //HABER
                        queryString += ",0.00"; //DEBE Q.
                        queryString += "," + (((rsRecords.getDouble("Debe") / montoDocumento) * montoTxt.getDoubleValueDoNotThrow()) * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
                    }
                    else {  //reversar haber
                        queryString += "," + ((rsRecords.getDouble("Haber") / montoDocumento) * montoTxt.getDoubleValueDoNotThrow()); //DEBE
                        queryString += ",0.00"; //HABER
                        queryString += "," + (((rsRecords.getDouble("Haber") / montoDocumento) * montoTxt.getDoubleValueDoNotThrow()) * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
                        queryString += ",0.00"; //HABER Q.
                    }
/*** JA 28 FEB 2022
                    if (rsRecords.getString("IdNomenclatura").trim().equals("7")) { // IVA
                        queryString += ",0.00"; //DEBE
                        queryString += "," + String.valueOf((montoTxt.getDoubleValueDoNotThrow() / 1.12) * 0.12); //HABER
                        queryString += ",0.00"; //DEBE Q.
                        queryString += "," + String.valueOf(((montoTxt.getDoubleValueDoNotThrow() / 1.12) * 0.12) * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
                    } else if (rsRecords.getString("IdNomenclatura").trim().equals("171")) { // OTROS ARBITRIOS
                        queryString += ",0.00"; //DEBE
                        queryString += "," + String.valueOf((montoTxt.getDoubleValueDoNotThrow() / 1.12) * 0.005); //HABER
                        queryString += ",0.00"; //DEBE Q.
                        queryString += "," + String.valueOf(((montoTxt.getDoubleValueDoNotThrow() / 1.12) * 0.005) * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
                    } else if (rsRecords.getString("IdNomenclatura").trim().equals("76")) { // es proveedores locales
                        queryString += "," + String.valueOf((montoTxt.getDoubleValueDoNotThrow())); //DEBE
                        queryString += ",0.00"; //HABER
                        queryString += "," + String.valueOf((montoTxt.getDoubleValueDoNotThrow()) * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
                        queryString += ",0.00"; //HABER Q.
                    } else {
                        queryString += ",0.00"; //DEBE
                        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow() / 1.12); //HABER
                        queryString += ",0.00"; //DEBE Q.
                        queryString += "," + String.valueOf((montoTxt.getDoubleValueDoNotThrow() / 1.12) * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
                    }
***/
                    queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow()); //SALDO
                    queryString += ",0.00"; //SALDO
                    queryString += ",'INGRESADO'";
                    queryString += "," + rsRecords.getString("IdLiquidacion");
                    if(rsRecords.getInt("IdLiquidador") > 0) {
                        queryString += "," + rsRecords.getString("IdLiquidador");
                    }
                    else {
                        queryString += "," + rsRecords.getString("IdProveedor");
                    }
                    queryString += ",'" + titleLbl.getValue() + "'";
//                    queryString += ",'NOTA DE CREDITO PARA : " + container.getContainerProperty(itemId, IngresoDocumentosView.DOCUMENTO_PROPERTY).getValue()
//                            + " " + container.getContainerProperty(itemId, IngresoDocumentosView.PROVEEDOR_PROPERTY).getValue() + "'";
                    queryString += ",'NO'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",null";
                    queryString += ",null";
                    queryString += ",0";
                    queryString += ",null";

                    queryString += "),";

                } while (rsRecords.next());

                queryString = queryString.substring(0, queryString.length() - 1);

                stQuery2.executeUpdate(queryString);

//Notification.show(titleLbl.getValue());

                Notification notif = new Notification("NOTA DE CREDITO COMPRA AGREGADA EXITOSAMENTE. POR FAVOR CONSULTE NUEVAMENTE EL LISTADO DE DOCUMENTOS INGRESADOS.",
                        Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.CHECK);
                notif.show(Page.getCurrent());

                MostrarPartidaContable mostrarPartidaContable
                        = new MostrarPartidaContable(codigoPartidaNC,
                                "NOTA DE CREDITO COMPRA",
                                titleLbl.getCaption(),
                                codigoPartidaNC
                        );
                UI.getCurrent().addWindow(mostrarPartidaContable);
                mostrarPartidaContable.center();

                close();

            }
        } catch (Exception ex1) {
            System.out.println(queryString);
            Notification notif = new Notification("ERROR AL BUSCAR CODIGO DE PARTIDA DE DOCUMENTO FACTURA." + ex1.getMessage(),
                    Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            ex1.printStackTrace();
        }

    }

    public TextField getSerieTxt() {
        return serieTxt;
    }

    public void setSerieTxt(TextField serieTxt) {
        this.serieTxt = serieTxt;
    }

    public void crearGridProducto() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners3");
        detalleLayout.setSpacing(true);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(ID_PRODUCTO_PROPERTY, String.class, null);
        container.addContainerProperty(NOMBRE_PROPERTY, String.class, null);
        container.addContainerProperty(CANTIDAD_PROPERTY, String.class, null);
        container.addContainerProperty(CANTIDAD_QUITADA_PROPERTY, String.class, null);
        container.addContainerProperty(BODEGA_PROPERTY, String.class, null);

        productosGrid = new Grid(container);
        productosGrid.setImmediate(true);
        productosGrid.setSelectionMode(Grid.SelectionMode.NONE);
        productosGrid.setHeightMode(HeightMode.ROW);
        productosGrid.setHeightByRows(5);
        productosGrid.setWidth("100%");
        productosGrid.setResponsive(true);
        productosGrid.setEditorBuffered(false);
        productosGrid.setVisible(false);

        productosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        productosGrid.getColumn(ID_PRODUCTO_PROPERTY).setHidable(true).setHidden(true);

        productosGrid.getColumn(CANTIDAD_QUITADA_PROPERTY).setEditable(true);

        /* DESCOMENTAR SI EN DADO CASO HAY QUE MOSTRAR PRECIOS
        productosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (PRECIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";            
            } else {
                return null;
            }
        });
         */
        detalleLayout.addComponent(productosGrid);

        mainLayout.addComponent(detalleLayout);
        mainLayout.setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarGridProducto(String idOrdenCompra) {

        container.removeAllItems();

        queryString = " SELECT *, inv_producto.Descripcion, inv_bodega.Nombre as NombreBodega ";
        queryString += " FROM orden_compra_detalle";
        queryString += " INNER JOIN inv_producto on orden_compra_detalle.IdProducto = inv_producto.IdProducto";
        queryString += " LEFT JOIN inv_bodega on orden_compra_detalle.IdBodega = inv_bodega.IdBodega";
        queryString += " WHERE IdOrdenCompra = " + idOrdenCompra;

        try {
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado  

                do {

                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords2.getString("Id"));
                    container.getContainerProperty(itemId, ID_PRODUCTO_PROPERTY).setValue(rsRecords2.getString("IdProducto"));
                    container.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords2.getString("Descripcion"));
                    container.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(rsRecords2.getString("Cantidad"));
                    container.getContainerProperty(itemId, CANTIDAD_QUITADA_PROPERTY).setValue("0");
                    container.getContainerProperty(itemId, BODEGA_PROPERTY).setValue(rsRecords2.getString("NombreBodega"));

                } while (rsRecords2.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tablea orden compra detalle:" + ex);
            ex.printStackTrace();
        }

    }
}
