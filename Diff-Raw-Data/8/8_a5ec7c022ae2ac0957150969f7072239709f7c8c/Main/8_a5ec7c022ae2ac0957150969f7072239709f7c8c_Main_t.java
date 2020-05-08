 package info.mabin.android.barcodememo;
 
 import info.mabin.android.barcodememo.R;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.util.Base64;
 import android.view.View;
 import android.widget.*;
 
 public class Main extends Activity{
 	int dataIdx;
 	int undoPnt;
 	private EditText txtCode;
 	private Intent scanIntent;
 	private DBHelper oDBHelper;
 	private String backupString;
 	private Boolean useContinuousScan = false; 
 	
 	int VERSION;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_barcode_memo);
 
 		txtCode = (EditText) findViewById(R.id.txtCode);
 		scanIntent = new Intent("com.google.zxing.client.android.SCAN");
 		scanIntent.putExtra("RESULT_DISPLAY_DURATION_MS", 0L);
 		
 		PackageManager pm = this.getPackageManager();
 		PackageInfo packageInfo = null;
 		try {
 			packageInfo = pm.getPackageInfo(this.getPackageName(), 0);
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		int VERSION = packageInfo.versionCode;
 		
 		List<String> changeLog = new ArrayList<String>();
 		changeLog.add(getString(R.string.change_log_1));
 		changeLog.add(getString(R.string.change_log_2));
 		changeLog.add(getString(R.string.change_log_3));
 		changeLog.add(getString(R.string.change_log_4));
 		changeLog.add(getString(R.string.change_log_5));
		changeLog.add(getString(R.string.change_log_6));
 		
 		oDBHelper = new DBHelper(this, null, VERSION, 
 				getString(R.string.welcome_message), 
 				getString(R.string.change_log_header), 
 				changeLog);
 		
 		txtCode.setText(oDBHelper.getLatestData());
 		txtCode.setSelection(txtCode.length());
 		backupString = txtCode.getText().toString();
 		
 		dataIdx = oDBHelper.getLatestID();
 		undoPnt = 0;
 		
 		findViewById(R.id.btnScan).setOnClickListener(onClickScan);
 		findViewById(R.id.btnShare).setOnClickListener(onClickShare);
 		findViewById(R.id.btnClear).setOnClickListener(onClickClear);
 		findViewById(R.id.btnUndo).setOnClickListener(onClickUndo);
 		findViewById(R.id.btnRedo).setOnClickListener(onClickRedo);
 		findViewById(R.id.btnClear).setOnLongClickListener(onLongClickClear);
 	}
 
 	@Override
 	public void onPause() {
 		addString(txtCode.getText().toString());
 
 		super.onPause();
 	}
 	
 	private void addString(String text){
     	if(!backupString.equals(txtCode.getText().toString())){
 			if(undoPnt != 0){
 	    		for(;undoPnt < 0; undoPnt++){
 	    			oDBHelper.delData();
 	    			dataIdx--;
 	    		}
 			}
 	
 			oDBHelper.putData(text);
 	    	backupString = text;
 	    	dataIdx++;
     	}
 	}
 	
 	private final Button.OnClickListener onClickScan = new Button.OnClickListener() {
 	    public void onClick(View v) {
 	    	showQuestion(R.string.continuous_scan_title, R.string.continuous_scan_message, 
 	    			onClickOkContinuousScan, onClickCancelContinuousScan);
 	    }
 	};
 
 	private final Button.OnClickListener onClickShare = new Button.OnClickListener() {
 	    public void onClick(View v) {
 			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
 			sharingIntent.setType("text/plain");
 			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
			
			String tmpContent = txtCode.getText().toString();
			tmpContent = tmpContent.replace("\n", "\r\n");
			
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, tmpContent);
 			
 			startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_dialog_title)));
 	    }
 	};
 
 	private final Button.OnClickListener onClickClear = new Button.OnClickListener() {
 	    public void onClick(View v) {
     		txtCode.setText("");
     		txtCode.setSelection(txtCode.length());
     		addString("");
 	    }
 	};
 	
 	private final Button.OnLongClickListener onLongClickClear = new Button.OnLongClickListener() {
 		public boolean onLongClick(View v) {
 	    	showQuestion(R.string.clearall_title, R.string.clearall_message, onClickOkClearAll);
 			
 			return true;
 		}
 	};
 	
 	private final DialogInterface.OnClickListener onClickOkClearAll = new DialogInterface.OnClickListener(){
 		public void onClick(DialogInterface dialog, int which) {
 			oDBHelper.clearAll();
 			dataIdx = 0;
 			txtCode.setText(oDBHelper.getLatestData());
     		txtCode.setSelection(txtCode.length());
 		}
 	};
 	
 	private final DialogInterface.OnClickListener onClickOkContinuousScan = new DialogInterface.OnClickListener(){
 		public void onClick(DialogInterface dialog, int which) {
 			useContinuousScan = true;
 			barcodeScan();
 		}
 	};
 	
 	private void barcodeScan(){
 		try{
     		startActivityForResult(scanIntent, 0);
     	} catch (Exception e){
     		showDialog(R.string.error, R.string.unknown_error);
     	}
 	}
 	
 	private final DialogInterface.OnClickListener onClickCancelContinuousScan = new DialogInterface.OnClickListener(){
 		public void onClick(DialogInterface dialog, int which) {
 			barcodeScan();
 		}
 	};
 	
 	private final Button.OnClickListener onClickUndo = new Button.OnClickListener() {
 	    public void onClick(View v) {
     		addString(txtCode.getText().toString());
 	    	
 	    	if((undoPnt * -1) != dataIdx){
     			undoPnt--;
 	    	}
 
 	    	txtCode.setText(oDBHelper.getData(dataIdx + undoPnt));
     		txtCode.setSelection(txtCode.length());
 	    	backupString = txtCode.getText().toString();
 	    }
 	};
 	
 	private final Button.OnClickListener onClickRedo = new Button.OnClickListener() {
 	    public void onClick(View v) {
 	    	if(undoPnt < 0)
 	    		undoPnt++;
 	    	
 	    	txtCode.setText(oDBHelper.getData(dataIdx + undoPnt));
     		txtCode.setSelection(txtCode.length());
 	    	backupString = txtCode.getText().toString();
 		}
 	};
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent intent){
 		if (requestCode == 0){
 			if (resultCode == RESULT_OK){
 				String contents = intent.getStringExtra("SCAN_RESULT");
 				if(contents.equals("TWFiaW4ncyBFYXN0ZXIgRWdnCg==")){
 					txtCode.append(new String(Base64.decode("VGhhbmsgeW91IGZvciBmaW5kaW5nIHRoaXMuIDpECg==", 0)));
 				} else {
 					txtCode.append(contents + "\n");
 					addString(txtCode.getText().toString());
 					txtCode.setSelection(txtCode.length());
 				}
 				if(useContinuousScan)
 					barcodeScan();
 			} else {
 				useContinuousScan = false;
 			}
 		}
 	}
 
 	private void showDialog(int title, int message){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(title);
 		builder.setMessage(message);
 		builder.setPositiveButton(getString(R.string.ok), null);
 		builder.show();
 	}
 	
 	private void showQuestion(int title, int message, DialogInterface.OnClickListener onClickListener){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(title);
 		builder.setMessage(message);
 		builder.setPositiveButton(getString(R.string.ok), onClickListener);
 		builder.setNegativeButton(getString(R.string.cancel), null);
 		builder.show();
 	}
 	
 	private void showQuestion(int title, int message, 
 			DialogInterface.OnClickListener onClickOkListener,
 			DialogInterface.OnClickListener onClickCancelListener){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(title);
 		builder.setMessage(message);
 		builder.setPositiveButton(getString(R.string.ok), onClickOkListener);
 		builder.setNegativeButton(getString(R.string.cancel), onClickCancelListener);
 		builder.show();
 	}
 }
