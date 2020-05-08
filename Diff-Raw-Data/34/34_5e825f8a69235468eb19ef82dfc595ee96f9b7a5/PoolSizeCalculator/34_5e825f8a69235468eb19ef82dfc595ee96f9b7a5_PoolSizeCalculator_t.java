 package final_project.control;
 
 import final_project.control.Constants;
 
 public class PoolSizeCalculator {
 	private int _numBigPools;
 	private int _numSmallPools;

 	/**
 	 * Calculates the number of pools of either size required to put all fencers into pools.
 	 * @param numPlayers A non-negative integer representing the number of fencers to be placed into pools
 	 * @param poolSize A non-negative integer representing the ideal number of fencers to be placed in each pool
 	 * @return A Java.awt.Point representing the proper number of pools of poolSize fencers(X) and the number of pools of poolSize - 1 fencers(Y)
 	 */
 	public PoolSizeCalculator(int numPlayers, int poolSize) throws IllegalArgumentException{
 		_numBigPools = 0;
 		_numSmallPools = 0;
 		int placedPlayers = 0;

 		if (isInvalidPoolSizeForNumPlayers(numPlayers, poolSize)) {
 			throw new IllegalArgumentException("Invalid pool size or number of players");
 		}

 		while (placedPlayers < numPlayers)
 			placedPlayers = (++_numBigPools * poolSize);

 		while (placedPlayers > numPlayers)
 			placedPlayers = (--_numBigPools * poolSize) + (++_numSmallPools * (poolSize - 1));

 		if (placedPlayers != numPlayers)
 			throw new IllegalArgumentException("Invalid pool size for given number of players");
 	}
 
 	private boolean isInvalidPoolSizeForNumPlayers(int numPlayers, int poolSize) {
 		return poolSize <= 0 || numPlayers <= 0 || numPlayers < poolSize || poolSize > 8 || poolSize < 4;
 	}

 	public int getNumBigPools() {
 		return _numBigPools;
 	}
 
 	public int getNumSmallPools() {
 		return _numSmallPools;
 	}
 }
