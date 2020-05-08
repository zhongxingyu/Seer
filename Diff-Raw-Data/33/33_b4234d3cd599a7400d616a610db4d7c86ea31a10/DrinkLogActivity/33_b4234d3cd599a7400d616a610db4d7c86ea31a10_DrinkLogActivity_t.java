 package org.g_okuyama.log;
 
 import java.io.File;
 import java.util.Locale;
 
 import com.ngigroup.adstir.AdstirTerminate;
 import com.ngigroup.adstir.AdstirView;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Camera;
 import android.opengl.Matrix;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.Transformation;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 public class DrinkLogActivity extends Activity {
     public static final String TAG = "DrinkLog";
     
     public static final int CATEGORY_WHISKEY = 0;
     public static final int CATEGORY_COCKTAIL = 1;
     public static final int CATEGORY_WINE = 2;
     public static final int CATEGORY_SHOCHU = 3;
     public static final int CATEGORY_SAKE = 4;
     public static final int CATEGORY_BRANDY = 5;
     public static final int CATEGORY_BEER = 6;
     public static final int CATEGORY_OTHER = 7;
     
     private static final int MENU_SETTINGS = 0;
     private static final int MENU_LINK = 1;
 
     private EditText mEditText = null;
     private int mItemIdx = 0;
     
     private AdstirView mAdstirView;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         ImageView image = (ImageView)findViewById(R.id.logo_noicon);
         if(!Locale.getDefault().equals(Locale.JAPAN)){
         	image.setImageResource(R.drawable.logo_eng);
         }
         
         setListener();
     }
     
     public void onStart(){
     	super.onStart();
     	
         ImageView imageView = (ImageView)findViewById(R.id.logo_icon);
         animation.setDuration(1500);
         imageView.startAnimation(animation);
     }
     
     private void setListener(){
         Button register_btn = (Button)this.findViewById(R.id.register);
         register_btn.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 registerConfirm();
             }
         });
 
         Button refer_btn = (Button)this.findViewById(R.id.refer);
         refer_btn.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 Intent intent = new Intent(DrinkLogActivity.this, LogListActivity.class);
                 startActivity(intent);
             }
         });
         
         //adstirݒ
        /*
         LinearLayout layout = (LinearLayout)findViewById(R.id.adspace);
         mAdstirView = new AdstirView(this, 1);
         layout.addView(mAdstirView);
        */
     }
     
     private void registerConfirm(){
         final String[] itemList = getResources().getStringArray(R.array.category_array);
         
         new AlertDialog.Builder(this)
         .setIcon(android.R.drawable.ic_dialog_info)
         .setTitle(R.string.dialog_category)
         .setSingleChoiceItems(itemList, 0, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 mItemIdx = which;
             }
         })
         .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 Intent intent = new Intent(DrinkLogActivity.this, RegisterActivity.class);
                 intent.putExtra("category", mItemIdx);
             	intent.putExtra("from", "DrinkLogActivity");
                 startActivity(intent);
             }
         })
         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 //Ȃ
             }
         })
        .show();        
     }
     
     /*
      * IvVj[̍쐬
      * Eݒ
      * EN
      */
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         MenuItem edit = menu.add(0, MENU_SETTINGS, 0 ,R.string.menu_settings);
         edit.setIcon(android.R.drawable.ic_menu_preferences);
 
         return true;
     }
     
     //IvVj[ĨXi
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case MENU_SETTINGS:
             setting();
             break;
             
             //TODO:Lłւ̃N
             
         default:
             //Ȃ
         }
 
         return true;
     }
     
     void setting(){
         Intent pref_intent = new Intent(this, DrinkLogPreference.class);
         startActivity(pref_intent);
     }
 
     
     protected void onPause(){
     	super.onPause();
     	if(mAdstirView != null){
     	    mAdstirView.stop();
     	}
     }
     
     public void finish(){
         new AlertDialog.Builder(this)
         .setTitle(R.string.finish)
         .setMessage(R.string.finish_confirm)
         .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 //AṽLbV폜
                 deleteCache(getCacheDir());
                 System.exit(RESULT_OK);
             }
         })
         .setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 //Ȃ
                 return;
             }
         })
         .show();        
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         new AdstirTerminate(this);
     }
 
     
     Animation animation = new Animation(){
     	Camera mCamera;
     	int mWidth;
     	
     	public void initialize(int width, int height, int parentWidth, int parentHeight){
     		super.initialize(width, height, parentWidth, parentHeight);
     		
     		mCamera = new Camera();
     		mWidth = width;
     	}
     	
     	protected void applyTransformation(float interpolatedTime, Transformation t){
     		android.graphics.Matrix matrix = t.getMatrix();
     		
     		mCamera.save();
     		mCamera.rotateY(360.0f * interpolatedTime);
     		mCamera.getMatrix(matrix);
     		mCamera.restore();
     		
     		matrix.preTranslate(-mWidth/2, 0);
     		matrix.postTranslate(mWidth/2, 0);
     	}
     };
     
     public static boolean deleteCache(File dir) {
         if(dir==null) {
             return false;
         }
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++) {
                 boolean success = deleteCache(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
         return dir.delete();
     }
 }
