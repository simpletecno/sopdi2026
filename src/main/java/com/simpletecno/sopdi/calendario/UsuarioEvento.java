package com.simpletecno.sopdi.calendario;

import com.vaadin.ui.components.calendar.event.BasicEvent;

/**
 * Evento de calendario de un usuario. Extiende {@link BasicEvent} agregando
 * el identificador en base de datos (IdEvento) y el lugar del evento.
 *
 * @author Jose Aguirre
 */
public class UsuarioEvento extends BasicEvent {

    private static final long serialVersionUID = 1L;

    private int idEvento;
    private String lugar;

    public int getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }
}
