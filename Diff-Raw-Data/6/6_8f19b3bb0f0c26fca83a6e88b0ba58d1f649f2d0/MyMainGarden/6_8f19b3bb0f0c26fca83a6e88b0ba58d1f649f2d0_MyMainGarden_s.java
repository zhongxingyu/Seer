 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.gots.R;
 import org.gots.action.GardeningActionInterface;
 import org.gots.action.bean.DeleteAction;
 import org.gots.ads.GotsAdvertisement;
 import org.gots.allotment.adapter.ListAllotmentAdapter;
 import org.gots.bean.Allotment;
 import org.gots.bean.BaseAllotmentInterface;
 import org.gots.broadcast.BroadCastMessages;
 import org.gots.seed.GotsGrowingSeedManager;
 import org.gots.weather.view.WeatherWidget;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class MyMainGarden extends AbstractActivity {
 
     private ListAllotmentAdapter lsa;
 
     ListView listAllotments;
 
     WeatherWidget weatherWidget;
 
     private Menu menu;
 
     private List<BaseAllotmentInterface> selectedAllotments = new ArrayList<BaseAllotmentInterface>();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         ActionBar bar = getSupportActionBar();
         bar.setDisplayHomeAsUpEnabled(true);
         bar.setTitle(R.string.dashboard_allotments_name);
 
         // GardenManager gm =GardenManager.getInstance();
         registerReceiver(seedBroadcastReceiver, new IntentFilter(BroadCastMessages.GROWINGSEED_DISPLAYLIST));
 
         setContentView(R.layout.garden);
         listAllotments = (ListView) findViewById(R.id.IdGardenAllotmentsList);
         lsa = new ListAllotmentAdapter(MyMainGarden.this, new ArrayList<BaseAllotmentInterface>());
         listAllotments.setAdapter(lsa);
         listAllotments.setDivider(null);
         listAllotments.setDividerHeight(0);
         listAllotments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 LinearLayout menuSelectable = (LinearLayout) view.findViewById(R.id.idMenuAllotment);
                 BaseAllotmentInterface selectAllotment = (BaseAllotmentInterface) listAllotments.getItemAtPosition(position);
                 if (menuSelectable.isSelected()) {
                     menuSelectable.setSelected(false);
                     selectedAllotments.remove(selectAllotment);
                 } else {
                     menuSelectable.setSelected(true);
                     selectedAllotments.add(selectAllotment);
                 }
 
                 if (menu != null) {
                     MenuItem item = menu.findItem(R.id.delete_garden);
                     if (selectedAllotments.size() > 0)
                         item.setVisible(true);
                     else
                         item.setVisible(false);
 
                 }
             }
 
         });
         // listAllotments.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
         // listAllotments.setBackgroundDrawable(getResources().getDrawable(R.drawable.help_hut_2));
         if (!gotsPrefs.isPremium()) {
             GotsAdvertisement ads = new GotsAdvertisement(this);
 
             LinearLayout layout = (LinearLayout) findViewById(R.id.idAdsTop);
             layout.addView(ads.getAdsLayout());
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.menu_garden, menu);
         this.menu = menu;
         return true;
     }
 
     public BroadcastReceiver seedBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             // onResume();
         }
     };
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case android.R.id.home:
             finish();
             return true;
         case R.id.new_allotment:
 
             // AllotmentDBHelper helper = new AllotmentDBHelper(this);
             // helper.insertAllotment(newAllotment);
             new AsyncTask<Void, Integer, BaseAllotmentInterface>() {
                 @Override
                 protected BaseAllotmentInterface doInBackground(Void... params) {
                     BaseAllotmentInterface newAllotment = new Allotment();
                     newAllotment.setName("" + new Random().nextInt());
                     return allotmentManager.createAllotment(newAllotment);
                 }
 
                 @Override
                 protected void onPostExecute(BaseAllotmentInterface result) {
                     onResume();
                     super.onPostExecute(result);
                 }
             }.execute();
 
             // listAllotments.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_simple));
             return true;
         case R.id.help:
             Intent browserIntent = new Intent(this, WebHelpActivity.class);
             browserIntent.putExtra(WebHelpActivity.URL, getClass().getSimpleName());
             startActivity(browserIntent);
 
             return true;
 
         case R.id.delete_garden:
             removeSelectedAllotment();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void removeSelectedAllotment() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Delete").setCancelable(false).setPositiveButton("OK",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         new AsyncTask<BaseAllotmentInterface, Integer, Void>() {
                             @Override
                             protected void onPreExecute() {
                                 super.onPreExecute();
                             }
 
                             @Override
                             protected Void doInBackground(BaseAllotmentInterface... params) {
                                 GardeningActionInterface actionItem = new DeleteAction(MyMainGarden.this);
                                 for (Iterator<BaseAllotmentInterface> iterator = selectedAllotments.iterator(); iterator.hasNext();) {
                                     BaseAllotmentInterface allotment = iterator.next();
                                     actionItem.execute(allotment, null);
                                 }
                                 return null;
                             }
 
                             @Override
                             protected void onPostExecute(Void result) {
                                 selectedAllotments.clear();
                                 onResume();
                                 super.onPostExecute(result);
                             }
                         }.execute();
                         dialog.dismiss();
                     }
                 }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
             }
         });
 
         builder.show();
     }
 
     private ProgressDialog dialog;
 
     @Override
     protected void onResume() {
         super.onResume();
         new AsyncTask<Void, Integer, List<BaseAllotmentInterface>>() {
 
             protected void onPreExecute() {
                 dialog = ProgressDialog.show(MyMainGarden.this, "", getResources().getString(R.string.gots_loading),
                         true);
                 dialog.setCanceledOnTouchOutside(true);
             };
 
             @Override
             protected List<BaseAllotmentInterface> doInBackground(Void... params) {
                 List<BaseAllotmentInterface> allotments = allotmentManager.getMyAllotments();
                 GotsGrowingSeedManager growingSeedManager = GotsGrowingSeedManager.getInstance().initIfNew(
                         MyMainGarden.this);
 
                 for (int i = 0; i < allotments.size(); i++) {
 
                     allotments.get(i).setSeeds(growingSeedManager.getGrowingSeedsByAllotment(allotments.get(i)));
                 }
                 return allotments;
             }
 
             @Override
             protected void onPostExecute(List<BaseAllotmentInterface> result) {
                 if (result != null)
                     lsa.setAllotments(result);
 
                 try {
                     dialog.dismiss();
                     dialog = null;
                 } catch (Exception e) {
                     // nothing
                 }
                 // if (listAllotments.getCount() == 0) {
                 // final String classname = getClass().getSimpleName();
                 // new AlertDialog.Builder(getApplicationContext()).setIcon(R.drawable.help).setTitle(
                 // R.string.menu_help_firstlaunch).setPositiveButton(R.string.button_ok,
                 // new DialogInterface.OnClickListener() {
                 // public void onClick(DialogInterface dialog, int whichButton) {
                 //
                 // Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                 // Uri.parse(HelpUriBuilder.getUri(classname)));
                 // startActivity(browserIntent);
                 // }
                 // }).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                 // public void onClick(DialogInterface dialog, int whichButton) {
                 //
                 // /* User clicked Cancel so do some stuff */
                 // }
                 // }).show();
                 // // Intent intent = new Intent(this, MyMainGardenFirstTime.class);
                 // // startActivity(intent);
                 // }
                 super.onPostExecute(result);
             }
         }.execute();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
     }
 
 }
