 package edu.berkeley.cs160.smartnature;
 
 import java.io.IOException;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import edu.berkeley.cs160.smartnature.StartScreen.GardenAdapter;
 
 
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class Encyclopedia extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener{
 	
 	static ResultAdapter adapter;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.encycl);
 		
 		Button searchButton = (Button) findViewById(R.id.searchButton);
 		
 		searchButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick (View v) {
         		EditText search = (EditText)findViewById(R.id.searchText);
         		String searchURL = "http://www.plantcare.com/encyclopedia/search.aspx?q=" + search.getText().toString();
         		//ListView content = (LinearLayout) findViewById(R.id.content);
 				//content.removeAllViews();
         		
         		try {
         			
         			ArrayList <SearchResult> resultList = new ArrayList <SearchResult> ();
 					Document doc = Jsoup.connect(searchURL).get();
 					Element resultBox = doc.getElementById("searchEncyclopedia");
 					//if(resultBox.attr("id").equals("_ctl0_mainHolder_noresults"))
 					final Elements results = resultBox.child(1).children();
 					
 					int numResults = results.size();
 					for(int i = 0; i < numResults; i++){
 						
 						final Element next = results.first();
 						String plantURL = "http://www.plantcare.com" + next.child(0).child(0).child(0).attr("src");
 						String name = next.child(1).text();
 						String altNames = next.child(2).text();
 						resultList.add(new SearchResult (name, altNames, plantURL));
 						/*
 						LinearLayout border = new LinearLayout(Encyclopedia.this);
 						LinearLayout plantEntry = new LinearLayout(Encyclopedia.this);
 						LinearLayout plantText = new LinearLayout(Encyclopedia.this);
 						ImageView pic = new ImageView(Encyclopedia.this);
 						TextView name = new TextView(Encyclopedia.this);
 						TextView altNames = new TextView(Encyclopedia.this);
 						
 						border.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
 						border.setOrientation(LinearLayout.VERTICAL);
 						border.setBackgroundColor(Color.BLACK);
 						border.setPadding(0, 2, 0, 2);
 						
 						plantEntry.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
 						plantEntry.setBackgroundColor(Color.WHITE);
 						plantEntry.setPadding(10, 0, 10, 0);
 						
 						//create pic
 						pic.setLayoutParams(new LayoutParams(60,60));
 						//Toast.makeText(Encycl.this, results.first().child(0).child(0).attr("href"), Toast.LENGTH_SHORT).show();
 						
 						String picURL = "http://www.plantcare.com" + next.child(0).child(0).child(0).attr("src");
 						try {
 							pic.setImageBitmap(BitmapFactory.decodeStream((new URL(picURL)).openConnection().getInputStream()));
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						pic.setPadding(0, 10, 10, 10);
 
 						plantText.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
 						plantText.setOrientation(LinearLayout.VERTICAL);
 						plantText.setBackgroundColor(Color.WHITE);
 						
 						name.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
 						name.setTextColor(Color.BLUE);
 						name.setTextSize(14);
 						name.setTypeface(Typeface.DEFAULT_BOLD);
 						name.setText(next.child(1).text());
 						name.setLinksClickable(true);
 						name.setHint(next.child(1).attr("href"));
 						
 						
 						altNames.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
 						altNames.setTextColor(Color.BLACK);
 						altNames.setTextSize(14);
 						altNames.setAutoLinkMask(Linkify.ALL);
 						altNames.setLinksClickable(true);
 						altNames.setText(next.child(2).text());
 						altNames.setPadding(0, 5, 0, 0);
 						
 						name.setOnClickListener(new OnClickListener(){
 							public void onClick(View v){
 								//Toast.makeText(Encycl.this, name.getHint(), Toast.LENGTH_SHORT).show();
 								
 								String plantURL = "http://www.plantcare.com/encyclopedia/" + next.child(1).attr("href");
 								Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(plantURL));
 				        		startActivity(browserIntent);
 							}
 						});
 						
 						plantText.addView(name);
 						plantText.addView(altNames);
 						plantEntry.addView(pic);
 						plantEntry.addView(plantText);
 						border.addView(plantEntry);
 						content.addView(border);
 						
 						results.remove(0);
 						*/
 					}
 					//ListView listView = (ListView) findViewById(R.id.searchList);
 
 					adapter = new ResultAdapter(Encyclopedia.this, R.layout.search_item, resultList);
					//this.setListAdapter(adapter);
 					} catch (IOException e) {
 					
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				//Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(searchURL));
         		//startActivity(browserIntent);
         		
 
         	
         	}
 
 			
 		});
         
         
         
 
     }
     
     class ResultAdapter extends ArrayAdapter<SearchResult> {
 		private ArrayList<SearchResult> items;
 		private LayoutInflater li;
 		
 		public ResultAdapter(Context context, int textViewResourceId, ArrayList<SearchResult> items) {
 			super(context, textViewResourceId, items);
 			li = ((ListActivity) context).getLayoutInflater();
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null)
 				v = li.inflate(R.layout.search_item, null);
 			SearchResult s = items.get(position);
 			((TextView) v.findViewById(R.id.name)).setText(s.getName());
 			((TextView) v.findViewById(R.id.altNames)).setText(s.getName());
 			try {
 				((ImageView) v.findViewById(R.id.searchPic)).setImageBitmap(BitmapFactory.decodeStream((new URL(s.getPicURL())).openConnection().getInputStream()));
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return v;
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
