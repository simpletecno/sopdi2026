package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.DescripcionWindow;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.util.List;

/**
 * Ventana modal que muestra, en un solo lugar, los recordatorios pendientes del
 * usuario: seguimientos y tareas vencidas de visitas/reuniones, y eventos de hoy
 * o vencidos de su calendario. Se abre al iniciar sesión (si hay pendientes) y
 * desde el botón campana del encabezado.
 *
 * @author Jose Aguirre
 */
@SuppressWarnings("serial")
public class RecordatoriosWindow extends Window {

    static final String TIPO_PROPERTY = "Tipo";
    static final String REFERENCIA_PROPERTY = "Referencia";
    static final String LUGARCLIENTE_PROPERTY = "Lugar / Cliente";
    static final String TITULO_PROPERTY = "Título";
    static final String DETALLE_PROPERTY = "Detalle";
    static final String FECHA_PROPERTY = "Fecha";
    static final String IDSEG_PROPERTY = "IdSeg";
    static final String IDEVENTO_PROPERTY = "IdEvento";

    private final IndexedContainer container = new IndexedContainer();
    private final Grid grid = new Grid();
    private final RecordatorioSeguimientoService seguimientoService = new RecordatorioSeguimientoService();
    private final RecordatorioEventoService eventoService = new RecordatorioEventoService();

    public RecordatoriosWindow(List<Recordatorio> recordatorios) {
        setCaption("SOPDI - Recordatorios");
        setModal(true);
        setResizable(true);
        setClosable(true);
        setWidth("980px");
        setHeight("460px");

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        container.addContainerProperty(TIPO_PROPERTY, String.class, "");
        container.addContainerProperty(REFERENCIA_PROPERTY, String.class, "");
        container.addContainerProperty(LUGARCLIENTE_PROPERTY, String.class, "");
        container.addContainerProperty(TITULO_PROPERTY, String.class, "");
        container.addContainerProperty(DETALLE_PROPERTY, String.class, "");
        container.addContainerProperty(FECHA_PROPERTY, String.class, "");
        container.addContainerProperty(IDSEG_PROPERTY, String.class, "");
        container.addContainerProperty(IDEVENTO_PROPERTY, String.class, "");

        grid.setContainerDataSource(container);
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addStyleName(ValoTheme.TABLE_COMPACT);
        grid.setDescription("Doble clic para ver el detalle.");

        grid.getColumn(IDSEG_PROPERTY).setHidden(true);
        grid.getColumn(IDEVENTO_PROPERTY).setHidden(true);
        grid.getColumn(TIPO_PROPERTY).setMaximumWidth(150);
        grid.getColumn(REFERENCIA_PROPERTY).setMaximumWidth(140);
        grid.getColumn(LUGARCLIENTE_PROPERTY).setMaximumWidth(200);
        grid.getColumn(FECHA_PROPERTY).setMaximumWidth(140);

        grid.addItemClickListener((ItemClickEvent.ItemClickListener) event -> {
            if (event.isDoubleClick() && event.getItemId() != null) {
                String detalle = String.valueOf(
                        container.getContainerProperty(event.getItemId(), DETALLE_PROPERTY).getValue());
                DescripcionWindow descripcionWindow = new DescripcionWindow(detalle);
                UI.getCurrent().addWindow(descripcionWindow);
                descripcionWindow.center();
            }
        });

        llenarGrid(recordatorios);

        layout.addComponent(grid);
        layout.setExpandRatio(grid, 1);

        Button atendidoBtn = new Button("Marcar atendido / realizado", FontAwesome.CHECK);
        atendidoBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        atendidoBtn.addClickListener(e -> marcarSeleccionado());

        Button verPdfBtn = new Button("Ver PDF cheques", FontAwesome.FILE_PDF_O);
        verPdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        verPdfBtn.setEnabled(false);
        verPdfBtn.setDescription("Abre el PDF de los cheques de este aviso");
        verPdfBtn.addClickListener(e -> abrirPdfAviso(verPdfBtn));

        // Habilitar botón Ver PDF solo cuando se selecciona un aviso de pago
        grid.addSelectionListener(event -> {
            Object selId = grid.getSelectedRow();
            boolean esPago = selId != null && Recordatorio.TIPO_AVISO_PAGO.equals(
                    String.valueOf(container.getContainerProperty(selId, TIPO_PROPERTY).getValue()));
            verPdfBtn.setEnabled(esPago);
        });

        Button cerrarBtn = new Button("Cerrar", FontAwesome.SIGN_OUT);
        cerrarBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        cerrarBtn.addClickListener(e -> close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.addComponents(atendidoBtn, verPdfBtn, cerrarBtn);

        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);

        setContent(layout);
    }

