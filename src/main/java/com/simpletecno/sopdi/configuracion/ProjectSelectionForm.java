/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class ProjectSelectionForm extends VerticalLayout {
    
    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    
    static final String LOGO_PROPERTY = "Logo";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String RESERVA_PROPERTY = "Reserva";
    static final String COSTO_TRAMITE_PROPERTY = "Costo tramite";
        
    VerticalLayout selectionPanel = new VerticalLayout();
    
    public Table projectsTable;
    
    Button selectBtn;
    Button exitBtn;
        
    public static Locale locale = new Locale("ES","GT"); 
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
                
    UI mainUI;
       
    public ProjectSelectionForm() {

        mainUI = UI.getCurrent();
        
        setSizeFull();
    
        Component selectionForm = buildSelectionForm();
        addComponent(selectionForm);
        setComponentAlignment(selectionForm, Alignment.MIDDLE_CENTER);

        Responsive.makeResponsive(selectionPanel);
        
    }

    private Component buildSelectionForm() {
            
        selectionPanel.setSizeUndefined();
        selectionPanel.setSpacing(true);
        selectionPanel.setImmediate(true);
        Responsive.makeResponsive(selectionPanel);
//        selectionPanel.addStyleName("login-panel");
        selectionPanel.setWidth("60%");

        selectionPanel.addComponent(createTable());

        selectBtn    = new Button("Seleccionar");
        selectBtn.setIcon(FontAwesome.HAND_O_UP);
        selectBtn.setClickShortcut(ShortcutAction.KeyCode.ENTER);       
        selectBtn.focus();
        selectBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        selectBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(projectsTable.size() > 0) {
                    if(projectsTable.getValue() != null) {
                        setProject();
                    }
                }
            }
        });

        exitBtn    = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        exitBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                ((SopdiUI) mainUI).logOff();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(selectBtn);
        buttonsLayout.setComponentAlignment(selectBtn, Alignment.BOTTOM_CENTER);
        
        selectionPanel.addComponent(buttonsLayout);
        selectionPanel.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        
        fillProyectoTable();

        return selectionPanel;
    }

    private Component createTable() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners4");
        
        projectsTable = new Table("Seleccione el proyecto ");
        projectsTable.addStyleName(ValoTheme.TABLE_SMALL);
        projectsTable.addStyleName(ValoTheme.TABLE_COMPACT);
        projectsTable.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        projectsTable.setImmediate(true);
        projectsTable.setSelectable(true);

        reportLayout.addComponent(projectsTable);
        reportLayout.setComponentAlignment(projectsTable, Alignment.MIDDLE_CENTER);

        projectsTable.setWidth("100%");
        projectsTable.setPageLength(5);
        
