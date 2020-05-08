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
 
 import org.opensuse.android.obs.data.Distribution;
 
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
 
 public class DistributionListActivity extends ListActivity {
 	private ProgressDialog progressDialog = null; 
     private List<Distribution> distributions = null;
     private ArrayAdapter<Distribution> adapter;
     private Runnable viewDistributions;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         distributions = new ArrayList<Distribution>();
         this.adapter = new DistributionAdapter(this, android.R.layout.simple_list_item_1, distributions);
         setListAdapter(this.adapter);
 
         viewDistributions = new Runnable(){
             @Override
             public void run() {
                 getDistributions();
             }
         };
         Thread thread =  new Thread(null, viewDistributions, getClass().getSimpleName() + "//retrieve_distributions");
         thread.start();
         progressDialog = ProgressDialog.show(DistributionListActivity.this,    
               "Please wait...", "Retrieving data ...", true);
               
     }
     private Runnable returnRes = new Runnable() {
 
         @Override
         public void run() {
             if(distributions != null && distributions.size() > 0){
                 adapter.notifyDataSetChanged();
                 for(int i=0;i<distributions.size();i++)
                 adapter.add(distributions.get(i));
             }
             progressDialog.dismiss();
             adapter.notifyDataSetChanged();
         }
     };
     
 	private void getDistributions(){
         try{
             distributions = new ArrayList<Distribution>();
             Client client = new Client(this);
             distributions = client.getDistributions();
             Log.i(getClass().getSimpleName(), distributions.size() + " distributions");
           } catch (Exception e) { 
            Log.e(getClass().getSimpleName(), e.getMessage());
           }
           runOnUiThread(returnRes);
       }
 	
 	  private class DistributionAdapter extends ArrayAdapter<Distribution> {
 
         private List<Distribution> items;
 
         public DistributionAdapter(Context context, int textViewResourceId, List<Distribution> items) {
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
                 Distribution dist = items.get(position);
                 if (dist != null) {
                         TextView tt = (TextView) v.findViewById(android.R.id.text1);
                         if (tt != null) {
                               tt.setText(dist.getName());
                         }
                 }
                 return v;
         }
 }
 }
