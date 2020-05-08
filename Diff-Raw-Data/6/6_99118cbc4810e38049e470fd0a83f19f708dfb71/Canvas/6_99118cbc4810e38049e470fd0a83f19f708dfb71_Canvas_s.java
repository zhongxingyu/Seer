 /* *****************************************************************************
  * Canvas.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 /**
  * A <code>Canvas</code> represents the underlying
  * area in which a compiled app will run
  */
 
 package org.openlaszlo.compiler;
 
 
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.io.File;
 import org.jdom.Element;
 import org.jdom.output.Format.TextMode;
 import org.openlaszlo.server.Configuration;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.apache.log4j.Logger;
 
 public class Canvas implements java.io.Serializable {
 
     // TODO: [old ebloch] change these to properties
     // TODO: [2003-10-25 bloch] or better yet derrive them from the schema
     
     private static Logger mLogger  = Logger.getLogger(Canvas.class);
 
     /** Default canvas width for compilation */
     private static final int DEFAULT_WIDTH = 500;
 
     /** Default canvas height for compilation */
     private static final int DEFAULT_HEIGHT = 400;
 
     /** Default canvas backgorund color */
     private static final int DEFAULT_BGCOLOR = 0xFFFFFF;
 
     /** Default canvas backgorund color */
     private static final String DEFAULT_TITLE = "OpenLaszlo Application";
 
     /** Default canvas id */
     private static final String DEFAULT_ID = "lzapp";
 
     /** Default canvas accessibility */
     private static final boolean DEFAULT_ACCESSIBLE = false;
 
     /** Default canvas font info */
     // TODO: [2003-10-25 bloch] this should come from the build system
     public static final String DEFAULT_VERSION = "1.1";
 
     // TODO: [2003-10-25 bloch] these should come from a properties file
     public static final String DEFAULT_FONT          = "Verdana,Vera,sans-serif";
 
     public static final String DEFAULT_FONT_FILENAME      = "verity" + File.separator + "verity11.ttf";
     public static final String DEFAULT_BOLD_FONT_FILENAME = "verity" + File.separator + "verity11bold.ttf";
     public static final String DEFAULT_ITALIC_FONT_FILENAME = "verity" + File.separator + "verity11italic.ttf";
     public static final String DEFAULT_BOLD_ITALIC_FONT_FILENAME = "verity" + File.separator + "verity11bolditalic.ttf";
     
     public String defaultFont () {
         return DEFAULT_FONT;
     }
 
     public String defaultFontsize () {
         return DEFAULT_FONTSIZE;
     }
 
 
     public static final String DEFAULT_FONTSIZE  = "11";
     public static final String DEFAULT_FONTSTYLE = "plain";
     
     /** Default persistent connection parameters */
     private static final long DEFAULT_HEARTBEAT = 5000; // 5 seconds
     private static final boolean DEFAULT_SENDUSERDISCONNECT = false;
     
     public String defaultFont = DEFAULT_FONT;
     public String defaultFontFilename = DEFAULT_FONT_FILENAME;
     public String defaultBoldFontFilename = DEFAULT_BOLD_FONT_FILENAME;
     public String defaultItalicFontFilename = DEFAULT_ITALIC_FONT_FILENAME;
     public String defaultBoldItalicFontFilename = DEFAULT_BOLD_ITALIC_FONT_FILENAME;
     
     /** File path relative to webapp. */
     private String mFilePath = null;
     /** Width of the canvas. */
     private int mWidth = DEFAULT_WIDTH;
     /** Height of the canvas. */
     private int mHeight = DEFAULT_HEIGHT;
     /** Background color of the canvas. */
     private int mBGColor = DEFAULT_BGCOLOR;
 
     // Dimension strings
     private String mWidthString = "100%";
     private String mHeightString = "100%";
 
     // Default to proxied deployment
     private boolean mProxied = true;
     
     /** FontInfo for the canvas. */
     private FontInfo mFontInfo = null;
     
     /** Title for the canvas. */
     private String mTitle = DEFAULT_TITLE;
     
     /** Version of the flash player file format which we compile to **/
     private String mRuntime = LPS.getProperty("compiler.runtime.default", LPS.getRuntimeDefault() );
     
     /** computed debug flag, based on canvas 'debug' attribute + compilation request args **/
     private boolean mDebug = false;
 
     /** computed backtrace flag, based on canvas 'backtrace' attribute + compilation request args **/
     private boolean mBacktrace = false;
 
     /** computed profile flag from request args **/
     private boolean mProfile = false;
 
     /** ID for the canvas. */
     private String mID = DEFAULT_ID;
     
     /** accessibility for the canvas. */
     private boolean mAccessible = DEFAULT_ACCESSIBLE;
 
     /** Persistent connection parameters. */
     private boolean mIsConnected = false;
     private boolean mSendUserDisconnect = false;
     private long mHeartbeat = 0;
     private String mAuthenticator = null;
     private String mGroup = null;
     private Set mAgents = null;
 
     private final Map mSecurityOptions = new Hashtable();
 
     private String mCompilationWarningText = null;
     private String mCompilationWarningXML = null;
     private Element mInfo = new org.jdom.Element("stats");
     
     public void setCompilationWarningText(String text) {
         mCompilationWarningText = text;
     }
 
     public void setCompilationWarningXML(String xml) {
         mCompilationWarningXML = xml;
     }
     
     public void setRuntime(String text) {
         mRuntime = text;
     }
 
     public String getRuntime() {
         return mRuntime;
     }
 
     public void setDebug(boolean val) {
         mDebug = val;
     }
 
     public boolean getDebug() {
         return(mDebug);
     }
 
     public void setBacktrace(boolean val) {
         mBacktrace = val;
     }
 
     public boolean getBacktrace() {
         return(mBacktrace);
     }
 
     public void setProfile(boolean val) {
         mProfile = val;
     }
 
     public boolean getProfile() {
         return(mProfile);
     }
 
 
     public void addInfo(Element info) {
         mInfo.addContent(info);
     }
 
     public String getCompilationWarningText() {
         return mCompilationWarningText;
     }
 
     public void setFontInfo(FontInfo info) {
         mFontInfo = info;
     }
     
     public String getInfoAsString() {
         org.jdom.output.XMLOutputter outputter =
             new org.jdom.output.XMLOutputter();
         outputter.getFormat().setTextMode(TextMode.NORMALIZE);
         return outputter.outputString(mInfo);
     }
 
     /** @return file path */
     public String getFilePath() {
         return mFilePath;
     }
 
     /** @param filePath */
     public void setFilePath(String filePath) {
         mFilePath = filePath;
     }
 
     /** @return width */
     public int getWidth() {
         return mWidth;
     }
 
     /** @return width */
     public String getWidthXML() {
         if (mWidthString == null)
             return "" + mWidth;
         else 
             return mWidthString;
     }
 
     /** @param w */
     public void setWidth(int w) {
         mWidth = w;
         mWidthString = null;
     }
 
     /** @param w */
     public void setWidthString(String w) {
         mWidthString = w;
     }
 
 
     /** @return height */
     public int getHeight() {
         return mHeight;
     }
 
     /** @return width */
     public String getHeightXML() {
         if (mHeightString == null)
             return "" + mHeight;
         else 
             return mHeightString;
     }
 
     /** @param h */
     public void setHeight(int h) {
         mHeight = h;
         mHeightString = null;
     }
 
     /** @param h */
     public void setHeightString(String h) {
         mHeightString = h;
     }
 
     /** @return Background color */
     public int getBGColor() {
         return mBGColor;
     }
 
     /** @return Returns bgcolor as a hexadecimal string */
     // TODO: [12-21-2002 ows] This belongs in a utility library.
     public String getBGColorString() {
         String red   = Integer.toHexString((mBGColor >> 16) & 0xff);
         String green = Integer.toHexString((mBGColor >> 8) & 0xff);
         String blue  = Integer.toHexString(mBGColor & 0xff);
         if (red.length() == 1)
             red = "0" + red;
         if (green.length() == 1)
             green = "0" + green;
         if (blue.length() == 1)
             blue = "0" + blue;
         return "#" + red + green + blue;
     }
 
     /** @param BGColor  Background color */
     public void setBGColor(int BGColor) {
         mBGColor = BGColor;
     }
 
     /** @return Title */
     public String getTitle() {
         return mTitle;
 
     }
 
     /** @return ID */
     public String getID() {
         return mID;
 
     }
 
     /** @return accessible */
     public boolean getAccessible() {
         return mAccessible;
     }
     
     public String getISBN() {
         return "192975213X";
     }
     
     /** @param t */
     public void setTitle(String t) {
         mTitle = t;
     }
 
     /** @param id */
     public void setID(String id) {
         mID = id;
     }
 
     /** @param accessible */
     public void setAccessible(boolean accessible) {
         mAccessible = accessible;
     }
 
     /** @return Heartbeat */
     public long getHeartbeat() {
         return mHeartbeat;
 
     }
 
     /** @param heartbeat */
     public void setHeartbeat(long heartbeat) {
         mHeartbeat = heartbeat;
     }
 
     /** @return Group */
     public String getGroup() {
         return mGroup;
 
     }
 
     /** @param g */
     public void setGroup(String g) {
         mGroup = g;
     }
 
     /** @return Authenticator */
     public String getAuthenticator() {
         return mAuthenticator;
 
     }
 
     /** @param a */
     public void setAuthenticator(String a) {
         mAuthenticator = a;
     }
 
     /** @return send user disconnect */
     public boolean doSendUserDisconnect() {
         return mSendUserDisconnect;
     }
 
     /** @param sud */
     public void setSendUserDisconnect(boolean sud) {
         mSendUserDisconnect = sud;
     }
 
     /** @param val */
     public void setProxied(boolean val) {
         mProxied = val;
     }
 
     /** @return is this app compiling for serverless deployment */
     public boolean isProxied() {
         return mProxied;
     }
 
     /** @return is connected */
     public boolean isConnected() {
         return mIsConnected;
     }
 
     /** @param isConnected */
     public void setIsConnected(boolean isConnected) {
         mIsConnected = isConnected;
     }
 
     /** @return agents */
     public Set getAgents() {
         return mAgents;
     }
 
     /** @param agent agent's URL */
     public void addAgent(String agent) {
         if (mAgents == null) 
             mAgents = new HashSet();
         mAgents.add(agent);
     }
 
 
     /** @return font info */
     public FontInfo getFontInfo() {
         return mFontInfo;
     }
 
     /** @return security options */
     public Map getSecurityOptions() {
         return mSecurityOptions;
     }
 
     /** @param element */
     public void setSecurityOptions(Element element) {
         Configuration.addOption(mSecurityOptions, element);
     }
 
     public String getXML(String content) {
         StringBuffer buffer = new StringBuffer();
         buffer.append(
             "<canvas " +
             "title='" + XMLUtils.escapeXml(getTitle()) + "' " +
             "bgcolor='" + getBGColorString() + "' " +
             "width='" + getWidthXML() + "' " +
             "height='" + getHeightXML() + "' " +
             "proxied='" + isProxied() + "' " +
             "runtime='" + getRuntime() +"' " +
            "lfc='" + LPS.getProperty("compiler.runtime.dir") +
            "/" + LPS.getLFCname(getRuntime(), mDebug, mProfile, mBacktrace) + "' " +
             "debug='" + mDebug + "' " +
             "id='" + XMLUtils.escapeXml(getID()) +"' " +
             "accessible='" + XMLUtils.escapeXml(getAccessible() + "") +"' " +
             ">");
         buffer.append(content);
         buffer.append(getInfoAsString());
         if (mCompilationWarningXML != null)
             buffer.append("<warnings>" + mCompilationWarningXML + "</warnings>");
         buffer.append("</canvas>");
         return buffer.toString();
     }
     
     /** 
      * Initialize persistent connection values.
      */
     protected void initializeConnection(Element elt) {
         // TODO: [2003-10-16 pkang] Create and move this function into
         // ConnectionCompiler.java
         Element eltConnection = elt.getChild("connection", elt.getNamespace());
         if (eltConnection!=null) {
 
             setIsConnected(true);
             setSendUserDisconnect(DEFAULT_SENDUSERDISCONNECT);
             setHeartbeat(DEFAULT_HEARTBEAT);
 
             String heartbeat = eltConnection.getAttributeValue("heartbeat");
             if (heartbeat != null) {
                 try {
                     setHeartbeat(Long.parseLong(heartbeat));
                 } catch (NumberFormatException e) {
                     throw new CompilationError(elt,  "heartbeat", e);
                 }
             }
 
             String sendUserDisconnect =
                 eltConnection.getAttributeValue("receiveuserdisconnect");
             if (sendUserDisconnect != null) {
                 setSendUserDisconnect(Boolean.valueOf(sendUserDisconnect).booleanValue());
             }
 
             String group = eltConnection.getAttributeValue("group");
             if (group != null) {
                 setGroup(group);
             }
 
             // Don't set a default authenticator in canvas. We want to be able
             // to override this through lps.properties. Return null if one does
             // not exist.
             String authenticator = eltConnection.getAttributeValue("authenticator");
             if (authenticator != null) {
                 setAuthenticator(authenticator);
             }
 
             List agents = eltConnection.getChildren("agent", elt.getNamespace());
             for (int i=0; i < agents.size(); i++) {
                 Element eltAgent = (Element)agents.get(i);
                 String url = eltAgent.getAttributeValue("url");
                 if (url != null || ! url.equals(""))
                     addAgent(url);
             }
         }
     }
 }
