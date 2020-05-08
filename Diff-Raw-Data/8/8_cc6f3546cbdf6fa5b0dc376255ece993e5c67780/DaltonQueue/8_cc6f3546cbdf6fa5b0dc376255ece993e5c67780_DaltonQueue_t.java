 /*
 Authors:
 Charles Forster
 Isabella Giovannini
 Jason Shein
 Katherine Odom
 Malina Buturovic
 Derek Whang -_- hohoho
 Annelise Steele <3
 Sophia Edelstein 
  */
 
 public class DaltonQueue extends java.util.LinkedList
 {
     /*
       Author: Malina
       Function: peek_all
       Parameters: none
       Returns: String with all elements in Queue
     */
     
     public String peek_all()
     {
 	offer("end of array");
 	String input="";
 
 	while(!peek().toString().equals("end of array"))
 	    {
 		input = input + " " + this.peek();
 		offer(poll());
 	    }
 	return input;
     }
  
 
 
 
     /*
       Author: Sophia
       Function: length
     */
     public int length()
     {
 	offer("end of array"); // puts in something to define the end of the array
 	int length =0; // declares variable length =0
 	
 	while(!peek().toString().equals("end of array"))  // while loop
 	    {
 		offer(poll());
 		length++;// increase length counter
 	    }
	poll();
 	return length; // returns length variable
     }
     
     
 
     /*                                                                          
                                                           
 
     /* 
        Author: ISABELLA and malina. 
 
      */
     /*
     void ReplaceAt(int index, int rep)
     {
 	Array[index] = rep;
     }
 
 
 
     /*
       Author: Isabella
      */
     //in progress
     void sort()
     {
 	//make the queue into the array:
 	Object[] queue = new Object[this.size()]; 
 	int counter = 0;
 	while (!this.isEmpty()) //go through the queue
 	    {
 		queue[counter++]= this.poll();
 	    }
 
 	//sort the array:
 	java.util.Arrays.sort(queue);
 
 	//return it to the queue:
 	for (counter=0; counter<queue.length; counter++)
 	    {
 		this.offer(queue[counter]);
 	    }
     }
 
 
     /*
       Author: Katherine
       Function: Dump (delete everything from the queue)
     */
 
     void dump()
     {
 	 while(!this.isEmpty())
 	     {
 		this.remove();
 	     } 
     }
        
     /*
       Author: Shein
       Function: switch(x,y)
       parameters: x and y are both distances from the front of the queue
       return: nothing
     */
     void switchElem(int x, int y)
 	{
 	    java.util.Queue queue2 = new java.util.LinkedList();
 	    java.util.Queue queue3 = new java.util.LinkedList();
 	    int object1;
 
 	    for(int i=0; i<x; i++)
 		{
 		    queue2.offer(this.poll());
 		    
 		}
 	    
 	    this.poll();
 	}
 }
