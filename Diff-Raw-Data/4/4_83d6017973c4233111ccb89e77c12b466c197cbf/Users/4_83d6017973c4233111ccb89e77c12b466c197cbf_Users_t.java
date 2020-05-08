 package controllers;
 
 import models.Artist;
 import models.User;
 import org.apache.commons.collections.CollectionUtils;
 import play.data.validation.Required;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 @With(Secure.class)
 public class Users extends Controller {
 
     public static User getUserFromSession() {
 
         String email = session.get("user");
 
         if (null == email) {
             Application.index();
         }
 
         return User.find("byEmail", email).first();
     }
 
     public static void index() {
 
         List<User> users = User.findAll();
 
         render(users);
     }
 
     public static void matchingUsers(@Required String email) {
 
         if (null == email) {
             Application.notFound("Email not found");
         }
 
         // get user from session
         User userSession = getUserFromSession();
 
         // get user to compare with
         User userCompare = User.find("byEmail", email).first();
 
         if (null == userCompare) {
             Application.notFound("User not found");
         }
 
         // get the artists from both users
         List<Artist> artistsSession = userSession.artists;
         List<Artist> artistsCompare = userCompare.artists;
 
         // get the matching count
         List<Artist> matchingArtists = new ArrayList<Artist>();
         matchingArtists.addAll(CollectionUtils.retainAll(artistsSession, artistsCompare));
 
         // get the matching percent
         Integer percentSession = matchingArtists.size() * 100 / artistsSession.size();
         Integer percentCompare = matchingArtists.size() * 100 / artistsCompare.size();
 
 
         render(userSession, userCompare, matchingArtists, artistsSession, artistsCompare, percentSession, percentCompare);
     }
 
     public static void profileDev() {
 
         User user = getUserFromSession();
 
         render(user);
     }
 
     public static void profile() {
 
        User user = getUserFromSession();
 
         render(user);
     }
 }
