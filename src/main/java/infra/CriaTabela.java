package infra;

import java.sql.Connection;
import java.sql.SQLOutput;
import java.sql.Statement;

public class CriaTabela {
    public static void main(String[] args) {
        String sql = """
                CREATE TABLE IF NOT EXISTS favorito(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL, --linha ou parada
                    refId TEXT NOT NULL, --id da linha ou parada vinda da API
                    label TEXT
                );          
                """;
        try(Connection conn = Conexao.conectar();
            Statement stmt = conn.createStatement()){

            stmt.execute(sql);
            System.out.println("tabela criada com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao criar tabela" + e.getMessage());
        }


        
    }
}
