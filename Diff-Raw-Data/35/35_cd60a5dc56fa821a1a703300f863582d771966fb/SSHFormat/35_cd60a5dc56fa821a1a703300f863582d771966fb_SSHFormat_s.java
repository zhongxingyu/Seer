 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.DatatypeConverter;
 
 public class SSHFormat {
 
 	private static final boolean DEBUG = false;
 
 	private BigInteger readInputFile(String fileName) throws IOException {
 		FileInputStream in = null;
		StringBuffer strContent = new StringBuffer("");
 
 		try {
 			in = new FileInputStream(fileName);
			int len;
			while ((len = in.read()) != -1) {
				strContent.append(len);
			}
 
			String string = new String(strContent);
 
 			BigInteger bigInteger = new BigInteger(string);
 			return bigInteger;
 
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 	}
 
 	private void writeOutputFile() {
 		PrintStream out = null;
 		try {
 			out = new PrintStream(new FileOutputStream("key.pem"));
 			out.print(keyPem);
 			out.close();
 
 			out = new PrintStream(new FileOutputStream("key.pub"));
 			out.print(keyPub);
 			out.close();
 
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	BigInteger e;
 	BigInteger d;
 	BigInteger p;
 	BigInteger q;
 	BigInteger n;
 	BigInteger ex1;
 	BigInteger ex2;
 	BigInteger co;
 	String keyPub;
 	String keyPem;
 
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
 
 	BigInteger gcd(BigInteger a, BigInteger b) {
 		BigInteger[] rl = extended_gcd(a, b);
 		return a.multiply(rl[0]).add(b.multiply(rl[1]));
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
 
 	public SSHFormat() throws IOException {
 		// TODO Auto-generated constructor stub
 		e = readInputFile("e.numb");
 		d = readInputFile("d.numb");
 		p = readInputFile("p.numb");
 		q = readInputFile("q.numb");
         n = p.multiply(q);
         ex1 = d.mod(p.subtract(BigInteger.ONE));
         ex2 = d.mod(q.subtract(BigInteger.ONE));
         BigInteger[] rl = extended_gcd(q, p);
         co = rl[0].add(p).mod(p);
 
         
 
 	}
 
 	public void run() throws IOException {
 		PerformGenerateKeyPublic();
 		PerformGenerateKeyPem();
 		System.out.println("Create key.pub			[DONE]");
 		System.out.println("Create key.pem			[DONE]");
 		writeOutputFile();
 	}
 
 	public void test() {
 		System.out.println(keyPub.toString());
 		System.out.println("\n\n");
 		System.out.println(keyPem.toString());
 		
 	}
 
 	private void PerformGenerateKeyPem() throws IOException {
 		// TODO Auto-generated method stub
 		StringBuilder content = new StringBuilder();
 		content.append("-----BEGIN RSA PRIVATE KEY-----\n");
 
 		/**
 		 * Perform format here
 		 */
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 
         /* Version */
         byte[] ver = new byte[] {0x02, 0x01, 0x00};
 
         /* Encode n */
         byte[] bn = n.toByteArray();
         if (bn[0] == 0) {
             byte[] tmp = new byte[bn.length - 1];
             System.arraycopy(bn, 1, tmp, 0, tmp.length);
             bn = tmp;
         }
         byte[] pn = concat(encodeLen(bn.length), bn);
 
         /* Encode e */
         byte[] be = e.toByteArray();
         if (be[0] == 0) {
             byte[] tmp = new byte[be.length - 1];
             System.arraycopy(be, 1, tmp, 0, tmp.length);
             be = tmp;
         }
         byte[] pe = concat(encodeLen(be.length), be);
 
         /* Encode d */
         byte[] bd = d.toByteArray();
         if (bd[0] == 0) {
             byte[] tmp = new byte[bd.length - 1];
             System.arraycopy(bd, 1, tmp, 0, tmp.length);
             bd = tmp;
         }
         byte[] pd = concat(encodeLen(bd.length), bd);
 
         /* Encode p */
         byte[] bp = p.toByteArray();
         if (bp[0] == 0) {
             byte[] tmp = new byte[bp.length - 1];
             System.arraycopy(bp, 1, tmp, 0, tmp.length);
             bp = tmp;
         }
         byte[] pp = concat(encodeLen(bp.length), bp);
 
         /* Encode q */
         byte[] bq = q.toByteArray();
         if (bq[0] == 0) {
             byte[] tmp = new byte[bq.length - 1];
             System.arraycopy(bq, 1, tmp, 0, tmp.length);
             bq = tmp;
         }
         byte[] pq = concat(encodeLen(bq.length), bq);
 
         /* Encode ex1 */
         byte[] bex1 = ex1.toByteArray();
         if (bex1[0] == 0) {
             byte[] tmp = new byte[bex1.length - 1];
             System.arraycopy(bex1, 1, tmp, 0, tmp.length);
             bex1 = tmp;
         }
         byte[] pex1 = concat(encodeLen(bex1.length), bex1);
 
         /* Encode ex2 */
         byte[] bex2 = ex2.toByteArray();
         if (bex2[0] == 0) {
             byte[] tmp = new byte[bex2.length - 1];
             System.arraycopy(bex2, 1, tmp, 0, tmp.length);
             bex2 = tmp;
         }
         byte[] pex2 = concat(encodeLen(bex2.length), bex2);
 
         /* Encode co */
         byte[] bco = co.toByteArray();
         if (bco[0] == 0) {
             byte[] tmp = new byte[bco.length - 1];
             System.arraycopy(bco, 1, tmp, 0, tmp.length);
             bco = tmp;
         }
         byte[] pco = concat(encodeLen(bco.length), bco);
 
         int len = ver.length + pn.length + pe.length + pd.length + pp.length + pq.length +
             pex1.length + pex2.length + pco.length;
 
         byte[] trick = encodeLen(len);
         trick[0] = 0x30;
         out.write(trick);
         out.write(ver);
         out.write(pn);
         out.write(pe);
         out.write(pd);
         out.write(pp);
         out.write(pq);
         out.write(pex1);
         out.write(pex2);
         out.write(pco);
 
 		/* -------------------------------------------------- */
 		String body = new String(DatatypeConverter.printBase64Binary(out
 				.toByteArray()));
 
 		if (DEBUG) {
 			System.out.println("DEBUG");
 			System.out.println(out);
 			System.out.println(body);
 		}
 
 		content.append(splitStringFixedSize(body, 70));
 
 		content.append("-----END RSA PRIVATE KEY-----\n");
 
 		keyPem = new String(content);
 	}
 
 	private void PerformGenerateKeyPublic() throws IOException {
 		// TODO Auto-generated method stub
 		StringBuilder content = new StringBuilder();
 		content.append("---- BEGIN SSH2 PUBLIC KEY ----\n");
 
 		/**
 		 * Perform format here
 		 */
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 		/* Write format identifier */
 		byte[] sshrsa = new byte[] { 0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's',
 				'a' };
 		out.write(sshrsa);
 
 		/* Encode the e */
 		byte[] data = e.toByteArray();
 		encodeUInt32(data.length, out);
 		out.write(data);
 
 		/* Encode the n */
 		data = (p.multiply(q)).toByteArray();
 		encodeUInt32(data.length, out);
 		out.write(data);
 
 		/* -------------------------------------------------- */
 		String body = new String(DatatypeConverter.printBase64Binary(out
 				.toByteArray()));
 
 		if (DEBUG) {
 			System.out.println("DEBUG");
 			System.out.println(out);
 			System.out.println(body);
 			System.out.println(splitStringFixedSize(body, 70));
 		}
 
 		content.append(splitStringFixedSize(body, 70));
 
 		content.append("---- END SSH2 PUBLIC KEY ----\n");
 
 		keyPub = new String(content);
 	}
 
 	public static StringBuilder splitStringFixedSize(String str, int fixed_size)
 	{
 		int pos = 0;
 		String tmp = "";
 		StringBuilder result = new StringBuilder();
 		List<String> returnValue = new ArrayList<String>((str.length()
 				+ fixed_size - 1)/ fixed_size);
 
 		for (pos = 0; pos < str.length(); pos += fixed_size)
 		{
 			tmp = str.substring(pos, Math.min(str.length(), pos + fixed_size));
 			returnValue.add(tmp);
 		}
 		
 		for (String string : returnValue) {
 			result.append(string+ '\n');
 		}
 
 		return result;
 	}
 
     byte[] concat(byte[] a, byte[] b) {
         byte[] result = new byte[a.length + b.length]; 
             System.arraycopy(a, 0, result, 0, a.length); 
                 System.arraycopy(b, 0, result, a.length, b.length); 
                     return result;
     } 
 
     public byte[] encodeLen(int len) {
         if (len <= 127) {
             byte[] rl = new byte[]{0x02, 0x00};
             rl[1] = (byte) len;
             return rl;
         } else {
             BigInteger b = BigInteger.valueOf(len);
             byte[] by = b.toByteArray();
             if (by[0] == 0) {
                 byte[] tmp = new byte[by.length-1];
                 System.arraycopy(by, 1, tmp, 0, tmp.length);
                 by = tmp;
             }
             byte[] fi = new byte[1];
             fi[0] = (byte) (0x80 | by.length);
             byte[] trick = new byte[] {0x02};
             return concat(trick, concat(fi, by));
         }
     }
 	public void encodeUInt32(int value, OutputStream out) throws IOException {
 		byte[] tmp = new byte[4];
 		tmp[0] = (byte) ((value >>> 24) & 0xff);
 		tmp[1] = (byte) ((value >>> 16) & 0xff);
 		tmp[2] = (byte) ((value >>> 8) & 0xff);
 		tmp[3] = (byte) (value & 0xff);
 		out.write(tmp);
 	}
 
 	public static void main(String[] args) throws IOException {
 		SSHFormat sshFormat = new SSHFormat();
 		sshFormat.run();
 		// sshFormat.test();
 	}
 }
