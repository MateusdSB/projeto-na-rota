package app;
import infra.ResetaBanco;
import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame{
    public MainApp(){
        super("NaRota - Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,560);
        setLocationRelativeTo(null);

        setJMenuBar(criarMenuBar());

        var label = new JLabel("<html><h2>NaRota</h2><p>Use o menu acima para explorar os módulos.</p></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label,BorderLayout.CENTER);
    }

   private JMenuBar criarMenuBar() {
        JMenuBar bar = new JMenuBar();

        //janela de cadastrar os favoritos
        JMenu cadastro = new JMenu("Cadastros");

        JMenuItem favoritos = new JMenuItem("Favoritos");
        favoritos.addActionListener(e ->
           SwingUtilities.invokeLater(() -> {
                FavoritosUI fav = new FavoritosUI();
                fav.setLocationRelativeTo(this);
                fav.setVisible(true);
           })
        );

        cadastro.add(favoritos);

        //janela de conexão com a API
       JMenu api = new JMenu("API");
       JMenuItem buscaLinhas = new JMenuItem("Busca Linhas");
       buscaLinhas.addActionListener(e ->SwingUtilities.invokeLater(()-> new BuscarLinhasUI().setVisible(true)));

       //janela para posições das linhas
       JMenuItem posicoes = new JMenuItem("Posições por Linha(em breve)");
       posicoes.addActionListener(e->
               JOptionPane.showMessageDialog(this,
                       "Será implementado após buscar linhas"));
       api.add(buscaLinhas);
       api.add(posicoes);


       //ferramenta de depuração - reseta a tabela de favoritos
       JMenu dev = new JMenu("Opcoes Avançadas");
       JMenuItem miLimpar = new JMenuItem("Limpar tabela 'favorito' + reset IDs");
       miLimpar.addActionListener(e -> {
           try {
               ResetaBanco.main(new String[]{});
               JOptionPane.showMessageDialog(this, "Banco limpo com sucesso.");
           } catch (Exception ex) {
               JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
           }
       });
       dev.add(miLimpar);

       JMenu sistema = new JMenu("Sistema");
       JMenuItem sair = new JMenuItem("Sair");
       sair.addActionListener(e -> System.exit(0));
       sistema.add(sair);

       bar.add(cadastro);
       bar.add(api);
       bar.add(dev);
       bar.add(sistema);
       return bar;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->new MainApp().setVisible(true));
    }
}
