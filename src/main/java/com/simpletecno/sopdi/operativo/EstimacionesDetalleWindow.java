/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class EstimacionesDetalleWindow extends Window {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;

    VerticalLayout mainLayout;

    protected static final String CUENTA_PROPERTY        = "Cuenta";
    protected static final String DESCRIPCION_PROPERTY   = "Descripción";
    protected static final String CANTIDAD_PROPERTY      = "Cantidad";
    protected static final String PRECIO_PROPERTY        = "Precio";
    protected static final String PROVISION_PROPERTY     = "Provision";
    protected static final String TOTALEST_PROPERTY      = "TotalEst";

    Label estimacionIdLbl;
    Label titleLbl;
    
    Utileria utileria = new Utileria();
    MarginInfo marginInfo;

    public IndexedContainer detailContainer = new IndexedContainer();
    Grid detailGrid;

    Button exitBtn;

    static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    static DecimalFormat moneyFormat = new DecimalFormat("#,###,##0.00");
              
    UI mainUI;
    
    public EstimacionesDetalleWindow(
            String estimacionId,
            String empresa,
            String empresaNombre,
            String proveedorId,
            String proveedorNombre,
            String idCC,
            String idex
        ) {
        this.mainUI = UI.getCurrent();
        
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        marginInfo = new MarginInfo(true, true, false, true);

        estimacionIdLbl = new Label(empresa + " Proveedor = " + proveedorId + " " + proveedorNombre + " CC = " + idCC);
        estimacionIdLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
  //      estimacionIdLbl.setSizeUndefined();
        estimacionIdLbl.addStyleName("h1_custom");
        
        titleLbl = new Label("Detalle del IDEX " + idex);
        
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h3_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
//        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl, estimacionIdLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);
        titleLayout.setComponentAlignment(estimacionIdLbl, Alignment.TOP_LEFT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        setContent(mainLayout);
        
        createGrids();
        createButtons();

        String queryString =  "SELECGT NoCuenta, Descripcion, ";
        queryString += " SUM(Total / Cantidad) PrecioTotal, SUM(Cantidad) CantidadTotal, SUM(Total) TotalTotal ";
        queryString += " FROM  DetalleItemsCostos";
        queryString += " WHERE IdEmpresa = " + empresa;
        queryString += " AND   IdProveedor = " + proveedorId;
        queryString += " AND   IDEX = '" + idex + "'";
        queryString += " AND   IdCC = '" + idCC + "'";
        queryString += " AND   Tipo IN ('INTINI', 'DOCA')";

System.out.println("\n\n"+queryString);

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                
                do {
                    
                    Object itemId = detailContainer.addItem();

                    detailContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    detailContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    detailContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(rsRecords.getString("CantidadTotal"));
                    detailContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(rsRecords.getString("PrecioTotal"));
                    detailContainer.getContainerProperty(itemId, PROVISION_PROPERTY).setValue("0.00");
                    detailContainer.getContainerProperty(itemId, TOTALEST_PROPERTY).setValue(moneyFormat.format(rsRecords.getDouble("TOTALTOTAL")));

                }while(rsRecords.next());

            }
        } 
        catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void createGrids() {

        Responsive.makeResponsive(this);
        
        setWidth("60%");
        setHeight("70%");

        detailContainer.addContainerProperty(CUENTA_PROPERTY,         String.class, null);
        detailContainer.addContainerProperty(DESCRIPCION_PROPERTY,    String.class, null);
        detailContainer.addContainerProperty(CANTIDAD_PROPERTY,       String.class, null);
        detailContainer.addContainerProperty(PRECIO_PROPERTY,         String.class, null);
        detailContainer.addContainerProperty(PROVISION_PROPERTY,      String.class, null);
        detailContainer.addContainerProperty(TOTALEST_PROPERTY,       String.class, null);
        
        detailGrid = new Grid("Detalle del IDEX", detailContainer);

        detailGrid.setHeightMode(HeightMode.ROW);
        detailGrid.setHeightByRows(10);
        detailGrid.setWidth("100%");
        detailGrid.setResponsive(true);
        detailGrid.setEditorBuffered(false);

        mainLayout.addComponents(detailGrid);
        mainLayout.setComponentAlignment(detailGrid, Alignment.MIDDLE_CENTER);

        detailGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1).setWidth(120);
        detailGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(1).setWidth(200);
        detailGrid.getColumn(CANTIDAD_PROPERTY).setExpandRatio(1).setWidth(100);
        detailGrid.getColumn(PRECIO_PROPERTY).setExpandRatio(1).setWidth(100);
        detailGrid.getColumn(PROVISION_PROPERTY).setExpandRatio(1).setWidth(100);
        detailGrid.getColumn(TOTALEST_PROPERTY).setExpandRatio(1).setWidth(110);
        
        detailGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if(TOTALEST_PROPERTY.equals(cellReference.getPropertyId()) 
                    || PRECIO_PROPERTY.equals(cellReference.getPropertyId())
                    || PROVISION_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            }
            else {
                return null;
            }
        });
        
    }

    private void createButtons() {

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
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }
}
