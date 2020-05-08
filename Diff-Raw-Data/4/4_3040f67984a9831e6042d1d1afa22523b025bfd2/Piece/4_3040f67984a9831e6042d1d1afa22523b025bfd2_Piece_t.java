 package core;
 
 import java.util.HashSet;
 
 
 public class Piece {
 	public enum ATTRIBUTES {
 		BIG,
 		RED,
 		SQUARE,
 		SOLID;
 	}
 	boolean[] attributes = new boolean[4];
 	
 	public Piece(boolean big, boolean red, boolean square, boolean solid) {
 		attributes[0] = big;
 		attributes[1] = red;
 		attributes[2] = square;
 		attributes[3] = solid;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o == null)
 			return false;
 		if (o == this)
 			return true;
 
 		if (!(o instanceof Piece))
 			return false;
 		Piece that = (Piece) o;
 
 		if (this.attributes[0] ==  that.attributes[0] && 
 				this.attributes[1] ==  that.attributes[1] && 
 				this.attributes[2] ==  that.attributes[2] && 
 				this.attributes[3] ==  that.attributes[3]) {
 			return true; 
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode() {
 		int ret = 0;
 		for (int i = 0; i < attributes.length; i++) {
 			ret += (attributes[i] ? Math.pow(2, i) : 0);
 		}
 		return ret;
 	}
 	
 	@Override
 	public String toString() {
 		String out = "";
 		if(attributes[1]) { // red
 			out = "r";
 		} else { // blue
 			out = "b";
 		}
 		if (attributes[0]) { // big
 			out = out.toUpperCase();
 		}
 		if (!attributes[3]) { // hole
 			out += "*";
 		}
 		if (!attributes[2]) { // circle
 			out = "(" + out + ")";
 		}
 		if (out.length() == 3){
 			out = " " + out;
 		}else if (out.length() == 2){
 			out = " " + out + " ";
 		} else if (out.length() == 1) {
 
 			out = "  " + out + " ";
 		}
 		return out;
 	}
 
 	public boolean[] getAttributes(){
 		return attributes;
 	}
 
 	public static Piece stringToPeace(String input) {
		//TODO: discuss if the line below is necessary
		//if(input == null){ throw new RuntimeException("Received null piece.");}
		if(input == null) return null;
 		String s = input.replaceAll(" ","");
 		boolean square = true;
 		boolean solid = true;
 		boolean big = true;
 		boolean red = true;
 		if (s.startsWith("(") && s.endsWith(")")) {
 			square = false;
 			s = s.substring(1, s.length() - 1);
 		}
 		if (s.endsWith("*")) {
 			solid = false;
 			s = s.substring(0, s.length() - 1);
 		}
 		if (s.toLowerCase().equals("r") || s.toLowerCase().equals("b")) {
 			if (Character.isLowerCase(s.charAt(0))) {
 				big = false;
 			}
 			if (s.toLowerCase().equals("b")) {
 				red = false;
 			}
 			return new Piece(big, red, square, solid);
 		}
 		return null;
 	}
 	
 	public static java.util.Set<Piece> getPieceSet(){
 		java.util.Set<Piece> ret = new HashSet<Piece>();
 		for (int i = 0; i < 16; i++) {
 			String binaryString = "000"+Integer.toBinaryString(i);
 			binaryString = binaryString.substring(binaryString.length()-4, binaryString.length());
 			ret.add(new Piece(binaryString.charAt(0) == '0', binaryString.charAt(1) == '0', binaryString.charAt(2) == '0', binaryString.charAt(3) == '0'));
 		}
 		return ret;
 	}
 }
