 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.businessprocess;
 
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IComponentNameReuser;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.xml.businessmodell.CompSystem;
 import org.eclipse.jubula.tools.xml.businessmodell.Component;
 
 
 
 /**
  * Abstract base class for IComponentNameMapper. Offers support for a cache
  * and a context.
  *
  * @author BREDEX GmbH
  * @created Feb 6, 2009
  */
 public abstract class AbstractComponentNameMapper implements
         IWritableComponentNameMapper {
 
     /** the cache for the Component Names */
     private IWritableComponentNameCache m_cache;
     
     /** the context within which the Component Names are mapped */
     private Object m_context;
 
     /**
      * Constructor
      * 
      * @param componentNameCache The cache for the Component Names.
      * @param context The context within which the Component Names are mapped.
      */
     public AbstractComponentNameMapper(
             IWritableComponentNameCache componentNameCache, Object context) {
         
         setCompNameCache(componentNameCache);
         setContext(context);
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public IWritableComponentNameCache getCompNameCache() {
         return m_cache;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setCompNameCache(
             IWritableComponentNameCache componentNameCache) {
         m_cache = componentNameCache;
     }
     
     /**
      * {@inheritDoc}
      */
     public void setContext(Object context) {
         m_context = context;
     }
 
     /**
      * 
      * @return the context for this mapper.
      */
     protected Object getContext() {
         return m_context;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public void changeReuse(
             IComponentNameReuser user, String oldGuid, String newGuid) 
         throws IncompatibleTypeException, PMException {
         
         if (user == null) {
             // null-safe
             return;
         }
         
         if (newGuid != null) {
             CompSystem compSystem = 
                 ComponentBuilder.getInstance().getCompSystem();
             List<Component> availableComponents = getFilterToolkitId() != null 
                 ? compSystem.getComponents(getFilterToolkitId(), true) 
                 : compSystem.getComponents();
             boolean isValidType = updateComponentType(
                     newGuid, user.getComponentType(
                             getCompNameCache(), availableComponents));
             if (!isValidType) {
                 String currType = 
                     getCompNameCache().getCompNamePo(newGuid)
                         .getComponentType();
                 
                 String userType = user.getComponentType(
                         getCompNameCache(), availableComponents);
                 IComponentNamePO compNamePo = 
                     getCompNameCache().getCompNamePo(newGuid);
                 // Component types are incompatible.
                 // Throw an exception with information about 
                 // the incompatibility.
                 StringBuilder msgBuid = new StringBuilder();
                 msgBuid.append(Messages.ErrorSavingChangedComponentName);
                 msgBuid.append(StringConstants.DOT);
                 msgBuid.append(StringConstants.NEWLINE);
                 msgBuid.append(StringConstants.APOSTROPHE);
                 msgBuid.append(currType);
                 msgBuid.append(StringConstants.APOSTROPHE);
                 msgBuid.append(StringConstants.SPACE);
                 msgBuid.append(StringConstants.MINUS);
                 msgBuid.append(StringConstants.RIGHT_INEQUALITY_SING);
                 msgBuid.append(StringConstants.SPACE);
                 msgBuid.append(StringConstants.APOSTROPHE);
                 msgBuid.append(userType);
                 msgBuid.append(StringConstants.APOSTROPHE);
                 msgBuid.append(StringConstants.EXCLAMATION_MARK);
                 String msg = msgBuid.toString();
                 throw new IncompatibleTypeException(
                         compNamePo, msg, 
                         MessageIDs.E_COMP_TYPE_INCOMPATIBLE, new String[]{
                                 compNamePo.getName(), 
                                CompSystemI18n.getString(currType, true), 
                                CompSystemI18n.getString(userType, true)});
             }
 
             getCompNameCache().addReuse(newGuid);
         }
 
         user.changeCompName(oldGuid, newGuid);
         
         if (oldGuid != null) {
             updateComponentType(oldGuid, null);
             getCompNameCache().removeReuse(oldGuid);
         }
     }
     
     /**
      * Updates the Component Type of the Component Name with the given GUID.
      * The Component Type will only be updated if the Component Name belongs
      * to the currently active project.
      * 
      * @param guid The GUID of the Component Name for which to update the
      *             Component Type.
      * @param typeOfReuse The new Component Type for which the Component Name 
      *                    is being used. May be <code>null</code> if the update
      *                    is caused by the removal of a reuse instance.
      * @return <code>true</code> if the update was successful, otherwise 
      *         <code>false</code> (if, for example, a Component Type 
      *         incompatibility was found).
      */
     private boolean updateComponentType(String guid, String typeOfReuse) {
         Set<String> usedTypes = getUsedTypes(guid);
         IComponentNamePO compNamePo = getCompNameCache().getCompNamePo(guid);
         
         if (typeOfReuse != null) {
             usedTypes.add(typeOfReuse);
         }
         final String newType = 
             ComponentNamesBP.getInstance().computeComponentType(
                     compNamePo.getName(), usedTypes);
         if (newType == null) {
             return false;
         }
 
         if (compNamePo.getParentProjectId() == null 
                 || compNamePo.getParentProjectId().equals(
                         GeneralStorage.getInstance().getProject().getId())) {
             compNamePo.setComponentType(newType);
         }
 
         if (newType.equals(ComponentNamesBP.UNKNOWN_COMPONENT_TYPE) 
                 && typeOfReuse != null) {
             return false;
         }
         
         return true;
     }
 
     /**
      * Provides an opportuninty to filter Component searches based on Toolkit.
      * All Component Type searches will use only Components from the Toolkit 
      * with the given ID and all "parents" (includes and depends).
      * Subclasses may override in order to allow such filtering.
      * 
      * @return the ID of the Toolkit on which all Component Type searches should
      *         be based, or <code>null</code> if no filtering should be used.
      */
     protected String getFilterToolkitId() {
         return null;
     }
 }
