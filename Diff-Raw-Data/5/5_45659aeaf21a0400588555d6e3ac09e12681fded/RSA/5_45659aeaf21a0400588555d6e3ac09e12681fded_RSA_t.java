 package rsaChat;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 
 /**
  * @author Baichuan Li
  * @author Hui Jia
  * @version Apr. 12 2012
  */
 class RSA {
     Scanner scan;
 
     public static void main(String[] args) {
 	new RSA().run();
     }
 
     private void run() {
 	scan = new Scanner(System.in);
 	System.out.println("Enter the nth prime and the mth prime to compute:");
 	int nth;
 	int mth;
 	try {
 	    nth = scan.nextInt();
 	    mth = scan.nextInt();
 	} catch (Exception e) {
 	    System.out.println("Input not valid integers. Program quit....");
 	    return;
 	}
 	long nthPrime = 0;
 	long mthPrime = 0;
 	// already consider 2 as a prime, so count starts from 1
 	int primeCount = 0;
 	int i = 2;
 	// computer the nth and mth prime number
 	while (true) {
 	    boolean prime = true;
 	    for (int j = 2; j < i; j++) {
 		if (i % j == 0) {
 		    prime = false;
 		    break;
 		}
 	    }
 	    if (prime) {
 		primeCount++;
 		if (primeCount == nth) {
 		    nthPrime = i;
 		} else if (primeCount == mth) {
 		    mthPrime = i;
 		}
 	    }
 	    if (nthPrime != 0 && mthPrime != 0) {
 		break;
 	    }
 	    i++;
 	}
 
 	BigInteger c = BigInteger.valueOf(nthPrime * mthPrime);
 	BigInteger m = BigInteger.valueOf((nthPrime - 1) * (mthPrime - 1));
 
 	BigInteger e = coprime(m);
 	BigInteger d = mod_inverse(e, m);
 	System.out.println(nth + "th prime = " + nthPrime + ", " + mth
 		+ "th prime = " + mthPrime + ", c = " + c + ", m = " + m
 		+ ", e = " + e + ", d = " + d + ", Public Key = (" + e + ", "
 		+ c + "), Private Key = (" + d + ", " + c + ")");
 
 	promptForEncrypt();
 	promptForDecrypt();
     }
 
     /**
      * Prompt the user for encrypt.
      */
     public void promptForEncrypt() {
 	System.out
 		.println("Please enter the public key (e, c): first e, then c");
 	BigInteger pubKey = scan.nextBigInteger();
 	BigInteger c_key = scan.nextBigInteger();
 	System.out.println("Please enter a sentence to encrypt");
	scan.nextLine();
	String input = scan.nextLine();
 	for (int k = 0; k < input.length(); k++) {
	    // System.out.println("Input length is:" + input.length());
 	    System.out.print(input.charAt(k) + " " + (int) input.charAt(k)
 		    + " encrypt to: ");
 	    BigInteger cipher = endecrypt(
 		    BigInteger.valueOf((long) input.charAt(k)), pubKey, c_key);
 	    System.out.println(cipher);
 	}
     }
 
     public void promptForDecrypt() {
 	System.out
 		.println("Please enter the private key (d, c): first d, then c");
 	BigInteger privateKey = scan.nextBigInteger();
 	BigInteger c_key = scan.nextBigInteger();
 	System.out
 		.println("Enter next char cipher value as an int, type quit to quit");
 
 	String input = scan.next();
 	while (!input.equals("quit")) {
 	    try {
 		BigInteger cipher = new BigInteger(input);
 		cipher = endecrypt(cipher, privateKey, c_key);
 		System.out.println((char) cipher.intValue() + " " + cipher);
 	    } catch (Exception e) {
 		System.out
 			.println("Input is not a valid int number. Program quit...");
 		return;
 	    }
 	    input = scan.next();
 
 	}
 
     }
 
     public static BigInteger coprime(BigInteger x) {
 	// I changed to method so the coprime number is restricted in the range
 	// (0, x), or it would return some negative numbers
 	Random rand = new Random();
 	int y = rand.nextInt(x.intValue());
 	while (GCD(x.intValue(), y) != 1) {
 	    y = rand.nextInt(x.intValue());
 	}
 	return BigInteger.valueOf(y);
 
     }
 
     public static int GCD(int a, int b) {
 	int r;
 	int x = a;
 	int y = b;
 	while (y != 0) {
 	    r = x % y;
 	    x = y;
 	    y = r;
 	}
 	return x;
     }
 
     // helper method for mod_inverse
     private static ArrayList<Integer> extendedGCD(int a, int b) {
 	ArrayList<Integer> k = new ArrayList<Integer>();
 	ArrayList<Integer> j = new ArrayList<Integer>();
 	ArrayList<Integer> r = new ArrayList<Integer>();
 	ArrayList<Integer> x = new ArrayList<Integer>();
 	ArrayList<Integer> y = new ArrayList<Integer>();
 	ArrayList<Integer> q = new ArrayList<Integer>();
 	ArrayList<Integer> result = new ArrayList<Integer>();
 	int gcd = 0;
 	int i = 0;
 	int m = 0;
 	k.add(b);
 	j.add(a);
 	r.add(a);
 
 	if (a == b) {
 	    gcd = a;
 	    x.add(1);
 	    y.add(0);
 	} else {
 	    while (r.get(i) != 0) {
 		q.add(k.get(i) / j.get(i));
 		r.add(k.get(i) - q.get(i) * j.get(i));
 		k.add(j.get(i));
 		j.add(r.get(i + 1));
 		i++;
 		gcd = j.get(i - 1);
 	    }
 	    x.add(1);
 	    y.add(0);
 	    i--;
 
 	    while (i > 0) {
 		y.add(x.get(m));
 		x.add(y.get(m) - q.get(i - 1) * x.get(m));
 		i--;
 		m++;
 		// int x1 = x.get(m);
 		// int y1 = y.get(m);
 	    }
 	    result.add(gcd);
 	    result.add(x.get(x.size() - 1));
 	    result.add(y.get(y.size() - 1));
 
 	}
 	return result;
 
     }
 
     /**
      * @param base
      * @param m
      * @return
      */
     public static BigInteger mod_inverse(BigInteger base, BigInteger m) {
 	// Mod inverse is getting negative numbers? I think it's better to
 	// change it to positive number in the range of (0, m)?
 	//changed this into positive number
 	int x = 0;
 	int result = 0;
 	if (GCD(base.intValue(), m.intValue()) == 1) {	  	
 	    x = extendedGCD(base.intValue(), m.intValue()).get(1); 
 	    if(x % m.intValue() < 0) result = m.intValue() + x % m.intValue();
 	    else result = x % m.intValue();
 	}
 
 	return BigInteger.valueOf(result);
     }
 
     // helper method for modulo
     private static ArrayList<Integer> int2baseTwo(int x) {
 	int q = x;
 	int k = 0;
 	// what is the function of this k? Seems to me it's not necessary at
 	// all?
 	ArrayList<Integer> a = new ArrayList<Integer>();
 	while (q != 0) {
 	    a.add(q % 2);
 	    q = q / 2;
 	    k++;
 	}
 	return a;
 
     }
 
     public static int modulo(int a, int b, int c) {
 	ArrayList<Integer> baseA = int2baseTwo(b);
 	int x = 1;
 	int power = a % c;
 	for (int i = 0; i < baseA.size(); i++) {
 	    if (baseA.get(i) == 1)
 		x = (x * power) % c;
 	    power = (power * power) % c;
 	}
 	return x;
 
     }
 
     /**
      * Computer Euler's Totient. Euler's totient or phi function is an
      * arithmetic function that counts the number of positive integers less than
      * or equal to n that are relatively prime to n.
      * 
      * @param n
      *            The number n to be computed.
      * @return The number of positive integers relatively prime to n.
      */
     int totient(int n) {
 	int count = 0;
 	for (int i = 1; i < n; i++) {
 	    if (GCD(i, n) == 1) {
 		count++;
 	    }
 	}
 	return count;
     }
 
     /**
      * Given an integer representing an ASCII character value, encrypt it via
      * the RSA crypto algorithm.
      * 
      * @param msg_or_cipher
      * @param key
      * @param c
      * @return
      */
     BigInteger endecrypt(BigInteger msg_or_cipher, BigInteger key, BigInteger c) {
 	return msg_or_cipher.modPow(key, c);
     }
 }
