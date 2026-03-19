/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
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
public class EstimacionesFacturaWindow extends Window {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;

    VerticalLayout mainLayout;

    Label estimacionIdLbl;
    Label titleLbl;
    
    Utileria utileria = new Utileria();
    MarginInfo marginInfo;

    TextField serieTxt;
    TextField numeroTxt;  
    DateField fechaDt;
    
    Button saveBtn;
    Button exitBtn;

    static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    
    String estimacionId;
    String empresa;
    String empresaNombre;
    String proveedorId;
    String proveedorNombre;
          
    UI mainUI;
    
    public EstimacionesFacturaWindow(
            String estimacionId, 
            String empresa, 
            String empresaNombre,
            String proveedorId,
            String proveedorNombre) {
        this.mainUI = UI.getCurrent();
        this.estimacionId = estimacionId;
        this.empresa = empresa;
        this.empresaNombre = empresaNombre;
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
        
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        Responsive.makeResponsive(this);
        
        setWidth("50%");
        setHeight("50%");

        marginInfo = new MarginInfo(true, true, false, true);

        estimacionIdLbl = new Label(estimacionId);
        estimacionIdLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
        estimacionIdLbl.addStyleName("h1_custom");
        
        titleLbl = new Label("RELACIONAR FACTURA CON ESTIMACION");
        
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
//        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl, estimacionIdLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);
        titleLayout.setComponentAlignment(estimacionIdLbl, Alignment.TOP_LEFT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        setContent(mainLayout);

        serieTxt = new TextField("Serie : ");
        serieTxt.setWidth("5em");
        serieTxt.focus();
        serieTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        numeroTxt = new TextField("Número : ");
        numeroTxt.setWidth("15em");
        
        fechaDt = new DateField("Fecha:");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setValue(new Date());

        mainLayout.addComponents(serieTxt, numeroTxt, fechaDt);
        mainLayout.setComponentAlignment(serieTxt, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(numeroTxt, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(fechaDt, Alignment.TOP_CENTER);

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
//        saveBtn.setWidth(175, Sizeable.UNITS_PIXELS);
        saveBtn.setDescription("Guardar");
        saveBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                updateRecord();
            }
        });

        exitBtn = new Button("Salir");
        exitBtn.setIcon(FontAwesome.EDIT);
//        exitBtn.setWidth(130, Sizeable.UNITS_PIXELS);
        exitBtn.setDescription("Salir");
        exitBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de SALIR?",
                    "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            close();
                        }
                    }
                });                       
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    private void updateRecord() {
     
        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de relacionar la factura ?",
                "SI", "NO", new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    String queryString;

                    try {
                        queryString = "Update DocumentosContablesAplicados ";
                        queryString += " Set Serie = '" + serieTxt.getValue()+ "'";
                        queryString += ",NoDocumento = '" + numeroTxt.getValue() + "'";
                        queryString += ",EstimacionEstatus = 'PAGADA'";
                        queryString += ",Fecha = '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                        queryString += " Where Estimacion  = " + estimacionId;
                        queryString += " And   IdEmpresa = " + empresa;
                        queryString += " And   IdProveedor = " + proveedorId;

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                        if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("EstimacionesView")) {

                            ((EstimacionesView) (mainUI.getNavigator().getCurrentView())).container.getContainerProperty(
                                    ((EstimacionesView) (mainUI.getNavigator().getCurrentView())).estimacionesGrid.getSelectedRow(), EstimacionesView.ESTATUS_PROPERTY).setValue("PAGADA");
                        }

                        Notification.show("OPERACION EXITOSA", Notification.Type.HUMANIZED_MESSAGE);
                        
                        close();
                    }
                    catch (Exception ex) {
                        Logger.getLogger(EstimacionesFacturaWindow.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("Error al actualizar registros de DocumentosContablesAplicados : " + ex.getMessage());
                        Notification.show("Error al intentar actualizar registros de DocumentosContablesAplicados..!", Notification.Type.ERROR_MESSAGE);
                    }

                }
            }
        });
        
    }   
        
}
