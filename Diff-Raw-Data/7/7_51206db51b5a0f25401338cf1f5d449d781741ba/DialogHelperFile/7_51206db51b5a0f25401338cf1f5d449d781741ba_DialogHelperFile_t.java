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
 
 package com.google.code.geobeagle.activity.main.fieldnotes;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.xmlimport.GeoBeagleEnvironment;
 import com.google.inject.Inject;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class DialogHelperFile implements DialogHelper {
     private final Context mContext;
    private final GeoBeagleEnvironment mGeoBeagleEnvironment;
 
     @Inject
    public DialogHelperFile(Context context, GeoBeagleEnvironment geoBeagleEnvironment) {
         mContext = context;
        mGeoBeagleEnvironment = geoBeagleEnvironment;
     }
 
     @Override
     public void configureEditor(EditText fieldNote) {
     }
 
     @Override
     public void configureDialogText(Dialog dialog, TextView fieldnoteCaveat) {
         fieldnoteCaveat.setText(String.format(mContext.getString(R.string.field_note_file_caveat),
                 mGeoBeagleEnvironment.getFieldNotesFilename()));
         dialog.setTitle(R.string.log_cache_to_file);
     }
 
 }
