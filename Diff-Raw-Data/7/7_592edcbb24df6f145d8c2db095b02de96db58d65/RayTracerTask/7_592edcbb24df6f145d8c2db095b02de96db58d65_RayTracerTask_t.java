 package distributed;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.gridgain.grid.GridException;
 import org.gridgain.grid.GridJob;
 import org.gridgain.grid.GridJobAdapterEx;
 import org.gridgain.grid.GridJobResult;
 import org.gridgain.grid.GridTaskSplitAdapter;
 import org.gridgain.grid.gridify.GridifyArgument;
 
 import scene.Scene;
 
 public class RayTracerTask extends GridTaskSplitAdapter<GridifyArgument, int[][]> {
 
 	@Override
 	protected Collection<? extends GridJob> split(int gridSize, GridifyArgument arg)
 			throws GridException {
 		Scene scene = (Scene)arg.getMethodParameters()[0];
		int delta = scene.getHeight() / gridSize + (scene.getHeight() % gridSize == 0 ? 0 : 1);
 		List<GridJobAdapterEx> jobs = new ArrayList<GridJobAdapterEx>(gridSize);
		for (int i = 0; i < gridSize; i++) {
			jobs.add(new GridJobAdapterEx(scene, i * delta, Math.min(i * delta + delta, scene.getHeight())) {
 
 				@Override
 				public OrderWrapper<int[][]> execute() throws GridException {					
 					return new OrderWrapper<int[][]>((Integer)argument(1), GridRenderer.executeRayTracer((Scene)argument(0),
 																			(Integer)argument(1), (Integer)argument(2)));
 				}
 				
 			});
 		}
 		return jobs;
 	}
 	
 	@Override
 	public int[][] reduce(List<GridJobResult> arg) throws GridException {
 		List<OrderWrapper<int[][]>> wrappers = new ArrayList<OrderWrapper<int[][]>>();
 		int width = 0, height = 0;
 		for (GridJobResult result : arg) { 
 			OrderWrapper<int[][]> ow = (OrderWrapper)result.getData();
 			wrappers.add(ow);
 			height += ow.getContent().length;
 		}
 		width = wrappers.get(0).getContent()[0].length;
 		Collections.sort(wrappers);
 		int[][] result = new int[height][width];
 		int pos = 0;
 		for (OrderWrapper<int[][]> wrapper : wrappers) {
 			int[][] data = wrapper.getContent();
 			for (int i = 0; i < data.length; i++) 
 				result[pos++] = data[i];			
 		}
 		return result;
 	}
 
 }
