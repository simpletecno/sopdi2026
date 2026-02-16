package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author user
 */
public class CuentasContablesDefaultView extends VerticalLayout implements View {
    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;

    VerticalLayout mainLayout;
    TabSheet tabSheet;
    TabSheet.Tab nomenclaturaTab;
    TabSheet.Tab cuentasDefaultTab;
    TwinColSelect miNomenclaturaSelect;

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;
    ComboBox cuentaContable6Cbx;
    ComboBox cuentaContable7Cbx;
    ComboBox cuentaContable8Cbx;
    ComboBox cuentaContable9Cbx;
    ComboBox cuentaContable10Cbx;
    ComboBox cuentaContable11Cbx;
    ComboBox cuentaContable12Cbx;
    ComboBox cuentaContable13Cbx;
    ComboBox cuentaContable14Cbx;
    ComboBox cuentaContable15Cbx;
    ComboBox cuentaContable16Cbx;
    ComboBox cuentaContable17Cbx;
    ComboBox cuentaContable18Cbx;
    ComboBox cuentaContable19Cbx;
    ComboBox cuentaContable20Cbx;
    ComboBox cuentaContable21Cbx;
    ComboBox cuentaContable22Cbx;
    ComboBox cuentaContable23Cbx;
    ComboBox cuentaContable24Cbx;
    ComboBox cuentaContable25Cbx;
    ComboBox cuentaContable26Cbx;
    ComboBox cuentaContable27Cbx;
    ComboBox cuentaContable28Cbx;
    ComboBox cuentaContable29Cbx;
    ComboBox cuentaContable30Cbx;
    ComboBox cuentaContable31Cbx;
    ComboBox cuentaContable32Cbx;
    ComboBox cuentaContable33Cbx;
    ComboBox cuentaContable34Cbx;
    ComboBox cuentaContable35Cbx;
    ComboBox cuentaContable36Cbx;
    ComboBox cuentaContable37Cbx;
    ComboBox cuentaContable38Cbx;
    ComboBox cuentaContable39Cbx;
    ComboBox cuentaContable40Cbx;
    ComboBox cuentaContable41Cbx;
    ComboBox cuentaContable42Cbx;
    ComboBox cuentaContable43Cbx;
    ComboBox cuentaContable44Cbx;

    public CuentasContablesDefaultView() {

        Responsive.makeResponsive(this);
        this.mainUI = UI.getCurrent();

        MarginInfo marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(marginInfo);
        mainLayout.setSpacing(true);
        mainLayout.addStyleName("rcorners3");
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");

        addComponent(mainLayout);

        Label titleLbl = new Label("Cuentas contables default para : " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + " "+ ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName());
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTabSheet();
//        createTablaCuentasContables();
//        createButtons();
    }

    private void crearTabSheet() {
        tabSheet = new TabSheet();
        tabSheet.addStyleName("rcorners3");
        tabSheet.addStyleName("h2");
        tabSheet.setSizeFull();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        tabSheet.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

        addTabNomcenclatura();
        addTabCuentasDefault();

        mainLayout.addComponent(tabSheet);
        mainLayout.setComponentAlignment(tabSheet, Alignment.TOP_CENTER);
    }

    private void addTabNomcenclatura() {

        nomenclaturaTab = tabSheet.addTab(createNomenclaturaSeelect(), "Mi Nomenclatura");
        nomenclaturaTab.setIcon(FontAwesome.FLAG_CHECKERED);
        nomenclaturaTab.setId("1");
        nomenclaturaTab.setStyleName("dirtyTabCaption");
    }

    private void addTabCuentasDefault() {
        cuentasDefaultTab = tabSheet.addTab(createTablaCuentasContables(), "Mis Cuentas Contables Default");
        cuentasDefaultTab.setIcon(FontAwesome.ALIGN_CENTER);
        cuentasDefaultTab.setId("2");
        cuentasDefaultTab.setStyleName("dirtyTabCaption");
    }

