 // 
 //  AndroidWaveActivityListAdapter.java
 //  AndroidWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-04-27.
 //  Copyright 2011 University of California, Berkeley. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waveui;
 
 import edu.berkeley.androidwave.R;
 import edu.berkeley.androidwave.waverecipe.WaveRecipe;
 import edu.berkeley.androidwave.waverecipe.WaveRecipeAuthorization;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.database.DataSetObserver;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
import java.util.ArrayList;
 
 /**
  * Custom ListAdapter for displaying WaveRecipeAuthorizations in the
  * AndroidWaveActivity
  */
 public class AndroidWaveActivityListAdapter extends ArrayAdapter<WaveRecipeAuthorization> {
     
     private static final String TAG = AndroidWaveActivityListAdapter.class.getSimpleName();
     
    private ArrayList<WaveRecipeAuthorization> items;
     
    public AndroidWaveActivityListAdapter(Context context, int textViewResourceId, ArrayList<WaveRecipeAuthorization> items) {
         super(context, textViewResourceId, items);
         this.items = items;
     }
     
     /**
      * android.widget.ArrayAdapter methods
      */
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         // load our view template
         if (v == null) {
             LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.main_authorization_cell, null); // second arg should be parent?
         }
         // now connect up the view
         WaveRecipeAuthorization auth = items.get(position);
         if (auth != null) {
             TextView recipeNameTextView = (TextView) v.findViewById(R.id.recipe_name);
             TextView recipeIdTextView = (TextView) v.findViewById(R.id.recipe_id);
             TextView clientNameTextView = (TextView) v.findViewById(R.id.client_name);
             TextView clientPackageTextView = (TextView) v.findViewById(R.id.client_package);
             
             WaveRecipe recipe = auth.getRecipe();
             assert recipe != null;
             
             recipeNameTextView.setText("Recipe: "+recipe.getName());
             recipeIdTextView.setText(recipe.getId());
             
             ComponentName clientName = auth.getRecipeClientName();
             String clientLabel = clientName.getShortClassName();
             PackageManager pm = getContext().getPackageManager();
             try {
                 ActivityInfo aInfo = pm.getActivityInfo(clientName, 0);   // may need flag PackageManager.GET_META_DATA
                 clientLabel = "App: "+aInfo.loadLabel(pm);
             } catch (PackageManager.NameNotFoundException nnfe) {
                 Log.d(TAG, "NameNotFoundException while getting info for calling activity", nnfe);
             }
             clientNameTextView.setText(clientLabel);
             clientPackageTextView.setText(clientName.getPackageName());
         }
         return v;
     }
 }
