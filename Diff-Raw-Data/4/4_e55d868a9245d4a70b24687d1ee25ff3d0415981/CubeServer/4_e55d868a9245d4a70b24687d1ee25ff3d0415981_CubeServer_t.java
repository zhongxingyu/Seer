 package me.asofold.bukkit.pic.core;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * 
  * @author mc_dev
  *
  */
 public final class CubeServer {
 	
 	private final Map<CubePos, CubeData> cubes = new HashMap<CubePos, CubeData>(500);
 	
 	public PicCore core;
 
 	public final int cubeSize;
 
 	public CubeServer(final PicCore core, int cubeSize){
 		this.core = core;
 		this.cubeSize = cubeSize;
 	}
 	
 	/**
 	 * Called when a CubeData has no players anymore.
 	 * @param cubeData
 	 */
 	public final void cubeEmpty(final CubeData cubeData) {
 		cubes.remove(cubeData.cube);
 		// TODO: park it. ? Map by hash directly into an Array of lists + check for new cubes by hash ?
 	}
 
 
 	public final void renderBlind(final PicPlayer pp, final Set<String> names) {
 		core.renderBlind(pp, names);
 	}
 	
 	public final void renderSeen(final PicPlayer pp, final Set<String> names) {
 		core.renderSeen(pp, names);
 	}
 
 	/**
 	 * Check which cubes have to be added.
 	 * @param pp
 	 * @param distCube Maximal distance to cube centers.
 	 */
 	public final void update(final PicPlayer pp, final int distCube) {
 		// Dumb: check all cubes within distance
 		for (int x = pp.x - distCube; x < pp.x + distCube; x += cubeSize){
 			for (int y = pp.y - distCube; y < pp.y + distCube; y += cubeSize){
 				for (int z = pp.z - distCube; z < pp.z + distCube; z += cubeSize){
 					// TODO: optimize here and just get the hash later 
					final CubePos pos = new CubePos(x, y, z);
 					if (pp.cubes.contains(pos)) continue;
 					CubeData data = cubes.get(pos);
 					if (data == null){
 						// create new one
 						data = new CubeData(new Cube(x, y, z, cubeSize), this);
 						cubes.put(pos, data);
 					}
 					data.add(pp);
					pp.cubes.add(data);
 				}
 			}
 		}
 	}
 	
 }
