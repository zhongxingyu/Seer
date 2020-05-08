 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 /**
  * 
  */
 package org.eclipse.dltk.ruby.core.text;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class RubyKeyword {
 
 	private final String name;
 
 	private static final Map keywords = new HashMap();
 
 	private final RubyContext introducedContext;
 
 	private RubyKeyword(String name, RubyContext introducedContext) {
 		this.name = name;
 		this.introducedContext = introducedContext;
 		keywords.put(name, this);
 	}
 
 	public RubyContext getIntroducedContext() {
 		return introducedContext;
 	}
 
 	public String toString() {
 		return name;
 	}
 
 	public static RubyKeyword byName(String name) {
 		return getKeyword(name, name.length());
 	}
 	
	private static String[] sKeywords = new String[] {"if", "else", "elsif", "unless", "while", "until", "in"
 		,"case", "when", "begin", "ensure", "module", "for", "then", "do", "and", "or", "not"
 		,"rescue", "return", "break", "next", "yield", "defined?", "super", "def", "undef", "alias", "class"
 		,"end", "self", "false", "true", "retry", "nil", "redo", "BEGIN", "END", "__LINE__", "__FILE__",
 		"sub", "sub!", "gsub", "gsub!", "scan", "index", "match", "require" };
 	
 	private static Map sKeywordContext = new HashMap();
 	static {
 		sKeywordContext.put("if", RubyContext.EXPRESSION_START);
 		sKeywordContext.put("else", RubyContext.EXPRESSION_START);
		sKeywordContext.put("elsif", RubyContext.EXPRESSION_START);
 		sKeywordContext.put("unless", RubyContext.EXPRESSION_START);
 		sKeywordContext.put("while", RubyContext.EXPRESSION_START);
 		sKeywordContext.put("until", RubyContext.EXPRESSION_START);
 		sKeywordContext.put("in", RubyContext.EXPRESSION_START);
 		sKeywordContext.put("case", RubyContext.EXPRESSION_START);
 		sKeywordContext.put("when", RubyContext.EXPRESSION_START);
 		sKeywordContext.put("begin", RubyContext.COMMAND_START);
 		sKeywordContext.put("ensure", RubyContext.COMMAND_START);
 		sKeywordContext.put("module", RubyContext.COMMAND_START);
 		sKeywordContext.put("for", RubyContext.COMMAND_START);
 		sKeywordContext.put("then", RubyContext.COMMAND_START);
 		sKeywordContext.put("do", RubyContext.COMMAND_START);
 		sKeywordContext.put("and", RubyContext.COMMAND_START);
 		sKeywordContext.put("or", RubyContext.COMMAND_START);
 		sKeywordContext.put("not", RubyContext.COMMAND_START);
 		sKeywordContext.put("rescue", RubyContext.KEYWORD_ARGUMENT);
 		sKeywordContext.put("return", RubyContext.KEYWORD_ARGUMENT);
 		sKeywordContext.put("break", RubyContext.KEYWORD_ARGUMENT);
 		sKeywordContext.put("next", RubyContext.KEYWORD_ARGUMENT);
 		sKeywordContext.put("yield", RubyContext.ARGUMENT);
 		sKeywordContext.put("defined?", RubyContext.ARGUMENT);
 		sKeywordContext.put("super", RubyContext.ARGUMENT);
 		sKeywordContext.put("def",RubyContext.NAME);
 		sKeywordContext.put("undef", RubyContext.NAME);
 		sKeywordContext.put("alias", RubyContext.NAME);
 		sKeywordContext.put("class", RubyContext.NAME);
 		sKeywordContext.put("end", RubyContext.NAME); 
 		sKeywordContext.put("self", RubyContext.NAME);
 		sKeywordContext.put("false", RubyContext.NAME);
 		sKeywordContext.put("true", RubyContext.NAME);
 		sKeywordContext.put("retry", RubyContext.NAME);
 		sKeywordContext.put("nil", RubyContext.NAME);
 		sKeywordContext.put("redo",RubyContext.NAME);
 		sKeywordContext.put("BEGIN", RubyContext.NAME);
 		sKeywordContext.put("END", RubyContext.NAME);
 		sKeywordContext.put("__LINE__", RubyContext.NAME);
 		sKeywordContext.put("__FILE__", RubyContext.NAME);
 		sKeywordContext.put("sub", RubyContext.NAME);
 		sKeywordContext.put("sub!", RubyContext.NAME);
 		sKeywordContext.put("gsub", RubyContext.NAME);
 		sKeywordContext.put("gsub!", RubyContext.NAME);
 		sKeywordContext.put("scan", RubyContext.NAME);
 		sKeywordContext.put("index", RubyContext.NAME);
 		sKeywordContext.put("match", RubyContext.NAME);
 	}
 
 	public static String[] findByPrefix (String prefix) {
 		List result = new ArrayList ();
 		for (int i = 0; i < sKeywords.length; i++) {
 			if (sKeywords[i].startsWith(prefix))
 				result.add(sKeywords[i]);
 		}		
 		return (String[]) result.toArray(new String[result.size()]);
 	}
 
 	public static RubyKeyword getKeyword(String str, int len) {
 		for (int i = 0; i < sKeywords.length; i++) {
 			if (sKeywords[i].equals(str)) {
 				return new RubyKeyword(str, (RubyContext)sKeywordContext.get(str));
 			}
 		}
 		return null;
 	}
 }
