 
 package com.tomovwgti.socket;
 
 import net.arnx.jsonic.JSON;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 import com.tomovwgti.aircon.json.Msg;
 
 import de.roderick.weberknecht.WebSocketEventHandler;
 import de.roderick.weberknecht.WebSocketMessage;
 
 public class AirConWebSocketActivity extends Activity {
     static final String TAG = AirConWebSocketActivity.class.getSimpleName();
 
     private static String WS_URI = "ws://192.168.110.110:8001/";
 
     private Handler handler = new Handler();
     private Activity activity;
     private AlertDialog mAlertDialog;
     private SharedPreferences pref;
     private SharedPreferences.Editor editor;
 
     private SeekBar mControlBar;
     private TextView mControl;
     private TextView mTempText;
 
     private static final String UU_STR = "uu";
     private static final String NYAA_STR = "nyaa";
 
    private static final String UU_TEXT = "(」・ω・)」うー！";
    private static final String NYAA_TEXT = "(／・ω・)／にゃー！";

     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mTempText = (TextView) findViewById(R.id.temprature);
         mControl = (TextView) findViewById(R.id.control);
         mControlBar = (SeekBar) findViewById(R.id.control_bar);
         mControlBar.setProgress(0);
         mControlBar.setMax(11);
         mControlBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 mControl.setText(String.valueOf(progress + 19));
 
                 if (fromUser) {
                     Msg msg = new Msg();
                     msg.setCommand("");
                     msg.setSender("mobile");
                     msg.setCommand("AirCon");
                     msg.setSetting(progress + 19);
                     String message = JSON.encode(msg);
                     WebSocketManager.send(message);
                     setSetting(progress + 19, Color.BLUE);
                 }
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         });
 
         pref = PreferenceManager.getDefaultSharedPreferences(this);
         editor = pref.edit();
 
         // IPアドレス確認ダイアログ
         mAlertDialog = showAlertDialog();
         mAlertDialog.show();
 
         // うーボタン押下時の挙動
         Button uuBtn = (Button) findViewById(R.id.btn_uu_btn);
         uuBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Msg msg = new Msg();
                 msg.setCommand("");
                 msg.setSender("android");
                 msg.setCommand("Message");
                 msg.setMessage(UU_STR);
                 String message = JSON.encode(msg);
                 WebSocketManager.send(message);
             }
         });
 
         // にゃーボタン押下時の挙動
         Button nyaaBtn = (Button) findViewById(R.id.btn_nyaa_btn);
         nyaaBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Msg msg = new Msg();
                 msg.setCommand("");
                 msg.setSender("android");
                 msg.setCommand("Message");
                 msg.setMessage(NYAA_STR);
                 String message = JSON.encode(msg);
                 WebSocketManager.send(message);
             }
         });
     }
 
     private void connectWebSocket() {
         Log.i(TAG, "connect start");
         // WebSocket通信開始
         WebSocketManager.connect(WS_URI, new WebSocketEventHandler() {
 
             @Override
             public void onOpen() {
                 Log.d(TAG, "websocket connect open");
             }
 
             @Override
             public void onMessage(WebSocketMessage message) {
                 Log.d(TAG, "websocket message");
                 String str = message.getText();
                 // 設定表示の更新
                 Msg msg = JSON.decode(str, Msg.class);
                 if (msg.getSetting() != 100) {
                     setSetting(msg.getSetting(), Color.GREEN);
                 }
                 setTemperature(msg.getTemperature());
             }
 
             @Override
             public void onClose() {
                 Log.d(TAG, "websocket connect close");
             }
         });
     }
 
     private void setSetting(final int setting, final int color) {
         // WebSocketHandlerのonMessageは別スレッドなのでhandlerを用いてviewの書き換えを行う
         handler.post(new Runnable() {
             @Override
             public void run() {
                 mControl.setText(String.valueOf(setting));
                 mControl.setTextColor(color);
                 mControlBar.setProgress(setting - 19);
             }
         });
     }
 
     private void setTemperature(final int temperature) {
         // WebSocketHandlerのonMessageは別スレッドなのでhandlerを用いてviewの書き換えを行う
         handler.post(new Runnable() {
             @Override
             public void run() {
                 mTempText.setText(String.valueOf(temperature));
             }
         });
     }
 
     private AlertDialog showAlertDialog() {
         LayoutInflater factory = LayoutInflater.from(this);
         final View entryView = factory.inflate(R.layout.dialog_entry, null);
         final EditText edit = (EditText) entryView.findViewById(R.id.username_edit);
 
         if (pref.getString("IPADDRESS", "").equals("")) {
             edit.setHint("***.***.***.***");
         } else {
             edit.setText(pref.getString("IPADDRESS", ""));
         }
         // キーハンドリング
         edit.setOnKeyListener(new View.OnKeyListener() {
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 // Enterキーハンドリング
                 if (KeyEvent.KEYCODE_ENTER == keyCode) {
                     // 押したときに改行を挿入防止処理
                     if (KeyEvent.ACTION_DOWN == event.getAction()) {
                         return true;
                     }
                     // 離したときにダイアログ上の[OK]処理を実行
                     else if (KeyEvent.ACTION_UP == event.getAction()) {
                         if (edit != null && edit.length() != 0) {
                             // ここで[OK]が押されたときと同じ処理をさせます
                             String editStr = edit.getText().toString();
                             // OKボタン押下時のハンドリング
                             Log.v(TAG, editStr);
                             (new Thread(new Runnable() {
                                 @Override
                                 public void run() {
                                     connectWebSocket();
                                 }
                             })).start();
                             // AlertDialogを閉じます
                             mAlertDialog.dismiss();
                         }
                         return true;
                     }
                 }
                 return false;
             }
         });
 
         // AlertDialog作成
         return new AlertDialog.Builder(this).setTitle("Server IP Address").setView(entryView)
                 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         String editStr = edit.getText().toString();
                         // OKボタン押下時のハンドリング
                         Log.v(TAG, editStr);
                         editor.putString("IPADDRESS", editStr);
                         editor.commit();
                         WS_URI = "ws://" + editStr + ":8001/";
                         (new Thread(new Runnable() {
                             @Override
                             public void run() {
                                 connectWebSocket();
                             }
                         })).start();
                     }
                 }).create();
     }
 }
