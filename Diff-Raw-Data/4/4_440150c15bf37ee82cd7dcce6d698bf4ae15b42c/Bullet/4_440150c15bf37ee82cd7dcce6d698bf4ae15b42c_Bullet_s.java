 import java.awt.Point;
 import java.awt.geom.Line2D;
 import java.io.DataInputStream;
 import java.nio.ByteBuffer;
 
 
 
 public class Bullet {	
 	private Line2D.Float d_line;
 	
 	private int d_id;
 	
 	private int d_transparency;
 	
 	public static int sendSize() {
 		
 		return 6 * 4;
 	}
 	
 	public void addToBuffer(ByteBuffer buffer) {
 		buffer.putFloat(d_line.x1);
 		buffer.putFloat(d_line.y1);
 		buffer.putFloat(d_line.x2);
 		buffer.putFloat(d_line.y2);
 		
 		buffer.putInt(d_id);
 		buffer.putInt(d_transparency);
 	}
 	
 	public Bullet() {
 		d_line = new Line2D.Float(0, 0, 0, 0);
 		d_transparency = 0;
 		d_id = -1;
 	}
 	
 	public void instantiate(float x, float y, float direction, int id) {
 		d_line.x1 = x;
 		d_line.y1 = y;
    	d_line.x2 = (float) (d_line.x1 + Math.sin(direction) * 1000);
		d_line.y2 = (float) (d_line.y1 + Math.cos(direction) * 1000);
 		Point point = SpectroPolaris.frame().gamePanel().model().visible(d_line.x1, d_line.y1, d_line.x2, d_line.y2);
 		
 		if(point != null) {
 			d_line.x2 = point.x;
 			d_line.y2 = point.y;
 		}
 		
 		d_id = id;
 		d_transparency = 255;
 	}
 	
 	public void instantiate(Bullet other) {
 		d_line.x1 = other.d_line.x1;
 		d_line.y1 = other.d_line.y1;
     	d_line.x2 = other.d_line.x2;
 		d_line.y2 = other.d_line.y2;
 		
 		
 		d_id = other.d_id;
 		d_transparency = 255;
 	}
 	
 	public void instantiate(DataInputStream in) throws Exception {
 		d_line.x1 = in.readFloat();
 		d_line.y1 = in.readFloat();
 		d_line.x2 = in.readFloat();
 		d_line.y2 = in.readFloat();
 		
 		d_id = in.readInt();
 		d_transparency = in.readInt();
 	}
 	
 	public Line2D.Float line() {
 		return d_line;
 	}
 	
 	public boolean step() {
 		if(destroyed())
 			return false;
 		
 		d_transparency -= 3000/d_transparency;
 			
 		if(d_transparency < 0) {
 			destroy();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void destroy() {
 		d_id = -1;
 	}
 	
 	public boolean destroyed() {
 		return d_id == -1;
 	}
 
 	public int id() {
 		return d_id;
 	}
 
 }
