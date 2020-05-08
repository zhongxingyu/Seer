package VariableInputApi;
 
 import java.awt.Component;
 import java.text.DecimalFormat;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.print.attribute.standard.OutputDeviceAssigned;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 /**
  * Creates a panel with JTextFields and JRadioButtons, based on a given number 
  * of objects. The user must first give an array of strings, to act as labels, 
  * then follow it with the individual objects that they want to change.
  * 
  * The new values can be accessed by using getUpdateVars()
  *
  * @author Karl Apsite
  * @version 9/30/2013
  */
 public class VarInputPanel extends JPanel{
 	private Object[] fields;
 	private Object[] copiedInput;
 	private Object[] output;
 	private int numPairs;
 	
 	/**
 	 * If the user sends in an array of strings and array of objects, the 
 	 * constructor will still function normally, however, this is a huge 
 	 * security flaw as this class will have access to the object[] variables 
 	 * the user gave.
 	 * 
 	 * @param l Detects an array of strings as the first argument
 	 * @param o Detects if the user specifically sent in an array of objects
 	 */
 	VarInputPanel(String[] l, Object[] o) {
 		/**
 		 * ERROR - We won't accept an array of objects... 
 		 * 		   this gives us too much power
 		 * 
 		 * As a side note, I'm not sure if A 'Security Exception' is the best 
 		 * option, but it sounded nice!
 		 */
 		throw new SecurityException("Can't accept an array of objects");
 	}
 	
 	
 	/**
 	 * If the user sent in individual objects, then this constructor will 
 	 * always be called.
 	 * 
 	 * @param vars[0] An array of strings
 	 * @param vars[1]... The objects to be changed
 	 */
 	VarInputPanel(Object... vars) {
 		super(new SpringLayout());
 		
 		if (!(vars[0] instanceof String[])) {
 			/**
 			 * ERROR - We need the first element to be an array 
 			 * 		   of Strings for labeling purposes
 			 */
 			throw new IllegalArgumentException("First element was " +
 											   "not of type String[]");
 		}
 		String[] labels = (String[]) vars[0]; 		
 		
 		numPairs = (labels.length<vars.length-1)? labels.length : vars.length-1; 
 		fields = new Object[numPairs];
 		copiedInput = vars;
 		
 		for (int i = 0; i < numPairs; i++) {
 			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
 			this.add(l);
 			
 			if (vars[i+1] instanceof Integer)
 				fields[i] = new JTextField("" + (Integer)vars[i+1], 4);
 			else if (vars[i+1] instanceof Double)
 				fields[i] = new JTextField("" + (Double)vars[i+1], 8);
 			else if (vars[i+1] instanceof String)
 				fields[i] = new JTextField("" + vars[i+1], 16);
 			else if (vars[i+1] instanceof Boolean)
 				fields[i] = new JRadioButton("", (Boolean)vars[i+1]);
 			else { 
 				//ERROR - You gave us a variable type that we didn't recognize!				
 				throw new IllegalArgumentException("Can only handle objects" +
 												   " that are of type Integer" +
 												   " and Boolean");
 			}
 			
 			l.setLabelFor((Component) fields[i]);
 			this.add((Component) fields[i]);
 		}
 
 		SpringUtilities.makeCompactGrid(this,
 				numPairs, 2, 	//rows, cols
 				6, 		  6,    //initX, initY		(in pixels)
 				6, 		  6);   //xSpace, ySpace	(in Pixels) 
 	}
 	
 	/**
 	 * This method will scan the Panel, and get each objects input.  After 
 	 * assessing what variable type is contained inside each element, the 
 	 * method will construct an array of objects containing each element.
 	 * 
 	 * @The updated variables from the Panel
 	 */
 	public Object[] getUpdatedVars() {
 		output = new Object[numPairs];
 
 		for (int i = 0; i < numPairs; i++) {
 			if (fields[i] instanceof JTextField) {
 				String str = ((JTextField)fields[i]).getText();
 				
 				if (isStringInteger(str)) {
 					output[i] = Integer.parseInt(str);
 				} else if (isStringDouble(str)) {
 					output[i] = Double.parseDouble(str);
 				} else {
 					output[i] = str;
 				}
 			}
 		    if (fields[i] instanceof JRadioButton) {
 		    	output[i] = ((JRadioButton)fields[i]).isSelected();
 		    }
 		}
 		
 		return output;
 	}
 	
 	/**
 	 * This method is sometimes useful to error check the users input.  
 	 * If the user constructs a panel with 3 ints and a boolean, then the user
 	 * might expect to receive 3 ints and a boolean.  If the program returns an
 	 * unexpected variable type, then the method returns false.
 	 * 
 	 * @return true if the outputs types match the expected inputs types.
 	 */
 	public Boolean doUpdatedVarsMatchInput() {
 		getUpdatedVars();
 		int l = copiedInput.length-1;
 		Boolean result = (l == output.length);
 		
 		for (int i=0; result && i<l; i++) {
 			result=result&&(copiedInput[i+1].getClass()==output[i].getClass());
 		}
 		return result;
 	}
 	
 	/**
 	 * Helper method for getUpdatedVars. Determines if a given string is 
 	 * actually an integer in disguise.  If it is, then true is returned.
 	 * Try/Catch blocks were not used for optimization purposes.
 	 * 
 	 * @param s input string
 	 * @return true if the given string is an integer.
 	 */
 	private boolean isStringInteger(String s) {
 		for (int i=0; i<s.length(); i++) {
 			int ch = s.charAt(i);
 			if ((i!=0 || ch!=45) && (ch<48 || ch>57)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Helper method for getUpdatedVars. Determines if a given string is 
 	 * actually an double in disguise.  If it is, then true is returned.
 	 * Try/Catch blocks were not used for optimization purposes.
 	 * 
 	 * @param s input string
 	 * @return true if the given string is an double.
 	 */
 	private boolean isStringDouble(String s) {
 		boolean decimalFlag = true;
 		for (int i=0; i<s.length(); i++) {
 			int ch = s.charAt(i);
 			if ((i!=0 || ch!=45) && (ch<48 || ch>57) && (ch==46 ^ decimalFlag))
 				return false;
 			decimalFlag = decimalFlag && (ch != 46);
 		}
 		return true;
 	}
 }
