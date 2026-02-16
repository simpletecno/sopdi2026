
package com.simpletecno.sopdi;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.utilerias.Utileria;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.sql.DataSource;

/**
 * Clase para manejar la conectividad a base de datos.
 * Lee variables de ambiente y recursos del archivo context.xml
 * @author jaguirre 
 **/
public class MyDatabaseProvider {
    private Connection currentConnection = null;
    public Utileria utileria = null;
    private EnvironmentVars variablesAmbiente = null;
    public String DtePath = "";

    public MyDatabaseProvider() {
        utileria = new Utileria();
        variablesAmbiente = new EnvironmentVars();
    }

    /**
     * Retorna el objeto Connection, que es una nueva conexion a base de datos.
     * @return currentConnection Connection.
     **/
    public Connection getNewConnection() {
        try {

            currentConnection = null;

            if(variablesAmbiente.getDbDataSourceName().equals("MYSQL")) {
               Class.forName("com.mysql.cj.jdbc.Driver").newInstance();  // desarrollo
               //Class.forName("com.mysql.jdbc.Driver").newInstance(); //produccion
            }
            if(variablesAmbiente.getDbDataSourceName().equals("MSSQL")) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            }
            
            currentConnection = DriverManager.getConnection(
                    (String)variablesAmbiente.getDB_URL(),
                    (String)variablesAmbiente.getDB_USERNAME(),
                    (String)variablesAmbiente.getDB_PASSWORD());
            
/***            
            DataSource ds = getDBDataSource();

            if (ds == null) {
                utileria.escribirLog("N/A", "", "\n\nConnectionBD->getConnection()...error al obtener el datasource.....\n\n");
                return null;
            }

            currentConnection = ds.getConnection();

            if (currentConnection == null){
                utileria.escribirLog("N/A", "", "\n\nConnectionBD->getConnection()...error al obtener el datasource.....\n\n");
            }
***/ 

        } catch (Exception ex) {
            utileria.escribirLog("N/A", "", "\n\nConnectionBD->getConnection()..Error..: " + ex.getMessage());
            ex.printStackTrace();
        }
        return currentConnection;
    }

    /**
     * Lee la variable de ambiente DBDATASOURCE contenida en el archivo web.xml,
     * para determinar que manejador de base de datos se usarara.
     * Luego lee el contexto su-resources.xml para extraer el recurso MYSQLDS o MSSQLDS, segun sea para MYSQL o para MS SQL SERVER
     * @return DataSource ds
     **/
    private DataSource getDBDataSource() {
        DataSource ds = null;

        try {
            
            if(variablesAmbiente.getDbDataSourceName().equals("MYSQL"))  {  // MySQL
                //Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                ds = (DataSource) variablesAmbiente.getCurrentContext().lookup("java:comp/env/MYSQLDS");
            }

            if(variablesAmbiente.getDbDataSourceName().equals("MSSQL")) {   // Microsoft SQLSERVER
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
                ds = (DataSource) variablesAmbiente.getCurrentContext().lookup("java:comp/env/MSSQLDS");
            }
            if(variablesAmbiente.getDbDataSourceName().equals("ORACLE")) {  // ORACLE
                Class.forName ("oracle.jdbc.driver.OracleDriver").newInstance();
                ds = (DataSource) variablesAmbiente.getCurrentContext().lookup("java:comp/env/ORACLEDS");
            }
            if(variablesAmbiente.getDbDataSourceName().equals("SQLITE")) {  // ORACLE
                Class.forName ("org.sqlite.JDBC").newInstance();
                ds = (DataSource) variablesAmbiente.getCurrentContext().lookup("java:comp/env/SQLITEDS");
            } 
            
        }
        catch(Exception cnfE) {
            utileria.escribirLog("N/A", "", "\n\nConnectionBD->getDBDataSource()...Error, no se pudo instanciar la clase..: " + cnfE.getMessage());
        }

        return ds;
    }
    
    /**
     * Lee la variable de ambiente DBDATASOURCE contenida en el archivo web.xml,
     * para determinar que manejador de base de datos se usarara.
     * @return String DBDATASOURCE
     */
    public String getUsedDBDataSource() {
        String usedDataSource = null;

        try {

            if(variablesAmbiente.getDbDataSourceName().equals("MYSQL")) { // MySQL
                usedDataSource = "MYSQL";
            }

            if(variablesAmbiente.getDbDataSourceName().equals("MSSQL")) { // Microsoft SQLSERVER
                usedDataSource = "MSSQL";
            }

            if(variablesAmbiente.getDbDataSourceName().equals("ORACLE")) { // ORACLE
                usedDataSource = "ORACLE";
            }
        }
        catch(Exception cnfE) {
            utileria.escribirLog("N/A", "", "\n\nConnectionBD->getUsedDBDataSource()...Error, no se pudo instanciar la clase..: " + cnfE.getMessage());
        }

        return usedDataSource;
    }

    /**
     * Retorna la coneccion actual de base de datos.
     * @return the currentConnection
     **/
    public Connection getCurrentConnection() {
        return currentConnection;
    }
}
