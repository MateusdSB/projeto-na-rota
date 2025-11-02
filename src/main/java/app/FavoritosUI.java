package app;

import dao.FavoritoDAO;
import model.Favorito;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class FavoritosUI extends JFrame {
    private final FavoritoDAO dao = new FavoritoDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Tipo", "Código linha/Parada ","Apelido"},0
    ){
        @Override public boolean isCellEditable(int r, int c){
            return false;
        }

    };
    private final JTable tabela = new JTable(model);

    public FavoritosUI(){
        super("NaRota - Favoritos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700,400);
        setLocationRelativeTo(null);

        JButton btnAdcionar = new JButton("Adicionar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnAtualizar = new JButton("Atualizar");

        btnAdcionar.addActionListener(e -> onAdd());
        btnExcluir.addActionListener(e -> onDelete());
        btnAtualizar.addActionListener(e -> onEdit());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnAdcionar);
        top.add(btnExcluir);
        top.add(btnAtualizar);

        add(top,BorderLayout.NORTH);
        add(new JScrollPane(tabela),BorderLayout.CENTER);
        loadData();
    }

    //Método para atualizar a tabela
    private void loadData(){
        model.setRowCount(0);
        try{
            List<Favorito> lista = dao.listarTodos();
            for(Favorito fav : lista){
                model.addRow(new Object[]{
                        fav.getId(),
                        fav.getType(),
                        fav.getRefId(),
                        fav.getLabel()});
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar: "+ e.getMessage()
            );
        }
    }
    
    //Método para adcionar um favorito na lista
    private void onAdd(){
        //o tipo de favorito(linha ou parada)
        JTextField campoTipo = new JTextField();

        //o ID de referencia da linha ou parada
        JTextField campoRefID = new JTextField();

        //O apelido  que o usuário pode dar aquele favorito
        JTextField campoApelido = new JTextField();

        Object[] formulario = {
            "Tipo (Linha/Parada): ", campoTipo,
            "Ref ID (Código da Linha/Parada): ", campoRefID,
            "Apelido : ", campoApelido
        };

        int opcao =  JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Adicionar Novo favorito",
                JOptionPane.OK_CANCEL_OPTION
        );

        //se o usuario clicar em 'OK'
        if(opcao == JOptionPane.OK_OPTION){

            //pega os textos que o usuario digitou nos campos
            String tipo = campoTipo.getText().trim();
            String refId = campoRefID.getText().trim();
            String apelido = campoApelido.getText().trim();

            //VALIDAÇÃO
            if(tipo.isEmpty() || refId.isEmpty() || apelido.isEmpty()){
                JOptionPane.showMessageDialog(this,
                        "Todos os campos devem ser preenchidos.",
                        "Erro de Validação", JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            tipo = tipo.toUpperCase();
            if(!tipo.equals("LINHA") && !tipo.equals("PARADA")){
                JOptionPane.showMessageDialog(this,"Tipo deve ser LINHA ou PARADA.");
                return;
            }
            try{
                dao.salvar(new Favorito(tipo,refId,apelido));
                JOptionPane.showMessageDialog(this,
                        "Favorito salvo com sucesso!"
                );

                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao salvar favorito: " + e.getMessage()
                );
            }
        }
    }

    //metodo que atualiza um item da tabela de favoritos
    private void onEdit(){
        int linha = tabela.getSelectedRow();
        if(linha < 0){
            JOptionPane.showMessageDialog(this,
                    "Selecione um registro para editar");
            return;
        }

        int id = (int)model.getValueAt(linha, 0);
        String tipoAtual = (String) model.getValueAt(linha, 1);
        String refAtual = (String) model.getValueAt(linha, 2);
        String apelidoAtual = (String) model.getValueAt(linha, 3);


        JTextField campoTipo =  new JTextField(tipoAtual);
        JTextField campoRefID =  new JTextField(refAtual);
        JTextField campoApelido =  new JTextField(apelidoAtual);

        Object[] formulario = {
                "Tipo (Linha/Parada): ", campoTipo,
                "Ref ID (Código da Linha/Parada): ", campoRefID,
                "Apelido : ", campoApelido
        };

        int opcao = JOptionPane.showConfirmDialog(
                this, formulario,
                "Editar Favorito (ID " + id + ")",
                JOptionPane.OK_CANCEL_OPTION
        );

        if(opcao == JOptionPane.OK_OPTION){
            String tipo = campoTipo.getText().trim();
            String refId = campoRefID.getText().trim();
            String apelido = campoApelido.getText().trim();

            if(tipo.isEmpty() || refId.isEmpty() || apelido.isEmpty()){
                JOptionPane.showMessageDialog(this,
                        "Todos os campos devem ser preenchidos.",
                        "Erro de validação",JOptionPane.ERROR_MESSAGE);
                return;
            }

            tipo = tipo.toUpperCase();
            if(!tipo.equals("LINHA") && !tipo.equals("PARADA")){
                JOptionPane.showMessageDialog(this,
                        "Tipo deve ser LINHA ou PARADA");
                return;
            }
            try{
                Favorito fav = new Favorito(tipo,refId,apelido);
                fav.setId(id);

                dao.atualizar(fav);
                JOptionPane.showMessageDialog(this,
                        "Favorito atualizado!");
                loadData();

            }catch (Exception e){
                JOptionPane.showMessageDialog(this,
                        "Erro ao atualizar favorito: " + e.getMessage());
            }
        }

    }

    //Metodo para deletar um item da lista
    private void onDelete(){
        int linha = tabela.getSelectedRow();
        if(linha < 0){
            JOptionPane.showMessageDialog(this,"Selecione um registro");
            return;
        }

        int id = (int) model.getValueAt(linha, 0);

        int confirma = JOptionPane.showConfirmDialog(this,
                "Excluir o favorito, ID " + id + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (confirma == JOptionPane.YES_OPTION){
            try{
                dao.deletar(id);
                loadData();

            }catch (Exception e){

                JOptionPane.showMessageDialog(this,"Erro ao excluir" + e.getMessage());
            }
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FavoritosUI().setVisible(true));
    }

}
