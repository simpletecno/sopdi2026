package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import org.vaadin.dialogs.ConfirmDialog;

public class ConciliacionBancariaView extends VerticalLayout implements View {

    Grid conciliacionGrid;
    public IndexedContainer container = new IndexedContainer();
    static final String ID_CONCILIACION_PROPERTY = "Id";
    static final String ID_CUENTAS_BANCO_PROPERTY = "Id Cuenta Banco";
    static final String ANIO_MES_PROPERTY = "Mes Conciliado";
    static final String SALDO_MES_ANTERIOR_PROPERTY = "Saldo mes anterior";
    static final String DEBITO_NOCONCILIADO_MONTO_PROPERTY = "Total Engresos";
    static final String CREDITO_NOCONCILIADO_MONTO_PROPERTY = "Total Ingresos";
    static final String SALDO_FINAL_BANCO_PROPERTY = "Saldo Final Banco";
    static final String SALDO_FINAL_CONTABLE_PROPERTY = "Saldo Final Contable";
    static final String SALDO_FINAL_CONTAQ_PROPERTY = "Saldo Final Contable Q.";
    static final String CORRELATIVO_PROPERTY = "Correlativo";
    static final String BANCO_PROPERTY = "Banco # de Cuenta";
    static final String CREADO_USUARIO_PROPERTY = "Usuario";
    static final String CREADO_FECHA_PROPERTY = "Creado";
    static final String ESTATUS_PROPERTY = "Estatus";

    ComboBox empresaCbx;

    UI mainUI;
    Statement stQuery, stQuery2;
    ResultSet rsRecords;
    ResultSet rsRecords2;
    String queryString;

    PopupDateField mesDt;

