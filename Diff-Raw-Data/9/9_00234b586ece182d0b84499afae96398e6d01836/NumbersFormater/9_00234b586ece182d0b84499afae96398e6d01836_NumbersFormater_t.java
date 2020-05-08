 package org.pescuma.buildhealth.analyser;
 
 import com.google.common.base.Strings;
 
 public class NumbersFormater {
 	
 	public static String formatBytes(double total) {
 		// http://en.wikipedia.org/wiki/Kilobyte
 		final String[] units = new String[] { "Ki", "Mi", "Gi", "Ti", "Pi", "Ei", "Zi", "Yi" };
 		final int scale = 1024;
 		
 		return format(total, "B", units, scale);
 	}
 	
 	public static String format1000(double total, String baseUnit) {
 		// http://en.wikipedia.org/wiki/Kilobyte
		final String[] units = new String[] { "k", "M", "G", "T", "P", "E", "Z", "Yi" };
 		final int scale = 1000;
 		
 		return format(total, baseUnit, units, scale);
 	}
 	
 	private static String format(double total, String baseUnit, final String[] units, final int scale) {
 		String unit = null;
 		for (int i = 0; i < units.length && total >= scale; i++) {
 			total /= scale;
 			unit = units[i];
 		}
 		
 		if (unit == null && Strings.isNullOrEmpty(baseUnit))
 			return String.format("%.0f", total);
 		else if (unit == null)
 			return String.format("%.0f %s", total, baseUnit);
 		else
 			return String.format("%.1f %s%s", total, unit, baseUnit);
 	}
 }
