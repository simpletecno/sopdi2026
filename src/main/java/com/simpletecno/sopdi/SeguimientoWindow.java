/*
 * Ventana para reigtro de Seguimiento de Visitas.
 * ...
 * @author Jose Aguirre
 */
package com.simpletecno.sopdi;

import com.simpletecno.sopdi.operativo.InspectionsTaskTrackView;
import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.utilerias.TicketsSoporteView;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import javax.mail.MessagingException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class SeguimientoWindow extends Window {

    Button acceptBtn;
    Button acceptAndCloseBtn;
    Button exitBtn;

    GridLayout gridLayout;
    TextField casoTxt;
    TextField nombreTxt;
    public TextArea seguimientoTxt;
    DateField fechaProximoSeguimientoDt;

    /** true si el seguimiento pertenece a una visita/reunión (soporta recordatorios). */
    boolean esVisita;

    UI mainUI;
    String registroId;
    String registroNombre;
    String estatus;
    String mailTo;

    private final VerticalLayout mainLayout = new VerticalLayout();

    Statement stQuery = null;
    SeguimientoHandler seguimientoHandler;

    public SeguimientoWindow(
            Object seguimientoHandler,
            String registroId,
            String registroNombre,
            String estatus,
            String mailTo) {

        this.mainUI = UI.getCurrent();
        if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("InspectionsTaskTrackView") == 0
                || mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TaskView") == 0) {
            this.seguimientoHandler = (SeguimientoHandler) seguimientoHandler;
        } else {
            this.seguimientoHandler = null;
        }
        this.registroId = registroId;
        this.registroNombre = registroNombre;
        this.estatus = estatus;
        this.mailTo = mailTo;

        this.setCaption("SOPDI -- Registro de seguimiento");

        gridLayout = new GridLayout(4, 4);
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);

        casoTxt = new TextField("Id : ");
        casoTxt.setValue(String.valueOf(registroId));
        casoTxt.setReadOnly(true);

        nombreTxt = new TextField("Nombre : ");
        nombreTxt.setWidth("500px");
        nombreTxt.setValue(registroNombre);
        nombreTxt.setReadOnly(true);

        seguimientoTxt = new TextArea("Texto del seguimiento u observación:");
        seguimientoTxt.setWidth("50em");
        seguimientoTxt.setHeight("10em");
        seguimientoTxt.setDescription("<<ingrese aqui el texto del seguimiento>>");
