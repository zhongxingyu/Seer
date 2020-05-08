 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import sun.audio.*;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 
 public class Board extends JPanel implements Runnable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5438534791450927749L;
 
 	// Global Variables
 
 	private final int DELAY = 50;
 
 	// Thread for the Board Class to run in separately from everything else
 	private Thread animator;
 
 	static Tower pendingTower;
 
 	Util.TrashType[] types = { Util.TrashType.paper, Util.TrashType.plastic };
 	private int TRASH_SPEED = 2;
 
 	// Images
 	private Image background;
 	private Image landFill;
 	private Image outline;
 
 	// Fonts
 	private Font bigfont = new Font("Georgia", Font.BOLD, 30);
 	private Font smallfont = new Font("Georgia", Font.BOLD, 20);
 
 	// Audio Player
 	AudioInputStream as;
 	static Boolean muted = false;
 	static Boolean effectMute = false;
 	private Clip clip;
 
 
 	/* Game State Variables */
 
 	// Game State Booleans
 	public static boolean inBetweenLevels = true;
 	private boolean ingame = true;
 	private boolean paintLevel =false;
 
 	// Game State Variables
 	private Integer budget = 200;
 	Double airQual = 1000.0;
 	private Integer level = 1;
 	private Integer landFillScore = 0;
 	private Integer escapedTrash =0;
 	private Integer moneyEarned = 0;
 	private Integer bonus = 0;
 
 
 	// Game State Lists
 	static ArrayList<ImageIcon> messages=new ArrayList<ImageIcon>();
 	ArrayList<Trash> trash = new ArrayList<Trash>();
 	ArrayList<Tower> towers = new ArrayList<Tower>(); // not sure if this should be static but am trying to add towers on button press
 	ArrayList<Integer> pathX = new ArrayList<Integer>();
 	ArrayList<Integer> pathY = new ArrayList<Integer>();
 	//Wavegen
 	WaveGen Wave = new WaveGen(this);
 	public Board() {
 
 		setDoubleBuffered(true);
 
 		makeBoard(pathX, pathY);
 
 		// My computer wants Board capitalized. -JJ
 		ImageIcon ii = new ImageIcon(this.getClass().getResource(
 		"pics/Board.png"));
 		background = ii.getImage();
 
 		ii = new ImageIcon(this.getClass().getResource("pics/landfill.png"));
 		landFill = ii.getImage();
 
 		ii = new ImageIcon(this.getClass().getResource(
 		"pics/outline.png"));
 		outline = ii.getImage();
 
 
 
 	}
 
 	public void addNotify() {
 		super.addNotify();
 		animator = new Thread(this);
 		animator.start();
 	}
 
 	public void makeBoard(ArrayList<Integer> pathX, ArrayList<Integer> pathY) {
 
 		pathX.add(0);
 		pathY.add(Util.pathPad);
 
 		pathX.add(Util.boardWidth - Util.pathPad - Util.pathWidth);
 		pathY.add(Util.pathPad);
 
 		pathX.add(Util.boardWidth - Util.pathPad - Util.pathWidth);
 		pathY.add(Util.pathPad + Util.pathHeight + Util.gapPad);
 
 		pathX.add(Util.pathPad);
 		pathY.add(Util.pathPad + Util.pathHeight + Util.gapPad);
 
 		pathX.add(Util.pathPad);
 		pathY.add(Util.pathPad + Util.pathHeight * 2 + Util.gapPad * 2);
 
 		pathX.add(Util.boardWidth - Util.pathPad - Util.pathWidth);
 		pathY.add(Util.pathPad + Util.pathHeight * 2 + Util.gapPad * 2);
 
 		pathX.add(Util.boardWidth - Util.pathPad - Util.pathWidth);
 		pathY.add(Util.boardHeight + Util.pathHeight);
 
 		pathX.add(-Util.pathWidth);
 		pathY.add(Util.boardHeight + Util.pathHeight);
 
 		pathX.add(-Util.pathWidth);
 		pathY.add(Util.pathPad);
 
 		String path = System.getProperty("user.dir");
 		path += "/Resources/audio/Menu.wav";
 		try {
 			InputStream in = new FileInputStream(path);
 			as = AudioSystem.getAudioInputStream(in);
 			clip = AudioSystem.getClip();
 			clip.open(as);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedAudioFileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void paint(Graphics g) {
 		super.paint(g);
 		Graphics2D g2d = (Graphics2D) g;
 		g2d.drawImage(background, 0, 0, this);
 
 		if (ingame) {
 
 			g2d.drawImage(landFill, 0, 0, this);
 			// Draw each piece of trash in our trash array onto the frame
 			for (int i = 0; i < trash.size(); i++) {
 				Trash curTrash = trash.get(i);
 				g2d.drawImage(curTrash.getImage(), (int) curTrash.getX(),
 						(int) curTrash.getY(), this);
 			}
 
 			for (int i = 0; i < towers.size(); i++) {
 				Tower curTower = towers.get(i);
 				g2d.drawImage(curTower.getArmImage(), (int) curTower.getArmX(),
 						(int) curTower.getArmY(), this);
 				g2d.drawImage(curTower.getBaseImage(), (int) curTower.getX(),
 						(int) curTower.getY(), this);
 				if(curTower.isHighLight()){
 
 					g2d.drawImage(outline,(int) curTower.getX()-10,
 							(int) curTower.getY()-10, this);
 				}
 
 			}
 
 			// JJ
 			if (pendingTower != null
 					&& (pendingTower.getX() != Integer.MIN_VALUE || pendingTower
 							.getY() != Integer.MIN_VALUE)) {
 				g2d.drawImage(pendingTower.getBaseImage(),
 						(int) pendingTower.getX(), (int) pendingTower.getY(),
 						this);
 			}
 
 
 
 			if(inBetweenLevels)
 			{
 				if(messages.size()!=0){
 					g2d.drawImage(messages.get(0).getImage(),135,135
 							,this);
 				}
 				g2d.setFont(bigfont);
 				g2d.setColor(Color.WHITE);
 				g2d.drawString("Wave "+level, 300, 550);
 				
 				if(level>1){
 					g2d.setFont(smallfont);
 					g2d.setColor(Color.WHITE);
					g2d.drawString("Last Wave - Money Earned: $"+moneyEarned+"  Bonus: $"+ bonus, 100, 580);
 				}
 			}
 
 		} else {
 			g2d.setFont(bigfont);
 			g2d.drawString("GAME OVER", 250, 300);
 
 		}
 		
 		Toolkit.getDefaultToolkit().sync();
 		g.dispose();
 
 
 	}
 
 	public static void startWave() {
 		inBetweenLevels = false;
 	}
 
 	public void removeMoney(int cost) {
 
 		budget -= cost;
 	}
 
 	public Integer getBudget() {
 		return budget;
 	}
 	public void setBudget(int aBudget){
 		budget = aBudget;
 	}
 
 	public void startMusic() {
 		clip.loop(Clip.LOOP_CONTINUOUSLY);
 		muted = false;
 	}
 
 	public boolean isMuted() {
 		return muted;
 	}
 
 	public void stopMusic() {
 		clip.stop();
 		muted = true;
 	}
 
 	public void run() {
 
 		long beforeTime, timeDiff, sleep;
 
 		beforeTime = System.currentTimeMillis();
 
 		int counter = 0;
 
 		startMusic();
 		
 		int oldBudget = budget;
 
 		ingame = true;
 
 		while (true) {
 			
 			trash = Wave.getWave(level);
 			messages=Wave.getMessages(level);
 			
 			while (trash.size() > 0) {
 				
 				counter++;
 				long pause = 0;
 				
 				while (inBetweenLevels) {
 					repaint();
 				}
 
 				for (int i = 0; i < trash.size(); i++) {
 
 					trash.get(i).followPath(pathX, pathY);
 
 					for (int j = 0; j < towers.size(); j++) {
 						// Windmills don't need to check for collisions
 						if (towers.get(j).getType() == Util.TowerType.windmill) {
 							break;
 						}
 						//towers.get(j).setFiring(true);
 						//System.out.println(!trash.get(i).isKilled() +" "+ !towers.get(j).getFiring() +" "+trash.get(i).detectCollisions(towers.get(j),pathX, pathY));
 						if (!trash.get(i).isKilled() && !towers.get(j).getFiring() && trash.get(i).detectCollisions(towers.get(j),pathX, pathY)) {
 							//System.out.println("SHIT");
 							calculateScore(towers.get(j),trash.get(i));
 							towers.get(j).setFiring(true,trash.get(i));
 							trash.get(i).setKilled();
 						}
 
 
 
 					}
 
 					if(trash.get(i).getImage()==null){
 						trash.remove(i);
 					}
 					// Check to make sure trash hasn't exited the board
 					else if (trash.get(i).getY() + 30 > Util.boardHeight) {
 						trash.remove(i);
 						landFillScore += 1;
 						escapedTrash +=1;
 						System.out.println(landFillScore);
 					}
 
 				}
 
 				for(int i=0; i<towers.size();i++){
 					if (towers.get(i).getFiring()){
 						towers.get(i).fire();
 					}
 				}
 
 				repaint();
 
 				timeDiff = System.currentTimeMillis() - beforeTime;
 				sleep = DELAY - timeDiff;
 
 				if (sleep < 0)
 					sleep = 2;
 				try {
 					Thread.sleep(sleep);
 				} catch (InterruptedException e) {
 					System.out.println("interrupted");
 				}
 
 				beforeTime = System.currentTimeMillis();
 			}
 			
			moneyEarned=budget-oldBudget;
 
 			// Wave has now ended
 			resetTowers();
 
 			// Check for windmills
 			for (int g = 0; g < towers.size(); g++) {
 				if (towers.get(g).getType() == Util.TowerType.windmill) {
 					budget += 150;
 				}
 			}
 
 			repaint();
 			level++;
 			calculateBonus();
 
 			inBetweenLevels = true;
 
 			if (airQual <= 0 || landFillScore>=100) {
 				break;
 			}
 
 		}
 
 		ingame = false;
 
 	}
 	private void calculateBonus(){
 		double multiplier = 1;
 		multiplier = (airQual/1000) * multiplier;
 		multiplier = (level/4) + multiplier; 
 		multiplier = multiplier - (escapedTrash/100);
 		bonus = (int)(multiplier*150);
 		budget += (int)(multiplier*150);
 
 	}
 
 	private void calculateScore(Tower tower, Trash trash){
 
 		switch(tower.type){
 			case incenerator: 
 				budget+=15; airQual-=15; break;
 			case recycle: budget +=25; airQual+=10;
 		}
 		
 
 
 	}
 
 	private void resetTowers() {
 		for (int i = 0; i < towers.size(); i++) {
 			towers.get(i).resetTower();
 		}
 	}
 
 	public Tower onTowerReturn(int x, int y){
 		for(int i=0; i<towers.size(); i++){
 			int tX=towers.get(i).getX();
 			int tY=towers.get(i).getY();
 			int tW=towers.get(i).getWidth();
 			int tH=towers.get(i).getHeight();
 
 			if(tX<=x && tX+tW>=x && tY<=y && tY+tH>=y){
 				return towers.get(i);
 			}
 		}
 		return null;
 	}
 
 	public boolean onTower(int x, int y){
 		for(int i=0; i<towers.size(); i++){
 			int tX=towers.get(i).getX();
 			int tY=towers.get(i).getY();
 			int tW=towers.get(i).getWidth();
 			int tH=towers.get(i).getHeight();
 
 			if(tX<=x && tX+tW>=x && tY<=y && tY+tH>=y){
 				return true;
 			}
 		}
 		return false;
 	}
 	// Method to determine if the user is adding the tower on top of the path
 	public boolean inPath(int x, int y) {
 
 		// Inside actual game board
 		if ((x > 0 && x > Util.boardWidth) || (y > 0 && y > Util.boardHeight)) {
 			return true;
 		}
 
 		// First row
 		if ((x > 0 && x < Util.boardWidth - Util.pathPad
 				+ (Util.towerWidth / 2))
 				&& (y > Util.pathPad - (Util.towerWidth / 2) && y < Util.pathPad
 						+ Util.pathWidth + (Util.towerWidth / 2))) {
 			return true;
 			// First down
 		} else if ((x < Util.boardWidth - Util.pathPad + (Util.towerWidth / 2) && x > (Util.boardWidth
 				- Util.pathPad - Util.pathWidth - (Util.towerWidth / 2)))
 				&& (y > Util.pathPad - (Util.towerWidth / 2) && (y < Util.pathPad
 						+ Util.gapPad + (Util.towerWidth / 2)))) {
 			return true;
 			// First Switch Back
 		} else if (x < Util.boardWidth - Util.pathPad + (Util.towerWidth / 2)
 				&& x > Util.pathPad - (Util.towerWidth / 2)
 				&& y > Util.pathPad + Util.pathWidth + Util.gapPad
 				- (Util.towerWidth / 2)
 				&& y < Util.pathPad + (2 * Util.pathWidth) + Util.gapPad
 				+ (Util.towerWidth / 2)) {
 			return true;
 			// Second Down
 		} else if (x > Util.pathPad - (Util.towerWidth / 2)
 				&& x < Util.pathPad + Util.pathWidth + (Util.towerWidth / 2)
 				&& y > Util.pathPad + Util.pathWidth + Util.gapPad
 				- (Util.towerWidth / 2)
 				&& y < Util.pathPad + 2 * Util.pathWidth + 2 * Util.gapPad
 				+ (Util.towerWidth / 2)) {
 			return true;
 			// Second Switch Back
 		} else if (x > Util.pathPad - (Util.towerWidth / 2)
 				&& x < Util.boardWidth - Util.pathPad + (Util.towerWidth / 2)
 				&& y > Util.pathPad + 2 * Util.pathWidth + 2 * Util.gapPad
 				- (Util.towerWidth / 2)
 				&& y < Util.pathPad + (3 * Util.pathWidth) + 2 * Util.gapPad
 				+ (Util.towerWidth / 2)) {
 			return true;
 			// Third Down
 		} else if ((x < Util.boardWidth - Util.pathPad + (Util.towerWidth / 2) && x > (Util.boardWidth
 				- Util.pathPad - Util.pathWidth - (Util.towerWidth / 2)))
 				&& y > Util.pathPad + (3 * Util.pathWidth) + 2 * Util.gapPad
 				+ (Util.towerWidth / 2)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	// JJ
 	public Tower addTower(Tower t) {
 		towers.add(t);
 		return t;
 	}
 
 	public static void addPendingTower(Tower t) {
 		pendingTower = t;
 	}
 
 	public static void removePendingTower() {
 		pendingTower = null;
 	}
 
 	public Integer getLevel() {
 		return level;
 	}
 
 	public void sellTower(Tower t, int cost){
 		budget+=cost;
 		towers.remove(t);
 	}
 
 	public void upgradeTower(Tower t, int cost){
 		budget-=cost;
 		t.upgrade();
 	}
 
 	public Integer getLandFillScore() {
 		return landFillScore;
 	}
 
 }
