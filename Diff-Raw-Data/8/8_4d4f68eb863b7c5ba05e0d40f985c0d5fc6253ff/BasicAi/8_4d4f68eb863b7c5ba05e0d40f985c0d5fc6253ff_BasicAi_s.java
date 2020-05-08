 package rps.client.ai;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 import rps.client.GameListener;
 import rps.game.Game;
 import rps.game.data.Figure;
 import rps.game.data.FigureKind;
 import rps.game.data.Player;
 import rps.game.data.Move;
 
 /**
  * This class contains a very basic AI, that allows to play a game against it.
  * The main benefit is to be able to test the UI.
  */
 public class BasicAi implements GameListener {
 
 	private Player player = new Player("Basic AI");
 	private Game game;
 
 	@Override
 	public void chatMessage(Player sender, String message) throws RemoteException {
 		if (!player.equals(sender)) {
 			game.sendMessage(player, "you said: " + message);
 		}
 	}
 	
 	public Player getPlayer() {
 		return this.player;
 	}
 	
 	/**
 	 * erstellt zufällige Startaufstellung
 	 */
 	@Override
 	public void provideInitialAssignment(Game game) throws RemoteException {
 		//Liste mit allen eigenen Figuren erstellen
 		ArrayList<FigureKind> list = new ArrayList<FigureKind>();
 		
 		list.add(FigureKind.TRAP);
 		list.add(FigureKind.FLAG);
 		
 		for(int i =0; i<4; i++) {
 			list.add(FigureKind.PAPER);
 			list.add(FigureKind.ROCK);
 			list.add(FigureKind.SCISSORS);
 		}
 		
 		Collections.shuffle(list); // Liste mischen -> zufällige Anordnung
 		
 		// komplettes Feld mit eigenen Figuren drauf (oben)
		FigureKind[] initialAssignment = new FigureKind[48];
 		
 		for(int i = 0; i<list.size(); i++) {
 			initialAssignment[i+28] = list.get(i);
 		}
 		
 		game.setInitialAssignment(this.player, initialAssignment);
 		this.game = game;
 	}
 	
 	// gibt zufällig Schere,Stein oder Papier zurück
 	private FigureKind randomChoice() {
 		Random rand = new Random();
 		int randomNumber = rand.nextInt(3) + 1;
 		
 		FigureKind choice = null;
 		switch(randomNumber) {
 			case 1:
 				choice = FigureKind.SCISSORS; //Schere auswählen
 				break;
 			case 2:
 				choice = FigureKind.ROCK;// Stein
 				break;
 			case 3:
 				choice = FigureKind.PAPER; // Papier
 				break;
 		}
 		return choice;
 	}
 
 	// Startentscheidung: Wählt zufällig Schere/Stein/Papier aus
 	@Override
 	public void provideInitialChoice() throws RemoteException {
 		this.game.setInitialChoice(this.player, randomChoice());
 	}
 	
 	@Override
 	public void startGame() throws RemoteException {}
 		
 	/**
 	 *  führt von allen möglichen Zügen einen zufälligen aus.(non-Javadoc)
 	 * @see rps.client.GameListener#provideNextMove()
 	 */
 	@Override
 	public void provideNextMove() throws RemoteException {
		Move[] possibleMoves = new Move[48];  // maximal 48 Züge möglich: 12 Figuren mit je 4 Möglichkeiten
 		
 		Figure[] feld = this.game.getField();
 		int counter = 0; // Anzahl der möglichen Züge
 		
		for(int i=0; i<42; i++) {			
 			if(feld[i].belongsTo(this.player)) {
 				Figure currentFigure = feld[i];
 				
 				if(currentFigure.getKind().isMovable()) { //Figur ist weder Falle, noch Flagge. (also beweglich)
 										
 					try{
 						if(this.getLeftFieldByIndex(i).belongsTo(this.player)) { //nicht in linker Spalte und Feld links daneben gehört nicht dem Spieler selbst
 							possibleMoves[counter] = new Move(i, i-1, feld);
 							counter++;
 						}
 					} catch (IndexOutOfBoundsException e){}	
 					
 					try{
 						if(this.getRightFieldByIndex(i).belongsTo(this.player)) { //nicht in rechter Spalte und Feld rechts daneben gehört nicht dem Spieler selbst
 							possibleMoves[counter] = new Move(i, i+1, feld);
 							counter++;
 						}
 					} catch (IndexOutOfBoundsException e){}
 					
 					try{
 						if(this.getUpFieldByIndex(i).belongsTo(this.player)) {
 							possibleMoves[counter] = new Move(i, i+7, feld);
 							counter++;
 						}
 					} catch (IndexOutOfBoundsException e){}
 					
 					try{
 						if(this.getDownFieldByIndex(i).belongsTo(this.player)) {
 							possibleMoves[counter] = new Move(i, i-7, feld);
 							counter++;
 						}
 					} catch (IndexOutOfBoundsException e){}
 				}
 			}
 		}		
 		
 		Random rand = new Random();
 		int randomNumber = rand.nextInt(counter); // Zahl zwischen 0 und (counter-1)		
 		this.game.move(player, possibleMoves[randomNumber].getFrom(), possibleMoves[randomNumber].getTo());
 	}	
 	
 	@Override
 	public void figureMoved() throws RemoteException {}
 
 	@Override
 	public void figureAttacked() throws RemoteException {}
 
 	// Nach Draw: Wählt zufällig aus Schere/Stein/Papier aus
 	@Override
 	public void provideChoiceAfterFightIsDrawn() throws RemoteException {
 		this.game.setUpdatedKindAfterDraw(this.player, randomChoice());
 	}
 
 	@Override
 	public void gameIsLost() throws RemoteException {
 		this.game.sendMessage(this.player, "Herzlichen Glückwunsch, Sie haben gewonnen!");
 	}
 
 	@Override
 	public void gameIsWon() throws RemoteException {
 		this.game.sendMessage(this.player, "Sie haben verloren.");
 	}
 
 	@Override
 	public void gameIsDrawn() throws RemoteException {
 		this.game.sendMessage(this.player, "Unentschieden.");
 	}
 
 	@Override
 	public String toString() {
 		return player.getNick();
 	}
 	
 	/**
 	 * Gibt das Feld links von i zurück
 	 * @param i
 	 * @return
 	 * @throws IndexOutOfBoundsException
 	 * @throws RemoteException Feld außerhalb
 	 */
 	private Figure getLeftFieldByIndex(int i) throws IndexOutOfBoundsException, RemoteException{
 		if(i%7==0) {
 			throw new IndexOutOfBoundsException();
 		}
 		return this.game.getField()[i-1];
 	}
 	
 	/**
 	 * Gibt das Feld rechts von i zurück
 	 * @param i
 	 * @return
 	 * @throws IndexOutOfBoundsException
 	 * @throws RemoteException Feld außerhalb
 	 */
 	private Figure getRightFieldByIndex(int i) throws IndexOutOfBoundsException, RemoteException{
 		if((i+1)%7==0) {
 			throw new IndexOutOfBoundsException();
 		}
 		return this.game.getField()[i+1];
 	}
 	
 	/**
 	 * Gibt das Feld über i zurück
 	 * @param i
 	 * @return
 	 * @throws IndexOutOfBoundsException
 	 * @throws RemoteException Feld außerhalb
 	 */
 	private Figure getUpFieldByIndex(int i) throws IndexOutOfBoundsException, RemoteException{
 		if(i>=35) {
 			throw new IndexOutOfBoundsException();
 		}
 		return this.game.getField()[i+7];
 	}
 	
 	/**
 	 * Gibt das Feld unter i zurück
 	 * @param i
 	 * @return
 	 * @throws IndexOutOfBoundsException
 	 * @throws RemoteException Feld außerhalb
 	 */
 	private Figure getDownFieldByIndex(int i) throws IndexOutOfBoundsException, RemoteException{
 		if(i<7) {
 			throw new IndexOutOfBoundsException();
 		}
 		return this.game.getField()[i-7];
 	}	
 }
