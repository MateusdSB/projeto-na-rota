package app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import api.OlhoVivoClient;
import com.fasterxml.jackson.databind.JsonNode;
import dao.ParadaDAO;
import app.BuscarLinhasUI;


public class PrevisaoUI extends JFrame {
    private static final ZoneId ZONA_SP = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter HORA_MIN = DateTimeFormatter.ofPattern("HH:mm");

    private final OlhoVivoClient client = MainApp.getClient();
    private final ParadaDAO paradaDAO = new ParadaDAO();

    // Tabela: previsões por LINHA (mostra ônibus chegando em paradas)
    private final DefaultTableModel modeloPorLinha = new DefaultTableModel(
            new Object[]{"Código Parada", "Nome da Parada", "Prefixo", "Previsão", "Acessível?", "Parada Próxima"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabelaPorLinha = new JTable(modeloPorLinha);
    private final JTextField txtCodigoLinha = new JTextField(10);
    private final JButton botaoBuscarLinha = new JButton("Buscar");

    // Tabela: previsões por PARADA (mostra ônibus por linha chegando nessa parada)
    private final DefaultTableModel modeloPorParada = new DefaultTableModel(
            new Object[]{"Código Linha", "Letreiro", "Sentido", "Prefixo", "Previsão", "Acessível?", "Lat", "Lon"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabelaPorParada = new JTable(modeloPorParada);
    private final JTextField txtCodigoParada = new JTextField(10);
    private final JButton botaoBuscarParada = new JButton("Buscar");

    public PrevisaoUI() {
        super("NaRota - Estimativas de Chegada");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 500);

        // Se não estiver autenticado, avisa, abre a tela de login e NÃO exibe esta janela
        if (!client.isAutenticado()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Você ainda não está autenticado. Autenticação necessária.",
                    "Autenticação necessária",
                    JOptionPane.INFORMATION_MESSAGE
            );
            SwingUtilities.invokeLater(() -> {
                BuscarLinhasUI ui = new BuscarLinhasUI();
                ui.setLocationRelativeTo(this);
                ui.setVisible(true);
            });
            dispose();
            return;
        }

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Por Linha", montarAbaPorLinha());
        abas.addTab("Por Parada", montarAbaPorParada());

        JButton botaoRecarregar = new JButton("Recarregar");
        botaoRecarregar.addActionListener(e -> {
            int i = abas.getSelectedIndex();
            if (i == 0) executarBuscarPorLinha();
            else executarBuscaPorParada();
        });

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(botaoRecarregar);

        add(abas, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
    }

    private JPanel montarAbaPorLinha() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Código da Linha (CL): "));
        topo.add(txtCodigoLinha);
        topo.add(botaoBuscarLinha);
        botaoBuscarLinha.addActionListener(e -> executarBuscarPorLinha());

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabelaPorLinha), BorderLayout.CENTER);
        return painel;
    }

