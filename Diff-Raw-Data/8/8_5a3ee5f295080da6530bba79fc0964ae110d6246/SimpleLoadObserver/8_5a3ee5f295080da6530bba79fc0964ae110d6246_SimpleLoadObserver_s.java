package game;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import com.golden.gamedev.object.SpriteGroup;
 
 import gameObjects.GameObject;
 import levelLoadSave.LoadObserver;
 /**
  * 
  * @author antaresyee
  *
  */
 
 
 public class SimpleLoadObserver extends LoadObserver {
 
     SpriteGroup mySpriteGroup;
     
     public SimpleLoadObserver(SpriteGroup spriteGroup) {
         super.myType = "Barrier";
         mySpriteGroup = spriteGroup;
     }
     
     @Override
     public void objectLoaded(GameObject go) {
         mySpriteGroup.add(go);
 
         //set sprite image
         try {
             File imgFile = new File(go.getImgPath());
             BufferedImage img = ImageIO.read(imgFile);
             go.setImage(img);
         } catch (IOException e) {
             e.printStackTrace();
         }
         
     }
 
 }
