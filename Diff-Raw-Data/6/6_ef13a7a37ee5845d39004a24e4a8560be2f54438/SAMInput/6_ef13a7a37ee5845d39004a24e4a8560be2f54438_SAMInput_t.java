 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2012
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2012-01-23
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam;
 
 import java.util.Stack;
 import java.io.StringReader;
 import java.io.CharArrayReader;
 import java.util.ArrayList;
 import eu.serscis.sam.node.Switch;
 import eu.serscis.sam.node.Token;
 import java.io.Reader;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.File;
 import eu.serscis.sam.parser.ParserException;
 
 class SAMInput {
 	private final File file;
 	private final Model model;
 	private final String rawText;
 	private final ArrayList<Directive> directives = new ArrayList<Directive>();
 
 	public SAMInput(File file, Model model) throws Exception {
 		this.file = file;
 		this.model = model;
 		StringBuffer buffer = new StringBuffer();
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		int lnum = 1;
 		int off = 0;
 		String line;
 		while ((line = reader.readLine()) != null) {
 			if (line.startsWith("#declare ")) {
 				handleDeclare(model, line, lnum);
 				line = "";
 			} else if (line.startsWith("#")) {
 				directives.add(new Directive(line, lnum, off));
 				line = "";
 			}
 			buffer.append(line + "\n");
 			off += line.length() + 1;
 			lnum++;
 		}
 		rawText = buffer.toString();
 	}
 
 	private void handleDeclare(Model model, String directive, int line) throws Exception {
 		String[] parts = directive.split(" +");
 		if (parts.length == 3) {
 			if ("#declare".equals(parts[0]) && "scenario".equals(parts[1])) {
 				model.addScenario(new PreprocToken(parts[2], line, 0));
 				return;
 			}
 		}
 
 		throw new InvalidModelException(new RuntimeException("Invalid #declare: " + directive),
 					file.toString(), directive, line, 1);
 	}
 
 	public Reader getScenario(String scenario) throws InvalidModelException {
 		// (note: nesting isn't very useful at the moment, since there's only ever one scenario active)
 		Stack<Directive.NestedIf> stack = new Stack<Directive.NestedIf>();
 		char[] chars = rawText.toCharArray();
 		int cutStart = -1;
 		int ignoresOnStack = 0;
 
 		for (Directive d : directives) {
 			String elseFor = null;
 			String[] parts = d.line.split(" +", 2);
 			String tok = parts[0];
 
 			if (tok.equals("#endif") || tok.equals("#elif") || tok.equals("#else")) {
 				if (tok.equals("#elif") && parts.length != 2) {
 					throw new InvalidModelException(new RuntimeException("Syntax error in #elif"),
 							file.toString(), d.line, d.lnum, 1);
 				} else if (parts.length != 1) {
 					throw new InvalidModelException(new RuntimeException("Syntax error in " + tok),
 							file.toString(), d.line, d.lnum, 1);
 				}
 
 				if (stack.empty()) {
 					throw new InvalidModelException(new RuntimeException("Unmatched #endif"),
 							file.toString(), d.line, d.lnum, 1);
 				}
 				Directive.NestedIf start = stack.pop();
 				if (!start.match) {
 					ignoresOnStack--;
 					if (ignoresOnStack == 0) {
 						for (int i = cutStart; i < d.off; i++) {
 							if (chars[i] != '\n') {
 								chars[i] = ' ';
 							}
 						}
 						cutStart = -1;
 					}
 				}
 
 				if (tok.equals("#else")) {
 					elseFor = start.getDirective().getArg();
 				}
 			}
 
 			if (parts[0].equals("#if") || parts[0].equals("#elif") || tok.equals("#else")) {
 				boolean match;
 				if (tok.equals("#else")) {
 					match = !elseFor.equals(scenario);
 				} else {
 					if (parts.length != 2) {
 						throw new InvalidModelException(new RuntimeException("Invalid " + parts[0] + " directive"),
 									file.toString(), d.line, d.lnum, 1);
 					}
 					String ifScenario = parts[1];
 					if (!model.scenarios.contains(ifScenario)) {
 						throw new InvalidModelException(new RuntimeException("Scenario '" + ifScenario + "' not declared"),
 									file.toString(), d.line, d.lnum, 5);
 					}
 					match = ifScenario.equals(scenario);
 				}
 				stack.push(d.nest(match));
 				if (!match) {
 					ignoresOnStack++;
 				}
 				if (cutStart == -1 && !match) {
 					cutStart = d.off;
 				}
 			} else if (!d.line.trim().equals("#endif")) {
				String extra = "";
				if (tok.equals("#ifdef")) {
					extra = " (did you mean #if ?)";
				}
				throw new InvalidModelException(new RuntimeException("Invalid pre-processor directive" + extra),
 							file.toString(), d.line, d.lnum, 1);
 			}
 		}
 
 		if (!stack.empty()) {
 			Directive.NestedIf last = stack.pop();
 			Directive d = last.getDirective();
 			throw new InvalidModelException(new RuntimeException("Missing #endif"),
 						file.toString(), d.line, d.lnum, 1);
 		}
 
 		return new CharArrayReader(chars);
 	}
 
 	public File getSource() {
 		return file;
 	}
 
 	public Reader getRawReader() {
 		return new StringReader(rawText);
 	}
 
 	private static class PreprocToken extends Token {
 		public PreprocToken(String text, int line, int pos) {
 			setText(text);
 			setLine(line);
 			setPos(pos);
 		}
 
 		@Override
 		public PreprocToken clone() {
 			return new PreprocToken(getText(), getLine(), getPos());
 		}
 
 		public void apply(Switch s) {
 		}
 	}
 
 	private class Directive {
 		private String line;
 		private int lnum;
 		private int off;
 
 		public Directive(String line, int lnum, int off) {
 			this.line = line;
 			this.lnum = lnum;
 			this.off = off;
 		}
 
 		public String getArg() throws InvalidModelException {
 			String[] parts = line.split(" +", 2);
 			if (parts.length != 2) {
 				throw new InvalidModelException(new RuntimeException("Wrong number of arguments"),
 						file.toString(), line, lnum, 1);
 			}
 			return parts[1];
 		}
 
 		public NestedIf nest(boolean match) {
 			return new NestedIf(match);
 		}
 
 		public class NestedIf {
 			public final boolean match;
 
 			public NestedIf(boolean match) {
 				this.match = match;
 			}
 
 			public Directive getDirective() {
 				return Directive.this;
 			}
 		}
 	}
 }
