 package com.jquirke.wit;
 import java.awt.Font;
 
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 
 public class TextBoxes {
 	
 	SqlQueries con = new SqlQueries();
 	Labels label = new Labels();
 	
 	
 	JTextField qty = new JTextField(80);
 	
 
 	
 	
 	public JComboBox getSalesRep(){	
 		
 		String names[] = {"Select rep","John Quirke","Mary Jones","Paddy Purcell","Joe Blogs"};
 		JComboBox combo = new JComboBox(names);
 		combo.setBounds(430, 60, 150, 20 );
 		combo.setSize(150,30);	
 		return combo;
 	}
 
 
 }// end TextBoxes
