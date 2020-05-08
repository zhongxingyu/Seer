 package org.jempe.counter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.DisplayMetrics;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.AlphaAnimation;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private Vibrator mVibrator = null;
 	private TextView mDisplayCount;
 	private TapCounter mTapCounter = new TapCounter();
 	public static final String PREFS_NAME = "CounterPrefs";
 	private TextView mTapMessage;
 	private TextView mCounterName;
 	private ImageView mCountSign;
 	private boolean mTapMessageHidden;
 	private boolean mCountForward;
 	private Typeface mNunito;
 	private int mHeight;
 	private int mWidth;
 	private SharedPreferences mSettings;
 	private ImageButton mDecreaseButton;
 	private View mSetInitialLayout;
 	private View mSetCounterNameLayout;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         mDisplayCount = (TextView)findViewById(R.id.displayCount);
         mTapMessage = (TextView)findViewById(R.id.tap_to_count_message);
         mCounterName = (TextView)findViewById(R.id.CounterName);
         mCountSign = (ImageView) findViewById(R.id.countSign);
         mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
         mDecreaseButton = (ImageButton) findViewById(R.id.decreaseButton);
         
         // Load fonts from assets folder
         mNunito = Typeface.createFromAsset(getAssets(), "Nunito-Regular.ttf");  
         
         // assign fonts to textViews
         mDisplayCount.setTypeface(mNunito);
     }
     
     public void onResume()
     {
     	super.onResume();
     	
         // get screen size
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
         mHeight = metrics.heightPixels;
         mWidth = metrics.widthPixels;
         
         // set size ratio between landscape and portrait
         float portrait_landscape_ratio = (float) 0.7;
         float count_sign_position = (float) 0.8;
         
         // if it is portrait dont resize the items
         if(mHeight > mWidth)
         {
         	portrait_landscape_ratio = count_sign_position = (float) 1.0;
         }
         
         // set the size of the textviews
         mDisplayCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHeight / (8 * portrait_landscape_ratio));
         mCounterName.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHeight / (18 * portrait_landscape_ratio));
         
         int hintFontRatio = 24;
         
         if(mHeight > 1000)
         {
         	hintFontRatio = 36;
         }
         else if(mHeight > 600)
         {
         	hintFontRatio = 32;
         }
         
         mTapMessage.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHeight / (hintFontRatio * portrait_landscape_ratio));
         
         int sign_width = (int) ((int) mHeight / (9 * portrait_landscape_ratio));
         
  
         RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(sign_width, sign_width);
         layoutparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
         layoutparams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
         layoutparams.rightMargin = 20;
         layoutparams.topMargin = (int) ((mHeight - (mHeight / (1.618 / count_sign_position))) - (sign_width * 1.5));
         mCountSign.setLayoutParams(layoutparams);
         
        RelativeLayout.LayoutParams layoutparamsname = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
        		RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutparamsname.addRule(RelativeLayout.ALIGN_RIGHT, R.id.countSign);
        layoutparamsname.addRule(RelativeLayout.BELOW, R.id.displayCount);
        layoutparamsname.topMargin = (int) ((int) (mHeight / (18 * portrait_landscape_ratio)) * -0.6);
        mCounterName.setLayoutParams(layoutparamsname);
        
         RelativeLayout.LayoutParams layoutparamsdec = new RelativeLayout.LayoutParams(sign_width, sign_width);
         layoutparamsdec.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
         layoutparamsdec.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
         layoutparamsdec.leftMargin = 20;
         layoutparamsdec.bottomMargin = 20;
         mDecreaseButton.setLayoutParams(layoutparamsdec);
         
         if(mHeight < mWidth)
         {
             RelativeLayout.LayoutParams layoutparamsdisplay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
             		RelativeLayout.LayoutParams.WRAP_CONTENT);
             layoutparamsdisplay.addRule(RelativeLayout.LEFT_OF, R.id.countSign);
             layoutparamsdisplay.addRule(RelativeLayout.ALIGN_TOP, R.id.countSign);
             layoutparamsdisplay.rightMargin = 8;
             layoutparamsdisplay.topMargin = sign_width / -5;
             mDisplayCount.setLayoutParams(layoutparamsdisplay);
         }
     	
     	
     	mSettings = getSharedPreferences(PREFS_NAME, 0);
 		
 		mTapCounter.changeCount(mSettings.getInt("latest_count", 0));
 		mCountForward = mSettings.getBoolean("count_forward", true);
 		
 		setIcons();
 		
         String currentCount = mTapCounter.getCount();
         String currentCounterName = mSettings.getString("counter_name", getString(R.string.default_counter_name));
         
         mCounterName.setText(currentCounterName);
         mDisplayCount.setText(currentCount);
         showTapMessage();
     }
     
     public void onPause()
     {
     	super.onPause();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
      
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         // Handle item selection
         switch (item.getItemId()) {
         	case R.id.view_source_menu:
         		viewSource();
         		return true;
             case R.id.count_type_menu:
                 toggleCountType();
                 return true;
             case R.id.reset_count_menu:
             	AlertDialog.Builder builder = new AlertDialog.Builder(this);
             	
             	builder.setMessage(R.string.reset_message)
                 .setTitle(R.string.reset_title);
             	
             	// Add the buttons
             	builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
             	           public void onClick(DialogInterface dialog, int id) {
             	            	resetCount();
             	           }
             	       });
             	builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
             	           public void onClick(DialogInterface dialog, int id) {
             	           }
             	       });
 
             	// Create the AlertDialog
             	AlertDialog dialog = builder.create();
             	dialog.show();
             	return true;
             case R.id.set_initial_count_menu:
                 AlertDialog.Builder builderInitialCount = new AlertDialog.Builder(this);
                 // Get the layout inflater
                 LayoutInflater inflater = this.getLayoutInflater();
 
                 mSetInitialLayout = inflater.inflate(R.layout.initial_count, null);
                 
                 // Inflate and set the layout for the dialog
                 // Pass null as the parent view because its going in the dialog layout
                 builderInitialCount.setView(mSetInitialLayout)
                 // Add action buttons
                        .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                         	   EditText mValue   = (EditText) mSetInitialLayout.findViewById(R.id.initial_number);
                         	   
                         	   String SelectedValue = mValue.getText().toString();
                         	   
                         	   if(SelectedValue.length() > 0)
                         	   {
                         		   setCount(Integer.parseInt(SelectedValue));
                         	   }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });      
                 AlertDialog dialogInitialCount = builderInitialCount.create();
             	dialogInitialCount.show();
             	return true;
             case R.id.set_counter_name_menu:
                 AlertDialog.Builder builderCounterName = new AlertDialog.Builder(this);
                 // Get the layout inflater
                 LayoutInflater inflaterCounterName = this.getLayoutInflater();
 
                 mSetCounterNameLayout = inflaterCounterName.inflate(R.layout.counter_name, null);
                 
                 // Inflate and set the layout for the dialog
                 // Pass null as the parent view because its going in the dialog layout
                 builderCounterName.setView(mSetCounterNameLayout)
                 // Add action buttons
                        .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                         	   EditText mValue   = (EditText) mSetCounterNameLayout.findViewById(R.id.set_counter_name);
                         	   
                         	   String SelectedValue = mValue.getText().toString();
                         	   
                         	   if(SelectedValue.length() > 0)
                         	   {
                         		   mCounterName.setText(SelectedValue);
                         		   
                         			SharedPreferences.Editor editor = mSettings.edit();
                         			editor.putString("counter_name", SelectedValue);
 
                         			editor.commit();
                         	   }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });      
                 AlertDialog dialogCounterName = builderCounterName.create();
             	dialogCounterName.show();
             	return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     public void incCount(View view)
     {
     	if(mCountForward == true)
     	{
     		mTapCounter.increaseCount();
     	}
     	else
     	{
     		mTapCounter.decreaseCount();
     	}
     	saveCount();
     	
         String currentCount = mTapCounter.getCount();
         
         mDisplayCount.setText(currentCount);
     	mVibrator.vibrate(50);
     	if(mTapMessageHidden == false)
     	{
     		hideTapMessage();
     	}
     }
     
     public void decreaseCount(View view)
     {
     	if(mCountForward == false)
     	{
     		mTapCounter.increaseCount();
     	}
     	else
     	{
     		mTapCounter.decreaseCount();
     	}
     	saveCount();
     	
         String currentCount = mTapCounter.getCount();
         
         mDisplayCount.setText(currentCount);
     	mVibrator.vibrate(150);
     }
     
     public void resetCount()
     {
    		mTapCounter.resetCount();
    		saveCount();
 	
    		String currentCount = mTapCounter.getCount();
     
    		mDisplayCount.setText(currentCount);
     }
     
     public void setCount(int CountValue)
     {
    		mTapCounter.changeCount(CountValue);
    		saveCount();
 	
    		String currentCount = mTapCounter.getCount();
     
    		mDisplayCount.setText(currentCount);
     }
     
     public void viewSource()
     {
     	Uri webpage = Uri.parse("https://github.com/jempe/-tapcounter-android");
     	Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
     	
     	startActivity(webIntent);
     }
     
     private void saveCount()
     {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putInt("latest_count", mTapCounter.tap_count);
 
 		editor.commit();
     }
     
     private void toggleCountType()
     {
 		if(mCountForward == true)
 		{
 			mCountForward = false;
 		}
 		else
 		{
 			mCountForward = true;
 		}
 		
 		setIcons();
     
 		SharedPreferences.Editor editor = mSettings.edit();
 		editor.putBoolean("count_forward", mCountForward);
 
 		editor.commit();
     }
     
     private void setIcons()
     {
     	if(mCountForward == false)
 		{
     		mCountSign.setImageResource(R.drawable.minus_light_blue);
     		mCountSign.setContentDescription("-");
     		mDecreaseButton.setImageResource(R.drawable.plus);
     		mDecreaseButton.setContentDescription("+");
 			mTapMessage.setText(R.string.tap_to_count_backward);
 		}
 		else
 		{
     		mCountSign.setImageResource(R.drawable.plus_light_blue);
     		mCountSign.setContentDescription("+");
     		mDecreaseButton.setImageResource(R.drawable.minus);
     		mDecreaseButton.setContentDescription("-");
 			mTapMessage.setText(R.string.tap_to_count);
 		}
     }
     
     @Override
     public void onBackPressed()
     {
         decreaseCount(mCountSign);
     }
     
     private void hideTapMessage()
     {
     	AlphaAnimation fadeOutAnimation = new AlphaAnimation(1, (float) 0);
     	fadeOutAnimation.setDuration(1500);
     	fadeOutAnimation.setFillAfter(true);
     	
     	mTapMessage.setAnimation(fadeOutAnimation);
     	mTapMessageHidden = true;
     }
     
     private void showTapMessage()
     {
     	AlphaAnimation fadeInAnimation = new AlphaAnimation((float) 0, 1);
     	fadeInAnimation.setDuration(1500);
     	fadeInAnimation.setFillAfter(true);
     	
     	mTapMessage.setAnimation(fadeInAnimation);
     	mTapMessageHidden = false;
     }
 }
