package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.extras.custom.ToggleSwitch ;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Ventana para administrar equivalencias de depreciación (Vaadin 7).
 * Carga nomenclaturas desde contabilidad_nomenclatura con filtro ID1 IN (1,6) e ID2 IN (1,2)
 */
public class DepreciacionesEquivalenciasForm extends Window {

    // Atributos globales
    Button btnAgregar;
    Grid gridEquivalencias;
    IndexedContainer equivalenciasContainer;
    ComboBox cbActivo;
    ComboBox cbDebe;
    ComboBox cbHaber;
    ToggleSwitch tsdeshabilitado;
    NumberField nfMeses;
    UI mainUI;
    String idEmpresa;
    // Control de modo edición
    private boolean modoEdicion = false;
    private Integer idEquivalenciaEnEdicion = null;

    // Variables para queries
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    private static class Nomenclatura {
        final int idNomenclatura;
        final String n5;
        final String noCuenta;

        Nomenclatura(int idNomenclatura, String n5, String noCuenta) {
            this.idNomenclatura = idNomenclatura;
            this.n5 = n5;
            this.noCuenta = noCuenta;
        }

        @Override
        public String toString() {
            return n5 + " — " + noCuenta;
        }
    }

    public DepreciacionesEquivalenciasForm() {
        super("Administrar Equivalencias");
        center();
        setModal(true);
        setWidth("800px");
        setHeight("600px");

        mainUI = UI.getCurrent();

        idEmpresa= ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(true, true, true, true));
        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();

        // Título
        Label titleLabel = new Label("Gestión de Equivalencias de Depreciación");
        titleLabel.addStyleName(ValoTheme.LABEL_H2);
        mainLayout.addComponent(titleLabel);

        // Sección: Formulario de entrada
        VerticalLayout formSection = new VerticalLayout();
        formSection.addStyleName("rcorners3");
        formSection.setMargin(true);
        formSection.setSpacing(true);
        formSection.setCaption("Crear Nueva Equivalencia");

        FormLayout form = new FormLayout();
        form.setMargin(false);
        form.setSpacing(true);

        // Crear ComboBox para Nomenclatura Activo
        cbActivo = new ComboBox("Nomenclatura Activo:");
        cbActivo.setWidth("100%");
        cbActivo.setInvalidAllowed(false);
        cbActivo.setNullSelectionAllowed(true);
        cbActivo.setFilteringMode(FilteringMode.CONTAINS);

        // Crear ComboBox para Nomenclatura Debe
        cbDebe = new ComboBox("Nomenclatura Debe:");
        cbDebe.setWidth("100%");
        cbDebe.setInvalidAllowed(false);
        cbDebe.setNullSelectionAllowed(true);
        cbDebe.setFilteringMode(FilteringMode.CONTAINS);

        // Crear ComboBox para Nomenclatura Haber
        cbHaber = new ComboBox("Nomenclatura Haber:");
        cbHaber.setWidth("100%");
        cbHaber.setInvalidAllowed(false);
        cbHaber.setNullSelectionAllowed(true);
        cbHaber.setFilteringMode(FilteringMode.CONTAINS);

        // Crear NumberField para Meses
        nfMeses = new NumberField();
        nfMeses.setWidth("100%");
        nfMeses.setValue(0.0);
        nfMeses.setRequired(true);

        // Crear ToggleSwitch para deshabilitado
        tsdeshabilitado = new ToggleSwitch("Habilitado", "deshabilitado");
        tsdeshabilitado.setWidth("100%");
        tsdeshabilitado.setValue(false);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setCaption("Meses:");
        horizontalLayout.setWidth("100%");
        horizontalLayout.setSpacing(true);
        horizontalLayout.addComponents(nfMeses, tsdeshabilitado);


        // Cargar datos de nomenclaturas desde BD
        cargarNomenclaturas(cbActivo, 1, 2);
        cargarNomenclaturas(cbDebe, 6, 1);
        cargarNomenclaturas(cbHaber, 1, 2);

