 package com.gris.ege.pager;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.webkit.WebView;
 
 public class TaskWebView extends WebView
 {
     public TaskWebView(Context context)
     {
         super(context);
     }
 
     public TaskWebView(Context context, AttributeSet attrs)
     {
         super(context, attrs);
     }
 
     public boolean isScaled()
     {
        return computeHorizontalScrollRange()>computeHorizontalScrollExtent();
     }
 }
