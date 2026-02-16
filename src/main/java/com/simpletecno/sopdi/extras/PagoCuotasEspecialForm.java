package com.simpletecno.sopdi.extras;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;

import java.sql.ResultSet;
import java.sql.Statement;

public class PagoCuotasEspecialForm extends Window {
    UI mainUI;

    String queryString;
    Statement stQuery1;
    ResultSet rsRecords1;


    Button pagarBtn;
    ComboBox cuentaCbx;

    // IdCuenta
    static final String ID_PROVEEDOR = "IdProveedor";
    static final String NIT_PROVEEDOR = "Nit";
    static final String NOMBRE_PROVEEDOR = "Nombre Proveedor";
    static final String RETIENEISR_PROPERTY = "Retenedor Isr";
    static final String RETIENEIVA_PROPERTY = "Retenedor Iva";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";
    static final String DIRECCION_PROPERTY = "DIRECCION";
    static final String CORREO_PROPERTY = "CORREO";

    public PagoCuotasEspecialForm (ComboBox cbx){
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("50%");
        setHeight("20%");
        setModal(true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        mainLayout.setResponsive(true);

        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setResponsive(true);
        layout.setHeight("100%");
        mainLayout.setWidth("100%");

        cuentaCbx = new ComboBox("Cuenta:");
        cuentaCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaCbx.setTextInputAllowed(true);
        cuentaCbx.setNewItemsAllowed(false);
        cuentaCbx.setNullSelectionAllowed(false);
        cuentaCbx.addItem("");
        cuentaCbx.select("");
        cuentaCbx.setRequired(true);
        cuentaCbx.setResponsive(true);
        cuentaCbx.setHeight("100%");
        cuentaCbx.addContainerProperty(ID_PROVEEDOR, Integer.class, 0);
        cuentaCbx.addContainerProperty(NIT_PROVEEDOR, String.class, "");
        cuentaCbx.addContainerProperty(NOMBRE_PROVEEDOR, String.class, "");
        cuentaCbx.addContainerProperty(RETIENEISR_PROPERTY, String.class, "");
        cuentaCbx.addContainerProperty(RETIENEIVA_PROPERTY, String.class, "");
        cuentaCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        cuentaCbx.addContainerProperty(DIRECCION_PROPERTY, String.class, "Guatemala, Guatemala");
        cuentaCbx.addContainerProperty(CORREO_PROPERTY, String.class, "");
        cuentaCbx.setSizeFull();

        llenarProveedores();

        pagarBtn = new Button("Pagar Cuotas");
        pagarBtn.addClickListener(clickEvent -> {
            if(((PagosCuotasInfileView) (mainUI.getNavigator().getCurrentView())).datosValidos(cuentaCbx)){
                ((PagosCuotasInfileView) (mainUI.getNavigator().getCurrentView())).generarDatosDePago();
                if (((PagosCuotasInfileView) (mainUI.getNavigator().getCurrentView())).documentoCeritficaroInfile(cuentaCbx)){
                    ((PagosCuotasInfileView) (mainUI.getNavigator().getCurrentView())).insertarPartidas(cuentaCbx);
                }
            }
            close();
            ((PagosCuotasInfileView) (mainUI.getNavigator().getCurrentView())).llenarCuotasPendientesGrid();
            ((PagosCuotasInfileView) (mainUI.getNavigator().getCurrentView())).llenarCuotasPagadasGrid();
        });
        mainLayout.addComponent(layout);
        mainLayout.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
        layout.addComponents(cuentaCbx, pagarBtn);
        layout.setComponentAlignment(cuentaCbx, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(pagarBtn, Alignment.MIDDLE_CENTER);

        layout.setExpandRatio(cuentaCbx, 1.5f);
        layout.setExpandRatio(pagarBtn, 1.0f);

        setContent(mainLayout);
    }

    private void llenarProveedores(){
        queryString = "SELECT * FROM proveedor ";
        queryString += "WHERE N0 IN (1, 2, 3, 4) ";
        queryString += "AND Inhabilitado = 0 ";
        queryString += "ORDER BY IdProveedor";

        Object itemId;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado
                do {
                    itemId = cuentaCbx.addItem();
                    cuentaCbx.setItemCaption(itemId, rsRecords1.getInt("IDProveedor") + " " + rsRecords1.getString("Nombre"));
                    cuentaCbx.getContainerProperty(itemId, ID_PROVEEDOR).setValue(rsRecords1.getInt("IDProveedor"));
                    cuentaCbx.getContainerProperty(itemId, NIT_PROVEEDOR).setValue(rsRecords1.getString("NIT"));
                    cuentaCbx.getContainerProperty(itemId, NOMBRE_PROVEEDOR).setValue(rsRecords1.getString("Nombre"));
                    cuentaCbx.getContainerProperty(itemId, RETIENEISR_PROPERTY).setValue(rsRecords1.getString("EsAgenteRetenedorIsr"));
                    cuentaCbx.getContainerProperty(itemId, RETIENEIVA_PROPERTY).setValue(rsRecords1.getString("EsAgenteRetenedorIva"));
                    cuentaCbx.getContainerProperty(itemId, NOMBRESINCODIGO_PROPERTY).setValue(rsRecords1.getString("Nombre"));
                    cuentaCbx.getContainerProperty(itemId, DIRECCION_PROPERTY).setValue(rsRecords1.getString("Direccion"));
                    cuentaCbx.getContainerProperty(itemId, CORREO_PROPERTY).setValue(rsRecords1.getString("Email"));
                }while (rsRecords1.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

}
