 import java.util.Random;
 
 
 public class measurements {
 	public static void main(String[] args){
 		double[][] resultArray=new double[10][4];
 		for(int m=10000;m<100001;m=m+10000){
 			int[][] array10=new int[10][4];
 			for(int i=0;i<10;i++){
 				array10[i]=measure(m);
 			}
 			int[] array=new int[4];
 			array[0]=m;
 			for(int i=0;i<10;i++){
 				array[1]+=array10[i][1];
 				array[2]+=array10[i][2];
 				array[3]+=array10[i][3];
 			}
 			double[] dArray=new double[4];
 			dArray[0]=m;
 			for(int i=1;i<4;i++){
 				dArray[i]=((double)array[i])/10;//is it really a double
 			}
 			resultArray[m/10000-1]=dArray;
 		}
 		for(int i=0; i<10;i++){
 			for(int j=0;j<4;j++){
 				System.out.println(resultArray[i][j]);
 			}
 		}
 	}
 
 	private static int[] measure(int m){
 		Random generator=new Random();
 		int [] resultsArray=new int[4];
 		resultsArray[0]=m;
 		int[] array=new int[m];
 		for(int i=0;i<m;i++){
 			array[i]=generator.nextInt(100000);
 		}
 		resultsArray[1]=BinomialHeap.sortArray(array);//don't know how to call it
 		resultsArray[2]=BinomialHeap.sortArray(array);
 		BinomialHeap heap=new BinomialHeap();
 		for(int i=0;i<array.length;i++){
 			heap.insert(array[i]);
 		}
 		for(int i=array.length-1;i>=0;i--){
 			array[i] = heap.findMin();
 		}
 		resultsArray[3]=BinomialHeap.sortArray(array);
 		return resultsArray;
 	}
 }
