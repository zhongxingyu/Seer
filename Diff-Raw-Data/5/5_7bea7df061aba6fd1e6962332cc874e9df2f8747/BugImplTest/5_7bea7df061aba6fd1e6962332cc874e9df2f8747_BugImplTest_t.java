 package org.test.bugtracker.impl.bo;
 
 import static junit.framework.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.PropertyValueException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.test.bugtracker.bo.BugBO;
 import org.test.bugtracker.bo.UserBO;
 import org.test.bugtracker.impl.model.BugImpl;
 import org.test.bugtracker.impl.model.CommentImpl;
 import org.test.bugtracker.impl.model.UserImpl;
 import org.test.bugtracker.model.Bug;
 import org.test.bugtracker.model.Comment;
 import org.test.bugtracker.model.User;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 @ContextConfiguration(locations = { "classpath:spring/context.xml" })
 public class BugImplTest extends AbstractTestNGSpringContextTests {
     private static final String MESSAGE = "message";
 
     private static final String TITLE = "title";
 
     private static final String USER_NAME = BugImplTest.class.getSimpleName();
 
     @Autowired
     private ApplicationContext context;
     @Autowired
     private BugBO bugBO;
     @Autowired
     private UserBO userBO;
 
     private User user;
     
     @BeforeClass
     private void setup() {
         user = new UserImpl();
         user.setLogin(USER_NAME);
         user.setPass(USER_NAME);
         userBO.save(user);
         assertNotNull(user.getId());
     }
 
     @Test(priority = 0)
     public void createNewBug() {
         Bug bug = new BugImpl();
         bug.setTitle(TITLE);
         bug.setMessage(MESSAGE);
         bug.setAuthor(user);
         bugBO.save(bug);
         assertNotNull(bug.getId());
         Bug bug1 = bugBO.findById(bug.getId());
         assertEquals(TITLE,bug1.getTitle());
         assertEquals(MESSAGE,bug1.getMessage());
     }
     
     @Test(priority = 0, expectedExceptions={PropertyValueException.class})
     public void createNewBugWithoutAuthor() {
         Bug bug = new BugImpl();
         bug.setTitle(TITLE);
         bug.setMessage(MESSAGE);        
         bugBO.save(bug);
         assertNotNull(bug.getId());
         Bug bug1 = bugBO.findById(bug.getId());
         assertEquals(TITLE,bug1.getTitle());
         assertEquals(MESSAGE,bug1.getMessage());
     }
 
     @Test(priority = 0)
     public void createNewBugWithEmptyComments() {
         Bug bug = new BugImpl();
         bug.setTitle(TITLE);
         bug.setMessage(MESSAGE);
         bug.setAuthor(user);
         bug.setComments(new ArrayList<Comment>());
         bugBO.save(bug);
         assertNotNull(bug.getId());
         Bug bug1 = bugBO.findById(bug.getId());
         assertEquals(TITLE,bug1.getTitle());
         assertEquals(MESSAGE,bug1.getMessage());
     }
     
     @Test(priority = 0)
     public void createNewBugWithNotEmptyComments() {
         Bug bug = new BugImpl();
         bug.setTitle(TITLE);
         bug.setMessage(MESSAGE);
         bug.setAuthor(user);
         List<Comment> comments = new ArrayList<Comment>();
         Comment comment = new CommentImpl();
         comment.setAuthor(user);
         comment.setMessage(MESSAGE);
         comment.setBug(bug);
         comments.add(comment);
         bug.setComments(comments);
         bugBO.save(bug);
        Long id = bug.getId();
        assertNotNull(id);        
        Bug bug2 = bugBO.findById(id);
         assertEquals(TITLE,bug2.getTitle());
         assertEquals(MESSAGE,bug2.getMessage());
         assertEquals(user, bug2.getAuthor());
         List<Comment> comments2 = bug2.getComments();
         assertNotNull(comments2);
         assertEquals(1, comments2.size());
         assertEquals(comment, comments2.get(0));
     }
     
     @Test(priority = 0)
     public void updateBug() {
         String title2 = "title2";
         String message2 = "message2";
         
         Bug bug = new BugImpl();
         bug.setTitle(TITLE);
         bug.setMessage(MESSAGE);
         bug.setAuthor(user);
         bugBO.save(bug);
         Long id = bug.getId();
         assertNotNull(id);
         Bug bug2 = bugBO.findById(id);
         assertEquals(TITLE,bug2.getTitle());
         assertEquals(MESSAGE,bug2.getMessage());
         bug2.setTitle(title2);
         bug2.setMessage(message2);
         bugBO.update(bug2);
         Long id2 = bug2.getId();
         assertEquals(bug.getId(),id2);
         Bug bug3 = bugBO.findById(id2);
         assertNotNull(bug3);
         assertEquals(title2,bug3.getTitle());
         assertEquals(message2,bug3.getMessage());
     }
 }
