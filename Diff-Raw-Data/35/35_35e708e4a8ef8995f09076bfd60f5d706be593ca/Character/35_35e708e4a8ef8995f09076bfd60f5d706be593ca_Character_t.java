 package Carbon;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
  
 public class Character
 {
	private int width, height, bWidth, bHeight, x, y, wx, wy, mx, my, health, maxHealth, score, space, r, money, speed;
 	private double xDifference, yDifference, angle;
 	private Rectangle rect;
 	private Weapon weap;
 	private Image still, bg;
 	private String name;
 	private boolean fired, reload;
 	
 	//Amount to offset weapon image by so it lines up with the hand
 	private int offX, pX, rX, sX, sgX, aX, snX;
 	private int offY, pY, rY, sY, sgY, aY, snY;
 	
 	//Distance from player to point of gun
 	private int disX, pistolX, revolverX, smgX, shotgunX, assaultX, sniperX;
 	private int disY, pistolY, revolverY, smgY, shotgunY, assaultY, sniperY;
 	
 	//Weapons
 	private Weapon pistol, revolver, smg, shotgun, assault, sniper;
 	
 	public Character()
 	{
 		still = new ImageIcon("assets/graphics/Character.png").getImage();
 		bg = new ImageIcon("assets/graphics/Background 2.png").getImage();
 		width = still.getWidth(null);
 		height = still.getHeight(null);
 		bWidth = bg.getWidth(null);
 		bHeight = bg.getHeight(null);
 		x = 100 - width;
 		y = 500 - height;
 		wx = 0;
 		wy = 0;
 		mx = x - width/2;
 		my = y - height/2;
 		health = 75;
 		maxHealth = 100;
 		score = 0;
 		space = 0;
 		r = 0;
 		money = 1000;
		speed = 10;
 		xDifference = 0;
 		yDifference = 0;
 		angle = 0;
 		rect = new Rectangle(x, y, 40, height);
 		fired = false;
 		reload = false;
 		pX = 93;
 		pY = 42;
 		rX = 92;
 		rY = 41;
 		sX = 85;
 		sY = 37;
 		sgX = 90;
 		sgY = 40;
 		aX = 60;
 		aY = 34;
 		snX = 56;
 		snY = 31;
 		pistol = new Weapon(x + pX, y + pY, 100, 10, 10, "Pistol");
 		revolver = new Weapon(x + rX, y + rY, 150, 6, 20, "Revolver");
 		smg = new Weapon(x + sX, y + sY, 200, 6, 5, "SMG");
 		shotgun = new Weapon(x + sgX, y + sgY, 75, 10, 10, "Shotgun");
 		assault = new Weapon(x + aX, y + aY, 500, 10, 25, "Assault Rifle");
 		sniper = new Weapon(x + snX, y + snY, 50, 10, 100, "Sniper Rifle");
 		weap = pistol;
 		offX = pX;
 		offY = pY;
 	}
 	
 	//Movement and location methods
 	public void setLocation(int x, int y)
 	{
 		this.x = x;
 		this.y = y;
 		keepInBounds();
 	}
 	public void moveLeft()
 	{
		moveLeft(speed);
 	}
 	public void moveLeft(int dx)
 	{
 		x -= dx;
 		keepInBounds();
 	}
 	public void moveRight()
 	{
		moveRight(speed);
 	}
 	public void moveRight(int dx)
 	{
 		x += dx;
 		keepInBounds();
 	}
 	public void jump()
 	{
 		
 	}
 	public void crouch()
 	{
 		
 	}
 	public void keepInBounds()
 	{
 		if (x < 0)
 			x = 0;
 		if (y < 0)
 			y = 0;
 		if(x + width > bWidth)
 			x = bWidth - width;
 		if (y + height > Menu.frame.getHeight())
 			y = Menu.frame.getHeight() - height;
 		rect.setLocation(x + 48, y);
 		weap.setLocation(x + offX, y + offY);
 		wx = weap.getOffsetX();
 		wy = weap.getOffsetY();
 	}
 	
 	//Get methods
 	public int getWidth()
 	{
 		return width;
 	}
 	public int getHeight()
 	{
 		return height;
 	}
 	public int getBWidth()
 	{
 		return bWidth;
 	}
 	public int getBHeight()
 	{
 		return bHeight;
 	}
 	public int getX()
 	{
 		return x;
 	}
 	public int getY()
 	{
 		return y;
 	}
 	public int getMX()
 	{
 		return mx;
 	}
 	public int getMY()
 	{
 		return my;
 	}
 	public int getHealth()
 	{
 		return health;
 	}
 	public int getMaxHealth()
 	{
 		return maxHealth;
 	}
 	public int getScore()
 	{
 		return score;
 	}
 	public int getSpace()
 	{
 		return space;
 	}
 	public int getR()
 	{
 		return r;
 	}
 	public int getMoney()
 	{
 		return money;
 	}
	public int getSpeed()
	{
		return speed;
	}
 	public String getName()
 	{
 		return name;
 	}
 	public Weapon getWeapon()
 	{
 		return weap;
 	}
 	public Image getImage()
 	{
 		return still;
 	}
 	public boolean getFired()
 	{
 		return fired;
 	}
 	public boolean getReload()
 	{
 		return reload;
 	}
 	public int getOffX()
 	{
 		return offX;
 	}
 	public int getOffY()
 	{
 		return offY;
 	}
 	public int getPX()
 	{
 		return pX;
 	}
 	public int getPY()
 	{
 		return pY;
 	}
 	public int getRX()
 	{
 		return rX;
 	}
 	public int getRY()
 	{
 		return rY;
 	}
 	public int getSX()
 	{
 		return sX;
 	}
 	public int getSY()
 	{
 		return sY;
 	}
 	public int getSGX()
 	{
 		return sgX;
 	}
 	public int getSGY()
 	{
 		return sgY;
 	}
 	public int getAX()
 	{
 		return aX;
 	}
 	public int getAY()
 	{
 		return aY;
 	}
 	public int getSNX()
 	{
 		return snX;
 	}
 	public int getSNY()
 	{
 		return snY;
 	}
 	public Weapon getPistol()
 	{
 		return pistol;
 	}
 	public Weapon getRevolver()
 	{
 		return revolver;
 	}
 	public Weapon getSMG()
 	{
 		return smg;
 	}
 	public Weapon getShotgun()
 	{
 		return shotgun;
 	}
 	public Weapon getAssault()
 	{
 		return assault;
 	}
 	public Weapon getSniper()
 	{
 		return sniper;
 	}
 	
 	//Set methods
 	public void setMX(int n)
 	{
 		mx = n;
 	}
 	public void setMY(int n)
 	{
 		my = n;
 	}
 	public void setMoney(int m)
 	{
 		money = m;
 	}
 	public void setName(String s)
 	{
 		name = s;
 	}
 	public void setR(int n)
 	{
 		r = n;
 	}
 	public void setSpace(int n)
 	{
 		space = n;
 	}
 	public void setWeapon(Weapon w)
 	{
 		weap = w;
 	}
 	public void setFired(boolean b)
 	{
 		fired = b;
 	}
 	public void setReload(boolean b)
 	{
 		reload = b;
 	}
 	public void setOffX(int x)
 	{
 		offX = x;
 	}
 	public void setOffY(int y)
 	{
 		offY = y;
 	}
 	//Other mutator methods
 	public void takeDamage(int n)
 	{
 		health -= n;
 	}
 	public void addR()
 	{
 		r++;
 	}
 	public void addSpace()
 	{
 		space++;
 	}
 	
 	//Weapon methods
 	public void updateWeapons()
 	{
 		pistol = new Weapon(x + pX, y + pY, 100, 10, 10, "Pistol");
 		revolver = new Weapon(x + rX, y + rY, 150, 6, 20, "Revolver");
 		smg = new Weapon(x + sX, y + sY, 200, 6, 5, "SMG");
 		shotgun = new Weapon(x + sgX, y + sgY, 75, 10, 10, "Shotgun");
 		assault = new Weapon(x + aX, y + aY, 500, 10, 25, "Assault Rifle");
 		sniper = new Weapon(x + snX, y + snY, 50, 10, 100, "Sniper Rifle");
 	}
 	public Bullet shoot()
 	{
 		xDifference = mx - wx;
 		yDifference = my - wy;
 		angle = Math.atan2(xDifference, yDifference);
 		return(weap.shoot(10, angle));
 	}
 	public void reload()
 	{
 		weap.reload();
 		space = 0;
 	}
 	
 	//Draw methods
 	public void draw(Graphics g)
 	{
 		g.setColor(new Color(100, 100, 100, 100));
 		g.fillRect(x, y, width, height);
 		//Clear color
 		//g.setColor(new Color(0, 0, 0, 0));
 		//White rectangle for testing
 		g.setColor(Color.WHITE);
 		g.fillRect(rect.x, rect.y, rect.width, rect.height);
 		g.drawImage(still, x, y, null);
 	}
 	public void undraw(Graphics g)
 	{
 		g.drawImage(bg, 0, 0, null);
 	}
 }
