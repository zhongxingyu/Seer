 package org.crowdball;
 
 import org.joda.time.DateTime;
 
 public class Ball {
 
 	static final int SINGLE_HIT = 1;
 	static final int ROOM_SIZE = 100;
 	static final int MAX_SPEED = 15;
 	int speed = 0;
 	long startTime;
 	long lastCalculatedTime;
 	
 	int position = 0;
 	private boolean gameOver;
 	
 	public Ball() {
 		long currentTimeMillis = getCurrentTimeMillis();
 		startTime = currentTimeMillis;
 		lastCalculatedTime = currentTimeMillis;
 	}
 	
 	
 	private long getCurrentTimeMillis() {
 		return new DateTime().getMillis();
 	}
 
 
 	public int speed() {
 		return speed;
 	}
 
 	public void hitRecievedFromA() {
		if(speed < Ball.MAX_SPEED)
 			speed += SINGLE_HIT;
 	}
 
 	public void hitRecievedFromB() {
		if(speed > -Ball.MAX_SPEED)
 			speed -= SINGLE_HIT;
 	}
 
 	public int position() {
 		long currentTimeMillis = getCurrentTimeMillis();
 		position = position + calculateRange(currentTimeMillis, lastCalculatedTime);
 		lastCalculatedTime = currentTimeMillis;
 		//DIRTY HACK TO SIMULATE WALL ...
 		if(ROOM_SIZE <= Math.abs(position)) {
 			//setSpeed(0.0);
 			//find out how to send the game over signal...
 			gameOver = true;
 		}
 		return position;
 	}
 	
 	void setPosition(int newPosition) {
 		position = newPosition;
 		lastCalculatedTime = getCurrentTimeMillis();
 	}
 	
 	void setSpeed(int newSpeed) {
 		this.speed = newSpeed;
 	}
 
 
 	private int calculateRange(long currentTimeMillis, long time) {
 		return (int)((currentTimeMillis - time) * speed / 1000);
 	}
 
 
 	public void resetAndStart() {
 		position = 0;
 		speed = -SINGLE_HIT;
 		gameOver = false;
 	}
 	
 	public boolean gameOver() {
 		return gameOver;
 	}
 
 	@Override
 	public String toString() {
 		return "Ball:"+
 		"\nPosition = " + position +
 		"\nSpeed= " + speed +
 		"\nLastUpdatedTime = " + lastCalculatedTime;
 	}
 	
 }
