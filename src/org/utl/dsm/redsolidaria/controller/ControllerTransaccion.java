package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Transaccion;
import org.utl.dsm.redsolidaria.model.Intercambio;
import org.utl.dsm.redsolidaria.model.Usuario;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerTransaccion {

    private ConexionMySql conexion;

    public ControllerTransaccion() {
        this.conexion = new ConexionMySql();
    }

    public void crearTransaccion(Transaccion transaccion) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO Transaccion (idIntercambio, idUsuarioOferente, idUsuarioSolicitante, horasIntercambiadas, fecha, detalles, verificadoOferente, verificadoSolicitante) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setInt(1, transaccion.getIntercambio() != null ? transaccion.getIntercambio().getIdIntercambio() : null);
            ps.setInt(2, transaccion.getOferente() != null ? transaccion.getOferente().getIdUsuario() : null);
            ps.setInt(3, transaccion.getSolicitante() != null ? transaccion.getSolicitante().getIdUsuario() : null);
            ps.setFloat(4, transaccion.getHorasIntercambiadas());
            ps.setDate(5, transaccion.getFecha() != null ? Date.valueOf(transaccion.getFecha()) : null);
            ps.setString(6, transaccion.getDetalles());
            ps.setBoolean(7, transaccion.isVerificadoOferente());
            ps.setBoolean(8, transaccion.isVerificadoSolicitante());
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public List<Transaccion> getTransaccionesPorUsuario(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Transaccion> transacciones = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT * FROM Transaccion WHERE idUsuarioOferente = ? OR idUsuarioSolicitante = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            rs = ps.executeQuery();
            while (rs.next()) {
                Transaccion transaccion = new Transaccion();
                transaccion.setIdTransaccion(rs.getInt("idTransaccion"));
                transaccion.setHorasIntercambiadas(rs.getFloat("horasIntercambiadas"));
                transaccion.setFecha(rs.getDate("fecha") != null ? rs.getDate("fecha").toLocalDate() : null);
                transaccion.setDetalles(rs.getString("detalles"));
                transacciones.add(transaccion);
            }
            return transacciones;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }
}