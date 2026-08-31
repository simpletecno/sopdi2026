package com.simpletecno.sopdi.tesoreria;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.text.DecimalFormat;

/**
 * Ventana modal para registrar el pago a un proveedor.
 * Permite indicar el monto a liquidar con anticipos y el monto para cheque.
 */
public class PagoProveedorWindow extends Window {

    static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###,##0.00");

    private double saldoDocumento = 0;
    private double saldoAnticipos = 0;

    // ── Campos ───────────────────────────────────────────────────────────────
    Label        saldoDocumentoLbl;
    Label        saldoAnticiposLbl;
    NumberField  montoAnticipoTxt;
    NumberField  montoChequeTxt;

    // ── Botones ──────────────────────────────────────────────────────────────
    private Button aceptarBtn;
    Button cancelarBtn;

    // ── Constantes ─────────────────────────────────────────────────────
    private String moneda;

    // ── Layout principal ─────────────────────────────────────────────────────
    VerticalLayout mainLayout;

    // ────────────────────────────────────────────────────────────────────────
    public PagoProveedorWindow() {
        injectStyles();

        setModal(true);
        setResizable(false);
        setDraggable(true);
        setCaption("Pagar a proveedor");
        setWidth("460px");
        setHeightUndefined();

        mainLayout = new VerticalLayout();
        mainLayout.addStyleName("ppw-main");
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setWidth("100%");

        setContent(mainLayout);

        buildHeader();
        buildForm();
        buildActionBar();
    }

