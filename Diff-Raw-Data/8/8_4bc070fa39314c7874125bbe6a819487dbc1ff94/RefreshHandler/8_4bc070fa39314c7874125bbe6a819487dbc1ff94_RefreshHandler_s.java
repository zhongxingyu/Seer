 package com.escoand.android.wceu;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import android.content.ContentValues;
 import android.content.Context;
 
 public final class RefreshHandler {
 	final static SimpleDateFormat dfInDate = new SimpleDateFormat("yyyyHHdd");
 	final static SimpleDateFormat dfInDateTime = new SimpleDateFormat(
 			"yyyyMMdd'T'HHmmss'Z'");
 	final static SimpleDateFormat dfOut = new SimpleDateFormat(
 			EventsDatabase.DATE_FORMAT);
 
 	public static boolean refreshAll(final Context context,
 			final NewsDatabase dbNews, final EventsDatabase dbEvents) {
 		String[] urls = context.getResources()
 				.getStringArray(R.array.urlEvents);
 		String[] categories = context.getResources().getStringArray(
 				R.array.categoryValues);
 		boolean result;
 
 		// refresh news
 		dbNews.clear();
 		result = RefreshNews(dbNews, context.getString(R.string.urlNews));
 		if (!result)
 			return false;
 
 		// refresh events
 		dbEvents.clear();
 		for (int i = 0; i < urls.length; i++) {
 			result = RefreshEvents(dbEvents, urls[i], categories[i]);
 			if (!result)
 				return false;
 		}
 
 		return result;
 	}
 
 	public static boolean RefreshNews(final NewsDatabase dbNews,
 			final String url) {
 		SAXParserFactory factory;
 		SAXParser parser;
 		XMLReader reader;
 		RSSHandler handler;
 		InputSource input;
 
 		try {
 			factory = SAXParserFactory.newInstance();
 			parser = factory.newSAXParser();
 			reader = parser.getXMLReader();
 			handler = new RSSHandler(dbNews);
 			input = new InputSource(new URL(url).openStream());
 
 			reader.setContentHandler(handler);
 			reader.parse(input);
 		}
 
 		/* catch errors */
 		catch (ParserConfigurationException e) {
			e.printStackTrace();
 		} catch (SAXException e) {
			e.printStackTrace();
 		} catch (MalformedURLException e) {
			e.printStackTrace();
 		} catch (IOException e) {
 			// e.printStackTrace();
 			return false;
 		}
 
 		return true;
 	}
 
 	public static boolean RefreshEvents(final EventsDatabase dbEvents,
 			final String url, final String category) {
 		BufferedReader reader;
 		String line;
 		ContentValues values = new ContentValues();
 
 		try {
 			reader = new BufferedReader(new InputStreamReader(
 					new URL(url).openStream()));
 
 			while ((line = reader.readLine()) != null) {
 				try {
 
 					/* new item */
 					if (line.equals("BEGIN:VEVENT")) {
 						values.clear();
 						values.put(EventsDatabase.COLUMN_CATEGORY, category);
 					}
 
 					/* read data */
 					else if (line.startsWith("DTSTART")) {
 						if (line.split(":")[1].indexOf('T') > 0)
 							values.put(EventsDatabase.COLUMN_DATE,
 									dfOut.format(dfInDateTime.parse(line
 											.split(":")[1])));
 						else
 							values.put(EventsDatabase.COLUMN_DATE, dfOut
 									.format(dfInDate.parse(line.split(":")[1])));
 					} else if (line.startsWith("DTEND")) {
 						if (line.split(":")[1].indexOf('T') > 0)
 							values.put(EventsDatabase.COLUMN_DATEEND,
 									dfOut.format(dfInDateTime.parse(line
 											.split(":")[1])));
 						else
 							values.put(EventsDatabase.COLUMN_DATEEND, dfOut
 									.format(dfInDate.parse(line.split(":")[1])));
 					} else if (line.startsWith("SUMMARY"))
 						values.put(EventsDatabase.COLUMN_TITLE,
 								line.split(":")[1]);
 					else if (line.startsWith("DESCRIPTION"))
 						values.put(EventsDatabase.COLUMN_TEXT,
 								line.split(":")[1]);
 					else if (line.startsWith("LOCATION"))
 						values.put(
 								EventsDatabase.COLUMN_URL,
 								"https://maps.google.com/maps?q="
 										+ line.split(":")[1]);
 
 					/* save item */
 					else if (line.equals("END:VEVENT"))
 						dbEvents.insertItem(values);
 
 					/* error */
 				} catch (IndexOutOfBoundsException e) {
 					// e.printStackTrace();
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			}
 
 			reader.close();
 		} catch (Exception e) {
 			return false;
 		}
 
 		return true;
 	}
 }
