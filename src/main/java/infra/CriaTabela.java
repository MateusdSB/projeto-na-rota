package infra;

import java.sql.Connection;
import java.sql.Statement;

public class CriaTabela {
    public static void main(String[] args) {
        String sql = """
                CREATE TABLE IF NOT EXISTS favorito(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT NOT NULL, --linha ou parada
                    ref_id TEXT NOT NULL, --id da linha ou parada vinda da API
                    apelido TEXT,
                    UNIQUE(tipo,ref_id) 
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
