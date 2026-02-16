package com.simpletecno.sopdi.ventas;


import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.extras.infile.InfileClient;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
//import guatefac.Guatefac;
//import guatefac.Guatefac_Service;

import javax.mail.MessagingException;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FacturaVentaAnularForm extends Window {
    VerticalLayout mainLayout;

    File pdfFile = null;
    String filePath = "";

    /* title */
    ComboBox empresaCbx;
    DateField fechaDt;

    TextArea motivo;

    Label codigoPartidaLabel;
    
    Button anularBtn;

    UI mainUI;
    InfileClient infileClient;
    
    String codigoPartida;
    String codigoCC;
    String tipoDocumento;
    int cuota;

    public FacturaVentaAnularForm(String codigoPartida, String codigoCC, String tipoDocumento, int cuota){
        this.codigoPartida = codigoPartida;
        this.codigoCC = codigoCC;
        this.tipoDocumento = tipoDocumento;
        this.cuota = cuota;
        
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setMargin(false);
        mainLayout.setWidth("100%");

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true, false, true));
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("80%");
        //setHeight("50%");
        infileClient = new InfileClient(((SopdiUI) mainUI).sessionInformation.getInfileEmisor());

                HorizontalLayout horizontalLayout1 = new HorizontalLayout();
        horizontalLayout1.setSizeFull();
        horizontalLayout1.setSpacing(true);
        horizontalLayout1.setMargin(new MarginInfo(false, true, false, true));

        HorizontalLayout horizontalLayout2 = new HorizontalLayout();
        horizontalLayout2.setSizeFull();
        horizontalLayout2.setSpacing(true);
        horizontalLayout2.setMargin(new MarginInfo(false, true, false, true));
        horizontalLayout2.addStyleName("rcorners3");

        VerticalLayout verticalLayout1 = new VerticalLayout();
        verticalLayout1.setSpacing(true);
        verticalLayout1.setResponsive(true);
        verticalLayout1.setMargin(false);
        verticalLayout1.setWidth("100%");

        HorizontalLayout anularLayout = new HorizontalLayout();
        anularLayout.setSizeFull();
        anularLayout.setSpacing(true);
        anularLayout.setMargin(new MarginInfo(false, true, false, true));

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("95%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        empresaCbx.addItem(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());
        empresaCbx.setItemCaption(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId(), ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName() + " : " +  ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen());
        empresaCbx.select(empresaCbx.getItemIds().iterator().next());

        codigoPartidaLabel = new Label("Partida: " + codigoPartida);
        codigoPartidaLabel.addStyleName(ValoTheme.LABEL_H2);
        codigoPartidaLabel.setSizeUndefined();
        codigoPartidaLabel.addStyleName("h1_custom");
        
        horizontalLayout1.addComponents(empresaCbx, codigoPartidaLabel);
        horizontalLayout1.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);
        horizontalLayout1.setComponentAlignment(codigoPartidaLabel, Alignment.MIDDLE_RIGHT);
        
        
        fechaDt = new DateField("Fecha Anulacion: ");
        fechaDt.setWidth("10em");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setValue(new java.util.Date());

        motivo = new TextArea("Motivo :", "A solicitud del Cliente");
        motivo.setWidth("100%");

        horizontalLayout2.addComponents(verticalLayout1);
        verticalLayout1.addComponents(fechaDt, motivo);
        
        anularBtn = new Button("Anular");
        anularBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        anularBtn.setDescription("ANULAR DOCUMENTO");
        anularBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            anularDocumento(codigoPartida, tipoDocumento);
            closeWindow();
        });

        anularLayout.addComponent(anularBtn);
        anularLayout.setComponentAlignment(anularBtn, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponents(horizontalLayout1, horizontalLayout2, anularLayout);

        setContent(mainLayout);
    }

    private boolean validacion(String codigoPartida){

        Statement stQuery;
        String queryString;
        ResultSet rsRecords;

        queryString = "SELECT CodigoCC, IdEmpresa, IdNomenclatura, Fecha, Max(IdConciliacion) AS IdConciliacion, count(CodigoPartida) AS DocumentosAsociados ";
        queryString += "FROM contabilidad_partida ";
        queryString += "WHERE codigoCC = '" + codigoPartida + "' ";
        queryString += "AND Estatus <> 'ANULADO' ";
        queryString += "AND IdEmpresa = " + empresaCbx.getValue() + " ";
        queryString += "AND IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes() + "," +
                                                   ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes() + "," +
                                                   ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes() + ") ";
        queryString += "GROUP BY CodigoCC, IdEmpresa, IdNomenclatura";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR FACTURA VENTA  : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                if(rsRecords.getInt("IdConciliacion") != 0){
                    Notification.show("El Documento ya fue Conciliado, No se puede anular!", Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if(rsRecords.getInt("DocumentosAsociados") > 1){
                    Notification.show("El Documento tiene otros Documentos Dependientes, No se Puede anular!", Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                java.util.GregorianCalendar fechaCrea = new java.util.GregorianCalendar();
                fechaCrea.setTime(rsRecords.getDate("Fecha"));

                java.util.GregorianCalendar fechaAnulacion = new java.util.GregorianCalendar();
                fechaAnulacion.setTime(fechaDt.getValue());

                if((fechaCrea.get(java.util.GregorianCalendar.MONTH) + 1) < fechaAnulacion.get(java.util.GregorianCalendar.MONTH)){
                    Notification.show("El Documento NO se puede anular despues de 2 Meses de su creacion. Atencion!", Notification.Type.WARNING_MESSAGE);
                    fechaDt.focus();
                    return false;
                }

                if(fechaCrea.after(fechaAnulacion)) {
                    Notification.show("El Documento NO se puede anular antes de su creacion. Atencion!", Notification.Type.WARNING_MESSAGE);
                    fechaDt.focus();
                    return false;
                }
            }
            return true;

        } catch (Exception ex) {
            System.out.println("Error al leer tabla de DOCUMENTO VENTA : " + ex);
            Notification.show("ERROR AL LEER DOCUMENTO VENTA PARA ANULACION : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }

    }

    private void anularDocumento(String codgioPartida, String tipoDocumento) {
        if(!validacion(codgioPartida)) return;

        Statement stQuery;
        String queryString;
        ResultSet rsRecords;

        queryString = "SELECT * ";
        queryString += "FROM contabilidad_partida ";
        queryString += "WHERE codigoPartida = '" + codgioPartida + "' ";
        queryString += "AND IdEmpresa = " + empresaCbx.getValue() + " ";
        queryString += "AND IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor() + "," +
                                                    ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + ")";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR FACTURA VENTA  : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                if(rsRecords.getString("UUID") != null && !rsRecords.getString("UUID").isEmpty()) {

                    /** Infile FEL **/
                    boolean respuesta = infileClient.generarAnulacion(
                            rsRecords.getString("UUID"),
                            rsRecords.getString("NitProveedor"),
                            codgioPartida,
                            motivo.getValue(),
                            rsRecords.getDate("Fecha"),
                            fechaDt.getValue()
                    );

                    try {
                        if (respuesta) {
                            // Insertar documentoFel
                            queryString =   "INSERT INTO certificado_fel_infile (";
                            queryString +=  "Fecha, Origen, Descripcion, Saldo, Creditos, AlertasInfile, AlertasSat, ";
                            queryString +=  "InformacionAdicional, UUID, Serie, Numero, JsonResponse, CodigoPartida, IdEmpresa, Estado) ";
                            queryString +=  "VALUES (";
                            queryString +=  "'" +Utileria.getFechaYYYYMMDDHHMMSS(infileClient.getFechaHoraCertificacion()) + "'";
                            queryString +=  ", '" + infileClient.getOrigen() + "'";
                            queryString +=  ", '" + infileClient.getDescripcion() + "'";
                            queryString +=  ", '" + infileClient.getSaldo() + "'";
                            queryString +=  ", '" + infileClient.getCreditos() + "'";
                            queryString +=  ", " + infileClient.getAlertasInfile();
                            queryString +=  ", " + infileClient.getAlertasSAT();
                            queryString +=  ", '" + infileClient.getInformacionAdicional() + "'";
                            queryString +=  ", '" + infileClient.getUUID() + "'";
                            queryString +=  ", '" + infileClient.getSerie() + "'";
                            queryString +=  ", '" + infileClient.getNumero() + "'";
                            queryString +=  ", '" + infileClient.getRespuesta() + "'";
                            queryString +=  ", '" + codigoPartida + "'";
                            queryString +=  ", '" + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + "'";
                            queryString +=  ", 'ANULADO')";


                            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY DOCUMENTO ELECTROCNICO : " + queryString);

                            try {

                                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                stQuery.executeUpdate(queryString);

                                ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));

                                close();

                            } catch (Exception ex1) {
                                System.out.println("Error al insertar Documetno Electronico  : " + ex1.getMessage());
                                ex1.printStackTrace();
                                Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                                        Notification.Type.HUMANIZED_MESSAGE);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.setIcon(FontAwesome.WARNING);
                                notif.show(Page.getCurrent());

                                try {
                                    String emailsTo[] = {"alerta@simpletecno.com"};
                                    MyEmailMessanger eMail = new MyEmailMessanger();

                                    eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
                                } catch (MessagingException ex2) {
                                    Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
                                }
                            }


                            // Actualizacion contabilidad_partida
                            try {

                                queryString = "UPDATE contabilidad_partida ";
                                queryString += "set Estatus = 'ANULADO' ";
                                if(cuota==1) queryString += ",codigoCC = '" + codgioPartida + "' ";
                                queryString += "WHERE codigoPartida = '" + codgioPartida + "' ";
                                queryString += "And IdEmpresa = " + empresaCbx.getValue();

                                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                stQuery.executeUpdate(queryString);


                            } catch (SQLException ex) {
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL MODIFICAR PARTIDA ESTATUS ANULADA");
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "QUERY : " + queryString);
                                Notification.show("ERROR AL MODIFICAR PARTIDA ESTATUS ANULADA");
                                ex.printStackTrace();
                                return;
                            }

                            if (cuota == 1) {
                                try {

                                    queryString = "DELETE FROM contabilidad_partida ";
                                    queryString += "WHERE codigoPartida = '" + codigoCC + "' ";
                                    queryString += "And IdEmpresa = " + empresaCbx.getValue();

                                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                    stQuery.executeUpdate(queryString);


                                } catch (SQLException ex) {
                                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL MODIFICAR PARTIDA ESTATUS ANULADA : " + infileClient.getDescripcionErrores().toString());
                                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "QUERY : " + queryString);
                                    Notification.show("ERROR AL MODIFICAR PARTIDA ESTATUS ANULADA  : " + infileClient.getDescripcionErrores().toString(), Notification.Type.ERROR_MESSAGE);
                                    ex.printStackTrace();
                                }
                                try {

                                    queryString = "UPDATE cuotas AS c ";
                                    queryString += "JOIN cuotas_tipo AS ct ON c.TipoCuota = ct.IdCuota ";
                                    queryString += "JOIN Producto_venta_empresa AS pve ON ct.IdProducto = pve.CorrelativoProducto ";
                                    queryString += "JOIN contabilidad_partida AS cp ON cp.idProducto = pve.IdProducto ";
                                    queryString += "AND c.CodigoPartida = cp.CodigoPartida ";
                                    if (((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                                        queryString += "SET c.MontoPagado = GREATEST(0, c.MontoPagado - cp.MontoDocumento) ";
                                    } else {
                                        queryString += "SET c.MontoPagado = GREATEST(0, c.MontoPagado - ROUND((cp.MontoDocumento * 112) / 100, 2)) ";
                                    }
                                    queryString += "WHERE cp.Estatus = 'ANULADO' ";
                                    queryString += "AND cp.CodigoPartida = '" + codgioPartida + "' ";
                                    queryString += "AND cp.IdEmpresa = " + empresaCbx.getValue() + ";";

                                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                    stQuery.executeUpdate(queryString);


                                } catch (SQLException ex) {
                                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL MODIFICAR CUOTA MONTO ANULADA : " + infileClient.getDescripcionErrores().toString());
                                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "QUERY : " + queryString);
                                    Notification.show("ERROR AL MODIFICAR CUOTA MONTO ANULADA  : " + infileClient.getDescripcionErrores().toString(), Notification.Type.ERROR_MESSAGE);
                                    ex.printStackTrace();
                                }
                            }

                            InfileClient.obtenerDTEPdf(rsRecords.getString("UUID"), rsRecords.getString("ArchivoNombre"));
                            ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));
                        } else {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR EL DOCUMENTO DE VENTA : " + infileClient.getDescripcionErrores().toString());
                            Notification.show("ERROR AL CERTIFICAR ANULACION DE DOCUMENTO DE VENTA  : " + infileClient.getDescripcionErrores().toString(), Notification.Type.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        Notification.show("ERROR AL CERTIFICAR ANULACION DE DOCUMENTO DE VENTA  : " + e.getMessage());
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR ANULACION DE DOCUMENTO VENTA");
                        e.printStackTrace();
                    }
                }

            }

        } catch (Exception ex) {
            System.out.println("Error al leer tabla de DOCUMENTO VENTA : " + ex);
            Notification.show("ERROR AL LEER DOCUMENTO VENTA PARA ANULACION : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        Notification.show("ANULACION DE DOCUMENTO VENTA EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

    }

    private void closeWindow(){
        this.close();
    }

}
