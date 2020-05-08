 package com.tenfood.api;
 
 import java.io.IOException;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import com.tenfood.api.service.FlickrService;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.*;
 
 import com.tenfood.api.resource.PhotoResource;
 
 public class ApiHandler extends HttpServlet {
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         Context context = new Context();
         String path = req.getRequestURI();
         Map<String, String[]> queryMap = req.getParameterMap();
         context.setQueryMap(queryMap);
         Object result = null;
 
         if ( path.equals("/photo") ) {
             PhotoResource resource = new PhotoResource(new FlickrService());
             result = resource.getPhotos(context);
         }
 
         resp.getWriter().println("{ \"photos\": ");
         resp.getWriter().println(new ObjectMapper().writeValueAsString(result));
         resp.getWriter().println(",");
        resp.getWriter().println("\"context\": {");
         resp.getWriter().println(new ObjectMapper().writeValueAsString(context.getMessages()));
        resp.getWriter().println("}}");
     }
 
     public static void main(String[] args) throws Exception{
         Server server = new Server(Integer.valueOf(System.getenv("PORT")));
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
         server.setHandler(context);
         context.addServlet(new ServletHolder(new ApiHandler()),"/*");
         server.start();
         server.join();   
     }
 }
