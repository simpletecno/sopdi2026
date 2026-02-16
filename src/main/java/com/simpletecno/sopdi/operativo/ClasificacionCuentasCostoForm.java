package com.simpletecno.sopdi.operativo;


import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.vaadin.ui.NumberField;

public class ClasificacionCuentasCostoForm extends Window {

    String queryString = "";
    Statement stQuery = null;
    UI mainUI;
    ResultSet rsRecords = null;

    FormLayout mainForm;
    NumberField idClasificacionTxt;
    TextField descripcionTxt;

    String idClasificacion;

    Button salirBtn;
    Button guardarBtn;

    public ClasificacionCuentasCostoForm(String clasificacion) {

        this.idClasificacion = clasificacion;
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setModal(true);

        mainForm = new FormLayout();
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        idClasificacionTxt = new NumberField("No. Clasificación : ");
        idClasificacionTxt.setWidth("10em");
        idClasificacionTxt.setMaxValue(99);
        idClasificacionTxt.setEnabled(false);

        descripcionTxt = new TextField("Descripción :");
        descripcionTxt.setWidth("10em");

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertarRegistro();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(guardarBtn);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(idClasificacionTxt);
        mainForm.addComponent(descripcionTxt);        
        mainForm.addComponent(buttonsLayout);
        
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);

        Label titleLbl = new Label("Clasificación cuentas de centro de costo");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(mainForm);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);        

        setContent(contentLayout);

        if (!this.idClasificacion.isEmpty()) {
            llenarClasificacion();
        }
    }

    public void llenarClasificacion() {

        String queryString = "";
        queryString = " Select * from clasificacion";
        queryString += " where IdClasificacion =" + idClasificacion;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado                          
                idClasificacionTxt.setValue(rsRecords.getString("IdClasificacion"));
                descripcionTxt.setValue(rsRecords.getString("Descripcion"));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla de clasficacion:" + ex);
            ex.printStackTrace();
        }
    }

    public void insertarRegistro() {
        try {

            if (idClasificacion.isEmpty()) {
                queryString = "Insert Into clasificacion (Descripcion)";
                queryString += " Values (";
                queryString += " '" + descripcionTxt.getValue() + "'";
                queryString += ")";
            } else {
                queryString = " Update clasificacion Set ";
                queryString += " Descripcion = '" + descripcionTxt.getValue()+"'";
                queryString += " Where IdClasificacion = " + idClasificacion;
            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((ClasificacionCuentasCostoView) (mainUI.getNavigator().getCurrentView())).llenarTablaCalificacion();

        } catch (SQLException ex) {
            System.out.println("error"+ex);
            System.out.println("Error al insertar o editar " + queryString);
            
        }
    }
}
