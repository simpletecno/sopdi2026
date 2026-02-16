
package com.simpletecno.sopdi.recursoshumanos;

import com.vaadin.server.*;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;

/**
 *
 * @author joseaguirre
 */
public class InfoGruposTrabajoForm extends Window {

    VerticalLayout mainLayout;
    MarginInfo  marginInfo;
        
    Button salirBtn;   
    UI mainUI;
    
    public InfoGruposTrabajoForm() {
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setCaption("Informacion sobre el funcionamiento de la vista Grupos de Trabajo");
        setWidth("75%");
        setHeight("65%");
        
        marginInfo = new MarginInfo(true,true,false,true);
        
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        createFormLayout();

        salirBtn = new Button("Salir");
        //salirBtn.setIcon(FontAwesome.); 
        salirBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {          close();  }
        });                

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);        
        
        setContent(mainLayout);
    }
    
    private void createFormLayout() {
        HorizontalLayout tab1Layout = new HorizontalLayout();
        tab1Layout.setSpacing(true);
        tab1Layout.setMargin(true);
        tab1Layout.setWidth("100%");
        tab1Layout.setHeight("100%");

        
        String basepath = VaadinService.getCurrent()
                  .getBaseDirectory().getAbsolutePath();

        // Image as a file resource
        FileResource resource = new FileResource(new File(basepath +
                        "\\WEB-INF\\logos\\dar_de_baja.jpeg"));
        
        System.out.println("direccion : " + basepath +
                        "\\WEB-INF\\logos\\dar_de_baja.jpeg");

        // Show the image in the application
        Image image = new Image("", resource);   
        image.setWidth("100%");
        image.setHeight("110%");
        
        
        Label instruccionesLbl = new Label(
                "<b>PARA DAR DE BAJA O CERRAR UN GRUPO</b>\n" +
                        "<ul>"+
                        "  <li><b>PASO 1: HACER DOBLE CLIC EN EL LISTADO DE GRUPOS.</b></li>"+
                        "  <li><b>PASO 2: EN ESTATUS SELECCIONAR INACTIVO.</b></li>"+
                        "  <li><b>PASO 3: GUARDAR EL CAMBIO HACIENDO CLICK EN ACTUALIZAR.</b></li>"+
                        "</ul>",                       
                ContentMode.HTML);
        instruccionesLbl.addStyleName(ValoTheme.LABEL_COLORED);
        
        tab1Layout.addComponent(instruccionesLbl);
        tab1Layout.setComponentAlignment(instruccionesLbl, Alignment.TOP_LEFT);
                   
        tab1Layout.addComponent(image);
        tab1Layout.setComponentAlignment(image, Alignment.TOP_RIGHT);
        
        mainLayout.addComponent(tab1Layout);
        mainLayout.setComponentAlignment(tab1Layout, Alignment.TOP_CENTER);

    }
      
}