    static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    public ConciliacionBancariaView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);

        Label titleLbl = new Label("LIBRO CONCILIACIONES BANCARIAS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);

        llenarComboEmpresa();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.BOTTOM_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearFormularioConciliacion();
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
            System.out.println("Error al llenar Combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearFormularioConciliacion() {

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("96%");
        mainLayout.addStyleName("rcorners3");
        mainLayout.setResponsive(true);
        mainLayout.setMargin(true);

        mesDt = new PopupDateField("SELECCIONE EL MES : ");
        mesDt.setResponsive(true);
        mesDt.setWidth("10em");
        mesDt.setValue(new java.util.Date());
        mesDt.setResolution(Resolution.MONTH);
        mesDt.setDateFormat("MM/yyyy");
        mesDt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                llenarGridConciliacion();
            }
        });

        container.addContainerProperty(ID_CONCILIACION_PROPERTY, String.class, null);
        container.addContainerProperty(ID_CUENTAS_BANCO_PROPERTY, String.class, null);
        container.addContainerProperty(ANIO_MES_PROPERTY, String.class, null);
        container.addContainerProperty(CORRELATIVO_PROPERTY, String.class, null);
        container.addContainerProperty(BANCO_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_MES_ANTERIOR_PROPERTY, String.class, null);
        container.addContainerProperty(DEBITO_NOCONCILIADO_MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(CREDITO_NOCONCILIADO_MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_FINAL_BANCO_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_FINAL_CONTABLE_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_FINAL_CONTAQ_PROPERTY, String.class, null);

        container.addContainerProperty(CREADO_USUARIO_PROPERTY, String.class, null);
        container.addContainerProperty(CREADO_FECHA_PROPERTY, String.class, null);

        conciliacionGrid = new Grid("", container);
        conciliacionGrid.setWidth("100%");
        conciliacionGrid.setResponsive(true);
        conciliacionGrid.setImmediate(true);
        conciliacionGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        conciliacionGrid.setDescription("Seleccione una conciliación");
        conciliacionGrid.setHeightMode(HeightMode.ROW);
        conciliacionGrid.setHeightByRows(12);
        conciliacionGrid.setResponsive(true);
        conciliacionGrid.setEditorBuffered(false);

        conciliacionGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (DEBITO_NOCONCILIADO_MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (CREDITO_NOCONCILIADO_MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        conciliacionGrid.getColumn(ID_CONCILIACION_PROPERTY).setHidable(true).setHidden(true);
        conciliacionGrid.getColumn(ID_CUENTAS_BANCO_PROPERTY).setHidable(true).setHidden(true);
        conciliacionGrid.getColumn(CREADO_USUARIO_PROPERTY).setHidable(true).setHidden(true);
        conciliacionGrid.getColumn(CREADO_FECHA_PROPERTY).setHidable(true).setHidden(true);
        conciliacionGrid.getColumn(ANIO_MES_PROPERTY).setHidable(true).setHidden(true);
        conciliacionGrid.getColumn(SALDO_FINAL_CONTABLE_PROPERTY).setHidable(true).setHidden(true);
        conciliacionGrid.getColumn(SALDO_FINAL_CONTAQ_PROPERTY).setHidable(true).setHidden(true);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setResponsive(true);
        buttonLayout.setMargin(new MarginInfo(true, false, false, false));
        buttonLayout.setWidth("100%");

        Button nuevaBtn = new Button("Nueva conciliación");
        nuevaBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        nuevaBtn.setIcon(FontAwesome.PLUS);
        nuevaBtn.addClickListener((event) -> {
            ConciliacionBancariaForm conciliacionForm = new ConciliacionBancariaForm("");
            UI.getCurrent().addWindow(conciliacionForm);
            conciliacionForm.center();
        });

        Button eliminarBtn = new Button("Eliminar conciliación");
        eliminarBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.addClickListener((event) -> {
            if (conciliacionGrid.getSelectedRow() != null) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de ELIMINAR esta Conciliaciòn?",
                        "SI", "NO", new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {

                            try {
                                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

                                queryString = "DELETE FROM conciliacion_bancaria_noconciliados";
                                queryString += " WHERE IdConciliacion = " + container.getContainerProperty(conciliacionGrid.getSelectedRow(), ID_CONCILIACION_PROPERTY).getValue();

                                System.out.println("Query Delete no conciliados " + queryString);
                                stQuery.executeUpdate(queryString);

                                queryString = "DELETE FROM contabilidad_conciliacion_bancaria";
                                queryString += " WHERE IdConciliacionBancaria = " + container.getContainerProperty(conciliacionGrid.getSelectedRow(), ID_CONCILIACION_PROPERTY).getValue();

                                System.out.println("Query Delete conciliacion " + queryString);
                                stQuery.executeUpdate(queryString);

                                queryString = "UPDATE contabilidad_partida set IdConciliacion = 0 ";
                                queryString += " WHERE IdConciliacion = " + container.getContainerProperty(conciliacionGrid.getSelectedRow(), ID_CONCILIACION_PROPERTY).getValue();

                                System.out.println("Query update IdConciliacion " + queryString);
                                stQuery.executeUpdate(queryString);

                                container.removeItem(conciliacionGrid.getSelectedRow());
                                conciliacionGrid.select(null);

                                Notification notif = new Notification("REGISTRO ELIMINADO CON EXITO!.", Notification.Type.HUMANIZED_MESSAGE);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.setIcon(FontAwesome.SAVE);
                                notif.show(Page.getCurrent());

                            } catch (Exception e) {
                                System.out.println("Error al intentar eliminar conciliación" + e);
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        Button editarBtn = new Button("Editar conciliación");
        editarBtn.setIcon(FontAwesome.EDIT);
        editarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        editarBtn.addClickListener((event) -> {
            if (conciliacionGrid.getSelectedRow() != null) {

                if (String.valueOf(container.getContainerProperty(conciliacionGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("FINALIZADA")) {
                    Notification notif = new Notification("ESTA CONCILIACIÒN YA FUE FINALIZADA NO PUEDE SER EDITADA.", Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                } else {
                    ConciliacionBancariaForm conciliacionForm = new ConciliacionBancariaForm(String.valueOf(container.getContainerProperty(conciliacionGrid.getSelectedRow(), ID_CONCILIACION_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(conciliacionForm);
                    conciliacionForm.center();
                }

            } else {
                Notification notif = new Notification("POR FAVOR SELECCIONE UNA CONCILIACIÓN PARA PODER EDITARLA.", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.CHECK);
                notif.show(Page.getCurrent());
            }
        });
        
        if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("AUDITORIA")) {
            nuevaBtn.setVisible(false);
            eliminarBtn.setVisible(false);
            editarBtn.setVisible(false);
            
        }

        Button pdfBtn = new Button("Re imprimir PDF");
        pdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        pdfBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        pdfBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (conciliacionGrid.getSelectedRow() != null) {
                    if (String.valueOf(container.getContainerProperty(conciliacionGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("FINALIZADA")) {
                        ConciliacionPDF conciliacionPDF = new ConciliacionPDF(String.valueOf(container.getContainerProperty(conciliacionGrid.getSelectedRow(), ID_CONCILIACION_PROPERTY).getValue()), getEmpresaNit(), Utileria.getFechaMMYYYY(mesDt.getValue()), getNombreBanco());
                        UI.getCurrent().addWindow(conciliacionPDF);
                        conciliacionPDF.center();
                    }
                } else {
                    Notification notif = new Notification("POR FAVOR SELECCIONE UNA CONCILIACIÓN PARA PODER EDITARLA.", Notification.Type.HUMANIZED_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.CHECK);
                    notif.show(Page.getCurrent());
                }
            }
        });

        buttonLayout.addComponents(eliminarBtn, editarBtn, pdfBtn, nuevaBtn);
        buttonLayout.setComponentAlignment(eliminarBtn, Alignment.TOP_LEFT);
        buttonLayout.setComponentAlignment(editarBtn, Alignment.TOP_CENTER);
        buttonLayout.setComponentAlignment(pdfBtn, Alignment.TOP_CENTER);
        buttonLayout.setComponentAlignment(nuevaBtn, Alignment.TOP_RIGHT);

        mainLayout.addComponents(mesDt, conciliacionGrid, buttonLayout);
        mainLayout.setComponentAlignment(mesDt, Alignment.MIDDLE_CENTER);
        mainLayout.setComponentAlignment(conciliacionGrid, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.TOP_CENTER);

        addComponent(mainLayout);
        setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);
    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        queryString = " SELECT Nit from contabilidad_empresa ";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) {
                strNit = rsRecords2.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }

    public void llenarGridConciliacion() {

        container.removeAllItems();

        queryString = " SELECT *, CCB.IdProveedor,CCB.NoCuenta, proveedor.Nombre";
        queryString += " FROM contabilidad_conciliacion_bancaria AS CB";
        queryString += " INNER JOIN contabilidad_cuentas_bancos AS CCB on CB.IdCuentaBanco = CCB.IdCuentaBanco";
        queryString += " INNER JOIN proveedor on CCB.IdProveedor = proveedor.IDProveedor";
        queryString += " WHERE CB.IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND CB.AnioMes = '" + Utileria.getFechaYYYYMM(mesDt.getValue()).replaceAll("/", "") + "'";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {

                Notification notif = new Notification("POR FAVOR ESPERE UN MOMENTO ", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.TIMES_CIRCLE);

                do {

                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, ID_CONCILIACION_PROPERTY).setValue(rsRecords.getString("IdConciliacionBancaria"));
                    container.getContainerProperty(itemId, ID_CUENTAS_BANCO_PROPERTY).setValue(rsRecords.getString("IdCuentaBanco"));
                    container.getContainerProperty(itemId, ANIO_MES_PROPERTY).setValue(rsRecords.getString("AnioMes"));
                    container.getContainerProperty(itemId, SALDO_MES_ANTERIOR_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("SaldoInicialContable")));
                    container.getContainerProperty(itemId, DEBITO_NOCONCILIADO_MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("EgresosNoConciliado_Monto")));
                    container.getContainerProperty(itemId, CREDITO_NOCONCILIADO_MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("IngresosNoConciliado_Monto")));
                    container.getContainerProperty(itemId, SALDO_FINAL_BANCO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("SaldoFinalBanco")));
                    container.getContainerProperty(itemId, SALDO_FINAL_CONTABLE_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("SaldoFinalContable")));
                    container.getContainerProperty(itemId, SALDO_FINAL_CONTAQ_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("SaldoFinalContable") * ((SopdiUI) UI.getCurrent()).getTasaCambioDelDia(Utileria.getFechaYYYYMMDD_1(Utileria.getFinMesDate(mesDt.getValue())))));
                    container.getContainerProperty(itemId, CORRELATIVO_PROPERTY).setValue(rsRecords.getString("Correlativo"));
                    container.getContainerProperty(itemId, BANCO_PROPERTY).setValue(rsRecords.getString("NoCuenta") + " " + rsRecords.getString("Nombre"));
                    container.getContainerProperty(itemId, CREADO_USUARIO_PROPERTY).setValue(rsRecords.getString("CreadoUsuario"));
                    container.getContainerProperty(itemId, CREADO_FECHA_PROPERTY).setValue(Utileria.getFechaYYYYMMDDHHMMSS(rsRecords.getDate("CreadoFechaYHora")));
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));

                } while (rsRecords.next());

            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla empresas contables :" + ex);
            ex.printStackTrace();
        }
    }

    public String getNombreBanco() {
        String nombreBanco = "";

        queryString = " SELECT *, CCB.IdProveedor,CCB.NoCuenta, proveedor.Nombre";
        queryString += " FROM contabilidad_conciliacion_bancaria AS CB";
        queryString += " INNER JOIN contabilidad_cuentas_bancos AS CCB on CB.IdCuentaBanco = CCB.IdCuentaBanco";
        queryString += " INNER JOIN proveedor on CCB.IdProveedor = proveedor.IDProveedor";
        queryString += " WHERE CB.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND CB.AnioMes = '" + Utileria.getFechaYYYYMM(mesDt.getValue()).replaceAll("/", "") + "'";
        queryString += " AND CB.IdConciliacionBancaria = " + String.valueOf(container.getContainerProperty(conciliacionGrid.getSelectedRow(), ID_CONCILIACION_PROPERTY).getValue());

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) {
                nombreBanco = rsRecords2.getString("proveedor.Nombre") + rsRecords2.getString("CCB.NoCuenta");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el nombre del banco(PROVEEDOR): " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return nombreBanco;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Conciliaciones Bancarias");
    }

}
