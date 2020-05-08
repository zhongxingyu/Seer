 /***********************************************************************************************
  * Copyright (c) Microsoft Corporation All rights reserved.
  * 
  * MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  ***********************************************************************************************/
 
 package com.microsoft.gittf.client.tfs.Library;
 
import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.DecimalFormat;
 import java.text.MessageFormat;
 import java.util.Calendar;
 
 import com.microsoft.gittf.client.tfs.TestEnvironmentConstants;
 
 /**
  * 
  * Helper to log output message to the console for test purposes.
  * 
  * @author jpresto
  * 
  */
 public class Logger
 {
     public static void logBreak()
     {
         log("-------------------------------------------------------------------------"); //$NON-NLS-1$
     }
 
     public static void log(String message)
     {
        PrintStream console = System.out;
 
         // update message with current time
         Calendar cal = Calendar.getInstance();
         DecimalFormat decimalFormat = new DecimalFormat("00"); //$NON-NLS-1$
         String timeStamp = MessageFormat.format("{0}:{1}:{2}", //$NON-NLS-1$
             decimalFormat.format(cal.get(Calendar.HOUR_OF_DAY)),
             decimalFormat.format(cal.get(Calendar.MINUTE)),
             decimalFormat.format(cal.get(Calendar.SECOND)));
 
         message = MessageFormat.format("{0}:  {1}", timeStamp, message); //$NON-NLS-1$
         if (console == null)
         {
             System.out.println(message);
         }
         else
         {
             console.printf(message);
         }
     }
 
     public static void logNewLine()
     {
         log(TestEnvironmentConstants.GetNewLine());
     }
 
     public static void logHeader(String header)
     {
         logBreak();
         log(header);
         logBreak();
     }
 
     public static void logException(Throwable e)
     {
         log(MessageFormat.format("Exception: '{0}'", e)); //$NON-NLS-1$
         StringWriter sw = new StringWriter(2000);
         PrintWriter pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         log(sw.getBuffer().toString());
     }
 
     public static void log(String header, String message)
     {
         if (header == null)
         {
             header = ""; //$NON-NLS-1$
         }
         if (message == null)
         {
             message = ""; //$NON-NLS-1$
         }
 
         logNewLine();
         logBreak();
         log(header);
         logBreak();
         log(message);
         logBreak();
         logNewLine();
     }
 }
