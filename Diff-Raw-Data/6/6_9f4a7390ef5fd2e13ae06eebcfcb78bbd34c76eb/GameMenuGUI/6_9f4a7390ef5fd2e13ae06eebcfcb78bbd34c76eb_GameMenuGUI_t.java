 package herbstJennrichLehmannRitter.ui.GUI;
 
 import herbstJennrichLehmannRitter.engine.Globals;
 import herbstJennrichLehmannRitter.ki.KI;
 import herbstJennrichLehmannRitter.server.GameServer;
 import herbstJennrichLehmannRitter.ui.impl.LocalUserInterface;
 
 import java.rmi.RemoteException;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**	Description of GameMenuGUI Class
  * Implementation of Menu Selection
  */
 
 public class GameMenuGUI {
 
 	private Shell shell;
 	private final Display display;
 	private Text nameTextField;
 	private Button startHostButton;
 	private Button startClientButton;
 	private Button startLocalButton;
 	private Button backButton;
 	
 	private MainMenuGUI mainMenuGUI;
 	protected GameServer gameServer;
 	
 	public GameMenuGUI(Display parent, MainMenuGUI mainMenuGUI) {
 		this.display = parent;
 		this.mainMenuGUI = mainMenuGUI;
 		initShell();
 		initNameTextLabel();
 		initNameTextField();
 		initSelectionTextLabel();
 		initStartHostButton();
 		initStartClientButton();
 		initStartLocalButton();
 		initBackButton();
 		this.shell.pack();
 		MainMenuGUI.setShellLocationCenteredToScreen(this.display, this.shell);
 	}
 	
 	public void open() {
 		this.shell.open();
 	}
 	
 	private void initShell() {
 		this.shell = new Shell(SWT.TITLE);
 		this.shell.setText("Spielauswahl");
 		this.shell.setLayout(new GridLayout(1, false));
 	}
 	
 	private void initNameTextLabel() {
 		createLabel("Bitte geben Sie ihren Namen an:");
 	}
 	
 	private void initNameTextField() {
 	    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalSpan = 3;
 		
 		this.nameTextField = new Text(this.shell, SWT.FILL);
 		this.nameTextField.setText(this.mainMenuGUI.getPlayerName());
 		this.nameTextField.setLayoutData(gridData);
 		this.nameTextField.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				Text changedText = (Text)e.widget;
 				GameMenuGUI.this.mainMenuGUI.setPlayerName(changedText.getText());
 			}
 		});
 	}
 	
 	private void initSelectionTextLabel() {
 		createLabel("Wählen Sie ihre Spieloption aus:");
 	}
 	
 	private void initStartHostButton() {
 		this.startHostButton = createButton("Starte als Host");
 		this.startHostButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				HostMenuGUI hostMenuGUI = new HostMenuGUI(GameMenuGUI.this.display, GameMenuGUI.this.mainMenuGUI);
 				PlayGameGUI playGameGUI = new PlayGameGUI(GameMenuGUI.this.display, 
 						GameMenuGUI.this.mainMenuGUI.getClientUserInterface(),
 						GameMenuGUI.this.mainMenuGUI.getGameServer());
 				playGameGUI.setPlayerName(GameMenuGUI.this.mainMenuGUI.getPlayerName());
 				GameMenuGUI.this.mainMenuGUI.getClientUserInterface().setPlayGameGUI(playGameGUI);
 				hostMenuGUI.open();
 			}
 		});
 	}
 	
 	private void initStartClientButton() {
 		this.startClientButton = createButton("Starte als Client");
 		this.startClientButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ClientMenuGUI clientMenuGUI = new ClientMenuGUI(GameMenuGUI.this.display, GameMenuGUI.this.mainMenuGUI);
 				clientMenuGUI.open();
 			}
 		});
 	}
 	
 	private void initStartLocalButton() {
 		this.startLocalButton= createButton("Lokales Spiel starten");
 		this.startLocalButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				try {
					GameMenuGUI.this.gameServer = Globals.getLocalGameServer();
					GameMenuGUI.this.mainMenuGUI.setGameServer(GameMenuGUI.this.gameServer);
					
 					PlayGameGUI playGameGUI = new PlayGameGUI(GameMenuGUI.this.display,
 							GameMenuGUI.this.mainMenuGUI.getClientUserInterface(),
 							GameMenuGUI.this.mainMenuGUI.getGameServer());
 
 					GameMenuGUI.this.gameServer.register(GameMenuGUI.this.mainMenuGUI.getClientUserInterface());
 
 					LocalUserInterface localUserInterface = new LocalUserInterface();
 					localUserInterface.setMainMenuGUI(GameMenuGUI.this.mainMenuGUI);
 					localUserInterface.setPlayGameGUI(playGameGUI);
 					
 					KI.startBridgedKIOnServer(GameMenuGUI.this.gameServer, GameMenuGUI.this.mainMenuGUI.getEnemyName(), 
 							localUserInterface);
 					
 					playGameGUI.setPlayerName(GameMenuGUI.this.mainMenuGUI.getPlayerName());
 					playGameGUI.setEnemyName(GameMenuGUI.this.mainMenuGUI.getEnemyName());
 					playGameGUI.open();
 				} catch (RemoteException e2) {
 					e2.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	private void initBackButton() {
 		this.backButton = createButton("Zurück");
 		this.backButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				GameMenuGUI.this.shell.setVisible(false);
 			}
 		});
 	}
 	
 	private Button createButton(String text) {
 		Button button = new Button(this.shell, SWT.NONE);
 		button.setText(text);
 		button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		return button;
 	}
 	
 	private void createLabel(String text) {
 	    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalSpan = 3;
 		
 		Label label = new Label(this.shell, SWT.FILL);
 		label.setText(text);
 		label.setBackground(this.shell.getBackground());
 		label.setLayoutData(gridData);
 	}
 
 }
