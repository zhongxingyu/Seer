 
 public class Enc {
 	// n is the block size, m is R's size
 	public static int n = 32;
 	public static int m = 16;
 
 	public static byte[][] toBlocks(String plaintext)
 	{
 		int remainder = plaintext.length()%n;
 		// right pad the string with space
 		if (remainder != 0) {
 			String pad = String.format("%1$-"+ remainder +"s","");
 			plaintext = plaintext + pad;
 		}
 		// find total number of blocks
 		int blockNum = plaintext.length()/n;
 		// convert string to byte array
 		String[] strArray = new String[blockNum];
 		byte[][] wordArray = new byte[blockNum][n];
 		for (int i = 0; i < blockNum; i++){
 			strArray[i] = plaintext.substring(i*n,(i+1)*n);
 			wordArray[i] = strArray[i].getBytes();
 			//System.out.println(byteArray[i]);
 		}
 		return wordArray;
 	}
 	
 	public static byte[] preEnc(byte[][] wordArray, int blockIndex, byte[] key2)
 	{	
 		byte[] word = wordArray[blockIndex];
 		//encrypt Word with E(key2)
 		return TwoBlockEncrypt(word, key2);
 	}
 	
 	public static byte[] getPubkey(byte[] X, byte[] key1)
 	{
 		// pass first n-m bytes of X to L
 		byte[] L = new byte[n-m];
 		for(int i = 0; i < n-m; i++){
 			L[i] = X[i];
 		}
 		//generate public key using f and key1, k = fkey1(L);
 		byte[] ki = PRF.PRF(L, key1);
 		return ki;
 	}
 	
 	public static byte[][] streamCipher(){
 		//generate streamCipher S = S1, S2, ... , Sl
 		return null;
 	}
 	
 	public static byte[] getT(int blockIndex, byte[][] S, byte[] key){
 		byte[] Si = new byte[n-m];
 		Si = S[blockIndex];
 		byte[] FkeySi = PRF.PRF(Si, key);
 		byte[] Ti = new byte[n];
 		// Ti = <Si, Fkey(si)>
 		System.arraycopy(Si, 0, Ti, 0, n-m);
 		System.arraycopy(FkeySi, 0, Ti, n-m, m);
 				
 		return Ti;
 	}
 	
 	public static byte[] getC(byte[]Xi, byte[]Ti){
 		byte[] Ci = new byte[n];
 		int k = 0;
	while(k < Xi.length) {
 			Ci[k] = (byte) (Xi[k] ^Ti[k]);
 			k++;
 		}
	return Ci;
 	}
 		
 }
