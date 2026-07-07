package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

/**
 * Ventana para gestionar el historial de responsables de un activo.
 * Permite:
 * - Seleccionar un nuevo responsable (proveedor)
 * - Definir fecha de inicio (la fecha fin del anterior se actualiza automáticamente)
 * - Generar PDF con firma del responsable actual
 * - Subir PDF con firma del responsable
 */
public class ActivosResponsableForm extends Window {
    
    // Constantes para Grid
    private static final String ID = "ID";
    private static final String RESPONSABLE = "Responsable";
    private static final String FECHA_INICIO = "Fecha Inicio";
    private static final String FECHA_FIN = "Fecha Fin";
    private static final String FIRMA_PATH = "FirmaFilePath";
    private static final String FIRMA = "Firma";
    private static final String ELIMINAR = "Eliminar";

    // Carpeta fija del servidor.
    // En producción puedes pasarla con:
    // -Dsopdi.firmasActivos.dir=/opt/sopdi/firmas_activos
    private static final String FIRMA_BASE_DIR = System.getProperty(
            "sopdi.firmasActivos.dir",
            System.getProperty("user.home") + File.separator + "sopdi_firmas_activos"
    );
    
    private long idActivo;
    private String codigoActivo;
    private String descripcion;
    private int idEmpresa;
    
    private ComboBox cbResponsable;
    private PopupDateField dfFechaInicio;
    private Button btnGuardar;
    private Button btnCancelar;

    private Table tableResponsables;
    private IndexedContainer responsablesContainer;
    
    private UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public ActivosResponsableForm(long idActivo, String codigoActivo, String descripcion, int idEmpresa) {
        super("Gestión de Responsables - Activo: " + codigoActivo);
        this.idActivo = idActivo;
        this.codigoActivo = codigoActivo;
        this.descripcion = descripcion;
        this.idEmpresa = idEmpresa;
        this.mainUI = UI.getCurrent();
        
        center();
        setModal(true);
        setWidth("800px");
        setHeight("600px");
        
        initLayout();
        cargarResponsablesActuales();
    }

    private void initLayout() {
        MarginInfo marginInfo = new MarginInfo(true, true, true, true);
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(marginInfo);
        mainLayout.setSpacing(true);

        // Sección de selección de responsable
        VerticalLayout seleccionLayout = crearSeccionSeleccion();
        
        // Grid de responsables actuales
        VerticalLayout gridLayout = crearGridResponsables();
        
        // Botones de acción
        HorizontalLayout botonesLayout = crearBotonesAccion();
        
        mainLayout.addComponents(seleccionLayout, gridLayout, botonesLayout);
        mainLayout.setComponentAlignment(botonesLayout, Alignment.BOTTOM_RIGHT);
        mainLayout.setExpandRatio(seleccionLayout, 1);
        mainLayout.setExpandRatio(gridLayout, 2);
        
        setContent(mainLayout);
    }

    private VerticalLayout crearSeccionSeleccion() {
        VerticalLayout layout = new VerticalLayout();
        layout.setCaption("Asignar Nuevo Responsable");
        layout.addStyleName("rcorners3");
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.setWidth("100%");

        cbResponsable = new ComboBox("Seleccionar Responsable:");
        cbResponsable.setWidth("100%");
        cbResponsable.setRequired(true);
        cbResponsable.setInvalidAllowed(false);
        cbResponsable.setNewItemsAllowed(false);
        cbResponsable.setTextInputAllowed(true);
        cbResponsable.setFilteringMode(FilteringMode.CONTAINS);
        llenarComboResponsables();

        dfFechaInicio = new PopupDateField("Fecha Inicio del Responsable:");
        dfFechaInicio.setWidth("100%");
        dfFechaInicio.setDateFormat("dd/MM/yyyy");
        dfFechaInicio.setRequired(true);
        dfFechaInicio.setValue(new Date());

        layout.addComponents(cbResponsable, dfFechaInicio);

        return layout;
    }

    private VerticalLayout crearGridResponsables() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setSpacing(true);

        responsablesContainer = new IndexedContainer();
        responsablesContainer.addContainerProperty(ID, Long.class, null);
        responsablesContainer.addContainerProperty(RESPONSABLE, String.class, null);
        responsablesContainer.addContainerProperty(FECHA_INICIO, Date.class, null);
        responsablesContainer.addContainerProperty(FECHA_FIN, Date.class, null);
        responsablesContainer.addContainerProperty(FIRMA_PATH, String.class, null);

        tableResponsables = new Table("Historial de Responsables:");
        tableResponsables.setContainerDataSource(responsablesContainer);
        tableResponsables.setWidth("100%");
        tableResponsables.setPageLength(6);
        tableResponsables.setSelectable(false);
        tableResponsables.setImmediate(true);

