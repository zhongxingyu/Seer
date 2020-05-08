 package com.stevengharms.javacard;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class JavaCardView{
 	private JButton button_add 		= new JButton("Add");
 	private JButton button_del 		= new JButton("Delete");	
 
	private JTextArea jtext_ques	= new JTextArea("Question:");
	private JTextArea jtext_answer 	= new JTextArea("Answer:");
 
 	private JButton button_back 	= new JButton("<");
     private JButton button_right 	= new JButton("Right!");
     private JButton button_wrong 	= new JButton("Wrong!");
 	private JButton button_forward 	= new JButton(">");
     private JButton button_exit 	= new JButton("Exit");
 
 	/* Button Listeners */
 	
 	class ButtonBackListener implements ActionListener {
 		public void actionPerformed (ActionEvent e){
			System.out.println("I got clicked!");
 		}
 	}
 
 	class ButtonRightListener implements ActionListener {
 		public void actionPerformed (ActionEvent e){
 			System.out.println("I got clicked!");
 		}
 	}
 
 	class ButtonWrongListener implements ActionListener {
 		public void actionPerformed (ActionEvent e){
 			System.out.println("I got clicked!");
 		}
 	}
 
 	class ButtonForwardListener implements ActionListener {
 		public void actionPerformed (ActionEvent e){
 			System.out.println("I got clicked!");
 		}
 	}
 
 	class ButtonExitListener implements ActionListener {
 		public void actionPerformed (ActionEvent e){
 			System.out.println("I got clicked!");
 		}
 	}
 
 	class ButtonAddListener implements ActionListener {
 		public void actionPerformed (ActionEvent e){
 			System.out.println("I got clicked!");
 		}
 	}
 
 	class ButtonDelListener implements ActionListener {
 		public void actionPerformed (ActionEvent e){
 			System.out.println("I got clicked!");
 		}
 	}
 
 
 	
 	public JavaCardView(){
 		// Top level container
 		JFrame frame = new JFrame();
 		frame.setSize(500,500);
 		
 		// East container
 		JPanel eastButtonPanel = new JPanel(new GridLayout(2,1));
 		eastButtonPanel.setSize(20,20);
 	    configureButtons(
 	      eastButtonPanel, new JButton[]{button_add,button_del} );
 		
 		
 		// West container
 		JPanel westTextPanel = new JPanel(new GridLayout(2,1) );
 		westTextPanel.setSize(200,200);
 		configureTextAreas(
 			westTextPanel, new JTextArea[]{jtext_ques, jtext_answer});
 			
 		//South container
 		JPanel southNavigationPanel = new JPanel();
 		configureButtons(
 		  southNavigationPanel, new JButton[]{button_back, button_right, button_wrong, button_forward, button_exit});
 		
 		// Attach the components 
 		frame.getContentPane().add(BorderLayout.EAST, eastButtonPanel);
 		frame.getContentPane().add(BorderLayout.CENTER, westTextPanel);
 		frame.getContentPane().add(BorderLayout.SOUTH, southNavigationPanel);
 
 		// Assign listeners
 		button_add.addActionListener(new ButtonAddListener());
 		button_del.addActionListener(new ButtonDelListener());
 		
 		button_back.addActionListener(new ButtonBackListener());
 		button_right.addActionListener(new ButtonRightListener());
 		button_wrong.addActionListener(new ButtonWrongListener());
 		button_forward.addActionListener(new ButtonAddListener());
 		button_exit.addActionListener(new ButtonExitListener());
 
 		
 		// Read 'em and weep
 		frame.setVisible(true);
 	}
 	
 	private Container configureButtons(Container c, JButton[] jcomps){
 		for (int i=0; i< jcomps.length; i++){
 			c.add(jcomps[i]);
 		}
 		return c;
 	}
 	private Container configureTextAreas(Container c, JTextArea[] jcomps){
 		for (int i=0; i< jcomps.length; i++){
			JTextArea jta = new JTextArea();
			jta.setLineWrap(true);
			jta.setPreferredSize(new Dimension(10,10));
			jta.setBorder(BorderFactory.createLineBorder(Color.black));
 
 			JLabel jl = new JLabel("TODO");
 			// System.out.println(jcomps[i]);
 			jl.setSize(5,1);
			jl.setLabelFor(jta);
 			
 			c.add(jl);
			c.add(jta);
			jcomps[i]=jta;
 		}
 		return c;
 	}
 	
 	public void setQuestion(String s){
 		jtext_ques.setText(s);
 	}
 	
 	public void setAnswer(String s){
 		jtext_answer.setText(s);
 	}
 }
