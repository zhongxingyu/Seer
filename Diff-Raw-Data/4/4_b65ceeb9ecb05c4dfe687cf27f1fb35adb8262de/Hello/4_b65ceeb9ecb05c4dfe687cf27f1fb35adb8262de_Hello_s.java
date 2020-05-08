 package no.thunaes.petter.svg.app.gui.domain;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class Hello extends JPanel {
 
	private JLabel label;
 	
 	public Hello() {
 		label = new JLabel();
 	}
 
 	public void hello() {
 		label.setText("Hello to you sir!");
 	}
 }
