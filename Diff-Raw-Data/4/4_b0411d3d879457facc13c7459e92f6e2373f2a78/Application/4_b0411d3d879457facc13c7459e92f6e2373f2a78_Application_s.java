 package controllers;
 
 import dataHelpers.ProfileData;
 import models.*;
 import org.codehaus.jackson.node.ObjectNode;
 import play.api.mvc.*;
 import play.data.DynamicForm;
 import play.mvc.*;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.BodyParser;
 import play.libs.Json;
 import java.util.List;
 
 import static play.data.Form.*;
 
 
 public class Application extends Controller {
 
     public static Result index() {
         if ( session("sessionUser") != null){
             return redirect( routes.Application.home() );
         }
         List<User> artistas = User.getArtistas();
         ObjectNode allArtistas = Json.newObject();
         allArtistas.put("allArtistas", Json.toJson( artistas ));
         //System.out.print(allArtistas);
         String artistasAsJson = allArtistas.toString();
         return ok(views.html.index.render( artistasAsJson ));
     }
 
     public static Result home(){
         if ( session("sessionUser") == null){
 
             return  redirect( routes.Application.index());
         }
         List<Feed> feeds = Feed.getFeeds();
         String feedsAsJson =  Json.toJson( feeds ).toString();
         return ok( views.html.home.home.render(  feedsAsJson ));
     }
 
     public static Result signOut(){
         session().remove("sessionUser");
         session().remove("currentUserId");
         return redirect("/");
 
     }
    public static Result artistas() {
        List<User> artistas = User.getArtistas();
        ObjectNode allArtistas = Json.newObject();
        allArtistas.put("allArtistas", Json.toJson( artistas ));
        //System.out.print(allArtistas);
        String artistasAsJson = allArtistas.toString();
        //return ok( Json.toJson( allArtistas ));
        //return ok( allArtistas );
        return ok( views.html.artistas.render( artistasAsJson ) );
     }
 
     public static Result deleteArtista( Long id){
         System.out.println( "User Id: " + id);
          User.deleteArtista( id );
         return redirect( routes.Application.artistas() );
     }
 
     public static Result newArtista() {
         User.createArtista();
         return redirect( routes.Application.artistas() );
     }
     public static Result byName( String name){
         List<User> artistas = User.findByName( name );
         ObjectNode allArtistas = Json.newObject();
         allArtistas.put("all artistas Found with Name: " + name, Json.toJson( artistas ));
         return ok( Json.toJson( allArtistas ));
     }
 
     public static Result searchArtistas( String q ){
         List<User> artistas = User.findByName( q );
         ObjectNode searchResult = Json.newObject();
         searchResult.put( "searchResult", Json.toJson( artistas ));
         return ok( Json.toJson( searchResult ));
     }
 
    public static Result profile( String userName ){
        User user = User.findUerByUserName( userName );
        ProfileData profileData = new ProfileData( user );
        //return ok( views.html.profile.profile.render( profileData.toString() ));
        return ok( views.html.profile.profile.render( Json.toJson( profileData ).toString() ));
    }
 
     //
     public static Result myProfile(){
 
         if ( session("sessionUser") != null){
 
            return  ok( session("user"));
         }
         else {
            return  ok(" no session -  not logged in");
         }
     }
     //
     public static Result myPhotos(){
         User u = User.findUserById( session("currentUserId") );
         if ( session("sessionUser") != null){
             List<MyPhoto> myphotos = MyPhoto.getMyPhotos( u.getId());
             return  ok( views.html.profile.myphotos.render( Json.toJson( myphotos ).toString() ));
             //return  ok( session("user"));
         }
         else {
             return  ok(" no session -  not logged in");
         }
     }
 
     public static Result myWidget( String userName){
         User u = User.findUerByUserName( userName );
         ProfileData profileData = new ProfileData( u );
         //return ok( views.html.profile.profile.render( profileData.toString() ));
 
         return  ok( views.html.widget.mywidget.render( Json.toJson( profileData ).toString() ));
 
     }
 
     public static Result myVideos(){
         User u = User.findUserById(session("currentUserId"));
         if ( session("sessionUser") != null){
             List<Video> myvideos = Video.getMyVideos( u.getId());
             return  ok( views.html.profile.myvideos.render( Json.toJson( myvideos ).toString() ));
             //return  ok( session("user"));
         }
         else {
             return  ok(" no session -  not logged in");
         }
     }
 
     public static Result addComment(){
         DynamicForm requestData = form().bindFromRequest();
         User u = User.findUserById(session("currentUserId"));
         String dataType = requestData.get("dataType");
         String photoId = requestData.get( "dataId");
         String comment = requestData.get("comment");
 
         if ( dataType.equals("profileImage")){
             System.out.println(" Ite is profile Image ");
              ProfileImageComment profileImagecomment = new ProfileImageComment( u, comment);
              ProfileImage profileImage = ProfileImage.findMyProfilePhotoById( photoId );
              profileImagecomment.setMyphoto( profileImage );
              profileImagecomment.save();
             return ok( Json.toJson( profileImagecomment ));
         }
         else {
              Comment myPhotoComment = new Comment( u, comment );
              MyPhoto myphoto = MyPhoto.findMyPhotoById( photoId );
              myPhotoComment.setMyphoto( myphoto );
              myPhotoComment.save();
              return ok( Json.toJson( myPhotoComment));
         }
 
         //return ok( Json.toJson( Comment.getCommentsByMyPhoto( myphotoId )));
         //return ok( Json.toJson( myComment));
     }
 
     public static Result getComments( String myphotoId){
         MyPhoto myphoto = MyPhoto.findMyPhotoById( myphotoId );
         List <Comment> comments = Comment.getCommentsByMyPhoto( myphotoId);
         return ok( Json.toJson( comments ));
 
     }
     // Get Comments for profile Images
     public static Result getProfileImageComment( String imageId){
         ProfileImage profileImage = ProfileImage.findMyProfilePhotoById( imageId );
         List <ProfileImageComment> comments = ProfileImageComment.getCommentsByMyProfilePhoto( imageId );
         return ok( Json.toJson( comments ));
 
     }
 
 
 }
   
 
