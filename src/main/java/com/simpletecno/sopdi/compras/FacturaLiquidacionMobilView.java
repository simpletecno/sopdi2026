package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.sun.istack.logging.Logger;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;

public class FacturaLiquidacionMobilView extends VerticalLayout implements View {

    static final String FECHA_PROPERTY = "Fecha";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FACTURA_PROPERTY = "Documento";
    static final String MONTO_PROPERTY = "Monto";
    static final String LIQUIDACION_PROPERTY = "LIQUIDACION";
    static final String CREADOSTAMP_PROPERTY = "Creado el";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    private Statement stQuery;
    private ResultSet rsRecords;

    private ComboBox proveedorCbx = new ComboBox("Proveedor : " );
    private ComboBox cuentaContableCbx = new ComboBox("Cuenta contable : ");
    private ComboBox centroCostoCbx = new ComboBox("Centro de costo : ");
    private TextField numeroTxt = new TextField("Número factura : ");
    private NumberField montoTxt = new NumberField("Monto : ");

    private Button guardarBtn = new Button("Guardar");
    private Button cerrarBtn = new Button("Cerrar Liquidación");

    private IndexedContainer documentosContainer = new IndexedContainer();
    private Grid documentosGrid;
    Grid.FooterRow footerliquidaciones;

    public FacturaLiquidacionMobilView() {

        setSpacing(false);
        setMargin(true);
        setHeightUndefined();

        HorizontalLayout empresaLayout = new HorizontalLayout();
        empresaLayout.setResponsive(true);
        empresaLayout.setSpacing(true);
        empresaLayout.setWidth("100%");

        Label empresaLbl = new Label(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName());
        empresaLbl.addStyleName(ValoTheme.LABEL_H4);
        empresaLbl.setWidth("100%");
        empresaLbl.addStyleName("h1_custom");

        empresaLayout.addComponent(empresaLbl);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(false);
        titleLayout.setWidth("100%");

        Label titleLbl = new Label("FACTURA LIQUIDACION MOBIL");
        titleLbl.addStyleName(ValoTheme.LABEL_H4);
        titleLbl.setWidth("100%");
        titleLbl.addStyleName("h1_custom");

        titleLayout.addComponent(titleLbl);

        addComponents(empresaLayout,titleLayout);
        setComponentAlignment(empresaLayout, Alignment.TOP_CENTER);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        proveedorCbx.setWidth("100%");
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(e -> {
            fillComboCuentaContable();
        });
        proveedorCbx.addStyleName(ValoTheme.COMBOBOX_SMALL);

        HorizontalLayout proveedorLayout = new HorizontalLayout();
        proveedorLayout.setResponsive(true);
        proveedorLayout.setSpacing(true);
        proveedorLayout.setWidth("90%");

        proveedorLayout.addComponent(proveedorCbx);
        proveedorLayout.setComponentAlignment(proveedorCbx, Alignment.MIDDLE_CENTER);

        llenarComboProveedor();

        cuentaContableCbx.setImmediate(true);
        cuentaContableCbx.setNullSelectionAllowed(false);
        cuentaContableCbx.setTextInputAllowed(true);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.clear();
        cuentaContableCbx.setWidth("100%");
        cuentaContableCbx.addValueChangeListener(e -> {
            llenarComboCentroCosto();
        });

        HorizontalLayout cuentaContableLayout = new HorizontalLayout();
        cuentaContableLayout.setResponsive(true);
        cuentaContableLayout.setSpacing(true);
        cuentaContableLayout.setWidth("90%");

        cuentaContableLayout.addComponent(cuentaContableCbx);
        cuentaContableLayout.setComponentAlignment(cuentaContableCbx, Alignment.MIDDLE_CENTER);

        centroCostoCbx.setWidth("100%");
        centroCostoCbx.setTextInputAllowed(false);
        centroCostoCbx.setInvalidAllowed(false);
        centroCostoCbx.setNewItemsAllowed(false);
        centroCostoCbx.setNullSelectionAllowed(true);
        centroCostoCbx.setFilteringMode(FilteringMode.STARTSWITH);
//        centroCostoCbx.addStyleName(ValoTheme.COMBOBOX_SMALL);

        HorizontalLayout centroCostoLayout = new HorizontalLayout();
        centroCostoLayout.setResponsive(true);
        centroCostoLayout.setSpacing(true);
        centroCostoLayout.setWidth("90%");

        centroCostoLayout.addComponent(centroCostoCbx);
        centroCostoLayout.setComponentAlignment(centroCostoCbx, Alignment.MIDDLE_CENTER);
//        llenarComboCentroCosto();

        numeroTxt.setWidth("100%");
        numeroTxt.addStyleName("mayusculas");
        numeroTxt.setInputPrompt("Número de factura SIN la serie..");
        numeroTxt.setDescription("Correlativo de factura");

        HorizontalLayout numeroLayout = new HorizontalLayout();
        numeroLayout.setResponsive(true);
        numeroLayout.setSpacing(true);
        numeroLayout.setWidth("90%");

        numeroLayout.addComponent(numeroTxt);
        numeroLayout.setComponentAlignment(numeroTxt, Alignment.MIDDLE_CENTER);

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
        montoTxt.setValue(0.00);

        HorizontalLayout montoLayout = new HorizontalLayout();
        montoLayout.setResponsive(true);
        montoLayout.setSpacing(true);
        montoLayout.setWidth("90%");

        montoLayout.addComponent(montoTxt);
        montoLayout.setComponentAlignment(montoTxt, Alignment.MIDDLE_CENTER);

        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertarDocumento();
            }
        });

        HorizontalLayout botonLayout = new HorizontalLayout();
        botonLayout.setResponsive(true);
        botonLayout.setSpacing(true);
        botonLayout.setMargin(true);
        botonLayout.setWidth("100%");

        botonLayout.addComponent(guardarBtn);
        botonLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

