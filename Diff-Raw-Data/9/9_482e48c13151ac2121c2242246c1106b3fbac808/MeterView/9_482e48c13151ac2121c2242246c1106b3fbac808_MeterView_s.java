 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.cachelist.view;
 
 import android.graphics.Color;
 import android.widget.TextView;
 
 public class MeterView {
     private final MeterFormatter mMeterFormatter;
     private final TextView mTextView;
 
     public MeterView(TextView textView, MeterFormatter meterFormatter) {
         mTextView = textView;
         mMeterFormatter = meterFormatter;
     }
 
     public void set(float accuracy, float azimuth) {
         mTextView.setText(mMeterFormatter.barsToMeterText(mMeterFormatter
                .accuracyToBarCount(accuracy), String.valueOf((int)azimuth) + "°"));
     }
 
     public void setLag(long lag) {
         mTextView.setTextColor(Color.argb(mMeterFormatter.lagToAlpha(lag), 147, 190, 38));
     }
 }
