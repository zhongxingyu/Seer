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
 
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class provides the capability for parsing HTML links of the form
  * <code>[http://example.com optional text]</code> as well as raw links
  * of the form <code>http://example.com</code>.
  */
 public class HtmlLinkTag implements JFlexParserTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(HtmlLinkTag.class.getName());
 	// temporary parameter passed to indicate that the fragment being parsed is a link caption
 	private static final String HTML_LINK_CAPTION = "html-link-caption";
 
 	/**
 	 * Given a String that represents a raw HTML link (a URL link that is
 	 * not enclosed in brackets), return a formatted HTML anchor tag.
 	 *
 	 * @param raw The raw HTML link that is to be converted into an HTML link.
 	 * @return A formatted HTML link.
 	 */
 	private String buildHtmlLinkRaw(ParserInput parserInput, int mode, String raw) throws ParserException {
 		String link = raw.trim();
 		// search for link text (space followed by text)
 		String punctuation = this.extractTrailingPunctuation(link);
 		String text = null;
 		int pos = link.indexOf(' ');
 		if (pos == -1) {
 			pos = link.indexOf('\t');
 		}
 		if (pos > 0) {
 			text = link.substring(pos+1).trim();
 			link = link.substring(0, pos).trim();
 			punctuation = "";
 		} else {
 			link = link.substring(0, link.length() - punctuation.length()).trim();
 		}
 		String html = this.linkHtml(parserInput, mode, link, text, punctuation);
 		return (html == null) ? raw : html;
 	}
 
 	/**
 	 * Returns any trailing period, comma, semicolon, or colon characters
 	 * from the given string.  This method is useful when parsing raw HTML
 	 * links, in which case trailing punctuation must be removed.  Note that
 	 * only punctuation that is not previously matched is trimmed - if the
 	 * input is "http://example.com/page_(page)" then the trailing parantheses
 	 * will not be trimmed.
 	 *
 	 * @param text The text from which trailing punctuation should be returned.
 	 * @return Any trailing punctuation from the given text, or an empty string
 	 *  otherwise.
 	 */
 	private String extractTrailingPunctuation(String text) {
 		if (StringUtils.isBlank(text)) {
 			return "";
 		}
 		StringBuilder buffer = new StringBuilder();
 		for (int i = text.length() - 1; i >= 0; i--) {
 			char c = text.charAt(i);
 			if (c == '.' || c == ';' || c == ',' || c == ':' || c == '(' || c == '[' || c == '{') {
 				buffer.append(c);
 				continue;
 			}
 			// if the value ends with ), ] or } then strip it UNLESS there is a matching
 			// opening tag
 			if (c == ')' || c == ']' || c == '}') {
 				String closeChar = String.valueOf(c);
 				String openChar = (c == ')') ? "(" : ((c == ']') ? "[" : "{");
 				int pos = Utilities.findMatchingStartTag(text, i, openChar, closeChar);
 				if (pos == -1) {
 					buffer.append(c);
 					continue;
 				}
 			}
 			break;
 		}
 		if (buffer.length() == 0) {
 			return "";
 		}
 		buffer = buffer.reverse();
 		return buffer.toString();
 	}
 
 	/**
 	 *
 	 */
 	private String linkHtml(ParserInput parserInput, int mode, String link, String text, String punctuation) throws ParserException {
 		if (link.toLowerCase().startsWith("mailto://")) {
 			// fix bad mailto syntax
 			link = "mailto:" + link.substring("mailto://".length());
 		}
 		String caption = link;
 		if (!StringUtils.isBlank(text)) {
 			// pass a parameter via the parserInput to prevent nested links from being generated
			parserInput.getTempParams().put(HTML_LINK_CAPTION, "true");
 			caption = JFlexParserUtil.parseFragment(parserInput, text, mode);
 			parserInput.getTempParams().remove(HTML_LINK_CAPTION);
 		}
 		String openTag = LinkUtil.buildHtmlLinkOpenTag(link, "externallink");
 		return openTag + caption + "</a>" + punctuation;
 	}
 
 	/**
 	 * Parse a Mediawiki HTML link of the form "[http://www.site.com/ text]" or
 	 * "http://www.site.com/" and return the resulting HTML output.
 	 */
 	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
 		if (logger.isFinerEnabled()) {
 			logger.finer("htmllink: " + raw + " (" + lexer.yystate() + ")");
 		}
 		if (raw == null || StringUtils.isBlank(raw)) {
 			// no link to display
 			return raw;
 		}
 		Boolean linkCaption = (Boolean)lexer.getParserInput().getTempParams().get(WikiLinkTag.LINK_CAPTION);
 		Boolean htmlLinkCaption = (Boolean)lexer.getParserInput().getTempParams().get(HTML_LINK_CAPTION);
 		if ((linkCaption != null && linkCaption.booleanValue()) || (htmlLinkCaption != null && htmlLinkCaption.booleanValue())) {
 			// do not parse HTML tags in link captions as that would result in HTML of the form
 			// "<a href="">this is the <a href="">link caption</a></a>"
 			return raw;
 		}
 		return this.buildHtmlLinkRaw(lexer.getParserInput(), lexer.getMode(), raw);
 	}
 }
