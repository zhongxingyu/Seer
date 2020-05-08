 package com.epam.memegen;
 
 import com.epam.memegen.model.Comment;
 import com.google.appengine.api.datastore.*;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import static com.epam.memegen.Util.isNullOrEmpty;
 
 /**
  * @author Andrey Mormysh
  */
 public class CommentsServlet extends HttpServlet {
 
   private UserService userService = UserServiceFactory.getUserService();
   private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
 
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

     String memeId = req.getParameter("id");
     if (memeId != null) {
       resp.getWriter().write(new Gson().toJson(getComments(Long.parseLong(memeId))));
     }
   }
 
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

     JsonElement jsonElement = new JsonParser().parse(req.getReader());
     Comment comment = new Gson().fromJson(jsonElement, Comment.class);
     String user = getUserEmail();
     if (isNullOrEmpty(comment.getText())) {
       return;
     }
     comment.setUser(user);
     comment.setTimestamp(new Date().getTime());
     saveComment(comment);
     comment.setUser(getUserName(comment.getUser()));
     resp.getWriter().write(new Gson().toJson(comment));
   }
 
   private void saveComment(Comment comment) {
     datastore.put(toEntity(comment));
   }
 
   private List<Comment> getComments(long memeId) {
     Query commentsQuery = new Query(Comment.KIND).setFilter(
         Query.FilterOperator.EQUAL.of(Comment.MEME_ID, memeId));
     List<Comment> comments = Lists.newArrayList();
     for (Entity entity : datastore.prepare(commentsQuery).asIterable()) {
       comments.add(fromEntity(entity));
     }
     Collections.sort(comments, new Comparator<Comment>() {
       public int compare(Comment o1, Comment o2) {
         return (int) (o1.getTimestamp() - o2.getTimestamp());
       }
     });
 
     return comments;
   }
 
   private String getUserName(String userEmail) {
     if (userEmail != null && userEmail.length() > 0) {
       int atIndex = userEmail.indexOf('@');
       if (atIndex > 0) {
         return userEmail.substring(0, atIndex);
       } else {
         return userEmail;
       }
     } else {
       return "Unknown";
     }
   }
 
   private String getUserEmail() {
     return userService.getCurrentUser().getEmail();
   }
 
   private Comment fromEntity(Entity entity) {
     long memeId = (Long) entity.getProperty(Comment.MEME_ID);
     String text = (String) entity.getProperty(Comment.TEXT);
     long timestamp = (Long) entity.getProperty(Comment.TIMESTAMP);
     String user = (String) entity.getProperty(Comment.USER);
     return new Comment(memeId, text, timestamp, user);
   }
 
   private Entity toEntity(Comment comment) {
     Entity entity = new Entity("Comment", KeyFactory.createKey(MemeDao.KIND, comment.getMemeId()));
     entity.setProperty(Comment.MEME_ID, comment.getMemeId());
     entity.setProperty(Comment.TEXT, comment.getText());
     entity.setProperty(Comment.TIMESTAMP, comment.getTimestamp());
     entity.setProperty(Comment.USER, getUserName(comment.getUser()));
     return entity;
   }
 }
