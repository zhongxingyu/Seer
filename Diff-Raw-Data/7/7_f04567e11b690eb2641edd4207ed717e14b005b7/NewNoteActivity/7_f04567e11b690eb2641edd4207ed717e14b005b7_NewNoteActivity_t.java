 package mtu.notes;
 
 import java.io.File;
 
 
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SlidingDrawer;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Build;
 
 @SuppressWarnings("deprecation")
 public class NewNoteActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_note);
 		Spinner spinner = (Spinner) findViewById(R.id.journalspinner);
 
 		ArrayList<String> list = new ArrayList<String>();
 		File file = new File(Environment.getExternalStorageDirectory() + "/category.txt");
 		if(file.exists())
 		{
 			String read;
 			try {
 				LineNumberReader in = new LineNumberReader(new FileReader(Environment.getExternalStorageDirectory() +"/category.txt"));
 				while((read = in.readLine()) != null)
 				{
 					System.out.println(read);
 					list.add(read);
 				}
 				in.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		list.add("None");
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Apply the adapter to the spinner
 		spinner.setAdapter(adapter);
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		//Initially bring the notetext to the front, then put the drawer in front of that
 		EditText notetext = (EditText) findViewById(R.id.notetext);
 		notetext.bringToFront();
 		SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
 		drawer.bringToFront();
 
 		//Put the drawing option at the back
 		Button yellowH = (Button) findViewById(R.id.yellowHigh);
 		yellowH.setVisibility(View.GONE);
 		Button blueH = (Button) findViewById(R.id.blueHigh);
 		blueH.setVisibility(View.GONE);
 		Button greenH = (Button) findViewById(R.id.greenHigh);
 		greenH.setVisibility(View.GONE);
 		Button pinkH = (Button) findViewById(R.id.pinkHigh);
 		pinkH.setVisibility(View.GONE);
 		Button blackP = (Button) findViewById(R.id.blackPen);
 		blackP.setVisibility(View.GONE);
 		Button blueP = (Button) findViewById(R.id.bluePen);
 		blueP.setVisibility(View.GONE);
 		Button redP = (Button) findViewById(R.id.redPen);
 		redP.setVisibility(View.GONE);
 		Button greenP = (Button) findViewById(R.id.greenPen);
 		greenP.setVisibility(View.GONE);
 		Button eraser = (Button) findViewById(R.id.eraserButton);
 		eraser.setVisibility(View.GONE);
 		Button clear = (Button) findViewById(R.id.clearButton);
 		clear.setVisibility(View.GONE);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.new_note, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void save(View view)
 	{
 		EditText text = (EditText) findViewById(R.id.notetext);
 		ScribbleView scribble = (ScribbleView) findViewById(R.id.scribbles);
 		Spinner cat = (Spinner) findViewById(R.id.journalspinner);
 		if(text != null)
 		{
 			EditText editText = (EditText)findViewById(R.id.titletext);
 
 			if(!editText.getText().toString().isEmpty())
 			{
 				String filename = editText.getText().toString() + ".txt";
 				String folder;
 				if(cat.getSelectedItem().toString().equals("None"))
 				{
 					folder = Environment.getExternalStorageDirectory().getPath() + "/None/" + editText.getText().toString();
 				}
 				else
 				{
 					folder = Environment.getExternalStorageDirectory().getPath() + "/" + cat.getSelectedItem().toString() + "/" + editText.getText().toString();
 				}
 				File dir = new File(folder);
 				boolean success = dir.mkdir();
 				int count = 0;
 				while(!success)
 				{
 					if(cat.getSelectedItem().toString().equals("None"))
 					{
 						folder = Environment.getExternalStorageDirectory().getPath() + "/None/" + editText.getText().toString() + count;
 					}
 					else
 					{
 						folder = Environment.getExternalStorageDirectory().getPath() + "/" + cat.getSelectedItem().toString() + "/" + editText.getText().toString() + count;
 					}
 					count++;
 					dir = new File(folder);
 					success = dir.mkdir();
 				}
 				File file = new File(folder, filename);
 				System.out.println(file.getPath());
 				try {
 					FileOutputStream out = new FileOutputStream(file);
 					out.write(text.getText().toString().getBytes());
 					out.close();
 					Context context = getApplicationContext();
 					CharSequence toastText = filename + " saved.";
 					int duration = Toast.LENGTH_SHORT;
 
 					Toast toast = Toast.makeText(context, toastText, duration);
 					toast.show();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				scribble.Save(editText, folder);
 			}
 			else
 			{
 				String filename = "notes.txt";
 				String folder;
 				if(cat.getSelectedItem().toString().equals("None"))
 				{
 					folder = Environment.getExternalStorageDirectory().getPath() + "/None/notes";
 				}
 				else
 				{
 					folder = Environment.getExternalStorageDirectory().getPath() + "/" + cat.getSelectedItem().toString() + "/notes";
 				}
 				File dir = new File(folder);
 				boolean success = dir.mkdir();
 				int count = 0;
 				while(!success)
 				{
 					if(cat.getSelectedItem().toString().equals("None"))
 					{
 						folder = Environment.getExternalStorageDirectory().getPath() + "/None/notes" + count;
 					}
 					else
 					{
 						folder = Environment.getExternalStorageDirectory().getPath() + "/" + cat.getSelectedItem().toString() + "/notes" + count;
 					}
 					count++;
 					dir = new File(folder);
 					success = dir.mkdir();
 				}
 				File file = new File(folder, filename);
 				try {
 					FileOutputStream out = new FileOutputStream(file);
 					out.write(text.getText().toString().getBytes());
 					out.close();
 					Context context = getApplicationContext();
 					CharSequence toastText = filename + " saved.";
 					int duration = Toast.LENGTH_SHORT;
 
 					Toast toast = Toast.makeText(context, toastText, duration);
 					toast.show();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				scribble.Save(editText, folder);
 			}
 		}
 	}
 
 	/**
 	 * Switches the view of the editText to the scribbleView and vice versa. Also switches
 	 * the buttons associated to each view.
 	 * @param view
 	 */
 	public void drawToggle(View view){
 		//The sliding drawer
 		SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
 		//The toggle button
 		ToggleButton toggle = (ToggleButton) findViewById(R.id.drawToggle);
 
 		//All the views for the text options
 		Button left = (Button) findViewById(R.id.leftA);
 		Button center = (Button) findViewById(R.id.center);
 		Button right = (Button) findViewById(R.id.rightA);
 		ToggleButton bold = (ToggleButton) findViewById(R.id.bold);
 		ToggleButton italicize = (ToggleButton) findViewById(R.id.italic);
 		ToggleButton underline = (ToggleButton) findViewById(R.id.underline);
 		Spinner size = (Spinner) findViewById(R.id.sizeSpinner);
 		Spinner color = (Spinner) findViewById(R.id.colorSpinner);
 
 		//All the views for the drawing options
 		Button yellowH = (Button) findViewById(R.id.yellowHigh);
 		Button blueH = (Button) findViewById(R.id.blueHigh);
 		Button greenH = (Button) findViewById(R.id.greenHigh);
 		Button pinkH = (Button) findViewById(R.id.pinkHigh);
 		Button blackP = (Button) findViewById(R.id.blackPen);
 		Button blueP = (Button) findViewById(R.id.bluePen);
 		Button redP = (Button) findViewById(R.id.redPen);
 		Button greenP = (Button) findViewById(R.id.greenPen);
 		Button eraser = (Button) findViewById(R.id.eraserButton);
 		Button clear = (Button) findViewById(R.id.clearButton);
 
 		//Bring scribbleView to the front
 		if(toggle.isChecked()){
 			ScribbleView scribbles = (ScribbleView) findViewById(R.id.scribbles);
 			scribbles.bringToFront();
 			drawer.bringToFront();
 
 			//Get rid of the text buttons
 			left.setVisibility(View.GONE);
 			center.setVisibility(View.GONE);
 			right.setVisibility(View.GONE);
 			bold.setVisibility(View.GONE);
 			italicize.setVisibility(View.GONE);
 			underline.setVisibility(View.GONE);
 			size.setVisibility(View.GONE);
 			color.setVisibility(View.GONE);
 
 			//Bring back the drawing buttons
 			yellowH.setVisibility(View.VISIBLE);
 			blueH.setVisibility(View.VISIBLE);
 			greenH.setVisibility(View.VISIBLE);
 			pinkH.setVisibility(View.VISIBLE);
 			blackP.setVisibility(View.VISIBLE);
 			blueP.setVisibility(View.VISIBLE);
 			redP.setVisibility(View.VISIBLE);
 			greenP.setVisibility(View.VISIBLE);
 			eraser.setVisibility(View.VISIBLE);
 			clear.setVisibility(View.VISIBLE);
 		}
 		//Bring noteView to the front
 		else{
 			EditText notetext = (EditText) findViewById(R.id.notetext);
 			notetext.bringToFront();
 			drawer.bringToFront();
 
 			//Get rid of all the drawing buttons
 			yellowH.setVisibility(View.GONE);
 			blueH.setVisibility(View.GONE);
 			greenH.setVisibility(View.GONE);
 			pinkH.setVisibility(View.GONE);
 			blackP.setVisibility(View.GONE);
 			blueP.setVisibility(View.GONE);
 			redP.setVisibility(View.GONE);
 			greenP.setVisibility(View.GONE);
 			eraser.setVisibility(View.GONE);
 			clear.setVisibility(View.GONE);
 
 			//Bring back all the text buttons
 			left.setVisibility(View.VISIBLE);
 			center.setVisibility(View.VISIBLE);
 			right.setVisibility(View.VISIBLE);
 			bold.setVisibility(View.VISIBLE);
 			italicize.setVisibility(View.VISIBLE);
 			underline.setVisibility(View.VISIBLE);
 			size.setVisibility(View.VISIBLE);
 			color.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	/**
 	 * Following 4 methods sets the highlighter color
 	 * @param view
 	 */
 	public void highlighterYellow(View view){
 		ScribbleView.highlightColor(Color.YELLOW);
 	}
 	public void highlighterBlue(View view){
 		ScribbleView.highlightColor(Color.BLUE);
 	}
 	public void highlighterGreen(View view){
 		ScribbleView.highlightColor(Color.GREEN);
 	}
 	public void highlighterPink(View view){
 		ScribbleView.highlightColor(Color.MAGENTA);
 	}
 
 	/**
 	 * The following 4 methods sets the pen color
 	 * @param view
 	 */
 	public void penBlack(View view){
 		ScribbleView.penColor(Color.BLACK);
 	}
 	public void penRed(View view){
 		ScribbleView.penColor(Color.RED);
 	}
 	public void penBlue(View view){
 		ScribbleView.penColor(Color.BLUE);
 	}
 	public void penGreen(View view){
 		ScribbleView.penColor(Color.GREEN);
 	}
 
 	/**
 	 * Erases the pen and/or highlighter on scribbleview
 	 * @param view
 	 */
 	public void eraser(View view){
 		ScribbleView.erase();
 	}
 
 	/**
 	 * Clears the scribbleView completely
 	 * @param view
 	 */
 	public void clear(View view){
 		ScribbleView scribble = (ScribbleView) findViewById(R.id.scribbles);
 		scribble.mBitmap = Bitmap.createBitmap(scribble.width, scribble.height, Bitmap.Config.ARGB_8888);
 		scribble.hBitmap = Bitmap.createBitmap(scribble.width, scribble.height, Bitmap.Config.ARGB_8888);
 		scribble.mCanvas = new Canvas(scribble.mBitmap);
 		scribble.hCanvas = new Canvas(scribble.hBitmap);
 		scribble.invalidate();
 	}
 
 	/**
 	 * The following 3 methods changes the alignment of the text in the editText
 	 * @param view
 	 */
 	public void alignLeft(View view){
 		EditText text = (EditText) findViewById(R.id.notetext);
 		text.setGravity(Gravity.LEFT);
 	}
 	public void alignCenter(View view){
 		EditText text = (EditText) findViewById(R.id.notetext);
 		text.setGravity(Gravity.CENTER_HORIZONTAL);
 	}
 	public void alignRight(View view){
 		EditText text = (EditText) findViewById(R.id.notetext);
 		text.setGravity(Gravity.RIGHT);
 	}
 
 
 	public void bold(View view){
 		EditText text = (EditText) findViewById(R.id.notetext);
 		ToggleButton bold = (ToggleButton) findViewById(R.id.bold);
 		ToggleButton italic = (ToggleButton) findViewById(R.id.italic);
 		
 		if(bold.isChecked()){
 			if(italic.isChecked()){
 				text.setTypeface(text.getTypeface(), Typeface.BOLD_ITALIC);
 			}
 			text.setTypeface(text.getTypeface(), Typeface.BOLD);
 		}
 		else{
 			if(italic.isChecked()){
 				text.setTypeface(text.getTypeface(), Typeface.ITALIC);
 			}
 			text.setTypeface(text.getTypeface(), Typeface.NORMAL);
 		}
 	}
 
 }
