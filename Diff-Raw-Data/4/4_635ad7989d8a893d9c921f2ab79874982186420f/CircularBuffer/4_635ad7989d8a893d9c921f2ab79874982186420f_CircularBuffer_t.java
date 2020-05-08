 package com.example.collabtext;
 
 import java.util.Vector;
 
 import android.util.Log;
 
 //This is a templated class for storing undo/redo in a circular buffer
 //I haven't tested it yet for improper indexing but I think it will work
 
 public class CircularBuffer<T> {
 	
 	final int MAXSIZE = 100;
 	
 	private Vector<T> buffer;
 	private int head_ptr;
 	private int local_ptr;
 	
 	
 	CircularBuffer(){
 		this.head_ptr = 0;
 		this.local_ptr = 0;
 		buffer = new Vector<T>();
 		buffer.setSize(MAXSIZE);
 		//Log.d("constructor", String.valueOf(buffer.size()));
 	}
 	
 	public void add(T addedObject){
 		//Log.d("the size of the buffer", String.valueOf(buffer.size()));
 		buffer.set(head_ptr, addedObject);
 		//Log.d("ADDED", "Object");
		head_ptr++;
 		if(head_ptr == MAXSIZE){
 			head_ptr = 0;
 		}
		
 		local_ptr = head_ptr;
 	}
 	
 	public T getUndo(){	
 		local_ptr--;
 		if(local_ptr == -1){
 			local_ptr = MAXSIZE-1;
 		}
 		Log.w("Header_ptr:", String.valueOf(head_ptr));
 		Log.w("local_ptr:", String.valueOf(local_ptr));
 		if(local_ptr != head_ptr){
 			//if(buffer.(local_ptr)){
 				//local_ptr++;
 			//}
 			return buffer.get(local_ptr);
 		}
 		else{
 			local_ptr++;
 			return null; 
 		}
 	}
 
 	public T getRedo(){
 		if(local_ptr != head_ptr){
 			local_ptr++;
 			if(local_ptr == MAXSIZE){
 				local_ptr = 0;
 			}
 			return buffer.get(local_ptr);
 		}
 		else{
 			return null; 
 		}
 	}
 	
 }
