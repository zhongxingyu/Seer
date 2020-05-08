 package admin;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class PlayerPanel extends JPanel {
 
 	private ImageIcon imgData;
 	private JLabel imgDisplay;
 
 	private EditPlayerFieldsPanel paneEditFields;
 	// container for top stuff
 	private JPanel paneTop;
 	
 	private JButton bCastOff;
 	private JButton bSavePlayer;
 	
 	
 	public PlayerPanel(){
 		paneTop = new JPanel();
 		paneTop.setLayout(new BoxLayout(paneTop, BoxLayout.X_AXIS));
 		
 		// TODO: Resize?
		String path = "res/test/defaultpic.png"; //apparently images have to be .png and alphanumeric
		ImageIcon imgD = new ImageIcon(path);
		imgDisplay = new JLabel("a");
		imgDisplay.setIcon(imgD);
 		
 		// holds all the fields
 		paneEditFields = new EditPlayerFieldsPanel(new String[] {"SEXY", "MORE SEXY"});
 		
 		// buttons:
 		bCastOff = new JButton("Cast Off");
 		bSavePlayer = new JButton("Save");
 		
 		// this does not need to be referenced else where, only for layout
 		JPanel paneButtons = new JPanel();
 		GridLayout bl = new GridLayout(2, 1);
 		paneButtons.setLayout(bl);
 		
 		paneButtons.add(bCastOff);
 		paneButtons.add(bSavePlayer);
 		
 		// add all components on top:
 		paneTop.add(imgDisplay);
 		paneTop.add(paneEditFields);
 		paneTop.add(paneButtons);
 		
 		add(paneTop);
 	}
 }
