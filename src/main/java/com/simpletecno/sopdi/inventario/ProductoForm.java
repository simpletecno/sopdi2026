package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class ProductoForm extends Window {

    String idProducto;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;
    String queryString = "";

    VerticalLayout mainForm;

    TextField grupoTxt;
    ComboBox n1Cbx;
    TextField grupo2Txt;
    ComboBox n2Cbx;
    TextField grupo3Txt;
    TextField noCuentaTxt;
    TextField descripcionTxt;
    ComboBox unidadCbx;
    TextField codigoBarrasTxt;
    ComboBox estatusCbx;
    NumberField montoTxt;

    Button guardarBtn;
    Button salirBtn;

    public ProductoForm(String idProducto) {
        this.mainUI = UI.getCurrent();
        this.idProducto = idProducto;
        setWidth("68%");
        setHeight("55%");
        setResponsive(true);
        setModal(true);

        mainForm = new VerticalLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");

        Label titleLbl;

        if (this.idProducto.trim().isEmpty()) {
            titleLbl = new Label("NUEVO PRODUCTO");
            titleLbl.setSizeUndefined();
            titleLbl.addStyleName(Runo.LABEL_H2);
        } else {
            titleLbl = new Label("EDITAR PRODUCTO");
            titleLbl.setSizeUndefined();
            titleLbl.addStyleName(Runo.LABEL_H2);
        }

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        crearComponents();

        setContent(mainForm);

        if (!idProducto.trim().isEmpty()) {
            llenarDatosProducto();
        }

    }

    public void crearComponents() {

        HorizontalLayout layotsHorizontal = new HorizontalLayout();
        layotsHorizontal.setSpacing(true);
        layotsHorizontal.setWidth("80%");

        FormLayout formRight = new FormLayout();
        formRight.setSpacing(true);
        formRight.setWidth("50%");
        formRight.setResponsive(true);

        FormLayout formLeft = new FormLayout();
        formLeft.setSpacing(true);
        formLeft.setWidth("50%");
        formLeft.setResponsive(true);

        grupoTxt = new TextField("GRUPO : ");
        grupoTxt.clear();
        grupoTxt.setWidth("15em");

        n1Cbx = new ComboBox("N1 : ");
        n1Cbx.setWidth("15em");
        n1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        n1Cbx.addItem("Equipo");
        n1Cbx.addItem("Servicios, Supervision y Mano Obra");
        n1Cbx.addItem("Materia Prima");
        n1Cbx.addItem("Materia Transformada");
        n1Cbx.addItem("Materia Transformada");

        grupo2Txt = new TextField("GRUPO 2 : ");
        grupo2Txt.setWidth("15em");

        n2Cbx = new ComboBox("N2: ");
        n2Cbx.setWidth("15em");
        n2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        llenarComboN2();

        grupo3Txt = new TextField("GRUPO 3 : ");
        grupo3Txt.setWidth("15em");

        noCuentaTxt = new TextField("NO CUENTA : ");
        noCuentaTxt.setWidth("15em");

        descripcionTxt = new TextField("DESCRIPCIÓN : ");
        descripcionTxt.setWidth("15em");

        unidadCbx = new ComboBox("UNIDAD : ");
        unidadCbx.setWidth("15em");
        unidadCbx.setFilteringMode(FilteringMode.CONTAINS);

        codigoBarrasTxt = new TextField("CODIGO BARRAS : ");
        codigoBarrasTxt.setWidth("15em");

        estatusCbx = new ComboBox("ESTATUS : ");
        estatusCbx.addItem("ACTIVO");
        estatusCbx.addItem("INCACTIVO");
        estatusCbx.select("ACTIVO");
        estatusCbx.setWidth("15em");
        estatusCbx.setFilteringMode(FilteringMode.CONTAINS);

        montoTxt = new NumberField("Precio : ");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("15em");
        montoTxt.setValue(0.00);

        llenarComboN1();
        llenarComboN2();
        llenarComboUnidad();

        formLeft.addComponents(grupoTxt, n1Cbx, grupo2Txt, n2Cbx, grupo3Txt, descripcionTxt);
        formRight.addComponents(noCuentaTxt, unidadCbx, codigoBarrasTxt, estatusCbx, montoTxt);

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
                insertarProducto();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setResponsive(true);
        buttonsLayout.setSpacing(true);

        buttonsLayout.addComponents(salirBtn, guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        layotsHorizontal.addComponents(formLeft, formRight);
        layotsHorizontal.setComponentAlignment(formLeft, Alignment.MIDDLE_CENTER);
        layotsHorizontal.setComponentAlignment(formRight, Alignment.MIDDLE_CENTER);

        mainForm.addComponent(layotsHorizontal);
        mainForm.setComponentAlignment(layotsHorizontal, Alignment.TOP_CENTER);

        mainForm.addComponent(buttonsLayout);
        mainForm.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarComboN1() {
        queryString = " SELECT N1 FROM inv_producto ";
        queryString += " GROUP BY N1";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                n1Cbx.addItem(rsRecords.getString("N1"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar N1: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboN2() {
        queryString = " SELECT N2 FROM inv_producto ";
        queryString += " GROUP BY N2";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                n2Cbx.addItem(rsRecords.getString("N2"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar N2: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboUnidad() {
        queryString = " SELECT Unidad FROM inv_producto ";
        queryString += " GROUP BY Unidad";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                unidadCbx.addItem(rsRecords.getString("Unidad"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Unidad: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarDatosProducto() {
        queryString = " SELECT * FROM inv_producto ";
        queryString += " WHERE IdProducto = " + idProducto;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                
                grupoTxt.setValue(rsRecords.getString("Grupo"));
                n1Cbx.select(rsRecords.getString("N1"));
                grupo2Txt.setValue(rsRecords.getString("Grupo2"));
                n2Cbx.setValue(rsRecords.getString("N2"));
                grupo3Txt.setValue(rsRecords.getString("Grupo3"));
                noCuentaTxt.setValue(rsRecords.getString("NoCuenta"));
                descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                unidadCbx.setValue(rsRecords.getString("Unidad"));
                if (rsRecords.getString("CodigoBarras") ==null) {
                    codigoBarrasTxt.setValue("");
                }else{
                    codigoBarrasTxt.setValue(rsRecords.getString("CodigoBarras"));
                }
              
                estatusCbx.select(rsRecords.getString("Estatus"));

            }

        } catch (Exception ex1) {
            System.out.println("Error al listar datos del producto" + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertarProducto() {

        try {

            if (grupoTxt.getValue().trim().isEmpty()) {
                Notification.show("Por favor ingrese el grupo", Notification.Type.WARNING_MESSAGE);
                grupoTxt.focus();
                return;
            }

            if (n1Cbx.getValue() == null) {
                Notification.show("Por favor seleccione el N1", Notification.Type.WARNING_MESSAGE);
                n1Cbx.focus();
                return;
            }
            if (grupo2Txt.getValue().trim().isEmpty()) {
                Notification.show("Por favor ingrese el grupo 2", Notification.Type.WARNING_MESSAGE);
                grupo2Txt.focus();
                return;
            }
            if (n2Cbx.getValue() == null) {
                Notification.show("Por favor seleccione el N2", Notification.Type.WARNING_MESSAGE);
                n2Cbx.focus();
                return;
            }

            if (grupo3Txt.getValue().trim().isEmpty()) {
                Notification.show("Por favor ingrese el grupo 3", Notification.Type.WARNING_MESSAGE);
                grupo3Txt.focus();
                return;
            }
            if (noCuentaTxt.getValue().trim().isEmpty()) {
                Notification.show("Por favor ingrese un No cuenta del producto", Notification.Type.WARNING_MESSAGE);
                noCuentaTxt.focus();
                return;
            }

            if (descripcionTxt.getValue().trim().isEmpty()) {
                Notification.show("Por favor ingrese una descripción del producto", Notification.Type.WARNING_MESSAGE);
                descripcionTxt.focus();
                return;
            }

            if (unidadCbx.getValue() == null) {
                Notification.show("Por favor seleccione una unidad para el producto", Notification.Type.WARNING_MESSAGE);
                unidadCbx.focus();
                return;
            }

            if (estatusCbx.getValue() == null) {
                Notification.show("Por favor seleccione un proveedor", Notification.Type.WARNING_MESSAGE);
                estatusCbx.focus();
                return;
            }

            if (idProducto.trim().isEmpty()) { /// NUEVO REGISTRO

                queryString = "Insert into inv_producto ";
                queryString += "(Grupo, N1, Grupo2, N2, Grupo3, NoCuenta, Descripcion, Unidad, CodigoBarras, Estatus, PrecioReferencia)";
                queryString += " Values ";
                queryString += "('" + grupoTxt.getValue() + "'";
                queryString += ",'" + n1Cbx.getValue() + "'";
                queryString += ",'" + grupo2Txt.getValue() + "'";
                queryString += ",'" + n2Cbx.getValue() + "'";
                queryString += ",'" + grupo3Txt.getValue() + "'";
                queryString += ",'" + noCuentaTxt.getValue() + "'";
                queryString += ",'" + descripcionTxt.getValue() + "'";
                queryString += ",'" + unidadCbx.getValue() + "'";
                queryString += ",'" + codigoBarrasTxt.getValue() + "'";
                queryString += ",'" + estatusCbx.getValue() + "'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow();
                queryString += ")";

            } else {

                queryString = "UPDATE inv_producto SET ";
                queryString += "  Grupo = '" + grupoTxt.getValue() + "'";
                queryString += ", N1 = '" + n1Cbx.getValue() + "'";
                queryString += ", Grupo2 = '" + grupo2Txt.getValue() + "'";
                queryString += ", N2 = '" + n2Cbx.getValue() + "'";
                queryString += ", Grupo3 = '" + grupo3Txt.getValue() + "'";
                queryString += ", NoCuenta = '" + noCuentaTxt.getValue() + "'";
                queryString += ", Descripcion = '" + descripcionTxt;
                queryString += ", Unidad = '" + unidadCbx.getValue() + "'";
                queryString += ", CodigoBarras = '" + codigoBarrasTxt.getValue() + "'";
                queryString += ", Estatus = '" + estatusCbx.getValue() + "'";
                queryString += ", PrecioReferencia = " + montoTxt.getDoubleValueDoNotThrow();
                queryString += " Where IdProducto = " + idProducto;

            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((InvProductoView) (mainUI.getNavigator().getCurrentView())).llenarTablaProductos();
            Notification notif = new Notification("REGISTRO AGREGADO o MODIFICADO EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            close();

        } catch (SQLException ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update o insert en tabla inv_prodcutos " + ex);
            ex.printStackTrace();
        }
    }

}
