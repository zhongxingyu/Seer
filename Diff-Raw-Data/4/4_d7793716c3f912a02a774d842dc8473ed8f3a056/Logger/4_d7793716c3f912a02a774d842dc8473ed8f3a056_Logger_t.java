 /*
  * Copyright 2012 Alexey Hanin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package getrest.android.util;
 
 import android.util.Log;
 
 import java.text.MessageFormat;
 
 /**
  * @author aha
  * @since 2012-01-16
  */
 public final class Logger {
 
     private String tag;
 
     public Logger(final String tag) {
         this.tag = tag;
     }
 
     public void trace(String message, Object... arguments) {
         if (Log.isLoggable(tag, Log.VERBOSE)) {
             Log.v(tag, MessageFormat.format(message, arguments));
         }
     }
 
     public void trace(String message, Throwable throwable, Object... arguments) {
         if (Log.isLoggable(tag, Log.VERBOSE)) {
             Log.v(tag, MessageFormat.format(message, arguments), throwable);
         }
     }
 
     public void debug(String message, Object... arguments) {
         if (Log.isLoggable(tag, Log.DEBUG)) {
             Log.d(tag, MessageFormat.format(message, arguments));
         }
     }
 
     public void debug(String message, Throwable throwable, Object... arguments) {
         if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, MessageFormat.format(message, arguments), throwable);
         }
     }
 
     public void info(String message, Object... arguments) {
         if (Log.isLoggable(tag, Log.INFO)) {
             Log.i(tag, MessageFormat.format(message, arguments));
         }
     }
 
     public void info(String message, Throwable throwable, Object... arguments) {
         if (Log.isLoggable(tag, Log.INFO)) {
             Log.i(tag, MessageFormat.format(message, arguments), throwable);
         }
     }
 
     public void warn(String message, Object... arguments) {
         if (Log.isLoggable(tag, Log.WARN)) {
             Log.w(tag, MessageFormat.format(message, arguments));
         }
     }
 
     public void warn(String message, Throwable throwable, Object... arguments) {
         if (Log.isLoggable(tag, Log.WARN)) {
             if (arguments.length == 0) {
                 Log.w(tag, throwable);
             } else {
                 Log.w(tag, MessageFormat.format(message, arguments), throwable);
             }
         }
     }
 
     public void error(String message, Object... arguments) {
         if (Log.isLoggable(tag, Log.ERROR)) {
             Log.e(tag, MessageFormat.format(message, arguments));
         }
     }
 
     public void error(String message, Throwable throwable, Object... arguments) {
         if (Log.isLoggable(tag, Log.ERROR)) {
             Log.e(tag, MessageFormat.format(message, arguments), throwable);
         }
     }
 
 }
