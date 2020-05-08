 package com.gmail.at.zhuikov.aleksandr.driveddoc.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
import java.security.Security;
 import java.security.cert.CertificateException;
 
import org.bouncycastle.jce.provider.BouncyCastleProvider;

 public class SignatureContainerService {
 
 	private static SignatureContainerService instance;
 
 	public static SignatureContainerService getInstance() {
 
 		if (instance == null) {
 			instance = new SignatureContainerService();
 		}
 
 		return instance;
 	}
 	
 	public boolean isValid(InputStream container, String pass) {
 		try {
 			getKeyStoreInstance().load(container, pass.toCharArray());
 		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
 			return false;
 		}
     	
 		return true;
 	}
 
 	private KeyStore getKeyStoreInstance() {
 		try {
			Security.addProvider(new BouncyCastleProvider());
 			return KeyStore.getInstance("PKCS12", "BC");
 		} catch (KeyStoreException | NoSuchProviderException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
