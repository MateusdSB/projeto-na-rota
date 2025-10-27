package infra;

import java.sql.Connection;
import java.sql.Statement;

public class ResetaBanco {
    public static void main(String[] args) {
        String sqlDelete = "DELETE FROM favorito;";
        String sqlResetSeq = "DELETE FROM sqlite_sequence WHERE name='favorito';";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlDelete);
            stmt.execute(sqlResetSeq);

            System.out.println("✅ Banco limpo e sequência de IDs reiniciada com sucesso!");
        } catch (Exception e) {
            System.out.println("❌ Erro ao limpar banco: " + e.getMessage());
        }
    }
}
