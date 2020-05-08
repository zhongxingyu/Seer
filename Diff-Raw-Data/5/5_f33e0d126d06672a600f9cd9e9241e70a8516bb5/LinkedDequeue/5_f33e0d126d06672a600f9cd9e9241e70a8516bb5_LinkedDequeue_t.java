 // File LinkedDequeue.java 
 /** 
  *  This class represents a Queue datatype implemented using a singly-linked
  *  list with appropriate operations.
  *
  * @author: Henry Leitner
  * @version: Last modified on April 11, 2013
  * Implements a Queue as a linked-list
  */
 
 public class LinkedDequeue
 {
     private QueueNode rear;
     private QueueNode front;
     private int count;
     
     /**
      *  The QueueNode class is an inner class implemented to model a queue node;
      *  it can contain an Object type of data, and also holds the link to the
      *  next node in the queue.  If there are no other nodes, the link will be null.
      */
      class QueueNode        // an inner class
      {

        private Object item;
        private QueueNode link;
    }
 
     /**
      *  This constructor for the class will set up the needed instance variables
      *  which begin with no nodes present and thus are set to null.
      */
     public LinkedQueue ()
     {
         rear = front = null;
         count = 0;
     }
 
     /**
      *  This method will construct a new QueueNode and add it onto the rear
      *  of the queue (standard FIFO behavior). If it is the first node added into
      *  the queue, both front and rear will reference it, otherwise it is added
      *  using the rear variable.  The node counter is also updated.
      *
      *  @param   x     The Object to be added as part of a new QueueNode
      */
     public void add (Object x)
     {
         QueueNode temp = new QueueNode();
         temp.item = x;
         temp.link = null;
 
         if (rear == null) front = rear = temp;
         else
         {
             rear.link = temp;
             rear = temp;
         }
         count++ ;
     }
 
     /**
      *  This method will test for an empty queue and return a boolean result.
      *
      *  @return     true for an empty list; false if the queue contains QueueNodes.
      */
     public boolean empty()
     {
         return ( count == 0 );
     }
 
     /**
      *  This method will evaluate and return the current size of the queue.
      *
      *  @return     An int describing the current number of nodes in the queue
      */
     public int size()
     {
         return count;
     }
     
     /**
      *  This method will remove an item from the front of the queue.  
      *  In doing so, the queue variables are reset to detach the node,
      *  and the Object which it contains is then returned.  The queue
      *  counter is also updated to reflect the removal.
      *
      *  @return     The Object which was just removed from the queue.
      */
     public Object delete ()
     {
        if ( empty() ) return null;
        else
        {
             Object tempItem = front.item;
             front = front.link;
             if (front == null)   rear = null;
             count -- ;
             return tempItem;
         }
     }
 }
