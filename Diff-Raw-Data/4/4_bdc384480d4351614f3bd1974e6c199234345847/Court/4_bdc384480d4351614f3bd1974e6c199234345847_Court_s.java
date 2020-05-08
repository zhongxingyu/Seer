 package com.mmakowski.android.volleyball.model;
 
 import static com.mmakowski.android.volleyball.model.GameplayConstants.*;
 import static com.mmakowski.android.volleyball.model.GameElementDimensions.BALL_SIZE_TO_VIEW_WIDTH_RATIO;
 import static com.mmakowski.android.volleyball.model.GameElementDimensions.COURT_WIDTH_TO_VIEW_WIDTH_RATIO;
 import static com.mmakowski.android.volleyball.model.GameElementDimensions.FLOOR_LEVEL_TO_VIEW_HEIGHT_RATIO;
 import static com.mmakowski.android.volleyball.model.GameElementDimensions.NET_HEIGHT_TO_VIEW_HEIGHT_RATIO;
 import static com.mmakowski.android.volleyball.model.GameElementDimensions.PLAYER_HEIGHT_TO_VIEW_HEIGHT_RATIO;
 import static com.mmakowski.android.volleyball.model.GameElementDimensions.PLAYER_WIDTH_TO_VIEW_WIDTH_RATIO;
 import static java.lang.Math.abs;
 import static java.lang.Math.min;
 import static java.lang.Math.pow;
 
 import java.util.Random;
 
 /**
  * Court represents all elements of the game and takes care of updating the physics.
  * 
  * @author mmakowski
  *
  */
 public final class Court {
 	public static final int HUMAN_TEAM = 0;
 	public static final int AI_TEAM = 1;
 	public static final int SIDE_LEFT = -1;
 	public static final int SIDE_RIGHT = 1;
 
 	private static final int PLAYERS_PER_TEAM = 2;
 	private static final int SETTER = 0;
 	private static final int ATTACKER = 1;
 	
 	private static final int STATE_NOT_SET_UP = 0;
 	private static final int STATE_HUMAN_TEAM_SERVE = 1;
 	private static final int STATE_AI_TEAM_SERVE = 2;
 	private static final int STATE_HUMAN_TEAM_PLAY = 3;
 	private static final int STATE_AI_TEAM_PLAY = 4;
 	
 	private static final float[] INITIAL_OFFSET = { 0.025f, 0.33f };
 	private int state = STATE_NOT_SET_UP;
 	private int lastTeamTouch;
 	private int[] lastPlayerTouch;
 	private int viewWidth;
 	public int width;
 	public int netPositionX;
 	public int netHeight;
 	public Player[][] players;
 	private int[] playerGoingForTheBall = new int[2];
 	public Ball ball;
 	public int floorLevel;
 	public int humanSide = SIDE_LEFT;
 	private int ballSize;
 	private int playerWidth;
 	private int playerHeight;
 	private int courtOffset;
 	private int[] points = {0, 0};
 	private int touchCount;
 	private long lastTouchTime = 0;
 	
 	private Random random = new Random();
 	
 	public synchronized void setUp() {
 		players = new Player[2][PLAYERS_PER_TEAM];
 		int playerPosY = playerHeight + floorLevel;
 		for (int t = 0; t < 2; t++) {
 			int dir = teamSideDirection(t);
 			for (int p = 0; p < PLAYERS_PER_TEAM; p++) {
 				players[t][p] = new Player(t, netPositionX + dir * ((int) (width * INITIAL_OFFSET[p]) + (dir == SIDE_LEFT ? playerWidth : 0)), playerPosY);
 				players[t][p].setDefaultBallTarget(this, oppositeSideDirection(t));
 			}
 		}
 		ball = new Ball();
 		lastPlayerTouch = new int[] { -1, -1};
 		enterState(STATE_HUMAN_TEAM_SERVE);
 	}
 
 	private int teamSideDirection(int team) {
 		return team * 2 - 1;
 	}
 
 	private void enterState(int newState) {
 		switch (newState) {
 		case STATE_HUMAN_TEAM_SERVE:
 			ball.positionX = courtOffset / 2;
 			ball.positionY = netHeight + floorLevel;
 			ball.velocityX = 230;
 			ball.velocityY = 100;
 			playerGoingForTheBall[HUMAN_TEAM] = playerGoingForTheBall[AI_TEAM] = ATTACKER;
 			lastTeamTouch = HUMAN_TEAM;
 			enterState(STATE_AI_TEAM_PLAY);
 			break;
 		case STATE_AI_TEAM_SERVE:
 			ball.positionX = viewWidth - courtOffset / 2;
 			ball.positionY = netHeight + floorLevel;
 			ball.velocityX = -230;
 			ball.velocityY = 100;
 			playerGoingForTheBall[HUMAN_TEAM] = playerGoingForTheBall[AI_TEAM] = ATTACKER;
 			lastTeamTouch = AI_TEAM;
 			enterState(STATE_HUMAN_TEAM_PLAY);
 			break;
 		case STATE_HUMAN_TEAM_PLAY:
 			for (Player player : players[AI_TEAM]) player.goHome();
 			playerGoingForTheBall[AI_TEAM] = ATTACKER;
 			break;
 		case STATE_AI_TEAM_PLAY:
 			for (Player player : players[HUMAN_TEAM]) player.goHome();
 			playerGoingForTheBall[HUMAN_TEAM] = ATTACKER;
 			break;
 		}
 		state = newState;
 	}
 
 	public synchronized void update(long elapsedTimeMs) {
 		float secFraction = elapsedTimeMs / 1000f;
 		updateBall(secFraction);
 		updatePlayers(secFraction);
 	}
 	
 	private void updatePlayers(float secFraction) {
 		for (int t = 0; t < 2; t++) {
 			for (Player player : players[t]) {
 				int posDiff = abs(player.targetPositionX - player.positionX);
 				if (posDiff != 0) {
 					int dir = player.targetPositionX > player.positionX ? 1 : -1;
 					int move = (int) min(posDiff, Physics.PLAYER_MAX_MOVEMENT_SPEED * secFraction);
 					player.positionX += dir * move;
 				}
 			}
 		}
 	}
 
 	private void updateBall(float secFraction) {
 		int nextX = (int) (ball.positionX + ball.velocityX * secFraction);
 		int sideEnteredByBall = ballEnteredSide(ball.positionX, nextX);
 		switch (sideEnteredByBall) {
		case SIDE_LEFT: enterState(STATE_HUMAN_TEAM_PLAY);
		case SIDE_RIGHT: enterState(STATE_AI_TEAM_PLAY);
 		}
 		ball.positionX = nextX;
 		ball.velocityX += Physics.aerodynamicDragDeceleration(ball.velocityX) * secFraction;  
 		ball.positionY = (int) (ball.positionY + ball.velocityY * secFraction);
 		if (ball.positionY - ballSize <= floorLevel) {
 			ballTouchedGround();
 		} else {
 			ball.velocityY -= Physics.GRAVITY * secFraction;
 		}
 		ball.velocityY += Physics.aerodynamicDragDeceleration(ball.velocityY) * secFraction;
 		processPlayerCollision();
 		// TODO: net collision 
 	}
 
 	/**
 	 * @return SIDE_LEFT if the ball has crossed from right to left, SIDE_RIGHT if the ball has crossed from left to right, 0 otherwise
 	 */
 	private int ballEnteredSide(int currX, int nextX) {
 		if (currX + ballSize / 2 < netPositionX && nextX + ballSize / 2 >= netPositionX) return SIDE_RIGHT;
 		if (currX + ballSize / 2 > netPositionX && nextX + ballSize / 2 <= netPositionX) return SIDE_LEFT;
 		return 0;
 	}
 
 	private void processPlayerCollision() {
 		for (int t = 0; t < 2; t++) {
 			for (int p = 0; p < PLAYERS_PER_TEAM; p++) {
 				Player player = players[t][p];
 				int offsetX = player.positionX - ball.positionX;
 				int offsetY = player.positionY - ball.positionY;
 				if (ballHitPlayer(offsetX, offsetY)) {
 					long currentTouchTime = System.currentTimeMillis();
 					if (lastTeamTouch == t) {
 						if (currentTouchTime - lastTouchTime > MAX_SINGLE_TOUCH_TIME) touchCount++;
 					} else {
 						touchCount = 1;
 					}
 					lastTouchTime = currentTouchTime;
 					touchCount = lastTeamTouch == t ? touchCount + 1 : 1;
 					if (touchCount > 3) {
 						tooManyTouches();
 						return;
 					}
 					bounceBallOffPlayer(player, offsetX);
 					playerGoingForTheBall[t] = otherPlayer(p);
 					lastTeamTouch = t;
 					lastPlayerTouch[t] = p;
 					player.goHome();
 					return;
 				}
 			}
 		}
 	}
 
 	private int otherPlayer(int player) {
 		return player == SETTER ? ATTACKER : SETTER;
 	}
 
 	private void tooManyTouches() {
 		int winner = opponent(lastTeamTouch);
 		points[winner]++;
 		// TODO: check for victory
 		enterState(winner == HUMAN_TEAM ? STATE_HUMAN_TEAM_SERVE : STATE_AI_TEAM_SERVE);
 	}
 
 	private boolean ballHitPlayer(int offsetX, int offsetY) {
 		return offsetX >= -playerWidth && offsetX <= ballSize && ball.positionY + ballSize > floorLevel && offsetY >= -ballSize;
 	}
 
 	private void bounceBallOffPlayer(Player player, int offsetX) {
 		int oppositeSideDirection = oppositeSideDirection(player.team);
 		int idealY = player.positionY + ballSize;
 		int idealX = oppositeSideDirection == SIDE_LEFT ? player.positionX - ballSize : player.positionX + playerWidth;
 		float ballPositionBonusX = 1f - abs(((float) ball.positionX - idealX) / ((float) playerWidth + (float) ballSize)); // worst X is at the opposite edge of the player
 		float ballPositionBonusY = 1f - abs(((float) ball.positionY - idealY) / ((float) playerHeight)); // worst Y is at the feet of the player;
 		float ballPositionBonus = ballPositionBonusX * ballPositionBonusY;
 		  
 		ball.velocityY = 180f * penalty(ballPositionBonus, MAX_DIFFICULT_POSITION_PENALTY) * penalty(player.accuracy, MAX_PLAYER_INACCURACY_PENALTY);
 		ball.velocityX = 120f * oppositeSideDirection * (float) pow(((float) abs(player.positionX - player.ballTargetX)) / ((float) width), 2)
 				* penalty(ballPositionBonus, MAX_DIFFICULT_POSITION_PENALTY) * penalty(player.accuracy, MAX_PLAYER_INACCURACY_PENALTY);
 	}
 	
 	/**
 	 * @param bonus a number in the range 0..1 -- if 1 there will be no penalty, if 0 there will be maximum penalty 
 	 * @param max maximum penalty 
 	 * @return normally-distributed penalty in the range -(1 - bonus) * max .. (1 - bonus) * max 
 	 */
 	private float penalty(float bonus, float max) {
 		return 2f * ((float) random.nextGaussian() - 0.5f) * (1f - bonus) * max + 1f;
 	}
 
 	private int oppositeSideDirection(int team) {
 		return teamSideDirection(team) * -1;  
 	}
 
 	private void ballTouchedGround() {
 		int winner;
 		int x = ball.positionX + ballSize / 2;
 		if (x < courtOffset || x > viewWidth - courtOffset) winner = opponent(lastTeamTouch);
 		else if (x <= netPositionX) winner = AI_TEAM;
 		else winner = HUMAN_TEAM;
 		points[winner]++;
 		// TODO: check for victory
 		enterState(winner == HUMAN_TEAM ? STATE_HUMAN_TEAM_SERVE : STATE_AI_TEAM_SERVE);
 	}
 
 	public synchronized void setViewDimensions(int width, int height) {
 		viewWidth = width;
 		this.width = (int) (width * COURT_WIDTH_TO_VIEW_WIDTH_RATIO);
 		courtOffset = (viewWidth - this.width) / 2;
 		netPositionX = width / 2;
 		netHeight = (int) (height * NET_HEIGHT_TO_VIEW_HEIGHT_RATIO);
 		floorLevel = (int) (height * FLOOR_LEVEL_TO_VIEW_HEIGHT_RATIO);
 		playerWidth = (int) (width * PLAYER_WIDTH_TO_VIEW_WIDTH_RATIO);
 		playerHeight = (int) (height * PLAYER_HEIGHT_TO_VIEW_HEIGHT_RATIO);
 		ballSize = (int) (width * BALL_SIZE_TO_VIEW_WIDTH_RATIO);
 		// TODO: reposition players and ball
 	}
 	
 	public boolean isSetUp() {
 		return state != STATE_NOT_SET_UP;
 	}
 
 	public void movePlayer(int team, int targetX) {
 		if (targetX < 0) targetX = 0;
 		else if (targetX + playerWidth > viewWidth) targetX = viewWidth - playerWidth;
 		else if (team == HUMAN_TEAM && targetX + playerWidth > netPositionX) targetX = netPositionX - playerWidth;
 		else if (team == AI_TEAM && targetX < netPositionX) targetX = netPositionX;
 		players[team][playerGoingForTheBall[team]].targetPositionX = targetX;
 	}
 
 	private final int opponent(int team) {
 		return team == HUMAN_TEAM ? AI_TEAM : HUMAN_TEAM;
 	}
 
 	public String getScore() {
 		return points[HUMAN_TEAM] + ":" + points[AI_TEAM];
 	}
 	
 }
