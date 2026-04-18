package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Formulario de mantenimiento para Cuentas de Banco y sus Chequeras.
 *
 * tipoTransaccion "0" = Nueva cuenta
 * tipoTransaccion "1" = Editar cuenta existente (habilita la pestaña Chequeras)
 *
 * Tabla principal : contabilidad_cuentas_bancos
 * Tabla chequeras : contabilidad_cuentas_bancos_chequera
 *   Campos: IdChequera, IdCuentaBanco, IdEmpresa, Del, Al, UltimoUtilizado
 */
public class CuentasBancosForm extends Window {

    // ── Cuenta banco ────────────────────────────────────────────────────────
    static final String NOCUENTA_PROPERTY = "";

    // ── Chequeras ────────────────────────────────────────────────────────────
    static final String ID_CHEQUERA_PROPERTY    = "IdChequera";
    static final String DEL_PROPERTY            = "Del (Desde)";
    static final String AL_PROPERTY             = "Al (Hasta)";
    static final String ULTIMO_PROPERTY         = "Último utilizado";
    static final String ESTATUS_CHEQUERA_PROP   = "Estatus";

    String idCuentaBancoEdit;
    String tipoTransaccion;
    String idChequeraEdit = "";
    boolean editandoChequera = false;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;
    String queryString = "";

    String empresaId   = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    // ── Tab 1: Cuenta Banco ──────────────────────────────────────────────────
    VerticalLayout mainForm;
    ComboBox cuentaContableCbx;
    ComboBox monedaCbx;
    TextField noCuentaTxt;
    ComboBox proveedorCbx;
    NumberField saltoTxt;
    Button salirBtn;

    // ── Tab 2: Chequeras ─────────────────────────────────────────────────────
    IndexedContainer chequeraContainer = new IndexedContainer();
    Grid chequeraGrid;
    Button nuevaChequeraBtn;
    Button editarChequeraBtn;
    Button eliminarChequeraBtn;
    VerticalLayout chequeraFormPanel;
    TextField delTxt;
    TextField alTxt;
    TextField ultimoTxt;
    Button guardarChequeraBtn;
    Button cancelarChequeraBtn;

    // ─────────────────────────────────────────────────────────────────────────

    public CuentasBancosForm(String tipoTransaccion, String idCuentaBancoEdit) {
        this.mainUI = UI.getCurrent();
        this.tipoTransaccion = tipoTransaccion;
        this.idCuentaBancoEdit = idCuentaBancoEdit;

        setWidth("72%");
        setHeight("85%");
        setResponsive(true);
        setModal(true);

        mainForm = new VerticalLayout();
        mainForm.setWidth("100%");
        mainForm.setSizeFull();
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        // ── Título ───────────────────────────────────────────────────────────
        Label titleLbl = new Label(empresaId + "  " + empresaNombre + "  –  CUENTA DE BANCO");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout(titleLbl);
        titleLayout.setWidth("100%");
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);
        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        // ── TabSheet ─────────────────────────────────────────────────────────
        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

        tabs.addTab(buildCuentaBancoTab(), "Cuenta Banco", FontAwesome.BANK);

        if (tipoTransaccion.equals("1")) {
            llenarDatos();
            tabs.addTab(buildChequerasTab(), "Chequeras", FontAwesome.BOOK);
        }

        mainForm.addComponent(tabs);
        mainForm.setExpandRatio(tabs, 1f);

        // ── Botón Salir (único en el panel principal) ────────────────────────
        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(e -> close());

        mainForm.addComponent(salirBtn);
        mainForm.setComponentAlignment(salirBtn, Alignment.BOTTOM_RIGHT);