//        seguimientoTxt.addStyleName("v-textfield-uppercase");

        // Solo las visitas/reuniones soportan el recordatorio de próximo seguimiento.
        this.esVisita = mainUI.getNavigator().getCurrentView().getClass()
                .getSimpleName().equals("InspectionsTaskTrackView");

        fechaProximoSeguimientoDt = new DateField("Fecha próximo seguimiento (opcional) :");
        fechaProximoSeguimientoDt.setDateFormat("dd/MM/yyyy");
        fechaProximoSeguimientoDt.setResolution(Resolution.DAY);
        fechaProximoSeguimientoDt.setDescription(
                "Si indica una fecha, este seguimiento aparecerá como recordatorio en pantalla a partir de ese día.");
        fechaProximoSeguimientoDt.setVisible(esVisita);

        gridLayout.addComponent(casoTxt, 0, 0);
        gridLayout.addComponent(nombreTxt, 1, 0, 2, 0);
        if (esVisita) {
            gridLayout.addComponent(fechaProximoSeguimientoDt, 0, 1, 1, 1);
        }

        gridLayout.addComponent(seguimientoTxt, 0, 3, 3, 3);

        seguimientoTxt.focus();

        mainLayout.addComponent(gridLayout);
        mainLayout.setComponentAlignment(gridLayout, Alignment.TOP_CENTER);

        acceptBtn = new Button("Guardar");
        acceptBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        acceptBtn.setIcon(FontAwesome.SAVE);
        acceptBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                validarYGuardar(false);
            }
        });

        acceptAndCloseBtn = new Button("Guardar y Cerrar Ticket");
        acceptAndCloseBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        acceptAndCloseBtn.setIcon(FontAwesome.SAVE);
        acceptAndCloseBtn.setVisible(false);
        acceptAndCloseBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                validarYGuardar(true);
            }
        });
        if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TicketsSoporteView") == 0) {
            acceptAndCloseBtn.setVisible(true);
        }

        exitBtn = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(acceptBtn);
        buttonsLayout.setComponentAlignment(acceptBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(acceptAndCloseBtn);
        buttonsLayout.setComponentAlignment(acceptAndCloseBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        setContent(mainLayout);

        setWidth("900px");
        setHeight("400px");

        setModal(true);
    }

    public void validarYGuardar(boolean cerrarTicket) {
        if (!datosValidos()) {
            return;
        }

        try {
            String queryString = " Insert Into ";

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("InspectionsTaskTrackView") == 0) {
                queryString += "visita_inspeccion_tarea_seguimiento (FechaYHora, IdVisitaInspeccionTarea, IdUsuario, ";
            }
            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("ChangeOrdersView") == 0) {
                queryString += "orden_cambio_seguimiento (FechaYHora, IdOrdenCambio, IdUsuario, ";
            }
            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TaskView") == 0) {
                queryString += "tarea_seguimiento (FechaYHora, IdTarea, IdUsuario, ";
            }
            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TicketsSoporteView") == 0) {
                queryString += "ticket_soporte_seguimiento (CreadoFechaYHora, IdTicket, CreadoUsuario, ";
            }
            // Columnas extra solo para visitas/reuniones (recordatorio).
            String fechaProximoValor = null;
            if (esVisita) {
                queryString += "FechaProximoSeguimiento, Atendido, ";
                fechaProximoValor = (fechaProximoSeguimientoDt.getValue() == null)
                        ? "NULL"
                        : "'" + Utileria.getFechaYYYYMMDD_1(fechaProximoSeguimientoDt.getValue()) + "'";
            }
            queryString += "  Observacion)";
            queryString += "  Values (";
            queryString += "  current_timestamp";
            queryString += ", " + registroId;
            queryString += ", " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            if (esVisita) {
                queryString += ", " + fechaProximoValor + ", 0";
            }
            queryString += ",'" + seguimientoTxt.getValue() + "'";
            queryString += ")";

            stQuery = ((SopdiUI) getUI()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            // Un seguimiento nuevo cierra los recordatorios pendientes previos de la misma tarea.
            if (esVisita) {
                stQuery.executeUpdate("UPDATE visita_inspeccion_tarea_seguimiento"
                        + " SET Atendido = 1"
                        + " WHERE IdVisitaInspeccionTarea = " + registroId
                        + " AND Atendido = 0"
                        + " AND IdSeguimiento <> LAST_INSERT_ID()");
            }

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TicketsSoporteView") == 0) {
                queryString = "Update ticket_soporte Set ";
                if (cerrarTicket) {
                    queryString += " Estatus = 'CERRADO' ";
                    ((TicketsSoporteView) (mainUI.getNavigator().getCurrentView())).ticketsContainer.getContainerProperty(((TicketsSoporteView) (mainUI.getNavigator().getCurrentView())).ticketsGrid.getSelectedRow(), TicketsSoporteView.ESTATUS_PROPERTY).setValue("CERRADO");
                } else {
                    queryString += " Estatus = 'EN PROCESO' ";
                    ((TicketsSoporteView) (mainUI.getNavigator().getCurrentView())).ticketsContainer.getContainerProperty(((TicketsSoporteView) (mainUI.getNavigator().getCurrentView())).ticketsGrid.getSelectedRow(), TicketsSoporteView.ESTATUS_PROPERTY).setValue("EN PROCESO");
                }
                queryString += " Where IdTicket = " + registroId;
                stQuery.executeUpdate(queryString);
            }

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("InspectionsTaskTrackView") == 0
                    || mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TaskView") == 0) {
                seguimientoHandler.fillTrackTable(registroId, registroNombre);
                ((InspectionsTaskTrackView)(mainUI.getNavigator().getCurrentView())).llenarNotasGrid();
            } else {
//                ((InspectionsView)(mainUI.getNavigator().getCurrentView())).fillTrackTable();
            }
            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("TicketsSoporteView")) {
                ((TicketsSoporteView) (mainUI.getNavigator().getCurrentView())).fillSeguimientoGrid();
            }

            close();

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TicketsSoporteView") == 0) {
                try {
                    String[] emailsTo = {mailTo};
                    MyEmailMessanger eMail = new MyEmailMessanger();

                    String texto = "DESCRIPCION : " + nombreTxt.getValue() + "\n";
                    texto += "EMPRESA     : " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName() + "\n";
                    texto += "CREADO POR  : " + ((SopdiUI) mainUI).sessionInformation.getStrUserFullName() + "\n";
                    texto += "ESTATUS     : " + estatus + "\n";
                    texto += "OBSERVACION : " + seguimientoTxt.getValue() + "\n";
                    texto += "\n\n\n***Creado automaticamente por el sistema SOPDI.***";

                    eMail.postMail(emailsTo, "SOPDI : Ticket de soporte : " + nombreTxt.getValue(), texto);
                } catch (MessagingException ex2) {
                    Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
                }
            }

        } catch (Exception ex1) {
            Logger.getLogger(SeguimientoWindow.class.getName()).log(Level.SEVERE, ex1.getMessage());
            Notification.show("ERROR FATAL DEL SISTEMA");
            System.out.println("ERROR AL INTENTAR INSERTAR SEGUIMIENTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    private boolean datosValidos() {
        if (String.valueOf(seguimientoTxt.getValue()).length() == 0) {
            Notification.show("No ha ingresado el seguimiento.", Notification.Type.ERROR_MESSAGE);
            seguimientoTxt.focus();
            return false;
        }
        return true;
    }
}
