package com.simpletecno.sopdi.utilerias;

/**
 * Estructura ligera y genérica de un recordatorio para mostrar en la ventana de
 * avisos. Sirve tanto para seguimientos/tareas de visita como para eventos del
 * calendario del usuario; los campos tienen nombres neutrales y cada fuente los
 * mapea a su semántica:
 *
 * <ul>
 *   <li>Visita: referencia=CodigoVisita, lugarCliente=Cliente, titulo=Tarea, detalle=Observación.</li>
 *   <li>Evento: referencia="Calendario", lugarCliente=Lugar, titulo=Título, detalle=Descripción.</li>
 * </ul>
 *
 * @author Jose Aguirre
 */
public class Recordatorio {

    // Tipos de recordatorio.
    public static final String TIPO_SEGUIMIENTO   = "SEGUIMIENTO";
    public static final String TIPO_TAREA_VENCIDA = "TAREA VENCIDA";
    public static final String TIPO_EVENTO_HOY    = "EVENTO HOY";
    public static final String TIPO_EVENTO_VENCIDO = "EVENTO VENCIDO";
    public static final String TIPO_AVISO_PAGO    = "AVISO PAGO";

    public String tipo;
    public String referencia;    // visita: CodigoVisita ; evento: "Calendario" ; pago: empresa
    public String lugarCliente;  // visita: Cliente ; evento: Lugar ; pago: usuario que autorizó
    public String titulo;        // visita: Tarea ; evento: Título ; pago: "Cheques por imprimir"
    public String detalle;       // visita: Observación ; evento: Descripción ; pago: detalle
    public String fecha;         // ya formateada para mostrar

    public String idSeguimiento; // solo visitas: permite "marcar atendido"
    public String idEvento;      // solo eventos: permite "marcar realizado"
    public String idAviso;       // solo avisos de pago: permite "marcar atendido"
}
