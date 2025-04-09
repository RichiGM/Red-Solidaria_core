package org.utl.dsm.redsolidaria.controller;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import org.utl.dsm.redsolidaria.model.Notificacion;
import org.utl.dsm.redsolidaria.model.Usuario;

/**
 *
 * @author danna
 */


public class ControllerNotificacion {
    
    public List<Notificacion> getAll(int idUsuario) throws Exception {
        String sql = "SELECT * FROM vista_notificacion WHERE idUsuario = ? ORDER BY fecha DESC, idNotificacion DESC";
        ConexionMySql connMySQL = new ConexionMySql();
        Connection conn = connMySQL.open();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idUsuario);
        ResultSet rs = pstmt.executeQuery();
        List<Notificacion> notificaciones = new ArrayList<>();
        
        while (rs.next()) {
            notificaciones.add(fillNotificacion(rs));
        }
        
        rs.close();
        pstmt.close();
        connMySQL.close();
        return notificaciones;
    }
    
    public List<Notificacion> getPendientes(int idUsuario) throws Exception {
        String sql = "SELECT * FROM vista_notificacion WHERE idUsuario = ? AND estatus = 0 ORDER BY fecha DESC, idNotificacion DESC";
        ConexionMySql connMySQL = new ConexionMySql();
        Connection conn = connMySQL.open();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idUsuario);
        ResultSet rs = pstmt.executeQuery();
        List<Notificacion> notificaciones = new ArrayList<>();
        
        while (rs.next()) {
            notificaciones.add(fillNotificacion(rs));
        }
        
        rs.close();
        pstmt.close();
        connMySQL.close();
        return notificaciones;
    }
    
    public boolean marcarLeida(int idNotificacion) throws Exception {
        String sql = "UPDATE notificacion SET estatus = 1 WHERE idNotificacion = ?";
        ConexionMySql connMySQL = new ConexionMySql();
        Connection conn = connMySQL.open();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idNotificacion);
        int result = pstmt.executeUpdate();
        
        pstmt.close();
        connMySQL.close();
        return result > 0;
    }
    
    public boolean marcarTodasLeidas(int idUsuario) throws Exception {
        String sql = "UPDATE notificacion SET estatus = 1 WHERE idUsuario = ?";
        ConexionMySql connMySQL = new ConexionMySql();
        Connection conn = connMySQL.open();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idUsuario);
        int result = pstmt.executeUpdate();
        
        pstmt.close();
        connMySQL.close();
        return result > 0;
    }
    
    private Notificacion fillNotificacion(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setIdNotificacion(rs.getInt("idNotificacion"));
        n.setTipo(rs.getString("tipo"));
        n.setContenido(rs.getString("contenido"));
        n.setFecha(rs.getObject("fecha", LocalDate.class));
        n.setEstatus(rs.getInt("estatus"));
        n.setIdEvento(rs.getInt("idEvento"));
        n.setTipoEvento(rs.getInt("tipoEvento"));
        
        // Crear y llenar el objeto Usuario
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellidos(rs.getString("apellido"));
        u.setCorreo(rs.getString("correo"));
        // Rellenar con otros campos si es necesario
        
        n.setUsuario(u);
        return n;
    }
}