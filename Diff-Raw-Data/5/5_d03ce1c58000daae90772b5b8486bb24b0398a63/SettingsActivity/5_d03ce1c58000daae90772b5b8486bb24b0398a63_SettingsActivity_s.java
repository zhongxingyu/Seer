 package com.charlesmadere.android.classygames.settings;
 
 
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 
 import com.actionbarsherlock.app.SherlockPreferenceActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.charlesmadere.android.classygames.R;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 
 
 /**
  * Some of the code used in making this class and it's corresponding XML files
  * was taken from the official Android Documentation.
  * https://developer.android.com/guide/topics/ui/settings.html
  */
 public class SettingsActivity extends SherlockPreferenceActivity implements
 	GameSettingsFragment.GameSettingsFragmentListeners
 {
 
 
 	private ListPreference playersCheckersPieceColor;
 	private ListPreference opponentsCheckersPieceColor;
 
 
 
 
 	@Override
 	@SuppressWarnings("deprecation")
 	protected void onCreate(final Bundle savedInstanceState)
 	// The addPreferencesFromResource methods below are causing some
 	// deprecation warnings. In this case, the fact that they're here is fine.
 	// They have to be used if the running version of Android is below
 	// Honeycomb (v3.0). Same situation with the findPreference methods. See
 	// more information about API levels here:
 	// https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels
 	{
 		super.onCreate(savedInstanceState);
 		Utilities.styleActionBar(getResources(), getSupportActionBar(), true);
 
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
 		// Check to see if the running version of Android is below Honeycomb.
 		{
 			// get the intent's action
 			final String action = getIntent().getAction();
 
 			if (Utilities.verifyValidString(action))
 			{
 				if (action.equals(getString(R.string.com_charlesmadere_android_classygames_settings_game)))
 				// the intent's action is saying that we need to show the game
 				// settings preference file
 				{
 					addPreferencesFromResource(R.xml.settings_game);
 				}
 				else if (action.equals(getString(R.string.com_charlesmadere_android_classygames_settings_miscellaneous)))
 				// the intent's action is saying that we need to show the
 				// miscellaneous settings preference file
 				{
 					addPreferencesFromResource(R.xml.settings_miscellaneous);
 				}
 				else if (action.equals(getString(R.string.com_charlesmadere_android_classygames_settings_register)))
 					// the intent's action is saying that we need to show the
 					// RegisterForNotificationsActivity
 				{
 					startActivity(new Intent(this, RegisterForNotificationsActivity.class));
 				}
 				else if (action.equals(getString(R.string.com_charlesmadere_android_classygames_settings_unregister)))
 					// the intent's action is saying that we need to show the
 					// UnregisterFromNotificationsActivity
 				{
 					startActivity(new Intent(this, UnregisterFromNotificationsActivity.class));
 				}
 				else
 				// The intent's action was something strange. We'll show the
 				// default preference file. This should (hopefully) never
 				// happen.
 				{
 					addPreferencesFromResource(R.xml.settings_headers_legacy);
 				}
 			}
 			else
 			// For Android devices running any version below Honeycomb. 
 			{
 				addPreferencesFromResource(R.xml.settings_headers_legacy);
 			}
 
 			playersCheckersPieceColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
 			{
 				@Override
 				public boolean onPreferenceChange(final Preference preference, final Object newValue)
 				{
 					if (opponentsCheckersPieceColor == null)
 					{
 						opponentsCheckersPieceColor = (ListPreference) findPreference(getString(R.string.settings_key_opponents_checkers_piece_color));
 					}
 
 					return onPlayersCheckersPieceColorPreferenceChange(opponentsCheckersPieceColor, newValue);
 				}
 			});
 
 			opponentsCheckersPieceColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
 			{
 				@Override
 				public boolean onPreferenceChange(final Preference preference, final Object newValue)
 				{
 					if (playersCheckersPieceColor == null)
 					{
 						playersCheckersPieceColor = (ListPreference) findPreference(getString(R.string.settings_key_players_checkers_piece_color));
 					}
 
 					return onOpponentsCheckersPieceColorPreferenceChange(playersCheckersPieceColor, newValue);
 				}
 			});
 		}
 	}
 
 
 	@Override
 	public void onBuildHeaders(final List<Header> target)
 	// Called only when this Android device is running Honeycomb and above.
 	{
 		super.onBuildHeaders(target);
 
 		loadHeadersFromResource(R.xml.settings_headers, target);
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				onBackPressed();
 				break;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 
 
 	@Override
 	public boolean onPlayersCheckersPieceColorPreferenceChange(final ListPreference opponentsCheckersPieceColor, final Object newValue)
 	{
 		final String newPlayerColor = (String) newValue;
 		final String opponentsColor = opponentsCheckersPieceColor.getValue();
 
 		if (newPlayerColor.equalsIgnoreCase(opponentsColor))
 		{
			Utilities.easyToast(this, R.string.make_sure_that_you_dont_set_both_teams_color_to_the_same_thing_this_setting_has_been_reset_to_the_default);
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 
 	@Override
 	public boolean onOpponentsCheckersPieceColorPreferenceChange(final ListPreference playersCheckersPieceColor, final Object newValue)
 	{
 		final String newOpponentColor = (String) newValue;
 		final String playersColor = playersCheckersPieceColor.getValue();
 
 		if (newOpponentColor.equalsIgnoreCase(playersColor))
 		{
			Utilities.easyToast(this, R.string.make_sure_that_you_dont_set_both_teams_color_to_the_same_thing_this_setting_has_been_reset_to_the_default);
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 
 }
