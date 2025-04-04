package org.utl.dsm.redsolidaria.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;

public class ControllerRecuperacion {
    
    private final ConexionMySql conexion;
    
    public ControllerRecuperacion() {
        this.conexion = new ConexionMySql();
    }
    
    /**
     * Genera un token de recuperación para el usuario con el correo especificado
     * @param email Correo electrónico del usuario
     * @return El token generado o null si el usuario no existe
     */
    public String generarTokenRecuperacion(String email) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = conexion.open();
            
            // Verificar si el usuario existe
            String queryCheck = "SELECT idUsuario FROM Usuario WHERE correo = ? AND estatus = 1";
            ps = conn.prepareStatement(queryCheck);
            ps.setString(1, email);
            rs = ps.executeQuery();
            
            if (!rs.next()) {
                return null; // Usuario no encontrado o inactivo
            }
            
            // Generar token único
            String token = UUID.randomUUID().toString();
            
            // Calcular fecha de expiración (24 horas desde ahora)
            LocalDateTime expiracion = LocalDateTime.now().plusHours(24);
            String expiracionStr = expiracion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Actualizar usuario con el token
            String queryUpdate = "UPDATE Usuario SET resetToken = ?, resetTokenExpires = ? WHERE correo = ?";
            ps.close();
            ps = conn.prepareStatement(queryUpdate);
            ps.setString(1, token);
            ps.setString(2, expiracionStr);
            ps.setString(3, email);
            ps.executeUpdate();
            
            return token;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Valida si un token de recuperación es válido y no ha expirado
     * @param token Token a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validarToken(String token) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = conexion.open();
            
            String query = "SELECT resetTokenExpires FROM Usuario WHERE resetToken = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, token);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                String expiracionStr = rs.getString("resetTokenExpires");
                if (expiracionStr == null) {
                    return false;
                }
                
                LocalDateTime expiracion = LocalDateTime.parse(expiracionStr, 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                // Verificar si el token ha expirado
                return LocalDateTime.now().isBefore(expiracion);
            }
            
            return false; // Token no encontrado
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Restablece la contraseña del usuario asociado al token
     * @param token Token de recuperación
     * @param nuevaContrasenia Nueva contraseña (sin encriptar)
     * @return true si se restableció correctamente, false en caso contrario
     */
    public boolean restablecerContrasenia(String token, String nuevaContrasenia) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            // Validar que el token sea válido
            if (!validarToken(token)) {
                return false;
            }
            
            conn = conexion.open();
            
            // Actualizar contraseña y limpiar token
            String query = "UPDATE Usuario SET contrasenia = SHA2(?, 256), resetToken = NULL, resetTokenExpires = NULL WHERE resetToken = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, nuevaContrasenia);
            ps.setString(2, token);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Obtiene el correo electrónico asociado a un token de recuperación
     * @param token Token de recuperación
     * @return Correo electrónico o null si el token no es válido
     */
    public String obtenerEmailPorToken(String token) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = conexion.open();
            
            String query = "SELECT correo FROM Usuario WHERE resetToken = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, token);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("correo");
            }
            
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
}