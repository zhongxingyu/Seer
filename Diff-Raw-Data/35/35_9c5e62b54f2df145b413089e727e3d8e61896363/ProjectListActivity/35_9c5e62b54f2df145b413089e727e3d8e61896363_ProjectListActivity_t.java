 /* 
   Copyright 2010 Duncan Mac-Vicar P. <dmacvicar@suse.de>
  
   This file is part of the openSUSE Build Service Android Client.
 
   openSUSE Build Service Android Client is free software: you
   can  redistribute it and/or modify it under the terms of the GNU
   General Public License as published by the Free Software Foundation,
   either version 3 of the License, or (at your option) any later
   version.
 
   openSUSE Build Service Android Client is distributed in the hope
   that it will be useful, but WITHOUT ANY WARRANTY; without even the
   implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.opensuse.android.obs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.opensuse.android.obs.data.Project;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class ProjectListActivity extends ListActivity {
 	private ProgressDialog progressDialog = null; 
     private List<Project> projects = null;
     private ArrayAdapter<Project> adapter;
     private Runnable viewProjects;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         projects = new ArrayList<Project>();
         this.adapter = new ProjectAdapter(this, android.R.layout.simple_list_item_1, projects);
         setListAdapter(this.adapter);
 
         viewProjects = new Runnable(){
             @Override
             public void run() {
                 getProjects();
             }
         };
         Thread thread =  new Thread(null, viewProjects, getClass().getSimpleName() + "//retrieve_projects");
         thread.start();
         progressDialog = ProgressDialog.show(ProjectListActivity.this,    
               "Please wait...", "Retrieving data ...", true);
               
     }
     private Runnable returnRes = new Runnable() {
 
         @Override
         public void run() {
             if(projects != null && projects.size() > 0){
                 adapter.notifyDataSetChanged();
                 for(int i=0;i<projects.size();i++)
                 adapter.add(projects.get(i));
             }
             progressDialog.dismiss();
             adapter.notifyDataSetChanged();
         }
     };
     
 	private void getProjects(){
         try{
             projects = new ArrayList<Project>();
             Client client = new Client(this);
             projects = client.getProjectIds();
             Log.i(getClass().getSimpleName(), projects.size() + " projects");
           } catch (Exception e) { 
        	Log.e(getClass().getSimpleName(), "Can't retrieve projects", e);
           }
           runOnUiThread(returnRes);
       }
 	
 	  private class ProjectAdapter extends ArrayAdapter<Project> {
 
         private List<Project> items;
 
         public ProjectAdapter(Context context, int textViewResourceId, List<Project> items) {
                 super(context, textViewResourceId, items);
                 this.items = items;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
                 View v = convertView;
                 if (v == null) {
                     LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     v = vi.inflate(android.R.layout.simple_list_item_1, null);
                 }
                 Project p = items.get(position);
                 if (p != null) {
                         TextView tt = (TextView) v.findViewById(android.R.id.text1);
                         if (tt != null) {
                               tt.setText(p.getName());                            
                         }
                 }
                 return v;
         }
 }
 }
