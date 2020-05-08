 import java.util.*;
 
 public class WeightedQuickUnion {
     private int[] Id;    
     private int[] Sz;    
     private int Count;   
 
     public WeightedQuickUnion(int n) {
         Count = n;
         Id = new int[n];
         Sz = new int[n];
 
         for (int i = 0; i < n; i++) {
             Id[i] = i;
             Sz[i] = 1;
         }
     }
 
     public int Count() {
         return Count;
     }
 
     public int Find(int p) {
 	int root = p; 
         
 	while (root != Id[root])
             root = Id[root];
 
	int t;
 	while (p != Id[p])
 	{
	    t = p;
 	    p = Id[p];
	    Id[t] = root;
 	}
 
         return root;
     }
 
     public boolean IsConnected(int p, int q) {
         return Find(p) == Find(q);
     }
 
     public void Union(int p, int q) {
         final int i = Find(p);
         final int j = Find(q);
 
         if (i == j) 
 	    return;
 
         if (Sz[i] < Sz[j]) 
 	{
 	    Id[i] = j; Sz[j] += Sz[i]; 
 	}
         else
 	{                 
 	    Id[j] = i; Sz[i] += Sz[j]; 
 	}
 
         Count--;
     }
 
     @Override
     public String toString()
     {
 	return "Trees: " + Count + 
 	    "\nArray: " + Arrays.toString(Id) + 
 	    "\nWeight: " + Arrays.toString(Sz) + ";";
     }
 
     //static
 
     public static void main(String[] args) {
 	Worse();
     }
 
     public static void Worse(){
         int n = 10;
 	WeightedQuickUnion alg = new WeightedQuickUnion(n);
 
 	alg.Union(0, 1);
 	System.out.println("Pair: 0 1 \n" + alg);
 	alg.Union(2, 3);
 	System.out.println("Pair: 2 3 \n" + alg);
 	alg.Union(4, 5);
 	System.out.println("Pair: 4 5 \n" + alg);
 	alg.Union(6, 7);
 	System.out.println("Pair: 6 7 \n" + alg);
        	alg.Union(0, 2);
 	System.out.println("Pair: 0 2 \n" +alg);
 	alg.Union(0, 4);
 	System.out.println("Pair: 0 4 \n" +alg);
 	alg.Union(4, 6);
         System.out.println("Pair: 4 6 \n" +alg);
     }
 }
 
