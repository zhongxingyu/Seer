 package fr.pronoschallenge.classement;
 
 import fr.pronoschallenge.R;
 import fr.pronoschallenge.auth.LoginActivity;
 import fr.pronoschallenge.profil.ProfilActivity;
 import fr.pronoschallenge.rest.QueryBuilder;
 import fr.pronoschallenge.rest.RestClient;
 import fr.pronoschallenge.util.NetworkUtil;
 import greendroid.app.GDActivity;
 import greendroid.widget.ActionBarItem;
 import greendroid.widget.NormalActionBarItem;
 import greendroid.widget.QuickActionGrid;
 import greendroid.widget.QuickActionWidget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ClassementActivity extends GDActivity {
 
 	public final static String CLASSEMENT_TYPE_GENERAL = "general";
 	public final static String CLASSEMENT_TYPE_HOURRA = "hourra";
 	public final static String CLASSEMENT_TYPE_MIXTE = "mixte";
 	public final static String CLASSEMENT_TYPE_GENERAL_LAST = "derniere_journee";
 	public final static String CLASSEMENT_TYPE_HOURRA_LAST = "hourra_derniere_journee";
 	
 	private String classementType;
 	private ListView classementListView;
     private TextView messageTextView;
     private boolean filtre;
     private String mUser;
 
     private QuickActionWidget classementQuickActionGrid;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		setActionBarContentView(R.layout.classement);
 		
 		// Obtain handles to UI objects
 		classementListView = (ListView) findViewById(R.id.classementList);
         messageTextView = (TextView) findViewById(R.id.classementMessage);
         filtre = false;
         mUser = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
         
         classementListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 			    int position, long id) {
 					ClassementEntry classementEntry = (ClassementEntry) classementListView.getItemAtPosition(position);
 					Bundle objetbunble = new Bundle();
 					objetbunble.putString("pseudo", classementEntry.getPseudo());
 					Intent intent = new Intent(ClassementActivity.this,	ProfilActivity.class);
 					intent.putExtras(objetbunble);
 					startActivity(intent);
 			}
         });
 
         // menu pour switcher entre les classements
         classementQuickActionGrid = new QuickActionGrid(this);
         classementQuickActionGrid.addQuickAction(new ClassementQuickAction(this, null, R.string.type_classement_general));
         classementQuickActionGrid.addQuickAction(new ClassementQuickAction(this, null, R.string.type_classement_hourra));
         classementQuickActionGrid.addQuickAction(new ClassementQuickAction(this, null, R.string.type_classement_mixte));
         classementQuickActionGrid.addQuickAction(new ClassementQuickAction(this, null, R.string.type_classement_general_last));
         classementQuickActionGrid.addQuickAction(new ClassementQuickAction(this, null, R.string.type_classement_hourra_last));
 
         classementQuickActionGrid.setOnQuickActionClickListener(new QuickActionWidget.OnQuickActionClickListener() {
             public void onQuickActionClicked(QuickActionWidget widget, int position) {
                 final CharSequence[] classementItems = {getString(R.string.type_classement_general), getString(R.string.type_classement_hourra), getString(R.string.type_classement_mixte), getString(R.string.type_classement_general_last), getString(R.string.type_classement_hourra_last)};
                 final String[] classementTypes = {ClassementActivity.CLASSEMENT_TYPE_GENERAL, ClassementActivity.CLASSEMENT_TYPE_HOURRA, ClassementActivity.CLASSEMENT_TYPE_MIXTE, ClassementActivity.CLASSEMENT_TYPE_GENERAL_LAST, ClassementActivity.CLASSEMENT_TYPE_HOURRA_LAST};
                 ClassementActivity classementActivity = (ClassementActivity) classementQuickActionGrid.getContentView().getContext();
                 if (position <= 2) {
                 	classementActivity.setTitle(getString(R.string.title_classement) + " " + classementItems[position]);
                 } else {
                 	classementActivity.setTitle(classementItems[position]);
                 }

                 new ClassementTask(classementActivity).execute(classementTypes[position]);
             }
         });
 
         // Ajout de l'item dans la barre de menu pour changer de classement
         ActionBarItem item = getActionBar().newActionBarItem(NormalActionBarItem.class);
         item.setDrawable(R.drawable.gd_action_bar_list);
         getActionBar().addItem(item);
 
         // Ajout de l'item dans la barre de menu pour activer/d�sactiver le filtre du classement
         ActionBarItem itemFiltre = getActionBar().newActionBarItem(NormalActionBarItem.class);
         itemFiltre.setDrawable(R.drawable.user_no_favorites);
         getActionBar().addItem(itemFiltre);
 
 		classementType = (String) this.getIntent().getExtras().get("fr.pronoschallenge.classement.ClassementType");
 
 		if(NetworkUtil.isConnected(this.getApplicationContext())) {
             String titre = (String) this.getIntent().getExtras().get("fr.pronoschallenge.classement.ClassementTitle"); 
 			if (classementType.compareTo(CLASSEMENT_TYPE_GENERAL_LAST) == 0 || classementType.compareTo(CLASSEMENT_TYPE_HOURRA_LAST) == 0) {
 				setTitle(titre);	
 			} else {
 				setTitle(getString(R.string.title_classement) + " " + titre);	
 			}		

             new ClassementTask(this).execute(classementType);
         } else {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("Connexion Internet indisponible")
                 .setPositiveButton("Fermer", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                             dialog.cancel();
                                             finish();
                                        }
                                    });
             AlertDialog dialog = builder.create();
             dialog.show();
         }
 	}
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(requestCode == 1) {
             String userName = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
             String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", null);
             if(userName == null || password == null) {
                 finish();
             }
         }
     }
 
 	private List<ClassementEntry> getClassement(String type) {
 		List<ClassementEntry> classementEntries = new ArrayList<ClassementEntry>();
 
         String userName = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
         String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", null);
 
 		String strClassement = RestClient.get(new QueryBuilder(this.getAssets(), "/rest/classement/" + type + "?filtre=" + (filtre ? "1" : "0")).getUri(), userName, password);
 
 		try {
 			// A Simple JSONObject Creation
 	        JSONObject json = new JSONObject(strClassement);
 
 	        // A Simple JSONObject Parsing
 	        JSONArray classementArray = json.getJSONArray("classement");
 	        for(int i=0;i<classementArray.length();i++)
 	        {
 	        	JSONObject jsonClassementEntry = classementArray.getJSONObject(i);
 
 	        	ClassementEntry classementEntry = new ClassementEntry();
 	        	classementEntry.setPlace(jsonClassementEntry.getInt("place"));
 	        	classementEntry.setPseudo(jsonClassementEntry.getString("pseudo"));
 	        	classementEntry.setPoints(jsonClassementEntry.getDouble("points"));
 	        	classementEntries.add(classementEntry);
 	        }
 
 		} catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
 		return classementEntries;
 	}
 
     @Override
     public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
         switch (position) {
             case 0:
                 classementQuickActionGrid.show(item.getItemView());
                 break;
             case 1:
             	if (!filtre) {
                     // on vérifie si l'utilisateur est connecté
                     String userName = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
                     String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", null);
                     if(userName == null || password == null) {
                         startActivityForResult(new Intent(this, LoginActivity.class), 1);
                     }
             		filtre = true;
             		item.setDrawable(R.drawable.user_favorites);
             	} else {
             		filtre = false;
             		item.setDrawable(R.drawable.user_no_favorites);
             	}
             	ClassementActivity classementActivity = (ClassementActivity) classementQuickActionGrid.getContentView().getContext();
             	new ClassementTask(classementActivity).execute(this.getClassementType());
             	break;            	
         }
 
         return true;
     }
 
 	public String getClassementType() {
 		return classementType;
 	}
 
 	public void setClassementType(String classementType) {
 		this.classementType = classementType;
 	}
 
     private class ClassementTask extends AsyncTask<String, Void, Boolean> {
 
         final private ClassementActivity activity;
         private List<ClassementEntry> classementEntries;
         private ProgressDialog dialog;
 
         private ClassementTask(ClassementActivity activity) {
             this.activity = activity;
             dialog = new ProgressDialog(activity);
         }
 
         protected void onPreExecute() {
             this.dialog.setMessage("Chargement");
             this.dialog.show();
         }
 
         @Override
         protected Boolean doInBackground(final String... args) {
 
             classementEntries = activity.getClassement(args[0]);
 
             return true;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             if(classementEntries.size() > 0) {
                 ClassementAdapter adapter = new ClassementAdapter(activity,	R.layout.classement_item, classementEntries, mUser);
                 classementListView.setAdapter(adapter);
                 adapter.notifyDataSetChanged();
                 activity.messageTextView.setVisibility(View.GONE);
                 activity.classementListView.setVisibility(View.VISIBLE);
             } else {
                 activity.classementListView.setVisibility(View.GONE);
                 activity.messageTextView.setText("Classement non disponible");
                 activity.messageTextView.setVisibility(View.VISIBLE);
             }
             if (dialog.isShowing()) {
                 dialog.dismiss();
             }
             super.onPostExecute(success);
         }
     }
 }
