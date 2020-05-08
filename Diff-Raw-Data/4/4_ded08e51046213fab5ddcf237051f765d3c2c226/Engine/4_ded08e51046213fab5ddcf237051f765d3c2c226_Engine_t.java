 package actors;
 
 import static robot.Platform.LEFT_MOTOR;
 import static robot.Platform.RIGHT_MOTOR;
 
 public class Engine implements Actor {
 
 	int newLeftSpeed = 0;
 	int newRightSpeed = 0;
 
 	@Override
 	public void commit() {
 		if (newLeftSpeed == 0) {
 			LEFT_MOTOR.stop();
 		} else if (newLeftSpeed > 0) {
 			LEFT_MOTOR.setSpeed(newLeftSpeed);
 			LEFT_MOTOR.forward();
 		} else {
 			LEFT_MOTOR.setSpeed(-newLeftSpeed);
 			LEFT_MOTOR.backward();
 		}
 		if (newRightSpeed == 0) {
 			RIGHT_MOTOR.stop();
 		} else if (newRightSpeed > 0) {
 			RIGHT_MOTOR.setSpeed(newRightSpeed);
 			RIGHT_MOTOR.forward();
 		} else {
 			RIGHT_MOTOR.setSpeed(-newRightSpeed);
 			RIGHT_MOTOR.backward();
 		}
 	}
 
 	/**
 	 * Stop the motor
 	 */
 	public void stop() {
 		newLeftSpeed = 0;
 		newRightSpeed = 0;
 	}
 
 	/**
 	 * Return, whether we are moving (or rotating)
 	 * 
 	 * @return True, iff the robot is either moving or rotating.
 	 */
 	public boolean isMoving() {
 		return LEFT_MOTOR.isMoving() || RIGHT_MOTOR.isMoving();
 	}
 
 	/**
 	 * Rotate the robot
 	 * 
 	 * @param speed
 	 *            The speed used for rotation. If -1000<=speed<0, we rotate left
 	 *            If 0<speed<1000, we rotate right
 	 */
 	public void rotate(int speed) {
 		if (speed < 0)
 			move(-speed, -1000);
 		else
 			move(speed, 1000);
 	}
 
 	/**
 	 * Move straight forward with a given speed
 	 * 
 	 * @param speed
 	 *            The speed to move with. If -1000<=speed<0, it moves backward.
 	 *            If 0<speed<1000, it moves forward.
 	 */
 	public void move(int speed) {
 		move(speed, 0);
 	}
 
 	/**
 	 * Move forward or backward
 	 * 
 	 * @param speed
 	 *            The speed. If -1000<=speed<0, it moves backward. If
 	 *            0<speed<1000, it moves forward.
 	 * @param direction
 	 *            The direction to move (speed difference between left and right
 	 *            chain). -1000<=direction<0 for moving left 0<direction<1000
 	 *            for moving right.
 	 */
 	public void move(int speed, int direction) {
 		if (1000 < speed || -1000 > speed || 1000 < direction
 				|| -1000 > direction)
 			throw new IllegalStateException("Incorrect parameters speed:"+speed+", direction:"+direction);
 
 		final int MAX = 1000;
 
 		int left = 500, right = 500;
 
		left += direction;
		right -= direction;
 
 		left *= 2;
 		right *= 2;
 
 		left = Math.min(MAX, Math.max(-MAX, left));
 		right = Math.min(MAX, Math.max(-MAX, right));
 
 		if (1000 < right || -1000 > right || 1000 < left || -1000 > left)
 			throw new IllegalStateException("Incorrect intermediate values");
 
 		left *= speed;
 		right *= speed;
 		left /= MAX;
 		right /= MAX;
 
 		if (1000 < right || -1000 > right || 1000 < left || -1000 > left)
 			throw new IllegalStateException("Incorrect intermediate 2 values");
 
 		newLeftSpeed = left;
 		newRightSpeed = right;
 	}
 }
