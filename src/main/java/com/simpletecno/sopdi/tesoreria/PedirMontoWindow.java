package com.simpletecno.sopdi.tesoreria;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.text.DecimalFormat;

/**
 * Ventana modal simple para pedir un monto numérico con un máximo permitido.
 * Usada en Anticipos OC para indicar el monto parcial o total a pagar.
 */
public class PedirMontoWindow extends Window {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###,##0.00");

    private double maxMonto  = 0;
    private String simbolo   = "Q.";

    private Label      maxMontoLbl;
    private NumberField montoField;
    private Button      aceptarBtn;
    private Button      cancelarBtn;

    public PedirMontoWindow() {
        injectStyles();

        setModal(true);
        setResizable(false);
        setDraggable(true);
        setWidth("380px");
        setHeightUndefined();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addStyleName("pmw-main");
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setWidth("100%");
        setContent(mainLayout);

        buildHeader(mainLayout);
        buildForm(mainLayout);
        buildActionBar(mainLayout);
    }

    // ── Estilos ──────────────────────────────────────────────────────────────

    private void injectStyles() {
        Page.getCurrent().getStyles().add(
                ".pmw-main { background:#F4F6F9; padding:0 !important; }" +

                ".pmw-header {" +
                "  background: linear-gradient(135deg,#1A237E 0%,#283593 100%);" +
                "  border-radius:8px 8px 0 0; padding:14px 20px !important; width:100%;" +
                "}" +
                ".pmw-header-icon { font-size:26px !important; color:#fff; }" +
                ".pmw-header-title {" +
                "  color:#fff !important; font-size:15px !important;" +
                "  font-weight:700 !important; margin:0 !important;" +
                "}" +
                ".pmw-header-sub { color:#9FA8DA !important; font-size:11px !important; margin:2px 0 0 0 !important; }" +

                ".pmw-card {" +
                "  background:#fff; padding:18px 24px !important; width:100%;" +
                "  box-shadow:0 1px 4px rgba(0,0,0,0.06);" +
                "}" +

                ".pmw-max-lbl {" +
                "  background:#E8EAF6; border:1px solid #C5CAE9; border-radius:6px;" +
                "  color:#1A237E !important; font-size:13px !important; font-weight:700 !important;" +
                "  padding:6px 14px !important; display:block; width:100%; margin-bottom:14px !important;" +
                "}" +

                ".pmw-action-bar {" +
                "  background:#F8FAFB; border-top:1px solid #E3E8EF;" +
                "  border-radius:0 0 8px 8px; padding:12px 20px !important; width:100%;" +
                "}" +

                ".pmw-btn-aceptar.v-button {" +
                "  background:linear-gradient(135deg,#1565C0 0%,#1976D2 100%) !important;" +
                "  color:#fff !important; border:none !important; border-radius:6px !important;" +
                "  font-weight:700 !important; padding:0 28px !important; height:36px !important;" +
                "  box-shadow:0 2px 8px rgba(21,101,192,0.30) !important;" +
                "}" +
                ".pmw-btn-cancelar.v-button {" +
                "  border:1px solid #CFD8DC !important; border-radius:6px !important;" +
                "  color:#546E7A !important; background:#fff !important;" +
                "  height:36px !important; padding:0 22px !important;" +
                "}"
        );
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private void buildHeader(VerticalLayout parent) {
        Label iconLbl = new Label(FontAwesome.MONEY.getHtml(), ContentMode.HTML);
        iconLbl.addStyleName("pmw-header-icon");
        iconLbl.setSizeUndefined();

        Label titleLbl = new Label("Monto a pagar");
        titleLbl.addStyleName("pmw-header-title");
        titleLbl.setSizeUndefined();

        Label subLbl = new Label("Indique el monto parcial o total a aplicar");
        subLbl.addStyleName("pmw-header-sub");
        subLbl.setSizeUndefined();

        VerticalLayout textCol = new VerticalLayout();
        textCol.setMargin(false);
        textCol.setSpacing(false);
        textCol.addComponents(titleLbl, subLbl);

        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("pmw-header");
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(false);
        header.addComponents(iconLbl, textCol);
        header.setExpandRatio(textCol, 1f);
        header.setComponentAlignment(iconLbl, Alignment.MIDDLE_LEFT);
        header.setComponentAlignment(textCol, Alignment.MIDDLE_LEFT);

        parent.addComponent(header);
    }

    // ── Formulario ───────────────────────────────────────────────────────────

    private void buildForm(VerticalLayout parent) {
        maxMontoLbl = new Label("Monto máximo:  Q. 0.00");
        maxMontoLbl.addStyleName("pmw-max-lbl");
        maxMontoLbl.setWidth("100%");

        montoField = new NumberField("Monto a pagar:");
        montoField.setDecimalAllowed(true);
        montoField.setDecimalPrecision(2);
        montoField.setMinimumFractionDigits(2);
        montoField.setDecimalSeparator('.');
        montoField.setDecimalSeparatorAlwaysShown(true);
        montoField.setValue(0d);
        montoField.setGroupingUsed(true);
        montoField.setGroupingSeparator(',');
        montoField.setGroupingSize(3);
        montoField.setImmediate(true);
        montoField.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoField.setWidth("100%");

        VerticalLayout card = new VerticalLayout();
        card.addStyleName("pmw-card");
        card.setWidth("100%");
        card.setSpacing(true);
        card.setMargin(false);
        card.addComponents(maxMontoLbl, montoField);

        parent.addComponent(card);
    }

    // ── Barra de acciones ────────────────────────────────────────────────────

    private void buildActionBar(VerticalLayout parent) {
        cancelarBtn = new Button("Cancelar", FontAwesome.TIMES);
        cancelarBtn.addStyleName("pmw-btn-cancelar");
        cancelarBtn.addClickListener(e -> close());

        aceptarBtn = new Button("Aceptar", FontAwesome.CHECK);
        aceptarBtn.addStyleName("pmw-btn-aceptar");
        aceptarBtn.addClickListener(e -> {
            double monto = montoField.getDoubleValueDoNotThrow();
            if (monto <= 0) {
                Notification.show("Ingrese un monto mayor a cero.", Notification.Type.WARNING_MESSAGE);
                montoField.focus();
                return;
            }
            if (monto > maxMonto) {
                Notification.show("El monto no puede ser mayor al máximo permitido (" + simbolo + " " +
                        NUMBER_FORMAT.format(maxMonto) + ").", Notification.Type.WARNING_MESSAGE);
                montoField.setValue(maxMonto);
                montoField.focus();
                return;
            }
            close();
        });

        Label spacer = new Label();

        HorizontalLayout actionBar = new HorizontalLayout();
        actionBar.addStyleName("pmw-action-bar");
        actionBar.setWidth("100%");
        actionBar.setSpacing(true);
        actionBar.setMargin(false);
        actionBar.addComponents(cancelarBtn, spacer, aceptarBtn);
        actionBar.setExpandRatio(spacer, 1f);
        actionBar.setComponentAlignment(cancelarBtn, Alignment.MIDDLE_LEFT);
        actionBar.setComponentAlignment(aceptarBtn,  Alignment.MIDDLE_RIGHT);

        parent.addComponent(actionBar);
    }

    // ── API pública ──────────────────────────────────────────────────────────

    public void setMaxMonto(double max, String simboloMoneda) {
        this.maxMonto = max;
        this.simbolo  = simboloMoneda;
        maxMontoLbl.setValue("Monto máximo:  " + simboloMoneda + " " + NUMBER_FORMAT.format(max));
    }

    public void setMonto(double monto) {
        montoField.setValue(monto);
    }

    public double getMonto() {
        return montoField.getDoubleValueDoNotThrow();
    }

    public Button getAceptarBtn() { return aceptarBtn; }
    public Button getCancelarBtn() { return cancelarBtn; }

    @Override
    public void attach() {
        super.attach();
        montoField.focus();
    }
}
