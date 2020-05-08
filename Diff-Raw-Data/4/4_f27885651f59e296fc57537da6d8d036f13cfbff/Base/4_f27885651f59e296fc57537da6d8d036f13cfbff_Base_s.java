 package glt;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Base {
 
 	// letter -> Base
 	static private Map<String, Base> all = new HashMap<String, Base>();
 	
 	// Base -> complement Base
 	static private Map<Base, Base> complements = new HashMap<Base, Base>();
 
 	// static initialiser to create all different bases.
 	// it has to be below the Maps so they are initialised now.
 	static {
 		
 		Base A = new Base("A");
 		Base C = new Base("C");
 		Base G = new Base("G");
 		Base T = new Base("T");
 		Base N = new Base("N");
 		
 		// set the complements for each base
 		A.setComplement(T);
 		C.setComplement(G);
 		G.setComplement(C);
 		T.setComplement(A);
 		N.setComplement(N);
 	}
 	
 	private String letter = null;
 	
 	private Base(String letter) {
 		this.letter = letter;
 		Base.all.put(letter, this);
 	}
 	
 	public String getLetter() {
 		return this.letter;
 	}
 	
 	static public Base forLetter(String letter) {
 		return Base.all.get(letter);
 	}
 	
 	private Base setComplement(Base base) {
 		Base.complements.put(this, base);
 		return this;
 	}
 	
	public Base getComplement(Base base) {
		return Base.complements.get(base);
 	}
 }
