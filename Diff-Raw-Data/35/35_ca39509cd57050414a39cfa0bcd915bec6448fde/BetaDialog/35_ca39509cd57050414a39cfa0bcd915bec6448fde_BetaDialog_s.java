 package com.brousalis;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 
 /**
  * BetaDialog is a custom extension of the Dialog class that
  * will allow us to show any popups related to the android
  * beta program.
  * @author Eric Stokes 10/28/2010
  *
  */
 public class BetaDialog extends Dialog {
 	
 	private Button _cancelButton;
 	private Button _submitButton;
 	
 	public BetaDialog(Context context, int xmlToLoad) {
 		super(context);
 		
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.setContentView(xmlToLoad);
 		this.setCancelable(true);
 		// We'll always have a cancel button
		_cancelButton = (Button) this.findViewById(xmlToLoad);
 		
		if((Button) this.findViewById(xmlToLoad) != null) {
			_submitButton = (Button) this.findViewById(xmlToLoad);
 		}
 		
 	}
 	
 	/**
 	 * Set the click listener of the Cancel Button
 	 * @param clickListener This will become the click listener of the cancel button
 	 * @return true for success, false for failure
 	 */
 	public boolean setCancelAction(View.OnClickListener clickListener) {
 		if(_cancelButton != null) {
 			_cancelButton.setOnClickListener(clickListener);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Set the click listener of the Submit Button
 	 * @param clickListener This will become the click listener of the submit button
 	 * @return true for success, false for failure
 	 */
 	public boolean setSubmitAction(View.OnClickListener clickListener) {
 		if(_submitButton != null) {
 			_submitButton.setOnClickListener(clickListener);
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		//super.onBackPressed();
 		this.cancel();
 	}
 	
 	@Override
 	public boolean onSearchRequested() {
 		// TODO Auto-generated method stub
 		return false;
 		//return super.onSearchRequested();
 	}
 }
