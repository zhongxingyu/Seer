 package moses.client.preferences;
 
 import moses.client.R;
 
 import android.app.Activity;
 
 import android.content.Context;
 import android.content.DialogInterface;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.ImageView;
 
 public class ImageArrayAdapter extends ArrayAdapter<CharSequence> {
 	private boolean[] index = null;
 	private int[] resourceIds = null;
 
 	public ImageArrayAdapter(Context context, int textViewResourceId, CharSequence[] objects, int[] ids, boolean[] i) {
 		super(context, textViewResourceId, objects);
 
 		index = i;
 
 		resourceIds = ids;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public View getView(int position, View convertView, final ViewGroup parent) {
 		final int realposition = position - 1;
 		LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
 		View row = inflater.inflate(R.layout.list_preference_multi_select, parent, false);
 
 		if (realposition >= 0) {
 			ImageView imageView = (ImageView) row.findViewById(R.id.image);
 			imageView.setImageResource(resourceIds[realposition]);
 		}
 
 		CheckedTextView checkedTextView = (CheckedTextView) row.findViewById(R.id.check);
 
 		checkedTextView.setText(getItem(position));
 
 		if (realposition >= 0 && index[realposition]) {
 			checkedTextView.setChecked(true);
 		} else {
 			boolean somethingsFalse = true;
 			for (boolean b : index) {
 				if (!b)
 					somethingsFalse = false;
 			}
 			if (somethingsFalse)
 				checkedTextView.setChecked(true);
 		}
 
 		if (realposition >= 0) {
 			row.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					CheckedTextView checkedTextView = (CheckedTextView) v.findViewById(R.id.check);
 					index[realposition] = !index[realposition];
 					checkedTextView.setChecked(index[realposition]);
					ImageArrayAdapter.this.notifyDataSetChanged();
 				}
 			});
 		} else {
 			row.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					boolean somethingsFalse = false;
 					for (boolean b : index) {
 						if (!b)
 							somethingsFalse = true;
 					}
 					if (somethingsFalse) {
 						for (int i = 0; i < index.length; ++i) {
 							index[i] = true;
 						}
 					} else {
 						for (int i = 0; i < index.length; ++i) {
 							index[i] = false;
 						}
 					}
 					CheckedTextView checkedTextView = (CheckedTextView) v.findViewById(R.id.check);
 					checkedTextView.setChecked(!checkedTextView.isChecked());
					ImageArrayAdapter.this.notifyDataSetChanged();
 				}
 			});
 		}
 		return row;
 	}
 }
