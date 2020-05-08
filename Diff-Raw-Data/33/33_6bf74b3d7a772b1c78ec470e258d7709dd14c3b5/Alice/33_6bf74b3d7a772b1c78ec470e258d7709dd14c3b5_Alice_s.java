 package Aufgabe2Diffie;
 
 import java.math.BigInteger;
 
 public class Alice {
 	
 	private static final BigPrimeN genN = BigPrimeN.create();
 	private static final BigInteger n = genN.next();
 	
 	private static final SmallIntegerG genG = SmallIntegerG.create(n);
 	private static final BigInteger g = genG.next();
 	
 	private final BigInteger x;
 	private BigInteger Kab;
 	
 	private Alice(BigInteger x){
 		this.x = x;
 	}
 	
 	public static Alice create(BigInteger x){
 		return new Alice(x);
 	}
 	
 	public void sendGyMODn(BigInteger GyMODn){
 		//Kab berechnen mit (y hoch yx) mod n 
 		Kab = GyMODn.modPow(x, n);
 	}
 
 	public BigInteger n(){
 		return n;
 	}
 	
 	public BigInteger g(){
 		return g;
 	}
 	
 	public BigInteger x(){
 		return x;
 	}
 	
 	public BigInteger Kab(){
 		return Kab;
 	}
 	
 	public void init(Bob bob){
 		bob.sendG(g);
 		bob.sendN(n);
 		bob.sendGxMODn(g.modPow(x, n));
 		bob.reply(this);
 	}
 
 	@Override
 	public String toString() {
		return "Alice(x=" + x + ", Kab=" + Kab + ", n=" + n() + ", g="
				+ g() + ")";
 	}
 	
 }
