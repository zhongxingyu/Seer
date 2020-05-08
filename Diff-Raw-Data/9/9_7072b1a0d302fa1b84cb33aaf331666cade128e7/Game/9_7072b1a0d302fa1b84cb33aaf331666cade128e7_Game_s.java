 /**
  * Main game
  * 
  * @author	Jamie of the Javateerz
  */
 
 import java.awt.*;
 import java.awt.image.BufferStrategy;
 import java.io.*;
 import java.util.ArrayList;
 
 import javax.sound.sampled.Clip;
 
 public class Game implements Serializable
 {
 	public static final int SPEED_THROTTLE		= 10;
 	
 	// number of pixels to move by
 	public static final int MOVE_SPEED			= 1;
 	
 	// delay (in number of frames) before another attack can be used
 	public static final int ATTACK_DELAY		= 20;
 	private int attackDelay						= 0;
 	public static final int TP_REGEN			= 5;
 	public static int tpRegen					= 0;
 	public static final int CHARGE_REGEN		= 200;
 	public static int chargeRegen				= 0;
 	
 	public static final int MIN_STUDENTS		= 20;
 	public static final int MAX_STUDENTS		= 30;
 	
 	public static final int MAX_STUDENT_SPEED	= 1;
 	public static final double GENDER_CHANCE	= 0.5;
 	
 	public static final double COUPLE_CHARGE_CHANCE	= 0.1;
 	public static final int COUPLE_CHARGE_AMOUNT	= 1;
 	
 	public static final double CUPPLE_CHANCE	= 0.6;
 	public static final double ATTACK_CHANCE	= 0.1;
 	
 	public static final int MIN_NUM_MOVES		= 10;
 	public static final int MAX_NUM_MOVES		= 400;
 	
 	public static final int DECOUPLE_SPACING	= 80;
 	public static final int COUPLE_CHANCE_MULTIPLIER = 400;
 	
 	public static final int STAT_PAD_TOP		= 10;
 	public static final int STAT_PAD_BOTTOM		= STAT_PAD_TOP;
 	public static final int STAT_PAD_LEFT_BAR	= STAT_PAD_TOP * 3 + 3;
 	public static final int BAR_HEIGHT			= 10;
 	public static final int BAR_SPACING			= 5;
 	public static final int BAR_MULTIPLIER		= 2;
 	public static final int STATS_BAR_HEIGHT	= STAT_PAD_TOP + BAR_HEIGHT * 2 +
 													BAR_SPACING + STAT_PAD_BOTTOM;
 	
 	public static final int PLAYER_X			= 10;
 	public static final int PLAYER_Y			= STATS_BAR_HEIGHT + 10;
 	public static final int PLAYER_HP			= 50;
 	public static final int PLAYER_TP			= 300;
 	public static double hpPercent;
 	public static double tpPercent;
 	
 	private static final long serialVersionUID	= 5L;
 	
 	private transient ParkViewProtector driver;
 	private transient Graphics g;
 	private transient BufferStrategy strategy;
 	
 	// objects on the screen
 	private Staff player;
 	private ArrayList<Student> students			= new ArrayList<Student>();
 	private ArrayList<Cupple> couples			= new ArrayList<Cupple>();
 	private ArrayList<Attack> attacks			= new ArrayList<Attack>();
 	private ArrayList<Wall> walls				= new ArrayList<Wall>();
 
 	/**
 	 * Constructor
 	 * 
 	 * @param w Width of the game canvas
 	 * @param h Height of the game canvas
 	 * @param g Graphics canvas
 	 * @param strategy Buffer strategy
 	 */
 	public Game(ParkViewProtector p)
 	{
 		init(p);
 		
 		// initialize everything
 		initPlayer();
 		initWalls();
 		initStudents();
 	}
 	
 	public void init(ParkViewProtector p)
 	{
 		this.driver							= p;
 		this.g								= p.getGraphics();
 		this.strategy						= p.getBufferStrategy();
 	}
 	
 	/**
 	 * Create and initialize player (the staff member we're playing as)
 	 */
 	public void initPlayer()
 	{
 		player						= new SpecialCharacter(PLAYER_X, PLAYER_Y, PLAYER_HP, PLAYER_TP);
 	}
 	
 	/**
 	 * Create some walls
 	 */
 	public void initWalls()
 	{
 		walls.add(new Wall(100, 0, ParkViewProtector.WIDTH - 60, 4));
 		walls.add(new Wall(100, 3, 4, 300));
 	}
 	
 	/**
 	 * Create and initialize students
 	 */
 	public void initStudents()
 	{
 		// create a random number of students using MIN_STUDENTS and MAX_STUDENTS; multiply
 		// it by 2 and divide to ensure that an even number is created to ensure proper
 		// coupling
 		int numStudents				= (int) (Math.random() * (MAX_STUDENTS - MIN_STUDENTS + 1)) + MIN_STUDENTS;
 		numStudents					= Math.round(numStudents * 2 / 2);
 		
 		Student student				= null;
 		
 		int x, y;
 		double speed;
 		char gender;
 		
 		for(int i = 0; i < numStudents; i++)
 		{
 			x						= (int) (Math.random() * ParkViewProtector.WIDTH) + 1;
 			y						= (int) (Math.random() * ParkViewProtector.HEIGHT) + 1;
 			speed					= Math.random() * MAX_STUDENT_SPEED + 1;
 			gender					= (Math.random() <= GENDER_CHANCE) ? 'm' : 'f';
 			
 			student					= new Student(x, y, 5, 5, speed, gender);
 			
 			// make sure that the student is not spawned on top of a wall)
 			while(!canMove(student.getBounds()))
 			{
 				x					= (int) (Math.random() * ParkViewProtector.WIDTH) + 1;
 				y					= (int) (Math.random() * ParkViewProtector.HEIGHT) + 1;
 				
 				student.moveTo(x, y);
 			}
 			
 			students.add(student);
 		}
 	}
 	
 	public void show()
 	{
 		Student currStudent; 
 		Cupple currCouple;
 		Attack currAttack;
 		
 		g						= (Graphics) strategy.getDrawGraphics();
 		
 		// draw the background
 		g.setColor(ParkViewProtector.COLOR_BG_2);
 		g.fillRect(0, 0, ParkViewProtector.WIDTH, ParkViewProtector.HEIGHT);
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw students
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < students.size(); i++)
 		{
 			currStudent			= students.get(i);
 			currStudent.draw(g);
 			chargeRegen+=1;
 			if(chargeRegen>=CHARGE_REGEN)
 			{
 				chargeRegen=0;
 				currStudent.adjustCharge(1);
 			}
 			
 			currStudent.step(this);
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw couples
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < couples.size(); i++)
 		{
 			currCouple			= couples.get(i);
 			currCouple.draw(g);
 			
 			currCouple.step(this);
 			
 			// did the couple hit the player? if so, decrease HP
 			if(currCouple.getBounds().intersects(player.getBounds()) &&
 					Math.random() <= ATTACK_CHANCE && !currCouple.getStunned())
 			{
 				if(player.getHp() <= 0)
 				{
 					gameOver();
 				}
 				else {
 					player.adjustHp(1);
 				}
 			}
 			
 			if(ParkViewProtector.shiftPressed)
 			{
 				showCharges();
 			}
 			
 			// update students
 			/*for(int j = 0; j < students.size(); j++)
 			{
 				// if we hit a student that isn't infected
 				if(currCouple.getBounds().intersects(students.get(j).getBounds())
 						&& !students.get(j).isInfected()
 						&& Math.random() <= INFECT_CHANCE)
 				{
 					students.get(j).infect();
 					System.out.println("student #" + j + " infected by couple #" + i);
 					break;
 				}
 			}*/
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw attacks
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < attacks.size(); i++)
 		{
 			currAttack			= attacks.get(i);
 			currAttack.draw(g);
 			
 			currAttack.move(MOVE_SPEED);
 			
 			// is the attack off the screen?
 			if(currAttack.getBounds().x < -currAttack.getBounds().width ||
 					currAttack.getBounds().x > ParkViewProtector.WIDTH ||
 					currAttack.getBounds().y < -currAttack.getBounds().height ||
 					currAttack.getBounds().y > ParkViewProtector.HEIGHT)
 			{
 				System.out.println("Attack #" + i +" went off screen, removing");
 				
 				attacks.remove(i);
 				i--;
 			}
 			
 			if(i >= 0 && currAttack.over())
 			{
 				attacks.remove(i);
 				i--;
 			}
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw walls
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		for(Wall w : walls)
 		{
 			w.draw(g);
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw player
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		player.draw(g);
 		player.decrementHitDelay(1);
 		tpRegen+=1;
 		if(tpRegen>=TP_REGEN)
 		{
 			if(player.getTp()<player.getMaxTp())
 				player.adjustTp(player.getMaxTp()/100);
 			tpRegen=0;
 		}
 		
 		hpPercent=(double)player.getHp()/(double)player.getMaxHp();
 		tpPercent=(double)player.getTp()/(double)player.getMaxTp();
 		if(ParkViewProtector.onePressed && !(player instanceof Stark))
 		{
 			player=new Stark((int) player.getBounds().getX(), (int) player.getBounds().getY(), (int) ((double)Stats.STARK_HP*hpPercent), (int)((double)Stats.STARK_TP*tpPercent));
 		}
 		
 		if(ParkViewProtector.twoPressed && !(player instanceof SpecialCharacter))
 		{
 			player=new SpecialCharacter((int) player.getBounds().getX(), (int) player.getBounds().getY(), (int) ((double)Stats.SPECIAL_HP*hpPercent), (int) ((double)Stats.SPECIAL_TP*tpPercent));
 		}
 		
 		for(int j = 0; j < attacks.size(); j++)
 		{
 			currAttack		= attacks.get(j);
 			
 			if(currAttack.getBounds().intersects(player.getBounds()) && player.isHittable() && !currAttack.isStudent())
 			{
 				player.adjustHp(currAttack.getDamage());
 				if(player.getHp()>player.getMaxHp())
 					player.setHp(player.getMaxHp());
 				player.setHitDelay(currAttack.getHitDelay());
 				
 				if(!currAttack.isAoE())
 				{
 					attacks.remove(j);
 				}
 				
 				if(currAttack.getStatus()==Status.STUN)
 				{
 					player.stun(currAttack.getStatusDuration());
 				}
 			}
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw statistics
 		////////////////////////////////////////////////////////////////////////////////////
 		// these are painted last to ensure that they are always on top
 
 		// background rectangle
 		g.setColor(ParkViewProtector.STATS_BAR_BG);
 		g.fillRect(0, 0, ParkViewProtector.WIDTH, STATS_BAR_HEIGHT);
 		
 		// draw labels
 		g.setColor(ParkViewProtector.STATS_BAR_FG);
 		g.setFont(new Font("System", Font.PLAIN, 10));
 		
 		int textCenter				= BAR_HEIGHT / 4 + g.getFontMetrics().getHeight() / 2;
 		
 		g.drawString("HP:", STAT_PAD_TOP, STAT_PAD_TOP + textCenter);
 		g.drawString("TP:", STAT_PAD_TOP, STAT_PAD_TOP + BAR_HEIGHT + BAR_SPACING + textCenter);
 		g.drawString("Speed: " + player.getSpeed(), 400, STAT_PAD_TOP + BAR_HEIGHT);
 		
 		// draw HP bar
 		int hpMaxWidth				= player.getMaxHp() * BAR_MULTIPLIER;
 		int hpBarWidth				= (int) (((double) player.getHp() / player.getMaxHp())
 											* hpMaxWidth);
 		
 		// background
 		g.setColor(ParkViewProtector.STATS_BAR_HP.darker().darker());
 		g.fillRect(STAT_PAD_LEFT_BAR, STAT_PAD_TOP, hpMaxWidth, BAR_HEIGHT);
 		
 		// main bar
 		g.setColor(ParkViewProtector.STATS_BAR_HP);
 		g.fillRect(STAT_PAD_LEFT_BAR, STAT_PAD_TOP, hpBarWidth, BAR_HEIGHT);
 		
 		// draw TP bar
 		int tpMaxWidth				= player.getMaxTp() * BAR_MULTIPLIER;
 		int tpBarWidth				= (int) (((double) player.getTp() / player.getMaxTp())
 											* tpMaxWidth);
 		
 		// background
 		g.setColor(ParkViewProtector.STATS_BAR_TP.darker().darker());
 		g.fillRect(STAT_PAD_LEFT_BAR, STAT_PAD_TOP + BAR_HEIGHT + BAR_SPACING, tpMaxWidth,
 				BAR_HEIGHT);
 		
 		// main bar
 		g.setColor(ParkViewProtector.STATS_BAR_TP);
 		g.fillRect(STAT_PAD_LEFT_BAR, STAT_PAD_TOP + BAR_HEIGHT + BAR_SPACING, tpBarWidth,
 				BAR_HEIGHT);
 		
 		// finish drawing
 		g.dispose();
 		strategy.show();
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Move the player
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		int distX = 0, distY = 0;
 		
 		if(ParkViewProtector.upPressed && !ParkViewProtector.downPressed
 				&& player.getBounds().y > 0)
 		{
 			distY						= -MOVE_SPEED;
 		}
 		
 		if(ParkViewProtector.downPressed && !ParkViewProtector.upPressed
 				&& player.getBounds().y < ParkViewProtector.HEIGHT - player.getBounds().height)
 		{
 			distY						= MOVE_SPEED;
 		}
 		
 		if(ParkViewProtector.leftPressed && !ParkViewProtector.rightPressed
 				&& player.getBounds().x > 0)
 		{
 			distX						= -MOVE_SPEED;
 		}
 		
 		if(ParkViewProtector.rightPressed && !ParkViewProtector.leftPressed
 				&& player.getBounds().x < ParkViewProtector.WIDTH - player.getBounds().width)
 		{
 			distX						= MOVE_SPEED;
 		}
 		
 		// if we are moving, check to see if location is clear, then move
 		if(distX != 0 || distY != 0)
 		{
 			if(canMove(player.getNewBounds(distX, distY)))
 			{
 				player.move(distX, distY);
 			}
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Create attacks
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		if(ParkViewProtector.attackPressed&&attackDelay == 0)
 		{
 			Attack testAttack;
 			int attackKey=0;
 			if(ParkViewProtector.zPressed)
 			{
 				attackKey=0;
 			}
 			else if(ParkViewProtector.xPressed)
 			{
 				attackKey=1;
 			}
 			else if(ParkViewProtector.cPressed)
 			{
 				attackKey=2;
 			}
 
 			testAttack			= player.getAttack(attackKey);
 			if(player.getTp()>testAttack.getTp())
 			{
 				player.stun(testAttack.getStillTime());
 				player.adjustTp(-testAttack.getTp());
 				testAttack.switchXY();
 				attacks.add(testAttack);
 				
 				try
 				{
 					Clip soundClip		= DataStore.INSTANCE.getAudioClip(testAttack.getName()+".wav");
 					soundClip.start();
 				}
 				catch(Exception e)
 				{
 					System.out.println("The attack has no sound.");
 				}
 				
 				// set delay
 				attackDelay			= testAttack.getReuse();
 			}
 			
 		}
 		// decrease delay if there is one
 		if(attackDelay > 0)
 			attackDelay--;
 		
 		// keep the game from running too fast
 		try
 		{
 			Thread.sleep(SPEED_THROTTLE);
 		}
 		catch(Exception e) {}
 	}
 	
 	/**
 	 * Random movement
 	 * 
 	 * @param Movable Object to move
 	 */
 	public void moveRandom(Movable obj)
 	{
 		int speed					= MOVE_SPEED;
 		int changeMoves				= (int) (Math.random() * (MAX_NUM_MOVES - MIN_NUM_MOVES) +
 				MIN_NUM_MOVES + 1);
 		
 		// change direction if the move count exceeds the number of moves to change after
 		if(obj.getMoveCount() <= 0 || obj.getMoveCount() > changeMoves)
 		{
 			// choose a new direction
 			obj.setDirection((int) (Math.random() * 4));
 			obj.resetMoveCount();
 		}
 		
 		// change direction if we hit the top or bottom
 		if(obj.getBounds().y <= STATS_BAR_HEIGHT && obj.getDirection() == Direction.NORTH)
 		{
 			obj.setDirection(Direction.SOUTH);
 			obj.resetMoveCount();
 		}
 		else if(obj.getBounds().y >= ParkViewProtector.HEIGHT - obj.getBounds().height  &&
 				obj.getDirection() == Direction.SOUTH)
 		{
 			obj.setDirection(Direction.NORTH);
 			obj.resetMoveCount();
 		}
 		else if(obj.getBounds().x <= 0 && obj.getDirection() == Direction.WEST)
 		{
 			obj.setDirection(Direction.EAST);
 			obj.resetMoveCount();
 		}
 		else if(obj.getBounds().x >= ParkViewProtector.WIDTH - obj.getBounds().width &&
 				obj.getDirection() == Direction.EAST)
 		{
 			obj.setDirection(Direction.WEST);
 			obj.resetMoveCount();
 		}
 		
 		// check for collisions
 		if(obj.getNewBounds(speed).intersects(player.getBounds())
 				|| !canMove(obj.getNewBounds(speed)))
 		{
 			// collision, must choose new direction
 			obj.resetMoveCount();
 		}
 		else {
 			obj.move(speed);
 		}
 	}
 	
 	/**
 	 * Loop through students, couples, and walls to see if the specified rectangle is
 	 * available
 	 * 
 	 * @return
 	 */
 	public boolean canMove(Rectangle newRect)
 	{
 		// students
 		/*for(Student s : students)
 		{
 			if(!s.getStunned() && newRect.intersects(s.getBounds()))
 			{
 				return false;
 			}
 		}*/
 		
 		// couples
 		/*for(Cupple c : couples)
 		{
 			if(newRect.intersects(c.getBounds()))
 			{
 				return false;
 			}
 		}*/
 		
 		// walls
 		for(Wall w : walls)
 		{
 			if(newRect.intersects(w.getBounds()))
 			{
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Attempt to couple with another student
 	 * 
 	 * @param currStudent
 	 * @return Whether or not the coupling succeeded
 	 */
 	public boolean attemptCoupling(Student currStudent)
 	{
 		int charge, student1, student2;
 		Student testStudent;
 		
 		int i						= students.indexOf(currStudent);
 		
 		for(int j = 0; j < students.size(); j++)
 		{
 			testStudent				= students.get(j);
 			
 			// don't do anything if it's us
 			if(i == j)
 			{
 				continue;
 			}
 			
 			// no same-sex couples (for now at least)
 			if(currStudent.getGender() == testStudent.getGender())
 			{
 				continue;
 			}
 
 			// did we hit another student with a charge?
 			if(currStudent.getBounds().intersects(students.get(j).getBounds())
 					&& testStudent.getCharge() > 0)
 			{
 				charge				= currStudent.getCharge() +
 										testStudent.getCharge();
 				
 				if(Math.random() * COUPLE_CHANCE_MULTIPLIER < charge)
 				{
 					couples.add(new Cupple(currStudent, testStudent));
 					
 					student1			= i;
 					student2			= j;
 					
 					if(student2 > student1)
 					{
 						student2--;
 					}
 					
 					try
 					{
 						students.remove(student1);
 						students.remove(student2);
 					}
 					catch(Exception e)
 					{
 						driver.error("Something went wrong when deleting someone :O", false);
 					}
 					
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Handle any attacks on a student
 	 * 
 	 * @param obj
 	 * @return Whether or not the attack hit the student
 	 */
 	public boolean handleAttacks(Student currStudent)
 	{
 		Attack currAttack;
 		
 		// hit by an attack?
 		for(int j = 0; j < attacks.size(); j++)
 		{
 			currAttack		= attacks.get(j);
 			
 			if(currAttack.getBounds().intersects(currStudent.getBounds()) &&
 					currStudent.getCharge() > 0 && currStudent.isHittable() &&
 					currAttack.isStudent())
 			{
 				if(currAttack.getStatus()==Status.STUN)
 				{
 					currStudent.stun(currAttack.getStatusDuration());
 				}
 				
 				if(!currAttack.isAoE())
 				{
 					attacks.remove(j);
 				}
 				
 				// FIXME: should be variable depending on strength
 				if(currStudent.getCharge()>-10)
 					currStudent.adjustCharge(-currAttack.getDamage()/3);
 				System.out.println("Student took "+ currAttack.getDamage()/3 + ", now has "+ currStudent.getCharge());
 				currStudent.setHitDelay(currAttack.getHitDelay());
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Handle any attacks on a couple
 	 * 
 	 * @param currCouple
 	 * @return Whether or not the attack hit the couple
 	 */
 	public boolean handleAttacks(Cupple currCouple)
 	{
 		Attack currAttack;
 		Student male, female;
 		
 		int i				= couples.indexOf(currCouple);
 		
 		// hit by an attack?
 		for(int j = 0; j < attacks.size(); j++)
 		{
 			currAttack		= attacks.get(j);
 			
 			if(currAttack.getBounds().intersects(currCouple.getBounds()) &&
 					currCouple.isHittable() && currAttack.isStudent())
 			{
 				currCouple.adjustHp(currAttack.getDamage());
 				currCouple.setHitDelay(currAttack.getHitDelay());
 				
 				if(!currAttack.isAoE())
 				{
 					attacks.remove(j);
 				}
 				
 				if(currAttack.getStatus()==Status.STUN)
 				{
 					currCouple.stun(currAttack.getStatusDuration());
 				}
 				
 				// FIXME: If multiple attacks hit something or what?
 				if(currCouple.getHp() <=0)
 				{
 					male		= currCouple.getMale();
 					male.moveTo(currCouple.getBounds().x, currCouple.getBounds().y);
 					male.setHitDelay(currAttack.getHitDelay());
 					
 					female		= currCouple.getFemale();
 					female.moveTo(currCouple.getBounds().x + DECOUPLE_SPACING,
 							currCouple.getBounds().y);
 					female.setHitDelay(currAttack.getHitDelay());
 					
 					// create students before removing couple
 					students.add(male);
 					students.add(female);
 					
 					couples.remove(i);
 				}
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Finds the index of the wall that is closest to the object
 	 * 
 	 * @param Movable Object to use
 	 */
 	public int findNearestWall(Movable obj)
 	{
 		Wall nearestWall			= null;
 		int smallestDist			= Integer.MAX_VALUE;
 		int distX, distY, dist;
 		
 		for(Wall wall : walls)
 		{
 			// compute X distance
 			if(wall.getBounds().getX() <= obj.getBounds().getX())
 			{
 				// wall lies to the left
 				distX					= (int) (obj.getBounds().getX() - wall.getBounds().getX());
 			}
 			else {
 				// wall lies to the right
 				distX					= (int) (wall.getBounds().getX() - obj.getBounds().getX());
 			}
 			
 			// compute Y distance
 			if(wall.getBounds().getY() <= obj.getBounds().getY())
 			{
 				// wall lies above
 				distY					= (int) (obj.getBounds().getY() - wall.getBounds().getY());
 			}
 			else {
 				// wall lies below
 				distY					= (int) (wall.getBounds().getY() - obj.getBounds().getY());
 			}
 			
 			// compute distance between wall and object
 			dist						= (int) Math.sqrt(distX * distX + distY * distY);
 			
 			if(dist < smallestDist)
 			{
 				nearestWall				= wall;
 				smallestDist			= dist;
 			}
 		}
 		
 		return walls.indexOf(nearestWall);
 	}
 	
 	/**
 	 * Returns the specified wall
 	 */
 	public Wall getWall(int wallId)
 	{
 		return walls.get(wallId);
 	}
 	
 	/**
 	 * "Game over" message
 	 */
 	public void gameOver()
 	{
 		String msg				= "GAME OVER";
 		
 		// draw the background
 		g.setColor(ParkViewProtector.COLOR_BG_1);
 		g.fillRect(0, 0, ParkViewProtector.WIDTH, ParkViewProtector.HEIGHT);
 		
 		// get width of string
 		int x					= ParkViewProtector.WIDTH / 2;
 		x					   -= g.getFontMetrics().stringWidth(msg);
 		int y					= ParkViewProtector.HEIGHT / 2;
 		y					   -= g.getFontMetrics().getHeight();
 		
 		System.out.println("Put it at (" + x + ", " + y + ")");
 		
 		// draw text
 		g.setFont(new Font("System", Font.BOLD, 32));
 		g.setColor(ParkViewProtector.COLOR_TEXT_1);
 		g.drawString(msg, x, y);
 		
 		// finish drawing
 		g.dispose();
 		strategy.show();
 		
 		try
 		{
 			Thread.sleep(15000);
 		}
 		catch(Exception e)
 		{
 		}
 		
 		driver.quit();
 	}
 	
 	public void showCharges()
 	{
 		g.setColor(ParkViewProtector.COLOR_BG_1);
 		
 		for(int i = 0;i < students.size();i++)
 		{
 			if(students.get(i).getCharge() > 0)
 			{
 				g.drawRect(students.get(i).x, students.get(i).y, 40, 64);
 			}
 		}
 	}
 	
 	/**
 	 * Read a save file
 	 * 
 	 * @param os Object input stream
 	 */
 	private void readObject(ObjectInputStream os)
 	{
 		try
 		{
 			player		= (Staff) os.readObject();
 			students	= (ArrayList<Student>) os.readObject();
 			couples		= (ArrayList<Cupple>) os.readObject();
 			attacks		= (ArrayList<Attack>) os.readObject();
 			
			// FIXME: see below
			walls		= (ArrayList<Wall>) os.readObject();
 		}
 		catch(ClassNotFoundException e)
 		{
 			driver.error("Your save file appears to be corrupt. A ClassNotFoundException occurred.", true);
 		}
 		catch(IOException e)
 		{
 			driver.error("Unable to read the save file", true);
 		}
 	}
 	
 	/**
 	 * Write a save file
 	 * 
 	 * @param os Object output stream
 	 * @throws IOException
 	 */
 	private void writeObject(ObjectOutputStream os) throws IOException
 	{
 		os.writeObject(player);
 		os.writeObject(students);
 		os.writeObject(couples);
 		os.writeObject(attacks);
		
		// FIXME: should we instead just save a level number and regenerate the walls each load?
		os.writeObject(walls);
 	}
 }
