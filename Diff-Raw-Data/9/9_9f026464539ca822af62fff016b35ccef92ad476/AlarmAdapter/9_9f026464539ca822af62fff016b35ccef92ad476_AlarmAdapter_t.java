 package com.utopia.lijiang;
 
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.Activity;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.utopia.lijiang.alarm.Alarm;
 import com.utopia.lijiang.alarm.AlarmManager;
 import com.utopia.lijiang.alarm.LocationAlarm;
 import com.utopia.lijiang.global.Status;
 
 /** Adapter for show Alarm list
  *  This activity is bound with alarm_listitem.xml
  * @author chao_zhou
  * @version 1.0.0.0
  * */
 public class AlarmAdapter extends BaseAdapter {
 
 	public final static int ListAlarmViewState = 0;
 	public final static int EditAlarmViewState = 1;
 	
 	
 	private int viewState = AlarmAdapter.ListAlarmViewState;
 	private List<Alarm> data = null;
 	private Activity activity = null;
 	private LayoutInflater layoutInflater; 
 	
 	public int getViewState() {
 		return viewState;
 	}
 
 	public void setViewState(int viewState) {
 		this.viewState = viewState;
 	}
 	
 	/**Constructor
 	 * @param context Current context
 	 * @param data Alarm list
 	 * */
 	public AlarmAdapter(Activity activity,List<Alarm> data){
 		this.data = data;
 		this.activity = activity;
 		this.layoutInflater = LayoutInflater.from(activity); 
 		this.viewState = AlarmAdapter.ListAlarmViewState;
 	}
 	
 	@Override
 	public int getCount() {
 		return data.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return data.get(position);
 	}
 	
 	@Override
 	public long getItemId(int position) {
 		return data.get(position).getId();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
       
 		if(viewState == AlarmAdapter.ListAlarmViewState){
 			return getListView(position,convertView,parent);
 		}
 		else{
 			return getEditView(position,convertView,parent);
 		}
 	}	
 	
 	
 	private View getEditView(int position, View convertView, ViewGroup parent) {
 		if(convertView == null){
 			Log.d("lijiang","inflate a new view");
 			convertView = layoutInflater.inflate(R.layout.alarm_edititem, null);
 		}
 		
         Alarm item = (Alarm)getItem(position);
         convertView.setTag(item);
         
         setTitle(item,convertView);
         setMessage(item,convertView);
         setDelete(item,convertView);
         setDistance(item,convertView);
 		return convertView;
 	}
 
 	private View getListView(int position, View convertView, ViewGroup parent){
 		if(convertView == null){
 			Log.d("lijiang","inflate a new view");
 			convertView = layoutInflater.inflate(R.layout.alarm_listitem, null);
 		}
 		
         Alarm item = (Alarm)getItem(position);
         convertView.setTag(item);
         
         setTitle(item,convertView);
         setMessage(item,convertView);
         setActiveAction(item,convertView);
         setDistance(item,convertView);
 		return convertView;
 		
 	}
 	
 	private void setDelete(final Alarm item, View convertView){
 		Button delButton = (Button)convertView.findViewById(R.id.alarmDelete);
 		delButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				AlarmManager mgr = AlarmManager.getInstance();
 				mgr.removeAlarm(item);
 				mgr.delete2DB(activity);
 				AlarmAdapter.this.notifyDataSetChanged();
 			}
 		
 		});
 	}
 	
 	private void setTitle(Alarm item, View convertView){
 		TextView title = (TextView)convertView.findViewById(R.id.alarmTitle);
 		title.setText(item.getTitle());
 	}
 	
 	private void setMessage(Alarm item, View convertView) {
 		// TODO Auto-generated method stub
 		TextView message = (TextView)convertView.findViewById(R.id.alarmMessage);
 		message.setText(item.getMessage());
 	}
 	
 	private void setActiveAction(final Alarm item, View convertView)
 	{
 		ToggleButton actButton = (ToggleButton)convertView.findViewById(R.id.alarmActive);
 		actButton.setChecked(item.isActive());
 		actButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				item.setActive(isChecked);
 			}});	
 	}
 	
 	private void setDistance(Alarm item, View convertView) {
 		// TODO Auto-generated method stub
 		final LocationAlarm locAlarm = (LocationAlarm)item;	
 		final TextView disView = (TextView)convertView.findViewById(R.id.alarmDistance);
 		String disString =getDistanceString(locAlarm.getDistance()); 
 		disView.setText(disString);
 		
 		Status.getCurrentStatus().addObserver(new Observer(){
 
 			@Override
 			public void update(Observable arg0, Object arg1) {
 				// TODO Auto-generated method stub
 				Log.d("lijiang","location changed, recaculate distance");
 				String disString =getDistanceString(locAlarm.getDistance());
 				disView.setText(disString);
 			}});
 	}
 	
 	private String getDistanceString(double meter){
		if(meter == -1)
 			return "???";
 		
 		if(meter<1000){return String.format("~%1$.0f", meter);}
 		else if(meter/1000 < 10){return String.format("~%1$.0f", meter/1000);}
 		else {return ">10Km";}
 	}
 
 	
 }
