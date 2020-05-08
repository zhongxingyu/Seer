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
 
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
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
 import eu.andlabs.studiolounge.gcp.Lounge;
 import eu.andlabs.studiolounge.gcp.Lounge.LobbyListener;
 
 public class LobbyFragment extends Fragment implements LobbyListener {
 	private ArrayList<Player> mPlayers = new ArrayList<Player>();
 	private ListView lobbyList;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater,
 			ViewGroup container, Bundle savedInstanceState) {
 
 		View lobby = inflater.inflate(R.layout.lobby, container, false);
 		lobby.findViewById(R.id.btn_host).setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 Lounge.getInstance(getActivity()).hostGame();
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
 						if (player.getHostedGame() != null &&
 						        !player.getPlayername().equals(Lounge
 						                .getInstance(getActivity()).getName())) {
 							Button b = (Button) view.findViewById(R.id.joinbtn);
 							b.setText(mPlayers.get(position).getHostedGame());
 							b.setVisibility(View.VISIBLE);
 							b.setOnClickListener(new OnClickListener() {
 
 										@Override
 										public void onClick(View v) {
 									        Lounge.getInstance(getActivity())
 									            .joinGame(player.getPlayername()
 									                , player.getHostedGame());
 									        launchGameApp(player.getHostedGame());										}
 									});
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
 
 		Lounge.getInstance(getActivity()).register(this);
 		lobbyList = (ListView) lobby.findViewById(R.id.list);
 		return lobby;
 	}
 
 	@Override
 	public void onPlayerLoggedIn(String player) {
 		Toast.makeText(getActivity(), player + " joined", 3000).show();
 		mPlayers.add(new Player(player));
 		((BaseAdapter) lobbyList.getAdapter()).notifyDataSetChanged();
 	}
 
 	@Override
 	public void onPlayerLeft(String player) {
 		Toast.makeText(getActivity(), player + " left", 3000).show();
 	}
 
 	@Override
 	public void onNewHostedGame(String player, String hostedGame) {
 		Log.i("io.socket", player);
 		for (Player p : mPlayers) {
 			if (p.getPlayername().equals(player)) {
 				p.setHostedGame(hostedGame);
 				((BaseAdapter) lobbyList.getAdapter()).notifyDataSetChanged();
 			}
 		}
 
 	}
 
 
 	@Override
 	public void onPlayerJoined(String player) {
 		Toast.makeText(getActivity(), player + " wants to join your game",
 				Toast.LENGTH_LONG).show();
 		launchGameApp(getActivity().getPackageName());
 	}
 	
 	private void launchGameApp(String pkgName) {
 	    PackageManager pm = getActivity().getPackageManager();
 	    Intent i = new Intent(Intent.ACTION_MAIN);
 	    i.addCategory("eu.andlabs.lounge");
 	    List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
 	    
 	    for(ResolveInfo info:list){
	        Intent launch = new Intent(Intent.ACTION_MAIN);
	        Log.i("debug", "found package "+info.activityInfo.packageName);
	        if(info.activityInfo.packageName.equalsIgnoreCase(pkgName)){
 	            Log.i("debug", "Packge Match found");
	            launch.setPackage(info.activityInfo.packageName);
	            launch.setClassName(getActivity(), info.activityInfo.name);
 	        }else{
 	            Log.i("debug", "NO Package Match");
	            launch = pm.getLaunchIntentForPackage(pkgName);
 	        }
	        startActivity(launch);
 	    }
 	    
 	}
 }
