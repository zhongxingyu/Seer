 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 
 import java.io.*;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import java.awt.*; 
 import java.awt.event.*; 
 import java.awt.geom.AffineTransform;
 
 public final class Player extends JComponent implements Runnable, FileLink {
 	Graphics2D g2d;
 	BufferedImage playerMove, playerMoveBuff;
 	
 	//player coordinates
 	volatile static int x,y;
 	static int absoluteX, absoluteY;
 	
 	//animation
 	static final int spriteGridX = 30*3;
 	static final int spriteGridY = 40*3;
 	
 	static boolean moveUp, moveRight, moveDown, moveLeft;
 	static double punchCounter = 1;
 	static boolean punchNow, block, loseLife;
 	static int lastDirection = 5;
 	static int newDirection;
 	static int moveStep = 0;
 	static double interStep = 0.1;
 	
 	//movement
 	static int dx, dy;
 	static final double SPEED = 1;
 	static final double SPEEDUP = 2;
 	double tmpSpeed = SPEED;
 	static boolean moveable = true;
 
 	//interaction
 	static Rectangle attackBound;
 	static int life = 3;
 	static int coins = 0;
 	
 	//worldmapnavigation
 	static Rectangle playerBoundN,playerBoundE,playerBoundS,playerBoundW;
 	
 	
 	public Player(){
 		System.err.println("->Player");
 		
 		try {
 			playerMove = ImageIO.read(player1_move);
 			playerMoveBuff = new BufferedImage(1260, 960, BufferedImage.TYPE_INT_ARGB);
 			} catch (IOException e) {
 				System.err.println("file not found");
 				System.exit(0);
 			}
 	}
 	
 	public void paintComponents(Graphics g){
 		g2d = (Graphics2D) g;
 		//System.out.println("Player.paintComponents");
 		g2d.drawImage(playerMoveBuff,Player.x,Player.y,this);
 	}
 	
 	public void run(){
 		if (Board.printMsg)
 			System.out.println("Player.run");
<<<<<<< HEAD
		
 		if (life <= 0)
 			Board.gameOver = true;
 		
		
		if (!block)
=======
 		if (!block || moveable)
>>>>>>> 0baa9836e2457425e5d85fb643f349be443b8344
 		move();
 		paintPlayer();
 		
 		if(punchCounter < 1){
 			punchCounter += 0.1;
 			System.out.println("punchCounter: "+punchCounter); 
 		}
 			
 	
 
 	}
 	
 	
 	private void move(){
 		dx=dy=0;
 		
 		if (moveLeft) dx = -1;
 		if (moveRight) dx = 1;
 		if (moveUp) dy = -1;
 		if (moveDown) dy = 1;
 		
 		
 		//System.out.println("X: " + x + ", Y: " + y);
 
 		double length = Math.sqrt(dx * dx + dy * dy);
 		
 		if (length != 0) {
 			
 		    dx *= tmpSpeed;
 		    dy *= tmpSpeed;
 
 		    //toogles movement sprites/animation
 		    if (dx != 0 || dy != 0){
 		    	if (moveStep >= 7 || newDirection != lastDirection || interStep > 8)
 		    		interStep = 0;
 		    		interStep += 0.08*tmpSpeed;
 		    		moveStep = Math.round((int)interStep);
 		    }
 		}
 		
 		
 		absoluteX += dx;
 		absoluteY += dy;
 		
 	
     	//fixed Camera
     	if(!Camera.cameraOn){
     		x += dx;
         	y += dy;
     	}
 		/*
     	System.out.println("Player: "+x+","+y);
 		System.out.println("Camera: "+Camera.cameraX+","+Camera.cameraY);
 		System.err.println("Absolut:"+absoluteX+","+absoluteY);
 		*/
 		//moveable Camera
 		if(Camera.cameraOn){
 			if(absoluteY < 2065 || absoluteY > 0){
 		    	Camera.cameraY = absoluteY;
 		    } 
 		    if(absoluteX < 1900|| absoluteX > 0){
 				Camera.cameraX = absoluteX;
 			} 
 		    Camera.alignCamera();
  		 }		
 
 		setBounds();
 		dx = dy = 0;
 	} //move
 	
 	
 	private void paintPlayer() {
 		
 		if ( moveUp == true &&  moveRight != true && moveDown != true &&moveLeft != true || lastDirection == 1){
 			if (newDirection != lastDirection || moveStep >= 8)
 				moveStep = 0;
 			playerMoveBuff = playerMove.getSubimage(moveStep*spriteGridX, 0*spriteGridY, spriteGridX, spriteGridY);
 			lastDirection = 1;
 			newDirection = lastDirection;
 		}
 		if ( moveUp != true &&  moveRight == true && moveDown != true &&moveLeft != true || lastDirection == 3){
 			if (newDirection != lastDirection || moveStep >= 8)
 				moveStep = 0;
 			playerMoveBuff = playerMove.getSubimage(moveStep*spriteGridX, 2*spriteGridY, spriteGridX, spriteGridY);
 			lastDirection = 3;
 			newDirection = lastDirection;
 		}					
 		if ( moveUp != true &&  moveRight != true && moveDown == true &&moveLeft != true|| lastDirection == 5){
 			if (newDirection != lastDirection || moveStep >= 8)
 				moveStep = 0;
 			playerMoveBuff = playerMove.getSubimage(moveStep*spriteGridX, 4*spriteGridY, spriteGridX, spriteGridY);
 			lastDirection = 5;
 			newDirection = lastDirection;
 		}		
 		if ( moveUp != true &&  moveRight != true && moveDown != true &&moveLeft == true || lastDirection == 7){
 			if (newDirection != lastDirection || moveStep >= 8)
 				moveStep = 0;
 			playerMoveBuff = playerMove.getSubimage(moveStep*spriteGridX, 6*spriteGridY, spriteGridX, spriteGridY);
 			lastDirection = 7;
 			newDirection = lastDirection;
 		}
 		
 		
 		if ( moveUp == true &&  moveRight == true && moveDown != true &&moveLeft != true || lastDirection == 2){
 			if (newDirection != lastDirection || moveStep >= 8)
 				moveStep = 0;
 			playerMoveBuff = playerMove.getSubimage(moveStep*spriteGridX, 1*spriteGridY, spriteGridX, spriteGridY);
 			lastDirection = 2;
 			newDirection = lastDirection;
 		}
 		if ( moveUp != true &&  moveRight == true && moveDown == true &&moveLeft != true || lastDirection == 4){
 			if (newDirection != lastDirection || moveStep >= 8)
 				moveStep = 0;
 			playerMoveBuff = playerMove.getSubimage(moveStep*spriteGridX, 3*spriteGridY, spriteGridX, spriteGridY);
 			lastDirection = 4;
 			newDirection = lastDirection;
 		}
 		if ( moveUp != true &&  moveRight != true && moveDown == true &&moveLeft == true|| lastDirection == 6){
 			if (newDirection != lastDirection || moveStep >= 8)
 				moveStep = 0;
 			playerMoveBuff = playerMove.getSubimage(moveStep*spriteGridX, 5*spriteGridY, spriteGridX, spriteGridY);
 			lastDirection = 6;
 			newDirection = lastDirection;
 		}
 		if ( moveUp == true &&  moveRight != true && moveDown != true &&moveLeft == true || lastDirection == 8){
 			if (newDirection != lastDirection || moveStep >= 8)
 				moveStep = 0;
 			playerMoveBuff = playerMove.getSubimage(moveStep*spriteGridX, 7*spriteGridY, spriteGridX, spriteGridY);
 			lastDirection = 8;
 			newDirection = lastDirection;
 		}
 		
 		if (punchNow && punchCounter >= 1){
 		
 			System.out.println("punchCounter: "+punchCounter); 
 			playerMoveBuff = playerMove.getSubimage((9+(int)punchCounter)*spriteGridX, (lastDirection-1)*spriteGridY, spriteGridX, spriteGridY);
 			if (punchCounter < 3)
 				punchCounter += 0.1;
 			if(punchCounter >= 3) {
 				punchCounter = -5;
 				punchNow = false;
 			}
 		}
 		
 		if (block) {
 			playerMoveBuff = playerMove.getSubimage(14*spriteGridX, 4*spriteGridY, spriteGridX, spriteGridY);
 		}
 		//System.out.println("X: " + x + ", Y: " + y);
 		//g2d.drawImage(playerMoveBuff ,x ,y , this);
 		
 		//playerBounds
 		
 
 	}
 	
 
 	public void keyPressed(KeyEvent e){
 		int key = e.getKeyCode();
 		Board.repaintNow = true;
 
 			if (key == KeyEvent.VK_UP && !block){
 				moveUp = true;
 			}
 			if (key == KeyEvent.VK_RIGHT && !block){
 				moveRight = true;
 			}
 			if (key == KeyEvent.VK_DOWN && !block){	
 				moveDown = true;
 			}
 			if (key == KeyEvent.VK_LEFT && !block){
 				moveLeft = true;
 			}
 			
 			if (key == KeyEvent.VK_D && !block){
 				punchNow = true;
 			}
 			if (key == KeyEvent.VK_S){	
 				block = true;
 			}
 
 			
 			
 			
 			if (key == KeyEvent.VK_SPACE){
 				Camera.moveFocus = true;
 			}
 			
 			if (key == KeyEvent.VK_X){
 				System.out.println("Player: "+x+","+y);
 				System.out.println("Camera: "+Camera.cameraX+","+Camera.cameraY);
 				System.err.println("Absolut:"+absoluteX+","+absoluteY);
 			}
 				
 			if (key == KeyEvent.VK_F){
 				tmpSpeed = SPEED*SPEEDUP;
 			}
 
 	}
 	
 	public void keyReleased(KeyEvent e){
 		int key = e.getKeyCode();
 		Board.repaintNow = true;
 		
 		if (key == KeyEvent.VK_UP){
 			moveUp = false;
 			moveStep = 0;
 		}
 		if (key == KeyEvent.VK_RIGHT){
 			moveRight = false;
 			moveStep = 0;
 		}
 		if (key == KeyEvent.VK_DOWN){
 			moveDown = false;
 			moveStep = 0;
 		}	
 		if (key == KeyEvent.VK_LEFT){
 			moveLeft = false;
 			moveStep = 0;
 		}
 		
 
 		if (key == KeyEvent.VK_D){
 			
 			if(punchCounter > 1){
 				punchNow = false;
 				punchCounter = -5;
 			}
 				
 		}
 		if (key == KeyEvent.VK_S){	
 			block = false;
 		}
 		
 		
 		if (key == KeyEvent.VK_F){
 			tmpSpeed = SPEED;
 		}
 		
 		if (key == KeyEvent.VK_C){
 			if(!Camera.cameraLock){
 				if(!Camera.cameraOn){
 					System.out.println("OverWorld Camera on");
 					Camera.toogleCamera(true);
 				} else {
 					System.out.println("OverWorld Camera off");
 					Camera.toogleCamera(false);
 				}
 			} else
 				System.err.println("Camera locked!");
 			
 			
 				
 		}
 		
 		if(key == KeyEvent.VK_Y){
 			Board.printMsg = !Board.printMsg;
 		}
 		
 		
 		if (key == KeyEvent.VK_M){
 			Board.menu = !Board.menu;
 			Board.menuThread = !Board.menuThread;
 			Board.ingameThread = !Board.ingameThread;
 			/*
 			//get game status
 			if(!Board.menu && Board.ingame)
 				System.out.println("Ingame");
 			if(Board.menu && Board.ingame)
 				System.out.println("IngameMenu");
 			if(Board.menu && !Board.ingame)
 				System.out.println("MainMenu");
 				*/
 		}
 		
 		if (key == KeyEvent.VK_N){
 			Board.ingame = !Board.ingame;
 			Board.menuThread = !Board.menuThread;
 			Board.ingameThread = !Board.ingameThread;
 			
 			/*
 			//get game status
 			if(!Board.menu && Board.ingame)
 				System.out.println("Ingame");
 			if(Board.menu && Board.ingame)
 				System.out.println("IngameMenu");
 			if(Board.menu && !Board.ingame)
 				System.out.println("MainMenu");
 				*/
 		}
 		
 		
 		if (key == KeyEvent.VK_B){
 			Board.paintBounds = !Board.paintBounds;
 		}
 		if (key == KeyEvent.VK_SPACE){
 			Camera.moveFocus = false;
 		}
 		
 	}
 	
 	public void setBounds(){
 	playerBoundS = new Rectangle (x+10,y+10,60,10);
 	playerBoundE = new Rectangle (x+10,y+10,10,90);
 	playerBoundN = new Rectangle (x+10,y+90,60,10);
 	playerBoundW = new Rectangle (x+60,y+10,10,90);
 	}
 	
 	public static void setAttackBounds(){
 		attackBound = new Rectangle(0,0,0,0);
 		
 		if(punchNow && punchCounter >= 1.3 && lastDirection == 1){
 			attackBound = new Rectangle(x+30,y,40,50);
 		}
 		if(punchNow && punchCounter >= 1.3 && lastDirection == 3){
 			attackBound = new Rectangle(x+50,y+10,40,50);
 		}
 		if(punchNow && punchCounter >= 1.3 && lastDirection == 5){
 			attackBound = new Rectangle(x,y+60,40,50);
 		}
 		if(punchNow && punchCounter >= 1.3 && lastDirection == 7){
 			attackBound = new Rectangle(x,y+10,40,50);
 		}
 		
 		if(punchNow && punchCounter >= 1.3 && lastDirection == 2){
 			attackBound = new Rectangle(x+40,y,40,70);
 			//AffineTransform attackBound = AffineTransform.getRotateInstance(Math.PI/4, attackBound.getCenterX(), attackBound.getCenterY());
 		}
 		if(punchNow && punchCounter >= 1.3 && lastDirection == 4){
 			attackBound = new Rectangle(x+40,y+40,40,70);
 		}
 		if(punchNow && punchCounter >= 1.3 && lastDirection == 6){
 			attackBound = new Rectangle(x,y+25,40,70);
 		}
 		if(punchNow && punchCounter >= 1.3 && lastDirection == 8){
 			attackBound = new Rectangle(x,y,40,70);
 		}
 	}
 	
 	public BufferedImage getImage(){
 		return playerMoveBuff;
 	}
 	
 }
