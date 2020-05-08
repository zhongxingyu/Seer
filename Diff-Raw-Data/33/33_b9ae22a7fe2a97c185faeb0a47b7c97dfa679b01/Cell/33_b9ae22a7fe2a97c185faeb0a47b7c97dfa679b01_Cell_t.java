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
	final Position _position;
 	int _currentGeneration;
	HashMap<Integer, Boolean> _lastTwoGenerations;
 
 	/* (non-Javadoc)
 	 * @see ex1.Living#getStatus(int)
 	 */
	
	public Cell(int x, int y, int rowLength){
		_position = new Position(x, y, rowLength);
		_currentGeneration = -1;
		_lastTwoGenerations = new HashMap<Integer,Boolean>();
	}
	
 	@Override
	public boolean getStatus(int generation) throws CellException {
 		if (generation > _currentGeneration)
 			throw new CellNotYetReady();
 		if (generation < _currentGeneration - 1)
 			throw new CellTooOld();
 		
 		if (! (_lastTwoGenerations.containsKey(generation)))
 			throw new CellUnknown();
 		
 		return _lastTwoGenerations.get(generation);
 	}
 
	public void update(int generation, boolean status) throws CellException{
		if (generation != _currentGeneration + 1)
			throw new CellUnknown();
		
		_currentGeneration = generation;
		_lastTwoGenerations.put(generation, status);
		_lastTwoGenerations.remove(generation - 2);
	}
 	
 }
