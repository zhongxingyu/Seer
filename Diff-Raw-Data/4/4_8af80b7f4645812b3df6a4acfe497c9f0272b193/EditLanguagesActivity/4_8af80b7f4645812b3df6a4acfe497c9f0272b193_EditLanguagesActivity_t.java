 package ro.undef.patois;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 import java.util.ArrayList;
 
 
 /**
  * Activity for editting the list of languages.
  */
 public class EditLanguagesActivity extends Activity {
     private final static String TAG = "EditLanguagesActivity";
 
     private LinearLayout mLayout;
     private LayoutInflater mInflater;
 
     private PatoisDatabase mDb;
     private ArrayList<LanguageEntry> mLanguages;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mInflater = getLayoutInflater();
         setContentView(R.layout.edit_languages);
        mLayout = (LinearLayout) findViewById(R.id.list);
 
         mDb = new PatoisDatabase(this);
         mDb.open();
 
         if (savedInstanceState != null) {
             loadLanguagesFromBundle(savedInstanceState);
         } else {
             loadLanguagesFromDatabase(mDb);
         }
         buildViews();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         for (LanguageEntry language : mLanguages) {
             language.syncFromView();
         }
 
         outState.putParcelableArrayList("languages", mLanguages);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_BACK: {
                 doSaveAction();
                 return true;
             }
         }
         return super.onKeyDown(keyCode, event);
     }
 
     private void loadLanguagesFromDatabase(PatoisDatabase db) {
         ArrayList<LanguageEntry> languages = new ArrayList<LanguageEntry>();
         Cursor cursor = db.getLanguages();
 
         while (cursor.moveToNext()) {
             languages.add(new LanguageEntry(cursor));
         }
 
         mLanguages = languages;
     }
 
     private void loadLanguagesFromBundle(Bundle savedInstanceState) {
         mLanguages = savedInstanceState.getParcelableArrayList("languages");
     }
 
     private void buildViews() {
         LayoutInflater inflater = mInflater;
         LinearLayout layout = mLayout;
         layout.removeAllViews();
 
         for (LanguageEntry language : mLanguages) {
             layout.addView(language.buildView(inflater, layout));
         }
     }
 
     private void doSaveAction() {
         for (LanguageEntry language : mLanguages) {
             language.saveToDatabase(mDb);
         }
         finish();
     }
 
     private static class LanguageEntry implements Parcelable {
         // These fields are saved in the parcel.
 
         /**
          * ID number of the the language row in the database.
          */
         public long id;
 
         /**
          * Short code for the language (e.g., "EN" for "English").
          */
         public String code;
 
         /**
          * Language name (e.g., "English").
          */
         public String name;
 
         /**
          * True if the data in the LanguageEntry is different from what's
          * stored in the database.
          */
         public boolean modified;
 
         /**
          * True if the user pressed the 'delete' button on this LanguageEntry.
          */
         public boolean deleted;
 
         /**
          * Position of the beginning of the selection in the "code" EditText
          * (-1 if there is no selection).
          */
         public int codeSelectionStart;
 
         /**
          * Position of the end of the selection in the "code" EditText
          * (-1 if there is no selection).
          */
         public int codeSelectionEnd;
 
         /**
          * Position of the beginning of the selection in the "name" EditText
          * (-1 if there is no selection).
          */
         public int nameSelectionStart;
 
         /**
          * Position of the end of the selection in the "name" EditText
          * (-1 if there is no selection).
          */
         public int nameSelectionEnd;
 
         // These fields are NOT saved in the parcel.
         private EditText mCodeEditText;
         private EditText mNameEditText;
 
         public LanguageEntry(long id, String code, String name) {
             this.id = id;
             this.code = code;
             this.name = name;
             this.modified = false;
             this.deleted = false;
             this.codeSelectionStart = -1;
             this.codeSelectionStart = -1;
             this.nameSelectionEnd = -1;
             this.nameSelectionEnd = -1;
         }
 
         public LanguageEntry() {
             this(-1, "", "");
         }
 
         public LanguageEntry(Cursor cursor) {
             this(cursor.getInt(PatoisDatabase.LANGUAGE_ID_COLUMN),
                  cursor.getString(PatoisDatabase.LANGUAGE_CODE_COLUMN),
                  cursor.getString(PatoisDatabase.LANGUAGE_NAME_COLUMN));
         }
 
         public View buildView(LayoutInflater inflater, LinearLayout parent) {
             View view = inflater.inflate(R.layout.edit_language_entry, parent, false);
 
             mCodeEditText = (EditText) view.findViewById(R.id.language_code);
             mNameEditText = (EditText) view.findViewById(R.id.language_name);
 
             mCodeEditText.setText(code);
             mNameEditText.setText(name);
 
             if (codeSelectionStart != -1 && codeSelectionEnd != -1) {
                 mCodeEditText.requestFocus();
                 mCodeEditText.setSelection(codeSelectionStart, codeSelectionEnd);
             }
             if (nameSelectionStart != -1 && nameSelectionEnd != -1) {
                 mNameEditText.requestFocus();
                 mNameEditText.setSelection(nameSelectionStart, nameSelectionEnd);
             }
 
             view.setTag(this);
 
             return view;
         }
 
         public void syncFromView() {
             String new_code = mCodeEditText.getText().toString();
             String new_name = mNameEditText.getText().toString();
 
             if (new_code != code || new_name != name)
                 modified = true;
 
             code = new_code;
             name = new_name;
 
             if (mCodeEditText.hasFocus()) {
                 codeSelectionStart = mCodeEditText.getSelectionStart();
                 codeSelectionEnd = mCodeEditText.getSelectionEnd();
             } else {
                 codeSelectionStart = -1;
                 codeSelectionEnd = -1;
             }
             if (mNameEditText.hasFocus()) {
                 nameSelectionStart = mNameEditText.getSelectionStart();
                 nameSelectionEnd = mNameEditText.getSelectionEnd();
             } else {
                 nameSelectionStart = -1;
                 nameSelectionEnd = -1;
             }
         }
 
         public void saveToDatabase(PatoisDatabase db) {
             syncFromView();
 
             if (id == -1 && !deleted) {
                 id = db.insertLanguage(code, name);
             } else if (modified && !deleted) {
                 db.updateLanguage(id, code, name);
             } else if (deleted) {
                 db.deleteLanguage(id);
             }
         }
 
         // The Parcelable interface implementation.
 
         public int describeContents() {
             return 0;
         }
 
         public void writeToParcel(Parcel out, int flags) {
             out.writeLong(id);
             out.writeString(code);
             out.writeString(name);
             out.writeInt(modified ? 1 : 0);
             out.writeInt(codeSelectionStart);
             out.writeInt(codeSelectionEnd);
             out.writeInt(nameSelectionStart);
             out.writeInt(nameSelectionEnd);
         }
 
         public static final Parcelable.Creator CREATOR
                 = new Parcelable.Creator() {
 
             public LanguageEntry createFromParcel(Parcel in) {
                 LanguageEntry language = new LanguageEntry(in.readLong(),
                                                            in.readString(),
                                                            in.readString());
                 language.modified = in.readInt() == 1;
 
                 language.codeSelectionStart = in.readInt();
                 language.codeSelectionEnd = in.readInt();
                 language.nameSelectionStart = in.readInt();
                 language.nameSelectionEnd = in.readInt();
 
                 return language;
             }
 
             public LanguageEntry[] newArray(int size) {
                 return new LanguageEntry[size];
             }
         };
     }
 }
