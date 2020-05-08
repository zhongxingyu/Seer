 package core;
 /**
  * Der GameController stellt das ViewModel nach dem MVVM Entwurfsmuster dar,
  * er bereitet die Daten fr die Visualisierung auf und beinhaltet die Ablauflogik
  *  
  * @author Sascha Ulbrich 
  */
 
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 
 import model.*;
 import utilities.*;
 
 import javafx.application.Platform;
 import javafx.beans.property.SimpleBooleanProperty;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 
 
 public class GameController implements GameEventListener, Observer{
 
 	private Game model;
 	private CommunicationServer comServ;
 	private KI ki;
 	private KIWorkerThread kiwt;
 
 	//Konstanten fr Zugriff auf Property Array
 	public final int ROLE_PROPERTY = 0;
 	public final int OWNPOINTS_PROPERTY = 1;
 	public final int OPPPOINTS_PROPERTY = 2;
 	public final int OPPNAME_PROPERTY = 3;
 	public final int OWNNAME_PROPERTY = 3;
 	public final int PATH_PROPERTY = 5;
 	public final int TIMEOUTSERVER_PROPERTY = 6;
 	public final int TIMEOUTDRAW_PROPERTY = 7;
 	public final int OPPTOKEN_PROPERTY = 8;
 	public final int OWNTOKEN_PROPERTY = 9;
 	public final int STATE_PROPERTY = 10;
 	public final int WINNER_PROPERTY = 11;
 	
 	//Properties fr DataBinding	
 	private SimpleStringProperty[] properties;
 	private SimpleStringProperty[][] field;
 	private ObservableList<SetProperty> sets;
 	private ObservableList<GameProperty> savedGames;
 	private ObservableList<Log.LogEntry> logEntries;
 	private SimpleBooleanProperty isReplay;
 	private SimpleBooleanProperty isWithoutServer;
 	private SimpleBooleanProperty isDBAvailable;
 	
 	
 	//Attribute fr Replay
 	private int nextMove;
 	private int currentSet;
 	private Set[] loadedSets;
 	private Move[] loadedMoves;
 	
 	/**
 	 * Konstruktor von Game
 	 * Die Property Objekte werden erstellt, aber noch nicht initialisiert
 	 */
 	public GameController(){
 		field = new SimpleStringProperty[Constants.gamefieldcolcount][Constants.gamefieldrowcount];
 		for(int i = 0; i < Constants.gamefieldcolcount; i++){
 			for(int j = 0; j< Constants.gamefieldrowcount; j++){
 				field[i][j] = new SimpleStringProperty(); 
 			}
 		}
 		
 		properties = new SimpleStringProperty[12];
 		for(int i = 0; i < properties.length; i++){
 			properties[i] = new SimpleStringProperty();
 		}
 		
 		isReplay = new SimpleBooleanProperty();
 		isWithoutServer = new SimpleBooleanProperty();
 		isDBAvailable = new SimpleBooleanProperty();
 		
 		sets = FXCollections.observableArrayList();		
 		savedGames = FXCollections.observableArrayList();
 		logEntries =  Log.getInstance().getLogEntries();
 	}
 	
 	//------------------------------------------------------------------------ API Methoden fr UI-Controller -----------------------------------------	
 	/**
 	 * Methode um ein Spiel zu starten	  
 	 */
 	public void startGame(){
 		Log.getInstance().write("Controller: starte Spiel, FxThread:" + Platform.isFxApplicationThread());
 		newGame(Constants.gamefieldcolcount, Constants.gamefieldrowcount);	
 		
 		//Communication Server nutzen?
 		if(!isWithoutServer.get() && !isReplay.get()){
 			comServ = CommunicationServer.getInstance();
 		}
 		Log.getInstance().write("Controller: Spiel gestartet, FxThread:" + Platform.isFxApplicationThread());
 		properties[STATE_PROPERTY].set(Constants.STATE_GAME_RUNNING);
 	}
 	
 	/**
 	 * Methode um einen neuen Satz zu starten	  
 	 */
 	public void startSet(){
 		Log.getInstance().write("Controller: starte Satz, FxThread:" + Platform.isFxApplicationThread());
 		ki = new KI(model);
 		
 		model.newSet().setStatus(Constants.STATE_SET_RUNNING);
 		
 		//ComServer starten
 		if(!isWithoutServer.get() && !isReplay.get()){
 			comServ.enableReading(model.getTimeoutServer(), model.getPath(), model.getRole(),true);
 		}
 		properties[STATE_PROPERTY].set(Constants.STATE_SET_RUNNING);		
 		
 		//X fngt bei manuellem Spiel an -> Wenn KI = x -> oppMove mit -1 auslsen (erster Zug ohne vorherigen Gegnerzug)
 		if(isWithoutServer.get() && model.getRole() == Constants.xRole){
 			oppMove((byte)-1);
 		}
 	}
 	
 	/**
 	 * Methode um ein Satz zu beenden
 	 * @param oppMove der letzte Zug des Gegners, falls es keinen letzten Zug gibt: -1  
 	 */	
 	public void endSet(byte oppMove){
 		Log.getInstance().write("Controller: beende Satz, FxThread:" + Platform.isFxApplicationThread());
 		if(!isWithoutServer.get()  && !isReplay.get()){
 			comServ.disableReading();
 		}
 		if (oppMove > -1){
 			addOppMove(oppMove);					
 		}
 		//TODO: TEST
 //		handleEvent(new GameEvent(GameEvent.Type.WinDetected, "1,1;2,2;3,3;4,4;"));
 		
 		Platform.runLater(new Runnable() {			
 			@Override
 			public void run() {
 				properties[STATE_PROPERTY].set(Constants.STATE_SET_ENDED);
 				model.getLatestSet().setStatus(Constants.STATE_SET_ENDED);
 			}
 		});
 	}
 	
 	/**
 	 * Methode um das aktuelle Spiel zu beenden	  
 	 */	
 	public void endGame(){
 		Log.getInstance().write("Controller: beende Spiel, FxThread:" + Platform.isFxApplicationThread());
 		reset();
 		properties[STATE_PROPERTY].set(Constants.STATE_APP_RUNNING);
 	}
 	
 	/**
 	 * Methode um ein Spiel zu laden
 	 * @param gameID ID von Game :Integer
 	 */	
 	public void loadGame(int gameID){
 		Log.getInstance().write("Controller: Spiel wird geladen, FxThread:" + Platform.isFxApplicationThread());
 		isReplay.set(true);
 		processGameLoad(gameID);
 	}
 	
 	/**
 	 * Methode um von dem geladenen Spiel den nchsten Zug zu laden	
 	 */	
 	public void loadNextMove(){
 		if(isReplay.get()){
 			processNextMove();
 		}
 	}
 	
 	/**
 	 * Methode um von dem geladenen Spiel den letzten Zug wieder zu entfernen 
 	 */	
 	public void removeLastMove(){
 		if(isReplay.get()){
 			processRemoveMove();
 		}
 	}
 	
 
 	/**
 	 * Methode um einen gegnerischen Zug hinzuzufgen -> Berechnung und Ausfhrung eines neuen Zuges.
 	 * Sollte nur fr das manuelle Spielen verwendet werden, ansonsten ber Event starten.
 	 * @param col Spalte in die der Gegner gesetzt hat
 	 */	
 	public void oppMove(byte col){
 		Log.getInstance().write("Controller: gegnerischen Zug empfangen, FxThread:" + Platform.isFxApplicationThread());
 		if(model.getLatestSet() != null && model.getLatestSet().getStatus() == Constants.STATE_SET_RUNNING){
 			if (col > -1){
 				addOppMove(col);					
 			}
 			//KI Workerthread starten
 		    if(kiwt != null && kiwt.isAlive()){
 		    	Log.getInstance().write("Controller: Neuer oppMove obwohl KI Thread noch luft!");
 		    }else{
 				kiwt = new KIWorkerThread(col);
 				kiwt.start();
 				Log.getInstance().write("Controller: KI Workerthread gestartet");
 		    }
 		}
 	}
 	
 	/**
 	 * Methode um vom UI aus den Gewinner zu besttigen und somit den Satz abzuschlieen und zu speichern
 	 */	
 	public void confirmSetWinner(){
 		Log.getInstance().write("Controller: Gewinner besttigt, FxThread:" + Platform.isFxApplicationThread());		
 		properties[STATE_PROPERTY].set(Constants.STATE_GAME_RUNNING);
 		model.save();
 	}
 	
 	/**
 	 * Methode um vom UI aus den aktuellen Satz zu verwerfen
 	 */	
 	public void discardSet(){
 		Log.getInstance().write("Controller: Satz verworfen, FxThread:" + Platform.isFxApplicationThread());
 		model.discardLatestSet();
 		properties[STATE_PROPERTY].set(Constants.STATE_GAME_RUNNING);
 	}
 	
 	//------ Getter fr Properties 	
 	/**
 	 * @return Properties fr DataBinding mit UI
 	 */
 	public SimpleStringProperty[] properties() {
 		return properties;
 	}
 	
 	/**
 	 * @return das Spielfeld (je Feld die Konstante xRole|oRole|noRole) 
 	 */
 	public SimpleStringProperty[][] field() {
 		return field;
 	}	
 	
 	/**
 	 * @return Liste der gespielte Stze 
 	 */
 	public ObservableList<SetProperty> sets() {
 		return sets;
 	}
 	
 	/**
 	 * @return Liste der gespeicherten Spiele
 	 */
 	public ObservableList<GameProperty> savedGames() {
 		return savedGames;
 	}
 	
 	/**
 	 * @return Liste der Logeintrge
 	 */
 	public ObservableList<Log.LogEntry> logEntries() {
 		return logEntries;
 	}
 	
 	/**
 	 * @return Boolean ob es gerade ein Replay ist
 	 */
 	public SimpleBooleanProperty isReplay() {
 		return isReplay;
 	}
 	
 	/**
 	 * @return Boolean ob mit Server gespielt wird
 	 */
 	public SimpleBooleanProperty isWithoutServer() {
 		return isWithoutServer;
 	}
 	
 	/**
 	 * @return Boolean ob Datenbank vefrgbar ist
 	 */
 	public SimpleBooleanProperty isDBAvailable() {
 		return isDBAvailable;
 	}
 	
 	//---------------Hilfsmethoden
 	
 	// Methode um ein Spiel zu laden
 	private void processGameLoad(int gameID) {
 		//game laden
 		model = DBConnection.getInstance().loadGame(gameID);
 		model.addObserver(this);
 		loadedSets = DBConnection.getInstance().loadAllSets(model.getID());
 		loadedMoves = new Move[0];
 		currentSet = -1;
 		Platform.runLater(new Runnable() {	
 			@Override
 			public void run() {
 				properties[ROLE_PROPERTY].set(String.valueOf(model.getRole()));
 				properties[OWNPOINTS_PROPERTY].set(String.valueOf(model.getOwnPoints()));
 				properties[OPPPOINTS_PROPERTY].set(String.valueOf(model.getOppPoints()));
 				properties[OPPNAME_PROPERTY].set(model.getOppName());
 				properties[OWNNAME_PROPERTY].set(model.getOwnName());
 				properties[PATH_PROPERTY].set(model.getPath());
 				properties[TIMEOUTSERVER_PROPERTY].set(String.valueOf(model.getTimeoutServer()));
 				properties[TIMEOUTDRAW_PROPERTY].set(String.valueOf(model.getTimeoutDraw()));
 				setTokens();				
 			}
 		});	
 		//Falls kein Replay -> letzten Satz anzeigen  (zum weiterspielen)
 		if(loadedSets != null && !isReplay.get()){
 			model.addSet(loadedSets[loadedSets.length-1]);
 			loadedMoves = DBConnection.getInstance().loadAllMoves(model.getID(), model.getLatestSet().getID());
 			for(Move move: loadedMoves){
 				model.addMove(move);
 			}
 		}
 		
 		//Falls Replay, aber kein Set vorhanden -> Replay beenden 
 		if(loadedSets == null && isReplay.get()) 
 		{
 			isReplay.set(false);				
 		}
 		properties[STATE_PROPERTY].set(Constants.STATE_GAME_RUNNING);
 	}
 	
 	//Methode um den nchsten Move zu laden
 	private void processNextMove() {
 		if(loadedMoves.length <= nextMove){
 			if(currentSet == loadedSets.length -1){
 				//letzter Satz -> Replay beenden			
 				//properties[STATE_PROPERTY].set(Constants.STATE_APP_RUNNING);	
 				isReplay.set(false);						
 				properties[STATE_PROPERTY].set(Constants.STATE_GAME_RUNNING);
 			}else{
 				//neues Set laden
 				properties[STATE_PROPERTY].set(Constants.STATE_SET_RUNNING);
 				currentSet++;
 				nextMove = 0;
 				model.addSet(loadedSets[currentSet]);	
 				loadedMoves = DBConnection.getInstance().loadAllMoves(model.getID(), model.getLatestSet().getID());
 				if(loadedMoves == null) loadedMoves = new Move[0];
 				updateField();
 				updateSets();
 			}		
 		}else{	
 			//Spielzug laden
 			model.addMove(loadedMoves[nextMove]);
 			nextMove++;
 		}	
 	}
 	
 	//Methode um einen Zug zu entfernen
 	private void processRemoveMove() {
 		if(nextMove > 0){	
 			//Spielzug entfernen
 			model.removeMove(loadedMoves[nextMove-1]);
 			nextMove--;
 		}		
 	}
 	
 	// --------------------------------------------------------------------------- Behandlung von GameEvents (vom ComServer) --------------------------------
 	/* (non-Javadoc)
 	 * @see GameEventListener#handleEvent(GameEvent)
 	 */	
 	/**
 	 * Methode um auf GameEvents zu reagieren
 	 * Umsetzung des Interfaces GameEventListener
 	 * @param event das geworfene GameEvent
 	 */	
 	@Override
 	public void handleEvent(GameEvent event) {
 		
 		switch (event.getType()) {
 			case EndSet:	//--------- Server hat den Satz beendet
 				Log.getInstance().write("Controller: EndSet Event empfange");
 				if(event.getArg() == "")
 					endSet((byte)-1);
 				else
 					endSet((byte)Integer.parseInt(event.getArg()));
 				break;
 			case OppMove:	//--------- ein gegnerischer Zug wurde vom Server mitgeteilt 
 				oppMove((byte) Integer.parseInt(event.getArg()));					
 				break;		
 			case WinnerSet: //Der Server hat einen Gewinner gesetzt
 				Log.getInstance().write("Controller: WinnerSet empfangen, FxThread:" + Platform.isFxApplicationThread());
 				if(((String)event.getArg()).charAt(0) == Constants.xRole || ((String)event.getArg()).charAt(0) == Constants.oRole){
 					Log.getInstance().write("Controller: WinnerSet gltig, FxThread:" + Platform.isFxApplicationThread());
 					model.getLatestSet().setWinner(((String)event.getArg()).charAt(0));
 				}
 				break;
 			case WinDetected: //Die KI hat 4 Tokens entdeckt -> Event.getArg() = "t1x,t1y;t2x,t2y;t3x,t3y;t4x,t4y;"
 				Log.getInstance().write("Controller: WinDetected empfangen, FxThread:" + Platform.isFxApplicationThread());
 				//Parse String als byte[][]
 				byte[][] data = new byte[4][2];				
 				String[] tokens = event.getArg().split(";");
 				for(int i=0; i < 4; i++){
 					data[i][0] =  (byte)Integer.parseInt(tokens[i].split(",")[0]);
 					data[i][1] = (byte)Integer.parseInt(tokens[i].split(",")[1]);
 				}
 				//Gewinner Tokens markieren
 				markWinTokens(data);
 				
 				//Satz beenden, falls es ein manuelles Spiel ist
 				if(isWithoutServer.get()  && !isReplay.get()){
 					endSet((byte)-1);
 				}
 				break;
 			default:
 				break;
 			}
 		}	
 
 	//-------------Hilfsmethoden 
 	
 	// Anhand der eigenen Rolle bestimme welche Rolle der Gegner hat und Zug entsprechend einfgen
 	private void addOppMove(byte col) {
 		if(model.getRole() == Constants.xRole)
 			model.addMove(new Move(Constants.oRole, col));
 		else
 			model.addMove(new Move(Constants.xRole, col));		
 	}
 	
 	//neues Spiel mit einer bestimmten Spalten- und Zeilenanzahl
 	private void newGame(int cols, int rows){		
 		//create new model		
 		model = new Game(cols, rows, properties[ROLE_PROPERTY].get().charAt(0), 
 				properties[OPPNAME_PROPERTY].get(), 
 				properties[OWNNAME_PROPERTY].get(),
 				properties[PATH_PROPERTY].get(), 
 				Integer.parseInt(properties[TIMEOUTSERVER_PROPERTY].get()), 
 				Integer.parseInt(properties[TIMEOUTDRAW_PROPERTY].get()));
 		model.addObserver(this);
 				
 		setTokens();		
 		
 		for(int i = 0; i < cols; i++){
 			for(int j = 0; j< rows; j++){
 				field[i][j].set(String.valueOf(Constants.noRole));
 			}
 		}
 		model.save();			
 	}
 	
 	//Methode um anhand der eigenen Rolle die Styles der Token bestimmen
 	private void setTokens(){
 		if(model.getRole() == Constants.oRole){
 			properties[OWNTOKEN_PROPERTY].setValue(String.valueOf(Constants.oRole));
 			properties[OPPTOKEN_PROPERTY].setValue(String.valueOf(Constants.xRole));
 		}else{
 			properties[OWNTOKEN_PROPERTY].setValue(String.valueOf(Constants.xRole));
 			properties[OPPTOKEN_PROPERTY].setValue(String.valueOf(Constants.oRole));
 		}
 	}
 	
 	//Methode um die Tokens, die zum Gewinn gefhrt haben zu markieren
 	private void markWinTokens(byte[][] data){
 		for(byte[] token: data){
 			field[token[0]][token[1]].set(
 					field[token[0]][token[1]].get().charAt(0) + 
 					String.valueOf(Constants.winMarker));
 		}
 	}
 	
 	//-------------------------------------------------------------------------Verarbeitung von Vernderungen im Datenmodell---------------------------------------------
 	/* (non-Javadoc)
 	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 	 */
 	/**
 	 * Methode um auf Vernderungen im Datenmodell zu reagieren
 	 * Umsetzung des Interfaces Observer
 	 * 
 	 * @param o das Objekt das sich vernder hat
 	 * @param arg Argumente die mit bergeben werden: der Name der Variable, die sich gendert hat
 	 */	
 	@Override
 	public void update(Observable o, Object arg) {
 		String changed = (String)arg;
 		switch (changed) {
 		case "winner":
 			Platform.runLater(new Runnable() {				
 				@Override
 				public void run() {
 					Log.getInstance().write(
 							"Controller: Winner changed empfangen, Stand: " +model.getOwnPoints()+":"+model.getOppPoints() 
 							+ "; FxThread:" + Platform.isFxApplicationThread());
 					
 					updateSets();
 
 					properties[WINNER_PROPERTY].setValue(String.valueOf(model.getLatestSet().getWinner()));
 					properties[OWNPOINTS_PROPERTY].setValue(String.valueOf(model.getOwnPoints()));
 					properties[OPPPOINTS_PROPERTY].setValue(String.valueOf(model.getOppPoints()));					
 				}
 			});								
 			break;
 		case "sets":
 			Log.getInstance().write("Controller: Set changed empfangen; FxThread:" + Platform.isFxApplicationThread());
 			updateField();
 			updateSets();
 			if(model.getLatestSet() != null) properties[WINNER_PROPERTY].setValue(String.valueOf(model.getLatestSet().getWinner()));
 			break;
 		case "field":
 			Log.getInstance().write("Controller: Field changed empfangen; FxThread:" + Platform.isFxApplicationThread());
 			updateField();
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	//---------------Hilfsmethoden
 	
 	//Field Property aktualisieren
 	private void updateField(){
 		if(model.getLatestSet() != null){
 			Platform.runLater(new Runnable() {			
 				@Override
 				public void run() {
 					Boolean[][] boolField = model.getLatestSet().getField();
 					for(int i = 0; i < Constants.gamefieldcolcount; i++){
 						for(int j = 0; j< Constants.gamefieldrowcount; j++){
 							String newStyle;
 							if(boolField[i][j] == null)
 								newStyle  = String.valueOf(Constants.noRole);
 							else if(boolField[i][j])
 								newStyle = String.valueOf(Constants.xRole);
 							else
 								newStyle = String.valueOf(Constants.oRole);
 							
 							if(field[i][j].getValue() != newStyle) field[i][j].set(newStyle);
 						}
 					}				
 				}
 			});
 		}
 	}
 			
 	//Tabelle der Sets neu erstellen
 	private void updateSets() {
 		sets.clear();
 		Iterator<Set> it = model.getSets().listIterator();
 		while(it.hasNext()){
 			Set set = it.next();
 			sets.add(new SetProperty(String.valueOf(set.getID()), String.valueOf(set.getWinner())));
 		}
 	}
 	
 	//----------------------------------------------------------------------------------- Initialisieren der Properties ----------------------------------
 	
 	/**
 	 * Initialisierungs Methode um initiale Werte zu setzen
 	 */
 	public void initialize() {
 		//Property Initialisierung
 		properties[ROLE_PROPERTY].set(String.valueOf(Constants.defaultRole));
 		properties[OWNPOINTS_PROPERTY].set("0");
 		properties[OPPPOINTS_PROPERTY].set("0");
 		properties[TIMEOUTSERVER_PROPERTY].set(String.valueOf(Constants.defaultTimeoutServer));
 		properties[TIMEOUTDRAW_PROPERTY].set(String.valueOf(Constants.defaultTimeoutDraw));
 		properties[OPPTOKEN_PROPERTY].set(String.valueOf(Constants.oRole));
 		properties[OWNTOKEN_PROPERTY].set(String.valueOf(Constants.xRole));		
 		properties[STATE_PROPERTY].set(Constants.STATE_APP_RUNNING);
 		properties[WINNER_PROPERTY].set(String.valueOf(Constants.noRole));
 		properties[WINNER_PROPERTY].addListener(new ChangeListener<String>() {
 			@Override
 			public void changed(ObservableValue<? extends String> arg0,
 					String arg1, String arg2) {model.getLatestSet().setWinner(properties[WINNER_PROPERTY].get().charAt(0));}
 		});
 		
 				
 		isReplay.set(false);
 		isWithoutServer.set(false);
 		isDBAvailable.set(!DBConnection.getInstance().isOfflineMode());
 									
 		//Dispatcher
 		EventDispatcher Dispatcher = EventDispatcher.getInstance();
 		try {			
 			Dispatcher.addListener(this);
 		
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		reset();
 	}
 	
 	//----------Hilfsmethoden
 	
 	//Methode um die initialen Werte zu setzen, die auch nach einem Spiel, wieder gesetzt werden mssen
 	private void reset(){
 		if(model != null) model = null;
 		
 		//Feld leeren
 		for(int i = 0; i < Constants.gamefieldcolcount; i++){
 			for(int j = 0; j< Constants.gamefieldrowcount; j++){
 				field[i][j].set(String.valueOf(Constants.noRole)); 
 			}
 		}
 		
 		//Liste der Stze zurck setzen
 		sets.clear();
 		sets.add(new SetProperty("keine ", "Stze"));		
 		
 		//Liste der gespeicherten Spiele laden
 		if(isDBAvailable.get()) 
 			loadSavedGames();
 	}
 	
 	private void loadSavedGames(){
 		savedGames.clear();
 		Game[] games = DBConnection.getInstance().loadAllGames();
		if(games != null){
			for(Game game: games){
				savedGames.add(new GameProperty(String.valueOf(game.getID()), game.getOppName()));
			}
 		}
 	}
 	
 	
 	
 	private class KIWorkerThread extends Thread{
 		
 		private byte oppMove;
 		
 		public KIWorkerThread(byte oppMove){
 			this.oppMove = oppMove;
 		}
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			byte newCol = ki.calculateNextMove(oppMove);			
 			//Zug auf Server schreiben und Server wieder berwachen
 			if(!isWithoutServer.get()  && !isReplay.get()){
 				comServ.writeMove(newCol, model.getPath(), model.getRole());
 				comServ.enableReading(model.getTimeoutServer(), model.getPath(), model.getRole(), false);
 			}
 			model.addMove(new Move(model.getRole(), newCol));
 		}		
 	}
 }
