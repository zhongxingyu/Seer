 /**
  * 
  */
 package open.gtaskdroid.adaptors;
 
 import java.util.List;
 
 import open.Gtaskdroid.R;
 
 import android.app.Activity;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 /**
  * @author rajith
  *
  */
 public class CursorArrayAdapter extends ArrayAdapter<CursorAdapterData> {
 
 	
 	private final List<CursorAdapterData> list;
 	private final Activity context;
 	
 	/**
 	 * 
 	 * @param context
 	 * @param list
 	 */
 	public CursorArrayAdapter(Activity context, List <CursorAdapterData> list) {
 		super(context,R.layout.event_list_row, list);
 		this.context=context;
 		this.list=list;		
 	}
 	
 	/**
 	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
 	 */
 	@Override
 	public View getView(int position,View convertView, ViewGroup parent ){
 		View view= null;
 		if (convertView == null) {
 			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.event_list_row, null);
 			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.taskTitle = (TextView) view.findViewById(R.id.taskTitle);
			viewHolder.taskDueDate = (TextView) view.findViewById(R.id.taskDueDate);
			viewHolder.taskDueTime = (TextView) view.findViewById(R.id.taskDueTime);
 
 			view.setTag(viewHolder);
 
 		} else {
 			view = convertView;
 		}
 		ViewHolder holder = (ViewHolder) view.getTag();
 		
 		if(list.get(position).getRowId()<0){
 			view.setFocusable(true);
 			view.setBackgroundColor(0xff36454F);//view.setBackgroundColor(0xff444444);eee9e9,36454F
 			fillViews(position, holder);
 			holder.taskTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,22);
 			holder.taskTitle.setTypeface(null, 2);			
 		}else{
 			fillViews(position, holder);
 		}
 		
 		if(list.get(position).isOverdue()){
			setTextColor(holder,0xff696969);
 		}else{
			setTextColor(holder,0xffffffff);
 		}
 		
 
 		return view;
 	}
 
 	/**
 	 * @param holder
 	 */
 	private void setTextColor(ViewHolder holder, int color) {
 		holder.taskTitle.setTextColor(color);
 		holder.taskDueDate.setTextColor(color);
 		holder.taskDueTime.setTextColor(color);
 	}
 
 	/**
 	 * @param position
 	 * @param holder
 	 */
 	private void fillViews(int position, ViewHolder holder) {
 		
 		holder.taskTitle.setText(list.get(position).getEventTitle());
 		holder.taskDueDate.setText(list.get(position).getWeekDate());
 		holder.taskDueTime.setText(list.get(position).getEventTime());
 	}
 	
 	/**
 	 * to hold row data without requesting again and again
 	 * @author rajith
 	 *
 	 */
 	static class ViewHolder {
 		
 	
 		protected TextView taskTitle;
 		protected TextView taskDueDate;
 		protected TextView taskDueTime;
 	}
 
 }
