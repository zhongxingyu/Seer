 package com.example.createrequestlist;
 
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.TextView;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 
 public class MainMenu extends Activity implements OnClickListener{
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         //ActivityのOnCreate実行
     	super.onCreate(savedInstanceState);
     	
     	//カスタムタイトルバーを使用
     	this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         this.setContentView(R.layout.main);		//レイアウト
         final Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/JohnHancockCP.otf");
         this.setHeader(tf);			//ヘッダー
         this.setFooter(tf);			//フッター
         
         //データベース事前設定
         this.setDataBase();
      
         /*
         //Button オブジェクト取得
         Button[] buttons = {
         	(Button)this.findViewById(R.id.order),
         	(Button)this.findViewById(R.id.editList),
         	(Button)this.findViewById(R.id.orderHistory),
         	(Button)this.findViewById(R.id.mail_setup)
         };
         */
         
         //Button オブジェクトにクリックリスナーを設定
         for(Button button:getButtons()){
         	button.setOnClickListener(this);
         	buttonStatus(button);
         }
     }
 
 	//Buttonオブジェクト取得
 	private Button[] getButtons(){
         Button[] buttons = {
         	(Button)this.findViewById(R.id.order),
         	(Button)this.findViewById(R.id.editList),
         	(Button)this.findViewById(R.id.orderHistory),
         	(Button)this.findViewById(R.id.mail_setup)
         };
         return buttons;
 	}
 	
 	//Buttonオブジェクトステータス変更
 	private void buttonStatus(Button button){
 		if(button.getId() != R.id.mail_setup){
 			if(!isAccount()){
 				button.setEnabled(false);
 			}else{
 				button.setEnabled(true);
 			}
 		}
 	}
 	
     @Override
 	protected void onRestart() {
     	//Activity再開時にも再チェック
         for(Button button:getButtons()){
         	buttonStatus(button);
         }
 		super.onRestart();
 	}
 
 	@Override
     public void onClick(View view){
     	//Intent変数初期化
     	Intent nextActivity = null;
     	
     	//押下別にインテント生成
     	switch(view.getId()){
     	case R.id.order:
     		nextActivity = new Intent(this,Category.class);
     		break;
     	case R.id.editList:
     		nextActivity = new Intent(this,EditList.class);
     		break;
     	case R.id.orderHistory:
     		if(!fileCheck()){
     			showMsg("おねがい履歴が存在しません。");
     			return ;	//強制終了
     		}
     		nextActivity = new Intent(this,OrderedHistory.class);
     		break;
     	case R.id.mail_setup:
     		nextActivity = new Intent(this,MailSetUp.class);
     		break;
     	}
     	//アクティビティ起動
     	startActivity(nextActivity);
     }
     
     private void setDataBase(){
     	//DatabaseHelperクラスのインスタンス生成
     	ProductDBHelper mDbHelper = new ProductDBHelper(this);
     	
     	//空のデータベースを作成する
     	mDbHelper.createEmptyDataBase();
     }
     
     //ファイル存在チェック
     private boolean fileCheck(){
     	String[] pFiles = this.fileList();
     	if(pFiles.length == 0 || pFiles == null){
     		return false;
     	}
     	return true;
     }
     
 	//ダイアログ表示
 	private void showMsg(String msg){
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 		dialog.setIcon(android.R.drawable.ic_menu_info_details);
 		dialog.setTitle("メッセージ");
 		dialog.setMessage(msg);
 		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 			}
 		});
 		dialog.show();
 	}
     
 	//ヘッダーのカスタマイズ
 	private void setHeader(Typeface tf){
 		//タイトル用のレイアウト設定
 		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.main_titlebar);
 		
 		//TextView オブジェクト取得
 		TextView title = (TextView)this.findViewById(R.id.title);
 		title.setTypeface(tf);
 	}
 	
 	//フッターのカスタマイズ
 	private void setFooter(Typeface tf){
 		//TextView オブジェクト取得
 		TextView footer = (TextView)this.findViewById(R.id.footer);
 		footer.setTypeface(tf);
 	}
 	
 	
 	private boolean isAccount(){
 		boolean isAccount = false;
 		//プリファレンス取得
 		SharedPreferences pref = this.getSharedPreferences(MailSetUp.FILE_NAME, MODE_PRIVATE);
 		String user = pref.getString("MAIL", "");
 		String pass = pref.getString("PASS", "");
 		
 		if(!("".equals(user.trim())) && !("".equals(pass.trim()))){
 			isAccount = true;
 		}
 		return isAccount;
 	}
 	
 	
 	//Backキー無効
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event){
 		//デバイスボタンが押下された場合
 		if(event.getAction() == KeyEvent.ACTION_DOWN){
 			//Backキーが押下された場合
 			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
 				this.moveTaskToBack(true);
 			}
 		}
 		return super.dispatchKeyEvent(event);
 	}
 }
