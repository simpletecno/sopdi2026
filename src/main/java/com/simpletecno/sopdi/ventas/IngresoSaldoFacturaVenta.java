package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import org.vaadin.ui.NumberField;

public class IngresoSaldoFacturaVenta extends Window {

    NumberField montoPagarTxt;
    NumberField totalEnganchesTxt;
    VerticalLayout mainLayout;
    Button guardarBtn;
    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords1;
    String queryString;

    double totalEnganches = 0.00;

    public String codigoPartida;
    public String saldo;
    String proveedor;
    String facturaNumero;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public IngresoSaldoFacturaVenta(String empresa1, String codigoPartida, String Saldo, String proveedor, String numeroFactura) {

        this.codigoPartida = codigoPartida;
        this.saldo = Saldo;
        this.proveedor = proveedor;
        this.facturaNumero = numeroFactura;
        setModal(true);
        setResponsive(true);

        System.out.println("SALDO" + saldo);
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " Ingrese el monto.");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        montoPagarTxt = new NumberField("Total a pagar:");
        montoPagarTxt.setDecimalAllowed(true);
        montoPagarTxt.setDecimalPrecision(2);
        montoPagarTxt.setMinimumFractionDigits(2);
        montoPagarTxt.setDecimalSeparator('.');
        montoPagarTxt.setDecimalSeparatorAlwaysShown(true);
        montoPagarTxt.setValue(0d);
        montoPagarTxt.setGroupingUsed(true);
        montoPagarTxt.setGroupingSeparator(',');
        montoPagarTxt.setGroupingSize(3);
        montoPagarTxt.setImmediate(true);
        montoPagarTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoPagarTxt.setWidth("10em");
        System.out.println("$.Dolares".replaceAll("\\$.", ""));
        montoPagarTxt.setValue(Double.valueOf(saldo.replaceAll(",", "").replaceAll("\\$.", "").replaceAll("Q.", "")));

        totalEnganchesTxt = new NumberField("Total de enganches :");
        totalEnganchesTxt.setDecimalAllowed(true);
        totalEnganchesTxt.setDecimalPrecision(2);
        totalEnganchesTxt.setMinimumFractionDigits(2);
        totalEnganchesTxt.setDecimalSeparator('.');
        totalEnganchesTxt.setDecimalSeparatorAlwaysShown(true);
        totalEnganchesTxt.setValue(0d);
        totalEnganchesTxt.setGroupingUsed(true);
        totalEnganchesTxt.setGroupingSeparator(',');
        totalEnganchesTxt.setGroupingSize(3);
        totalEnganchesTxt.setImmediate(true);
        totalEnganchesTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        totalEnganchesTxt.setWidth("10em");

        guardarBtn = new Button("Aplicar");
        guardarBtn.setIcon(FontAwesome.CHECK);
        guardarBtn.setDescription("Aplicar el monto.");
        guardarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (montoPagarTxt.getDoubleValueDoNotThrow() > totalEnganchesTxt.getDoubleValueDoNotThrow() || montoPagarTxt.getDoubleValueDoNotThrow() <= 0.00) {
                    Notification.show("El monto a rebajar no puede ser mayor al monto del los enganches. Y tampoco puede ser igual a 0.", Notification.Type.WARNING_MESSAGE);                    
                } else {
                    ActualizarSaldo();
                    close();
                }
            }
        });
        mainLayout.addComponent(titleLbl);
        mainLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        mainLayout.addComponent(montoPagarTxt);
        mainLayout.setComponentAlignment(montoPagarTxt, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(totalEnganchesTxt);
        mainLayout.setComponentAlignment(totalEnganchesTxt, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(guardarBtn);
        mainLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        setContent(mainLayout);
        buscarTotalEnganches();
    }

    public void buscarTotalEnganches() {

        queryString = " SELECT *";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE TipoDocumento = 'ENGANCHES'";
        queryString += " AND NombreProveedor = '" + proveedor + "'";

        System.out.println("query" + queryString);

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) {
                totalEnganches += rsRecords1.getDouble("Saldo");
                System.out.println("total de enganches" + totalEnganches);
            }

            totalEnganchesTxt.setValue(totalEnganches);
            totalEnganchesTxt.setReadOnly(true);

        } catch (SQLException ex) {
            System.out.println("Error al buscar enganches del cliente" + ex);
            ex.printStackTrace();
        }
    }

    public void ActualizarSaldo() {

        double nuevoSaldo = Double.valueOf(saldo.replaceAll(",", "").replaceAll("Q.", "").replaceAll("$.", ""))
                - montoPagarTxt.getDoubleValueDoNotThrow();

        System.out.println("Nuevo saldo" + nuevoSaldo);
        String queryString = "UPDATE contabilidad_partida";
        queryString += " SET Saldo = " + nuevoSaldo;
        queryString += " WHERE CodigoPartida = " + codigoPartida;
        queryString += " AND IdEmpresa = " + empresaId;

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            reducirEnganche();
            ((FacturaVentaView) (((SopdiUI) UI.getCurrent()).getNavigator().getCurrentView())).llenarTablaFacturaVenta();
            Notification.show("Autorización exitosa.");

            close();
        } catch (Exception ex) {
            Notification.show("Error al momento de actualizar el registro del documento Monto a pagar o Anticipo a aplicar.", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }

    }

    public void reducirEnganche() {

        queryString = " SELECT *";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE TipoDocumento = 'ENGANCHES'";
        queryString += " AND NombreProveedor = '" + proveedor + "'";
        queryString += " GROUP BY CodigoPartida";

        double montoPagar = montoPagarTxt.getDoubleValueDoNotThrow();
        double nuevoSaldo = 0.00;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) {

                if (rsRecords1.getDouble("Saldo") != 0.00) {

                    if (rsRecords1.getDouble("Saldo") <= montoPagar) {

                        montoPagar = montoPagar - rsRecords1.getDouble("Saldo");
                        nuevoSaldo = rsRecords1.getDouble("Saldo") - montoPagar;

                        if (nuevoSaldo <= 0 || nuevoSaldo > 0) {

                            queryString = "";
                            queryString = " UPDATE contabilidad_partida";
                            queryString += " SET Saldo = 0 ";
                            queryString += " ,Estatus = 'PAGADO'";
                            queryString += " ,NoDOCA = '" + facturaNumero + "'";
                            queryString += " ,TipoDOCA = 'FACTURA VENTA'";
                            queryString += " WHERE CodigoPartida = " + rsRecords1.getString("CodigoPartida");
                            queryString += " AND Haber = 0.00";

                            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                            stQuery2.executeUpdate(queryString);
                        }

                    } else {

                        montoPagar = rsRecords1.getDouble("Saldo") - montoPagar;
                        queryString = "";
                        queryString = " UPDATE contabilidad_partida";
                        queryString += " SET Saldo = " + montoPagar;
                        queryString += " ,Estatus = 'PAGADO'";
                        queryString += " ,NoDOCA = '" + facturaNumero + "'";
                        queryString += " ,TipoDOCA = 'FACTURA VENTA'";
                        queryString += " WHERE CodigoPartida = " + rsRecords1.getString("CodigoPartida");
                        queryString += " AND Haber = 0.00";

                        stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        stQuery2.executeUpdate(queryString);
                    }
                }
            }

        } catch (SQLException ex) {
            System.out.println("Error al buscar enganches del cliente" + ex);
            ex.printStackTrace();
        }
    }
}
