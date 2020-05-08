 package wombat.scheme.libraries;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import java.util.*;
 
 import wombat.scheme.libraries.gui.ImageFrame;
 
 /**
  * Represent turtle graphics.
  */
 public class TurtleAPI {
 	/**
 	 * Convert a turtle to an image.
 	 * @param data Lines each with "tick x0 y0 x1 y1 r g b" 
 	 * @return An image.
 	 */
 	public static Image turtleToImage(String data) {
 		@SuppressWarnings("unused")
 		class LineData {
 			int Tick;
 			double X0;
 			double Y0;
 			double X1;
 			double Y1;
 			Color C;
 			
 			LineData(int tick, double x0, double y0, double x1, double y1, Color c) {
 				Tick = tick;
 				X0 = x0;
 				Y0 = y0;
 				X1 = x1;
 				Y1 = y1;
 				C = c;
 			}
 		}
 		
 		List<LineData> lines = new ArrayList<LineData>();
 		
 		double minX = 0;
 		double maxX = 0;
 		double minY = 0; 
 		double maxY = 0;
 		
 		for (String line : data.split("\n")) {
			String[] parts = line.trim().split(" ");
			if (parts.length == 0) continue;
			if (parts.length != 8) throw new IllegalArgumentException("Malformed turtle data with line: " + line);
 			lines.add(new LineData(
 				Integer.parseInt(parts[0]),
 				Double.parseDouble(parts[1]),
 				-1.0 * Double.parseDouble(parts[2]),
 				Double.parseDouble(parts[3]),
 				-1.0 * Double.parseDouble(parts[4]),
 				new Color(
 					Integer.parseInt(parts[5]),
 					Integer.parseInt(parts[6]),
 					Integer.parseInt(parts[7])
 					)
 				));
		}
 		
 		for (LineData line : lines) {
 			minX = Math.min(line.X0, Math.min(line.X1, minX));
 			maxX = Math.max(line.X0, Math.max(line.X1, maxX));
 			minY = Math.min(line.Y0, Math.min(line.Y1, minY));
 			maxY = Math.max(line.Y0, Math.max(line.Y1, maxY));
 		}
 		minX -= 1;
 		maxX += 1;
 		minY -= 1;
 		maxY += 1;
 		
 		BufferedImage bi = new BufferedImage((int) (maxX - minX), (int) (maxY - minY), BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = (Graphics2D) bi.getGraphics();
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
 		
 		for (LineData line : lines) {
 			g.setColor(line.C);
 			g.drawLine(
 				(int) (line.X0 - minX), 
 				(int) (line.Y0 - minY),
 				(int) (line.X1 - minX),
 				(int) (line.Y1 - minY)
 			);
 		}
 		
 		return bi;
 	}
 	
 	/**
 	 * Draw a set of turtle lines.
 	 * @param data Lines each with "tick x0 y0 x1 y1 r g b"
 	 */
 	public static void drawTurtle(String data) {
 		ImageFrame iframe = new ImageFrame(turtleToImage(data));
 		iframe.setTitle("draw-turtle");
 		iframe.setVisible(true);
 	}
 }
