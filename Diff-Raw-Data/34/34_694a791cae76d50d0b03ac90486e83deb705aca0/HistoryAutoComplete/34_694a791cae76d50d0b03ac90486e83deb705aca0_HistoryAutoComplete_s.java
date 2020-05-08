 package com.papagiannis.tuberun;
 
 import android.content.Context;
import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.widget.AutoCompleteTextView;
 
 public class HistoryAutoComplete extends AutoCompleteTextView {
 
     public HistoryAutoComplete(Context context) {
         super(context);
     }
 
     public HistoryAutoComplete(Context arg0, AttributeSet arg1) {
         super(arg0, arg1);
     }
 
     public HistoryAutoComplete(Context arg0, AttributeSet arg1, int arg2) {
         super(arg0, arg1, arg2);
     }
 
     @Override
     public boolean enoughToFilter() {
         return true;
     }
     
     public void manualFilter() {
         performFiltering("",0);
     }
 
 }
