 import java.awt.Graphics;
 
 
 public class Missle extends Bullet{
 	
 	private int distance;
 	
 	public Missle(int sx, int sy, int ex, int ey) {
 		super(sx, sy, ex, ey);
 	}
 	
 	public void MouseMoved(int x, int y){
 		startx = currentx;
 		starty = currenty;
 		endx = x;
 		endy = y;
 		count = 4;
 	}
 	
 	protected void increaseX(int ammount){
 		double xdistance = endx-startx;
 		double ydistance = endy-starty;
 		double theta = Math.atan(ydistance/xdistance);
 		if(xdistance < 0 ){
 			theta+=Math.PI;
 		}
 		double x = (count)*Math.cos(theta);
 		double y = (count)*Math.sin(theta);
 		currentx=(int)(x+startx);
 		currenty=(int)(y+starty);
 		count+=4;
 		distance+=4;
 	}
 	
 	public void drawBullet(Graphics g){
 		super.drawBullet(g);
 		if(distance>1000){
 			finished();
 		}
 	}
 	
 	protected boolean isnearX(){
		if(currentx<(int)endx+4 && currentx > (int)endx-4){
			if(currenty<(int)endy+4 && currenty > (int)endy-4){
 				return false;
 			}
 		}
 		return true;
 	}
 }
