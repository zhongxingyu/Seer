 package ly.jamie.fontawesomebrowser;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class ChangeFontAdapter extends BaseAdapter {
 	private Map<String, String> _strings;
 	private String[] _keys;
 	private Context _context;
 	private Typeface _typeface;
 	
 	public ChangeFontAdapter(Context context, Map<String,String> strings, Typeface typeface) {
 		_strings = strings;
 		_context = context;
 		_typeface = typeface;
 		_keys = strings.keySet().toArray(new String[_strings.size()]);
 		Arrays.sort(_keys);
 	}
 	
 	public int getCount() {
 		return _keys.length;
 	}
 
 	public Object getItem(int position) {
 		return _strings.get(getKey(position));
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 	
 	public String getKey(int position) {
 		return _keys[position];
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		TextView textViewIconName, textViewIcon;
 		boolean firstInitialization = false;
 		
 		if(convertView != null && convertView.getClass() == TextView.class) {
 			// do nothing
 		}
 		else {
 			firstInitialization = true;
 			
 			LayoutInflater vi = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = vi.inflate(R.layout.icon_row, null);
 		}
 		
		textViewIconName = (TextView) convertView.findViewById(R.id.textViewIconName);
 		textViewIconName.setText(getKey(position));
 		
		textViewIcon = (TextView) convertView.findViewById(R.id.textViewIcon);
 		if(firstInitialization) {
 			textViewIcon.setTypeface(_typeface);
 			textViewIcon.setTextSize(30);
 		}
 		
 		
 		textViewIcon.setText((String) getItem(position));
 
 		return convertView;
 	}
 }
