 package bomberman;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
import javax.swing.JPanel;
 
 public class Tuere extends JFrame {
 	private static final long serialVersionUID = -2181712976539020219L;
 	private JLabel win1 = new JLabel(new ImageIcon("src/gfx/win3.gif"));
 	private JLabel win2 = new JLabel(new ImageIcon("src/gfx/explode.gif"));
 	private int rasterPunktX, rasterPunktY;
	private JPanel jPanel;
 	private JLabel[] toRemove;
 
	public Tuere(int rasterPunktX, int rasterPunktY, JPanel jPanel, JLabel[] toRemove) {
 		this.jPanel = jPanel;
 		this.toRemove = toRemove;
 		this.rasterPunktX = rasterPunktX;
 		this.rasterPunktY = rasterPunktY;
 	}
 
 	
 	 public void isExit(BomberMan b) {
 		 if(b.getRasterPunktX() == rasterPunktX && b.getRasterPunktY() == rasterPunktY) {
 			 end();
 		 }
 	 }
 	 
 
 	private void end() {
 		for( JLabel r : toRemove ) {
 			jPanel.remove(r);
 		}
 		
		jPanel.add(win1);
 		win1.setBounds(1, 1, 840, 600);
 		win1.setVisible(true);
 		win1.repaint();
 		
		jPanel.add(win2);
		win2.setBounds(225, 10, 390, 196);
 		win2.setVisible(true);
 		win2.repaint();
 	}
 
 }
