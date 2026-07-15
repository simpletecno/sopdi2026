package com.simpletecno.sopdi.api;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Valida el header X-API-Key contra la tabla api_key.
 * Rechaza con 401 si la clave no existe, está inactiva o vencida.
 * Las rutas de Swagger (openapi.json / openapi.yaml) quedan excluidas.
 *
 * Además, resuelve la empresa (IdEmpresa) a la que pertenece la clave y la
 * expone como propiedad del request ({@link #EMPRESA_PROPERTY}) para que los
 * recursos limiten sus operaciones a esa empresa. Una clave con IdEmpresa NULL
 * se considera global (sin restricción de empresa).
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyFilter implements ContainerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    /** Nombre de la propiedad del request que contiene el IdEmpresa de la clave (Long o null). */
    public static final String EMPRESA_PROPERTY = "apiKeyIdEmpresa";

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String path = ctx.getUriInfo().getPath();

        // Permitir el endpoint de Swagger sin autenticación
        if (path.equals("openapi.json") || path.equals("openapi.yaml")) {
            return;
        }

        String apiKey = ctx.getHeaderString(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Falta el header X-API-Key\"}")
                    .type("application/json")
                    .build());
            return;
        }

        KeyInfo info = lookupKey(apiKey);
        if (!info.valid) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"API Key inválida, inactiva o vencida\"}")
                    .type("application/json")
                    .build());
            return;
        }

        // Empresa asociada a la clave (null = clave global sin restricción).
        ctx.setProperty(EMPRESA_PROPERTY, info.idEmpresa);
    }

    /**
     * Busca la clave activa y vigente. Devuelve si es válida y, en tal caso, el
     * IdEmpresa asociado (null cuando la columna es NULL → clave global).
     */
    private KeyInfo lookupKey(String apiKey) {
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT IdEmpresa FROM api_key WHERE KeyValue = ? AND Activo = 1"
                             + " AND (FechaVencimiento IS NULL OR FechaVencimiento >= CURDATE())")) {
            ps.setString(1, apiKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long e = rs.getLong("IdEmpresa");
                    Long idEmpresa = rs.wasNull() ? null : e;
                    return new KeyInfo(true, idEmpresa);
                }
                return new KeyInfo(false, null);
            }
        } catch (Exception e) {
            System.err.println("ApiKeyFilter error: " + e.getMessage());
            return new KeyInfo(false, null);
        }
    }

    /** Resultado de la validación de la clave. */
    private static final class KeyInfo {
        final boolean valid;
        final Long idEmpresa;
        KeyInfo(boolean valid, Long idEmpresa) {
            this.valid = valid;
            this.idEmpresa = idEmpresa;
        }
    }
}
