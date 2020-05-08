 package mbakhoff;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Map;
 import java.util.HashMap;
 
 public class Gui implements MEventListener {
 	
 	public static void main(String[] args) {
 		ConnectionManager mgr = new ConnectionManager();
 		new Gui(mgr);
 		mgr.mainLoop();
 	}
 
 	private ConnectionManager mgr = null;
 	private Map<String,GuiTab> tabMap = new HashMap<String,GuiTab>();
 	private CLI dummyConsole = null;
 	
 	private JFrame frame = null;
 	private JTabbedPane pane = null;
 	private JPanel ctl = null;
 	
 	// control panel elements
 	private DefaultListModel mdl = null; 
 	private JTextArea log = null;
 	private JTextField tfCmd = null;
 	private JList list = null;
 	private JTextField tfNick = null;
 	private JTextField tfAddr = null;
 	private JButton bAdd = null;
 	
 	public Gui(ConnectionManager mgr) {
 		this.mgr = mgr;
 		EventDispatch.get().addListener(this);
 		// init
 		initComponents();
 		// focus on textfield
 		tfNick.requestFocus();
 	}
 			
 	public void connectionRequestEvent() {
 		String nick = tfNick.getText();
 		String addr = tfAddr.getText();
 		tfNick.setText("");
 		tfAddr.setText("");
 		EventDispatch.get().debug("gui connectionReq: "+nick+":"+addr);
 		mgr.mapNick(nick, addr);
 		// refocus
 		tfNick.requestFocus();
 	}
 
 	// MEventListener
 	public void messageReceived(String nick, String message) {
 		log.append(nick+" says: "+message+"\n");
 		GuiTab tab = tabMap.get(nick);
 		if (tab == null) {
 			tab = addTab(nick);
 		}
 		tab.messageReceived(message);
 	}
 
 	// MEventListener
 	public void messageDebug(String message) {
 		log.append("DEBUG: "+message+"\n");
 	}
 	
 	public void messageConsole(String msg) {
 		log.append(msg+"\n");
 	}
 
 	// MEventListener
 	public void peeringEvent() {
 		mdl.clear();
 		for (String s : mgr.getMap()) {
 			mdl.addElement(s);
 		}
 	}
 	
 	protected GuiTab addTab(String nick) {
 		GuiTab t = new GuiTab(mgr, this, nick);
 		pane.add(nick, t);
 		pane.setTabComponentAt(pane.indexOfComponent(t), createTabComponent(t));
 		tabMap.put(nick, t);
 		return t;
 	}
 	
 	protected void removeTab(GuiTab tab) {
 		EventDispatch.get().debug("gui: removing tab for "+tab.getNick());
 		tabMap.remove(tab.getNick());
 		pane.remove(tab);
 	}
 	
 	protected void peerSelected(String peer) {
 		EventDispatch.get().debug("gui selected peer: "+peer);
 		GuiTab tab = tabMap.get(peer);
 		if (tab == null) {
 			tab = addTab(peer);
 		}
 		pane.setSelectedComponent(tab);
 	}
 	
 	protected void initComponents() {
 		// frame
 		frame = new JFrame("oop-messenger");
 		pane = new JTabbedPane();
 		// control panel
 		ctl = new JPanel();
 		peerviewInit(ctl);
 		peerviewLayout(ctl);
 		pane.addTab("Peers", ctl);
 		// frame layout
 		frame.add(pane);
 		frame.pack();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 	}
 	
 	protected void peerviewInit(JPanel p) {
 		// log textarea
 		log = new JTextArea("");
 		log.setEditable(false);
 		dummyConsole = new CLI(mgr, false);
 		tfCmd = new JTextField();
 		tfCmd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				 String cmd = tfCmd.getText().trim();
 				 tfCmd.setText("");
 				 dummyConsole.parse(cmd);
 			}			
 		});
 		// list + model
 		mdl = new DefaultListModel();
 		list = new JList(mdl);
 		//list.setCellRenderer(new MyCellRenderer());
 		list.setFixedCellWidth(150);
 		list.addKeyListener(new KeyListener() {
 			public void keyTyped(KeyEvent e) {}
 			public void keyReleased(KeyEvent e) {}
 			public void keyPressed(KeyEvent ev) {
 				if (ev.getKeyCode() == KeyEvent.VK_ENTER)
 					peerSelected((String)list.getSelectedValue());
 			}
 		});
 		list.addMouseListener(new MouseListener() {
 			public void mousePressed(MouseEvent e) {}
 			public void mouseReleased(MouseEvent e) {}
 			public void mouseEntered(MouseEvent e) {}
 			public void mouseExited(MouseEvent e) {}
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() > 1) {
 					peerSelected((String)list.getSelectedValue());
 				}
 			}
 		});
 		tfNick = new JTextField();
 		tfNick.setColumns(8);
 		tfNick.setToolTipText("Insert nickname");
 		tfAddr = new JTextField();
 		tfAddr.setColumns(10);
 		tfAddr.setToolTipText("Insert ip/hostname");
 		tfAddr.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				connectionRequestEvent();
 			}
 		});
 		bAdd = new JButton("Connect");
 		bAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				connectionRequestEvent();
 			}
 		});
 	}
 	
 	protected void peerviewLayout(JPanel p) {
 		// enable JList, JTextArea scrolling
 		JScrollPane listWrapper = new JScrollPane();
 		listWrapper.setViewportView(list);
 		listWrapper.setBorder(BorderFactory.createTitledBorder("Peers"));
 		JScrollPane scrolledLog = new JScrollPane();
 		scrolledLog.setViewportView(log);
 		scrolledLog.setHorizontalScrollBarPolicy(
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrolledLog.setPreferredSize(new Dimension(600, 400));
 		JPanel logAndConsole = new JPanel(new BorderLayout());
		logAndConsole.add(scrolledLog, BorderLayout.CENTER);
 		logAndConsole.add(tfCmd, BorderLayout.SOUTH);
 		logAndConsole.setBorder(BorderFactory.createTitledBorder("Console"));
 		// haha edu desifeerimisel
 		GroupLayout gl = new GroupLayout(p);
 		p.setLayout(gl);
 		gl.setAutoCreateContainerGaps(true);
 		gl.setAutoCreateGaps(true);
 		gl.linkSize(SwingConstants.VERTICAL, tfNick, tfAddr, bAdd);
 		gl.setHorizontalGroup(
 				gl.createSequentialGroup()
 					.addGroup(gl.createParallelGroup()
 							.addComponent(listWrapper)
 							.addGroup(gl.createSequentialGroup()
 									.addComponent(tfNick)
 									.addComponent(tfAddr)
 									.addComponent(bAdd)))
 					.addComponent(logAndConsole));
 		gl.setVerticalGroup(
 				gl.createParallelGroup()
 					.addGroup(gl.createSequentialGroup()
 							.addComponent(listWrapper)
 							.addGroup(gl.createParallelGroup()
 									.addComponent(tfNick)
 									.addComponent(tfAddr)
 									.addComponent(bAdd)))
 					.addComponent(logAndConsole));
 	}
 	
 	protected static Icon getCloseIcon() {
 		return new ImageIcon("stock-close.png");
 	}
 	
 	protected Component createTabComponent(final GuiTab tab) {
 		JPanel root = new JPanel(new BorderLayout(1,0));
 		root.setOpaque(false);
 		JButton closeButton = new JButton(getCloseIcon());
 		closeButton.setContentAreaFilled(false);
 		closeButton.setMargin(new Insets(1,-1,-1,-1));
 		closeButton.setBorderPainted(false);
 		closeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				removeTab(tab);
 			}
 		});
 		root.add(new JLabel(tab.getNick()), BorderLayout.WEST);
 		root.add(closeButton, BorderLayout.EAST);
 		return root;
 	}
 		
 }
