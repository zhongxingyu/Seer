 package controllers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import models.MyGroup;
 import models.TPicture;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 
 import play.Play;
 import play.mvc.Controller;
 
 public class TPictures extends Controller {
 
 	public static String filepath = "./public/upload/";
 	public static String filename = "";
 
 	public static void index() {
 		render();
 	}
 
 	public static void upload(String qqfile) {
 		
 		FileOutputStream moveTo = null;
 		String appendedFileName ="";
 		
 		Long group_id = params.get("grpid", Long.class);
 		MyGroup group = MyGroup.findById(group_id);
 		
		//if (request.isNew) {
 			filename = request.headers.get("x-file-name").value();
 			filename = filename.replaceAll("%20", "_");
 			filename = filename.replaceAll(" ", "_");
 			appendedFileName = group.name + "_" + filename;
 			try {
 				InputStream data = request.body;
 				moveTo = new FileOutputStream(filepath + appendedFileName);
 				IOUtils.copy(data, moveTo);
 			}
 			catch (Exception ex) {
 				// catch file exception
 				// catch IO Exception later on
 				renderJSON("{success: false}");
 			}
	//	}
 		
 		group.addNewPicture(group, appendedFileName);
 		renderJSON("{success: true}");
 	}
 
 	public static void rupload(String qqfile) {
 
 		if (request.isNew) {
 
 			FileOutputStream moveTo = null;
 
 			// Logger.info("Name of the file %s", qqfile);
 			// Another way I used to grab the name of the file
 			String filename = request.headers.get("x-file-name").value();
 
 			// Logger.info("Absolute on where to send %s",
 			// Play.getFile("").getAbsolutePath() + File.separator + "uploads" +
 			// File.separator);
 			try {
 
 				InputStream data = request.body;
 				moveTo = new FileOutputStream("./public/upload/" + filename);
 				IOUtils.copy(data, moveTo);
 
 			} catch (Exception ex) {
 
 				// catch file exception
 				// catch IO Exception later on
 				renderJSON("{success: false}");
 			}
 
 		}
 
 		renderJSON("{success: true}");
 	}
 }
