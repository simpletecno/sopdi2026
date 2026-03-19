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
import java.text.SimpleDateFormat;
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
    
    public Statement stQuery = null;
    public ResultSet rsRecords  = null;
    public ResultSet rsRecords1 = null;
    
    protected static final String CODIGO_PROPERTY    = "Id";
    protected static final String EMPRESA_PROPERTY   = "Empresa";
    protected static final String DIVISION_PROPERTY   = "División o Depto.";
    protected static final String USUARIO_PROPERTY   = "Usuario";
    protected static final String NOMBRE_PROPERTY    = "Nombre";
    protected static final String PERFIL_PROPERTY    = "Perfil";
    protected static final String ESTATUS_PROPERTY   = "Estatus";
    protected static final String OPTIONS_PROPERTY = "-";

    Button newBtn;
    Button exportExcelBtn;

    TextField nombreTxt;
    public Table usersTable;
            
    final UI mainUI = UI.getCurrent();
       
    public UsersView() {
        
        setResponsive(true);
        MarginInfo marginInfo = new MarginInfo(true,true,false,true); 

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setMargin(false);
        filterLayout.setSpacing(true);
        filterLayout.addStyleName("rcorners3");
        filterLayout.setResponsive(true);
        
        addComponent(filterLayout);
        setComponentAlignment(filterLayout, Alignment.TOP_CENTER);

        Label viewCaption = new Label("Usuarios del sistema");

        viewCaption.setStyleName(ValoTheme.LABEL_H3);
        
        nombreTxt = new TextField("Nombre");
        nombreTxt.setDescription("Buscar por nombre");
        nombreTxt.setWidth("100%");
        nombreTxt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                fillReportTable();
            }
        });
        
//        nombreTxt.focus();

        filterLayout.addComponent(viewCaption);        
        filterLayout.addComponent(nombreTxt);

        createReportTable();
                            
        newBtn    = new Button("Nuevo");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.setWidth(130,Sizeable.UNITS_PIXELS);
