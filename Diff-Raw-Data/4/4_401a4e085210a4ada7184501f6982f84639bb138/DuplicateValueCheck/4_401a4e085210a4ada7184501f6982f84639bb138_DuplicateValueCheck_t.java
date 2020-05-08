 /*******************************************************************************
  * Copyright (c) 2007 Pascal Essiembre.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Pascal Essiembre - initial API and implementation
  ******************************************************************************/
 package org.eclipse.babel.core.message.checks;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.babel.core.message.Message;
 import org.eclipse.babel.core.message.MessagesBundle;
 import org.eclipse.babel.core.message.MessagesBundleGroup;
import org.eclipse.babel.core.util.BabelUtils;
 
 /**
  * Checks if key as a duplicate value.
  * @author Pascal Essiembre (pascal@essiembre.com)
  */
 public class DuplicateValueCheck implements IMessageCheck {
 
     private String[] duplicateKeys;
     
     /**
      * Constructor.
      */
     public DuplicateValueCheck() {
         super();
     }
 
     public boolean checkKey(
             MessagesBundleGroup messagesBundleGroup, Message message) {
         Collection keys = new ArrayList();
         if (message != null) {
             MessagesBundle messagesBundle =
             		messagesBundleGroup.getMessagesBundle(message.getLocale());
             for (Iterator iter = messagesBundle.messageIterator();
             		iter.hasNext();) {
                 Message duplicateEntry = (Message) iter.next();
                 if (!message.getKey().equals(duplicateEntry.getKey())
                            && BabelUtils.equals(message.getValue(),
                                     duplicateEntry.getValue())) {
                     keys.add(duplicateEntry.getKey());
                 }
             }
             if (!keys.isEmpty()) {
                 keys.add(message.getKey());
             }
         }
 
         duplicateKeys = (String[]) keys.toArray(new String[]{});
         return !keys.isEmpty();
     }
     
     public String[] getDuplicateKeys() {
         return duplicateKeys;
     }
 }
