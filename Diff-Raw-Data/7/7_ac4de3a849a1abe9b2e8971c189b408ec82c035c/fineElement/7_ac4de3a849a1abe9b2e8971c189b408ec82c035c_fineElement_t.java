 package edu.buffalo.cse605.finelist.src;
 
 import java.util.concurrent.locks.*;
 
 
 public class fineElement<T> {
 	private T val;
     private fineElement<T> next, prev;
     public final Lock nextlock=new ReentrantLock();
     public final Lock prevlock=new ReentrantLock();
     
     
     // Constructor
     public fineElement(T val) {
     	this(val, null, null);
     }
 
     // Constructor
     public fineElement(T val, fineElement<T> prev, fineElement<T> next) {
     	if ( val == null ) {
     		// Throw Exception
     		throw new Error("can`t create element");
     	}
     	// Assign Value
     	this.val = val;
     	// Assign Prev and Next
     	if ( prev != null && next != null ) {
 	    	this.prev = prev;
 	    	this.next = next;
     	} else if ( prev == null && next == null ) {
     		this.prev = this;
     		this.next = this;
     	}
       
     }
     
     // GET next Element
     public fineElement<T> next() {
     	return this.next;
     }
     
     // GET prev Element
    public  fineElement<T> prev() {
     	return this.prev;
     }
     
     // Add an element after this element
     public  boolean addAfter(fineElement<T> el) {
     	
              el.next=this.next;
              el.prev=this;
              if(this.prev.prev==this){//having two elements
             	 this.prev.prev=el;
             	 this.next=el;
              }
              else if(this.prev==this){//having one elements
             	 this.prev=el;
             	 this.next=el;
             	 
              }
              else{
             	 this.next.prev=el;
             	 this.next=el;
              }
     	   
     	//this.next = el;
     	//el.prev = this;
     	return true;
     }
     
     // Add an element before this element
    public boolean addBefore(fineElement<T> el) {
     	    el.next = this;
     		el.prev=this.prev;
     		if(this.next==this){
     			this.prev=el;
     			this.next=el;
     		}
     		else if(this.next.next==this){
     			this.next.next=el;
     			this.prev=el;
     		}
     		else{
     			this.prev.next=el;
     			this.prev=el;
     		}
     	
     
     	return true;
     }
      
     public boolean delete(){
     		System.out.println("delete "+this.val);
     		 if(this.next==this.prev && this.next!=this){//two elements in the list
     			this.next.next=this.prev;
     			this.next.prev=this.prev;
     			
     		}
     		else if(this.prev==this.next.next){//three elements in the list
     			this.next.prev=this.prev;
     			this.prev.next=this.next;
     			
     		}
 
     		else{this.next.prev=this.prev;
     		this.prev.next=this.next;
     		
     		}
 
     		
     		return true;
     		   }
     
     // Helper
     public String toString() {
     	return "v:" + this.val + ",prev:" + this.prev.val + ",next:" + this.next.val;
     }
 
 	//@Override
 	public T value() {
 		return this.val;
 	}
 }
