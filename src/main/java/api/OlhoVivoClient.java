package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OlhoVivoClient {
    private static final String BASE = "https://api.olhovivo.sptrans.com.br/v2.1";

    private  final HttpClient http = HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();

    private  final ObjectMapper M = new ObjectMapper();
    private boolean autenticado = false;


    public boolean isAutenticado(){
        return autenticado;
    }

    //método para realizar a autenticação na API
    public boolean autenticar(String token) throws Exception {
        String url = BASE + "/Login/Autenticar?token=" +
                URLEncoder.encode(token.trim(), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body() != null ? response.body().trim() : "";


        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + body);
        }
        autenticado = "true".equalsIgnoreCase(body);
        return autenticado;
    }

    //retorna o JSON com o resultado da busca "/Linha/Buscar?termosBusca="
    public JsonNode buscarLinha(String termosBusca)throws Exception{
        if(!autenticado){
            throw new IllegalStateException("Precisa se autenticar primeiro");
        }

        String url = BASE +"/Linha/Buscar?termosBusca=" +
                URLEncoder.encode(termosBusca, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept","application/json")
                .build();

        HttpResponse<String> response = http.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!= 200){
            throw new RuntimeException("HTTP " + response.statusCode()+": " + response.body());
        }

        return M.readTree(response.body());
    }

    //previsões de chegada linha(onibus) /Previsao/Linha?codigoLinha={cl}
    public JsonNode previsaoPorLinha(String codigoLinha)throws Exception{
        if(!autenticado){
            throw new IllegalStateException("Precisa se autenticar primeiro");
        }

        String url = BASE +"/Previsao/Linha?codigoLinha=" +
                URLEncoder.encode(codigoLinha, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept","application/json")
                .build();

        HttpResponse<String> response = http.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!= 200){
            throw new RuntimeException("HTTP " + response.statusCode()+": " + response.body());
        }

        return M.readTree(response.body());
    }

    //previsões de chegada parada /Previsao/Parada?codigoParada={cl}
    public JsonNode previsaoPorParada(String codigoParada)throws Exception{
        if(!autenticado){
            throw new IllegalStateException("Precisa se autenticar primeiro");
        }

        String url = BASE +"/Previsao/Parada?codigoParada=" +
                URLEncoder.encode(codigoParada, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept","application/json")
                .build();

        HttpResponse<String> response = http.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!= 200){
            throw new RuntimeException("HTTP " + response.statusCode()+": " + response.body());
        }

        return M.readTree(response.body());
    }



}
