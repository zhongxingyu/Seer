 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jem.internal.beaninfo.adapters;
 /*
  *  $RCSfile: BeaninfoClassAdapter.java,v $
 *  $Revision: 1.45 $  $Date: 2005/09/14 21:19:25 $ 
  */
 
 import java.io.FileNotFoundException;
 import java.lang.ref.WeakReference;
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.*;
 import org.eclipse.emf.ecore.*;
 import org.eclipse.emf.ecore.change.*;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.ESuperAdapter;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.*;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 
 import org.eclipse.jem.internal.beaninfo.*;
 import org.eclipse.jem.internal.beaninfo.common.*;
 import org.eclipse.jem.internal.beaninfo.core.*;
 import org.eclipse.jem.internal.beaninfo.core.BeanInfoCacheController.ClassEntry;
 import org.eclipse.jem.internal.java.beaninfo.IIntrospectionAdapter;
 import org.eclipse.jem.internal.proxy.core.*;
 import org.eclipse.jem.java.*;
 import org.eclipse.jem.java.impl.JavaClassImpl;
 import org.eclipse.jem.util.TimerTests;
 import org.eclipse.jem.util.logger.proxy.Logger;
 
 import com.ibm.etools.emf.event.EventFactory;
 import com.ibm.etools.emf.event.EventUtil;
 
 /**
  * Beaninfo adapter for doing introspection on a Java Model class.
  * <p>
  * The first time a introspect request is made, it will use the ClassEntry from the cache controller to determine if it should load from the
  * cache or do a complete introspection. After that it will always do a complete introspection when it is marked as needing introspection.
  * This is because the cache is useless to us then. At that point in time we already know that we or one of our superclasses or one of 
  * outer classes has changed, thereby requiring us to reintrospect, the cache is invalid at that point. Also once we did an introspection we
  * don't want to do a load from cache but instead do a merge because someone may of been holding onto the features and we don't want to
  * throw them away unnecessarily.
  * <p> 
  * TODO Need to re-look into this to see if we can do a merge with the cache file because if it was an external jar and it has now gone
  * valid, why waste time reintrospecting. But this needs to be carefully thought about. 
  * <p> 
  * The resource change listener
  * will automatically mark any subclasses (both the BeaninfoClassAdapter and the ClassEntry) as being stale for us. That way we don't need
  * to waste time checking the ce and superclasses everytime. We will keep the ClassEntry around simply to make the marking of it as stale easier for the
  * resource listener. Finding the ce everytime would be expensive.
  * <p> 
  * This is the process for determining if introspection required:
  * <ol>
  * <li>If needsIntrospection flag is false, then do nothing. Doesn't need introspection.
  * <li>If class is undefined, then just set up for an undefined class and return. (In this case the CE should be thrown away, it should of been deleted).
  * <li>If never introspected (not RETRIEVED_FULL_DOCUMENT), get the CE, get the modification stamp and call isStaleStamp. This determines if this
  * stamp or any super class stamp is stale wrt to current stamp. If it is not stale, then load from cache because the cache is good. 
  * <li>If no cache or cache is stale or has introspected once, then introspect and replace cache.
  * </ol>
  * 
  */
 
 public class BeaninfoClassAdapter extends AdapterImpl implements IIntrospectionAdapter {
 	
 	public static final String REFLECT_PROPERTIES = "Reflect properties";	// Reflect properties in IDE //$NON-NLS-1$
 	public static final String APPLY_EXTENSIONS = "Apply Overrides";	// Apply override files //$NON-NLS-1$
 	public static final String REMOTE_INTROSPECT = "Remote Introspect";	// Introspect on remote //$NON-NLS-1$
 	public static final String INTROSPECT = "Introspect";	// Straight introspection, whether load from cache or introspect. //$NON-NLS-1$
 	public static final String LOAD_FROM_CACHE = "Load from Cache"; //$NON-NLS-1$
 	
 	
 	public static BeaninfoClassAdapter getBeaninfoClassAdapter(EObject jc) {
 		return (BeaninfoClassAdapter) EcoreUtil.getExistingAdapter(jc, IIntrospectionAdapter.ADAPTER_KEY);
 	}
 	
 	/**
 	 * Clear out the introspection because introspection is being closed or removed from the project.
 	 * Don't want anything hanging around that we had done.
 	 */
 	public void clearIntrospection() {
 		// Clear out the beandecorator if implicitly created.
 		Iterator beanItr = getJavaClass().getEAnnotationsInternal().iterator();
 		while (beanItr.hasNext()) {
 			EAnnotation dec = (EAnnotation) beanItr.next();
 			if (dec instanceof BeanDecorator) {
 				BeanDecorator decor = (BeanDecorator) dec;
 				if (decor.getImplicitDecoratorFlag() == ImplicitItem.IMPLICIT_DECORATOR_LITERAL) {
 					beanItr.remove();
 					((InternalEObject) decor).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 				} else {
 					BeanInfoDecoratorUtility.clear((BeanDecorator) dec);
 				}
 				break;
 			}
 		}
 		// Clear out the features that we implicitly created.
 		Iterator propItr = getJavaClass().getEStructuralFeaturesInternal().iterator();
 		while (propItr.hasNext()) {
 			EStructuralFeature prop = (EStructuralFeature) propItr.next();
 			Iterator pdItr = prop.getEAnnotations().iterator();
 			while (pdItr.hasNext()) {
 				EAnnotation dec = (EAnnotation) pdItr.next();
 				if (dec instanceof PropertyDecorator) {
 					PropertyDecorator pd = (PropertyDecorator) dec;
 					if (pd.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL)
 						BeanInfoDecoratorUtility.clear(pd);
 					else {
 						pdItr.remove(); // Remove it from the property.
 						((InternalEObject) pd).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 					}
 					if (pd.getImplicitDecoratorFlag() == ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL) {
 						propItr.remove(); // Remove the feature itself
 						((InternalEObject) prop).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 					}
 					break;
 				}
 			}
 		}
 		
 		// Clear out the operations that we implicitly created.
 		Iterator operItr = getJavaClass().getEOperationsInternal().iterator();
 		while (operItr.hasNext()) {
 			EOperation oper = (EOperation) operItr.next();
 			Iterator mdItr = oper.getEAnnotations().iterator();
 			while (mdItr.hasNext()) {
 				EAnnotation dec = (EAnnotation) mdItr.next();
 				if (dec instanceof MethodDecorator) {
 					MethodDecorator md = (MethodDecorator) dec;
 					if (md.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL)
 						BeanInfoDecoratorUtility.clear(md);
 					else {
 						mdItr.remove(); // Remove it from the operation.
 						((InternalEObject) md).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 					}
 					if (md.getImplicitDecoratorFlag() == ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL) {
 						operItr.remove(); // Remove the oepration itself
 						((InternalEObject) oper).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 					}
 					break;
 				}
 			}
 			
 			// Clear out the events that we implicitly created.
 			Iterator evtItr = getJavaClass().getEventsGen().iterator();
 			while (evtItr.hasNext()) {
 				JavaEvent evt = (JavaEvent) evtItr.next();
 				Iterator edItr = evt.getEAnnotations().iterator();
 				while (edItr.hasNext()) {
 					EAnnotation dec = (EAnnotation) edItr.next();
 					if (dec instanceof EventSetDecorator) {
 						EventSetDecorator ed = (EventSetDecorator) dec;
 						if (ed.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL)
 							BeanInfoDecoratorUtility.clear(ed);
 						else {
 							edItr.remove(); // Remove it from the event.
 							((InternalEObject) ed).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						}
 						if (ed.getImplicitDecoratorFlag() == ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL) {
 							evtItr.remove(); // Remove the event itself
 							((InternalEObject) evt).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						}
 						break;
 					}
 				}
 			}				
 		}
 
 		synchronized(this) {
 			needsIntrospection = true;
 		}
 	}
 	
 	private void clearAll() {
 		clearIntrospection();	// First get rid of the ones we did so that they are marked as proxies.
 		
 		// Clear out the annotations.
 		getJavaClass().getEAnnotationsInternal().clear();
 		// Clear out the attributes.
 		getJavaClass().getEStructuralFeaturesInternal().clear();	
 		// Clear out the operations.
 		getJavaClass().getEOperationsInternal().clear();
 		// Clear out the events.
 		getJavaClass().getEventsGen().clear();
 		
 		retrievedExtensionDocument = NEVER_RETRIEVED_EXTENSION_DOCUMENT;	// Since we cleared everything, go back to no doc applied.
 	}
 	
 
 	/**
 	 * @version 	1.0
 	 * @author
 	 */
 
 	// A URI that will never resolve. Used to mark an object as no longer valid.
 	protected static final URI BAD_URI = URI.createURI("baduri"); //$NON-NLS-1$
 
 	protected boolean needsIntrospection = true;
 	
 	protected BeanInfoCacheController.ClassEntry classEntry;
 	
 	protected boolean isIntrospecting;
 
 	protected boolean isDoingAllProperties;
 
 	protected boolean isDoingAllOperations;
 
 	protected boolean isDoingAllEvents;
 
 	protected final static int
 		NEVER_RETRIEVED_EXTENSION_DOCUMENT = 0,
 		RETRIEVED_ROOT_ONLY = 1,
 		RETRIEVED_FULL_DOCUMENT = 2,
 		CLEAR_EXTENSIONS = 3;
 	protected int retrievedExtensionDocument = NEVER_RETRIEVED_EXTENSION_DOCUMENT;
 
 	protected BeaninfoAdapterFactory adapterFactory;
 
 	private WeakReference staleFactory; // When reference not null, then this factory is a stale factory and 
 	// a new one is needed when the factory returned == this one.
 	// It is a WeakRef so that if the factory goes away on its own
 	// that we don't hold onto it.
 
 	// A temporary hashset of the local properties. Used when creating a new
 	// property to use the old one. It is cleared out at the end of attribute introspection.
 	// It is only built once during attribute introspection so that we have the snapshot
 	// of before the introspection (i.e. the ones that were specifically added by users
 	// and not through introspection). The ones that we are adding do not need to be checked
 	// against each other because there will not be conflicts.
 	//
 	// Also at the end, we will go through the properties and see if it exists in the fPropertiesMap and
 	// it is not the value Boolean.FALSE in the map, and the entry is implicitly created. This means this
 	// was an implicit entry from the previous introspection and was not re-created in this introspection.
 	private HashMap propertiesMap;
 	private List featuresRealList; // Temp pointer to the real list we are building. It is the true list in java class.
 	
 	// A temporary hashmap of the local operations. Used when creating a new
 	// operation to reuse the old one. It is cleared out at the end of operation introspection.
 	// It is only built once during operation introspection so that we have the snapshot
 	// of before the introspection.
 	private HashMap operationsMap;
 	private EList operationsRealList; // Temp pointer to the real list we are building. It is the true list in java class.
 	// A set of operations as we create them so that we which ones we added/kept and which are no longer in use and can be removed.	
 	// If they aren't in this set at the end, then we know it should be removed if it is one we had created in the past.
 	private HashSet newoperations; 
 
 	// A temporary hashset of the local events. Used when creating a new
 	// event to use the old one. It is cleared out at the end of event introspection.
 	// It is only built once during event introspection so that we have the snapshot
 	// of before the introspection (i.e. the ones that were specifically added by users
 	// and not through introspection). The ones that we are adding do not need to be checked
 	// against each other because there will not be conflicts.
 	//
 	// Also at the end, we will go through the events and see if it exists in the fEventsMap and
 	// it is not the value Boolean.FALSE in the map, and the entry is implicitly created. This means this
 	// was an implicit entry from the previous introspection and was not re-created in this introspection.
 	private HashMap eventsMap;
 	private EList eventsRealList; // Temp pointer to the real list we are building. It is the true list in java class.
 
 	private Boolean defaultBound;
 	// Whether this class is default bound or not (i.e. does this class implement add/remove property change listener. If null, then not yet queried.
 
 	public BeaninfoClassAdapter(BeaninfoAdapterFactory factory) {
 		super();
 		adapterFactory = factory;
 	}
 
 	/*
 	 * Answer whether this java class is still connected to a live resource. It happens during unloading
 	 * that we are no longer connected to a resource (because the javapackage has already been processed and
 	 * unloaded) or the resource is in the process of being unloaded, but the unloading process will still 
 	 * call accessors on this java class, which will come over here. In those cases we should treat as
 	 * introspection completed without doing anything.
 	 */
 	protected boolean isResourceConnected() {
 		Resource res = getJavaClass().eResource();
 		return res != null && res.isLoaded();
 	}
 
 	protected final ProxyFactoryRegistry getRegistry() {
 		ProxyFactoryRegistry factory = adapterFactory.getRegistry();
 		if (staleFactory != null && factory == staleFactory.get()) {
 			// We need to recycle the factory. The returned factory is the same factory when it went stale.
 			factory = adapterFactory.recycleRegistry();
 		}
 		staleFactory = null; // Whether we recycled or not, it is no longer stale.
 		return factory;
 	}
 
 	/**
 	 * Return whether this adapter has been marked as stale. Needed
 	 * by the users so that they can recycle if necessary.
 	 */
 	public boolean isStale() {
 		return staleFactory != null;
 	}
 
 	protected BeaninfoAdapterFactory getAdapterFactory() {
 		return adapterFactory;
 	}
 
 	public boolean isAdapterForType(Object key) {
 		return IIntrospectionAdapter.ADAPTER_KEY.equals(key);
 	}
 
 	/**
 	 * This map is keyed by name. It is a snapshot of the properties at the
 	 * time the introspection/reflection of properties was started. It is used
 	 * for quick lookup.
 	 *
 	 * Once a property is used, the entry is replaced with a Boolean.FALSE. This
 	 * is so we know which have already been used and at the end, which need
 	 * to be deleted since they weren't used (i.e. the ones that aren't FALSE).
 	 */
 	protected HashMap getPropertiesMap() {
 		if (propertiesMap == null) {
 			List localFeatures = getJavaClass().getEStructuralFeaturesInternal();
 			propertiesMap = new HashMap(localFeatures.size());
 			Iterator itr = localFeatures.iterator();
 			while (itr.hasNext()) {
 				EStructuralFeature feature = (EStructuralFeature) itr.next();
 				propertiesMap.put(feature.getName(), feature);
 			}
 		}
 		return propertiesMap;
 	}
 
 	/**
 	 * Get it once so that we don't need to keep getting it over and over.
 	 */
 	protected List getFeaturesList() {
 		if (featuresRealList == null)
 			featuresRealList = getJavaClass().getEStructuralFeaturesInternal();
 		return featuresRealList;
 	}
 
 	/**
 	 * The map is keyed by longName. If a Method is passed in, then the
 	 * id of the method is used (this is in reflection), if an IBeanProxy
 	 * is passed in, then an id is created and looked up (this is in introspection).
 	 * The map is used for a quick lookup of behaviors at the time introspection
 	 * of behaviors started.
 	 */
 	protected HashMap getOperationsMap() {
 		if (operationsMap == null) {
 			List locals = getJavaClass().getEOperationsInternal();
 			int l = locals.size();
 			operationsMap = new HashMap(l);
 			for (int i = 0; i < l; i++) {
 				EOperation op = (EOperation) locals.get(i);
 				operationsMap.put(formLongName(op), op);
 			}
 		}
 		return operationsMap;
 	}
 
 	/**
 	 * Get it once so that we don't need to keep getting it over and over.
 	 */
 	protected EList getOperationsList() {
 		if (operationsRealList == null)
 			operationsRealList = getJavaClass().getEOperationsInternal();
 		return operationsRealList;
 	}
 
 	/**
 	 * The map is keyed by name.
 	 * The map is used for a quick lookup of events at the time introspection
 	 * of events started.
 	 *
 	 * Once an event is used, the entry is replaced with a Boolean.FALSE. This
 	 * is so we know which have already been used and at the end, which need
 	 * to be deleted since they weren't used (i.e. the ones that aren't FALSE).
 	 */
 	protected HashMap getEventsMap() {
 		if (eventsMap == null) {
 			List locals = getJavaClass().getEventsGen();
 			eventsMap = new HashMap(locals.size());
 			Iterator itr = locals.iterator();
 			while (itr.hasNext()) {
 				JavaEvent event = (JavaEvent) itr.next();
 				eventsMap.put(event.getName(), event);
 			}
 		}
 		return eventsMap;
 	}
 
 	/**
 	 * Get it once so that we don't need to keep getting it over and over.
 	 */
 	protected EList getEventsList() {
 		if (eventsRealList == null)
 			eventsRealList = getJavaClass().getEventsGen();
 		return eventsRealList;
 	}
 
 	public void introspectIfNecessary() {
 		introspectIfNecessary(false);
 	}
 	
 	protected void introspectIfNecessary(boolean doOperations) {
 		boolean doIntrospection = false;
 		synchronized (this) {
 			doIntrospection = needsIntrospection && !isIntrospecting; 
 			if (doIntrospection)
 				isIntrospecting = true;
 		}
 		if (doIntrospection) {
 			boolean didIntrospection = false;
 			try {				
 				introspect(doOperations);
 				didIntrospection = true;
 			} catch (Throwable e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(
 					new Status(
 						IStatus.WARNING,
 						BeaninfoPlugin.getPlugin().getBundle().getSymbolicName(),
 						0,
 						MessageFormat.format(
 							BeanInfoAdapterMessages.INTROSPECT_FAILED_EXC_, 
 							new Object[] { getJavaClass().getJavaName(), ""}), //$NON-NLS-1$
 						e));
 			} finally {
 				synchronized (this) {
 					isIntrospecting = false;
 					needsIntrospection = !didIntrospection;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get the class entry. 
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	public BeanInfoCacheController.ClassEntry getClassEntry() {
 		return classEntry;
 	}
 	
 	private boolean canUseCache() {
 		// We check our level, we assume not stale unless CE says stale.
 		synchronized (this) {
 			// We may already have a class entry due to a subclass doing a check, so if we do, we'll use it. Else we'll get the latest one.
 			if (classEntry == null)
 				classEntry = BeanInfoCacheController.INSTANCE.getClassEntry(getJavaClass());
 			if (classEntry != null) {
 				// We have a cache to check.
 				long modStamp = classEntry.getModificationStamp();
 				// A sanity check, if this was an old, but now deleted one we want to throw it away and get it again. It may now be valid.
 				if (modStamp == BeanInfoCacheController.ClassEntry.DELETED_MODIFICATION_STAMP) {
 					classEntry = BeanInfoCacheController.INSTANCE.getClassEntry(getJavaClass());
 					if (classEntry != null)
 						modStamp = classEntry.getModificationStamp();
 				}
 				if (modStamp == IResource.NULL_STAMP || modStamp == BeanInfoCacheController.ClassEntry.DELETED_MODIFICATION_STAMP)
 					return false;	// We are stale.
 			} else
 				return false;	// We don't have a cache entry to check against, which means are deleted, or never cached.
 		}
 		
 		// Now try the supers to see if we are out of date to them.
 		// Note: Only if this is an interface could there be more than one eSuperType.
 		List supers = getJavaClass().getESuperTypes();
 		if (!supers.isEmpty()) {
 			BeaninfoClassAdapter bca = getBeaninfoClassAdapter((EObject) supers.get(0));
 			ClassEntry superCE = bca.getClassEntry();
 			// If super is defined and out of date don't use cache. If super is undefined and super modification stamp is not super_undefined,
 			// then that means it was defined at time of cache and now not defined, so don't use cache.
 			if (superCE != null) {
 				if (superCE.getModificationStamp() != classEntry.getSuperModificationStamp())
 					return false; // Out-of-date wrt/super.
 			} else if (classEntry.getSuperModificationStamp() != ClassEntry.SUPER_UNDEFINED_MODIFICATION_STAMP)
 				return false;	// was previously defined, and now undefined, so out of date.
 			String[] iNames = classEntry.getInterfaceNames();
 			if (iNames != null) {
 				if (iNames.length != supers.size() - 1)
 					return false; // We have a different number of supers, so stale.
 				// Now the interfaces may not be in the same order, but there shouldn't be too many, so we'll use O(n2) lookup. We'll try starting at
 				// the same index just in case. That way if they are the same order it will be linear instead. Most likely will be same order.
 				long[] iStamp = classEntry.getInterfaceModificationStamps();
 				for (int i = 1; i <= iNames.length; i++) {
 					JavaClass javaClass = (JavaClass) supers.get(i);
 					String intName = (javaClass).getQualifiedNameForReflection();
 					// Find it in the names list.
 					int stop = i-1;
 					int indx = stop;	// Start at the stop, when we hit it again we will be done.
 					boolean found = false;
 					do {
 						if (iNames[indx].equals(intName)) {
 							found = true;
 							break;
 						}
 					} while ((++indx)%iNames.length != stop);
 					if (!found)
 						return false;	// Couldn't find it, so we are stale.]
 					
 					superCE = getBeaninfoClassAdapter(javaClass).getClassEntry();
 					if (superCE != null) {
 						if (superCE.getModificationStamp() != iStamp[indx])
 							return false; // Out-of-date wrt/interface.
 					} else if (iStamp[indx] != ClassEntry.SUPER_UNDEFINED_MODIFICATION_STAMP)
 						return false;	// was previously defined, and now undefined, so out of date.
 				}
 			}
 		}
 		return true;	// If we got here everything is ok.
 	}
 
 	private boolean canUseOverrideCache() {
 		// We check our config stamp.
 		// TODO in future can we listen for config changes and mark classes stale if the config changes?
 		synchronized (this) {
 			// We may already have a class entry due to a subclass doing a check, so if we do, we'll use it. Else we'll get the latest one.
 			if (classEntry == null)
 				classEntry = BeanInfoCacheController.INSTANCE.getClassEntry(getJavaClass());
 			if (classEntry != null) {
 				// We have a cache to check.
 				// A sanity check, if this was an old, but now deleted one we want to throw it away and get it again. It may now be valid.
 				if (classEntry.isDeleted()) {
 					classEntry = BeanInfoCacheController.INSTANCE.getClassEntry(getJavaClass());
 					if (classEntry != null)
 						if (classEntry.isDeleted())
 							return false;	// We have been deleted. Probably shouldn't of gotton here in this case.
 				}
 			} else
 				return false;	// We don't have a cache entry to check against, which means are deleted, or never cached.
 			return classEntry.getConfigurationModificationStamp() == Platform.getPlatformAdmin().getState(false).getTimeStamp();
 		}
 		
 	}
 
 	/**
 	 * Check if the cache for this entry is stale compared to the modification stamp, or if the modification stamp itself is stale, meaning
 	 * subclass was stale already.
 	 * 
 	 * @param requestStamp the timestamp to compare against. 
 	 * @return <code>true</code> if we can use the incoming cache stamp.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected boolean canUseCache(long requestStamp) {
 		long modStamp;
 		// Now get the current mod stamp for this class so we can compare it.
 		synchronized (this) {
 			// We may already have a class entry due to a subclass doing a check, so if we do, we'll use it. Else we'll get the latest one.
 			if (classEntry == null)
 				classEntry = BeanInfoCacheController.INSTANCE.getClassEntry(getJavaClass());
 			if (classEntry != null) {
 				// We have a cache to check.
 				modStamp = classEntry.getModificationStamp();
 				// A sanity check, if this was an old, but now deleted one we want to throw it away and get it again. It may now be valid.
 				if (modStamp == BeanInfoCacheController.ClassEntry.DELETED_MODIFICATION_STAMP) {
 					classEntry = BeanInfoCacheController.INSTANCE.getClassEntry(getJavaClass());
 					if (classEntry != null)
 						modStamp = classEntry.getModificationStamp();
 				}
 				if (modStamp == IResource.NULL_STAMP && modStamp == BeanInfoCacheController.ClassEntry.DELETED_MODIFICATION_STAMP)
 					return false;	// Since we are stale, child asking question must also be stale and can't use the cache.
 			} else
 				return false;	// We don't have a cache entry to check against, so child must be stale too.
 		}
 		// If the requested stamp is not the same as ours, then it is out of date (it couldn't be newer but it could be older).
 		return requestStamp == modStamp;
 	}
 		
 	/*
 	 * This should only be called through introspectIfNecessary so that flags are set correctly.
 	 */
 	private void introspect(boolean doOperations) {
 		IBeanProxy beaninfo = null;
 		try {
 			if (isResourceConnected()) {
 				// See if are valid kind of class.
 				if (getJavaClass().getKind() == TypeKind.UNDEFINED_LITERAL) {
 					// Not valid, don't let any further introspection occur.
 					// Mark that we've done all introspections so as not to waste time until we get notified that it has been added
 					// back in.
 					synchronized(this) {
 						if (classEntry != null) {
 							classEntry.markDeleted();	// mark it deleted in case still sitting in cache (maybe because it was in an external jar, we can't know if deleted when jar changed).
 							classEntry = null;	// Get rid of it since now deleted.
 						}
 						needsIntrospection = false;
 					}
 					
 					if (retrievedExtensionDocument == RETRIEVED_FULL_DOCUMENT || retrievedExtensionDocument == CLEAR_EXTENSIONS) {
 						// We've been defined at one point. Need to clear everything and step back
 						// to never retrieved so that we now get the root added in. If we had been
 						// previously defined, then we didn't have root. We will have to lose
 						// all other updates too. But they can come back when we're defined.
 						// Or we've been asked to clear all.
 						clearAll();
 						retrievedExtensionDocument = NEVER_RETRIEVED_EXTENSION_DOCUMENT;
 					}
 					if (retrievedExtensionDocument == NEVER_RETRIEVED_EXTENSION_DOCUMENT)
 						applyExtensionDocument(true);	// Add in Root stuff so that it will work correctly even though undefined.
 				} else {
 					// TODO For now cause recycle of vm if any super type is stale so that if registry is stale for the super type it will be
 					// recreated. This is needed because reflection requires superProperties, etc. and recreating the registry
 					// currently re-marks everyone as stale, including this subclass. This would mean that
 					// re-introspection would need to be done, even though we just did it. The stale registry business needs to be re-addressed so that it is
 					// a lot smarter. 
 					List supers = getJavaClass().getEAllSuperTypes();
 					for (int i = 0; i < supers.size(); i++) {
 						BeaninfoClassAdapter bca = (BeaninfoClassAdapter) EcoreUtil.getExistingAdapter((EObject) supers.get(i),
 								IIntrospectionAdapter.ADAPTER_KEY);
 						if (bca != null && bca.isStale())
 							bca.getRegistry();
 					}
 					// Now we need to force introspection, if needed, of all parents because we need them to be up-to-date for the
 					// class entry.
 					// TODO see if we can come up with a better way that we KNOW the CE is correct, even if any supers are stale.
 					supers = getJavaClass().getESuperTypes();
 					for (int i = 0; i < supers.size(); i++) {
 						BeaninfoClassAdapter bca = (BeaninfoClassAdapter) EcoreUtil.getRegisteredAdapter((EObject) supers.get(i),
 								IIntrospectionAdapter.ADAPTER_KEY);
 						bca.introspectIfNecessary();
 					}
 
 					TimerTests.basicTest.startCumulativeStep(INTROSPECT);
 					if (retrievedExtensionDocument == RETRIEVED_ROOT_ONLY || retrievedExtensionDocument == CLEAR_EXTENSIONS) {
 						// We need to clear out EVERYTHING because we are coming from an undefined to a defined.
 						// Nothing previous is now valid. (Particularly the root stuff).
 						// Or we had a Clean requested and need to clear the extensions too.
 						clearAll();
 					}
 					boolean firstTime = false;
 					if (retrievedExtensionDocument != RETRIEVED_FULL_DOCUMENT) {
 						firstTime = true;	// If we need to apply the extension doc, then this is the first time.
 						applyExtensionDocument(false); // Apply the extension doc before we do anything.
 					}
 
 					// Now check to see if we can use the cache.
 					boolean doIntrospection = true;
 					if (firstTime) {
 						// Check if we can use the cache. Use Max value so that first level test (ourself) will always pass and go on to the supers.
 						if (canUseCache()) {
 							TimerTests.basicTest.startCumulativeStep(LOAD_FROM_CACHE);
 							// We can use the cache.
 							Resource cres = BeanInfoCacheController.INSTANCE.getCache(getJavaClass(), classEntry, true);
 							if (cres != null) {
 								try {
 									// Got a cache to use, now apply it.
 									for (Iterator cds = cres.getContents().iterator(); cds.hasNext();) {
 										ChangeDescription cacheCD = (ChangeDescription) cds.next();
 										cacheCD.apply();
 									}
 									// We need to walk through and create the appropriate ID's for events/actions/properties because by
 									// default from the change descriptions these don't get reflected back. And if some one doesn't
 									// use an ID to get them, they won't have an id set.
 									doIDs();
 									doIntrospection = false;
 								} catch (RuntimeException e) {
 									BeaninfoPlugin.getPlugin().getLogger().log(
 										MessageFormat.format(
 												BeanInfoAdapterMessages.INTROSPECT_FAILED_EXC_, 
 												new Object[] { getJavaClass().getJavaName(), ""}), //$NON-NLS-1$
 										Level.WARNING);
 									BeaninfoPlugin.getPlugin().getLogger().log(e);
 								} finally {
 									// Remove the cres since it is now invalid. The applies cause them to be invalid.
 									cres.getResourceSet().getResources().remove(cres);
 									if (doIntrospection) {
 										// Apply had failed. We don't know how far it went. We need to wipe out and reget EVERYTHING.
 										clearAll();
 										applyExtensionDocument(false); // Re-apply the extension doc before we do anything else.
 									}
 								}
 							}
 							TimerTests.basicTest.stopCumulativeStep(LOAD_FROM_CACHE);
 						}
 					}
 					
 					if (doIntrospection) {
 						// Finally we can get to handling ourselves.
 						TimerTests.basicTest.startCumulativeStep(REMOTE_INTROSPECT);
 						BeanDecorator decor = Utilities.getBeanDecorator(getJavaClass());
 						if (decor == null) {
 							decor = BeaninfoFactory.eINSTANCE.createBeanDecorator();
 							decor.setImplicitDecoratorFlag(ImplicitItem.IMPLICIT_DECORATOR_LITERAL);
 							getJavaClass().getEAnnotations().add(decor);
 						} else
 							BeanInfoDecoratorUtility.clear(decor);	// Clear out previous results.
 						
 						boolean doReflection = true;
 						if (doOperations)
 							newoperations = new HashSet(50);
 						if (decor.isDoBeaninfo()) {
 							int doFlags = 0;
 							if (decor == null || decor.isMergeIntrospection())
 								doFlags |= IBeanInfoIntrospectionConstants.DO_BEAN_DECOR;
 							if (decor == null || decor.isIntrospectEvents())
 								doFlags |= IBeanInfoIntrospectionConstants.DO_EVENTS;
 							if (decor == null || decor.isIntrospectProperties())
 								doFlags |= IBeanInfoIntrospectionConstants.DO_PROPERTIES;
 							if (doOperations && (decor == null || decor.isIntrospectMethods()))
 								doFlags |= IBeanInfoIntrospectionConstants.DO_METHODS;
 							
 							if (doFlags != 0) {
 								// There was something remote to do.
 								IBeanTypeProxy targetType = null;
 								ProxyFactoryRegistry registry = getRegistry();
 								if (registry != null && registry.isValid())
 									targetType = registry.getBeanTypeProxyFactory().getBeanTypeProxy(getJavaClass().getQualifiedNameForReflection());
 								if (targetType != null) {
 									if (targetType.getInitializationError() == null) {
 										// If an exception is thrown, treat this as no proxy, however log it because we
 										// shouldn't have exceptions during introspection, but if we do it should be logged
 										// so it can be corrected.
 										try {
 											beaninfo = getProxyConstants().getIntrospectProxy().invoke(null,
 													new IBeanProxy[] { targetType, getRegistry().getBeanProxyFactory().createBeanProxyWith(false), getRegistry().getBeanProxyFactory().createBeanProxyWith(doFlags)});
 										} catch (ThrowableProxy e) {
 											BeaninfoPlugin.getPlugin().getLogger().log(
 													new Status(IStatus.WARNING, BeaninfoPlugin.getPlugin().getBundle().getSymbolicName(), 0,
 															MessageFormat.format(BeanInfoAdapterMessages.INTROSPECT_FAILED_EXC_, new Object[] { //$NON-NLS-1$
 																	getJavaClass().getJavaName(), ""}), //$NON-NLS-1$
 																	e));
 										}
 									} else {
 										// The class itself couldn't be initialized. Just log it, but treat as no proxy.
 										BeaninfoPlugin.getPlugin().getLogger()
 										.log(
 												new Status(IStatus.WARNING, BeaninfoPlugin.getPlugin().getBundle().getSymbolicName(), 0,
 														MessageFormat.format(BeanInfoAdapterMessages.INTROSPECT_FAILED_EXC_, new Object[] { //$NON-NLS-1$
 																getJavaClass().getJavaName(), targetType.getInitializationError()}), null));
 									}
 								} else {
 									// The class itself could not be found. Just log it, but treat as no proxy.
 									BeaninfoPlugin.getPlugin().getLogger().log(
 											new Status(IStatus.INFO, BeaninfoPlugin.getPlugin().getBundle().getSymbolicName(), 0, MessageFormat.format(
 													BeanInfoAdapterMessages.INTROSPECT_FAILED_EXC_, new Object[] { 
 															getJavaClass().getJavaName(),
 															BeanInfoAdapterMessages.BeaninfoClassAdapter_ClassNotFound}), 
 															null));
 								}
 								
 								if (beaninfo != null) {
 									doReflection = false;	// We have a beaninfo, so we are doing introspection.
 									final BeanDecorator bdecor = decor;
 									// We have a beaninfo to process.
 									BeanInfoDecoratorUtility.introspect(beaninfo, new BeanInfoDecoratorUtility.IntrospectCallBack() {
 
 										/*; (non-Javadoc)
 										 * @see org.eclipse.jem.internal.beaninfo.adapters.BeanInfoDecoratorUtility.IntrospectCallBack#process(org.eclipse.jem.internal.beaninfo.common.BeanRecord)
 										 */
 										public BeanDecorator process(BeanRecord record) {
 											return bdecor;
 										}
 
 										/* (non-Javadoc)
 										 * @see org.eclipse.jem.internal.beaninfo.adapters.BeanInfoDecoratorUtility.IntrospectCallBack#process(org.eclipse.jem.internal.beaninfo.common.PropertyRecord)
 										 */
 										public PropertyDecorator process(PropertyRecord record) {
 											return calculateProperty(record, false);
 										}
 
 										/* (non-Javadoc)
 										 * @see org.eclipse.jem.internal.beaninfo.adapters.BeanInfoDecoratorUtility.IntrospectCallBack#process(org.eclipse.jem.internal.beaninfo.common.IndexedPropertyRecord)
 										 */
 										public PropertyDecorator process(IndexedPropertyRecord record) {
 											return calculateProperty(record, true);
 										}
 
 										/* (non-Javadoc)
 										 * @see org.eclipse.jem.internal.beaninfo.adapters.BeanInfoDecoratorUtility.IntrospectCallBack#process(org.eclipse.jem.internal.beaninfo.common.MethodRecord)
 										 */
 										public MethodDecorator process(MethodRecord record) {
 											return calculateOperation(record);
 										}
 
 										/* (non-Javadoc)
 										 * @see org.eclipse.jem.internal.beaninfo.adapters.BeanInfoDecoratorUtility.IntrospectCallBack#process(org.eclipse.jem.internal.beaninfo.common.EventSetRecord)
 										 */
 										public EventSetDecorator process(EventSetRecord record) {
 											return calculateEvent(record);
 										}
 									});
 								} 
 							}
 						}
 						
 						if (doReflection) {
 							// Need to do reflection stuff.
 							if (decor.isIntrospectProperties())
 								reflectProperties();
 							if (doOperations && decor.isIntrospectMethods())
 								reflectOperations();
 							if (decor.isIntrospectEvents())
 								reflectEvents();
 						}
 						ChangeDescription cd = ChangeFactory.eINSTANCE.createChangeDescription();						
 						BeanInfoDecoratorUtility.buildChange(cd, decor);
 						finalizeProperties(cd);
 						if (doOperations)
 							finalizeOperations(cd);
 						finalizeEvents(cd);
 
 						classEntry = BeanInfoCacheController.INSTANCE.newCache(getJavaClass(), cd, doOperations ? BeanInfoCacheController.REFLECTION_OPERATIONS_CACHE : BeanInfoCacheController.REFLECTION_CACHE);
 						TimerTests.basicTest.stopCumulativeStep(REMOTE_INTROSPECT); 
 					}
 					TimerTests.basicTest.stopCumulativeStep(INTROSPECT);
 				}
 				getAdapterFactory().registerIntrospection(getJavaClass().getQualifiedNameForReflection(), this);
 			}
 		} finally {
 			if (beaninfo != null) {
 				beaninfo.getProxyFactoryRegistry().releaseProxy(beaninfo); // Dispose of the beaninfo since we now have everything.
 			}
 			eventsMap = null; // Clear out the temp lists.
 			eventsRealList = null;
 			operationsMap = null; // Clear out the temp lists.
 			operationsRealList = null;
 			newoperations = null;
 			propertiesMap = null; // Get rid of accumulated map.
 			featuresRealList = null; // Release the real list.
 		}
 	}
 
 	private void doIDs() {
 		// Do properties.
 		if (getJavaClass().eIsSet(EcorePackage.eINSTANCE.getEClass_EStructuralFeatures())) {
 			List features = getFeaturesList();
 			int len = features.size();
 			for (int i = 0; i < len; i++) {
 				EStructuralFeature f = (EStructuralFeature) features.get(i);
 				PropertyDecorator pd = Utilities.getPropertyDecorator(f);
 				if (pd == null || !pd.isMergeIntrospection())
 					continue; // Not a property for us to give an ID to.
 				setPropertyID(f.getName(), f);
 			}
 		}
 		
 		// Do events.
 		if (getJavaClass().eIsSet(JavaRefPackage.eINSTANCE.getJavaClass_Events())) {
 			List events = getEventsList();
 			int len = events.size();
 			for (int i = 0; i < len; i++) {
 				BeanEvent e = (BeanEvent) events.get(i);
 				EventSetDecorator ed = Utilities.getEventSetDecorator(e);
 				if (ed == null || !ed.isMergeIntrospection())
 					continue; // Not an event for us to give an ID to.
 				setEventID(e.getName(), e);
 			}
 		}
 
 		// Do Operations.
 		if (getJavaClass().eIsSet(EcorePackage.eINSTANCE.getEClass_EOperations())) {
 			List ops = getOperationsList();
 			int len = ops.size();
 			for (int i = 0; i < len; i++) {
 				EOperation o = (EOperation) ops.get(i);
 				MethodDecorator md = Utilities.getMethodDecorator(o);
 				if (md == null || !md.isMergeIntrospection())
 					continue; // Not an event for us to give an ID to.
 				setMethodID(o.getName(), o);
 			}
 		}
 	}
 
 	private static final String ROOT_OVERRIDE = BeaninfoPlugin.ROOT+'.'+BeaninfoPlugin.OVERRIDE_EXTENSION;	 //$NON-NLS-1$
 	
 	protected void applyExtensionDocument(boolean rootOnly) {
 		try {
 			TimerTests.basicTest.startCumulativeStep(APPLY_EXTENSIONS);
 			boolean canUseCache = !rootOnly && canUseOverrideCache();
 			boolean alreadyRetrievedRoot = retrievedExtensionDocument == RETRIEVED_ROOT_ONLY;
 			retrievedExtensionDocument = rootOnly ? RETRIEVED_ROOT_ONLY : RETRIEVED_FULL_DOCUMENT;
 			JavaClass jc = getJavaClass();
 			Resource mergeIntoResource = jc.eResource();
 			ResourceSet rset = mergeIntoResource.getResourceSet();
 			String className = jc.getName();
 			if (canUseCache) {
 				// We can use the cache, see if we actually have one.
 				if (getClassEntry().overrideCacheExists()) {
 					// Get the cache and apply it before anything else.
 					Resource cacheRes = BeanInfoCacheController.INSTANCE.getCache(jc, getClassEntry(), false);
 					if (cacheRes != null) {
 						try {
 							new ExtensionDocApplies(null, rset, jc, null).run(cacheRes);
 						} catch (WrappedException e) {
 							BeaninfoPlugin.getPlugin().getLogger().log(
 									new Status(IStatus.WARNING, BeaninfoPlugin.PI_BEANINFO_PLUGINID, 0,
 											"Error processing file\"" + cacheRes.getURI() + "\"", e.exception())); //$NON-NLS-1$ //$NON-NLS-2$						
 						} finally {
 							cacheRes.getResourceSet().getResources().remove(cacheRes); // We don't need it around once we do the merge. Normal merge would of gotton rid of it, but in case of error we do it here.
 						}
 					} else
 						canUseCache = false; // Need to rebuild the cache.
 				}
 			}
 			List overrideCache = null;
 			if (!alreadyRetrievedRoot && (rootOnly || jc.getSupertype() == null)) {
 				// It is a root class. Need to merge in root stuff.
 				if (!canUseCache) {
 					overrideCache = createOverrideCache(overrideCache, getAdapterFactory().getProject(), BeaninfoPlugin.ROOT, ROOT_OVERRIDE, rset, jc);
 				}
 				applyExtensionDocTo(rset, jc, ROOT_OVERRIDE, BeaninfoPlugin.ROOT, BeaninfoPlugin.ROOT);
 				if (rootOnly)
 					return;
 			}
 
 			String baseOverridefile = className + '.' + BeaninfoPlugin.OVERRIDE_EXTENSION; // getName() returns inner classes with "$" notation, which is good. //$NON-NLS-1$
 			String packageName = jc.getJavaPackage().getPackageName();
 			if (!canUseCache) {
 				overrideCache = createOverrideCache(overrideCache, getAdapterFactory().getProject(), packageName, baseOverridefile, rset, jc);
 			}
 			applyExtensionDocTo(rset, jc, baseOverridefile, packageName, className);
 			
 			if (!canUseCache) {
 				// We have an override cache to store. If the cache is null, this will flag that there is no override cache for the current configuration. That way we won't bother trying again until config changes.
 				BeanInfoCacheController.INSTANCE.newCache(jc, overrideCache, BeanInfoCacheController.OVERRIDES_CACHE);
 			}
 			
 		} finally {
 			TimerTests.basicTest.stopCumulativeStep(APPLY_EXTENSIONS);	
 		}
 	}
 	
 	/*
 	 * Build up the fixed overrides into the cache, and apply as we gather them.
 	 * Return the cache or null if the cache is empty at the end.
 	 */
 	private List createOverrideCache(List cache, IProject project, String packageName, String overrideFile, ResourceSet rset, JavaClass mergeIntoJavaClass) {
 		// Now get the overrides paths
 		String[] paths = BeaninfoPlugin.getPlugin().getOverridePaths(project, packageName);
 		if (paths.length == 0)
 			return cache;
 		
 		// Now apply the overrides.
 		if (cache == null)
 			cache = new ArrayList();
 		BeaninfoPlugin.IOverrideRunnable runnable = new ExtensionDocApplies(overrideFile, rset, mergeIntoJavaClass, cache);
 		for (int i = 0; i < paths.length; i++) {
 			runnable.run(paths[i]);
 		}
 		return !cache.isEmpty() ? cache : null;
 	}
 	
 	private static final URI ROOT_URI = URI.createGenericURI(BeaninfoPlugin.ROOT_SCHEMA, BeaninfoPlugin.ROOT_OPAQUE, null);
 	private static final String ROOT_FRAGMENT = "//@root";
 	private static final Pattern FRAGMENT_SPLITTER = Pattern.compile("/");
 	
 	private class ExtensionDocApplies implements BeaninfoPlugin.IOverrideRunnable {
 		
 		private final String overrideFile;
 		private final ResourceSet rset;
 		private final JavaClass mergeIntoJavaClass;
 		private final List overridesCache;
 
 		public ExtensionDocApplies(String overrideFile, ResourceSet rset, JavaClass mergeIntoJavaClass, List overridesCache) {
 			this.overrideFile = overrideFile;
 			this.rset = rset;
 			this.mergeIntoJavaClass = mergeIntoJavaClass;
 			this.overridesCache = overridesCache;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.beaninfo.core.BeaninfoPlugin.IOverrideRunnable#run(java.lang.String)
 		 */
 		public void run(String overridePath) {
 			Resource overrideRes = null;
 			URI uri = URI.createURI(overridePath+overrideFile);
 			try {
 				overrideRes = rset.getResource(uri, true);
 				run(overrideRes);
 			} catch (WrappedException e) {
 				// FileNotFoundException is ok
 				if (!(e.exception() instanceof FileNotFoundException)) {
 					if (e.exception() instanceof CoreException
 						&& ((CoreException) e.exception()).getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
 						// This is ok. Means uri_mapping not set so couldn't find in Workspace, also ok.
 					} else {
 						BeaninfoPlugin.getPlugin().getLogger().log(new Status(IStatus.WARNING, BeaninfoPlugin.PI_BEANINFO_PLUGINID, 0, "Error loading file\"" + uri + "\"", e.exception())); //$NON-NLS-1$ //$NON-NLS-2$						
 					}
 				}
 				// In case it happened after creating resource but during load. Need to get rid of it in the finally.	
 				overrideRes = rset.getResource(uri, false);				
 			} catch (Exception e) {
 				// Couldn't load it for some reason.
 				BeaninfoPlugin.getPlugin().getLogger().log(new Status(IStatus.WARNING, BeaninfoPlugin.PI_BEANINFO_PLUGINID, 0, "Error loading file\"" + uri + "\"", e)); //$NON-NLS-1$ //$NON-NLS-2$
 				overrideRes = rset.getResource(uri, false); // In case it happened after creating resource but during load so that we can get rid of it.
 			} finally {
 				if (overrideRes != null)
 					rset.getResources().remove(overrideRes); // We don't need it around once we do the merge. Normal merge would of gotton rid of it, but in case of error we do it here.
 			}
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.beaninfo.core.BeaninfoPlugin.IOverrideRunnable#run(org.eclipse.emf.ecore.resource.Resource)
 		 */
 		public void run(Resource overrideRes) {
 			try {
 				EList contents = overrideRes.getContents();
 				if (overridesCache != null) {
 					// TODO Until https://bugs.eclipse.org/bugs/show_bug.cgi?id=109169 is fixed, we need our own Copier.
 					EcoreUtil.Copier copier = new EcoreUtil.Copier() {
 
 						private static final long serialVersionUID = 1L;
 
 						protected void copyReference(EReference eReference, EObject eObject, EObject copyEObject) {
 							if (eObject.eIsSet(eReference)) {
 								if (eReference.isMany()) {
 									List source = (List) eObject.eGet(eReference);
 									InternalEList target = (InternalEList) copyEObject.eGet(getTarget(eReference));
 									if (source.isEmpty()) {
 										target.clear();
 									} else {
 										boolean isBidirectional = eReference.getEOpposite() != null;
 										int index = 0;
 										for (Iterator k = ((EcoreEList) source).basicIterator(); k.hasNext();) {
 											Object referencedEObject = k.next();
 											Object copyReferencedEObject = get(referencedEObject);
 											if (copyReferencedEObject == null) {
 												if (!isBidirectional) {
 													target.addUnique(index, referencedEObject);
 													++index;
 												}
 											} else {
 												if (isBidirectional) {
 													int position = target.indexOf(copyReferencedEObject);
 													if (position == -1) {
 														target.addUnique(index, copyReferencedEObject);
 													} else if (index != position) {
 														target.move(index, copyReferencedEObject);
 													}
 												} else {
 													target.addUnique(index, copyReferencedEObject);
 												}
 												++index;
 											}
 										}
 									}
 								} else {
 									Object referencedEObject = eObject.eGet(eReference, false);
 									if (referencedEObject == null) {
 										copyEObject.eSet(getTarget(eReference), null);
 									} else {
 										Object copyReferencedEObject = get(referencedEObject);
 										if (copyReferencedEObject == null) {
 											if (eReference.getEOpposite() == null) {
 												copyEObject.eSet(getTarget(eReference), referencedEObject);
 											}
 										} else {
 											copyEObject.eSet(getTarget(eReference), copyReferencedEObject);
 										}
 									}
 								}
 							}
 						}
 					};
 					
 					// We fixup the CD first so that when serialized it will be to the correct object already. Simplifies apply later.
 					Iterator itr = contents.iterator();
 					while (itr.hasNext()) {
 						Object o = itr.next();
 						if (o instanceof ChangeDescription) {
 							fixupCD((ChangeDescription) o);
 						}
 					}
 
 				    Collection result = copier.copyAll(contents);
 				    copier.copyReferences();
 					overridesCache.addAll(result);	// Make a copy for the override cache to be used next time needed.
 				}
 				
 				if (!contents.isEmpty()) {
 					// TODO It could be either the old event model or the new ChangeDescription. When we remove Event Model we need to remove
 					// the test. This could be the override cache too, so we must apply the contents in the order they are in the resource.
 					try {
 						List events = new ArrayList(1);
 						events.add(null);	// EventUtil needs a list, but we will be calling one by one. So just reuse this list.
 						Iterator itr = contents.iterator();
 						while (itr.hasNext()) {
 							Object o = itr.next();
 							if (o instanceof ChangeDescription) {
 								ChangeDescription cd = (ChangeDescription) o;
 								fixupCD(cd);
 								cd.apply();
 							} else {
 								// It is the old format.
 								events.set(0, o);
 								EventUtil util = EventFactory.eINSTANCE.createEventUtil(mergeIntoJavaClass, rset);
 								util.doForwardEvents(events);
 							}
 						}
 					} finally {
 						uninstallRootResource();
 					}						
 				}
 			} catch (WrappedException e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(new Status(IStatus.WARNING, BeaninfoPlugin.PI_BEANINFO_PLUGINID, 0, "Error processing file\"" + overrideRes.getURI() + "\"", e.exception())); //$NON-NLS-1$ //$NON-NLS-2$						
 			}
 		}
 		
 		/*
 		 * fix it up so that references to "X:ROOT#//@root" are replaced with reference to the javaclass.
 		 */
 		private void fixupCD(ChangeDescription cd) {
 			EStructuralFeature keyFeature = ChangePackage.eINSTANCE.getEObjectToChangesMapEntry_Key();
 			Iterator changes = cd.getObjectChanges().iterator();
 			while (changes.hasNext()) {
 				EObject entry = (EObject) changes.next();
 				EObject key = (EObject) entry.eGet(keyFeature, false);
 				if (key != null && key.eIsProxy()) {
 					// See if it is our special.
 					URI uri = ((InternalEObject) key).eProxyURI();
 					String rootFrag = uri.fragment();
 					if (BeaninfoPlugin.ROOT_SCHEMA.equals(uri.scheme()) && BeaninfoPlugin.ROOT_OPAQUE.equals(uri.opaquePart()) && (rootFrag != null && rootFrag.startsWith(ROOT_FRAGMENT))) {
 						// It is one of ours. Get the fragment, remove the leading //root and append to the end of the
 						// uri from the java class itself.
 						if (rootFrag.length() <= ROOT_FRAGMENT.length())
 							entry.eSet(keyFeature, mergeIntoJavaClass);	// No sub-path, so just the java class.
 						else {
 							// There is a sub-path below the java class that is needed. Need to walk down the path.
 							String[] path = FRAGMENT_SPLITTER.split(rootFrag.substring(ROOT_FRAGMENT.length()+1));
 							InternalEObject newKey = (InternalEObject) mergeIntoJavaClass;
 							for (int i = 0; newKey != null && i < path.length; i++) {
 								newKey = (InternalEObject) newKey.eObjectForURIFragmentSegment(path[i]);
 							}
 							if (newKey != null)
 								entry.eSet(keyFeature, newKey);
 						}
 					}
 				}
 			}
 		}
 		
 		/*
 		 * Uninstall the fake root resource. This must be called after installRootResource has been called. So must use try/finally. 
 		 * 
 		 * 
 		 * @since 1.2.0
 		 */
 		private void uninstallRootResource() {
 			Resource root = rset.getResource(ROOT_URI, false);
 			if (root != null)
 				rset.getResources().remove(root);
 		}
 	}
 	protected void applyExtensionDocTo(ResourceSet rset, JavaClass mergeIntoJavaClass, String overrideFile, String packageName, String className) {
 		BeaninfoPlugin.getPlugin().applyOverrides(getAdapterFactory().getProject(), packageName, className, mergeIntoJavaClass, rset, 
 			new ExtensionDocApplies(overrideFile, rset, mergeIntoJavaClass, null));
 	}
 
 	/**
 	 * Return the target as a JavaClass 
 	 */
 	protected JavaClassImpl getJavaClass() {
 		return (JavaClassImpl) getTarget();
 	}
 
 	/**
 	 * Answer the beaninfo constants record
 	 */
 	protected BeaninfoProxyConstants getProxyConstants() {
 		return BeaninfoProxyConstants.getConstants(getRegistry());
 	}
 
 	/**
 	 * @see org.eclipse.jem.java.beaninfo.IIntrospectionAdapter#getEStructuralFeatures()
 	 */
 	public EList getEStructuralFeatures() {
 		introspectIfNecessary();
 		return getJavaClass().getEStructuralFeaturesInternal();
 	}
 	
 	/**
 	 * @see org.eclipse.jem.java.beaninfo.IIntrospectionAdapter#getAllProperties()
 	 */
 	public EList getAllProperties() {
 		return allProperties();
 	}	
 	
 	/**
 	 * @see org.eclipse.jem.java.beaninfo.IIntrospectionAdapter#getEOperations()
 	 */
 	public EList getEOperations() {
 		if (getClassEntry() != null && getClassEntry().isOperationsStored())
 			introspectIfNecessary();	// Already stored, just do if necessary.
 		else {
 			synchronized (this) {
 				needsIntrospection = true;	// Force reintrospection because we either aren't storing operations, or have never loaded.
 			}
 			introspectIfNecessary(true);	// But force the operations now.
 		}
 		return getJavaClass().getEOperationsInternal();
 	}
 	
 	/**
 	 * @see org.eclipse.jem.java.beaninfo.IIntrospectionAdapter#getEAllOperations()
 	 */
 	public BasicEList getEAllOperations() {
 		return allOperations();
 	}
 	
 	/**
 	 * @see org.eclipse.jem.java.beaninfo.IIntrospectionAdapter#getEvents()
 	 */
 	public EList getEvents() {
 		introspectIfNecessary();
 		return getJavaClass().getEventsGen();
 	}
 	
 	/**
 	 * @see org.eclipse.jem.java.beaninfo.IIntrospectionAdapter#getAllEvents()
 	 */
 	public EList getAllEvents() {
 		return allEvents();
 	}					
 
 	private void finalizeProperties(ChangeDescription cd) {
 		// Now go through the list and remove those that should be removed, and set the etype for those that don't have it set.
 		Map oldLocals = getPropertiesMap();
 		Iterator itr = getFeaturesList().iterator();
 		while (itr.hasNext()) {
 			EStructuralFeature a = (EStructuralFeature) itr.next();
 			PropertyDecorator p = Utilities.getPropertyDecorator(a);
 			Object aOld = oldLocals.get(a.getName());
 			if (aOld != null && aOld != Boolean.FALSE) {
 				// A candidate for removal. It was in the old list and it was not processed.
 				if (p != null) {
 					ImplicitItem implicit = p.getImplicitDecoratorFlag();
 					if (implicit != ImplicitItem.NOT_IMPLICIT_LITERAL) {
 						p.setEModelElement(null); // Remove from the feature;
 						((InternalEObject) p).eSetProxyURI(BAD_URI);
 						// Mark it as bad proxy so we know it is no longer any use.
 						p = null;
 						if (implicit == ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL) {
 							itr.remove(); // Remove it, this was implicitly created and not processed this time.
 							((InternalEObject) a).eSetProxyURI(BAD_URI);	// Mark it as bad proxy so we know it is no longer any use.
 							continue;
 						}
 						// Need to go on because we need to check the eType to make sure it is set. At this point we have no decorator but we still have a feature.
 					}
 				}
 			}
 			
 			// [79083] Also check for eType not set, and if it is, set it to EObject type. That way it will be valid, but not valid as 
 			// a bean setting.
 			if (a.getEType() == null) {
 				// Set it to EObject type. If it becomes valid later (through the class being changed), then the introspect/reflect
 				// will set it to the correct type.
 				a.setEType(EcorePackage.eINSTANCE.getEObject());
 				Logger logger = BeaninfoPlugin.getPlugin().getLogger();
 				if (logger.isLoggingLevel(Level.WARNING))
 					logger.logWarning("Feature \""+getJavaClass().getQualifiedName()+"->"+a.getName()+"\" did not have a type set. Typically due to override file creating feature but property not found on introspection/reflection."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			
 			if (p != null && cd != null) {
 				// Now create the appropriate cache entry for this property.
 				BeanInfoDecoratorUtility.buildChange(cd, p);
 			}
 		}
 	}
 
 	/**
 	 * merge all  the Properties (i.e. supertypes) (properties)
 	 */
 	protected EList allProperties() {
 		
 		EList jcAllProperties = getJavaClass().getAllPropertiesGen();
 		BeaninfoSuperAdapter superAdapter =
 			(BeaninfoSuperAdapter) EcoreUtil.getRegisteredAdapter(getJavaClass(), BeaninfoSuperAdapter.class);
 		if (jcAllProperties != null) {
 			// See if new one required.
 			if (superAdapter == null || !superAdapter.isAllPropertiesCollectionModified())
 				return jcAllProperties;
 			// Can't get superadapter, so must not be attached to a resource, so return current list. Or no change required.       		
 		}
 		
 		UniqueEList.FastCompare allProperties = new UniqueEList.FastCompare() {
 			/**
 			 * Comment for <code>serialVersionUID</code>
 			 * 
 			 * @since 1.1.0
 			 */
 			private static final long serialVersionUID = 1L;
 
 			protected Object[] newData(int capacity) {
 				return new EStructuralFeature[capacity];
 			}
 		};
 		boolean doAllProperties = false;
 		synchronized(this) {
 			// If we are introspecting, don't do all properties because it is an invalid list. Just return empty without reseting the all modified flag.
 			doAllProperties = !isDoingAllProperties && !isIntrospecting && isResourceConnected();
 			if (doAllProperties)
 				isDoingAllProperties = true;
 		}
 		if (doAllProperties) {
 			try {
 				EList localProperties = getJavaClass().getProperties();
 				// Kludge: BeanInfo spec doesn't address Interfaces, but some people want to use them.
 				// Interfaces can have multiple extends, while the Introspector ignores these for reflection,
 				// the truth is most people want these. So we will add them in. But since there could be more than one it
 				// gets confusing. We need to look for dups from the super types and still keep order.
 				//
 				// Supertypes will never be more than one entry for classes, it is possible to be 0, 1, 2 or more for interfaces.
 				List superTypes = getJavaClass().getESuperTypes();
 				if (!superTypes.isEmpty()) {
 					// Now we need to merge in the supers.
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					// If there is only one supertype, we can add to the actual events, else we will use the linked hashset so that
 					// we don't add possible duplicates (e.g. IA extends IB,IC and IB extends IC. In this case there would be dups
 					// because IB would contribute IC's too).
 					Collection workingAllProperties = superTypes.size() == 1 ? (Collection) allProperties : new LinkedHashSet(); 
 					// We will now merge as directed.
 					boolean mergeAll = bd == null || bd.isMergeSuperProperties();
 					if (!mergeAll) {
 						// we don't to want to merge super properties, but we still need super non-properties or explict ones.
 						int lenST = superTypes.size();
 						for (int i=0; i<lenST; i++) {
 							List supers = ((JavaClass) superTypes.get(i)).getAllProperties();
 							int len = supers.size();
 							for (int i1 = 0; i1 < len; i1++) {
 								EStructuralFeature p = (EStructuralFeature) supers.get(i1);
 								PropertyDecorator pd = Utilities.getPropertyDecorator(p);
 								if ( pd == null || (pd.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL && !pd.isMergeIntrospection()))
 									workingAllProperties.add(p);
 							}
 						}
 					} else {
 						// We want to merge all.					
 						// BeanInfo could of given us the not merge list. If the list is empty, then we accept all.
 						if (bd != null && bd.eIsSet(BeaninfoPackage.eINSTANCE.getBeanDecorator_NotInheritedPropertyNames())) {
 							// We were given a list of names.
 							// Get the names into a set to create a quick lookup.
 							HashSet superSet = new HashSet(bd.getNotInheritedPropertyNames());
 							
 							// Now walk and add in non-bean properties (and bean properties that were explicitly added and not mergeable (i.e. didn't come thru beaninfo)) 
 							// and add those not specifically called out by BeanInfo in the not inherited list.
 							int lenST = superTypes.size();
 							for (int i=0; i<lenST; i++) {
 								List supers = ((JavaClass) superTypes.get(i)).getAllProperties();
 								int len = supers.size();
 								for (int i1 = 0; i1 < len; i1++) {
 									EStructuralFeature p = (EStructuralFeature) supers.get(i1);
 									PropertyDecorator pd = Utilities.getPropertyDecorator(p);
 									if (pd == null || (pd.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL && !pd.isMergeIntrospection()) || !superSet.contains(pd.getName()))
 										workingAllProperties.add(p);
 								}
 							}
 						} else {
 							// BeanInfo (or reflection always does this) called out that all super properties are good
 							int lenST = superTypes.size();
 							for (int i=0; i<lenST; i++) {
 								workingAllProperties.addAll(((JavaClass) superTypes.get(i)).getAllProperties());
 							}
 						}
 					}
 					if (workingAllProperties != allProperties)
 						allProperties.addAll(workingAllProperties);	// Now copy over the ordered super properties.
 				}
 				allProperties.addAll(localProperties);				
 				superAdapter.setAllPropertiesCollectionModified(false); // Now built, so reset to not changed.
 			} finally {
 				synchronized(this) {
 					isDoingAllProperties = false;
 				}
 			}
 		}
 
 		if (!allProperties.isEmpty()) {
 			allProperties.shrink();
 			return new EcoreEList.UnmodifiableEList.FastCompare(
 				getJavaClass(),
 				null,
 				allProperties.size(),
 				allProperties.data());
 		} else
 			return ECollections.EMPTY_ELIST;
 	}
 
 	/*
 	 * Fill in the property using the prop record. If this one that should have merging done, then
 	 * return the PropertyDecorator so that it can have the PropertyRecord merged into it. Else
 	 * return null if no merging.
 	 */
 	protected PropertyDecorator calculateProperty(PropertyRecord pr, boolean indexed) {
 		// If this is an indexed property, then a few fields will not be set in here, but
 		// will instead be set by the calculateIndexedProperty, which will be called.
 		boolean changeable = pr.writeMethod != null || (pr.field != null && !pr.field.readOnly);
 		JavaHelpers type = pr.propertyTypeName != null ? Utilities.getJavaType(MapJNITypes.getFormalTypeName(pr.propertyTypeName), getJavaClass().eResource().getResourceSet()) : null;
 
 		if (indexed) {
 			// If no array write method found, then see if there is an indexed write method. If there is, then it is changable.
 			if (!changeable)
 				changeable = ((IndexedPropertyRecord) pr).indexedWriteMethod != null;
 			if (type == null) {
 				// If no array type from above, create one from the indexed type proxy. Add '[]' to turn it into an array.
 				type = Utilities.getJavaType(MapJNITypes.getFormalTypeName(((IndexedPropertyRecord) pr).indexedPropertyTypeName)+"[]", getJavaClass().eResource().getResourceSet()); //$NON-NLS-1$
 			}
 		}
 
 		if (type != null)
 			return createProperty(pr.name, indexed, changeable, type); // A valid property descriptor.
 		else
 			return null;
 	}
 
 	/**
 	 * Fill in the property and its decorator using the passed in information.
 	 */
 	protected PropertyDecorator createProperty(String name, boolean indexed, boolean changeable, EClassifier type) {
 		// First find if there is already a property of this name, and if there is, is the PropertyDecorator
 		// marked to not allow merging in of introspection results.		
 		HashMap existingLocals = getPropertiesMap();
 		EStructuralFeature prop = null;
 		PropertyDecorator pd = null;
 		Object p = existingLocals.get(name);
 		if (Boolean.FALSE == p)
 			return null; // We've already processed this name, can't process it again.
 		if (p != null) {
 			// We've found one of the same name. Whether we modify it or use it as is, we put in a
 			// special dummy in its place. That marks that we've already processed it and accepted it.
 			existingLocals.put(name, Boolean.FALSE);
 
 			// If the decorator for this entry says not to merge then return.
 			// If there is no PropertyDecorator, then we will merge. If they
 			// didn't want to merge then should of created of property decorator and said no merge.
 			pd = Utilities.getPropertyDecorator((EStructuralFeature) p);
 			if (pd != null && !pd.isMergeIntrospection())
 				return null;
 			prop = (EStructuralFeature) p;
 		}
 
 		// Need to test if this is an implicit decorator and it is not of the 
 		// same type (i.e. is indexed now but wasn't or visa-versa, then we need
 		// to get rid of the decorator and recreate it. If it is not implicit, then
 		// we have to use it as is because the user specified, so it won't become
 		// an indexed if the user did not created it as an index, and visa-versa.
 		// Also if it is implicit, then we need to unset certain features that may of
 		// been set by a previous reflection which has now become introspected.
 		// When reflected we set the actual fields instead of the letting proxy determine them.
 		if (pd != null) {
 			if (pd.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL) {
 				// We can't change the type for explicit.
 				indexed = pd instanceof IndexedPropertyDecorator;
 			} else {
 				if ((indexed && !(pd instanceof IndexedPropertyDecorator)) || (!indexed && pd instanceof IndexedPropertyDecorator)) {
 					// It is implicit and of a different type, so just get rid of it and let it be recreated correctly.
 					prop.getEAnnotations().remove(pd);
 					pd = null;
 				}
 			}
 			if (pd != null)
 				if (indexed)
 					BeanInfoDecoratorUtility.clear((IndexedPropertyDecorator) pd);
 				else
 					BeanInfoDecoratorUtility.clear(pd);
 		}
 
 		ImplicitItem implicit = pd == null ? ImplicitItem.IMPLICIT_DECORATOR_LITERAL : pd.getImplicitDecoratorFlag();
 		if (prop == null) {
 			// We will create a new property.
 			// We can't have an implicit feature, but an explicit decorator.
 			getFeaturesList().add(prop = EcoreFactory.eINSTANCE.createEReference());
 			implicit = ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL;
 		}
 
 		setPropertyID(name, prop);
 		prop.setName(name);
 		prop.setTransient(false);
 		prop.setVolatile(false);
 		prop.setChangeable(changeable);
 
 		// Containment and Unsettable is tricky for EReferences. There is no way to know whether it has been explicitly set to false, or it defaulted to
 		// false because ECore has not made containment/unsettable an unsettable feature. So we need to instead use the algorithm of if we here 
 		// created the feature, then we will by default set it to containment/unsettable. If it was created through diff merge, then
 		// we will leave it alone. It is the responsibility of the merge file writer to set containment/unsettable correctly.
 		if (implicit == ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL) {
 			prop.setUnsettable(true);
 		}
 		prop.setEType(type);
 		if (!indexed) {
 			prop.setLowerBound(0);
 			prop.setUpperBound(1);
 		} else {
 			prop.setLowerBound(0);
 			prop.setUpperBound(-1);
 			prop.setUnique(true);
 		}
 
 		// Now create/fill in the property descriptor for it.
 		// If there already is one then we
 		// will use it. This allows merging with beanfinfo.		
 		if (pd == null) {
 			pd =
 				(!indexed)
 					? BeaninfoFactory.eINSTANCE.createPropertyDecorator()
 					: BeaninfoFactory.eINSTANCE.createIndexedPropertyDecorator();
 			pd.setImplicitDecoratorFlag(implicit);
 			prop.getEAnnotations().add(pd);
 		}
 		return pd;
 	}
 
 	/**
 	 * @param name
 	 * @param prop
 	 * 
 	 * @since 1.1.0
 	 */
 	private void setPropertyID(String name, EStructuralFeature prop) {
 		// Now fill it in. Normal id for an attribute is "classname.attributename" but we can't use that
 		// for us because that format is used by Java Core for a field and there would be confusion.
 		// So we will use '/' instead.
 		((XMIResource) prop.eResource()).setID(prop, getJavaClass().getName() + BeaninfoJavaReflectionKeyExtension.FEATURE + name);
 	}
 
 	/*
 	 * Reflect the properties. This requires going through local methods and matching them up to
 	 * see if they are properties.
 	 */
 	private void reflectProperties() {
 		// If we are set to mergeSuperTypeProperties, then we need to get the super properties.
 		// This is so that duplicate any from super that we find here. When reflecting we don't
 		// allow discovered duplicates unless they are different types.
 		//
 		// Kludge: BeanInfo spec doesn't address Interfaces, but some people want to use them.
 		// Interfaces can have multiple extends, while the Introspector ignores these for reflection,
 		// the truth is most people want these. So we will add them in. But since there could be more than one it
 		// gets confusing. We need to look for dups from the super types.
 		//
 		// Supertypes will never be more than one entry for classes, it is possible to be 0, 1, 2 or more for interfaces.
 		TimerTests.basicTest.startCumulativeStep(REFLECT_PROPERTIES);		
 		Set supers = new HashSet(50);
 		BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 		if (bd == null || bd.isMergeSuperProperties()) {
 			List superTypes = getJavaClass().getESuperTypes();
 			if (!superTypes.isEmpty()) {
 				int lenST = superTypes.size();
 				for (int i=0; i<lenST; i++) {
 					List superAll = ((JavaClass) superTypes.get(i)).getAllProperties();
 					int len = superAll.size();
 					for (int i1=0; i1<len; i1++) {
 						EStructuralFeature sf = (EStructuralFeature) superAll.get(i1);
 						supers.add(sf.getName());
 					}
 				}
 			}
 		}
 
 		// If any of the classes in the hierarchy are bound, then all reflected properties are considered bound.
 		boolean isBound = isDefaultBound();
 		if (!isBound) {
 			List superTypes = getJavaClass().getEAllSuperTypes();
 			// Start from end because that will be first class above the this one.
 			for (int i=superTypes.size()-1; !isBound && i>=0; i--) {
 				JavaClass spr = (JavaClass) superTypes.get(i);
 				BeaninfoClassAdapter bi = (BeaninfoClassAdapter) EcoreUtil.getExistingAdapter(spr, IIntrospectionAdapter.ADAPTER_KEY);
 				// They should all be reflected, but be on safe side, check if null.
 				if (bi != null)
 					isBound = bi.isDefaultBound();
 			}
 		}
 
 		HashMap props = new HashMap();
 
 		Iterator itr = getJavaClass().getPublicMethods().iterator();
 		while (itr.hasNext()) {
 			Method mthd = (Method) itr.next();
 			if (mthd.isStatic() || mthd.isConstructor())
 				continue; // Statics/constructors don't participate as properties
 			if (mthd.getName().startsWith("get")) { //$NON-NLS-1$
 				String name = java.beans.Introspector.decapitalize(mthd.getName().substring(3));
 				if (name.length() == 0 || supers.contains(name))
 					continue;	// Had get(...) and not getXXX(...) so not a getter. Or this is the same name as a super, either way ignore it.
 				PropertyInfo propInfo = (PropertyInfo) props.get(name);
 				if (propInfo == null) {
 					propInfo = new PropertyInfo();
 					if (propInfo.setGetter(mthd, false))
 						props.put(name, propInfo);
 				} else
 					propInfo.setGetter(mthd, false);
 			} else if (mthd.getName().startsWith("is")) { //$NON-NLS-1$
 				String name = java.beans.Introspector.decapitalize(mthd.getName().substring(2));
 				if (name.length() == 0 || supers.contains(name))
 					continue;	// Had is(...) and not isXXX(...) so not a getter. Or this is the same name as a super, either way ignore it.
 				PropertyInfo propInfo = (PropertyInfo) props.get(name);
 				if (propInfo == null) {
 					propInfo = new PropertyInfo();
 					if (propInfo.setGetter(mthd, true))
 						props.put(name, propInfo);
 				} else
 					propInfo.setGetter(mthd, true);
 			} else if (mthd.getName().startsWith("set")) { //$NON-NLS-1$
 				String name = java.beans.Introspector.decapitalize(mthd.getName().substring(3));
 				if (name.length() == 0 || supers.contains(name))
 					continue;	// Had set(...) and not setXXX(...) so not a setter. Or this is the same name as a super, either way ignore it.				
 				PropertyInfo propInfo = (PropertyInfo) props.get(name);
 				if (propInfo == null) {
 					propInfo = new PropertyInfo();
 					if (propInfo.setSetter(mthd))
 						props.put(name, propInfo);
 				} else
 					propInfo.setSetter(mthd);
 			}
 		}
 
 		// Now go through the hash map and create the properties.
 		itr = props.entrySet().iterator();
 		while (itr.hasNext()) {
 			Map.Entry entry = (Map.Entry) itr.next();
 			((PropertyInfo) entry.getValue()).createProperty((String) entry.getKey(), isBound);
 		}
 		TimerTests.basicTest.stopCumulativeStep(REFLECT_PROPERTIES);		
 	}
 
 	private class PropertyInfo {
 		
 		public EClassifier type, indexedType;
 		public boolean constrained;
 		public Method getter, setter, indexedGetter, indexedSetter;
 
 		public boolean setGetter(Method get, boolean mustBeBoolean) {
 			List parms = get.getParameters();
 			if (parms.size() > 1)
 				return false; // Invalid - improper number of parms.
 			boolean indexed = parms.size() == 1;
 			if (indexed && !((JavaParameter) parms.get(0)).getEType().getName().equals("int")) //$NON-NLS-1$
 				return false; // Invalid - a parm that is not an int is invalid for indexed.
 			EClassifier retType = get.getReturnType();
 			if (retType == null || retType.getName().equals("void")) //$NON-NLS-1$
 				return false; // Invalid - must have a return type
 			if (mustBeBoolean && !retType.getName().equals("boolean")) //$NON-NLS-1$
 				return false; // Invalid - it must be a boolean.
 			if (indexed) {
 				if (indexedType != null) {
 					if (indexedType != retType)
 						return false; // Invalid - type is different from previous info.
 				}
 				if (type != null && !(((JavaHelpers) type).isArray() && ((ArrayType) type).getComponentType() == retType))
 					return false; // Invalid - indexed type doesn't match component type of base type.
 			} else {
 				if (type != null) {
 					if (type != retType)
 						return false; // Invalid - type is different from previous info.
 				}
 				if (indexedType != null && !(((JavaHelpers) retType).isArray() && ((ArrayType) retType).getComponentType() == indexedType))
 					if (type == null) {
 						// We had a potential indexed and had not yet found the regular type. We've now found
 						// the regular type, and it is not indexed. So it takes priority and will wipe out
 						// the indexed type.
 						indexedGetter = null;
 						indexedSetter = null;
 						indexedType = null;
 					} else
 						return false; // Invalid - indexed type doesn't match component type of base type we already have
 			}
 
 			if (indexed) {
 				if (indexedGetter != null)
 					return false; // Already have an indexed getter.
 				indexedGetter = get;
 				indexedType = retType;
 			} else {
 				if (getter != null)
 					return false; // Already have a getter
 				getter = get;
 				type = retType;
 			}
 			return true;
 		}
 
 		public boolean setSetter(Method set) {
 			List parms = set.getParameters();
 			if (parms.size() > 2 || parms.size() < 1)
 				return false; // Invalid - improper number of parms.
 			boolean indexed = parms.size() == 2;
 			if (indexed && !((JavaParameter) parms.get(0)).getEType().getName().equals("int")) //$NON-NLS-1$
 				return false; // Invalid - a parm that is not an int is invalid for indexed.
 			EClassifier retType = set.getReturnType();
 			if (retType != null && !retType.getName().equals("void")) //$NON-NLS-1$
 				return false; // Invalid - must not have a return type
 			EClassifier propType = null;
 			if (indexed) {
 				propType = ((JavaParameter) parms.get(1)).getEType();
 				if (indexedType != null) {
 					if (indexedType != propType)
 						return false; // Invalid - type is different from previous info.
 				}
 				if (type != null && !(((JavaHelpers) type).isArray() && ((ArrayType) type).getComponentType() == propType))
 					return false; // Invalid - indexed type doesn't match component type of base type, or base type not an array
 			} else {
 				propType = ((JavaParameter) parms.get(0)).getEType();
 				if (type != null) {
 					if (type != propType)
 						return false; // Invalid - type is different from previous info.
 				}
 				if (indexedType != null
 					&& !(((JavaHelpers) propType).isArray() && ((ArrayType) propType).getComponentType() == indexedType))
 					if (type == null) {
 						// We had a potential indexed and had not yet found the regular type. We've now found
 						// the regular type, and it is not indexed of the correct type. So it takes priority and will wipe out
 						// the indexed type.
 						indexedGetter = null;
 						indexedSetter = null;
 						indexedType = null;
 					} else
 						return false; // Invalid - indexed type doesn't match component type of base type from this setter.
 			}
 
 			if (indexed) {
 				if (indexedSetter != null)
 					return false; // Already have an indexed getter.
 				indexedSetter = set;
 				indexedType = propType;
 			} else {
 				if (setter != null)
 					return false; // Already have a getter
 				setter = set;
 				type = propType;
 			}
 
 			if (set.getJavaExceptions().contains(Utilities.getJavaClass("java.beans.PropertyVetoException", getJavaClass().eResource().getResourceSet()))) //$NON-NLS-1$
 				constrained = true;
 			return true;
 		}
 
 		public void createProperty(String name, boolean isBound) {
 			boolean indexed = indexedType != null;
 			if (indexed && type == null)
 				return; // A potential indexed, but never found the getter/setter of the regular type.
 
 			PropertyDecorator prop =
 				BeaninfoClassAdapter.this.createProperty(
 					name,
 					indexed,
 					(!indexed) ? (setter != null) : (setter != null || indexedSetter != null),
 					type);
 			if (prop == null)
 				return; // Reflection not wanted.
 
 			indexed = prop instanceof IndexedPropertyDecorator; // It could of been forced back to not indexed if explicitly set.
 
 			// At this point in time all implicit settings have been cleared. This is done back in createProperty() method above.
 			// So now apply reflected settings on the property decorator. 
 			BeanInfoDecoratorUtility.setProperties(prop, isBound, constrained, getter, setter);
 			if (indexed)
 				BeanInfoDecoratorUtility.setProperties((IndexedPropertyDecorator) prop, indexedGetter, indexedSetter);
 
 		}
 	};
 
 	/*
 	 * Should only be called from introspect so that flags are correct.
 	 */
 	private void finalizeOperations(ChangeDescription cd) {
 		// Now go through the list and remove those that should be removed.
 		Iterator itr = getOperationsList().iterator();
 		while (itr.hasNext()) {
 			EOperation a = (EOperation) itr.next();
 			MethodDecorator m = Utilities.getMethodDecorator(a);
 			if (!newoperations.contains(a)) {
 				// A candidate for removal. It is in the list but we didn't add it. Check to see if it one we had created in the past.
 				// If no methoddecorator, then keep it, not one ours.
 				if (m != null) {
 					ImplicitItem implicit = m.getImplicitDecoratorFlag();
 					if (implicit != ImplicitItem.NOT_IMPLICIT_LITERAL) {
 						m.setEModelElement(null); // Remove it because it was implicit.
 						 ((InternalEObject) m).eSetProxyURI(BAD_URI);
 						if (implicit == ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL) {
 							itr.remove(); // The operation was implicit too.
 							 ((InternalEObject) a).eSetProxyURI(BAD_URI);
 						}
 						continue;	// At this point we no longer have a method decorator, so go to next.
 					}
 				}
 			}
 			
 			if (m != null && cd != null) {
 				// Now create the appropriate cache entry for this method.
 				BeanInfoDecoratorUtility.buildChange(cd, m);
 			}
 		}
 	}
 
 	/**
 	 * Fill in the behavior and its decorator using the mthdDesc.
 	 */
 	protected MethodDecorator calculateOperation(MethodRecord record) {
 		return createOperation(record.name, formLongName(record), null, record);
 	}
 
 	/**
 	 * Fill in the behavior and its decorator using the passed in information.
 	 */
 	protected MethodDecorator createOperation(String name, String longName, Method method, MethodRecord record) {
 		// First find if there is already a behavior of this name and method signature , and if there is, is the MethodDecorator
 		// marked to not allow merging in of introspection results.
 		HashMap existingLocals = getOperationsMap();
 		EOperation oper = null;
 		MethodDecorator md = null;
 		Object b = null;
 		if (name != null)
 			b = existingLocals.get(longName);
 		else
 			b = existingLocals.get(longName);
 			
 		if (b != null) {
 			// If the decorator for this entry says not to merge then return.
 			// If there is no decorator, then we will merge. If they didn't want to
 			// merge, then they should of created a decorator with no merge on it.
 			md = Utilities.getMethodDecorator((EOperation) b);
 			if (md != null && !md.isMergeIntrospection())
 				return null;
 			oper = (EOperation) b;
 		}
 
 		// Need to find the method and method id.
 		if (method == null) {
 			// No method sent, create a proxy to it from the record.
 			method = BeanInfoDecoratorUtility.createJavaMethodProxy(record.methodForDescriptor);
 		}
 
 		ImplicitItem implicit = md == null ? ImplicitItem.IMPLICIT_DECORATOR_LITERAL : ImplicitItem.NOT_IMPLICIT_LITERAL;
 		if (oper == null) {
 			// We will create a new MethodProxy.
 			oper = BeaninfoFactory.eINSTANCE.createMethodProxy();
 			getOperationsList().add(oper);
 			implicit = ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL; 
 		}		
 		if (name == null)
 			name = method.getName();		
 
 		// Now fill it in.
 		if (oper instanceof MethodProxy)
 			 ((MethodProxy) oper).setMethod(method);
 		setMethodID(name, oper);
 		oper.setName(name);
 		newoperations.add(oper);
 
 		// Now create/fill in the method decorator for it.
 		// If there already is one then we
 		// will use it. This allows merging with beaninfo.		
 		if (md == null) {
 			md = BeaninfoFactory.eINSTANCE.createMethodDecorator();
 			md.setImplicitDecoratorFlag(implicit);
 			oper.getEAnnotations().add(md);
 		} else
 			BeanInfoDecoratorUtility.clear(md);
 		return md;
 	}
 
 	/**
 	 * @param name
 	 * @param oper
 	 * 
 	 * @since 1.1.0
 	 */
 	private void setMethodID(String name, EOperation oper) {
 		((XMIResource) oper.eResource()).setID(oper, getJavaClass().getName() + BeaninfoJavaReflectionKeyExtension.BEHAVIOR + name);
 	}
 
 	private void reflectOperations() {
 		// If we are set to mergeSuperTypeBehaviors, then we need to get the super behaviors.
 		// This is so that duplicate any from super that we find here. When reflecting we don't
 		// allow discovered duplicates unless they are different signatures. So all super operations
 		// will be allowed and we will not override them.
 		//
 		// Kludge: BeanInfo spec doesn't address Interfaces, but some people want to use them.
 		// Interfaces can have multiple extends, while the Introspector ignores these for reflection,
 		// the truth is most people want these. So we will add them in. But since there could be more than one it
 		// gets confusing. We need to look for dups from the super types.
 		//
 		// Supertypes will never be more than one entry for classes, it is possible to be 0, 1, 2 or more for interfaces.
 		Set supers = new HashSet(50);
 		BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 		if (bd == null || bd.isMergeSuperMethods()) {
 			List superTypes = getJavaClass().getESuperTypes();
 			if (!superTypes.isEmpty()) {
 				int lenST = superTypes.size();
 				for (int i=0; i<lenST; i++) {
 					List superAll = ((JavaClass) superTypes.get(i)).getEAllOperations();
 					int len = superAll.size();
 					for (int i1=0; i1<len; i1++) {
 						EOperation op = (EOperation) superAll.get(i1);
 						supers.add(formLongName(op));
 					}
 				}
 			}
 		}
 
 		Iterator itr = getJavaClass().getPublicMethods().iterator();
 		while (itr.hasNext()) {
 			Method mthd = (Method) itr.next();
 			if (mthd.isStatic() || mthd.isConstructor())
 				continue; // Statics/constructors don't participate as behaviors	
 			String longName = formLongName(mthd);
 			// Add if super not already contain it.
 			if (!supers.contains(longName))
 				createOperation(null, longName, mthd, null);	// Don't pass a name, try to create it by name, only use ID if there is more than one of the same name.
 		}
 	}
 
 	/*
 	 * merge all  the Behaviors(i.e. supertypes)
 	 */
 	protected BasicEList allOperations() {
 		BasicEList jcAllOperations = (BasicEList) getJavaClass().primGetEAllOperations();
 		BeaninfoSuperAdapter superAdapter =
 			(BeaninfoSuperAdapter) EcoreUtil.getRegisteredAdapter(getJavaClass(), BeaninfoSuperAdapter.class);
 		if (jcAllOperations != null) {
 			// See if new one required.
 			if (superAdapter == null || !superAdapter.isAllOperationsCollectionModified())
 				return jcAllOperations;
 			// Can't get superadapter, so must not be attached to a resource, so return current list. Or no change required.       		
 		}
 
 		UniqueEList.FastCompare allOperations = new UniqueEList.FastCompare() {
 			/**
 			 * Comment for <code>serialVersionUID</code>
 			 * 
 			 * @since 1.1.0
 			 */
 			private static final long serialVersionUID = 1L;
 
 			protected Object[] newData(int capacity) {
 				return new EOperation[capacity];
 			}
 		};
 		boolean doAllOperations = false;
 		synchronized(this) {
 			// If we are introspecting, don't do all operatoins because it is an invalid list. Just return empty without reseting the all modified flag.
 			doAllOperations = !isDoingAllOperations && !isIntrospecting && isResourceConnected();
 			if (doAllOperations)
 				isDoingAllOperations = true;
 		}
 
 		if (doAllOperations) {
 			try {
 				EList localOperations = getJavaClass().getEOperations();
 				// Kludge: BeanInfo spec doesn't address Interfaces, but some people want to use them.
 				// Interfaces can have multiple extends, while the Introspector ignores these for reflection,
 				// the truth is most people want these. So we will add them in. But since there could be more than one it
 				// gets confusing. We need to look for dups from the super types and still keep order.
 				//
 				// Supertypes will never be more than one entry for classes, it is possible to be 0, 1, 2 or more for interfaces.
 				List superTypes = getJavaClass().getESuperTypes();
 				if (!superTypes.isEmpty()) {
 					// Now we need to merge in the supers.
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					// If there is only one supertype, we can add to the actual events, else we will use the linked hashset so that
 					// we don't add possible duplicates (e.g. IA extends IB,IC and IB extends IC. In this case there would be dups
 					// because IB would contribute IC's too).
 					Collection workingAllOperations = superTypes.size() == 1 ? (Collection) allOperations : new LinkedHashSet(); 
 					// We will now merge as directed.
 					boolean mergeAll = bd == null || bd.isMergeSuperMethods();
 					if (!mergeAll) {
 						// we don't to want to merge super operations, but we still need super non-operations.
 						int lenST = superTypes.size();
 						for (int i=0; i<lenST; i++) {
 							List supers = ((JavaClass) superTypes.get(i)).getEAllOperations();
 							int len = supers.size();
 							for (int i1 = 0; i1 < len; i1++) {
 								EOperation o = (EOperation) supers.get(i1);
 								MethodDecorator md = Utilities.getMethodDecorator(o);
 								if (md == null || (md.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL && !md.isMergeIntrospection()))
 									workingAllOperations.add(o);
 							}
 						}
 					} else {
 						// We want to merge all.
 						// BeanInfo could of given us the don't merge list. If the list is empty, then we accept all.
 						if (bd != null && bd.eIsSet(BeaninfoPackage.eINSTANCE.getBeanDecorator_NotInheritedMethodNames())) {
 							// We were given a list of names.
 							// Get the names into a set to create a quick lookup.
 							HashSet superSet = new HashSet(bd.getNotInheritedMethodNames());
 							
 							// Now walk and add in non-bean operations (and bean operations that were explicitly added and not mergeable (i.e. didn't come thru beaninfo)) 
 							// and add those not specifically called out by BeanInfo not merge list.
 							int lenST = superTypes.size();
 							for (int i=0; i<lenST; i++) {
 								List supers = ((JavaClass) superTypes.get(i)).getEAllOperations();
 								int len = supers.size();
 								for (int i1 = 0; i1 < len; i1++) {
 									EOperation o = (EOperation) supers.get(i1);
 									MethodDecorator md = Utilities.getMethodDecorator(o);
 									if (md == null || (md.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL && !md.isMergeIntrospection()))
 										workingAllOperations.add(o);
 									else {
 										String longName = formLongName(o);
 										if (longName == null || !superSet.contains(longName))
 											workingAllOperations.add(o);
 									}
 								}
 							}
 						} else {
 							int lenST = superTypes.size();
 							for (int i=0; i<lenST; i++) {
 								workingAllOperations.addAll(((JavaClass) superTypes.get(i)).getEAllOperations());
 							}
 						}
 					}
 					if (workingAllOperations != allOperations)
 						allOperations.addAll(workingAllOperations);	// Now copy over the ordered super operations.					
 				}
 				allOperations.addAll(localOperations);				
 				ESuperAdapter sa = getJavaClass().getESuperAdapter();
 				sa.setAllOperationsCollectionModified(false); // Now built, so reset to not changed.
 			} finally {
 				synchronized(this) {
 					isDoingAllProperties = false;
 				}
 			}
 		}
 
 		allOperations.shrink();
 		return new EcoreEList.UnmodifiableEList.FastCompare(
 			getJavaClass(),
 			EcorePackage.eINSTANCE.getEClass_EAllOperations(),
 			allOperations.size(),
 			allOperations.data());
 
 	}
 
 	/*
 	 * Should only be called from introspect so that flags are correct.
 	 */
 	private void finalizeEvents(ChangeDescription cd) {
 		// Now go through the list and remove those that should be removed.
 		Map oldLocals = getEventsMap();
 		Iterator itr = getEventsList().iterator();
 		while (itr.hasNext()) {
 			JavaEvent a = (JavaEvent) itr.next();
 			EventSetDecorator e = Utilities.getEventSetDecorator(a);
 			Object aOld = oldLocals.get(a.getName());
 			if (aOld != null && aOld != Boolean.FALSE) {
 				// A candidate for removal. It was in the old list and it was not processed.
 				if (e != null) {
 					ImplicitItem implicit = e.getImplicitDecoratorFlag();
 					if (implicit != ImplicitItem.NOT_IMPLICIT_LITERAL) {
 						e.setEModelElement(null); // Remove it because it was implicit.
 						((InternalEObject) e).eSetProxyURI(BAD_URI);
 						if (implicit == ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL) {
 							itr.remove(); // The feature was implicit too.
 							((InternalEObject) a).eSetProxyURI(BAD_URI);
 						}
 						continue;	// At this point we don't have a decorator any more, so don't bother going on.
 					}
 				}
 			}
 			
 			if (e != null && cd != null) {
 				// Now create the appropriate cache entry for this event.
 				BeanInfoDecoratorUtility.buildChange(cd, e);
 			}
 		}
 	}
 
 	/**
 	 * Fill in the event and its decorator using the eventDesc.
 	 */
 	protected EventSetDecorator calculateEvent(EventSetRecord record) {
 		return createEvent(record.name);
 	}
 
 	/**
 	 * Fill in the event and its decorator using the passed in information.
 	 */
 	protected EventSetDecorator createEvent(String name) {
 		// First find if there is already an event of this name, and if there is, is the EventSetDecorator
 		// marked to not allow merging in of introspection results.
 		HashMap existingLocals = getEventsMap();
 		JavaEvent event = null;
 		EventSetDecorator ed = null;
 		Object b = existingLocals.get(name);
 		if (Boolean.FALSE == b)
 			return null; // We've already processed this event, can't process it again.			
 		if (b != null) {
 			// We've found one of the same event. Whether we modify it or use it as is, we put in a
 			// special dummy in its place. That marks that we've already processed it and accepted it.
 			existingLocals.put(name, Boolean.FALSE);
 
 			// If the decorator for this entry says not to merge then return.
 			// If there is no decorator, then we will merge. If they didn't want to
 			// merge, then they should of created a decorator with no merge on it.
 			ed = Utilities.getEventSetDecorator((JavaEvent) b);
 			if (ed != null && !ed.isMergeIntrospection())
 				return null;
 			event = (JavaEvent) b;
 		}
 		
 		ImplicitItem implicit = ed == null ? ImplicitItem.IMPLICIT_DECORATOR_LITERAL : ImplicitItem.NOT_IMPLICIT_LITERAL;
 		if (event == null) {
 			// We will create a new Event.
 			event = BeaninfoFactory.eINSTANCE.createBeanEvent();
 			getEventsList().add(event);
 			implicit = ImplicitItem.IMPLICIT_DECORATOR_AND_FEATURE_LITERAL; // Can't have an implicit feature but explicit decorator.
 		}
 
 		setEventID(name, event);
 		event.setName(name);
 
 		// Now create in the event decorator for it.
 		// If there already is one then we
 		// will use it. This allows merging with beaninfo.		
 		if (ed == null) {
 			ed = BeaninfoFactory.eINSTANCE.createEventSetDecorator();
 			ed.setImplicitDecoratorFlag(implicit);
 			event.getEAnnotations().add(ed);
 		} else
 			BeanInfoDecoratorUtility.clear(ed);
 		return ed;
 	}
 
 	/**
 	 * @param name
 	 * @param event
 	 * 
 	 * @since 1.1.0
 	 */
 	private void setEventID(String name, JavaEvent event) {
 		// Now fill it in.
 		((XMIResource) event.eResource()).setID(event, getJavaClass().getName() + BeaninfoJavaReflectionKeyExtension.EVENT + name);
 	}
 
 	/**
 	 * Reflect the events. This requires going through local public methods and creating an 
 	 * event for the discovered events.
 	 */
 	protected void reflectEvents() {
 		// If we are set to mergeSuperTypeEvents, then we need to get the super events.
 		// This is so that duplicate any from super that we find here. When reflecting we don't
 		// allow discovered duplicates.
 		//
 		// Kludge: BeanInfo spec doesn't address Interfaces, but some people want to use them.
 		// Interfaces can have multiple extends, while the Introspector ignores these for reflection,
 		// the truth is most people want these. So we will add them in. But since there could be more than one it
 		// gets confusing. We need to look for dups from the super types.
 		//
 		// Supertypes will never be more than one entry for classes, it is possible to be 0, 1, 2 or more for interfaces.
 		Set supers = new HashSet(50);
 		BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 		if (bd == null || bd.isMergeSuperEvents()) {
 			List superTypes = getJavaClass().getESuperTypes();
 			if (!superTypes.isEmpty()) {
 				int lenST = superTypes.size();
 				for (int i=0; i<lenST; i++) {
 					List superAll = ((JavaClass) superTypes.get(i)).getAllEvents();
 					int len = superAll.size();
 					for (int i1=0; i1<len; i1++) {
 						JavaEvent se = (JavaEvent) superAll.get(i1);
 						supers.add(se.getName());
 					}
 				}
 			}
 		}
 
 		HashMap events = new HashMap();
 		eventListenerClass = (JavaClass) JavaRefFactory.eINSTANCE.reflectType("java.util.EventListener", getJavaClass()); // Initialize, needed for building eventinfos. //$NON-NLS-1$
 		tooManyExceptionClass = (JavaClass) JavaRefFactory.eINSTANCE.reflectType("java.util.TooManyListenersException", getJavaClass()); // Initialize, needed for building eventinfos. //$NON-NLS-1$
 		Iterator itr = getJavaClass().getPublicMethods().iterator();
 		while (itr.hasNext()) {
 			Method mthd = (Method) itr.next();
 			if (mthd.isStatic() || mthd.isConstructor())
 				continue; // Statics/constructors don't participate in events.
 			String key = validEventAdder(mthd);
 			if (key != null) {
 				EventInfo eventInfo = (EventInfo) events.get(key);
 				if (eventInfo == null) {
 					eventInfo = new EventInfo();
 					eventInfo.setAdder(mthd);
 					events.put(key, eventInfo);
 				} else
 					eventInfo.setAdder(mthd);
 			} else {
 				key = validEventRemove(mthd);
 				if (key != null) {
 					EventInfo eventInfo = (EventInfo) events.get(key);
 					if (eventInfo == null) {
 						eventInfo = new EventInfo();
 						eventInfo.setRemover(mthd);
 						events.put(key, eventInfo);
 					} else
 						eventInfo.setRemover(mthd);
 				}
 			}
 		}
 
 		eventListenerClass = null; // No longer need it.
 
 		// Now actually create the events.
 		HashSet eventNames = new HashSet(events.size()); // Set of found event names, to prevent duplicates
 		Iterator evtItr = events.entrySet().iterator();
 		while (evtItr.hasNext()) {
 			Map.Entry eventMap = (Map.Entry) evtItr.next();
 			EventInfo ei = (EventInfo) eventMap.getValue();
 			if (ei.isValidInfo()) {
 				String eventName = getEventName((String) eventMap.getKey());
 				if (eventNames.contains(eventName))
 					continue;	// Aleady created it. (Note: Introspector actually takes last one over previous dups, but the order is totally undefined, so choosing first is just as good or bad.
 
 				if (supers.contains(eventName))
 					continue; // Don't override a super event.
 
 				if (ei.createEvent(eventName))
 					eventNames.add(eventName); // It was validly created.
 			}
 		}
 
 		tooManyExceptionClass = null; // No longer need it.
 	}
 
 	/**
 	 * merge all the Events (i.e. supertypes)
 	 */
 	protected EList allEvents() {
 
 		EList jcAllEvents = getJavaClass().getAllEventsGen();
 		BeaninfoSuperAdapter superAdapter =
 			(BeaninfoSuperAdapter) EcoreUtil.getRegisteredAdapter(getJavaClass(), BeaninfoSuperAdapter.class);
 		if (jcAllEvents != null) {
 			// See if new one required.
 			if (superAdapter == null || !superAdapter.isAllEventsCollectionModified())
 				return jcAllEvents;
 			// Can't get superadapter, so must not be attached to a resource, so return current list. Or no change required.       		
 		}
 
 		UniqueEList.FastCompare allEvents = new UniqueEList.FastCompare() {
 			/**
 			 * Comment for <code>serialVersionUID</code>
 			 * 
 			 * @since 1.1.0
 			 */
 			private static final long serialVersionUID = 1L;
 
 			protected Object[] newData(int capacity) {
 				return new JavaEvent[capacity];
 			}
 		};
 		
 		boolean doAllEvents = false;
 		synchronized(this) {
 			// If we are introspecting, don't do all properties because it is an invalid list. Just return empty without reseting the all modified flag.
 			doAllEvents = !isDoingAllEvents && !isIntrospecting && isResourceConnected();
 			if (doAllEvents)
 				isDoingAllEvents = true;
 		}
 
 		if (doAllEvents) {
 			try {
 				EList localEvents = getJavaClass().getEvents();
 				// Kludge: BeanInfo spec doesn't address Interfaces, but some people want to use them.
 				// Interfaces can have multiple extends, while the Introspector ignores these for reflection,
 				// the truth is most people want these. So we will add them in. But since there could be more than one it
 				// gets confusing. We need to look for dups from the super types and still keep order.
 				//
 				// Supertypes will never be more than one entry for classes, it is possible to be 0, 1, 2 or more for interfaces.
 				List superTypes = getJavaClass().getESuperTypes();
 				if (!superTypes.isEmpty()) {
 					// Now we need to merge in the supers.
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					// If there is only one supertype, we can add to the actual events, else we will use the linked hashset so that
 					// we don't add possible duplicates (e.g. IA extends IB,IC and IB extends IC. In this case there would be dups
 					// because IB would contribute IC's too).
 					Collection workingAllEvents = superTypes.size() == 1 ? (Collection) allEvents : new LinkedHashSet(); 
 					// We will now merge as directed.
 					boolean mergeAll = bd == null || bd.isMergeSuperEvents();
 					if (!mergeAll) {
 						// we don't to want to merge super events, but we still need super non-events or explicit ones.
 						int lenST = superTypes.size();
 						for (int i=0; i<lenST; i++) {
 							List supers = ((JavaClass) superTypes.get(i)).getAllEvents();
 							int len = supers.size();
 							for (int i1 = 0; i1 < len; i1++) {
 								JavaEvent e = (JavaEvent) supers.get(i1);
 								EventSetDecorator ed = Utilities.getEventSetDecorator(e);
 								if (ed == null || (ed.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL && !ed.isMergeIntrospection()))
 									workingAllEvents.add(e);
 							}
 						}
 					} else {
 						// We want to merge all.					
 						// BeanInfo has given us the not merge list. If the list is empty, then we accept all.
 						if (bd != null && bd.eIsSet(BeaninfoPackage.eINSTANCE.getBeanDecorator_NotInheritedEventNames())) {
 							// We were given a list of names.
 							// Get the names into a set to create a quick lookup.
 							HashSet superSet = new HashSet(bd.getNotInheritedEventNames());
 							
 							// Now walk and add in non-bean events (and bean events that were explicitly added and not mergeable (i.e. didn't come thru beaninfo)) 
 							// and add those not specifically called out by BeanInfo.
 							int lenST = superTypes.size();
 							for (int i=0; i<lenST; i++) {
 								List supers = ((JavaClass) superTypes.get(i)).getAllEvents();
 								int len = supers.size();
 								for (int i1 = 0; i1 < len; i1++) {
 									JavaEvent e = (JavaEvent) supers.get(i1);
 									EventSetDecorator ed = Utilities.getEventSetDecorator(e);
 									if (ed == null || (ed.getImplicitDecoratorFlag() == ImplicitItem.NOT_IMPLICIT_LITERAL && !ed.isMergeIntrospection()) || !superSet.contains(ed.getName()))
 										workingAllEvents.add(e);
 								}
 							}
 						} else {
 							// BeanInfo called out that all super events should be added, or no beaninfo.
 							int lenST = superTypes.size();
 							for (int i=0; i<lenST; i++) {
 								workingAllEvents.addAll(((JavaClass) superTypes.get(i)).getAllEvents());
 							}
 						}
 					}
 					if (workingAllEvents != allEvents)
 						allEvents.addAll(workingAllEvents);	// Now copy over the ordered super events.
 				}
 				allEvents.addAll(localEvents);				
 				superAdapter.setAllEventsCollectionModified(false); // Now built, so reset to not changed.
 			} finally {
 				synchronized (this) {
 					isDoingAllEvents = false;
 				}
 			}
 		}
 
 		if (allEvents.isEmpty())
 			return ECollections.EMPTY_ELIST;
 		else {
 			allEvents.shrink();
 			return new EcoreEList.UnmodifiableEList.FastCompare(
 				getJavaClass(),
 				JavaRefPackage.eINSTANCE.getJavaClass_AllEvents(),
 				allEvents.size(),
 				allEvents.data());
 		}
 
 	}
 
 	private JavaClass eventListenerClass, // Event Listener class. Needed for validation.
 	tooManyExceptionClass; // Too many listeners exception.
 
 	/*
 	 * Pass in the key, it will be used to form the name.
 	 */
 	protected String getEventName(String key) {
 		return key.substring(0, key.indexOf(':'));
 	}
 
 	/*	
 	 * Answers event key if valid, null if not valid.
 	 */
 	protected String validEventAdder(Method method) {
 		String name = method.getName();
 		if (!name.startsWith("add") || !name.endsWith("Listener")) //$NON-NLS-1$ //$NON-NLS-2$
 			return null; // Not valid format for an add listener name.
 
 		List parms = method.getParameters();
 		if (parms.size() != 1)
 			return null; // Invalid - improper number of parms.
 
 		EClassifier returnType = method.getReturnType();
 		if (returnType == null || !returnType.getName().equals("void")) //$NON-NLS-1$
 			return null; // Invalid - must be void return type.
 
 		EClassifier parmType = ((JavaParameter) parms.get(0)).getEType();
 		if (!BeaninfoClassAdapter.this.eventListenerClass.isAssignableFrom(parmType))
 			return null; // Parm must be inherit from EventListener
 
 		// This is a unique containing event name and listener type
 		// This is so we can have a unique key for two events with the same
 		// name but different listener type. (This matches Introspector so that we aren't
 		// coming up with different results.
 		return java.beans.Introspector.decapitalize(name.substring(3, name.length() - 8))
 			+ ':'
 			+ ((JavaHelpers) parmType).getQualifiedName();
 	}
 
 	/*
 	 * Answers event key if valid, null if not valid.
 	 */
 	protected String validEventRemove(Method method) {
 		String name = method.getName();
 		if (!name.startsWith("remove") || !name.endsWith("Listener")) //$NON-NLS-1$ //$NON-NLS-2$
 			return null; // Not valid format for a remove listener name.
 
 		List parms = method.getParameters();
 		if (parms.size() != 1)
 			return null; // Invalid - improper number of parms.
 
 		EClassifier returnType = method.getReturnType();
 		if (returnType == null || !returnType.getName().equals("void")) //$NON-NLS-1$
 			return null; // Invalid - must be void return type.
 
 		EClassifier parmType = ((JavaParameter) parms.get(0)).getEType();
 		if (!BeaninfoClassAdapter.this.eventListenerClass.isAssignableFrom(parmType))
 			return null; // Parm must be inherit from EventListener
 
 		// This is a unique containing event name and listener type
 		// This is so we can have a unique key for two events with the same
 		// name but different listener type. (This matches Introspector so that we aren't
 		// coming up with different results).
 		return java.beans.Introspector.decapitalize(name.substring(6, name.length() - 8))
 			+ ':'
 			+ ((JavaHelpers) parmType).getQualifiedName();
 	}
 
 	public boolean isDefaultBound() {
 		if (defaultBound == null) {
 			// Haven't yet decided on it.
 			Iterator methods = getJavaClass().getPublicMethods().iterator();
 			boolean foundAdd = false, foundRemove = false;
 			while (methods.hasNext() && (!foundAdd || !foundRemove)) {
 				Method method = (Method) methods.next();
 				if ("addPropertyChangeListener".equals(method.getName())) { //$NON-NLS-1$
 					List parms = method.getParameters();
 					if (parms.size() == 1) {
 						JavaParameter parm = (JavaParameter) parms.get(0);
 						if ("java.beans.PropertyChangeListener".equals(((JavaHelpers) parm.getEType()).getQualifiedName())) { //$NON-NLS-1$
 							foundAdd = true;
 							continue;
 						}
 					}
 				} else if ("removePropertyChangeListener".equals(method.getName())) { //$NON-NLS-1$
 					List parms = method.getParameters();
 					if (parms.size() == 1) {
 						JavaParameter parm = (JavaParameter) parms.get(0);
 						if ("java.beans.PropertyChangeListener".equals(((JavaHelpers) parm.getEType()).getQualifiedName())) { //$NON-NLS-1$
 							foundRemove = true;
 							continue;
 						}
 					}
 				}
 			}
 
 			defaultBound = (foundAdd && foundRemove) ? Boolean.TRUE : Boolean.FALSE;
 		}
 		return defaultBound.booleanValue();
 	}
 
 	private class EventInfo {
 		
 		public Method addListenerMethod;
 		public Method removeListenerMethod;
 
 		public void setAdder(Method addMethod) {
 			addListenerMethod = addMethod;
 		}
 
 		public void setRemover(Method removeMethod) {
 			removeListenerMethod = removeMethod;
 		}
 
 		// Answer whether this is a valid event info.
 		public boolean isValidInfo() {
 			return (addListenerMethod != null && removeListenerMethod != null);
 		}
 
 		public boolean createEvent(String name) {
 			EventSetDecorator ed = BeaninfoClassAdapter.this.createEvent(name);
 			if (ed == null)
 				return false; // Reflection not wanted.
 
 
 			// See if unicast.
 			boolean unicast = false;
 			List exceptions = addListenerMethod.getJavaExceptions();
 			int len = exceptions.size();
 			for(int i=0; i<len; i++) {
 				if (exceptions.get(i) == BeaninfoClassAdapter.this.tooManyExceptionClass) {
 					unicast = true;
 					break;
 				}
 			}
 			// We'll let listener methods get retrieved dynamically when needed.
 			BeanInfoDecoratorUtility.setProperties(ed, addListenerMethod, removeListenerMethod, unicast, (JavaClass) ((JavaParameter) addListenerMethod.getParameters().get(0)).getEType());
 			return true;
 		}
 
 	}
 
 	/**
 	 * Marh the factory as stale, but leave the overrides alone. They are not stale.
 	 * @param stale
 	 * 
 	 * @since 1.1.0.1
 	 */
 	public void markStaleFactory(ProxyFactoryRegistry stale) {
 		markStaleFactory(stale, false);
 	}
 	
 	/**
 	 * Mark this factory as the stale factory.
 	 * 
 	 * @param clearOverriddes clear the overrides too. They are stale.
 	 */
 	public void markStaleFactory(ProxyFactoryRegistry stale, boolean clearOverriddes) {
 		if (staleFactory == null) {
 			// It's not stale so make it stale.
 			// So that next access will re-introspect
 			defaultBound = null; // So query on next request.
 			staleFactory = new WeakReference(stale);
 
 			// Need to mark the esuperadapter that things have changed so that any 
 			// subtype will know to reuse the parent for anything that requires knowing parent info.
 			Adapter a = EcoreUtil.getExistingAdapter(getTarget(), ESuperAdapter.class);
 			// Simulate that this objects super has changed. This will make all subclasses
 			// think about the super has changed and next retrieving anything that involves the
 			// super will cause a rebuild to occur.
 			Notification note =
 				new ENotificationImpl((InternalEObject) getTarget(), Notification.SET, EcorePackage.ECLASS__ESUPER_TYPES, null, null);
 			if (a != null)
 				a.notifyChanged(note);
 			// Do the same with BeaninfoSuperAdapter so that events also will be rebuilt.
 			a = EcoreUtil.getExistingAdapter(getTarget(), BeaninfoSuperAdapter.ADAPTER_KEY);
 			if (a != null)
 				a.notifyChanged(note);
 			synchronized (this) {
 				needsIntrospection = true;
 				if (clearOverriddes) {
 					retrievedExtensionDocument = CLEAR_EXTENSIONS;
					if (classEntry != null) {
						classEntry.markDeleted();
						classEntry = null; // Get a new one next time.
					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Form a longname for the addkey function.
 	 */
 	private String formLongName(EOperation feature) {
 		Method mthd = null;
 		if (feature instanceof Method)
 			mthd = (Method) feature;
 		else if (feature instanceof MethodProxy)
 			mthd = ((MethodProxy) feature).getMethod();
 		else
 			return null; // Don't know what it is.
 
 		StringBuffer longName = new StringBuffer(100);
 		longName.append(feature.getName()); // Feature Name
 		longName.append(':');
 		longName.append(mthd.getName()); // Method Name
 		longName.append('(');
 		List p = mthd.getParameters();
 		for (int i = 0; i < p.size(); i++) {
 			JavaParameter parm = (JavaParameter) p.get(i);
 			if (i>0)
 				longName.append(',');
 			longName.append(parm.getJavaType().getQualifiedName());
 		}
 
 		return longName.toString();
 	}
 	
 	/*
 	 * More than one operation can have the same name. To identify them uniquely, the long name is created,
 	 * which is formed from the name and the method signature.
 	 */
 	private String formLongName(MethodRecord record) {
 		StringBuffer longName = new StringBuffer(100);
 		longName.append(record.name); // Feature Name
 		longName.append(':');
 		longName.append(record.methodForDescriptor.methodName); // Method Name
 		longName.append('(');
 		String[] p = record.methodForDescriptor.parameterTypeNames;
 		if (p != null)
 			for (int i = 0; i < p.length; i++) {
 				if (i>0)
 					longName.append(',');
 				longName.append(MapJNITypes.getFormalTypeName(p[i]));
 			}
 
 		return longName.toString();
 	}
 	/**
 	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(Notification)
 	 */
 	public void notifyChanged(Notification msg) {
 		// In case of removing adapter, make sure we are first removed from the factory so it doesn't know about us anymore.
 		if (msg.getEventType() == Notification.REMOVING_ADAPTER)
 			getAdapterFactory().removeAdapter(this);
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return super.toString() + '(' + (getJavaClass() != null ? getJavaClass().getQualifiedName() : "?") + ')'; //$NON-NLS-1$
 	}
 
 
 }
