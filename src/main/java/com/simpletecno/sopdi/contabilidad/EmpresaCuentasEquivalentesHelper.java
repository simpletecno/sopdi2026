package com.simpletecno.sopdi.contabilidad;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;

import java.util.*;

/**
 * Helper para gestionar relaciones Nomenclatura-Empresa-Nomenclatura_1
 * y sincronizar opciones en componentes Vaadin (OptionGroup/ComboBox),
 * tolerando IDs mixtos (String/Number) mediante normalización a Long.
 *
 * NOTA: Para remover/restaurar en los componentes se usa SIEMPRE el ID original,
 * pero para comparar seleccionados se usa el ID normalizado.
 */
public class EmpresaCuentasEquivalentesHelper {

    // Si prefieres Integer, cambia normalizeId() y estos generics a Integer.
    private final Map<Long, Map<Long, Set<Long>>> nomenEmpresaNomenMap = new LinkedHashMap<>();
    private final Map<Long, Set<Long>> nomenEmpreMap = new HashMap<>(); // Nomenclatura -> Empresas
    private final Map<Long, Set<Long>> empreNomenMap = new HashMap<>(); // Empresa -> Nomenclaturas_1
    private final Map<Long, Set<Long>> nomenNomenMap = new HashMap<>(); // Nomenclatura -> Nomenclaturas_1

    // Backup de ítems removidos: usa el ID ORIGINAL del contenedor como clave
    private final Map<Object, Map<String, Object>> backupItems = new HashMap<>();

