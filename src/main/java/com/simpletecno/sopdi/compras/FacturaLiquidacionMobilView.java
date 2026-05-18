package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
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
import java.text.SimpleDateFormat;
import java.util.logging.Level;

public class FacturaLiquidacionMobilView extends VerticalLayout implements View {

    static final String TIPO_DOCUMENTO = "Tipo doc.";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FACTURA_PROPERTY = "Documento";
    static final String MONTO_PROPERTY = "Monto";
    static final String LIQUIDACION_PROPERTY = "LIQUIDACION";
    static final String CREADOSTAMP_PROPERTY = "Creado el";
    static final String RAZON_PROPERTY = "Razón";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    private Statement stQuery;
    private ResultSet rsRecords;

    private final DateField fechaDt = new DateField();
    private final ComboBox tipoDocumentoCbx = new ComboBox();
    private final ComboBox proveedorCbx = new ComboBox();
    private final ComboBox cuentaContableCbx = new ComboBox();
    private final ComboBox centroCostoCbx = new ComboBox();
    private final TextField numeroTxt = new TextField();
    private final NumberField montoTxt = new NumberField();
    private final TextField razonTxt = new TextField();

    private final Button guardarBtn = new Button("Guardar");
    private final Button cerrarBtn = new Button("Cerrar Liquidación");

    private final IndexedContainer documentosContainer = new IndexedContainer();
    private Grid documentosGrid;
    Grid.FooterRow footerliquidaciones;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public FacturaLiquidacionMobilView() {

        setSpacing(false);
        setMargin(true);
        setHeightUndefined();

        HorizontalLayout empresaLayout = new HorizontalLayout();
        empresaLayout.setResponsive(true);
        empresaLayout.setSpacing(true);
        empresaLayout.setWidth("100%");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(false);
        titleLayout.setWidth("100%");

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " FACTURA LIQUIDACION MOBIL");
        titleLbl.addStyleName(ValoTheme.LABEL_H4);
        titleLbl.setWidth("100%");
        titleLbl.addStyleName("h2_custom");

        titleLayout.addComponent(titleLbl);

        addComponents(empresaLayout,titleLayout);
        setComponentAlignment(empresaLayout, Alignment.TOP_CENTER);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        HorizontalLayout fechaLayout = new HorizontalLayout();
        fechaLayout.setResponsive(true);
        fechaLayout.setSpacing(true);
        fechaLayout.setWidth("90%");

