 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class MagicSquares {
 	
 	int order;
 	int max;
 	int magic_constant;
 	int count_magic;
 	
 	public static void main(String[] args) {
 		if (args.length > 0) {
 			
 			MagicSquares obj = new MagicSquares();
 			
 			long start_time = System.currentTimeMillis();
 			
 			obj.order = Integer.parseInt(args[0]);
 			obj.max = obj.order*obj.order;
 			obj.magic_constant = (obj.order*obj.order*obj.order + obj.order) / 2;
 			
 			System.out.println("Finding all magic matricies of order " + obj.order);
 			
 			int[] begin = new int[obj.max];
 			int[] end = new int[obj.max];
             for (int i=0; i<obj.max; i++) {
                 begin[i] = i + 1;
                 end[i] = obj.max - i;
             }
             
             //int[] current_permutation = begin;
             long i = 0;
             long end_i = MagicSquares.factorial(obj.max);
             
            int num_threads = 16;
            int chunk_size = 40320;
             
             ArrayList<Thread> threads = new ArrayList<Thread>();
             
             while (true) {
             	
             	long a = i;
             	long b = i + chunk_size;
             	if (b > end_i) {
             		b = end_i;
             	}
             	
             	if (threads.size() < num_threads) {
             		Thread t = new Thread(obj.new MatrixThread(a,b));
                 	threads.add(t);
                 	t.run();
             	} else {
            		for (int j = 0; j < threads.size(); j++) {
            			if (!threads.get(j).isAlive()) {
            				threads.remove(j);
            				Thread t = new Thread(obj.new MatrixThread(a,b));
            				threads.add(j, t);
            			}
             		}
             	}
             	
             	if (i >= end_i)
             		break;
             	
             	i += chunk_size + 1;
             	
             	/*MagicSquares.SquareMatrix m = obj.new SquareMatrix(current_permutation);
             	if (m.is_magic()) {
             		count_magic++;
             		System.out.println(m.toString());
             	}
             	if (Arrays.equals(current_permutation, end))
             		break;
             	MagicSquares.nextPermutation(current_permutation);*/
             }
             
 			long end_time = System.currentTimeMillis();
             long runtime = end_time - start_time;
             //System.out.println(runtime);
             double runtime_seconds = (double) runtime / (double)1000;
             System.out.println("Found "+obj.count_magic+" magic squares in "+String.format("%f", runtime_seconds)+" seconds");
             //System.out.println("Found "+count_magic+" magic squares");
            
         } else {
             System.out.println("Usage: java MagicSquares <order>");
         }
 	}
 	
 	public class MatrixThread extends Thread {
 		long a;
 		long b;
 		public MatrixThread(long a, long b) {
 			this.a = a;
 			this.b = b;
 		}
 		public void run() {
 			for (long i = this.a; i < this.b; i++) {
 				int[] current_permutation = MagicSquares.getPermutation(max, i);
 				MagicSquares.SquareMatrix m = new SquareMatrix(current_permutation);
 	        	if (m.is_magic()) {
 	        		count_magic++;
 	        		System.out.println(m.toString());
 	        	}
 			}
 		}
 	}
 	
 	public class SquareMatrix {
 		
 		int[][] data;
 		
 		public SquareMatrix(int[] single_dimentional_data) {
 			this.data = new int[order][order];
 			for (int i = 0; i < single_dimentional_data.length; i++) {
 				int n = i % order;
 				int m = i/order;
 				this.data[m][n] = single_dimentional_data[i];
 			}
 		}
 
 		private int[] get_col(int n) {
 			int[] result = new int[order];
 			for (int m = 0; m < order; m++)
 				result[m] = this.data[m][n];
 			return result;
 		}
 
 		private int[] get_left_diagonal() {
 			int[] result = new int[order];
 			for (int i = 0; i < order; i++)
 				result[i] = this.data[i][i];
 			return result;
 		}
 		
 
 		private int[] get_right_diagonal() {
 			int[] result = new int[order];
 			for (int i = 0; i < order; i++)
 				result[i] = this.data[i][order - 1 - i];
 			return result;
 		}
 		
 
 		public boolean is_magic() {
 			for (int m = 0; m < order; m++) {
 				if (array_sum(this.data[m]) != magic_constant)
 					return false;
 			}
 			for (int n = 0; n < order; n++) {
 				if (array_sum(this.get_col(n)) != magic_constant)
 					return false;
 			}
 			if (array_sum(this.get_left_diagonal()) != magic_constant)
 				return false;
 			if (array_sum(this.get_right_diagonal()) != magic_constant)
 				return false;
 			return true;
 		}
 		
 
 		public String toString() {
 			String max_term = order * order + "";
 			int max_term_size = max_term.length();
 			
 			String result = "";
 			
 			String border = "+";
 			String border_between = "|";
 			for (int i = 0; i < order; i++) {
 				border += "--" + MagicSquares.str_repeat("-", max_term_size);
 				border_between += "--" + MagicSquares.str_repeat("-", max_term_size);
 				if (i != order - 1 ) {
 					border += "-";
 					border_between += "+";
 				}
 			}
 			border += "+\n";
 			border_between += "|\n";
 			
 			result += border;
 			
 			for (int m = 0; m < order; m++) {
 				result += "|";
 				for (int n = 0; n < order; n++) {
 					int padding_right = max_term_size - (data[m][n] + "").length();
 					result += " " + data[m][n] + MagicSquares.str_repeat(" ", padding_right)+ " |";
 				}
 				result += "\n";
 				if (m != order - 1)
 					result += border_between;
 			}
 			result += border;
 			return result;
 		}
 
 		
 	}
 	
 	private static int array_sum(int[] arr) {
 		int sum = 0;
 		for (int i = 0; i < arr.length; i++) {
 			sum += arr[i];
 		}
 		return sum;
 	}
 	
 	private static String str_repeat(String str, int repeat) {
 		String result = "";
 		for (int i = 0; i < repeat; i++) {
 			result += str;
 		}
 		return result;
 	}
 	
 	public static long factorial(int n) {
 		long ret = 1;
         for (int i = 1; i <= n; ++i) ret *= i;
         return ret;
     }
 	
 	public static int[] getPermutation(int n, long i) {
 		ArrayList<Integer> p = new ArrayList<Integer>();
 		for (int k = 0; k < n; k++) {
 			p.add(k+1);
 		}
 		
 		int[] r = new int[n];
 		for (int j = 0; j<n; j++) {
 			long g = MagicSquares.factorial(p.size() - 1);
 			r[j] = p.get((int)(i/g));
 			p.remove((int)(i/g));
 			i = i % g;
 		}
 		return r;
 	}
 
 	public static void nextPermutation(int[] digits) {
 		
 		int n = digits.length - 1; 
 		
 		int j = n - 1;
 		while (digits[j] > digits[j+1]) {
 			j--;
 		}
 		
 		int k = n;
 		while (digits[j] > digits[k])
 			k--;
 		
 		int temp = digits[j];
 		digits[j] = digits[k];
 		digits[k] = temp;
 		
 		int r = n;
 		int s = j + 1;
 		
 		while (r > s) {
 			temp = digits[r];
 			digits[r] = digits[s];
 			digits[s] = temp;
 			r--;
 			s++;
 		}
 		
 	}
 
 }
