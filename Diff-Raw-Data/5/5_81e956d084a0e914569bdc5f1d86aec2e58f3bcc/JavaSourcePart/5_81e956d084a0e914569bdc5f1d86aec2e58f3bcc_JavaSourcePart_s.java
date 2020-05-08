 package org.oobium.build.esp.parts;
 
 import org.oobium.build.esp.EspPart;
 
 public class JavaSourcePart extends EspPart {
 
 	private boolean simple;
 	
 	public JavaSourcePart(EspPart parent, Type type, int start, int end) {
 		super(parent, type, start, end, true);
 		parse();
 	}
 
 	public boolean isSimple() {
 		return simple;
 	}
 
 	private int parseString(int start) {
 		int s1 = start;
 		while(s1 < end) {
 			switch(ca[s1]) {
 			case '"':
				if(ca[s1-1] != '/') {
 					return s1 + 1;
 				}
 				break;
 			case '$':
				if(ca[s1-1] != '/') {
 					EspPart part = new JavaSourceStringPart(this, s1, end);
 					s1 = part.getEnd() - 1;
 				}
 				break;
 			}
 			s1++;
 		}
 		return s1;
 	}
 	
 	private void parse() {
 		int count = 0;
 		int s1 = start;
 		while(s1 < end) {
 			if(ca[s1] == '"') {
 				s1 = parseString(s1+1);
 				count++;
 			} else {
 				s1++;
 			}
 		}
 		
 		simple = (count == 1 && ca[start] == '"' && ca[end-1] == '"' && !hasParts());
 	}
 	
 }
