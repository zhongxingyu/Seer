 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 /** @file Goalie.java
 * Class file for Goalie class
 * @author Joel Tanzi
 * @date 11 October 2011
 * @version 1.3 
 */
 
 /** @class Goalie
 * The Goalie class inherits from the Player class.  The Goalie is a specialized
 * type of Player that may catch the ball under certain conditions and defends the goal
 * from the opposing team. 
 */
 public class Goalie extends Player {
 
 	/**
 	 * 
 	 */
 	public Goalie() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 	
 	public Goalie(String team){
 		super.getRoboClient().setTeam(team);
 	}
 
 	 /**
 	 * Initializes the Player with the RoboCup server as a goalie.
 	 * @pre A RoboCup server is available.
 	 * @post The Player has been initialized to the correct team as a goalie.
 	 */
 	public void initGoalie(double x, double y) throws SocketException, UnknownHostException {
 
 		rc.dsock = new DatagramSocket();
 		rc.initGoalie(getParser(), getMem());
 		try {
 			move(x,y);
 			Thread.sleep(100);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		@SuppressWarnings("unused")
 		GoalieBrain b = new GoalieBrain(this);
 	}
 
 	 /**
 	 * Causes the Goalie to catch the ball.
 	 * @pre Playmode is play-on, ball is within goalkeeper zone and in the catchable area.
 	 * @post The Goalie has caught the ball.
 	 */
 	public void catchball(double d) throws UnknownHostException{
 		rc.catchball(d);
 		ballCaught = true;
 	}
 
 	
 	/**
 	 * Turns goalie toward the ball
 	 * @post The goalie will turn in the direction of the ball
 	 */
 	public void followBall() {
 
 
 		try {
 			if(!getMem().isObjVisible("ball")) {
 				turn(45);
 				return;
 			}
 			if(getMem().isObjVisible("ball")) {
 				ObjBall ball = getMem().getBall();
 
 				if((ball.getDirection() > 5.0) || (ball.getDirection() < -5.0)) {
 					turn(ball.getDirection() * (1 + (5 * getMem().getAmountOfSpeed())));
 				}
 				if(ballInGoalzone(ball)){
 					//System.out.println("flag in defendGoal");
 					defendGoal(ball);
 				} else {
 					//System.out.println("flag in positionGoalie");
 					positionGoalie(ball);
 				}
 			}
 
 		} catch (UnknownHostException e) {
 			System.out.println("Error in Goalie.followBall()");
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * A method to determine whether the ball is in the penalty box
 	 * 
 	 * @param ball the ObjBall to follow
 	 * @pre this must be called with an ObjBall
 	 * @post true if ball is in penalty box, false if it's not
 	 * @return boolean
 	 */
 	public boolean ballInGoalzone(ObjBall ball) {
 		
 		if(ball == null)
 			return false;
 		
 		
 		Pos ballPos = mh.getPos(ball.getDistance(), getDirection() + ball.getDirection());
 		ballPos = mh.vAdd(getPosition(), ballPos);
 
 		if(((ballPos.x <= -36) && (ballPos.x >= -52.5)) && ((-20.16 <= ballPos.y) && (ballPos.y <= 20.16)))
 			return true;
 		else
 			return false;			
 	}
 	
 	
 	/**
 	 * Returns true or false depending on whether the ball is within the catchable range
 	 * of the goalie.
 	 * @pre The ball is visible to the goalie
 	 * @post The ball is determined to catchable or not.
 	 * @return boolean True if catchable, false if not.
 	 */
 	public boolean catchable() {
 		
 		boolean catchable = false;
 		
 		//Test for visibility
 		if (getMem().isObjVisible("ball") && ballInGoalzone(getMem().getBall())) {
 			
 			//Test for moment range
 			if (getMem().getBall().getDirection()> -180 && getMem().getBall().getDirection() < 180) {
 				
 				//Test for catchable distance
 				if (getMem().getBall().getDistance() < 2.0) {
 					catchable = true;
 				}
 			}
 		}		
 		return catchable;
 	}
 	
 
 
 	/**
 	 * Causes the goalie to act to intercept the ball as it approaches the goal.
 	 * @param ObjBall representing the ball in play.
 	 * @throws UnknownHostException 
 	 * @throws InterruptedException 
 	 * @pre The ball has entered the goal zone.
 	 * @post The ball has been caught by the goalie, or the goalie has missed the ball.
 	 */
 	public void defendGoal(ObjBall ball) throws UnknownHostException, InterruptedException {
 		//Pos ridBallPoint = new Pos(0,0);
 		
 		//Move to catchable range of ball
 		if (ball.getDistance() > 1.0) {
 			getAction().gotoPoint(mh.getNextBallPoint(ball));
 			//Thread.sleep(100);
 		}
 		else {
 			if((getMem().side.compareTo("l") == 0) && ((getMem().playMode.compareTo("goalie catch ball_l") == 0) || (getMem().playMode.compareTo("free_kick_l") == 0))) {
 				Thread.sleep(500);
 				turn(-getMem().getDirection());
 				Thread.sleep(200);
 				kick(100, 0);
 				Thread.sleep(100);	
 			}
 			else if((getMem().side.compareTo("r") == 0) && ((getMem().playMode.compareTo("goalie catch ball_r") == 0) || (getMem().playMode.compareTo("free_kick_r") == 0))) {
 				Thread.sleep(500);
 				turn(-getMem().getDirection());
 				Thread.sleep(200);
 				kick(100, 0);
 				Thread.sleep(100);
 			}
 			else {
 				catchball(getMem().getBall().getDirection());
 				
 				
 			}
 			
 			
 			/*
 			//If ball is in catchable area, catch it
 			System.out.println("catchable");
 			if (!ballCaught) {
 				catchball(getMem().getBall().getDirection());
 				Thread.sleep(100);
 				ballCaught = true;	
 			}
 			
 			//kickToPlayer(closestPlayer());
 			getAction().kickToPoint(ball, ridBallPoint);
 			Thread.sleep(100);	
 			*/
 		}
 	} //end method
 	
 	
 	/**
 	 * Moves goalie to specific points within the goalbox dependent upon where the ball is on the field.
 	 * @param ball An ObjBall representing the ball in play.
 	 * @throws InterruptedException 
 	 * @pre The ball is visible.
 	 * @post The goalie has moved to a strategic position to get between the ball and the goal.
 	 */
 	public void positionGoalie(ObjBall ball) throws InterruptedException {
 		
 		Pos ballPos = mh.getPos(ball.getDistance(), getDirection() + ball.getDirection());
 		ballPos = mh.vAdd(getPosition(), ballPos);
 		Pos upper = new Pos(-49, -6);
 		Pos middle = new Pos(-49, 0);
 		Pos lower = new Pos (-49, 6);		
 		//ballPos.print("ballPos: ");
 
 		if (!ballInGoalzone(ball)) {
 			if (ballPos.y < -18) {  //If ball is in upper portion of field
 				//System.out.println("flag1");
 				getAction().gotoSidePoint(upper);
 				Thread.sleep(100);
 			}
 			else if (ballPos.y > -18 && ballPos.y < 18) { //If ball is midfield vertically
 				//System.out.println("flag2");
 				getAction().gotoSidePoint(middle);
 				Thread.sleep(100);
 			}
 			else {  //If ball is in lower portion of field
 				//System.out.println("flag3");
 				getAction().gotoSidePoint(lower);
 				Thread.sleep(100);
 			}
 		}
 	}
 	
 	/**
 	 * Moves goalie between the ball and the goal (under construction)
 	 * @param ball An ObjBall.
 	 * @pre Ball is visible to the goalie.
 	 * @post The goalie has moved to a point on the line between the ball and the goal.
 	 */
 	public void getBtwBallAndGoal(ObjBall ball) {
 		
 		Pos ballPos = mh.getPos(ball.getDistance(), getDirection() + ball.getDirection());
 		ballPos = mh.vAdd(getPosition(), ballPos);
 		Pos goalPos = getMem().getOwnGoalPos();
 		Pos newPos = new Pos();
 		boolean between = false;
 		
 		double slope = (goalPos.y - ballPos.y)/(goalPos.x - ballPos.x);
 		double x_p = 0.66 * (goalPos.x - ballPos.x) + ballPos.x;
 		double y_int = ballPos.y - ballPos.x * slope;
 		double y_p = slope * x_p + y_int;
 		newPos.x = x_p;
 		newPos.y = y_p;
 		
 		if (ball.getDirChng() > 20 & !between) {
 			getAction().gotoPoint(newPos);
 			between = true;
 		}		
 	}
 
 	/**
 	 * Returns the closest player to the goalie on the same team.
 	 * @post The closest player to the goalie has been determined.
 	 * @return ObjPlayer
 	 * @throws InterruptedException 
 	 * @throws UnknownHostException 
 	 */
 	public ObjPlayer closestPlayer() throws UnknownHostException, InterruptedException {
 		ObjPlayer closestPlayer = new ObjPlayer();
 		double distance = 0;
 
 		//Loop through arraylist of ObjPlayers
 		for (int i = 0; i < getMem().getPlayers().size(); ++i) {
 
 			if (!getMem().getPlayers().isEmpty()) {  
 				if (distance == 0 && getMem().getPlayers().get(i).getTeam() == rc.getTeam()) {
 					distance = getMem().getPlayers().get(i).getDistance();
 				}
 				else {
 
 					//Test if this player is closer than the previous one
 					if (distance > getMem().getPlayers().get(i).getDistance() && getMem().getPlayers().get(i).getTeam() == rc.getTeam()) {
 						distance = getMem().getPlayers().get(i).getDistance();
 						closestPlayer = getMem().getPlayers().get(i);
 					}
 				}
 			}
 			else {  //No players in goalie's sight, so turn to another point to check again
 				turn(30);
 				
 				if (!getMem().getPlayers().isEmpty()) {  
 					if (distance == 0 && getMem().getPlayers().get(i).getTeam() == rc.getTeam()) {
 						distance = getMem().getPlayers().get(i).getDistance();
 					}
 					else {
 						//Test if this player is closer than the previous one
 						if (distance > getMem().getPlayers().get(i).getDistance() && getMem().getPlayers().get(i).getTeam() == rc.getTeam()) {
 							distance = getMem().getPlayers().get(i).getDistance();
 							closestPlayer = getMem().getPlayers().get(i);
 						}
 					}
 				}
 				
 			}
 		}		
 		return closestPlayer;
 	}
 
 	/**
 	 * Causes goalie to kick the ball to a specific player.
 	 * @pre A player is in sight of the goalie.
 	 * @post The goalie has kicked the ball to the player passed to the function.
 	 * @param player An ObjPlayer representing the player to receive the ball.
 	 */
 	public void kickToPlayer(ObjPlayer player) {
 
 		if(getMem().isObjVisible("ball")) {
 			ObjBall ball = getMem().getBall();
 			getAction().kickToPoint(ball, mh.getPos(new Polar(player.getDistance(), player.getDirection())));
 			//ballCaught = false;
 		}
 	}
 	
 	/**
 	 * Causes the goalie to kick the ball out of bounds
 	 * @pre Goalie has control of the ball
 	 * @post Ball has been kicked out of bounds
 	 */
 	public void kickBallOutOfBounds() {
 		try {
 			//Locate closest flag on a boundary line
 			ObjFlag kickFlag = new ObjFlag();
 			kickFlag = getMem().getClosestBoundary();
 			//System.out.println("Flag name: " + kickFlag.getFlagName());
 			
 			//Test to ensure the flag is within a kickable range, and
 			// is not dangerously close to the goal, and kick it if allowable
 			if (kickFlag.getDistance() < 25 && kickFlag.getFlagName() != "flt10" && kickFlag.getFlagName() != "fl0"
 				&& kickFlag.getFlagName() != "flb10" && kickFlag.getFlagName() != "frt10" 
 					&& kickFlag.getFlagName() != "fr0" && kickFlag.getFlagName() != "frb10") {
 				kick(90,kickFlag.getDirection());
 				//ballCaught = false;
 				Thread.sleep(100);
 			}			
 			else {  //Turn to a new position and check flag again
 				turn(-30);
 				Thread.sleep(100);
 				
 				//Kick if the boundary flag is now reachable
 				kickFlag = getMem().getClosestBoundary();
 				if (kickFlag.getDistance() < 25 && kickFlag.getFlagName() != "flt10" && kickFlag.getFlagName() != "fl0"
 					&& kickFlag.getFlagName() != "flb10" && kickFlag.getFlagName() != "frt10" 
 						&& kickFlag.getFlagName() != "fr0" && kickFlag.getFlagName() != "frb10") {
 					kick(90,kickFlag.getDirection());
 					//ballCaught = false;
 					Thread.sleep(100);
 				}	
 			}
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	//Run method for Goalie's individual thread
 	public void run() {
 		while(true) {
 			try {
 				receiveInput();
 			} catch (InterruptedException e) {
 				System.out.println("Interrupt error at Player.run");
 				e.printStackTrace();
 			}
			/*
 			if(getMem().current != null) {
 				Pos pt = mh.vSub(getMem().current, getMem().home);
 				
 				if(mh.mag(pt) > 0.5) {
 					getMem().isHome = false;
 				}
 				else
 					getMem().isHome = true;
 			}
 			else 
 				System.out.println("Current is null");
			*/
 		}
 		
 	}	
 	
 	
 	public boolean ballTurn = false;
 	public MathHelp mh = new MathHelp();
 	boolean ballCaught = false;
 
 
 } //end class
 
