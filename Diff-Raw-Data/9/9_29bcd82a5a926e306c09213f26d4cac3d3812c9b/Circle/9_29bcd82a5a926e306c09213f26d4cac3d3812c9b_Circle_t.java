 package com.subquantum.game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 
 public class Circle {
 	private int ballX = 0;
 	private int ballY = 0;
 	private int ballR = 0;
 	private Color ballColor = Color.white;
 	
 	private int ballXDir = 1;
 	private int ballYDir = 1;
 	
 	private static boolean ballMoving = false;
 
 	public Circle (Color col) {
 		ballR = 10;
 		ballX = Game.WIDTH/2 - ballR;
 		ballY = Game.HEIGHT/2 - ballR;
 		
 		ballColor = col;
 	}
 	
 	public void draw(Graphics g) {
 		g.setColor(ballColor);
 		g.fillOval(ballX, ballY, ballR*2, ballR*2);
 		Graphics2D g2d = (Graphics2D)g;
 		//g2d.draw3DRect(playerX, playerY, playerW, playerH, true);
 		//g2d.fillRect(playerX+1, playerY+1, playerW-1, playerH-1);
 		
 		//g2d.draw
     }
 	
 	public void move() {
 		int x = 3;
 		int y = 4;	
 		
 		
 		if ((ballX+ballR*2 >= Game.WIDTH)|| (ballX <= 0)) {
 			ballXDir = ballXDir * -1;
 			
 			if (ballXDir == -1 ){
 				Scoring.player1++;
 			}
 			if (ballXDir == 1) {
 				Scoring.player2++;
 			}
 			
 			ballMoving = false;
 			ballX = (Game.WIDTH/2) - ballR;
 			ballY = (Game.HEIGHT/2)-ballR;
 
 		}
 		
 		if ((ballY + ballR*2 >= Game.HEIGHT) || (ballY <= 0)) {
 			ballYDir = ballYDir * -1;
 			//System.out.println("ballY: " + ballY + " Game.HEIGHT: "+ Game.HEIGHT);
 		}
 		if (checkColision()) {
 			//System.out.println("DirX: "+ ballXDir+"DirY: " + ballYDir);
 			//ballYDir = ballYDir * -1;
			
			if (ballXDir == -1) {
				ballX = Game.player1.playerX + Game.player1.playerW;
			}
			if (ballXDir == 1) {
				ballX = Game.player2.playerX - ballR*2;
			}
 			ballXDir = ballXDir * -1;
 			
			
 			//ballX = ballX - (ballR);
 			//ballY = ballY + ballR;
 			
 			//ballX = ballX + (x*ballXDir);
 			//ballY = ballX + (y*ballYDir);
 			
 			//System.out.println("HIT");
 		}
 			
 		
 		//System.out.println("DirX: "+ ballXDir+"DirY: " + ballYDir);
 		if (ballMoving) {
 			ballX = ballX + (x*ballXDir);
 			ballY = ballY + (y*ballYDir);
 		}
 	}
 	
 	public boolean checkColision() {
 		
 		if ((ballX+(ballR*2) > Game.player2.playerX) ){
 			if (ballY+ballR < Game.player2.playerY+Game.player2.playerH){
 				if (ballY > Game.player2.playerY) {
 					return true;
 				}
 			}
 		}
 		
 		if ((ballX < Game.player1.playerX+Game.player1.playerW) ){
 			if (ballY+ballR < Game.player1.playerY+Game.player1.playerH){
 				if (ballY > Game.player1.playerY) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 	
 	public static void enableBall() {
 		ballMoving = true;
 	}
 	public static void disableBall() {
 		ballMoving = false;
 	}
 }
