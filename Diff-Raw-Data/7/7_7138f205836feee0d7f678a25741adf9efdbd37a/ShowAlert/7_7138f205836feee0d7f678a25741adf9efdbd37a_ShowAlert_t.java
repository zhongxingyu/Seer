 package com.qeevee.gq.rules.act;
 
 import com.qeevee.ui.BitmapUtil;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.R;
 
 public class ShowAlert extends Action {
 
 	@Override
 	protected boolean checkInitialization() {
 		return params.containsKey("message");
 	}
 
 	@Override
 	public void execute() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(
				(Context) GeoQuestApp.getCurrentActivity()).setTitle(
 				(params.get("message"))).setPositiveButton(R.string.ok,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				});
		if (params.containsKey("image")) {
			Bitmap bitmap = BitmapUtil.loadBitmap(params.get("image"), false);
 			builder.setIcon(new BitmapDrawable(bitmap));
 		}
 		builder.show();
 	}
 
 }
