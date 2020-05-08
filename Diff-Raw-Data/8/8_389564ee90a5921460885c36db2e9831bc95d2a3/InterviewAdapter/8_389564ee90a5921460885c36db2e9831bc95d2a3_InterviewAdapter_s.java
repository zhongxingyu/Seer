 package com.yahoo.interviewr;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class InterviewAdapter extends BaseAdapter {
 
     private Activity mActivity;
     private LayoutInflater mInflater = null;
     private ArrayList <InterviewRowObject> mInterviewObjects;
 
     public InterviewAdapter(Activity a, 
     		ArrayList<InterviewRowObject> interviewObjects) {
     	mActivity = a;
     	mInflater = (LayoutInflater) mActivity
     			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         mInterviewObjects = interviewObjects;
     }
 
     public class ViewHolder {
         public TextView nameTv;
         public TextView interviewTimeTv;
         public TextView aboutMeTv;
         public ImageView picture;
         public RelativeLayout expandedView;
         public InterviewRowObject interviewRow;
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
     	InterviewRowObject thisRow = mInterviewObjects.get(position);
         View vi = convertView;
         ViewHolder holder;
         if (convertView == null) {
             vi = mInflater.inflate(R.layout.interview_item, null);
             holder = new ViewHolder();
             holder.nameTv = (TextView) vi.findViewById(R.id.name);
             holder.interviewTimeTv = (TextView) vi.findViewById(R.id.interview_time);
             holder.expandedView = (RelativeLayout) vi.findViewById(R.id.bottom_part);
             holder.picture = (ImageView) vi.findViewById(R.id.picture);
             holder.aboutMeTv = (TextView) vi.findViewById(R.id.about_me);
             // add data about the interview row to the view as the tag
             holder.interviewRow = thisRow;
             vi.setTag(holder);
         } else {
             holder = (ViewHolder) vi.getTag();
         }
         
         holder.nameTv.setText(thisRow.mName);
         holder.interviewTimeTv.setText(thisRow.mInterviewTime);
         holder.picture.setImageResource(thisRow.mPhotoId);
         holder.aboutMeTv.setText(thisRow.mAboutMe);
         
         if (thisRow.mExpanded) {
         	holder.expandedView.setVisibility(View.VISIBLE);
         }
         else {
         	holder.expandedView.setVisibility(View.GONE);
         }
 		
         // if slot is breaktime, make unclickable
 		if (!thisRow.mName.equals("Break")) {
			convertView.setOnClickListener(onCellClicked);
 		}
         
         return vi;
     }
 	
 	private OnClickListener onCellClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 				ViewHolder holder = (ViewHolder) v.getTag();
 				InterviewRowObject row = holder.interviewRow;
 				RelativeLayout expandedView = holder.expandedView;
 				if (row.mExpanded) {
 					collapse(expandedView);
 				}
 				else {
 					expand(expandedView);
 				}
 				row.mExpanded = !row.mExpanded;				
     	}
 	};
 	
 	private void collapse(View expandedView) {
 		expandedView.setVisibility(View.GONE);
 		DropDownAnim dda = new DropDownAnim(expandedView, true);
 		expandedView.startAnimation(dda);
 	}
 	
 	private void expand(View expandedView) {
 		expandedView.setVisibility(View.VISIBLE);
 		expandedView.getLayoutParams().height = 0;
 		DropDownAnim dda = new DropDownAnim(expandedView, true);
 		expandedView.startAnimation(dda);
 	}
 
 	@Override
 	public int getCount() {
 		return mInterviewObjects.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return mInterviewObjects.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 }