//        addComponents(proveedorLayout, centroCostoLayout, cuentaContableLayout, numeroLayout, montoLayout, botonLayout);
        addComponents(proveedorLayout,cuentaContableLayout, centroCostoLayout, numeroLayout, botonLayout);
        setComponentAlignment(proveedorLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(centroCostoLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(cuentaContableLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(numeroLayout, Alignment.MIDDLE_CENTER);
//        setComponentAlignment(montoLayout, Alignment.MIDDLE_CENTER);

        crearGridDocumentos();
        llenarGridDocumentos();
    }

    public void llenarComboProveedor() {
        String queryString = " SELECT prv.IdProveedor, prv.Nombre ";
        queryString += " FROM empleado_liquidador el";
        queryString += " INNER JOIN proveedor prv ON prv.IdProveedor = el.IdProveedor ";
        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor() == null || ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor().isEmpty()) {
            Notification.show("El usuario no tiene un IdLiquidador asignado.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        queryString += " WHERE el.IdEmpleado = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
        queryString += " AND   el.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY prv.Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                do {
                    proveedorCbx.addItem(rsRecords.getString("IdProveedor"));
                    proveedorCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre"));
                } while (rsRecords.next());
            }
        } catch (Exception ex1) {
            Notification.show("ERROR AL BUSCAR PROVEEDORES : " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }
    }

    public void llenarComboCentroCosto() {

        centroCostoCbx.removeAllItems();

        centroCostoCbx.addItem("0");
        centroCostoCbx.setItemCaption("0", "NO APLICA");

        if(cuentaContableCbx.getValue() == null) {
            return;
        }

        String queryString = " SELECT * FROM centro_costo";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId();
        queryString += " AND Inhabilitado = 0";
        queryString += " AND IdNomenclatura = " + cuentaContableCbx.getValue();
        queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        centroCostoCbx.addItem("0");
        centroCostoCbx.setItemCaption("0", "NO APLICA");

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    centroCostoCbx.addItem(rsRecords.getString("IdCentroCosto"));
                    //centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto") + " " + rsRecords.getString("Grupo"));
                    centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto"));
                } while (rsRecords.next());
            }

            centroCostoCbx.select("0");

        } catch (Exception ex1) {
            Notification.show("ERROR AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }
    }

    private void fillComboCuentaContable() {

        cuentaContableCbx.removeAllItems();

        String queryString = " SELECT cn.* ";
        queryString += " FROM  contabilidad_nomenclatura cn";
        queryString += " INNER JOIN empleado_liquidador el ON el.IdNomenclatura = cn.IdNomenclatura ";
        queryString += " WHERE cn.Estatus = 'HABILITADA'";
        queryString += " AND   el.IdEmpleado = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
        queryString += " AND   el.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND   el.IdNomenclatura = cn.IdNomenclatura";
        queryString += " ORDER BY cn.N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"),  rsRecords.getString("NoCuenta") + " (" +  rsRecords.getString("N5") + ")");
            }
            if(cuentaContableCbx.size() > 0) {
                cuentaContableCbx.select(cuentaContableCbx.getItemIds().iterator().next());
            }
        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            Notification.show("Error al leer cuentas contables.", Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }

    }

    private void insertarDocumento() {
        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor seleccione el proveedor.", Notification.Type.WARNING_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        if (cuentaContableCbx.getValue() == null) {
            Notification.show("Por favor seleccione la cuenta contable.", Notification.Type.WARNING_MESSAGE);
            cuentaContableCbx.focus();
            return;
        }
        if (centroCostoCbx.getValue() == null) {
            Notification.show("Por favor seleccione el centro de costo", Notification.Type.WARNING_MESSAGE);
            centroCostoCbx.focus();
            return;
        }
        if (this.numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese el número de documento.", Notification.Type.WARNING_MESSAGE);
            numeroTxt.focus();
            return;
        }
