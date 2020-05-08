 package complexion.test;
 
 import complexion.server.Atom;
 
 public class Test_verb {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Atom A = new Atom();
		Object[] met_args = {"Head"};
 		A.callVerb("printTest", met_args);
 	}
 
}
