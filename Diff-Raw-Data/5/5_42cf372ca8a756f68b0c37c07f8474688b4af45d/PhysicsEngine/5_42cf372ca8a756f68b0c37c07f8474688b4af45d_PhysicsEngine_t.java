 package com.hack.balls.engine;
 
 import java.util.List;
 import java.util.Vector;
 
 import com.hack.balls.model.Ball;
 import com.hack.balls.model.Coin;
 import com.hack.balls.model.Player;
 import com.hack.balls.util.PhysicsUtil;
 
 public class PhysicsEngine implements Runnable {
 	private Vector<Ball> balls;
 	private Ball player;
 	public static final int NUM_COINS = 1;
 	public static final int COIN_RADIUS = 2;
 	public static final int COIN_VALUE = 1;
 	public static final int BALL_RADIUS = 4;
 	private int score;
 	public boolean gameOver = false;
 	private int x, y;
 
 	public PhysicsEngine(int x, int y) {
 		this.x = x;
 		this.y = y;
 		balls = new Vector<Ball>();
 		score = 0;
 		player = new Player(0, 0, 5, 0, BALL_RADIUS);
 		balls.add(player);
 		for (int i = 0; i < NUM_COINS; i++) {
 			int coinX = ((int) (Math.random() * (x - COIN_RADIUS)))
 					+ COIN_RADIUS / 2;
 			int coinY = ((int) (Math.random() * (y - COIN_RADIUS)))
 					+ COIN_RADIUS / 2;
 			balls.add(new Coin(coinX, coinY, COIN_VALUE));
 		}
 	}
 
 	@Override
 	public void run() {
 		while (!gameOver) {
 			gameOver = PhysicsUtil.outOfBounds(player, x, y);
 			for (Ball b : balls) {
 				b.setX(b.getX() + b.getDx());
 				b.setY(b.getY() + b.getDy());
 			}
			for (int i = 0; i < balls.size(); i++) {
				Ball b = balls.get(i);
 				if (!(b == player)) {
 					if (PhysicsUtil.colliding(player, b))
 						score += ((Coin) b).getValue();
 					balls.remove(b);
					i -= 1;
 				}
 			}
 		}
 	}
 
 	public Ball getPlayer() {
 		return player;
 	}
 
 	public List<Ball> getBalls() {
 		return balls;
 	}
 
 	public int getScore() {
 		return score;
 	}
 }
