 
 package com.lofibucket.yotris.ui.gui.components;
 
 import com.lofibucket.yotris.logic.GameState;
 import com.lofibucket.yotris.ui.CommandContainer;
 import com.lofibucket.yotris.ui.gui.action.HideWindowActionListener;
 import com.lofibucket.yotris.util.HighScores;
 import com.lofibucket.yotris.util.ScoreEntry;
 import com.lofibucket.yotris.util.Settings;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.util.List;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  * The score view that opens after game over, or when triggered from the menu.
  */
 
 public class ScoreWindow extends JFrame {
 	private CommandContainer container;
 	private Settings settings;
 	private HighScores scores;
 	private GameState state;
 
 	/**
 	 * The constructor used when the window is opened at game over.
 	 * @param container	the CommandContainer to use (UserInterface)
 	 * @param settings		current settings 
 	 * @param state 	the game state at the moment when the game ended
 	 */
 	public ScoreWindow(CommandContainer container, Settings settings, 
 			GameState state) {
 		super("Scores");
 
 		init(container, settings);
 
 		this.state = state;
 		scores = new HighScores();
 		checkScore();
 		
 		createComponents();
 		pack();
 	}
 
 	/**
 	 * The constructor used when opened from the menu. We don't try to update
 	 * the scores in this one.
 	 * @param container	the CommandContainer to use (UserInterface)
 	 * @param settings		current settings 
 	 */
 	ScoreWindow(CommandContainer container, Settings settings) {
 		super("Scores");
 		
 		init(container, settings);
 
 		scores = new HighScores();
 		createComponents();
 		pack();
 	}
 
 	private void init(CommandContainer container, Settings settings) {
 		this.container = container;
 		this.settings = settings;
 
 		setPreferredSize(new Dimension(400, 300));
 		setResizable(false);
 	}
 
 	private void checkScore() {
 		if (state == null) {
 			System.out.println("null");
 			return;
 		}
 
 		if (!scores.isHighScore(state.score)) {
 			return;
 		}
 
 		Object response = JOptionPane.showInputDialog("A new highscore! Please enter your name:");
 
 		if (response == null) {
 			return;
 		}
 
 		String name = (String)response;
 
 		scores.insertScoreEntry(name, state.score);
 	}
 
 	private void createComponents() {
 		JPanel pane = new JPanel();
 		this.add(pane);
 		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
 
 		JLabel title = new JLabel("Hall of Fame");
 		title.setFont(new Font("arial", Font.BOLD, 16));
 		pane.add(title);
 
 		List<ScoreEntry> top = scores.getTopTen();
 
 		for (int i=0;i<top.size();i++) {
 			ScoreEntry entry = top.get(i);
 			JLabel label = new JLabel((i+1) + ".  " + entry.getName() + 
 					": " + entry.getScore());
 			label.setAlignmentX(Component.CENTER_ALIGNMENT);
 			pane.add(label);
 		}
 		
 		JButton nappu = new JButton("Close");
 
 		title.setAlignmentX(Component.CENTER_ALIGNMENT);
 		nappu.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		pane.add(Box.createVerticalGlue());
 		pane.add(nappu);
 
 		nappu.addActionListener(new HideWindowActionListener(this));
 	}
 }
 
