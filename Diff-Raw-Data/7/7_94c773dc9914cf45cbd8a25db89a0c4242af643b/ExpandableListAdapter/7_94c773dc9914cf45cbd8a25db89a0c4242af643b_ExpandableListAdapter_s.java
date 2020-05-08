 package se3350y.aleph.ScanActivity;
  
 import java.util.ArrayList;
 
 import se3350y.aleph.Listeners.OnElementChangedListener;
 import se3350y.aleph.Listeners.OnInspectionChangedListener;
 import se3350y.aleph.Listeners.OnInspectionElementCompletedListener;
 import se3350y.aleph.firealertscanner.R;
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.TextView;
  
 public class ExpandableListAdapter extends BaseExpandableListAdapter {
 	
  
     private Context context;
     private ArrayList<Equipment> groups;
     
     boolean saving = false;
     
     public void setSaving(boolean _saving){
     	saving = _saving;
     }
     
     public boolean getSaving()
     {
     	return saving;
     }
     
     OnInspectionChangedListener onInspectionChangedListener = null;
 	
 	public void setOnInspectionChangedListener(OnInspectionChangedListener listener) {
 		onInspectionChangedListener = listener;
 	}
 	
 	public boolean groupsCompleted(){
 		
 		Equipment group;
 		
 		for(int i = 0; i < getGroupCount(); i++){
 			
 			group = (Equipment) getGroup(i);
 			
 			if(!group.getCompleted()){
 				return false;
 			}
 			
 		}
 		
 		return true;
 	}
 	
 	// This function is called after the check was complete
 	public void OnInspectionChangeMade(){
 	    // Check if the Listener was set, otherwise we'll get an Exception when we try to call it
 	    if(onInspectionChangedListener!=null) {
 	        onInspectionChangedListener.onInspectionChanged();
 	    }
 	}
         
  
     public ExpandableListAdapter(Context context, ArrayList<Equipment> groups) {
         this.context = context;
         this.groups = groups;
     }
     
     public void addItem(InspectionElement item, Equipment group) {
 		if (!groups.contains(group)) {
 			groups.add(group);
 		}
 		int index = groups.indexOf(group);
 		ArrayList<InspectionElement> ch = groups.get(index).getItems();
 		ch.add(item);
 		groups.get(index).setItems(ch);
 	}
  
     @Override
     public Object getChild(int groupPosition, int childPosition) {
     	ArrayList<InspectionElement> chList = groups.get(groupPosition).getItems();
 		return chList.get(childPosition);
     }
     
     public Object getParent(int groupPosition){
     	Equipment group = (Equipment) getGroup(groupPosition);
     	
     	return group;
     }
  
     @Override
     public long getChildId(int groupPosition, int childPosition) {
         return childPosition;
     }
     
  
     public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView,
 			ViewGroup parent) {
     	
     	
     	//Used to do stuff, I dunno it's hard to explain
     	View view = null;
     	
     	Object child = null;
     	
     	final Equipment groupParent = (Equipment) getGroup(groupPosition);
     	
     	
     	//Gets a reference to the current child
 		
     	if(groupParent.getName().contains("Extinguisher")){
     		child = (ExtinguisherPassFailElement) getChild(groupPosition, childPosition);
     		view = ((ExtinguisherPassFailElement) child).XMLInflator(convertView, parent, context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
     		
     		RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.extinguisherRadioGroup);
     		
     		if(saving){
     			for (int i = 0; i < radioGroup.getChildCount(); i++) {
     					radioGroup.getChildAt(i).setEnabled(false);
     			}
 
     		}
     		else{
     			for (int i = 0; i < radioGroup.getChildCount(); i++) {
 					radioGroup.getChildAt(i).setEnabled(true);
     			}
     		}
     		
     	}
     	else if(groupParent.getName().contains("FireHoseCabinet")){
     		
     		//There's too different input methods here
     		child = (InspectionElement) getChild(groupPosition, childPosition);
     		
 
     		if( ((InspectionElement) child).getName().contains("Hose Re-Rack") || ((InspectionElement) child).getName().contains("Hydrostatic Test Due")){
     			view = ((FireHoseCabinetYesNoElement) child).XMLInflator(convertView, parent, context.getSystemService(context.LAYOUT_INFLATER_SERVICE));
     			
     			RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.fireHoseCabinetRadioGroup);
     			
     			if(saving){
         			for (int i = 0; i < radioGroup.getChildCount(); i++) {
         					radioGroup.getChildAt(i).setEnabled(false);
         			}
 
         		}
         		else{
         			for (int i = 0; i < radioGroup.getChildCount(); i++) {
     					radioGroup.getChildAt(i).setEnabled(true);
         			}
         		}
     			
     		}
     		else{
     			view = ((FireHoseCabinetGoodPoorElement) child).XMLInflator(convertView, parent, context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
     			
     			Spinner spinner = (Spinner) view.findViewById(R.id.goodPoorSpinner);
     			
     			if(saving){
         			spinner.setEnabled(false);
 
         		}
         		else{
         			spinner.setEnabled(true);
         		}
     			
     		}
     		
     	}
     	else if(groupParent.getName().contains("EmergencyLight")){
     		child = (EmergencyLightYesNoElement) getChild(groupPosition, childPosition);
     		view = ((EmergencyLightYesNoElement) child).XMLInflator(convertView, parent, context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
     		
     		
     		RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.emergencyLightRadioGroup);
 			
 			if(saving){
     			for (int i = 0; i < radioGroup.getChildCount(); i++) {
     					radioGroup.getChildAt(i).setEnabled(false);
     			}
 
     		}
     		else{
     			for (int i = 0; i < radioGroup.getChildCount(); i++) {
 					radioGroup.getChildAt(i).setEnabled(true);
     			}
     		}
     		
     	}
     	
     	
 		
 		//Sets Text
 		final TextView tv = (TextView) view.findViewById(R.id.inspectionElement);
 		tv.setText(((InspectionElement) child).getName().toString());
 		if (((InspectionElement) child).getChanged()) {
 			tv.setText("*"+tv.getText());
 		}
 		tv.setTag(((InspectionElement) child).getTag());
 		final Object childFinal = child;
 		
 		((InspectionElement) child).setOnElementChangedListener(new OnElementChangedListener(){
 			@Override
 			public void onElementChanged() {
 				// TODO Auto-generated method stub
 				Log.i("ExpandableListAdapter","Element Change Made");
 				OnInspectionChangeMade();
 				if (!((String) tv.getText()).contains("*")) {
 					((InspectionElement) childFinal).setChanged(true);
 					tv.setText("*"+tv.getText());
 				}
 				if (!groupParent.getName().contains("*")) {
 					groupParent.setChanged(true);
 					groupParent.setName("*"+groupParent.getName());
					notifyDataSetChanged();
 				}
 				
 			}
 		});
 		
 		
 		
 		((InspectionElement) child).setOnInspectionElementCompletedListener(new OnInspectionElementCompletedListener(){
 			@Override
 			public void onInspectionElementComplete() {
 				tv.setTextColor(Color.GREEN);
				//notifyDataSetChanged();
 			}
 		});
 		
 		if(((InspectionElement) child).getCompleted()){
 			tv.setTextColor(Color.GREEN);
 			
 		}
 		
 		return view;
 	}
     
     public boolean isGroupCompleted(int groupPosition){
     	
     	boolean test = true;
     	InspectionElement child;
     	
     	for(int i = 0; i < getChildrenCount(groupPosition); i++){
     		
     		child = (InspectionElement) getChild(groupPosition, i);
     		
     		test = child.getCompleted();
     		
     		if(test == false){
     			return false;
     		}
     		
     	}
     	
     
     	return true;
     	
     }
  
     public int getChildrenCount(int groupPosition) {
 		// TODO Auto-generated method stub
 		ArrayList<InspectionElement> chList = groups.get(groupPosition).getItems();
 
 		return chList.size();
 
 	}
  
     @Override
     public Object getGroup(int groupPosition) {
     	return groups.get(groupPosition);
     }
  
     @Override
     public int getGroupCount() {
     	return groups.size();
     }
  
     @Override
     public long getGroupId(int groupPosition) {
         return groupPosition;
     }
  
     public View getGroupView(int groupPosition, boolean isLastChild, View view,
 			ViewGroup parent) {
     	
 		Equipment group = (Equipment) getGroup(groupPosition);
 		
 		if (view == null) {
 			LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inf.inflate(R.layout.list_group, null);
 		}
 		
 		if(isGroupCompleted(groupPosition)){
 			group.setCompleted(true);
 		}
 		
 		if(group.getCompleted()){
 			group.setColor(Color.GREEN);
 		}
 		else
 			group.setColor(Color.BLACK);
 		
 		TextView tv = (TextView) view.findViewById(R.id.lblListHeader);
 		tv.setText(group.getName());
 		tv.setTextColor(group.getColor());
 		
 		// TODO Auto-generated method stub
 
 		tv = (TextView) view.findViewById(R.id.location);
 		tv.setText(group.getLocation());
 		
 
 		return view;
 	}
  
     @Override
     public boolean hasStableIds() {
         return true;
     }
  
     @Override
     public boolean isChildSelectable(int groupPosition, int childPosition) {
         return true;
     }
 
 	
 }
