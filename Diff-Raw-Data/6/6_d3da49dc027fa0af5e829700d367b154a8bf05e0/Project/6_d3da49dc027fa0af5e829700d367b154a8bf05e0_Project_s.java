 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.laukvik.photogallery;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Vector;
 import javax.swing.ImageIcon;
 
 /**
  *
  * @author morten
  */
 public class Project {
 
     public String nr;
     public String title;
     public String type;
     public String date;
     public String description;
     public String brief;
     public String link;
 //    public String surl;
     public URL thumbnail;
     ImageIcon icon;
             
     Vector<Photo> photos;
     Gallery gallery;
     
     
     public Project() {
         this.photos = new Vector<Photo>();
     }
     
     public ImageIcon getIcon(){
        if (icon == null){
             this.icon = new ImageIcon( thumbnail );
             this.icon.getImage();
            
         }
         return icon;
     }
     
     public void add( Photo photo ){
         photo.project = this;
         this.photos.add( photo );
     }
     
     public void remove( Photo photo ){
         if (photo.project == this){
             photo.project = null;
         }
         this.photos.remove( photo );
     }
     
     public void removeAllPhotos(){
         this.photos.removeAllElements();
     }
     
     
 }
