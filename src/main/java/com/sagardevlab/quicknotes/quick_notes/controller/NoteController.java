package com.sagardevlab.quicknotes.quick_notes.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.sagardevlab.quicknotes.quick_notes.service.NoteService;

@Controller("/api/v1/")
public class NoteController {
    
    @Autowired
    private NoteService noteService;

    @GetMapping("/")
    public String editorPage(Model model){
        model.addAttribute("note", new Note());
        return "index";
    }

    @GetMapping("/notes")
    public String notesPage(Model model) {
        model.addAttribute("notes", noteService.findAll());
        return "notes";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        model.addAttribute("note", noteService.findById(id));
        return "index";
    }

    @GetMapping("/api/notes")
    @ResponseBody
    public ResponseEntity<List<Note>> getAllNotes() {
        return ResponseEntity.ok(noteService.findAll());
    }

    @GetMapping("/api/notes/{id}")
    @ResponseBody
    public ResponseEntity<Note> getNote(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.findById(id));
    }

    @PostMapping("/api/notes")
    @ResponseBody
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        return ResponseEntity.ok(noteService.save(note));
    }

    @PutMapping("/api/notes/{id}")
    @ResponseBody
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note note) {
        note.setId(id);
        return ResponseEntity.ok(noteService.save(note));
    }

    @DeleteMapping("/api/notes/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
