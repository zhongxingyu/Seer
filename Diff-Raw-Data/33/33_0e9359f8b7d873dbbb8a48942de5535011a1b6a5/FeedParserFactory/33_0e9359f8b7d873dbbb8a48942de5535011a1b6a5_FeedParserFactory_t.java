 package com.ouchadam.podcast.parser;
 
 import com.ouchadam.podcast.Message;
 import org.w3c.dom.Document;
 
 import java.util.List;
 
 public abstract class FeedParserFactory {
 
 	public static List<Message> getParser(Document doc){
         return new FeedItemFactory().parse(doc);
 	}
 
 }
