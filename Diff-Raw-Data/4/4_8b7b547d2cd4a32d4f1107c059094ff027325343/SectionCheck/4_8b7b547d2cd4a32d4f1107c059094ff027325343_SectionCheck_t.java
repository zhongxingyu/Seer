 package com.htmlthor;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 import org.json.simple.JSONObject;
 
 
 public class SectionCheck {
 	
 	private List<String> filesInZip = null;
 	private String filePath = null;
 	int brokenLinks;
 	int colOffset;
 	List<String> source = null;
 	int errorsInLine = 0;
 	
 	/* Just an empty constructor */
 	public SectionCheck() {
 		
 	}
 
 		/* This is the coding for the NEW error checking */
 		public JSONObject findErrors(List<String> fileContents) {
 		
 			source = fileContents;
 		
 			Mysqlfunctions sql = new Mysqlfunctions();
 		
 			colOffset = 0;
 		
 			JSONObject errors = new JSONObject();
 			boolean openTag = false;
 			boolean closeTag =  false;
 			boolean startComment = false;
 			boolean startPhp = false;
 			boolean whiteSpaceFlag = false;
 			boolean selfClosing = false;
 			int tagStart = 0;
 			String tag = "lol";
 			int errorCount = 0;
 			boolean endTagName = false;
 			boolean faultyTag = false;
 			boolean tagChecked = false;
 			boolean selfClosingError = false;
 			boolean openDoctype = false;
 			
 			boolean isClosingTag = false;
 			
 			List<String> singularTags = new ArrayList<String>();
 			List<String> ids = new ArrayList<String>();
 			
 			// variables used for escaping script/style tag content
 			boolean openScript = false;
 			boolean openStyle = false;
 			boolean openSvg = false;
 			boolean openMath = false;
 			boolean openCode = false;
 			int endEscapedTagPhase = 0; // used to find </script> and </style>
 			// 0: < not found
 			// 1: < found, / not found - whitespace permitted
 			// 2: / found, script/style not found - whitespace permitted
 			// 3: s/m/c found - whitespace not permitted
 			// 4: c/t/a/v/o found
 			// 5: r/y/g/t/d found
 			// 6: i/l/h/e found
 			// 7: p/e found
 			// 8: t found - looking for >
 			
 			// variables used for checking attributes and values
 			int attrStart = 0;
 			int attrValStart = 0;
 			boolean openAttr = false;
 			int attrPhase = 0;
 			String attribute = "lel";
 			// 1: attribute key name started - ends at whitespace or =
 			// 2: attribute key name finished, looking for =
 			// 3: = found - looking for quotes to start value - whitespace permitted
 			// 4: " found - ignoring everything until matching " found
 			// 5: ' found - ignoring everything until matching ' found
 			// 6: value not enclosed in quotes - add error when end of attribute value found
 			
 			/* START OF AMEER'S CODE */
 			/* Instantiation of Encapsulation class. */
 			Encapsulation encap = new Encapsulation();
 			/* END OF AMEER'S CODE */
 			
 			int endTagColumnNo = 0;
 			int endAttrColumnNo = 0;
 			
 			brokenLinks = 0;
 			
 			JSONObject error;
 			List<String> attributeList = new ArrayList<String>();
 			List<String> requiredTags = new ArrayList<String>();
 			String prevTag = "";
 			
 			long timeoutStart = System.currentTimeMillis();
 			long timeoutEnd = timeoutStart+30000;
 			
 			
 			/* Iterates over the lines of the given file. */
 			for (int i=0; i<fileContents.size(); i++) {
 				String nextLine = fileContents.get(i);
 				colOffset = 0;
 				errorsInLine = 0;
 				/* Initialise the character array on the new line. */
 				char[] intermediate = nextLine.toCharArray();
 				CharArray charArray = new CharArray(nextLine.toCharArray());
				
				if (openTag == true) {
					whiteSpaceFlag = true;
				}
 			
 				//Check for open tags
 				for(int j=0; j<charArray.getLength(); j++) {
 				
 					if (System.currentTimeMillis() > timeoutEnd) {
 						error = errorConstructor("Your file reached the time limit of 30 seconds at line " + Integer.toString(i+1) + " and column " + Integer.toString(j), "syntax", i+1, j-1, "");
 						errors.put(errorCount, error);
 						errorCount += 1;
 						errors.put("count", errorCount);
 						return errors;
 					}
 				
 					if (charArray.getChar(j) == '<' || charArray.getChar(j) == '>') {
 						colOffset += 3;
 					} else if (charArray.getChar(j) == '"') {
 						colOffset += 5;
 					} else if (charArray.getChar(j) == '\'') {
 						colOffset += 4;
 					}
 					
 				
 				
 					// ==============================================
 					// check whether a style tag is open, in which case content will be unchecked
 					// until </style> is found
 					// ==============================================
 					if (openStyle) {
 						if (endEscapedTagPhase == 0) {
 							if (charArray.getChar(j) == '<') {
 								// look for next char
 								endEscapedTagPhase = 1;
 							}
 						} else if (endEscapedTagPhase == 1) {
 							if (charArray.getChar(j) == '/') {
 								// look for next char
 								tagStart = j;
 								endEscapedTagPhase = 2;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 2) {
 							if (charArray.getChar(j) == 's' || charArray.getChar(j) == 'S') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 't' || charArray.getChar(j) == 'T') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'y' || charArray.getChar(j) == 'Y') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'l' || charArray.getChar(j) == 'L') {
 								// look for next char
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == 'e' || charArray.getChar(j) == 'E') {
 								// look for next char
 								endEscapedTagPhase = 7;
 								endTagColumnNo = j;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 7) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								encap.encapsulation("/style", i+1, tagStart, endTagColumnNo);
 								openStyle = false;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						}
 						continue;
 					}
 					// ==============================================
 					// check whether a style tag is open, in which case content will be unchecked
 					// until </script> is found
 					// ==============================================
 					if (openScript) {
 						if (endEscapedTagPhase == 0) {
 							if (charArray.getChar(j) == '<') {
 								// look for next char
 								endEscapedTagPhase = 1;
 							}
 						} else if (endEscapedTagPhase == 1) {
 							if (charArray.getChar(j) == '/') {
 								// look for next char
 								endEscapedTagPhase = 2;
 								tagStart = j;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 2) {
 							if (charArray.getChar(j) == 's' || charArray.getChar(j) == 'S') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'c' || charArray.getChar(j) == 'C') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'r' || charArray.getChar(j) == 'R') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'i' || charArray.getChar(j) == 'I') {
 								// look for next char
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == 'p' || charArray.getChar(j) == 'P') {
 								// look for next char
 								endEscapedTagPhase = 7;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 7) {
 							if (charArray.getChar(j) == 't' || charArray.getChar(j) == 'T') {
 								// look for next char
 								endEscapedTagPhase = 8;
 								endTagColumnNo = j;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 8) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								encap.encapsulation("/script", i+1, tagStart, endTagColumnNo);
 								openScript = false;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						}
 						continue;
 					}
 					
 					/* START OF AMEER'S CODE */
 					if (openSvg) {
 						if (endEscapedTagPhase == 0) {
 							if (charArray.getChar(j) == '<') {
 								// look for next char
 								endEscapedTagPhase = 1;
 							}
 						} else if (endEscapedTagPhase == 1) {
 							if (charArray.getChar(j) == '/') {
 								// look for next char
 								endEscapedTagPhase = 2;
 								tagStart = j;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 2) {
 							if (charArray.getChar(j) == 's' || charArray.getChar(j) == 'S') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'v' || charArray.getChar(j) == 'V') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'g' || charArray.getChar(j) == 'G') {
 								// look for next char
 								endTagColumnNo = j;
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								encap.encapsulation("/svg", i+1, tagStart, endTagColumnNo);
 								openSvg = false;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						}
 						continue;
 					}
 					
 					if (openMath) {
 						if (endEscapedTagPhase == 0) {
 							if (charArray.getChar(j) == '<') {
 								// look for next char
 								endEscapedTagPhase = 1;
 							}
 						} else if (endEscapedTagPhase == 1) {
 							if (charArray.getChar(j) == '/') {
 								// look for next char
 								tagStart = j;
 								endEscapedTagPhase = 2;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 2) {
 							if (charArray.getChar(j) == 'm' || charArray.getChar(j) == 'M') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'a' || charArray.getChar(j) == 'A') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 't' || charArray.getChar(j) == 'T') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'h' || charArray.getChar(j) == 'H') {
 								// look for next char
 								endTagColumnNo = j;
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								encap.encapsulation("/math", i+1, tagStart, endTagColumnNo);
 								openMath = false;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} 
 						continue;
 					}
 					
 					if (openCode) {
 						if (endEscapedTagPhase == 0) {
 							if (charArray.getChar(j) == '<') {
 								// look for next char
 								endEscapedTagPhase = 1;
 							}
 						} else if (endEscapedTagPhase == 1) {
 							if (charArray.getChar(j) == '/') {
 								// look for next char
 								tagStart = j;
 								endEscapedTagPhase = 2;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 2) {
 							if (charArray.getChar(j) == 'c' || charArray.getChar(j) == 'C') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'o' || charArray.getChar(j) == 'O') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'd' || charArray.getChar(j) == 'D') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'e' || charArray.getChar(j) == 'E') {
 								// look for next char
 								endTagColumnNo = j;
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								openCode = false;
 								encap.encapsulation("/code", i+1, tagStart, endTagColumnNo);
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						}
 						continue;
 					}
 					/* END OF AMEER'S CODE */
 					
 					// ==============================================
 					// Script/style checking done
 					// ==============================================
 				
 				
 					// ==============================================
 					// Doctype fix start
 					// ==============================================
 				
 					if (openDoctype) {
 						if (attrPhase == 1) {
 							if (charArray.getChar(j) == ' ') {
 								continue;
 							}
 							if (charArray.getChar(j) == 'h' || charArray.getChar(j) == 'H') {
 								attrPhase = 2;
 								continue;
 							} else {
 								error = errorConstructor(sql.getErrMsg(1), "syntax", i+1, endTagColumnNo, tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 5;
 								if (charArray.getChar(j) == '>') {
 									openDoctype = false;
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}
 								continue;
 							}
 						}
 						if (attrPhase == 2) {
 							if (charArray.getChar(j) == 't' || charArray.getChar(j) == 'T') {
 								attrPhase = 3;
 								continue;
 							} else {
 								error = errorConstructor(sql.getErrMsg(1), "syntax", i+1, endTagColumnNo, tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 5;
 								if (charArray.getChar(j) == '>') {
 									openDoctype = false;
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}
 								continue;
 							}
 						}
 						if (attrPhase == 3) {
 							if (charArray.getChar(j) == 'm' || charArray.getChar(j) == 'M') {
 								attrPhase = 4;
 								continue;
 							} else {
 								error = errorConstructor(sql.getErrMsg(1), "syntax", i+1, endTagColumnNo, tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 5;
 								if (charArray.getChar(j) == '>') {
 									openDoctype = false;
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}
 								continue;
 							}
 						}
 						if (attrPhase == 4) {
 							if (charArray.getChar(j) == 'l' || charArray.getChar(j) == 'L') {
 								attrPhase = 5;
 								continue;
 							} else {
 								error = errorConstructor(sql.getErrMsg(1), "syntax", i+1, endTagColumnNo, tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 5;
 								if (charArray.getChar(j) == '>') {
 									openDoctype = false;
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}
 								continue;
 							}
 						}
 						if (attrPhase == 5) {
 							if (charArray.getChar(j) == '>') {
 								openDoctype = false;
 								closeTag = true;
 								openTag = false;
 								tag = null;
 								endTagName = false;
 								tagChecked = false;
 								faultyTag = false;
 							} else if (charArray.getChar(j) != ' ') {
 								error = errorConstructor(sql.getErrMsg(1), "syntax", i+1, endTagColumnNo, tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 6;
 							}
 							continue;
 						}
 						if (attrPhase == 6) {
 							if (charArray.getChar(j) == '>') {
 								
 								openDoctype = false;
 								closeTag = true;
 								openTag = false;
 								tag = null;
 								endTagName = false;
 								tagChecked = false;
 								faultyTag = false;
 							}
 							continue;
 						}
 					
 					
 					
 					}
 				
 					// ==============================================
 					// Doctype fix end
 					// ==============================================
 				
 					// ==============================================
 					// Attribute checking start
 					// ==============================================
 					if (openAttr) {
 						if (attrPhase == 1) {
 							// looking for end of attribute key
 							if (charArray.getChar(j) == ' ') {
 								// attribute key has ended
 								attribute = charArray.getString(attrStart, j-1);
 								endAttrColumnNo = j-1;
 								if(attributeList.contains(attribute.toLowerCase())) {
 									// Duplicate attribute use for this tag
 									error = errorConstructor(attribute + sql.getErrMsg(44), "syntax", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								else {
 									attributeList.add(attribute.toLowerCase());
 								}
 								
 								if (tag.equalsIgnoreCase("meta") && attribute.equalsIgnoreCase("charset")) {
 									requiredTags.add(tag.toLowerCase());
 								}
 								
 								List<String> attrList = new ArrayList<String>();
 								attrList = sql.getAttr(tag);
 								boolean validAttr = false;
 								for (int a = 0; a < attrList.size(); a++) {
 									if(attrList.get(a).equalsIgnoreCase(attribute)) {
 										validAttr = true;	
 									}
 								}
 								
 								if (tag.equalsIgnoreCase("meta") && attribute.equalsIgnoreCase("charset")) {
 									requiredTags.add(tag.toLowerCase());
 								}
 								
 								if (!validAttr) {
 									if (attribute.length() > 4) {
 										if (attribute.substring(0,5).equalsIgnoreCase("data-")) {
 											validAttr = true;
 										}
 									}
 									if (!validAttr) {
 										error = errorConstructor(attribute + " " + sql.getErrMsg(23), "syntax", i+1, endAttrColumnNo, attribute);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 								} else if (sql.isDeprecatedAttribute(attribute, tag)) {
 									error = errorConstructor(sql.getErrMsg(30).replaceAll("--attr", attribute).replaceAll("--tag", tag), "deprecated", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								
 								attrPhase = 2;
 							} else if (charArray.getChar(j) == '>' || charArray.getChar(j) == '/') {
 								// did not find value for attribute
 								attribute = charArray.getString(attrStart, j-1);
 								endAttrColumnNo = j-1;
 								if(!sql.isAttrBool(attribute)) {
 									// did not find a value for the key
 									error = errorConstructor(sql.getErrMsg(43).replaceAll("-attr", attribute), "syntax", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								
 								if(attributeList.contains(attribute.toLowerCase())) {
 									// Duplicate attribute use for this tag
 									error = errorConstructor(attribute + sql.getErrMsg(44), "syntax", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								} else {
 									attributeList.add(attribute.toLowerCase());
 								}
 								
 								List<String> attrList = new ArrayList<String>();
 								attrList = sql.getAttr(tag);
 								boolean validAttr = false;
 								for (int a = 0; a < attrList.size(); a++) {
 									if(attrList.get(a).equalsIgnoreCase(attribute)) {
 										validAttr = true;	
 									}
 								}
 								if (!validAttr) {
 									if (attribute.length() > 4) {
 										if (attribute.substring(0,5).equalsIgnoreCase("data-")) {
 											validAttr = true;
 										}
 									}
 									if (!validAttr) {
 										error = errorConstructor(attribute + " " + sql.getErrMsg(23), "syntax", i+1, endAttrColumnNo, attribute);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 								} else if (sql.isDeprecatedAttribute(attribute, tag)) {
 									error = errorConstructor(sql.getErrMsg(30).replaceAll("--attr", attribute).replaceAll("--tag", tag), "deprecated", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								
 								j--;
 								
 								attrPhase = 0;
 								openAttr = false;
 								attribute = "";
 								continue;
 								} else if (charArray.getChar(j) == '=') {
 								// attribute key has ended
 								attribute = charArray.getString(attrStart, j-1);
 								endAttrColumnNo = j-1;
 								if(attributeList.contains(attribute.toLowerCase())) {
 									// Duplicate attribute use for this tag
 									error = errorConstructor(attribute + sql.getErrMsg(44), "syntax", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								} else {
 									attributeList.add(attribute.toLowerCase());
 								}
 								
 								if (tag.equalsIgnoreCase("meta") && attribute.equalsIgnoreCase("charset")) {
 									requiredTags.add(tag.toLowerCase());
 								}
 								
 								List<String> attrList = new ArrayList<String>();
 								attrList = sql.getAttr(tag);
 								boolean validAttr = false;
 								for (int a = 0; a < attrList.size(); a++) {
 									if(attrList.get(a).equalsIgnoreCase(attribute)) {
 										validAttr = true;	
 									}
 								}
 								if (!validAttr) {
 									if (attribute.length() > 4) {
 										if (attribute.substring(0,5).equalsIgnoreCase("data-")) {
 											validAttr = true;
 										}
 									}
 									if (!validAttr) {
 										error = errorConstructor(attribute + " " + sql.getErrMsg(23), "syntax", i+1, endAttrColumnNo, attribute);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 								} else if (sql.isDeprecatedAttribute(attribute, tag)) {
 									error = errorConstructor(sql.getErrMsg(30).replaceAll("--attr", attribute).replaceAll("--tag", tag), "deprecated", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								
 								attrPhase = 3;
 							}
 							continue;
 						} else if (attrPhase == 2) {
 							// looking for the end of whitespace before the =
 							if (charArray.getChar(j) == ' ') {
 								continue;
 							} else if (charArray.getChar(j) == '=') {
 								attrPhase = 3;
 								continue;
 							}else {
 								
 								if(!sql.isAttrBool(attribute)) {
 									// did not find a value for the key
 									error = errorConstructor(sql.getErrMsg(43).replaceAll("-attr", attribute), "syntax", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								attrPhase = 0;
 								openAttr = false;
 								attribute = "";
 							}
 						} else if (attrPhase == 3) {
 							// looking for quotes to start 
 							if (charArray.getChar(j) == ' ') {
 							// do nothing
 							} else if (charArray.getChar(j) == '"') {
 								attrPhase = 4;
 								attrValStart = j;
 							} else if (charArray.getChar(j) == '\'') {
 								attrPhase = 5;
 								attrValStart = j;
 							} else if (charArray.getChar(j) == '#' && attribute.equalsIgnoreCase("href")) {
 								if (charArray.getChar(j+1) == ' ' || charArray.getChar(j+1) == '>' || charArray.getChar(j+1) == '/') {
 									// unquoted # for href
 									error = errorConstructor(sql.getErrMsg(42), "warning", i+1, j, "#");
 									errors.put(errorCount, error);
 									errorCount += 1;
 									
 									attrPhase = 0;
 									openAttr = false;
 									attribute = "";
 								}
 								continue;
 							} else {
 								// value not enclosed in quotes
 								attrPhase = 6;
 								attrValStart = j;
 							}
 							continue;
 						} else if (attrPhase == 4) {
 							// looking for end of double quotes
 							if (charArray.getChar(j) == '"') {
 							
 								if(sql.isAttrBool(attribute)) {
 									// this type of attribute cannot have a value
 									error = errorConstructor(sql.getErrMsg(40).replaceAll("--attr",attribute), "syntax", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 							
 							
 								// check for broken link
 								if (attribute.equalsIgnoreCase("src") || attribute.equalsIgnoreCase("href")) {
 									String attributeVal = charArray.getString(attrValStart+1, j-1);
 									String wrongLoc = checkPathExists(attributeVal);
 									if (wrongLoc != null) {
 										error = errorConstructor(sql.getErrMsg(40).replaceAll("--fp",wrongLoc), "broken", i+1, j-1, attributeVal);
 										errors.put(errorCount, error);
 										errorCount += 1; 
 										brokenLinks += 1;
 									}
 								}
 							
 								// check for unique id
 								if (attribute.equalsIgnoreCase("id")) {
 									String attributeVal = charArray.getString(attrValStart+1, j-1);
 									boolean matchedID = false;
 									for (int a = 0; a < ids.size(); a++) {
 										if(ids.get(a).equalsIgnoreCase(attributeVal)) {
 											matchedID = true;
 										}
 							
 									}
 									if (matchedID) {
 										// error for duplicate id
 										error = errorConstructor(attributeVal + sql.getErrMsg(39), "semantic", i+1, j-1, attributeVal);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									} else {
 										ids.add(attributeVal);
 									}
 								}
 							
 								// reset attribute flags
 								attrPhase = 0;
 								openAttr = false;
 								attribute = "";
 							}
 							continue;
 						} else if (attrPhase == 5) {
 							// looking for end of single quotes
 							if (charArray.getChar(j) == '\'') {
 								
 								if(sql.isAttrBool(attribute)) {
 									// this type of attribute cannot have a value
 									error = errorConstructor(sql.getErrMsg(40).replaceAll("--attr",attribute), "syntax", i+1, endAttrColumnNo, attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								
 								// check for broken link
 								if (attribute.equalsIgnoreCase("src") || attribute.equalsIgnoreCase("href")) {
 									String attributeVal = charArray.getString(attrValStart+1, j-1);
 									String wrongLoc = checkPathExists(attributeVal);
 									if (wrongLoc != null) {
 										error = errorConstructor(sql.getErrMsg(40).replaceAll("--fp",wrongLoc), "broken", i+1, j-1, attributeVal);
 										errors.put(errorCount, error);
 										errorCount += 1;
 										brokenLinks += 1;
 									}
 								}
 								
 								// check for unique id
 								if (attribute.equalsIgnoreCase("id")) {
 									String attributeVal = charArray.getString(attrValStart+1, j-1);
 									boolean matchedID = false;
 									for (int a = 0; a < ids.size(); a++) {
 										if(ids.get(a).equalsIgnoreCase(attributeVal)) {
 											matchedID = true;
 										}
 							
 									}
 									if (matchedID) {
 										// error for duplicate id
 										error = errorConstructor(attributeVal + sql.getErrMsg(39), "semantic", i+1, j-1, attributeVal);
 										error = new JSONObject();
 										errors.put(errorCount, error);
 										errorCount += 1;
 									} else {
 										ids.add(attributeVal);
 									}
 								}
 							
 								// reset attribute flags
 								attrPhase = 0;
 								openAttr = false;
 								attribute = "";
 							}
 							continue;
 						} else if (attrPhase == 6) {
 							// looking for end of attribute
 							if (charArray.getChar(j) == ' ' || charArray.getChar(j) == '/' || charArray.getChar(j) == '>') {
 								String attributeVal = charArray.getString(attrValStart, j-1);
 								error = errorConstructor(sql.getErrMsg(38).replaceAll("--attval", attributeVal).replaceAll("--att", attribute), "semantic", i+1, j-1, attributeVal);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								// reached end of attribute value
 								attrPhase = 0;
 								openAttr = false;
 								attribute = "";
 							}
 							continue;
 						}
 						
 					}
 				
 				
 				
 				
 					if(charArray.getChar(j)=='<') {
 						
 						openTag = true;
 						while (charArray.getChar(j+1) == ' ') {
 							j = j+1;
 						}
 						tagStart = j+1;
 						// Check if opened a comment tag
 						if (charArray.getLength() >= j+4) {
 							if((charArray.getChar(j+1)=='!') && (charArray.getChar(j+2)=='-') && (charArray.getChar(j+3)=='-')) {
 								startComment = true;
 								openTag = false;
 							}
 						}
 						
 						// Check if opened a php tag
 						if (charArray.getLength() >= j+5) {
 							if((charArray.getChar(j+1)=='?') && (charArray.getChar(j+2)=='p' || charArray.getChar(j+2)=='P') && (charArray.getChar(j+3)=='h' || charArray.getChar(j+3)=='H') && (charArray.getChar(j+4)=='p' || charArray.getChar(j+4)=='P')) {
 								startPhp = true;
 							}
 						}
 						
 						j = j+1;
 						
 						
 						
 								
 						
 					}
 			
 			
 								
 					
 			
 			
 					// As long as a comment tag is not open, another tag is open and 
 					// whitespace has not been reached to signal the end of the tag name:
 					if ((startComment==false) && (openTag==true) && (startPhp==false)) {
 						if(whiteSpaceFlag==false) {
 							if((charArray.getChar(j)==' ')||(charArray.getChar(j)=='>'|| charArray.getChar(j)=='\t')) {
 								if((charArray.getChar(j)==' ' || charArray.getChar(j)=='\t') && endTagName) {
 									whiteSpaceFlag = true;
 								}
 								if (openAttr == true) {
 									openAttr = false;
 									// check attribute stuff here
 								}
 								if (!endTagName && !charArray.getString(tagStart, j-1).trim().equals("/")) {
 									tag = charArray.getString(tagStart, j-1);
 									tag = tag.replaceAll(" ", "");
 									endTagColumnNo = j-1;
 									
 									// Error messages for values that are not deprecated, but best practice asks they not be used
 									if(tag.equalsIgnoreCase("s")) {
 										error = errorConstructor(sql.getErrMsg(24), "warning", i+1, endTagColumnNo, tag);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 									if(tag.equalsIgnoreCase("i")) {
 										error = errorConstructor(sql.getErrMsg(25), "warning", i+1, endTagColumnNo, tag);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 									if(tag.equalsIgnoreCase("b")) {
 										error = errorConstructor(sql.getErrMsg(26), "warning", i+1, endTagColumnNo, tag);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 									if(tag.equalsIgnoreCase("u")) {
 										error = errorConstructor(sql.getErrMsg(27), "warning", i+1, endTagColumnNo, tag);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 									
 									
 									attrPhase = 0;
 									openAttr = false;
 									
 									// Initiate required attributes list
 									attributeList = new ArrayList<String>();
 									
 									// Check if tag and tag before tag are br tag
 									if(prevTag.equalsIgnoreCase("br") && tag.equalsIgnoreCase("br")) {
 										error = errorConstructor(sql.getErrMsg(37), "semantic", i+1, endTagColumnNo, tag);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 									
 									
 									// Assign tag to prevTag
 									prevTag = tag;
 									
 									if(!singularTags.contains(tag.toLowerCase()) && !tag.equalsIgnoreCase("!doctype")) {
 										if (tag.length() > 0) {
 											encap.encapsulation(tag.toLowerCase(), i+1, tagStart, endTagColumnNo);
 										}
 									}
 									
 									if (tag.equalsIgnoreCase("!DOCTYPE")) {
 										openDoctype = true;
 										attrPhase = 1;
 										j--;
 									}
 									
 									
 									if(tag.equalsIgnoreCase("html")||tag.equalsIgnoreCase("head")||tag.equalsIgnoreCase("body")||tag.equalsIgnoreCase("!DOCTYPE")||
 										tag.equalsIgnoreCase("title")||tag.equalsIgnoreCase("meta")) {
 										
 										if(!tag.equalsIgnoreCase("meta")) {
 											if(singularTags.contains(tag.toLowerCase())) {
 												error = new JSONObject();
 												if (tag.equalsIgnoreCase("html")) {
 													error = errorConstructor(sql.getErrMsg(8), "semantic", i+1, endTagColumnNo, tag);
 												} else if (tag.equalsIgnoreCase("head")) {
 													error = errorConstructor(sql.getErrMsg(9), "semantic", i+1, endTagColumnNo, tag);
 												} else if (tag.equalsIgnoreCase("title")) {
 													error = errorConstructor(sql.getErrMsg(10), "semantic", i+1, endTagColumnNo, tag);
 												} else if (tag.equalsIgnoreCase("!doctype")) {
 													error = errorConstructor(sql.getErrMsg(7), "semantic", i+1, endTagColumnNo, tag);
 												} else if (tag.equalsIgnoreCase("body")) {
 													error = errorConstructor(sql.getErrMsg(16), "semantic", i+1, endTagColumnNo, tag);
 												}
 												errors.put(errorCount, error);
 												errorCount += 1;
 											}
 											else {
 												singularTags.add(tag.toLowerCase());
 											}
 											requiredTags.add(tag.toLowerCase());
 										}
 										
 										
 									}
 									
 									if (tag.length() > 0) {
 										if (tag.substring(0,1).equalsIgnoreCase("/")) {
 											isClosingTag = true;
 											tag = tag.substring(1);
 										}
 										else {
 											isClosingTag = false;
 										}
 									}
 									endTagName = true;
 									if (charArray.getChar(j)==' ' || charArray.getChar(j)=='\t') {
 										whiteSpaceFlag = true;
 									}
 									
 								}
 								
 								
 								
 								
 								
 								if (!tagChecked && endTagName) {
 								
 								
 								
 									// If it is not a valid tag
 									if(!sql.checkValidTag(tag)) {
 										
 										// Note that some of these additions should use database references in future
 										error = new JSONObject();
 										if (tag.equalsIgnoreCase("doctype")) {
 											error = errorConstructor(sql.getErrMsg(1), "syntax", i+1, endTagColumnNo, tag);
 										} else {
 											error = errorConstructor(sql.getErrMsg(18).replaceAll("--tag", tag), "syntax", i+1, endTagColumnNo, tag);
 										}
 										errors.put(errorCount, error);
 										errorCount += 1;
 										faultyTag = true;
 										
 									}	
 									// If it a deprecated tag
 									else if(!sql.isDeprecated(tag)) {
 										error = errorConstructor(sql.getErrMsg(29).replaceAll("--element", tag), "deprecated", i+1, endTagColumnNo, tag);
 										errors.put(errorCount, error);
 										errorCount += 1;
 										faultyTag = true;
 									}
 									tagChecked = true;
 								}
 								
 							
 								if(charArray.getChar(j)=='>') {
 								
 								attribute = "";
 								openAttr = false;
 								attrPhase = 0;
 								
 								if (!isClosingTag) {
 									List<String> requiredAttributes = new ArrayList<String>();
 									requiredAttributes = sql.requiresAttr(tag.toLowerCase());
 									Boolean erroredAttrAlready = false;
 									for(int z = 0; z < requiredAttributes.size(); z++) {
 										if(!attributeList.contains(requiredAttributes.get(z).toLowerCase())) {
 											if(requiredAttributes.get(z).equalsIgnoreCase("alt")) {
 												error = errorConstructor(sql.getErrMsg(36), "warning", i+1, endTagColumnNo, tag);
 												errors.put(errorCount, error);
 												errorCount += 1;
 												erroredAttrAlready = true;
 											}
 											if(requiredAttributes.get(z).equalsIgnoreCase("name")&&(tag.equalsIgnoreCase("input"))) {
 												error = errorConstructor(sql.getErrMsg(35), "warning", i+1, endTagColumnNo, tag);
 												errors.put(errorCount, error);
 												errorCount += 1;
 												erroredAttrAlready = true;
 											}
 											if(requiredAttributes.get(z).equalsIgnoreCase("value")&&(tag.equalsIgnoreCase("input"))) {
 												error = errorConstructor(sql.getErrMsg(34), "warning", i+1, endTagColumnNo, tag);
 												errors.put(errorCount, error);
 												errorCount += 1;
 												erroredAttrAlready = true;
  											}
 											if(!erroredAttrAlready) {
 												error = errorConstructor(sql.getErrMsg(21).replaceAll("--tag",tag).replaceAll("--attr", requiredAttributes.get(z)), "syntax", i+1, endTagColumnNo, tag);
 												errors.put(errorCount, error);
 												errorCount += 1;
 											}
 										}
 									}
 								}
 								
 								
 									// Check if self closing
 									selfClosing = sql.isSelfClosing(tag);
 								
 								
 									int closingChecker = j-1;
 									while (charArray.getChar(closingChecker) == ' ' && closingChecker > 0) {
 										
 										closingChecker = closingChecker-1;
 										
 									}
 								
 								
 									// Check if comment tag closed
 									
 									if (selfClosing) { 		
 										
 										if(charArray.getChar(closingChecker) != '/') {
 											
 											selfClosingError = true;
 											
 											if (selfClosingError) {
 												error = errorConstructor(sql.getErrMsg(19).replaceAll("--element_uc", tag), "warning", i+1, endTagColumnNo, tag);
 												errors.put(errorCount, error);
 												errorCount += 1;
 											}
 										} else {
 										
 											selfClosingError = false;
 											if (closingChecker > 0) {
 												if (charArray.getChar(closingChecker-1) != ' ') {
 													selfClosingError = true;
 												}
 											}
 											
 											if (selfClosingError) {
 												error = errorConstructor(sql.getErrMsg(33), "warning", i+1, closingChecker, "/");
 												errors.put(errorCount, error);
 												errorCount += 1;
 											}
 										
 										}
 										selfClosing = false;
 									} else if (!selfClosing) { 						
 										if(charArray.getChar(closingChecker) == '/') {
 											
 											selfClosingError = true;
 											
 											if (closingChecker > 0) {
 												if (charArray.getChar(closingChecker-1) != ' ') {
 													selfClosingError = false;
 												}
 											}
 											
 											if (selfClosingError) {									
 										
 												error = errorConstructor(sql.getErrMsg(31).replaceAll("--element_uc", tag), "semantic", i+1, closingChecker, "/");
 												errors.put(errorCount, error);
 												errorCount += 1;
 											}
 										}
 										selfClosing = false;
 									}
 									
 									if (tag.equalsIgnoreCase("script")) {
 										openScript = true;
 									}
 									
 									else if (tag.equalsIgnoreCase("style")) {
 										openStyle = true;
 									}
 									
 									else if(tag.equalsIgnoreCase("svg")) {
 										openSvg = true;
 									}
 									
 									else if(tag.equalsIgnoreCase("math")) {
 										openMath = true;
 									}
 									
 									else if(tag.equalsIgnoreCase("code")) {
 										openCode = true;
 									}
 							
 									/* Resets flag values and tag string */
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 									attrPhase = 0;
 									openAttr = false;
 								}	
 							}
 						
 						}
 						else {
 							if (j != 0) {
 									
 								if((charArray.getChar(j) != '>')) {
 									if ((charArray.getChar(j-1) == ' ') && (charArray.getChar(j) != ' ')) {
 										if( (Character.isLetter(charArray.getChar(j))) == true) {
 											attrStart = j;
 											openAttr = true;
 											attrPhase = 1;
 										}
 									}
 								} else {
 									j = j - 1;
 								}
 							}
 							whiteSpaceFlag = false;
 						}
 						
 						
 					}
 					if (startComment == true && j > 2) {
 						if((charArray.getChar(j-2)=='-') && (charArray.getChar(j-1)=='-') && (charArray.getChar(j)=='>')) {
 							closeTag = true;
 							openTag = false;
 							tag = null;
 							endTagName = false;
 							tagChecked = false;
 							faultyTag = false;
 							attrPhase = 0;
 							openAttr = false;
 							startComment = false;
 							continue;
 						}
 					}
 					if (startPhp == true && j > 1) {
 						if((charArray.getChar(j-1)=='?') && (charArray.getChar(j)=='>')) {
 							startPhp = false;
 						}
 					}
 					
 					// If an attribute has been reached and finished via the = operator
 					if(charArray.getChar(j) == '=') {
 						if(openAttr == true) {
 							String attr = charArray.getString(attrStart, j-1);
 							List<String> attrList = new ArrayList<String>();
 							attrList = sql.getAttr(tag);
 							boolean validAttr = false;
 							for (int a = 0; a < attrList.size(); a++) {
 								if(attrList.get(a).equalsIgnoreCase(attribute)) {
 									validAttr = true;	
 								}
 							
 							}
 							if (!validAttr) {
 								error = errorConstructor(attr + " " + sql.getErrMsg(23), "syntax", i+1, endAttrColumnNo, attribute);
 								errors.put(errorCount, error);
 								errorCount += 1;
 							}
 					
 						}
 					}
 					
 					
 				}
 			}
 			
 			/* START OF AMEER'S CODE */
 			ArrayList<JSONObject> encapErrorList = parseEncapsulationErrors(encap.getErrorList());
 			for(int i = 0; i < encapErrorList.size(); i++) {
 				errors.put(errorCount, encapErrorList.get(i));
 				errorCount++;
 			}
 			/* END OF AMEER'S CODE */
 			
 			if(!requiredTags.contains("html")) {
 				error = errorConstructor(sql.getErrMsg(3), "syntax", 1, 0, "");
 				errors.put(errorCount, error);
 				errorCount += 1;
 			}
 			if(!requiredTags.contains("head")) {
 				error = errorConstructor(sql.getErrMsg(4), "syntax", 1, 0, "");
 				errors.put(errorCount, error);
 				errorCount += 1;
 			}
 			if(!requiredTags.contains("body")) {
 				error = errorConstructor(sql.getErrMsg(6), "syntax", 1, 0, "");
 				errors.put(errorCount, error);
 				errorCount += 1;
 			}
 			if(!requiredTags.contains("title")) {
 				error = errorConstructor(sql.getErrMsg(5), "semantic", 1, 0, "");
 				errors.put(errorCount, error);
 				errorCount += 1;
 			}
 			if(!requiredTags.contains("meta")) {
 				error = errorConstructor(sql.getErrMsg(32), "semantic", 1, 0, "");
 				errors.put(errorCount, error);
 				errorCount += 1;
 			}
 			if(!requiredTags.contains("!doctype")) {
 				error = errorConstructor(sql.getErrMsg(2), "syntax", 1, 0, "");
 				errors.put(errorCount, error);
 				errorCount += 1;
 			}
 			
 			
 			
 			
 			errors.put("count", errorCount);
 			return errors;
 		}			
 		
 		/* START OF AMEER'S CODE */
 		/**
 		 * Takes the list of encapsulation errors and converts them to a
 		 * list of JSONObject error objects.
 		 * 
 		 * @param errors the list of encapsulation errors as Strings
 		 * @return an ArrayList of JSONObjects, containing each error
 		 */
 		private ArrayList<JSONObject> parseEncapsulationErrors(ArrayList<String> errors) {
 			Mysqlfunctions sql = new Mysqlfunctions();
 			int NUM_ERROR_VALUES = 4;
 			JSONObject error = new JSONObject();
 			String[] errorValues = new String[NUM_ERROR_VALUES];
 			ArrayList<JSONObject> errorList = new ArrayList<JSONObject>(errors.size());
 			
 			String type = "syntax";
 			int line = 1;
 			int col = 0;
 			int errorCode;
 			String errorExcerpt = "";
 			
 			for(int i = 0; i < errors.size(); i++) {
 				errorValues = errors.get(i).split(" ", NUM_ERROR_VALUES);
 				
 				errorCode = Integer.parseInt(errorValues[3]);
 				line = Integer.parseInt(errorValues[1]);
 				col = Integer.parseInt(errorValues[2]);
 				errorExcerpt = errorValues[0];
 				if (errorExcerpt == null) {
 					errorExcerpt = "";
 				}
 				col = col + errorExcerpt.length() - 1;
 				/*
 				if(errorExcerpt.charAt(0) == '/') {
 					errorExcerpt = "FIXED";
 					//errorExcerpt = errorValues[4].substring(1);
 				} else {
 					errorExcerpt = "FIXED";
 					//errorExcerpt = errorValues[4];
 				}*/
 				String message = sql.getErrMsg(errorCode);
 				if (message == null) {
 					message = "No message could be found for error code: "+Integer.toString(errorCode);
 				}
 				error = errorConstructor(message, type, line, col, errorExcerpt);
 				errorList.add(error);
 			}
 			
 			return errorList;
 		}
 		
 		/**
 		 * Helper function for error construction. Takes various parameters
 		 * and returns a JSONObject containing the values required for the
 		 * error object.
 		 * 
 		 * @param message the error message of the error
 		 * @param type the type of error
 		 * @param line the line number the error is located on
 		 * @param col the column the error starts
 		 * @param errorExcerpt the error excerpt of the error
 		 * @return a JSONObject representing the error
 		 */
 		private JSONObject errorConstructor(String message, String type, int line, int col, String errorExcerpt) {
 			JSONObject error = new JSONObject();
 			error.put("message", message);
 			error.put("type", type);
 			error.put("line", line);
 			error.put("col", col);
 			error.put("errorExcerpt", errorExcerpt);
 			error.put("colOffset", colOffset);
 			int lengthOffset = 0;
 			/*
 			for (int i = 0; i < errorExcerpt.length(); i++) {
 				if (errorExcerpt.charAt(i) == '<' || errorExcerpt.charAt(i) == '>') {
 					lengthOffset += 3;
 				} else if (errorExcerpt.charAt(i) == '"') {
 					lengthOffset += 5;
 				} else if (errorExcerpt.charAt(i) == '\'') {
 					lengthOffset += 4;
 				}
 			}
 			error.put("lengthOffset", lengthOffset);
 			*/
 			int expectedCol = col+1-errorExcerpt.length();
 			String lineText = source.get(line-1);
 			int index = 0;
 			int count = 0;
 			while (index != -1 && index != expectedCol) {
 				if (count == 0) {
 					index = lineText.indexOf(errorExcerpt);
 				} else {
 					index = lineText.indexOf(errorExcerpt, index+1);
 				}
 				count += 1;
 			}
 			
 			if (errorExcerpt.equals("/")) {
 				count += errorsInLine;
 			}
 			
 			if (index == -1) {
 				error.put("occ", -1);
 			} else {
 				error.put("occ", count);
 			}
 			
 			if (errorExcerpt.equals("")) {
 				error.put("occ", -1);
 			}
 			if (count != -1) {
 				errorsInLine++;
 			}
 			return error;
 		}
 		/* END OF AMEER'S CODE */
 		
 		/**
 		 * Adds a list of file names that have been passed along with the current file. Used to check
 		 * for broken links. User must always call addFilePath after addAssociatedFiles before 
 		 * using findErrors.
 		 * @param filenames List of filenames (including file path)
 		 */
 		public void addAssociatedFiles(List<String> filenames) {
 			filesInZip = filenames;
 		}
 		
 		/**
 		 * Adds a path of the file that is about to be checked. Used to find broken links.
 		 * Will still required a call to findErrors before the file is checked.
 		 * This method exists instead of an overloaded findErrors method.
 		 * @param filepath Full path of the file that will be checked next.
 		 */
 		public void addFilePath(String filepath) {
 			filePath = filepath;
 		}
 		
 		/**
 		 * Returns the number of broken links found in the last checked file.
 		 * @return The number of broken links in the last file.
 		 */
 		public int getBrokenLinks() {
 			return brokenLinks;
 		}
 		
 		/**
 		 * Checks whether a path reference exists in a zip file. Should be used on the values of
 		 * attributes like src. Any full path will be considered valid (eg. starting with http://).
 		 * Any reference to a javascript function will also be considered valid, despite the
 		 * risk that function does not exist.
 		 * @param filepath The file path to check whether valid
 		 * @return The string of where the file was expected if non-existent. Otherwise null.
 		 */
 		private String checkPathExists(String filepath) {
 			if (filesInZip == null) {
 				return null;
 			}
 			if (filepath.toLowerCase().indexOf("http://") == 0 || filepath.toLowerCase().indexOf("javascript:") == 0) {
 				return null;
 			}
 			String currentPath = upOneFolder(filePath); // eliminated the file name
 			try {
 			while (filepath.indexOf("../") == 0) {
 				currentPath = upOneFolder(currentPath);
 				filepath = filepath.substring(3);
 			}
 			currentPath = currentPath + filepath;
 			} catch (Exception ex) {
 				return "Something broke";
 			}
 			
 			if (filesInZip.contains(currentPath)) {
 				return null;
 			}
 			return currentPath;
 		}
 		
 		/**
 		 * Gets the file path of parent folder of current file.
 		 * @param path Path of the file to find parent folder of
 		 * @return The file path of the parent folder
 		 */
 		private String upOneFolder(String path) {
 			int iter = path.length()-1;
 			if (path.indexOf("/") != -1) {
 				while (path.charAt(iter) != '/') {
 					iter--;
 				}
 			} else {
 				return "";
 			}
 			return path.substring(0, iter);
 		}
 		
 		/**
 		 * Class for accessing the character array of the line of the HTML file
 		 * being parsed. 
 		 *
 		 * @author Ameer Sabri
 		 */
 		public class CharArray {
 				
 			private char[] charArray;
 				
 			/**
 			 * Constructor for the CharArray.
 			 */
 			public CharArray(char[] charArray) {
 				this.charArray = charArray;				
 			}
 					
 			/**
 			 * Returns the character at the index given.
 			 *
 			 * @param i the index of the character in the array.
 			 * @return the character at the specified index.
 			 */
 			public char getChar(int i) {
 				return charArray[i];
 			}
 					
 			/**
 			 * Returns the length of the CharArray.
 			 *
 			 * @return the length of the CharArray
 			 */
 			public int getLength() {
 				return charArray.length;
 			}
 					
 			/**
 			 * Returns a String that returns the tag given between the string
 			 * start and end index given in the array.
 			 *
 			 * @param strStart the start index of the string
 			 * @param strEnd the end index of the string
 			 * @return the string specified by the string's start and end index. 
 			 */
 			public String getString(int strStart, int strEnd) {
 					
 				StringBuilder str = new StringBuilder();
 				
 				/* Iterates through the array between the indices
 				 * and adds each character to the tag string. 
 				 */
 				for(int j=strStart; j < (strEnd + 1); j++) {
 					str.append(this.getChar(j));
 				}
 					
 				return str.toString();
 			}			
 		}
 
 }
