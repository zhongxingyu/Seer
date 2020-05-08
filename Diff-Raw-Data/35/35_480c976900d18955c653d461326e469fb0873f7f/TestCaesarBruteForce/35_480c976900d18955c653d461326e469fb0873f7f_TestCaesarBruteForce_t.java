 /**
  * 
  */
 package tools;
 
 import java.util.Map;
 
 import krypto.tools.CaesarBruteForce;
 
 /**
  * @author MWeigert
  *
  */
 public class TestCaesarBruteForce {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
		String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 		String str = "VHqDP zqwJ yC AECFBuJJ LuHMqxHBEIJuD jqNy GKuH tKHsx RqOuHD";
 		CaesarBruteForce cb = new CaesarBruteForce(str.toUpperCase());
 		Map<Character, String> report = cb.getReport();
 		System.out.println("Cipher:       " + cb.getCipher());
 		System.out.println("******************************************" +
				"*******************************");
		for (int i = 0; i < 26; i++) {
			System.out.println("Key: " + abc.charAt(i) + " Plain: " + report.get(abc.charAt(i)));
 		}
 	}
 
 }
