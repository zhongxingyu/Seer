 package implementation;
 
 import java.util.Arrays;
 
 import edu.rit.util.Hex;
 import edu.rit.util.Packing;
 
 public class Blowfish01
 		implements BlockCipher{
 
 	byte[] P;
 	/**
 	 * Returns this block cipher's block size in bytes.
 	 *
 	 * @return  Block size.
 	 */
 	public int blockSize(){
 		//Blowfish uses 64 bit blocks
 		return 8;
 	}
 
 	/**
 	 * Returns this block cipher's key size in bytes.
 	 *
 	 * @return  Key size.
 	 */
 	public int keySize(){
 		//Blowfish has a variable length key ranging from 32-448 bits, 
 		//we have chosen to use a 64 bit key.
 		return 8;
 	}
 
 	/**
 	 * Set the key for this block cipher. <TT>key</TT> must be an array of bytes
 	 * whose length is equal to <TT>keySize()</TT>.
 	 *
 	 * @param  key  Key.
 	 */
 	public void setKey
 	   (byte[] key){}
 
 	/**
 	 * Encrypt the given plaintext. <TT>text</TT> must be an array of bytes
 	 * whose length is equal to <TT>blockSize()</TT>. On input, <TT>text</TT>
 	 * contains the plaintext block. The plaintext block is encrypted using the
 	 * key specified in the most recent call to <TT>setKey()</TT>. On output,
 	 * <TT>text</TT> contains the ciphertext block.
 	 *
 	 * @param  text  Plaintext (on input), ciphertext (on output).
 	 */
 	public void encrypt
 	   (byte[] text){
 		//long data = Packing.packLongBigEndian (text, 0);
 		long zeros = Packing.packLongBigEndian (Hex.toByteArray("1515151500000000"), 0);
		long xL = Packing.packLongBigEndian (text, 0) & zeros;
		long xR = Packing.packLongBigEndian (text, 3) << 32;
 
 		//16 round feistel network
 		for(int i=0; i<16; i++){
 			xL ^= P[i];
 			xR ^= F(xL);
 			long temp = xL;
 			xL = xR;
 			xR = temp;
 		}
 		
 		long temp = xL;
 		xL = xR;
 		xR = temp;
 		
 		xR ^= P[16];
 		xL ^= P[17];
 		xR >>= 32;
 		
 		long encrypted =  xL ^ xR;
 		Packing.unpackLongBigEndian(encrypted, text, 0);
 	}
 
 	private long F(long x){
 		return 0;
 	}
 	
 	/**
 	 * Test main program
 	 */
 	public static void main
 		(String[] args)
 		{
 		BlockCipher cipher = new Blowfish01();
 
 		byte[] plaintextZero = new byte [8];
 		Arrays.fill (plaintextZero, (byte)0x15);
 		cipher.encrypt(plaintextZero);
 
 		}
 
 }
