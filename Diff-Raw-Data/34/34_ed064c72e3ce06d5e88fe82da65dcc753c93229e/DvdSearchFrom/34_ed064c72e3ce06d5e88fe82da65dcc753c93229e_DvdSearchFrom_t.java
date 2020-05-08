 package forms;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.cache.Cache;
 import play.mvc.Controller;
 import play.mvc.Http.Context;
 
 import com.google.gson.Gson;
 
 import controllers.Secured;
 
 /**
  * This holds the filter for listing the dvd
  * 
  * @author tuxburner
  * 
  */
 public class DvdSearchFrom {
 
   /**
    * If the user searched for a dvd
    */
   public String searchFor = null;
 
   /**
    * The currently viewed page
    */
   public int currentPage = 0;
 
   /**
    * If the user only wants to see a specific genre
    */
   public String genre = null;
 
   /**
    * If the user wants to see movies by an actor
    */
   public String actor = null;
 
   /**
    * If the user wants to see a movie by a director
    */
   public String director = null;
 
   /**
    * If the user wants to see dvds from a user
    */
   public String userName = null;
 
   /**
    * If set to true and username is set only lend dvds will be shown
    */
   public boolean lendDvd = false;
 
   /**
    * If set to true only dvds linked with movies to review are displayed
    */
   public boolean moviesToReview = false;
 
   /**
    * Age Rating of the movie
    */
   public String ageRating = null;
 
   /**
    * What field in the database to use to order the list
    */
   public EDvdListOrderBy orderBy = EDvdListOrderBy.DATE;
 
   /**
    * How to order the result list
    */
   public EDvdListOrderHow orderHow = EDvdListOrderHow.DOWN;
 
   /**
    * What copy type to display
    */
   public String copyType = null;
 
   /**
    * Checks if the searchForm should be displayed in the advanced mode
    */
   public static boolean displayAdvancedForm() {
     final DvdSearchFrom form = DvdSearchFrom.getCurrentSearchForm();
     return (StringUtils.isEmpty(form.copyType) == false || form.lendDvd == true);
   }
 
   /**
    * Gets the current search form from the cache if the cache is empty a new one
    * is created
    * 
    * @return
    */
   public static DvdSearchFrom getCurrentSearchForm() {
 
     final Context ctx = Controller.ctx();
 
     if (ctx == null) {
       return null;
     }
 
     final Object object = Cache.get(ctx.session().get(Secured.AUTH_SESSION) + ".dvdlistform");
     DvdSearchFrom returnVal = null;
     if (object == null || object instanceof DvdSearchFrom == false) {
       returnVal = new DvdSearchFrom();
       Cache.set(ctx.session().get(Secured.AUTH_SESSION) + ".dvdlistform", returnVal);
     } else {
       returnVal = (DvdSearchFrom) object;
     }
 
     return returnVal;
   }
 
   /**
    * Write the {@link DvdSearchFrom} to the cache for the user
    * 
    * @param ctx
   * @param dvdSearchFrom
    */
  public static void setCurrentSearchForm(final DvdSearchFrom dvdSearchFrom) {

    // make sure when set to lend and no username is given set it to the current
    // user
    if (dvdSearchFrom.lendDvd == true && StringUtils.isEmpty(dvdSearchFrom.userName) == true) {
      dvdSearchFrom.userName = Controller.request().username();
    }

    Cache.set(Controller.ctx().session().get(Secured.AUTH_SESSION) + ".dvdlistform", dvdSearchFrom);
   }
 
   public static String getAgeRatingsAsJson() {
     return new Gson().toJson(DvdForm.getAgeRatings());
   }
 
   public static String getCopyTypesJson() {
     return new Gson().toJson(DvdForm.getCopyTypes());
   }
 
 }
