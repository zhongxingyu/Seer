 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.preferences;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.widget.TextView;
 
 /**
  * Represents parameters of selected font for widget items text.
  * 
  * Instances of this class cache the various parameters needed to set the currently selected item
  * font. This class is immutable.
  * 
  * @author Tal Dayan
  */
 public class WidgetItemFontVariation {
     private final Typeface mTypeFace;
     private final int mColor;
     private final int mTextSize;
     private final float mLineSpacingMultiplier;
 
     /**
      * Construct a new variation.
      * 
      * @param typeFace the typeface t use
      * @param color the text color for non completed items.
      * @param textSize the text size.
      * @param lineSpacingMultiplier The line spacing multiplier to use.
      */
     private WidgetItemFontVariation(Typeface typeFace, int color, int textSize,
             float lineSpacingMultiplier) {
         this.mTypeFace = typeFace;
         this.mColor = color;
         this.mTextSize = textSize;
         this.mLineSpacingMultiplier = lineSpacingMultiplier;
     }
 
     /**
      * Apply this font variation to given text view.
      * 
      * @param textView the item's text view.
      * @param isCompleted true if the item is completed.
      */
     public void apply(TextView textView) {
         textView.setTypeface(mTypeFace);
         textView.setTextColor(mColor);
         textView.setTextSize(mTextSize);
         textView.setLineSpacing(0.0f, mLineSpacingMultiplier);
     }
 
     public static final WidgetItemFontVariation newFromCurrentPreferences(Context context,
             SharedPreferences sharedPreferences) {
         final ItemFontType fontType = PreferencesTracker
                 .readWidgetFontTypeFontTypePreference(sharedPreferences);
         final int color = PreferencesTracker.readWidgetTextColorPreference(sharedPreferences);
 
         final int rawFontSize = PreferencesTracker
                 .readWidgetItemFontSizePreference(sharedPreferences);
         final int fontSize = (int) (rawFontSize * fontType.scale);
 
         return new WidgetItemFontVariation(fontType.getTypeface(context), color, fontSize,
                fontType.lineSpacingMultipler);
     }
 
     public int getTextSize() {
         return mTextSize;
     }
 }
