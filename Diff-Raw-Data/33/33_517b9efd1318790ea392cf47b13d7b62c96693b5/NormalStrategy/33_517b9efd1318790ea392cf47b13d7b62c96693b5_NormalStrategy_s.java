 package de.gruppe12.ki;
 
 import de.fhhannover.inform.hnefatafl.vorgaben.BoardContent;
 import de.fhhannover.inform.hnefatafl.vorgaben.MoveStrategy;
 import de.gruppe12.shared.*;
 
 
 /**
 * Die Klasse NormalStrategy ist die normale Spielstrategie <p>
 * Copyright: (c) 2011 <p>
 * Company: Gruppe 12 <p>
 * @author Markus
 * @version 0.6.0 03.12.2011
 * �nderungen: 03.12. Fehler beseitigt, javadoc erweitert
 */
 
 public class NormalStrategy implements MoveStrategy {
 	private int grpNr;
 	private String name;
 	//private Node<Move> verlauf;
 	private Board b;
 	
 	public NormalStrategy(){
 		grpNr = 12;
 		name = "normal"; 
 		b = new Board();
 	}
 	
 	@Override
 	public int getGroupNr() {
 		return grpNr;
 	}
 
 	@Override
 	public String getStrategyName() {
 		return name;
 	}	
 	
 	/**
 	 * 
 	 * GenerateMoves()
 	 * erzeutgt ein Array mit den n�chsten m�glichen Schritten
 	 * jeder Spielfigur 
 	 * 
 	 * @return ein Array mit allen m�glichen Moves
 	 */
 	private Move[] GenerateMoves(BoardContent type) {
 		Move[] mList = new Move[500]; 
 		Cell[] cList = new Cell[24];
 		int m1=0;
 		int c1=0;
 		
 		//prüft wo Steine sind
 		for(int i=0;i<13;i++){
 			for(int j=0;j<13;j++){
 				//speichert nur wenn Typ gleich ist
 				if(b.getCell(i,j).getContent() == type){
 					cList[c1]=new Cell(i,j, type);
 					c1++;
 				}
 			}
 		}
 		for(int i=0;i<c1;i++){
 			int r=12-cList[i].getCol();
 			int l=12-cList[i].getRow();
 			for(int j=0;j<r;j++){
 				mList[m1]=new Move(cList[i],new Cell(cList[i].getCol()+j,cList[i].getRow(), cList[i].getContent()));
 				m1++;
 			}
 			for(int h=0;h<l;h++){
 				mList[m1]=new Move(cList[i],new Cell(cList[i].getCol(),cList[i].getRow()+h, cList[i].getContent()));
 				m1++;		
 			}
 		}		
 		return mList;
 	}
 
 	/**
 	 * calculateValue
 	 * Bewertet den �bergebenen Move.
 	 * 
 	 * @return Bewertung des Moves
 	 */
 	   private int calculateValue(Move m, boolean isDefTurn) {
 		   	 if (m == null){
 		   		 return -1;
 		   	 }
 		   
 		      int value=0;
 		      //Figur die Bewegt wird
 		      BoardContent p = m.getFromCell().getContent();
 		      //Felder in umgebung
 			  BoardContent c1 = b.getCell(m.getToCell().getCol()+1,m.getToCell().getRow()).getContent();
 			  BoardContent c2 = b.getCell(m.getToCell().getCol()+2,m.getToCell().getRow()).getContent();
 			  BoardContent c3 = b.getCell(m.getToCell().getCol(),m.getToCell().getRow()+1).getContent();
 			  BoardContent c4 = b.getCell(m.getToCell().getCol(),m.getToCell().getRow()+2).getContent();
 		      
 			  
 			  //testet, wenn Gegner und kein leeres Feld steigt Value
 		      if((c1 != p)&&(c1!=BoardContent.EMPTY)){
 		    	  value++;
 		      }
 		      if((c2 != p)&&(c1!=BoardContent.EMPTY)){
 		    	  value++;
 		      }
 		      if((c3 != p)&&(c1!=BoardContent.EMPTY)){
 		    	  value++;
 		      }
 		      if((c4 != p)&&(c1!=BoardContent.EMPTY)){
 		    	  value++;
		      }
		      //checkt ob Move zul�ssig
		      if(!MoveCheck.check(m,b,isDefTurn)){
		    	  return -1;
		      } 		      
 		      return value;
 		   }
 	
 	public String toString(){
 		return name+" Strategy, Gruppe"+grpNr;	
 	}
 	
 	private Board doMove(de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove2, Board board){
 		if (currentMove2 != null){
			board.setCell(new Cell(currentMove2.getFromCell().getCol(), currentMove2.getFromCell().getRow(), BoardContent.EMPTY));
			board.setCell(new Cell(currentMove2.getToCell().getCol(), currentMove2.getToCell().getRow(), currentMove2.getToCell().getContent()));
			board = RemoveCheck.checkForRemove(currentMove2, board);
 		}		
 		return board;		
 	}
 
 	@Override
 	public de.fhhannover.inform.hnefatafl.vorgaben.Move calculateAttackerMove(
 			de.fhhannover.inform.hnefatafl.vorgaben.Move arg0, int arg1) {		
 		// Move übernehmen	
 				b = doMove(arg0,b);
 				
 		//Speichern in Verlaufbaum, muss noch durchdacht werden
 				//verlauf.setLeft(new Node<Move>(lastMove));
 				
 				//Abbruchbedingung Zeit muss eingef�gt werden
 				//if(time>= thinktimeInSeconds){
 				//zeit abgelaufen -> Spiel vorbei ?
 				//} else {
 				//Hauptcode zur Berechnung n�chster Schritt
 			    Move[] moves = GenerateMoves(BoardContent.ATTACKER);
 			    Move move;
 			    Move best_move=moves[0];
 			    Move last_bm=best_move;
 			    for(int i=1; i<moves.length; i++) {
 			       move = moves[i];
 			       if (calculateValue(move, false) > calculateValue(best_move, false)) {
 			          last_bm = best_move;
 			    	  best_move = move;
 			       }
 			    }
 			    if(best_move!=arg0){
 			    	return best_move;
 			    } else { 
 			    	return last_bm;
 			    }
 				//}	
 	}
 
 	@Override
 	public de.fhhannover.inform.hnefatafl.vorgaben.Move calculateDefenderMove(
 			de.fhhannover.inform.hnefatafl.vorgaben.Move arg0, int arg1) {
 		// Move übernehmen	
 		b = doMove(arg0,b);
 		
 		//Speichern in Verlaufbaum, muss noch durchdacht werden
 				//verlauf.setRight(new Node<Move>(lastMove));
 				
 				//Abbruchbedingung Zeit muss eingef�gt werden
 				//if(time>= thinktimeInSeconds){
 				//zeit abgelaufen -> Spiel vorbei ?
 				//} else {
 				//Hauptcode zur Berechnung n�chster Schritt
 			    Move[] moves = GenerateMoves(BoardContent.DEFENDER);
 			    Move move;
 			    Move best_move=moves[0];
 			    Move last_bm=best_move;
 			    for(int i=1; i<moves.length; i++) {
 			       move = moves[i];
 			       if (calculateValue(move, true) > calculateValue(best_move, true)) {
 			          last_bm = best_move;
 			    	   best_move = move;
 			       }
 			    }
 			    if(best_move!=arg0){
 			    	return best_move;
 			    } else { 
 			    	return last_bm;
 			    }
 				//}	
 	}
 
 //	//Methoden m�ssen importiert werden, vlt in vorgaben l�schen ?
 //	@Override
 //	public de.fhhannover.inform.hnefatafl.vorgaben.Move calculateDefenderMove(
 //			de.fhhannover.inform.hnefatafl.vorgaben.Move lastMove,
 //			int thinktimeInSeconds) {
 //		// TODO Auto-generated method stub
 //		return null;
 //	}
 //
 //	@Override
 //	public de.fhhannover.inform.hnefatafl.vorgaben.Move calculateAttackerMove(
 //			de.fhhannover.inform.hnefatafl.vorgaben.Move lastMove,
 //			int thinktimeInSeconds) {
 //		// TODO Auto-generated method stub
 //		return null;
 //	}
 }
