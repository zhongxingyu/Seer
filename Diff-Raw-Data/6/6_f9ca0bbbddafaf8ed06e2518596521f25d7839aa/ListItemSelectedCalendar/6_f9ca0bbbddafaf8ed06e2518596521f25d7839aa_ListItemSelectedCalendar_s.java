 package com.messedagliavr.messeapp;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.messedagliavr.messeapp.calendar.eventparser;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ListItemSelectedCalendar extends Activity {
 
 	@Override
 	public void onBackPressed() {
 		startActivity(new Intent(this, calendar.class));
 		setContentView(R.layout.list_item);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.list_item_selected, menu);
 		return true;
 	}
 	
 	public String idical=null;
 	
 	public class eventparser extends AsyncTask<Void, Void, String[]> {
 		@Override
 		protected String[] doInBackground(Void... params) {
 			String ical = "http://lookedpath.altervista.org/test.php?id="
 					+ idical;
 			XMLParser parser = new XMLParser();
 			String xml = parser.getXmlFromUrl(ical);
 			Document doc = parser.getDomElement(xml);
 			NodeList nl = doc.getElementsByTagName("VEVENT");
 
 			System.out.println("Dopo elements");
 			String[] dati = { "", "", "", "", "" };
 			Element e = (Element) nl.item(0);
 			dati[0] = parser.getValue(e, "SUMMARY");
 			int l=parser.getValue(e, "DESCRIPTION").length()-3;
 			dati[1] = parser.getValue(e, "DESCRIPTION").substring(4,l);
 			dati[2] = parser.getValue(e, "LOCATION");
 			dati[3] = parser.getValue(e, "DTSTART");
 			dati[4] = parser.getValue(e, "DTEND");
 			System.out.println("Dopo array");
 			System.out.println(dati.toString());
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"yyyyMMdd'T'HHmmss");
 			Date fine = null;
 			Date inizio = null;
 			try {
 				fine = dateFormat.parse(dati[4].toString());
 				inizio = dateFormat.parse(dati[3].toString());
 				System.out.println("Prima secondo try");
 				Intent intent = new Intent(Intent.ACTION_INSERT)
 						.setType("vnd.android.cursor.item/event")
 						.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
 								inizio.getTime())
 						.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
 								fine.getTime())
 						.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
 						.putExtra(Events.TITLE, dati[0])
 						.putExtra(Events.DESCRIPTION, dati[1])
 						.putExtra(Events.EVENT_LOCATION,
 								dati[2] + " A. Messedaglia");
 				startActivity(intent);
 			} catch (java.text.ParseException e1) {
 				e1.printStackTrace();
 			} catch (Exception e2) {
 				e2.printStackTrace();
 				Toast.makeText(ListItemSelectedCalendar.this, R.string.noapilevel,
 						Toast.LENGTH_LONG).show();
 			}
 			return dati;
 		}
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.ical:
 			Intent intent = getIntent();
 			String ical=intent.getStringExtra(calendar.ICAL);
 			int l = ical.length() - 5;
 			idical = ical.substring(3, l);
 			new eventparser().execute();
 			break;
 
 		}
 		return true;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list_item_selected);
 		Intent intent = getIntent();
 		String titolorw = intent.getStringExtra(calendar.TITLE);
 		String descrizionerw = intent.getStringExtra(calendar.DESC); 
 		TextView titoloview = (TextView) findViewById(R.id.TitoloView);
 		Spanned titolo = Html.fromHtml(titolorw);
 		titoloview.setText(titolo);
 		WebView descrizioneview = (WebView) findViewById(R.id.DescrizioneView);
 			descrizioneview.loadData(descrizionerw, "text/html", "UTF-8");
 	}
 
 }
