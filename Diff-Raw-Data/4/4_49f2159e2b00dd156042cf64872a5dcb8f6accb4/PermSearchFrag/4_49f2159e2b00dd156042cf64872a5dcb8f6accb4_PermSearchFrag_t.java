 package edu.nkuresearch.securitychecker.fragments;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import edu.nkuresearch.securitychecker.HomeActivity;
 import edu.nkuresearch.securitychecker.R;
 import edu.nkuresearch.securitychecker.SearchResultActivity;
 
 public class PermSearchFrag extends SherlockFragment{
 
 	private LayoutInflater mInflater;
 	private ListView lv;
 	private LinkedList<String> list;
 	private LinkedList<String> selectedList;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		mInflater = inflater;
 		((HomeActivity) getSherlockActivity()).startProgress();
 		View v = inflater.inflate(R.layout.perm_list, container, false);
 		Button scanBtn = (Button) v.findViewById(R.id.headBtn);
 		scanBtn.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getSherlockActivity(), SearchResultActivity.class);
 				intent.putExtra("LIST", selectedList);
 				startActivity(intent);
 			}
 		});
 		lv = (ListView) v.findViewById(R.id.permlist);
 		list = new LinkedList<String>();
 		selectedList = new LinkedList<String>();
 		new ListPerms().execute((Void) null);
 		return v;
 	}
 	
 	class ListPerms extends AsyncTask<Void, Void, Void>{
 
 		@Override
 		protected Void doInBackground(Void... params) { 
 			try {
 				if(getSherlockActivity() != null && getSherlockActivity().getAssets() != null){
 					BufferedReader br = new BufferedReader(new InputStreamReader(getSherlockActivity().getAssets().open("permissionsList.txt")));
 					String line;
 					while((line = br.readLine()) != null){
 						String[] splitStr = line.split(":-:");
 						list.add(splitStr[0]);
 					}
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			lv.setAdapter(new PermListAdapter());
 			if(getSherlockActivity() != null)
 				((HomeActivity) getSherlockActivity()).stopProgress();
 			super.onPostExecute(result);
 		}
 	}
 	
 	private class PermListAdapter implements ListAdapter{
 		@Override
 		public int getCount() {
 			return list.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public int getItemViewType(int position) {
 			return 1;
 		}
 
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			
 			if( convertView == null)
 				convertView = mInflater.inflate(R.layout.check_item, null, false);
 			final TextView tv = (TextView)convertView.findViewById(R.id.checkText);
 			final CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);
			if(selectedList.contains(list.get(position)))
				cb.setChecked(true);
			else
				cb.setChecked(false);
 			tv.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					cb.toggle();
 					if(cb.isChecked())
 						selectedList.add(list.get(position));
 					else
 						selectedList.remove(tv.getText().toString());
 				}
 			});
 			tv.setText(list.get(position));
 			return convertView;
 
 		}
 
 		@Override
 		public int getViewTypeCount() {
 			return 1;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			return false;
 		}
 
 		@Override
 		public boolean isEmpty() {
 			return list.isEmpty();
 		}
 
 		@Override
 		public void registerDataSetObserver(DataSetObserver observer) {
 			
 		}
 
 		@Override
 		public void unregisterDataSetObserver(DataSetObserver observer) {
 			
 		}
 
 		@Override
 		public boolean areAllItemsEnabled() {
 			return false;
 		}
 
 		@Override
 		public boolean isEnabled(int position) {
 			return true;
 		}
 	}
 }
