 package org.fourdnest.androidclient.ui;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.R;
 
 import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class EggListAdapter extends BaseAdapter {
 	protected List<Egg> eggs;
 	private ViewGroup parent;
 
 	public EggListAdapter(ViewGroup parent) {
 		this.eggs = new ArrayList<Egg>();
 		this.setParent(parent);
 	}
 
 	public int getCount() {
 		return this.eggs.size();
 	}
 
 	public Object getItem(int arg0) {
 		return this.eggs.get(arg0);
 	}
 
 	public long getItemId(int arg0) {
 		return arg0;
 	}
 
 	public View getView(int arg0, View arg1, ViewGroup arg2) {
 		View arg1t = arg1;
 		if (arg1t == null) {
 			LayoutInflater inflater = LayoutInflater.from(getParent().getContext());
 			arg1t = inflater.inflate(R.layout.egg_element_small, getParent(), false);
 		}
 
 		Egg egg = (Egg) this.getItem(arg0);
 		Date a = new Date(egg.getLastUpload());
 
 		ImageView thumbnail = (ImageView) arg1t.findViewById(R.id.thumbnail);
 		TextView message = (TextView) arg1t.findViewById(R.id.message);
 		TextView date = (TextView) arg1t.findViewById(R.id.date);
 
 		return arg1t;
 	}
 
 	public void setEggs(List<Egg> eggs) {
 		// TODO: Get a real implementation for this
 		this.eggs = new ArrayList<Egg>();
 		for (int i = 0; i < 8; i++) {
 			this.eggs.add(new Egg());
 		}
 	}
 
	public ViewGroup getParent() {
 		return parent;
 	}
 
	public void setParent(ViewGroup parent) {
 		this.parent = parent;
 	}
 
 }
