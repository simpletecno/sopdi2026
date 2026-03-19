package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

public class ModificarPartidaContableView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    public ResultSet rsRecords1 = null;

    IndexedContainer container = new IndexedContainer();
    static final String ID_PARTIDA_PROPERTY = "IdPartida";
    static final String ID_EMPRESA_PROPERTY = "Empresa";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo de Partida";
    static final String CODIGO_CC_PROPERTY = "CódigoCC";
    static final String FECHA_PROPERTY = "Fecha Documento";
    static final String DESCRIPCION_PROPERTY = "Descripción Documento";
    static final String ORDEN_COMPRA_PROPERTY = "O. Compra";
    static final String ID_PROVEEDOR_PROPERTY = "IdProveedor";
    static final String NIT_PROVEEDOR_PROPERTY = "Nit";
    static final String NOMBRE_PROVEEDOR_PROPERTY = "Nombre";
    static final String NOMBRE_CHEQUE_PROPERTY = "Nombre cheque";
    static final String NUMERO_DOCTO_PROPERTY = "# Documento";
    static final String SERIE_PROPERTY = "Serie Documento";
    static final String TIPO_DOC_PROPERTY = "T. Documento";
    static final String ID_CONTABLE_PROPERTY = "Id Nomenc";
    static final String MONEDA_PROPERTY = "Moneda Documento";
    static final String MONTO_PROPERTY = "Monto Documento";
    static final String DEBE_PROPERTY = "Monto Debe";
    static final String HABER_PROPERTY = "Monto Haber";
    static final String TIPO_CAMBIO_PROPERTY = "Tipo cambio";
    static final String DEBEQ_PROPERTY = "Debe Quetzales.";
    static final String HABERQ_PROPERTY = "Haber Quetzales.";
    static final String SALDO_PROPERTY = "Saldo Documento";
    static final String MONTO_AUTORIZADO_PROPERTY = "Monto Autorizado P.";
    static final String MONTO_APLICAR_PROPERTY = "Monto Aplicar Antic";
    static final String ID_LIQUIDACION_PROPERTY = "Id Liquidación";
    static final String ID_LIQUIDADOR_PROPERTY = "Id Liquidador";
    static final String TIPO_ENGANCHE_PROPERTY = "Tipo de Enganche";
    static final String TIPO_VENTA_PROPERTY = "Tipo de Venta";
    static final String TIPO_DOCA_PROPERTY = "Tipo Documento";
    static final String NODOCA_PROPERTY = "No DOCA";
    static final String REFERENCIA_PROPERTY = "Referencia";
    static final String CREADOFECHAYHORA_PROPERTY = "Creado Fecha";
    static final String CREADO_USUARIO_PROPERTY = "Creado Usuario";
    static final String ANIO_PROPERTY = "AÑO";
    static final String PAGADO_IVA_PROPERTY = "Pagado IVA";
    static final String ID_COINCILIACION_PROPERTY = "Coinciliación";
    static final String ELIMINAR_PROPERTY = "Eliminar";
    Grid partidaGrid;

    Button updateBtn;
    Button buscarBtn;
    Button agregarBtn;
    Button cancelarBtn;
    Button refrescarBtn;

    TextField buscarCodigoTxt;

    UI mainUI = UI.getCurrent();

    VerticalLayout mainLayout;

    String queryString;

    public ModificarPartidaContableView() {

        setResponsive(true);
        setWidth("100%");
        setHeight("95%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.addStyleName("rcorners3");

        addComponent(mainLayout);

        Label titleLbl = new Label(" -- BUSCAR O MODIFICAR PARTIDA CONTABLE -- ");
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setResponsive(true);
        filtrosLayout.setMargin(false);
        filtrosLayout.setSpacing(true);

        buscarCodigoTxt = new TextField("Ingrese codigo de partida");
        buscarCodigoTxt.setWidth("13em");

        refrescarBtn = new Button("Revertir");
        refrescarBtn.setIcon(FontAwesome.REFRESH);
        refrescarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        refrescarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!buscarCodigoTxt.getValue().trim().isEmpty()) {
                    fillGrid();
                }
            }
        });

        agregarBtn = new Button("Agregar Linea");
        agregarBtn.setIcon(FontAwesome.PLUS);
        agregarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        agregarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                agregarLinea();
            }
        });

        buscarBtn = new Button("Buscar");
        buscarBtn.setIcon(FontAwesome.SEARCH_PLUS);
        buscarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        buscarBtn.setDescription("Buscar Partida");
        buscarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (!buscarCodigoTxt.getValue().trim().isEmpty()) {
                fillGrid();
            }else{
                container.removeAllItems();
            }                
        });

        cancelarBtn = new Button("Cancelar");
        cancelarBtn.setIcon(FontAwesome.CLOSE);
        cancelarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        cancelarBtn.setDescription("Buscar Partida");
        cancelarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (!buscarCodigoTxt.getValue().trim().isEmpty()) {
                fillGrid();
            }
        });

        updateBtn = new Button("Guardar");
        updateBtn.setIcon(FontAwesome.SAVE);
        updateBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        updateBtn.setDescription("Actualizar cambios");
        updateBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (container.size() == 0) { /// hay registros
                Notification notif = new Notification("No hay registros para actualizar..", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {
                try {
                    String codigoPartida = "codigoPartida1";
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                    ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

                    for (Object itemId : container.getItemIds()) {
                        Item item = container.getItem(itemId);

                        if (!codigoPartida.equals("codigoPartida1")) { /// VALIDAR QUE LAS LINEAS DESPUES DE LA PRIMERA COINCIDAN EN EL CODIGO PARTIDA
                            if (!codigoPartida.equals(String.valueOf(item.getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue()))) {
                                Notification notif = new Notification("Uno de los còdigos de partida no concuerda con los demas por favor revise..", Notification.Type.WARNING_MESSAGE);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.setIcon(FontAwesome.WARNING);
                                notif.show(Page.getCurrent());
                                return;
                            }
                        }

                        codigoPartida = String.valueOf(item.getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue());
                    }

                    if (!buscarCodigoTxt.getValue().trim().isEmpty()) { /// GUARDAR COPIA DE LA PARTIDA
                        queryString = " INSERT INTO contabilidad_partida_temporal";
                        queryString += " SELECT * FROM contabilidad_partida WHERE CodigoPartida = '" + buscarCodigoTxt.getValue().trim() + "'";

                        stQuery.executeUpdate(queryString);

                        queryString = " DELETE FROM contabilidad_partida "; /// ELIMINAR LA PARTIDA
                        queryString += " WHERE CodigoPartida = '" + buscarCodigoTxt.getValue().trim() + "'";

                        stQuery.executeUpdate(queryString);

                    }

                    queryString = " SELECT * FROM contabilidad_partida"; /// VERIFICAR QUE EL CODIGO PARTIDA INGRESADO NO SEA REPETIDO
                    queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";

                    rsRecords = stQuery.executeQuery(queryString);

                    if (rsRecords.next()) {
                        Notification.show("Este codigo de partida ya existe ya fué ingresado, por favor verifique.!.", Notification.Type.WARNING_MESSAGE);
                        try {
                            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
                            return;

                        } catch (SQLException ex1) {
                            Logger.getLogger(ModificarPartidaContableView.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                        return;
                    }

                    for (Object itemId : container.getItemIds()) { /// SI TODO ESTA BIEN RECORRER EL GRID E INSERTAR

                        Item item = container.getItem(itemId);

                        queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, Fecha, Descripcion, IdOrdenCompra,";
                        queryString += " IdProveedor, NITProveedor, NombreProveedor, NombreCheque, NumeroDocumento, SerieDocumento, TipoDocumento,";
                        queryString += " IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber, TipoCambio, DebeQuetzales, HaberQuetzales, ";
                        queryString += " Saldo, MontoAutorizadoPagar, MontoAplicarAnticipo, IdLiquidacion, IdLiquidador, TipoEnganche, TipoVenta, TipoDOCA, ";
                        queryString += " NoDoca, Referencia, CreadoFechayHora, CreadoUsuario, Año, PagadoIva, IdConciliacion, IdCentroCosto)";
                        queryString += " VALUES ";
                        queryString += " (";
                        queryString += String.valueOf(item.getItemProperty(ID_EMPRESA_PROPERTY).getValue());
                        queryString += ",'" + String.valueOf(item.getItemProperty(ESTATUS_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue()) + "'"; //codigoCC                        
                        queryString += ",'" + String.valueOf(item.getItemProperty(CODIGO_CC_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(FECHA_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(DESCRIPCION_PROPERTY).getValue()) + "'";

                        if (String.valueOf(item.getItemProperty(ORDEN_COMPRA_PROPERTY).getValue()) == null
                                || String.valueOf(item.getItemProperty(ORDEN_COMPRA_PROPERTY).getValue()).equals("")) {
                            queryString += ",0";
                        } else {
                            queryString += "," + String.valueOf(item.getItemProperty(ORDEN_COMPRA_PROPERTY).getValue());
                        }

                        queryString += "," + String.valueOf(item.getItemProperty(ID_PROVEEDOR_PROPERTY).getValue());
                        queryString += ",'" + String.valueOf(item.getItemProperty(NIT_PROVEEDOR_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(NOMBRE_PROVEEDOR_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(NOMBRE_CHEQUE_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(NUMERO_DOCTO_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(SERIE_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(TIPO_DOC_PROPERTY).getValue()) + "'";
                        queryString += "," + String.valueOf(item.getItemProperty(ID_CONTABLE_PROPERTY).getValue());
                        queryString += ",'" + String.valueOf(item.getItemProperty(MONEDA_PROPERTY).getValue()) + "'";
                        queryString += "," + String.valueOf(item.getItemProperty(MONTO_PROPERTY).getValue());

                        if (String.valueOf(item.getItemProperty(DEBE_PROPERTY).getValue()).equals("0.00")
                                && String.valueOf(item.getItemProperty(HABER_PROPERTY).getValue()).equals("0.00")) {
                            Notification.show("Por favor verifique los montos de esta partida..", Notification.Type.WARNING_MESSAGE);
                            return;
                        }
                        queryString += "," + String.valueOf(item.getItemProperty(DEBE_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(HABER_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(TIPO_CAMBIO_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(DEBEQ_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(HABERQ_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(SALDO_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(MONTO_AUTORIZADO_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(MONTO_APLICAR_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(ID_LIQUIDACION_PROPERTY).getValue());
                        queryString += "," + String.valueOf(item.getItemProperty(ID_LIQUIDADOR_PROPERTY).getValue());
                        queryString += ",'" + String.valueOf(item.getItemProperty(TIPO_ENGANCHE_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(TIPO_VENTA_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(TIPO_DOCA_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(NODOCA_PROPERTY).getValue()) + "'";
                        queryString += ",'" + String.valueOf(item.getItemProperty(REFERENCIA_PROPERTY).getValue()) + "'";
                        queryString += ",current_timestamp";
                        queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                        queryString += "," + String.valueOf(item.getItemProperty(ANIO_PROPERTY).getValue());
                        queryString += ",'" + String.valueOf(item.getItemProperty(PAGADO_IVA_PROPERTY).getValue()) + "'";
                        queryString += "," + String.valueOf(item.getItemProperty(ID_COINCILIACION_PROPERTY).getValue());
                        queryString += ",0";
                        queryString += ")";
                        
                        System.out.println("query string " + queryString);

                        stQuery.executeUpdate(queryString);
                    }

                    Notification notif = new Notification("Partida Actualizada con Exito!.", Notification.Type.HUMANIZED_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.CHECK);
                    notif.show(Page.getCurrent());

                    ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
                    ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

                    fillGrid();

                } catch (SQLException ex) {

                    try {
                        ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                        ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
                    } catch (SQLException ex1) {
                        Logger.getLogger(ModificarPartidaContableView.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    Notification.show("Por favor verifique los campos de la partida : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

            }
        });

        filtrosLayout.addComponents(buscarCodigoTxt, buscarBtn, refrescarBtn);
        filtrosLayout.setComponentAlignment(buscarCodigoTxt, Alignment.TOP_LEFT);
        filtrosLayout.setComponentAlignment(buscarBtn, Alignment.BOTTOM_LEFT);
        filtrosLayout.setComponentAlignment(refrescarBtn, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(filtrosLayout);
        mainLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_CENTER);

        createGrid();

    }

    public void createGrid() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, null);
        container.addContainerProperty(ID_EMPRESA_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(ORDEN_COMPRA_PROPERTY, String.class, null);
        container.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(NIT_PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(NOMBRE_PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(NOMBRE_CHEQUE_PROPERTY, String.class, null);
        container.addContainerProperty(NUMERO_DOCTO_PROPERTY, String.class, null);
        container.addContainerProperty(SERIE_PROPERTY, String.class, null);
        container.addContainerProperty(TIPO_DOC_PROPERTY, String.class, null);
        container.addContainerProperty(ID_CONTABLE_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(DEBE_PROPERTY, String.class, null);
        container.addContainerProperty(HABER_PROPERTY, String.class, null);
        container.addContainerProperty(TIPO_CAMBIO_PROPERTY, String.class, null);
        container.addContainerProperty(DEBEQ_PROPERTY, String.class, null);
        container.addContainerProperty(HABERQ_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_AUTORIZADO_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_APLICAR_PROPERTY, String.class, null);
        container.addContainerProperty(ID_LIQUIDACION_PROPERTY, String.class, null);
        container.addContainerProperty(ID_LIQUIDADOR_PROPERTY, String.class, null);
        container.addContainerProperty(TIPO_ENGANCHE_PROPERTY, String.class, null);
        container.addContainerProperty(TIPO_VENTA_PROPERTY, String.class, null);
        container.addContainerProperty(TIPO_DOCA_PROPERTY, String.class, null);
        container.addContainerProperty(NODOCA_PROPERTY, String.class, null);
        container.addContainerProperty(REFERENCIA_PROPERTY, String.class, null);
        container.addContainerProperty(CREADOFECHAYHORA_PROPERTY, String.class, null);
        container.addContainerProperty(CREADO_USUARIO_PROPERTY, String.class, null);
        container.addContainerProperty(ANIO_PROPERTY, String.class, null);
        container.addContainerProperty(PAGADO_IVA_PROPERTY, String.class, null);
        container.addContainerProperty(ID_COINCILIACION_PROPERTY, String.class, null);
        container.addContainerProperty(ELIMINAR_PROPERTY, String.class, null);

        partidaGrid = new Grid("Partida Contable", container);
        partidaGrid.setWidth("100%");
        partidaGrid.setDescription("Seleccione un registro.");
        partidaGrid.setHeightMode(HeightMode.ROW);
        partidaGrid.setHeightByRows(12);
        //partidaGrid.setResponsive(true);
        partidaGrid.setEditorEnabled(true);
        partidaGrid.setEditorBuffered(true);
        partidaGrid.setImmediate(true);
        //partidaGrid.addItemClickListener((event) -> {
        //    if (event != null) {
        //        partidaGrid.editItem(event.getItemId());
        //    }
        //});

        partidaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBEQ_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABERQ_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TIPO_CAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (MONTO_APLICAR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (MONTO_AUTORIZADO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (ID_EMPRESA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ORDEN_COMPRA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ID_CONTABLE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ID_LIQUIDADOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ID_LIQUIDACION_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ID_COINCILIACION_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });

        partidaGrid.getColumn(ELIMINAR_PROPERTY).setRenderer(new ButtonRenderer(e
                -> eliminarRegistroTabla(e)));
        partidaGrid.setResponsive(true);
        partidaGrid.setEditorBuffered(false);

        partidaGrid.getColumn(CREADO_USUARIO_PROPERTY).setHidden(true).setHidable(true);
        partidaGrid.getColumn(CREADOFECHAYHORA_PROPERTY).setHidden(true).setHidable(true);

        HorizontalLayout layoutButton = new HorizontalLayout();
        layoutButton.setSpacing(true);
        layoutButton.addComponents(cancelarBtn, agregarBtn, updateBtn);
        layoutButton.setComponentAlignment(cancelarBtn, Alignment.MIDDLE_CENTER);
        layoutButton.setComponentAlignment(agregarBtn, Alignment.MIDDLE_CENTER);
        layoutButton.setComponentAlignment(updateBtn, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(partidaGrid);
        mainLayout.setComponentAlignment(partidaGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(layoutButton);
        mainLayout.setComponentAlignment(layoutButton, Alignment.MIDDLE_CENTER);

    }

    public void fillGrid() {

        container.removeAllItems();

        queryString = "SELECT * ";
        queryString += " FROM contabilidad_partida ";
        queryString += " WHERE CodigoPartida = '" + buscarCodigoTxt.getValue().trim() + "'";
        queryString += " ORDER BY IdPartida";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    container.getContainerProperty(itemId, ID_EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    container.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    container.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(rsRecords.getString("Fecha"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    container.getContainerProperty(itemId, ORDEN_COMPRA_PROPERTY).setValue(rsRecords.getString("IdOrdenCompra"));
                    container.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    container.getContainerProperty(itemId, NIT_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NITProveedor"));
                    container.getContainerProperty(itemId, NOMBRE_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    container.getContainerProperty(itemId, NOMBRE_CHEQUE_PROPERTY).setValue(rsRecords.getString("NombreCheque"));
                    container.getContainerProperty(itemId, NUMERO_DOCTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                    container.getContainerProperty(itemId, SERIE_PROPERTY).setValue(rsRecords.getString("SerieDocumento"));
                    container.getContainerProperty(itemId, TIPO_DOC_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    container.getContainerProperty(itemId, ID_CONTABLE_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                    container.getContainerProperty(itemId, MONTO_PROPERTY).setValue(rsRecords.getString("MontoDocumento"));
                    container.getContainerProperty(itemId, DEBE_PROPERTY).setValue(rsRecords.getString("Debe"));
                    container.getContainerProperty(itemId, HABER_PROPERTY).setValue(rsRecords.getString("Haber"));
                    container.getContainerProperty(itemId, TIPO_CAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                    container.getContainerProperty(itemId, DEBEQ_PROPERTY).setValue(rsRecords.getString("DebeQuetzales"));
                    container.getContainerProperty(itemId, HABERQ_PROPERTY).setValue(rsRecords.getString("HaberQuetzales"));
                    container.getContainerProperty(itemId, SALDO_PROPERTY).setValue(rsRecords.getString("Saldo"));
                    container.getContainerProperty(itemId, MONTO_AUTORIZADO_PROPERTY).setValue(rsRecords.getString("MontoAutorizadoPagar"));
                    container.getContainerProperty(itemId, MONTO_APLICAR_PROPERTY).setValue(rsRecords.getString("MontoAplicarAnticipo"));
                    container.getContainerProperty(itemId, ID_LIQUIDACION_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));
                    container.getContainerProperty(itemId, ID_LIQUIDADOR_PROPERTY).setValue(rsRecords.getString("IdLiquidador"));
                    container.getContainerProperty(itemId, TIPO_ENGANCHE_PROPERTY).setValue(rsRecords.getString("TipoEnganche"));
                    container.getContainerProperty(itemId, TIPO_VENTA_PROPERTY).setValue(rsRecords.getString("TipoVenta"));
                    container.getContainerProperty(itemId, TIPO_DOCA_PROPERTY).setValue(rsRecords.getString("TipoDOCA"));
                    container.getContainerProperty(itemId, NODOCA_PROPERTY).setValue(rsRecords.getString("NoDOCA"));
                    container.getContainerProperty(itemId, REFERENCIA_PROPERTY).setValue(rsRecords.getString("Referencia"));
                    container.getContainerProperty(itemId, CREADOFECHAYHORA_PROPERTY).setValue(rsRecords.getString("CreadoFechaYHora"));
                    container.getContainerProperty(itemId, CREADO_USUARIO_PROPERTY).setValue(rsRecords.getString("CreadoUsuario"));
                    container.getContainerProperty(itemId, ANIO_PROPERTY).setValue(rsRecords.getString("Año"));
                    container.getContainerProperty(itemId, PAGADO_IVA_PROPERTY).setValue(rsRecords.getString("PagadoIva"));
                    container.getContainerProperty(itemId, ID_COINCILIACION_PROPERTY).setValue(rsRecords.getString("IdConciliacion"));
                    container.getContainerProperty(itemId, ELIMINAR_PROPERTY).setValue("ELIMINAR");

                } while (rsRecords.next());

            }
        } catch (Exception ex) {
            Logger.getLogger(ModificarPartidaContableView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de contabilidad_partida : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de contabilidad_partida..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    public void agregarLinea() {
        Object itemId = container.addItem();

        container.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue("0");
        container.getContainerProperty(itemId, ID_EMPRESA_PROPERTY).setValue("0");
        container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue("INGRESADO");
        container.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("");
        container.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue("");
        container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaYYYYMMDD_1(new java.util.Date()));
        container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("");
        container.getContainerProperty(itemId, ORDEN_COMPRA_PROPERTY).setValue("0");
        container.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue("0");
        container.getContainerProperty(itemId, NIT_PROVEEDOR_PROPERTY).setValue("0");
        container.getContainerProperty(itemId, NOMBRE_PROVEEDOR_PROPERTY).setValue("");
        container.getContainerProperty(itemId, NOMBRE_CHEQUE_PROPERTY).setValue("");
        container.getContainerProperty(itemId, NUMERO_DOCTO_PROPERTY).setValue("");
        container.getContainerProperty(itemId, SERIE_PROPERTY).setValue("");
        container.getContainerProperty(itemId, TIPO_DOC_PROPERTY).setValue("");
        container.getContainerProperty(itemId, ID_CONTABLE_PROPERTY).setValue("0");
        container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue("QUETZALES");
        container.getContainerProperty(itemId, MONTO_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, DEBE_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, HABER_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, TIPO_CAMBIO_PROPERTY).setValue("1.00000");
        container.getContainerProperty(itemId, DEBEQ_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, HABERQ_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, SALDO_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, MONTO_AUTORIZADO_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, MONTO_APLICAR_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, ID_LIQUIDACION_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, ID_LIQUIDADOR_PROPERTY).setValue("0.00");
        container.getContainerProperty(itemId, TIPO_ENGANCHE_PROPERTY).setValue(" ");
        container.getContainerProperty(itemId, TIPO_VENTA_PROPERTY).setValue(" ");
        container.getContainerProperty(itemId, TIPO_DOCA_PROPERTY).setValue("FACTURA");
        container.getContainerProperty(itemId, NODOCA_PROPERTY).setValue(" ");
        container.getContainerProperty(itemId, REFERENCIA_PROPERTY).setValue(" ");
        container.getContainerProperty(itemId, CREADOFECHAYHORA_PROPERTY).setValue("");
        container.getContainerProperty(itemId, CREADO_USUARIO_PROPERTY).setValue("");
        container.getContainerProperty(itemId, ANIO_PROPERTY).setValue("0");
        container.getContainerProperty(itemId, PAGADO_IVA_PROPERTY).setValue("NO");
        container.getContainerProperty(itemId, ID_COINCILIACION_PROPERTY).setValue("0");
        container.getContainerProperty(itemId, ELIMINAR_PROPERTY).setValue("Eliminar");
    }

    public void eliminarRegistroTabla(ClickableRenderer.RendererClickEvent e) {

        partidaGrid.select(e.getItemId());

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de Eliminar la factura y sus registros relacionados?",
                "SI", "NO", new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    try {

                        Notification notif = new Notification("Registro eliminado exitosamente!.", Notification.Type.TRAY_NOTIFICATION);
                        notif.setDelayMsec(1500);
                        notif.setPosition(Position.MIDDLE_CENTER);
                        notif.setIcon(FontAwesome.CHECK);
                        notif.show(Page.getCurrent());

                        container.removeItem(e.getItemId());

                    } catch (Exception ex) {
                        System.out.println("Error al intentar eliminar " + ex);
                        ex.printStackTrace();
                    }
                } else {
                    Notification.show("Operación cacelada!", Notification.Type.WARNING_MESSAGE);
                }
            }
        });
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Modificar partidas contables");
    }
}
