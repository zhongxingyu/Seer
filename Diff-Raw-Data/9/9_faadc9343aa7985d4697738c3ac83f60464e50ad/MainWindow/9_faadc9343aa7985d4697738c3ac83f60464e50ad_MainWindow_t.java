 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.io.*;
 import javax.imageio.*;
 public class MainWindow {
 	TreeView treeView;
 	JFrame mainFrame;
 	JMenuBar menuBar;
 	Box buttonBox;
 
 	public MainWindow(TreeView theTreeView) {
 		Box theBox = new Box(BoxLayout.Y_AXIS);
 		mainFrame = new JFrame("Skill Tree Ver 0.0a");
 		JMenu JMenuRef;
 		BufferedImage image;
 
 		try{
			image = ImageIO.read(new File("../lib/resources/images/wolf.png"));
 		}catch(Exception e){
 			image = new BufferedImage(40,40,1);
 		}
 		//initializing the menu bar
 		menuBar = new JMenuBar();
 		JMenuRef = new JMenu("File");//file
 		JMenuRef.add(new JMenuItem("New"));
 		JMenuRef.add(new JMenuItem("Load"));
 		JMenuRef.addSeparator();
 		JMenuRef.add(new JMenuItem("Add"));
 		JMenuRef.add(new JMenuItem("Remove"));
 		JMenuRef.add(new JMenuItem("Remove Branch"));
 		JMenuRef.addSeparator();
 		JMenuRef.add(new JMenuItem("Exit"));
 		menuBar.add(JMenuRef);
 		JMenuRef = new JMenu("Help"); //help
 		JMenuRef.add(new JMenuItem("About"));
 		JMenuRef.add(new JMenuItem("lulz"));
 		menuBar.add(JMenuRef);
 		mainFrame.setJMenuBar(menuBar);
 		
 		buttonBox = new Box(BoxLayout.X_AXIS);
 		buttonBox.add(new JButton("Add",new ImageIcon(image.getScaledInstance(50,50,Image.SCALE_SMOOTH))));
 		buttonBox.add(new JButton("Remove",new ImageIcon(image.getScaledInstance(50,50,Image.SCALE_SMOOTH))));
 		theBox.add(buttonBox);
 		
 
 		this.treeView =  theTreeView;
 		theBox.add(this.treeView);
 
 		
 		mainFrame.add(theBox);
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame.setSize(250, 250);
 		mainFrame.setLocation(300,200);
 		mainFrame.pack();
 		mainFrame.setVisible(true);
 				          
 	}
 	    
 }
