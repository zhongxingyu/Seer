 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class GUI extends JFrame
 {
 	private static final int H = 970; // Height of window
 	private static final int W = 590; // Width of window
 	
 	private static final int LM1 = 10;	//left margin
 	private static final int LM2 = 300;	//Second Left margin (2nd column)
 	
 	private static final int VM = 10;	//Vertical Margin inbetween 
 	
 	private static final int WLABEL = 150; // w oflabel
 	private static final int HLABEL = 20; // h of label
 
 	private static final int WSIZE = 260; // Width of sub window
 	private static final int HSIZE = 250; // Height of sub window
 	
 	private static final int HBTN = 40; // Height of button
 	
 	private static final String[] labelNames = {"Enter text to encrypt", 
 			"Generated key", "Generated text", "Enter text to decrypt",
 			"Enter key", "Decrypted text"};
 	
 	private static final int size = labelNames.length;
 	
 	private JLabel[] labels 	  = new JLabel[size];
 	private JTextArea[] textBoxes = new JTextArea[size];
 	
 	private static final String BUTTON_NAME1 = "Encrypt";
 	private static final String BUTTON_NAME2 = "Decrypt";
 	private static final String[] BTN_NAMES = {BUTTON_NAME1, BUTTON_NAME2};
 	
 	
 	private JButton[] buttons = new JButton[2];
 	private ButtonPress onBtnPress = new ButtonPress();
 	
 	private Translator trans = new Translator();
 
 	public GUI()
 	{
 		Container cp = getContentPane(); // Content pane
 		cp.setLayout(null); // No layout manager
 		setSize(W, H); // Size of Window
 		setTitle("'Ashu' Enc/Dec 0.8 by Almas. Alpha tester - Atheryos");
 		Font font = new Font("Monospaced", Font.PLAIN, 12); // Font Used
 		
 		for (int i = 0; i < size; i++)
 		{
 			labels[i] = new JLabel(labelNames[i]);
 			labels[i].setFont(font);
 			
 			textBoxes[i] = new JTextArea();
 			textBoxes[i].setFont(font);
 			textBoxes[i].setLineWrap(true);
 			
 			int LM = (i >= 3) ? LM2 : LM1;
			int TM = VM;
 			
 			if (i == 3)
 				TM = VM;
 			
 			labels[i].setBounds(LM, TM, WLABEL, HLABEL);
 			TM += HLABEL + VM;
 			textBoxes[i].setBounds(LM, TM, WSIZE, HSIZE);
 			TM += HSIZE + VM;
 			
 			cp.add(labels[i]);
 			cp.add(textBoxes[i]);
 		}
 		
 		for (int i = 0; i < buttons.length; i++)
 		{
 			buttons[i] = new JButton(BTN_NAMES[i]);
 			if (i == 0)
 				buttons[i].setBounds(LM1, 880, WSIZE, HBTN);
 			else
 				buttons[i].setBounds(LM2, 880, WSIZE, HBTN);
 			buttons[i].addActionListener(onBtnPress);
 			cp.add(buttons[i]);
 		}
 		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);// Exit on close
 	}
 	
 	class ButtonPress implements ActionListener
 	{
 		public void actionPerformed(ActionEvent ae)
 		{
 			String text = "";
 			
 			switch(ae.getActionCommand())
 			{
 				case BUTTON_NAME1:
 					
 					text = textBoxes[0].getText();
 					textBoxes[2].setText(trans.encrypt(text));	//because of the layout 2 comes before 1
 					textBoxes[1].setText(trans.getKey());
 					break;
 					
 				case BUTTON_NAME2:
 					
 					text = textBoxes[3].getText();
 					String key = textBoxes[4].getText();
 					textBoxes[5].setText(trans.decrypt(text, key));	
 					break;
 					
 				default:
 					break;
 			}
 		}
 	}
 }
