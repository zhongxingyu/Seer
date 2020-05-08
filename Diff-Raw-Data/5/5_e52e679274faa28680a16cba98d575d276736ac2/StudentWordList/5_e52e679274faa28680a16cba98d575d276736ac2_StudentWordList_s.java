 package com.example.wordsforkids;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.Vector;
 
 import com.example.utils.Utils;
 
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class StudentWordList extends Activity {
 	public final static String WORD_ID = "com.example.wordsforkids.WORD_ID";
 
 	Vector<String> scores;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_student_word_list);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		final List<Photo> photos = WordListOpenHelper.getInstance(this).getAllPhotos();
 		Vector<String> pics = new Vector<String>();
 		scores = new Vector<String>();
 		for (Photo photo : photos) {
 		    pics.add(photo.getFilename());
 		    scores.add(photo.getScore()+"");
 		}
 
 		
 		GridView gridview = (GridView) findViewById(R.id.gridview);
 		LazyAdapter adapter = new LazyAdapter(this, pics);
  
         gridview.setAdapter(adapter);
  
 		final Context me = this;
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent intent = new Intent(StudentWordList.this, StudentAnswer.class);
 				intent.putExtra(WORD_ID, photos.get(position).getID()+"");
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 				startActivity(intent);
 			}
 		});
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.student_word_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	
     public class LazyAdapter extends BaseAdapter {
         
         private Activity activity;
         private Vector<String> data;
         private LayoutInflater inflater = null;
         
         public LazyAdapter(Activity a, Vector<String> d) {
             activity = a;
             data=d;
             inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
 
         public int getCount() {
             return data.size();
         }
 
         public Object getItem(int position) {
             return position;
         }
 
         public long getItemId(int position) {
             return position;
         }
         
         public View getView(int position, View convertView, ViewGroup parent) {
             View vi=convertView;
             if(convertView==null)
                 vi = inflater.inflate(R.layout.gridbox, null);
 
             ImageView image=(ImageView)vi.findViewById(R.id.imageView2);
             TextView text = (TextView)vi.findViewById(R.id.textView2);
 //            text.setText(position + "");
             text.setText(scores.get(position));
             if (Integer.parseInt(scores.get(position)) <= 0) {
            	text.setTextColor(Color.parseColor("#FF0000"));
             } else {
            	text.setTextColor(Color.parseColor("#00FF00"));
             }
             displayImage(data.get(position), image);
             return vi;
         }
     }
     
     public void displayImage(String url, ImageView imageView) {
         FileInputStream in;
         try {
             in = new FileInputStream(url);
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inSampleSize = 10;
             Bitmap bmp = BitmapFactory.decodeStream(in, null, options);
             imageView.setImageBitmap(bmp);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
     }
 
 }
