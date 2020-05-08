 package de.eahapp.gui.adabter;
 
 import java.util.Vector;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import de.eahapp.R;
 
 /**
  * Class to fill the ListView with some data
  */
 public class BusLoaderListAdapter extends EfficientAdapter {
 	private Context ctx;
 
 	public BusLoaderListAdapter(Context context, Vector<?> elements) {
 		super(context, elements);
 		this.ctx = context;
 	}
 
 	@Override
 	protected void insertData(View convertView, int position) {
 		String val = (String) this.elements.get(position);
 		ViewHolder holder = null;
 		holder = (ViewHolder) convertView.getTag();
 		String[] vals = val.split(",");
		holder.title.setText("Ziel: " +vals[0]);
		holder.text.setText("Ankunftszeit: " + vals[1]);
		holder.text2.setText("In: " +vals[2]);
 		holder.textNummer.setText(vals[3]);
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 5;
 	}
 
 
 
 	@Override
 	protected View inflateView(int pos) {
 		View item = null;
 		ViewHolder holder;
 		item = this.mInflater.inflate(R.layout.haltestellen_listview_item_layout, null);
 
 		// Creates a ViewHolder and store references to the two children views
 		// we want to bind data to.
 		holder = new ViewHolder();
 		holder.title = (TextView) item.findViewById(R.id.haltestelle_target_text);
 		holder.text = (TextView) item.findViewById(R.id.ankunftszeit_text);
 		holder.text2 = (TextView) item.findViewById(R.id.ankuftszeit_2_text);
 		holder.textNummer = (TextView) item.findViewById(R.id.nummer_text);
 		holder.image = (ImageView) item.findViewById(R.id.grid_view_item_list_item_image);
 		item.setTag(holder);
 		return item;
 	}
 
 	class ViewHolder {
 		TextView title;
 		TextView text;
 		TextView text2;
 		TextView textNummer;
 		ImageView image;
 	}
 
 
 
 
 }
