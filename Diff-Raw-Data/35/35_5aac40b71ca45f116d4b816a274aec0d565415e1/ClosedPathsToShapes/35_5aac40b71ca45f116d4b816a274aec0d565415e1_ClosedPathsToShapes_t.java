 package nogbeter.graphtheory;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import bezier.util.Tuple;
 
 import nogbeter.paths.Path;
 import nogbeter.paths.compound.ClosedPath;
 import nogbeter.paths.compound.ClosedPathIndex;
 import nogbeter.paths.factory.PathFactory;
 import nogbeter.paths.results.project.BestProject;
 import nogbeter.points.twod.Vec;
 
 public class ClosedPathsToShapes {
 	
 	public static Path closedPathsToShapes(List<ClosedPath> closed){
 		Tuple<List<Integer>,List<Integer>> bh = splitIntoBordersAndHoles(closed);
 		List<Integer> borders = bh.l;
 		List<Integer> holes = bh.r;
 		
 		boolean[][] contains = new boolean[closed.size()][closed.size()];
 		for(int j : holes){
 			boolean found = false;
 			for(int i : borders){
 
 				if(closed.get(i).contains(closed.get(j))){
 					contains[i][j] = true;
 					found = true;
 					break;
 				} else if(!closed.get(j).contains(closed.get(i))){
 					contains[j][i] = true;
 				}
 			}
//			if(!found)
//			System.out.printf("Not contained in anything: %s\n", closed.get(j).getArbPoint());
 		}
 		try{
 			List<Tree> forrest = new Graph(contains).getForrest();
 			List<Path> res = new ArrayList<Path>();
 //			System.out.printf("Number of shapes: %d\n", forrest.size());
 			for(Tree f : forrest){
				if(closed.get(f.root).isDefindedClockwise()){
 					res.add(makeShape(closed,f));
 				}
 			}
 			return PathFactory.createSet(res);
 		} catch(CycleFoundException e){
 			System.out.printf("Cycle found!:\n");
 			List<Path> res = new ArrayList<Path>();
 			for(int i : e.cycle){
 				res.add(closed.get(i));
 				System.out.printf("\t%s\n",closed.get(i));
 			}
 			
 			return PathFactory.createSet(res);
 		}
 	}
 
 
 	private static List<Path> makeShapeSet(List<ClosedPath> closed, List<Tree> forrest) {
 		List<Path> shapes = new ArrayList<Path>();
 		for(Tree t : forrest){
 			shapes.add(makeShape(closed,t));
 		}
 		return shapes;
 	}
 
 	private static Path makeShape(List<ClosedPath> closed, Tree t) {
 		return PathFactory.createShape(closed.get(t.root), makeShapeSet(closed, Arrays.asList(t.children)));
 	}
 
 	static Tuple<List<Integer>,List<Integer>> splitIntoBordersAndHoles(List<ClosedPath> closed){
 		List<Integer> borders = new ArrayList<Integer>();
 		List<Integer> holes = new ArrayList<Integer>();
 		for(int i = 0 ; i < closed.size() ; i++){
 			if(closed.get(i).isDefindedClockwise()){
 				borders.add(i);
 			} else {
 				holes.add(i);
 			}
 		}
 		return new Tuple<List<Integer>, List<Integer>>(borders, holes);
 	}
 	
 }