    // ---------- Normalización ----------
    private static Long normalizeId(Object o) {
        if (o == null) throw new IllegalArgumentException("ID nulo");
        if (o instanceof Long)    return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Number)  return ((Number) o).longValue(); // Short, BigDecimal (ojo), etc.
        if (o instanceof String)  return Long.parseLong(((String) o).trim());
        throw new IllegalArgumentException("ID no numérico: " + o + " (" + o.getClass() + ")");
    }

    // ---------- Constructor: canoniza entradas ----------
    public EmpresaCuentasEquivalentesHelper(List<Object[]> relaciones) {
        for (Object[] r : relaciones) {
            Long nomen   = normalizeId(r[0]);
            Long empre   = normalizeId(r[1]);
            Long nomen_1 = normalizeId(r[2]);

            nomenEmpresaNomenMap
                    .computeIfAbsent(nomen, k -> new LinkedHashMap<>())
                    .computeIfAbsent(empre, k -> new HashSet<>())
                    .add(nomen_1);

            nomenEmpreMap
                    .computeIfAbsent(nomen, k -> new HashSet<>())
                    .add(empre);

            empreNomenMap
                    .computeIfAbsent(empre, k -> new HashSet<>())
                    .add(nomen_1);

            nomenNomenMap
                    .computeIfAbsent(nomen, k -> new HashSet<>())
                    .add(nomen_1);
        }
    }

    // ---------- Consultas (normalizan parámetros) ----------
    public Set<Long> getEmpresas(Object nomen) {
        Long key = normalizeId(nomen);
        return nomenEmpreMap.getOrDefault(key, Collections.emptySet());
    }

    public Set<Long> getNomenclaturas_e(Object empre) {
        Long key = normalizeId(empre);
        return empreNomenMap.getOrDefault(key, Collections.emptySet());
    }

    public Set<Long> getNomenclaturas_n(Object nomen) {
        Long key = normalizeId(nomen);
        return nomenNomenMap.getOrDefault(key, Collections.emptySet());
    }

    public Set<Long> getNomenclaturas_en(Object empre, Object nomen) {
        Set<Long> a = new HashSet<>(getNomenclaturas_e(empre));
        a.retainAll(getNomenclaturas_n(nomen));
        return a;
    }

    // ---------- Sincronización de UI (genérico para OptionGroup/ComboBox) ----------
    public void change(OptionGroup opciones, Set<Long> selectedIds) {
        changeGeneric(opciones, selectedIds);
    }

    public void change(ComboBox opciones, Set<Long> selectedIds) {
        changeGeneric(opciones, selectedIds);
    }

    /**
     * Remueve del componente todo ítem cuyo ID NORMALIZADO no esté en selectedIds.
     * - Hace backup con el ID ORIGINAL.
     * - Luego remove con el ID ORIGINAL (evita fallos por tipos distintos).
     */
    @SuppressWarnings("unchecked")
    private void changeGeneric(AbstractSelect opciones, Set<Long> selectedIds) {

        if (opciones == null) throw new IllegalArgumentException("Contenedor nulo");
        if (opciones.getItemIds() == null || opciones.getItemIds().isEmpty()) return;
        if (selectedIds != null && selectedIds.isEmpty()) return;


        Map<Object, Long> index = new LinkedHashMap<>();
        for (Object id : (Collection<Object>) opciones.getItemIds()) {
            index.put(id, normalizeId(id));
        }

        // Seleccionados normalizados (protege null)
        Set<Long> selected = (selectedIds == null ? Collections.<Object>emptySet() : selectedIds)
                .stream()
                .map(EmpresaCuentasEquivalentesHelper::normalizeId)
                .collect(java.util.stream.Collectors.toSet());

        // Determina originales a remover
        List<Object> toRemoveOriginals = new ArrayList<>();
        for (Map.Entry<Object, Long> e : index.entrySet()) {
            if (!selected.contains(e.getValue())) {
                toRemoveOriginals.add(e.getKey());
            }
        }

        // Backup + Remove con ID ORIGINAL
        for (Object originalId : toRemoveOriginals) {
            Item item = opciones.getItem(originalId);
            if (item != null) backupItem(originalId, item);
            opciones.removeItem(originalId);
        }
    }

    // ---------- Backup / Restore ----------
    public void backupItem(Object id, Item item) {
        Map<String, Object> props = new HashMap<>();
        for (Object propId : item.getItemPropertyIds()) {
            Property<?> p = item.getItemProperty(propId);
            props.put(propId.toString(), p != null ? p.getValue() : null);
        }
        backupItems.put(id, props);
    }

    public void restoreAll(OptionGroup opciones) {
        restoreItemsGeneric(opciones, backupItems.keySet());
        backupItems.clear();
    }

    public void restoreAll(ComboBox opciones) {
        restoreItemsGeneric(opciones, backupItems.keySet());
        backupItems.clear();
    }

    public void restoreItems(OptionGroup opciones, Set<Object> ids) {
        restoreItemsGeneric(opciones, ids);
    }

    public void restoreItems(ComboBox opciones, Set<Object> ids) {
        restoreItemsGeneric(opciones, ids);
    }

    @SuppressWarnings("unchecked")
    private void restoreItemsGeneric(AbstractSelect opciones, Set<Object> ids) {
        if (ids == null || ids.isEmpty()) return;

        for (Object id : ids) {
            Map<String, Object> props = backupItems.get(id);
            if (props == null) continue;

            if (opciones.getItem(id) == null) {
                opciones.addItem(id);
            }

            // Restaura solo propiedades existentes en el contenedor
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                Property<Object> property = opciones.getContainerProperty(id, entry.getKey());
                if (property == null) {
                    // Si el contenedor no tiene la propiedad, la omitimos con seguridad.
                    // (Agregar propiedades globales depende del tipo de ContainerDataSource)
                    continue;
                }
                try {
                    // Intento directo
                    property.setValue(entry.getValue());
                } catch (Converter.ConversionException | ClassCastException ex) {
                    // Si hay incompatibilidad de tipos, lo omitimos para no romper la UI.
                    // (Opcional: loggear)
                }
            }
        }
    }

    // ---------- Debug ----------
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EmpresaCuentasEquivalentesHelper:");
        sb.append("\nNomenclatura -> Empresas:");
        nomenEmpreMap.forEach((k, v) -> sb.append("\n ").append(k).append(" -> ").append(v));

        sb.append("\n\nEmpresa -> Nomenclaturas_1:");
        empreNomenMap.forEach((k, v) -> sb.append("\n ").append(k).append(" -> ").append(v));

        sb.append("\n\nNomenclatura -> Nomenclaturas_1:");
        nomenNomenMap.forEach((k, v) -> sb.append("\n ").append(k).append(" -> ").append(v));

        sb.append("\n\nNomenclatura -> Empresa -> Nomenclatura_1:");
        nomenEmpresaNomenMap.forEach((k1, map) -> {
            sb.append("\n ").append(k1).append(" -> {");
            map.forEach((k2, set) -> sb.append("\n   ").append(k2).append(" -> ").append(set));
            sb.append("\n }");
        });

        StringBuilder sb1 = new StringBuilder();

        return sb1.toString();
    }
}
