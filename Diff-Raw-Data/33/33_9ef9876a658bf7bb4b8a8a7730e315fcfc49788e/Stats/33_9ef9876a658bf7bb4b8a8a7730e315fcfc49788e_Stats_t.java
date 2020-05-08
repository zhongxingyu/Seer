 public class Stats {
 	
 	public static void main(String[] args) {
		int[] a = {4, 5, -3 ,-2, 1, 0, 5, 8};
		//-9, -6, -5, 1, 3, 4, 5, 5

		if (a.length == 0) {
			throw new IllegalArgumentException("Length cannot be 0!");
		} 
 
 		System.out.println("Max of a: " + max(a));
 		System.out.println("Min of a: " + min(a));
 		System.out.println("Mean of a: " + mean(a));
		//System.out.println("Mean of b: " + mean(b));
 		System.out.println("Median of a: " + median(a));
		//System.out.println("Median of b: " + median(b));
 		System.out.println("Quartile 1 of a: " + quartile1(a));
		//System.out.println("Quartile 1 of b: " + quartile1(b));
 		System.out.println("Quartile 3 of a: " + quartile3(a));
		//System.out.println("Quartile 3 of b: " + quartile3(b));
 		System.out.println("Mode of a: " + mode(a));
		//System.out.println("Mode of b: " + mode(b));
 		System.out.println("Standard deviation of a: " + standardDeviation(a));
		//System.out.println("Standard deviation of b: " + standardDeviation(b));
 
 		// System.out.println("Fibonacci sequence of length 20: ");
 		// printArray(fibonacci(20));
 	}
 
 	public static void printArray(int[] a) {
 		for (int i = 0 ; i < a.length ; i++) {
 			System.out.print(a[i] + " ");
 		}
 	}
 
 	public static int max(int[] a) {
 		int last = a[0];
 
 		for (int i = 0 ; i < a.length ; i++) {
 			last = Math.max(last, a[i]);
 		}
 
 		return last;
 	}
 
 	public static int min(int[] a) {
 		int last = a[0];
 
 		for (int i = 0 ; i < a.length ; i++) {
 			last = Math.min(last, a[i]);
 		}
 
 		return last;
 	}
 
 	public static double sum(int[] a) {
 		double sum = 0;
 
 		for (int i = 0 ; i < a.length ; i++) {
 			sum += a[i];
 		}
 
 		return sum;
 	}
 
 	public static double mean(int[] a) {
 		return sum(a) / a.length;
 	}
 
 	public static int getSmallest(int[] a, int startingIndex) {
 		int smallest = a[startingIndex];
 		int index = startingIndex;
 		for (int i = startingIndex ; i < a.length ; i++) {
 			if (a[i] < smallest) {
 				smallest = a[i];
 				index = i;
 			}
 		}
 
 		return index;
 	}
 
 	public static int[] sortA(int[] a) {
 		int start = 0;
 		for (int i = start ; i < a.length ; i++) {
 			int index = getSmallest(a, start);
 			int temp = a[start];
 			a[start] = a[index];
 			a[index] = temp;
 			start++;
 		}
 
 		return a;
 	}
 
 	public static double median(int[] a) {
 		return median(a, 0 , a.length - 1);
 	}
 
 	public static double median(int[] a, int startIndex, int endIndex) {
		if (a.length == 1) {
			return a[0];
		}
 		a = sortA(a);
 		int length = (endIndex - startIndex) + 1;
 		if (length % 2 == 0) {
 			//Even
 			return ((a[startIndex + (length / 2) - 1] + a[startIndex + (length / 2)]) / 2.0);
 		} else {
 			return a[startIndex + (length / 2)];
 		}
 	}
 
 	public static double quartile1(int[] a) {
		return median(a, 0, (a.length / 2) - 1);
 	}
 
 	public static double quartile3(int[] a) {
 		if (a.length % 2 == 0) {
 			return median(a, (a.length / 2), (a.length - 1));
 		} else {
 			return median(a, (a.length / 2), (a.length));
 		}
 	}
 
 	public static int mode(int[] a) {
 		int max = a[0];
 		int lastCount = 0;
 
 		for (int i = 0 ; i < a.length ; i++) {
 			int count = 0;
 			for (int j = 0 ; j < a.length ; j++) {
 				if (a[j] == a[i]) {
 					count++;
 				}
 			}
 			if (count > lastCount) {
 				lastCount = count;
 				max = a[i];
 			}
 		}
 
 		return max;
 	}
 
 	public static double standardDeviation(int[] a) {
 		double sum = 0;
 		double mean = mean(a);
 
 		for (int i = 0 ; i < a.length ; i++) {
 			sum += Math.pow(mean - a[i], 2);
 		}
 
 		sum /= a.length - 1;
 
 		return Math.sqrt(sum);
 	}
 
 	public static int[] fibonacci(int length) {
 		int[] a = new int[length];
 		int last = 0;
 		int secondLast = 1;
 		a[0] = 0;
 		a[1] = 1;
 		for (int i = 2 ; i < length ; i++) {
 			a[i] = last + secondLast;
 			last = secondLast;
 			secondLast = a[i];
 		}
 
 		return a;
 	}
 }
