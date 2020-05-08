 package com.moulis.eleonore.activities;
 
 import java.text.DateFormat;
 import java.util.List;
 import java.util.Locale;
 
 import com.moulis.eleonore.R;
 import com.moulis.eleonore.controllers.EleonoreMainController;
 import com.moulis.eleonore.model.Diet;
 import com.moulis.eleonore.model.DietStep;
 import com.moulis.eleonore.units.EleonoreCurrentUnit;
 import com.moulis.eleonore.units.EleonoreUnit;
 import com.moulis.eleonore.units.EleonoreUnitsConverter;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.Gravity;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 /**
  *   Copyright (C) 2012  MOULIS Marius <moulis.marius@gmail.com>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public class EleonoreHistoryActivity extends Activity 
 {
 	private Diet diet;
 	private TableLayout tableLayout;
 	private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
 	private final EleonoreUnitsConverter converter = EleonoreUnitsConverter.INSTANCE;
 	private final EleonoreUnit currentUnit = EleonoreCurrentUnit.getCurrentUnit();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);         
         setContentView(R.layout.history);
         
         initializeDiet();
         retrieveTable();
         setTableContent();
        
     }
     
 	private void initializeDiet()
 	{
 		this.diet = (Diet) getIntent().getSerializableExtra(EleonoreMainController.DIET_STATISTICS_DATA);
 	}
 	
 	private void retrieveTable()
 	{
 		this.tableLayout = (TableLayout) findViewById(R.id.historyActivityTable);
 	}
 	
 	private void setTableContent()
 	{
 		List<DietStep> dietSteps = diet.getDiet();
 		
 		for (int i = 0 ; i < dietSteps.size() ; i++)
 		{
 			TableRow row = buildRow(dietSteps.get(i));
 			if (i == 0)
 				addDefaultIdiomToRow(row);
 			else
 				addIdiomToRow(row, dietSteps.get(i-1), dietSteps.get(i));
 			tableLayout.addView(row);
 		}
 	}
 	
 	private void addIdiomToRow(TableRow row, DietStep step1, DietStep step2)
 	{
 		ImageView imageView = new ImageView(this);
 		double step1Weight = step1.getWeight();
 		double step2Weight = step2.getWeight();
 		double delta = step1Weight - step2Weight;
 		if (delta == 0)
 			imageView.setImageResource(R.drawable.constant);
 		else if (delta > 0)
 			imageView.setImageResource(R.drawable.decrease);
 		else 
 			imageView.setImageResource(R.drawable.increase);
 		
 		row.addView(imageView);
 			
 	}
 	
 	private void addDefaultIdiomToRow(TableRow row)
 	{
 		ImageView imageView = new ImageView(this);
 		imageView.setImageResource(R.drawable.constant);
 		row.addView(imageView);
 	}
 
 	private TableRow buildRow(DietStep step)
 	{
 		TableRow row = new TableRow(this);
 		row.setGravity(Gravity.CENTER);
 		TextView dateTextView = buildDateTextView(step);
 		TextView weightTextView = buildWeightTextView(step);
 		
 		row.addView(dateTextView);
 		row.addView(weightTextView);
 		
 		return row;
 	}
 	
 	private TextView buildDateTextView(DietStep step)
 	{
 		TextView textView = new TextView(this);
 		String date = dateFormat.format(step.getDate());
 		textView.setText(date);
		textView.setTextSize(convertDpToPixels(13));
 		
 		return textView;
 	}
 	
 	private TextView buildWeightTextView(DietStep step)
 	{
 		TextView textView = new TextView(this);
 		double weight = converter.convertFromKilograms(currentUnit, step.getWeight());
 		String text = converter.formatValue(weight) + " " + currentUnit + " ";
 		textView.setText(text);
		textView.setTextSize(convertDpToPixels((float)12.5));
 		textView.setGravity(Gravity.RIGHT);
 		
 		return textView;		
 	}
 	
 	private float convertDpToPixels(float dp)
 	{
 		DisplayMetrics metrics = getResources().getDisplayMetrics();
 		int pixels = (int) (metrics.density * dp + 0.5f);
 		return pixels;
 	}
 
 }
