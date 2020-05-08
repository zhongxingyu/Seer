 package ru.peppers;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.text.InputType;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import model.Order;
 import myorders.MyCostOrder;
 
 public class MyOrderItemActivity extends BalanceActivity {
 
     protected static final int REQUEST_EXIT = 0;
     private CountDownTimer timer;
     private TextView counterView;
     private MyCostOrder order;
     private Dialog dialog;
     private Bundle bundle;
     private TextView tv;
 
     private Timer myTimer = new Timer();
     private Integer refreshperiod = null;
     private boolean start = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.myorder);
         Bundle bundle = getIntent().getExtras();
         int index = bundle.getInt("index");
 
         counterView = (TextView) findViewById(R.id.textView1);
         tv = (TextView) findViewById(R.id.textView2);
         order = (MyCostOrder) TaxiApplication.getDriver().getOrder(index);
 
         ArrayList<String> orderList = order.toArrayList();
 
         int arraySize = orderList.size();
         for (int i = 0; i < arraySize; i++) {
             tv.append(orderList.get(i));
             tv.append("\n");
         }
 
         // if (order.getTimerDate() != null) {
         // timerInit(order);
         // }
 
         Button button = (Button) findViewById(R.id.button1);
         button.setText(this.getString(R.string.choose_action));
         button.setOnClickListener(new Button.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 initActionDialog();
             }
 
         });
         getOrder();
 
     }
 
     private void getOrder() {
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
         nameValuePairs.add(new BasicNameValuePair("action", "get"));
         nameValuePairs.add(new BasicNameValuePair("mode", "available"));
         nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
         nameValuePairs.add(new BasicNameValuePair("object", "order"));
         nameValuePairs.add(new BasicNameValuePair("orderid", order.get_index()));
 
         Document doc = PhpData.postData(this, nameValuePairs, PhpData.newURL);
         if (doc != null) {
             Node responseNode = doc.getElementsByTagName("response").item(0);
             Node errorNode = doc.getElementsByTagName("message").item(0);
 
             if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                 PhpData.errorFromServer(this, errorNode);
             else {
                 try {
                     initOrder(doc);
                 } catch (Exception e) {
                     PhpData.errorHandler(this, e);
                 }
             }
         }
     }
 
     private void initOrder(Document doc) throws DOMException, ParseException {
         tv.setText("");
         // nominalcost - рекомендуемая стоимость заказа
         // class - класс автомобля (0 - все равно, 1 - Эконом, 2 - Стандарт,
         // 3 - Базовый)
         // addressdeparture - адрес подачи автомобиля
         // departuretime - время подачи(если есть)
         // paymenttype - форма оплаты (0 - наличные, 1 - безнал)
         // invitationtime - время приглашения (если пригласили)
         // quantity - количество заказов от этого клиента
         // comment - примечание
         // nickname - ник абонента (если есть)
         // registrationtime - время регистрации заказа
         // addressarrival - куда поедут
 
         Element item = (Element) doc.getElementsByTagName("order").item(0);
 
         Node nominalcostNode = item.getElementsByTagName("nominalcost").item(0);
         Node classNode = item.getElementsByTagName("classid").item(0);
         Node addressdepartureNode = item.getElementsByTagName("addressdeparture").item(0);
         Node departuretimeNode = item.getElementsByTagName("departuretime").item(0);
         Node paymenttypeNode = item.getElementsByTagName("paymenttype").item(0);
         Node quantityNode = item.getElementsByTagName("quantity").item(0);
         Node commentNode = item.getElementsByTagName("comment").item(0);
         Node nicknameNode = item.getElementsByTagName("nickname").item(0);
         Node addressarrivalNode = item.getElementsByTagName("addressarrival").item(0);
         Node orderIdNode = item.getElementsByTagName("orderid").item(0);
         Node invitationNode = item.getElementsByTagName("invitationtime").item(0);
       // Node accepttimeNode = item.getElementsByTagName("accepttime").item(0);
 
         Date accepttime = null;
         Integer nominalcost = null;
         Integer carClass = 0;
         String addressdeparture = null;
         Date departuretime = null;
         Integer paymenttype = null;
         Integer quantity = null;
         String comment = null;
         String nickname = null;
         Date invitationtime = null;
         String addressarrival = null;
         String orderId = null;
 
         // if(departuretime==null)
         // //TODO:не предварительный
         // else
         // //TODO:предварительный
 
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
 
         if (!classNode.getTextContent().equalsIgnoreCase(""))
             carClass = Integer.valueOf(classNode.getTextContent());
 
         if (!nominalcostNode.getTextContent().equalsIgnoreCase(""))
             nominalcost = Integer.parseInt(nominalcostNode.getTextContent());
 
         // if (!registrationtimeNode.getTextContent().equalsIgnoreCase(""))
         // registrationtime = format.parse(registrationtimeNode.getTextContent());
 
         if (!addressdepartureNode.getTextContent().equalsIgnoreCase(""))
             addressdeparture = addressdepartureNode.getTextContent();
 
         if (!addressarrivalNode.getTextContent().equalsIgnoreCase(""))
             addressarrival = addressarrivalNode.getTextContent();
 
         if (!paymenttypeNode.getTextContent().equalsIgnoreCase(""))
             paymenttype = Integer.parseInt(paymenttypeNode.getTextContent());
 
         if (!departuretimeNode.getTextContent().equalsIgnoreCase(""))
             departuretime = format.parse(departuretimeNode.getTextContent());
 
         if (!commentNode.getTextContent().equalsIgnoreCase(""))
             comment = commentNode.getTextContent();
 
         if (!orderIdNode.getTextContent().equalsIgnoreCase(""))
             orderId = orderIdNode.getTextContent();
 
         if (!invitationNode.getTextContent().equalsIgnoreCase(""))
             invitationtime = format.parse(invitationNode.getTextContent());
 
        //if (!accepttimeNode.getTextContent().equalsIgnoreCase(""))
        //    accepttime = format.parse(accepttimeNode.getTextContent());
 
         order = new MyCostOrder(this, orderId, nominalcost, addressdeparture, carClass, comment,
                 addressarrival, paymenttype, invitationtime, departuretime,accepttime);
 
         if (!nicknameNode.getTextContent().equalsIgnoreCase("")) {
             nickname = nicknameNode.getTextContent();
 
             if (!quantityNode.getTextContent().equalsIgnoreCase(""))
                 quantity = Integer.parseInt(quantityNode.getTextContent());
             order.setAbonent(nickname);
             order.setRides(quantity);
         }
 
         ArrayList<String> orderList = order.toArrayList();
         int arraySize = orderList.size();
         for (int i = 0; i < arraySize; i++) {
             tv.append(orderList.get(i));
             tv.append("\n");
         }
 
         // TODO:UPDATE ORDER
         Node refreshperiodNode = doc.getElementsByTagName("refreshperiod").item(0);
         Integer newrefreshperiod = null;
         if (!refreshperiodNode.getTextContent().equalsIgnoreCase(""))
             newrefreshperiod = Integer.valueOf(refreshperiodNode.getTextContent());
 
         boolean update = false;
 
         Log.d("My_tag", refreshperiod + " " + newrefreshperiod + " " + update);
 
         if (newrefreshperiod != null) {
             if (refreshperiod != newrefreshperiod) {
                 refreshperiod = newrefreshperiod;
                 update = true;
             }
         }
 
         Log.d("My_tag", refreshperiod + " " + newrefreshperiod + " " + update);
 
         if (update && refreshperiod != null) {
             if (start) {
                 myTimer.cancel();
                 start = true;
                 Log.d("My_tag", "cancel timer");
             }
             final Handler uiHandler = new Handler();
 
             TimerTask timerTask = new TimerTask() { // Определяем задачу
                 @Override
                 public void run() {
                     uiHandler.post(new Runnable() {
                         @Override
                         public void run() {
                             getOrder();
                         }
                     });
                 }
             };
 
             myTimer.schedule(timerTask, 1000 * refreshperiod, 1000 * refreshperiod);
         }
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         myTimer.cancel();
         if (timer != null)
             timer.cancel();
     }
 
     private OnClickListener onContextMenuItemListener() {
         return new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 dialog.dismiss();
                 switch (item) {
                     case 0:
                         inviteDialog();
                         break;
                     case 1:
                         priceDialog();
                         break;
                     case 2:
                         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                         nameValuePairs.add(new BasicNameValuePair("action", "callback"));
                         nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
                         nameValuePairs.add(new BasicNameValuePair("object", "driver"));
                         nameValuePairs.add(new BasicNameValuePair("orderid", order.get_index()));
 
                         Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs,
                                 PhpData.newURL);
                         if (doc != null) {
                             Node responseNode = doc.getElementsByTagName("response").item(0);
                             Node errorNode = doc.getElementsByTagName("message").item(0);
 
                             if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                                 PhpData.errorFromServer(MyOrderItemActivity.this, errorNode);
                             else {
                                 new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("Звонок")
                                         .setMessage("Ваш запрос принят. Пожалуйста ожидайте звонка")
                                         .setNeutralButton("Ок", null).show();
                             }
                         }
                         break;
                     case 3:
                         timeDialog();
                         break;
                     default:
                         break;
                 }
             }
 
         };
     }
 
     private void inviteDialog() {
 
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
         nameValuePairs.add(new BasicNameValuePair("action", (order.get_invitationtime() == null) ? "invite"
                 : "hurry"));
         nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
         nameValuePairs.add(new BasicNameValuePair("object", "client"));
         nameValuePairs.add(new BasicNameValuePair("orderid", order.get_index()));
 
         Document doc = PhpData.postData(this, nameValuePairs, PhpData.newURL);
         if (doc != null) {
             Node responseNode = doc.getElementsByTagName("response").item(0);
             Node errorNode = doc.getElementsByTagName("message").item(0);
 
             if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                 PhpData.errorFromServer(MyOrderItemActivity.this, errorNode);
             else {
                 new AlertDialog.Builder(MyOrderItemActivity.this).setTitle(this.getString(R.string.Ok))
                         .setMessage(this.getString(R.string.invite_sended))
                         .setNeutralButton(this.getString(R.string.close), null).show();
             }
         }
     }
 
     private void timeDialog() {
         AlertDialog.Builder alert = new AlertDialog.Builder(MyOrderItemActivity.this);
         alert.setTitle(this.getString(R.string.time));
         final String cs[] = new String[] { "3", "5", "7", "10", "15" };// , "20", "25", "30", "35"};
 
         alert.setItems(cs, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 // https://www.abs-taxi.ru/fcgi-bin/office/cman.fcgi?module=mobile;object=order;action=delay;time=7
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                 nameValuePairs.add(new BasicNameValuePair("action", "delay"));
                 nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
                 nameValuePairs.add(new BasicNameValuePair("object", "order"));
                 nameValuePairs.add(new BasicNameValuePair("minutes", cs[which]));
                 nameValuePairs.add(new BasicNameValuePair("orderid", order.get_index()));
 
                 Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs, PhpData.newURL);
                 if (doc != null) {
                     Node responseNode = doc.getElementsByTagName("response").item(0);
                     Node errorNode = doc.getElementsByTagName("message").item(0);
 
                     if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                         PhpData.errorFromServer(MyOrderItemActivity.this, errorNode);
                     else {
                         dialog.dismiss();
                         // final Order order = TaxiApplication.getDriver().getOrder(index);
 
                         // if (order.getTimerDate() != null) {
                         // Calendar cal = Calendar.getInstance();
                         // cal.add(Calendar.MINUTE, Integer.valueOf((String) cs[which]));
                         //
                         // order.setTimerDate(cal.getTime());
                         // timer.cancel();
                         // timerInit(order);
                         // }
                         new AlertDialog.Builder(MyOrderItemActivity.this)
                                 .setTitle(MyOrderItemActivity.this.getString(R.string.Ok))
                                 .setMessage(MyOrderItemActivity.this.getString(R.string.order_delayed))
                                 .setNeutralButton(MyOrderItemActivity.this.getString(R.string.close), null)
                                 .show();
                     }
                 }
             }
 
         });
         alert.show();
 
     }
 
     private void initActionDialog() {
         ArrayList<String> arrayList = new ArrayList<String>();
         if (order.get_invitationtime() == null)
             arrayList.add(this.getString(R.string.invite));
         else
             arrayList.add("Поторопить");
         arrayList.add(this.getString(R.string.close));
         arrayList.add(this.getString(R.string.call_office));
 
         if (order.get_invitationtime() == null)
             arrayList.add(this.getString(R.string.delay));
 
         // final CharSequence[] items = { this.getString(R.string.invite) : ,
         // this.getString(R.string.delay), this.getString(R.string.close),
         // this.getString(R.string.call_office)};
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(this.getString(R.string.choose_action));
         builder.setItems(arrayList.toArray(new String[arrayList.size()]), onContextMenuItemListener());
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     private void timerInit(final Order order) {
         long diffInMs = order.getTimerDate().getTime() - new Date().getTime();
 
         timer = new CountDownTimer(diffInMs, 1000) {
 
             public void onTick(long millisUntilFinished) {
                 int seconds = ((int) millisUntilFinished / 1000) % 60;
                 String secondsStr = String.valueOf(seconds);
                 if (seconds <= 9)
                     secondsStr = "0" + seconds;
 
                 counterView.setText(((int) millisUntilFinished / 1000) / 60 + ":" + secondsStr);
                 if ((((int) millisUntilFinished / 1000) / 60) == 1
                         && (((int) millisUntilFinished / 1000) % 60) == 0) {
                     initActionDialog();
                 }
             }
 
             public void onFinish() {
                 counterView.setText(MyOrderItemActivity.this.getString(R.string.ended_timer));
                 alertDelay(order);
 
                 MediaPlayer mp = MediaPlayer.create(getBaseContext(), (R.raw.sound));
                 mp.start();
 
             }
         }.start();
     }
 
     private void alertDelay(final Order order) {
         AlertDialog.Builder alert = new AlertDialog.Builder(MyOrderItemActivity.this);
         alert.setTitle(this.getString(R.string.time));
         final CharSequence cs[];
 
         cs = new String[] { "3", "5", "7", "10", "15", "20", "25", "30", "35" };
 
         alert.setItems(cs, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 // Bundle extras = getIntent().getExtras();
                 // int id = extras.getInt("id");
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                 nameValuePairs.add(new BasicNameValuePair("action", "saveminutes"));
                 nameValuePairs.add(new BasicNameValuePair("order_id", String.valueOf(order.get_index())));
                 // nameValuePairs.add(new BasicNameValuePair("id",
                 // String.valueOf(id)));
                 nameValuePairs.add(new BasicNameValuePair("minutes", String.valueOf(cs[which])));
                 Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs);
                 if (doc != null) {
                     Node errorNode = doc.getElementsByTagName("error").item(0);
                     if (Integer.parseInt(errorNode.getTextContent()) == 1)
                         PhpData.errorHandler(MyOrderItemActivity.this, null);
                     else {
                         Calendar cal = Calendar.getInstance();
                         cal.setTime(new Date());
                         cal.add(Calendar.MINUTE, Integer.valueOf((String) cs[which]));
                         order.setTimerDate(cal.getTime());
                         timerInit(order);
                     }
                 }
             }
 
         });
         alert.show();
     }
 
     private void priceDialog() {
 
         // View view = getLayoutInflater().inflate(R.layout.custom_dialog,
         // null);
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         int isLightTheme = settings.getInt("theme", 0);
         if (isLightTheme != 0)
             dialog = new Dialog(this, android.R.style.Theme_Light);
         else
             dialog = new Dialog(this, android.R.style.Theme_Black);
         dialog.setContentView(R.layout.custom_dialog);
         dialog.setTitle(this.getString(R.string.price));
         dialog.show();
 
         Button btn = (Button) dialog.findViewById(R.id.button1);
         EditText input = (EditText) dialog.findViewById(R.id.editText1);
         input.setInputType(InputType.TYPE_CLASS_NUMBER);
         btn.setOnClickListener(onSavePrice(dialog));
 
         LinearLayout ll = (LinearLayout) dialog.findViewById(R.id.layout1);
         if (order.get_paymenttype() == 1 || (order.getAbonent() != null && order.get_paymenttype() == 0))// безнал
             ll.setVisibility(View.VISIBLE);
         if (order.getAbonent() != null && order.get_paymenttype() == 0)
             ((TextView) dialog.findViewById(R.id.textView2)).setText("Сдача");
 
         final CheckBox cb = (CheckBox) dialog.findViewById(R.id.checkBox1);
         final TextView tv = (TextView) dialog.findViewById(R.id.textView3);
         Button btn1 = (Button) dialog.findViewById(R.id.button2);
         Button btn2 = (Button) dialog.findViewById(R.id.button3);
         tv.setText(MyOrderItemActivity.this.getText(R.string.end_point) + " " + order.get_addressarrival());
         btn1.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 Intent intent = new Intent(MyOrderItemActivity.this, DistrictActivity.class);
                 intent.putExtra("close", true);
                 startActivityForResult(intent, REQUEST_EXIT);
             }
 
         });
         btn2.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 bundle = null;
                 tv.setText(MyOrderItemActivity.this.getText(R.string.end_point) + " "
                         + order.get_addressarrival());
             }
 
         });
 
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         if (requestCode == REQUEST_EXIT) {
             if (resultCode == RESULT_OK) {
                 bundle = data.getExtras();
                 TextView tv = (TextView) dialog.findViewById(R.id.textView3);
                 String district = bundle.getString("districtname");
                 String subdistrict = bundle.getString("subdistrictname");
                 String rayonString = "";
                 if (district != null) {
                     rayonString = district;
                     if (subdistrict != null)
                         rayonString += ", " + subdistrict;
                 }
                 tv.setText("Район: " + rayonString);
             }
         }
     }
 
     private Button.OnClickListener onSavePrice(final Dialog dialog) {
         return new Button.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 EditText input = (EditText) dialog.findViewById(R.id.editText1);
                 EditText cashless = (EditText) dialog.findViewById(R.id.editText2);
                 RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radioGroup1);
                 int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                 String state = "1";
                 if (checkedRadioButtonId == R.id.radio0) {
                     state = "1";
                 } else if (checkedRadioButtonId == R.id.radio1) {
                     state = "0";
                 }
 
                 if (input.getText().length() != 0) {
                     String value = input.getText().toString();
 
                     String cashvalue;
                     if (cashless.getText().length() == 0 && order.get_paymenttype() == 1)
                         cashvalue = value;
                     else {
                         cashvalue = "0";
 
                         if (order.get_paymenttype() == 1)
                             cashvalue = cashless.getText().toString();
                         else if (order.getAbonent() != null)
                             cashvalue = "-" + cashless.getText().toString();
                     }
 
                     List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                     nameValuePairs.add(new BasicNameValuePair("action", "close"));
                     nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
                     nameValuePairs.add(new BasicNameValuePair("object", "order"));
                     nameValuePairs.add(new BasicNameValuePair("orderid", order.get_index()));
                     // if (order.get_nominalcost() != null)
                     // orderCost = String.valueOf(order.get_nominalcost());
                     nameValuePairs.add(new BasicNameValuePair("cost", value));
                     nameValuePairs.add(new BasicNameValuePair("cashless", cashvalue));
                     nameValuePairs.add(new BasicNameValuePair("state", state));
                     if (bundle != null) {
                         nameValuePairs
                                 .add(new BasicNameValuePair("districtid", bundle.getString("district")));
                         // nameValuePairs.add(new
                         // BasicNameValuePair("subdistrictid",
                         // bundle.getString("subdistrict")));
                     }
                     Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs, PhpData.newURL);
                     if (doc != null) {
                         Node responseNode = doc.getElementsByTagName("response").item(0);
                         Node errorNode = doc.getElementsByTagName("message").item(0);
 
                         if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                             PhpData.errorFromServer(MyOrderItemActivity.this, errorNode);
                         else {
                             dialog.dismiss();
                             new AlertDialog.Builder(MyOrderItemActivity.this)
                                     .setTitle(MyOrderItemActivity.this.getString(R.string.Ok))
                                     .setMessage(MyOrderItemActivity.this.getString(R.string.order_closed))
                                     .setNeutralButton(MyOrderItemActivity.this.getString(R.string.close),
                                             new OnClickListener() {
 
                                                 @Override
                                                 public void onClick(DialogInterface dialog, int which) {
                                                     if (timer != null)
                                                         timer.cancel();
                                                     setResult(RESULT_OK);
                                                     finish();
                                                 }
                                             }).show();
                         }
                     }
                 }
             }
         }
 
         ;
     }
 }
