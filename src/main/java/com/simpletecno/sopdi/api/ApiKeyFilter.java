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
 * Rechaza con 401 si la clave no existe o está inactiva.
 * Las rutas de Swagger (openapi.json / openapi.yaml) quedan excluidas.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyFilter implements ContainerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

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

        if (!isValidKey(apiKey)) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"API Key inválida o inactiva\"}")
                    .type("application/json")
                    .build());
        }
    }

    private boolean isValidKey(String apiKey) {
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM api_key WHERE KeyValue = ? AND Activo = 1")) {
            ps.setString(1, apiKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("ApiKeyFilter error: " + e.getMessage());
            return false;
        }
    }
}