package app;

import api.OlhoVivoClient;
import com.fasterxml.jackson.databind.JsonNode;
import dao.FavoritoDAO;
import model.Favorito;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BuscarLinhasUI  extends JFrame {
    private final OlhoVivoClient client = new OlhoVivoClient();
    private final FavoritoDAO favDAO = new FavoritoDAO();

    private final JTextField textToken = new JTextField();
    private final JButton botaoAutenticar = new JButton("autenticar");
    private final JTextField textBusca = new JTextField();
    private final JButton botaoBuscar = new JButton("Buscar");
    private final JButton botaoSalvarFavorito = new JButton("Salvar Favorito");

    //desenha a tabela na tela
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Codigo da linha","Letreiro","Terminal(ida)","Terminal(volta)","Sentido","Circular?"},0
    ){
        @Override
        public boolean isCellEditable(int r, int c){return  false;}
    };

    private final  JTable tabela = new JTable(modelo);

    //desenha a tabela onde atualiza a busca das linhas
    public BuscarLinhasUI(){
        super("NaRota - Buscar Linhas (API)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900,520);
        setLocationRelativeTo(null);

        JPanel topoPainel = new JPanel(new GridLayout(2,1,8,8));

        //inserir token e autenticar na API
        JPanel painelAutenticacao = new JPanel(new BorderLayout(8,8));
        painelAutenticacao.add(new JLabel("Token da API OlhoVivo:"), BorderLayout.WEST);
        painelAutenticacao.add(textToken, BorderLayout.CENTER);
        painelAutenticacao.add(botaoAutenticar, BorderLayout.EAST);


        //painel de buscar uma linha
        JPanel painelBuscar = new JPanel(new BorderLayout(8,8));
        painelBuscar.add(new JLabel("Buscar (Código ou nome da linha):"), BorderLayout.WEST);
        painelBuscar.add(textBusca, BorderLayout.CENTER);
        painelBuscar.add(botaoBuscar, BorderLayout.EAST);

        topoPainel.add(painelAutenticacao);
        topoPainel.add(painelBuscar);


        JPanel fundoPainel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botaoSalvarFavorito.setEnabled(false);
        fundoPainel.add(botaoSalvarFavorito);

        add(topoPainel,BorderLayout.NORTH);
        add(new JScrollPane(tabela),BorderLayout.CENTER);
        add(fundoPainel,BorderLayout.SOUTH);

        //layout inicial
        textBusca.setEnabled(false);
        botaoBuscar.setEnabled(false);


        //ações dos botões
        botaoAutenticar.addActionListener(e -> autenticar());
        botaoBuscar.addActionListener(e->buscar());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            boolean selecao = tabela.getSelectedRow() >= 0;
            botaoSalvarFavorito.setEnabled(selecao);
        });
        botaoSalvarFavorito.addActionListener(e ->salvarFavoritoSelecionado());
    }

    //método que autentica na API
    private void autenticar(){
        try{
            String token = textToken.getText().trim();
            if(token.isEmpty()){
                JOptionPane.showMessageDialog(this,"informe o token.");
                return;
            }

            boolean tokenOk = client.autenticar(token);
            if(tokenOk){
                JOptionPane.showMessageDialog(this,"Autenticado com sucesso!");
                textBusca.setEnabled(true);
                botaoBuscar.setEnabled(true);
            }else {
                JOptionPane.showMessageDialog(this, "Token inválido.");
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(this,"Erro ao autenticar: " + e.getMessage());
        }
    }

    //método que realiza a busca
    private void buscar(){
        try{
            String termoBusca = textBusca.getText().trim();
            if(termoBusca.isEmpty()){
                JOptionPane.showMessageDialog(this, "Digite um termo de busca(ex. 8000, Lapa, Paraiso etc ).");
                return;
            }
            JsonNode array = client.buscarLinha(termoBusca);
            modelo.setRowCount(0);
            for(JsonNode linha : array){
                int codigoLinha = linha.path("cl").asInt(-1);
                String letreiroLinha = linha.path("lt").asText("");
                String terminalPrincipal = linha.path("tp").asText("");
                String terminalSecundario = linha.path("ts").asText("");
                int sentidoLinha = linha.path("sl").asInt(-1);
                boolean linhaCircular = linha.path("lc").asBoolean(false);
                modelo.addRow(new Object[]{codigoLinha,letreiroLinha,terminalPrincipal,terminalSecundario,sentidoLinha,linhaCircular});
            }
            if(modelo.getRowCount() == 0){
                JOptionPane.showMessageDialog(this, "Nenhuma linha encontrada.");
            }

        }catch (Exception ex){
            JOptionPane.showMessageDialog(this, "Erro na busca: " + ex.getMessage());
        }
    }

    //método que salva o favorito
    private void salvarFavoritoSelecionado(){
        int linha = tabela.getSelectedRow();
        if(linha < 0) return;

        String refId = String.valueOf(modelo.getValueAt(linha,0));
        String lt = String.valueOf(modelo.getValueAt(linha,1));
        String tp = String.valueOf(modelo.getValueAt(linha,2));
        String apelidoSugerido = lt.isBlank() ? tp : lt;

        String apelido = JOptionPane.showInputDialog(this,
                "apelido para salvar como favorito:",
                apelidoSugerido
        );
        if (apelido == null) return;


        try{
            Favorito favorito = new Favorito("LINHA", refId,apelido);
            favDAO.salvar(favorito);
            JOptionPane.showMessageDialog(this, "Favorito salvo!");
        }catch (Exception ex){
            JOptionPane.showMessageDialog(this, "Erro ao salvar favorito: " + ex.getMessage());
        }
     }

}
