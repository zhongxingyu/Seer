 package edu.ucsb.cs56.projects.utilities.TSA_Encryption;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Color.*;
 
 
 /**
 Cryptography GUI only main method
 
 @author Gary Mak, Rishabh Jha
 @version CS56 W14
 
 **/
 
 public class CryptographyGUI{
     JFrame frame;
     JTextField textField;
     JPanel panel;
     JPanel encryptedPanel;
     /**
        The encrypt function receives an array of integers and transforms it into a long value.
        @return a long with the resulting value
        @param numlist an int[] (list on integers) that gets iterated through throughout the function, the integers must be greater than or equal to 1 and less than or equal to 1000, and the length of the list must be greater than or equal to 2 and less than or equal to 50.
      */
     
	public long encrypt1(int[] numList) {
 	    long totalCheck = 0;
 	    long total = 1;
 	    
 	    for (int r=0; r < numList.length; r++){
 		if (numList[r] < 1 || numList[r] > 1000){
 		    System.out.println("Number Error; Integer x must follow: 1 <= x <= 1000");
 		    return totalCheck;
 		}
 	    }
 	    
 	    if (numList.length > 50 || numList.length < 2){
 		System.out.println("Number List Error; List length must be between 2 and 50 inclusive");
 		return totalCheck;
 	    }
 	    
 	    for (int i=0; i < numList.length; i++){
 		numList[i]++;
 		total = 1;
 		
 		for (int t=0; t < numList.length; t++){
 		    total = total * numList[t];
 		if (total > totalCheck){
 		    totalCheck = total;
 		}
 		}
 		numList[i]--;
 	    }
 	    
 	    return totalCheck;
 	}
 	    
 
 
     /**
        The encrypt function receives an array of integers and transforms it into a long value.
        @return a long with the resulting value
        @param numlist an int[] (list on integers) that gets iterated through throughout the function, the integers must be greater than or equal to -50 and less than or equal to 50, and the length of the list must be greater than or equal to 2 and less than or equal to 10.
      */
 	
	public long encrypt2(int[] numList){
 	    long totalCheck = 0;
 	    long total = 1;
 	    
 	    for (int r=0; r < numList.length; r++){
 		if (numList[r] < -50 || numList[r] > 50){
 		    System.out.println("Number Error; Integer x must follow: -50 <= x <= 50");
 		    return 0;
 		}
 	    }
 	    
 	    if (numList.length > 10){
 		System.out.println("Number List Error; List length must be less than 10 inclusive");
 		return 0;
 	    }
 	    
 	    
 
 	    for (int i = 0; i < numList.length; i++){
 		numList[i]++;
 		total *= numList[i];
 		numList[i]--;
 	    }
 
 	    return total;
 	    
 	}
 
     /**
        The encrypt function receives an array of integers and transforms it into a long value.
        @return a long with the resulting value
        @param numlist an int[] (list on integers) that gets iterated through throughout the function, the integers must be greater than or equal to -1000 and less than or equal to 1000, and the length of the list must be greater than or equal to 2 and less than or equal to 10.
      */
	public long encrypt3(int[] numList){
 	    long totalCheck = 0;
 	    long total = 1;
 
 	    for (int r=0; r < numList.length; r++){
 		if (numList[r] < 1 || numList[r] > 1000){
 		    System.out.println("Number Error; Integer x must follow: -1000 <= x <= 1000");
 		    return 0;
 	    }
 	    }
 	    
 	    if (numList.length > 10){
 		System.out.println("Number List Error; List length must be less than 10 inclusive");
 		return 0;
 	    }
 	    
 	    for (int i=0; i < numList.length; i++){
 		if (i%2 == 0) {
 		totalCheck += (numList[i] + i*i);
 		}
 		else {
 		    totalCheck -= (numList[i] + i*i);
 		}
 	    }
 	    
 	    return totalCheck;
 	}
 
 
 
 
 
 
 
 
 
 
     //all of our inner classes
 
     class Method1Listener implements ActionListener{
 	public void actionPerformed(ActionEvent event){
 	    
 	    panel.removeAll();
 	    panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
 
 	    JButton encrypt = new JButton("Encrypt");
 	    encrypt.addActionListener(new Encrypt1Listener());
 
 	    JPanel panel1 = new JPanel();
 	    panel1.add(textField);
 
 	    JPanel panel2 = new JPanel();
 	    panel2.add(encrypt);
 
 	    JPanel title = new JPanel();
 	    JLabel pageTitle = new JLabel("Input a list of integers to encrypt (separated by comma's)");
 	    title.add(pageTitle);
 
 	    JPanel length = new JPanel();
 	    JLabel lengthRestriction = new JLabel("Length of list between 2 and 50");
 	    length.add(lengthRestriction);
 
 	    panel.add(pageTitle);
 	    panel.add(lengthRestriction);
 	    panel.add(panel1);
 	    panel.add(panel2);
 	    panel.add(encryptedPanel);
 
 	    frame.getContentPane().removeAll();
 	    frame.getContentPane().add(panel);
 
 	    frame.invalidate();
 	    frame.validate();
 	}	
     }//end of crypt 1 inner class
 
     class Method2Listener implements ActionListener{
 	public void actionPerformed(ActionEvent event){
 	    
 	    panel.removeAll();
 	    panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
 
 	    JButton encrypt = new JButton("Encrypt");
 	    encrypt.addActionListener(new Encrypt2Listener());
 
 	    JPanel panel1 = new JPanel();
 	    panel1.add(textField);
 
 	    JPanel panel2 = new JPanel();
 	    panel2.add(encrypt);
 
 	    JPanel title = new JPanel();
 	    JLabel pageTitle = new JLabel("Input a list of integers to encrypt (separated by comma's)");
 	    title.add(pageTitle);
 
 	    JPanel length = new JPanel();
 	    JLabel lengthRestriction = new JLabel("Length of list between 2 and 50");
 	    length.add(lengthRestriction);
 
 	    panel.add(pageTitle);
 	    panel.add(lengthRestriction);
 	    panel.add(panel1);
 	    panel.add(panel2);
 	    panel.add(encryptedPanel);
 	    
 	    frame.getContentPane().removeAll();
 	    frame.getContentPane().add(panel);
 
 	    frame.invalidate();
 	    frame.validate();
 	}
     }//end of cryptography2 inner class  
 
     class Method3Listener implements ActionListener{
 	public void actionPerformed(ActionEvent event){
 	    
 	    panel.removeAll();
 	    panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
 
 	    JButton encrypt = new JButton("Encrypt");
 	    encrypt.addActionListener(new Encrypt3Listener());
 	    
 	    JPanel panel1 = new JPanel();
 	    panel1.add(textField);
 
 	    JPanel panel2 = new JPanel();
 	    panel2.add(encrypt);
 
 	    JPanel title = new JPanel();
 	    JLabel pageTitle = new JLabel("Input a list of integers to encrypt (separated by comma's)");
 	    title.add(pageTitle);
 
 	    JPanel length = new JPanel();
 	    JLabel lengthRestriction = new JLabel("Length of list between 2 and 50");
 	    length.add(lengthRestriction);
 
 	    panel.add(pageTitle);
 	    panel.add(lengthRestriction);
 	    panel.add(panel1);
 	    panel.add(panel2);
 	    panel.add(encryptedPanel);
 
 	    frame.getContentPane().removeAll();
 	    frame.getContentPane().add(panel);
 
 	    frame.invalidate();
 	    frame.validate();
 
 
 	}
     }//end of cryptography3 inner class
 
 
 
     //listeners for our second screen
 
     class Encrypt1Listener implements ActionListener{
 	public void actionPerformed(ActionEvent event){
 	    try{
 		String input = textField.getText();
 		int[] intArray = stringToIntArray(input);
 		long encryptedValue = encrypt1(intArray);
 
 		JLabel encryptedLabel = new JLabel(Long.toString(encryptedValue));
 		encryptedPanel.add(encryptedLabel);
 
 		panel.add(encryptedPanel);
 
 		frame.invalidate();
 		frame.validate();
 	    }
 	    catch (NumberFormatException ex) {
 		System.out.println("Error!");
 	    }
 	}
     }// end of Encrypt1Listener
     
     class Encrypt2Listener implements ActionListener{
 	public void actionPerformed(ActionEvent event){
 	    try{
 		String input = textField.getText();
 		int[] intArray = stringToIntArray(input);
 		long encryptedValue = encrypt2(intArray);
 
 		JLabel encryptedLabel = new JLabel(Long.toString(encryptedValue));
 		encryptedPanel.add(encryptedLabel);
 		
 		panel.add(encryptedPanel);
 
 		frame.invalidate();
 		frame.validate();
 	    }
 	    catch (NumberFormatException ex) {
 		System.out.println("Error!");
 	    }   
      	}	
     }// end of Encrypt2Listener
 
  class Encrypt3Listener implements ActionListener{
 	public void actionPerformed(ActionEvent event){
 	    try{
 		String input = textField.getText();
 		int[] intArray = stringToIntArray(input);
 		long encryptedValue = encrypt3(intArray);
 
 		JLabel encryptedLabel = new JLabel(Long.toString(encryptedValue));
 		encryptedPanel.add(encryptedLabel);
 
 		panel.add(encryptedPanel);
 
 		frame.invalidate();
 		frame.validate();
 	    }
 	    catch (NumberFormatException ex) {
 		System.out.println("Error!");
 	    }	    
      	}	
     }// end of Encrypt3Listener
 
 
 
 
 
     //reads input and puts it into an array to intput into encrypt methods
     public int[] stringToIntArray(String input){
 	System.out.println(input);
 
 	String[] StringArray = input.split(",");
 	for(String intString:StringArray)
 	    {
 		System.out.println(intString);
 	    }
 
 	int[] intArray = new int[StringArray.length];
 
 	int i = 0;
 	for(String intString:StringArray)
 	    {
 		System.out.println(intString);
 		 int x = Integer.parseInt(intString);
 		 intArray[i] = x;
 		 i++;
 	    }
 	return intArray;
     }// end of stringToIntArray
 		
 	    
 	
 	
 
     //go() makes our initial GUI while main calls go()
 
     public static void main(String[] args){
 	CryptographyGUI gui = new CryptographyGUI();
 	gui.go();
 
     }//end of main
 
 
 
     public void go(){
 	frame = new JFrame();
 	panel = new JPanel();
 
 	//for later use
 	textField = new JTextField(20);
 	encryptedPanel = new JPanel();
 
 	frame.setSize(640,480);
 	frame.setTitle("Encryption");
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	JPanel title = new JPanel();
 	JLabel pageTitle = new JLabel("Choose an encryption method");
 	title.add(pageTitle);
 	
 	JButton button1 = new JButton("Method 1");
 	button1.addActionListener(new Method1Listener());
 
 	JButton button2 = new JButton("Method 2");
 	button2.addActionListener(new Method2Listener());
 	
 	JButton button3 = new JButton("Method 3");
 	button3.addActionListener(new Method3Listener());
 	
 	JPanel panel1 = new JPanel();
 	JPanel panel2 = new JPanel();
 	JPanel panel3 = new JPanel();
 	
 	panel1.add(button1);
 	panel2.add(button2);
 	panel3.add(button3);
 	
 	panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
 	
 	panel.add(title);
 	panel.add(panel1);
 	panel.add(panel2);
 	panel.add(panel3);
 	
 	frame.getContentPane().add(panel);
 
 
 	frame.setVisible(true);
 	}//end of go
 
 
 
 
 
 
 
 }//end of class
