 package complexion.test;
 
 import complexion.server.Atom;
 
 public class Test_verb {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Atom A = new Atom();
		Object[] met_args = new Object[2];
		met_args[0] = "Head";
		met_args[1] = "Beep";
 		A.callVerb("printTest", met_args);
		
 	}
 
}
