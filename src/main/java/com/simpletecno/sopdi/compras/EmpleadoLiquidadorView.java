/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.seguridad.UsersView;
import com.simpletecno.sopdi.*;
import com.simpletecno.sopdi.recursoshumanos.EmpleadoLiquidadorForm;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
public class EmpleadoLiquidadorView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords  = null;
    public ResultSet rsRecords1 = null;

    protected static final String ID_PROPERTY           = "Id";
    protected static final String EMPRESA_PROPERTY      = "Empresa";
    protected static final String LIQUIDADOR_PROPERTY   = "Liquidador";
    protected static final String PROVEEDOR_PROPERTY    = "Proveedor";
    protected static final String NOMENCLATURA_PROPERTY = "Cuenta Contable";
    protected static final String OPTIONS_PROPERTY      = "-";

    public Table empleadoLiquidadorTable;

    String queryString;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public EmpleadoLiquidadorView() {

        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " RELACION LIQUIDADORES Y CONTABILIDAD");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTable();

        fillTable();

    }

    public void createTable() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("95%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);

        empleadoLiquidadorTable = new Table("Relación Liquidador Contabilidad");

        reportLayout.addComponent(empleadoLiquidadorTable);
        reportLayout.setComponentAlignment(empleadoLiquidadorTable, Alignment.MIDDLE_CENTER);

        empleadoLiquidadorTable.setWidth("100%");
        empleadoLiquidadorTable.setResponsive(true);
        empleadoLiquidadorTable.setPageLength(20);

        empleadoLiquidadorTable.setImmediate(true);
        empleadoLiquidadorTable.setSelectable(true);

        empleadoLiquidadorTable.addContainerProperty(ID_PROPERTY,    String.class, null);
        empleadoLiquidadorTable.addContainerProperty(EMPRESA_PROPERTY,   String.class, null);
        empleadoLiquidadorTable.addContainerProperty(LIQUIDADOR_PROPERTY,   String.class, null);
        empleadoLiquidadorTable.addContainerProperty(PROVEEDOR_PROPERTY,   String.class, null);
        empleadoLiquidadorTable.addContainerProperty(NOMENCLATURA_PROPERTY,   String.class, null);
        empleadoLiquidadorTable.addContainerProperty(OPTIONS_PROPERTY,   MenuBar.class, null);

        empleadoLiquidadorTable.setColumnAlignments(Table.Align.CENTER, Table.Align.LEFT, Table.Align.LEFT,
                Table.Align.LEFT, Table.Align.LEFT, Table.Align.CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);

        Button newBtn;
        newBtn    = new Button("Agregar");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.setWidth(130,Sizeable.UNITS_PIXELS);
//        newBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        newBtn.setDescription("Registrar nueva relación");
        newBtn.addListener ((Button.ClickListener) event -> {
            EmpleadoLiquidadorForm  empleadoLiquidadorForm = new EmpleadoLiquidadorForm("");
            UI.getCurrent().addWindow(empleadoLiquidadorForm);
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.addComponent(newBtn);

        buttonsLayout.setComponentAlignment(newBtn, Alignment.BOTTOM_CENTER);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

    }

    public void fillTable() {

        empleadoLiquidadorTable.removeAllItems();
        empleadoLiquidadorTable.setFooterVisible(false);

        queryString =  "SELECT EL.*, EMP.Empresa EmpresaNombre, LIQ.Nombre LiquidadorNombre, PRV.Nombre ProveedorNombre,";
        queryString += " CONCAT(NOM.IdNomenclatura, ' ', NOM.NoCuenta, ' ', NOM.N5) Nomenclatura ";
        queryString += " FROM empleado_liquidador EL";
        queryString += " INNER JOIN contabilidad_empresa EMP On EMP.IdEmpresa = EL.IdEmpresa";
        queryString += " INNER JOIN contabilidad_nomenclatura NOM On NOM.IdNomenclatura = EL.IdNomenclatura";
        queryString += " INNER JOIN proveedor LIQ ON LIQ.IDProveedor = EL.IdEmpleado";
        queryString += " INNER JOIN proveedor PRV ON PRV.IDProveedor = EL.IdProveedor";
        queryString += " AND   EL.IdEmpresa = " + empresaId;

System.out.println("\n\n"+queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                // Define a common menu command for all the menu items.
                MenuBar.Command mycommand = new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        if(empleadoLiquidadorTable.getValue() != null) {
                            MenuBar menuBar = (MenuBar)empleadoLiquidadorTable.getContainerProperty(empleadoLiquidadorTable.getValue(), OPTIONS_PROPERTY).getValue();
                            if(menuBar.getItems().get(0).getChildren().contains(selectedItem)) {

                                if(selectedItem.getId() == 3) { // editar
                                    EmpleadoLiquidadorForm  empleadoLiquidadorForm = new EmpleadoLiquidadorForm(String.valueOf(empleadoLiquidadorTable.getValue()));
                                    UI.getCurrent().addWindow(empleadoLiquidadorForm);
                                }
                                if(selectedItem.getId() == 5) { // eliminar
                                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro?",
                                            "SI", "NO", new ConfirmDialog.Listener() {

                                                public void onClose(ConfirmDialog dialog) {
                                                    if (dialog.isConfirmed()) {
                                                        Notification.show("NO DISPONIBLE EN ESTA VERSION!", Notification.Type.WARNING_MESSAGE);
                                                    }
                                                }
                                            });
                                }
                            }
                            else {
                                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                            }
                        }
                    }
                };

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                do {

                    MenuBar contactMenu = new MenuBar();
                    contactMenu.setCaption("Menú");
                    contactMenu.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
                    contactMenu.addStyleName(ValoTheme.MENUBAR_SMALL);
                    contactMenu.addStyleName(ValoTheme.MENU_APPEAR_ON_HOVER);
                    contactMenu.setSizeUndefined();
                    contactMenu.setData(rsRecords.getInt("Id"));
                    MenuBar.MenuItem menuItem = contactMenu.addItem("", FontAwesome.EDIT, null);
                    menuItem.addItem("Editar", FontAwesome.EYE, mycommand);
                    menuItem.addSeparator();
                    menuItem.addItem("Eliminar", FontAwesome.TRASH, mycommand);

                    empleadoLiquidadorTable.addItem(new Object[] {
                            rsRecords.getString("Id"),
                            rsRecords.getString("EmpresaNombre"),
                            rsRecords.getString("LiquidadorNombre"),
                            rsRecords.getString("ProveedorNombre"),
                            rsRecords.getString("Nomenclatura"),
                            contactMenu
                    }, rsRecords.getInt("Id"));

                }while(rsRecords.next());

                if(rsRecords.first()) {
                    empleadoLiquidadorTable.select(rsRecords.getInt("Id"));
                }
            }
        }
        catch (Exception ex) {
            Logger.getLogger(UsersView.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al intentar leer datos relación liquidador y cuentas contables..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    public boolean exportToExcel() {
        ExcelExport excelExport;

        excelExport = new ExcelExport(empleadoLiquidadorTable);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName("SOPDI_Usuarios.xls");

        String mainTitle = "SOPDI - RELACION LIQUIDADOR CONTABILIDAD AL: "  + new Utileria().getFechaYYYYMMDD_1(new Date());

        excelExport.setReportTitle(mainTitle);

        excelExport.export();

        return true;

    }

    /**
     * This class creates a streamresource. This class implements
     * the StreamSource interface which defines the getStream method.
     */
    public static class ShowExcelFile implements StreamResource.StreamSource {

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        public ShowExcelFile(File fileToOpen) {
            try {

                FileOutputStream fost = new FileOutputStream(fileToOpen);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public InputStream getStream() {
            // Here we return the pdf contents as a byte-array
            return new ByteArrayInputStream(os.toByteArray());
        }
    }

    void setTableTitle(String tableTitle) {
        if(empleadoLiquidadorTable != null) {
            empleadoLiquidadorTable.setCaption(tableTitle);
            empleadoLiquidadorTable.setDescription(tableTitle);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - CENTRO COSTO CONTABILIDAD ");

    }
}
