 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
 
 public class Bomberman extends JFrame implements KeyListener {
     
 	//main players
     Board board;
     JLayeredPane layeredPane;
     Player playerMe;
     Player playerOpp;
     int bomb;
     Timer t;
     MyConnection con;
     String walls, bonuses;
 	
 	// TIMER
 	Timer timer;
 	JLayeredPane gameTimer = new JLayeredPane();
 	JTextField tf = new JTextField();
 	JLabel timerLabel; 
 	int count;
 	static Font font = new Font("Century Gothic", Font.PLAIN, 20);
     
     public Bomberman (MyConnection con, Player p, Player o, String walls, String bonuses) {
         
         this.con = con;
 		this.setSize(600,650);
         setTitle("Bomberman");
         Dimension boardSize = new Dimension(600, 650);
         layeredPane = new JLayeredPane();
         getContentPane().add(layeredPane);
         layeredPane.setPreferredSize(boardSize);
         addKeyListener(this);
         
         playerMe = p;
         playerOpp = o;
 		playerMe.bombCount = 1;
 		playerOpp.bombCount = 1;
 		playerMe.bombLen = 1;
 		playerOpp.bombLen = 1;
         this.walls = walls;
         this.bonuses = bonuses;
         board = new Board(walls, bonuses);
         layeredPane.add(board, JLayeredPane.DEFAULT_LAYER);
         
 		// Timer
 		timerLabel = new JLabel("Waiting for other player..", SwingConstants.CENTER);
 		timerLabel.setFont(font);
 		timerLabel.setBounds(100,600,400,50);
 		layeredPane.add(timerLabel);
 		
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		pack();
 		setResizable(true);
 		setLocationRelativeTo(null);
     }
     
 	public void startTime() {
 		con.sendMessage("/startT");
     }
     	
 	public void startT(){
 		int count = 120;
 		timerLabel.setText("Time left: " + count);
 			
 		TimeClass tc = new TimeClass(count);
 		timer = new Timer(1000, tc);
 		timer.start();
 	}
 
 	public class TimeClass implements ActionListener {
 		int counter;
 		int flag = 0;
 		
 		public TimeClass(int counter){
 			this.counter = counter;
 		}
 		
 		public void actionPerformed(ActionEvent tc){
 			counter--;
 			if( counter >= 1 ) timerLabel.setText("Time left: " + counter);
 			else{
 				timer.stop();
 				timerLabel.setText("TIME'S UP!");
 			}
 		}
 		
 	}
 
     public void startGame() {
 		con.sendMessage("/startG");
     }
 	
 	public void startG(){
 		this.playerMe.loc = board.addPlayer(this.playerMe, this.playerMe.startPos);
 		this.playerOpp.loc = board.addPlayer(this.playerOpp, this.playerOpp.startPos);
 		this.playerMe.x = this.playerMe.loc / 11;
 		this.playerMe.y = this.playerMe.loc % 11;
 		this.playerOpp.x = this.playerOpp.loc / 11;
 		this.playerOpp.y = this.playerOpp.loc % 11;
 	}
     final int LEFT = 0, RIGHT = 1, UP = 2, DOWN = 3;
 	public void keyPressed(KeyEvent e) {
         int key = e.getKeyCode();
 		board.square[playerMe.x][playerMe.y].remove(playerMe.piece);
 		int old = playerMe.loc;
 		if(!playerMe.dead) {
 			int bombCount = playerMe.bombCount;
 			int bombLen = playerMe.bombLen;
 			if (key == KeyEvent.VK_LEFT) {
 				if ( playerMe.move(board, LEFT) ) {
 					int x = playerMe.x;
 					int y = playerMe.y;
 					playerMe = new Player(playerMe.name, LEFT, x, y, bombCount, bombLen);
 					con.sendMessage("/playerMove " + playerMe.name + " " + x + " " + y + " " + LEFT);
 				}
 			}
 
 			if (key == KeyEvent.VK_RIGHT) {
 				if ( playerMe.move(board, RIGHT) ) {
 					int x = playerMe.x;
 					int y = playerMe.y;
 					playerMe = new Player(playerMe.name, RIGHT, x, y, bombCount, bombLen);
 					con.sendMessage("/playerMove " + playerMe.name + " " + x + " " + y + " " + RIGHT);
 				}
 			}
 
 			if (key == KeyEvent.VK_UP) {
 				if ( playerMe.move(board, UP) ) {
 					int x = playerMe.x;
 					int y = playerMe.y;
 					playerMe = new Player(playerMe.name, UP, x, y, bombCount, bombLen);
 					con.sendMessage("/playerMove " + playerMe.name + " " + x + " " + y + " " + UP);
 				}
 			}
 
 			if (key == KeyEvent.VK_DOWN) {
 				if ( playerMe.move(board, DOWN) ) {
 					int x = playerMe.x;
 					int y = playerMe.y;
 					playerMe = new Player(playerMe.name, DOWN, x, y, bombCount, bombLen);
 					con.sendMessage("/playerMove " + playerMe.name + " " + x + " " + y + " " + DOWN);
 				}
 			}
 			
 			if (key == KeyEvent.VK_SPACE) {
 				System.out.println("bombcount: " + playerMe.bombCount);
 				if(playerMe.bombCount > 0) {
 					System.out.println("pwede");
 					playerMe.bombCount--;
 					con.sendMessage("/playerBomb " + playerMe.name + " " + playerMe.x + " " + playerMe.y);
 				}
 			}
 		}
     }
 	
 	public void updateBoard() {
 		JPanel panel = board.square[playerMe.x][playerMe.y];
 		panel.add(playerMe.piece);
 		validate();
 		repaint();
 		if(panel.getBackground() == Color.red) {
 			JOptionPane.showMessageDialog(this, "GAME OVER! YOU DIED :(");
 			playerMe.piece.setVisible(false);
 		} else if (panel.getBackground() == Color.orange) {
 			playerMe.bombLen++;
 			panel.setBackground(Color.black);
 			panel.removeAll();
 			panel.add(playerMe.piece);
 		} else if (panel.getBackground() == Color.yellow) {
 			playerMe.bombCount++;
 			panel.setBackground(Color.black);
 			panel.removeAll();
 			panel.add(playerMe.piece);
 		}
 		validate();
 		repaint();
 	}
 	
 	public void updateOpponent(int oldLoc, int x, int y, int direction) {
 		switch (direction) {
 			case 0:	y++; break;
 			case 1:	y--; break;
 			case 2:	x++; break;
 			case 3:	x--; break;
 		}
 		
 		JPanel panel = board.square[x][y];
 		panel.remove(playerOpp.piece);
 		
 		boolean go = playerOpp.move(board, direction);
 		if(go) {
 			panel = board.square[playerOpp.x][playerOpp.y];
 			int bombCount = playerOpp.bombCount;
 			int bombLen = playerOpp.bombLen;
 			int nx = playerOpp.x;
 			int ny = playerOpp.y;
 			playerOpp = new Player(playerOpp.name, direction, nx, ny, bombCount, bombLen);
 			panel.add(playerOpp.piece);
 		}
 		validate();
 		repaint();
 		
 		if (panel.getBackground() == Color.orange) {
 			playerOpp.bombLen++;
 			panel.setBackground(Color.black);
 			panel.removeAll();
 			panel.add(playerOpp.piece);
 		} else if (panel.getBackground() == Color.yellow) {
 			playerOpp.bombCount++;
 			panel.setBackground(Color.black);
 			panel.removeAll();
 			panel.add(playerOpp.piece);
 		}
 		validate();
 		repaint();
 		
 	}
 	
 	public void updateBomb(int x, int y) {
 		updateBoard();
 		JPanel panel = board.square[x][y];
 		panel.setBackground(Color.white);
 		panel.add( new JLabel( new ImageIcon("data/bomb.png") ) );
 		validate();
 		repaint();
 	}
 	
     public void fire(int x, int y, int bombLen) {
         System.out.println("FIRE!");
 		JPanel p = board.square[x][y];
         p.removeAll();
         p.add(new JLabel(new ImageIcon("data/fire_mid.png")));
 		p.setBackground(Color.red);
 		JPanel side;
 		Color c;
 		final int[] DIRECTION = {-1, 1, -11, 11};
 		final String[] DIRECT = {"left", "right", "up", "down"};
 		final int LEFT = 0, RIGHT = 1, UP = 2, DOWN = 3;
 		
 		int fx = x, fy = y;
 		
 		for (int j = 0; j < 4; j++) {
 			int len = 0;
 			fx = x;
 			fy = y;
 			while (len <= bombLen) {
 				len++;
 				boolean go = false;
 				switch (j) {
 					case LEFT:
 						if (fy - 1 >= 0) {
 							c = board.square[fx][fy-1].getBackground();
 							if (c != Color.gray) {
 								fy--;
 								go = true;
 							}
 						}
 						break;
 					case RIGHT:
 						if (fy + 1 <= 10) {
 							c = board.square[fx][fy+1].getBackground();
 							if (c != Color.gray) {
 								fy++;
 								go = true;
 							}
 						}
 						break;
 					case UP:
 						if (fx - 1 >= 0) {
 							c = board.square[fx-1][fy].getBackground();
 							if (c != Color.gray) {
 								fx--;
 								go = true;
 							}
 						}
 						break;
 					case DOWN:
 						if (fx + 1 <= 10) {
 							c = board.square[fx+1][fy].getBackground();
 							if (c != Color.gray) {
 								fx++;
 								go = true;
 							}
 						}
 						break;
 				}
 				
 				if (go) {
 					side = board.square[fx][fy];
 					c = side.getBackground();
 					side.removeAll();
 					Color newcolor = Color.black;
 					
 					// LIMIT
 					if(len == bombLen) {
 						side.add( new JLabel( new ImageIcon("data/fire_" + DIRECT[j] + ".png") ) );
 						validate();
 						repaint();
 					}
 					else {
 						if(j <= 1) side.add( new JLabel( new ImageIcon("data/fire_horizontal.png") ) );
 						else side.add( new JLabel( new ImageIcon("data/fire_vertical.png") ) );
 						validate();
 						repaint();
 					}
 					
 					if (c == Color.black || c == Color.green) {
 						newcolor = Color.red;
 					}
 					else if (c == Color.blue) {
 						System.out.println("bleu");
 						int temp = (fx * 11) + fy;
 						if (bonuses.charAt(temp) == '1') {
 							newcolor = Color.orange;
 						} else if (bonuses.charAt(temp) == '2') {
 							newcolor = Color.yellow;
 						}
 					}
 					
 					side.setBackground(newcolor);
 					
 					
 					if (c != Color.black || len == bombLen) {
 						break;
 					}
 					
 
 				}
 				
 			}
 		}
 		
 		if(playerMe.dead) {
 			con.sendMessage("/dead "+playerMe.name);
 		}
     }
 	
 	public void removeBomb(int x, int y, int bombLen) {
 		JPanel panel = board.square[x][y];
 		panel.removeAll();
 		panel.setBackground(Color.black);
 		
 		JPanel side;
 		Color c;
 		int fx, fy;
 		for (int j = 0; j < 4; j++) {
 			int len = 0;
 			fx = x;
 			fy = y;
 			while (len <= bombLen) {
 				len++;
 				boolean go = false;
 				switch (j) {
 					case LEFT:
 						if (fy - 1 >= 0) {
 							c = board.square[fx][fy-1].getBackground();
 							if (c != Color.gray) {
 								fy--;
 								go = true;
 							}
 						}
 						break;
 					case RIGHT:
 						if (fy + 1 <= 10) {
 							c = board.square[fx][fy+1].getBackground();
 							if (c != Color.gray) {
 								fy++;
 								go = true;
 							}
 						}
 						break;
 					case UP:
 						if (fx - 1 >= 0) {
 							c = board.square[fx-1][fy].getBackground();
 							if (c != Color.gray) {
 								fx--;
 								go = true;
 							}
 						}
 						break;
 					case DOWN:
 						if (fx + 1 <= 10) {
 							c = board.square[fx+1][fy].getBackground();
 							if (c != Color.gray) {
 								fx++;
 								go = true;
 							}
 						}
 						break;
 				}
 				
 				if (go) {
 					side = board.square[fx][fy];
 					c = side.getBackground();
 					side.removeAll();
 					Color newcolor = Color.black;
 					
 					if (c == Color.red) {
 						newcolor = Color.black;
 						
 						side.setBackground(newcolor);
 					}
 					else if (c == Color.orange) {
 						side.add( new JLabel( new ImageIcon("data/star.gif") ) );
 					}
 					else if (c == Color.yellow) {
 						side.add( new JLabel( new ImageIcon("data/shroom.gif") ) );
 					}
 					
 					if (c != Color.red || c != Color.yellow || c != Color.orange) {
 						break;
 					}
					validate();
					repaint();
 				}
 			}
 		}
 	}
 
     public void keyReleased(KeyEvent e) {
         int key = e.getKeyCode();
 
         if (key == KeyEvent.VK_LEFT) {
 //             playerMe.dx = 0;
         }
 
         if (key == KeyEvent.VK_RIGHT) {
 //             playerMe.dx = 0;
         }
 
         if (key == KeyEvent.VK_UP) {
 //             playerMe.dy = 0;
         }
 
         if (key == KeyEvent.VK_DOWN) {
 //             playerMe.dy = 0;
         }
         
         if (key == KeyEvent.VK_SPACE) {
             
         }
     }
 
     @Override
     public void keyTyped(KeyEvent ke) {
     }
 	
 }
