 /* Copyright 2010, OpenPlans
  
  This program is free software: you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, either version 3 of
  the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>. */
 
 package org.openplans.delayfeeder;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Set;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.openplans.delayfeeder.feed.RouteFeed;
 import org.openplans.delayfeeder.feed.RouteFeedItem;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.SessionHolder;
 import org.springframework.transaction.support.TransactionSynchronizationManager;
 
 import com.sun.jersey.api.spring.Autowire;
 import com.sun.jersey.api.json.JSONWithPadding;
 import com.sun.syndication.feed.synd.SyndCategory;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 @Path("/status")
 @XmlRootElement
 @Autowire
 public class RouteStatus {
 	private static final Logger logger = Logger.getLogger(RouteStatus.class);
 
 	private static final long FEED_UPDATE_FREQUENCY = 15 * 60 * 1000;
 	private SessionFactory sessionFactory;
 
 	@Autowired
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	/**
 	 * Main entry point for the route status server
 	 */
 	@GET
 	@Produces("application/x-javascript")
 	public JSONWithPadding getStatus(
 			@QueryParam("route") List<String> routes,
 			@QueryParam("callback") String callback) {
 
 		RouteStatusResponse response = new RouteStatusResponse();
 
 		try {
 			Session session = sessionFactory.getCurrentSession();
 			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
 			
 			session.beginTransaction();
 			
 			response.items = new ArrayList<RouteStatusItem>(routes.size());
 			for (String routeId : routes) {
 				String[] parts = routeId.split(",");
 
 				if(parts.length != 2)
 					continue;
 					
 				String agency = parts[0];
 				String route = parts[1];
 
 				if(agency == null || route == null)
 					continue;
 				
 				/* get data from cache */
 				RouteFeedItem item = getLatestItem(agency, route, session);
 				RouteStatusItem status = new RouteStatusItem();
 				response.items.add(status);
 				
 				/* serve data */
 				status.agency = agency;
 				status.route = route;
 				if (item != null) {
 					status.status = item.title;
 					status.date = item.date;
 					status.link = item.link;
 					status.category = item.category;
 				}
 			}
 			
 			session.flush();
 			session.getTransaction().commit();
 		} finally {
 			TransactionSynchronizationManager.unbindResource(sessionFactory);			
 		}
 		
 		return new JSONWithPadding(response,callback);
 	}
 
 	private RouteFeedItem getLatestItem(String agency, String route, Session session) {
 		RouteFeed feed = getFeed(agency, route, session);
 		if (feed == null) {
 			return null;
 		}
 
 		GregorianCalendar now = new GregorianCalendar();
 		if (feed.lastFetched == null
 				|| now.getTimeInMillis() - feed.lastFetched.getTimeInMillis() > FEED_UPDATE_FREQUENCY) {
 
 			try {
 				refreshFeed(feed, session);
 			} catch (Exception e) {
 				e.printStackTrace();
 				logger.warn(e.fillInStackTrace());
 			}
 		}
 
 		Set<RouteFeedItem> list = feed.items;
 
 		if (list.size() == 0) {
 			return null;
 		}
 		RouteFeedItem item = (RouteFeedItem)list.toArray()[0];
 		return item;
 	}
 
 	private void refreshFeed(RouteFeed feed, Session session) throws IllegalArgumentException,
 			FeedException, IOException {
		
		// flush existing items
		for(RouteFeedItem item : feed.items)
		{
			session.delete(item);
		}
		
		feed.items.clear();
		
 		URL url = new URL(feed.url);
 
 		SyndFeedInput input = new SyndFeedInput();
 		SyndFeed inFeed = input.build(new XmlReader(url));
 
 		Date lastEntry = null;
 		if(feed.lastEntry != null) 
 			lastEntry = feed.lastEntry.getTime();
 		
 		for (Object obj : inFeed.getEntries()) {
 			SyndEntry entry = (SyndEntry) obj;
 			Date date = entry.getPublishedDate();
 			if (lastEntry == null || date.getTime() > lastEntry.getTime()) {
 				lastEntry = date;
 
 				RouteFeedItem item = new RouteFeedItem();
 				item.date = new GregorianCalendar();
 				item.date.setTimeInMillis(date.getTime());
 				item.title = entry.getTitle();
 				
 				StringBuilder categoryStringBuilder = new StringBuilder();
 				@SuppressWarnings("unchecked")
 				List<SyndCategory> entryCategories = entry.getCategories();
 				if(entryCategories != null) {
 					for(SyndCategory category : entryCategories) {
 						categoryStringBuilder.append(category.getName());
 					}
 				}
 				item.category = categoryStringBuilder.toString();
 				
 				item.link = entry.getLink();
 				item.feed = feed;
 				feed.items.add(item);
 				session.save(item);
 			}
 		}
 		if(feed.lastEntry == null) {
 			feed.lastEntry = new GregorianCalendar();
 		}
 		if (lastEntry != null) {
 			feed.lastEntry.setTime(lastEntry);
 		}
 		if(feed.lastFetched == null) {
 			feed.lastFetched = new GregorianCalendar();
 		}
 		feed.lastFetched.setTime(new Date());
 		
 		session.save(feed);
 	}
 
 	private RouteFeed getFeed(String agency, String route, Session session) {
 
 		Query query = session
 				.createQuery("from RouteFeed where agency = :agency and route = :route");
 		query.setParameter("agency", agency);
 		query.setParameter("route", route);
 		@SuppressWarnings("rawtypes")
 		List list = query.list();
 		if (list.size() == 0) {
 			return null;
 		}
 		RouteFeed feed = (RouteFeed) list.get(0);
 		return feed;
 	}
 
 }
