 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.jp.helper;
 
 import it.sayservice.platform.smartplanner.data.message.Position;
 import it.sayservice.platform.smartplanner.data.message.RType;
 import it.sayservice.platform.smartplanner.data.message.TType;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.Toast;
 import eu.trentorise.smartcampus.jp.Config;
 import eu.trentorise.smartcampus.jp.R;
 import eu.trentorise.smartcampus.jp.custom.UserPrefsHolder;
 
 public class PrefsHelper {
 
 	public static void buildUserPrefsView(Context ctx, UserPrefsHolder userPrefsHolder, View view) {
 
 		TableLayout tTypesTableLayout = (TableLayout) view.findViewById(R.id.transporttypes_table);
 		tTypesTableLayout.removeAllViews(); // prevents duplications
 		RadioGroup rTypesRadioGroup;
 
 		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 
 		tTypesTableLayout.setShrinkAllColumns(true);
 
 		TableRow tableRow = new TableRow(ctx);
 		for (int tCounter = 0; tCounter < Config.TTYPES_ALLOWED.length; tCounter++) {
 			TType tType = Config.TTYPES_ALLOWED[tCounter];
 
 			if ((tCounter > 0 && tCounter % 2 == 0)) {
 				tTypesTableLayout.addView(tableRow);
 				tableRow = new TableRow(ctx);
 				tableRow.setGravity(Gravity.CENTER_VERTICAL);
 				tableRow.setLayoutParams(params);
 			}
 
 			List<TType> tTypesList = Arrays.asList(userPrefsHolder.getTransportTypes());
 			CheckBox cb = new CheckBox(ctx);
 			cb.setText(Utils.getTTypeUIString(ctx, tType));
 			cb.setTextColor(ctx.getResources().getColor(android.R.color.black));
 			cb.setTag(tType);
 			cb.setChecked(tTypesList.contains(tType));
 
 			tableRow.addView(cb);
 		}
 		//put the last row in the layout
 		tTypesTableLayout.addView(tableRow);
 		tableRow = new TableRow(ctx);
 		tableRow.setGravity(Gravity.CENTER_VERTICAL);
 		tableRow.setLayoutParams(params);
 
 		rTypesRadioGroup = (RadioGroup) view.findViewById(R.id.routetypes_radioGroup);
 		rTypesRadioGroup.removeAllViews(); // prevent duplications
 
 		for (RType rType : Config.RTYPES_ALLOWED) {
 
 			RadioButton rb = new RadioButton(ctx);
 			rb.setText(Utils.getRTypeUIString(ctx, rType));
 			rb.setTextColor(ctx.getResources().getColor(android.R.color.black));
 			rb.setTag(rType);
 			rTypesRadioGroup.addView(rb);
 			if (rType.equals(userPrefsHolder.getRouteType())) {
 				rTypesRadioGroup.check(rb.getId());
 			}
 
 		}
 
 	}
 
 	public static void buildSavePrefsBtnFromViews(final Context ctx, final SharedPreferences userPrefs, Button btn,
 			final TableLayout tTypesTableLayout, final RadioGroup rTypesRadioGroup) {
 		btn.setOnClickListener(new Button.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				UserPrefsHolder userPrefsHolder = PrefsHelper.userPrefsViews2Holder(tTypesTableLayout, rTypesRadioGroup,
 						userPrefs);
 
 				SharedPreferences.Editor prefsEditor = userPrefs.edit();
 
 				// transport types
 				List<TType> tTypesList = Arrays.asList(userPrefsHolder.getTransportTypes());
 				for (TType tType : Config.TTYPES_ALLOWED) {
 					prefsEditor.putBoolean(tType.toString(), tTypesList.contains(tType));
 				}
 
 				// route types
 				prefsEditor.putString(Config.USER_PREFS_RTYPE, userPrefsHolder.getRouteType().toString());
 
 				boolean success = prefsEditor.commit();
 				if (success) {
 					Toast toast = Toast.makeText(ctx, R.string.toast_prefs_saved, Toast.LENGTH_SHORT);
 					toast.show();
 				}
 			}
 		});
 	}
 
 	public static UserPrefsHolder sharedPreferences2Holder(SharedPreferences userPrefs) {
 		RType rType = RType.valueOf(userPrefs.getString(Config.USER_PREFS_RTYPE, RType.fastest.toString()));
 		List<TType> tTypesList = new ArrayList<TType>();
 
 		for (TType tType : Config.TTYPES_ALLOWED) {
 			if (userPrefs.getBoolean(tType.toString(), false)) {
 				tTypesList.add(tType);
 			}
 		}
		//add by default transit if the preferences are not set
		if (tTypesList.size()==0)
			tTypesList.addAll(Config.TRANSIT_SET);
 		String addressString = userPrefs.getString(Config.USER_PREFS_FAVORITES, null);
 		List<Position> list = new ArrayList<Position>();
 
 		if (addressString != null) {
 			list = eu.trentorise.smartcampus.android.common.Utils.convertJSONToObjects(addressString, Position.class);
 		}
 
 		return new UserPrefsHolder(list, rType, tTypesList.toArray(new TType[] {}));
 	}
 
 	public static UserPrefsHolder userPrefsViews2Holder(TableLayout tTypesTableLayout, RadioGroup rTypesRadioGroup,
 			SharedPreferences userPrefs) {
 		// route types
 		int checkedId = rTypesRadioGroup.getCheckedRadioButtonId();
 		RadioButton rb = (RadioButton) rTypesRadioGroup.findViewById(checkedId);
 		RType rbTType = (RType) rb.getTag();
 
 		// transport types
 		List<TType> tTypesList = new ArrayList<TType>();
 		for (int pos = 0; pos < tTypesTableLayout.getChildCount(); pos++) {
 			TableRow tr = (TableRow) tTypesTableLayout.getChildAt(pos);
 			for (int trPos = 0; trPos < tr.getChildCount(); trPos++) {
 				CheckBox cb = (CheckBox) tr.getChildAt(trPos);
 				TType itemTType = (TType) cb.getTag();
 				Set<TType> setOfttype = new HashSet<TType>(Config.TTYPES_MAPPED.get(itemTType));
 				if (cb.isChecked()) {
 					//add the set choosed
 					tTypesList.addAll(setOfttype);
 				}
 			}
 		}
 		String addressString = userPrefs.getString(Config.USER_PREFS_FAVORITES, null);
 		List<Position> list = new ArrayList<Position>();
 
 		if (addressString != null) {
 			list = eu.trentorise.smartcampus.android.common.Utils.convertJSONToObjects(addressString, Position.class);
 		}
 
 		return new UserPrefsHolder(list, rbTType, tTypesList.toArray(new TType[] {}));
 	}
 
 }
