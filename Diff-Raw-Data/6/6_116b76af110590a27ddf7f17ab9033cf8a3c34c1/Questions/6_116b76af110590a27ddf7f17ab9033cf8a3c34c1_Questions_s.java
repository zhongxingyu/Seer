 package controllers.admin;
 
 import controllers.CRUD;
 import controllers.SecuredCrud;
import models.Interview;
 import models.Question;
 import models.Role;
 import play.mvc.Http;
 import security.Secure;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Mathurin
  * Date: 13/02/12
  * Time: 15:04
  * To change this template use File | Settings | File Templates.
  */
 @Secure(role = Role.TECHNICAL_ADMIN)
 @CRUD.For(Question.class)
 public class Questions extends SecuredCrud {
     public static void save(Long id){
         Question q = Question.findById(id);
         String questionLabel = Http.Request.current().params.get("label");
         String questionDescription = Http.Request.current().params.get("description");
         String questionAnswer = Http.Request.current().params.get("answer");
         validation.valid(q);
         q.label = questionLabel;
         q.description = questionDescription;
         q.answer = questionAnswer;
         q.save();
         flash.success(play.i18n.Messages.get("crud.saved", Question.class.getSimpleName()));
         if (params.get("_save") != null) {
             redirect(request.controller + ".list");
         }
         redirect(request.controller + ".show", id);
     }
 }
