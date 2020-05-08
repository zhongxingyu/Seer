 /************************************************************************
  * Copyright (c) 2009, Cloudsmith Inc and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * - Cloudsmith Inc - initial API and implementation.
  * 
  *************************************************************************/
 package org.eclipse.b3;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.xtext.Grammar;
 import org.eclipse.xtext.GrammarUtil;
 import org.eclipse.xtext.IGrammarAccess;
 import org.eclipse.xtext.conversion.IValueConverter;
 import org.eclipse.xtext.conversion.ValueConverter;
 import org.eclipse.xtext.conversion.ValueConverterException;
 import org.eclipse.xtext.conversion.impl.AbstractDeclarativeValueConverterService;
 import org.eclipse.xtext.conversion.impl.AbstractNullSafeConverter;
 import org.eclipse.xtext.parsetree.AbstractNode;
 import org.eclipse.xtext.util.Strings;
 
 import com.google.inject.Inject;
 
 /**
  * Converters for BeeLang terminals.
  */
 public class BeeLangTerminalConverters extends AbstractDeclarativeValueConverterService {
 
 	private Grammar grammar;
 
 	@ValueConverter(rule = "BooleanValue")
 	public IValueConverter<Boolean> BooleanValue() {
 		return new IValueConverter<Boolean>() {
 
 			public String toString(Boolean value) {
 				return value.toString();
 			}
 
 			public Boolean toValue(String string, AbstractNode node) {
 				if(Strings.isEmpty(string))
 					throw new ValueConverterException("Could not convert empty string to boolean", node, null);
 				return new Boolean(string).equals(Boolean.TRUE)
 						? Boolean.TRUE
 						: Boolean.FALSE;
 			}
 
 		};
 	}
 
 	@ValueConverter(rule = "DOCUMENTATION")
 	public IValueConverter<String> DOCUMENTATION() {
 		return new AbstractNullSafeConverter<String>() {
 			@Override
 			protected String internalToString(String value) {
 				String converted = Strings.convertToJavaString(value, true);
 				converted = converted.substring(2, converted.length() - 2);
 				return "/**\n" + converted + (converted.length() > 0
 						? "\n"
 						: "") + "*/\n";
 			}
 
 			@Override
 			protected String internalToValue(String string, AbstractNode node) {
 				String lines[] = string.split("[\n\r]");
 				StringBuffer buf = new StringBuffer();
 				for(int i = 0; i < lines.length; i++) {
 					// get rid of documentation start
 					String s = lines[i];
 					if(s.startsWith("/**"))
 						lines[i] = s = s.substring(3);
 					// get rid of documentation end
 					if(s.endsWith("*/"))
 						lines[i] = s = s.substring(0, s.length() - 2);
 					String trimmed = s.trim();
 					if(!trimmed.startsWith("*")) {
 						// no leading *, keep the whitespace at the beginning
 						int pos = s.indexOf(trimmed);
 						if(pos > 0)
 							lines[i] = s.substring(0, pos) + trimmed;
 					}
 					else {
 						while(trimmed.startsWith("*"))
 							trimmed = trimmed.substring(1, trimmed.length());
 						lines[i] = trimmed;
 					}
 					if(i != 0)
 						buf.append("\n");
 					buf.append(lines[i]);
 				}
 				return Strings.convertFromJavaString(buf.toString(), true);
 			}
 		};
 	}
 
 	@Override
 	protected Grammar getGrammar() {
 		return grammar;
 	}
 
 	@ValueConverter(rule = "ID")
 	public IValueConverter<String> ID() {
 		return new AbstractNullSafeConverter<String>() {
 			@Override
 			protected String internalToString(String value) {
 				if(GrammarUtil.getAllKeywords(getGrammar()).contains(value)) {
 					return "^" + value;
 				}
 				return value;
 			}
 
 			@Override
 			protected String internalToValue(String string, AbstractNode node) {
 				return string.startsWith("^")
 						? string.substring(1)
 						: string;
 			}
 		};
 	}
 
 	@ValueConverter(rule = "IntValue")
 	public IValueConverter<Integer> IntValue() {
 		return new IValueConverter<Integer>() {
 
 			public String toString(Integer value) {
 				return value.toString();
 			}
 
 			public Integer toValue(String string, AbstractNode node) throws ValueConverterException {
 				int radix = 10;
 				if(Strings.isEmpty(string))
 					throw new ValueConverterException("Can not convert empty string to int", node, null);
 				try {
 					if(string.startsWith("0x") || string.startsWith("0X")) {
 						radix = 16;
 						string = string.substring(2);
 					}
 					else if(string.startsWith("0") && string.length() > 1)
 						radix = 8;
 
 					return new Integer(Integer.valueOf(string, radix));
 				}
 				catch(NumberFormatException e) {
 					String format = "";
 					switch(radix) {
 						case 8:
 							format = "octal";
 							break;
 						case 10:
 							format = "decimal";
 							break;
 						case 16:
 							format = "hexadecimal";
 							break;
 					}
 					throw new ValueConverterException(
 						"Can not convert to " + format + " integer : " + string, node, null);
 				}
 			}
 
 		};
 	}
 
 	@ValueConverter(rule = "Path")
 	public IValueConverter<URI> Path() {
 		return URI();
 	}
 
 	@ValueConverter(rule = "REGULAR_EXPR")
 	public IValueConverter<Pattern> Pattern() {
 		return new IValueConverter<Pattern>() {
 
 			public String toString(Pattern value) {
 				StringBuffer buffer = new StringBuffer();
 				buffer.append("/");
 				buffer.append(value.toString());
 				buffer.append("/");
 				int flags = value.flags();
 				if((flags & Pattern.CANON_EQ) != 0)
 					buffer.append('c');
 				if((flags & Pattern.DOTALL) != 0)
 					buffer.append('d');
 				if((flags & Pattern.CASE_INSENSITIVE) != 0)
 					buffer.append('i');
 				if((flags & Pattern.MULTILINE) != 0)
 					buffer.append('m');
 				if((flags & Pattern.UNICODE_CASE) != 0)
 					buffer.append('u');
 				return buffer.toString();
 			}
 
 			public Pattern toValue(String string, AbstractNode node) {
 				if(Strings.isEmpty(string))
 					throw new ValueConverterException(
 						"Could not convert empty string to regular expression", node, null);
 				int firstSlash = string.indexOf('/');
 				int lastSlash = string.lastIndexOf('/');
 				if(lastSlash - firstSlash <= 0)
 					throw new ValueConverterException("The regular expression is empty", node, null);
 				String patternString = string.substring(firstSlash + 1, lastSlash);
 				String flagString = string.substring(lastSlash + 1);
 				int flags = 0;
 				int counts[] = new int[5];
 				for(int i = 0; i < flagString.length(); i++)
 					switch(flagString.charAt(i)) {
 						case 'i':
 							counts[0]++;
 							flags |= Pattern.CASE_INSENSITIVE;
 							break;
 						case 'm':
 							counts[1]++;
 							flags |= Pattern.MULTILINE;
 							break;
 						case 'u':
 							counts[2]++;
 							flags |= Pattern.UNICODE_CASE;
 							break;
 						case 'c':
 							counts[3]++;
 							flags |= Pattern.CANON_EQ;
 							break;
 						case 'd':
 							counts[4]++;
 							flags |= Pattern.DOTALL;
 							break;
 						default:
 							throw new ValueConverterException(
 								"Flag character after /: expected one of i, m, u, c, d, but got: '" +
 										flagString.charAt(i) + "'.", node, null);
 					}
 				for(int i = 0; i < counts.length; i++)
 					if(counts[i] > 1)
 						throw new ValueConverterException("Flag character after /: used multiple times.", node, null);
 				try {
 					return Pattern.compile(patternString, flags);
 				}
 				catch(PatternSyntaxException e) {
 					throw new ValueConverterException(
 						"Could not convert '" + string + "' to regular expression", node, e);
 				}
 				catch(IllegalArgumentException e) {
 					throw new ValueConverterException(
 						"Internal error translating pattern flags - please log bug report", node, e);
 				}
 			}
 
 		};
 	}
 
 	@ValueConverter(rule = "RealValue")
 	public IValueConverter<Double> RealValue() {
 		return new IValueConverter<Double>() {
 
 			public String toString(Double value) {
 				return value.toString();
 			}
 
 			public Double toValue(String string, AbstractNode node) {
 				if(Strings.isEmpty(string))
 					throw new ValueConverterException("Could not convert empty string to double", node, null);
 				try {
 					return new Double(string);
 				}
 				catch(NumberFormatException e) {
 					throw new ValueConverterException("Could not convert '" + string + "' to double", node, null);
 				}
 			}
 
 		};
 	}
 
 	@Override
 	@Inject
 	public void setGrammar(IGrammarAccess grammarAccess) {
 		this.grammar = grammarAccess.getGrammar();
 	}
 
 	@ValueConverter(rule = "STRING")
 	public IValueConverter<String> STRING() {
 		return new AbstractNullSafeConverter<String>() {
 			@Override
 			protected String internalToString(String value) {
 				return '"' + Strings.convertToJavaString(value) + '"';
 			}
 
 			@Override
 			protected String internalToValue(String string, AbstractNode node) {
 				return Strings.convertFromJavaString(string.substring(1, string.length() - 1), true);
 			}
 		};
 	}
 
 	@ValueConverter(rule = "URI")
 	public IValueConverter<URI> URI() {
 		return new IValueConverter<URI>() {
 
 			public String toString(URI value) {
 				if(value == null)
 					return null;
 				return '"' + value.toString() + '"';
 			}
 
 			public URI toValue(String string, AbstractNode node) throws ValueConverterException {
 				if(string == null)
 					return null;
				// if(Strings.isEmpty(string))
				// throw new ValueConverterException("Can not convert empty string to URI", node, null);
 				try {
 					int truncate = string.startsWith("\"") && string.endsWith("\"")
 							? 1
 							: 0;
 					string = Strings.convertFromJavaString(string.substring(truncate, string.length() - truncate), true);
 
 					return new URI(string);
 				}
 				catch(URISyntaxException e) {
 					throw new ValueConverterException(
 						"Value'" + string + "' is not a valid URI :" + e.getMessage(), node, null);
 				}
 			}
 
 		};
 	}
 
 	@ValueConverter(rule = "VersionLiteral")
 	public IValueConverter<Version> Version() {
 		return new IValueConverter<Version>() {
 
 			public String toString(Version value) {
 				return value.toString();
 			}
 
 			public Version toValue(String string, AbstractNode node) throws ValueConverterException {
 				if(Strings.isEmpty(string))
 					throw new ValueConverterException("Can not convert empty string to Version", node, null);
 				try {
 					char c = string.charAt(0);
 					if(c == '"' || c == '\"')
 						string = Strings.convertFromJavaString(string.substring(1, string.length() - 1), true);
 
 					return Version.create(string);
 				}
 				catch(IllegalArgumentException e) {
 					throw new ValueConverterException("Version '" + string + "' is not a valid version: " +
 							e.getMessage(), node, null);
 				}
 			}
 
 		};
 	}
 
 	@ValueConverter(rule = "VersionRangeLiteral")
 	public IValueConverter<VersionRange> VersionRange() {
 		return new IValueConverter<VersionRange>() {
 
 			public String toString(VersionRange value) {
 				return value.toString();
 			}
 
 			public VersionRange toValue(String string, AbstractNode node) throws ValueConverterException {
 				if(Strings.isEmpty(string))
 					throw new ValueConverterException("Can not convert empty string to VersionRange", node, null);
 				try {
 					char c = string.charAt(0);
 					if(c == '"' || c == '\"')
 						string = Strings.convertFromJavaString(string.substring(1, string.length() - 1), true);
 
 					return new VersionRange(string);
 				}
 				catch(IllegalArgumentException e) {
 					throw new ValueConverterException("VersionRange '" + string + "' is not a valid range: " +
 							e.getMessage(), node, null);
 				}
 			}
 
 		};
 	}
 }
