 package net.codegames.towerninja;
 
 //kennt alle steine
 // kennt Turm
 
 /**
  * @author chameleon
  * 
  *         This class contains a tower data structure. The method createStone
  *         allows creating new stones for free spots within the tower.
  */
 public class Game {
 
 	/**
 	 * A tower represented by a 2-dimensional array. the first dimension
 	 * describes the towers width. The second its height. When a new stone is
 	 * added to a certain location, it must not actually be located at that
 	 * position already. It will fly towards that position though.
 	 */
 	private Stone[][] tower = new Stone[4][10];
 
 	public void update() {
 		createStone();
 		// steine erzeugen
 		// steine bewegen
 	}
 
 	private void createStone() {
		towerHeightLoop: for (int i = 0; i < tower.length; i++) {
			for (int j = 0; j < tower[0].length; j++) {
 				if (tower[i][j] == null) {
 					tower[i][j] = new Stone(50, 5);
 					break towerHeightLoop;
 				}
 			}
 		}
 	}
 
 	/**
 	 * draws every stone
 	 */
 	void drawStones() {
 
 	}
 }
