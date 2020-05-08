 package uk.co.ataulmunim.android.widget;
 
 import android.app.Activity;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import com.nicedistractions.shortstacks.R;
 import de.keyboardsurfer.android.widget.crouton.Configuration;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 
 /**
  * Provides extension of Crouton by Ben Weiss.
  * 
  * @author ataulm
  */
 public class CroutonEx {
     private static final String TAG = CroutonEx.class.getSimpleName();
 
     public static final int holoRedLight = 0xffff4444;
     public static final int holoGreenLight = 0xff99cc00;
     public static final int holoBlueLight = 0xff33b5e5;
     public static final int holoOrangeLight = 0xffffbb33;
     
     public static final int holoRedDark = 0xffcc0000;
     public static final int holoGreenDark = 0xff669900;
     public static final int holoBlueDark = 0xff0099cc;
     public static final int holoOrangeDark = 0xffff8800;
 
     /** Default style for alerting the user. */
     public static final Style ALERT;
     /** Default style for confirming an action. */
     public static final Style CONFIRM;
     /** Default style for warning the user. */
     public static final Style WARN;
     /** Default style for general information. */
     public static final Style INFO;
 
     public static final Configuration SHORT_CONFIG;
     
     static {
         ALERT = new Style.Builder()
                 .setBackgroundColorValue(holoRedLight)
                .setHeightDimensionResId(R.dimen.actionbar_height)
                 .build();
         CONFIRM = new Style.Builder()
                 .setBackgroundColorValue(holoGreenLight)
                .setHeightDimensionResId(R.dimen.actionbar_height)
                 .build();
         WARN = new Style.Builder()
                 .setBackgroundColorValue(holoOrangeLight)
                .setHeightDimensionResId(R.dimen.actionbar_height)
                 .build();
         INFO = new Style.Builder()
                 .setBackgroundColorValue(holoBlueLight)
                .setHeightDimensionResId(R.dimen.actionbar_height)
                 .build();
 
         SHORT_CONFIG = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT).build();
     }
 
     public static Crouton makeText(Activity activity, int textResource, Style style) {
         return makeText(activity, textResource, style, SHORT_CONFIG, true);
     }
 
     public static Crouton makeText(Activity activity, CharSequence text, Style style) {
         return makeText(activity, text, style, SHORT_CONFIG, true);
     }
 
     /**
      * Returns a {@link Crouton} specifying text, style and configuration.
      * 
      * @param activity
      * @param text
      * @param style
      * @param configuration
      * @param dismissOnClick
      * @return
      */
     public static Crouton makeText(Activity activity, CharSequence text, Style style, Configuration configuration,
             boolean dismissOnClick) {
 
         Crouton crouton = Crouton.makeText(activity, text, style);
         crouton.setConfiguration(configuration);
         if (dismissOnClick) makeDismissOnClick(crouton);
 
         return crouton;
     }
 
     public static Crouton makeText(Activity activity, int textResource, Style style, Configuration configuration,
             boolean dismissOnClick) {
         Crouton crouton = Crouton.makeText(activity, textResource, style);
         crouton.setConfiguration(configuration);        
         if (dismissOnClick) makeDismissOnClick(crouton);
         
         return crouton;
     }
 
     /**
      * Adds an onClickListener to dismiss the {@link Crouton} immediately.
      * 
      * @param crouton
      * @return
      */
     public static Crouton makeDismissOnClick(final Crouton crouton) {
         return crouton.setOnClickListener(new View.OnClickListener() {            
             @Override
             public void onClick(View v) {
                 Crouton.hide(crouton);                
             }
         });
     }
 }
