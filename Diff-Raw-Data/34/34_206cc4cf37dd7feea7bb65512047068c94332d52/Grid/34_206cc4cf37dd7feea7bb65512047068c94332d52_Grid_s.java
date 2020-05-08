 package world;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Random;
 
 /**
  * @author aclement
  * 
  */
 public class Grid {
 
 	private HashMap<Point, GridSpace> grid;
 	private Point characterLocation;
 	private ArrayList<Point> enemyLocations;
 	private int numKilled;
 
 	/**
 	 * 
 	 */
 	public Grid(int numKilled) {
 		enemyLocations = new ArrayList<Point>();
 		setGrid(new HashMap<Point, GridSpace>());
 		this.numKilled = numKilled;
 	}
 
 	public HashMap<Point, GridSpace> getGrid() {
 		return grid;
 	}
 
 	public void setGrid(HashMap<Point, GridSpace> grid) {
 		this.grid = grid;
 	}
 
 	public Point getCharacterLocation() {
 		return characterLocation;
 	}
 
 	public void setCharacterLocation(Point characterLocation) {
 		this.characterLocation = characterLocation;
 	}
 
 	public ArrayList<Point> getEnemyLocation() {
 		return enemyLocations;
 	}
 
 	public void setEnemyLocation(Point enemyLocation, Point oldLocation) {
 		this.enemyLocations.remove(oldLocation);
 		this.enemyLocations.add(enemyLocation);
 	}
 
 	private void setEnemyLocation(Point enemyLocation) {
 		this.enemyLocations.add(enemyLocation);
 	}
 
 	private void removeEnemy(Point enemyLocation) {
 		this.enemyLocations.remove(enemyLocation);
 	}
 
 	public void moveCharacter(int x, int y, int lastKeyPressed) {
 		this.retractWeapon(lastKeyPressed);
 		Point newLocation = new Point((int) getCharacterLocation().getX() + x, (int) getCharacterLocation().getY() + y);
 		GridSpace gs = grid.get(getCharacterLocation());
 		GridSpace gs2 = grid.get(newLocation);
 
 		// Can move
 		// gs2.returnThings().size == 0
 		// !gs2.hasSolid()
 		// gs2.returnWeapons.size() !=0 && gs2.getLivingThings() == 0
 		// gs2.getLivingThings() //iterate over array list checking that
 		// !isSolid
 
 		if (gs2.returnThings().size() > 0) {
 			if (gs2.hasSolid()) {
 				if (gs2.returnWeapons().size() == 0) {
 					return;
 				} else {
 					for (LivingThing e : gs2.returnLivingThings()) {
 						if (e.getSolid()) {
 							return;
 						}
 					}
 					for (Terrain t : gs2.returnTerrain()) {
 						if (t.getSolid()) {
 							return;
 						}
 					}
 				}
 			}
 		}
 		Thing t = gs.remove(gs.returnThings().get(0));
 		gs2.add(t);
 		gs.sortArrayOfThings();
 		gs2.sortArrayOfThings();
 		grid.put(getCharacterLocation(), gs);
 		grid.put(newLocation, gs2);
 		setCharacterLocation(newLocation);
 
 	}
 
 	public void moveRangedWeapon() {
 		for (Entry<Point, GridSpace> e : grid.entrySet()) {
 			if (e.getValue().returnWeapons().size() > 0) {
 				if (e.getValue().returnWeapons().get(0) instanceof RangedWeapon) {
 					if (((RangedWeapon) (e.getValue().returnWeapons().get(0))).getCurrentSpeed() > 0) {
 						Point targetPoint = new Point((int) (e.getKey().getX() + 1), (int) (e.getKey().getY()));
 						GridSpace target = grid.get(targetPoint);
 						if (targetPoint.getX() == 101) {
 							e.getValue().remove(e.getValue().returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 						} else if (target.returnLivingThings().size() > 0) {
 							target.add(e.getValue().returnWeapons().get(0));
 							e.getValue().remove(e.getValue().returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 							dealDamage(target, target.returnWeapons().get(0), targetPoint);
 							target.remove(target.returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 						} else if (target.hasSolid()) {
 							e.getValue().remove(e.getValue().returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 						} else {
 							target.add(e.getValue().returnWeapons().get(0));
 							e.getValue().remove(e.getValue().returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 						}
 					} else if (((RangedWeapon) (e.getValue().returnWeapons().get(0))).getCurrentSpeed() < 0) {
 						Point targetPoint = new Point((int) (e.getKey().getX() - 1), (int) (e.getKey().getY()));
 						GridSpace target = grid.get(targetPoint);
 						if (targetPoint.getX() == -1) {
 							e.getValue().remove(e.getValue().returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 						} else if (target.returnLivingThings().size() > 0) {
 							target.add(e.getValue().returnWeapons().get(0));
 							e.getValue().remove(e.getValue().returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 							dealDamage(target, target.returnWeapons().get(0), targetPoint);
 							target.remove(target.returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 						} else if (target.hasSolid()) {
 							e.getValue().remove(e.getValue().returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 						} else {
 							target.add(e.getValue().returnWeapons().get(0));
 							e.getValue().remove(e.getValue().returnWeapons().get(0));
 							e.getValue().sortArrayOfThings();
 						}
 					}
 				}
 			}
 
 		}
 	}
 
 	public void moveEnemy(int x, int y, Point enemyToMove) {
 		Point newLocation = new Point((int) (enemyToMove.getX() + x), (int) (enemyToMove.getY() + y));
 		GridSpace gs = grid.get(enemyToMove);
 		GridSpace gs2 = grid.get(newLocation);
 		// if (this.characterLocation.getX() - this.enemyLocation.getX() >=
 		// 0) {
 		// newLocation.translate(1, 0);
 		// } else {
 		// newLocation.translate(-1, 0);
 		// }
 
 		if (gs2.returnThings().size() > 0) {
 			if (gs2.hasSolid()) {
 				if (gs2.returnWeapons().size() == 0) {
 					return;
 				} else {
 					for (LivingThing e : gs2.returnLivingThings()) {
 						if (e.getSolid()) {
 							return;
 						}
 					}
 					for (Terrain t : gs2.returnTerrain()) {
 						if (t.getSolid()) {
 							return;
 						}
 					}
 				}
 			}
 		}
 		Thing t = gs.remove(gs.returnThings().get(0));
 		gs2.add(t);
 		gs.sortArrayOfThings();
 		gs2.sortArrayOfThings();
 		grid.put(enemyToMove, gs);
 		grid.put(newLocation, gs2);
 		setEnemyLocation(newLocation, enemyToMove);
 		enemyToMove.translate(x, y);
 
 	}
 
 	public void makeDefaultGrid() {
 		for (int i = 0; i < 101; i++) {
 			for (int j = 0; j < 25; j++) {
 				GridSpace d = new GridSpace(new ArrayList<Thing>());
 				if (i == 100) {
 					d.add(new Terrain(false, Color.BLACK));
 				} else if (j >= 22) {
 					d.add(new Terrain(true, Color.GREEN));
 				} else if (j > 2) {
 
 				} else {
 					d.add(new Terrain(true, Color.DARK_GRAY));
 				}
 				d.sortArrayOfThings();
 				grid.put(new Point(i, j), d);
 			}
 		}
 		Forge f = new Forge();
 		ArrayList<Thing> things = new ArrayList<Thing>();
 		Character c = new Character(true, Color.BLUE);
 		c.setMaxHp(20);
 		c.setHp(20);
		c.setWeapon(f.constructMeleeWeapons(3, (LivingThing) c));
 		c.setWeapon(f.constructMagic(3, (LivingThing) c));
 		things.add(c);
 		GridSpace test = new GridSpace(things);
 		test.sortArrayOfThings();
 		ArrayList<Thing> enemies = new ArrayList<Thing>();
 		enemies.add(new Enemy(true, Color.ORANGE, "Jerome", 10, 10, 10));
 		grid.put(new Point(15, 21), test);
 		setCharacterLocation(new Point(15, 21));
 		things = new ArrayList<Thing>();
 		test = new GridSpace(things);
 		test.sortArrayOfThings();
 		GridSpace enemiesSpace = new GridSpace(enemies);
 		grid.put(new Point(20, 21), test);
 		grid.put(new Point(25, 19), enemiesSpace);
 		setEnemyLocation(new Point(25, 19));
 	}
 
 	public void useWeapon(int lastKeyPressed) {
 		int dir = 1;
 		Point charLoc = new Point(this.getCharacterLocation());
 		if (!(grid.get(charLoc).returnCharacter().getWeapon() instanceof RangedWeapon)) {
 			if (lastKeyPressed == KeyEvent.VK_A) {
 				dir = -1;
 			} else if (lastKeyPressed == KeyEvent.VK_D) {
 				dir = 1;
 			}
 			Point side = new Point((int) (getCharacterLocation().getX() + dir), (int) getCharacterLocation().getY());
 			GridSpace target = grid.get(side);
 
 			target.add(grid.get(charLoc).returnCharacter().getWeapon());
 			target.sortArrayOfThings();
 			dealDamage(target, grid.get(charLoc).returnCharacter().getWeapon(), side);
 
 		} else {
 			if (lastKeyPressed == KeyEvent.VK_A) {
 				dir = -1;
 			} else if (lastKeyPressed == KeyEvent.VK_D) {
 				dir = 1;
 			}
 			Point side = new Point((int) (getCharacterLocation().getX() + dir), (int) getCharacterLocation().getY());
 			GridSpace target = grid.get(side);
 			RangedWeapon middleMan = (RangedWeapon) grid.get(charLoc).returnCharacter().getWeapon();
 			RangedWeapon newWeapon = new RangedWeapon(middleMan.getSolid(), middleMan.getColor(), middleMan.getL(),
 					middleMan.getRange(), middleMan.getSpeed());
 			newWeapon.setDamage(middleMan.getDamage());
 			newWeapon.setCurrentSpeed(newWeapon.getSpeed() * dir);
 			target.add(newWeapon);
 			target.sortArrayOfThings();
 		}
 
 	}
 
 	private void dealDamage(GridSpace target, Weapon weapon, Point targetLocation) {
 		ArrayList<LivingThing> livingThings = target.returnLivingThings();
 		if (livingThings == null || livingThings.size() == 0) {
 			return;
 		} else {
 			for (LivingThing livingThing : livingThings) {
 				int hp = livingThing.getHp();
 				hp -= weapon.getDamage().getBaseHpDamage();
 				if (hp <= 0) {
 					target.remove(livingThing);
 					removeEnemy(targetLocation);
 					System.out.println("Killed that dude!");
 					numKilled++;
 				} else {
 					livingThing.setHp(hp);
 				}
 
 			}
 		}
 	}
 
 	public void retractWeapon(int lastKeyPressed) {
 		int dir = 1;
 		Point charLoc = new Point(this.getCharacterLocation());
 		if (!(grid.get(charLoc).returnCharacter().getWeapon() instanceof RangedWeapon)) {
 			if (lastKeyPressed == KeyEvent.VK_A) {
 				dir = -1;
 			} else if (lastKeyPressed == KeyEvent.VK_D) {
 				dir = 1;
 			}
 			Point side = new Point((int) (getCharacterLocation().getX() + dir), (int) getCharacterLocation().getY());
 			GridSpace target = grid.get(side);
 			target.remove(grid.get(charLoc).returnCharacter().getWeapon());
 			target.sortArrayOfThings();
 		}
 	}
 
 	public void characterDamage(Enemy e) {
 		if (grid.get(characterLocation).returnCharacter() == null) {
 			return;
 		} else {
 			grid.get(characterLocation).returnCharacter().updateHp(-5);
 			if (grid.get(characterLocation).returnCharacter().getHp() <= 0) {
 				grid.get(characterLocation).remove(grid.get(characterLocation).returnCharacter());
 				System.out.println("You died.");
 			}
 		}
 	}
 
 	public int getNumKilled() {
 		return numKilled;
 	}
 
 	public void setNumKilled(int numKilled) {
 		this.numKilled = numKilled;
 	}
 
 	public void spawnNewEnemy(Point point, Enemy enemy) {
 		ArrayList<Thing> enemies = new ArrayList<Thing>();
 		enemies.add(enemy);
 		GridSpace enemiesSpace = new GridSpace(enemies);
 		enemiesSpace.sortArrayOfThings();
 		grid.put(point, enemiesSpace);
 		setEnemyLocation(point);
 
 	}
 
 	public Point findValidEnemyLocation() {
 		Point p = null;
 		Random r = new Random();
 		for (int i = 0; i < 10000; i++) {
 			p = new Point(r.nextInt(100), r.nextInt(19) + 3);
 			if (!grid.get(p).hasSolid()) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	public void killAllEnemies() {
 		for (Entry<Point, GridSpace> e : grid.entrySet()) {
 			GridSpace gs = e.getValue();
 			gs.remove(gs.returnEnemy());
 			gs.sortArrayOfThings();
 		}
 		enemyLocations = new ArrayList<>();
 	}
 }
