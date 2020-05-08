 package com.android.locproof.stamp;
 
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.security.interfaces.DSAKeyPairGenerator;
 import java.security.interfaces.DSAPrivateKey;
 import java.security.interfaces.DSAPublicKey;
 import java.security.interfaces.RSAPrivateKey;
 import java.security.interfaces.RSAPublicKey;
 import java.security.spec.DSAPrivateKeySpec;
 import java.security.spec.DSAPublicKeySpec;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 /**
  * @author Oscar
  *
  */
 public class CryptoUtil {
 	
 //	private static final String SHA1_PRNG = "SHA1PRNG";
 //	private static final SecureRandom _sRandom;
 //	static {
 //	    try {
 //	    	_sRandom = SecureRandom.getInstance( SHA1_PRNG );
 //	    } catch ( NoSuchAlgorithmException e ) {
 //	        throw new Error(e);
 //	    }
 //	}
 	
 	// Default big length of random numbers used in string commitments (= bit length of SHA1 output)
 	private static final int DEFAULT_BIT_LENGTH = 160;
 	
 	private static final SecureRandom _sRandom;
 	static {
 	    try {
 	    	_sRandom = SecureRandom.getInstance("SHA1PRNG");
 	    } catch ( NoSuchAlgorithmException e ) {
 	        throw new Error(e);
 	    }
 	}
 
 	
 	public static void main(String []args) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
 		byte[] test = "test".getBytes();
 		KeyPair kp = generateDSAKeyPair(512);
 		byte[] sig = signDSA((DSAPrivateKey) kp.getPrivate(), test);
 		System.out.println(sig);
 		
 		byte[] sha = SHA1(test);
 		System.out.println(sha);
 
 		BigInteger salt = getRandomSecureNumber();
 		Commitment c = getCommitment(test, salt);
 		
 		if(deCommit(test, salt, c)){
 			System.out.println("Verified!");
 		}
 	}
 	
 	/**
 	 * Direct generation of random numbers used for commitments
 	 * @return
 	 */
 	public static BigInteger getRandomSecureNumber() {
 		return getRandomSecureNumber(DEFAULT_BIT_LENGTH);
 	}
 	
 	/**
 	 * Secure random number generation with a specified bit length
 	 * @param bitLength
 	 * @return
 	 */
 	public static BigInteger getRandomSecureNumber(int bitLength){
 		return new BigInteger(bitLength, _sRandom);
 	}
 	
 	/**
 	 * Generate the SHA1 hash of a message 
 	 * @param message
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 * @throws UnsupportedEncodingException
 	 */
 	public static byte[] SHA1(byte[] message) 
 			throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
 		MessageDigest md;
 		md = MessageDigest.getInstance("SHA-1");
 		md.update(message);
 		return md.digest();
 	}
 	
 	/**
 	 * Generate a hash chain of a random number
 	 * @param r1
 	 * @param numHash
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 * @throws UnsupportedEncodingException
 	 */
 	public static LinkedList<BigInteger> getHashChain(BigInteger r1, int numHash) throws NoSuchAlgorithmException, UnsupportedEncodingException{
 		
 		LinkedList<BigInteger> hashChain = new LinkedList<BigInteger>();
 		hashChain.add(r1);
 		
 		for(int i = 1; i<numHash; i++){
 			byte[] lastR = hashChain.getLast().toByteArray();
 			hashChain.add(new BigInteger(1, SHA1(lastR))); 
 		}
 		
 		return hashChain;
 	}
 
 	// DSA operations
 
 	/**
 	 * Generation of a DSA key pair
 	 * @param keySize
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeySpecException
 	 */
 	public static KeyPair generateDSAKeyPair(int keySize) throws NoSuchAlgorithmException, InvalidKeySpecException {
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
 		kpg.initialize(keySize);
 
 		return  kpg.genKeyPair();
 	}
 	
 	public static DSAPublicKey getDSAPubKeyfromEncoded(byte[] encKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
 		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
 		KeyFactory kf= KeyFactory.getInstance("DSA");
 		return (DSAPublicKey) kf.generatePublic(pubKeySpec);
 	}
 	
 	public static DSAPrivateKey getDSAPriKeyfromEncoded(byte[] encKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
 		KeyFactory kf= KeyFactory.getInstance("DSA");
 		return (DSAPrivateKey) kf.generatePrivate(pubKeySpec);
 	}
 
 	/**
 	 * Sign on a message with DSA private key
 	 * @param priKey
 	 * @param message
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeyException
 	 * @throws SignatureException
 	 */
 	public static byte[] signDSA(DSAPrivateKey priKey, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
 		Signature sig = Signature.getInstance("SHA1withDSA");
 		sig.initSign(priKey);
 		
 		sig.update(message);
 		return sig.sign();
 	}
 
 	/**
 	 * Signature verification with DSA public key, original message and signed message
 	 * @param pubKey
 	 * @param message
 	 * @param signedMessage
 	 * @return
 	 * @throws SignatureException
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeyException
 	 */
 	public static boolean verifyDSASig(DSAPublicKey pubKey, byte[] message, byte[] signedMessage) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException{
 		Signature sig = Signature.getInstance("SHA1withDSA");
 		sig.initVerify(pubKey);
 		sig.update(message);
 		return sig.verify(signedMessage);
 	}
 
 	// RSA operations
 
 	/**
 	 * Generation of a RSA key pair
 	 * @param keySize
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeySpecException
 	 */
 	public static KeyPair generateRSAKeyPair(int keySize) throws NoSuchAlgorithmException, InvalidKeySpecException {
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		kpg.initialize(keySize);
 
 		return kpg.genKeyPair();
 	}
 	
 	public static RSAPublicKey getRSAPubKeyfromEncoded(byte[] encKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
 		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
 		KeyFactory kf= KeyFactory.getInstance("RSA");
 		return (RSAPublicKey) kf.generatePublic(pubKeySpec);
 	}
 	
 	public static RSAPrivateKey getRSAPriKeyfromEncoded(byte[] encKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
 		KeyFactory kf= KeyFactory.getInstance("RSA");
 		return (RSAPrivateKey) kf.generatePrivate(pubKeySpec);
 	}
 
 	/**
 	 * RSA encryption
 	 * @param pubKey
 	 * @param message
 	 * @return
 	 * @throws IllegalBlockSizeException
 	 * @throws BadPaddingException
 	 * @throws NoSuchPaddingException
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeyException
 	 */
 	public static byte[] encryptRSA(RSAPublicKey pubKey, byte[] message) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException{
 		Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
 		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
 
 		return cipher.doFinal(message);
 	}
 
 	/**
 	 * RSA decryption
 	 * @param priKey
 	 * @param message
 	 * @return
 	 * @throws IllegalBlockSizeException
 	 * @throws BadPaddingException
 	 * @throws NoSuchPaddingException
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeyException
 	 */
 	public static byte[] decryptRSA(RSAPrivateKey priKey, byte[] message) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException{
 		Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
 		cipher.init(Cipher.DECRYPT_MODE, priKey);
 
 		return cipher.doFinal(message);
 	}
 
 	// String commitment
 	// http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.57.2794	
 	
 	/**
 	 * Commit a message in byte array form with a random salt
 	 * @param message
 	 * @param salt
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 * @throws UnsupportedEncodingException
 	 */
 	public static Commitment getCommitment(byte[] message, BigInteger salt) throws NoSuchAlgorithmException, UnsupportedEncodingException{		
 		BigInteger s = new BigInteger(1, SHA1(message));
 		BigInteger a = getRandomSecureNumber(DEFAULT_BIT_LENGTH);
 		BigInteger b = s.subtract(a.and(salt));
 		BigInteger y = new BigInteger(1, SHA1(salt.toByteArray()));
 		
 		return new Commitment(a.toByteArray(), b.toByteArray(), y.toByteArray());
 	}
 	
 	/**
 	 * Decommit and verify a commitment 
 	 * @param message
 	 * @param salt
 	 * @param c
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 * @throws UnsupportedEncodingException
 	 */
 	public static boolean deCommit(byte[] message, BigInteger salt, Commitment c) throws NoSuchAlgorithmException, UnsupportedEncodingException{
 		BigInteger a = new BigInteger(1, c.getA());
 		BigInteger b = new BigInteger(1, c.getB());
 		BigInteger y = new BigInteger(1, c.getY());
 		BigInteger s = new BigInteger(1, SHA1(message));
 		BigInteger y1 = new BigInteger(1, SHA1(salt.toByteArray()));
 		
 		if (y.equals(y1) && b.equals(s.subtract(a.and(salt)))){
 			return true;
 		}
 		return false;
 	}
 	
 	
 	//Bussard-Bagga specific operations
 	
 	/**
 	 * This is used to get h only
 	 * @param p
 	 * @return
 	 */
 	public static BigInteger getH(BigInteger p){
 		BigInteger rand = getRandomSecureNumber(p.bitLength());
 		return rand.mod(p);
 	}
 
 	/**
 	 * This is used to get u only
 	 * @param p
 	 * @return
 	 */
 	public static BigInteger getU(BigInteger p){
 		BigInteger rand = getRandomSecureNumber(p.bitLength());
 		return rand.mod(p.subtract(BigInteger.ONE));
 	}
 
 	/**
 	 * This is used to get any random nonce depending on p, including r, k, v
 	 * @param p
 	 * @return
 	 */
 	public static BigInteger getNonce(BigInteger p){
 		BigInteger rand = getRandomSecureNumber(p.bitLength());
 		return rand.mod(p);
 	}
 
 	/**
 	 * Symmetric encryption of private key x
 	 * @param u
 	 * @param k
 	 * @param p
 	 * @param x
 	 * @return
 	 */
 	public static BigInteger getE(BigInteger u, BigInteger k, BigInteger p, BigInteger x){
 		return u.multiply(x).subtract(k).mod(p.subtract(BigInteger.ONE));
 	}
 
 	/**
 	 * Get an array of bit commitments of e or k
 	 * @param g
 	 * @param h
 	 * @param v
 	 * @param ek
 	 * @return
 	 */
 	public static ArrayList<byte[]> getBitCommitments(BigInteger g, BigInteger p, BigInteger h, BigInteger v, BigInteger ek){
 		ArrayList<byte[]> cs = new ArrayList<byte[]>();
 		for (int i = 0; i< ek.bitCount(); i++){
 			BigInteger c;
 			if (ek.testBit(i)){
 				c = (g.multiply(h.modPow(v, p))).mod(p);
 				// Unable to do BigInteger.pow(BigInteger), not sure about the mathematical requirement here
 			}else{
 				c = h.modPow(v, p);
 			}
 			cs.add(c.toByteArray());
 		}
 		return cs;
 	}
 
 
 	/*ZK proof*/
 	public static BigInteger getZ(ArrayList<byte[]> c1, ArrayList<byte[]> c2){
 		BigInteger z = BigInteger.ZERO;
 		
 		if(c1.size() != c2.size()){
 			return z; 
 		}
 		
 		for(int i=0; i<c1.size(); i++){
 			BigInteger c1BI = new BigInteger(1, c1.get(i));
 			BigInteger c2BI = new BigInteger(1, c2.get(i));
 			z = z.add(c1BI.multiply(c2BI).pow((int) Math.pow(2, i)));
 		}
 		return z;
 	}
 
 }
