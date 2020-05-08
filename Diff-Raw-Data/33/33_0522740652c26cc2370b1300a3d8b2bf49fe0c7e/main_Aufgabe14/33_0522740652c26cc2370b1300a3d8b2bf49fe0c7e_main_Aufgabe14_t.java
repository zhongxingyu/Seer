 
 public class main_Aufgabe14 {
     
 	public static void main(String[] args) {
 		int[] array= {4, 9, 3, 6, 2 , 1, 3, 7, 2, 1, 2, 20};
 		int[] array2= {1, 9, 3, 6, 2 , 1, 3, 7, 2, 1, 3, 5, 1, 15, 2, 8};
 		int start = 0;
 		
 		
 		StaightSort(array, start, array.length);
 		printArray(array);
 		StaightSort(array2, start, array2.length);
 		printArray(array2);
 		printArray(mergeSortedArrays(array, array2));
 		
		
 	}
 	
 	public static void StaightSort(int[] array, int start, int end){
 		int pos = start;
 		int tempValue;
 		int tempPos;
 				
 		while (start < (end - 1)) {
 			tempValue = array[start];
 			tempPos = start;
 			for (pos = start; pos < end; ++pos){				
 				if (array[pos] < tempValue ) {
 				  tempValue = array[pos];
 				  tempPos = pos;
 				}
 			}
 			if (array[tempPos] != array[start]) {
 				array[tempPos] = array[start];
 				array[start] = tempValue;
 			}
 			++start;
 		}
 	}
 	
 	public static void binSort(int[] array) {
 		int temp;
 		int[] newArray = new int[array.length];
		int[] leftSortedArray = new int[2];
 		int left = 0, right = 2;
 		int i;
 		
 		// generieren von 2er soriteren arrays
 		for (i = 0; i < array.length ; ++i) {
 			temp = array[i];
 			if (temp > array[++i]){
 				array[i - 1] = array[i];
 				array[i] = temp;
 			}
			mergeSortedArrays()
 		}
 		i = 0;
 		//Fixme
 		newArray[0] = array[0];
 		newArray[1] = array[1];
 		
 
 			
 		while (i < array.length) {
 			if (array[left] > array[right]) {
 				newArray[i++] = array[right++];				
 			} else {
 				newArray[i++] = array[left++];				
 			}
 		}
 	}
 	
 	public static int[] mergeSortedArrays(int[] array1, int[] array2) {
 		int[] newArray;
 		int i1 = 0, i2 = 0;
 		
 		int maxLenght = array1.length + array2.length;
 		newArray = new int[maxLenght];
 		
 		for (int i = 0; i < maxLenght; ++i) {
             try {
 			    if (array1[i1] < array2[i2]) {
 			    	newArray[i] = array1[i1++];
 			    } else {		    	
 			    	newArray[i] = array2[i2++];
 			    }
 	    	} catch (ArrayIndexOutOfBoundsException e) {
 	    		
				if (array1.length > i1) {
					newArray[i] = array1[i1++];
					
				} else
				
				if (array2.length > i2) {
					newArray[i] = array2[i2++];
				}
 	    	}
 		}		
 		return newArray;
 	}
 	
 	private static void printArray(int[] myArray){
  		System.out.print("[");
 		for (int i = 0; i < myArray.length; ++i) {
 			 System.out.printf("%1$3d", myArray[i]);
 		}
 		System.out.println(" ]");
 	}
 	
 }
