 package search.search34;
 
 
 public final class ZMap4 implements Hash{
 	private final CuckooHash2 alwaysReplace;
 	private final TTEntry[] depthReplace;
 	private final int size;
 	private final TTEntry temp = new TTEntry();
 	
 	/**
 	 * creates a new ZMap
 	 * @param size hash map size, in powers of 2
 	 */
 	public ZMap4(int size){
 		this.size = size;
 		alwaysReplace = new CuckooHash2(size, 16);
 		depthReplace = new TTEntry[1<<size];
 		for(int i = 0; i < 1<<size; i++){
 			depthReplace[i] = new TTEntry();
 		}
 	}
 	
 	public TTEntry get(long zkey){
 		TTEntry e = alwaysReplace.get(zkey);
 		if(e != null && e.zkey == zkey){
 			return e;
 		} else{
 			final int index = (int)(zkey >>> (64-size));
 			TTEntry d = depthReplace[index];
 			if(d.zkey == zkey){
 				return d;
 			}
 		}
 		return null;
 	}
 	
 	public void put(final long zkey, final TTEntry e){
 		TTEntry t = get(zkey);
		if(t != null){
			if(t.depth <= e.depth){
				TTEntry.swap(t, e);
			}
 		} else{
 			final int index = (int)(zkey >>> (64-size));
 			final int seq = e.seq;
 			final TTEntry d = depthReplace[index];
 			if(d.seq != seq || e.depth >= d.depth){
 				temp.fill(d);
 				d.fill(e);
 				if(temp.zkey == zkey) return;
 			} else{
 				alwaysReplace.put(zkey, e);
 			}
 		}
 	}
 	
 	public void clear(){
 		for(int a = 0; a < 1<<size; a++){
 			depthReplace[a].clear();
 		}
 		alwaysReplace.clear();
 	}
 }
