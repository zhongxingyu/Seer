 /**
  *
  * nutz - Markdown processor for JVM
  * Copyright (c) 2012, Sandeep Gupta
  * 
  * http://www.sangupta/projects/nutz
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.nutz;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 
 import com.sangupta.nutz.ast.AbstractListNode;
 import com.sangupta.nutz.ast.BlockQuoteNode;
 import com.sangupta.nutz.ast.CodeBlockNode;
 import com.sangupta.nutz.ast.HRuleNode;
 import com.sangupta.nutz.ast.HeadingNode;
 import com.sangupta.nutz.ast.LineType;
 import com.sangupta.nutz.ast.ListItemNode;
 import com.sangupta.nutz.ast.NewLineNode;
 import com.sangupta.nutz.ast.Node;
 import com.sangupta.nutz.ast.OrderedListNode;
 import com.sangupta.nutz.ast.ParagraphNode;
 import com.sangupta.nutz.ast.PlainTextNode;
 import com.sangupta.nutz.ast.RootNode;
 import com.sangupta.nutz.ast.UnorderedListNode;
 
 /**
  * Parse the given markup and create an AST (abstract syntax tree),
  * 
  * @author sangupta
  * @since 0.1
  */
 public class Parser {
 
 	/**
 	 * Internal reader that reads line by line from the markup
 	 * provided.
 	 */
 	protected BufferedReader reader = null;
 	
 	/**
 	 * Currently read line
 	 */
 	protected TextLine line = null;
 	
 	/**
 	 * Reference to the root node of the AST
 	 */
 	protected final RootNode ROOT_NODE = new RootNode();
 	
 	/**
 	 * A collector that is used to collect data when we enter
 	 * an iterative function that needs look ahead.
 	 */
 	protected StringBuilder collector = new StringBuilder(1024);
 	
 	/**
 	 * Reference to the last node that was added to the AST
 	 */
 	protected Node lastNode = null;
 	
 	/**
 	 * Internal reference to the {@link TextNodeParser} that is used to
 	 * parse text nodes recursively.
 	 */
 	protected TextNodeParser textNodeParser;
 	
 	/**
 	 * Specifies the options that are to be used when parsing and emitting
 	 * HTML code.
 	 */
 	protected ProcessingOptions options = null;
 	
 	/**
 	 * Default constructor that creates an instance of this parser
 	 * enabling all default extensions.
 	 */
 	public Parser() {
 		this.options = new ProcessingOptions();
 		reset();
 	}
 	
 	/**
 	 * Contruct a new instance of this parser and set the provided
 	 * processing options to be used.
 	 * 
 	 * @param processingOptions
 	 */
 	public Parser(ProcessingOptions processingOptions) {
 		this.options = processingOptions;
 		reset();
 	}
 	
 	/**
 	 * Reset internal state
 	 */
 	protected void reset() {
 		this.line = null;
 		this.lastNode = null;
 		this.collector.setLength(0);
 		if(ROOT_NODE.getReferenceLinks() != null) {
 			ROOT_NODE.getReferenceLinks().clear();
 		}
 		
 		if(ROOT_NODE.getChildren() != null) {
 			ROOT_NODE.getChildren().clear();
 		}
 	}		
 	
 	/**
 	 * Parse the string-represented markup and create the AST.
 	 * 
 	 * @param markup
 	 * @return
 	 * @throws Exception
 	 */
 	public RootNode parse(String markup) throws IOException {
 		reader = new BufferedReader(new StringReader(markup));
 		reset();
 		readLines(ROOT_NODE);
 		return ROOT_NODE;
 	}
 	
 	/**
 	 * Parse the text stream specified by Reader and create the AST.
 	 * 
 	 * @param reader
 	 * @return
 	 * @throws IOException
 	 */
 	public RootNode parse(Reader reader) throws IOException {
 		if(!(reader instanceof BufferedReader)) {
 			this.reader = new BufferedReader(reader);
 		}
 		reset();
 		readLines(ROOT_NODE);
 		return ROOT_NODE;
 	}
 	
 	/**
 	 * Read all lines one-by-one and create the AST.
 	 * 
 	 * @throws Exception
 	 */
 	protected void readLines(Node root) throws IOException {
 		// create a text node parser
 		// this is done later to make sure that it can be overridden
 		// in child implementations
 		if(this.textNodeParser == null) {
 			this.textNodeParser = new TextNodeParser();
 		}
 
 		// reset all values
 		// clear up any current collector
 		lastNode = null;
 		collector.setLength(0);
 		
 		// start parsing
 		TextLine currentLine = new TextLine("", this.options);
 		do {
 			// the check on this.line == currentLine is intended
 			// we actually need to compare references to see
 			// if any of the downstream code read more of stuff
 			// and moved the pointer down
 			// this will allow us to consume the remaining line
 			if(this.line == null || this.line == currentLine) {
 				readLine();
 			}
 			currentLine = this.line;
 			
 			if(this.line.isNull) {
 				return;
 			}
 			
 			this.lastNode = parseLine(root);
 			if(this.lastNode != null) {
 				root.addChild(this.lastNode);
 			}
 		} while(true);
 	}
 	
 	/**
 	 * Read one more line from the input buffer
 	 * 
 	 * @throws IOException
 	 */
 	protected void readLine() throws IOException {
 		this.line = new TextLine(reader.readLine(), this.options);
 	}
 
 	/**
 	 * Parse the read line and construct AST
 	 * 
 	 * @param line
 	 * @throws Exception
 	 */
 	protected Node parseLine(Node currentRoot) throws IOException {
 		LineType lineType = this.line.lineType;
 		
 		switch(lineType) {
 			case HtmlComment:
 				return parseText(currentRoot, true);
 				
 			case Heading:
 				return parseHeading(currentRoot);
 				
 			case FencedCodeBlock:
 				return parseFencedCodeBlock();
 				
 			case HeadingIndicator:
 				if(lastNode instanceof ParagraphNode) {
 					int style = 1;
 					if(this.line.charAt(this.line.leadingPosition) == '-') {
 						style = 2;
 					}
 					
 					boolean broken = ((ParagraphNode) lastNode).breakIntoTextAndHeading(style);
 					if(broken) {
 						return null;
 					}
 				}
 				if(this.line.horizontalRule) {
 					return new HRuleNode();
 				}
 				
 			case UnorderedList:
 				return parseList(currentRoot, false);
 				
 			case OrderedList:
 				return parseList(currentRoot, true);
 				
 			case HRule:
 				return new HRuleNode();
 				
 			case CodeBlock:
 				return parseVerbatimBlock();
 				
 			case BlockQuote:
 				String blockText = parseBlockText();
 				RootNode rootNode = new Parser().parse(blockText);
 				return new BlockQuoteNode(rootNode);
 				
 			case Empty:
 				return new PlainTextNode(currentRoot, "\n");
 				
 			case LinkReference:
 				boolean found = parseLinkReference();
 				if(found) {
 					return null;
 				}
 				
 			case UnknownText:
 				return parseText(currentRoot, true);
 				
 			case Xml:
 				throw new IllegalArgumentException("This case has never been coded");
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Create a code block out of the verbatim block that has been created using
 	 * 4 starting spaces.
 	 * 
 	 * @param line
 	 * @return
 	 * @throws IOException
 	 */
 	protected CodeBlockNode parseVerbatimBlock() throws IOException {
 		if(this.line.trimEmpty) {
 			return null;
 		}
 		
 		String lang = null;
 		boolean firstLine = true;
 		
 		collector.setLength(0);
 		
 		int empty = 0;
 		
 doWhileLoop:		
 		do {
 			if(firstLine) {
 				int index = line.trim().indexOf('!');
 				if(index != -1) {
 					int spaceIndex = line.indexOf(Identifiers.SPACE, index + 1);
 					if(spaceIndex == -1) {
 						lang = line.substring(0, index).trim();
 					}
 				}
 			}
 
 			// append line to collector
 			if(!firstLine || lang == null) {
 				collector.append(line);
 				collector.append(Identifiers.NEW_LINE);
 			}
 			
 			// read one more line
 			firstLine = false;
 			readLine();
 			if(line.isNull) {
 				break;
 			}
 			
 			switch(line.lineType) {
 				case CodeBlock:
 					empty = 0;
 					continue;
 					
 				case Empty:
 					empty++;
 					if(empty >= 2) {
 						break doWhileLoop;
 					}
 					continue;
 					
 				default:
 					break doWhileLoop;
 			}
 		} while(true);
 		
		return new CodeBlockNode(collector.toString().trim(), lang);
 	}
 
 	/**
 	 * Parse the given line and extract the link reference that is present in it
 	 * and add it to the root node.
 	 * 
 	 * @param line
 	 * @return
 	 */
 	protected boolean parseLinkReference() {
 		int index = this.line.indexOf(']');
 		if(index == -1) {
 			return false;
 		}
 		
 		if(!(line.charAt(index + 1) == ':')) {
 			return false;
 		}
 		
 		String id = this.line.substring(this.line.leadingPosition + 1, index);
 		String link = this.line.substring(index + 2).trim();
 		
 		// extract any title if available
 		String[] tokens = MarkupUtils.parseLinkAndTitle(link);
 		
 		ROOT_NODE.addReferenceLink(id, tokens[0].trim(), tokens[1]);
 		
 		return true;
 	}
 
 	/**
 	 * Create an ordered list
 	 * 
 	 * @param line
 	 * @return
 	 * @throws IOException
 	 */
 	protected Node parseList(Node currentRoot, boolean ordered) throws IOException {
 		AbstractListNode listNode;
 		if(ordered) {
 			listNode = new OrderedListNode();
 		} else {
 			listNode = new UnorderedListNode();
 		}
 		
 		// we store current lists leading position
 		// this will help us identify child lists
 		final int thisListsLeadingPosition = this.line.leadingPosition;
 		
 		StringBuilder collector = new StringBuilder(512);
 		int looseEntries = 0;
 		int empty = 0;
 		ListItemNode lastChildNode = null;
 		boolean hangingText = false;
 		
 doWhileLoop:
 		do {
 			LineType lineType = this.line.lineType;
 			
 			if(lineType == null) {
 				break doWhileLoop;
 			}
 			
 			// check for sublists
 			if(lineType == LineType.CodeBlock) {
 				if(this.line.isUnorderedList()) {
 					lineType = LineType.UnorderedList;
 				} else if(this.line.lineStartsWithNumber()) {
 					lineType = LineType.OrderedList;
 				}
 			}
 			
 			switch(lineType) {
 				case OrderedList:
 				case UnorderedList:
 					if(collector.length() > 0) {
 						String text = collector.toString();
 						collector.setLength(0);
 						
 						// check for loose mode
 						if(listNode.getChildren() == null) {
 							if(empty > 0) {
 								listNode.setLooseItems(true);
 							}
 						}
 						
 						lastChildNode = (ListItemNode) this.textNodeParser.parse(listNode, new ListItemNode(listNode), text);
 						if(!hangingText) {
 							listNode.addChild(lastChildNode);
 						} else {
 							listNode.lastNode().addChild(lastChildNode);
 						}
 						
 						hangingText = false;
 					}
 					
 					// check for child lists
 					if(this.line.leadingPosition > thisListsLeadingPosition) {
 						// we have encountered a child list
 						// read a new child list
 						Node childList = parseList(listNode, this.line.lineType == LineType.OrderedList ? true : false);
 						
 						// get current element of the list
 						Node currentElementInList = listNode.lastNode();
 						currentElementInList.addChild(childList);
 						
 						switch(this.line.lineType) {
 							case OrderedList:
 							case UnorderedList:
 								break;
 								
 							case CodeBlock:
 							case UnknownText:
 								hangingText = true;
 								break;
 								
 							default:
 								break doWhileLoop;
 						}
 						
 						// continue and process the new line
 						continue;
 					}
 					
 					if(this.line.leadingPosition < thisListsLeadingPosition) {
 						// break out
 						// we are done creating a child list
 						break doWhileLoop;
 					}
 					
 					String text = this.line.getText();
 					
 					int start = this.line.leadingPosition + 1;
 					if(ordered) {
 						int dot = this.line.indexOf('.');
 						start = dot + 1;
 					} else {
 						start = this.line.leadingPosition + 1;
 					}
 
 					// extract the text
 					if(MarkupUtils.isWhiteSpace(text.charAt(start))) {
 						text = text.substring(++start);
 					} else {
 						text = text.substring(start);
 					}
 					
 					// append to collector
 					collector.append(text);
 					collector.append(Identifiers.NEW_LINE);
 					empty = 0;
 					break;
 					
 				case UnknownText:
 				case CodeBlock:
 					// break off on sub lists
 					if(this.line.leadingPosition <= thisListsLeadingPosition) {
 						// break out
 						// we are done creating a child list
 						break doWhileLoop;
 					}
 					
 					if(empty == 0 || (lineType == LineType.CodeBlock) && (empty < 2)) {
 						collector.append(this.line.getText());
 						collector.append(Identifiers.NEW_LINE);
 					} else {
 						break doWhileLoop;
 					}
 					
 					empty = 0;
 					break;
 					
 				case Empty:
 					empty++;
 					looseEntries++;
 					collector.append(this.line.getText());
 					collector.append(Identifiers.NEW_LINE);
 					if(empty == 2) {
 						break doWhileLoop;
 					}
 					break;
 
 				// for all other line types break off
 				default:
 					break doWhileLoop;
 			}
 			
 			readLine();
 			
 			if(this.line.isNull) {
 				break;
 			}
 		} while(true);
 		
 		// set the last element
 		if(collector.length() > 0) {
 			String text = collector.toString();
 			if(looseEntries == empty) {
 				do {
 					if(text.endsWith("\n\n")) {
 						text = text.substring(0, text.length() - 1);
 					} else {
 						break;
 					}
 				} while(true);
 			}
 			
 			lastChildNode = (ListItemNode) this.textNodeParser.parse(listNode, new ListItemNode(listNode), text);
 			
 			if(!hangingText) {
 				listNode.addChild(lastChildNode);
 			} else {
 				listNode.lastNode().addChild(lastChildNode);
 			}
 		}
 		
 		return listNode;
 	}
 
 	/**
 	 * Read a fenced code block from the line
 	 * 
 	 * @param line
 	 * @return
 	 */
 	protected CodeBlockNode parseFencedCodeBlock() throws IOException {
 		String language = null;
 		char terminator = this.line.terminator;
 		
 		if(line.length > 3) {
 			language = line.substring(3);
 		}
 		
 		collector.setLength(0);
 		
 		// start reading more lines
 		// till we get an ending fenced code block
 		do {
 			readLine();
 			
 			if(line.isNull) {
 				break;
 			}
 			
 			if(line.startsWith(terminator)) {
 				break;
 			}
 			
 			collector.append(line);
 			collector.append("\n");
 		} while(true);
 		
 		// read one more line from the file as we
 		// are still on the ending separator
 		readLine();
 		
		CodeBlockNode codeBlock = new CodeBlockNode(collector.toString().trim(), language);
 		return codeBlock;
 	}
 
 	/**
 	 * Parse a heading from this line
 	 * 
 	 * @param line
 	 * @return
 	 * @throws Exception
 	 */
 	protected HeadingNode parseHeading(Node currentRoot) throws IOException {
 		int headCount = 1;
 		int index = 1;
 		do {
 			if(line.charAt(index) == '#') {
 				headCount++;
 			} else {
 				break;
 			}
 			
 			index++;
 		} while(true);
 
 		// strip off all hash signs per the last non-hash character
 		this.line.trim();
 		index = this.line.length;
 		do {
 			if(this.line.charAt(index - 1) == '#') {
 				// skip
 			} else {
 				break;
 			}
 			
 			index--;
 		} while(true);
 		
 		this.line = new TextLine(this.line.substring(headCount, index).trim(), this.options);
 		
 		Node textNode = parseText(currentRoot, false);
 		HeadingNode heading = new HeadingNode(headCount, textNode);
 		
 		this.line = null;
 		
 		return heading;
 	}
 	
 	/**
 	 * Parse text from the given line
 	 * 
 	 * @param readLine
 	 * @param fetchMoreLines indicates if we can read ahead more lines
 	 * @return
 	 * @throws Exception
 	 */
 	protected Node parseText(Node currentRoot, boolean fetchMoreLines) throws IOException {
 		if(!fetchMoreLines) {
 			return textNodeParser.parse(currentRoot, this.line.getText());
 		}
 		
 		if(this.line.trimEmpty) {
 			return new NewLineNode(currentRoot);
 		}
 		
 		collector.setLength(0);
 
 		doWhileLoop:		
 		do {
 			if(this.line.isEmpty) {
 				// this is a break for a new line
 				// exit now
 				break doWhileLoop;
 			}
 			
 			collector.append(this.line.getText());
 			collector.append('\n');
 			
 			if(this.line.endsWith("  ")) {
 				this.line = new TextLine(reader.readLine(), this.options);
 				break doWhileLoop;
 			}
 
 			this.line = new TextLine(reader.readLine(), this.options);
 			if(line == null || line.isEmpty || line.trimEmpty) {
 				break doWhileLoop;
 			}
 			
 			// this signifies a presence of heading
 			// need to break here
 			if(this.line.startsWith("===") || this.line.startsWith("---")) {
 				break doWhileLoop;
 			}
 		} while(true);
 		
 		return textNodeParser.parse(currentRoot, collector.toString());
 	}
 	
 	/**
 	 * Parse block quote text that is identified by presence of a 
 	 * greater than sign at the beginning of the line.
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	protected String parseBlockText() throws IOException {
 		StringBuilder builder = new StringBuilder(1024);
 		
 		int index = -1;
 		String text;
 		do {
 			index = line.indexOf('>');
 
 			if(index >= 0) {
 				index++;
 				if(index < line.length && line.charAt(index) == Identifiers.SPACE) {
 					text = line.substring(index + 1);
 				} else {
 					text = line.substring(index);
 				}
 
 				builder.append(text);
 				builder.append(Identifiers.NEW_LINE);
 			}
 			
 			readLine();
 			
 			if(this.line.isNull) {
 				break;
 			}
 		} while(index >= 0);
 		
 		return builder.toString();
 	}
 	
 }
