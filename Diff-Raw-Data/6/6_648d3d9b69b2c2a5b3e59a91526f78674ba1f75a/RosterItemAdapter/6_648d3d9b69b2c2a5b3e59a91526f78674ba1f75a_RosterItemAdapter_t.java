 package directi.androidteam.training.chatclient.Roster;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Roster.util.ImageResize;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 9/13/12
  * Time: 7:25 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RosterItemAdapter extends BaseAdapter {
     Context context;
     public ArrayList<RosterEntry> rosterEntries;
 
     public RosterItemAdapter(Context context) {
         this.context = context;
         rosterEntries = new ArrayList<RosterEntry>();
         Log.d("XXXX", "roster refresh called with size " + rosterEntries.size());
     }
     public void setRosterEntries(ArrayList<RosterEntry> rosterEntriesInput){
         rosterEntries = rosterEntriesInput;
         Log.d("XXXX", "roster refresh called with size " + rosterEntries.size());
     }
 
     @Override
     public int getCount() {
         return rosterEntries.size();
     }
 
     @Override
     public Object getItem(int i) {
         return rosterEntries.get(i);
     }
 
     @Override
     public long getItemId(int i) {
         return i;
     }
 
     @Override
     public boolean isEnabled(int position)
     {
         return true;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         Log.d("XXXXX", "get view is called for position " + position);
         View v = convertView;
         RosterItemHolder rosterItemHolder;
         if(convertView==null){
             LayoutInflater inflater = ((Activity)context).getLayoutInflater();
             v = inflater.inflate(R.layout.rosterlistitem,null);
 
             rosterItemHolder = new RosterItemHolder();
             rosterItemHolder.rosterImg = (ImageView) v.findViewById(R.id.roster_image);
             rosterItemHolder.rosterJid = (TextView) v.findViewById(R.id.roster_item);
            rosterItemHolder.availabilityImage = (ImageView)v.findViewById(R.id.list_availability_image);
             rosterItemHolder.rosterStatus = (TextView) v.findViewById(R.id.roster_status);
             v.setTag(rosterItemHolder);
         }
         else {
             rosterItemHolder = (RosterItemHolder) v.getTag();
         }
         RosterEntry rosterEntry = rosterEntries.get(position);
         if(rosterEntry!=null){
             new ImageResize().attachIcon(rosterItemHolder.rosterImg,context);
             rosterItemHolder.rosterJid.setText(rosterEntry.getJid());
             if(rosterEntry.getPresence()!=null) {
                 Log.d("ssss","jid : "+rosterEntry.getJid()+"  presence :"+rosterEntry.getPresence() + "status"+rosterEntry.getStatus());
             if(rosterEntry.getPresence().equals("dnd")) {
                 rosterItemHolder.availabilityImage.setImageResource(R.drawable.red);
             }
             else if(rosterEntry.getPresence().equals("away")) {
                 rosterItemHolder.availabilityImage.setImageResource(R.drawable.yellow);
             }
             else if(rosterEntry.getPresence().equals("chat")) {
                 rosterItemHolder.availabilityImage.setImageResource(R.drawable.green);
             }
             else {
                 rosterItemHolder.availabilityImage.setImageResource(R.drawable.gray);
             }
             }
         else {
                 rosterItemHolder.availabilityImage.setImageResource(R.drawable.gray);
          }
             if(rosterEntry.getStatus()!=null) {
                 rosterItemHolder.rosterStatus.setText(rosterEntry.getStatus());
             }
 
         }
         return v;
     }
 
     static class RosterItemHolder{
         ImageView rosterImg;
         TextView rosterJid;
         TextView rosterStatus;
         ImageView availabilityImage;
     }
 
     @Override
     public  int getViewTypeCount () {
         return 1;
     }
 }
