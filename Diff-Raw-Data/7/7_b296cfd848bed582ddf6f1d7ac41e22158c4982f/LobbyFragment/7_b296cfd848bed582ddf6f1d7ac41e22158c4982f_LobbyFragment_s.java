 /*
  * Copyright (C) 2012 http://andlabs.eu
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.andlabs.studiolounge;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import eu.andlabs.studiolounge.gcp.GCPService;
 import eu.andlabs.studiolounge.gcp.Lounge;
 import eu.andlabs.studiolounge.gcp.Lounge.LobbyListener;
 
 public class LobbyFragment extends Fragment implements LobbyListener {
 	private ArrayList<Player> mPlayers = new ArrayList<Player>();
 	private ListView lobbyList;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    Log.i("Lounge", "LobbyFragment on CREATE");
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 
     @Override
     public void onStart() {
         Log.i("Lounge", "LobbyFragment on START");
         ((LoungeActivity)getActivity()).mLounge.register(this);
         mPlayers.clear();
         super.onStart();
     }
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater,
 			ViewGroup container, Bundle savedInstanceState) {
 
 		View lobby = inflater.inflate(R.layout.lobby, container, false);
 		lobby.findViewById(R.id.btn_host).setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
 
                 ((LoungeActivity)getActivity()).mLounge.hostGame();
             }
         });
 		((ListView) lobby.findViewById(R.id.list))
 				.setAdapter(new BaseAdapter() {
 
 					@Override
 					public int getCount() {
 						return mPlayers.size();
 					}
 
 					@Override
 					public View getView(final int position, View view,
 							ViewGroup parent) {
 						if (view == null)
 							view = inflater.inflate(R.layout.lobby_list_entry, null);
 						final TextView playerLabel = (TextView) view
 								.findViewById(R.id.playername);
 						final Player player = mPlayers.get(position);
 						playerLabel.setText(player.getPlayername());
 						Button b = (Button) view.findViewById(R.id.joinbtn);
 						if (player.getHostedGame() != null) {
 							b.setText(player.getHostedGame().split("\\.")[4]);
 							b.setVisibility(View.VISIBLE);
 							b.setOnClickListener(new OnClickListener() {
 
 										@Override
 										public void onClick(View v) {
 										    ((LoungeActivity)getActivity()).mLounge
 									            .joinGame(player.getPlayername()
 									                , player.getHostedGame());
 									        launchGameApp(player.getHostedGame());										}
 									});
 						} else {
 						    b.setVisibility(View.GONE);
 						}
 						return view;
 					}
 
 					@Override
 					public long getItemId(int position) {
 						return 0;
 					}
 
 					@Override
 					public Object getItem(int position) {
 						return null;
 					}
 				});
 
 		lobbyList = (ListView) lobby.findViewById(R.id.list);
 		return lobby;
 	}
 
 
 	@Override
 	public void onPlayerLoggedIn(String player) {
 //		Toast.makeText(getActivity(), player + " joined", 3000).show();
 		mPlayers.add(new Player(player));
 		((BaseAdapter) lobbyList.getAdapter()).notifyDataSetChanged();
 	}
 
 	@Override
 	public void onPlayerLeft(String player) {
 		Toast.makeText(getActivity(), player + " left", 3000).show();
 	}
 
 	@Override
 	public void onNewHostedGame(String player, String hostedGame) {
 		for (Player p : mPlayers) {
 			if (p.getPlayername().equals(player)) {
 				p.setHostedGame(hostedGame);
 				((BaseAdapter) lobbyList.getAdapter()).notifyDataSetChanged();
 			}
 		}
 
 	}
 
 
 	@Override
 	public void onPlayerJoined(String player) {
 		if (!player.equals(GCPService.mName))
 		    launchGameApp(getActivity().getPackageName());
 	}
 	
 	private void launchGameApp(String pkgName) {
		pkgName = "de.andlabs.gravitywins";
 	    PackageManager pm = getActivity().getPackageManager();
 	    Intent i = new Intent(Intent.ACTION_MAIN);
 	    i.addCategory("eu.andlabs.lounge");
 	    List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
 	    
 	    for(ResolveInfo info:list){
 	        Intent launch = new Intent(Intent.ACTION_MAIN);
 	        Log.i("debug", "found package "+info.activityInfo.packageName);
 	        if(info.activityInfo.packageName.equalsIgnoreCase(pkgName)){
 	            Log.i("debug", "Packge Match found");
 	            launch.setComponent(new ComponentName(
 	                    info.activityInfo.packageName, info.activityInfo.name));
 	        } else {
 	            Log.i("debug", "NO Package Match");
 	            launch = pm.getLaunchIntentForPackage(pkgName);
 	        }
 	        startActivity(launch);
 	    }
 	}
 
     @Override
     public void onStop() {
         ((LoungeActivity)getActivity()).mLounge.unregister(this);
         super.onStop();
     }
 }
