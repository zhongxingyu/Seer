 package controllers;
 
 import controllers.securesocial.SecureSocial;
 import mocks.MockFactory;
 import models.User;
 import play.Play;
 import play.mvc.Controller;
 import play.mvc.With;
 import securesocial.provider.SocialUser;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.TimeZone;
 
//@With( SecureSocial.class )
 public class Application extends Controller {
 
     public static void index(boolean disableAutoLogin) {
         if (disableAutoLogin) {
             render();
         } else {
             SocialUser user = getSocialUser();
             User emUser = User.getUser(user);
             boolean hasTournamentStarted = hasTournamentStarted();
             List<User> groupUsers = User.getUserGroupPointSorted(emUser.group);
             int userPlaceInGroup = groupUsers.indexOf(emUser) + 1; // +1 since index is zero-based
             int usersInGroup = groupUsers.size();
             render(user, emUser, hasTournamentStarted, userPlaceInGroup, usersInGroup);
         }
     }
 
     static SocialUser getSocialUser() {
         if (Play.mode.isProd()) {
             return SecureSocial.getCurrentUser();
         } else {
             return MockFactory.getMockedSocialUser();
         }
     }
 
 
     // The tournament starts the 8th of June 18:00 CET
     static boolean hasTournamentStarted() {
         Calendar tournamentStart = Calendar.getInstance(TimeZone.getTimeZone("CET"));
         tournamentStart.set(2012, Calendar.JUNE, 8, 17, 59);
         Calendar now = Calendar.getInstance();
         return now.after(tournamentStart);
     }
 
}
