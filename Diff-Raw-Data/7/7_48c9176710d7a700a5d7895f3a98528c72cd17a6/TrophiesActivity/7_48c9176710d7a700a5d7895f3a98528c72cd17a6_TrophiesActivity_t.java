 package jp.knct.di.c6t.ui.collection;
 
 import java.text.ParseException;
 import java.util.List;
 
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.OutcomesClient;
 import jp.knct.di.c6t.model.Trophy;
 import jp.knct.di.c6t.util.ActivityUtil;
import jp.knct.di.c6t.util.ImageUtil;
 import jp.knct.di.c6t.util.TimeUtil;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public class TrophiesActivity extends ListActivity {
 	public List<Trophy> mTrophies;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_trophies);
 
 		OutcomesClient client = new OutcomesClient();
 		try {
 			mTrophies = client.getTrophies(this);
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		setListAdapter(new TrophiesAdapter(this, mTrophies));
 	}
 
 	private class TrophiesAdapter extends ArrayAdapter<Trophy> {
 		private List<Trophy> mTrophies;
 		private LayoutInflater mInflater;
 
 		public TrophiesAdapter(Activity context, List<Trophy> trophies) {
 			super(context, 0, trophies);
 			mTrophies = trophies;
 			mInflater = context.getLayoutInflater();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = mInflater.inflate(R.layout.list_item_trophy, parent, false);
 			}
 
 			Trophy trophy = mTrophies.get(position);
 			new ActivityUtil(convertView)
 					.setText(R.id.list_item_trophy_route_name, trophy.getExploration().getRoute().getName())
					.setText(R.id.list_item_trophy_achieved_at, TimeUtil.format(trophy.getAchievedAt()))
					.setImageBitmap(R.id.list_item_trophy_group_photo, ImageUtil.decodeBitmap(trophy.getPhotoUri(), 10));
 
 			return convertView;
 		}
 
 	}
 }
