package com.sagardevlab.quicknotes.quick_notes.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sagardevlab.quicknotes.quick_notes.model.Note;
import com.sagardevlab.quicknotes.quick_notes.service.AIService;
import com.sagardevlab.quicknotes.quick_notes.service.NoteService;

@Controller("/api/v1/")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private AIService aiService;

    private String getUserId(OAuth2AuthenticationToken auth) {
        return auth.getPrincipal().getAttribute("sub");
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String editorPage(Model model) {
        model.addAttribute("note", new Note());
        return "index";
    }

    @GetMapping("/notes")
    public String notesPage(Model model, OAuth2AuthenticationToken auth) {
        model.addAttribute("notes", noteService.findAll(getUserId(auth)));
        return "notes";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model, OAuth2AuthenticationToken auth) {
        model.addAttribute("note", noteService.findById(id, getUserId(auth)));
        return "index";
    }

    @GetMapping("/api/notes")
    @ResponseBody
    public ResponseEntity<List<Note>> getAllNotes(OAuth2AuthenticationToken auth) {
        return ResponseEntity.ok(noteService.findAll(getUserId(auth)));
    }

    @GetMapping("/api/notes/{id}")
    @ResponseBody
    public ResponseEntity<Note> getNote(@PathVariable Long id, OAuth2AuthenticationToken auth) {
        return ResponseEntity.ok(noteService.findById(id, getUserId(auth)));
    }

    @PostMapping("/api/notes")
    @ResponseBody
    public ResponseEntity<Note> createNote(@RequestBody Note note, OAuth2AuthenticationToken auth) {
        return ResponseEntity.ok(noteService.save(note, getUserId(auth)));
    }

    @PutMapping("/api/notes/{id}")
    @ResponseBody
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note note, OAuth2AuthenticationToken auth) {
        note.setId(id);
        return ResponseEntity.ok(noteService.save(note, getUserId(auth)));
    }

    @DeleteMapping("/api/notes/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, OAuth2AuthenticationToken auth) {
        noteService.delete(id, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/ai-chat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aiChat(
            @RequestBody Map<String, String> body,
            OAuth2AuthenticationToken auth) {
        try {
            String message     = body.getOrDefault("message", "");
            String noteContent = body.getOrDefault("noteContent", "");
            AIService.AIResponse result = aiService.chat(noteContent, message);
            Map<String, Object> response = new HashMap<>();
            response.put("type", result.type());
            response.put("reply", result.reply());
            if (result.content() != null) response.put("content", result.content());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI error: " + e.getMessage()));
        }
    }
}
