 package problem2685;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 /**
  * Numeral System http://poj.org/problem?id=2685
  * 
  * @author koduki.
  * 
  */
 public class Main {
 	/**
 	 * mcxi encode table.
 	 */
 	private static final Map<Integer, String> encodeTable = new HashMap<Integer, String>() {{
 		put(1000, "m");
 		put(100, "c");
 		put(10, "x");
 		put(1, "i");
 	}};
 
 	/**
 	 * mcxi decode table.
 	 */
 	private static final Map<Character, Integer> decodeTable = new HashMap<Character, Integer>() {{
 		this.put('m', 1000);
 		this.put('c', 100);
 		this.put('x', 10);
 		this.put('i', 1);
 	}};
 
 	/**
 	 * mcxi記法を整数に変換する.
 	 * 
 	 * @param mcxi 変換対象のmcxi記法.
 	 * @return 変換した整数.
 	 */
 	int mcxiToInteger(LinkedList<Character> mcxi) {
 		LinkedList<Character> xs = mcxi;
 		int sum = 0;
 		while (!xs.isEmpty()) {
 			int x = Integer.parseInt(xs.pop().toString());
 			int y = decodeTable.get(xs.pop());
 			sum += x * y;
 		}
 
 		return sum;
 	}
 
 	/**
 	 * mcxiの省略部分を補完する.
 	 * 
 	 * @param mcxi 補完対象のmcxi記法文字列.
 	 * @return 補完されたmcxi記法.
 	 */
 	LinkedList<Character> completeMcxi(String mcxi) {
 		LinkedList<Character> xs = new LinkedList<Character>();
 		boolean num_flg = true;
 		for (char c : mcxi.toCharArray()) {
 			if (c == 'm' || c == 'c' || c == 'x' || c == 'i') {
 				if (num_flg) xs.add('1');
 				num_flg = true;
 			} else {
 				num_flg = false;
 			}
 			xs.add(c);
 		}
 		return xs;
 	}
 
 	/**
 	 * 整数をmcxi記法に変換する.
 	 * 
 	 * @param n target number.
 	 * @return mcxi format number.
 	 */
 	String IntegerToMcxi(int n) {
 		StringBuilder sb = new StringBuilder();
 
 		int[] xs = { 1000, 100, 10, 1 };
 		for (int x : xs) {
 			int a = n / x;
 			n = n % x;
			if (a > 1) sb.append(a);
			if (a > 0) sb.append(encodeTable.get(x));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * mcxi記法で与えられた値の合計を返す.
 	 * 
 	 * @param arg1 mcxi format number.
 	 * @param arg2 mcxi format number.
 	 * @return result with mcxi format.
 	 */
 	public String add(String arg1, String arg2) {
 		int x = mcxiToInteger(completeMcxi(arg1));
 		int y = mcxiToInteger(completeMcxi(arg2));
 
 		return IntegerToMcxi(x + y);
 	}
 
 	/**
 	 * 与えられた問題の結果を返す.
 	 * 
 	 * @param in problem input.
 	 * @return preblem result.
 	 */
 	public List<String> solve(InputStream in) {
 		Scanner sc = new Scanner(in);
 
 		List<String> result = new ArrayList<String>();
 		int n = sc.nextInt();
 		for (int i = 0; i < n; i++) {
 			String arg1 = sc.next();
 			String arg2 = sc.next();
 			result.add(add(arg1, arg2));
 		}
 		return result;
 	}
 
 	/**
 	 * kick method.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Main target = new Main();
 		List<String> result = target.solve(System.in);
 
 		for (String r : result) {
 			System.out.println(r);
 		}
 	}
 
 }
