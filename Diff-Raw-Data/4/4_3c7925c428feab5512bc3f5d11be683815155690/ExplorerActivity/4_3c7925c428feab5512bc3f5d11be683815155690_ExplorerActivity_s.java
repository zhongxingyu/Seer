 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Refract.
  *
  * Refract is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Refract is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Refract. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.refract.activity;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.Date;
 
 import com.ijuru.refract.Bookmark;
 import com.ijuru.refract.Constants;
 import com.ijuru.refract.R;
 import com.ijuru.refract.RefractApplication;
 import com.ijuru.refract.renderer.Complex;
 import com.ijuru.refract.renderer.Mapping;
 import com.ijuru.refract.renderer.Palette;
 import com.ijuru.refract.renderer.RendererParams;
 import com.ijuru.refract.renderer.Renderer;
 import com.ijuru.refract.renderer.RendererListener;
 import com.ijuru.refract.ui.RendererView;
 import com.ijuru.refract.ui.StatusPanel;
 import com.ijuru.refract.utils.Preferences;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.Bitmap.CompressFormat;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * Activity for exploring fractals
  */
 public class ExplorerActivity extends Activity implements RendererListener {
 	
 	private RendererView rendererView;
 	private StatusPanel statusPanel;
 	
 	/**
 	 * @see com.ijuru.refract.activity.ExplorerActivity#onCreate(Bundle)
 	 */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_explorer);
 		
 		rendererView = (RendererView)findViewById(R.id.rendererView);
 		statusPanel = (StatusPanel)findViewById(R.id.statusPanel);
 		
 		rendererView.setRendererListener(this);
     }
 
 	/**
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.explorer, menu);
 	    return true;
 	}
 	
 	/**
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menureset:
 			rendererView.reset();
 	    	break;
 		case R.id.menuwallpaper:
 			onMenuWallpaper();
 	    	break;
 		case R.id.menubookmarks:
 			onMenuBookmarks();
 	    	break;	
 		case R.id.menusave:
 			onMenuSave();
 	    	break;
 		case R.id.menusettings:
 			startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
 	    	break;	
 		case R.id.menugoto:
 			onMenuGoto();
 	    	break;
 	    case R.id.menuabout:
 	    	onMenuAbout();
 	    	break;
 		}
 		return true;
 	}
 	
 	/**
 	 * Displays the wallpaper activity
 	 */
 	private void onMenuWallpaper() {
 		Intent intent = new Intent(getApplicationContext(), WallpaperActivity.class);
 		intent.putExtra(Constants.EXTRA_PARAMS, rendererView.getRendererParams());
 		startActivity(intent);
 	}
 	
 	/**
 	 * Displays the bookmarks activity
 	 */
 	private void onMenuBookmarks() {
 		Intent intent = new Intent(getApplicationContext(), BookmarksActivity.class);
 		intent.putExtra(Constants.EXTRA_PARAMS, rendererView.getRendererParams());
 		intent.putExtra("thumbnail", captureBookmarkThumbnail());
 		startActivity(intent);
 	}
 	
 	/**
 	 * Saves the current render to the gallery
 	 */
 	private void onMenuSave() {
 		File saveDir = ((RefractApplication)getApplication()).getSaveDirectory();
 		File saveFile = new File(saveDir, new Date().getTime() + ".png");
 		Bitmap bitmap = rendererView.getBitmap();
 		
 		try {
 			OutputStream stream = new FileOutputStream(saveFile);
 			bitmap.compress(CompressFormat.PNG, 80, stream);
 			stream.close();
 			
 			// Tell the media scanner about the new file
 			Uri contentUri = Uri.fromFile(saveFile);
 		    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 		    mediaScanIntent.setData(contentUri);
 		    sendBroadcast(mediaScanIntent);
 			
 			Toast.makeText(this, R.string.str_imagesaved, Toast.LENGTH_SHORT).show();
 		} catch (Exception e) {
 			Toast.makeText(this, R.string.err_unabletosave, Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	/**
 	 * Displays the goto dialog
 	 */
 	private void onMenuGoto() {
 		// Get and inflate the dialog view
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		final View layout = inflater.inflate(R.layout.dialog_goto, (ViewGroup)findViewById(R.id.layout_root));
 		final EditText editReal = (EditText)layout.findViewById(R.id.realcoordinate);
 		final EditText editImag = (EditText)layout.findViewById(R.id.imaginarycoordinate);
 		final EditText editZoom = (EditText)layout.findViewById(R.id.zoomfactor);
 		
 		// Set controls to curent renderer parameters
 		RendererParams params = rendererView.getRendererParams();
 		editReal.setText("" + params.getOffset().re);
 		editImag.setText("" + params.getOffset().im);
 		editZoom.setText("" + params.getZoom());
 
 		// Construct the dialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				try {
 					double offset_re = Double.parseDouble(editReal.getText().toString());
 					double offset_im = Double.parseDouble(editImag.getText().toString());
 					double zoom = Double.parseDouble(editZoom.getText().toString());
 					rendererView.getRendererParams().setOffset(new Complex(offset_re, offset_im));
 					rendererView.getRendererParams().setZoom(zoom);
 				}
 				catch (NumberFormatException ex) {
 					Toast.makeText(ExplorerActivity.this, R.string.err_invalidnumber, Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		builder.setNegativeButton(android.R.string.cancel, null);
 		
 		// Show it...
 		builder.show();
 	}
 	
 	/**
 	 * Displays the about dialog
 	 */
 	private void onMenuAbout() {
 		RefractApplication app = (RefractApplication)getApplication();
 		
 		String title = getString(R.string.app_name) + " " + app.getVersionName();
 		String message = 
 				"Thank you for downloading " + getString(R.string.app_name) + "\n" +
 				"\n" +
 				"Enjoy exploring the world of Mandelbrot!";
 		
 		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
 			.setPositiveButton(android.R.string.ok, null).show();
 	}
 
 	/**
 	 * Captures a thumbnail image for a bookmark
 	 * @return the thumbnail image
 	 */
 	private Bitmap captureBookmarkThumbnail() {
 		Bitmap srcBitmap = rendererView.getBitmap();	
 		Bitmap dstBitmap = Bitmap.createBitmap(Bookmark.THUMBNAIL_WIDTH, Bookmark.THUMBNAIL_HEIGHT, Bitmap.Config.ARGB_8888);
 		
 		// Calculated center cropped square from renderer image
 		double srcAspectRatio = srcBitmap.getWidth() / (double)srcBitmap.getHeight();	
 		int srcSize = (srcAspectRatio >= 1.0) ? srcBitmap.getHeight() : srcBitmap.getWidth();
 		int srcCenterX = srcBitmap.getWidth() / 2;
 		int srcCenterY = srcBitmap.getHeight() / 2;
 		Rect srcRect = new Rect(srcCenterX - srcSize / 2, srcCenterY - srcSize / 2, srcCenterX + srcSize / 2, srcCenterY + srcSize / 2);
 		
 		// Scale and draw into thumbnail bitmap
 		Canvas dstCanvas = new Canvas(dstBitmap);
 		Rect dstRect = new Rect(0, 0, Bookmark.THUMBNAIL_WIDTH, Bookmark.THUMBNAIL_HEIGHT);
 		dstCanvas.drawBitmap(srcBitmap, srcRect, dstRect, null);
 		
 		return dstBitmap;
 	}
 	
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onRendererCreated(RendererView, Renderer)
 	 */
 	@Override
 	public void onRendererCreated(RendererView view, Renderer renderer) {
 		// Load renderer options from preferences
 		Palette palette = Palette.getPresetByName(Preferences.getStringPreference(this, Constants.PREF_PALETTE_PRESET, R.string.def_palettepreset));
 		Mapping paletteMapping = Preferences.getMappingPreference(this, Constants.PREF_PALETTE_MAPPING, Mapping.REPEAT);
 		int paletteSize = Preferences.getIntegerPreference(this, Constants.PREF_PALETTE_SIZE, R.integer.def_palettesize);
 		int setColor = Preferences.getIntegerPreference(this, Constants.PREF_PALETTE_SETCOLOR, R.integer.def_palettesetcolor);
 		
 		renderer.setPalette(palette, paletteSize, setColor);
 		view.setPaletteMapping(paletteMapping);
 		
 		// Load renderer parameters from preferences
 		RendererParams params = Preferences.getParametersPreference(this, Constants.PREF_PARAMS);
 		view.setRendererParams(params);
 		
 		// Load renderer view options from preferences
 		int itersPerFrame = Preferences.getIntegerPreference(this, Constants.PREF_ITERS_PERFRAME, R.integer.def_itersperframe);
 		view.setIterationsPerFrame(itersPerFrame);
 	}
 
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onOffsetChanged(RendererView, Renderer, Complex)
 	 */
 	@Override
 	public void onRendererOffsetChanged(RendererView view, Renderer renderer, Complex offset) {
 		statusPanel.setCoords(offset.re, offset.im);
 	}
 
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onZoomChanged(RendererView, Renderer, double)
 	 */
 	@Override
 	public void onRendererZoomChanged(RendererView view, Renderer renderer, double zoom) {
 		statusPanel.setZoom(zoom);
 	}
 
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onUpdate(RendererView, Renderer, int)
 	 */
 	@Override
 	public void onRendererIterated(RendererView view, Renderer renderer, int iters) {
 		long avgFrameTime = rendererView.getRendererThread().calcSmoothedFrameTime();
 		
 		statusPanel.setPerformanceInfo(iters, avgFrameTime > 0 ? 1000.0 / avgFrameTime : 0);
 	}
 	
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onRendererDestroy(RendererView, Renderer)
 	 */
 	@Override
 	public void onRendererDestroy(RendererView view, Renderer renderer) {
 		// Save renderer parameters to preferences
 		Preferences.setParametersPreference(this, Constants.PREF_PARAMS, rendererView.getRendererParams());
 	}
 	
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onRendererAllocationFailed(RendererView, Renderer)
 	 */
 	@Override
 	public void onRendererAllocationFailed(RendererView view, Renderer renderer) {
 		Toast.makeText(this, "Unable to allocate renderer resources", Toast.LENGTH_LONG).show();
 	}
 }
