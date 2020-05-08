 package controllers;
 
 import models.BlobImage;
 import play.i18n.Lang;
 import play.mvc.Controller;
 
 import java.util.List;
 
 public class Application extends Controller {
 
 	public static void changeLang(String lang, String url) {
 		if (lang != null) {
 			Lang.change(lang);
 		}
 
 		redirect(url);
 	}
 
 	/**
 	 * Shows a list of all the created projects in the system
 	 */
 	public static void showImageList() {
 		List<BlobImage> images = BlobImage.findAll();
 
 		render("Application/imageList.html", images);
 	}
 

 	public static void homepage() {
 		render("homepage.html");
 	}
 	
 	public static void imageUploader(){
 		render("imageUploadTest.html");
 	}
 }
