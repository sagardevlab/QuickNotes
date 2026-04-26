package com.sagardevlab.quicknotes.quick_notes.service;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final RestClient restClient = RestClient.create();

    public List<String> summarize(String html) {
        String text = Jsoup.parse(html).text().trim();
        if (text.isEmpty()) return List.of("Note is empty.");

        String raw = callGroq(text);
        return parseBullets(raw);
    }

    @SuppressWarnings("unchecked")
    private String callGroq(String text) {
        String truncated = text.length() > 3000 ? text.substring(0, 3000) + "…" : text;

        String prompt = """
                Summarize the following note into exactly 5 bullet points.
                Rules:
                - Start each point with a dash (-)
                - Use simple, everyday English — a 10-year-old should understand
                - Each point must be one short sentence, max 15 words
                - Do NOT copy text directly — rephrase everything
                - Cover only the most important ideas

                Note:
                %s
                """.formatted(truncated);

        Map<String, Object> response = restClient.post()
                .uri("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "model", "llama-3.1-8b-instant",
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "max_tokens", 300,
                        "temperature", 0.3
                ))
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private List<String> parseBullets(String raw) {
        return Arrays.stream(raw.split("\n"))
                .map(String::trim)
                .filter(line -> line.startsWith("-"))
                .map(line -> line.replaceFirst("^-+\\s*", ""))
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }
}
