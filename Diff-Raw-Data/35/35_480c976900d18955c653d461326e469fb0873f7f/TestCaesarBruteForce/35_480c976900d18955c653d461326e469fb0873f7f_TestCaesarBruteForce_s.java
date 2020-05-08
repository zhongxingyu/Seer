 /**
  * 
  */
 package tools;
 
import java.io.File;
import java.util.Iterator;
 import java.util.Map;
 
import krypto.io.LoadTextFile;
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
		LoadTextFile ltf = new LoadTextFile();
		File file = new File("D:\\Daten\\Bunny\\Dropbox\\Semester04\\InformatikProjekt\\Grup" +
				"pen\\Gruppe_10_Weigert_Ljubicic\\Cipher\\Caesar.txt");
		ltf.loadFile(file);
 		String str = "VHqDP zqwJ yC AECFBuJJ LuHMqxHBEIJuD jqNy GKuH tKHsx RqOuHD";
 		CaesarBruteForce cb = new CaesarBruteForce(str.toUpperCase());
 		Map<Character, String> report = cb.getReport();
 		System.out.println("Cipher:       " + cb.getCipher());
 		System.out.println("******************************************" +
				"****************************************************");
		for (Iterator<Character> iter = report.keySet().iterator(); iter.hasNext();) {
			Character key = iter.next();
			System.out.println("Key: " + key + " Plain: " + report.get(key));
 		}
 	}
 
 }
