/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author user
 */
public class IngresosVariadosForm extends Window {

    UI mainUI;

    VerticalLayout mainLayout;
    HorizontalLayout layoutTitle;
    Label titleLbl;

    Button prestamosBtn;
    Button engancheBtn;
    Button ventaMonedaBtn;
    Button interesesDevengadosBtn;
    Button reembolsosAnticiposBtn;
    Button reembolsosSueldosBtn;
    Button pagoDocumentosVentaBtn;
    Button anticiposClientesBtn;
    Button anticiposEmpresaRelacionadaBtn;
    Button cobroServicioBancoBtn;
    Button chequeDevueltoBtn;
    Button redepositoClienteBtn;
    Button prestamosTercerosBtn;
    Button chequesTesoreriaBtn;

    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    String horizontal_height_percent = "65%";
    String horizontal_width_percent = "90%";

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public IngresosVariadosForm(String empresa) {

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("87%");
        setHeight("85%");
        setModal(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        mainLayout.setResponsive(true);

        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        titleLbl = new Label(empresaId + " " + empresaNombre + " INGRESO A BANCOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        crearBotones();

    }

    public void crearBotones() {

        prestamosBtn = new Button("PRESTAMOS");
        prestamosBtn.setIcon(FontAwesome.BUILDING);
        prestamosBtn.setWidth("18em");
        prestamosBtn.setHeight("5em");
        prestamosBtn.setDescription("Ingreso de prestamos.");
        prestamosBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoPrestamosForm ingresoPrestamosForm = new IngresoPrestamosForm("");
                mainUI.addWindow(ingresoPrestamosForm);
                ingresoPrestamosForm.center();

            }
        });

