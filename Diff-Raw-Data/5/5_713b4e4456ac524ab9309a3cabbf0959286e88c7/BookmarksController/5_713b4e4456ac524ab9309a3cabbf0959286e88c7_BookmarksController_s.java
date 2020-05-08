 package controllers;
 
 import com.avaje.ebean.Page;
 import helpers.CacheHelper;
 import helpers.ECacheObjectName;
 import models.Bookmark;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.html.bookmarks.bookmarklist;
 
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tuxburner
  * Date: 4/29/13
  * Time: 12:04 AM
  * To change this template use File | Settings | File Templates.
  */
 @Security.Authenticated(Secured.class)
 public class BookmarksController extends Controller {
 
   /**
    * Lists all the {@models.Bookmarks} which the current user created
    * @return
    */
   public static Result listBookmarks(final Integer page) {
     Page<Bookmark> listForUser = Bookmark.getBookmarksForUser(page);
 
     return ok(bookmarklist.render(listForUser,page));
   }
 
   /**
    * Creates a {@link models.Bookmark}
    * @param copyId
    * @return
    */
   public static Result bookmarkCopy(final Long copyId) {
 
     final Bookmark bookmark = Bookmark.bookmarkCopy(copyId);
     if(bookmark == null) {
       return badRequest();
     }
 
     final String msg = Messages.get("msg.success.bookmarkAdded",bookmark.copy.movie.title);
     Controller.flash("success",msg);
 
     CacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);
 
    return redirect(routes.BookmarksController.listBookmarks(null));
   }
 
   /**
    * Removes a {@link Bookmark}
    * @param bookmarkId
    * @return
    */
   public static Result removeBookmark(final Long bookmarkId) {
 
     String title = Bookmark.removeBookmark(bookmarkId);
 
     final String msg = Messages.get("msg.success.bookmarkRemoved",title);
     Controller.flash("success",msg);
 
     CacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);
 
    return redirect(routes.BookmarksController.listBookmarks(null));
   }
 
   /**
    * Gets all {@Dvd#id} which the user bookedmarked
    * @return
    */
   public static Set<Long> getBookmarkedCopyIds() {
     final Callable<Set<Long>> callable = new Callable<Set<Long>>() {
       @Override
       public Set<Long> call() throws Exception {
         return Bookmark.getBookmarkCopyIdsForUser();
       }
     };
 
     return CacheHelper.getSessionObjectOrElse(ECacheObjectName.BOOKMARKS,callable);
   }
 
 }
