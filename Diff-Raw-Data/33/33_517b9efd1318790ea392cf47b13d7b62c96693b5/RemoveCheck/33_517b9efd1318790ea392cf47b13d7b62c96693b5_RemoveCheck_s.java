 /**
  * @author Julian Kipka
  * @version = 1.0
  * 
  */
 
 package de.gruppe12.shared;
 
 import de.fhhannover.inform.hnefatafl.vorgaben.BoardContent;
 import de.gruppe12.logic.GameLog;
 
 public class RemoveCheck {
 	
 	public static Board checkForRemove(de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove2, Board board){
 		
 		Board temp = new Board();
 		board = doMove(currentMove2, board);
 		
 		temp.set(checkSurround(currentMove2,board));
 		temp = checkForEnd(currentMove2, board);
 		
 		return temp;
 		
 	}
 	
 	private static Board doMove(de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove2, Board board){
 		board.setCell(new Cell(currentMove2.getFromCell().getCol(), currentMove2.getFromCell().getRow(), BoardContent.EMPTY));
		board.setCell(new Cell(currentMove2.getToCell().getCol(), currentMove2.getToCell().getRow(), currentMove2.getToCell().getContent()));
 		return board;
 	}
 	
 	private static BoardContent[][] checkSurround(de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove2, Board board){
 		int x=currentMove2.getToCell().getCol(),y=currentMove2.getToCell().getRow();
 		BoardContent me = currentMove2.getToCell().getContent();
 		BoardContent tboard[][] = board.get();
 		
 		
 		/* Ab hier Try-Catch weil es sein k�nnte das ein Stein sich an der Seite befindet 
 		 * Es g�be sonst eine Index out of Bound Exception
 		 */
 		
 		// Links gucken und evtl entfernen
 		try{
 			if(tboard[x-1][y]==opposite(me)&&tboard[x-2][y]==me){
 				tboard[x-1][y] = BoardContent.EMPTY;
 				GameLog.logDebugEvent(opposite(me).toString() + " entfernt: " + (x - 1) + ", " + y);
 			}
 		} catch (Exception e){
 			
 		}
 		
 		// Rechts gucken und evtl entfernen
 			try{
 				if(tboard[x+1][y]==opposite(me)&&tboard[x+2][y]==me){
 					tboard[x+1][y] = BoardContent.EMPTY;
 					GameLog.logDebugEvent(opposite(me).toString() + " entfernt: " + (x + 1) + ", " + y);
 				}
 			} catch (Exception e){
 				
 			}
 		
 		// Unten gucken und evtl entfernen
 			try{
 				if(tboard[x][y+1]==opposite(me)&&tboard[x][y+2]==me){
 					tboard[x][y+1] = BoardContent.EMPTY;
 					GameLog.logDebugEvent(opposite(me).toString() + " entfernt: " + x + ", " + (y + 1));
 				}
 			} catch (Exception e){
 				
 			}
 			
 			// Unten gucken und evtl entfernen
 			try{
 				if(tboard[x][y-1]==opposite(me)&&tboard[x][y-2]==me){
 					tboard[x][y-1] = BoardContent.EMPTY;
 					GameLog.logDebugEvent(opposite(me).toString() + " entfernt: " + x + ", " + (y - 1));
 				}
 			} catch (Exception e){
 				
 			}
 		
 			return tboard;
 	}
 	
 	private static Board checkForEnd(de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove2, Board board){
 		
 		int x=currentMove2.getToCell().getCol(),y=currentMove2.getToCell().getRow();
 		BoardContent tboard[][] = board.get();
		Board tb = board;
		
 		
 		// Prüfen ob alle Fluchburgen blockiert sind
 		if (board.getCell(0, 1).getContent() == BoardContent.ATTACKER &&
 				board.getCell(1, 0).getContent() == BoardContent.ATTACKER &&
 				board.getCell(11, 0).getContent() == BoardContent.ATTACKER &&
 				board.getCell(12, 1).getContent() == BoardContent.ATTACKER &&
 				board.getCell(0, 11).getContent() == BoardContent.ATTACKER &&
 				board.getCell(1, 12).getContent() == BoardContent.ATTACKER &&
 				board.getCell(12, 11).getContent() == BoardContent.ATTACKER &&
 				board.getCell(11, 12).getContent() == BoardContent.ATTACKER
 				){
 			tb.setFinish();
 			tb.setAttackerWon();
 			GameLog.logDebugEvent("Spielende");
			return tb;
			
 		}
 		
 		
 		// K�nig zieht auf Burg beendet die Methode und setzt das Finish Flag
 		if(currentMove2.getToCell().getContent()==BoardContent.KING &&
 				((x==0&&y==0) || (x==0&&y==tboard[0].length-1) || (x==tboard.length-1&&y==0) || (x==tboard.length-1&&y==tboard[0].length-1))
 		) {
 			tb.setFinish();
 			tb.setDefenderWon();
 			GameLog.logDebugEvent("Spielende");
 			return tb;
 		}
 		
 		
 		/* Suche K�nig
 		 * JA ist ineffizient
 		 */
 		for(int i=0; i<tboard.length;i++){
 			for(int j = 0; j < tboard[i].length; j++){
 				if(tboard[i][j]==BoardContent.KING){
 					x=i;
 					y=j;
 				}
 			}
 		}
 		
 		//Ist der K�nig umzingelt wenn ja su = 4
 		int sur = 0;
 		
 		//links
 		try{
 			if(tboard[x-1][y]==BoardContent.ATTACKER) sur++;			
 		} catch (Exception e){
 			sur++;
 		}
 
 		//Rechts
 		try{
 			if(tboard[x+1][y]==BoardContent.ATTACKER) sur++;			
 		} catch (Exception e){
 			sur++;
 		}
 
 		//oben
 		try{
 			if(tboard[x][y-1]==BoardContent.ATTACKER) sur++;			
 		} catch (Exception e){
 			sur++;
 		}
 		
 		//unten
 		try{
 			if(tboard[x][y+1]==BoardContent.ATTACKER) sur++;			
 		} catch (Exception e){
 			sur++;
 		}
 		
 		if(sur==4) {
 			GameLog.logDebugEvent("Spielende");
 			tb.setFinish();
 			tb.setAttackerWon();
 		}
 		
 		return tb;		
 		
 	}
 	
 	
 	/** opposite
 	 * 
 	 * Helfer zum ermitteln des Gegenspielers
 	 * 
 	 * @param bc
 	 * @return
 	 */
 	private static BoardContent opposite(BoardContent bc){
 		if(bc==BoardContent.ATTACKER)
 			return BoardContent.DEFENDER;
 		else 
 			return BoardContent.ATTACKER;
 	}
 }
