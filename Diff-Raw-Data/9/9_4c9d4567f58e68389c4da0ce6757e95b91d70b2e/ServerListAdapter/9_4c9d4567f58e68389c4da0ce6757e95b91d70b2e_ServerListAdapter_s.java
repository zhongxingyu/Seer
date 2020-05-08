 /**
  * 
  */
 package nz.co.pentacog.mctracker;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 /**
  * @author Affian
  *
  */
 public class ServerListAdapter extends BaseAdapter implements Filterable {
 	
 	private ArrayList<Server> serverList = null;
 
 	public ServerListAdapter() {
 		this(new ArrayList<Server>());
 	}
 	
 	/**
 	 * 
 	 */
 	public ServerListAdapter(ArrayList<Server> serverList) {
 		this.serverList = serverList;
 		
 		try {
 			serverList.add(new Server("My Server", InetAddress.getByName("192.168.2.118")));
 			serverList.add(new Server("Blake's Server", InetAddress.getByName("182.160.139.146")));
 			serverList.add(new Server("1.7 Server via URL", InetAddress.getByName("server.aussiegamerhub.com")));
 			serverList.add(new Server("Localhost - No Server", InetAddress.getByName("localhost")));
 			
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getCount()
 	 */
 	@Override
 	public int getCount() {
 		return serverList.size();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getItem(int)
 	 */
 	@Override
 	public Server getItem(int position) {
 		return serverList.get(position);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getItemId(int)
 	 */
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getItemViewType(int)
 	 */
 	@Override
 	public int getItemViewType(int position) {
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		RelativeLayout serverView = null;
 		Server server = serverList.get(position);
 		
 		if (convertView == null) {
 			serverView = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
 		} else {
 			serverView = (RelativeLayout) convertView;
 		}
 		
 		String error = null;
 		if (!server.queried) {
 			try {
 				String[] parts = null;
 				byte[] bytes = new byte[128];
 				Socket sock = new Socket(server.address, server.port);
 				OutputStream os = sock.getOutputStream();
 				InputStream is = sock.getInputStream();
 				
 				os.write(MCServerTrackerActivity.PACKET_REQUEST_CODE);
 				is.read(bytes);
 				ByteBuffer b = ByteBuffer.wrap(bytes);
 				b.get();
 				short stringLen = b.getShort();
 				byte[] stringData = new byte[stringLen * 2];
 				b.get(stringData);
 	
 				String message = "";
 				try {
 					message = new String(stringData, "UTF-16BE");
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 	 
 				parts = message.split("\u00A7");
 				
 				if (parts.length == 3) {
 					server.motd = parts[0];
 					server.playerCount = Integer.parseInt(parts[1]);
 					server.maxPlayers = Integer.parseInt(parts[2]);
 					
 				} else {
 					error = parent.getResources().getString(R.string.server_version_error);
 				}
 				
 			} catch (IOException e) {
 				error = e.getLocalizedMessage();
 			}
 			server.queried = true;
 		}
 		//set server name
 		TextView serverTitle = (TextView) serverView.findViewById(R.id.serverTitle);
 		serverTitle.setText(server.name);
 		//set server IP
 		TextView serverIp = (TextView) serverView.findViewById(R.id.serverIp);
 		String serverName = server.address.toString();
 		if (!serverName.startsWith("/")) {
 			int index = serverName.lastIndexOf('/');
 			String tempString;
 			tempString = serverName.substring(index+1);
 			serverName = serverName.substring(0, index);
 			serverName += " " + tempString;
 		} else {
 			serverName = serverName.replace("/", "");
 		}
 		serverIp.setText(serverName + ":" + server.port);
 		
 		if (error == null) {
 			TextView playerCount = (TextView) serverView.findViewById(R.id.playerCount);
 			playerCount.setText("" + server.playerCount + "/" + server.maxPlayers);
			playerCount.setVisibility(View.VISIBLE);
 			
 			TextView serverData = (TextView) serverView.findViewById(R.id.serverData);
 			serverData.setText(server.motd);
 		} else {
 			/*
 			 * No Internet = "Network Unreachable"
 			 * open port but no server = "The operation timed out"
 			 * No open ports = <address> - Connection refused
 			 */
 			
			TextView playerCount = (TextView) serverView.findViewById(R.id.playerCount);
			playerCount.setVisibility(View.INVISIBLE);
 			
 			TextView serverData = (TextView) serverView.findViewById(R.id.serverData);
 			serverData.setText(error);
 		}
 		
 		//Server data
 		
 		
 		//if has ping/player data set that too
 		
 		return serverView;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getViewTypeCount()
 	 */
 	@Override
 	public int getViewTypeCount() {
 		return 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#hasStableIds()
 	 */
 	@Override
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#isEmpty()
 	 */
 	@Override
 	public boolean isEmpty() {
 		return serverList.isEmpty();
 	}
 
 	
 
 	/* (non-Javadoc)
 	 * @see android.widget.ListAdapter#areAllItemsEnabled()
 	 */
 	@Override
 	public boolean areAllItemsEnabled() {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.ListAdapter#isEnabled(int)
 	 */
 	@Override
 	public boolean isEnabled(int position) {
 		return true;
 	}
 
 	@Override
 	public Filter getFilter() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void remove(int position) {
 		serverList.remove(position);
 	}
 
 	public void add(Server newServer) {
 		serverList.add(newServer);
 		
 	}
 
 }
