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

    public List<Note> findAll(){
        return noteRepository.findAllByOrderByUpdatedAtDesc();
    }

    public Note findById(Long id){
        return noteRepository.findById(id).orElseThrow(()-> new RuntimeException("Note not found: " + id));
    }

    public Note save(Note note){
        return noteRepository.save(note);
    }

    public void delete(Long id){
        noteRepository.deleteById(id);
    }

}
