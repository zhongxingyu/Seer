 // Ajay Jain
 // February 23, 2013
 // Game.java
 // Final game project
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import javax.sound.sampled.*;
 import java.net.URL;
 
 import java.io.*;
 import javax.imageio.ImageIO;
 
 import java.util.Random;
 
 public class Game {
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				(new SlimeRun()).init();
 			}
 		});
 	}
 }
 
 class SlimeRun {
 	private JFrame frame;
 	private StartScreen startPanel;
 	private GamePanel gamePanel;
 	private ImageLoader images;
 	private QuestionLoader questions;
 	private QuestionFrame qframe;
     
     private Clip guitarAudio, happyAudio;
 	
 	// Set up frame, show start screen
 	public void init() {
 		// Begin loading block images
 		images = new ImageLoader();
 		images.start();
 
 		// Begin loading questions
 		questions = new QuestionLoader();
 		questions.start();
 		
 		frame = new JFrame("Slime Run");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(64*15+10, 64*5);
 		frame.setLocation(100, 100);
 		frame.setResizable(false);
 		
 		// Create start screen
 		startPanel = new StartScreen();
 		// Attach a listener that fires on start button press
 		startPanel.addStartListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				startPanel.setVisible(false);
 				run();
                 // Play music
                if (happyAudio.isOpen())
                	happyAudio.stop();
                if (guitarAudio.isOpen())
                	guitarAudio.loop(Clip.LOOP_CONTINUOUSLY);
 			}
 		});
 		// Add start screen
 		frame.getContentPane().add(startPanel, BorderLayout.CENTER);
 		
 		frame.setVisible(true);
         
         // Load happy music
        URL url = getClass().getClassLoader().getResource("audio/happy.au");
         try {
             happyAudio = AudioSystem.getClip();
             happyAudio.open(AudioSystem.getAudioInputStream(url));
         } catch (Exception exc) {
             exc.printStackTrace(System.out);
         }
         // Play music
        if (happyAudio.isOpen())
	        happyAudio.loop(Clip.LOOP_CONTINUOUSLY);
         
         // Load guitar music
        URL guitarUrl = getClass().getClassLoader().getResource("audio/guitarStrum.au");
         try {
             guitarAudio = AudioSystem.getClip();
             guitarAudio.open(AudioSystem.getAudioInputStream(guitarUrl));
         } catch (Exception exc) {
             exc.printStackTrace(System.out);
         }
 	}
 	
 	// Launch actual game
 	private void run() {
 		gamePanel = new GamePanel();
 		frame.getContentPane().add(gamePanel);
 	}
 	
 	class GamePanel extends JPanel {	
 		private MainPanel main;
 		private JMenuBar top;
 		private JButton quit, resume;
 		private JLabel directions;
 		
 		private ActionListener quitListener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Hide panels
 				gamePanel.setVisible(false);
 				if (qframe != null)
 					qframe.setVisible(false);
 					
 				// Update & show start screen
 				startPanel.loadMissed();
 				startPanel.setVisible(true);
 				
 				// Stop and destroy the game
 				main.timer.stop();
 				main.player = null;
 				main = null;
 				gamePanel = null;
 			}
 		};
 		
 		public GamePanel() {
 			setLayout(new BorderLayout());
 			createTopBar();
 			
 			main = new MainPanel();
 			add(main, BorderLayout.CENTER);
 		}
 		
 		public void createTopBar() {
 			// Button to go to main screen
 			quit = new JButton("Quit");
 			quit.addActionListener(quitListener);
 			
 			// Game instructions
 			directions = new JLabel("   [W] or [UP] to jump, [S] or [DOWN] to crouch, [ESC] or [P] to pause or resume");
 			
 			// Add components to top panel
 			top = new JMenuBar();
 			top.setBackground(new Color(105, 204, 255, 150));
 			top.setPreferredSize(new Dimension(frame.getWidth(), 33));	// Block size 64 px
 			top.add(quit);
 			top.add(directions);
 			add(top, BorderLayout.NORTH);
 		}
 		
 		//class TopPanel extends JPanel implements MouseListener {}
 		
 		class MainPanel extends JPanel		 {
 			private byte[] map = new byte[15];
 			
 			private Image groundBlock;
 			private Image background;
 			private int w, h;
 			private boolean paused = false;
 
 			// The last question asked. Used in question saving and incorrect question display.
 			private String[] lastQuestion;
 
 			Slime player;	// The player
 			Timer timer;	// 4 milisecond game loop
 			
 			public MainPanel() {
 				super();
 
 				w = getWidth();
 				h = getHeight();
 				
 				player = new Slime();
 				// Set the difficulty based on the start panel JSlider
 				player.max_x = startPanel.getDifficulty();
 
 				// Set block for the ground
 				randomGround();
 				// Select a background
 				randomBack();
 				// Generate a map, skipping the first 4 columns
 				generateMap(4);
 				
 				timer = new Timer(4, new GameLoop());
 				timer.start();
 				// Start listening for key presses
 				//addKeyListener(this);
 				bindKeyStrokes();
 			}
 			
 			public void paintComponent(Graphics g) {
 				super.paintComponent(g);
 
 				// Draw background
 				g.drawImage(background, 0, 0, 64*16, 64*4, null);
 				// Draw slime character
 				player.drawSlime(g, this);
 				// Draw map
 				drawMap(g);
 				
 				if (paused) {
 					g.setFont(new Font("Sans-Serif", Font.BOLD, 40));
 					g.setColor(Color.red);
 					g.drawString("PAUSED", 40, 60);
 					g.setFont(new Font("Sans-Serif", Font.BOLD, 16));
 					g.drawString("Press [P] or [ESC]", 50, 80);
 				}
 			}
 
 			// Choose random block for ground
 			private void randomGround() {
 				switch (new Random().nextInt(4)) {
 					case 0:
 						groundBlock = images.darkGrassDirtBlock;
 						break;
 					case 1:
 						groundBlock = images.grassDirtBlock;
 						break;
 					case 2:
 						groundBlock = images.stoneBlock;
 						break;
 					case 3:
 						groundBlock = images.stonePathBlock;
 						break;
 				}
 			}
 
 			// Choose random background  image
 			private void randomBack() {
 				// Restore this after images have been optimized
 				switch (new Random().nextInt(3)) {
 					case 0:
 						background = images.swampBackground;
 						break;
 					case 1:
 						background = images.desertBackground;
 						break;
 					case 2:
 						background = images.starBackground;
 						break;
 				}
 				//background = images.starBackground;
 			}
 			
 			// Draws blocks
 			private void drawMap(Graphics g) {
 				// Draw floor
 				for (int col = 0; col < map.length; col++) {
 					g.setColor(Color.black);
 					g.drawImage(groundBlock, col*64, 3*64, 64, 64, null);
 					// g.drawImage(images.caveBlock, col*64, 0, 64, 64, this);
 					//g.drawRect(col*64, 3*64, 64, 64);
 					
 					switch (map[col]) {
 						case GameObject.BUMP:
 							// Draw rock
 							g.drawImage(images.rock, col*64, 2*64, 64, 64, null);
 							// Draw question mark
 							// g.setColor(Color.red);
 							// g.setFont(new Font("Sans-Serif", Font.BOLD, 40));
 							// g.drawString("?", col*64+11*2, 2*64+30*2);
 							break;
 						case GameObject.SPIKES:
 							// Draw spikes
 							g.drawImage(images.spikes, col*64, 2*64, 64, 64, null);
 							// Draw question mark
 							// g.setColor(Color.red);
 							// g.setFont(new Font("Sans-Serif", Font.BOLD, 40));
 							// g.drawString("?", col*64+11*2, 2*64+30*2);
 							break;
 						case GameObject.OVERHANG:
 							// Draw overhang
 							g.drawImage(images.roots, col*64, 0, 64, 146, null);
 							// Draw question mark
 							// g.setColor(Color.red);
 							// g.setFont(new Font("Sans-Serif", Font.BOLD, 40));
 							// g.drawString("?", col*64+22, 1*64+60);
 					}
 				}
 			}
 			
 			// Generate map for one screen.
 			// numberToSkip - Columns to skip
 			private void generateMap(int numberToSkip) {
 				// Clear map
 				for (int col = 0; col < map.length; col++)
 					map[col] = GameObject.AIR;
 				
 				for (int col = 0; col < map.length; col++) {
 					// Skip the first startCol columns
 					if (numberToSkip > 0) {
 						numberToSkip--;
 						continue;
 					}
 					
 					double rand = Math.random();
 					if (rand > .8) {
 						if (Math.random() > .5)
 							map[col] = GameObject.BUMP;
 						else
 							map[col] = GameObject.SPIKES;
 						// Skip a column so the user can actually jump over a bump
 						numberToSkip = 2;
 					} else if (rand > .6) {
 						map[col] = GameObject.OVERHANG;
 						numberToSkip = 2;
 					}
 				}
 			}
 
 			private void askQuestion() {
 				pauseGame();
				if (happyAudio.isOpen())
                	happyAudio.loop(Clip.LOOP_CONTINUOUSLY);
 				
 				if (qframe == null)
 					// Create JFrame for question and answers
 					qframe = new QuestionFrame();
 				
 				// Create listeners for when question is answered
 				IncorrectListener il = new IncorrectListener();
 				CorrectListener cl = new CorrectListener();
 				
 				// Default: Electricity and Magnetism
 				String[][] subject = questions.em;
 				
 				if (startPanel.subjects[1].isSelected())
 					subject = questions.machines;
 				else if (startPanel.subjects[2].isSelected())
 					subject = questions.anatomy;
 				else if (startPanel.subjects[3].isSelected())
 					subject = questions.vocab;
 				else if (startPanel.subjects[4].isSelected())
 					subject = questions.spanish;
 				
 				assembleQuestionFrame(subject, il, cl);
 			}
 			
 			private void assembleQuestionFrame(String[][] subject, ActionListener il, ActionListener cl) {
 				Random rand = new Random();
 				int correctQuestionIndex = rand.nextInt(subject.length);
 				String[] question = lastQuestion = subject[correctQuestionIndex];
 				// System.out.println(randIndex+" of "+subject.length);
 				// System.out.println("subject[randIndex] length "+subject[randIndex].length);
 				// System.out.println(java.util.Arrays.toString(question));
 				
 				// Init JButton array for each choice
 				JButton[] choices = new JButton[4];
 				// Randomly place the button for the correct choice in the choices array
 				int correctChoiceIndex = rand.nextInt(4);
 				// System.out.println(choices.length);
 				// System.out.println(question.length);
 				// System.out.println(subject.length);
 				choices[correctChoiceIndex] = new JButton("<html><p>"+question[1]+"</p></html>");
 				choices[correctChoiceIndex].addActionListener(cl);	// Add listener that is called on selection of the correct choice
 				for (int i = 0; i < 4; i++) {
 					if (i == correctChoiceIndex) continue;	// Skip the correct choice (already placed)
 					int randQuestionIndex = rand.nextInt(subject.length);	// Choose a random index
 					do {
 						String choice = subject[randQuestionIndex][1];	// Text of answer choice from the data set
 						choices[i] = new JButton("<html><p>"+choice+"</p></html>");
 						choices[i].addActionListener(il);	// Incorrect answer
 					} while (randQuestionIndex == correctQuestionIndex); // Don't place the correct choice twice
 				}
 				
 				qframe.askQuestion(question, choices);
 				qframe.setVisible(true);
 			}
 			
 			class IncorrectListener implements ActionListener {
 				private JFrame inframe;
 				private JSplitPane question;
 				private JTextArea qArea, aArea; // text areas for the question and answer
 				private JButton quit, resume;
 				private JLabel incorr;
 				
 				public IncorrectListener() {
 					inframe = new JFrame();
 					inframe.setTitle("GAME OVER");
 					inframe.setSize(300, 200);
 					inframe.setLocation(200, 480);
 					
 					// Top Label
 					incorr = new JLabel("INCORRECT");
 					incorr.setForeground(Color.red);
 					inframe.getContentPane().add(incorr, BorderLayout.NORTH);
 					
 					// Question boxes
 					qArea = new JTextArea();
 					qArea.setEditable(false);
 					qArea.setLineWrap(true);
 					qArea.setForeground(Color.blue);
 					
 					// Answer box
 					aArea = new JTextArea();
 					aArea.setEditable(false);
 					aArea.setLineWrap(true);
 					aArea.setForeground(Color.blue);
 					
 					// Add to split pane and frame
 					question = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, qArea, aArea);
 					inframe.getContentPane().add(question, BorderLayout.CENTER);
 					
 					// Quit button
 					quit = new JButton("Quit");
 					quit.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							inframe.setVisible(false);
 							quitListener.actionPerformed(e);
							if (happyAudio.isOpen())
                            	happyAudio.loop(Clip.LOOP_CONTINUOUSLY);
 						}
 					});
 
 					// Resume button
 					resume = new JButton("Continue");
 					resume.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							inframe.setVisible(false);
 							new CorrectListener().actionPerformed(e);
 						}
 					});
 
 					// Panel containing buttons
 					JPanel buttonPanel = new JPanel();
 					buttonPanel.setLayout(new GridLayout(1, 2));
 					buttonPanel.add(resume);
 					buttonPanel.add(quit);
 					inframe.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
 				}
 				
 				public void actionPerformed(ActionEvent e) {
 					System.out.println("INCORRECT");
 					qArea.setText(lastQuestion[0]);
 					aArea.setText(lastQuestion[1]);
 					inframe.setVisible(true);
 					new MissedIO().write(lastQuestion);
 				}
 			}
 			
 			class CorrectListener implements ActionListener {
 				public void actionPerformed(ActionEvent e) {
 					System.out.println("CORRECT");
 					// Hide question dialouge
 					qframe.setVisible(false);
 					// Free the player from the colliding block
 					map[player.getColumn()] = GameObject.AIR;
 					// Stop movement (player had hit an object)
 					player.vel_x = 0;
 					player.vel_y = 0;
 					if (player.isDucking())
 						player.stand();
 					// Repaint to hide the destroyed block
 					repaint();
 				}
 			}
 			
 			// Pause the game
 			private void pauseGame() {
 				paused = true;
 				repaint();
 				timer.stop();
				if (guitarAudio.isOpen())
                	guitarAudio.stop();
 			}
 			
 			private void resumeGame() {
 				timer.start();
 				paused = false;
				if (happyAudio.isOpen())
                	happyAudio.stop();
                if (guitarAudio.isOpen())
                	guitarAudio.loop(Clip.LOOP_CONTINUOUSLY);
 			}
 
 			// Create and attach actions to be executed on key strokes.
 			private void bindKeyStrokes() {
 				InputMap in = getInputMap();
 				ActionMap ac = getActionMap();
 				
 				// Jump
 				in.put(KeyStroke.getKeyStroke('w'), "jump");
 				in.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "jump");
 				ac.put("jump", new AbstractAction() {
 					public void actionPerformed(ActionEvent e) {
 						player.jump();
 					}
 				});
 
 				// Duck
 				in.put(KeyStroke.getKeyStroke('s'), "duck");
 				in.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "duck");
 				ac.put("duck", new AbstractAction() {
 					public void actionPerformed(ActionEvent e) {
 						player.duck();
 					}
 				});
 
 				// Stand
 				in.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "stand");
 				in.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "stand");
 				ac.put("stand", new AbstractAction() {
 					public void actionPerformed(ActionEvent e) {
 						player.stand();
 					}
 				});
 
 				// Pause
 				in.put(KeyStroke.getKeyStroke('p'), "pause");
 				in.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pause");
 				ac.put("pause", new AbstractAction() {
 					public void actionPerformed(ActionEvent e) {
 						if (paused)
 							resumeGame();
 						else
 							pauseGame();
 					}
 				});
 			}
 
 			class GameLoop implements ActionListener {
 				private int overlapCycles = 0;	// how long has the player been on top of an object?
 				
 				public void actionPerformed(ActionEvent e) {
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             main.repaint();
                             main.grabFocus();
                             
                             detectCollision();
                             
                             // Update map for next painting
                             player.move();
                             // If the player passed the edge of the screen
                             if (player.x >= 64*14) {
                                 randomGround();	// Swap ground block
                                 randomBack();	// Swap background image
                                 generateMap(3);	// Regenerate the map
                                 player.x = 0; // Move to start
                             }
                         }
                     });
 				}
 				
 				// Collision detection for player
 				private void detectCollision() {
 					int playerColumn = player.getColumn();
 					boolean isBump = map[playerColumn] == GameObject.BUMP ||
 											map[playerColumn] == GameObject.SPIKES;
 					if (isBump && player.y >= 64*2) {
 						if (overlapCycles > 1) {
 							askQuestion();
 							overlapCycles = 0;
 						} else
 							overlapCycles++;
 						System.out.println("Overlapping BUMP");
 					}
 					if (map[playerColumn] == GameObject.OVERHANG /*&& player.x - playerColumn*64 > 3*/ && player.y <= 64*2+18) {
 						player.stand();
 						if (overlapCycles > 3) {
 							askQuestion();
 							overlapCycles = 0;
 						} else
 							overlapCycles++;
 						System.out.println("Overlapping OVERHANG");
 					}
 				}
 			}
 		}
 	}
 }
