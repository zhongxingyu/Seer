 package data_structures.implementation;
 
 import data_structures.Sorted;
 
 public class FineGrainedList<T extends Comparable<T>> implements Sorted<T> {
 	
 
 	private FineElement<T> head;
 	private FineElement<T> tail;
 
 	public FineGrainedList(){
 		this.head=new FineElement<T>();
 		this.head.setHead();
 		this.tail=new FineElement<T>();
 		this.tail.setTail();
 		this.head.next=this.tail;
 	}
 
 	public void add(T t) {
 		FineElement<T> newEle = new FineElement<T>(t);
 
 		if(Sorted.DEBUG){
 			System.out.println(this.toString());
 			System.out.println("New element"+t);
 		}
 
 		if(t == null)
 			return;
 
 		head.lock(); //this is because, we cannot permit that the head changes between this and the
 					// next instruction.
 		FineElement<T> pred=head;
 		try{
 			FineElement<T> curr=(FineElement<T>) pred.next;
 			curr.lock();
 			try{
 				while(newEle.compareTo(curr)<0){
 					pred.unlock();
 					pred=curr;
 					curr=(FineElement<T>) curr.next;
 					curr.lock();
 				}
 				newEle.next=curr;
 				pred.next=newEle;
 				return;
 			}finally{
 				curr.unlock();
 			}	
 		}finally{
 			pred.unlock();
 		}
 	}
 
 	public void remove(T t) {
 		FineElement<T> newEle = new FineElement<T>(t);
 
 		if(Sorted.DEBUG){
 			System.out.println(this.toString());
 			System.out.println("Remove element"+t);
 		}
 
 		if(t == null)
 			return;
 
 		head.lock();
 		FineElement<T> pred=head;
 		try{
 			FineElement<T> curr=(FineElement<T>) pred.next;
 			curr.lock();
 			try{
 				while(newEle.compareTo(curr)<0){
 					pred.unlock();
 					pred=curr;
 					curr=(FineElement<T>) curr.next;
 					curr.lock();
 				}
 				if(curr.isTail())
 					return;
 				pred.next=curr.next;
 				return;
 			}finally{
 				curr.unlock();
 			}
 			
 		}finally{
 			pred.unlock();
 		}
 	}
 
 	public String toString() {
 		String ris="";
		for(FineElement<T> curr=(FineElement<T>) this.head.next;curr.isTail();curr=(FineElement<T>) curr.next){
 			ris+="["+curr.getValue()+"]";
 			if(!curr.next.isTail())
 				ris+="->";
 		}
 		return ris; 
 	}
 }
