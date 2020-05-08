 package edu.ncsu.csc216.solitaire.ui;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.Scanner;
 import java.util.zip.DataFormatException;
 
 import javax.swing.*;
 
 import edu.ncsu.csc216.solitaire.model.*;
 
 /**
  * The userInterface
  * @author Andrew Kofink, William Blazer
  */
 public class UI {
 	
 	private static Deck deck;
 	
 	private static MessageFrame mf;
 	
 	private static Message message;
 	
 	/**
 	 * @param args The input parameters to the program
 	 */
 	public static void main(String[] args) {
 		userInterface();
 
 		mf = new MessageFrame();
 		mf.setVisible(true);
 	}
 	
 	/**
 	 * Interfaces with the user and accepts the input file
 	 */
 	public static void userInterface() {
 		
 		//Get the deck file from the user
 		JFileChooser chooser = new JFileChooser(new File("."));
 		int returnValue = chooser.showOpenDialog(null);
 		
 		try {
 			if (returnValue == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().canRead()) {
 				File f = chooser.getSelectedFile();
 				Scanner in = new Scanner(f);
 				
 				//Look for proper file format
 				String deckString = "";
 				while (in.hasNextLine()) {
 					deckString += in.nextLine();
 				}
 				
 				//Match this regular expression, or the file format is wrong
 				if (!deckString.matches("\\A(\\d+[ ]){27}\\d+\\z")) {
 					throw new DataFormatException();
 				}
 
 				//Gather file contents into an array of integers
 				in = new Scanner(f);
 				int[] deckInts = new int[28];
 				for (int i = 0; i < deckInts.length; i++) {
 					deckInts[i] = in.nextInt();
 				}
 				
 				deck = new Deck(deckInts);
			} else if (returnValue == JFileChooser.CANCEL_OPTION) {
				System.exit(0);
 			} else {
 				throw new FileNotFoundException();
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found.");
 			userInterface();
 		} catch (DataFormatException dfe) {
 			System.out.println("Invalid file.");
 			userInterface();
 		}
 	}
 	
 	public static Deck getDeck() {
 		return deck;
 	}
 	
 	public static MessageFrame getMessageFrame() {
 		return mf;
 	}
 	
 	public static void setMessageFrame(MessageFrame newFrame) {
 		mf = newFrame;
 	}
 	
 	public static void setMessage(Message m) {
 		message = m;
 	}
 	
 	public static Message getMessage() {
 		return message;
 	}
 }
 
 class MessageFrame extends JFrame implements ActionListener {
 	
 	private JTextArea messageTextArea;
 	
 	private char encType;
 	
 	public MessageFrame() {
 		super();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new FlowLayout());
 		
 		JPanel messagePanel = new JPanel();
 		messagePanel.setLayout(new BorderLayout());
 		add(messagePanel);
 		
 		JLabel messageTextLabel = new JLabel("Please enter a message.");
 		messagePanel.add(messageTextLabel, BorderLayout.NORTH);
 		
 		messageTextArea = new JTextArea(5, 20);
 		JScrollPane scrollPane = new JScrollPane(messageTextArea);
 		messagePanel.add(scrollPane, BorderLayout.CENTER);
 		
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new FlowLayout());
 		messagePanel.add(buttons, BorderLayout.SOUTH);
 		
 		JButton encrypt = new JButton("Encrypt");
 		encrypt.addActionListener(this);
 		buttons.add(encrypt);
 		
 		JButton decrypt = new JButton("Decrypt");
 		decrypt.addActionListener(this);
 		buttons.add(decrypt);
 		
 		pack();
 	}
 	
 	public void actionPerformed(ActionEvent ae) {
 		Message m = new Message(messageTextArea.getText());
 		UI.setMessage(m);		
 		
 		CardIcons.reset();
 		CardIcons.init().setVisible(true);
 		
 		if (ae.getActionCommand().toLowerCase().startsWith("e")) {
 			encType = 'e';
 		} else {
 			encType = 'd';
 		}
 		dispose();
 	}
 	
 	public String getMessageText() {
 		return messageTextArea.getText();
 	}
 	
 	public void clearMessageText() {
 		messageTextArea.setText("");
 	}
 	
 	public char getEncType() {
 		return encType;
 	}
 }
