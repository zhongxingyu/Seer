 package me.taedium.android.view;
 
 import java.util.ArrayList;
 
 import me.taedium.android.ApplicationGlobals;
 import me.taedium.android.ApplicationGlobals.RecParamType;
 import me.taedium.android.FirstStart;
 import me.taedium.android.R;
 import me.taedium.android.api.Caller;
 import me.taedium.android.domain.Recommendation;
 import me.taedium.android.widgets.ExpandablePanel;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.LinearGradient;
 import android.graphics.Shader;
 import android.graphics.Shader.TileMode;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 
 public class RecommendationAdapter extends FragmentPagerAdapter {
 
     private Context mContext;
     private ArrayList<Recommendation> mRecommendations;
     private boolean showSingleRec = false;
     
     public RecommendationAdapter(FragmentActivity activity, ArrayList<Recommendation> recommendations) {
     	super(activity.getSupportFragmentManager());
     	mContext = activity;
     	mRecommendations = recommendations;
     }
     
     public void setShowSingleRec(boolean showSingleRec) {
     	this.showSingleRec = showSingleRec;
     }
     
     public Recommendation getRecommendation(int position) {
     	if (mRecommendations.size() >0) {
 	    	return mRecommendations.get(position);
     	}
     	return null;
     }
     
     public void removeRecommendation(int index) {
     	mRecommendations.remove(index);
     	notifyDataSetChanged();
     }
     
     @Override
     public int getCount() {
         return mRecommendations.size();
     }
     
     @Override
     public Fragment getItem(int position) {
     	return new RecFragment(position);
     }
 
	private class RecFragment extends ListFragment {
 		private int mPosition;
 		
 		public RecFragment (int position) {
 			mPosition = position;
 		}
 		
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 	        // If we are near the end of the list, grab some more activities
 	        if (mPosition == mRecommendations.size() - 9 && !showSingleRec) {
 	            new GetRecommendationTask().execute();
 	        }
 	        
 	        final Recommendation rec = mRecommendations.get(mPosition);
 	        View view = inflater.inflate(R.layout.view_fragment, null);
 	       
 	        // Don't show arrows on first/last recommendation or if we are only showing one recommendation
 	        if (mPosition == 0 || showSingleRec) {
 	            ImageView prevArrow = (ImageView)view.findViewById(R.id.ivRecPrev);
 	            prevArrow.setVisibility(View.GONE);
 	        } 
 	        if (mPosition == mRecommendations.size() - 1 || showSingleRec) {
 	            ImageView nextArrow = (ImageView)view.findViewById(R.id.ivRecNext);
 	            nextArrow.setVisibility(View.GONE);
 	        }
 	        
 	        // Disable scroll bar
 	        ScrollView  sv = (ScrollView)view.findViewById(R.id.svRec);
 	        sv.setVerticalScrollBarEnabled(false);
 	        
 	        // Set the name
 	        TextView name = (TextView)view.findViewById(R.id.tvRecName);
 	        name.setText(rec.name);
 	        
 	        // Set the description
 	        TextView desc = (TextView)view.findViewById(R.id.tvRecDescription);
 	        desc.setText(rec.description);
 	        Linkify.addLinks(desc, Linkify.ALL);
 	        
 	        // Set expandable panel listener
 	        ExpandablePanel panel = (ExpandablePanel)view.findViewById(R.id.epRecDescription);
 	        final int collapsedHeight = panel.getCollapsedHeight();
 	        panel.setOnExpandListener(new ExpandablePanel.OnExpandListener() {
 	            public void onCollapse(View handle, View content) {
 	                Button btn = (Button)handle;
 	                btn.setText("More");
 	                
 	                if (btn.isShown()) {
 	                    TextView desc = (TextView)content;
 	                    Shader textShader = new LinearGradient(0, 0, 0, collapsedHeight,
 	                                                         new int[] {0xFF000000, 0xCC000000, 0x0000000}, null, TileMode.CLAMP);
 	                    desc.getPaint().setShader(textShader);
 	                }
 	            }
 	            public void onExpand(View handle, View content) {
 	                Button btn = (Button)handle;
 	                btn.setText("Less");
 	               
 	                if (btn.isShown()) {
 	                    TextView desc = (TextView)content;
 	                    desc.getPaint().setShader(null);
 	                }
 	            }
 	        });
 	       
 	        // Context used for getting strings from res files
 	        Context context = mContext;
 	        
 	        if (rec.min_people == 0 && rec.cost == 0 && rec.max_duration == 0) {
 	            TextView addlInfo = (TextView) view.findViewById(R.id.tvAddlInfo);
 	            addlInfo.setVisibility(View.GONE);
 	        }
 	        
 	        // Set the number of people        
 	        TextView numPeople = (TextView)view.findViewById(R.id.tvRecNumPeople);
 	        if (rec.min_people != 0) {
 	            if (rec.min_people == rec.max_people) {
 	                numPeople.setText(context.getString(R.string.stFor) + " " + rec.min_people); 
 	            } else {
 	                numPeople.setText(context.getString(R.string.stFrom) + " " + rec.min_people + 
 	                		" " + context.getString(R.string.stTo) + " " + rec.max_people);
 	            }
 	        } else {
 	            TextView label = (TextView)view.findViewById(R.id.tvRecNumPeopleLabel);
 	            ImageView numPeopleIcon = (ImageView)view.findViewById(R.id.ivRecNumPeople);
 	            label.setVisibility(View.GONE);
 	            numPeople.setVisibility(View.GONE);
 	            numPeopleIcon.setVisibility(View.GONE);
 	        }
 	        
 	        // Set the cost
 	        TextView cost = (TextView)view.findViewById(R.id.tvRecCost);
 	        if (rec.cost != 0) {
 	            String suffix = " " + context.getString(R.string.stTotal);
 	            if (rec.cost_is_per_person) {
 	                suffix = " " + context.getString(R.string.stPerPerson);
 	            }
 	            String c = Double.toString(rec.cost);
 	            if (c.indexOf('.') == c.length()-2) {
 	            	c = c + "0";
 	            }
 	            cost.setText("$" + c + suffix);
 	        } else {
 	            TextView label = (TextView)view.findViewById(R.id.tvRecCostLabel);
 	            ImageView costIcon = (ImageView)view.findViewById(R.id.ivRecCost);
 	            label.setVisibility(View.GONE);
 	            cost.setVisibility(View.GONE);
 	            costIcon.setVisibility(View.GONE);
 	        }
 	        
 	        // Set the duration
 	        TextView duration = (TextView)view.findViewById(R.id.tvRecDuration);
 	        if (rec.max_duration != 0) {
 	            int minDuration = rec.min_duration;
 	            int maxDuration = rec.max_duration;
 	            String suffix = maxDuration == 1 ? 
 	            		" " + context.getString(R.string.stMinute) : " " + context.getString(R.string.stMinutes);
 	            if (minDuration >= 60) {
 	                minDuration = Math.round(minDuration / 60);
 	                maxDuration = Math.round(maxDuration / 60);
 	                suffix = maxDuration == 1 ? 
 	                		" " + context.getString(R.string.stHour) : " " + context.getString(R.string.stHours);
 	            }
 	            if (minDuration == maxDuration) {
 	                duration.setText(maxDuration + suffix);
 	            } else {
 	                duration.setText(minDuration + " " + context.getString(R.string.stTo) + " " + maxDuration + suffix);
 	            }
 	        } else {
 	            TextView label = (TextView)view.findViewById(R.id.tvRecDurationLabel);
 	            ImageView durationIcon = (ImageView)view.findViewById(R.id.ivRecDuration);
 	            label.setVisibility(View.GONE);
 	            duration.setVisibility(View.GONE);
 	            durationIcon.setVisibility(View.GONE);
 	        }
 	        
 	        // Set show map listener
 	        Button map = (Button)view.findViewById(R.id.bShowMap);
 	        if (rec.lon != 0 || rec.lat != 0) {
 	            map.setOnClickListener(new Button.OnClickListener() {
 	                public void onClick(View arg0) {
 	                    /**/
 	                    Intent intent = new Intent(mContext, MapDetail.class);
 	                    Bundle bundle = new Bundle();
 	                    bundle.putDouble("lat", rec.lat);
 	                    bundle.putDouble("long", rec.lon);
 	                    intent.putExtras(bundle);
 	                    /**
 	                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr=" + rec.getLat() + "," + rec.getLong()));
 	                    intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));
 	                    /**
 	                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + rec.getLat() + "," + rec.getLong() + "?z=16"));
 	                    /**/
 	                    mContext.startActivity(intent);
 	                }
 	            });
 	        } else {
 	            map.setVisibility(View.GONE);
 	        }
 	
 	        return view;
 	    }
 	    
 	    
 	    // This class gets a new set of recommendations in the background and adds it to the current recommendation list
 	    private class GetRecommendationTask extends AsyncTask<Void, Void, ArrayList<Recommendation>> {
 	        @Override
 	        protected void onPostExecute(ArrayList<Recommendation> recs) {
 	            mRecommendations.addAll(recs);
 	            // This doesn't actually work
 	            // See http://code.google.com/p/android/issues/detail?id=19001
 	            notifyDataSetChanged();
 	        }
 	
 	        @Override
 	        protected ArrayList<Recommendation> doInBackground(Void... params) {
 	        	if (ApplicationGlobals.getInstance().isLocationEnabled()) {
 	        		Location location = ApplicationGlobals.getInstance().getCurrentLocation();
 	        		if (location != null) {
 	        			FirstStart.addRecommendationParam(RecParamType.LAT, Double.toString(location.getLatitude()));
 	        			FirstStart.addRecommendationParam(RecParamType.LONG, Double.toString(location.getLongitude()));
 	        		}
 	        	}
 	            return Caller.getInstance(mContext).getRecommendations();
 	        }
 	    }
 	}
 }
