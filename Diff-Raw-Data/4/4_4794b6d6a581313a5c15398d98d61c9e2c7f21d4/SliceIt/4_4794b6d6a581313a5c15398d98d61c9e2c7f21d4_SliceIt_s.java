 // SliceIt.java
 
 package ph.sm.sliceIt;
 
 import java.util.ArrayList;
 
 import ch.aplu.android.*;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 /**
  * For now: only slicing direct line between press and release!
  * 
  * @author panmari
  * 
  */
 public class SliceIt extends GameGrid implements GGTouchListener {
	private Location sliceStart;
 	private GGPanel p;
 	private Location prevTouchLoc;
 	private ArrayList<Location> sliceLocs;
 
 	public SliceIt() {
 		super(0, 0, 1);
 	}
 
 	public void main() {
 		setStatusText("SliceIt started");
 		addTouchListener(this, GGTouch.release | GGTouch.press | GGTouch.drag);
 		addActor(new Fruit(this), new Location(50, 10));
 		this.p = getPanel();
 		p.setPaintColor(Color.BLUE);
 		setSimulationPeriod(30);
 		doRun();
 	}
 
 	public boolean touchEvent(GGTouch touch) {
 		switch (touch.getEvent()) {
 		case GGTouch.press:
			sliceStart = toLocation(touch);
 			break;
 		case GGTouch.drag:
 			sliceLocs = getLocationsInbetween(prevTouchLoc, toLocation(touch));
 			prevTouchLoc = toLocation(touch);
 			for (Location l : sliceLocs)
 				p.drawPoint(l.getX(), l.getY());
 			break;
 		case GGTouch.release:
 			// p.drawLine(toPoint(sliceStart), toPoint(getTouchPoint(touch)));
 			break;
 		}
 		refresh();
 		return false;
 	}
 
 	private ArrayList<Location> getLocationsInbetween(Location loc1,
 			Location loc2) {
 		float dx = Math.abs(loc2.x - loc1.x);
 		float dy = Math.abs(loc2.y - loc1.y);
 		ArrayList<Location> locs = new ArrayList<Location>();
 		int sx, sy, x = loc1.x, y = loc1.y;
 		if (loc1.x < loc2.x)
 			sx = 1;
 		else
 			sx = -1;
 		if (loc1.y < loc2.y)
 			sy = 1;
 		else
 			sy = -1;
 		float err = dx - dy;
 		do {
 			locs.add(new Location(x, y));
 			float e2 = 2 * err;
 			if (e2 > -dy) {
 				err = err - dy;
 				x += sx;
 			}
 			if (e2 < dx) {
 				err = err + dx;
 				y += sy;
 			}
 		} while (x != loc2.x | y != loc2.y);
 		return locs;
 	}
 
 	private Location toLocation(GGTouch touch) {
 		return toLocation(new Point(touch.getX(), touch.getY()));
 	}
 
 	public ArrayList<Location> getSliceLocs() {
 		return sliceLocs;
 	}
 }
