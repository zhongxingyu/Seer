 /*
  * Copyright 2008 Sven Strickroth <email@cs-ware.de>
  * 
  * This file is part of JBubbleBreaker.
  * 
  * JBubbleBreaker is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as     
  * published by the Free Software Foundation.                            
  * 
  * JBubbleBreaker is distributed in the hope that it will be useful,     
  * but WITHOUT ANY WARRANTY; without even the implied warranty of        
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         
  * GNU General Public License for more details.                          
  * 
  * You should have received a copy of the GNU General Public License     
  * along with JBubbleBreaker. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jbubblebreaker;
 
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  * Provides the JBubbleBreaker core 
  * @author Sven Strickroth
  */
 public abstract class Game extends MouseAdapter {
 	/**
 	 * Stores the number of marked Bubbles
 	 */
 	protected int marked = 0;
 	/**
 	 * Stores the JPanel with the Bubble-matrix
 	 */
 	protected JPanel playgroundPanel = new JPanel();
 	/**
 	 * Stores a JLabel where the possible points are shown
 	 */
 	private JLabel possiblePoints = new JLabel("0");
 	/**
 	 * Stores the playground, the matrix of Bubbles
 	 */
 	protected Playground playground;
 	/**
 	 * Stores a reference of the points label in the GUI, instantiate on creation with a JPanel to avoid null-PounterExceptions
 	 */
 	private JLabel pointsLabel = new JLabel();
 	/**
 	 * Stores the points the user gained
 	 */
 	private int points = 0;
 	
 	/**
 	 * Prepares a new playground
 	 * @param rows of the matrix
 	 * @param cols of the matrix
 
 	 */
 	public Game(int rows, int cols) {
 		playgroundPanel.setLayout(null);
 		
 		playground = new Playground(playgroundPanel.getWidth(), playgroundPanel.getHeight(), rows, cols, this);
 
 		possiblePoints.setSize(50,50);
 		playgroundPanel.add(possiblePoints);
 		possiblePoints.setVisible(false);
 		
 		newGame();
 		
 		playgroundPanel.addComponentListener(new ComponentAdapter() {
 			public void componentResized(ComponentEvent arg0) {
 				playground.resized(playgroundPanel.getWidth(), playgroundPanel.getHeight());
 				playgroundPanel.repaint();
 			}
 		});
 		playgroundPanel.addMouseListener(this);
 	}
 	
 	/**
 	 * sets the label where to show the points
 	 * @param pointsLabel reference to the points Label in the GUI
 	 */
 	final public void setPointsLabel(JLabel pointsLabel) {
 		this.pointsLabel = pointsLabel;
 		pointsLabel.setText("Points: "+ points);
 	}
 	
 	/**
 	 * Creates a new game.
 	 * Resets all values and creates a new Bubble matrix.
 	 */
 	final public void newGame() {
 		points = 0;
 		marked = 0;
 		possiblePoints.setVisible(false);
 		
 		// remove all Bubbles from playgroundPanel 
 		int row = playground.getRows() - 1;
 		int col = playground.getCols() - 1;
 		while(col >= 0 && playground.isEmpty(row,col) == false) {
 			while(row >= 0 && playground.isEmpty(row, col) == false) {
 				removeBubble(row, col);
 				row--;
 			}
 			col--;
 			row = playground.getRows() - 1;
 		}
 		fillPlayground();
 		playgroundPanel.repaint();
 		playgroundPanel.setEnabled(true);
 		if (isSolveable() == false) {
 			gameOver();
 		}
 	}
 
 	/**
 	 * fills the playground/matrix with Bubbles
 	 */
 	protected void fillPlayground() { }
 	
 	/**
 	 * Returns the JPanel with the Bubbles inside
 	 * @return playground-JPanel
 	 */
 	final public JPanel getPanel() {
 		return playgroundPanel;
 	}
 
 	/**
 	 * Find (reculsively) all Bubbles with the same color-index which are next to each other 
 	 * @param row row-index to start the search
 	 * @param col column-index to start the search
 	 */
 	protected void findsame(int row, int col) {}
 
 	/**
 	 * This method is called, when game is not solveable any more 
 	 */
 	private void gameOver() {
 		playgroundPanel.setEnabled(false);
 		JOptionPane.showMessageDialog(null, "Game over. Points: "+ getPoints(), "JBubbleBreaker", JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	/**
 	 * Checks if the current game is solveable
 	 * @return playground solveable?
 	 */
 	protected boolean isSolveable() { return false; }
 
 	/**
 	 * Remove all marked Bubbles
 	 * @param row row-index of a marked Bubble, unused atm
 	 * @param col column-index of a marked Bubble, unused atm
 	 */
 	protected void removeMarkedBubbles(int row, int col) {};
 
 	/**
 	 * Removes a special Bubble
 	 * @param row row-index
 	 * @param col col-index
 	 */
 	final protected void removeBubble(int row, int col) {
		playgroundPanel.remove(playground.getBubble(row,col));
		playground.removeBubble(row,col);
 	}
 	
 	/**
 	 * Returns the game-Mode
 	 * @return the gameMode name 
 	 */
 	public String getMode() { return "overwrite me!"; };
 	
 	/**
 	 * Unmarks all Bubbles
 	 */
 	private void unmarkAll() {
 		marked=0;
 		for(int i=0; i < playground.getRows(); i++) {
 			for(int j=0; j < playground.getCols(); j++) {
 				playground.unMark(i, j);
 			}
 		}
 		possiblePoints.setVisible(false);
 	}
 
 	@Override
 	final public void mouseClicked(MouseEvent arg0) {
 		pointsLabel.setText("Points: "+ points);
 		if (arg0.getSource() == playgroundPanel || playgroundPanel.isEnabled() == false) {
 			unmarkAll();
 			return;
 		}
 		Bubble my = (Bubble)(arg0.getSource());
 		if (my == null) { return; }
 		//System.out.println(my.getRow() +"x"+ my.getCol());
 		if (marked != 0) {
 			if (my.isMarked() == false) {
 				unmarkAll();
 			} else {
 				points += getCalculatedPoints();
 				removeMarkedBubbles(my.getRow(),my.getCol());
 				pointsLabel.setText("Points: "+ points);
 				playgroundPanel.repaint();
 				marked=0;
 				possiblePoints.setVisible(false);
 				if (isSolveable() == false) {
 					gameOver();
 				}
 			}
 			return;
 		}
 		marked=0;
 		findsame(my.getRow(),my.getCol());
 		possiblePoints.setText(getCalculatedPoints().toString());
 		possiblePoints.setLocation((int)my.getLocation().getX()+10,(int)my.getLocation().getY()-12);
 		possiblePoints.setVisible(true);
 		if (marked==1) {
 			unmarkAll();
 		}
 	}
 
 	/**
 	 * Calculates the points
 	 * @return calculated points
 	 */
 	protected Integer getCalculatedPoints() { return 0;	}
 
 	/**
 	 * Return the points of the user in the current game
 	 * @return the points
 	 */
 	final public int getPoints() {
 		return points;
 	}
 }
