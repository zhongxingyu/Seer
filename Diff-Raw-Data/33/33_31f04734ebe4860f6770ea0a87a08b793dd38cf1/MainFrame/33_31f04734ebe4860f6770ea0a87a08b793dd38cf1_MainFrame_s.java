 package gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 
 import main.Engine;
 import main.Functions;
 import main.Message;
 
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.text.SimpleDateFormat;
 import java.util.LinkedList;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import java.awt.Font;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JCheckBox;
 
 public class MainFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane;
 	private JTextField inputField;
 	private Engine engine;
 	private JTextArea textArea;
 	private JTextArea keyField;
 
 	private JScrollPane scrollPane;
 
 	private JCheckBox encryptBox;
 
 	private String chatroom;
 	// Name of the last sender
 	private String lastSender = "";
 
 	// The only message to be preserved when the view is refreshed;
 	private String entryMessage;
 
 	public LinkedList<Message> messageList = new LinkedList<Message>();
 	private JTextField amountField;
 
 	public MainFrame(Engine e) {
 
 		engine = e;
 		chatroom = e.chatroom;
 
 		setTitle("CHATROOM : " + chatroom);
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(250, 100, 600, 450);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		textArea = new JTextArea();
 		textArea.setBounds(15, 15, 461, 367);
 		entryMessage = Functions.getTime(new SimpleDateFormat("(HH:mm:ss)"))
 				+ ": Welcome to chatroom \"" + chatroom + "\", " + engine.name;
 		textArea.append(entryMessage);
 		textArea.append("\n");
 
 		textArea.setEditable(false);
 
 		contentPane.add(textArea);
 		scrollPane = new JScrollPane(textArea);
 		scrollPane.setBounds(textArea.getBounds());
 		contentPane.add(scrollPane);
 
 		inputField = new JTextField();
 		inputField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 					send(inputField.getText());
 					inputField.setText("");
 				}
 			}
 		});
 		inputField.setBounds(15, 394, 490, 28);
 		contentPane.add(inputField);
 		inputField.setColumns(10);
 
 		keyField = new JTextArea();
 		keyField.setEditable(false);
 		keyField.setFont(new Font("AppleGothic", Font.ITALIC, 12));
 		keyField.setBounds(488, 60, 101, 270);
 		contentPane.add(keyField);
 
 		amountField = new JTextField();
 		amountField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 					engine.sendRequest(Integer.parseInt(amountField.getText()));
 					amountField.setText("");
 				} else {
 					if (arg0.getKeyCode() == KeyEvent.VK_0
 							|| arg0.getKeyCode() == KeyEvent.VK_1
 							|| arg0.getKeyCode() == KeyEvent.VK_2
 							|| arg0.getKeyCode() == KeyEvent.VK_3
 							|| arg0.getKeyCode() == KeyEvent.VK_4
 							|| arg0.getKeyCode() == KeyEvent.VK_5
 							|| arg0.getKeyCode() == KeyEvent.VK_6
 							|| arg0.getKeyCode() == KeyEvent.VK_7
 							|| arg0.getKeyCode() == KeyEvent.VK_8
 							|| arg0.getKeyCode() == KeyEvent.VK_9) {
 						if (amountField.getText().length() >= 2) {
 							amountField.setText(amountField.getText()
 									.substring(0, 1));
 						}
 					} else if (arg0.getKeyCode() != KeyEvent.VK_DELETE) {
 						amountField.setText("");
 					}
 				}
 			}
 		});
 		amountField.setBounds(488, 362, 101, 28);
 		contentPane.add(amountField);
 		amountField.setColumns(10);
 
 		JButton generateButton = new JButton("Generate");
 		generateButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				if (amountField.getText().length() > 0) {
 					if (amountField.getText().length() > 0) {
 						engine.sendRequest(Integer.parseInt(amountField
 								.getText()));
 						amountField.setText("");
 					}
 				}
 			}
 		});
 		generateButton.setFont(new Font("AppleGothic", Font.BOLD, 15));
 		generateButton.setBounds(488, 333, 101, 29);
 		contentPane.add(generateButton);
 
 		JLabel KeyLabel = new JLabel("Keys");
 		KeyLabel.setFont(new Font("AppleGothic", Font.BOLD | Font.ITALIC, 30));
 		KeyLabel.setBounds(495, 16, 89, 42);
 		contentPane.add(KeyLabel);
 
 		encryptBox = new JCheckBox("Encrypt");
 		encryptBox.setFont(new Font("AppleGothic", Font.PLAIN, 13));
 		encryptBox.setBounds(507, 397, 82, 23);
 		contentPane.add(encryptBox);
 
 		update();
 	}
 
 	// Sends the plaintext
 	private void send(String s) {
		if (s.length() > engine.keyList.size()) {
			textArea.append("\n");
			textArea.append("NOT ENOUGH KEYS TO ENCRYPT (Need "
					+ (s.length() - engine.keyList.size()) + " more keys)");
			textArea.append("\n");
			textArea.append("\n");
			return;
		}
		
 		s = Functions.clearIllegal(s);
				
 		Message m = new Message(s, engine.name);
 		if (encryptBox.isSelected()) {
 			// Encrypt
 			int l = s.length();
 			int[] key = new int[l];
 
 			engine.printKeys();
 			for (int i = 0; i < l; i++) {
 				key[i] = engine.keyList.get(0);
 				engine.keyList.poll();
 			}
 			System.out.println("Removed first " + l + "items of KeyList");
 			engine.printKeys();
 			update();
 
 			System.out.println("Encrypting " + s + " with: ");
 			for (int i : key) {
 				System.out.print(i);
 				System.out.print(",");
 			}
 			System.out.println();
 
 			String cipherText = Functions.encrypt(s, key);
 			System.out.println(s + " --> " + cipherText);
 			m.cipherText = cipherText;
 			m.encrypted = true;
 			engine.send(cipherText, true);
 		} else {
 			engine.send(s, false);
 		}
 		messageList.add(m);
 		update();
 	}
 
 	// Refreshes the view
 	public void update() {
 		textArea.setText(entryMessage);
 		textArea.append("\n");
 		for (Message m : messageList) {
 			if (lastSender.equalsIgnoreCase(m.sender)) {
 				textArea.append("\n");
 				textArea.append(m.format());
 			} else {
 				textArea.append("\n");
 				textArea.append("\n");
 				textArea.append(m.sender);
 				textArea.append("\n");
 				textArea.append(m.format());
 				lastSender = m.sender;
 			}
 		}
 		//Focuses
 		scrollPane.grabFocus();
 		
 		// Scrolls the scrollPane to the bottom
 		JScrollBar vertical = scrollPane.getVerticalScrollBar();
 		vertical.setValue(vertical.getMaximum());
 
 		keyField.setText("Keys: (" + engine.keyList.size() + ")");
 		keyField.append("\n");
 		for (int i : engine.keyList) {
 			keyField.append(String.valueOf(i));
 			keyField.append("\n");
 		}
 	}
 }
