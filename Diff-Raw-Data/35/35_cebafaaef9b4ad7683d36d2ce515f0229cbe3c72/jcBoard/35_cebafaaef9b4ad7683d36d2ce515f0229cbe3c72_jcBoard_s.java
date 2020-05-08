 /*****************************************************************************
  * jcBoard.java - Encapsulation of a chess board
 * by Franois Dominic Larame
  *
  * Purpose: This object contains all of the data and methods required to
  * process a chess board in the game.  It uses the ubiquitous "bitboard"
  * representation.
  *
  * History:
  * 08.06.00 Created
  * 14.08.00 Made "HashLock" a relative clone of "HashKey"; the java
  *          Object.hashCode method is unsuitable to our purposes after all,
  *          probably because it includes memory addresses in the calculation.
  ***************************************************************************/
 
 package javachess;
 import java.util.Random;
 import java.io.*;
 
 /****************************************************************************
  * public class jcBoard
  *
  * Notes:
  * 1. The squares are numbered line by line, starting in the corner occupied by
  * Black's Queen's Rook at the beginning of the game.  There are no constants
  * to represent squares, as they are usually manipulated algorithmically, in
  * sequences, instead of being explicitly identified in the code.
  ***************************************************************************/
 
 public class jcBoard
 {
 
 /***************************************************************************
  * CONSTANTS
  **************************************************************************/
 
   // Codes representing pieces
   public static final int PAWN = 0;
   public static final int KNIGHT = 2;
   public static final int BISHOP = 4;
   public static final int ROOK = 6;
   public static final int QUEEN = 8;
   public static final int KING = 10;
   public static final int WHITE_PAWN = PAWN + jcPlayer.SIDE_WHITE;
   public static final int WHITE_KNIGHT = KNIGHT + jcPlayer.SIDE_WHITE;
   public static final int WHITE_BISHOP = BISHOP + jcPlayer.SIDE_WHITE;
   public static final int WHITE_ROOK = ROOK + jcPlayer.SIDE_WHITE;
   public static final int WHITE_QUEEN = QUEEN + jcPlayer.SIDE_WHITE;
   public static final int WHITE_KING = KING + jcPlayer.SIDE_WHITE;
   public static final int BLACK_PAWN = PAWN + jcPlayer.SIDE_BLACK;
   public static final int BLACK_KNIGHT = KNIGHT + jcPlayer.SIDE_BLACK;
   public static final int BLACK_BISHOP = BISHOP + jcPlayer.SIDE_BLACK;
   public static final int BLACK_ROOK = ROOK + jcPlayer.SIDE_BLACK;
   public static final int BLACK_QUEEN = QUEEN + jcPlayer.SIDE_BLACK;
   public static final int BLACK_KING = KING + jcPlayer.SIDE_BLACK;
   public static final int EMPTY_SQUARE = 12;
 
   // Useful loop boundary constants, to allow looping on all bitboards and
   // on all squares of a chessboard
   public static final int ALL_PIECES = 12;
   public static final int ALL_SQUARES = 64;
 
   // Indices of the "shortcut" bitboards containing information on "all black
   // pieces" and "all white pieces"
   public static final int ALL_WHITE_PIECES = ALL_PIECES + jcPlayer.SIDE_WHITE;
   public static final int ALL_BLACK_PIECES = ALL_PIECES + jcPlayer.SIDE_BLACK;
   public static final int ALL_BITBOARDS = 14;
 
   // The possible types of castling moves; add the "side" constant to
   // pick a specific move for a specific player
   public static final int CASTLE_KINGSIDE = 0;
   public static final int CASTLE_QUEENSIDE = 2;
 
 /***************************************************************************
  * DATA MEMBERS
  **************************************************************************/
 
   // An array of bitfields, each of which contains the single bit associated
   // with a square in a bitboard
   public static long SquareBits[];
 
   // Private table of random numbers used to compute Zobrist hash values
   // Contains a signature for any kind of piece on any square of the board
   private static int HashKeyComponents[][];
   private static int HashLockComponents[][];
 
   // Private table of tokens (string representations) for all pieces
   public static String PieceStrings[];
 
   // Data needed to compute the evaluation function
   private int MaterialValue[ ];
   private int NumPawns[ ];
   private static int PieceValues[ ];
 
   // And a few flags for special conditions.  The ExtraKings are a device
   // used to detect illegal castling moves: the rules of chess forbid castling
   // when the king is in check or when the square it flies over is under
   // attack; therefore, we add "phantom kings" to the board for one move only,
   // and if the opponent can capture one of them with its next move, then
   // castling was illegal and search can be cancelled
   private long ExtraKings[];
   public static long EXTRAKINGS_WHITE_KINGSIDE;
   public static long EXTRAKINGS_WHITE_QUEENSIDE;
   public static long EXTRAKINGS_BLACK_KINGSIDE;
   public static long EXTRAKINGS_BLACK_QUEENSIDE;
   public static long EMPTYSQUARES_WHITE_KINGSIDE;
   public static long EMPTYSQUARES_WHITE_QUEENSIDE;
   public static long EMPTYSQUARES_BLACK_KINGSIDE;
   public static long EMPTYSQUARES_BLACK_QUEENSIDE;
 
   // static member initialization
   static
   {
     // Build the SquareBits constants
     SquareBits = new long[ ALL_SQUARES ];
     for( int i = 0; i < ALL_SQUARES; i++ )
     {
       // Note: the 1L specifies that the 1 we are shifting is a long int
       // Java would, by default, make it a 4-byte int and be unable to
       // shift the 1 to bits 32 to 63
       SquareBits[ i ] = ( 1L << i );
     }
 
     // Build the extrakings constants
     EXTRAKINGS_WHITE_KINGSIDE = SquareBits[ 60 ] | SquareBits[ 61 ];
     EXTRAKINGS_WHITE_QUEENSIDE = SquareBits[ 60 ] | SquareBits[ 59 ];
     EXTRAKINGS_BLACK_KINGSIDE = SquareBits[ 4 ] | SquareBits[ 5 ];
     EXTRAKINGS_BLACK_QUEENSIDE = SquareBits[ 4 ] | SquareBits[ 3 ];
     EMPTYSQUARES_WHITE_KINGSIDE = SquareBits[ 61 ] | SquareBits[ 62 ];
     EMPTYSQUARES_WHITE_QUEENSIDE = SquareBits[ 59 ] | SquareBits[ 58 ] | SquareBits[ 57 ];
     EMPTYSQUARES_BLACK_KINGSIDE = SquareBits[ 5 ] | SquareBits[ 6 ];
     EMPTYSQUARES_BLACK_QUEENSIDE = SquareBits[ 3 ] | SquareBits[ 2 ] | SquareBits[ 1 ];
 
     // Build the hashing database
     HashKeyComponents = new int[ ALL_PIECES ][ ALL_SQUARES ];
     HashLockComponents = new int[ ALL_PIECES ][ ALL_SQUARES ];
     Random rnd = new Random();
     for( int i = 0; i < ALL_PIECES; i++ )
     {
       for( int j = 0; j < ALL_SQUARES; j++ )
       {
         HashKeyComponents[ i ][ j ] = rnd.nextInt();
         HashLockComponents[ i ][ j ] = rnd.nextInt();
       }
     }
 
     // Tokens representing the various concepts in the game, for printint
     // and file i/o purposes
     // PieceStrings contains an extra string representing empty squares
     PieceStrings = new String[ ALL_PIECES + 1 ];
     PieceStrings[ WHITE_PAWN ] = "WP";
     PieceStrings[ WHITE_ROOK ] = "WR";
     PieceStrings[ WHITE_KNIGHT ] = "WN";
     PieceStrings[ WHITE_BISHOP ] = "WB";
     PieceStrings[ WHITE_QUEEN ] = "WQ";
     PieceStrings[ WHITE_KING ] = "WK";
     PieceStrings[ BLACK_PAWN ] = "BP";
     PieceStrings[ BLACK_ROOK ] = "BR";
     PieceStrings[ BLACK_KNIGHT ] = "BN";
     PieceStrings[ BLACK_BISHOP ] = "BB";
     PieceStrings[ BLACK_QUEEN ] = "BQ";
     PieceStrings[ BLACK_KING ] = "BK";
     PieceStrings[ ALL_PIECES ] = "  ";
 
     // Numerical evaluation of piece material values
     PieceValues = new int[ ALL_PIECES ];
     PieceValues[ WHITE_PAWN ] = 100;
     PieceValues[ BLACK_PAWN ] = 100;
     PieceValues[ WHITE_KNIGHT ] = 300;
     PieceValues[ BLACK_KNIGHT ] = 300;
     PieceValues[ WHITE_BISHOP ] = 350;
     PieceValues[ BLACK_BISHOP ] = 350;
     PieceValues[ WHITE_ROOK ] = 500;
     PieceValues[ BLACK_ROOK ] = 500;
     PieceValues[ BLACK_QUEEN ] = 900;
     PieceValues[ WHITE_QUEEN ] = 900;
     PieceValues[ WHITE_KING ] = 2000;
     PieceValues[ BLACK_KING ] = 2000;
   }
 
   // The actual data representation of a chess board.  First, an array of
   // bitboards, each of which contains flags for the squares where you can
   // find a specific type of piece
   private long BitBoards[];
 
   // And a few other flags
   private boolean CastlingStatus[];
   private boolean HasCastled[];
   private long EnPassantPawn;
 
   // Whose turn is it?
   int CurrentPlayer;
 
 /**************************************************************************
  * METHODS
  **************************************************************************/
 
   // Accessors
   public boolean GetCastlingStatus( int which ) { return CastlingStatus[ which ]; }
   public boolean GetHasCastled( int which ) { return HasCastled[ which ]; }
   public long GetEnPassantPawn() { return EnPassantPawn; }
   public long GetExtraKings( int side ) { return ExtraKings[ side ]; }
   public void SetExtraKings( int side, long val )
   {
     // Mark a few squares as containing "phantom kings" to detect illegal
     // castling
     ExtraKings[ side ] = val;
     BitBoards[ KING + side ] |= ExtraKings[ side ];
     BitBoards[ ALL_PIECES + side ] |= ExtraKings[ side ];
   }
   public void ClearExtraKings( int side )
   {
     BitBoards[ KING + side ] ^= ExtraKings[ side ];
     BitBoards[ ALL_PIECES + side ] ^= ExtraKings[ side ];
     // Note: one of the Extra Kings is superimposed on the rook involved in
     // the castling, so the next step is required to prevent ALL_PIECES from
     // forgetting about the rook at the same time as the phantom king
     BitBoards[ ALL_PIECES + side ] |= BitBoards[ ROOK + side ];
     ExtraKings[ side ] = 0;
   }
   public int GetCurrentPlayer()  { return CurrentPlayer; }
   public long GetBitBoard( int which ) { return BitBoards[ which ]; }
 
   // Look for the piece located on a specific square
   public int FindBlackPiece( int square )
   {
     // Note: we look for kings first for two reasons: because it helps
     // detect check, and because there may be a phantom king (marking an
     // illegal castling move) and a rook on the same square!
     if ( ( BitBoards[ BLACK_KING ] & SquareBits[ square ] ) != 0 )
       return BLACK_KING;
     if ( ( BitBoards[ BLACK_QUEEN ] & SquareBits[ square ] ) != 0 )
       return BLACK_QUEEN;
     if ( ( BitBoards[ BLACK_ROOK ] & SquareBits[ square ] ) != 0 )
       return BLACK_ROOK;
     if ( ( BitBoards[ BLACK_KNIGHT ] & SquareBits[ square ] ) != 0 )
       return BLACK_KNIGHT;
     if ( ( BitBoards[ BLACK_BISHOP ] & SquareBits[ square ] ) != 0 )
       return BLACK_BISHOP;
     if ( ( BitBoards[ BLACK_PAWN ] & SquareBits[ square ] ) != 0 )
       return BLACK_PAWN;
     return EMPTY_SQUARE;
   }
   public int FindWhitePiece( int square )
   {
     if ( ( BitBoards[ WHITE_KING ] & SquareBits[ square ] ) != 0 )
       return WHITE_KING;
     if ( ( BitBoards[ WHITE_QUEEN ] & SquareBits[ square ] ) != 0 )
       return WHITE_QUEEN;
     if ( ( BitBoards[ WHITE_ROOK ] & SquareBits[ square ] ) != 0 )
       return WHITE_ROOK;
     if ( ( BitBoards[ WHITE_KNIGHT ] & SquareBits[ square ] ) != 0 )
       return WHITE_KNIGHT;
     if ( ( BitBoards[ WHITE_BISHOP ] & SquareBits[ square ] ) != 0 )
       return WHITE_BISHOP;
     if ( ( BitBoards[ WHITE_PAWN ] & SquareBits[ square ] ) != 0 )
       return WHITE_PAWN;
     return EMPTY_SQUARE;
   }
 
 
   // Constructor
   public jcBoard()
   {
     BitBoards = new long[ ALL_BITBOARDS ];
     CastlingStatus = new boolean[ 4 ];
     HasCastled = new boolean[ 2 ];
     ExtraKings = new long[ 2 ];
     NumPawns = new int[ 2 ];
     MaterialValue = new int[ 2 ];
     StartingBoard();
   }
 
   // public boolean Clone
   // Make a deep copy of a jcBoard object; assumes that memory has already
   // been allocated for the new object, which is always true since we
   // "allocate" jcBoards from a permanent array
   public boolean Clone( jcBoard target )
   {
     EnPassantPawn = target.EnPassantPawn;
     for( int i = 0; i < 4; i++ )
     {
       CastlingStatus[ i ] = target.CastlingStatus[ i ];
     }
     for( int i = 0; i < ALL_BITBOARDS; i++ )
     {
       BitBoards[ i ] = target.BitBoards[ i ];
     }
     MaterialValue[ 0 ] = target.MaterialValue[ 0 ];
     MaterialValue[ 1 ] = target.MaterialValue[ 1 ];
     NumPawns[ 0 ] = target.NumPawns[ 0 ];
     NumPawns[ 1 ] = target.NumPawns[ 1 ];
     ExtraKings[ 0 ] = target.ExtraKings[ 0 ];
     ExtraKings[ 1 ] = target.ExtraKings[ 1 ];
     HasCastled[ 0 ] = target.HasCastled[ 0 ];
     HasCastled[ 1 ] = target.HasCastled[ 1 ];
     CurrentPlayer = target.CurrentPlayer;
     return true;
   }
 
   // public boolean Print
   // Display the board on standard output
   public boolean Print()
   {
     for( int line = 0; line < 8; line++ )
     {
       System.out.println( "-----------------------------------------" );
       System.out.println( "|    |    |    |    |    |    |    |    |" );
       for( int col = 0; col < 8; col++ )
       {
         long bits = SquareBits[ line * 8 + col ];
 
         // Scan the bitboards to find a piece, if any
         int piece = 0;
         while ( ( piece < ALL_PIECES ) && ( ( bits & BitBoards[ piece ] ) == 0 ) )
           piece++;
 
         // One exception: don't show the "phantom kings" which the program places
         // on the board to detect illegal attempts at castling over an attacked
         // square
         if ( ( piece == BLACK_KING ) && ( ( ExtraKings[ jcPlayer.SIDE_BLACK ] & SquareBits[ line * 8 + col ] ) != 0 ) )
           piece = EMPTY_SQUARE;
         if ( ( piece == WHITE_KING ) && ( ( ExtraKings[ jcPlayer.SIDE_WHITE ] & SquareBits[ line * 8 + col ] ) != 0 ) )
           piece = EMPTY_SQUARE;
 
         // Show the piece
         System.out.print( "| " + PieceStrings[ piece ] + " " );
       }
       System.out.println( "|" );
       System.out.println( "|    |    |    |    |    |    |    |    |" );
     }
     System.out.println( "-----------------------------------------" );
     if ( CurrentPlayer == jcPlayer.SIDE_BLACK )
       System.out.println( "NEXT MOVE: BLACK ");
     else
       System.out.println( "NEXT MOVE: WHITE" );
 
     return true;
   }
 
   // public int SwitchSides
   // Change the identity of the player to move
   public int SwitchSides()
   {
     if ( CurrentPlayer == jcPlayer.SIDE_WHITE )
       SetCurrentPlayer( jcPlayer.SIDE_BLACK );
     else
       SetCurrentPlayer( jcPlayer.SIDE_WHITE );
 
     return CurrentPlayer;
   }
 
   // public int HashKey
   // Compute a 32-bit integer to represent the board, according to Zobrist[70]
   public int HashKey()
   {
     int hash = 0;
     // Look at all pieces, one at a time
     for( int currPiece = 0; currPiece < ALL_PIECES; currPiece++ )
     {
       long tmp = BitBoards[ currPiece ];
       // Search for all pieces on all squares.  We could optimize here: not
       // looking for pawns on the back row (or the eight row), getting out
       // of the "currSqaure" loop once we found one king of one color, etc.
       // But for simplicity's sake, we'll keep things generic.
       for( int currSquare = 0; currSquare < ALL_SQUARES; currSquare++ )
       {
         // Zobrist's method: generate a bunch of random bitfields, each
         // representing a certain "piece X is on square Y" predicate; XOR
         // the bitfields associated with predicates which are true.
         // Therefore, if we find a piece (in tmp) in a certain square,
         // we accumulate the related HashKeyComponent.
         if ( ( tmp & SquareBits[ currSquare ] ) != 0 )
           hash ^= HashKeyComponents[ currPiece ][ currSquare ];
       }
     }
     return hash;
   }
 
   // public int HashLock
   // Compute a second 32-bit hash key, using an entirely different set
   // piece/square components.
   // This is required to be able to detect hashing collisions without
   // storing an entire jcBoard in each slot of the jcTranspositionTable,
   // which would gobble up inordinate amounts of memory
   public int HashLock()
   {
     int hash = 0;
     for( int currPiece = 0; currPiece < ALL_PIECES; currPiece++ )
     {
       long tmp = BitBoards[ currPiece ];
       for( int currSquare = 0; currSquare < ALL_SQUARES; currSquare++ )
       {
         if ( ( tmp & SquareBits[ currSquare ] ) != 0 )
           hash ^= HashLockComponents[ currPiece ][ currSquare ];
       }
     }
     return hash;
   }
 
   // public boolean ApplyMove
   // Change the jcBoard's internal representation to reflect the move
   // received as a parameter
   public boolean ApplyMove( jcMove theMove )
   {
     // If the move includes a pawn promotion, an extra step will be required
     // at the end
     boolean isPromotion = ( theMove.MoveType >= jcMove.MOVE_PROMOTION_KNIGHT );
     int moveWithoutPromotion = ( theMove.MoveType & jcMove.NO_PROMOTION_MASK );
     int side = theMove.MovingPiece % 2;
 
     // For now, ignore pawn promotions
     switch( moveWithoutPromotion )
     {
       case jcMove.MOVE_NORMAL:
         // The simple case
         RemovePiece( theMove.SourceSquare, theMove.MovingPiece );
         AddPiece( theMove.DestinationSquare, theMove.MovingPiece );
         break;
       case jcMove.MOVE_CAPTURE_ORDINARY:
         // Don't forget to remove the captured piece!
         RemovePiece( theMove.SourceSquare, theMove.MovingPiece );
         RemovePiece( theMove.DestinationSquare, theMove.CapturedPiece );
         AddPiece( theMove.DestinationSquare, theMove.MovingPiece );
         break;
       case jcMove.MOVE_CAPTURE_EN_PASSANT:
         // Here, we can use our knowledge of the board to make a small
         // optimization, since the pawn to be captured is always
         // "behind" the moving pawn's destination square, we can compute its
         // position on the fly
         RemovePiece( theMove.SourceSquare, theMove.MovingPiece );
         AddPiece( theMove.DestinationSquare, theMove.MovingPiece );
         if ( ( theMove.MovingPiece % 2 ) == jcPlayer.SIDE_WHITE )
           RemovePiece( theMove.DestinationSquare + 8, theMove.CapturedPiece );
         else
           RemovePiece( theMove.DestinationSquare - 8, theMove.CapturedPiece );
         break;
       case jcMove.MOVE_CASTLING_QUEENSIDE:
         // Again, we can compute the rook's source and destination squares
         // because of our knowledge of the board's structure
         RemovePiece( theMove.SourceSquare, theMove.MovingPiece );
         AddPiece( theMove.DestinationSquare, theMove.MovingPiece );
         int theRook = ROOK + ( theMove.MovingPiece % 2 );
         RemovePiece( theMove.SourceSquare - 4, theRook );
         AddPiece( theMove.SourceSquare - 1, theRook );
         // We must now mark some squares as containing "phantom kings" so that
         // the castling can be cancelled by the next opponent's move, if he
         // can move to one of them
         if ( side == jcPlayer.SIDE_WHITE )
         {
           SetExtraKings( side, EXTRAKINGS_WHITE_QUEENSIDE );
         }
         else
         {
           SetExtraKings( side, EXTRAKINGS_BLACK_QUEENSIDE );
         }
         HasCastled[ side ] = true;
         break;
       case jcMove.MOVE_CASTLING_KINGSIDE:
         // Again, we can compute the rook's source and destination squares
         // because of our knowledge of the board's structure
         RemovePiece( theMove.SourceSquare, theMove.MovingPiece );
         AddPiece( theMove.DestinationSquare, theMove.MovingPiece );
         theRook = ROOK + ( theMove.MovingPiece % 2 );
         RemovePiece( theMove.SourceSquare + 3, theRook );
         AddPiece( theMove.SourceSquare + 1, theRook );
         // We must now mark some squares as containing "phantom kings" so that
         // the castling can be cancelled by the next opponent's move, if he
         // can move to one of them
         if ( side == jcPlayer.SIDE_WHITE )
         {
           SetExtraKings( side, EXTRAKINGS_WHITE_KINGSIDE );
         }
         else
         {
           SetExtraKings( side, EXTRAKINGS_BLACK_KINGSIDE );
         }
         HasCastled[ side ] = true;
         break;
       case jcMove.MOVE_RESIGN:
         // FDL Later, ask the AI player who resigned to print the continuation
         break;
       case jcMove.MOVE_STALEMATE:
         System.out.println( "Stalemate - Game is a draw." );
         break;
     }
 
     // And now, apply the promotion
     if ( isPromotion )
     {
       int promotionType = ( theMove.MoveType & jcMove.PROMOTION_MASK );
       int color = ( theMove.MovingPiece % 2 );
       switch( promotionType )
       {
         case jcMove.MOVE_PROMOTION_KNIGHT:
           RemovePiece( theMove.DestinationSquare, theMove.MovingPiece );
           AddPiece( theMove.DestinationSquare, KNIGHT + color );
           break;
         case jcMove.MOVE_PROMOTION_BISHOP:
           RemovePiece( theMove.DestinationSquare, theMove.MovingPiece );
           AddPiece( theMove.DestinationSquare, BISHOP + color );
           break;
         case jcMove.MOVE_PROMOTION_ROOK:
           RemovePiece( theMove.DestinationSquare, theMove.MovingPiece );
           AddPiece( theMove.DestinationSquare, ROOK + color );
           break;
         case jcMove.MOVE_PROMOTION_QUEEN:
           RemovePiece( theMove.DestinationSquare, theMove.MovingPiece );
           AddPiece( theMove.DestinationSquare, QUEEN + color );
           break;
       }
     }
 
     // If this was a 2-step pawn move, we now have a valid en passant
     // capture possibility.  Otherwise, no.
     if ( ( theMove.MovingPiece == jcBoard.WHITE_PAWN ) &&
          ( theMove.SourceSquare - theMove.DestinationSquare == 16 ) )
       SetEnPassantPawn( theMove.DestinationSquare + 8 );
     else if ( ( theMove.MovingPiece == jcBoard.BLACK_PAWN ) &&
               ( theMove.DestinationSquare - theMove.SourceSquare == 16 ) )
       SetEnPassantPawn( theMove.SourceSquare + 8 );
     else
       ClearEnPassantPawn();
 
     // And now, maintain castling status
     // If a king moves, castling becomes impossible for that side, for the
     // rest of the game
     switch( theMove.MovingPiece )
     {
       case WHITE_KING:
         SetCastlingStatus( CASTLE_KINGSIDE + jcPlayer.SIDE_WHITE, false );
         SetCastlingStatus( CASTLE_QUEENSIDE + jcPlayer.SIDE_WHITE, false );
         break;
       case BLACK_KING:
         SetCastlingStatus( CASTLE_KINGSIDE + jcPlayer.SIDE_BLACK, false );
         SetCastlingStatus( CASTLE_QUEENSIDE + jcPlayer.SIDE_BLACK, false );
         break;
       default:
         break;
     }
 
     // Or, if ANYTHING moves from a corner, castling becomes impossible on
     // that side (either because it's the rook that is moving, or because
     // it has been captured by whatever moves, or because it is already gone)
     switch( theMove.SourceSquare )
     {
       case 0:
         SetCastlingStatus( CASTLE_QUEENSIDE + jcPlayer.SIDE_BLACK, false );
         break;
       case 7:
         SetCastlingStatus( CASTLE_KINGSIDE + jcPlayer.SIDE_BLACK, false );
         break;
       case 56:
         SetCastlingStatus( CASTLE_QUEENSIDE + jcPlayer.SIDE_WHITE, false );
         break;
       case 63:
         SetCastlingStatus( CASTLE_KINGSIDE + jcPlayer.SIDE_WHITE, false );
         break;
       default:
         break;
     }
 
     // All that remains to do is switch sides
     SetCurrentPlayer( ( GetCurrentPlayer() + 1 ) % 2 );
     return true;
   }
 
   // public boolean Load
   // Load a board from a file
   public boolean Load( String fileName ) throws Exception
   {
     // Clean the board first
     EmptyBoard();
 
     // Open the file as a Java tokenizer
     FileReader fr = new FileReader( fileName );
     StreamTokenizer tok = new StreamTokenizer( fr );
     tok.eolIsSignificant( false );
     tok.lowerCaseMode( false );
 
     // Whose turn is it to play?
     tok.nextToken();
     if ( tok.sval.equalsIgnoreCase( jcPlayer.PlayerStrings[ jcPlayer.SIDE_WHITE ] ) )
       SetCurrentPlayer( jcPlayer.SIDE_WHITE );
     else
       SetCurrentPlayer( jcPlayer.SIDE_BLACK );
 
     // Read the positions of all the pieces
     // First, look for the number of pieces on the board
     tok.nextToken();
     int numPieces = (int) tok.nval;
 
     // Now, loop on the pieces in question
     for( int i = 0; i < numPieces; i++ )
     {
       // What kind of piece is this, and where does it go?
       tok.nextToken();
       String whichPieceStr = tok.sval;
 
       int whichPiece = 0;
       while ( !whichPieceStr.equalsIgnoreCase( PieceStrings[ whichPiece ] ) )
         whichPiece++;
 
       tok.nextToken();
       int whichSquare = (int) tok.nval;
 
       // Add the piece to the board
       AddPiece( whichSquare, whichPiece );
     }
 
     // Now, read the castling status flags
     for( int i = 0; i < 4; i++ )
     {
       tok.nextToken();
       if ( "TRUE".equalsIgnoreCase( tok.sval ) )
         SetCastlingStatus( i, true );
       else
         SetCastlingStatus( i, false );
     }
 
     // And finally, read the bitboard representing the position of the en
     // passant pawn, if any
     tok.nextToken();
     SetEnPassantPawn( (long) tok.nval );
 
     fr.close();
     return true;
   }
 
   // public boolean Save
   // Save the state of the game to a file
   public boolean Save( String fileName ) throws Exception
   {
     // Open the file for business
     FileWriter fr = new FileWriter( fileName );
     BufferedWriter bw = new BufferedWriter( fr );
 
     // Whose turn is it?
     bw.write( jcPlayer.PlayerStrings[ CurrentPlayer ] );
     bw.newLine();
 
     // Count the pieces on the board
     int numPieces = 0;
     for( int i = 0; i < ALL_SQUARES; i++ )
     {
       if ( ( SquareBits[ i ] & BitBoards[ ALL_WHITE_PIECES ] ) != 0 )
         numPieces++;
       if ( ( SquareBits[ i ] & BitBoards[ ALL_BLACK_PIECES ] ) != 0 )
         numPieces++;
     }
     bw.write( String.valueOf( numPieces ) );
     bw.newLine();
 
     // Dump the pieces, one by one
     for( int piece = 0; piece < ALL_PIECES; piece++ )
     {
       for( int square = 0; square < ALL_SQUARES; square++ )
       {
         if ( ( BitBoards[ piece ] & SquareBits[ square ] ) != 0 )
         {
           bw.write( PieceStrings[ piece ] + " " + String.valueOf( square ) );
           bw.newLine();
         }
       }
     }
 
     // And finally, dump the castling status and the en passant pawn
     for( int i = 0; i < 4; i++ )
     {
       if ( CastlingStatus[ i ] )
         bw.write( "TRUE" );
       else
         bw.write( "FALSE" );
       bw.newLine();
     }
 
     bw.write( String.valueOf( EnPassantPawn ) );
 
     bw.close();
     return true;
   }
 
   // public int EvalMaterial
   // Compute the board's material balance, from the point of view of the "side"
   // player.  This is an exact clone of the eval function in CHESS 4.5
   public int EvalMaterial( int side )
   {
     // If both sides are equal, no need to compute anything!
     if ( MaterialValue[ jcPlayer.SIDE_BLACK ] == MaterialValue[ jcPlayer.SIDE_WHITE ] )
       return 0;
 
     int otherSide = ( side + 1 ) % 2;
     int matTotal = MaterialValue[ side ] + MaterialValue[ otherSide ];
 
     // Who is leading the game, material-wise?
     if ( MaterialValue[ jcPlayer.SIDE_BLACK ] > MaterialValue[ jcPlayer.SIDE_WHITE ] )
     {
       // Black leading
       int matDiff = MaterialValue[ jcPlayer.SIDE_BLACK ] - MaterialValue[ jcPlayer.SIDE_WHITE ];
       int val = Math.min( 2400, matDiff ) +
                   ( matDiff * ( 12000 - matTotal ) * NumPawns[ jcPlayer.SIDE_BLACK ] )
                   / ( 6400 * ( NumPawns[ jcPlayer.SIDE_BLACK ] + 1 ) );
       if ( side == jcPlayer.SIDE_BLACK )
         return val;
       else
         return -val;
     }
     else
     {
       // White leading
       int matDiff = MaterialValue[ jcPlayer.SIDE_WHITE ] - MaterialValue[ jcPlayer.SIDE_BLACK ];
       int val = Math.min( 2400, matDiff ) +
                   ( matDiff * ( 12000 - matTotal ) * NumPawns[ jcPlayer.SIDE_WHITE ] )
                   / ( 6400 * ( NumPawns[ jcPlayer.SIDE_WHITE ] + 1 ) );
 
       if ( side == jcPlayer.SIDE_WHITE )
         return val;
       else
         return -val;
    }
   }
 
   // public boolean StartingBoard
   // Restore the board to a game-start position
   public boolean StartingBoard()
   {
     // Put the pieces on the board
     EmptyBoard();
     AddPiece( 0, BLACK_ROOK );
     AddPiece( 1, BLACK_KNIGHT );
     AddPiece( 2, BLACK_BISHOP );
     AddPiece( 3, BLACK_QUEEN );
     AddPiece( 4, BLACK_KING );
     AddPiece( 5, BLACK_BISHOP );
     AddPiece( 6, BLACK_KNIGHT );
     AddPiece( 7, BLACK_ROOK );
     for( int i = 8; i < 16; i++ )
     {
       AddPiece( i, BLACK_PAWN );
     }
 
     for( int i = 48; i < 56; i++ )
     {
       AddPiece( i, WHITE_PAWN );
     }
     AddPiece( 56, WHITE_ROOK );
     AddPiece( 57, WHITE_KNIGHT );
     AddPiece( 58, WHITE_BISHOP );
     AddPiece( 59, WHITE_QUEEN );
     AddPiece( 60, WHITE_KING );
     AddPiece( 61, WHITE_BISHOP );
     AddPiece( 62, WHITE_KNIGHT );
     AddPiece( 63, WHITE_ROOK );
 
     // And allow all castling moves
     for( int i = 0; i < 4; i++ )
     {
       CastlingStatus[ i ] = true;
     }
     HasCastled[ 0 ] = false;
     HasCastled[ 1 ] = false;
     ClearEnPassantPawn();
 
     // And ask White to play the first move
     SetCurrentPlayer( jcPlayer.SIDE_WHITE );
     return true;
   }
 
 /******************************************************************************
  * PRIVATE METHODS
  *****************************************************************************/
 
   // private boolean AddPiece
   // Place a specific piece on a specific board square
   private boolean AddPiece( int whichSquare, int whichPiece )
   {
     // Add the piece itself
     BitBoards[ whichPiece ] |= SquareBits[ whichSquare ];
 
     // And note the new piece position in the bitboard containing all
     // pieces of its color.  Here, we take advantage of the fact that
     // all pieces of a given color are represented by numbers of the same
     // parity
     BitBoards[ ALL_PIECES + ( whichPiece % 2 ) ] |= SquareBits[ whichSquare ];
 
     // And adjust material balance accordingly
     MaterialValue[ whichPiece % 2 ] += PieceValues[ whichPiece ];
     if ( whichPiece == WHITE_PAWN )
       NumPawns[ jcPlayer.SIDE_WHITE ]++;
     else if ( whichPiece == BLACK_PAWN )
       NumPawns[ jcPlayer.SIDE_BLACK ]++;
 
     return true;
   }
 
   // private boolean RemovePiece
   // Eliminate a specific piece from a specific square on the board
   // Note that you MUST know that the piece is there before calling this,
   // or the results will not be what you expect!
   private boolean RemovePiece( int whichSquare, int whichPiece )
   {
     // Remove the piece itself
     BitBoards[ whichPiece ] ^= SquareBits[ whichSquare ];
     BitBoards[ ALL_PIECES + ( whichPiece % 2 ) ] ^= SquareBits[ whichSquare ];
 
     // And adjust material balance accordingly
     MaterialValue[ whichPiece % 2 ] -= PieceValues[ whichPiece ];
     if ( whichPiece == WHITE_PAWN )
       NumPawns[ jcPlayer.SIDE_WHITE ]--;
     else if ( whichPiece == BLACK_PAWN )
       NumPawns[ jcPlayer.SIDE_BLACK ]--;
     return true;
   }
 
 
   // private boolean EmptyBoard
   // Remove every piece from the board
   private boolean EmptyBoard()
   {
     for( int i = 0; i < ALL_BITBOARDS; i++ )
     {
       BitBoards[ i ] = 0;
     }
     ExtraKings[ 0 ] = 0;
     ExtraKings[ 1 ] = 0;
     EnPassantPawn = 0;
     MaterialValue[ 0 ] = 0;
     MaterialValue[ 1 ] = 0;
     NumPawns[ 0 ] = 0;
     NumPawns[ 1 ] = 0;
     return true;
   }
 
   // private boolean SetCastlingStatus
   // Change one of the "castling status" flags
   // parameter whichFlag should be a sum of a side marker and a castling
   // move identifier, for example, jcPlayer.SIDE_WHITE + CASTLE_QUEENSIDE
   private boolean SetCastlingStatus( int whichFlag, boolean newValue )
   {
     CastlingStatus[ whichFlag ] = newValue;
     return true;
   }
 
   // private boolean SetEnPassantPawn
   // If a pawn move has just made en passant capture possible, mark it as
   // such in a bitboard (containing the en passant square only)
   private boolean SetEnPassantPawn( int square )
   {
     ClearEnPassantPawn();
     EnPassantPawn |= SquareBits[ square ];
     return true;
   }
 
   private boolean SetEnPassantPawn( long bitboard )
   {
     EnPassantPawn = bitboard;
     return true;
   }
 
   // private boolean ClearEnPassantPawn
   // Indicates that there is no en passant square at all.  Technically, this
   // job could have been handled by SetEnPassaantPawn( long ) with a null
   // parameter, but I have chosen to add a method to avoid problems if I ever
   // forgot to specify 0L: using 0 would call the first form of the Set method
   // and indicate an en passant pawn in a corner of the board, with possibly
   // disastrous consequences!
   private boolean ClearEnPassantPawn()
   {
     EnPassantPawn = 0;
     return true;
   }
 
   // private boolean SetCurrentPlayer
   // Whose turn is it?
   private boolean SetCurrentPlayer( int which )
   {
     CurrentPlayer = which;
     return true;
   }
 }
