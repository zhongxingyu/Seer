 package competitiveProg;
 
 import java.util.Arrays;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.Map.Entry;
 
 public class MathsLib {
 	public static void main(String[] args) {
 		// test suite
 		System.out.println(fastExp(2, 10) == 1024);
 		System.out.println(gcd(55, 89) == 1);
 		System.out.println(extendedGCD(6, 9).getKey() * 6
 				+ extendedGCD(6, 9).getValue() * 9 == 3);
 		System.out.println(isPrime(1000000007));
 		System.out.println(isPrime(9) == false);
 		System.out.println(inverseMod(3, 7) == 5);
 		System.out.println(chooseMod(10, 5, 1000000007) == 252);
 		System.out.println(choose(20, 10) == 184756);
 		System.out.println(Arrays.equals(primeList(19), new int[] { 2, 3, 5, 7, 11, 13, 17, 19 }));
 	}
 
 	public static long fastExp(long base, long exp) {
 		long acc = 1;
 		if (exp == 0)
 			return acc;
 		while (exp > 0) {
 			if (exp % 2 == 1) {
 				acc *= base;
 			}
 			base *= base;
 			exp /= 2;
 		}
 		return acc;
 	}
 
 	// returns base ^ exp mod n
	public static int fastExpMod(int base, int exp, int n) {
		int result = 1;
 		if (exp == 0)
 			return 1;
 		while (exp > 0) {
 			if ((exp & 2) == 1) {
 				result = (result * base) % n;
 			}
 			base = (base * base) % n;
 			exp = (exp * 2) % n;
 		}
		return result;
 	}
 
 	public static long gcd(long a, long b) {
 		long t;
 		if (a < b) {
 			t = a;
 			a = b;
 			b = t;
 		}
 		while (a % b != 0) {
 			t = a % b;
 			a = b;
 			b = t;
 		}
 		return b;
 	}
 
 	// This returns the value x, y such that ax + by = gcd(a, b). Useful for
 	// calculating inverse if a b are coprime, because it we get that x is the
 	// inverse of a, y is the inverse of b. Please note that this might return
 	// negative numbers
 	public static Entry<Integer, Integer> extendedGCD(int a, int b) {
 		int x1 = 0, x2 = 1;
 		int y1 = 1, y2 = 0;
 		int temp;
 		while (b != 0) {
 			int q = a / b;
 			temp = a;
 			a = b;
 			b = temp % b;
 			temp = x1;
 			x1 = x2 - q * x1;
 			x2 = temp;
 			temp = y1;
 			y1 = y2 - q * y1;
 			y2 = temp;
 		}
 		return new SimpleEntry<Integer, Integer>(x2, y2);
 	}
 
 	// returns x such that ax = 1 mod n. Only works if gcd(a,n) = 1 because we
 	// know ax + bn = c has solution only if gcd (a,n) | c
 	public static int inverseMod(int a, int n) {
 		int ans = extendedGCD(a, n).getKey();
 		return (ans < 0) ? (ans % n) + n : (ans % n);
 	}
 
 	// Another way: We know that a^p-1 == 1 mod p. So b = a^p-2 is a
 	// multiplicative inverse
 	public static int inverseMod2(int a, int n) {
 		return fastExpMod(a, n - 2, n);
 	}
 
 	// returns a choose b mod n
 	public static int chooseMod(int a, int b, int n) {
 		long num = 1, den = 1;
 		b = Math.min(b, a - b);
 		for (int i = 0; i < b; i++) {
 			num = (num * (a - i)) % n;
 		}
 		for (int i = 2; i <= b; i++) {
 			den = (den * i) % n;
 		}
 		return (int) ((num * (inverseMod((int) den, n) % n)) % n);
 	}
 
 	// returns a choose b
 	public static long choose(int a, int b) {
 		b = Math.min(b, a - b);
 		long result = 1;
 		int aCounter = 0;
 		int bCounter = 2;
 		while (aCounter < b) {
 			while (bCounter <= b) {
 				if (result % bCounter != 0) {
 					result *= a - aCounter++;
 				} else {
 					result /= bCounter++;
 				}
 			}
 			result *= a - aCounter++;
 		}
 		return result;
 	}
 
 	// returns whether n is prime without using any cache table
 	public static boolean isPrime(int n) {
 		if (n == 1 || n % 2 == 0)
 			return false;
 		else if (n <= 3)
 			return true;
 		else {
 			for (int i = 3; i * i <= n; i += 2) {
 				if (n % i == 0)
 					return false;
 			}
 			return true;
 		}
 	}
 
 	// return list of primes up to n
 	public static int[] primeList(int n) {
 		// by prime number theorem, we need roughly n/log n space
 		int[] tempBuffer = new int[(int) (n / Math.log(n) + 10)];
 		tempBuffer[0] = 2;
 		tempBuffer[1] = 3;
 		int counter = 2;
 		for (int i = 5; i <= n; i += 2) {
 			if (isPrime(i)) {
 				tempBuffer[counter++] = i;
 			}
 		}
 		return Arrays.copyOf(tempBuffer, counter);
 	}
 }
