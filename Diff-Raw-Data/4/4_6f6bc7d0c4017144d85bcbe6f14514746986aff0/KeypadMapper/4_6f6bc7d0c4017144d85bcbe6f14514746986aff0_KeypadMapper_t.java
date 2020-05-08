 package org.osm.keypadmapper;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.Date;
 
 import junit.framework.Assert;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.TextView;
 import org.osm.keypadmapper.R;
 
 public class KeypadMapper extends Activity {
 /*    private int ids[] = { R.id.button_0, R.id.button_1, R.id.button_2,
     		R.id.button_3, R.id.button_4, R.id.button_5, R.id.button_6,
     		R.id.button_7, R.id.button_8, R.id.button_9, R.id.button_C,
     		R.id.button_DEL, R.id.button_L, R.id.button_F, R.id.button_R
     };*/
     private String val = "";
     void SetButtons (ViewGroup btnGroup)
     {
         for (int i = 0; i < btnGroup.getChildCount(); i++) {
         	if (ViewGroup.class.isInstance(btnGroup.getChildAt(i))) {
         		SetButtons ((ViewGroup) btnGroup.getChildAt(i));
         	}
         	else if (Button.class.isInstance(btnGroup.getChildAt(i))){
             Button button = (Button) btnGroup.getChildAt(i);//findViewById (ids[i]);
             button.setOnClickListener(new Button.OnClickListener() {
           	  private void Do (String s) {
           		  PrintWriter out = null;
           		  Date d = new Date ();
           		  try {
           			  out = new PrintWriter (new FileOutputStream (
           					  "/sdcard/keypadmapper", true));
           			  out.print (d.getTime());
               		  out.print (s);
               		  out.println (val);
           		  } catch (FileNotFoundException e) {
           			  Assert.assertNotNull ("Error writing the file!", out);
   				} finally {
           			  if (out != null) out.close ();        			  
              			  Assert.assertNotNull ("Error writing the file!", out);
          		  }
       			  /*if (out == null) AlertDialog.Builder(mContext)
                     .setIcon(R.drawable.alert_dialog_icon)
                     .setTitle("Error writing file !")*/
                     /*.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
 
                         }
                     }); */
                     //.create ();
       			  val = "";        		  
           	  }
           	  public void onClick (View v) {
                     if (v == findViewById (R.id.button_C)) val = "";
                    else if (v == findViewById (R.id.button_DEL)) {
                    	if (val.length() > 0) val = val.substring(0, val.length()-1);
                    }
                     else if (v == findViewById (R.id.button_L)) Do ("L");
                     else if (v == findViewById (R.id.button_F)) Do ("F");
                     else if (v == findViewById (R.id.button_R)) Do ("R");
                     else val = val + v.getTag();
                     TextView tw = (TextView) findViewById (R.id.text);
                     tw.setText (val);
           	  }
             });
           }    
         }
     }
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
         SetButtons ((ViewGroup) findViewById (R.id.buttonGroup));
     }
 }
