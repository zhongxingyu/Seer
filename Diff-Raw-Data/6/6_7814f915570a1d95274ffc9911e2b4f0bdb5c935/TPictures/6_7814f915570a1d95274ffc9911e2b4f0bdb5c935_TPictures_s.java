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
 		String filename = "";
 		if (request.isNew) {
 
 			FileOutputStream moveTo = null;
 
 			// Logger.info("Name of the file %s", qqfile);
 			// Another way I used to grab the name of the file
 			filename = request.headers.get("x-file-name").value();
 
 			// Logger.info("Absolute on where to send %s",
 			// Play.getFile("").getAbsolutePath() + File.separator + "uploads" +
 			// File.separator);
 			try {
 
 				InputStream data = request.body;
 				moveTo = new FileOutputStream("./public/upload/"+ filename);
 				IOUtils.copy(data, moveTo);
 
 			} catch (Exception ex) {
 
 				// catch file exception
 				// catch IO Exception later on
 				renderJSON("{success: false}");
 			}
 		}
 		Long group_id = params.get("grpid", Long.class);
 		MyGroup grp = MyGroup.findById(group_id);
 		filename = filename.replaceAll("%20", "_");
 		String appendedFileName = grp.name + "_" + filename;
 		grp.addNewPicture(grp, appendedFileName);
 		renderJSON("{success: true}");
 	}
 }
