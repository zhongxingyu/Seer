 package org.softwaresynthesis.mytalk.server.authentication.security;
 
 /**
  * Algoritmo di crittografia AES a 128 bit
  * 
  * @author 	Andrea Meneghinello
  * @version 3.0
  */
 public class AESAlgorithm implements ISecurityStrategy
 {
 	private CryptoTemplate template;
 	
 	@Override
 	public String encode(String plainText) throws Exception 
 	{
 		String result = null;
		this.template = new Encode();
		result = this.template.execute(plainText);
 		return result;
 	}
 
 	@Override
 	public String decode(String encodedText) throws Exception 
 	{
 		String result = null;
		this.template = new Decode();
		result = this.template.execute(encodedText);
 		return result;
 	}
 }
