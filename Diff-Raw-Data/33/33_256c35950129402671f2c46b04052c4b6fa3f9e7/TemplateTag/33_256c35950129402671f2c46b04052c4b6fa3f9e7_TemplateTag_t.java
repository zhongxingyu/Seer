 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.parser.jflex;
 
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Topic;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserDocument;
 import org.jamwiki.parser.ParserTag;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 
 /**
  *
  */
 public class TemplateTag implements ParserTag {
 
 	private static WikiLogger logger = WikiLogger.getLogger(TemplateTag.class.getName());
 	private static Pattern PARAM_NAME_PATTERN = null;
 	private Hashtable parameterValues = new Hashtable();
 
 	static {
 		try {
 			PARAM_NAME_PATTERN = Pattern.compile("(([^\\[\\{\\=]+)=)(.*)");
 		} catch (Exception e) {
 			logger.severe("Unable to compile pattern", e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private String applyParameter(ParserInput parserInput, String param) throws Exception {
 		if (this.parameterValues == null) return param;
 		String name = this.parseParamName(param);
 		String defaultValue = this.parseParamDefaultValue(parserInput, param);
 		String value = (String)this.parameterValues.get(name);
 		if (value == null && defaultValue == null) return param;
 		return (value != null) ? value : defaultValue;
 	}
 
 	/**
 	 * Search through content, starting at a specific position, and search for the
 	 * first position after a matching end tag for a specified start tag.  For instance,
 	 * if called with a start tag of "<b>" and an end tag of "</b>", this method
 	 * will operate as follows:
 	 *
 	 * "01<b>567</b>23" returns 12.
 	 * "01<b>56<b>01</b>67</b>23" returns 22.
 	 */
 	private int findMatchingEndTag(String content, int start, String startToken, String endToken) {
 		int pos = start;
 		int count = 0;
 		String substring = "";
 		while (pos < content.length()) {
 			substring = content.substring(pos);
 			if (substring.startsWith(startToken)) {
 				count++;
 				pos += startToken.length();
 			} else if (substring.startsWith(endToken)) {
 				count--;
 				pos += endToken.length();
 			} else {
 				pos++;
 			}
 			if (count == 0) return pos;
 		}
 		return -1;
 	}
 
 	/**
 	 * Parse a call to a Mediawiki template of the form "{{template|param1|param2}}"
 	 * and return the resulting template output.
 	 */
 	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception {
 		// extract the template name
 		String name = this.parseTemplateName(raw);
 		// get the parsed template body
 		Topic templateTopic = WikiBase.getHandler().lookupTopic(parserInput.getVirtualWiki(), name, false);
 		this.processTemplateMetadata(parserInput, parserDocument, templateTopic, raw);
 		// make sure template was not redirected
 		if (templateTopic != null && templateTopic.getTopicType() == Topic.TYPE_REDIRECT) {
 			templateTopic = Utilities.findRedirectedTopic(templateTopic, 0);
			name = templateTopic.getName();
 		}
 		if (templateTopic != null && templateTopic.getTopicType() == Topic.TYPE_REDIRECT) {
 			// redirection target does not exist
 			templateTopic = null;
 		}
 		if (mode < JFlexParser.MODE_TEMPLATE) {
 			return raw;
 		}
		return this.processTemplateContent(parserInput, parserDocument, templateTopic, raw, name);
 	}
 
 	/**
 	 *
 	 */
 	private String parseParamDefaultValue(ParserInput parserInput, String raw) throws Exception {
 		int pos = raw.indexOf("|");
 		String defaultValue = null;
 		if (pos == -1) {
 			return null;
 		}
 		if (pos + 1 >= raw.length() - "}}}".length()) {
 			return null;
 		}
 		defaultValue = raw.substring(pos + 1, raw.length() - "}}}".length());
 		return ParserUtil.parseFragment(parserInput, defaultValue, JFlexParser.MODE_TEMPLATE);
 	}
 
 	/**
 	 *
 	 */
 	private String parseParamName(String raw) throws Exception {
 		int pos = raw.indexOf("|");
 		String name = null;
 		if (pos != -1) {
 			name = raw.substring("{{{".length(), pos);
 		} else {
 			name = raw.substring("{{{".length(), raw.length() - "}}}".length());
 		}
 		name = name.trim();
 		if (!StringUtils.hasText(name)) {
 			// FIXME - no need for an exception
 			throw new Exception("No parameter name specified");
 		}
 		return name;
 	}
 
 	/**
 	 *
 	 */
 	private String parseTemplateBody(ParserInput parserInput, String content) throws Exception {
 		StringBuffer output = new StringBuffer();
 		int pos = 0;
 		while (pos < content.length()) {
 			String substring = content.substring(pos);
 			if (substring.startsWith("{{{")) {
 				// template
 				int endPos = findMatchingEndTag(content, pos, "{{{", "}}}");
 				if (endPos != -1) {
 					String param = content.substring(pos, endPos);
 					output.append(this.applyParameter(parserInput, param));
 				}
 				pos = endPos;
 			} else {
 				output.append(content.charAt(pos));
 				pos++;
 			}
 		}
 		return ParserUtil.parseFragment(parserInput, output.toString(), JFlexParser.MODE_TEMPLATE);
 	}
 
 	/**
 	 *
 	 */
	private String processTemplateContent(ParserInput parserInput, ParserDocument parserDocument, Topic templateTopic, String raw, String name) throws Exception {
 		if (templateTopic == null) {
 			return "[[" + name + "]]";
 		}
 		// set template parameter values
 		this.parseTemplateParameterValues(parserInput, raw);
 		return this.parseTemplateBody(parserInput, templateTopic.getTopicContent());
 	}
 
 	/**
 	 *
 	 */
 	private void processTemplateMetadata(ParserInput parserInput, ParserDocument parserDocument, Topic templateTopic, String raw) throws Exception {
 		String name = (templateTopic != null) ? templateTopic.getName() : this.parseTemplateName(raw);
 		parserDocument.addLink(name);
 	}
 
 	/**
 	 *
 	 */
 	private String parseTemplateName(String raw) throws Exception {
 		if (!StringUtils.hasText(raw)) {
 			throw new Exception("Empty template text");
 		}
 		if (!raw.startsWith("{{") || !raw.endsWith("}}")) {
 			throw new Exception ("Invalid template text: " + raw);
 		}
 		int pos = raw.indexOf("|");
 		String name = null;
 		if (pos != -1) {
 			name = raw.substring("{{".length(), pos);
 		} else {
 			name = raw.substring("{{".length(), raw.length() - "}}".length());
 		}
 		name = name.trim();
 		if (!StringUtils.hasText(name)) {
 			// FIXME - no need for an exception
 			throw new Exception("No template name specified");
 		}
 		if (!name.startsWith(WikiBase.NAMESPACE_TEMPLATE + WikiBase.NAMESPACE_SEPARATOR)) {
 			name = WikiBase.NAMESPACE_TEMPLATE + WikiBase.NAMESPACE_SEPARATOR + name;
 		}
 		return name;
 	}
 
 	/**
 	 *
 	 */
 	private void parseTemplateParameterValues(ParserInput parserInput, String raw) throws Exception {
 		String content = "";
 		content = raw.substring("{{".length(), raw.length() - "}}".length());
 		// strip the template name
 		int pos = content.indexOf("|");
 		if (pos == -1) return;
 		pos++;
 		Matcher nameMatcher = null;
 		int endPos = -1;
 		int count = 1;
 		String substring = "";
 		String name = "";
 		String value = "";
 		while (pos < content.length()) {
 			substring = content.substring(pos);
 			if (!StringUtils.hasText(name)) {
 				nameMatcher = PARAM_NAME_PATTERN.matcher(substring);
 				if (nameMatcher.matches()) {
 					name = nameMatcher.group(2);
 					pos += nameMatcher.group(1).length();
 					continue;
 				} else {
 					name = new Integer(count).toString();
 				}
 			}
 			endPos = -1;
 			if (substring.startsWith("{{{")) {
 				// template parameter
 				endPos = findMatchingEndTag(content, pos, "{{{", "}}}");
 			} else if (substring.startsWith("{{")) {
 				// template
 				endPos = findMatchingEndTag(content, pos, "{{", "}}");
 			} else if (substring.startsWith("[[")) {
 				// link
 				endPos = findMatchingEndTag(content, pos, "[[", "]]");
 			} else if (substring.startsWith("{|")) {
 				// table
 				endPos = findMatchingEndTag(content, pos, "{|", "|}");
 			} else if (content.charAt(pos) == '|') {
 				// new parameter
 				value = ParserUtil.parseFragment(parserInput, value, JFlexParser.MODE_TEMPLATE);
 				this.parameterValues.put(name, value);
 				name = "";
 				value = "";
 				pos++;
 				count++;
 				continue;
 			}
 			if (endPos != -1) {
 				value += content.substring(pos, endPos);
 				pos = endPos;
 			} else {
 				value += content.charAt(pos);
 				pos++;
 			}
 		}
 		if (StringUtils.hasText(name)) {
 			// add the last one
 			value = ParserUtil.parseFragment(parserInput, value, JFlexParser.MODE_TEMPLATE);
 			this.parameterValues.put(name, value);
 		}
 	}
 }
