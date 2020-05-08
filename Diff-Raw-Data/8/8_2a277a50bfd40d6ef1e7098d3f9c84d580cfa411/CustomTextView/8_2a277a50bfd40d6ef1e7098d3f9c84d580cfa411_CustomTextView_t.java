 package com.fun.midworx.com.fun.midworx.views;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.widget.TextView;
 
 import com.fun.midworx.R;
 
 /**
  * Created by Rotem on 28/07/13.
  */
 public class CustomTextView extends TextView {
     private final static int AGENCY = 0;
     private static Typeface agency;
 
 
     public CustomTextView(Context context) {
         super(context);
     }
 
     public CustomTextView(Context context, AttributeSet attrs) {
         super(context, attrs);
         parseAttributes(context, attrs);
     }
 
     public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         parseAttributes(context, attrs);
     }
 
     private void parseAttributes(Context context, AttributeSet attrs) {
         if (agency == null) {
            if (isInEditMode()) {
                agency = Typeface.DEFAULT;
            } else {
                agency = Typeface.createFromAsset(context.getAssets(), "AGENCYR.TTF");
            }
         }
 
         TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
 
         //The value 0 is a default, but shouldn't ever be used since the attr is an enum
         int typeface = values.getInt(R.styleable.CustomTextView_typeface, 0);
 
         switch(typeface) {
             case AGENCY: default:
                 //You can instantiate your typeface anywhere, I would suggest as a
                 //singleton somewhere to avoid unnecessary copies
                 setTypeface(agency);
                 break;
         }
     }
 }
