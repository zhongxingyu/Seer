 /**
  * 
  */
 package com.cee.news.store.jcr;
 
 import static com.cee.news.store.jcr.JcrStoreConstants.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.RowIterator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cee.news.model.Article;
 import com.cee.news.model.EntityKey;
 import com.cee.news.model.Site;
 import com.cee.news.model.TextBlock;
 import com.cee.news.model.WorkingSet;
 import com.cee.news.store.ArticleChangeListener;
 import com.cee.news.store.ArticleStore;
 import com.cee.news.store.StoreException;
 
 /**
  * JCR implementation of the store {@link NewsStore}
  */
 public class JcrArticleStore extends JcrStoreBase implements ArticleStore {
 
 	private final static Logger LOG = LoggerFactory.getLogger(JcrArticleStore.class);
 
     private final static String SELECT_ARTICLES_OF_SITE_ORDERED_BY_DATE = "SELECT s.[news:name] AS " + SITE_NAME_SELECTOR + ", a.[news:id] AS " + ARTICLE_ID_SELECTOR + ", a.[news:title] AS " + ARTICLE_TITLE_SELECTOR + " "
                                                                         + "FROM [news:article] AS a INNER JOIN [news:site] AS s ON ISCHILDNODE(a, s) "
                                                                         + "WHERE %s " 
                                                                         + "ORDER BY a.[news:published] DESC";
     
     private final static String SELECT_RELATED_ARTICLES = //"SELECT s.[news:name] AS " + SITE_NAME_SELECTOR + ", a.[news:id] AS " + ARTICLE_ID_SELECTOR + ", a.[news:title] AS " + ARTICLE_TITLE_SELECTOR + " "
     													"SELECT * "
 													    + "FROM [news:article] "
 													    //+ "WHERE (%s) AND 
 													    + "WHERE SIMILAR(., '/news:content/%s/news:title') "; 
 													    //+ "ORDER BY a.[news:published] DESC";
     
     private final static String OR = "OR ";
     
     private final static String WHERE_SITE_NAME_TERM = "s.[news:name] = '%s' ";
     
     //TODO: Implement paging
     private final static int DEFAULT_QUERY_LIMIT = 10;
     
     private List<ArticleChangeListener> changeListeners;
     
     public JcrArticleStore() {
     }
 
     public JcrArticleStore(Session session) throws StoreException {
         setSession(session);
     }
     
     protected Node getArticleNode(String siteName, String articleName) throws RepositoryException {
         if (siteName == null) {
             throw new IllegalArgumentException("Parameter siteName must not be null");
         }
         if (articleName == null) {
             throw new IllegalArgumentException("Parameter articleName must not be null");
         }
         return getContentNodeOrNull(getArticlePath(siteName, articleName));
     }
     
     protected Node getArticleNodeByPath(String articlePath) throws RepositoryException {
         if (articlePath == null) {
             throw new IllegalArgumentException("Parameter articlePath must not be null");
         }
         return getContentNodeOrNull(articlePath);
     }
 
     public EntityKey update(Site site, Article article) throws StoreException {
         if (site == null) {
             throw new IllegalArgumentException("Parameter site must not be null");
         }
         if (article == null) {
             throw new IllegalArgumentException("Parameter article must not be null");
         }
         Node articleNode = null;
         String siteName = site.getName();
         String articleId = article.getExternalId();
         
         try {
             articleNode = getArticleNode(siteName, articleId);
         } catch (RepositoryException e) {
             throw new StoreException(article, "Could not retrive article node", e);
         }
         
         boolean articleCreated = false;
         
         if (articleNode == null) {
         	articleCreated = true;
             Node siteNode = null;
             try {
                 siteNode = getContentNodeOrNull(JcrSiteStore.getSitePath(siteName));
             } catch (RepositoryException e) {
                 throw new StoreException(site, "Could not find site node", e);
             }
             if (siteNode == null) {
                 throw new StoreException(site, "Site does not exists");
             }
             try {
                 articleNode = siteNode.addNode(getArticlePath(articleId), NODE_ARTICLE);
             } catch (RepositoryException e) {
                 throw new StoreException(article, "Could not add article to site", e);
             }
         } else {
             //remove existing content for re-adding
             try {
                 NodeIterator iter = articleNode.getNodes(NODE_CONTENT);
                 while(iter.hasNext()) {
                     iter.nextNode().remove();
                 }
                 getSession().save();
             } catch (RepositoryException e) {
                 throw new StoreException(article, "Could not remove existing content from node", e);
             }
         }
 
         try {
             articleNode.setProperty(PROP_ID, articleId);
             String title = article.getTitle();
             String shortText = article.getShortText();
             if (shortText == null) {
                 shortText = title;
             }
             articleNode.setProperty(PROP_SHORT_TEXT, shortText);
             articleNode.setProperty(PROP_TITLE, title);
             articleNode.setProperty(PROP_LOCATION, article.getLocation());
             articleNode.setProperty(PROP_PUBLISHED, article.getPublishedDate());
             for (TextBlock text : article.getContent()) {
                 Node textNode = articleNode.addNode(NODE_CONTENT, NODE_TEXTBLOCK);
                 textNode.setProperty(PROP_CONTENT, text.getContent());
                 textNode.setProperty(PROP_WORDS, text.getNumWords());
             }
         } catch (RepositoryException e) {
             throw new StoreException(article, "Could not persist article", e);
         }
         try {
             getSession().save();
             if (articleCreated) {
             	fireArticleCreated(site, article);
             } else {
             	fireArticleChanged(site, article);
             }
             EntityKey key = new EntityKey(article.getTitle(), getArticlePath(siteName, articleId));
             LOG.debug("Created article {}", key);
             return key;
         } catch (RepositoryException e) {
             throw new StoreException(site, "Could not save session", e);
         }
     }
     
     protected void fireArticleChanged(Site site, Article article) {
    	if (changeListeners == null) return;
     	for (ArticleChangeListener changeListener : changeListeners) {
 			changeListener.onArticleChanged(site, article);
 		}
     }
     
     protected void fireArticleCreated(Site site, Article article) {
    	if (changeListeners == null) return;
     	for (ArticleChangeListener changeListener : changeListeners) {
 			changeListener.onArticleCreated(site, article);
 		}
     }
     
     @Override
     public void addArticleChangeListener(ArticleChangeListener listener) {
     	if (changeListeners == null) {
     		changeListeners = new ArrayList<ArticleChangeListener>();
     	}
     	changeListeners.add(listener);
     }
 
     public Article getArticle(String articlePath) throws StoreException {
         if (articlePath == null) {
             throw new IllegalArgumentException("Parameter articlePath must not be null");
         }
         Node articleNode = null;
         try {
             articleNode = getArticleNodeByPath(articlePath);
         } catch (RepositoryException e) {
             throw new StoreException("Could not retrive article node", e);
         }
 
         if (articleNode == null) {
             return null;
         } else {
             Article article = new Article();
             try {
                 article.setExternalId(articleNode.getProperty(PROP_ID).getString());
                 article.setLocation(articleNode.getProperty(PROP_LOCATION).getString());
                 article.setPublishedDate(articleNode.getProperty(PROP_PUBLISHED).getDate());
                 article.setShortText(articleNode.getProperty(PROP_SHORT_TEXT).getString());
                 article.setTitle(articleNode.getProperty(PROP_TITLE).getString());
             } catch (RepositoryException e) {
                 throw new StoreException("Could not set article properties", e);
             }
             return article;
         }
     }
     
     protected String buildExpression(String formatStr, String operator, List<String> keys) {
     	boolean isFirst = true;
     	StringBuilder buffer = new StringBuilder();
     	for (String key : keys) {
 			if (isFirst) {
 				isFirst = false;
 			} else {
 				buffer.append(operator);
 			}
 			buffer.append(String.format(formatStr, key));
 		}
     	return buffer.toString();
     }
 
     protected RowIterator getArticlesOfSitesOrderedByPublication(List<String> siteNames) throws RepositoryException {
         testSession();
         QueryManager queryManager = getSession().getWorkspace().getQueryManager();
         String whereExpression = buildExpression(WHERE_SITE_NAME_TERM, OR, siteNames);
         Query q = queryManager.createQuery(String.format(SELECT_ARTICLES_OF_SITE_ORDERED_BY_DATE, whereExpression), Query.JCR_SQL2);
         q.setOffset(0);
         q.setLimit(DEFAULT_QUERY_LIMIT);
         return q.execute().getRows();
     }
     
     public List<EntityKey> getArticlesOrderedByDate(Site site) throws StoreException {
         if (site == null) {
             throw new IllegalArgumentException("Parameter site must not be null");
         }
         try {
             List<String> sites = new ArrayList<String>();
             sites.add(site.getName());
             return buildPathList(getArticlesOfSitesOrderedByPublication(sites));
         } catch (RepositoryException e) {
             throw new StoreException("Could not retrieve articles", e);
         }
     }
     
     @Override
     public List<EntityKey> getArticlesOrderedByDate(WorkingSet workingSet) throws StoreException {
     	if (workingSet == null) {
             throw new IllegalArgumentException("Parameter workingSet must not be null");
         }
     	if (workingSet.getSites() == null || workingSet.getSites().isEmpty()) {
     		return new ArrayList<EntityKey>();
     	}
         try {
         	return buildPathList(
         			getArticlesOfSitesOrderedByPublication(
         					EntityKey.extractNames(
         							workingSet.getSites())));
         } catch (RepositoryException e) {
             throw new StoreException("Could not retrieve articles", e);
         }
     }
     
     public List<EntityKey> getRelatedArticlesOrderedByRelevance(String articleKey, WorkingSet workingSet) throws StoreException {
     	// TODO Auto-generated method stub
     	return null;
     }
     
     @Override
     public List<TextBlock> getContent(String articlePath) throws StoreException {
         if (articlePath == null) {
             throw new IllegalArgumentException("Parameter path must not be null");
         }
         Node articleNode = null;
         try {
             articleNode = getArticleNodeByPath(articlePath);
         } catch (RepositoryException e) {
             throw new StoreException("Could not retrieve article node", e);
         }
         if (articleNode == null) {
             throw new StoreException("Unknown article");
         }
         List<TextBlock> content = new ArrayList<TextBlock>();
         try {
             NodeIterator iter = articleNode.getNodes(NODE_CONTENT);
             while (iter.hasNext()) {
                 Node textNode = iter.nextNode();
                 content.add(new TextBlock(textNode.getProperty(PROP_CONTENT).getString(), (int) textNode.getProperty(PROP_WORDS).getLong()));
             }
         } catch (RepositoryException e) {
             throw new StoreException("Could not retrieve text blocks of article", e);
         }
         return content;
     }
 }
