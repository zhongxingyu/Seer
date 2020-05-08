 package com.epam.memegen;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonWriter;
 
 @SuppressWarnings("serial")
 public class MemeServlet extends HttpServlet {
   private static final Logger logger = Logger.getLogger(MemeServlet.class.getName());
   private final MemeDao memeDao = new MemeDao();
 
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     resp.setContentType("application/json");
     resp.setCharacterEncoding("UTF-8");
 
     if (req.getPathInfo() == null) {
       resp.sendError(HttpServletResponse.SC_NOT_FOUND);
       return;
     }
 
     String idStr = req.getPathInfo().replaceAll("[^0-9]+", "");
     if (idStr.equals("")) {
       resp.sendError(HttpServletResponse.SC_NOT_FOUND);
       return;
     }
     long id = Long.valueOf(idStr);
 
     String json = memeDao.getAsJson(id);
     if (json == null) {
       resp.sendError(HttpServletResponse.SC_NOT_FOUND);
       return;
     }
     resp.getWriter().write(json);
   }
 
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
       IOException {
     try {
       JsonElement jsonElement = new JsonParser().parse(req.getReader());
       String json = memeDao.create(jsonElement);
       resp.setStatus(HttpServletResponse.SC_OK);
       resp.getWriter().write(json);
     } catch (IllegalArgumentException e) {
       logger.log(Level.WARNING, e.getMessage(), e);
       writeError(400, e.getMessage(), resp);
     } catch (IOException e) {
       logger.log(Level.SEVERE, e.getMessage(), e);
       writeError(400, e.getMessage(), resp);
     }
   }
 
   @Override
   protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException {
     String idStr = req.getPathInfo().replaceAll("[^0-9]+", "");
     if (idStr.equals("")) {
      resp.sendError(404);
       return;
     }
     long id = Long.valueOf(idStr);
 
     try {
       memeDao.delete(id);
     } catch (EntityNotFoundException e) {
      resp.sendError(404, "No such meme");
       return;
     }
   }
 
   private void writeError(int statusCode, String message, HttpServletResponse resp) throws IOException {
     resp.setStatus(statusCode);
     resp.setContentType("application/json");
 
     JsonWriter w = new JsonWriter(resp.getWriter());
     w.beginObject().name("message").value(message).endObject();
     w.close();
   }
 }
