 package com.media_moderator.article_data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Log;
 import android.util.Xml;
 
 public class Alchemy_Parser {
 
 	
 	private static final String ns = null;
     //TODO find way to pass source to parser
     public String source = null;
 	
 	public List<Keyword> parse(InputStream in) throws XmlPullParserException, IOException {
         try {
             XmlPullParser parser = Xml.newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             parser.setInput(in, null);
             parser.nextTag();
             return readFeed(parser);
         } finally {
             in.close();
         }
     }
 	
 	private List<Keyword> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
         List<Keyword> Keys = new ArrayList<Keyword>();
 
         while (parser.getName() != "Keywords") {
         	parser.nextTag();
         }
 
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             // Starts by looking for the entry tag
             if (name.equals("keyword")) {
                 Keys.add(readKeyword(parser));
             } else {
                 skip(parser);
             }
         }  
         return Keys;
     }
 	
 	 private Keyword readKeyword(XmlPullParser parser) throws XmlPullParserException, IOException {
	        parser.require(XmlPullParser.START_TAG, ns, "item");
 
 	        String word = null;
 	    	String relevance = null;
 	    	String sentiment = null;
 	        while (parser.next() != XmlPullParser.END_TAG) {
 	            if (parser.getEventType() != XmlPullParser.START_TAG) {
 	                continue;
 	            }
 	            String name = parser.getName();
 	            if (name.equals("text")) {
 	                word = readWord(parser);
 	            } 
 	            else if (name.equals("relevance")) {
 	                relevance = readRelevance(parser);
	            } else if (name.equals("sentiment")) {
 	                sentiment = readSentiment(parser);
 	            }
 	            else {
 	                skip(parser);
 	            }
 	        }
 	        Log.i("parser", word);
 	        return new Keyword(word, relevance, sentiment);
 	    }
 	 
 	 private String readWord(XmlPullParser parser) throws IOException, XmlPullParserException {
 	        parser.require(XmlPullParser.START_TAG, ns, "text");
 	        String text = readText(parser);
 	        parser.require(XmlPullParser.END_TAG, ns, "text");
 	        return text;
 	    }
 	      
 	    // Processes link tags in the feed.
 	    private String readRelevance(XmlPullParser parser) throws IOException, XmlPullParserException {
 	        parser.require(XmlPullParser.START_TAG, ns, "relevance");
 	        String Relevance = readText(parser);
 	        parser.require(XmlPullParser.END_TAG, ns, "relevance");
 	        return Relevance;
 	    }
 
 	    // Processes summary tags in the feed.
 	    private String readSentiment(XmlPullParser parser) throws IOException, XmlPullParserException {
 	        String sentiment = null;
 	    	parser.require(XmlPullParser.START_TAG, ns, "sentiment");
 	        parser.nextTag();
 	        while (parser.getName() != "sentiment") {
 	        	if (parser.getName() == "type") {
	        		sentiment = readText(parser);
 	        	}
 	        	else {
 	        		skip(parser);
 	        	}
 	        }
 	        parser.require(XmlPullParser.END_TAG, ns, "sentiment");
 	        return sentiment;
 	    }
 	    
 	    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
 	        String result = "";
 	        if (parser.next() == XmlPullParser.TEXT) {
 	            result = parser.getText();
 	            parser.nextTag();
 	        }
 	        return result;
 	    }
 	    
 	    //when there is a tag we don't care about, skip it
 	    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            throw new IllegalStateException();
 	        }
 	        int depth = 1;
 	        while (depth != 0) {
 	            switch (parser.next()) {
 	            case XmlPullParser.END_TAG:
 	                depth--;
 	                break;
 	            case XmlPullParser.START_TAG:
 	                depth++;
 	                break;
 	            }
 	        }
 	     }
 
 }