        setContent(mainForm);
    }

    // =========================================================================
    //  Tab 1 – Cuenta Banco
    // =========================================================================

    private VerticalLayout buildCuentaBancoTab() {
        VerticalLayout tab = new VerticalLayout();
        tab.setMargin(true);
        tab.setSpacing(true);

        FormLayout form = new FormLayout();
        form.setSpacing(true);
        form.setMargin(true);
        form.setWidth("90%");
        form.setResponsive(true);

        cuentaContableCbx = new ComboBox("Cuenta contable");
        cuentaContableCbx.setIcon(FontAwesome.MONEY);
        cuentaContableCbx.setWidth("75%");
        cuentaContableCbx.addContainerProperty(NOCUENTA_PROPERTY, String.class, "");
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.setNullSelectionAllowed(false);
        form.addComponent(cuentaContableCbx);

        monedaCbx = new ComboBox("Moneda");
        monedaCbx.setIcon(FontAwesome.BALANCE_SCALE);
        monedaCbx.setWidth("75%");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
        form.addComponent(monedaCbx);

        noCuentaTxt = new TextField("Número de cuenta bancaria");
        noCuentaTxt.setIcon(FontAwesome.BANK);
        noCuentaTxt.setWidth("75%");
        form.addComponent(noCuentaTxt);

        proveedorCbx = new ComboBox("Banco");
        proveedorCbx.setIcon(FontAwesome.BANK);
        proveedorCbx.setWidth("75%");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setNullSelectionAllowed(false);
        form.addComponent(proveedorCbx);

        saltoTxt = new NumberField("Saldo bancario actual");
        saltoTxt.setValidationVisible(false);
        saltoTxt.setDecimalAllowed(true);
        saltoTxt.setDecimalPrecision(2);
        saltoTxt.setMinimumFractionDigits(2);
        saltoTxt.setDecimalSeparator('.');
        saltoTxt.setDecimalSeparatorAlwaysShown(true);
        saltoTxt.setValue(0d);
        saltoTxt.setGroupingUsed(true);
        saltoTxt.setGroupingSeparator(',');
        saltoTxt.setGroupingSize(3);
        saltoTxt.setImmediate(true);
        saltoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        saltoTxt.setWidth("8em");
        form.addComponent(saltoTxt);

        llenarComboNomenclatura();
        llenarComboProveedor();

        tab.addComponent(form);

        // ── Botón Guardar cuenta (exclusivo de este tab) ─────────────────────
        Button guardarBtn = new Button("Guardar cuenta");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(e -> insertarCuentasBancos());

        tab.addComponent(guardarBtn);
        tab.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        return tab;
    }

    // =========================================================================
    //  Tab 2 – Chequeras
    // =========================================================================

    private Panel buildChequerasTab() {

        // ── Formulario inline (se construye primero para declarar los campos) ─
        chequeraFormPanel = new VerticalLayout();
        chequeraFormPanel.setVisible(false);
        chequeraFormPanel.setWidth("100%");
        chequeraFormPanel.setSpacing(true);
        chequeraFormPanel.setMargin(new MarginInfo(false, false, true, false));
        chequeraFormPanel.addStyleName(ValoTheme.LAYOUT_CARD);

        Label chequeraFormTitle = new Label("Datos de la chequera");
        chequeraFormTitle.addStyleName(ValoTheme.LABEL_H3);
        chequeraFormPanel.addComponent(chequeraFormTitle);

        HorizontalLayout chequeraFieldsRow = new HorizontalLayout();
        chequeraFieldsRow.setSpacing(true);
        chequeraFieldsRow.setWidth("100%");

        delTxt = new TextField("Del — No. cheque inicial");
        delTxt.setIcon(FontAwesome.HASHTAG);
        delTxt.setWidth("170px");
        delTxt.setRequired(true);

        alTxt = new TextField("Al — No. cheque final");
        alTxt.setIcon(FontAwesome.HASHTAG);
        alTxt.setWidth("170px");
        alTxt.setRequired(true);

        ultimoTxt = new TextField("Último utilizado");
        ultimoTxt.setIcon(FontAwesome.CHECK_SQUARE_O);
        ultimoTxt.setWidth("170px");
        ultimoTxt.setDescription("Se inicializa con el valor de 'Del' al crear.");

        chequeraFieldsRow.addComponents(delTxt, alTxt, ultimoTxt);
        chequeraFormPanel.addComponent(chequeraFieldsRow);

        guardarChequeraBtn  = new Button("Guardar chequera", FontAwesome.SAVE);
        guardarChequeraBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        cancelarChequeraBtn = new Button("Cancelar", FontAwesome.TIMES);
        cancelarChequeraBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);

        guardarChequeraBtn.addClickListener(e -> guardarChequera());
        cancelarChequeraBtn.addClickListener(e -> {
            chequeraFormPanel.setVisible(false);
            limpiarFormChequera();
        });

        HorizontalLayout chequeraFormButtons = new HorizontalLayout(cancelarChequeraBtn, guardarChequeraBtn);
        chequeraFormButtons.setSpacing(true);
        chequeraFormPanel.addComponent(chequeraFormButtons);

        // ── Grid de chequeras ────────────────────────────────────────────────
        chequeraContainer.addContainerProperty(ID_CHEQUERA_PROPERTY,  String.class, null);
        chequeraContainer.addContainerProperty(DEL_PROPERTY,          String.class, null);
        chequeraContainer.addContainerProperty(AL_PROPERTY,           String.class, null);
        chequeraContainer.addContainerProperty(ULTIMO_PROPERTY,       String.class, null);
        chequeraContainer.addContainerProperty(ESTATUS_CHEQUERA_PROP, String.class, null);

        chequeraGrid = new Grid("Chequeras registradas", chequeraContainer);
        chequeraGrid.setWidth("100%");
        chequeraGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        chequeraGrid.setHeightMode(HeightMode.ROW);
        chequeraGrid.setHeightByRows(5);
        chequeraGrid.setResponsive(true);
        chequeraGrid.setImmediate(true);

        chequeraGrid.getColumn(ID_CHEQUERA_PROPERTY).setHidable(true).setHidden(true);
        chequeraGrid.getColumn(DEL_PROPERTY).setExpandRatio(2);
        chequeraGrid.getColumn(AL_PROPERTY).setExpandRatio(2);
        chequeraGrid.getColumn(ULTIMO_PROPERTY).setExpandRatio(2);
        chequeraGrid.getColumn(ESTATUS_CHEQUERA_PROP).setExpandRatio(2);

        // ── Barra de botones de la grilla ────────────────────────────────────
        nuevaChequeraBtn    = new Button("Nueva",    FontAwesome.PLUS);
        editarChequeraBtn   = new Button("Editar",   FontAwesome.EDIT);
        eliminarChequeraBtn = new Button("Eliminar", FontAwesome.TRASH);
        eliminarChequeraBtn.addStyleName(ValoTheme.BUTTON_DANGER);

        nuevaChequeraBtn.addClickListener(e -> {
            editandoChequera = false;
            idChequeraEdit = "";
            limpiarFormChequera();
            chequeraFormPanel.setVisible(true);
        });

        editarChequeraBtn.addClickListener(e -> {
            if (chequeraGrid.getSelectedRow() == null) {
                Notification.show("Seleccione una chequera para editar.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            idChequeraEdit = String.valueOf(
                chequeraContainer.getContainerProperty(chequeraGrid.getSelectedRow(), ID_CHEQUERA_PROPERTY).getValue());
            delTxt.setValue(nvl(String.valueOf(chequeraContainer.getContainerProperty(chequeraGrid.getSelectedRow(), DEL_PROPERTY).getValue())));
            alTxt.setValue(nvl(String.valueOf(chequeraContainer.getContainerProperty(chequeraGrid.getSelectedRow(), AL_PROPERTY).getValue())));
            ultimoTxt.setValue(nvl(String.valueOf(chequeraContainer.getContainerProperty(chequeraGrid.getSelectedRow(), ULTIMO_PROPERTY).getValue())));
            editandoChequera = true;
            chequeraFormPanel.setVisible(true);
        });

        eliminarChequeraBtn.addClickListener(e -> eliminarChequera());

        HorizontalLayout chequeraActionBar = new HorizontalLayout(nuevaChequeraBtn, editarChequeraBtn, eliminarChequeraBtn);
        chequeraActionBar.setSpacing(true);

        // ── Layout del tab: form (top) → grid → botones ──────────────────────
        VerticalLayout tab = new VerticalLayout();
        tab.setWidth("100%");
        tab.setMargin(true);
        tab.setSpacing(true);
        tab.addComponent(chequeraFormPanel);   // ← formulario al tope, siempre visible
        tab.addComponent(chequeraGrid);
        tab.addComponent(chequeraActionBar);

        // Panel con scroll: si el contenido supera el alto disponible, se desplaza
        // sin afectar los botones de la ventana exterior.
        Panel scrollPanel = new Panel();
        scrollPanel.setSizeFull();
        scrollPanel.setContent(tab);

        llenarGridChequeras();
        return scrollPanel;
    }

    // =========================================================================
    //  Operaciones CRUD – Cuenta Banco
    // =========================================================================

    public void llenarComboProveedor() {
        queryString  = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE EsBanco = 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre";

        try {
            stQuery   = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                proveedorCbx.addItem(rsRecords.getString("IdProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre"));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar proveedores de tipo banco: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void llenarComboNomenclatura() {
        queryString  = " SELECT * FROM contabilidad_nomenclatura_empresa ";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " AND IdEmpresa = '" + empresaId + "'";
        queryString += " ORDER BY N5";

        try {
            stQuery   = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("N5"));
                cuentaContableCbx.getItem(rsRecords.getString("IdNomenclatura"))
                    .getItemProperty(NOCUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar cuentas contables: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void llenarDatos() {
        queryString  = " SELECT * FROM contabilidad_cuentas_bancos ";
        queryString += " WHERE IdCuentaBanco = " + idCuentaBancoEdit;

        try {
            stQuery   = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                cuentaContableCbx.select(rsRecords.getString("IdNomenclatura"));
                monedaCbx.select(rsRecords.getString("Moneda"));
                noCuentaTxt.setValue(rsRecords.getString("NoCuenta"));
                proveedorCbx.select(rsRecords.getString("IdProveedor"));
                saltoTxt.setValue(rsRecords.getDouble("Saldo"));
            }
        } catch (Exception ex) {
            System.out.println("Error al llenar registro de contabilidad_cuentas_banco: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void insertarCuentasBancos() {
        try {
            if (cuentaContableCbx.getValue() == null) {
                Notification.show("Por favor selecciona una cuenta contable.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            if (monedaCbx.getValue() == null) {
                Notification.show("Por favor seleccione un tipo de moneda.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            if (proveedorCbx.getValue() == null) {
                Notification.show("Por favor seleccione un banco.", Notification.Type.WARNING_MESSAGE);
                return;
            }

            if (tipoTransaccion.equals("0")) {
                queryString  = "INSERT INTO contabilidad_cuentas_bancos ";
                queryString += "(IdEmpresa, IdNomenclatura, IdProveedor, NoCuenta, Moneda, Saldo)";
                queryString += " VALUES ";
                queryString += "(" + empresaId;
                queryString += ", " + cuentaContableCbx.getValue();
                queryString += ", " + proveedorCbx.getValue();
                queryString += ", '" + noCuentaTxt.getValue() + "'";
                queryString += ", '" + monedaCbx.getValue() + "'";
                queryString += ", " + saltoTxt.getValue();
                queryString += ")";
            } else {
                queryString  = "UPDATE contabilidad_cuentas_bancos SET ";
                queryString += "  IdNomenclatura = " + cuentaContableCbx.getValue();
                queryString += ", IdProveedor = " + proveedorCbx.getValue();
                queryString += ", NoCuenta = '" + noCuentaTxt.getValue() + "'";
                queryString += ", Moneda = '" + monedaCbx.getValue() + "'";
                queryString += ", Saldo = " + saltoTxt.getValue();
                queryString += " WHERE IdCuentaBanco = " + idCuentaBancoEdit;
            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((CuentasContablesBancosView) (mainUI.getNavigator().getCurrentView())).llenarTablaCuentas();
            ((CuentasContablesBancosView) (mainUI.getNavigator().getCurrentView())).cuentasGrid.select(null);

            Notification notif = new Notification("Cuenta guardada correctamente.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            close();

        } catch (SQLException ex) {
            Notification notif = new Notification("Error de base de datos: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(2500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error al guardar cuenta banco: " + ex);
            ex.printStackTrace();
        }
    }

    // =========================================================================
    //  Operaciones CRUD – Chequeras
    // =========================================================================

    public void llenarGridChequeras() {
        chequeraContainer.removeAllItems();

        queryString  = " SELECT * FROM contabilidad_cuentas_bancos_chequera ";
        queryString += " WHERE IdCuentaBanco = " + idCuentaBancoEdit;
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Del";

        try {
            stQuery   = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                Object itemId = chequeraContainer.addItem();

                String del           = nvl(rsRecords.getString("Del"));
                String al            = nvl(rsRecords.getString("Al"));
                String ultimoUsado   = nvl(rsRecords.getString("UltimoUtilizado"));

                chequeraContainer.getContainerProperty(itemId, ID_CHEQUERA_PROPERTY).setValue(rsRecords.getString("IdChequera"));
                chequeraContainer.getContainerProperty(itemId, DEL_PROPERTY).setValue(del);
                chequeraContainer.getContainerProperty(itemId, AL_PROPERTY).setValue(al);
                chequeraContainer.getContainerProperty(itemId, ULTIMO_PROPERTY).setValue(ultimoUsado);
                chequeraContainer.getContainerProperty(itemId, ESTATUS_CHEQUERA_PROP).setValue(calcularEstatus(del, al, ultimoUsado));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar chequeras: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void guardarChequera() {
        String del = delTxt.getValue() == null ? "" : delTxt.getValue().trim();
        String al  = alTxt.getValue()  == null ? "" : alTxt.getValue().trim();

        if (del.isEmpty()) {
            Notification.show("Ingrese el número inicial (Del) de la chequera.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (al.isEmpty()) {
            Notification.show("Ingrese el número final (Al) de la chequera.", Notification.Type.WARNING_MESSAGE);
            return;
        }

        String ultimoUsado = (ultimoTxt.getValue() == null || ultimoTxt.getValue().trim().isEmpty())
                ? del
                : ultimoTxt.getValue().trim();

        try {
            if (editandoChequera && !idChequeraEdit.isEmpty()) {
                queryString  = "UPDATE contabilidad_cuentas_bancos_chequera SET ";
                queryString += "  Del = " + del;
                queryString += ", Al = " + al;
                queryString += ", UltimoUtilizado = " + ultimoUsado;
                queryString += " WHERE IdChequera = " + idChequeraEdit;
            } else {
                queryString  = "INSERT INTO contabilidad_cuentas_bancos_chequera";
                queryString += " (IdCuentaBanco, IdEmpresa, Del, Al, UltimoUtilizado)";
                queryString += " VALUES (" + idCuentaBancoEdit;
                queryString += ", " + empresaId;
                queryString += ", " + del;
                queryString += ", " + al;
                queryString += ", " + ultimoUsado + ")";
            }

Logger.getLogger(CuentasBancosForm.class.getName()).log(Level.INFO, "queryString: {0}", queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            llenarGridChequeras();
            chequeraFormPanel.setVisible(false);
            limpiarFormChequera();
            chequeraGrid.select(null);

            Notification notif = new Notification(
                    editandoChequera ? "Chequera actualizada." : "Chequera registrada.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

        } catch (SQLException ex) {
            Notification.show("Error al guardar la chequera: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            System.out.println("Error al guardar chequera: " + ex);
            ex.printStackTrace();
        }
    }

    private void eliminarChequera() {
        if (chequeraGrid.getSelectedRow() == null) {
            Notification.show("Seleccione una chequera para eliminar.", Notification.Type.WARNING_MESSAGE);
            return;
        }

        String idChequera = String.valueOf(
            chequeraContainer.getContainerProperty(chequeraGrid.getSelectedRow(), ID_CHEQUERA_PROPERTY).getValue());

        try {
            queryString  = "DELETE FROM contabilidad_cuentas_bancos_chequera ";
            queryString += " WHERE IdChequera = " + idChequera;
            queryString += " AND IdEmpresa = " + empresaId;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            llenarGridChequeras();
            chequeraGrid.select(null);
            chequeraFormPanel.setVisible(false);

            Notification notif = new Notification("Chequera eliminada.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.TRASH);
            notif.show(Page.getCurrent());

        } catch (SQLException ex) {
            Notification.show("Error al eliminar la chequera: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            System.out.println("Error al eliminar chequera: " + ex);
            ex.printStackTrace();
        }
    }

    private void limpiarFormChequera() {
        delTxt.setValue("");
        alTxt.setValue("");
        ultimoTxt.setValue("");
        idChequeraEdit = "";
        editandoChequera = false;
    }

    // =========================================================================
    //  Utilidades
    // =========================================================================

    /** Evita NullPointerException al mostrar valores de base de datos. */
    private String nvl(String value) {
        return value == null ? "" : value;
    }

    /**
     * Calcula el estatus visual de una chequera:
     *   AGOTADA  — el último cheque utilizado alcanzó el límite de la chequera.
     *   EN USO   — tiene cheques utilizados pero aún le quedan disponibles.
     *   NUEVA    — ningún cheque ha sido utilizado (UltimoUtilizado == Del).
     */
    private String calcularEstatus(String del, String al, String ultimoUtilizado) {
        try {
            long nDel   = Long.parseLong(del.replaceAll("[^0-9]", ""));
            long nAl    = Long.parseLong(al.replaceAll("[^0-9]", ""));
            long nUltimo = Long.parseLong(ultimoUtilizado.replaceAll("[^0-9]", ""));

            if (nUltimo >= nAl) return "AGOTADA";
            if (nUltimo <= nDel) return "NUEVA";
            return "EN USO";
        } catch (NumberFormatException ex) {
            return "";
        }
    }
}
