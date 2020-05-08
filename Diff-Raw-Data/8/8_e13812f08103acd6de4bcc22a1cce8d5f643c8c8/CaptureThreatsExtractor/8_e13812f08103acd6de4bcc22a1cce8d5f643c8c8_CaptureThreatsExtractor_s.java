 package feature_extractor;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 
 import ai_util.Util;
 import arimaa3.ArimaaMove;
 import arimaa3.GameState;
 import arimaa3.GenCaptures;
 import arimaa3.MoveList;
 
 //TODO: Make this class as efficient as possible--I can totally see this being a bottleneck.
 public class CaptureThreatsExtractor extends AbstractExtractor {
 
 	/** Used in bit manipulations -- the size of a byte is 8 */
 	private static final int SIZE_OF_BYTE = 8;
 	
 	private GameState prev, curr;
 	
 	/* Excerpt from Wu on Capture Threats
 	 * ----------------------------------
 	 * Threatening and defending the capture of a piece plays a key role in many tactics.
 	 *		THREATENS CAP(type,s,trap): Threatens capture of opposing piece of type type (0-7)
 	 *			in s (1-4) steps , in trap trap (0-3). (128 features)
 	 *		INVITES CAP MOVED(type,s,trap): Moves own piece of type type (0-7) so that it can be
 	 *		 	captured by opponent in s (1-4) steps , in trap trap (0-3). (128 features)
 	 *		INVITES CAP UNMOVED(type,s,trap): Own piece of type type (0-7) can now be captured by
 	 *			opponent in s (1-4) steps, in trap trap (0-3), but it was not itself moved. 
 	 *			(128 features)
 	 *		PREVENTS CAP(type,loc): Removes the threat of capture from own piece of type type (0-7)
 	 *			at location loc (0-31). (256 features)
 	 *			
 	 *	The way in which one defends the capture of a piece is also important.
 	 *		CAP DEF ELE(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap 
 	 *			trap (0-3) by using the elephant as a trap defender. (16 features)
 	 *		CAP DEF OTHER(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
 	 *			trap (0-3) by using a non-elephant piece as a trap defender. (16 features)
 	 *		CAP DEF RUNAWAY(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
 	 *			trap (0-3) by making the threatened piece run away. (16 features)
 	 *		CAP DEF INTERFERE(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
 	 *			trap (0-3) by freezing or blocking the threatening piece. (16 features)
 	 */
 	
 	/** Extracts Capture Threats information, using prev and curr 
 	 * @param prev The previous game state (leading to curr)
 	 * @param curr The current game state to be analyzed */
 	public CaptureThreatsExtractor(GameState prev, GameState curr) {
 		this.prev = prev;
 		this.curr = curr;
 	}
 	
 	
 	/** The portion of the BitSet that will be updated is CAPTURE_THREATS_END - CAPTURE_THREATS_START
 	 *  + 1 (== 704) bits. These bits will be split up into categories according to the constants
 	 *  in `FeatureConstants.CaptureThreats`.
 	 *  */
 	@Override
 	public void updateBitSet(BitSet bitset) {
 		//might need to have some more information passed around later on...
 		threatensCap(bitset); 
 	}
 
 	@Override
 	public int startOfRange() {
 		return FeatureRange.CAPTURE_THREATS_START;
 	}
 
 	
 	@Override
 	public int endOfRange() {
 		return FeatureRange.CAPTURE_THREATS_END;
 	}
 	
 	/** Overloaded method-wrapper to give default bitset offset
 	 * (See other method below for more comments). */
 	private void threatensCap(BitSet bitset) {
 		threatensCap(curr, bitset, CaptureThreats.THREATENS_CAP_OFFSET);
 	}
 	
 	/** Updates the bitset with the THREATENS CAP features 
 	 * @param curr is the GameState after the move is made (will be used to determine threats) 
 	 * @param bitset is the bitset to be updated (as per recordMove)
 	 * @param offset is the offset into the bit-vector where the bits will be updated <br>
 	 *   <i>[Added mainly to support using this method later to update small bitsets (offset 0)
 	 *   to pass information to other methods.]</i>*/
 	private void threatensCap(GameState curr, BitSet bitset, int offset) {
 		int jeffBachersSize = 400000; //TODO: Figure out what the maximum size can be and use instead... (definitely way less than 400,000?)
 		boolean completeTurn = false; //allows us to get captures with fewer than 4 moves
 		MoveList moveList = new MoveList(jeffBachersSize);
 		
 		GenCaptures captures = new GenCaptures();
 		
 		/* Play a pass-move for the opponent in order to see threats made by original move 
 		 * (we need to check possible captures for the player who just played) */
 		int opponent = curr.player;
 		GameState currOppPass = new GameState();
 		currOppPass.playPASS(curr); //copies curr into currPassOpp before playing pass
 		captures.genCaptures(currOppPass, moveList, completeTurn); //fills moveList with capture threats
 		
 		/* We want types before a potential capture (we're "threatening" to capture these types);
 		 * do it once outside the loop and pass in */
 		byte[] oppPieceTypes = SteppingOnTrapsExtractor.getPieceTypesForPlayer(
 									opponent, calculatePieceTypes(curr)
 								); 
 		
 		for (ArimaaMove move : moveList) {//seems to avoid null moves...
 			GameState postCapture = new GameState();
 			postCapture.playFullClear(move, currOppPass);
 			recordCaptureMove(curr, postCapture, move.steps, bitset, offset, opponent, oppPieceTypes);
 		}
 	}
 	
 	 /** The bitset is updated as follows: <br>
 	 *  ->For each trap, for each step, the piece type. (32 * trap + 8 * step + type) <br> 
 	 *  -><i>[32 is the number of bits taken by all (step,type) combinations. 8 is the number</i>
 	 *  of bits taken by all types.] 
 	 *  @param preCapture the GameState before any capture (the "threatening, original" game state)
 	 *  @param postCapture the GameState after a capturing move has been played
 	 *  @param numSteps the number of steps in the move from pre to postCapture
 	 *  @param toUpdate the BitSet that will be updated (as above)
 	 *  @param bitOffset the offset into the BitSet relative to which point bits will be set
 	 *  @param opponent PL_WHITE or PL_BLACK (the opponent, whose piece are potentially captured)
 	 *  @param oppPieceTypes the Wu-piece-type array of <i>opponent</i> piece types
 	 *  */
 	/* Sorry for all of these parameters...but I didn't want to make assumptions and mess up
 	 * any recalculations of parameters, etc. 
 	 * Also, pardon the cryptic bit manipulations... */
 	//TODO: Suggest better bit manipulations/information passing
 	//TODO: figure out which trap resulted in the capture... (using trap 2 currently)
 	private void recordCaptureMove(GameState preCapture, GameState postCapture, int numSteps,
 						BitSet toUpdate, int bitOffset, int opponent, byte[] oppPieceTypes) { 
 		short captured = getCapturesFromStates(preCapture, postCapture,	opponent, oppPieceTypes);
 		
 		while (captured != 0) { //keep "popping" most-significant ones until there aren't any left.			
 			int type = Util.FirstOne(captured);
 			captured ^= (1 << type); //turn off the type-bit just considered (faster than & ~?)
 			int trap = getTrapNumber(preCapture, postCapture); //2
 			
 			if (type >= SIZE_OF_BYTE) type -= SIZE_OF_BYTE; // 0 <= type < SIZE_OF_BYTE (8)
 			int bitToSet = bitOffset + 32 * trap + 8 * (numSteps - 1) + type; //TODO: remove hardcoding
 			toUpdate.set(bitToSet);
 		}
 		
 	}
 	
 	
 	/** @return a short containing information as follows:
 	 * The least-significant byte has 1s at the positions of captured types <br>
 	 * e.g. if a piece of "Type 2: Non-rabbit, 2 opponent pieces stronger" is captured,
 	 * then 1 << 2 (the 3rd bit) is set.<br>
 	 * The most-significant byte has the same information but is only set if
 	 * there are two captures of the same piece-type (impossible to have more than
 	 * 2 captures)
 	 * */
 	private short getCapturesFromStates(GameState preCapture, GameState postCapture,
 													int opponent, byte[] oppPieceTypes) {
 		short captures = 0;
		
		//TODO: Assumption: the piece_bbs are visited in what order? OH MY GOD WHY IS THIS SO HARD :(
		for (int i = opponent; i < oppPieceTypes.length; i++) {
 			int preCaptureCount = Util.PopCnt(preCapture.piece_bb[i]);
 			int postCaptureCount = Util.PopCnt(postCapture.piece_bb[i]);
 			int numCaptures = preCaptureCount - postCaptureCount; //numCaptures >= 0
 			
 			int pieceType = oppPieceTypes[i / 2]; //TODO: use all pieceTypes instead of just opp?
 			
 			switch (numCaptures) {
 				case 0:
 					break;
 				case 2:
 					captures |= (1 << (pieceType + SIZE_OF_BYTE)); 
 					//fall through
 				case 1:
 					captures |= (1 << pieceType);
 					break;
 			}
 		}
 		
 		return captures;
 	}
 	
 	/** Deduces the trap number used in capturing ... this seems impossible */
 	private int getTrapNumber(GameState preCapture, GameState postCapture) {
 		return 2;
 	}
 	
 	// Calculates the piece type (e.g. 3) for each piece id (e.g. black dog) for the current game state.
 	//TODO: Merge w/ FeatureExtractor's method
 	private byte[] calculatePieceTypes(GameState curr){
 		byte[] pieceTypes = new byte[curr.piece_bb.length];
 		
 		for (int i = 0; i < 2; i++){ // calculate for rabbits 
 			byte numStronger = FeatureExtractor.countOneBits(curr.stronger_enemy_bb[i]);
 			if (numStronger < 5)
 				pieceTypes[i] = 7;
 			else if (numStronger < 7)
 				pieceTypes[i] = 6;
 			else
 				pieceTypes[i] = 5;
 		}
 		
 		for (int i = 2; i < 12; i++){ // calculate for non-rabbits
 			byte numStronger = FeatureExtractor.countOneBits(curr.stronger_enemy_bb[i]);
 			switch (numStronger) {
 				case 0: pieceTypes[i] = 0; break;
 				case 1: pieceTypes[i] = 1; break;
 				case 2: pieceTypes[i] = 2; break;
 				case 3: case 4: pieceTypes[i] = 3; break;
 				default: pieceTypes[i] = 4; break;
 			}
 		}
 		
 		return pieceTypes;
 	}
 	
 
 
 
 
 
 	// -------------- "INEFFICIENT" but trying to get this to work --------------- //
 	private void recordCaptureMoveInefficient(GameState preCapture, GameState postCapture, 
 			int numSteps, BitSet toUpdate, int bitOffset, int opponent, byte[] oppPieceTypes) {
 		
 		final int MAX_NUM_CAPTURES = 2;
 		ArrayList<Integer> trapsUsed = new ArrayList<Integer>(MAX_NUM_CAPTURES); //updated in place
 		ArrayList<Integer> captured = new ArrayList<Integer>(MAX_NUM_CAPTURES); //updated in place
 		
 		getCapturesFromStatesInefficient(preCapture, postCapture, 
 					opponent, oppPieceTypes, captured, trapsUsed);
 		assert(trapsUsed.size() == captured.size());
 		
 		for (int i = 0; i < captured.size(); i++) {			
 			int type = captured.get(i);
 			int trap = trapsUsed.get(i);
 			
 			int bitToSet = bitOffset + 32 * trap + 8 * (numSteps - 1) + type; //TODO: remove hardcoding
 			toUpdate.set(bitToSet);
 		}
 	}
 	
 	private void getCapturesFromStatesInefficient(GameState preCapture, GameState postCapture, 
 			int opponent, byte[] oppPieceTypes, ArrayList<Integer> captured, 
 												ArrayList<Integer> trapsUsed) {
 		
		for (int piece = opponent; piece < oppPieceTypes.length; piece++) {
 			int preCaptureCount = Util.PopCnt(preCapture.piece_bb[piece]);
 			int postCaptureCount = Util.PopCnt(postCapture.piece_bb[piece]);
 			int numCaptures = preCaptureCount - postCaptureCount; //numCaptures >= 0
 			
 			int pieceType = oppPieceTypes[piece / 2]; //TODO: use all pieceTypes instead of just opp?
 			
 			if (numCaptures != 0) {
 				for (int capt = 0; capt < numCaptures; capt++)
 					captured.add(pieceType);
 				
 				updateTrapsUsedInefficient(preCapture, postCapture, piece, trapsUsed);
 			}
 		}
 	}
 	
 	private void updateTrapsUsedInefficient(GameState preCapture, GameState postCapture,
 												int piece, ArrayList<Integer> trapsUsed) {
 		
 	}
 
 }
 
 
 // Garbage / stuff that may come in handy later
 
 // -- PRINT captured moves in a MoveList
 //System.out.println("Capture Moves: ");
 //System.out.println("Initial board: \n" + currPassOpp.toBoardString() + "\n");
 //for (ArimaaMove move : moveList) {
 //	if (move == null) continue;
 //	
 //	GameState currCopy = new GameState();
 //	currCopy.playFullClear(move, currPassOpp);
 //	
 //	System.out.println(move.toString());
 //	System.out.println(currCopy.toBoardString());
 //}
 //System.out.println();
