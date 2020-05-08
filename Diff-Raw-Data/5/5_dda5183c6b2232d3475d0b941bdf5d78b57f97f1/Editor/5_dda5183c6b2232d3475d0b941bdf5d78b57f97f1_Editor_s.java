 package GUI;
 
 import Models.WorldModel;
 import java.awt.Dimension;
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * Wrapper class for the EditorPanel and EditorList
  *
  * @author FlashYoshi
  */
 public class Editor {
 
     public Editor(JPanel panel) {
         /*TODO: Display Dimension-selection screen first*/
         JFrame frame = (JFrame) panel.getParent().getParent().getParent();
        frame.setResizable(false);
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
         Dimension panelSize = panel.getSize();
         WorldModel world = new WorldModel(128, 128);
         EditorPanel editPanel = new EditorPanel(new Dimension((int) (panelSize.width * 0.8), panelSize.height), world);
         EditorList list = new EditorList(new Dimension((int) (panelSize.width * 0.2), panelSize.height), world);
 
         panel.add(editPanel);
         panel.add(list);
     }
 }
