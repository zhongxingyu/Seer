 package se.chalmers.krogkollen.adapter;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: filipcarlen
  * Date: 2013-10-16
  * Time: 11:17
  * To change this template use File | Settings | File Templates.
  */
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.google.android.gms.maps.model.LatLng;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.map.UserLocation;
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.utils.Distance;
 
 import java.text.*;
 
 /**
  * An adapter handling the different items in a search list
  */
 public class SearchViewAdapter extends ArrayAdapter<IPub> {
 
     Context context;
     int layoutResourceId;
     IPub data[] = null;
     View row;
     PubHolder holder;
 
     /**
      * A constructor that creates an SearchViewAdapter.
      * @param context
      * @param layoutResourceId
      * @param data
      */
     public SearchViewAdapter(Context context, int layoutResourceId, IPub[] data) {
         super(context, layoutResourceId, data);
         this.layoutResourceId = layoutResourceId;
         this.context = context;
         this.data = data;
 
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         row = convertView;
         holder = null;
 
         if(row == null){
             LayoutInflater inflater = ((Activity)context).getLayoutInflater();
             row = inflater.inflate(layoutResourceId, parent, false);
 
             holder = new PubHolder();
             holder.imgIcon = (ImageView)row.findViewById(R.id.listview_image);
             holder.txtTitle = (TextView)row.findViewById(R.id.listview_title);
             holder.distanceText = (TextView)row.findViewById(R.id.listview_distance);
 
             row.setTag(holder);
         } else {
             holder = (PubHolder)row.getTag();
         }
 
         IPub pub = data[position];
         DecimalFormat numberFormat = new DecimalFormat("#0.00");
         holder.txtTitle.setText(pub.getName());
         holder.distanceText.setText(""+(numberFormat.format(Distance.calcDistBetweenTwoLatLng(new LatLng(pub.getLatitude(), pub.getLongitude()), UserLocation.getInstance().getCurrentLatLng())))+" km");
 
         switch(pub.getQueueTime()){
             case 1:
                 holder.imgIcon.setImageResource(R.drawable.detailed_queue_green);
                 break;
             case 2:
                 holder.imgIcon.setImageResource(R.drawable.detailed_queue_yellow);
                 break;
             case 3:
                 holder.imgIcon.setImageResource(R.drawable.detailed_queue_red);
                 break;
             default :
                 holder.imgIcon.setImageResource(R.drawable.detailed_queue_gray);
                 break;
         }
         return row;
     }
 
     /**
      * Static holder for pubs
      */
     static class PubHolder
     {
         ImageView imgIcon;
         TextView txtTitle;
         TextView distanceText;
     }
 }
 
