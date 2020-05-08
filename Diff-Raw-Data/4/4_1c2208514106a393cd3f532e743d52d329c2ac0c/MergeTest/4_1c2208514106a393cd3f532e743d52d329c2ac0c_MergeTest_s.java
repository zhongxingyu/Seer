 package mergesort;
 
 import java.util.Random;
 
 public class MergeTest {
 
 	
 	public static void main(String[] args) {
 		
 		System.out.println("Test fr M = 10, 100, 1000 und n = 10, 100, 1000, 100000 soweit sinnvoll.");
 		
		int[] ns = {10, 100, 1000};
		int[] ms = {10, 100, 1000, 100000};
 		
 		Random gen = new Random(System.currentTimeMillis());
 		Mergesort<Integer> merge = new Mergesort<Integer>();
 		MMergesort<Integer> mmerge;
 		
 		for(int n : ns){
 			Integer[] sort = new Integer[n];
 			Integer[] save = new Integer[n];
 			for(int i = 0; i<n; i++){
 				sort[i] = gen.nextInt();
 			}
 			System.out.println("---- n = "+n+" -----------");
 			System.arraycopy(sort, 0, save, 0, sort.length);
 			merge.sort(sort);
 			System.out.print("Mergesort:"+merge.getStatistic()+"\n");
 			for(int m : ms){
 				System.arraycopy(save, 0, sort, 0, sort.length);
 				mmerge = new MMergesort<Integer>(m);
 				mmerge.sort(sort);
 				System.out.print("MMergesort(m="+m+"):"+mmerge.getStatistic()+"\n");
 			}
 		}
 			
 		
 		
 	}
 
 }
