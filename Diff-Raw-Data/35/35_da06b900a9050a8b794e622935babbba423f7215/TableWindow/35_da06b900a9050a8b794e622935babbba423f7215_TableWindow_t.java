 package gui;
 
 import gameLogic.SetCard;
 import gameLogic.SetTable;
 import gui.ImgPanel.imgAccessor;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 
 import aurelienribon.slidinglayout.SLAnimator;
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenManager;
 import aurelienribon.tweenengine.equations.Bounce;
 import aurelienribon.tweenengine.equations.Elastic;
 
 public class TableWindow {
 
 	public JFrame frmTable;
 	public static boolean isAdmin  = false;
 	private SetTable setTable = null;
 	private JTable playerList;
 	private JPanel tablePanel;
 	private JPanel imgPanel;
 	private JLabel lblStartGame;
 	private JTextArea chatWindow;
 	private boolean didVote = false;
 	private JTextField textField;
 	private JScrollPane scrollPane;
 	private static TweenManager resultTweens = null;
 	private JButton btnReady = null;
 	private JButton btnExit = null;
 	private MP3 sound = null;
 	
 	public TableWindow() {
 		setTable = new SetTable();
 		resultTweens = SLAnimator.createTweenManager();
 		Tween.registerAccessor(SetCard.class, new SetCard.Accessor());
 		Tween.registerAccessor(ImgPanel.class, new ImgPanel.imgAccessor());
 		SLAnimator.start();
 		initialize();
 	}
 		@SuppressWarnings("serial")
 	private void initialize() {
 		
 		frmTable = new JFrame();
 		frmTable.setResizable(false);
 		frmTable.setTitle("Table View");
 		frmTable.setBounds(100, 100, 1050, 600);
 		frmTable.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmTable.getContentPane().setLayout(null);
 		
 		btnExit = new JButton("EXIT TABLE");
 		btnExit.setBounds(884, 526, 130, 23);
 		btnExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				MainClient.sendMessage("E");
 			}
 		});
 		
 		frmTable.getContentPane().add(btnExit);
 		
 		tablePanel = new JPanel();
 		tablePanel.setBounds(873, 11, 150, 175);
 		frmTable.getContentPane().add(tablePanel);
 		
 		playerList = new JTable();
 		playerList.setEnabled(false);
 		playerList.setBounds(0, 0, 150, 175);
 		
 		JTableHeader tableHeader = playerList.getTableHeader();
 		playerList.setShowGrid(false);
 		playerList.setModel(new DefaultTableModel(
 			new Object[][] {},
 			new String[] {"Player", "Points" })
 		{
 			boolean[] columnEditables = new boolean[] {
 				false, false
 			};
 			public boolean isCellEditable(int row, int column) {
 				return columnEditables[column];
 			}
 		});
 		tablePanel.setLayout(new BorderLayout());
 		tablePanel.add(playerList,BorderLayout.CENTER);
 		tablePanel.add(tableHeader, BorderLayout.NORTH);
 		
 		btnReady = new JButton("Ready!");
 		btnReady.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(!didVote){
 					MainClient.sendMessage("G");
 					didVote = true;
 					btnReady.setEnabled(false);
 					btnReady.setText("Waiting");
 					btnExit.setEnabled(false);
 					lblStartGame.setText("Waiting for Players...");
 				}
 			}
 		});
 		btnReady.setBounds(884, 502, 130, 23);
 		frmTable.getContentPane().add(btnReady);
 		
 		JButton btnCheat = new JButton("CHEAT");
 		btnCheat.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				SetTable.cheat();
 			}
 		});
 		btnCheat.setBounds(884, 478, 130, 23);
 		if(isAdmin){
 			frmTable.getContentPane().add(btnCheat);
 		}
 		
 		scrollPane = new JScrollPane();
 		scrollPane.setBounds(873, 197, 150, 234);
 		frmTable.getContentPane().add(scrollPane);
 		
 		chatWindow = new JTextArea();
 		scrollPane.setViewportView(chatWindow);
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		chatWindow.setForeground(Color.BLUE);
 		chatWindow.setEditable(false);
 		chatWindow.setLineWrap(true);
 		chatWindow.setWrapStyleWord(true);
 		
 		textField = new JTextField();
 		textField.setColumns(10);
 		textField.setBounds(873, 437, 150, 30);
 		textField.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent evt) {
 				if(evt.getKeyCode() == KeyEvent.VK_ENTER){
                    sendChat();
                 }
 			}
 			@Override
 			public void keyReleased(KeyEvent e) {}
 			@Override
 			public void keyTyped(KeyEvent e) {}
 		});
 		
 		frmTable.getContentPane().add(textField);
 		frmTable.setGlassPane(new JPanel());
 		((JPanel)frmTable.getGlassPane()).setOpaque(false);
 		imgPanel = new ImgPanel(0);
 		imgPanel.setBounds(440,270,1,1);
 		imgPanel.setVisible(false);
 		
 	}
 		
 	public void tableReset() {
 		setTable.tableView.removeAll();
 		setTable.tableView.repaint();
 				
 		btnReady.setEnabled(true);
 		btnReady.setText("Ready");
 		
		btnExit.setEnabled(true);
		
 		didVote = false;
 		
 		lblStartGame = new JLabel("Click Ready!");
 		lblStartGame.setFont(new Font("Sans", Font.BOLD, 36));
 		lblStartGame.setBounds(50, 145, 750, 150);
 		
 		setTable = new SetTable();
 			
 		setTable.tableView.setBorder(BorderFactory.createBevelBorder(1));
 		setTable.tableView.add(lblStartGame);
 		setTable.tableView.setBounds(10, 10, 850, 550);
 		setTable.tableView.setLayout(null);
 		frmTable.getContentPane().add(setTable.tableView);
 	}
 	
 	public void updatePlayers(String[] splitLine) { // splitLine: P;3;4;Nico;2;Sameer;3;Vasily;14
 		int numPlayers = Integer.parseInt(splitLine[1]);
 		Object data [][] = new Object[numPlayers][4];
 		for(int i = 0; i<numPlayers; i++){
 			data[i][0] = splitLine[2*i+3];
 			data[i][1] = splitLine[2*i+4];
 		}
 		String colName [] = {"Player", "Points"};
 		DefaultTableModel tm = new DefaultTableModel(data,colName);
 		playerList.setModel(tm);
 		tm.fireTableDataChanged();
 	}
 
 	//Parses incoming message and adds cards to the board
 	public void tableCards(final String[] splitLine) { // splitLine: T;12;00;01;02;03;04;05;06;07;08;09;10;11
 		Thread startGameThread = new Thread() {
 			public void run() {
 				lblStartGame.setText("Enjoy your game! 3");
 				try {
 					Thread.sleep(700);
 					lblStartGame.setText("Enjoy your game! 2");
 					Thread.sleep(700);
 					lblStartGame.setText("Enjoy your game! 1");
 					Thread.sleep(600);
 				} catch (InterruptedException e) {
 					//do nothing
 				}
 				lblStartGame.setVisible(false);
 				int cardNum = 0;
 				for( int i =0; i< Integer.parseInt(splitLine[1]); i++){
 					cardNum = Integer.parseInt(splitLine[i+2]);
 					setTable.addToTable(cardNum);
 				}
 				setTable.tableView.removeAll();
 				setTable.tableView.updateUI();
 				setTable.tableView.repaint();
 				setTable.tableView.revalidate();
 				setTable.tableView.initialize(setTable.defaultLayout());
 			}
 		};			
 		startGameThread.start();
 	}
 	
 	public void sendChat() {
 		String txt = textField.getText();
 		if(txt.contains(";")) {
 			JOptionPane.showMessageDialog(frmTable, "Cannot send message with \";\" in it.");
 		} else if(!txt.isEmpty()) {
 			MainClient.sendMessage("Q;" + txt);
 			chatWindow.setCaretPosition(chatWindow.getDocument().getLength());
 			textField.setText(null);
 		}
 	}
 	
 	public void newChat(String user, String chat) {
 		chatWindow.append(user + ": " + chat + "\n");
 	}
 
 	public void setMade(int C1, int C2, int C3) {
 		setTable.setMade(C1, C2, C3);
 	}
 	
 	public void youScrewedUp() {
 		sound = new MP3(0);
 		sound.play();
 		imgPanel = new ImgPanel(0);
 		imgPanel.setBounds(440,270,1,1);
 		imgPanel.setOpaque(false);
 		((JPanel)frmTable.getGlassPane()).add(imgPanel);
 		frmTable.getGlassPane().setVisible(true);
 		Tween.to(imgPanel, imgAccessor.SCALE, 0.5f)
 			.targetRelative(-225,-225,450,450)
 			.ease(Elastic.OUT)
 			.setCallbackTriggers(TweenCallback.COMPLETE)
 			.setCallback(new TweenCallback() {
 				@Override
 				public void onEvent(int arg0, BaseTween<?> arg1) {
 					imgPanel.setVisible(false);
 					frmTable.getGlassPane().setVisible(false);
 				}
 			})
 			.start(resultTweens);
 	}
 	
 	public void youMadeASet() {
 		sound = new MP3(1);
 		sound.play();
 		imgPanel = new ImgPanel(1);
 		imgPanel.setBounds(440,270,1,1);
 		imgPanel.setOpaque(false);
 		((JPanel)frmTable.getGlassPane()).add(imgPanel);
 		frmTable.getGlassPane().setVisible(true);
 		Tween.to(imgPanel, imgAccessor.SCALE, 1f)
 			.targetRelative(-225,-225,450,450)
 			.ease(Bounce.OUT)
 			.setCallbackTriggers(TweenCallback.COMPLETE)
 			.setCallback(new TweenCallback() {
 				@Override
 				public void onEvent(int arg0, BaseTween<?> arg1) {
 					imgPanel.setVisible(false);
 					frmTable.getGlassPane().setVisible(false);
 				}
 			})
 			.start(resultTweens);
 	}
 	
 	public static void sendSet( int C1, int C2, int C3){
 		MainClient.sendMessage("S;"+ C1+ ";"+ C2 + ";" + C3);
 	}
 
 	public void newCards(int C1, int C2, int C3) {
 		setTable.newCards(C1, C2, C3);
 	}
 
 	public void noSets() {
 		// TODO : Display a "No More Sets" indicator
 		JOptionPane.showMessageDialog(frmTable, "No more sets, bro!");
 	}
 
 	public void gameOver() {
 		// TODO : Display a "Game Over" screen
 		// setTable.clearCards();
 		
 		tableReset();
 		JOptionPane.showMessageDialog(frmTable, "Game Over, bro!");
 		
 	}
 	
 	
 	public void hideTable(){
 		frmTable.setVisible(false);
 	}
 	
 	public void showTable(){
 		didVote = false;
 		initialize();
 		tableReset();
 		frmTable.setVisible(true);
 	}
 }
