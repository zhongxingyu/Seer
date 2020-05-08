 package org.jboss.seam.university.action;
 
 import java.util.Date;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.jcr.Node;
 import javax.jcr.Repository;
 import javax.jcr.Session;
 import javax.persistence.EntityManager;
 
 import org.jboss.seam.remoting.annotations.WebRemote;
import org.jboss.seam.security.annotations.LoggedIn;
 import org.jboss.seam.university.model.Category;
 
 /**
  * 
  * @author Shane Bryzak
  *
  */
 public @RequestScoped class ContentAction {
 
     //@Inject Logging log;
     
     @Inject EntityManager entityManager;
     
     @Inject Repository repository;
     
     @Inject LatestContentAction latestContent;
     
     @Inject TempFileManager fileManager;
         
    @WebRemote @LoggedIn
     public boolean saveLocalContent(Category category, String title, String content) throws Exception  {   
         
         if (title == null) {
             throw new IllegalArgumentException("Error - title parameter cannot be null");
         }
         
         Session session = repository.login("default");
         try {          
             Node rootNode = session.getRootNode();            
             Node categoryParent = rootNode.hasNode(category.getName()) ? rootNode.getNode(category.getName()) :
                 rootNode.addNode(category.getName());
 
             Node contentNode = categoryParent.addNode(title);
             contentNode.setProperty("content", content);
 
             Date now = new Date();
             //contentNode.setProperty("created", now);
             session.save();
             
             latestContent.add(contentNode.getIdentifier(), now);
             
             return true;
         }
         finally {
             session.logout();
         }
     }
 }
