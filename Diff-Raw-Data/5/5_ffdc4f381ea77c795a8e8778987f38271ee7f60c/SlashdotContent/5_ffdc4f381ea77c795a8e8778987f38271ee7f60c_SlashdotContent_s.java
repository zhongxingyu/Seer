 package com.noughmad.plusfive;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.Date;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 
 public class SlashdotContent {
 	
 	public static SimpleDateFormat sDateFormat = new SimpleDateFormat("EEEE MMM dd, yyyy @hh:mma", Locale.US);
 	public static String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:20.0) Gecko/20100101 Firefox/20.0";
 	
 	public static boolean refreshStories(Context context, Calendar date) {
 		if (date == null) {
 			date = Calendar.getInstance();
 		}
 		
 		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
 		format.setCalendar(date);
 		String source = "http://classic.slashdot.org/?issue=" + format.format(date.getTime());
 
 		return refreshStories(context, source);
 	}
 	
 	public static boolean refreshStories(Context context, String source) {
 		Log.i("RefreshStories", "Refreshing from " + source);
 
         if (context == null) {
            Log.w("RefreshStories", "Trying to refreshing without a Context");
             return false;
         }
 		
 		Document doc;
 		try {
 			doc = Jsoup.connect(source).userAgent(USER_AGENT).get();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		Uri storiesUri = Uri.withAppendedPath(SlashdotProvider.BASE_URI, SlashdotProvider.STORIES_TABLE_NAME);
 				
 		Elements articles = doc.select("article[data-fhid]");
 		Log.d("RefreshStories", "Found " + articles.size() + " articles");
 		
 		for (Element article : articles) {
 			Elements titles = article.select("header h2.story");
 			if (titles.isEmpty()) {
 				continue;
 			}
 			
 			long id = Long.parseLong(article.attr("data-fhid"));
 			
 			ContentValues values = new ContentValues();
 			
 			Element title = titles.first();
 			Element link = title.select("a[href]").first();
 
 			values.put(SlashdotProvider.STORY_TITLE, link.html());
 
 			String storyUrl = link.attr("href");
 			if (!storyUrl.startsWith("http")) {
 				storyUrl = "http:" + storyUrl;
 			}
 			values.put(SlashdotProvider.STORY_URL, storyUrl);
 			values.put(SlashdotProvider.STORY_SUMMARY, article.select("div#text-" + id).first().html());
 			values.put(SlashdotProvider.STORY_COMMENT_COUNT, Integer.parseInt(article.select("span.commentcnt-" + id).first().html()));
 			
 			String date = article.select("time").html().substring(3);
 			int timeIndex = date.indexOf('@');
 			values.put(SlashdotProvider.STORY_DATE, date.substring(0, timeIndex - 1));
 			
 			try {
 				values.put(SlashdotProvider.STORY_TIME, sDateFormat.parse(date).getTime());
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 						
 			Uri uri = ContentUris.withAppendedId(storiesUri, id);
 			Cursor existing = context.getContentResolver().query(uri, new String[] {SlashdotProvider.ID}, null, null, null);
 			if (existing.moveToFirst()) {
 				context.getContentResolver().update(uri, values, null, null);
 			} else {
 				context.getContentResolver().insert(uri, values);
 			}
 			existing.close();
 		}
 
         Elements quote = doc.select("section.bq blockquote.msg p");
         if (!quote.isEmpty())
         {
             ContentValues values = new ContentValues();
             values.put(SlashdotProvider.QUOTE_DATE, Calendar.getInstance().getTimeInMillis());
             values.put(SlashdotProvider.QUOTE_CONTENT, quote.first().html());
 
             Uri uri = Uri.withAppendedPath(SlashdotProvider.BASE_URI, SlashdotProvider.QUOTES_TABLE_NAME);
             context.getContentResolver().insert(uri, values);
         }
 		
 		return true;
 	}
 	
 	private static void parseComment(Context context, Uri baseUri, Element tree, int level, String parentTitle) {
         long id = Long.parseLong(tree.id().substring(5));
         String title = "";
 		if (!tree.hasClass("hidden")) {
 
             Element comment = tree.select("div.cw").first();
 
             ContentValues values = new ContentValues();
             values.put(SlashdotProvider.COMMENT_LEVEL, level);
 
             values.put(SlashdotProvider.ID, id);
 
             title = tree.select("a#comment_link_" + id).first().html();
             if (title.trim().equals("Re:") && parentTitle != null) {
                 if (parentTitle.startsWith("Re:")) {
                     title = parentTitle;
                 } else {
                     title = "Re:" + parentTitle;
                 }
             }
             values.put(SlashdotProvider.COMMENT_TITLE, title);
 
             String author = null;
             Elements authorLinks = comment.select("span.by a");
             if (!authorLinks.isEmpty()) {
                 author = authorLinks.first().html();
             } else {
                 author = comment.select("span.by").first().html();
             }
 
             values.put(SlashdotProvider.COMMENT_AUTHOR, author);
 
             values.put(SlashdotProvider.COMMENT_CONTENT, comment.select("div#comment_body_" + id).first().html());
 
             String scoreHtml = comment.select("span.score").first().html();
             values.put(SlashdotProvider.COMMENT_SCORE_TEXT, scoreHtml);
             int pos = scoreHtml.indexOf("Score:</span>");
             String score = scoreHtml.substring(pos + 13, pos + 15);
             if (!score.startsWith("-")) {
                 score = score.substring(0, 1);
             }
             values.put(SlashdotProvider.COMMENT_SCORE_NUM, Integer.parseInt(score));
 
             Uri uri = ContentUris.withAppendedId(baseUri, id);
             Cursor existing = context.getContentResolver().query(uri, new String[] {SlashdotProvider.ID}, null, null, null);
             boolean exists = existing.getCount() > 0;
             existing.close();
 
             if (exists) {
                 context.getContentResolver().update(uri, values, null, null);
             } else {
                 context.getContentResolver().insert(uri, values);
             }
         }
 		
 		for (Element subTree : tree.select("ul#commtree_" + id + " > li.comment")) {
 			parseComment(context, baseUri, subTree, level + 1, title);
 		}
 	}
 	
 	public static void refreshComments(Context context, long storyId, String source) {
         if (context == null) {
            Log.w("RefreshComments", "Trying to refreshing comments without a Context");
             return;
         }
 
 
         Uri storyUri = ContentUris.withAppendedId(Uri.withAppendedPath(SlashdotProvider.BASE_URI, SlashdotProvider.STORIES_TABLE_NAME), storyId);
 		
 		if (source == null) {
 			Cursor story = context.getContentResolver().query(storyUri, new String[] {SlashdotProvider.STORY_URL}, null, null, null);
 			
 			if (story.moveToFirst()) {
 				source = story.getString(0);
 				story.close();
 			} else {
 				story.close();
 				return;
 			}
 		}
 		
 		Log.i("RefreshComments", "Refreshing from " + source);
 
 		Document doc;
 		try {
 			doc = Jsoup.connect(source).userAgent(USER_AGENT).get();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		Uri baseUri = Uri.withAppendedPath(storyUri, SlashdotProvider.COMMENTS_TABLE_NAME);
 		context.getContentResolver().delete(baseUri, null, null);
 		
 		for (Element tree : doc.select("ul#commentlisting > li.comment")) {
 			parseComment(context, baseUri, tree, 0, null);			
 		}
 	};
 }
