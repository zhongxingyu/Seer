 package org.maupu.android.tmh;
 
 import org.maupu.android.tmh.database.object.BaseObject;
 import org.maupu.android.tmh.ui.CustomTitleBar;
 import org.maupu.android.tmh.ui.SimpleDialog;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 /**
  * Class representing an Activity to add or edit objects
  * @author nmaupu
  *
  */
 public abstract class AddOrEditActivity<T extends BaseObject> extends TmhActivity implements OnClickListener {
 	public static final String EXTRA_OBJECT_ID = "base_object";
 	private Button buttonContinue;
 	private Button buttonContinueAndAdd;
 	private Button buttonReset;
 	//private TextView textInfo;
 	private int contentView;
 	private T obj;
 	private int title;
 
 	private Integer icon;
 
 	public AddOrEditActivity(int title, Integer icon, int contentView, T obj) {
 		this.contentView = contentView;
 		this.obj = obj;
 		this.title = title;
 		this.icon = icon;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// Disable add button for addOrEdit activities
 		MenuItem mi = menu.findItem(R.id.item_add);
 		if(mi != null)
 			mi.setEnabled(false);
 		return true;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		CustomTitleBar ctb = new CustomTitleBar(this);
 		setContentView(contentView);
 		super.setTitle(getString(title));
 		ctb.setName(getString(title));
 		ctb.setIcon(icon);
 
 		buttonContinue = (Button)findViewById(R.id.button_continue);
 		buttonContinueAndAdd = (Button)findViewById(R.id.button_continue_and_add);
 		buttonReset = (Button)findViewById(R.id.button_reset);
 		buttonContinue.setOnClickListener(this);
 		buttonContinueAndAdd.setOnClickListener(this);
 		buttonReset.setOnClickListener(this);
 		/*textInfo = (TextView)findViewById(R.id.text_info);
 		if(textInfo != null) {
 			textInfo.setText("Please fill this form");
 			textInfo.setVisibility(View.VISIBLE);
 		}*/
 
 		// Retrieve extra parameter
 		retrieveItemFromExtra();
		// Init all widgets
		initResources();
 		// Fill form fields
 		baseObjectToFields(obj);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void retrieveItemFromExtra() {
 		try {
 			Intent intent = this.getIntent();
 			Bundle bundle = intent.getExtras();
 			T objnew = (T) bundle.get(AddOrEditActivity.EXTRA_OBJECT_ID);
 			if(objnew != null) {
 				buttonContinueAndAdd.setEnabled(false);
 				obj = objnew;
 			}
 		} catch (NullPointerException e) {
 			// Here, nothing is allocated, we keep default obj
 		} catch (ClassCastException cce) {
 			// This exception should not be thrown
 			throw cce;
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.button_continue:
 			onContinue(true);
 			break;
 		case R.id.button_continue_and_add:
 			if(onContinue(false)) {
 				Toast.makeText(this, getString(R.string.toast_success), Toast.LENGTH_SHORT).show();
 				obj.reset();
 				refreshDisplay();
 			}
 			break;
 		case R.id.button_reset:
 			baseObjectToFields(null);
 			break;
 		}
 	}
 
 	private boolean onContinue(final boolean disposeActivity) {
 		if(validate()) {
 			fieldsToBaseObject(obj);
 			
 			if(obj.getId() != null) {
 				// Show a confirm dialog when updating
 				final TmhActivity current = this; // WAT the heck I have just done ?
 				SimpleDialog.confirmDialog(this, getString(R.string.confirm_modification), new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						obj.update();
 						
 						// Dispose this activity
 						if(disposeActivity)
 							current.finish();
 					}
 				}).show();
 			} else {
 				obj.insert();
 				
 				if(disposeActivity)
 					super.finish();
 			}
 			
 			return true;
 		} else {
 			SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_add_object)).show();
 			return false;
 		}
 	}
 	
 	/**
 	 * Method called just after ui creation to finish initialization
 	 */
 	protected abstract void initResources();
 	/**
 	 * Method called to validate all field before updating or adding a baseObject
 	 * @return
 	 */
 	protected abstract boolean validate();
 	/**
 	 * Method to fill all fields from a given object
 	 * If parameter is null, all fields must be reset
 	 * @param obj
 	 */
 	protected abstract void baseObjectToFields(T obj);
 	/**
 	 * Method to fill a baseObject from fields's content
 	 * @param obj
 	 */
 	protected abstract void fieldsToBaseObject(T obj);
 
 	public void refreshDisplay() {
 		// Restore from begining
 		baseObjectToFields(obj);
 	}
 
 	// Menu item is disabled, so do nothing here
 	protected Intent onAddClicked() {
 		return null;
 	}
	
	protected T getObj() {
		return obj;
	}
 }
