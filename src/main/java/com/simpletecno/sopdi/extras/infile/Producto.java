package com.simpletecno.sopdi.extras.infile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que representa un producto o servicio en un DTE.
 * Ahora usa BigDecimal internamente para manejar montos con mayor precisión.
 */
public class Producto {

    // --- Atributos principales ---
    private String nombre;
    private BigDecimal monto;       // Usamos BigDecimal en lugar de Double
    private int cantidad;
    private String comentario;
    private String bienOServicio;   // “B” = Bien, “S” = Servicio (por defecto Bien)
    private Map<Integer, Integer> frases = new HashMap<>(); // <TipoFrase, Escenario>

    // --- Constructores ---
    public Producto() {
        // Constructor vacío necesario para deserialización (JSON/XML)
    }

    /**
     * Constructor que acepta Double o BigDecimal como monto.
     * (Se convierte internamente a BigDecimal para evitar errores de redondeo)
     */
    public Producto(String nombre, Double monto, int cantidad, String comentario, String bienOServicio) {
        this.nombre = nombre;
        this.monto = monto != null ? BigDecimal.valueOf(monto) : BigDecimal.ZERO;
        this.cantidad = cantidad;
        this.comentario = comentario;
        this.bienOServicio = bienOServicio;
    }

    public Producto(String nombre, BigDecimal monto, int cantidad, String comentario, String bienOServicio) {
        this.nombre = nombre;
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.cantidad = cantidad;
        this.comentario = comentario;
        this.bienOServicio = bienOServicio;
    }

    public Producto(String nombre, Double monto, int cantidad, String comentario, String bienOServicio, Map<Integer, Integer> frases) {
        this(nombre, monto, cantidad, comentario, bienOServicio);
        this.frases = frases != null ? frases : new HashMap<>();
    }

    // --- Getters y Setters ---
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getMonto() { return monto; }

    // Permite recibir tanto BigDecimal como Double
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setMonto(Double monto) { this.monto = monto != null ? BigDecimal.valueOf(monto) : BigDecimal.ZERO; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getBienOServicio() { return bienOServicio; }
    public void setBienOServicio(String bienOServicio) { this.bienOServicio = bienOServicio; }

    public Map<Integer, Integer> getFrases() { return frases; }
    public void setFrases(Map<Integer, Integer> frases) { this.frases = frases; }

    // --- Métodos auxiliares ---
    public void agregarFrase(int tipo, int escenario) { frases.put(tipo, escenario); }
    public boolean tieneFrase(int tipo) { return frases.containsKey(tipo); }

    // --------------------------------------------
// FUNCIONES DE COMPARACIÓN DE MONTOS
// --------------------------------------------

    /**
     * Retorna true si el monto es mayor que 0.
     */
    public boolean tieneMontoMayorCero() {
        return monto != null && monto.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Retorna true si el monto es igual a 0.
     */
    public boolean tieneMontoCero() {
        return monto != null && monto.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Retorna true si el monto es menor que 0.
     */
    public boolean tieneMontoMenorCero() {
        return monto != null && monto.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Compara el monto con otro BigDecimal.
     * Devuelve:
     *   > 0 si es mayor,
     *   < 0 si es menor,
     *   0 si son iguales.
     */
    public int compararMonto(BigDecimal otroMonto) {
        if (monto == null || otroMonto == null) return 0;
        return monto.compareTo(otroMonto);
    }

    /**
     * Compara el monto con un valor Double.
     */
    public int compararMonto(Double otroMonto) {
        if (monto == null || otroMonto == null) return 0;
        return monto.compareTo(BigDecimal.valueOf(otroMonto));
    }

    /**
     * Retorna true si el monto es mayor que el valor dado.
     */
    public boolean esMayorQue(BigDecimal otroMonto) {
        return monto != null && otroMonto != null && monto.compareTo(otroMonto) > 0;
    }

    public boolean esMayorQue(Double otroMonto) {
        return monto != null && otroMonto != null && monto.compareTo(BigDecimal.valueOf(otroMonto)) > 0;
    }

    /**
     * Retorna true si el monto es menor que el valor dado.
     */
    public boolean esMenorQue(BigDecimal otroMonto) {
        return monto != null && otroMonto != null && monto.compareTo(otroMonto) < 0;
    }

    public boolean esMenorQue(Double otroMonto) {
        return monto != null && otroMonto != null && monto.compareTo(BigDecimal.valueOf(otroMonto)) < 0;
    }

    /**
     * Retorna true si el monto es igual al valor dado (sin importar la escala decimal).
     */
    public boolean esIgualA(BigDecimal otroMonto) {
        return monto != null && otroMonto != null && monto.compareTo(otroMonto) == 0;
    }

    public boolean esIgualA(Double otroMonto) {
        return monto != null && otroMonto != null && monto.compareTo(BigDecimal.valueOf(otroMonto)) == 0;
    }

    /**
     * Compara el monto con el monto de otro Producto.
     */
    public int compararMontoCon(Producto otroProducto) {
        if (otroProducto == null || otroProducto.monto == null) return 0;
        return this.monto.compareTo(otroProducto.monto);
    }


    // --- toString (para depuración) ---
    @Override
    public String toString() {
        return "Producto{" +
                "nombre='" + nombre + '\'' +
                ", monto=" + monto +
                ", cantidad=" + cantidad +
                ", comentario='" + comentario + '\'' +
                ", bienOServicio='" + bienOServicio + '\'' +
                ", frases=" + frases.toString() +
                '}';
    }
}