    private Component createNomenclaturaSeelect() {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth("100%");
        verticalLayout.setHeight("100%");

        miNomenclaturaSelect = new TwinColSelect();
        miNomenclaturaSelect.setLeftColumnCaption("Nomenclatura Master :");
        miNomenclaturaSelect.setRightColumnCaption("Mi nomenclatura :");
        miNomenclaturaSelect.setImmediate(true);
        miNomenclaturaSelect.setNullSelectionAllowed(false);
        miNomenclaturaSelect.setNewItemsAllowed(false);
        miNomenclaturaSelect.setWidth("100%");
        miNomenclaturaSelect.setHeight("100%");
        miNomenclaturaSelect.setRows(30);
        miNomenclaturaSelect.addStyleName("mybigcaption");

        String queryString = "SELECT * FROM contabilidad_nomenclatura ";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " ORDER BY NoCuenta";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {
                do {
                    String id = rsRecords.getString("IdNomenclatura");
                    String nombre = rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5");

                    miNomenclaturaSelect.addItem(id);
                    miNomenclaturaSelect.setItemCaption(id, nombre);
                } while (rsRecords.next());

                queryString = "SELECT * ";
                queryString += " FROM contabilidad_nomenclatura_empresa ";
                queryString += " WHERE Estatus = 'HABILITADA'";
                queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += " ORDER BY NoCuenta";

                rsRecords = stQuery.executeQuery(queryString);
                if (rsRecords.next()) {
                    do {
                        miNomenclaturaSelect.select(rsRecords.getString("IdNomenclatura"));
                    } while (rsRecords.next());
                }
            }

        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR MI NOMENCLATURA DE CUENTAS CONTABLES", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                    "ERROR AL INTENTAR BUSCAR CATALOGO DE MI NOMENCLATURA DE CUENTAS CONTABLES", ex1);
            ex1.printStackTrace();
        }
        verticalLayout.addComponents(miNomenclaturaSelect, createButtonsTab1());

