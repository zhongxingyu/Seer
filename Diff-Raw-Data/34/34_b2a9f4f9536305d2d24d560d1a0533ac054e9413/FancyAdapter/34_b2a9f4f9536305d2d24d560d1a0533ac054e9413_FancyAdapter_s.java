 package ch.shibastudio.test;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class FancyAdapter extends BaseAdapter{
 
 	static class ViewHolder{
 		TextView tvCharacter;
 		TextView tvInfo1;
 		TextView tvInfo2;
 		ImageView ivFavorite;
 	}
 	private LayoutInflater mInflater;
 	private ArrayList<CharacterInfos> mChars = new ArrayList<CharacterInfos>();
 	private Context mCtx;
 	
 	public FancyAdapter(Context c){
 		mCtx = c;
 		mInflater = LayoutInflater.from(c);
 	}
 	
 	public void addEntry(CharacterInfos c){
 		mChars.add(c);
 		notifyDataSetChanged();
 	}
 	
 	@Override
 	public int getCount() {
 		return mChars.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return mChars.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder = null;
 		if(convertView == null){
 			convertView = mInflater.inflate(R.layout.listrow, null);
 			holder = new ViewHolder();
 			holder.tvCharacter = (TextView)convertView.findViewById(R.id.character);
 			holder.tvInfo1 = (TextView)convertView.findViewById(R.id.info1);
 			holder.tvInfo2 = (TextView)convertView.findViewById(R.id.info2);
 			holder.ivFavorite = (ImageView)convertView.findViewById(R.id.favicon);
 			convertView.setTag(holder);
 		}else{
 			holder = (ViewHolder)convertView.getTag();
 		}
 		
 		holder.tvCharacter.setText(mChars.get(position).character);
 		holder.tvInfo1.setText(mChars.get(position).info1);
 		holder.tvInfo2.setText(mChars.get(position).info2);
 		if(mChars.get(position).favorite){
 			holder.ivFavorite.setImageResource(R.drawable.favorite);
 		}else{
 			holder.ivFavorite.setImageResource(R.drawable.favoritedisabled);
 		}
 		
		holder.tvCharacter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(mCtx, "Character pressed!", Toast.LENGTH_SHORT).show();				
			}
		});
		
 		holder.ivFavorite.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(mCtx, "Star pressed!", Toast.LENGTH_SHORT).show();
 			}
 		});
 		
 		return convertView;
 	}
 
 }
