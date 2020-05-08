 package pdf;
 
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import play.Play;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Date;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public abstract class PdfGenerator {
 
     private static final String RESOURCES_PATH = "/app/pdf/resources/";
 
     private static final String GENERATED_PATH = "/generated/";
 
     private static final String GENERATED_EXTENSION = ".pdf";
 
     protected Font textBoldFont;
     protected Font textFont;
     protected Font titleFont;
 
     public PdfGenerator() {
 
         try {
             FontFactory.register(Play.getFile(RESOURCES_PATH + "ARIALN.ttf").getPath(), "arialnarrow_normal");
             FontFactory.register(Play.getFile(RESOURCES_PATH + "ARIALNB.ttf").getPath(), "arialnarrow_bold");
         } catch (Exception e) {
             // Heroku fix
            FontFactory.register(Play.getFile(RESOURCES_PATH.substring(3) + "ARIALN.ttf").getPath(), "arialnarrow_normal");
            FontFactory.register(Play.getFile(RESOURCES_PATH.substring(3) + "ARIALNB.ttf").getPath(), "arialnarrow_bold");
         }
 
         textBoldFont = FontFactory.getFont("arialnarrow_bold", 8);
         textFont = FontFactory.getFont("arialnarrow_normal", 8);
         titleFont = FontFactory.getFont("arialnarrow_bold", 14);
     }
 
     protected File getFileForGeneration(String folder, String name) {
 
         File folderFile = new File(GENERATED_PATH + folder);
         if (!folderFile.exists()) {
             folderFile.mkdirs();
         }
 
         return new File(folderFile, name + GENERATED_EXTENSION);
     }
 
     protected File getSupinfoLogo() {
 
         try {
             return Play.getFile(RESOURCES_PATH + "supinfo_logo.png");
         } catch (Exception e) {
             // Heroku fix
            return Play.getFile(RESOURCES_PATH.substring(3) + "supinfo_logo.png");
         }

     }
 }
