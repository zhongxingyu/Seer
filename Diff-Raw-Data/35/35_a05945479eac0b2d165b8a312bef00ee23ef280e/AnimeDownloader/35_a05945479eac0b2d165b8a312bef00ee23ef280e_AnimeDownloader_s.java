 package eu.isweb.animeplayer;
 
 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import eu.isweb.animeplayer.HTMLLinkExtrator.HtmlLink;
 
 public abstract class AnimeDownloader<T>
 	extends AsyncTask<String, Void, ArrayList<T>> {
 	protected ConnectionManager cm;
 	protected static String USER_AGENT_MOBILE = "Mozilla/5.0 (Linux; U; Android 2.2; en-gb; GT-P1000 Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
 	protected int MAX_RETRY = 3;
 	protected boolean bSuccess = true;
 	protected Activity context = null;
 	
 	public AnimeDownloader() {
 		super();
 	}
 	
 	public AnimeDownloader(Activity c) {
 		super();
 		context = c;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		cm = ConnectionManager.getInstance();
 		cm.add(this.hashCode());
 		
 		if (context != null)
 			context.setProgressBarIndeterminateVisibility(true);
 		
 		super.onPreExecute();
 	}
 	
 	protected ArrayList<T> doInBackgroundAction(Document result) throws Exception { return null; };
 	protected ArrayList<T> doInBackgroundAction(Vector<HtmlLink> result) throws Exception { return null; };
 	abstract protected void onPostExecuteAction(ArrayList<T> result);
 	
 	@Override
 	protected ArrayList<T> doInBackground(String... attributes) {
 		Log.d("JD", "AnimeDownloader.doInBackground task start");
     	Document doc = null;
     	Connection.Response connection = null;
     	ArrayList<T> result = new ArrayList<T>();
     	
    		do {
    			Log.d("JD", "MAX_RETRY="+MAX_RETRY);
    			try {
    				Log.d("JD", "AnimeDownloader.doInBackground connection start");
     			connection = Jsoup.connect(attributes[0]).timeout(2000)
     					.userAgent(USER_AGENT_MOBILE).execute();
     			Log.d("JD", "AnimeDownloader.doInBackground connection end");
     			
     			if(connection.statusCode() != 200)
     				throw new Exception("error status code " + connection.statusCode());
     			
     			if( attributes.length > 1 && attributes[1].equals("main") ) {
     				String code = connection.body();
     				try {
     					code = code.substring(code.indexOf("<li  class=\"active\" id=\"allTab\">"));
     				}catch( Exception e ) {}	
     			    result = doInBackgroundAction(new HTMLLinkExtrator().grabHTMLLinks(code));
     			}else{
     				doc = connection.parse();
     				
     				if(doc == null)
             			throw new Exception("error while parsing");
             		
     				result = doInBackgroundAction(doc);
     			}
    			} catch (Exception e) {
    				Log.e("JD", e.toString());
    				try { Thread.sleep(500); } catch (InterruptedException e1) {}
    				bSuccess = false;
 			}
    		} while(!bSuccess && MAX_RETRY-- > 1);
    		
    		Log.d("JD", "AnimeDownloader.doInBackground task end");
     	return result;
     }
 	
 	@Override
 	protected void onPostExecute(ArrayList<T> result) {
 		cm.remove(this.hashCode());
 		
 		if (context != null && cm.count() == 0)
 			context.setProgressBarIndeterminateVisibility(false);
 		
 		onPostExecuteAction(result);
 		super.onPostExecute(result);
 	}
 	
 	@Override
 	protected void onCancelled() {
 		cm.remove(this.hashCode());
 		super.onCancelled();
 	}
 }
