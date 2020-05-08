 package fr.s13d.photobackup.journal;
 
 import java.io.File;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.ThumbnailUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import fr.s13d.photobackup.R;
 
 public class JournalAdapter extends BaseAdapter {
 	private final Activity activity;
 	private final List<JournalEntry> values;
 	private static LayoutInflater inflater=null;
 
 	public JournalAdapter(Activity newActivity, List<JournalEntry> newValues) {
 		activity = newActivity;
 		values = newValues;
 		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 
 	@Override
 	public int getCount() {
 		return values.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return values.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View vi = convertView;
 		if(convertView == null) {
 			vi = inflater.inflate(R.layout.list_row, null);
 		}
 
		// picture
		ImageView thumbImage = (ImageView)vi.findViewById(R.id.list_image);
 		JournalEntry entry = values.get(position);
 		Bitmap fullResolutionPicture = BitmapFactory.decodeFile(entry.getFilename());
 		Bitmap thumbnailPicture = ThumbnailUtils.extractThumbnail(fullResolutionPicture, 180, 180);
		thumbImage.setImageBitmap(thumbnailPicture);
 
 		// filename
 		TextView textView = (TextView)vi.findViewById(R.id.filename);
 		File file = new File(entry.getFilename());
 		textView.setText(file.getName());
 
 		return vi;
 	}
 }
