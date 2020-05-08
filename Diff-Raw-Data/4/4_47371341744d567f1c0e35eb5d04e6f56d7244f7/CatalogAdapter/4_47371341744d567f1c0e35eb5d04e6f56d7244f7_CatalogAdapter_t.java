 package great.team;
 
import great.team.activities.ItemsOverviewActivity;
 import great.team.entity.Catalog;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 
 public class CatalogAdapter extends BaseAdapter {
 	private List<Catalog> mCatalogs; // root catalogs
 	private Context mContext;
 
 	public CatalogAdapter(Context c, List<Catalog> catalogs) {
 		mContext = c;
 		mCatalogs = catalogs;
 	}
 
 	@Override
 	public int getCount() {
 		return mCatalogs.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return mCatalogs.get(position);
 	}
 	
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	Catalog getCatalogById(String id){
 		for(Catalog cat : mCatalogs){
 			if(cat.getId().toString().equals(id))
 				return cat;
 		}
 		return null;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Button but;
 		if (convertView == null) {
 			but = new Button(mContext);
 			// imageView.setLayoutParams(new GridView.LayoutParams(45, 45));
 		} else {
 			but = (Button) convertView;
 		}
 
 		but.setText(mCatalogs.get(position).getName());
 		but.setTag(mCatalogs.get(position).getId());
 		but.setOnClickListener( new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
				Intent intent = new Intent(mContext, ItemsOverviewActivity.class);
 				String id = v.getTag().toString();
 				System.out.println("id : " + id);
 				Catalog curCat = getCatalogById(id);
 				intent.putExtra("catalog", curCat);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				mContext.startActivity(intent);
 			}
 		});
 		return but;
 	}
 
 }
 
