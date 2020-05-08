 package adp2.app;
 
 import adp2.parser.TspFile;
 
 public class Controller {
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {;
		TspFile t = TspFile.open("samples/ant9.tsp");
 		System.out.println(t.matrix());
 	}
 }
