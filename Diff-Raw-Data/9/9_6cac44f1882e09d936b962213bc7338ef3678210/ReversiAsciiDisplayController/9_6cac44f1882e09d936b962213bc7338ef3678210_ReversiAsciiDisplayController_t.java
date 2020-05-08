 package reversi.server.display;
 
 import java.util.Map;
 
 import reversi.models.ReversiBoard;
 import reversi.models.ReversiEntity;
 import reversi.models.ReversiPlayer;
 import base.models.BoardPiece;
 import base.models.Position;
 
 public class ReversiAsciiDisplayController {
 
 	public static final String whiteReversiPiece = "O";
 	public static final String blackReversiPiece = "@";
 	public static final String boardPipe = "|";
 	public static final String boardEmptySpace = "_";
 	public static final String commentLine = ";";
 	
 	private static final String[] columnAlpha = { "a", "b", "c", "d", "e", "f",
 			"g", "h" };
 
 	public static String drawBoard(ReversiBoard board) {
		Map<Position, BoardPiece<ReversiEntity>> boardPieces = board.getBoardElements();
 
 		StringBuilder builder = new StringBuilder();
 
 		builder.append(commentLine);
 		builder.append("  ");
 		
 		for(int c = 0; c < 8; c += 1)
 		{
 			builder.append(boardEmptySpace);
 			builder.append(" ");
 		}
 		
		builder.append("\r\n");
 		
 		for (int r = 1; r <= 8; r += 1) {
 			builder.append(commentLine);
 			builder.append(r);
 
 			for (int c = 0; c < 8; c += 1) {
 
 				builder.append(boardPipe);
 
 				Position position = new Position(c, r);
 
 				BoardPiece<ReversiEntity> boardPiece = boardPieces.get(position);
 
 				String representation = drawElement(boardPiece);
 
 				builder.append(representation);
 			}
 
 			builder.append(boardPipe);
			builder.append("\r\n");
 		}
 
 		builder.append(commentLine);
 		builder.append("  ");
 		
 		for(int c = 0; c < 8; c += 1)
 		{
 			String currentColumn = columnAlpha[c];
 			builder.append(currentColumn);
 			builder.append(" ");
 		}
 		
 		String output = builder.toString();
 		return output;
 	}
 
 	public static String drawElement(BoardPiece<ReversiEntity> piece) {
 		String result = "";
 
 		ReversiEntity reversiPiece = piece.getEntity();
 
 		if (reversiPiece == null) {
 			result = boardEmptySpace;
 		} else {
 			ReversiPlayer player = reversiPiece.getOwner();
 			result = player.getAsciiDisplayPiece();
 		}
 
 		return result;
 	}
 
 }
