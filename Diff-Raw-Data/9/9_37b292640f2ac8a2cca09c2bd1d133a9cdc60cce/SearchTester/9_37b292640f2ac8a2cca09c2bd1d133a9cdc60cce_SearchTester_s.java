 package util.analysis;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import search.Search4;
 import search.SearchStat;
 import search.search33.SearchS4V33t;
 import state4.StateUtil;
 import uci.Position;
 import util.FenParser;
 import eval.Evaluator2;
 import eval.evalV10.SuperEvalS4V10;
 
 /**
  * tests search implementations by running through random test positions
  *
  */
 public final class SearchTester {
 	
 	public static void main(String[] args) throws IOException{
 		
 		//load positions
 		final File posf = new File("sample-positions.fen");
 		final Scanner scanner = new Scanner(posf);
 		final List<Position> pos = new ArrayList<>();
 		while(scanner.hasNextLine()){
 			final String fen = scanner.nextLine();
 			final Position p = FenParser.parse(fen);
 			pos.add(p);
 		}
 		scanner.close();
 		
 		
 		//initialize search
 		final int searchDepth = 5;
 		final int hashSize = 20;
 		final Evaluator2 e = new SuperEvalS4V10();
 		final Search4 searcher =
 				//new SearchS4V32k(e, hashSize, false);
 				new SearchS4V33t(e, hashSize, false);
 		
 		//search positions
 		final int len = pos.size();
 		final SearchStat agg = new SearchStat();
 		for(int a = 0; a < len; a++){
 			final Position p = pos.get(a);
 			searcher.resetSearch();
 			
			System.out.println(StateUtil.fen(p.sideToMove, p.s));
 			searcher.search(p.sideToMove, p.s, null, searchDepth);
 			SearchStat.agg(searcher.getStats(), agg);
 			
 			if(a % 100 == 0 && a != 0){
 				printAggStats(agg, a);
 				System.out.println("-------------------------------------");
 			}
 		}
 
 		System.out.println("position file = \""+posf+"\"");
 		System.out.println("search = "+searcher.getClass().getName());
 		System.out.println("eval = "+e.getClass().getName());
 		System.out.println("search depth = "+searchDepth);
 		System.out.println("hash size = "+hashSize);
 		System.out.println();
 		printAggStats(agg, len);
 	}
 	
 	private static void printAggStats(final SearchStat agg, final int len){
 		System.out.println("positions searched = "+len);
 		System.out.println("avg nodes searched = "+(agg.nodesSearched*1./len));
 		System.out.println("avg time (ms) = "+(agg.searchTime*1./len));
 		System.out.println("avg nodes/ms = "+(agg.nodesSearched*1./agg.searchTime));
 		System.out.println("avg braching factor = "+(agg.empBranchingFactor*1./len));
 		System.out.println("avg hash hit rate = "+(agg.hashHits*1./agg.nodesSearched));
 	}
 }
