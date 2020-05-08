 package com.cs301w01.meatload.adapters;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.cs301w01.meatload.model.Album;
 import com.cs301w01.meatload.model.Tag;
 
 /**
  * Special TagAdapter used with the AutoCompleteTextView in EditPictureActivity.
  * 
  * Returns a TextView with the text set to the current tag's name, as part of the List passed in
  * the constructor.
  * @author Blake Bouchard
  * @see TagAdapter
  */
 public class SimpleTagAdapter extends TagAdapter {
 
 	int resource;
 	String response;
 	Context context;
 	
 	/**
 	 * Main constructor for the SimpleTagAdapter object.
 	 * @param context Context in which the TagAdapter exists
 	 * @param textViewResourceId ID of the textview being used
 	 * @param tags List of tags to attach to the AutoCorrectTextView
 	 */
 	public SimpleTagAdapter(Context context, int textViewResourceId, List<Tag> tags) {
 		super(context, textViewResourceId, tags);
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent) {
 		TextView tagNameView;
 		// Get current album object
 		Tag tag = getItem(position);
 
 		// Inflate the view
 		if (convertView == null) {
 			tagNameView = new TextView(getContext());
 			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi;
			vi = (LayoutInflater) getContext().getSystemService(inflater);
 			tagNameView = (TextView) vi.inflate(resource, null);
 		} else {
 			tagNameView = (TextView) convertView;
 		}
  
         // Assign the appropriate data from tag object
 		tagNameView.setText(tag.getName());
         
         return tagNameView;
     }
 
 }
