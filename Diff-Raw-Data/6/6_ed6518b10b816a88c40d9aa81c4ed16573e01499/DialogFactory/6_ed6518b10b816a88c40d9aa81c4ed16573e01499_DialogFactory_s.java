 /**
  * 
  */
 package com.markupartist.iglaset.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface.OnClickListener;
 
 /**
  * @author marco
  *
  */
 public class DialogFactory {
 	
 	public final static int DIALOG_SEARCH_NETWORK_PROBLEM = 0;
 	
 	public static Dialog createNetworkProblemDialog(
 			Activity activity,
 			OnClickListener onClickListener) {
 		Dialog dialog = createNetworkProblemDialog((Context) activity, onClickListener);
 		dialog.setOwnerActivity(activity);
 		return dialog;
 	}
 	
 	public static Dialog createNetworkProblemDialog(
 			Context ctx,
 			OnClickListener onClickListener) {
         return new AlertDialog.Builder(ctx)
         .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Ett fel intrffade")
        .setMessage("Kunde inte ansluta till servern. Frsk igen, eller Cancel fr att g tillbaka till fregende vy.")
        .setPositiveButton("Frsk igen", onClickListener)
         .setNegativeButton(ctx.getText(android.R.string.cancel), onClickListener)
         .create();
 	}
 }
