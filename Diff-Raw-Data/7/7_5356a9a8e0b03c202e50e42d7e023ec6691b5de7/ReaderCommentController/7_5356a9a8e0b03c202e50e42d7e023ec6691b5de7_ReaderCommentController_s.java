 package controllers;
 
 import models.ReaderComment;
 import play.mvc.Controller;
 
 public class ReaderCommentController extends Controller {
 
     public static void readerComments (String emailAddress, String message){
       
                    
        ReaderComment addcomment = new ReaderComment(emailAddress, message);
        addcomment.save();
         flash.success("Thank you, your comment has been sent to an editor");
        render ("ReaderCommentController/index.html", addcomment);
         
         
     }
         
     }
     
