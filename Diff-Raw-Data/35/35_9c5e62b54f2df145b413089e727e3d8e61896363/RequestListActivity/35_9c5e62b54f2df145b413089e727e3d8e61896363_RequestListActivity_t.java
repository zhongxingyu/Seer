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
   along with this program. If not, see <http://www.gnu.org/licenses/>.
   
   Diff colouring concept from Hubroid - A GitHub app for Android
   
   Copyright (c) 2010 Eddie Ringle <eddie@eringle.net>.
   All rights reserved.
  
   Redistribution and use in source and binary forms, with or
   without modification, are permitted provided that the following
   conditions are met:
    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.
    * Neither the name of Idlesoft nor the names of
      its contributors may be used to endorse or promote
      products derived from this software without specific
      prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
   CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
   EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
 
 package org.opensuse.android.obs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import org.opensuse.android.obs.data.Request;
 
 public class RequestListActivity extends ListActivity {
 	private ProgressDialog progressDialog = null; 
     private List<Request> requests = null;
     private ArrayAdapter<Request> adapter;
     private Runnable viewRequests;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         requests = new ArrayList<Request>();
         this.adapter = new RequestListAdapter(this, android.R.layout.simple_list_item_1, requests);
         setListAdapter(this.adapter);
 
         viewRequests = new Runnable(){
             @Override
             public void run() {
                 getRequests();
             }
         };
         Thread thread =  new Thread(null, viewRequests, getClass().getSimpleName() + "//retrieve_requests");
         thread.start();
         progressDialog = ProgressDialog.show(RequestListActivity.this,    
               "Please wait...", "Retrieving data ...", true);
               
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
       super.onListItemClick(l, v, position, id);
       Request req = requests.get(position);
       Bundle extras = new Bundle();
       extras.putString("id", req.getId());
       Intent intent = new Intent(RequestListActivity.this, RequestActivity.class);
       intent.putExtras(extras);
       startActivity(intent);
     }
 
     
     private Runnable returnRes = new Runnable() {
 
         @Override
         public void run() {
             if(requests != null && requests.size() > 0){
                 adapter.notifyDataSetChanged();
                 for(int i=0;i<requests.size();i++)
                 adapter.add(requests.get(i));
             }
             progressDialog.dismiss();
             adapter.notifyDataSetChanged();
         }
     };
     
 	private void getRequests(){
         try{
             requests = new ArrayList<Request>();
             Client client = new Client(this);
             requests = client.getMyRequests();
             Log.i(getClass().getSimpleName(), requests.size() + " requests");
          } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Can't retrieve requests", e);
           }
           runOnUiThread(returnRes);
       }
 	
 	  
 }
