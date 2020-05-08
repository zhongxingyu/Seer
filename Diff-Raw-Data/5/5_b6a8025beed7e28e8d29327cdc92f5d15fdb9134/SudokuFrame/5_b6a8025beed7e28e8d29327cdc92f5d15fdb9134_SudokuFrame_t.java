 import java.awt.*;
 import java.awt.event.*;
 import java.lang.Integer;
 import javax.swing.*;
 
 /**
  * 
  * @author Hayden, Laura, Jerome, Steven
  * some code copied from Core Java Fundamentals Book 1 Chapter 8, Event Handling p329/ Cay Hortsmann
  * @version 0.1
  */
 
 
 /**
  * A frame with a panel button
  */
 
 //NB: JFrame's default layoutManager is BorderLayout (so no need to setLayout unless we want to change it)
 class SudokuFrame extends JFrame
 {
 	public SudokuFrame(Board board)
 	{
 		setTitle("SudokuFrame");
 		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
 		
		currentValue = "-1";
 		
 		//button panel is created, which will be added to SudokuFrame
 		buttonPanel = new JPanel();
 		
 		//create buttons - the makebutton method adds the buttons automatically to buttonPanel 
 		for(int i = 0; i<9; i++)
 		{
 			for(int j = 0; j<9; j++){
 				String cellVal;
 				if (board.isVisibleCellValue(i,j))
 				{
 					cellVal = Integer.toString(board.getCellValue(i,j));
 				}
 				else
 				{
 					cellVal = "";
 				}
 				makeGridButton(cellVal, currentValue, buttonPanel);
 			}
 		}
 		
 		/*
 		 * buttons are set in a grid layout
 		 * NB: I have read that GridBagLayout is a slightly more complex but highly customizable layout manager (we can determine the size of each button individually)
 		 * - this can be looked into later
 		 */
 		buttonPanel.setLayout(new GridLayout(9,9));
 		
 		//creating a list of panels
 		
 		commandPanel = new JPanel();
 		commandPanel.setLayout(new FlowLayout());
 		makeMenuButton("NEW EASY", Color.PINK, commandPanel);
 		makeMenuButton("NEW MEDIUM", Color.CYAN, commandPanel);
 		makeMenuButton("NEW HARD", Color.RED, commandPanel);
 		makeMenuButton("Click for Hint", Color.BLUE, commandPanel);
 		makeMenuButton("Give up?", Color.GREEN, commandPanel);
 		
 		makeMenuButton("DRAFT", Color.PINK, commandPanel);
 		makeMenuButton("WTF", Color.CYAN, commandPanel);
 		
 		scorePanel = new JPanel();
 		scorePanel.setLayout(new FlowLayout());
 		scorePanel.setVisible(true);
 		
 		makeMenuButton("PAUSE", Color.BLACK, scorePanel);
 		makeMenuButton("TIME ME", Color.DARK_GRAY, scorePanel);
 		
 		
 		String keyLabels = "123456789";
 		int count = 0;
 		
 		for (int i = 0; i < keyLabels.length(); i++,count++)
 	      {
 	         final String label = keyLabels.substring(i, i + 1);
 	         JButton keyButton = new JButton(label);
 	         NumberSelect action = new NumberSelect(label);
 	         keyButton.addActionListener(action);
 	         scorePanel.add(keyButton);
 	      }
 		
 		//add or nest panels to frame
 		add(buttonPanel,BorderLayout.CENTER);
 		add(commandPanel,BorderLayout.SOUTH);
 		add(scorePanel,BorderLayout.NORTH);
 		
 	}
 	
 	/*
 	 * makeButton makes a JButton, which is then added to the buttonPanel, with an attached ActionListener
 	 * ActionListener should be detached to add different functionality to different buttons
 	 * in the future, add 1 more argument to represent a type of ActionListener
 	 */
 	public JButton makeGridButton(String name, String currentValue, JPanel panel)
 	{
 		JButton button = new JButton(name);
 		panel.add(button);
 		
 		NumberInsert insert = new NumberInsert(button);
 		button.addActionListener(insert);
 		return button;
 	}
 	
 	public JButton makeMenuButton(String name, Color backgroundColor, JPanel panel)
 	{
 		JButton button = new JButton(name);
 		panel.add(button);
 		NumberAction action = new NumberAction(backgroundColor);
 		button.addActionListener(action);
 		return button;
 	}
 	
 /**
  * An action listener that sets the panel's background color
  */
 	
 	private class NumberInsert implements ActionListener
 	{
 		public NumberInsert(JButton button)
 		{
 			//newValue = getCurrentValue();
 			b = button;
 		}
 		
 		public void actionPerformed(ActionEvent event)
 		{
			if(!getCurrentValue().equalsIgnoreCase("-1"))
 			b.setText(getCurrentValue());
 		}
 		private JButton b;
 		//private String newValue;
 	}
 	
 	private class NumberSelect implements ActionListener
 	{
 		public NumberSelect(String select)
 		{
 			newCurrentValue = select;
 		}
 		
 		public void actionPerformed(ActionEvent event)
 		{
 			currentValue = event.getActionCommand();
 		}
 		private String newCurrentValue;
 	}
 	
 	//Dummy actionListner to be modified / deleted
 	private class NumberAction implements ActionListener
 	{
 		public NumberAction(Color c)
 		{
 			backgroundColor = c;
 		}
 		
 		public void actionPerformed(ActionEvent event)
 		{
 			buttonPanel.setBackground(backgroundColor);
 		}
 		
 		private Color backgroundColor;
 	}
 	
 	public String getCurrentValue(){
 		return this.currentValue;
 	}
 	
 	private String currentValue;
 	//private int value;
 	private JPanel buttonPanel;
 	private JPanel hintPanel;
 	private JPanel commandPanel;
 	private JPanel modePanel;
 	private JPanel scorePanel;
 	
 	//you can play around with width and height as well - only affects initial window size, nothing more
 	public static final int DEFAULT_WIDTH = 900;
 	public static final int DEFAULT_HEIGHT = 700;
 }
 
 
 
 
 
