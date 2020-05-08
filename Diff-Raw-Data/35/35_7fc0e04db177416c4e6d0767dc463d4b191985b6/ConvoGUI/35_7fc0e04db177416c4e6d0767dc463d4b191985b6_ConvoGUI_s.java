 package ui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import main.Client;
 import messages.RequestMessage;
 import messages.TextMessage;
 import messages.TypingMessage;
 
 public class ConvoGUI extends JFrame implements KeyListener  {
 
 //	private JLabel friendIs;
 	private JLabel status; // TODO: update whether typing, idle, etc!!!
 	private JTextArea convo;
 	private JButton enter;
 	private JTextField newText;
 	private final static String newline = "\n";
 	private JScrollPane scrollPane;
 
 	private int roomID;
 	private String fromUsername;
 	private JLabel pugLabel;
 	
 	private String baseTitle;
 	private boolean otherText = false; // true if the other person has sent some text
 	
 	public long lastKeyPress; 
 	
 	public ConvoGUI(String fromUsername, int roomID) {
 		this.fromUsername = fromUsername;
 		this.baseTitle = "Room " + roomID;
 		this.roomID = roomID;
 		
 		enter = new JButton("Enter");
 		enter.setName("Enter");
 		enter.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				enterTextFromField();
 			}
 		});
 		
 		java.net.URL imageURL = LoginGUI.class.getResource("img/pugCslice.png");
 		ImageIcon pugPic = new ImageIcon(imageURL);
 		pugLabel = new JLabel(pugPic);
 
 
 		newText = new JTextField();
 		newText.setName("newText");
         newText.addKeyListener(this);
 		newText.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent a) {
 				enterTextFromField();
 			}
 		});
 		
 		lastKeyPress = System.currentTimeMillis();
 		
 		convo = new JTextArea();
 		convo.setName("Convo");
 		convo.setEditable(false);
         scrollPane = new JScrollPane(convo);
 
         status = new JLabel("Is Idle");  // TODO: update whether typing, idle, etc!!!
         status.setName("Status");
         
         /*
         friendIs = new JLabel(" "+fromUsername+":");
         friendIs.setName("friendIs");
 */
 
 		setPreferredSize(new Dimension(450, 450));
 		createAndShowGUI();
 		
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if(e.getKeyCode() == 10){
 	        Client.getQueue().offer(new TypingMessage(fromUsername, roomID, TypingMessage.types.NOTHING));
 			return;
 		}
     	new Thread(new Runnable(){
     		public void run(){
     			if(System.currentTimeMillis()-lastKeyPress > 1000)
    					Client.getQueue().offer(new TypingMessage(fromUsername,	roomID,	TypingMessage.types.TYPING));
     			
     			lastKeyPress = System.currentTimeMillis();
     			
     			try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
     		
     			if(System.currentTimeMillis()-lastKeyPress > 900)
     				Client.getQueue().offer(new TypingMessage(fromUsername,
     														  roomID,
     														  newText.getText().equals("") ? TypingMessage.types.NOTHING : 
     															  							 TypingMessage.types.ENTERED));
     		}
     	}).start();
     }
 
 	public void setStatus(TypingMessage message) {
 		setTitle(baseTitle + message.toTitle());
 	}
 	private void enterTextFromField(){
 		String toAdd = fromUsername + ": "+ newText.getText() + newline;
 		convo.append(toAdd);
 		Client.getQueue().offer(new TextMessage(fromUsername, roomID, newText.getText()));
 		newText.setText("");
 	}
 
 	public void handleTextMessage(TextMessage message) {
 		convo.append(message.getFromUsername() + ": " + message.getText() + newline);
 		if(!otherText) {
 			this.setVisible(true);
 			otherText = true;
 		}
 		System.out.println(message.getFromUsername() + "|" + message.getText() + " just sent a message (enterText)");
 	}
 	
 	public void createAndShowGUI() {
 
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 
 		layout.setHorizontalGroup(layout
 				.createSequentialGroup()
 					.addGroup(layout.createParallelGroup()
 							//.addComponent(friendIs)
 							.addComponent(status)
 							.addComponent(pugLabel))
 					.addGroup(layout.createParallelGroup()
 								.addGroup(layout.createSequentialGroup().addComponent(scrollPane))
 								.addGroup(
 										layout.createSequentialGroup().addComponent(newText)
 										.addComponent(enter))));
 
 		layout.setVerticalGroup(layout
 				.createParallelGroup()
 				.addGroup(layout.createSequentialGroup()
 						//.addComponent(friendIs)
 						.addComponent(status)
 						.addComponent(pugLabel))
 				.addGroup(layout.createSequentialGroup()
 							.addGroup(layout.createParallelGroup().addComponent(scrollPane))
 							.addGroup(
 									layout.createParallelGroup().addComponent(newText)
 									.addComponent(enter))));
 
 		layout.linkSize(SwingConstants.HORIZONTAL, status, pugLabel);
 		layout.linkSize(SwingConstants.VERTICAL, newText, enter);
 
 		setTitle(baseTitle);
 		pack();
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
 	}
 
 	public static void main(final String[] args) {
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 
 				ConvoGUI main = new ConvoGUI("chase", 0);
 
 				main.setVisible(true);
 
 			}
 		});
 	}
 	
 	public boolean hasOtherText() {
 		return otherText;
 	}
 
 }
