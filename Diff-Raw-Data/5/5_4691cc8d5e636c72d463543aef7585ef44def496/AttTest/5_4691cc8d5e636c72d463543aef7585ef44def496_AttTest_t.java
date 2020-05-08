 import java.util.Random;
 
 
 public class AttTest {
 	public static void main(String[] args) throws Exception {
 		System.out.println("@relation vreme" + "\n");
 
 		String[] opis = {"hladno", "toplo", "vetrovno", "deevno", "snei"};
 		int temperatura = 0;
 		String[] veter = {"0", "5", "10", "20", "50"};
 		String[] moznost_padavin = {"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
 
 		String zdruzi = "";
 		zdruzi = zdruzi + opis[0];
		for (int i=1; i<opis.length; i++){
 			if (i<opis.length)
 				zdruzi = zdruzi + ",";
 			zdruzi = zdruzi + opis[i];
 		}
 		System.out.println("@attribute opis {" + zdruzi + "}");
 		
 		System.out.println("@attribute temperatura NUMERIC");
 
 		zdruzi = "";
 		zdruzi = zdruzi + veter[0];
		for (int i=1; i<veter.length; i++){
 			if (i<veter.length)
 				zdruzi = zdruzi + ",";
 			zdruzi = zdruzi + veter[i];
 		}
 		System.out.println("@attribute veter {" + zdruzi + "}");
 
 		zdruzi = "";
 		zdruzi = zdruzi + moznost_padavin[0];
 		for (int i=1; i<moznost_padavin.length; i++){
 			if (i<moznost_padavin.length)
 				zdruzi = zdruzi + ",";
 			zdruzi = zdruzi + moznost_padavin[i];
 		}
 		System.out.println("@attribute moznost_padavin {" + zdruzi + "}" + "\n" +
 				"\n" + "@data");
 
 		Random r = new Random();
 		for (int i=0; i<2000; i++){
 		temperatura = r.nextInt(35);
 		System.out.println(opis[r.nextInt(opis.length)] + "," + temperatura +
 				"," + veter[r.nextInt(veter.length)] + "," + moznost_padavin[r.nextInt(moznost_padavin.length)]);
 		}
 		
 
 	}
 }
