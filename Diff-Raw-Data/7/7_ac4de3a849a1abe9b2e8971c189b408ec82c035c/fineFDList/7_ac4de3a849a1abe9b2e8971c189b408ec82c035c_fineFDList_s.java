 package edu.buffalo.cse605.finelist.src;
 
import edu.buffalo.cse605.list.src.*;
 
 public class fineFDList<T> {
 	private fineElement<T> head;
 	private fineCursor<T> cursor;
 	
 	
 	
 	public fineFDList(T val) {
 		this.head = new fineElement<T>(val, null, null);
 		
 	}
 	
 	
 	public fineElement<T> head() {
 		return this.head;
 	}
 	
 	public fineCursor<T> freader(fineElement<T> start) {
 		if (cursor == null) {
 			cursor = new fineCursor<T>(start);
 		}
 		// TODO: Logical Fallacy, make sure I can call reader multiple times
 		return this.cursor;
 	}
 }
