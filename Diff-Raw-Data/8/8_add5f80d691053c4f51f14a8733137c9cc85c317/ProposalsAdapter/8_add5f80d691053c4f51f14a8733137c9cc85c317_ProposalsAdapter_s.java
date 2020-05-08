 package com.call4paperz.android.adapters;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import com.call4paperz.android.R;
 import com.call4paperz.android.model.Proposal;
 
 import java.util.List;
 
 public class ProposalsAdapter extends ArrayAdapter<Proposal> {
 
     public ProposalsAdapter(Context context, List<Proposal> proposals) {
        super(context, R.layout.proposals_item, R.proposal.name, proposals);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 
         final Proposal proposal = getItem(position);
 
         View view = super.getView(position, convertView, parent);
 
        TextView points = (TextView) view.findViewById(R.proposal.points);
         points.setText(String.valueOf(proposal.getAcceptancePoints()));
 
         int color;
         if( proposal.getAcceptancePoints() > 0 ) {
             color = R.color.green;
         } else if( proposal.getAcceptancePoints() < 0 ) {
             color = R.color.red;
         } else {
             color = R.color.gray;
         }
 
         points.setBackgroundResource(color);
 
        TextView speaker = (TextView) view.findViewById(R.proposal.speaker);
         speaker.setText(proposal.getUser().getName());
 
         return view;
 
     }
 
 }
