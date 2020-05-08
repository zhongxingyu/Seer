 package org.softwaresynthesis.mytalk.server.authentication.security;
 
 import java.security.Key;
 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 
 /**
  * Rappresenta la struttura dell'algoritmo
  * di crittografia AES a 128 bit
  * 
  * @author 	Andrea Meneghinello
  * @version	3.0
  */
 public abstract class CryptoTemplate 
 {
 	private byte[] key;
 	private Cipher cipher;
 	private Key generated;
 	
 	/**
 	 * Inizializza il CryptoTemplate con la chiave
 	 * di cifratura
 	 */
 	public CryptoTemplate()
 	{
 		this.key = new byte[]{'C', 'p', '2', 'Q', 'j', 'w', 'M', 'F', '7', 'e', 'j', 'N', 't', 'd', 'b', '1'};
 	}
 	
 	/**
 	 * Esegue l'operazione di crypto desiderata
 	 * 
 	 * @param 	text	{@link String} testo che deve
 	 * 					essere manipolato
 	 * @return	{@link String} testo manipolato
 	 * @throws 	Exception	{@link Exception} lanciata
 	 * 						in caso di errori durante la
 	 * 						procedura
 	 */
 	public String execute(String text) throws Exception
 	{
 		String result = null;
 		cipher = Cipher.getInstance("AES");
 		this.generated = new SecretKeySpec(this.key, "AES");
 		result = this.completeAlgorithm(text);
 		return result;
 	}
 	
 	/**
 	 * Restituisce il cifratore alle sottoclassi
 	 * in modo che possano criptare/decriptare il
 	 * testo
 	 * 
 	 * @return	{@link Cipher} cifratore
 	 */
 	protected Cipher getCipher()
 	{
 		return this.cipher;
 	}
 	
 	/**
 	 * Restituisce la chiave generata a runtime
 	 * a partire dalla chiave statica. Verrà
 	 * utilizzata nel processo di codifica/decodifica
 	 * 
 	 * @return {@link Key} chiave segreta
 	 */
 	protected Key getGeneratedKey()
 	{
 		return this.generated;
 	}
	
	/**
	 * Passo finale dell'algoritmo
	 * 
	 * @param 	text	{@link String} testo da manipolare
	 * @return	{@link String} testo manipolato
	 * @throws 	Exception	{@link Exception} in caso di
	 * 						errori durante il processo
	 */
	public abstract String completeAlgorithm(String text) throws Exception;
 }
