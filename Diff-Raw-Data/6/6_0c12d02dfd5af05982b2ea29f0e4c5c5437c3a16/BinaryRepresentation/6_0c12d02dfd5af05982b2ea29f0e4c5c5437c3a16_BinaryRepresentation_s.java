 package edu.iastate.se339.text;
 
 public class BinaryRepresentation extends AbstractRepresentation {
 	
 	public BinaryRepresentation(byte[] rawBytes){
 		this.rawBytes = rawBytes;
 		bitsPerChar = 1;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		for(byte b : rawBytes){
			byte mask = (byte) 0xFF;
 			for(int i=7; i>=0; i--){
				mask = (byte) (mask >> 1);
				sb.append(Integer.toString((b & mask) >> i));
 			}
 		}
 		return sb.toString();
 	}
 
 }
