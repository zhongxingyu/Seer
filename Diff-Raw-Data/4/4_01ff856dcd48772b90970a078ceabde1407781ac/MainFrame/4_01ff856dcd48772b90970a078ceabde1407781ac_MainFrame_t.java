 import javax.swing.*;
 
 import model.*;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 
 public class MainFrame extends JFrame {
 	
 	public MainFrame() {
 		GraphicalPanel graphicalPanel = new GraphicalPanel();
 		this.add(graphicalPanel, BorderLayout.CENTER);
 		ToolsPanel toolsPanel = new ToolsPanel(graphicalPanel.getDesign(), graphicalPanel);
 		this.add(toolsPanel, BorderLayout.SOUTH);
 		GraphicalMouseMotionListener mouseListener = new GraphicalMouseMotionListener(graphicalPanel, toolsPanel);
 		graphicalPanel.addMouseListener(mouseListener);
 		graphicalPanel.addMouseMotionListener(mouseListener);
 		//MenuBar//////////////////////////////////////////////////
		
		if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0)
			System.setProperty("apple.laf.useScreenMenuBar", "true");
 		JMenuBar menuBar = new JMenuBar();
 		this.setJMenuBar(menuBar);
 		//File Menu
 		JMenu fileMenu = new JMenu("File");
 		JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_Q);
 		JMenuItem exportItem = new JMenuItem("Export to Code");
 		//Help Menu
 		JMenu helpMenu = new JMenu("Help");
 		JMenuItem aboutItem = new JMenuItem("About");
 		
 		menuBar.add(fileMenu);
 		menuBar.add(helpMenu);
 		
 		fileMenu.add(exportItem);
 		fileMenu.add(exitItem);
 		
 		helpMenu.add(aboutItem);
 		
 		MenuActionListener listener = new MenuActionListener();
 		exitItem.addActionListener(listener);
 		exportItem.addActionListener(listener);
 		aboutItem.addActionListener(listener);
 		////////////////////////////////////////////////////////////
 
 		this.setTitle("Class Builder - CS124 Group Project");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setLocation(60, 60);
 		this.pack();
 		this.setResizable(false);
 		this.setVisible(true);
 		
 	}
 
 	
 }
