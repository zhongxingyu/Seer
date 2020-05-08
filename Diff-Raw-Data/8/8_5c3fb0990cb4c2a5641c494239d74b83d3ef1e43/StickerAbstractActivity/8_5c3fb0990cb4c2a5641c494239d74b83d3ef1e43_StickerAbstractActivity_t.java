 package at.almeida.mypanini.activity;
 
 import android.app.Activity;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.TextView;
 import at.almeida.mypanini.R;
 import at.almeida.mypanini.adapters.AbstractStickerAdapter;
 import at.almeida.mypanini.adapters.StickerAlbumDbAdapter;
 import at.almeida.mypanini.listeners.MissingStickerLongItemClickListener;
 
 public abstract class StickerAbstractActivity extends Activity {
 
 	protected Long albumId;
 	protected AbstractStickerAdapter stickerAdapter;
 	private boolean skipLoad;
 
 	public static String FONT_MIA = "fonts/MiasScribblings.ttf";
 
 	protected void alterTextViewFont(TextView textview, String fontLocation) {
 		Typeface tf = Typeface.createFromAsset(getAssets(), fontLocation);
 		textview.setTypeface(tf);
 	}
 
 	public Typeface findFont(String fontPath) {
 		return Typeface.createFromAsset(getAssets(), fontPath);
 	}
 
 	protected Long retrieveAlbumId(Bundle savedInstanceState) {
 		Long id = (savedInstanceState == null) ? null
 				: (Long) savedInstanceState
 						.getSerializable(StickerAlbumDbAdapter.KEY_ID);
 		if (albumId == null) {
 			Bundle extras = getIntent().getExtras();
 			id = extras != null ? extras.getLong(StickerAlbumDbAdapter.KEY_ID)
 					: null;
 		}
 		return id;
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		albumId = retrieveAlbumId(savedInstanceState);
 
 		setContentView(R.layout.sticker_gridview);
 		GridView gridview = (GridView) findViewById(R.id.stickergridview);
 
 		stickerAdapter = createStickerAdapter();
 		skipLoad = true;
 
 		stickerAdapter.changeFont(findFont(FONT_MIA));
 		gridview.setAdapter(stickerAdapter);
 
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 
 				long stickerToChangeId = parent.getAdapter()
 						.getItemId(position);
 				stickerAdapter.updateCurrentSelection(v, position,
 						stickerToChangeId);
 			}
 
 		});
 
 		// adds the functionality on long item press
 		gridview.setOnItemLongClickListener(new MissingStickerLongItemClickListener(
 				this, stickerAdapter));
 
 	}
 	
 	@Override
 	protected void onResume() {
		if(stickerAdapter.getAlbumId() != null && !skipLoad){
 			stickerAdapter.populateModel(stickerAdapter.getAlbumId());
 			stickerAdapter.notifyDataSetChanged();
			
 		}
		skipLoad = false;
 		super.onResume();
 	}
 
 	/**
 	 * Instantiates a new sticker adapter to be used in this Activity
 	 * 
 	 * @return
 	 */
 	public abstract AbstractStickerAdapter createStickerAdapter();
 }
