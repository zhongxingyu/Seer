 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.allotment.adapter;
 
 import java.util.List;
 
 import org.gots.R;
 import org.gots.action.bean.SowingAction;
 import org.gots.action.view.ActionWidget;
 import org.gots.allotment.AllotmentManager;
 import org.gots.allotment.view.QuickAllotmentActionBuilder;
 import org.gots.bean.BaseAllotmentInterface;
 import org.gots.preferences.GotsPreferences;
 import org.gots.seed.GotsGrowingSeedManager;
 import org.gots.seed.adapter.ListGrowingSeedAdapter;
 import org.gots.seed.provider.local.GotsGrowingSeedProvider;
 import org.gots.ui.HutActivity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Point;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ListAllotmentAdapter extends BaseAdapter implements OnClickListener {
     Context mContext;
 
     private ListGrowingSeedAdapter listGrowingSeedAdapter;
 
     // private GridView listSeeds;
 
     private List<BaseAllotmentInterface> myAllotments;
 
     private GotsGrowingSeedProvider growingSeedManager;
 
     @Override
     public void notifyDataSetChanged() {
 
         super.notifyDataSetChanged();
     }
 
     public ListAllotmentAdapter(Context mContext, List<BaseAllotmentInterface> allotments) {
         this.mContext = mContext;
         myAllotments = allotments;
         growingSeedManager = GotsGrowingSeedManager.getInstance().initIfNew(mContext);
     }
 
     public void setAllotments(List<BaseAllotmentInterface> allotments) {
         myAllotments = allotments;
         notifyDataSetChanged();
     };
 
     @Override
     public int getCount() {
         return myAllotments.size();
     }
 
     @Override
     public BaseAllotmentInterface getItem(int position) {
         return myAllotments.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return myAllotments.get(position).getId();
     }
 
     public class Holder {
         public GridView listSeeds;
 
         public LinearLayout menu;
     }
 
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         LinearLayout ll = (LinearLayout) convertView;
         Holder holder;
         if (ll == null) {
             holder = new Holder();
             ll = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.list_allotments, parent, false);
             if (GotsPreferences.DEBUG) {
                 TextView textView = new TextView(mContext);
                 textView.setText("(" + getItem(position).getId() + ")" + getItem(position).getUUID());
                 ll.addView(textView);
             }
 
             holder.listSeeds = (GridView) ll.findViewById(R.id.IdGrowingSeedList);
             holder.menu = (LinearLayout) ll.findViewById(R.id.idMenuAllotment);
             ll.setTag(holder);
             ll.setDescendantFocusability(LinearLayout.FOCUS_BLOCK_DESCENDANTS);
         } else
             holder = (Holder) ll.getTag();
 
         //
         // new AsyncTask<LinearLayout, Integer, List<GrowingSeedInterface>>() {
         // Holder holder;
         //
         // @Override
         // protected List<GrowingSeedInterface> doInBackground(LinearLayout... params) {
         // holder = (Holder) params[0].getTag();
         // return growingSeedManager.getGrowingSeedsByAllotment(myAllotments.get(position));
         //
         // }
         //
         // protected void onPostExecute(List<GrowingSeedInterface> mySeeds) {
         //
         // };
         // }.execute(ll);
         WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
         Display display = wm.getDefaultDisplay();
         Point size = new Point();
        int width;
        int height;
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            width = display.getWidth();
            height = display.getHeight();
        } else {
            display.getSize(size);
            width = size.x;
            height = size.y;
        }
         int layoutsize = 200;
         int nbcolumn = (width - 200) / layoutsize;
        if (nbcolumn < 1)
            nbcolumn = 1;
         holder.listSeeds.setNumColumns(nbcolumn);

         listGrowingSeedAdapter = new ListGrowingSeedAdapter(mContext, getItem(position).getSeeds());
         holder.listSeeds.setAdapter(listGrowingSeedAdapter);
 
         // holder.listSeeds.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
         // layoutsize));
        // if (holder.listSeeds.getCount() % nbcolumn == 0)
        holder.listSeeds.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                (holder.listSeeds.getCount() / nbcolumn + 1) * layoutsize));
        // else
        // holder.listSeeds.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
        // ((holder.listSeeds.getCount() / nbcolumn) + 1) * layoutsize));
 
         holder.menu.removeAllViews();
 
         SowingAction sow = new SowingAction(mContext);
         ActionWidget widget = new ActionWidget(mContext, sow);
         widget.setTag(position);
         widget.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 AllotmentManager.getInstance().setCurrentAllotment(getItem(Integer.valueOf(v.getTag().toString())));
 
                 Intent i = new Intent(mContext, HutActivity.class);
                 i.putExtra("org.gots.allotment.reference", getItem(Integer.valueOf(v.getTag().toString())).getId());
                 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 mContext.startActivity(i);
             }
         });
 
         widget.setPadding(4, 4, 4, 8);
         holder.menu.addView(widget);
 
         return ll;
     }
 
     @Override
     public void onClick(View v) {
         QuickAllotmentActionBuilder actionsBuilder = new QuickAllotmentActionBuilder(v);
         actionsBuilder.show();
 
     }
 }
