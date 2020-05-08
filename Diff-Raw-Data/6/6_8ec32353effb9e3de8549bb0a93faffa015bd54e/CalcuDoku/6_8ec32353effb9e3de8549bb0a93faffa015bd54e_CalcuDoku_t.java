 package sia.calcudoku;
 
 import sia.gps.AStarEngine;
 import sia.gps.BFSEngine;
 import sia.gps.DFSEngine;
 import sia.gps.GPSEngine;
 import sia.gps.GreedyEngine;
 import sia.gps.HIDFSEngine;
 import sia.gps.IDFSEngine;
 
 
 public class CalcuDoku {
 	
 	public static void main(String args[]){
 		if (args == null || args.length < 3) {
 			System.out.println("Formato de argumentos invalido");
 			System.out.println("Ingrese [BFS|DFS|IDFS|HIDFS|Greedy|Astar] [QuadOp3x3|QuadOp4x4|QuadOp4x4tricky|5x5|QuadOp6x6|DualOp8x8] [width] [groups|rowsNCols|both] [limitIDFS]");
 			return;
 		}
 		for(String s:args){
 			System.out.println(s);
		}
 		CalcuDokuProblem problem = new CalcuDokuProblem("src/main/resources/levels/"+args[1], Integer.parseInt(args[2]),args.length>3?Heuristic.valueOf(args[3].toUpperCase()):Heuristic.BOTH);
 		GPSEngine engine = null;
 		long a = System.currentTimeMillis();
 		switch (args[0].toUpperCase()) {
 		case "BFS":
 			engine = new BFSEngine();
 			break;
 		case "DFS":
 			engine = new DFSEngine();
 			break;
 		case "ASTAR":
 			engine = new AStarEngine();
 			break;
 		case "IDFS":
 			engine = new IDFSEngine(Integer.valueOf(args[4]));
 			break;
 		case "HIDFS":
 			engine = new HIDFSEngine(Integer.valueOf(args[4]));
 			break;
 		case "GREEDY":
 			engine = new GreedyEngine();
 			break;
 		default:
 			break;
 		}
 		engine.engine(problem);
 		
 		System.out.println((System.currentTimeMillis()-a)  + " ms");
 	}
 
 }