//        if (this.montoTxt.getDoubleValueDoNotThrow() == 0) {
//            Notification.show("Por favor ingrese el monto de la factura.", Notification.Type.WARNING_MESSAGE);
//            montoTxt.focus();
//            return;
//        }

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de registrar esta acción ?",
                "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            String queryString = " SELECT * FROM documento_liq_mobil";
                            queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                            queryString += " AND   IdProveedor = " + String.valueOf(proveedorCbx.getValue());
                            queryString += " AND   Numero = '" + numeroTxt.getValue().toUpperCase().trim() + "'";

                            try {
                                rsRecords = stQuery.executeQuery(queryString);

                                if (rsRecords.next()) {
                                    Notification.show("Este documento ya fué ingresado, revise.!.", Notification.Type.WARNING_MESSAGE);
                                    numeroTxt.focus();
                                    return;
                                }

                                queryString = "INSERT INTO documento_liq_mobil (IdEmpresa, IdProveedor, IdEmpleado, IdCentroCosto, ";
                                queryString += " CodigoCentroCosto, IdNomenclatura, Numero, Monto, CreadoUsuario, CreadoFechaYHora) ";
                                queryString += " VALUES (";
                                queryString += ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                                queryString += ","  + proveedorCbx.getValue();
                                queryString += ","  + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
                                queryString += ","  + centroCostoCbx.getValue();
                                queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
                                queryString += ","  + cuentaContableCbx.getValue();
                                queryString += ",'" + numeroTxt.getValue().toUpperCase().trim() + "'";
                                queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
                                queryString += ","  + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                                queryString += ",current_timestamp";
                                queryString += ")";

                                stQuery.executeUpdate(queryString);

//                                facturaRegistradaLiquidacionMobil(String.valueOf(proveedorCbx.getValue()), numeroTxt.getValue().toUpperCase().trim());

                                Notification.show("DOCUMENTO REGISTRADO OK!", Notification.Type.HUMANIZED_MESSAGE);

                                numeroTxt.setValue("");
//                                montoTxt.setValue(0.00);

                            } catch (Exception ex1) {
                                Notification.show("ERROR AL INSERTAR REGISTRO.", Notification.Type.ERROR_MESSAGE);
                                ex1.printStackTrace();
                            }
                        } else {
                            Notification.show("OPERACION CANCELADA POR USUARIO", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }
        );

    }

    private void facturaRegistradaLiquidacionMobil(String idProveedor, String numero) {

        String queryString = " SELECT Id ";
        queryString += " FROM documentos_fel_sat";
        queryString += " WHERE IdProveedor = " + idProveedor;
        queryString += " AND   Numero = '" + numero + "'";
        queryString += " AND   Contabilizada = 'N'";
        queryString += " AND   IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {

                queryString = "UPDATE documentos_fel_sat SET ";
                queryString += " Accion = 'Liquidación'";
                queryString += ",IdLiquidador = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
                queryString += ",IdCentroCosto = " + centroCostoCbx.getValue();
                queryString += ",IdNomenclatura = " + cuentaContableCbx.getValue();
                queryString += ",CodigoCentroCosto = '" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
                queryString += ",ModificadoUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                queryString += ",ModificadoFechaYHora = current_timestamp";
                queryString += " WHERE Id = " + rsRecords.getString("Id");

//                queryString += " WHERE IdProveedor = " + idProveedor;
//                queryString += " AND   Numero = '" + numero + "'";
//                queryString += " AND   IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

                stQuery.executeUpdate(queryString);

            }
        } catch (Exception ex1) {
            Logger.getLogger(this.getClass()).log(Level.SEVERE, "Error al actualizar facturas previamente registradas documentos_fel_sat.", ex1);
            ex1.printStackTrace();
        }

    }

    public void crearGridDocumentos() {

        VerticalLayout documentosLayout = new VerticalLayout();
        documentosLayout.setWidth("100%");
        documentosLayout.addStyleName("rcorners3");
        documentosLayout.setResponsive(true);

        documentosContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(FACTURA_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(MONTO_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(LIQUIDACION_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(CREADOSTAMP_PROPERTY, String.class, "");

        documentosGrid = new Grid("Liquidación actual ", documentosContainer);

        documentosGrid.setWidth("100%");
        documentosGrid.setImmediate(true);
        documentosGrid.setSelectionMode(Grid.SelectionMode.NONE);
        documentosGrid.setHeightMode(HeightMode.ROW);
        documentosGrid.setHeightByRows(5);
        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);

        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);

        documentosGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(2);
        documentosGrid.getColumn(FACTURA_PROPERTY).setExpandRatio(2);
        documentosGrid.getColumn(MONTO_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(LIQUIDACION_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(CREADOSTAMP_PROPERTY).setExpandRatio(1);

        documentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LIQUIDACION_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });

        footerliquidaciones = documentosGrid.appendFooterRow();
        footerliquidaciones.getCell(FACTURA_PROPERTY).setText("Total");
        footerliquidaciones.getCell(MONTO_PROPERTY).setText("0.00");
        footerliquidaciones.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        Grid.HeaderRow filterRow = documentosGrid.appendHeaderRow();

        Grid.HeaderCell cell0 = filterRow.getCell(FACTURA_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(FACTURA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(FACTURA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell0.setComponent(filterField0);
        Grid.HeaderCell cell2 = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(15);

        filterField2.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        documentosLayout.addComponent(documentosGrid);
        documentosLayout.setComponentAlignment(documentosGrid, Alignment.MIDDLE_CENTER);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidth("100%");
        buttonsLayout.setSpacing(true);

        documentosLayout.addComponent(buttonsLayout);
        documentosLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);

        cerrarBtn.setIcon(FontAwesome.CLOSE);
        cerrarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cerrarBtn.setDescription("Cerrar ésta liquidación");
        cerrarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CERRAR liquidación?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    String queryString = " UPDATE contabilidad_partida";
                                    queryString += " SET Estatus = 'CERRADO'";
                                    queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                                    queryString += " AND IdLiquidador = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
                                    queryString += " AND IdLiquidacion > 0";
                                    queryString += " AND Estatus IN ('INGRESADO', 'REVISADO')";

System.out.println("Query cerrrar liquidacion=" + queryString);

                                    try {
                                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                        stQuery.executeUpdate(queryString);

                                        Notification.show("Liquidación cerrada exitosamente.", Notification.Type.HUMANIZED_MESSAGE);
                                        llenarGridDocumentos();

                                    } catch (Exception ex) {
                                        System.out.println("Error al intentar CERRAR liquidación : " + ex.getMessage());
                                        Notification.show("Error al intentar CERRAR liquidación : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                                    }
                                }
                            }
                  });
            }
        });

        buttonsLayout.addComponent(cerrarBtn);
        buttonsLayout.setComponentAlignment(cerrarBtn, Alignment.MIDDLE_CENTER);

        addComponent(documentosLayout);

        setComponentAlignment(documentosLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarGridDocumentos() {

        documentosContainer.removeAllItems();
        footerliquidaciones.getCell(MONTO_PROPERTY).setText("0.00");

        String queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida ";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor() == null || ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor().isEmpty()) {
            Notification.show("El usuario no tiene un IdLiquidador asignado.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        queryString += " AND IdLiquidador = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
        queryString += " AND IdLiquidacion > 0";
        queryString += " AND Estatus IN ('INGRESADO', 'REVISADO')";
        queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
        queryString += " ORDER BY NombreProveedor";

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // si hay facturas compra por pagar

                documentosGrid.setCaption("Liquidación actual de " + rsRecords.getString("IdLiquidacion"));
                double totalMonto = 0.0;

                do {

                    Object itemId = documentosContainer.addItem();

                    documentosContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    documentosContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " +rsRecords.getString("NumeroDocumento"));
                    documentosContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Haber")));
                    documentosContainer.getContainerProperty(itemId, LIQUIDACION_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));
                    documentosContainer.getContainerProperty(itemId, CREADOSTAMP_PROPERTY).setValue(rsRecords.getString("CreadoFechaYHora"));
                    totalMonto += rsRecords.getDouble("MontoDocumento");

                } while (rsRecords.next());
                footerliquidaciones.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalMonto));
            }
            else {
                cerrarBtn.setEnabled(false);
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas de Liquidaciones : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Fact Liqui Mobil");
    }
}

