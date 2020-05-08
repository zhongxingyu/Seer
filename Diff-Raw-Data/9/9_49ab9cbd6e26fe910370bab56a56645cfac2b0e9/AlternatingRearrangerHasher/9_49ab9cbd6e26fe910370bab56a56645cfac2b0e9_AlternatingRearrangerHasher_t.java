 package edu.berkeley.gamesman.hasher;
 
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.Hasher;
 import edu.berkeley.gamesman.util.OptionProcessor;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * The AlternatingRearrangerHash perfectly encodes boards where pieces are placed by two alternating players
  * There should be no empty spaces.  Use a tiered game if possible.
  * 
  * @author Steven Schlansker
  */
 public class AlternatingRearrangerHasher extends Hasher<char[]> {
 	
 	static {
 		OptionProcessor.acceptOption("cd", "cachedepth", true, "The depth of level that the AlternatingRearrangerHash should cache","12");
 	}
 	
 	private int cacheLevel = 0;
 	private Map<String,BigInteger> rearrCache = new ConcurrentHashMap<String, BigInteger>();
 	private Map<BigInteger,String> unrearrCache = new ConcurrentHashMap<BigInteger, String>();
 
 	@Override
 	public void setGame(Game<char[]> game, char[] p){
 		Util.assertTrue(p.length == 2,"Wrong number of pieces for AlternatingRearrangerHasher");
 		super.setGame(game,p);
 		cacheLevel = Integer.parseInt(OptionProcessor.checkOption("cachedepth"));
 	}
 	
 	@Override
 	public BigInteger hash(char[] board, int l) {
 		int[] count = countxo(board,l);
 		BigInteger val = pieceRearrange(board,0,count[0],count[1],count[2]);
 		//Util.debug("ARH hashes "+Arrays.toString(board)+" ("+l+") to "+val);
 		return val;
 	}
 
 	@Override
 	public char[] unhash(BigInteger hash, int l) {
 		int x = l/2 + l%2;
 		int o = l/2;
 
 		char[] result = new char[l];
 
 		pieceUnrearrange(hash, result, 0, l, x, o);
 		
 		return result;
 	}
 	
 	protected int[] countxo(char[] board, int l){
 		int ret[] = new int[3];
 		for(char c : board){
 			if(ret[0] == l) break;
 			
 			ret[0]++;
 			if(c == 'X' || c == 'x') ret[1]++;
 			else ret[2]++;
 
 		}
 		return ret;
 	}
 	
 	protected BigInteger pieceRearrange(char[] board, int src, int pcs, int x, int o){
 		if(pcs == 1 || x == 0) return BigInteger.ZERO;
 		boolean cache = false;
 		String cachestr = null;
 		
 		BigInteger rVal;
 		
 		if(board.length - src <= cacheLevel){
 			cachestr = new String(board).substring(src)+pcs+""+x+""+o;
 			if((rVal = rearrCache.get(cachestr)) != null)
 				return rVal;
 			cache = true;
 		}
 		
 		if(board[src] == 'X'){
 			rVal = pieceRearrange(board, src+1, pcs-1, x-1, o);
 		}else{
 			BigInteger off = BigInteger.valueOf(Util.nCr_arr[pcs-1][x-1]);
 			rVal = pieceRearrange(board, src+1, pcs-1, x, o-1);
 			rVal = off.add(rVal);
 		}
 		
 		if(cache){
 			rearrCache.put(cachestr, rVal);
 		}
 
 		return rVal;
 	}
 	
 	protected void pieceUnrearrange(BigInteger hash,char[] board, int src, int pcs, int x, int o){
 		if(pcs == 0) return;
 
 		BigInteger off;
 		boolean cache = false;
 		
 		if(board.length - src == cacheLevel){
 			String str;
 			if((str = unrearrCache.get(hash)) != null){
 				str.getChars(0, str.length()-1, board, src);
 				return;
 			}
 			cache = true;
 		}
 		
 		if(x == 0)
 			off = BigInteger.ZERO;
 		else
 			off = BigInteger.valueOf(Util.nCr_arr[pcs-1][x-1]);
 		if((hash.compareTo(off) < 0 || o == 0) && x > 0){
 			board[src] = 'X';
 			pieceUnrearrange(hash, board, src+1, pcs-1, x-1, o);
 		}else{
 			board[src] = 'O';
 			pieceUnrearrange(hash.subtract(off), board, src+1, pcs-1, x, o-1);
 		}
 		
 		//if(cache){
 		//	unrearrCache.put(hash, new String(board,src,board.length-src));
 		//}
 	}
 
 	@Override
 	public BigInteger maxHash(int boardlen) {
		return BigInteger.valueOf(Util.nCr(boardlen,(boardlen+1)/2));//.subtract(BigInteger.ONE);
 	}
 
 	@Override
 	public String describe() {
 		return "ARH"+Arrays.toString(pieces);
 	}
 
 }
