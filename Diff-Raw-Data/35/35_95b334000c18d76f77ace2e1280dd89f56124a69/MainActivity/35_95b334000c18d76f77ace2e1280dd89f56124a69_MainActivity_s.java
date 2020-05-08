 package org.catalog.android.client;
 
import org.catalog.model.Bottle;

 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.method.PasswordTransformationMethod;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private boolean isAuth = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_layout);
 
 		final Button btnAddBottle = (Button) findViewById(R.id.btnAddBottle);
 		btnAddBottle.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (isAuth) {
 					Intent intent = new Intent(MainActivity.this,
 							AddBottleActivity.class);
 					startActivity(intent);
 				} else {
 					authDialog(MainActivity.this);
 				}
 			}
 		});
 
 		final Button btnShowBottle = (Button) findViewById(R.id.btnShowBottle);
 		btnShowBottle.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				final AlertDialog.Builder bottleIdDialogBuilder = new AlertDialog.Builder(
 						MainActivity.this);
 				final EditText input = new EditText(MainActivity.this);
 
 				bottleIdDialogBuilder
 						.setTitle(getString(R.string.enter_bottle_id));
 				bottleIdDialogBuilder
 						.setMessage(getString(R.string.please_enter_bottle_id));
 				bottleIdDialogBuilder.setView(input);
 				bottleIdDialogBuilder.setPositiveButton(getString(R.string.ok),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								try {
 									Intent intent = new Intent(
 											MainActivity.this,
 											ShowBottleActivity.class);
 									intent.putExtra(
 											getString(R.string.bottleid),
 											Integer.parseInt(input.getText()
 													.toString()));
 									startActivity(intent);
 								} catch (NumberFormatException ex) {
 
 								}
 							}
 						});
 
 				bottleIdDialogBuilder.setNegativeButton(
 						getString(R.string.cancel),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								dialog.cancel();
 							}
 						});
 				bottleIdDialogBuilder.show();
 			}
 		});
 
 		final Button btnShowAllBottles = (Button) findViewById(R.id.btnShowAllBottles);
 		btnShowAllBottles.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this,
 						ShowAllBottlesActivity.class);
 				startActivity(intent);
 			}
 		});
 	}
 
 	private void authDialog(final Context context) {
 		final EditText inputUser = new EditText(context);
 		final EditText inputPass = new EditText(context);
 		inputPass.setTransformationMethod(new PasswordTransformationMethod());
 		LinearLayout linearLayout = new LinearLayout(context);
 		linearLayout.setOrientation(1);
 		linearLayout.addView(inputUser);
 		linearLayout.addView(inputPass);
 		final AlertDialog.Builder authDialogBuilder = new AlertDialog.Builder(
 				context);
 		authDialogBuilder.setTitle(getString(R.string.enter_bottle_id));
 		authDialogBuilder
 				.setMessage(getString(R.string.please_enter_bottle_id));
 		authDialogBuilder.setView(linearLayout);
		authDialogBuilder.setPositiveButton(getString(R.string.ok),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						if (inputUser.getText().toString().equals("admin")
 								&& inputPass.getText().toString()
 										.equals("pass")) {
 							isAuth = true;
 						} else {
 							Toast.makeText(context,
 									"Wrong username or password!",
 									Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 
 		authDialogBuilder.setNegativeButton(getString(R.string.cancel),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						dialog.cancel();
 					}
				});
 		authDialogBuilder.show();
 	}
 }
