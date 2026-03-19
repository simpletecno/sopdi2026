package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.poi.ss.formula.functions.Index;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProveedorEmpresaView extends VerticalLayout implements View {

    private IndexedContainer maestroContainer = new IndexedContainer();
    private IndexedContainer misContainer = new IndexedContainer();

    private Grid gridMaestro;
    private Grid gridMis;

    private Button btnAgregar; // pasa del maestro a mis
    private Button btnNuevo;
    private Button btnEditar;
    private Button btnQuitar;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    String idEmpresa = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

    public ProveedorEmpresaView() {
        setSizeFull();
        setSpacing(true);
        setMargin(true);

        buildUI();
        loadData(); 
        refreshButtonsState();
    }

    private void buildUI() {
        Label title = new Label("Proveedores, Clientes y Otros");
        title.addStyleName(ValoTheme.LABEL_H2);

        maestroContainer.addContainerProperty("codigo", String.class, null);
        maestroContainer.addContainerProperty("nombre", String.class, "");
        maestroContainer.addContainerProperty("nit", String.class, "");

        gridMaestro = new Grid("Maestro de Proveedores", maestroContainer);
        gridMaestro.setSizeFull();
        gridMaestro.setSelectionMode(Grid.SelectionMode.MULTI);

        Grid.HeaderRow filterRow = gridMaestro.appendHeaderRow();

        Grid.HeaderCell cell1 = filterRow.getCell("nombre");

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(15);

        filterField1.addTextChangeListener(change -> {
            maestroContainer.removeContainerFilters("nombre");

            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                maestroContainer.addContainerFilter(
                        new SimpleStringFilter("nombre",
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        misContainer.addContainerProperty("codigo", String.class, null);
        misContainer.addContainerProperty("nombre", String.class, "");
        misContainer.addContainerProperty("nit", String.class, "");
        misContainer.addContainerProperty("codigoInterno", String.class, "");

        gridMis = new Grid("Proveedores de esta Empresa", misContainer);
        gridMis.setSizeFull();
        gridMis.setSelectionMode(Grid.SelectionMode.SINGLE);

        Grid.HeaderRow filterRowMis = gridMis.appendHeaderRow();

        Grid.HeaderCell cellMiss1 = filterRowMis.getCell("nombre");

        TextField filterFieldMiss1 = new TextField();
        filterFieldMiss1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldMiss1.setInputPrompt("Filtrar");
        filterFieldMiss1.setColumns(15);

        filterFieldMiss1.addTextChangeListener(change -> {
            misContainer.removeContainerFilters("nombre");

            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                misContainer.addContainerFilter(
                        new SimpleStringFilter("nombre",
                                change.getText(), true, false));
            }
        });
        cellMiss1.setComponent(filterFieldMiss1);

        // Botón para pasar seleccionados
        btnAgregar = new Button(">>");
        btnAgregar.setDescription("Agregar al grid de la derecha (Mis Proveedores)");
        btnAgregar.addStyleName(ValoTheme.BUTTON_PRIMARY);

        btnAgregar.addClickListener(e -> {
            Collection<Object> selected = gridMaestro.getSelectedRows();
            if (selected == null || selected.isEmpty()) {
                Notification.show("Selecciona uno o más proveedores del maestro.",
                        Notification.Type.WARNING_MESSAGE);
                return;
            }

            agregarProveedor();
        });

        btnQuitar = new Button("<<");
        btnQuitar.setDescription("Quitar del grid de la derecha (Mis Proveedores)");
        btnQuitar.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnQuitar.addClickListener(e -> {
            Collection<Object> selected = gridMis.getSelectedRows();
            if (selected == null || selected.isEmpty()) {
                Notification.show("Selecciona uno o más proveedores del grid Mis Proveedores.",
                        Notification.Type.WARNING_MESSAGE);
                return;
            }

            quitarProveedor();
        });

        // Layout central: grid maestro | botones | grid mis
        VerticalLayout middleButtons = new VerticalLayout(btnAgregar, btnQuitar);
        middleButtons.setSpacing(true);
        middleButtons.setMargin(true);
        middleButtons.setWidthUndefined();
        middleButtons.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        HorizontalLayout gridsRow = new HorizontalLayout();
        gridsRow.setSizeFull();
        gridsRow.setSpacing(true);

        gridsRow.addComponent(gridMaestro);
        gridsRow.addComponent(middleButtons);
        gridsRow.setComponentAlignment(middleButtons, Alignment.MIDDLE_CENTER);
        gridsRow.addComponent(gridMis);

        gridsRow.setExpandRatio(gridMaestro, 1f);
        gridsRow.setExpandRatio(gridMis, 1f);

        // Botones bottom del segundo grid
        btnNuevo = new Button("Nuevo");
        btnEditar = new Button("Editar");

        btnNuevo.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        btnEditar.addStyleName(ValoTheme.BUTTON_PRIMARY);

        btnNuevo.addClickListener(e -> onNuevo());
        btnEditar.addClickListener(e -> onEditar());

        HorizontalLayout bottomButtons = new HorizontalLayout(btnNuevo, btnEditar);
        bottomButtons.setSpacing(false);
        bottomButtons.setWidth("25%");
        bottomButtons.setComponentAlignment(btnNuevo, Alignment.MIDDLE_LEFT);
        bottomButtons.setComponentAlignment(btnEditar, Alignment.MIDDLE_LEFT);
//        bottomButtons.setComponentAlignment(btnQuitar, Alignment.MIDDLE_LEFT);

        // Reaccionar a selección en "Mis Proveedores" para habilitar Editar/Quitar
        gridMis.addSelectionListener(e -> refreshButtonsState());

        addComponent(title);
        setComponentAlignment(title, Alignment.TOP_CENTER);
        addComponent(gridsRow);
        addComponent(bottomButtons);
        setComponentAlignment(bottomButtons, Alignment.BOTTOM_RIGHT);
//        setExpandRatio(bottomButtons, 1);

        setExpandRatio(gridsRow, 1f);
    }

    private void loadData() {
        maestroContainer.removeAllItems();
        misContainer.removeAllItems();

        String queryString = " SELECT * ";
        queryString += " FROM proveedor";
        queryString += " WHERE Inhabilitado = 0";
        queryString += " AND Codigo NOT IN (SELECT pe.Codigo FROM proveedor_empresa pe WHERE pe.IdEmpresa = " + idEmpresa + ")";
        queryString += " ORDER BY proveedor.Nombre";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            Object itemId;

            if (rsRecords.next()) { //  encontrado

                do {
                    itemId = maestroContainer.addItem();

                    maestroContainer.getContainerProperty(itemId, "codigo").setValue(rsRecords.getString("Codigo"));
                    maestroContainer.getContainerProperty(itemId, "nombre").setValue(rsRecords.getString("Nombre"));
                    maestroContainer.getContainerProperty(itemId, "nit").setValue(rsRecords.getString("Nit"));
                } while (rsRecords.next());
            }

            queryString = " SELECT * ";
            queryString += " FROM proveedor_empresa";
            queryString += " WHERE Inhabilitado = 0";
            queryString += " AND IdEmpresa = " + idEmpresa ;
            queryString += " ORDER BY Nombre";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    itemId = misContainer.addItem();

                    misContainer.getContainerProperty(itemId, "codigo").setValue(rsRecords.getString("Codigo"));
                    misContainer.getContainerProperty(itemId, "nombre").setValue(rsRecords.getString("Nombre"));
                    misContainer.getContainerProperty(itemId, "nit").setValue(rsRecords.getString("Nit"));
                    misContainer.getContainerProperty(itemId, "codigoInterno").setValue(rsRecords.getString("CodigoInterno"));

                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar proveedores: " + ex1.getMessage());
            Notification.show("Error al listar proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void agregarProveedor() {
        int moved = 0;

        try {

            String queryString = "INSERT INTO proveedor_empresa (Codigo, IdEmpresa, IdProveedor, NIT, TipoPersona ";
            queryString += ",Genero, Nombre, PrimerNombre, SegundoNombre, PrimerApellido, SegundoApellido, ApellidoCasada ";
            queryString += ",Nacionalidad, DPI, Direccion, Regimen, Telefono, TelefonoEmergencia, Email, EsProveedor ";
            queryString += ",EsCliente, EsAgenteRetenedorISR, EsAgenteRetenedorIVA, EsInstitucionFiscal ";
            queryString += ",EsInstitucionSeguroSocial, EsSujetoRetencionDefinitivaISR, EsBanco)";
            queryString += " SELECT Codigo, " + idEmpresa + ", Codigo, NIT, TipoPersona ";
            queryString += ",Genero, Nombre, PrimerNombre, SegundoNombre, PrimerApellido, SegundoApellido, ApellidoCasada ";
            queryString += ",Nacionalidad, DPI, Direccion, Regimen, Telefono, TelefonoEmergencia, Email, EsProveedor ";
            queryString += ",EsCliente, EsAgenteRetenedorISR, EsAgenteRetenedorIVA, EsInstitucionFiscal ";
            queryString += ",EsInstitucionSeguroSocial, EsSujetoRetencionDefinitivaISR, EsBanco ";
            queryString += " FROM proveedor ";
            queryString += " WHERE Codigo IN (";
            for(Object itemId : gridMaestro.getSelectedRows()) {
                queryString += "'" + maestroContainer.getContainerProperty(itemId, "codigo").getValue() + "',";
                moved++;
            }
            queryString = queryString.substring(0, queryString.length() - 1);
            queryString += ")";

Logger.getLogger(ProveedorEmpresaView.class.getName()).log(Level.INFO, queryString);

            stQuery.executeUpdate(queryString);

            Notification.show("Agregados: " + moved, Notification.Type.TRAY_NOTIFICATION);
            gridMaestro.deselectAll();
            gridMis.deselectAll();
            loadData();

        } catch (Exception ex1) {
            System.out.println("Error al agregar a mis proveedores: " + ex1.getMessage());
            Notification.show("Error al agregar a mis proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    private void quitarProveedor() {
        int moved = 0;

        try {

            String queryString = "SELECT * FROM contabilidad_partida";
            queryString += " WHERE IdProveedor IN (";
            for(Object itemId : gridMis.getSelectedRows()) {
                queryString += "'" + misContainer.getContainerProperty(itemId, "codigo").getValue() + "',";
                moved++;
            }
            queryString = queryString.substring(0, queryString.length() - 1);
            queryString += ")";
            queryString += " AND IdEmpresa = " + idEmpresa;

            Logger.getLogger(ProveedorEmpresaView.class.getName()).log(Level.INFO, queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                Notification.show("PROVEEDORES CON HISTORIAL EN LA EMPRESA, NO SE PERMITE QUITAR. ", Notification.Type.WARNING_MESSAGE);
            }
            else {
                queryString = "DELETE FROM proveedor_empresa";
                queryString += " WHERE Codigo IN (";
                for(Object itemId : gridMis.getSelectedRows()) {
                    queryString += "'" + misContainer.getContainerProperty(itemId, "codigo").getValue() + "',";
                    moved++;
                }
                queryString = queryString.substring(0, queryString.length() - 1);
                queryString += ")";
                queryString += " AND IdEmpresa = " + idEmpresa;

                Logger.getLogger(ProveedorEmpresaView.class.getName()).log(Level.INFO, queryString);

                stQuery.executeUpdate(queryString);

                Notification.show("Quitados: " + moved, Notification.Type.TRAY_NOTIFICATION);
                gridMaestro.deselectAll();
                gridMis.deselectAll();
                loadData();
            }

        } catch (Exception ex1) {
            System.out.println("Error al quitar de mis proveedores: " + ex1.getMessage());
            Notification.show("Error al quitar de mis proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void refreshButtonsState() {
        boolean hasSelection = gridMis.getSelectedRow() != null;
        btnEditar.setEnabled(hasSelection);
        btnQuitar.setEnabled(hasSelection);
    }

    // --- Acciones del grid "Mis Proveedores" ---

    private void onNuevo() {
        ProveedorEmpresaForm proveedorEmpresaForm =
                new ProveedorEmpresaForm("0");
        UI.getCurrent().addWindow(proveedorEmpresaForm);
        proveedorEmpresaForm.center();
        proveedorEmpresaForm.setModal(true);
    }

    private void onEditar() {
        Collection<Object> selected = gridMis.getSelectedRows();
        if (selected == null || selected.isEmpty()) {
            Notification.show("Selecciona un proveedor!",
                    Notification.Type.WARNING_MESSAGE);
            return;
        }

        ProveedorEmpresaForm proveedorEmpresaForm =
                new ProveedorEmpresaForm((String) misContainer.getContainerProperty(gridMis.getSelectedRow(), "codigo").getValue());
        UI.getCurrent().addWindow(proveedorEmpresaForm);
        proveedorEmpresaForm.center();
        proveedorEmpresaForm.setModal(true);
    }

    private void onQuitar() {
        Proveedor selected = (Proveedor) gridMis.getSelectedRow();
        if (selected == null) return;

        misContainer.removeItem(selected);
        gridMis.deselectAll();
        refreshButtonsState();
    }

    // --- Utilidades / Datos ---

    private boolean containsById(BeanItemContainer<Proveedor> container, Integer id) {
        for (Proveedor p : container.getItemIds()) {
            if (Objects.equals(p.getId(), id)) return true;
        }
        return false;
    }
        @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub
        Page.getCurrent().setTitle("Sopdi - Mis proveedores");
    }

    public class Proveedor  {

        private static final long serialVersionUID = 1L;

        private Integer id;
        private String codigo;
        private Integer codigoInterno;
        private String nit;
        private String tipoPersona;
        private String regimen;
        private String genero;
        private String nombre;
        private String primerNombre;
        private String segundoNombre;
        private String primerApellido;
        private String segundoApellido;
        private String apellidoCasada;
        private String nacionalidad;
        private String dpi;
        private String direccion;
        private String telefono;
        private String telefonoEmergencia;
        private String email;

        private Boolean esProveedor;
        private Boolean esCliente;
        private Boolean esAgenteRetenedorISR;
        private Boolean esAgenteRetenedorIVA;
        private Boolean esInstitucionFiscal;
        private Boolean esInstitucionSeguroSocial;
        private Boolean esSujetoRetencionDefinitivaISR;
        private Boolean esBanco;
        private Boolean inhabilitado;

        // 🔥 Constructor vacío (OBLIGATORIO para frameworks)
        public Proveedor() {
        }

        // 🔥 Constructor útil para grids / DTO rápidos
        public Proveedor(int id, String codigo, String nombre, String nit) {
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.nit = nit;
        }

        // ================= GETTERS & SETTERS =================

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public Integer getCodigoInterno() {
            return codigoInterno;
        }

        public void setCodigoInterno(Integer codigoInterno) {
            this.codigoInterno = codigoInterno;
        }

        public String getNit() {
            return nit;
        }

        public void setNit(String nit) {
            this.nit = nit;
        }

        public String getTipoPersona() {
            return tipoPersona;
        }

        public void setTipoPersona(String tipoPersona) {
            this.tipoPersona = tipoPersona;
        }

        public String getRegimen() {
            return regimen;
        }

        public void setRegimen(String regimen) {
            this.regimen = regimen;
        }

        public String getGenero() {
            return genero;
        }

        public void setGenero(String genero) {
            this.genero = genero;
        }

        public String getNombre() {
            return nombre;
        }

        // 🔥 IMPORTANTE para búsquedas y grids
        public String getNombreCompleto() {
            if (nombre != null && !nombre.isEmpty()) {
                return nombre;
            }

            return (primerNombre + " " +
                    (segundoNombre != null ? segundoNombre : "") + " " +
                    primerApellido + " " +
                    (segundoApellido != null ? segundoApellido : "")).trim();
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getPrimerNombre() {
            return primerNombre;
        }

        public void setPrimerNombre(String primerNombre) {
            this.primerNombre = primerNombre;
        }

        public String getSegundoNombre() {
            return segundoNombre;
        }

        public void setSegundoNombre(String segundoNombre) {
            this.segundoNombre = segundoNombre;
        }

        public String getPrimerApellido() {
            return primerApellido;
        }

        public void setPrimerApellido(String primerApellido) {
            this.primerApellido = primerApellido;
        }

        public String getSegundoApellido() {
            return segundoApellido;
        }

        public void setSegundoApellido(String segundoApellido) {
            this.segundoApellido = segundoApellido;
        }

        public String getApellidoCasada() {
            return apellidoCasada;
        }

        public void setApellidoCasada(String apellidoCasada) {
            this.apellidoCasada = apellidoCasada;
        }

        public String getNacionalidad() {
            return nacionalidad;
        }

        public void setNacionalidad(String nacionalidad) {
            this.nacionalidad = nacionalidad;
        }

        public String getDpi() {
            return dpi;
        }

        public void setDpi(String dpi) {
            this.dpi = dpi;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }

        public String getTelefono() {
            return telefono;
        }

        // 🔥 Normalizador útil
        public String getTelefonoNormalizado() {
            if (telefono == null || telefono.equalsIgnoreCase("SIN TELEFONO")) {
                return "";
            }
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public String getTelefonoEmergencia() {
            return telefonoEmergencia;
        }

        public void setTelefonoEmergencia(String telefonoEmergencia) {
            this.telefonoEmergencia = telefonoEmergencia;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Boolean getEsProveedor() {
            return esProveedor;
        }

        public void setEsProveedor(Boolean esProveedor) {
            this.esProveedor = esProveedor;
        }

        public Boolean getEsCliente() {
            return esCliente;
        }

        public void setEsCliente(Boolean esCliente) {
            this.esCliente = esCliente;
        }

        public Boolean getEsAgenteRetenedorISR() {
            return esAgenteRetenedorISR;
        }

        public void setEsAgenteRetenedorISR(Boolean esAgenteRetenedorISR) {
            this.esAgenteRetenedorISR = esAgenteRetenedorISR;
        }

        public Boolean getEsAgenteRetenedorIVA() {
            return esAgenteRetenedorIVA;
        }

        public void setEsAgenteRetenedorIVA(Boolean esAgenteRetenedorIVA) {
            this.esAgenteRetenedorIVA = esAgenteRetenedorIVA;
        }

        public Boolean getEsInstitucionFiscal() {
            return esInstitucionFiscal;
        }

        public void setEsInstitucionFiscal(Boolean esInstitucionFiscal) {
            this.esInstitucionFiscal = esInstitucionFiscal;
        }

        public Boolean getEsInstitucionSeguroSocial() {
            return esInstitucionSeguroSocial;
        }

        public void setEsInstitucionSeguroSocial(Boolean esInstitucionSeguroSocial) {
            this.esInstitucionSeguroSocial = esInstitucionSeguroSocial;
        }

        public Boolean getEsSujetoRetencionDefinitivaISR() {
            return esSujetoRetencionDefinitivaISR;
        }

        public void setEsSujetoRetencionDefinitivaISR(Boolean esSujetoRetencionDefinitivaISR) {
            this.esSujetoRetencionDefinitivaISR = esSujetoRetencionDefinitivaISR;
        }

        public Boolean getEsBanco() {
            return esBanco;
        }

        public void setEsBanco(Boolean esBanco) {
            this.esBanco = esBanco;
        }

        public Boolean getInhabilitado() {
            return inhabilitado;
        }

        public void setInhabilitado(Boolean inhabilitado) {
            this.inhabilitado = inhabilitado;
        }

        // 🔥 CRÍTICO para grids y containers
        @Override
        public String toString() {
            return nombre + " (" + nit + ")";
        }
    }
}
