 package com.example.haw_app;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 import com.example.haw_app.veranstaltungsplan.implementations.Veranstaltungsplan;
 import com.example.haw_app.veranstaltungsplan.implementations.VeranstaltungsplanTask;
 
 public class VeranstaltungsplanActivity extends Activity {
	public static Veranstaltungsplan vp;
 	private ProgressDialog mDialog;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.vp_menu);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		try {
			vp = Veranstaltungsplan.getInstance();
		} catch (Exception e) {
			builder.setMessage(
					"Fehler beim laden des Veranstaltungsplans! Error: "
							+ e.toString()).setNeutralButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
 		}
 		AlertDialog alertDialog = builder.create();
 		alertDialog.show();
 	}
 
 	public void vpAktualisieren(Veranstaltungsplan vpnew) {
 		vp = vpnew;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setMessage("Veranstaltungsplan.txt wurde aktualisiert!")
 				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 					}
 				});
 		AlertDialog alertDialog = builder.create();
 		alertDialog.show();
 		mDialog.dismiss();
 	}
 
 	public static Veranstaltungsplan getVp() {
 		return vp;
 	}
 
 	public void anzeigenClick(View view) {
 		startActivity(new Intent(this, VeranstaltungsplanAnzeigenActivity.class));
 	}
 
 	public void exportierenClick(View view) {
 		int requestCode = 1;
 		startActivityForResult(new Intent(this,
 				VeranstaltungsplanExportierenActivity.class), requestCode);
 	}
 
 	public void aktualisierenClick(View view) {
 		mDialog = ProgressDialog.show(VeranstaltungsplanActivity.this,
 				"Downloade Veranstaltungsplan", "Loading...");
 		try {
 			new VeranstaltungsplanTask().execute(this);
 		} catch (Exception e) {
 			mDialog.dismiss();
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 			builder.setMessage(
 					"Veranstaltungsplan.txt konnte nicht aktualisiert werden! Error: "
 							+ e).setNeutralButton("OK",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 						}
 					});
 			AlertDialog alertDialog = builder.create();
 			alertDialog.show();
 		}
 		;
 	}
 }
