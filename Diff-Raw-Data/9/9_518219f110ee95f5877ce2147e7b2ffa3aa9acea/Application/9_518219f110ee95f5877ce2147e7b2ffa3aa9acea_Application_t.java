 /**
  * comment
  */
 package controllers;
 
 import java.util.List;
 import com.avaje.ebean.Page;
 import com.avaje.ebean.PagingList;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.formdata.CountryType;
 import views.formdata.LoginFormData;
 import views.formdata.SearchFormData;
 import views.formdata.SurferFormData;
 import views.formdata.SurferTypes;
 import views.html.Index;
 import views.html.Login;
 import views.html.ShowSurfer;
 import views.html.ManageSurfer;
 import views.html.Updates;
 import views.html.Search;
 import models.Surfer;
 import models.SurferDB;
 import models.SurferUpdateDB;
 import models.UserInfo;
 import models.UserInfoDB;
 
 /**
  * Implements the controllers for this application.
  */
 public class Application extends Controller {
 
   
   /**
    * Returns the home page. 
    * @return The resulting home page. 
    */
   public static Result index() {
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(Index.render("Welcome to the home page.", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()),
                            searchForm, SurferTypes.getTypes(), CountryType.getTypes()));
   }
   
   @Security.Authenticated(Secured.class)
   public static Result newSurfer(){
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     String user = userInfo.getEmail();
     SurferFormData surferFD = new SurferFormData();
     List<String> foot = SurferDB.getFootstyleList();
     Form<SurferFormData> formData = Form.form(SurferFormData.class).fill(surferFD);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(ManageSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData, 
         SurferTypes.getTypes(), foot, searchForm, CountryType.getTypes()));
   }
   
   @Security.Authenticated(Secured.class)
   public static Result postSurfer(){
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     String user = userInfo.getEmail();
     Form<SurferFormData> formData = Form.form(SurferFormData.class).bindFromRequest();
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     if (formData.hasErrors()) {
       flash("error", "Please correct the form below.");
       List<String> foot = SurferDB.getFootstyleList();
       return badRequest(ManageSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), 
           formData, SurferTypes.getTypes(), foot, searchForm, CountryType.getTypes()));
     }
     else {
       SurferFormData data = formData.get();
       flash("success", String.format("Successfully added %s", data.name));
       SurferDB.add(data.slug, data);
       return ok(ShowSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), SurferDB.getSurfer(data.slug),
           searchForm, SurferTypes.getTypes(), CountryType.getTypes()));
     }
   }
   
   @Security.Authenticated(Secured.class)
   public static Result deleteSurfer(String slug){
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     String user = userInfo.getEmail();
     SurferDB.deleteSurfer(slug);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(Index.render("", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), searchForm, 
                            SurferTypes.getTypes(), CountryType.getTypes()));
   }
   
   @Security.Authenticated(Secured.class)
   public static Result manageSurfer(String slug){
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     String user = userInfo.getEmail();
     SurferFormData surferFD = new SurferFormData(SurferDB.getSurfer(slug));
     List<String> foot = SurferDB.getFootstyleList();
     Form<SurferFormData> formData = Form.form(SurferFormData.class).fill(surferFD);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(ManageSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()) ,formData, 
         SurferTypes.getTypes(SurferDB.getSurfer(slug).getType()), foot, searchForm, CountryType.getTypes()));
   }
   
   public static Result getSurfer(String slug){
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     if (SurferDB.getSurfer(slug) != null){
       return ok(ShowSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()) ,SurferDB.getSurfer(slug),
                 searchForm, SurferTypes.getTypes(), CountryType.getTypes()));
     }
     else { 
       return ok(Index.render("", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), searchForm,
                 SurferTypes.getTypes(), CountryType.getTypes()));
     }
   }
   
   /**
    * Return the update page.
    * @return The update page. 
    */
   public static Result getUpdates(){
       SearchFormData searchFormData = new SearchFormData();
       Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
       return ok(Updates.render("", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), SurferUpdateDB.getUpdates(),
                 searchForm, SurferTypes.getTypes(), CountryType.getTypes()));
   }
   
   /**
    * Provides the Login page (only to unauthenticated users). 
    * @return The Login page. 
    */
   public static Result login() {
     Form<LoginFormData> formData = Form.form(LoginFormData.class);
     //Form<RegistrationFormData> regFormData = Form.form(RegistrationFormData.class);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData, searchForm,
               SurferTypes.getTypes(), CountryType.getTypes()));
   }
   
   public static Result postLogin() {
 
     // Get the submitted form data from the request object, and run validation.
     Form<LoginFormData> formData = Form.form(LoginFormData.class).bindFromRequest();
     //Form<RegistrationFormData> regFormData = Form.form(RegistrationFormData.class);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
 
     if (formData.hasErrors()) {
       flash("error", "Login credentials not valid.");
       return badRequest(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData,
                         searchForm, SurferTypes.getTypes(), CountryType.getTypes()));
     }
     else {
       // email/password OK, so now we set the session variable and only go to authenticated pages.
       session().clear();
       session("email", formData.get().email);
       return redirect(routes.Application.index());
     }
   }
   
   /**
    * Logs out (only for authenticated users) and returns them to the Index page. 
    * @return A redirect to the Index page. 
    */
   @Security.Authenticated(Secured.class)
   public static Result logout() {
     session().clear();
     return redirect(routes.Application.index());
   }
   
   /**
    * Result page from the Search widget.
    * @return The search results.
    */
   public static Result search(int page) {
     Form<SearchFormData> formData = Form.form(SearchFormData.class).bindFromRequest();
     SearchFormData data = formData.get(); 
     Page<Surfer> results = SurferDB.search(data.searchTerm, data.type, data.country, page);
     System.out.println("DEBUG " + data.searchTerm + " " + data.type + " " + data.country + " " + page);
     return ok(Search.render("Search Results", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()),
         formData, SurferTypes.getTypes(), CountryType.getTypes(), data.searchTerm, data.type, data.country,
         results));
   }
  
  public static Result pageSearch(String searchTerm, String type, String country, int page) {
    Form<SearchFormData> formData = Form.form(SearchFormData.class).bindFromRequest();
    SearchFormData data = formData.get(); 
    Page<Surfer> results = SurferDB.search(searchTerm, type, country, page - 1);
    return ok(Search.render("Search Results", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()),
        formData, SurferTypes.getTypes(), CountryType.getTypes(), searchTerm, type, country,
        results));
  }
 }
