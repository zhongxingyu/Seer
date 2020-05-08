 package Gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 import Map.Cell;
 import Map.Grid;
 
 public class MapDisplay extends JPanel {
 
 	private static Grid data;
 
 	public MapDisplay() {
 
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 
 		for (int x = 0; x < data.getWidth(); x++) {
 			for (int y = 0; y < data.getHeight(); y++) {
 				if (data.getCellValue(x, y) == Cell.Dead) {
 					g.setColor(Color.white);
					g.fillRect(x * 4, y * 4, 4, 4);
 				} else {
 					g.setColor(Color.black);
					g.fillRect(x * 4, y * 4, 4, 4);
 				}
 			}
 		}
 
 	}
 
 	public void updateMap(Grid data) {
 		MapDisplay.data = data;
		this.repaint();
 	}
 
 }
