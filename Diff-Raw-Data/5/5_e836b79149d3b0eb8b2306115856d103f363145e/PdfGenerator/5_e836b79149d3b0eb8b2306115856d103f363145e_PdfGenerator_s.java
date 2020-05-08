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
 
     private static final String GENERATED_PATH = "/generated/";
 
     private static final String GENERATED_EXTENSION = ".pdf";
 
     protected Font textBoldFont;
     protected Font textFont;
     protected Font titleFont;
 
     protected String rootPath;
 
     public PdfGenerator() {
 
         rootPath = Play.configuration.getProperty("my.pdf.resources.path");
 
         String mode = Play.configuration.getProperty("application.mode");
 
         if (mode.equals("dev")) {
            rootPath = Play.applicationPath.getPath() + "/pdf/resources/";
         } else {
            rootPath = "app/pdf/resources/";
         }
 
         FontFactory.register(new File(rootPath + "ARIALN.ttf").getPath(), "arialnarrow_normal");
         FontFactory.register(new File(rootPath + "ARIALNB.ttf").getPath(), "arialnarrow_bold");
 
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
 
         return new File(rootPath + "supinfo_logo.png");
     }
 }