    // ── CSS ──────────────────────────────────────────────────────────────────
    private void injectStyles() {
        Page.getCurrent().getStyles().add(

                /* ── Fondo general ──────────────────────────────────────────── */
                ".ppw-main {" +
                "  background: #F4F6F9;" +
                "  padding: 0 !important;" +
                "}" +

                /* ── Header ─────────────────────────────────────────────────── */
                ".ppw-header {" +
                "  background: linear-gradient(135deg, #1B5E20 0%, #2E7D32 100%);" +
                "  border-radius: 8px 8px 0 0;" +
                "  padding: 14px 20px !important;" +
                "  width: 100%;" +
                "}" +
                ".ppw-header-icon {" +
                "  font-size: 26px !important;" +
                "  line-height: 1;" +
                "  color: #fff;" +
                "}" +
                ".ppw-header-title {" +
                "  color: #ffffff !important;" +
                "  font-size: 15px !important;" +
                "  font-weight: 700 !important;" +
                "  margin: 0 !important;" +
                "  letter-spacing: 0.02em;" +
                "}" +
                ".ppw-header-sub {" +
                "  color: #A5D6A7 !important;" +
                "  font-size: 11px !important;" +
                "  margin: 2px 0 0 0 !important;" +
                "}" +

                /* ── Card del formulario ─────────────────────────────────────── */
                ".ppw-card {" +
                "  background: #ffffff;" +
                "  border-radius: 0;" +
                "  box-shadow: 0 1px 4px rgba(0,0,0,0.06);" +
                "  padding: 18px 24px !important;" +
                "  width: 100%;" +
                "}" +

                /* ── Etiqueta saldo anticipos ────────────────────────────────── */
                ".ppw-saldo-lbl {" +
                "  background: #E8F5E9;" +
                "  border: 1px solid #C8E6C9;" +
                "  border-radius: 6px;" +
                "  color: #1B5E20 !important;" +
                "  font-size: 13px !important;" +
                "  font-weight: 700 !important;" +
                "  padding: 6px 14px !important;" +
                "  display: block;" +
                "  width: 100%;" +
                "  margin-bottom: 14px !important;" +
                "}" +

                /* ── Separador de sección ────────────────────────────────────── */
                ".ppw-divider {" +
                "  border: none;" +
                "  border-top: 1px solid #E3E8EF;" +
                "  margin: 10px 0 16px 0;" +
                "  width: 100%;" +
                "}" +

                /* ── Barra de acciones ───────────────────────────────────────── */
                ".ppw-action-bar {" +
                "  background: #F8FAFB;" +
                "  border-top: 1px solid #E3E8EF;" +
                "  border-radius: 0 0 8px 8px;" +
                "  padding: 12px 20px !important;" +
                "  width: 100%;" +
                "}" +

                /* ── Botón Aceptar ───────────────────────────────────────────── */
                ".ppw-btn-aceptar.v-button {" +
                "  background: linear-gradient(135deg, #1565C0 0%, #1976D2 100%) !important;" +
                "  color: #fff !important;" +
                "  border: none !important;" +
                "  border-radius: 6px !important;" +
                "  font-weight: 700 !important;" +
                "  padding: 0 28px !important;" +
                "  height: 36px !important;" +
                "  box-shadow: 0 2px 8px rgba(21,101,192,0.30) !important;" +
                "}" +
                ".ppw-btn-aceptar.v-button:hover {" +
                "  background: linear-gradient(135deg, #1976D2 0%, #1E88E5 100%) !important;" +
                "}" +

                /* ── Botón Cancelar ──────────────────────────────────────────── */
                ".ppw-btn-cancelar.v-button {" +
                "  border: 1px solid #CFD8DC !important;" +
                "  border-radius: 6px !important;" +
                "  color: #546E7A !important;" +
                "  background: #ffffff !important;" +
                "  height: 36px !important;" +
                "  padding: 0 22px !important;" +
                "}" +
                ".ppw-btn-cancelar.v-button:hover {" +
                "  background: #F5F5F5 !important;" +
                "}"
        );
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private void buildHeader() {
        Label iconLbl = new Label(FontAwesome.MONEY.getHtml(), ContentMode.HTML);
        iconLbl.addStyleName("ppw-header-icon");
        iconLbl.setSizeUndefined();

        Label titleLbl = new Label("Pagar a proveedor");
        titleLbl.addStyleName("ppw-header-title");
        titleLbl.setSizeUndefined();

        Label subLbl = new Label("Indique los montos a aplicar y confirme el pago");
        subLbl.addStyleName("ppw-header-sub");
        subLbl.setSizeUndefined();

        VerticalLayout textCol = new VerticalLayout();
        textCol.setMargin(false);
        textCol.setSpacing(false);
        textCol.addComponents(titleLbl, subLbl);

        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("ppw-header");
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(false);
        header.addComponents(iconLbl, textCol);
        header.setExpandRatio(textCol, 1f);
        header.setComponentAlignment(iconLbl,  Alignment.MIDDLE_LEFT);
        header.setComponentAlignment(textCol,  Alignment.MIDDLE_LEFT);

        mainLayout.addComponent(header);
    }

    // ── Formulario ───────────────────────────────────────────────────────────
    private void buildForm() {
        // -- Etiqueta saldo documento
        saldoDocumentoLbl = new Label("Saldo documento:  Q 0.00");
        saldoDocumentoLbl.addStyleName("ppw-saldo-lbl");
        saldoDocumentoLbl.setWidth("100%");

        // -- Etiqueta saldo anticipos
        saldoAnticiposLbl = new Label("Saldo anticipos:  Q 0.00");
        saldoAnticiposLbl.addStyleName("ppw-saldo-lbl");
        saldoAnticiposLbl.setWidth("100%");

        // -- Campo: monto a liquidar con anticipos
        montoAnticipoTxt = buildNumberField("Monto liquidar con anticipos");
        montoAnticipoTxt.setWidth("100%");

        Label divider = new Label("<hr/>", ContentMode.HTML);
        divider.addStyleName("ppw-divider");
        divider.setWidth("100%");

        // -- Campo: monto para cheque
        montoChequeTxt = buildNumberField("Monto para cheque");
        montoChequeTxt.setWidth("100%");

        // ── Lógica de complemento automático ─────────────────────────────────
        // Cuando el usuario fija el anticipo, el cheque toma el residuo (y viceversa).
        montoAnticipoTxt.addValueChangeListener(e -> {
            if (!montoAnticipoTxt.isReadOnly()) {
                double anticipo = montoAnticipoTxt.getDoubleValueDoNotThrow();
                // Clamp: no puede superar el saldo disponible de anticipos ni el saldo del documento
                double maxAnticipo = Math.min(saldoAnticipos, saldoDocumento);
                if (anticipo > maxAnticipo) {
                    anticipo = maxAnticipo;
                    montoAnticipoTxt.setValue(anticipo);
                }
                double cheque = Math.max(0, saldoDocumento - anticipo);
                montoChequeTxt.setValue(cheque);
            }
        });

        montoChequeTxt.addValueChangeListener(e -> {
            double cheque = montoChequeTxt.getDoubleValueDoNotThrow();
            // Clamp: no puede superar el saldo del documento
            if (cheque > saldoDocumento) {
                cheque = saldoDocumento;
                montoChequeTxt.setValue(cheque);
            }
            if (!montoAnticipoTxt.isReadOnly()) {
                double anticipo = Math.max(0, saldoDocumento - cheque);
                // Clamp anticipo a saldo disponible
                anticipo = Math.min(anticipo, saldoAnticipos);
                montoAnticipoTxt.setValue(anticipo);
            }
        });

        VerticalLayout card = new VerticalLayout();
        card.addStyleName("ppw-card");
        card.setWidth("100%");
        card.setSpacing(true);
        card.setMargin(false);
        card.addComponents(saldoDocumentoLbl, saldoAnticiposLbl, montoAnticipoTxt, divider, montoChequeTxt);

        mainLayout.addComponent(card);
    }

    // ── Barra de acciones ────────────────────────────────────────────────────
    private void buildActionBar() {
        cancelarBtn = new Button("Cancelar", FontAwesome.TIMES);
        cancelarBtn.addStyleName("ppw-btn-cancelar");
        cancelarBtn.addClickListener(e -> close());

        setAceptarBtn(new Button("Aceptar", FontAwesome.CHECK));
        getAceptarBtn().addStyleName("ppw-btn-aceptar");
        getAceptarBtn().addClickListener(e -> {
            if(montoAnticipoTxt.getDoubleValueDoNotThrow() > getSaldoAnticipos()) {
                Notification.show("El monto a liquidar con anticipos no puede ser mayor al saldo de anticipos", Notification.Type.WARNING_MESSAGE);
                montoAnticipoTxt.focus();
                return;
            }
            if((montoAnticipoTxt.getDoubleValueDoNotThrow() + montoChequeTxt.getDoubleValueDoNotThrow()) > saldoDocumento) {
                Notification.show("El monto a liquidar no puede ser mayor al saldo del documento", Notification.Type.WARNING_MESSAGE);
                montoChequeTxt.focus();
            }
            else {
                close();
            }
        });


        Label spacer = new Label();

        HorizontalLayout actionBar = new HorizontalLayout();
        actionBar.addStyleName("ppw-action-bar");
        actionBar.setWidth("100%");
        actionBar.setSpacing(true);
        actionBar.setMargin(false);
        actionBar.addComponents(cancelarBtn, spacer, getAceptarBtn());
        actionBar.setExpandRatio(spacer, 1f);
        actionBar.setComponentAlignment(cancelarBtn, Alignment.MIDDLE_LEFT);
        actionBar.setComponentAlignment(getAceptarBtn(),  Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(actionBar);
    }

    // ── Utilidades ───────────────────────────────────────────────────────────
    private NumberField buildNumberField(String caption) {
        NumberField field = new NumberField(caption);
        field.setDecimalAllowed(true);
        field.setDecimalPrecision(2);
        field.setMinimumFractionDigits(2);
        field.setDecimalSeparator('.');
        field.setDecimalSeparatorAlwaysShown(true);
        field.setValue(0d);
        field.setGroupingUsed(true);
        field.setGroupingSeparator(',');
        field.setGroupingSize(3);
        field.setImmediate(true);
        field.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        return field;
    }

    // ── Getters públicos para uso externo ─────────────────────────────────────
    public double getMontoAnticipo() {
        return montoAnticipoTxt.getDoubleValueDoNotThrow();
    }

    public double getMontoCheque() {
        return montoChequeTxt.getDoubleValueDoNotThrow();
    }

    public void setSaldoDocumento(String saldo) {
        saldoDocumentoLbl.setValue("Saldo documento:  " +  moneda + " " + saldo);
    }

    public void setSaldoAnticipos(double saldo) {
        saldoAnticiposLbl.setValue("Saldo anticipos:  " + moneda + " " + NUMBER_FORMAT.format(saldo));
        saldoAnticipos = saldo;
    }

    public void setMontoAnticipo(double monto) {
        montoAnticipoTxt.setReadOnly(false);
        montoAnticipoTxt.setValue(monto);
        if(monto == 0) {
            montoAnticipoTxt.setReadOnly(true);
        }
    }

    public void setMontoCheque(double monto) {
        montoChequeTxt.setValue(monto);
    }

    public Button getAceptarBtn() {
        return aceptarBtn;
    }

    public void setAceptarBtn(Button aceptarBtn) {
        this.aceptarBtn = aceptarBtn;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public double getSaldoDocumento() {
        return saldoDocumento;
    }

    public void setSaldoDocumento(double saldoDocumento) {
        this.saldoDocumento = saldoDocumento;
    }

    public double getSaldoAnticipos() {
        return saldoAnticipos;
    }
}
