 package nz.co.searchwellington.repositories;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import nz.co.searchwellington.dates.DateFormatter;
 import nz.co.searchwellington.model.ArchiveLink;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.PublisherContentCount;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.Website;
 import nz.co.searchwellington.model.decoraters.highlighting.SolrHighlightingNewsitemDecorator;
 import nz.co.searchwellington.model.decoraters.highlighting.SolrHighlightingWebsiteDecorator;
 import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
 import nz.co.searchwellington.repositories.solr.SolrQueryService;
 
 import org.apache.log4j.Logger;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.SolrQuery.ORDER;
 import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
 import org.apache.solr.client.solrj.response.FacetField;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.client.solrj.response.FacetField.Count;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 public class SolrBackedResourceDAO {
 
     private static final int MAXIMUM_NEWSITEMS_ON_MONTH_ARCHIVE = 1000;
 
 	static Logger log = Logger.getLogger(SolrBackedResourceDAO.class);
     
     private SolrQueryService solrQueryService;
     private ResourceRepository resourceDAO;
     private TagDAO tagDAO;
     private SolrResourceHydrator resourceHydrator;
     
     private String solrUrl;
 
     
 	public SolrBackedResourceDAO(SolrQueryService solrQueryService, ResourceRepository resourceDAO, TagDAO tagDAO, SolrResourceHydrator resourceHydrator) {		
 		this.solrQueryService = solrQueryService;
 		this.resourceDAO = resourceDAO;
 		this.tagDAO = tagDAO;
 		this.resourceHydrator = resourceHydrator;
 	}
 
 	public void setSolrUrl(String solrUrl) {
 		this.solrUrl = solrUrl;
 	}
 	
 	public List<Resource> getAllFeeds(boolean showBroken, boolean orderByLatestItemDate) {
 		SolrQuery query = new SolrQueryBuilder().type("F").showBroken(showBroken).maxItems(500).toQuery();
 		
 		if (orderByLatestItemDate) {
 			setFeedLatestItemOrder(query);
 		} else {
 			setTitleSortOrder(query);
 		}
     	return getQueryResults(query);
 	}
 
 	public List<Resource> getTaggedNewsitems(Set<Tag> name, boolean showBroken, int maxItems) {
 		return getTaggedNewsitems(name, showBroken, 0, maxItems);
 	}
 	
 	
 	public int getTaggedNewitemsCount(Tag tag, boolean showBroken) {
 		log.info("Getting newsitem count for tag: " + tag);		
 		Set<Tag> tags = new HashSet<Tag>();
     	tags.add(tag);
     	SolrQuery query = getTaggedContentSolrQuery(tags, showBroken, "N");
     	return getQueryCount(query);
 	}
 	
 
 	public int getCommentedNewsitemsCount(boolean showBroken) {
 		log.info("Getting commented newsitem count");
 		SolrQuery query = getCommentedNewsitemsQuery(showBroken);
 		return getQueryCount(query);
 	}
 
 	
 	public List<Resource> getRecentCommentedNewsitemsForTag(Tag tag, boolean showBroken, int maxItems) {
 		log.info("Getting recent commented newsitem count");
 		// TODO duplication - with what?
 		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type("N").tag(tag).dateRange(14).commented(true).toQuery();					
 		setDateDescendingOrder(query);
 		query.setRows(maxItems);
 		return getQueryResults(query);
 	}
 
 	public int getCommentedNewsitemsForTagCount(Tag tag, boolean showBroken) {
 		log.info("Getting commented newsitem count for tag: " + tag);
 		SolrQuery query = getCommentedNewsitemsForTagQuery(tag, showBroken);
 		return getQueryCount(query);	
 	}
 	
 	public List<Resource> getTaggedWebsites(Tag tag, boolean showBroken, int maxItems) {		
 		Set<Tag> tags = new HashSet<Tag>();
 		tags.add(tag);
 		return getTaggedWebsites(tags, showBroken, maxItems);
 	}
 	
 		
 	public List<Resource> getTaggedGeotaggedNewsitems(Tag tag, int maxItems, boolean showBroken) {
 		log.info("Getting geotagged newsitems for tag: " + tag );
 		SolrQuery query = new SolrQueryBuilder().tag(tag).type("N").geotagged().showBroken(showBroken).maxItems(maxItems).toQuery();
 		setDateDescendingOrder(query);;
 		return getQueryResults(query);
 	}
 
 	
 	
 	
 	public List<Resource> getTaggedWebsites(Set<Tag> tags, boolean showBroken, int maxItems) {
 		log.info("Getting websites for tags: " + tags );
 		SolrQuery query = new SolrQueryBuilder().tags(tags).type("W").showBroken(showBroken).maxItems(maxItems).toQuery();
 		setTitleSortOrder(query);
 		return getQueryResults(query);
 	}
 	
 	public Date getLastLiveTimeForTag(Tag tag) {
 		SolrQuery latestItemForTagQuery = new SolrQueryBuilder().tag(tag).showBroken(false).maxItems(1).toQuery();
 		latestItemForTagQuery.setSortField("lastLive", ORDER.desc);
 		List<Resource> resources = getQueryResults(latestItemForTagQuery);
 		if (resources.size() == 1) {
 			return resources.get(0).getLiveTime();
 		}				
 		return null;
 	}
 	
 
 	
 	public List<Resource> getTagWatchlist(Tag tag, boolean showBroken) {
 		log.info("Getting watchlist for tag: " + tag);
 		SolrQuery query = new SolrQueryBuilder().tag(tag).type("L").showBroken(showBroken).toQuery();
 		setTitleSortOrder(query);
 		return getQueryResults(query);
 	}
 	
 	
 	public List<Resource> getLatestWebsites(int maxItems, boolean showBroken) {
 		SolrQuery query = new SolrQueryBuilder().type("W").showBroken(showBroken).maxItems(maxItems).toQuery();
 		setDateDescendingOrder(query);
 		return getQueryResults(query);
 	}
 	
 	
 
 	public List<Resource> getTaggedFeeds(Tag tag, boolean showBroken) {
 		log.info("Getting feeds for tag: " + tag);
 		SolrQuery query = new SolrQueryBuilder().tag(tag).type("F").showBroken(showBroken).toQuery();
 		setTitleSortOrder(query);
 		return getQueryResults(query);
 	}
 	
 	
 	public List<Resource> getCommentedNewsitems(int maxItems, boolean showBroken, boolean hasComments, int startIndex) {	
 		SolrQuery query = getCommentedNewsitemsQuery(showBroken);
 		setDateDescendingOrder(query);;
 		query.setRows(maxItems);
 		query.setStart(startIndex);		
 		return getQueryResults(query);		    	
 	}
 	
 	
 	
 	public List<Resource> getRecentTwitteredNewsitems(int maxItems, boolean showBroken) {
 		SolrQuery query = new SolrQueryBuilder().type("N").
 			showBroken(showBroken).
 			dateRange(60).
 			twitterCount(2).
 			maxItems(maxItems).toQuery();
 	
 		setDateDescendingOrder(query);
 		return getQueryResults(query);
 	}
 	
 	
 	public List<Resource> getRecentTwitteredNewsitemsForTag(int maxItems, boolean showBroken, Tag tag) {
 		SolrQuery query = new SolrQueryBuilder().type("N").
 			showBroken(showBroken).
 			dateRange(60).
 			tag(tag).
 			twitterCount(2).
 			maxItems(maxItems).toQuery();
 	
 		setDateDescendingOrder(query);
 		return getQueryResults(query);
 	}
 
 	public List<Resource> getPublisherTagCombinerNewsitems(Website publisher, Tag tag, boolean showBroken, int maxItems) {
 		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type("N").tag(tag).maxItems(maxItems).publisher(publisher).toQuery();			
 		setDateDescendingOrder(query);
 		return getQueryResults(query);
 	}
 	
 	
 	public List<Resource> getPublisherFeeds(Website publisher, boolean showBroken) {
 		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type("F").publisher(publisher).toQuery();			
 		setTitleSortOrder(query);
 		return getQueryResults(query);
 	}
 	
 	public List<Resource> getPublisherWatchlist(Website publisher, boolean showBroken) {
 		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type("L").publisher(publisher).toQuery();			
 		setTitleSortOrder(query);
 		return getQueryResults(query);
 	}
 	
 	
 	public List<Resource> getCommentedNewsitemsForTag(Tag tag, boolean showBroken, int maxItems, int startIndex) {	
 		SolrQuery query = getCommentedNewsitemsForTagQuery(tag, showBroken);			
 		setDateDescendingOrder(query);;
 		query.setStart(startIndex);
 		query.setRows(maxItems);
 		return getQueryResults(query);
 	}
 	
 
 	public List<ArchiveLink> getArchiveMonths(boolean showBroken) {
 		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type("N").toQuery();
 		query.addFacetField("month");
 		query.setFacetMinCount(1);
 		query.setFacetSort(false);
 		query.setFacetLimit(1000);
 			
 		List<ArchiveLink> archiveLinks = new ArrayList<ArchiveLink>();
 		QueryResponse response = solrQueryService.querySolr(query);
 		if (response != null) {
 			FacetField facetField = response.getFacetField("month");
 			if (facetField != null && facetField.getValues() != null) {
 				log.debug("Found facet field: " + facetField);
 				List<Count> values = facetField.getValues();
 				Collections.reverse(values);
 				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMM");
 				for (Count count : values) {
 					final String monthString = count.getName();					
 					DateTime month = fmt.parseDateTime(monthString);
 					final Long relatedItemCount = count.getCount();
 					archiveLinks.add(new ArchiveLink(month.toDate(), relatedItemCount.intValue()));									
 				}
 			}
 		}			
 		return archiveLinks;
 	}
 
 	
 	// TODO this one is abit odd -should be it's own solr query to include feeds and watchlists in the defination of 'publisher';
 	public List<Resource> getAllPublishers(boolean shouldShowBroken, boolean mustHaveNewsitems) {
 		List <Resource> publishers = new ArrayList<Resource>();
 		for (PublisherContentCount publisherCount : this.getAllPublishersWithNewsitemCounts(shouldShowBroken, mustHaveNewsitems)) {
 			publishers.add(publisherCount.getPublisher());
 		}
 		return publishers;
 	}
 	
 	
 	public List<PublisherContentCount> getAllPublishersWithNewsitemCounts(boolean showBroken, boolean mustHaveNewsitems) {		
 			SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).toQuery();
 			query.addFacetField("publisher");
 			query.setFacetMinCount(1);
 			query.setFacetSort(false);
 			query.setFacetLimit(2000);
 			
 			List<PublisherContentCount> publishers = new ArrayList<PublisherContentCount>();
 			QueryResponse response = solrQueryService.querySolr(query);
 			if (response != null) {
 				FacetField facetField = response.getFacetField("publisher");
 				if (facetField != null && facetField.getValues() != null) {
 					log.debug("Found facet field: " + facetField);
 					List<Count> values = facetField.getValues();			
 					for (Count count : values) {
 						final int relatedPublisherId = Integer.parseInt(count.getName());
 						Website relatedPublisher = (Website) resourceDAO.loadResourceById(relatedPublisherId);
 						if (relatedPublisher != null) {
 							final Long relatedItemCount = count.getCount();
 							publishers.add(new PublisherContentCount(relatedPublisher, relatedItemCount.intValue()));
 						} else {
 							log.warn("Could not find website object for publisher id: " + relatedPublisherId);
 						}
 					}
 				}
 			}
 			Collections.sort(publishers);
 			return publishers;
 	}
 	
 
 	public List<Resource> getNewsitemsForMonth(Date month, boolean showBroken) {	
 		final String monthString = new DateFormatter().formatDate(month, DateFormatter.MONTH_FACET);
 		SolrQuery query = new SolrQueryBuilder().month(monthString).type("N").showBroken(showBroken).toQuery();
 		setDateDescendingOrder(query);;
 		query.setRows(MAXIMUM_NEWSITEMS_ON_MONTH_ARCHIVE);
 		return getQueryResults(query);
 	}
 
 	public int getPublisherNewsitemsCount(Website publisher, boolean showBroken) {
 		log.info("Getting publisher newsitem count for publisher: " + publisher);
 		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type("N").publisher(publisher).toQuery();
 		return getQueryCount(query);
 	}
 	
 	// TODO need a paginating method
 	public List<Resource> getAllWatchlists(boolean showBroken) {
 		// TODO make this limit to things which changed in the last week.
 		SolrQuery query = new SolrQueryBuilder().type("L").maxItems(255).showBroken(showBroken).toQuery();
 		query.setSortField("lastChanged", ORDER.desc);
 		return getQueryResults(query);
 	}
 	
 	
 	
 		
 	public List<Resource> getBrokenSites() {
 		SolrQuery query = new SolrQueryBuilder().type("W").maxItems(255).isBroken().toQuery();
 		query.setSortField("titleSort", ORDER.asc);
 		return getQueryResults(query);
 	}
 	
 
 	public List<Resource> getCalendarFeedsForTag(Tag tag, boolean showBroken) {
 		SolrQuery query = new SolrQueryBuilder().type("C").showBroken(showBroken).toQuery();
 		setTitleSortOrder(query);
 		return getQueryResults(query);
 	}
 
 
 	public List<Resource> getPublisherNewsitems(Website publisher, int maxItems, boolean showBroken) {
 		return getPublisherNewsitems(publisher, maxItems, showBroken, 0);
 	}
 		
 	public List<Resource> getPublisherNewsitems(Website publisher, int maxItems, boolean showBroken, int startIndex) {
		SolrQuery query = new SolrQueryBuilder().type("N").publisher(publisher).showBroken(showBroken).maxItems(maxItems).startIndex(startIndex).toQuery();
 		setDateDescendingOrder(query);
 		return getQueryResults(query);
 	}
 	
 
 	public List<Resource> getTaggedNewsitems(Set<Tag> tags, boolean showBroken, int startIndex, int maxItems) {
 		log.info("Getting newsitems for tags: " + tags + " startIndex: " + startIndex + " maxItems: " + maxItems);		
 		SolrQuery query = new SolrQueryBuilder().type("N").tags(tags).showBroken(showBroken).startIndex(startIndex).maxItems(maxItems).toQuery();
 		setDateDescendingOrder(query);;
     	return getQueryResults(query);
     }
 	
 	
 	private List<Resource> getQueryResults(SolrQuery query) {
 		List<Resource> results = new ArrayList<Resource>();
 		log.debug("Solr query: " + query);
 		QueryResponse response = solrQueryService.querySolr(query);
 		if (response != null) {
 			loadResourcesFromSolrResults(results, response);			
 		}
 		return results;
 	}
 
 	
 	private int getQueryCount(SolrQuery query) {
 		QueryResponse response = solrQueryService.querySolr(query);	
 		if (response !=null) {
 			Long count =  response.getResults().getNumFound();
 			return count.intValue();			
 		} 
 		return 0;		
 	}
 
 	
 
 
 	
 	
 	public List<Resource> getLatestNewsitems(int maxItems, boolean showBroken) {
 		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken).maxItems(maxItems).toQuery();
 		setDateDescendingOrder(query);
 		return getQueryResults(query);
 	}
 
 	
 	
 	
	public Map<String, Integer> getArchiveStatistics(boolean showBroken) { // TODO fails is admin user is logged in - because you can't facet on a null query
		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).toQuery();
 		SolrServer solr;
 		try {
 			solr = new CommonsHttpSolrServer(solrUrl);
 
 			query.addFacetField("type");
 			query.setFacetMinCount(1);
 
 			QueryResponse response = solr.query(query);
 			FacetField facetField = response.getFacetField("type");
 			if (facetField != null && facetField.getValues() != null) {
 				log.info("Found facet field: " + facetField);
 				Map<String, Integer> typeCounts = new HashMap<String, Integer>();
 				List<Count> values = facetField.getValues();
 				for (Count count : values) {
 					String type = (String) count.getName();
 					final Long typeCount = count.getCount();
 					typeCounts.put(type, typeCount.intValue());
 				}
 				return typeCounts;
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SolrServerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		log.warn("returning null");
 		return null;
 
 	}
 	
 	
 
 	
 	public List<Tag> getCommentedTags(boolean showBroken) {
 		List<Integer> tagIds = new ArrayList<Integer>();
 		try {
 			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
 			SolrQuery query = getCommentedNewsitemsQuery(showBroken);			
 			query.addFacetField("tags");
 			query.setFacetMinCount(1);
 			query.setFacetSort(true);
 			
 			QueryResponse response = solr.query(query);
 			FacetField facetField = response.getFacetField("tags");
 			if (facetField != null && facetField.getValues() != null) {
 				log.debug("Found facet field: " + facetField);
 				List<Count> values = facetField.getValues();
 				for (Count count : values) {
 					final int tagId = Integer.parseInt(count.getName());
 					tagIds.add(tagId);
 				}				
 			}
 			
 		} catch (MalformedURLException e) {
 			log.error(e);
 		} catch (SolrServerException e) {
 			log.error(e);
 		}
 		return tagDAO.loadTagsById(tagIds);
 	}
 	
 	
 	public List<Resource> getAllValidGeocoded(int maxItems, boolean showBroken) {
 		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(true).geotagged().maxItems(maxItems).toQuery();
 		setDateDescendingOrder(query);;
 		return getQueryResults(query);
 	}
 	
 	
 	public List<Tag> getGeotaggedTags(boolean showBroken) {
 		List<Integer> tagIds = new ArrayList<Integer>();
 		try {
 			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
 			SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken).geotagged().toQuery();		
 			query.addFacetField("tags");
 			query.setFacetMinCount(1);
 			query.setFacetSort(true);
 			
 			QueryResponse response = solr.query(query);
 			FacetField facetField = response.getFacetField("tags");
 			if (facetField != null && facetField.getValues() != null) {
 				log.debug("Found facet field: " + facetField);
 				List<Count> values = facetField.getValues();
 				for (Count count : values) {
 					final int tagId = Integer.parseInt(count.getName());
 					tagIds.add(tagId);
 				}				
 			}
 			
 		} catch (MalformedURLException e) {
 			log.error(e);
 		} catch (SolrServerException e) {
 			log.error(e);
 		}
 		return tagDAO.loadTagsById(tagIds);
 	}
 
 	private void loadResourcesFromSolrResults(List<Resource> results, QueryResponse response) {
 		SolrDocumentList solrResults = response.getResults();
 	
 		for (SolrDocument result : solrResults) {			
 			Resource resource = resourceHydrator.hydrateResource(result);
 			if (resource != null) {
 				if (response.getHighlighting() != null) {
 					processHighlighting(results, response, resource);
 				} else {
 					results.add(resource);
 				}
 				
 			}		
 		}
 	}
 
 	
 	private void processHighlighting(List<Resource> results, QueryResponse response, Resource resource) {
 		Map<String, List<String>> map = response.getHighlighting().get(resource.getId());
 		if (resource.getType().equals("N") && !map.isEmpty()) {
 			log.debug("Highlighting: " + map);
 			results.add(new SolrHighlightingNewsitemDecorator((Newsitem) resource, map));
 		} else if (resource.getType().equals("W") && !map.isEmpty()) {
 			log.debug("Highlighting: " + map);
 			results.add(new SolrHighlightingWebsiteDecorator((Website) resource, map));
 		} else {
 			results.add(resource);
 		}
 	}
 		
 	private SolrQuery getCommentedNewsitemsQuery(boolean showBroken) {
 		return new SolrQueryBuilder().showBroken(showBroken).type("N").commented(true).toQuery();			
 	}
 	
 	
 	private SolrQuery getCommentedNewsitemsForTagQuery(Tag tag, boolean showBroken) {
 		return new SolrQueryBuilder().showBroken(showBroken).type("N").tag(tag).commented(true).toQuery();
 	}
 	
 	private SolrQuery getTaggedContentSolrQuery(Set<Tag> tags, boolean showBroken, String type) {			
 		return new SolrQueryBuilder().tags(tags).showBroken(showBroken).type(type).toQuery();		
 	}
 	
 
 	private void setDateDescendingOrder(SolrQuery query) {
 		query.setSortField("date", ORDER.desc);
 		query.addSortField("id", ORDER.desc);
 	}
 	
 
 	private void setTitleSortOrder(SolrQuery query) {
 		query.setSortField("titleSort", ORDER.asc);
 	}
 	
 	private void setFeedLatestItemOrder(SolrQuery query) {
 		query.setSortField("feedLatestItemDate", ORDER.desc);
 	}
 
 }
