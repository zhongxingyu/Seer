 package sf.pnr.base;
 
 import junit.framework.TestCase;
 import sf.pnr.alg.PawnHashTable;
 
 import static sf.pnr.base.Utils.*;
 import static sf.pnr.base.Evaluation.*;
 
 /**
  */
 public class EvaluationTest extends TestCase {
 
     private Evaluation eval;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         eval = new Evaluation();
     }
 
     @Override
     protected void tearDown() throws Exception {
         eval = null;
         super.tearDown();
     }
 
     public void testDrawByInsufficientMaterialKnights() {
         final Board board = StringUtils.fromFen("4k3/3nn3/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertFalse(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialBishopsOfOppositeColors() {
         final Board board = StringUtils.fromFen("7k/7B/7K/8/7B/8/8/8 w - - 0 1");
         assertFalse(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialBishopsOfOppositeColors2() {
         final Board board = StringUtils.fromFen("4bb2/4k3/4B3/3K4/8/8/8/4B3 w - - 0 2");
         assertFalse(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testUnstoppablePawn() {
         final Board board = StringUtils.fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
         final int score = eval.evaluate(board);
         assertTrue("Score: " + score, score > VAL_QUEEN - 200);
         assertTrue("Score: " + score, score < VAL_QUEEN + 200);
     }
 
     public void testPawnEvalBasic() {
         final Board board = new Board();
         board.clear();
         assertEquals(0, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
 
         eval = new Evaluation();
         board.restart();
         assertEquals(0, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPawnEvalDoublePawns() {
         final Board board = StringUtils.fromFen("8/p7/K7/p3p2p/k3p2P/4N3/PP4P1/8 w - - 0 1");
         final int stage = board.getStage();
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         final int positionalBonusWhite = getPositionalBonusPawn(A[1], shiftWhite, stage) +
             getPositionalBonusPawn(B[1], shiftWhite, stage) + getPositionalBonusPawn(G[1], shiftWhite, stage) +
             getPositionalBonusPawn(H[3], shiftWhite, stage);
         final int positionalBonusBlack = getPositionalBonusPawn(A[4], shiftBlack, stage) +
             getPositionalBonusPawn(A[6], shiftBlack, stage) + getPositionalBonusPawn(E[3], shiftBlack, stage) +
             getPositionalBonusPawn(E[4], shiftBlack, stage) + getPositionalBonusPawn(H[4], shiftBlack, stage) +
             BONUS_PASSED_PAWN_PER_SQUARE * 3;
         assertEquals(positionalBonusWhite - positionalBonusBlack - 2 * PENALTY_DOUBLE_PAWN - 3 * PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPawnEvalBackwardsPawns() {
         final Board board = StringUtils.fromFen("8/Qp4pk/p5b1/5p1p/3B2nP/1P4PK/P1P1r1B1/3r4 b - - 0 1");
         assertEquals(30, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPositionalValuePawn() {
         final Board board = new Board();
         board.restart();
         assertEquals(0, computePositionalBonusNoPawnAsWhite(board));
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         assertEquals(VAL_POSITION_BONUS_PAWN[E[3] + shiftWhite] - VAL_POSITION_BONUS_PAWN[E[1] + shiftWhite],
             computePositionalGain(PAWN, E[1], E[3], board.getStage(), shiftWhite));
         board.move(StringUtils.fromSimple("e2e4"));
         assertEquals(0, computePositionalBonusNoPawnAsWhite(board));
         assertEquals(-(VAL_POSITION_BONUS_PAWN[E[6] + shiftBlack] - VAL_POSITION_BONUS_PAWN[E[4] + shiftBlack]),
             computePositionalGain(PAWN, E[6], E[4], board.getStage(), shiftBlack));
         board.move(StringUtils.fromSimple("e7e5"));
         assertEquals(0, computePositionalBonusNoPawnAsWhite(board));
     }
 
     public void testIsolatedPawnOnFileA() {
         final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/P7/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         final int positionalBonusWhite = getPositionalBonusPawn(A[2], SHIFT_POSITION_BONUS[WHITE], stage);
         int positionalBonusBlack = 0;
         for (int i = 0; i < 8; i++) {
             positionalBonusBlack += getPositionalBonusPawn(getPosition(i, 6), SHIFT_POSITION_BONUS[BLACK], stage);
         }
         assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testIsolatedPawnOnFileB() {
         final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/1P6/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         final int positionalBonusWhite = getPositionalBonusPawn(B[2], SHIFT_POSITION_BONUS[WHITE], stage);
         int positionalBonusBlack = 0;
         for (int i = 0; i < 8; i++) {
             positionalBonusBlack += getPositionalBonusPawn(getPosition(i, 6), SHIFT_POSITION_BONUS[BLACK], stage);
         }
         assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testIsolatedPawnOnFileH() {
         final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/7P/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         final int positionalBonusWhite = getPositionalBonusPawn(H[2], SHIFT_POSITION_BONUS[WHITE], stage);
         int positionalBonusBlack = 0;
         for (int i = 0; i < 8; i++) {
             positionalBonusBlack += getPositionalBonusPawn(getPosition(i, 6), SHIFT_POSITION_BONUS[BLACK], stage);
         }
         assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPassedPawnOnFileA() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/P7/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         assertEquals(getPositionalBonusPawn(A[2], SHIFT_POSITION_BONUS[WHITE], stage) + BONUS_PASSED_PAWN_PER_SQUARE +
             PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPassedPawnOnFileB() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/1P6/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         assertEquals(getPositionalBonusPawn(B[2], SHIFT_POSITION_BONUS[WHITE], stage) + BONUS_PASSED_PAWN_PER_SQUARE +
             PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPassedPawnOnB4() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/1P6/8/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         assertEquals(getPositionalBonusPawn(B[3], SHIFT_POSITION_BONUS[WHITE], stage) + BONUS_PASSED_PAWN_PER_SQUARE * 2 +
             PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPassedPawnOnFileH() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/7P/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         assertEquals(getPositionalBonusPawn(H[2], SHIFT_POSITION_BONUS[WHITE], stage) + BONUS_PASSED_PAWN_PER_SQUARE +
             PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPromotionDistanceStoppableWhite() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/1P6/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceStoppable2White() {
         final Board board = StringUtils.fromFen("4k3/8/8/1P6/8/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceUnstoppableWhite() {
         final Board board = StringUtils.fromFen("4k3/8/1P6/8/8/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(2, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, BLACK_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceUnstoppable2White() {
         final Board board = StringUtils.fromFen("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(1, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(1, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, BLACK_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceStoppableBlack() {
         final Board board = StringUtils.fromFen("4k3/8/1p6/8/8/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceStoppable2Black() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/1p6/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceUnstoppableBlack() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/1p6/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(2, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceUnstoppable2Black() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/1p6/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(1, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(1, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPositionalBonus() {
         final Board board = StringUtils.fromFen("8/5p2/3p1kp1/PR2b2p/r7/8/1P6/1K5B w - - 2 45");
         final int stage = board.getStage();
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         final int openingScore =
             VAL_POSITION_BONUS_ROOK[B[4] + shiftWhite] - VAL_POSITION_BONUS_ROOK[A[3] + shiftBlack] +
             VAL_POSITION_BONUS_BISHOP[H[0] + shiftWhite] - VAL_POSITION_BONUS_BISHOP[E[4] + shiftBlack] +
             VAL_POSITION_BONUS_KING[B[0] + shiftWhite] - VAL_POSITION_BONUS_KING[F[5] + shiftBlack];
         final int endGameScore =
             VAL_POSITION_BONUS_ROOK_ENDGAME[B[4] + shiftWhite] - VAL_POSITION_BONUS_ROOK_ENDGAME[A[3] + shiftBlack] +
             VAL_POSITION_BONUS_BISHOP[H[0] + shiftWhite] - VAL_POSITION_BONUS_BISHOP[E[4] + shiftBlack] +
             VAL_POSITION_BONUS_KING_ENDGAME[B[0] + shiftWhite] - VAL_POSITION_BONUS_KING_ENDGAME[F[5] + shiftBlack];
         final int bonus = (openingScore * (STAGE_MAX - stage) + endGameScore * stage) / STAGE_MAX;
         assertEquals(bonus, computePositionalBonusNoPawnAsWhite(board));
     }
 
     public void testPositionalBonusQueens() {
         final Board board = StringUtils.fromFen("1q4k1/8/8/2Q5/8/8/8/2K5 w - - 0 1");
         final int stage = board.getStage();
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         final int openingScore =
             VAL_POSITION_BONUS_QUEEN[C[4] + shiftWhite] - VAL_POSITION_BONUS_QUEEN[B[7] + shiftBlack] +
             VAL_POSITION_BONUS_KING[C[0] + shiftWhite] - VAL_POSITION_BONUS_KING[G[7] + shiftBlack];
         final int endGameScore =
             VAL_POSITION_BONUS_QUEEN_ENDGAME[C[4] + shiftWhite] - VAL_POSITION_BONUS_QUEEN_ENDGAME[B[7] + shiftBlack] +
             VAL_POSITION_BONUS_KING_ENDGAME[C[0] + shiftWhite] - VAL_POSITION_BONUS_KING_ENDGAME[G[7] + shiftBlack];
         final int bonus = (openingScore * (STAGE_MAX - stage) + endGameScore * stage) / STAGE_MAX;
         assertEquals(bonus, computePositionalBonusNoPawnAsWhite(board));
     }
 
     public void testPositionalBonus2() {
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         final int[] pawnEndGame = Evaluation.VAL_POSITION_BONUS_PAWN_ENDGAME;
         assertTrue(pawnEndGame[A[6] + shiftWhite] >= pawnEndGame[A[1] + shiftWhite]);
         assertTrue(pawnEndGame[A[6] + shiftBlack] <= pawnEndGame[A[1] + shiftBlack]);
     }
 
     public void testPositionalBonusSymmetry() throws NoSuchFieldException, IllegalAccessException {
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         for (int[] arr: Evaluation.VAL_POSITION_BONUS_OPENING) {
             assertEquals(128, arr.length);
             for (int rank = 0; rank < 8; rank++) {
                 for (int file = 0; file < 8; file++) {
                     final int whitePos = Utils.getPosition(file, rank);
                     final int blackPos = Utils.getPosition(file, 7 - rank);
                     assertEquals(StringUtils.toString0x88(whitePos), arr[whitePos + shiftWhite], arr[blackPos + shiftBlack]);
                 }
             }
         }
         for (int[] arr: Evaluation.VAL_POSITION_BONUS_ENDGAME) {
             assertEquals(128, arr.length);
             for (int rank = 0; rank < 8; rank++) {
                 for (int file = 0; file < 8; file++) {
                     final int whitePos = Utils.getPosition(file, rank);
                     final int blackPos = Utils.getPosition(file, 7 - rank);
                     assertEquals(StringUtils.toString0x88(whitePos), arr[whitePos + shiftWhite], arr[blackPos + shiftBlack]);
                 }
             }
         }
     }
 
     public void testPawnMirrorEvalPawnStorm() {
         final Board boardWhite = StringUtils.fromFen("1k6/8/1P6/P1P5/8/8/8/6K1 w - - 0 1");
         final int pawnHashValueWhite = eval.pawnEval(boardWhite);
         final int pawnValueWhite = PawnHashTable.getValueFromPawnHashValue(pawnHashValueWhite);
         final int valueWhite = eval.evaluate(boardWhite);
 
         final Board boardBlack = StringUtils.fromFen("1k6/8/8/8/5p1p/6p1/8/6K1 b - - 0 1");
         final int pawnHashValueBlack = eval.pawnEval(boardBlack);
         final int pawnValueBlack = PawnHashTable.getValueFromPawnHashValue(pawnHashValueBlack);
         final int valueBlack = eval.evaluate(boardBlack);
 
         assertEquals(pawnValueWhite, -pawnValueBlack);
         assertEquals(valueWhite, valueBlack);
     }
 
     public void testPawnMirrorEvalHalfInitial() {
         final Board boardWhite = StringUtils.fromFen("5k2/8/8/8/8/8/PPPPPPPP/RNBKQBNR w KQ - 0 1");
         final int pawnHashValueWhite = eval.pawnEval(boardWhite);
         final int pawnValueWhite = PawnHashTable.getValueFromPawnHashValue(pawnHashValueWhite);
         final int valueWhite = eval.evaluate(boardWhite);
 
         final Board boardBlack = StringUtils.fromFen("rnbqkbnr/pppppppp/8/8/8/8/8/2K5 b kq - 0 1 ");
         final int pawnHashValueBlack = eval.pawnEval(boardBlack);
         final int pawnValueBlack = PawnHashTable.getValueFromPawnHashValue(pawnHashValueBlack);
         final int valueBlack = eval.evaluate(boardBlack);
 
         assertEquals(pawnValueWhite, -pawnValueBlack);
         assertEquals(valueWhite, valueBlack);
     }
 
     private static int getPositionalBonusPawn(final int position, final int shift, final int stage) {
         final int openingBonus = VAL_POSITION_BONUS_PAWN[position + shift] * (STAGE_MAX - stage);
         final int endgameBonus = VAL_POSITION_BONUS_PAWN_ENDGAME[position + shift] * stage;
         return (openingBonus + endgameBonus) / STAGE_MAX;
     }
 
     public void testCastlingPenalty() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/8/4K2R w K - 1 1");
         assertEquals(PENALTY_CASTLING_PENDING - PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
         board.move(StringUtils.fromSimple("e1g1") | MT_CASTLING_KINGSIDE);
         assertEquals(-PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
     }
 
     public void testMobility() {
         final Board board = StringUtils.fromFen("8/1R2K2k/8/8/8/8/8/8 w - - 0 1");
         final int score = Evaluation.computeMobilityBonusAsWhite(board);
        assertEquals((10 + 8 - 5) * BONUS_MOBILITY + BONUS_DEFENSE + BONUS_DISTANCE_ROOK[1], score);
     }
 
     public void testMobilityBonusArraySizes() {
         assertEquals(8 + 1, BONUS_MOBILITY_KNIGHT.length);
         assertEquals(13 + 1, BONUS_MOBILITY_BISHOP.length);
         assertEquals(14 + 1, BONUS_MOBILITY_ROOK.length);
         assertEquals(27 + 1, BONUS_MOBILITY_QUEEN.length);
         assertEquals(8 + 1, BONUS_MOBILITY_KING.length);
     }
 
     public void testMobilityKnight() {
         final Board board = StringUtils.fromFen("8/p3pk2/2N5/8/1P6/1K6/8/8 w - - 0 1");
         final int[] distance = new int[1];
         final int score = Evaluation.computeMobilityBonusKnight(board, WHITE, distance);
        assertEquals(5 * BONUS_MOBILITY + BONUS_DEFENSE + 2 * BONUS_ATTACK, score);
         assertEquals(BONUS_DISTANCE_KNIGHT[2], distance[0]);
     }
 
     public void testMobilityPawn() {
         final Board board = StringUtils.fromFen("8/p3p3/2N1kB2/1P6/8/1K6/8/8 w - - 1 1");
         final int score = Evaluation.computeMobilityBonusPawnAsWhite(board);
         assertEquals((BONUS_MOBILITY + BONUS_DEFENSE) - (2 * BONUS_MOBILITY + BONUS_ATTACK) - BONUS_HUNG_PIECE, score);
     }
 
     public void testMobilityPawn2() {
         final Board board = StringUtils.fromFen("4k3/8/8/3R1r2/2P1P1P1/8/8/4K3 w - - 1 1");
         final int score = Evaluation.computeMobilityBonusPawnAsWhite(board);
         assertEquals(3 * BONUS_MOBILITY + 2 * BONUS_DEFENSE + 2 * BONUS_ATTACK + BONUS_HUNG_PIECE, score);
     }
 
     public void testMobilityPawnBlockedDoubleWhite() {
         final Board board = StringUtils.fromFen("4k3/8/8/5Pp1/3P4/2P5/1PPP4/4K3 w - - 1 1");
         final int score = Evaluation.computeMobilityBonusPawnAsWhite(board);
         assertEquals(6 * BONUS_MOBILITY + 3 * BONUS_DEFENSE - BONUS_MOBILITY, score);
     }
 
     public void testMobilityPawnBlockedDoubleBlack() {
         final Board board = StringUtils.fromFen("4k3/1ppp4/2p5/3p1Pp1/8/8/8/4K3 b - - 1 1");
         final int score = Evaluation.computeMobilityBonusPawnAsWhite(board);
         assertEquals(BONUS_MOBILITY - (6 * BONUS_MOBILITY + 3 * BONUS_DEFENSE), score);
     }
 }
