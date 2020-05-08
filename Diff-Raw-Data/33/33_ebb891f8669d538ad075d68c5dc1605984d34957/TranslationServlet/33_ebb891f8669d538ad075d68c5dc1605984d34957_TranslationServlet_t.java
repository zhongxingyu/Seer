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
 package org.jamwiki.servlets;
 
 import java.io.File;
 import java.util.Enumeration;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.SortedProperties;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to provide admins with the ability to create and edit JAMWiki message
  * keys.  Note that the application server must be restarted for any
  * translation changes to be visible on the site.
  */
 public class TranslationServlet extends JAMWikiServlet {
 
 	private static WikiLogger logger = WikiLogger.getLogger(TranslationServlet.class.getName());
 	protected static final String JSP_ADMIN_TRANSLATION = "admin-translation.jsp";
 	private SortedProperties translations = new SortedProperties();
 
 	/**
 	 * This method handles the request after its parent class receives control.
 	 *
 	 * @param request - Standard HttpServletRequest object.
 	 * @param response - Standard HttpServletResponse object.
 	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String function = request.getParameter("function");
 		if (StringUtils.hasText(function)) {
 			translate(request, next, pageInfo);
 		} else {
 			view(request, next, pageInfo);
 		}
 		next.addObject("translations", new TreeMap(this.translations));
 		next.addObject("codes", this.retrieveTranslationCodes());
 		if (request.getParameter("language") != null) {
 			next.addObject("language", request.getParameter("language"));
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private String filename(HttpServletRequest request) {
 		String filename = "ApplicationResources.properties";
 		String language = request.getParameter("language");
 		if (StringUtils.hasText(language)) {
 			// FIXME - should also check for valid language code
 			filename = "ApplicationResources_" + language + ".properties";
 		}
 		return filename;
 	}
 
 	/**
 	 *
 	 */
 	private TreeSet retrieveTranslationCodes() throws Exception {
 		TreeSet codes = new TreeSet();
 		File propertyRoot = Utilities.getClassLoaderRoot();
 		File[] files = propertyRoot.listFiles();
 		File file;
 		String filename;
 		for (int i = 0; i < files.length; i++) {
 			file = files[i];
 			if (!file.isFile()) {
 				continue;
 			}
 			filename = file.getName();
 			if (!StringUtils.hasText(filename)) {
 				continue;
 			}
 			if (!filename.startsWith("ApplicationResources_") || !filename.endsWith(".properties")) {
 				continue;
 			}
 			String code = filename.substring("ApplicationResources_".length(), filename.length() - ".properties".length());
 			if (StringUtils.hasText(code)) {
 				codes.add(code);
 			}
 		}
 		return codes;
 	}
 
 	/**
 	 *
 	 */
 	private void translate(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		pageInfo.setContentJsp(JSP_ADMIN_TRANSLATION);
 		pageInfo.setAdmin(true);
 		pageInfo.setPageTitle(new WikiMessage("translation.title"));
 		Enumeration names = request.getParameterNames();
 		String name;
 		while (names.hasMoreElements()) {
 			name = (String)names.nextElement();
 			if (!name.startsWith("translations[") || !name.endsWith("]")) {
 				continue;
 			}
 			String key = name.substring("translations[".length(), name.length() - "]".length());
 			String value = request.getParameter(name);
 			this.translations.setProperty(key, value);
 		}
 		Environment.saveProperties(filename(request), this.translations, null);
 		this.writeTopic(request, null);
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String language = request.getParameter("language");
 		String filename = filename(request);
 		this.translations = new SortedProperties(Environment.loadProperties("ApplicationResources.properties"));
 		if (StringUtils.hasText(language)) {
 			filename = filename(request);
 			this.translations.putAll(Environment.loadProperties(filename));
 		}
 		pageInfo.setContentJsp(JSP_ADMIN_TRANSLATION);
 		pageInfo.setAdmin(true);
 		pageInfo.setPageTitle(new WikiMessage("translation.title"));
 	}
 
 	/**
 	 *
 	 */
 	protected void writeTopic(HttpServletRequest request, String editComment) throws Exception {
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		String topicName = NamespaceHandler.NAMESPACE_JAMWIKI + NamespaceHandler.NAMESPACE_SEPARATOR + Utilities.decodeFromRequest(filename(request));
 		String contents = "<pre><nowiki>\n" + Utilities.readFile(filename(request)) + "\n</nowiki></pre>";
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			topic = new Topic();
 			topic.setVirtualWiki(virtualWiki);
 			topic.setName(topicName);
 		}
 		topic.setTopicContent(contents);
 		topic.setReadOnly(true);
 		topic.setTopicType(Topic.TYPE_SYSTEM_FILE);
 		TopicVersion topicVersion = new TopicVersion(Utilities.currentUser(request), request.getRemoteAddr(), editComment, contents);
 		WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, true, null);
 	}
 }
