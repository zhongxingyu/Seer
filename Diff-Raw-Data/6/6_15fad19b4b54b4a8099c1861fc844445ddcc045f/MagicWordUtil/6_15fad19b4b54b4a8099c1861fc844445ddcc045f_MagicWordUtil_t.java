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
 
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 
 /**
  * Process magic words.  See http://www.mediawiki.org/wiki/Help:Magic_words
  */
 public class MagicWordUtil {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(MagicWordUtil.class.getName());
 	// current date values
 	private static final String MAGIC_CURRENT_DAY = "CURRENTDAY";
 	private static final String MAGIC_CURRENT_DAY2 = "CURRENTDAY2";
 	private static final String MAGIC_CURRENT_DAY_NAME = "CURRENTDAYNAME";
 	private static final String MAGIC_CURRENT_DAY_OF_WEEK = "CURRENTDOW";
 	private static final String MAGIC_CURRENT_MONTH = "CURRENTMONTH";
 	private static final String MAGIC_CURRENT_MONTH_ABBR = "CURRENTMONTHABBREV";
 	private static final String MAGIC_CURRENT_MONTH_NAME = "CURRENTMONTHNAME";
 	private static final String MAGIC_CURRENT_TIME = "CURRENTTIME";
 	private static final String MAGIC_CURRENT_HOUR = "CURRENTHOUR";
 	private static final String MAGIC_CURRENT_WEEK = "CURRENTWEEK";
 	private static final String MAGIC_CURRENT_YEAR = "CURRENTYEAR";
 	private static final String MAGIC_CURRENT_TIMESTAMP = "CURRENTTIMESTAMP";
 	// local date values
 	private static final String MAGIC_LOCAL_DAY = "LOCALDAY";
 	private static final String MAGIC_LOCAL_DAY2 = "LOCALDAY2";
 	private static final String MAGIC_LOCAL_DAY_NAME = "LOCALDAYNAME";
 	private static final String MAGIC_LOCAL_DAY_OF_WEEK = "LOCALDOW";
 	private static final String MAGIC_LOCAL_MONTH = "LOCALMONTH";
 	private static final String MAGIC_LOCAL_MONTH_ABBR = "LOCALMONTHABBREV";
 	private static final String MAGIC_LOCAL_MONTH_NAME = "LOCALMONTHNAME";
 	private static final String MAGIC_LOCAL_TIME = "LOCALTIME";
 	private static final String MAGIC_LOCAL_HOUR = "LOCALHOUR";
 	private static final String MAGIC_LOCAL_WEEK = "LOCALWEEK";
 	private static final String MAGIC_LOCAL_YEAR = "LOCALYEAR";
 	private static final String MAGIC_LOCAL_TIMESTAMP = "LOCALTIMESTAMP";
 	// statistics
 	private static final String MAGIC_CURRENT_VERSION = "CURRENTVERSION";
 	private static final String MAGIC_NUMBER_ARTICLES = "NUMBEROFARTICLES";
 	private static final String MAGIC_NUMBER_ARTICLES_R = "NUMBEROFARTICLES:R";
 	private static final String MAGIC_NUMBER_PAGES = "NUMBEROFPAGES";
 	private static final String MAGIC_NUMBER_PAGES_R = "NUMBEROFPAGES:R";
 	private static final String MAGIC_NUMBER_FILES = "NUMBEROFFILES";
 	private static final String MAGIC_NUMBER_FILES_R = "NUMBEROFFILES:R";
 	private static final String MAGIC_NUMBER_USERS = "NUMBEROFUSERS";
 	private static final String MAGIC_NUMBER_USERS_R = "NUMBEROFUSERS:R";
 	private static final String MAGIC_NUMBER_ADMINS = "NUMBEROFADMINS";
 	private static final String MAGIC_NUMBER_ADMINS_R = "NUMBEROFADMINS:R";
 	private static final String MAGIC_PAGES_IN_NAMESPACE = "PAGESINNAMESPACE";
 	private static final String MAGIC_PAGES_IN_NAMESPACE_NS = "PAGESINNS:ns";
 	private static final String MAGIC_PAGES_IN_NAMESPACE_NS_R = "PAGESINNS:ns:R";
 	// page values
 	private static final String MAGIC_PAGE_NAME = "PAGENAME";
 	private static final String MAGIC_PAGE_NAME_E = "PAGENAMEE";
 	private static final String MAGIC_SUB_PAGE_NAME = "SUBPAGENAME";
 	private static final String MAGIC_SUB_PAGE_NAME_E = "SUBPAGENAMEE";
 	private static final String MAGIC_BASE_PAGE_NAME = "BASEPAGENAME";
 	private static final String MAGIC_BASE_PAGE_NAME_E = "BASEPAGENAMEE";
 	private static final String MAGIC_NAMESPACE = "NAMESPACE";
 	private static final String MAGIC_NAMESPACE_E = "NAMESPACEE";
 	private static final String MAGIC_FULL_PAGE_NAME = "FULLPAGENAME";
 	private static final String MAGIC_FULL_PAGE_NAME_E = "FULLPAGENAMEE";
 	private static final String MAGIC_TALK_SPACE = "TALKSPACE";
 	private static final String MAGIC_TALK_SPACE_E = "TALKSPACEE";
 	private static final String MAGIC_SUBJECT_SPACE = "SUBJECTSPACE";
 	private static final String MAGIC_SUBJECT_SPACE_E = "SUBJECTSPACEE";
 	private static final String MAGIC_ARTICLE_SPACE = "ARTICLESPACE";
 	private static final String MAGIC_ARTICLE_SPACE_E = "ARTICLESPACEE";
 	private static final String MAGIC_TALK_PAGE_NAME = "TALKPAGENAME";
 	private static final String MAGIC_TALK_PAGE_NAME_E = "TALKPAGENAMEE";
 	private static final String MAGIC_SUBJECT_PAGE_NAME = "SUBJECTPAGENAME";
 	private static final String MAGIC_SUBJECT_PAGE_NAME_E = "SUBJECTPAGENAMEE";
 	private static final String MAGIC_ARTICLE_PAGE_NAME = "ARTICLEPAGENAME";
 	private static final String MAGIC_ARTICLE_PAGE_NAME_E = "ARTICLEPAGENAMEE";
 	private static final String MAGIC_REVISION_ID = "REVISIONID";
 	private static final String MAGIC_REVISION_DAY = "REVISIONDAY";
 	private static final String MAGIC_REVISION_DAY2 = "REVISIONDAY2";
 	private static final String MAGIC_REVISION_MONTH = "REVISIONMONTH";
 	private static final String MAGIC_REVISION_YEAR = "REVISIONYEAR";
 	private static final String MAGIC_REVISION_TIMESTAMP = "REVISIONTIMESTAMP";
 	private static final String MAGIC_REVISION_USER = "REVISIONUSER";
 	private static final String MAGIC_SITE_NAME = "SITENAME";
 	private static final String MAGIC_SERVER = "SERVER";
 	private static final String MAGIC_SCRIPT_PATH = "SCRIPTPATH";
 	private static final String MAGIC_SERVER_NAME = "SERVERNAME";
 	private static List<String> MAGIC_WORDS = new ArrayList<String>();
 
 	static {
 		// current date values
 		MAGIC_WORDS.add(MAGIC_CURRENT_DAY);
 		MAGIC_WORDS.add(MAGIC_CURRENT_DAY2);
 		MAGIC_WORDS.add(MAGIC_CURRENT_DAY_NAME);
 		MAGIC_WORDS.add(MAGIC_CURRENT_DAY_OF_WEEK);
 		MAGIC_WORDS.add(MAGIC_CURRENT_MONTH);
 		MAGIC_WORDS.add(MAGIC_CURRENT_MONTH_ABBR);
 		MAGIC_WORDS.add(MAGIC_CURRENT_MONTH_NAME);
 		MAGIC_WORDS.add(MAGIC_CURRENT_TIME);
 		MAGIC_WORDS.add(MAGIC_CURRENT_HOUR);
 		MAGIC_WORDS.add(MAGIC_CURRENT_WEEK);
 		MAGIC_WORDS.add(MAGIC_CURRENT_YEAR);
 		MAGIC_WORDS.add(MAGIC_CURRENT_TIMESTAMP);
 		// local date values
 		MAGIC_WORDS.add(MAGIC_LOCAL_DAY);
 		MAGIC_WORDS.add(MAGIC_LOCAL_DAY2);
 		MAGIC_WORDS.add(MAGIC_LOCAL_DAY_NAME);
 		MAGIC_WORDS.add(MAGIC_LOCAL_DAY_OF_WEEK);
 		MAGIC_WORDS.add(MAGIC_LOCAL_MONTH);
 		MAGIC_WORDS.add(MAGIC_LOCAL_MONTH_ABBR);
 		MAGIC_WORDS.add(MAGIC_LOCAL_MONTH_NAME);
 		MAGIC_WORDS.add(MAGIC_LOCAL_TIME);
 		MAGIC_WORDS.add(MAGIC_LOCAL_HOUR);
 		MAGIC_WORDS.add(MAGIC_LOCAL_WEEK);
 		MAGIC_WORDS.add(MAGIC_LOCAL_YEAR);
 		MAGIC_WORDS.add(MAGIC_LOCAL_TIMESTAMP);
 		// statistics
 		MAGIC_WORDS.add(MAGIC_CURRENT_VERSION);
 		MAGIC_WORDS.add(MAGIC_NUMBER_ARTICLES);
 		MAGIC_WORDS.add(MAGIC_NUMBER_ARTICLES_R);
 		MAGIC_WORDS.add(MAGIC_NUMBER_PAGES);
 		MAGIC_WORDS.add(MAGIC_NUMBER_PAGES_R);
 		MAGIC_WORDS.add(MAGIC_NUMBER_FILES);
 		MAGIC_WORDS.add(MAGIC_NUMBER_FILES_R);
 		MAGIC_WORDS.add(MAGIC_NUMBER_USERS);
 		MAGIC_WORDS.add(MAGIC_NUMBER_USERS_R);
 		MAGIC_WORDS.add(MAGIC_NUMBER_ADMINS);
 		MAGIC_WORDS.add(MAGIC_NUMBER_ADMINS_R);
 		MAGIC_WORDS.add(MAGIC_PAGES_IN_NAMESPACE);
 		MAGIC_WORDS.add(MAGIC_PAGES_IN_NAMESPACE_NS);
 		MAGIC_WORDS.add(MAGIC_PAGES_IN_NAMESPACE_NS_R);
 		// page values
 		MAGIC_WORDS.add(MAGIC_PAGE_NAME);
 		MAGIC_WORDS.add(MAGIC_PAGE_NAME_E);
 		MAGIC_WORDS.add(MAGIC_SUB_PAGE_NAME);
 		MAGIC_WORDS.add(MAGIC_SUB_PAGE_NAME_E);
 		MAGIC_WORDS.add(MAGIC_BASE_PAGE_NAME);
 		MAGIC_WORDS.add(MAGIC_BASE_PAGE_NAME_E);
 		MAGIC_WORDS.add(MAGIC_NAMESPACE);
 		MAGIC_WORDS.add(MAGIC_NAMESPACE_E);
 		MAGIC_WORDS.add(MAGIC_FULL_PAGE_NAME);
 		MAGIC_WORDS.add(MAGIC_FULL_PAGE_NAME_E);
 		MAGIC_WORDS.add(MAGIC_TALK_SPACE);
 		MAGIC_WORDS.add(MAGIC_TALK_SPACE_E);
 		MAGIC_WORDS.add(MAGIC_SUBJECT_SPACE);
 		MAGIC_WORDS.add(MAGIC_SUBJECT_SPACE_E);
 		MAGIC_WORDS.add(MAGIC_ARTICLE_SPACE);
 		MAGIC_WORDS.add(MAGIC_ARTICLE_SPACE_E);
 		MAGIC_WORDS.add(MAGIC_TALK_PAGE_NAME);
 		MAGIC_WORDS.add(MAGIC_TALK_PAGE_NAME_E);
 		MAGIC_WORDS.add(MAGIC_SUBJECT_PAGE_NAME);
 		MAGIC_WORDS.add(MAGIC_SUBJECT_PAGE_NAME_E);
 		MAGIC_WORDS.add(MAGIC_ARTICLE_PAGE_NAME);
 		MAGIC_WORDS.add(MAGIC_ARTICLE_PAGE_NAME_E);
 		MAGIC_WORDS.add(MAGIC_REVISION_ID);
 		MAGIC_WORDS.add(MAGIC_REVISION_DAY);
 		MAGIC_WORDS.add(MAGIC_REVISION_DAY2);
 		MAGIC_WORDS.add(MAGIC_REVISION_MONTH);
 		MAGIC_WORDS.add(MAGIC_REVISION_YEAR);
 		MAGIC_WORDS.add(MAGIC_REVISION_TIMESTAMP);
 		MAGIC_WORDS.add(MAGIC_REVISION_USER);
 		MAGIC_WORDS.add(MAGIC_SITE_NAME);
 		MAGIC_WORDS.add(MAGIC_SERVER);
 		MAGIC_WORDS.add(MAGIC_SCRIPT_PATH);
 		MAGIC_WORDS.add(MAGIC_SERVER_NAME);
 	}
 
 	/**
 	 * Determine if a template name corresponds to a magic word requiring
 	 * special handling.  See http://meta.wikimedia.org/wiki/Help:Magic_words
 	 * for a list of Mediawiki magic words.
 	 */
 	protected static boolean isMagicWord(String name) {
 		return MAGIC_WORDS.contains(name);
 	}
 
 	/**
 	 * Process a magic word, returning the value corresponding to the magic
 	 * word value.  See http://meta.wikimedia.org/wiki/Help:Magic_words for a
 	 * list of Mediawiki magic words.
 	 */
 	protected static String processMagicWord(ParserInput parserInput, String name) throws DataAccessException {
 		SimpleDateFormat formatter = new SimpleDateFormat();
 		TimeZone utc = TimeZone.getTimeZone("GMT+00");
 		Date current = new Date(System.currentTimeMillis());
 		// local date values
 		if (name.equals(MAGIC_LOCAL_DAY)) {
 			formatter.applyPattern("d");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_DAY2)) {
 			formatter.applyPattern("dd");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_DAY_NAME)) {
 			formatter.applyPattern("EEEE");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_DAY_OF_WEEK)) {
 			formatter.applyPattern("F");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_MONTH)) {
 			formatter.applyPattern("MM");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_MONTH_ABBR)) {
 			formatter.applyPattern("MMM");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_MONTH_NAME)) {
 			formatter.applyPattern("MMMM");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_TIME)) {
 			formatter.applyPattern("HH:mm");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_HOUR)) {
 			formatter.applyPattern("HH");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_WEEK)) {
 			formatter.applyPattern("w");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_YEAR)) {
 			formatter.applyPattern("yyyy");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_LOCAL_TIMESTAMP)) {
 			formatter.applyPattern("yyyyMMddHHmmss");
 			return formatter.format(current);
 		}
 		// current date values
 		formatter.setTimeZone(utc);
 		if (name.equals(MAGIC_CURRENT_DAY)) {
 			formatter.applyPattern("d");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_DAY2)) {
 			formatter.applyPattern("dd");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_DAY_NAME)) {
 			formatter.applyPattern("EEEE");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_DAY_OF_WEEK)) {
 			formatter.applyPattern("F");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_MONTH)) {
 			formatter.applyPattern("MM");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_MONTH_ABBR)) {
 			formatter.applyPattern("MMM");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_MONTH_NAME)) {
 			formatter.applyPattern("MMMM");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_TIME)) {
 			formatter.applyPattern("HH:mm");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_HOUR)) {
 			formatter.applyPattern("HH");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_WEEK)) {
 			formatter.applyPattern("w");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_YEAR)) {
 			formatter.applyPattern("yyyy");
 			return formatter.format(current);
 		}
 		if (name.equals(MAGIC_CURRENT_TIMESTAMP)) {
 			formatter.applyPattern("yyyyMMddHHmmss");
 			return formatter.format(current);
 		}
 		// statistics
 		NumberFormat numFormatter = NumberFormat.getInstance();
 		if (name.equals(MAGIC_CURRENT_VERSION)) {
 			return WikiVersion.CURRENT_WIKI_VERSION;
 		}
 		if (name.equals(MAGIC_NUMBER_ARTICLES)) {
 			int results = WikiBase.getDataHandler().lookupTopicCount(parserInput.getVirtualWiki(), Namespace.MAIN_ID);
 			return numFormatter.format(results);
 		}
 		if (name.equals(MAGIC_NUMBER_ARTICLES_R)) {
 			int results = WikiBase.getDataHandler().lookupTopicCount(parserInput.getVirtualWiki(), Namespace.MAIN_ID);
 			return Integer.toString(results);
 		}
 		if (name.equals(MAGIC_NUMBER_PAGES)) {
 			int results = WikiBase.getDataHandler().lookupTopicCount(parserInput.getVirtualWiki(), null);
 			return numFormatter.format(results);
 		}
 		if (name.equals(MAGIC_NUMBER_PAGES_R)) {
 			int results = WikiBase.getDataHandler().lookupTopicCount(parserInput.getVirtualWiki(), null);
 			return Integer.toString(results);
 		}
 		if (name.equals(MAGIC_NUMBER_FILES)) {
 			int results = WikiBase.getDataHandler().lookupWikiFileCount(parserInput.getVirtualWiki());
 			return numFormatter.format(results);
 		}
 		if (name.equals(MAGIC_NUMBER_FILES_R)) {
 			int results = WikiBase.getDataHandler().lookupWikiFileCount(parserInput.getVirtualWiki());
 			return Integer.toString(results);
 		}
 		if (name.equals(MAGIC_NUMBER_USERS)) {
 			int results = WikiBase.getDataHandler().lookupWikiUserCount();
 			return numFormatter.format(results);
 		}
 		if (name.equals(MAGIC_NUMBER_USERS_R)) {
 			int results = WikiBase.getDataHandler().lookupWikiUserCount();
 			return Integer.toString(results);
 		}
 		/*
 		if (name.equals(MAGIC_NUMBER_ADMINS)) {
 		}
 		if (name.equals(MAGIC_NUMBER_ADMINS_R)) {
 		}
 		if (name.equals(MAGIC_PAGES_IN_NAMESPACE)) {
 		}
 		if (name.equals(MAGIC_PAGES_IN_NAMESPACE_NS)) {
 		}
 		if (name.equals(MAGIC_PAGES_IN_NAMESPACE_NS_R)) {
 		}
 		*/
 		// page values
 		WikiLink wikiLink = LinkUtil.parseWikiLink(parserInput.getVirtualWiki(), parserInput.getTopicName());
 		if (name.equals(MAGIC_FULL_PAGE_NAME)) {
 			return parserInput.getTopicName();
 		}
 		if (name.equals(MAGIC_FULL_PAGE_NAME_E)) {
 			return Utilities.encodeAndEscapeTopicName(parserInput.getTopicName());
 		}
 		if (name.equals(MAGIC_PAGE_NAME)) {
 			return wikiLink.getArticle();
 		}
 		if (name.equals(MAGIC_PAGE_NAME_E)) {
 			return Utilities.encodeAndEscapeTopicName(wikiLink.getArticle());
 		}
 		if (name.equals(MAGIC_SUB_PAGE_NAME)) {
 			String topic = wikiLink.getArticle();
 			int pos = topic.lastIndexOf('/');
 			if (pos != -1 && pos < topic.length()) {
 				topic = topic.substring(pos + 1);
 			}
 			return topic;
 		}
 		if (name.equals(MAGIC_SUB_PAGE_NAME_E)) {
 			String topic = wikiLink.getArticle();
 			int pos = topic.lastIndexOf('/');
 			if (pos != -1 && pos < topic.length()) {
 				topic = topic.substring(pos + 1);
 			}
 			return Utilities.encodeAndEscapeTopicName(topic);
 		}
 		if (name.equals(MAGIC_BASE_PAGE_NAME)) {
 			String topic = wikiLink.getArticle();
 			int pos = topic.lastIndexOf('/');
 			if (pos != -1 && pos < topic.length()) {
 				topic = topic.substring(0, pos);
 			}
 			return topic;
 		}
 		if (name.equals(MAGIC_BASE_PAGE_NAME_E)) {
 			String topic = wikiLink.getArticle();
 			int pos = topic.lastIndexOf('/');
 			if (pos != -1 && pos < topic.length()) {
 				topic = topic.substring(0, pos);
 			}
 			return Utilities.encodeAndEscapeTopicName(topic);
 		}
 		if (name.equals(MAGIC_NAMESPACE)) {
 			return wikiLink.getNamespace().getLabel(parserInput.getVirtualWiki());
 		}
 		if (name.equals(MAGIC_NAMESPACE_E)) {
 			return Utilities.encodeAndEscapeTopicName(wikiLink.getNamespace().getLabel(parserInput.getVirtualWiki()));
 		}
 		if (name.equals(MAGIC_TALK_SPACE)) {
 			Namespace result = Namespace.findCommentsNamespace(wikiLink.getNamespace());
 			return (result != null) ? result.getLabel(parserInput.getVirtualWiki()) : name;
 		}
 		if (name.equals(MAGIC_TALK_SPACE_E)) {
 			Namespace result = Namespace.findCommentsNamespace(wikiLink.getNamespace());
 			return (result != null) ? Utilities.encodeAndEscapeTopicName(result.getLabel(parserInput.getVirtualWiki())) : Utilities.encodeAndEscapeTopicName(name);
 		}
 		if (name.equals(MAGIC_SUBJECT_SPACE) || name.equals(MAGIC_ARTICLE_SPACE)) {
 			Namespace result = Namespace.findMainNamespace(wikiLink.getNamespace());
 			return (result != null) ? result.getLabel(parserInput.getVirtualWiki()) : name;
 		}
 		if (name.equals(MAGIC_SUBJECT_SPACE_E) || name.equals(MAGIC_ARTICLE_SPACE_E)) {
 			Namespace result = Namespace.findMainNamespace(wikiLink.getNamespace());
 			return (result != null) ? Utilities.encodeAndEscapeTopicName(result.getLabel(parserInput.getVirtualWiki())) : Utilities.encodeAndEscapeTopicName(name);
 		}
 		if (name.equals(MAGIC_TALK_PAGE_NAME)) {
 			return WikiUtil.extractCommentsLink(parserInput.getVirtualWiki(), parserInput.getTopicName());
 		}
 		if (name.equals(MAGIC_TALK_PAGE_NAME_E)) {
 			return Utilities.encodeAndEscapeTopicName(WikiUtil.extractCommentsLink(parserInput.getVirtualWiki(), parserInput.getTopicName()));
 		}
 		if (name.equals(MAGIC_SUBJECT_PAGE_NAME) || name.equals(MAGIC_ARTICLE_PAGE_NAME)) {
 			return WikiUtil.extractTopicLink(parserInput.getVirtualWiki(), parserInput.getTopicName());
 		}
 		if (name.equals(MAGIC_SUBJECT_PAGE_NAME_E) || name.equals(MAGIC_ARTICLE_PAGE_NAME_E)) {
 			return Utilities.encodeAndEscapeTopicName(WikiUtil.extractTopicLink(parserInput.getVirtualWiki(), parserInput.getTopicName()));
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(parserInput.getVirtualWiki(), parserInput.getTopicName(), false, null);
 		TopicVersion topicVersion = null;
 		Date revision = null;
 		// null check needed for the test data handler, which does not implement topic versions
 		if (topic != null && topic.getCurrentVersionId() != null) {
 			topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topic.getCurrentVersionId());
 			revision = topicVersion.getEditDate();
 		}
 		formatter.setTimeZone(utc);
 		if (name.equals(MAGIC_REVISION_DAY)) {
 			if (revision == null) {
 				return "";
 			}
 			formatter.applyPattern("d");
 			return formatter.format(revision);
 		}
 		if (name.equals(MAGIC_REVISION_DAY2)) {
 			if (revision == null) {
 				return "";
 			}
 			formatter.applyPattern("dd");
 			return formatter.format(revision);
 		}
 		if (name.equals(MAGIC_REVISION_MONTH)) {
 			if (revision == null) {
 				return "";
 			}
 			formatter.applyPattern("MM");
 			return formatter.format(revision);
 		}
 		if (name.equals(MAGIC_REVISION_YEAR)) {
 			if (revision == null) {
 				return "";
 			}
 			formatter.applyPattern("yyyy");
 			return formatter.format(revision);
 		}
 		if (name.equals(MAGIC_REVISION_TIMESTAMP)) {
 			if (revision == null) {
 				return "";
 			}
 			formatter.applyPattern("yyyyMMddHHmmss");
 			return formatter.format(revision);
 		}
 		if (name.equals(MAGIC_REVISION_USER)) {
 			if (topicVersion == null) {
 				return "";
 			}
 			WikiUser wikiUser = (topicVersion.getAuthorId() != null) ? WikiBase.getDataHandler().lookupWikiUser(topicVersion.getAuthorId()) : null;
 			return (wikiUser != null) ? wikiUser.getUsername() : topicVersion.getAuthorDisplay();
 		}
 		if (name.equals(MAGIC_REVISION_ID)) {
 			return (topicVersion == null) ? "" : Integer.toString(topicVersion.getTopicVersionId());
 		}
 		if (name.equals(MAGIC_SITE_NAME)) {
 			return Environment.getValue(Environment.PROP_SITE_NAME);
 		}
 		if (name.equals(MAGIC_SERVER)) {
 			return Environment.getValue(Environment.PROP_SERVER_URL);
 		}
 		/*
 		if (name.equals(MAGIC_SCRIPT_PATH)) {
 		}
 		*/
 		if (name.equals(MAGIC_SERVER_NAME)) {
			// strip the opening "http://" if there is one
			String result = Environment.getValue(Environment.PROP_SERVER_URL);
			int pos = result.indexOf("://");
			return (pos == -1) ? result : result.substring(pos + "://".length());
 		}
 		return name;
 	}
 }
