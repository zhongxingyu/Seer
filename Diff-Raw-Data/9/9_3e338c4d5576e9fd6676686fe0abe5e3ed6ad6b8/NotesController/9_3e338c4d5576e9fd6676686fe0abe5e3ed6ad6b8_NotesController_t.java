 package devoxx.core.controllers;
 
 import devoxx.api.NoteDoneEvent;
 import devoxx.core.db.NotesModel;
 import devoxx.core.db.NotesModel.Notes;
 import devoxx.core.db.NotesModel.Note;
 import devoxx.core.fwk.api.Controller;
 import devoxx.core.util.F;
 import devoxx.core.util.F.Unit;
 import java.sql.Connection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import java.util.List;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.ws.rs.*;
 import org.jboss.weld.environment.osgi.api.events.InterBundleEvent;
 
 @Path("notes")
 public class NotesController implements Controller {
     
     @Inject Event<InterBundleEvent> evt;
     
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public List<Note> notes() {
         final List<Note> notes = new ArrayList<Note>();
         NotesModel.DB.withConnection(new F.Function<Connection, F.Unit>() {
             @Override
             public F.Unit apply(Connection _) {
                 notes.addAll(Notes._.findAll());
                 return F.Unit.unit();
             }
         });
         return notes;
     }
     
     @GET @Path("{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Note getNote(@PathParam("id") final Long id) {
         return NotesModel.DB.withConnection(new F.Function<Connection, Note>() {
             @Override
             public Note apply(Connection _) {
                 return Notes._.findById(id).getOrNull();
             }
         });
     }
     
     @DELETE @Path("{id}")
     public void remNote(@PathParam("id") final Long id) {
         NotesModel.DB.withConnection(new F.Function<Connection, Unit>() {
             @Override
             public Unit apply(Connection _) {
                 Notes._.delete(id);
                 return F.Unit.unit();
             }
         });
     }
     
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     public Note create(@FormParam("title") String title, 
                        @FormParam("content") String content) {
         SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
         final Note note = new Note(System.currentTimeMillis(), format.format(new Date()), title, content, Boolean.FALSE);
         NotesModel.DB.withConnection(new F.Function<Connection, F.Unit>() {
             @Override
             public F.Unit apply(Connection _) {
                 Notes._.insertAll(note);
                 return F.Unit.unit();
             }
         });
         return note;
     }
     
     @POST @Path("{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Note update(@PathParam("id") final Long id, 
                        @FormParam("title") final String title, 
                        @FormParam("content") final String content, 
                        @FormParam("done") final Boolean done) {
         return NotesModel.DB.withConnection(new F.Function<Connection, Note>() {
             @Override
             public Note apply(Connection _) {
                 for (Note note : Notes._.findById(id)) {
                    boolean changed = false;
                     try {
                         note.title = title;
                         note.content = content;
                        changed = (note.done == done);
                         note.done = done;
                         return Notes._.update(note);
                     } finally {
                        if (changed) {
                            evt.fire(new InterBundleEvent(new NoteDoneEvent(note.id, note.date, note.title, note.content), NoteDoneEvent.class));
                        }
                     }
                 }
                 return null;
             }
         });
     }
 }
