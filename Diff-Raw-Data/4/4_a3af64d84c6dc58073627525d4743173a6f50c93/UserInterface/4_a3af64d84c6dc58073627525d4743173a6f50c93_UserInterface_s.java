 package fr.umlv.escape.front;
 
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 import fr.umlv.escape.R;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.game.Player;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.weapon.ListWeapon;
 import fr.umlv.escape.weapon.Weapon;
 
 /**
  * Contains tools functions who help to print the User Interface in game
  */
 public class UserInterface {
 	
 	private UserInterface(){}
 	
 	/**
 	 * return the {@link weapon} if click is on icon weapon
 	 * @param p position of click user
 	 * @return the weapon or null if click isn't on icon's weapon
 	 */
 	public static Weapon clickIsWeaponSelect(Point p){
 		List<Weapon> weaponList = Game.getTheGame().getPlayer1().getShip().getListWeapon().getWeapons();
 		for(Weapon w : weaponList){
 			Point highLeft = w.getHighLeft();
 			Point downRight = w.getDownRight();
 
 			if(
 					highLeft != null &&
 					downRight != null &&
 					p.y > highLeft.y && //up
 					p.x < downRight.x &&//right
 					p.y < downRight.y &&//bottom
 					p.x > highLeft.x //left
 					){
 				return w;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Return the level to launch
 	 * @param p position of click by player
 	 * @return level number or -1 if don't click on icon 
 	 */
 	public static int getLevelToLaunch(Point p){
 		int height = (FrontApplication.HEIGHT - 100)/2;
 		if(p.y > height && p.y < height + 100){
 			if(p.x > 100 && p.x < 200){
 				return 1;
 			} else if(p.x > 260 && p.x < 360){
 				return 2;
 			} else if(p.x > 420 && p.x < 520){
 				return 3;
 			}
 		}
 		return -1;
 	}
 	
 	public static void drawUIScoresAndLife(Canvas canvas){
 		Player player = Game.getTheGame().getPlayer1();
 		String life = String.valueOf(player.getLife());
 		String health = String.valueOf(player.getShip().getHealth());
 		String score = String.valueOf(player.getScore());
 		
 		Paint p = new Paint();
 		FrontImages fi = FrontApplication.frontImage;
 		p.setTextSize(25);
 		
 		Bitmap bmp = fi.getImage("hearth");
 		canvas.drawBitmap(bmp, 10, 10, p);
 		canvas.drawText(life, 60, 40, p);
 		bmp = fi.getImage("health");
 		canvas.drawBitmap(bmp, 90, 10, p);
 		canvas.drawText(health, 130, 40, p);
 		canvas.drawText(score, 200, 40, p);
 	}
 	
 	public static void drawWeaponsIcons(Canvas canvas){
 		Ship s = Game.getTheGame().getPlayer1().getShip();
 		Weapon current_weapon = s.getCurrentWeapon();
 		List<Weapon> weapons = s.getListWeapon().getWeapons();
 		Paint p = new Paint();
 		FrontImages fi = FrontApplication.frontImage;
 		p.setStyle(Paint.Style.STROKE);
 		Bitmap bmp;
 		
 		for(int i=0; i<weapons.size();i++){
 			Weapon w = weapons.get(i);
 			bmp = fi.getImage(w.getName());
 			if(w == current_weapon){
 				p.setColor(Color.BLUE);
 			}
			canvas.drawRect(FrontApplication.WIDTH - 60, 50+i*75, FrontApplication.WIDTH - 10, 110+i*75, p);
			canvas.drawBitmap(bmp, FrontApplication.WIDTH - 60, 70+i*75, p);
 			p.setColor(Color.BLACK);
 		}
 	}
 }
