 import java.awt.*;
 import java.awt.event.*;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 
 import javax.swing.*;
 
 
 public class MainMenu {
   	
 	private Sudoku sudoku;
   	private JLabel continueError;
   	private JButton playButton;
   	private JButton loadButton;
   	private JButton highScoreButton;
   	private JButton instructionButton;
   	private JButton exitButton;
 
 	public void startMainMenu(final BackgroundJFrame f) {
 		f.setBackgroundImage("image4.jpg");
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 		f.setLayout(gridbag);
 
 		c.fill = GridBagConstraints.BOTH;
 		
 		this.errorMessage(f, c);
 		this.playButton(f, c);
 		this.loadButton(f, c);
 		this.highScoresButton(f, c);
 		this.instructionButton(f, c);
 		this.exitButton(f, c);
 		
 		f.setLocationRelativeTo(null);
 		//f.pack();
 		f.setVisible(true);
 	}
       
 	private void playButton(final BackgroundJFrame f, final GridBagConstraints c) {
 		playButton = new JButton("New Game");
 		c.gridx = 1;
 		c.gridy = 10;
 		c.ipady = 10;
 		c.gridwidth = 1;
 		c.gridheight = 1;
		c.insets = new Insets(230,0,0,0);
 		f.add(playButton, c);
 		playButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					f.getContentPane().removeAll();
 					SwingUtilities.updateComponentTreeUI(f);
 					DifficultySelectMenu m = new DifficultySelectMenu();
 					m.startDifficultySelectMenu(f);	
 					
 				}
 		});
 	}
 
 	private void errorMessage(BackgroundJFrame f, GridBagConstraints c) {
 		Font font = new Font("Papyrus", Font.BOLD, 14);
 		continueError = new JLabel("No Previous Game");
 		continueError.setFont(font);
 		continueError.setForeground(Color.red);
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 3;
		c.insets = new Insets(0,0,20,0);
 		f.getContentPane().add(continueError, c);
 		continueError.setVisible(false);
 		SwingUtilities.updateComponentTreeUI(f);
 	}
 
 	private void loadButton(final BackgroundJFrame f, GridBagConstraints c) {
 		loadButton = new JButton("Continue Game");
 		c.gridx = 1;
 		c.gridy = 11;
 		c.insets = new Insets(10,0,0,0);
 		c.gridheight = 1;
 		f.add(loadButton, c);
 		loadButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					try {
 						FileInputStream fileStream = new FileInputStream("MyGame.ser");
 						ObjectInputStream os = new ObjectInputStream(fileStream);
 						Object one = os.readObject();
 						Object two = os.readObject();
 						Object three = os.readObject();
 						os.close();
 
 						sudoku = (Sudoku) one;
 						int hintsLeft = (int) two;
 						long saveTime = (long) three;
 
 						PlayMenu p = new PlayMenu(sudoku);
 						p.setHintsLeft(hintsLeft);
 						p.setStartTime(saveTime);
 
 						f.getContentPane().removeAll();
 						SwingUtilities.updateComponentTreeUI(f);
 
 						p.startPlayMenu(f);
 
 					} catch (ClassNotFoundException | IOException e) {
 						continueError.setVisible(true);
 					}
 
 				}
 		});
 	}
 
 	/**
 	 * Resets the high score by replacing the score with NOHIGHSCORE.
 	 * @param f
 	 * @param c
 	 */
 	private void highScoresButton (final BackgroundJFrame f, GridBagConstraints c) {
 		highScoreButton = new JButton("High Scores");
 		c.gridx = 1;
 		c.gridy = 12;
 		c.gridheight = 1;
 		f.add(highScoreButton, c);
 		highScoreButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 
 					f.getContentPane().removeAll();
 					SwingUtilities.updateComponentTreeUI(f);
 					HighScoreMenu p = new HighScoreMenu();
 					p.startHighScoreMenu(f);
 
 				}
 		});
 
 	}
 
 
 	private void instructionButton(final BackgroundJFrame f, GridBagConstraints c) {
 		instructionButton = new JButton("Instruction");
 		c.gridx = 1;
 		c.gridy = 13;
 		c.insets = new Insets(10,0,0,0);
 		c.gridheight = 1;
 		f.add(instructionButton, c);
 		instructionButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					f.getContentPane().removeAll();
 					SwingUtilities.updateComponentTreeUI(f);
 					InstructionsPage instructions = new InstructionsPage();
 					instructions.startInstructionsPage(f);
 				}
 		});
 	}
 
 	private void exitButton(final BackgroundJFrame f, GridBagConstraints c) {
 		exitButton = new JButton("Exit");
 		c.gridx = 1;
 		c.gridy = 14;
 		c.gridheight = 1;
 		
 		Font font = new Font("Avenir", Font.BOLD, 12);
 		exitButton.setFont(font);
 		
 		f.add(exitButton, c);
 		exitButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					System.exit(0);
 				}
 		});
 	}
 
 }
