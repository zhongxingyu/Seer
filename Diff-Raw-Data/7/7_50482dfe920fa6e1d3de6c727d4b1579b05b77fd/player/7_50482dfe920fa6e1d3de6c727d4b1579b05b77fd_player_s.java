 import java.awt.Point;
 
 
 
 public class player {
 	Point position;
 	
 	int hp = 10, fuel = 100, money = 0;
 	public player(int x, int y){
 		position = new Point(x, y);
 	}
 	
 	public void move(int dx, int dy){
 		position.translate(dx, dy);
 		game.refresh();
 	}
 
 	public Point getPosition() {
 		return position;
 	}
 
 	public void setPosition(Point position) {
 		this.position = position;
 	}
 
 	public int getHp() {
 		return hp;
 	}
 
 	public int getFuel() {
 		return fuel;
 	}
 
 	public int getMoney() {
 		return money;
 	}
 	
 	
 }
