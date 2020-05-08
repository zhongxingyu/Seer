 package de.saschahlusiak.hrw.dienststatus.dienste;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.saschahlusiak.hrw.dienststatus.AboutActivity;
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.dienstdetails.DetailActivity;
 import de.saschahlusiak.hrw.dienststatus.model.HRWNode;
 import de.saschahlusiak.hrw.dienststatus.model.HRWService;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class HRWDienststatusActivity extends Activity implements
 		OnItemClickListener {
 	
 	ListView list;
 	ProgressBar progress;
 	DienststatusAdapter adapter;
 	
 	private final String tag = HRWDienststatusActivity.class.getSimpleName();
 	private String level = "all";
 	private final HttpUriRequest uri = new HttpGet(
 			"http://nagvis-pub.hs-weingarten.de/cgi-bin/nagxml.pl?all");
 	private static final String WEBSITE = "http://www.hs-weingarten.de/web/rechenzentrum/dienststatus";
 	private static Document dom = null;
 	private static ArrayList<HRWNode> allnodes = new ArrayList<HRWNode>();
 
 	private void fillLevel(String level) {
		synchronized(allnodes) {
			for (HRWNode node : allnodes) {
				if (level != null) {
					if (node.id.matches("^" + level + "\\.\\d*$"))
						adapter.addNode(node);
				} else {
					if (node.status != 0 && !node.hasSubItems)
						adapter.addNode(node);
				}
 			}
 		}
 		if (level == null)
 			adapter.sortAll();
 	}
 
 	private class RefreshTask extends AsyncTask<Void, Integer, String> {
 		private void parseService(HRWNode node, Node property) {
 			HRWService service = new HRWService(null, null);
 			for (int i = 0; i < property.getChildNodes().getLength(); i++) {
 				Node sub = property.getChildNodes().item(i);
 
 				if (sub.getNodeName().equals("name"))
 					service.name = sub.getTextContent();
 				if (sub.getNodeName().equals("output"))
 					service.output = sub.getTextContent();
 			}
 			if (service.output != null)
 				node.output.add(service);
 		}
 
 		public void parseLevel(Node item, HRWNode HRWparent) {
 			NodeList properties = item.getChildNodes();
 			HRWNode node = new HRWNode(HRWparent);
 			allnodes.add(node);
 			for (int j = 0; j < properties.getLength(); j++) {
 				Node property = properties.item(j);
 				String name = property.getNodeName();
 
 				if (name.equals("name"))
 					node.name = property.getTextContent();
 				if (name.equals("title"))
 					node.title = property.getTextContent();
 				if (name.equals("url"))
 					node.url = property.getTextContent();
 				if (name.equals("duration"))
 					node.duration = property.getTextContent();
 				if (name.equals("acknowledged"))
 					node.acknowledged = Integer.valueOf(property
 							.getTextContent()) == 1;
 				if (name.equals("status"))
 					node.status = Integer.valueOf(property.getTextContent());
 				if (name.equals("menuindex"))
 					node.id = property.getTextContent();
 				if (name.equals("output"))
 					node.output.add(new HRWService(null, property.getTextContent()));
 				if (name.equals("service"))
 					parseService(node, property);
 				if (name.equals("group")) {
 					node.hasSubItems = true;
 					parseLevel(property, node);
 				}
 				if (name.equals("hostentry")) {
 					for (int k = 0; k < property.getChildNodes().getLength(); k++) {
 						Node p = property.getChildNodes().item(k);
 						if (p.getNodeName().equals("service"))
 							parseService(node, p);
 					}
 				}
 			}
 		}
 		
 		
 		@Override
 		protected void onPreExecute() {
 			progress.setMax(3);
 			progress.setProgress(0);
 			progress.setVisibility(View.VISIBLE);
 			super.onPreExecute();
 		}
 
 		@Override
 		protected String doInBackground(Void... arg0) {
 			publishProgress(0, 0);
 			if (dom == null) {
 				try {
 					DefaultHttpClient client = new DefaultHttpClient();
 					final HttpResponse resp = client.execute(uri);
 
 					final StatusLine status = resp.getStatusLine();
 					if (status.getStatusCode() != 200) {
 						Log.d(tag, "HTTP error, invalid server status code: "
 								+ resp.getStatusLine());
 						
 						return getString(R.string.invalid_http_status,
 												resp.getStatusLine());
 					}
 
 					publishProgress(1, 1);
 
 					DocumentBuilderFactory factory = DocumentBuilderFactory
 							.newInstance();
 					DocumentBuilder builder = factory.newDocumentBuilder();
 					dom = builder.parse(resp.getEntity().getContent());
 
 					publishProgress(2, 1);
 				} catch (UnknownHostException e) {
 					Log.e(tag, e.getMessage());
 					dom = null;
 					return "Unknown host: " + e.getMessage();
 				}catch (Exception e) {
 					Log.e(tag, e.getMessage());
 					dom = null;
 					return e.getMessage();
 				}
 
 				/* Should always be != null */
 				if (dom != null) {
 					Element root = dom.getDocumentElement();
 					NodeList items = root.getElementsByTagName("map");
					synchronized(allnodes) {
						allnodes.clear();
 
						NodeList properties = items.item(0).getChildNodes();
						for (int j = 0; j < properties.getLength(); j++) {
							Node property = properties.item(j);
							String name = property.getNodeName();
 
							if (name.equals("group")) {
								parseLevel(property, null);
							}
 						}
 					}
 				}
 			}
 
 			return null;
 		}
 		
 		@Override
 		protected void onCancelled() {
 			adapter.notifyDataSetChanged();
 			progress.setVisibility(View.GONE);
 			Toast.makeText(HRWDienststatusActivity.this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
 
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			progress.setProgress(values[0]);
 			super.onProgressUpdate(values);
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			progress.setVisibility(View.GONE);
 			if (dom != null)
 				fillLevel(level);
 			else
 				Toast.makeText(HRWDienststatusActivity.this, result, Toast.LENGTH_SHORT).show();
 			adapter.notifyDataSetChanged();
 			super.onPostExecute(result);
 		}
 	}
 	
 	RefreshTask refreshTask = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Intent intent;
 		Bundle extras;
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_activity);
 		
 		list = (ListView) findViewById(R.id.list);
 		progress = (ProgressBar) findViewById(R.id.progress);
 
 		intent = getIntent();
 
 		level = "all";
 		// level = null;
 		if (intent != null) {
 			extras = intent.getExtras();
 			if (extras != null) {
 				level = extras.getString("level");
 				setTitle(extras.getString("name"));
 			}
 		} else
 			setTitle(R.string.app_name);
 
 		adapter = new DienststatusAdapter(this, level == null);
 		list.setAdapter(adapter);
 
 		list.setOnItemClickListener(this);
 		registerForContextMenu(list);
 
 		if (dom != null) {
 			fillLevel(level);
 		} else {
 			if (refreshTask != null) {
 				refreshTask.cancel(true);
 				try {
 					refreshTask.get();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			refreshTask = new RefreshTask();
 			refreshTask.execute();
 		}
 	}
 
 	public void showDetails(HRWNode node) {
 		Intent intent = new Intent(this, DetailActivity.class);
 		intent.putExtra("name", node.name);
 		intent.putExtra("duration", node.duration);
 		intent.putExtra("url", node.url);
 		intent.putExtra("label", node.title);
 		intent.putExtra("status", node.status);
 		intent.putExtra("acknowledged", node.acknowledged);
 		intent.putExtra("name", node.name);
 		if (node.getParent() != null)
 			intent.putExtra("path", node.getParent().getPath(true));
 		if (node.output != null)
 			intent.putExtra("output", node.output);
 		startActivity(intent);
 	}
 
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		HRWNode node;
 		String newtitle;
 
 		node = (HRWNode) adapter.getItem(position);
 		if (node.hasSubItems == false) {
 			showDetails(node);
 			return;
 		}
 
 		Intent viewIntent = new Intent(this, HRWDienststatusActivity.class);
 
 		newtitle = node.getPath(true);
 		viewIntent.putExtra("level", node.id);
 		viewIntent.putExtra("name", newtitle);
 		startActivity(viewIntent);
 	}
 
 	@Override
 	public void onCreateContextMenu(android.view.ContextMenu menu, View v,
 			android.view.ContextMenu.ContextMenuInfo menuInfo) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.contextmenu, menu);
 
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		HRWNode node = (HRWNode) adapter.getItem((int) info.id);
 
 		menu.setHeaderTitle(node.name);
 
 		if (node.url == null)
 			menu.findItem(R.id.openinbrowser).setEnabled(false);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		HRWNode node;
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		node = (HRWNode) adapter.getItem((int) info.id);
 		if (node == null)
 			return false;
 
 		switch (item.getItemId()) {
 		case R.id.openinbrowser:
 			if (node.url == null)
 				return false;
 			Intent intent = new Intent("android.intent.action.VIEW", Uri
 					.parse(node.url));
 			startActivity(intent);
 			return true;
 
 		case R.id.details:
 			showDetails(node);
 
 			return true;
 			
 		case R.id.sendemail:
 			try {
 				intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("plain/text");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "rz-service@hs-weingarten.de" });
 				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
 				intent.putExtra(Intent.EXTRA_TEXT, "Siehe: <br>" + WEBSITE + " ,<br>" + node.getFullPath() + "<br><br>");
 				startActivity(intent);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 			
 			
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.optionsmenu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.refresh) {
 			if (refreshTask != null) {
 				refreshTask.cancel(true);
 				try {
 					refreshTask.get();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			dom = null;
 			adapter.clear();
 			adapter.notifyDataSetChanged();
 			
 			refreshTask = new RefreshTask();
 			refreshTask.execute();
 			
 			return true;
 		}
 		/*
 		 * if (item.getItemId() == R.id.preferences) { Intent intent = new
 		 * Intent(this, HRWPreferences.class); startActivity(intent); }
 		 */
 		if (item.getItemId() == R.id.about) {
 			Intent intent = new Intent(this, AboutActivity.class);
 			startActivity(intent);
 		}
 		if (item.getItemId() == R.id.gotowebsite) {
 			Intent intent = new Intent(
 					"android.intent.action.VIEW",
 					Uri.parse(WEBSITE));
 			startActivity(intent);
 			return true;
 		}
 		if (item.getItemId() == R.id.sendemail) {
 			try {
 				Intent intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("plain/text");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "rz-service@hs-weingarten.de" });
 				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
 				intent.putExtra(Intent.EXTRA_TEXT, "Siehe: " + WEBSITE);
 				startActivity(intent);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
