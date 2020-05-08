 package com.level42.mixit.activities;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.google.inject.Inject;
 import com.level42.mixit.R;
 import com.level42.mixit.adapters.FilterAdapter;
 import com.level42.mixit.adapters.TalksAdapter;
 import com.level42.mixit.listeners.OnTaskPostExecuteListener;
 import com.level42.mixit.models.PlanningTalk;
 import com.level42.mixit.models.Talk;
 import com.level42.mixit.services.IPlanningService;
 import com.level42.mixit.tasks.GetPlanningAsyncTask;
 import com.level42.mixit.utils.MessageBox;
 import com.level42.mixit.utils.Utils;
 
 /**
  * Activité d'affichage du planning de l'événément MixIt.
  */
 @ContentView(R.layout.activity_planning)
 public class PlanningActivity extends RoboActivity implements Observer {
 
     /**
      * Contrôle : Liste des sessions.
      */
     @InjectView(R.id.listTalks)
     private ListView listTalks;
 
     /**
      * Contrôle : Liste des date.
      */
     @InjectView(R.id.listDateTalks)
     private ListView listDateTalks;
 
     /**
      * Contrôle : Liste des heures.
      */
     @InjectView(R.id.listHeureTalks)
     private ListView listHeureTalks;
     
     /**
      * Interface vers le service de gestion du planning.
      */
     @Inject
     private IPlanningService planningService;
 
     /**
      * Planning de l'activité.
      */
     private PlanningTalk planning = new PlanningTalk();
     
     /**
      * Boite d'attente de chargement.
      */
     private ProgressDialog progressDialog;
 
     /**
      * Adapter de la liste des talks.
      */
     private TalksAdapter adapter;
     
     private FilterAdapter adapterDate;
     
     private FilterAdapter adapterHeure;
 
     /**
      * Date sélectionnée
      */
     private String selectedDate;
     
     /**
      * Heure sélectionée
      */
     private String selectedHeure;
     
     /**
      * Listener pour la tâche asynchrone
      */
     private OnTaskPostExecuteListener<List<Talk>> listenerAsync = 
             new OnTaskPostExecuteListener<List<Talk>>() {
         public void onTaskPostExecuteListener(List<Talk> result) {
             if (result != null) {
                 planning.setGroupedTalks(result);
             }
             if (progressDialog.isShowing()) {
                 progressDialog.dismiss();
             }
         }
 
         public void onTaskInterruptListener(Exception cancelReason) {
             OnClickListener listener = new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog,
                         int which) {
                     finish();
                 }
             };
             MessageBox.showError(
                     getResources().getString(
                             R.string.label_dialog_error),
                     cancelReason.getMessage(), listener,
                     PlanningActivity.this);
         }
 
         public void onTaskCancelledListener() {
             MessageBox.showInformation(
                     getResources().getString(
                             R.string.label_dialog_aborted),
                     PlanningActivity.this);
         }
     };
     
     /*
      * (non-Javadoc)
      * @see roboguice.activity.RoboActivity#onCreate(android.os.Bundle)
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         planning.addObserver(this);
         adapter = new TalksAdapter(this.getBaseContext());
         listTalks.setAdapter(adapter);
 
         listTalks.setOnItemClickListener(new ListView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 Intent talkActivity = new Intent(
                         PlanningActivity.this,
                         TalkActivity.class);
                 talkActivity.putExtra(TalkActivity.TALK_ID, id);
                 PlanningActivity.this.startActivity(talkActivity);
             }
         });
 
         @SuppressWarnings("unchecked")
         List<Talk> savedTalks = (List<Talk>) getLastNonConfigurationInstance();
         if (savedTalks == null) {
             this.setupProgressDialog();
             this.refreshTalks();
         } else {
             planning.setGroupedTalks(savedTalks);
         }
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onRetainNonConfigurationInstance()
      */
     @Override
     public Object onRetainNonConfigurationInstance() {
         return this.planning.getGroupedTalks();
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu_main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_adresse:
                 Intent adresseActivity = new Intent(PlanningActivity.this,
                         AdresseActivity.class);
                 PlanningActivity.this.startActivity(adresseActivity);
                 break;
             case R.id.menu_billet:
                 Intent ticketActivity = new Intent(PlanningActivity.this,
                         TicketActivity.class);
                 PlanningActivity.this.startActivity(ticketActivity);
                 break;
             case R.id.menu_ltalks:
                 Intent lTalkList = new Intent(PlanningActivity.this, 
                         LightningTalkListActivity.class);
                 PlanningActivity.this.startActivity(lTalkList);
                 break;
             case R.id.menu_talks:
                 Intent talkList = new Intent(PlanningActivity.this, 
                         TalkListActivity.class);
                 PlanningActivity.this.startActivity(talkList);
                 break;
             case R.id.menu_preference:
                 Intent preferences = new Intent(PlanningActivity.this, 
                         PreferencesActivity.class);
                 PlanningActivity.this.startActivity(preferences);
                 break;
             default:
                 break;
         }
         return true;
     }
     
     /**
      * Affiche la boite de chargement.
      */
     protected void setupProgressDialog() {
         if (progressDialog == null) {
             progressDialog = MessageBox
                     .getProgressDialog(PlanningActivity.this);
         }
     }
 
     /**
      * Affiche la liste des dates
      */
     protected void displayDate() {
         List<String> listDate = new ArrayList<String>();
         String lastDate = null;
         String crtDate = null;
         List<Talk> grouped = this.planning.getGroupedTalks();
         for (Talk groupe : grouped) {  
 
             SimpleDateFormat format = new SimpleDateFormat("d");
             crtDate = format.format(groupe.getDateSession());
             
             if (lastDate == null || !lastDate.equals(crtDate)) {
                 if (lastDate == null) {
                     selectedDate = crtDate;
                 }
                 listDate.add(crtDate);
                 lastDate = crtDate;
             }
         }
         adapterDate = new FilterAdapter(PlanningActivity.this, listDate, selectedDate, R.layout.planning_item_date);        
         listDateTalks.setAdapter(adapterDate);
         listDateTalks.setOnItemClickListener(new ListView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedDate = (String) parent.getItemAtPosition(position);
                adapterDate.setSelected(selectedDate);
                displayHeure();
                adapter.updateTalks(getTalksFromSelectedDateTime());
             }
         });
     }
     
     /**
      * Affiche la liste des heures
      */
     protected void displayHeure() {
         List<String> listHeure = new ArrayList<String>();
         String lastHeure = null;
         String crtHeure = null;
         List<Talk> talks = this.planning.getGroupedTalks();               
         SimpleDateFormat formatJour = new SimpleDateFormat("d");
         for (Talk talk : talks) {
             
             if (selectedDate.equals(formatJour.format(talk.getDateSession()))) {
                 
                 SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                 crtHeure = format.format(talk.getDateSession());
                 
                 if (lastHeure == null || !lastHeure.equals(crtHeure) ) {
                     if (lastHeure == null) {
                         selectedHeure = crtHeure;
                     }
                     listHeure.add(crtHeure);
                     lastHeure = crtHeure;
                 } 
             }
         }
 
         adapterHeure = new FilterAdapter(PlanningActivity.this, listHeure, selectedHeure, R.layout.planning_item_heure); 
         listHeureTalks.setAdapter(adapterHeure);
         listHeureTalks.setOnItemClickListener(new ListView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedHeure = (String) parent.getItemAtPosition(position);
                adapterHeure.setSelected(selectedHeure);
                adapter.updateTalks(getTalksFromSelectedDateTime());
             }
         });
     }
     
     /**
      * Rafraichit la liste des sessions.
      */
     protected void refreshTalks() {
         // Préparation du service
         GetPlanningAsyncTask getTalksAsyncService = new GetPlanningAsyncTask();
 
         // Ajout d'un listener pour récupérer le retour
         getTalksAsyncService.setPostExecuteListener(listenerAsync);
 
         // Execution du service
        Integer defaultDelay = Integer.valueOf(getString(R.string.planningDelay));
         
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
        Integer delay = Integer.valueOf(preferences.getString(PreferencesActivity.PREF_SESSION_DELAY, String.valueOf(defaultDelay)));
         
        getTalksAsyncService.execute(planningService, delay);
     }
     
     /**
      * Retourne la liste des talks correspondants à la
      * date et l'heure sélectionnée
      * @return Liste des talks
      */
     protected List<Talk> getTalksFromSelectedDateTime() {
         List<Talk> selectedTalks = new ArrayList<Talk>();
         String sessionDate = null;
         String selected = selectedDate + " " + selectedHeure;
         List<Talk> talks = this.planning.getGroupedTalks();
         for (Talk talk : talks) {
             
             SimpleDateFormat format = new SimpleDateFormat("d HH:mm");
             sessionDate = format.format(talk.getDateSession());
             
             if (sessionDate.equals(selected) ) {
                 selectedTalks.add(talk);
             } 
         }
         return selectedTalks;
     }
 
     /**
      * Méthode appelée lorsqu'un objet sur lequel l'activité est abonnée est mis à jour.
      * @param observable Objet mis à jour
      * @param data Données mise à jour
      */
     public void update(Observable observable, Object data) {
         if (observable instanceof PlanningTalk) {
             Log.d(Utils.LOGTAG, "Changement sur le planning");
             PlanningTalk planning = (PlanningTalk) observable;
             if (planning != null) {
                 this.displayDate();
                 this.displayHeure();
                 adapter.updateTalks(getTalksFromSelectedDateTime());
             }
         }
     }
 }
