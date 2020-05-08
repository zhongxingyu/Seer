 package org.example;
 
 import com.googlecode.htmleasy.ViewWith;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.PathParam;
 import org.hibernate.Session;
 
 
 @Path("/question")
 public class QuestionController {
 	
 	@GET
 	@Path("/{questionID}")
	@ViewWith("/soy/questions.questionview")
 	public Question showQuestion(@PathParam("questionID") int questionID) {
 		Session session = SessionFactoryManager.getInstance().openSession();	
 		session.beginTransaction();
		Question q = new Question("hi", null, null);
 		session.save(q);
 		session.getTransaction().commit();
 		session.close();
 
 		session = SessionFactoryManager.getInstance().openSession();
 		session.beginTransaction();
 		Question result = (Question)session.createQuery( 
 				"from Question where id = ?")
 				.setInteger(0, questionID)
 				.uniqueResult();
 		
 		session.getTransaction().commit();
 		session.close();
 		return result;
 	}
 	
 	
 }
