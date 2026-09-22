/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.configuracion.ProjectsSelectionWindow;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class UsersView extends VerticalLayout implements View {

    public Statement stQuery    = null;
    public ResultSet rsRecords  = null;
    public ResultSet rsRecords1 = null;

    protected static final String CODIGO_PROPERTY   = "Id";
    protected static final String EMPRESA_PROPERTY  = "Empresa";
    protected static final String DIVISION_PROPERTY = "División o Depto.";
    protected static final String USUARIO_PROPERTY  = "Usuario";
    protected static final String NOMBRE_PROPERTY   = "Nombre";
    protected static final String PERFIL_PROPERTY   = "Perfil";
    protected static final String ESTATUS_PROPERTY  = "Estatus";
    protected static final String OPTIONS_PROPERTY  = "-";

    Button    newBtn;
    Button    exportExcelBtn;
    TextField nombreTxt;

    /** Referencia de compatibilidad — apunta a activeTable. */
    public Table usersTable;
    Table    activeTable;
    Table    inactiveTable;
    TabSheet tabSheet;

    final UI mainUI = UI.getCurrent();

    public UsersView() {

        setSizeFull();
        setSpacing(true);
        setMargin(new MarginInfo(true, true, false, true));

        // ── Barra superior ────────────────────────────────────────────
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidth("100%");
        topBar.setSpacing(true);
        topBar.addStyleName("rcorners3");
        topBar.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        Label viewCaption = new Label("Usuarios del sistema");
        viewCaption.setStyleName(ValoTheme.LABEL_H3);

        nombreTxt = new TextField("Nombre");
        nombreTxt.setDescription("Buscar por nombre");
        nombreTxt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                fillReportTable();
            }
        });

        newBtn = new Button("Nuevo");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.setWidth(130, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Registrar nuevo usuario");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UserForm userForm = new UserForm();
                userForm.idUsuario = 0;
                userForm.nombreTxt.focus();
                UI.getCurrent().addWindow(userForm);
            }
        });

        exportExcelBtn = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Table current = getVisibleTable();
                if (current.size() > 0) {
                    exportToExcel(current);
                }
            }
        });

        topBar.addComponent(viewCaption);
        topBar.addComponent(nombreTxt);
        topBar.addComponent(newBtn);
        topBar.addComponent(exportExcelBtn);
        topBar.setExpandRatio(nombreTxt, 1);

        addComponent(topBar);

        // ── TabSheet ──────────────────────────────────────────────────
        activeTable   = buildTable();
        inactiveTable = buildTable();
        usersTable    = activeTable;

        VerticalLayout activeWrapper = new VerticalLayout();
        activeWrapper.setSizeFull();
        activeWrapper.addComponent(activeTable);
        activeWrapper.setExpandRatio(activeTable, 1);

        VerticalLayout inactiveWrapper = new VerticalLayout();
        inactiveWrapper.setSizeFull();
        inactiveWrapper.addComponent(inactiveTable);
        inactiveWrapper.setExpandRatio(inactiveTable, 1);

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(activeWrapper,   "Usuarios Activos");
        tabSheet.addTab(inactiveWrapper, "Usuarios Inactivos");

        addComponent(tabSheet);
        setExpandRatio(tabSheet, 1);

        fillReportTable();
    }

    private Table buildTable() {
        Table t = new Table();
        t.setSizeFull();
        t.setImmediate(true);
        t.setSelectable(true);
        t.addContainerProperty(CODIGO_PROPERTY,   String.class,  null);
        t.addContainerProperty(EMPRESA_PROPERTY,  String.class,  null);
        t.addContainerProperty(DIVISION_PROPERTY, String.class,  null);
        t.addContainerProperty(USUARIO_PROPERTY,  String.class,  null);
        t.addContainerProperty(NOMBRE_PROPERTY,   String.class,  null);
        t.addContainerProperty(PERFIL_PROPERTY,   String.class,  null);
        t.addContainerProperty(ESTATUS_PROPERTY,  String.class,  null);
        t.addContainerProperty(OPTIONS_PROPERTY,  MenuBar.class, null);
        t.setColumnAlignments(
                Table.Align.CENTER, Table.Align.LEFT,   Table.Align.LEFT,
                Table.Align.LEFT,   Table.Align.LEFT,   Table.Align.LEFT,
                Table.Align.CENTER, Table.Align.CENTER);
        return t;
    }

    /** Retorna la tabla de la pestaña actualmente visible. */
    private Table getVisibleTable() {
        if (tabSheet == null || tabSheet.getSelectedTab() == null) return activeTable;
        return tabSheet.getTabPosition(tabSheet.getTab(tabSheet.getSelectedTab())) == 0
                ? activeTable : inactiveTable;
    }

    /** @deprecated Solo por compatibilidad con llamadas externas. */
    public void createReportTable() { /* no-op: tablas creadas en constructor */ }

    public void fillReportTable() {

        activeTable.removeAllItems();
        inactiveTable.removeAllItems();

        String queryString  = "Select Usr.*, Emp.Nombre EmpresaNombre ";
               queryString += " From  usuario Usr";
               queryString += " Inner Join empresa Emp On Emp.IdEmpresa = Usr.IdEmpresa";
               queryString += " Where Usr.IdEmpresa > 0";
        if (((SopdiUI) mainUI).sessionInformation.getStrUserProfile().compareTo("DESARROLLADOR") == 0) {
            queryString += " And Usr.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId();
        }
        if (nombreTxt != null && !nombreTxt.getValue().trim().isEmpty()) {
            queryString += " And Usr.Nombre Like '%" + nombreTxt.getValue().trim() + "%'";
        }
        queryString += " Order By Usr.IdEmpresa, Usr.Nombre";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection()
                    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            MenuBar.Command cmdActive   = buildMenuCommand(activeTable);
            MenuBar.Command cmdInactive = buildMenuCommand(inactiveTable);

            while (rsRecords.next()) {
                boolean esActivo  = "ACTIVO".equalsIgnoreCase(rsRecords.getString("Estatus"));
                Table   destino   = esActivo ? activeTable   : inactiveTable;
                MenuBar.Command cmd = esActivo ? cmdActive   : cmdInactive;

                MenuBar contactMenu = new MenuBar();
                contactMenu.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
                contactMenu.addStyleName(ValoTheme.MENUBAR_SMALL);
                contactMenu.addStyleName(ValoTheme.MENU_APPEAR_ON_HOVER);
                contactMenu.setSizeUndefined();
                contactMenu.setData(rsRecords.getInt("IdUsuario"));
                MenuBar.MenuItem menuItem = contactMenu.addItem("", FontAwesome.EDIT, null);
                menuItem.addItem("Editar",                               FontAwesome.EYE,           cmd);
                menuItem.addSeparator();
                menuItem.addItem("Permisos asignados",                   FontAwesome.CHECK,         cmd);
                menuItem.addSeparator();
                menuItem.addItem("Proyectos asignados",                  FontAwesome.COG,           cmd);
                menuItem.addSeparator();
                menuItem.addItem("Empresas Asignadas",                   FontAwesome.BUILDING,      cmd);
                menuItem.addSeparator();
                menuItem.addItem("Tipos de Ordenes de Compra Asignados", FontAwesome.CC_MASTERCARD, cmd);
                menuItem.addSeparator();
                menuItem.addItem("Eliminar",                             FontAwesome.TRASH,         cmd);

                destino.addItem(new Object[]{
                    rsRecords.getString("IdUsuario"),
                    rsRecords.getString("EmpresaNombre"),
                    rsRecords.getString("Division"),
                    rsRecords.getString("Usuario"),
                    rsRecords.getString("Nombre"),
                    rsRecords.getString("Perfil"),
                    rsRecords.getString("Estatus"),
                    contactMenu
                }, rsRecords.getInt("IdUsuario"));
            }

            if (activeTable.size() == 0 && inactiveTable.size() == 0) {
                UserForm userForm = new UserForm();
                userForm.idUsuario = 0;
                userForm.usuarioTxt.focus();
                UI.getCurrent().addWindow(userForm);
                Notification.show("No ha creado ningún usuario. Por favor ingrese un usuario.",
                        Notification.Type.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            Logger.getLogger(UsersView.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al intentar leer registros de usuarios.", Notification.Type.ERROR_MESSAGE);
        }
    }

    private MenuBar.Command buildMenuCommand(final Table table) {
        return new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                if (table.getValue() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                MenuBar menuBar = (MenuBar) table.getContainerProperty(table.getValue(), OPTIONS_PROPERTY).getValue();
                if (!menuBar.getItems().get(0).getChildren().contains(selectedItem)) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                    return;
                }

                int    itemId      = selectedItem.getId();
                int    idUsuarioSel = Integer.parseInt(String.valueOf(table.getValue()));
                String nombreSel   = String.valueOf(table.getContainerProperty(table.getValue(), NOMBRE_PROPERTY).getValue());

                Notification.show(itemId + "  " + nombreSel, Notification.Type.TRAY_NOTIFICATION);

                if (itemId == 3) { // editar
                    UserForm userForm = new UserForm();
                    userForm.idUsuario = idUsuarioSel;
                    userForm.fillUserData();
                    userForm.nombreTxt.focus();
                    UI.getCurrent().addWindow(userForm);
                }
                if (itemId == 5) { // permisos
                    UI.getCurrent().addWindow(
                            new UsuarioPermisosForm(idUsuarioSel, nombreSel));
                }
                if (itemId == 7) { // proyectos asignados
                    UI.getCurrent().addWindow(
                            new ProjectsSelectionWindow(String.valueOf(table.getValue())));
                }
                if (itemId == 9) { // empresas asignadas
                    UI.getCurrent().addWindow(
                            new UsuarioPermisosEmpresaForm(idUsuarioSel, nombreSel));
                }
                if (itemId == 11) { // tipos ordenes de compra
                    UI.getCurrent().addWindow(
                            new UsuarioPermisosOrdenCompraForm(idUsuarioSel, nombreSel));
                }
                if (itemId == 13) { // eliminar
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "¿Está seguro de eliminar el registro?",
                            "SI", "NO", new ConfirmDialog.Listener() {
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        Notification.show("NO DISPONIBLE EN ESTA VERSION!", Notification.Type.WARNING_MESSAGE);
                                    }
                                }
                            });
                }
            }
        };
    }

    public boolean exportToExcel() {
        return exportToExcel(getVisibleTable());
    }

    private boolean exportToExcel(Table table) {
        ExcelExport excelExport = new ExcelExport(table);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName("SOPDI_Usuarios.xls");
        new Utileria();
        excelExport.setReportTitle("SOPDI - USUARIOS AL: " + Utileria.getFechaYYYYMMDD_1(new Date()));
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
            return new ByteArrayInputStream(os.toByteArray());
        }
    }

    void setTableTitle(String tableTitle) {
        if (activeTable   != null) activeTable.setCaption(tableTitle);
        if (inactiveTable != null) inactiveTable.setCaption(tableTitle);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - USUARIOS");
    }
}
