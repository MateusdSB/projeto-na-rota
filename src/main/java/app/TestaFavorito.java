package app;

import dao.FavoritoDAO;
import model.Favorito;

public class TestaFavorito {
    public static void main(String[] args) {
        FavoritoDAO dao = new FavoritoDAO();

        System.out.println("Inserindo favoritos");
        dao.salvar(new Favorito("Linha","8000", "casa-trabalho"));
        dao.salvar(new Favorito("parada","340015329","Parada do INSS"));

        System.out.println("listando Favoritos");
        //verificar esse metodo e deixar mais principiante
        dao.listarTodos().forEach(System.out::println);

        System.out.println("deletando id 1...");
        dao.deletar(1);

        System.out.println("Listando após deleção...");
        dao.listarTodos().forEach(System.out::println);
    }
}
