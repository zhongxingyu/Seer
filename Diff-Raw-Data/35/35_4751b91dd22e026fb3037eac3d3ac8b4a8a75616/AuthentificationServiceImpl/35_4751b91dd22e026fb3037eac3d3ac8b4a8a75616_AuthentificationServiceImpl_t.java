 package org.daum.javase.webportal.server;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.daum.common.genmodel.Agent;
import org.daum.common.genmodel.SitacFactory;
 import org.daum.javase.webportal.client.AuthentificationService;
 import org.daum.library.ormH.persistence.PersistenceSession;
 import org.daum.library.ormH.persistence.PersistenceSessionFactoryImpl;
 import org.daum.library.ormH.utils.PersistenceException;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pdespagn
  * Date: 7/13/12
  * Time: 4:10 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class AuthentificationServiceImpl extends RemoteServiceServlet implements AuthentificationService {
     private PersistenceSessionFactoryImpl factory;
     private Logger logger = LoggerFactory.getLogger(this.getClass());
 
 
     public AuthentificationServiceImpl(PersistenceSessionFactoryImpl factory){
         this.factory = factory;
     }
 
     @Override
     public boolean authenticateAgent(String name, String password) {
         return false;
     }
 
     @Override
     public Agent createAgent(String nom, String prenom, String matricule, String password) {
 
         logger.debug("CALL createAgent " + factory + " " + nom + " " + prenom);
         PersistenceSession session = null;
         Agent agent = null;
         try {
             session = factory.getSession();
             agent = SitacFactory.createAgent();
             agent.setNom(nom);
             agent.setPrenom(prenom);
             agent.setMatricule(matricule);
             agent.setPassword(password);
             session.save(agent);
         } catch (PersistenceException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             session.close();
         }
         return agent;
     }
 
 
 
 
 
 
 
 
 }
