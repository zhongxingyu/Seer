 package net.fhtagn.zoobeditor.browser;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import net.fhtagn.zoobeditor.Common;
 import net.fhtagn.zoobeditor.EditorConstants;
 import net.fhtagn.zoobeditor.R;
 import net.fhtagn.zoobeditor.Series;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.ContentUris;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 public class OfflineSerieViewActivity extends Activity {
 	static final String TAG = "OfflineSerieViewActivity";
 	static final int DIALOG_CONFIRM_DELETE = 0;
 	static final int DIALOG_RATE = 1;
 	private long serieID;
 	
 	private JSONObject serieObj = null;
 	private JSONArray levelsArray = null;
 	
 	@Override
 	protected void onCreate (Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent i = getIntent();
 		if (i == null) {
 			Log.e(TAG, "onCreate : null intent");
 			finish();
 		}
 		serieID = i.getLongExtra("serieid", -1);
 		if (serieID == -1) {
 			Log.e(TAG, "onCreate : serieID = -1");
 			finish();
 		}
 		
 		
 		Cursor cur = managedQuery(ContentUris.withAppendedId(Series.CONTENT_URI, serieID), new String[]{Series.JSON, Series.RATING, Series.MY_RATING, Series.NAME, Series.PROGRESS}, null, null, null);
 		if (!cur.moveToFirst()) {
 			Log.e(TAG, "onCreate: !cur.moveToFirst");
 			finish();
 		}
 		
 		setContentView(R.layout.serieview);
 		
 		try {
 			serieObj = new JSONObject(cur.getString(cur.getColumnIndex(Series.JSON)));
 			levelsArray = serieObj.getJSONArray("levels");
 			
 			TextView serieName = (TextView)findViewById(R.id.name);
 			serieName.setText(cur.getString(cur.getColumnIndex(Series.NAME)));
 			/*GridView gridView = (GridView)findViewById(android.R.id.list);
 			gridView.setAdapter(new LevelsAdapter(this, serieObj.getJSONArray("levels")));*/
 			SeriePreviewGrid previewGrid = (SeriePreviewGrid)findViewById(R.id.seriepreview);
 	    previewGrid.setSerie(serieObj);
 	    
 	    
 	    //BEGIN rating
 	    RatingBar communityRating = (RatingBar)findViewById(R.id.rating);
 	    RatingBar myRating = (RatingBar)findViewById(R.id.my_rating);
 	    myRating.setOnTouchListener(new OnTouchListener() {
 				@Override
         public boolean onTouch(View view, MotionEvent event) {
 					if (event.getAction() == MotionEvent.ACTION_UP) {
 						showDialog(DIALOG_RATE);
 					}
 	        return true;
         }
 	    });
 	    
 	    if (cur.isNull(cur.getColumnIndex(Series.RATING)))
 	    	communityRating.setVisibility(View.INVISIBLE);
 	    else
 	    	communityRating.setRating(cur.getFloat(cur.getColumnIndex(Series.RATING)));
 	    
	    if (cur.isNull(cur.getColumnIndex(Series.MY_RATING)))
	    	myRating.setRating(0);
	    else
	    	myRating.setRating(cur.getFloat(cur.getColumnIndex(Series.MY_RATING)));
 	    //END rating
 	    
 			
 			Button playBtn = (Button)findViewById(R.id.btn_play);
 			playBtn.setText(R.string.btn_play_serie);
 			playBtn.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					if (serieObj == null) {
 						Log.e(TAG, "serieObj = null");
 						return;
 					}
 					Intent i = Common.playSerie(serieID);
 					startActivity(i);
 				}
 			});
 		} catch (JSONException e) {
 			e.printStackTrace();
 			finish();
 		}
 	}
 	
 	@Override
 	protected Dialog onCreateDialog (int id) {
 		switch (id) {
 			case DIALOG_CONFIRM_DELETE: {
 				return Common.createConfirmDeleteDialog(this, R.string.confirm_delete_serie, new DialogInterface.OnClickListener() {
 	  			public void onClick (DialogInterface dialog, int id) {
 	  				Uri deleteUri = ContentUris.withAppendedId(Series.CONTENT_URI, serieID);
 	  				getContentResolver().delete(deleteUri, null, null);
 	  				OfflineSerieViewActivity.this.finish();
 	  			}
 				});
 			}
 			case DIALOG_RATE: {
 				return Common.createRateDialog(this, serieID);
 			}
 			default:
 				return null;
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu (Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.offlineserieview_menu, menu);
 		Common.createCommonOptionsMenu(this, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected (MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.delete:
 				showDialog(DIALOG_CONFIRM_DELETE);
 				return true;
 		}
 		return Common.commonOnOptionsItemSelected(this, item);
 	}
 }
