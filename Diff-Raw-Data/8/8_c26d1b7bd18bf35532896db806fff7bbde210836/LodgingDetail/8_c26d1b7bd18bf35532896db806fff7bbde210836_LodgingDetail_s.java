 
 package es.juvecyl.app;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.location.Address;
 import android.location.Geocoder;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import es.juvecyl.app.navs.DetailsNav;
 import es.juvecyl.app.navs.DetailsNavMaker;
 import es.juvecyl.app.utils.ImageDownloader;
 import es.juvecyl.app.utils.Lodging;
 import es.juvecyl.app.utils.LodgingSingleton;
 import es.juvecyl.app.utils.ProvinceSingleton;
 import es.juvecyl.app.utils.XMLDB;
 
 @SuppressLint("InflateParams")
 public class LodgingDetail extends SherlockActivity {
     private float scale;
     private XMLDB db;
     private HashSet<String> favs;
     private boolean isImageFitToScreen;
     private Resources res;
     private Lodging target;
     private LinearLayout detailsContainer, linearInsideScroll;
     private TextView title, detailsTarget;
     private ImageView image;
     private ListView navList;
     private DetailsNavMaker arrayAdapter;
     private Vibrator vibe;
     private DrawerLayout drawerLayout;
     private String targetProvince;
     private ScrollView scrollMain;
     private View gMaps;
     private GoogleMap map;
     // ESCUCHADOR DEL MENU NAV
     private ArrayList<DetailsNav> navdata = new ArrayList<DetailsNav>();
     private ActionBarDrawerToggle navToggle;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
         setContentView(R.layout.details);
         res = getResources();
         // /////////////////////////////////////////////////////////////////////
         // CARGAMOS LOS DATOS
         // /////////////////////////////////////////////////////////////////////
         db = LodgingSingleton.getInstance(getApplicationContext()).getDB();
         // /////////////////////////////////////////////////////////////////////
         // PARA QUE LOS PIXELS SEAN EN FUNCION DE LA DENSIDAD DE LA PANTALLA
         // /////////////////////////////////////////////////////////////////////
         scale = getResources().getDisplayMetrics().density;
         // /////////////////////////////////////////////////////////////////////
         // CARGAMOS EL ALOJAMIENTO
         // /////////////////////////////////////////////////////////////////////
         Bundle bundle = this.getIntent().getExtras();
         targetProvince = bundle.getString("lodging");
         LocateLodging(Integer.parseInt(targetProvince));
         navList = (ListView) findViewById(R.id.main_menu_drawer);
         // /////////////////////////////////////////////////////////////////////
         // DECLARAMOS EL VIBRADOR
         // /////////////////////////////////////////////////////////////////////
         vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         // /////////////////////////////////////////////////////////////////////
         // RECOGEMOS OBJETOS DEL LAYOUT
         // /////////////////////////////////////////////////////////////////////
         drawerLayout = (DrawerLayout) findViewById(R.id.details_drawer_layout);
         detailsContainer = (LinearLayout) findViewById(R.id.details_container);
         gMaps = LayoutInflater.from(this).inflate(
                 R.layout.map, null);
         map = (
                 (MapFragment) getFragmentManager().
                         findFragmentById(R.id.map)).getMap();
         map.setMyLocationEnabled(true);
         title = (TextView) findViewById(R.id.details_title);
         image = (ImageView) findViewById(R.id.details_image);
         // /////////////////////////////////////////////////////////////////////
         // LLAMADA PARA EL MENÚ SUPERIOR
         // /////////////////////////////////////////////////////////////////////
         topMenuMaker();
         // /////////////////////////////////////////////////////////////////////
         // LLAMADA PARA EL MENÚ LATERAL
         // /////////////////////////////////////////////////////////////////////
         navListMaker();
         // /////////////////////////////////////////////////////////////////////
         // PINTAMOS DATOS BÁSICOS – IMAGEN Y TÍTULO
         // /////////////////////////////////////////////////////////////////////
         isImageFitToScreen = false;
         new ImageDownloader(LodgingDetail.this).execute(target.getImage());
         title.setText(target.getTitle());
         title.setTextColor(res.getColor(R.color.white));
         title.setPadding((int) (10 * scale + 0.5f),
                 (int) (20 * scale + 0.5f),
                 (int) (10 * scale + 0.5f),
                 (int) (20 * scale + 0.5f));
         Log.d("provincia", target.getProvince());
         title.setBackgroundColor(
                 res.getColor(ProvinceSingleton.
                         getInstance().
                         getProvince(target.getProvince()).
                         getColor())
                 );
         printDetails("desc");
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         navToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         navToggle.onConfigurationChanged(newConfig);
     }
 
     // /////////////////////////////////////////////////////////////////////////
     //
     //
     // FUNCIONES DE CLASE
     //
     //
     // /////////////////////////////////////////////////////////////////////////
     private void LocateLodging(int id) {
         target = db.getLodgings().get(id);
     }
 
     @SuppressLint("NewApi")
     private void printContactInfo() {
         detailsContainer.removeAllViews();
         DisplayMetrics dm = new DisplayMetrics();
         this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
         int width = dm.widthPixels;
         //
         // DECLARACIÓN DEL CONTENEDOR CON SCROLL
         //
         scrollMain = new ScrollView(getApplicationContext());
         RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         scrollParams.setMargins(0, (int) (10 * scale + 0.5f), 0, 0);
         scrollParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                 RelativeLayout.TRUE);
         scrollMain.setLayoutParams(scrollParams);
         //
         // DECLARACIÓN DEL CONTENEDOR DENTRO DEL SCROLL
         //
         linearInsideScroll = new LinearLayout(getApplicationContext());
         LinearLayout.LayoutParams linearInsideParams = new LinearLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
         linearInsideScroll.setOrientation(LinearLayout.VERTICAL);
         linearInsideScroll.setLayoutParams(linearInsideParams);
         // /////////////////////////////////////////////////////////////////////////
         //
         // ZONA DE NÚMEROS
         //
         // /////////////////////////////////////////////////////////////////////////
         //
         // TÍTULO
         //
         LinearLayout.LayoutParams numberLayout = new LinearLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT,
                 LayoutParams.WRAP_CONTENT);
         numberLayout.setMargins(
                 0,
                 (int) (5 * scale + 0.5f),
                 0,
                 (int) (5 * scale + 0.5f));
         TextView numbersTitle = new TextView(getApplicationContext());
         numbersTitle.setGravity(Gravity.CENTER);
         numbersTitle.setLayoutParams(numberLayout);
         numbersTitle.setText(res.getText(R.string.coding_phones));
         numbersTitle.setTextSize(10 * scale + 0.5f);
         numbersTitle.setTextColor(res.getColor(R.color.pure_black));
         numbersTitle.setPadding(
                 (int) (5 * scale + 0.5f),
                 (int) (15 * scale + 0.5f),
                 (int) (5 * scale + 0.5f),
                 (int) (15 * scale + 0.5f));
         //
         // AÑADIENDO EL TÍTULO
         //
         linearInsideScroll.addView(numbersTitle);
         //
         // NÚMEROS
         //
         LinearLayout container;
         TextView[] phonesOutput = new TextView[target.getPhone().size()];
         ImageButton[] phonesBtn = new ImageButton[target.getPhone().size()];
         for (int i = 0; i < target.getPhone().size(); i++) {
             container = new LinearLayout(getApplicationContext());
             container.setOrientation(LinearLayout.HORIZONTAL);
             phonesOutput[i] = new TextView(getApplicationContext());
             phonesBtn[i] = new ImageButton(getApplicationContext());
             phonesOutput[i].setText(target.getPhone().get(i));
             LinearLayout.LayoutParams prmX = new LinearLayout.LayoutParams(
                     (int) (width * 0.7f),
                     LayoutParams.WRAP_CONTENT);
             prmX.setMargins(0, (int) (5 * scale + 0.5f), 0,
                     (int) (5 * scale + 0.5f));
             phonesOutput[i].setLayoutParams(prmX);
             phonesOutput[i].setTextColor(res.getColor(R.color.pure_black));
             phonesOutput[i].setPadding(
                     (int) (5 * scale + 0.5f),
                     (int) (10 * scale + 0.5f),
                     (int) (5 * scale + 0.5f),
                     (int) (10 * scale + 0.5f));
             container.addView(phonesOutput[i]);
             phonesBtn[i].setTag(target.getPhone().get(i));
             RelativeLayout.LayoutParams shareParams = new RelativeLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
             shareParams.addRule(
                     RelativeLayout.CENTER_HORIZONTAL, phonesOutput[i].getId());
             shareParams.width = (int) (50 * scale + 0.5f);
             shareParams.height = (int) (50 * scale + 0.5f);
             shareParams.setMargins(
                     (int) (5 * scale + 0.5f),
                     (int) (10 * scale + 0.5f),
                     (int) (5 * scale + 0.5f),
                     (int) (10 * scale + 0.5f));
             phonesBtn[i].setLayoutParams(shareParams);
             phonesBtn[i].setBackgroundResource(R.drawable.tfno_btn);
             phonesBtn[i].setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     try {
                         String number = (String) v.getTag();
                         Intent callIntent = new Intent(Intent.ACTION_DIAL);
                         callIntent.setData(Uri.parse("tel:" + number));
                         startActivity(callIntent);
                     } catch (ActivityNotFoundException activityException) {
                         Toast.makeText(
                                 getApplicationContext(),
                                 res.getString(R.string.coding_call_fail),
                                 Toast.LENGTH_LONG).show();
                     }
                 }
             });
             container.addView(phonesBtn[i]);
             linearInsideScroll.addView(container);
         }
         // /////////////////////////////////////////////////////////////////////////
         //
         // ZONA DE EMAILS
         //
         // /////////////////////////////////////////////////////////////////////////
         //
         // TÍTULO
         //
         LinearLayout.LayoutParams mailLayout = new LinearLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT,
                 LayoutParams.WRAP_CONTENT);
         mailLayout.setMargins(
                 0,
                 (int) (5 * scale + 0.5f),
                 0,
                 (int) (5 * scale + 0.5f));
         TextView mailsTitle = new TextView(getApplicationContext());
         mailsTitle.setGravity(Gravity.CENTER);
         mailsTitle.setLayoutParams(mailLayout);
         mailsTitle.setText(res.getText(R.string.coding_mails));
         mailsTitle.setTextSize(10 * scale + 0.5f);
         mailsTitle.setTextColor(res.getColor(R.color.pure_black));
         mailsTitle.setPadding(
                 (int) (5 * scale + 0.5f),
                 (int) (15 * scale + 0.5f),
                 (int) (5 * scale + 0.5f),
                 (int) (15 * scale + 0.5f));
         //
         // AÑADIENDO EL TÍTULO
         //
         linearInsideScroll.addView(mailsTitle);
         //
         // MAILS
         //
         TextView[] mailsOutput = new TextView[target.getEmail().size()];
         ImageButton[] mailsBtn = new ImageButton[target.getEmail().size()];
         for (int i = 0; i < target.getEmail().size(); i++) {
             container = new LinearLayout(getApplicationContext());
             container.setOrientation(LinearLayout.HORIZONTAL);
             mailsOutput[i] = new TextView(getApplicationContext());
             mailsBtn[i] = new ImageButton(getApplicationContext());
             mailsOutput[i].setText(target.getEmail().get(i));
             LinearLayout.LayoutParams prmX = new LinearLayout.LayoutParams(
                     (int) (width * 0.7f),
                     LayoutParams.WRAP_CONTENT);
             prmX.setMargins(0, (int) (5 * scale + 0.5f), 0,
                     (int) (5 * scale + 0.5f));
             mailsOutput[i].setLayoutParams(prmX);
             mailsOutput[i].setTextColor(res.getColor(R.color.pure_black));
             mailsOutput[i].setPadding(
                     (int) (5 * scale + 0.5f),
                     (int) (10 * scale + 0.5f),
                     (int) (5 * scale + 0.5f),
                     (int) (10 * scale + 0.5f));
             container.addView(mailsOutput[i]);
             mailsBtn[i].setTag(target.getEmail().get(i));
             RelativeLayout.LayoutParams shareParams = new RelativeLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
             shareParams.addRule(
                     RelativeLayout.ALIGN_RIGHT, mailsOutput[i].getId());
             shareParams.width = (int) (50 * scale + 0.5f);
             shareParams.height = (int) (50 * scale + 0.5f);
             mailsBtn[i].setLayoutParams(shareParams);
             mailsBtn[i].setBackgroundResource(R.drawable.email_btn);
             mailsBtn[i].setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     String mail = (String) v.getTag();
                     Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                     mailIntent.setData(Uri.fromParts(
                             "mailto",
                             mail,
                             null));
                     mailIntent.putExtra(
                             Intent.EXTRA_SUBJECT,
                             res.getString(R.string.coding_send_mail));
                     startActivity(mailIntent);
                 }
             });
             container.addView(mailsBtn[i]);
             linearInsideScroll.addView(container);
         }
         // /////////////////////////////////////////////////////////////////////////
         //
         // ZONA DE WEBS
         //
         // /////////////////////////////////////////////////////////////////////////
         //
         // TÍTULO
         //
         LinearLayout.LayoutParams webLayout = new LinearLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT,
                 LayoutParams.WRAP_CONTENT);
         webLayout.setMargins(
                 0,
                 (int) (5 * scale + 0.5f),
                 0,
                 (int) (5 * scale + 0.5f));
         TextView websTitle = new TextView(getApplicationContext());
         websTitle.setLayoutParams(webLayout);
         websTitle.setGravity(Gravity.CENTER);
         websTitle.setText(res.getText(R.string.coding_webs));
         websTitle.setTextSize(10 * scale + 0.5f);
         websTitle.setTextColor(res.getColor(R.color.pure_black));
         websTitle.setPadding(
                 (int) (5 * scale + 0.5f),
                 (int) (15 * scale + 0.5f),
                 (int) (5 * scale + 0.5f),
                 (int) (15 * scale + 0.5f));
         //
         // AÑADIENDO EL TÍTULO
         //
         linearInsideScroll.addView(websTitle);
         //
         // WEBS
         //
         TextView[] websOutput = new TextView[target.getWeb().size()];
         ImageButton[] websBtn = new ImageButton[target.getWeb().size()];
         for (int i = 0; i < target.getWeb().size(); i++) {
             container = new LinearLayout(getApplicationContext());
             container.setOrientation(LinearLayout.HORIZONTAL);
             websOutput[i] = new TextView(getApplicationContext());
             websBtn[i] = new ImageButton(getApplicationContext());
             websOutput[i].setText(target.getWeb().get(i));
             LinearLayout.LayoutParams prmX = new LinearLayout.LayoutParams(
                     (int) (width * 0.7f),
                     LayoutParams.WRAP_CONTENT);
             prmX.setMargins(0, (int) (5 * scale + 0.5f), 0,
                     (int) (5 * scale + 0.5f));
             websOutput[i].setLayoutParams(prmX);
             websOutput[i].setTextColor(res.getColor(R.color.pure_black));
             websOutput[i].setPadding(
                     (int) (5 * scale + 0.5f),
                     (int) (10 * scale + 0.5f),
                     (int) (5 * scale + 0.5f),
                     (int) (10 * scale + 0.5f));
             container.addView(websOutput[i]);
             websBtn[i].setTag(target.getWeb().get(i));
             RelativeLayout.LayoutParams shareParams = new RelativeLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
             shareParams.addRule(
                     RelativeLayout.ALIGN_RIGHT, mailsOutput[i].getId());
             shareParams.width = (int) (50 * scale + 0.5f);
             shareParams.height = (int) (50 * scale + 0.5f);
             websBtn[i].setLayoutParams(shareParams);
             websBtn[i].setBackgroundResource(R.drawable.web_btn);
             websBtn[i].setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     String url = (String) v.getTag();
                     String[] aux = url.split(" ");
                     url = aux[0];
                     Intent linkIntent = new Intent(Intent.ACTION_VIEW);
                     if (!url.startsWith("http://") &&
                             !url.startsWith("https://")) {
                         url = "http://" + url;
                     }
                     linkIntent.setData(Uri.parse(url));
                     startActivity(linkIntent);
                 }
             });
             container.addView(websBtn[i]);
             linearInsideScroll.addView(container);
         }
         scrollMain.addView(linearInsideScroll);
         detailsContainer.addView(scrollMain);
     }
 
     private void printDetails(String parameter) {
         String output = "";
         if (parameter.equals("contact")) {
             printContactInfo();
             title.setVisibility(View.VISIBLE);
             image.setVisibility(View.VISIBLE);
         }
         else if (parameter.equals("desc")) {
             output = target.getDesc();
             title.setVisibility(View.VISIBLE);
             image.setVisibility(View.VISIBLE);
             detailsContainer.removeAllViews();
             //
             // DECLARACIÓN DEL CONTENEDOR CON SCROLL
             //
             scrollMain = new ScrollView(getApplicationContext());
             RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
             scrollParams.setMargins(0, (int) (10 * scale + 0.5f), 0, 0);
             scrollParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                     RelativeLayout.TRUE);
             scrollMain.setLayoutParams(scrollParams);
             //
             // DECLARACIÓN DEL CONTENEDOR DENTRO DEL SCROLL
             //
             linearInsideScroll = new LinearLayout(getApplicationContext());
             LinearLayout.LayoutParams linearInsideParams = new LinearLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
             linearInsideScroll.setOrientation(LinearLayout.VERTICAL);
             linearInsideScroll.setLayoutParams(linearInsideParams);
             LinearLayout.LayoutParams prm = new LinearLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT,
                     LayoutParams.WRAP_CONTENT);
             prm.setMargins(0, (int) (5 * scale + 0.5f), 0,
                     (int) (5 * scale + 0.5f));
             detailsTarget = new TextView(getApplicationContext());
             detailsTarget.setLayoutParams(prm);
             detailsTarget.setText(output);
             detailsTarget.setTextColor(res.getColor(R.color.pure_black));
             detailsTarget.setPadding((int) (10 * scale + 0.5f),
                     (int) (20 * scale + 0.5f),
                     (int) (10 * scale + 0.5f),
                     (int) (20 * scale + 0.5f));
             scrollMain.addView(detailsTarget);
             detailsContainer.addView(scrollMain);
         }
         else if (parameter.equals("loc")) {
             title.setVisibility(View.GONE);
             image.setVisibility(View.GONE);
             DisplayMetrics dm = new DisplayMetrics();
             this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
             int width = dm.widthPixels;
             int height = dm.heightPixels;
             //
             // DECLARACIÓN DEL CONTENEDOR CON SCROLL
             //
             scrollMain = new ScrollView(getApplicationContext());
             RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
             scrollParams.setMargins(0, (int) (10 * scale + 0.5f), 0, 0);
             scrollParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                     RelativeLayout.TRUE);
             scrollMain.setLayoutParams(scrollParams);
             //
             // DECLARACIÓN DEL CONTENEDOR DENTRO DEL SCROLL
             //
             linearInsideScroll = new LinearLayout(getApplicationContext());
             LinearLayout.LayoutParams linearInsideParams = new LinearLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
             linearInsideScroll.setOrientation(LinearLayout.VERTICAL);
             linearInsideScroll.setLayoutParams(linearInsideParams);
             output = target.getLoc();
             detailsContainer.removeAllViews();
             LinearLayout.LayoutParams prm = new LinearLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT,
                     LayoutParams.WRAP_CONTENT);
             prm.setMargins(0, (int) (5 * scale + 0.5f), 0,
                     (int) (5 * scale + 0.5f));
             detailsTarget = new TextView(getApplicationContext());
             detailsTarget.setLayoutParams(prm);
             detailsTarget.setText(output);
             detailsTarget.setTextColor(res.getColor(R.color.pure_black));
             detailsTarget.setPadding((int) (10 * scale + 0.5f),
                     (int) (20 * scale + 0.5f),
                     (int) (10 * scale + 0.5f),
                     (int) (20 * scale + 0.5f));
             linearInsideScroll.addView(detailsTarget);
             LinearLayout.LayoutParams prmX = new LinearLayout.LayoutParams(
                     width, (int) (height * 0.7f));
             gMaps.setLayoutParams(prmX);
             try {
                 Geocoder geocoder = new Geocoder(getApplicationContext());
                 try {
                     map.clear();
                     List<Address> addresses = geocoder.getFromLocationName(output, 1);
                     if (addresses.size() > 0) {
                         double latitude = addresses.get(0).getLatitude();
                         double longitude = addresses.get(0).getLongitude();
                         LatLng lodging = new LatLng(latitude, longitude);
                         map.moveCamera(
                                 CameraUpdateFactory.newLatLngZoom(lodging, 13));
                         map.addMarker(new MarkerOptions()
                                 .title(target.getTitle())
                                 .snippet(target.getProvince())
                                 .position(lodging));
                     }
                 } catch (Exception e) {
                     // NO PINTAMOS MARCADOR PORQUE NO LO HA ENCONTRADO
                 }
                 linearInsideScroll.addView(gMaps);
             } catch (Exception e) {
                 Log.d("fail", e.toString());
                 Toast.makeText(
                         getApplicationContext(),
                         res.getString(R.string.coding_no_gmaps),
                         Toast.LENGTH_LONG).show();
             }
             scrollMain.addView(linearInsideScroll);
             detailsContainer.addView(scrollMain);
         }
         else {
             linearInsideScroll.addView(detailsTarget);
             detailsContainer.addView(linearInsideScroll);
             detailsContainer.addView(detailsTarget);
         }
     }
 
     // /////////////////////////////////////////////////////////////////////////
     //
     //
     // SECCIÓN BOTONES ANDROID
     //
     //
     // /////////////////////////////////////////////////////////////////////////
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             vibe.vibrate(60);
             finish();
         }
         return super.onKeyDown(keyCode, event);
     }
 
     public void updateBitmap(Bitmap image2) {
         image.setImageBitmap(image2);
         image.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 vibe.vibrate(60);
                 if (isImageFitToScreen) {
                     isImageFitToScreen = false;
                     image.setLayoutParams(new LinearLayout.LayoutParams(
                             LinearLayout.LayoutParams.MATCH_PARENT, (int) (100 * scale + 0.5f)));
                     image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                 } else {
                     isImageFitToScreen = true;
                     image.setLayoutParams(new LinearLayout.LayoutParams(
                             LinearLayout.LayoutParams.MATCH_PARENT,
                             LinearLayout.LayoutParams.MATCH_PARENT));
                     image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                 }
             }
         });
 
     }
 
     // /////////////////////////////////////////////////////////////////////////
     //
     //
     // SECCIÓN MENÚ SUPERIOR
     //
     //
     // /////////////////////////////////////////////////////////////////////////
 
     private void topMenuMaker() {
         getSupportActionBar().setHomeButtonEnabled(true);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     // /////////////////////////////////////////////////////////////////////////
     // AÑADIMOS ELEMENTOS AL MENU DEL ACTION BAR
     // /////////////////////////////////////////////////////////////////////////
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         int itemId = item.getItemId();
         switch (itemId) {
             case android.R.id.home:
                 vibe.vibrate(60);
                 if (drawerLayout.isDrawerOpen(navList)) {
                     drawerLayout.closeDrawer(navList);
                 } else {
                     drawerLayout.openDrawer(navList);
                 }
                 break;
             case R.id.favs:
                 vibe.vibrate(60);
                 startActivity(new Intent(this, Favs.class));
                 break;
             case R.id.reload:
                 vibe.vibrate(60);
                 startActivity(new Intent(this, DownloadXML.class));
                 finish();
                 break;
         }
         return true;
     }
 
     // /////////////////////////////////////////////////////////////////////////
     //
     //
     // SECCIÓN MENÚ LATERAL
     //
     //
     // /////////////////////////////////////////////////////////////////////////
     private void navListMaker() {
         navList = (ListView) findViewById(R.id.details_menu_drawer);
         // /////////////////////////////////////////////////////////////////////
         // CREANDO EL NAVEGADOR LATERAL
         // /////////////////////////////////////////////////////////////////////
         navdata = new DetailsNav(getApplicationContext()).getNav();
         arrayAdapter = new DetailsNavMaker(this, R.layout.main_list, navdata);
         navList.setAdapter(arrayAdapter);
         navList.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                 vibe.vibrate(60);
                 String selected = ((DetailsNav) a.getAdapter().getItem(pos))
                         .getTitle();
                 if (selected.equals(res.getString(R.string.coding_back))) {
                     finish();
                 }
                 if (selected.equals(res.getString(R.string.coding_desc))) {
                     printDetails("desc");
                     drawerLayout.closeDrawer(navList);
                 }
                 if (selected.equals(res.getString(R.string.coding_contact))) {
                     printDetails("contact");
                     drawerLayout.closeDrawer(navList);
                 }
                 if (selected.equals(res.getString(R.string.coding_loc))) {
                     printDetails("loc");
                     drawerLayout.closeDrawer(navList);
                 }
                 if (selected.equals(res.getString(R.string.coding_fav))) {
                     try {
                         favs = (HashSet<String>) getSharedPreferences(
                                 "PREFERENCE", MODE_PRIVATE).getStringSet(
                                 "favs", null);
                         if (favs.contains(targetProvince)) {
                             favs.remove(targetProvince);
                             drawerLayout.closeDrawer(navList);
                             Toast.makeText(
                                     getApplicationContext(),
                                     res.getString(R.string.coding_del_fav_txt),
                                     Toast.LENGTH_SHORT).show();
                         }
                         else {
                             favs.add(targetProvince);
                             getSharedPreferences(
                                     "PREFERENCE", MODE_PRIVATE).
                                     edit().
                                     putStringSet("favs", favs).commit();
                             drawerLayout.closeDrawer(navList);
                             Toast.makeText(
                                     getApplicationContext(),
                                     res.getString(R.string.coding_new_fav_txt),
                                     Toast.LENGTH_SHORT).show();
                         }
                     }
                     catch (NullPointerException e) {
                         favs = new HashSet<String>();
                         favs.add(targetProvince);
                         getSharedPreferences(
                                 "PREFERENCE", MODE_PRIVATE).
                                 edit().
                                 putStringSet("favs", favs).commit();
                         drawerLayout.closeDrawer(navList);
                         Toast.makeText(
                                 getApplicationContext(),
                                 R.string.coding_new_fav_txt,
                                 Toast.LENGTH_SHORT).show();
                     }
                 }
             }
         });
         // /////////////////////////////////////////////////////////////////////
         // DECLARAMOS EL ESCUCHADOR DE NUESTRO MENU LATERAL
         // /////////////////////////////////////////////////////////////////////
         navToggle = new ActionBarDrawerToggle(
                 this, drawerLayout,
                 R.drawable.ic_drawer,
                 R.string.open_drawer,
                 R.string.close_drawer
                 ) {
                     public void onDrawerClosed(View view) {
                         invalidateOptionsMenu();
                     }
 
                     public void onDrawerOpened(View view) {
                         invalidateOptionsMenu();
                     }
                 };
         drawerLayout.setDrawerListener(navToggle);
     }
 }
