package dao;


import infra.Conexao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ParadaDAO {
    public int importarParadaCSV(String caminhoArquivo){
        int inseridos = 0;
        String insert = "INSERT OR REPLACE INTO parada(parada_id, parada_name, parada_lat, parada_lon) VALUES (?,?,?,?)";

        try(Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);

            try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo));
                 PreparedStatement ps = conn.prepareStatement(insert)) {


                String header = br.readLine();
                if (header == null) throw new IllegalArgumentException("Arquivo vazio: " + caminhoArquivo);
                char sep = header.indexOf(';') >= 0 ? ';' : ',';


                String[] headCols = csvSplit(header, sep);
                int idxId   = findIndex(headCols, new String[]{"stop_id"});
                int idxName = findIndex(headCols, new String[]{"stop_name","stop_nome","name"});
                int idxLat  = findIndex(headCols, new String[]{"stop_lat","lat","latitude"});
                int idxLon  = findIndex(headCols, new String[]{"stop_lon","lon","lng","longitude"});

                if (idxId < 0 || idxName < 0 || idxLat < 0 || idxLon < 0) {
                    throw new IllegalArgumentException("Cabeçalho inválido: " + header);
                }

                //  Linhas
                String linha;
                while ((linha = br.readLine()) != null) {
                    if (linha.isBlank()) continue;

                    String[] cols = csvSplit(linha, sep);
                    int need = Math.max(Math.max(idxId, idxName), Math.max(idxLat, idxLon));
                    if (cols.length <= need) continue; // linha curta

                    String paradaId   = stripQuotes(cols[idxId]);
                    String paradaName = stripQuotes(cols[idxName]);
                    String sLat = stripQuotes(cols[idxLat]).replace(',', '.');
                    String sLon = stripQuotes(cols[idxLon]).replace(',', '.');

                    if (paradaId.isBlank() || sLat.isBlank() || sLon.isBlank()) continue;

                    double paradaLat, paradaLon;
                    try {
                        paradaLat = Double.parseDouble(sLat);
                        paradaLon = Double.parseDouble(sLon);
                    } catch (NumberFormatException nfe) {
                        continue; // pula linha ruim
                    }

                    ps.setString(1, paradaId);
                    ps.setString(2, paradaName);
                    ps.setDouble(3, paradaLat);
                    ps.setDouble(4, paradaLon);
                    ps.addBatch();
                    inseridos++;
                }

                ps.executeBatch();
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao importar paradas: " + e.getMessage(), e);
        }
        return inseridos;
    }




    private static String[] csvSplit(String line, char sep) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == sep && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String stripQuotes(String s){
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length()-1);
        }
        return s.trim();
    }

    private static int findIndex(String[] headers, String[] candidates) {
        for (int i = 0; i < headers.length; i++) {
            String h = stripQuotes(headers[i]).trim();
            for (String cand : candidates) {
                if (h.equalsIgnoreCase(cand)) return i;
            }
        }
        return -1;
    }

    public  Object[] acharParadaMaisProxima(double lat, double lon, double raioMetros){
        double degLat = raioMetros / 111_320.0;
        double cos = Math.cos(Math.toRadians(lat));
        if (cos < 0.000001) cos = 0.000001;
        double degLon = raioMetros / (111_320.0 * cos);

        double minLat = lat - degLat, maxLat = lat + degLat;
        double minLon = lon - degLon, maxLon = lon  + degLon;

        String sql = """
            SELECT parada_id, parada_name, parada_lat, parada_lon
            FROM parada
            WHERE parada_lat BETWEEN ? AND ?
              AND parada_lon BETWEEN ? AND ?
        """;

        Object[] melhor = null;
        double best = Double.MAX_VALUE;

        try(Connection conn = Conexao.conectar();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setDouble(1, minLat);
            ps.setDouble(2, maxLat);
            ps.setDouble(3, minLon);
            ps.setDouble(4, maxLon);

            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    String id = rs. getString(1);
                    String nome = rs. getString(2);
                    double slat = rs.getDouble(3);
                    double slon = rs.getDouble(4);

                    double d = haversineMeters(lat,lon,slat,slon);
                    if (d < best){
                        best = d;
                        melhor = new Object[]{id, nome, slat,slon, best};
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar parada próxima: " + e.getMessage(), e);
        }
        return  melhor;
    }

    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2){
        double raio = 6_371_000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return raio * c;
    }

}
