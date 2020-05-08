 package main.java;
 
 public class PlayerAction {
 	Player player;
 	Map map;
 	ItemMap itemMap;
 	Inventory inventory;
 
 	public PlayerAction(Game game) {
 		this.player = game.player;
 		this.map = game.map;
 		this.itemMap = game.itemMap;
 		this.inventory = game.inventory;
 	}
 
 	public void move(int direction) {
 		if (direction == 1) {
 			// up
 			if (this.map.getMap(this.player.getY() - 1, this.player.getX()) != 1) {
 				this.player.move(0, -1);
 			}
 		} else if (direction == 2) {
 			// right
 			if (this.map.getMap(this.player.getY(), this.player.getX() + 1) != 1) {
 				this.player.move(1, 0);
 			}
 		} else if (direction == 3) {
 			// down
 			if (this.map.getMap(this.player.getY() + 1, this.player.getX()) != 1) {
 				this.player.move(0, 1);
 			}
 		} else if (direction == 4) {
 			// left
 			if (this.map.getMap(this.player.getY(), this.player.getX() - 1) != 1) {
 				this.player.move(-1, 0);
 			}
 		}
 	}
 
 	public void pickUpItem() {
 		int xp = this.player.getX();
 		int yp = this.player.getY();
 		if (this.itemMap.getItemMap(yp, xp) != 0) {
			int ItemID = this.itemMap.getItemMap(yp, xp);
			this.inventory.add(ItemID);
			this.itemMap.ItemMapDesign[yp][xp] = 0;
 		}
 	}
 
 	public void fight() {
 	}
 
 	public void openInventory() {
 		this.inventory.ausgabe();
 	}
 }
