package com.simpletecno.sopdi.api;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Crea conexiones JDBC para el contexto de la API REST.
 * Usa las mismas variables de ambiente que el resto de la app (web.xml).
 */
public class DbHelper {

    private DbHelper() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MariaDB no encontrado", e);
        }
        EnvironmentVars env = new EnvironmentVars();
        return DriverManager.getConnection(env.getDB_URL(), env.getDB_USERNAME(), env.getDB_PASSWORD());
    }
}