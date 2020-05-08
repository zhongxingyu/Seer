 package com.androidmontreal.tododetector.ui.list;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.androidmontreal.tododetector.R;
 import com.androidmontreal.tododetector.json.DataExtractor;
 import com.androidmontreal.tododetector.json.datatype.Elements;
 import com.androidmontreal.tododetector.json.datatype.TodoElement;
 import com.androidmontreal.tododetector.network.NetworkChatter;
 import com.androidmontreal.tododetector.network.interfaces.INetworkResponse;
 import com.androidmontreal.tododetector.network.utilities.DataDownload;
 import com.androidmontreal.tododetector.ui.toaster;
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 public class ViewOneList extends SherlockActivity implements INetworkResponse {
 
 	/*******************************************************
 	 *                                                     *
 	 *                   Class Variables                   *
 	 *                                                     *
 	 *******************************************************/
 	// Inter-thread Message Handler
 	private final Handler mMessageChannel = new Handler();
 	private long listId = -1;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.onelist);
 
 		getSupportActionBar().setSubtitle(getString(R.string.actOneListSubtitle));
 		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME );
 
 		long mIntentData = (getIntent().getExtras()!= null
 				?getIntent().getExtras().getLong("listId")
 						:null);
 
 
 		/*ImageAdapter lImAd = new ImageAdapter(getListFromServer(mIntentData));
 		lImAd.initialize();
 
 		ListView lListDisplay = new ListView(this);
 		lListDisplay.setAdapter(lImAd);
 
 		getContainer().addView(lListDisplay);*/
 		getListFromServer(mIntentData);
 		
 		listId = mIntentData;
 		getBtnServer().setEnabled(true);
 
 	}
 
 	private void getListFromServer(long mIntentData) {
 		requestDataFromServer(mIntentData);
 		//return getFakeList();	
 	}
 
 	private Elements getFakeList() {
 		Elements fakeList = new Elements();
 		fakeList.setListElements(new ArrayList<TodoElement>());
 		fakeList.getListElements().add(new TodoElement(false, "http://fc07.deviantart.net/fs6/i/2005/058/a/7/moo__by_smashmethod.jpg"));
 		fakeList.getListElements().add(new TodoElement(true, "http://www.nutshell.ca/images/moo.jpg"));
 		return fakeList;
 	}
 
 	// Exit from Menubar
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		this.finish();
 		return true;
 	}
 
 	/** IMPORTED CODE **/
 
 	/*******************************************************
 	 *                                                     *
 	 *                   Data Requesting                   *
 	 *                                                     *
 	 *******************************************************/
 	// JSON Data decryption happens in a network-approach-coded library (from google)
 	// basically, we need to process that data in another thread and keep any laggy
 	// stuff from the UI thread.
 	private void requestDataFromServer(long pListID) {
 		toaster.printMessage(this, "attempting datasync");
 		String lBaseURL = getValueFromPreference(getString(R.string.strServerBaseURL));
 		if(lBaseURL.equalsIgnoreCase("")){
 			toaster.printMessage(this, "You have not configured the base URL correctly. RTFM or GTFO");
 			//return;
 		}
 		NetworkChatter.getOneList(lBaseURL, pListID, this);
 	}
 	private void processResponseInThread(HttpResponse response){
 
 		final HttpResponse argument = response;
 
 		new Thread(new Runnable() {
 			public void run() {
 				JsonObject lNetResponse = null;
 				lNetResponse = DataExtractor.getJsonObjectFromEntity(argument.getEntity());
 				Elements returnData = 
 						(new Gson()).fromJson(lNetResponse, Elements.class);
 				mMessageChannel.post(new ServerDataRunnable(returnData));
 			}
 		}).start();
 	}
 	private class ServerDataRunnable implements Runnable {
 		ServerDataRunnable(Elements pObject) {
 			communicatedObject = pObject;
 		}
 		protected Elements communicatedObject = null;
 		public void run() {
 			processServerData(communicatedObject);
 		}
 	}
 	protected void processServerData(Elements communicatedObject) {
 		
 		toaster.printMessage(this, "did it! "+communicatedObject.getListElements().get(0).getImageurl());
 
 		getSupportActionBar().setSubtitle(getString(R.string.actOneListSubtitle)+ communicatedObject.getName());
 		
 		ImageAdapter serverImageAdapter = new ImageAdapter(communicatedObject);
 		getListContainer().removeAllViews();
 
 		toaster.printMessage(this, "adapter _should_ be okay");
 		
 		ListView lListDisplay = new ListView(this);
 		lListDisplay.setAdapter(serverImageAdapter);
 		
 		getListContainer().addView( lListDisplay );
 	}
 
 	/*******************************************************
 	 *                                                     *
 	 *                   Utility Methods                   *
 	 *                                                     *
 	 *******************************************************/
 	private ViewOneList getActivity() {return this;}
 	private String getValueFromPreference(String pKey) {
 		return PreferenceManager.getDefaultSharedPreferences(this).getString(pKey, "");
 	}
 	public void onNetworkResponseReceived(HttpResponse response) {
 		processResponseInThread(response);
 	}
 
 	/*******************************************************
 	 *                                                     *
 	 *                    UI Accessors                     *
 	 *                                                     *
 	 *******************************************************/
 	protected Button getBtnAction() {
 		return (Button) getButtonContainer().findViewById(R.id.btnAction);
 	}
 	protected Button getBtnServer() {
 		return (Button) getButtonContainer().findViewById(R.id.btnServer);
 	}
 	private OnClickListener mServerClickListener = new OnClickListener() {
 		
 		public void onClick(View v) {
 			requestDataFromServer(listId);
 		}
 	};
 
 	protected LinearLayout getListContainer() {
 		return (LinearLayout) this.findViewById(R.id.lytListContainer);
 	}
 	protected LinearLayout getButtonContainer() {
 		return (LinearLayout) this.findViewById(R.id.lytButtonContainer);
 	}
 
 	/*******************************************************
 	 *                                                     *
 	 *                   Internal Class                    *
 	 *                                                     *
 	 *******************************************************/
 	public class ImageAdapter extends BaseAdapter {
 		private LayoutInflater mInflater;
 		public List<TodoElement> mElementsList = null;
 
 		@SuppressWarnings("unused")
 		private ImageAdapter() {
 			// locked CTOR
 		}
 		public ImageAdapter(Elements elements) {
 			// locked CTOR
 			mElementsList = elements.getListElements();
 			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 
 		public void initialize() {
 			notifyDataSetChanged();
 		}
 
 		public int getCount() {
 			return mElementsList.size();
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			final ViewHolder holder;
 			if (convertView == null) {
 				holder = new ViewHolder();
 				convertView = mInflater.inflate(R.layout.galleryitem, null);
 				holder.imageview = (ImageView) convertView
 						.findViewById(R.id.elementImage);
 				holder.checkbox = (CheckBox) convertView
 						.findViewById(R.id.elementChecked);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 			holder.checkbox.setId(position);
 			holder.imageview.setId(position);
 			holder.checkbox.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					// Normally, the current status of the checkbox is equal to the actual
 					// element's checked status. So we invert it, pure and simple
 					CheckBox cb = (CheckBox) v;
 					int id = cb.getId();
 					if (mElementsList.get(id).isChecked()) {
 						cb.setChecked(false);
 						mElementsList.get(id).setChecked(false);
 					} else {
 						cb.setChecked(true);
 						mElementsList.get(id).setChecked(true);
 					}
 				}
 			});
 			holder.imageview.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					holder.checkbox.callOnClick();
 				}
 			});
 			TodoElement item = mElementsList.get(position);
 			loadImageFromURL(item.getImageurl(), holder.imageview);
 			holder.checkbox.setChecked(item.isChecked());
 			return convertView;
 		}
 		void loadImageFromURL(final String pURL, final ImageView targetImVw) {
			toaster.printMessage(getActivity(), "Attempting to load an image");
 			new Thread(new Runnable() {
 				public void run() {
 					try {
 						ByteArrayOutputStream mBytes = DataDownload.DownloadFileToByte( pURL );
 
 						mMessageChannel.post(new ImageRunnable(
 								targetImVw,
 								new BitmapDrawable(
 										targetImVw.getResources(),
 										BitmapFactory.decodeByteArray(mBytes.toByteArray(), 0, mBytes.toByteArray().length)
 										)));
 						mBytes.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					Log.d(getActivity().getLocalClassName(), "in theory, loaded " + pURL);
 				}
 			}).start();
 		}
 
 	};
 	class ViewHolder {
 		ImageView imageview;
 		CheckBox checkbox;
 	};
 
 	class ImageRunnable implements Runnable {
 		ImageRunnable (ImageView pIV, BitmapDrawable pBD) {
 			rIV = pIV;
 			rBD = pBD;
 		}
 		public ImageView rIV;
 		public BitmapDrawable rBD;
 		public void run() {
 			loadImageToView(rIV, rBD);
 		}
 	};
 
 	private void loadImageToView(final ImageView pTargetImageView, final BitmapDrawable pImage) {
 		pTargetImageView.setImageDrawable(pImage);
 	}
 }
