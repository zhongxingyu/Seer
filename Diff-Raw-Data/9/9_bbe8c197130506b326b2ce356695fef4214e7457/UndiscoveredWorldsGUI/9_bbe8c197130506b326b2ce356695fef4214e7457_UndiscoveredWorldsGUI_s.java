 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.*;
 
 public class UndiscoveredWorldsGUI extends JFrame implements ActionListener {
 
 	private JButton optionsButton;
 
 	public UndiscoveredWorldsGUI() {
 		super("Undiscovered Worlds");
 		setBackground(Color.BLACK);
 		
 		optionsButton = new JButton();
 		optionsButton.setLocation(702, 0);
 		optionsButton.setSize(38, 38);
 		optionsButton.setIcon(new ImageIcon("Art" + File.separator + "OptionButton.png"));
 		optionsButton.setToolTipText("Options Menu");
 		optionsButton.addActionListener(this);
 
 		Galaxy Andromeda = new Galaxy();
 		Player player = new Player();
 
 		Andromeda.Generate(1);
 		// Andromeda.DisplayCluster();
 
 		player.setLoc(Andromeda.randomStart());
 
 		SpacePanel test = new SpacePanel(player);
 		// test.populateSpace();
		// test.setPreferredSize(new Dimension(720, 720));
 		// test.setLocation(0, 0);
 		this.add(optionsButton);
 		this.add(test);
 		// System.out.println("This was run");
 		/*
 		 * GUITest test = new GUITest(Andromeda.GetSectors());
 		 * test.setPreferredSize(new Dimension(720,720)); test.setLocation(0,
 		 * 0); test.setLayout(null);
 		 * 
 		 * setLayout(new FlowLayout()); setBackground(Color.DARK_GRAY);
 		 */
 		// test.add(optionsButton);
 		// add(test);
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		UWOptionsPanel optionWindow = new UWOptionsPanel();
 		optionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		optionWindow.setLocation(
 				(int) (this.getLocation().getX() + (this.getWidth() / 4)),
 				(int) (this.getLocation().getY() + (this.getHeight() / 3)));
 		optionWindow.setSize(400, 200);
 		optionWindow.setVisible(true);
 		optionWindow.setResizable(false);
 	}
 
 }
