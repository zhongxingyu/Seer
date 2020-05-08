 package com.funkymonkeysoftware.adm;
 
 import java.net.MalformedURLException;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioGroup.LayoutParams;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.funkymonkeysoftware.adm.checker.CheckerLink;
 import com.funkymonkeysoftware.adm.checker.CheckerModel;
 import com.funkymonkeysoftware.adm.checker.DownloadRow;
 
 /**
  * User interface class for the link checker activity
  * 
  * @author James Ravenscroft
  *
  */
 public class LinkCheckerActivity extends Activity implements OnClickListener, Observer{
 	
 	
 	/**
 	 * A progress dialog shown for when the link checker is actually running
 	 */
 	private ProgressDialog pdialog;
 	
 	/**
 	 * Flag determines whether all links are selected or not
 	 */
 	private boolean selectAll = true;
 	
 	/**
 	 * The internal model representation of this view
 	 */
 	private CheckerModel model;
 	
 	private LinkedList<DownloadRow> rows;
 	
 	/**
 	 * The button used to toggle selection of all/none of the check urls
 	 */
 	private Button selectAllBtn;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//set up the link checker
 		setContentView(R.layout.linkchecker);
 		
 		//set up the model for the view
 		model = new CheckerModel(this);
 
 		//set up as listener for buttons
 		Button addLinksBtn = (Button)findViewById(R.id.addLinksBtn);
 		addLinksBtn.setOnClickListener(this);
 		
 		Button checkLinksBtn = (Button)findViewById(R.id.checkLinksBtn);
 		checkLinksBtn.setOnClickListener(this);
 		
 		Button removeOfflineBtn = (Button)findViewById(R.id.removeOfflineBtn);
 		removeOfflineBtn.setOnClickListener(this);
 		
 		selectAllBtn = (Button)findViewById(R.id.toggleSelectAllBtn);
 		selectAllBtn.setOnClickListener(this);
 		
 		Button removeSelectedBtn = (Button)findViewById(R.id.removeSelectedBtn);
 		removeSelectedBtn.setOnClickListener(this);
 		
 		Button downloadSelectedBtn = (Button)findViewById(R.id.downloadSelectedBtn);
 		downloadSelectedBtn.setOnClickListener(this);
 		
 		//initialise list of rows
 		rows = new LinkedList<DownloadRow>();
 		
 		//load the links
 		try {
 			model.loadLinks();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//add this object as a model listener
 		model.addObserver(this);
 		
 		updateDisplay();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 	
 	private void updateDisplay(){
 		//get the table to append things to
 		TableLayout table = (TableLayout)findViewById(R.id.checkLinksTable);
 	
 		table.setColumnStretchable(1, true);
 		
 		//set select all to none (since all links are initially selected)
 		selectAll = true;
 		selectAllBtn.setText(R.string.select_none);
 		
 		//empty the table view
 		table.removeAllViews();
 		rows.clear();
 		
 		if(model.getLinkCount() < 1) {
 			//provide some kind of error message
 			TextView error = new TextView(this);
 			TableRow r = new TableRow(this);
 			error.setText("No URLS, consider adding some!");
 			r.addView(error);
 			table.addView(r);
 		}else{
 			
 			for(CheckerLink l : model.getLinks()){
 				DownloadRow tr = new DownloadRow(this, l.getURL().toString(), l.getStatus());
 				
 				tr.setSelected(l.isSelected());
 				
 				//add the row to the table and the rows list
 				rows.add(tr);
 				table.addView(tr, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, 
 						LayoutParams.FILL_PARENT));
 			}
 		}
 	}
 	
 	
 	/**
 	 * Called when the user presses select all/none button
 	 */
 	private void toggleSelectAll(){
 		
 		//swap the flag
 		selectAll = !selectAll;
 		
 		//see what to display on the select button
 		int id = selectAll ? R.string.select_none : R.string.select_all;
 		
 		//set the new text
 		selectAllBtn.setText(id);
 		
 		//now iterate through all rows and select them
 		
 		for(DownloadRow r : rows){
 			r.setSelected(selectAll);
 		}
 	}
 	
 	/**
 	 * Force the window to redraw all links when its shown again
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		//call redraw process
 		try {
 			model.loadLinks();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		updateDisplay();
 	}
 	
 	/**
 	 * Callback method that is executed when one of the buttons is clicked.
 	 */
 	public void onClick(View v) {
 		
 		switch(v.getId()){
 		
 		case R.id.addLinksBtn:
 			//show the add links view		
 			startActivity(new Intent(this, LinkInputActivity.class));
 			break;
 			
 		case R.id.checkLinksBtn:
 			checkUrls();
 			break;
 			
 		//when the user selects only offline links
 		case R.id.removeOfflineBtn:
 			//select only offline links
 			model.selectOffline();
 			updateDisplay();
			//update the select all button
			if(selectAll){
				toggleSelectAll();
			}
 			break;
 			
 		case R.id.removeSelectedBtn:
 			model.removeSelected();
 			updateDisplay();
 			break;
 		
 		case R.id.toggleSelectAllBtn:
 			toggleSelectAll();
 			break;
 			
 		case R.id.downloadSelectedBtn:
 			model.downloadSelected();
 			updateDisplay();
 			break;
 		}
 	}
 
 	
 	/**
 	 * This function carries out initialisation of the link checker etc
 	 */
 	private void checkUrls(){
 		
 		pdialog = new ProgressDialog(this);
 		
 		pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		
 		pdialog.setMax(model.getLinkCount());
 		pdialog.setProgress(0);
 		
 		//set progress dialog text
 		pdialog.setMessage("Initialising Checker...");
 		
 		//show the progress bar
 		pdialog.show();	
 		
 		//run the model updater
 		model.checkLinks();
 	}
 
 	/**
 	 * Callback for the CheckerModel when a link has been checked
 	 * 
 	 */
 	public void update(Observable observable, Object data) {
 		
 			if(data instanceof Integer){
 				Log.v("linkchecker", "running update with progress: "+data);
 				
 				pdialog.setProgress((Integer)data);
 				pdialog.setMessage(String.format("Checking Links: %d of %d",
 						(Integer)data, 
 						pdialog.getMax()));
 				
 				if((Integer)data == 101){
 					pdialog.hide();
 					updateDisplay();
 				}
 			
 			}
 	}
 }
