 /*
  * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package controllers;
 
 import com.avaje.ebean.Ebean;
 import controllers.compositions.UserCheck;
 import data.validation.GsConstraints;
 import models.*;
 import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.data.Form;
 import play.data.validation.Constraints;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Result;
 import play.mvc.With;
 import server.ApplicationContext;
 import server.HeaderMessage;
 import server.exceptions.ServerException;
 import utils.CollectionUtils;
 import utils.RestUtils;
 import views.html.common.linkExpired;
 import views.html.widgets.admin.newPassword;
 import views.html.widgets.admin.resetPassword;
 import views.html.widgets.admin.signin;
 import views.html.widgets.admin.signup;
 import views.html.widgets.dashboard.account;
 import views.html.widgets.dashboard.angularjs_widget;
 import views.html.widgets.dashboard.previewWidget;
 import views.html.widgets.dashboard.widgets;
 import views.html.widgets.widget;
 import views.html.widgets.widgetSinglePage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Pattern;
 
 import static utils.RestUtils.*;
 
 
 /**
  * Widget Admin controller.
  * 
  * @author Igor Goldenberg
  */
 public class WidgetAdmin extends Controller
 {
 
     private static Logger logger = LoggerFactory.getLogger( WidgetAdmin.class );
 
     public static Result getWidget( String apiKey ){
         if ( StringUtils.isEmpty(apiKey)){
             return badRequest("apiKey required");
         }
         Widget widgetItem = Widget.getWidget(apiKey);
         if ( widgetItem == null || !widgetItem.isEnabled()){
             return ok();
         }
         return ok(widget.render(ApplicationContext.get().conf().mixpanelApiKey, widgetItem));
     }
     
     public static Result getWidgetSinglePage( String apiKey ){
         Widget widgetItem = Widget.getWidget(apiKey);
         if ( widgetItem == null ){
             return notFound("widget does not exist");
         }else if( !widgetItem.isEnabled() ){
             return badRequest( "widget disabled");
         }
         return ok(widgetSinglePage.render(widgetItem, request().host()));
     }
 
     public static Result icon( String apiKey ){
         WidgetIcon widgetItem = WidgetIcon.findByWidgetApiKey( apiKey );
        if ( widgetItem == null || ArrayUtils.isEmpty(widgetItem.getData()) ){
             return notFound(  );
         }
         return ok( widgetItem.getData() ).as( widgetItem.getContentType() );
     }
 
     public static Result embedImage( String apiKey ){
         Widget widgetItem = Widget.getWidget(apiKey);
         if ( widgetItem == null ){
             return notFound("widget does not exist");
         }else if( !widgetItem.isEnabled() ){
             return badRequest( "widget disabled");
         }
     	//TODO(ran): This will be project specific logo, with number of users etc, something engaging...
     	return redirect("/img/cloudify-logo-embed.png");
     }
 
     
 
 	/*
 	 * Creates new account.
 	 */
 	public static Result signUp( String email, String passwordConfirmation, String password, String firstname, String lastname )
 	{
         try {
             Constraints.EmailValidator ev =  new Constraints.EmailValidator();
             if ( StringUtils.isEmpty( email ) || !ev.isValid( email ) ){
                 new HeaderMessage().setError( "Email is incorrect" ).apply( response().getHeaders() );
                 return internalServerError(  );
             }
             if ( !validatePassword( password, passwordConfirmation, email ) ) {
                 return internalServerError();
             }
 
             User.Session session = User.newUser( firstname, lastname, email, password ).getSession();
             return RestUtils.resultAsJson( session );
         } catch ( ServerException ex ) {
             return resultErrorAsJson( ex.getMessage() );
         }
     }
 
     public static Result logout(){
         session().clear();
         response().discardCookies( "authToken" );
         return redirect( routes.WidgetAdmin.index() );
     }
 
     public static Result index()
     {
         // lets assume that if we have "authToken" we are already logged in
         // and we can redirect to widgets.html
         Http.Cookie authToken = request().cookies().get( "authToken" );
         if ( authToken != null && User.validateAuthToken( authToken.value(), true ) != null ) {
             return redirect( routes.WidgetAdmin.newWidgetsPage() );
         }
         else{
             return redirect( routes.WidgetAdmin.getSigninPage( null ) );
         }
     }
 
     /**
      *
      * this method will reset the user's password.
      * the parameters are cryptic on purpose.
      *
      * @param p - the hmac
      * @param pi - the user id
      * @return -
      */
     public static Result resetPasswordAction( String p, Long pi ){
         User user = User.findById( pi );
         // validate p
         if ( !ApplicationContext.get().getHmac().compare( p, user.getEmail(),  user.getId(), user.getPassword()  )){
             return badRequest(  linkExpired.render() );
         }
         // if p is valid lets reset the password
         String newPasswordStr = StringUtils.substring( p, 0, 7 );
         user.encryptAndSetPassword( newPasswordStr );
         user.save();
         return ok( newPassword.render( newPasswordStr ) );
     }
 
     public static Result postResetPassword( String email, String h ){
         logger.info( "user {} requested password reset", email );
         if ( !StringUtils.isEmpty( h ) ){
             return badRequest(  ); // this is a bot.. lets block it.
         }
 
         if ( StringUtils.isEmpty( email ) || !(new Constraints.EmailValidator().isValid( email )) ){
             new HeaderMessage().setError( "Invalid email" ).apply( response().getHeaders() );
             return badRequest(  );
         }
 
         User user = User.find.where(  ).eq( "email",email ).findUnique();
         if ( user == null ){
             return ok(  ); // do not notify if user does not exist. this is a security breach..
             // simply reply that an email was sent to the address.
         }
 
         ApplicationContext.get().getMailSender().resetPasswordMail( user );
         return ok(  );
     }
 
 
     public static Result getAccountPage(){
         return ok( account.render() );
     }
 
     public static Result getWidgetsPage(){
         return ok( widgets.render() );
     }
     public static Result getSigninPage( String message ){
         return ok( signin.render( message ));
     }
 
     public static Result getSignupPage(){
         return ok( signup.render() );
     }
     public static Result getResetPasswordPage(){
         return ok( resetPassword.render() );
     }
 
 
     public static Result checkPasswordStrength( String password, String email ){
         if ( !StringUtils.isEmpty( email  ) && new Constraints.EmailValidator().isValid( email )){
             String result = isPasswordStrongEnough( password, email );
             if ( result != null ){
                 new HeaderMessage().setError( result ).apply( response().getHeaders() );
                 return internalServerError(  );
             }
             return ok(  );
         }
         return ok(  );
     }
 
     private static String isPasswordStrongEnough( String password, String email ){
         if ( StringUtils.length( password ) < 8 ){
             return "Password is too short";
         }
         if ( !Pattern.matches( "(?=^.{8,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$", password ) && !StringUtils.containsIgnoreCase( email, password ) ){
             return "Password must match requirements";
         }
 
         Set<String> strSet = new HashSet<String>(  );
         for ( String s : password.split( "" ) ) {
             if ( StringUtils.length( s ) > 0){
                 strSet.add( s.toLowerCase( ) );
             }
         }
 
         if ( CollectionUtils.size( strSet ) < 3 ){
             return "Too many repeating letters";
         }
 
         if ( StringUtils.getLevenshteinDistance( password, email.split( "@" )[0] ) < 5 || StringUtils.getLevenshteinDistance( password, email.split( "@" )[1] ) < 5 ){
             return "Password similar to email";
         }
 
         return null;
     }
 
     public static Result getPasswordMatch( String authToken, String password ){
         User user = User.validateAuthToken( authToken );
         String passwordWeakReason = isPasswordStrongEnough( password, user.getEmail() );
         if ( passwordWeakReason == null ){
             return ok( );
         }
         return ok( passwordWeakReason );
     }
 
     /**
      *
      * @param newPassword - the password user chose
      * @param confirmPassword - the confirmed password
      * @param email - user's email. used for checking similarity to password. passwords that are similar to email are considered weak.
      * @return true iff password is considered strong enough according to our policy.
      */
     private static boolean validatePassword( String newPassword, String confirmPassword, String email )
     {
         if ( !StringUtils.equals( newPassword, confirmPassword ) ) {
             new HeaderMessage().setError( "Passwords do not match" ).apply( response().getHeaders() );
             return false;
         }
 
         String passwordWeakReason = isPasswordStrongEnough( newPassword, email );
         if ( passwordWeakReason != null ) {
             new HeaderMessage().setError( passwordWeakReason ).apply( response().getHeaders() );
             return false;
         }
         return true;
     }
     public static Result postChangePassword( String authToken, String oldPassword, String newPassword, String confirmPassword ){
         User user = User.validateAuthToken( authToken );
         if ( !user.comparePassword( oldPassword )){
             new HeaderMessage().setError( "Wrong Password" ).apply( response().getHeaders() );
             return internalServerError();
         }
 
         if ( !validatePassword( newPassword, confirmPassword, user.getEmail() ) ){
             return internalServerError(  );
         }
 
 
         user.encryptAndSetPassword( newPassword );
         user.save();
         new HeaderMessage().setSuccess( "Password Changed Successfully" ).apply( response().getHeaders() );
         return ok(  );
     }
 
 	/**
 	 * Login with existing account.
 	 * 
 	 * @param email
 	 * @param password
 	 * @return
 	 */
 	public static Result signIn( String email, String password )
 	{
 		try
 		{
 			User.Session session = User.authenticate(email, password);
 
 			return resultAsJson(session);
 		}catch( ServerException ex )
 		{
 			return resultErrorAsJson(ex.getMessage());
 		}
 	}
 
 	
 	public static Result getAllUsers( String authToken )
 	{
 		User.validateAuthToken(authToken);   // TODO : remove these validations and use "action interceptor"
                                             // there's no official documentation for interceptors. see code sample at : http://stackoverflow.com/questions/9629250/how-to-avoid-passing-parameters-everywhere-in-play2
 		List<User> users = User.getAllUsers();
 
 		return resultAsJson(users);
 	}
 
     /**
      * This function will save the widget.
      * It receives the user's authToken, the widget in JSON format, and icon file in the request body.
      *
      * This method handles 2 scenarios (not best practice, we know),
      *  - if widget exists
      *  - if widget does not exists - we create it
      *
      *
      * Removing an icon is decided if form gets "removeIcon" key.
      * @return the widget as JSON - without the icon data.
      */
     public static Result postWidget( ){
 
         // read everything from the form.
         Http.MultipartFormData body = request().body().asMultipartFormData();
         String widgetString = body.asFormUrlEncoded().get( "widget" )[0];
         boolean removeIcon = body.asFormUrlEncoded().containsKey("removeIcon");
         String authToken = body.asFormUrlEncoded().get( "authToken" )[0];
         Http.MultipartFormData.FilePart picture = body.getFile( "icon" );
 
         JsonNode jsonNode = Json.parse(widgetString);
         Widget w = null;
         User user = null;
         if ( jsonNode.has( "id" ) ){
             String widgetApiKey = jsonNode.get("apiKey").asText();
             w = getWidgetSafely( authToken, widgetApiKey );
         }else{
             user = User.validateAuthToken( authToken );
         }
 
         ObjectMapper mapper = new ObjectMapper();
 
         Form<Widget> validator = form( Widget.class ).bind( jsonNode );
 
         // ignore apiKey errors
         validator.errors().remove("apiKey");
 
         if ( validator.hasErrors() ){
             new HeaderMessage().populateFormErrors( validator ).apply( response().getHeaders() );
             logger.error("trying to save an invalid widget " + validator.toString());
             return badRequest( );
         }
 
         try {
             if ( w == null ){
                 // creating a new widget.
                 w = mapper.treeToValue( jsonNode, Widget.class );
                 w.init();
             }else{
                 mapper.readerForUpdating( w ).treeToValue( jsonNode, Widget.class );
             }
             logger.info( "successfully turned json to widget [{}]", w );
 
             if ( user != null ){
                 user.addNewWidget( w );
             }
 
             w.save(  );
             w.refresh( );
 
             if ( removeIcon ){
                 WidgetIcon icon = WidgetIcon.findByWidgetApiKey( w.getApiKey() );
                 if ( icon != null ){
                     w.setIcon(null);
                     w.save();
                     icon.delete();
                 }
             }
 
             // now lets handle the icon - but only if one was posted.
             if ( picture != null ) {
 
                 // decide if widget already has an icon or not
                 WidgetIcon icon = WidgetIcon.findByWidgetApiKey( w.getApiKey() );
 
                 if ( icon == null ){
                     icon = new WidgetIcon();
                 }
                 String fileName = picture.getFilename();
                 String contentType = picture.getContentType();
 
                 File file = picture.getFile();
                 byte[] iconData = IOUtils.toByteArray( new FileInputStream( file ) );
 
                 icon.setName( fileName );
                 icon.setContentType( contentType );
                 icon.setData( iconData );
                 Ebean.save( icon ); // supports both save and update.
                 w.setIcon( icon );
                 w.save(  );
                 return ok( "Added icon successfully" );
             }
 
 
             return ok(  Json.toJson( w ) );
         } catch ( IOException e ) {
             logger.error( "unable to turn body to Json",e  );
         }
         logger.info( "saving widget [{}]", widgetString );
         return ok(  );
     }
 
 
 	public static Result createNewWidget( String widgetId, String authToken,  String productName, String productVersion,
 										  String title, String youtubeVideoUrl, String providerURL,
 										  String recipeURL, String consolename, String consoleurl, String rootpath, String recipeName, String consoleUrlService )
 	{
         User user = User.validateAuthToken(authToken);
         Widget widget = null;
         if ( !NumberUtils.isNumber( widgetId ) ){
 		    widget = user.createNewWidget( productName, productVersion, title, youtubeVideoUrl, providerURL, recipeURL, consolename, consoleurl, rootpath );
         }else{
             Long widgetIdLong = Long.parseLong( widgetId );
             widget = Widget.findByUserAndId( user, widgetIdLong );
             if ( widget == null ){
                 new HeaderMessage().setError( "User is not allowed to edit this widget" ).apply( response().getHeaders() );
                 return badRequest(  );
             }
             widget.setProductName( productName );
             widget.setProductVersion( productVersion );
             widget.setTitle( title );
             widget.setYoutubeVideoUrl( youtubeVideoUrl );
             widget.setProviderURL( providerURL );
             widget.setRecipeURL( recipeURL );
             widget.setConsoleName( consolename );
             widget.setConsoleURL( consoleurl );
             widget.setRecipeRootPath( rootpath );
             widget.setRecipeName( recipeName );
             widget.setConsoleUrlService( consoleUrlService );
             widget.save();
         }
 
         logger.info( "edited widget : " + widget.toString() );
         return ok( Json.toJson(widget) );
 //		return resultAsJson(widget);
 	}
 
 	
 	public static Result getAllWidgets( String authToken )
 	{
 		User user = User.validateAuthToken(authToken);
 		List<Widget> list = null;
 
         if ( user.getSession().isAdmin() )   {
             list = Widget.find.all(); // Utils.workaround( Widget.find.all() );
 //            list = Utils.workaround( Widget.find.all() );
         }
         else {
             list = user.getWidgets();
         }
 
 
         ObjectMapper mapper = new ObjectMapper();
         mapper.getSerializationConfig().addMixInAnnotations( Widget.class, Widget.IncludeInstancesMixin.class );
         return ok( Json.toJson(list) );
 	}
 
 
 
 	public static Result shutdownInstance( String authToken, String instanceId )
 	{
 		User.validateAuthToken(authToken);
 		ApplicationContext.get().getWidgetServer().undeploy( ServerNode.getServerNode( instanceId )); // todo : link to user somehow
 		return ok(OK_STATUS).as("application/json");
 	}
 
 
     @With( UserCheck.class )
     public static Result listWidgets( Long userId, String authToken ){
         User user = ( User) ctx().args.get("user");
         return ok(Json.toJson(Widget.findByUser( user )));
     }
 
 	public static Result disableWidget( String authToken, String apiKey )
 	{
         return enableDisableWidget( authToken, apiKey, false );
 	}
 
     private static Result enableDisableWidget( String authToken, String apiKey, boolean enabled )
     {
         getWidgetSafely( authToken, apiKey ).setEnabled( enabled ).save();
         return ok(OK_STATUS).as("application/json");
     }
 
 
     public static Result enableWidget( String authToken, String apiKey )
 	{
         return enableDisableWidget( authToken, apiKey, true );
 	}
 
     private static Widget getWidgetSafely( String authToken, Long widgetId, boolean allowAdmin ){
         User user = User.validateAuthToken( authToken );
         if ( allowAdmin && user.isAdmin()){
             return Widget.find.byId( widgetId );
         }else{
             return Widget.findByUserAndId( user, widgetId );
         }
     }
 
     private static Widget getWidgetSafely( String authToken, String apiKey )
     {
 
         User user = User.validateAuthToken(authToken);
         if ( user.isAdmin() ){
              return Widget.getWidget(apiKey);
         }else{
             return Widget.getWidgetByApiKey(user, apiKey);
         }
     }
 
     public static Result getUserWidgetTemplate(){
         return ok( views.html.widgets.userWidgets.render() ); // we do this so we can get the embed code.. we cannot use angularJS as we might want to email it too..
     }
 
     public static Result deleteWidget( String authToken, String apiKey ){
         logger.info( "got a request to delete widget [{}]", apiKey );
         Widget widget = getWidgetSafely( authToken, apiKey );
         widget.delete(  );
         return ok( );
     }
 
 
 
     public static Result previewWidget( String apiKey ){
         Http.Cookie authTokenCookie = request().cookies().get("authToken");
 
         if ( authTokenCookie == null ){
              redirect("/");
         }
 
         String authToken = authTokenCookie.value();
         Widget widget = getWidgetSafely( authToken, apiKey );
         return ok( previewWidget.render(widget, request().host()));
     }
 	
 	public static Result regenerateWidgetApiKey( String authToken, String apiKey )
 	{
         Widget w = getWidgetSafely( authToken, apiKey ).regenerateApiKey();
         logger.info( "regenerated api key to [{}]", w );
         Map<String, Object> result = new HashMap<String, Object>(  );
         result.put("widget", w);
 		return ok( Json.toJson( result ) );
 	}
 
 	public static Result headers()
 	{
     	Http.Request req = Http.Context.current().request();
     	
     	StringBuilder sb = new StringBuilder("HEADERS:");
     	sb.append( "\nRemote address: " ).append( req.remoteAddress() );
     	
     	Map<String, String[]> headerMap = req.headers();
     	for (String headerKey : headerMap.keySet()) 
     	{
     	    for( String s : headerMap.get(headerKey) )
     	    	sb.append( "\n" ).append( headerKey ).append( "=" ).append( s );
     	}
 
     	return ok(sb.toString());
 	}
 
     public static Result postRequireLogin( String authToken, Long widgetId, boolean requireLogin,  String loginVerificationUrl, String webServiceKey ){
 
         Widget widget = getWidgetSafely( authToken, widgetId, false );
         if ( widget == null ){
             new HeaderMessage().setError(" User is not allowed to edit this widget ").apply(response().getHeaders());
             return badRequest();
         }
         GsConstraints.UrlValidator validator = new GsConstraints.UrlValidator();
         if ( !validator.isValid(loginVerificationUrl) ){
             new HeaderMessage().addFormError("loginVerificationUrl", "invalid value").apply(response().getHeaders());
             return badRequest();
         }
 
         widget.setRequireLogin( requireLogin );
         widget.setLoginVerificationUrl( loginVerificationUrl );
         widget.setWebServiceKey( webServiceKey );
         widget.save();
         return ok();
     }
 
     public static Result postWidgetDescription( String authToken, Long widgetId, String description ){
 
         Widget widget = getWidgetSafely( authToken, widgetId, false );
         if ( widget == null ){
             new HeaderMessage().setError(" User is not allowed to edit this widget ").apply(response().getHeaders());
             return badRequest();
         }
 
         widget.setDescription( description );
         widget.save(  );
         return ok(  );
 
     }
 
     public static Result newWidgetsPage(){
         return ok( angularjs_widget.render() );
     }
 
 }
