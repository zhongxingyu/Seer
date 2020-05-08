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
 package org.eclipse.babel.editor;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Observable;
 
 import org.eclipse.babel.core.message.MessagesBundleGroup;
 import org.eclipse.babel.core.util.BabelUtils;
 import org.eclipse.babel.editor.resource.validator.MessagesBundleGroupValidator;
 import org.eclipse.babel.editor.resource.validator.IValidationMarkerStrategy;
 import org.eclipse.babel.editor.resource.validator.ValidationFailureEvent;
 
 
 /**
  * @author Pascal Essiembre
  *
  */
 public class MessagesEditorMarkers
         extends Observable implements IValidationMarkerStrategy {
 
     private final Collection validationEvents = new ArrayList();
     private final Collection failedKeys = new ArrayList();
     private final MessagesBundleGroup messagesBundleGroup;
     
     /**
      * @param messagesBundleGroup
      */
     public MessagesEditorMarkers(final MessagesBundleGroup messagesBundleGroup) {
         super();
         this.messagesBundleGroup = messagesBundleGroup;
         validate();
         messagesBundleGroup.addPropertyChangeListener(new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent evt) {
                 clear();
                 validate();
             } 
         });
     }
 
     /**
      * @see org.eclipse.babel.editor.resource.validator.IValidationMarkerStrategy#markFailed(org.eclipse.core.resources.IResource, org.eclipse.babel.core.bundle.checks.IBundleEntryCheck)
      */
     public void markFailed(ValidationFailureEvent event) {
         validationEvents.add(event);
         failedKeys.add(event.getKey());
         System.out.println("CREATE EDITOR MARKER");
        setChanged();
        notifyObservers(this);
     }
 
     public void clear() {
         validationEvents.clear();
         failedKeys.clear();
        setChanged();
        notifyObservers(this);
     }
 
     public boolean isMarked(String key)  {
         return failedKeys.contains(key);
     }
 
     public Collection getFailedChecks(final String key) {
         return getFailedChecks(new IEventFilter() {
             public boolean filter(ValidationFailureEvent event) {
                 return BabelUtils.equals(event.getKey(), key);
             }
         });
     }
     public Collection getFailedChecks(final String key, final Locale locale) {
         return getFailedChecks(new IEventFilter() {
             public boolean filter(ValidationFailureEvent event) {
                 return BabelUtils.equals(event.getKey(), key)
                         && BabelUtils.equals(locale, event.getLocale());
             }
         });
     }
     
     
     private Collection getFailedChecks(IEventFilter filter) {
         Collection checks = new ArrayList();
         for (Iterator iter = validationEvents.iterator(); iter.hasNext();) {
             ValidationFailureEvent event = (ValidationFailureEvent) iter.next();
             if (filter.filter(event)) {
                 checks.add(event.getCheck());
             }
         }
         return checks;
     }
     private interface IEventFilter {
         boolean filter(ValidationFailureEvent event);
     }
     
     private void validate() {
         //TODO in a UI thread
         MessagesBundleGroupValidator.validate(messagesBundleGroup, this);
     }
 }
