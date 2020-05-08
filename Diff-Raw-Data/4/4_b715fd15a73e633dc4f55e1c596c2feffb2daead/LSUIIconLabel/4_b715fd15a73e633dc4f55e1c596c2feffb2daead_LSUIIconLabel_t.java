 package rocks6205.editor.viewcomponents;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
import rocks6205.system.properties.LSEditorGUIConstants;
 
 /**
  * Custom JLabel with icon.
  * 
  * @author Cheow Yeong Chi
  * 
  * @since 2.2
  */
 public class LSUIIconLabel extends JLabel {
     public LSUIIconLabel() {}
 
     public LSUIIconLabel(String imagePath) {
         super(createIcon(imagePath));
     }
 
     /*
      * METHODS
      */
 
     /**
      * Creates an <code>ImageIcon</code> instance from a image path <code>path</code>
      * provided.
      *
      * @param iconName Name to icon image
      * @return <code>ImageIcon</code> object
      */
     private static ImageIcon createIcon(String iconName) {
        String    string = LSEditorGUIConstants.DEFAULT_PATH_TO_TOOLBAR_ICONS + iconName;
         ImageIcon icon   = new ImageIcon(string);
 
         return icon;
     }
 
     /**
      * Sets the button's default icon with name to icon image <code>iconName</code>.
      *
      * @param iconPath Name to icon image
      */
     public void setIcon(String iconName) {
         ImageIcon icon = createIcon(iconName);
 
         super.setIcon(icon);
     }
 }
