package org.utl.dsm.redsolidaria.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import org.utl.dsm.redsolidaria.model.Ciudad;
import org.utl.dsm.redsolidaria.model.Estado;

public class ControllerUbicacion {

    ConexionMySql conexionMySql = new ConexionMySql();

    public List<Estado> getTodosEstados() throws SQLException {
        String query = "SELECT * FROM Estado";
        List<Estado> estados = new ArrayList<>();

        try ( Connection conn = conexionMySql.open();  PreparedStatement ps = conn.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Estado estado = new Estado();
                estado.setIdEstado(rs.getInt("idEstado"));
                estado.setNombre(rs.getString("nombre"));
                estados.add(estado);
            }
            System.out.println("Estados obtenidos: " + estados.size());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al obtener estados: " + e.getMessage(), e);
        }
        return estados;
    }

    public List<Ciudad> getCiudadesPorEstado(int idEstado) throws SQLException {
        String query = "SELECT * FROM Ciudad WHERE idEstado = ?";
        List<Ciudad> ciudades = new ArrayList<>();

        try ( Connection conn = conexionMySql.open();  PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idEstado);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ciudad ciudad = new Ciudad();
                    ciudad.setIdCiudad(rs.getInt("idCiudad"));
                    ciudad.setNombre(rs.getString("nombre"));
                    ciudad.setIdEstado(rs.getInt("idEstado"));
                    ciudades.add(ciudad);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al obtener ciudades: " + e.getMessage(), e);
        }
        return ciudades;
    }

    public int obtenerEstadoPorCiudad(int idCiudad) {
        int idEstado = -1;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Abrir la conexión a la base de datos
            conn = conexionMySql.open();

            // Consulta para obtener el estado a partir del ID de la ciudad
            String query = "SELECT e.idEstado AS estado FROM Ciudad c JOIN Estado e ON c.idEstado = e.idEstado WHERE c.idCiudad = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idCiudad);

            // Ejecutar la consulta
            rs = ps.executeQuery();

            // Obtener el estado
            if (rs.next()) {
                idEstado = rs.getInt("idEstado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cerrar los recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return idEstado;
    }
}
