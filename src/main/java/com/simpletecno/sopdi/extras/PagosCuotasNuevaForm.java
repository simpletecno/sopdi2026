package com.simpletecno.sopdi.extras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PagosCuotasNuevaForm extends Window {
    UI mainUI;

    String queryString;
    Statement stQuery1;
    ResultSet rsRecords1;

    String idProveedor;
    String nombreProveedor;

    VerticalLayout mainLayout;

    Label tituloLbl;

    static final String ID_CUOTA = "IdCuota";
    static final String TIPO_CUOTA = "Tipo Cuota";
    static final String MONTO_CUOTA = "Monto Cuota";

    static final String ID_PROVEEDOR = "IdProveedor";
    static final String NOMBRE_PROVEEDOR = "Nombre Proveedor";

    ComboBox cuotaCbx;
    ComboBox unidadCbx;
    NumberField montoTxt;
    TextField referenciaTxt;
    DateField fechaPagoInicioDt;
    DateField fechaCorteMultaAtrasoDt;
    NumberField montoMultaAtrasoTxt;

    Button agregarCuotaBtn;

    public PagosCuotasNuevaForm(String idProveedor, String nombreProveedor){
        this.idProveedor = idProveedor;
        this.nombreProveedor = nombreProveedor;

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("60%");
        setHeight("35%");
        setModal(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        crearFormulario();

        llenarProveedores();
        llenaCuotas();
    }

    private void crearFormulario(){
        HorizontalLayout layout1 = new HorizontalLayout();
        layout1.setMargin(new MarginInfo(false, true, false, true));
        layout1.setSpacing(true);
        layout1.setResponsive(true);
        layout1.setWidth("100%");

        HorizontalLayout layout2 = new HorizontalLayout();
        layout2.setMargin(new MarginInfo(false, true, false, true));
        layout2.setSpacing(true);
        layout2.setResponsive(true);
        layout2.setWidth("100%");
        layout2.setHeight("100%");

        HorizontalLayout layout3 = new HorizontalLayout();
        layout3.setMargin(new MarginInfo(false, true, true, true));
        layout3.setSpacing(true);
        layout3.setResponsive(true);
        layout3.setWidth("100%");

        tituloLbl = new Label("Cuenta:  " + idProveedor + " - " + nombreProveedor);
        tituloLbl.setWidth("100%");

        cuotaCbx = new ComboBox("Tipo Cuota:");
        cuotaCbx.setWidth("100%");
        cuotaCbx.setWidth("100%");
        cuotaCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuotaCbx.setTextInputAllowed(true);
        cuotaCbx.setNewItemsAllowed(false);
        cuotaCbx.setNullSelectionAllowed(false);
        cuotaCbx.addItem("");
        cuotaCbx.select("");
        cuotaCbx.setRequired(true);
        cuotaCbx.setResponsive(true);
        cuotaCbx.addContainerProperty(ID_CUOTA, Integer.class, 0);
        cuotaCbx.addContainerProperty(TIPO_CUOTA, String.class, "");
        cuotaCbx.addContainerProperty(MONTO_CUOTA, Double.class, 0.0);
        cuotaCbx.addValueChangeListener(valueChangeEvent -> {
            if(montoTxt != null){
                montoTxt.setValue((Double) cuotaCbx.getContainerProperty(cuotaCbx.getValue(), MONTO_CUOTA).getValue());
            }
        });

        Label blankLbl1 = new Label("");
        blankLbl1.setWidth("100%");
        blankLbl1.setResponsive(true);

        Label blankLbl2 = new Label("");
        blankLbl2.setWidth("100%");
        blankLbl2.setResponsive(true);

        unidadCbx = new ComboBox("Unidad:");
        unidadCbx.setWidth("100%");
        unidadCbx.setFilteringMode(FilteringMode.CONTAINS);
        unidadCbx.setTextInputAllowed(true);
        unidadCbx.setNewItemsAllowed(false);
        unidadCbx.setNullSelectionAllowed(false);
        unidadCbx.addItem("");
        unidadCbx.select("");
        unidadCbx.setRequired(true);
        unidadCbx.setResponsive(true);
        unidadCbx.addContainerProperty(ID_PROVEEDOR, Integer.class, 0);
        unidadCbx.addContainerProperty(NOMBRE_PROVEEDOR, String.class, "");

        fechaPagoInicioDt = new DateField("Fecha Inicio Cuota:");
        fechaPagoInicioDt.setWidth("100%");
        fechaPagoInicioDt.setHeight("100%");
        fechaPagoInicioDt.setDateFormat("dd/MM/yyyy");
        fechaPagoInicioDt.setValue(new java.util.Date());

        fechaCorteMultaAtrasoDt = new DateField("Fecha Multa Atraso:");
        fechaCorteMultaAtrasoDt.setWidth("100%");
        fechaCorteMultaAtrasoDt.setHeight("100%");
        fechaCorteMultaAtrasoDt.setDateFormat("dd/MM/yyyy");
        fechaCorteMultaAtrasoDt.setValue(new java.util.Date());

        montoTxt = new NumberField("Monto Cuota:");
        montoTxt.setWidth("100%");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setValue(0.00);
        montoTxt.setRequired(true);
        montoTxt.setResponsive(true);

        montoMultaAtrasoTxt = new NumberField("Monto Multa Atraso:");
        montoMultaAtrasoTxt.setWidth("100%");
        montoMultaAtrasoTxt.setDecimalAllowed(true);
        montoMultaAtrasoTxt.setDecimalPrecision(2);
        montoMultaAtrasoTxt.setMinimumFractionDigits(2);
        montoMultaAtrasoTxt.setDecimalSeparator('.');
        montoMultaAtrasoTxt.setDecimalSeparatorAlwaysShown(true);
        montoMultaAtrasoTxt.setGroupingUsed(true);
        montoMultaAtrasoTxt.setGroupingSeparator(',');
        montoMultaAtrasoTxt.setGroupingSize(3);
        montoMultaAtrasoTxt.setImmediate(true);
        montoMultaAtrasoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoMultaAtrasoTxt.setValue(0.00);
        montoMultaAtrasoTxt.setRequired(true);
        montoMultaAtrasoTxt.setResponsive(true);

        referenciaTxt = new TextField("Referencia:");
        referenciaTxt.setResponsive(true);
        referenciaTxt.setSizeFull();
        referenciaTxt.setValue("Cuota Agregada");

        agregarCuotaBtn = new Button("Agregar Cuota");
        agregarCuotaBtn.setWidth("100%");
        agregarCuotaBtn.addClickListener(clickEvent -> {
            if(datosValidos()) nuevaCuota();
        });

        mainLayout.addComponents(tituloLbl, layout1, layout2, layout3);

        layout1.addComponents(unidadCbx, cuotaCbx, blankLbl1);
        layout1.setComponentAlignment(unidadCbx, Alignment.TOP_CENTER);
        layout1.setComponentAlignment(cuotaCbx, Alignment.TOP_CENTER);
        layout1.setComponentAlignment(blankLbl1, Alignment.TOP_CENTER);

        layout1.setExpandRatio(unidadCbx, 2.0f);
        layout1.setExpandRatio(cuotaCbx, 2.0f);
        layout1.setExpandRatio(blankLbl1, 1.0f);

        layout2.addComponents(referenciaTxt, fechaPagoInicioDt, fechaCorteMultaAtrasoDt, blankLbl2);
        layout2.setComponentAlignment(referenciaTxt, Alignment.MIDDLE_CENTER);
        layout2.setComponentAlignment(fechaPagoInicioDt, Alignment.MIDDLE_CENTER);
        layout2.setComponentAlignment(fechaCorteMultaAtrasoDt, Alignment.MIDDLE_CENTER);
        layout2.setComponentAlignment(blankLbl2, Alignment.MIDDLE_CENTER);

        layout2.setExpandRatio(referenciaTxt, 2.0f);
        layout2.setExpandRatio(fechaPagoInicioDt, 1.0f);
        layout2.setExpandRatio(fechaCorteMultaAtrasoDt, 1.0f);
        layout2.setExpandRatio(blankLbl2, 1.0f);

        layout3.addComponents(montoTxt, montoMultaAtrasoTxt, agregarCuotaBtn);
        layout3.setComponentAlignment(montoTxt, Alignment.BOTTOM_CENTER);
        layout3.setComponentAlignment(montoMultaAtrasoTxt, Alignment.BOTTOM_CENTER);
        layout3.setComponentAlignment(agregarCuotaBtn, Alignment.BOTTOM_CENTER);

        layout3.setExpandRatio(montoTxt, 2.0f);
        layout3.setExpandRatio(montoMultaAtrasoTxt, 2.0f);
        layout3.setExpandRatio(agregarCuotaBtn, 1.0f);


    }


    private void llenarProveedores(){
        queryString = "SELECT * FROM proveedor p ";
        queryString += "INNER JOIN cuotas_unidad_cuenta cuc ON p.IDProveedor = cuc.IdUnidad ";
        queryString += "AND cuc.IdCuenta = " + idProveedor + " ";
        queryString += "WHERE p.N0 IN (5) ";
        queryString += "AND p.Inhabilitado = 0 ";
        queryString += "ORDER BY p.IdProveedor";

        Object itemId;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado
                do {
                    itemId = unidadCbx.addItem();
                    unidadCbx.setItemCaption(itemId, rsRecords1.getInt("IDProveedor") + " " + rsRecords1.getString("Nombre"));
                    unidadCbx.getContainerProperty(itemId, ID_PROVEEDOR).setValue(rsRecords1.getInt("IDProveedor"));
                    unidadCbx.getContainerProperty(itemId, NOMBRE_PROVEEDOR).setValue(rsRecords1.getString("Nombre"));
                }while (rsRecords1.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }


    private void llenaCuotas(){
        queryString = "SELECT * FROM cuotas_tipo ";
        queryString += "WHERE IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString += "AND Inhabilitado = 0 ";

        Object itemId;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado
                do {
                    itemId = cuotaCbx.addItem();
                    cuotaCbx.setItemCaption(itemId, rsRecords1.getInt("IdNomenclatura") + " " + rsRecords1.getString("TipoCuota"));
                    cuotaCbx.getContainerProperty(itemId, ID_CUOTA).setValue(rsRecords1.getInt("IdCuota"));
                    cuotaCbx.getContainerProperty(itemId, TIPO_CUOTA).setValue(rsRecords1.getString("TipoCuota"));
                    cuotaCbx.getContainerProperty(itemId, MONTO_CUOTA).setValue(rsRecords1.getDouble("Valor"));
                }while (rsRecords1.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public boolean datosValidos() {

        if (unidadCbx.getValue() == null || unidadCbx.getValue().equals("0")) {
            Notification.show("Por favor ingrese la unidad.", Notification.Type.WARNING_MESSAGE);
            unidadCbx.focus();
            return false;
        }

        if (cuotaCbx.getValue() == null || cuotaCbx.getValue().equals("")) {
            Notification.show("Por favor ingrese el tipo de Cuota.", Notification.Type.WARNING_MESSAGE);
            cuotaCbx.focus();
            return false;
        }

        if (this.montoTxt.getDoubleValueDoNotThrow() <= 0) {
            Notification.show("Por favor ingrese el monto de la Cuota.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return false;
        }

        if (fechaPagoInicioDt.getValue().after(fechaCorteMultaAtrasoDt.getValue())) {
            Notification.show("La Fecha de Multa no puede ser anes de la Fecha de Pago.", Notification.Type.WARNING_MESSAGE);
            fechaCorteMultaAtrasoDt.focus();
            return false;
        }

        return true;
    }

    private void nuevaCuota(){
        //240 2025 02 01 02 501114
        Utileria u = new Utileria();
        String codigoCobro = ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + u.getFechaSinFormato(new Date());
        codigoCobro += String.format("%02d", cuotaCbx.getContainerProperty(cuotaCbx.getValue(), ID_CUOTA).getValue());
        codigoCobro += String.format("%06d", unidadCbx.getContainerProperty(unidadCbx.getValue(), ID_PROVEEDOR).getValue());

        queryString =   "INSERT INTO cuotas (";
        queryString +=  "IdCobro, IdEmpresa, IdUnidad, Nombre, Referencia, TipoCuota, TipoIngreso, Cuota, Mes, Año, Fecha, ";
        queryString +=  "UltimoDiaPago, CobroAdicional, IdCuenta, Cuenta)";
        queryString +=  "VALUES (";
        queryString +=  " " + codigoCobro + " ";
        queryString +=  ", " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString +=  ", " + unidadCbx.getContainerProperty(unidadCbx.getValue(), ID_PROVEEDOR).getValue() + " ";
        queryString +=  ", '" + unidadCbx.getContainerProperty(unidadCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "' ";
        queryString +=  ", '" + referenciaTxt.getValue() + "' ";
        queryString +=  ", " + cuotaCbx.getContainerProperty(cuotaCbx.getValue(), ID_CUOTA).getValue() + " ";
        queryString +=  ", '" + cuotaCbx.getContainerProperty(cuotaCbx.getValue(), TIPO_CUOTA).getValue() + "' ";
        queryString +=  ", '" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "' ";
        queryString +=  ", " + ((fechaPagoInicioDt.getValue()).getMonth() + 1) + " ";
        queryString +=  ", " + ((fechaPagoInicioDt.getValue()).getYear() + 1900) + " ";
        queryString +=  ", '" + Utileria.getFechaYYYYMMDD_1(fechaPagoInicioDt.getValue()) + "' ";
        queryString +=  ", '" + Utileria.getFechaYYYYMMDD_1(fechaCorteMultaAtrasoDt.getValue()) + "' ";
        queryString +=  ", '" + Utileria.format(montoMultaAtrasoTxt.getDoubleValueDoNotThrow()) + "' ";
        queryString +=  ", " + idProveedor + " ";
        queryString +=  ", '" + nombreProveedor + "')";


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY INSERTAR CUOTA : " + queryString);

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1.executeUpdate(queryString);

        } catch (Exception ex1) {
            System.out.println("Error al Insertar cuota  : " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
        }

        Notification notif = new Notification("CUOTA REGISTRADA EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
        notif.setDelayMsec(1500);
        notif.setPosition(Position.MIDDLE_CENTER);
        notif.setIcon(FontAwesome.CHECK);
        notif.show(Page.getCurrent());

        ((PagosCuotasInfileView) (mainUI.getNavigator().getCurrentView())).llenarCuotasPendientesGrid();
        close();
    }

}
