 package org.touchirc.adapter;
 
 import org.touchirc.R;
 import org.touchirc.irc.IrcService;
 import org.touchirc.model.Server;
 
 import android.content.Context;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class ServerAdapter extends BaseAdapter {
 
 	private SparseArray<Server> serversList;
 	private Context c;
 	private IrcService ircService;
 
 	public ServerAdapter (SparseArray<Server> servers_AL, Context c, IrcService service){
 		this.serversList = servers_AL;
 		this.c = c;
 		this.ircService = service;
 	}
 
 	public int getCount() {
 		return serversList.size();
 	}
 
 	public Server getItem(int position) {
 		return serversList.valueAt(position);
 	}
 
 	public long getItemId(int position) {
 		return serversList.keyAt(position);
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Server s = serversList.valueAt(position); // Collect the server concerned
 		if(s == null)
 			return null;
 		if (convertView == null){
 			LayoutInflater vi = (LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = vi.inflate(R.layout.item_list, null);
 		}
 		
 		TextView autoConnect_TV = (TextView) convertView.findViewById(R.id.textView_DEFAULT);
 		
 		// Checking if the current server is auto-connected
 		if(s.isAutoConnect()){
 			autoConnect_TV.setBackgroundResource(R.drawable.object_border);
 			autoConnect_TV.setText(R.string.AUTO);
 		}
 		else{
 			// To ensure the fact that when the method is recalled the resources are corrects
 			autoConnect_TV.setBackgroundResource(0);
 			autoConnect_TV.setText("");
 		}
 
 		TextView serverName_TV = (TextView) convertView.findViewById(R.id.textView_itemName);
 		serverName_TV.setText(s.getName());
 		
 		TextView hostnameServer_TV = (TextView) convertView.findViewById(R.id.textView_itemSecondInfo);
 		hostnameServer_TV.setText(s.getHost());
 		
		if(ircService.isConnected(s))
 			convertView.findViewById(R.id.serverConnectedImageView).setVisibility(View.VISIBLE);

 		return convertView;
 	}
 }
