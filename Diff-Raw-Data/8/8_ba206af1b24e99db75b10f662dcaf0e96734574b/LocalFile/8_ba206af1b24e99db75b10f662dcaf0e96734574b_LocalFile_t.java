 /*
  *   Copyright 2012 Hai Bison
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package group.pals.android.lib.ui.filechooser.io;
 
 import group.pals.android.lib.ui.filechooser.utils.history.History;
 
 import java.io.File;
 import java.util.List;
 
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * This is a wrapper for {@link File}.
  * 
  * @author Hai Bison
  * @since v3.2
  */
 public class LocalFile extends File implements IFile {
 
     /**
      * 
      */
     private static final long serialVersionUID = 2068049445895497580L;
 
     public LocalFile(String pathname) {
         super(pathname);
     }// LocalFile()
 
     public LocalFile(File file) {
         this(file.getAbsolutePath());
     }// LocalFile()
 
     @Override
     public IFile parentFile() {
         return getParent() == null ? null : new LocalFile(getParent());
     }// parentFile()
 
     /**
      * By default, {@link File} compares to another one by its pathname. So if
      * two different {@link File}'s have same pathname, they are equal. But to
      * let the {@link History} works, we don't do that.<br>
      * For example we have this history: 1-2-3-2-4-5, and it is used in a
      * {@link List}, then if we use {@link File}, we will only get {@code 1} for
      * method {@link List#indexOf(Object)} with file "2". But we have 2
      * positions for "2".<br>
      * <br>
      * So, we override this method :-) The result is a comparison of operator
      * {@code ==}
      */
     @Override
     public boolean equals(Object obj) {
         return this == obj;
     }
 
     @Override
     public boolean equalsToPath(IFile file) {
         return file == null ? false : getAbsolutePath().equals(file.getAbsolutePath());
     }// equalsToPath()
 
     /*-----------------------------------------------------
      * Parcelable
      */
 
    static final String _Pathname = "pathname";

     @Override
     public int describeContents() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         Bundle b = new Bundle();
 
        b.putSerializable(_Pathname, getAbsolutePath());
 
         dest.writeBundle(b);
     }
 
     public static final Parcelable.Creator<LocalFile> CREATOR = new Parcelable.Creator<LocalFile>() {
 
         public LocalFile createFromParcel(Parcel in) {
             return new LocalFile(in);
         }
 
         public LocalFile[] newArray(int size) {
             return new LocalFile[size];
         }
     };
 
     private LocalFile(Parcel in) {
        this(in.readBundle().getString(_Pathname));
     }
 }
