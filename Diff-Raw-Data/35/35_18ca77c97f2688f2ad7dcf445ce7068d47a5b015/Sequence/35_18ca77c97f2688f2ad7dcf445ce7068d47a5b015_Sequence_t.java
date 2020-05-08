 /**
  * A Java Long disguised with a fancier name
  * and some useful methods to represent a
  * DNA sequence of length between 1 - 31.
  * @author Jacob Biggs
  *
  */
 public class Sequence implements Comparable<Sequence> {
	private Long seq = 0L;
 	private int length;
 	
 	/**
 	 * Creates a new Sequence with the given
 	 * Long
 	 * @param seq
 	 */
 	public Sequence(Long seq, int sequenceLength){
 		this.seq = seq;
 		this.length = sequenceLength;
 	}
 	
 	
 	public Sequence(String str, int sequenceLength){
 		if (str.length() > 31 || str.length() < 1){
 			throw new IllegalArgumentException("Input String length was 0 or >31:\n" + str);
 		} else if (sequenceLength > 31 || sequenceLength < 1) {
 			throw new IllegalArgumentException("sequenceLength must be between 1 and 31 (inclusive)");
 		} else if (sequenceLength != str.length()) {
 			throw new IllegalArgumentException("Input String length ("+str.length()+") was different from sequenceLength ("+sequenceLength+")");
 		}
 		
 		char c;
		int strlen = str.length();
 		
		for (int i = 0, p = strlen - 1; i < strlen; i++, p--) {
 			
			System.out.println("i:" + i + " p:" + p);
 			c = str.charAt(p);
 			switch (c) {
 			
 				case ' ':
 					i--;
					strlen--;
 					break;
 					
 				case 'A':
 				case 'a':
 					break;
 					
 				case 'T':
 				case 't':
 					seq += (long) 3 * (long) Math.pow(2, i*2);
 					break;
 					
 				case 'C':
 				case 'c':
 					seq += (long) 1 * (long) Math.pow(2, i*2);
 					break;
 					
 				case 'G':
 				case 'g':
 					seq += (long) 2 * (long) Math.pow(2, i*2);
 					break;
 			}
 		}
 	}
 
 	public Long val(){
 		return this.seq;
 	}
 
 	@Override
 	public int compareTo(Sequence that) {
 		if (this.val() > that.val()) {
 			return 1;
 		} else if(this.val() == that.val()) {
 			return 0;
 		}
 		return -1;
 	}
 }
