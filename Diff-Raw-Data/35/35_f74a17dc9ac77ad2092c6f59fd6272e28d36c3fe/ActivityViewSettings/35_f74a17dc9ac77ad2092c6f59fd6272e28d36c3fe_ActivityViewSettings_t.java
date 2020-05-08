 package pl.edu.amu.wmi.lsm.snake;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ActivityViewSettings extends ListActivity  {
    private SharedPreferences mPrefs;
    private final String SHARED_PREFERENCE = "Settings";
    private final String KEY_LANG = "lang";
    public String lang;
    private final String KEY_COL = "color";
    public String color;
    private final String KEY_SOUND = "sound";
    public boolean sound;
 
     @Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//setContentView(R.layout.settings);

                 mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                 String[] settings = getResources().getStringArray(R.array.settings_array);
                 setListAdapter(new ArrayAdapter<String>(this, R.layout.settings, settings));
                 
                 ListView lv = getListView();
                 lv.setTextFilterEnabled(true);
                       lv.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
             int position, long id) {
             int wybor = safeLongToInt(id);
             switch (wybor) {
                 case 0: // change color
                     alertColors();
                     break;
                 case 1: // language change
                     alertLang();
                     break;
                 case 2: // sounds
                     alertSounds();
                     break;
                 case 3: // powrot do glownego
                     Intent i = new Intent(ActivityViewSettings.this, SnakeActivity.class);
 //                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   //Toast.makeText(getApplicationContext(), mPrefs.getString(KEY_COL, " ")+" lol", Toast.LENGTH_SHORT).show();
 
                     i.putExtra(KEY_COL, mPrefs.getString(KEY_COL, "0;255;0"));
 //                    i.putExtra(KEY_LANG, mPrefs.getString(KEY_LANG, "ENG"));
 //                    i.putExtra(KEY_SOUND, mPrefs.getBoolean(KEY_SOUND, true));
                     startActivity(i);
                     break;
             }
 //          Toast.makeText(getApplicationContext(), ((TextView) view).getText() ,
 //              Toast.LENGTH_SHORT).show();
 
         }
       });
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
             super.onSaveInstanceState(outState);
             outState.putString(KEY_COL, color);
             outState.putString(KEY_LANG, lang);
             outState.putBoolean(KEY_SOUND, sound);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedIS) {
             Log.i("onRestoreInstanceState", "onRestoreInstanceState");
             super.onRestoreInstanceState(savedIS);
             color = savedIS.getString(KEY_COL);
             lang = savedIS.getString(KEY_LANG);
             sound = savedIS.getBoolean(KEY_SOUND);
     }
 
 //    @Override
 //    protected void onPause() {
 //        savePreferences(KEY_COL, color);
 //    }
 
 public void alertLang()
     {
         final CharSequence[] items = {"PL", "EN"};
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Pick a language");
         builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 Editor editor = mPrefs.edit();
                 lang = items[item].toString();
                 editor.putString(KEY_LANG, lang);
                 editor.commit();
                 dialog.dismiss();
                // Toast.makeText(getApplicationContext(), tmp+" lol", Toast.LENGTH_SHORT).show();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
 public void alertColors()
 //        Player wants to change color theme for the custom one. He touches part of the
 //screen with change theme button. Then he selects “Custom theme”. He can set colors of his
 //custom theme by entering 3 numbers (between 0 and 255 inclusive) . Colors of elements
 //visible on screen are changed.
     {
         final CharSequence[] items = {"Custom", "Red", "Green", "Blue"};
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Pick a color");
         builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
 
                 if(item == 0) //custom
                 {
                     alertCustom();
                 }
                 if(item == 1) //red
                 {
                     color = "255;0;0";
                 }
                 if(item == 2) //green
                 {
                     color = "0;255;0";
                 }
                 if(item == 3) //blue
                 {
                     color = "0;0;255";
                 }
 //                Editor editor = mPrefs.edit();
 //                editor.putString(KEY_COL, color);
 //                editor.commit();
                 savePreferences(KEY_COL, color);
                 dialog.dismiss();
                 //Toast.makeText(getApplicationContext(), mPrefs.getString(KEY_COL, null), Toast.LENGTH_SHORT).show();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
 public void alertSounds()
     {
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setMessage("Sounds:")
            .setCancelable(false)
            .setPositiveButton("On", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Editor editor = mPrefs.edit();
                    editor.putString(KEY_SOUND, "1");
                    editor.commit();
                }
            })
            .setNegativeButton("Off", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Editor editor = mPrefs.edit();
                    editor.putString(KEY_SOUND, "0");
                    editor.commit();
                }
            });
     AlertDialog alert = builder.create();
     alert.show();
 }
 
 public void alertCustom()
     {
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setMessage("Not yet. Sorry...")
        .setCancelable(false)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
            }
        });
     AlertDialog alert = builder.create();
     alert.show();
 }
 
 private void savePreferences(String key, String value) {
     //SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
    // SharedPreferences.Editor editor = sharedPreferences.edit();
     SharedPreferences.Editor editor = mPrefs.edit();
     editor.putString(key, value);
     editor.commit();
 }
 
 public static int safeLongToInt(long l) {
     if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
         throw new IllegalArgumentException
             (l + " cannot be cast to int without changing its value.");
     }
     return (int) l;
 }
 }
