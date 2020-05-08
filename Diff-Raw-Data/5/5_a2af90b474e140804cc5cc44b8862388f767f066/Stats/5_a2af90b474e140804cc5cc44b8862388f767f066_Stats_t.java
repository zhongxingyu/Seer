 import java.util.Arrays;
 public class Stats {
 
 	public static void main(String[] args) {
		int [] a = {100,26,433,24,24,19};
 		Arrays.sort(a);
 		System.out.println("Original Array(sorted): ");
 		print(a);
 		System.out.println("The max value is: "+ max(a));
 		System.out.println("The min value is: "+ min(a));
 		System.out.println("The mean value is: "+ mean(a));
 		System.out.println("The median value is: "+median(a));
 		System.out.println("The lower quartile is: "+lowerQuartile(a));
 		System.out.println("The upper quartile is: "+upperQuartile(a));
 		System.out.println("The mode is: "+mode(a));
 		System.out.println("The standard Deviation is: "+standardDeviation(a));
 	}
 
 	public static void print(int[] a){
 		for (int i=0; i<a.length; i++) {
 			System.out.println(a[i]+" ");
 		}
 		System.out.println();
 	}
 
 	// public static int[] sort(int[] a){
 	// 	int [] b = new int [a.length];
 	// 	int max = 0;
 	// 	for (int j=0; j<a.length; j++) {
 	// 		for (int i = 0; i<a.length; i++) {
 	// 			if (j == 0) {
 	// 				b[0] = min(a);
 	// 				max = b[0];
 	// 			}else{
 	// 				if (b[j-1] < a[j] && a[j] < max) {
 	// 					b[j] = a[j];
 	// 				}
 	// 			}
 	// 		}
 	// 	}
 	// 	return b;
 	// }
 
 	public static int max(int[] a){
 		int max = a[0];
 		for (int i = 0; i<a.length; i++) {
 			if (a[i] > max) {
 				max = a[i];
 			}
 		}
 		return max;
 	}
 
 	public static int min(int[] a){
 		int min = a[0];
 		for (int i = 0; i<a.length; i++) {
 			if (a[i] < min) {
 				min = a[i];
 			}
 		}
 		return min;
 	}
 
 	public static double mean(int[] a){
 		double mean = 0.0;
 		for (int i=0; i<a.length; i++) {
 			mean = mean + a[i];
 		}
 		return (mean/a.length);
 	}
 
 	public static double median(int[] a){
 		double median = 0.0;
 		if (a.length % 2 == 0) {
 			median = ((double)a[a.length/2 + 1] + (double)a[a.length/2-1])/2;
 		}else{
 			median = (a[a.length/2]);
 		}
 		return median;
 	}
 
 	public static double lowerQuartile(int[] a) {
 		double median = 0.0;
 		int oddLength = a.length + 1;
 		if (a.length % 2 == 0) {
 			if ((a.length/2) % 2 == 0) {
 				median = ((double)a[a.length/4] + (double)a[(a.length/4 - 1)]) / 2.0;
 			} else {
 				median = a[(oddLength/4)];
 			}
 		} else {
 			if ((oddLength/2) % 2 == 0) {
 				median = ((double)a[oddLength/4] + (double)a[(oddLength/4 + 1)]) / 2.0;
 			} else {
 				median = a[((oddLength - 2)/4 - 1)] + a[(oddLength - 2)/4];
 			}
 		}
 		return median;
 	}
 
	public static double upperQuartile(int[] a){
 		double median = 0.0;
 		int oddLength = a.length - 1;
 		if (a.length % 2 == 0) {
 			if ((a.length/2) % 2 == 0) {
 				median = ((double)a[a.length/4 + (a.length/2)] + (double)a[(a.length/4 - 1)+(a.length/2)]) / 2.0;
 			} else {
 				median = a[(oddLength/4)+ (int)(oddLength/2+1)];
 			}
 		} else {
 			if ((oddLength/2) % 2 == 0) {
 				median = ((double)a[oddLength/4 + oddLength/2] + (double)a[(oddLength/4 + 1) + (oddLength/2)]) / 2.0;
 			} else {
 				median = (a[((oddLength/4)) + (int)(oddLength/2 + 1)])/2.0;
 			}
 		}
 		return median;
 	}
 
 	public static int mode(int[] a){
 		int mode = a[0];
 		int countMode = 1;
 		int[] count = new int [a.length];
 		for (int i=0; i<a.length; i++) {
 			count [i]=0;
 		}
 		for (int i=0; i<a.length; i++) {
 			for (int j=0; j<a.length; j++) {
 				if (a[i] == a[j]) {
 					count [i] += 1;
 				}
 			}
 		}
 		for (int i=0; i<a.length; i++) {
 			if (count[i]>countMode) {
 				mode = a[i];
 				countMode = count[i];
 			}
 		}
 		return mode;
 	}
 
 	public static double standardDeviation(int[] a) {
 		int u = 0;
 		int [] m = new int [a.length];
 		int sum = 0;
 		double standardDeviation = 0;
 		for (int i = 0; i < a.length; i++) {
 			u += a[i];
 		}
 		u /= a.length;
 		for (int i = 0; i < a.length; i++) {
 			m[i] = a[i] - u;
 			m[i] *= m[i];
 		}
 		for (int i = 0; i < a.length; i++) {
 			sum += m[i];
 		}
 		standardDeviation = sum / (double)a.length;
 		return Math.sqrt(standardDeviation);
 
 	}
 }
