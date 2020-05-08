 package org.birch.cryptomainia;
 
import java.io.Console;
 import java.util.Properties;
 
 import org.jasypt.util.text.BasicTextEncryptor;
 
 public class EncryptArgv extends Mainia {
 
 	public EncryptArgv() {
 		cryptoProperties = new Properties();
 	}
 
 	public static void main(String[] args) throws Exception {
		Console console = System.console();
		if (console == null) {
			error("Couldn't get Console instance");
 		}
		console.printf("Password: ");
		char[] password = console.readPassword();
		EncryptArgv self = new EncryptArgv();
		self.readKey();
		System.out.println(self.encrypt(new String(password)));
		java.util.Arrays.fill(password, ' ');
 	}
 
	public String encrypt(String password) throws Exception {
 		if (isAlgorithm("tolower")) {
			return password.toUpperCase();
 		} else if (isAlgorithm("org.jasypt.util.text.BasicTextEncryptor")) {
 			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
 			textEncryptor.setPassword(getKey());
			String myEncryptedText = textEncryptor.encrypt(password);
 			return myEncryptedText;
 		} else
 			throw new Exception("Bad algorithm: " + getAlgorithm());
 	}
 
 }
