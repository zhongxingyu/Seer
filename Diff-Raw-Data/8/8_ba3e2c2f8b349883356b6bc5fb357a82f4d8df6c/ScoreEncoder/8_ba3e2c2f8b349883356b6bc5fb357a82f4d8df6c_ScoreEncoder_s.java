 package eval;
 
 
 
 
 /** encoder for score, margin, and any flags*/
 public final class ScoreEncoder {
 	public final static int scoreBits = 18;
 	public final static int marginBits = 11;
 	public final static int flagBits = 3;
 	
 	private final static int scoreMask;
 	private final static int scoreSignMask;
 	
 	private final static int marginMask;
 	private final static int marginSignMask;
 	
 	private final static int flagMask;
 	private final static int scoreMaskOffset;
 	private final static int marginMaskOffset;
 	private final static int flagMaskOffset;
 	
 	static{
 		scoreMaskOffset = 0;
 		marginMaskOffset = scoreBits;
 		flagMaskOffset = scoreBits + marginBits;
 		
 		{
 			int temp = 0;
 			for(int a = 0; a < scoreBits-1; a++) temp |= 1 << (scoreMaskOffset + a);
 			scoreMask = temp;
 			scoreSignMask = 1 << scoreBits-1;
 		}
 		{
 			int temp = 0;
 			for(int a = 0; a < marginBits-1; a++) temp |= 1 << a;
 			marginMask = temp;
 			marginSignMask = 1 << marginBits-1;
 		}
 		{
 			int temp = 0;
 			for(int a = 0; a < flagBits; a++) temp |= 1 << (flagMaskOffset + a);
 			flagMask = temp;
 		}
 	}
 	
 	public static int getScore(final int scoreEncoding){
 		return (scoreEncoding & scoreMask) - (scoreEncoding & scoreSignMask);
 	}
 	
 	public static int getMargin(final int scoreEncoding){
 		final int margin = scoreEncoding >>> marginMaskOffset;
 		//System.out.println(margin & marginMask);
 		//System.out.println(marginSignMask & margin);
 		return (margin & marginMask) - (marginSignMask & margin);
 	}
 	
 	public static int getFlags(final int scoreEncoding){
 		return (scoreEncoding & flagMask) >>> flagMaskOffset;
 	}
 	
 	public static int encode(final int score, final int margin, final int flags){
 		assert score < 1<<scoreBits;
 		assert margin < 1<<marginBits;
 		assert flags < 1<<flagBits;
 		assert flags >= 0;
 		
 		final int marginNegative = (margin >>> 31) << (marginBits-1);
 		final int marginValue =  (margin >>> 31)*(marginNegative-Math.abs(margin)) + (1-(margin >>> 31))*margin;
 		//System.out.println("margin value = "+marginValue);
 		//System.out.println("margin sign = "+marginNegative);
 		
		return (score & (scoreMask | scoreSignMask)) |
 				((marginValue|marginNegative) << marginMaskOffset) |
 				(flags << flagMaskOffset);
 	}
 	
 	public static void main(String[] args){
 		int a = -5;
 		System.out.println(a >>> 31);
 	}
 }
