 package com.roscopeco.scratch.codegen;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 
 import com.roscopeco.scratch.io.objects.Form;
 import com.roscopeco.scratch.io.objects.ImageMedia;
 import com.roscopeco.scratch.io.objects.ScratchMedia;
 import com.roscopeco.scratch.io.objects.ScratchSpriteMorph;
 import com.roscopeco.scratch.io.objects.ScratchStageMorph;
 import com.roscopeco.scratch.io.objects.ScriptableScratchMorph;
 import com.roscopeco.scratch.io.objects.SoundMedia;
 
/**
 * As well as generating the resource files, this also generates
 * the public static final vars in the Sprite classes with the
 * resource handles.
 * 
 * @author rosco
 */
 public class ResourceGenerator {
   private final String resDir;
   private final String codeDir;
   private final ScratchStageMorph stage;
   private final ScratchSpriteMorph sprite;
   private final boolean isStage;
   
   // Used to generate the array of costumes instance variable. 
   // written after all sprites are processed.
   private final StringBuilder arraySb;
   
   ResourceGenerator(String resDir, String codeDir, ScratchStageMorph stage) {
     this.resDir = resDir;
     this.codeDir = codeDir;
     this.stage = stage;
     this.sprite = null;
     this.isStage = true;
     this.arraySb = new StringBuilder();
   }
 
   ResourceGenerator(String resDir, String codeDir, ScratchSpriteMorph sprite) {
     this.resDir = resDir;
     this.codeDir = codeDir;
     this.stage = null;
     this.sprite = sprite;
     this.isStage = false;
     this.arraySb = new StringBuilder();    
   }
 
   /**
    * Generate the resources. The mainSb parameter should be the 
    * StringBuilder being used to generate the Sprite/Stage main
    * class.
    * 
    * @param mainSb
    */
   public void generate(StringBuilder mainSb) throws IOException {
     File resDirFile = new File(resDir);
     File codeDirFile = new File(codeDir);
     resDirFile.mkdirs();
     codeDirFile.mkdirs();
     
     if (isStage) {
       generate(mainSb, stage, resDirFile, codeDirFile);
     } else {
       generate(mainSb, sprite, resDirFile, codeDirFile);
     }
     mainSb.append("\n");
     
     mainSb.append("  Costume[] costumes = new Costume[] {\n");
     mainSb.append(arraySb);
     mainSb.append("  };\n\n");
   }
   
   void generate(StringBuilder mainSb, ScriptableScratchMorph obj, File resDirFile, File codeDirFile) 
   throws IOException {
     File imgDir = new File(resDirFile, "drawable");
     File sndDir = new File(resDirFile, "raw");
     
     imgDir.mkdirs();
     sndDir.mkdirs();
     
     for (int i = 0; i < obj.media().size(); i++) {
       ScratchMedia media = obj.media().get(i);
       if (media instanceof ImageMedia) {
         processImage(i, mainSb, obj.objName().toString(), (ImageMedia)media, imgDir);
       } else if (media instanceof SoundMedia)  {
         processSound(mainSb, obj.objName().toString(), (SoundMedia)media, sndDir);        
       } else {
         throw new IllegalArgumentException("Encountered an unknown media type (" + media.mediaName() + " : "  + media.getClass().getName());
       }
     }    
   }
   
   static final Pattern NOTAZ09USCORE = Pattern.compile("[^a-z0-9_]");
   
   String imageFilename(String objName, String mediaName, String ext) {
     return NOTAZ09USCORE.matcher(objName.toLowerCase()).replaceAll("_") + "_" +
            NOTAZ09USCORE.matcher(mediaName.toLowerCase()).replaceAll("_") + ext;
   }
   
   void processImage(int index, StringBuilder mainSb, String objName, ImageMedia media, File dir) throws IOException {
    String varname = ObjectCodeGenerator.scratchNameToIdentifier(media.mediaName().toString(), false);
     String imageName = null;
     
     // Write the image file
     if (media.jpegBytes() != null) {
       // Write JPEG
       imageName = imageFilename(objName, media.mediaName().toString(), ".jpg");
       FileOutputStream fos = null;
       try {
         fos = new FileOutputStream(new File(dir, imageName));
         fos.write(media.jpegBytes().bytes());
       } finally {
         if (fos != null) fos.close();
       }
     } else {
       Form f;
     
       if (media.compositeForm() != null) {
         f = media.compositeForm();
       } else if (media.form() != null) { 
         f = media.form();
       } else {
         throw new UnsupportedOperationException("Media with unsupported image type (" + media.mediaName() + ")");      
       }      
 
       imageName = imageFilename(objName, media.mediaName().toString(), ".png");
       BufferedImage img = f.getImage();
       if (img == null) {
         System.err.println("Warning: no image for " + media.mediaName());
       } else {
         File imgFile = new File(dir, imageName);
         ImageIO.write(img, "png", imgFile);
       }
     }
 
     if (imageName != null) {
       // Add code to load the image in the object's class.    
       mainSb.append("  public static final Costume ").
           append(varname).
           append(" = MediaManager.instance().loadImage(").
               append(index + 1).
               append(", \"").append(media.mediaName().toString()).append("\", \"").
               append(imageName).append("\");\n");
       
       // Add this element to the array instance var
       arraySb.append("    ").append(varname).append(",\n");
     }
   }
   
   void processSound(StringBuilder mainSb, String objName, SoundMedia media, File dir) {
     // TODO not yet implemented
     mainSb.append("  // Skipped sound: ").append(media.mediaName()).append("\n");
   }
 }
