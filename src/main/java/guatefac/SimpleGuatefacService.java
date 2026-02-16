package guatefac;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleGuatefacService {

    private static final QName SERVICE_NAME = new QName("http://dbguatefac/Guatefac.wsdl", "Guatefac");
    private String url;

    public SimpleGuatefacService(){
        //url = "https://pdte.guatefacturas.com:443/webservices63/felprima/Guatefac?wsdl"; // TEMPORAL, CABIAR FUTURO | Coneccion a Produccion
        url = "https://dte.guatefacturas.com/webservices63/feltestSB/Guatefac"; // TEMPORAL, CABIAR FUTURO | Coneccion a Prueba

    }

    public SimpleGuatefacService(String url){
        this.url = url; // TEMPORAL, CABIAR FUTURO
    }

    /**
     * Creates a Guatefac service instance with HTTP Basic Authentication.
     *
     * @param username The username for authentication.
     * @param password The password for authentication.
     * @return The Guatefac service instance.
     * @throws Exception if an error occurs during initialization.
     */
    public Guatefac createService(String username, String password) throws Exception {

        return createService(username, password, url);
    }

    /**
     * Creates a Guatefac service instance with HTTP Basic Authentication.
     *
     * @param username The username for authentication.
     * @param password The password for authentication.
     * @param url The Url ofthe webservice.
     * @return The Guatefac service instance.
     * @throws Exception if an error occurs during initialization.
     */
    public Guatefac createService(String username, String password, String url) throws Exception {

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        });

        // Create the service instance
        URL wsdlLocation = new URL(url);
        Guatefac_Service service = new Guatefac_Service(wsdlLocation, SERVICE_NAME);

        // Get the port from the service
        Guatefac port = service.getGuatefacPort();

        // Set HTTP Basic Authentication headers
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        // Create Basic Authentication header
        String authString = username + ":" + password;
        String authEncBytes = Base64.getEncoder().encodeToString(authString.getBytes());
        String authHeader = "Basic " + authEncBytes;

        // Set the HTTP headers directly in the request context
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", Arrays.asList(authHeader));

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            requestContext.put("javax.xml.ws.http.request.headers." + entry.getKey(), entry.getValue());
        }

        return port;
    }

    public String getUrl(){
        return url;
    }
}