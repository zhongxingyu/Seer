 package main.java.CryptoEngine;
 
 import java.math.BigInteger;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import main.java.RSAEngine.KeyGen;
 
 public class CertificateGenerator {
 	private String firstname;
 	private String lastname;
 	private String username;
 	private String email;
 	private String notes;
 	
 	public CertificateGenerator() {
 		firstname = "firstn";
 		lastname = "lastn";
 		username = "user";
 		email = "email";
 		notes = "";
 	}
 	
 	public CertificateGenerator(String fn, String ln, String usern, String em) {
 		firstname = fn;
 		lastname = ln;
 		username = usern;
 		email = em;
 		notes = "";
 	}
 	
 	public CertificateGenerator(String fn, String ln, String usern, String em, String note) {
 		firstname = fn;
 		lastname = ln;
 		username = usern;
 		email = em;
 		notes = note;
 	}
 	
 	public String generate(BigInteger mod, BigInteger public_exp) throws JSONException {
 		JSONObject j = new JSONObject();
 		
 		j.put("version", 1);
 		j.put("realname", lastname + ", " + firstname);
 		j.put("username", username);
 		j.put("email", email);
 		j.put("not_before", System.currentTimeMillis());
 		j.put("not_after", System.currentTimeMillis() + 31536000); // Year after not_before
 		j.put("notes", notes);
		j.put("modulus", mod.toString(16));			
		j.put("public_exponent", public_exp.toString(16));		
 		
 		return j.toString();		
 	}
 	public static void main(String args[]) throws IOException, JSONException, NoSuchAlgorithmException {
 		if(args.length < 4){					// wrong arguments
 			System.out.println("Please use the correct format");
 			System.out.println("First name, Last name, Username, Email, Notes");
 			return;
 		}
 		
 		KeyGen k = new KeyGen();
 		BigInteger[] arr = k.GenerateKey(1024); // arr = {e, d, n}
 		CertificateGenerator c;
 		Signature s = new Signature();
 		
 		if(args.length == 4)					// notes = ""
 			c = new CertificateGenerator(args[0], args[1], args[2], args[3]);
 		else									// notes not null
 			c = new CertificateGenerator(args[0], args[1], args[2], args[3], args[4]);
 		
 		FileWriter fstream = new FileWriter("out.txt"); // Writing the certificate and signature to a file
 	    BufferedWriter out = new BufferedWriter(fstream); 
 	    String str = c.generate(arr[2], arr[0]);		// Creating certificate
 	    String sig = s.generateSignature(args[2], str, arr[1], arr[2]); // Signing certificate
 	    
 	    Boolean b = s.verifySignature(sig, arr[0], arr[2]);	// Debugging steps
 	    System.out.println("Verify the Signature: "  + b);
 	    System.out.println(str);
 	    System.out.println(sig);
	    System.out.println("Private key: " + arr[1].toString(16));
 	    
 	    out.write("Certificate: " + str);							// Print the Certificate and Signed Certificate to out.txt
 	    out.write("\n");
 	    out.write("Signed Certificate: " + sig);
 	    
 	    out.close(); 	//Close the output stream
 	}
 }