        engancheBtn = new Button("ENGANCHES");
        engancheBtn.setIcon(FontAwesome.HOUZZ);
        engancheBtn.setDescription("Ingreso de enganches.");
        engancheBtn.setWidth("18em");
        engancheBtn.setHeight("5em");
        engancheBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoEnganchesForm ingresoEnganchesForm = new IngresoEnganchesForm("");
                mainUI.addWindow(ingresoEnganchesForm);
                ingresoEnganchesForm.center();

            }
        });

        ventaMonedaBtn = new Button("DEPOSITO VENTA DE MONEDA");
        ventaMonedaBtn.setIcon(FontAwesome.BANK);
        ventaMonedaBtn.setDescription("Ingreso de deposito por venta de moneda");
        ventaMonedaBtn.setWidth("18em ");
        ventaMonedaBtn.setHeight("5em");
        ventaMonedaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoDepositoVentaMonedaForm depositoMonedaForm = new IngresoDepositoVentaMonedaForm("");
                mainUI.addWindow(depositoMonedaForm);
                depositoMonedaForm.center();

            }
        });

        interesesDevengadosBtn = new Button("INTERESES DEVENGADOS");
        interesesDevengadosBtn.setIcon(FontAwesome.BATTERY_1);
        interesesDevengadosBtn.setDescription("Ingreso de intereses devengados");
        interesesDevengadosBtn.setWidth("18em");
        interesesDevengadosBtn.setHeight("5em");
        interesesDevengadosBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                IngresoInteresesDevengadosForm ingresoInteresesDevengadosForm = new IngresoInteresesDevengadosForm("");
                mainUI.addWindow(ingresoInteresesDevengadosForm);
                ingresoInteresesDevengadosForm.center();

            }
        });

        reembolsosAnticiposBtn = new Button("REEMBOLSO DE ANTICIPOS");
        reembolsosAnticiposBtn.setIcon(FontAwesome.BATTERY_3);
        reembolsosAnticiposBtn.setDescription("Ingreso de reembolsos de anticipos.");
        reembolsosAnticiposBtn.setWidth("18em");
        reembolsosAnticiposBtn.setHeight("5em");
        reembolsosAnticiposBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                IngresoReembolsoAnticiposForm reembolsoAnticiposForm = new IngresoReembolsoAnticiposForm("");
                mainUI.addWindow(reembolsoAnticiposForm);
                reembolsoAnticiposForm.center();

            }
        });

        reembolsosSueldosBtn = new Button("REEMBOLSO DE SUELDOS");
        reembolsosSueldosBtn.setIcon(FontAwesome.BATTERY_1);
        reembolsosSueldosBtn.setDescription("Ingreso de reembolsos de sueldos.");
        reembolsosSueldosBtn.setWidth("18em");
        reembolsosSueldosBtn.setHeight("5em");
        reembolsosSueldosBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoReembolsoSueldo reembolsoSueldosForm = new IngresoReembolsoSueldo();
                mainUI.addWindow(reembolsoSueldosForm);
                reembolsoSueldosForm.center();

            }
        });

        pagoDocumentosVentaBtn = new Button("PAGO DE DOCUMENTO VENTA");
        pagoDocumentosVentaBtn.setIcon(FontAwesome.BARCODE);
        pagoDocumentosVentaBtn.setDescription("Ingreso de pago de documento venta.");
        pagoDocumentosVentaBtn.setWidth("18em");
        pagoDocumentosVentaBtn.setHeight("5em");
        pagoDocumentosVentaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                PagoDocumentoVentaForm pagoDocumentoVentaForm = new PagoDocumentoVentaForm();
                mainUI.addWindow(pagoDocumentoVentaForm);
                pagoDocumentoVentaForm.center();

            }
        });

        anticiposClientesBtn = new Button("ANTICIPO DE CLIENTES");
        anticiposClientesBtn.setIcon(FontAwesome.BARCODE);
        anticiposClientesBtn.setDescription("Ingreso de anticipo de clientes.");
        anticiposClientesBtn.setWidth("18em");
        anticiposClientesBtn.setHeight("5em");
        anticiposClientesBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoAnticipoClientesForm ingresoAnticipoClientesForm = new IngresoAnticipoClientesForm();
                mainUI.addWindow(ingresoAnticipoClientesForm);
                ingresoAnticipoClientesForm.center();
            }
        });

        anticiposEmpresaRelacionadaBtn = new Button("ANTICIPO DE EMPRESA REL.");
        anticiposEmpresaRelacionadaBtn.setIcon(FontAwesome.BARCODE);
        anticiposEmpresaRelacionadaBtn.setDescription("Ingreso de anticipo de EMPRESA RELACIONADA (TRASLADO DE FONDOS).");
        anticiposEmpresaRelacionadaBtn.setWidth("18em");
        anticiposEmpresaRelacionadaBtn.setHeight("5em");
        anticiposEmpresaRelacionadaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoAnticipoEmpresaRelacionadaForm ingresoAnticipoEmpresaRelacionadaForm = new IngresoAnticipoEmpresaRelacionadaForm();
                mainUI.addWindow(ingresoAnticipoEmpresaRelacionadaForm);
                ingresoAnticipoEmpresaRelacionadaForm.center();
            }
        });

        cobroServicioBancoBtn = new Button("DEBITO COBRO BANCO");
        cobroServicioBancoBtn.setIcon(FontAwesome.BANK);
        cobroServicioBancoBtn.addStyleName("buttonred");
        cobroServicioBancoBtn.setDescription("Nota de débito por cobro de banco.");
        cobroServicioBancoBtn.setWidth("18em");
        cobroServicioBancoBtn.setHeight("5em");
        cobroServicioBancoBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                NotaDebitoBancoForm notaDebitoBancoForm = new NotaDebitoBancoForm(NotaDebitoBancoForm.OTROS_COBROS_BANCARIOS);
                mainUI.addWindow(notaDebitoBancoForm);
                notaDebitoBancoForm.center();
            }
        });

        chequeDevueltoBtn = new Button("DEBITO CHEQUE DEVUELTO");
        chequeDevueltoBtn.setIcon(FontAwesome.BANK);
        chequeDevueltoBtn.addStyleName("buttonred");
        chequeDevueltoBtn.setDescription("Nota de débito por contracargo (cheque devuelto).");
        chequeDevueltoBtn.setWidth("18em");
        chequeDevueltoBtn.setHeight("5em");
        chequeDevueltoBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                NotaDebitoBancoForm notaDebitoBancoForm = new NotaDebitoBancoForm(NotaDebitoBancoForm.DEBITO_CHEQUE_DEVUELTO);
                mainUI.addWindow(notaDebitoBancoForm);
                notaDebitoBancoForm.center();
            }
        });

        redepositoClienteBtn = new Button("RE DEPOSITO CHEQUE DVTO");
        redepositoClienteBtn.setIcon(FontAwesome.REPEAT);
        redepositoClienteBtn.setDescription("VOLVER A DEPOSITAR CHEQUE DEVUELTO.");
        redepositoClienteBtn.setWidth("18em");
        redepositoClienteBtn.setHeight("5em");
        redepositoClienteBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoRedepositoChequeDevueltoForm ingresoRedepositoChequeDevueltoForm = new IngresoRedepositoChequeDevueltoForm();
                mainUI.addWindow(ingresoRedepositoChequeDevueltoForm);
                ingresoRedepositoChequeDevueltoForm.center();
            }
        });

        prestamosTercerosBtn = new Button("PRESTAMOS DE TERCEROS");
        prestamosTercerosBtn.setIcon(FontAwesome.USER);
        prestamosTercerosBtn.setDescription("Ingrear prestomo ajeno al banco.");
        prestamosTercerosBtn.setWidth("18em");
        prestamosTercerosBtn.setHeight("5em");
        prestamosTercerosBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoPrestamosTercerosForm ingresoPrestamosTercerosForm = new IngresoPrestamosTercerosForm("");
                mainUI.addWindow(ingresoPrestamosTercerosForm);
                ingresoPrestamosTercerosForm.center();
            }
        });

        chequesTesoreriaBtn = new Button("CHEQUES EN TESORERIA");
        chequesTesoreriaBtn.setIcon(FontAwesome.MONEY);
        chequesTesoreriaBtn.setDescription("Cheques generados pero no cobrados");
        chequesTesoreriaBtn.setWidth("18em");
        chequesTesoreriaBtn.setHeight("5em");
        chequesTesoreriaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ChequeTesoreriaForm chequeTesoreriaForm = new ChequeTesoreriaForm();
                mainUI.addWindow(chequeTesoreriaForm);
                chequeTesoreriaForm.center();
            }
        });

    //  -----  Layout de los botones
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.setHeight(horizontal_height_percent);
        buttonsLayout.setWidth(horizontal_width_percent);
        buttonsLayout.setSizeUndefined();
        buttonsLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout.addComponents(prestamosBtn, engancheBtn, anticiposClientesBtn);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        HorizontalLayout buttonsLayout2 = new HorizontalLayout();
        buttonsLayout2.setSpacing(true);
        buttonsLayout2.setMargin(false);
        buttonsLayout2.setHeight(horizontal_height_percent);
        buttonsLayout2.setWidth(horizontal_width_percent);
        buttonsLayout2.setSizeUndefined();
        buttonsLayout2.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout2.addComponents(pagoDocumentosVentaBtn, reembolsosAnticiposBtn, reembolsosSueldosBtn);

        mainLayout.addComponent(buttonsLayout2);
        mainLayout.setComponentAlignment(buttonsLayout2, Alignment.BOTTOM_CENTER);

        HorizontalLayout buttonsLayout3 = new HorizontalLayout();
        buttonsLayout3.setSpacing(true);
        buttonsLayout3.setMargin(false);
        buttonsLayout3.setHeight(horizontal_height_percent);
        buttonsLayout3.setWidth(horizontal_width_percent);
        buttonsLayout3.setSizeUndefined();
        buttonsLayout3.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout3.addComponents(interesesDevengadosBtn, ventaMonedaBtn, anticiposEmpresaRelacionadaBtn);

        mainLayout.addComponent(buttonsLayout3);
        mainLayout.setComponentAlignment(buttonsLayout3, Alignment.BOTTOM_CENTER);

        HorizontalLayout buttonsLayout4 = new HorizontalLayout();
        buttonsLayout4.setSpacing(true);
        buttonsLayout4.setMargin(false);
        buttonsLayout4.setHeight(horizontal_height_percent);
        buttonsLayout4.setWidth(horizontal_width_percent);
        buttonsLayout4.setSizeUndefined();
        buttonsLayout4.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout4.addComponents(cobroServicioBancoBtn, chequeDevueltoBtn, redepositoClienteBtn);

        mainLayout.addComponent(buttonsLayout4);
        mainLayout.setComponentAlignment(buttonsLayout4, Alignment.BOTTOM_CENTER);

        HorizontalLayout buttonsLayout5 = new HorizontalLayout();
        buttonsLayout5.setSpacing(true);
        buttonsLayout5.setMargin(false);
        buttonsLayout5.setHeight(horizontal_height_percent);
        buttonsLayout5.setWidth(horizontal_width_percent);
        buttonsLayout5.setSizeUndefined();
        buttonsLayout5.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        buttonsLayout5.addComponents(prestamosTercerosBtn, chequesTesoreriaBtn);

        mainLayout.addComponent(buttonsLayout5);
        mainLayout.setComponentAlignment(buttonsLayout5, Alignment.BOTTOM_CENTER);
    }
}
