package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;

import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class AutorizarVentaMonedaForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String GRUPO_PROPERTY = "GRUPO";
    static final String NOMBRESINCODIGO_PROPERTY = "NombreSinCodigo";

    VerticalLayout mainLayout;

    NumberField montoAutorizarTxt;

    ComboBox empresaCbx;
    ComboBox monedaCbx;
    ComboBox proveedorCbx;

    Button salirBtn;
    Button autorizarBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public AutorizarVentaMonedaForm() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            setWidth("80%");
            setHeight("30%");
        }
        else {
            setWidth("90%");
            setHeight("60%");
        }

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(new MarginInfo(false, true, false, true));
        mainLayout.setResponsive(true);
        mainLayout.setWidth("100%");

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setWidth("100%");
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true, false, true));
        layoutTitle.setSizeUndefined();
        layoutTitle.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("100%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        Label titleLbl = new Label("AUTORIZAR VENTA DE MONEDA");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            titleLbl.addStyleName(ValoTheme.LABEL_H2);
        }
        else {
            titleLbl.addStyleName(ValoTheme.LABEL_H4);
        }
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setMargin(new MarginInfo(false, true, false, true));
        filtrosLayout.setSpacing(true);
        filtrosLayout.setWidth("100%");
//        filtrosLayout.setSizeUndefined();
        filtrosLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setWidth("100%");
        monedaCbx.addItem("DOLARES");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.select("DOLARES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);

        montoAutorizarTxt = new NumberField("Monto : ");
        montoAutorizarTxt.setDecimalAllowed(true);
        montoAutorizarTxt.setDecimalPrecision(2);
        montoAutorizarTxt.setMinimumFractionDigits(2);
        montoAutorizarTxt.setDecimalSeparator('.');
        montoAutorizarTxt.setDecimalSeparatorAlwaysShown(true);
        montoAutorizarTxt.setValue(0d);
        montoAutorizarTxt.setGroupingUsed(true);
        montoAutorizarTxt.setGroupingSeparator(',');
        montoAutorizarTxt.setGroupingSize(3);
        montoAutorizarTxt.setImmediate(true);
        montoAutorizarTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
//        montoAutorizarTxt.setWidth("10em");
//        if (mainUI.getPage().getBrowserWindowWidth() < 736) {
            montoAutorizarTxt.setWidth("100%");
//        }

        proveedorCbx = new ComboBox("Proveedor: ");
        proveedorCbx.setWidth("100%");
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.addContainerProperty(GRUPO_PROPERTY, String.class, "");
        proveedorCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
//            verificarProveedor();
        });
        llenarComboProveedor();

        autorizarBtn = new Button("Autorizar");
        autorizarBtn.setIcon(FontAwesome.SAVE);
//        autorizarBtn.setWidth("60%");
//        autorizarBtn.setHeight("80%");
        autorizarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertRegistro();
            }
        });

        salirBtn = new Button("Salir");
//        salirBtn.setWidth("60%");
//        salirBtn.setHeight("80%");
        salirBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        layoutTitle.addComponents(empresaCbx, titleLbl);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

//        filtrosLayout.addComponents(monedaCbx, montoAutorizarTxt, proveedorCbx, autorizarBtn,salirBtn);
        filtrosLayout.addComponents(monedaCbx, montoAutorizarTxt, proveedorCbx, autorizarBtn);
        filtrosLayout.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_RIGHT);
//        filtrosLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);

        filtrosLayout.setExpandRatio(monedaCbx, 1.0f);
        filtrosLayout.setExpandRatio(montoAutorizarTxt, 1.0f);
        filtrosLayout.setExpandRatio(proveedorCbx, 3.0f);
        filtrosLayout.setExpandRatio(autorizarBtn, 1.0f);

        mainLayout.addComponent(layoutTitle);
        mainLayout.addComponent(filtrosLayout);
        mainLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_CENTER);

        setContent(mainLayout);

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }
            rsRecords.first();

            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboProveedor() {
        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsPlanilla = 0";
        queryString += " Order By Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {

                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(GRUPO_PROPERTY).setValue(rsRecords.getString("GRUPO"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(rsRecords.getString("Nombre"));

                if(rsRecords.getInt("EsBanco") == 1) {
                    if(rsRecords.getString("Nombre").contains("Industrial")) {
                        proveedorCbx.select(rsRecords.getString("IDProveedor"));
                    }
                }
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertRegistro() {

        if (monedaCbx.getValue() == null) {
            Notification.show("Por favor, Seleccione una moneda..", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            return;
        }

        if (montoAutorizarTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, Ingrese un monto..", Notification.Type.ERROR_MESSAGE);
            montoAutorizarTxt.focus();
            return;
        }

        queryString = "  Insert Into autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
        queryString += " Fecha, Moneda, Monto, CodigoCC, CuentaContableLiquidar, ";
        queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += "(";
        queryString += "'" + AutorizacionesPagoView.VENTA_MONEDA + "'";
        queryString += "," + String.valueOf(empresaCbx.getValue());
        queryString += "," + proveedorCbx.getValue();
        queryString += ",current_date";
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoAutorizarTxt.getValue();
        queryString += ",'" + "TEMP_" + new java.util.Date().getTime() + "'";
        queryString += ",''"; // cuentacontableliquidar
        queryString += ",'" + AutorizacionesPagoView.VENTA_MONEDA + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification notif = new Notification("AUTORIZACION EXITOSA",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            close();
        } catch (Exception ex1) {
            System.out.println("Error al insertar en la tabla autorizacion_pago : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }
}
