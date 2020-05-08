 package poker;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.text.DefaultCaret;
 
 public class Window {
 
 	private JTextArea community;
 	private JTextArea score;
 	private JTextArea playcards;
 	private JTextArea terminal;
 	private JTextField bidfield;
 	private JButton bid;
 	private JButton call;
 	private JButton fold;
 	private Poker caller; //used to see who called this window
 	private JMenuItem play;
 	private JMenuItem how;
 	private JMenuItem about;
 	private JMenuItem exit;
 	private JMenu menu;
 	private JMenuBar menubar;
 	private JFrame window;
 	private Thread background;
 	
 	public Window() {
 		//Creates the Main Window and sets settings
 		final Window win = this;
 		window = new JFrame("Texas Hold 'em");
 		window.setSize(640, 480);
 		window.setMinimumSize(new Dimension(640,480));
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//Creates main panel and sets settings
 		JPanel full = new JPanel();
 		full.setLayout(new GridBagLayout());
 		GridBagConstraints constraints = new GridBagConstraints();
 		//create sub panels and sets settings
 		JPanel interaction = new JPanel();
 		interaction.setLayout(new BoxLayout(interaction, BoxLayout.Y_AXIS));
 		interaction.setBorder(BorderFactory.createMatteBorder(5,0,5,5, Color.black));
 		JPanel first = new JPanel();
 		first.setLayout(new BoxLayout(first, BoxLayout.X_AXIS));
 		JPanel second = new JPanel();
 		second.setLayout(new BoxLayout(second, BoxLayout.X_AXIS));
 		JPanel third = new JPanel();
 		third.setLayout(new BoxLayout(third, BoxLayout.X_AXIS));
 		JPanel fourth = new JPanel();
 		fourth.setLayout(new BoxLayout(fourth, BoxLayout.X_AXIS));
 		JPanel top  = new JPanel();
 		top.setLayout(new BoxLayout(top,BoxLayout.X_AXIS));
 					
 		//initialize the text areas
 		community = new JTextArea();
 		community.setLineWrap(true);
 		community.setEditable(false);
 		community.setBorder(BorderFactory.createLineBorder(Color.black, 5));
 		score = new JTextArea();
 		score.setLineWrap(true);
 		score.setEditable(false);
 		score.setBorder(BorderFactory.createMatteBorder(5,0,5,5, Color.black));
 		playcards = new JTextArea(); 
 		playcards.setLineWrap(true);
 		playcards.setEditable(false);
 		playcards.setBorder(BorderFactory.createMatteBorder(0,5,5,5, Color.black));
 		terminal = new JTextArea();
 		terminal.setLineWrap(true);
 		terminal.setEditable(false);
 		DefaultCaret caret = (DefaultCaret)terminal.getCaret();
 		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setPreferredSize(new Dimension(640, 500));
 		scrollPane.setViewportView(terminal);
 		scrollPane.setBorder(BorderFactory.createMatteBorder(0,5,5,5, Color.black));
 		//Initializes interactive widgets
 		bidfield = new JTextField();
 		bidfield.setMaximumSize(new Dimension(80,35));
 		bid = new JButton("Bid");
 		bid.addActionListener(new ActionListener (){
 			public void actionPerformed(ActionEvent ae){
 				String entered = bidfield.getText();
 				int ent;
 				try {
 					ent = Integer.parseInt(entered);
 					Poker.dropbox = ent;
 					synchronized (Poker.getLock()){
 						Poker.getLock().notify();
 					}
 				}
 				catch (NumberFormatException e){
 					print("Invalid Number, please enter again");
 				}
 				finally {
 					bidfield.setText(null);
 				}
 			}
 		});
 		call = new JButton("Call");
 		call.addActionListener(new ActionListener () {
 			public void actionPerformed(ActionEvent ae) {
 				synchronized (Poker.getLock()) {
 					Poker.dropbox = "call";
 					Poker.getLock().notify();
 				}
 			}
 		});
 		fold = new JButton("Fold");
 		fold.addActionListener(new ActionListener () {
 			public void actionPerformed(ActionEvent ae) {
 				synchronized (Poker.getLock()){
 					Poker.dropbox = "folding";
 					Poker.getLock().notify();
 				}
 			}
 		});
 		//menu bar editing
 		menu = new JMenu("File");
 		play = new JMenuItem("Play new game"); 
 		play.addActionListener(new ActionListener (){
 			public void actionPerformed(ActionEvent ae) {
 				boolean start = true;
 				boolean okay = false;
 				int playerNum = 0;
 				String message = "How many players?";
 				while(!okay){
 					String numberAI = JOptionPane.showInputDialog(null,message,"New Game", JOptionPane.QUESTION_MESSAGE);
 					if (numberAI == null){
 						start = false;
 						break;
 					}
 					try {
 						playerNum = Integer.parseInt(numberAI);
 						if (playerNum < 3 )
 							throw new NumberFormatException("Not enough players");
 						if (playerNum > 24)
 							throw new NumberFormatException("Too many players");
 						okay = true;
 						
 					}
 					catch (NumberFormatException e){
 						message = "Invalid number. Try again";
 						if (e.getMessage().equals("Not enough players") || e.getMessage().equals("Too many players"))
 							message = e.getMessage();
 					}
 				}
 				if (start){
 					if (background != null){
 						background.interrupt();
 					}
 					//Actual Creation
 					caller = new Poker(playerNum);
 					caller.passWindow(win);
 					win.redrawScore();
 					background = new Thread(caller);
 					background.start();
 				}
 			}
 		});
 		how = new JMenuItem("How to play");
 		how.addActionListener(new ActionListener (){
 			public void actionPerformed(ActionEvent ae) {
				JOptionPane.showMessageDialog(null,"Each player is given two cards at the beginning. Between betting rounds cards are added\n" +
						"to a community hand until the community hand has five cards. Whichever player has the\n" +
						"best hand their hand combined with the community hand wins the round. When only one player has money left after\n" +
						"a round they are the winner.","How to play",JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
 		about = new JMenuItem("About");
 		about.addActionListener(new ActionListener () {
 			public void actionPerformed(ActionEvent ae) {
 				JOptionPane.showMessageDialog(null, "Programmed by: Paul Steele\nCheck out paul-steele.com", "About",JOptionPane.PLAIN_MESSAGE);
 			}
 		});
 		exit = new JMenuItem("Exit");
 		exit.addActionListener(new ActionListener (){
 			public void actionPerformed(ActionEvent ae) {
 				int reallyExit = JOptionPane.showConfirmDialog(null, "Do you really want to exit?");
 				if (reallyExit == 0){
 					System.exit(0);
 				}
 			}
 		});
 		//Initializes MenuBar
 		menubar = new JMenuBar();
 		menu.add(play);
 		menu.add(how);
 		menu.add(about);
 		menu.add(exit);
 		menubar.add(menu);
 		//Set up Interactions pane
 		first.add(Box.createRigidArea(new Dimension(10,0)));
 		first.add(bidfield);
 		first.add(Box.createRigidArea(new Dimension(10,0)));
 		second.add(Box.createGlue());
 		second.add(bid);
 		second.add(Box.createGlue());
 		third.add(Box.createGlue());
 		third.add(call);
 		third.add(Box.createGlue());
 		fourth.add(Box.createGlue());
 		fourth.add(fold);
 		fourth.add(Box.createGlue());
 		interaction.add(Box.createGlue());
 		interaction.add(first);
 		interaction.add(Box.createGlue());
 		interaction.add(second);
 		interaction.add(Box.createGlue());
 		interaction.add(third);
 		interaction.add(Box.createGlue());
 		interaction.add(fourth);
 		interaction.add(Box.createGlue());
 		//add widgets to full
 		constraints.fill  = GridBagConstraints.BOTH;
 		constraints.gridx =0;
 		constraints.gridy = 0;
 		constraints.gridwidth = 2;
 		constraints.weightx = .7;
 		constraints.weighty = 0.0;
 		full.add(community, constraints);
 		constraints.gridx = 2;
 		constraints.gridwidth = 1;
 		constraints.weightx = .3;
 		full.add(score, constraints);
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.gridwidth = 2;
 		constraints.weighty = 0.0;
 		constraints.weightx =.7;
 		full.add(playcards, constraints);
 		constraints.gridx= 0;
 		constraints.gridy = 2;
 		constraints.gridwidth = 2;
 		constraints.weighty = 1.0;
 		full.add(scrollPane, constraints);
 		constraints.gridx =2;
 		constraints.gridwidth = 1;
 		constraints.weightx = .3;
 		full.add(interaction, constraints);
 		//add main panel to window
 		window.add(full);
 		//add the menubar
 		window.setJMenuBar(menubar);
 		//start off buttons disabled
 		buttonsEnabled(false);
 		//display window
 		window.setResizable(true);
 		window.setVisible(true);
 	}
 
 	public void redrawScore(){
 		score.setText("-----Cash Amounts-----\n"); //clears out the text
 		if (caller != null){
 			for (int i = 1; i < caller.PLAYERS + 1; i++){
 				score.append(caller.getPlayer(i).getName()+": $"+caller.getPlayer(i).getCash() + "\n");
 			}
 			score.append("\nCash in pot: " + caller.getPot());
 		}
 	}
 
 	
 	public void print(String str){
 		terminal.append(str + "\n");
 	}
 	
 	public void printToCommunity(String str) {
 		community.append(str);
 	}
 	public void clearCommunity(){
 		community.setText("-----Community Cards-----\n");
 	}
 	
 	public void buttonsEnabled(boolean desired){
 		bid.setEnabled(desired);
 		call.setEnabled(desired);
 		fold.setEnabled(desired);
 	}
 	
 	public void printToPlayerCards(String str){
 		playcards.append(str);
 	}
 	
 	public void clearPlayerCards(){
 		playcards.setText("-----Your Hand-----\n");
 	}
 }
