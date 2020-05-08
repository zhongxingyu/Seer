 package program.localization;
 
 import java.util.ArrayList;
 
 public class LinearColorMap {
 	private int length;
 	private ArrayList<LineSegment> segments;
 	
 	public LinearColorMap(int length) {
 		this.length = length;
 		segments = new ArrayList<LineSegment>();
 	}
 	
 	public void addSegment(double x0, double x1, Color color) {
 		segments.add(new LineSegment(x0, x1, color));
 	}
 	
 	public int getLength() {
 		return length;
 	}
 	
 	public boolean isColorAt(Color color, int x) {
		if(x > length || x < 0) return false;
 		
 		Color c = getColorAt(x);
 		if(c == color) return true;	
 		
 		return false;		
 	}
 	
 	public Color getColorAt(int x) {		
 		for(int i=0;i<segments.size();i++) {
 			if(x >= segments.get(i).getX0() && x <= segments.get(i).getX1()) {
 				return segments.get(i).getColor();
 			}
 		}
 		
 		return null;
 	}
 	
 
 }
