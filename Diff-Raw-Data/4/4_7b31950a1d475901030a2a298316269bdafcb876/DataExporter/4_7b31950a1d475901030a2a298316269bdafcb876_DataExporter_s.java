 package org.dentleisen.appening2;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import twitter4j.GeoLocation;
 import twitter4j.StatusUpdate;
 import twitter4j.TwitterException;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.feed.synd.SyndImage;
 import com.sun.syndication.feed.synd.SyndImageImpl;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedOutput;
 
 public class DataExporter {
 
 	private static Logger log = Logger.getLogger(DataExporter.class);
 
 	private static final long minMentions = Utils
 			.getCfgInt("appening.twitter.minMentions");
 	private static final long minMentionsDays = Utils
 			.getCfgInt("appening.twitter.minMentionDays");
 
 	private static final String urlPrefix = Utils
 			.getCfgStr("appening.twitter.urlPrefix");
 
 	private static final long interval = Utils
 			.getCfgInt("appening.export.intervalSeconds") * 1000;
 
 	private static final double minRank = Utils
 			.getCfgDbl("appening.twitter.minRank");
 
 	private static final double lastMentionedMins = Utils
 			.getCfgDbl("appening.twitter.lastMentionedMins");
 
 	private static final int numMessages = 20;
 	private static final int numLinks = 10;
 	private static final int numImages = 5;
 	private static final int thumbnailSize = 300;
 
 	private static Set<String> ignoreDomains = new HashSet<String>();
 	static {
 		ignoreDomains.add("foursquare");
 		ignoreDomains.add("twitter");
 		ignoreDomains.add("t.co");
 	}
 
 	// @SuppressWarnings("unchecked")
 	@SuppressWarnings("unchecked")
 	public static void runTask() {
 		log.info("Starting Exporter Run");
 
 		List<PopularPlace> places = Place.loadPopularPlaces(minMentions,
 				minMentionsDays);
 		log.info("Exporting JSON for " + places.size() + " places...");
 
 		JSONArray placesJson = new JSONArray();
 		// JSON Loop
 		for (PopularPlace p : places) {
 			JSONObject placeJson = p.toJSON();
 			JSONArray messagesJson = new JSONArray();
 			JSONArray linksJson = new JSONArray();
 			JSONArray imagesJson = new JSONArray();
 
 			List<Message> recentMessages = p.loadRecentMessages(numMessages,
 					Utils.messageThreshold);
 
 			// check last resolved timestamp for this place
 			Date lastResolved = p.lastResolved();
 			List<Message> msgsToResolve = new ArrayList<Message>();
 			for (Message msg : recentMessages) {
 				if (msg.getCreated().after(lastResolved)) {
 					msgsToResolve.add(msg);
 				}
 				messagesJson.add(msg.toJSON());
 			}
 			Collection<WebResource> newResources = WebResource.resolveLinks(
 					msgsToResolve, p);
 			for (WebResource wr : newResources) {
 				boolean ignore = false;
 				for (String ignoreDomain : ignoreDomains) {
 
 					if (wr.getUrlObj().getHost().contains(ignoreDomain)) {
 						ignore = true;
 					}
 				}
 				if (ignore) {
 					continue;
 				}
 
 				if (wr.isImage()) {
 					File imageFile = wr.downloadImageAndResize(thumbnailSize);
 					String imgUrl = Utils.fileToS3(imageFile, Utils.s3Prefix
 							+ p.id + "-" + wr.getTweeted().getTime() + ".png",
 							"image/png");
 					wr.setImageUrl(imgUrl);
 					imageFile.delete();
 				}
 				wr.save();
 			}
 			p.setResolved();
 
 			List<WebResource> allResources = WebResource.loadResources(p,
 					numLinks, numImages);
 			for (WebResource wr : allResources) {
 				if (wr.isImage()) {
 					imagesJson.add(wr.toJSON());
 				} else {
 					linksJson.add(wr.toJSON());
 				}
 			}
 			placeJson.put("links", linksJson);
 			placeJson.put("images", imagesJson);
 
 			String messagesJsonUrl = Utils.jsonArrToS3(messagesJson,
 					Utils.s3Prefix + p.id + "-messages.json");
 			placeJson.put("messagesUrl", messagesJsonUrl);
 
 			placesJson.add(placeJson);
 		}
 		Utils.jsonArrToS3(placesJson, Utils.s3Prefix + "places.json");
 
 		// Twitter loop
 		log.info("Exporting Twitter");
 		for (PopularPlace p : places) {
 			if (p.rank < minRank) {
 				log.debug("Ignoring " + p.name + " (" + p.id + ") - rank "
 						+ p.rank + " < " + minRank);
 				continue;
 			}
 			if (p.wasMentioned(lastMentionedMins)) {
 				log.debug("Ignoring " + p.name + " (" + p.id
 						+ ") - already mentioned in last " + lastMentionedMins
 						+ " minutes");
 				continue;
 			}
 
 			p.setMentioned();
 			StatusUpdate su = new StatusUpdate("'" + p.name + "' - "
 					+ p.getLink(urlPrefix) + " (" + Math.round(p.rank) + ")");
 			su.setLocation(new GeoLocation(p.lat, p.lng));
 			try {
 				Utils.getTwitter().updateStatus(su);
 			} catch (TwitterException e) {
 				log.warn("Unable to update Twitter", e);
 			}
 		}
 
 		log.info("Exporting RSS");
 		// RSS loop
 		SyndFeed feed = new SyndFeedImpl();
 		feed.setFeedType("rss_2.0");
 		feed.setLink("http://www.appening.at");
 		feed.setTitle("Appening Amsterdam");
 		feed.setDescription("Things happening now in Amsterdam according to Twitter.");
 		SyndImage image = new SyndImageImpl();
 		image.setTitle("Appening Amsterdam");
 
 		// careful when updating webpage...
 		image.setUrl("http://www.appening.at/appening-icon-300.png");
 		feed.setImage(image);
 		List<PopularPlace> rssPlaces = Place.loadLastMentionedPlaces(50);
 		List<SyndEntry> entries = new ArrayList<SyndEntry>();
 
 		for (PopularPlace p : rssPlaces) {
 			SyndEntry entry = new SyndEntryImpl();
 			SyndContent description;
 
 			entry.setTitle(p.name);
 
 			entry.setLink(p.getLink(urlPrefix));
 
 			entry.setPublishedDate(p.lastMentioned);
 			description = new SyndContentImpl();
 			description.setType("text/html");
 
 			String html = "<h2><a href=\"" + entry.getLink() + "\">" + p.name
 					+ "</a></h2>";
 
 			html += "<a href=\"https://maps.google.com/maps?t=m&q=loc:"
 					+ p.lat
 					+ ","
 					+ p.lng
 					+ "&z=13\"><img src=\"http://maps.googleapis.com/maps/api/staticmap?key="
 					+ Utils.getCfgStr("appening.export.googleMapsKey")
 					+ "&sensor=false&zoom=13&size=500x250&format=PNG&maptype=terrain&markers=color:red|label:A|"
 					+ p.lat + "," + p.lng + "\"/></a>";
 
 			html += "<br><div>";
 
 			List<WebResource> allResources = WebResource.loadResources(p,
 					numLinks, numImages);
 			for (WebResource wr : allResources) {
 				if (wr.isImage()) {
 					html += "<a href=\"" + wr.getUrl()
 							+ "\"><img width=\"150px\" src=\""
 							+ wr.getImageUrl() + "\" title=\"" + wr.getTitle()
 							+ "\"/></a>&nbsp;";
 				}
 			}
 
 			html += "</div><br><ul>";
 
 			for (WebResource wr : allResources) {
 				if (!wr.isImage()) {
					html += "<a href=\"" + wr.getUrl() + "\">" + wr.getTitle()
							+ "\"/></a></li>";
 				}
 			}
 
 			html += "</ul><br><ul>";
 
 			List<Message> recentMessages = p.loadRecentMessages(numMessages,
 					Utils.messageThreshold);
 
 			for (Message m : recentMessages) {
 				html += "<li>"
 						+ Utils.linkify("@" + m.getUser() + ": " + m.getText())
 						+ "</li>";
 			}
 
 			html += "</ul>";
 
 			description.setValue(html);
 
 			entry.setDescription(description);
 			entries.add(entry);
 		}
 
 		feed.setEntries(entries);
 		SyndFeedOutput output = new SyndFeedOutput();
 		try {
 			String rss = output.outputString(feed);
 			Utils.stringToS3(rss, Utils.s3Prefix + "feed.rss",
 					"application/rss+xml");
 		} catch (FeedException e) {
 			log.warn("Unable to create RSS feed", e);
 		}
 
 		log.info("Exporting Run Finished");
 	}
 
 	public static void main(String[] args) {
 		Timer t = new Timer();
 		TimerTask tt = new TimerTask() {
 			@Override
 			public void run() {
 				runTask();
 			}
 		};
 		t.scheduleAtFixedRate(tt, 0, interval);
 
 	}
 }
