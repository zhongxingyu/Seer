 package cstdr.ningningcat.widget;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.Gravity;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import cstdr.ningningcat.NncApp;
 
 /**
  * 自定义导航栏
  * @author cstdingran@gmail.com
  */
 public class DRNavigationBar extends TextView {
 
     public DRNavigationBar(Context context) {
         super(context);
         init();
     }
 
     private void init() {
         LayoutParams LP=new LayoutParams(LayoutParams.MATCH_PARENT, (int)(NncApp.getUI_SCALE_X() * 48));
         this.setLayoutParams(LP);
         this.setBackgroundColor(Color.DKGRAY);
         this.setTextSize((int)(NncApp.getUI_SCALE_X() * 14));
         this.setTextColor(Color.WHITE);
         this.setGravity(Gravity.CENTER_VERTICAL);
     }
 
 }
