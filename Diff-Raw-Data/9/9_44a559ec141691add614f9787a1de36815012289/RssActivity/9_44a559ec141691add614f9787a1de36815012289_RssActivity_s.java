 package org.oetc.oetc12_android;
 
 import java.net.URL;
 import java.util.concurrent.ExecutionException;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 
 public class RssActivity extends Activity implements OnItemClickListener{
 
	public final String appleFeed = "http://www.apple.com/pr/feeds/pr.rss";

 	private RSSFeed feed = null;
 	protected GetRssFeed _getRssFeed;
 	ListView itemlist;
 	
 	 ArrayAdapter<RSSItem> adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.rsslist);
         
         _getRssFeed = new GetRssFeed();
         // go get our feed!
        try {
    	   feed = _getRssFeed.execute(appleFeed).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
        
         itemlist = (ListView) findViewById(R.id.listContact);
        
         // display UI
         adapter = UpdateDisplay();
         itemlist.setAdapter(adapter);
         
         itemlist.setOnItemClickListener(this);
 	}
 	
 	 class GetRssFeed extends AsyncTask<String, Integer, RSSFeed>{
 //	    	ProgressDialog myPD = null;
 			@Override
 			protected RSSFeed doInBackground(String... params) {
 				// TODO Auto-generated method stub
 				try
 		    	{
 //					myPD = ProgressDialog.show(SportsActivity.this, "Please wait...", "Loading Sports News...",true);
 		    		// setup the url
 		    	   URL url = new URL(params[0]);
 
 		           // create the factory
 		           SAXParserFactory factory = SAXParserFactory.newInstance();
 		           // create a parser
 		           SAXParser parser = factory.newSAXParser();
 
 		           // create the reader (scanner)
 		           XMLReader xmlreader = parser.getXMLReader();
 		           // instantiate our handler
 		           RSSHandler theRssHandler = new RSSHandler();
 		           // assign our handler
 		           xmlreader.setContentHandler(theRssHandler);
 		           // get our data via the url class
 		           InputSource is = new InputSource(url.openStream());
 		           // perform the synchronous parse           
 		           xmlreader.parse(is);
 		           // get the results - should be a fully populated RSSFeed instance, or null on error
 		           return theRssHandler.getFeed();
 		    	}
 		    	catch (Exception ee)
 		    	{
 		    		// if we have a problem, simply return null
 		    		return null;
 		    	}
 			}
 			
 			protected void onProgressUdate(Integer...integers){
 				super.onProgressUpdate(integers);
 //				_percentField.setText((integers[0] * 2)+"%");
 //				_percentField.setTextSize(integers[0]);
 			}
 			
 			@Override
 			protected void onPreExecute(){
 				super.onPreExecute();
 			}
 	    }
 	    
 	    
 	    private ArrayAdapter<RSSItem> UpdateDisplay()
 	    {
 	        ArrayAdapter<RSSItem> adapter = new ArrayAdapter<RSSItem>(
 	        			this,
 	        			R.layout.rssrow,
 	        			feed.getAllItems());
 	        
 	        return adapter;
 	       
 	    }
 
 	 public void onItemClick(AdapterView parent, View v, int position, long id)
      {
 
     	 Intent itemintent = new Intent(this,ShowDescription.class);
          
     	 Bundle b = new Bundle();
     	 b.putString("title", feed.getItem(position).getTitle());
     	 b.putString("description", feed.getItem(position).getDescription().toString());
     	 b.putString("link", feed.getItem(position).getLink());
     	 b.putString("pubdate", feed.getItem(position).getPubDate());
     	 
     	 itemintent.putExtra("android.intent.extra.INTENT", b);
          
          startActivity(itemintent);
      }
 }
