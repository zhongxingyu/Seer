 package project1.calculator;
 
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 
 /**
  * A class designed to run classes CalculatorButtonsV01, CalculatorFunctions, and CalculatorMenus.
  * @author
  *
  */
 public class CalculatorMainV01 {
 
 	/**
 	 * Method that materializes the calculator and its functions.
 	 * @param args not used.
 	 */
 	public static void main(String[] args) {
 		
 		JFrame frame = new JFrame("Calculator");
 		frame.setSize(new Dimension(454,460));
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		
 		CalculatorFunctions f = new CalculatorFunctions();
 		CalculatorButtonsV01 c = new CalculatorButtonsV01(f);
 		CalculatorMenus m = new CalculatorMenus();
 	
 		c.addGUIToFrame(frame);
 		m.addMenus(frame);
 		frame.setVisible(true);
 	}
 
 }
