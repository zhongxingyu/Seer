 /**
  * 
  */
 
 /**
  * @author adrian
  *
  */
 public class WeightedQuickUnion implements DynamicConnectivity {
 	private int[] id;
 	private int[] sz;
 	
 	public WeightedQuickUnion(int N){
 		id = new int[N];
 		sz = new int[N];
 		for(int i=0; i < N; i++){
 			id[i] = i;
 			sz[i] = 1;
 		}
 	}
 	
 	/**
 	 * Find root
 	 * @param i
 	 * @return
 	 */
 	private int find(int i){
 		while(i != id[i]){
 			id[i] = id[id[i]]; // path compression
 			i = id[i];
 		}
 		return i;
 	}
 
 	/* (non-Javadoc)
 	 * @see DynamicConnectivity#union(int, int)
 	 */
 	@Override
 	public void union(int p, int q) {
 		int findq = find(q);
 		int findp = find(p);
 		
 		if(sz[p] > sz[q]){
 			id[findq] = find(p);
 			sz[findq] += sz[p];
 		} else {
 			id[findp] = find(q);
 			sz[findp] += sz[q];
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see DynamicConnectivity#connected(int, int)
 	 */
 	@Override
 	public boolean connected(int p, int q) {
 		return find(p) == find(q);
 	}
 }
