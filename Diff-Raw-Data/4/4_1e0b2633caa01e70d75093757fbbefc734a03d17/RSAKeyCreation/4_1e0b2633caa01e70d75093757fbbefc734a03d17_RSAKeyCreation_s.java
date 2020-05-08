 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.security.spec.RSAKeyGenParameterSpec;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.Arrays;
 
 
 public class RSAKeyCreation {
 
 	/**
 	 * @param args
 	 * @throws NoSuchAlgorithmException 
 	 * @throws InvalidAlgorithmParameterException 
 	 * @throws InvalidKeySpecException 
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {
 		if (args.length != 1) {
 			System.out.println("Usage: java RSAKeyCreation <name>");
 		}
 		
 		String name = args[0];
 		
 		// Beispiel: java RSAKeyCreation KMueller
 		// erzeugt die Ausgabedateien KMueller.pub und  KMueller.prv
 		RSAKeyGenParameterSpec rsaKeyGenParameterSpec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
 		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
 		keyPairGenerator.initialize(rsaKeyGenParameterSpec);
 		
 		KeyPair keyPair = keyPairGenerator.generateKeyPair();
 		KeyFactory keyFac = KeyFactory.getInstance("RSA");
 		
 		X509EncodedKeySpec pubKey = keyFac.getKeySpec(keyPair.getPublic(), X509EncodedKeySpec.class);
 		PKCS8EncodedKeySpec prvKey = keyFac.getKeySpec(keyPair.getPrivate(), PKCS8EncodedKeySpec.class);
 		
 		byte[] pubEncoded = pubKey.getEncoded();
 		byte[] prvEncoded = prvKey.getEncoded();
 		
 		DataOutputStream pubOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(String.format("%s.pub",name))));
 		DataOutputStream prvOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(String.format("%s.prv",name))));
 		
 		// 1. Lnge des Inhaber-Namens (integer)
		pubOut.writeInt(name.getBytes().length -1);
		prvOut.writeInt(name.getBytes().length -1);
 		
 		// 2. Inhaber-Name (Bytefolge)
 		pubOut.writeBytes(name);
 		prvOut.writeBytes(name);
 		
 		// 3. Lnge des Schlssels (integer)
 		pubOut.writeInt(pubEncoded.length);
 		prvOut.writeInt(prvEncoded.length);
 		
 		// 4. Schlssel (Bytefolge)
 		pubOut.write(pubEncoded); // [X.509-Format]
 		prvOut.write(prvEncoded); // [PKCS8-Format]
 		
 		// close
 		pubOut.close();
 		prvOut.close();
 	}
 }
