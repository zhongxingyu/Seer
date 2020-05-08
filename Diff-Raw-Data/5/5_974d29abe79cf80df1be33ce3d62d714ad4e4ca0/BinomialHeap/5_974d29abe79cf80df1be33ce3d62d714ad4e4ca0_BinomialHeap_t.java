 /**
 
  * BinomialHeap
  *
  * An implementation of lazy binomial heap over non-negative integers.
  */
 public class BinomialHeap
 {
 	private LinkedList list;
 	private Tree tree;
 	private int size;
 	private int count_links;
     private int tree_depth;
 
 	private static class BinomialTree {
 		// mid is only important if left == right == null
 		BinomialTree next;
 		BinomialTree child;
 		int value;
 		
 		BinomialTree(BinomialTree next, BinomialTree child) {
                         this.next = next.next == null ? null : next;
 		        this.value = next.value;
 			assert(next.value <= child.value);
 			this.child = child;
 		}
 		
 		BinomialTree(int value) {
 			this.next = this.child = null;
 			this.value = value;
 		}
 	}
 	
 	private static class LinkedList {
 		int degree;//tree's degree
 		BinomialTree tree;
 		LinkedList next;
 		
 		LinkedList(BinomialTree tree, int degree, LinkedList next) {
 			// degree should be tree's degree
 			this.degree = degree;
 			this.tree = tree;
 			this.next=next;//changed
 		}
 	}
 	
 	private static class Tree {
 		Tree left;
 		LinkedList center;
 		Tree right;
 		
 		Tree(Tree left, LinkedList center, Tree right) {
 			this.left = left;
 			this.center = center;
 			this.right = right;
 		}
 	}
 	
 	
 
    /**
     * public boolean empty()
     *
     * precondition: none
     *
     * The method returns true if and only if the heap
     * is empty.
     *
     */
     public boolean empty()
     {
    		return size == 0;
     }
 
    /**
     * public void insert(int value)
     *
     * Insert value into the heap
     *
     */
     public void insert(int value)//mor
     {
     	BinomialTree t=new BinomialTree(value);
     	if (list.tree.value> value){
     		list = new LinkedList(t,0,list);
     	}
     	else{
     		LinkedList l=new LinkedList(t,0,list.next);
     		list=new LinkedList(list.tree,list.degree,l);
     	}
     	size++;
     }
 
    /**
     * public void deleteMin()
     *
     * Delete the minimum value.
     * Return the number of linking actions that occured in the process.
     *
     */
     public int deleteMin()//ariel
     {
         int max_deg = 0;
         int link_count = 0;
         size--;
         for(int s=size;s>0;s = s >> 2) max_deg++;
         BinomialTree[] target = new BinomialTree[max_deg];
         
         BinomialTree cur = list.tree;
        for(int deg=list.degree;deg>0;deg--,cur=cur.next)
             target[deg-1] = cur.child;
         assert(cur == null);
 
         link_count += link_list(target, list.next);
         link_count += link_tree(target, tree);
 
         tree = null;
         tree_depth = 0;
         list = collect_target(target);
      	return link_count;
     }
 
     private int link_list(BinomialTree[] target, LinkedList list) {
         int link_count = 0;
 
         while(list != null) {
             if(target[list.degree] == null)
                 target[list.degree] = list.tree;
             else {
                 link_count++;
                 target[list.degree] = (list.tree.value >
                                        target[list.degree].value) ?
                       new BinomialTree(target[list.degree], list.tree)
                     : new BinomialTree(list.tree, target[list.degree]);
             }
             list = list.next;
         }
 
         return link_count;
     }
 
     private int link_tree(BinomialTree[] target, Tree tree) {
         if(tree == null) return 0;
         // I don't want to do stack-recursion here - may overflow
         Tree[] stack = new Tree[tree_depth];
         int tree_idx = 0;
         int link_count = 0;
         stack[tree_idx] = tree;
         while(stack[tree_idx].left != null) {
             stack[tree_idx+1] = stack[tree_idx].left;
             tree_idx++;
         }
         
         while(true) {
             link_count += link_list(target, stack[tree_idx].center);
 
             if(stack[tree_idx].right != null) {
                 stack[tree_idx] = stack[tree_idx].right;
                 while(stack[tree_idx].left != null) {
                     stack[tree_idx+1] = stack[tree_idx].left;
                     tree_idx++;
                 }
             } else if(tree_idx == 0)
                 return link_count;
             else
                 tree_idx--;
         }
     }
 
     private LinkedList collect_target(BinomialTree[] target) {
         BinomialTree min_tree = null;
         int min_deg = -1;
         LinkedList list = null;
         for(int deg=0;deg<target.length;deg++) {
             BinomialTree t = target[deg];
             if(t == null) continue;
             if(min_tree == null) {
                 min_tree = t;
                 min_deg = deg;
             } else if(t.value < min_tree.value) {
                 list = new LinkedList(min_tree, min_deg, list);
                 min_tree = t;
                 min_deg = deg;
            } else list = new LinkedList(t, deg, list);
         }
 
         if(min_tree != null)
             list = new LinkedList(min_tree, min_deg, list);
 
         return list;
     }
 
    /**
     * public int findMin()
     *
     * Return the minimum value
     *
     */
     public int findMin()//mor
     {
     	return list.tree.value;
     }
 
    /**
     * public void meld (BinomialHeap heap2)
     *
     * Meld the heap with heap2
     *
     */
     public void meld (BinomialHeap heap2)//mor-heap2 supposse to be unvalid after meld
     {
     	if(heap2.list.tree.value<list.tree.value){
     		list=new LinkedList(heap2.list.tree,heap2.list.degree,list);
     		BinomialTree t=heap2.list.next.tree;
     		heap2.list=new LinkedList(t,heap2.list.next.degree,heap2.list.next.next);
     	}
     	tree=new Tree(tree,heap2.list,heap2.tree);
     	if(heap2.tree_depth>tree_depth){
     	tree_depth=heap2.tree_depth+1;
     	}
     	else{
     		tree_depth++;
     	}
     	size+=heap2.size;
     }
 
    /**
     * public int size()
     *
     * Return the number of elements in the heap
     *
     */
     public int size()
     {
     	return size;
     }
 
 
    /**
     * public static int sortArray(int[] array)
     *
     * Sort an array by using insert and deleteMin actions on a new heap.
     * Return the number of linking actions that occurred in the process.
     *
     */
     public static int sortArray(int[] array)//mor
     {
     	BinomialHeap Bheap=new BinomialHeap();
     	for(int i=0;i<array.length;i++){
     		Bheap.insert(array[i]);
     	}
     	for(int i=0;i<array.length;i++){
     		array[i]=Bheap.deleteMin();
     	}
         return Bheap.count_links; 
     }
 
    /**
     * public int[] treesRanks()
     *
     * Return an array containing the ranks of the trees that represent the heap
     * in ascending order.
     *
     */
     public int[] treesRanks()//ariel
     {
         int[] arr = new int[42]; //
         return arr; //	 to be replaced by student code
     }
 
 }
