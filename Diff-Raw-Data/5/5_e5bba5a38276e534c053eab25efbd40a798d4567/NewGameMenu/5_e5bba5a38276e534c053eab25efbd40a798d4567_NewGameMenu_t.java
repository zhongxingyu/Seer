 package view;
 
 import controller.Controller;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 /**
  * @author Stephen Bos
  * July, 2013
  * 
  * This class is a simple JFrame menu that has controls to set the player types and the strategies used by
  * the computer players. When the "Begin" button is pressed, this frame closes itself and forwards the options
  * selected by the user to the controller to start a new game.
  */
 public class NewGameMenu extends JFrame{
 	
 	private static final long serialVersionUID = 1L;
 	
 	/*===================================
  	FIELDS
  	===================================*/
 	
 	private static final String[] Players = {"Human","Computer"};
 	private static final String[] Strategies = {"Move First", "Move Last", "Capture", "Cautious", "Random"};
 
 	private JComboBox[] playerSelectors;
 	private JComboBox[] strategySelectors;
 	private JLabel[] playerLabels;
 	private JButton cancelButton;
 	private JButton startButton;
 	private PlayerTypeListener playerTypeListener;
 	private CancelButtonListener cancelButtonListener;
 	private StartButtonListener startButtonListener;
 	private Controller controller;
 	
 	/*===================================
  	CONSTRUCTOR
  	===================================*/
 	
 	public NewGameMenu(Controller controller){
 		this.controller=controller;
 		
 		this.initializeComponents();
 		this.layoutContent();
 		
 		this.setVisible(false);
 		this.pack();
 	}
 	
 	/*===================================
 	 METHODS
 	 ===================================*/
 	
 	/**
 	 * Initializes all the components in this window.
 	 */
 	private void initializeComponents(){
 		// Initialize action listeners
 		this.playerTypeListener = new PlayerTypeListener();
 		this.cancelButtonListener = new CancelButtonListener();
 		this.startButtonListener = new StartButtonListener();
 		
 		// Initialize buttons and add their respective listeners
 		this.cancelButton = new JButton("Cancel");
 		this.startButton = new JButton("Begin");	
 		cancelButton.addActionListener(cancelButtonListener);
 		startButton.addActionListener(startButtonListener);
 		
 		// Initialize component arrays
 		this.playerLabels = new JLabel[4];
 		this.playerSelectors = new JComboBox[4];
 		this.strategySelectors = new JComboBox[4];
 		
 		// Load the component arrays and set the initial properties of the combo boxes
 		for(int i=0;i<4;i++){
 			playerLabels[i] = new JLabel("Player "+(i+1));
 			playerLabels[i].setForeground(Color.WHITE);
 			playerSelectors[i] = new JComboBox(Players);
 			playerSelectors[i].setSelectedIndex(1);
 			playerSelectors[i].addActionListener(this.playerTypeListener);
 			strategySelectors[i] = new JComboBox(Strategies);
 			strategySelectors[i].setEnabled(true);
 		}
 		
 		// Set the first pair of combo boxes to correspond to a human player.
 		playerSelectors[0].setSelectedIndex(0);
 		strategySelectors[0].setEnabled(false);
 	}
 	
 	/**
 	 * Lays out the content of this window in a grid format.
 	 */
 	private void layoutContent(){
 		
 		// Create headings for each column
 		JLabel heading1 = new JLabel("");
 		JLabel heading2 = new JLabel("Player Type:");
 		heading2.setForeground(Color.WHITE);
 		JLabel heading3 = new JLabel("Strategy:");
 		heading3.setForeground(Color.WHITE);
 		
 		// Create a panel to hold the content.
 		JPanel contentHolder = new JPanel(new GridLayout(6,3,5,5));
 		contentHolder.setBackground(Color.BLACK);
 		
 		// Add the headings to the panel.
 		contentHolder.add(heading1);
 		contentHolder.add(heading2);
 		contentHolder.add(heading3);
 		
 		// Add the combo boxes and their labels to the panel.
 		for(int i=0; i<4; i++){
 			contentHolder.add(playerLabels[i]);
 			contentHolder.add(playerSelectors[i]);
 			contentHolder.add(strategySelectors[i]);
 		}
 		
 		// Add the buttons to the panel.
 		contentHolder.add(Box.createGlue());
 		contentHolder.add(cancelButton);
 		contentHolder.add(startButton);
 		
 		// Add the panel to this window.
 		this.add(contentHolder);
 	}
 	
 	/**
 	 * This method is called when the "Begin" button is pressed. It stores the selected options in a 2D
 	 * array of strings and then forwards this information to the controller to start a new game.
 	 */
 	private void startGame(){
 		String[][] selectedOptions = new String[4][2];
 		for(int i=0; i<4; i++){
			int playerIndex=this.playerSelectors[i].getSelectedIndex();
			selectedOptions[i][0]=Players[playerIndex];
			selectedOptions[i][1]=playerIndex == 0?null:Strategies[this.strategySelectors[i].getSelectedIndex()];
 		}
 		
 		this.controller.requestNewGame(selectedOptions);
 	}
 
 	/*===================================
 	 ACTION LISTENERS
 	 ===================================*/
 	
 	private class StartButtonListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			NewGameMenu.this.setVisible(false);
 			NewGameMenu.this.startGame();
 		}
 	}
 	
 	private class CancelButtonListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			NewGameMenu.this.setVisible(false);
 		}
 	}
 	
 	private class PlayerTypeListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			JComboBox source = (JComboBox)e.getSource();
 			int index = java.util.Arrays.asList(NewGameMenu.this.playerSelectors).indexOf(source);
 			
 			if(source.getSelectedIndex()==0){
 				NewGameMenu.this.strategySelectors[index].setEnabled(false);
 			}
 			else{
 				NewGameMenu.this.strategySelectors[index].setEnabled(true);
 			}
 		}
 	}
 }
