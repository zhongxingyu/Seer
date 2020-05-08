 package de.zigapeda.flowspring.gui;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 import de.zigapeda.flowspring.Main;
 
 public class Volumebar extends JPanel implements MouseListener, MouseMotionListener {
 	private static final long serialVersionUID = 4040860927056250732L;
 //	private static final String uz_lange_variable_um_den_zeilen_umbruch_zu_testen_den_eclipse_beim_drucken_von_source_dateien_macht_platzhalter_platzhalter_platzhalter = "bla_platzhalter_platzhalter_platzhalter_platzhalter_platzhalter";
 	private JProgressBar volumebar;
 	private JLabel volume;
 	
 	public Volumebar() {
 		super();
 		this.setLayout(null);
 		this.volumebar = new JProgressBar();
 		this.volume = new JLabel("volume");
 		this.volumebar.setBounds(0, 14, 100, 14);
 		this.volume.setBounds(5, 0, 95, 14);
 		this.volumebar.setMaximum(100);
 		this.volumebar.addMouseListener(this);
 		this.volumebar.addMouseMotionListener(this);
 		this.volumebar.setValue(100);
 		this.add(this.volumebar);
 		this.add(this.volume);
 	}
	
	public int getValue() {
		return this.volumebar.getValue();
	}
 
 	public void mouseClicked(MouseEvent e) {
 
 	}
 
 	public void mousePressed(MouseEvent e) {
 		this.volumebar.setValue(e.getX());
 		Main.getWindow().getPlayercontroller().setGain(this.volumebar.getValue());
 		this.volumebar.repaint();
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		
 	}
 
 	public void mouseExited(MouseEvent e) {
 		
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		this.volumebar.setValue(e.getX());
 		Main.getWindow().getPlayercontroller().setGain(this.volumebar.getValue());
 		this.volumebar.repaint();
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		
 	}
 }
