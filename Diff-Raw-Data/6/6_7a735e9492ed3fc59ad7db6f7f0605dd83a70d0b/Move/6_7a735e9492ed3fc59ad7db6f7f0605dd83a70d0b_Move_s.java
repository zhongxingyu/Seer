 import java.io.Serializable;
 
 public class Move implements Serializable {
 	private static final long serialVersionUID = 1L;
 	public static final int NORMAL = 0;//TODO: enum? Maybe not since we do math on it
 	public static final int EN_PASSANT = 1;
 	public static final int CASTLE = 2;
 	public static final int PROMOTE_QUEEN = 3;
 	public static final int PROMOTE_KNIGHT = 4;
 	public static final int PROMOTE_ROOK = 5;
 	public static final int PROMOTE_BISHOP = 6;
 	public static final int PLACEMENT = 7;
 
 	public Piece piece;
 	public Tile toTile;
	public int moveType;
 	public Player player = null;
 	public Move(Piece piece, Tile toTile) {
 		this.piece = piece;
 		this.toTile = toTile;
		this.moveType = PLACEMENT;
 	}
 	public Move(Piece piece, Tile toTile, Player p) {
 		this.piece = piece;
 		this.toTile = toTile;
		this.moveType = PLACEMENT;
 		this.player = p;
 	}
 	public boolean equals(Move m) {
 		return piece.equals(m.piece) && toTile.equals(m.toTile);//TODO: will this need to be changed to account for movingPiece
 	}
 }
