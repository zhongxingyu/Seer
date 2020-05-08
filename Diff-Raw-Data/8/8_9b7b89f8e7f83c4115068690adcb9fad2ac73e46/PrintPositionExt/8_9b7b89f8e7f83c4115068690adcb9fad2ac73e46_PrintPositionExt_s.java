 package chess.uci.controlExtension;
 
 import chess.state4.State4;
 import chess.uci.Position;
 import chess.uci.UCIEngine;
 
 /** extension for printing a position*/
 public class PrintPositionExt implements ControlExtension {
 
 	@Override
 	public void execute(String command, Position pos, UCIEngine engine) {
 		if(pos == null){
 			System.out.println("no state information");
 		} else{
 			System.out.println("side to move: "+(pos.sideToMove == 0? "w": "b"));
 
 			State4 s = pos.s;
 			String castleInfo = "";
			if(s.kingMoved[0] && s.rookMoved[0][0]) castleInfo += "q";
			if(s.kingMoved[0] && s.rookMoved[0][1]) castleInfo += "k";
			if(s.kingMoved[1] && s.rookMoved[1][0]) castleInfo += "Q";
			if(s.kingMoved[1] && s.rookMoved[1][1]) castleInfo += "K";
 			System.out.println("castle rights: "+castleInfo);
 
 			System.out.println("\n"+pos.s);
 		}
 	}
 }
