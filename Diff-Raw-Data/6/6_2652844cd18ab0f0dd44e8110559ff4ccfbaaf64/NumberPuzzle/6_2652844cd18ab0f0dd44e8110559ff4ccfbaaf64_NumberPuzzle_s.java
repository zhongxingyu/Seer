 // MinimalPong.java
 
 package ph.sm.numberpuzzle;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.graphics.Point;
 import ch.aplu.android.Actor;
 import ch.aplu.android.GGActorTouchListener;
 import ch.aplu.android.GGTouch;
 import ch.aplu.android.GameGrid;
 import ch.aplu.android.L;
 import ch.aplu.android.Location;
 
 public class NumberPuzzle extends GameGrid implements GGActorTouchListener {
 	
 	private Location initialLoc;
 	private Actor dragActor;
 	
 	public NumberPuzzle() {
 		super(4, 4, cellZoom(60));
 	}
 
 	public void main() {
 		getBg().clear(DKGRAY);
 		setSimulationPeriod(30);
 		NumberStone[] stones = new NumberStone[15];
 		for (int i = 0; i < 15; i++) {
 			stones[i] = new NumberStone(i);
 			stones[i].addActorTouchListener(this, GGTouch.drag
 					| GGTouch.release | GGTouch.press, true);
 			//addActorNoRefresh(stones[i], new Location(i % 4, i / 4)); //for sorted arrangement
 			addActorNoRefresh(stones[i],getRandomEmptyLocation()); //for random arrangement
 		}
 		while (computeParity() % 2 == 0) {
 			L.d("Game is not solvable, doing some random shuffling..." );
 			NumberStone randomStone = stones[(int)(Math.random()*stones.length)];
 			randomStone.setLocation(getRandomEmptyLocation());
 		}
 		L.d("" + computeParity());
 		doRun();
 	}
 	/**
 	 * Only empty locations in a 4-neighborhood of the initial location
 	 * are valid move locations. 
 	 * @param loc, the location checked for validity
 	 * @return
 	 */
 	private boolean isMoveValid(Location loc) {
 		if (!isInGrid(loc))
 			return false;
 		else if (getNumberOfActorsAt(loc) > 1)
 			return false;
 		else return initialLoc.getNeighbourLocations(0.5).contains(loc); 
 	}
 
 	@Override
 	public void actorTouched(Actor actor, GGTouch touch, Point spot) {
 		switch (touch.getEvent()) {
 		case GGTouch.press:
			L.d("Press event spawned");
 			initialLoc = actor.getLocation();
 			dragActor = actor;
 			dragActor.setOnTop();
 			break;
 		case GGTouch.drag:
 			dragActor.setPixelLocation(new Point(touch.getX(), touch.getY()));
 			break;
 		case GGTouch.release:
			// even though the "OnTopOnly" flag is set when registering the listener,
			// once in a while an actor from the bottom spawns an event. 
			// setPixelLocation could cause this issue.
			L.d("Release event spawned from " + actor);
 			if (!isMoveValid(dragActor.getLocation()))
 				dragActor.setLocation(initialLoc);
 			dragActor.setLocationOffset(new Point(0,0));
 			break;
 		}	
 	}
 	
 	/**
 	 * To check if an arrangement of NumberStones is solvable, its parity has
 	 * to be computed. See http://de.wikipedia.org/wiki/15-Puzzle.
 	 * In two lines:
 	 * - If the parity is odd, the arrangement can be rearranged to 1-2-3-4 ... 14-15-[ ].
 	 * - If the parity is even, this can not be done.
 	 * @return the parity, that has to be checked for oddity.
 	 */
 	private int computeParity() {
 		int parity = 0;
 		for (int y = 0; y < getNbVertCells(); y++) {
 			for (int x = 0; x < getNbHorzCells(); x++) {
 				NumberStone check = (NumberStone)getOneActorAt(new Location(x, y));
 				if (check == null) //don't do this for gap
 					continue;
 				NumberStone next = check.getNextStone();
 				while (next != null) {
 				   if (next.getId() < check.getId())
 					   parity++;
 				   next = next.getNextStone();
 				}
 			}
 		}
 		// add row of gap to parity
 		parity += getEmptyLocations().get(0).getY();
 		return parity;
 	}
 	
 }
 
 class NumberStone extends Actor {
 	public NumberStone(int id) {
 		super("stone", 15);
 		show(id);
 	}
 	
 	/**
 	 * @return the number printed on this numberStone
 	 */
 	public int getId() {
 		return this.getIdVisible() + 1;
 	}
 	
 	/**
 	 * Returns the NumberStone that comes next when going first 
 	 * horizontally, then vertically through the grid. 
 	 * Does also handle the gap correctly (skips it)
 	 * @return null, if last stone
 	 */
 	public NumberStone getNextStone() {
 		NumberStone nextStone = null;
 		int nextX = getX();
 		int nextY = getY();
 		// loop is necessary to handle gap (iterates twice if at gap)
 		while (nextStone == null) {
 			nextX++;
 			// switch to next row this stone is last of row
 			if (nextX >= gameGrid.getNbHorzCells()) {
 				nextX = 0;
 				nextY += 1;
 			}
 			// return null if we were at last position
 			if (nextY >= gameGrid.getNbHorzCells())
 				return null;
 			nextStone = (NumberStone) gameGrid.getOneActorAt(new Location(nextX, nextY));
 		}
 		return nextStone;
 	}
 	
 	public String toString() {
 		return "NS " + getId() + " at: " + getLocation();
 	}
 }
