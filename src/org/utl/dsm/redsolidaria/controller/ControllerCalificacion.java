package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import org.utl.dsm.redsolidaria.model.Calificacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerCalificacion {

    private final ConexionMySql conexion = new ConexionMySql();

    public int insertarCalificacion(Calificacion calificacion) throws SQLException {
        String query = "INSERT INTO Calificacion (idTransaccion, idUsuarioCalificador, idUsuarioCalificado, calificacion, comentario, fecha) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = conexion.open();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, calificacion.getIdTransaccion());
            ps.setInt(2, calificacion.getIdUsuarioCalificador());
            ps.setInt(3, calificacion.getIdUsuarioCalificado());
            ps.setInt(4, calificacion.getCalificacion());
            ps.setString(5, calificacion.getComentario());
            ps.setDate(6, Date.valueOf(calificacion.getFecha()));
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idCalificacion = rs.getInt(1);
                    
                    // Actualizar la reputación del usuario calificado
                    actualizarReputacionUsuario(calificacion.getIdUsuarioCalificado(), conn);
                    
                    return idCalificacion;
                }
                return 0;
            }
        }
    }
    
    private void actualizarReputacionUsuario(int idUsuario, Connection conn) throws SQLException {
        String queryPromedio = "SELECT AVG(calificacion) as promedio FROM Calificacion WHERE idUsuarioCalificado = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(queryPromedio)) {
            ps.setInt(1, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    float reputacion = rs.getFloat("promedio");
                    
                    // Actualizar la reputación del usuario
                    String queryUpdate = "UPDATE Usuario SET reputacion = ? WHERE idUsuario = ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(queryUpdate)) {
                        psUpdate.setFloat(1, reputacion);
                        psUpdate.setInt(2, idUsuario);
                        psUpdate.executeUpdate();
                    }
                }
            }
        }
    }
    
    public List<Calificacion> getCalificacionesPorUsuario(int idUsuario) throws SQLException {
        List<Calificacion> calificaciones = new ArrayList<>();
        String query = "SELECT c.*, u.nombre as nombreCalificador " +
                       "FROM Calificacion c " +
                       "JOIN Usuario u ON c.idUsuarioCalificador = u.idUsuario " +
                       "WHERE c.idUsuarioCalificado = ? " +
                       "ORDER BY c.fecha DESC";
        
        try (Connection conn = conexion.open();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Calificacion c = new Calificacion();
                    c.setIdCalificacion(rs.getInt("idCalificacion"));
                    c.setIdTransaccion(rs.getInt("idTransaccion"));
                    c.setIdUsuarioCalificador(rs.getInt("idUsuarioCalificador"));
                    c.setIdUsuarioCalificado(rs.getInt("idUsuarioCalificado"));
                    c.setCalificacion(rs.getInt("calificacion"));
                    c.setComentario(rs.getString("comentario"));
                    Date sqlDate = rs.getDate("fecha");
                    if (sqlDate != null) {
                        c.setFecha(sqlDate.toLocalDate());
                    }
                    c.setNombreCalificador(rs.getString("nombreCalificador"));
                    
                    calificaciones.add(c);
                }
            }
        }
        
        return calificaciones;
    }
}