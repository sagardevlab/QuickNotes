package com.sagardevlab.quicknotes.quick_notes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sagardevlab.quicknotes.quick_notes.model.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByUpdatedAtDesc(String userId);

    Optional<Note> findByIdAndUserId(Long id, String userId);
}
