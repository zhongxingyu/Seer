 package org.rhq.pocket.alert;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import org.rhq.core.domain.rest.AlertDefinition;
 import org.rhq.core.domain.rest.AlertRest;
 import org.rhq.pocket.R;
 
 /**
  * View Adapter for single AlertList items
  * @author Heiko W. Rupp
  */
 public class AlertListItemAdapter extends ArrayAdapter<AlertRest> {
 
     private LayoutInflater inflater;
     private List<AlertRest> alertList;
 
     public AlertListItemAdapter(Context context, int textViewResourceId, List<AlertRest> alertList) {
         super(context, textViewResourceId,alertList);
         this.alertList = alertList;
         inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 
         ViewHolder viewHolder;
 
         // Use ViewHolder pattern to only inflate once
         if (convertView ==null) {
 
             convertView = inflater.inflate(R.layout.alert_list_item,null);
 
             viewHolder = new ViewHolder();
             viewHolder.nameView = (TextView) convertView.findViewById(R.id.name_view);
             viewHolder.resourceView = (TextView) convertView.findViewById(R.id.resource_view);
             convertView.setTag(viewHolder);
         }
         else {
             viewHolder = (ViewHolder) convertView.getTag();
         }
 
         AlertRest alertRest = alertList.get(position);
         AlertDefinition definition = alertRest.getAlertDefinition();
        if (definition!=null) {
             if (definition.getPriority().equals("MEDIUM"))
                 convertView.setBackgroundColor(Color.rgb(120,120,0));
             else if (definition.getPriority().equals("HIGH"))
                 convertView.setBackgroundColor(Color.rgb(68,0,0));
             else
                 convertView.setBackgroundColor(Color.DKGRAY);
         }
         viewHolder.nameView.setText(alertRest.getName());
        viewHolder.resourceView.setText(alertRest.getResource().getResourceName());
 
         return convertView;
     }
 
     private static class ViewHolder {
         TextView nameView;
         TextView resourceView;
     }
 }
