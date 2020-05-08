 package Gui;
 
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 
 import Map.Grid;
 
 public class GuiMain extends JFrame {
 
	MapDisplay mapDisplay; // JPanel
 
 	public GuiMain(Grid myGrid) {
 		super("Life Simulation");
 
 		initialiseMap(myGrid);
 		new ArrangeGui(this);
 		initialiseFrame();
 	}
 
 	private void initialiseMap(Grid myGrid) {
 
 		mapDisplay = new MapDisplay();
		mapDisplay.setPreferredSize(new Dimension(myGrid.getWidth() * 2, myGrid
				.getHeight() * 2));
 		mapDisplay.updateMap(myGrid);
 
 	}
 
 	private void initialiseFrame() {
 
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(true);
 		pack();
 
 	}
 
 }
