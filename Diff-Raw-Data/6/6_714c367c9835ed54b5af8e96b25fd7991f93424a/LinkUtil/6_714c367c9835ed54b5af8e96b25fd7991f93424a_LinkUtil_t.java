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
 package org.jamwiki.utils;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.parser.ParserException;
 
 /**
  * General utility methods for handling both wiki topic links and HTML links.
  * Wiki topic links are generally of the form "Topic?query=param#Section".
  * HTML links are of the form http://example.com/.
  */
 public class LinkUtil {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(LinkUtil.class.getName());
 
 	/**
 	 *
 	 */
 	private LinkUtil() {
 	}
 
 	/**
 	 * Build a query parameter.  If root is empty, this method returns
 	 * "?param=value".  If root is not empty this method returns root +
 	 * "&amp;param=value".  Note that param and value will be  URL encoded,
 	 * and if "query" does not start with a "?" then one will be pre-pended.
 	 *
 	 * @param query The existing query parameter, if one is available.  If the
 	 *  query parameter does not start with "?" then one will be pre-pended.
 	 * @param param The name of the query parameter being appended.  This
 	 *  value will be URL encoded.
 	 * @param value The value of the query parameter being appended.  This
 	 *  value will be URL encoded.
 	 * @return The full query string generated using the input parameters.
 	 */
 	public static String appendQueryParam(String query, String param, String value) {
 		String url = "";
 		if (!StringUtils.isBlank(query)) {
 			if (query.charAt(0) != '?') {
 				query = "?" + query;
 			}
 			url = query + "&amp;";
 		} else {
 			url = "?";
 		}
 		if (StringUtils.isBlank(param)) {
 			return query;
 		}
 		url += Utilities.encodeAndEscapeTopicName(param) + "=";
 		if (!StringUtils.isBlank(value)) {
 			url += Utilities.encodeAndEscapeTopicName(value);
 		}
 		return url;
 	}
 
 	/**
 	 * Utility method for building a URL link to a wiki edit page for a
 	 * specified topic.
 	 *
 	 * @param context The servlet context for the link that is being created.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param topic The name of the topic for which an edit link is being
 	 *  created.
 	 * @param query Any existing query parameters to append to the edit link.
 	 *  This value may be either <code>null</code> or empty.
 	 * @param section The section defined by the name parameter within the
 	 *  HTML page for the topic being edited.  If provided then the edit link
 	 *  will allow editing of only the specified section.
 	 * @return A url that links to the edit page for the specified topic.
 	 *  Note that this method returns only the URL, not a fully-formed HTML
 	 *  anchor tag.
 	 * @throws DataAccessException Thrown if any error occurs while builing the link URL.
 	 */
 	public static String buildEditLinkUrl(String context, String virtualWiki, String topic, String query, int section) throws DataAccessException {
 		query = LinkUtil.appendQueryParam(query, "topic", topic);
 		if (section > 0) {
 			query += "&amp;section=" + section;
 		}
 		WikiLink wikiLink = new WikiLink();
 		// FIXME - hard coding
 		wikiLink.setDestination("Special:Edit");
 		wikiLink.setQuery(query);
 		return LinkUtil.buildTopicUrl(context, virtualWiki, wikiLink);
 	}
 
 	/**
 	 * Parse a link of the form http://example.com and return the opening tag of the
 	 * form <a href="http://example.com">.
 	 */
 	public static String buildHtmlLinkOpenTag(String link, String cssClass) throws ParserException {
 		String linkLower = link.toLowerCase();
 		if (linkLower.startsWith("mailto://")) {
 			// fix bad mailto syntax
 			link = "mailto:" + link.substring("mailto://".length());
 		}
 		String protocol = "";
 		if (linkLower.startsWith("http://")) {
 			protocol = "http://";
 		} else if  (linkLower.startsWith("https://")) {
 			protocol = "https://";
 		} else if (linkLower.startsWith("ftp://")) {
 			protocol = "ftp://";
 		} else if (linkLower.startsWith("mailto:")) {
 			protocol = "mailto:";
 		} else if (linkLower.startsWith("news://")) {
 			protocol = "news://";
 		} else if (linkLower.startsWith("telnet://")) {
 			protocol = "telnet://";
 		} else if (linkLower.startsWith("file://")) {
 			protocol = "file://";
 		} else {
 			throw new ParserException("Invalid protocol in link " + link);
 		}
 		link = link.substring(protocol.length());
 		// make sure link values are properly escaped.
 		link = StringUtils.replace(link, "<", "%3C");
 		link = StringUtils.replace(link, ">", "%3E");
 		link = StringUtils.replace(link, "\"", "%22");
 		link = StringUtils.replace(link, "\'", "%27");
 		String target = (Environment.getBooleanValue(Environment.PROP_EXTERNAL_LINK_NEW_WINDOW)) ? " target=\"_blank\"" : "";
 		if (cssClass == null) {
 			cssClass = "externallink";
 		}
 		String html = "<a class=\"" + cssClass + "\" rel=\"nofollow\"";
 		html += " href=\"" + protocol + link + "\"" + target + ">";
 		return html;
 	}
 
 	/**
 	 * Build the HTML anchor link to a topic page for a given WikLink object.
 	 *
 	 * @param context The servlet context for the link that is being created.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param wikiLink The WikiLink object containing all relevant information
 	 *  about the link being generated.
 	 * @param text The text to display as the link content.
 	 * @param style The CSS class to use with the anchor HTML tag.  This value
 	 *  can be <code>null</code> or empty if no custom style is used.
 	 * @param target The anchor link target, or <code>null</code> or empty if
 	 *  no target is needed.
 	 * @param escapeHtml Set to <code>true</code> if the link caption should
 	 *  be HTML escaped.  This value should be <code>true</code> in any case
 	 *  where the caption is not guaranteed to be free from potentially
 	 *  malicious HTML code.
 	 * @return An HTML anchor link that matches the given input parameters.
 	 * @throws DataAccessException Thrown if any error occurs while retrieving
 	 *  topic information.
 	 */
 	public static String buildInternalLinkHtml(String context, String virtualWiki, WikiLink wikiLink, String text, String style, String target, boolean escapeHtml) throws DataAccessException {
 		String url = LinkUtil.buildTopicUrl(context, virtualWiki, wikiLink);
 		String topic = wikiLink.getDestination();
 		if (StringUtils.isBlank(text)) {
 			text = topic;
 		}
 		if (!wikiLink.getNamespace().getId().equals(Namespace.MEDIA_ID) && !StringUtils.isBlank(topic) && StringUtils.isBlank(style)) {
 			if (InterWikiHandler.isInterWiki(virtualWiki)) {
 				style = "interwiki";
 			} else if (!LinkUtil.isExistingArticle(virtualWiki, topic)) {
 				style = "edit";
 			}
 		}
 		if (!StringUtils.isBlank(style)) {
 			style = " class=\"" + style + "\"";
 		} else {
 			style = "";
 		}
 		if (!StringUtils.isBlank(target)) {
 			target = " target=\"" + target + "\"";
 		} else {
 			target = "";
 		}
 		if (StringUtils.isBlank(topic) && !StringUtils.isBlank(wikiLink.getSection())) {
 			topic = wikiLink.getSection();
 		}
 		StringBuilder html = new StringBuilder();
 		html.append("<a href=\"").append(url).append('\"').append(style);
 		html.append(" title=\"").append(StringEscapeUtils.escapeHtml(topic)).append('\"').append(target).append('>');
 		if (escapeHtml) {
 			html.append(StringEscapeUtils.escapeHtml(text));
 		} else {
 			html.append(text);
 		}
 		html.append("</a>");
 		return html.toString();
 	}
 
 	/**
 	 * Build a URL to the topic page for a given topic.
 	 *
 	 * @param context The servlet context path.  If this value is
 	 *  <code>null</code> then the resulting URL will NOT include context path,
 	 *  which breaks HTML links but is useful for servlet redirection URLs.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param topic The topic name for the URL that is being generated.
 	 * @param validateTopic Set to <code>true</code> if the topic must exist and
 	 *  must not be a "Special:" page.  If the topic does not exist then a link to
 	 *  an edit page will be returned.
 	 * @throws DataAccessException Thrown if any error occurs while retrieving topic
 	 *  information.
 	 */
 	public static String buildTopicUrl(String context, String virtualWiki, String topic, boolean validateTopic) throws DataAccessException {
 		if (StringUtils.isBlank(topic)) {
 			return null;
 		}
 		WikiLink wikiLink = LinkUtil.parseWikiLink(virtualWiki, topic);
 		if (validateTopic) {
 			return LinkUtil.buildTopicUrl(context, virtualWiki, wikiLink);
 		} else {
 			return LinkUtil.buildTopicUrlNoEdit(context, virtualWiki, wikiLink.getDestination(), wikiLink.getSection(), wikiLink.getQuery());
 		}
 	}
 
 	/**
 	 * Build a URL to the topic page for a given topic.
 	 *
 	 * @param context The servlet context path.  If this value is
 	 *  <code>null</code> then the resulting URL will NOT include context path,
 	 *  which breaks HTML links but is useful for servlet redirection URLs.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param wikiLink The WikiLink object containing all relevant information
 	 *  about the link being generated.
 	 * @throws DataAccessException Thrown if any error occurs while retrieving topic
 	 *  information.
 	 */
 	public static String buildTopicUrl(String context, String virtualWiki, WikiLink wikiLink) throws DataAccessException {
 		String url = null;
 		if (wikiLink.getNamespace().getId().equals(Namespace.MEDIA_ID)) {
 			// for the "Media:" namespace, link directly to the file
 			String filename = Namespace.namespace(Namespace.FILE_ID).getLabel(virtualWiki) + Namespace.SEPARATOR + wikiLink.getArticle();
 			url = ImageUtil.buildImageFileUrl(context, virtualWiki, filename);
 			if (url == null) {
 				url = LinkUtil.buildTopicUrlNoEdit(context, virtualWiki, "Special:Upload", null, "?topic=" + filename);
 			}
 		} else {
 			String topic = wikiLink.getDestination();
 			String section = wikiLink.getSection();
 			String query = wikiLink.getQuery();
 			url = LinkUtil.buildTopicUrlNoEdit(context, virtualWiki, topic, section, query);
 			if (StringUtils.isBlank(topic) && !StringUtils.isBlank(section)) {
 				// do not check existence for section links
 				return url;
 			}
 			if (!LinkUtil.isExistingArticle(virtualWiki, topic)) {
 				url = LinkUtil.buildEditLinkUrl(context, virtualWiki, topic, query, -1);
 			}
 		}
 		return url;
 	}
 
 	/**
 	 * Build a URL to the topic page for a given topic.  This method does NOT verify
 	 * if the topic exists or if it is a "Special:" page, simply returning the URL
 	 * for the topic and virtual wiki.
 	 *
 	 * @param context The servlet context path.  If this value is
 	 *  <code>null</code> then the resulting URL will NOT include context path,
 	 *  which breaks HTML links but is useful for servlet redirection URLs.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param topicName The name of the topic for which a link is being built.
 	 * @param section The section of the page (#section) for which a link is
 	 *  being built.
 	 * @param queryString Query string parameters to append to the link.
 	 * @throws Exception Thrown if any error occurs while builing the link URL.
 	 */
 	private static String buildTopicUrlNoEdit(String context, String virtualWiki, String topicName, String section, String queryString) {
 		if (StringUtils.isBlank(topicName) && !StringUtils.isBlank(section)) {
 			return "#" + Utilities.encodeAndEscapeTopicName(section);
 		}
 		StringBuilder url = new StringBuilder();
 		if (context != null) {
 			url.append(context);
 		}
 		// context never ends with a "/" per servlet specification
 		url.append('/');
 		// get the virtual wiki, which should have been set by the parent servlet
 		url.append(Utilities.encodeAndEscapeTopicName(virtualWiki));
 		url.append('/');
 		url.append(Utilities.encodeAndEscapeTopicName(topicName));
 		if (!StringUtils.isBlank(queryString)) {
 			if (queryString.charAt(0) != '?') {
 				url.append('?');
 			}
 			url.append(queryString);
 		}
 		if (!StringUtils.isBlank(section)) {
 			if (section.charAt(0) != '#') {
 				url.append('#');
 			}
 			url.append(Utilities.encodeAndEscapeTopicName(section));
 		}
 		return url.toString();
 	}
 
 	/**
 	 * Generate the HTML for an interwiki anchor link.
 	 *
 	 * @param wikiLink The WikiLink object containing all relevant information
 	 *  about the link being generated.
 	 * @return The HTML anchor tag for the interwiki link.
 	 */
 	public static String interWiki(WikiLink wikiLink) {
 		String url = InterWikiHandler.formatInterWiki(wikiLink.getInterWiki(), wikiLink.getDestination());
 		String text = (!StringUtils.isBlank(wikiLink.getText())) ? wikiLink.getText() : wikiLink.getDestination();
 		return "<a class=\"interwiki\" title=\"" + text + "\" href=\"" + url + "\">" + text + "</a>";
 	}
 
 	/**
 	 * Utility method for determining if an article name corresponds to a valid
 	 * wiki link.  In this case an "article name" could be an existing topic, a
 	 * "Special:" page, a user page, an interwiki link, etc.  This method will
 	 * return true if the given name corresponds to a valid special page, user
 	 * page, topic, or other existing article.
 	 *
 	 * @param virtualWiki The virtual wiki for the topic being checked.
 	 * @param articleName The name of the article that is being checked.
 	 * @return <code>true</code> if there is an article that exists for the given
 	 *  name and virtual wiki.
 	 * @throws DataAccessException Thrown if an error occurs during lookup.
 	 */
 	public static boolean isExistingArticle(String virtualWiki, String articleName) throws DataAccessException {
 		if (StringUtils.isBlank(virtualWiki) || StringUtils.isBlank(articleName)) {
 			return false;
 		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(virtualWiki, articleName);
		if (PseudoTopicHandler.isPseudoTopic(wikiLink.getDestination())) {
 			return true;
 		}
		if (wikiLink.getInterWiki() != null) {
 			return true;
 		}
 		if (StringUtils.isBlank(Environment.getValue(Environment.PROP_BASE_FILE_DIR)) || !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
 			// not initialized yet
 			return false;
 		}
 		return (WikiBase.getDataHandler().lookupTopicId(virtualWiki, articleName) != null);
 	}
 
 	/**
 	 *
 	 */
 	private static int prefixPosition(String topicName) {
 		int prefixPosition = topicName.indexOf(Namespace.SEPARATOR, 1);
 		// if a match is found and it's not the last character of the name, it's a prefix.
 		return (prefixPosition != -1 && (prefixPosition + 1) < topicName.length()) ? prefixPosition : -1;
 	}
 
 	/**
 	 * Make sure a URL does not contain any extraneous characters such as "//" in
 	 * places where it should not.
 	 *
 	 * @param url The URL to be normalized.
 	 * @return The normalized URL.
 	 */
 	public static String normalize(String url) {
 		if (StringUtils.isBlank(url)) {
 			return url;
 		}
 		// first find the protocol
 		int pos = url.indexOf("://");
 		if (pos == -1 || pos == (url.length() - 1)) {
 			return url;
 		}
 		String protocol = url.substring(0, pos + "://".length());
 		String remainder = url.substring(protocol.length());
 		return protocol + StringUtils.replace(remainder, "//", "/");
 	}
 
 	/**
 	 * Parse a wiki topic link and return a <code>WikiLink</code> object
 	 * representing the link.  Wiki topic links are of the form "Topic?Query#Section".
 	 *
 	 * @param virtualWiki The current virtual wiki.
 	 * @param raw The raw topic link text.
 	 * @return A WikiLink object that represents the link.
 	 */
 	public static WikiLink parseWikiLink(String virtualWiki, String raw) {
 		// note that this functionality was previously handled with a regular
 		// expression, but the expression caused CPU usage to spike to 100%
 		// with topics such as "Urnordisch oder Nordwestgermanisch?"
 		String processed = raw.trim();
 		WikiLink wikiLink = new WikiLink();
 		if (wikiLink.getNamespace() == null) {
 			throw new IllegalStateException("Unable to determine namespace for topic.  This error generally indicates a configuration or database issue.  Check the logs for additional information.");
 		}
 		if (StringUtils.isBlank(processed)) {
 			return new WikiLink();
 		}
 		// first look for a section param - "#..."
 		int sectionPos = processed.indexOf('#');
 		if (sectionPos != -1 && sectionPos < processed.length()) {
 			String sectionString = processed.substring(sectionPos + 1);
 			wikiLink.setSection(sectionString);
 			if (sectionPos == 0) {
 				// link is of the form #section, no more to process
 				return wikiLink;
 			}
 			processed = processed.substring(0, sectionPos);
 		}
 		// now see if the link ends with a query param - "?..."
 		int queryPos = processed.indexOf('?', 1);
 		if (queryPos != -1 && queryPos < processed.length()) {
 			String queryString = processed.substring(queryPos + 1);
 			wikiLink.setQuery(queryString);
 			processed = processed.substring(0, queryPos);
 		}
 		// search for a namespace or virtual wiki
 		String topic = LinkUtil.processVirtualWiki(processed, wikiLink);
 		if (wikiLink.getVirtualWiki() != null) {
 			// strip the virtual wiki
 			processed = topic;
 			virtualWiki = wikiLink.getVirtualWiki().getName();
 		}
 		wikiLink.setText(processed);
 		topic = LinkUtil.processNamespace(virtualWiki, topic, wikiLink);
 		if (!wikiLink.getNamespace().getId().equals(Namespace.MAIN_ID)) {
 			// store the display name WITH any extra spaces
 			wikiLink.setText(processed);
 			// update original text in case topic was of the form "xxx: topic"
 			processed = wikiLink.getNamespace().getLabel(virtualWiki) + Namespace.SEPARATOR + topic;
 		}
 		// if no namespace or virtual wiki, see if there's an interwiki link
 		if (wikiLink.getNamespace().getId().equals(Namespace.MAIN_ID) && wikiLink.getVirtualWiki() == null) {
 			topic = LinkUtil.processInterWiki(processed, wikiLink);
 			if (wikiLink.getInterWiki() != null) {
 				// strip the interwiki
 				processed = topic;
 				wikiLink.setText(processed);
 			}
 		}
 		if (wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
 			// captions are handled differently for images, so clear the link text value.
 			wikiLink.setText(null);
 		} else if (!StringUtils.isBlank(wikiLink.getSection())) {
 			wikiLink.setText(wikiLink.getText() + "#" + wikiLink.getSection());
 		}
 		wikiLink.setArticle(Utilities.decodeTopicName(topic, true));
 		// destination is namespace + topic
 		wikiLink.setDestination(Utilities.decodeTopicName(processed, true));
 		return wikiLink;
 	}
 
 	/**
 	 *
 	 */
 	private static String processInterWiki(String processed, WikiLink wikiLink) {
 		int prefixPosition = LinkUtil.prefixPosition(processed);
 		if (prefixPosition == -1) {
 			return processed;
 		}
 		String linkPrefix = processed.substring(0, prefixPosition).trim();
 		if (InterWikiHandler.isInterWiki(linkPrefix)) {
 			wikiLink.setInterWiki(linkPrefix);
 		}
 		return (wikiLink.getInterWiki() != null) ? processed.substring(prefixPosition + Namespace.SEPARATOR.length()).trim(): processed;
 	}
 
 	/**
 	 *
 	 */
 	private static String processVirtualWiki(String processed, WikiLink wikiLink) {
 		int prefixPosition = LinkUtil.prefixPosition(processed);
 		if (prefixPosition == -1) {
 			return processed;
 		}
 		String linkPrefix = processed.substring(0, prefixPosition).trim();
 		try {
 			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(linkPrefix);
 			if (virtualWiki != null) {
 				wikiLink.setVirtualWiki(virtualWiki);
 			}
 		} catch (DataAccessException e) {
 			// this should not happen, if it does then swallow the error
 			logger.warning("Failure while trying to lookup virtual wiki: " + linkPrefix, e);
 		}
 		return (wikiLink.getVirtualWiki() != null) ? processed.substring(prefixPosition + Namespace.SEPARATOR.length()).trim(): processed;
 	}
 
 	/**
 	 *
 	 */
 	private static String processNamespace(String virtualWiki, String processed, WikiLink wikiLink) {
 		int prefixPosition = LinkUtil.prefixPosition(processed);
 		if (prefixPosition == -1) {
 			return processed;
 		}
 		String linkPrefix = processed.substring(0, prefixPosition).trim();
 		try {
 			Namespace namespace = WikiBase.getDataHandler().lookupNamespace(virtualWiki, linkPrefix);
 			if (namespace != null) {
 				wikiLink.setNamespace(namespace);
 			}
 		} catch (DataAccessException e) {
 			// this should not happen, if it does then throw a runtime exception
 			throw new IllegalStateException("Failure while trying to lookup namespace: " + linkPrefix, e);
 		}
 		return (!wikiLink.getNamespace().getId().equals(Namespace.MAIN_ID)) ? processed.substring(prefixPosition + Namespace.SEPARATOR.length()).trim(): processed;
 	}
 }
