 package edu.berkeley.cs160.smartnature;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.ScaleAnimation;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.ZoomControls;
 
 import java.util.ArrayList;
 
 public class EditScreen extends Activity implements View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener, ColorPickerDialog.OnColorChangedListener {
 	
 	Garden mockGarden;
 	EditView editView;
 	Plot plot, oldPlot;
 	Bundle extras;
 	ZoomControls zoomControls;
 	
 	boolean footerShown;
 	/** false if activity has been previously started */
 	boolean firstInit = true;
 	/** User-related options */
 	boolean createPoly;
 	boolean hintsOn, showLabels = true, showFullScreen, zoomAutoHidden;
 	/** describes what zoom button was pressed: 1 for +, -1 for -, and 0 by default */
 	int zoomPressed;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		firstInit = savedInstanceState == null;
 		showFullScreen = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
 		if (showFullScreen)
 			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
 		super.onCreate(savedInstanceState);
 		
 		extras = getIntent().getExtras();
 		mockGarden = GardenGnome.gardens.get(extras.getInt("garden_id"));
 		setTitle(extras.getString("name") + " (Edit mode)"); 
 		
 		if (extras.getInt("type") == Plot.POLY)
 			createPoly = true;
 		
 		if (firstInit && extras.containsKey("type"))
 			createPlot();
 		else
 			loadPlot();
 		
 		if (firstInit) { 
 			oldPlot = new Plot(plot);
 			mockGarden.addPlot(oldPlot);
 		} else
 			oldPlot = mockGarden.getPlot(mockGarden.size() - 1);
 		
 		plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
 		setContentView(R.layout.edit_plot);
 		editView = (EditView) findViewById(R.id.edit_view);
 		
 		if (createPoly)
 			((Button)findViewById(R.id.save_btn)).setText("Save shape");
 		
 		if (firstInit) {
 			editView.zoomScale = extras.getFloat("zoom_scale");
 			editView.dragMatrix.setValues(extras.getFloatArray("drag_matrix"));
 			editView.bgDragMatrix.setValues(extras.getFloatArray("bgdrag_matrix"));
 			editView.onAnimationEnd();
 		}
 		
 		hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
 		if (hintsOn) {
 			TextView hint = (TextView) findViewById(R.id.edit_hint);
 			hint.setText(R.string.hint_editscreen);
 			if (createPoly)
 				hint.setText(R.string.hint_editpoly); 
 			
 			hint.setVisibility(View.VISIBLE);
 		}
 		
 		findViewById(R.id.edit_footer).getBackground().setAlpha(getResources().getInteger(R.integer.bar_trans));
 		initButton(R.id.save_btn);
 		initButton(R.id.edit_zoomfit_btn);
 		
 		zoomControls = (ZoomControls) findViewById(R.id.edit_zoom_controls);
 		zoomControls.setOnZoomInClickListener(zoomIn);
 		zoomControls.setOnZoomOutClickListener(zoomOut);
 		zoomAutoHidden = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("zoom_autohide", false);
 		if (zoomAutoHidden)
 			zoomControls.setVisibility(View.GONE);
 		
 		editView.invalidate();
 	}
 
 	public void initButton(int id) {
 		View view = findViewById(id);
 		view.setOnClickListener(this);
 		view.setOnFocusChangeListener(this);
 		view.setOnTouchListener(this);
 		view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
 	}
 	
 	public void createPlot() {
 		RectF gBounds = mockGarden.getRawBounds();
 		int type = extras.getInt("type");
 		String name = extras.getString("name");
 		Rect bounds = new Rect((int)gBounds.left, (int)gBounds.top, (int)gBounds.right, (int)gBounds.bottom);
 		bounds.inset((int)gBounds.width()/3, (int)gBounds.height()/3);
 		if (type == Plot.POLY)
 			plot = new Plot(name, bounds, new float[] { 0, 0 });
 		else
 			plot = new Plot(name, bounds, type);
 			
 		mockGarden.addPlot(plot);
 	}
 	
 	public void loadPlot() {
 		if (extras.containsKey("type"))
 			plot = mockGarden.getPlot(mockGarden.size() - 2);
 		else
 			plot = mockGarden.getPlot(extras.getInt("plot_id"));
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putFloat("zoom_scale", editView.zoomScale);
 		savedInstanceState.putBoolean("portrait_mode", editView.portraitMode);
 		savedInstanceState.putBoolean("create_poly", createPoly);
 		if (createPoly)
 			savedInstanceState.putFloatArray("poly_points", toFloatArray(editView.polyPts));
 		float[] values = new float[9], bgvalues = new float[9];
 		editView.dragMatrix.getValues(values);
 		editView.bgDragMatrix.getValues(bgvalues);
 		savedInstanceState.putFloatArray("drag_matrix", values);
 		savedInstanceState.putFloatArray("bgdrag_matrix", bgvalues);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		editView.zoomScale = savedInstanceState.getFloat("zoom_scale");
 		boolean prevPortraitMode = savedInstanceState.getBoolean("portrait_mode");
 		boolean portraitMode = getWindowManager().getDefaultDisplay().getWidth() < getWindowManager().getDefaultDisplay().getHeight();
 		createPoly = savedInstanceState.getBoolean("create_poly");
 		if (createPoly) {
 			float[] pts = savedInstanceState.getFloatArray("poly_points");
 			for (float pt : pts)
 				editView.polyPts.add(pt);
 		}
 		float[] values = savedInstanceState.getFloatArray("drag_matrix");
 		float[] bgvalues = savedInstanceState.getFloatArray("bgdrag_matrix");
 		
 		if (portraitMode && !prevPortraitMode) {
 			// changed from landscape to portrait
 			float tmp = values[Matrix.MTRANS_X];
 			values[Matrix.MTRANS_X] = -values[Matrix.MTRANS_Y];
 			values[Matrix.MTRANS_Y] = tmp;
 			tmp = bgvalues[Matrix.MTRANS_X];
 			bgvalues[Matrix.MTRANS_X] = -bgvalues[Matrix.MTRANS_Y];
 			bgvalues[Matrix.MTRANS_Y] = tmp;
 		}
 		else if (!portraitMode && prevPortraitMode) {
 			// changed from portrait to landscape
 			float tmp = values[Matrix.MTRANS_X];
 			values[Matrix.MTRANS_X] = values[Matrix.MTRANS_Y];
 			values[Matrix.MTRANS_Y] = -tmp;
 			tmp = bgvalues[Matrix.MTRANS_X];
 			bgvalues[Matrix.MTRANS_X] = bgvalues[Matrix.MTRANS_Y];
 			bgvalues[Matrix.MTRANS_Y] = -tmp;
 		}
 		
 		editView.dragMatrix.setValues(values);
 		editView.bgDragMatrix.setValues(bgvalues);
 		editView.onAnimationEnd();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if (createPoly) {
 			float[] pts = toFloatArray(editView.polyPts);
 			plot.set(new Plot(plot.getName(), pts));
 			plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
 			oldPlot.set(plot);
 			((Button)findViewById(R.id.save_btn)).setText(R.string.btn_save_edit);
 			if (hintsOn) {
 				TextView hint = (TextView) findViewById(R.id.edit_hint);
 				hint.setText(R.string.hint_editscreen);
 			}
 			createPoly = false;
 			editView.invalidate();
 		}
 		else {
 			mockGarden.remove(oldPlot);
 			plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
 			Intent intent = new Intent();
 			intent.putExtra("zoom_scale", editView.zoomScale);
 			float[] values = new float[9], bgvalues = new float[9];
 			editView.dragMatrix.getValues(values);
 			editView.bgDragMatrix.getValues(bgvalues);
 			intent.putExtra("drag_matrix", values);
 			intent.putExtra("bgdrag_matrix", bgvalues);
 			setResult(RESULT_OK, intent);
 			finish();
 			overridePendingTransition(0, 0);
 		}
 	}
 	
 	/** in this method views actually have valid dimensions */
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		/*
 		if (firstInit && !footerShown) {
 			footerShown = true;
 			TranslateAnimation anim = new TranslateAnimation(0, 0, findViewById(R.id.edit_footer).getHeight(), 0);
 			anim.setDuration(getResources().getInteger(R.integer.footer_duration));
 			findViewById(R.id.edit_footer).startAnimation(anim);
 		}
 		*/
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.plot_edit_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.m_changecolor:
 			int color = getPreferences(MODE_PRIVATE).getInt("color", Color.WHITE);
 			new ColorPickerDialog(this, this, color).show();
 			break;
 		case R.id.m_revert:
 			plot.set(oldPlot);
 			plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
 			editView.invalidate();
 			break;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 			case R.id.save_btn:
 				onBackPressed();
 				break;
 			case R.id.edit_zoomfit_btn:
 				editView.zoomScale = 1;
 				mockGarden.refreshBounds(mockGarden.size() - (createPoly ? 2 : 1));
 				editView.reset();
 				break;
 		}
 	}
 	
 	View.OnClickListener zoomIn = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			handleZoom();
 			if (zoomPressed == 0) {
 				zoomPressed = 1;
 				float zoomScalar = getResources().getDimension(R.dimen.zoom_scalar);
 				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, editView.getWidth()/2f, editView.getHeight()/2f);
 				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
 				editView.startAnimation(anim);
 			}
 		}
 	};
 	
 	View.OnClickListener zoomOut = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			handleZoom();
 			if (zoomPressed == 0) {
 				zoomPressed = -1;
 				float zoomScalar = 1/getResources().getDimension(R.dimen.zoom_scalar);
 				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, editView.getWidth()/2f, editView.getHeight()/2f); 
 				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
 				editView.startAnimation(anim);
 			}
 		}
 	};
 	
 	public void handleZoom() {
 		zoomControls.removeCallbacks(autoHide);
 		if (!zoomControls.isShown())
 			zoomControls.show();
 		zoomControls.postDelayed(autoHide, getResources().getInteger(R.integer.hidezoom_delay));
 	}
 	
 	Runnable autoHide = new Runnable() {
 		@Override
 		public void run() {
 			if (zoomControls.isShown()) {
 				zoomControls.removeCallbacks(this);
 				zoomControls.hide();
 			}
 		}
 	};
 	
 	@Override
 	public void colorChanged(int color) {
 		getPreferences(MODE_PRIVATE).edit().putInt("color", color).commit();
 		plot.getPaint().setColor(color);
 		editView.invalidate();
 	}
 	
 	public static float[] toFloatArray(ArrayList<Float> list) {
 		float[] pts = new float[list.size()];
 		for (int i = 0; i < pts.length; i++)
 			pts[i] = list.get(i);
 		return pts;
 	}
 	
 	@Override
 	public void onFocusChange(View view, boolean hasFocus) {
 		if (hasFocus)
 			view.getBackground().setAlpha(0xff);
 		else
 			view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
 	}
 	
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN)
 			view.getBackground().setAlpha(0xff);
 		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			if (!view.isPressed()) {
 				view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
 				view.invalidate();
 			}
 		}
 		else {
 			view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
 			view.invalidate();
 		}
 		return false;
 	}
 	
 }
