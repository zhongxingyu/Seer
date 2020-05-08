 package unionfind;
 
<<<<<<< HEAD
public class QuickFind implements UnionFind{
=======
 public class QuickFind implements UnionFind {
>>>>>>> quickUnion
 	
 	private int[]	id;
 	
 	public QuickFind(int number) {
 		id = new int[number];
 		for (int i = 0; i < id.length; i++)
 			id[i] = i;
 	}
 	
 	@Override
 	public boolean connected(int p, int q) {
 		return id[p] == id[q];
 	} // end method connected
 	
 	@Override
 	public void union(int p, int q) {
 		if (id[p] == id[q])
 			return;
 		
 		int pid = id[p];
 		int qid = id[q];
 		for (int i = 0; i < id.length; i++)
 			if (id[i] == pid)
 				id[i] = qid;
 	} // end method union
 } // end class QuickFind
