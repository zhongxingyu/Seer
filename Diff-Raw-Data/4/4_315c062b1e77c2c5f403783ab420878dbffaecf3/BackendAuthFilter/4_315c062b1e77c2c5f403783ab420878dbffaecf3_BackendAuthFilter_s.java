 package de.anycook.api.filter;
 
import de.anycook.db.mysql.DBUser;
 import de.anycook.session.Session;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.container.ContainerRequestContext;
 import javax.ws.rs.container.ContainerRequestFilter;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.Provider;
 import java.io.IOException;
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: moji8208
  * Date: 3/15/13
  * Time: 10:51 AM
  * To change this template use File | Settings | File Templates.
  */
 @Provider
 public class BackendAuthFilter implements ContainerRequestFilter {
     private HttpServletRequest request;
     private HttpHeaders hh;
     private final Logger logger;
 
     public BackendAuthFilter(@Context HttpServletRequest request, @Context HttpHeaders hh){
         logger = Logger.getLogger(getClass());
         this.request = request;
         this.hh = hh;
     }
 
     @Override
     public void filter(ContainerRequestContext containerRequest){
         String path = containerRequest.getUriInfo().getPath();
 
         if(path.startsWith("/backend")){
             logger.debug(String.format("Authfilter: %s", path));
             try {
                 Session.checkAdminLogin(request, hh);
             } catch (SQLException|IOException e) {
                 logger.error(e ,e);
                 throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            } catch (DBUser.UserNotFoundException e) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
             }
         }
     }
 }
