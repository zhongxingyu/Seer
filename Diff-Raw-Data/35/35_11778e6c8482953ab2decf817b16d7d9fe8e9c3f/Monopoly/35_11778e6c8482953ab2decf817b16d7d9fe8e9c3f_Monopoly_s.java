 package com.sirolf2009.monopolie;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import jxl.Sheet;
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 
 import com.sirolf2009.monopolie.card.Cards;
 import com.sirolf2009.monopolie.card.ICard;
 import com.sirolf2009.monopolie.communication.Connector;
 import com.sirolf2009.monopolie.communication.ICommunicator;
 import com.sirolf2009.monopolie.communication.Receiver;
 import com.sirolf2009.monopolie.communication.Sender;
 import com.sirolf2009.monopolie.communication.packet.PacketTeam;
 import com.sirolf2009.monopolie.street.Street;
 import com.sirolf2009.monopolie.team.Team;
 
 public class Monopoly implements ICommunicator {
 
 	private JFrame frame;
 	private JPanel menus;
 	private JPanel menuConnect;
 	private JPanel menuTeam;
 	private JPanel menuGame;
 	private JTextField txtIP;
 	private JComboBox<Team> boxTeamColor;
 	private NextMenu nextmenu;
 	private JLabel map;
 	private JScrollPane scrMap;
 	private JLabel lblTeamColor;
 	private JLabel lblMoney;
 	private JList<Street> lstOwnedStreets;
 	private JLabel lblDraws;
 	private JLabel lblDrawTimer;
 	private JLabel lblDrawCooldown;
 	private JLabel lblInJail;
 	private JButton btnJail;
 	private JButton btnDraw;
 	private int drawTimer;
 	private int drawCooldown;
 	
 	private Cards cards;	
 	public int menuID = 1;
 	private static Team[] teams;
 	public static Team localTeam;
 	private Sender sender;
 	private Receiver receiver;
 	public Map<String, Street> streets = new HashMap<String, Street>();
 	public Map<String, JStreetButton> streetButtons = new HashMap<String, JStreetButton>();
 
 	private Socket socket;
 	public Connector connector;
 	public static int port = 1941;
 	private boolean isConnected;
 
 	public static Monopoly instance;
 
 	public Monopoly(String[] args) {
 		instance = this;
 		try {
 			init(args);
 			menuLoop();
 			gameLoop();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		} catch (BiffException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void gameLoop() throws InterruptedException {
 		showNextMenu();
 		updateLocalTeamInfo();
 		while(true) {
 			drawTimer--;
 			if(drawTimer <= 0) {
				drawCard();
 				drawTimer = 15;
 			}
 			if(drawCooldown > 0) {
 				drawCooldown--;
 			}
 			updateLocalTeamInfo();
 			Thread.sleep(60000);
			//1000*60=60 000
 		}
 	}
 
 	private void menuLoop() throws InterruptedException {
 		do {
 			menuID = 1;
 			showNextMenu();
 			while(menuID == 1) { Thread.sleep(1); }
 			showNextMenu();
 			while(menuID == 2) { Thread.sleep(1); }
 			localTeam = teams[boxTeamColor.getSelectedIndex()];
 		} while(!connect(txtIP.getText()));
 	}
 
 	private void init(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, URISyntaxException, BiffException {
 		if(args.length>0) {
 			port = Integer.parseInt(args[0]);
 		}
 		
 		cards = new Cards();
 		drawTimer = 15;
 		
 		nextmenu = new NextMenu(this);
 		for(LookAndFeelInfo look : UIManager.getInstalledLookAndFeels()) {
 			if(look.getName() == "Nimbus") {
 				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
 				break;
 			}
 		}
 		BufferedImage imgJotijota = ImageIO.read(getClass().getResourceAsStream("/images/jotijota.png"));
 		BufferedImage imgMonopoly = ImageIO.read(getClass().getResourceAsStream("/images/monopoly.png"));
 		BufferedImage imgMap = ImageIO.read(getClass().getResourceAsStream("/images/zalmplaat.png"));
 
 		frame = new JFrame("Monopolie Client");
 		frame.setSize(1200, 600);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		//Connect menu
 		menuConnect = new JPanel();
 		menuConnect.setLayout(null);
 		txtIP = new JTextField("localhost");
 		txtIP.setBounds(500, 200, 200, 50);
 		JButton btnConnect = new JButton("OK");
 		btnConnect.setBounds(500, 300, 200, 50);
 		btnConnect.addActionListener(nextmenu);
 		JLabel lblJotiLogo1 = new JLabel(new ImageIcon(imgJotijota.getScaledInstance(100, 150, 0)));
 		lblJotiLogo1.setBounds(0, 0, 100, 150);
 		JLabel lblMonopolyLogo1 = new JLabel(new ImageIcon(imgMonopoly.getScaledInstance(300, 200, 0)));
 		lblMonopolyLogo1.setBounds(1200-300-15, 0, 300, 200);
 		menuConnect.add(txtIP);
 		menuConnect.add(btnConnect);
 		menuConnect.add(lblJotiLogo1);
 		menuConnect.add(lblMonopolyLogo1);
 
 		//Choose team menu
 		menuTeam = new JPanel();
 		menuTeam.setLayout(null);
 		boxTeamColor = new JComboBox<Team>(teams);
 		boxTeamColor.setBounds(500, 200, 200, 50);
 		JButton btnColor = new JButton("OK");
 		btnColor.setBounds(500, 300, 200, 50);
 		btnColor.addActionListener(nextmenu);
 		JLabel lblJotiLogo2 = new JLabel(new ImageIcon(imgJotijota.getScaledInstance(100, 150, 0)));
 		lblJotiLogo2.setBounds(0, 0, 100, 150);
 		JLabel lblMonopolyLogo2 = new JLabel(new ImageIcon(imgMonopoly.getScaledInstance(300, 200, 0)));
 		lblMonopolyLogo2.setBounds(1200-300-15, 0, 300, 200);
 		menuTeam.add(boxTeamColor);
 		menuTeam.add(btnColor);
 		menuTeam.add(lblJotiLogo2);
 		menuTeam.add(lblMonopolyLogo2);
 
 		//Game Menu frame width:1200 height:600
 		menuGame = new JPanel();
 		menuGame.setLayout(null);
 
 		map = new JLabel(new ImageIcon(imgMap));
 
 		Workbook workbook = Workbook.getWorkbook(getClass().getResourceAsStream("/Zalmplaat.xls"));
 		Sheet sheet = workbook.getSheet(0);
 		for(int i = 0; i < 69; i++) {
 			Street street = new Street();
 			street.name = sheet.getCell(0, i).getContents();
 			street.x = Integer.parseInt(sheet.getCell(1, i).getContents());
 			street.y = Integer.parseInt(sheet.getCell(2, i).getContents());
 			street.size = 0; // Integer.parseInt(sheet.getCell(3, i).getContents());
 			JStreetButton button = new JStreetButton(street);
 			button.setBounds(street.x-10, street.y-10, 20, 20);
 			map.add(button);
 			streets.put(street.name, street);
 			streetButtons.put(street.name, button);
 		}
 		scrMap = new JScrollPane(map, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scrMap.setBounds(0, 0, 1000, 570);
 		
 		JPanel test = new JPanel();
 		test.setBounds(1015, 0, 150, 600);
 		lblTeamColor = new JLabel("EASTER EGG");
 		//lblTeamColor.setBounds(1000+15, 15, 150, 20);
 		lblMoney = new JLabel();
 		//lblMoney.setBounds(1000+15, 15+20, 150, 20);
 		lblDraws = new JLabel();
 		//lblDraws.setBounds(1000+15, 15+20*2, 150, 20);
 		lblDrawTimer = new JLabel();
 		lblDrawCooldown = new JLabel();
 		//lblDrawTimer.setBounds(1000+15, 15+20*3, 150, 20);
 		JLabel lblOwnedStreets = new JLabel("Straten");
 		//lblOwnedStreets.setBounds(1000+15, 15+20*4, 150, 20);
 		lstOwnedStreets = new JList<Street>();
 		JScrollPane scrLstOwnedStreets = new JScrollPane(lstOwnedStreets);
 		lstOwnedStreets.setSize(150, 200);
 		//scrLstOwnedStreets.setBounds(1000+15, 15+20*5, 150, 200);
 		btnDraw = new JButton("Kans");
 		btnDraw.addActionListener(new Draw(this));
 		lblInJail = new JLabel();
 		btnJail = new JButton("Uit de gevangenis 50");
 		btnJail.addActionListener(new BuyOut(this));
 
 		menuGame.add(scrMap);
 		test.add(lblTeamColor);
 		test.add(lblMoney);
 		test.add(lblOwnedStreets);
 		test.add(scrLstOwnedStreets);
 		test.add(lblInJail);
 		test.add(lblDraws);
 		test.add(lblDrawTimer);
 		test.add(lblDrawCooldown);
 		test.add(btnDraw);
 		test.add(btnJail);
 		menuGame.add(test);
 //		menuGame.add(scrMap);
 //		menuGame.add(lblTeamColor);
 //		menuGame.add(lblMoney);
 //		menuGame.add(lblOwnedStreets);
 //		menuGame.add(scrLstOwnedStreets);
 //		menuGame.add(lblDraws);
 //		menuGame.add(lblDrawTimer);
 
 		menus = new JPanel(new CardLayout());
 		menus.add(menuConnect, 1+"");
 		menus.add(menuTeam, 2+"");
 		menus.add(menuGame, 3+"");
 
 		frame.getContentPane().add(menus);
 		frame.setVisible(true);
 	}
 	
 	public void updateLocalTeamInfo() {
 		lblTeamColor.setText(localTeam.toString());
 		lblTeamColor.setForeground(localTeam.teamColor);
 		lblMoney.setText("Geld: "+localTeam.money);
 		DefaultListModel<Street> model = new DefaultListModel<Street>();
 		for(Street street : localTeam.getOwnedStreets(streets.values())) {
 			model.addElement(street);
 		}
 		lstOwnedStreets.setModel(model);
 		lblInJail.setText("In de gevangenis: "+localTeam.inJail);
 		lblDraws.setText("Kanskaarten: "+localTeam.draws);
 		lblDrawTimer.setText("Auto kans: "+drawTimer);
 		lblDrawCooldown.setText("Kans cooldown: "+drawCooldown);
 		btnJail.setVisible(localTeam.inJail);
 	}
 	
 	public void updateStreetButtons() {
 		for(JStreetButton button : streetButtons.values()) {
 			button.street = streets.get(button.street.name);
 			button.initPopupWithInvoke();
 			button.repaint();
 		}
 	}
 
 	public boolean connect(String host) {
 		try {
 			System.out.println("connecting to "+host+":"+port);
 			socket = new Socket(host, port);
 			sender = new Sender(socket);
 			new Thread(sender, "client sender").start();
 			receiver = new Receiver(this);
 			new Thread(receiver, "client receiver").start();
 			sender.send(new PacketTeam(localTeam));
 			isConnected = true;
 			System.out.println("connected");
 			return true;
 		} catch (IOException e) {
 			System.err.println("Could not connect");
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
	public void drawCard() {
		if(localTeam.draws <= 0 || drawCooldown > 0)
			return;
 		ICard card = cards.getRandomCard();
 		final JPopupMenu cardPopup = new JPopupMenu("Kans");
 		cardPopup.add(new JLabel(card.description()));
 		JButton ok = new JButton("OK");
 		ok.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				cardPopup.setVisible(false);
 			}
 		});
 		cardPopup.add(ok);
 		cardPopup.setLocation(400, 300);
 		cardPopup.setVisible(true);
 		card.draw(localTeam);
 		localTeam.draws--;
 		drawCooldown = 5;
 		drawTimer = 15;
 		getSender().send(new PacketTeam(localTeam));
 		updateLocalTeamInfo();
 	}
 	
 	public void setStreet(String name, Street street) {
 		streets.put(name, street);
 	}
 	
 	public Street getStreet(String name) {
 		return streets.get(name);
 	}
 
 	@Override
 	public Sender getSender() {
 		return sender;
 	}
 
 	@Override
 	public Receiver getReceiver() {
 		return receiver;
 	}
 
 	@Override
 	public Socket getSocket() {
 		return socket;
 	}
 
 	@Override
 	public boolean isRemote() {
 		return false;
 	}
 
 	@Override
 	public boolean isConnected() {
 		return isConnected;
 	}
 	
 	@Override
 	public void disconnect() {
 		isConnected = false;
 	}
 	
 	public void showNextMenu() {
 		CardLayout cl = (CardLayout)(menus.getLayout());
 		cl.show(menus, menuID+"");
 	}
 	
 	public Team getTeamFromColor(Color color) {
 		for(Team team : teams) {
 			if(team.teamColor.equals(color)) {
 				return team;
 			}
 		}
 		return null;
 	}
 
 	static {
 		teams = new Team[] {new Team(Color.red), new Team(Color.green), new Team(Color.blue), new Team(Color.cyan), new Team(Color.magenta), new Team(Color.orange), new Team(Color.yellow), new Team(Color.pink)};
 	}
 
 	public static void main(String[] args) {
 		new Monopoly(args);
 	}
 
 
 }
 
 class NextMenu implements ActionListener {
 
 	private Monopoly client;
 
 	public NextMenu(Monopoly client) {
 		this.client = client;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getID() == 1001) {
 			System.out.println("toggling menu");
 			client.menuID++;
 		}
 	}
 
 }
 
 class Draw implements ActionListener {
 
 	private Monopoly client;
 
 	public Draw(Monopoly client) {
 		this.client = client;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getID() == 1001) {
			client.drawCard();
 		}
 	}
 
 }
 
 class BuyOut implements ActionListener {
 
 	private Monopoly client;
 
 	public BuyOut(Monopoly client) {
 		this.client = client;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getID() == 1001) {
 			if(Monopoly.localTeam.money < 50 || !Monopoly.localTeam.inJail)
 				return;
 			Monopoly.localTeam.money -= 50;
 			Monopoly.localTeam.inJail = false;
 			client.getSender().send(new PacketTeam(Monopoly.localTeam));
 			client.updateLocalTeamInfo();
 			client.updateStreetButtons();
 		}
 	}
 
 }
