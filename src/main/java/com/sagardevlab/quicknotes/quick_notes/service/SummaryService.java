package com.sagardevlab.quicknotes.quick_notes.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private static final Set<String> STOP_WORDS = Set.of(
        "the","a","an","and","or","but","in","on","at","to","for","of","with",
        "is","are","was","were","be","been","being","have","has","had","do","does",
        "did","will","would","could","should","may","might","this","that","these",
        "those","it","its","i","you","he","she","we","they","not","from","by","as"
    );

    public List<String> summarize(String html) {
        String text = Jsoup.parse(html).text().trim();
        if (text.isEmpty()) return List.of("Note is empty.");

        // Split into sentences
        String[] raw = text.split("(?<=[.!?])\\s+");
        List<String> sentences = Arrays.stream(raw)
                .map(String::trim)
                .filter(s -> s.split("\\s+").length >= 4)
                .collect(Collectors.toList());

        if (sentences.size() <= 5) {
            return sentences.isEmpty() ? List.of(text) : sentences;
        }

        // Word frequency (excluding stop words)
        Map<String, Integer> freq = new HashMap<>();
        for (String s : sentences) {
            for (String w : s.toLowerCase().split("[^a-zA-Z0-9]+")) {
                if (w.length() > 3 && !STOP_WORDS.contains(w)) {
                    freq.merge(w, 1, Integer::sum);
                }
            }
        }

        // Score each sentence
        List<Map.Entry<Integer, String>> scored = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String s = sentences.get(i);
            double score = 0;
            String[] words = s.toLowerCase().split("[^a-zA-Z0-9]+");
            for (String w : words) {
                if (freq.containsKey(w)) score += freq.get(w);
            }
            if (words.length > 0) score /= words.length;
            scored.add(Map.entry(i, s + "|" + score));
        }

        // Pick top 5 by score, preserve original order
        return scored.stream()
                .sorted(Comparator.comparingDouble(e -> -Double.parseDouble(e.getValue().split("\\|")[1])))
                .limit(5)
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(e -> e.getValue().split("\\|")[0])
                .collect(Collectors.toList());
    }
}
