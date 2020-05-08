 package dk.statsbiblioteket.doms.authchecker;
 
 import dk.statsbiblioteket.doms.authchecker.exceptions.*;
 import dk.statsbiblioteket.doms.authchecker.fedoraconnector.FedoraConnector;
 import dk.statsbiblioteket.doms.authchecker.ticketissuer.Ticket;
 import dk.statsbiblioteket.doms.authchecker.ticketissuer.TicketNotFoundException;
 import dk.statsbiblioteket.doms.authchecker.ticketissuer.TicketSystem;
 import dk.statsbiblioteket.doms.authchecker.user.Roles;
 import dk.statsbiblioteket.doms.authchecker.user.User;
 import dk.statsbiblioteket.doms.authchecker.userdatabase.UserDatabase;
 import dk.statsbiblioteket.doms.webservices.configuration.ConfigCollection;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.UriInfo;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Sep 30, 2010
  * Time: 2:54:18 PM
  * To change this template use File | Settings | File Templates.
  */
 @Path("/")
 public class Authchecker {
 
     private static UserDatabase users;
 
     private static UserDatabase adminUsers;
 
     private static final Object lock = new Object();
 
     private static final String USER_TTL_PROP ="dk.statsbiblioteket.doms.authchecker.users.timeToLive";
     private static final String ADMINUSER_TTL_PROP ="dk.statsbiblioteket.doms.authchecker.adminusers.timeToLive";
     private Log log = LogFactory.getLog(Authchecker.class);
     private String fedoralocation;
     private FedoraConnector fedora;
 
     private static final Random random = new Random();
 
     public Authchecker() throws BackendException {
         log.trace("Created a new authchecker webservice object");
         synchronized (lock){
             if (users == null){
                 long ttl;
                 try {
                     String ttlString = ConfigCollection.getProperties()
                             .getProperty(ADMINUSER_TTL_PROP,""+48*60*60*1000);
                     log.trace("Read '"+ADMINUSER_TTL_PROP+"' property as '"+ttlString+"'");
                     ttl = Long.parseLong(ttlString);
                 } catch (NumberFormatException e) {
                     log.warn("Could not parse the  '"+ ADMINUSER_TTL_PROP
                              +"' as a long, using default 30 sec timetolive",e);
                     ttl = 30*1000;
                 }
                 adminUsers = new UserDatabase(ttl);
             }
             if (users == null){
                 long ttl;
                 try {
                     String ttlString = ConfigCollection.getProperties()
                             .getProperty(USER_TTL_PROP,""+30*1000);
                     log.trace("Read '"+USER_TTL_PROP+"' property as '"+ttlString+"'");
                     ttl = Long.parseLong(ttlString);
                 } catch (NumberFormatException e) {
                     log.warn("Could not parse the  '"+ USER_TTL_PROP
                              +"' as a long, using default 30 sec timetolive",e);
                     ttl = 30*1000;
                 }
                 users = new UserDatabase(ttl);
             }
 
 
             if (fedoralocation == null){
                 fedoralocation = ConfigCollection.getProperties().getProperty(
                         "dk.statsbiblioteket.doms.authchecker.fedoralocation");
                 if (fedoralocation == null){
                     throw new FedoraException("Could not determine fedora location");
                 }
 
                 fedora = new FedoraConnector(fedoralocation);
             }
 
 
         }
     }
 
     @POST
     @Path("createAdminUser/{user}/WithTheseRoles")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Produces({MediaType.TEXT_XML})
     public User createAdminUserWithTheseRoles(
             @PathParam("user") String username,
            MultivaluedMap<String,String> roles) throws
                                                  FedoraException,
                                                  URLNotFoundException,
                                                  InvalidCredentialsException,
                                                  ResourceNotFoundException,
                                                  MissingArgumentException {
 
         log.trace("Entered createUserWithTheseRoles with the params"
                   + "'user='"+ username +"'");
 
         /*Generate user information*/
         String password = mkpass(username);
 
         log.trace("Password created for user='"+ username +"': '"+password+"'");
         /*Store user information in temp database*/
 
         List<Roles> fedoraroles = new ArrayList<Roles>();
         for (Map.Entry<String, List<String>> stringListEntry : roles.entrySet()) {
             if (stringListEntry.getKey().equals("fedoraRole")){
                 continue;
             }
             if (stringListEntry.getKey().equals("url")){
                 continue;
             }
             fedoraroles.add(new Roles(stringListEntry.getKey(),stringListEntry.getValue()));
         }
         User user = adminUsers.addUser(username, password,fedoraroles);
 
         log.trace("User='"+ username +" added to the database");
 
         log.trace("Success, returning user='"+ username +"' password");
         return user;
     }
 
 
     @POST
     @Path("isURLallowedFor/{user}/WithTheseRoles")
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Produces({MediaType.TEXT_XML})
     public User isUrlAllowedForThisUserWithTheseRoles(
             @PathParam("user") String username,
             @QueryParam("url") String resource,
             MultivaluedMap<String,String> roles) throws
                                                  FedoraException,
                                                  URLNotFoundException,
                                                  InvalidCredentialsException,
                                                  ResourceNotFoundException,
                                                  MissingArgumentException {
 
         log.trace("Entered isAllowedForThisUserWithTheseRoles with the params"
                   + "url='"+resource+"' and user='"+ username +"'");
 
         /*Generate user information*/
         String password = mkpass(username);
 
         if (resource == null){
             throw new MissingArgumentException("url is missing");
         }
 
 
         log.trace("Password created for user='"+ username +"': '"+password+"'");
         /*Store user information in temp database*/
 
         List<Roles> fedoraroles = new ArrayList<Roles>();
         for (Map.Entry<String, List<String>> stringListEntry : roles.entrySet()) {
             if (stringListEntry.getKey().equals("fedoraRole")){
                 continue;
             }
             if (stringListEntry.getKey().equals("url")){
                 continue;
             }
             fedoraroles.add(new Roles(stringListEntry.getKey(),stringListEntry.getValue()));
         }
         User user = users.addUser(username, password,fedoraroles);
 
         log.trace("User='"+ username +" added to the database");
 
         /*Check auth against Fedora*/
 
         String pid = fedora.getObjectWithThisURL(resource, username, password);
         log.trace("Found pid='"+pid+"' with the url '"+resource+"'");
 
         log.trace("Attempting to get datastream profile of pid '"+pid+"' with the username '"+ username
                   +"'");
         fedora.getDatastreamProfile(pid,"CONTENTS", username,password);
 
         log.trace("Success, returning user='"+ username +"' password");
         return user;
     }
 
 
     @GET
     @Path("isURLallowedFor/{user}/WithTheseRoles")
     @Produces({MediaType.TEXT_XML})
     public User isUrlAllowedForThisUserWithTheseRoles(
             @PathParam("user") String username,
             @QueryParam("url") String resource,
             @Context UriInfo ui) throws
                                  FedoraException,
                                  URLNotFoundException,
                                  InvalidCredentialsException,
                                  ResourceNotFoundException, MissingArgumentException {
         MultivaluedMap<String,String> params = ui.getQueryParameters();
         return isUrlAllowedForThisUserWithTheseRoles(username,resource,params);
     }
 
 
 
     @GET
     @Path("isURLallowedWithTheseRoles")
     @Produces({MediaType.TEXT_XML})
     public User isUrlAllowedWithTheseRoles(
             @QueryParam("url") String resource,
             @Context UriInfo ui) throws
                                  FedoraException,
                                  URLNotFoundException,
                                  InvalidCredentialsException,
                                  ResourceNotFoundException, MissingArgumentException {
 
         String username = mkUsername();
         return isUrlAllowedForThisUserWithTheseRoles(username,resource,ui);
 
     }
 
     @POST
     @Path("isURLallowedWithTheseRoles")
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Produces({MediaType.TEXT_XML})
     public User isUrlAllowedWithTheseRoles(
             @QueryParam("url") String resource,
             MultivaluedMap<String,String> roles) throws
                                                  FedoraException,
                                                  URLNotFoundException,
                                                  InvalidCredentialsException,
                                                  ResourceNotFoundException, MissingArgumentException {
 
         String username = mkUsername();
         return isUrlAllowedForThisUserWithTheseRoles(username,resource,roles);
 
     }
 
 
 
     private String mkUsername(){
         return UUID.randomUUID().toString();
     }
 
     private String mkpass(String user) {
         return UUID.randomUUID().toString();
     }
 
 
 
     @GET
     @Path("getRolesForUser/{user}/withPassword/{password}")
     @Produces({MediaType.TEXT_XML})
     public User getRolesForUser(
             @PathParam("user") String username,
             @PathParam("password") String password)
             throws BackendException {
         log.trace("Entered getRolesForUser with the params user='"+username+"' and"
                   + "password='"+password+"'");
         log.trace("Getting user='"+username+"' from the database");
         User user = users.getUser(username);
 
         if (user != null && user.getPassword().equals(password)){
             log.trace("User='"+username+"' found and password matches");
             return user;
         } else{
             user = adminUsers.getUser(username);
             if (user != null && user.getPassword().equals(password)){
                 log.trace("User='"+username+"' found and password matches");
                 return user;
             } else {
                 log.debug("User='"+username+"' not found, or password does not match");
                 throw new UserNotFoundException("Username '"+username+"' with password '"+password+"' not found in user database");
             }
         }
     }
 
 }
