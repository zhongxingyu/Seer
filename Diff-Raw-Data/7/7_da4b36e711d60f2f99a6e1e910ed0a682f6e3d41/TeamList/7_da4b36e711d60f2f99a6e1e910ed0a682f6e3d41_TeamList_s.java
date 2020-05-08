 package net.djmacgyver.bgt.team;
 
 import net.djmacgyver.bgt.R;
 import net.djmacgyver.bgt.socket.ServerList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class TeamList extends ServerList {
 	public TeamList(Context context) {
 		super(context);
 	}
 	
 	@Override
 	protected String getServerCommand() {
		return "getMaps";
 	}
 	
 	@Override
 	public int getItemViewType(int arg0) {
 		return 0;
 	}
 
 	@Override
 	public View getView(int arg0, View arg1, ViewGroup arg2) {
 		LayoutInflater inf = LayoutInflater.from(getContext());
 		View v = inf.inflate(R.layout.maplistitem, arg2, false);
 		TextView text = (TextView) v.findViewById(R.id.mapName);
 		JSONObject team;
 		try {
 			team = getData().getJSONObject(arg0);
 			text.setText(team.getString("name"));
 		} catch (JSONException e) {
 			text.setText("undefined");
 		}
 		return v;
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 1;
 	}
 }
