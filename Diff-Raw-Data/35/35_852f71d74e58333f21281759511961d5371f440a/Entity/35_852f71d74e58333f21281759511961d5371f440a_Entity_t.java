 package roborally.model;
 
 import roborally.basics.Position;
 import roborally.model.Board;
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Raw;
 
 /**
  * Deze klasse houdt een object bij dat op een board kan staan en een positie kan hebben.
  * 
  * @invar	Indien bord of positie null is, moet de andere van de 2 ook null zijn.
  * 			|isValid()
  * 
  * @author 	Bavo Goosens (1e bachelor informatica, r0297884), Samuel Debruyn (1e bachelor informatica, r0305472)
  * 
  * @version 1.0
  */
 public class Entity {
 	
 	/**
 	 * Positie van dit object (niet noodzakelijk).
 	 */
 	private Position position = null;
 	
 	/**
 	 * Indien het object vernietigd is wordt dit true.
 	 */
 	private boolean isTerminated = false;
 	
 	/**
 	 * Het board waarop dit object staat (niet noodzakelijk).
 	 */
 	private Board board = null;
 	
 	/**
 	 * Methode die het board instelt waartoe dit object behoort.
 	 * 
 	 * @param 	board
 	 * 			Het board waarop dit object zich bevindt.
 	 * 
 	 * @post	new.board == board
 	 */
 	@Raw
 	private void setBoard(Board board){
 		this.board = board;
 	}
 	
 	/**
 	 * Methode die het board teruggeeft waarop dit object zich bevindt. Deze methode kan ook null teruggeven wat wil zeggen dat het object zich niet op een board bevindt.
 	 * 
 	 * @return	this.board
 	 * 			Het board waarop dit object zich bevindt of null als het object niet op een board staat;
 	 */
 	@Basic
 	public Board getBoard(){
 		return this.board;
 	}
 	/**
 	 * Geeft de positie van dit object terug.
 	 * 
 	 * @return	De positie van het object.
 	 * 			|this.position
 	 */
 	@Basic
 	public Position getPosition() {
 		return position;
 	}
 
 	/**
 	 * Wijzigt de positie van dit object naar de nieuwe positie.
 	 * 
 	 * @param	position
 	 * 			De nieuwe positie. Null indien deze buiten het bord is.
	 * 
	 * @throws	IllegalArgumentException
	 * 			|this.isOnBoard()
	 * 
	 * @throws	IllegalStateException 
	 * 			|this.getBoard().isValidBoardPosition(position)
 	 */
 	@Raw
	public void setPosition(Position position) throws IllegalArgumentException, IllegalStateException{
		if(!this.isOnBoard()){
			throw new IllegalStateException("Het object staat niet op een bord.");
		}else if(!this.getBoard().isValidBoardPosition(position)){
			throw new IllegalArgumentException("De gegeven positie is ongeldig voor dit bord.");
		}else{
			this.position = position;
		}
 	}
 	
 	/**
 	 * Deze methode vernietigt het object.
 	 * 
 	 * @post	Het object heeft geen positie meer.
 	 * 			|new.getPosition() == null
 	 * @post	Het object is vernietigd.
 	 * 			|new.isDestroyed()
 	 * @post	Het object staat niet meer op een bord.
 	 * 			|new.getBoard() == null
 	 */
 	public void destroy(){
 		this.isTerminated = true;
 		this.removeFromBoard();
 	}
 	
 	/**
 	 * Deze methode geeft true indien het object vernietigd is, anders false.
 	 * 
 	 * @return	|this.isTerminated
 	 */
 	@Basic
 	public boolean isDestroyed(){
 		return isTerminated;
 	}
 	
 	/**
 	 * Plaats het object op een bord met een geldige positie.
 	 * 
 	 * @param	board
 	 * 			Het bord waarop het object geplaatst moet worden.
 	 * 
 	 * @param	position
 	 * 			De plaats waar het object moet komen.
 	 * 
 	 * @post	|new.getBoard() == board
 	 * @post	|new.getPosition() == position
 	 * 			
 	 */
 	public void putOnBoard(Board board, Position position){
 		//TODO: add to board in Board
 		this.setBoard(board);
 		this.setPosition(position);
 	}
 	
 	/**
 	 * Verwijdert de entity van een bord en haalt de opgeslagen positie weg.
 	 * 
 	 * @post	|new.getBoard() == null
 	 * @post	|new.getPosition() == null
 	 */
 	public void removeFromBoard(){
 		//TODO: remove from board in Board
 		this.setBoard(null);
 		this.setPosition(null);
 	}
 	
 	/**
 	 * Kijkt na of het object op het bord staat met een geldige positie.
 	 * 
 	 * @return	(this.getPosition() != null && this.getBoard() != null)
 	 */
 	public boolean isOnBoard(){
 		return (this.getPosition() != null && this.getBoard() != null);
 	}
 	
 	/**
 	 * Kijk na of het object geldig is. Indien bord of positie null is, moet de andere van de 2 ook null zijn.
 	 * 
 	 * @return !(this.getBoard() == null ^ this.getPosition() == null)
 	 */
 	public boolean isValid(){
 		return !(getBoard() == null ^ getPosition() == null);
 	}
 }
