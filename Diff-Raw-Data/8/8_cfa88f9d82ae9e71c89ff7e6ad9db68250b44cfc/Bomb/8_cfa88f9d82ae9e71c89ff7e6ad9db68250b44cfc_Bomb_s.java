 
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Bomb {
 	Point position;
 	int count;
 	Bomberman bomberman;
 	boolean isVisible;
 	boolean isExploded;
 	int radius;
 	List<Point> explosionArray;
 	Player owner;
 	
 	public Bomb(Bomberman bomberman) {
 		this.bomberman = bomberman;
 	}
 	
 	/**
 	 * Verarbeitet die Aktionen der Bombe
 	 */
 	public void process() {
 		if (isVisible) {
 			if (count == 0) {
 				bomberman.playSound(bomberman.soundMap.get("ticktock"));
 			}
 			if (count == 2000 / 25 /* 2 Sekunden */) {
 				explosionArray = getExplosionArray();
 				isExploded = true;
 				bomberman.playSound(bomberman.soundMap.get("bomb"));
 			}
 			if (count == 3000 / 25 /* 3 Sekunden */) {
 				isVisible = false;
 				owner.setActiveBombs(owner.getActiveBombs()-1);
 				Point origin = new Point(getExplosionArray().get(0));
 				
 				
 				
 				// Bomben kollision
 				for(int i = 1; i <= radius; i++){
 					if(origin.y-i <= 12 && bomberman.stage.isPointOnField(new Point(origin.x, origin.y-i), Stage.BLOCK)){
 						break;
 					}
 					if(origin.y-i >= 0 && bomberman.stage.isPointOnField(new Point(origin.x, origin.y-i), Stage.BOX)){
 						bomberman.stage.destroyBox(new Point(origin.x, origin.y-i));
 						break;
 					}
 				}
 				
 				for(int i = 1; i <= radius; i++){
 					if(origin.y+i <= 12 && bomberman.stage.isPointOnField(new Point(origin.x, origin.y+i), Stage.BLOCK)){
 						break;
 					}
 					if(origin.y+i <= 12 && bomberman.stage.isPointOnField(new Point(origin.x, origin.y+i), Stage.BOX)){
 						bomberman.stage.destroyBox(new Point(origin.x, origin.y+i));
 						break;
 					}
 				}
 				
 				for(int i = 1; i <= radius; i++){
 					if(origin.x-i <= 12 && bomberman.stage.isPointOnField(new Point(origin.x-i, origin.y), Stage.BLOCK)){
 						break;
 					}
 					if(origin.x-i >= 0 &&bomberman.stage.isPointOnField(new Point(origin.x-i, origin.y), Stage.BOX)){
 						bomberman.stage.destroyBox(new Point(origin.x-i, origin.y));
 						break;
 					}
 				}
 				
 				for(int i = 1; i <= radius; i++){
 					if(origin.x+i <= 12 && bomberman.stage.isPointOnField(new Point(origin.x+i, origin.y), Stage.BLOCK)){
 						break;
 					}
 					if(origin.x+i <= 16 && bomberman.stage.isPointOnField(new Point(origin.x+i, origin.y), Stage.BOX)){
 						bomberman.stage.destroyBox(new Point(origin.x+i, origin.y));
 						break;
 					}
 				}
 			}
 			count++;
 		}
 	}
 	
 	/**
 	 * Legt eine Bombe auf die Feld-Position auf der der Spieler1 steht
 	 */
 	public void setToPlayerPos() {
 		position = new Point(bomberman.player.getStagePosition());
 		count = 0;
 		isVisible = true;
 		isExploded = false;
 		radius = 4;
 	}
 	
 	/**
 	 * Legt eine Bombe auf die Feld-Position auf der der Spieler2 steht
 	 */
 	public void setToPlayer2Pos() {
 		position = new Point(bomberman.player2.getStagePosition());
 		count = 0;
 		isVisible = true;
 		isExploded = false;
 		radius = 4;
 	}
 	
 	/**
 	 * prft ob die Bombe auf dem feld an Punkt p gesetzt werden kann oder ob dort schon eine bombe liegt
 	 * @param p Punkt an dem grprft wird
 	 * @return
 	 */
 	public boolean canLayOn(Point p){
 		boolean erg = true;
 		for (Bomb bomb : bomberman.bombList) {
			if (p.equals(bomb.position)) {
 				erg = false;
 				break;
 			}
 		}
 		return erg;
 	}
 	
 	/**
 	 * Prft ob an der Feld-Position p eine Explosion sichtbar sein darf
 	 * @param p Feld-Position
 	 * @return
 	 */
 	
 	public boolean canExplodeTo(Point p) {
 		if (bomberman.stage.isPointOnField(p, Stage.BLOCK) || bomberman.stage.isPointOnField(p, Stage.BOX)) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Setzt den Besitzer der Bombe
 	 * @param player Player, der die Bombe gelegt hat
 	 */
 	
 	public void setOwner(Player player){
 	this.owner = player;	
 	}
 	
 	/**
 	 * Erstellt ein Array der Feld-Positionen an denen eine Explosion sichtbar sein wird
 	 * @return
 	 */
 	public List<Point> getExplosionArray() {
 		List<Point> explosionArray = new ArrayList<Point>();
 		if (radius > 0)
 			explosionArray.add(position);
 		for (int i = 1; i < radius; i++) {
 			if (canExplodeTo(new Point(position.x + i, position.y))) {
 				explosionArray.add(new Point(position.x + i, position.y));
 			}
 			else {
 				break;
 			}
 		}
 		
 		for (int i = 1; i < radius; i++) {
 			if (canExplodeTo(new Point(position.x - i, position.y))) {
 				explosionArray.add(new Point(position.x - i, position.y));
 			}
 			else {
 				break;
 			}
 		}
 		
 		for (int i = 1; i < radius; i++) {
 			if (canExplodeTo(new Point(position.x, position.y + i))) {
 				explosionArray.add(new Point(position.x, position.y + i));
 			}
 			else {
 				break;
 			}
 		}
 		
 		for (int i = 1; i < radius; i++) {
 			if (canExplodeTo(new Point(position.x, position.y - i))) {
 				explosionArray.add(new Point(position.x, position.y - i));
 			}
 			else {
 				break;
 			}
 		}
 		
 		return explosionArray;
 	}
 }
