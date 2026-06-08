package com.simpletecno.sopdi.seguridad;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;

import java.sql.Time;
import java.time.LocalTime;

/**
 * Reglas de control de acceso por usuario:
 *   - Restriccion por horario  (rango de horas permitido en el dia).
 *   - Restriccion por IP        (lista blanca de IPs / rangos CIDR autorizados).
 *
 * Ambas restricciones son OPCIONALES por usuario: si el dato esta vacio/NULL,
 * esa restriccion no aplica para el usuario.
 *
 * Los metodos de validacion retornan {@code null} cuando el acceso esta permitido,
 * o un mensaje de error (String) cuando el acceso debe bloquearse.
 *
 * @author joseaguirre
 */
public final class ControlAcceso {

    private ControlAcceso() {
    }

    /**
     * Valida el horario de acceso del usuario.
     *
     * @param inicio hora de inicio permitida (columna usuario.HorarioAccesoInicio); null = sin restriccion
     * @param fin    hora de fin permitida    (columna usuario.HorarioAccesoFin);    null = sin restriccion
     * @return null si el acceso esta permitido; mensaje de error si esta fuera de horario.
     */
    public static String validarHorario(Time inicio, Time fin) {
        // Si falta cualquiera de los dos limites, no se aplica restriccion horaria.
        if (inicio == null || fin == null) {
            return null;
        }

        LocalTime ahora  = LocalTime.now();
        LocalTime desde  = inicio.toLocalTime();
        LocalTime hasta  = fin.toLocalTime();

        boolean permitido;
        if (desde.compareTo(hasta) <= 0) {
            // Rango normal dentro del mismo dia, ej. 08:00 - 18:00
            permitido = !ahora.isBefore(desde) && !ahora.isAfter(hasta);
        } else {
            // Rango que cruza la medianoche, ej. 22:00 - 06:00
            permitido = !ahora.isBefore(desde) || !ahora.isAfter(hasta);
        }

        if (permitido) {
            return null;
        }

        return "Acceso no permitido a esta hora. Horario autorizado: "
                + desde.toString() + " a " + hasta.toString() + ".";
    }

    /**
     * Valida que la IP del cliente este dentro de la lista blanca del usuario.
     *
     * @param ipsAutorizadas lista separada por comas de IPs o rangos CIDR
     *                       (columna usuario.IpsAutorizadas); null/vacio = sin restriccion
     * @param ipCliente      IP del cliente (ver {@link #obtenerIpCliente()})
     * @return null si el acceso esta permitido; mensaje de error si la IP no esta autorizada.
     */
    public static String validarIp(String ipsAutorizadas, String ipCliente) {
        // Sin lista configurada => sin restriccion por IP.
        if (ipsAutorizadas == null || ipsAutorizadas.trim().isEmpty()) {
            return null;
        }

        if (ipCliente == null || ipCliente.trim().isEmpty()) {
            return "No fue posible determinar la direccion de red de origen. Acceso denegado.";
        }

        String ip = ipCliente.trim();
        for (String entrada : ipsAutorizadas.split(",")) {
            String permitida = entrada.trim();
            if (permitida.isEmpty()) {
                continue;
            }
            if (permitida.contains("/")) {
                if (ipEnRangoCidr(ip, permitida)) {
                    return null;
                }
            } else if (permitida.equals(ip)) {
                return null;
            }
        }

        return "Acceso no permitido desde su ubicacion de red (" + ip + ").";
    }

    /**
     * Obtiene la IP del cliente desde la peticion HTTP actual. Considera el
     * encabezado X-Forwarded-For por si el servidor esta detras de un proxy o
     * balanceador; de lo contrario usa la IP remota directa.
     *
     * @return la IP del cliente, o null si no hay peticion disponible.
     */
    public static String obtenerIpCliente() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null) {
            return null;
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            // X-Forwarded-For puede traer una lista: cliente, proxy1, proxy2...
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    /**
     * Indica si una IPv4 esta dentro de un rango CIDR, ej. "192.168.1.0/24".
     * Para entradas no IPv4 o mal formadas retorna false (se ignora la entrada).
     */
    private static boolean ipEnRangoCidr(String ip, String cidr) {
        try {
            String[] partes = cidr.split("/");
            long red       = ipv4ALong(partes[0]);
            int  prefijo   = Integer.parseInt(partes[1].trim());
            long objetivo  = ipv4ALong(ip);

            if (red < 0 || objetivo < 0 || prefijo < 0 || prefijo > 32) {
                return false;
            }

            long mascara = prefijo == 0 ? 0L : (0xFFFFFFFFL << (32 - prefijo)) & 0xFFFFFFFFL;
            return (red & mascara) == (objetivo & mascara);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Convierte una IPv4 "a.b.c.d" a su valor numerico; retorna -1 si no es IPv4 valida.
     */
    private static long ipv4ALong(String ip) {
        String[] octetos = ip.trim().split("\\.");
        if (octetos.length != 4) {
            return -1;
        }
        long valor = 0;
        for (String octeto : octetos) {
            int n = Integer.parseInt(octeto.trim());
            if (n < 0 || n > 255) {
                return -1;
            }
            valor = (valor << 8) | n;
        }
        return valor;
    }

    /**
     * Normaliza una hora ingresada como "HH:mm" o "HH:mm:ss" a un literal SQL.
     *
     * @param hhmm texto de hora; vacio/null => "NULL"
     * @return "'HH:mm:ss'" valido para SQL, o "NULL" si esta vacio.
     * @throws IllegalArgumentException si el texto no tiene formato de hora valido.
     */
    public static String horaALiteralSql(String hhmm) {
        if (hhmm == null || hhmm.trim().isEmpty()) {
            return "NULL";
        }
        String t = hhmm.trim();
        // Acepta H:mm, HH:mm o HH:mm:ss
        if (!t.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
            throw new IllegalArgumentException("Formato de hora invalido: '" + hhmm + "'. Use HH:mm (ej. 08:00).");
        }
        String[] p = t.split(":");
        int h = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        int s = p.length == 3 ? Integer.parseInt(p[2]) : 0;
        if (h > 23 || m > 59 || s > 59) {
            throw new IllegalArgumentException("Hora fuera de rango: '" + hhmm + "'.");
        }
        return String.format("'%02d:%02d:%02d'", h, m, s);
    }

    /**
     * Convierte un java.sql.Time a texto "HH:mm" para mostrar en formularios; "" si es null.
     */
    public static String timeAHhmm(Time time) {
        if (time == null) {
            return "";
        }
        LocalTime lt = time.toLocalTime();
        return String.format("%02d:%02d", lt.getHour(), lt.getMinute());
    }
}