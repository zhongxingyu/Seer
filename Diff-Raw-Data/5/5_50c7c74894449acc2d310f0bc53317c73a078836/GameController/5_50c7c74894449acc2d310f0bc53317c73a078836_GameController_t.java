 package core;
 /**
  * Der GameController stellt das ViewModel nach dem MVVM Entwurfsmuster dar,
  * er bereitet die Daten fr die Visualisierung auf und beinhaltet die Ablauflogik
  *  
  * @author Sascha Ulbrich 
  */
 
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.ResourceBundle;
 
 import model.*;
 import utilities.*;
 import view.*;
 
 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.fxml.Initializable;
 import javafx.stage.Stage;
 
 public class GameController extends Application implements GameEventListener, Observer, Initializable{
 
 	private Game model;
 	private CommunicationServer comServ;
 	private KI ki;
 
 	//Konstanten fr Zugriff auf Property Array
 	public final int ROLE_PROPERTY = 0;
 	public final int OWNPOINTS_PROPERTY = 1;
 	public final int OPPPOINTS_PROPERTY = 2;
 	public final int OPPNAME_PROPERTY = 3;
 	public final int PATH_PROPERTY = 4;
 	public final int TIMEOUTSERVER_PROPERTY = 5;
 	public final int TIMEOUTDRAW_PROPERTY = 6;
 	public final int OPPTOKEN_PROPERTY = 7;
 	public final int OWNTOKEN_PROPERTY = 8;
 	public final int STATE_PROPERTY = 9;
 	public final int WINNER_PROPERTY = 10;
 	
 	//Properties fr DataBinding	
 	private SimpleStringProperty[] properties;
 	private SimpleStringProperty[][] styleField;
 	private ObservableList<Log.LogEntry> logItems;
 	private ObservableList<SetProperty> sets;
 	private ObservableList<GameProperty> savedGames;
 	
 	//--------------------- API Methoden fr UI-Controller -----------------------------------------	
 	/**
 	 * Methode um ein Spiel zu starten	  
 	 */
 	public void startGame(){
 		Log.getInstance().write("Controller: starte Spiel, FxThread:" + Platform.isFxApplicationThread());
 		newGame(Constants.gamefieldcolcount, Constants.gamefieldrowcount);		
 		properties[STATE_PROPERTY].set(Constants.STATE_GAME_RUNNING);
 	}
 	
 	/**
 	 * Methode um einen neuen Satz zu starten	  
 	 */
 	public void startSet(){
 		Log.getInstance().write("Controller: starte Satz, FxThread:" + Platform.isFxApplicationThread());
 		if(model.getLatestSet() != null){
 			model.save();
 		}
 		ki = new KI(model);
 		
 		model.newSet();
 		
 		//ComServer starten
 		comServ.enableReading(model.getTimeoutServer(), model.getPath(), model.getRole());
 		properties[STATE_PROPERTY].set(Constants.STATE_SET_RUNNING);
 	}
 	
 	/**
 	 * Methode um ein Satz zu beenden, falls es keinen letzten Zug gibt: oppMove = -1
 	 * @param der letzte Zug des Gegners :Byte  
 	 */	
 	public void endSet(byte oppMove){
 		Log.getInstance().write("Controller: beende Satz, FxThread:" + Platform.isFxApplicationThread());
 		comServ.disableReading();
 		if (oppMove > -1){
 			addOppMove(oppMove);					
 		}
 		properties[STATE_PROPERTY].set(Constants.STATE_SET_ENDED);
 		model.getLatestSet().setStatus(Constants.STATE_SET_ENDED);
 	}
 	
 	/**
 	 * Methode um das aktuelle Spiel zu beenden	  
 	 */	
 	public void endGame(){
 		Log.getInstance().write("Controller: beende Spiel, FxThread:" + Platform.isFxApplicationThread());
 		properties[STATE_PROPERTY].set(Constants.STATE_APP_RUNNING);
 		model.save();
 	}
 	
 	/**
 	 * Methode um ein Spiel zu laden
 	 * @param ID von Game :Integer
 	 */	
 	public void loadGame(int gameID){
 		Log.getInstance().write("Controller: Spiel wird geladen, FxThread:" + Platform.isFxApplicationThread());
 //		TODO: DBConnection.getInstance().loadGame(gameID);
 		Platform.runLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				updateField();
 				updateSets();
 				properties[ROLE_PROPERTY].set(String.valueOf(model.getRole()));
 				properties[OWNPOINTS_PROPERTY].set(String.valueOf(model.getOwnPoints()));
 				properties[OPPPOINTS_PROPERTY].set(String.valueOf(model.getOppPoints()));
 				properties[OPPNAME_PROPERTY].set(model.getOppName());
 				properties[PATH_PROPERTY].set(model.getPath());
 				properties[TIMEOUTSERVER_PROPERTY].set(String.valueOf(model.getTimeoutServer()));
 				properties[TIMEOUTDRAW_PROPERTY].set(String.valueOf(model.getTimeoutDraw()));
 				properties[WINNER_PROPERTY].set(String.valueOf(model.getLatestSet().getWinner()));
 				setTokens();				
 			}
 		});	
 		properties[STATE_PROPERTY].set(Constants.STATE_GAME_RUNNING);
 	}
 	
 	/**
 	 * Methode um einen gegnerischen Zug hinzuzufgen -> Berechnung und Ausfhrung eines neuen Zuges.
 	 * Sollte nur fr das manuelle Spielen verwendet werden, ansonsten ber Event starten.
 	 * @param Spalte :Byte 
 	 */	
 	public void oppMove(byte col){
 		Log.getInstance().write("Controller: gegnerischen Zug empfangen, FxThread:" + Platform.isFxApplicationThread());
 		if (col > -1){
 			addOppMove(col);					
 		}
 		byte newCol = ki.calculateNextMove(col);			
 		//Zug auf Server schreiben und Server wieder berwachen
 		comServ.writeMove(newCol, model.getPath(), model.getRole());
 		comServ.enableReading(model.getTimeoutServer(), model.getPath(), model.getRole());
 		
 		model.addMove(model.getRole(), newCol);
 	}
 	
 	/**
 	 * Methode um vom UI aus den Gewinner zu besttigen und somit den Satz abzuschlieen
 	 */	
 	public void confirmSetWinner(){
 		
 	}
 	
 	// Getter fr Properties 	
 	/**
 	 * @return Properties fr DataBinding mit UI:StringProperty
 	 */
 	public SimpleStringProperty[] properties() {
 		return properties;
 	}
 	
 	/**
 	 * @return Spielfeld :GameField
 	 */
 	public SimpleStringProperty[][] styleField() {
 		return styleField;
 	}	
 	
 	/**
 	 * @return Logeintrge :ObservableList<Log.LogEntry>
 	 */
 	public ObservableList<Log.LogEntry> logItems() {
 		return logItems;
 	}
 	
 	/**
 	 * @return Gespielte Stze :ObservableList<Log.LogEntry>
 	 */
 	public ObservableList<SetProperty> sets() {
 		return sets;
 	}
 	
 	/**
 	 * @return Liste der gespeicherten Spiele :ObservableList<GameProperty>
 	 */
 	public ObservableList<GameProperty> savedGames() {
 		//TODO: Liste der gespeicherten Spiel
 		return savedGames;
 	}
 	
 	// ------------------------------------- Behandlung von GameEvents (vom ComServer) --------------------------------
 	/* (non-Javadoc)
 	 * @see GameEventListener#handleEvent(GameEvent)
 	 */	
 	/**
 	 * Methode um auf GameEvents zu reagieren
 	 * @param das geworfene Event :GameEvent
 	 */	
 	@Override
 	public void handleEvent(GameEvent event) {
 		
 		switch (event.getType()) {
 			case StartGame:  //--------- Spiel starten gedrckt
 				startGame();
 				break;			
 			case StartSet: 	//--------- Satz starten gedrckt 
 				startSet();		
 				break;
 			case EndSet:	//--------- Satz abbrechen gedrckt oder Server hat den Satz beendet
				if(event.getArg() == "")
					endSet((byte)-1);
				else
					endSet((byte)Integer.parseInt(event.getArg()));
 				break;
 			case EndGame:	//--------- Spiel beenden gedrckt
 				endGame();
 				break;				
 			case OppMove:	//--------- ein gegnerischer Zug wurde vom Server mitgeteilt 
 				oppMove((byte) Integer.parseInt(event.getArg()));					
 				break;		
 			case LoadGame: //Ein Spiel soll geladen werden
 				loadGame(Integer.parseInt(event.getArg()));				
 				break;
 			case WinnerSet: //Der Server hat einen Gewinner gesetzt
 				if(((String)event.getArg()).charAt(0) == Constants.xRole || ((String)event.getArg()).charAt(0) == Constants.oRole){
 					model.getLatestSet().setWinner(((String)event.getArg()).charAt(0));
 				}
 				//TODO: nach Verknpfung mit FXML: in die confirmSetWinner verschieben
 				model.save();
 				properties[STATE_PROPERTY].set(Constants.STATE_GAME_RUNNING);
 				break;
 			default:
 				break;
 			}
 		}
 	
 	
 
 	//Hilfsmethoden 
 	
 	/**
 	 * einen Gegnerischen Zug in das Datenmodell einfgen
 	 * @param Spalte :byte
 	 */	
 	private void addOppMove(byte col) {
 		if(model.getRole() == Constants.xRole)
 			model.addMove(Constants.oRole, col);
 		else
 			model.addMove(Constants.xRole, col);		
 	}
 	
 	/**
 	 * Methode um ein neues Spiel zu starten
 	 * @param Spaltenanzahl :Integer, Zeilenanzahl :Integer
 	 */	
 	private void newGame(int cols, int rows){		
 		if(model != null){			
 			model.save();
 		}
 		
 		//create new model		
 		model = new Game(cols, rows, properties[ROLE_PROPERTY].get().charAt(0), 
 				properties[OPPNAME_PROPERTY].get(), 
 				properties[PATH_PROPERTY].get(), 
 				Integer.parseInt(properties[TIMEOUTSERVER_PROPERTY].get()), 
 				Integer.parseInt(properties[TIMEOUTDRAW_PROPERTY].get()));
 		model.addObserver(this);
 				
 		setTokens();		
 		
 		for(int i = 0; i < cols; i++){
 			for(int j = 0; j< rows; j++){
 				styleField[i][j].set(Constants.emptyToken);
 			}
 		}		
 		model.save();			
 	}
 	
 	/**
 	 * Methode um anhand der eigenen Rolle die Styles der Token bestimmen
 	 * TODO: in UI Controller auslagern
 	 */	
 	private void setTokens(){
 		if(model.getRole() == Constants.oRole){
 			properties[OWNTOKEN_PROPERTY].setValue(Constants.oToken);
 			properties[OPPTOKEN_PROPERTY].setValue(Constants.xToken);
 		}else{
 			properties[OWNTOKEN_PROPERTY].setValue(Constants.xToken);
 			properties[OPPTOKEN_PROPERTY].setValue(Constants.oToken);
 		}
 	}
 	
 	//---------------------Verarbeitung von Vernderungen im Datenmodell---------------------------------------------
 	/* (non-Javadoc)
 	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 	 */
 	/**
 	 * Methode um auf Vernderungen im Datenmodell zu reagieren
 	 * Als Argument wird der Name der Variable bergeben, die sich gendert hat
 	 * 
 	 * @param das Objekt das sich vernder hat :Observable, Argumente die mit bergeben werden :Object
 	 */	
 	@Override
 	public void update(Observable o, Object arg) {
 		String changed = (String)arg;
 		switch (changed) {
 		case "winner":
 			Log.getInstance().write(
 					"Controller: Winner changed empfangen, Stand: " +model.getOwnPoints()+":"+model.getOppPoints() 
 					+ "; FxThread:" + Platform.isFxApplicationThread());
 			
 			
 //			sets.get(model.getLatestSet().getID()-1).setWinner(String.valueOf(model.getLatestSet().getWinner()));
 			updateSets();
 			//Sicherstellen, dass updates von TextProperties im UI Thread stattfinden
 			Platform.runLater(new Runnable() {					
 				@Override
 				public void run() {
 					properties[OWNPOINTS_PROPERTY].setValue(String.valueOf(model.getOwnPoints()));
 					properties[OPPPOINTS_PROPERTY].setValue(String.valueOf(model.getOppPoints()));					
 				}
 			});		
 			model.save();
 			break;
 		case "status":
 			Log.getInstance().write("Controller: Status changed empfangen, FxThread:" + Platform.isFxApplicationThread());
 //			properties[STATUS_PROPERTY].set(model.getLatestSet().getStatus());
 			break;
 		case "sets":
 			Log.getInstance().write("Controller: Set changed empfangen; FxThread:" + Platform.isFxApplicationThread());
 			updateField();
 			updateSets();
 			break;
 		case "field":
 			Log.getInstance().write("Controller: Field changed empfangen; FxThread:" + Platform.isFxApplicationThread());
 			updateField();
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	//Hilfsmethoden
 	
 	/**
 	 * Field Property aktualisieren
 	 * TODO: Converter im UI Controller? Um unabhngig von UI Styles zu sein
 	 */	
 	private void updateField(){
 		Boolean[][] boolField = model.getLatestSet().getField();
 		for(int i = 0; i < Constants.gamefieldcolcount; i++){
 			for(int j = 0; j< Constants.gamefieldrowcount; j++){
 				String newStyle;
 				if(boolField[i][j] == null)
 					newStyle  = Constants.emptyToken;
 				else if(boolField[i][j])
 					newStyle = Constants.xToken;
 				else
 					newStyle = Constants.oToken;
 				
 				if(styleField[i][j].getValue() != newStyle) styleField[i][j].set(newStyle);
 			}
 		}
 	}
 	
 	/**
 	 * Tabelle der Sets neu erstellen
 	 */
 	private void updateSets() {
 		sets.clear();
 		Iterator<Set> it = model.getSets().listIterator();
 		while(it.hasNext()){
 			Set set = it.next();
 			sets.add(new SetProperty(String.valueOf(set.getID()), String.valueOf(set.getWinner())));
 		}
 	}
 	
 	//---------------- Methoden zum starten und initialisieren des Programms -------------------
 	
 	/**
 	 * 1. Main Methode zum Starten des Programms 	 *  
 	 * @param Argumente :String[]
 	 */	
 	public static void main(String[] args) {
 		launch(args);
 	}
 	
 	/**
 	 * 2. start Method von Application, wird aufgerufen, nach dem durch launch ein JavaFX Programm aufgebaut wurde.
 	 * Von Interface Application  
 	 * @param Stage von JavaFX :Stage
 	 */
 	@Override 
 	public void start (Stage mainstage) throws Exception{
 		
 		IGameView view = new MainGUI();
 		initialize(null, null);
 		view.init(mainstage, this);
 		mainstage.setHeight(550);
 		mainstage.setWidth(820);
 		mainstage.setTitle("4 Gewinnt - untitled0815");
 		mainstage.show();
 		
 		
 	}	
 	
 	/**
 	 * 3. Initialisierungs Methode die durch das laden der FXML in der Startmethode ausgelst wird, nach dem das UI Konstrukt erstellt wurde.
 	 * Von Interface Initializable
 	 * @param erstes Argument :URL, zweites Argument :ResourceBundle
 	 */
 	@Override
 	public void initialize(URL arg0, ResourceBundle arg1) {
 		//Property Initialisierung
 		styleField = new SimpleStringProperty[Constants.gamefieldcolcount][Constants.gamefieldrowcount];
 		for(int i = 0; i < Constants.gamefieldcolcount; i++){
 			for(int j = 0; j< Constants.gamefieldrowcount; j++){
 				styleField[i][j] = new SimpleStringProperty(Constants.emptyToken); 
 			}
 		}
 		
 		properties = new SimpleStringProperty[12];
 		properties[ROLE_PROPERTY] = new SimpleStringProperty();
 		properties[OWNPOINTS_PROPERTY] = new SimpleStringProperty("0");
 		properties[OPPPOINTS_PROPERTY] = new SimpleStringProperty("0");
 		properties[OPPNAME_PROPERTY] = new SimpleStringProperty();
 		properties[PATH_PROPERTY] = new SimpleStringProperty();
 		properties[TIMEOUTSERVER_PROPERTY] = new SimpleStringProperty(String.valueOf(Constants.defaultTimeoutServer));
 		properties[TIMEOUTDRAW_PROPERTY] = new SimpleStringProperty(String.valueOf(Constants.defaultTimeoutDraw));
 		properties[OPPTOKEN_PROPERTY] = new SimpleStringProperty(Constants.oToken);
 		properties[OWNTOKEN_PROPERTY] = new SimpleStringProperty(Constants.xToken);		
 		properties[WINNER_PROPERTY] = new SimpleStringProperty();
 		properties[STATE_PROPERTY] = new SimpleStringProperty(Constants.STATE_APP_RUNNING);
 			
 		sets = FXCollections.observableArrayList();		
 		savedGames = FXCollections.observableArrayList();	
 		
 		logItems = Log.getInstance().getLogEntries();
 			
 		//Communication Server
 		comServ = CommunicationServer.getInstance();
 				
 		//Dispatcher
 		EventDispatcher Dispatcher = EventDispatcher.getInstance();
 		try {			
 			Dispatcher.addListener(GameEvent.Type.StartGame.toString(), this);
 			Dispatcher.addListener(GameEvent.Type.EndGame.toString(), this);
 			Dispatcher.addListener(GameEvent.Type.EndSet.toString(), this);
 			Dispatcher.addListener(GameEvent.Type.LoadGame.toString(), this);
 			Dispatcher.addListener(GameEvent.Type.StartSet.toString(), this);
 			Dispatcher.addListener(GameEvent.Type.OppMove.toString(), this);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 	}
 
 	
 }
