package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.compras.OrdenCompraAnticiposForm;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Vista de autorizaciones de pago para Fundamex.
 * Muestra 5 opciones con descripción de su propósito.
 */
public class AutorizacionesPagoFundamexView extends VerticalLayout implements View {

    Button pagoDocumentoBtn;
    Button anticiposProveedorOCBtn;
    Button pagoLiquidacionBtn;
    Button planillaBtn;
    Button devolucionPrestamoTercerosBtn;

    UI mainUI;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizacionesPagoFundamexView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(true);
        crearLayout();
    }

    private HorizontalLayout crearFila(Button btn, String descripcion) {
        TextArea descripcionArea = new TextArea();
        descripcionArea.setValue(descripcion);
        descripcionArea.setReadOnly(true);
        descripcionArea.setWidth("40em");
        descripcionArea.setRows(2);
        descripcionArea.addStyleName(ValoTheme.TEXTAREA_BORDERLESS);

        HorizontalLayout fila = new HorizontalLayout(btn, descripcionArea);
        fila.setSpacing(true);
        fila.setComponentAlignment(btn, Alignment.MIDDLE_LEFT);
        fila.setComponentAlignment(descripcionArea, Alignment.MIDDLE_LEFT);
        return fila;
    }

    private void crearLayout() {

        pagoDocumentoBtn = new Button(AutorizacionesPagoView.PAGO_DOCUMENTO);
        pagoDocumentoBtn.setIcon(FontAwesome.BARCODE);
        pagoDocumentoBtn.setWidth("21em");
        pagoDocumentoBtn.setHeight("5em");
        pagoDocumentoBtn.addStyleName("btn-wordwrap");
        pagoDocumentoBtn.addClickListener(e -> {
            AutorizarPagoFacturaForm pagoFactura = new AutorizarPagoFacturaForm();
            mainUI.addWindow(pagoFactura);
            pagoFactura.center();
        });

        anticiposProveedorOCBtn = new Button(AutorizacionesPagoView.ANTICIPO_PROVEEDOR_OC);
        anticiposProveedorOCBtn.setIcon(FontAwesome.CHILD);
        anticiposProveedorOCBtn.setWidth("21em");
        anticiposProveedorOCBtn.setHeight("5em");
        anticiposProveedorOCBtn.addStyleName("btn-wordwrap");
        anticiposProveedorOCBtn.addStyleName("btn-wordwrap-tall");
        anticiposProveedorOCBtn.addClickListener(e -> {
            OrdenCompraAnticiposForm ordenCompraAnticiposForm = new OrdenCompraAnticiposForm();
            mainUI.addWindow(ordenCompraAnticiposForm);
            ordenCompraAnticiposForm.center();
        });

        pagoLiquidacionBtn = new Button(AutorizacionesPagoView.PAGO_LIQUIDACION);
        pagoLiquidacionBtn.setIcon(FontAwesome.LIST_OL);
        pagoLiquidacionBtn.setWidth("21em");
        pagoLiquidacionBtn.setHeight("5em");
        pagoLiquidacionBtn.addStyleName("btn-wordwrap");
        pagoLiquidacionBtn.addClickListener(e -> {
            AutorizarPagoLiquidacionForm form = new AutorizarPagoLiquidacionForm();
            mainUI.addWindow(form);
            form.center();
        });

        planillaBtn = new Button(AutorizacionesPagoView.PAGO_PLANILLA);
        planillaBtn.setIcon(FontAwesome.USERS);
        planillaBtn.setWidth("21em");
        planillaBtn.setHeight("5em");
        planillaBtn.addStyleName("btn-wordwrap");
        planillaBtn.addClickListener(e -> {
            AutorizarPagoPlanillaForm form = new AutorizarPagoPlanillaForm();
            mainUI.addWindow(form);
            form.center();
        });

        devolucionPrestamoTercerosBtn = new Button(AutorizacionesPagoView.DEVOLUCION_PRESTAMO_TERCERO);
        devolucionPrestamoTercerosBtn.setIcon(FontAwesome.USER);
        devolucionPrestamoTercerosBtn.setWidth("21em");
        devolucionPrestamoTercerosBtn.setHeight("5em");
        devolucionPrestamoTercerosBtn.addStyleName("btn-wordwrap");
        devolucionPrestamoTercerosBtn.addClickListener(e -> {
            AutorizarDevolucionPrestamoTerceroForm form = new AutorizarDevolucionPrestamoTerceroForm();
            mainUI.addWindow(form);
            form.center();
        });

        Component f1 = crearFila(pagoDocumentoBtn,
                "En esta opción se autoriza el pago de todos los documentos de proveedores.");
        Component f2 = crearFila(anticiposProveedorOCBtn,
                "En esta opción se autorizan los pagos de proveedores que requieran anticipo.");
        Component f3 = crearFila(pagoLiquidacionBtn,
                "En esta opción se autoriza el pago de las liquidaciones de empleados habilitados para liquidar.");
        Component f4 = crearFila(planillaBtn,
                "En esta opción se autorizan los pagos de planilla. No se utiliza si el pago se hace por carga de archivo al banco.");
        Component f5 = crearFila(devolucionPrestamoTercerosBtn,
                "Esta opción no tiene uso regular. Se da en el caso que alguien en una emergencia haya dado un préstamo temporal y haya que pagárselo de regreso.");

        addComponent(f1); setComponentAlignment(f1, Alignment.MIDDLE_CENTER);
        addComponent(f2); setComponentAlignment(f2, Alignment.MIDDLE_CENTER);
        addComponent(f3); setComponentAlignment(f3, Alignment.MIDDLE_CENTER);
        addComponent(f4); setComponentAlignment(f4, Alignment.MIDDLE_CENTER);
        addComponent(f5); setComponentAlignment(f5, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Autorizar pagos");
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue(empresaId + " " + empresaNombre + " AUTORIZAR PAGOS DE....");
        Page.getCurrent().getStyles().add(
                ".v-button.btn-wordwrap {" +
                "  position: relative;" +
                "}" +
                ".v-button.btn-wordwrap-tall {" +
                "  height: auto !important;" +
                "  min-height: 5em;" +
                "}" +
                ".v-button.btn-wordwrap .v-button-wrap {" +
                "  padding-right: 26px;" +
                "}" +
                ".v-button.btn-wordwrap .v-button-caption {" +
                "  white-space: normal !important;" +
                "  word-break: break-word;" +
                "  line-height: 1.3;" +
                "  text-align: left;" +
                "}" +
                ".v-button.btn-wordwrap .v-icon {" +
                "  position: absolute;" +
                "  right: 10px;" +
                "  top: 50%;" +
                "  transform: translateY(-50%);" +
                "}"
        );
    }
}
