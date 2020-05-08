 package nz.co.searchwellington.repositories;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import nz.co.searchwellington.model.CalendarFeed;
 import nz.co.searchwellington.model.CommentFeed;
 import nz.co.searchwellington.model.DiscoveredFeed;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.ResourceImpl;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.Twit;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.model.Watchlist;
 import nz.co.searchwellington.model.Website;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.joda.time.DateTime;
 
 
 public abstract class HibernateResourceDAO extends AbsractResourceDAO implements ResourceRepository {
 
     SessionFactory sessionFactory;
     private TagDAO tagDAO;
     private TweetDAO tweetDAO;
     
     public HibernateResourceDAO() {
     }
     
     
     public HibernateResourceDAO(SessionFactory sessionFactory, TagDAO tagDAO, TweetDAO twitterDAO) {     
         this.sessionFactory = sessionFactory;
         this.tagDAO = tagDAO;
         this.tweetDAO = twitterDAO;
     }
     
     
     @SuppressWarnings("unchecked")
     public List<Integer> getAllResourceIds() {
         Set<Integer> resourceIds = new HashSet<Integer>();        
         Session session = sessionFactory.getCurrentSession();
         return session.createQuery("select id from nz.co.searchwellington.model.ResourceImpl order by id DESC").setFetchSize(100).list();        
        
     }
     
     
     @Override
 	public Tag createNewTag() {
 		return tagDAO.createNewTag();
 	}
 
 
 	public List<String> getPublisherNamesByStartingLetters(String q) {
          Session session = sessionFactory.getCurrentSession();
          List<String> rows = session.createQuery("select name from nz.co.searchwellington.model.ResourceImpl where type='W' and name like ? order by name").setString(0, q + '%').setMaxResults(50).list();        
          return rows;
 	}
 
 
 	@SuppressWarnings("unchecked")
     final public List<Feed> getAllFeeds() {
         return sessionFactory.getCurrentSession().createCriteria(Feed.class).
         addOrder(Order.desc("latestItemDate")).
         addOrder(Order.asc("name")).
         setCacheable(true).
         list();    
     }
 	
 	
 	
 
 
 	@SuppressWarnings("unchecked")
     final public List<Feed> getAllFeedsByName() {
     	  return sessionFactory.getCurrentSession().createCriteria(Feed.class).      
           addOrder(Order.asc("name")).
           setCacheable(true).
           list();
 	}
 
 
 	@SuppressWarnings("unchecked")
     final public List<Feed> getFeedsToRead() {
         return sessionFactory.getCurrentSession().createCriteria(Feed.class).
         add(Restrictions.ne("acceptancePolicy", "ignore")).
         addOrder(Order.asc("lastRead")).
         setCacheable(false).
         list();
     }
     
     
     
     @SuppressWarnings("unchecked")
     final public List<Resource> getAllCalendarFeeds() {
         return sessionFactory.getCurrentSession().createCriteria(CalendarFeed.class).       
         addOrder(Order.asc("name")). 
         setCacheable(true).
         list();    
     }
     
     
     
     
     
     @SuppressWarnings("unchecked")
     final public List<Resource> getAllWebsites() {
         return sessionFactory.getCurrentSession().createCriteria(Website.class).       
         addOrder(Order.asc("name")). 
         setCacheable(true).
         list();
     }
     
     
     
         
     @SuppressWarnings("unchecked")
     // TODO add discovered timestamp and order by that.
     final public List<DiscoveredFeed> getAllDiscoveredFeeds() {
         return sessionFactory.getCurrentSession().createCriteria(DiscoveredFeed.class).
         setCacheable(true).
         addOrder(Order.desc("id")).
         list();                
     }
     
     
     
     
     @SuppressWarnings("unchecked")
     final public List<Feed> getPublisherFeeds(Website publisher) {             
         return sessionFactory.getCurrentSession().createCriteria(Feed.class).
         add(Restrictions.eq("publisher", publisher)).
         addOrder(Order.asc("name")).
         list();
     }
   
     @SuppressWarnings("unchecked")  
     final public List<Watchlist> getPublisherWatchlist(Website publisher) {    
         return sessionFactory.getCurrentSession().createCriteria(Watchlist.class).
                 add(Restrictions.eq("publisher", publisher)).
                 addOrder(Order.asc("name")).
                 list();
     }
     
     
     @SuppressWarnings("unchecked")
 	@Override
 	public List<Newsitem> getNewsitemsForFeed(Feed feed) {
     	return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
     		add(Restrictions.eq("feed", feed)).
     		addOrder(Order.desc("date")).
     		list();
     }
 	
 
 	@SuppressWarnings("unchecked")  
     final public List<Resource> getOwnedBy(User owner, int maxItems) {    
         return sessionFactory.getCurrentSession().createCriteria(Resource.class).
                 add(Restrictions.eq("owner", owner)).
                 addOrder(Order.desc("date")).
                 addOrder(Order.desc("id")).
                 setMaxResults(maxItems).
                 list();
     }
     
     
     
    
     
     
     @SuppressWarnings("unchecked")
     public List<Newsitem> getRecentUntaggedNewsitems() {
         return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
                 add(Restrictions.isEmpty("tags")).
                 add(Expression.eq("httpStatus", 200)).
                 addOrder(Order.desc("date")).
                 setMaxResults(12).
                 setCacheable(true).list();        
     }
 
     
     @SuppressWarnings("unchecked")
     public List<Resource> getAllPublishersMatchingStem(String stem, boolean showBroken) {
         List<Resource> allPublishers = new ArrayList<Resource>();
         if (showBroken) {
             allPublishers = sessionFactory.getCurrentSession().createCriteria(Website.class).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list();
         } else { 
             allPublishers = sessionFactory.getCurrentSession().createCriteria(Website.class).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).add(Expression.eq("httpStatus", 200)).addOrder(Order.asc("name")).list();            
         }               
         return allPublishers;
     }
     
     
     
     
     
     @SuppressWarnings("unchecked")
     public List<Resource> getNewsitemsMatchingStem(String stem) {    
         return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list();        
     }
     
     
     
     
    
     @SuppressWarnings("unchecked")
     public List<Resource> getNotCheckedSince(Date oneMonthAgo, int maxItems) {     
         return sessionFactory.getCurrentSession().createCriteria(Resource.class).
         add(Restrictions.lt("lastScanned", oneMonthAgo)).addOrder(Order.asc("lastScanned")).
         setMaxResults(maxItems).list();       
     }
     
     
         
     
     @Override
 	public List<Resource> getNotCheckedSince(Date launchedDate, Date lastScanned, int maxItems) {
     	   return sessionFactory.getCurrentSession().createCriteria(Resource.class).
     	   add(Restrictions.gt("liveTime", launchedDate)).
            add(Restrictions.lt("lastScanned", lastScanned)).
            addOrder(Order.asc("lastScanned")).
            setMaxResults(maxItems).list();       
 	}
 
 
 	@SuppressWarnings("unchecked")
     public List<CommentFeed> getCommentFeedsToCheck(int maxItems) {
         return sessionFactory.getCurrentSession().createCriteria(CommentFeed.class).
         addOrder(Order.desc("lastRead")).
         setCacheable(false).
         setMaxResults(maxItems).
         list();
     }
 
     
     @SuppressWarnings("unchecked")
     public List<Resource> getAllResources() {
         return sessionFactory.getCurrentSession().createCriteria(Resource.class).list();   
     }
 
 
     public List<Tag> getAllTags() {        
         return tagDAO.getAllTags();      
     }
     
    
     public List<Tag> loadTagsById(List<Integer> tagIds) {
 		return tagDAO.loadTagsById(tagIds);		
 	}
 
     public void saveTag(Tag editTag) {
         tagDAO.saveTag(editTag);
     }
 
     @SuppressWarnings("unchecked")
     public List<Tag> getTopLevelTags() {
     	return tagDAO.getTopLevelTags();
     }
     
    
     @SuppressWarnings("unchecked")
     public List<Resource> getTwitterMentionedNewsitems(int maxItems) {
     	 return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
     	 	setMaxResults(maxItems).
          	add(Restrictions.isNotEmpty("reTwits")).
          	addOrder(Order.desc("date")).         
          	setCacheable(true).
          	list();
     }
     
     
     
 
 
     
     @SuppressWarnings("unchecked")
     public List<Resource> getRecentlyChangedWatchlistItems() {       
         return sessionFactory.getCurrentSession().createCriteria(Watchlist.class)
             .add(Restrictions.sqlRestriction(" last_changed > DATE_SUB(now(), INTERVAL 7 DAY) "))
             .addOrder(Order.desc("lastChanged"))
             .setCacheable(true).
             list();        
     }
 
 
 	public int getCommentCount() {
         // TODO implement show broken logic if the parent newsitem is broken
         return ((Long) sessionFactory.getCurrentSession().
         		iterate("select count(*) from Comment").
         		next()).intValue();
     }
     
 
     public boolean isResourceWithUrl(String url) {
         Resource existingResource = loadResourceByUrl(url);               
         return existingResource != null;
     }
 
 
     public Resource loadResourceById(int resourceID) {
     	return (Resource) sessionFactory.getCurrentSession().get(ResourceImpl.class, resourceID);        
     }
 
     
     public Resource loadResourceByUrl(String url) {
         return (Resource) sessionFactory.getCurrentSession().createCriteria(Resource.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();        
     }
     
     
     
 	public Website getPublisherByUrlWords(String urlWords) {
 		return (Website) sessionFactory.getCurrentSession().createCriteria(Website.class).add(Expression.eq("urlWords", urlWords)).setMaxResults(1).uniqueResult();    		
 	}
 	
 	public Website getPublisherByName(String name) {
 		return (Website) sessionFactory.getCurrentSession().createCriteria(Website.class).add(Expression.eq("name", name)).setMaxResults(1).uniqueResult();    		
 	}
 
 	public Feed loadFeedByUrlWords(String urlWords) {
 		return (Feed) sessionFactory.getCurrentSession().createCriteria(Feed.class).add(Expression.eq("urlWords", urlWords)).setMaxResults(1).uniqueResult();
 	}
         
     final public Resource loadResourceByUniqueUrl(String url) {
         return (Resource) sessionFactory.getCurrentSession().createCriteria(Resource.class).add(Expression.eq("url", url)).uniqueResult();        
     }
     
     
     
     public CommentFeed loadCommentFeedByUrl(String url) {
         return (CommentFeed) sessionFactory.getCurrentSession().createCriteria(CommentFeed.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();  
     }
     
     
     
     public DiscoveredFeed loadDiscoveredFeedByUrl(String url) {
         return (DiscoveredFeed) sessionFactory.getCurrentSession().createCriteria(DiscoveredFeed.class).
         add(Expression.eq("url", url)).
         setMaxResults(1).
         setCacheable(true).
         uniqueResult();  
     }
     
     
     public Tag loadTagById(int tagID) {
       return tagDAO.loadTagById(tagID);
     }
         
     public Tag loadTagByName(String tagName) {
         return tagDAO.loadTagByName(tagName);
     }
         
     
     public List<Twit> getAllTweets() {
 		return tweetDAO.getAllTweets();
 	}
     
     public void saveTweet(Twit twit) {     
     	tweetDAO.saveTwit(twit);
     }
         
 	public Twit loadTweetByTwitterId(Long twitterId) {
     	 return tweetDAO.loadTweetByTwitterId(twitterId);  
 	}
 
 	
 	
 
 
 
 	public void saveResource(Resource resource) {
 		if (resource.getType().equals("N")) {
 			if (((Newsitem) resource).getImage() != null) {
 				sessionFactory.getCurrentSession().saveOrUpdate(((Newsitem) resource).getImage());
 			}
 	}
 		
         sessionFactory.getCurrentSession().saveOrUpdate(resource);
         sessionFactory.getCurrentSession().flush();
        if (resource.getType().equals("F")) {
             // TODO can this be done for just the publisher only?
        	sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds");
        }
      
         // TODO for related tags, can we be abit more subtle than this?
         // Clear related tags query.
        // sessionFactory.evictQueries();
     }
 
     
     
     public void saveDiscoveredFeed(DiscoveredFeed discoveredFeed) {
         sessionFactory.getCurrentSession().saveOrUpdate(discoveredFeed);
         sessionFactory.getCurrentSession().flush();
     }
     
     
     public void saveCommentFeed(CommentFeed commentFeed) {
         sessionFactory.getCurrentSession().saveOrUpdate(commentFeed);
         sessionFactory.getCurrentSession().flush();
     }
 
 
 
  
     
     @SuppressWarnings("unchecked")
     public List<Newsitem> getLatestTwitteredNewsitems(int number, boolean showBroken) {        
         return criteriaForLatestNewsitems(number, showBroken).
         add(Expression.isNotNull("submittingTwit")).
         setCacheable(true).list();    
     }
     
     private Criteria criteriaForLatestNewsitems(int number, boolean showBroken) {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
             addOrder(Order.desc("date")).
             addOrder(Order.desc("id")).
             setMaxResults(number);
         if (!showBroken) {
             criteria.add(Expression.eq("httpStatus", 200));
         }
         return criteria;
     }
     
     
     @SuppressWarnings("unchecked")
     public List<Resource> getLatestWebsites(int maxNumberOfItems, boolean showBroken) {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Website.class).addOrder( Order.desc("date"));
         if (!showBroken) {
             criteria.add(Expression.eq("httpStatus", 200));
         }
         return criteria.setCacheable(true).
         setMaxResults(maxNumberOfItems).
         list();
     }
 
     
    
      
   
     
     
     @SuppressWarnings("unchecked")
     public List<Resource> getTaggedResources(Tag tag, int max_newsitems) {
         return sessionFactory.getCurrentSession().createCriteria(Resource.class).createCriteria("tags").add(Restrictions.eq("id", tag.getId())).list();
     }
     
    
             
     public void deleteResource(Resource resource) {
         sessionFactory.getCurrentSession().delete(resource);       
         // flush collection caches.  
         sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.newsitems");
         sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds");
         sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.watchlist");
         sessionFactory.evictCollection("nz.co.searchwellington.model.DiscoveredFeed.references");
         sessionFactory.getCurrentSession().flush();
     }
 
 
     public void deleteTag(Tag tag) {
         sessionFactory.getCurrentSession().delete(tag);
         sessionFactory.getCurrentSession().flush();
     }
 
 
     public List<Tag> getTagsMatchingKeywords(String keywords) {
         throw(new UnsupportedOperationException());
     }
 
 
     
 
     
        
     @SuppressWarnings("unchecked")
     public Date getNewslogLastChanged() {        
         Criteria latestNewsitemsCriteria = criteriaForLatestNewsitems(20, false);
         latestNewsitemsCriteria.addOrder( Order.desc("liveTime"));
         List<Resource> currentNewsitems = latestNewsitemsCriteria.setCacheable(true).list();        
         for (Resource resource : currentNewsitems) {           
         	DateTime latestChange = null;
             if (latestChange == null) {
                 latestChange = new DateTime(resource.getLastChanged());
                 log.debug("Setting last changed to: " + latestChange);
             }
             if (resource.getLastChanged() != null && new DateTime(resource.getLastChanged()).isAfter(latestChange)) {
                 latestChange = new DateTime(resource.getLastChanged());                
                 return latestChange.toDate();
             }
         }
         return null;
     }
 
 
     
     @SuppressWarnings("unchecked")
 	public List<Resource> getResourcesWithTag(Tag tag) {
     	log.info(tag.getName());
     	Criteria taggedResources = sessionFactory.getCurrentSession().createCriteria(Resource.class).
     		addOrder(Order.desc("date")).
     		addOrder(Order.desc("id")).
     		createAlias("tags", "rt").
     		add(Restrictions.eq("rt.id", tag.getId()));
     	
     	return taggedResources.list();
 	}
 
 
 	@Override
 	public Newsitem loadNewsitemBySubmittingTwitterId(long twitterId) {		
 		Criteria criteria = sessionFactory.getCurrentSession().
 			createCriteria(Newsitem.class).
 			createAlias("submittingTwit", "st").
 			add(Restrictions.eq("st.twitterid", twitterId));		
 		return (Newsitem) criteria.uniqueResult();
 	}
 
 }
