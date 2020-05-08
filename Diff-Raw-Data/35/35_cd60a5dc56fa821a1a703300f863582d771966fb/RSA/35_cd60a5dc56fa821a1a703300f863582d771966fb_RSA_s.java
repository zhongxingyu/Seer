 import java.security.SecureRandom;
 import java.math.BigInteger;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 


 public class RSA {
 	
 	static boolean DEBUG = false;
 	
 	BigInteger[] extended_gcd(BigInteger a, BigInteger b) {
 		BigInteger x = BigInteger.ZERO;
 		BigInteger y = BigInteger.ONE;
 		BigInteger lastx = BigInteger.ONE;
 		BigInteger lasty = BigInteger.ZERO;
 		
 		while (b.compareTo(BigInteger.ZERO) != 0) {
 			BigInteger[] rl = a.divideAndRemainder(b);
 			BigInteger quotient = rl[0];
 			BigInteger r = rl[1];
 			a = b; b = r;
 			BigInteger tmp = lastx.subtract(quotient.multiply(x));
 			lastx = x; x = tmp; 
 			tmp = lasty.subtract(quotient.multiply(y));
 			lasty = y; y = tmp; 
 		}
 		
 		BigInteger[] rl = {lastx, lasty};
 		return rl;
 		
 	}
 	
 	BigInteger[] createpq(int bitlen) {
 		BigInteger p = createBigPrimeNumber(bitlen);
 		BigInteger q; 
 		while   ((q = createBigPrimeNumber(bitlen)).compareTo(p) == 0) ;
 		
 		BigInteger[] rl = {p, q};
 		// System.out.println("p  q" +p.toString(2).length() + "   " + q.toString(2).length());
 
 		return rl;
 		
 	}
 	
 	BigInteger gcd(BigInteger a, BigInteger b) {
 		BigInteger[] rl = extended_gcd(a, b);
 		return a.multiply(rl[0]).add(b.multiply(rl[1]));
 	}
 	
 	BigInteger finde(BigInteger p, BigInteger q) {
 		BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		BigInteger e = createBigNumber(phi.bitLength()).mod(phi);
 		while (gcd(e, phi).compareTo(BigInteger.ONE) != 0) {
 			e = e.add(BigInteger.ONE).mod(phi);
 		}
 		
 		if (e.bitLength() < 2)
 			return finde(p, q);
 
 		return e;
 	}
 	
 	BigInteger encrypt(BigInteger m, BigInteger e, BigInteger n) {
 		return powmod(m, e, n);
 	}
 
 	BigInteger decrypt(BigInteger c, BigInteger d, BigInteger n) {
 		return powmod(c, d, n);
 	}
 	
 	
 	BigInteger findd(BigInteger e, BigInteger p, BigInteger q) {
 		BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
 		BigInteger[] rl = extended_gcd(e, phi);
 		
 		return rl[0].add(phi).mod(phi);
 	}
 	
 	/*
 	 * Return a^d mod n
 	 * 
 	 */
 	BigInteger powmod(BigInteger a, BigInteger d, BigInteger n) {
 		int i;
 		BigInteger rl = BigInteger.ONE;
 		
 		for (i = d.bitLength()-1; i >= 0; i--) {
 			rl = rl.multiply(rl).mod(n);
 			if (d.testBit(i)) rl = rl.multiply(a).mod(n);
 		}
 		
 		return rl;
 		
 	}
 	
 	
 	/*
 	 * This method return a random number sized bitlen
 	 * 
 	 */
 	BigInteger createBigNumber(int bitlen) {
 		BigInteger num = BigInteger.ZERO;
 		SecureRandom random = new SecureRandom();
 		
 		int i;
 		
 		// This loop creates a bitlen-bits number
 		for (i = 0; i < bitlen-1; i++) {
 			//num = num.multiply(BigInteger.valueOf(2));
 			if (random.nextBoolean() == true) num = num.setBit(i);
 		}
 		
 		num = num.setBit(bitlen - 1);
 
 		return num;
 	
 	}
 	
 	
 	/*
 	 * This method implements Miller-Rabin primality test
 	 * Reference algorithm at http://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test 
 	 * 
 	 * Input:
 	 * 		BigInteger n:	number we want check
 	 * 		int		   k  :	the accuracy of the test	
 	 * 
 	 * Note: This algorithm run in O(k*lg^3(n)) time
 	 * TODO: We need a faster algorithm! 
 	 * 		+ Hint: We can use BitSieve ;)
 	 * 				or FFT ?
 	 */
 	boolean isPrime(BigInteger n, int k) {
 		int i;
 		
 		// n - 1= 2^s x d, s.t: d is odd number
 		int s = 0;
 		BigInteger d = n.subtract(BigInteger.ONE);
 		BigInteger np1 = n.subtract(BigInteger.ONE);
 		BigInteger TWO = BigInteger.valueOf(2);
 		while (d.mod(TWO).compareTo(BigInteger.ZERO) == 0) {
 			s++;
 			d = d.divide(TWO);
 		}
 		
 		// repeat k times
 		for (i = 1; i <= k; i++) {
 			BigInteger a = createBigNumber(n.bitLength()).mod(np1);
 
 			// a must be in the range [2, n-2]
 			if (!(a.compareTo(BigInteger.ONE)  == 1))
 				a.add(TWO);
 			
 			BigInteger x = powmod(a, d, n);
 			if (x.compareTo(BigInteger.ONE) == 0 || x.compareTo(np1) == 0) continue;
 			int r;
 			boolean flag = false;
 			for (r = 1; r <= s; r++) {
 				x = x.multiply(x).mod(n);
 				if (x.compareTo(BigInteger.ONE) == 0)
 					return false;
 				if (x.compareTo(np1) == 0) {flag = true; break;}
 			}
 			if (!flag) return false;
 			
 		}
 		
 		return true;
 	}
 	
 	/*
 	 * This method uses PKCS#1v1.5
 	 * 
 	 * Reference at http://www.di-mgt.com.au/rsa_alg.html#pkcs1schemes
 	 */
 	
 	byte[] encryptData(byte[] M, int len, BigInteger e, BigInteger n) {
 		int size = (n.bitLength()-1)/8 + 1;
 		if (len > size - 11) return null;
 		
 		// mesg = 00 || 02 || PS || 00 || M
 		byte[] mesg = new byte[size];
 		mesg[0] = 0; mesg[1] = 0x02;
 		int lenps = size - len - 3;
 		SecureRandom rnd = new SecureRandom();
 		for (int i = 1; i <= lenps; i++)
 			mesg[1+i] = (new Integer(1+rnd.nextInt(255))).byteValue();
 		
 		mesg[2+lenps] = 0x00;
 		
 		for (int i = 0; i < len; i++)
 			mesg[i+ 3+lenps] = M[i];
 		
 		byte[] cipher = new byte[size];
 		byte[] c = encrypt(new BigInteger(1, mesg), e, n).toByteArray();
 		if (c[0] == 0) {
 			byte[] tmp = new byte[c.length - 1];
 			System.arraycopy(c, 1, tmp, 0, tmp.length);
 			c = tmp;
 		}
 
 /*
 		System.out.println("size " + n.bitLength() + " c length " + c.length +": " +n.toByteArray()[0]);
 		System.out.println("n " + n.toString(2).length());
 */
 		for (int i= 0; i < c.length; i++)
 			cipher[size - c.length + i] = c[i];
 		
 		return cipher;
 	}
 	
 	byte[] decryptData(byte[] C, int len, BigInteger d, BigInteger n) {
 		int size = (n.bitLength()-1)/8 + 1;
 		
 		BigInteger cipher = new BigInteger(1, C);
 		BigInteger M = decrypt(cipher, d, n);
 		byte[] mesg = M.toByteArray();
 		if (mesg[0] == 0) {
 			byte[] tmp = new byte[mesg.length - 1];
 			System.arraycopy(mesg, 1, tmp, 0, tmp.length);
 			mesg = tmp;
 		}
 		
 		int c = 1;
 		while (mesg[c] != 0) c++;
 		
 		byte[] rl  = new byte[size - 3 - c + 1];
 		for (int i = 0; i < rl.length; i++)
 			rl[i] = mesg[i+c+1];
 		
 		return rl;
 	}
 
 	
 	void decryptFile(String fileName, BigInteger d, BigInteger n) throws IOException {
         FileInputStream in = null;
         FileOutputStream out = null;
 
         try {
             in = new FileInputStream(fileName);
             out = new FileOutputStream("out.decrypt");
 			int size = (n.bitLength()-1)/8 + 1;
             
             byte[] buff = new byte[size];
             int len;
             while ( (len = in.read(buff)) != -1 ) {
             	byte[] cipher = decryptData(buff, len, d, n);
             	out.write(cipher);
             }
         } finally {
             if (in != null) {
                 in.close();
             }
             if (out != null) {
                 out.close();
             }
         }
 		
 	}
 
 	void encryptFile(String fileName, BigInteger e, BigInteger n) throws IOException {
         FileInputStream in = null;
         FileOutputStream out = null;
 
         try {
             in = new FileInputStream(fileName);
             out = new FileOutputStream("out.encrypt");
 			int size = (n.bitLength()-1)/8 + 1;
             
             byte[] buff = new byte[size - 11];
             int len;
             while ( (len = in.read(buff)) != -1 ) {
             	byte[] cipher = encryptData(buff, len, e, n);
             	out.write(cipher);
             }
         } finally {
             if (in != null) {
                 in.close();
             }
             if (out != null) {
                 out.close();
             }
         }
 		
 	}
 	
 	
 	BigInteger createBigPrimeNumber(int bitlen){
 		BigInteger num = createBigNumber(bitlen);
 				
 		num = num.setBit(0); // ensure num is odd number ;)
 		
 		/* 
 		 * A sieve here ;) 
 		 * This solution boosts two or three times faster but It's still slower that JAVA lib implement
 		 * 
 		 * */
 		int prime[] = {3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97};
 		boolean[] sieve = new boolean[10009];
 		int i;
 		for (i = 0; i < prime.length; i++) {
 			int m = num.mod(BigInteger.valueOf(prime[i])).intValue();
 			int off = (prime[i] - m) % prime[i];
 			for (int j=0; j < (10000 - off)/prime[i]; j++)
 				sieve[off +j*prime[i]] = true;
 		}
 		
 		int c = 0;
 		BigInteger snum = num;
 		while (!isPrime(num, 10)) {
 			while (sieve[c] != false && c < 10000)  c +=2;
 			
 			//System.out.println("c  " + c);
 			// increase c units
 			num = snum.add(BigInteger.valueOf(c));
 			c += 2;
 			
 			// check if now num > n^bitlen - 1, this is what we don't want!
 			if (num.bitLength() > bitlen) 
 				return createBigPrimeNumber(bitlen);
 		}
 		
 		return num;
 	}
 	
 	
 	void showHelp() {
 		System.err.println("Usage: java RSA BITSIZE FILENAME\nEncrypt FILENAME file with primes p,d size BITSIZE bits");
 		System.exit(0);
 	}
 	
 	int bitSize;
 	String fileName;
 	
 	void writeToDisk(BigInteger e, BigInteger d, BigInteger p, BigInteger q) {
 		PrintStream out = null;
         try {
 			out = new PrintStream(new FileOutputStream("e.numb"));
			out.print(e.toString());
 			out.close();
 			
 			out = new PrintStream(new FileOutputStream("d.numb"));
			out.print(d.toString());
 			out.close();
 			
 			out = new PrintStream(new FileOutputStream("p.numb"));
			out.print(p.toString());
 			out.close();
 			
 			out = new PrintStream(new FileOutputStream("q.numb"));
			out.print(q.toString());
 			out.close();
         } catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	void run(String[] args) throws Exception{
 
 		/* 
 		 * This below code checks randomness of our createBigNumber method
 		 */
 		if (DEBUG) {
 			
 			System.out.println("gcd " + gcd(BigInteger.valueOf(12), BigInteger.valueOf(16)).toString());
 
 			int[] count = new int[100];
 			int i;
 			for (i = 1; i <= 100000; i++) 
 				count[createBigNumber(6).intValue()]++;
 			
 			for (i = 31; i<= 64; i++) System.out.print(count[i] + " ");
 			
 			System.out.println("\n");
 
 			// check create prime number method
 			// result: It works correctly but slow (FIX!)
 			System.out.println(createBigPrimeNumber(512).toString());
 			
 			SecureRandom rnd = new SecureRandom();
 			System.out.println(BigInteger.probablePrime(1024, rnd).toString());
 		}
 
 		if (args.length != 2) showHelp();
 
 		//try {
 			/// TODO: bitSize = Integer.parser(args[0]);  DONE
 			bitSize = Integer.parseInt(args[0]);
 			fileName = args[1];
 			
 			System.out.print("Create p, q primes");
 			BigInteger[] rl = createpq(bitSize);
 			System.out.println("		[DONE]");
 
 			BigInteger p = rl[0];
 			BigInteger q = rl[1];
 			System.out.print("Find e number");
 			BigInteger e = finde(p, q);
 			System.out.println("			[DONE]");
 			System.out.print("Find d number");
 			BigInteger d = findd(e, p, q);
 			System.out.println("			[DONE]");
 			BigInteger n = p.multiply(q);
 			//int size = n.bitLength()/8;
 			//int datasize = size - 11;
 			writeToDisk(e, d, p, q);
 			System.out.println("Write to disk			[DONE]");
 			System.out.print("Encrypt	file");
 			encryptFile(fileName, e, n);
 			System.out.println("			[DONE]");
 			System.out.print("Decrypt file");
 			decryptFile("out.encrypt", d, n);
 			System.out.println("			[DONE]");
 	}
 
     public static void main(String[] args) throws Exception{
         System.out.println("Welcome to Crypto Assignment\nAuthors: Thong Nguyen & Khoi Nguyen\n");
         
         new RSA().run(args);
     }
 
 }
