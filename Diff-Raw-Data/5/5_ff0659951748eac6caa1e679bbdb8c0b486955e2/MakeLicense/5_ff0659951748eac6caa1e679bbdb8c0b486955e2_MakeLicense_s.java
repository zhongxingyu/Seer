 package org.xtremeturmoil.standalone;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 import org.apache.commons.io.FileUtils;
 import org.xtremeturmoil.simplelicense.model.SimpleLicence;
 
 /**
  * Class for building a license file.
  * @author jataylor2012
  *
  */
 public class MakeLicense {
 
 	/**
 	 * Stand-alone utility for building license.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length!=6) {
			System.err.println("Usage: keyFile validForDays companyName numberOfUnits startFrom(dd/mm/yyyy) output");
 		} else {
 			try {
 				String key = args[0];
 				int validForDays = Integer.valueOf(args[1]);
 				String name = args[2];
 				int numberOfUnits = Integer.valueOf(args[3]);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
 				Date start = sdf.parse(args[4]);
 				String output = args[5];
 				MakeLicense mklicence = new MakeLicense(validForDays, name, numberOfUnits, start);
 				mklicence.setKey(key);
 				byte[] cipherLicense = mklicence.create();
 				mklicence.write(output, cipherLicense);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private SimpleLicence licence;
 	private String key;
 	
 	public MakeLicense(int validForDays, String companyName, int numberOfUnits, Date start) {
 		licence = new SimpleLicence(validForDays, companyName, numberOfUnits, start);
 	}
 	
 	/**
 	 * Set the key you want to use to create the licence, typically
 	 * the private key. You will use the public to read the license
 	 * and distribute with your application.
 	 * @param key
 	 */
 	public void setKey(String key) {
 		this.key = key;
 	}
 	
 	/**
 	 * Returns the encrypted licence key.
 	 * @return
 	 * @throws InvalidKeyException
 	 * @throws NoSuchAlgorithmException
 	 * @throws NoSuchPaddingException
 	 * @throws IllegalBlockSizeException
 	 * @throws BadPaddingException
 	 */
 	public byte[] create() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
 		UseKeys encrypt = new UseKeys(key);
 		return encrypt.encrypt(licence.toString().getBytes());
 	}
 	
 	/**
 	 * Writes an encrypted license key to a file.
 	 * @param licenceFile
 	 * @param licence
 	 */
 	public void write(String licenceFile, byte[] licence) {
 		try {
 			FileUtils.writeByteArrayToFile(new File(licenceFile), licence);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 }
