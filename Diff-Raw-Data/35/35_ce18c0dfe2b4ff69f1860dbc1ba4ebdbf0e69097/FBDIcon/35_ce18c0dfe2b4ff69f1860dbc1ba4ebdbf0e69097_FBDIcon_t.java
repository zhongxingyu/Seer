 /*
  * FBDIcon.java
  *
  * Created on June 22, 2007, 3:59 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.gatech.statics.application.ui;
 
 import com.jme.image.Texture;
 import com.jmex.bui.icon.BIcon;
 import com.jmex.bui.BContainer;
 import com.jmex.bui.BImage;
 import com.jmex.bui.BLabel;
 import com.jmex.bui.icon.ImageIcon;
 import com.jmex.bui.layout.BorderLayout;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class FBDIcon extends BContainer {
     
     //private static Texture tex = TextureManager.loadTexture(
     //            FBDIcon.class.getClassLoader().getResource("rsrc/FBD_Interface/cable.png"),
     //            Texture.MM_LINEAR, Texture.FM_LINEAR);
 
     //private Texture tex;
     private BLabel label;
     private BImage image;
     
     /** Creates a new instance of FBDIcon */
    public FBDIcon(BImage image) {//(Texture tex) {
         setStyleClass("info_window");
         setPreferredSize(90, 90);
        this.image = image;
 
         //Texture tex = TextureManager.loadTexture(
         //        getClass().getClassLoader().getResource("rsrc/FBD_Interface/cable.png"),
         //        Texture.MM_LINEAR, Texture.FM_LINEAR);
 
        //image = new BImage(tex, 90, 90, 128, 128);
         BIcon icon = new ImageIcon(image);
         label = new BLabel(icon);
         //new BLabel("FBD "+fbd.getBodies());
         add(label, BorderLayout.CENTER);
         label.setBounds(0, 0, 90, 90);
     }
     
     public void update() {
         image.updateRenderState();
         label.invalidate();
         /*if(tRenderer.isSupported()) {
             
             tRenderer.setupTexture(tex);
             tRenderer.setCamera(StaticsApplication.getApp().getCamera());
             //tRenderer.setBackgroundColor(ColorRGBA.randomColor());
             
             //fbd.updateNodes();
             tRenderer.render(fbd.getNode(RepresentationLayer.modelBodies), tex);
             image.updateRenderState();
             //label.invalidate();
         }*/
     }
 }
