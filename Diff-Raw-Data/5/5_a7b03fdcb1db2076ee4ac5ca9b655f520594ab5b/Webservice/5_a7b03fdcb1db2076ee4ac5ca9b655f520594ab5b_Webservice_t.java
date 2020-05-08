 package dk.statsbiblioteket.doms.authchecker;
 
 import dk.statsbiblioteket.doms.authchecker.exceptions.*;
 import dk.statsbiblioteket.doms.authchecker.fedoraconnector.FedoraConnector;
 import dk.statsbiblioteket.doms.authchecker.ticketissuer.Ticket;
 import dk.statsbiblioteket.doms.authchecker.ticketissuer.TicketNotFoundException;
 import dk.statsbiblioteket.doms.authchecker.ticketissuer.TicketSystem;
 import dk.statsbiblioteket.doms.authchecker.user.Roles;
 import dk.statsbiblioteket.doms.authchecker.user.User;
 import dk.statsbiblioteket.doms.authchecker.userdatabase.UserDatabase;
 import dk.statsbiblioteket.doms.webservices.ConfigCollection;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.ws.rs.*;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Sep 30, 2010
  * Time: 2:54:18 PM
  * To change this template use File | Settings | File Templates.
  */
 @Path("/")
 public class Webservice {
 
     private static TicketSystem tickets;
     private static UserDatabase users;
 
     private static final Object lock = new Object();
 
     private static final String TICKET_TTL_PROP = "dk.statsbiblioteket.doms.authchecker.tickets.timeToLive";
 
     private static final String USER_TTL_PROP ="dk.statsbiblioteket.doms.authchecker.users.timeToLive";
     private Log log = LogFactory.getLog(Webservice.class);
     private String fedoralocation;
     private FedoraConnector fedora;
 
     private static final Random random = new Random();
 
     public Webservice() throws BackendException {
         log.trace("Created a new authchecker webservice object");
         synchronized (lock){
             if (tickets == null){
                 long ttl;
                 try {
                     String ttlString = ConfigCollection.getProperties()
                             .getProperty(TICKET_TTL_PROP,""+30*1000);
                     log.trace("Read '"+TICKET_TTL_PROP+"' property as '"+ttlString+"'");
                     ttl = Long.parseLong(ttlString);
                 } catch (NumberFormatException e) {
                     log.warn("Could not parse the  '"+ TICKET_TTL_PROP
                              +"' as a long, using default 30 sec timetolive",e);
                     ttl = 30*1000;
                 }
                 tickets = new TicketSystem(ttl);
 
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
 
 
     @GET
     @Path("isURLallowedFor/{user}/WithTheseRoles")
     @Produces("text/plain")
     public User isUrlAllowedForThisUserWithTheseRoles(
             @QueryParam("url")
             String url,
             @PathParam("user")
             String username,
             @QueryParam("role")
             List<String> roles) throws
                                 FedoraException,
                                 URLNotFoundException,
                                 InvalidCredentialsException,
                                 ResourceNotFoundException {
         log.trace("Entered isAllowedForThisUserWithTheseRoles with the params"
                   + "url='"+url+"' and user='"+ username +"' and roles='"+ roles.toString()+"'");
 
         /*Generate user information*/
         String password = mkpass(username);
 
         log.trace("Password created for user='"+ username +"': '"+password+"'");
         /*Store user information in temp database*/
 
         Roles fedoraroles = new Roles("fedoraRole", roles);
         User user = users.addUser(username, password, fedoraroles);
 
         log.trace("User='"+ username +" added to the database");
 
         /*Check auth against Fedora*/
 
         String pid = fedora.getObjectWithThisURL(url, username, password);
         log.trace("Found pid='"+pid+"' with the url '"+url+"'");
 
         log.trace("Attempting to get datastream profile of pid '"+pid+"' with the username '"+ username
                   +"'");
         fedora.getDatastreamProfile(pid,"CONTENTS", username,password);
 
         log.trace("Success, returning user='"+ username +"' password");
         return user;
     }
 
 
     @GET
     @Path("isURLallowedWithTheseRoles")
     @Produces("text/plain")
     public User isUrlAllowedWithTheseRoles(
             @QueryParam("url")
             String url,
             @QueryParam("role")
             List<String> roles) throws
                                 FedoraException,
                                 URLNotFoundException,
                                 InvalidCredentialsException,
                                 ResourceNotFoundException {
         log.trace("Entered isAllowedWithTheseRoles with the params"
                   + "url='"+url+"' and roles='"+ roles.toString()+"'");
 
         String username = mkUsername();
         return isUrlAllowedForThisUserWithTheseRoles(url,username,roles);
 
     }
 
     private String mkUsername(){
         return "user"+random.nextInt();
     }
 
     private String mkpass(String user) {
         return user+random.nextInt();
     }
 
 
 
     @GET
     @Path("getRolesForUser/{user}/withPassword/{password}")
     @Produces("text/xml")
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
             log.debug("User='"+username+"' not found, or password does not match");
             throw new UserNotFoundException("Username '"+username+"' with password '"+password+"' not found in user database");
         }
     }
 
     @POST
     @Path("issueTicket")
     public Ticket issueTicket(
             @QueryParam("username")
             String username,
             @QueryParam("url")
             String url){
         log.trace("Entered issueTicket with params '"+username+"' and url='"+url+"'");
         Ticket ticket = tickets.issueTicket(username, url);
         log.trace("Issued ticket='"+ticket.getID()+"'");
         return ticket;
     }
 
 
     @GET
    @Path("resolveTicket")
     public Ticket resolveTicket(
            @QueryParam("ID")
             String ID)
             throws TicketNotFoundException {
         log.trace("Entered resolveTicket with param ID='"+ID+"'");
         Ticket ticket = tickets.getTicketFromID(ID);
         if (ticket == null){
             throw new TicketNotFoundException("The ticket ID '"+ID+"' was not found in the system");
         }
         log.trace("Found ticket='"+ticket.getID()+"'");
         return ticket;
     }
 }
