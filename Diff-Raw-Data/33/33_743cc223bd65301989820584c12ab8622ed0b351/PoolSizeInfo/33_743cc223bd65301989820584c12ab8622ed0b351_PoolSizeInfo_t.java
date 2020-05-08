 package final_project.model;
 
 public class PoolSizeInfo {
     public int _poolSize, _numBigPools, _numSmallPools;
 
     public PoolSizeInfo(int poolSize, int numBigPools, int numSmallPools) {
         _poolSize = poolSize;
         _numBigPools = numBigPools;
         _numSmallPools = numSmallPools;
     }

    public String toString() {
        return "("+_poolSize+" "+_numBigPools+" "+_numSmallPools+")";
    }
 }
