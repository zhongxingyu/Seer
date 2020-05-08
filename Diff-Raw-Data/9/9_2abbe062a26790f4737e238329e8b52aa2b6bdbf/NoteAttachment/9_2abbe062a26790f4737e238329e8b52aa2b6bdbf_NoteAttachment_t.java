 /* *************************************************************************************************
  *                                         eNotes                                                  *
  * *************************************************************************************************
  * File:        NoteAttachment.java                                                                *
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
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.content.Context;
 import android.util.Base64;
 
 /**
  * Represents an attachment to a note
  * @author Emanuele Alimonda
  * @author Giovanni Serra
  */
 public class NoteAttachment {
    /** File type constants */
    public static final int kFileTypeInvalid = 0;
    public static final int kFileTypePicture = 1;
    public static final int kFileTypeAudio   = 2;
    public static final int kFileTypeVideo   = 3;
    public static final int kFileTypeMax     = 4;
 
    /** File extensions */
    public static final String kFileExtensionPicture = ".jpg";
    public static final String kFileExtensionAudio   = ".amr";
    public static final String kFileExtensionVideo   = ".3gp";
    
    /** MIME Types */
    public static final String kFileMimePicture = "image/*";
    public static final String kFileMimeAudio   = "audio/*";
    public static final String kFileMimeVideo   = "video/*";
 
    /** JSON field names */
    public static final String kAttachmentFileName = "name";
    public static final String kAttachmentFileType = "type";
    public static final String kAttachmentFileData = "data";
 
    /** Other constants */
    public static final long kMaxAttachmentSize = 1000000; // 1MB
 
    /** Attachment's filename */
    private String _filename;
    /** Attachment's contents */
    private ByteBuffer _filedata;
    /** Attachment's file type (see constants) */
    private int _filetype;
    /** Current context */
    private Context _context;
 
    /**
     * Default constructor.  Creates an empty attachment.
     * @param context The current context
     */
    public NoteAttachment(Context context) {
       this._context = context;
       this.init();
    }
 
    /**
     * Constructor.  Creates an attachment from a local file.
     * @param context    The current context
     * @param filetype   The file type ID
     * @param file       A reference to the file
     */
    public NoteAttachment(Context context, int filetype, File file) {
       this(context);
       this.init(filetype, file);
    }
 
    /**
     * Constructor.  Creates an attachment from an input stream.
     * @param context    The current context
     * @param filetype   The file type ID
     * @param filename   The file's name
     * @param stream     The input stream
     */
    public NoteAttachment(Context context, int filetype, String filename, InputStream stream) {
       this(context);
       this.init(filetype, filename, stream);
    }
 
    /**
     * Constructor.  Creates an attachment from a JSON object.
     * @param context    The current context
     * @param contents   A JSON object representing the attachment
     */
    public NoteAttachment(Context context, JSONObject contents) {
       this(context);
       this.init(contents);
    }
 
    /**
     * Constructor.  Creates an attachment from a (base64-encoded) string.
     * @param context    The current context
     * @param filetype   The file type ID
     * @param filename   The file's name
     * @param filedata   The file contents, with base64 encoding
     */
    public NoteAttachment(Context context, int filetype, String filename, String filedata) {
       this(context);
       this.init(filetype, filename, filedata);
    }
 
    /**
     * Constructor.  Creates an attachment from a raw byte buffer.
     * @param context    The current context
     * @param filetype   The file type ID
     * @param filename   The file's name
     * @param filedata   The file raw contents
     */
    public NoteAttachment(Context context, int filetype, String filename, ByteBuffer filedata) {
       this(context);
       this.init(filetype, filename, filedata);
    }
 
    /**
     * Initialize an empty attachment.
     */
    private void init() {
       this.setFilename(null);
       this.setRawData(null);
       this.setFiletype(kFileTypeInvalid);
    }
 
    /**
     * Initialize an attachment from a local file.
     * @param filetype   The file type ID
     * @param file       A reference to the file
     */
    private void init(int filetype, File file) {
       if (file == null || !file.isFile()) {
          this.init();
          return;
       }
       try {
          FileInputStream importStream = new FileInputStream(file);
          this.init(filetype, file.getName(), importStream);
       } catch (FileNotFoundException e) {
          this.init();
          e.printStackTrace();
       }
    }
 
    /**
     * Initialize an attachment from an input stream.
     * @param filetype   The file type ID
     * @param filename   The file's name
     * @param stream     The input stream
     */
    private void init(int filetype, String filename, InputStream stream) {
       if (filetype <= kFileTypeInvalid || filetype >= kFileTypeMax) {
          this.init();
          return;
       }
       this.setFiletype(filetype);
       this.setFilename(filename);
 
       try {
          int bufferSize = 0x20000; // ~130k
          byte[] buffer = new byte[bufferSize];
          ByteArrayOutputStream importBufferStream = new ByteArrayOutputStream(bufferSize);
          int read;
          while (true) {
             read = stream.read(buffer);
             if (read == -1) {
                break;
             }
             importBufferStream.write(buffer, 0, read);
             // Don't exceed max filesize
             if (importBufferStream.size() > kMaxAttachmentSize) {
                this.init();
                return;
             }
          }
          this.setRawData(ByteBuffer.wrap(importBufferStream.toByteArray()));
       } catch (IOException e) {
          this.init();
          e.printStackTrace();
       }
    }
 
    /**
     * Initialize an attachment from a JSON object.
     * @param contents   A JSON object representing the attachment
     */
    private void init(JSONObject contents) {
       if (contents == null) {
          this.init();
          return;
       }
       try {
          if (contents.has(kAttachmentFileName)
                && contents.has(kAttachmentFileType)
                && contents.has(kAttachmentFileData)
          ) {
             this.init(contents.getInt(kAttachmentFileType),
                   contents.getString(kAttachmentFileName),
                   contents.getString(kAttachmentFileData));
          }
       } catch (JSONException e) {
          this.init();
          e.printStackTrace();
       }
    }
 
    /**
     * Initialize an attachment from a (base64-encoded) string.
     * @param filetype   The file type ID
     * @param filename   The file's name
     * @param filedata   The file contents, with base64 encoding
     */
    private void init(int filetype, String filename, String filedata) {
       this.setFiletype(filetype);
       this.setFilename(filename);
       this.setEncodedData(filedata);
    }
    /**
     * Initialize an attachment from a raw byte buffer.
     * @param filetype   The file type ID
     * @param filename   The file's name
     * @param filedata   The file raw contents
     */
    private void init(int filetype, String filename, ByteBuffer filedata) {
       this.setFiletype(filetype);
       this.setFilename(filename);
       this.setRawData(filedata);
    }
 
    // Accessors
    /**
     * Return the attachment's filename.
     * @return  The filename
     */
    public String getFilename() {
       return this._filename;
    }
    /**
     * Set the attachment's filename.
     * @param filename   The filename to set.  null for a default filename.
     *                   A filename extension is appended where appropriate.
     */
    public void setFilename(String filename) {
       if (filename == null || filename == "") {
          this._filename = this._context.getResources().getString(R.string.noname);
       } else {
          this._filename = filename;
       }
       switch (this.getFiletype()) {
       case kFileTypePicture:
          if (!this._filename.endsWith(kFileExtensionPicture)) {
             this._filename += kFileExtensionPicture;
          }
          break;
       case kFileTypeAudio:
          if (!this._filename.endsWith(kFileExtensionAudio)) {
             this._filename += kFileExtensionAudio;
          }
          break;
       case kFileTypeVideo:
          if (!this._filename.endsWith(kFileExtensionVideo)) {
             this._filename += kFileExtensionVideo;
          }
          break;
       default:
          break;
       }
    }
 
    /**
     * Return the attachment contents in raw form
     * @return  The raw attachment contents
     */
    public ByteBuffer getRawData() {
       return this._filedata;
    }
    /**
     * Set the attachment's contents
     * @param rawdata The attachment contents, in raw form
     */
    public void setRawData(ByteBuffer rawdata) {
       if (rawdata == null) {
          this._filedata = ByteBuffer.allocate(0);
       } else {
          this._filedata = rawdata;
       }
    }
    /** Return the attachment contents, base64-encoded.
     * @return  The attachment contents, as a base64-encoded string
     */
    public String getEncodedData() {
       return Base64.encodeToString(this._filedata.array(), Base64.DEFAULT|Base64.NO_WRAP|Base64.URL_SAFE);
    }
    /**
     * Set the attachment's contents.
     * @param encodeddata   The attachment contents, as a base64-encoded string
     */
    public void setEncodedData(String encodeddata) {
       if (encodeddata == null) {
          this._filedata = ByteBuffer.allocate(0);
       } else {
         try {
            byte[] buffer = Base64.decode(encodeddata, Base64.DEFAULT|Base64.NO_WRAP|Base64.URL_SAFE);
            this._filedata = ByteBuffer.wrap(buffer);
         } catch (IllegalArgumentException e) {
            this._filedata = ByteBuffer.allocate(0);
            e.printStackTrace();
         }
       }
    }
 
    /**
     * Return the attachment's file type ID.
     * @return  The attachment's file type ID
     */
    public int getFiletype() {
       return this._filetype;
    }
    /**
     * Set the attachment's file type ID.
     * @param filetype   The file type ID
     */
    public void setFiletype(int filetype) {
       if (filetype >= kFileTypeMax || filetype <= kFileTypeInvalid) {
          this._filetype = kFileTypeInvalid;
          return;
       }
       this._filetype = filetype;
    }
 
    /**
     * Export the attachment as a JSON object.
     * @return  A json object containing the attachment info and data
     */
    public JSONObject getJson() {
       if (this._filetype <= kFileTypeInvalid || this._filetype >= kFileTypeMax) {
          return null;
       }
       JSONObject jsObject;
       try {
          jsObject = new JSONObject();
          jsObject.put(kAttachmentFileName, this.getFilename());
          jsObject.put(kAttachmentFileType, this.getFiletype());
          jsObject.put(kAttachmentFileData, this.getEncodedData());
       } catch (JSONException e) {
          return null;
       }
       return jsObject;
    }
 }
 /* vim: set ts=3 sw=3 smarttab expandtab cc=101 : */
