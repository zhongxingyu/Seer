 
 package cha.domain;
 
 import cha.event.Event;
 import cha.event.EventBus;
 
 /**
  * @author Malla
  *
  */
 public class Turn {
 
 	private final Piece piece;
 	private TurnType tt = null;
 	private Category c;
 	private int steps;
 	private int tempBet;
 	private String tempOpp;
 	private boolean isTurnOver;
 
 
 	public Turn(Piece p){
 		isTurnOver=false;
 		this.piece = p;
 		tempBet=0;
 		tempOpp=null;
 		c=Board.getInstance().getTile(piece.getPosition()).getCategory();
 		steps=0;
 	}
 
 	public Piece getPiece(){
 		return piece;
 	}
 
 	public void startMission(){
 		tt.startMission(c);
 	}
 
 	public void determinType(){
 		if(Board.getInstance().getTile(piece.getPosition())
 				.isChallenge()){
 			EventBus.getInstance().publish(Event.IsChallenge, null, null);	
 	} else 
 			EventBus.getInstance().publish(Event.MakeBet, null, null);
 	}
 
 	public void setTurnType(){
 		if(tempBet==0){
 			if(tempOpp!=null){
 				Piece oppPiece=null;
 				for (int i = 0; i < Board.getInstance().getNumberOfPieces(); i++) {
 					if (tempOpp.contains(Board.getInstance()
 							.getTeamName(i))){
 						oppPiece = Board.getInstance().getPiece(i);
 					}
 				}
 				tt=new Challenge(oppPiece);
 			}
 		}
 		else{
 			steps=tempBet;
 			tt=new NormalTurn(steps);
 		}
 	}
 
 	public void setTempBet(int tb){
 		tempBet=tb;
 		EventBus.getInstance().publish(Event.UpdateBet, null, null);	//Publicerar att Tile clickats
 	}
 	public int getBet(){
 		return tempBet;
 	}
 	public int getSteps(){
 		return steps;
 	}
 	public void setTempOpp(String s){
 		tempOpp=s;
 	}
 	public String getTempOpp(){
 		return tempOpp;
 	}
 
 	public TurnType getTurnType(){
 		return tt;
 	}
<<<<<<< HEAD

	public void setSteps(int steps){
		this.steps=steps;
	}

=======
 	
 	/**
 	 * @param b to be true if player has successfully managed his NormalTurn
 	 * or has won the Challenge.
 	 */
>>>>>>> 24b943cf6691a355a0970e7f7e6b41a2bfe75311
 	public void finishTurn(boolean b){
 		movePiece(b);
 		isTurnOver=true;
 	}
 	
 	public boolean isTurnOver(){
 		return isTurnOver;
 	}
 	
 
 	/**
 	 * @param b if true, player moves forward
 	 * if false, player move backwards
 	 */
 	private void movePiece(boolean b){
 		if(b && tt instanceof Challenge){
 			piece.movePieceForward(((Challenge)tt).getChaScore());
 		}
 		else if (b){
 			piece.movePieceForward(steps);
 		}
 		else {
 			piece.movePieceBackward();
 		}	
 	}
 }
 
 
 
