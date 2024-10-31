package com.alura.literalura.service;

import com.alura.literalura.dto.GutendexResponseDTO;
import com.alura.literalura.dto.BookDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class GutendexClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    String gutendexURL = "https://gutendex.com/books/?search=";

    public GutendexClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public List<BookDTO> searchBooksByTitle(String title) throws Exception {
        String url = gutendexURL + title;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            GutendexResponseDTO gutendexResponse = objectMapper.readValue(response.body(), GutendexResponseDTO.class);
            return gutendexResponse.getResults();
        } else {
            throw new RuntimeException("Error al consultar la API de Gutendex");
        }
    }
}