package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CRUD de Departamentos (tabla empleado_departamento).
 * Auto-crea la tabla y la columna Departamento en proveedor_empresa si no existen.
 */
public class DepartamentosPuestosView extends VerticalLayout implements View {

    ResultSet rsRecords = null;
    UI mainUI;

    static final String ID_PROPERTY          = "Id";
    static final String DEPARTAMENTO_PROPERTY = "Departamento";
    static final String DESCRIPCION_PROPERTY  = "Descripción";
    static final String EMPLEADOS_PROPERTY    = "Empleados";

    IndexedContainer deptoContainer = new IndexedContainer();
    Grid deptoGrid;

    TextField departamentoTxt = new TextField("Departamento");
    TextArea  descripcionTxt  = new TextArea("Descripción");

    Button nuevoBtn;
    Button guardarBtn;
    Button eliminarBtn;

    private String idEditando = null;

    String empresaId     = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public DepartamentosPuestosView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(new MarginInfo(true, true, true, true));

        asegurarEstructura();
        crearGrid();
        crearFormulario();
        llenar();
    }

    // ── Auto-estructura ──────────────────────────────────────────────────────

    /**
     * Crea la tabla empleado_departamento y la columna Departamento en
     * proveedor_empresa si aún no existen (idempotente).
     */
    private void asegurarEstructura() {
        try {
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            // Tabla empleado_departamento
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS empleado_departamento ("
                + " Id INT AUTO_INCREMENT PRIMARY KEY,"
                + " IdEmpresa INT NOT NULL,"
                + " Departamento VARCHAR(100) NOT NULL,"
                + " Descripcion VARCHAR(1024),"
                + " UNIQUE KEY ux_emp_dpto (IdEmpresa, Departamento)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Columna Departamento en proveedor_empresa
            String checkCol = "SELECT COUNT(*) FROM information_schema.COLUMNS"
                    + " WHERE TABLE_SCHEMA = DATABASE()"
                    + " AND TABLE_NAME = 'proveedor_empresa'"
                    + " AND COLUMN_NAME = 'Departamento'";
            ResultSet rs = st.executeQuery(checkCol);
            boolean existe = rs.next() && rs.getInt(1) > 0;
            rs.close();
            if (!existe) {
                st.executeUpdate(
                    "ALTER TABLE proveedor_empresa ADD COLUMN Departamento VARCHAR(100) DEFAULT ''");
            }
            st.close();
        } catch (Exception ex) {
            Logger.getLogger(DepartamentosPuestosView.class.getName())
                    .log(Level.WARNING, "asegurarEstructura: {0}", ex.getMessage());
        }
    }

    // ── Grid ─────────────────────────────────────────────────────────────────

    private void crearGrid() {
        deptoContainer.addContainerProperty(ID_PROPERTY,           String.class,  "");
        deptoContainer.addContainerProperty(DEPARTAMENTO_PROPERTY, String.class,  "");
        deptoContainer.addContainerProperty(DESCRIPCION_PROPERTY,  String.class,  "");
        deptoContainer.addContainerProperty(EMPLEADOS_PROPERTY,    Integer.class, 0);

        deptoGrid = new Grid("Departamentos registrados", deptoContainer);
        deptoGrid.setWidth("100%");
        deptoGrid.setHeightMode(HeightMode.ROW);
        deptoGrid.setHeightByRows(8);
        deptoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        deptoGrid.setImmediate(true);

        deptoGrid.getColumn(ID_PROPERTY).setHidden(true);
        deptoGrid.getColumn(DEPARTAMENTO_PROPERTY).setExpandRatio(2);
        deptoGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(4);
        deptoGrid.getColumn(EMPLEADOS_PROPERTY).setWidth(110);

        deptoGrid.setCellStyleGenerator(cell -> {
            if (EMPLEADOS_PROPERTY.equals(cell.getPropertyId())) return "centeralign";
            return null;
        });

        HeaderRow filterRow = deptoGrid.appendHeaderRow();
        HeaderCell filterCell = filterRow.getCell(DEPARTAMENTO_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar...");
        filterField.setColumns(12);
        filterField.addTextChangeListener(e -> {
            deptoContainer.removeContainerFilters(DEPARTAMENTO_PROPERTY);
            if (!e.getText().isEmpty()) {
                deptoContainer.addContainerFilter(
                        new SimpleStringFilter(DEPARTAMENTO_PROPERTY, e.getText(), true, false));
            }
        });
        filterCell.setComponent(filterField);

        deptoGrid.addSelectionListener(e -> {
            Object selected = deptoGrid.getSelectedRow();
            if (selected != null) cargarEnFormulario(selected);
        });

        deptoGrid.addItemClickListener(event -> {
            if (EMPLEADOS_PROPERTY.equals(event.getPropertyId())) {
                int count = (Integer) deptoContainer.getContainerProperty(event.getItemId(), EMPLEADOS_PROPERTY).getValue();
                String depto = nvl(deptoContainer.getContainerProperty(event.getItemId(), DEPARTAMENTO_PROPERTY).getValue());
                if (count > 0) {
                    abrirVentanaEmpleados(depto);
                } else {
                    Notification.show("El departamento \"" + depto + "\" no tiene empleados asignados.",
                            Notification.Type.HUMANIZED_MESSAGE);
                }
            }
        });

        Panel gridPanel = new Panel();
        gridPanel.setWidth("100%");
        gridPanel.addStyleName("rcorners3");
        gridPanel.setContent(deptoGrid);
        addComponent(gridPanel);
    }

    // ── Formulario ───────────────────────────────────────────────────────────

    private void crearFormulario() {
        departamentoTxt.setWidth("20em");
        departamentoTxt.setRequired(true);
        departamentoTxt.setMaxLength(100);

        descripcionTxt.setWidth("40em");
        descripcionTxt.setHeight("5em");
        descripcionTxt.setMaxLength(1024);

        nuevoBtn = new Button("Nuevo");
        nuevoBtn.setIcon(FontAwesome.PLUS);
        nuevoBtn.addClickListener(e -> prepararNuevo());

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(e -> guardar());

        eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        eliminarBtn.setEnabled(false);
        eliminarBtn.addClickListener(e -> eliminar());

        HorizontalLayout botonesLayout = new HorizontalLayout(nuevoBtn, guardarBtn, eliminarBtn);
        botonesLayout.setSpacing(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(new MarginInfo(true, true, true, true));
        formLayout.setSpacing(true);
        formLayout.addComponents(departamentoTxt, descripcionTxt);

        VerticalLayout panelContent = new VerticalLayout();
        panelContent.setSpacing(true);
        panelContent.setMargin(new MarginInfo(true, true, true, true));

        Label formTitleLbl = new Label("Datos del departamento");
        formTitleLbl.addStyleName(ValoTheme.LABEL_H3);
        panelContent.addComponent(formTitleLbl);
        panelContent.addComponent(formLayout);
        panelContent.addComponent(botonesLayout);

        Panel formPanel = new Panel();
        formPanel.setWidth("100%");
        formPanel.addStyleName("rcorners3");
        formPanel.setContent(panelContent);
        addComponent(formPanel);
    }

    // ── Lógica ───────────────────────────────────────────────────────────────

    private void prepararNuevo() {
        idEditando = null;
        deptoGrid.deselectAll();
        departamentoTxt.setReadOnly(false);
        departamentoTxt.setValue("");
        descripcionTxt.setValue("");
        eliminarBtn.setEnabled(false);
        departamentoTxt.focus();
    }

    private void cargarEnFormulario(Object itemId) {
        idEditando = nvl(deptoContainer.getContainerProperty(itemId, ID_PROPERTY).getValue());
        departamentoTxt.setReadOnly(false);
        departamentoTxt.setValue(nvl(deptoContainer.getContainerProperty(itemId, DEPARTAMENTO_PROPERTY).getValue()));
        descripcionTxt.setValue(nvl(deptoContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).getValue()));
        int empleados = (Integer) deptoContainer.getContainerProperty(itemId, EMPLEADOS_PROPERTY).getValue();
        eliminarBtn.setEnabled(empleados == 0);
    }

    private void guardar() {
        String depto = departamentoTxt.getValue().trim();
        if (depto.isEmpty()) {
            Notification.show("Ingrese el nombre del departamento.", Notification.Type.WARNING_MESSAGE);
            departamentoTxt.focus();
            return;
        }
        try {
            if (idEditando == null) {
                String sql = "INSERT INTO empleado_departamento (IdEmpresa, Departamento, Descripcion) VALUES (?, ?, ?)";
                PreparedStatement ps = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(sql);
                ps.setString(1, empresaId);
                ps.setString(2, depto);
                ps.setString(3, descripcionTxt.getValue().trim());
                ps.executeUpdate();
                ps.close();
                mostrarExito("Departamento registrado exitosamente.");
            } else {
                Object selectedRow = deptoGrid.getSelectedRow();
                int empleados = selectedRow != null
                        ? (Integer) deptoContainer.getContainerProperty(selectedRow, EMPLEADOS_PROPERTY).getValue()
                        : 0;
                String sql;
                PreparedStatement ps;
                if (empleados > 0) {
                    sql = "UPDATE empleado_departamento SET Descripcion = ? WHERE Id = ? AND IdEmpresa = ?";
                    ps = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(sql);
                    ps.setString(1, descripcionTxt.getValue().trim());
                    ps.setString(2, idEditando);
                    ps.setString(3, empresaId);
                } else {
                    sql = "UPDATE empleado_departamento SET Departamento = ?, Descripcion = ? WHERE Id = ? AND IdEmpresa = ?";
                    ps = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(sql);
                    ps.setString(1, depto);
                    ps.setString(2, descripcionTxt.getValue().trim());
                    ps.setString(3, idEditando);
                    ps.setString(4, empresaId);
                }
                ps.executeUpdate();
                ps.close();
                mostrarExito("Departamento actualizado exitosamente.");
            }
            llenar();
            prepararNuevo();
        } catch (Exception ex) {
            Logger.getLogger(DepartamentosPuestosView.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al guardar: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        Object itemId = deptoGrid.getSelectedRow();
        if (itemId == null) {
            Notification.show("Seleccione un departamento para eliminar.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        int empleados = (Integer) deptoContainer.getContainerProperty(itemId, EMPLEADOS_PROPERTY).getValue();
        if (empleados > 0) {
            Notification.show("No se puede eliminar: el departamento tiene " + empleados + " empleado(s) asignado(s).",
                    Notification.Type.WARNING_MESSAGE);
            return;
        }
        String id = nvl(deptoContainer.getContainerProperty(itemId, ID_PROPERTY).getValue());
        try {
            PreparedStatement ps = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection()
                    .prepareStatement("DELETE FROM empleado_departamento WHERE Id = ? AND IdEmpresa = ?");
            ps.setString(1, id);
            ps.setString(2, empresaId);
            ps.executeUpdate();
            ps.close();
            mostrarExito("Departamento eliminado.");
            llenar();
            prepararNuevo();
        } catch (Exception ex) {
            Logger.getLogger(DepartamentosPuestosView.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al eliminar: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    // ── Ventana de empleados por departamento ─────────────────────────────────

    private void abrirVentanaEmpleados(String departamento) {
        final String COD  = "Código";
        final String NOM  = "Nombre";
        final String ESTA = "Estado";

        IndexedContainer empContainer = new IndexedContainer();
        empContainer.addContainerProperty(COD,  String.class, "");
        empContainer.addContainerProperty(NOM,  String.class, "");
        empContainer.addContainerProperty(ESTA, String.class, "");

        String sql = "SELECT pe.IdProveedor, pe.Nombre, pe.Inhabilitado"
                + " FROM proveedor_empresa pe"
                + " WHERE pe.Departamento = ? AND pe.IdEmpresa = ? AND pe.EsPlanilla = 1"
                + " ORDER BY pe.Nombre";
        try {
            PreparedStatement ps = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(sql);
            ps.setString(1, departamento);
            ps.setString(2, empresaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object row = empContainer.addItem();
                empContainer.getContainerProperty(row, COD).setValue(rs.getString("IdProveedor"));
                empContainer.getContainerProperty(row, NOM).setValue(rs.getString("Nombre"));
                empContainer.getContainerProperty(row, ESTA).setValue(
                        "1".equals(rs.getString("Inhabilitado")) ? "Inhabilitado" : "Activo");
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            Logger.getLogger(DepartamentosPuestosView.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al obtener empleados: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return;
        }

        Grid empGrid = new Grid("Empleados del departamento: " + departamento, empContainer);
        empGrid.setWidth("100%");
        empGrid.setHeightMode(HeightMode.ROW);
        empGrid.setHeightByRows(Math.min(empContainer.size(), 12));
        empGrid.setSelectionMode(Grid.SelectionMode.NONE);
        empGrid.getColumn(COD).setWidth(90);
        empGrid.getColumn(NOM).setExpandRatio(1);
        empGrid.getColumn(ESTA).setWidth(110);

        Button cerrarBtn = new Button("Cerrar");
        cerrarBtn.setIcon(FontAwesome.TIMES);
        cerrarBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);

        Window win = new Window();
        win.setCaption("Departamento: " + departamento + "  (" + empContainer.size() + " empleado(s))");
        win.addStyleName("proveedor-window");
        win.setWidth("55%");
        win.setHeightUndefined();
        win.setModal(true);
        win.center();

        cerrarBtn.addClickListener(e -> win.close());

        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.setSpacing(true);
        content.addComponent(empGrid);
        content.addComponent(cerrarBtn);
        content.setComponentAlignment(cerrarBtn, Alignment.BOTTOM_RIGHT);
        win.setContent(content);

        UI.getCurrent().addWindow(win);
    }

    // ── Carga de datos ────────────────────────────────────────────────────────

    public void llenar() {
        deptoContainer.removeAllItems();
        String sql = "SELECT d.Id, d.Departamento, d.Descripcion, COUNT(pe.IdProveedor) AS Empleados"
                + " FROM empleado_departamento d"
                + " LEFT JOIN proveedor_empresa pe ON d.Departamento = pe.Departamento"
                + "   AND d.IdEmpresa = pe.IdEmpresa AND pe.EsPlanilla = 1"
                + " WHERE d.IdEmpresa = ?"
                + " GROUP BY d.Id, d.Departamento, d.Descripcion"
                + " ORDER BY d.Departamento";
        try {
            PreparedStatement ps = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(sql);
            ps.setString(1, empresaId);
            rsRecords = ps.executeQuery();
            while (rsRecords.next()) {
                Object itemId = deptoContainer.addItem();
                deptoContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                deptoContainer.getContainerProperty(itemId, DEPARTAMENTO_PROPERTY).setValue(rsRecords.getString("Departamento"));
                deptoContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(
                        rsRecords.getString("Descripcion") != null ? rsRecords.getString("Descripcion") : "");
                deptoContainer.getContainerProperty(itemId, EMPLEADOS_PROPERTY).setValue(rsRecords.getInt("Empleados"));
            }
            rsRecords.close();
            ps.close();
        } catch (Exception ex) {
            Logger.getLogger(DepartamentosPuestosView.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al cargar departamentos: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private void mostrarExito(String mensaje) {
        Notification notif = new Notification(mensaje, Notification.Type.HUMANIZED_MESSAGE);
        notif.setDelayMsec(1500);
        notif.setPosition(Position.MIDDLE_CENTER);
        notif.setIcon(FontAwesome.CHECK);
        notif.show(Page.getCurrent());
    }

    private String nvl(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue(
                empresaId + " " + empresaNombre + " — Departamentos");
        Page.getCurrent().setTitle("Sopdi - Departamentos");
    }
}
