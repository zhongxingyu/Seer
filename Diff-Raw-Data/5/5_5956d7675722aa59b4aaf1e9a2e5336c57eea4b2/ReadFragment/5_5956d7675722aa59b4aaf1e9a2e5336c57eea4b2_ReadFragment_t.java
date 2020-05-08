 package org.fourdnest.androidclient.ui;
 
 import org.fourdnest.androidclient.R;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class ReadFragment extends Fragment {
 	
 	public ReadFragment() {
 		super();
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if (container == null) {
             // We have different layouts, and in one of them this
             // fragment's containing frame doesn't exist.  The fragment
             // may still be created from its saved state, but there is
             // no reason to try to create its view hierarchy because it
             // won't be displayed.  Note this is not needed -- we could
             // just run the code below, where we would create and return
             // the view hierarchy; it would just never be used.
             return null;
         }
 		View view = (View)inflater.inflate(R.layout.read_view, container, false);
 		TextView txt = (TextView) view.findViewById(R.id.massiveText);
		String lol = "Read View\n";
 		for(int i = 0; i<1000; i++){
			lol = lol.concat("OL");
 		}
 		txt.setText(lol);
 		return view;
 	}
 }
