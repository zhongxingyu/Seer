 package com.dev.campus.event;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import android.util.Log;
 
 import com.dev.campus.CampusUB1App;
 import com.dev.campus.event.Feed.FeedType;
 import com.dev.campus.util.TimeExtractor;
 
 public class EventParser {
 
 	private ArrayList<Event> mParsedEvents;
 	private ArrayList<Date> mParsedEventDates;
 
 	public EventParser() {
 
 	}
 
 	public ArrayList<Event> getParsedEvents() {
 		return mParsedEvents;
 	}
 
 	public ArrayList<Date> getParsedEventDates() {
 		return mParsedEventDates;
 	}
 
 	public void parseEvents(Category category, ArrayList<Event> existingEvents) throws ParseException, IOException {
 		ArrayList<Event> events = new ArrayList<Event>();
 		ArrayList<Date> dates = new ArrayList<Date>();
 		for (Feed feed : category.getFeeds()) {
 			if (feed.getType().isFiltered()) {
 				Event event;
 				Date buildDate = new Date(0);
 
 				String url = feed.getUrl();
 				InputStream input = new URL(url).openStream();
 				Document xmlDoc = Jsoup.parse(input, "UTF-8", url);
 				if (xmlDoc.toString().contains("iso-8859-1")) {
 					InputStream inputISO = new URL(url).openStream();
 					xmlDoc = Jsoup.parse(inputISO, "CP1252", url);
 				}
 
 				String lastBuildDate = xmlDoc.select("lastBuildDate").text();
 				buildDate = TimeExtractor.createDate(lastBuildDate, "EEE, d MMM yyyy HH:mm:ss Z");
 				dates.add(buildDate);
 
 				for (Element item : xmlDoc.select("item")) {
 					event = new Event();
 
 					String title = item.select("title").text();
 					event.setTitle(title);
 
 					String description = item.select("description").text();
 					event.setDescription(description);
 
 					if (feed.getType().equals(FeedType.LABRI_FEED))
 						event.setDetails(description);
					else
						event.setDetails(item.select("content|encoded").text());
 
 					Date d = null;
 					String pubDate = item.select("pubDate").text();
 					d = TimeExtractor.getCorrectDate(pubDate, event.getDetails());
 					event.setDate(d);
 
 					event.setCategory(category.toString());
 					event.setSource(feed.getType());
 
 					if (!event.getTitle().equals("")) {
 						if(existingEvents.contains(event)) {
 							break;
 						}
 						events.add(event);
 					}
 				}
 
 			}
 			else if (feed.getType().equals(FeedType.LABRI_FEED_HTML)&& CampusUB1App.persistence.isFilteredLabri()){
 				CampusUB1App.LogD(feed.getUrl());
 				events.addAll(EventHtmlParser.parse(CampusUB1App.persistence.getNbMonth(), feed.getUrl()));
 				dates.add(new Date());
 			}
 		}
 
 		events.addAll(existingEvents);
 		mParsedEvents = events;
 		mParsedEventDates = dates;
 	}
 
 	public boolean isLatestVersion(Category category, List<Date> dates) throws ParseException, IOException {
 		int i = 0;
 		for (Feed feed : category.getFeeds()) {
 			if (feed.getType().isFiltered()) {
 				Date buildDate;
 				String url = feed.getUrl();
 				InputStream input = new URL(url).openStream();
 				Document xmlDoc = Jsoup.parse(input, "UTF-8", url);
 				if (xmlDoc.toString().contains("iso-8859-1")) {
 					InputStream inputISO = new URL(url).openStream();
 					xmlDoc = Jsoup.parse(inputISO, "CP1252", url);
 				}
 
 				String lastBuildDate = xmlDoc.select("lastBuildDate").text();
 				buildDate = TimeExtractor.createDate(lastBuildDate, "EEE, d MMM yyyy HH:mm:ss Z");
 				if (dates.get(i++).getTime() != buildDate.getTime())
 					return false;
 
 			}
 			else {
 				return false;
 			}
 		}
 		return true;
 	}
 }
