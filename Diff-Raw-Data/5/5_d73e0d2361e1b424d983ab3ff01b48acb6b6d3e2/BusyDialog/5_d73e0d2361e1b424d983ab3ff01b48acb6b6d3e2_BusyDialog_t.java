 package org.redbus.ui;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface.OnCancelListener;
 
 public class BusyDialog {
 	
 	private ProgressDialog pd;
 	private Context ctx;
 	
 	public BusyDialog(Context ctx) {
 		this.ctx = ctx;
 	}
 	
	public void show(OnCancelListener onCancel, String reason) {
 		dismiss();
		pd = ProgressDialog.show(ctx, "", reason, true, true, onCancel);
 	}
 
 	public void dismiss() {
 		if (pd != null) {
 			try {
 				pd.dismiss();
 			} catch (Throwable t) {
 			}
 		}
 		pd = null;
 	}
 }
