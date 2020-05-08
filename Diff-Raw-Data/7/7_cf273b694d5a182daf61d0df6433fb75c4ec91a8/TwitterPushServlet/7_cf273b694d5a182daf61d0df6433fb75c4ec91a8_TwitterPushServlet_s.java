 package com.sudhakar.notesoneffectivejava;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterFactory;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.sudhakar.notesoneffectivejava.model.Note;
 import com.sudhakar.notesoneffectivejava.model.TwiJavaNotes;
 
 
 
 /**
  * The Class TwitterPushServlet.
  *
  *	- tweet java notes to Twitter
  *
  * @author Sudhakar Duraiswamy
  */
 public class TwitterPushServlet  extends HttpServlet {
 	
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = 8696297746927565032L;
 	
 	/** The Constant logger. */
 	final static Logger logger = Logger.getLogger(TwitterPushServlet.class.getName());
 	
 	/* (non-Javadoc)
 	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 /*		if (req.getHeader("X-AppEngine-Cron") == null) {
 			return;
 		}*/
 		
 		logger.log(Level.INFO, "Starting the scheduled task of sending out Java Notes");
 		
 		
 		Twitter  twitter = TwitterFactory.getSingleton();
 		
 		try {
 			
 			Key key = KeyFactory.createKey(TwiJavaNotes.class.getSimpleName(), "twitter");
 			EntityManager em =  EMF.getEntityManager();
 			TwiJavaNotes twitterNotes = null;
 			try {
 				Query maxCountQuery =  em.createQuery("select count(note.title) from Note note");
 				Long maxCount =  (Long) maxCountQuery.getSingleResult();
 				
 				
 				
 				twitterNotes = em.find(TwiJavaNotes.class, key);				
 				List<Long> subscriberIds = twitterNotes.getFollowers();
 				
 				System.out.println("twitterNotes :"+twitterNotes);
 				
 				int javaNoteId = twitterNotes.getNextNoteId();
				javaNoteId = (javaNoteId==0 || javaNoteId >= maxCount) ? 1 : javaNoteId+1;
 				
 				
 				Note note = em.find(Note.class,javaNoteId);
 				System.out.println("Note for  :"+javaNoteId+ " is "+note);
 				while(null == note){					
 					
					javaNoteId = (javaNoteId==0 || javaNoteId >= maxCount) ? 1 : javaNoteId+1;
 					
 					try {
 						System.out.println("javaNoteId :"+javaNoteId);
 						//note = em.find(Note.class,javaNoteId);
 						Query query = em.createQuery("Select from Note note where note.ord = "+javaNoteId);
 						note = (Note) query.getSingleResult();
 					} catch (Exception e) {
 						note = null;
 						System.out.println("Note is NUll :"+note);
 					}
 					
 					if(null != note){
 						System.out.println("Note found :"+note);
 						em.getTransaction().begin();
 						twitterNotes.setNextNoteId(javaNoteId);
 						em.persist(twitterNotes);
 						em.getTransaction().commit();
 						
 						//Send messages to twitter.
 						System.out.println("Note :"+note);
 						StringBuilder javatip = new StringBuilder();
 						javatip.append(note.getTitle().replace("<br>", " \n "));			
 						javatip.append("More on :"+" www.sudhakar.duraiswamy/notes?id="+note.getId());
 						javatip.append("  #Effective_Java");				
 						logger.log(Level.INFO, "Jave Tip Send out "+javatip);
 						
 						twitter.updateStatus(javatip.toString());
 						for(Long id :subscriberIds){
 							twitter.sendDirectMessage(id, javatip.toString());
 						}
 						
 						resp.setContentType("text/html");
 						resp.getWriter().print("Effective Jave Tip Successfully send out to Subscribers !!.");						
 					}
 				}
 
 			}catch (Exception e) {
 				e.printStackTrace();
 				logger.log(Level.INFO, "Jave Tip Sending failed!! ",e);
 				em.getTransaction().rollback();
 				throw new Exception(e);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();			
 			resp.setContentType("text/html");
 			resp.getWriter().print("Jave Tip  Sending FAILED !!.");
 		}
 	}
 	
 	/**
 	 * The main method.
 	 *
 	 * @param args the arguments
 	 */
 	public static void main(String[] args) {
 	
 
 	}
 }
