 package org.fourdnest.androidclient.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.R;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class EggReaderAdapter extends EggListAdapter {
 
 	public EggReaderAdapter(ViewGroup parent) {
 		super(parent);
 	}
 	
 	public View getView(int arg0, View arg1, ViewGroup arg2) {

		if (arg1 == null) {
 			LayoutInflater inflater = LayoutInflater.from(getParent().getContext());
			arg1 = inflater.inflate(R.layout.egg_element_large, getParent(), false);
 		}
 
 		Egg egg = (Egg) this.getItem(arg0);
 
		ImageView thumbnail = (ImageView) arg1.findViewById(R.id.thumbnail);
		TextView message = (TextView) arg1.findViewById(R.id.message);
		TextView date = (TextView) arg1.findViewById(R.id.date);
 
		return arg1;
 	}
 
 	public void setEggs(List<Egg> eggs) {
 		// TODO: Get a real implementation for this
 		this.eggs = new ArrayList<Egg>();
 		for (int i = 0; i < 8; i++) {
 			this.eggs.add(new Egg());
 		}
 	}
 
 }
