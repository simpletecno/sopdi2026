package com.simpletecno.sopdi.utilerias;

import com.vaadin.server.FontAwesome;

//para iconos en menu principal

public class FontAwesomeUtil {
    public static FontAwesome fromName(String name) {
        FontAwesome[] arr = FontAwesome.values();
        int len = arr.length;

        for(int i = 0; i < len; ++i) {
            FontAwesome f = arr[i];
            if(f.name().equals(name)) {
                return f;
            }
        }

        System.out.println("name " + name + " not found in FontAwesome");
        return null;
    }
}