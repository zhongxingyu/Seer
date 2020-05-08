 package kniemkiewicz.jqblocks.ingame.renderer;
 
 import kniemkiewicz.jqblocks.ingame.PointOfView;
import kniemkiewicz.jqblocks.util.GeometryUtils;
 import org.newdawn.slick.Image;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 
 /**
  * User: krzysiek
  * Date: 23.09.12
  */
 @Component
 public class BackgroundRenderer {
 
   @Resource
   Image skyBackgroundImage;
 
   @Autowired
   PointOfView pointOfView;
 
   public void render() {
    int dx = (-pointOfView.getShiftX() / 8) % pointOfView.getWindowWidth();
    if (dx < 0) {
      dx += pointOfView.getWindowWidth();
    }
     skyBackgroundImage.draw(dx - pointOfView.getWindowWidth(),0, pointOfView.getWindowWidth(), pointOfView.getWindowHeight());
     skyBackgroundImage.draw(dx,0, pointOfView.getWindowWidth(), pointOfView.getWindowHeight());
   }
 }
