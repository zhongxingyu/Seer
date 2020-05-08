 package io.github.axxiss.places.model;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * Created by alexis on 9/14/13.
  */
 public class PeriodData implements Parcelable {
     private int day;
 
     private String time;
 
     /**
     * A number from 0–6, corresponding to the days of the week, starting on Sunday. For example, 2
      * means Tuesday.
      *
      * @return the day
      */
     public int getDay() {
         return day;
     }
 
     /**
     * May contain a time of day in 24-hour hhmm format (values are in the range 0000–2359). The
     * time will be reported in the Place’s timezone.
      *
      * @return the time
      */
     public String getTime() {
         return time;
     }
 
     protected PeriodData(Parcel in) {
         day = in.readInt();
         time = in.readString();
     }
 
     public int describeContents() {
         return 0;
     }
 
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeInt(day);
         dest.writeString(time);
     }
 
     public static final Parcelable.Creator<PeriodData> CREATOR = new Parcelable.Creator<PeriodData>() {
         public PeriodData createFromParcel(Parcel in) {
             return new PeriodData(in);
         }
 
         public PeriodData[] newArray(int size) {
             return new PeriodData[size];
         }
     };
 }
