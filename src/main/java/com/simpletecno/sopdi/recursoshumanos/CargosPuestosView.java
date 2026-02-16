package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.operativo.VisitasView;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CargosPuestosView  extends VerticalLayout implements View {
    Statement stQuery = null;
    ResultSet rsRecords = null;
    String queryString;

    List<Object> cargoList = new ArrayList<>();
    List<Object> cargoTempList = new ArrayList<>();
    List<Object> cargoNoModList = new ArrayList<>();
    UI mainUI;

    static String ID_PROPERTY = "ID";
    static String CARGO_PROPERTY = "Cargo / Puesto";
    static String DESCRIPCION_PROPERTY = "Descripcion";
    static String NUMERO_PROPERTY = "No.";

    HorizontalLayout botonesLayout;
    HorizontalLayout cargoLayout;

    Grid cargoGrid;
    IndexedContainer cargoContainer;

    Button guardarBtn;
    Button eliminarBtn;

    public CargosPuestosView(){
        this.mainUI = UI.getCurrent();

        crearHeader();
        crearCargosGrid();
        crearBotonesLayout();
        llenarCargoGrid();

    }

    private void crearCargosGrid(){
        cargoContainer = new IndexedContainer();
        cargoContainer.addContainerProperty(ID_PROPERTY, String.class, "0");
        cargoContainer.addContainerProperty(CARGO_PROPERTY, String.class, "");
        cargoContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        cargoContainer.addContainerProperty(NUMERO_PROPERTY, Integer.class, 0);

        cargoGrid = new Grid(cargoContainer);

        cargoGrid.setEditorBuffered(false);
        cargoGrid.setHeight("90%");
        cargoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        cargoGrid.setWidth("100%");
        cargoGrid.setImmediate(true);
        cargoGrid.setResponsive(true);
        cargoGrid.setEditorBuffered(false);

        cargoGrid.getColumn(ID_PROPERTY).setHidden(true);
        cargoGrid.getColumn(CARGO_PROPERTY).setExpandRatio(3);
        cargoGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(5);
        cargoGrid.getColumn(NUMERO_PROPERTY).setExpandRatio(1);

        cargoGrid.addItemClickListener((event) -> {
            if (event != null) {
                if (event.isDoubleClick()) {
                    if (event.getItemId() == null) {
                        return;
                    }
                    CargoEditarFrom cargoEditarFrom
                            = new CargoEditarFrom(
                            String.valueOf(event.getItem().getItemProperty(CARGO_PROPERTY).getValue()).trim(),
                            String.valueOf(event.getItem().getItemProperty(DESCRIPCION_PROPERTY).getValue()).trim(),
                            event.getItemId(),
                            cargoNoModList.contains(event.getItemId())
                    );
                    mainUI.addWindow(cargoEditarFrom);
                    cargoEditarFrom.center();
                    cargoEditarFrom.cargoTxt.focus();
                }
            }

        });

        cargoLayout = new HorizontalLayout();
        cargoLayout.setWidth("98%");
        cargoLayout.addStyleName("rcorners3");
        cargoLayout.setResponsive(true);
        cargoLayout.setMargin(true);
        cargoLayout.addComponents(cargoGrid);
        cargoLayout.setComponentAlignment(cargoGrid, Alignment.MIDDLE_CENTER);

        addComponent(cargoLayout);
        setComponentAlignment(cargoLayout, Alignment.MIDDLE_CENTER);
    }

    private void crearBotonesLayout(){
        guardarBtn = new Button("Guardar");

        guardarBtn.addClickListener(clickEvent -> {
            guardar();
        });

        eliminarBtn = new Button("ELIMINAR");
        eliminarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        eliminarBtn.setDescription("ANULAR DOCUMENTO");

        eliminarBtn.addClickListener(clickEvent -> {
            eliminar();
        });


        botonesLayout = new HorizontalLayout(guardarBtn, eliminarBtn);
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        botonesLayout.setComponentAlignment(guardarBtn, Alignment.MIDDLE_RIGHT);
        botonesLayout.setComponentAlignment(eliminarBtn, Alignment.MIDDLE_LEFT);
        addComponent(botonesLayout);
        setComponentAlignment(botonesLayout, Alignment.MIDDLE_CENTER);

    }

    private void crearHeader(){
        Label titleLbl = new Label("CARGOS Y PUESTOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        ComboBox empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("95%");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        empresaCbx.addItem(((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId());
        empresaCbx.setItemCaption(((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId(), ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName());

        empresaCbx.select(((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId());

        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
    }

    public void addEmpyCargo(boolean reset) {
        if (reset){
            cargoContainer.removeAllItems();
            cargoList.clear();
        }
        Object itemId = cargoContainer.addItem();
        cargoTempList.add(itemId);
    }

    private void llenarCargoGrid(){
        queryString = "SELECT ec.*, COUNT(p.IDProveedor) Cuenta ";
        queryString += "FROM empleado_cargo ec ";
        queryString += "LEFT JOIN proveedor p On ec.Cargo = p.Cargo ";
        queryString += "AND ec.IdEmpresa = p.IdEmpresa ";
        queryString += "WHERE ec.IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString += "GROUP BY ec.Cargo";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                Object itemId = cargoContainer.addItem();
                cargoContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                cargoContainer.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Cargo"));
                cargoContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                cargoContainer.getContainerProperty(itemId, NUMERO_PROPERTY).setValue(rsRecords.getInt("Cuenta"));

                if(rsRecords.getInt("Cuenta") > 0) cargoNoModList.add(itemId);
                cargoList.add(itemId);
            }
            addEmpyCargo(false);
        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores/Empleados " + ex1.getMessage());
            ex1.printStackTrace();

        }
    }

    private void guardar(){
        String notificacioMensaje = "";
        if(!cargoList.isEmpty()) {
            for (Object itemid : cargoList) {
                queryString = "UPDATE empleado_cargo SET ";
                queryString += "Cargo = '" + cargoContainer.getContainerProperty(itemid, CARGO_PROPERTY).getValue() + "', ";
                queryString += "Descripcion = '" + cargoContainer.getContainerProperty(itemid, DESCRIPCION_PROPERTY).getValue() + "' ";
                queryString += "WHERE Id = " + cargoContainer.getContainerProperty(itemid, ID_PROPERTY).getValue() + "; ";

                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    stQuery.executeUpdate(queryString);
                } catch (Exception ex) {
                    Logger.getLogger(VisitasView.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Error al Actualizar los puesto: " + ex.getMessage());
                    Notification.show("Error al Actualizar los puesto..!", Notification.Type.ERROR_MESSAGE);
                    break;
                }
            }

            notificacioMensaje += "| Aualizado |";
        }


        queryString = "";
        if(cargoTempList.size() > 1) {
            queryString += "INSERT INTO empleado_cargo (IdEmpresa, Cargo, Descripcion) VALUES ";
            for (Object itemid : cargoTempList) {
                if (!cargoContainer.getContainerProperty(itemid, CARGO_PROPERTY).getValue().equals("")) {
                    queryString += "(" + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId()  + ", ";
                    queryString += "'" + cargoContainer.getContainerProperty(itemid, CARGO_PROPERTY).getValue() + "', ";
                    queryString += "'" + cargoContainer.getContainerProperty(itemid, DESCRIPCION_PROPERTY).getValue() + "'), ";
                    cargoList.add(itemid);
                }
            }
            cargoTempList.removeAll(cargoList);
            try {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString.substring(0, queryString.length() - 2));
            } catch (Exception ex) {
                Logger.getLogger(VisitasView.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error al Agregar los puesto: " + ex.getMessage());
                Notification.show("Error al Agregar los puesto..!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            notificacioMensaje += "| Creado |";
        }

        if(notificacioMensaje.length() > 1){
            Notification.show(notificacioMensaje, Notification.Type.HUMANIZED_MESSAGE);
        }
    }

    private void eliminar(){
        Object itemId = cargoGrid.getSelectedRow();
        if(!cargoNoModList.contains(itemId)) {
            if(cargoList.contains(itemId)) {
                queryString = "DELETE FROM empleado_cargo WHERE id = " + cargoContainer.getContainerProperty(itemId, ID_PROPERTY).getValue();

                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    stQuery.executeUpdate(queryString);
                } catch (Exception ex) {
                    Logger.getLogger(VisitasView.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Error al Eliminar puesto: " + ex.getMessage());
                    Notification.show("Error al Eliminar puesto..!", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                cargoList.remove(itemId);
            }
            cargoTempList.remove(itemId); // Si no lo tiene, no pasa nada

            Notification notif = new Notification("Eliminado con Exito", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.STAR_O);
            notif.show(Page.getCurrent());
        }else {
            Notification notif = new Notification("Existen Empleados con este puesto, no se puede eliminar", Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(1000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        Page.getCurrent().setTitle("Sopdi - Cargos Y Puesto");
    }

}