        tableResponsables.addGeneratedColumn(FIRMA, new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Button btnFirma = new Button("Firma");
                btnFirma.setIcon(FontAwesome.FILE_PDF_O);

                btnFirma.addClickListener(event -> {
                    Long idResponsable = (Long) responsablesContainer
                            .getContainerProperty(itemId, ID)
                            .getValue();

                    String responsable = (String) responsablesContainer
                            .getContainerProperty(itemId, RESPONSABLE)
                            .getValue();

                    Date fechaInicio = (Date) responsablesContainer
                            .getContainerProperty(itemId, FECHA_INICIO)
                            .getValue();

                    String firmaPath = (String) responsablesContainer
                            .getContainerProperty(itemId, FIRMA_PATH)
                            .getValue();

                    abrirVentanaFirma(idResponsable, responsable, fechaInicio, firmaPath);
                });

                return btnFirma;
            }
        });

        tableResponsables.addGeneratedColumn(ELIMINAR, new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Button btnEliminar = new Button("Eliminar");
                btnEliminar.setIcon(FontAwesome.TRASH_O);
                btnEliminar.setStyleName(ValoTheme.BUTTON_DANGER);

                String firmaPath = (String) responsablesContainer
                        .getContainerProperty(itemId, FIRMA_PATH)
                        .getValue();

                boolean puedeEliminar = isBlank(firmaPath);
                btnEliminar.setEnabled(puedeEliminar);

                btnEliminar.addClickListener(event -> {
                    Long idResponsable = (Long) responsablesContainer
                            .getContainerProperty(itemId, ID)
                            .getValue();

                    confirmarEliminarResponsable(idResponsable);
                });

                return btnEliminar;
            }
        });

        tableResponsables.setVisibleColumns(
                RESPONSABLE,
                FECHA_INICIO,
                FECHA_FIN,
                FIRMA,
                ELIMINAR
        );

        tableResponsables.setColumnHeader(RESPONSABLE, "Responsable");
        tableResponsables.setColumnHeader(FECHA_INICIO, "Fecha Inicio");
        tableResponsables.setColumnHeader(FECHA_FIN, "Fecha Fin");
        tableResponsables.setColumnHeader(FIRMA, "Firma");
        tableResponsables.setColumnHeader(ELIMINAR, "Eliminar");

        layout.addComponent(tableResponsables);

        return layout;
    }


    private HorizontalLayout crearBotonesAccion() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setMargin(false);

        btnGuardar = new Button("Asignar");
        btnGuardar.setIcon(FontAwesome.SAVE);
        btnGuardar.setStyleName(ValoTheme.BUTTON_PRIMARY);
        btnGuardar.addClickListener(event -> guardarResponsable());

        btnCancelar = new Button("Cerrar");
        btnCancelar.setIcon(FontAwesome.TIMES);
        btnCancelar.addClickListener(event -> close());

        layout.addComponents(btnGuardar, btnCancelar);
        
        return layout;
    }

    private void llenarComboResponsables() {
        // Traer proveedores de la base de datos
        String query = "SELECT IdProveedor, Nombre FROM proveedor_empresa " +
                       "WHERE IdEmpresa = " + idEmpresa + " " +
                       "AND inhabilitado = 0 " +
                       "AND EsPlanilla = 1 " +
                       "ORDER BY Nombre";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(query);

            while (rsRecords.next()) {
                cbResponsable.addItem(rsRecords.getInt("IdProveedor"));
                cbResponsable.setItemCaption(rsRecords.getInt("IdProveedor"), 
                        rsRecords.getString("Nombre"));
            }

        } catch (Exception ex) {
            System.out.println("Error al llenar combo de responsables: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void cargarResponsablesActuales() {
        responsablesContainer.removeAllItems();

        String query = "SELECT ar.id, cp.Nombre, ar.FechaInicio, ar.FechaFin, ar.FirmaFilePath " +
                "FROM activos_responsable ar " +
                "INNER JOIN proveedor_empresa cp ON ar.IdProveedor = cp.IdProveedor " +
                "WHERE ar.idActivo = " + idActivo + " " +
                "AND cp.IdEmpresa = " + idEmpresa + " " +
                "ORDER BY ar.FechaInicio DESC, ar.id DESC";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(query);

            while (rsRecords.next()) {
                Object itemId = responsablesContainer.addItem();

                responsablesContainer.getContainerProperty(itemId, ID)
                        .setValue(rsRecords.getLong("id"));

                responsablesContainer.getContainerProperty(itemId, RESPONSABLE)
                        .setValue(rsRecords.getString("Nombre"));

                responsablesContainer.getContainerProperty(itemId, FECHA_INICIO)
                        .setValue(rsRecords.getDate("FechaInicio"));

                responsablesContainer.getContainerProperty(itemId, FECHA_FIN)
                        .setValue(rsRecords.getDate("FechaFin"));

                responsablesContainer.getContainerProperty(itemId, FIRMA_PATH)
                        .setValue(rsRecords.getString("FirmaFilePath"));
            }

        } catch (Exception ex) {
            System.out.println("Error al cargar responsables actuales: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void guardarResponsable() {
        try {
            if (cbResponsable.getValue() == null || dfFechaInicio.getValue() == null) {
                Notification.show("Debe seleccionar responsable y fecha", Notification.Type.WARNING_MESSAGE);
                return;
            }

            int idProveedor = (int) cbResponsable.getValue();
            Date fechaInicio = dfFechaInicio.getValue();

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String updateAnterior = "UPDATE activos_responsable SET FechaFin = '" +
                    Utileria.getFechaYYYYMMDD_1(fechaInicio) + "' " +
                    "WHERE idActivo = " + idActivo + " AND FechaFin IS NULL";

            stQuery.executeUpdate(updateAnterior);

            String insertResponsable =
                    "INSERT INTO activos_responsable (idActivo, IdProveedor, FechaInicio) VALUES (" +
                            idActivo + ", " +
                            idProveedor + ", '" +
                            Utileria.getFechaYYYYMMDD_1(fechaInicio) + "')";

            stQuery.executeUpdate(insertResponsable);

            String updateActivos = "UPDATE activos SET IdProveedorResponsable = " +
                    idProveedor + " " +
                    "WHERE id = " + idActivo;

            stQuery.executeUpdate(updateActivos);

            Notification notif = new Notification(
                    "Responsable asignado exitosamente",
                    Notification.Type.HUMANIZED_MESSAGE
            );
            notif.setDelayMsec(2000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.show(Page.getCurrent());

            cargarResponsablesActuales();

            cbResponsable.setValue(null);
            dfFechaInicio.setValue(new Date());

        } catch (Exception ex) {
            System.out.println("Error al guardar responsable: " + ex.getMessage());
            ex.printStackTrace();
            Notification.show("Error al guardar: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void abrirVentanaFirma(
            Long idResponsable,
            String responsable,
            Date fechaInicio,
            String firmaPath
    ) {
        ActivosResponsableFirmaWindow window = new ActivosResponsableFirmaWindow(
                mainUI,
                idActivo,
                idResponsable,
                codigoActivo,
                descripcion,
                responsable,
                fechaInicio,
                firmaPath,
                () -> cargarResponsablesActuales()
        );

        UI.getCurrent().addWindow(window);
    }

    private void confirmarEliminarResponsable(Long idResponsable) {
        Window confirm = new Window("Confirmar eliminación");
        confirm.setModal(true);
        confirm.center();
        confirm.setWidth("350px");

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);

        Label mensaje = new Label("¿Está seguro de eliminar este responsable?");

        Button btnSi = new Button("Eliminar");
        btnSi.setStyleName(ValoTheme.BUTTON_DANGER);
        btnSi.addClickListener(event -> {
            eliminarResponsableSinPDF(idResponsable);
            confirm.close();
        });

        Button btnNo = new Button("Cancelar");
        btnNo.addClickListener(event -> confirm.close());

        HorizontalLayout botones = new HorizontalLayout(btnSi, btnNo);
        botones.setSpacing(true);

        layout.addComponents(mensaje, botones);
        confirm.setContent(layout);

        UI.getCurrent().addWindow(confirm);
    }

    private void eliminarResponsableSinPDF(Long idResponsable) {
        try {
            String firmaPath = obtenerFirmaPath(idResponsable);

            if (!isBlank(firmaPath)) {
                Notification.show(
                        "No se puede eliminar porque ya tiene PDF/firma asignado.",
                        Notification.Type.WARNING_MESSAGE
                );
                return;
            }

            String sql = "DELETE FROM activos_responsable " +
                    "WHERE id = ? AND (FirmaFilePath IS NULL OR FirmaFilePath = '')";

            PreparedStatement ps = ((SopdiUI) mainUI)
                    .databaseProvider
                    .getCurrentConnection()
                    .prepareStatement(sql);

            ps.setLong(1, idResponsable);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                Notification.show("Responsable eliminado.", Notification.Type.HUMANIZED_MESSAGE);
                cargarResponsablesActuales();
            } else {
                Notification.show("No se eliminó el responsable.", Notification.Type.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al eliminar: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean esPDF(String filename, String mimeType) {
        boolean mimeOk = mimeType != null && mimeType.equalsIgnoreCase("application/pdf");
        boolean extensionOk = filename != null && filename.toLowerCase().endsWith(".pdf");

        return mimeOk || extensionOk;
    }

    private String obtenerFirmaPath(Long idResponsable) throws Exception {
        String sql = "SELECT FirmaFilePath FROM activos_responsable WHERE id = ?";

        PreparedStatement ps = ((SopdiUI) mainUI)
                .databaseProvider
                .getCurrentConnection()
                .prepareStatement(sql);

        ps.setLong(1, idResponsable);

        ResultSet rs = ps.executeQuery();

        String path = null;
        if (rs.next()) {
            path = rs.getString("FirmaFilePath");
        }

        rs.close();
        ps.close();

        return path;
    }
}



