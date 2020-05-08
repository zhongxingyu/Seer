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
 
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class parses signature tags of the form <code>~~~</code>,
  * <code>~~~~</code> and <code>~~~~~</code>.
  */
 public class WikiSignatureTag implements JFlexParserTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiSignatureTag.class.getName());
 
 	/**
 	 *
 	 */
 	private String buildWikiSignature(JFlexLexer lexer, boolean includeUser, boolean includeDate) throws ParserException {
 		String signature = "";
 		if (includeUser) {
 			signature = this.retrieveUserSignature(lexer.getParserInput());
			// parse signature as link in order to store link metadata
			WikiLinkTag wikiLinkTag = new WikiLinkTag();
			wikiLinkTag.parse(lexer, signature);
 			if (lexer.getMode() != JFlexParser.MODE_MINIMAL) {
 				try {
 					signature = JFlexParserUtil.parseFragment(lexer.getParserInput(), lexer.getParserOutput(), signature, lexer.getMode());
 				} catch (ParserException e) {
 					logger.error("Failure while building wiki signature", e);
 					// FIXME - return empty or a failure indicator?
 					return "";
 				}
 			}
 		}
 		if (includeUser && includeDate) {
 			signature += " ";
 		}
 		if (includeDate) {
 			SimpleDateFormat format = new SimpleDateFormat();
 			format.applyPattern(Environment.getDatePatternValue(Environment.PROP_PARSER_SIGNATURE_DATE_PATTERN, true, true));
 			signature += format.format(new java.util.Date());
 		}
 		return signature;
 	}
 
 	/**
 	 * Parse a Mediawiki signature of the form "~~~~" and return the resulting
 	 * HTML output.
 	 */
 	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
 		if (raw.equals("~~~")) {
 			return this.buildWikiSignature(lexer, true, false);
 		}
 		if (raw.equals("~~~~")) {
 			return this.buildWikiSignature(lexer, true, true);
 		}
 		if (raw.equals("~~~~~")) {
 			return this.buildWikiSignature(lexer, false, true);
 		}
 		return raw;
 	}
 
 	/**
 	 *
 	 */
 	private String retrieveUserSignature(ParserInput parserInput) {
 		WikiUser user = parserInput.getWikiUser();
 		if (user != null && !StringUtils.isBlank(user.getSignature())) {
 			return user.getSignature();
 		}
 		String login = parserInput.getUserDisplay();
 		String email = parserInput.getUserDisplay();
 		String displayName = parserInput.getUserDisplay();
 		String userId = "-1";
 		if (user != null && !StringUtils.isBlank(user.getUsername())) {
 			login = user.getUsername();
 			displayName = (!StringUtils.isBlank(user.getDisplayName())) ? user.getDisplayName() : login;
 			email = user.getEmail();
 			userId = Integer.toString(user.getUserId());
 		}
 		if (login == null || displayName == null) {
 			logger.info("Signature tagged parsed without user information available, returning empty");
 			return "";
 		}
 		MessageFormat formatter = new MessageFormat(Environment.getValue(Environment.PROP_PARSER_SIGNATURE_USER_PATTERN));
 		Object params[] = new Object[7];
 		params[0] = Namespace.namespace(Namespace.USER_ID).getLabel(parserInput.getVirtualWiki()) + Namespace.SEPARATOR + login;
 		// FIXME - hard coding
 		params[1] = Namespace.namespace(Namespace.SPECIAL_ID).getLabel(parserInput.getVirtualWiki()) + Namespace.SEPARATOR + "Contributions?contributor=" + login;
 		params[2] = Namespace.namespace(Namespace.USER_COMMENTS_ID).getLabel(parserInput.getVirtualWiki()) + Namespace.SEPARATOR + login;
 		params[3] = login;
 		params[4] = displayName;
 		params[5] = email!=null ? email : "";
 		params[6] = userId;
 		return formatter.format(params);
 	}
 }
