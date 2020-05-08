 package ar.edu.itba.sia.gps.impl;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import ar.edu.itba.sia.AppConfig;
 import ar.edu.itba.sia.domain.Board;
 import ar.edu.itba.sia.domain.Piece;
 import ar.edu.itba.sia.gps.api.CostFunction;
 import ar.edu.itba.sia.gps.api.GPSProblem;
 import ar.edu.itba.sia.gps.api.GPSRule;
 import ar.edu.itba.sia.gps.api.GPSState;
 import ar.edu.itba.sia.gps.api.Heuristic;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class GPSProblemImpl implements GPSProblem {
 	private int height, width;
 	private List<Piece> all = Lists.newArrayList();
 	private List<GPSRule> rules = Lists.newArrayList();
 	private GPSState initState;
 	private static int id = 0;
 	private List<Heuristic> heuristics = Lists.newArrayList();
     private double printInterval;
 	private static boolean checkSymmetry;
 
     public static int depthSize = 12;
     private final AppConfig config;
 
     public GPSProblemImpl(int height, int width, List<Piece> allPieces, int colorCount, AppConfig config) {
 		this.height = height;
 		this.width = width;
 		all.addAll(allPieces);
 
         this.config = config;
 		generateRules(config.getCostFunction());
 		this.initState = GPSStateImpl.initialState(height, width, all, colorCount);
 		this.heuristics.addAll(config.getHeuristics());
         this.printInterval = config.getNodePrintFactor();
         checkSymmetry = config.getCheckSymmetry();
         depthSize = config.cacheDepthSize();
 	}
 
 	private void generateRules(CostFunction costFunction) {
 		for(Piece piece: all) {
 			for (int i = 0; i < height; i++) {
 				for (int j = 0; j < width; j++) {
 					rules.add(new GPSRuleImpl(piece, j, i, costFunction));
 					Piece rotated = piece.rotate(1);
 					for (int k = 0; k < 3; k++) {
 						rules.add(new GPSRuleImpl(rotated, j ,i, costFunction));
 						rotated = rotated.rotate(1);
 					}
 				}
 			}
 		}
         if (config.shuffleRules()) {
             shuffle(rules);
         }
 	}
 	
	private static <T> void shuffle(List<T> list) {
 		Random random = new Random();
		for (int i = 0; i < 256; i++) {
 			T auxA = null;
 			int a = random.nextInt(list.size());
 			auxA = list.get(a);
 			list.remove(a);
 			list.add(0, auxA);
 		}
 	}
 
 	public GPSState getInitState() {
 		return initState;
 	}
 
 	public List<GPSRule> getRules() {
 		return rules;
 	}
 
 	private Map<GPSState, Integer> resultCache = Maps.newHashMap();
 	
 	public Integer getHValue(GPSState state) {
 		
 		if (resultCache.containsKey(state)) {
 			return resultCache.get(state);
 		}
 		
 		int max = 0;
 		for(Heuristic heuristic: heuristics) {
 			int result = heuristic.apply(state);
 			if(result > max) {
 				max = result;
 			}
 		}
 		resultCache.put(state, max);
 		return max;
 	}
 
     @Override
     public double getPrintInterval() {
         return printInterval;
     }
 
     @Override
     public AppConfig getConfig() {
         return config;
     }
 
     public boolean checkGoalState(GPSState state) {
 		Board board = state.getBoard();
         return board.getPieceCount() == board.getWidth() * board.getHeight() && state.getBoard().isValid();
     }
 	
 	public static int nextId() {
 		return id++;
 	}
 
 	public static boolean checkSymmetry() {
 		return checkSymmetry;
 	}
 
 }