//        newBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        newBtn.setDescription("Registrar nuevo cliente");
        newBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                UserForm userForm = new UserForm();
                userForm.idUsuario = 0;
                userForm.nombreTxt.focus();
                UI.getCurrent().addWindow(userForm);
            }
        });

        exportExcelBtn    = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120,Sizeable.UNITS_PIXELS);
        exportExcelBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(usersTable.size() > 0) {
//                    PronetWebPayMain.getInstance().mainWindow.getWindow().showNotification("EN CONSTRUCCION!");            
                    exportToExcel();
                }
            }
        });
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(exportExcelBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        fillReportTable();
    }
            
    public void createReportTable() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("95%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);

        usersTable = new Table("Usuarios del sistema ");

        reportLayout.addComponent(usersTable);
        reportLayout.setComponentAlignment(usersTable, Alignment.MIDDLE_CENTER);

        usersTable.setWidth("100%");
        usersTable.setResponsive(true);
        usersTable.setPageLength(20);
        
        usersTable.setImmediate(true);
        usersTable.setSelectable(true);
        
        usersTable.addContainerProperty(CODIGO_PROPERTY,    String.class, null);
        usersTable.addContainerProperty(EMPRESA_PROPERTY,   String.class, null);
        usersTable.addContainerProperty(DIVISION_PROPERTY,   String.class, null);
        usersTable.addContainerProperty(USUARIO_PROPERTY,   String.class, null);
        usersTable.addContainerProperty(NOMBRE_PROPERTY,    String.class, null);
//        usersTable.addContainerProperty(EMAIL_PROPERTY,     String.class, null);
        usersTable.addContainerProperty(PERFIL_PROPERTY,    String.class, null);
        usersTable.addContainerProperty(ESTATUS_PROPERTY,   String.class, null);
        usersTable.addContainerProperty(OPTIONS_PROPERTY,   MenuBar.class, null);

        usersTable.setColumnAlignments(new Table.Align[] { 
                Table.Align.CENTER, Table.Align.LEFT,  Table.Align.LEFT, Table.Align.LEFT,
                Table.Align.LEFT,   /*Table.Align.LEFT,*/  Table.Align.LEFT,
                Table.Align.CENTER, Table.Align.CENTER
        });

        addComponent(reportLayout);        
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void fillReportTable() {
        
        usersTable.removeAllItems();        
        usersTable.setFooterVisible(false);
                
        String queryString = "";
        
        queryString =  "Select Usr.*, Emp.Nombre EmpresaNombre ";
        queryString += " From  usuario Usr";
        queryString += " Inner Join empresa Emp On Emp.IdEmpresa = Usr.IdEmpresa";
        queryString += " Where Usr.IdEmpresa  > 0"; //solo para tener los And's
        if(((SopdiUI) mainUI).sessionInformation.getStrUserProfile().compareTo("DESARROLLADOR") == 0) {
            queryString += " And Usr.IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrCompanyId();
        }
        if(nombreTxt != null) {
            if(!nombreTxt.getValue().trim().isEmpty()) {
                queryString += " And Usr.Nombre Like '%" + nombreTxt.getValue().trim() + "%'";
            }
        }
        queryString += " Order By Usr.IdEmpresa, Usr.Nombre";

//System.out.println("\n\n"+queryString);

        try {
            
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                // Define a common menu command for all the menu items.
                MenuBar.Command mycommand = new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        if(usersTable.getValue() != null) {
                            MenuBar menuBar = (MenuBar)usersTable.getContainerProperty(usersTable.getValue(), OPTIONS_PROPERTY).getValue();
                            if(menuBar.getItems().get(0).getChildren().contains(selectedItem)) {
                                String msg = String.valueOf(selectedItem.getId()) + "  ";
                                msg += usersTable.getContainerProperty(usersTable.getValue(), NOMBRE_PROPERTY).getValue();
                                Notification.show(msg, Notification.Type.TRAY_NOTIFICATION);

                                if(selectedItem.getId() == 3) { // editar
                                    UserForm userForm = new UserForm();
                                    userForm.idUsuario = Integer.parseInt(String.valueOf(usersTable.getValue()));
                                    userForm.fillUserData();
                                    userForm.nombreTxt.focus();
                                    UI.getCurrent().addWindow(userForm);
                                }
                                if(selectedItem.getId() == 5) { // permisos
                                    UsuarioPermisosForm usuarioPermisosForm =
                                            new UsuarioPermisosForm(
                                                    Integer.parseInt(String.valueOf(usersTable.getValue())),
                                                    String.valueOf(usersTable.getContainerProperty(usersTable.getValue(), NOMBRE_PROPERTY).getValue()));
                                    UI.getCurrent().addWindow(usuarioPermisosForm);
                                }
                                if(selectedItem.getId() == 7) { // proyectos asignados
                                    ProjectsSelectionWindow projectsSelectionWindow =
                                            new ProjectsSelectionWindow(String.valueOf(usersTable.getValue()));
                                    UI.getCurrent().addWindow(projectsSelectionWindow);
                                }
                                if(selectedItem.getId() == 9) { // empresas asignadas
                                    UsuarioPermisosEmpresaForm usuarioPermisosEmpresaForm =
                                            new UsuarioPermisosEmpresaForm(
                                                    Integer.parseInt(String.valueOf(usersTable.getValue())),
                                                    String.valueOf(usersTable.getContainerProperty(usersTable.getValue(), NOMBRE_PROPERTY).getValue()));
                                    UI.getCurrent().addWindow(usuarioPermisosEmpresaForm);
                                }
                                if(selectedItem.getId() == 11) { // tipos ordenes de compra asignados
                                    UsuarioPermisosOrdenCompraForm usuarioPermisosOrdenCompraForm =
                                            new UsuarioPermisosOrdenCompraForm(
                                                    Integer.parseInt(String.valueOf(usersTable.getValue())),
                                                    String.valueOf(usersTable.getContainerProperty(usersTable.getValue(), NOMBRE_PROPERTY).getValue()));
                                    UI.getCurrent().addWindow(usuarioPermisosOrdenCompraForm);
                                }
                                if(selectedItem.getId() == 13) { // eliminar
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
                    contactMenu.setData(rsRecords.getInt("IdUsuario"));
                    MenuBar.MenuItem menuItem = contactMenu.addItem("", FontAwesome.EDIT, null);
                    menuItem.addItem("Editar", FontAwesome.EYE, mycommand);                    
                    menuItem.addSeparator();
                    menuItem.addItem("Permisos asignados", FontAwesome.CHECK, mycommand);
                    menuItem.addSeparator();
                    menuItem.addItem("Proyectos asignados", FontAwesome.COG, mycommand);
                    menuItem.addSeparator();
                    menuItem.addItem("Empresas Asignadas", FontAwesome.BUILDING, mycommand);
                    menuItem.addSeparator();
                    menuItem.addItem("Tipos de Ordenes de Compra Asignados", FontAwesome.CC_MASTERCARD, mycommand);
                    menuItem.addSeparator();
                    menuItem.addItem("Eliminar", FontAwesome.TRASH, mycommand);
                    
                    usersTable.addItem(new Object[] {    
                        rsRecords.getString("IdUsuario"),
                        rsRecords.getString("EmpresaNombre"),
                        rsRecords.getString("Division"),
                        rsRecords.getString("Usuario"),
                        rsRecords.getString("Nombre"),
//                        rsRecords.getString("Email"),
                        rsRecords.getString("Perfil"),
                        rsRecords.getString("Estatus"),
                        contactMenu
                    }, rsRecords.getInt("IdUsuario"));

                }while(rsRecords.next());

                if(rsRecords.first()) {
                    usersTable.select(rsRecords.getInt("IdUsuario"));
                }
            }
            else {
                UserForm userForm = new UserForm();
                userForm.idUsuario = 0;
                userForm.usuarioTxt.focus();
                UI.getCurrent().addWindow(userForm);

                Notification.show("No ha creado ningun usuario asesor para este proyecto,  por favor ingrese un usuario asesor.", Notification.Type.WARNING_MESSAGE);
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(UsersView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de usuarios : " + ex.getMessage());
            Notification.show("Error al intentar leer registros usuarios..!", Notification.Type.ERROR_MESSAGE);
        }

    } 
        
    public boolean exportToExcel() {
        ExcelExport excelExport;

        excelExport = new ExcelExport(usersTable);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName("SOPDI_Usuarios.xls");
        
        String mainTitle = "SOPDI - USUARIOS AL: "  + new Utileria().getFechaYYYYMMDD_1(new Date());
  
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
        if(usersTable != null) {
            usersTable.setCaption(tableTitle);
            usersTable.setDescription(tableTitle);
        }            
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - USUARIOS ");

    }
}