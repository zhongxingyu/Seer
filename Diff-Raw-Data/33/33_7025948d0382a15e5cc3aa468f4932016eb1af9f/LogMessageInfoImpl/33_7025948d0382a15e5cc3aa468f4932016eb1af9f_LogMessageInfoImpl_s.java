 /*
  * $Id: LogMessageInfoImpl.java 1247 2008-11-04 20:00:09Z amandel $
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
 package org.jcoderz.commons;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 
 /**
  * This abstract class implements the interface
  * {@link org.jcoderz.commons.LogMessageInfo}.
  * <p>
  * All parameters are stored as immutable fields.
  * Besides the parameters from the interface, an (unique) error
  * id must be supplied during construction.
  *
  * @author Michael Griffel
  */
 public abstract class LogMessageInfoImpl
       implements LogMessageInfo
 {
    /** use this serialVersionUID for serialization. */
    static final long serialVersionUID = 1L;
 
    private final String mErrorSymbol;
    private final int mErrorId;
    private final Level mLogLevel;
    private final String mMessagePattern;
    private final String mSolution;
    private final List<String> mParameters;
    private final BusinessImpact mBusinessImpact;
    private final Category mCategory;
    private final String mApplicationName;
    private final String mApplicationNameAbbreviation;
    private final String mGroupName;
    private final String mGroupNameAbbreviation;
 
    protected LogMessageInfoImpl (String symbol, int id, Level level,
          String text, String solution, BusinessImpact businessImpact,
          Category category, String[] params, String appName,
          String appNameAbbr, String groupName, String groupNameAbbr)
    {
       mErrorSymbol = symbol;
       mErrorId = id;
       mLogLevel = level;
       mMessagePattern = text;
       mSolution = solution;
       mBusinessImpact = businessImpact;
       mCategory = category;
       mParameters = Collections.unmodifiableList(Arrays.asList(params));
       mApplicationName = appName;
       mApplicationNameAbbreviation = appNameAbbr;
       mGroupName = groupName;
       mGroupNameAbbreviation = groupNameAbbr;
    }
 
    /**
     * Returns the (unique) integer representation of the log message info.
     * This integer representation is also called the <b>error identifier</b>
     * an is constructed as defined below:
     * <pre>
     *               +=========+=========+=========+=========+
     * ID (32 bit) : |0XXX XXXX|YYYY YYYY|ZZZZ ZZZZ|ZZZZ ZZZZ|
     *               +=========+=========+=========+=========+
     *
     *               XXX (7 bit)  -> application @id
     *               YYY (8 bit)  -> group @id
     *               ZZZ (16 bit) -> message @id
     * </pre>
     * <b>Note:</b>
     * This value should also be printed in its hexadecimal string
     * representation since it could then be easily <i>masked</i> by
     * human readers.
     * @return the integer representation of the log message info.
     */
    public final int toInt ()
    {
       return mErrorId;
    }
 
    /**
     * Returns the string representation of the log message info. This is the
     * symbolic string representation as defined in the parameter
     * <code>symbol</code> during construction.
     * @see java.lang.Object#toString()
     * @return the string representation of the log message info.
     */
    public final String toString ()
    {
       return mErrorSymbol;
    }
 
    // LogMessageInfo interface
 
    /** {@inheritDoc} */
    public final String getSymbol ()
    {
       return mErrorSymbol;
    }
 
    /** {@inheritDoc} */
    public final Level getLogLevel ()
    {
       return mLogLevel;
    }
 
    /** {@inheritDoc} */
    public final String getMessagePattern ()
    {
       return mMessagePattern;
    }
 
    /** {@inheritDoc} */
    public final StringBuffer formatMessage (Map parameters, StringBuffer buffer)
    {
        final StringBuffer result
            = buffer != null ? buffer : new StringBuffer();
        try
        {
           final MessageFormat formatter = new MessageFormat(getMessagePattern());
 
           final List parameter = new ArrayList();
 
           if (parameters != null && !getParameterList().isEmpty())
           {
             final Iterator i = getParameterList().iterator();
              while (i.hasNext())
              {
                 final String parameterName = (String) i.next();
                 final List<?> parameterValues = (List<?>) parameters.get(parameterName);
                 if (parameterValues == null || parameterValues.isEmpty())
                 {
                    parameter.add(null);
                 }
                 else
                 {
                    parameter.add(parameterValues.get(0));
                 }
              }
           }
           formatter.format(parameter.toArray(), result, null);
        }
        // could be caused by invalid message format!
        catch (Exception ex)
        {
            result.append(parameters);
            result.append(' ');
            result.append(getMessagePattern());
        }
       return result;
    }
 
    /** {@inheritDoc} */
    public final String getSolution ()
    {
       return mSolution;
    }
 
    /** {@inheritDoc} */
    public final BusinessImpact getBusinessImpact ()
    {
       return mBusinessImpact;
    }
 
    /** {@inheritDoc} */
    public final Category getCategory ()
    {
       return mCategory;
    }
 
    /** {@inheritDoc} */
    public final List<String> getParameterList ()
    {
       return mParameters;
    }
 
    /** {@inheritDoc} */
    public String getAppName ()
    {
       return mApplicationName;
    }
 
    /** {@inheritDoc} */
    public String getAppNameAbbreviation ()
    {
       return mApplicationNameAbbreviation;
    }
 
    /** {@inheritDoc} */
    public String getGroupName ()
    {
       return mGroupName;
    }
 
    /** {@inheritDoc} */
    public String getGroupNameAbbreviation ()
    {
       return mGroupNameAbbreviation;
    }
 }
