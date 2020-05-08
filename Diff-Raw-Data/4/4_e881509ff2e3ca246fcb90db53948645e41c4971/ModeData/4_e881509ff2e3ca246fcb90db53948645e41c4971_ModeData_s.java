 package edu.brown.cs.systems.modes.lib.data;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 
 /**
  * Information on app modes
  * @author Marcelo Martins <martins@cs.brown.edu>
  *
  */
 public class ModeData implements Parcelable {
 
     static private final String TAG = "ModeData";
 
     private long id;
     private String name;
     private long uid;
     private String description;
 
     public ModeData(long id, String name, long uid, String description) {
         this.id = id;
         this.name = name;
         this.uid = uid;
         this.description = description;
     }
 
     public ModeData(Parcel source) {
         /*
          * Reconstruct from Parcel
          */
         Log.v(TAG, "AppModeMessage(Parcel source)");
        id = source.readInt();
         name = source.readString();
         description = source.readString();
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         assert id > 0;
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public long getUid() {
         return uid;
     }
 
     public void setUid(long uid) {
         assert uid > 0;
         this.uid = uid;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public int describeContents() {
         return 0;
     }
 
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeLong(id);
         dest.writeString(name);
         dest.writeLong(uid);
         dest.writeString(description);
     }
 
     /**
      * Require for unmarshalling data stored in Parcel
      * 
      * @author Marcelo Martins <martins@cs.brown.edu>
      * 
      */
     public static final Parcelable.Creator<ModeData> CREATOR = new Parcelable.Creator<ModeData>() {
         public ModeData createFromParcel(Parcel source) {
             return new ModeData(source);
         }
 
         public ModeData[] newArray(int size) {
             return new ModeData[size];
         }
     };
 }
