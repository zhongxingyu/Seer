 package nl.jpoint.chronicle.controller;
 
 import com.google.inject.Inject;
 import nl.jpoint.chronicle.dao.PageDAO;
 import nl.jpoint.chronicle.domain.Page;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import javax.inject.Singleton;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.io.IOException;
 import java.util.Date;
 
 @Singleton
 @Path("/page")
 public class PageController {
 
     @Inject
     private ObjectMapper mapper;
     @Inject
     private PageDAO pageDAO;
 
     @POST
     @Path("/{uri}")
     public String postPage(@PathParam("uri") String uri, String body) throws IOException {
         Page page = mapper.readValue(body, Page.class);
         page.setUri(uri);
         pageDAO.save(page);
         return formatPage(page);
     }
 
     @GET
     @Path("/{pageName}")
     @Produces(MediaType.APPLICATION_JSON)
     public String getPage(@PathParam("pageName") String pageName) throws IOException {
 
         String uri = parsePageUri(pageName);
         Page page = pageDAO.findByUri(uri);
 
         if (page == null) {
             page = new Page();
             page.setUri(pageName);
             page.setParent(parseParentUri(pageName));
             page.getMeta().setCreated((new Date().getTime()));
 
             if ("main".equals(uri)) {
                 createContentForDefaultPage(page);
             } else {
                 page.setTitle("New page: " + uri);
             }
         }
 
         return formatPage(page);
 
     }
 
     private void createContentForDefaultPage(Page page) {
         page.setTitle("Welcome to Chronicle");
         page.setContent("Here should be more explanation about this project, maybe just load the README.md once the project has Markdown parsing!");
     }
 
     private String formatPage(Page page) throws IOException {
         return mapper.writeValueAsString(page);
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public String getAllPages() throws IOException {
         return mapper.writeValueAsString(pageDAO.query());
 
     }
 
     @DELETE
    public void deletePage(Page page) {
        pageDAO.delete(page);
     }
 
     public static String parsePageUri(String target) {
         while (target.contains("//")) {
             target = target.replaceAll("//", "/");
         }
         if (target.startsWith("/")) {
             target = target.substring(1, target.length());
         }
         if (target.endsWith("/")) {
             target = target.substring(0, target.length() - 1);
         }
 
         return target;
     }
 
     public static String parseParentUri(String target) {
         String uri = parsePageUri(target);
 
         if (!uri.contains("/")) {
             return "";
         }
         return uri.substring(0, uri.lastIndexOf('/'));
     }
 
 }
