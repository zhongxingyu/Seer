 package com.chess.genesis;
 
 import android.content.Context;
 import android.view.View;
 import android.widget.FrameLayout;
 
 class BoardButton extends FrameLayout
 {
 	private static final int[] pieceImages = {
 		R.drawable.piece_black_king,		R.drawable.piece_black_queen,
 		R.drawable.piece_black_rook,		R.drawable.piece_black_bishop,
 		R.drawable.piece_black_knight,		R.drawable.piece_black_pawn,
 		R.drawable.square_none,
 		R.drawable.piece_white_pawn,		R.drawable.piece_white_knight,
 		R.drawable.piece_white_bishop,		R.drawable.piece_white_rook,
 		R.drawable.piece_white_queen,		R.drawable.piece_white_king};
 
 	private static final int WHITE = 0;
 	private static final int BLACK = 1;
 
 	private final int squareColor;
 	private final int squareIndex;
 
 	private int piece = 0;
 	private boolean isHighlighted = false;
 	private boolean isCheck = false;
 	private boolean isLast = false;
 
 	public BoardButton(final Context context, final int index)
 	{
 		super(context);
 		View.inflate(context, R.layout.framelayout_boardbutton, this);
 
 		squareIndex = index;
 		squareColor = ((index / 16) % 2 == 1)?
				((index % 2 == 1)? WHITE : BLACK) :
				((index % 2 == 1)? BLACK : WHITE);
 		setId(squareIndex);
 
 		setSquareImage();
 	}
 
 	private void setSquareImage()
 	{
 		final int image = (squareColor == WHITE)?
 			R.drawable.square_light : R.drawable.square_dark;
 
 		final MyImageView img = (MyImageView) findViewById(R.id.board_layer);
 		img.setImageResource(image);
 	}
 
 	private void setHighlightImage()
 	{
 		final int image = isHighlighted?
 				R.drawable.square_ih_green :
 			(isLast?
 				R.drawable.square_ih_purple :
 			(isCheck?
 				R.drawable.square_ih_red :
 				R.drawable.square_none));
 
 		final MyImageView img = (MyImageView) findViewById(R.id.highlight_layer);
 		img.setImageResource(image);
 	}
 
 	public void resetSquare()
 	{
 		isHighlighted = false;
 		isCheck = false;
 
 		setHighlightImage();
 		setPiece(0);
 	}
 
 	public void setPiece(final int piece_type)
 	{
 		piece = piece_type;
 
 		final MyImageView img = (MyImageView) findViewById(R.id.piece_layer);
 		img.setImageResource(pieceImages[piece + 6]);
 	}
 
 	public int getPiece()
 	{
 		return piece;
 	}
 
 	public int getIndex()
 	{
 		return squareIndex;
 	}
 
 	public void setHighlight(final boolean mode)
 	{
 		isHighlighted = mode;
 		setHighlightImage();
 	}
 
 	public void setCheck(final boolean mode)
 	{
 		isCheck = mode;
 		setHighlightImage();
 	}
 
 	public void setLast(final boolean mode)
 	{
 		isLast = mode;
 		setHighlightImage();
 	}
 }
