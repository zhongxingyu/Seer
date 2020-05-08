 /*
  * $Id: AppInfoSaxHandler.java 1575 2009-12-07 07:19:21Z amandel $
  *
  * Copyright 2006, The jCoderZ.org Project. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials
  *      provided with the distribution.
  *    * Neither the name of the jCoderZ.org Project nor the names of
  *      its contributors may be used to endorse or promote products
  *      derived from this software without specific prior written
  *      permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jcoderz.commons.taskdefs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 class AppInfoSaxHandler
       extends DefaultHandler
 {
    /** Application Identifier of fawkeZ. */
    public static final int APPLICATION_ID_FWK = 1;
    /** Application Identifier of asf. */
    public static final int APPLICATION_ID_ASF = 100;
    /** Application Identifier of ppg. */
    public static final int APPLICATION_ID_PPG = 101;
    /** Application Identifier of taco. */
    public static final int APPLICATION_ID_TAC = 102;
    /** Application Identifier of application (amandel). */
    public static final int APPLICATION_ID_ACM = 120;
    /** Application Identifier of application (amandel). */
    public static final int APPLICATION_ID_ACR = 121;
    /** Application Identifier of application (amandel). */
    public static final int APPLICATION_ID_DMB = 122;
 
    private static final Pattern REGEX_SINGLE_QUOTES
          = Pattern.compile(".*[^']'[^'].*",
             Pattern.DOTALL + Pattern.MULTILINE);
 
    private static final Pattern REGEX_VARIABLES
          = Pattern.compile(".*\\{.+\\}.*",
             Pattern.DOTALL + Pattern.MULTILINE);
 
    private static final String EMPTY_STRING = "";
 
    private SAXParseException mSaxParseException = null;
    private boolean mValidationError = false;
 
    /** Maps application id (Integer) to app short-name (String). */
    private final NamedMap mMap = new NamedMap("applications");
 
    private String mCurrentAppName = EMPTY_STRING;
    private String mCurrentGrpName = EMPTY_STRING;
    private String mCurrentMsgName = EMPTY_STRING;
    private int mCurrentAppId = 0;
    private int mCurrentGrpId = 0;
    private int mCurrentMsgId = 0;
 
    private final List<String> mWarningMessages = new ArrayList<String>();
 
    private final StringBuffer mBuffer = new StringBuffer();
    private boolean mCaptureCharacters = false;
 
    /**
     * Constructor.
     */
    public AppInfoSaxHandler ()
    {
       mMap.registerApplication(APPLICATION_ID_FWK, "FWK");
       mMap.registerApplication(APPLICATION_ID_ASF, "ASF");
       mMap.registerApplication(APPLICATION_ID_PPG, "PPG");
       mMap.registerApplication(APPLICATION_ID_TAC, "TAC");
       mMap.registerApplication(APPLICATION_ID_ACR, "ACR");
       mMap.registerApplication(APPLICATION_ID_ACM, "ACM");
       mMap.registerApplication(APPLICATION_ID_DMB, "DMB");
    }
 
    /** {@inheritDoc} */
    public void error (SAXParseException exception)
    {
      mValidationError = true;
      mSaxParseException = exception;
    }
 
    /** {@inheritDoc} */
    public void fatalError (SAXParseException exception)
    {
       mValidationError = true;
       mSaxParseException = exception;
    }
 
    /** {@inheritDoc} */
    public void warning (SAXParseException exception)
    {
       // NOP
    }
 
    /** {@inheritDoc} */
    public void startDocument ()
    {
       reset();
    }
 
    /** {@inheritDoc} */
    public void startElement (String uri, String localName,
          String qName, Attributes attributes)
          throws SAXException
    {
       try
       {
          if ("application".equals(localName))
          {
             mCurrentAppName = attributes.getValue("short-name");
             mCurrentAppId = Integer.parseInt(attributes.getValue("id"));
 
             mMap.addApplication(mCurrentAppId, mCurrentAppName);
          }
          else if ("group".equals(localName))
          {
             mCurrentGrpName = attributes.getValue("short-name");
             mCurrentGrpId = Integer.parseInt(attributes.getValue("id"));
 
             mMap.getApp(mCurrentAppId).addGroup(mCurrentGrpId, mCurrentGrpName);
          }
          else if ("message".equals(localName))
          {
             mCurrentMsgName = attributes.getValue("name");
             mCurrentMsgId = Integer.parseInt(attributes.getValue("id"));
 
             mMap.getApp(mCurrentAppId).getGrp(
                   mCurrentGrpId).addMessage(mCurrentMsgId, mCurrentMsgName);
          }
          else if ("text".equals(localName) || "solution".equals(localName))
          {
             captureCharacters();
          }
       }
       catch (AppInfoException e)
       {
          throw new SAXException(e);
       }
    }
 
    /** {@inheritDoc} */
    public void endElement (String uri, String localName, String qName)
    {
       if ("text".equals(localName))
       {
          final String cdata = characters().trim();
          validateText(cdata);
       }
       else if ("solution".equals(localName))
       {
          final String cdata = characters().trim();
          validateSolution(cdata);
       }
       else if ("description".equals(localName))
       {
          final String cdata = characters().trim();
          validateDescription(cdata);
       }
       else if ("procedure".equals(localName))
       {
          final String cdata = characters().trim();
          validateProcedure(cdata);
       }
       else if ("validation".equals(localName))
       {
          final String cdata = characters().trim();
          validateValidation(cdata);
       }
       else if ("application".equals(localName))
       {
          mCurrentAppId = 0;
          mCurrentAppName = EMPTY_STRING;
       }
       else if ("group".equals(localName))
       {
          mCurrentGrpId = 0;
          mCurrentGrpName = EMPTY_STRING;
       }
       else if ("message".equals(localName))
       {
          mCurrentMsgId = 0;
          mCurrentMsgName = EMPTY_STRING;
       }
    }
 
    /** {@inheritDoc} */
    public void characters (char[] ch, int start, int length)
    {
       if (mCaptureCharacters)
       {
          mBuffer.append(ch, start, length);
       }
    }
 
    /**
     * Returns the captured characters and <b>clears</b> the internal
     * buffer.
     * @return the captured characters.
     */
    public String characters ()
    {
       final String result = mBuffer.toString();
       mBuffer.setLength(0);
       mCaptureCharacters = false;
       return result;
    }
 
    /**
     * Returns <tt>true</tt> if there are warning messages available.
     * @return <tt>true</tt> if there are warning messages available;
     *       <tt>false</tt> otherwise.
     */
    public boolean hasWarningMessages ()
    {
       return !mWarningMessages.isEmpty();
    }
 
    /**
     * Returns a list&lt;String&gt; of warning messages.
     * @return a list&lt;String&gt; of warning messages.
     */
    public List<String> getWarningMessages ()
    {
       return mWarningMessages;
    }
 
 
    void captureCharacters ()
    {
       mCaptureCharacters = true;
    }
 
    SAXParseException getParseException ()
    {
       return mSaxParseException;
    }
 
    boolean hasValidationErrors ()
    {
       return mValidationError;
    }
 
    private void validateText (String cdata)
    {
       if (cdata != null)
       {
          if (REGEX_SINGLE_QUOTES.matcher(cdata).matches())
          {
             warn("The text element contains single quotes.");
          }
       }
    }
 
    private void validateSolution (String cdata)
    {
       if (cdata != null)
       {
          if (REGEX_VARIABLES.matcher(cdata).matches())
          {
             warn("The solution element MUST NOT use variables: " + cdata);
          }
       }
    }
 
    private void validateDescription (String cdata)
    {
       if (cdata != null)
       {
          if (REGEX_VARIABLES.matcher(cdata).matches())
          {
             warn("The solution element MUST NOT use variables: " + cdata);
          }
       }
    }
 
    private void validateProcedure (String cdata)
    {
       if (cdata != null)
       {
          if (REGEX_VARIABLES.matcher(cdata).matches())
          {
             warn("The solution element MUST NOT use variables: " + cdata);
          }
       }
    }
 
    private void validateValidation (String cdata)
    {
       if (cdata != null)
       {
          if (REGEX_VARIABLES.matcher(cdata).matches())
          {
             warn("The solution element MUST NOT use variables: " + cdata);
          }
       }
    }
 
    private void warn (String message)
    {
       mWarningMessages.add("[" + mCurrentAppName + "."
             + mCurrentGrpName + "." + mCurrentMsgName + "] " + message);
    }
 
    private void reset ()
    {
       mBuffer.setLength(0);
       mCaptureCharacters = false;
       mCurrentAppName = EMPTY_STRING;
       mCurrentGrpName = EMPTY_STRING;
       mCurrentMsgName = EMPTY_STRING;
       mWarningMessages.clear();
       mCurrentAppId = 0;
       mCurrentGrpId = 0;
       mCurrentMsgId = 0;
    }
 
    private static class NamedMap
    {
      private final Map mMap = new HashMap();
       private final String mName;
 
       public NamedMap (String name)
       {
          mName = name;
       }
 
       public String getName ()
       {
          return mName;
       }
 
       public boolean contains (int id)
       {
          return mMap.containsKey(id);
       }
 
       public NamedMap getApp (int id)
       {
          return (NamedMap) mMap.get(id);
       }
 
       public void registerApplication (int id, String appName)
       {
          mMap.put(id, new NamedMap(appName));
       }
 
       public void addApplication (int id, String appName)
             throws AppInfoException
       {
          if (!contains(id))
          {
             throw new AppInfoException(
                   "Application " + appName + " with the identifier "
                   + id + " is not registered. "
                   + "Registered applications are " + mMap);
          }
       }
 
 
       public NamedMap getGrp (int id)
       {
          return (NamedMap) mMap.get(id);
       }
 
       public void addGroup (int id, String groupName)
             throws AppInfoException
       {
          if (contains(id))
          {
             final String registeredGrpName
                   = ((NamedMap) mMap.get(id)).getName();
             throw new AppInfoException("The group " + groupName
                   + " with the id " + id
                   + " is already assigned to " + registeredGrpName + ".");
          }
          mMap.put(id, new NamedMap(groupName));
       }
 
       public void addMessage (int id, String messageName)
             throws AppInfoException
       {
          if (contains(id))
          {
             final String registeredMsgName = (String) mMap.get(id);
             throw new AppInfoException("The message " + messageName
                   + " with the id " + id
                   + " is already assigned to " + registeredMsgName + ".");
          }
          mMap.put(id, messageName);
 
       }
 
       public String toString ()
       {
          final StringBuffer sb = new StringBuffer();
          sb.append(mName);
          sb.append(' ');
          sb.append(mMap);
          return sb.toString();
       }
    }
 
 
    private static class AppInfoException
          extends Exception
    {
       private static final long serialVersionUID = 1L;
 
       public AppInfoException (String msg)
       {
          super(msg);
       }
 
       public AppInfoException (String msg, Throwable cause)
       {
          super(msg, cause);
       }
    }
 
 }
