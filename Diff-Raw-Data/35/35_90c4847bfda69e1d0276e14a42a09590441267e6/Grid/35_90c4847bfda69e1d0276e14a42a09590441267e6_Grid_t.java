 package tron.colin.zeidler;
 
 public class Grid {
 	int[][] grid;
 
 	Grid(int x, int y) {
 		x = x / 5;
 		y = y / 5;
 		grid = new int[x][y];
 		for (int i = 0; i < x; i++) {
 			for (int j = 0; j < y; j++) {
 				grid[i][j] = 0;
 			}
 		}
 	}

 	public void print() {
 		for (int i = 0; i < grid.length; i++) {
 			for (int j = 0; j < grid[i].length; j++) {
 				System.out.print(grid[i][j]);
 			}
 			System.out.println();
 		}
 		System.out.println();
 		System.out.println();
 		System.out.println();
 
 	}
 
 	public void collisionCheck(Player aPlayer) {
		// if moved over already full cell, die.
 		if (aPlayer.moved) {
			if (aPlayer.xSpeed < 0) {
				//	Player grid looks like this: 	x0
				//	x = check, 0 = do nothing		x0
				if(grid[aPlayer.x / 5][aPlayer.y / 5] != 0 || grid[aPlayer.x / 5][aPlayer.y / 5 + 1] != 0) {
					aPlayer.dead = true;
				}
			} else if (aPlayer.xSpeed > 0) {
				//	Player grid looks like this: 	0x
				//	x = check, 0 = do nothing		0x
				if(grid[aPlayer.x / 5 + 1][aPlayer.y / 5] != 0 || grid[aPlayer.x / 5 + 1][aPlayer.y / 5 + 1] != 0) {
					aPlayer.dead = true;
				}
			} else if (aPlayer.ySpeed < 0) {
				//	Player grid looks like this: 	xx
				//	x = check, 0 = do nothing		00
				if(grid[aPlayer.x / 5][aPlayer.y / 5] != 0 || grid[aPlayer.x / 5 + 1][aPlayer.y / 5] != 0) {
					aPlayer.dead = true;
				}
			} else if (aPlayer.ySpeed > 0) {
				//	Player grid looks like this: 	00
				//	x = check, 0 = do nothing		xx
				if(grid[aPlayer.x / 5][aPlayer.y / 5 + 1] != 0 || grid[aPlayer.x / 5 + 1][aPlayer.y / 5 + 1] != 0) {
					aPlayer.dead = true;
				}
 			}
 		}
 
 	}
 
 }
