package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author user
 */
public class ProgramaTrabajoCalificarForm extends Window {

    // Tabla Recursos Humanos
    static final String ID_PROVEEDOR_PROPERTY = "IdProveedor";
    static final String NOMBRE_PROVEEDOR_PROPERTY = "Nombre";
    static final String EVAL_PROVEEDOR_PROPERTY = "Evaluación";

    // Tabla Lista
    static final String ID_LISTA_PROPERTY = "Id";
    static final String LISTA_PROPERTY = "Lista";

    // Tabla Caracteristicas
    static final String ID_CARACTERISTICA_PROPERTY = "Id";
    static final String CARACTERISTICA_PROPERTY = "Caracteristica"; // <- Se usara tambien en Supervisiones
    static final String OTRO_EVAL_PROPERTY = "Evaluación Otro";
    static final String EVAL_CARACTERISTICA_PROPERTY = "Evaluación";

    // Tabla Supervisiones
    static final String ID_SUPERVISION_PROPERTY = "Id";
    static final String FECHA_COMETARIO_PROPERTY = "Fecha";
    static final String NOMBRE_COMENTADOR_PROPERTY = "Nombre";
    static final String TIPO_COMENTADOR_PROPERTY = "Cargo";
    static final String COMENTARIO_PROPERTY = "Comentario";

    EnvironmentVars environmentsVars;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;
    Statement stQuery1 = null;
    ResultSet rsRecords1 = null;
    String queryString = "";

    VerticalLayout mainLayout;
    Layout listasLayout;


    Grid gridListas ;
    Grid gridSupervisiones ;
    IndexedContainer listasContainer = new IndexedContainer();
    IndexedContainer caracteristicasContainer = new IndexedContainer();
    IndexedContainer supervisionesContainer = new IndexedContainer();

    Table tableRH;
    Table tableCaracteristicas;
    IndexedContainer containerRH = new IndexedContainer();
    IndexedContainer containerCaracteristicas = new IndexedContainer();

    Map<Integer, StarRating> rhEvaluacion = new HashMap<>();
    Map<Integer, StarRating> regionEvaluacionPasiva = new HashMap<>();
    Map<Integer, StarRating> regionEvaluacionActiva = new HashMap<>();

    Button saveBtn;
    Button salirBtn;



    String idPlanTrabajoIdex; //puede ser rh tambien
    String tipo;
    boolean esJefe;
    boolean esMaestro;
    boolean esSupervisor;
    String centroCosto;
    String idex;
    String descripcion;

    public ProgramaTrabajoCalificarForm(
            String tipo,
            boolean esJefe,
            boolean esMaestro,
            boolean esSupervisor,
            String idPlanTrabajoIdex,
            String centroCosto,
            String idex,
            String descripcion) {
        this.mainUI = UI.getCurrent();
        this.idPlanTrabajoIdex = idPlanTrabajoIdex;
        this.tipo = tipo;
        this.esJefe = esJefe;
        this.esMaestro = esMaestro;
        this.esSupervisor = esSupervisor;
        this.centroCosto = centroCosto;
        this.idex = idex;
        this.descripcion = descripcion;

        if(tipo.equals("SUPERVISAR")) {
            setWidth("90%");
        }
        else {
            setWidth("90%");
        }
        setHeightUndefined();
        setResponsive(true);
        setModal(true);

        environmentsVars = new EnvironmentVars();

        mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeightUndefined();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");

        Label titleLbl;
        if(tipo.equals("SUPERVISAR")) {
            titleLbl = new Label("SUPERVISAR TAREA " + centroCosto + " " + idex + " " + descripcion);
        }
        else {
            titleLbl = new Label("CALIFICAR TAREA " + centroCosto + " " + idex + " " + descripcion);
        }
        titleLbl.setSizeUndefined();
//            titleLbl.addStyleName(Runo.);

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            listasLayout = new HorizontalLayout();
            listasLayout.setWidth("100%");
            listasLayout.setHeightUndefined();
            ((HorizontalLayout)listasLayout).setMargin(true);
            ((HorizontalLayout)listasLayout).setSpacing(true);
        }else{
            listasLayout = new VerticalLayout();
            listasLayout.setWidth("100%");
            listasLayout.setHeightUndefined();
            ((VerticalLayout)listasLayout).setMargin(true);
            ((VerticalLayout)listasLayout).setSpacing(true);
        }