    private JPanel montarAbaPorParada() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Código da Parada (CP): "));
        topo.add(txtCodigoParada);
        topo.add(botaoBuscarParada);
        botaoBuscarParada.addActionListener(e -> executarBuscaPorParada());

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabelaPorParada), BorderLayout.CENTER);
        return painel;
    }

    // ===== BUSCA POR LINHA =====
    private void executarBuscarPorLinha() {
        String cl = txtCodigoLinha.getText().trim();
        if (cl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o código da linha (CL numérico).");
            return;
        }
        if (!client.isAutenticado()) {
            JOptionPane.showMessageDialog(this, "Você ainda não está autenticado. Autenticação necessária.");
            SwingUtilities.invokeLater(() -> {
                BuscarLinhasUI ui = new BuscarLinhasUI();
                ui.setLocationRelativeTo(this);
                ui.setVisible(true);
            });
            dispose();
            return;
        }

        limpaTabela(modeloPorLinha);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Void, Void>() {
            private Exception failure;

            @Override
            protected Void doInBackground() {
                try {
                    System.out.println("[API] /Previsao/Linha cl=" + cl);
                    JsonNode raiz = client.previsaoPorLinha(cl);
                    System.out.println(raiz.toPrettyString());

                    // Se vier vazio, avisa amigavelmente e sai
                    if (!raiz.has("ps") || !raiz.get("ps").isArray() || raiz.get("ps").isEmpty()) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(
                                        PrevisaoUI.this,
                                        "Nenhum ônibus em operação para esta linha no momento.\nTente novamente em outro horário.",
                                        "Sem previsões disponíveis",
                                        JOptionPane.INFORMATION_MESSAGE
                                )
                        );
                        return null;
                    }

                    String horaBase = raiz.hasNonNull("hr") ? raiz.get("hr").asText() : null;

                    for (JsonNode parada : raiz.get("ps")) {
                        String codigoParada = parada.path("cp").asText("");
                        String nomeParada   = parada.path("np").asText("");
                        Double pyParada     = parada.hasNonNull("py") ? parada.get("py").asDouble() : null;
                        Double pxParada     = parada.hasNonNull("px") ? parada.get("px").asDouble() : null;

                        if (parada.has("vs") && parada.get("vs").isArray()) {
                            for (JsonNode v : parada.get("vs")) {
                                String prefixo        = v.path("p").asText("");
                                String tempoPrevisto  = v.path("t").asText("");
                                boolean acessivel     = v.path("a").asBoolean(false);
                                Double py             = v.hasNonNull("py") ? v.get("py").asDouble() : pyParada;
                                Double px             = v.hasNonNull("px") ? v.get("px").asDouble() : pxParada;
                                String previstaLocal  = toHoraLocal(tempoPrevisto, horaBase);

                                String local = "";
                                try {
                                    if (py != null && px != null) {
                                        Object[] prox = paradaDAO.acharParadaMaisProxima(py, px, 250);
                                        if (prox != null) {
                                            local = prox[1] + " (aprox. " + Math.round((double) prox[4]) + " m)";
                                        }
                                    }
                                } catch (Exception ignore) {
                                    local = "Erro ao localizar parada";
                                }

                                modeloPorLinha.addRow(new Object[]{
                                        codigoParada, nomeParada, prefixo, previstaLocal, (acessivel ? "Sim" : "Não"), local
                                });
                            }
                        }
                    }

                    if (modeloPorLinha.getRowCount() == 0) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(
                                        PrevisaoUI.this,
                                        "Sem veículos previstos para esta linha agora.",
                                        "Sem dados",
                                        JOptionPane.INFORMATION_MESSAGE
                                )
                        );
                    }
                } catch (Exception ex) {
                    failure = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                if (failure != null) {
                    JOptionPane.showMessageDialog(
                            PrevisaoUI.this,
                            "Erro na consulta de previsão por linha:\n" + failure.getMessage(),
                            "Falha na consulta",
                            JOptionPane.ERROR_MESSAGE
                    );
                    failure.printStackTrace();
                }
            }
        }.execute();
    }

    // ===== BUSCA POR PARADA =====
    private void executarBuscaPorParada() {
        String cp = txtCodigoParada.getText().trim();
        if (cp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o código da parada (CP).");
            return;
        }
        if (!client.isAutenticado()) {
            JOptionPane.showMessageDialog(this, "Você ainda não está autenticado. Autenticação necessária.");
            SwingUtilities.invokeLater(() -> {
                BuscarLinhasUI ui = new BuscarLinhasUI();
                ui.setLocationRelativeTo(this);
                ui.setVisible(true);
            });
            dispose();
            return;
        }

        limpaTabela(modeloPorParada);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Void, Void>() {
            private Exception failure;

            @Override
            protected Void doInBackground() {
                try {
                    System.out.println("[API] /Previsao/Parada cp=" + cp);
                    JsonNode raiz = client.previsaoPorParada(cp);
                    System.out.println(raiz.toPrettyString());

                    if (!raiz.has("l") || !raiz.get("l").isArray() || raiz.get("l").isEmpty()) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(
                                        PrevisaoUI.this,
                                        "Nenhum ônibus em operação para esta parada no momento.\nTente novamente em outro horário.",
                                        "Sem previsões disponíveis",
                                        JOptionPane.INFORMATION_MESSAGE
                                )
                        );
                        return null;
                    }

                    String horaBase = raiz.hasNonNull("hr") ? raiz.get("hr").asText() : null;

                    for (JsonNode linha : raiz.get("l")) {
                        String codigoLinha  = linha.path("cl").asText("");
                        String letreiro     = linha.path("lt").asText("");
                        int sl              = linha.path("sl").asInt(0);
                        String sentido      = (sl == 1 ? "ida" : (sl == 2 ? "volta" : String.valueOf(sl)));

                        if (linha.has("vs") && linha.get("vs").isArray()) {
                            for (JsonNode v : linha.get("vs")) {
                                String prefixo       = v.path("p").asText("");
                                String tempoPrevisto = v.path("t").asText("");
                                boolean acessivel    = v.path("a").asBoolean(false);
                                Double py            = v.hasNonNull("py") ? v.get("py").asDouble() : null;
                                Double px            = v.hasNonNull("px") ? v.get("px").asDouble() : null;
                                String previstaLocal = toHoraLocal(tempoPrevisto, horaBase);

                                modeloPorParada.addRow(new Object[]{
                                        codigoLinha, letreiro, sentido, prefixo, previstaLocal, (acessivel ? "Sim" : "Não"), py, px
                                });
                            }
                        }
                    }

                    if (modeloPorParada.getRowCount() == 0) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(
                                        PrevisaoUI.this,
                                        "Sem veículos previstos para esta parada agora.",
                                        "Sem dados",
                                        JOptionPane.INFORMATION_MESSAGE
                                )
                        );
                    }
                } catch (Exception ex) {
                    failure = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                if (failure != null) {
                    JOptionPane.showMessageDialog(
                            PrevisaoUI.this,
                            "Erro na consulta de previsão por parada:\n" + failure.getMessage(),
                            "Falha na consulta",
                            JOptionPane.ERROR_MESSAGE
                    );
                    failure.printStackTrace();
                }
            }
        }.execute();
    }

    // ===== Utilitários =====
    private static void limpaTabela(DefaultTableModel model) {
        model.setRowCount(0);
    }

    /** Converte "t" (ISO UTC ou "HH:mm") para hora local de SP usando "hr" como base quando necessário. */
    public static String toHoraLocal(String isoUtcOrHour, String hrBaseUtc) {
        if (isoUtcOrHour == null || isoUtcOrHour.isBlank()) return "";
        try {
            // Caso 1: campo "t" veio como ISO UTC
            Instant instant = Instant.parse(isoUtcOrHour);
            return HORA_MIN.format(LocalDateTime.ofInstant(instant, ZONA_SP).toLocalTime());
        } catch (Exception ignore) {
            try {
                // Caso 2: campo "t" veio como "HH:mm" (usa "hr" como base)
                if (hrBaseUtc == null) return isoUtcOrHour;
                LocalTime horaLocal = LocalTime.parse(isoUtcOrHour);
                Instant base = Instant.parse(hrBaseUtc);
                ZonedDateTime zona = ZonedDateTime.ofInstant(base, ZONA_SP)
                        .withHour(horaLocal.getHour()).withMinute(horaLocal.getMinute());
                return HORA_MIN.format(zona.toLocalTime());
            } catch (Exception e) {
                // Se nada der certo, retorna como veio
                return isoUtcOrHour;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrevisaoUI().setVisible(true));
    }
}