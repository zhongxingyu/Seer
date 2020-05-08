 package controllers;
 
 import java.util.*;
 import java.io.File;
 
 import play.mvc.*;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.data.*;
 import play.data.validation.*;
 import play.db.ebean.*;
 import play.Logger;
 
 import securesocial.core.java.SecureSocial;
 import securesocial.core.Identity;
 
 import helpers.UnauthorizedException;
 import views.html.*;
 import views.html.notes.*;
 import models.*;
 
 public class Notes extends Controller {
 
 	static Form<Note> noteForm = Form.form(Note.class);
 	static Form<Comment> commentForm = Form.form(Comment.class);
 	static Form<String> searchForm = Form.form(String.class);
 
 	public static Result index() {
 		return ok(index.render(Note.all(), noteForm, searchForm));
 	}
 
 	public static Result list() {
 		return ok(index.render(Note.all(), noteForm, searchForm));
 	}
 
 	public static Result show(Long id) {
 		return ok(show.render(Note.find.ref(id), noteForm, commentForm, searchForm));
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result newNote() {
 		return ok(new_note.render(noteForm));
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result create() {
 		Form<Note> filledForm = noteForm.bindFromRequest();
 		
 		if(filledForm.hasErrors()) {
 			for(String key : filledForm.errors().keySet()){
 				List<ValidationError> currentError = filledForm.errors().get(key);
 				for(ValidationError error : currentError){
 					flash("error", key + ": " + error.message());
 				}
 			}     
 			return badRequest(new_note.render(filledForm));
 		} else {
             Http.MultipartFormData body = request().body().asMultipartFormData();
             if(body != null){
 	            Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
 
	            S3File s3File = null;
 	            if (uploadFilePart != null) {
 	                s3File = new S3File();
 	                s3File.name = uploadFilePart.getFilename();
 	                s3File.file = uploadFilePart.getFile();
 	                s3File.save();
 	            }
             }
 			
 			Note note = Note.create(filledForm.get(), Form.form().bindFromRequest().get("tagList"), User.currentUser(), s3File);
             flash("info", "Successfully created note!");
 			return redirect(routes.Notes.show(note.id));
 		}
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result edit(Long id) {
 		Note note = Note.find.ref(id);
 		if(note.allows(User.currentUser())) {
 			Form<Note> filledForm = noteForm.fill(note);
 			return ok(edit.render(note, filledForm));
 		} else {
 			flash("error", "You are not authorized to edit this note!");
 			return badRequest(index.render(Note.all(), noteForm, searchForm));
 		}
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result update(Long id) {
 		Form<Note> filledForm = noteForm.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			return badRequest(index.render(Note.all(), filledForm, searchForm));
 		} else {
 			Note.update(id, filledForm.get(), Form.form().bindFromRequest().get("tagList"));
 			flash("info", "Successfully update note!");
 			return redirect(routes.Notes.show(id));
 		}
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result delete(Long id) {
 		Note note = Note.find.ref(id);
 
 		if(note.allows(User.currentUser())) {
 			Note.delete(id);
 			flash("info", "Successfully deleted note!");
 			return redirect(routes.Notes.list());
 		} else {
 			flash("error", "You are not authorized to delete this note!");
 			return badRequest(index.render(Note.all(), noteForm, searchForm));
 		}
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result newComment(Long id) {
 		Form<Comment> filledForm = commentForm.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			return badRequest(show.render(Note.find.ref(id), noteForm, commentForm, searchForm));
 		} else {
 			Comment.create(id, filledForm.get(), User.currentUser());
 			flash("info", "Successfully posted comment!");
 			return redirect(routes.Notes.show(id));
 		}
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result deleteComment(Long id, Long commentId) {
 		Comment comment = Comment.find.ref(commentId);
 		if(comment.allows(User.currentUser())) {
 			Comment.delete(commentId);
 			flash("info", "Successfully deleted comment!");
 			return redirect(routes.Notes.show(id));
 		} else {
 			flash("error", "You are not authorized to delete this comment!");
 			return badRequest(show.render(Note.find.ref(id), noteForm, commentForm, searchForm));
 		}
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result toggleUpVote(Long id) {
 		Note.find.ref(id).toggleUpVote(User.currentUser());
 		return redirect(routes.Notes.show(id));
 	}
 	
 	@SecureSocial.SecuredAction
 	public static Result toggleDownVote(Long id) {
 		Note.find.ref(id).toggleDownVote(User.currentUser());
 		return redirect(routes.Notes.show(id));
 	}
 }
