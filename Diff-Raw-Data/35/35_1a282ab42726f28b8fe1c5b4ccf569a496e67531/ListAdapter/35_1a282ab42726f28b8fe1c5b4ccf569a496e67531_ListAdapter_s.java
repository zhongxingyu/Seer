 /*
 * Copyright (C) 2013 Juan Pons (see README for details)
 * This file is part of miAulaVirtual.
 *
 * miAulaVirtual is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * miAulaVirtual is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with miAulaVirtual. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */
 
 package com.jp.miaulavirtual;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.preference.PreferenceManager;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Creamos nuestro propio adaptador para la lista con la siguiente clase: AdaptadorDocs
  */
 
 class ListAdapter extends BaseAdapter {
     
     Activity context;
     String[] names;
     String[] types;
 
     public ListAdapter(Activity context,  String[] names, String[] types) {
     	 super();
          this.context = context;
          this.names = new String[names.length];
          System.arraycopy(names, 0, this.names, 0, names.length);
          this.types = new String[types.length];
          System.arraycopy(types, 0, this.types, 0, types.length);
         
     }
      
     public int getCount() {
     	if(names==null) return 1;
         return names.length;
     }
     
     public Object getItem(int position) {
         return position;
     }
     
     public long getItemId(int position) {
         return position;
     }
     
     public void clearData() {
         // clear the data
         names = null;
         types = null;
     }
     
 	private void setRestrictedOrientation() {
 		/* We don't want change screen orientation */
 	    //---get the current display info---
 	    WindowManager wm = context.getWindowManager();
 	    Display d = wm.getDefaultDisplay();
 	    if (d.getWidth() > d.getHeight()) {
 	    	//---change to landscape mode---
 	    	context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
 	    } else {
 	    	//---change to portrait mode---
 	    	context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
 	    }
 	}
     
 	public View getView(int position, View convertView, ViewGroup parent) {
     	View item;
     	if(names!=null) {
     	LayoutInflater inflater = context.getLayoutInflater();
     	
         item = inflater.inflate(R.layout.list_docs, null);
         item.setMinimumHeight(65);  
         item.setPadding(14, 0, 6, 0);
         TextView title;
         ImageView image;
         int ico;
         // Title
 	    title = (TextView)item.findViewById(R.id.list_title);
 	    String rgxTitle;
 	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); // Obtenemos las preferencias
 	    
 	    // Image
 	    image = (ImageView)item.findViewById(R.id.folderImage);
 	    switch(Integer.parseInt(types[position].toString())) {
 	    case 0: // Back button
 	    	rgxTitle = names[position].toString().replaceAll( "\\d{4}-\\d{4}\\s|\\d{4}-\\d{2}\\s|Documentos\\sde\\s?|Gr\\..+?\\s|\\(.+?\\)", "" );
 	    	title.setText(rgxTitle.trim());
 	    	ico = context.getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_back", null, null); // Back ico
 	    	image.setImageResource(ico);
 	    	break;
 	    case 1: // Folder
 	    	rgxTitle = names[position].toString().replaceAll( "\\d{4}-\\d{4}\\s|\\d{4}-\\d{2}\\s|Documentos\\sde\\s?|Gr\\..+?\\s|\\(.+?\\)", "" );
 	    	title.setText(rgxTitle.trim());
 	    	ico = context.getResources().getIdentifier("com.jp.miaulavirtual:drawable/icon_folder", null, null); // Folder ico
 	    	image.setImageResource(ico);
 	    	break;
 	    case 2: // PDF
 	    	rgxTitle = names[position].toString();
 	    	title.setText(rgxTitle.trim());
 	    	ico = context.getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_pdf", null, null); // PDF ico
 	    	image.setImageResource(ico);
 	    	break;	    	
 	    case 3: // Excel
 	    	rgxTitle = names[position].toString();
 	    	title.setText(rgxTitle.trim());
 	    	ico = context.getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_excel", null, null); // Excel ico
 	    	image.setImageResource(ico);
 	    	break;
 	    case 4: // Power Point
 	    	rgxTitle = names[position].toString();
 	    	title.setText(rgxTitle.trim());
 	    	ico = context.getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_ppt", null, null); // PPT ico
 	    	image.setImageResource(ico);
 	    	break;
 	    case 5: // Word
 	    	rgxTitle = names[position].toString();
 	    	title.setText(rgxTitle.trim());
 	    	ico = context.getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_word", null, null); // Word ico
 	    	image.setImageResource(ico);
 	    	break;
 	    case 6: // Communities and others
 	    	rgxTitle = names[position].toString().replaceAll( "\\d{4}-\\d{4}\\s|\\d{4}-\\d{2}\\s|Documentos\\sde\\s?|Gr\\..+?\\s|\\(.+?\\)", "" );
 	    	title.setText(rgxTitle.trim());
 	    	ico = context.getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_comu", null, null); // Word ico
 	    	image.setImageResource(ico);
 	    	break;
 	    default: // Default
 	    	rgxTitle = names[position].toString();
 	    	title.setText(rgxTitle);
 	    	ico = context.getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_def", null, null); // Other ico
 	    	image.setImageResource(ico);
 	    	break;
 	    }
 	    if(!prefs.getBoolean("pattern", true)) title.setText(names[position].toString());
         
         /*
          * Trick: When we clear the List (because user click any subject) we change the item count to 1 and we load the loading page (load_list) with restricted orientation.
          */
     	} else { 			
     		setRestrictedOrientation();
     
     		LayoutInflater inflater = context.getLayoutInflater();
     	    item = inflater.inflate(R.layout.load_list, null);
     	    item.setMinimumHeight(35);
     	}
         return(item);
     }
 }
