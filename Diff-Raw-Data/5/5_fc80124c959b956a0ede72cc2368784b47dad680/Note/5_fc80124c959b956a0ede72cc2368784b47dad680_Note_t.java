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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.UUID;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.net.Uri;
 import android.text.format.Time;
 
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
    // TODO
    /** Note URL */
    private String _URL;
    /** Note tags */
    private ArrayList<String> _tags;
 
    /** Default constructor.  Creates an empty note with an auto-generated GUID */
    public Note() {
       this(null, null, null, null, null, null);
    }
    /**
     * Constructor.  Creates an empty note with a given title and GUID
     * @param GUID    The GUID of the note
     * @param title   The title of the note
     */
    public Note(String GUID, String title) {
       this(GUID, title, null, null, null, null);
    }
    /**
     * Constructor
     * @param GUID       The GUID of the note
     * @param title      The title of the note
     * @param timestamp  Last modification timestamp
     * @param text       Content (text) of the note
     * @param URL        Attached URL to the note
     * @param tags       Tags of the note, as a space-delimited string
     */
    public Note(
          String GUID,
          String title,
          Time timestamp,
          String text,
          String URL,
          String tags
          ) {
       if (GUID == null) {
          this._GUID = UUID.randomUUID().toString();
       } else {
          this._GUID = GUID;
       }
 
       if (title != null) {
          this._title = title;
       } else {
          this._title = "";
       }
 
       if (timestamp != null) {
          this._timestamp = timestamp;
       } else {
          Time ts = new Time();
          ts.setToNow();
          this._timestamp = ts;
       }
 
       this._text = null;
       this._URL = null;
       this._tags = null;
       this._loaded = false;
       
       if (text != null) {
          this._text = text;
          if (URL != null) {
         	 this._URL = URL;
          }
          if (tags != null) {
         	 this.setTagsFromString(tags);
          }
          this._loaded = true;
       }
       
       if (this._text == null) {
          this._text = "";
       }
       if (this._URL == null) {
          this._URL = "";
       }
       if (this._tags == null) {
          this._tags = new ArrayList<String>();
       }
 
       this._dirty = false;
    }
 
    /**
     * Import note from JSON data
     * @param json    A JSON object representing the note
     */
    public void NoteFromJSON(String json) {
       JSONObject jsObject;
       if (json == null) {
     	  return;
       }
       try {
          jsObject = new JSONObject(json);
 
          if (jsObject.has(kText)) {
             this._text = jsObject.getString(kText);
         } else {
        	 this._text = "";
          }
 
          if (jsObject.has(kURL)) {
             this._URL = jsObject.getString(kURL);
         } else {
        	 this._URL = "";
          }
 
          // TODO: Attachments
       } catch (JSONException e) {
          return;
       }
       return;
    }
 
    /**
     * Export note to JSON data
     * @return  A JSON object representing the note
     * */
    public String getJSON() {
       JSONObject jsObject;
       try {
          jsObject = new JSONObject();
          jsObject.put(kText, this._text);
          jsObject.put(kURL, this._URL);
          // TODO: Attachments
       } catch (JSONException e) {
          return "";
       }
       return jsObject.toString();
    }
 
    // Accessors
    /**
     * Get the note's GUID
     * @return  The note's GUID
     */
    public String getGUID() {
       return this._GUID;
    }
    /**
     * Set the note's GUID
     * @param GUID    A new GUID to set
     */
    public void setGUID(String GUID) {
       if (GUID == null) {
     	  return;
       }
       this._GUID = GUID;
       this.setDirty(true);
    }
 
    /**
     * Return the note details loaded state
     * @return  true if details have been loaded, false otherwise
     */
    public boolean isLoaded() {
       return this._loaded;
    }
    /**
     * Set the note details' loaded state
     * @param state   State to set
     */
    public void setLoaded(boolean state) {
       this._loaded = state;
       this.setDirty(false);
    }
 
    /**
     * Return the note's dirty state
     * @return  true is the note was edited after last save, false otherwise
     */
    public boolean isDirty() {
       return this._dirty;
    }
    /**
     * Set the dirty state for a note
     * @param state   Whether the note was edited after last save
     */
    public void setDirty(boolean state) {
       this._dirty = state;
    }
 
    /**
     * Get the note's title
     * @return  The note's title
     */
    public String getTitle() {
       return this._title;
    }
    /**
     * Set the note's title
     * @param title   A new title to set
     */
    public void setTitle(String newTitle) {
       if (newTitle == null) {
     	  this._title = "";
       } else {
     	  this._title = newTitle;
       }
       this.setDirty(true);
    }
 
    /**
     * Get the note's last modification timestamp
     * @return  The last modification timestamp
     */
    public Time getTimestamp() {
       return this._timestamp;
    }
    /**
     * Set the note's last modification timestamp
     * @param timestamp  The timestamp to set.  It'll be set to now if it's null
     */
    public void setTimestamp(Time timestamp) {
       if (timestamp == null) {
          this._timestamp.setToNow();
       } else {
          this._timestamp = timestamp;
       }
    }
 
    /**
     * Get the note's contents
     * @return  The note's contents (text)
     */
    public String getText() {
       return this._text;
    }
    /**
     * Set the note's contents
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
     * Get the note's attached URL
     * @return  The note's attached URL
     */
    public String getURL() {
       return this._URL;
    }
    /**
     * Set the note's attached URL
     * @param URL     A new URL to attach (replacing the previous one)
     */
    public void setURL(String URL) {
       if (URL == null) {
     	  this._URL = "";
       } else {
     	  this._URL = URL;
       }
       this.setDirty(true);
    }
 
    /**
     * Get the note's tags
     * @return  The note's tags as an ArrayList.  In no tags are set, return an empty array.
     */
    public ArrayList<String> getTags() {
       return this._tags;
    }
    /**
     * Set the note's tags
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
     * @param tags	A String containing the tags to set, separated by spaces
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
     * Get the note's tags as a string
     * @return	The note's tags as a space-delimited String.
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
 }
 /* vim: set ts=3 sw=3 smarttab expandtab cc=101 : */
