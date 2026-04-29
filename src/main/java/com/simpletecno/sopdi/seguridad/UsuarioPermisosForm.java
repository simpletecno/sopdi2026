package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Formulario para asignar permisos de menú a un usuario.
 */
public class UsuarioPermisosForm extends Window {

    private static final String COL_ID     = "ID";
    private static final String COL_OPCION = "Opción";

    private final VerticalLayout mainLayout = new VerticalLayout();
    private final IndexedContainer permisosContainer = new IndexedContainer();
    private final Grid permisosGrid = new Grid("", permisosContainer);
    private final UI mainUI;
    private final int idUsuario;

    Statement stQuery  = null;
    ResultSet rsRecords = null;

    public UsuarioPermisosForm(int idUsuario, String usuarioNombre) {
        this.idUsuario = idUsuario;
        this.mainUI    = UI.getCurrent();

        setResponsive(true);
        setCaption("Permisos de Usuario — " + usuarioNombre + " (ID: " + idUsuario + ")");
        setWidth("55%");
        setHeight("80%");
        center();

        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(new MarginInfo(true, true, false, true));

        createGrid();

        Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener(e -> saveGroup());

        mainLayout.addComponent(saveBtn);
        mainLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        mainLayout.setExpandRatio(permisosGrid, 1f);

        setContent(mainLayout);
    }

    private void createGrid() {
        permisosContainer.addContainerProperty(COL_ID,     String.class, "");
        permisosContainer.addContainerProperty(COL_OPCION, String.class, "");

        permisosGrid.setSizeFull();
        permisosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        permisosGrid.setDescription("Seleccione uno o varios permisos.");
        permisosGrid.setResponsive(true);

        // ID column: fixed narrow width; Opción column: expand to fill
        permisosGrid.getColumn(COL_ID).setWidth(70);
        permisosGrid.getColumn(COL_OPCION).setExpandRatio(1);

        llenarGridPermisos();

        mainLayout.addComponent(permisosGrid);
        mainLayout.setComponentAlignment(permisosGrid, Alignment.TOP_CENTER);
    }

    void llenarGridPermisos() {
        String query =
            "SELECT * FROM opcion_menu OM" +
            " LEFT OUTER JOIN usuario_permisos UP" +
            "   ON UP.IdOpcionMenu = OM.IdOpcionMenu AND UP.IdUsuario = " + idUsuario +
            " WHERE OM.Estatus = 'ACTIVO'" +
            " ORDER BY OM.Orden";

        permisosContainer.removeAllItems();

        try {
            stQuery   = ((SopdiUI) UI.getCurrent()).databaseProvider
                            .getCurrentConnection()
                            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(query);

            while (rsRecords.next()) {
                Object itemId = permisosContainer.addItem();

                String clase       = rsRecords.getString("Clase");
                String descripcion = rsRecords.getString("Descripcion");
                boolean esCategoria = clase == null || clase.isEmpty();

                permisosContainer.getContainerProperty(itemId, COL_ID)
                    .setValue(rsRecords.getString("IdOpcionMenu"));
                permisosContainer.getContainerProperty(itemId, COL_OPCION)
                    .setValue(esCategoria ? "▶ " + descripcion : descripcion);

                if (rsRecords.getObject("IdUsuarioPermiso") != null) {
                    permisosGrid.select(itemId);
                }
            }
        } catch (Exception ex) {
            Notification.show("Error al cargar permisos: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void saveGroup() {
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            // Eliminar permisos actuales del usuario
            stQuery.executeUpdate("DELETE FROM usuario_permisos WHERE IdUsuario = " + idUsuario);

            // Insertar sólo si hay permisos seleccionados
            java.util.Collection<Object> seleccionados = permisosGrid.getSelectedRows();
            if (!seleccionados.isEmpty()) {
                StringBuilder insert = new StringBuilder(
                    "INSERT INTO usuario_permisos (IdUsuario, IdOpcionMenu) VALUES ");

                boolean primero = true;
                for (Object item : seleccionados) {
                    if (!primero) insert.append(",");
                    insert.append("(")
                          .append(idUsuario).append(",")
                          .append(permisosContainer.getContainerProperty(item, COL_ID).getValue())
                          .append(")");
                    primero = false;
                }
                stQuery.executeUpdate(insert.toString());
            }

            Notification.show("Permisos guardados correctamente.", Notification.Type.HUMANIZED_MESSAGE);
            close();

        } catch (Exception ex) {
            Notification.show("Error al guardar permisos: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}