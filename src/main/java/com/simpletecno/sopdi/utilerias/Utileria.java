package com.simpletecno.sopdi.utilerias;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase Utileria, contiene varios metodos utiles para el programa, como la
 * obtencion de fechas en formatos especiales.
 *
 * @author (jaguirre )
 */
public class Utileria {

    public static DecimalFormat numberFormatMoney = new DecimalFormat("#,###,##0.00");
    public static DecimalFormat numberFormatEntero = new DecimalFormat("######0.00");
    public static DecimalFormat numberFormatSimple = new DecimalFormat("######0.0");

    // Zona horaria explícita para evitar corrimientos de día
    private static final ZoneId ZONE_GT = ZoneId.of("America/Guatemala");
    private static final DateTimeFormatter FMT_BASICO = DateTimeFormatter.BASIC_ISO_DATE; // "YYYYMMDD"


    private String referencia = "";

    private static final Map<Integer, String> codigosPostales = new HashMap<>();
    static {
        codigosPostales.put(1, "01001");
        codigosPostales.put(2, "01002");
        codigosPostales.put(3, "01003");
        codigosPostales.put(4, "01004");
        codigosPostales.put(5, "01005");
        codigosPostales.put(6, "01006");
        codigosPostales.put(7, "01007");
        codigosPostales.put(8, "01008");
        codigosPostales.put(9, "01009");
        codigosPostales.put(10, "01010");
        codigosPostales.put(11, "01011");
        codigosPostales.put(12, "01012");
        codigosPostales.put(13, "01013");
        codigosPostales.put(14, "01014");
        codigosPostales.put(15, "01015");
        codigosPostales.put(16, "01016");
        codigosPostales.put(17, "01017");
        codigosPostales.put(18, "01018");
        codigosPostales.put(19, "01019");
        codigosPostales.put(21, "01021");
    }
    /**
     * Obtiene el código postal correspondiente a una dirección de Guatemala.
     * @param direccion Cadena de texto que representa la dirección (ej. "Zona 16 Santa Rosita").
     * @return Código postal correspondiente a la zona detectada o un mensaje de error si no se encuentra.
     */
    public static String obtenerCodigoPostal(String direccion) {
        direccion = direccion.toLowerCase();

        // Regex para capturar "zona 16", "z 16", "zona16", "z16"
        Pattern pattern = Pattern.compile("\\b(?:zona|z)\\s*([0-9]{1,2})\\b", Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(direccion);

        if (matcher.find()) {
            try {
                int zona = Integer.parseInt(matcher.group(1));
                if (codigosPostales.containsKey(zona)) {
                    return codigosPostales.get(zona);
                }
            } catch (NumberFormatException e) {
                return "01001";
            }
        }

        return "01001";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Utileria utileria = (Utileria) o;
        return Objects.equals(referencia, utileria.referencia);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(referencia);
    }

    /**
     *
     */
    public Utileria() {

        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        int x = new Double(Math.random() * 999).intValue();

        referencia = String.valueOf(today.get(java.util.GregorianCalendar.YEAR)).substring(2);
        referencia += String.format("%03d", today.get(java.util.GregorianCalendar.DAY_OF_YEAR));
        referencia += String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        referencia += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));
        referencia += String.format("%03d", x);
    }

    public Utileria(int inicial) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        int x = new Double(Math.random() * inicial).intValue();

        referencia = String.valueOf(today.get(java.util.GregorianCalendar.YEAR)).substring(2);
        referencia += String.format("%03d", today.get(java.util.GregorianCalendar.DAY_OF_YEAR));
        referencia += String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        referencia += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));
        referencia += String.format("%03d", x);
    }

    /**
     * Returns the formated form of the number given
     * @param numero Double a Combertir en String dos decimales
     * @return formated String
     */
    public static String format(Double numero){
        return Utileria.numberFormatEntero.format(numero + 0.0001);
    }
    public static String format(BigDecimal numero) { return Utileria.numberFormatEntero.format(numero); }

    /**
     * Returns the formated form of the number given
     * @param numero Double a Combertir en String un decimal
     * @return formated String
     */
    public static String formatSimple(Double numero){
        return Utileria.numberFormatSimple.format(numero + 0.0001);
    }

    /**
     * Returns the formated form of the number given
     * @param n Numero a redondear a dos decimales
     * @return BigDecimal Redondeado
     */
    public static Double round(Number n) {
        return BigDecimal.valueOf(n.doubleValue())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Returns the formated form of the number given
     * @param n Numero a redondear
     * @param scale Numero de decimales a redondear
     *  @return BigDecimal Redondeado
     */
    public static double round(Number n, int scale) {
        if (n == null) throw new IllegalArgumentException("El número no puede ser null");
        return BigDecimal.valueOf(n.doubleValue())
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }


    /**
     * Get the value of referencia
     *
     * @return the value of referencia
     */
    public String getReferencia() {
        return referencia;
    }

    public String getAutorizacion() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();

        String autorizacion = String.valueOf(today.get(java.util.GregorianCalendar.YEAR)).substring(3, 4);
        autorizacion += rellenaString(String.valueOf(today.get(java.util.GregorianCalendar.DAY_OF_YEAR)), '0', 3, 1);
        autorizacion += String.valueOf(today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        autorizacion += String.valueOf(today.get(java.util.GregorianCalendar.MINUTE));

        return autorizacion;
    }

    /**
     * Retorna la fecha del sistema en formato yyyy/mm/dd
     *
     * @return la fecha del sistema en formato yyyy/mm/dd
     */
    public String getFecha() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();

        String fecha;
        fecha = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fecha += "/";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));

        return fecha;
    }

    public static String getStaticFecha() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();

        String fecha;

        fecha = String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fecha += "/";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fecha;
    }

    /**
     * Retorna un string dd/mm/yyyy conteniendo el valor dateToConvert enviado
     * dd/mm/yy
     *
     * @return la fecha del sistema en formato dd/mm/yy
     */
    public static String getStaticFecha(Date dateToConvert) {
        if(dateToConvert == null) return "";

        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        String fecha;
        fecha = String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fecha += "/";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fecha;
    }

    public static String getStaticMes(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);
        return String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
    }

    public static String getStaticAno(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);
        return String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
    }

    public static int getStaticUltimoDiaDelMes(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        return today.getActualMaximum(java.util.GregorianCalendar.DAY_OF_MONTH);
    }


    /**
     * Retorna un string dd/mm/yyyy conteniendo el valor dateToConvert enviado
     * dd/mm/yy
     *
     * @return la fecha del sistema en formato dd/mm/yy
     */
    public String getFecha(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        String fecha;
        fecha = String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fecha += "/";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fecha;
    }

    /**
     * Retorna un string mm/dd/yyyy conteniendo el valor dateToConvert enviado
     * mm/dd/yyyy
     *
     * @return la fecha del sistema en formato mm/dd/yyyy
     */
    public String getFecha_mmddyyy() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(new Date());

        String fecha = "";
        fecha = String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fecha += "/";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fecha;
    }

    /**
     * Retorna un string yyyy/mm/dd conteniendo el valor dateToConvert enviado
     * dd/mm/yy
     *
     * @return la fecha en formato yyyy/mm/dd
     */
    public String getFechaYYYYMMDD(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        String fecha;
        fecha = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fecha += "/";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));

        return fecha;
    }

    /**
     * Retorna un string yyyy-mm-dd conteniendo el valor dateToConvert enviado
     * dd-mm-yy
     *
     * @param dateToConvert
     * @return la fecha en formato yyyy-mm-dd
     */
    public static String getFechaYYYYMMDD_1(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        String fecha;
        fecha = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fecha += "-";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "-";
        fecha += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));

        return fecha;
    }


    /**
     * Retorna un string yyyy-mm-dd HH:MM:SS conteniendo el valor dateToConvert
     *
     * @param dateToConvert
     * @return la fecha en formato yyyy-mm-dd HH:MM:SS
     */
    public static String getFechaYYYYMMDDHHMMSS(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        String fechaHora;
        fechaHora = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fechaHora += "-";
        fechaHora += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fechaHora += "-";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fechaHora += " ";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        fechaHora += ":";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));
        fechaHora += ":";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.SECOND));
        System.out.println("fechaHora=" + fechaHora);
        return fechaHora;
    }

    /**
     * Para la fecha de nacimiento
     *
     * @return campo Date con la fecha 1990-01-01
     */
    public Date getPastDate() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.set(1990, 1, 1);

        return today.getTime();
    }

    /**
     * Retorna la hora del sistema en formato HHmmss
     *
     * @return la hora del sistema en formato HHmmss
     */
    public String getHora() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();

        String hora;
        hora = String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        hora += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));
        hora += String.format("%02d", today.get(java.util.GregorianCalendar.SECOND));

        return hora;
    }

    /**
     * Retorna la hora del sistema en formato HH:mm:ss
     *
     * @return la hora del sistema en formato HH:mm:ss
     */
    public static String getHora_1(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        String hora;
        hora = String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        hora += ":";
        hora += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));
        hora += ":";
        hora += String.format("%02d", today.get(java.util.GregorianCalendar.SECOND));

        return hora;
    }

    /**
     * Retorna la fecha y la hora del sistema en formato yyyy/mm/yy HH:mm:ss
     *
     * @return la fecha y la hora del sistema en formato yyyy/mm/yy HH:mm:ss
     */
    public String getFechaHoraFormateada() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();

        String fechaHora;
        fechaHora = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fechaHora += "/";
        fechaHora += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fechaHora += "/";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fechaHora += " ";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        fechaHora += ":";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));
        fechaHora += ":";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.SECOND));

        return fechaHora;
    }

    /**
     * Retorna la fecha y la hora del sistema en formato yyyymmddHHmmss
     *
     * @return la fecha y hora del sistema en formato yyyymmddHHmmss
     */
    public String getFechaHoraSinFormato() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();

        String fechaHora;
        fechaHora = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fechaHora += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.SECOND));

        return fechaHora;
    }

    /**
     * Retorna la fecha y la hora del sistema en formato yyyymmddHHmmss
     *
     * @return la fecha y hora del sistema en formato yyyymmddHHmmss
     */
    public String getFechaHoraSinFormato(Date laFecha) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(laFecha);

        String fechaHora;
        fechaHora = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fechaHora += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.SECOND));

        return fechaHora;
    }

    /**
     * Retorna la fecha y la hora del sistema en formato yyyymmdd
     * @return la fecha y hora del sistema en formato yyyymmdd
     */
    public String getFechaSinFormato(Date laFecha) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(laFecha);

        String fechaHora;
        fechaHora = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fechaHora += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));

        return fechaHora;
    }

    /**
     * Retorna la fecha dada en formato ddmmyyyy
     * @return la fecha dada en formato ddmmyyyy
     */
    public String getFechaSinFormato_v2(Date laFecha) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(laFecha);

        String fechaHora;
        fechaHora = String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fechaHora += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fechaHora += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fechaHora;
    }

    /**
     * Retorna un string mm/yyyy conteniendo el valor dateToConvert enviado
     * mm/dd/yyyy
     *
     * @return la fecha del sistema en formato mm/yyyy
     */
    public String getFecha_mmyyy(Date laFecha) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(laFecha);

        String fecha;
        fecha = String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fecha;
    }

    public static Date getPrimerDiaDelMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.getActualMinimum(Calendar.DAY_OF_MONTH),
                cal.getMinimum(Calendar.HOUR_OF_DAY),
                cal.getMinimum(Calendar.MINUTE),
                cal.getMinimum(Calendar.SECOND));
        return cal.getTime();
    }

    public static Date getUltimoDiaDelMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.getActualMaximum(Calendar.DAY_OF_MONTH),
                cal.getMaximum(Calendar.HOUR_OF_DAY),
                cal.getMaximum(Calendar.MINUTE),
                cal.getMaximum(Calendar.SECOND));
        return cal.getTime();
    }

    public static Date getUltimoFechaDelMes(java.util.Date dateToConvert) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(dateToConvert);

        // Establece el día al último día del mes
        int ultimoDia = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, ultimoDia);

        // Convertir a java.sql.Date
        return new java.sql.Date(calendar.getTimeInMillis());
    }

    public static Date getFinMesDate(Date fecha) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(fecha);

        Calendar cal = Calendar.getInstance();
        cal.set(today.get(GregorianCalendar.YEAR),
                today.get(GregorianCalendar.MONTH),
                today.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public static Date getInicioMesDate(Date fecha) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(fecha);

        Calendar cal = Calendar.getInstance();
        cal.set(today.get(GregorianCalendar.YEAR),
                today.get(GregorianCalendar.MONTH),
                today.getActualMinimum(GregorianCalendar.DAY_OF_MONTH));
        return cal.getTime();
    }


    public String getMesUltimoDia(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        String fecha;
        fecha = String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fecha += "/";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%02d", today.getActualMaximum(java.util.GregorianCalendar.DAY_OF_MONTH));

        return fecha;
    }

    public int getUltimoDiaDelMes(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        return today.getActualMaximum(java.util.GregorianCalendar.DAY_OF_MONTH);
    }

    public static Date getPrimerDiaDelAnio() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR),
                Calendar.JANUARY,
                cal.getActualMinimum(Calendar.DAY_OF_MONTH),
                cal.getMinimum(Calendar.HOUR_OF_DAY),
                cal.getMinimum(Calendar.MINUTE),
                cal.getMinimum(Calendar.SECOND)
        );
        return cal.getTime();
    }

    /**
     * Retorna la ip local
     *
     * @return la ip local
     */
    public String getLocalIpAddress() {
        String ipAddress = "";
        try {
            InetAddress ownIP = InetAddress.getLocalHost();
            ipAddress = ownIP.getHostAddress();
            //System.out.println("Identificacion de NODO " + ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    /**
     * Rellena una cadena con el caracter de relleno, de una longitud dada y
     * hacia la izquierda o hacia la derecha.
     *
     * @param cadena String con la cadena original
     * @param caracterRelleno char[1] contiene el caracter de relleno
     * @param longitud int largo del relleno o de la cadena final...
     * @param lado int 0=izquierda, 1=derecha
     * @return String nueva cadena de caracteres.
     */
    public String rellenaString(String cadena, char caracterRelleno, int longitud, int lado) {
        String salida = cadena;

        //Si tiene la misma longitud la devuelve
        if (salida.length() == longitud) {
            return salida;
        }
        //Si es mas larga la trunca
        if (salida.length() > longitud) {
            return salida.substring(0, longitud);
        }
        //Si es menor, entonces modificamos
        if (salida.length() < longitud) {
            if (lado == 1) {
                //	Rellenar por la derecha
                for (int k = cadena.length(); k < longitud; k++) {
                    salida = salida + caracterRelleno;
                }
            } else {
                if (lado == 0) {
                    //	Rellenar por la izquierda
                    for (int k = cadena.length(); k < longitud; k++) {
                        salida = caracterRelleno + salida;
                    }
                } else {
                    return "Lado es incorrecto";
                }
            }

            return salida;
        }
        return cadena;
    }

    /**
     * Escribe en la salida standard de JAVA o Tomcat.
     *
     * @param sessionID
     * @param eFace
     * @param textoLog
     */
    public void escribirLog(String sessionID, String eFace, String textoLog) {
        String sNuevoTexto;
        byte[] buf = new byte[1024];
        Date fechaActual;
        SimpleDateFormat formatoFechaHora;
        String fechaHoraActual;

        fechaActual = new Date();
        formatoFechaHora = new SimpleDateFormat("yyyyMMdd_HHmmss");
        fechaHoraActual = formatoFechaHora.format(fechaActual);
        String nuevoTexto = fechaHoraActual + "TellerServerWS v 1.0 (" + sessionID + " " + eFace + ") [" + textoLog + "]";

        System.out.println(nuevoTexto);

        /**
         * if(sMensaje.contains("-2|ERROR") || sMensaje.contains("-1|ERROR") ||
         * sMensaje.contains("INSERT INTO")) { EnviarCorreo enviarCorreo = new
         * EnviarCorreo(sEmailSMTP, sEmailFrom, sEmailFrom, "Error en
         * SwitchTelepin", sNuevoTexto); System.out.println("EMAIL:" +
         * sEmailSMTP + " " + sEmailFrom + " " + sMensaje);
         *
         * enviarCorreo.send(); }
         *
         */
        /**
         * try {
         *
         * MulticastSocket mServer = new MulticastSocket();
         *
         * InetAddress group = InetAddress.getByName("225.4.5.6");
         *
         * DatagramPacket packet;
         *
         * buf = sNuevoTexto.getBytes(); packet = new DatagramPacket(buf,
         * buf.length, group, 4459); * mServer.send(packet);
         *
         * }
         * catch(Exception ex1) { System.out.println("ERROR (EscribirLog(()): "
         * + ex1.getMessage().trim()); } ***
         */
    }

    /**
     * Remueve caracteres especiales newline, carriage return, tab y white
     * space, de la cadena dada.
     *
     * @param toBeEscaped string to escape
     * @return String nueva cadena de carecteres sin los caracteres especiales.
     *
     *
     */
    public static String removeFormattingCharacters(final String toBeEscaped) {
        StringBuilder escapedBuffer = new StringBuilder();
        for (int i = 0; i < toBeEscaped.length(); i++) {
            if ((toBeEscaped.charAt(i) != '\n') && (toBeEscaped.charAt(i) != '\r')
                    && (toBeEscaped.charAt(i) != '\t')) {
                escapedBuffer.append(toBeEscaped.charAt(i));
            }
        }
        String s = escapedBuffer.toString().trim();
        return s;//
        // Strings.replaceSubString(s, "\"", "")
    }

    /**
     * Retorna la fecha en formato dd/mm/yyyy.
     *
     * @param dateToConvert
     * @return String dd/mm/yyyy .
     *
     *
     */
    public static String getFechaDDMMYYYY(Date dateToConvert) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateToConvert);

        String fecha;
        fecha = String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fecha += "/";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fecha;
    }

    /**
     * Retorna la fecha y la hora del sistema en formato dd/mm/yyyy hh:mm
     *
     * @return la fecha y hora del sistema en formato dd/mm/yyyy hh:mm
     */
    public static String getFechaDDMMYYYY_HHMM() {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(new java.util.Date());

        String fechaHora = "";
        fechaHora = String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fechaHora += "/";
        fechaHora += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fechaHora += "/";
        fechaHora += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fechaHora += " ";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        fechaHora += ":";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));

        return fechaHora;
    }

    /**
     * Retorna la fecha y la hora del sistema en formato dd/mm/yyyy hh:mm
     *
     * @param dateTime
     * @return la fecha y hora del sistema en formato dd/mm/yyyy hh:mm
     */
    public static String getFechaDDMMYYYY_HHMM_2(Date dateTime) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateTime);

        String fechaHora = "";
        fechaHora = String.format("%02d", today.get(java.util.GregorianCalendar.DAY_OF_MONTH));
        fechaHora += "/";
        fechaHora += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fechaHora += "/";
        fechaHora += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fechaHora += " ";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.HOUR_OF_DAY));
        fechaHora += ":";
        fechaHora += String.format("%02d", today.get(java.util.GregorianCalendar.MINUTE));

        return fechaHora;
    }

    /**
     * Retorna la fecha y la hora del sistema en formato dd/mm/yyyy hh:mm:ss
     *
     * @param dateTime
     * @return la fecha y hora del sistema en formato dd/mm/yyyy hh:mm
     */
    public static String getFechaDDMMYYYY_HHMM_SS(Date dateTime) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateTime);

        String fechaHora = "";
        fechaHora += String.format("%04d", today.get(GregorianCalendar.YEAR));
        fechaHora += "-";
        fechaHora += String.format("%02d", (today.get(GregorianCalendar.MONTH) + 1));
        fechaHora += "-";
        fechaHora += String.format("%02d", today.get(GregorianCalendar.DAY_OF_MONTH));
        fechaHora += "T";
        fechaHora += String.format("%02d", today.get(GregorianCalendar.HOUR_OF_DAY));
        fechaHora += ":";
        fechaHora += String.format("%02d", today.get(GregorianCalendar.MINUTE));
        fechaHora += ":";
        fechaHora += String.format("%02d", today.get(GregorianCalendar.SECOND));
        fechaHora += today.toZonedDateTime().getOffset().toString();

        return fechaHora;
    }

    /**
     * Retorna la fecha en formato mm/yyyy
     *
     * @param dateTime fecha a ser convertida
     * @return String la fecha en formato mm/yyyy
     */
    public static String getFechaMMYYYY(Date dateTime) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateTime);

        String fecha = "";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));
        fecha += "/";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fecha;
    }

    public static String getFechaYYYYMM(Date dateTime) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateTime);

        String fecha = "";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));

        return fecha;
    }

    public static String getFechaYYYY(Date dateTime) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateTime);

        String fecha = "";
        fecha += String.format("%04d", today.get(java.util.GregorianCalendar.YEAR));

        return fecha;
    }

    public static String getFechaMM(Date dateTime) {
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(dateTime);

        String fecha = "";
        fecha += String.format("%02d", (today.get(java.util.GregorianCalendar.MONTH) + 1));

        return fecha;
    }

    /**
     *
     * Retorna la fecha en forma DD/MM/YYY
     *
     * @param
     * @return Date mañana
     */
    public static String getFormatoDDMMYYYY(String dato) {

        String fecha = "";
        fecha += dato.substring(6, 8);
        fecha += "/";
        fecha += dato.substring(4, 6);
        fecha += "/";
        fecha += dato.substring(0, 4);

        return fecha;
    }

    /**
     *
     * Retorna la fecha del dia de Hoy
     *
     * @param
     * @return Date Hoy
     */
    public static Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        Calendar.getInstance(Locale.getDefault());
        return cal.getTime();
    }

    /**
     *
     * Retorna la fecha del dia de mañana
     *
     * @param
     * @return Date mañana
     */
    public static Date getTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Calendar.getInstance(Locale.getDefault());
        return cal.getTime();
    }

    /**
     *
     * Retorna la fecha del dia de siguiente de una fecha dada
     *
     * @param
     * @return Date mañana
     */
    public static Date getTomorrow(Date today) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Calendar.getInstance(Locale.getDefault());
        return cal.getTime();
    }

    /**
     *
     * Retorna la fecha del dia anterior de una fecha dada
     *
     * @param
     * @return Date mañana
     */
    public static Date getYesterday(Date today) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Calendar.getInstance(Locale.getDefault());
        return cal.getTime();
    }

    /**
     * Retorna verdadero si la fecha es mayor o igual a hoy
     *
     * @param date la fecha a ser comprobada
     * @return boolean
     */
    public static boolean esMayorIgualHoy(Date date) {
        Calendar laFecha = Calendar.getInstance();
        laFecha.setTime(date);

        Calendar hoy = Calendar.getInstance();

        return (laFecha.after(hoy) || laFecha.equals(hoy));
    }

    /**
     * Agreaga un filtro de Texto a una columna de un Grid
     *
     * @param filterRow fila de Filto
     * @param property propiedad de la columna
     * @param container container del grid
     * @param size largo del input del filtro
     */
    public static void addTextFilter(Grid.HeaderRow filterRow, String property, IndexedContainer container, int size){

        Grid.HeaderCell cell1 = filterRow.getCell(property);

        TextField filterField1 = new TextField();

        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);

        filterField1.setInputPrompt("Filtrar por " + property);
        if(0 < size) {
            filterField1.setColumns(size);
        }else {
            filterField1.setWidth("85%");
        }
        filterField1.addTextChangeListener(change -> {
                    container.removeContainerFilters(property);
                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty()) {
                        container.addContainerFilter(
                                new SimpleStringFilter(property,
                                        change.getText(), true, false));
                    }
                }
        );
        cell1.setComponent(filterField1);
    }

    /**
     * Agreaga un ComboBox de filtro a una columna de un Grid
     *
     * @param filterRow fila de Filto
     * @param property propiedad de la columna
     * @param container container del grid
     */
    public static void addComboFilter(Grid.HeaderRow filterRow, String property, IndexedContainer container) {

        Grid.HeaderCell cell = filterRow.getCell(property);

        ComboBox comboFilter = new ComboBox();
        comboFilter.setInputPrompt("Filtrar por " + property);
        comboFilter.setNullSelectionAllowed(true);
        comboFilter.setTextInputAllowed(false); // Opcional: evita que escriban valores libres
        comboFilter.addStyleName(ValoTheme.COMBOBOX_TINY);
        comboFilter.setWidth("100%");

        // Agrega valores únicos de la columna al ComboBox
        Set<Object> uniqueValues = new HashSet<>();
        for (Object itemId : container.getItemIds()) {
            Object value = container.getContainerProperty(itemId, property).getValue();
            if (value != null && !uniqueValues.contains(value)) {
                uniqueValues.add(value);
                comboFilter.addItem(value);
            }
        }

        // Listener para aplicar el filtro cuando el valor cambia
        comboFilter.addValueChangeListener(event -> {
            container.removeContainerFilters(property);
            Object selected = comboFilter.getValue();
            if (selected != null) {
                container.addContainerFilter(new SimpleStringFilter(property, selected.toString(), true, false));
            }
        });

        // Colocar el ComboBox en la cabecera
        cell.setComponent(comboFilter);
    }

    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    /**
     * Genera el siguiente {@code codigoPartida} de forma atómica para la tupla
     * ({@code idEmpresa}, {@code fecha}, {@code tipo}).
     *
     * <p><b>Formato del código</b>:
     * <pre>{idEmpresa}{YYYYMMDD}{tipo}{correlativo(3 dígitos)}</pre>
     * Ejemplo: {@code 12 20250908 0 001} → {@code 1220250908001}
     * </p>
     *
     * <p><b>Importante</b>:
     * usa la <b>misma Connection</b> para el {@code UPDATE ... LAST_INSERT_ID(...)}
     * y para el {@code SELECT LAST_INSERT_ID()} o el valor no será el esperado.</p>
     *
     * @param conn      Conexión JDBC abierta (no se cierra dentro del método).
     * @param idEmpresa Identificador numérico de la empresa.
     * @param fecha     Fecha tipo Date.
     * @param tipo      Tipo numérico de 1 dígito (rango esperado: 0..9).
     * @return          Código generado con el formato indicado.
     *
     * @throws IllegalArgumentException si la fecha no tiene un formato válido o {@code tipo} no está en 0..9.
     * @throws RuntimeException         si ocurre un error SQL o si el correlativo excede 999.
     */
    public static String nextCodigoPartida(Connection conn, String idEmpresa, Date fecha, int tipo) {
        if (fecha == null) throw new IllegalArgumentException("fecha null");
        validarTipoUnDigito(tipo);

        // Convertimos Date -> LocalDate -> "YYYYMMDD" (para el código y para la tabla CHAR(8))
        LocalDate ld = fecha.toInstant().atZone(ZONE_GT).toLocalDate();
        String fecha8 = ld.format(FMT_BASICO);

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO folio_codigo_partida (IdEmpresa, Fecha, Tipo, Valor) " +
                        "VALUES (?, ?, ?, 1) " +
                        "ON DUPLICATE KEY UPDATE Valor = LAST_INSERT_ID(Valor + 1)");
             PreparedStatement ps2 = conn.prepareStatement("SELECT LAST_INSERT_ID()")) {

            ps.setString(1, idEmpresa);
            ps.setString(2, fecha8);          // <-- Si tu columna es DATE: usa ps.setDate(2, java.sql.Date.valueOf(ld));
            ps.setInt(3, tipo);
            ps.executeUpdate();

            int corr;
            try (ResultSet rs = ps2.executeQuery()) {
                if (!rs.next()) throw new SQLException("LAST_INSERT_ID() vacío");
                corr = rs.getInt(1);
            }
            if (corr > 999) {
                throw new IllegalStateException("Correlativo excede 999 (empresa=" + idEmpresa +
                        ", fecha=" + fecha8 + ", tipo=" + tipo + ", corr=" + corr + " )");
            }

            return String.valueOf(idEmpresa) + fecha8 + tipo + String.format("%03d", corr);

        } catch (SQLException e) {
            throw new RuntimeException("Error SQL generando codigoPartida", e);
        }
    }

    /**
     * Reserva de forma atómica {@code cantidad} correlativos consecutivos para la tupla
     * ({@code idEmpresa}, {@code fecha}, {@code tipo}) y devuelve el arreglo de
     * {@code codigoPartida} ya formateados.
     *
     * <p>Útil cuando necesitas varios códigos seguidos (por ejemplo, Anticipo y Cuota).
     * Internamente, hace un incremento en bloque ({@code Valor += cantidad}) y devuelve
     * los códigos resultantes en orden ascendente.</p>
     *
     * <p><b>Formato del código</b>:
     * <pre>{idEmpresa}{YYYYMMDD}{tipo}{correlativo(3 dígitos)}</pre>
     * </p>
     *
     * <p><b>Notas</b>:
     * <ul>
     *   <li>Debe existir la tabla de folios indicada en la documentación de la clase.</li>
     *   <li>Se requiere usar la <b>misma Connection</b> para el {@code UPDATE ... LAST_INSERT_ID(...)}
     *       y el {@code SELECT LAST_INSERT_ID()}.</li>
     *   <li>Si el último correlativo supera 999 se lanza excepción (el formato actual contempla 3 dígitos).</li>
     * </ul>
     * </p>
     *
     * @param conn      Conexión JDBC abierta (no se cierra dentro del método).
     * @param idEmpresa Identificador numérico de la empresa.
     * @param fecha     Fecha tipo Date.
     * @param tipo      Tipo numérico de 1 dígito (rango esperado: 0..9).
     * @param cantidad  Cantidad de códigos a reservar (mayor o igual que 1).
     * @return          Arreglo con {@code cantidad} códigos en orden ascendente.
     *
     * @throws IllegalArgumentException si la fecha/tipo no son válidos o {@code cantidad} &lt; 1.
     * @throws RuntimeException         si ocurre un error SQL o si el correlativo excede 999.
     *dc
     */
    public static String[] nextCodigosPartida(Connection conn, String idEmpresa, java.util.Date fecha, int tipo, int cantidad) {
        if (fecha == null) throw new IllegalArgumentException("fecha null");
        if (cantidad <= 0) throw new IllegalArgumentException("cantidad debe ser > 0");
        validarTipoUnDigito(tipo);

        LocalDate ld = fecha.toInstant().atZone(ZONE_GT).toLocalDate();
        String fecha8 = ld.format(FMT_BASICO);

        try (PreparedStatement psEnsure = conn.prepareStatement(
                "INSERT INTO folio_codigo_partida (IdEmpresa, Fecha, Tipo, Valor) " +
                        "VALUES (?, ?, ?, 1) " +
                        "ON DUPLICATE KEY UPDATE Valor = LAST_INSERT_ID(Valor + 1)");
             PreparedStatement psBump = conn.prepareStatement(
                     "UPDATE folio_codigo_partida SET Valor = LAST_INSERT_ID(Valor + ?) WHERE IdEmpresa=? AND Fecha=? AND Tipo=?");
             PreparedStatement psGet = conn.prepareStatement("SELECT LAST_INSERT_ID()")) {

            // Crea la fila si no existe
            psEnsure.setString(1, idEmpresa);
            psEnsure.setString(2, fecha8);          // <-- Si tu columna es DATE: usa psEnsure.setDate(2, java.sql.Date.valueOf(ld));
            psEnsure.setInt(3, tipo);
            psEnsure.executeUpdate();

            // Incremento atómico por 'cantidad'
            psBump.setInt(1, cantidad);
            psBump.setString(2, idEmpresa);
            psBump.setString(3, fecha8);            // <-- Si tu columna es DATE: usa psBump.setDate(3, java.sql.Date.valueOf(ld));
            psBump.setInt(4, tipo);
            psBump.executeUpdate();

            int ultimo;
            try (ResultSet rs = psGet.executeQuery()) {
                rs.next(); ultimo = rs.getInt(1);
            }

            if (ultimo > 999) {
                throw new IllegalStateException("Correlativo excede 999 (empresa=" + idEmpresa +
                        ", fecha=" + fecha8 + ", tipo=" + tipo + ")");
            }

            String prefijo = String.valueOf(idEmpresa) + fecha8 + tipo;
            String[] codigos = new String[cantidad];
            for (int i = 0; i < cantidad; i++) {
                int corr = ultimo - (cantidad - 1 - i);
                codigos[i] = prefijo + String.format("%03d", corr);
            }
            return codigos;

        } catch (SQLException e) {
            throw new RuntimeException("Error SQL generando códigos", e);
        }
    }
    // ---------- Helpers ----------
    /** En tu formato actual 'tipo' es 1 dígito (0..9). */
    private static void validarTipoUnDigito(int tipo) {
        if (tipo < 0 || tipo > 9) {
            throw new IllegalArgumentException("Tipo debe ser un dígito 0..9. Valor=" + tipo);
        }
    }

    public static String getUUID_2() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}

