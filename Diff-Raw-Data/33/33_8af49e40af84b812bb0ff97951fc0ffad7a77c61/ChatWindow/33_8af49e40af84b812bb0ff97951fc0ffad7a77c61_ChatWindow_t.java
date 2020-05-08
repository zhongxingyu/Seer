 package bcmGUI;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.LinkedList;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.Timer;
 import javax.swing.border.EmptyBorder;
 
 import bcmBackend.Friend;
 import bcmBackend.Informable;
 import bcmBackend.bcmPlaySound;
 import bcmNetworking.BCMProtocol;
 import bcmNetworking.Bridge;
 import bcmNetworking.ReceiveSocketSW;
 import bcmNetworking.SendSocketSW;
 
 public class ChatWindow extends JFrame {
 	private static final long serialVersionUID = 958392446358889555L;
 	
 	//THREAD TESTER // DEBUG
 	//private FakeFriend ff;
 	
 	//VALUES
 	private Friend chatmate;
 	private String username;
 
 	//CONNECTIONS
 		//private ManagerSocket mgtSocket = new ManagerSocket();
 	
 	// MESSAGE LOG UPDATER
 		private LinkedList<String> messageBuffer = new LinkedList<String>();
 		private Timer updatetimer;
		private int updatedelay = 3000;
 		
 	// BUZZ FUNCTION VARIABLES
 		private short buzzBuffer = 0;
 		private Timer buzztime;
 		private short currentBuzzFrame;
 		private int[] bdaX;
 		private int[] bdaY;
 		private double[] baMagnitude = new double[BCMTheme.MAX_BUZZ_FRAMES];
 		private Rectangle boundsholder;
 		
 	// COMPONENTS
 	private JPanel contentPane = new JPanel();	
 		private JPanel pnlComposing = new JPanel();
 			private JTextField composeMessageField = new JTextField();
 			private JButton btnSendButton = new JButton("Send");
 			private final JTextArea messageLogTextArea = new JTextArea();
 			private final JScrollPane scrlpnMsgLogArea = new JScrollPane(messageLogTextArea);
 			private final JToolBar chatToolBar = new JToolBar();
 				private final JButton btnBuzz = new JButton("BUZZ");
 				private final JLabel lblisTypingLabel = new JLabel("");
 				private final Component toolbarSeparator = Box.createHorizontalStrut(20);
 				
 	private JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrlpnMsgLogArea, pnlComposing);
 	
 	//NETWORKING
 	private ReceiveSocketSW receiveSocket;
 	private SendSocketSW sendSocket;
 	private Bridge bridge;
 	private Informable informable;
 	
 	//CONSTRUCTOR
 	public ChatWindow(Friend cm, String username, Bridge bridge) {
 		this.username = username;
 		
 		//prepare window
 		this.chatmate = cm;
 		this.setIconImage(BCMTheme.CHAT_ICON);
 		this.setTitle(chatmate.getNickname());
 		
 		//prepare components
 		setComponentProperties();
 		setComponentHierarchy();
 		this.setVisible(true);	
 		
 		//networking setup
 		informable = new Informable(){
 			@Override
 			public void messageReceived(String message){
 				parseIncomingMessage(message);
 				
 			}
 		};
 		
 		this.bridge = bridge;
 		
 		
 		//SET THIS TEXT IF CONNECTION WAS SUCCESSFUL
 		//TODO : Remove FakeFriend after hooking to real connection
 		//connectToFakeFriend();
 		messageLogTextArea.append("Now chatting with " + chatmate.getNickname() + "\n");
 		
 		
 	}
 	public ChatWindow(Friend cm, String username, ReceiveSocketSW receiveSocket, SendSocketSW sendSocket, Bridge bridge) {
 		this(cm, username, bridge);
 		this.sendSocket = sendSocket;
 		this.receiveSocket = receiveSocket;
 		this.receiveSocket.setInformable(informable);
 		enableChat();
 	}
 	
 	
 
 	
 	private void setComponentProperties() {
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		this.addWindowListener(evlCloseWindow);
 		this.setBounds(100, 100, 450, 300);
 		this.setContentPane(contentPane);
 		
 		splitPane.resetToPreferredSizes();
 		splitPane.setResizeWeight(1.0);
 		splitPane.setDividerSize(4);
 		
 		pnlComposing.setLayout(new BorderLayout(0, 0));
 		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		
 		composeMessageField.setPreferredSize(new Dimension(10, 30));
 		composeMessageField.setColumns(10);
 		chatToolBar.setFloatable(false);
 		
 		// MESSAGE LOG AREA
 		scrlpnMsgLogArea.setAutoscrolls(true);
 		messageLogTextArea.setEditable(false);
 		messageLogTextArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
 		messageLogTextArea.setRows(10);
 		messageLogTextArea.setWrapStyleWord(true);
 		messageLogTextArea.setLineWrap(true);
 		
 		
 		//MESSAGE LOG UPDATER TIMER
 		updatetimer = new Timer(updatedelay, evlUpdater);
 		updatetimer.setRepeats(true);
 		
 		//ASSIGN LISTENERS
 		composeMessageField.addKeyListener(evlmsgField);
 		btnSendButton.addActionListener(evlSendButton);
 
 		composeMessageField.grabFocus();
 		
 		//BUZZ FEATURE
 		btnBuzz.setMnemonic('B');
 		btnBuzz.setToolTipText("Send a buzz to your friend");
 		btnBuzz.addActionListener(evlBuzzer);
 		
 		buzztime = new Timer(BCMTheme.BUZZ_TIMER_DELAY, evlJiggle);
 		buzztime.setRepeats(true);
 		genBuzzMagnitudeArray();
 		
 		//DISABLE BY DEFAULT
 		composeMessageField.setEnabled(false);
 		messageLogTextArea.setEnabled(false);
 		btnBuzz.setEnabled(false);
 		btnSendButton.setEnabled(false);
 		this.setTitle("Attempting to connect to " + chatmate.getNickname());
 		lblisTypingLabel.setText("Attempting to connect to " + chatmate.getNickname());
 	}
 	
 	
 	private String getMessageBoxContents() {
 		if(!composeMessageField.getText().equals("")) {
 			String toSend = BCMProtocol.MESSAGE_CODE + composeMessageField.getText();
 			messageLogTextArea.append("\n" + username + ": " + toSend.substring(1));
 			composeMessageField.setText("");
 			composeMessageField.grabFocus();
 			return toSend;
 		}
 		return "";
 	}
 	
 	/*
 	// replace contents with something that hooks onto the client thread 
 	private void sendMessage(String msg) {
 		//TODO: Pass data to the thread to send a message
 		//messageLogTextArea.append("\n<" + username + "> " + msg + "");   // dummy send
 		//ff.sendMsg(msg);
 	}*/
 	
 	private void sendIsTyping() {
 		bridge.putMessage(BCMProtocol.ISTYPING_CODE + "");
 		//TODO: Pass data to the thread to send the typing status
 	    // code is BCMProtocol.ISTYPING_CODE
 		
 	}
 	
 	private void sendBuzz() {
 		bridge.putMessage(BCMProtocol.BUZZ_CODE + "");
 		//TODO: Pass data to the thread to send a buzz
 		// code is BCMProtocol.BUZZ_CODE
 		
 	}
 	
 	
 	
 	
 	private void setComponentHierarchy() {
 		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		contentPane.add(splitPane, BorderLayout.CENTER);
 		pnlComposing.add(composeMessageField, BorderLayout.CENTER);
 		pnlComposing.add(btnSendButton, BorderLayout.EAST);
 		pnlComposing.add(chatToolBar, BorderLayout.NORTH);
 		chatToolBar.add(btnBuzz);
 		chatToolBar.add(toolbarSeparator);
 		chatToolBar.add(lblisTypingLabel);
 	}
 	
 	
 	
 	// BUZZ FUNCTION
 	private void buzzWindow(){
 		if (currentBuzzFrame == 0) {
			new bcmPlaySound("Greetings.wav").run();
 			newBuzzArray();
 			boundsholder = this.getBounds();
 			buzztime.start();
 			messageLogTextArea.append("\n BUZZ!!");
 		}	
 	}
 
 	private void newBuzzArray() {
 		bdaX = new int[BCMTheme.MAX_BUZZ_FRAMES];
 		bdaY = new int[BCMTheme.MAX_BUZZ_FRAMES];
 		for(int r = 0; r < BCMTheme.MAX_BUZZ_FRAMES; r++) {
 			double direction = (Math.random() * 180) * ((currentBuzzFrame % 2) + 1);
 			bdaX[r] = (int)(Math.cos(direction) * baMagnitude[r]);
 			bdaY[r] = (int)(Math.sin(direction) * baMagnitude[r]);
 		}
 	}
 	
 	private void genBuzzMagnitudeArray() {
 		for(int r = 0; r < BCMTheme.MAX_BUZZ_FRAMES; r++) {
 			baMagnitude[r] = BCMTheme.MAX_BUZZ_MAGNITUDE / (int)(Math.pow((double)r + 1, 1.2));
 			//System.out.printf("%1.1f  ", baMagnitude[r]);		//DEBUG display Magnitude array
 		}
 	}
 	
 	// EVL JIGGLE IS REPEATEDLY TRIGGERED
 	private ActionListener evlJiggle = new ActionListener() {
 		public void actionPerformed(ActionEvent ae){
 			oscillateWindow();
 		}
 	};
 	
 	// CONSEQUENTLY, OSCILLATEWINDOW IS CALLED SEVERAL TIMES
 	private void oscillateWindow(){
 		if (currentBuzzFrame < BCMTheme.MAX_BUZZ_FRAMES) {
 			this.setBounds(boundsholder.x + bdaX[currentBuzzFrame], boundsholder.y + bdaY[currentBuzzFrame], boundsholder.width, boundsholder.height);
 			currentBuzzFrame++;
 		} else {
 			buzztime.stop();
 			buzzBuffer--;
 			currentBuzzFrame = 0;
 			this.setBounds(boundsholder);
 			return;
 		}
 	}
 	
 	
 	
 
 	
 	//LISTENERS
 	private KeyAdapter evlmsgField = new KeyAdapter() {
 		@Override
 		public void keyPressed(KeyEvent k) {
 			
 			sendIsTyping();
 			
 			if (k.getKeyCode() == KeyEvent.VK_ENTER) { 
 				bridge.putMessage(getMessageBoxContents());
 			}
 			
 			if (k.getKeyCode() == KeyEvent.VK_B && k.getModifiers() == KeyEvent.VK_CONTROL) {
 				buzzWindow();
 			}
 		}
 	};
 	
 	private ActionListener evlSendButton = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			bridge.putMessage(getMessageBoxContents());
 		}
 	};
 	
 	private ActionListener evlBuzzer = new ActionListener() {
 		public void actionPerformed(ActionEvent arg0) {
 			if (currentBuzzFrame == 0) { 
 				buzzBuffer++;
 				sendBuzz();
 				buzzWindow();
 			}
 			
 		}
 	};
 	
 	private ActionListener evlUpdater = new ActionListener(){
 		public void actionPerformed(ActionEvent arg0){
 			showNotTyping();
 		}
 	};
 	
 	private void showIsTyping() {
 		this.setTitle(chatmate.getNickname() + " is typing...");
 		lblisTypingLabel.setText(chatmate.getNickname() + " is typing...");
 	}
 	
 	private void showNotTyping() {
 		this.setTitle(chatmate.getNickname());
 		lblisTypingLabel.setText("");
 	}
 	
 	
 	private void parseIncomingMessage(String currMessage) {
 		
 		char code;
 
 			code = currMessage.charAt(0);
 			
 			if(code == BCMProtocol.MESSAGE_CODE) {
 				messageLogTextArea.append("\n" + chatmate.getNickname() + ": " + currMessage.substring(1));
 				showNotTyping();
 			}
 			else if (code == BCMProtocol.CLOSED_CODE) {
 				messageLogTextArea.append("\n" + chatmate.getNickname() + " has gone offline.");
 				JOptionPane.showMessageDialog(null, "Chatmate has left the chat. Closing Window.");
 				timeToClose();
 			}
 			else if (code == BCMProtocol.ISTYPING_CODE) {
 				showIsTyping();
 			}
 			else if (code == BCMProtocol.BUZZ_CODE) {
 				buzzBuffer++;
 				buzzWindow();
 				showNotTyping();
 			}
 			
 	}
 	
 	// WINDOW CLOSER
 	private WindowAdapter evlCloseWindow = new WindowAdapter() {
 	    public void windowClosing(WindowEvent e) {
 	        timeToClose();
 	    }
 	};
 	
 	private void timeToClose() {
 		//ff.cancel(false);
 		bridge.putMessage(BCMProtocol.CLOSED_CODE+ "");
 		if(receiveSocket!=null)
 			receiveSocket.cancel(false);
 		if(sendSocket!=null)
 			sendSocket.cancel(false);
     	this.dispose();
 	}
 	
 	
 	public void setReceiveSocket (ReceiveSocketSW receiveSocket){
 		this.receiveSocket = receiveSocket;
 		this.receiveSocket.setInformable(informable);
 	}
 	
 	public void setSendSocket (SendSocketSW sendSocket){
 		this.sendSocket = sendSocket;
 	}
 	
 	public void enableChat (){
 		composeMessageField.setEnabled(true);
 		messageLogTextArea.setEnabled(true);
 		btnBuzz.setEnabled(true);
 		btnSendButton.setEnabled(true);
 		this.setTitle("");
 		lblisTypingLabel.setText("");
 		updatetimer.start();
 	}
 	
 	//FAKEFRIEND
 	/*private void connectToFakeFriend() {
 		if (ff == null) {
 				//TO START A NEW SWINGWORKER THREAD
 		        SwingUtilities.invokeLater(new Runnable() {
 		            public void run() {
 		            	ff = new FakeFriend(messageBuffer);
 		            	ff.execute();
 		            }
 		        });
 		       //TO START A NEW THREAD
 		}
 	}*/
 	
 	
 	
 	
 }
