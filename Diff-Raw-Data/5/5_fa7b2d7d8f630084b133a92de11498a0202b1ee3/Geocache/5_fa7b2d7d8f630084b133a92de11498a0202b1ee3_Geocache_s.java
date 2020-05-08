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
 
 package com.google.code.geobeagle.data;
 
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * Geocache or letterbox description, id, and coordinates.
  */
 public class Geocache implements Parcelable {
     public static enum Provider {
         ATLAS_QUEST(0), GROUNDSPEAK(1), MY_LOCATION(-1);
 
         private final int mIx;
 
         Provider(int ix) {
             mIx = ix;
         }
 
         public int toInt() {
             return mIx;
         }
     }
 
     public static enum Source {
         GPX(0), MY_LOCATION(1), WEB_URL(2);
 
         public static class SourceFactory {
             private final Source mSources[] = new Source[values().length];
 
             public SourceFactory() {
                 for (Source source : values())
                     mSources[source.mIx] = source;
             }
 
             public Source fromInt(int i) {
                 return mSources[i];
             }
         }
 
         private final int mIx;
 
         Source(int ix) {
             mIx = ix;
         }
 
         public int toInt() {
             return mIx;
         }
     }
 
     public static Parcelable.Creator<Geocache> CREATOR = new GeocacheFactory.CreateGeocacheFromParcel();
     public static final String ID = "id";
     public static final String LATITUDE = "latitude";
     public static final String LONGITUDE = "longitude";
     public static final String NAME = "name";
     public static final String SOURCE_NAME = "sourceName";
     public static final String SOURCE_TYPE = "sourceType";
 
     private final CharSequence mId;
     private final double mLatitude;
     private final double mLongitude;
     private final CharSequence mName;
     private final String mSourceName;
     private final Source mSourceType;
 
     public Geocache(CharSequence id, CharSequence name, double latitude, double longitude,
             Source sourceType, String sourceName) {
         mId = id;
         mName = name;
         mLatitude = latitude;
         mLongitude = longitude;
         mSourceType = sourceType;
         mSourceName = sourceName;
     }
 
     public int describeContents() {
         return 0;
     }
 
     public Provider getContentProvider() {
         String prefix = mId.subSequence(0, 2).toString();
         if (prefix.equals("GC"))
             return Provider.GROUNDSPEAK;
         if (prefix.equals("LB"))
             return Provider.ATLAS_QUEST;
         else
             return Provider.MY_LOCATION;
     }
 
     public CharSequence getId() {
         return mId;
     }
 
     public CharSequence getIdAndName() {
         if (mId.length() == 0)
             return mName;
         else if (mName.length() == 0)
             return mId;
         else
             return mId + ": " + mName;
     }
 
     public double getLatitude() {
         return mLatitude;
     }
 
     public double getLongitude() {
         return mLongitude;
     }
 
     public CharSequence getName() {
         return mName;
     }
 
     public CharSequence getShortId() {
         if (mId.length() > 2)
             return mId.subSequence(2, mId.length());
         else
             return "";
     }
 
     public String getSourceName() {
         return mSourceName;
     }
 
     public Source getSourceType() {
         return mSourceType;
     }
 
     public void writeToParcel(Parcel out, int flags) {
         Bundle bundle = new Bundle();
         bundle.putCharSequence(ID, mId);
         bundle.putCharSequence(NAME, mName);
         bundle.putDouble(LATITUDE, mLatitude);
         bundle.putDouble(LONGITUDE, mLongitude);
         bundle.putInt(SOURCE_TYPE, mSourceType.mIx);
         bundle.putString(SOURCE_NAME, mSourceName);
         out.writeBundle(bundle);
     }
 
     public void writeToPrefs(Editor editor) {
         editor.putString(ID, mId.toString());
         editor.putString(NAME, mName.toString());
         editor.putFloat(LATITUDE, (float)mLatitude);
         editor.putFloat(LONGITUDE, (float)mLongitude);
         editor.putInt(SOURCE_TYPE, mSourceType.mIx);
         editor.putString(SOURCE_NAME, mSourceName);
     }
 
 }
