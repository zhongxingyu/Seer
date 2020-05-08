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
 
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.io.FilenameUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiImage;
 import org.springframework.util.StringUtils;
 
 /**
  *
  */
 public class LinkUtil {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(LinkUtil.class.getName());
 	private static Pattern TOPIC_LINK_PATTERN = null;
 
 	static {
 		try {
 			TOPIC_LINK_PATTERN = Pattern.compile("(((([^\\n\\r\\?\\#\\:]+)\\:)?([^\\n\\r\\?\\#]+))?(#([^\\n\\r\\?]+))?)+(\\?([^\\n\\r]+))?");
 		} catch (Exception e) {
 			logger.severe("Unable to compile pattern", e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static String buildEditLinkUrl(String context, String virtualWiki, String topic, String query, int section) throws Exception {
 		if (StringUtils.hasText(query)) {
 			if (!query.startsWith("?")) query = "?" + query;
 			query += "&amp;topic=" + Utilities.encodeForURL(topic);
 		} else {
 			query = "?topic=" + Utilities.encodeForURL(topic);
 		}
 		if (section > 0) {
 			query += "&amp;section=" + section;
 		}
 		WikiLink wikiLink = new WikiLink();
 		// FIXME - hard coding
 		wikiLink.setDestination("Special:Edit");
 		wikiLink.setQuery(query);
 		return LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
 	}
 
 	/**
 	 *
 	 */
 	public static String buildImageLinkHtml(String context, String virtualWiki, String topicName, boolean frame, boolean thumb, String align, String caption, int maxDimension, boolean suppressLink, String style, boolean escapeHtml) throws Exception {
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		WikiFile wikiFile = WikiBase.getHandler().lookupWikiFile(virtualWiki, topicName);
 		if (topic.getTopicType() == Topic.TYPE_FILE) {
 			// file, not an image
 			if (!StringUtils.hasText(caption)) {
 				caption = topicName.substring(WikiBase.NAMESPACE_IMAGE.length() + 1);
 			}
 			String url = FilenameUtils.normalize(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) + "/" + wikiFile.getUrl());
 			url = FilenameUtils.separatorsToUnix(url);
 			return "<a href=\"" + url + "\">" + Utilities.escapeHTML(caption) + "</a>";
 		}
 		String html = "";
 		WikiImage wikiImage = ImageUtil.initializeImage(wikiFile, maxDimension);
 		if (caption == null) caption = "";
 		if (frame || thumb || StringUtils.hasText(align)) {
 			html += "<div class=\"";
 			if (thumb || frame) {
 				html += "imgthumb ";
 			}
 			if (align != null && align.equalsIgnoreCase("left")) {
 				html += "imgleft ";
 			} else if (align != null && align.equalsIgnoreCase("center")) {
 				html += "imgcenter ";
 			} else {
 				// default right alignment
 				html += "imgright ";
 			}
 			html = html.trim() + "\">";
 		}
 		if (wikiImage.getWidth() > 0) {
 			html += "<div style=\"width:" + (wikiImage.getWidth() + 2) + "px\">";
 		}
 		if (!suppressLink) html += "<a class=\"wikiimg\" href=\"" + LinkUtil.buildInternalLinkUrl(context, virtualWiki, topicName) + "\">";
 		if (!StringUtils.hasText(style)) style = "wikiimg";
 		html += "<img class=\"" + style + "\" src=\"";
 		String url = new File(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH), wikiFile.getUrl()).getPath();
 		url = FilenameUtils.separatorsToUnix(url);
 		html += url;
 		html += "\"";
 		html += " width=\"" + wikiImage.getWidth() + "\"";
 		html += " height=\"" + wikiImage.getHeight() + "\"";
 		html += " alt=\"" + Utilities.escapeHTML(caption) + "\"";
 		html += " />";
 		if (!suppressLink) html += "</a>";
 		if (StringUtils.hasText(caption)) {
 			html += "<div class=\"imgcaption\">";
 			if (escapeHtml) {
 				html += Utilities.escapeHTML(caption);
 			} else {
 				html += caption;
 			}
 			html += "</div>";
 		}
 		if (wikiImage.getWidth() > 0) {
 			html += "</div>";
 		}
 		if (frame || thumb || StringUtils.hasText(align)) {
 			html += "</div>";
 		}
 		return html;
 	}
 
 	/**
 	 *
 	 */
 	public static String buildInternalLinkHtml(String context, String virtualWiki, WikiLink wikiLink, String text, String style, boolean escapeHtml) throws Exception {
 		String url = LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
 		String topic = wikiLink.getDestination();
 		if (!StringUtils.hasText(text)) text = topic;
 		if (StringUtils.hasText(topic) && !WikiBase.exists(virtualWiki, topic) && !StringUtils.hasText(style)) {
 			style = "edit";
 		}
 		if (StringUtils.hasText(style)) {
 			style = " class=\"" + style + "\"";
 		} else {
 			style = "";
 		}
 		String html = "<a title=\"" + Utilities.escapeHTML(text) + "\" href=\"" + url + "\"" + style + ">";
 		if (escapeHtml) {
 			html += Utilities.escapeHTML(text);
 		} else {
 			html += text;
 		}
 		html += "</a>";
 		return html;
 	}
 
 	/**
 	 *
 	 */
 	public static String buildInternalLinkUrl(String context, String virtualWiki, String topic) throws Exception {
 		if (!StringUtils.hasText(topic)) {
 			return null;
 		}
 		WikiLink wikiLink = LinkUtil.parseWikiLink(topic);
 		return LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
 	}
 
 	/**
 	 *
 	 */
 	public static String buildInternalLinkUrl(String context, String virtualWiki, WikiLink wikiLink) throws Exception {
 		String topic = wikiLink.getDestination();
 		String section = wikiLink.getSection();
 		String query = wikiLink.getQuery();
 		if (!StringUtils.hasText(topic) && StringUtils.hasText(section)) {
 			String url = "";
 			return "#" + Utilities.encodeForURL(section);
 		}
 		if (!WikiBase.exists(virtualWiki, topic)) {
 			return LinkUtil.buildEditLinkUrl(context, virtualWiki, topic, query, -1);
 		}
 		String url = context;
 		// context never ends with a "/" per servlet specification
 		url += "/";
 		// get the virtual wiki, which should have been set by the parent servlet
 		url += Utilities.encodeForURL(virtualWiki);
 		url += "/";
 		url += Utilities.encodeForURL(topic);
 		if (StringUtils.hasText(section)) {
			url += "#" + Utilities.encodeForURL(section);
 		}
 		if (StringUtils.hasText(query)) {
			url += "?" + query;
 		}
 		return url;
 	}
 
 	/**
 	 * Parse a topic name of the form "Topic#Section?Query", and return a WikiLink
 	 * object representing the link.
 	 *
 	 * @param raw The raw topic link text.
 	 * @return A WikiLink object that represents the link.
 	 */
 	public static WikiLink parseWikiLink(String raw) {
 		WikiLink wikiLink = new WikiLink();
 		if (!StringUtils.hasText(raw)) {
 			return new WikiLink();
 		}
 		raw = raw.trim();
 		Matcher m = TOPIC_LINK_PATTERN.matcher(raw);
 		if (!m.matches()) {
 			return new WikiLink();
 		}
 		wikiLink.setNamespace(m.group(4));
 		wikiLink.setDestination(m.group(2));
 		wikiLink.setSection(m.group(7));
 		wikiLink.setQuery(m.group(9));
 		return wikiLink;
 	}
 }
