 package binarySearch;
 
 import java.util.Arrays; 
 
 public class BinarySearch {
 	
	public int[] numbers = new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}; 
 	
 	/**
 	 * @param num
 	 * @param key
 	 * 
 	 * Function recursively searches sorted array of integers, finding the specific number (key).
 	 * Search looks at the midpoint of array, checking to see if midpoint is number being sought,
 	 * if not, depending of whether the sought number is greater than, or less than, the midpoint
 	 * the function copies the upper, or lower, half of the array and passes it into a recursive 
 	 * function call.
 	 * 
 	 */
 	public int performSearch(int[] num, int key){
 		if(num.length == 0){
 			System.out.println("Array empty"); 
 			return 0; 
 		}else{
 			int mid; 
 			int number=0; 
 			mid = (num.length)/2; 
 			if(key == num[mid]){
 				number =  num[mid]; 
 				System.out.println("Found the number " + number); 
 				return number; 
 			}else if((key < num[mid]) && num.length > 1){
 				num = Arrays.copyOfRange(num, 0, mid); 
 				System.out.println("Low Range: " + Arrays.toString(num)); 
 				return performSearch(num, key); 
 			}else if((key > num[mid]) && num.length > 1){
 				num = Arrays.copyOfRange(num, mid, num.length); 
 				System.out.println("High Range: " + Arrays.toString(num)); 
 				return performSearch(num, key); 
 			}else{
 				System.out.println("Number does not exist in array."); 
 				return 0; 
 			}
 			//return number; 
 		}
 		
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		int key = 22; 
 		BinarySearch bs = new BinarySearch(); 
 		int index = bs.performSearch(bs.numbers, key); 
 		System.out.println("Number " + index);
 	}
 
 }
