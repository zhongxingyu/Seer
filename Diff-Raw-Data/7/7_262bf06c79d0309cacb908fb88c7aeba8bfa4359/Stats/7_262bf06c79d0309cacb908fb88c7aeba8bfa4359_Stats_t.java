 public class Stats {
 	public static void main(String[] args) {
		int[] a = {1, 2, 3, 4};
 
 		print(max(a));
 		print(min(a));
 		print(mean(a));
 		print(median(a));
 		print(quartile1(a));
 		print(quartile3(a));
 		print(mode(a));
 		print(standardDeviation(a));
 	}
 	public static void print(double a) {
 		System.out.println(a);
 	}
 	public static int max(int[] a) {
 		int largest = a[0];
 
 		for (int i=1; i<a.length; i++) {
 			if (a[i] > largest) {
 				largest = a[i];
 			}
 		}
 		
 		return largest;
 	}
 	public static int min(int[] a) {
 		int min = a[0];
 		for (int i=1; i<a.length; i++) {
 			if (a[i] < min) {
 				min = a[i];
 			}
 		}
 		
 		return min;
 	}
 	public static double mean(int[] a) {
 		double mean = 0.0;
 		double sum = 0.0;
 		double j = 0.0;
 		for (int i = 0; i<a.length; i++) {
 			sum += a[i];
 			j++;
 		}
 		mean = sum/j;
 		
 		return mean;
 	}
 	public static double median(int[] a) {
 		double median = 0.0;
 		int i = ((a.length / 2) - 1);
 		double add = a[i];
 		double add2 = a[i+1];
 		if ((a.length % 2) == 0) {
 			median = ((add + add2) / 2.0);;
 		} else {
 			median = add2;
 		}
 		return median;
 	}
 	public static double quartile1(int[] a) {
 		double total = 0.0;
 		int i = 0;
 		int length = 0;
 		if (a.length % 2 == 0) {
 			i = (a.length / 2);
 		} else {
 			i = (a.length / 2) + 1;
 		}
 		
 		if (i % 2 == 0) {
 			length = (i / 2) - 1;
 			total = (a[length] + a[length + 1]) / 2.0;
 		} else {
 			length = (i / 2);
 			total = a[length];
 		}
 		return total;
 	}
 	public static double quartile3(int[] a) {
 		double total = 0.0;
 		int i = 0;
 		int length = a.length;
 
 		if (a.length % 2 ==0) {
 			i = (a.length / 2);
 		} else {
 			i = (a.length / 2) + 1;
 		}
 
 		if (i % 2 == 0) {
 			length = ((i/2) + (a.length)) / 2;
 			total = (a[length] + a[length+1]) / 2.0;
 		} else {
			length = (i / 2);
			total = (a[length] * 2);
 		}
 		return total;
 	}
 	public static double mode(int[] a) {
 		int length = 0;
 		for (int j=0; j<a.length; j++) {
 			length++;
 		}
 		double num = a[0];
 		int finalCount = 0;
 		for (int i=0; i<a.length; i++) {
 			int occurance = 0;
 			for (int r=0; r<a.length; r++) {
 				if (a[r] == a[i]) {
 					occurance++;
 				}
 			}
 			if (occurance > finalCount) {
 				finalCount = occurance;
 				num = a[i];
 			}
 		}
 		
 		
 		return num;
 	}
 	public static double standardDeviation(int[] a) {
 		double total = 0.0;
 		double sum = 0.0;
 		double mean = mean(a);
 		for (int i = 0; i<a.length; i++) {
 			sum += Math.pow((mean - a[i]), 2);
 		}
 		sum = sum / (a.length-1);
 		total = Math.sqrt(sum);
 		
 		return total;
 	}
 }
