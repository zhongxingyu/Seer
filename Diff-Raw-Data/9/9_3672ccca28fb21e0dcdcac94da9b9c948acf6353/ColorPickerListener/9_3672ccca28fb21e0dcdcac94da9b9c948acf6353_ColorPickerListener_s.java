 package listeners;
 
 import java.awt.Color;
 
 import javapaint.CanvasPanel;
 import javapaint.Utensil;
 
 import javax.swing.colorchooser.DefaultColorSelectionModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 /*
  * I think this class is where the color problem is!
  */
 
 public class ColorPickerListener implements ChangeListener{
 	private Utensil ut = null;
 
	public ColorPickerListener(CanvasPanel p){
		ut = p.getUtensil();
 	}
 
 	public void setColor(Color c){
 		ut.setColor(c);
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent arg0) {
 		setColor(((DefaultColorSelectionModel) arg0.getSource()).getSelectedColor());
 	}
 }
