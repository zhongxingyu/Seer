 package com.blackboard;
 
 public class HorizontalLine implements Line {
 	
 	private int length;
 	private int segments = 1;
 	
 	public void setLength(int length) {
 		this.length = length;
 	}
 	
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		int splitBoundry = length / segments;
 		int extras = length % segments;
 		int segmentsPrinted = 0;
 		for (int i = 0; i < length; i++) {
 			if ((i + 1) % splitBoundry == 0) {
 				// If we reach the end of the "normal", non-leftover segments then 
 				// append a character without a trailing space and break out of the loop
 				// so that we can print the leftovers at the end
 				if (segments == segmentsPrinted + 1) {
 					sb.append("=");
 					break;
 				}
				// If we reach the end of the string then don't print a trailing space
 				if (i + 1 == length) { 
 					sb.append("="); // Avoid spurious whitespace
 					segmentsPrinted += 1;
 				} else {
 					sb.append("= "); //Add space char for split
 					segmentsPrinted += 1;
 				}
 			} else {
 				// Only print if we have not yet reached the end of
				// "normal" non-leftover segments
 				if (segments != segmentsPrinted) {
 					sb.append("=");
 				}
 			}
 		}
 		// If we have leftovers then pop them on the end
 		// I decided to do this instead of redistribute among the pieces
 		// as that re-arranges things and, if used on a non-uniform string, would
 		// corrupt it.
 		if (extras > 0) {
 			for(int curExtra = 0; curExtra < extras; curExtra++) {
 				sb.append("=");
 			}
 		}
 		return sb.toString();
 	}
 	
 	public void split(int segments) {
 		if (segments > length) {
 			throw new IllegalArgumentException("Cannot split a string in more pieces than its constituent parts");
 		}
 		if (length == 0) {
 			throw new IllegalArgumentException("Can't split zero length string");
 		}
 		if (segments == 0) {
 			throw new IllegalArgumentException("Can't split a string into zero pieces");
 		}
 		this.segments = segments;
 	}
 }
