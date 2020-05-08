 package com.hack.balls.util;
 
 import com.hack.balls.model.Ball;
 
 public class PhysicsUtil {
 	public static double distance(Ball ball1, Ball ball2) {
 		return Math.sqrt(Math.pow(ball2.getX() - ball1.getX(), 2)
 				+ Math.pow(ball2.getY() - ball2.getY(), 2));
 	}
 
 	public static boolean colliding(Ball ball1, Ball ball2) {
 		return (ball1.getRadius() + ball2.getRadius()) >= distance(ball1, ball2);
 	}
 
 	public static boolean outOfBounds(Ball ball, int x, int y) {
 		return ((ball.getX() + ball.getRadius() > x) || (ball.getX()
				- ball.getRadius() < 0))
 				|| ((ball.getY() + ball.getRadius() > y) || (ball.getY()
						- ball.getRadius() < 0));
 	}
 }
