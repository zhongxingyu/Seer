 package Carbon;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import java.util.*;
 
 public class Panel extends JPanel implements KeyListener, MouseListener, MouseMotionListener
 {
 	static Panel mainPanel;
 	static Character c;
 	static Enemy e;
 	Image bi;
 	Graphics bg;
 	static ArrayList<Integer> keys;
 	static ArrayList<Bullet> bullets;
 	static ArrayList<Obstacle> obstacles;
 	static ArrayList<Enemy> enemies;
 	boolean redraw = true;
 	static int xx, yy, ll, k, ri, li;
 	Obstacle ob1, ob2, ob3, ob4, ob5, ob6, ob7;
 	
 	public Panel()
 	{
 		c = new Character();
 		e = new Enemy();
 		setFocusable(true);
 		requestFocus();
 		addKeyListener(this);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		keys = new ArrayList<Integer>();
 		bullets = new ArrayList<Bullet>();
 		obstacles = new ArrayList<Obstacle>();
 		enemies = new ArrayList<Enemy>();
 		xx = 0;
 		yy = 0;
 		ll = 30;
 		k = c.getBWidth();
 		ri = 0;
 		li = 0;
 		
 		ob1 = new Obstacle(100, 100);
 		ob2 = new Obstacle(100, c.getY() - ob1.getHeight());
 		ob3 = new Obstacle(100, c.getY() + c.getHeight());
 		ob4 = new Obstacle(200, c.getY() - ob1.getHeight());
 		ob5 = new Obstacle(200, c.getY() + c.getHeight());
 		ob6 = new Obstacle(400, c.getY() - ob1.getHeight());
 		ob7 = new Obstacle(400, c.getY() + c.getHeight());
 		obstacles.add(ob1);
 		obstacles.add(ob2);
 		obstacles.add(ob3);
 		obstacles.add(ob4);
 		obstacles.add(ob5);
 		obstacles.add(ob6);
 		obstacles.add(ob7);
 	}
 	
 	public void paint(Graphics g)
 	{
 		if (getWidth() != Menu.frame.getWidth() || getHeight() != Menu.frame.getHeight())
 			Menu.frame.setPreferredSize(new Dimension(Menu.frame.getWidth() * 2 - getWidth(), Menu.frame.getHeight() * 2 - getHeight()));
 		if (bg == null)
 		{
 			while (bi == null)
 				bi = createImage(k, Menu.frame.getHeight());
 			while (bg == null)
 				bg = bi.getGraphics();
 		}
 		draw(bg);
 		paintBullet(bg);
 		paintObstacle(bg);
 		g.drawImage(bi, xx, yy, this);
 		repaint();
 	}
 	
 	public void paintBullet(Graphics g)
 	{
 			for(Bullet b:bullets)
 				b.draw(g);
 	}
 	
 	public void paintObstacle(Graphics g)
 	{
 			for(Obstacle o:obstacles)
 				o.draw(g);
 	}
 	
 	public void draw(Graphics g)
 	{
 		c.undraw(g);
 		update();
 		
 		if(redraw)
 		{
 			g.setColor(Color.BLACK);
 			g.fillRect(0, 0, k, Menu.frame.getHeight());
 			redraw = false;
 		}
 		
 		c.getWeapon().draw(g);
 		c.draw(g);
 		e.draw(g);
 		
 		g.setColor(Color.BLACK);
 		g.setFont(new Font("FFF Galaxy", Font.PLAIN, 20));
 		g.drawString("Name: " + c.getName(), 50 - xx, 580);
 		g.drawString("HP: " + c.getHealth(), 50 - xx, 610);
 		g.drawString("Ammo: " + c.getWeapon().getAmmo(), 50 - xx, 640);
 		g.drawString("Score: " + c.getScore(), 50 - xx, 670);
 		g.drawString("Location: " + c.getMX() + " " + c.getMY(), 50 - xx, 700);
 		g.drawString("Money: "+ c.getMoney(), 50 - xx, 730);
 		
 		g.setColor(Color.WHITE);
 		g.fillRect(c.getX() + 41, c.getY() - 25, c.getMaxHealth() / 2, 5);
 		g.setColor(Color.GREEN);
 		g.fillRect(c.getX() + 41, c.getY() - 25, c.getHealth() / 2, 5);
 	}
 	
 	public void update()
 	{
 		try
 		{
 			Thread.sleep(5);
 		}
 		catch(Exception e){}
 		
 		if (keys.contains(KeyEvent.VK_D))
 		{
 			for (Obstacle ob:obstacles)
 			{
 				if (c.getY() + c.getHeight() > ob.getY() && c.getY() < ob.getY() + ob.getHeight() &&
						(c.getX() + c.getSpeed() + c.getWidth() > ob.getX() && c.getX() + c.getSpeed() + c.getWidth() < ob.getX() + ob.getWidth()))
 					ri = -1;
 			}
 			if (ri > 0)
 			{
 				c.moveRight();
 				if((xx * -1) < k - Menu.frame.getWidth() && Menu.frame.getWidth() * .5 - xx < c.getX())
					xx = xx - c.getSpeed();
 			}
 			ri = 1;
 		}
 		if (keys.contains(KeyEvent.VK_A))
 		{
 			for (Obstacle ob:obstacles)
 			{
 				if (c.getY() + c.getHeight() > ob.getY() && c.getY() < ob.getY() + ob.getHeight() &&
						(c.getX() - c.getSpeed() + c.getWidth() < ob.getX() && c.getX() - c.getSpeed() + c.getWidth() > ob.getX() + ob.getWidth()))
 					li = -1;
 			}
 			if(li > 0)
 			{
 				c.moveLeft();
 				if (xx < 0 && (Menu.frame.getWidth() * .2 - xx) > c.getX())
					xx = xx + c.getSpeed();
 			}
 			li = 1;
 		}
 		if (keys.contains(KeyEvent.VK_W))
 			c.jump();
 		if (keys.contains(KeyEvent.VK_S))
 			c.crouch();
 		if (keys.contains(KeyEvent.VK_R))
 		{
 			c.addR();
 			c.setReload(true);
 		}
 		if (!keys.contains(KeyEvent.VK_R))
 			c.setReload(false);
 		if (keys.contains(KeyEvent.VK_P))
 		{
 			//keys.remove(new Integer(KeyEvent.VK_P));
 			Menu.frame.getContentPane().removeAll();
 			Menu.frame.getContentPane().add(Menu.myPanel);
 			Menu.frame.getContentPane().revalidate(); 
 			Menu.frame.getContentPane().repaint();
 			Menu.myPanel.setFocusable(true);
 			Menu.myPanel.requestFocusInWindow();
 		}
 		if (keys.contains(KeyEvent.VK_O))
 		{
 			//keys.remove(new Integer(KeyEvent.VK_O));
 			Menu.frame.getContentPane().removeAll();
 			Menu.frame.getContentPane().add(Menu.store.storepanel);
 			Menu.frame.getContentPane().revalidate(); 
 			Menu.frame.getContentPane().repaint();
 			Menu.myPanel.setFocusable(true);
 			Menu.myPanel.requestFocusInWindow();
 		}
 		
 		if (c.getSpace() >= 1 && c.getWeapon().getAmmo() > 0 && c.getFired() == false)
 		{
 			Bullet newBullet = c.shoot();
 			bullets.add(newBullet);
 			c.setSpace(0);
 		}
 		if (c.getR() >= 1 && c.getReload() == false)
 		{
 			c.reload();
 			c.setR(0);
 		}
 		for (int i = 0; i < bullets.size(); i++)
 		{
 			bullets.get(i).move();
 			if ((bullets.get(i).getX() > Menu.frame.getWidth() + 10) || (bullets.get(i).getX() < 0))
 			{
 				bullets.get(i).kill();
 				bullets.remove(i);
 				i--;
 				continue;
 			}
 			if ((bullets.get(i).getY() > Menu.frame.getHeight() + 10) || (bullets.get(i).getY() < 0))
 			{
 				bullets.get(i).kill();
 				bullets.remove(i);
 				i--;
 				continue;
 			}
 		}
 	}
 	
 	//Listener methods
 	public void keyPressed(KeyEvent e)
 	{
 		if (!(Panel.keys.contains(e.getKeyCode())))
 			keys.add(e.getKeyCode());
 	}
 	public void keyTyped(KeyEvent e){}
 	public void keyReleased(KeyEvent e)
 	{
 		keys.remove(new Integer(e.getKeyCode()));
 	}
 	
 	public void mouseMoved(MouseEvent e)
 	{
 		c.setMX(e.getX());
 		c.setMY(e.getY());
 	}
 	public void mousePressed(MouseEvent e)
 	{
 		c.addSpace();
 		c.setFired(true);
 	}
 	public void mouseReleased(MouseEvent e)
 	{
 		c.setFired(false);
 	}
 	public void mouseClicked(MouseEvent e)
 	{
 		
 	}
 	public void mouseDragged(MouseEvent arg0)
 	{
 		
 	}
 	public void mouseEntered(MouseEvent e)
 	{
 		
 	}
 	public void mouseExited(MouseEvent e)
 	{
 		
 	}
 }
