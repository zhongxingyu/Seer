 package hu.vmiklos.pacman;
 
 import java.util.Random;
 import javax.microedition.lcdui.Canvas;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Image;
 
 public class Game extends Canvas implements Runnable {
 	private int width;
 	private int height;
 	private int devWidth;
 	private int devHeight;
 	private float ratio;
 	private Graphics graphics;
 	private Image image;
 	private Random random = null;
 
 	// status machine
 	private boolean started = false;
 	private boolean paused = false; // if started, paused or resumed?
 	private boolean scared = false;
 	private boolean dying = false;
 
 	// rules
 	private final int maxghosts = 12;
 	private final int minscaredtime = 20;
 	private final int maxspeed = 6;
 	// valid speeds of ghosts
 	private final int validspeeds[] = { 1, 2, 3, 3, 4, 4 };
 	private final int xblocknum = 15;
 	private final int yblocknum = 13;
 
 	// defaults
 	private int ghostnum = 6;
 	private int currentspeed = 4;
 
 	// generated values
 	private int blocksize;
 
 	// status variables
 	private int pacsleft, score, deathcounter;
 	private int scaredcount, scaredtime;
 
 	// positions
 	private int[] dx, dy, ghostx, ghosty, ghostdx, ghostdy, ghostspeed;
 	private int pacmanx, pacmany, pacmandx, pacmandy, reqdx, reqdy;
 
 	// the maze
 	private final short leveldata[] = { 
 			19,26,26,22, 9,12,19,26,22, 9,12,19,26,26,22,
 			37,11,14,17,26,26,20,15,17,26,26,20,11,14,37,
 			17,26,26,20,11, 6,17,26,20, 3,14,17,26,26,20,
 			21, 3, 6,25,22, 5,21, 7,21, 5,19,28, 3, 6,21,
 			21, 9, 8,14,21,13,21, 5,21,13,21,11, 8,12,21,
 			25,18,26,18,24,18,28, 5,25,18,24,18,26,18,28,
 			6,21, 7,21, 7,21,11, 8,14,21, 7,21, 7,21,03,
 			19,24,26,24,26,16,26,18,26,16,26,24,26,24,22,
 			21, 3, 2, 2, 6,21,15,21,15,21, 3, 2, 2,06,21,
 			21, 9, 8, 8, 4,17,26, 8,26,20, 1, 8, 8,12,21,
 			17,26,26,22,13,21,11, 2,14,21,13,19,26,26,20,
 			37,11,14,17,26,24,22,13,19,24,26,20,11,14,37,
 			25,26,26,28, 3, 6,25,26,28, 3, 6,25,26,26,28
 	};
 	private short[] screendata;
 	private Font font;
 	private Pacman pacman;
 	private String hint;
 
 	public Game(Pacman pacman) {
 		hint = "App started!";
 		this.pacman = pacman;
 		screendata = new short[xblocknum*yblocknum];
 		ghostx = new int[maxghosts];
 		ghostdx = new int[maxghosts];
 		ghosty = new int[maxghosts];
 		ghostdy = new int[maxghosts];
 		ghostspeed = new int[maxghosts];
 		dx=new int[4];
 		dy=new int[4];
 		width = 360;
 		font = Font.getFont(Font.FONT_STATIC_TEXT);
 		height = 312;
 		devWidth = getWidth();
 		devHeight = getHeight();
 		init();
 		blocksize = 24;
 		ratio = (float)Math.min(devHeight-font.getHeight(), devWidth) / Math.max(height, width);
 	}
 
 	public void run() {
 		long  starttime;
 
 		initLevel();
 		while(true) {
 			starttime=System.currentTimeMillis();
 			try {
 				if (!paused)
 					repaint();
 				// 25fps -> wait at least 40ms if repaint() was faster
 				starttime += 40;
 				Thread.sleep(Math.max(0, starttime-System.currentTimeMillis()));
 			} catch(java.lang.InterruptedException ie) {
 				break;
 			}
 		}
 	}
 
 	public void start() {
 		hint = "Game started!";
 		started = true;
 		init();
 	}
 	
 	public void stop() {
 		hint = "Game ended.";
 		started = false;
 		initLevel();
 	}
 	
 	public void pause() {
 		hint = "Game paused.";
 		paused = true;
 		repaint();
 	}
 	
 	public void resume() {
 		hint = "Game resumed!";
 		paused = false;
 	}
 
 	protected void keyPressed(int key) {
 		if(started) {
 			if(key == Canvas.KEY_NUM4 || key == Canvas.LEFT || key == -3) {
 				reqdx = -1;
 				reqdy = 0;
 			} else if(key == Canvas.KEY_NUM6 || key == Canvas.RIGHT || key == -4) {
 				reqdx = 1;
 				reqdy = 0;
 			} else if(key == Canvas.KEY_NUM2 || key == Canvas.UP || key == -1) {
 				reqdx = 0;
 				reqdy = -1;
 			} else if(key == Canvas.KEY_NUM8 || key == Canvas.DOWN || key == -2) {
 				reqdx = 0;
 				reqdy = 1;
 			}
 		}
 	}
 	
 	protected void paint(Graphics g) {
 		int x, y;
 		short i = 0;
 
 		if (graphics == null && width > 0 && height > 0) {
 			image = Image.createImage(devWidth, devHeight);
 			graphics = image.getGraphics();
 		}
 		if(graphics == null || image == null) {
 			return;
 		}
 
 		graphics.setColor(255, 255, 255);
 		graphics.fillRect(0, 0, devWidth, devHeight);
 		// draw the maze
 		for(y = 0; y < blocksize*yblocknum; y += blocksize) {
 			for(x = 0; x < blocksize*xblocknum; x += blocksize) {
 				// borders
 				if(!scared)
 					graphics.setColor(0, 0, 0);
 				else
 					graphics.setColor(255, 0, 0);
 				/*  2
 				 * 1 4
 				 *  8
 				 */
 				if((screendata[i]&1) != 0)
 					graphics.drawLine(toPixel(x),toPixel(y),toPixel(x),toPixel(y+blocksize-1));
 				if((screendata[i]&2) != 0)
 					graphics.drawLine(toPixel(x),toPixel(y),toPixel(x+blocksize-1),toPixel(y));
 				if((screendata[i]&4) != 0)
 					graphics.drawLine(toPixel(x+blocksize),toPixel(y),toPixel(x+blocksize),toPixel(y+blocksize));
 				if ((screendata[i]&8) != 0)
 					graphics.drawLine(toPixel(x),toPixel(y+blocksize),toPixel(x+blocksize-1),toPixel(y+blocksize));
 				if ((screendata[i]&16) != 0) {
 					graphics.setColor(0, 0, 0);
 					graphics.fillRect(toPixel(x+blocksize/2),toPixel(y+blocksize/2),1,1);
 				}
 				if ((screendata[i]&32) != 0) {
 					graphics.setColor(0, 0, 255);
 					graphics.fillRect(toPixel(x+1),toPixel(y+1),toPixel(blocksize-1),toPixel(blocksize-1));
 				}
 				i++;
 			}
 		}
 		if(started) {
 			// play the game
 			if (dying) {
 				deathcounter--;
 				if((deathcounter%8)<4) {
 					graphics.setColor(255, 255, 255);
 					graphics.fillRect(toPixel(pacmanx+1), toPixel(pacmany+1), toPixel(blocksize-1), toPixel(blocksize-1));
 				}
 				else if((deathcounter%8)>=4) {
 					graphics.setColor(255, 255, 0);
 					graphics.fillRect(toPixel(pacmanx+1), toPixel(pacmany+1), toPixel(blocksize-1), toPixel(blocksize-1));
 				} if(deathcounter == 0) {
 					pacsleft--;
 					if(pacsleft == 0) {
 						started = false;
 						hint = "Game over!";
 						pacman.getCommandHandler().stop();
 					}
 					initLevel();
 				}
 			} else {
 				updateWalls();
 				// if we are not dying, we can move pacman
 				int     pos;
 				short   ch;
 
 				if (reqdx==-pacmandx && reqdy==-pacmandy) {
 					pacmandx=reqdx;
 					pacmandy=reqdy;
 				} if (pacmanx%blocksize==0 && pacmany%blocksize==0) {
 					pos=pacmanx/blocksize+xblocknum*(int)(pacmany/blocksize);
 					ch=screendata[pos];
 					if ((ch&16)!=0) {
 						screendata[pos]=(short)(ch&15);
 						score++;
 					} 
 					if ((ch&32)!=0) {
 						scared=true;
 						scaredcount=scaredtime;
 						screendata[pos]=(short)(ch&15);
 						score+=5;
 					}
 
 					if (reqdx!=0 || reqdy!=0) {
 						if (!( (reqdx==-1 && reqdy==0 && (ch&1)!=0) ||
 								(reqdx==1 && reqdy==0 && (ch&4)!=0) ||
 								(reqdx==0 && reqdy==-1 && (ch&2)!=0) ||
 								(reqdx==0 && reqdy==1 && (ch&8)!=0))) {
 							pacmandx=reqdx;
 							pacmandy=reqdy;
 						}
 					}
 
 					// check if we should stop pacman
 					if ( (pacmandx==-1 && pacmandy==0 && (ch&1)!=0) ||
 							(pacmandx==1 && pacmandy==0 && (ch&4)!=0) ||
 							(pacmandx==0 && pacmandy==-1 && (ch&2)!=0) ||
 							(pacmandx==0 && pacmandy==1 && (ch&8)!=0)) {
 						pacmandx=0;
 						pacmandy=0;
 					}
 				}
 				pacmanx=pacmanx+currentspeed*pacmandx;
 				pacmany=pacmany+currentspeed*pacmandy;
 				graphics.setColor(255, 255, 0);
 				graphics.fillRect(toPixel(pacmanx+1), toPixel(pacmany+1), toPixel(blocksize-1), toPixel(blocksize-1));
 				checkMaze();
 			}
 		} else {
 			// demo
 			updateWalls();
 		}
 		if(!dying) {
 			// if we're not dying, we should move the ghosts (demo or game)
 			int pos;
 			int count;
 			int j;
 
 			for (i=0; i<ghostnum; i++) {
 				if (ghostx[i]%blocksize==0 && ghosty[i]%blocksize==0) {
 					pos=ghostx[i]/blocksize+xblocknum*(int)(ghosty[i]/blocksize);
 					count=0;
 					// no direction by default
 					for(j=0;j<4;j++) {
 						dx[j]=0;
 						dy[j]=0;
 					}
 					if ((screendata[pos]&1)==0 && ghostdx[i]!=1) {
 						dx[count]=-1;
 						dy[count]=0;
 						count++;
 					}
 					if ((screendata[pos]&2)==0 && ghostdy[i]!=1) {
 						dx[count]=0;
 						dy[count]=-1;
 						count++;
 					}
 					if ((screendata[pos]&4)==0 && ghostdx[i]!=-1) {
 						dx[count]=1;
 						dy[count]=0;
 						count++;
 					}
 					if ((screendata[pos]&8)==0 && ghostdy[i]!=-1) {
 						dx[count]=0;
 						dy[count]=1;
 						count++;
 					}
 					if (count==0) {
 						if ((screendata[pos]&15)==15) {
 							ghostdx[i]=0;
 							ghostdy[i]=0;
 						} else {
 							ghostdx[i]=-ghostdx[i];
 							ghostdy[i]=-ghostdy[i];
 						}
 					} else {
 						if(random == null)
 							random = new Random();
 						while(true) {
 							// find a possible direction
 							count = Math.abs(random.nextInt()%4);
 							if(dx[count]==0 && dy[count]==0)
 								continue;
 							else
 								break;
 						}
 						ghostdx[i]=dx[count]; // random: 0-3
 						ghostdy[i]=dy[count];
 					}
 				}
 				ghostx[i]=ghostx[i]+(ghostdx[i]*ghostspeed[i]);
 				ghosty[i]=ghosty[i]+(ghostdy[i]*ghostspeed[i]);
 				graphics.setColor(255, 0, 0);
 				graphics.fillRect(toPixel(ghostx[i]+1), toPixel(ghosty[i]+1), toPixel(blocksize-1), toPixel(blocksize-1));
 
 				if (pacmanx>(ghostx[i]-(blocksize/2)) && pacmanx<(ghostx[i]+(blocksize/2)) &&
 						pacmany>(ghosty[i]-(blocksize/2)) && pacmany<(ghosty[i]+(blocksize/2)) && started) {
 					if (scared) {
 						score+=10;
 						ghostx[i]=7*blocksize;
 						ghosty[i]=6*blocksize;
 					} else {
 						dying=true;
 						deathcounter=64;
 					}
 				}
 			}
 		}
 		graphics.setColor(0, 0, 0);
 		int bottomPos = toPixel(height)+((devHeight-toPixel(height)-font.getHeight())/2);
 		graphics.drawString("Score: " + score, devWidth, bottomPos, Graphics.TOP | Graphics.RIGHT);
 		graphics.drawString(hint, 0, bottomPos, Graphics.TOP | Graphics.LEFT);
 		g.drawImage(image, 0, 0, 0);
 	}
 
 	/**
 	 * Converts our device independent pixels to real ones.
 	 * 
 	 * Note that the target canvas is smaller than the one of the device to allow space for the status bar.
 	 */
 	private int toPixel(float p) {
 		return (int)(p * ratio);
 	}
 
 	// starts a demo or a game
 	private void init() {
 		pacsleft = 3;
 		score = 0;
 		scaredtime = 120;
 		initLevel();
 		ghostnum = 6;
 		currentspeed = 4;
 		drawMaze();
 	}
 
 	// resets the state of the level (after a death, etc)
 	private void initLevel() {
 		short i;
 		int dx = 1;
 
 		for (i = 0; i < ghostnum; i++) {
 			ghostx[i] = 7 * blocksize;
 			ghosty[i] = 6 * blocksize;
 			ghostdx[i] = dx;
 			ghostdy[i] = 0;
 			dx = - dx;
 			ghostspeed[i] = validspeeds[currentspeed - 3];
 		}
 		pacmanx = 7 * blocksize;
 		pacmany = 9 * blocksize;
 		pacmandx = 0;
 		pacmandy = 0;
 		reqdx = 0;
 		reqdy = 0;
 		dying = false;
 		scared = false;
 	}
 
 	// draws the maze from scratch (after a death, after a completed level)
 	private void drawMaze() {
 		int i;
 
 		for (i = 0; i<xblocknum*yblocknum; i++) {
 			screendata[i]=leveldata[i];
 		}
 	}
 
 	// checks if this is the end of the game or not
 	private void checkMaze() {
 		short i=0;
 		boolean finished=true;
 
 		while (i < xblocknum * yblocknum && finished) {
 			if ((screendata[i]&48)!=0)
 				finished = false;
 			i++;
 		}
 		if (finished) {
 			score += 50;
 			try { 
 				Thread.sleep(3000);
 			} catch (InterruptedException e) {
 			}
 			if (ghostnum < maxghosts)
 				ghostnum++; 
 			if (currentspeed<maxspeed)
				currentspeed+=2;
 			scaredtime=scaredtime-20;
 			if (scaredtime<minscaredtime)
 				scaredtime=minscaredtime;
 			initLevel();
 			drawMaze();
 		}
 	}
 
 	// lock / unlock the ghosts
 	private void updateWalls() {
 		scaredcount--;
 		if (scaredcount<=0)
 			scared=false;
 
 		if (scared) {
 			screendata[6*xblocknum+6]=11;
 			screendata[6*xblocknum+8]=14;
 		} else {
 			screendata[6*xblocknum+6]=10;
 			screendata[6*xblocknum+8]=10;
 		}
 	}
 }
