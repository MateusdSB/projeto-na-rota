package infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static final String URL ="jdbc:sqlite:narota.db";

    public static Connection conectar(){
        try{
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Erro na conexão com banco de dados: " +e.getMessage());
            //imprime o rastro completo do erro
            e.printStackTrace();
            return null;
        }
    }

}
