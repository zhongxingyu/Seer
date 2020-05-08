 package gui;
 
import java.util.ArrayList;

 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import server.GameMethodsImpl;
 import server.TerritoryManager;
 import valueobjects.PlayerCollection;
 import valueobjects.Territory;
 
 /**
  * @author Hendrik
  */
 public class RiskGUI {
 	
 	private GameMethodsImpl game;
 	private Shell shell;
 	private Image map;
 	private Image[] units = new Image[6];
 	private Composite mainWindow;
 	private int imgWidth;
 	private int imgHeight;
 	private final int defaultSizeX = 800;
 	private final int defaultSizeY = 600;
 	private final int maxSizeX = 1920;
 	private final int maxSizeY = 1080;
 	private TerritoryManager territoryManager;
 	private PlayerCollection playerManager;
 	private Button[] button = new Button[42];
 	private Button[] playerButtons;
 	private Text eventWindow;
 	private String events = "";
 	private Player currentPlayer;
 	
 	/**
 	 * creates a new GUI and Game
 	 * @param display the Display on which the shell is shown
 	 */
 	public RiskGUI(Display display) {
 		
 		//{--- TEST SETUP START
 		eventWindowAppendText("Eine neue Runde Risiko wird gestartet!");
 
 		// Create the game instance
 		game = new GameMethodsImpl();
 		game.addPlayer("Hendrik");
 		game.addPlayer("Jannes");
 		game.addPlayer("Timur");
 		game.addPlayer("Philipp");
 		game.addPlayer("Teschke");
 		game.addPlayer("Eirund");
 		
 		territoryManager = game.getTerritoryManager();
		playerManager = game.getPlayerManager();
 		
 		//TEST SETUP ENDE ---}
 		
 		game.start();
 		
 		// Automatically place start units
 		game.placeStartUnitsRandomly();
 		
 		territoryManager = game.getTerritoryManager();
 		playerManager = game.getPlayers();
 		
 		//Create a new Shell with Title
 		shell = new Shell(display);
 		shell.setText("EinsZweiRisiko");
 		
 		//Set size to default
 		shell.setSize(defaultSizeX, defaultSizeY);
 
 		//Create a new Composite make it default size adjust the Shell
 		mainWindow = new Composite(shell, SWT.NONE);
 		mainWindow.setSize(defaultSizeX, defaultSizeY);
 		shell.pack();
 		
 		//Shell set minimum size to default size
 		shell.setMinimumSize(shell.getBounds().width, shell.getBounds().height);
 		
 		//Composite set size to max size
 		mainWindow.setSize(maxSizeX, maxSizeY);
 		
 		Device dev = shell.getDisplay();
 		
 		
 		//load images for Button
 		try {
 			map = new Image(dev, "assets/riskClean.png");
 			
 			//load unit pictures
 			//BLACK
 			units[0] = new Image(dev, "assets/unitsBLACK.png");
 			//BLUE
 			units[1] = new Image(dev, "assets/unitsGREEN.png");
 			//RED
 			units[2] = new Image(dev, "assets/unitsRED.png");
 			//YELLOW
 			units[3] = new Image(dev, "assets/unitsYELLOW.png");
 			//PINK
 			units[4] = new Image(dev, "assets/unitsPINK.png");
 			//GREEN
 			units[5] = new Image(dev, "assets/unitsBLUE.png");
 			
 			//rescale unit icons for Buttons
 			for(int i = 0; i < units.length; i++){
 				units[i] =  new Image(dev, units[i].getImageData().scaledTo(16, 16));
 			}
 		} catch (Exception e) {
 			System.out.println("Cannot load image");
 			System.out.println(e.getMessage());
 			System.exit(1);
 		}
 
 		imgWidth = map.getBounds().width;
 		imgHeight = map.getBounds().height;
 
 		mainWindow.setBackgroundImage(map);
 		
 		createButtons();
 		
 		createEventWindow();
 		
 		//createCardWindow();
 
 		//resize listener which auto centers the game
 		shell.addListener(SWT.Resize, new Listener() {
 			public void handleEvent(Event e) {
 				centerImage(mainWindow);
 				createButtons();
 				createEventWindow();
 			}
 		});
 
 		shell.addShellListener(new ShellListener() {
 			@Override
 			public void shellActivated(ShellEvent e) {
 				centerImage(mainWindow);
 				createButtons();
 				createEventWindow();
 			}
 
 			@Override
 			public void shellClosed(ShellEvent e) {
 				centerImage(mainWindow);
 				createButtons();
 				createEventWindow();
 			}
 
 			@Override
 			public void shellDeactivated(ShellEvent e) {
 				centerImage(mainWindow);
 				createButtons();
 				createEventWindow();
 			}
 
 			@Override
 			public void shellDeiconified(ShellEvent e) {
 				centerImage(mainWindow);
 				createButtons();
 				createEventWindow();
 			}
 
 			@Override
 			public void shellIconified(ShellEvent e) {
 				centerImage(mainWindow);
 				createButtons();
 				createEventWindow();
 			}
 		});
 
 		center(shell);
 
 		shell.open();
 
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 	
 	private void eventWindowAppendText(String string) {
 		events = string + "\n" + events;
 		
 	}
 
 	private void createEventWindow() {
 		mainWindow.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		eventWindow = new Text(mainWindow,SWT.NONE|SWT.MULTI|SWT.WRAP|SWT.RESIZE|SWT.V_SCROLL);
 		eventWindow.setBounds(0, 0, 250, 50);
 		eventWindow.setText(events);
 		eventWindow.setLocation(new Point(((imgWidth -shell.getClientArea().width)/ 2 +  shell.getClientArea().width - 250),((imgHeight - shell.getClientArea().height)/2 + shell.getClientArea().height -50)));
 	}
 
 	/**
 	 * creates a Button on every Territory and adds a Tooltip and lable to it
 	 */
 	private void createButtons() {
 		
 		//clear composite
 		for(Control kid: mainWindow.getChildren()){
 			kid.dispose();
 		}
 		
 		//size of the Buttons
 		int buttonSizeW = 45;
 		int buttonSizeH = 20;
 		
 		Territory territory;
 		
 		//NORD-AMERIKA
 		
 		//Alaska
 		territory = territoryManager.getTerritoryByName("Alaska");
 		button[0] = new Button(mainWindow, SWT.PUSH);
 		button[0].setImage(units[territory.getOwner().getColor()]);
 		button[0].setText(String.valueOf(territory.getUnits()));
 		button[0].setBounds(615-buttonSizeW/2, 332-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[0].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Nordwest-Territorium
 		territory = territoryManager.getTerritoryByName("Nordwest-Territorium");
 		button[1] = new Button(mainWindow, SWT.PUSH);
 		button[1].setImage(units[territory.getOwner().getColor()]);
 		button[1].setText(String.valueOf(territory.getUnits()));
 		button[1].setBounds(690-buttonSizeW/2, 342-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[1].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Alberta
 		territory = territoryManager.getTerritoryByName("Alberta");
 		button[2] = new Button(mainWindow, SWT.PUSH);
 		button[2].setImage(units[territory.getOwner().getColor()]);
 		button[2].setText(String.valueOf(territory.getUnits()));
 		button[2].setBounds(682-buttonSizeW/2, 382-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[2].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Ontario
 		territory = territoryManager.getTerritoryByName("Ontario");
 		button[3] = new Button(mainWindow, SWT.PUSH);
 		button[3].setImage(units[territory.getOwner().getColor()]);
 		button[3].setText(String.valueOf(territory.getUnits()));
 		button[3].setBounds(736-buttonSizeW/2, 400-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[3].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Quebec
 		territory = territoryManager.getTerritoryByName("Quebec");
 		button[4] = new Button(mainWindow, SWT.PUSH);
 		button[4].setImage(units[territory.getOwner().getColor()]);
 		button[4].setText(String.valueOf(territory.getUnits()));
 		button[4].setBounds(790-buttonSizeW/2, 404-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[4].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Weststaaten
 		territory = territoryManager.getTerritoryByName("Weststaaten");
 		button[5] = new Button(mainWindow, SWT.PUSH);
 		button[5].setImage(units[territory.getOwner().getColor()]);
 		button[5].setText(String.valueOf(territory.getUnits()));
 		button[5].setBounds(687-buttonSizeW/2, 445-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[5].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Oststaaten
 		territory = territoryManager.getTerritoryByName("Oststaaten");
 		button[6] = new Button(mainWindow, SWT.PUSH);
 		button[6].setImage(units[territory.getOwner().getColor()]);
 		button[6].setText(String.valueOf(territory.getUnits()));
 		button[6].setBounds(750-buttonSizeW/2, 465-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[6].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Mittel-Amerika
 		territory = territoryManager.getTerritoryByName("Mittelamerika");
 		button[7] = new Button(mainWindow, SWT.PUSH);
 		button[7].setImage(units[territory.getOwner().getColor()]);
 		button[7].setText(String.valueOf(territory.getUnits()));
 		button[7].setBounds(690-buttonSizeW/2, 512-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[7].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Grönland
 		territory = territoryManager.getTerritoryByName("Grönland");
 		button[8] = new Button(mainWindow, SWT.PUSH);
 		button[8].setImage(units[territory.getOwner().getColor()]);
 		button[8].setText(String.valueOf(territory.getUnits()));
 		button[8].setBounds(843-buttonSizeW/2, 320-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[8].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//SÜDAMERIKA
 		
 		//Venezuela
 		territory = territoryManager.getTerritoryByName("Venezuela");
 		button[9] = new Button(mainWindow, SWT.PUSH);
 		button[9].setImage(units[territory.getOwner().getColor()]);
 		button[9].setText(String.valueOf(territory.getUnits()));
 		button[9].setBounds(748-buttonSizeW/2, 571-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[9].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Peru
 		territory = territoryManager.getTerritoryByName("Peru");
 		button[10] = new Button(mainWindow, SWT.PUSH);
 		button[10].setImage(units[territory.getOwner().getColor()]);
 		button[10].setText(String.valueOf(territory.getUnits()));
 		button[10].setBounds(757-buttonSizeW/2, 641-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[10].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Brasilien
 		territory = territoryManager.getTerritoryByName("Brasilien");
 		button[11] = new Button(mainWindow, SWT.PUSH);
 		button[11].setImage(units[territory.getOwner().getColor()]);
 		button[11].setText(String.valueOf(territory.getUnits()));
 		button[11].setBounds(810-buttonSizeW/2, 623-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[11].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Argentinien
 		territory = territoryManager.getTerritoryByName("Argentinien");
 		button[12] = new Button(mainWindow, SWT.PUSH);
 		button[12].setImage(units[territory.getOwner().getColor()]);
 		button[12].setText(String.valueOf(territory.getUnits()));
 		button[12].setBounds(765-buttonSizeW/2, 710-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[12].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//EUROPA
 		
 		//Island
 		territory = territoryManager.getTerritoryByName("Island");
 		button[13] = new Button(mainWindow, SWT.PUSH);
 		button[13].setImage(units[territory.getOwner().getColor()]);
 		button[13].setText(String.valueOf(territory.getUnits()));
 		button[13].setBounds(910-buttonSizeW/2, 378-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[13].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Skandinavien
 		territory = territoryManager.getTerritoryByName("Skandinavien");
 		button[14] = new Button(mainWindow, SWT.PUSH);
 		button[14].setImage(units[territory.getOwner().getColor()]);
 		button[14].setText(String.valueOf(territory.getUnits()));
 		button[14].setBounds(968-buttonSizeW/2, 383-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[14].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Großbritannien
 		territory = territoryManager.getTerritoryByName("Großbritannien");
 		button[15] = new Button(mainWindow, SWT.PUSH);
 		button[15].setImage(units[territory.getOwner().getColor()]);
 		button[15].setText(String.valueOf(territory.getUnits()));
 		button[15].setBounds(892-buttonSizeW/2, 444-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[15].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//MittelEuropa
 		territory = territoryManager.getTerritoryByName("Mitteleuropa");
 		button[16] = new Button(mainWindow, SWT.PUSH);
 		button[16].setImage(units[territory.getOwner().getColor()]);
 		button[16].setText(String.valueOf(territory.getUnits()));
 		button[16].setBounds(967-buttonSizeW/2, 454-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[16].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//SüdEuropa
 		territory = territoryManager.getTerritoryByName("Südeuropa");
 		button[17] = new Button(mainWindow, SWT.PUSH);
 		button[17].setImage(units[territory.getOwner().getColor()]);
 		button[17].setText(String.valueOf(territory.getUnits()));
 		button[17].setBounds(976-buttonSizeW/2, 503-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[17].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//WestEuropa
 		territory = territoryManager.getTerritoryByName("Westeuropa");
 		button[18] = new Button(mainWindow, SWT.PUSH);
 		button[18].setImage(units[territory.getOwner().getColor()]);
 		button[18].setText(String.valueOf(territory.getUnits()));
 		button[18].setBounds(915-buttonSizeW/2, 508-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[18].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Ukraine
 		territory = territoryManager.getTerritoryByName("Ukraine");
 		button[19] = new Button(mainWindow, SWT.PUSH);
 		button[19].setImage(units[territory.getOwner().getColor()]);
 		button[19].setText(String.valueOf(territory.getUnits()));
 		button[19].setBounds(1042-buttonSizeW/2, 415-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[19].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//AFRIKA
 		
 		//Nordwestafrika
 		territory = territoryManager.getTerritoryByName("Nordwestafrika");
 		button[20] = new Button(mainWindow, SWT.PUSH);
 		button[20].setImage(units[territory.getOwner().getColor()]);
 		button[20].setText(String.valueOf(territory.getUnits()));
 		button[20].setBounds(938-buttonSizeW/2, 606-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[20].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Ägypten
 		territory = territoryManager.getTerritoryByName("Ägypten");
 		button[21] = new Button(mainWindow, SWT.PUSH);
 		button[21].setImage(units[territory.getOwner().getColor()]);
 		button[21].setText(String.valueOf(territory.getUnits()));
 		button[21].setBounds(998-buttonSizeW/2, 577-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[21].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Ostafrika
 		territory = territoryManager.getTerritoryByName("Ostafrika");
 		button[22] = new Button(mainWindow, SWT.PUSH);
 		button[22].setImage(units[territory.getOwner().getColor()]);
 		button[22].setText(String.valueOf(territory.getUnits()));
 		button[22].setBounds(1037-buttonSizeW/2, 649-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[22].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Kongo
 		territory = territoryManager.getTerritoryByName("Kongo");
 		button[23] = new Button(mainWindow, SWT.PUSH);
 		button[23].setImage(units[territory.getOwner().getColor()]);
 		button[23].setText(String.valueOf(territory.getUnits()));
 		button[23].setBounds(998-buttonSizeW/2, 678-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[23].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Südafrika
 		territory = territoryManager.getTerritoryByName("Südafrika");
 		button[24] = new Button(mainWindow, SWT.PUSH);
 		button[24].setImage(units[territory.getOwner().getColor()]);
 		button[24].setText(String.valueOf(territory.getUnits()));
 		button[24].setBounds(999-buttonSizeW/2, 743-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[24].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Madagaskar
 		territory = territoryManager.getTerritoryByName("Madagaskar");
 		button[25] = new Button(mainWindow, SWT.PUSH);
 		button[25].setImage(units[territory.getOwner().getColor()]);
 		button[25].setText(String.valueOf(territory.getUnits()));
 		button[25].setBounds(1075-buttonSizeW/2, 745-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[25].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//ASIEN
 		
 		//Ural
 		territory = territoryManager.getTerritoryByName("Ural");
 		button[26] = new Button(mainWindow, SWT.PUSH);
 		button[26].setImage(units[territory.getOwner().getColor()]);
 		button[26].setText(String.valueOf(territory.getUnits()));
 		button[26].setBounds(1121-buttonSizeW/2, 404-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[26].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Sibirien
 		territory = territoryManager.getTerritoryByName("Sibirien");
 		button[27] = new Button(mainWindow, SWT.PUSH);
 		button[27].setImage(units[territory.getOwner().getColor()]);
 		button[27].setText(String.valueOf(territory.getUnits()));
 		button[27].setBounds(1165-buttonSizeW/2, 365-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[27].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Jakutien
 		territory = territoryManager.getTerritoryByName("Jakutien");
 		button[28]= new Button(mainWindow, SWT.PUSH);
 		button[28].setImage(units[territory.getOwner().getColor()]);
 		button[28].setText(String.valueOf(territory.getUnits()));
 		button[28].setBounds(1219-buttonSizeW/2, 344-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[28].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Kamtschatka
 		territory = territoryManager.getTerritoryByName("Kamtschatka");
 		button[29]= new Button(mainWindow, SWT.PUSH);
 		button[29].setImage(units[territory.getOwner().getColor()]);
 		button[29].setText(String.valueOf(territory.getUnits()));
 		button[29].setBounds(1285-buttonSizeW/2, 346-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[29].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Irkutsk
 		territory = territoryManager.getTerritoryByName("Irkutsk");
 		button[30]= new Button(mainWindow, SWT.PUSH);
 		button[30].setImage(units[territory.getOwner().getColor()]);
 		button[30].setText(String.valueOf(territory.getUnits()));
 		button[30].setBounds(1200-buttonSizeW/2, 416-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[30].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Mongolei
 		territory = territoryManager.getTerritoryByName("Mongolei");
 		button[31]= new Button(mainWindow, SWT.PUSH);
 		button[31].setImage(units[territory.getOwner().getColor()]);
 		button[31].setText(String.valueOf(territory.getUnits()));
 		button[31].setBounds(1218-buttonSizeW/2, 460-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[31].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//China
 		territory = territoryManager.getTerritoryByName("China");
 		button[32]= new Button(mainWindow, SWT.PUSH);
 		button[32].setImage(units[territory.getOwner().getColor()]);
 		button[32].setText(String.valueOf(territory.getUnits()));
 		button[32].setBounds(1195-buttonSizeW/2, 509-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[32].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Japan
 		territory = territoryManager.getTerritoryByName("Japan");
 		button[33]= new Button(mainWindow, SWT.PUSH);
 		button[33].setImage(units[territory.getOwner().getColor()]);
 		button[33].setText(String.valueOf(territory.getUnits()));
 		button[33].setBounds(1299-buttonSizeW/2, 464-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[33].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Afghanistan
 		territory = territoryManager.getTerritoryByName("Afghanistan");
 		button[34]= new Button(mainWindow, SWT.PUSH);
 		button[34].setImage(units[territory.getOwner().getColor()]);
 		button[34].setText(String.valueOf(territory.getUnits()));
 		button[34].setBounds(1105-buttonSizeW/2, 472-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[34].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Mittlerer Osten
 		territory = territoryManager.getTerritoryByName("Mittlerer Osten");
 		button[35]= new Button(mainWindow, SWT.PUSH);
 		button[35].setImage(units[territory.getOwner().getColor()]);
 		button[35].setText(String.valueOf(territory.getUnits()));
 		button[35].setBounds(1054-buttonSizeW/2, 558-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[35].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Indien
 		territory = territoryManager.getTerritoryByName("Indien");
 		button[36]= new Button(mainWindow, SWT.PUSH);
 		button[36].setImage(units[territory.getOwner().getColor()]);
 		button[36].setText(String.valueOf(territory.getUnits()));
 		button[36].setBounds(1153-buttonSizeW/2, 550-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[36].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Siam
 		territory = territoryManager.getTerritoryByName("Siam");
 		button[37]= new Button(mainWindow, SWT.PUSH);
 		button[37].setImage(units[territory.getOwner().getColor()]);
 		button[37].setText(String.valueOf(territory.getUnits()));
 		button[37].setBounds(1214-buttonSizeW/2, 576-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[37].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//AUSTRALIEN
 		
 		//Indonesien
 		territory = territoryManager.getTerritoryByName("Indonesien");
 		button[38]= new Button(mainWindow, SWT.PUSH);
 		button[38].setImage(units[territory.getOwner().getColor()]);
 		button[38].setText(String.valueOf(territory.getUnits()));
 		button[38].setBounds(1226-buttonSizeW/2, 664-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[38].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Neu-Guinea
 		territory = territoryManager.getTerritoryByName("Neu-Guinea");
 		button[39]= new Button(mainWindow, SWT.PUSH);
 		button[39].setImage(units[territory.getOwner().getColor()]);
 		button[39].setText(String.valueOf(territory.getUnits()));
 		button[39].setBounds(1292-buttonSizeW/2, 638-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[39].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Westaustralien
 		territory = territoryManager.getTerritoryByName("West-Australien");
 		button[40]= new Button(mainWindow, SWT.PUSH);
 		button[40].setImage(units[territory.getOwner().getColor()]);
 		button[40].setText(String.valueOf(territory.getUnits()));
 		button[40].setBounds(1258-buttonSizeW/2, 740-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[40].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		//Ostaustralien
 		territory = territoryManager.getTerritoryByName("Ost-Australien");
 		button[41]= new Button(mainWindow, SWT.PUSH);
 		button[41].setImage(units[territory.getOwner().getColor()]);
 		button[41].setText(String.valueOf(territory.getUnits()));
 		button[41].setBounds(1306-buttonSizeW/2, 712-buttonSizeH/2, buttonSizeW, buttonSizeH);
 		button[41].setToolTipText(territory.getName() + " gehört " + territory.getOwner().getName());
 		
 		for(int i = 0; i < button.length; i++) {
 			button[i].addMouseListener(new MouseListener() {
 				@Override
 				public void mouseDoubleClick(MouseEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 
 				@Override
 				public void mouseDown(MouseEvent e) {
 					 openDialog(e);
 				}
 
 				@Override
 				public void mouseUp(MouseEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 		      });
 		}
 		
 		playerButtons = new Button[playerManager.size()];
 		int biggestButton = 0;
 		
 		//Create some Buttons and Display the Player names Colors and current Unitammount
 		//TODO rausfinden warum das der ALIGN nicht klappt
 		for(int i = 0; i < playerManager.size();i++){
 			playerButtons[i] = new Button(mainWindow, SWT.TOGGLE | SWT.LEFT);
 			playerButtons[i].setImage(units[playerManager.get(i).getColor()]);
 			playerButtons[i].setText(playerManager.get(i).getName() + "(" + playerManager.get(i).getAllUnits() + ")");
 			playerButtons[i].setAlignment(SWT.LEFT);
 			playerButtons[i].pack();
 			if(playerButtons[i].getBounds().width > biggestButton){
 				biggestButton = playerButtons[i].getBounds().width;
 				playerButtons[i].setSize(biggestButton, 20);
 			}
 			playerButtons[i].setLocation(new Point(((imgWidth -shell.getClientArea().width)/ 2 + 10),((imgHeight - shell.getClientArea().height)/2 + shell.getClientArea().height -10 - playerManager.size()*20 + (i*20))));
 		}
 		
 		//make all Buttons same size
 		for(Button button:playerButtons){
 			if(button.getBounds().width > biggestButton){
 				biggestButton = button.getBounds().width;
 				button.setSize(biggestButton, 20);
 			} else if (button.getBounds().width < biggestButton){
 				button.setSize(biggestButton, 20);
 			}
 		}
 	}
 
 	private void openDialog(MouseEvent e) {
 		
 		Button clickedButton = (Button) e.widget;
 		
 		
 		Territory territory = territoryManager.getTerritoryByName(cutTooltip(clickedButton.getToolTipText()));
 		
 		String phase;
 		
 		//ATTACK
 		phase = "ATTACK";
 		
 		//DENFENSE
 		
 		//PLACE
 		
 		//MOVE
 		boolean dialogCancel = true;
 		
 		ActionDialog dialog = new ActionDialog(shell,SWT.NONE,phase,territory);
 		
 		dialog.open();
 		
 		if(!dialogCancel){
 			territory.setUnits(Integer.parseInt(dialog.result.toString()));
 			
 			//currently this function disables all playowned buttons
 			for(int i = 0; i < button.length; i++){
 				Territory territory2 = territoryManager.getTerritoryByName(cutTooltip(button[i].getToolTipText()));
 				if(territory2.getOwner().equals(territory.getOwner())){
 					button[i].setEnabled(false);
 				}
 			}
 			//TODO TEST AUSGABE ENTFERNEN
 			eventWindowAppendText("XY" + " möchte mit " + dialog.result.toString() + " Einheiten von " + territory.getName() + " angreifen!");
 		}
 		Display.getCurrent().update();
 		shell.update();
 	}
 	
 	/**
 	 * Method to find out which territory the Button belongs to
 	 * @param toolTipText of the calling Button
 	 * @return the name of the Territory of the calling Button
 	 */
 	private String cutTooltip(String toolTipText) {
 		String cutted = "";
 		
 		char[] toolTipChar = toolTipText.toCharArray();
 		
 		for(int i = 0; i < toolTipChar.length; i++ ){
 			if(toolTipChar[i] == ' '){
 				if(cutted.equals("Mittlerer")){
 					cutted = "Mittlerer Osten";
 				}
 				return cutted;
 			}
 			cutted += toolTipChar[i];
 		}
 		
 		return cutted;
 	}
 
 
 	/**
 	 * Centers a Shell in the middle of the Screen
 	 * @param shell which should be centered
 	 */
 	private void center(Shell shell) {
 
 		Rectangle bds = shell.getDisplay().getBounds();
 
 		Point p = shell.getSize();
 
 		int nLeft = (bds.width - p.x) / 2;
 		int nTop = (bds.height - p.y) / 2;
 
 		shell.setBounds(nLeft, nTop, p.x, p.y);
 	}
 
 	/**
 	 * Centers an image in the middle of the Shell
 	 */
 	private void centerImage(Composite window) {
 		// center Composition
 		int shellClientWidth = shell.getClientArea().width;
 		int shellClientHeight = shell.getClientArea().height;
 
 		Rectangle bds = window.getBounds();
 		Point p = window.getSize();
 
 		int nLeft = (bds.width - p.x) / 2;
 		int nTop = (bds.height - p.y) / 2;
 
 		window.setLocation(-((imgWidth - shellClientWidth) / 2) + nLeft,
 				-((imgHeight - shellClientHeight) / 2) + nTop);
 	}
 
 	@Override
 	public void finalize() {
 		map.dispose();
 	}
 
 	public static void main(String[] args) {
 		Display display = new Display();
 		RiskGUI rFenster = new RiskGUI(display);		
 		rFenster.finalize();
 		display.dispose();
 	}
 }
