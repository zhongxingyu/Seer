 /*
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
 
 /*
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
 
 package org.jledit.utils;
 
 
import java.awt.*;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 
 public final class ClipboardUtils {
 
     private ClipboardUtils() {
         //Utility Class
     }
 
     /**
      * Reads the String content of the {@link Clipboard}.
      * @return
      */
     public static String getContnet() {
         String result = "";
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         try {
             result = (String) clipboard.getData(DataFlavor.stringFlavor);
         } catch (Exception ex) {
             //noop
         }
         return result;
     }
 
     /**
      * Sets the content to the {@link Clipboard}.
      * @param content
      */
     public static void setContnent(String content) {
        String result = "";
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         try {
             StringSelection selection = new StringSelection( content );
             clipboard.setContents(selection, selection);
         } catch (Exception ex) {
             //noop
         }
     }
 }
