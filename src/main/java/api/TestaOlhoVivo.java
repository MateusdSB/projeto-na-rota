package api;

public class TestaOlhoVivo {
    public static void main(String[] args) {
        try {
            // seu token real
            String token = "2518af06b42628b62d68d5906ed375e7ddc1aeeb39896f3711c7e6b113ee4d5a";

            OlhoVivoClient client = new OlhoVivoClient();
            boolean ok = client.autenticar(token);

            System.out.println("Autenticado: " + ok);

            if (ok) {
                var json = client.buscarLinha("8000");
                System.out.println("Retorno da busca:");
                System.out.println(json.toPrettyString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
