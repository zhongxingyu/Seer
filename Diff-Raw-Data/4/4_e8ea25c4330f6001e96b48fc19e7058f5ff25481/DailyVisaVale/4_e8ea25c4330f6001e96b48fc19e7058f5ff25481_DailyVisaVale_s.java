 package com.wirapuru.dailyvisavale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.*;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import hirondelle.date4j.DateTime;
 import org.htmlcleaner.CleanerProperties;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 import org.htmlcleaner.XPatherException;
 import org.json.JSONArray;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.*;
 
 public class DailyVisaVale extends Activity
 {
     public static final String PREFS_GLOBAL_SETTINGS = "settings";
     public static final String PREFS_CARD_NUMBERS = "card_numbers";
     public static final String PREF_CARD_NUMBERS_JSON = "json";
     public static final String PREFS_CURRENT_CARD_NUMBER = "current_card_number";
     public static final String PREF_CURRENT_CARD_NUMBER = "card_number";
     public static final String PREFS_LAST_CARD_NUMBER = "last_card_number";
     public static final String PREF_LAST_CARD_NUMBER = "card_number";
 
     public static final String LOG_TAG_ALL = "dvv";
     public static final String URL_TO_FETCH_CARD_DATA = "http://www.cbss.com.br/inst/convivencia/SaldoExtrato.jsp?numeroCartao=";
 
     public GlobalSettings global_settings;
     public CardNumbers card_numbers = new CardNumbers();
    public CardData card = new CardData();
     public UI ui = new UI();
     
     private ProgressDialog dialog = null;
     private String current_card_number = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // load global settings
         global_settings = new GlobalSettings();
 
         // default interface setup
         ui.setupMainInterface();
         
         // autocomplete past typed card numbers
 //        clearTypedCardNumbers();
         card_numbers.rememberLastCard();
         card_numbers.buildCardNumberAutoComplete(this);
     }
 
     private class GlobalSettings {
         public final static int SETTING_TIMEZONE = 0;
         public final static int SETTING_LOCALE = 1;
         public final static int SETTING_IGNORE_TODAY = 2;
 
         
         final SharedPreferences settings = getSharedPreferences(PREFS_GLOBAL_SETTINGS, 0);
         private final ArrayList<String> defaults = new ArrayList<String>();
         
         public GlobalSettings() {
             setupDefaults();            
         }
 
         private void setupDefaults() {
             HashMap<Integer, String> default_values = new HashMap<Integer, String>();
 
             default_values.put(SETTING_TIMEZONE, "-3");
             default_values.put(SETTING_LOCALE, "pt_BR");
             default_values.put(SETTING_IGNORE_TODAY, "false");
             
             defaults.addAll(default_values.values());
         }
 
         public String getDefault(int setting_index) {
             return defaults.get(setting_index);
         }
         
         public String get(int setting_index) {
             return settings.getString(String.valueOf(setting_index), getDefault(setting_index)); 
         }
 
         public Boolean asBoolean(int setting_index) {
             return Boolean.valueOf(get(setting_index));
         }
 
         public String asString(int setting_index) {
             return get(setting_index);
         }
 
         public Integer asInteger(int setting_index) {
             return Integer.parseInt(get(setting_index));
         }
     }
     
     private class CardNumbers {
         /**
          * Called onClick event on main button at main interface.
          * Validates, saves and sets card number.
          *
          * @param view
          */
         public void inputCardNumber(View view) {
             AutoCompleteTextView tx_card_number = (AutoCompleteTextView) findViewById(R.id.card_number);
             String str_card_number = tx_card_number.getText().toString();
 
             if (cardNumberIsValid(str_card_number)) {
                 saveCardNumber(str_card_number);
                 setCurrentCardNumber(str_card_number);
                 buildCardNumberAutoComplete(view.getContext());
                 logCardNumbers();
 
                 try {
                     card.setAndLoad(str_card_number);
                     card.fetchRemoteData();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
             } else {
                 Toast toast = Toast.makeText(getApplicationContext(), "Invalid card number: "+str_card_number, Toast.LENGTH_SHORT);
                 toast.show();
             }
         }
 
         public void setCurrentCardNumber(String card_number) {
             current_card_number = card_number;
             saveCurrentCardNumber(card_number);
         }
 
         public boolean cardNumberIsValid(String card_number) {
             return card_number.length() == 16;
         }
 
         public boolean rememberLastCard() {
             final SharedPreferences prefs_last_card_number = getSharedPreferences(PREFS_LAST_CARD_NUMBER, 0);
 
             if (prefs_last_card_number.contains(PREF_LAST_CARD_NUMBER)) {
                 final AutoCompleteTextView txt_card_number = (AutoCompleteTextView) findViewById(R.id.card_number);
                 txt_card_number.setText(prefs_last_card_number.getString(PREF_LAST_CARD_NUMBER, null));
                 return true;
             }
 
             return false;
         }
 
         public void buildCardNumberAutoComplete(Context context) {
             List<String> card_numbers_list = Util.JSONArrayToList(getTypedCardNumbers());
 
             if (card_numbers_list.size() > 0) {
                 final AutoCompleteTextView txt_card_number = (AutoCompleteTextView) findViewById(R.id.card_number);
                 ArrayAdapter<String> adapter =  new ArrayAdapter<String>(context, R.layout.card_number_item, card_numbers_list);
                 txt_card_number.setThreshold(0);
 
                 txt_card_number.setAdapter(adapter);
             }
         }
 
         public void saveCardNumber(String str_card_number) {
             try {
                 final SharedPreferences prefs_card_numbers = getSharedPreferences(PREFS_CARD_NUMBERS, 0);
 
                 JSONArray json_array = new JSONArray();
                 if (prefs_card_numbers.contains(PREF_CARD_NUMBERS_JSON)) {
                     final String json_string = prefs_card_numbers.getString(PREF_CARD_NUMBERS_JSON, null);
                     if (json_string != null) {
                         List<String> card_numbers = Util.JSONArrayToList(new JSONArray(json_string));
                         if (card_numbers.contains(str_card_number))
                             throw new CardNumberExistsException();
                         for (String card_number : card_numbers)
                             json_array.put(card_number);
                     }
                 }
                 json_array.put(str_card_number);
 
                 SharedPreferences.Editor prefs_editor = prefs_card_numbers.edit();
                 prefs_editor.clear();
                 prefs_editor.putString(PREF_CARD_NUMBERS_JSON, json_array.toString());
                 prefs_editor.commit();
 
                 // set last card number
                 saveLastCardNumber(str_card_number);
 
             } catch (CardNumberExistsException e) {
                 saveLastCardNumber(str_card_number);
             } catch (Exception e) {
                 Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
                 toast.show();
             }
         }
 
         public void saveLastCardNumber(String str_card_number) {
             final SharedPreferences prefs_last_card_number = getSharedPreferences(PREFS_LAST_CARD_NUMBER, 0);
             SharedPreferences.Editor prefs_editor = prefs_last_card_number.edit();
             prefs_editor.clear();
             prefs_editor.putString(PREF_LAST_CARD_NUMBER, str_card_number);
             prefs_editor.commit();
         }
 
 
         public void saveCurrentCardNumber(String str_card_number) {
             try {
                 final SharedPreferences prefs_current_card_number = getSharedPreferences(PREFS_CURRENT_CARD_NUMBER, 0);
 
                 SharedPreferences.Editor prefs_editor = prefs_current_card_number.edit();
                 prefs_editor.clear();
                 prefs_editor.putString(PREF_CURRENT_CARD_NUMBER, str_card_number);
                 prefs_editor.commit();
 
             } catch (Exception e) {
                 Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
                 toast.show();
             }
         }
 
         public void logCardNumbers() {
             try {
                 String json_string = getTypedCardNumbers().toString();
                 String to_log = json_string.length() > 0 ? "Stored card numbers: "+json_string : "Empty card numbers.";
                 Log.i(LOG_TAG_ALL, to_log);
             } catch (Exception e) {
                 Log.w(LOG_TAG_ALL, e.getMessage());
             }
         }
 
         public JSONArray getTypedCardNumbers() {
             try {
                 SharedPreferences prefs_card_numbers = getSharedPreferences(PREFS_CARD_NUMBERS, 0);
 
                 return prefs_card_numbers.contains(PREF_CARD_NUMBERS_JSON) ?
                         new JSONArray(prefs_card_numbers.getString(PREF_CARD_NUMBERS_JSON, null)) :
                         new JSONArray();
 
             } catch (Exception e) {
                 Log.w(LOG_TAG_ALL, "Cannot get card numbers: " + e.getMessage());
                 return new JSONArray();
             }
         }
 
         public void clearTypedCardNumbers() {
             SharedPreferences.Editor editor = getSharedPreferences(PREFS_CARD_NUMBERS,0).edit();
             editor.clear();
             editor.commit();
         }
     }
 
     public class CardData {
         public static final String PREFS_CARD_PREFIX = "card__";
         public static final String PREF_CARD_JSON_DATA = "json_data";
 
         public static final char DATA_BALANCE = 'b';
         public static final char DATA_LAST_DATE = 'l';
         public static final char DATA_CHARGES_DATES = 'c';
         
         private static final int FETCH_DATA_TIMEOUT_MS = 10000;
         private static final int FETCH_DATA_LOOPTIME_MS = 1000;
 
         private String card_number;
         private HashMap<Character, Object> raw_data = new HashMap<Character, Object>();
         private HashMap<Character, Object> parsed_data = new HashMap<Character, Object>();
         private ArrayList<DateTime> current_period_days = new ArrayList<DateTime>();
         private ArrayList<DateTime> period_remaining_days = new ArrayList<DateTime>();
 
         public void setAndLoad(String card_number) throws Exception{
             if (!numberIsValid(card_number))
                 throw new Exception("Invalid card number: '"+card_number+"'.");
 
             this.setCardNumber(card_number);
 //            this.loadData();            
         }
 //        private void loadData() {
 //            final SharedPreferences prefs_data = getSharedPreferences(PREFS_CARD_PREFIX+this.getCardNumber(), 0);
 //
 //            try {
 //                JSONArray json_data = prefs_data.contains(PREF_CARD_JSON_DATA) ?
 //                        new JSONArray(prefs_data.getString(PREF_CARD_NUMBERS_JSON, null)) :
 //                            new JSONArray();
 //
 //            } catch (Exception e) {
 //                Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
 //                toast.show();
 //            }
 //        }
         public void fetchRemoteData() {
             try {
                 // start fetch process
                 dialog = new ProgressDialog(DailyVisaVale.this);
                 dialog.setTitle("Coletando dados");
                 dialog.setMessage("Por favor, aguarde...");
                 dialog.show();
 
                 Fetchables fetchables = new Fetchables();
 
                 fetchables.add( Fetchable.TYPE_SINGLE,
                         DATA_BALANCE,
                         "//td[@class='corUm fontWeightDois'][2]",
                         URL_TO_FETCH_CARD_DATA+this.getCardNumber());
                 fetchables.add( Fetchable.TYPE_SINGLE,
                         DATA_LAST_DATE,
                         "//table[@class='consulta'][1]/tbody/tr[@class='rowTable'][3]/td[@class='corUm'][1]",
                         URL_TO_FETCH_CARD_DATA+this.getCardNumber());
                 fetchables.add( Fetchable.TYPE_COLLECTION,
                         DATA_CHARGES_DATES,
                         "//table[@class='consulta'][3]/tbody/tr[@class='rowTable']/td[1]",
                         URL_TO_FETCH_CARD_DATA+this.getCardNumber());
 
                 RemoteParsedData task = new RemoteParsedData(fetchables);
                 Thread thread = new Thread(task);
                 thread.start();
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         public void parseRawData() {
             for (Character data_name : raw_data.keySet()) {
                 switch (data_name) {
                     case DATA_BALANCE:
                         parsed_data.put(DATA_BALANCE, Util.extractMoneyValue(raw_data.get(DATA_BALANCE).toString()));
                         break;
                     case DATA_LAST_DATE:
                         parsed_data.put(DATA_LAST_DATE, Util.extractDate(raw_data.get(DATA_LAST_DATE).toString()));
                         break;
                     case DATA_CHARGES_DATES:
                         ArrayList<DateTime> values = new ArrayList<DateTime>();
                         // todo solve unchecked cast
                         for (String date_string : (ArrayList<String>) raw_data.get(DATA_CHARGES_DATES))
                             values.add(Util.extractDate(date_string));
                         parsed_data.put(DATA_CHARGES_DATES, values);
                         break;
                 }
             }
         }
         
         public void buildCurrentPeriodCalendar() {
             DateTime start_date = (DateTime) parsed_data.get(DATA_LAST_DATE);
             DateTime end_date = getLastDayForPeriodStartingIn(start_date);
 
             // todo apply national holydays
             BusinessDaysCalendar business_days = new BusinessDaysCalendar();
             current_period_days.addAll(business_days.getBusinessDaysInRange(start_date, end_date));
             int i = 1;
         }
         
         private DateTime getLastDayForPeriodStartingIn(DateTime start) {
             // todo must check if period has variable rules
             return start.plus(0,1,0,0,0,0, DateTime.DayOverflow.LastDay);
         }
 
         private void processPeriodRemainingDays() {
             TimeZone timezone = TimeZone.getTimeZone(global_settings.asString(GlobalSettings.SETTING_TIMEZONE));
             DateTime today = DateTime.today(timezone);
             
             ArrayList<DateTime> period_days = getCurrentPeriodDays();
             period_remaining_days.clear();
             for (DateTime day : period_days) {
                 if (day.isInTheFuture(timezone) || (!isToIgnoreToday() && day.isSameDayAs(today)) ) {
                     period_remaining_days.add(day);
                 }
             }
         }
 
         public String getCardNumber() {
             return card_number;
         }
 
         public void setCardNumber(String card_number) {
             this.card_number = card_number;
         }
         
         final public boolean numberIsValid(String card_number) {
             return card_number.length() == 16;
         }
         
         final public Boolean isToIgnoreToday() {
             if (global_settings.asBoolean(GlobalSettings.SETTING_IGNORE_TODAY))
                 return true;
             
             // try to guess if today was charged already
             DateTime today = DateTime.today(TimeZone.getTimeZone(global_settings.asString(GlobalSettings.SETTING_TIMEZONE)));
             // todo solve unchecked cast
             ArrayList<DateTime> charges_dates = (ArrayList<DateTime>) parsed_data.get(DATA_CHARGES_DATES);
             DateTime last_charged_day = charges_dates.size() == 0 ? today.minusDays(1) : charges_dates.get(0);
             return today.isSameDayAs(last_charged_day);
         }
 
         public HashMap<Character, Object> getRawData() {
             return raw_data;
         }
 
         public HashMap<Character, Object> getParsedData() {
             return parsed_data;
         }
 
         public ArrayList<DateTime> getCurrentPeriodDays() {
             if (current_period_days.size() == 0)
                 buildCurrentPeriodCalendar();
             return current_period_days;
         }
 
         public ArrayList<DateTime> getPeriodRemainingDays() {
             if (period_remaining_days.size() == 0)
                 processPeriodRemainingDays();            
             return period_remaining_days;
         }
         
         public Float getRemainingBalance() throws Exception {
             if (!parsed_data.containsKey(DATA_BALANCE)) 
                 throw new Exception("Parsed data doesn't seem to exist.");
             
             return (Float) parsed_data.get(DATA_BALANCE);
         }
 
         public Float getParsedDataAsFloat(char data_name_key) throws Exception {
             if (!parsed_data.containsKey(data_name_key))
                 throw new Exception("Parsed data at key '"+data_name_key+"' not defined.");
             return (Float) parsed_data.get(data_name_key);
         }
         
         private class MetaStats {
             
             public static final int DAILY_AMOUNT_LEFT_TO_SPENT = 10;
 
             private DateTime last_update;
             private Boolean expired;
 
             public MetaStats() {
 
             }
 
             public void expire() {
                 setExpired(true);
             }
 
             public DateTime getLastUpdate() {
                 return last_update;
             }
 
             public void setLastUpdate(DateTime last_update) {
                 this.last_update = last_update;
             }
 
             public Boolean getExpired() {
                 return expired;
             }
 
             public void setExpired(Boolean expired) {
                 this.expired = expired;
             }
         }
         
     }
 
     private class Fetchables implements Iterable<Fetchable> {
         private ArrayList<Fetchable> fetchables = new ArrayList<Fetchable>();
         private ArrayList<Character> names = new ArrayList<Character>();
 
         public void add(byte type, char name, String expression, String url) {
             int i = fetchables.size();
             fetchables.add(i, new Fetchable(type, name, expression, url));
             names.add(i, name);
         }
         
         public Fetchable getByName(Character name) throws Exception {
             int i = names.indexOf(name);
             if (i >= 0)
                 return fetchables.get(i);
             else
                 throw new Exception("Fetchable named '"+name+"' doesn't exist.");
         }
 
         @Override
         public Iterator<Fetchable> iterator() {
             return fetchables.iterator();
         }
     }
 
     private class Fetchable {
         public static final byte TYPE_SINGLE = 1;
         public static final byte TYPE_COLLECTION = 2;
 
         private Character name;
         private String expression;
         private String url;
         private byte type;
         
         public Fetchable(byte type, Character name, String expression, String url) {
             setName(name);
             setExpression(expression);
             setUrl(url);
             setType(type);
         }
 
         public Character getName() {
             return name;
         }
 
         public void setName(Character name) {
             this.name = name;
         }
 
         public String getExpression() {
             return expression;
         }
 
         public void setExpression(String expression) {
             this.expression = expression;
         }
 
         public String getUrl() {
             return url;
         }
 
         public void setUrl(String url) {
             this.url = url;
         }
 
         public byte getType() {
             return type;
         }
 
         public void setType(byte type) {
             this.type = type;
         }
     }
 
     private class RemoteParsedData implements Runnable {
         private static final int RESULT_ALL_DATA_DONE = 1;
         private static final int RESULT_INTERRUPTED = 11;
         private static final int RESULT_TIMEOUT = 12;
         
         private Fetchables _fetchables;
         private HashMap<Character, Object> fetched_values = new HashMap<Character, Object>();
         public final HashMap<URL, TagNode> tagnode_cache = new HashMap<URL, TagNode>();
 
         public RemoteParsedData(Fetchables fetchables) {
             setFetchables(fetchables);
         }
 
         public void run() {
             try {
 
                 fetchRemoteData();
                 handler.sendEmptyMessage(RESULT_ALL_DATA_DONE);
                 
             } catch (FetchRemoteDataException e) {
                 switch (e.getError()) {
                     case FetchRemoteDataException.ERROR_INTERRUPTED:
                         handler.sendEmptyMessage(RESULT_INTERRUPTED);
                         break;
                     case FetchRemoteDataException.ERROR_TIMEOUT:
                         handler.sendEmptyMessage(RESULT_TIMEOUT);
                         break;
                     case FetchRemoteDataException.ERROR_UNKNOWN:
                         break;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         
         private void fetchRemoteData() throws Exception {
             // parser from htmlcleaner
             HtmlCleaner pageParser = new HtmlCleaner();
             CleanerProperties props = pageParser.getProperties();
             props.setAllowHtmlInsideAttributes(true);
             props.setAllowMultiWordAttributes(true);
             props.setRecognizeUnicodeChars(true);
             props.setOmitComments(true);
 
             for (Fetchable fetchable : getFetchables()) {
                 if (Thread.currentThread().isInterrupted())
                     throw new FetchRemoteDataException(FetchRemoteDataException.ERROR_INTERRUPTED);
 
                 Character data_name = fetchable.getName();
                 String data_url = fetchable.getUrl();
                 String data_expression = fetchable.getExpression();
 
                 try {
                     URL url = new URL(data_url);
 
                     // todo implement timeout handling
                     TagNode node = tagnode_cache.containsKey(url) ?
                             tagnode_cache.get(url) :
                             pageParser.clean(new InputStreamReader(url.openConnection().getInputStream()));
 
                     try {
                         Object[] td_nodes = node.evaluateXPath(data_expression);
                         switch (fetchable.getType()) {
                             case Fetchable.TYPE_SINGLE:
                                 fetched_values.put(data_name, new StringBuilder());
                                 for (int i =0; i < td_nodes.length; i++) {
                                     if (fetched_values.get(data_name).toString().length() == 0) {
                                         List children = ((TagNode)td_nodes[i]).getChildren();
                                         if (children.size() > 0) {
                                             ((StringBuilder)fetched_values.get(data_name)).append(((TagNode)td_nodes[i]).getChildren().get(0).toString());
                                         }
                                     } else {
                                         break;
                                     }
                                 }
                                 break;
                             case Fetchable.TYPE_COLLECTION:
                                 ArrayList<String> values = new ArrayList<String>();
                                 for (int i =0; i < td_nodes.length; i++) {
                                     List children = ((TagNode)td_nodes[i]).getChildren();
                                     if (children.size() > 0)
                                         values.add(((TagNode)td_nodes[i]).getChildren().get(0).toString());
                                 }
                                 fetched_values.put(data_name, values);
                                 break;
                         }
 
                     } catch (XPatherException e) {
                         e.printStackTrace();
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }            
         }
         
         private Handler handler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 switch (msg.what) {
 
                     case RESULT_ALL_DATA_DONE:
 
                         try {
                             card.raw_data.putAll(fetched_values);
                                 card.parseRawData();
                             card.buildCurrentPeriodCalendar();
                             card.processPeriodRemainingDays();
                             Float daily_amount = new MetaStatDailyAmount(card).getAsFloat();
 
                             dialog.dismiss();
 
                             Toast toast = Toast.makeText(getApplicationContext(), daily_amount.toString(), Toast.LENGTH_LONG);
                             toast.show();
 
                             Log.v(LOG_TAG_ALL, card.toString());
                             Log.v(LOG_TAG_ALL, card.parsed_data.toString());
                             Log.v(LOG_TAG_ALL, card.current_period_days.toString());
                             Log.v(LOG_TAG_ALL, card.period_remaining_days.toString());
                         } catch (Exception e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
 
                         break;
 
                     case RESULT_INTERRUPTED:
                         card.raw_data.clear();
                         dialog.dismiss();
                         break;
 
                     case RESULT_TIMEOUT:
                         card.raw_data.clear();
                         dialog.dismiss();
                         ui.showTimeoutDialog();
                         break;
                 }
 
             }
         };
 
         public Fetchables getFetchables() {
             return this._fetchables;
         }
 
         public void setFetchables(Fetchables fetchables) {
             this._fetchables = fetchables;
         }
         
         private class FetchRemoteDataException extends Exception {
             public static final byte ERROR_UNKNOWN = 1;
             public static final byte ERROR_TIMEOUT = 2;
             public static final byte ERROR_INTERRUPTED = 3;
 
             private byte code;
 
             public FetchRemoteDataException() {
                 super();
                 setCode(ERROR_UNKNOWN);
             }
 
             public FetchRemoteDataException(byte code) {
                 super();
                 setCode(code);
             }            
             
             public byte getError() {
                 return getCode();
             }
 
             public byte getCode() {
                 return code;
             }
 
             public void setCode(byte code) {
                 this.code = code;
             }
         }
     }
 
     private class UI {
         public void setupMainInterface() {
             // button onclick
             Button bt_input_card_number = (Button) findViewById(R.id.set_card_number);
             bt_input_card_number.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     card_numbers.inputCardNumber(view);
                 }
             });
             
         }
         
         public void showTimeoutDialog() {
             AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
 
             DialogInterface.OnClickListener retry = new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     dialog.dismiss();
                     card_numbers.inputCardNumber(getCurrentFocus());
                 }
             };
 
             DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     dialog.dismiss();
                 }
             };
 
             builder.setTitle("Conexão Visa Vale")
                     .setCancelable(true)
                     .setMessage("O Visa Vale está demorando a responder ou se encontra indisponível.\nO que deseja fazer?")
                     .setPositiveButton("Tentar novamente", retry)
                     .setNegativeButton("Cancelar", cancel)
             ;
 
             builder.create().show();
         }
     }
 
     private class CardNumberExistsException extends Exception {
 
     }
 
     private class FetchRemoteDataTimeoutException extends Exception {
 
     }
 }
