 package com.darkrockstudios.apps.tminus.loaders;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.android.volley.Response;
 import com.android.volley.Response.ErrorListener;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.darkrockstudios.apps.tminus.TMinusApplication;
 import com.darkrockstudios.apps.tminus.database.DatabaseHelper;
 import com.darkrockstudios.apps.tminus.database.tables.RocketDetail;
 import com.darkrockstudios.apps.tminus.launchlibrary.Rocket;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.sql.SQLException;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by Adam on 7/22/13.
  * Dark Rock Studios
  * darkrockstudios.com
  */
 public class RocketDetailFetcher
 {
 	private static final String TAG = RocketDetailFetcher.class.getSimpleName();
 
 	private static final Pattern WIKI_ARTICLE_PATTERN =
 			Pattern.compile( "^http[s]?://[a-z]{2}.wikipedia.org/wiki/([a-zA-Z0-9-_\\(\\)]+)/?(?:\\?.*)?$" );
 
 	private static void requestRocketImage( String articleTitle, IntermediateLoadListener listener, Context context )
 	{
 		Log.d( TAG, "Requesting Rocket Image..." );
 
 		// This gives the "page image"
 		// Action: query
 		// prop=pageimages
 
 		final String baseUrl = "http://en.wikipedia.org";
 		final String imageQuery =
 				"/w/api.php?action=query&prop=pageimages&format=json&piprop=thumbnail%7Cname&pithumbsize=512&pilimit=1&indexpageids=&redirects=&titles=";
 
 		final String url = baseUrl + imageQuery + articleTitle;
 
 		WikiImageListener imageListener = new WikiImageListener( listener, context );
 		JsonObjectRequest request = new JsonObjectRequest( url, null, imageListener, imageListener );
 		request.setTag( context );
 		TMinusApplication.getRequestQueue().add( request );
 	}
 
 	private static void requestRocketArticle( String articleTitle, IntermediateLoadListener listener, Context context )
 	{
 		Log.d( TAG, "Requesting Rocket Article..." );
 
 		final String baseUrl = "http://en.wikipedia.org";
 		final String articleQuery =
 				"/w/api.php?action=parse&format=json&prop=wikitext&section=0&contentformat=text%2Fx-wiki&contentmodel=wikitext&redirects=&page=";
 
 		final String url = baseUrl + articleQuery + articleTitle;
 
 		WikiArticleListener articleListener = new WikiArticleListener( listener, context );
 		JsonObjectRequest request = new JsonObjectRequest( url, null, articleListener, articleListener );
 		request.setTag( context );
 		TMinusApplication.getRequestQueue().add( request );
 	}
 
 	public static void requestRocketDetails( Rocket rocket, RocketDetailFetchListener listener, Context context )
 	{
 		String articleTitle = extractArticleTitle( rocket );
 
 		if( articleTitle != null )
 		{
 			Log.d( TAG, "Requesting Rocket Details from wiki article: " + articleTitle );
 			IntermediateLoadListener intermediateLoadListener = new IntermediateLoadListener( rocket.id, listener );
 			requestRocketArticle( articleTitle, intermediateLoadListener, context );
 			requestRocketImage( articleTitle, intermediateLoadListener, context );
 		}
 		else
 		{
 			Log.d( TAG, "Could not extract Wiki article for rocket." );
 			if( rocket != null )
 			{
 				Log.d( TAG, "Rocket: " + rocket.name );
 				Log.d( TAG, "WikiURL: " + (rocket.wikiURL != null ? rocket.wikiURL : "null") );
 			}
 		}
 	}
 
 	private static String extractArticleTitle( Rocket rocket )
 	{
 		final String articleTitle;
 		if( rocket != null && rocket.wikiURL != null && !rocket.wikiURL.isEmpty() )
 		{
 			Matcher matcher = WIKI_ARTICLE_PATTERN.matcher( rocket.wikiURL );
 			if( matcher.matches() && matcher.groupCount() == 1 )
 			{
 				articleTitle = matcher.group( 1 );
 			}
 			else
 			{
 				articleTitle = null;
 			}
 		}
 		else
 		{
 			articleTitle = null;
 		}
 
 		return articleTitle;
 	}
 
 	public static interface RocketDetailFetchListener
 	{
 		public void rocketDetailFetchSuccessful( int rocketId );
 
 		public void rocketDetailFetchFailed( int rocketId );
 	}
 
 	// This Wiki api call will get the summary section for a given article
 	// /w/api.php?action=parse&format=json&page=Falcon_9&prop=text&section=0
 
 	// the Parse tree option might be what we want, it allows us to traverse the content via XML rather than regex that crap
 
 	private static class IntermediateLoadListener
 	{
 		private int                       m_rocketId;
 		private RocketDetailFetchListener m_listener;
 
 		private boolean m_imageLoadFinished;
 		private boolean m_imageLoadSuccess;
 		private boolean m_articleLoadFinished;
 		private boolean m_articleLoadSuccess;
 
 		public IntermediateLoadListener( int rocketId, RocketDetailFetchListener listener )
 		{
 			m_rocketId = rocketId;
 			m_listener = listener;
 		}
 
 		public int getRocketId()
 		{
 			return m_rocketId;
 		}
 
 		public void imageLoadFinished( boolean success )
 		{
 			m_imageLoadFinished = true;
 			m_imageLoadSuccess = success;
 
 			if( m_imageLoadSuccess )
 			{
 				Log.i( TAG, "Wiki IMAGE url retrieval successful" );
 			}
 			else
 			{
 				Log.w( TAG, "Wiki IMAGE url retrieval failed!" );
 			}
 
 			checkAllDone();
 		}
 
 		public void articleLoadFinished( boolean success )
 		{
 			m_articleLoadFinished = true;
 			m_articleLoadSuccess = success;
 
 			if( m_articleLoadSuccess )
 			{
 				Log.i( TAG, "Wiki ARTICLE retrieval successful" );
 			}
 			else
 			{
 				Log.w( TAG, "Wiki ARTICLE retrieval failed!" );
 			}
 			checkAllDone();
 		}
 
 		private void checkAllDone()
 		{
 			if( m_imageLoadFinished && m_articleLoadFinished )
 			{
 				if( m_listener != null )
 				{
 					if( m_imageLoadSuccess || m_articleLoadSuccess )
 					{
 						m_listener.rocketDetailFetchSuccessful( m_rocketId );
 					}
 					else
 					{
 						m_listener.rocketDetailFetchFailed( m_rocketId );
 					}
 				}
 			}
 		}
 	}
 
 	private static class WikiArticleListener implements Response.Listener<JSONObject>, ErrorListener
 	{
 		private static final Pattern COMMENT_PATTERN      = Pattern.compile( "\\<\\!--(.*?)--\\>" );
 		private static final Pattern GENERAL_LINK_PATTERN = Pattern.compile( "\\[\\[(.*?:)?(.*?)(\\|.*?)?\\]\\]" );
 		private static final Pattern SIMPLE_LINK_PATTERN  = Pattern.compile( "\\[\\[([^|]*?)\\]\\]" );
 		private static final Pattern ASSET_PATTERN        = Pattern.compile( "\\[\\[[a-zA-Z]+:(.*?)\\]\\]" );
		private static final Pattern REF_PATTERN     = Pattern.compile( "(<ref>.*?</ref>)", Pattern.CASE_INSENSITIVE );
		private static final Pattern CITE_PATTERN    = Pattern.compile( "(\\{\\{cite.*?\\}\\})", Pattern.CASE_INSENSITIVE );
		private static final Pattern CONVERT_PATTERN =
				Pattern.compile( "\\{\\{convert\\|([0-9]+)\\|([a-zA-Z]+)\\}\\}", Pattern.CASE_INSENSITIVE );
 		private static final Pattern BOLD_PATTERN         = Pattern.compile( "'''(.+?)'''" );
 		private static final Pattern ITALICS_PATTERN      = Pattern.compile( "''(.+?)''" );
 
 		final private Context                  m_context;
 		final private IntermediateLoadListener m_listener;
 
 		public WikiArticleListener( IntermediateLoadListener listener, Context context )
 		{
 			m_listener = listener;
 			m_context = context;
 		}
 
 		@Override
 		public void onResponse( JSONObject response )
 		{
 			Log.d( TAG, "Received wiki ARTICLE data, parsing..." );
 			try
 			{
 				JSONObject parse = response.getJSONObject( "parse" );
 				JSONObject text = parse.getJSONObject( "wikitext" );
 
 				String articleText = text.getString( "*" );
 				articleText = cleanUpWikiText( articleText );
 
 				if( saveToDatabase( articleText ) )
 				{
 					m_listener.articleLoadFinished( true );
 				}
 				else
 				{
 					m_listener.articleLoadFinished( false );
 				}
 			}
 			catch( JSONException e )
 			{
 				e.printStackTrace();
 				m_listener.articleLoadFinished( false );
 			}
 		}
 
 		@Override
 		public void onErrorResponse( VolleyError error )
 		{
 			m_listener.articleLoadFinished( false );
 		}
 
 		private boolean saveToDatabase( String articleText )
 		{
 			boolean success = false;
 
 			final DatabaseHelper databaseHelper = OpenHelperManager.getHelper( m_context, DatabaseHelper.class );
 			if( databaseHelper != null )
 			{
 				try
 				{
 					final int rocketId = m_listener.getRocketId();
 
 					Dao<RocketDetail, Integer> rocketDetailDao = databaseHelper.getDao( RocketDetail.class );
 					RocketDetail rocketDetail = rocketDetailDao.queryForId( rocketId );
 					if( rocketDetail == null )
 					{
 						rocketDetail = new RocketDetail();
 						rocketDetail.rocketId = rocketId;
 						rocketDetail.summary = articleText;
 						rocketDetailDao.create( rocketDetail );
 						success = true;
 					}
 					else
 					{
 						rocketDetail.summary = articleText;
 						rocketDetailDao.update( rocketDetail );
 						success = true;
 					}
 				}
 				catch( SQLException e )
 				{
 					e.printStackTrace();
 				}
 
 				OpenHelperManager.releaseHelper();
 			}
 
 			return success;
 		}
 
 		private String cleanUpWikiText( final String wikiText )
 		{
 			// Unescape the wikitext
 			String articleText = wikiText.replace( "\\\"", "\"" );
 			articleText = articleText.replace( "\\/", "/" );
 
 			articleText = removeInfoBox( articleText );
 
 			// Use our regex patterns to clean out the wiki syntax and replace it with mostly plain text
 			// or simple HTML for formatting
 			Matcher matcher;
 
 			matcher = COMMENT_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "" );
 
 			matcher = REF_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "" );
 
 			matcher = CITE_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "" );
 
 			matcher = CONVERT_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "$1 $2" );
 
 			matcher = ASSET_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "" );
 
 			matcher = SIMPLE_LINK_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "$1" );
 
 			matcher = GENERAL_LINK_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "$2" );
 
 			matcher = BOLD_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "<strong>$1</strong>" );
 
 			matcher = ITALICS_PATTERN.matcher( articleText );
 			articleText = matcher.replaceAll( "<em>$1</em>" );
 
 			// Lastly trim any whitespace, and then space things out
 			articleText = articleText.trim();
 			articleText = articleText.replace( "\n", "<br/>" );
 
 			return articleText;
 		}
 
 		private String removeInfoBox( final String articleText )
 		{
 			final String cleanedArticle;
 
 			// Find the end of the Infobox
 			final String infoBox = "{{Infobox";
 			final String openBracket = "{{";
 			final String closeBracket = "}}";
 			final Locale locale = Locale.ENGLISH;
 			int curPos = articleText.toLowerCase( locale ).indexOf( infoBox.toLowerCase( locale ) );
 			if( curPos > -1 )
 			{
 				curPos += infoBox.length();
 				int openBrackets = 1;
 				while( openBrackets > 0 )
 				{
 					int nextOpen = articleText.indexOf( openBracket, curPos );
 					int nextClose = articleText.indexOf( closeBracket, curPos );
 					if( nextOpen < nextClose )
 					{
 						curPos = nextOpen + openBracket.length();
 						++openBrackets;
 					}
 					else
 					{
 						curPos = nextClose + closeBracket.length();
 						--openBrackets;
 					}
 				}
 
 				// Grab everything immediately after the Infobox
 				cleanedArticle = articleText.substring( curPos, articleText.length() );
 			}
 			else
 			{
 				cleanedArticle = articleText;
 			}
 
 			return cleanedArticle;
 		}
 	}
 
 	private static class WikiImageListener implements Response.Listener<JSONObject>, ErrorListener
 	{
 		final private Context                  m_context;
 		final private IntermediateLoadListener m_listener;
 
 		public WikiImageListener( IntermediateLoadListener listener, Context context )
 		{
 			m_listener = listener;
 			m_context = context;
 		}
 
 		@Override
 		public void onResponse( JSONObject response )
 		{
 			Log.d( TAG, "Received wiki IMAGE data, parsing..." );
 			if( response != null )
 			{
 				try
 				{
 					Log.d( TAG, response.toString() );
 					JSONObject parse = response.getJSONObject( "query" );
 					JSONArray pageIdsArray = parse.getJSONArray( "pageids" );
 
 					if( pageIdsArray.length() == 1 )
 					{
 						final String pageId = pageIdsArray.getString( 0 );
 
 						JSONObject pages = parse.getJSONObject( "pages" );
 						JSONObject rocketPage = pages.getJSONObject( pageId );
 
 						JSONObject rocketThumbnail = rocketPage.getJSONObject( "thumbnail" );
 						String rocketThumbnailUrl = rocketThumbnail.getString( "source" );
 
 						if( rocketThumbnailUrl != null )
 						{
 							if( saveToDatabase( rocketThumbnailUrl ) )
 							{
 								m_listener.imageLoadFinished( true );
 							}
 							else
 							{
 								m_listener.imageLoadFinished( false );
 							}
 						}
 					}
 				}
 				catch( JSONException e )
 				{
 					e.printStackTrace();
 					m_listener.imageLoadFinished( false );
 				}
 			}
 		}
 
 		@Override
 		public void onErrorResponse( VolleyError error )
 		{
 			Log.w( TAG, "Wiki image request failed." );
 			m_listener.imageLoadFinished( false );
 		}
 
 		private boolean saveToDatabase( String rocketThumbnailUrl )
 		{
 			boolean success = false;
 
 			final DatabaseHelper databaseHelper = OpenHelperManager.getHelper( m_context, DatabaseHelper.class );
 			if( databaseHelper != null )
 			{
 				try
 				{
 					final int rocketId = m_listener.getRocketId();
 
 					Dao<RocketDetail, Integer> rocketDetailDao = databaseHelper.getDao( RocketDetail.class );
 					RocketDetail rocketDetail = rocketDetailDao.queryForId( rocketId );
 					if( rocketDetail == null )
 					{
 						rocketDetail = new RocketDetail();
 						rocketDetail.rocketId = rocketId;
 						rocketDetail.imageUrl = rocketThumbnailUrl;
 						rocketDetailDao.create( rocketDetail );
 						success = true;
 					}
 					else
 					{
 						rocketDetail.imageUrl = rocketThumbnailUrl;
 						rocketDetailDao.update( rocketDetail );
 						success = true;
 					}
 				}
 				catch( SQLException e )
 				{
 					e.printStackTrace();
 				}
 
 				OpenHelperManager.releaseHelper();
 			}
 
 			return success;
 		}
 	}
 }