//        projectsTable.addContainerProperty(CODIGO_PROPERTY,    String.class, null);
        projectsTable.addContainerProperty(LOGO_PROPERTY,      Image.class, null);
        projectsTable.addContainerProperty(NOMBRE_PROPERTY,    String.class, null);
        projectsTable.addContainerProperty(MONEDA_PROPERTY,       String.class, null);
        projectsTable.addContainerProperty(RESERVA_PROPERTY,       String.class, null);
        projectsTable.addContainerProperty(COSTO_TRAMITE_PROPERTY, String.class, null);

        projectsTable.setColumnAlignments(new Table.Align[] { 
                Table.Align.CENTER, Table.Align.LEFT,  Table.Align.LEFT,
                Table.Align.RIGHT,  Table.Align.RIGHT
        });
        
        projectsTable.addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    if(projectsTable.size() > 0) {
                        if(projectsTable.getValue() != null) {
                            setProject();
                        }
                    }
                }
            }
        });        

        projectsTable.setColumnWidth(MONEDA_PROPERTY, 0);
        projectsTable.setColumnWidth(RESERVA_PROPERTY, 0);
        projectsTable.setColumnWidth(COSTO_TRAMITE_PROPERTY, 0);
        
        return reportLayout;
    }

    public void fillProyectoTable() {
        
        projectsTable.removeAllItems();
                
        String queryString = "SELECT Pro.*";
        queryString += " FROM  proyecto_usuario ProUsr";
        queryString += " INNER JOIN proyecto Pro ON Pro.IdProyecto = ProUsr.IdProyecto";
        queryString += " WHERE ProUsr.IdUsuario = " + ((SopdiUI)mainUI).sessionInformation.getStrUserId();

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                int primerRegistro = rsRecords.getInt("IdProyecto");
                 
                Image proyectoLogo;
                
                do {
                    final byte docBytes[] = rsRecords.getBytes("Logo");
                    StreamResource logoStreamResource = null;

                    if(docBytes != null ) {
                        logoStreamResource = new StreamResource(
                            new StreamResource.StreamSource() {
                                @Override
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(docBytes);
                                }
                            },rsRecords.getString("IdProyecto")
                        );
                    }

                    proyectoLogo = new Image(null, logoStreamResource);
                    proyectoLogo.setImmediate(true);
                    proyectoLogo.setWidth("40px");
                    proyectoLogo.setHeight("40px"); 
                                        
                    projectsTable.addItem(new Object[] {    
  //                      rsRecords.getString("IdProyecto"),
                        proyectoLogo,
                       rsRecords.getString("Nombre"),
                       rsRecords.getString("Moneda"),
                       numberFormat.format(rsRecords.getDouble("MontoReserva")),
                       numberFormat.format(rsRecords.getDouble("CostoTramite"))
                    }, rsRecords.getInt("IdProyecto"));

                }while(rsRecords.next());

                projectsTable.select(primerRegistro);
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectSelectionForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de proyectos : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de proyectos..!", Notification.Type.ERROR_MESSAGE);
        }

    } 
   
    private void setProject() {
        ((SopdiUI)mainUI).sessionInformation.setStrProjectId(String.valueOf(projectsTable.getValue()));   

        ((SopdiUI)mainUI).sessionInformation.setStrProjectName(String.valueOf(projectsTable.getContainerProperty(projectsTable.getValue(), NOMBRE_PROPERTY).getValue()));

        try {
            rsRecords.first();

            do {
                if(rsRecords.getString("IdProyecto").compareTo(String.valueOf(projectsTable.getValue())) == 0) {
                    final byte docBytes[] = rsRecords.getBytes("Logo");
                    StreamResource logoStreamResource = null;

                    if(docBytes != null ) {
                        logoStreamResource = new StreamResource(
                            new StreamResource.StreamSource() {
                                @Override
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(docBytes);
                                }
                            },rsRecords.getString("IdProyecto")
                        );
                    }
                    ((SopdiUI)mainUI).sessionInformation.setProjectStreamResource(logoStreamResource);
                    ((SopdiUI)mainUI).sessionInformation.setStrProjectCurrency(rsRecords.getString("Moneda"));
                    ((SopdiUI)mainUI).sessionInformation.setStrProjectReserveAmount(numberFormat.format(rsRecords.getDouble("MontoReserva")));
                    ((SopdiUI)mainUI).sessionInformation.setStrProjectGestionAmount(numberFormat.format(rsRecords.getDouble("CostoTramite")));
                    ((SopdiUI)mainUI).sessionInformation.setDblBudgetCharge(rsRecords.getDouble("FactorPresupuestoCliente"));
                    ((SopdiUI)mainUI).sessionInformation.setDiasAntesHoyFechaTareaProgramada(rsRecords.getInt("DiasAntesHoyFechaTareaProgramada"));
                    ((SopdiUI)mainUI).sessionInformation.setDiasDespuesHoyFechaTareaProgramada(rsRecords.getInt("DiasDespuesHoyFechaTareaProgramada"));
                }
            } while(rsRecords.next());

        } catch(Exception ex1) {
            ex1.printStackTrace();
        }

        ((SopdiUI)mainUI).buildMainView();
    } 
}