 package net.radspark.android.warpdrive;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 
 public class LoadingScreenActivity extends Activity {
 
 	private String className;
 	private Bundle extras;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.loadingscreen);
      
         className = getIntent().getExtras().getString("class");
         extras = getIntent().getExtras();
         
         setTitle("Warpdrive - Laddar");
         
         Thread workerThread = new Thread() {
         	@Override
         	public void run() {
         		try {
         			super.run();
         			
         			extras.putSerializable("quotes", getQuotes(extras.getInt("pos"), extras.getInt("page")));
         		} catch(Exception e) {
         			e.printStackTrace();
         		} finally {
         			try {
 						Intent newIntent = new Intent(LoadingScreenActivity.this, Class.forName(className));
 						newIntent.putExtras(extras);
 						startActivity(newIntent);
         			} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 					}
         		}
         	}
         };
         
         workerThread.start();
 	}
 	
 	public ArrayList<Quote> getQuotes(int pos, int page) {
 		ArrayList<Quote> quoteList = new ArrayList<Quote>();
 		
 		try {
     		Log.d("Warpdrive", "Connecting...");
 			Document doc = Jsoup.connect(UrlBuilder.getFinalUrl(pos, page)).get();
 			
 			Log.d("Warpdrive", "Selecting...");
 			Elements quotes = doc.select("#main div");
 			
 			Log.d("Warpdrive", "Iterating elements...");
 			
 			int processed = 0;
 			for(Element elem : quotes) {
 				if(processed > 0 && processed <= 25) {
 					Element para = elem.child(0);
 					
 					Element footPara = null;
 					try {
 						footPara = elem.child(1);
 					} catch(Exception e) {
 					}
 					
 					quoteList.add(new Quote(
 							Integer.parseInt(elem.id().substring(2)),
							Integer.parseInt(para.child(2).html().replaceAll("<.*?>.*?<\\/.*?>", "").trim()),
							Integer.parseInt(para.child(3).html().replaceAll("<.*?>", "").trim()),
							Integer.parseInt(para.child(4).child(0).html().replaceAll("<.*?>", "").trim()),
 							Html.fromHtml(para.select("tt").get(0).html()).toString(),
 							footPara == null ? "" : Html.fromHtml("Fotnot: " + footPara.child(0).html().replaceAll("<.*?>.*?<\\/.*?>", "").trim() + " ").toString()
 						));
 				}
 				
 				processed++;
 				
 				extras.putInt("count", processed - 2);
 			}
 			Log.d("Warpdrive", "Done!");
 		} catch (IOException e) {
 			//TODO: Launch ErrorActivity
 			e.printStackTrace();
 		}
 		
 		return quoteList;
 	}
 }
