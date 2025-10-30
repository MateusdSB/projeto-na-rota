package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OlhoVivoClient {
    private static final String BASE = "http://api.olhovivo.sptrans.com.br/v2.1";
    private  final HttpClient http = HttpClient.newHttpClient();
    private  final ObjectMapper M = new ObjectMapper();
    private boolean autenticado = false;


    public boolean isAutenticado(){
        return autenticado;
    }
    //método para realizar a autenticação na API
    public boolean autenticar(String token)throws Exception{
        String url = BASE + "/Login/Autenticar?token=" +
                URLEncoder.encode(token, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Accept","application/json")
                .build();

        HttpResponse<String> response = http.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!= 200){
            throw new RuntimeException("HTTP " + response.statusCode()+": " + response.body());
        }
        String body = response.body().trim();
        autenticado = "true".equalsIgnoreCase(body);
        return autenticado;
    }

    //retorna o JSON com o resultado da busca
    public JsonNode buscarLinha(String termosBusca)throws Exception{
        if(autenticado)throw new IllegalStateException("Precisa se autenticar primeiro");
        String url = BASE +"/Linha/Buscar?termosBusca=" +
                URLEncoder.encode(termosBusca, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Accept","application/json")
                .build();

        HttpResponse<String> response = http.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!= 200){
            throw new RuntimeException("HTTP " + response.statusCode()+": " + response.body());
        }
        return M.readTree(response.body());
    }
}