        form.addComponents(cbActivo, cbDebe, cbHaber, horizontalLayout);
        formSection.addComponent(form);

        // ...existing code...

        // Botones de acción
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setMargin(false);

        btnAgregar = new Button("Agregar Equivalencia");
        btnAgregar.setStyleName(ValoTheme.BUTTON_PRIMARY);
        btnAgregar.addClickListener(event -> {
            if (modoEdicion) {
                actualizarEquivalencia();
            } else {
                agregarEquivalencia();
            }
        });

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.addClickListener(event -> {
            limpiarFormulario();
            cancelarEdicion();
        });

        buttonLayout.addComponents(btnAgregar, btnLimpiar);
        formSection.addComponent(buttonLayout);

        mainLayout.addComponent(formSection);

        // Sección: Grid de equivalencias existentes
        VerticalLayout gridSection = new VerticalLayout();
        gridSection.setMargin(false);
        gridSection.setSpacing(true);
        gridSection.setCaption("Equivalencias Registradas");
        gridSection.setSizeFull();

        gridEquivalencias = new Grid();
        gridEquivalencias.setWidth("100%");
        gridEquivalencias.setHeightMode(HeightMode.ROW);
        gridEquivalencias.setHeightByRows(3);

        // Crear container
        equivalenciasContainer = new IndexedContainer();
        equivalenciasContainer.addContainerProperty("Id", Integer.class, null);
        equivalenciasContainer.addContainerProperty("NomenclaturaActivo", String.class, null);
        equivalenciasContainer.addContainerProperty("NomenclaturaDebe", String.class, null);
        equivalenciasContainer.addContainerProperty("NomenclaturaHaber", String.class, null);
        equivalenciasContainer.addContainerProperty("Meses", Integer.class, null);
        equivalenciasContainer.addContainerProperty("deshabilitado", Boolean.class, null);

        gridEquivalencias.setContainerDataSource(equivalenciasContainer);

        // Listener para selección de fila
        gridEquivalencias.addSelectionListener(event -> {
            if (event.getSelected() != null && !event.getSelected().isEmpty()) {
                // Cuando se selecciona una fila
                Integer idSeleccionado = (Integer) event.getSelected().iterator().next();
                Integer id = (Integer)equivalenciasContainer.getContainerProperty(idSeleccionado, "Id").getValue();
                cargarEquivalenciaEnFormulario(id);
            } else {
                // Cuando se deselecciona (grid vacío)
                limpiarFormulario();
                cancelarEdicion();
            }
        });

        gridSection.addComponent(gridEquivalencias);
        gridSection.setExpandRatio(gridEquivalencias, 1f);

        mainLayout.addComponent(gridSection);
        mainLayout.setExpandRatio(gridSection, 1f);

        // Botón de cierre
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setSpacing(true);
        footerLayout.setMargin(false);

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.addClickListener(event -> close());

        footerLayout.addComponent(btnCerrar);
        footerLayout.setComponentAlignment(btnCerrar, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(footerLayout);

        setContent(mainLayout);

        // Cargar equivalencias existentes
        cargarEquivalenciasDelGrid();
    }

