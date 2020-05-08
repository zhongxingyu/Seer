 package net.rhatec.amtmobile.dialog;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.rhatec.amtmobile.R;
 import net.rhatec.amtmobile.helpers.MenuCreator;
 import net.rhatec.amtmobile.manager.FavorisManager;
import net.rhatec.amtmobile.notifications.OnetimeAlarmReceiver;
 import net.rhatec.amtmobile.providers.TransportProvider;
 import net.rhatec.amtmobile.tasks.DeleteFolderTask;
 
import android.app.AlarmManager;
 import android.app.AlertDialog;
import android.app.PendingIntent;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class PreferenceDlg extends PreferenceActivity implements OnPreferenceClickListener
 {
 
 	ArrayList<HashMap<String, String>>	m_List;
 	ListView							m_ListeView;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		this.setTitle(getResources().getText(R.string.preference_title));
 		addPreferencesFromResource(R.xml.preferences);
 
 		Preference effFavPref = findPreference("effFavoris");
 		effFavPref.setOnPreferenceClickListener(this);
 
 		Preference resetDbaPref = findPreference("resetDba");
 		resetDbaPref.setOnPreferenceClickListener(this);
 
 		Preference resetAll = findPreference("resetAll");
 		resetAll.setOnPreferenceClickListener(this);
		
		Preference aPropos = findPreference("aPropos");
		aPropos.setOnPreferenceClickListener(this);
 
 		Preference userguide = findPreference("userguide");
 		userguide.setOnPreferenceClickListener(this);
 
 		
 		Preference startPage = (Preference) findPreference("startPage");
 		startPage.setOnPreferenceClickListener(this);
 		 
 
		Preference removeNotification = findPreference("removeNotification");
		removeNotification.setOnPreferenceClickListener(this);
		
		
 
 	}
 
 	@Override
 	public boolean onPreferenceClick(final Preference preference)
 	{
 		boolean success = true;
 		if (preference.getKey().equals("aPropos"))
 		{
 			startActivity(new Intent(this, AProposDlg.class));
 		} else if (preference.getKey().equals("resetAll"))
 		{
 			AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
 			builder.setTitle(R.string.PreferenceDlg_Confirmation_ResetAll_Title);
 			builder.setCancelable(true);
 			builder.setPositiveButton(R.string.PreferenceDlg_Confirmation_ResetAll_Pos, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which)
 				{
 					DeleteFolderTask t = new DeleteFolderTask(TransportProvider.getRootPath());
 					ModalWaitDialog d = new ModalWaitDialog();
 					d.runTask(preference.getContext(), getResources().getString(R.string.PreferenceDlg_remise_a_zero_favoris_patienter), t);
 				}
 			});
 			builder.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which)
 				{
 
 				}
 			});
 			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				@Override
 				public void onCancel(DialogInterface dialog)
 				{
 
 				}
 			});
 			builder.setMessage(R.string.PreferenceDlg_Confirmation_ResetAll_Message);
 			builder.create().show();
 
 		} else if (preference.getKey().equals("effFavoris"))
 		{
 			Toast.makeText(getBaseContext(), FavorisManager.supprimerFichierFavoris(), Toast.LENGTH_LONG).show();
 		} else if (preference.getKey().equals("resetDba"))
 		{
 			startActivity(new Intent(this, SupprimerDBADlg.class));
 		} else if (preference.getKey().equals("userguide"))
 		{
 			startActivity(new Intent(this, UserManualDlg.class));
 
 		}
		else if (preference.getKey().equals("removeNotification"))
		{
			Intent intentToFire = new Intent(this, OnetimeAlarmReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
		}
 		
 		  else if(preference.getKey().equals("startPage")) 
 		  {
 			 // Toast.makeText(getBaseContext(),
 			 // FavorisManager.supprimerFichierFavoris(), Toast.LENGTH_LONG).show();
 		 }
 		 
 		else
 		{
 			success = false;
 		}
 		return success;
 
 	}
 
 	// XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 	// Crations des boutons du menu
 	// XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 	/** hook into menu button for activity */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuCreator.populateMenu(menu, this.getBaseContext());
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/** when menu button option selected */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		MenuCreator.startActivityFromMenu(item, this);
 		return super.onOptionsItemSelected(item);
 	}
 }
