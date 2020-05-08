 package herbstJennrichLehmannRitter.ui.GUI;
 
 import herbstJennrichLehmannRitter.engine.Globals;
 import herbstJennrichLehmannRitter.ki.KI;
 import herbstJennrichLehmannRitter.server.GameServer;
 import herbstJennrichLehmannRitter.ui.impl.LocalEnemyKIUserInterface;
 
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
 import org.eclipse.swt.widgets.Text;
 
 /**	Description of GameMenuGUI Class
  * Implementation of Menu Selection
  */
 
 public class GameMenuGUI extends AbstractMagicGUIElement {
 
 	private Text nameTextField;
 	private Button startHostButton;
 	private Button startClientButton;
 	private Button startLocalButton;
 	private Button backButton;
 	
 	private MainMenuGUI mainMenuGUI;
 	protected GameServer gameServer;
 	
 	private ClientMenuGUI clientMenuGUI;
 	
 	public GameMenuGUI(Display parent, MainMenuGUI mainMenuGUI) {
 		super(parent);
 		this.mainMenuGUI = mainMenuGUI;
 		this.clientMenuGUI = new ClientMenuGUI(getDisplay(), this.mainMenuGUI);
 		initGUI();
 	}
 	
 	@Override
 	protected void onInitGUI() {
 		initNameTextLabel();
 		initNameTextField();
 		initSelectionTextLabel();
 		initStartHostButton();
 		initStartClientButton();
 		initStartLocalButton();
 		initBackButton();
 	}
 	
 	@Override
 	protected void onInitShell() {
 		getShell().setText("Spielauswahl");
 		getShell().setLayout(new GridLayout(1, false));
 	}
 	
 	private void initNameTextLabel() {
 		createLabel("Bitte geben Sie ihren Namen an:");
 	}
 	
 	private void initNameTextField() {
 	    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalSpan = 3;
 		
 		this.nameTextField = new Text(getShell(), SWT.FILL);
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
 				GameMenuGUI.this.gameServer = Globals.getLocalGameServer();
 				GameMenuGUI.this.mainMenuGUI.setGameServer(GameMenuGUI.this.gameServer);
 
 				HostMenuGUI hostMenuGUI = new HostMenuGUI(getDisplay(), GameMenuGUI.this.mainMenuGUI);
 				
 				PlayGameGUI playGameGUI = new PlayGameGUI(getDisplay(), 
 						GameMenuGUI.this.mainMenuGUI.getClientUserInterface(), 
 							GameMenuGUI.this.gameServer);
 				
 				playGameGUI.setPlayerName(GameMenuGUI.this.mainMenuGUI.getPlayerName());
 				playGameGUI.setEnemyName("Gegner");
 				
 				hostMenuGUI.setPlayGameGUI(playGameGUI);
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
 				GameMenuGUI.this.clientMenuGUI.open();
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
 					
 					PlayGameGUI playGameGUI = new PlayGameGUI(getDisplay(),
 							GameMenuGUI.this.mainMenuGUI.getClientUserInterface(),
 							GameMenuGUI.this.mainMenuGUI.getGameServer());
 
 					GameMenuGUI.this.gameServer.register(GameMenuGUI.this.mainMenuGUI.
																		getClientUserInterface());
 
 					LocalEnemyKIUserInterface localUserInterface = new LocalEnemyKIUserInterface();
 					localUserInterface.setMainMenuGUI(GameMenuGUI.this.mainMenuGUI);
 					localUserInterface.setPlayGameGUI(playGameGUI);
 					
 					KI.startBridgedKIOnServer(GameMenuGUI.this.gameServer, GameMenuGUI.this.mainMenuGUI.
																	getEnemyName(),localUserInterface);
 					
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
 				getShell().close();
 			}
 		});
 	}
 	
 	private Button createButton(String text) {
 		Button button = new Button(getShell(), SWT.NONE);
 		button.setText(text);
 		button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		return button;
 	}
 	
 	private void createLabel(String text) {
 	    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalSpan = 3;
 		
 		Label label = new Label(getShell(), SWT.FILL);
 		label.setText(text);
 		label.setBackground(getShell().getBackground());
 		label.setLayoutData(gridData);
 	}
 
 }
