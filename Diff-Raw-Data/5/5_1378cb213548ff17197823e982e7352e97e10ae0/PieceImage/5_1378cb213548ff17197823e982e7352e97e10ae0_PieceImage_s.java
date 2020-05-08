 package models;
 
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.validation.*;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import net.coobird.thumbnailator.Thumbnails;
 
 @SuppressWarnings("serial")
 @Entity
 public class PieceImage extends Model {
 
     @Id
     @Constraints.Required
     public Long id;
 
     @Constraints.Required
     public String name;
 
     @Constraints.Required
     public String description;
 
     @Constraints.Required
     public String focus;
 
     @Constraints.Required
     public String url;
 
     @Constraints.Required
     public String thumbnail;
 
     @ManyToOne
     @Constraints.Required
     public Piece piece;
     
     public void setUrl(final String url){
     	this.url = url;
     	this.thumbnail = "public/images/thumbnail_"+this.name+".png";
     	
     	new Thread(new Runnable() {
             public void run(){
             	BufferedImage image =null;
                 try{
                    image = ImageIO.read(new URL(url));
                   File fold=new File("public/images/thumbnail_"+name+".png");
                    fold.delete();
                    Thumbnails.of(image)
         		       .size(60, 60)
        		       .toFile("public/images/thumbnail_"+name+".png");
                 }catch(IOException e){
                     e.printStackTrace();
                 }
             }
         }).start();
     }
 
     public static Finder<Long,PieceImage> find = new Finder<>(
             Long.class, PieceImage.class
     );
 
 }
