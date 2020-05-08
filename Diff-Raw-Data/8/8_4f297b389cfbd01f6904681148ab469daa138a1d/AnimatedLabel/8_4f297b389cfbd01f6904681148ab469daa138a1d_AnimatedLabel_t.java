 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package laboonrace;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 
 /**
  *
  * @author eddy
  */
 public class AnimatedLabel extends JLabel {
     
    protected int imageIndex = 0;
     private int fromIndex = 0;
     private int toIndex = 0;
     private boolean repeat = false;
     private boolean finished = false;
     private ArrayList<ImageIcon> images = new ArrayList<>();
     
     public AnimatedLabel() {
         setDoubleBuffered(true);
     }
     
     public void initImages(ArrayList<ImageIcon> images){
         this.images.clear();
         this.images = (ArrayList<ImageIcon>) images.clone();
     }
     
     public void start(int fromIndex, int toIndex, boolean repeat){
         this.fromIndex = fromIndex;
         this.toIndex = toIndex;
         this.repeat = repeat;
         setFinished(false);
         setIndex(fromIndex);
     }
     
     public boolean isFinished() {
         return finished;
     }
     public void setFinished(boolean bool) {
         finished = bool;
     }
     
     public int getNumberOfImages() {
         return images.size();
     }
    
     public int getIndex() {
         return imageIndex;
     }
     
     public void setIndex(int index) {
         if(index >= 0 && index < images.size()) {
             imageIndex = index;
         }
     }
     
     public static ArrayList<ImageIcon> getImages(String imageName, Dimension size) {
         ArrayList<ImageIcon> newImages = new ArrayList<>();
         ArrayList<String> allNames = new ArrayList<>();
         try {
             allNames = getResourcesNamesFromPackage(imageName);
         } catch (IOException ex) {
             Logger.getLogger(AnimatedLabel.class.getName()).log(Level.SEVERE, 
                     null, ex);
         }
         
         Collections.sort(allNames);
         for(String fileName : allNames) {
             java.net.URL imageURL = Main.class.getResource("images/" + fileName);
             if (imageURL != null) {
                 ImageIcon icon = new ImageIcon(imageURL);
                 Image img = icon.getImage();
                 Image newimg = img.getScaledInstance( size.width, size.height,
                         Image.SCALE_SMOOTH ) ;
                 icon = new ImageIcon(newimg);
                 newImages.add(icon);
             }
         }
         return newImages;
     }
     public void setImages(ArrayList<ImageIcon> images) {
         this.images = images;
     }
     
     ////////////////////////////////////////////////////////////////////////////
     
     protected void updateIcon() {
         if(!isFinished()) {
             updateImage();
             updateIndex();
         }
     }
     
     private void updateImage() {
         if(imageIndex < images.size() && imageIndex >= 0) {
             ImageIcon icon = images.get(imageIndex);
             setImage(icon);
         }
     }
     
     private void updateIndex() {
         if(imageIndex == toIndex) {
             if(repeat) {
                 setIndex(fromIndex);
             } else {
                 setFinished(true);
             }
         } else if(fromIndex < toIndex){
             setIndex(++imageIndex);
         } else if(fromIndex > toIndex) {
             setIndex(--imageIndex);
         }
     }
     
     private void setImage(ImageIcon icon) {
         setIcon(icon);
         setSize(icon.getIconWidth(), icon.getIconHeight());
         revalidate();
     }
     
     private static ArrayList<String>getResourcesNamesFromPackage(String name) 
             throws IOException{
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         URL packageURL;
         ArrayList<String> names = new ArrayList<>();
         packageURL = classLoader.getResource("laboonrace/images");
 
         if(packageURL.getProtocol().equals("jar")) {
             String jarFileName;
             JarFile jf ;
             Enumeration<JarEntry> jarEntries;
 
             // build jar file name, then loop through zipped entries
             jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
             jarFileName = jarFileName.substring(5,jarFileName.indexOf("!"));
             jf = new JarFile(jarFileName);
             jarEntries = jf.entries();
             while(jarEntries.hasMoreElements()){
                 String entryName = jarEntries.nextElement().getName();
                 entryName = entryName.substring(entryName.lastIndexOf("/")+1);
                 if(Pattern.matches("^.*" + name + "-\\d+.png", entryName)) {
                     names.add(entryName);
                 }
             }
 
         // loop through files in classpath
         } else {
             File folder = new File(URLDecoder.decode(packageURL.getFile(), 
                     "UTF-8"));
             File[] contenuti = folder.listFiles();
             for(File actual: contenuti){
                 String entryName = actual.getName();
                 if(Pattern.matches("^.*" + name + "-\\d+.png", entryName)) {
                     names.add(entryName);
                 }
             }
         }
         return names;
     }
 
 }
