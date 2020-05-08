 package com.rubengm.wifileaks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.text.ClipboardManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Main extends ListActivity {
 	List<ScanResult> results = new ArrayList<ScanResult>();
 	WifiAdapter wa = new WifiAdapter();
 	Refrescame refrescame;
 	private boolean manualReload = true;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setListAdapter(wa);
 		comienzaRefresco();
 		getListView().setDividerHeight(0);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		String clave = Keygen.calc(wa.getItem(position));
 		if(clave.equals(Keygen.INCOMPATIBLE)) {
 			//TODO: Hacer algo?
 		} else {
 			ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			cm.setText(clave);
 			Toast.makeText(Main.this, "Clave copiada en el portapapeles", Toast.LENGTH_SHORT).show();
 			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
 		}
 		super.onListItemClick(l, v, position, id);
 	}
 
 	private void paraRefresco() {
 		if(refrescame != null)
 			refrescame.continua = false;
 		refrescame = null;
 	}
 
 	/**
 	 * Lo he dejado de llamar temporalmente hasta que lo hagamos desactivable ;P
 	 */
 	private void comienzaRefresco() {
 		if(refrescame == null) {
 			refrescame = new Refrescame();
 			refrescame.execute();
 		}
 		if (refrescame.continua == false) {
 			refrescame = new Refrescame();
 			refrescame.execute();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		paraRefresco();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onPause() {
 		paraRefresco();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		//comienzaRefresco();
 		super.onResume();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 0, 0, "Refrescar");
 		return super.onCreateOptionsMenu(menu);
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case 0:
 			refresca();
 			manualReload = true;
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	Runnable doRefresca = new Runnable() {
 		@Override
 		public void run() {
 			refresca();
 		}
 	};
 
 	private void refresca() {
 		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		wifiMgr.startScan();
 		List<ScanResult> tmpresults = wifiMgr.getScanResults();
 		if(tmpresults == null) tmpresults = new ArrayList<ScanResult>();
 		if(manualReload) {
 			results = tmpresults;
 			manualReload = false;
 		} else {
			long time = System.currentTimeMillis();
 			for(ScanResult s : tmpresults) {
 				boolean found = false;
 				int i = 0;
 				while(!found && i < results.size()) {
 					if(results.get(i).BSSID.equals(s.BSSID)) {
 						found = true;
 						results.get(i).level = s.level;
 					}
 					i++;
 				}
 			}
 		}
 		((WifiAdapter)getListAdapter()).notifyDataSetChanged();
 		if(results.size() == 0) {
			Toast.makeText(Main.this, "Sin resultados. Tienes la wifi activa?", Toast.LENGTH_LONG).show();
 		} 
 	}
 
 	private class WifiAdapter extends BaseAdapter {
 
 		@Override
 		public int getCount() {
 			return results.size();
 		}
 
 		@Override
 		public ScanResult getItem(int position) {
 			return results.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ScanResult item = getItem(position);
 			View v = null;
 			if(convertView != null) {
 				v = convertView;
 			} else {
 				v = View.inflate(Main.this, R.layout.row_wifi, null);
 			}
 			String key = Keygen.calc(item);
 			if(Keygen.INCOMPATIBLE.equals(key)) {
 				getBackground(v).setBackgroundResource(R.drawable.background_row_ko);
 			} else {
 				getBackground(v).setBackgroundResource(R.drawable.background_row);
 			}
 			getSsid(v).setText(item.SSID + " :: " + item.level + " dBm");
 			getBssid(v).setText(item.BSSID);
 			getKey(v).setText(key);
 			getCapabilities(v).setText(item.capabilities);
 			getVal(v).setProgress(calcProgress(item.level));
 			return v;
 		}
 
 		private int calcProgress(int level) {
 			int progress = 100 + level;
 			if(progress < 0) progress = 0;
 			if(progress > 100) progress = 100;
 			return progress;
 		}
 
 		private LinearLayout getBackground(View v) {
 			return (LinearLayout)v.findViewById(R.id.linearRow);
 		}
 
 		private ProgressBar getVal(View v) {
 			return (ProgressBar)v.findViewById(R.id.val);
 		}
 
 		private TextView getSsid(View v) {
 			return (TextView)v.findViewById(R.id.tvSsid);
 		}
 
 		private TextView getCapabilities(View v) {
 			return (TextView)v.findViewById(R.id.tvCapabilities);
 		}
 
 		private TextView getBssid(View v) {
 			return (TextView)v.findViewById(R.id.tvBssid);
 		}
 
 		private TextView getKey(View v) {
 			return (TextView)v.findViewById(R.id.tvKey);
 		}
 	}
 
 	private class Refrescame extends AsyncTask<Void, Void, Void> {
 		public boolean continua = true;
 		@Override
 		protected Void doInBackground(Void... params) {
 			while(continua) {
 				runOnUiThread(doRefresca);
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 					continua = false;
 				}
 			}
 			return null;
 		}
 
 	}
 }
