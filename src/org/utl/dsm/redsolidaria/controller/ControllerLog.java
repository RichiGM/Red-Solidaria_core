package org.utl.dsm.redsolidaria.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.codec.digest.DigestUtils;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;

public class ControllerLog {

    public boolean validateUser (String email, String password) {
        String query = "SELECT COUNT(*) FROM usuario WHERE correo = ? AND contrasenia = ? AND estatus = 1";
        ConexionMySql conexionMySql = new ConexionMySql();

        try (Connection conn = conexionMySql.open(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Si el resultado es mayor a 0, el usuario existe
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al validar el usuario: " + e.getMessage());
        }

        return false;
    }

    public String checkUsers(String email) throws Exception {
        String sql = "SELECT * FROM usuario WHERE correo = ?";
        ConexionMySql connMySQL = new ConexionMySql();
        String token = null; // Token almacenado en la BD
        String tokenizer = null; // Token generado
        Date myDate = new Date(); // Fecha actual
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(myDate);
        String sql2 = "";

        try (Connection conn = connMySQL.open(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                token = rs.getString("lastToken"); // Obtener el lastToken desde la BD

                if (token != null) {
                    token = token.trim();
                } else {
                    token = "";  // Evitar NullPointerException
                }

                if (!token.isEmpty()) { // Si ya tiene un hash, solo actualiza la fecha
                    sql2 = "UPDATE usuario SET dateLastToken = ? WHERE correo = ?";
                } else { // Si no tiene token, generarlo
                    String newToken = "RED_SOLIDARIA" + "." + email + "." + fecha;
                    tokenizer = DigestUtils.md5Hex(newToken); // Generar token MD5
                    sql2 = "UPDATE usuario SET lastToken= ?, dateLastToken = ? WHERE correo = ?";
                }

                try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                    if (!token.isEmpty()) {
                        ps.setString(1, fecha);
                        ps.setString(2, email);
                    } else {
                        ps.setString(1, tokenizer);
                        ps.setString(2, fecha);
                        ps.setString(3, email);
                    }
                    ps.executeUpdate();
                }

                return tokenizer != null ? tokenizer : token;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al verificar el usuario: " + e.getMessage());
        }

        return null;
    }

    public void logoutUser (String email) {
        String query = "UPDATE usuario SET lastToken = NULL WHERE correo = ?";
        ConexionMySql conexionMySql = new ConexionMySql();

        try (Connection conn = conexionMySql.open(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al hacer logout del usuario: " + e.getMessage(), e);
        }
    }
}