        return verticalLayout;
    }

    private Component createTablaCuentasContables() {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth("100%");
        verticalLayout.setHeight("100%");

        cuentaContable1Cbx = new ComboBox("Proveedores : ");
        cuentaContable1Cbx.setWidth("25em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);
        cuentaContable1Cbx.addStyleName("mybluecaption");

        cuentaContable2Cbx = new ComboBox("Clientes : ");
        cuentaContable2Cbx.setWidth("25em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);
        cuentaContable2Cbx.setStyleName("mybluecaption");

        cuentaContable3Cbx = new ComboBox("Instituciones : ");
        cuentaContable3Cbx.setWidth("25em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);
        cuentaContable3Cbx.addStyleName("mybluecaption");

        cuentaContable4Cbx = new ComboBox("Bancos moneda local : ");
        cuentaContable4Cbx.setWidth("25em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable4Cbx.setInvalidAllowed(false);
        cuentaContable4Cbx.setNewItemsAllowed(false);
        cuentaContable4Cbx.addStyleName("mybluecaption");

        cuentaContable5Cbx = new ComboBox("Bancos moneda extranjera : ");
        cuentaContable5Cbx.setWidth("25em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable5Cbx.setInvalidAllowed(false);
        cuentaContable5Cbx.setNewItemsAllowed(false);
        cuentaContable5Cbx.addStyleName("mybluecaption");

        cuentaContable6Cbx = new ComboBox("Anticipo a proveedores : ");
        cuentaContable6Cbx.setWidth("25em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable6Cbx.setInvalidAllowed(false);
        cuentaContable6Cbx.setNewItemsAllowed(false);
        cuentaContable6Cbx.addStyleName("mybluecaption");

        cuentaContable7Cbx = new ComboBox("Anticipo de clientes : ");
        cuentaContable7Cbx.setWidth("25em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable7Cbx.setInvalidAllowed(false);
        cuentaContable7Cbx.setNewItemsAllowed(false);
        cuentaContable7Cbx.addStyleName("mybluecaption");

        cuentaContable8Cbx = new ComboBox("Liquidaciones caja chica : ");
        cuentaContable8Cbx.setWidth("25em");
        cuentaContable8Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable8Cbx.setInvalidAllowed(false);
        cuentaContable8Cbx.setNewItemsAllowed(false);
        cuentaContable8Cbx.addStyleName("mybluecaption");

        cuentaContable9Cbx = new ComboBox("Compras : ");
        cuentaContable9Cbx.setWidth("25em");
        cuentaContable9Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable9Cbx.setInvalidAllowed(false);
        cuentaContable9Cbx.setNewItemsAllowed(false);
        cuentaContable9Cbx.addStyleName("mybluecaption");

        cuentaContable10Cbx = new ComboBox("Ventas : ");
        cuentaContable10Cbx.setWidth("25em");
        cuentaContable10Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable10Cbx.setInvalidAllowed(false);
        cuentaContable10Cbx.setNewItemsAllowed(false);
        cuentaContable10Cbx.addStyleName("mybluecaption");

        cuentaContable11Cbx = new ComboBox("Abastos : ");
        cuentaContable11Cbx.setWidth("25em");
        cuentaContable11Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable11Cbx.setInvalidAllowed(false);
        cuentaContable11Cbx.setNewItemsAllowed(false);
        cuentaContable11Cbx.addStyleName("mybluecaption");

        cuentaContable12Cbx = new ComboBox("Enganches clientes : ");
        cuentaContable12Cbx.setWidth("25em");
        cuentaContable12Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable12Cbx.setInvalidAllowed(false);
        cuentaContable12Cbx.setNewItemsAllowed(false);
        cuentaContable12Cbx.addStyleName("mybluecaption");

        cuentaContable13Cbx = new ComboBox("IVA por cobrar : ");
        cuentaContable13Cbx.setWidth("25em");
        cuentaContable13Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable13Cbx.setInvalidAllowed(false);
        cuentaContable13Cbx.setNewItemsAllowed(false);
        cuentaContable13Cbx.addStyleName("mybluecaption");

        cuentaContable14Cbx = new ComboBox("IVA por pagar : ");
        cuentaContable14Cbx.setWidth("25em");
        cuentaContable14Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable14Cbx.setInvalidAllowed(false);
        cuentaContable14Cbx.setNewItemsAllowed(false);
        cuentaContable14Cbx.addStyleName("mybluecaption");

        cuentaContable15Cbx = new ComboBox("Efectivo en tránsito : ");
        cuentaContable15Cbx.setWidth("25em");
        cuentaContable15Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable15Cbx.setInvalidAllowed(false);
        cuentaContable15Cbx.setNewItemsAllowed(false);
        cuentaContable15Cbx.addStyleName("mybluecaption");

        cuentaContable16Cbx = new ComboBox("Diferencial cambiario : ");
        cuentaContable16Cbx.setWidth("25em");
        cuentaContable16Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable16Cbx.setInvalidAllowed(false);
        cuentaContable16Cbx.setNewItemsAllowed(false);
        cuentaContable16Cbx.addStyleName("mybluecaption");

        cuentaContable17Cbx = new ComboBox("Préstamos : ");
        cuentaContable17Cbx.setWidth("25em");
        cuentaContable17Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable17Cbx.setInvalidAllowed(false);
        cuentaContable17Cbx.setNewItemsAllowed(false);
        cuentaContable17Cbx.addStyleName("mybluecaption");

        cuentaContable18Cbx = new ComboBox("Interéses préstamos : ");
        cuentaContable18Cbx.setWidth("25em");
        cuentaContable18Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable18Cbx.setInvalidAllowed(false);
        cuentaContable18Cbx.setNewItemsAllowed(false);
        cuentaContable18Cbx.addStyleName("mybluecaption");

        cuentaContable19Cbx = new ComboBox("Interéses devengados : ");
        cuentaContable19Cbx.setWidth("25em");
        cuentaContable19Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable19Cbx.setInvalidAllowed(false);
        cuentaContable19Cbx.setNewItemsAllowed(false);
        cuentaContable19Cbx.addStyleName("mybluecaption");

        cuentaContable20Cbx = new ComboBox("Anticipo honorarios: ");
        cuentaContable20Cbx.setWidth("25em");
        cuentaContable20Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable20Cbx.setInvalidAllowed(false);
        cuentaContable20Cbx.setNewItemsAllowed(false);
        cuentaContable20Cbx.addStyleName("mybluecaption");

        cuentaContable21Cbx = new ComboBox("Anticipo sueldos: ");
        cuentaContable21Cbx.setWidth("25em");
        cuentaContable21Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable21Cbx.setInvalidAllowed(false);
        cuentaContable21Cbx.setNewItemsAllowed(false);
        cuentaContable21Cbx.addStyleName("mybluecaption");

        cuentaContable22Cbx = new ComboBox("Sueldos por pagar: ");
        cuentaContable22Cbx.setWidth("25em");
        cuentaContable22Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable22Cbx.setInvalidAllowed(false);
        cuentaContable22Cbx.setNewItemsAllowed(false);
        cuentaContable22Cbx.addStyleName("mybluecaption");

        cuentaContable23Cbx = new ComboBox("ISR Gasto: ");
        cuentaContable23Cbx.setWidth("25em");
        cuentaContable23Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable23Cbx.setInvalidAllowed(false);
        cuentaContable23Cbx.setNewItemsAllowed(false);
        cuentaContable23Cbx.addStyleName("mybluecaption");

        cuentaContable24Cbx = new ComboBox("ISR por pagar: ");
        cuentaContable24Cbx.setWidth("25em");
        cuentaContable24Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable24Cbx.setInvalidAllowed(false);
        cuentaContable24Cbx.setNewItemsAllowed(false);
        cuentaContable24Cbx.addStyleName("mybluecaption");

        cuentaContable25Cbx = new ComboBox("ISR Retenido por pagar: ");
        cuentaContable25Cbx.setWidth("25em");
        cuentaContable25Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable25Cbx.setInvalidAllowed(false);
        cuentaContable25Cbx.setNewItemsAllowed(false);
        cuentaContable25Cbx.addStyleName("mybluecaption");

        cuentaContable26Cbx = new ComboBox("ISR Opcional mensual por pagar: ");
        cuentaContable26Cbx.setWidth("25em");
        cuentaContable26Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable26Cbx.setInvalidAllowed(false);
        cuentaContable26Cbx.setNewItemsAllowed(false);
        cuentaContable26Cbx.addStyleName("mybluecaption");

        cuentaContable27Cbx = new ComboBox("Redondeo: ");
        cuentaContable27Cbx.setWidth("25em");
        cuentaContable27Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable27Cbx.setInvalidAllowed(false);
        cuentaContable27Cbx.setNewItemsAllowed(false);
        cuentaContable27Cbx.addStyleName("mybluecaption");

        cuentaContable28Cbx = new ComboBox("Multas y rectificaciones: ");
        cuentaContable28Cbx.setWidth("25em");
        cuentaContable28Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable28Cbx.setInvalidAllowed(false);
        cuentaContable28Cbx.setNewItemsAllowed(false);
        cuentaContable28Cbx.addStyleName("mybluecaption");

        cuentaContable29Cbx = new ComboBox("Cuota patronal Igss por pagar: ");
        cuentaContable29Cbx.setWidth("25em");
        cuentaContable29Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable29Cbx.setInvalidAllowed(false);
        cuentaContable29Cbx.setNewItemsAllowed(false);
        cuentaContable29Cbx.addStyleName("mybluecaption");

        cuentaContable30Cbx = new ComboBox("Cuota laboral Igss por pagar: ");
        cuentaContable30Cbx.setWidth("25em");
        cuentaContable30Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable30Cbx.setInvalidAllowed(false);
        cuentaContable30Cbx.setNewItemsAllowed(false);
        cuentaContable30Cbx.addStyleName("mybluecaption");

        cuentaContable31Cbx = new ComboBox("Cuota patronal Igss (gasto): ");
        cuentaContable31Cbx.setWidth("25em");
        cuentaContable31Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable31Cbx.setInvalidAllowed(false);
        cuentaContable31Cbx.setNewItemsAllowed(false);
        cuentaContable31Cbx.addStyleName("mybluecaption");

        cuentaContable32Cbx = new ComboBox("Otros arbitrios: ");
        cuentaContable32Cbx.setWidth("25em");
        cuentaContable32Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable32Cbx.setInvalidAllowed(false);
        cuentaContable32Cbx.setNewItemsAllowed(false);
        cuentaContable32Cbx.addStyleName("mybluecaption");

        cuentaContable33Cbx = new ComboBox("Provisión compras: ");
        cuentaContable33Cbx.setWidth("25em");
        cuentaContable33Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable33Cbx.setInvalidAllowed(false);
        cuentaContable33Cbx.setNewItemsAllowed(false);
        cuentaContable33Cbx.addStyleName("mybluecaption");

        cuentaContable34Cbx = new ComboBox("Servicios bancos: ");
        cuentaContable34Cbx.setWidth("25em");
        cuentaContable34Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable34Cbx.setInvalidAllowed(false);
        cuentaContable34Cbx.setNewItemsAllowed(false);
        cuentaContable34Cbx.addStyleName("mybluecaption");

        cuentaContable35Cbx = new ComboBox("Cheques devueltos: ");
        cuentaContable35Cbx.setWidth("25em");
        cuentaContable35Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable35Cbx.setInvalidAllowed(false);
        cuentaContable35Cbx.setNewItemsAllowed(false);
        cuentaContable35Cbx.addStyleName("mybluecaption");

        cuentaContable36Cbx = new ComboBox("Perdidas Ganancias Ejercicios Anteriores: ");
        cuentaContable36Cbx.setWidth("25em");
        cuentaContable36Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable36Cbx.setInvalidAllowed(false);
        cuentaContable36Cbx.setNewItemsAllowed(false);
        cuentaContable36Cbx.addStyleName("mybluecaption");

        cuentaContable37Cbx = new ComboBox("Sueldo Ordinario: ");
        cuentaContable37Cbx.setWidth("25em");
        cuentaContable37Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable37Cbx.setInvalidAllowed(false);
        cuentaContable37Cbx.setNewItemsAllowed(false);
        cuentaContable37Cbx.addStyleName("mybluecaption");

        cuentaContable38Cbx = new ComboBox("Sueldo Extraoridnario: ");
        cuentaContable38Cbx.setWidth("25em");
        cuentaContable38Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable38Cbx.setInvalidAllowed(false);
        cuentaContable38Cbx.setNewItemsAllowed(false);
        cuentaContable38Cbx.addStyleName("mybluecaption");

        cuentaContable39Cbx = new ComboBox("Bonificacion Decreto 37-2001: ");
        cuentaContable39Cbx.setWidth("25em");
        cuentaContable39Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable39Cbx.setInvalidAllowed(false);
        cuentaContable39Cbx.setNewItemsAllowed(false);
        cuentaContable39Cbx.addStyleName("mybluecaption");

        cuentaContable40Cbx = new ComboBox("Bonificacion Decreto 78_89: ");
        cuentaContable40Cbx.setWidth("25em");
        cuentaContable40Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable40Cbx.setInvalidAllowed(false);
        cuentaContable40Cbx.setNewItemsAllowed(false);
        cuentaContable40Cbx.addStyleName("mybluecaption");

        cuentaContable41Cbx = new ComboBox("Aguinaldo ");
        cuentaContable41Cbx.setWidth("25em");
        cuentaContable41Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable41Cbx.setInvalidAllowed(false);
        cuentaContable41Cbx.setNewItemsAllowed(false);
        cuentaContable41Cbx.addStyleName("mybluecaption");

        cuentaContable42Cbx = new ComboBox("Bono 14 ");
        cuentaContable42Cbx.setWidth("25em");
        cuentaContable42Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable42Cbx.setInvalidAllowed(false);
        cuentaContable42Cbx.setNewItemsAllowed(false);
        cuentaContable42Cbx.addStyleName("mybluecaption");

        cuentaContable43Cbx = new ComboBox("Titulo acción ");
        cuentaContable43Cbx.setWidth("25em");
        cuentaContable43Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable43Cbx.setInvalidAllowed(false);
        cuentaContable43Cbx.setNewItemsAllowed(false);
        cuentaContable43Cbx.addStyleName("mybluecaption");

        cuentaContable44Cbx = new ComboBox("Titulo acción 2");
        cuentaContable44Cbx.setWidth("25em");
        cuentaContable44Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable44Cbx.setInvalidAllowed(false);
        cuentaContable44Cbx.setNewItemsAllowed(false);
        cuentaContable44Cbx.addStyleName("mybluecaption");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        horizontalLayout.setResponsive(true);
        horizontalLayout.setWidth("100%");
        horizontalLayout.setHeight("100%");
//        horizontalLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        horizontalLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        VerticalLayout verticalLayout1 = new VerticalLayout();
        verticalLayout1.setMargin(true);
        verticalLayout1.setSpacing(true);
        verticalLayout1.setResponsive(true);
        verticalLayout1.setCaption("");

        VerticalLayout verticalLayout2= new VerticalLayout();
        verticalLayout2.setMargin(true);
        verticalLayout2.setSpacing(true);
        verticalLayout2.setResponsive(true);
        verticalLayout2.setCaption("");

        VerticalLayout verticalLayout3= new VerticalLayout();
        verticalLayout3.setMargin(true);
        verticalLayout3.setSpacing(true);
        verticalLayout3.setResponsive(true);
        verticalLayout3.setCaption("");

        verticalLayout1.addComponents(
                cuentaContable1Cbx,
                cuentaContable2Cbx,
                cuentaContable3Cbx,
                cuentaContable4Cbx,
                cuentaContable5Cbx,
                cuentaContable6Cbx,
                cuentaContable7Cbx,
                cuentaContable8Cbx,
                cuentaContable9Cbx,
                cuentaContable10Cbx,
                cuentaContable11Cbx,
                cuentaContable13Cbx,
                cuentaContable14Cbx,
                cuentaContable15Cbx,
                cuentaContable16Cbx
        );

        verticalLayout2.addComponents(
                cuentaContable17Cbx,
                cuentaContable18Cbx,
                cuentaContable19Cbx,
                cuentaContable20Cbx,
                cuentaContable21Cbx,
                cuentaContable22Cbx,
                cuentaContable23Cbx,
                cuentaContable24Cbx,
                cuentaContable25Cbx,
                cuentaContable26Cbx,
                cuentaContable27Cbx,
                cuentaContable28Cbx,
                cuentaContable29Cbx,
                cuentaContable30Cbx,
                cuentaContable31Cbx
        );

        verticalLayout3.addComponents(
                cuentaContable32Cbx,
                cuentaContable33Cbx,
                cuentaContable34Cbx,
                cuentaContable35Cbx,
                cuentaContable36Cbx,
                cuentaContable37Cbx,
                cuentaContable38Cbx,
                cuentaContable39Cbx,
                cuentaContable40Cbx,
                cuentaContable41Cbx,
                cuentaContable42Cbx,
                cuentaContable43Cbx,
                cuentaContable44Cbx
        );

        horizontalLayout.addComponents(verticalLayout1, verticalLayout2,verticalLayout3);

        verticalLayout.addComponents(horizontalLayout, createButtonsTab2());

        llenarComboCuentaContable();

        return verticalLayout;
    }

    public void llenarComboCuentaContable() {

        String
        queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                cuentaContable1Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable1Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable2Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable2Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable3Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable3Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable4Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable4Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable5Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable5Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable6Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable6Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable7Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable7Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable8Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable8Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable9Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable9Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable10Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable10Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable11Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable11Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable12Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable12Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable13Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable13Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable14Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable14Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable15Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable15Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable16Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable16Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable17Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable17Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable18Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable18Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable19Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable19Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable20Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable20Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable21Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable21Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable22Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable22Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable23Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable23Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable24Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable24Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable25Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable25Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable26Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable26Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable27Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable27Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable28Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable28Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable29Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable29Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable30Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable30Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable31Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable31Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable32Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable32Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable33Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable33Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable34Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable34Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable35Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable35Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable36Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable36Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable37Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable37Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable38Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable38Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable39Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable39Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable40Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable40Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable41Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable41Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable42Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable42Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable43Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable43Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable44Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable44Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
            }
            selectCuentasContablesPorDefault();

        } catch (Exception ex1) {
            System.out.println("Error al LEER combo cuentas contables DEFAULT: " + ex1.getMessage());
            ex1.printStackTrace();
            Notification.show("Error al leer cuentas contables default: " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    void selectCuentasContablesPorDefault() {
        String queryString = " SELECT * ";
        queryString += " FROM cuentas_contables_default";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                cuentaContable1Cbx.select(rsRecords.getString("Proveedores"));
                cuentaContable2Cbx.select(rsRecords.getString("Clientes"));
                cuentaContable3Cbx.select(rsRecords.getString("Instituciones"));
                cuentaContable4Cbx.select(rsRecords.getString("BancosMonedaLocal"));
                cuentaContable5Cbx.select(rsRecords.getString("BancosMonedaExtranjera"));
                cuentaContable6Cbx.select(rsRecords.getString("AnticiposProveedor"));
                cuentaContable7Cbx.select(rsRecords.getString("AnticiposClientes"));
                cuentaContable8Cbx.select(rsRecords.getString("LiquidacionesCajaChicha"));
                cuentaContable9Cbx.select(rsRecords.getString("Compras"));
                cuentaContable10Cbx.select(rsRecords.getString("Ventas"));
                cuentaContable11Cbx.select(rsRecords.getString("Abastos"));
                cuentaContable12Cbx.select(rsRecords.getString("Enganches"));
                cuentaContable13Cbx.select(rsRecords.getString("IvaPorCobrar"));
                cuentaContable14Cbx.select(rsRecords.getString("IvaPorPagar"));
                cuentaContable15Cbx.select(rsRecords.getString("EfectivoEnTransito"));
                cuentaContable16Cbx.select(rsRecords.getString("DiferencialCambiario"));
                cuentaContable17Cbx.select(rsRecords.getString("Prestamos"));
                cuentaContable18Cbx.select(rsRecords.getString("InteresesPrestamo"));
                cuentaContable19Cbx.select(rsRecords.getString("InteresesDevengados"));
                cuentaContable20Cbx.select(rsRecords.getString("AnticipoHonorarios"));
                cuentaContable21Cbx.select(rsRecords.getString("AnticipoSueldos"));
                cuentaContable22Cbx.select(rsRecords.getString("SueldosPorPagar"));
                cuentaContable23Cbx.select(rsRecords.getString("IsrGasto"));
                cuentaContable24Cbx.select(rsRecords.getString("IsrPorPagar"));
                cuentaContable25Cbx.select(rsRecords.getString("IsrRetenidoPorPagar"));
                cuentaContable26Cbx.select(rsRecords.getString("IsrOpcionalMensualPorPagar"));
                cuentaContable27Cbx.select(rsRecords.getString("Redondeo"));
                cuentaContable28Cbx.select(rsRecords.getString("MultasYRectificaciones"));
                cuentaContable29Cbx.select(rsRecords.getString("CuotaPatronalIgssPorPagar"));
                cuentaContable30Cbx.select(rsRecords.getString("CuotaLaboralIgssPorPagar"));
                cuentaContable31Cbx.select(rsRecords.getString("CuotaPatronalIgss")); //gasto
                cuentaContable32Cbx.select(rsRecords.getString("OtrosArbitrios")); //gasto
                cuentaContable33Cbx.select(rsRecords.getString("ProvisionCompras")); //gasto
                cuentaContable34Cbx.select(rsRecords.getString("ServiciosBancos")); //gasto
                cuentaContable35Cbx.select(rsRecords.getString("ChequesDevueltos")); //gasto
                cuentaContable36Cbx.select(rsRecords.getString("PerdidasGananciasEjercicioAnterior")); //resultados
                cuentaContable37Cbx.select(rsRecords.getString("SueldoOrdinario"));
                cuentaContable38Cbx.select(rsRecords.getString("SueldoExtraordinario"));
                cuentaContable39Cbx.select(rsRecords.getString("Bono37_2001"));
                cuentaContable40Cbx.select(rsRecords.getString("Bono78_89"));
                cuentaContable41Cbx.select(rsRecords.getString("Aguinaldo"));
                cuentaContable42Cbx.select(rsRecords.getString("Bono14"));
                cuentaContable43Cbx.select(rsRecords.getString("TituloAccion"));
                cuentaContable44Cbx.select(rsRecords.getString("TituloAccion2"));

            }
        } catch (Exception ex1) {
            System.out.println("Error al intntentar leer tabla cuentas_contables_default:  " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private HorizontalLayout createButtonsTab1() {

        Button saveBtn = new Button("GUARDAR");
        saveBtn.setIcon(FontAwesome.PLUS);
        saveBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        saveBtn.setDescription("GUARDAR CAMBIOS.");
        saveBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                    //DELETE
                    String queryString =  "DELETE FROM contabilidad_nomenclatura_empresa ";
                    queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

                    stQuery.executeUpdate(queryString);

                    if(miNomenclaturaSelect.getValue() != null) {
                        queryString =  "INSERT INTO contabilidad_nomenclatura_empresa (IdEmpresa, IdNomenclatura, ";
                        queryString += "Reporte, ID1,N1,ID2,N2,ID3,N3,ID4,N4,ID5,NoCuenta,N5,";
                        queryString += "FiltrarIngresoDocumentos, FiltrarFormularioLiquidacion, Tipo, CodigoCC)";
                        queryString += " VALUES ";

                        Set<String> seleccionados = (Set<String>)miNomenclaturaSelect.getValue();

                        String subQueryString;

                        //recorreger los items seleccionados del componente miNomenclaturaSelec
                        for(Object selectedItem:seleccionados) {

                            subQueryString = "SELECT * FROM contabilidad_nomenclatura ";
                            subQueryString += " WHERE IdNomenclatura = " + selectedItem.toString();

                            rsRecords = stQuery.executeQuery(subQueryString);
                            rsRecords.next();

                            queryString += "(";
                            queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                            queryString += "," + selectedItem.toString();
                            queryString += ",'" + rsRecords.getString("Reporte") + "'";
                            queryString += ",'" + rsRecords.getString("Id1") + "'";
                            queryString += ",'" + rsRecords.getString("N1") + "'";
                            queryString += ",'" + rsRecords.getString("Id2") + "'";
                            queryString += ",'" + rsRecords.getString("N2") + "'";
                            queryString += ",'" + rsRecords.getString("Id3") + "'";
                            queryString += ",'" + rsRecords.getString("N3") + "'";
                            queryString += ",'" + rsRecords.getString("Id4") + "'";
                            queryString += ",'" + rsRecords.getString("N4") + "'";
                            queryString += ",'" + rsRecords.getString("Id5") + "'";
                            queryString += ",'" + rsRecords.getString("NoCuenta") + "'";
                            queryString += ",'" + rsRecords.getString("N5") + "'";
                            queryString += ",'" + rsRecords.getString("FiltrarIngresoDocumentos") + "'";
                            queryString += ",'" + rsRecords.getString("FiltrarFormularioLiquidacion") + "'";
                            queryString += ",'" + rsRecords.getString("Tipo") + "'";
                            queryString += ",'" + rsRecords.getString("CodigoCC") + "'";
                            queryString += "),";
                        }
                        queryString = queryString.substring(0, queryString.length()-1);
                    }

//System.out.println(queryString);

                    stQuery.executeUpdate(queryString);

                    ((SopdiUI) mainUI).fillCuentasContablesPorDefault();

                    Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

                } catch (Exception ex) {
                    Notification.show("Error al creaar/actualizar cuentas contables de empresa: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    System.out.println("Error en el boton guardar cambios cuentas contables de empresa.");
                    ex.printStackTrace();
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.MIDDLE_CENTER);

        return buttonsLayout;
    }

    private HorizontalLayout createButtonsTab2() {

        Button saveBtn = new Button("GUARDAR");
        saveBtn.setIcon(FontAwesome.PLUS);
        saveBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        saveBtn.setDescription("GUARDAR CAMBIOS.");
        saveBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                    //DELETE
                    String queryString =  "DELETE FROM cuentas_contables_default ";
                    queryString += " WHERE IdEmpresa = " +  ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

                    stQuery.executeUpdate(queryString);

                    //INSERT
                    queryString =  "INSERT INTO cuentas_contables_default (";
                    queryString += " IdEmpresa, Proveedores, Clientes, Instituciones, BancosMonedaLocal, BancosMonedaExtranjera, ";
                    queryString += " AnticiposProveedor, AnticiposClientes, LiquidacionesCajaChicha, Compras, Ventas, Abastos, ";
                    queryString += " Enganches, IvaPorCobrar, IvaPorPagar, EfectivoEnTransito, DiferencialCambiario, Prestamos, ";
                    queryString += " InteresesPrestamo, InteresesDevengados, AnticipoHonorarios, AnticipoSueldos, SueldosPorPagar,";
                    queryString += " IsrGasto, IsrPorPagar, IsrRetenidoPorPagar, IsrOpcionalMensualPorPagar, Redondeo, MultasYRectificaciones, ";
                    queryString += " CuotaPatronalIgssPorPagar, CuotaLaboralIgssPorPagar, CuotaPatronalIgss, OtrosArbitrios, ";
                    queryString += " ProvisionCompras, ServiciosBancos, ChequesDevueltos, PerdidasGananciasEjercicioAnterior, SueldoOrdinario, ";
                    queryString += " SueldoExtraordinario, Bono37_2001, Bono78_89, Aguinaldo, Bono14, TituloAccion, TituloAccion2 ";
                    queryString +=   ") ";
                    queryString += " VALUES ( ";
                    queryString +=  ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString +=  ",'" + cuentaContable1Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable2Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable3Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable4Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable5Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable6Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable7Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable8Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable9Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable10Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable11Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable12Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable13Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable14Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable15Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable16Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable17Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable18Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable19Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable20Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable21Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable22Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable23Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable24Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable25Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable26Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable27Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable28Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable29Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable30Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable31Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable32Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable33Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable34Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable35Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable36Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable37Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable38Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable39Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable40Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable41Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable42Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable43Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable44Cbx.getValue() + "'";
                    queryString += " ) ";

                    stQuery.executeUpdate(queryString);

                    ((SopdiUI) mainUI).fillCuentasContablesPorDefault();

                    Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

                } catch (Exception ex) {
                    Notification.show("Error al creaar/actualizar cuentas contables default: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    System.out.println("Error en el boton guardar cambios cuentas contables por default.");
                    ex.printStackTrace();
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.MIDDLE_RIGHT);

        return buttonsLayout;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Cuentas contables Default");
    }
}
