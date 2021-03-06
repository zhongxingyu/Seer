 /**
  * The MIT License (MIT)
  * Copyright (c) 2012 David Carver
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
  * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
  * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package us.nineworlds.serenity.ui.browser.tv.seasons;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import us.nineworlds.serenity.SerenityApplication;
 import us.nineworlds.serenity.core.model.SeriesContentInfo;
 import us.nineworlds.serenity.core.model.impl.TVShowSeriesInfo;
 import us.nineworlds.serenity.core.services.ShowSeasonRetrievalIntentService;
 import us.nineworlds.serenity.ui.views.TVShowSeasonImageView;
 
 import us.nineworlds.serenity.R;
 
 import com.nostra13.universalimageloader.core.ImageLoader;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * 
  * @author dcarver
  *
  */
 public class TVShowSeasonImageGalleryAdapter extends BaseAdapter {
 	
 	private static List<TVShowSeriesInfo> seasonList = null;
 	private static Activity context;
 	
 	private ImageLoader imageLoader;
 	private static final int SIZE_HEIGHT = 200;
 	private static final int SIZE_WIDTH = 400;
 	private static ProgressDialog pd;
 	private Handler handler;
 	private String key;
 	private static TVShowSeasonImageGalleryAdapter notifyAdapter;
 	private String baseUrl;
 	
 	public TVShowSeasonImageGalleryAdapter(Context c, String key) {
 		context = (Activity)c;
 		this.key = key;
 		
 		seasonList = new ArrayList<TVShowSeriesInfo>();
 		
 		imageLoader = SerenityApplication.getImageLoader();
 		notifyAdapter = this;
 		
 		baseUrl = SerenityApplication.getPlexFactory().baseURL();
 		fetchData();
 	}
 	
 	protected void fetchData() {
 		pd = ProgressDialog.show(context, "", "Retrieving Seasons");
 		handler = new ShowSeasonRetrievalHandler();
 		Messenger messenger = new Messenger(handler);
 		Intent intent = new Intent(context, ShowSeasonRetrievalIntentService.class);
 		intent.putExtra("MESSENGER", messenger);
 		intent.putExtra("key", key);
 		context.startService(intent);
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getCount()
 	 */
 	public int getCount() {
 		
 		return seasonList.size();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getItem(int)
 	 */
 	public Object getItem(int position) {
 		
 		return seasonList.get(position);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getItemId(int)
 	 */
 	public long getItemId(int position) {
 		return position;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
 	 */
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		SeriesContentInfo pi = seasonList.get(position);
 		TVShowSeasonImageView mpiv = new TVShowSeasonImageView(context, pi);
 		mpiv.setBackgroundColor(Color.BLACK);
 		mpiv.setScaleType(ImageView.ScaleType.FIT_XY);
 		int width = 200;
 		int height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
 		mpiv.setLayoutParams(new Gallery.LayoutParams(width, height));
 		
 		imageLoader.displayImage(pi.getPosterURL(), mpiv);
 		return mpiv;
 	}
 	
 	private static class ShowSeasonRetrievalHandler extends Handler {
 		
 		@Override
 		public void handleMessage(Message msg) {
 			seasonList = (List<TVShowSeriesInfo>) msg.obj;
 			
 			if (seasonList != null) {
 				if (!seasonList.isEmpty()) {
 					TextView titleView = (TextView)context.findViewById(R.id.tvShowSeasonsDetailText);
 					titleView.setText(seasonList.get(0).getParentShowTitle());
					TextView textView = (TextView) context.findViewById(R.id.tvShowSeasonsItemCount);
					textView.setText(Integer.toString(seasonList.size()) + " Item(s)");
					
 				}
 				notifyAdapter.notifyDataSetChanged();
 				pd.dismiss();
 			}
 			
			
 		}
 	}
 
 }
