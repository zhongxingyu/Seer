 package poker.manager_player;
 
 import java.util.Iterator;
 
 import lombok.Getter;
 
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

 /* TODO:: Suggests poss error. Not so relevant for circular list, but still (esp if empty?)
 
 "It: Iterator next() method can't throw NoSuchElementException (IT_NO_SUCH_ELEMENT)
 This class implements the java.util.Iterator interface.  However, its next() method is not
 capable of throwing java.util.NoSuchElementException.  The next() method should be changed
 so it throws NoSuchElementException if is called when there are no more elements to return"
  */
 	
 	
 	
 
 
 
 
 
 
 
 /*
  * A List Class (backed by an Array) designed for poker games.
  * 
  * This class differs from an ordinary list in that it iterates from the dealer rather
  * than the beginning of the list.
  * 
  * Operations:
  * add() adds a given object to the end of the list
  * newDealer() increments the position of the dealer
  * iterator() iterates through the items from the dealer. Items (players) can be removed
  * from the list using this function.
  */
 
 public class CircularArrayList<T> implements Iterable<T> {
 	
 	private int numItems;	//not Lomboked as called different name - consider renaming
 	private T[] players;
 	@Getter private int dealer;
 	private static final int INIT_LENGTH = 10;
 	@Getter private int index;
 	
 	@SuppressWarnings("unchecked")
 	public CircularArrayList(){
 		
 		players = (T[]) new Object[INIT_LENGTH];
 		dealer = 0;
 		numItems = 0;
 		index = 0;
 	}
 	
 	public T get(int i){
 		if(i < numItems){
 			return players[i];
 		}else{
 			throw new IndexOutOfBoundsException();
 		}
 	}
 	
 	public void add(T player){
 		
 		if(numItems < players.length){
 			players[numItems] = player;
 			numItems++;
 		}else{
 			@SuppressWarnings("unchecked")
 			T[] temp = (T[]) new Object[players.length * 2];
 			for(int i = 0; i < players.length; i++){
 				temp[i] = players[i];
 			}
 			temp[numItems] = player;
 			players = temp;
 			numItems++;
 		}
 		
 	}
 	
 	public T getTheDealer(){
 		return players[dealer];
 	}
 	
 	public void newDealer(){
 		if(dealer == numItems - 1){
 			dealer = 0;
 		}else{
 			dealer++;
 		}
 	}
 
 	public void setIndex(){
 		if(dealer == numItems - 1){
 			index = 0;
 		}else{
 			index = dealer + 1;
 		}
 	}
 	
 	private void reSizeArray(int i){
 		@SuppressWarnings("unchecked")
 		T[] temp = (T[]) new Object[players.length];
 		for(int k = 0; k < i; k++){
 			temp[k] = players[k];
 		}
 		for(int k = i + 1; k < numItems; k++){
 			temp[k - 1] = players[k];
 		}
 		players = temp;
 		numItems--;
 	}
 	
 	public boolean remove(T t){
 		for(int i = 0; i < numItems; i++){
 			if(players[i].equals(t)){
 				if(dealer <= i){
 					reSizeArray(i);
 					if(dealer == numItems - 1){
 						newDealer();
 					}
 					return true;
 				}else{
 					reSizeArray(i);
 					dealer--;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public Iterator<T> iterator() {
 		
 		setIndex();
 		
 		Iterator<T> iterator = new Iterator<T>(){
 			int counter = 0;
 			
 			@Override
 			public boolean hasNext(){
 				return (counter != numItems);
 			}
 			
 			@Override
 			public T next(){		// TODO: see note
 				if(index == numItems - 1){
 					index = 0;
 					counter++;
 					return players[numItems - 1];
 				}else{
 					index++;
 					counter++;
 					return players[index - 1];
 				}
 			}
 
 			@Override
 			public void remove() {
				throw new NotImplementedException();
 			}
 		};
 		return iterator;
 	}
 	
 	public int getSize(){
 		return numItems;
 	}
	
	/*public int getDealer() {
		return dealer;
	}
	
	public int getIndex(){
		return index;
	}*/
 
 }
