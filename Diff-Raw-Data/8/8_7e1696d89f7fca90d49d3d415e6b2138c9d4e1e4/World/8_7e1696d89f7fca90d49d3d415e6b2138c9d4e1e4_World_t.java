 package world;
 
 import gui.Background;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 /**
  * @author King
  * 
  */
 public class World {
 
 	private ArrayList<Background> BackGrounds;
 
 	/**
 	 * Constructor of world
 	 */
 	public World() {
 	}
 
 	/**
 	 * @param i
 	 *            . drawWorld has a seed where it will call a specific array of
 	 *            Backgrounds, it then checks if the the character is at the end
 	 *            of the map and if it is it moves to the next background
 	 */
 	public Grid drawWorld(int i) {
 		ArrayList<Background> BackGrounds = new ArrayList<Background>();
 		if (i == 1) {
 			BackGrounds = this.world1();
 		}
 		Background a = BackGrounds.get(0);
 
 		Grid g = new Grid();
 		HashMap<Point, GridSpace> grid = g.getGrid();
 		for (int d = 0; d < 101; d++) {
 			for (int j = 0; j < 25; j++) {
 				GridSpace f = new GridSpace(new ArrayList<Thing>());
 				if (d == 100) {
 					f.add(new Terrain(false, Color.BLACK));
 				} else if (j >= 22) {
 					f.add(new Terrain(true, a.getGroundColor()));
 				} else if (j > 2) {
 
 				} else {
 					f.add(new Terrain(true, a.getTopColor()));
 				}
 				if (a.getHill()) {
 					this.drawHill(g);
 				}
 
 				// needs to check if there is a hill in the background then
 				// call
 				// the draw hill method
 				f.sortArrayOfThings();
 				grid.put(new Point(d, j), f);
 			}
 		}
 		g.setGrid(grid);
 		return g;
 
 	}
 
 	/**
 	 * Will draw a hill at a random place on the map
 	 */
 	public void drawHill(Grid g) {
 		Random r = new Random();
 		int height;
 		int width;
 		int seed;
 		height = r.nextInt(20) + 1;
 		seed = r.nextInt(50) + 2;
 		HashMap<Point, GridSpace> grid = g.getGrid();
 		ArrayList<Integer> a = new ArrayList<Integer>();
 		int u = -1;
 
 		for (int p = 0; p <= height; p = p + 3) {
 			a.add(23 - p);
 		}
 		int xpos = 0;
 		for (int i = 0; i < a.size(); i++) {
 			width = (r.nextInt(7) + 1) + seed;
			if(width >= 100)
				width = (r.nextInt(7) + 1) + seed;
 			for (int s = 0; s < width; s++) {
 				int topBlock = a.get(i);
 				for (int j = 23; j > topBlock; j--) {
 					GridSpace f = new GridSpace(new ArrayList<Thing>());
 					f.add(new Terrain(true, Color.GREEN));
 					grid.put(new Point(xpos, j), f);
 
 				}
 				xpos++;
 			}
 		}
 
 	}
 
 	/**
 	 * @return World 1 traits will be stored here
 	 */
 	public ArrayList<Background> world1() {
 		Color ground = new Color(0);
 
 		ArrayList<Background> BackGrounds = new ArrayList<Background>();
 
 		Background a = new Background(ground.GREEN, ground.CYAN, ground.darkGray, 2, 22, true);
 		BackGrounds.add(a);
 		Background b = new Background(ground.GREEN, ground.CYAN, ground.darkGray, 2, 22, false);
 		BackGrounds.add(b);
 		Background c = new Background(ground.GREEN, ground.CYAN, ground.darkGray, 2, 22, true);
 		BackGrounds.add(c);
 		return BackGrounds;
 	}
 
 	/**
 	 * @return World 2 traits will be stored here
 	 */
 	public ArrayList<Background> world2() {
 		Color ground = new Color(0);
 
 		ArrayList<Background> BackGrounds = new ArrayList<Background>();
 
 		Background a = new Background(ground.GREEN, ground.CYAN, ground.darkGray, 2, 22, true);
 		BackGrounds.add(a);
 		Background b = new Background(ground.GREEN, ground.CYAN, ground.darkGray, 2, 22, false);
 		BackGrounds.add(b);
 		Background c = new Background(ground.GREEN, ground.CYAN, ground.darkGray, 2, 22, true);
 		BackGrounds.add(c);
 		return BackGrounds;
 	}
 
 }
