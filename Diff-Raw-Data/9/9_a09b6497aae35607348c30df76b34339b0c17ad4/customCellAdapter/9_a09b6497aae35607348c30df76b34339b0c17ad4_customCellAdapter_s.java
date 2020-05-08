 package com.klusman.java2;
 
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class customCellAdapter extends BaseAdapter {
 
 		private JSONArray items;
 	    private Context cont;
 	    
 	    
 	    public customCellAdapter(Context context, JSONArray array)
 	    {
 	        super();
 	        this.items = array;
 	        this.cont = context;
 	    }
 
 	    @Override
 	    public int getCount() {
 	        return items.length();
 	    }
 
 	    @Override
 	    public Object getItem(int position) {
 	        return null;
 	    }
 
 	    @Override
 	    public long getItemId(int position) {
 	        return 0;
 
 	    }@Override
 	    public View getView(int position, View convertView, ViewGroup parent) 
 	    {
 	        View v = convertView;
 	        //WebIView sath;
 	        TextView Title;
 	        Log.i("LISTVIEW", "Checking Position" + position);
 	        try
 	        {       
 	            if(!items.isNull(position))
 	            {
 	                JSONObject item = items.getJSONObject(position);
 	                if (v == null) {
 	                    v = LayoutInflater.from(cont).inflate(R.layout.day_tile_act, null);
 	                }           
 	                //sath = (WebIView) v.findViewById(R.id.sathumbnail);
 	                Title = (TextView) v.findViewById(R.id.title);
/*
 	                if(item.has("image") && sath != null)
 	                {
 	                    JSONObject thisImage = item.getJSONObject("image");
 	                    sath.reset();
 	                    sath.setImageUrl(thisImage.getString("thumbnail"));
 	                    sath.loadImage();
 	                }*/
 	                if(Title != null)
 	                {
 	                	Title.setText(item.getString("date"));
 	                }
 	            }else{
 	                return null;
 	            }
 	        }
 	        catch(Exception e)
 	        {
 	            Log.e("num", "LIST ERROR! " + e.toString());
 	        }
 	        return v;
 	    }
 	
 }
