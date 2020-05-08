 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class StrMatch {
 
 	public static void main(String[] args) throws Exception {
 
 		if (args.length != 3) {
 			throw new IllegalArgumentException(
 					"usage: pattern.txt source.txt results.txt");
 		}
 
 		// pattern file parsing
 		String pattern = args[0];
 		File p;
 		ArrayList<String> eachPattern = new ArrayList<String>();
 		int largestps = 2048; // longest pattern size
 
 		// source file parsing
 		String source = args[1];
 		FileReader fr = null;
 
 		// output writer
 		String results = args[2];
 		File outFile = new File(results);
 		StringBuffer output = new StringBuffer();
 
 		try {
 			// read pattern
 			p = new File(pattern);
 			Scanner scan = new Scanner(p).useDelimiter("&");
 			while (scan.hasNext()) {
 				String thisPattern = scan.next();
 
 				if (thisPattern.length() > largestps) {
 					largestps = thisPattern.length();
 				}
 
 				if (thisPattern.trim().length() != 0) {
 					// System.out.println(thisPattern);
 					eachPattern.add(thisPattern);
 				}
 			}
 		} catch (IOException e) {
 			System.out.println("could not read pattern file: " + pattern);
 			System.out.println(e);
 		}
 
 		try {
 			fr = new FileReader(source);
 			LookBackStringBuffer lbsb = new LookBackStringBuffer(source,
 					largestps);
 
 			for (String s : eachPattern) {
 
 				System.out.println("*********pattern: ");
 				System.out.println(s + "\n");
 
 				lbsb.reset();
 				Stopwatch t = new Stopwatch();
 				String found;
 
 				// RK rolling linear sum
 				t.start();
 				long[] rk = rabinKarp(s, lbsb);
 				t.stop();
 
 				found = (rk[0] == 1) ? "MATCHED: " : "FAILED: ";
 				output.append("RK " + found + s + "\n");
 				lbsb.reset();
 
 				System.out.println("rk:  " + rk[0] + " " + rk[1] + "  "
 						+ t.time() + " " + rk[2]);
 
 				// RK rolling base sum
 				t.start();
 				long[] rk2 = rabinKarp2(s, lbsb);
 				t.stop();
 
 				found = (rk2[0] == 1) ? "MATCHED: " : "FAILED: ";
 				output.append("RK2 " + found + s + "\n");
 				lbsb.reset();
 
 				System.out.println("rk2:  " + rk2[0] + " " + rk2[1] + "  "
 						+ t.time() + " " + rk2[2]);
 
 				// KMP
 				t.start();
 				long[] kmp = knuthMorrisPratt(s, lbsb);
 				t.stop();
 
 				found = (kmp[0] == 1) ? "MATCHED: " : "FAILED: ";
 				output.append("KMP " + found + s + "\n");
 				lbsb.reset();
 
 				System.out.println("KMP: " + kmp[0] + " " + kmp[1] + "  "
 						+ t.time());
 
 				// BM
 				t.start();
 				long[] bm = boyerMoore(s, lbsb);
 				t.stop();
 
 				found = (bm[0] == 1) ? "MATCHED: " : "FAILED: ";
 				output.append("BM " + found + s + "\n");
 				lbsb.reset();
 
 				System.out.println("BM:  " + bm[0] + " " + bm[1] + "   "
 						+ t.time());
 
 				// Brute Force
 				t.start();
 				long[] bf = bruteForce(s, lbsb);
 				t.stop();
 
 				found = (bf[0] == 1) ? "MATCHED: " : "FAILED: ";
 				output.append("Brute Force " + found + s + "\n");
 
 				System.out.println("BF:  " + bf[0] + " " + bf[1] + "  "
 						+ t.time());
 			}
 
 			// write the output buffer to output file
 			FileWriter fw = new FileWriter(outFile);
 			fw.write(output.toString());
 			fw.close();
 
 		} catch (IOException e) {
 			System.out.println("could not read sourece file: " + source);
 		}
 
 	}
 
 	// Brute Force Algorithm
 	public static long[] bruteForce(String pattern, LookBackStringBuffer source)
 			throws Exception {
 		assert (pattern.length() > 0) : "pattern cannot be empty";
 
 		// result[0] shows success or failure, result[1] shows number of
 		// comparisons
 		long[] result = new long[2];
 
 		// for (int i = 0; i < source.length() - pattern.length() + 1; i++) {
 		for (int i = 0; source.hasAvailable(i + pattern.length()); i++) {
 			int tempi = i;
 			boolean match = true;
 			for (int j = 0; j < pattern.length() & match; j++, tempi++) {
 				result[1] += 1;
 				if (source.charAt(tempi) != pattern.charAt(j)) {
 					match = false;
 				}
 			}
 			if (match) {
 				result[0] = 1;
 				return result;
 			}
 		}
 
 		return result;
 	}
 
 	// Rabin-Karp Algorithm, Rolling linear sum
 	public static long[] rabinKarp(String pattern, LookBackStringBuffer source)
 			throws IOException, Exception {
 
 		long[] result = new long[3];
 
 		if (!source.hasAvailable(pattern.length())) {
 			return result;
 		}
 
 		long patternHC = 0;
 		long sourceHC = 0;
 
 		for (int i = 0; i < pattern.length(); i++) {
 			// calculate the whole pattern hash
 			long patternCharInt = pattern.charAt(i);
 			patternHC += patternCharInt;
 			// calculate first source has
 			char sourceChar = source.charAt(i);
 			long sourceCharInt = (long) sourceChar;
 			sourceHC += sourceCharInt;
 		}
 
 		for (int i = pattern.length(); source.hasAvailable(i + 1); i++) {
 			result[1]++;
 			if (patternHC == sourceHC) {
 
 				result[2]++;
 				int k = 0;
 				int j = 0;
 				for (j = i - pattern.length(); j < i; j++, k++) {
 					char src = source.charAt(j);
 					char pat = pattern.charAt(k);
 					result[1]++;
 
 					if (src != pat)
 						break;
 				}
 
 				if (j == i) {
 					result[0] = 1;
 					return result;
 				}
 
 			}
 
 			if (!source.hasAvailable(i + 1)) {
 				break;
 			}
 
 			char next = source.charAt(i);
 			long nextC = (long) next;
 			long prevHC = source.charAt(i - pattern.length());
 
 			sourceHC = sourceHC - prevHC + nextC;
 		}
 
 		return result;
 	}
 
 	// Rabin-Karp Algorithm, Rolling base sum
 	public static long[] rabinKarp2(String pattern, LookBackStringBuffer source)
 			throws IOException, Exception {
 
 		long p = 17389;
 		long base = 10;
 		long f = fastExp(base, pattern.length() - 1, p);
 
 		long[] result = new long[3];
 
 		if (!source.hasAvailable(pattern.length())) {
 			return result;
 		}
 
 		long patternHC = 0;
 		long sourceHC = 0;
 
 		for (int i = 0; i < pattern.length(); i++) {
 			// calculate the whole pattern hash
 			long patternCharInt = (long) pattern.charAt(i);
 			patternHC = (patternHC * base + patternCharInt) % p;
 
 			// calculate first source has
 			long sourceCharInt = (long) source.charAt(i);
 			sourceHC = (sourceHC * base + sourceCharInt) % p;
 		}
 
 		for (int i = pattern.length(); source.hasAvailable(i + 1); i++) {
 			result[1]++;
 			if (patternHC == sourceHC) {
 				result[2]++;
 
 				int k = 0;
 				int j = 0;
 				for (j = i - pattern.length(); j < i; j++, k++) {
 					char src = source.charAt(j);
 					char pat = pattern.charAt(k);
 					result[1]++;
 
 					if (src != pat)
 						break;
 				}
 
 				if (j == i) {
 					result[0] = 1;
 					return result;
 				}
 
 			}
 
 			if (!source.hasAvailable(i + 1)) {
 				break;
 			}
 
 			char next = source.charAt(i);
 			long nextC = (long) next;
 			long prevC = source.charAt(i - pattern.length());
 
 			sourceHC = mod(((sourceHC - prevC * f) * base + nextC), p);
 
 		}
 
 		return result;
 	}
 
 	/*
 	 * fastExp and reverseBits from Ryan's RSA project
 	 */
 	private static long fastExp(long num, long e, long n) {
 		e = reverseBits(e);
 		int skipped = 0;
 		while ((e & 1l) != 1) {
 			skipped++;
 			e >>= 1;
 		}
 		long c = 1;
 		for (int remaining = 64 - skipped; remaining > 0; remaining--) {
 			if ((e & 1) == 0) {
 				c = ((c * c) % n);
 			} else {
 				c = ((((c * c) % n) * num) % n);
 			}
 			e >>>= 1;
 		}
 		return c;
 	}
 
 	private static long reverseBits(long e) {
 		e = ((e >>> 1) & 0x5555555555555555l)
 				| ((e & 0x5555555555555555l) << 1);
 		e = ((e >>> 2) & 0x3333333333333333l)
 				| ((e & 0x3333333333333333l) << 2);
 		e = ((e >>> 4) & 0x0F0F0F0F0F0F0F0Fl)
 				| ((e & 0x0F0F0F0F0F0F0F0Fl) << 4);
 		e = ((e >>> 8) & 0x00FF00FF00FF00FFl)
 				| ((e & 0x00FF00FF00FF00FFl) << 8);
 		e = ((e >>> 16) & 0x0000FFFF0000FFFFl)
 				| ((e & 0x0000FFFF0000FFFFl) << 16);
 		e = (e >>> 32) | (e << 32);
 		return e;
 	}
 
 	public static long mod(long n, long x) {
 		return (n >= 0) ? (n % x) : ((n - (n / x - 1) * x) % x);
 	}
 
 	// Knuth-Morris-Pratt Algorithm
 	public static long[] knuthMorrisPratt(String pattern,
 			LookBackStringBuffer source) throws Exception {
 		assert (pattern.length() > 0) : "pattern cannot be empty";
 
 		long[] result = new long[2];
 
 		LookBackStringBuffer s = source;
 		String w = pattern;
 
 		int m = 0;
 		int i = 0;
 		int[] T = partialMatchTable(w);
 
 		// for (int ii = 0; ii < T.length; ii++) {
 		// System.out.print(T[ii]);
 		// }
 		// System.out.println();
 
 		while (s.hasAvailable(m + i + 1)) {
 
 			result[1]++;
 			if (w.charAt(i) == s.charAt(m + i)) {
 				if (i == w.length() - 1) {
 					result[0] = 1;
 					return result;
 				}
 				i++;
 
 			} else {
 				m = m + i - T[i];
 				if (T[i] > -1) {
 					i = T[i];
 				} else {
 					i = 0;
 				}
 			}
 		}
 
 		return result;
 	}
 
 	// partial match table to support Knuth-Morris-Pratt Algorithm
 	private static int[] partialMatchTable(String pattern) {
 
 		String w = pattern;
 
 		int[] T = new int[pattern.length()];
 		T[0] = -1;
 
 		int pos = 2;
 		int cnd = 0;
 
 		while (pos < pattern.length()) {
 			if (w.charAt(pos - 1) == w.charAt(cnd)) {
 				cnd++;
 				T[pos] = cnd;
 				pos++;
 			} else if (cnd > 0) {
 				cnd = T[cnd];
 			} else {
 				T[pos] = 0;
 				pos++;
 			}
 		}
 
 		return T;
 	}
 
 	// Boyer-Moore Algorithm
 	public static long[] boyerMoore(String pattern, LookBackStringBuffer source)
 			throws Exception {
 
 		long result[] = new long[2];
 		int pl = pattern.length();
 
 		if (pl == 0 | !source.hasAvailable(0)) {
 			return result;
 		}
 
 		int[] badcharacter = prepare_badcharacter(pattern);
 		int[] goodSuffix = prepare_goodSuffix(pattern);
 
 		int s = 0;
 		while (source.hasAvailable(s + pl)) {
 			result[1]++;
 			int j = pl;
 			while (j > 0 && (pattern.charAt(j - 1) == source.charAt(s + j - 1))) {
 				result[1]++;
 				j--;
 			}
 			if (j > 0) {
 				int x = ((int) source.charAt(s + j - 1)) & 255;
 				int k = badcharacter[x];
 				int m = j - k - 1;
 				if (k < j & m > goodSuffix[j]) {
 					s += m;
 				} else {
 					s += goodSuffix[j];
 				}
 			} else {
 				result[0] = 1;
 				return result;
 			}
 		}
 		return result;
 
 	}
 
 	// computer prefix
 	private static int[] compute_prefix(String str) {
 
 		int size = str.length();
 		int[] result = new int[size];
 
 		int k = 0;
 
 		for (int q = 1; q < size; q++) {
 
 			char ck = str.charAt(k);
 			char cq = str.charAt(q);
 
 			while (k > 0 && ck != cq) {
 				k = result[k - 1];
 			}
 
 			if (ck == cq) {
 				k++;
 			}
 
 			result[q] = k;
 		}
 		return result;
 	}
 
 	// prepare bad character, bad symbol heuristic
 	private static int[] prepare_badcharacter(String str) {
 
 		int size = str.length();
 		int charE = 256;
 		int[] result = new int[charE];
 
 		for (int i = 0; i < charE; i++) {
 			result[i] = -1;
 		}
 
 		for (int j = 0; j < size; j++) {
 			result[(int) str.charAt(j)] = j;
 		}
 
 		return result;
 	}
 
 	// good suffix heuristic
 	private static int[] prepare_goodSuffix(String str) {
 
 		int size = str.length();
 		int[] result = new int[size + 1];
 
 		// reverse string
 		StringBuffer sb = new StringBuffer(str.length());
 		for (int i = str.length() - 1; i >= 0; i--) {
 			sb.append(str.charAt(i));
 		}
 
 		String reversed = sb.toString();
 
 		int[] prefix_normal = compute_prefix(str);
 		int[] prefix_reverse = compute_prefix(reversed);
 
 		for (int i = 0; i <= size; i++) {
 			result[i] = size - prefix_normal[size - 1];
 		}
 
 		for (int ii = 0; ii < size; ii++) {
 			int j = size - prefix_reverse[ii];
 			int k = ii - prefix_reverse[ii] + 1;
 
 			if (result[j] > k) {
 				result[j] = k;
 			}
 		}
 		return result;
 	}
 
 }
