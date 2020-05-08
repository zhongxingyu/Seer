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
 package org.jamwiki.db;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import net.sf.ehcache.Element;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.DataHandler;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.authentication.JAMWikiAuthenticationConfiguration;
 import org.jamwiki.authentication.WikiUserDetails;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.LogItem;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.RoleMap;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.Watchlist;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiGroup;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.ParserUtil;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.WikiCache;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.transaction.TransactionStatus;
 
 /**
  * Default implementation of the {@link org.jamwiki.DataHandler} interface for
  * ANSI SQL compatible databases.
  */
 public class AnsiDataHandler implements DataHandler {
 
 	private static final String CACHE_NAMESPACE_LIST = "org.jamwiki.db.AnsiDataHandler.CACHE_NAMESPACE_LIST";
 	private static final String CACHE_TOPICS = "org.jamwiki.db.AnsiDataHandler.CACHE_TOPICS";
 	private static final String CACHE_TOPIC_VERSIONS = "org.jamwiki.db.AnsiDataHandler.CACHE_TOPIC_VERSIONS";
 	private static final String CACHE_USER_BY_USER_ID = "org.jamwiki.db.AnsiDataHandler.CACHE_USER_BY_USER_ID";
 	private static final String CACHE_USER_BY_USER_NAME = "org.jamwiki.db.AnsiDataHandler.CACHE_USER_BY_USER_NAME";
 	private static final String CACHE_VIRTUAL_WIKI_LIST = "org.jamwiki.db.AnsiDataHandler.CACHE_VIRTUAL_WIKI_LIST";
 	private static final WikiLogger logger = WikiLogger.getLogger(AnsiDataHandler.class.getName());
 
 	private final QueryHandler queryHandler = new AnsiQueryHandler();
 
 	/**
 	 *
 	 */
 	private void addCategories(List<Category> categoryList, int topicId, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = -1;
 		for (Category category : categoryList) {
 			virtualWikiId = this.lookupVirtualWikiId(category.getVirtualWiki());
 			this.validateCategory(category);
 		}
 		try {
 			this.queryHandler().insertCategories(categoryList, virtualWikiId, topicId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addGroupMember(String username, int groupId, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().insertGroupMember(username, groupId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addLogItem(LogItem logItem, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(logItem.getVirtualWiki());
 		this.validateLogItem(logItem);
 		try {
 			this.queryHandler().insertLogItem(logItem, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addRecentChange(RecentChange change, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(change.getVirtualWiki());
 		this.validateRecentChange(change);
 		try {
 			this.queryHandler().insertRecentChange(change, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addTopic(Topic topic, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 		try {
 			this.validateTopic(topic);
 			this.queryHandler().insertTopic(topic, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addTopicVersion(Topic topic, TopicVersion topicVersion, Connection conn) throws DataAccessException, WikiException {
 		if (topicVersion.getPreviousTopicVersionId() == null && topic.getCurrentVersionId() != null) {
 			topicVersion.setPreviousTopicVersionId(topic.getCurrentVersionId());
 		}
 		topicVersion.setTopicId(topic.getTopicId());
 		topicVersion.initializeVersionParams(topic);
 		try {
 			this.validateTopicVersion(topicVersion);
 			this.queryHandler().insertTopicVersion(topicVersion, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		topic.setCurrentVersionId(topicVersion.getTopicVersionId());
 	}
 
 	/**
 	 *
 	 */
 	private void addUserDetails(WikiUserDetails userDetails, Connection conn) throws DataAccessException, WikiException {
 		this.validateUserDetails(userDetails);
 		try {
 			this.queryHandler().insertUserDetails(userDetails, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateVirtualWiki(virtualWiki);
 			this.queryHandler().insertVirtualWiki(virtualWiki, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws DataAccessException, WikiException {
 		this.validateWatchlistEntry(topicName);
 		try {
 			this.queryHandler().insertWatchlistEntry(virtualWikiId, topicName, userId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiFile(WikiFile wikiFile, Connection conn) throws DataAccessException, WikiException {
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
 			this.validateWikiFile(wikiFile);
 			this.queryHandler().insertWikiFile(wikiFile, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiFileVersion(WikiFileVersion wikiFileVersion, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateWikiFileVersion(wikiFileVersion);
 			this.queryHandler().insertWikiFileVersion(wikiFileVersion, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiGroup(WikiGroup group, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateWikiGroup(group);
 			this.queryHandler().insertWikiGroup(group, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiUser(WikiUser user, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateWikiUser(user);
 			this.queryHandler().insertWikiUser(user, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public boolean authenticate(String username, String password) throws DataAccessException {
 		if (StringUtils.isBlank(password)) {
 			return false;
 		}
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			// password is stored encrypted, so encrypt password
 			String encryptedPassword = Encryption.encrypt(password);
 			return this.queryHandler().authenticateUser(username, encryptedPassword, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 * Utility method for retrieving a user display name.
 	 */
 	private String authorName(Integer authorId, String authorName) throws DataAccessException {
 		if (authorId != null) {
 			WikiUser user = this.lookupWikiUser(authorId);
 			authorName = user.getUsername();
 		}
 		return authorName;
 	}
 
 	/**
 	 *
 	 */
 	public boolean canMoveTopic(Topic fromTopic, String destination) throws DataAccessException {
 		Topic toTopic = this.lookupTopic(fromTopic.getVirtualWiki(), destination, false, null);
 		if (toTopic == null || toTopic.getDeleteDate() != null) {
 			// destination doesn't exist or is deleted, so move is OK
 			return true;
 		}
 		if (toTopic.getRedirectTo() != null && toTopic.getRedirectTo().equals(fromTopic.getName())) {
 			// source redirects to destination, so move is OK
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	private static void checkLength(String value, int maxLength) throws WikiException {
 		if (value != null && value.length() > maxLength) {
 			throw new WikiException(new WikiMessage("error.fieldlength", value, Integer.valueOf(maxLength).toString()));
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void deleteRecentChanges(Topic topic, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().deleteRecentChanges(topic.getTopicId(), conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteTopic(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (topicVersion != null) {
 				// delete old recent changes
 				deleteRecentChanges(topic, conn);
 			}
 			// update topic to indicate deleted, add delete topic version.  parser output
 			// should be empty since nothing to add to search engine.
 			ParserOutput parserOutput = new ParserOutput();
 			topic.setDeleteDate(new Timestamp(System.currentTimeMillis()));
 			this.writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	private void deleteTopicCategories(Topic topic, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().deleteTopicCategories(topic.getTopicId(), conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void deleteWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().deleteWatchlistEntry(virtualWikiId, topicName, userId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void executeUpgradeQuery(String prop, Connection conn) throws SQLException {
 		this.queryHandler().executeUpgradeQuery(prop, conn);
 	}
 
 	/**
 	 *
 	 */
 	public void executeUpgradeUpdate(String prop, Connection conn) throws SQLException {
 		this.queryHandler().executeUpgradeUpdate(prop, conn);
 	}
 
 	/**
 	 *
 	 */
 	public List<Category> getAllCategories(String virtualWiki, Pagination pagination) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getCategories(virtualWikiId, virtualWiki, pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getAllRoles() throws DataAccessException {
 		try {
 			return this.queryHandler().getRoles();
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getAllTopicNames(String virtualWiki) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getAllTopicNames(virtualWikiId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<WikiFileVersion> getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws DataAccessException {
 		WikiFile wikiFile = lookupWikiFile(virtualWiki, topicName);
 		if (wikiFile == null) {
 			throw new DataAccessException("No topic exists for " + virtualWiki + " / " + topicName);
 		}
 		try {
 			return this.queryHandler().getAllWikiFileVersions(wikiFile, descending);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<LogItem> getLogItems(String virtualWiki, int logType, Pagination pagination, boolean descending) throws DataAccessException {
 		List<LogItem> logItems = new ArrayList<LogItem>();
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getLogItems(virtualWikiId, virtualWiki, logType, pagination, descending);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws DataAccessException {
 		try {
 			return this.queryHandler().getRecentChanges(virtualWiki, pagination, descending);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapByLogin(String loginFragment) throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapByLogin(loginFragment);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapByRole(String authority) throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapByRole(authority);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoleMapGroup(String groupName) throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapGroup(groupName);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapGroups() throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapGroups();
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoleMapUser(String login) throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapUser(login);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getTopicHistory(String virtualWiki, String topicName, Pagination pagination, boolean descending) throws DataAccessException {
 		Topic topic = this.lookupTopic(virtualWiki, topicName, true, null);
 		if (topic == null) {
 			return new ArrayList<RecentChange>();
 		}
 		try {
 			return this.queryHandler().getTopicHistory(topic.getTopicId(), pagination, descending);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getTopicsAdmin(String virtualWiki, Pagination pagination) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getTopicsAdmin(virtualWikiId, pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws DataAccessException {
 		try {
 			if (this.lookupWikiUser(userString) != null) {
 				return this.queryHandler().getUserContributionsByLogin(virtualWiki, userString, pagination, descending);
 			} else {
 				return this.queryHandler().getUserContributionsByUserDisplay(virtualWiki, userString, pagination, descending);
 			}
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 * Return a List of all VirtualWiki objects that exist for the Wiki.
 	 */
 	public List<VirtualWiki> getVirtualWikiList() throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_VIRTUAL_WIKI_LIST, CACHE_VIRTUAL_WIKI_LIST);
 		if (cacheElement != null) {
 			return (List<VirtualWiki>)cacheElement.getObjectValue();
 		}
 		List<VirtualWiki> virtualWikis = new ArrayList<VirtualWiki>();
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			virtualWikis = this.queryHandler().getVirtualWikis(conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 		WikiCache.addToCache(CACHE_VIRTUAL_WIKI_LIST, CACHE_VIRTUAL_WIKI_LIST, virtualWikis);
 		return virtualWikis;
 	}
 
 	/**
 	 * Retrieve a watchlist containing a List of topic ids and topic
 	 * names that can be used to determine if a topic is in a user's current
 	 * watchlist.
 	 */
 	public Watchlist getWatchlist(String virtualWiki, int userId) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			List<String> watchedTopicNames = this.queryHandler().getWatchlist(virtualWikiId, userId);
 			return new Watchlist(virtualWiki, watchedTopicNames);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 * Retrieve a watchlist containing a List of RecentChanges objects
 	 * that can be used for display on the Special:Watchlist page.
 	 */
 	public List<RecentChange> getWatchlist(String virtualWiki, int userId, Pagination pagination) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getWatchlist(virtualWikiId, userId, pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Category> lookupCategoryTopics(String virtualWiki, String categoryName) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().lookupCategoryTopics(virtualWikiId, virtualWiki, categoryName);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Namespace lookupNamespace(String virtualWiki, String namespaceString) throws DataAccessException {
 		if (namespaceString == null) {
 			return null;
 		}
 		List<Namespace> namespaces = this.lookupNamespaces();
 		for (Namespace namespace : namespaces) {
 			if (namespace.getLabel(virtualWiki).equals(namespaceString)) {
 				// found a match, return it
 				return namespace;
 			}
 		}
 		// no result found
 		return null;
 	}
 
 	/**
 	 *
 	 */
 	public List<Namespace> lookupNamespaces() throws DataAccessException {
 		// first check the cache
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_NAMESPACE_LIST, CACHE_NAMESPACE_LIST);
 		if (cacheElement != null) {
 			return (List<Namespace>)cacheElement.getObjectValue();
 		}
 		// if not in the cache, go to the database
 		List<Namespace> namespaces = null;
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			namespaces = this.queryHandler().lookupNamespaces(conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 		WikiCache.addToCache(CACHE_NAMESPACE_LIST, CACHE_NAMESPACE_LIST, namespaces);
 		return namespaces;
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Connection conn) throws DataAccessException {
 		if (StringUtils.isBlank(virtualWiki) || StringUtils.isBlank(topicName)) {
 			return null;
 		}
 		long start = System.currentTimeMillis();
 		String key = WikiCache.key(virtualWiki, topicName);
 		if (conn == null) {
 			// retrieve topic from the cache only if this call is not currently a part
 			// of a transaction to avoid retrieving data that might have been updated
 			// as part of this transaction and would thus now be out of date
 			Element cacheElement = WikiCache.retrieveFromCache(CACHE_TOPICS, key);
 			if (cacheElement != null) {
 				Topic cacheTopic = (Topic)cacheElement.getObjectValue();
 				return (cacheTopic == null || (!deleteOK && cacheTopic.getDeleteDate() != null)) ? null : new Topic(cacheTopic);
 			}
 		}
 		Namespace namespace = LinkUtil.parseWikiLink(virtualWiki, topicName).getNamespace();
 		boolean caseSensitive = true;
 		if (namespace.equals(Namespace.SPECIAL)) {
 			// invalid namespace
 			return null;
 		}
 		if (namespace.equals(Namespace.TEMPLATE) || namespace.equals(Namespace.USER) || namespace.equals(Namespace.CATEGORY)) {
 			// user/template/category namespaces are case-insensitive
 			caseSensitive = false;
 		}
 		Topic topic = null;
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 			topic = this.queryHandler().lookupTopic(virtualWikiId, virtualWiki, topicName, caseSensitive, conn);
 			if (conn == null) {
 				// add topic to the cache only if it is not currently a part of a transaction
 				// to avoid caching something that might need to be rolled back
 				Topic cacheTopic = (topic == null) ? null : new Topic(topic);
 				WikiCache.addToCache(CACHE_TOPICS, key, cacheTopic);
 			}
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		if (logger.isFineEnabled()) {
 			double execution = ((System.currentTimeMillis() - start) / 1000.000);
 			if (execution > 0.005) {
 				logger.fine("WARNING: slow topic lookup for: " + topicName + " (" + execution + " s)");
 			}
 		}
 		return (topic == null || (!deleteOK && topic.getDeleteDate() != null)) ? null : topic;
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopicById(String virtualWiki, int topicId) throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_TOPICS, topicId);
 		if (cacheElement != null) {
 			return (Topic)cacheElement.getObjectValue();
 		}
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		Topic result = null;
 		try {
 			result = this.queryHandler().lookupTopicById(virtualWikiId, virtualWiki, topicId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_TOPICS, topicId, result);
 		return result;
 	}
 
 	/**
 	 * Return a count of all topics, including redirects, comments pages and templates,
 	 * currently available on the Wiki.  This method excludes deleted topics.
 	 *
 	 * @param virtualWiki The virtual wiki for which the total topic count is being returned
 	 *  for.
 	 */
 	public int lookupTopicCount(String virtualWiki) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().lookupTopicCount(virtualWikiId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Map<Integer, String> lookupTopicByType(String virtualWiki, TopicType topicType1, TopicType topicType2, Pagination pagination) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().lookupTopicByType(virtualWikiId, topicType1, topicType2, pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(int topicVersionId) throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_TOPIC_VERSIONS, topicVersionId);
 		if (cacheElement != null) {
 			return (TopicVersion)cacheElement.getObjectValue();
 		}
 		TopicVersion topicVersion = null;
 		try {
 			topicVersion = this.queryHandler().lookupTopicVersion(topicVersionId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_TOPIC_VERSIONS, topicVersionId, topicVersion);
 		return topicVersion;
 	}
 
 	/**
 	 *
 	 */
 	public VirtualWiki lookupVirtualWiki(String virtualWikiName) throws DataAccessException {
 		List<VirtualWiki> virtualWikis = this.getVirtualWikiList();
 		for (VirtualWiki virtualWiki : virtualWikis) {
 			if (virtualWiki.getName().equals(virtualWikiName)) {
 				// found a match, return it
 				return virtualWiki;
 			}
 		}
 		// no result found
 		return null;
 	}
 
 	/**
 	 *
 	 */
 	private int lookupVirtualWikiId(String virtualWikiName) throws DataAccessException {
 		VirtualWiki virtualWiki = this.lookupVirtualWiki(virtualWikiName);
 		return (virtualWiki == null) ? -1 : virtualWiki.getVirtualWikiId();
 	}
 
 	/**
 	 *
 	 */
 	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws DataAccessException {
 		Topic topic = this.lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			return null;
 		}
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 			return this.queryHandler().lookupWikiFile(virtualWikiId, virtualWiki, topic.getTopicId());
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 * Return a count of all wiki files currently available on the Wiki.  This
 	 * method excludes deleted files.
 	 *
 	 * @param virtualWiki The virtual wiki of the files being retrieved.
 	 */
 	public int lookupWikiFileCount(String virtualWiki) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().lookupWikiFileCount(virtualWikiId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiGroup lookupWikiGroup(String groupName) throws DataAccessException {
 		try {
 			return this.queryHandler().lookupWikiGroup(groupName);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(int userId) throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_USER_BY_USER_ID, userId);
 		if (cacheElement != null) {
 			return (WikiUser)cacheElement.getObjectValue();
 		}
 		WikiUser user = null;
 		try {
 			user = this.queryHandler().lookupWikiUser(userId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_USER_BY_USER_ID, userId, user);
 		return user;
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(String username) throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_USER_BY_USER_NAME, username);
 		if (cacheElement != null) {
 			return (WikiUser)cacheElement.getObjectValue();
 		}
 		WikiUser result = null;
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			int userId = this.queryHandler().lookupWikiUser(username, conn);
 			if (userId != -1) {
 				result = lookupWikiUser(userId);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 		WikiCache.addToCache(CACHE_USER_BY_USER_NAME, username, result);
 		return result;
 	}
 
 	/**
 	 * Return a count of all wiki users.
 	 */
 	public int lookupWikiUserCount() throws DataAccessException {
 		try {
 			return this.queryHandler().lookupWikiUserCount();
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public String lookupWikiUserEncryptedPassword(String username) throws DataAccessException {
 		try {
 			return this.queryHandler().lookupWikiUserEncryptedPassword(username);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupWikiUsers(Pagination pagination) throws DataAccessException {
 		try {
 			return this.queryHandler().lookupWikiUsers(pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void moveTopic(Topic fromTopic, TopicVersion fromVersion, String destination) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (!this.canMoveTopic(fromTopic, destination)) {
 				throw new WikiException(new WikiMessage("move.exception.destinationexists", destination));
 			}
 			Topic toTopic = this.lookupTopic(fromTopic.getVirtualWiki(), destination, false, conn);
 			boolean detinationExistsFlag = (toTopic != null && toTopic.getDeleteDate() == null);
 			if (detinationExistsFlag) {
 				// if the target topic is a redirect to the source topic then the
 				// target must first be deleted.
 				this.deleteTopic(toTopic, null);
 			}
 			// first rename the source topic with the new destination name
 			String fromTopicName = fromTopic.getName();
 			fromTopic.setName(destination);
 			// only one version needs to create a recent change entry, so do not create a log entry
 			// for the "from" version
 			fromVersion.setRecentChangeAllowed(false);
 			ParserOutput fromParserOutput = ParserUtil.parserOutput(fromTopic.getTopicContent(), fromTopic.getVirtualWiki(), fromTopic.getName());
 			writeTopic(fromTopic, fromVersion, fromParserOutput.getCategories(), fromParserOutput.getLinks());
 			// now either create a new topic that is a redirect with the
 			// source topic's old name, or else undelete the new topic and
 			// rename.
 			if (detinationExistsFlag) {
 				// target topic was deleted, so rename and undelete
 				toTopic.setName(fromTopicName);
 				writeTopic(toTopic, null, null, null);
 				this.undeleteTopic(toTopic, null);
 			} else {
 				// create a new topic that redirects to the destination
 				toTopic = new Topic(fromTopic);
 				toTopic.setTopicId(-1);
 				toTopic.setName(fromTopicName);
 			}
 			String content = ParserUtil.parserRedirectContent(destination);
 			toTopic.setRedirectTo(destination);
 			toTopic.setTopicType(TopicType.REDIRECT);
 			toTopic.setTopicContent(content);
 			TopicVersion toVersion = fromVersion;
 			toVersion.setTopicVersionId(-1);
 			toVersion.setVersionContent(content);
 			toVersion.setRecentChangeAllowed(true);
 			ParserOutput toParserOutput = ParserUtil.parserOutput(toTopic.getTopicContent(), toTopic.getVirtualWiki(), toTopic.getName());
 			writeTopic(toTopic, toVersion, toParserOutput.getCategories(), toParserOutput.getLinks());
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (ParserException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void orderTopicVersions(Topic topic, List<Integer> topicVersionIdList) throws DataAccessException {
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 			this.queryHandler().orderTopicVersions(topic, virtualWikiId, topicVersionIdList);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected QueryHandler queryHandler() {
 		return this.queryHandler;
 	}
 
 	/**
 	 *
 	 */
 	public void reloadLogItems() throws DataAccessException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			List<VirtualWiki> virtualWikis = this.getVirtualWikiList();
 			for (VirtualWiki virtualWiki : virtualWikis) {
 				this.queryHandler().reloadLogItems(virtualWiki.getVirtualWikiId(), conn);
 			}
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void reloadRecentChanges() throws DataAccessException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().reloadRecentChanges(conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void setup(Locale locale, WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
 		WikiDatabase.initialize();
 		// determine if database exists
 		Connection conn = null;
 		Statement stmt = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.createStatement();
 			stmt.executeQuery(WikiDatabase.getExistenceValidationQuery());
 			return;
 		} catch (SQLException e) {
 			// database not yet set up
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt);
 		}
 		WikiDatabase.setup(locale, user, username, encryptedPassword);
 	}
 
 	/**
 	 *
 	 */
 	public void setupSpecialPages(Locale locale, WikiUser user, VirtualWiki virtualWiki) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			// create the default topics
 			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STARTING_POINTS, user, false);
 			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_LEFT_MENU, user, true);
 			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_BOTTOM_AREA, user, true);
 			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user, true);
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void undeleteTopic(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			// update topic to indicate deleted, add delete topic version.  if
 			// topic has categories or other metadata then parser document is
 			// also needed.
 			ParserOutput parserOutput = ParserUtil.parserOutput(topic.getTopicContent(), topic.getVirtualWiki(), topic.getName());
 			topic.setDeleteDate(null);
 			this.writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (ParserException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void updateSpecialPage(Locale locale, String virtualWiki, String topicName, String userDisplay) throws DataAccessException, WikiException {
 		logger.info("Updating special page " + virtualWiki + " / " + topicName);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			String contents = WikiUtil.readSpecialPage(locale, topicName);
 			Topic topic = this.lookupTopic(virtualWiki, topicName, false, conn);
 			int charactersChanged = StringUtils.length(contents) - StringUtils.length(topic.getTopicContent());
 			topic.setTopicContent(contents);
 			// FIXME - hard coding
 			TopicVersion topicVersion = new TopicVersion(null, userDisplay, "Automatically updated by system upgrade", contents, charactersChanged);
 			ParserOutput parserOutput = ParserUtil.parserOutput(topic.getTopicContent(), virtualWiki, topicName);
 			writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (ParserException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (IOException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	private void updateTopic(Topic topic, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 		this.validateTopic(topic);
 		try {
 			this.queryHandler().updateTopic(topic, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateUserDetails(WikiUserDetails userDetails, Connection conn) throws DataAccessException, WikiException {
 		this.validateUserDetails(userDetails);
 		try {
 			this.queryHandler().updateUserDetails(userDetails, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws DataAccessException, WikiException {
 		this.validateVirtualWiki(virtualWiki);
 		try {
 			this.queryHandler().updateVirtualWiki(virtualWiki, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateWikiFile(WikiFile wikiFile, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
 		this.validateWikiFile(wikiFile);
 		try {
 			this.queryHandler().updateWikiFile(wikiFile, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateWikiGroup(WikiGroup group, Connection conn) throws DataAccessException, WikiException {
 		this.validateWikiGroup(group);
 		try {
 			this.queryHandler().updateWikiGroup(group, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateWikiUser(WikiUser user, Connection conn) throws DataAccessException, WikiException {
 		this.validateWikiUser(user);
 		try {
 			this.queryHandler().updateWikiUser(user, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void validateAuthority(String role) throws WikiException {
 		checkLength(role, 30);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateCategory(Category category) throws WikiException {
 		checkLength(category.getName(), 200);
 		checkLength(category.getSortKey(), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateLogItem(LogItem logItem) throws WikiException {
 		checkLength(logItem.getUserDisplayName(), 200);
 		checkLength(logItem.getLogParamString(), 500);
 		logItem.setLogComment(StringUtils.substring(logItem.getLogComment(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateNamespace(Namespace mainNamespace, Namespace commentsNamespace) throws WikiException {
 		checkLength(mainNamespace.getDefaultLabel(), 200);
 		if (commentsNamespace != null) {
 			checkLength(commentsNamespace.getDefaultLabel(), 200);
			if (commentsNamespace == null || !commentsNamespace.equals(mainNamespace)) {
 				throw new WikiException(new WikiMessage("error.commentsnamespace", commentsNamespace.getDefaultLabel(), mainNamespace.getDefaultLabel()));
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void validateNamespaceTranslation(Namespace namespace, String virtualWiki) throws WikiException {
 		checkLength(namespace.getLabel(virtualWiki), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateRecentChange(RecentChange change) throws WikiException {
 		checkLength(change.getTopicName(), 200);
 		checkLength(change.getAuthorName(), 200);
 		checkLength(change.getVirtualWiki(), 100);
 		change.setChangeComment(StringUtils.substring(change.getChangeComment(), 0, 200));
 		checkLength(change.getParamString(), 500);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateRole(Role role) throws WikiException {
 		checkLength(role.getAuthority(), 30);
 		role.setDescription(StringUtils.substring(role.getDescription(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateTopic(Topic topic) throws WikiException {
 		checkLength(topic.getName(), 200);
 		checkLength(topic.getRedirectTo(), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateTopicVersion(TopicVersion topicVersion) throws WikiException {
 		checkLength(topicVersion.getAuthorDisplay(), 100);
 		checkLength(topicVersion.getVersionParamString(), 500);
 		topicVersion.setEditComment(StringUtils.substring(topicVersion.getEditComment(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateUserDetails(WikiUserDetails userDetails) throws WikiException {
 		checkLength(userDetails.getUsername(), 100);
 		// do not throw exception containing password info
 		if (userDetails.getPassword() != null && userDetails.getPassword().length() > 100) {
 			throw new WikiException(new WikiMessage("error.fieldlength", "-", "100"));
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void validateVirtualWiki(VirtualWiki virtualWiki) throws WikiException {
 		checkLength(virtualWiki.getName(), 100);
 		checkLength(virtualWiki.getDefaultTopicName(), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWatchlistEntry(String topicName) throws WikiException {
 		checkLength(topicName, 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWikiFile(WikiFile wikiFile) throws WikiException {
 		checkLength(wikiFile.getFileName(), 200);
 		checkLength(wikiFile.getUrl(), 200);
 		checkLength(wikiFile.getMimeType(), 100);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWikiFileVersion(WikiFileVersion wikiFileVersion) throws WikiException {
 		checkLength(wikiFileVersion.getUrl(), 200);
 		checkLength(wikiFileVersion.getMimeType(), 100);
 		checkLength(wikiFileVersion.getAuthorDisplay(), 100);
 		wikiFileVersion.setUploadComment(StringUtils.substring(wikiFileVersion.getUploadComment(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWikiGroup(WikiGroup group) throws WikiException {
 		checkLength(group.getName(), 30);
 		group.setDescription(StringUtils.substring(group.getDescription(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWikiUser(WikiUser user) throws WikiException {
 		checkLength(user.getUsername(), 100);
 		checkLength(user.getDisplayName(), 100);
 		checkLength(user.getCreateIpAddress(), 39);
 		checkLength(user.getLastLoginIpAddress(), 39);
 		checkLength(user.getDefaultLocale(), 8);
 		checkLength(user.getEmail(), 100);
 		checkLength(user.getEditor(), 50);
 		checkLength(user.getSignature(), 255);
 	}
 
 	/**
 	 *
 	 */
 	public void writeFile(WikiFile wikiFile, WikiFileVersion wikiFileVersion) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			WikiUtil.validateTopicName(wikiFile.getVirtualWiki(), wikiFile.getFileName());
 			if (wikiFile.getFileId() <= 0) {
 				addWikiFile(wikiFile, conn);
 			} else {
 				updateWikiFile(wikiFile, conn);
 			}
 			wikiFileVersion.setFileId(wikiFile.getFileId());
 			// write version
 			addWikiFileVersion(wikiFileVersion, conn);
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeNamespace(Namespace mainNamespace, Namespace commentsNamespace) throws DataAccessException, WikiException {
 		this.validateNamespace(mainNamespace, commentsNamespace);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().updateNamespace(mainNamespace, commentsNamespace, conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 		WikiCache.removeAllFromCache(CACHE_NAMESPACE_LIST);
 	}
 
 	/**
 	 *
 	 */
 	public void writeNamespaceTranslations(List<Namespace> namespaces, String virtualWiki) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		for (Namespace namespace : namespaces) {
 			this.validateNamespaceTranslation(namespace, virtualWiki);
 		}
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().updateNamespaceTranslations(namespaces, virtualWiki, virtualWikiId, conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 		WikiCache.removeAllFromCache(CACHE_NAMESPACE_LIST);
 	}
 
 	/**
 	 *
 	 */
 	public void writeRole(Role role, boolean update) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.validateRole(role);
 			if (update) {
 				this.queryHandler().updateRole(role, conn);
 			} else {
 				this.queryHandler().insertRole(role, conn);
 			}
 			// FIXME - add caching
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeRoleMapGroup(int groupId, List<String> roles) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().deleteGroupAuthorities(groupId, conn);
 			for (String authority : roles) {
 				this.validateAuthority(authority);
 				this.queryHandler().insertGroupAuthority(groupId, authority, conn);
 			}
 			// refresh the current role requirements
 			JAMWikiAuthenticationConfiguration.resetJamwikiAnonymousAuthorities();
 			JAMWikiAuthenticationConfiguration.resetDefaultGroupRoles();
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeRoleMapUser(String username, List<String> roles) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().deleteUserAuthorities(username, conn);
 			for (String authority : roles) {
 				this.validateAuthority(authority);
 				this.queryHandler().insertUserAuthority(username, authority, conn);
 			}
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 * Commit changes to a topic (and its version) to the database or
 	 * filesystem.
 	 *
 	 * @param topic The topic object that is to be committed.  If the topic
 	 *  id is empty or less than zero then the topic is added, otherwise an
 	 *  update is performed.
 	 * @param topicVersion The version associated with the topic that is
 	 *  being added.  This parameter should never be null UNLESS the change is
 	 *  not user visible, such as when deleting a topic temporarily during
 	 *  page moves.
 	 * @param categories A mapping of categories and their associated sort keys (if any)
 	 *  for all categories that are associated with the current topic.
 	 * @param links A List of all topic names that are linked to from the
 	 *  current topic.  These will be passed to the search engine to create
 	 *  searchable metadata.
 	 */
 	public void writeTopic(Topic topic, TopicVersion topicVersion, LinkedHashMap<String, String> categories, List<String> links) throws DataAccessException, WikiException {
 		long start = System.currentTimeMillis();
 		WikiUtil.validateTopicName(topic.getVirtualWiki(), topic.getName());
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (topic.getTopicId() <= 0) {
 				// create the initial topic record
 				addTopic(topic, conn);
 			} else if (topicVersion == null) {
 				// if there is no version record then update the topic.  if there is a version
 				// record then the topic will be updated AFTER the version record is created.
 				this.updateTopic(topic, conn);
 			}
 			if (topicVersion != null) {
 				// write version
 				addTopicVersion(topic, topicVersion, conn);
 				// update the topic AFTER creating the version so that the current_topic_version_id parameter is set properly
 				this.updateTopic(topic, conn);
 				String authorName = this.authorName(topicVersion.getAuthorId(), topicVersion.getAuthorDisplay());
 				LogItem logItem = LogItem.initLogItem(topic, topicVersion, authorName);
 				RecentChange change = null;
 				if (logItem != null) {
 					this.addLogItem(logItem, conn);
 					change = RecentChange.initRecentChange(logItem);
 				} else {
 					change = RecentChange.initRecentChange(topic, topicVersion, authorName);
 				}
 				if (topicVersion.isRecentChangeAllowed()) {
 					this.addRecentChange(change, conn);
 				}
 			}
 			if (categories != null) {
 				// add / remove categories associated with the topic
 				this.deleteTopicCategories(topic, conn);
 				if (topic.getDeleteDate() == null && !categories.isEmpty()) {
 					List<Category> categoryList = new ArrayList<Category>();
 					for (String categoryName : categories.keySet()) {
 						Category category = new Category();
 						category.setName(categoryName);
 						category.setSortKey(categories.get(categoryName));
 						category.setVirtualWiki(topic.getVirtualWiki());
 						category.setChildTopicName(topic.getName());
 						categoryList.add(category);
 					}
 					this.addCategories(categoryList, topic.getTopicId(), conn);
 				}
 			}
 			if (links != null) {
 				WikiBase.getSearchEngine().updateInIndex(topic, links);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 		// update the cache AFTER the commit
 		String key = WikiCache.key(topic.getVirtualWiki(), topic.getName());
 		WikiCache.removeFromCache(WikiBase.CACHE_PARSED_TOPIC_CONTENT, key);
 		WikiCache.addToCache(CACHE_TOPICS, key, topic);
 		logger.fine("Wrote topic " + topic.getName() + " with params [categories is null: " + (categories == null) + "] / [links is null: " + (links == null) + "] in " + ((System.currentTimeMillis() - start) / 1000.000) + " s.");
 	}
 
 	/**
 	 *
 	 */
 	public void writeTopicVersion(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			this.addTopicVersion(topic, topicVersion, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void writeVirtualWiki(VirtualWiki virtualWiki) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			WikiUtil.validateTopicName(null, virtualWiki.getName());
 			if (virtualWiki.getVirtualWikiId() <= 0) {
 				this.addVirtualWiki(virtualWiki, conn);
 			} else {
 				this.updateVirtualWiki(virtualWiki, conn);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 		// flush the cache
 		WikiCache.removeAllFromCache(CACHE_VIRTUAL_WIKI_LIST);
 	}
 
 	/**
 	 *
 	 */
 	public void writeWatchlistEntry(Watchlist watchlist, String virtualWiki, String topicName, int userId) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 			String article = WikiUtil.extractTopicLink(virtualWiki, topicName);
 			String comments = WikiUtil.extractCommentsLink(virtualWiki, topicName);
 			if (watchlist.containsTopic(topicName)) {
 				// remove from watchlist
 				this.deleteWatchlistEntry(virtualWikiId, article, userId, conn);
 				this.deleteWatchlistEntry(virtualWikiId, comments, userId, conn);
 				watchlist.remove(article);
 				watchlist.remove(comments);
 			} else {
 				// add to watchlist
 				this.addWatchlistEntry(virtualWikiId, article, userId, conn);
 				this.addWatchlistEntry(virtualWikiId, comments, userId, conn);
 				watchlist.add(article);
 				watchlist.add(comments);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeWikiGroup(WikiGroup group) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (group.getGroupId() <= 0) {
 				this.addWikiGroup(group, conn);
 			} else {
 				this.updateWikiGroup(group, conn);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeWikiUser(WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
 		WikiUtil.validateUserName(user.getUsername());
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (user.getUserId() <= 0) {
 				WikiUserDetails userDetails = new WikiUserDetails(username, encryptedPassword, true, true, true, true, JAMWikiAuthenticationConfiguration.getDefaultGroupRoles());
 				this.addUserDetails(userDetails, conn);
 				this.addWikiUser(user, conn);
 				// add all users to the registered user group
 				this.addGroupMember(user.getUsername(), WikiBase.getGroupRegisteredUser().getGroupId(), conn);
 				// FIXME - reconsider this approach of separate entries for every virtual wiki
 				List<VirtualWiki> virtualWikis = this.getVirtualWikiList();
 				for (VirtualWiki virtualWiki : virtualWikis) {
 					LogItem logItem = LogItem.initLogItem(user, virtualWiki.getName());
 					this.addLogItem(logItem, conn);
 					RecentChange change = RecentChange.initRecentChange(logItem);
 					this.addRecentChange(change, conn);
 				}
 			} else {
 				if (!StringUtils.isBlank(encryptedPassword)) {
 					WikiUserDetails userDetails = new WikiUserDetails(username, encryptedPassword, true, true, true, true, JAMWikiAuthenticationConfiguration.getDefaultGroupRoles());
 					this.updateUserDetails(userDetails, conn);
 				}
 				this.updateWikiUser(user, conn);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 		// update the cache AFTER the commit
 		WikiCache.addToCache(CACHE_USER_BY_USER_ID, user.getUserId(), user);
 		WikiCache.addToCache(CACHE_USER_BY_USER_NAME, user.getUsername(), user);
 	}
 }
