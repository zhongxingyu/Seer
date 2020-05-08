 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.android.preference;
 
import java.util.Arrays;

 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.preference.ListPreference;
 import android.util.AttributeSet;
 
 /**
  * This class is a modified version of https://gist.github.com/cardil/4754571
  * Because we wish to support API levels below 11, so we need this class. 
  * 
  */
 public class MultiSelectListPreference extends ListPreference {
 
     private  boolean[] entryChecked;
 
     public MultiSelectListPreference(Context context, AttributeSet attributeSet) {
         super(context, attributeSet);
         entryChecked = new boolean[getEntries().length];
     }
 
     public MultiSelectListPreference(Context context) {
         this(context, null);
     }
 
     @Override
     protected void onPrepareDialogBuilder(Builder builder) {
         CharSequence[] entries = getEntries();
         CharSequence[] entryValues = getEntryValues();
         if (entries == null || entryValues == null
                 || entries.length != entryValues.length) {
             throw new IllegalStateException(
                     "MultiSelectListPreference requires an entries array and an entryValues "
                             + "array which are both the same length");
         }
 
         OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
             public void onClick(DialogInterface dialog, int which, boolean val) {
                 entryChecked[which] = val;
             }
         };
         
         builder.setMultiChoiceItems(entries, entryChecked, listener);
     }
 
     @Override
     protected void onDialogClosed(boolean positiveResult) {
     	if (positiveResult) {
     		callChangeListener(entryChecked);
     	}
     }
 
     public void setEntryChecked(boolean[] entryChecked) {
    	this.entryChecked = Arrays.copyOf(entryChecked, entryChecked.length);
     }
 
     public boolean[] getEntryChecked() {
    	return Arrays.copyOf(this.entryChecked, this.entryChecked.length);
     }
 }
