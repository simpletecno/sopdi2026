/*
 * Ventana para reigtro de Seguimiento de Visitas.
 * ...
 * @author Jose Aguirre
*/

package com.simpletecno.sopdi.utilerias;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class DescripcionWindow extends Window
{      
    private com.vaadin.ui.Label descripcionLbl;
        
    private VerticalLayout mainLayout = new VerticalLayout();
    
    private Button exitBtn;

    public DescripcionWindow(String descripcion) {

        setCaption("SOPDI -- Seguimiento");
        
        descripcionLbl = new com.vaadin.ui.Label ("Texto del Seguimiento:");
        descripcionLbl.setWidth("600px");
        descripcionLbl.setWidth("300px");
        descripcionLbl.setValue(descripcion);
//        descripcionLbl.addStyleName("v-textfield-uppercase");
        
        mainLayout.addComponent(descripcionLbl);
                
        exitBtn = new Button("Salir");
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.setIcon(FontAwesome.ARROW_RIGHT); 
        exitBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                close();
            }
        });        
    
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.setMargin(true);
        footer.addComponent(exitBtn);
        footer.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);

        mainLayout.addComponent(footer);
        mainLayout.setComponentAlignment(footer, Alignment.BOTTOM_CENTER);

        setContent(mainLayout);
 
        setWidth("550px");
        setHeight("300px");
                        
        setModal(true);        
    }            
}
