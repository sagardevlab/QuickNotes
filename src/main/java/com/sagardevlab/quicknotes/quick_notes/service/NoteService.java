package com.sagardevlab.quicknotes.quick_notes.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sagardevlab.quicknotes.quick_notes.model.Note;
import com.sagardevlab.quicknotes.quick_notes.repository.NoteRepository;

@Service
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;

    public List<Note> findAll(String userId){
        return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public Note findById(Long id, String userId){
        return noteRepository.findByIdAndUserId(id,userId).orElseThrow(()-> new RuntimeException("Note not found: " + id));
    }

    public Note save(Note note, String userId){
        note.setUserId(userId);
        return noteRepository.save(note);
    }

    public void delete(Long id, String userId){
        Note note = findById(id, userId);
        noteRepository.delete(note);
    }

}
