 package models;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 
 import play.Logger;
 import play.data.validation.MaxSize;
 import play.db.jpa.Model;
 
 @Entity
 public class Configuration extends Model {
     
 
 	@Column(name="setting_type")
 	public String type;
 	public String look;
 	@MaxSize(2000)
 	@Column(name="text", length=2000)
 	public String welcome_text;
 	
 	public static String getTheme () {
 		Configuration set = find("byType", "General").first();
 		if (set == null) {
 			set = new Configuration();
 			set.type = "General";
 			set.look = "default";
 			set.save();
 		}
 		return set.look;
 	}
 	
 	public static List<String> getThemes() {
 		List<String> themes = new ArrayList<String>();
		File dir = new File("public/themes");
 		// The list of files can also be retrieved as File objects
 		File[] files = dir.listFiles();
 
 		// This filter only returns directories
 		FileFilter fileFilter = new FileFilter() {
 		    public boolean accept(File file) {
 		        return file.isDirectory();
 		    }
 		};
 		files = dir.listFiles(fileFilter);
 		
 		 for (int i=0; i<files.length; i++) {
 		        // Get filename of file or directory
 			 Logger.info("directory : %s", files[i].getName());
 		        themes.add(files[i].getName());
 		    }
 		return themes;
 		
 	}
 
 	
 }
