 package util;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import com.novell.ldap.util.Base64;
 
 public enum Encryptor {
 	MD5("MD5"), SHA("SHA");
 
 	public static final Encryptor JAMA_DEFAULT = Encryptor.SHA;
 
 	private String alg;
 
 
 	private Encryptor(String alg) {
 		this.alg = alg;
 	}
 
 
 	public String getAlg() {
 		return alg;
 	}
 
 
 	public String encrypt(String pwdPlainText) throws NoSuchAlgorithmException {
 		MessageDigest md = MessageDigest.getInstance(alg);
 		md.update(pwdPlainText.getBytes());
 		return Base64.encode(md.digest());
 	}
 
 
 	public boolean areEquals(String plainPwd, String encrypted, boolean encryptedWithPrefix) throws NoSuchAlgorithmException {
 		String arg1, arg2;
 
 		arg1 = encrypt(plainPwd);
 		if (encryptedWithPrefix) {
 			arg2 = encrypted.replace("{" + alg + "}", "").trim();
 		}
 		else {
 			arg2 = encrypted;
 		}
 
 		return arg1.equals(arg2);
 	}
 }