    // 06/07/2026: Id1 y Id2 son caracteristicas de las nomenclaturas dentro de la base de datos
    private void cargarNomenclaturas(ComboBox comboBox, int id1, int id2) {
        String query = "SELECT IdNomenclatura, N5, NoCuenta FROM contabilidad_nomenclatura " +
                "WHERE ID1 = " + id1 + " " +
                "AND ID2 = " + id2 + " " +
                "ORDER BY N5 ASC";

        try {
            Statement stmt = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int idNomenclatura = rs.getInt("IdNomenclatura");
                String n5 = rs.getString("N5");
                String noCuenta = rs.getString("NoCuenta");

                Nomenclatura nomenclatura = new Nomenclatura(idNomenclatura, n5, noCuenta);
                comboBox.addItem(nomenclatura);
                comboBox.setItemCaption(nomenclatura, n5 + " — " + noCuenta);
                System.out.println(idNomenclatura + " " + n5);
            }

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            Notification.show("Error al cargar nomenclaturas: " + ex.getMessage(),
                            Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Agrega una equivalencia: inserta en BD y actualiza el grid
     */
    private void agregarEquivalencia() {
        // Validar que todos los campos estén seleccionados
        if (cbActivo.getValue() == null) {
            Notification.show("Debe seleccionar Nomenclatura Activo", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (cbDebe.getValue() == null) {
            Notification.show("Debe seleccionar Nomenclatura Debe", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (cbHaber.getValue() == null) {
            Notification.show("Debe seleccionar Nomenclatura Haber", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (nfMeses.getValue() == null || nfMeses.getDoubleValueDoNotThrow() <= 0) {
            Notification.show("Debe ingresar Meses (mayor a 0)", Notification.Type.WARNING_MESSAGE);
            return;
        }

        // Obtener valores
        int idNomenclaturaActivo = ((Nomenclatura) cbActivo.getValue()).idNomenclatura;
        int idNomenclaturaDebe = ((Nomenclatura) cbDebe.getValue()).idNomenclatura;
        int idNomenclaturaHaber = ((Nomenclatura) cbHaber.getValue()).idNomenclatura;
        int meses = (int) nfMeses.getDoubleValueDoNotThrow();
        boolean deshabilitado = tsdeshabilitado.getValue();

        // Obtener el próximo ID para tipo_depreciacion
        int nuevoId = obtenerProximoIdTipoDepreciacion();
        if (nuevoId <= 0) {
            Notification.show("Error al obtener el siguiente ID", Notification.Type.ERROR_MESSAGE);
            return;
        }

        // Insertar en BD
        String queryInsert = "INSERT INTO tipo_depreciacion (" +
                "Id, " +
                "IdEmpresa, " +
                "IdNomenclaturaActivo, " +
                "IdNomenclaturaDebe, " +
                "IdNomenclaturaHaber, " +
                "Meses, " +
                "deshabilitado) " +
                "VALUES (" +
                    nuevoId + ", " +
                    idEmpresa + ", " +
                    idNomenclaturaActivo + ", " +
                    idNomenclaturaDebe + ", " +
                    idNomenclaturaHaber + ", " +
                    meses + ", " +
                    (deshabilitado ? 1 : 0) + ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryInsert);

            // Agregar al grid
            agregarFilaAlGrid(nuevoId, idNomenclaturaActivo, idNomenclaturaDebe, idNomenclaturaHaber, meses, deshabilitado);

            // Limpiar campos
            limpiarFormulario();

            // Notificación de éxito
            Notification notif = new Notification("Equivalencia agregada exitosamente", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

        } catch (Exception ex) {
            Notification.show("Error al guardar equivalencia: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Obtiene el próximo ID disponible en tipo_depreciacion
     */
    private int obtenerProximoIdTipoDepreciacion() {
        String query = "SELECT MAX(Id) as MaxId FROM tipo_depreciacion";
        int nuevoId = 1;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(query);

            if (rsRecords.next()) {
                int maxId = rsRecords.getInt("MaxId");
                nuevoId = maxId + 1;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return nuevoId;
    }

    /**
     * Agrega una fila al grid de equivalencias
     */
    private void agregarFilaAlGrid(int id, int idActivo, int idDebe, int idHaber, int meses, boolean deshabilitado) {
        // Obtener nombres de nomenclaturas
        String nombreActivo = obtenerNombreNomenclatura(idActivo);
        String nombreDebe = obtenerNombreNomenclatura(idDebe);
        String nombreHaber = obtenerNombreNomenclatura(idHaber);

        // Agregar fila al grid
        Object itemId = gridEquivalencias.addRow(id, nombreActivo, nombreDebe, nombreHaber, meses, deshabilitado);
    }

    /**
     * Obtiene el nombre (N5) de una nomenclatura por su ID
     */
    private String obtenerNombreNomenclatura(int idNomenclatura) {
        String query = "SELECT N5, NoCuenta FROM contabilidad_nomenclatura WHERE IdNomenclatura = " + idNomenclatura;
        String resultado = "";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(query);

            if (rsRecords.next()) {
                String n5 = rsRecords.getString("N5");
                String noCuenta = rsRecords.getString("NoCuenta");
                resultado = n5 + " — " + noCuenta;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return resultado;
    }

    /**
     * Limpia el formulario
     */
    private void limpiarFormulario() {
        cbActivo.setValue(null);
        cbDebe.setValue(null);
        cbHaber.setValue(null);
        nfMeses.setValue(0.0);
        tsdeshabilitado.setValue(false);
    }

    /**
     * Carga todas las equivalencias del grid desde la tabla tipo_depreciacion
     */
    private void cargarEquivalenciasDelGrid() {
        equivalenciasContainer.removeAllItems();

        String query = "SELECT td.Id, td.IdNomenclaturaActivo, td.IdNomenclaturaDebe, " +
                "td.IdNomenclaturaHaber, td.Meses, td.deshabilitado, " +
                "ca.n5 AS n5Activo, ca.NoCuenta AS NoCuentaActivo, " +
                "cd.n5 AS n5Debe, cd.NoCuenta AS NoCuentaDebe, " +
                "ch.n5 AS n5Haber, ch.NoCuenta AS NoCuentaHaber " +
                "FROM tipo_depreciacion td " +
                "LEFT JOIN contabilidad_nomenclatura ca ON td.IdNomenclaturaActivo = ca.IdNomenclatura " +
                "LEFT JOIN contabilidad_nomenclatura cd ON td.IdNomenclaturaDebe = cd.IdNomenclatura " +
                "LEFT JOIN contabilidad_nomenclatura ch ON td.IdNomenclaturaHaber = ch.IdNomenclatura " +
                "WHERE td.idEmpresa = " + idEmpresa + " " +
                "ORDER BY Id ASC";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(query);
            if (rsRecords.next()) {
                do {
                    int id = rsRecords.getInt("Id");
                    int idActivo = rsRecords.getInt("IdNomenclaturaActivo");
                    int idDebe = rsRecords.getInt("IdNomenclaturaDebe");
                    int idHaber = rsRecords.getInt("IdNomenclaturaHaber");
                    int meses = rsRecords.getInt("Meses");
                    boolean deshabilitado = rsRecords.getInt("deshabilitado") == 1;
                    String nombreActivo = rsRecords.getString("n5Activo") + " — " + rsRecords.getString("NoCuentaActivo");
                    String nombreDebe = rsRecords.getString("n5Debe") + " — " + rsRecords.getString("NoCuentaDebe");
                    String nombreHaber = rsRecords.getString("n5Haber") + " — " + rsRecords.getString("NoCuentaHaber");

                    gridEquivalencias.addRow(id, nombreActivo, nombreDebe, nombreHaber, meses, deshabilitado);
                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            Notification.show("Error al cargar equivalencias: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Carga una equivalencia en el formulario al ser seleccionada
     */
    private void cargarEquivalenciaEnFormulario(Integer idEquivalencia) {
        String query = "SELECT Id, IdNomenclaturaActivo, " +
                "IdNomenclaturaDebe, " +
                "IdNomenclaturaHaber, Meses, deshabilitado " +
                "FROM tipo_depreciacion WHERE Id = " + idEquivalencia;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(query);

            if (rsRecords.next()) {
                int idNomenclaturaActivo = rsRecords.getInt("IdNomenclaturaActivo");
                int idNomenclaturaDebe = rsRecords.getInt("IdNomenclaturaDebe");
                int idNomenclaturaHaber = rsRecords.getInt("IdNomenclaturaHaber");
                int meses = rsRecords.getInt("Meses");
                boolean deshabilitado = rsRecords.getInt("deshabilitado") == 1;

                // Buscar y seleccionar las nomenclaturas en los ComboBox
                seleccionarNomenclaturaEnCombo(cbActivo, idNomenclaturaActivo);
                seleccionarNomenclaturaEnCombo(cbDebe, idNomenclaturaDebe);
                seleccionarNomenclaturaEnCombo(cbHaber, idNomenclaturaHaber);

                // Llenar Meses y deshabilitado
                nfMeses.setValue((double) meses);
                tsdeshabilitado.setValue(deshabilitado);

                // Activar modo edición
                modoEdicion = true;
                idEquivalenciaEnEdicion = idEquivalencia;
                btnAgregar.setCaption("Actualizar Equivalencia");
                btnAgregar.setStyleName(ValoTheme.BUTTON_DANGER);
            }

        } catch (Exception ex) {
            Notification.show("Error al cargar equivalencia: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Busca y selecciona una nomenclatura en un ComboBox por su ID
     */
    private void seleccionarNomenclaturaEnCombo(ComboBox combo, int idNomenclatura) {
        for (Object item : combo.getItemIds()) {
            if (item instanceof Nomenclatura) {
                Nomenclatura nom = (Nomenclatura) item;
                if (nom.idNomenclatura == idNomenclatura) {
                    combo.select(item);
                    return;
                }
            }
        }
    }

    /**
     * Actualiza una equivalencia existente
     */
    private void actualizarEquivalencia() {
        // Validar campos
        if (cbActivo.getValue() == null) {
            Notification.show("Debe seleccionar Nomenclatura Activo", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (cbDebe.getValue() == null) {
            Notification.show("Debe seleccionar Nomenclatura Debe", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (cbHaber.getValue() == null) {
            Notification.show("Debe seleccionar Nomenclatura Haber", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (nfMeses.getValue() == null || nfMeses.getDoubleValueDoNotThrow() <= 0) {
            Notification.show("Debe ingresar Meses (mayor a 0)", Notification.Type.WARNING_MESSAGE);
            return;
        }

        // Obtener valores
        int idNomenclaturaActivo = ((Nomenclatura) cbActivo.getValue()).idNomenclatura;
        int idNomenclaturaDebe = ((Nomenclatura) cbDebe.getValue()).idNomenclatura;
        int idNomenclaturaHaber = ((Nomenclatura) cbHaber.getValue()).idNomenclatura;
        int meses = (int) nfMeses.getDoubleValueDoNotThrow();
        boolean deshabilitado = tsdeshabilitado.getValue();

        // Query UPDATE
        String queryUpdate = "UPDATE tipo_depreciacion SET " +
                "IdNomenclaturaActivo = " + idNomenclaturaActivo + ", " +
                "IdNomenclaturaDebe = " + idNomenclaturaDebe + ", " +
                "IdNomenclaturaHaber = " + idNomenclaturaHaber + ", " +
                "Meses = " + meses + ", " +
                "deshabilitado = " + (deshabilitado ? 1 : 0) + " " +
                "WHERE Id = " + idEquivalenciaEnEdicion;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryUpdate);

            // Recargar grid
            cargarEquivalenciasDelGrid();

            // Limpiar y cancelar edición
            limpiarFormulario();
            cancelarEdicion();

            // Notificación de éxito
            Notification notif = new Notification("Equivalencia actualizada exitosamente", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

        } catch (Exception ex) {
            Notification.show("Error al actualizar equivalencia: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Cancela el modo de edición
     */
    private void cancelarEdicion() {
        modoEdicion = false;
        idEquivalenciaEnEdicion = null;
        btnAgregar.setCaption("Agregar Equivalencia");
        btnAgregar.setStyleName(ValoTheme.BUTTON_PRIMARY);
        gridEquivalencias.deselectAll();
    }
}

