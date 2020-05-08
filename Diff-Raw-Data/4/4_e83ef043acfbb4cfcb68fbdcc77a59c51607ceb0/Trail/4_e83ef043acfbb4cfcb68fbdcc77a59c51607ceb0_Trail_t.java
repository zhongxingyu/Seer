 import java.awt.Graphics;
 import java.awt.Point;
 
 public class Trail extends ArenaObject {
 	protected LightCycle creator;
 
 	public Trail(LightCycle creator, Point p) {
 		super(p);
 		this.creator = creator;
 	}
 
 	@Override
 	public void actOn(LightCycle l) {
 		l.kill();
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(creator.getColor());
 		int x = position.x;
 		int y = position.y;
		int d = creator.WIDTH / 4;
		g.fillRect(x-d, y-d, 2*d, 2*d);
 	}
 }
