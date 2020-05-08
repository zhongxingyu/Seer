 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.designtime.internal.view.model.jsp.registry;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jst.jsf.common.internal.policy.OrderedListProvider;
 import org.eclipse.jst.jsf.common.internal.policy.OrderedListProvider.OrderableObject;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.DefaultJSPTagResolver;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TagIntrospectingStrategy;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.JSPTagResolvingStrategy.StrategyDescriptor;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.persistence.PersistedDataTagStrategy;
 
 /**
  * Preferences model for the TLD registry.  This class is not thread-safe and
  * a single instance should only be used by one owner.
  * 
  * @author cbateman
  * 
  */
 public class TLDRegistryPreferences
 {
     private final static Map<String, StrategyDescriptor> ALL_KNOWN_STRATEGIES;
 
     private final IPreferenceStore             _prefStore;
     private final CopyOnWriteArrayList<PropertyListener> _listeners;
     private final AtomicBoolean                _isDisposed = new AtomicBoolean(false);
 
     private final static String                KEY_STRATEGY_ID_ORDER = "org.eclipse.jst.jsf.designtime.jsp.registry.StrategyIDOrder"; //$NON-NLS-1$
 
     private final static List<OrderableObject> DEFAULT_STRATEGY_ORDER;
 
     static
     {
         final List<OrderableObject> list = new ArrayList<OrderableObject>();
 
         // NOTE !!! this ordering is important and effects the default order
         // in which strategies will be consulted !!!
         list.add(new OrderableObject(new StrategyIdentifier(PersistedDataTagStrategy.createDescriptor()), true));
         list.add(new OrderableObject(new StrategyIdentifier(DefaultJSPTagResolver.createDescriptor()), true));
        // Bug 312954 - temporarily disable the default preference for the
        // TagIntrospectingStrategy. When bug 312936 and 240394 get resolved,
        // roll back this change and re-enable.
        list.add(new OrderableObject(new StrategyIdentifier(TagIntrospectingStrategy.createDescriptor()), false));
         DEFAULT_STRATEGY_ORDER = Collections.unmodifiableList(list);
         
 
         final Map<String, StrategyDescriptor> knownDescriptors = new HashMap<String, StrategyDescriptor>();
         for (final OrderableObject object : DEFAULT_STRATEGY_ORDER)
         {
             StrategyIdentifier strategyId = (StrategyIdentifier) object.getObject();
             knownDescriptors.put(strategyId.getId(), strategyId._descriptor);
         }
         ALL_KNOWN_STRATEGIES = Collections.unmodifiableMap(knownDescriptors);
     }
 
     private List<OrderableObject>              _ids;
     private List<OrderableObject>              _originalIds;
     private IPropertyChangeListener            _propertyListener;
 
     /**
      * Constructor
      * 
      * @param prefStore
      */
     public TLDRegistryPreferences(final IPreferenceStore prefStore)
     {
         _prefStore = prefStore;
         _ids = new ArrayList<OrderableObject>();
         _listeners = new CopyOnWriteArrayList<PropertyListener>();
     }
 
     /**
      * Dispose of this preferences object
      */
     public void dispose()
     {
         if (_isDisposed.compareAndSet(false, true))
         {
             if (_propertyListener != null)
             {
                 _prefStore.removePropertyChangeListener(_propertyListener);
             }
         }
     }
 
     void addListener(final PropertyListener propListener)
     {
         if (!assertNotDisposed())
         {
             return;
         }
 
         if (_propertyListener == null)
         {
             _propertyListener = new IPropertyChangeListener()
             {
                 public void propertyChange(PropertyChangeEvent event)
                 {
                     if (KEY_STRATEGY_ID_ORDER.equals(event.getProperty()))
                     {
                         fireStrategyOrderChanged();
                     }
                 }
             };
             
             _prefStore.addPropertyChangeListener(_propertyListener);
         }
         _listeners.addIfAbsent(propListener);
     }
 
     void removeListener(final PropertyListener propListener)
     {
         if (!assertNotDisposed())
         {
             return;
         }
         _listeners.remove(propListener);
 
         if (_listeners.isEmpty())
         {
             _prefStore.removePropertyChangeListener(_propertyListener);
             _propertyListener = null;
         }
     }
 
     private void fireStrategyOrderChanged()
     {
         if (!assertNotDisposed())
         {
             return;
         }
         for (final PropertyListener listener : _listeners)
         {
             listener.strategyOrderChanged();
         }
     }
 
     /**
      * @return false if the assertion fails
      */
     private boolean assertNotDisposed()
     {
         if (_isDisposed.get())
         {
             JSFCorePlugin.log(new Exception("Stack trace only"), "TLDRegistryPreferences is disposed"); //$NON-NLS-1$ //$NON-NLS-2$
             return false;
         }
         return true;
     }
 
     /**
      * IPreferenceStore The default preference loader
      */
     public void load()
     {
         load(_prefStore);
     }
 
     /**
      * @return the ordered list provider for the strategy id ordering
      */
     public OrderedListProvider getOrderedListProvider()
     {
         return new MyOrderedListProvider();
     }
 
     /**
      * @return the strategy id ordering
      */
     public List<OrderableObject> getStrategyIdOrdering()
     {
         return _ids;
     }
 
     /**
      * @param ids
      */
     public void setStrategyIdOrdering(final List<OrderableObject> ids)
     {
         _ids = ids;
     }
 
     /**
      * @return the list of strategy ids in the order they should be consulted
      */
     public List<String> getEnabledIds()
     {
         final List<String> strategies = new ArrayList<String>();
 
         for (final OrderableObject id : _ids)
         {
             if (id.isEnabled())
             {
                 StrategyIdentifier strategyId = (StrategyIdentifier) id.getObject();
                 strategies.add(strategyId.getId());
             }
         }
         return strategies;
     }
 
     /**
      * Loads preferences from prefStore
      * 
      * @param prefStore
      */
     private void load(final IPreferenceStore prefStore)
     {
         if (!prefStore.contains(KEY_STRATEGY_ID_ORDER))
         {
             prefStore.setDefault(KEY_STRATEGY_ID_ORDER,
                     serialize(DEFAULT_STRATEGY_ORDER));
         }
         List<OrderableObject> ids = deserialize(prefStore
                 .getString(KEY_STRATEGY_ID_ORDER));
         if (ids == null)
         {
             ids = deserialize(serialize(DEFAULT_STRATEGY_ORDER));
         }
         _ids = ids;
         final List<OrderableObject> originalList = new ArrayList<OrderableObject>();
         for (final OrderableObject id : _ids)
         {
             final OrderableObject copy = id.clone();
             originalList.add(copy);
         }
         _originalIds = Collections.unmodifiableList(originalList);
     }
 
     private String serialize(final List<OrderableObject> ids)
     {
         final StringBuffer buffer = new StringBuffer();
 
         for (final OrderableObject id : ids)
         {
             StrategyIdentifier strategyId = (StrategyIdentifier) id.getObject();
             buffer.append("dummyValue"); //$NON-NLS-1$
             buffer.append(","); //$NON-NLS-1$
             buffer.append(strategyId.getId());
             buffer.append(","); //$NON-NLS-1$
             buffer.append(id.isEnabled());
             buffer.append(","); //$NON-NLS-1$
         }
         return buffer.toString();
     }
 
     private List<OrderableObject> deserialize(final String serializedList)
     {
         final List<OrderableObject> list = new ArrayList<OrderableObject>();
         final String[] ids = serializedList.split(","); //$NON-NLS-1$
         if ((ids.length % 3) != 0)
         {
             return null;
         }
 
         for (int i = 0; i < ids.length; i += 3)
         {
             /// ingore the dummy value: final String displayName = ids[i];
             String id = ids[i + 1];
             final String enabled = ids[i + 2];
 
             // fix old id for meta-data resolver
             if ("org.eclipse.jst.jsf.THISISTEMPORARY".equals(id)) //$NON-NLS-1$
             {
                 id = DefaultJSPTagResolver.ID;
             }
 
             final StrategyDescriptor desc = ALL_KNOWN_STRATEGIES.get(id);
             
             if (desc == null)
             {
                 JSFCorePlugin.log(new Exception("Stack trace only"), "Error: unknown strategy id: "+id); //$NON-NLS-1$ //$NON-NLS-2$
             }
             else
             {
                 final StrategyIdentifier strategyIdentifier = new StrategyIdentifier(desc);
                 list.add(new OrderableObject(strategyIdentifier
                         , Boolean.valueOf(enabled).booleanValue()));
             }
         }
         return list;
     }
 
     /**
      * Commits but does not store the preferences
      * 
      * @param prefStore
      */
     public void commit(final IPreferenceStore prefStore)
     {
         prefStore.setValue(KEY_STRATEGY_ID_ORDER,
                 serialize(getStrategyIdOrdering()));
         // refresh local copy of preferences
         load();
     }
 
     /**
      * Reverts the model to it's defaults. Does not commit to pref store.
      */
     public void setDefaults()
     {
          setStrategyIdOrdering(new ArrayList<OrderableObject>(
                 DEFAULT_STRATEGY_ORDER));
     }
 
     /**
      * @return true if this preference object's properties have
      * changed since load() was last called.
      */
     public boolean isDirty()
     {
         // dirty if the current list is not equal to the original list
         // generated at load time.
         return !(_ids.equals(_originalIds));
     }
 
     /**
      * Used as the model for sorting and enabling strategy identifiers.
      * 
      */
     public static class StrategyIdentifier
     {
         private final StrategyDescriptor _descriptor;
 
         StrategyIdentifier(final StrategyDescriptor descriptor)
         {
             _descriptor = descriptor;
         }
 
         /**
          * @return the id
          */
         public String getId()
         {
             return _descriptor.getId();
         }
 
         @Override
         public boolean equals(Object obj)
         {
             if (obj instanceof StrategyIdentifier)
             {
                 return getId().equals(((StrategyIdentifier)obj).getId());
             }
             return false;
         }
 
         @Override
         public int hashCode()
         {
             return getId().hashCode();
         }
 
         /**
          * @return the display name of the strategy
          */
         public String getDisplayName()
         {
             return _descriptor.getDisplayName();
         }
     }
 
     private class MyOrderedListProvider extends OrderedListProvider
     {
         @Override
         protected List<OrderableObject> createAndPopulateOrderedObjects()
         {
             return _ids;
         }
     }
 
     static abstract class PropertyListener
     {
         public abstract void strategyOrderChanged();
     }
 }
