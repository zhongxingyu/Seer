 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.mvnsearch.snippet.plugin.util;
 
 import com.intellij.openapi.util.text.StringUtil;
 import org.mvnsearch.snippet.impl.mvnsearch.SnippetService;
import org.mvnsearch.snippet.plugin.SnippetAppComponent;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 import java.util.List;
 
 /**
  * snippet mnenonic completer
  *
  * @author linux_china@hotmail.com
  */
 public class SnippetMnemonicCompleter extends AutoCompleter {
     public SnippetMnemonicCompleter(JTextComponent comp) {
         super(comp);
     }
 
     /**
      * update list model depending on the data in textfield
      *
      * @return data updated or not
      */
     protected boolean updateListData() {
         String value = textComp.getText();
         if (StringUtil.isNotEmpty(value)) {
             SnippetService service = SnippetAppComponent.getInstance().getSnippetService();
            //find file fragement
            List<String> mnemonicList = service.findMnemonicList(value, true);
             if (mnemonicList != null && mnemonicList.size() > 0) {
                 if (!(mnemonicList.size() == 1 && mnemonicList.get(0).equals(value))) {
                     list.setListData(mnemonicList.toArray());
                     return true;
                 }
             }
         }
         list.setListData(new String[0]);
         return true;
     }
 
     /**
      * user has selected some item in the list. update textfield accordingly
      *
      * @param selected item
      */
     protected void acceptedListItem(String selected) {
         if (selected == null)
             return;
         int prefixlen = textComp.getDocument().getLength();
         try {
             textComp.getDocument().insertString(textComp.getCaretPosition(), selected.substring(prefixlen), null);
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
     }
 }
