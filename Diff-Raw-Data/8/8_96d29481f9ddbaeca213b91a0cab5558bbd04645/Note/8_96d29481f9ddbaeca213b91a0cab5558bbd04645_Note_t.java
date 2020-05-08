 /* *************************************************************************************************
  *                                         eNotes                                                  *
  * *************************************************************************************************
  * File:        Note.java                                                                          *
  * Copyright:   (c) 2011-2012 Emanuele Alimonda, Giovanni Serra                                    *
  *              eNotes is free software: you can redistribute it and/or modify it under the terms  *
  *              of the GNU General Public License as published by the Free Software Foundation,    *
  *              either version 3 of the License, or (at your option) any later version.  eNotes is *
  *              distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without  *
  *              even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  *
  *              See the GNU General Public License for more details.  You should have received a   *
  *              copy of the GNU General Public License along with eNotes.                          *
  *              If not, see <http://www.gnu.org/licenses/>                                         *
  * *************************************************************************************************/
 
 package it.unica.enotes;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.UUID;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.content.Context;
 import android.net.Uri;
 import android.text.format.Time;
 import android.webkit.URLUtil;
 
 /**
  * Represents each note entry and offers conversion methods from and to the JSON format.
  * @author Emanuele Alimonda
  * @author Giovanni Serra
  */
 public class Note {
    /** Static references to fields (used in Bundles, JSON, Database, etc.) */
    public static final String kID         = "_id";
    public static final String kGUID       = "guid";
    public static final String kTitle      = "title";
    public static final String kTimestamp  = "modified_date";
    public static final String kURL        = "url";
    public static final String kTags       = "tags";
    public static final String kText       = "text";
    public static final String kAttachment = "attachment";
    public static final String kContent    = "content";
 
    /** Content provider authority */
    public static final String kAuthority  = "it.unica.enotes.notes";
 
    /** Content types used by the notes */
    public static final String kContentType      = "vnd.android.cursor.dir/vnd.enotes.note";
    public static final String kContentItemType  = "vnd.android.cursor.item/vnd.enotes.note";
 
    /** Note base URI */
    public static final Uri kContentURI = Uri.parse("content://" + kAuthority + "/notes");
 
    // Members
    /** Have the note details been loaded? */
    private boolean _loaded;
    /** Have the note details been changed since the last save? */
    private boolean _dirty;
    /** Global Unique IDentifier of the note */
    private String _GUID;
    /** Note title */
    private String _title;
    /** Last modification timestamp */
    private Time _timestamp;
    /** Note contents (plain text) */
    private String _text;
    /** Note attachment */
    private NoteAttachment _attachment;
    /** Note URL */
    private String _URL;
    /** Note tags */
    private ArrayList<String> _tags;
    /** Context (for strings, etc) */
    private Context _context;
 
    /** Default constructor.  Creates an empty note with an auto-generated GUID.
     * @param context The current context
     */
    public Note(Context context) {
       this._context = context;
       this.init();
    }
    /**
     * Constructor.  Creates an empty note with a given title and GUID.
     * @param context The current context
     * @param GUID    The GUID of the note
     * @param title   The title of the note
     */
    public Note(Context context, String GUID, String title) {
       this._context = context;
       this.init(GUID, title);
    }
    /**
     * Constructor.
     * @param context    The current context
     * @param GUID       The GUID of the note
     * @param title      The title of the note
     * @param timestamp  Last modification timestamp
     * @param text       Content (text) of the note
     * @param URL        Attached URL to the note
     * @param attachment Attached file to the note
     * @param tags       Tags of the note, as a space-delimited string
     */
    public Note(
          Context context,
          String GUID,
          String title,
          Time timestamp,
          String text,
          String URL,
          NoteAttachment attachment,
          String tags
          ) {
       this._context = context;
       this.init(GUID, title, timestamp, text, URL, attachment, tags);
    }
    /**
     * Constructor.
     * @param context    The current context
     * @param GUID       The GUID of the note
     * @param title      The title of the note
     * @param timestamp  Last modification timestamp
     * @param json       A JSON object containing text, URL and/or attachment, in string form
     * @param tags       Tags of the note, as a space-delimited string
     */
    public Note(Context context, String GUID, String title, Time timestamp, String json, String tags) {
       this._context = context;
       this.init(GUID, title, timestamp, json, tags);
    }
 
    /** Initialize an empty note with an auto-generated GUID */
    private void init() {
       this.init(null, null, null, null, null, null, null);
    }
    /**
     * Initialize an empty note with a given title and GUID
     * @param GUID    The GUID of the note
     * @param title   The title of the note
     */
    private void init(String GUID, String title) {
       this.init(GUID, title, null, null, null, null, null);
    }
    /**
     * Initialize a note with the given values.
     * @param GUID       The GUID of the note
     * @param title      The title of the note
     * @param timestamp  Last modification timestamp
     * @param text       Content (text) of the note
     * @param URL        Attached URL to the note
     * @param attachment Attached file to the note
     * @param tags       Tags of the note, as a space-delimited string
     */
    private void init(
          String GUID,
          String title,
          Time timestamp,
          String text,
          String URL,
          NoteAttachment attachment,
          String tags
          ) {
       this.setGUID(GUID);
       this.setTitle(title);
       this.setTimestamp(timestamp);
      this.setTagsFromString(tags);
 
       if (text == null) {
          // If text is null, the note wasn't loaded
          this.setText(null);
          this.setURL(null);
          this.setAttachment(null);
          this.setLoaded(false);
       } else {
          this.setText(text);
          this.setURL(URL);
          this.setAttachment(attachment);
          this.setLoaded(true);
       }
 
       this.setDirty(false);
    }
    /**
     * Initialize a note with the given values and JSON data
     * @param GUID       The GUID of the note
     * @param title      The title of the note
     * @param timestamp  Last modification timestamp
     * @param json       A JSON object containing text, URL and/or attachment, in string form
     * @param tags       Tags of the note, as a space-delimited string
     */
    private void init(String GUID, String title, Time timestamp, String json, String tags) {
       this.init(GUID, title, timestamp, null, null, null, tags);
       this.setFromJSON(json);
       this.setDirty(false);
    }
 
    /**
     * Amend a note from JSON data.
     * @param json    A JSON object representing the note, in string form
     */
    public void setFromJSON(String json) {
       JSONObject jsObject;
       if (json == null) {
          return;
       }
       try {
          jsObject = new JSONObject(json);
 
          if (jsObject.has(kText)) {
             this.setText(jsObject.getString(kText));
          }
 
          if (jsObject.has(kURL)) {
             this.setURL(jsObject.getString(kURL));
          }
 
          if (jsObject.has(kAttachment)) {
              this.setAttachment(new NoteAttachment(this._context,
                       jsObject.getJSONObject(kAttachment)));
           }
       } catch (JSONException e) {
          e.printStackTrace();
       }
       return;
    }
 
    /**
     * Export note to JSON data.
     * @return  A JSON object representing the note
     * */
    public String getJSON() {
       JSONObject jsObject;
       try {
          jsObject = new JSONObject();
          jsObject.put(kText, this._text);
          jsObject.put(kURL, this._URL);
          jsObject.put(kAttachment, this._attachment.getJson());
       } catch (JSONException e) {
          e.printStackTrace();
          return "";
       }
       return jsObject.toString();
    }
 
    // Accessors
    /**
     * Get the note's GUID.
     * @return  The note's GUID
     */
    public String getGUID() {
       return this._GUID;
    }
    /**
     * Set the note's GUID.
     * @param GUID    A new GUID to set or null for an auto-generated one
     */
    public void setGUID(String GUID) {
       if (GUID == null) {
          this._GUID = UUID.randomUUID().toString();
       } else {
          this._GUID = GUID;
       }
       this.setDirty(true);
    }
 
    /**
     * Return the note details loaded state.
     * @return  true if details have been loaded, false otherwise
     */
    public boolean isLoaded() {
       return this._loaded;
    }
    /**
     * Set the note details' loaded state.
     * @param state   State to set
     */
    public void setLoaded(boolean state) {
       this._loaded = state;
    }
 
    /**
     * Return the note's dirty state.
     * @return  true is the note was edited after last save, false otherwise
     */
    public boolean isDirty() {
       return this._dirty;
    }
    /**
     * Set the dirty state for a note.
     * @param state   Whether the note was edited after last save
     */
    public void setDirty(boolean state) {
       this._dirty = state;
    }
 
    /**
     * Get the note's title.
     * @return  The note's title.
     */
    public String getTitle() {
       return this._title;
    }
    /**
     * Set the note's title.
     * @param title   A new title to set or null to set a default one.
     */
    public void setTitle(String title) {
       if (title == null) {
          this._title = this._context.getResources().getString(R.string.untitled);
       } else {
          this._title = title;
       }
       this.setDirty(true);
    }
 
    /**
     * Get the note's last modification timestamp.
     * @return  The last modification timestamp.
     */
    public Time getTimestamp() {
       return this._timestamp;
    }
    /**
     * Set the note's last modification timestamp.
     * @param timestamp  The timestamp to set.  It'll be set to now if it's null.
     */
    public void setTimestamp(Time timestamp) {
       if (timestamp == null) {
          this._timestamp = new Time();
          this._timestamp.setToNow();
       } else {
          this._timestamp = timestamp;
       }
       this.setDirty(true);
    }
 
    /**
     * Get the note's contents.
     * @return  The note's contents (text)
     */
    public String getText() {
       return this._text;
    }
    /**
     * Set the note's contents.
     * @param text    New text contents for the note
     */
    public void setText(String text) {
       if (text == null) {
          this._text = "";
       } else {
          this._text = text;
       }
       this.setDirty(true);
    }
 
    /**
     * Get the note's attached URL.
     * @return  The note's attached URL
     */
    public String getURL() {
       return this._URL;
    }
    /**
     * Set the note's attached URL.
     * @param URL     A new URL to attach (replacing the previous one)
     */
    public void setURL(String URL) {
       if (URL == null || !URLUtil.isValidUrl(URL)) {
          this._URL = "";
       } else {
          this._URL = URL;
       }
       this.setDirty(true);
    }
    /**
     * Get the note's attached file.
     * @return  The note's attached file
     */
    public NoteAttachment getAttachment() {
       return this._attachment;
    }
    /**
     * Set the note's attached file.
     * @param URL     A new file to attach (replacing the previous one)
     */
    public void setAttachment(NoteAttachment attachment) {
       if (attachment == null) {
          this._attachment = new NoteAttachment(this._context);
       } else {
          this._attachment = attachment;
       }
       this.setDirty(true);
    }
 
    /**
     * Get the note's tags.
     * @return  The note's tags as an ArrayList.  In no tags are set, return an empty array.
     */
    public ArrayList<String> getTags() {
       return this._tags;
    }
    /**
     * Set the note's tags.
     * @param tags    An ArrayList containing the tags to set
     */
    public void setTags(ArrayList<String> tags) {
       if (tags == null) {
          this._tags = new ArrayList<String>();
       } else {
          this._tags = tags;
       }
       this.setDirty(true);
    }
    /**
     * Set the note's tags from a string
     * @param tags A String containing the tags to set, separated by spaces
     */
    public void setTagsFromString(String tags) {
       if (tags == null) {
          this.setTags(null);
          return;
       }
       tags = tags.replaceAll("\\s+", " ");
       tags = tags.trim();
       this.setTags(new ArrayList<String>(Arrays.asList(tags.split(" "))));
    }
    /**
     * Get the note's tags as a string.
     * @return  The note's tags as a space-delimited String.
     */
    public String getTagsAsString() {
       if (this._tags == null || this._tags.size() <= 0) {
          return "";
       }
       String ret = " ";
       for (int i = 0; i < this._tags.size(); i++) {
          ret += this._tags.get(i) + " ";
       }
       return ret;
    }
 
    // Static methods
    /**
     * Get the shared directory for temporary files
    * @return  A File representing the temporary directory
     * @throws  FileNotFoundException   when the directory isn't found and can't be created
     */
    public static File getSharedTmpDir() throws FileNotFoundException {
       String systemTmpDirPath = System.getProperty("java.io.tmpdir");
       File tmpDir = new File(systemTmpDirPath, "eNotesTmp");
       // Ensure it's a directory
       if (tmpDir.exists() && !tmpDir.isDirectory()) {
          tmpDir.delete();
       }
       if (!tmpDir.exists() && !tmpDir.mkdirs()) {
          throw new FileNotFoundException();
       }      
       if (tmpDir.exists() && tmpDir.isDirectory()) {
          return tmpDir;
       }
       throw new FileNotFoundException();
    }
 }
 /* vim: set ts=3 sw=3 smarttab expandtab cc=101 : */
