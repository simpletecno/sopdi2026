
package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.seguridad.UsuarioCorreoForm;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ThreadLocalRandom;


/* Se solicita un token para cualquier ingreso de documento que tenga mas de un mes de atraso */
public class TokenForm extends Window {
    
    VerticalLayout mainLayout;
    
    TextField tokenTxt;
    Button enviarBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    
    public TokenForm(){
        
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("45%");
        setHeight("40%");
               
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
                
        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");
        
        HorizontalLayout layoutToken = new HorizontalLayout();
        layoutToken.setSpacing(true);
        //layoutToken.setMargin(true);
                
        Label titleLbl = new Label("TOKEN GENERADO CON EXITO! ");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");                       
        
        tokenTxt = new TextField();
        tokenTxt.setStyleName(ValoTheme.TEXTFIELD_HUGE);
        tokenTxt.setValue(generarToken());         
        
        enviarBtn = new Button("Enviar A ");
        enviarBtn.setIcon(FontAwesome.SEND);
        enviarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        enviarBtn.setStyleName(ValoTheme.BUTTON_HUGE);
        enviarBtn.addClickListener((event) -> {
            UsuarioCorreoForm usuarioCorreoForm = new UsuarioCorreoForm(tokenTxt.getValue());
            UI.getCurrent().addWindow(usuarioCorreoForm);
            usuarioCorreoForm.center();
            //(Notification.show("Por el momento esta opción no esta habilitada..", Notification.Type.TRAY_NOTIFICATION);
        });
                
        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);              
        
        layoutToken.addComponents(tokenTxt,enviarBtn);
        layoutToken.setComponentAlignment(enviarBtn, Alignment.BOTTOM_CENTER);
                
        mainLayout.addComponent(layoutTitle);
        mainLayout.addComponent(layoutToken);
        
        mainLayout.setComponentAlignment(layoutToken, Alignment.MIDDLE_CENTER);
        
        setContent(mainLayout);
    }

    public String generarToken(){
        
        String token = "";
        
        int valorMinimo = 1111;
        int valorMaximo = 3333;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomNum = random.nextInt(valorMinimo, valorMaximo + 1);
        System.out.println("Random Number 1 : "+randomNum);
        
        int valorMinimo2 = 4444;
        int valorMaximo2 = 8888;
        ThreadLocalRandom random2 = ThreadLocalRandom.current();
        int randomNum2 = random2.nextInt(valorMinimo2, valorMaximo2 + 1);
        System.out.println("Random Number 2: "+randomNum2);
        
        token = randomNum + "-"+ randomNum2;               
        
        try {
            
            queryString = "Select * from token";
        
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            
            if(!rsRecords.next()){
                insertarToken(token);
            }else{
                validadToken(token);
            }
            
        } catch (Exception e) {
            System.out.println("Error al consultar " +e);
            e.printStackTrace();
        }
                                              
        return token;
        
    }
    
    public boolean validadToken(String token){    
        
        boolean validado = false;
        
        try{            
            queryString = "select * from token ";
            queryString += "where Codigo =  '" + token +"'";           
                    
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                generarToken();                                                     
            }else{
                validado = true;  
                insertarToken(token);                               
            }                  
        }catch(Exception ex){                    
            System.out.println("Error al momento de consultar");
            ex.printStackTrace();
        }                
        return validado;
    }
    
    public void insertarToken(String token){
        
        queryString = " INSERT INTO token(Codigo, FechaCreado, Estatus) Values ( ";
        queryString += "'" + token + "'";
        queryString += ",current_timestamp";
        queryString += ",'DISPONIBLE'";
        queryString += ")";          
                               
        try {            
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);       
                
            ((TokenView) (mainUI.getNavigator().getCurrentView())).llenarGridTokens();            
        } catch (Exception e) {
            System.out.println("Error " +e);            
            e.printStackTrace();
        }        
    }
}
