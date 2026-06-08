package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Autorizar Pagos Especiales.
 *
 * Vista basada en {@link AutorizarPagosCorrientesView}. A diferencia de los pagos
 * corrientes, aquí el usuario primero elige el TIPO de pago especial mediante el
 * combo {@link #tipoPagoCbx}; según la opción seleccionada se arma/carga la
 * operación correspondiente.
 *
 * NOTA: Esta clase contiene únicamente el armazón de la vista y el combo de
 * selección. La lógica de negocio (grids, queries, aplicación contable, PDF, etc.)
 * se implementa aparte.
 *
 * @author user
 */
public class AutorizarPagosEspecialesView extends VerticalLayout implements View {

    /** Opciones del combo de tipo de pago especial. */
    public static final String ANTICIPO_HONORARIOS              = "ANTICIPO HONORARIOS";
    public static final String ANTICIPO_SUELDOS                 = "ANTICIPO SUELDOS";
    public static final String ANTICIPOS_POR_LIQUIDAR_EMPRESA   = "ANTICIPOS POR LIQUIDAR EMPRESA";
    public static final String VENTA_DE_MONEDA                  = "VENTA DE MONEDA";
    public static final String TRASLADO_CUENTAS                 = "TRASLADO CUENTAS";
    public static final String PRESTAMOS_BANCARIOS              = "PRESTAMOS BANCARIOS";
    public static final String ACREEDORES_POR_LIQUIDAR_EMPRESA  = "ACREEDORES POR LIQUIDAR EMPRESA";
    public static final String PAGO_PRESTAMOS_CORTO_PLAZO       = "PAGO PRESTAMOS CORTO PLAZO";
    public static final String DEVOLUCION_ANTICIPO_CLIENTE      = "DEVOLUCION ANTICIPO CLIENTE";
    public static final String EFECTIVO_EN_TRANSITO             = "EFECTIVO EN TRANSITO";

    /** Lista ordenada de las opciones, en el orden solicitado para el combo. */
    public static final String[] TIPOS_PAGO_ESPECIAL = {
            ANTICIPO_HONORARIOS,
            ANTICIPO_SUELDOS,
            ANTICIPOS_POR_LIQUIDAR_EMPRESA,
            VENTA_DE_MONEDA,
            TRASLADO_CUENTAS,
            PRESTAMOS_BANCARIOS,
            ACREEDORES_POR_LIQUIDAR_EMPRESA,
            PAGO_PRESTAMOS_CORTO_PLAZO,
            DEVOLUCION_ANTICIPO_CLIENTE,
            EFECTIVO_EN_TRANSITO
    };

    VerticalLayout mainLayout;

    /** Combo de elección del tipo de pago especial (al inicio de la vista). */
    ComboBox tipoPagoCbx;

    /** Contenedor donde se monta la operación según el tipo de pago elegido. */
    VerticalLayout contenidoLayout;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizarPagosEspecialesView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setHeightUndefined();
        addStyleName("ape-view");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " Autorizar Pagos Especiales");
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        mainLayout.addComponent(titleLbl);

        crearComboTipoPago();

        contenidoLayout = new VerticalLayout();
        contenidoLayout.setSpacing(true);
        contenidoLayout.setWidth("100%");
        mainLayout.addComponent(contenidoLayout);

        addComponent(mainLayout);
    }

    /**
     * Crea el combo de elección con los tipos de pago especial y lo coloca al
     * inicio de la vista. Al cambiar la selección se delega en
     * {@link #onTipoPagoSeleccionado(String)}.
     */
    private void crearComboTipoPago() {
        tipoPagoCbx = new ComboBox("Tipo de pago especial :");
        tipoPagoCbx.setWidth("22em");
        tipoPagoCbx.setNullSelectionAllowed(false);
        tipoPagoCbx.setNewItemsAllowed(false);
        tipoPagoCbx.setInvalidAllowed(false);
        tipoPagoCbx.setTextInputAllowed(false);
        tipoPagoCbx.setImmediate(true);
        tipoPagoCbx.setInputPrompt("Seleccione el tipo de pago");

        for (String tipo : TIPOS_PAGO_ESPECIAL) {
            tipoPagoCbx.addItem(tipo);
        }

        tipoPagoCbx.addValueChangeListener((Property.ValueChangeListener) event -> {
            Object seleccion = tipoPagoCbx.getValue();
            if (seleccion == null) {
                return;
            }
            onTipoPagoSeleccionado(String.valueOf(seleccion));
        });

        mainLayout.addComponent(tipoPagoCbx);
        mainLayout.setComponentAlignment(tipoPagoCbx, Alignment.TOP_LEFT);
    }

    /**
     * Punto de enganche para la lógica de negocio: se invoca cada vez que el
     * usuario elige un tipo de pago en el combo. Aquí se debe armar/cargar la
     * operación correspondiente (grids, queries, botones, etc.).
     *
     * @param tipoPago una de las constantes {@link #TIPOS_PAGO_ESPECIAL}
     */
    private void onTipoPagoSeleccionado(String tipoPago) {
        contenidoLayout.removeAllComponents();

        // TODO: lógica de negocio por cada tipo de pago especial.
        Notification.show("Tipo seleccionado: " + tipoPago, Notification.Type.TRAY_NOTIFICATION);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Pagos especiales");
    }
}