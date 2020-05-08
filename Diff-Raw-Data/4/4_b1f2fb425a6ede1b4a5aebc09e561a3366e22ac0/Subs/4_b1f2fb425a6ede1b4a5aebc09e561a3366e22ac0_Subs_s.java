 // Play this game!!!
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.awt.geom.*;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.ListIterator;
 import javazoom.jl.player.*;
 
 public class Subs extends JPanel {
 	public BufferedImage myImage;
 	public Graphics2D buffer;
 	public Timer t = new Timer(30, new Listener());
 	public Timer time = new Timer(1000, new TimeListener());
 	final static int screenwidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()), screenheight = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
 	final static int LEFT = 0, UP = 1, RIGHT = 2, DOWN = 3, SPACE = 4, B = 5, F = 6, R = 7, V = 8;
 	boolean[] keys = new boolean[9];
 	final static int RUNNING = 1, MENU=2;
 	int gamestate=RUNNING;
 	public int screenystart = 500;
 	public int subxpos = 50;
 	public int subypos = 350;
 	int seconds = 0;
 	int count = 1;
 	public boolean air = false;
 	public boolean music = false;
 	public int hitpoints = 25;
 	public int hitpointfrac = 0;
 	public int reloadtime = 10;
 	public int killcount = 0;
 	public int points = 0;
 	public int BGcount = 0;
 	public ArrayList<missile> currentmissiles = new ArrayList<missile>();
 	public ArrayList<enemy> currentenemies = new ArrayList<enemy>();
 	public ArrayList<missile> leftovermissiles = new ArrayList<missile>();
 	public ArrayList<explosion> currentexplosions = new ArrayList<explosion>();
 	public InputStream gamemusic;
 	public Player player;
 	public PlayerThread pt;
 	ImageIcon bgii = new ImageIcon("Images/DeepSeaBG.png");
 	Image bgwater = bgii.getImage();
 	ImageIcon bgi = new ImageIcon("Images/SkylineBG.png");
 	Image bgair = bgi.getImage();
 	ArrayList<Integer> mtnxs = new ArrayList<Integer>();
 	ArrayList<Integer> mtnsizes = new ArrayList<Integer>();
 	ArrayList<Integer> mtnnums = new ArrayList<Integer>();
 	public int difficulty = 0;
 //       ArrayList<Integer> icexs = new ArrayList<Integer>();
 //       ArrayList<Integer> icesizes = new ArrayList<Integer>();
 //       ArrayList<Integer> icenums = new ArrayList<Integer>();
 	public Subs() {
 		initMusic();
 		myImage = new BufferedImage(screenwidth, screenheight, BufferedImage.TYPE_INT_RGB);
 		buffer = (Graphics2D)myImage.getGraphics();
 		addKeyListener(new KeyListener());
 		setFocusable(true);
 		mtnxs.add(100);
 		mtnxs.add(600);
 		mtnxs.add(1100);
 		mtnsizes.add(300);
 		mtnsizes.add(150);
 		mtnsizes.add(200);
 		mtnnums.add((int)(Math.random()*3) + 1);
 		mtnnums.add((int)(Math.random()*3) + 1);
 		mtnnums.add((int)(Math.random()*3) + 1);
 		difficulty = 0;
 		gamestate=MENU;
 //          icexs.add(100);
 //          icexs.add(550);
 //          icexs.add(950);
 //          icesizes.add(150);
 //          icesizes.add(300);
 //          icesizes.add(200);
 //          icenums.add((int)(Math.random()*2)+1);
 //          icenums.add((int)(Math.random()*2)+1);
 //          icenums.add((int)(Math.random()*2)+1);
 		t.start();
 		time.start();
 	}
 	public void newGame(int d) {
 		difficulty=d;
 		subxpos = 50;
 		subypos = 350;
 		gamestate=RUNNING;
 
 	}
 	public void move() {
 		if (keys[UP]) {
 			subypos -= 8;
 		}
 		if (keys[DOWN]) {
 			subypos += 8;
 		}
 		if (keys[LEFT]) subxpos -= 8;
 		if (keys[RIGHT]) subxpos += 8;
 		if (keys[F]) {
 			if (reloadtime >= 4) {
 				missile m = new missile(subxpos + 125, subypos + 28, "Right");
 				buffer.drawImage(m.getImage(), m.getX(), m.getY(), m.getWidth(), m.getHeight(), null);
 				currentmissiles.add(m);
 				reloadtime = 0;
 			}
 		}
 		else if (keys[V]) {
 			if (reloadtime >= 4) {
 				missile m = new missile(subxpos + 125, subypos + 38, "UpRight");
 				buffer.drawImage(m.getImage(), m.getX(), m.getY(), m.getWidth(), m.getHeight(), null);
 				currentmissiles.add(m);
 				reloadtime = 0;
 			}
 		}
 		else if (keys[R]) {
 			if (reloadtime >= 4) {
 				missile m = new missile(subxpos + 125, subypos + 38, "DownRight");
 				buffer.drawImage(m.getImage(), m.getX(), m.getY(), m.getWidth(), m.getHeight(), null);
 				currentmissiles.add(m);
 				reloadtime = 0;
 			}
 		}
 		reloadtime++;
 		if (subxpos < 0) subxpos = 0;
 		if (subypos < 38) subypos = 38;
 		if (subxpos > screenwidth - 125) {
 			if (gamestate==MENU){
 				newGame((int)(9.0*subypos/screenheight+1));
 			}
			subxpos = screenwidth - 125;
 		}
 		if (subypos > screenheight - 38) {
 			subypos = screenheight - 38;
 		}
 	}
 	public void movemissile() {
 		ListIterator it = currentmissiles.listIterator();
 		missile m;
 		while (it.hasNext()) {
 			m = (missile)it.next();
 			m.move();
 			ListIterator enemies = currentenemies.listIterator();
 			enemy e;
 			boolean hit = false;
 			while (enemies.hasNext()) {
 				e = (enemy)enemies.next();
 				if (m.getX() > e.getX() - 10 && m.getX() < e.getX() + 60
 				        && m.getY() > e.getY() - 10 && m.getY() < e.getY() + 60) {
 					hit = true;
 					currentexplosions.add(new explosion(m.getX(), m.getY(), 10));
 					e.takeDamage();
 					if (e.getHP() <= 0) {
 						killcount++;
 						points += e.getLevel() * 1000;
 						currentexplosions.add(new explosion(e.getX(), e.getY(), e.getSize()));
 						leftovermissiles.addAll(e.getMissiles());
 						enemies.remove();
 					}
 				}
 				ArrayList<missile> ems = e.getMissiles();
 				Iterator emit = ems.iterator();
 				while (emit.hasNext()) {
 					missile em = (missile)emit.next();
 					if (m.getX() > em.getX() - 10 && m.getX() < em.getX() + 20
 					        && m.getY() > em.getY() - 10 && m.getY() < em.getY() + 20) {
 						currentexplosions.add(new explosion(em.getX(), em.getY(), 10));
 						emit.remove();
 						hit = true;
 					}
 				}
 			}
 			if (hit) {
 				it.remove();
 				points += 100;
 			}
 			else if (m.getX() > screenwidth || m.getX() < 0) {
 				it.remove();
 			}
 			if (m.getDir().equals("Right")) buffer.drawImage(m.getImage(), m.getX() - 20, m.getY(), m.getWidth(), m.getHeight(), null);
 			else buffer.drawImage(m.getImage(), m.getX(), m.getY() - 20, m.getWidth(), m.getHeight(), null);
 		}
 	}
 	public void moveleftovers() {
 		ListIterator missileiter = leftovermissiles.listIterator();
 		while (missileiter.hasNext()) {
 			missile m = (missile)missileiter.next();
 			m.move();
 			if (m.getX() >= subxpos - 10 && m.getX() < subxpos + 125
 			        && m.getY() >= subypos - 10 && m.getY() < subypos + 38) {
 				hitpoints--;
 				currentexplosions.add(new explosion(m.getX(), m.getY(), 10));
 				missileiter.remove();
 			}
 			else if (m.getX() > screenwidth || m.getX() + 10 < 0) {
 				missileiter.remove();
 			}
 			buffer.drawImage(m.getImage(), m.getX() + 20, m.getY(), m.getWidth(), m.getHeight(), null);
 		}
 	}
 	public void drawBG() {
 		//          buffer.drawImage(bgair,0,0-screenystart,1280,700,null);
 		buffer.drawImage(bgwater, 0, 0, 1280, 800, null);
 		for (int counter = 0; counter < mtnxs.size(); counter++) {
 			int nextx = mtnxs.get(counter);
 			int nextsize = mtnsizes.get(counter);
 			int nextnum = mtnnums.get(counter);
 			mtnxs.set(counter, nextx - 1);
 			ImageIcon mtn = new ImageIcon("Images/Mountain-" + nextnum + ".png");
 			buffer.drawImage(mtn.getImage(), nextx, 800 - nextsize, nextsize, nextsize, null);
 		}
 //          for(int counter = 0; counter < icexs.size(); counter++)
 //          {
 //             int nextx = icexs.get(counter);
 //             int nextsize = icesizes.get(counter);
 //             int nextnum = icenums.get(counter);
 //             icexs.set(counter,nextx-1);
 //             ImageIcon ice = new ImageIcon("Images/Iceberg-" + nextnum + ".png");
 //             buffer.drawImage(ice.getImage(),nextx,0,nextsize,nextsize,null);
 //          }
 //          if(Math.random() > .99)
 //          {
 //             int lastice = 0;
 //             int lastsize = 0;
 //             if(icexs.size() > 0) lastice = icexs.get(icexs.size()-1);
 //             if(icesizes.size() > 0) lastsize = icesizes.get(icesizes.size()-1);
 //             if(lastice+lastsize < screenwidth)
 //             {
 //                int newice = screenwidth-1;
 //                int newsize = (int)((Math.random()*200)+100);
 //                icexs.add(newice);
 //                icesizes.add(newsize);
 //                icenums.add((int)((Math.random()*3)+1));
 //             }
 //          }
 		if (Math.random() > .9) {
 			int lastmtn = 0;
 			int lastsize = 0;
 			if (mtnxs.size() > 0) lastmtn = mtnxs.get(mtnxs.size() - 1);
 			if (mtnsizes.size() > 0) lastsize = mtnsizes.get(mtnsizes.size() - 1);
 			if (lastmtn + lastsize < screenwidth) {
 				int newmtn = screenwidth - 1;
 				int newsize = (int)((Math.random() * 200) + 100);
 				mtnxs.add(newmtn);
 				mtnsizes.add(newsize);
 				mtnnums.add((int)((Math.random()*2) + 1));
 			}
 		}
 		if (keys[DOWN] && !keys[UP]) {
 			AffineTransform save = buffer.getTransform();
 			AffineTransform tilt = (AffineTransform)(save.clone());
 			tilt.rotate(170, subxpos + 62, subypos + 19);
 			buffer.setTransform(tilt);
 			ImageIcon sub = new ImageIcon("Images/submarine.png");
 			Image submarine = sub.getImage();
 			buffer.drawImage(submarine, subxpos, subypos, 125, 38, null);
 			buffer.setTransform(save);
 		}
 		if (keys[UP] && !keys[DOWN]) {
 			AffineTransform save = buffer.getTransform();
 			AffineTransform tilt = (AffineTransform)(save.clone());
 			tilt.rotate(-170, subxpos + 62, subypos + 19);
 			buffer.setTransform(tilt);
 			ImageIcon sub = new ImageIcon("Images/submarine.png");
 			Image submarine = sub.getImage();
 			buffer.drawImage(submarine, subxpos, subypos, 125, 38, null);
 			buffer.setTransform(save);
 		}
 		if (!keys[UP] && !keys[DOWN]) {
 			ImageIcon sub = new ImageIcon("Images/submarine.png");
 			Image submarine = sub.getImage();
 			buffer.drawImage(submarine, subxpos, subypos, 125, 38, null);
 		}
 		BGcount++;
 		if (BGcount >= 5000 - screenwidth) BGcount = 0;
 	}
 	public void drawScore() {
 		buffer.setColor(Color.red);
 		buffer.setFont(new Font("Default", 0, 32));
 		buffer.drawString("Time: " + seconds, 100, 75);
 		buffer.drawString("Life: " + hitpoints + "/25", 400, 75);
 		buffer.drawString("Kills: " + killcount, 700, 75);
 		buffer.drawString("Score: " + points, 1000, 75);
 		repaint();
 	}
 	public void drawMenu() {
 		buffer.setColor(Color.red);
 		buffer.setFont(new Font("Default", 0, 64));
 		buffer.drawString("Subs", 550, 100);
 		buffer.setColor(Color.black);
 		buffer.setFont(new Font("Default", 0, 32));
 		buffer.drawString("Welcome to SUBS! By Jeremy Vercillo", 50, 160);
 		buffer.drawString("Move with the Arrow Keys.", 50, 190);
 		buffer.drawString("Fire with:", 50, 250);
 		buffer.drawString("V (diagonally downward shot)", 300, 250);
 		buffer.drawString("F (forward shot)", 300, 280);
 		buffer.drawString("R (diagonally upward shot)", 300, 310);
 		buffer.drawString("Kill as many enemies as you can before you die!", 50, 340);
 		//Level Select:
 		buffer.setColor(Color.yellow);
 		for (int i =1; i<=9; i++) {
 			buffer.drawString(""+i, screenwidth-50, screenheight/9*i-15);
 		}
 		repaint();
 	}
 	public void moveenemies() {
 		double rand = Math.random();
 		if (rand <= (.01 * count / 10 * difficulty)) {
 			int level = (int)(Math.random() * 4) + 1;
 			int newx = screenwidth - 1;
 			int newy = (int)(Math.random() * (screenheight - 50)) + 50;
 			enemy newenemy = new standardenemy(level, newx, newy);
 			currentenemies.add(newenemy);
 		}
 		if (rand <= (.01 * count * difficulty / 5)) {
 			int newx = (int)(Math.random() * (screenwidth - 400)) + 400;
 			int newy = (int)(Math.random() * (screenheight - 50)) + 50;
 			enemy newspike = new spikeballenemy(1, newx, newy);
 			currentenemies.add(newspike);
 		}
 		ListIterator iter = currentenemies.listIterator();
 		enemy e;
 		while (iter.hasNext()) {
 			e = (enemy)iter.next();
 			e.move();
 			ArrayList<missile> temp = e.getMissiles();
 			ListIterator missileiter = temp.listIterator();
 			missile emissile;
 			while (missileiter.hasNext()) {
 				emissile = (missile)missileiter.next();
 				emissile.move();
 				if (emissile.getX() >= subxpos - 10 && emissile.getX() < subxpos + 125
 				        && emissile.getY() >= subypos - 10 && emissile.getY() < subypos + 38) {
 					hitpoints--;
 					currentexplosions.add(new explosion(emissile.getX(), emissile.getY(), 10));
 					missileiter.remove();
 				}
 				else if (emissile.getX() > screenwidth || emissile.getX() + 10 < 0) {
 					missileiter.remove();
 				}
 				buffer.drawImage(emissile.getImage(), emissile.getX() + 20, emissile.getY(), emissile.getWidth(), emissile.getHeight(), null);
 			}
 			if (e.getX() >= subxpos - 50 && e.getX() < subxpos + 125
 			        && e.getY() >= subypos - 50 && e.getY() < subypos + 28) {
 				hitpoints -= 5;
 				killcount++;
 				points += e.getLevel() * 1000;
 				currentexplosions.add(new explosion(e.getX(), e.getY(), e.getSize()));
 				leftovermissiles.addAll(e.getMissiles());
 				iter.remove();
 			}
 			else if (e.getX() > screenwidth || e.getX() + 50 < 0) {
 				iter.remove();
 			}
 			buffer.drawImage(e.getImage(), e.getX(), e.getY(), 50, 50, null);
 		}
 		repaint();
 	}
 	public void explosions() {
 		ListIterator explosions = currentexplosions.listIterator();
 		ArrayList<explosion> explosionstoadd = new ArrayList<explosion>();
 		while (explosions.hasNext()) {
 			explosion ex = (explosion)explosions.next();
 			if (ex.getSize() > ex.getMaxSize()) explosions.remove();
 			else {
 				buffer.drawImage(ex.getImage(), ex.getX(), ex.getY(), ex.getSize(), ex.getSize(), null);
 				ex.move();
 				if (ex.inRadius(subxpos, subypos)) hitpointfrac++;
 				else if (ex.inRadius(subxpos + 125, subypos)) hitpointfrac++;
 				else if (ex.inRadius(subxpos, subypos + 38)) hitpointfrac++;
 				else if (ex.inRadius(subxpos + 125, subypos + 38)) hitpointfrac++;
 				if (hitpointfrac >= 10) {
 					hitpoints--;
 					hitpointfrac = 0;
 				}
 				ListIterator it = currentenemies.listIterator();
 				while (it.hasNext()) {
 					enemy e = (enemy)it.next();
 					if (ex.inRadius(e.getX(), e.getY())) {
 						e.takePartDamage();
 						drawhealth();
 						if (e.getHealth() <= 0) {
 							explosionstoadd.add(new explosion(e.getX(), e.getY(), 50));
 							it.remove();
 						}
 					}
 					else if (ex.inRadius(e.getX() + 50, e.getY())) {
 						e.takePartDamage();
 						drawhealth();
 						if (e.getHealth() <= 0) {
 							explosionstoadd.add(new explosion(e.getX(), e.getY(), 50));
 							it.remove();
 						}
 					}
 					else if (ex.inRadius(e.getX(), e.getY() + 50)) {
 						e.takePartDamage();
 						drawhealth();
 						if (e.getHealth() <= 0) {
 							explosionstoadd.add(new explosion(e.getX(), e.getY(), 50));
 							it.remove();
 						}
 					}
 					else if (ex.inRadius(e.getX() + 50, e.getY() + 50)) {
 						e.takePartDamage();
 						drawhealth();
 						if (e.getHealth() <= 0) {
 							explosionstoadd.add(new explosion(e.getX(), e.getY(), 50));
 							it.remove();
 						}
 					}
 					//                   ListIterator mit = e.getMissiles().listIterator();
 					//                   while(mit.hasNext())
 					//                   {
 					//                      missile m = (missile)mit.next();
 					//                      if(ex.inRadius(m.getX(),m.getY()))
 					//                      {
 					//                         explosionstoadd.add(new explosion(m.getX(),m.getY(),10));
 					//                         mit.remove();
 					//                      }
 					//                      else if(ex.inRadius(m.getX()+m.getWidth(),m.getY()))
 					//                      {
 					//                         explosionstoadd.add(new explosion(m.getX(),m.getY(),10));
 					//                         mit.remove();
 					//                      }
 					//                      else if(ex.inRadius(m.getX(),m.getY()+m.getHeight()))
 					//                      {
 					//                         explosionstoadd.add(new explosion(m.getX(),m.getY(),10));
 					//                         mit.remove();
 					//                      }
 					//                      else if(ex.inRadius(m.getX()+m.getWidth(),m.getY()+m.getHeight()))
 					//                      {
 					//                         explosionstoadd.add(new explosion(m.getX(),m.getY(),10));
 					//                         mit.remove();
 					//                      }
 					//                   }
 				}
 				//                ListIterator i = currentmissiles.listIterator();
 				//                while(i.hasNext())
 				//                {
 				//                   missile m = (missile)i.next();
 				//                   if(ex.inRadius(m.getX(),m.getY()))
 				//                   {
 				//                      explosionstoadd.add(new explosion(m.getX(),m.getY(),10));
 				//                      i.remove();
 				//                   }
 				//                   else if(ex.inRadius(m.getX()+m.getWidth(),m.getY()))
 				//                   {
 				//                      explosionstoadd.add(new explosion(m.getX(),m.getY(),10));
 				//                      i.remove();
 				//                   }
 				//                   else if(ex.inRadius(m.getX(),m.getY()+m.getHeight()))
 				//                   {
 				//                      explosionstoadd.add(new explosion(m.getX(),m.getY(),10));
 				//                      i.remove();
 				//                   }
 				//                   else if(ex.inRadius(m.getX()+m.getWidth(),m.getY()+m.getHeight()))
 				//                   {
 				//                      explosionstoadd.add(new explosion(m.getX(),m.getY(),10));
 				//                      i.remove();
 				//                   }
 				//                }
 			}
 		}
 		currentexplosions.addAll(explosionstoadd);
 		// method for explosion hitting an enemy or the sub.
 		repaint();
 	}
 	public void drawhealth() {
 for (enemy e : currentenemies) {
 			int health = e.getHealth() * 50 / e.getMaxHealth();
 			buffer.setColor(Color.green);
 			buffer.fillRect(e.getX(), e.getY() + 52, health, 5);
 		}
 		repaint();
 	}
 	private class TimeListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			seconds++;
 			if (seconds % 30 == 0) count += 1;
 		}
 	}
 	private class Listener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (!music) {
 				playMusic(gamemusic);
 				music = true;
 			}
 			if (gamestate==MENU){
 				move();
 				drawBG();
 				moveleftovers();
 				movemissile();
 				moveenemies();
 				explosions();
 				drawMenu();
 			}
 			else if (hitpoints <= 0) {
 				hitpoints = 0;
 				drawBG();
 				buffer.setColor(Color.red);
 				buffer.setFont(new Font("Default", 0, 144));
 				buffer.drawString("You Lose", 300, 300);
 				drawScore();
 				time.stop();
 				t.stop();
 				repaint();
 			}
 			else {
 				move();
 				drawBG();
 				moveleftovers();
 				movemissile();
 				moveenemies();
 				explosions();
 				drawhealth();
 				drawScore();
 			}
 		}
 	}
 	public class KeyListener extends KeyAdapter {
 		public void keyPressed(KeyEvent e) {
 			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
 				keys[SPACE] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 				keys[RIGHT] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_UP) {
 				keys[UP] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 				keys[LEFT] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 				keys[DOWN] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_B) {
 				keys[B] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_R) {
 				keys[R] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_F) {
 				keys[F] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_V) {
 				keys[V] = true;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
 				System.exit(0);
 		}
 
 		public void keyReleased(KeyEvent e) {
 			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
 				keys[SPACE] = false;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 				keys[RIGHT] = false;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_UP) {
 				keys[UP] = false;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 				keys[LEFT] = false;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 				keys[DOWN] = false;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_B) {
 				keys[B] = false;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_F) {
 				keys[F] = false;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_R) {
 				keys[R] = false;
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_V) {
 				keys[V] = false;
 			}
 		}
 	}
 	public void playMusic(InputStream in) {
 		try {
 			player = new Player(in);
 			pt = new PlayerThread();
 			pt.start();
 		}
 		catch (Exception e) {
 			System.out.println("Music failed to play");
 		}
 	}
 	public void initMusic() {
 		try {
 			gamemusic = new FileInputStream("Music/Sailing Theme.mp3");
 		}
 		catch (Exception e) {
 			System.out.println(e);
 			System.out.println("Music failed to load");
 		}
 	}
 	public class PlayerThread extends Thread {
 		public void run() {
 			try {
 				player.play();
 			}
 			catch (Exception e) {
 				System.out.println("PlayerThread error");
 			}
 		}
 	}
 	public void paintComponent(Graphics g) {
 		g.drawImage(myImage, 0, 0, 1280, screenheight, null);
 	}
 	public static void main(String[] args) {
 		JFrame frame = new JFrame("Submarine game!");
 		//          GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 		//
 		//          if(gd.isFullScreenSupported()) {
 		//             gd.setFullScreenWindow(frame);
 		//          }
 		//          else {
 		//             frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 		//          }
 		//          frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 		//          frame.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
 		//          frame.setMinimumSize(Toolkit.getDefaultToolkit().getScreenSize());
 		//          frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
 		// 			frame.pack();
 		frame.setSize(screenwidth, screenheight);
 		frame.setLocationRelativeTo(null);
 		frame.setUndecorated(true);
 		frame.setResizable(false);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setContentPane(new Subs());
 		frame.setVisible(true);
 		try {
 			frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("DNE.none").getImage(), new Point(3, 3), "DNE"));
 		}
 		catch (Exception e) {}
 	}
 }
 // work on level design
 // level class with enemies, release time, etc
 // high score saving
 // text file
 // plane enemies
 // destroyer type enemies (ships on surface)
 // giant underwater bombs with chains
 // marine life (bonus??? negative???)
 // powerups???
