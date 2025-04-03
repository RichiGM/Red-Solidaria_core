package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Servicio;
import org.utl.dsm.redsolidaria.model.Categoria;
import org.utl.dsm.redsolidaria.model.Usuario;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerServicio {

    private ConexionMySql conexion;

    public ControllerServicio() {
        this.conexion = new ConexionMySql();
    }

    public void publicarServicio(Servicio servicio) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO Servicio (titulo, descripcion, modalidad, estatus, idCategoria, idUsuario) VALUES (?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, servicio.getTitulo());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getModalidad());
            ps.setInt(4, servicio.getEstatus());
            ps.setInt(5, servicio.getCategoria() != null ? servicio.getCategoria().getIdCategoria() : null);
            ps.setInt(6, servicio.getUsuario() != null ? servicio.getUsuario().getIdUsuario() : null);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public Servicio getServicioById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = conexion.open();
            String query = "SELECT * FROM Servicio WHERE idServicio = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                Servicio servicio = new Servicio();
                servicio.setIdServicio(rs.getInt("idServicio"));
                servicio.setTitulo(rs.getString("titulo"));
                servicio.setDescripcion(rs.getString("descripcion"));
                servicio.setModalidad(rs.getInt("modalidad"));
                servicio.setEstatus(rs.getInt("estatus"));
                return servicio;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public void actualizarServicio(Servicio servicio) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "UPDATE Servicio SET titulo = ?, descripcion = ?, modalidad = ?, estatus = ?, idCategoria = ?, idUsuario = ? WHERE idServicio = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, servicio.getTitulo());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getModalidad());
            ps.setInt(4, servicio.getEstatus());
            ps.setInt(5, servicio.getCategoria() != null ? servicio.getCategoria().getIdCategoria() : null);
            ps.setInt(6, servicio.getUsuario() != null ? servicio.getUsuario().getIdUsuario() : null);
            ps.setInt(7, servicio.getIdServicio());
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public void eliminarServicio(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "DELETE FROM Servicio WHERE idServicio = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public List<Servicio> getTodosServicios() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Servicio> servicios = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT * FROM Servicio";
            ps = conn.prepareStatement(query);
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
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }
}