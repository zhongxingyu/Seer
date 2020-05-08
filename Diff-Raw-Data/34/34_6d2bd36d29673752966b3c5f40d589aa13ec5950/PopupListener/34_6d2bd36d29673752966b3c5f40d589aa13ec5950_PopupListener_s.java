 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JOptionPane;
 
 /**
  * Button listener pentru salvare modificari.
  * 
  * @author Unknown-Revengers
  */
 public class PopupListener implements ActionListener {
 
 	private ElementJPanel panel;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param panel
 	 */
	public PopupListener(ElementJPanel panel){
 		this.panel = panel;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
		
 		String action = e.getActionCommand();
 		JOptionPane.showMessageDialog(null, "Realizeaza actiunea selectata: " + action + ".");
		
 	}

 }
