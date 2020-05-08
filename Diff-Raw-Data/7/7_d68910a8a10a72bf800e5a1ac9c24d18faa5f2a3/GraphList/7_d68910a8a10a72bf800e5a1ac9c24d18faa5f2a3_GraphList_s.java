 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at 
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1.fragments;
 
 import java.util.ArrayList;
 
 import uk.co.jwlawson.nof1.BuildConfig;
 import uk.co.jwlawson.nof1.Keys;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 
 /**
  * List of all questions
  * 
  * @author John Lawson
  * 
  */
 public class GraphList extends FragList {
 	private static final String TAG = "GraphList";
 	private static final boolean DEBUG = true && BuildConfig.DEBUG;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		SharedPreferences sp = getActivity().getSharedPreferences(Keys.QUES_NAME, Context.MODE_PRIVATE);
 
 		ArrayList<String> list = new ArrayList<String>();
 		for (int i = 0; sp.contains(Keys.QUES_TEXT + i); i++) {
 			list.add(sp.getString(Keys.QUES_TEXT + i, "No question found!"));
 		}
 
 		setArrayList(list);
 
 		if (DEBUG) Log.d(TAG, "List created");
 	}
 
 }
