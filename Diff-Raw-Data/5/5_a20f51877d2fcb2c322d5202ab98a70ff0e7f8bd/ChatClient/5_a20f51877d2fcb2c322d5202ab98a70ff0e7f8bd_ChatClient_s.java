 package chat;
 
 import com.alee.laf.WebLookAndFeel;
import hypeerweb.HyPeerWeb;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import static java.lang.Thread.sleep;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 
 /**
  * The Graphical User Interface for the Chat Client
  * TODO:
  *	- listener on close to clean-up, delete InceptionSegment, etc.
  *  - add buttons for actions sidebar (e.g. addNode, disconnect, deleteNode, etc)
  *  - write code for GraphTab/ListTab
  * @author isaac
  */
 public class ChatClient extends JFrame{
 	//Window title
 	private final String title = "HyPeerWeb Chat v0.1a";
 	//Window dimensions
 	private final int width = 750, height = 700;
 	//Upper vertical split percentage
 	private final double vsplitWeight = 0.8;
 	//Action bar's pixel width
 	private final int actionBarWidth = 150;
 	//Main pane (graph, list, chat) padding
 	private final int pad = 8;
 	private final EmptyBorder padding = new EmptyBorder(pad, pad, pad, pad);
 	
 	//GUI components
 	private ChatTab chat;
 		
 	public ChatClient(){
 		initGUI();
 		//Bind to a hypeerweb segment here...
 	}
 	
 	// <editor-fold defaultstate="collapsed" desc="GUI INITIALIZATION">
 	private void initGUI(){
 		//Initialize the window
 		setTitle(title);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(width, height);
 		setLocationRelativeTo(null);
 		
 		//Divide the upper bar of the window into two halves
 		//Left half will have node lists and the chat box; Right will be for actions
 		JPanel hSplit = new JPanel();
 		hSplit.setLayout(new BorderLayout());
 		add(hSplit);
 		
 		//Right half will be an actions bar
 		hSplit.add(initActionBar(), BorderLayout.EAST);
 		
 		//Left half will be a tabbed pane for graphing/chatting		
 		GraphTab x = new GraphTab();
 		JTabbedPane tabs = new JTabbedPane();
 		hSplit.add(tabs, BorderLayout.CENTER);
 		chat = new ChatTab(padding, title);
 		tabs.addTab("Chat", chat);
 		tabs.addTab("Node Graph", x);
 		tabs.addTab("Node List", new ListTab());
 		try {
			HyPeerWeb web = new HyPeerWeb(null, -1);
 			for (int i=0; i<100; i++)
 				web.addNode();
 			//x.draw(web.getFirstNode(), height);
 		} catch (Exception ex) {
 			System.out.println("Cannot hypeerweb stuff");
 		}
 	}
 	public JPanel initActionBar(){
 		JPanel bar = new JPanel();
 		JPanel box = initNetworkBox();
 		JPanel box2 = initConnectionBox();
 		
 		// <editor-fold defaultstate="collapsed" desc="Layout components in a stack">
 		GroupLayout stack = new GroupLayout(bar);
 		bar.setLayout(stack);
 		stack.setHorizontalGroup(
             stack.createParallelGroup()
 				.addComponent(box)
 				.addComponent(box2)
         );
         stack.setVerticalGroup(
             stack.createParallelGroup(GroupLayout.Alignment.CENTER)
 				.addGroup(stack.createSequentialGroup()
 					.addContainerGap(20, 20)
 	                .addComponent(box)
 					.addComponent(box2)
 					.addContainerGap(1000, Short.MAX_VALUE))
         );
 		// </editor-fold>
 		
 		return bar;
 	}
 	public JPanel initNetworkBox(){
 		//Create a network
 		JButton btnCreate = new JButton("Create Network");
 		
 		//Connect to a network
 		JButton btnJoin = new JButton("Join");
 		JButton btnWatch = new JButton("Watch");
 		
 		//Network connection configuration
 		final String t1 = "IP Address", t2 = "Port #";
 		final JTextField txtIP = new JTextField(t1);
 		final JTextField txtPort = new JTextField(t2);
 		smartTextField(t1, txtIP);
 		smartTextField(t2, txtPort);
 		CompoundBorder txtPad = new CompoundBorder(
 			txtIP.getBorder(),
 			(new EmptyBorder(2, 2, 2, 2))
 		);
 		txtIP.setBorder(txtPad);
 		txtPort.setBorder(txtPad);
 		
 		//Chat simulation
 		JButton testBtn = new JButton("Run Simulation");
 		testBtn.setPreferredSize(new Dimension(150, 25));
 		testBtn.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				testChatRoom();
 			}
 		});
 		
 		// <editor-fold defaultstate="collapsed" desc="Layout components in grid">
 		JPanel box = new JPanel();
 		box.setBorder(padding);
 		box.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.ipadx = 3;		c.ipady = 3;
 		c.gridwidth = 2;	c.gridheight = 1;
 		c.gridx = 0;		c.gridy = 0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0, 0, 4, 0);
 		box.add(btnCreate, c);
 		c.gridy++;
 		c.gridwidth = 1;
 		c.insets.right = 4;
 		box.add(btnJoin, c);
 		c.gridx = 1;
 		c.insets.right = 0;
 		box.add(btnWatch, c);
 		c.gridwidth = 2;
 		c.gridy++;
 		c.gridx = 0;
 		c.insets.top = 2;
 		box.add(txtIP, c);
 		c.gridy++;
 		c.insets.top = 0;
 		box.add(txtPort, c);
 		c.gridy++;
 		c.insets.top = 20;
 		box.add(testBtn, c);
 		// </editor-fold>
 		
 		return box;
 	}
 	public JPanel initConnectionBox(){		
 		//Segment name
 		Font bold = new Font("SansSerif", Font.BOLD, 12);
 		JLabel lblSeg = new JLabel("Subnet Name:");
 		lblSeg.setFont(bold);
 		JTextField txtSeg = new JTextField();
 		
 		//Username
 		JLabel lblName = new JLabel("Chat Alias:");
 		lblName.setFont(bold);
 		JTextField txtName = new JTextField();
 		
 		//Disconnect button
 		JButton btn = new JButton("Disconnect");
 		btn.setPreferredSize(new Dimension(150, 25));
 		
 		// <editor-fold defaultstate="collapsed" desc="Layout components in grid">
 		JPanel box = new JPanel();
 		box.setBorder(padding);
 		box.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.ipadx = 3;		c.ipady = 3;
 		c.gridwidth = 2;	c.gridheight = 1;
 		c.gridx = 0;		c.gridy = 0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0, 0, 2, 0);
 		box.add(lblSeg, c);
 		c.gridy++;
 		box.add(txtSeg, c);
 		c.gridy++;
 		box.add(lblName, c);
 		c.gridy++;
 		c.insets.bottom = 4;
 		box.add(txtName, c);
 		c.gridy++;
 		box.add(btn, c);
 		// </editor-fold>
 		
 		return box;
 	}
 	private void smartTextField(final String defVal, final JTextField txt){
 		txt.addFocusListener(new FocusListener(){
 			@Override
 			public void focusGained(FocusEvent e) {
 				if (txt.getText().equals(defVal))
 					txt.setText(null);
 			}
 			@Override
 			public void focusLost(FocusEvent e) {
 				if (txt.getText().isEmpty())
 					txt.setText(defVal);
 			}
 		});
 	}
 	
 	public static void main(String args[]) {
 		//Load the look-and-feel
 		/*
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
 			System.out.println("Could not load look-and-feel to start the GUI");
 		}
 		//*/
 		WebLookAndFeel.install();
 
 		//Start up the window
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				new ChatClient().setVisible(true);
 			}
 		});
 	}
 	//</editor-fold>
 	
 	//ACTIONS
 	public void testChatRoom(){
 		(new Thread(new ChatSimulation())).start();
 	}
 	
 	private class ChatSimulation implements Runnable{
 		@Override
 		public void run() {
 			int delay = 3000;
 			try {
 				chat.writeStatus("Beginning chat simulation");
 				chat.updateUser(0, "user91023");
 				sleep(delay);
 				chat.receiveMessage(0, -1, "Hello world");
 				sleep(delay);
 				chat.updateUser(0, "isaac");
 				sleep(delay);
 				chat.receiveMessage(0, -1, "blue babies");
 				sleep(delay);
 				chat.receiveMessage(0, -1, "Someone join me.... I'm getting bored");
 				sleep(delay);
 				chat.updateUser(1, "John");
 				sleep(delay);
 				chat.receiveMessage(0, 1, "Dude what is up");
 				sleep(delay);
 				chat.receiveMessage(1, 0, "Hey, I like your style bro");
 				sleep(delay);
 				chat.updateUser(0, null);
 				sleep(delay);
 				chat.updateUser(1, null);
 				chat.writeStatus("Chat simulation finished");
 			} catch (InterruptedException ex) {
 				Logger.getLogger(ChatTab.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 	}
 	
 }
