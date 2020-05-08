 /* CS3283/CS3284 Project
  * 
  * Game Server GUI: simple graphical user interface for the game server
  * 
  */
 
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import javax.swing.JTextArea;
 import java.awt.Insets;
 import java.awt.Color;
 import javax.swing.JScrollPane;
 import javax.swing.text.DefaultCaret;
 import javax.swing.JButton;
 import javax.swing.border.TitledBorder;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import javax.swing.UIManager;
 import javax.swing.border.EtchedBorder;
 import java.awt.Font;
 import javax.swing.JTextField;
 
 public class GameServerGUI {
 
 	private JFrame frame;
 	private GameServer gameServer;
 	private GameServerThread gameServerThread;
 
 	JButton startButton = new JButton("Start Server");
 	JButton closeButton = new JButton("Close Server");
 
 	JTextArea JConsole = new JTextArea();
 	JTextArea JKeyCode = new JTextArea();
 	JTextArea JPlayer = new JTextArea();
 	JTextArea JTreasure = new JTextArea();
 	JTextArea JControlHelp = new JTextArea();
 
 	JPanel panelKeyCodePairingList = new JPanel();
 	JPanel panelTreasureList = new JPanel();
 	JPanel panelPlayerInfo = new JPanel();
 	JPanel panelControls = new JPanel();
 
 	JButton btnMoveUp = new JButton("^");
 	JButton btnMoveDown = new JButton("v");
 	JButton btnMoveLeft = new JButton("<");
 	JButton btnMoveRight = new JButton(">");
 	private JTextField textFieldPlayerID;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					System.out.println("Starting ProjectKim Server GUI");
 					GameServerGUI window = new GameServerGUI();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 * @throws Exception 
 	 */
 	public GameServerGUI() throws Exception {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame("Project Kim Server");
 		frame.getContentPane().setBackground(Color.LIGHT_GRAY);
 		frame.setResizable(false);
 		frame.setBounds(100, 100, 600, 600);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 
 		JScrollPane JConsoleScrollPane = new JScrollPane();
 		JConsoleScrollPane.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		JConsoleScrollPane.setBounds(10, 253, 429, 305);
 
 		DefaultCaret caret = (DefaultCaret)JConsole.getCaret();
 		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 		JConsoleScrollPane.setBackground(Color.LIGHT_GRAY);
 		frame.getContentPane().add(JConsoleScrollPane);
 
 		JConsoleScrollPane.setViewportView(JConsole);
 		JConsole.setMargin(new Insets(5, 5, 5, 5));
 		JConsole.setRows(5);
 		JConsole.setEditable(false);
 		JConsole.setText("Press Start to continue...");
 		panelKeyCodePairingList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		panelKeyCodePairingList.setBounds(10, 11, 574, 100);
 
 		frame.getContentPane().add(panelKeyCodePairingList);
 		panelKeyCodePairingList.setLayout(new BorderLayout(0, 0));
 		JKeyCode.setLineWrap(true);
 		JKeyCode.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Key Code pairing list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		JKeyCode.setEditable(false);
 		JKeyCode.setMargin(new Insets(5, 5, 5, 5));
 
 		JKeyCode.setText("Displays all KeyCodes paired with their respective Node\n"
 				+ "KeyCode: 1234    Node: 16\nKeyCode pair: 1234[16]\n"
 				+"Each KeyCode can only be use once by each player");
 		panelKeyCodePairingList.add(JKeyCode, BorderLayout.CENTER);
 		panelTreasureList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		panelTreasureList.setBounds(10, 132, 170, 100);
 
 		frame.getContentPane().add(panelTreasureList);
 		panelTreasureList.setLayout(new BorderLayout(0, 0));
 		JTreasure.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Treasure List", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panelTreasureList.add(JTreasure, BorderLayout.CENTER);
 		JTreasure.setEditable(false);
 		JTreasure.setMargin(new Insets(5, 5, 5, 5));
		JTreasure.setText("Displays treasure chests\nlocated at each Node\n0: no treasure chest\n1: has treasure chest");
 		panelPlayerInfo.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		panelPlayerInfo.setBounds(200, 132, 384, 100);
 
 		frame.getContentPane().add(panelPlayerInfo);
 		panelPlayerInfo.setLayout(new BorderLayout(0, 0));
 		JPlayer.setBorder(new TitledBorder(null, "Player Infomation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panelPlayerInfo.add(JPlayer, BorderLayout.CENTER);
 
 		JPlayer.setEditable(false);		
 		JPlayer.setMargin(new Insets(5, 5, 5, 5));
 		JPlayer.setText("Display infomation of all players in the game\n" +"Example:\n"
 				+"Player 0:   logon: ?  Score: ?  keysHeld: ?  Location: ?\n");
 		panelControls.setBackground(Color.LIGHT_GRAY);
 		panelControls.setBorder(new TitledBorder(null, "Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panelControls.setBounds(449, 253, 135, 305);
 
 		frame.getContentPane().add(panelControls);
 		btnMoveUp.setFont(new Font("Tahoma", Font.BOLD, 11));
 		btnMoveUp.setBounds(52, 89, 30, 30);
 		btnMoveUp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int playerID = Integer.parseInt(textFieldPlayerID.getText());
 				if(playerID >= 0 && playerID <=3 )
 					gameServer.movePlayer(playerID,0);
 			}
 		});
 		panelControls.setLayout(null);
 		panelControls.add(btnMoveUp);
 		btnMoveUp.setMargin(new Insets(2, 2, 2, 2));
 		btnMoveUp.setEnabled(false);
 		btnMoveDown.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int playerID = Integer.parseInt(textFieldPlayerID.getText());
 				if(playerID >= 0 && playerID <=3 )
 					gameServer.movePlayer(playerID,1);
 			}
 		});
 		btnMoveDown.setBounds(52, 171, 30, 30);
 		panelControls.add(btnMoveDown);
 		btnMoveDown.setMargin(new Insets(2, 2, 2, 2));
 		btnMoveDown.setEnabled(false);
 		btnMoveLeft.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int playerID = Integer.parseInt(textFieldPlayerID.getText());
 				if(playerID >= 0 && playerID <=3 )
 					gameServer.movePlayer(playerID,2);
 			}
 		});
 		btnMoveLeft.setBounds(10, 130, 30, 30);
 		panelControls.add(btnMoveLeft);
 		btnMoveLeft.setMargin(new Insets(2, 2, 2, 2));
 		btnMoveLeft.setEnabled(false);
 		btnMoveRight.setBounds(95, 130, 30, 30);
 		panelControls.add(btnMoveRight);
 
 		btnMoveRight.setMargin(new Insets(2, 2, 2, 2));
 		btnMoveRight.setEnabled(false);
 		closeButton.setBounds(10, 55, 115, 23);
 		panelControls.add(closeButton);
 		closeButton.setMargin(new Insets(2, 5, 2, 5));
 		closeButton.setEnabled(false);
 		startButton.setBounds(10, 21, 115, 23);
 		panelControls.add(startButton);
 		startButton.setMargin(new Insets(2, 5, 2, 5));
 
 		textFieldPlayerID = new JTextField();
 		textFieldPlayerID.setText("1");
 		textFieldPlayerID.setEnabled(false);
 		textFieldPlayerID.setMargin(new Insets(5, 9, 5, 9));
 		textFieldPlayerID.setBounds(52, 130, 30, 30);
 		panelControls.add(textFieldPlayerID);
 		textFieldPlayerID.setColumns(10);
 		
 		JTextArea JControlHelp = new JTextArea();
 		JControlHelp.setMargin(new Insets(2, 5, 2, 5));
 		JControlHelp.setEditable(false);
 		JControlHelp.setText("Remote controls:\r\nEnter playerID\r\nPress the buttons \r\nto move around ");
 		JControlHelp.setBounds(10, 212, 115, 82);
 		panelControls.add(JControlHelp);
 		startButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					startButton.setEnabled(false);
 					closeButton.setEnabled(true);
 					btnMoveUp.setEnabled(true);
 					btnMoveDown.setEnabled(true);
 					btnMoveLeft.setEnabled(true);
 					btnMoveRight.setEnabled(true);
 					textFieldPlayerID.setEnabled(true);
 					gameServerThread = new GameServerThread();
 					gameServerThread.start();
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		closeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					//connectThread.stop();
 					startButton.setEnabled(true);
 					closeButton.setEnabled(false);
 					btnMoveUp.setEnabled(false);
 					btnMoveDown.setEnabled(false);
 					btnMoveLeft.setEnabled(false);
 					btnMoveRight.setEnabled(false);
 					textFieldPlayerID.setEnabled(false);
 					JKeyCode.setText("Displays all KeyCodes paired with their respective Node\n"
 							+ "KeyCode: 1234    Node: 16\nKeyCode pair: 1234[16]\n"
 							+"Each KeyCode can only be use once by each player");
					JTreasure.setText("Displays treasure chests\nlocated at each Node\n0: no treasure chest\n1: has treasure chest");
 					JPlayer.setText("Display infomation of all players in the game\n" +"Example:\n"
 							+"Player 0:   logon: ?  Score: ?  keysHeld: ?  Location: ?\n");
 					gameServer.disconnect();
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		btnMoveRight.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int playerID = Integer.parseInt(textFieldPlayerID.getText());
 				if(playerID >= 0 && playerID <=3 )
 					gameServer.movePlayer(playerID,3);
 			}
 		});
 	}
 
 
 	class GameServerThread extends Thread {
 
 		public void run() {
 			System.out.println("GUI initiatised!");
 			try {
 				gameServer = new GameServer();
 				gameServer.connect();
 			} catch (Exception e) {
 				// 
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	// Game Server: feedback to client's requests through UDP connections
 	class GameServer {
 
 		private  EventHandler eventHandler;
 		private  DatagramSocket socket ;
 
 		public void connect() throws Exception {
 
 			System.out.println("Game Server starting up... ");
 			JConsole.setText("Press Start to continue...\nGame Server starting up... ");
 
 			/* *** Initialization *** */
 			String reply = ""; // stores the reply info
 			eventHandler = new EventHandler();
 
 			JPlayer.setText(eventHandler.getPlayerInfoString());
 			JKeyCode.setText(eventHandler.getKeyCodeString());
 			JTreasure.setText(eventHandler.getTreasureInfoString());
 
 			//use DatagramSocket for UDP connection
 			//@SuppressWarnings("resource")
 			socket = new DatagramSocket(9001);
 			byte[] incomingBuffer = new byte[1000];
 
 			// Constantly receiving incoming packets
 			while (true)
 			{		
 				JConsole.setText(JConsole.getText() + "\nWaiting for incoming packet from game client... ");
 				DatagramPacket incomingPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length); 
 
 				socket.receive(incomingPacket);
 
 				// convert content of packet into a string 
 				String request = new String(incomingPacket.getData(), 0, incomingPacket.getLength() );
 
 				/* ----------------------------------------------------- */
 				// pass client request to event handler to compute results
 
 				reply = eventHandler.computeEventsReply(request);
 
 				/* ----------------------------------------------------- */
 
 				// convert reply into array of bytes (output buffer)
 				byte[] outputBuffer = new byte[1000];
 				outputBuffer = reply.getBytes();
 
 				// create reply packet using output buffer.
 				// Note: destination address/port is retrieved from incomingPacket
 				DatagramPacket outPacket = new DatagramPacket(outputBuffer, outputBuffer.length, incomingPacket.getAddress(), incomingPacket.getPort());
 
 				// finally, send the packet
 				socket.send(outPacket);
 				System.out.println("Sent reply: " + reply + " [GamerServer.java]");
 				JConsole.setText(JConsole.getText() + "\nSent reply: " + reply);
 				JPlayer.setText(eventHandler.getPlayerInfoString());
 				JTreasure.setText(eventHandler.getTreasureInfoString());
 			}
 
 		}
 
 		public void movePlayer(int playerID, int direction) {
 			eventHandler.movePlayer(playerID, direction);
 			JPlayer.setText(eventHandler.getPlayerInfoString());
 
 		}
 
 		String getPlayerInfoString(){	
 			return eventHandler.getPlayerInfoString();
 		}
 
 		String getTreasureInfoString(){	
 			return eventHandler.getTreasureInfoString();
 		}
 
 		public void disconnect() throws Exception {
 			JConsole.setText(JConsole.getText() + "\nClosing server\nPress Start to continue...");
 			//socket.disconnect();
 
 			socket.close();
 			//connectThread.destroy();
 		}
 
 	}
 }
