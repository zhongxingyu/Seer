 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt2;
 
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import cm.aptoide.pt2.contentloaders.ImageLoader;
 import cm.aptoide.pt2.contentloaders.SimpleCursorLoader;
 import cm.aptoide.pt2.services.AIDLServiceDownloadManager;
 import cm.aptoide.pt2.services.ServiceDownloadManager;
 import cm.aptoide.pt2.views.ViewApk;
 import cm.aptoide.pt2.views.ViewCache;
 import cm.aptoide.pt2.views.ViewDownloadManagement;
 
 public class ScheduledDownloads extends FragmentActivity implements LoaderCallbacks<Cursor>{
 	private Database db;
 	HashMap<String,ScheduledDownload> planets = new HashMap<String, ScheduledDownload>();
 	ListView lv;
 	CursorAdapter adapter;
 	ImageLoader imageLoader;
 	private Button installButton;
 	
 
 	private boolean isRunning = false;
 
 	private AIDLServiceDownloadManager serviceDownloadManager = null;
 
 	private boolean serviceManagerIsBound = false;
 
 	private ServiceConnection serviceManagerConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the object we can use to
 			// interact with the service.  We are communicating with the
 			// service using AIDL, so here we set the remote service interface.
 			serviceDownloadManager = AIDLServiceDownloadManager.Stub.asInterface(service);
 			serviceManagerIsBound = true;
 			
 			Log.v("Aptoide-ScheduledDownloads", "Connected to ServiceDownloadManager");
 	        
 			continueLoading();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			serviceManagerIsBound = false;
 			serviceDownloadManager = null;
 			
 			Log.v("Aptoide-ScheduledDownloads", "Disconnected from ServiceDownloadManager");
 		}
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstance) {
 		super.onCreate(savedInstance);
 		setContentView(R.layout.sch_downloadempty);
 
 		if(!isRunning){
 			isRunning = true;
 
 			if(!serviceManagerIsBound){
 	    		bindService(new Intent(this, ServiceDownloadManager.class), serviceManagerConnection, Context.BIND_AUTO_CREATE);
 	    	}
 			
 		}
 		
 	}
 	
 	private void continueLoading(){
 		lv = (ListView) findViewById(android.R.id.list);
 		db= Database.getInstance(this);
 		
 		imageLoader = ImageLoader.getInstance(this);
 		adapter = new CursorAdapter(this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
 			
 			@Override
 			public View newView(Context context, Cursor arg1, ViewGroup arg2) {
 				return LayoutInflater.from(context).inflate(R.layout.sch_download, null);
 			}
 			
 			@Override
 			public void bindView(View convertView, Context arg1, Cursor c) {
 				// Planet to display
 			      ScheduledDownload scheduledDownload = planets.get(c.getString(0)); 
 
 			      // The child views in each row.
 			      CheckBox checkBoxScheduled ; 
 			      TextView textViewName ;
 			      TextView textViewVersion ;
 			      ImageView imageViewIcon ; 
 			      
 			      // Create a new row view
 			      if ( convertView.getTag() == null ) {
 			        
 			        // Find the child views.
 			        textViewName = (TextView) convertView.findViewById(R.id.name);
 			        textViewVersion = (TextView) convertView.findViewById(R.id.appversion);
 			        checkBoxScheduled = (CheckBox) convertView.findViewById(R.id.schDwnChkBox);
 			        imageViewIcon = (ImageView) convertView.findViewById(R.id.appicon);
 			        // Optimization: Tag the row with it's child views, so we don't have to 
 			        // call findViewById() later when we reuse the row.
 			        convertView.setTag( new Holder(textViewName,textViewVersion,checkBoxScheduled,imageViewIcon) );
 
 			        // If CheckBox is toggled, update the planet it is tagged with.
 			        checkBoxScheduled.setOnClickListener( new View.OnClickListener() {
 			          public void onClick(View v) {
 			            CheckBox cb = (CheckBox) v ;
 			            ScheduledDownload schDownload = (ScheduledDownload) cb.getTag();
 			            schDownload.setChecked( cb.isChecked() );
 			          }
 			        });        
 			      }
 			      // Reuse existing row view
 			      else {
 			        // Because we use a ViewHolder, we avoid having to call findViewById().
 			        Holder viewHolder = (Holder) convertView.getTag();
 			        checkBoxScheduled = viewHolder.checkBoxScheduled ;
 			        textViewVersion = viewHolder.textViewVersion;
 			        textViewName = viewHolder.textViewName ;
 			        imageViewIcon = viewHolder.imageViewIcon ;
 			      }
 
 			      // Tag the CheckBox with the Planet it is displaying, so that we can
 			      // access the planet in onClick() when the CheckBox is toggled.
 			      checkBoxScheduled.setTag( scheduledDownload ); 
 			      
 			      // Display planet data
 			      checkBoxScheduled.setChecked( scheduledDownload.isChecked() );
 			      textViewName.setText( scheduledDownload.getName() );  
 			      textViewVersion.setText( ""+scheduledDownload.getVername() );
 			      
 			      // ((TextView) v.findViewById(R.id.isinst)).setText(c.getString(3));
 			      // ((TextView) v.findViewById(R.id.name)).setText(c.getString(2));
 			      String hashCode = (c.getString(2)+"|"+c.getString(3));
			      Log.d("ScheduledDownloadsIcons",hashCode);
 			      imageLoader.DisplayImage(hashCode, imageViewIcon, arg1, hashCode);
 			}
 		};
 		lv.setAdapter(adapter);
 		getSupportLoaderManager().initLoader(0, null, this);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View item, int arg2,
 					long arg3) {
 				ScheduledDownload planet = (ScheduledDownload) ((Holder)item.getTag()).checkBoxScheduled.getTag();
 		        planet.toggleChecked();
 		        Holder viewHolder = (Holder) item.getTag();
 		        viewHolder.checkBoxScheduled.setChecked( planet.isChecked() );
 			}
 		});
 		installButton = (Button) findViewById(R.id.sch_down);
 		installButton.setText(getText(R.string.schDown_installselected));
 		installButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				if(isAllChecked()){
 					for(String planet : planets.keySet()){
 						if (planets.get(planet).checked){
 							ScheduledDownload schDown = planets.get(planet);
 							ViewApk apk = new ViewApk();
 							apk.setApkid(schDown.getApkid());
 							apk.setVercode(schDown.getVercode());
 							apk.setVername(schDown.getVername());
 							apk.setMd5(schDown.getMd5());
 							try {
 								serviceDownloadManager.callStartDownload(new ViewDownloadManagement(schDown.getUrl(),apk,new ViewCache(apk.hashCode(), apk.getMd5())));
 							} catch (RemoteException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 
 				} else {
 					Toast.makeText(ScheduledDownloads.this, R.string.schDown_nodownloadselect,
 							Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 		if(getIntent().hasExtra("downloadAll")){
 			AlertDialog alrt = new AlertDialog.Builder(this).create();
 			alrt.setMessage(getText(R.string.schDown_install));
 			alrt.setButton(Dialog.BUTTON_POSITIVE,getText(R.string.btn_yes), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					for(String planet : planets.keySet()){
 						ScheduledDownload schDown = planets.get(planet);
 						ViewApk apk = new ViewApk();
 						apk.setApkid(schDown.getApkid());
 						apk.setVercode(schDown.getVercode());
 						apk.setVername(schDown.getVername());
 						apk.setMd5(schDown.getMd5());
 						try {
 							serviceDownloadManager.callStartDownload(new ViewDownloadManagement(schDown.getUrl(),apk,new ViewCache(apk.hashCode(), apk.getMd5())));
 						} catch (RemoteException e) {
 							e.printStackTrace();
 						}
 					}
 					finish();
 					return;
 				} }); 
 			alrt.setButton(Dialog.BUTTON_NEGATIVE,getText(R.string.btn_no), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 					return;
 				}});
 			alrt.show();
 		}
 	}
 	
 	private static class ScheduledDownload {
 	    private String name = "" ;
 	    private String url = "" ;
 	    private String apkid = "" ;
 	    private String md5 = "" ;
 	    private String vername = "";
 	    private int vercode = 0 ;
 	    private boolean checked = false ;
 	    public ScheduledDownload( String name, boolean checked ) {
 	      this.name = name ;
 	      this.checked = checked ;
 	    }
 	    public String getName() {
 	      return name;
 	    }
 	    public boolean isChecked() {
 	      return checked;
 	    }
 	    public void setChecked(boolean checked) {
 	      this.checked = checked;
 	    }
 	    public String toString() {
 	      return name ; 
 	    }
 	    public void toggleChecked() {
 	      checked = !checked ;
 	    }
 		public String getUrl() {
 			return url;
 		}
 		public void setUrl(String url) {
 			this.url = url;
 		}
 		public int getVercode() {
 			return vercode;
 		}
 		public void setVercode(int vercode) {
 			this.vercode = vercode;
 		}
 		public String getApkid() {
 			return apkid;
 		}
 		public void setApkid(String apkid) {
 			this.apkid = apkid;
 		}
 		public String getMd5() {
 			return md5;
 		}
 		public void setMd5(String md5) {
 			this.md5 = md5;
 		}
 		public String getVername() {
 			return vername;
 		}
 		public void setVername(String vername) {
 			this.vername = vername;
 		}
 	  }
 	  
 	  /** Holds child views for one row. */
 	  private static class Holder {
 	    public CheckBox checkBoxScheduled ;
 	    public TextView textViewName ;
 	    public TextView textViewVersion;
 	    public ImageView imageViewIcon ;
 	    public Holder( TextView textView, TextView textViewVersion, CheckBox checkBox, ImageView imageView ) {
 	      this.checkBoxScheduled = checkBox ;
 	      this.textViewName = textView ;
 	      this.textViewVersion = textViewVersion;
 	      this.imageViewIcon = imageView ;
 	    }
 	  }
 	  
 		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 			SimpleCursorLoader a = new SimpleCursorLoader(this) {
 				
 				
 
 				@Override
 				public Cursor loadInBackground() {
 					
 					return db.getScheduledDownloads();
 				}
 			};
 			return a;
 		}
 		public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
 			planets.clear();
 			if(c.getCount()==0){
 				findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
 			}else{
 				findViewById(android.R.id.empty).setVisibility(View.GONE);
 			}
 			for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
 				ScheduledDownload scheduledDownload = new ScheduledDownload(c.getString(1), true);
 				scheduledDownload.setUrl(c.getString(5));
 				scheduledDownload.setApkid(c.getString(2));
 				scheduledDownload.setVercode(Integer.parseInt(c.getString(3)));
 				scheduledDownload.setMd5(c.getString(6));
 				scheduledDownload.setVername(c.getString(4));
 				planets.put(c.getString(0),scheduledDownload);
 			}
 			adapter.swapCursor(c);
 			
 			
 		}
 		public void onLoaderReset(Loader<Cursor> arg0) {
 			adapter.swapCursor(null);
 		}
 		private boolean isAllChecked(){
 			if(planets.isEmpty()){
 				return false;
 			}
 			for(String planet : planets.keySet()){
 				if (planets.get(planet).checked){
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		
 		@Override
 		public boolean onCreateOptionsMenu(Menu menu) {
 			
 			menu.add(Menu.NONE,1, 0, R.string.schDown_invertselection).setIcon(R.drawable.ic_menu_invert);
 			menu.add(Menu.NONE,2, 0, R.string.schDown_removeselected).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 			return super.onCreateOptionsMenu(menu);
 		}
 		
 		public boolean onMenuItemSelected(int featureId, MenuItem item) {
 			AlertDialog alrt = new AlertDialog.Builder(this).create();
 			switch (item.getItemId()) {
 			case 2:
 				if(isAllChecked()){
 					alrt.setMessage(getText(R.string.schDown_sureremove));
 					alrt.setButton(Dialog.BUTTON_POSITIVE,getText(R.string.btn_yes), new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							for(String planet : planets.keySet()){
 								if (planets.get(planet).checked){
 									db.deleteScheduledDownload(planet);
 								}
 							}
 							getSupportLoaderManager().restartLoader(0, null, ScheduledDownloads.this);
 							return;
 						} }); 
 					alrt.setButton(Dialog.BUTTON_NEGATIVE,getText(R.string.btn_no), new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							return;
 						}});
 					alrt.show();
 				} else {
 					Toast.makeText(this, R.string.schDown_nodownloadselect,
 							Toast.LENGTH_LONG).show();
 				}
 
 				break;
 
 			case 1:
 				for(String planet : planets.keySet()){
 					planets.get(planet).toggleChecked();
 				}
 				adapter.notifyDataSetInvalidated();
 				break;
 			default:
 				break;
 			}
 			
 			return super.onMenuItemSelected(featureId, item);
 		}
 		
 		@Override
 		protected void onDestroy() {
 			unbindService(serviceManagerConnection);
 			super.onDestroy();
 		}
 		
 }
