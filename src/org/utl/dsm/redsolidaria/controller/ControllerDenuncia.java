package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Denuncia;
import org.utl.dsm.redsolidaria.model.Usuario;
import org.utl.dsm.redsolidaria.model.Servicio;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerDenuncia {

    private ConexionMySql conexion;

    public ControllerDenuncia() {
        this.conexion = new ConexionMySql();
    }

    public void reportarDenuncia(Denuncia denuncia) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO Denuncia (motivo, descripcion, idUsuarioDenunciante, idUsuarioReportado, idServicio, estatus) VALUES (?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, denuncia.getMotivo());
            ps.setString(2, denuncia.getDescripcion());
            ps.setInt(3, denuncia.getDenunciante() != null ? denuncia.getDenunciante().getIdUsuario() : null);
            ps.setInt(4, denuncia.getReportado() != null ? denuncia.getReportado().getIdUsuario() : null);
            ps.setInt(5, denuncia.getServicio() != null ? denuncia.getServicio().getIdServicio() : null);
            ps.setInt(6, denuncia.getEstatus());
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public List<Denuncia> getTodasDenuncias() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Denuncia> denuncias = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT * FROM Denuncia";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Denuncia denuncia = new Denuncia();
                denuncia.setIdDenuncia(rs.getInt("idDenuncia"));
                denuncia.setMotivo(rs.getString("motivo"));
                denuncia.setDescripcion(rs.getString("descripcion"));
                denuncia.setEstatus(rs.getInt("estatus"));
                denuncias.add(denuncia);
            }
            return denuncias;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }
}