package guibin.zhang.leetcode.tree;
 
 /**
  *
  * https://d396qusza40orc.cloudfront.net/algs4partI/slides/24PriorityQueues.pdf
  * 
  * Binary heap: Array representation of a heap-ordered complete binary tree.
  * Basically binary heap is a complete binary tree.
  * 
  * Binary heap has features:
  * 1. Parent's key >= children's keys.
  * 
  * Array representation of the MaxHeap
  * 1. index starts from 1
  * 2. Take nodes in **level** order.
  * 3. No explicit link needed.
  * 
  * Attribute:
  * 1. Largest key is a[1], which is the root of the binary tree.
  * 2. Parent of node K is at K/2.
  * 3. Children of node K are at 2K and 2K + 1.
  * 
  * How to eliminate the violation:
  * 1. Exchange key in child with key in parent.
  * 2. Repeat until heap order restored.
  * 
  * @author Guibin Zhang <guibin.beijing@gmail.com>
  */
 public class MaxPriorityQueue<T extends Comparable<T>> {
     
     private T[] pq;
     private int N;
    private int capacity;
     
     public MaxPriorityQueue(int capacity) {
        pq = (T[]) new Comparable[capacity + 1];//Since the heap index starts from 1 instead of 0;
        this.capacity = capacity;
     }
     
     /**
      * Eliminate the violation from children to parent, from bottom up.
     * @param K
      */
     private void swim(int K) {
         //When parent less than child, exchange root with child. K/2 is parent, K is child
         while (K > 1 && pq[K/2].compareTo(pq[K]) < 0) {
            swap(K/2, K);
             K = K/2;
         }
     }
     
     /**
      * Eliminate violation from parent to children, from top down.
      * 
      * @param K 
      */
     private void sink(int K) {
        while (K * 2 < N) {
            int j = 2*K;
            //Find out the bigger child
            if (j < N && pq[j].compareTo(pq[j + 1]) < 0) j++;
            //If root < children
            if (pq[K].compareTo(pq[j]) < 0) {
                //Exchange root with the bigger child
               swap(K, j);
                //Keep on sink down
                K = j;
            } else {
                break;
            }
        } 
     }
     
    public void insert(T x) throws Exception {
        if (N >= capacity) throw new Exception("Exceed capacity.");
        
         pq[++N] = x;
         swim(N);
     }
     
    public T delMax() throws Exception {
        if (N == 0) throw new Exception("Delete the empty heap.");
        
         T max = pq[1];
         //Move the last element to the root
        swap(1, N --);
         sink(1);
         //Prevent memroy leak
         pq[N + 1] = null;
         return max;
     }
     
     public boolean isEmpty() {
         return N == 0;
     }
     
    private void swap(int i, int j) {
         T tmp = pq[i];
         pq[i] = pq[j];
         pq[j] = tmp;
     }
 }
