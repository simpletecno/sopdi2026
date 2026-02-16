package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.ui.*;
import com.vaadin.data.util.BeanItemContainer;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;


public class EmpresaCuentaEquivalenteResumen extends Window {

    private static final String PROPERTY_DOCUMENTO = "documento";
    private static final String PROPERTY_PARTIDA = "partida";
    private static final String PROPERTY_CC = "partidaCC";
    private static final String PROPERTY_MONEDA = "moneda";
    private static final String PROPERTY_DEBE = "debe";
    private static final String PROPERTY_HABER = "haber";
    private static final String PROPERTY_DEBE_QUETZALES = "debeQuetzales";
    private static final String PROPERTY_HABER_QUETZALES = "haberQuetzales";

    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    UI mainUI;

    int idEmpresa;
    int idProveedor;
    int idNomenclatura;
    String nomenclaturaNombre;
    int idEmpresa_1;
    int idProveedor_1;
    int idNomenclatura_1;;
    String nomenclaturaNombre_1;

    BigDecimal[] totales = new BigDecimal[4]; // Debe, Haber, Debe Qtz, Haber Qtz
    BigDecimal[] totales_1 = new BigDecimal[4]; // Debe, Haber, Debe Qtz, Haber Qtz

    public EmpresaCuentaEquivalenteResumen(int idEmpresa, int idProveedor, int idNomenclatura, String nomenclaturaNombre,
                                           int idEmpresa_1, int idProveedor_1, int idNomenclatura_1, String nomenclaturaNombre_1) {
        this.mainUI = UI.getCurrent();

        this.idEmpresa = idEmpresa;
        this.idProveedor = idProveedor;
        this.idNomenclatura = idNomenclatura;
        this.nomenclaturaNombre = nomenclaturaNombre;
        this.idEmpresa_1 = idEmpresa_1;
        this.idProveedor_1 = idProveedor_1;
        this.idNomenclatura_1 = idNomenclatura_1;
        this.nomenclaturaNombre_1 = nomenclaturaNombre_1;


        setCaption("Resumen de Cuentas Equivalentes");
        setWidth("90%");
        setHeight("80%");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);

        Table tablaIzquierda = new Table("Izquierda");
        tablaIzquierda.addContainerProperty(PROPERTY_DOCUMENTO, String.class, null);
        tablaIzquierda.addContainerProperty(PROPERTY_PARTIDA, String.class, null);
        tablaIzquierda.addContainerProperty(PROPERTY_CC, String.class, null);
        tablaIzquierda.addContainerProperty(PROPERTY_MONEDA, String.class, null);
        tablaIzquierda.addContainerProperty(PROPERTY_DEBE, Double.class, null);
        tablaIzquierda.addContainerProperty(PROPERTY_HABER, Double.class, null);
        tablaIzquierda.addContainerProperty(PROPERTY_DEBE_QUETZALES, Double.class, null);
        tablaIzquierda.addContainerProperty(PROPERTY_HABER_QUETZALES, Double.class, null);

        Table tablaDerecha = new Table("Derecha");
        tablaDerecha.addContainerProperty(PROPERTY_DOCUMENTO, String.class, null);
        tablaDerecha.addContainerProperty(PROPERTY_PARTIDA, String.class, null);
        tablaDerecha.addContainerProperty(PROPERTY_CC, String.class, null);
        tablaDerecha.addContainerProperty(PROPERTY_MONEDA, String.class, null);
        tablaDerecha.addContainerProperty(PROPERTY_DEBE, Double.class, null);
        tablaDerecha.addContainerProperty(PROPERTY_HABER, Double.class, null);
        tablaDerecha.addContainerProperty(PROPERTY_DEBE_QUETZALES, Double.class, null);
        tablaDerecha.addContainerProperty(PROPERTY_HABER_QUETZALES, Double.class, null);

        tablaIzquierda.setWidth("100%");
        tablaDerecha.setWidth("100%");

        layout.addComponent(tablaIzquierda);
        layout.addComponent(tablaDerecha);

        setContent(layout);
    }

    private void llenarTabla(Table table, int idEmpresa, int idProveedor, int idNomenclatura) {
        // Lógica para llenar las tablas con datos
        queryString = "SELECT cp.NumeroDocumento, cp.CodigoPartida, cp.CodigoCC, cp.MonedaDocumento, \n" +
                "cp.Debe, cp.DebeQuetzales, cp.Haber, cp.HaberQuetzales \n" +
                "FROM contabilidad_partida cp\n" +
                "WHERE cp.IdEmpresa = " + idEmpresa + "\n" +
                "AND cp.IdNomenclatura = " + idNomenclatura + "\n" +
                "AND cp.IdProveedor = " + idProveedor;

        try {
            stQuery = ((SopdiUI)mainUI).databaseProvider.getCurrentConnection().createStatement();

        }catch (Exception e) {
            Notification.show("ERROR AL LLENAR TABLA DERECHA, ", Notification.Type.WARNING_MESSAGE);
            e.printStackTrace();
        }

    }

}

