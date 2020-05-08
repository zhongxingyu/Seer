 package com.summit.notebook.service;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.bson.types.ObjectId;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Field;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.core.query.Update;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.summit.notebook.domain.Note;
 import com.summit.notebook.domain.Notebook;
 import com.summit.notebook.repository.NotebookRepository;
 
 @Service
 @Transactional
 public class NotebookServiceImpl implements NotebookService {
 
     @Autowired
     NotebookRepository notebookRepository;
 
     @Autowired
     private MongoTemplate mongoTemplate;
 
     public long countAllNotebooks() {
         return notebookRepository.count();
     }
 
     public void deleteNotebook(Notebook notebook) {
         notebookRepository.delete(notebook);
     }
 
     public Notebook findNotebook(BigInteger id) {
         return notebookRepository.findOne(id);
     }
 
     public List<Notebook> findAllNotebooks() {
         return notebookRepository.findAll();
     }
 
     public List<Notebook> findNotebookEntries(int firstResult, int maxResults) {
         return notebookRepository.findAll(
                 new org.springframework.data.domain.PageRequest(firstResult
                         / maxResults, maxResults)).getContent();
     }
 
     public void saveNotebook(Notebook notebook) {
         notebookRepository.save(notebook);
     }
 
     public Notebook updateNotebook(Notebook notebook) {
         return notebookRepository.save(notebook);
     }
 
     @Override
     public void pushNotesToNotebook(BigInteger notebookId, Note note) {
         mongoTemplate.updateFirst(
                 new Query(Criteria.where("id").is(notebookId)),
                 new Update().push("notes", note), Notebook.class);
     }
 
     public List<Note> findAllNotes(BigInteger notebookId) {
         return notebookRepository.findOne(notebookId).getNotes();
     }
 
     public int notesCount(BigInteger notebookId) {
         return findAllNotes(notebookId).size();
     }
 
     public Note findNote(BigInteger notebookId, String noteId) {
         Notebook notebook = findNotebook(notebookId);
         Note requestedNote = null;
         for (Note note : notebook.getNotes()) {
             if (StringUtils.equals(noteId, note.getId())) {
                 requestedNote = note;
                 break;
             }
         }
         return requestedNote;
     }
 
     public void updateNote(BigInteger notebookId, Note note) {
         mongoTemplate.updateFirst(
                 Query.query(Criteria.where("_id").is(new ObjectId(notebookId.toString(16)))
                        .and("notes._id").is(note.getId())),
                 new Update().set("notes.$.title", note.getTitle()).set(
                         "notes.$.text", note.getText()).set("notes.$.tags", note.getTags()), Notebook.class);
     }
 
     public List<Note> findNotes(final BigInteger notebookId, final int start,
             final int end) {
         Query query = Query.query(Criteria.where("id").is(notebookId));
         query.fields().slice("notes", start, end);
         Notebook notebook = mongoTemplate.findOne(query, Notebook.class);
         return notebook.getNotes();
     }
 
     public void removeNoteFromNotebook(BigInteger notebookId, String noteId) {
         Query query = Query.query(Criteria.where("id").is(notebookId));
         Map<String, Object> map = new HashMap<String, Object>();
        map.put("_id", noteId);
         Update update = new Update().pull("notes", map);
         mongoTemplate.updateFirst(query, update, Notebook.class);
     }
 
     @Override
     public List<Notebook> findAllNotebookForUser(String username) {
         Query query = Query.query(Criteria.where("author").is(username));
         Field fields = query.fields();
         fields.include("name");
         fields.include("id");
         List<Notebook> notebooks = mongoTemplate.find(query, Notebook.class);
         return notebooks;
     }
 }
