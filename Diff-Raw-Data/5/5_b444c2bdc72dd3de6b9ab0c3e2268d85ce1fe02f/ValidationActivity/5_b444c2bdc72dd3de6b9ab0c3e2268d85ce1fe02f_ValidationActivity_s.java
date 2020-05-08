 package com.opt.mobipag.gui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 import android.widget.AdapterView.OnItemSelectedListener;
 import com.opt.mobipag.R;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import com.opt.mobipag.data.*;
 import com.opt.mobipag.database.*;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.*;
 
 public class ValidationActivity extends Activity {
     private Context c;
     private AlertDialog m_Alerter;
     private final SignatureDataSource datasource = new SignatureDataSource(this);
     private final OccasionalDataSource datasource2 = new OccasionalDataSource(this);
     private final UserDataSource datasource3 = new UserDataSource(this);
     private final TicketDataSource datasource4 = new TicketDataSource(this);
     private final UtilsDataSource datasource5 = new UtilsDataSource(this);
     private final StopDataSource datasource6 = new StopDataSource(this);
     private final LineDataSource datasource7 = new LineDataSource(this);
     private final ZoneDataSource datasource8 = new ZoneDataSource(this);
     private User user;
     private String title;
     private String lastStop;
     private int time;
     private int titleid = -1;
     private int stopid = -1;
     private int lineid = -1;
     private int seq = -1;
     private String data = null;
     private boolean withSig = true;
     private final ArrayList<String> titulosWithSig = new ArrayList<String>();
     private final ArrayList<String> titulosWithoutSig = new ArrayList<String>();
     private List<Occasional> occasionals;
     private Occasional oc;
     private String email;
     private final ArrayList<Stop> stops = new ArrayList<Stop>();
     private final ArrayList<String> paragens = new ArrayList<String>();
     private final ArrayList<String> paragens2 = new ArrayList<String>();
     private final ArrayList<String> linhas_names = new ArrayList<String>();
     private final ArrayList<Line> linhas = new ArrayList<Line>();
     private ProgressDialog dialog;
     private Location l;
     private MySpinner spStops;
     private MySpinner spLinhas;
     private Spinner spZones;
     private ArrayAdapter<String> adapter;
     private ArrayAdapter<String> adapterLinhas;
     private ArrayAdapter<String> adapter2;
     private ArrayAdapter<String> adapter3;
     private String paragem;
     private String linha = null;
     private Signature s;
     private GPSTracker gpstracker;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.validation);
         ActionBar header = (ActionBar) findViewById(R.id.header);
         header.initHeader();
         header.helpButton.setOnClickListener(new ImageButton.OnClickListener() {
             public void onClick(View view) {
                 Intent i = new Intent(view.getContext(), HelpActivity.class);
                 i.putExtra("HELP", 2);
                 startActivity(i);
             }
         });
 
         c = this;
 
         LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         List<String> providers = lm.getProviders(true);
 
         if (providers.size() == 1) {
             setResult(3);
             finish();
         }
 
         gpstracker = new GPSTracker(this);
         l = gpstracker.getLocation();
 
         dialog = ProgressDialog.show(this, "", getText(R.string.loading), true);
         dialog.setCancelable(true);
         dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
             public void onCancel(DialogInterface dialog) {
                 finish();
             }
         });
 
         email = getIntent().getStringExtra("USER_EMAIL");
 
         datasource4.open();
         datasource4.deleteAll();
         datasource4.close();
         new GetTickets().execute(email);
 
         spStops = (MySpinner) findViewById(R.id.sp_estacaoEntrada);
         adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, paragens);
         adapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
         spStops.setAdapter(adapter);
         spStops.setOnItemSelectedListener(new OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                 stopid = position;
                 if (paragens.get(position) != null) {
                     if (position < paragens2.size())
                         paragem = paragens2.get(position);
                     if (position == paragens2.size()) {
                         linhas.clear();
                         linhas_names.clear();
                         spLinhas.setEnabled(false);
                         adapterLinhas.notifyDataSetChanged();
                         Intent myIntent = new Intent(parentView.getContext(), ManualStopSelector.class);
                         startActivityForResult(myIntent, 0);
                         lineid = -1;
                     } else {
                         dialog.show();
                         new RetrieveLines().execute(paragens2.get(position));
                         spLinhas.setAdapter(adapterLinhas);
                     }
                 } else
                     stopid = -1;
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parentView) {
             }
         });
 
         spLinhas = (MySpinner) findViewById(R.id.sp_Lines);
         adapterLinhas = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, linhas_names);
         adapterLinhas.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
         spLinhas.setAdapter(adapterLinhas);
         spLinhas.setEnabled(false);
         spLinhas.setOnItemSelectedListener(new OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                 lineid = position;
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parentView) {
             }
 
         });
 
         datasource3.open();
         user = datasource3.getUserByEmail(email);
         datasource3.close();
 
 
         adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titulosWithSig);
         adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titulosWithoutSig);
         adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spZones = (Spinner) findViewById(R.id.sp_Titulo);
         spZones.setAdapter(adapter2);
         spZones.setOnItemSelectedListener(new OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                 updateFields(s, position);
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parentView) {
             }
         });
 
         m_Alerter = new AlertDialog.Builder(this).create();
         m_Alerter.setButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 finish();
             }
         });
 
         ImageButton b_Reload = (ImageButton) findViewById(R.id.reload);
         //			int h = spStops.getHeight();
         //			b_Reload.setLayoutParams(new LayoutParams(h, h));
         b_Reload.setOnClickListener(new ImageButton.OnClickListener() {
             public void onClick(View view) {
                 Context c = view.getContext();
                 if (c != null) {
                     dialog = ProgressDialog.show(c, "", getText(R.string.loading), true);
                     dialog.setCancelable(true);
                     dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                         public void onCancel(DialogInterface dialog) {
                             finish();
                         }
                     });
                     l = gpstracker.getLocation();
                     new RetrieveStops().execute();
                 }
             }
         });
 
         Button b_Validate = (Button) findViewById(R.id.b_ValidateTrip);
         b_Validate.setOnClickListener(new Button.OnClickListener() {
             public void onClick(View view) {
                 if(!spZones.isEnabled()){
                     Toast.makeText(c, getText(R.string.noTitleBesidesSig), Toast.LENGTH_LONG).show();
                 }
                 else if (stopid != -1 && lineid != -1) {
                     gpstracker.stopUsingGPS();
                     dialog.setCancelable(false);
                     dialog.show();
                     c = view.getContext();
                     String[] ss = title.split(" ");
                     String ticketType = null, ticketName = null;
 
                     if (!title.contains(getText(R.string.assinatura))) {
                         if (ss.length == 3) {
                             ticketType = ss[0] + ss[1];
                             ticketName = ss[2];
                         } else if (ss.length == 2) {
                             ticketType = ss[0];
                             ticketName = ss[1];
                         }
                     } else {
                         ticketType = ss[0];
                         ticketName = "Z" + (ss.length - 1);
                     }
 
                     String newVal = "true";
                     if (oc != null && titleid == oc.getId())
                         newVal = "false";
                     new Validate().execute(email, ticketType, ticketName, linhas.get(lineid).getPathcode(), paragens2.get(stopid), newVal);
                 } else {
                     Toast.makeText(c, getText(R.string.selectStopLine), Toast.LENGTH_SHORT).show();
                 }
             }
         });
 
     }
 
     public void onDestroy(){
         gpstracker.stopUsingGPS();
         super.onDestroy();
     }
 
     private void updateFields(final Signature s, int position) {
         if (oc != null && position == 0) {
             data = oc.getFirstval();
             titleid = oc.getId();
             time = oc.getTempoviagem();
             if (time == 24 * 60)
                 title = getText(R.string.andante24_) + oc.getDetails();
             else
                 title = getText(R.string.ocasional_) + oc.getDetails();
         } else {
             data = null;
             if (withSig)
                 title = titulosWithSig.get(position);
             else
                 title = titulosWithoutSig.get(position);
             if (title.contains(getText(R.string.assinatura))) {
                 titleid = s.getId();
                 datasource5.open();
                 time = datasource5.getTravelTimeByTypology("Z" + s.getZonas().size());
                 datasource5.close();
             } else if (title.contains(getText(R.string.ocasional_))) {
                 String[] ss = title.split(" ");
                 for (Occasional o : occasionals)
                     if (o.getNumzonas() == Integer.parseInt(ss[1].substring(1))) {
                         titleid = o.getId();
                         title = ss[0] + " " + ss[1];
                         time = o.getTempoviagem();
                         break;
                     }
             } else if (title.contains(getText(R.string.andante24_))) {
                 String[] ss = title.split(" ");
                 for (Occasional o : occasionals)
                     if (o.getTempoviagem() == 24 * 60 && o.getNumzonas() == Integer.parseInt(ss[2].substring(1))) {
                         titleid = o.getId();
                         title = ss[0] + " " + ss[1] + " " + ss[2];
                         time = o.getTempoviagem();
                         break;
                     }
             }
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (resultCode) {
             case 1:
                 String s = data.getAction();
                 assert s != null;
                 String[] ss = s.split("%%");
                 paragem = ss[0];
                 linha = ss[1];
 
                 dialog.show();
                 new RetrieveStopsByWord().execute(paragem);
                 break;
         }
     }
 
     class GetTickets extends AsyncTask<String, Integer, Boolean> {
         @Override
         protected Boolean doInBackground(String... arg0) {
 
             boolean result = false;
 
             JSONObject serviceResult = null;
             try {
                 serviceResult = WebServiceHandler.RequestGET(getText(R.string.SERVERMP)+getText(R.string.GETTICKETSURL).toString() + "?email=" + URLEncoder.encode(arg0[0], "UTF-8") + "&publicKey=" + "temp");
             } catch (UnsupportedEncodingException e1) {
                 e1.printStackTrace();
             }
 
             if (serviceResult != null) {
                 int code = 0;
                 try {
                     code = (Integer) serviceResult.get("Code");
                 } catch (JSONException e1) {
                     e1.printStackTrace();
                 }
                 switch (code) {
                     case 4100:
                         JSONArray o2 = null;
                         try {
                             o2 = serviceResult.getJSONArray("ListTickets");
                         } catch (JSONException e) {
                             e.printStackTrace();
                         }
 
                         assert o2 != null;
                         for (int i = 0; i < o2.length(); i++) {
                             int amount = 0;
                             String name = null;
                             String type = "";
                             JSONObject j = null;
                             try {
                                 j = o2.getJSONObject(i);
                                 amount = j.getInt("Amount");
                                 name = j.getString("Name");
                                 type = j.getString("Type");
                             } catch (JSONException e) {
                                 e.printStackTrace();
                             }
                             if (type.equals("Assinatura")) {
                                 createSignature(j);
                             } else if (type.equals("Andante24")) {
                                 createAndante24(amount, name);
                             } else {
                                 createOccasional(amount, name);
                             }
                         }
                         result = getValidacoes(result, arg0[0]);
                         break;
                     case 4102:
                         return false;
                 }
             }
             return result;
         }
 
         private boolean getValidacoes(boolean result, String s) {
             try {
                 JSONObject serviceResult2 = WebServiceHandler.RequestGET(getText(R.string.SERVERMP)+getText(R.string.GETVALHISTORYURL).toString() + "?email=" + URLEncoder.encode(s, "UTF-8") + "&publicKey=" + "temp");
 
                 if (serviceResult2 != null) {
                     int code2 = (Integer) serviceResult2.get("Code");
 
                     switch (code2) {
                         case 4150:
                             JSONArray o2 = serviceResult2.getJSONArray("ListAccountValidations");
 
                             Occasional o = null;
                             for (int i = 0; i < o2.length(); i++) {
                                 JSONObject j = o2.getJSONObject(i);
 
                                 Boolean signature = j.getBoolean("Signature");
 
                                 String date = j.getString("TimeStamp");
                                 long mili = Utils.dateFromJSON(date);
                                 date = Utils.parseDate(new Date(mili), true);
 
                                 if (signature)
                                     break;
                                 else {
                                     String PathCode = j.getString("PathCode");
                                     String StopCode = j.getString("StopCode");
                                     String ticketType = j.getString("TicketType");
                                     String ticketName = j.getString("TicketName");
                                     Boolean isNewVal = j.getBoolean("isNewValidation");
                                     int seqId = j.getInt("Id");
 
                                     if (i == 0) {
                                         datasource5.open();
                                         if (ticketType.equals("Ocasional")) {
                                             o = new Occasional(-1, ticketName, datasource5.getPriceByTypologyAndType(ticketName, datasource5.PRICE_OCCASIONAL), datasource5.getNumZonesByTypology(ticketName), datasource5.getTravelTimeByTypology(ticketName), new ArrayList<Validation>(), null);
                                         } else if (ticketType.equals("Andante24")) {
                                             o = new Occasional(-1, ticketName, datasource5.getPriceByTypologyAndType(ticketName, datasource5.PRICE_OCCASIONAL), datasource5.getNumZonesByTypology(ticketName), 24 * 60, new ArrayList<Validation>(), null);
                                         }
                                         datasource5.close();
                                     }
                                     long sid = -1;
                                     long lid = -1;
 
                                     sid = addStop(StopCode, sid);
                                     lid = addLine(PathCode, StopCode, lid);
 
                                     if (sid != -1 && lid != -1) {
                                         assert o != null;
                                         o.getValidacoes().add(new Validation((int) sid, (int) lid, date, seqId));
 
                                         if (isNewVal) {
                                             o.setFirstval(date);
 
                                             if (Utils.checkValidity(o)) {
                                                 datasource3.open();
                                                 User user = datasource3.getUserByEmail(email);
                                                 datasource3.close();
                                                 datasource2.open();
                                                 long tId = datasource2.createOccasional(o.getDetails(), o.getPrice(), 2, o.getNumzonas(), o.getTempoviagem(), user.getId());
                                                 datasource2.close();
                                                 datasource4.open();
                                                 for (int k = o.getValidacoes().size() - 1; k >= 0; k--) {
                                                     datasource4.addValidation((int) tId, o.getValidacoes().get(k).getIdStop(), o.getValidacoes().get(k).getIdLine(), o.getValidacoes().get(k).getSeqId(), o.getValidacoes().get(k).getDate());
                                                 }
                                                 datasource4.changeTicketStatusById((int) tId, 3, -1, -1, -1);
                                                 datasource4.close();
 
                                                 Intent i1 = new Intent(c, TravelTimer.class);
                                                 i1.putExtra("USER_EMAIL", email);
                                                 if (o.getTempoviagem() == 24 * 60)
                                                     i1.putExtra("title", "Andante 24 " + o.getDetails());
                                                 else
                                                     i1.putExtra("title", "Ocasional " + o.getDetails());
                                                 i1.putExtra("titleid", 1);
                                                 i1.putExtra("time", o.getTempoviagem());
                                                 i1.putExtra("date", o.getFirstval());
                                                 startService(i1);
                                             }
                                             break;
 
                                         }
                                     }
                                 }
                             }
                             result = true;
                             break;
                         default:
                             result = false;
                             break;
                     }
 
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
             return result;
         }
 
         private void createSignature(JSONObject j) {
             int month = 0;
             try {
                 month = j.getInt("SigMonth");
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             int year = 0;
             try {
                 year = j.getInt("SigYear");
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             String zones = null;
             try {
                 zones = j.getString("SigZones");
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             assert zones != null;
             String[] ss = zones.split(", ");
             List<Zone> zonas = new ArrayList<Zone>();
             datasource8.open();
             for (String s : ss)
                 zonas.add(datasource8.getZoneByDescriptor(s));
             datasource8.close();
             datasource.open();
             datasource2.open();
             datasource3.open();
             datasource5.open();
             datasource.createSignature(year + "/" + month, datasource5.getPriceByTypologyAndType("Z" + ss.length, datasource5.PRICE_SIGNATURE), 1, datasource5.getTravelTimeByTypology("Z" + ss.length), zonas, datasource3.getUserByEmail(email).getId());
             datasource.close();
             datasource2.close();
             datasource3.close();
             datasource5.close();
         }
 
         private void createOccasional(int amount, String name) {
             datasource.open();
             datasource2.open();
             datasource3.open();
             datasource5.open();
             for (int k = 0; k < amount; k++)
                 datasource2.createOccasional(name, datasource5.getPriceByTypologyAndType(name, datasource5.PRICE_OCCASIONAL), 0, datasource5.getNumZonesByTypology(name), datasource5.getTravelTimeByTypology(name), datasource3.getUserByEmail(email).getId());
             datasource.close();
             datasource2.close();
             datasource3.close();
             datasource5.close();
         }
 
         private void createAndante24(int amount, String name) {
             datasource.open();
             datasource2.open();
             datasource3.open();
             datasource5.open();
             for (int k = 0; k < amount; k++)
                 datasource2.createOccasional(name, datasource5.getPriceByTypologyAndType(name, datasource5.PRICE_ANDANTE24), 0, datasource5.getNumZonesByTypology(name), 24 * 60, datasource3.getUserByEmail(email).getId());
             datasource.close();
             datasource2.close();
             datasource3.close();
             datasource5.close();
         }
 
         // Called once the background activity has completed
         @Override
         protected void onPostExecute(Boolean result) {
             if (result) {
                 datasource.open();
                 s = datasource.getSignature(user.getId(), true);
                 datasource.close();
                 datasource2.open();
                 oc = datasource2.getActiveOccasional(user.getId());
                 if (oc != null)
                     if (oc.getTempoviagem() == 24 * 60) {
                         titulosWithSig.add(getText(R.string.andante24_) + oc.getDetails() + getText(R.string.ativo));
                         titulosWithoutSig.add(getText(R.string.andante24_) + oc.getDetails() + getText(R.string.ativo));
                     } else {
                         titulosWithSig.add(getText(R.string.ocasional_) + oc.getDetails() + getText(R.string.ativo));
                         titulosWithoutSig.add(getText(R.string.ocasional_) + oc.getDetails() + getText(R.string.ativo));
                     }
                 if (s != null)
                     titulosWithSig.add(getText(R.string.assinatura) + "(" + s.getListZonas() + ")");
 
 
                 int[] ocasional = new int[13];
                 Arrays.fill(ocasional, 0);
                 int[] andante24 = new int[13];
                 Arrays.fill(andante24, 0);
                 occasionals = datasource2.getAllAvailableOccasionals(user.getId());
                 datasource2.close();
                 for (Occasional o : occasionals)
                     if (o.getTempoviagem() == 24 * 60)
                         andante24[o.getNumzonas()]++;
                     else
                         ocasional[o.getNumzonas()]++;
 
                 for (int i = 2; i < 13; i++)
                     if (ocasional[i] > 1) {
                         titulosWithSig.add(getText(R.string.ocasional).toString() + i + getText(R.string.saldo_) + ocasional[i] + getText(R.string.titulos) + ")");
                         titulosWithoutSig.add(getText(R.string.ocasional).toString() + i + getText(R.string.saldo_) + ocasional[i] + getText(R.string.titulos) + ")");
                     } else if (ocasional[i] > 0) {
                         titulosWithSig.add(getText(R.string.ocasional).toString() + i + getText(R.string.saldo_) + ocasional[i] + getText(R.string.titulo) + ")");
                         titulosWithoutSig.add(getText(R.string.ocasional).toString() + i + getText(R.string.saldo_) + ocasional[i] + getText(R.string.titulo) + ")");
                     }
                 for (int i = 2; i < 13; i++)
                     if (andante24[i] > 1) {
                         titulosWithSig.add(getText(R.string.andante24).toString() + i + getText(R.string.saldo_) + andante24[i] + getText(R.string.titulos) + ")");
                         titulosWithoutSig.add(getText(R.string.andante24).toString() + i + getText(R.string.saldo_) + andante24[i] + getText(R.string.titulos) + ")");
                     } else if (andante24[i] > 0) {
                         titulosWithSig.add(getText(R.string.andante24).toString() + i + getText(R.string.saldo_) + andante24[i] + getText(R.string.titulo) + ")");
                         titulosWithoutSig.add(getText(R.string.andante24).toString() + i + getText(R.string.saldo_) + andante24[i] + getText(R.string.titulo) + ")");
                     }
 
                 if (titulosWithSig.size() == 0) {
                     Toast.makeText(c, getText(R.string.sem_titulos), Toast.LENGTH_SHORT).show();
                     dialog.dismiss();
                     finish();
                 } else {
                     adapter2.notifyDataSetChanged();
                     new RetrieveStops().execute();
                     updateFields(s, 0);
                 }
             }
             else{
                 Toast.makeText(c, getText(R.string.verifyConnection), Toast.LENGTH_SHORT).show();
                 dialog.dismiss();
                 finish();
             }
         }
 
         private long addStop(String StopCode, long sid) {
             datasource6.open();
             Stop stop = datasource6.getStopByCodsms(StopCode);
             if (stop != null)
                 sid = stop.getId();
             else {
                 JSONArray serviceResult5 = WebServiceHandler.RequestGETArray(getText(R.string.SERVER).toString()+getText(R.string.STOPWORDURL) + "?word=" + StopCode + "&username=MOBIPAG");
 
                 if (serviceResult5 != null) {
                     JSONObject json_stop = null;
                     try {
                         json_stop = serviceResult5.getJSONObject(0);
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                     assert json_stop != null;
                     try {
                         sid = datasource6.createStop(json_stop.getString("name"), json_stop.getString("code"), json_stop.getString("provider"), json_stop.getDouble("coordX"), json_stop.getDouble("coordY"));
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }
             }
             datasource6.close();
             return sid;
         }
 
         private long addLine(String PathCode, String StopCode, long lid) {
             datasource7.open();
             String[] linecode = PathCode.split("-");
             Line line = datasource7.getLineByDescriptor(linecode[0]);
             if (line != null)
                 lid = line.getId();
             else {
 
                 JSONArray serviceResult6 = WebServiceHandler.RequestGETArray(getText(R.string.SERVER).toString()+getText(R.string.LINEURL) + "?stop=" + URLEncoder.encode(StopCode) + "&username=MOBIPAG");
                 if (serviceResult6 != null)
                     for (int k = 0; k < serviceResult6.length(); k++) {
                         JSONObject json_line = null;
                         try {
                             json_line = serviceResult6.getJSONObject(k);
                         } catch (JSONException e) {
                             e.printStackTrace();
                         }
                         assert json_line != null;
                         String lc = null;
                         try {
                             lc = json_line.getString("GoPathCode");
                         } catch (JSONException e) {
                             e.printStackTrace();
                         }
                         assert lc != null;
                         if (lc.equals(PathCode))
                             try {
                                 lid = datasource7.createLine(json_line.getString("LineCode"), json_line.getString("LineName"), json_line.getString("GoPathCode"));
                             } catch (JSONException e) {
                                 e.printStackTrace();
                             }
                     }
             }
             datasource7.close();
             return lid;
         }
 
     }
 
     private class Validate extends AsyncTask<String, Integer, Boolean> {
         @Override
         protected Boolean doInBackground(String... arg0) {
             String param = null;
 
             String firstPath;
             String firstStop;
             if(arg0[5].equals("true")){
                 firstPath = arg0[3];
                 firstStop = arg0[4];
             }
             else{
                 datasource2.open();
                 Occasional o = datasource2.getActiveOccasional(user.getId());
                 datasource2.close();
 
                 datasource7.open();
                firstPath = datasource7.getLineById(o.getValidacoes().get(0).getIdLine()).getPathcode();
                 datasource7.close();
                 datasource6.open();
                firstStop = datasource6.getStopById(o.getValidacoes().get(0).getIdStop()).getCodsms();
                 datasource6.close();
             }
 
             try {
                 param = "email=" + URLEncoder.encode(arg0[0], "UTF-8") +
                         "&ticketType=" + URLEncoder.encode(arg0[1], "UTF-8") +
                         "&ticketName=" + URLEncoder.encode(arg0[2], "UTF-8") +
                         "&path=" + URLEncoder.encode(arg0[3], "UTF-8") +
                         "&stop=" + URLEncoder.encode(arg0[4], "UTF-8") +
                         "&firstPath=" + URLEncoder.encode(firstPath, "UTF-8") +
                         "&firstStop=" + URLEncoder.encode(firstStop, "UTF-8") +
                         "&IsNewValidation=" + URLEncoder.encode(arg0[5], "UTF-8") +
                         "&publicKey=" + URLEncoder.encode("temp", "UTF-8");
             } catch (UnsupportedEncodingException e1) {
                 e1.printStackTrace();
             }
 
             try {
                 //JSONObject serviceResult = WebServiceHandler.RequestPOST(getText(R.string.REGISTERURL).toString(), param);
                 JSONObject serviceResult = WebServiceHandler.RequestGET(getText(R.string.SERVERMP)+getText(R.string.VALIDATEURL).toString() + "?" + param);
 
                 boolean result = false;
                 if (serviceResult != null) {
                     int code = (Integer) serviceResult.get("Code");
                     switch (code) {
                         case 4200:
                             JSONObject j = serviceResult.getJSONObject("Validation");
                             lastStop = j.getString("lastAllowedStopName");
                             seq = j.getInt("seq");
                             result = true;
                             break;
                         case 4201:
                             m_Alerter.setMessage(getText(R.string.error_invalid_key));
                             result = false;
                             break;
                         case 4202:
                             m_Alerter.setMessage(getText(R.string.error_titulo_inexistente));
                             result = false;
                             break;
                         case 4203:
                             m_Alerter.setMessage(getText(R.string.email_inexistente));
                             result = false;
                             break;
                     }
                 } else {
                     m_Alerter.setMessage(getText(R.string.verifyConnection));
                 }
                 return result;
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             return Boolean.FALSE;
         }
 
         // Called once the background activity has completed
         @Override
         protected void onPostExecute(Boolean result) {
             if (result) {
                 Stop s = stops.get(stopid);
                 datasource6.open();
                 datasource7.open();
                 long sid = datasource6.createStop(s.getNome(), s.getCodsms(), s.getOperador(), s.getCordenadas().first, s.getCordenadas().second);
                 long lid = datasource7.createLine(linhas.get(lineid).getDescritor(), linhas.get(lineid).getName(), linhas.get(lineid).getPathcode());
 
                 datasource4.open();
                 datasource4.changeTicketStatusById(titleid, 1, sid, lid, seq);
                 datasource4.close();
                 datasource6.close();
                 datasource7.close();
                 m_Alerter.setMessage(getText(R.string.val_success) + lastStop);
 
 
                 Intent i = new Intent(c, TravelTimer.class);
                 i.putExtra("USER_EMAIL", email);
                 i.putExtra("title", title);
                 i.putExtra("titleid", 1);
                 i.putExtra("time", time);
                 if (data == null)
                     i.putExtra("date", Utils.currentDate());
                 else
                     i.putExtra("date", data);
                 startService(i);
             }
             dialog.dismiss();
             m_Alerter.show();
         }
     }
 
     private class RetrieveLines extends AsyncTask<String, Integer, String> {
         @Override
         protected String doInBackground(String... arg0) {
 
             JSONArray serviceResult = null;
             try {
                 serviceResult = WebServiceHandler.RequestGETArray(getText(R.string.SERVER)+getText(R.string.LINEURL).toString() + "?stop=" + URLEncoder.encode(arg0[0], "UTF-8") + "&username=MOBIPAG");
             } catch (UnsupportedEncodingException e1) {
                 e1.printStackTrace();
             }
 
             linhas_names.clear();
             linhas.clear();
             assert serviceResult != null;
             try {
                 for (int i = 0; i < serviceResult.length(); i++) {
                     JSONObject j = serviceResult.getJSONObject(i);
 
                     Line l = new Line(0, j.getString("LineCode"), j.getString("LineName"), j.getString("GoPathCode"));
                     linhas.add(l);
                     String name;
                     if (l.getName().contains("_")) {
                         String[] nome = l.getName().split("_|->");
                         name = nome[0] + " " + nome[1] + "\n[" + j.getString("GoTerminal") + "]";
                     } else {
                         String[] nome = l.getName().split("->");
                         name = "Linha " + nome[0] + "\n[" + j.getString("GoTerminal") + "]";
                     }
                     linhas_names.add(name);
 
                 }
 
             } catch (JSONException e) {
                 e.printStackTrace();
             } catch (Exception ee) {
                 ee.printStackTrace();
             }
             return null;
         }
 
         // Called once the background activity has completed
         @Override
         protected void onPostExecute(String result) {
             adapterLinhas.notifyDataSetChanged();
             if (!linhas_names.isEmpty()) {
                 spLinhas.setEnabled(true);
                 if (linha != null) {
                     int i = 0;
                     while (i < linhas.size() && !linhas.get(i).getDescritor().equals(linha))
                         i++;
                     spLinhas.setSelection(i);
                 }
                 if (s != null)
                     new CheckStopZone().execute(linhas.get(0).getPathcode(), paragem);
                 else
                     dialog.dismiss();
             } else
                 dialog.dismiss();
         }
     }
 
     private class RetrieveStops extends AsyncTask<String, Integer, String> {
         @Override
         protected String doInBackground(String... arg0) {
 
             if (l != null) {
                 String lat = Double.toString(l.getLatitude()).replace(".", ",");
                 String lon = Double.toString(l.getLongitude()).replace(".", ",");
 
                 JSONArray serviceResult = WebServiceHandler.RequestGETArray(getText(R.string.SERVER).toString()+getText(R.string.STOPSURL) + "?latitude=" + lat + "&longitude=" + lon + "&radius=" + getText(R.string.stop_radius) + "&username=MOBIPAG");
 
                 stops.clear();
                 paragens.clear();
                 paragens2.clear();
                 if (serviceResult != null) {
                     for (int i = 0; i < serviceResult.length(); i++) {
                         try {
                             JSONObject j = (JSONObject) serviceResult.get(i);
                             stops.add(new Stop(-1, j.getString("name"), j.getString("code"), j.getString("provider"), j.getDouble("coordX"), j.getDouble("coordY")));
                         } catch (JSONException e) {
                             e.printStackTrace();
                         }
                     }
                     Collections.sort(stops, new Comparator<Stop>() {
                         @Override
                         public int compare(Stop lhs, Stop rhs) {
                             double distx = lhs.getCordenadas().first - l.getLatitude();
                             double disty = lhs.getCordenadas().second - l.getLongitude();
                             double d1 = Math.sqrt(distx * distx + disty * disty);
                             distx = rhs.getCordenadas().first - l.getLatitude();
                             disty = rhs.getCordenadas().second - l.getLongitude();
                             double d2 = Math.sqrt(distx * distx + disty * disty);
 
                             return (int) (d1 - d2);
                         }
                     });
 
                     for (Stop s : stops) {
                         String[] cod = s.getCodsms().split("_");
                         String name = s.getNome();
                         if (cod.length > 1)
                             name += " [" + cod[1] + "]";
                         name += "\n" + s.getOperador();
                         paragens.add(name);
                         paragens2.add(s.getCodsms());
                     }
                 }
                 paragens.add(getText(R.string.other).toString());
             }
             else{
                 stops.clear();
                 paragens.clear();
                 paragens2.clear();
                 paragens.add(getText(R.string.other).toString());
             }
             return null;
         }
 
         // Called once the background activity has completed
         @Override
         protected void onPostExecute(String result) {
             adapter.notifyDataSetChanged();
             if (!paragens2.isEmpty()) {
                 paragem = paragens2.get(0);
                 new RetrieveLines().execute(paragens2.get(0));
             } else {
                 linhas_names.clear();
                 linhas.clear();
                 lineid = -1;
                 spLinhas.setEnabled(false);
                 adapterLinhas.notifyDataSetChanged();
                 dialog.dismiss();
             }
         }
     }
 
     private class RetrieveStopsByWord extends AsyncTask<String, Integer, String> {
         @Override
         protected String doInBackground(String... arg0) {
 
             JSONArray serviceResult = null;
             try {
                 serviceResult = WebServiceHandler.RequestGETArray(getText(R.string.SERVER).toString()+getText(R.string.STOPWORDURL) + "?word=" + URLEncoder.encode(arg0[0], "UTF-8") + "&username=MOBIPAG");
             } catch (UnsupportedEncodingException e1) {
                 e1.printStackTrace();
             }
 
             stops.clear();
             paragens.clear();
             paragens2.clear();
             if (serviceResult != null) {
                 for (int i = 0; i < serviceResult.length(); i++) {
                     try {
                         JSONObject j = (JSONObject) serviceResult.get(i);
                         stops.add(new Stop(-1, j.getString("name"), j.getString("code"), j.getString("provider"), j.getDouble("coordX"), j.getDouble("coordY")));
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }
 
                 for (Stop s : stops) {
                     String[] cod = s.getCodsms().split("_");
                     String name = s.getNome();
                     if (cod.length > 1)
                         name += " [" + cod[1] + "]";
                     name += "\n" + s.getOperador();
                     paragens.add(name);
                     paragens2.add(s.getCodsms());
                 }
             }
             return null;
         }
 
         // Called once the background activity has completed
         @Override
         protected void onPostExecute(String result) {
             adapter.notifyDataSetChanged();
             if (!paragens2.isEmpty()) {
                 spStops.setSelection(0);
                 paragem = paragens2.get(0);
                 new RetrieveLines().execute(paragem);
             } else
                 dialog.dismiss();
         }
     }
 
     private class CheckStopZone extends AsyncTask<String, Integer, String> {
         @Override
         protected String doInBackground(String... arg0) {
 
             JSONObject serviceResult = null;
             try {
                 serviceResult = WebServiceHandler.RequestGET(getText(R.string.SERVERMP).toString()+getText(R.string.STOPZONEURL) + "?pathCode=" + URLEncoder.encode(arg0[0], "UTF-8") + "&stopCode=" + URLEncoder.encode(arg0[1], "UTF-8"));
             } catch (UnsupportedEncodingException e1) {
                 e1.printStackTrace();
             }
 
             if (serviceResult != null) {
                 try {
                     int code = serviceResult.getInt("Code");
                     switch (code){
                         case 1050:
                             String zona = serviceResult.getString("CodedMessage");
                             zona = zona.substring(0, zona.length() - 1);
                             withSig = s.getListZonas().contains(zona);
                             break;
                         case 1051:
                             withSig = false;
                             break;
                     }
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
             return null;
         }
 
         // Called once the background activity has completed
         @Override
         protected void onPostExecute(String result) {
             if (withSig) {
                 spZones.setAdapter(adapter2);
                 spZones.setEnabled(true);
             } else{
                 spZones.setAdapter(adapter3);
                 if(adapter3.isEmpty())
                     spZones.setEnabled(false);
                 else
                     spZones.setEnabled(true);
             }
             dialog.dismiss();
         }
     }
 }
