 package Part1;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Rectangle;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.event.MouseInputAdapter;
 import Part1.DijkstraSP.CompareType;
 import Part1.DijkstraSP.TransportWay;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 /*
  * Window class is the GUI of our program, which puts the map and other components together
  */ 
 
 public class Window extends JFrame {
 	
 	private static final long serialVersionUID = 1L;
 	private static Window instance;
 	private static JLayeredPane screen;
 	private static Container contentPane;
 	private boolean dragging;
 	private boolean noMoreBoxes;
 
 	//Fields used for drag zoom
 	private static int pressedX;
 	private static int pressedY;
 	private static int releasedX;
 	private static int releasedY;
 	private static JComponent rect;
 	private Timer timer;
 	private static int maxHeight;
 
 	//Buttons to pan and zoom
 	private JButton resetZoom, zoomOut, zoomIn, shortest, fastestButton, shipUnselected, shipSelected, search, findButton;
 	private JButton west, east, north, south, findPath, bikeUnselected, bikeSelected, carUnselected, carSelected, reset, findPathBlue;
 	private JButton findPlace, findPlaceBlue;
 	private JTextField from, to, find;
 	private JComboBox searchFromResultBox, searchToResultBox, searchFindResultBox;
 	private boolean navigateVisible = false;
 	private boolean fromMarked, toMarked, findMarked;
 	
 	private boolean byCar = true;
 	private boolean fastest = true;
 	private boolean byShip = true;
 	
 	// The tree flags
 	//private Flag fromFlag = new Flag(true);
 	//private Flag toFlag = new Flag(false);
 	private Flag fromFlag = new Flag(1);
 	private Flag toFlag = new Flag(2);
 	private Flag findFlag = new Flag(3);
 	
 	// Currently saved from and to text
 	private String fromText;
 	private String toText;
 	private String findText;
 	
 	//Currently saved node to search for
 	private Node findNode;
 	
 	// The label holding the current city and zip
 	private JLabel cityAndZipLabel;
 	
 	// The default text in the textfields
 	private final String fromDefault = "Enter startpoint";
 	private final String toDefault = "Enter destination";
 	private final String findDefault = "Enter address";
 	
 	//GUI background
 	private JPanel background, routeInfo;
 
 	// The temporary displacement of the buffered image
 	private int mousePanX;	
 	private int mousePanY;
 	
 	private int infoSize;
 	
 	public enum TextType {
 		FIND, TO, FROM;
 	}
 
 	/**
 	 * Constructor for the window class.
 	 */
 	private Window(){
 		super("Pytheas");
 	}
 
 	/**
 	 * Checks if an instance of the class is created.
 	 * If not a new is created.
 	 * 
 	 * @return The current instance of window. If no such a new i created and returned.
 	 */
 	public static Window use() {
 		if(instance == null) {
 			instance = new Window();
 			instance.makeContent();
 		}
 		return instance;
 	}
 
 	/**
 	 * The method is used to set up the initial instance of the window.
 	 * After setting the dimension and the layout a splash-screen
 	 * is displayed while the program is loading.
 	 */
 	private void makeContent(){
 		getEffectiveScreenSize();
 		contentPane = getContentPane();
 		setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
 		contentPane.setPreferredSize(new Dimension((int)(640*WindowHandler.getRatio()),640)); //Sets the dimension on the content pane.
 		screen = new JLayeredPane();	
 		screen.add(Map.use(), JLayeredPane.DEFAULT_LAYER);
 		contentPane.add(screen);
 		createButtons();
 		addButtons();	
 		setBackground(new Color(71, 180, 201));
 		pack();
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	/**
 	 * Adds the needed listeners to the window instance.
 	 */
 	public void addListeners() {
 		addKeyListener(new MKeyListener());
 		addComponentListener(new resizeListener());
 		Map.use().addMouseWheelListener(new mouseWheel());
 		//Listeners for when mouse is pressed, dragged or released
 		MouseListener mouseListener = new MouseListener();
 		Map.use().addMouseListener(mouseListener);
 		Map.use().addMouseMotionListener(mouseListener);
 		//Listener for the buttons for zoom, pan
 		addButtonListeners();
 	}
 
 	/**
 	 * Used to get the effective size of the screen. 
 	 * The effective size of the screen is equals to the size of the screen 
 	 * excluding the size reserved for other objects such as docks and tool bars.
 	 */
 	private static void getEffectiveScreenSize() {
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		maxHeight  = ge.getMaximumWindowBounds().height;
 	}
 
 	/**
 	 * Redraws the map when its content has changed or 
 	 * the window has been resized. 
 	 */
 	public void updateMap() {
 		Map.use().updatePath();
 		validate();
 				
 		if(!isVisible()){
 			SplashScreen.use().setAlwaysOnTop(true);
 			Map.use().setBounds(0, 0, contentPane.getPreferredSize().width, contentPane.getPreferredSize().height);		
 			addListeners();
 			setVisible(true);
 			SplashScreen.use().setAlwaysOnTop(false);
 		} else {
 			requestFocus();			
 		}
 		Map.use().createBufferImage();
 		repaint();
 	}
 
 	/**
 	 * Creates the buttons which makes up the GUI
 	 */
 	private void createButtons() {
 		//Icons from http://www.iconfinder.com/search/?q=iconset%3Abrightmix
 		resetZoom = createButton("ResetZoom.png", "Reset zoom", 75, 75);
 		zoomOut = createButton("minus_black.png", "Zoom out", 105, 175);
 		zoomIn = createButton("plus_black.png", "Zoom in", 55, 175);
 		west = createButton("West.png", "West", 25, 75);
 		east = createButton("East.png", "East", 125, 75);
 		north = createButton("North.png", "North",75, 25);
 		south = createButton("South.png", "South", 75, 125);
 		
 		findPath = createButton("flag3.png", "Find Path", 100, 215);
 		findPathBlue = createButton("flag_blue.png", "Find Path", 100, 215);
 		findPathBlue.setVisible(false);
 		findPlace = createButton("pin_grey.png", "Find Place", 50, 215);
 		findPlaceBlue = createButton("pin_blue.png", "Find Place", 50, 215);
 		findPlace.setVisible(false);
 		
 		bikeUnselected = createButton("cycle_unmarked.png", "By bike or walking", 25, 330);
 		bikeUnselected.setVisible(false);
 		bikeSelected = createButton("cycle_marked.png", "By bike or walking", 25, 330);
 		bikeSelected.setVisible(false);
 		carUnselected = createButton("motor_unmarked.png", "By car", 78, 330);
 		carUnselected.setVisible(false);
 		carSelected = createButton("motor_marked.png", "By car", 78, 330);
 		carSelected.setVisible(false);
 		shipUnselected = createButton("ferry_unmarked.png", "I would like to travel with ferry", 125, 330);
 		shipUnselected.setVisible(false);
 		shipSelected = createButton("ferry_marked.png", "I would like to travel with ferry", 125, 330);
 		shipSelected.setVisible(false);
 		
 		shortest = new JButton("Shortest");
 		shortest.setMargin(new Insets(5,5,5,5));
 		shortest.setBounds(20, 375, 70, 20);
 		shortest.setVisible(false);
 		
 		fastestButton = new JButton("Fastest");
 		fastestButton.setMargin(new Insets(5,5,5,5));
 		fastestButton.setBounds(95, 375, 70, 20);
 		fastestButton.setVisible(false);
 		
 		searchFromResultBox = new JComboBox();
 		searchToResultBox = new JComboBox();
 		searchFindResultBox = new JComboBox();
 
 		from = new JTextField(fromDefault);
 		fromText = fromDefault;
 		from.setBounds(20, 260, 145, 25);
 		from.setVisible(false);
 		
 		to = new JTextField(toDefault);
 		toText = toDefault;
 		to.setBounds(20, 295, 145, 25);
 		to.setVisible(false);
 		
 		find = new JTextField(findDefault);
 		findText = findDefault;
 		find.setBounds(20, 260, 145, 25);
 		find.setVisible(true);
 		
 		search = new JButton("Search");
 		search.setBounds(20, 405,70, 20);
 		search.setMargin(new Insets(5,5,5,5));
 		search.setFont(null);
 		search.setVisible(false);
 		
 		findButton = new JButton("Find");
 		findButton.setBounds(20, 295,70, 20);
 		findButton.setMargin(new Insets(5,5,5,5));
 		findButton.setFont(null);
 		findButton.setVisible(true);
 		
 		reset = new JButton("Reset");
 		reset.setBounds(95, 295, 70, 20);
 		reset.setMargin(new Insets(5,5,5,5));
 		reset.setFont(null);
 		reset.setVisible(true);
 		
 		cityAndZipLabel = new JLabel("");
 		cityAndZipLabel.setBounds(20, 640-40, 200, 20);
 		cityAndZipLabel.setVisible(true);
 		
 		//Internet magic from http://tips4java.wordpress.com/2009/05/31/backgrounds-with-transparency/
 		background = new JPanel(){
 		
 		    /**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			protected void paintComponent(Graphics g)
 		    {
 		        g.setColor( getBackground() );
 		        g.fillRect(0, 0, getWidth(), getHeight());
 		        super.paintComponent(g);
 		    }
 		};
 		background.setOpaque(false);
 		background.setBackground(new Color(65,105,225,50)); //royalblue
 		background.setBounds(10,15,165,315);
 		routeInfo = new JPanel(){
 			
 		    /**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			protected void paintComponent(Graphics g)
 		    {
 		        g.setColor( getBackground() );
 		        g.fillRect(0, 0, getWidth(), getHeight());
 		        super.paintComponent(g);
 		    }
 		};
 	}
 	
 	/**
 	 * Each button gets a listener with a corresponding action
 	 */
 	private void addButtonListeners(){
 
 		resetZoom.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				WindowHandler.resetMap();
 			}
 		});
 
 		zoomOut.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				WindowHandler.zoomOut();
 			}
 		});
 
 		zoomIn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				WindowHandler.zoomIn();
 			}
 		});
 
 		north.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				PanHandler.directionPan(Direction.NORTH);
 			}
 		});
 
 		south.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				PanHandler.directionPan(Direction.SOUTH);
 			}
 		});
 		
 		west.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				PanHandler.directionPan(Direction.WEST);
 			}
 		});
 
 		east.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				PanHandler.directionPan(Direction.EAST);
 			}
 		});
 		
 		from.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				fromText = from.getText();
 				addressParse(fromText, 185, 260, TextType.FROM);
 			}
 		});
 		
 		
 		from.addMouseListener(new mouseOnText(TextType.FROM));
 
 		to.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				toText = to.getText();
 				addressParse(toText, 185, 295, TextType.TO);
 			}
 		});
 		
 		to.addMouseListener(new mouseOnText(TextType.TO));
 		
 		find.addActionListener(new ActionListener() { //Find single address text field listener
 			public void actionPerformed(ActionEvent evt) {
 				findText = find.getText();
 				addressParse(findText, 185, 260, TextType.FIND);
 			}
 		});
 		
 		find.addMouseListener(new mouseOnText(TextType.FIND));
 		
 		bikeUnselected.addActionListener(new ActionListener(){ //If choosing bike
 
 			public void actionPerformed(ActionEvent evt) {
 				byCar = false;
 				carSelected.setVisible(false);
 				carUnselected.setVisible(true);
 				bikeSelected.setVisible(true);
 				bikeUnselected.setVisible(false);
 				shortest.setVisible(false);
 				fastestButton.setVisible(false);
 				search.setBounds(20, 375,70, 20);
 				reset.setBounds(95, 375, 70, 20);
 				fastest = false; //We want the shortest route if by bike
 			}
 		});
 		
 		carUnselected.addActionListener(new ActionListener(){ //If choosing car
 
 			public void actionPerformed(ActionEvent evt) {
 				byCar = true;
 				carSelected.setVisible(true);
 				carUnselected.setVisible(false);
 				bikeSelected.setVisible(false);
 				bikeUnselected.setVisible(true);
 				shortest.setVisible(true);
 				shortest.setFont(null);
 				reset.setBounds(95, 405, 70, 20);
 				fastestButton.setVisible(true);
 				fastestButton.setFont(new Font("Shortest", Font.BOLD, 12));
 				search.setBounds(20, 405,70, 20);
 				fastest = true; //We want the fastest route by default if by car
 			}
 		});
 		
 		shipUnselected.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				shipUnselected.setVisible(false);
 				shipSelected.setVisible(true);
 				byShip = true;
 			}
 		});
 		
 		shipSelected.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				shipSelected.setVisible(false);
 				shipUnselected.setVisible(true);
 				byShip = false;
 			}
 		});
 		
 		fastestButton.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				fastestButton.setFont(new Font("Fastest", Font.BOLD, 12));
 				shortest.setFont(null);
 				fastest = true;
 			}
 		});
 		
 		shortest.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				shortest.setFont(new Font("Shortest", Font.BOLD, 12));
 				fastestButton.setFont(null);
 				fastest = false;
 			}
 		});
 		
 		reset.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				from.setText(fromDefault);
 				fromText = fromDefault;
 				to.setText(toDefault);
 				toText = toDefault;
 				find.setText(findDefault);
 				findText = findDefault;
 				screen.remove(searchFromResultBox);
 				screen.remove(searchToResultBox);
 				screen.remove(routeInfo);
 				screen.remove(searchFindResultBox);
 				fromMarked = false;
 				toMarked = false;
 				Map.use().resetPath();
 				updateMap();
 			}
 		});
 
 		search.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				// If the text has been changed the user must choose a new address
 				if (!fromText.equals(from.getText())) fromMarked = false;
 				if (!toText.equals(to.getText())) toMarked = false;
 				
 				if(!fromMarked) {
 					String fromText = from.getText();
 					addressParse(fromText, 185, 260, TextType.FROM);
 				}
 				if (!toMarked) {	
 					String toText = to.getText();
 					addressParse(toText, 185, 295,TextType.TO);
 				}
 				
 				if(fromMarked && toMarked) {
 					System.out.println("searching for route");
 					//Checks if the user is going by car or bike, and if they want the shortest or fastest route
 					if(byCar) { 
 						if(fastest) {
 							WindowHandler.pathFinding(TransportWay.CAR, CompareType.FASTEST, byShip);
 						} else {
 							WindowHandler.pathFinding(TransportWay.CAR, CompareType.SHORTEST, byShip);
 						}
 					} else {
 						WindowHandler.pathFinding(TransportWay.BIKE, CompareType.SHORTEST, byShip);
 					}
 				}
 			}
 		});
 		
 		findButton.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				// If the text has been changed the user must choose a new address
 				if (!findText.equals(find.getText())) findMarked = false;
 				
 				if(!findMarked) {
 					String findText = find.getText();
 					addressParse(findText, 185, 260, TextType.FIND);
 				}
 				
 				if(findMarked) {
 					WindowHandler.centerOnNode(findNode);
 					updateMap();
 				}
 			}
 		});
 		
 		findPlace.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent evt) {
 				findPlace.setVisible(false);
 				findPlaceBlue.setVisible(true);
 				findPathBlue.setVisible(false);
 				findPath.setVisible(true);
 				
 				find.setVisible(true);
 				reset.setBounds(95, 295, 70, 20);
 				findPath.setVisible(true);
 				findPathBlue.setVisible(false);
 				to.setVisible(false);
 				from.setVisible(false);
 				bikeUnselected.setVisible(false);
 				bikeSelected.setVisible(false);
 				carUnselected.setVisible(false);
 				carSelected.setVisible(false);
 				searchFromResultBox.setVisible(false);
 				searchToResultBox.setVisible(false);
 				navigateVisible = false;
 				shortest.setVisible(false);
 				fastestButton.setVisible(false);
 				shipUnselected.setVisible(false);
 				shipSelected.setVisible(false);
 				search.setVisible(false);
 				findButton.setVisible(true);
 				background.setBounds(10,15,165,315);
 				repositionInfo();
 			}
 		});
 		
 		findPath.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent evt) {
 				if(!navigateVisible){
 					to.setVisible(true);
 					from.setVisible(true);
 					find.setVisible(false);
 					if (byCar) {
 						bikeUnselected.setVisible(true);
 						carSelected.setVisible(true);
 						reset.setBounds(95, 405, 70, 20);
 						if (fastest) {
 							shortest.setVisible(true);
 							fastestButton.setVisible(true);
 							fastestButton.setFont(new Font("Fastest", Font.BOLD, 12));
 							shortest.setFont(null);
 						}
 						else {
 							shortest.setVisible(true);
 							fastestButton.setVisible(true);
 							shortest.setFont(new Font("Shortest", Font.BOLD, 12));
 							fastestButton.setFont(null);
 						}
 					}
 					else {
 						bikeSelected.setVisible(true);
 						carUnselected.setVisible(true);
 						reset.setBounds(95, 375, 70, 20);
 					}
 					navigateVisible = true;
 					if (byShip) shipSelected.setVisible(true);
 					else shipUnselected.setVisible(true);
 					search.setVisible(true);
 					findButton.setVisible(false);
 					findPath.setVisible(false);
 					findPathBlue.setVisible(true);
 					findPlace.setVisible(true);
 					findPlaceBlue.setVisible(false);
 					searchFindResultBox.setVisible(false);
 					background.setBounds(10,15,165,420);
 					repositionInfo();
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Parses the address from the search text field to the search box
 	 * 
 	 * @param text The text to search for cities with
 	 * @param x The x-position of the search box to be created
 	 * @param y The y-position of the search box to be created
 	 * @param fromBool Set to true if the search comes from the "from-field". Set to false if the search comes from the "to-field"
 	 */
 	private void addressParse(String text, int x,int y, TextType t){		
 		String[] result = AddressParser.use().parseAddress(text);
 		String[] setArray = new String[0];
 		String[] zipArray;
 		String[] cityNameArray; // used to sort the results
 		
 		// If there has been typed in a city name
 		if (!result[5].equals("")) {
 			HashMap<String, String> zipToCityMap = WindowHandler.getZipToCityMap();
 			Set<String> zips = zipToCityMap.keySet();
 			ArrayList<String> zipList = new ArrayList<String>();
 			for (String zip : zips) {
 				if (result[5].toLowerCase().equals(zipToCityMap.get(zip).toLowerCase())) {
 					if (result[0].equals("")) {
 						zipList.add(zip);
 					}
 					else if (WindowHandler.getRoadToZipMap().get(result[0]).contains(zip)) {
 						zipList.add(zip);
 					}
 				}
 			}
 			cityNameArray = new String[zipList.size()];
 			setArray = new String[zipList.size()];
 			for (int i = 0; i < setArray.length; i++) {
 				setArray[i] = result[0]+" " + result[1] + result[2]+" " + result[3] + " " + zipList.get(i) + " " + result[5];
 				setArray[i] = setArray[i].replaceAll("\\s+", " ").trim();
 				cityNameArray[i] = result[5];
 			}
 			zipArray = Arrays.copyOf(zipList.toArray(), zipList.size(), String[].class);
 		}
 		// If there has been typed in a zip code
 		else if(!(result[4].equals(""))){
 			setArray = new String[1];
 			cityNameArray = new String[]{WindowHandler.getZipToCityMap().get(result[4])};
 			setArray[0] = result[0]+" " + result[1] + result[2]+" " + result[3] + " " + result[4]+ " " + WindowHandler.getZipToCityMap().get(result[4]);
 			setArray[0] = setArray[0].replaceAll("\\s+", " ").trim();
 			zipArray = new String[]{result[4]};
 			}
 		// If there has been typed in no city name or zip code
 		else{
 			HashSet<String> set = WindowHandler.getRoadToZipMap().get(result[0]);
 			if (set == null) setArray = new String[0];
 			else setArray = set.toArray(new String[0]);
 			cityNameArray = new String[setArray.length];
 			zipArray = new String[setArray.length];
 			for (int i = 0; i < setArray.length; i++) {
 				zipArray[i] = setArray[i];
 			}
 			for(int i = 0; i < setArray.length; i++){
 				String city = WindowHandler.getZipToCityMap().get(setArray[i]);
 				cityNameArray[i] = city;
 				setArray[i] = result[0]+" " + result[1] + result[2]+" " + result[3] + " " + setArray[i] + " " + city;
 				setArray[i] = setArray[i].replaceAll("\\s+", " ").trim();				
 			}						
 		}
 		if (zipArray.length == 0) setArray = new String[]{"No results"};
 		// sort the result according to the city name
 		Arrays.sort(cityNameArray);
 		for (int i = 0; i < cityNameArray.length; i++) {
 			for (int j = 0; j < setArray.length; j++) {
 				if (setArray[j].contains(cityNameArray[i])) {
 					String temp = setArray[i]; setArray[i] = setArray[j]; setArray[j] = temp;
 					temp = zipArray[i]; zipArray[i] = zipArray[j]; zipArray[j] = temp;
 					continue;
 				}
 			}
 		}
 		createSearchBox(setArray,zipArray,result,x,y,t);
 	}
 	
 	/**
 	 * Creates a search box which shows the search results from the address search fields
 	 * 
 	 * @param addressArray Holds the addresses that matches the search
 	 * @param zipArray Holds the zips matching the addresses in addressArray
 	 * @param textArray Holds the array resulting from parsing the text from the textfield
 	 * @param x The x-position of the search box
 	 * @param y The y-position of the search box
 	 * @param fromBool Set to true if the search text field is the "from field", set to false if the search text field if the "to field"
 	 */
 	private void createSearchBox(String[] addressArray, String[] zipArray, String[] textArray, int x, int y, TextType t){
 		if(t == TextType.FROM) {
 			screen.remove(searchFromResultBox);
 			searchFromResultBox = new JComboBox(addressArray);
 			searchFromResultBox.setBounds(x,y ,200,25);	
 			searchFromResultBox.addActionListener(new comboBoxListener(searchFromResultBox, zipArray, textArray, t));
 			screen.add(searchFromResultBox, JLayeredPane.PALETTE_LAYER);
 		} else if (t == TextType.TO) {
 			screen.remove(searchToResultBox);
 			searchToResultBox = new JComboBox(addressArray);
 			searchToResultBox.setBounds(x,y ,200,25);
 			searchToResultBox.addActionListener(new comboBoxListener(searchToResultBox, zipArray, textArray, t));
 			screen.add(searchToResultBox, JLayeredPane.PALETTE_LAYER);
 		}	
 		else if (t == TextType.FIND) {
 			screen.remove(searchFindResultBox);
 			searchFindResultBox = new JComboBox(addressArray);
 			searchFindResultBox.setBounds(x, y, 200, 25);
 			searchFindResultBox.addActionListener(new comboBoxListener(searchFindResultBox, zipArray, textArray, t));
 			screen.add(searchFindResultBox, JLayeredPane.PALETTE_LAYER);
 		}
 	}
 	
 	/**
 	 * Buttons added to the Pallette_Layer, above the map layer
 	 */
 	private void addButtons(){
 		screen.add(resetZoom, JLayeredPane.PALETTE_LAYER);
 		screen.add(zoomOut, JLayeredPane.PALETTE_LAYER);
 		screen.add(zoomIn, JLayeredPane.PALETTE_LAYER);
 		screen.add(west, JLayeredPane.PALETTE_LAYER);
 		screen.add(east, JLayeredPane.PALETTE_LAYER);
 		screen.add(north, JLayeredPane.PALETTE_LAYER);
 		screen.add(south, JLayeredPane.PALETTE_LAYER);
 		screen.add(from, JLayeredPane.PALETTE_LAYER);
 		screen.add(to, JLayeredPane.PALETTE_LAYER);
 		screen.add(find, JLayeredPane.PALETTE_LAYER);
 		screen.add(search, JLayeredPane.PALETTE_LAYER);
 		screen.add(findButton, JLayeredPane.PALETTE_LAYER);
 		screen.add(findPath, JLayeredPane.PALETTE_LAYER);
 		screen.add(findPathBlue, JLayeredPane.PALETTE_LAYER);
 		screen.add(bikeUnselected, JLayeredPane.PALETTE_LAYER);
 		screen.add(carUnselected, JLayeredPane.PALETTE_LAYER);
 		screen.add(bikeSelected, JLayeredPane.PALETTE_LAYER);
 		screen.add(carSelected, JLayeredPane.PALETTE_LAYER);
 		screen.add(fastestButton, JLayeredPane.PALETTE_LAYER);
 		screen.add(shortest, JLayeredPane.PALETTE_LAYER);
 		screen.add(shipUnselected, JLayeredPane.PALETTE_LAYER);
 		screen.add(shipSelected, JLayeredPane.PALETTE_LAYER);
 		screen.add(reset, JLayeredPane.PALETTE_LAYER);
 		screen.add(findPlace, JLayeredPane.PALETTE_LAYER);
 		screen.add(findPlaceBlue, JLayeredPane.PALETTE_LAYER);
 		screen.add(background, JLayeredPane.PALETTE_LAYER);
 		screen.add(cityAndZipLabel, JLayeredPane.PALETTE_LAYER);
 	}
 
 	/**
 	 * Method to create buttons, only to avoid code duplication
 	 */
 	private JButton createButton(String file, String hoverText, int x, int y){
 		ImageIcon icon = new ImageIcon(file, hoverText);
 		JButton button = new JButton(icon);		
 		button.setBorderPainted(false); 
 		button.setContentAreaFilled(false); 
 		button.setFocusPainted(false); 
 		button.setBounds(x, y, icon.getIconWidth(),icon.getIconHeight());
 		button.setToolTipText(hoverText);
 		return button;
 	}
 
 	public void addPathInfo(TransportWay transport) {
 		double dist = (Map.use().getPathLengt()/1000);
 		int hour = (int)Map.use().getDriveTime()/60, min = (int)Map.use().getDriveTime()%60;
 		DecimalFormat df = new DecimalFormat();
 		if(routeInfo != null) screen.remove(routeInfo);
 		routeInfo = new JPanel() {
 			protected void paintComponent(Graphics g)
 		    {
 		        g.setColor( getBackground() );
 		        g.fillRect(0, 0, getWidth(), getHeight());
 		        super.paintComponent(g);
 		    }
 		};
 		routeInfo.setLayout(new GridLayout(2, 1));
 		routeInfo.setOpaque(false);
 		routeInfo.setBackground(new Color(0,0,0,50));
 		String distStr;
 		if(dist < 1) {
 			df.applyPattern(".###");
 			df.setRoundingMode(RoundingMode.HALF_UP);
 			distStr = ("Distance: " + df.format(dist).replaceAll("\\.|,", "") + " m");
 		} else if (dist < 100) {
 			df.applyPattern("###.#");
 			df.setRoundingMode(RoundingMode.HALF_UP);
 			distStr = "Distance: " + df.format(dist) + " km";
 		} else {
 			df.applyPattern("####");
 			df.setRoundingMode(RoundingMode.HALF_UP);
 			distStr = "Distance: " + df.format(dist) + " km";
 		}
 		String timeStr;
 		if(hour < 1) {
 
 			timeStr = ("Time: " + min + " min");
 		} else {
 			timeStr = ("Time: " + hour + ":" + min);
 		}
 		if (transport == TransportWay.CAR) { 
 			infoSize = 50;
 			routeInfo.add(new JLabel(distStr, SwingConstants.HORIZONTAL));
 			routeInfo.add(new JLabel(timeStr, SwingConstants.HORIZONTAL));
 		}
 		else {
 			infoSize = 25;
 			routeInfo.add(new JLabel(distStr, SwingConstants.HORIZONTAL));
 		}
 			
 		repositionInfo();
 		if(dist > 0) routeInfo.setVisible(true);
 		else 	      routeInfo.setVisible(false);
 		screen.add(routeInfo, JLayeredPane.PALETTE_LAYER);
 		System.out.println("route Added");
 	}
 	
 	private void repositionInfo() {
 		Rectangle r = background.getBounds();
 		routeInfo.setBounds(10, (r.height + r.y +5), r.width, infoSize);
 	}
 
 
 	/**
 	 * Method for drawing the rectangle to show where the user is dragging for zoom
 	 * The method compares where the user is dragging from and to, and hereby calculates
 	 * the rectangle.
 	 */
 	@SuppressWarnings("serial")
 	static class DrawRect extends JComponent {
 		public void paint(Graphics g) {
 			super.paint(g);
 			g.setColor(Color.orange);
 			if(getMousePosition() != null && !Window.use().noMoreBoxes) {
 				if(pressedX < getMousePosition().x && pressedY < getMousePosition().y) {
 					g.drawRect(pressedX, pressedY, getMousePosition().x - pressedX, getMousePosition().y - pressedY);
 				}
 				else if(pressedX > getMousePosition().x && pressedY > getMousePosition().y) {
 					g.drawRect(getMousePosition().x, getMousePosition().y, pressedX - getMousePosition().x, pressedY - getMousePosition().y);
 				}
 				else if(pressedX < getMousePosition().x && pressedY > getMousePosition().y) {
 					g.drawRect(pressedX, getMousePosition().y, getMousePosition().x - pressedX, pressedY - getMousePosition().y);
 				}
 				else if(pressedX > getMousePosition().x && pressedY < getMousePosition().y) {
 					g.drawRect(getMousePosition().x, pressedY, pressedX - getMousePosition().x, getMousePosition().y - pressedY);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds a key listener used to move around the map.
 	 */
 	class MKeyListener extends KeyAdapter {
 		/**
 		 * Adds a key listener that sends the pressed button to the pan method of WindowHandler.
 		 */
 		public void keyPressed(KeyEvent e) {
 			switch(e.getKeyCode()){
 			case KeyEvent.VK_1:
 				WindowHandler.zoomOut();
 				break;
 			case KeyEvent.VK_2:
 				WindowHandler.zoomIn();
 				break;
 			case KeyEvent.VK_UP:
 				PanHandler.directionPan(Direction.NORTH);
 				break;
 			case KeyEvent.VK_DOWN:
 				PanHandler.directionPan(Direction.SOUTH);
 				break;
 			case KeyEvent.VK_LEFT:
 				PanHandler.directionPan(Direction.WEST);
 				break;
 			case KeyEvent.VK_RIGHT:
 				PanHandler.directionPan(Direction.EAST);
 				break;
 			}
 		}
 	}
 	
 	private class comboBoxListener implements ActionListener {
 		JComboBox searchResultBox;
 		TextType t; //From to or find comboBox
 		String[] zipArray, textArray;
 		
 		public comboBoxListener(JComboBox searchResultBox, String[] zipArray, String[] textArray, TextType t) {
 			this.searchResultBox = searchResultBox;
 			this.t = t;
 			this.zipArray = zipArray;
 			this.textArray = textArray;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {	
 			// if the zip array is empty, the search yielded no results
 			if (zipArray.length == 0) return;
 			
 			if(t == TextType.FROM) fromMarked = true; //register if the from or to combo box is marked
 			else if (t == TextType.TO) toMarked = true;
 			else if (t == TextType.FIND) findMarked = true;
 			int i = searchResultBox.getSelectedIndex();
 			Edge randomCorrectEdge = null;
 			Node flagNode = null;
 			String text = (String) searchResultBox.getSelectedItem();
 			// if there is no road name we look for a random road in the zip code area
 			if (textArray[0].equals("")) {
 				ArrayList<Edge> allEdgesForZip = new ArrayList<Edge>();
 				for(Edge edge : WindowHandler.getEdges()){
 					if (edge.getV_POSTNR().equals(zipArray[i]) && edge.getH_POSTNR().equals(zipArray[i])) {
 						allEdgesForZip.add(edge);
 					}
 				}
 				randomCorrectEdge = allEdgesForZip.get(allEdgesForZip.size()/2);
 			}
 			else {
 				for(Edge edge : WindowHandler.getEdges()){
 					if(edge.getVEJNAVN().equals(textArray[0]) && (edge.getV_POSTNR().equals(zipArray[i]) || edge.getH_POSTNR().equals(zipArray[i]) )){
 						randomCorrectEdge = edge;
 						String houseNumberString = textArray[1];
 						if (!houseNumberString.equals("")) {
 							int houseNumber = Integer.parseInt(houseNumberString);
 							if (houseNumber % 2 == 0) {
 								if (houseNumber >= edge.getHouseNumberMinEven() && houseNumber <= edge.getHouseNumberMaxEven()) {
 									WindowHandler.setNode(edge.getFromNode().getKdvID(), t);
 									flagNode = edge.getFromNode();
 									randomCorrectEdge = null;
 									break;
 								}
 							}
 							else if (houseNumber >= edge.getHouseNumberMinOdd() && houseNumber <= edge.getHouseNumberMaxOdd()) {
 									WindowHandler.setNode(edge.getFromNode().getKdvID(), t);
 									flagNode = edge.getFromNode();
 									randomCorrectEdge = null;
 									break;
 							}
 						}
 						else {
 							WindowHandler.setNode(edge.getFromNode().getKdvID(), t);
 							flagNode = edge.getFromNode();
 							randomCorrectEdge = null;
 							break;
 						}
 					}
 				}
 			}
 			if (randomCorrectEdge != null){
 				WindowHandler.setNode(randomCorrectEdge.getFromNode().getKdvID(), t);
 				flagNode = randomCorrectEdge.getFromNode();
 			}
 			searchResultBox.setVisible(false);
 			if (flagNode != null) {
 				if(t == TextType.FROM){
 					from.setText(text);
 					fromText = text;
 					double x = flagNode.getXCord();
 					double y = flagNode.getYCord();
 					fromFlag.setPosition(x, y);
 					Map.use().addFlag(fromFlag);
 				}
 				else if (t == TextType.TO) {
 					to.setText(text);
 					toText = text;
 					double x = flagNode.getXCord();
 					double y = flagNode.getYCord();
 					toFlag.setPosition(x, y);
 					Map.use().addFlag(toFlag);
 				}
 				else if (t == TextType.FIND) {
 					find.setText(text);
 					findText = text;
 					double x = flagNode.getXCord();
 					double y = flagNode.getYCord();
 					findFlag.setPosition(x, y);
 					Map.use().addFlag(findFlag);
 					findNode = flagNode;
 				}
 			}
 			updateMap();
 		}
 	}
 
 	/**
 	 * Adds a listener to the window instance.
 	 * The listener recalculates the position of each edge so 
 	 * that it fits the screen.
 	 */
 	private class resizeListener extends ComponentAdapter {
 		int height;
 		int width;
 		final int MIN_HEIGHT = 505;
 
 		public void componentResized(ComponentEvent evt) {
 			if(timer == null){
 				timer = new Timer(50, new ResizeTask());
 				timer.start();
 			}
 			timer.restart();
 		}
 
 		private class ResizeTask implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				timer.stop();
 				if(Window.use().getHeight() < maxHeight && Window.use().getWidth()/WindowHandler.getRatio() < maxHeight) {
 					if(Math.abs(height - Window.use().getHeight())>0 && Window.use().getHeight() < maxHeight){
 						if (Window.use().getHeight() < MIN_HEIGHT)
 							Window.use().setPreferredSize(new Dimension((int)(MIN_HEIGHT*WindowHandler.getRatio()), MIN_HEIGHT));
 						else
 							Window.use().setPreferredSize(new Dimension((int)(Window.use().getHeight()*WindowHandler.getRatio()), Window.use().getHeight()));
 					} else if(Math.abs(width - Window.use().getWidth())>0 && Window.use().getHeight() < maxHeight){
 						if (Window.use().getWidth()/WindowHandler.getRatio() < MIN_HEIGHT)
 							Window.use().setPreferredSize(new Dimension((int)(MIN_HEIGHT*WindowHandler.getRatio()), MIN_HEIGHT));
 						else
 							Window.use().setPreferredSize(new Dimension(Window.use().getWidth(), (int)(Window.use().getWidth()/WindowHandler.getRatio())));
 					}
 				} else {
 					Window.use().setPreferredSize(new Dimension((int)(maxHeight*WindowHandler.getRatio()), maxHeight));
 				}
 				cityAndZipLabel.setBounds(20, contentPane.getHeight()-40, 200, 20);
 				pack();
 				Map.use().setSize(Window.use().getSize());
 				if(Map.use().getRoadSegments() != null)
 					Map.use().updatePix();
 				height = Window.use().getHeight();
 				width = Window.use().getWidth();
 				timer = null;
 				Map.use().createBufferImage();
 			}
 		}
 	}
 	
 	private class MouseListener extends MouseInputAdapter {
 		int prevX;
 		int prevY;
 
 		public void mousePressed(MouseEvent e) {
 			if (SwingUtilities.isRightMouseButton(e)) {
 				pressedX = e.getX();
 				pressedY = e.getY();
 				noMoreBoxes = false;
 			} else if (SwingUtilities.isLeftMouseButton(e)) {
 				pressedX = e.getX();
 				pressedY = e.getY();
 			}
 		}
 
 		public void mouseDragged(MouseEvent e) {
 			if (SwingUtilities.isRightMouseButton(e) && !noMoreBoxes) {
 				rect = new DrawRect();
 				rect.setBounds(0, 0, contentPane.getWidth(), contentPane.getHeight());
 				screen.add(rect, JLayeredPane.POPUP_LAYER);
 			}
 			else if (SwingUtilities.isLeftMouseButton(e)) {
 				if (!dragging) {
 					prevX = e.getX();					// Before dragging starts, prevX and prevY is set to 
 					prevY = e.getY();					// the current cursor location.
 					dragging = true;					// Dragging is then set to begin.		
 				}										
 				else {
 					setMousePanX(e.getX() - prevX);		// While we are dragging, mousePanX and mousePanY is continually set to 
 					setMousePanY(e.getY() - prevY);		// difference between cursors start location and cursors current location.
 					repaint();							// The Window is then continually repainted using the override 
 				}										// method paint() in Map.
 			}
 		}
 		
 		/**
 		 *  Sets the tooltip with the road name and the label at the bottom of the screen with the city name and zip
 		 */
 		public void mouseMoved(MouseEvent e){
 			Edge closestEdge = WindowHandler.closestEdge(e.getX(), e.getY());
 			if (!(closestEdge == null)) {
 				String roadName;
 				if(closestEdge.getVEJNAVN().length() > 0) {
 					roadName = closestEdge.getVEJNAVN();
 				} else {
 					roadName =  "No name found";
 				}
 				Map.use().setToolTipText(roadName);
 				String zip = closestEdge.getH_POSTNR();
 				String cityName = WindowHandler.getZipToCityMap().get(zip);
 				if (cityName != null) {
 					cityAndZipLabel.setText(zip + " " + cityName);
 				}
 			}
 			
 		}
 		
 		public void mouseReleased(MouseEvent e) {
 			if (SwingUtilities.isRightMouseButton(e)) {
 				releasedX = e.getX();
 				releasedY =  e.getY();
 				WindowHandler.pixelSearch(pressedX, releasedX, pressedY, releasedY);
 				rect.setVisible(false); //Removes the rectangle when zoom box is chosen
 				noMoreBoxes = true;
 				updateMap();
 			}
 			else if (SwingUtilities.isLeftMouseButton(e)) {
 				if (dragging) {
 					PanHandler.pixelPan((prevX-e.getX()), (e.getY()-prevY));	// When mouse is released, the new map data is calculated.
 					setMousePanX(0);											// mousePanX and mousePanY is reset to zero.
 					setMousePanY(0);
 					dragging = false;											// Dragging ends.
 				}
 			}
 			Map.use().createBufferImage();										// The new image is drawn to the buffer and flipped in when
 		}																		// it is completed (see Map.flipImageBuffer() for details).
 	}
 	
 	private class mouseWheel implements MouseWheelListener{
 		int notches;
 		
 		public void mouseWheelMoved(MouseWheelEvent e) {
 			notches += e.getWheelRotation();
 			if(timer == null){
 				timer = new Timer(5, new mouseWheelMovedZoom());
 				timer.start();
 			}
 			timer.restart();
 		
 		}
 		private class mouseWheelMovedZoom implements ActionListener {
 			public void actionPerformed(ActionEvent e) {
 				timer.stop();
 				if (notches < 0) {
 					WindowHandler.zoomIn();
 				} else {	            
 					WindowHandler.zoomOut();
 				}
 				Map.use().createBufferImage();
 				}
 			}	
 	}
 
 //	private class mouseWheelZoom implements MouseWheelListener{
 //		public void mouseWheelMoved(MouseWheelEvent e) {
 //			int notches = e.getWheelRotation();
 //			if (notches < 0) {
 //				WindowHandler.zoomIn();
 //			} else {	            
 //				WindowHandler.zoomOut();
 //			}
 //			Map.use().createBufferImage();
 //		}
 //	}
 	
 	private class mouseOnText extends MouseAdapter {
 		private TextType t;
 		
 		mouseOnText(TextType t) {
 			this.t = t;
 		}
 		
 		@Override
 		public void mousePressed(MouseEvent e) {
 			if (TextType.FROM == t && from.getText().equals(fromDefault)) from.setText("");
 			else if (t == TextType.TO && to.getText().equals(toDefault)) to.setText("");
 			else if (t == TextType.FIND && find.getText().equals(findDefault)) find.setText("");			
 		}
 	}
 	
 	public int getMapWidth() {
 		return contentPane.getWidth();
 	}
 
 	public int getMapHeight() {
 		return contentPane.getHeight();
 	}
 
 	public int getMousePanX() {
 		return mousePanX;
 	}
 	
 	public int getMousePanY() {
 		return mousePanY;
 	}
 
 	public void setMousePanX(int inputMousePanX) {
 		mousePanX = inputMousePanX;
 	}
 
 	public void setMousePanY(int inputMousePanY) {
 		mousePanY = inputMousePanY;
 	}
 }
