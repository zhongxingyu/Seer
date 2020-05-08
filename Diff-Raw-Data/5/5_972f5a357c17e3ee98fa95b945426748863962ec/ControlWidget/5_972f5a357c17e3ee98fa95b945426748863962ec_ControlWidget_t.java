 package gui.controlwidgets;
 
 import icarus.operatingsoftware.PlantControl;
import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.util.Observer;
import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 /**
  *
  * @author drm
  */
 public abstract class ControlWidget extends JPanel implements Observer, ActionListener {
 
     protected PlantControl plant;
     protected Box vbox;
 
     public ControlWidget(PlantControl plantControl) {
         this.plant = plantControl;
         this.vbox = Box.createVerticalBox();
        add(vbox);
     }
     
     protected JLabel addTitle(String text) {
         JLabel label = new JLabel(text);
         vbox.add(Align.left(label));
         return label;
     }
     
     protected JButton addButton(String text) {
         JButton button = new JButton(text);
         button.addActionListener(this);
         vbox.add(Align.centerVertical(button));
         return button;
     }
     
     protected JToggleButton addToggleButton(String text) {
         JToggleButton button = new JToggleButton(text);
         button.addActionListener(this);
         vbox.add(Align.centerVertical(button));
         return button;
     }
 }
