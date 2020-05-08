 package com.cesarandres.ps2link.soe.view;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.android.volley.Request;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.Response.ErrorListener;
 import com.android.volley.Response.Listener;
 import com.android.volley.toolbox.NetworkImageView;
 import com.cesarandres.ps2link.ApplicationPS2Link;
 import com.cesarandres.ps2link.R;
 import com.cesarandres.ps2link.soe.SOECensus;
 import com.cesarandres.ps2link.soe.SOECensus.Game;
 import com.cesarandres.ps2link.soe.SOECensus.Verb;
 import com.cesarandres.ps2link.soe.content.CharacterEvent;
 import com.cesarandres.ps2link.soe.content.Faction;
 import com.cesarandres.ps2link.soe.content.WDS_Stat;
 import com.cesarandres.ps2link.soe.content.WDS_Stat.Day;
 import com.cesarandres.ps2link.soe.content.item.IContainDrawable;
 import com.cesarandres.ps2link.soe.content.response.Item_list_response;
 import com.cesarandres.ps2link.soe.content.response.Server_response;
 import com.cesarandres.ps2link.soe.util.Collections;
 import com.cesarandres.ps2link.soe.util.QueryString;
 import com.cesarandres.ps2link.soe.util.Collections.PS2Collection;
 import com.cesarandres.ps2link.soe.util.QueryString.SearchModifier;
 import com.cesarandres.ps2link.soe.volley.GsonRequest;
 
 public class WdsAdapter extends BaseAdapter {
 
 	private ArrayList<WDS_Stat> wdsStats;
 	protected LayoutInflater mInflater;
 	private static Bitmap icon_vs;
 	private static Bitmap icon_nc;
 	private static Bitmap icon_tr;
 	private static final String VS = "vs";
 	private static final String NC = "nc";
 	private static final String TR = "tr";
 
 	private Hashtable<View, GsonRequest> requestTable;
 
 	public WdsAdapter(Context context, ArrayList<WDS_Stat> wdsStats) {
 		this.mInflater = LayoutInflater.from(context);
 		for (int i = 0; i < wdsStats.size(); i++) {
 			if (!wdsStats.get(i).getStat_name().startsWith("EmpireScore.WDS01")) {
 				wdsStats.remove(i);
 				i--;
 			} else if (wdsStats.get(i).getWorld_id().equals("19") || wdsStats.get(i).getWorld_id().equals("3")) {
 				wdsStats.remove(i);
 				i--;
 			}
 		}
 		java.util.Collections.sort(wdsStats);
 		int iterations = wdsStats.size() / 3;
 		for (int i = 0; i < (iterations); i++) {
 			wdsStats.add(i * 4, new WDS_Stat());
 		}
 
 		this.wdsStats = wdsStats;
 		requestTable = new Hashtable<View, GsonRequest>(20);
 		icon_vs = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_faction_vs);
 		icon_tr = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_faction_tr);
 		icon_nc = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_faction_nc);
 	}
 
 	@Override
 	public int getCount() {
 		return this.wdsStats.size();
 	}
 
 	@Override
 	public WDS_Stat getItem(int position) {
 		return wdsStats.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.layout_wds_stat_item, null);
 
 			holder = new ViewHolder();
 			holder.faction = (ImageView) convertView.findViewById(R.id.imageViewWdsStatItemFaction);
 			holder.server = (TextView) convertView.findViewById(R.id.TextViewWdsStatItemServer);
 			holder.today = (TextView) convertView.findViewById(R.id.TextViewWdsStatItemToday);
 			holder.week = (TextView) convertView.findViewById(R.id.TextViewWdsStatItemWeek);
 			holder.month = (TextView) convertView.findViewById(R.id.TextViewWdsStatItemMonth);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 			GsonRequest request = (requestTable.get(convertView));
 			if (request != null) {
 				request.cancel();
 			}
 		}
 
 		if (position % 4 == 0) {
 			if (getItem(position).getWorld_name() == null) {
 				updateServer(getItem(position + 1).getWorld_id(), PS2Collection.WORLD, holder.server, position, convertView);
 			} else {
 				holder.server.setText(getItem(position).getWorld_name());
 			}
 			holder.faction.setImageBitmap(null);
 			holder.server.setVisibility(View.VISIBLE);
 			holder.today.setText("Today:");
 			holder.week.setText("Week:");
 			holder.month.setText("Season:");
 			holder.today.setGravity(Gravity.NO_GRAVITY);
 			holder.week.setGravity(Gravity.NO_GRAVITY);
 			holder.month.setGravity(Gravity.NO_GRAVITY);
 
 			return convertView;
 		}
 		holder.server.setVisibility(View.INVISIBLE);
 		if (getItem(position).getFaction().equals(VS)) {
 			holder.faction.setImageBitmap(icon_vs);
 		} else if (getItem(position).getFaction().equals(NC)) {
 			holder.faction.setImageBitmap(icon_nc);
 		} else if (getItem(position).getFaction().equals(TR)) {
 			holder.faction.setImageBitmap(icon_tr);
 		}
 
 		holder.today.setText(Integer.toString(getItem(position).getToday()));
 		holder.week.setText(Integer.toString(getItem(position).getThisWeek()));
 		holder.month.setText(Integer.toString(getItem(position).getThisMonth()));
 		holder.today.setGravity(Gravity.CENTER_HORIZONTAL);
 		holder.week.setGravity(Gravity.CENTER_HORIZONTAL);
 		holder.month.setGravity(Gravity.CENTER_HORIZONTAL);
 
 		return convertView;
 	}
 
 	static class ViewHolder {
 		ImageView faction;
 		TextView server;
 		TextView today;
 		TextView week;
 		TextView month;
 	}
 
 	private void updateServer(String world_id, PS2Collection collection, final TextView name, final int position, View view) {
 
 		URL url;
 		try {
			url = SOECensus.generateGameDataRequest(Verb.GET, Game.PS2V2, PS2Collection.WORLD, "",
 					QueryString.generateQeuryString().AddComparison("world_id", SearchModifier.EQUALS, world_id));
 			name.setText("Loading...");
 			Listener<Server_response> success = new Response.Listener<Server_response>() {
 				@Override
 				public void onResponse(Server_response response) {
 					if (response.getWorld_list().size() > 0) {
 						String worldName = response.getWorld_list().get(0).getName().getEn();
 						getItem(position).setWorld_name(worldName);
 						name.setText(worldName);
 					} else {
 						name.setText("UNKNOWN");
 					}
 
 				}
 			};
 
 			ErrorListener error = new Response.ErrorListener() {
 				@Override
 				public void onErrorResponse(VolleyError error) {
 					error.equals(new Object());
 					name.setText("UNKNOWN");
 				}
 			};
 			GsonRequest<Server_response> gsonOject = new GsonRequest<Server_response>(url.toString(), Server_response.class, null, success, error);
 			gsonOject.setTag(this);
 			requestTable.put(view, gsonOject);
 			ApplicationPS2Link.volley.add(gsonOject);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
