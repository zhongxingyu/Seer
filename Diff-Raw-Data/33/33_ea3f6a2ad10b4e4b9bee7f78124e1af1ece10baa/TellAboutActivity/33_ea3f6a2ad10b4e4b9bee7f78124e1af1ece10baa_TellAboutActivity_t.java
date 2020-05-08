 package com.vgershman.whocall.activity;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.text.InputType;
 import android.view.View;
 import android.widget.*;
 import com.vgershman.whocall.R;
 import com.vgershman.whocall.app.AppInfo;
 import com.vgershman.whocall.connection.Request;
 import com.vgershman.whocall.connection.RequestPostCallback;
 import com.vgershman.whocall.util.DialogsUtil;
 import com.vgershman.whocall.util.OnInputChangedListener;
 import com.vgershman.whocall.util.ReverseGeoCoding;
 import com.vgershman.whocall.util.ReverseGeoCodingListener;
 
 import java.util.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: User
  * Date: 29.01.13
  * Time: 0:22
  * To change this template use File | Settings | File Templates.
  */
 public class TellAboutActivity extends Activity {
 
     String locationText;
     String point;
 
     String type = "Физическое лицо";
     String fio="";
     String phone="";
     String email="";
     String time="";
     String comments="";
 
 
     FrameLayout fioSetting;
     FrameLayout accountType;
     FrameLayout phoneSetting;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.tell_avout_myself_activity);
        if(getIntent().getBooleanExtra("myself",false)){
            ((TextView)findViewById(R.id.textTitle)).setText(getText(R.string.sharetitle));
        }else {
            ((TextView)findViewById(R.id.textTitle)).setText(getText(R.string.aboutmetitle));
        }


         ImageButton navBack = (ImageButton)findViewById(R.id.nav_back);
         getCurrentLocation();
         navBack.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 onBackPressed();
             }
         });
 
         try{
             String myEmail = getMyEmail();
             if (myEmail!=null){
                 ((TextView)findViewById(R.id.itemEMailSelection)).setText(myEmail);
             }
 
         }catch (Exception ex){}
 
         try{
             String myPhone = getMyPhoneNumber();
             if (myPhone!=null){
                 ((TextView)findViewById(R.id.itemPhoneSelection)).setText(myPhone);
             }
 
         }catch (Exception ex){}
 
        if(getIntent().getStringExtra("phone")!=null){
            ((TextView)findViewById(R.id.itemPhoneSelection)).setText(getIntent().getStringExtra("phone"));
            phone = getIntent().getStringExtra("phone");
        }

         findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(checkFields()){
                     Map<String,String> params = new HashMap<String, String>();
                     params.put("name",fio);
                     params.put("phone",phone);
                     params.put("email",email);
                     params.put("worktime",time);
                     params.put("description",comments);
                     params.put("point",point);
                     params.put("location",locationText);
                     params.put("imei", AppInfo.getIMEI());
                     Request.postInfo(params,new RequestPostCallback() {
                         @Override
                         public void onSuccess() {
                             finish();
                         }
 
                         @Override
                         public void onFailure() {
                             Toast.makeText(TellAboutActivity.this,getText(R.string.server_error),2000).show();
                         }
                     });
 
                 }
             }
         });
 
 
         accountType = (FrameLayout)findViewById(R.id.accountType);
         accountType.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 final List<String> options = new ArrayList<String>();
                 options.add("Физическое лицо");
                 options.add("Юридическое лицо");
                 options.add("Мошенник");
                 DialogsUtil.showDropdownDialog(TellAboutActivity.this,"Тип аккаунта", options,new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         TextView textView = (TextView)findViewById(R.id.itemAccountSelection);
                         textView.setText(options.get(i));
                         type = options.get(i);
                     }
                 });
             }
         });
 
         fioSetting = (FrameLayout) findViewById(R.id.fioSetting);
         fioSetting.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 DialogsUtil.showInputDialog(TellAboutActivity.this, "Введите ваши Ф.И.О.", InputType.TYPE_CLASS_TEXT, new OnInputChangedListener() {
                     @Override
                     public void onInputChanged(String input) {
                         TextView fioSelection = (TextView) findViewById(R.id.itemFIOSelection);
                         fioSelection.setText(input);
                         fio = input;
                     }
                 });
             }
         });
 
         phoneSetting = (FrameLayout) findViewById(R.id.phoneSetting);
         phoneSetting.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 DialogsUtil.showInputDialog(TellAboutActivity.this, "Телефон", InputType.TYPE_CLASS_PHONE, new OnInputChangedListener() {
                     @Override
                     public void onInputChanged(String input) {
                         TextView fioSelection = (TextView) findViewById(R.id.itemPhoneSelection);
                         fioSelection.setText(input);
                         phone = input;
                     }
                 });
             }
         });
 
          findViewById(R.id.emailSetting).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  DialogsUtil.showInputDialog(TellAboutActivity.this, "Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, new OnInputChangedListener() {
                      @Override
                      public void onInputChanged(String input) {
                          ((TextView) findViewById(R.id.itemEMailSelection)).setText(input);
                          email = input;
                      }
                  });
              }
          });
 
         findViewById(R.id.timeSetting).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 DialogsUtil.showInputDialog(TellAboutActivity.this, "Рабочее время", InputType.TYPE_DATETIME_VARIATION_TIME, new OnInputChangedListener() {
                     @Override
                     public void onInputChanged(String input) {
                         ((TextView) findViewById(R.id.itemTimeSelection)).setText(input);
                         time = input;
                     }
                 });
             }
         });
 
         findViewById(R.id.commentSetting).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 DialogsUtil.showInputDialog(TellAboutActivity.this, "Комментарии", InputType.TYPE_CLASS_TEXT, new OnInputChangedListener() {
                     @Override
                     public void onInputChanged(String input) {
                         ((TextView) findViewById(R.id.itemNotificationSelection)).setText(input);
                         comments = input;
                     }
                 });
             }
         });
     }
 
     private void getCurrentLocation() {
         LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
         Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 
         if (location == null) {
             location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         }
         if (location != null) {
             point = "Lat:" + location.getLatitude() + ", Lon:" + location.getLongitude();
             new ReverseGeoCoding(this, location, new ReverseGeoCodingListener() {
                 @Override
                 public void OnSuccess(String s) {
                     locationText = s;
                 }
 
                 @Override
                 public void OnFailure() {
                     locationText = "";
 
                 }
             }).execute();
         } else {
             point = "";
             locationText = "";
         }
     }
 
 
 
     private boolean checkFields() {
         Toast toast = Toast.makeText(this, "Неверно введен телефон и/или название", 2000);
         if (phone.length() < 5) {
             toast.show();
             return false;
         }
         if (fio.length()  < 3) {
             toast.show();
             return false;
         }
         return true;
     }
 
     private String getMyPhoneNumber() {
         TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
         String number = tm.getLine1Number();
         return number;
     }
 
     private String getMyEmail() {
         AccountManager am = AccountManager.get(this);
         Account[] accounts = am.getAccounts();
         ArrayList<String> googleAccounts = new ArrayList<String>();
         for (Account ac : accounts) {
             String acname = ac.name;
             String actype = ac.type;
 
             if (ac.type.equals("com.google")) {
                 googleAccounts.add(ac.name);
             }
 
         }
         if (googleAccounts.size() == 0) {
             return null;
         } else {
             return googleAccounts.get(0);
         }
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();    //To change body of overridden methods use File | Settings | File Templates.
         finish();
     }
 }
