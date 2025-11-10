package infra;

import dao.ParadaDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CriaTabela {

    public static void main(String[] args) {
        criarEstrutura();

        // ===== Importar stops.txt se a tabela 'parada' estiver vazia =====
        try {
            if (tabelaParadaVazia()) {
                // 1) se foi passado argumento, usa
                String caminho = (args != null && args.length > 0 && args[0] != null && !args[0].isBlank())
                        ? args[0]
                        : localizarStopsPadrao(); // 2) caso contrário, tenta achar automático

                if (caminho == null) {
                    System.out.println("""
                        A tabela 'parada' está vazia e o arquivo stops.txt não foi encontrado automaticamente.
                        Coloque o arquivo em um destes locais e rode novamente:
                          - stops.txt  (na raiz do projeto)
                          - src/main/resources/stops.txt
                        Ou rode esta classe informando o caminho como argumento.
                        """);
                } else {
                    ParadaDAO dao = new ParadaDAO();
                    int n = dao.importarParadaCSV(caminho);
                    System.out.println("Importado stops.txt: " + n + " paradas. (" + caminho + ")");
                }
            } else {
                System.out.println("Tabela 'parada' já possui dados. Nenhuma importação necessária.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao verificar/importar paradas: " + e.getMessage());
        }
    }

    private static void criarEstrutura() {
        String sqlFavorito = """
                CREATE TABLE IF NOT EXISTS favorito (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo   TEXT NOT NULL,      -- LINHA ou PARADA
                    ref_id TEXT NOT NULL,      -- id da linha/parada vindo da API
                    apelido TEXT,
                    UNIQUE(tipo, ref_id)
                );
                """;

        String sqlParada = """
                CREATE TABLE IF NOT EXISTS parada (
                    parada_id   TEXT PRIMARY KEY,
                    parada_name TEXT NOT NULL,
                    parada_lat  REAL NOT NULL,
                    parada_lon  REAL NOT NULL
                );
                """;

        String idxParadaLat = "CREATE INDEX IF NOT EXISTS idx_parada_lat ON parada(parada_lat)";
        String idxParadaLon = "CREATE INDEX IF NOT EXISTS idx_parada_lon ON parada(parada_lon)";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlFavorito);
                stmt.execute(sqlParada);
                stmt.execute(idxParadaLat);
                stmt.execute(idxParadaLon);
            }
            conn.commit();
            System.out.println("tabela criada/atualizada com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao criar/atualizar tabelas: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private static boolean tabelaParadaVazia() throws Exception {
        String sql = "SELECT 1 FROM parada LIMIT 1";
        try (var conn = Conexao.conectar();
             var ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return !rs.next();
        }
    }

    /** Procura stops.txt em locais padrão do projeto. */
    private static String localizarStopsPadrao() {
        Path p1 = Paths.get("stops.txt"); // raiz do projeto
        if (Files.exists(p1)) return p1.toAbsolutePath().toString();

        Path p2 = Paths.get("src", "main", "resources", "stops.txt");
        if (Files.exists(p2)) return p2.toAbsolutePath().toString();

        return null;
    }
}