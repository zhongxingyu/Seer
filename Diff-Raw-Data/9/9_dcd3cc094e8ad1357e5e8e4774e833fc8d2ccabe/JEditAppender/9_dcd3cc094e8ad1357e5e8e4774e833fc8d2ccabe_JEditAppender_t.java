 /*
  * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
  *
  * This software is distributable under the BSD license. See the terms of the
  * BSD license in the documentation provided with this software.
  */
 package de.hunsicker.jalopy.plugin.jedit;
 
 import org.gjt.sp.jedit.view.message.Message;
 import org.gjt.sp.jedit.view.message.MessageTab;
 import org.gjt.sp.jedit.view.message.MessageType;
 import org.gjt.sp.jedit.view.message.MessageView;
 
 import java.io.File;
 
 import de.hunsicker.jalopy.plugin.AbstractAppender;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.spi.LoggingEvent;
 
 import org.apache.oro.text.regex.MatchResult;
 
 
 /**
  * Appender which displays messages in a jEdit message view.
  *
  * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
  * @version $Revision$
  */
 final class JEditAppender
     extends AbstractAppender
 {
     //~ Static variables/initializers ----------------------------------------------------
 
     /** Our message category. */
     private static final MessageTab TAB_JALOPY = new MessageTab("Jalopy" /* NOI18N */);
 
     //~ Constructors ---------------------------------------------------------------------
 
     /**
      * Creates a new JEditAppender object.
      */
     public JEditAppender()
     {
         super();
     }
 
     //~ Methods --------------------------------------------------------------------------
 
     /**
      * Does the actual outputting.
      *
      * @param ev logging event.
      */
     public void append(LoggingEvent ev)
     {
         switch (ev.getLevel().toInt())
         {
             case Level.WARN_INT :
                 append(ev, MessageType.WARN);
 
                 break;
 
             case Level.DEBUG_INT :
                 append(ev, MessageType.DEBUG);
 
                 break;
 
             case Level.ERROR_INT :
             case Level.FATAL_INT :
                 append(ev, MessageType.ERROR);
 
                 break;
 
             case Level.INFO_INT :
             default :
                 append(ev, MessageType.INFO);
 
                 break;
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void clear()
     {
         MessageView.getInstance().clear(TAB_JALOPY);
     }
 
 
     /**
      * Adds a new message to the message view.
      *
      * @param ev the log4j logging event.
      * @param type the type of the logging event.
      */
     private void append(
         LoggingEvent ev,
         MessageType  type)
     {
         MatchResult result = parseMessage(ev);
 
         if (result == null)
         {
             MessageView.getInstance().addMessage(
                 new Message(ev.getRenderedMessage(), type), TAB_JALOPY, false);
         }
         else
         {
             String text = result.group(POS_TEXT);
             String filename = result.group(POS_FILENAME);
             int line = 0;
             int column = 0;
 
             try
             {
                 line = Integer.parseInt(result.group(POS_LINE));
                 column = Integer.parseInt(result.group(POS_COLUMN));
             }
             catch (NumberFormatException ex)
             {
                // never happens as the regex already validated the numbers
             }
 
             MessageView.getInstance().addMessage(
                 new Message(text, new File(filename), line, column, 0, type), TAB_JALOPY,
                 false);
         }
     }
 }