        mainLayout.addComponent(listasLayout);
        mainLayout.setComponentAlignment(listasLayout, Alignment.TOP_CENTER);

//        createGrids();

        if(!tipo.equals("PERSONAL")) { // calificar calidad o supervisar
            createListaGrid();
            llenarListaGrid();

            createCaracteristicasTable();
            llenarCaracteristicasTable();

            if(tipo.equals("CALIDAD")) {
                containerCaracteristicas.addContainerProperty(EVAL_CARACTERISTICA_PROPERTY, HorizontalLayout.class, "");
            }
            else if(tipo.equals("SUPERVISAR")) {
                crearSuperviscionGrid();
            }

        }else{
            createPersonalTable();
            llenarPersonalTable();
        }

        crearButtons();

        setContent(mainLayout);

    }

    private void createPersonalTable(){
        tableRH = new Table("Evaluacion Recursos Humanos");
        tableRH.setWidth("95%");
        tableRH.setImmediate(true);
        tableRH.setPageLength(5);

        containerRH.addContainerProperty(ID_PROVEEDOR_PROPERTY, Integer.class, null);
        containerRH.addContainerProperty(NOMBRE_PROVEEDOR_PROPERTY, String.class, null);
        containerRH.addContainerProperty(EVAL_PROVEEDOR_PROPERTY, HorizontalLayout.class, null);

        tableRH.setContainerDataSource(containerRH);
        tableRH.setVisibleColumns(ID_PROVEEDOR_PROPERTY, NOMBRE_PROVEEDOR_PROPERTY, EVAL_PROVEEDOR_PROPERTY);
        mainLayout.addComponent(tableRH);

    }

    private void createListaGrid(){
        listasContainer.addContainerProperty(ID_LISTA_PROPERTY, String.class, "");
        listasContainer.addContainerProperty(LISTA_PROPERTY, String.class, "");

        gridListas = new Grid("Listas", listasContainer);
        gridListas.setWidth("100%");
        gridListas.setImmediate(true);
        gridListas.setSelectionMode(Grid.SelectionMode.MULTI);
        gridListas.setDescription("Seleccione uno o varios registros.");
        gridListas.setHeightMode(HeightMode.ROW);
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            gridListas.setHeightByRows(8);
        }else{
            gridListas.setHeightByRows(4);
        }

        gridListas.addSelectionListener((event) -> {
            if (event != null) {
                llenarCaracteristicasTable();
            }
        });

        gridListas.getColumn(ID_LISTA_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        listasLayout.addComponent(gridListas);

    }

    private void createCaracteristicasTable(){
        tableCaracteristicas = new Table("");
        tableCaracteristicas.setWidth("100%");
        tableCaracteristicas.setImmediate(true);
        tableCaracteristicas.setSelectable(true);
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            tableCaracteristicas.setPageLength(8);
        }else{
            tableCaracteristicas.setPageLength(4);
        }
        tableCaracteristicas.setContainerDataSource(containerCaracteristicas);

        containerCaracteristicas.addContainerProperty(ID_CARACTERISTICA_PROPERTY, String.class, "");
        containerCaracteristicas.addContainerProperty(CARACTERISTICA_PROPERTY, String.class, "");
        containerCaracteristicas.addContainerProperty(EVAL_CARACTERISTICA_PROPERTY, HorizontalLayout.class, "");
        tableCaracteristicas.setVisibleColumns(ID_CARACTERISTICA_PROPERTY, CARACTERISTICA_PROPERTY, EVAL_CARACTERISTICA_PROPERTY);
        if(tipo.equals("CALIDAD")) {
            containerCaracteristicas.addContainerProperty(OTRO_EVAL_PROPERTY, Integer.class, 0);
            tableCaracteristicas.setVisibleColumns(ID_CARACTERISTICA_PROPERTY, CARACTERISTICA_PROPERTY, EVAL_CARACTERISTICA_PROPERTY, OTRO_EVAL_PROPERTY);
        }
        else if(tipo.equals("SUPERVISAR")) {
            // Listener para detectar la seleccion
            tableCaracteristicas.addValueChangeListener(event -> {
                Object itemId = tableCaracteristicas.getValue(); // Fila detectada
                if (itemId != null) {
                    String idCaracteristica = (String) tableCaracteristicas.getItem(itemId).getItemProperty(ID_CARACTERISTICA_PROPERTY).getValue();
                    llenarSupervisionesGrid(Integer.parseInt(idCaracteristica));
                }
            });
         }

        listasLayout.addComponent(tableCaracteristicas);
    }

    private void  crearSuperviscionGrid(){
        supervisionesContainer.addContainerProperty(ID_SUPERVISION_PROPERTY, String.class, "");
        supervisionesContainer.addContainerProperty(FECHA_COMETARIO_PROPERTY, String.class, "");
        supervisionesContainer.addContainerProperty(TIPO_COMENTADOR_PROPERTY, String.class, "");
        supervisionesContainer.addContainerProperty(CARACTERISTICA_PROPERTY, String.class, "");
        supervisionesContainer.addContainerProperty(NOMBRE_COMENTADOR_PROPERTY, String.class, "");
        supervisionesContainer.addContainerProperty(COMENTARIO_PROPERTY, String.class, "");

        gridSupervisiones = new Grid("Supervisiones", supervisionesContainer);
        gridSupervisiones.setWidth("100%");
        gridSupervisiones.setHeightUndefined();
        gridSupervisiones.setImmediate(true);
        gridSupervisiones.setHeightMode(HeightMode.ROW);
        gridSupervisiones.setHeightByRows(4);

        gridSupervisiones.getColumn(ID_SUPERVISION_PROPERTY).setExpandRatio(1);
        gridSupervisiones.getColumn(FECHA_COMETARIO_PROPERTY).setExpandRatio(1);
        gridSupervisiones.getColumn(TIPO_COMENTADOR_PROPERTY).setExpandRatio(1);
        gridSupervisiones.getColumn(CARACTERISTICA_PROPERTY).setExpandRatio(2);
        gridSupervisiones.getColumn(NOMBRE_COMENTADOR_PROPERTY).setExpandRatio(2);
        gridSupervisiones.getColumn(COMENTARIO_PROPERTY).setExpandRatio(4);

        if(mainUI.getPage().getBrowserWindowWidth() <= 736) {
            gridSupervisiones.getColumn(ID_SUPERVISION_PROPERTY).setHidable(true).setHidden(true);
            gridSupervisiones.getColumn(TIPO_COMENTADOR_PROPERTY).setHidable(true).setHidden(true);
        }

        mainLayout.addComponent(gridSupervisiones);
        mainLayout.setComponentAlignment(gridSupervisiones, Alignment.BOTTOM_CENTER);


    }

    private void llenarPersonalTable(){

        queryString = "Select plan_trabajo_idex_rh.Id, plan_trabajo_idex_rh.IdEmpleado, prv.Nombre ";
        queryString += " From plan_trabajo_idex_rh ";
        queryString += " Inner Join proveedor prv On prv.IdProveedor = plan_trabajo_idex_rh.IdEmpleado";
        if (esJefe) {
            queryString += " Where plan_trabajo_idex_rh.Cargo In ('RH1', 'RH2') ";
            queryString += " And   plan_trabajo_idex_rh.EsJefe <> 'SI'";
        } else if (esMaestro) {
            queryString += " Where plan_trabajo_idex_rh.Cargo In ('RH1', 'RH2') ";
            queryString += " And   plan_trabajo_idex_rh.EsJefe = 'SI'";
        } else {
            queryString += " Where plan_trabajo_idex_rh.Cargo Like 'MAESTRO%'";
        }
        queryString += " And plan_trabajo_idex_rh.IdPlanTrabajoIdex = " + idPlanTrabajoIdex;
        queryString += " AND prv.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY prv.Nombre";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                do {
                    Object itemId = containerRH.addItem();
                    containerRH.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getInt("IdEmpleado"));
                    containerRH.getContainerProperty(itemId, NOMBRE_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("Nombre"));

                    // Crear componente StarRating para calificar la capital
                    StarRating ratingStars = new StarRating(5);

                    // Asociar el Recurso Humano (RH) con el Rating
                    rhEvaluacion.put(rsRecords.getInt("IdEmpleado"), ratingStars);

                    // Puedes agregar el valor predeterminado de las estrellas aquí si es necesario
                    HorizontalLayout ratingLayout = new HorizontalLayout();
                    ratingLayout.addComponent(ratingStars);
                    ratingLayout.setComponentAlignment(ratingStars, Alignment.TOP_CENTER);

                    containerRH.getContainerProperty(itemId, EVAL_PROVEEDOR_PROPERTY).setValue(ratingLayout);
                }while (rsRecords.next());
            }
        }catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CARACTERISTICAS DE CALIDAD", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR BUSCAR CARACTERISTICAS DE CALIDAD : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    private void llenarCaracteristicasTable(){
        String lista = "0";
        try {
            containerCaracteristicas.removeAllItems();
            regionEvaluacionActiva = new HashMap<>();

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            Object objectItem;

            for (Object itemId : gridListas.getSelectedRows()) {
                lista += "," + listasContainer.getContainerProperty(itemId, ID_LISTA_PROPERTY).getValue();
            }
            queryString = "SELECT cld.*, IFNULL( ptic.Valor, 0) as Valor";
            queryString += " FROM calidad_listas_detalle cld";
            queryString += " LEFT JOIN plan_trabajo_idex_ca ptic ON cld.id = ptic.IdCaracteristica";
            queryString +=                                      " AND ptic.IdEmpleado != " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
            queryString +=                                      " AND ptic.IdPlanTrabajoIdex = " + idPlanTrabajoIdex;
            queryString += " WHERE IdLista in (" + lista+ ")";
            queryString += " ORDER BY IdLista";
            System.out.println("queryCaracteristicas=" + queryString);
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    objectItem = containerCaracteristicas.addItem();
                    addCaracteristica(objectItem, rsRecords.getInt("Id"), rsRecords.getString("Descripcion"), rsRecords.getInt("Valor"));
                } while (rsRecords.next());
            }
        }
        catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CARACTERISTICAS DE CALIDAD", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR BUSCAR CARACTERISTICAS DE CALIDAD : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void addCaracteristica(Object item, int id, String descripcion, int valor){
        containerCaracteristicas.getContainerProperty(item, ID_CARACTERISTICA_PROPERTY).setValue(id + "");
        containerCaracteristicas.getContainerProperty(item, CARACTERISTICA_PROPERTY).setValue(descripcion);


        if(tipo.equals("CALIDAD")) {
            containerCaracteristicas.getContainerProperty(item, OTRO_EVAL_PROPERTY).setValue(valor);
            // Crear componente StarRating para calificar la capital o jalar el ya existente
            StarRating ratingStars = new StarRating(5);

            if(regionEvaluacionPasiva.containsKey(id)){
                ratingStars =  regionEvaluacionPasiva.get(id);
            }else{
                regionEvaluacionPasiva.put(id, ratingStars);
            }

            regionEvaluacionActiva.put(id, ratingStars);

            HorizontalLayout ratingLayout = new HorizontalLayout();
            ratingLayout.addComponent(ratingStars);
            ratingLayout.setComponentAlignment(ratingStars, Alignment.TOP_CENTER);
            containerCaracteristicas.getContainerProperty(item, EVAL_CARACTERISTICA_PROPERTY).setValue(ratingLayout);

        }else if(tipo.equals("SUPERVISAR")){

            HorizontalLayout nuevoComentarioLayput = new HorizontalLayout();
            Button nuevoComentarioBtn = new Button("Nuevo Comentaro");
            String[] data = new String[2];
            data[0] = id + "";
            data[1] = descripcion;
            nuevoComentarioLayput.addComponent(nuevoComentarioBtn);
            nuevoComentarioLayput.setComponentAlignment(nuevoComentarioBtn, Alignment.TOP_CENTER);

            nuevoComentarioBtn.setData(data);
            nuevoComentarioBtn.setHeight("20px");
            nuevoComentarioBtn.addClickListener(clickEvent -> {
                ProgramaTrabajoSupervisionTextWindow supervisionTextWindow = new ProgramaTrabajoSupervisionTextWindow(
                        idPlanTrabajoIdex,
                        ((String[])clickEvent.getButton().getData())[0],
                        ((String[])clickEvent.getButton().getData())[1],
                        caracteristicasContainer
                );
                UI.getCurrent().addWindow(supervisionTextWindow);
                supervisionTextWindow.center();
                supervisionTextWindow.setCaption("TEXTWINDOW");
                llenarSupervisionesGrid(Integer.parseInt(((String[]) clickEvent.getButton().getData())[0]));
            });
            containerCaracteristicas.getContainerProperty(item, EVAL_CARACTERISTICA_PROPERTY).setValue(nuevoComentarioLayput);

        }
    }

    public void llenarSupervisionesGrid(int idDetalle){

        supervisionesContainer.removeAllItems();

        queryString = "SELECT ptis.*, p.Cargo, p.Nombre, cld.Descripcion as DetalleNombre, cl.Descripcion as TareaNombre";
        queryString += " FROM plan_trabajo_idex_su as ptis";
        queryString += " INNER JOIN proveedor as p ON p.IdProveedor = ptis.IdEmpleado";
        queryString += " INNER JOIN calidad_listas_detalle cld ON cld.id = ptis.IdCaracteristica";
        queryString += " INNER JOIN calidad_listas cl ON cl.Id = cld.IdLista ";
        queryString += " WHERE ptis.IdCaracteristica in (SELECT id" +
                                                       " FROM calidad_listas_detalle" +
                                                       " WHERE IdLista = (SELECT IdLista FROM calidad_listas_detalle WHERE id =" + idDetalle + " ))" ;
        queryString += " AND ptis.idPlanTrabajoIdex = " + idPlanTrabajoIdex;
        queryString += " ORDER BY ptis.FechaYHora";

        System.out.println("queryCaracteristicas=" + queryString);
        try{
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                Object objectItem;

                gridSupervisiones.setCaption("Supervision: " + rsRecords.getString("TareaNombre"));
                do {
                    objectItem = supervisionesContainer.addItem();
                    supervisionesContainer.getContainerProperty(objectItem, ID_SUPERVISION_PROPERTY).setValue(rsRecords.getString("Id"));
                    supervisionesContainer.getContainerProperty(objectItem, FECHA_COMETARIO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaYHora")));
                    supervisionesContainer.getContainerProperty(objectItem, NOMBRE_COMENTADOR_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    supervisionesContainer.getContainerProperty(objectItem, CARACTERISTICA_PROPERTY).setValue(rsRecords.getString("DetalleNombre"));
                    supervisionesContainer.getContainerProperty(objectItem, TIPO_COMENTADOR_PROPERTY).setValue(rsRecords.getString("Cargo"));
                    supervisionesContainer.getContainerProperty(objectItem, COMENTARIO_PROPERTY).setValue(rsRecords.getString("Comentario"));

                }while (rsRecords.next());
            }

        }catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR SUPERVICIONES", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR DEL SISTEMA AL BUSCAR SUPERVICIONES : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    private void llenarListaGrid() {

        queryString = "Select * ";
        queryString += " From calidad_listas ";
//        queryString += " Order By lista.Id";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                Object objectItem;

                do {
                    objectItem = listasContainer.addItem();
                    listasContainer.getContainerProperty(objectItem, ID_LISTA_PROPERTY).setValue(rsRecords.getString("Id"));
                    listasContainer.getContainerProperty(objectItem, LISTA_PROPERTY).setValue(rsRecords.getString("Descripcion"));

                } while(rsRecords.next());
            }
        }
        catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR LISTAS DE CARACTERISTICAS DE CALIDAD", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR BUSCAR LISTAS DE CARACTERISTICAS DE CALIDAD : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearButtons() {

        saveBtn = new Button("Aceptar");
        saveBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        saveBtn.setIcon(FontAwesome.CHECK);
        saveBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                if(tipo.equals("SUPERVISAR") == false) { //CALIDAD Y PERSONAL
                    for (Object idexObject : caracteristicasContainer.getItemIds()) {
                        if (caracteristicasContainer.getContainerProperty(idexObject, "valor").getValue().toString().trim().isEmpty()) {
                            Notification.show("Todas las lineas deben tener valor de calificación. Revise!", Notification.Type.WARNING_MESSAGE);
                            return;
                        }
                    }
                }

                String messageConfirm = "Está seguro de ACEPTAR LA CALIFICACION DE " + tipo + " ?";
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", messageConfirm,
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    try {
                                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                                        queryString = "Insert Into plan_trabajo_idex_rh_ca (IdPlanTrabajoIdex, IdUsuario, IdEmpleado, FechaYHora, Valor)";
                                        queryString += " Values ";

                                        for (Integer idproveedor : rhEvaluacion.keySet()){

                                            if (rhEvaluacion.get(idproveedor).getSelectedStars() < 1){
                                                Notification.show("NO A CALIFICADO TODOS LOS EMPLEADOS CON AL MENOS 1 ESTRELLA");
                                                return;
                                            }

                                            queryString += "(" + idPlanTrabajoIdex; //id rh
                                            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                                            queryString += "," + idproveedor;
                                            queryString += ",current_timestamp";
                                            queryString += ",'" + rhEvaluacion.get(idproveedor).getSelectedStars() + "'";
                                            queryString += "), ";


                                        }

                                        if(!rhEvaluacion.isEmpty()) {
                                            System.out.println("INSERT plan_trabajo_idex_rh_ca =" + queryString);
                                            stQuery.executeUpdate(queryString.substring(0, queryString.length() - 2));
                                        }

                                        queryString = "Insert Into plan_trabajo_idex_ca (IdPlanTrabajoIdex, IdUsuario, IdCaracteristica, IdEmpleado, FechaYHora, Valor)";
                                        queryString += " Values ";

                                        for (Integer idCaracteristica : regionEvaluacionActiva.keySet()) {

                                            if (regionEvaluacionActiva.get(idCaracteristica).getSelectedStars() < 1){
                                                Notification.show("NO A CALIFICADO TODAS LAS TAREAS CON AL MENOS 1 ESTRELLA");
                                                return;
                                            }
                                            queryString += "(" + idPlanTrabajoIdex;
                                            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                                            queryString += "," + idCaracteristica;
                                            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
                                            queryString += ",current_timestamp";
                                            queryString += ",'" + regionEvaluacionActiva.get(idCaracteristica).getSelectedStars() + "'";
                                            queryString += "), ";

                                        } //endfor

                                        if(!regionEvaluacionActiva.isEmpty()) {
                                            System.out.println("INSERT plan_trabajo_idex_ca =" + queryString);
                                            stQuery.executeUpdate(queryString.substring(0, queryString.length() - 2));
                                        }

                                        if(tipo.equals("SUPERVISAR")) {
                                            Notification.show("SUPERVISION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

                                            String token = "";

                                            int valorMinimo = 1111;
                                            int valorMaximo = 3333;
                                            ThreadLocalRandom random = ThreadLocalRandom.current();
                                            int randomNum = random.nextInt(valorMinimo, valorMaximo + 1);
//                                            System.out.println("Random Number 1 : "+randomNum);

                                            int valorMinimo2 = 4444;
                                            int valorMaximo2 = 8888;
                                            ThreadLocalRandom random2 = ThreadLocalRandom.current();
                                            int randomNum2 = random2.nextInt(valorMinimo2, valorMaximo2 + 1);
//                                            System.out.println("Random Number 2: "+randomNum2);

//                                            token = randomNum + "-"+ randomNum2;
                                            token = String.valueOf(randomNum);

                                            queryString = " INSERT INTO token(Codigo, FechaCreado, Estatus) Values ( ";
                                            queryString += "'" + token + "'";
                                            queryString += ",current_timestamp";
                                            queryString += ",'DISPONIBLE'";
                                            queryString += ")";

                                            stQuery.executeUpdate(queryString);

                                            ConfirmDialog.show(UI.getCurrent(), "TOKEN :", token,
                                                    "ACEPTAR", "ACEPTAR", new ConfirmDialog.Listener() {

                                                        public void onClose(ConfirmDialog dialog) {
                                                            // NINGUNA ACCION, SOLO SE UTILIZA PARA MOSTRAR EL TOKEN...
                                                        }
                                                    }
                                            );

                                        }
                                        else {
                                            Notification.show("CALIFICACION " + tipo + " EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
                                        }

                                        close();

                                        if(tipo.equals("CALIDAD") && (esMaestro || esSupervisor)) {
                                            ProgramaTrabajoCalificarForm planTrabajoCalificarForm = new ProgramaTrabajoCalificarForm(
                                                    "PERSONAL",
                                                    esJefe,
                                                    esMaestro,
                                                    esSupervisor,
                                                    idPlanTrabajoIdex,
                                                    centroCosto,
                                                    idex,
                                                    descripcion
                                            );
                                            planTrabajoCalificarForm.center();
                                            planTrabajoCalificarForm.setModal(true);
                                            UI.getCurrent().addWindow(planTrabajoCalificarForm);
                                        }
                                    } catch (Exception ex) {
                                        Notification.show("Error al INSERTAR registro en tabla plan_trabajo_idex_rh_ca o plan_trabajo_idex_rh_su : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.EJECT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setResponsive(true);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");

        if(tipo.equals("SUPERVISAR")) {
            buttonsLayout.addComponents(salirBtn);
        }
        else {
            buttonsLayout.addComponents(salirBtn, saveBtn);
            buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        }

        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);

    }

    static class StarRating extends HorizontalLayout {

        private int maxStars;
        private int selectedStars = 0;
        Button[] estrellas = new Button[5];

        public StarRating(int maxStars) {
            this.maxStars = maxStars;
            this.selectedStars = 0; // Valor inicial de calificación
            buildStars();
        }

        private void buildStars() {

            // Limpia las estrellas previas si es necesario
            removeAllComponents();

            // Crear los botones de estrella
            for (int i = 1; i <= maxStars; i++) {
                final int starValue = i;
                Button starButton = new Button();
                starButton.setHeight("20px");
                starButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
                estrellas[i-1] = starButton;

                starButton.setIcon(FontAwesome.STAR_O); // Usa el ícono de estrella

                starButton.addClickListener(event -> {
                    if(selectedStars == starValue){
                        selectedStars = 0;
                    }else{
                        selectedStars = starValue;
                    }
                    updateButtons(); // Actualiza las estrellas visualmente
                });

                addComponent(starButton);
                setComponentAlignment(starButton, Alignment.TOP_CENTER);
            }
        }


        private void updateButtons(){
            for (int i = 1; i <= maxStars; i++) {
                if(i <= selectedStars){
                    estrellas[i-1].setIcon(FontAwesome.STAR);
                }else{
                    estrellas[i-1].setIcon(FontAwesome.STAR_O);
                }
            }
        }

        // Obtiene la calificación seleccionada
        public int getSelectedStars() {
            return selectedStars;
        }

        public void selectStar(int x){
            selectedStars = x;
            updateButtons();
        }
    }

    static class Comentario {
        String comentario = "";
        Boolean guardado = false;

        public Comentario(String comentario){
            this.comentario = comentario;
        }

        public Boolean getGuardado() {
            return guardado;
        }
        public String getComentario(){
            return comentario;
        }

        public void setComentario(String comentario){
            this.comentario = comentario;
        }

        public void setGuardado(Boolean guardado) {
            this.guardado = guardado;
        }
    }
}
