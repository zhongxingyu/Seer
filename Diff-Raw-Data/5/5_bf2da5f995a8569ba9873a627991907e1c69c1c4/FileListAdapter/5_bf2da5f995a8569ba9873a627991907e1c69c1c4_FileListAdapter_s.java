 package com.appmogli.recursivelistfragment;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class FileListAdapter extends BaseAdapter {
 
 	private File root = null;
 	private Context context = null;
 	private ArrayList<File> stack = new ArrayList<File>(); 
 	
 	public FileListAdapter(Context context, File root) {
 		this.context = context;
 		this.root = root;
 	}
 	
 	@Override
 	public int getCount() {
 		return root.listFiles().length;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return root.listFiles()[position];
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		TextView tv = null;
 		if(convertView == null) {
 			tv = (TextView) LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
 		} else {
 			tv =(TextView) convertView;
 		}
 		File f = root.listFiles()[position];
 		if(f.isDirectory()) {
			tv.setTextColor(Color.GREEN);
		} else {
 			tv.setTextColor(Color.BLUE);
 		}
 		tv.setText(f.getName());
 		return tv;
 		
 	}
 	
 	public boolean pushRoot(File newRoot) {
 		if(newRoot.isDirectory()) {
 			stack.add(root);
 			setRoot(newRoot);
 			return true;
 		}
 		return false;
 	}
 	
 	private void setRoot(File newRoot) {
 		this.root = newRoot;
 		notifyDataSetChanged();
 	}
 
 	public void pop() {
 		if(stack.size() > 0) {
 			File root = stack.remove(stack.size() - 1);
 			setRoot(root);
 		}
 	}
 
 	public boolean isStackEmpty() {
 		return stack.size() == 0;
 	}
 
 }
