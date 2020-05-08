 /**
  * 
  */
 package ex1;
 
 import java.util.HashMap;
 
 /**
  * @author Dima
  *
  */
 public class Cell implements Living {
	Position _position;
 	int _currentGeneration;
	HashMap<Integer, Integer> _lastTwoGenerations;
 
 	/* (non-Javadoc)
 	 * @see ex1.Living#getStatus(int)
 	 */
 	@Override
	public int getStatus(int generation) throws CellException {
 		if (generation > _currentGeneration)
 			throw new CellNotYetReady();
 		if (generation < _currentGeneration - 1)
 			throw new CellTooOld();
 		
 		if (! (_lastTwoGenerations.containsKey(generation)))
 			throw new CellUnknown();
 		
 		return _lastTwoGenerations.get(generation);
 	}
 
	public int update(int generation, boolean status)
 	
 }
