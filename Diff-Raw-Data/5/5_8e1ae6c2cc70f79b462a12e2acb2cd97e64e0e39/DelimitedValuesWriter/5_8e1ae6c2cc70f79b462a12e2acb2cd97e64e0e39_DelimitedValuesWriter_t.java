 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.commons.text;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Writer;
 
 /**
  * Print values as a comma separated list.
  * More information about this class is available from <a target="_top" href=
  * "http://ostermiller.org/utils/CSV.html">ostermiller.org</a>.
  *
  * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
  * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
  */
 public class DelimitedValuesWriter
 {
     /**
      * If auto flushing is enabled.
      */
     protected boolean autoFlush = true;
 
     /**
      * If auto flushing is enabled.
      */
     protected boolean alwaysQuote = false;
 
     /**
      * true iff an error has occurred.
      */
     protected boolean error = false;
 
     /**
      * Delimiter character written.
      */
     protected char delimiterChar = ',';
 
     /**
      * Quoting character written.
      */
     protected char quoteChar = '"';
 
     /**
      * The place that the values get written.
      */
     protected Writer out;
 
     /**
      * True iff we just began a new line.
      */
     protected boolean newLine = true;
 
     /**
      * Character used to start comments. (Default is '#')
      */
     protected char commentStart = '#';
 
     /**
      * Change this printer so that it uses a new delimiter.
      *
      * @param newDelimiter The new delimiter character to use.
      */
     public void changeDelimiter(char newDelimiter)
     {
         if(delimiterChar == newDelimiter) return; // no need to do anything.
         if(newDelimiter == '\n' || newDelimiter == '\r' ||
           newDelimiter == quoteChar)
             throw new RuntimeException("Invalid delimiter: " + newDelimiter);
 
         delimiterChar = newDelimiter;
     }
 
     /**
      * Change this printer so that it uses a new character for quoting.
      *
      * @param newQuote The new character to use for quoting.
      */
     public void changeQuote(char newQuote)
     {
         if(newQuote == '\n' || newQuote == '\r' ||
           newQuote == delimiterChar)
             throw new RuntimeException("Invalid quote character: " + newQuote);
 
         quoteChar = newQuote;
     }
 
     /**
      * Create a printer that will print values to the given
      * stream.	 Character to byte conversion is done using
      * the default character encoding.	Comments will be
      * written using the default comment character '#', the delimiter will
      * be the comma, the quote character will be double quotes,
      * quotes will be used when needed, and auto flushing
      * will be enabled.
      *
      * @param out stream to which to print.
      */
     public DelimitedValuesWriter(OutputStream out)
     {
         this.out = new OutputStreamWriter(out);
     }
 
     /**
      * Create a printer that will print values to the given
      * stream.	Comments will be
      * written using the default comment character '#', the delimiter will
      * be the comma, the quote character will be double quotes,
      * quotes will be used when needed, and auto flushing
      * will be enabled.
      *
      * @param out stream to which to print.
      */
     public DelimitedValuesWriter(Writer out)
     {
         this.out = out;
     }
 
     /**
      * Create a printer that will print values to the given
      * stream.	 Character to byte conversion is done using
      * the default character encoding.  The delimiter will
      * be the comma, the quote character will be double quotes,
      * quotes will be used when needed, and auto flushing
      * will be enabled.
      *
      * @param out          stream to which to print.
      * @param commentStart Character used to start comments.
      */
     public DelimitedValuesWriter(OutputStream out, char commentStart)
     {
         this(out);
         this.commentStart = commentStart;
     }
 
     /**
      * Create a printer that will print values to the given
      * stream.  The delimiter will
      * be the comma, the quote character will be double quotes,
      * quotes will be used when needed, and auto flushing
      * will be enabled.
      *
      * @param out          stream to which to print.
      * @param commentStart Character used to start comments.
      */
     public DelimitedValuesWriter(Writer out, char commentStart)
     {
         this(out);
         this.commentStart = commentStart;
     }
 
     /**
      * Create a printer that will print values to the given
      * stream.	The comment character will be the number sign, the delimiter will
      * be the comma, and the quote character will be double quotes.
      *
      * @param out         stream to which to print.
      * @param alwaysQuote true if quotes should be used even when not strictly needed.
      * @param autoFlush   should auto flushing be enabled.
      *
      * @since ostermillerutils 1.02.26
      */
     public DelimitedValuesWriter(Writer out, boolean alwaysQuote, boolean autoFlush)
     {
         this.out = out;
         setAlwaysQuote(alwaysQuote);
         setAutoFlush(autoFlush);
     }
 
     /**
      * Create a printer that will print values to the given
      * stream.	Quotes will be used when needed, and auto flushing
      * will be enabled.
      *
      * @param out          stream to which to print.
      * @param commentStart Character used to start comments.
      * @param delimiter    The new delimiter character to use.
      * @param quote        The new character to use for quoting.
      */
     public DelimitedValuesWriter(Writer out, char commentStart, char quote, char delimiter)
     {
         this.out = out;
         this.commentStart = commentStart;
         changeQuote(quote);
         changeDelimiter(delimiter);
     }
 
     /**
      * Create a printer that will print values to the given
      * stream.
      *
      * @param out          stream to which to print.
      * @param commentStart Character used to start comments.
      * @param delimiter    The new delimiter character to use.
      * @param quote        The new character to use for quoting.
      * @param alwaysQuote  true if quotes should be used even when not strictly needed.
      * @param autoFlush    should auto flushing be enabled.
      */
     public DelimitedValuesWriter(Writer out, char commentStart, char quote, char delimiter, boolean alwaysQuote, boolean autoFlush)
     {
         this.out = out;
         this.commentStart = commentStart;
         changeQuote(quote);
         changeDelimiter(delimiter);
         setAlwaysQuote(alwaysQuote);
         setAutoFlush(autoFlush);
     }
 
     /**
      * Print the string as the last value on the line.	The value
      * will be quoted if needed.
      * <p/>
      * This method never throws an I/O exception. The client may inquire as to whether
      * any errors have occurred by invoking checkError().  If an I/O Exception is
      * desired, the client should use the corresponding writeln method.
      *
      * @param value value to be outputted.
      */
     public void println(String value)
     {
         try
         {
             writeln(value);
         }
         catch(IOException iox)
         {
             error = true;
         }
     }
 
     /**
      * Print the string as the last value on the line.	The value
      * will be quoted if needed.
      *
      * @param value value to be outputted.
      */
     public void writeln(String value) throws IOException
     {
         try
         {
             write(value);
             writeln();
         }
         catch(IOException iox)
         {
             error = true;
             throw iox;
         }
     }
 
     /**
      * Output a blank line.
      * <p/>
      * This method never throws an I/O exception. The client may inquire as to whether
      * any errors have occurred by invoking checkError().  If an I/O Exception is
      * desired, the client should use the corresponding writeln method.
      */
     public void println()
     {
         try
         {
             writeln();
         }
         catch(IOException iox)
         {
             error = true;
         }
     }
 
     /**
      * Output a blank line.
      */
     public void writeln() throws IOException
     {
         try
         {
             out.write("\n");
             if(autoFlush) flush();
             newLine = true;
         }
         catch(IOException iox)
         {
             error = true;
             throw iox;
         }
     }
 
     /**
      * Print a single line of comma separated values.
      * The values will be quoted if needed.  Quotes and
      * and other characters that need it will be escaped.
      * <p/>
      * This method never throws an I/O exception. The client may inquire as to whether
      * any errors have occurred by invoking checkError().  If an I/O Exception is
      * desired, the client should use the corresponding writeln method.
      *
      * @param values values to be outputted.
      */
     public void println(String[] values)
     {
         try
         {
             writeln(values);
         }
         catch(IOException iox)
         {
             error = true;
         }
     }
 
     /**
      * Print a single line of comma separated values.
      * The values will be quoted if needed.  Quotes and
      * and other characters that need it will be escaped.
      *
      * @param values values to be outputted.
      */
     public void writeln(String[] values) throws IOException
     {
         try
         {
             print(values);
             writeln();
         }
         catch(IOException iox)
         {
             error = true;
             throw iox;
         }
     }
 
     /**
      * Print a single line of comma separated values.
      * The values will be quoted if needed.  Quotes and
      * and other characters that need it will be escaped.
      * <p/>
      * This method never throws an I/O exception. The client may inquire as to whether
      * any errors have occurred by invoking checkError().  If an I/O Exception is
      * desired, the client should use the corresponding writeln method.
      *
      * @param values values to be outputted.
      */
     public void print(String[] values)
     {
         try
         {
             write(values);
         }
         catch(IOException iox)
         {
             error = true;
         }
     }
 
     /**
      * Print a single line of comma separated values.
      * The values will be quoted if needed.  Quotes and
      * and other characters that need it will be escaped.
      *
      * @param values values to be outputted.
      */
     public void write(String[] values) throws IOException
     {
         try
         {
             for(int i = 0; i < values.length; i++)
             {
                 write(values[i]);
             }
         }
         catch(IOException iox)
         {
             error = true;
             throw iox;
         }
     }
 
     /**
      * Print several lines of comma separated values.
      * The values will be quoted if needed.  Quotes and
      * newLine characters will be escaped.
      * <p/>
      * This method never throws an I/O exception. The client may inquire as to whether
      * any errors have occurred by invoking checkError().  If an I/O Exception is
      * desired, the client should use the corresponding writeln method.
      *
      * @param values values to be outputted.
      */
     public void println(String[][] values)
     {
         try
         {
             writeln(values);
         }
         catch(IOException iox)
         {
             error = true;
         }
     }
 
     /**
      * Print several lines of comma separated values.
      * The values will be quoted if needed.  Quotes and
      * newLine characters will be escaped.
      *
      * @param values values to be outputted.
      */
     public void writeln(String[][] values) throws IOException
     {
         try
         {
             for(int i = 0; i < values.length; i++)
             {
                 writeln(values[i]);
             }
             if(values.length == 0)
             {
                 writeln();
             }
         }
         catch(IOException iox)
         {
             error = true;
             throw iox;
         }
     }
 
     /**
      * Put a comment among the comma separated values.
      * Comments will always begin on a new line and occupy a
      * least one full line. The character specified to star
      * comments and a space will be inserted at the beginning of
      * each new line in the comment.  If the comment is null,
      * an empty comment is outputted.
      * <p/>
      * This method never throws an I/O exception. The client may inquire as to whether
      * any errors have occurred by invoking checkError().  If an I/O Exception is
      * desired, the client should use the corresponding writelnComment method.
      *
      * @param comment the comment to output.
      */
     public void printlnComment(String comment)
     {
         try
         {
             writelnComment(comment);
         }
         catch(IOException iox)
         {
             error = true;
         }
     }
 
     /**
      * Put a comment among the comma separated values.
      * Comments will always begin on a new line and occupy a
      * least one full line. The character specified to star
      * comments and a space will be inserted at the beginning of
      * each new line in the comment.  If the comment is null,
      * an empty comment is outputted.
      *
      * @param comment the comment to output.
      */
     public void writelnComment(String comment) throws IOException
     {
         try
         {
             if(comment == null) comment = "";
             if(!newLine)
             {
                 writeln();
             }
             out.write(commentStart);
             out.write(' ');
             for(int i = 0; i < comment.length(); i++)
             {
                 char c = comment.charAt(i);
                 switch(c)
                 {
                     case '\r':
                         {
                             if(i + 1 < comment.length() && comment.charAt(i + 1) == '\n')
                             {
                                 i++;
                             }
                         } //break intentionally excluded.
                     case '\n':
                         {
                             writeln();
                             out.write(commentStart);
                             out.write(' ');
                         }
                         break;
                     default:
                         {
                             out.write(c);
                         }
                         break;
                 }
             }
             writeln();
         }
         catch(IOException iox)
         {
             error = true;
             throw iox;
         }
     }
 
     /**
      * Print the string as the next value on the line.	The value
      * will be quoted if needed.  If value is null, an empty value is printed.
      * <p/>
      * This method never throws an I/O exception. The client may inquire as to whether
      * any errors have occurred by invoking checkError().  If an I/O Exception is
      * desired, the client should use the corresponding println method.
      *
      * @param value value to be outputted.
      */
     public void print(String value)
     {
         try
         {
             write(value);
         }
         catch(IOException iox)
         {
             error = true;
         }
     }
 
     /**
      * Print the string as the next value on the line.	The value
      * will be quoted if needed.  If value is null, an empty value is printed.
      *
      * @param value value to be outputted.
      */
     public void write(String value) throws IOException
     {
         try
         {
             if(value == null) value = "";
             boolean quote = false;
             if(alwaysQuote)
             {
                 quote = true;
             }
             else if(value.length() > 0)
             {
                 char c = value.charAt(0);
                 if(newLine && (c < '0' || (c > '9' && c < 'A') || (c > 'Z' && c < 'a') || (c > 'z')))
                 {
                     quote = true;
                 }
                 if(c == ' ' || c == '\f' || c == '\t')
                 {
                     quote = true;
                 }
                 for(int i = 0; i < value.length(); i++)
                 {
                     c = value.charAt(i);
                     if(c == quoteChar || c == delimiterChar || c == '\n' || c == '\r')
                     {
                         quote = true;
                     }
                 }
                 if(c == ' ' || c == '\f' || c == '\t')
                 {
                     quote = true;
                 }
             }
             else if(newLine)
             {
                 // always quote an empty token that is the first
                 // on the line, as it may be the only thing on the
                 // line.  If it were not quoted in that case,
                 // an empty line has no tokens.
                 quote = true;
             }
             if(newLine)
             {
                 newLine = false;
             }
             else
             {
                 out.write(delimiterChar);
             }
             if(quote)
             {
                 out.write(escapeAndQuote(value));
             }
             else
             {
                 out.write(value);
             }
             if(autoFlush) flush();
         }
         catch(IOException iox)
         {
             error = true;
             throw iox;
         }
     }
 
     /**
      * Enclose the value in quotes and escape the quote
      * and comma characters that are inside.
      *
      * @param value needs to be escaped and quoted
      *
      * @return the value, escaped and quoted.
      */
     private String escapeAndQuote(String value)
     {
         int count = 2;
         for(int i = 0; i < value.length(); i++)
         {
             char c = value.charAt(i);
             switch(c)
             {
                 case '\n':
                 case '\r':
                 case '\\':
                     {
                         count++;
                     }
                     break;
                 default:
                     {
                         if(c == quoteChar)
                         {
                             count++;
                         }
                     }
                     break;
             }
         }
         StringBuffer sb = new StringBuffer(value.length() + count);
         sb.append(quoteChar);
         for(int i = 0; i < value.length(); i++)
         {
             char c = value.charAt(i);
             switch(c)
             {
                 case '\n':
                     {
                         sb.append("\\n");
                     }
                     break;
                 case '\r':
                     {
                         sb.append("\\r");
                     }
                     break;
                 case '\\':
                     {
                         sb.append("\\\\");
                     }
                     break;
                 default:
                     {
                         if(c == quoteChar)
                         {
                             sb.append("\\" + quoteChar);
                         }
                         else
                         {
                             sb.append(c);
                         }
                     }
             }
         }
         sb.append(quoteChar);
         return (sb.toString());
     }
 
     /**
      * Flush any data written out to underlying streams.
      */
     public void flush() throws IOException
     {
         out.flush();
     }
 
     /**
      * Close any underlying streams.
      */
     public void close() throws IOException
     {
         out.close();
     }
 
     /**
      * Flush the stream if it's not closed and check its error state.
      * Errors are cumulative; once the stream encounters an error,
      * this routine will return true on all successive calls.
      *
      * @return True if the print stream has encountered an error,
      *         either on the underlying output stream or during a format conversion.
      */
     public boolean checkError()
     {
         try
         {
             if(error) return true;
             flush();
             if(error) return true;
             if(out instanceof PrintWriter)
             {
                 error = ((PrintWriter) out).checkError();
             }
         }
         catch(IOException iox)
         {
             error = true;
         }
         return error;
     }
 
     /**
      * Set flushing behavior.  Iff set, a flush command
      * will be issued to any underlying stream after each
      * print or write command.
      *
      * @param autoFlush should auto flushing be enabled.
      */
     public void setAutoFlush(boolean autoFlush)
     {
         this.autoFlush = autoFlush;
     }
 
     /**
      * Set whether values printers should always be quoted, or
      * whether the printer may, at its discretion, omit quotes
      * around the value.
      *
      * @param alwaysQuote true if quotes should be used even when not strictly needed.
      */
     public void setAlwaysQuote(boolean alwaysQuote)
     {
         this.alwaysQuote = alwaysQuote;
     }
 }