    private void llenarGrid(List<Recordatorio> recordatorios) {
        container.removeAllItems();
        if (recordatorios == null) {
            return;
        }
        for (Recordatorio r : recordatorios) {
            Object itemId = container.addItem();
            container.getContainerProperty(itemId, TIPO_PROPERTY).setValue(r.tipo);
            container.getContainerProperty(itemId, REFERENCIA_PROPERTY).setValue(r.referencia == null ? "" : r.referencia);
            container.getContainerProperty(itemId, LUGARCLIENTE_PROPERTY).setValue(r.lugarCliente == null ? "" : r.lugarCliente);
            container.getContainerProperty(itemId, TITULO_PROPERTY).setValue(r.titulo == null ? "" : r.titulo);
            container.getContainerProperty(itemId, DETALLE_PROPERTY).setValue(r.detalle == null ? "" : r.detalle);
            container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(r.fecha == null ? "" : r.fecha);
            // idSeguimiento se usa también para idAviso (avisos de pago)
            String idSeg = r.idSeguimiento != null ? r.idSeguimiento
                         : r.idAviso != null ? r.idAviso : "";
            container.getContainerProperty(itemId, IDSEG_PROPERTY).setValue(idSeg);
            container.getContainerProperty(itemId, IDEVENTO_PROPERTY).setValue(r.idEvento == null ? "" : r.idEvento);
        }
        setCaption("SOPDI - Recordatorios (" + container.size() + ")");
    }

    private void abrirPdfAviso(Button source) {
        Object itemId = grid.getSelectedRow();
        if (itemId == null) return;
        String idAviso = String.valueOf(container.getContainerProperty(itemId, IDSEG_PROPERTY).getValue());
        java.sql.Connection cnx = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection();
        byte[] bytes = AvisoPagoChequeService.getPdfBytes(cnx, idAviso);
        if (bytes == null || bytes.length == 0) {
            Notification.show("No hay PDF adjunto en este aviso.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        // Mostrar el PDF en una ventana modal con BrowserFrame
        com.vaadin.server.StreamResource pdfResource = new com.vaadin.server.StreamResource(
                () -> new java.io.ByteArrayInputStream(bytes),
                "cheques_" + idAviso + ".pdf");
        pdfResource.setMIMEType("application/pdf");

        com.vaadin.ui.BrowserFrame frame = new com.vaadin.ui.BrowserFrame("", pdfResource);
        frame.setSizeFull();

        com.vaadin.ui.Window pdfWindow = new com.vaadin.ui.Window("PDF — Cheques por imprimir");
        pdfWindow.setModal(false);
        pdfWindow.setResizable(true);
        pdfWindow.setWidth("90%");
        pdfWindow.setHeight("85%");
        pdfWindow.setContent(frame);
        UI.getCurrent().addWindow(pdfWindow);
        pdfWindow.center();
    }

    private void marcarSeleccionado() {
        Object itemId = grid.getSelectedRow();
        if (itemId == null) {
            Notification.show("Seleccione un recordatorio.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        String tipo = String.valueOf(container.getContainerProperty(itemId, TIPO_PROPERTY).getValue());
        String idSeg = String.valueOf(container.getContainerProperty(itemId, IDSEG_PROPERTY).getValue());
        String idEvento = String.valueOf(container.getContainerProperty(itemId, IDEVENTO_PROPERTY).getValue());
        java.sql.Connection cnx = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection();

        boolean ok = false;
        if (Recordatorio.TIPO_SEGUIMIENTO.equals(tipo) && idSeg != null && !idSeg.trim().isEmpty()) {
            ok = seguimientoService.marcarAtendido(cnx, idSeg);
        } else if ((Recordatorio.TIPO_EVENTO_HOY.equals(tipo) || Recordatorio.TIPO_EVENTO_VENCIDO.equals(tipo))
                && idEvento != null && !idEvento.trim().isEmpty()) {
            ok = eventoService.marcarRealizado(cnx, idEvento);
        } else if (Recordatorio.TIPO_AVISO_PAGO.equals(tipo)) {
            String idAviso = String.valueOf(
                    container.getContainerProperty(itemId, IDSEG_PROPERTY).getValue());
            ok = AvisoPagoChequeService.marcarAtendido(cnx, idAviso);
        } else {
            Notification.show("Las tareas vencidas se cierran cambiando su estatus en la visita.",
                    Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (ok) {
            container.removeItem(itemId);
            setCaption("SOPDI - Recordatorios (" + container.size() + ")");
            Notification.show("Recordatorio actualizado.", Notification.Type.TRAY_NOTIFICATION);
        } else {
            Notification.show("No se pudo actualizar el recordatorio.", Notification.Type.ERROR_MESSAGE);
        }
    }
}
