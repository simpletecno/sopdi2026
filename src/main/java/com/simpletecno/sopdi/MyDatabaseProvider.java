
package com.simpletecno.sopdi;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.utilerias.Utileria;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Maneja la conectividad a MariaDB.
 * Lee las variables de conexión desde el archivo context.xml vía EnvironmentVars.
 * @author jaguirre
 **/
public class MyDatabaseProvider {

    private Connection currentConnection = null;
    public Utileria utileria = null;
    private EnvironmentVars variablesAmbiente = null;

    public MyDatabaseProvider() {
        utileria = new Utileria();
        variablesAmbiente = new EnvironmentVars();
    }

    /**
     * Retorna una nueva conexión a MariaDB.
     * @return Connection, o null si falla.
     **/
    public Connection getNewConnection() {
        currentConnection = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            currentConnection = DriverManager.getConnection(
                    variablesAmbiente.getDB_URL(),
                    variablesAmbiente.getDB_USERNAME(),
                    variablesAmbiente.getDB_PASSWORD());
        } catch (Exception ex) {
            utileria.escribirLog("N/A", "", "\nMyDatabaseProvider->getNewConnection() Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return currentConnection;
    }

    /**
     * Cierra la conexión actual si está abierta.
     **/
    public void closeConnection() {
        if (currentConnection != null) {
            try {
                currentConnection.close();
            } catch (Exception ex) {
                utileria.escribirLog("N/A", "", "\nMyDatabaseProvider->closeConnection() Error: " + ex.getMessage());
            } finally {
                currentConnection = null;
            }
        }
    }

    /**
     * Retorna el nombre del datasource configurado en web.xml (DBDATASOURCE).
     * @return String con el nombre del datasource, ej. "MYSQL"
     */
    public String getUsedDBDataSource() {
        return variablesAmbiente.getDbDataSourceName();
    }

    /**
     * Retorna la conexión actual sin crear una nueva.
     * @return currentConnection
     **/
    public Connection getCurrentConnection() {
        return currentConnection;
    }
}
