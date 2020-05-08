 package UI;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
 import java.util.Observable;
 import java.util.Random;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import com.sun.org.apache.bcel.internal.generic.GETSTATIC;
 
 import GameField.GameField;
 import MathematicalLogik.MultDivCheck;
 import MathematicalLogik.MultDivPlusMinusCheck;
 import MathematicalLogik.PlusMinusCheck;
 
 /** Board represents the Graphical Userinterface of the GameField.
  * Implements Observable so that an other graphical userinterface can get the updates of the board.
  * The class implements the creation of a new random number for playing Legaue of Numbers.
  * 
  * @author rana
  *
  */
 
 public class Board extends Observable{
 	public final static int PLUSMINUS = 1;
 	public final static int MULITDIV = 2;
 	public final static int PLUSMINUSMULTDIV = 3;
 	private Font handwritting;
 	
 	/**
 	 * 
 	 * @param panel A JPanel for the JButtons of the GameField
 	 * @param gameValue must be a static final int of the class. Determine which mathematical operations are allowed.
 	 * @param maxNumber specifies the maximum of the values on a Field (the Buttons). 
 	 */
 	
 	public Board( JPanel	panel, int gameValue, int maxNumber){
 		this.panel = panel;
 		this.gameValue = gameValue;
 		this.maxNumber = maxNumber;
 		
 		//Schriftart laden
 		try {
			Path Pat = Paths.get(Board.class.getResource("GrungeHandwriting.ttf").toString());
			String ParsedPat = Pat.toString();
			InputStream myStream = new BufferedInputStream(new FileInputStream(ParsedPat)); 	 
			//InputStream myStream = new BufferedInputStream(new FileInputStream("GrungeHandwriting.ttf"));
 			handwritting = Font.createFont(Font.TRUETYPE_FONT, myStream).deriveFont(Font.PLAIN, 30);
 			/*InputStream is = this.getClass().getResourceAsStream("GrungeHandwriting.ttf");
 			//File fontFile = new File(this.getClass().getResource("../../../Fonts/Handwritting.ttf").toURI());
 			this.handwritting = Font.createFont(Font.PLAIN, is);//.deriveFont(Font.PLAIN, 20f);*/
 		} catch (FontFormatException e) {
 			System.out.println("Probleme mit dem Lesen der Schriftart");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Probleme beim I/O");
 			e.printStackTrace();
 		} 
 		//Schriftart verwenden
 		panel.setFont(this.handwritting);
 		reset();
 		panel.setSize(model.getWidth()*50, model.getHeight()*50);
 		panel.setLayout(new GridLayout(model.getWidth(), model.getHeight()));
 	}
 	/** Creates a new GameField.
 	 * An 2-dimensional JButtonArray of the size 10*10 and fill it with the values of the GameField.
 	 * Every Button has an overwritten ActionListener which count how many buttons are clicked. If 3 Buttons are clicked, it will controll whether it is a valid game move for the gameMode (gameValue in constructor).
 	 * If the move is valid, it will create a new random number. 
 	 * This method notify the observers.
 	 */
 	public void reset(){
 		model = new GameField();
 		values = new JButton[3];
 		counterForValue = 0;
 		createNewNumber();
 		buttons = new JButton[model.getWidth()][model.getHeight()];
 		int i, j;
 		for (i=0; i < model.getWidth(); i++){
 			for (j=0; j < model.getHeight(); j++){
 				buttons[i][j] = new JButton(model.getValue(j, i) + "");
 				buttons[i][j].setSize(50, 50);
 				/* HowTo identify a Button: 
 				 * int Value / model.getWidth() = row
 				 * int Value % model.getWidth() = column 
 				 */
 				buttons[i][j].setActionCommand(i*model.getWidth() + j + ""); 
 				buttons[i][j].setFont(this.handwritting);
 				buttons[i][j].setBackground(Color.darkGray);
 				buttons[i][j].setForeground(Color.white);
 				panel.add(buttons[i][j]);
 				buttons[i][j].addActionListener(new ActionListener() {
 					
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						String s = e.getActionCommand();
 						int buttonid = Integer.parseInt(s);
 						int column = buttonid % getWidthGameField();
 						int row = buttonid / getWidthGameField();
 						enableButtons(row, column);
 						if (!setValue(buttons[row][column])){
 							/*JDialog dialog = new JDialog();
 							dialog.add(new JLabel("You have choosen three values :)"));
 							dialog.setSize(200, 200);
 							dialog.setVisible(true);*/
 							System.out.println("3 Values has been chosen");
 						}
 					
 						if (check){
 								int[] intValues = new int[3];
 								for (int i=0; i < 3; i++){
 									int buttonString = Integer.parseInt(values[i].getActionCommand());
 									column = buttonString % getWidthGameField();
 									row = buttonString / getWidthGameField();
 									intValues[i] = getValue(row, column);
 								}
 								boolean checked = false;
 								if (gameValue == PLUSMINUS){
 									if (PlusMinusCheck.check(intValues[0], intValues[1], intValues[2], actualSearchedNumber)){
 										validAnswer = true;
 										striking();
 										checked = true;
 									}
 								}
 								if (gameValue == MULITDIV){
 									if (MultDivCheck.check(intValues[0], intValues[1], intValues[2], actualSearchedNumber)){
 										validAnswer = true;
 										striking();
 										checked = true;
 									}
 								}
 								if (gameValue == PLUSMINUSMULTDIV){
 									if (MultDivPlusMinusCheck.check(intValues[0], intValues[1], intValues[2], actualSearchedNumber)){
 										validAnswer = true;
 										striking();
 										checked = true;
 									}
 								}
 								if (!checked){
 									validAnswer = false;
 								}
 								else{
 									createNewNumber();
 								}
 								
 								update();
 								setChanged();
 								notifyObservers(actualSearchedNumber);
 							}
 						}
 				});
 			}
 		}
 	}
 	/** Updates the GUI of the Board (Enabling of the Buttons)
 	 *  
 	 */
 	
 	public void update(){
 		for (int i=0; i < model.getWidth(); i++){
 			for (int j=0; j < model.getHeight(); j++){
 				if (model.getStriked(j,i)){
 					buttons[i][j].setEnabled(false);
 				}
 				else{
 					buttons[i][j].setEnabled(true);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Enable all buttons around the clicked button
 	 * @param x int value for the width
 	 * @param y int value for the height
 	 * 
 	 */
 	public void enableButtons(int x, int y){
 		for (int i=0; i < model.getWidth(); i++){
 			for (int j=0; j < model.getHeight(); j++){
 				buttons[i][j].setEnabled(false);
 				//Setting the Buttons around not enabled!
 				if (i == x-1 || i == x+1){ 
 					if ( j==y){
 						if (! model.getStriked(j, i) && !fieldAlreadyChoosen(buttons[i][j])){
 							buttons[i][j].setEnabled(true);
 						}
 					}
 				}
 				if (x == i){
 					if (j == y-1 || j == y+1){
 						if (! model.getStriked(j, i) && !fieldAlreadyChoosen(buttons[i][j])){
 							buttons[i][j].setEnabled(true);
 						}
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * 
 	 * @return the height of the GameField
 	 */
 	public int getHeightGameField(){
 		return model.getHeight();
 	}
 	/**
 	 * 
 	 * @return the width of the GameField
 	 */
 	public int getWidthGameField(){
 		return model.getWidth();
 	}
 	/**
 	 * 
 	 * @return the actual Number wich should be searched
 	 */
 	public int getActualSearchedNumber(){
 		return actualSearchedNumber;
 	}
 	/**
 	 * Create a new number by calling method randomNumber();
 	 */
 	public boolean getValidAnswer(){
 		return validAnswer;
 	}
 	
 	private void createNewNumber(){
 		System.out.println("New generated number " + randomNumber());
 	}
 	/**
 	 * 
 	 * @param i width
 	 * @param j height
 	 * @return the value of the model
 	 */
 	private int getValue(int i, int j){
 		return model.getValue(j, i);
 	}
 	/**
 	 * returns whether the field is striked
 	 * @param i width
 	 * @param j height
 	 */
 	private boolean getStriked(int i, int j){
 		return model.getStriked(j, i);
 	}
 	/**
 	 * Set the field striked.
 	 * @param i width
 	 * @param j height
 	 */
 	private void setStriked(int i, int j){
 		model.setStriked(i, j);
 	}
 	/**
 	 * Could change the value of a JButton
 	 * @param the button which should be checked
 	 * @return counterForValue < 2 -> true else false
 	 */
 	private boolean setValue(JButton value){
 		if (counterForValue < 2){
 			check = false;
 			values[counterForValue] = value;
 			counterForValue++;
 			return true;
 		}
 		if (counterForValue == 2){
 			values[2] = value;
 		}
 		counterForValue = 0;
 		check = true;
 		return false;
 	}
 	/**
 	 * Checks whether an JButton is choosen an second time
 	 * @param b JButton which should controlled
 	 * @return true if the JButton is already choosen else false.
 	 */
 	private boolean fieldAlreadyChoosen(JButton b){
 		for (int i=0; i < counterForValue; i++){
 			if (values[i].equals(b)){
 				return true;
 			}
 		}
 		return false;
 	}
 	/** Set the clicked values as striked
 	 * 
 	 */
 	private void striking(){
 		for (int i=0; i < 3; i++){
 			int buttonString = Integer.parseInt(values[i].getActionCommand());
 			int column = buttonString % getWidthGameField();
 			int row = buttonString / getWidthGameField();
 			setStriked(column, row);
 		}
 	}
 	/**
 	 * Creates a new random number. The maximum is the possible maximum number with the game config.
 	 * e.g. MaxNumber is ten and only plus and minus is allowed: 10 + 10 + 10 = 30.
 	 * @return the new random number, which can be build on the game Board.
 	 */
 	
 	private int randomNumber(){
 		Random r = new Random();
 		int ret = 0;
 		int max = 0;
 		if (gameValue == PLUSMINUS){
 			max = 3 * maxNumber;
 		}
 		if (gameValue == MULITDIV || gameValue == PLUSMINUSMULTDIV){
 			max = maxNumber * maxNumber * maxNumber;
 		}
 		ret = r.nextInt(max);
 		while (! controllRandomNumber(ret)){
 			ret = r.nextInt(max);
 		}
 		actualSearchedNumber = ret;
 		return ret;
 	}
 	/**
 	 * 
 	 * @param random an potential value for checking whether the value is valid for the game.
 	 * @return whether the random number can be computate on the build.
 	 */
 	private boolean controllRandomNumber(int random){
 		for (int i=0; i < model.getHeight(); i++){
 			for (int j=0; j < model.getWidth(); j++){
 				if (i+2 < model.getWidth()){
 					if (gameValue == PLUSMINUS){
 						
 						if (PlusMinusCheck.check(getValue(i,j),getValue(i+1,j),getValue(i+2,j), random)){
 							return true;
 						}
 					}
 					if (gameValue == MULITDIV){
 						if (MultDivCheck.check(getValue(i,j), getValue(i+1,j), getValue(i+2,j), random))
 							return true;
 					}
 					if (gameValue == PLUSMINUSMULTDIV){
 						if (MultDivPlusMinusCheck.check(getValue(i,j), getValue(i+1,j), getValue(i+2, j), random))
 							return true;
 					}
 				}
 				if (j+2 < model.getHeight()){
 					switch (gameValue){
 					case PLUSMINUS: if (PlusMinusCheck.check(getValue(i,j), getValue(i,j+1), getValue(i, j+2), random))
 						return true;
 						break;
 					case MULITDIV: if (MultDivCheck.check(getValue(i,j), getValue(i,j+1), getValue(i, j+2), random))
 						return true;
 						break;
 					case PLUSMINUSMULTDIV: if (MultDivPlusMinusCheck.check(getValue(i,j), getValue(i, j+1), getValue(i, j+2), random))
 						return true;
 						break;
 					}
 				}
 				if (j+1 < model.getHeight() && i+1 < model.getWidth()){
 					switch (gameValue){
 					case PLUSMINUS: if (PlusMinusCheck.check(getValue(i,j), getValue(i+1, j), getValue(i+1,j+1), random))
 						return true;
 						if (PlusMinusCheck.check(getValue(i,j), getValue(i, j+1), getValue(i+1, j), random))
 							return true;
 						break;
 					case MULITDIV: if (MultDivCheck.check(getValue(i,j), getValue(i+1, j), getValue(i+1, j+1), random))
 						return true;
 						if (MultDivCheck.check(getValue(i,j), getValue(i+1, j), getValue(i+1, j+1), random))
 							return true;
 						break;
 					case PLUSMINUSMULTDIV: if (MultDivPlusMinusCheck.check(getValue(i,j),getValue(i+1, j), getValue(i+1, j+1), random))
 							return true;
 						if (MultDivPlusMinusCheck.check(getValue(i,j), getValue(i+1,j), getValue(i+1, j+1), random))
 							return true;
 						break;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	private boolean validAnswer;
 	private boolean check; 
 	private int counterForValue;
 	private int gameValue;
 	private int maxNumber;
 	private int actualSearchedNumber;
 	private JButton[] values;
 	private JButton[][] buttons;
 	private GameField model;
 	private JPanel panel;
 }
