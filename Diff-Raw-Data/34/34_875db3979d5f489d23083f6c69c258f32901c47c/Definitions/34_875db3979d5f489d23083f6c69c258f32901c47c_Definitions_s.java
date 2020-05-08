 package chess;
 
 /* 
  * "Exploding bitboards" implementation was adapted from the C++ implementation
  *   found at http://chessprogramming.wikispaces.com/Exploding+Bitboards
  */
 
 public class Definitions
 {
 	public static final int HEIGHT = 480;
 	public static final int WIDTH = 640;
 	public static enum Color { WHITE, BLACK };
 	public static enum State { UNCHECKED, NORMAL, CHECKMATE, STALEMATE };
 	public static final int NUMROWS = 8;
 	public static final int NUMCOLS = 8;
 	public static final int NUMPIECES = 12;
 	public static final String PIECENAMES[] = {"BK","BQ","BN","BB","BR","BP","WK","WQ","WN","WB","WR","WP"};
 	public static final String RMARKERS[] = {"8","7","6","5","4","3","2","1"};
 	public static final String CMARKERS[] = {"a","b","c","d","e","f","g","h"};
	public static final int MAXDEPTH = 2; //PLY = 2 * DEPTH
	public static final double MAXTHINKINGTIME = 10000; //in seconds
 
 	public static final int repsB[] =
 		{
 		6, 5, 4, 3, 3, 4, 5, 6,
 		5, 5, 4, 3, 3, 4, 5, 5,
 		4, 4, 4, 3, 3, 4, 4, 4,
 		3, 3, 3, 3, 3, 3, 3, 3,
 		3, 3, 3, 3, 3, 3, 3, 3,
 		4, 4, 4, 3, 3, 4, 4, 4,
 		5, 5, 4, 3, 3, 4, 5, 5,
 		6, 5, 4, 3, 3, 4, 5, 6,
 		};
 	public static final int repsR[] =
 		{
 		7, 6, 6, 6, 6, 6, 6, 8,
 		7, 5, 5, 5, 5, 5, 5, 8,
 		7, 5, 4, 4, 4, 4, 5, 8,
 		7, 5, 4, 3, 3, 4, 5, 8,
 		7, 5, 4, 3, 3, 4, 5, 8,
 		7, 5, 4, 4, 4, 4, 5, 8,
 		7, 5, 5, 5, 5, 5, 5, 8,
 		7, 6, 6, 6, 6, 6, 6, 8,
 		};
 	public static final long allH = 0x0101010101010101L;
 	public static final long allA = 0x8080808080808080L;
 
 	public static final long[] initB = new long[64];
 	public static final long[] maskB = new long[64];
 	public static final long[] initR = new long[64];
 	public static final long[] maskR = new long[64];
 	public static final int[][] rankR = new int[8][128];
 
 	public static void makeInitB()
 	{
 		for ( int sq = 0; sq < 64; ++sq )
 		{
 			long bb = 1L << sq;
 			initB[sq]  = (bb >>> 9) & 0x7f7f7f7f7f7f7f7fL;
 			initB[sq] |= (bb >>> 7) & 0xfefefefefefefefeL;
 			initB[sq] |= (bb << 9) & 0xfefefefefefefefeL;
 			initB[sq] |= (bb << 7) & 0x7f7f7f7f7f7f7f7fL;
 		}
 	}
 
 	public static void makeMaskB()
 	{
 		int sq;
 		for ( sq = 0; sq < 64; ++sq )
 		{
 			maskB[sq] = 0;
 			int i;
 			for ( i = sq - 9; i >= 0 && i % 8 != 7; i -= 9 )
 				maskB[sq] |= 1L << i;
 			for ( i = sq - 7; i >= 0 && i % 8 != 0; i -= 7 )
 				maskB[sq] |= 1L << i;
 			for ( i = sq + 9; i < 64 && i % 8 != 0; i += 9 )
 				maskB[sq] |= 1L << i;
 			for ( i = sq + 7; i < 64 && i % 8 != 7; i += 7 )
 				maskB[sq] |= 1L << i;
 		}
 	}
 
 	public static long bishopAttacks(int sq, long free)
 	{
 		long msk = maskB[sq];
 		long bb  = initB[sq];
 		long at  = bb;
 		bb &= free;
 		switch (Definitions.repsB[sq])
 		{
 		case 6:
 			bb >>>= 9; bb *= 0x00050005; bb &= msk; at |= bb; bb &= free;
 		case 5:
 			bb >>>= 9; bb *= 0x00050005; bb &= msk; at |= bb; bb &= free;
 		case 4:
 			bb >>>= 9; bb *= 0x00050005; bb &= msk; at |= bb; bb &= free;
 		case 3:
 			bb >>>= 9; bb *= 0x00050005; bb &= msk; at |= bb; bb &= free;
 			bb >>>= 9; bb *= 0x00050005; bb &= msk; at |= bb; bb &= free;
 			bb >>>= 9; bb *= 0x00050005; bb &= msk; at |= bb;
 		}
 		return at;
 	}
 
 	public static long bishopAttacks(long bishops, long free)
 	{
 		long att = 0;
 		for (int i = 0; i < 64; i++)
 		{
 			if (((bishops >>> i) & 1) != 0)
 			{
 				att |= bishopAttacks(i, free);
 			}
 		}
 		return att;
 	}
 	
 	public static void makeInitR()
 	{
 		for (int sq = 0; sq < 64; ++sq)
 		{
 			long bb = 1L << sq;
 			initR[sq]  = (bb >>> 8);
 			initR[sq] |= (bb >>> 1) & 0x7f7f7f7f7f7f7f7fL;
 			initR[sq] |= (bb << 1) & 0xfefefefefefefefeL;
 			initR[sq] |= (bb << 8);
 		}
 	}
 
 	public static void makeMaskR()
 	{
 		int sq;
 		for (sq = 0; sq < 64; ++sq)
 		{
 			maskR[sq] = 0;
 			int i;
 			for ( i = sq - 8; i >= 0; i -= 8 )
 				maskR[sq] |= 1L << i;
 			for ( i = sq - 1; i >= 0 && (i & 7) != 7; --i )
 				maskR[sq] |= 1L << i;
 			for ( i = sq + 1; i < 64 && (i & 7) != 0; ++i )
 				maskR[sq] |= 1L << i;
 			for ( i = sq + 8; i < 64; i += 8 )
 				maskR[sq] |= 1L << i;
 		}
 	}
 
 	public static void makeRankR()
 	{
 		for ( int sq = 0; sq < 8; ++sq )
 		{
 			for ( int i = 0; i < 128; i += 2 )
 			{
 				int rr = 0;
 				int j;
 				for ( j = sq - 1; j >= 0; --j )
 				{
 					rr |= (1L << j);
 					if ( (i & (1L << j)) == 0 )  // the 1 bits are the free squares
 						break;
 				}
 				for ( j = sq + 1; j < 8; ++j )
 				{
 					rr |= (1L << j);
 					if ( (i & (1L << j)) == 0 )  // the 1 bits are the free squares
 						break;
 				}
 				rankR[sq][i  ] = rr;
 				rankR[sq][i+1] = rr;
 			}
 		}
 	}
 
 	public static long rookAttacks(int sq, long free)
 	{
 		long msk = maskR[sq];   // The mask kills scattered bits
 		long bb  = initR[sq];   // This drives the expansion/explosion. Here is the start.
 		long at  = bb;          // Collecting the resulting attacks
 		long cl  = at;          // Clears some intermediate overflows
 		long ov;                // A nasty overflow bit when 4 directions in first step are possible.
 		
 		bb &= free;
 		int todo = 5;
 		switch (Definitions.repsR[sq])
 		{
 		case 8:
 			bb >>>= 8; bb *= 0x00010081L; bb &= msk; at |= bb; bb &= free;
 			bb &= ~cl;
 			break;
 					case 7:
 						bb >>>= 8; bb *= 0x00010201L; bb &= msk; at |= bb; bb &= free;
 						bb &= ~cl;
 						break;
 				case 6:
 					bb >>>= 8; bb *= 0x00010281L;
 					//bb &= ~1L << (sq - 6);      // Clears for b-squares a nasty overflow from south-west-first-step to h file
 					bb &= msk; at |= bb; bb &= free;
 					bb &= ~cl;
 					break;
 					case 5:
 						cl = at;
 						bb >>>= 8; bb *= 0x00010281L;
 						ov = 1L << (sq + 3);
 						bb |= (bb & ov) >>> 1;
 						bb &= ~ov;
 						//bb &= ~1L << (sq - 6);      // Clears for b-squares a nasty overflow from south-west-first-step to h file
 						bb &= msk; at |= bb; bb &= free;
 						bb &= ~cl;
 						todo = 4;
 						break;
 					case 4:
 						cl = at;
 						bb >>>= 8; bb *= 0x00010281L;
 						ov = 1L << (sq + 3);
 						bb |= (bb & ov) >> 1;
 						bb &= ~ov;
 						bb &= msk; at |= bb; bb &= free;
 						bb &= ~cl;
 						todo = 3;
 						break;
 					case 3:
 						cl = at;
 						bb >>>= 8; bb *= 0x00010281L;
 						ov = 1L << (sq + 3);
 						bb |= (bb & ov) >>> 1;
 						bb &= ~ov;
 						bb &= msk; at |= bb; bb &= free;
 						bb &= ~cl;
 						todo = 2;
 						break;
 		default:
 			break;
 		}
 
 		switch (todo)
 		{
 		case 5:
 			cl = at;
 			bb >>>= 8; bb *= 0x00010281L; bb &= msk; at |= bb; bb &= free;
 			bb &= ~cl;
 		case 4:
 			cl = at;
 			bb >>>= 8; bb *= 0x00010281L; bb &= msk; at |= bb; bb &= free;
 			bb &= ~cl;
 			case 3:
 				cl = at;
 				bb >>>= 8; bb *= 0x00010281L; bb &= msk; at |= bb; bb &= free;
 				bb &= ~cl;
 			case 2:
 				cl = at;
 				bb >>>= 8; bb *= 0x00010281L; bb &= msk; at |= bb; bb &= free;
 				bb &= ~cl;
 				bb >>>= 8; bb *= 0x00010281L; bb &= msk; at |= bb;
 				break;
 		}
 		if (sq < 8) // or do this before the switch
 		{
 			byte a1a8 = (byte) free;
 			at |= rankR[sq][a1a8 & 0x7e];
 		}
 		return at;
 	}
 
 	public static long rookAttacks(long rooks, long free)
 	{
 		long att = 0;
 		for (int i = 0; i < 64; i++)
 		{
 			if (((rooks >>> i) & 1) != 0)
 			{
 				att |= rookAttacks(i, free);
 			}
 		}
 		return att;
 	}
 	
 	public static long queenAttacks(int sq, long free)
 	{
 		return rookAttacks(sq, free) | bishopAttacks(sq, free);
 	}
 
 	public static long queenAttacks(long queens, long free)
 	{
 		long att = 0;
 		for (int i = 0; i < 64; i++)
 		{
 			if (((queens >>> i) & 1) != 0)
 			{
 				att |= queenAttacks(i, free);
 			}
 		}
 		return att;	
 	}
 	
 	public static long knightAttacks(long knights) {
 		long l1 = (knights >>> 1) & 0x7f7f7f7f7f7f7f7fL;
 		long l2 = (knights >>> 2) & 0x3f3f3f3f3f3f3f3fL;
 		long r1 = (knights << 1) & 0xfefefefefefefefeL;
 		long r2 = (knights << 2) & 0xfcfcfcfcfcfcfcfcL;
 		long h1 = l1 | r1;
 		long h2 = l2 | r2;
 		return (h1<<16) | (h1>>>16) | (h2<<8) | (h2>>>8);
 	}
 
 	public static long kingAttacks(long kings) {
 		long attacks = ((kings >>> 1) & ~allA) | ((kings << 1) & ~allH);
 		kings |= attacks;
 		attacks |= (kings << 8) | (kings >>> 8);
 		return attacks;
 	}
 
 	private static long wSinglePushTargets(long wpawns, long empty) {
 		return (wpawns << 8) & empty;
 	}
 	private static long wDblPushTargets(long wpawns, long empty) {
 		long rank4 = 0x00000000FF000000L;
 		long singlePushs = wSinglePushTargets(wpawns, empty);
 		return (singlePushs << 8) & empty & rank4;
 	}
 	private static long bSinglePushTargets(long bpawns, long empty) {
 		return (bpawns >>> 8) & empty;
 	}
 	private static long bDblPushTargets(long bpawns, long empty) {
 		long rank5 = 0x000000FF00000000L;
 		long singlePushs = bSinglePushTargets(bpawns, empty);
 		return (singlePushs >>> 8) & empty & rank5;
 	}
 	private static long wPawnsAble2Push(long wpawns, long empty) {
 		return (empty >>> 8) & wpawns;
 	}
 	private static long wPawnsAble2DblPush(long wpawns, long empty) {
 		long rank4 = 0x00000000FF000000L;
 		long emptyRank3 = ((empty & rank4) >>> 8) & empty;
 		return wPawnsAble2Push(wpawns, emptyRank3);
 	}
 	private static long bPawnsAble2Push(long bpawns, long empty) {
 		return (empty << 8) & bpawns;
 	}
 	private static long bPawnsAble2DblPush(long bpawns, long empty) {
 		long rank5 = 0x000000FF00000000L;
 		long emptyRank6 = ((empty & rank5) << 8) & empty;
 		return bPawnsAble2Push(bpawns, emptyRank6);
 	}
 
 	public static long wpawnAttacks(long wpawns)
 	{
 		long leftcapts = ((wpawns << 9) & ~allH);
 		long rightcapts = ((wpawns << 7) & ~allA);
 		return leftcapts | rightcapts;
 	}
 
 	public static long wpawnMoves(long wpawns, long empty)
 	{
 		return (wSinglePushTargets(wpawns, empty) | wDblPushTargets(wPawnsAble2DblPush(wpawns, empty), empty));
 	}
 
 	public static long bpawnAttacks(long bpawns)
 	{
 		long leftcapts = ((bpawns >>> 7) & ~allH);
 		long rightcapts = ((bpawns >>> 9) & ~allA);
 		return leftcapts | rightcapts;
 	}
 
 	public static long bpawnMoves(long bpawns, long empty)
 	{
 		return (bSinglePushTargets(bpawns, empty) | bDblPushTargets(bPawnsAble2DblPush(bpawns, empty), empty));
 	}
 
 	public static boolean isAttacked(int square, Color color, long pieces, long pawns, 
 			long knights, long bishops, long rooks, long queens, long kings) //color is the color of piece doing the attacking
 	{
 		if (square < 0 || square > 63)
 			return false; //can't attack a square outside the board
 		
 		if (color == Color.WHITE)
 		{
 			if (((wpawnAttacks(pawns) >>> square) & 1) == 1)
 				return true;
 		}
 		else
 		{
 			if (((bpawnAttacks(pawns) >>> square) & 1) == 1)
 				return true;
 		}		
 		if (((knightAttacks(knights) >>> square) & 1) == 1)
 			return true;
 		if (((kingAttacks(kings) >>> square) & 1) == 1)
 			return true;
 		if ((bishopAttacks(square, ~pieces) & (bishops | queens)) > 0)
 			return true;
 		if ((rookAttacks(square, ~pieces) & (rooks | queens)) > 0)
 			return true;
 		return false;
 	}
 
 	public static boolean isAttacked(StandardChessBoard scb, int square, Color color)
 	{
 		long colorpieces = scb.m_white;
 		if (color == Color.BLACK)
 			colorpieces = scb.m_black;
 		return isAttacked(square, color, scb.m_white | scb.m_black, colorpieces & scb.m_pawns, colorpieces & scb.m_knights,
 				colorpieces & scb.m_bishops, colorpieces & scb.m_rooks, colorpieces & scb.m_queens, colorpieces & scb.m_kings);
 	}
 
 	public static Color flip(Color c)
 	{
 		if (c == Color.WHITE)
 		{
 			return Color.BLACK;
 		}
 		return Color.WHITE;
 	}
 }
 
