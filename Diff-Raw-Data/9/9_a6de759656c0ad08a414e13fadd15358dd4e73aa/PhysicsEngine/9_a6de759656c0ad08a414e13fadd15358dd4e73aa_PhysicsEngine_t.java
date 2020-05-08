 package com.hack.balls.engine;
 
 import java.util.List;
 import java.util.Vector;
 
 import com.hack.balls.model.Ball;
 import com.hack.balls.model.Coin;
 import com.hack.balls.model.Player;
 import com.hack.balls.util.PhysicsUtil;
 
 public class PhysicsEngine implements Runnable {
 	private List<Ball> balls;
 	private Player player;
 	public static final int NUM_COINS = 1;
 	public static final int COIN_RADIUS = 10;
 	public static final int COIN_VALUE = 50;
 	public static final int BALL_RADIUS = 30;
 	private int score;
 	public boolean gameOver = false;
 	private int x, y;
 
 	public PhysicsEngine(int x, int y) {
 		this.x = x;
 		this.y = y;
 		balls = new Vector<Ball>();
 		score = 0;
 		player = new Player(x / 2, y / 2, 2, 0, BALL_RADIUS);
 		balls.add(player);
 		for (int i = 0; i < NUM_COINS; i++) {
 			addCoin();
 		}
 	}
 
 	@Override
 	public void run() {
		while (!PhysicsUtil.outOfBounds(player, x, y)) {
 			for (int i = 0; i < balls.size(); i++) {
 				Ball b = balls.get(i);
 
 				b.setX(b.getX() + b.getDx());
 				b.setY(b.getY() + b.getDy());
 			}
 			int addCoin = 0;
 			for (int i = 0; i < balls.size(); i++) {
 				Ball b = balls.get(i);
 				if (!(b == player)) {
 					if (PhysicsUtil.colliding(player, b)) {
 						score += ((Coin) b).getValue();
 						balls.remove(b);
 						addCoin += 1;
 						player.increaseSpeed();
 					}
 				}
 
 			}
 			addCoins(addCoin);
 			try {
 				Thread.sleep(20);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("Game over");
 	}
 
 	public void addCoins(int n) {
 		for (int j = 0; j < n; j++) {
 			addCoin();
 		}
 	}
 
 	public void addCoin() {
 		int coinX = ((int) (Math.random() * (x - 2 * COIN_RADIUS)))
 				+ COIN_RADIUS;
 		int coinY = ((int) (Math.random() * (y - 2 * COIN_RADIUS)))
 				+ COIN_RADIUS;
 		balls.add(new Coin(coinX, coinY, COIN_VALUE, COIN_RADIUS
 				+ (int) (Math.random() * 20)));
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public List<Ball> getBalls() {
 		return balls;
 	}
 
 	public int getScore() {
 		return score;
 	}
 }
