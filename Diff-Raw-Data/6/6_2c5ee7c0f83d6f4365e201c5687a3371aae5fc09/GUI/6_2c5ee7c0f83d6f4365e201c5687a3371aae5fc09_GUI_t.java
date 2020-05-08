 package gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JList;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.Timer;
 
 import components.MyBorder;
 import components.MyButton;
 import components.MyImagePanel;
 import components.MyLabel;
 import components.MyListRenderer;
 import components.MyMap;
 import components.MyPasswordField;
 import components.MySheepButton;
 import components.MyTextField;
 
 import div.ClientConnection;
 import div.MyPoint;
 import div.Sheep;
 import div.User;
 import div.UserRegistration;
 
 /**
  * Klasse som lager GUI
  * 
  * @author andreas
  * 
  */
 
 // asdasdassdasd
 public class GUI extends JFrame {
 
 	public static void main(String args[]) {
		try {
			ClientConnection.open(null);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
 		GUI f = new GUI();
 		f.setVisible(true);
 	}
 
 	// Plassere alt bedre.
 
 	private int width;
 	private int height;
 
 	private User user; // gitt bruker etter en har logget paa
 	private User tUser = new User(); // test bruker
 	private Sheep tSheep = new Sheep();
 	private Sheep editSheep;
 	private MySheepButton editSheepButton;
 
 	public static int START = 0, LOGIN = 1, REG = 2, FORGOT = 3, MAIN = 4,
 			SEARCH = 5, EDIT = 6, LIST = 7, SHEEPREG = 8;
 	private int state; // hvor i programmet en er
 
 	// Font
 	public final static String fontType = "Tahoma";
 	public final static Font FONT = new Font(fontType, Font.BOLD, 12);
 
 	public final static Color valid = Color.green;
 	public final static Color invalid = Color.red;
 
 	private MyListener actionListener;
 	private FocusListener focusListener;
 	private TimerListener tListener;
 	private Timer timer;
 
 	private JLayeredPane lp;
 
 	private KartverketStaticMap kartverketStaticMap;
 
 	public GUI() {
 		super("Tittel");
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		width = 1200;
 		height = 600;
 		setBounds(0, 0, width, height + 20); // +20 for tittel bar'en
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 
 		actionListener = new MyListener();
 		createFocusListener();
 		tListener = new TimerListener();
 		timer = new Timer(100, tListener);
 		// timer.start();
 
 		kartverketStaticMap = new KartverketStaticMap();
 
 		this.lp = getLayeredPane();
 
 		createLowerPanel();
 
 		createButtons();
 		createLoginInterfaceComponents();
 		createRegisterInterfaceComponents();
 		createForgotInterfaceComponents();
 
 		createMainInterfaceComponents();
 		createSearchInterfaceComponents();
 		createEditInterfaceComponents();
 		createRegSheepInterfaceComponents();
 		createListInterfaceComponents();
 
 		createPanels();
 
 		createComponentArrays();
 		// cListCreate();
 
 		// Setter alle start komponentene synlige
 		changeToStartInterface(true);
 
 		repaintPanel();
 	}
 
 	public ArrayList<JComponent> getComponentList(int arg) {
 		if (arg == LOGIN) {
 			return loginComps;
 		} else if (arg == REG) {
 			return registerComps;
 		} else if (arg == START) {
 			return startComps;
 		} else if (arg == FORGOT) {
 			return forgotComps;
 		} else {
 			return null;
 		}
 	}
 
 	public void repaintPanel() {
 		repaint();
 		validate();
 	}
 
 	private BufferedImage getGoogleImage(double latitude, double longitude) {
 		BufferedImage img = (BufferedImage) (GoogleStaticMap.getImage(latitude,
 				longitude, 12, 2 * width / 3, height, 1));
 		return img;
 	}
 
 	private void updateRightPanelMap(double latitude, double longitude) {
 		rightPanel.changeImage(getGoogleImage(latitude, longitude), 0);
 		rightPanel.repaint();
 	}
 
 	/**
 	 * Lager to paneler fordelt paa: leftPanel (width/3) og rightPanel
 	 * (2*width/3)
 	 * 
 	 * @param lp
 	 *            LayeredPane
 	 */
 	private void createPanels() {
 		boolean normal = true;
 		if (normal) {
 			BufferedImage img2 = null;
 			Image scaledImage = null;
 			try {
 				img2 = ImageIO.read(this.getClass().getClassLoader()
 						.getResource("images/sheepbackground.jpeg"));
 				scaledImage = img2
 						.getScaledInstance(300, 300, Image.SCALE_FAST);
 			} catch (IOException e) {
 				System.err.println(e.getLocalizedMessage());
 			}
 			Image[] imgs2 = { img2 };
 			int[] xs2 = { 0 };
 			int[] ys2 = { 0 };
 
 			rightPanel = new MyImagePanel(imgs2, xs2, ys2);
 			rightPanel.repaint();
 			rightPanel.setBounds(width / 3, 0, 2 * width / 3, height);
 		} else {
 			int test = 1;
 			if (test == 0) {
 				double lat = 63.43;
 				double lon = 10.39;
 
 				double w = 0.0172;
 				double h = 0.00577;
 
 				int zoom = 15;
 				BufferedImage img1 = (BufferedImage) GoogleStaticMap.getImage(
 						lat, lon, zoom, 400, 300, 1);
 				BufferedImage img2 = (BufferedImage) GoogleStaticMap.getImage(
 						lat, lon + w, zoom, 400, 300, 1);
 				BufferedImage img3 = (BufferedImage) GoogleStaticMap.getImage(
 						lat - h, lon, zoom, 400, 300, 1);
 				BufferedImage img4 = (BufferedImage) GoogleStaticMap.getImage(
 						lat - h, lon + w, zoom, 400, 300, 1);
 				BufferedImage[] imgs2 = { img1, img2, img3, img4 };
 				int[] xs2 = { 0, 400, 0, 400 };
 				int[] ys2 = { 0, 0, 300, 300 };
 
 				rightPanel = new MyImagePanel(imgs2, xs2, ys2);
 				rightPanel.repaint();
 				rightPanel.setBounds(width / 3, 0, 2 * width / 3, height);
 			} else if (test == 1) {
 				double lat = 63.43;
 				double lon = 10.39;
 				int zoom = 15;
 
 				double w = 0.02745;
 				double h = 0.01231;
 
 				Image img1 = GoogleStaticMap.getImage(lat, lon, zoom, 640, 640,
 						1);
 				Image img2 = GoogleStaticMap.getImage(lat, lon + w, zoom, 640,
 						640, 1);
 				Image img3 = GoogleStaticMap.getImage(lat - h, lon, zoom, 640,
 						640, 1);
 				Image img4 = GoogleStaticMap.getImage(lat - h, lon + w, zoom,
 						640, 640, 1);
 				img1.getScaledInstance(320, 320, Image.SCALE_DEFAULT);
 				img2.getScaledInstance(320, 320, Image.SCALE_DEFAULT);
 				img3.getScaledInstance(320, 320, Image.SCALE_DEFAULT);
 				img4.getScaledInstance(320, 320, Image.SCALE_DEFAULT);
 
 				Image[] imgs2 = { img1, img2, img3, img4 };
 				int[] xs2 = { 0, 320, 0, 320 };
 				int[] ys2 = { 0, 0, 320, 320 };
 
 				rightPanel = new MyImagePanel(imgs2, xs2, ys2);
 				rightPanel.repaint();
 				rightPanel.setBounds(width / 3, 0, 2 * width / 3, height);
 			}
 		}
 		BufferedImage img1 = null;
 		try {
 			img1 = ImageIO.read(this.getClass().getClassLoader()
 					.getResource("images/blackcarbon.jpg"));
 		} catch (IOException e) {
 			System.err.println(e.getLocalizedMessage());
 		}
 		BufferedImage[] imgs = { img1 };
 		int[] xs = { 0 };
 		int[] ys = { 0 };
 
 		leftPanel = new MyImagePanel(imgs, xs, ys);
 		leftPanel.repaint();
 		leftPanel.setBounds(0, 0, width / 3, height);
 
 		lp.add(leftPanel);
 		lp.add(rightPanel);
 	}
 
 	/**
 	 * Lager alle knappene og plasserer dem i start posisjonene.
 	 * 
 	 * @param lp
 	 *            LayeredPane
 	 */
 	private void createButtons() {
 		int bw = width / 6;
 		int bh = height / 10;
 
 		loginButton = new MyButton(new JButton(), "Login", "bluesheepicon2");
 		loginButton.setBounds(width / 12, 7 * height / 20, bw, bh);
 		loginButton.addActionListener(actionListener);
 
 		registerButton = new MyButton(new JButton(), "Register", "blueformicon");
 		registerButton.setBounds(width / 12, 9 * height / 20, bw, bh);
 		registerButton.addActionListener(actionListener);
 
 		forgotButton = new MyButton(new JButton(), "Forgot", "bluequestionicon");
 		forgotButton.setBounds(width / 12, 125 * height / 200, bw, 3 * bh / 4);
 		forgotButton.addActionListener(actionListener);
 
 		sendButton = new MyButton(new JButton(), "Send", null);
 		sendButton.setBounds(width / 12, 9 * height / 20, bw, 3 * bh / 4);
 		sendButton.addActionListener(actionListener);
 
 		backButton = new MyButton(new JButton(), "Back", null);
 		backButton.setBounds(11 * width / 48, 21 * height / 25, bw / 2, bh);
 		backButton.addActionListener(actionListener);
 
 		exitButton = new MyButton(new JButton(), "Exit", null);
 		exitButton.setBounds(width / 48, 21 * height / 25, bw / 2, bh);
 		exitButton.addActionListener(actionListener);
 
 		lp.add(loginButton);
 		lp.add(registerButton);
 		lp.add(forgotButton);
 		lp.add(sendButton);
 		lp.add(backButton);
 		lp.add(exitButton);
 	}
 
 	/**
 	 * Creates all the components to the login interface
 	 * 
 	 * @param lp
 	 *            LayeredPane
 	 */
 
 	private void createLoginInterfaceComponents() {
 		int cw = 4 * width / 30;
 		int ch = 3 * height / 40;
 
 		unField = new MyTextField(new JTextField(), "blueusericon", "Email");
 		unField.setBounds(width / 12, 35 * height / 200, width / 6, ch);
 		unField.addActionListener(actionListener);
 		unField.setName("unField");
 
 		pwField = new MyPasswordField(new JPasswordField(), "bluekeyicon",
 				"Password");
 		// mask formatter!
 		pwField.setBounds(width / 12, 55 * height / 200, width / 6, ch);
 		pwField.addActionListener(actionListener);
 		pwField.setName("pwField");
 
 		lp.add(unField);
 		lp.add(pwField);
 	}
 
 	/**
 	 * Creates all the components to the register interface
 	 * 
 	 * @param lp
 	 *            LayeredPane
 	 */
 
 	private void createRegisterInterfaceComponents() {
 		int cw = width / 9 /* 4 * width / 30 */;
 		int ch = 3 * height / 40;
 
 		regNameField = new MyTextField(new JTextField(), "bluesheepicon",
 				"Firstname");
 		regNameField.setBounds(width / 12, 20 * height / 200, 3 * cw / 2, ch);
 		regNameField.addActionListener(actionListener);
 		regNameField.addFocusListener(focusListener);
 		regNameField.setName("regNameField");
 
 		regLNameField = new MyTextField(new JTextField(), "bluesheepicon",
 				"Lastname");
 		regLNameField.setBounds(width / 12, 35 * height / 200, 3 * cw / 2, ch);
 		regLNameField.addActionListener(actionListener);
 		regLNameField.addFocusListener(focusListener);
 		regLNameField.setName("regLNameField");
 
 		regEmailField = new MyTextField(new JTextField(), "bluemailicon",
 				"Email");
 		regEmailField.setBounds(width / 12, 50 * height / 200, 3 * cw / 2, ch);
 		regEmailField.addActionListener(actionListener);
 		regEmailField.addFocusListener(focusListener);
 		regEmailField.setName("regEmailField");
 
 		regPhoneField = new MyTextField(new JTextField(), "bluephoneicon",
 				"Phone");
 		regPhoneField.setBounds(width / 12, 65 * height / 200, 3 * cw / 2, ch);
 		regPhoneField.addActionListener(actionListener);
 		regPhoneField.addFocusListener(focusListener);
 		regPhoneField.setName("regPhoneField");
 
 		regLongitudeField = new MyTextField(new JTextField(), null, "Longitude");
 		regLongitudeField.setBounds(width / 12 + 3 * cw / 4, 80 * height / 200,
 				3 * cw / 4, ch);
 		regLongitudeField.addActionListener(actionListener);
 		regLongitudeField.addFocusListener(focusListener);
 		regLongitudeField.setName("regLongitudeField");
 
 		regLatitudeField = new MyTextField(new JTextField(), null, "Latitude");
 		regLatitudeField.setBounds(width / 12, 80 * height / 200, 3 * cw / 4,
 				ch);
 		regLatitudeField.addActionListener(actionListener);
 		regLatitudeField.addFocusListener(focusListener);
 		regLatitudeField.setName("regLatitudeField");
 
 		regPwField = new MyPasswordField(new JPasswordField(), "bluekeyicon",
 				"Password");
 		regPwField.setBounds(width / 12, 95 * height / 200, 3 * cw / 2, ch);
 		regPwField.addActionListener(actionListener);
 		regPwField.addFocusListener(focusListener);
 		regPwField.setName("regPwField");
 
 		regPwField2 = new MyPasswordField(new JPasswordField(), "bluekeyicon",
 				"Password");
 		regPwField2.setBounds(width / 12, 110 * height / 200, 3 * cw / 2, ch);
 		regPwField2.setName("regPwField2");
 		regPwField2.addFocusListener(focusListener);
 		regPwField2.addActionListener(actionListener);
 
 		lp.add(regNameField);
 		lp.add(regLNameField);
 		lp.add(regEmailField);
 		lp.add(regPhoneField);
 		lp.add(regLongitudeField);
 		lp.add(regLatitudeField);
 		lp.add(regPwField);
 		lp.add(regPwField2);
 	}
 
 	/**
 	 * Creates all the components to the forgot interface
 	 * 
 	 * @param lp
 	 *            LayeredPane
 	 */
 
 	private void createForgotInterfaceComponents() {
 		int cw = width / 6/* 4 * width / 30 */;
 		int ch = 3 * height / 40;
 
 		emailField = new MyTextField(new JTextField(), "bluemailicon", "Email");
 		emailField.setBounds(width / 12, 6 * height / 20, cw, ch);
 		emailField.addActionListener(actionListener);
 		emailField.setName("emailField");
 
 		// lp.add(emailLabel);
 		lp.add(emailField);
 	}
 
 	private void createMainInterfaceComponents() {
 		// TODO
 		int cw = width / 6;
 		int ch = 3 * height / 40;
 
 		sheepRegButton = new MyButton(new JButton(), "Register sheep", null);
 		sheepRegButton.setBounds(width / 12, 30 * height / 200, cw, ch);
 		sheepRegButton.addActionListener(actionListener);
 
 		listButton = new MyButton(new JButton(), "List over sheeps", null);
 		listButton.setBounds(width / 12, 50 * height / 200, cw, ch);
 		listButton.addActionListener(actionListener);
 
 		searchButton = new MyButton(new JButton(), "Search for sheep", null);
 		searchButton.setBounds(width / 12, 70 * height / 200, cw, ch);
 		searchButton.addActionListener(actionListener);
 
 		editButton = new MyButton(new JButton(), "Edit sheep", null);
 		editButton.setBounds(width / 12, 120 * height / 200, cw, ch);
 		editButton.addActionListener(actionListener);
 
 		homeButton = new MyButton(new JButton(), "Home", null);
 		homeButton.setBounds(width / 12, 110 * height / 200, cw, ch);
 		homeButton.addActionListener(actionListener);
 
 		lp.add(sheepRegButton);
 		lp.add(listButton);
 		lp.add(searchButton);
 		lp.add(editButton);
 		lp.add(homeButton);
 	}
 
 	private void createSearchInterfaceComponents() {
 		// TODO
 		int cw = width / 6;
 		int ch = 3 * height / 40;
 
 		searchLabel = new JLabel("");
 		searchLabel.setBounds(width / 12, 80 * height / 200, cw, ch);
 		searchLabel.setForeground(Color.RED);
 		searchLabel.setHorizontalAlignment(JLabel.CENTER);
 		searchLabel.setVisible(false);
 
 		searchField = new MyTextField(new JTextField(), "bluesheepicon", "ID");
 		searchField.setBounds(width / 12, 65 * height / 200, cw, ch);
 		searchField.addFocusListener(focusListener);
 		searchField.setName("searchField");
 
 		// searchButton = new MyButton(new JButton(), "Search");
 		// searchButton.setBounds(width / 12, 80 * height / 200, cw, ch);
 
 		lp.add(searchLabel);
 		lp.add(searchField);
 		// lp.add(searchButton);
 	}
 
 	private void createEditInterfaceComponents() {
 		// TODO
 		int cw = width / 6;
 		int ch = 3 * height / 40;
 
 		editIdField = new MyTextField(new JTextField(), "bluesheepicon", "");
 		editIdField.setBounds(width / 12, 25 * height / 200, cw, ch);
 		editIdField.addFocusListener(focusListener);
 		editIdField.setName("editIdField");
 
 		editAgeField = new MyTextField(new JTextField(), "bluesheepicon", "");
 		editAgeField.setBounds(width / 12, 40 * height / 200, cw, ch);
 		editAgeField.addFocusListener(focusListener);
 		editAgeField.setName("editAgeField");
 
 		editWeightField = new MyTextField(new JTextField(), "bluesheepicon", "");
 		editWeightField.setBounds(width / 12, 55 * height / 200, cw, ch);
 		editWeightField.addFocusListener(focusListener);
 		editWeightField.setName("editWeightField");
 
 		editSexField = new MyTextField(new JTextField(), "bluesheepicon", "");
 		editSexField.setBounds(width / 12, 70 * height / 200, cw, ch);
 		editSexField.addFocusListener(focusListener);
 		editSexField.setName("editSexField");
 
 		editShepherdField = new MyTextField(new JTextField(), "bluesheepicon",
 				"");
 		editShepherdField.setBounds(width / 12, 85 * height / 200, cw, ch);
 		editShepherdField.addFocusListener(focusListener);
 		editShepherdField.setName("editShepherdField");
 
 		clearAllButton = new MyButton(new JButton(), "Clear All", null);
 		clearAllButton.setBounds(width / 12, 110 * height / 200, cw, ch);
 		clearAllButton.addActionListener(actionListener);
 		clearAllButton.setName("clearAllButton");
 
 		updateButton = new MyButton(new JButton(), "Update", null);
 		updateButton.setBounds(width / 12, 130 * height / 200, cw, ch);
 		updateButton.addActionListener(actionListener);
 		updateButton.setName("updateButton");
 
 		regretButton = new MyButton(new JButton(), "Regret", null);
 		regretButton.setBounds(width / 12 + cw / 2, 110 * height / 200, cw / 2,
 				ch);
 		regretButton.addActionListener(actionListener);
 		regretButton.setName("regretButton");
 
 		lp.add(editIdField);
 		lp.add(editAgeField);
 		lp.add(editWeightField);
 		lp.add(editSexField);
 		lp.add(editShepherdField);
 		lp.add(clearAllButton);
 		lp.add(updateButton);
 		lp.add(regretButton);
 	}
 
 	int lastIndex = -1;
 	int firstIndex = -1;
 
 	private void createListInterfaceComponents() {
 		// TODO
 		int cw = width / 6;
 		int ch = 3 * height / 40;
 
 		sheepy = new MyButton(new JButton(), "DONT CLICK", "bluesheepicon");
 		sheepy.setBounds(width / 12, 20 * height / 200, cw, ch);
 		sheepy.addActionListener(actionListener);
 
 		sheepListModel = new DefaultListModel();
 
 		sheepList = new JList(sheepListModel);
 		sheepList
 				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		sheepList.setCellRenderer(new MyListRenderer());
 
 		sheepList.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent evt) {
 				JList list = (JList) evt.getSource();
 				if (evt.getClickCount() == 2) {
 					int index = list.locationToIndex(evt.getPoint());
 					System.out.println(index);
 					Sheep s = null;
 					String id = ((String) sheepListModel.getElementAt(index))
 							.substring(0, 7);
 					System.out.println(id);
 					for (MySheepButton b : mySheepButtons) {
 						if (b.getSheep().getId() == (Integer.parseInt(id))) {
 							s = b.getSheep();
 						}
 					}
 					setLwEditSheep(s);
 				} else if (evt.getClickCount() == 3) { // Triple-click
 					int index = list.locationToIndex(evt.getPoint());
 
 				}
 			}
 		});
 
 		sheepScrollPane = new JScrollPane(sheepList);
 		sheepScrollPane.setBounds(width / 24, 50 * height / 200, 3 * cw / 2,
 				ch * 6);
 		sheepScrollPane.setVisible(false);
 
 		lp.add(sheepScrollPane);
 		lp.add(sheepy);
 	}
 
 	private void createRegSheepInterfaceComponents() {
 		// TODO
 		int cw = width / 6;
 		int ch = 3 * height / 40;
 		regIDField = new MyTextField(new JTextField(), "bluesheepicon", "ID");
 		regIDField.setBounds(width / 12, 25 * height / 200, cw, ch);
 		regIDField.addFocusListener(focusListener);
 		regIDField.setName("regIDField");
 
 		regAgeField = new MyTextField(new JTextField(), "bluesheepicon",
 				"Birthyear");
 		regAgeField.setBounds(width / 12, 45 * height / 200, cw, ch);
 		regAgeField.addFocusListener(focusListener);
 		regAgeField.setName("regAgeField");
 
 		regWeightField = new MyTextField(new JTextField(), "bluesheepicon",
 				"Weight");
 		regWeightField.setBounds(width / 12, 65 * height / 200, cw, ch);
 		regWeightField.addFocusListener(focusListener);
 		regWeightField.setName("regWeightField");
 
 		regSexField = new MyTextField(new JTextField(), "bluesheepicon", "Sex");
 		regSexField.setBounds(width / 12, 85 * height / 200, cw, ch);
 		regSexField.addFocusListener(focusListener);
 		regSexField.setName("regSexField");
 
 		regShepherdField = new MyTextField(new JTextField(), "bluesheepicon",
 				"Shepard");
 		regShepherdField.setBounds(width / 12, 105 * height / 200, cw, ch);
 		regShepherdField.addFocusListener(focusListener);
 		regShepherdField.setName("regShepherdField");
 
 		lp.add(regIDField);
 		lp.add(regAgeField);
 		lp.add(regWeightField);
 		lp.add(regSexField);
 		lp.add(regShepherdField);
 	}
 
 	// metode for � f� ut et punkt fra en streng
 	private MyPoint getLocationPoint(String arg) throws Exception {
 		String[] list = arg.split(",");
 		try {
 			return new MyPoint(Double.parseDouble(list[0]),
 					Double.parseDouble(list[1]));
 		} catch (Exception e) {
 			System.err.println("Fikk ikke gjort om string til Point location.");
 		}
 		return null;
 	}
 
 	private void addMySheepButtons() {
 		mySheepButtons = new ArrayList<MySheepButton>();
 		ArrayList<Sheep> list = user.getSheepList();
 		int zoom = 15;
 		double numw = 0.0123 / 2;
 		double numh = 0.0275 / 2;
 
 		double nh = height / 2 / numh;
 		double nw = width / 3 / numw;
 		System.out.println("lengden p sauelisten: " + list.size());
 		if (!list.isEmpty()) {
 			for (Sheep s : list) {
 				MyPoint p = null;
 				try {
 					p = getLocationPoint(s.getLocation().getPosition());
 					System.out
 							.println(p.getLatitude() + " " + p.getLongitude());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				int x = (int) ((user.getLongitudeDouble() - p.getLongitude()) * nw);
 				int y = (int) ((user.getLatitudeDouble() - p.getLatitude()) * nh);
 
 				mySheepButtons.add(new MySheepButton(new JButton(), s, 2
 						* width / 3 - x, height / 2 + y, 10, this));
 			}
 			for (MySheepButton b : mySheepButtons) {
 				lp.add(b);
 			}
 		}
 	}
 
 	public void updateSheepButtonPosition(int x, int y) {
 		for (MySheepButton b : mySheepButtons) {
 			Point p = b.getLocation();
 			b.setLocation(p.x - x, p.y - y);
 		}
 	}
 
 	public void changeMySheepButtonBounds(double lat, double lon, int zoom,
 			int x, int y) {
 		double numw = 0.0275;
 		double numh = 0.0123;
 
 		double nh = height / numh;
 		double nw = 2 * width / 3 / numw;
 		for (MySheepButton b : mySheepButtons) {
 			MyPoint p = null;
 			try {
 				p = getLocationPoint(b.getSheep().getLocation().getPosition());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			int dx = (int) ((lon - p.getLongitude()) * nw) - x - 400;
 			int dy = (int) ((lat - p.getLatitude()) * nh) - y + 250;
 
 			b.setLocation(2 * width / 3 - dx, height / 2 + dy);
 			if (zoom == 17) {
 				b.changeSize(20);
 			} else {
 				b.changeSize(10);
 			}
 			b.repaint();
 		}
 	}
 
 	private void createMap() {
 		myMap = new MyMap(2 * width / 3, 85 * height / 100,
 				user.getLatitudeDouble(), user.getLongitudeDouble(), this);
 		myMap.setBounds(width / 3, 0, 2 * width / 3, 85 * height / 100);
 		lp.add(myMap);
 	}
 
 	private void createLowerPanel() {
 		try {
 			lwEditLabel = new JLabel() {
 				BufferedImage image = ImageIO
 						.read(this.getClass().getClassLoader()
 								.getResource("images/blackcarbon.jpg"));;
 
 				protected void paintComponent(Graphics g) {
 					super.paintComponent(g);
 					g.drawImage(image, 0, 0, this);
 				}
 			};
 
 			int cw = width / 12;
 			int ch = 2 * height / 40;
 
 			lwEditLabel.setBounds(width / 3, 85 * height / 100, 2 * width / 3,
 					15 * height / 100);
 			lwEditLabel.setVisible(false);
 
 			lwEditIdField = new MyTextField(new JTextField(), null, "");
 			lwEditIdField.setBounds(41 * width / 120, 9 * height / 10,
 					5 * cw / 8, ch);
 			lwEditIdField.setEditable(false);
 
 			// lwEditAgeField = new MyTextField(new JTextField(), null, "");
 			// lwEditAgeField.setBounds(24 * width / 60, 9 * height / 10, cw /
 			// 2, ch);
 
 			lwEditWeightField = new MyTextField(new JTextField(), null, "");
 			lwEditWeightField.setBounds(24 * width / 60, 9 * height / 10,
 					cw / 2, ch);
 
 			lwEditOwnerField = new MyTextField(new JTextField(), null, "");
 			lwEditOwnerField.setBounds(27 * width / 60, 9 * height / 10,
 					27 * cw / 16, ch);
 
 			lwEditShepherdField = new MyTextField(new JTextField(), null, "");
 			lwEditShepherdField.setBounds(36 * width / 60, 9 * height / 10,
 					27 * cw / 16, ch);
 
 			lwEditHeartrateField = new MyTextField(new JTextField(), null, "");
 			lwEditHeartrateField.setBounds(45 * width / 60, 9 * height / 10,
 					cw / 2, ch);
 
 			lwEditTemperatureField = new MyTextField(new JTextField(), null, "");
 			lwEditTemperatureField.setBounds(48 * width / 60, 9 * height / 10,
 					cw / 2, ch);
 
 			lwEditBirthyearField = new MyTextField(new JTextField(), null, "");
 			lwEditBirthyearField.setBounds(51 * width / 60, 9 * height / 10,
 					cw, ch);
 
 			lwEditIdLabel = new MyLabel(new JLabel(), "ID:", null);
 			lwEditIdLabel.setBounds(41 * width / 120, 85 * height / 100,
 					5 * cw / 8, ch);
 
 			// lwEditAgeLabel = new MyLabel(new JLabel(), "Age:", null);
 			// lwEditAgeLabel.setBounds(24 * width / 60, 85 * height / 100, cw /
 			// 2, ch);
 
 			lwEditWeightLabel = new MyLabel(new JLabel(), "Weight:", null);
 			lwEditWeightLabel.setBounds(24 * width / 60, 85 * height / 100,
 					cw / 2, ch);
 
 			lwEditOwnerLabel = new MyLabel(new JLabel(), "Owner:", null);
 			lwEditOwnerLabel.setBounds(27 * width / 60, 85 * height / 100,
 					27 * cw / 16, ch);
 
 			lwEditShepherdLabel = new MyLabel(new JLabel(), "Sheperd:", null);
 			lwEditShepherdLabel.setBounds(36 * width / 60, 85 * height / 100,
 					27 * cw / 16, ch);
 
 			lwEditHeartrateLabel = new MyLabel(new JLabel(), "", "redtempicon");
 			lwEditHeartrateLabel.setBounds(45 * width / 60, 85 * height / 100,
 					cw / 2, ch);
 
 			lwEditTemperatureLabel = new MyLabel(new JLabel(), "",
 					"redhearticon");
 			lwEditTemperatureLabel.setBounds(48 * width / 60,
 					85 * height / 100, cw / 2, ch);
 
 			lwEditBirthyearLabel = new MyLabel(new JLabel(), "Birthyear:", null);
 			lwEditBirthyearLabel.setBounds(51 * width / 60, 85 * height / 100,
 					cw, ch);
 
 			lwEditButton = new MyButton(new JButton(), "", "redgearicon");
 			lwEditButton.setBounds(56 * width / 60, 85 * height / 100, cw, cw);
 			lwEditButton.setBorder(null);
 			lwEditButton.setName("lwEditButton");
 			lwEditButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					editSheep();
 				}
 			});
 
 			lp.add(lwEditButton);
 			lp.add(lwEditIdField);
 			// lp.add(lwEditAgeField);
 			lp.add(lwEditWeightField);
 			lp.add(lwEditOwnerField);
 			lp.add(lwEditShepherdField);
 			lp.add(lwEditHeartrateField);
 			lp.add(lwEditTemperatureField);
 			lp.add(lwEditBirthyearField);
 
 			lp.add(lwEditIdLabel);
 			// lp.add(lwEditAgeLabel);
 			lp.add(lwEditWeightLabel);
 			lp.add(lwEditOwnerLabel);
 			lp.add(lwEditShepherdLabel);
 			lp.add(lwEditHeartrateLabel);
 			lp.add(lwEditTemperatureLabel);
 			lp.add(lwEditBirthyearLabel);
 
 			lp.add(lwEditLabel);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets together the different lists of components that are showing in the
 	 */
 	private void createComponentArrays() {
 
 		startComps = new ArrayList<JComponent>();
 		startComps.add(loginButton);
 		startComps.add(registerButton);
 		startComps.add(exitButton);
 
 		loginComps = new ArrayList<JComponent>();
 		loginComps.add(pwField);
 		loginComps.add(unField);
 		loginComps.add(forgotButton);
 		loginComps.add(loginButton);
 		loginComps.add(backButton);
 		loginComps.add(exitButton);
 
 		forgotComps = new ArrayList<JComponent>();
 		forgotComps.add(emailField);
 		forgotComps.add(backButton);
 		forgotComps.add(exitButton);
 		forgotComps.add(sendButton);
 
 		registerComps = new ArrayList<JComponent>();
 		registerComps.add(regNameField);
 		registerComps.add(regLNameField);
 		registerComps.add(regEmailField);
 		registerComps.add(regPhoneField);
 		registerComps.add(regLongitudeField);
 		registerComps.add(regLatitudeField);
 		registerComps.add(regPwField);
 		registerComps.add(regPwField2);
 		registerComps.add(registerButton);
 		registerComps.add(backButton);
 		registerComps.add(exitButton);
 
 		mainComps = new ArrayList<JComponent>();
 		mainComps.add(sheepRegButton);
 		mainComps.add(listButton);
 		mainComps.add(searchButton);
 		mainComps.add(homeButton);
 		mainComps.add(backButton);
 		mainComps.add(exitButton);
 
 		searchComps = new ArrayList<JComponent>();
 		searchComps.add(searchLabel);
 		searchComps.add(searchField);
 		searchComps.add(searchButton);
 		searchComps.add(backButton);
 		searchComps.add(exitButton);
 
 		editComps = new ArrayList<JComponent>();
 		editComps.add(editIdField);
 		editComps.add(editAgeField);
 		editComps.add(editWeightField);
 		editComps.add(editSexField);
 		editComps.add(editShepherdField);
 		editComps.add(clearAllButton);
 		editComps.add(updateButton);
 		editComps.add(backButton);
 		editComps.add(exitButton);
 
 		listComps = new ArrayList<JComponent>();
 		listComps.add(sheepy);
 		listComps.add(sheepScrollPane);
 		listComps.add(backButton);
 		listComps.add(exitButton);
 
 		regSheepComps = new ArrayList<JComponent>();
 		regSheepComps.add(regIDField);
 		regSheepComps.add(regAgeField);
 		regSheepComps.add(regWeightField);
 		regSheepComps.add(regSexField);
 		regSheepComps.add(regShepherdField);
 		regSheepComps.add(sheepRegButton);
 		regSheepComps.add(exitButton);
 		regSheepComps.add(backButton);
 
 		fieldComps = new ArrayList<JComponent>();
 		fieldComps.add(regNameField);
 		fieldComps.add(regLNameField);
 		fieldComps.add(regEmailField);
 		fieldComps.add(regPhoneField);
 		fieldComps.add(regPwField);
 		fieldComps.add(regPwField2);
 		fieldComps.add(pwField);
 		fieldComps.add(unField);
 		fieldComps.add(emailField);
 
 		lwEditComps = new ArrayList<JComponent>();
 		lwEditComps.add(lwEditLabel);
 		lwEditComps.add(lwEditIdField);
 		// lwEditComps.add(lwEditAgeField);
 		lwEditComps.add(lwEditWeightField);
 		lwEditComps.add(lwEditOwnerField);
 		lwEditComps.add(lwEditShepherdField);
 		lwEditComps.add(lwEditHeartrateField);
 		lwEditComps.add(lwEditTemperatureField);
 		lwEditComps.add(lwEditBirthyearField);
 		lwEditComps.add(lwEditIdLabel);
 		// lwEditComps.add(lwEditAgeLabel);
 		lwEditComps.add(lwEditWeightLabel);
 		lwEditComps.add(lwEditOwnerLabel);
 		lwEditComps.add(lwEditShepherdLabel);
 		lwEditComps.add(lwEditHeartrateLabel);
 		lwEditComps.add(lwEditTemperatureLabel);
 		lwEditComps.add(lwEditBirthyearLabel);
 		lwEditComps.add(lwEditButton);
 	}
 
 	/**
 	 * Metode for aa plassere de ulike komponentene bedre, for � slippe mye av
 	 * den samme koden.
 	 * 
 	 * @param list
 	 */
 	private void placeComponentsNeat(ArrayList<Component> list) {
 		int cw = width / 6;
 		int ch = 3 * height / 40;
 		int rh = height * 8 / 10;
 
 		ArrayList<Component> labelList = new ArrayList<Component>();
 		ArrayList<Component> fieldList = new ArrayList<Component>();
 		ArrayList<Component> buttonList = new ArrayList<Component>();
 
 		// plasserer alle komponenter som skal plasseres i ulike lister
 		for (Component c : list) {
 			if (c instanceof JLabel) {
 				labelList.add(c);
 			} else if (c instanceof JTextField || c instanceof JPasswordField) {
 				fieldList.add(c);
 			} else if (c instanceof JButton) {
 				buttonList.add(c);
 			}
 		}
 		int ih = 1; // h�yden for alle labels og feilds
 
 		for (Component c : buttonList) {
 
 		}
 
 	}
 
 	/**
 	 * Metode for � bytte til start interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToStartInterface(Boolean bool) {
 		if (bool) {
 			loginButton.setBounds(width / 12, 6 * height / 20, width / 6,
 					height / 10);
 			registerButton.setBounds(width / 12, 9 * height / 20, width / 6,
 					height / 10);
 			state = 0;
 		}
 		for (Component c : startComps) {
 			c.setVisible(bool);
 		}
 		repaintPanel();
 	}
 
 	/**
 	 * Metode for � bytte til login interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToLoginInterface(boolean bool) {
 		for (Component c : loginComps) {
 			c.setVisible(bool);
 		}
 		if (bool) {
 			loginButton.setBounds(width / 12, 85 * height / 200, width / 6,
 					3 * height / 40);
 			forgotButton.setBounds(width / 12, 105 * height / 200, width / 6,
 					3 * height / 40);
 			registerButton.setVisible(!bool);
 			backButton.setText("Back");
 			unField.requestFocus();
 			state = LOGIN;
 		}
 		repaintPanel();
 	}
 
 	/**
 	 * Metode for � bytte til register interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToRegisterInterface(boolean bool) {
 		if (bool) {
 			registerButton.setBounds(width / 12, 130 * height / 200, width / 6,
 					3 * height / 40);
 			backButton.setText("Back");
 			state = REG;
 		}
 		for (Component c : registerComps) {
 			c.setVisible(bool);
 		}
 		repaintPanel();
 	}
 
 	/**
 	 * Metode for � bytte til forgot interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToForgotInterface(boolean bool) {
 		if (bool) {
 			sendButton.setBounds(width / 12, 85 * height / 200, width / 6,
 					3 * height / 40);
 			backButton.setText("Back");
 			state = FORGOT;
 		}
 		for (Component c : forgotComps) {
 			c.setVisible(bool);
 		}
 		repaintPanel();
 	}
 
 	/**
 	 * Metode for � bytte til main interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToMainInterface(boolean bool) {
 		if (bool) {
 			searchButton.setBounds(width / 12, 70 * height / 200, width / 6,
 					3 * height / 40);
 			sheepRegButton.setBounds(width / 12, 30 * height / 200, width / 6,
 					3 * height / 40);
 			backButton.setText("Log out");
 			editButton.setVisible(false);
 			if (editSheepButton != null) {
 				((MyBorder) editSheepButton.getBorder()).setColor(Color.RED);
 			}
 			state = MAIN;
 		}
 		for (Component c : mainComps) {
 			c.setVisible(bool);
 		}
 		repaintPanel();
 	}
 
 	/**
 	 * Metode for � bytte til search interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToSearchInterface(boolean bool) {
 		if (bool) {
 			searchButton.setBounds(width / 12, 85 * height / 200, width / 6,
 					3 * height / 40);
 			searchLabel.setText("");
 			backButton.setText("Back");
 			state = SEARCH;
 		}
 		for (Component c : searchComps) {
 			c.setVisible(bool);
 		}
 	}
 
 	/**
 	 * Metode for � bytte til edit interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToEditInterface(boolean bool) {
 		if (bool) {
 			backButton.setText("Back");
 			state = EDIT;
 		}
 		for (Component c : editComps) {
 			c.setVisible(bool);
 		}
 	}
 
 	/**
 	 * Metode for � bytte til list interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToListInterface(boolean bool) {
 		if (bool) {
 			backButton.setText("Back");
 			state = LIST;
 		}
 		for (Component c : listComps) {
 			c.setVisible(bool);
 		}
 	}
 
 	/**
 	 * Metode for � bytte til register sheep interface
 	 * 
 	 * @param bool
 	 *            true for synlig
 	 */
 	private void changeToRegSheepInterface(boolean bool) {
 		if (bool) {
 			sheepRegButton.setBounds(width / 12, 130 * height / 200, width / 6,
 					3 * height / 40);
 			backButton.setText("Back");
 			state = SHEEPREG;
 		}
 		for (Component c : regSheepComps) {
 			c.setVisible(bool);
 		}
 	}
 
 	/**
 	 * Metode som pr�ver � registrere bruker naar brukeren ber om det
 	 */
 	// Henter informasjon fra tekst boksene og skriver dem ut.
 	private boolean registerUser() {
 		String firstName = regNameField.getText();
 		String lastName = regLNameField.getText();
 		String email = regEmailField.getText();
 		String phoneNr = regPhoneField.getText();
 		String latitude = regLatitudeField.getText();
 		String longitude = regLongitudeField.getText();
 		char[] pw = regPwField.getPassword();
 		char[] pw2 = regPwField2.getPassword();
 
 		String password = "";
 		for (int i = 0; i < pw.length; i++) {
 			password += pw[i];
 		}
 		String password2 = "";
 		for (int i = 0; i < pw2.length; i++) {
 			password2 += pw2[i];
 		}
 		// Sjekker om passordene stemmer oversend
 		if (password.equals(password2)) {
 			try {
 				// Registrer bruker
 				UserRegistration.registerUser(firstName, lastName, email,
 						password2, phoneNr, latitude + "," + longitude);
 				System.out.println("User registered.");
 				return true;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Metode som prover aa logge inn med gitt email og passord
 	 */
 	private boolean login() {
 		String email = unField.getText();
 		String password = "";
 		char[] c = pwField.getPassword();
 		for (int i = 0; i < c.length; i++) {
 			password += c[i];
 		}
 
 		// FOR TESTING
 		if (email.equals("")) {
 			try {
 				this.user = UserRegistration
 						.login("test0@test.test", "passord");
 				System.out.println(user);
 				try {
 					user.updateSheepList();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				changeToLoginInterface(false);
 				changeToMainInterface(true);
 				System.out.println("Logged in: " + user.getFirstName());
 				rightPanel.setVisible(false);
 
 				addMySheepButtons();
 				createMap();
 				myMap.setUser(user);
 				updateSheepList();
 
 				for (JComponent jc : lwEditComps) {
 					jc.setVisible(true);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println(e.getLocalizedMessage());
 				System.out.println("Noe galt med admin bruker");
 			}
 			return true;
 		}
 		User user = UserRegistration.login(email, password);
 		if (user != null) {
 			this.user = user;
 			try {
 				user.updateSheepList();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			changeToLoginInterface(false);
 			changeToMainInterface(true);
 			System.out.println("Logged in: " + user.getEmail());
 			rightPanel.setVisible(false);
 
 			addMySheepButtons();
 			createMap();
 			myMap.setUser(user);
 			updateSheepList();
 
 			for (JComponent jc : lwEditComps) {
 				jc.setVisible(true);
 			}
 
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Metode som vil prove aa sende nytt passord til email
 	 */
 	private boolean sendEmail() {
 		boolean sendmail = true;
 		if (sendmail) { // you know
 			if (emailField.getText() != null) {
 				// TODO finnes email, og sende og bytte passord.
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Metode der en kan endre paa saue informasjonen
 	 */
 	private Sheep editSheep() {
 		// editIdField.setText("" + editSheep.getId());
 		// editAgeField.setText("" + editSheep.getBirthyear());
 		// editWeightField.setText("" + editSheep.getWeight());
 		// editSexField.setText("" + editSheep.getGender());
 		// editShepherdField.setText("" + editSheep.getShepherd());
 		String id = lwEditIdField.getText();
 		String age = lwEditAgeField.getText();
 		String weight = lwEditWeightField.getText();
 		String owner = lwEditOwnerField.getText();
 		String shepherd = lwEditShepherdField.getText();
 		String heartrate = lwEditHeartrateField.getText();
 		String temperature = lwEditTemperatureField.getText();
 		String birthyear = lwEditBirthyearField.getText();
 
 		try {
 			// user.editSheep(owner, shepherd, weight);
 		} catch (Exception e) {
 			System.out.println("something wrong!");
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Registrerer sau med informasjon gitt i tekstboksene.
 	 * 
 	 * @return true Hvis sau er blitt registrert
 	 */
 	private boolean registerSheep() {
 		String id = regIDField.getText();
 		String age = regAgeField.getText();
 		String weight = regWeightField.getText();
 		String gender = regSexField.getText();
 		String shepherd = regShepherdField.getText();
 
 		try {
 			user.registerSheep(id, age, weight, gender, user.getEmail(),
 					shepherd);
 			return true;
 		} catch (NumberFormatException e) {
 			System.out.println("hey wrong");
 			return false;
 		} catch (Exception e) {
 			System.out.println("Hey wrong." + e.getLocalizedMessage());
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Metode som logger ut ifra bruker
 	 */
 	private void logout() {
 		System.out.println("Logged out from: " + user.getEmail());
 		myMap.setUser(null);
 		rightPanel.setVisible(true);
 		for (JComponent jc : lwEditComps) {
 			jc.setVisible(false);
 		}
 		this.user = null;
 	}
 
 	/**
 	 * Soker etter riktig sau og zoomer inn p� kartet til rett sau.
 	 */
 	private void searchSheep() {
 		String input = searchField.getText();
 		int id = -1;
 		try {
 			id = Integer.parseInt(input);
 			searchButton.setBounds(width / 12, 85 * height / 200, width / 6,
 					3 * height / 40);
 			searchLabel.setText("");
 			editButton.setVisible(false);
 			Sheep sheep = null; // getSheep(ID);
 			for (MySheepButton b : mySheepButtons) {
 				if (id == b.getSheep().getId()) {
 					sheep = b.getSheep();
 					editSheepButton = b;
 					((MyBorder) b.getBorder()).setColor(Color.BLUE);
 				}
 			}
 			if (sheep != null) {
 				double latitude = Double.parseDouble(sheep.getLocation()
 						.getLatitude());
 				double longitude = Double.parseDouble(sheep.getLocation()
 						.getLongitude());
 				editSheep = sheep;
 				try {
 					myMap = new MyMap(2 * width / 3, 85 * height / 100,
 							latitude, longitude, this);
 					myMap.zoomInOnSheep(latitude, longitude);
 					double lat = Double.parseDouble(editSheep.getLocation()
 							.getLatitude());
 					double lon = Double.parseDouble(editSheep.getLocation()
 							.getLongitude());
 					changeMySheepButtonBounds(lat, lon, 15, 0, 0);
 				} catch (Exception e) {
 					System.out.println("no internet");
 				}
 				editButton.setVisible(true);
 			} else {
 				// kan ikke finne sheep, vise fram p� en m�te
 				searchButton.setBounds(width / 12, 95 * height / 200,
 						width / 6, 3 * height / 40);
 				searchLabel.setText("No sheep by that ID, (" + input + ").");
 			}
 		} catch (NumberFormatException e) {
 			// feil verdier for input
 			searchButton.setBounds(width / 12, 95 * height / 200, width / 6,
 					3 * height / 40);
 			searchLabel.setText("ID input is not a valid number.");
 			editButton.setVisible(false);
 		}
 	}
 
 	/**
 	 * Endre fargen p� saue knappene
 	 * 
 	 * @param num
 	 *            Tall for forskjellige farger
 	 */
 	private void changeMySheepButtonColors(int num) {
 		// 1 = m/f, 2
 		// System.out.println(num);
 		if (num == 1) {
 			for (MySheepButton b : mySheepButtons) {
 				Sheep s = b.getSheep();
 				if (s.getGender() == 'm') {
 					b.setColor(Color.BLUE); // m
 				} else {
 					b.setColor(Color.MAGENTA); // f
 				}
 			}
 		} else if (num == 2) {
 			for (MySheepButton b : mySheepButtons) {
 				b.setColor(Color.RED);
 			}
 		} else if (num == 3) {
 			for (MySheepButton b : mySheepButtons) {
 				b.setColor(Color.GREEN);
 			}
 		} else if (num == 4) {
 			for (MySheepButton b : mySheepButtons) {
 				b.setColor(Color.CYAN);
 			}
 		} else if (num == 5) {
 			for (MySheepButton b : mySheepButtons) {
 				b.setColor(Color.PINK);
 			}
 		}
 	}
 
 	/**
 	 * Setter inn informasjon fra en sau inn i lwEdit feltene.
 	 * 
 	 * @param s
 	 *            Gitt Sheep
 	 */
 
 	public void setLwEditSheep(Sheep s) {
 		if (s == null) {
 
 		} else {
 			lwEditIdField.setText("" + s.getId());
 			// lwEditAgeField.setText("" + s.getBirthyear());
 			lwEditWeightField.setText("" + s.getWeight());
 			lwEditOwnerField.setText("" + user.getEmail()); // get owner
 			lwEditShepherdField.setText("" + s.getShepherd());
 			lwEditHeartrateField.setText("" + s.getHeartrate());
 			lwEditTemperatureField.setText("" + s.getTemperature());
 			lwEditBirthyearField.setText("" + s.getBirthyear());
 			System.out.println(s.getLocation());
 			double lat = Double.parseDouble(s.getLocation().getLatitude());
 			double lon = Double.parseDouble(s.getLocation().getLongitude());
 			System.out.println(lat + "  " + lon);
 			myMap.changeToSheepImages(lat, lon);
 			changeMySheepButtonBounds(lat, lon, 15, 0, 0);
 
 		}
 	}
 
 	private void updateSheepList() {
 		for (MySheepButton s : mySheepButtons) {
 			sheepListModel.addElement(s.toString());
 		}
 
 	}
 
 	/**
 	 * Lager en FocusListener som lytter til naar en bruker trykker p�
 	 * tekstbokser og forlater dem
 	 */
 
 	private void createFocusListener() {
 		focusListener = new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent e) {
 				Component comp = e.getComponent();
 				if (comp instanceof JTextField) {
 					((JTextField) comp).selectAll();
 					comp.setBackground(Color.WHITE);
 				}
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				Component component = e.getComponent();
 
 				if (component instanceof JTextField) {
 					JTextField fc = (JTextField) component;
 					String name = fc.getName();
 					String input = null;
 					if (name.equals("regNameField")) {
 						try {
 							input = regNameField.getText();
 							System.out.println(input);
 							if (!input.equals("")) {
 								tUser.setFirstName(input);
 								((MyBorder) regNameField.getBorder())
 										.changeColor(valid);
 							}
 						} catch (Exception exc) {
 							((MyBorder) regNameField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regLNameField")) {
 						try {
 							input = regLNameField.getText();
 							if (!input.equals("")) {
 								tUser.setLastName(input);
 								((MyBorder) regLNameField.getBorder())
 										.changeColor(valid);
 							}
 						} catch (Exception exc) {
 							((MyBorder) regLNameField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regEmailField")) {
 						try {
 							input = regEmailField.getText();
 							if (!input.equals("")) {
 								tUser.setEmail(input);
 								((MyBorder) regEmailField.getBorder())
 										.changeColor(valid);
 							}
 						} catch (Exception exc) {
 							((MyBorder) regEmailField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regPhoneField")) {
 						try {
 							input = regPhoneField.getText();
 							if (!input.equals("")) {
 								tUser.setPhoneNr(input);
 								((MyBorder) regPhoneField.getBorder())
 										.changeColor(valid);
 							}
 						} catch (Exception exc) {
 							((MyBorder) regPhoneField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regLatitudeField")) {
 						try {
 							input = regLatitudeField.getText();
 							if (!input.equals("")) {
 								System.out.println(input);
 								tUser.setLatitude(input);
 								((MyBorder) regLatitudeField.getBorder())
 										.changeColor(valid);
 								System.out.println("hore");
 								if (((MyBorder) regLongitudeField.getBorder())
 										.getColor().equals(valid)) {
 									updateRightPanelMap(
 											tUser.getLatitudeDouble(),
 											tUser.getLongitudeDouble());
 								}
 							}
 						} catch (Exception exc) {
 							((MyBorder) regLatitudeField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regLongitudeField")) {
 						try {
 							input = regLongitudeField.getText();
 							if (!input.equals("")) {
 								tUser.setLongitude(input);
 								((MyBorder) regLongitudeField.getBorder())
 										.changeColor(valid);
 								if (((MyBorder) regLatitudeField.getBorder())
 										.getColor().equals(valid)) {
 									updateRightPanelMap(
 											tUser.getLatitudeDouble(),
 											tUser.getLongitudeDouble());
 								}
 							}
 						} catch (Exception exc) {
 							((MyBorder) regLongitudeField.getBorder())
 									.changeColor(invalid);
 						}
 
 					} else if (name.equals("regPwField")) {
 						try {
 							char[] list = regPwField.getPassword();
 							String password1 = "";
 							for (int i = 0; i < list.length; i++) {
 								password1 += list[i];
 							}
 							if (!password1.equals("")) {
 								tUser.setPassword(password1);
 								((MyBorder) regPwField.getBorder())
 										.changeColor(valid);
 							}
 						} catch (Exception exc) {
 							((MyBorder) regPwField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regPwField2")) {
 						try {
 							char[] list = regPwField.getPassword();
 							String password1 = "";
 							for (int i = 0; i < list.length; i++) {
 								password1 += list[i];
 							}
 
 							char[] list2 = regPwField2.getPassword();
 							String password2 = "";
 							for (int i = 0; i < list2.length; i++) {
 								password2 += list[i];
 							}
 
 							if (password1.equals(password2)) {
 								tUser.setPassword(password2);
 								((MyBorder) regPwField2.getBorder())
 										.changeColor(valid);
 							} else {
 								// LEGGE TIL EN PASSORD MATCHER IKKE LABEL
 								// ELLERNO
 								((MyBorder) regPwField2.getBorder())
 										.changeColor(invalid);
 								System.out.println("Password does not match!");
 								return;
 							}
 						} catch (Exception exc) {
 							((MyBorder) regPwField.getBorder())
 									.changeColor(invalid);
 							((MyBorder) regPwField2.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regIDField")) {
 						try {
 							// TODO metode for � sjekke om ID field finnes.
 							input = "1337";// regIDField.getID();
 							if (Integer.parseInt(input) >= 0) {
 								((MyBorder) regIDField.getBorder())
 										.changeColor(valid);
 							} else {
 								((MyBorder) regIDField.getBorder())
 										.changeColor(invalid);
 							}
 						} catch (Exception exc) {
 							((MyBorder) regIDField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regAgeField")) {
 						try {
 							input = regAgeField.getText();
 							tSheep.setBirthyear(Integer.parseInt(input));
 							((MyBorder) regAgeField.getBorder())
 									.changeColor(valid);
 						} catch (Exception exc) {
 							((MyBorder) regAgeField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regWeightField")) {
 						try {
 							input = regWeightField.getText();
 							tSheep.setWeight(Integer.parseInt(input));
 							((MyBorder) regWeightField.getBorder())
 									.changeColor(valid);
 						} catch (Exception exc) {
 							((MyBorder) regWeightField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regSexField")) {
 						try {
 							input = regSexField.getText();
 							if (input.charAt(0) == 'm'
 									|| input.charAt(0) == 'f') {
 								((MyBorder) regSexField.getBorder())
 										.changeColor(valid);
 							} else {
 								((MyBorder) regSexField.getBorder())
 										.changeColor(invalid);
 							}
 						} catch (Exception exc) {
 							((MyBorder) regSexField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("regShepherdField")) {
 						try {
 							input = regShepherdField.getText();
 							tSheep.setShepherd(input);
 							((MyBorder) regShepherdField.getBorder())
 									.changeColor(valid);
 						} catch (Exception exc) {
 							((MyBorder) regShepherdField.getBorder())
 									.changeColor(invalid);
 						}
 					} else if (name.equals("searchField")) {
 						// mmmh
 					}
 				}
 			}
 		};
 	}
 
 	/**
 	 * Egen version av ActionListener klasse som haandterer knappetrykking og
 	 * naar "enter" blir trykket p� mens i en tekst boks
 	 * 
 	 * @author andreas
 	 * 
 	 */
 	private class MyListener implements ActionListener {
 		// bruke setActionCommand p� alt som skal lyttes p� og bruke det
 		// istedenfor navn?
 		private int delete = 0;
 
 		@Override
 		public void actionPerformed(ActionEvent arg) {
 
 			if (arg.getSource() instanceof JButton) {
 				JButton pressed = (JButton) arg.getSource();
 				String text = pressed.getText();
 				System.out.println(text);
 
 				if (text.equals("Register")) {
 					if (state == 2) {
 						if (registerUser()) {
 							changeToRegisterInterface(false);
 							changeToLoginInterface(true);
 						} else {
 							// istedenfor boolean fra registerUser() faa tall
 							// for aa finne ut om hvilke verdier som er feil
 							// Noe lignende med login!
 						}
 					} else {
 						changeToLoginInterface(false);
 						changeToRegisterInterface(true);
 					}
 				} else if (text.equals("Login")) {
 					// sjekker: hvis man er i login interface og trykker login
 					// ->
 					if (state == 1) {
 						// metode for aa sjekke om brukernavn og passord stemmer
 						login();
 					} else {
 						changeToStartInterface(false);
 						changeToLoginInterface(true);
 					}
 				} else if (text.equals("Exit")) {
 					System.exit(0);
 					ClientConnection.close();
 				} else if (text.equals("Back")) {
 					// state holder styr paa hvor man er, saa programet vet
 					// hvor en skal sendes tilbake til
 					if (state == LOGIN) {
 						changeToLoginInterface(false);
 						changeToStartInterface(true);
 					} else if (state == REG) {
 						changeToRegisterInterface(false);
 						changeToStartInterface(true);
 					} else if (state == FORGOT) {
 						changeToForgotInterface(false);
 						changeToLoginInterface(true);
 					} else if (state == SEARCH) {
 						changeToSearchInterface(false);
 						changeToMainInterface(true);
 						regretButton.setVisible(false);
 						changeMySheepButtonBounds(user.getLatitudeDouble(),
 								user.getLongitudeDouble(), 15, 0, 0);
 						myMap.bigMap();
 					} else if (state == EDIT) {
 						changeToEditInterface(false);
 						changeToMainInterface(true);
 					} else if (state == LIST) {
 						changeToListInterface(false);
 						changeToMainInterface(true);
 					} else if (state == SHEEPREG) {
 						changeToRegSheepInterface(false);
 						changeToMainInterface(true);
 					}
 				} else if (text.equals("Forgot")) {
 					changeToLoginInterface(false);
 					changeToForgotInterface(true);
 				} else if (text.equals("Send")) {
 					// sjekker: er i forgot interface og trykker send ->
 					if (state == 3) {
 						// SEND EMAIL!
 						if (sendEmail()) {
 							changeToForgotInterface(false);
 							changeToLoginInterface(true);
 						} else {
 							// fikk ikke sendt mail av en eller annen grunn
 						}
 					}
 				} else if (text.equals("Register sheep")) {
 					System.out.println("sheep reg");
 					if (state == SHEEPREG) {
 						registerSheep();
 					} else {
 						changeToMainInterface(false);
 						changeToRegSheepInterface(true);
 					}
 				} else if (text.equals("List over sheeps")) {
 					System.out.println("sheep list");
 					changeToMainInterface(false);
 					changeToListInterface(true);
 				} else if (text.equals("Search for sheep")) {
 					System.out.println("sheep search");
 					if (state == SEARCH) {
 						searchSheep();
 					} else {
 						changeToMainInterface(false);
 						changeToSearchInterface(true);
 					}
 
 				} else if (text.equals("Edit sheep")) {
 					System.out.println("sheep edit");
 					changeToSearchInterface(false);
 					editButton.setVisible(false);
 					changeToEditInterface(true);
 
 					editSheep();
 				} else if (text.equals("Home")) {
 					changeToMainInterface(true);
 					// TODO
 				} else if (text.equals("Log out")) {
 					changeToMainInterface(false);
 					changeToStartInterface(true);
 					logout();
 				} else if (text.equals("Clear All")) {
 					editIdField.setText("");
 					editAgeField.setText("");
 					editWeightField.setText("");
 					editSexField.setText("");
 					editShepherdField.setText("");
 
 					clearAllButton.setBounds(width / 12, 110 * height / 200,
 							width / 12, 3 * height / 40);
 					regretButton.setVisible(true);
 				} else if (text.equals("Update")) {
 					for (JComponent c : editComps) {
 						if (c instanceof JTextField) {
 							if (((MyBorder) ((JTextField) c).getBorder())
 									.getColor().equals(invalid)) {
 								return; // return hvis et felt er r�dt
 							}
 						}
 					}
 					editSheep();
 				} else if (text.equals("Regret")) {
 					editIdField.setText("" + editSheep.getId());
 					editAgeField.setText("" + editSheep.getBirthyear());
 					editWeightField.setText("" + editSheep.getWeight());
 					editSexField.setText("" + editSheep.getGender());
 					editShepherdField.setText("" + editSheep.getShepherd());
 
 					clearAllButton.setBounds(width / 12, 110 * height / 200,
 							width / 6, 3 * height / 40);
 					regretButton.setVisible(false);
 				} else if (text.equals("DONT CLICK")) {
 					delete++;
 					if (delete % 4 == 1) {
 						changeMySheepButtonColors(1);
 					} else if (delete % 4 == 2) {
 						changeMySheepButtonColors(2);
 					} else if (delete % 4 == 2) {
 						changeMySheepButtonColors(3);
 					} else if (delete % 4 == 3) {
 						changeMySheepButtonColors(4);
 					} else {
 						changeMySheepButtonColors(5);
 					}
 				}
 			} else if (arg.getSource() instanceof JTextField) {
 				// System.out.println(((JTextField) arg.getSource()).getName());
 				JTextField pressed = (JTextField) arg.getSource();
 				String text = pressed.getName();
 				// Register user
 				System.out.println(text);
 				if (state == 1) {
 					if (text.equals("unField")) {
 						pwField.requestFocus();
 					} else if (text.equals("pwField")) {
 						// LOG IN!
 						login();
 					}
 				} else if (state == 2) {
 					if (text.equals("regNameField")) {
 						regLNameField.requestFocus();
 					} else if (text.equals("regLNameField")) {
 						regEmailField.requestFocus();
 					} else if (text.equals("regEmailField")) {
 						regPhoneField.requestFocus();
 					} else if (text.equals("regPhoneField")) {
 						regPwField.requestFocus();
 					} else if (text.equals("regPwField")) {
 						regPwField2.requestFocus();
 					} else if (text.equals("regPwField2")) {
 						// REGISTER USER!
 						if (registerUser()) {
 							changeToRegisterInterface(false);
 							changeToLoginInterface(true);
 						}
 					}
 				} else if (state == 3) {
 					if (text.equals("emailField")) {
 						// SEND EMAIL!
 						if (sendEmail()) {
 							changeToForgotInterface(false);
 							changeToLoginInterface(true);
 						} else {
 							// fikk ikke sendt mail av en eller annen grunn
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private class MySheepButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg) {
 
 		}
 
 	}
 
 	// Lytter for � h�ndtere tid. Til komponenter som skal flyttes over tid.
 	// Animasjoner!
 	/**
 	 * A class that handles time. Can be used for animations.
 	 * 
 	 * @author andreas
 	 * 
 	 */
 	private class TimerListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 		}
 	}
 
 	// Variables for swing
 	private JButton loginButton;
 	private JButton registerButton;
 	private JButton forgotButton;
 	private JButton backButton;
 	private JButton exitButton;
 	private JButton sendButton;
 
 	private MyImagePanel rightPanel;
 	private MyImagePanel leftPanel;
 
 	// login components
 	private JTextField unField;
 	private JPasswordField pwField;
 
 	// forgot components
 	private JTextField emailField;
 
 	// register components
 	private JTextField regNameField;
 	private JTextField regLNameField;
 	private JTextField regEmailField;
 	private JTextField regPhoneField;
 	private JTextField regLongitudeField;
 	private JTextField regLatitudeField;
 	private JPasswordField regPwField;
 	private JPasswordField regPwField2;
 
 	// main components
 	private JButton sheepRegButton;
 	private JButton listButton;
 	private JButton searchButton;
 	private JButton editButton;
 	private JButton homeButton;
 	private MyMap myMap;
 
 	// search components
 	private JLabel searchLabel;
 	private JTextField searchField;
 
 	// edit components
 	private JTextField editIdField;
 	private JTextField editAgeField;
 	private JTextField editWeightField;
 	private JTextField editSexField;
 	private JTextField editShepherdField;
 	private JButton clearAllButton;
 	private JButton updateButton;
 	private JButton regretButton;
 
 	// list components
 	private JButton sheepy;
 	private DefaultListModel sheepListModel;
 	private JList sheepList;
 	private JScrollPane sheepScrollPane;
 
 	// register sheep components
 	private JTextField regIDField;
 	private JTextField regAgeField;
 	private JTextField regWeightField;
 	private JTextField regSexField;
 	private JTextField regShepherdField;
 
 	// Sheep button list
 	private ArrayList<MySheepButton> mySheepButtons;
 
 	// lower panel
 	private JButton lwEditButton;
 
 	private JLabel lwEditLabel;
 	private JTextField lwEditIdField;
 	private JTextField lwEditAgeField;
 	private JTextField lwEditWeightField;
 	private JTextField lwEditOwnerField;
 	private JTextField lwEditShepherdField;
 	private JTextField lwEditHeartrateField;
 	private JTextField lwEditTemperatureField;
 	private JTextField lwEditBirthyearField;
 
 	private JLabel lwEditIdLabel;
 	private JLabel lwEditAgeLabel;
 	private JLabel lwEditWeightLabel;
 	private JLabel lwEditOwnerLabel;
 	private JLabel lwEditShepherdLabel;
 	private JLabel lwEditHeartrateLabel;
 	private JLabel lwEditTemperatureLabel;
 	private JLabel lwEditBirthyearLabel;
 
 	// List over the different components
 	private ArrayList<JComponent> buttonComps;
 	private ArrayList<JComponent> startComps;
 	private ArrayList<JComponent> loginComps;
 	private ArrayList<JComponent> registerComps;
 	private ArrayList<JComponent> forgotComps;
 	private ArrayList<JComponent> mainComps;
 	private ArrayList<JComponent> searchComps;
 	private ArrayList<JComponent> editComps;
 	private ArrayList<JComponent> listComps;
 	private ArrayList<JComponent> regSheepComps;
 	private ArrayList<JComponent> lwEditComps;
 
 	private ArrayList<JComponent> fieldComps;
 }
