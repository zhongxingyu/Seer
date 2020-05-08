 package org.touchirc.view;
 
 import java.util.ArrayList;
 
 import org.touchirc.R;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.text.Editable;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.TextWatcher;
 import android.text.method.LinkMovementMethod;
 import android.text.style.ClickableSpan;
 import android.text.style.ImageSpan;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.TextView;
 
 public class MultipleChannelTextView extends MultiAutoCompleteTextView{
 
 	private ArrayList<String> channelList = new ArrayList<String>();
 
 	public MultipleChannelTextView(Context context) {
 		super(context);
 		init(context);
 	}
 
 	public MultipleChannelTextView(Context context, AttributeSet attrs){
 		super(context, attrs);
 		init(context);
 	}
 
 	public MultipleChannelTextView(Context context, AttributeSet attrs, int defStyle){
 		super(context, attrs, defStyle);
 		init(context);
 	}
 
 	public ArrayList<String> getChannelList() {
 		ArrayList<String> list = new ArrayList<String>();
 		String [] tab = getText().toString().trim().split(" ");
 		for(String s : tab){
			list.add(s);
 		}
 		return list; 
 	}
 
 	public void init(Context context){
 		addTextChangedListener(watcher);
 	}
 
 	private TextWatcher watcher = new TextWatcher(){
 
 		/**
 		 * 
 		 * Verify if the user has pressed space at the last position.
 		 * If positive, re-generate the list of channels.
 		 * 
 		 */
 
 		@Override
 		public void afterTextChanged(Editable s) {
 			if(s.length() > 1 && s.toString().charAt(s.length()-1) == ' '){
 				channelList.removeAll(channelList);
 				String tab[] = s.toString().trim().split(" ");
 				for(String str : tab){
 					channelList.add("#" + str);
 				}
 			}
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			if(count >=1){
 				if(s.charAt(start) == ' ')
 					setChannel(getText().toString().trim().split(" "));
 			}
 
 		}
 	};
 
 	/**
 	 * 
 	 * Generate chips of channels thanks to an array of strings (from the EditText).
 	 * Called only if the user has pressed space at last position.
 	 * 
 	 * @param channels
 	 */
 
 	public void setChannel(String[] channels){
 		if(getText().toString().charAt(getText().length()-1) == ' '){
 
 			SpannableStringBuilder ssb = new SpannableStringBuilder(getText());
 
 			int x = 0; // position of the cursor
 			for (String c : channels){
 				// Create the textview and add the "#" to signify the channel's label
 				TextView tv = createChannelTextView("#" + c);
 				Bitmap bitmap = convertViewToDrawable(tv);
 				BitmapDrawable bmd = new BitmapDrawable(getResources(), bitmap);
 				bmd.setBounds(0, 0, bmd.getIntrinsicWidth(),bmd.getIntrinsicHeight());
 
 				// Add the span (containing the channel's label) on the SpannableStringBuilder, following the previous
 				ssb.setSpan(new ImageSpan(bmd), x, x + c.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 
 				// Make the span text clickable : if the user clicks on a span, he removes the span.
 				// (implicit call of the method afterTextChanged)
 				ClickableSpan clickSpan = new ClickableSpan() {
 
 					@Override
 					public void onClick(View view) {
 						int i = ((EditText) view).getSelectionStart();
 						int j = ((EditText) view).getSelectionEnd();
 						// Manage the following case : the user has removed the space following the span
 						if(getText().toString().contains(" ")){
 							getText().replace(Math.min(i, j), Math.max(i, j)+1, "", 0, 0); // +1 for the space
 						}
 						else{
 							getText().replace(Math.min(i, j), Math.max(i, j), "", 0, 0); // if the user has removed the space
 						}
 					}
 
 				};
 
 				setMovementMethod(LinkMovementMethod.getInstance());
 				ssb.setSpan(clickSpan, x, x + c.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 				x = x + c.length() +1;
 			}
 			setText(ssb);
 			setSelection(getText().length());
 		}
 	}
 
 	// Generate Dynamically a textview
 	public TextView createChannelTextView(String channelName){
 		LayoutInflater l = (LayoutInflater) getContext().getSystemService(SherlockActivity.LAYOUT_INFLATER_SERVICE);
 		TextView tv = (TextView) l.inflate(R.layout.channel_edittext, null);
 		tv.setText(channelName);
 		tv.setTextSize(20);
 		// Add a cross at the end of the text
 		tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.abs__ic_clear_holo_light, 0);
 		return tv;
 	}
 
 	public static Bitmap convertViewToDrawable(View view) {
 		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
 		view.measure(spec, spec);
 		// Resize the textview
 		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
 		// Create the Drawable thanks to a Bitmap
 		Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(b);
 		canvas.translate(-view.getScrollX(), -view.getScrollY());
 		view.draw(canvas);
 		view.setDrawingCacheEnabled(true);
 		Bitmap cacheBmp = view.getDrawingCache();
 		Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
 		view.destroyDrawingCache();
 		return viewBmp;
 	}
 }
