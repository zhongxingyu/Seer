 package gui.includes.Add;
 
 import gui.MainWindow;
 import gui.includes.MenuBar;
 import gui.includes.Terrain.Terrain;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 public class AddStep1 extends JPanel {
 	
 	public static AddStep1 panel = new AddStep1();
 	private JButton voirterrain = new JButton("Voir terrain");
 	private JPanel terrainContainer = new JPanel();
 	
 	public AddStep1() {
 		this.add(new JLabel("Add Step 1", 10));
 		this.add(voirterrain);
 		
 		voirterrain.addActionListener(new ActionListener() { 
 			public void actionPerformed(ActionEvent e) { 
 				voirterrainPressed();
 			} 
 		});
 		
 	}
 	
 	public void voirterrainPressed() {
 		MainWindow.fenetre.buildWindow(Terrain.terrain);
		Terrain.terrain.requestFocusInWindow();
 	}
 }
