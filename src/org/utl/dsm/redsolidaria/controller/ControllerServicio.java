package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Servicio;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerServicio {

    private final ConexionMySql conexion = new ConexionMySql();

    // Método para agregar un nuevo servicio
    public void agregarServicio(Servicio servicio) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO Servicio (titulo, descripcion, modalidad, estatus, idUsuario) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, servicio.getTitulo());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getModalidad());
            ps.setInt(4, servicio.getEstatus());
            ps.setInt(5, servicio.getIdUsuario()); // Usar el ID de usuario directamente
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al agregar el servicio: " + e.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    // Método para modificar un servicio existente
    public void modificarServicio(Servicio servicio) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "UPDATE Servicio SET titulo = ?, descripcion = ?, modalidad = ?, estatus = ? WHERE idServicio = ? AND idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, servicio.getTitulo());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getModalidad());
            ps.setInt(4, servicio.getEstatus());
            ps.setInt(5, servicio.getIdServicio());
            ps.setInt(6, servicio.getIdUsuario());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el servicio o no tienes permiso para modificarlo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al modificar el servicio: " + e.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    // Método para obtener los servicios de un usuario específico
    public List<Servicio> obtenerMisServicios(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Servicio> servicios = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT idServicio, titulo, descripcion, modalidad, estatus FROM Servicio WHERE idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();
            while (rs.next()) {
                Servicio servicio = new Servicio();
                servicio.setIdServicio(rs.getInt("idServicio"));
                servicio.setTitulo(rs.getString("titulo"));
                servicio.setDescripcion(rs.getString("descripcion"));
                servicio.setModalidad(rs.getInt("modalidad"));
                servicio.setEstatus(rs.getInt("estatus"));
                servicios.add(servicio);
            }
            return servicios;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al obtener los servicios: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
}
