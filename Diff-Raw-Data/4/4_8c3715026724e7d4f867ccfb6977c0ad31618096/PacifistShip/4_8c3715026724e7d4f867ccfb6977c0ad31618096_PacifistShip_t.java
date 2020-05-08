 //import java.awt.Color;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 import info.gridworld.actor.Actor;
 //import info.gridworld.actor.Bug;
 //import info.gridworld.actor.Critter;
 //import info.gridworld.actor.Flower;
 import info.gridworld.grid.Grid;
 import info.gridworld.grid.Location;
 
 public class PacifistShip extends Actor implements KeyboardControllable
 {
 	private int lives;
 	private int bombs;
 	private int scoreMultiplier;
 	private boolean isInvincible;
 	private int invincibleCounter;
 	private ActorWorld world;
 
 	public PacifistShip(ActorWorld w)
 	{
 		setColor(null);
 		lives = 1;
 		bombs = 1;
 		scoreMultiplier = 1;
 		isInvincible = false;
 		invincibleCounter = 0;
 		world = w;
 	}
 
 	public PacifistShip(ActorWorld w, int l, int b)
 	{
 		setColor(null);
 		lives = l;
 		bombs = b;
 		scoreMultiplier = 1;
 		isInvincible = false;
 		invincibleCounter = 0;
 		world = w;
 	}
 
 	public void hud()
 	{
 		String life;
 		String bomb;
 		if(this.getBombs() > 0)
 			bomb = bombs+"";
 		else bomb = "No bombs in the wild.";
 		if(this.getLives() > 0)
 			life = lives+"";
 		else life = "Good luck. I just want you to know, we're all counting on you.";
 
 		world.setMessage("Lives: "+life+"\t\tMultiplier: "
 				+scoreMultiplier+"\nBombs: "+bomb+
 				"\t\tInvincible Counter: "+invincibleCounter+
 				"\tScore: "+world.getScore());
 	}
 
 	public void checkInvincible()
 	{
 		if(invincibleCounter == 0)
 		{
 			isInvincible = false;
 			invincibleCounter = 0;
 		}
 		else if(isInvincible)
 			invincibleCounter--;
 	}
 
 	public void act()
 	{
 		hud();
 		checkInvincible();
 		scoreMultiplier++;
 	}
 
 	public void move()
 	{
 		Grid<Actor> gr = getGrid();
 		if (gr == null)
 			return;
 		Location loc = getLocation();
 		Location next = loc.getAdjacentLocation(getDirection());
 		if(isInvincible == false && (gr.get(next) instanceof Drone
 				|| gr.get(next) instanceof CentipedeTail))
 			die();
 		else if(gr.get(next) instanceof PowerUp)
 		{
 			processPowerUp(gr.get(next));
 			moveTo(next);
 		}
 		else if(gr.get(next) instanceof Gate)
 		{
 			((Gate)(gr.get(next))).detonate(this.getDirection());
 			moveTo(next);
 		}
 		else if(gr.isValid(next))
 			moveTo(next);
 	}
 
 	public boolean canMove()
 	{
 		Grid<Actor> gr = getGrid();
 		if (gr == null)
 			return false;
 		Location loc = getLocation();
 		Location next = loc.getAdjacentLocation(getDirection());
 		if (!gr.isValid(next))
 			return false;
 		Actor neighbor = gr.get(next);
 		return (neighbor == null) || !(neighbor instanceof Laser)
 		&& !(neighbor instanceof SpaceDebris);
 	}
 
 	public void actionToPerform(String description)
 	{
 		if(description.equals("UP"))
 		{
 			setDirection(0);
 			if(canMove())
 				move();
 		}
 		else if(description.equals("DOWN"))
 		{
 			setDirection(180);
 			if(canMove())
 				move();
 		}
 		else if(description.equals("LEFT"))
 		{
 			setDirection(270);
 			if(canMove())
 				move();
 		}
 		else if(description.equals("RIGHT"))
 		{
 			setDirection(90);
 			if(canMove())
 				move();
 		}
 		else if (description.equals("SPACE"))
 			bomb();
 	}
 
 	public ArrayList<Actor> getBombTargets()
 	{
 		Grid<Actor> gr = getGrid();
 		ArrayList<Actor> targets = new ArrayList<Actor>();
 		Location loc = this.getLocation();
 
 		for(int r = loc.getRow()-2; r <= loc.getRow()+2; r++)
 		{
 			for(int c = loc.getCol()-2; c <= loc.getCol()+2; c++)
 			{
 				Location check = new Location(r,c);
 				if(gr.isValid(check) && (gr.get(check) instanceof Drone)/* &&
 					!(gr.get(check) instanceof PacifistShip)*/)
 					targets.add(gr.get(check));
 			}
 		}
 		return targets;
 	}
 
 	public void bomb()
 	{
 		if(bombs > 0)
 		{
 			ArrayList<Actor> targets = getBombTargets();
 			for(Actor a : targets)
 				((Drone)(a)).die();
 			bombs--;
 		}
 	}
 
 	public void die()
 	{
 		Grid<Actor> gr = getGrid();
 		ArrayList<Actor> targets = new ArrayList<Actor>();
 		Location loc = this.getLocation();
 		for(int r = loc.getRow()-2; r <= loc.getRow()+2; r++)
 		{
 			for(int c = loc.getCol()-2; c <= loc.getCol()+2; c++)
 			{
 				Location check = new Location(r,c);
 				if(gr.isValid(check) && (gr.get(check) instanceof Actor) &&
 						(gr.get(check) instanceof Drone))
 					targets.add(gr.get(check));
 			}
 		}
 		for(Actor a : targets)
 			a.removeSelfFromGrid();
 		deathMessage();
 	}
 
 	public void deathMessage()
 	{
 		if(lives > 0)
 		{
 			if(lives - 1 == 1)
 				JOptionPane.showMessageDialog(null, "You died!\n"+(lives-1)+" life left.");
 			else JOptionPane.showMessageDialog(null, "You died!\n"+(lives-1)+" lives left.");
 			this.setLives(lives-1);
 			scoreMultiplier = 1;
 		}
 		else
 		{
			world.getFrame().dispose();
 			JOptionPane.showMessageDialog(null, "GAME OVER");
 			leaderboard();
 		}
 	}
 
 	public ArrayList<LeaderboardEntry> retrieveLB()
 	{
 		ArrayList<LeaderboardEntry> leaderboard = new ArrayList<LeaderboardEntry>();
 
 		try{
 			Scanner inFile = new Scanner(new File("leaderboard.txt"));
 
 			String line;
 			String[] data;
 
 			while (inFile.hasNext())
 			{
 				LeaderboardEntry one = new LeaderboardEntry();
 
 				line = inFile.nextLine();
 				data = line.split(" ");
 				one.setInitials(data[0]);
 				one.setScore(Integer.parseInt(data[1]));
 
 				leaderboard.add(one);
 			}
 			inFile.close();
 		}catch (Exception e){
 			System.out.println("error reading file: "+e);
 		}
 		return leaderboard;
 	}
 
 	public ArrayList<LeaderboardEntry> addEntry(ArrayList<LeaderboardEntry> leaderboard)
 	{
 		LeaderboardEntry entry = new LeaderboardEntry();
 		String initials = "";
 		while(initials.length() != 3)
 			initials = JOptionPane.showInputDialog(null, "Enter your initials:");
 		entry.setInitials(initials.toUpperCase());
 		entry.setScore(world.getScore());
 		leaderboard.add(entry);
 		return leaderboard;
 	}
 
 	public void leaderboard()
 	{
 		ArrayList<LeaderboardEntry> leaderboard = addEntry(retrieveLB());
 
 		try{
 			PrintWriter outFile = new PrintWriter(new FileOutputStream("leaderboard.txt"));
 			for(int i = 0; i < leaderboard.size(); i++)
 				outFile.println(leaderboard.get(i).getInitials()+" "+leaderboard.get(i).getScore());
 			outFile.close();
 		}catch(Exception e){
 			System.out.println("error writing to file "+e);
 		}
 		GameRunner.print(leaderboard);
 	}
 
 	public void processPowerUp(Actor a)
 	{
 		if(a instanceof ExtraLife)
 			lives++;
 		else if(a instanceof ExtraBomb)
 			bombs++;
 		else if(a instanceof Invincibility)
 		{
 			isInvincible = true;
 			invincibleCounter = 10;
 		}
 		a.removeSelfFromGrid();
 	}
 
 	private void setLives(int l)
 	{
 		if(l >= 0)
 			lives = l;
 		else System.out.println("Invalid lives.");
 	}
 
 	public int getLives()
 	{
 		return lives;
 	}
 
 	public int getBombs()
 	{
 		return bombs;
 	}
 
 	public int getScoreMultiplier()
 	{
 		return scoreMultiplier;
 	}
 
 	public boolean getInvinc()
 	{
 		return isInvincible;
 	}
 }
