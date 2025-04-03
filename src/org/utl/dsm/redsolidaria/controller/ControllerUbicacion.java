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

    public List<Estado> getTodosEstados() throws SQLException {
        String query = "SELECT * FROM Estado";
        List<Estado> estados = new ArrayList<>();
        ConexionMySql conexionMySql = new ConexionMySql();
        
        try (Connection conn = conexionMySql.open();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
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
        ConexionMySql conexionMySql = new ConexionMySql();
        
        try (Connection conn = conexionMySql.open();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, idEstado);
            try (ResultSet rs = ps.executeQuery()) {
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
}