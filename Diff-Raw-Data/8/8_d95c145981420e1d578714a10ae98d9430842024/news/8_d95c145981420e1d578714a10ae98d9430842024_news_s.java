 package com.messedagliavr.messeapp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class news extends ListActivity {
 
 	public static final String TITLE = "title";
 	public static final String DESC = "description";
 	public String[] titolim;
 	public String[] descrizionim;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		new connection().execute();
 		System.out.println("prova1");
 
 	}
 
 	public class connection extends
 			AsyncTask<Void, Void, HashMap<String, ArrayList<String>>> {
 
 		public HashMap<String, ArrayList<String>> doInBackground(Void... params) {
 			HashMap<String, ArrayList<String>> temhashmap = new HashMap<String, ArrayList<String>>();
 			ArrayList<String> titoli = new ArrayList<String>();
 			ArrayList<String> descrizioni = new ArrayList<String>();
 			System.out.println("prova2");
 			// All static variables
			final String URL = "http://www.messedaglia.it/index.php/archivio-news?format=feed&type=rss";
 			// XML node keys
 			final String ITEM = "item"; // parent node
 			final String TITLE = "title";
 			final String DESC = "description";
 			Element e = null;
 			ArrayList<HashMap<String, String>> menuItems = new ArrayList<HashMap<String, String>>();
 
 			XMLParser parser = new XMLParser();
 			String xml = parser.getXmlFromUrl(URL); // getting XML
 			Document doc = parser.getDomElement(xml); // getting DOM element
 			NodeList nl = doc.getElementsByTagName(ITEM);
 
 			// looping through all item nodes <item>
 			for (int i = 0; i < nl.getLength(); i++) {
 				// creating new HashMap
 				HashMap<String, String> map = new HashMap<String, String>();
 				e = (Element) nl.item(i);
 				// adding each child node to HashMap key => value
 				map.put(TITLE, parser.getValue(e, TITLE));
 				map.put(DESC, parser.getValue(e, DESC));
 				// adding HashList to ArrayList
 				menuItems.add(map);
 
 			}
 
 			for (int c = 0; c < nl.getLength(); c++) {
 				e = (Element) nl.item(c);
 
 				titoli.add(parser.getValue(e, TITLE));
 				descrizioni.add(parser.getValue(e, DESC));
 
 			}
 			temhashmap.put("titoli", titoli);
 			temhashmap.put("descrizioni", descrizioni);
 			return temhashmap;
 		}
 
 		public void onPostExecute(HashMap<String, ArrayList<String>> resultmap) {
 			if (resultmap.size() > 0) {
 				// get titoli ArrayList here
 				final ArrayList<String> titoli = resultmap.get("titoli");
 				// titolim = titoli.toArray(new String[titoli.size()]);
 				// get descrizioni ArrayList here
 				final ArrayList<String> descrizioni = resultmap
 						.get("descrizioni");
 				// descrizionim = descrizioni.toArray(new
 				// String[titoli.size()]);
 				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 						news.this, android.R.layout.simple_list_item_1, titoli);
 				setContentView(R.layout.list_item);
 				ListView listView = (ListView) news.this
 						.findViewById(android.R.id.list);
 				listView.setAdapter(adapter);
 				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					public void onItemClick(AdapterView<?> parentView,
 							View childView, int position, long id) {
 						// Here write your code for starting the new activity on
 						// selection of list item
 						System.out.println("provaclick2");
 						Intent intent = new Intent(news.this,
 								ListItemSelected.class);
 						intent.putExtra(TITLE, titoli.get(position));
 						System.out.println(titoli.get(position));
 						System.out.println(" - ");
 						System.out.println(descrizioni.get(position));
 						intent.putExtra(DESC, descrizioni.get(position));
 						startActivity(intent);
 					}
 
 					public void onNothingSelected(AdapterView<?> parentView) {
 					}
 				});
 			}
 
 		}
 	}
 }
