 /*
  * $Id: BasicLogLineFormat.java 1537 2009-07-13 14:30:55Z amandel $
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
 package org.jcoderz.commons.logging;
 
import java.text.Format;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 import org.jcoderz.commons.BusinessImpact;
 import org.jcoderz.commons.Category;
 import org.jcoderz.commons.Loggable;
 import org.jcoderz.commons.LoggableImpl;
 import org.jcoderz.commons.types.Date;
 import org.jcoderz.commons.util.Constants;
 
 
 
 /**
  * This class is the base class for log formats formatting the main log line of
  * the log message, which contains all fields required for CA Unicenter
  * integration. All sub classes of this are required to use these fields at the
  * index required by this, but are allowed to append additional fields.
  *
  */
 public abstract class BasicLogLineFormat
       extends LogLineFormat
 {
    /** The number of parameters of the basic format. */
    protected static final int NUMBER_OF_PARAMETERS = 9;
 
    private static final int TIMESTAMP_INDEX = 0;
    private static final int NODEID_INDEX = 1;
    private static final int INSTANCEID_INDEX = 2;
    private static final int THREADID_INDEX = 3;
    private static final int LEVEL_INDEX = 4;
    private static final int MESSAGEID_INDEX = 5;
    private static final int BUSINESSIMPACT_INDEX = 6;
    private static final int CATEGORY_INDEX = 7;
    private static final int THREAD_NAME_INDEX = 7;
    private static final int TRACKINGID_INDEX = 8;
 
    /**
     * Common format for CA Unicenter log lines.
     * Has the fields in following order:
     * <ul>
     * <li>timestamp
     * <li>node id
     * <li>instance id
     * <li>thread id
     * <li>logger level
     * <li>message id
     * <li>business impact
     * <li>category
     * <li>tracking number
     * <li>thread name
     * </ul>
     */
    private static final String LOGLINE_FORMAT_PATTERN
          = "{0} {1} {2} {3} {4} {5} {6} {7} {8}";
 
    private static final String NO_MSG_SYMBOL = "TRACEMSG";
 
    private final int mNumParameters;
    private final String mLogFormatPattern;
 
 
    /**
     * Creates a new instance of this and initializes the message format.
     *
     * @param type THe log line type.
     * @param additionalPattern The pattern for additional fields not included by
     * this. Must start with the desired delimiter before the first field.
     * @param numAdditionalParameters The number of additional fields.
     */
    protected BasicLogLineFormat (
          final LogLineType type,
          final String additionalPattern,
          final int numAdditionalParameters)
    {
       super(type, new MessageFormat(type.getTypeSpecifier() + " "
             + LOGLINE_FORMAT_PATTERN + additionalPattern),
             NUMBER_OF_PARAMETERS + numAdditionalParameters);
 
       mNumParameters = NUMBER_OF_PARAMETERS + numAdditionalParameters;
       mLogFormatPattern = LOGLINE_FORMAT_PATTERN + additionalPattern;
    }
 
    /**
     * Gets the formats as array for formatting all elements of a basic log line.
     *
     * @param options The display options specifying which fields to display.
     * Will be ignored and might be null if <code>ignoreOptions == true</code>.
     * @param ignoreOptions flag whether to ignore the supplied options and
     * return the formats for all fields.
     *
     * @return List filled with formats for each selected field. Might be empty,
     * never null.
     */
   protected static List<Format> getBasicFormatList (
          final DisplayOptions options,
          final boolean ignoreOptions)
    {
      final List<Format> formatList = new ArrayList<Format>();
       if (ignoreOptions || options.displayTimestamp())
       {
          // date
          formatList.add(getTimestampFormat());
       }
       if (ignoreOptions || options.displayNodeId())
       {
          // node id
          formatList.add(getNodeIdFormat());
       }
       if (ignoreOptions || options.displayInstanceId())
       {
          // instance id
          formatList.add(getInstanceIdFormat());
       }
       if (ignoreOptions || options.displayThreadId())
       {
          // thread id
          formatList.add(getThreadIdFormat());
       }
       if (ignoreOptions || options.displayLoggerLevel())
       {
          // logger level
          formatList.add(getLoggerLevelFormat());
       }
       if (ignoreOptions || options.displaySymbolId())
       {
          // symbol
          formatList.add(getMessageSymbolFormat());
       }
       if (ignoreOptions || options.displayBusinessImpact())
       {
          // business impact
          formatList.add(getBusinessImpactFormat());
       }
       if (ignoreOptions || options.displayThreadName())
       {
          // category
          formatList.add(getThreadNameFormat());
       }
       // was replaced with thread name in default
       if (!ignoreOptions && options.displayCategory())
       {
          // category
          formatList.add(getCategoryFormat());
       }
       if (ignoreOptions || options.displayTrackingNumber())
       {
          // sequence of tracking id
          formatList.add(getTrackingNumberFormat());
       }
       return formatList;
    }
 
    /**
     * Formats the supplied LogRecord with the encapsulated basic message format.
     * Data for additional fields has to be set before calling this.
     * Appends a line feed after the data is formatted into the StringBuffer.
     *
     * @param sb The StringBuffer where to append the formatted LogRecord.
     * @param record The LogRecord to format.
     * @param loggable Unused by this, might be null.
     * @param trackingIdSequence The list containing the sequence of tracking
     * ids contributing to this log message.
     */
    protected final void basicFormat (
          final StringBuffer sb,
          final LogRecord record,
          final Loggable loggable,
          final List trackingIdSequence)
    {
       setLevel(record.getLevel());
       setTrackingIds(trackingIdSequence);
 
       if (loggable != null)
       {
          setTimestamp(new Date(loggable.getEventTime()));
          setNodeId(loggable.getNodeId());
          setInstanceId(loggable.getInstanceId());
          setThreadId(loggable.getThreadId());
          setMessageId(
                Integer.toHexString(loggable.getLogMessageInfo().toInt()));
          setBusinessImpact(loggable.getLogMessageInfo().getBusinessImpact());
          setCategory(loggable.getLogMessageInfo().getCategory());
          try
          {
              setThreadName(loggable.getThreadName());
          }
          catch (AbstractMethodError ex)
          {
              // We have a old loggable that does not support
              // thread name jet.
              setThreadName(Thread.currentThread().getName());
          }
       }
       else
       {
          setTimestamp(new Date(record.getMillis()));
          setNodeId(LoggableImpl.NODE_ID);
          setInstanceId(LoggableImpl.INSTANCE_ID);
          setThreadId(record.getThreadID());
          setBusinessImpact(BusinessImpact.NONE);
          setCategory(Category.TECHNICAL);
          // Take care this one might be wrong!
          setThreadName(Thread.currentThread().getName());
          setMessageId(NO_MSG_SYMBOL);
       }
       format(sb);
       sb.append(Constants.LINE_SEPARATOR);
    }
 
    /**
     * Parses a log line, which must be formatted by this, and sets the
     * appropriate basic values of the supplied LogFileEntry. Derived types might
     * retrieve specific field values after this has been called.
     *
     * @param sb The StringBuffer containing the current log line.
     * @param entry The LogFileEntry for which to parse the log line.
     *
     * @throws ParseException if an error occurs parsing <code>sb</code>.
     *
     * @see org.jcoderz.commons.logging.LogLineFormat#parse(java.lang.StringBuffer, org.jcoderz.commons.logging.LogFileEntry)
     */
    protected final void basicParse (StringBuffer sb, LogFileEntry entry)
          throws ParseException
    {
       try
       {
          parse(sb);
 
          entry.setBusinessImpact(getBusinessImpact());
          // entry.setCategory(getCategory());
          entry.setThreadName(getThreadName());
          entry.setInstanceId(getInstanceId());
          entry.setLoggerLevel(getLevel());
          entry.setNodeId(getNodeId());
          entry.setThreadId(getThreadId());
          entry.setSymbolId(getMessageId());
          entry.setTimestamp(getTimestamp());
 
          // the last element within the tracking id sequence is the id of the
          // current entry.
          final List trackingIds = getTrackingIds();
          entry.setTrackingNumber((String) trackingIds.get(
                trackingIds.size() - 1));
       }
       catch (ParseException pex)
       {
          // just rethrow
          throw pex;
       }
       catch (Exception ex)
       {
          final ParseException pex = new ParseException(
                "Got an error parsing " + sb, 0);
          pex.initCause(ex);
          throw pex;
       }
    }
 
 
    /**
     * Sets the timestamp.
     *
     * @param timestamp The timestamp to set.
     */
    protected final void setTimestamp (final Date timestamp)
    {
       setParameter(TIMESTAMP_INDEX, timestamp);
    }
 
    /**
     * Gets the timestamp of a parsed log line.
     *
     * @return Timestamp of parsed log line.
     */
    protected final Date getTimestamp ()
    {
       return (Date) getParameter(TIMESTAMP_INDEX);
    }
 
    /**
     * Sets the node id to dump.
     *
     * @param nodeId The node id to dump.
     */
    protected final void setNodeId (final String nodeId)
    {
       setParameter(NODEID_INDEX, nodeId);
    }
 
    /**
     * Gets the node id of a parsed log line.
     *
     * @return Node id of parsed log line.
     */
    protected final String getNodeId ()
    {
       return (String) getParameter(NODEID_INDEX);
    }
 
    /**
     * Sets the instance id to dump.
     *
     * @param instanceId The instance id to dump.
     */
    protected final void setInstanceId (final String instanceId)
    {
       setParameter(INSTANCEID_INDEX, instanceId);
    }
 
    /**
     * Gets the instance id of a parsed log line.
     *
     * @return Instance id of parsed log line.
     */
    protected final String getInstanceId ()
    {
       return (String) getParameter(INSTANCEID_INDEX);
    }
 
    /**
     * Sets the thread id from the message to dump.
     *
     * @param threadId The thread id to dump.
     */
    protected final void setThreadId (final long threadId)
    {
       setParameter(THREADID_INDEX, String.valueOf(threadId));
    }
 
    /**
     * Gets the thread id of a parsed log line.
     *
     * @return Thread id of parsed log line.
     */
    protected final long getThreadId ()
    {
       return Long.parseLong((String) getParameter(THREADID_INDEX));
    }
 
    /**
     * Sets the thread name from the message to dump.
     *
     * @param threadName The thread name to dump.
     */
    protected final void setThreadName (String threadName)
    {
       setParameter(THREAD_NAME_INDEX, threadName);
    }
 
    /**
     * Gets the thread id of a parsed log line.
     *
     * @return Thread id of parsed log line.
     */
    protected final String getThreadName ()
    {
       return (String) getParameter(THREAD_NAME_INDEX);
    }
 
    /**
     * Sets the list of tracking ids to dump.
     *
     * @param trackingIds The sequence of tracking ids to dump.
     */
    protected final void setTrackingIds (final List trackingIds)
    {
       setParameter(TRACKINGID_INDEX, trackingIds);
    }
 
    /**
     * Gets the sequence of tracking ids of a parsed log line.
     *
     * @return Sequence of tracking ids of parsed log line.
     */
    protected final List getTrackingIds ()
    {
       return (List) getParameter(TRACKINGID_INDEX);
    }
 
    /**
     * Sets the level of the message to dump,
     *
     * @param level The log level of the message.
     */
    protected final void setLevel (final Level level)
    {
       setParameter(LEVEL_INDEX, level.getName());
    }
 
    /**
     * Gets the log level of a parsed log line.
     *
     * @return Log level of parsed log line.
     */
    protected final Level getLevel ()
    {
       return Level.parse((String) getParameter(LEVEL_INDEX));
    }
 
    /**
     * Sets the message id from the message to dump.
     *
     * @param messageId The message id of the message.
     */
    protected final void setMessageId (final String messageId)
    {
       setParameter(MESSAGEID_INDEX, messageId);
    }
 
    /**
     * Gets the message id of a parsed log line.
     *
     * @return Message id of parsed log line.
     */
    protected final String getMessageId ()
    {
       return (String) getParameter(MESSAGEID_INDEX);
    }
 
    /**
     * Sets the business impact from the message to dump.
     *
     * @param impact The business impact of the message.
     */
    protected final void setBusinessImpact (final BusinessImpact impact)
    {
       setParameter(BUSINESSIMPACT_INDEX, impact.toString());
    }
 
    /**
     * Gets the business impact of a parsed log line.
     *
     * @return Business impact of parsed log line.
     */
    protected final BusinessImpact getBusinessImpact ()
    {
       return BusinessImpact.fromString(
             (String) getParameter(BUSINESSIMPACT_INDEX));
    }
 
    /**
     * Sets the category to dump.
     *
     * @param category The category of the message to dump.
     */
    protected final void setCategory (final Category category)
    {
       setParameter(CATEGORY_INDEX, category.toString());
    }
 
    /**
     * Gets the category of a parsed log line.
     *
     * @return Category of parsed log line.
     */
    protected final Category getCategory ()
    {
       return Category.fromString((String) getParameter(CATEGORY_INDEX));
    }
 
 }
