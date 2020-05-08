 public class Heap {
   private Node[] Heap; // Pointer to the heap array 
   private int size; // Maximum size of the heap 
   private int n; // Number of elements now in the heap 
   
   // Constructor - Done
   public Heap(Node[] q, int x, int y)
   {
       Heap = q;
       size = x;
       n = y;			
   }		
   
   public void enqueue(int Object_ID, int priority)
   {
 	  if(n != size)
 	  {
 		  Node node = new Node();
 		  node.id = Object_ID;
 		  node.priority = priority;
 		  
 		  insert(node);
 		  
 		  buildheap();
 	  }
   }
   
   // Insert value into heap
   public void insert(Node q)
   {
       Heap[n] = q;
       n = n + 1;
   }
   
   // Done
   public int dequeue()
   {			
 	  if(n != 0)
 	  {
       	Node root = removemax();
       
         siftdown(0);
 
         return root.id;
       }
       else
 	  {
         return -1;
 	  }
       
   }
   
   // Remove maximum value - Done
   public Node removemax()
   {
       Node root = Heap[0];
       
       n = n - 1;
       Heap[0] = Heap[n];
       
       return root;
   }
   
   // Remove value at specified position - Done
   public Node remove(int x)
   {
       Node root = Heap[x];
       
       n = n - 1;
       Heap[x] = Heap[n];
       
       return root;			
   }
   
   // Done
   public void changeWeight(int Object_ID, int new_priority)
   {
       boolean didFind = false;
      for(int i = 0; i < n; i++)
       {
		  Node x = Heap[i];

           if(x.id == Object_ID)
           {
               didFind = true;
               x.priority = new_priority;
           }
       }
       
       if(!didFind)
       {
           //doneGoofed
       }
 
 	  buildheap();
   }
   
   // Put an element in its correct place ~ max-heapify(int) - Done 
   private void siftdown(int x) 
   {
         int bigChild;
         boolean done = false;		      
         Node root = Heap[x]; 
         
         while((x < n/2) && !done)       
         {		    	  
             int left = 2*x+1;
             int right = left+1;
                                            
             if(right < n && Heap[left].priority < Heap[right].priority)
             {			        	  
                 bigChild = right;
             }
             else
             {		        	 
                 bigChild = left;
             }
                                            
             if(root.priority >= Heap[bigChild].priority)
             {
                   done = true;		        	  
             }
             else
             {                      
                 Heap[x] = Heap[bigChild];
                 x = bigChild;  
             }         
         } 
         
         Heap[x] = root;           
   }
   
   // Return current size of the heap - Done
   public int heapsize() 
   {
       return n;
   }
   
   // TRUE if pos is a leaf position - Done
   public boolean isLeaf(int x)  
   {
       boolean leaf = false;
       
       if(leftChild(x) == -1 && rightChild(x) == -1 )
       {
           leaf = true;
       }
       
       return leaf;
   }
   
   // Return position for left child - Done
   public int leftChild(int x)
   {
       int retval = -1;
   
       if((2*x) > n)
       {
           retval = 2*x;				
       }
           
       return retval;		
   }
   
   // Return position for right child - Done
   public int rightChild(int x)
   {
       int retval = -1;
       
       if((2*x + 1) > n)
       {
           retval = 2*x + 1;				
       }
                       
       return retval;	
   }
   
   // Return position for parent - Done
   public int parent(int x)
   {
       int retval = -1;
       
       if(x != 0)
       {
           retval = x/2;				
       }
           
       return retval;
   }
   
   // Heapify contents of heap
   public void buildheap()
   {
       for(int i = n-1; i >= 0; i--)
       {
           siftdown(i);
       }
   }
   
   // Return contents of heap
   public String toString() {
     String output = "[ ";
     
     for (int i = 0; i < n; i++) {
       output += "[ id = " + Heap[i].id + ", priority = " + Heap[i].priority + " ]";
       if (i != n - 1) output += ", ";
     }
     
     return output + " ]";
   }
   
   // Getter for heap.
   public Node[] getHeap() {
     return Heap;
   }
 }
 
