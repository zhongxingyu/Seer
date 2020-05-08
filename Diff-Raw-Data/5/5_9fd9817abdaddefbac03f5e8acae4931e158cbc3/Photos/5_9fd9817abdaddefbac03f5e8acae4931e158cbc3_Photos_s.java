  package controllers;
 
  import com.amazonaws.auth.BasicAWSCredentials;
  import com.amazonaws.services.s3.AmazonS3;
  import com.amazonaws.services.s3.AmazonS3Client;
  import com.amazonaws.services.s3.model.CannedAccessControlList;
  import com.amazonaws.services.s3.model.ObjectMetadata;
  import com.amazonaws.services.s3.model.PutObjectRequest;
 
  import models.User;
  import play.Logger;
  import play.Play;
  import play.data.validation.Valid;
  import play.mvc.Before;
  import play.mvc.Controller;
 
  import java.io.*;
  import java.util.List;
 
 
 
 
 
  public class Photos extends Controller {
      @Before
      static void addUser() {
          User user = connected();
          if(user != null) {
              renderArgs.put("user", user);
          }
      }
 
      private static long MAX_SIZE = 2621440;
 
      static User connected() {
          if(renderArgs.get("user") != null) {
              return renderArgs.get("user", User.class);
          }
          String email = session.get("user");
          if(email != null) {
              return User.find("byEmail", email).first();
          }
          return null;
      }
 
      public static void index() {
          if(connected() != null) {
              User user = connected();
              render(user);
          }
          flash.error("Please log in first to view photos.");
          Application.login_page();
      }
      public static void addAlbum(){
          User user = connected();
          render(user);
      }
 
      public static void uploadPhoto(File photo) {
          User user = connected();
 
          String mimeType = play.libs.MimeTypes.getMimeType(photo.getAbsolutePath());
          if(!mimeType.equals("image/jpeg") && !mimeType.equals("image/gif") && !mimeType.equals("image/png")){
              flash.error("File is not jpg, gif, or png. Please upload this type of file.");
              render("@profile", user, photo);
          }
          if(photo.length()>MAX_SIZE){
              flash.error("File is too large, must be less than 2.5mb");
              render("@profile", user, photo);
          }
          flash.error("");
          String accessKey = "AKIAIIDVPNAYFEVBVVFA";
          String secretKey = "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";
          AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
          String key = "profile"+user.id;
          s3.putObject(new PutObjectRequest("globafitnessphotos", key, photo).withCannedAcl(CannedAccessControlList.PublicRead));
          flash.success("You have successfully uploaded a photo");
          render("@profile",user);
      }
 
      public static void upload(String qqfile){
          if(request.isNew) {
 
              //FileOutputStream moveTo = null;
 
              Logger.info("Name of the file %s", qqfile);
              // Another way I used to grab the name of the file
             String filename = request.headers.get("x-file-name").value();
 
              Logger.info("Absolute on where to send %s", Play.getFile("").getAbsolutePath() + File.separator + "uploads" + File.separator);
              try {
 
                  InputStream data = request.body;
 
                  String accessKey = "AKIAIIDVPNAYFEVBVVFA";
                  String secretKey = "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";
                  AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
                 String key = ""+filename;
                  s3.putObject(new PutObjectRequest("globafitnessphotos", key, data, new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead));
                  //s3.putObject(new PutObjectRequest("globafitnessphotos", key, photo).withCannedAcl(CannedAccessControlList.PublicRead));
 
                  //moveTo = new FileOutputStream(new File(Play.getFile("").getAbsolutePath()) + File.separator + "uploads" + File.separator + filename );
                  //IOUtils.copy(data, moveTo);
 
              } catch(Exception ex) {
 
                  // catch file exception
                  // catch IO Exception later on
                  renderJSON("{success: false}");
              }
 
          }
          renderJSON("{success: true}");
      }
  }
 
 
