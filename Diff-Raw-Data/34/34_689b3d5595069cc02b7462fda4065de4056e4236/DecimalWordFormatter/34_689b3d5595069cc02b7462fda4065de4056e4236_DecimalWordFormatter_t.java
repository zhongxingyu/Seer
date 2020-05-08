 package ch.hszt.kfh.compost.ui.formatting;
 
 import ch.hszt.kfh.compost.Tools;
 
 public class DecimalWordFormatter implements DataFormatter {
 
 	@Override
 	public String format(boolean[] msb, boolean[] lsb) {
		boolean[] b = new boolean[msb.length + lsb.length];
		for (int i = 0; i < msb.length; i++) {
			b[i] = msb[i];
		}
		for (int i = 0; i < lsb.length; i++) {
			b[i + msb.length] = lsb[i];
		}
		return (Tools.fromComplement(b)) + "";
 	}
 
 	@Override
 	public String format(boolean[] word) {
 		return Tools.fromComplement(word) + "";
 	}
 
 	@Override
 	public String getName() {
 		return "Decimal (Word)";
 	}
 	
 	@Override
 	public String toString() {
 		return getName();
 	}
 	
 
 }
