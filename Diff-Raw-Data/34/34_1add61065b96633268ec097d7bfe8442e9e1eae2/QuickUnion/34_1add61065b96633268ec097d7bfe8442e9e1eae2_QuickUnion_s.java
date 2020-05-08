 /**
  * 
  */
 
 /**
  * @author adrian
  *
  */
 public class QuickUnion implements DynamicConnectivity {
 	private int[] id;
 	
 	public QuickUnion(int N){
 		id = new int[N];
		for(int i=0; i < N; i++) id[i] = i;
 	}
 	
 	/**
 	 * Find root
 	 * @param i
 	 * @return
 	 */
 	private int find(int i){
		while(i != id[i]) i = id[i];
 		return i;
 	}
 
 	/* (non-Javadoc)
 	 * @see DynamicConnectivity#union(int, int)
 	 */
 	@Override
 	public void union(int p, int q) {
		id[find(p)] = find(q);
 	}
 
 	/* (non-Javadoc)
 	 * @see DynamicConnectivity#connected(int, int)
 	 */
 	@Override
 	public boolean connected(int p, int q) {
 		return find(p) == find(q);
 	}
 
 }
