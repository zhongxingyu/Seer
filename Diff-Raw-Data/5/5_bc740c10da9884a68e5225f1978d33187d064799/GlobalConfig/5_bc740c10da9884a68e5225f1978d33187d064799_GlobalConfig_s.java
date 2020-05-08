 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.conf;
 
 import org.logicprobe.LogicMail.PlatformInfo;
 import org.logicprobe.LogicMail.util.Serializable;
 import org.logicprobe.LogicMail.util.SerializableHashtable;
 import org.logicprobe.LogicMail.util.UniqueIdGenerator;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 /**
  * Store the global configuration for LogicMail.
  */
 public class GlobalConfig implements Serializable {
     private long uniqueId;
 
     /** WiFi support is disabled, best for non-WiFi devices */
     final public static int WIFI_DISABLED = 0;
 
     /** Prompt for WiFi use whenever establishing a connection */
     final public static int WIFI_PROMPT = 1;
 
     /** Always use WiFi */
     final public static int WIFI_ALWAYS = 2;
 
     /** Prefer plain text display for messages */
     final public static int MESSAGE_DISPLAY_PLAIN_TEXT = 0;
 
     /** Prefer HTML display for messages */
     final public static int MESSAGE_DISPLAY_HTML = 1;
 
     /** language code to use for the UI, or null for system default */
     private String languageCode;
 
     /** true to enable Unicode normalization */
     private boolean unicodeNormalization;
 
     /** Preferred message display format */
     private int messageDisplayFormat;
 
     /** Number of message headers to retrieve */
     private int retMsgCount;
 
     /** True for ascending, false for descending */
     private boolean dispOrder;
 
     /** Root URL for local file storage */
     private String localDataLocation = "";
 
     /** Mode for WiFi support */
     private int wifiMode;
 
     /** Connection debugging */
     private boolean connDebug;
 
     /** Hide deleted messages */
     private boolean hideDeletedMsg;
 
     /** Local host name override */
     private String localHostname;
 
     public static String FILE_URL_PREFIX = "file:///";
     
     /**
      * Instantiates a new global configuration.
      */
     public GlobalConfig() {
         setDefaults();
     }
 
     /**
      * Instantiates a new global configuration.
      * 
      * @param input The input data to deserialize the contents from
      */
     public GlobalConfig(DataInput input) {
         try {
             deserialize(input);
         } catch (IOException ex) {
             setDefaults();
         }
     }
 
     /**
      * Sets the defaults.
      */
     private void setDefaults() {
         uniqueId = UniqueIdGenerator.getInstance().getUniqueId();
         this.languageCode = "";
         this.unicodeNormalization = false;
         this.messageDisplayFormat = GlobalConfig.MESSAGE_DISPLAY_PLAIN_TEXT;
         this.retMsgCount = 30;
         this.dispOrder = false;
         this.wifiMode = GlobalConfig.WIFI_DISABLED;
         this.hideDeletedMsg = true;
         this.localHostname = "";
        this.localDataLocation = null;
     }
 
     /**
      * Sets the language code.
      * 
      * @param languageCode the new language code, or an empty string for the system default.
      */
     public void setLanguageCode(String languageCode) {
         this.languageCode = languageCode;
     }
 
     /**
      * Gets the language code.
      * 
      * @return the language code, or an empty string for the system default.
      */
     public String getLanguageCode() {
         return languageCode;
     }
 
     /**
      * Sets whether unicode normalization is enabled.
      * 
      * @param unicodeNormalization True if unicode normalization is enabled
      */
     public void setUnicodeNormalization(boolean unicodeNormalization) {
         this.unicodeNormalization = unicodeNormalization;
     }
 
     /**
      * Gets whether unicode normalization is enabled.
      * 
      * @return True if unicode normalization is enabled
      */
     public boolean getUnicodeNormalization() {
         return unicodeNormalization;
     }
 
     /**
      * Sets the preferred message display format.
      * 
      * @param messageDisplayFormat the new preferred message display format
      */
     public void setMessageDisplayFormat(int messageDisplayFormat) {
         this.messageDisplayFormat = messageDisplayFormat;
     }
 
     /**
      * Gets the preferred message display format.
      * 
      * @return the preferred message display format
      */
     public int getMessageDisplayFormat() {
         return messageDisplayFormat;
     }
 
     /**
      * Set the number of message headers to retrieve.
      * 
      * @param retMsgCount The number of message headers to retrieve
      */
     public void setRetMsgCount(int retMsgCount) {
         this.retMsgCount = retMsgCount;
     }
 
     /**
      * Get the number of message headers to retrieve.
      * 
      * @return The number of message headers to retrieve
      */
     public int getRetMsgCount() {
         return retMsgCount;
     }
 
     /**
      * Set the message display order.
      * 
      * @param dispOrder True for ascending, false for descending
      */
     public void setDispOrder(boolean dispOrder) {
         this.dispOrder = dispOrder;
     }
 
     /**
      * Get the message display order.
      * 
      * @return True for ascending, false for descending
      */
     public boolean getDispOrder() {
         return dispOrder;
     }
 
     /**
      * Get the root URL for local file storage.
      * 
      * @param localDataLocation The local data URL
      */
     public void setLocalDataLocation(String localDataLocation) {
         this.localDataLocation = validateLocalDataLocation(localDataLocation);
     }
 
     /**
      * Set the root URL for local file storage.
      * 
      * @return The local data URL
      */
     public String getLocalDataLocation() {
        if(localDataLocation == null) {
             String[] fsRoots = PlatformInfo.getInstance().getFilesystemRoots();
             if(fsRoots.length > 0) {
                 setLocalDataLocation(fsRoots[0]);
             }
         }
         return localDataLocation;
     }
 
     /**
      * Gets the WiFi connection mode.
      * 
      * @return The WiFi connection mode
      */
     public int getWifiMode() {
         return wifiMode;
     }
 
     /**
      * Sets the WiFi connection mode.
      * 
      * @param wifiMode The WiFi connection mode
      */
     public void setWifiMode(int wifiMode) {
         this.wifiMode = wifiMode;
     }
 
     /**
      * Gets the connection debugging mode.
      * 
      * @return True to enable, false to disable
      */
     public boolean getConnDebug() {
         return connDebug;
     }
 
     /**
      * Sets the connection debugging mode.
      * 
      * @param connDebug True if enabled, false if disabled
      */
     public void setConnDebug(boolean connDebug) {
         this.connDebug = connDebug;
     }
 
     /**
      * Gets whether deleted messages should be hidden.
      * 
      * @return True if hidden, false if shown
      */
     public boolean getHideDeletedMsg() {
         return hideDeletedMsg;
     }
 
     /**
      * Sets whether deleted messages should be hidden.
      * 
      * @param hideDeletedMsg True to hide, false to show
      */
     public void setHideDeletedMsg(boolean hideDeletedMsg) {
         this.hideDeletedMsg = hideDeletedMsg;
     }
 
     /**
      * Gets the local hostname override.
      * 
      * @return The local hostname
      */
     public String getLocalHostname() {
         return this.localHostname;
     }
 
     /**
      * Sets the local hostname override.
      * 
      * @param localHostname The local hostname
      */
     public void setLocalHostname(String localHostname) {
         this.localHostname = localHostname;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.util.Serializable#serialize(java.io.DataOutput)
      */
     public void serialize(DataOutput output) throws IOException {
         output.writeLong(uniqueId);
 
         SerializableHashtable table = new SerializableHashtable();
 
         table.put("global_languageCode", languageCode);
         table.put("global_unicodeNormalization", new Boolean(unicodeNormalization));
         table.put("global_messageDisplayFormat", new Integer(messageDisplayFormat));
         table.put("global_retMsgCount", new Integer(retMsgCount));
         table.put("global_dispOrder", new Boolean(dispOrder));
         table.put("global_localDataLocation", localDataLocation);
         table.put("global_wifiMode", new Integer(wifiMode));
         table.put("global_connDebug", new Boolean(connDebug));
         table.put("global_hideDeletedMsg", new Boolean(hideDeletedMsg));
         table.put("global_localHostname", localHostname);
 
         table.serialize(output);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.util.Serializable#deserialize(java.io.DataInput)
      */
     public void deserialize(DataInput input) throws IOException {
         setDefaults();
         uniqueId = input.readLong();
 
         SerializableHashtable table = new SerializableHashtable();
         table.deserialize(input);
 
         Object value;
 
         value = table.get("global_languageCode");
         if(value != null && value instanceof String) {
             languageCode = (String)value;
         }
 
         value = table.get("global_unicodeNormalization");
         if(value != null && value instanceof Boolean) {
             unicodeNormalization = ((Boolean)value).booleanValue();
         }
 
         value = table.get("global_messageDisplayFormat");
         if ((value != null) && value instanceof Integer) {
             messageDisplayFormat = ((Integer) value).intValue();
         }
 
         value = table.get("global_retMsgCount");
         if ((value != null) && value instanceof Integer) {
             retMsgCount = ((Integer) value).intValue();
         }
 
         value = table.get("global_dispOrder");
         if ((value != null) && value instanceof Boolean) {
             dispOrder = ((Boolean) value).booleanValue();
         }
 
         value = table.get("global_localDataLocation");
         if ((value != null) && value instanceof String) {
             setLocalDataLocation((String)value);
         }
         else {
             String[] fsRoots = PlatformInfo.getInstance().getFilesystemRoots();
             if(fsRoots.length > 0) {
                 setLocalDataLocation(fsRoots[0]);
             }
         }
 
         value = table.get("global_wifiMode");
         if ((value != null) && value instanceof Integer) {
             wifiMode = ((Integer) value).intValue();
             if ((wifiMode < 0) || (wifiMode > 2)) {
                 wifiMode = GlobalConfig.WIFI_DISABLED;
             }
         }
 
         value = table.get("global_connDebug");
         if ((value != null) && value instanceof Boolean) {
             connDebug = ((Boolean) value).booleanValue();
         }
 
         value = table.get("global_hideDeletedMsg");
         if ((value != null) && value instanceof Boolean) {
             hideDeletedMsg = ((Boolean) value).booleanValue();
         }
 
         value = table.get("global_localHostname");
         if ((value != null) && value instanceof String) {
             localHostname = (String) value;
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.util.Serializable#getUniqueId()
      */
     public long getUniqueId() {
         return uniqueId;
     }
     
     /**
      * Checks provided filesystem root to make sure it exists,
      * creating any intermediate directories as necessary,
      * and returns a fully qualified file URL.
      * 
      * @param fsRoot filesystem root to validate
      * @return fully qualified and valid file URL,
      * or null if one could not be created
      */
     private static String validateLocalDataLocation(String fsRoot) {
         String url;
         if(fsRoot != null) {
             // Clean up the string, removing everything but the base
             int p = fsRoot.indexOf('/', FILE_URL_PREFIX.length() - 1);
             int q = fsRoot.indexOf('/', p + 1);
             if(p != -1 && q != -1 && p < q) {
                 fsRoot = fsRoot.substring(p + 1, q + 1);
             }
             
             // Add the prefix
             url = FILE_URL_PREFIX + fsRoot;
             
             // Append the necessary elements, creating directories as necessary
             if(url.indexOf("Card/") != -1) {
                 try {
                     FileConnection conn = (FileConnection)Connector.open(url + "BlackBerry/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                     
                     conn = (FileConnection)Connector.open(url + "logicmail/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                 } catch (IOException e) {
                     url = null;
                 }
             }
             else if(url.indexOf("store/") != -1) {
                 try {
                     FileConnection conn = (FileConnection)Connector.open(url + "home/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                     
                     conn = (FileConnection)Connector.open(url + "user/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                     
                     conn = (FileConnection)Connector.open(url + "logicmail/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                 } catch (IOException e) {
                     url = null;
                 }
             }
             else {
                 try {
                     FileConnection conn = (FileConnection)Connector.open(url + "logicmail/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                 } catch (IOException e) {
                     url = null;
                 }
             }
         }
         else {
             url = null;
         }
         return url;
     }
 }
