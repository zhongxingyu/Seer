 package com.coffeeandpower.adapters;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.R;
 import com.coffeeandpower.cont.UserSmart;
 import com.coffeeandpower.imageutil.ImageLoader;
 
 public class MyUserSmartAdapter extends BaseAdapter {
 
 	private ArrayList<UserSmart> mudArray;
 	private LayoutInflater inflater;
 	public ImageLoader imageLoader;
 
 	public MyUserSmartAdapter(Activity context, ArrayList<UserSmart> mudArray) {
 
 		this.inflater = context.getLayoutInflater();
 		this.imageLoader = new ImageLoader(context.getApplicationContext());
 
 		if (mudArray != null) {
 			this.mudArray = mudArray;
 		} else {
 			this.mudArray = new ArrayList<UserSmart>();
 		}
 	}
 
 	@Override
 	public int getCount() {
 		return mudArray.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return mudArray.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	public static class ViewHolder {
 		public TextView textNickName;
 		public TextView textJobName;
 		public TextView textCheckins;
 		public ImageView profileImage;
 
 		public ViewHolder(View convertView) {
 
 			this.textNickName = (TextView) convertView.findViewById(R.id.text_name);
 			this.textJobName = (TextView) convertView.findViewById(R.id.job_name);
 			this.textCheckins = (TextView) convertView.findViewById(R.id.text_checkins);
 			this.profileImage = (ImageView) convertView.findViewById(R.id.imageview_image);
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		if (convertView == null) {
 			convertView = inflater.inflate(R.layout.item_list_worked, null);
 			holder = new ViewHolder(convertView);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		holder.textJobName.setText(mudArray.get(position).getHeadLine().equals("null") ? "" : (mudArray.get(position).getHeadLine()
 				.equals("NULL") ? "" : ((mudArray.get(position).getHeadLine().equals(null) ? "" : AppCAP.cleanResponseString(mudArray
 				.get(position).getHeadLine())))));
 
 		if (AppCAP.isLoggedIn()) {
			holder.textNickName.setText(AppCAP.cleanResponseString(mudArray.get(position).getNickName()));
 
 			String checkStr = mudArray.get(position).getCheckInCount() == 1 ? mudArray.get(position).getCheckInCount() + " Checkin"
 					: mudArray.get(position).getCheckInCount() + " Checkins";
 			holder.textCheckins.setText(checkStr);
 
 			// Try to load profile image
 			imageLoader.DisplayImage(mudArray.get(position).getFileName(), holder.profileImage, R.drawable.default_avatar50, 70);
 		} else {
 			holder.textNickName.setText("Name Hidden");
 			imageLoader.DisplayImage("", holder.profileImage, R.drawable.default_avatar50_login, 70);
 		}
 
 		return convertView;
 	}
 }
