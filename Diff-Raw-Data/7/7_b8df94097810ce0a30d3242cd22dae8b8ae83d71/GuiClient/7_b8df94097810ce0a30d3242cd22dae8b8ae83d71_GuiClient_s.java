 package com.garethmurphy.simplechat;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.UIManager;
 import javax.swing.JButton;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.ImageIcon;
 import javax.swing.JScrollPane;
 
 
 public class GuiClient {
 	
 	// Change these settings for your own server setup!
 	private String ip = "127.0.0.1";
 	private int port = 5000;
 	
 	private BufferedReader in;
 	private PrintWriter out;
 
 	private JFrame frmSimplechat;
 	private JTextField textField;
 	private JButton btnNewButton;
 	private JTextPane textPane;
 	private JScrollPane scrollPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					GuiClient window = new GuiClient();
 					window.frmSimplechat.setVisible(true);
 					window.textField.requestFocusInWindow();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public GuiClient() {
 		initialize();
 		try {
 			setupSocket();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmSimplechat = new JFrame();
 		frmSimplechat.setTitle("SimpleChat");
 		frmSimplechat.setBounds(100, 100, 450, 300);
 		frmSimplechat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{434, 0, 0};
 		gridBagLayout.rowHeights = new int[]{229, 28, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		frmSimplechat.getContentPane().setLayout(gridBagLayout);
 		
 		textField = new JTextField();
 		textField.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String msg = textField.getText();
 				sendMsg(msg);
 				textField.setText("");
 				textField.requestFocus();
 			}
 		});
 		
 		scrollPane = new JScrollPane();
 		scrollPane.setFocusTraversalKeysEnabled(false);
 		scrollPane.setFocusable(false);
 		scrollPane.setRequestFocusEnabled(false);
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridwidth = 2;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 0;
 		frmSimplechat.getContentPane().add(scrollPane, gbc_scrollPane);
 		
 		textPane = new JTextPane();
 		textPane.setFocusTraversalKeysEnabled(false);
 		textPane.setFocusCycleRoot(false);
 		textPane.setRequestFocusEnabled(false);
 		scrollPane.setViewportView(textPane);
 		textPane.setEditable(false);
 		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 5, 5, 0);
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.anchor = GridBagConstraints.NORTH;
 		gbc_textField.gridx = 0;
 		gbc_textField.gridy = 1;
 		frmSimplechat.getContentPane().add(textField, gbc_textField);
 		textField.setColumns(10);
 		
 		btnNewButton = new JButton("Send");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String msg = textField.getText();
 				sendMsg(msg);
 				textField.setText("");
 				textField.requestFocus();
 			}
 		});
 		btnNewButton.setIcon(new ImageIcon(GuiClient.class.getResource("/com/garethmurphy/simplechat/res/balloon-white-left.png")));
 		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
 		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
 		gbc_btnNewButton.gridx = 1;
 		gbc_btnNewButton.gridy = 1;
 		frmSimplechat.getContentPane().add(btnNewButton, gbc_btnNewButton);
 	}
 	
 	private void setupSocket() throws IOException {
 		Socket socket = new Socket(ip, port);
 		
 		// Set up input...
 		InputStreamReader reader = new InputStreamReader(
 				socket.getInputStream());
 		in = new BufferedReader(reader);
 		
 		Thread listenThread = new Thread(new MsgListener());
 		listenThread.start();
 		
 		// And output...
 		out = new PrintWriter(socket.getOutputStream());
 	}
 	
 	private void sendMsg(String msg) {
 		out.println(msg);
 		out.flush();
 	}
 	
 	class MsgListener implements Runnable {
 		@Override
 		public void run() {
 			String msg;
 			
 			try {
 				while((msg = in.readLine()) != null) {
 					textPane.setText(textPane.getText() + "\n" + msg);
 					
 					// Auto-scrolls the JTextPane
 					textPane.setCaretPosition(textPane.getText().length());
 				}
 			} catch(IOException e) {
 				e.printStackTrace();
 			}			
 		}
 	}
 }
