 package project1.calculator;
 
 import static org.junit.Assert.*;
 
 import javax.swing.JFrame;
 
 import org.junit.Test;
 
 public class CalculatorButtonsV01Test {
 
 	CalculatorFunctions f = new CalculatorFunctions();
 	CalculatorButtonsV01 c = new CalculatorButtonsV01(f);
 	JFrame frameTest = new JFrame();
 	
 	@Test
 	public void testCalcButtons() 
 	{
 		c.addGUIToFrame(frameTest);
 		
 		c.buttons[0].doClick();	
 		
 		assertTrue(
 				
 				c.calcFunctions.getFirstInput().equals("q")
 				
 			);
 		
 		c.buttons[16].doClick();
 		
 		
 		assertTrue(
 				
 				c.calcFunctions.getFirstInput().equals("0")
 				
 				);
 		
 		c.buttons[4].doClick();
 		c.buttons[7].doClick();
 		c.buttons[17].doClick(); // Equals button
 		
 		assertTrue(
 				
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 				-	47 ) < 1e-10
 				
 				);
 		
 		assertTrue(
 				
 				c.calcFunctions.getFirstInput().equals("q")
 				
 			);
 		
 		c.buttons[17].doClick(); // Equals button
 
 		assertTrue(
 				
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 				-	94 ) < 1e-10
 				);
 		
 		c.buttons[10].doClick(); // Division button
 		c.buttons[5].doClick(); 
 		c.buttons[17].doClick(); // Equals button
 		
 		assertTrue(
 				
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 						-	18.8 ) < 1e-10
 				);
 		
 		assertTrue(
 				
 				c.calcFunctions.getFirstInput().equals("q")
 				
 			);
 		
 		frameTest.dispose();
 		
 	}
 	
 	
 	@Test
 	public void testCalcButtons2()
 	{
 		
 
 		c.addGUIToFrame(frameTest);
 		assertTrue(
 				
 				c.calcFunctions.getLastPressedButton().equals("0")
 				
 				);
 		
 		c.buttons[10].doClick(); // Division button
 		c.buttons[5].doClick(); 
 		c.buttons[17].doClick(); // Equals button
 		
 		
 		assertTrue(
 				
 				c.calcFunctions.getLastPressedButton().equals("=")
 				&&	
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 					-	0 ) < 1e-10
 				);
 		
 		c.buttons[25].doClick(); // CE button
 		c.buttons[2].doClick(); // 2 button
 		c.buttons[8].doClick(); // 8 button
 		c.buttons[25].doClick(); // CE button
 		c.buttons[15].doClick();// minus button
 		c.buttons[25].doClick(); // CE button
 		
 		c.buttons[17].doClick(); // Equals button
 		
 		assertTrue(
 					
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 					-	0 ) < 1e-10
 				);
 		
 		c.buttons[15].doClick();// minus button
 		c.buttons[8].doClick(); // 8 button
 		c.buttons[25].doClick(); // CE button
 		c.buttons[7].doClick(); // 7 button
 		c.buttons[3].doClick(); // 3 button
 		c.buttons[5].doClick(); // 5 button
 		c.buttons[24].doClick(); // Backspace button
 		c.buttons[10].doClick(); // Division button
 		c.buttons[11].doClick(); // Multiplication button
 		c.buttons[17].doClick(); // Equals button
 		
 		assertTrue(
 				
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 					-	5329 ) < 1e-10
 				);
 		
 		c.buttons[10].doClick(); // Division button
 		c.buttons[8].doClick(); // 8 button
 		c.buttons[6].doClick(); // 6 button
 		c.buttons[25].doClick(); // CE button
 		c.buttons[8].doClick(); // 8 button
 		c.buttons[17].doClick(); // Equals button
 		
 		assertTrue(
 				
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 					-	666.125 ) < 1e-10
 				);
 		
 		c.buttons[11].doClick(); // Multiplication button
 		c.buttons[4].doClick(); // 4 button
 		c.buttons[15].doClick();// minus button
 		c.buttons[2].doClick(); // 2 button
 		c.buttons[0].doClick(); // 0 button
 		c.buttons[0].doClick(); // 0 button
 		c.buttons[0].doClick(); // 0 button
 		c.buttons[10].doClick(); // Division button
 		c.buttons[7].doClick(); // 7 buttons
 		c.buttons[15].doClick();// minus button
 		c.buttons[9].doClick(); // 9 button
 		c.buttons[0].doClick(); // 0 button
 		c.buttons[10].doClick(); // Division button
 		c.buttons[4].doClick(); // 4 button
 		c.buttons[15].doClick();// minus button
 		c.buttons[8].doClick(); // 8 button
 		c.buttons[17].doClick(); // Equals button
 		
 		assertTrue(
 				
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 					+	6.767857143 ) < 1e-6
 				);
 	
 		frameTest.dispose();
 	}
 	
 	@Test
 	public void testSqrt()
 	{
 		c.addGUIToFrame(frameTest);
 		
 		c.buttons[9].doClick(); // 9 button
 		c.buttons[14].doClick(); // sqrt button
 		c.buttons[16].doClick();// plus button
 		c.buttons[17].doClick(); // Equals button
 		
 		assertTrue(
 				
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 					-	6) < 1e-6
 				);
 		
 		c.buttons[11].doClick(); // Multiplication button
 		c.buttons[4].doClick(); // 4 button
 		c.buttons[15].doClick();// minus button
 		c.buttons[2].doClick(); // 2 button
 		c.buttons[0].doClick(); // 0 button
 		c.buttons[10].doClick(); // Division button
 		c.buttons[7].doClick(); // 7 buttons
 		c.buttons[14].doClick(); // sqrt button
 		c.buttons[15].doClick();// minus button
 		c.buttons[10].doClick(); // Division button
 		c.buttons[4].doClick(); // 4 button
 		c.buttons[15].doClick();// minus button
 		c.buttons[8].doClick(); // 8 button
 		c.buttons[17].doClick(); // Equals button
 		
 		assertTrue(
 				
 				Math.abs(Double.parseDouble(c.txtField.getText()) 
 					+	7.622035527 ) < 1e-6
 				);
 		
		c.buttons[26].doClick(); // Clear button
		c.buttons[3].doClick(); // 3 button
		c.buttons[16].doClick();// plus button
		c.buttons[6].doClick(); // 6 button
		c.buttons[17].doClick(); // Equals button.
		c.buttons[14].doClick(); // sqrt button
		c.buttons[17].doClick(); // Equals button.
		
		assertTrue(
				

				Math.abs(Double.parseDouble(c.txtField.getText()) 
					-	12) < 1e-6
				
				);
		
		c.buttons[17].doClick(); // Equals button.
		assertTrue(
				
				Math.abs(Double.parseDouble(c.txtField.getText()) 
					-	15) < 1e-6
				
				);
 		
 	}
 	
 	
 
 }
