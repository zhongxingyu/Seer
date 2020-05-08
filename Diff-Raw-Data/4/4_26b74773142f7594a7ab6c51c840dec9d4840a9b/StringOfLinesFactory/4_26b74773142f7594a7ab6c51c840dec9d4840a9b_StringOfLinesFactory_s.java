 package ch.unibe.scg.cc.lines;
 
 import ch.unibe.scg.cc.javaFrontend.JavaTokenizer;
 
 public class StringOfLinesFactory {
 	public StringOfLines make(String string, char separator) {
 		String stringEndingInNewline = string;
 		if (!string.endsWith(String.valueOf(separator))) {
			stringEndingInNewline = string + "\n";
 		}
 		return makeSanitized(stringEndingInNewline, separator);
 	}
 
 	StringOfLines makeSanitized(String string, char separator) {
 		int count = JavaTokenizer.countOccurrences(string, separator);
 		int[] separators = new int[count + 1];
 		separators[0] = 0;
 		int thisIndex = string.indexOf(separator);
 		int i;
 		for (i = 1; thisIndex != -1; thisIndex = string.indexOf(separator, thisIndex + 1)) {
 			separators[i] = thisIndex;
 			i++;
 		}
 		assert i == separators.length : "" + (separators.length) + " : " + i;
 		return new StringOfLines(string, separators);
 
 	}
 
 	public StringOfLines make(String string) {
 		return this.make(string, '\n');
 	}

 }
