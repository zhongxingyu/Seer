 package jp.gr.java_conf.konkonlab.game_of_life.models.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.util.Pair;
 
 import jp.gr.java_conf.konkonlab.game_of_life.models.LifePattern;
 import junit.framework.TestCase;
 
 public class LifePatternTest extends TestCase {
 
 	public void testBlock() throws Exception {
 		String name = new String("Block");
 		int type = LifePattern.TYPE_STILL_LIFE;
 		int numCellX = 2;
 		int numCellY = 2;
 		
 		int[][] pairs = {
 				{0,0}, {0,1}, {1,0}, {1,1}
 		};
 		List<Pair<Integer, Integer>> cells = createCellList(pairs);
 		
 		LifePattern life = new LifePattern(name,type,cells,numCellX,numCellY);
 		
		assertEquals(life.getName(), name);
		assertEquals(life.getType(), type);
		assertEquals(life.getNumCellX(), numCellX);
		assertEquals(life.getNumCellY(), numCellY);
 		assertCellList(pairs, life);
 	}
 
 	private void assertCellList(int[][] pairs, LifePattern life) {
 		for(int i = 0; i < pairs.length;i++){
 			assertTrue(life.isAliveAt(pairs[i][0], pairs[i][1]));
 		}
 	}
 
 	private List<Pair<Integer, Integer>> createCellList(int[][] pairs) {
 		List<Pair<Integer, Integer>> cells = new ArrayList<Pair<Integer, Integer>>();
 		
 		for(int i = 0; i < pairs.length;i++){
 			cells.add(new Pair<Integer,Integer>(pairs[i][0],pairs[i][1]));
 		}
 		return cells;
 	}
 }
