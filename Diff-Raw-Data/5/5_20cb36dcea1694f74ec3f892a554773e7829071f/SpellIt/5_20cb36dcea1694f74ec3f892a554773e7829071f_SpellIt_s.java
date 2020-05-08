 /*
  * @(#)SpellIt.java
  *
  * Copyright (c) 2008, Erik C. Thauvin (http://erik.thauvin.net/)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of the authors nor the names of its contributors may be
  * used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * $Id$
  *
  */
 package net.thauvin.erik.android.spellit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * <code>SpellIt</code> is a alphabet speller for Android.
  * 
  * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
  * @version $Revision$, $Date$
  * @created Oct 30, 2008
  * @since 1.0
  */
 public class SpellIt extends Activity
 {
 	private static final int MAX_HISTORY_SIZE = 15;
 	private static final int MENU_ABOUT = 0;
 	private static final int MENU_NATO = 1;
 	private static final String PREFS_HISTORY = "history";
 	private static final String PREFS_NAME = "SpellIt";
 	private static final String PREFS_NATO = "nato";
 
 	private String[] mAlphabet;
 	private int mCurrentChar = -1;
 	private String mEntry;
 	private final List<String> mHistory = new ArrayList<String>(MAX_HISTORY_SIZE);
 	private boolean mNato;
 
 	/**
 	 * Adds to the history.
 	 * 
 	 * @param entry The entry to add.
 	 */
 	private void addHistory(String entry)
 	{
 		if (!mHistory.contains(entry))
 		{
 			if (mHistory.size() >= MAX_HISTORY_SIZE)
 			{
 				mHistory.remove(0);
 			}
 
 			mHistory.add(entry);
 
 			savePrefs();
 		}
 	}
 
 	/**
 	 * Returns the specified phonetic alphabet value.
 	 * 
 	 * @param index The desired alphabet index.
 	 * @return The specified alphabet value.
 	 */
 	private String getAlphabet(int index)
 	{
 		return mAlphabet[index];
 	}
 
 	/**
 	 * Returns the current character count.
 	 * 
 	 * @return The current character count.
 	 */
 	private int getCurrentChar()
 	{
 		return mCurrentChar;
 	}
 
 	/**
 	 * Gets the current entry.
 	 * 
 	 * @return The new entry.
 	 */
 	private String getEntry()
 	{
 		return mEntry;
 	}
 
 	/**
 	 * Returns the current version number.
 	 * 
 	 * @return The current version number or empty.
 	 */
 	private String getVersionNumber()
 	{
 		try
 		{
 			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
 		}
 		catch (final NameNotFoundException e)
 		{
 			return "";
 		}
 	}
 
 	/**
 	 * Initializes the various controls.
 	 */
 	private void init()
 	{
 		final AutoCompleteTextView entryFld = (AutoCompleteTextView) findViewById(R.id.main_entry_fld);
 		final Button spellBtn = (Button) findViewById(R.id.main_spell_btn);
 		final TextView resultFld = (TextView) findViewById(R.id.main_result_fld);
 		final TextView tapLbl = (TextView) findViewById(R.id.main_tap_lbl);
 
 		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
 
 		setHistory(settings.getString(PREFS_HISTORY, ""));
 		setNato(settings.getBoolean(PREFS_NATO, false));
 		setAutoComplete(entryFld);
 		setAlphabet(isNato());
 
 		final ClipboardManager clip = (ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
 		if (clip.hasText())
 		{
 			entryFld.setText(clip.getText());
 		}
 
 		entryFld.setOnClickListener(new Button.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				spellBtn.performClick();
 			}
 		});
 
 		spellBtn.setOnClickListener(new Button.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				final String newEntry = entryFld.getText().toString();
 
 				if (!TextUtils.isEmpty(newEntry))
 				{
 					tapLbl.setText(R.string.main_tap_lbl_txt);
 
 					if (!newEntry.equals(getEntry()))
 					{
 						setEntry(newEntry);
 						setCurrentChar(0);
 						addHistory(getEntry());
 						setAutoComplete(entryFld);
 					}
 					showNextChar(resultFld, tapLbl);
 				}
 				else
 				{
 					Toast.makeText(SpellIt.this, R.string.main_entry_err_txt, Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 
 		resultFld.setOnClickListener(new Button.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				if (getCurrentChar() >= 0)
 				{
 					showNextChar(resultFld, tapLbl);
 				}
 			}
 		});
 
 		tapLbl.setOnClickListener(new Button.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				if (getCurrentChar() >= 0)
 				{
 					showNextChar(resultFld, tapLbl);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Returns the NATO flag.
 	 * 
 	 * @return The NATO flag.
 	 */
 	private boolean isNato()
 	{
 		return mNato;
 	}
 
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		init();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		menu.add(0, MENU_ABOUT, 0, R.string.about_menu_txt).setIcon(android.R.drawable.ic_menu_info_details);
 		menu.add(0, MENU_NATO, 0, R.string.nato_menu_txt);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		if (item.getItemId() == MENU_ABOUT)
 		{
 			final LayoutInflater factory = LayoutInflater.from(this);
 			final View aboutView = factory.inflate(R.layout.about, null);
 
 			new AlertDialog.Builder(this).setView(aboutView).setIcon(android.R.drawable.ic_dialog_info).setTitle(
 					getString(R.string.app_name) + ' ' + getVersionNumber()).setPositiveButton(R.string.alert_dialog_ok,
 					new DialogInterface.OnClickListener()
 					{
 						public void onClick(DialogInterface dialog, int whichButton)
 						{
 							// do nothing
 						}
 					}).show();
 
 			return true;
 		}
 		else if (item.getItemId() == MENU_NATO)
 		{
 			setAlphabet(toggleNato());
 			savePrefs();
 
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		super.onPrepareOptionsMenu(menu);
 
 		if (isNato())
 		{
 			menu.findItem(MENU_NATO).setIcon(android.R.drawable.presence_online);
 		}
 		else
 		{
 			menu.findItem(MENU_NATO).setIcon(android.R.drawable.presence_offline);
 		}
 
 		return true;
 	}
 
 	/**
 	 * Saves the preferences.
 	 */
 	private void savePrefs()
 	{
 		final SharedPreferences settings = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
 		final SharedPreferences.Editor editor = settings.edit();
 		editor.putString(PREFS_HISTORY, TextUtils.join(",", mHistory));
 		editor.putBoolean(PREFS_NATO, isNato());
 		editor.commit();
 	}
 
 	/**
 	 * Set the alphabet.
 	 * 
 	 * @param isNato The NATO flag.
 	 */
 	private void setAlphabet(Boolean isNato)
 	{
 		if (isNato)
 		{
 			mAlphabet = getResources().getStringArray(R.array.nato_alphabet);
 		}
 		else
 		{
 			mAlphabet = getResources().getStringArray(R.array.alphabet);
 		}
 	}
 
 	/**
 	 * Sets the auto-complete values of the specified field.
 	 * 
 	 * @param field The field to the auto-complete for.
 	 */
 	private void setAutoComplete(AutoCompleteTextView field)
 	{
 		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, mHistory);
 		field.setAdapter(adapter);
 	}
 
 	/**
 	 * Sets the current character count.
 	 * 
 	 * @param count The current character count to set.
 	 */
 	private void setCurrentChar(int count)
 	{
 		mCurrentChar = count;
 	}
 
 	/**
 	 * Sets the current entry.
 	 * 
 	 * @param entry The entry to set.
 	 */
 	private void setEntry(String entry)
 	{
 		mEntry = entry;
 	}
 
 	/**
 	 * Sets the history.
 	 * 
 	 * @param history The comma-delimited history string.
 	 */
 	private void setHistory(String history)
 	{
 		final String[] entries = TextUtils.split(history, ",");
 		for (final String entry : entries)
 		{
 			mHistory.add(entry);
 		}
 	}
 
 	/**
 	 * Sets the NATO flag.
 	 * 
 	 * @param nato The new NATO flag.
 	 */
 	private void setNato(boolean isNato)
 	{
 		mNato = isNato;
 	}
 
 	private void showNextChar(TextView view, TextView label)
 	{
 		if (getCurrentChar() < getEntry().length())
 		{
 			final char c = getEntry().charAt(getCurrentChar());
 
			if ((c < 123) && (c > 96))
 			{
 				int offset = 97;
 
 				if ((c < 91) && (c > 64))
 				{
 					offset = 65;
 				}
 
 				view.setText(String.valueOf(c).toUpperCase() + ' ' + getString(R.string.as_in_txt) + ' ' + getAlphabet(c - offset));
			}
 			else if ((c < 58) && (c > 47))
 			{
 				view.setText(getAlphabet(c - 22));
 			}
 			else if (c == ' ')
 			{
 				view.setText(getString(R.string.space_txt));
 			}
 			else
 			{
 				view.setText(String.valueOf(c));
 			}
 
 			setCurrentChar(getCurrentChar() + 1);
 		}
 		else
 		{
 			view.setText(getString(R.string.done_txt));
 			label.setText(getString(R.string.main_tap_lbl_repeat_txt));
 			setCurrentChar(0);
 		}
 	}
 
 	/**
 	 * Toggles the NATO flag.
 	 * 
 	 * @return The NATO flag value.
 	 */
 	private boolean toggleNato()
 	{
 		mNato ^= true;
 
 		return mNato;
 	}
 
 }
