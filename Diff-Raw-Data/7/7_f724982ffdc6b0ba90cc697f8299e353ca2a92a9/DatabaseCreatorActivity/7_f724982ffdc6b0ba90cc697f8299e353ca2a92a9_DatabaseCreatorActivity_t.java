 package net.hath.hondroid.database;
 
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.hath.hondroid.MainActivity;
 import net.hath.hondroid.R;
 import net.hath.hondroid.network.Downloader;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class DatabaseCreatorActivity extends Activity {
 
 	private static final String HON_URL = "http://www.heroesofnewerth.com/heroes.php?hero_id=2";
 	private static final String HON_ICON = "http://www.heroesofnewerth.com/images/heroes/%d/icon_128.jpg";
 	private static final String HON_SPELL = "http://www.heroesofnewerth.com/images/heroes/%d/ability%d_128.jpg";
 	private static final String TAG = "DatabaseCreator";
 
 	private ProgressBar pb;
 	private TextView tv;
 	private Button btn;
 	private DatabaseAdapter da;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.db_creator);
 
 		da = new DatabaseAdapter(this);
 
 		tv = (TextView) findViewById(R.id.dbcreator_text);
 		pb = (ProgressBar) findViewById(R.id.dbcreator_progress);
 		btn = (Button) findViewById(R.id.dbcreator_btn);
 
 		btn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				new DownloadManager().execute();
 				btn.setEnabled(false);
 				tv.setText(getString(R.string.dbcreator_startdl));
 			}
 		});
 
 	}
 
 	private ArrayList<Integer> getIDs(String url) {
 		ArrayList<Integer> ids = new ArrayList<Integer>();
 		String html = Downloader.downloadHTML(url);
		String pattern = "heroes/(\\d+)/icon_128";
 		Pattern p = Pattern.compile(pattern);
 		Matcher m = p.matcher(html);
 		while (m.find()) {
 			ids.add(Integer.parseInt(m.group(1)));
 		}
 		Collections.sort(ids);
 
 		/**  Temp loop for emulating ID list of 5 numbers only
 		ArrayList<Integer> temp = new ArrayList<Integer>();
 		for(int i=0;i<5;i++){
 			temp.add(ids.get(i));
 		}
 		*/
 		
		Log.d(TAG, ids.size()+" IDs found.");
 		return ids;
 	}
 
 	private SparseArray<String> getURLs(ArrayList<Integer> ids) {
 		SparseArray<String> sa = new SparseArray<String>();
 		for (int id : ids) {
 			sa.append(id, HON_URL.substring(0, 50) + id);
 		}
 		return sa;
 	}
 
 	/**
 	 * 
 	 * @param ids
 	 *            ArrayList of all valid hero IDs
 	 * @return 'Map' of IDs and a Bitmap array
 	 */
 	private SparseArray<String[]> getImageURLs(ArrayList<Integer> ids) {
 		SparseArray<String[]> sa = new SparseArray<String[]>();
 		for (int id : ids) {
 			String[] ar = new String[5];
 			for (int i = 0; i < ar.length; i++) {
 				if (i == 0)
 					ar[i] = String.format(HON_ICON, id);
 				else
 					ar[i] = String.format(HON_SPELL, id, i);
 			}
 			sa.put(id, ar);
 		}
 		return sa;
 	}
 
 	// TODO: Clean up this whole method. It looks like shit. 
 	private void parseAndInsert(String html, int id) {
 		// Name
 		String pattern = "hero_name\\\">([\\w\\s-]+)";
 		Pattern p = Pattern.compile(pattern);
 		Matcher m = p.matcher(html);
 		String name = m.find() ? m.group(1) : "";
 
 		// Faction
 		pattern = "Team:</span>\\s(\\w+)";
 		p = Pattern.compile(pattern);
 		m = p.matcher(html);
 		String faction = m.find() ? m.group(1) : "";
 
 		// Primary attribute
 		pattern = "Type:</span>\\s(\\w+)";
 		p = Pattern.compile(pattern);
 		m = p.matcher(html);
 		String attribute = m.find() ? m.group(1) : "";
 
 		Log.i(TAG, String.format("PUT %d, %s, %s, %s", id, name, faction, attribute));
 		da.addHero(id, name, faction, attribute);
 
 		// //////////
 		// Spells //
 		// //////////
 		List<String[]> spellStrings = new ArrayList<String[]>();
 		pattern = "<div class=\\\"simple\\\" style=\\\"display:none\\\">\\s*\\n.*";
 		p = Pattern.compile(pattern);
 		m = p.matcher(html);
 		while (m.find()) {
 			String[] sa = { "", m.group() };
 			spellStrings.add(sa);
 		}
 
 		pattern = "<[^>]*>";
 		p = Pattern.compile(pattern);
 		for (int i = 0; i < spellStrings.size(); i++) {
 			m = p.matcher(spellStrings.get(i)[1]);
 			StringBuffer sb = new StringBuffer();
 			while (m.find()) {
 				m.appendReplacement(sb, " ");
 			}
 			m.appendTail(sb);
 			// Removes multiple white space in a row, around slahses and before periods.  
 			String[] s = { "", sb.toString().replaceAll("\\s+", " ").trim().replaceAll("[/\\s]{3}", "/").replaceAll("\\s[\\.]", "\\.") };
 			spellStrings.set(i, s);
 		}
 
 		// Spell names
 		pattern = "128\\.jpg\\\">\\s*<h1>([\\w\\s'-]+)</h1>";
 		p = Pattern.compile(pattern);
 		m = p.matcher(html);
 
 		int counter = 0;
 		while (m.find()) {
 			if (!m.group(1).equals("Hero Bio"))
 				spellStrings.get(counter++)[0] = (m.group(1));
 		}
 
 		for (int i = 0; i < 4; i++) {
 			da.addSpell(id, i, spellStrings.get(i)[0], spellStrings.get(i)[1]);
 		}
 	}
 
 	private class DownloadManager extends AsyncTask<Void, Integer, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// TODO Auto-generated method stub
 			Log.i(TAG, "Starting DB update");
 
 			ArrayList<Integer> ids = getIDs(HON_URL);
 			SparseArray<String> urls = getURLs(ids);
 			SparseArray<String[]> imageurls = getImageURLs(ids);
 			int counter = 0;
 			for (int i : ids) {
 				// Downloads the urls, parses them, and inserts them into the
 				// database
 				publishProgress(counter++, ids.size());
 
 				String url = urls.get(i);
 				String site = Downloader.downloadHTML(url);
				Log.d(TAG, "URL length: "+site.length());
 				parseAndInsert(site, i);
 
 				// Downloads and saves the images.
 				for (int j = 0; j < 5; j++) {
 					Bitmap bm = Downloader.downloadBitmap(imageurls.get(i)[j]);
 					try {
 						FileOutputStream out = openFileOutput(String.format(j==0?"hero_%d":("spell_%d_"+j),i), Context.MODE_PRIVATE);
 						bm.compress(Bitmap.CompressFormat.PNG, 90, out);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			// TODO Auto-generated method stub
 			super.onProgressUpdate(values);
 			pb.setMax(values[1]);
 			pb.setProgress(values[0]);
 			tv.setText(getString(R.string.downloading) + values[0] + "/" + values[1]);
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			// TODO Auto-generated method stub
 			super.onPostExecute(result);
 			DatabaseCreatorActivity.this.startActivity(new Intent(DatabaseCreatorActivity.this, MainActivity.class));
 			DatabaseCreatorActivity.this.finish();
 		}
 	}
 
 }
