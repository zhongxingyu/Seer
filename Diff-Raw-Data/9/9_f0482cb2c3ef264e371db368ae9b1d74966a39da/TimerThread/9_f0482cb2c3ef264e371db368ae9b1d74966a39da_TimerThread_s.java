 package util;
 
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 
 import search.MoveSet;
 import search.SearchListener2;
 import state4.BitUtil;
 import state4.Masks;
 import state4.State4;
 
 public final class TimerThread extends Thread{
 	enum SearchType{
 		planningSearch,
 		fixedTimeSearch
 	}
 	
 	private final static class PlySearchResult{
 		int ply;
 		long move;
 		int score;
 	}
 	
 	private final class TimeParams{
 		volatile SearchType type;
 		
 		volatile State4 s;
 		volatile int player;
 		volatile long time;
 		volatile long inc;
 		volatile MoveSet moveStore;
 	}
 
 	private final SearchListener2 l = new SearchListener2() {
 		@Override
 		public void plySearched(long move, int ply, int score) {
 			PlySearchResult temp = new PlySearchResult();
 			temp.move = move;
 			temp.ply = ply;
 			temp.score = score;
 			plyq.add(temp);
 			
 			TimerThread.this.interrupt();
 		}
 		@Override
 		public void failLow(int ply) {
 			/*q.add(failLow);
 			TimerThread.this.interrupt();*/
 		}
 		@Override
 		public void failHigh(int ply) {
 			/*q.add(failHigh);
 			TimerThread.this.interrupt();*/
 		}
 	};
 	
 	/** stores extra time from aspiration window failures*/
 	private final LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<Integer>();
 	private final LinkedBlockingQueue<PlySearchResult> plyq = new LinkedBlockingQueue<PlySearchResult>();
 	private final TimeParams p = new TimeParams();
 	private final SearchThread searcher;
 	private final Semaphore sem = new Semaphore(1);
 	private volatile boolean searching = false;
 	
 	public TimerThread(SearchThread s){
 		this.searcher = s;
 	}
 	
 	@Override
 	public void run(){
		
 		for(;;){
			
 			if(searching){
 				sem.acquireUninterruptibly();
 				synchronized(p){
 					searcher.setSearchListener(l);
 					searcher.startSearch(p.player, p.s, p.moveStore);
 					
 					if(p.type == SearchType.planningSearch){
 						planningTimeSearch(p);
 					} else if(p.type == SearchType.fixedTimeSearch){
 						fixedTimeSearch(p);
 					}
 					
 					searching = false;
 				}
 				sem.release();
 			}
 			
 			while(!searching){
 				try{
 					synchronized(this){
 						wait();
 					}
 				} catch(InterruptedException e){}
 			}
 		}
 	}
 	
 	/**
 	 * start a search that plans its time dynamically based on positional characteristics and alloted time
 	 * @param search
 	 * @param s
 	 * @param player
 	 * @param time
 	 * @param inc
 	 */
 	public void startTimePlanningSearch(State4 s, int player, long time, long inc, MoveSet moveStore){
 		synchronized(p){
 			p.type = SearchType.planningSearch;
 			p.s = s;
 			p.player = player;
 			p.inc = inc;
 			p.moveStore = moveStore;
 			
 			searching = true;
 		}
 		interrupt();
 	}
 	
 	public void startFixedTimeSearch(State4 s, int player, long time, MoveSet moveStore){
 		synchronized(p){
 			p.type = SearchType.fixedTimeSearch;
 			p.s = s;
 			p.player = player;
 			p.moveStore = moveStore;
 			
 			searching = true;
 		}
 	}
 	
 	private void fixedTimeSearch(TimeParams p){
 		long targetTime = p.time;
 		
 		final long start = System.currentTimeMillis();
 		long time;
 		while((time = System.currentTimeMillis()-start) < targetTime){
 			try{
 				Thread.sleep(time/2);
 			} catch(InterruptedException e){}
 		}
 		searcher.stopSearch();
 	}
 	
 	private void planningTimeSearch(TimeParams p){
 		State4 s = p.s;
 		long time = p.time;
 		long inc = p.inc;
 		
		
 		long start = System.currentTimeMillis();
 		final int material = getMaterial(s);
 		long target = time / (getHalfMovesRemaining(material)/2);
 		target *= .8;
 		target += .8*inc;
 		long maxTime = (long)(target * 1.3);
 		
 		long move = 0;
 		int currentPly = 0;
 		int lastpvChange = 1;
 		int currentScore = 0;
 		
 		final boolean checking = isChecked(0, s) | isChecked(1, s);
 		
 		
 		int minDepth = 16;
 		for(int a = 0; a < 2; a++){
 			if(s.pieceCounts[a][State4.PIECE_TYPE_QUEEN] == 0) minDepth++;
 			int pieces = s.pieceCounts[a][State4.PIECE_TYPE_BISHOP]+
 					s.pieceCounts[a][State4.PIECE_TYPE_KNIGHT]+
 					s.pieceCounts[a][State4.PIECE_TYPE_ROOK];
 			if(pieces == 0) minDepth++;
 			if(pieces <= 3) minDepth++;
 		}
 		final int pawns = s.pieceCounts[0][State4.PIECE_TYPE_PAWN] + s.pieceCounts[1][State4.PIECE_TYPE_PAWN];
 		if(pawns <= 8) minDepth++;
 		if(pawns <= 4) minDepth++;
 		if(checking) minDepth++;
 		
 		long currentTime;
 		while((currentTime = System.currentTimeMillis()-start) < target && currentTime < maxTime && searching){
 			
 			while(!plyq.isEmpty()){
 				PlySearchResult r = plyq.poll();
 				if(r.ply != 1 && r.move != move){
 					lastpvChange = r.ply;
 				}
 				move = r.move;
 				currentPly = r.ply;
 				currentScore = r.score;
 			}
 			
 			if(currentScore < 70000){
 				if(!checking && currentPly-lastpvChange+1 > 7 && currentPly >= minDepth){ //perhaps increase difficulty with fail lows
 					break;
 					//target -= target*.2;
 				} else if(checking && currentPly-lastpvChange+1 > 8 && currentPly >= 19){
 					break;
 				}
 			}
 			if(currentScore > 80000){
 				break;
 			}
 			
 			//handle adjustments from search failures
 			/*while(!q.isEmpty()){
 				//int failType = q.poll();
 				//double scale = failType == failHigh? .005: .01;
 				double scale = 0;
 				target += target*scale;
 			}*/
 
 			final long sleepTime = (target-(System.currentTimeMillis()-start))/2;
 			if(sleepTime >= 1){
 				try{
 					Thread.sleep(sleepTime);
 				} catch(InterruptedException e){}
 			}
 		}
 		
 		searcher.stopSearch();
 	}
 	
 	public void stopSearch(){
 		searching = false;
 		searcher.stopSearch();
 		
 		sem.acquireUninterruptibly();
 		sem.release();
 	}
 
 	/** tests to see if the passed player is in check*/
 	private static boolean isChecked(final int player, final State4 s){
 		return State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
 	}
 	
 	private static int getMaterial(State4 s){
 		int m = 0;
 		for(int a = 0; a < 2; a++){
 			m += s.pieceCounts[a][State4.PIECE_TYPE_BISHOP]*3;
 			m += s.pieceCounts[a][State4.PIECE_TYPE_KNIGHT]*3;
 			m += s.pieceCounts[a][State4.PIECE_TYPE_PAWN]*1;
 			m += s.pieceCounts[a][State4.PIECE_TYPE_QUEEN]*9;
 			m += s.pieceCounts[a][State4.PIECE_TYPE_ROOK]*5;
 		}
 		return m;
 	}
 	
 	/**
 	 * gets remaining half moves as calculated by
 	 * 'time management procedures in computer chess'
 	 * @param material
 	 * @return returns half moves remaining
 	 */
 	private static int getHalfMovesRemaining(int material){
 		if(material < 20){
 			return material+10;
 		} else if(material <= 60){
 			return (int)(3./8*material+22);
 		} else{
 			return (int)(5./4*material-30);
 		}
 	}
 
 	public SearchListener2 getListener(){
 		return l;
 	}
 }
