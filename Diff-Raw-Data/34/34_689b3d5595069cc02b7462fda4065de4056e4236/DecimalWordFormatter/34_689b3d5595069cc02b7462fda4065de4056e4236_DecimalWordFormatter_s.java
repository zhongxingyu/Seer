 package ch.hszt.kfh.compost.ui.formatting;
 
 import ch.hszt.kfh.compost.Tools;
 
 public class DecimalWordFormatter implements DataFormatter {
 
 	@Override
 	public String format(boolean[] msb, boolean[] lsb) {
		return (Tools.fromComplement(msb) * 256 + Tools.fromComplement(lsb)) + "";
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