        fechaLayout.addComponent(fechaDt);
        fechaLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_CENTER);

        tipoDocumentoCbx.setTextInputAllowed(false);
        tipoDocumentoCbx.setNewItemsAllowed(false);
        tipoDocumentoCbx.setNullSelectionAllowed(false);
        tipoDocumentoCbx.setInputPrompt("Tipo de documento");
        tipoDocumentoCbx.setDescription("Tipo de documento");
        tipoDocumentoCbx.setWidth("100%");
        tipoDocumentoCbx.addItem("FACTURA");
        tipoDocumentoCbx.addItem("RECIBO CONTABLE");
        tipoDocumentoCbx.addItem("RECIBO CORRIENTE");
        tipoDocumentoCbx.select("FACTURA");
        tipoDocumentoCbx.setFilteringMode(FilteringMode.STARTSWITH);

        HorizontalLayout tipoDocumentoLayout = new HorizontalLayout();
        tipoDocumentoLayout.setResponsive(true);
        tipoDocumentoLayout.setSpacing(true);
        tipoDocumentoLayout.setWidth("90%");

        tipoDocumentoLayout.addComponent(tipoDocumentoCbx);
        tipoDocumentoLayout.setComponentAlignment(tipoDocumentoCbx, Alignment.MIDDLE_CENTER);

        proveedorCbx.setWidth("100%");
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.addContainerProperty("nit", String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(e -> {
            fillComboCuentaContable();
        });

        HorizontalLayout proveedorLayout = new HorizontalLayout();
        proveedorLayout.setResponsive(true);
        proveedorLayout.setSpacing(true);
        proveedorLayout.setWidth("90%");

        proveedorLayout.addComponent(proveedorCbx);
        proveedorLayout.setComponentAlignment(proveedorCbx, Alignment.MIDDLE_CENTER);

        llenarComboProveedor();

        cuentaContableCbx.setImmediate(true);
        cuentaContableCbx.setInputPrompt("Cuenta contable");
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
        centroCostoCbx.setInputPrompt("Centro costo");
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
        numeroTxt.setInputPrompt("Número");
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
        montoTxt.setDescription("Monto de la factura");
        montoTxt.setInputPrompt("Monto");
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

        razonTxt.setWidth("100%");
        razonTxt.setDescription("Razón de la factura");
        HorizontalLayout razonLayout = new HorizontalLayout();
        razonLayout.setResponsive(true);
        razonLayout.setSpacing(true);
        razonLayout.setWidth("90%");

        razonLayout.addComponent(razonTxt);
        razonLayout.setComponentAlignment(razonTxt, Alignment.MIDDLE_CENTER);

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
        addComponents(fechaLayout,tipoDocumentoLayout,proveedorLayout,cuentaContableLayout,centroCostoLayout,montoLayout,numeroLayout,razonLayout,botonLayout);
        setComponentAlignment(fechaLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(tipoDocumentoLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(proveedorLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(centroCostoLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(cuentaContableLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(numeroLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(montoLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(razonLayout, Alignment.MIDDLE_CENTER);

        crearGridDocumentos();
        llenarGridDocumentos();
    }

    public void llenarComboProveedor() {
        String queryString = " SELECT prv.IdProveedor, prv.Nombre, prv.NIT ";
        queryString += " FROM empleado_liquidador el";
        queryString += " INNER JOIN proveedor_empresa prv ON prv.IdProveedor = el.IdProveedor ";
        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor() == null || ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor().isEmpty()) {
            Notification.show("El usuario no tiene un IdLiquidador asignado.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        queryString += " WHERE el.IdEmpleado = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
        queryString += " AND   el.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND   prv.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY prv.Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                do {
                    proveedorCbx.addItem(rsRecords.getString("IdProveedor"));
                    proveedorCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre"));
                    proveedorCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "nit").setValue(rsRecords.getString("NIT"));
                } while (rsRecords.next());
            }
            proveedorCbx.addItem(((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor());
            proveedorCbx.setItemCaption(((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor(), ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName());
            proveedorCbx.getContainerProperty(((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor(), "nit").setValue("");

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
        queryString += " FROM  contabilidad_nomenclatura_empresa cn";
        queryString += " INNER JOIN empleado_liquidador el ON el.IdNomenclatura = cn.IdNomenclatura ";
        queryString += " WHERE cn.Estatus = 'HABILITADA'";
        queryString += " AND   el.IdEmpleado = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
        queryString += " AND   el.IdEmpresa = " + empresaId;
        queryString += " AND   cn.IdEmpresa = " + empresaId;
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
                            queryString += " AND   IdProveedor = " + proveedorCbx.getValue();
                            queryString += " AND   Numero = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
//                            queryString += " AND   TipoDocumento = '" + tipoDocumentoCbx.getValue().toString() + "'";

                            try {
                                rsRecords = stQuery.executeQuery(queryString);

                                if (rsRecords.next()) {
                                    Notification.show("Este documento ya fué ingresado, revise.!.", Notification.Type.WARNING_MESSAGE);
                                    numeroTxt.focus();
                                    return;
                                }
                            }
                            catch (Exception ex1) {
                                System.out.println("Error al insertar documento: " + ex1.getMessage());
                                Notification.show("Error al insertar documento.", Notification.Type.ERROR_MESSAGE);
                                ex1.printStackTrace();
                                return;
                            }

                            if(tipoDocumentoCbx.getValue().equals("FACTURA")) {
                                registrarLiquidacionMobil(false);
                            } else if(tipoDocumentoCbx.getValue().equals("RECIBO CONTABLE") || tipoDocumentoCbx.getValue().equals("RECIBO CORRIENTE")) {
                                registrarLiquidacionMobil(true);
                                registrarDirectoLiquidacion();
                            }
                        } else {
                            Notification.show("OPERACION CANCELADA POR USUARIO", Notification.Type.WARNING_MESSAGE);
                        }

                        limpiarCampos();
                    }
                }
        );

    }

    /*
        Solamente para documento tipo FACTURA.
        Hace el registro en la tabla documento_liq_movil, para que luego
        en ImportarFelSatView se haga el proceso general de documento compra.
     */
    private void registrarLiquidacionMobil(boolean noMensaje) {

        String queryString = "INSERT INTO documento_liq_mobil (IdEmpresa, IdProveedor, IdEmpleado, IdCentroCosto, ";
        queryString += " CodigoCentroCosto, IdNomenclatura, Numero, Monto, CreadoUsuario, CreadoFechaYHora, Contabilizado) ";
        queryString += " VALUES (";
        queryString += empresaId;
        queryString += ","  + proveedorCbx.getValue();
        queryString += ","  + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
        queryString += ","  + centroCostoCbx.getValue();
        queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
        queryString += ","  + cuentaContableCbx.getValue();
        queryString += ",'" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
        queryString += ","  + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        if(!noMensaje) {
            queryString += ",0";
        }
        else {
            queryString += ",1";
        }
        queryString += ")";

        try {
            stQuery.executeUpdate(queryString);

            if(!noMensaje) {
                Notification.show("DOCUMENTO REGISTRADO OK!", Notification.Type.HUMANIZED_MESSAGE);
                limpiarCampos();
            }

        } catch (Exception ex1) {
            Notification.show("ERROR AL INSERTAR REGISTRO.", Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }
    }

    /*
        Solamente para tipo de documento RECIBO CONTABLE y RECIBO CORRIENTE
        Registra directamente en la liquidación abierta del liquidador.
        Crea la partida contable del documento.
     */
    private void registrarDirectoLiquidacion() {

        String codigoCC;
        String nombreProveedor = proveedorCbx.getItemCaption(proveedorCbx.getValue());
        String nitProveedor = proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "nit").getValue().toString();
        String nombreLiquidador = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName();
        String idLiquidador = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
        int ultimaLiquidacion;

        String idCentroCosto = centroCostoCbx.getValue().toString();
        String codigoCentroCosto = centroCostoCbx.getItemCaption(centroCostoCbx.getValue());
        String idNomenclatura = cuentaContableCbx.getValue().toString();

        // encontrar la ultima liquidación abierta del liquidador
        String queryString = " SELECT CodigoCC, IdLiquidacion ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND IdLiquidador = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();;
        queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
        queryString += " AND IdLiquidacion > 0 ";
        queryString += " AND Estatus IN ('INGRESADO', 'REVISADO')";
        queryString += " AND EXTRACT(YEAR FROM Fecha) > 2024";
        queryString += " GROUP BY CodigoCC, IdLiquidacion";

        Logger.getLogger(this.getClass()).log(Level.INFO, "Liquidador=" + idLiquidador + " " + nombreLiquidador + " query=" + queryString);

        try {
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // encontrado
                codigoCC = rsRecords.getString("CodigoCC");
                ultimaLiquidacion = rsRecords.getInt("IdLiquidacion");
            } else { // no tiene liquidacion abierta, crear una nueva
                queryString = "SELECT *";
                queryString += " FROM  contabilidad_empresa";
                queryString += " WHERE IdEmpresa = " + empresaId;

                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {
                    ultimaLiquidacion = rsRecords.getInt("IdUltimaLiquidacion") + 1;
                } else {
                    ultimaLiquidacion = 1;
                }

                String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
                //0123456789
                //1234567890
                String ultimoEncontado; //yyyy-mm-dd
                String dia = fecha.substring(8, 10);
                String mes = fecha.substring(5, 7);
                String año = fecha.substring(0, 4);

                codigoCC = empresaId + año + mes + dia + "9";

                queryString = " SELECT codigoCC FROM contabilidad_partida ";
                queryString += " WHERE codigoCC LIKE '" + codigoCC + "%'";
                queryString += " ORDER BY codigoCC DESC ";
                queryString += " LIMIT 1";

                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado
                    ultimoEncontado = rsRecords.getString("codigoCC").substring(12, 15);
                    codigoCC += String.format("%03d", (Integer.parseInt(ultimoEncontado) + 1));
                } else {
                    codigoCC += "001";
                }
            }

            Logger.getLogger(this.getClass()).log(Level.INFO, "Liquidador=" + idLiquidador + " " + nombreLiquidador + " codigoCC=" + codigoCC + " liquidacionId=" + ultimaLiquidacion);

            //crear la partida contable para este documento
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
            //0123456789
            //1234567890
            String ultimoEncontado; //yyyy-mm-dd
            String dia = fecha.substring(8, 10);
            String mes = fecha.substring(5, 7);
            String año = fecha.substring(0, 4);

            String codigoPartida = empresaId + año + mes + dia + "2";

            queryString = " SELECT codigoPartida FROM contabilidad_partida ";
            queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
            queryString += " ORDER BY codigoPartida desc ";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);
                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));
            } else {
                codigoPartida += "001";
            }
            /// HABER ingreso del LIQUIDACION
            queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
            queryString += " TipoDocumento, Fecha, NITProveedor, IdProveedor, NombreProveedor,";
            queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber, ";
            queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, MontoDocumento, Saldo, IdLiquidador, IdLiquidacion, ";
            queryString += " Descripcion, IdCentroCosto, CodigoCentroCosto, CreadoUsuario, CreadoFechaYHora)";
            queryString += " VALUES ";
            queryString += " (";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoCC + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedor + "'";
            queryString += ", " + proveedorCbx.getValue();
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue().toUpperCase().trim() + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
            queryString += ",'QUETZALES'";
            queryString += ",0.00"; // DEBE
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //HABER
            queryString += ",0.00"; //DEBE Q
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",1";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // montodocumento
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // SALDO
            queryString += "," + idLiquidador;
            queryString += "," + ultimaLiquidacion;
            queryString += ",'LIQUIDACION GASTO " + ultimaLiquidacion + " " + nombreLiquidador +  " " + razonTxt.getValue() + "'";
            queryString += "," + idCentroCosto;
            queryString += ",'" + codigoCentroCosto + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            /// DEBE ingreso del costo
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoCC + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedor + "'";
            queryString += ", " + proveedorCbx.getValue();
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue().toUpperCase().trim() + "'";
            queryString += "," + cuentaContableCbx.getValue();
            queryString += ",'QUETZALES'";
            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA") && !tipoDocumentoCbx.getValue().equals("RECIBO CORRIENTE")) {
                queryString += "," + montoTxt.getDoubleValueDoNotThrow()/1.12; //DEBE
                queryString += ",0.00"; // HABER
                queryString += "," + montoTxt.getDoubleValueDoNotThrow()/1.12; //DEBE  Q
                queryString += ",0.00"; //HABER Q
            } else {
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //DEBE
                queryString += ",0.00"; // HABER
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //DEBE  Q
                queryString += ",0.00"; //HABER Q
            }
            queryString += ",1";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // montodocumento
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // SALDO
            queryString += "," + idLiquidador;
            queryString += "," + ultimaLiquidacion;
            queryString += ",'LIQUIDACION GASTO " + ultimaLiquidacion + " " + nombreLiquidador +  " " + razonTxt.getValue() + "'";
            queryString += "," + idCentroCosto;
            queryString += ",'" + codigoCentroCosto + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            /// DEBE ingreso del IVA
            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA") && !tipoDocumentoCbx.getValue().equals("RECIBO CORRIENTE")) {
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += ",'" + nitProveedor + "'";
                queryString += ", " + proveedorCbx.getValue();
                queryString += ",'" + nombreProveedor + "'";
                queryString += ",''";
                queryString += ",'" + numeroTxt.getValue().toUpperCase().trim() + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();
                queryString += ",'QUETZALES'";
                queryString += "," + ((montoTxt.getDoubleValueDoNotThrow()/1.12) * .12); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + ((montoTxt.getDoubleValueDoNotThrow()/1.12) * .12); //DEBE Q
                queryString += ",0.00"; //HABER Q
                queryString += ",1";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // montodocumento
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // SALDO
                queryString += "," + idLiquidador;
                queryString += "," + ultimaLiquidacion;
                queryString += ",'LIQUIDACION GASTO " + ultimaLiquidacion + " " + nombreLiquidador +  " " + razonTxt.getValue() + "'";
                queryString += "," + idCentroCosto;
                queryString += ",'" + codigoCentroCosto + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ")";
            }

            Logger.getLogger(this.getClass()).log(Level.INFO, "queryPartidaLiquidacionFelSatMobil=" + queryString);

            stQuery.executeUpdate(queryString);

            queryString = "UPDATE contabilidad_empresa SET";
            queryString += " IdUltimaLiquidacion = " + ultimaLiquidacion;
            queryString += " WHERE IdEmpresa = " + empresaId;

            Logger.getLogger(this.getClass()).log(Level.INFO, "queryUpdateUltimaLiquidacion=" + queryString);

            stQuery.executeUpdate(queryString);

            Notification.show("DOCUMENTO REGISTRADO OK!", Notification.Type.HUMANIZED_MESSAGE);
            llenarGridDocumentos();

        } catch(Exception exception) {
            Logger.getLogger(this.getClass()).log(Level.SEVERE, "Error al actualizar facturas previamente registradas documentos_fel_sat.", exception);
            Notification.show("Error al actualizar facturas previamente registradas documentos_fel_sat", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void facturaRegistradaLiquidacionMobil(String idProveedor, String numero) {

        String queryString = " SELECT Id ";
        queryString += " FROM documentos_fel_sat";
        queryString += " WHERE IdProveedor = " + idProveedor;
        queryString += " AND   Numero = '" + numero + "'";
        queryString += " AND   Contabilizada = 'N'";
        queryString += " AND   IdEmpresa = " + empresaId;

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
            Notification.show("Error al actualizar facturas previamente registradas documentos_fel_sat", Notification.Type.ERROR_MESSAGE);
        }

    }

    public void crearGridDocumentos() {

        VerticalLayout documentosLayout = new VerticalLayout();
        documentosLayout.setWidth("100%");
        documentosLayout.setHeight("100%");
        documentosLayout.addStyleName("rcorners3");
        documentosLayout.setResponsive(true);
        documentosLayout.setMargin(true);
        documentosLayout.setSpacing(true);

        documentosContainer.addContainerProperty(TIPO_DOCUMENTO, String.class, "");
        documentosContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(FACTURA_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(MONTO_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(LIQUIDACION_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(RAZON_PROPERTY, String.class, "");
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

                documentosGrid.setCaption("Liquidación " + rsRecords.getString("IdLiquidacion") + " de " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor() + " " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() );
                double totalMonto = 0.0;

                do {

                    Object itemId = documentosContainer.addItem();

                    documentosContainer.getContainerProperty(itemId, TIPO_DOCUMENTO).setValue(rsRecords.getString("TipoDocumento"));
                    documentosContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    documentosContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " +rsRecords.getString("NumeroDocumento"));
                    documentosContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Haber")));
                    documentosContainer.getContainerProperty(itemId, LIQUIDACION_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));
                    documentosContainer.getContainerProperty(itemId, CREADOSTAMP_PROPERTY).setValue(rsRecords.getString("CreadoFechaYHora"));
                    documentosContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Descripcion"));
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
            Notification.show("Error al listar tabla Facturas de Liquidaciones : " + ex.getMessage());
        }
    }

    private void limpiarCampos() {
        tipoDocumentoCbx.select("FACTURA");
        proveedorCbx.select("0");
        cuentaContableCbx.select("0");
        centroCostoCbx.select("NO APLICA");
        numeroTxt.setValue("");
        montoTxt.setValue(0.0);
        razonTxt.setValue("");
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Fact Liqui Mobil");
    }
}

