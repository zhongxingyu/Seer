 package it.rikiji.android.walrusdict;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.Gson;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	EditText input;
 	CustomListAdapter adapter;
 	public static final String PROT = "http";
 	public static final String HOST = "192.168.1.201";
 	public static final String PORT = "8331";
 	protected ArrayList<String[]> data;
 	protected String[][] dataOrig;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		new AnkiCommand().execute("sync");
 		this.input = (EditText) findViewById(R.id.input);
 		input.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable ed) {
 				String s = input.getText().toString();
				if (s.length() > 2)
 					new DownloadData().execute(s);
 				Log.d("onTextChanged", input.getText().toString());
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 		});
 
 		ListView list = (ListView) findViewById(R.id.list);
 
 		this.data = new ArrayList<String[]>();
 		this.adapter = new CustomListAdapter(this, R.layout.list_item, data);
 		list.setAdapter(this.adapter);
 
 		list.setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				String[] e = MainActivity.this.data.get(position);
 				String[] card = generate_card(e);
 				new AnkiCommand().execute("push", "y", card[0], card[1]);
 				new AnkiCommand().execute("push", "n", card[1], card[0]);
 				Toast.makeText(getApplicationContext(), "Sending to anki...",
 						Toast.LENGTH_SHORT).show();
 				return true;
 			}
 		});
 	}
 
 	private String[] generate_card(String[] entry) {
 		String[] card = new String[2];
 		/* front */
 		card[0] = entry[1] + "<br>" + entry[0];
 		String rev = entry[0].charAt(3) + "" + entry[0].charAt(4) + "-"
 				+ entry[0].charAt(0) + "" + entry[0].charAt(1);
 		card[1] = entry[2] + "<br>" + rev;
 		return card;
 	}
 
 	private String format(String[] input, String delimiter) {
 		StringBuilder sb = new StringBuilder();
 		for (String value : input) {
 			sb.append(value);
 			sb.append(delimiter);
 		}
 		int length = sb.length();
 		if (length > 0) {
 			// Remove the extra delimiter
 			sb.setLength(length - delimiter.length());
 		}
 		return sb.toString();
 	}
 
 	private Menu getListAdapter() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_sync:
			new AnkiCommand().execute("sync", "y");
 			return true;
 		case R.id.menu_author:
 			Intent brt = new Intent(Intent.ACTION_VIEW,
 					Uri.parse("https://twitter.com/rikiji"));
 			startActivity(brt);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public static String root_url() {
 		return PROT + "://" + HOST + ":" + PORT + "/";
 	}
 
 	private class DownloadData extends AsyncTask {
 
 		private final String url = root_url() + "query?val=";
 
 		protected void onPostExecute(Object result) {
 
 			Gson gson = new Gson();
 			Log.d("debug", "fetched:  " + result);
 			MainActivity.this.dataOrig = gson.fromJson((String) result,
 					String[][].class);
 			MainActivity.this.data.clear();
 			int i = 0;
 			for (String[] x : MainActivity.this.dataOrig) {
 				MainActivity.this.data.add(x);
 			}
 			MainActivity.this.adapter.notifyDataSetChanged();
 		}
 
 		protected Object doInBackground(Object... params) {
 
 			String val = params[0].toString();
 			String result = "[]";
 			try {
				URL apiUrl = new URL(this.url + URLEncoder.encode(val, "utf-8"));
 				HttpURLConnection conn = (HttpURLConnection) apiUrl
 						.openConnection();
 				conn.setConnectTimeout(30000);
 				conn.setReadTimeout(30000);
 				conn.setRequestMethod("GET");
 				conn.connect();
 				InputStream is = conn.getInputStream();
 				BufferedReader rd = new BufferedReader(
 						new InputStreamReader(is), 24000);
 				String line;
 				StringBuilder sb = new StringBuilder();
 				while ((line = rd.readLine()) != null) {
 					sb.append(line);
 				}
 				rd.close();
 				result = sb.toString();
 			} catch (Exception e) {
 				Log.d("error", e.getMessage());
 			}
 			return result;
 		}
 	}
 
 	private class AnkiCommand extends AsyncTask {
 
 		private final String url = root_url();
 		private boolean popup = false;
 
 		protected void onPostExecute(Object result) {
 
 			if (result != null) {
 				Gson gson = new Gson();
 				Log.d("debug", "fetched:  " + result);
 				String[] res = gson.fromJson((String) result, String[].class);
 				if (this.popup)
 					Toast.makeText(getApplicationContext(), res[0],
 							Toast.LENGTH_LONG).show();
 			} else {
 				Toast.makeText(getApplicationContext(), "error",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 		protected Object doInBackground(Object... params) {
 
 			String result = "[]";
 			URL apiUrl;
 			if (params.length > 1 && params[1].equals("y"))
 				this.popup = true;
 
 			try {
 				if (params[0].equals("push")) {
 					/* pushing a new card to anki */
 					apiUrl = new URL(this.url + "push?front="
 							+ URLEncoder.encode((String) params[2], "utf-8")
 							+ "&back="
 							+ URLEncoder.encode((String) params[3], "utf-8"));
 				} else {
 					/* sync deck */
 					apiUrl = new URL(this.url + "sync");
 				}
 
 				HttpURLConnection conn = (HttpURLConnection) apiUrl
 						.openConnection();
 				conn.setConnectTimeout(30000);
 				conn.setReadTimeout(30000);
 				conn.setRequestMethod("POST");
 				conn.connect();
 				InputStream is = conn.getInputStream();
 				BufferedReader rd = new BufferedReader(
 						new InputStreamReader(is), 4096);
 				String line;
 				StringBuilder sb = new StringBuilder();
 				while ((line = rd.readLine()) != null) {
 					sb.append(line);
 				}
 				rd.close();
 				result = sb.toString();
 			} catch (Exception e) {
 				Log.d("error", e.getMessage());
 			}
 			return result;
 		}
 	}
 }
