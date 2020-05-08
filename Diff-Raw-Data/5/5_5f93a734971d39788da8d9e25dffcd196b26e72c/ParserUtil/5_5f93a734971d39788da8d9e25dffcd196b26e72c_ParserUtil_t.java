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
 package org.jamwiki.parser;
 
 import java.io.StringReader;
 import java.text.SimpleDateFormat;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 
 /**
  * Utility methods used with the Mediawiki lexers.
  */
 public class ParserUtil {
 
 	private static Logger logger = Logger.getLogger(ParserUtil.class.getName());
 	private static Pattern TAG_PATTERN = null;
 	private static Pattern JAVASCRIPT_PATTERN1 = null;
 	private static Pattern JAVASCRIPT_PATTERN2 = null;
 	private static Pattern IMAGE_SIZE_PATTERN = null;
 	// FIXME - make configurable
 	private static final int DEFAULT_THUMBNAIL_SIZE = 180;
 
 	static {
 		try {
 			TAG_PATTERN = Pattern.compile("<[ ]*([^\\ />]+)([ ]*(.*?))([/]?[ ]*>)");
 			// catch script insertions of the form "onsubmit="
 			JAVASCRIPT_PATTERN1 = Pattern.compile("( on[^=]{3,}=)+", Pattern.CASE_INSENSITIVE);
 			// catch script insertions that use a javascript url
 			JAVASCRIPT_PATTERN2 = Pattern.compile("(javascript[ ]*\\:)+", Pattern.CASE_INSENSITIVE);
 			// look for image size info in image tags
 			IMAGE_SIZE_PATTERN = Pattern.compile("([0-9]+)[ ]*px", Pattern.CASE_INSENSITIVE);
 		} catch (Exception e) {
 			logger.error("Unable to compile pattern", e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static String buildEditLinkUrl(ParserInput parserInput, int section) {
 		if (!parserInput.getAllowSectionEdit()) return "";
 		String output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[";
 		String url = "";
 		try {
 			url = LinkUtil.buildEditLinkUrl(parserInput.getContext(), parserInput.getVirtualWiki(), parserInput.getTopicName(), null, section);
 		} catch (Exception e) {
 			logger.error("Failure while building link for topic " + parserInput.getVirtualWiki() + " / " + parserInput.getTopicName(), e);
 		}
 		output += "<a href=\"" + url + "\">";
 		output += Utilities.getMessage("common.sectionedit", parserInput.getLocale());
 		output += "</a>]</div>";
 		return output;
 	}
 
 	/**
 	 * Given a String that represents a Wiki HTML link (a URL with an optional
 	 * link text that is enclosed in brackets), return a formatted HTML anchor tag.
 	 *
 	 * @param raw The raw Wiki syntax that is to be converted into an HTML link.
 	 * @return A formatted HTML link for the Wiki syntax.
 	 */
 	protected static String buildHtmlLink(String raw) {
 		if (raw == null || raw.length() <= 2) {
 			// no link, display the raw text
 			return raw;
 		}
 		// strip the first and last brackets
 		String link = raw.substring(1, raw.length() - 1).trim();
 		return buildHtmlLinkRaw(link);
 	}
 
 	/**
 	 * Given a String that represents a raw HTML link (a URL link that is
 	 * not enclosed in brackets), return a formatted HTML anchor tag.
 	 *
 	 * @param raw The raw HTML link that is to be converted into an HTML link.
 	 * @return A formatted HTML link.
 	 */
 	protected static String buildHtmlLinkRaw(String raw) {
 		if (raw == null) return raw;
 		String link = raw.trim();
 		if (link.length() <= 0) {
 			// no link to display
 			return raw;
 		}
 		// search for link text (space followed by text)
 		String punctuation = Utilities.extractTrailingPunctuation(link);
 		String text = "";
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
 			text = link;
 		}
 		String html = linkHtml(link, text, punctuation);
 		return (html != null) ? html : raw;
 	}
 
 	/**
 	 *
 	 */
 	protected static String buildInternalLinkUrl(ParserInput parserInput, String raw) {
 		String context = parserInput.getContext();
 		String virtualWiki = parserInput.getVirtualWiki();
 		try {
 			String content = ParserUtil.extractLinkContent(raw);
 			if (!StringUtils.hasText(content)) {
 				// invalid link
 				return raw;
 			}
 			String url = ParserUtil.extractLinkUrl(content);
 			String topic = LinkUtil.extractLinkTopic(url);
 			String section = LinkUtil.extractLinkSection(url);
 			String query = LinkUtil.extractLinkQuery(url);
 			if (!StringUtils.hasText(topic) && !StringUtils.hasText(section)) {
 				// invalid topic
 				return raw;
 			}
 			if (topic.startsWith(WikiBase.NAMESPACE_IMAGE)) {
 				// parse as an image
 				return ParserUtil.parseImageLink(parserInput, content);
 			}
 			if (topic.startsWith(":") && StringUtils.countOccurrencesOf(topic, ":") >= 2) {
 				// see if this is a virtual wiki
 				int pos = topic.indexOf(":", 1);
 				String tmp = topic.substring(1, pos);
 				if (WikiBase.getHandler().lookupVirtualWiki(tmp) != null && topic.length() > pos) {
 					virtualWiki = tmp;
 					topic = topic.substring(pos + 1);
 				}
 			}
 			if (topic.startsWith(":") && topic.length() > 1) {
 				// strip opening colon
 				topic = topic.substring(1);
 			}
 			String text = ParserUtil.extractLinkText(content);
 			if (!StringUtils.hasText(text) && StringUtils.hasText(topic)) {
 				text = topic;
 				if (StringUtils.hasText(section)) {
					text += "#" + Utilities.decodeURL(section);
 				}
 			} else if (!StringUtils.hasText(text) && StringUtils.hasText(section)) {
				text = Utilities.decodeURL(section);
 			} else {
 				text = ParserUtil.parseFragment(parserInput, text);
 			}
 			// do not escape text html - already done by parser
 			return LinkUtil.buildInternalLinkHtml(context, virtualWiki, topic, section, query, text, null, false);
 		} catch (Exception e) {
 			logger.error("Failure while parsing link " + raw, e);
 			return "";
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static String buildWikiSignature(ParserInput parserInput, boolean includeUser, boolean includeDate) {
 		try {
 			String signature = "";
 			if (includeUser) {
 				String context = parserInput.getContext();
 				String virtualWiki = parserInput.getVirtualWiki();
 				// FIXME - need a utility method for user links
 				String topic = WikiBase.NAMESPACE_USER + parserInput.getUserIpAddress();
 				String text = parserInput.getUserIpAddress();
 				if (parserInput.getWikiUser() != null) {
 					WikiUser user = parserInput.getWikiUser();
 					topic = WikiBase.NAMESPACE_USER + user.getLogin();
 					text = (user.getDisplayName() != null) ? user.getDisplayName() : user.getLogin();
 				}
 				String link = "";
 				if (parserInput.getMode() == ParserInput.MODE_SAVE) {
 					// FIXME - mediawiki specific.
 					link = "[[" + topic + "|" + text + "]]";
 				} else {
 					link += LinkUtil.buildInternalLinkHtml(context, virtualWiki, topic, text, null, true);
 				}
 				signature += link;
 			}
 			if (includeUser && includeDate) {
 				signature += " ";
 			}
 			if (includeDate) {
 				SimpleDateFormat format = new SimpleDateFormat();
 				format.applyPattern("dd-MMM-yyyy HH:mm zzz");
 				signature += format.format(new java.util.Date());
 			}
 			return signature;
 		} catch (Exception e) {
 			logger.error("Failure while building wiki signature", e);
 			// FIXME - return empty or a failure indicator?
 			return "";
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static String extractLinkContent(String raw) {
 		if (raw == null || raw.length() <= 4 || !raw.startsWith("[[") || !raw.endsWith("]]")) {
 			logger.warn("ParserUtil.extractLinkContent called with invalid raw text: " + raw);
 			return null;
 		}
 		// strip the first and last brackets
 		String content = raw.substring(2, raw.length() - 2).trim();
 		if (!StringUtils.hasText(content)) {
 			// empty brackets, no topic to display
 			return null;
 		}
 		return content.trim();
 	}
 
 	/**
 	 *
 	 */
 	protected static String extractLinkText(String raw) {
 		if (raw == null) {
 			logger.warn("ParserUtil.extractLinkText called with invalid raw text: " + raw);
 			return null;
 		}
 		// search for topic text ("|" followed by text)
 		int pos = raw.indexOf('|');
 		if (pos <= 0 || raw.length() <= (pos + 1)) {
 			return null;
 		}
 		return raw.substring(pos+1).trim();
 	}
 
 	/**
 	 *
 	 */
 	protected static String extractLinkUrl(String raw) {
 		if (raw == null) {
 			logger.warn("ParserUtil.extractLinkTopic called with invalid raw text: " + raw);
 			return null;
 		}
 		String url = raw;
 		int pos = url.indexOf("|");
 		if (pos == 0) {
 			// topic cannot start with "|"
 			return null;
 		}
 		if (pos != -1) {
 			url = url.substring(0, pos);
 		}
 		return url;
 	}
 
 	/**
 	 *
 	 */
 	protected static String linkHtml(String link, String text, String punctuation) {
 		String html = null;
 		// in case of script attack, replace script tags (cannot use escapeHTML due
 		// to the possibility of ampersands in the link)
 		link = StringUtils.replace(link, "<", "&lt;");
 		link = StringUtils.replace(link, ">", "&gt;");
 		link = StringUtils.replace(link, "\"", "&quot;");
 		link = StringUtils.replace(link, "'", "&apos;");
 		text = Utilities.escapeHTML(text);
 		String linkLower = link.toLowerCase();
 		if (linkLower.startsWith("mailto://")) {
 			// fix bad mailto syntax
 			link = "mailto:" + link.substring("mailto://".length());
 		}
 		if (linkLower.startsWith("http://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if  (linkLower.startsWith("https://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("ftp://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("mailto:")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("news://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("telnet://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("file://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		}
 		return html;
 	}
 
 	/**
 	 *
 	 */
 	protected static String parseImageLink(ParserInput parserInput, String content) throws Exception {
 		String context = parserInput.getContext();
 		String virtualWiki = parserInput.getVirtualWiki();
 		String url = ParserUtil.extractLinkUrl(content);
 		String topic = LinkUtil.extractLinkTopic(url);
 		String text = ParserUtil.extractLinkText(content);
 		boolean thumb = false;
 		boolean frame = false;
 		String caption = null;
 		String align = null;
 		int maxDimension = -1;
 		if (text != null) {
 			StringTokenizer tokens = new StringTokenizer(text, "|");
 			while (tokens.hasMoreTokens()) {
 				String token = tokens.nextToken();
 				if (!StringUtils.hasText(token)) continue;
 				if (token.equalsIgnoreCase("noframe")) {
 					frame = false;
 				} else if (token.equalsIgnoreCase("frame")) {
 					frame = true;
 				} else if (token.equalsIgnoreCase("thumb")) {
 					thumb = true;
 				} else if (token.equalsIgnoreCase("right")) {
 					align = "right";
 				} else if (token.equalsIgnoreCase("left")) {
 					align = "left";
 				} else if (token.equalsIgnoreCase("center")) {
 					align = "center";
 				} else {
 					Matcher m = IMAGE_SIZE_PATTERN.matcher(token);
 					if (m.find()) {
 						maxDimension = new Integer(m.group(1)).intValue();
 					} else {
 						// FIXME - this is a hack.  images may contain piped links, so if
 						// there was previous caption info append the new info.
 						if (!StringUtils.hasText(caption)) {
 							caption = token;
 						} else {
 							caption += "|" + token;
 						}
 					}
 				}
 			}
 			if (thumb && maxDimension <= 0) {
 				maxDimension = DEFAULT_THUMBNAIL_SIZE;
 			}
 			caption = ParserUtil.parseFragment(parserInput, caption);
 		}
 		// do not escape html for caption since parser does it above
 		return LinkUtil.buildImageLinkHtml(context, virtualWiki, topic, frame, thumb, align, caption, maxDimension, false, null, false);
 	}
 
 	/**
 	 * Provide a way to run the pre-processor against a fragment of text, such
 	 * as an image caption.  This method should be used sparingly since it is
 	 * not very efficient.
 	 */
 	protected static String parseFragment(ParserInput parserInput, String fragment) throws Exception {
 		// FIXME - consider yypushstream() and yypopstream() as potentially more efficient
 		// ways to handle this functionality
 		if (!StringUtils.hasText(fragment)) return fragment;
 		JAMWikiParser parser = new JAMWikiParser(parserInput);
 		StringReader raw = new StringReader(fragment);
 		ParserOutput parserOutput = parser.parsePreProcess(raw);
 		return parserOutput.getContent();
 	}
 
 	/**
 	 * Clean up HTML tags to make them XHTML compliant (lowercase, no
 	 * unnecessary spaces).
 	 */
 	protected static String sanitizeHtmlTag(String tag) {
 		tag = StringUtils.deleteAny(tag, " ").toLowerCase();
 		if (tag.endsWith("/>")) {
 			// spaces were stripped, so make sure tag is of the form "<br />"
 			tag = tag.substring(0, tag.length() - 2) + " />";
 		}
 		return tag;
 	}
 
 	/**
 	 * Strip Wiki markup from text
 	 */
 	protected static String stripMarkup(String text) {
 		// FIXME - this could be a bit more thorough and also strip HTML
 		text = StringUtils.delete(text, "'''");
 		text = StringUtils.delete(text, "''");
 		text = StringUtils.delete(text, "[[");
 		text = StringUtils.delete(text, "]]");
 		return text;
 	}
 
 	/**
 	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
 	 * a bad thing, so clean up HTML tags to remove any such attributes.
 	 */
 	protected static String validateHtmlTag(String tag) {
 		Matcher m = TAG_PATTERN.matcher(tag);
 		if (!m.find()) {
 			logger.error("Failure while attempting to match html tag for pattern " + tag);
 			return tag;
 		}
 		String tagOpen = m.group(1);
 		String attributes = m.group(2);
 		String tagClose = m.group(4);
 		attributes = ParserUtil.validateHtmlTagAttributes(attributes);
 		tag = "<" + tagOpen.toLowerCase().trim();
 		tag += attributes;
 		if (!attributes.endsWith(" ")) tag += " ";
 		if (tagClose.indexOf("/") != -1) {
 			tagClose = "/>";
 		}
 		tag += tagClose.trim();
 		return tag;
 	}
 
 	/**
 	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
 	 * a bad thing, so clean up HTML tags to remove any such attributes.
 	 */
 	protected static String validateHtmlTagAttributes(String attributes) {
 		if (!StringUtils.hasText(attributes)) return attributes;
 		if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT)) {
 			// FIXME - can these two patterns be combined into one?
 			// pattern requires a space prior to the "onFoo", so make sure one exists
 			Matcher m = JAVASCRIPT_PATTERN1.matcher(" " + attributes);
 			if (m.find()) {
 				logger.warn("Attempt to include Javascript in Wiki syntax " + attributes);
 				return "";
 			}
 			m = JAVASCRIPT_PATTERN2.matcher(attributes);
 			if (m.find()) {
 				logger.warn("Attempt to include Javascript in Wiki syntax " + attributes);
 				return "";
 			}
 		}
 		return attributes;
 	}
 }
