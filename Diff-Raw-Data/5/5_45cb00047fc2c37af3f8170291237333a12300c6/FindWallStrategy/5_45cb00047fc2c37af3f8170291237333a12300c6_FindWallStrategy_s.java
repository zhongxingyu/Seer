 package strategies.wall_follower.find_wall;
 
 import static robot.Platform.ENGINE;
 import static robot.Platform.HEAD;
 import strategies.util.ChildStrategy;
 import strategies.util.MoveDistanceStrategy;
 import utils.Utils.Side;
 
 public class FindWallStrategy extends ChildStrategy {
 	/**
 	 * Speed and direction to drive to the wall
 	 */
 	private final int DRIVE_TO_WALL_SPEED;
 	private final int DRIVE_TO_WALL_DIRECTION = 0;
 
 	private MoveDistanceStrategy moveBack;
 
 	private final int FRONT_POSITION = 0;
 	private final int SIDE_POSITION;
 
 	private final int SEARCH_WALL_SPEED;
 	private final int SEARCH_WALL_DIRECTION;
 	private final int WALL_DISTANCE;
 	private final int NEAR_WALL_DISTANCE;
 
 	// TODO SM (SB) check if collision was false positive through turning head
 	// to front and measuring resistance
 	private enum State {
 		START, // run is only called, if too far away from wall
 		START_MOVE_BACK, // start move backwards to ensure proximity to wall
 		MOVE_BACK, // moving...
 		START_TURN_HEAD_FORWARD, // finished moving -> start to turn head to
 									// front
 		TURN_HEAD_FORWARD, // head turning...
 		START_FIND_WALL, // start turn, until a near wall is found
 		FIND_WALL, // turning...
 		START_DRIVE_TO_WALL, // drive near to wall
 		DRIVE_TO_WALL, // driving...
 		START_LOOSE_WALL, // turn away from wall until you don't see it
 		LOOSE_WALL, // turning...
 		START_TURN_HEAD_SIDEWAYS, // start turning head to wall
 		TURN_HEAD_SIDEWAYS, // head turning...
 		START_SEARCH_WALL, // start turning robot until wall is found
 		SEARCH_WALL, // turning...
 		WALL_FOUND
 	}
 
 	private State currentState;
 
 	private State checkState() {
 		State newState = currentState;
 
 		switch (newState) {
 		case START:
 			newState = State.START_MOVE_BACK;
 			break;
 		case START_MOVE_BACK:
 			newState = State.MOVE_BACK;
 			break;
 		case MOVE_BACK:
 			if (moveBack.isFinished())
 				newState = State.START_TURN_HEAD_FORWARD;
 			break;
 		case START_TURN_HEAD_FORWARD:
 			newState = State.TURN_HEAD_FORWARD;
 			break;
 		case TURN_HEAD_FORWARD:
 			if (!HEAD.isMoving())
 				newState = State.START_FIND_WALL;
 			break;
 		case START_FIND_WALL:
 			newState = State.FIND_WALL;
 			break;
 		case FIND_WALL:
 			if (HEAD.getDistance() < WALL_DISTANCE)
 				newState = State.START_DRIVE_TO_WALL;
 			break;
 		case START_DRIVE_TO_WALL:
 			newState = State.DRIVE_TO_WALL;
 			break;
 		case DRIVE_TO_WALL:
 			if (HEAD.getDistance() < NEAR_WALL_DISTANCE)
 				newState = State.START_LOOSE_WALL;
 			break;
 		case START_LOOSE_WALL:
 			newState = State.LOOSE_WALL;
 			break;
 		case LOOSE_WALL:
 			if (HEAD.getDistance() > WALL_DISTANCE)
 				newState = State.START_TURN_HEAD_SIDEWAYS;
 			break;
 		case START_TURN_HEAD_SIDEWAYS:
 			newState = State.TURN_HEAD_SIDEWAYS;
 			break;
 		case TURN_HEAD_SIDEWAYS:
 			if (!HEAD.isMoving())
 				newState = State.START_SEARCH_WALL;
 			break;
 		case START_SEARCH_WALL:
 			newState = State.SEARCH_WALL;
 			break;
 		case SEARCH_WALL:
 			if (HEAD.getDistance() < WALL_DISTANCE)
 				newState = State.WALL_FOUND;
 			break;
 		case WALL_FOUND:
 			break;
 		}
 
 		if (currentState != newState)
 			System.out.println(currentState.name() + " -> " + newState.name());
 
 		return newState;
 	}
 
 	public FindWallStrategy(Side headSide, int backwardSpeed,
 			int backwardPosition, int nearWallDistance,
 			int wallDistance, int searchWallSpeed,
 			int searchWallDirection, int driveToWallSpeed) {
 		SIDE_POSITION = 1000 * headSide.getValue();
 
 		moveBack = new MoveDistanceStrategy();
 		moveBack.setSpeed(backwardSpeed);
 		moveBack.setTargetPosition(backwardPosition);
 
 		NEAR_WALL_DISTANCE = nearWallDistance;
 		DRIVE_TO_WALL_SPEED = driveToWallSpeed;
 
 		WALL_DISTANCE = wallDistance;
 		SEARCH_WALL_SPEED = searchWallSpeed;
 		SEARCH_WALL_DIRECTION = searchWallDirection * headSide.getValue();
 	}
 
 	@Override
 	public boolean willStart() {
 		// start at start
 		return true;
 	}
 
 	@Override
 	public boolean isStopped() {
 		return currentState == State.WALL_FOUND;
 	}
 
 	@Override
 	protected void childInit() {
 		currentState = State.START;
 	}
 
 	@Override
 	public void check() {
 		//
 	}
 
 	@Override
 	public void work() {
 
 		State oldState = currentState;
 		currentState = checkState();
 		if (oldState != currentState)
 			System.out.println("running: " + currentState.name());
 
 		switch (currentState) {
 		case START:
 			break;
 		case START_MOVE_BACK: // start move backwards to ensure proximity to
 								// wall
 			moveBack.init(); // TODO SB constructor
 			moveBack.init();
 			moveBack.setSpeed(1000);
 			moveBack.setTargetPosition(-200);
 			break;
 		case MOVE_BACK:
 			moveBack.run();
 			// moving...
 			break;
 		case START_TURN_HEAD_FORWARD:
 			// finished moving -> start to turn head to front
 			ENGINE.stop();
			HEAD.moveTo(FRONT_POSITION, true);
 			break;
 		case TURN_HEAD_FORWARD:
 			// head turning...
 			break;
 		case START_FIND_WALL: // start turn, until a near wall is found
 			ENGINE.move(SEARCH_WALL_SPEED, SEARCH_WALL_DIRECTION);
 			break;
 		case FIND_WALL:
 			// turning...
 			break;
 		case START_DRIVE_TO_WALL: // drive near to wall
 			ENGINE.stop();
 			ENGINE.move(DRIVE_TO_WALL_SPEED, DRIVE_TO_WALL_DIRECTION);
 			break;
 		case DRIVE_TO_WALL:
 			// driving...
 			break;
 		case START_LOOSE_WALL: // turn away from wall until you don't see it
 			ENGINE.move(SEARCH_WALL_SPEED, -SEARCH_WALL_DIRECTION);
 			break;
 		case LOOSE_WALL: // turning...
 			break;
 		case START_TURN_HEAD_SIDEWAYS: // start turning head to wall
 			ENGINE.stop();
			HEAD.moveTo(SIDE_POSITION, true);
 			break;
 		case TURN_HEAD_SIDEWAYS:
 			// head turning...
 			break;
 		case START_SEARCH_WALL: // start turning robot until wall is found
 			ENGINE.move(SEARCH_WALL_SPEED, SEARCH_WALL_DIRECTION);
 			break;
 		case SEARCH_WALL:
 			// turning...
 			break;
 		case WALL_FOUND:
 			ENGINE.stop();
 			break;
 		}
 	}
 }
