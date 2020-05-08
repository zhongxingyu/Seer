 package amber.gui.editor.map.tool._2d;
 
 import amber.gui.editor.map.MapContext;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import javax.swing.JSlider;
 import static javax.swing.SwingConstants.HORIZONTAL;
 import org.lwjgl.input.Keyboard;
 
 /**
  *
  * @author Tudor
  */
 public class Eraser2D extends AbstractTool2D {
 
     private int size = 1;
 
     public Eraser2D(MapContext context) {
         super(context);
     }
 
     public boolean apply(int x, int y) {
         boolean modified = false;
         for (int w = 0; w != size; w++) {
             for (int h = 0; h != size; h++) {
                if (isInBounds(x + w, y + h)) {
                    modified |= context.map.getLayer(context.layer).getTile(x + w, y + h, 0) != null;
                    context.map.getLayer(context.layer).setTile(x + w, y + h, 0, null);
                }
             }
         }
         return modified;
     }
 
     @Override
     public void doKey(int keycode) {
         switch (keycode) {
             case Keyboard.KEY_ADD:
                 size++;
                 break;
             case Keyboard.KEY_SUBTRACT:
                 if (size > 1) {
                     size--;
                 }
                 break;
         }
     }
 
     @Override
     public Dimension getDrawRectangleSize() {
         return new Dimension(size, size);
     }
 
     @Override
     public BufferedImage getPreview() {
         BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
         image.setRGB(0, 0, 0x00FFFFFF);
         return image;
     }
 
     public Component getContextComponent() {
         JSlider sizeControl = new JSlider(HORIZONTAL, 0, Math.max(context.map.getWidth(), context.map.getLength()), 0);
         return sizeControl;
     }
 }
