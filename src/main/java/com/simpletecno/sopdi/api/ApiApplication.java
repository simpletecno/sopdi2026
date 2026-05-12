package com.simpletecno.sopdi.api;

import com.simpletecno.sopdi.api.resource.ContabilidadPartidaResource;
import com.simpletecno.sopdi.api.resource.ProveedorResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Punto de entrada de la API REST (Jersey / JAX-RS).
 * Se registra como servlet en web.xml bajo /api/*.
 */
public class ApiApplication extends ResourceConfig {

    public ApiApplication() {
        // Recursos JAX-RS
        register(ProveedorResource.class);
        register(ContabilidadPartidaResource.class);

        // Serialización JSON con Jackson
        register(JacksonFeature.class);

        // Filtro de seguridad (API Key)
        register(ApiKeyFilter.class);

        // Endpoint OpenAPI: /api/openapi.json  y  /api/openapi.yaml
        register(OpenApiResource.class);

        configureSwagger();
    }

    private void configureSwagger() {
        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key");

        Map<String, SecurityScheme> schemes = new LinkedHashMap<>();
        schemes.put("ApiKeyAuth", apiKeyScheme);

        OpenAPI openApi = new OpenAPI()
                .info(new Info()
                        .title("SOPDI REST API")
                        .version("1.0.0")
                        .description("API REST para Proveedor y Contabilidad Partida. " +
                                "Autenticación mediante header **X-API-Key**."))
                .schemaRequirement("ApiKeyAuth", apiKeyScheme)
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));

        openApi.setComponents(new io.swagger.v3.oas.models.Components()
                .securitySchemes(schemes));

        SwaggerConfiguration config = new SwaggerConfiguration()
                .openAPI(openApi)
                .prettyPrint(true)
                .resourcePackages(Set.of(
                        "com.simpletecno.sopdi.api.resource"));

        try {
            new io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder<>()
                    .application(this)
                    .openApiConfiguration(config)
                    .buildContext(true);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar Swagger", e);
        }
    }
}