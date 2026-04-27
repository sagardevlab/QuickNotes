package com.sagardevlab.quicknotes.quick_notes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    public record AIResponse(String type, String reply, String content) {}

    public AIResponse chat(String noteHtml, String userMessage) {
        String noteText = (noteHtml != null && !noteHtml.isBlank())
                ? Jsoup.parse(noteHtml).text()
                : "";

        String systemPrompt = """
                You are an AI writing assistant inside a note-taking app called NoteHelper.
                Always respond ONLY with valid JSON — no text outside the JSON object.

                If the user asks to modify, rewrite, improve, fix, translate, summarize, shorten, or change the note:
                {"type":"edit","reply":"<one short sentence describing what you did>","content":"<full rewritten note as plain text>"}

                For any question, explanation, or conversation (not editing the note):
                {"type":"chat","reply":"<your response>"}

                Be concise and friendly. For edits, improve the writing — don't just copy it back.
                """;

        String userContent = noteText.isBlank()
                ? userMessage
                : "Current note:\n\"\"\"\n" + (noteText.length() > 3000 ? noteText.substring(0, 3000) + "…" : noteText)
                  + "\n\"\"\"\n\nUser: " + userMessage;

        String raw = callGroq(systemPrompt, userContent);
        return parseResponse(raw);
    }

    @SuppressWarnings("unchecked")
    private String callGroq(String systemPrompt, String userContent) {
        Map<String, Object> response = restClient.post()
                .uri("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "model", "llama-3.1-8b-instant",
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userContent)
                        ),
                        "max_tokens", 1024,
                        "temperature", 0.4
                ))
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    @SuppressWarnings("unchecked")
    private AIResponse parseResponse(String raw) {
        try {
            int start = raw.indexOf('{');
            int end   = raw.lastIndexOf('}');
            if (start >= 0 && end > start) {
                Map<String, String> map = mapper.readValue(raw.substring(start, end + 1), Map.class);
                return new AIResponse(
                        map.getOrDefault("type", "chat"),
                        map.getOrDefault("reply", "Done!"),
                        map.get("content")
                );
            }
        } catch (Exception ignored) {}
        return new AIResponse("chat", raw.isBlank() ? "Sorry, I couldn't process that." : raw, null);
    }
}
