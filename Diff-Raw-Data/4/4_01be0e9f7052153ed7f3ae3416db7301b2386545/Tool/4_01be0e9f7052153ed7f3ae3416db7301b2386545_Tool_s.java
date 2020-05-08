 import java.awt.Insets;
 import java.io.File;
 
 import javax.swing.AbstractButton;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JToggleButton;
 
 public enum Tool {
     PEN, STAMP;
 
     private AbstractButton button;
     private static final int BUTTON_MARGIN = 5;
 
     private Tool() {
        Icon icon = new ImageIcon("resources" + File.separator + this.name()
                + ".png");
 
         button = new JToggleButton(icon);
 
         button.setMargin(new Insets(BUTTON_MARGIN, BUTTON_MARGIN,
                 BUTTON_MARGIN, BUTTON_MARGIN));
     }
 
     public AbstractButton getButton() {
         if (this.name().equals(PEN.name())) {
             button.setSelected(true);
         }
         return button;
     }
 
     public String toString() {
         String word = name();
         return word.charAt(0) + word.substring(1).toLowerCase();
     }
 }
