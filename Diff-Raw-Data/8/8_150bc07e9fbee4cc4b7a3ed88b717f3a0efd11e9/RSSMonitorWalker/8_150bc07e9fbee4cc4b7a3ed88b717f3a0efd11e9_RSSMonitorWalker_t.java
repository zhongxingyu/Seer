 package br.ufmg.dcc.vod.spiderpig.jobs.youtube.rss;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.log4j.Logger;
 
 import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
 import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
 import br.ufmg.dcc.vod.spiderpig.master.walker.AbstractWalker;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.NeverEndingCondition;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.gdata.client.youtube.YouTubeService;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.youtube.VideoEntry;
 import com.google.gdata.data.youtube.VideoFeed;
 
 public class RSSMonitorWalker extends AbstractWalker {
 
 	private static final Logger LOG = Logger.getLogger(RSSMonitorWalker.class);
 	
 	public static final String MAX_MONITOR = 
 			"master.walkstrategy.youtube.rss.max";
 	public static final String TIME_BETWEEN = 
 			"master.walkstrategy.youtube.rss.time";
 	public static final String FEED = 
 			"master.walkstrategy.youtube.rss.feeds";
 	
 	private int maxMonitor;
 	private long timeBetween;
 	private List<String> feeds;
 	
 	private LinkedHashSet<CrawlID> monitoredIds;
	private HashSet<CrawlID> dispatched;
 	private List<CrawlID> toCrawlList;
 	
 	private ThroughputManager throughputManager;
 	private Requester<List<CrawlID>> requester;
 
 	
 	public RSSMonitorWalker() {
 		this.monitoredIds = new LinkedHashSet<>();
 		this.dispatched = new HashSet<>();
 		this.toCrawlList = new ArrayList<>();
 		this.maxMonitor = 0;
 		this.feeds = null;
 		this.timeBetween = 0;
 		this.throughputManager = null;
 	}
 	
 	@Override
 	protected List<CrawlID> getToWalkImpl(CrawlID crawled, List<CrawlID> links) {
 		this.dispatched.remove(crawled);
 		return getToWalk();
 	}
 
 	private List<CrawlID> getToWalk() {
 		
 		//adds new elements until it reaches totalMonitor
 		if (this.toCrawlList.size() < this.maxMonitor) {
 			try {
 				if (this.dispatched.isEmpty()) {
 					List<CrawlID> links = 
 							this.throughputManager.sleepAndPerform(null, 
 									this.requester);
 					this.monitoredIds.addAll(links);
 					this.dispatched.addAll(links);
 					this.toCrawlList = ImmutableList.copyOf(this.monitoredIds);
 					return this.toCrawlList;
 				} else {
 					return Collections.emptyList();
 				}
 			} catch (Exception e) {
 				LOG.error("Unable to parse feeds " + this.feeds, e);
 				return Collections.emptyList();
 			}
 		} else {
 			return this.toCrawlList;
 		}
 		
 	}
 
 	@Override
 	protected void errorReceivedImpl(CrawlID crawled) {
 		this.dispatched.remove(crawled);
 	}
 	
 	@Override
 	protected List<CrawlID> filterSeeds(List<CrawlID> seeds) {
 		try {
 			return getToWalk();
 		} catch (Exception e) {
 			LOG.error("Unable to dispatch seeds " + this.feeds, e);
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override
 	public Set<String> getRequiredParameters() {
 		return new HashSet<String>(Arrays.asList(MAX_MONITOR, TIME_BETWEEN,
 				FEED));
 	}
 
 	@Override
 	protected StopCondition createStopCondition() {
 		return new NeverEndingCondition();
 	}
 	
 	@Override
 	public Void realConfigurate(Configuration configuration) throws Exception {
 		
 		this.maxMonitor = configuration.getInt(MAX_MONITOR);
 		this.timeBetween = configuration.getLong(TIME_BETWEEN);
 		this.throughputManager = new ThroughputManager(this.timeBetween);
 		
 		String[] feedsArray = configuration.getStringArray(FEED);
 		
 		this.feeds = Lists.newArrayList(feedsArray);
 		this.requester = new FeedRequester();
 		
 		return null;
 	}
 	
 	private class FeedRequester implements Requester<List<CrawlID>> {
 		@Override
 		public List<CrawlID> performRequest(String nullString) 
 				throws Exception {
 			
 			List<CrawlID> returnVal = new ArrayList<>();
 			YouTubeService service = new YouTubeService("");
 			for (String feed : RSSMonitorWalker.this.feeds) {
 				LOG.info("Parsing feed " + feed);
 				String[] feedSplit = feed.split("/");
 				String feedType = feedSplit[feedSplit.length - 1];
 				
 				URL feedLink = new URL(feed);
 				while (feedLink != null) {
 					VideoFeed videoFeed = service.getFeed(feedLink, 
 							VideoFeed.class);
 					
 					for (VideoEntry entry : videoFeed.getEntries()) {
 						String[] vidIdSplit = entry.getId().split(":");
 						String vidId = vidIdSplit[vidIdSplit.length - 1];
 						LOG.info("Found video " + vidId);
 						returnVal.add(CrawlID.newBuilder().
 								setId(vidId).
 								setResourceType(feedType).
 								build());
 					}
 						
 					Link nextLink = videoFeed.getLink("next", 
 	                		"application/atom+xml");
 					if (nextLink != null) {
 						feedLink = new URL(nextLink.getHref());
 					} else {
 						feedLink = null;
 					}
 				}
 			}
 
 			return returnVal;
 		}
 	}
 }
