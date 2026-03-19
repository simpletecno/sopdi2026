package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.compras.OrdenCompraAnticiposForm;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class AutorizacionesPagoView extends VerticalLayout implements View {

    static public final String ANTICIPO_PROVEEDOR = "ANTICIPO A PROVEEDOR";
    static public final String ANTICIPO_HONORARIOS = "ANTICIPO DE HONORARIO";
    static public final String ANTICIPO_SUELDOS = "ANTICIPO DE SUELDO";
    static public final String TRASLADO_EMP_REL = "TRASLADO A EMPRESA"; // RELACIONADA
    static public final String PAGO_DOCUMENTO = "PAGO DE DOCUMENTO";
    static public final String PAGO_LIQUIDACION = "PAGO DE LIQUIDACION";
    static public final String PAGO_PRESTAMO = "PAGO DE PRESTAMO";
    static public final String PAGO_PLANILLA = "PAGO DE PLANILLA";
    static public final String VENTA_MONEDA = "VENTA DE MONEDA";
    static public final String DEVOLUCION_CLIENTE = "DEVOLUCION A CLIENTE";
    static public final String DEVOLUCION_PRESTAMO_TERCERO = "DEVOLUCION PRESTAMO";
    static public final String ANTICIPO_PROVEEDOR_OC = "ANTICIPO A PROVEEDOR OC";

    Button anticiposProveedorBtn;
    Button anticiposSueldoBtn;
    Button anticipoHonorarioBtn;
    Button pagoDocumentoBtn;
    Button pagoLiquidacionBtn;
    Button trasladoERBtn;
    Button pagoPrestamoBtn;
    Button ventaMonedaBtn;
    Button devolucionClienteBtn;
    Button planillaBtn;
    Button devolucionPrestamoTercerosBtn;
    Button anticiposProveedorOCBtn;

    UI mainUI;

    Statement stQuery;
    ResultSet rsRecords;

    String queryString;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizacionesPagoView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " AUTORIZAR PAGOS DE....");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            titleLbl.addStyleName(ValoTheme.LABEL_H2);
        }
        else {
            titleLbl.addStyleName(ValoTheme.LABEL_H4);
        }
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearBotones();

    }

    public void crearBotones() {

        anticiposProveedorBtn = new Button(ANTICIPO_PROVEEDOR);
        anticiposProveedorBtn.setIcon(FontAwesome.CHILD);
        anticiposProveedorBtn.setWidth("17em");
        anticiposProveedorBtn.setHeight("5em");
        anticiposProveedorBtn.setDescription(ANTICIPO_PROVEEDOR);
        anticiposProveedorBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                AutorizarPagoAnticipoForm autorizarPagoAnticipo = new AutorizarPagoAnticipoForm(ANTICIPO_PROVEEDOR);
                mainUI.addWindow(autorizarPagoAnticipo);
                autorizarPagoAnticipo.center();

            }
        });
        anticiposProveedorBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        pagoDocumentoBtn = new Button(PAGO_DOCUMENTO);
        pagoDocumentoBtn.setIcon(FontAwesome.BARCODE);
        pagoDocumentoBtn.setDescription(PAGO_DOCUMENTO);
        pagoDocumentoBtn.setWidth("17em");
        pagoDocumentoBtn.setHeight("5em");
        pagoDocumentoBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AutorizarPagoFacturaForm pagoFactura = new AutorizarPagoFacturaForm();
                mainUI.addWindow(pagoFactura);
                pagoFactura.center();
            }
        });

        pagoLiquidacionBtn = new Button(PAGO_LIQUIDACION);
        pagoLiquidacionBtn.setIcon(FontAwesome.LIST_OL);
        pagoLiquidacionBtn.setDescription(PAGO_LIQUIDACION);
        pagoLiquidacionBtn.setWidth("17em ");
        pagoLiquidacionBtn.setHeight("5em");
        pagoLiquidacionBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                System.out.println("Entro a liquidaciones");
                AutorizarPagoLiquidacionForm pagoLiquidacion = new AutorizarPagoLiquidacionForm();
                mainUI.addWindow(pagoLiquidacion);
                pagoLiquidacion.center();
            }
        });
        pagoLiquidacionBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        anticiposSueldoBtn = new Button(ANTICIPO_SUELDOS);
        anticiposSueldoBtn.setIcon(FontAwesome.BATTERY_1);
        anticiposSueldoBtn.setWidth("17em");
        anticiposSueldoBtn.setHeight("5em");
        anticiposSueldoBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                AutorizarPagoAnticipoForm autorizarPagoAnticipo = new AutorizarPagoAnticipoForm(ANTICIPO_SUELDOS);
                mainUI.addWindow(autorizarPagoAnticipo);
                autorizarPagoAnticipo.center();
            }
        });
        anticiposSueldoBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        anticipoHonorarioBtn = new Button(ANTICIPO_HONORARIOS);
        anticipoHonorarioBtn.setIcon(FontAwesome.BATTERY_3);
        //anticipoHonorarioBtn.setDescription(ANTICIPO_HONORARIOS);
        anticipoHonorarioBtn.setWidth("17em");
        anticipoHonorarioBtn.setHeight("5em");
        anticipoHonorarioBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                  AutorizarPagoAnticipoForm autorizarPagoAnticipo = new AutorizarPagoAnticipoForm(ANTICIPO_HONORARIOS);
                  mainUI.addWindow(autorizarPagoAnticipo);
                  autorizarPagoAnticipo.center();
            }
        });
        anticipoHonorarioBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        trasladoERBtn = new Button(TRASLADO_EMP_REL);
        trasladoERBtn.setIcon(FontAwesome.HOUZZ);
        trasladoERBtn.setDescription(TRASLADO_EMP_REL);
        trasladoERBtn.setWidth("17em");
        trasladoERBtn.setHeight("5em");
        trasladoERBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AutorizarPagoEmpresaRelacionadaForm empresaForm = new AutorizarPagoEmpresaRelacionadaForm();
                mainUI.addWindow(empresaForm);
                empresaForm.center();
            }
        });
        trasladoERBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        ventaMonedaBtn = new Button(VENTA_MONEDA);
        ventaMonedaBtn.setIcon(FontAwesome.BANK);
        ventaMonedaBtn.setWidth("17em");
        ventaMonedaBtn.setHeight("5em");
        ventaMonedaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AutorizarVentaMonedaForm compraMoneda = new AutorizarVentaMonedaForm();
                mainUI.addWindow(compraMoneda);
                compraMoneda.center();

            }
        });
        ventaMonedaBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        pagoPrestamoBtn = new Button(PAGO_PRESTAMO);
        pagoPrestamoBtn.setIcon(FontAwesome.BUILDING);
        pagoPrestamoBtn.setWidth("17em");
        pagoPrestamoBtn.setHeight("5em");
        pagoPrestamoBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AutorizarPagoPrestamoForm prestamoForm = new AutorizarPagoPrestamoForm();
                mainUI.addWindow(prestamoForm);
                prestamoForm.center();

            }
        });
        pagoPrestamoBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        devolucionClienteBtn = new Button(DEVOLUCION_CLIENTE);
        devolucionClienteBtn.setIcon(FontAwesome.HOUZZ);
        devolucionClienteBtn.setWidth("17em");
        devolucionClienteBtn.setHeight("5em");
        devolucionClienteBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AutorizarPagoDevolucionClienteForm devolucionForm = new AutorizarPagoDevolucionClienteForm();
                mainUI.addWindow(devolucionForm);
                devolucionForm.center();

            }
        });
        devolucionClienteBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        planillaBtn = new Button(PAGO_PLANILLA);
        planillaBtn.setIcon(FontAwesome.USERS);
        planillaBtn.setWidth("17em");
        planillaBtn.setHeight("5em");
        planillaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AutorizarPagoPlanillaForm planillaForm = new AutorizarPagoPlanillaForm();
                mainUI.addWindow(planillaForm);
                planillaForm.center();

            }
        });
        planillaBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        devolucionPrestamoTercerosBtn = new Button(DEVOLUCION_PRESTAMO_TERCERO);
        devolucionPrestamoTercerosBtn.setIcon(FontAwesome.USER);
        devolucionPrestamoTercerosBtn.setWidth("17em");
        devolucionPrestamoTercerosBtn.setHeight("5em");
        devolucionPrestamoTercerosBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AutorizarDevolucionPrestamoTerceroForm pagoPrestamoTercero = new AutorizarDevolucionPrestamoTerceroForm();
                mainUI.addWindow(pagoPrestamoTercero);
                pagoPrestamoTercero.center();

            }
        });
        devolucionPrestamoTercerosBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        anticiposProveedorOCBtn = new Button(ANTICIPO_PROVEEDOR_OC);
        anticiposProveedorOCBtn.setIcon(FontAwesome.SHOPPING_CART);
        anticiposProveedorOCBtn.setWidth("17em");
        anticiposProveedorOCBtn.setHeight("5em");
        anticiposProveedorOCBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                OrdenCompraAnticiposForm ordenCompraAnticiposForm = new OrdenCompraAnticiposForm();
                mainUI.addWindow(ordenCompraAnticiposForm);
                ordenCompraAnticiposForm.center();
            }
        });
        anticiposProveedorOCBtn.setEnabled(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR"));

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.setHeight("100%");
        buttonsLayout.setWidth("90%");
        buttonsLayout.setSizeUndefined();
        buttonsLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout.addComponents(anticiposProveedorBtn, pagoDocumentoBtn, pagoLiquidacionBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        HorizontalLayout buttonsLayout2 = new HorizontalLayout();
        buttonsLayout2.setSpacing(true);
        buttonsLayout2.setMargin(true);
        buttonsLayout2.setHeight("100%");
        buttonsLayout2.setWidth("90%");
        buttonsLayout2.setSizeUndefined();
        buttonsLayout2.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout2.addComponents(anticiposSueldoBtn, anticipoHonorarioBtn, trasladoERBtn);
//        buttonsLayout2.addComponents(trasladoERBtn);

        addComponent(buttonsLayout2);
        setComponentAlignment(buttonsLayout2, Alignment.BOTTOM_CENTER);

        HorizontalLayout buttonsLayout3 = new HorizontalLayout();
        buttonsLayout3.setSpacing(true);
        buttonsLayout3.setMargin(true);
        buttonsLayout3.setHeight("100%");
        buttonsLayout3.setWidth("90%");
        buttonsLayout3.setSizeUndefined();
        buttonsLayout3.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout3.addComponents(ventaMonedaBtn, pagoPrestamoBtn, planillaBtn);
        //buttonsLayout3.addComponents(trasladoERBtn, ventaMonedaBtn, pagoPrestamoBtn);

        addComponent(buttonsLayout3);
        setComponentAlignment(buttonsLayout3, Alignment.BOTTOM_CENTER);

        HorizontalLayout buttonsLayout4 = new HorizontalLayout();
        buttonsLayout4.setSpacing(true);
        buttonsLayout4.setMargin(true);
        buttonsLayout4.setHeight("100%");
        buttonsLayout4.setWidth("90%");
        buttonsLayout4.setSizeUndefined();
        buttonsLayout4.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout4.addComponents(devolucionClienteBtn, devolucionPrestamoTercerosBtn, anticiposProveedorOCBtn);

        addComponent(buttonsLayout4);
        setComponentAlignment(buttonsLayout4, Alignment.BOTTOM_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Autorizar pagos");
    }
}
