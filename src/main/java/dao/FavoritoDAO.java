package dao;

import infra.Conexao;
import model.Favorito;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class FavoritoDAO {

    public List<Favorito>listarTodos(){
        List<Favorito> lista = new ArrayList<>();
        String sql = "SELECT id, tipo AS type, ref_id, apelido AS label FROM favorito ORDER BY id";
        try (Connection conn = Conexao.conectar();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            while (rs.next()){
                Favorito fav = new Favorito();
                fav.setId(rs.getInt("id"));
                fav.setType(rs.getString("type"));
                fav.setRefId(rs.getString("ref_id"));
                fav.setLabel(rs.getString("label"));
                lista.add(fav);

            }
        }catch (Exception e){

            System.out.println("Erro ao listar favorito: " +e.getMessage());
        }
        return lista;
    }

    public void salvar(Favorito favorito){
        String sql = "INSERT INTO favorito(tipo, ref_id,apelido) VALUES(?,?,?)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

           pstmt.setString(1, favorito.getType());
           pstmt.setString(2, favorito.getRefId());
           pstmt.setString(3, favorito.getLabel());
           pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Erro ao salvar favorito: " +e.getMessage());
        }
    }

    public void atualizar(Favorito favorito){
        String sql = "UPDATE favorito SET tipo = ?, ref_id = ?, apelido = ? WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,favorito.getType());
            pstmt.setString(2,favorito.getRefId());
            pstmt.setString(3,favorito.getLabel());
            pstmt.setInt(4,favorito.getId());

            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erro ao atualizar favorito: " + e.getMessage());
            throw  new RuntimeException(e);
        }
    }



    public void deletar(int id){
        String sql = "DELETE FROM favorito WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1,id);
            pstmt.executeUpdate();

        }catch (Exception e){
            System.out.println("ERRO ao deletar favorito" + e.getMessage());
        }
    }
}
