 package villebasse.gamelogic;
 
 import java.util.Vector;
 import villebasse.gamelogic.defaultpieces.*;
 
 
 public class DefaultDeckWithoutCloisters extends Deck
 {
 	public DefaultDeckWithoutCloisters()
 	{
 		this.pieces = new Vector<Piece>(72);
 
 		for (int i = 0; i < 5; ++i)
 			this.pieces.add(new PieceDiagonalCity());
 
		for (int i = 0; i < 2; ++i)
 			this.pieces.add(new PieceCityEndWithIntersection());
 
 		for (int i = 0; i < 3; ++i)
 			this.pieces.add(new PieceFacingCities());
 
 		for (int i = 0; i < 9; ++i)
 			this.pieces.add(new PieceRoadTurn());
 
 		for (int i = 0; i < 8; ++i)
 			this.pieces.add(new PieceRoad());
 
 		for (int i = 0; i < 4; ++i)
 			this.pieces.add(new PieceTIntersection());
 
 		for (int i = 0; i < 4; ++i)
 			this.pieces.add(new PieceBigCity());
 
 		for (int i = 0; i < 3; ++i)
 			this.pieces.add(new PieceBigCityWithRoad());
 
 		for (int i = 0; i < 2; ++i)
 			this.pieces.add(new PieceCityCorner());
 
 		for (int i = 0; i < 5; ++i)
 			this.pieces.add(new PieceCityEnd());
 
		for (int i = 0; i < 4; ++i)
 			this.pieces.add(new PieceCityEndWithRoad());
 
 		for (int i = 0; i < 3; ++i)
 			this.pieces.add(new PieceCityEndWithRoadLeft());
 
 		for (int i = 0; i < 3; ++i)
 			this.pieces.add(new PieceCityEndWithRoadRight());
 
 		for (int i = 0; i < 3; ++i)
 			this.pieces.add(new PieceCityPipe());
 
 		for (int i = 0; i < 5; ++i)
 			this.pieces.add(new PieceDiagonalCityWithRoad());
 
 		/*
 		this.pieces.add(new PieceXIntersection());
 		this.pieces.add(new PieceFullCity());
 		*/
 
 		this.shuffle();
		this.pieces.add(new PieceCityEndWithIntersection());
 	}
 }
