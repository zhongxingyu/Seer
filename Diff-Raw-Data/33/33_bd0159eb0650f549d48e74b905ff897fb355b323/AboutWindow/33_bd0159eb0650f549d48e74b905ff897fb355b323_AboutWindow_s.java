 package FRC_Score_Sys;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.net.URI;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 public class AboutWindow extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public AboutWindow() {
 		setTitle("About App");
 		//this.setSize(300, 300);
 
 		JPanel panel = new JPanel();
 		getContentPane().add(panel, BorderLayout.NORTH);
 		panel.setLayout(new GridLayout(0, 1, 0, 0));
 
 		try {
 
 			JLabel lbl = new JLabel("Matt's FRC 2013 Scoring Application");
 			lbl.setHorizontalAlignment(SwingConstants.CENTER);
 			panel.add(lbl);
 
 			lbl = new JLabel("");
 			lbl.setHorizontalAlignment(SwingConstants.CENTER);
 			panel.add(lbl);
 
 			lbl = new JLabel("Created by Matt Glennon/FNS Network");
 			lbl.setHorizontalAlignment(SwingConstants.CENTER);
 			panel.add(lbl);
 
 			LinkLabel lnk = new LinkLabel(new URI("https://www.fnsnet.net"), "Visit FNSNetwork");
 			lnk.setHorizontalAlignment(SwingConstants.CENTER);
 			lnk.init();
 			panel.add(lnk);
 			
 			lbl = new JLabel("Please report all bugs/feature requests");
 			lbl.setHorizontalAlignment(SwingConstants.CENTER);
 			panel.add(lbl);
 			
 			lnk = new LinkLabel(new URI("https://github.com/VirginiaFIRST/frc2013score/issues?state=open"), "Bug Tracker & Updates");
 			lnk.setHorizontalAlignment(SwingConstants.CENTER);
 			lnk.init();
 			panel.add(lnk);
 
 			lbl = new JLabel("Created for VirginiaFIRST");
 			lbl.setHorizontalAlignment(SwingConstants.CENTER);
 			panel.add(lbl);
 				
 			lnk = new LinkLabel(new URI("http://www.virginiafirst.org"), "Visit VirginiaFIRST");
 			lnk.setHorizontalAlignment(SwingConstants.CENTER);
 			lnk.init();
 			panel.add(lnk);
 			
 			lbl = new JLabel("This is Free software!");
 			lbl.setHorizontalAlignment(SwingConstants.CENTER);
 			panel.add(lbl);
 			
 			lnk = new LinkLabel(new URI("https://github.com/VirginiaFIRST/frc2013score/blob/master/LICENSE"), "GNU GENERAL PUBLIC LICENSE - Read More Here");
 			lnk.setHorizontalAlignment(SwingConstants.CENTER);
 			lnk.init();
 			panel.add(lnk);
 		} catch (Exception e) {
 
 		}
 		pack();
 	}
 }
