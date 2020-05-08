 package util.analysis;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import search.Search4;
 import search.search33.SearchS4V33t;
 import state4.BitUtil;
 import state4.State4;
 import state4.StateUtil;
 import util.opening2.Book;
 import eval.Evaluator2;
 import eval.expEvalV3.E4;
import eval.expEvalV3.E4v2;
 import eval.expEvalV3.gparams.GParams1v2;
 
 /**
  * Simple launcher for playing two AIs. Prints board state after each move
  * and stops when a king has died. Uses {@link State2} for board representation
  * @author jdc2172
  *
  */
 public class GauntletP {
 	final static Book b = new Book(new File("megabook.bk"));
 	
 	private final static class GauntletThread extends Thread{
 		private final long time;
 		private final Search4[] search;
 		public final AtomicInteger[] counts = new AtomicInteger[3];
 		private final int maxDrawCount;
 		
 		GauntletThread(long time, Search4[] search, int maxDrawCount){
 			this.time = time;
 			this.search = search;
 			this.maxDrawCount = maxDrawCount;
 			for(int a = 0; a < counts.length; a++){
 				counts[a] = new AtomicInteger(0);
 			}
 		}
 		
 		public void run(){
 			final boolean print = false;
 			
 
 			final int[] wins = new int[2];
 			int draws = 0;
 
 			int[] move = new int[2];
 			for(int w = 0; ; w++){
 				State4 state = new State4(b.getSeed(), maxDrawCount);
 				state.initialize();
 				int turn = 0;
 				final int searchOffset = w%2;
 				boolean draw = false;
 				for(int a = 0; a < 2; a++) search[a].resetSearch();
 				boolean outOfBook = false;
 
 				while(state.pieceCounts[turn][State4.PIECE_TYPE_KING] != 0 &&
 						!isMate(turn, state) && !state.isForcedDraw()){
 
 					if(print) System.out.println("===========================================================");
 					if(print) System.out.println("search0 = "+search[0].getClass().getSimpleName()+
 							", search1 = "+search[1].getClass().getSimpleName());
 					if(print) System.out.println("current: (w0,w1,d) = ("+wins[0]+","+wins[1]+","+draws+")");
 					if(print) System.out.println("moving player "+turn);
 					if(print) System.out.println(StateUtil.fen(turn, state));
 					if(print) System.out.println("search state:\n"+state);
 					if(print) System.out.println("draw count = "+state.drawCount);
 
 					int[] bookMove = b.getRandomMove(turn, state, 100);
 					if(bookMove != null && !outOfBook){
 						System.arraycopy(bookMove, 0, move, 0, 2);
 						if(print) System.out.println("book move");
 					} else{
 						outOfBook = true;
 						//search[(turn+searchOffset)%2].search(turn, state, move, maxDepth);
 						//RawTimerThread3.search(search[(turn+searchOffset)%2], state, turn, time, 0, move);
 						search(turn, state, search[(turn+searchOffset)%2], move, time);
 					}
 					
 					if(print) System.out.println("search time = "+search[turn].getStats().searchTime);
 					if(print) System.out.println("move = "+getMoveString(move, 0)+" -> "+getMoveString(move, 1));
 					if(print) System.out.println();
 					
 					if(move[0] == move[1]){
 						draw = true;
 						break;
 					}
 					
 					state.executeMove(turn, 1L<<move[0], 1L<<move[1]);
 					turn = 1-turn;
 				}
 
 				if(state.isForcedDraw() || draw){
 					//fos.write(draw);
 					if(print) System.out.println("draw");
 					draws++;
 					counts[2].incrementAndGet();
 				} else{
 					final int winner = 1-turn;
 					//fos.write(winner == 0? whiteWin: blackWin);
 					if(print) System.out.println("player "+winner+" wins");
 					wins[(winner+searchOffset)%2]++;
 					counts[(winner+searchOffset)%2].incrementAndGet();
 				}
 				
 				System.out.println("--- (w0,w1,d) = ("+wins[0]+","+wins[1]+","+draws+")");
 			}
 		}
 	}
 	
 	public static void main(String[] args) throws IOException{
 		
 		final int hashSize = 20;
 		final long time = 50;
 		final int maxDrawCount = 50;
 
 		final int threads = 3;
 		final GauntletThread[] t = new GauntletThread[threads];
 		
 		System.out.print("initializing... ");
 		for(int a = 0; a < threads; a++){
 			Evaluator2 e1 =
 					//new SuperEvalS4V10();
 					//new ExpEvalV3v4();
 					//new ExpEvalV3v4(GParams1.buildEval());
 					new E4(GParams1v2.buildEval());
 					//new ExpEvalV3();
 					//new SuperEvalS4V10v4();
 					//new EvalS4V10v5();
 
 			Evaluator2 e2 = 
 					//new SuperEvalS4V10v4();
 					//new ExpEvalV3v4(GParams3.buildEval());
 					//new SuperEvalS4V10();
 					//new E4(GParams3Base.buildEval());
 					//new ExpEvalV3v4(GParams3.buildEval());
					//new SuperEvalS4V8();
 					//new ExpEvalV1();
					new E4v2(GParams1v2.buildEval());
 			
 			final Search4[] search = new Search4[2];
 			search[0] = new SearchS4V33t(e1, hashSize, false);
 			search[1] = new SearchS4V33t(e2, hashSize, false);
 			t[a] = new GauntletThread(time, search, maxDrawCount);
 			t[a].setDaemon(true);
 		}
 		System.out.println("complete");
 		
 		Thread controller = new Thread(){
 			public void run(){
 				final int[] counts = new int[3]; //wins0, wins1, draws
 				for(;;){
 					
 					for(int a = 0; a < 3; a++) counts[a] = 0;
 					
 					for(int a = 0; a < threads; a++){
 						for(int q = 0; q < 3; q++){
 							counts[q] += t[a].counts[q].get();
 						}
 					}
 					System.out.println("agg (w0,w1,d) = ("+counts[0]+","+counts[1]+","+counts[2]+")");
 					
 					try{
 						Thread.sleep(5*1000);
 					} catch(InterruptedException e){}
 				}
 			}
 		};
 		controller.start();
 		
 		for(int a = 0; a < threads; a++){
 			t[a].start();
 		}
 	}
 	
 	private static void search(final int player, final State4 s, final Search4 search, final int[] moveStore, final long targetTime){
 		final long start = System.currentTimeMillis();
 		
 		final Thread t = new Thread(){
 			public void run(){
 				search.search(player, s, moveStore, 99);
 			}
 		};
 		t.setDaemon(true);
 		t.start();
 		
 		while(System.currentTimeMillis()-start < targetTime){
 			final long diff = targetTime-(System.currentTimeMillis()-start);
 			if(diff/2 >= 1){
 				try{
 					Thread.sleep(diff/2);
 				} catch(InterruptedException e){}
 			}
 		}
 		
 		while(t.isAlive()){
 			search.cutoffSearch();
 			try{
 				t.join(500);
 			} catch(InterruptedException e){}
 		}
 	}
 
 	private final static int[] pawnOffset = new int[]{9,7,8,16};
 	/** checks that the player to move is not mated using their king*/
 	private static boolean isMate(int turn, State4 s){
 		if(!State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s)){
 			return false;
 		}
 		final long kings = s.kings[turn];
 		for(long m = State4.getKingMoves(turn, s.pieces, s.kings[turn]); m != 0; m &= m-1){
 			s.executeMove(turn, kings, m&-m);
 			boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
 			s.undoMove();
 			if(!attacked) return false;
 		}
 		for(long queens = s.queens[turn]; queens != 0; queens &= queens-1){
 			final long queen = queens&-queens;
 			for(long m = State4.getQueenMoves(turn, s.pieces, queen); m != 0; m &= m-1){
 				s.executeMove(turn, queen, m&-m);
 				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
 				s.undoMove();
 				if(!attacked) return false;
 			}
 		}
 		for(long knights = s.knights[turn]; knights != 0; knights &= knights-1){
 			final long knight = knights&-knights;
 			for(long m = State4.getKnightMoves(turn, s.pieces, knight); m != 0; m &= m-1){
 				s.executeMove(turn, knight, m&-m);
 				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
 				s.undoMove();
 				if(!attacked) return false;
 			}
 		}
 		for(long bishops = s.knights[turn]; bishops != 0; bishops &= bishops-1){
 			final long bishop = bishops&-bishops;
 			for(long m = State4.getBishopMoves(turn, s.pieces, bishop); m != 0; m &= m-1){
 				s.executeMove(turn, bishop, m&-m);
 				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
 				s.undoMove();
 				if(!attacked) return false;
 			}
 		}
 		for(long rooks = s.knights[turn]; rooks != 0; rooks &= rooks-1){
 			final long rook = rooks&-rooks;
 			for(long m = State4.getRookMoves(turn, s.pieces, rook); m != 0; m &= m-1){
 				s.executeMove(turn, rook, m&-m);
 				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
 				s.undoMove();
 				if(!attacked) return false;
 			}
 		}
 		final long[] temp = new long[4];
 		temp[0] = State4.getRightPawnAttacks(turn, s.pieces, s.enPassante, s.pawns[turn]);
 		temp[1] = State4.getLeftPawnAttacks(turn, s.pieces, s.enPassante, s.pawns[turn]);
 		temp[2] = State4.getPawnMoves(turn, s.pieces, s.pawns[turn]);
 		temp[3] = State4.getPawnMoves2(turn, s.pieces, s.pawns[turn]);
 		for(int a = 0; a < 4; a++){
 			for(long pawnsMoves = temp[a]; pawnsMoves != 0; pawnsMoves &= pawnsMoves-1){
 				final long move = pawnsMoves&-pawnsMoves;
 				final long pawn = turn == 0? move>>>pawnOffset[a]: move<<pawnOffset[a];
 				s.executeMove(turn, pawn, move&-move);
 				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
 				s.undoMove();
 				if(!attacked) return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param l
 	 * @param i offset
 	 * @return
 	 */
 	private static String getMoveString(int[] l, int i){
 		return ""+(char)('A'+l[i]%8)+(l[i]/8+1);
 	}
 }
