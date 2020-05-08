 package mdettlaff.cloudreader.service;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import mdettlaff.cloudreader.domain.Feed;
 import mdettlaff.cloudreader.domain.FeedItem;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.stereotype.Service;
 
import com.google.common.base.Objects;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 @Service
 public class FeedParserService {
 
 	private static final int DEFAULT_MAX_WIDTH = 255;
 
 	public Feed parseFeed(URL feedSource) throws FeedException, IOException {
 		SyndFeedInput input = new SyndFeedInput();
 		SyndFeed syndFeed = input.build(new XmlReader(feedSource));
 		Feed feed = new Feed(feedSource.toString());
 		feed.setTitle(abbreviate(syndFeed.getTitle()));
 		feed.setLink(abbreviate(syndFeed.getLink()));
 		List<FeedItem> items = new ArrayList<>();
 		for (Object entry : syndFeed.getEntries()) {
 			FeedItem item = createFeedItem((SyndEntry) entry);
 			item.setFeed(feed);
 			items.add(item);
 		}
 		feed.setItems(items);
 		return feed;
 	}
 
 	private FeedItem createFeedItem(SyndEntry entry) {
 		FeedItem item = new FeedItem();
 		item.setTitle(abbreviate(StringUtils.trim(entry.getTitle())));
 		item.setLink(abbreviate(entry.getLink()));
 		item.setDescription(StringUtils.trim(getDescription(entry)));
 		item.setDate(getDate(entry));
 		item.setAuthor(abbreviate(entry.getAuthor()));
 		item.setUri(abbreviate(entry.getUri()));
 		return item;
 	}
 
 	private Date getDate(SyndEntry entry) {
 		if (entry.getPublishedDate() != null) {
 			return entry.getPublishedDate();
 		} else {
 			return entry.getUpdatedDate();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private String getDescription(SyndEntry entry) {
 		SyndContent content = entry.getDescription();
 		if (content == null) {
 			List<SyndContent> contents = entry.getContents();
 			if (!contents.isEmpty()) {
 				content = contents.get(0);
 			}
 		}
		return content == null ? null : content.getValue();
 	}
 
 	private String abbreviate(String input) {
 		return StringUtils.abbreviate(input, DEFAULT_MAX_WIDTH);
 	}
 }
