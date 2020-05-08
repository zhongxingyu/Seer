 package dude.morrildl.lifer.providence;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.ListFragment;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleAdapter;
 import dude.morrildl.lifer.R;
 
 public class EventHistoryFragment extends ListFragment {
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.event_history_fragment, container,
 				false);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		OpenHelper helper = new OpenHelper(getActivity());
 		SQLiteDatabase db = helper.getReadableDatabase();
 		Cursor c = db.query("events", new String[] { "which", "type", "event",
 				"ts" }, null, null, null, null, "ts desc");
 		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
 		c.moveToFirst();
 		while (!c.isAfterLast()) {
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("which", c.getString(0));
 			map.put("type", c.getString(1));
 			map.put("event", c.getString(2));
 			String ts = c.getString(3);
 			SimpleDateFormat sdf = new SimpleDateFormat(
 					"yyyy'-'MM'-'dd'T'HH:mm:ss");
 			try {
 				java.util.Date parsedTs = sdf.parse(ts);
				sdf = new SimpleDateFormat("E, d MMMMMMMMM y 'at' K:m:sa");
 				ts = sdf.format(parsedTs);
 			} catch (ParseException e) {
 				ts = "";
 			}
 			map.put("ts", ts);
 			list.add(map);
 			c.moveToNext();
 		}
 		c.close();
 		db.close();
 
 		SimpleAdapter adapter = new SimpleAdapter(getActivity(), list,
 				R.layout.event_history_row, new String[] { "which", /* "type", */
 				"event", "ts" }, new int[] { R.id.event_which,
 				/* R.id.event_type, */R.id.event_action, R.id.event_ts });
 		setListAdapter(adapter);
 	}
 
 	public void onPause() {
 		super.onPause();
 	}
 }
