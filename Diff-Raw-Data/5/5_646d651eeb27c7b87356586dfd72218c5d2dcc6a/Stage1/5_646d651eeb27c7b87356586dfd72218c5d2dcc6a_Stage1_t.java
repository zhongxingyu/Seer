 package chess.eval.e9.pipeline;
 
 import chess.eval.e9.PawnHash;
 import chess.eval.e9.PawnHashEntry;
 import chess.eval.e9.Weight;
 import chess.eval.e9.pawnEval.PawnEval;
 import chess.state4.State4;
 
 /**
  * preliminary stage evaluations, very basic but comprise large proportion of eval score
  */
 public final class Stage1 implements MidStage {
 
 	/** helper stage to check for score cutoffs*/
 	public final static class CutoffCheck implements MidStage{
 		private final MidStage next;
 		private final int stage;
 
 		public CutoffCheck(MidStage next, int stage){
 			this.next = next;
 			this.stage = stage;
 		}
 
 		@Override
 		public EvalResult eval(Team allied, Team enemy, BasicAttributes basics, EvalContext c, State4 s, int score) {
 			final int stage1MarginLower; //margin for a lower cutoff
 			final int stage1MarginUpper; //margin for an upper cutoff
 			if(allied.queenCount != 0 && enemy.queenCount != 0){
 				//both sides have queen, apply even margin
 				stage1MarginLower = -82; //margin scores taken from profiled mean score diff, 1.7 std
 				stage1MarginUpper = 76;
 			} else if(allied.queenCount != 0){
 				//score will be higher because allied queen, no enemy queen
 				stage1MarginLower = -120;
 				stage1MarginUpper = 96;
 			} else if(enemy.queenCount != 0){
 				//score will be lower because enemy queen, no allied queen
 				stage1MarginLower = -92;
 				stage1MarginUpper = 128;
 			} else{
 				stage1MarginLower = -142;
 				stage1MarginUpper = 141;
 			}
 
 			final boolean lowerBoundCutoff = score+stage1MarginUpper <= c.lowerBound; //highest score still less than alpha
 			final boolean upperBoundCutoff = score+stage1MarginLower >= c.upperBound; //lowest score still greater than beta
 
 			if(lowerBoundCutoff || upperBoundCutoff){
 				return new EvalResult(score, stage1MarginLower, stage1MarginUpper, stage);
 			} else{
 				return next.eval(allied, enemy, basics, c, s, score);
 			}
 		}
 	}
 	private final static int tempoWeight = S(14, 5);
 	private final static int bishopPairWeight = S(10, 42);
 
 	/** assist stage to check for score cutoffs before continuing down eval pipeline*/
 	private final MidStage cutoffChecker;
 	private final PawnHash pawnHash;
 	private final PawnHashEntry filler = new PawnHashEntry();
 
 	public Stage1(PawnHash pawnHash, MidStage next, int stage){
 		this.pawnHash = pawnHash;
 		this.cutoffChecker = new CutoffCheck(next, stage);
 	}
 
 	@Override
 	public EvalResult eval(Team allied, Team enemy, BasicAttributes basics, EvalContext c, State4 s, int prevScore) {
 		int player = c.player;
 
 		//load hashed pawn values, if any
 		final long pawnZkey = s.pawnZkey();
 		final PawnHashEntry phEntry = pawnHash.get(pawnZkey);
 		final PawnHashEntry loader;
 		if(phEntry == null){
 			filler.passedPawns = 0;
 			filler.zkey = 0;
 			loader = filler;
 		} else{
 			loader = phEntry;
 		}
 
 		int stage1Score = 0;
 		stage1Score += S(basics.materialScore);
		/*stage1Score += tempoWeight;
 
 		if(allied.bishopCount == 2){ //note, case 2 bishops on same square is not caught
 			stage1Score += bishopPairWeight;
 		}
 		if(enemy.bishopCount == 2){
 			stage1Score += -bishopPairWeight;
 		}
 
 		stage1Score += PawnEval.scorePawns(player, s, loader, enemy.queens, basics.nonPawnMaterialScore) -
				PawnEval.scorePawns(1-player, s, loader, allied.queens, basics.nonPawnMaterialScore);*/
 
 		int score = prevScore +
 				Weight.interpolate(stage1Score, c.scale) +
 				Weight.interpolate(S((int)(Weight.egScore(stage1Score)*.1), 0), c.scale);
 
 		if(phEntry == null){ //store newly calculated pawn values
 			loader.zkey = pawnZkey;
 			pawnHash.put(pawnZkey, loader);
 		}
 
 		//check for score cutoff then continue if necessary
 		return cutoffChecker.eval(allied, enemy, basics, c, s, score);
 	}
 
 	/** build a weight scaling from passed start,end values*/
 	private static int S(int start, int end){
 		return Weight.encode(start, end);
 	}
 
 	private static int S(int weight){
 		return Weight.encode(weight, weight);
 	}
 }
