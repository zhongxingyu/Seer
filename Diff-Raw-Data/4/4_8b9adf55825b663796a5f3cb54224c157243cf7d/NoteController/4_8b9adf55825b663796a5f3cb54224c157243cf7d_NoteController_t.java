 package com.xebia.xke.html5.web;
 
 import java.util.Collection;
 import java.util.List;
 
import com.google.common.base.Strings;
 import com.xebia.xke.html5.model.Note;
 import com.xebia.xke.html5.service.NotesService;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  */
 @Controller
 @RequestMapping( "/notes")
 public class NoteController {
 
     private static final org.slf4j.Logger log = LoggerFactory.getLogger(NoteController.class);
 
     @Autowired
     NotesService service;
     
     @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json")
     public ResponseEntity<Note> save(@PathVariable String id, @RequestBody Note note){
         note.id = id;
         return new ResponseEntity<Note>(service.save(note), HttpStatus.OK);
     }
 
     @RequestMapping(method = RequestMethod.POST, produces = "application/json")
     public ResponseEntity<Note> create(@RequestBody Note note){
         return new ResponseEntity<Note>(service.save(note), HttpStatus.OK);
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
     public ResponseEntity<Boolean> delete(@PathVariable String id){
         return new ResponseEntity<Boolean>(service.delete(id), HttpStatus.OK);
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
     public ResponseEntity<Note> get(@PathVariable String id){
         return new ResponseEntity<Note>(service.getNote(id), HttpStatus.OK);
     }
 
     @RequestMapping( method = RequestMethod.GET, produces = "application/json")
     public ResponseEntity<List<Note>> allNotes(){
         ResponseEntity responseEntity= new ResponseEntity<List<Note>>(service.getAllNotes(), HttpStatus.OK );
         return responseEntity;
     }
 
     @RequestMapping( value= "/search/{searchKey}", method = RequestMethod.GET, produces = "application/json")
     public ResponseEntity<Collection<Note>> search(@PathVariable String searchKey){
        ResponseEntity responseEntity= new ResponseEntity<Collection<Note>>(Strings.isNullOrEmpty(searchKey) ? service.getAllNotes(): service.findNote(searchKey), HttpStatus.OK );
         return responseEntity;
     }
 
 }
