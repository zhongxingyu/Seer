 package org.eclipse.jem.internal.beaninfo.adapters;
 /*******************************************************************************
  * Copyright (c)  2001, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: BeaninfoClassAdapter.java,v $
 *  $Revision: 1.10 $  $Date: 2004/04/27 15:25:23 $ 
  */
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.text.MessageFormat;
 import java.util.*;
 
 import org.eclipse.core.resources.IResourceStatus;
 import org.eclipse.core.runtime.*;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.*;
 import org.eclipse.emf.ecore.*;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.ESuperAdapter;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 
 import org.eclipse.jem.internal.beaninfo.*;
 import org.eclipse.jem.internal.beaninfo.core.*;
 import org.eclipse.jem.internal.proxy.core.*;
 
 import com.ibm.etools.emf.event.EventFactory;
 import com.ibm.etools.emf.event.EventUtil;
 import org.eclipse.jem.java.*;
 import org.eclipse.jem.internal.java.beaninfo.IIntrospectionAdapter;
 import org.eclipse.jem.java.impl.JavaClassImpl;
 
 /**
  * Beaninfo adapter for doing introspection on a Java Model class.
  */
 
 public class BeaninfoClassAdapter extends AdapterImpl implements IIntrospectionAdapter {
 	
 	/**
 	 * Clear out the introspection because introspection is being closed or removed from the project.
 	 * Don't want anything hanging around that we had done.
 	 */
 	public void clearIntrospection() {
 		if (beaninfo != null) {
 			beaninfo = null;
 		}
 		if (hasIntrospected) {
 			// Clear out the beandecorator if implicitly created.
 			Iterator beanItr = getJavaClass().getEAnnotations().iterator();
 			while (beanItr.hasNext()) {
 				EAnnotation dec = (EAnnotation) beanItr.next();
 				if (dec instanceof BeanDecorator) {
 					BeanDecorator decor = (BeanDecorator) dec;
 					decor.setDescriptorProxy(null);
 					decor.setDecoratorProxy(null);
 					if (decor.isImplicitlyCreated() == BeanDecorator.IMPLICIT_DECORATOR) {
 						beanItr.remove();
 						((InternalEObject) decor).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						break;
 					}
 				}
 			}
 		}
 	
 		if (hasIntrospectedProperties) {
 			// Clear out the features that we implicitly created.
 			Iterator propItr = getJavaClass().getEStructuralFeaturesGen().iterator();
 			while (propItr.hasNext()) {
 				EStructuralFeature prop = (EStructuralFeature) propItr.next();
 				Iterator pdItr = prop.getEAnnotations().iterator();
 				while (pdItr.hasNext()) {
 					EAnnotation dec = (EAnnotation) pdItr.next();
 					if (dec instanceof PropertyDecorator) {
 						PropertyDecorator pd = (PropertyDecorator) dec;
 						pd.setDescriptorProxy(null);
 						pd.setDecoratorProxy(null);
 						if (pd.isImplicitlyCreated() != PropertyDecorator.NOT_IMPLICIT) {
 							pdItr.remove(); // Remove it from the property.
 							 ((InternalEObject) pd).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						}
 						if (pd.isImplicitlyCreated() == PropertyDecorator.IMPLICIT_DECORATOR_AND_FEATURE) {
 							propItr.remove(); // Remove the feature itself
 							 ((InternalEObject) prop).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						}
 						break;
 					}
 				}
 			}
 	
 			hasIntrospectedProperties = false;
 		}
 	
 		if (hasIntrospectedOperations) {
 			// Clear out the operations that we implicitly created.
 			Iterator operItr = getJavaClass().getEOperationsGen().iterator();
 			while (operItr.hasNext()) {
 				EOperation oper = (EOperation) operItr.next();
 				Iterator mdItr = oper.getEAnnotations().iterator();
 				while (mdItr.hasNext()) {
 					EAnnotation dec = (EAnnotation) mdItr.next();
 					if (dec instanceof MethodDecorator) {
 						MethodDecorator md = (MethodDecorator) dec;
 						md.setDescriptorProxy(null);
 						md.setDecoratorProxy(null);
 						if (md.isImplicitlyCreated() != MethodDecorator.NOT_IMPLICIT) {
 							mdItr.remove(); // Remove it from the operation.
 							 ((InternalEObject) md).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						}
 						if (md.isImplicitlyCreated() == MethodDecorator.IMPLICIT_DECORATOR_AND_FEATURE) {
 							operItr.remove(); // Remove the oepration itself
 							 ((InternalEObject) oper).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						}
 						break;
 					}
 				}
 			}
 	
 			hasIntrospectedOperations = false;
 		}
 		
 		if (hasIntrospectedEvents) {
 			// Clear out the eventsthat we implicitly created.
 			Iterator evtItr = getJavaClass().getEventsGen().iterator();
 			while (evtItr.hasNext()) {
 				JavaEvent evt = (JavaEvent) evtItr.next();
 				Iterator edItr = evt.getEAnnotations().iterator();
 				while (edItr.hasNext()) {
 					EAnnotation dec = (EAnnotation) edItr.next();
 					if (dec instanceof EventSetDecorator) {
 						EventSetDecorator ed = (EventSetDecorator) dec;
 						ed.setDescriptorProxy(null);
 						ed.setDecoratorProxy(null);
 						if (ed.isImplicitlyCreated() != EventSetDecorator.NOT_IMPLICIT) {
 							edItr.remove(); // Remove it from the event.
 							 ((InternalEObject) ed).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						}
 						if (ed.isImplicitlyCreated() == EventSetDecorator.IMPLICIT_DECORATOR_AND_FEATURE) {
 							evtItr.remove(); // Remove the event itself
 							 ((InternalEObject) evt).eSetProxyURI(BAD_URI); // Mark it as bad proxy so we know it is no longer any use.
 						}
 						break;
 					}
 				}
 			}
 	
 			hasIntrospectedEvents = false;
 		}		
 	}
 	
 	private void clearAll() {
 		if (beaninfo != null) {
 			beaninfo = null;
 		}
 		// Clear out the annotations.
 		getJavaClass().getEAnnotations().clear();
 		// Clear out the attributes.
 		getJavaClass().getEStructuralFeaturesGen().clear();	
 		// Clear out the operations.
 		getJavaClass().getEOperationsGen().clear();
 		// Clear out the events.
 		getJavaClass().getEventsGen().clear();
 	}
 	
 
 	/**
 	 * @version 	1.0
 	 * @author
 	 */
 
 	protected static final URI BAD_URI = URI.createURI("baduri"); //$NON-NLS-1$
 	// A URI that will never resolve. Used to mark an object as no longer valid.
 	protected boolean hasIntrospected;
 	protected boolean isIntrospecting;
 
 	protected boolean hasIntrospectedProperties;
 	protected boolean isIntrospectingProperties;
 
 	protected boolean isDoingAllProperties;
 
 	protected boolean hasIntrospectedOperations;
 	protected boolean isIntrospectingOperations;
 
 	protected boolean isDoingAllOperations;
 
 	protected boolean hasIntrospectedEvents;
 	protected boolean isIntrospectingEvents;
 
 	protected boolean isDoingAllEvents;
 
 	protected final static int
 		NEVER_RETRIEVED_EXTENSION_DOCUMENT = 0,
 		RETRIEVED_ROOT_ONLY = 1,
 		RETRIEVED_FULL_DOCUMENT = 2;
 	protected int retrievedExtensionDocument = NEVER_RETRIEVED_EXTENSION_DOCUMENT;
 
 	protected IBeanProxy beaninfo;
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
 			List localFeatures = (List) getJavaClass().getEStructuralFeaturesGen();
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
 			featuresRealList = (List) getJavaClass().getEStructuralFeaturesGen();
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
 			List locals = (List) getJavaClass().getEOperationsGen();
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
 			operationsRealList = getJavaClass().getEOperationsGen();
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
 			List locals = (List) getJavaClass().getEventsGen();
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
 		if (!hasIntrospected && !isIntrospecting) {
 			try {
 				introspect();
 			} catch (Throwable e) {
 				hasIntrospected = false;
 				BeaninfoPlugin.getPlugin().getLogger().log(
 					new Status(
 						IStatus.WARNING,
 						BeaninfoPlugin.getPlugin().getDescriptor().getUniqueIdentifier(),
 						0,
 						MessageFormat.format(
 							BeaninfoPlugin.getPlugin().getDescriptor().getResourceString(BeaninfoProperties.INTROSPECTFAILED),
 							new Object[] { getJavaClass().getJavaName(), ""}),
 						e));
 			}
 		}
 	}
 
 	public void introspect() {
 		isIntrospecting = true;
 		try {
 			if (isResourceConnected()) {
 				// See if are valid kind of class.
 				if (getJavaClass().getKind() == TypeKind.UNDEFINED_LITERAL) {
 					// Not valid, don't let any further introspection occur.
 					if (retrievedExtensionDocument == RETRIEVED_FULL_DOCUMENT) {
 						// We've been defined at one point. Need to clear everything and step back
 						// to never retrieved so that we now get the root added in. If we had been
 						// previously defined, then we didn't have root. We will have to lose
 						// all other updates too. But they can come back when we're defined.
 						clearAll();
 						retrievedExtensionDocument = NEVER_RETRIEVED_EXTENSION_DOCUMENT;
 					}
 					if (retrievedExtensionDocument == NEVER_RETRIEVED_EXTENSION_DOCUMENT)
 						applyExtensionDocument(true);	// Add in Root stuff so that it will work correctly even though undefined.					
 					hasIntrospected = hasIntrospectedOperations = hasIntrospectedProperties = hasIntrospectedEvents = true;
 				} else {
 					if (retrievedExtensionDocument == RETRIEVED_ROOT_ONLY) {
 						// We need to clear out EVERYTHING because we are coming from an undefined to a defined.
 						// Nothing previous is now valid. (Particually the root stuff).
 						clearAll();
 					}
 					if (retrievedExtensionDocument != RETRIEVED_FULL_DOCUMENT)
 						applyExtensionDocument(false); // Apply the extension doc before we do anything.
 					BeanDecorator decor = Utilities.getBeanDecorator(getJavaClass());
 					if (decor == null || decor.isDoBeaninfo()) {
 						IBeanTypeProxy targetType = null;
 						ProxyFactoryRegistry registry = getRegistry();
 						if (registry != null && registry.isValid())
 							targetType =
 								registry.getBeanTypeProxyFactory().getBeanTypeProxy(getJavaClass().getQualifiedNameForReflection());
 						if (targetType != null) {
 							if (targetType.getInitializationError() == null) {
 								// If an exception is thrown, treat this as no proxy, however log it because we
 								// shouldn't have exceptions during introspection, but if we do it should be logged
 								// so it can be corrected.
 								try {
 									beaninfo =
 										getProxyConstants().getIntrospectProxy().invoke(
 											null,
 											new IBeanProxy[] { targetType, getRegistry().getBeanProxyFactory().createBeanProxyWith(false)});
 								} catch (ThrowableProxy e) {
 									BeaninfoPlugin.getPlugin().getLogger().log(
 										new Status(
 											IStatus.WARNING,
 											BeaninfoPlugin.getPlugin().getDescriptor().getUniqueIdentifier(),
 											0,
 											MessageFormat.format(
 												BeaninfoPlugin.getPlugin().getDescriptor().getResourceString(
 													BeaninfoProperties.INTROSPECTFAILED),
 												new Object[] { getJavaClass().getJavaName(), ""}),
 											e));
 								}
 							} else {
 								// The class itself couldn't be initialized. Just log it, but treat as no proxy.
 								BeaninfoPlugin.getPlugin().getLogger().log(
 									new Status(
 										IStatus.WARNING,
 										BeaninfoPlugin.getPlugin().getDescriptor().getUniqueIdentifier(),
 										0,
 										MessageFormat.format(
 											BeaninfoPlugin.getPlugin().getDescriptor().getResourceString(
 												BeaninfoProperties.INTROSPECTFAILED),
 											new Object[] { getJavaClass().getJavaName(), targetType.getInitializationError()}),
 										null));
 							}
 						} else {
 							// The class itself could not be found. Just log it, but treat as no proxy.
 							BeaninfoPlugin.getPlugin().getLogger().log(
 								new Status(
 									IStatus.INFO,
 									BeaninfoPlugin.getPlugin().getDescriptor().getUniqueIdentifier(),
 									0,
 									MessageFormat.format(
 										BeaninfoPlugin.getPlugin().getDescriptor().getResourceString(BeaninfoProperties.INTROSPECTFAILED),
 										new Object[] { getJavaClass().getJavaName(), "Class not found"}),
 									null));
 						}
 					}
 					calculateBeanDescriptor(decor);
 					hasIntrospected = true;
 				}
 				getAdapterFactory().registerIntrospection(getJavaClass().getQualifiedNameForReflection(), this);
 			}
 		} finally {
 			isIntrospecting = false;
 		}
 	}
 
 	private static final String OVERRIDE_EXTENSION = ".override";	//$NON-NLS-1$
 	private static final String ROOT = "..ROOT.."; //$NON-NLS-1$
 	private static final String ROOT_OVERRIDE = "..ROOT.."+OVERRIDE_EXTENSION;	 //$NON-NLS-1$
 	
 	protected void applyExtensionDocument(boolean rootOnly) {
 		boolean alreadyRetrievedRoot = retrievedExtensionDocument == RETRIEVED_ROOT_ONLY;
 		retrievedExtensionDocument = rootOnly ? RETRIEVED_ROOT_ONLY : RETRIEVED_FULL_DOCUMENT;
 		JavaClass jc = getJavaClass();
 		Resource mergeIntoResource = jc.eResource();
 		ResourceSet rset = mergeIntoResource.getResourceSet();
 		String className = getJavaClass().getName();
 		getRegistry();	// Need to have a registry to get override paths initialized correctly. There is a possibility that overrides asked before any registry created.
 		if (!alreadyRetrievedRoot && (rootOnly || jc.getSupertype() == null)) {
 			// It is a root class. Need to merge in root stuff.
 			applyExtensionDocTo(rset, jc, ROOT_OVERRIDE, ROOT, ROOT);
 			if (rootOnly)
 				return;
 		}
 
 		String baseOverridefile = className + OVERRIDE_EXTENSION; // getName() returns inner classes with "$" notation, which is good. //$NON-NLS-1$
 		applyExtensionDocTo(rset, jc, baseOverridefile, getJavaClass().getJavaPackage().getPackageName(), className);
 	}
 	
 	protected void applyExtensionDocTo(final ResourceSet rset, final EObject mergeIntoEObject, final String overrideFile, String packageName, String className) {
 		BeaninfoPlugin.getPlugin().applyOverrides(getAdapterFactory().getProject(), packageName, className, rset, 
 			new BeaninfoPlugin.IOverrideRunnable() {
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
 								BeaninfoPlugin.getPlugin().getLogger().log(new Status(IStatus.WARNING, BeaninfoPlugin.PI_BEANINFO_PLUGINID, 0, "Error loading file\"" + overridePath + "\"", e.exception())); //$NON-NLS-1$ //$NON-NLS-2$						
 							}
 						}
 						// In case it happened after creating resource but during load. Need to get rid of it in the finally.	
 						overrideRes = rset.getResource(uri, false);				
 					} catch (Exception e) {
 						// Couldn't load it for some reason.
 						BeaninfoPlugin.getPlugin().getLogger().log(new Status(IStatus.WARNING, BeaninfoPlugin.PI_BEANINFO_PLUGINID, 0, "Error loading file\"" + overridePath + "\"", e)); //$NON-NLS-1$ //$NON-NLS-2$
 						overrideRes = rset.getResource(uri, false); // In case it happened after creating resource but during load.
 			
 					} finally {
 						if (overrideRes != null)
 							rset.getResources().remove(overrideRes); // We don't need it around once we do the merge.
 					}
 				}
 				
 				/* (non-Javadoc)
 				 * @see org.eclipse.jem.internal.beaninfo.core.BeaninfoPlugin.IOverrideRunnable#run(org.eclipse.emf.ecore.resource.Resource)
 				 */
 				public void run(Resource overrideRes) {
 					try {
 						EventUtil util = EventFactory.eINSTANCE.createEventUtil(mergeIntoEObject, rset);
 						util.doForwardEvents(overrideRes.getContents());
 					} catch (WrappedException e) {
 						BeaninfoPlugin.getPlugin().getLogger().log(new Status(IStatus.WARNING, BeaninfoPlugin.PI_BEANINFO_PLUGINID, 0, "Error processing file\"" + overrideRes.getURI() + "\"", e.exception())); //$NON-NLS-1$ //$NON-NLS-2$						
 					}
 				}
 			});
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
 		introspectProperties();
 		return getJavaClass().getEStructuralFeaturesGen();
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
 		return introspectOperations();
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
 		return introspectEvents();
 	}
 	
 	/**
 	 * @see org.eclipse.jem.java.beaninfo.IIntrospectionAdapter#getAllEvents()
 	 */
 	public EList getAllEvents() {
 		return allEvents();
 	}					
 
 	/**
 	 * Fill in the BeanDescriptorDecorator.
 	 */
 	protected void calculateBeanDescriptor(BeanDecorator decor) {
 		// If there already is one, then we
 		// will use it. This allows merging with beanfinfo.
 		// If this was an implicit one left over from a previous introspection before going stale,
 		// this is OK because we always want a bean decorator from introspection. We will reuse it.
 
 		if (decor == null) {
 			decor = BeaninfoFactory.eINSTANCE.createBeanDecorator();
 			decor.setImplicitlyCreated(BeanDecorator.IMPLICIT_DECORATOR);
 			getJavaClass().getEAnnotations().add(decor);
 		}
 
 		if (beaninfo != null && decor.isMergeIntrospection()) {
 			decor.setDescriptorProxy(getProxyConstants().getBeanDescriptorProxy().invokeCatchThrowableExceptions(beaninfo));
 			decor.setMergeSuperPropertiesProxy(
 				((IBooleanBeanProxy) getProxyConstants().getIsMergeInheritedPropertiesProxy().invokeCatchThrowableExceptions(beaninfo))
 					.getBooleanValue());
 			decor.setMergeSuperBehaviorsProxy(
 				((IBooleanBeanProxy) getProxyConstants().getIsMergeInheritedMethodsProxy().invokeCatchThrowableExceptions(beaninfo))
 					.getBooleanValue());
 			decor.setMergeSuperEventsProxy(
 				((IBooleanBeanProxy) getProxyConstants().getIsMergeInheritedEventsProxy().invokeCatchThrowableExceptions(beaninfo))
 					.getBooleanValue());					
 		} else {
 			decor.setDescriptorProxy(null);
 			decor.setMergeSuperEventsProxy(null);
 			decor.setMergeSuperPropertiesProxy(null);
 			decor.setMergeSuperBehaviorsProxy(null);
 		}
 		decor.setDecoratorProxy(null);
 	}
 
 	/**
 	 * introspect the Properties
 	 */
 	protected void introspectProperties() {
 		introspectIfNecessary();
 		if (!isIntrospecting && !isIntrospectingProperties && !hasIntrospectedProperties) {
 			isIntrospectingProperties = true;
 			try {
 				if (isResourceConnected()) {
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					if (bd == null || bd.isIntrospectProperties()) {
 						// bd wants properties to be introspected/reflected
 						if (beaninfo != null) {
 							IArrayBeanProxy props =
 								(IArrayBeanProxy) getProxyConstants().getPropertyDescriptorsProxy().invokeCatchThrowableExceptions(
 									beaninfo);
 							if (props != null) {
 								int propSize = props.getLength();
 								for (int i = 0; i < propSize; i++)
 									calculateProperty(props.getCatchThrowableException(i));
 							}
 						} else
 							reflectProperties(); // No beaninfo, so use reflection to create properties
 					}
 
 					// Now go through the list and remove those that should be removed.
 					Map oldLocals = getPropertiesMap();
 					Iterator itr = getFeaturesList().iterator();
 					while (itr.hasNext()) {
 						EStructuralFeature a = (EStructuralFeature) itr.next();
 						PropertyDecorator p = Utilities.getPropertyDecorator(a);
 						Object aOld = oldLocals.get(a.getName());
 						if (aOld != null && aOld != Boolean.FALSE) {
 							// A candidate for removal. It was in the old list and it was not processed.
 							if (p != null) {
 								int implicit = p.isImplicitlyCreated();
 								if (implicit != PropertyDecorator.NOT_IMPLICIT) {
 									p.setEModelElement(null); // Remove from the feature;
 									((InternalEObject) p).eSetProxyURI(BAD_URI);
 									// Mark it as bad proxy so we know it is no longer any use.
 									p = null;
 								}
 
 								if (implicit == PropertyDecorator.IMPLICIT_DECORATOR_AND_FEATURE) {
 									itr.remove(); // Remove it, this was implicitly created and not processed this time.
 									((InternalEObject) a).eSetProxyURI(BAD_URI);
 									// Mark it as bad proxy so we know it is no longer any use.
 								}
 							}
 						}
 					}
 				}
 				hasIntrospectedProperties = true;
 			} finally {
 				isIntrospectingProperties = false;
 				propertiesMap = null; // Get rid of accumulated map.
 				featuresRealList = null; // Release the real list.
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
 		
 		UniqueEList allProperties = new UniqueEList() {
 			protected Object[] newData(int capacity) {
 				return new EStructuralFeature[capacity];
 			}
 
 			protected boolean useEquals() {
 				return false;
 			}
 
 		};
 		if (!isIntrospecting && !isDoingAllProperties && isResourceConnected()) {
 			isDoingAllProperties = true;
 			try {
 				EList localProperties = getJavaClass().getProperties();
 				JavaClass superType = getJavaClass().getSupertype();
 				if (superType != null) {
 					// Now we need to merge in the supers.
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					List supers = superType.getAllProperties();					
 
 					// We will now merge as directed.
 					boolean mergeAll = bd == null || bd.isMergeSuperProperties();
 					if (!mergeAll) {
 						// we don't to want to merge super properties, but we still need super non-properties or explict ones.
 						int len = supers.size();
 						for (int i = 0; i < len; i++) {
 							EStructuralFeature p = (EStructuralFeature) supers.get(i);
 							PropertyDecorator pd = Utilities.getPropertyDecorator(p);
 							if ( pd == null || (pd.isImplicitlyCreated() == FeatureDecorator.NOT_IMPLICIT && !pd.isMergeIntrospection()))
 								allProperties.add(p);
 						}
 					} else {
 						// We want to merge all.					
 						if (beaninfo != null) {
 							// BeanInfo has given us the merge list. If the list is empty, then we accept all.
 							IArrayBeanProxy superNames = (IArrayBeanProxy) getProxyConstants().getInheritedPropertyDescriptorsProxy().invokeCatchThrowableExceptions(beaninfo);
 							if (superNames != null) {
 								// We were given a list of names.
 								// Get the names into a set to create a quick lookup.
 								int l = superNames.getLength();
 								HashSet superSet = new HashSet(l);
 								for (int i = 0; i < l; i++) {
 									superSet.add(((IStringBeanProxy) superNames.getCatchThrowableException(i)).stringValue());
 								}
 								
 								// Now walk and add in non-bean properties (and bean properties that were explicitly added and not mergeable (i.e. didn't come thru beaninfo)) 
 								// and those specifically called out by BeanInfo.
 								int len = supers.size();
 								for (int i = 0; i < len; i++) {
 									EStructuralFeature p = (EStructuralFeature) supers.get(i);
 									PropertyDecorator pd = Utilities.getPropertyDecorator(p);
 									if (pd == null || (pd.isImplicitlyCreated() == FeatureDecorator.NOT_IMPLICIT && !pd.isMergeIntrospection()) || superSet.contains(pd.getName()))
 										allProperties.add(p);
 								}							
 							} else {
 								// BeanInfo called out that all super properties are good
 								allProperties.addAll(supers);
 							}
 						} else {
 							// We don't have a BeanInfo telling us how to merge. This means we're reflecting and have prevented
 							// dups. So we accept all supers.
 							allProperties.addAll(supers);
 						}
 					}
 				}
 				allProperties.addAll(localProperties);				
 				superAdapter.setAllPropertiesCollectionModified(false); // Now built, so reset to not changed.
 			} finally {
 				isDoingAllProperties = false;
 			}
 		}
 
 		allProperties.shrink();
 		return new EcoreEList.UnmodifiableEList(
 			getJavaClass(),
 			null,
 			allProperties.size(),
 			allProperties.data());
 	}
 
 	/**
 	 * Fill in the property and its decorator using the propDesc.
 	 */
 	protected void calculateProperty(IBeanProxy propDesc) {
 		// See if this is an indexed property. If it is, then a few fields will not be set in here, but
 		// will instead be set by the calculateIndexedProperty, which will be called.
 		boolean indexed = propDesc.getProxyFactoryRegistry().getBeanTypeProxyFactory().getBeanTypeProxy("java.beans.IndexedPropertyDescriptor").equals(propDesc.getTypeProxy()); //$NON-NLS-1$
 		String name = ((IStringBeanProxy) getProxyConstants().getNameProxy().invokeCatchThrowableExceptions(propDesc)).stringValue();
 		boolean changeable;
 		EClassifier type = null;
 
 		changeable = getProxyConstants().getWriteMethodProxy().invokeCatchThrowableExceptions(propDesc) != null;
 		IBeanTypeProxy typeProxy = (IBeanTypeProxy) getProxyConstants().getPropertyTypeProxy().invokeCatchThrowableExceptions(propDesc);
 		if (typeProxy != null)
 			type = Utilities.getJavaClass(typeProxy, getJavaClass().eResource().getResourceSet());
 
 		if (indexed) {
 			// If no array write method found, then see if there is an indexed write method. If there is, then it is changable.
 			if (!changeable)
 				changeable = getProxyConstants().getIndexedWriteMethodProxy().invokeCatchThrowableExceptions(propDesc) != null;
 			if (typeProxy == null) {
 				// If no array type proxy from above, create one from the indexed type proxy.
 				typeProxy = (IBeanTypeProxy) getProxyConstants().getIndexedPropertyTypeProxy().invokeCatchThrowableExceptions(propDesc);
 				if (typeProxy != null) {
 					typeProxy = typeProxy.getProxyFactoryRegistry().getBeanTypeProxyFactory().getBeanTypeProxy(typeProxy.getTypeName(), 1);
 					type = Utilities.getJavaClass(typeProxy, getJavaClass().eResource().getResourceSet());
 				}
 			}
 		}
 
 		if (type != null)
 			createProperty(name, indexed, changeable, type, propDesc); // A valid property descriptor.
 	}
 
 	/**
 	 * Fill in the property and its decorator using the passed in information.
 	 */
 	public PropertyDecorator createProperty(String name, boolean indexed, boolean changeable, EClassifier type, IBeanProxy propDesc) {
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
 		if (pd != null && pd.isImplicitlyCreated() == FeatureDecorator.NOT_IMPLICIT) {
 			// We can't change the type for explicit.
 			indexed = pd instanceof IndexedPropertyDecorator;
 		} else if (
 			pd != null
 				&& pd.isImplicitlyCreated() != FeatureDecorator.NOT_IMPLICIT
 				&& ((indexed && !(pd instanceof IndexedPropertyDecorator)) || (!indexed && pd instanceof IndexedPropertyDecorator))) {
 			prop.getEAnnotations().remove(pd);
 			pd = null;
 		}
 
 		int implicit = pd == null ? FeatureDecorator.IMPLICIT_DECORATOR : pd.isImplicitlyCreated();
 		if (prop == null) {
 			// We will create a new property.
 			// We can't have an implicit feature, but an explicit decorator.
 			getFeaturesList().add(prop = EcoreFactory.eINSTANCE.createEReference());
 			implicit = FeatureDecorator.IMPLICIT_DECORATOR_AND_FEATURE;
 		}
 
 		// Now fill it in. Normal id for an attribute is "classname.attributename" but we can't use that
 		// for us because that format is used by Java Core for a field and there would be confusion.
 		// So we will use '/' instead.
 		 ((XMIResource) prop.eResource()).setID(prop, getJavaClass().getName() + BeaninfoJavaReflectionKeyExtension.FEATURE + name);
 		prop.setName(name);
 		prop.setTransient(false);
 		prop.setVolatile(false);
 		prop.setChangeable(changeable);
 
 		// Containment and Unsettable is tricky for EReferences. There is no way to know whether it has been explicitly set to false, or it defaulted to
 		// false because ECore has not made containment/unsettable an unsettable feature. So we need to instead use the algorithm of if we here 
 		// created the feature, then we will by default set it to containment/unsettable. If it was created through diff merge, then
 		// we will leave it alone. It is the responsibility of the merge file writer to set containment/unsettable correctly.
 		if (implicit == FeatureDecorator.IMPLICIT_DECORATOR_AND_FEATURE) {
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
 			pd.setImplicitlyCreated(implicit);
 			prop.getEAnnotations().add(pd);
 		}
 		pd.setDescriptorProxy(propDesc);
 		pd.setDecoratorProxy(null);
 		return pd;
 	}
 
 	/**
 	 * Reflect the properties. This requires going through local methods and matching them up to
 	 * see if they are properties.
 	 */
 	protected void reflectProperties() {
 		// If we are set to mergeSuperTypeProperties, then we need to get the super properties.
 		// This is so that duplicate any from super that we find here. When reflecting we don't
 		// allow discovered duplicates unless they are different types.
 		HashMap supers = new HashMap(50);
 		BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 		if (bd.isMergeSuperProperties()) {
 			JavaClass superType = getJavaClass().getSupertype();
 			if (superType != null) {
 				Iterator superAllItr = superType.getAllProperties().iterator();
 				while (superAllItr.hasNext()) {
 					EStructuralFeature sf = (EStructuralFeature) superAllItr.next();
 					supers.put(sf.getName(), sf);
 				}
 				// Kludge: The above requests for super properties could of caused a recycle (in case the super
 				// was stale). Because of this we need to reintrospect to mark ourselves as not stale.
 				introspectIfNecessary();
 			}
 		}
 
 		// If any of the classes in the hierarchy are bound, then all reflected properties are considered bound.
 		boolean isBound = isDefaultBound();
 		if (!isBound) {
 			List superTypes = getJavaClass().getEAllSuperTypes();
 			ListIterator sprs = superTypes.listIterator(superTypes.size());
 			// Start from end because that will be first class above the this one.
 			while (sprs.hasPrevious() && !isBound) {
 				JavaClass spr = (JavaClass) sprs.previous();
 				BeaninfoClassAdapter bi = (BeaninfoClassAdapter) EcoreUtil.getExistingAdapter(spr, IIntrospectionAdapter.ADAPTER_KEY);
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
 				if (name.length() == 0)
 					continue;	// Had get(...) and not getXXX(...) so not a getter.
 				PropertyInfo propInfo = (PropertyInfo) props.get(name);
 				if (propInfo == null) {
 					propInfo = new PropertyInfo();
 					if (propInfo.setGetter(mthd, false))
 						props.put(name, propInfo);
 				} else
 					propInfo.setGetter(mthd, false);
 			} else if (mthd.getName().startsWith("is")) { //$NON-NLS-1$
 				String name = java.beans.Introspector.decapitalize(mthd.getName().substring(2));
 				if (name.length() == 0)
 					continue;	// Had is(...) and not isXXX(...) so not a getter.				
 				PropertyInfo propInfo = (PropertyInfo) props.get(name);
 				if (propInfo == null) {
 					propInfo = new PropertyInfo();
 					if (propInfo.setGetter(mthd, true))
 						props.put(name, propInfo);
 				} else
 					propInfo.setGetter(mthd, true);
 			} else if (mthd.getName().startsWith("set")) { //$NON-NLS-1$
 				String name = java.beans.Introspector.decapitalize(mthd.getName().substring(3));
 				if (name.length() == 0)
 					continue;	// Had set(...) and not setXXX(...) so not a setter.				
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
 			EStructuralFeature sf = (EStructuralFeature) supers.get(entry.getKey());
 			// Create it if the sf is not a super.
 			if (sf == null)
 				 ((PropertyInfo) entry.getValue()).createProperty((String) entry.getKey(), isBound);
 		}
 	}
 
 	private class PropertyInfo {
 		
 		public EClassifier type, indexedType;
 		public boolean constrained;
 		public Method getter, setter, indexedGetter, indexedSetter;
 
 		public boolean setGetter(Method get, boolean mustBeBoolean) {
 			List parms = (List) get.getParameters();
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
 			List parms = (List) set.getParameters();
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
 					type,
 					null);
 			if (prop == null)
 				return; // Reflection not wanted.
 
 			indexed = prop instanceof IndexedPropertyDecorator; // It could of been forced back to not indexed if explicitly set.
 
 			// Now fill in the property decorator info. If our decorator was not implicit, then we need
 			// to use a proxy so that the use can override specific settings while we supply the defaults.
 			// If our decorator was implicitly created, then we know that there are no user data and we
 			// can use our decorator directly.
 			if (prop.isImplicitlyCreated() == FeatureDecorator.NOT_IMPLICIT) {
 				PropertyDecorator propProxy =
 					indexed
 						? BeaninfoFactory.eINSTANCE.createIndexedPropertyDecorator()
 						: BeaninfoFactory.eINSTANCE.createPropertyDecorator();
 				// Create a proxy.
 				prop.setDecoratorProxy(propProxy);
 				prop = propProxy;
 			}
 
 			prop.setBound(isBound);
 			prop.setConstrained(constrained);
 			if (getter != null)
 				prop.setReadMethod(getter);
 			else
 				prop.eUnset(BeaninfoPackage.eINSTANCE.getPropertyDecorator_ReadMethod());
 			if (setter != null)
 				prop.setWriteMethod(setter);
 			else
 				prop.eUnset(BeaninfoPackage.eINSTANCE.getPropertyDecorator_WriteMethod());
 			if (indexed) {
 				IndexedPropertyDecorator iprop = (IndexedPropertyDecorator) prop;
 				if (indexedGetter != null)
 					iprop.setIndexedReadMethod(indexedGetter);
 				else
 					iprop.eUnset(BeaninfoPackage.eINSTANCE.getIndexedPropertyDecorator_IndexedReadMethod());
 				if (indexedSetter != null)
 					iprop.setIndexedWriteMethod(indexedSetter);
 				else
 					iprop.eUnset(BeaninfoPackage.eINSTANCE.getIndexedPropertyDecorator_IndexedReadMethod());
 			}
 
 		}
 	};
 
 	/**
 	 * introspect the behaviors (methods)
 	 */
 	protected EList introspectOperations() {
 		introspectIfNecessary();
 		if (!isIntrospecting && !isIntrospectingOperations && !hasIntrospectedOperations) {
 			isIntrospectingOperations = true;
 			try {
 				if (isResourceConnected()) {
 					newoperations = new HashSet(50);
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					if (bd == null || bd.isIntrospectBehaviors()) {
 						// bd wants behaviors to be introspected/reflected
 						if (beaninfo != null) {
 							IArrayBeanProxy mthds =
 								(IArrayBeanProxy) getProxyConstants().getMethodDescriptorsProxy().invokeCatchThrowableExceptions(beaninfo);
 							if (mthds != null) {
 								int mthdSize = mthds.getLength();
 								for (int i = 0; i < mthdSize; i++)
 									calculateOperation(mthds.getCatchThrowableException(i));
 							}
 						} else
 							reflectOperations(); // No beaninfo, so use reflection to create behaviors
 					}
 
 					// Now go through the list and remove those that should be removed.
 					Iterator itr = getOperationsList().iterator();
 					while (itr.hasNext()) {
 						EOperation a = (EOperation) itr.next();
 						MethodDecorator m = Utilities.getMethodDecorator(a);
 						if (!newoperations.contains(a)) {
 							// A candidate for removal. It is in the list but we didn't add it. Check to see if it one we had created in the past.
 							// If no methoddecorator, then keep it, not one ours.
 							if (m != null) {
 								int implicit = m.isImplicitlyCreated();
 								if (implicit != MethodDecorator.NOT_IMPLICIT) {
 									m.setEModelElement(null); // Remove it because it was implicit.
 									 ((InternalEObject) m).eSetProxyURI(BAD_URI);
 									m = null;
 								}
 								if (implicit == MethodDecorator.IMPLICIT_DECORATOR_AND_FEATURE) {
 									itr.remove(); // The feature was implicit too.
 									 ((InternalEObject) a).eSetProxyURI(BAD_URI);
 									a = null;
 								}
 							}
 						}
 					}
 					hasIntrospectedOperations = true;
 				}
 			} finally {
 				isIntrospectingOperations = false;
 				operationsMap = null; // Clear out the temp lists.
 				operationsRealList = null;
 				newoperations = null;
 
 			}
 		}
 		return getJavaClass().getEOperationsGen();
 	}
 
 	/**
 	 * Fill in the behavior and its decorator using the mthdDesc.
 	 */
 	protected void calculateOperation(IBeanProxy operDesc) {
 		String name = ((IStringBeanProxy) getProxyConstants().getNameProxy().invokeCatchThrowableExceptions(operDesc)).stringValue();
 		createOperation(name, formLongName(name, operDesc), null, operDesc);
 	}
 
 	/**
 	 * Fill in the behavior and its decorator using the passed in information.
 	 */
 	public MethodDecorator createOperation(String name, String longName, Method method, IBeanProxy mthdDesc) {
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
 			// No method sent, create a proxy to it.
 			method = JavaRefFactory.eINSTANCE.createMethod();
 			URI uri = Utilities.getMethodURI((IMethodProxy) getProxyConstants().getMethodProxy().invokeCatchThrowableExceptions(mthdDesc));
 			((InternalEObject) method).eSetProxyURI(uri);
 		}
 
 		int implicit = md == null ? FeatureDecorator.IMPLICIT_DECORATOR : FeatureDecorator.NOT_IMPLICIT;
 		if (oper == null) {
 			// We will create a new MethodProxy.
 			oper = BeaninfoFactory.eINSTANCE.createMethodProxy();
 			getOperationsList().add(oper);
 			implicit = FeatureDecorator.IMPLICIT_DECORATOR_AND_FEATURE; 
 		}		
 		if (name == null)
 			name = method.getName();		
 
 		// Now fill it in.
 		if (oper instanceof MethodProxy)
 			 ((MethodProxy) oper).setMethod(method);
 		((XMIResource) oper.eResource()).setID(oper, getJavaClass().getName() + BeaninfoJavaReflectionKeyExtension.BEHAVIOR + name);
 		oper.setName(name);
 		newoperations.add(oper);
 
 		// Now create/fill in the method decorator for it.
 		// If there already is one then we
 		// will use it. This allows merging with beaninfo.		
 		if (md == null) {
 			md = BeaninfoFactory.eINSTANCE.createMethodDecorator();
 			md.setImplicitlyCreated(implicit);
 			oper.getEAnnotations().add(md);
 		}
 		md.setDescriptorProxy(mthdDesc);
 		md.setDecoratorProxy(null);
 		return md;
 	}
 
 	/**
 	 * Reflect the behaviors. This requires going through local public methods and creating a 
 	 * method proxy for it.
 	 */
 	protected void reflectOperations() {
 		// If we are set to mergeSuperTypeBehaviors, then we need to get the super behaviors.
 		// This is so that duplicate any from super that we find here. When reflecting we don't
 		// allow discovered duplicates unless they are different signatures. So all super operations
 		// will be allowed and we will not override them.
 		HashMap supers = new HashMap(50);
 		BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 		if (bd.isMergeSuperBehaviors()) {
 			EClass superType = getJavaClass().getSupertype();
 			if (superType != null) {
 				Iterator superAllItr = superType.getEAllOperations().iterator();
 				while (superAllItr.hasNext()) {
 					EOperation op = (EOperation) superAllItr.next();
 					supers.put(formLongName(op), op);
 				}
 				// Kludge: The above requests for super properties could of caused a recycle (in case the super
 				// was stale). Because of this we need to reintrospect to mark ourselves as not stale.
 				introspectIfNecessary();
 			}
 		}
 
 		Iterator itr = getJavaClass().getPublicMethods().iterator();
 		while (itr.hasNext()) {
 			Method mthd = (Method) itr.next();
 			if (mthd.isStatic() || mthd.isConstructor())
 				continue; // Statics/constructors don't participate as behaviors	
 			String longName = formLongName(mthd);
 			if (supers.get(longName) != null)
 				continue;	// Already exists in supers, don't override.
 				
 			createOperation(null, longName, mthd, null);	// Don't pass a name, try to create it by name, only use ID if there is more than one of the same name.
 		}
 	}
 
 	/**
 	 * merge all  the Behaviors(i.e. supertypes)
 	 */
 	protected BasicEList allOperations() {
 		UniqueEList allOperations = new UniqueEList() {
 			protected Object[] newData(int capacity) {
 				return new EOperation[capacity];
 			}
 
 			protected boolean useEquals() {
 				return false;
 			}
 		};
 		if (!isIntrospecting && !isDoingAllOperations && isResourceConnected()) {
 			isDoingAllOperations = true;
 			try {
 				EList localOperations = getJavaClass().getEOperations();
 				JavaClass superType = getJavaClass().getSupertype();
 				if (superType != null) {
 					// Now we need to merge in the supers.
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					List supers = superType.getEAllOperations();					
 
 					// We will now merge as directed.
 					boolean mergeAll = bd == null || bd.isMergeSuperBehaviors();
 					if (!mergeAll) {
 						// we don't to want to merge super properties, but we still need super non-properties.
 						int len = supers.size();
 						for (int i = 0; i < len; i++) {
 							EOperation o = (EOperation) supers.get(i);
 							MethodDecorator md = Utilities.getMethodDecorator(o);
 							if (md == null || (md.isImplicitlyCreated() == FeatureDecorator.NOT_IMPLICIT && !md.isMergeIntrospection()))
 								allOperations.add(o);
 						}
 					} else {
 						// We want to merge all.					
 						if (beaninfo != null) {
 							// BeanInfo has given us the merge list. If the list is empty, then we accept all.
 							IArrayBeanProxy superNames = (IArrayBeanProxy) getProxyConstants().getInheritedMethodDescriptorsProxy().invokeCatchThrowableExceptions(beaninfo);
 							if (superNames != null) {
 								// We were given a list of names.
 								// Get the names into a set to create a quick lookup.
 								int l = superNames.getLength();
 								HashSet superSet = new HashSet(l);
 								for (int i = 0; i < l; i++) {
 									superSet.add(((IStringBeanProxy) superNames.getCatchThrowableException(i)).stringValue());
 								}
 								
 								// Now walk and add in non-bean properties (and bean operations that were explicitly added and not mergeable (i.e. didn't come thru beaninfo)) 
 								// and those specifically called out by BeanInfo.
 								int len = supers.size();
 								for (int i = 0; i < len; i++) {
 									EOperation o = (EOperation) supers.get(i);
 									MethodDecorator md = Utilities.getMethodDecorator(o);
 									if (md == null || (md.isImplicitlyCreated() == FeatureDecorator.NOT_IMPLICIT && !md.isMergeIntrospection()))
 										allOperations.add(o);
 									else {
 										String longName = formLongName(o);
 										if (longName == null || superSet.contains(longName))
 											allOperations.add(o);
 									}
 								}						
 							} else {
 								// BeanInfo called out that all super properties 
 								allOperations.addAll(supers);
 							}
 						} else {
 							// We don't have a BeanInfo telling us how to merge, so we did reflection. But if that is the case, we already
 							// took the supers into account and did not override any thru the reflection, so all supers are good.
 							allOperations.addAll(supers);
 						}
 					}
 				}
 				allOperations.addAll(localOperations);				
 				ESuperAdapter sa = getJavaClass().getESuperAdapter();
 				sa.setAllOperationsCollectionModified(false); // Now built, so reset to not changed.
 			} finally {
 				isDoingAllOperations = false;
 			}
 		}
 
 		allOperations.shrink();
 		return new EcoreEList.UnmodifiableEList(
 			getJavaClass(),
 			EcorePackage.eINSTANCE.getEClass_EAllOperations(),
 			allOperations.size(),
 			allOperations.data());
 
 	}
 
 	/**
 	 * introspect the events
 	 */
 	protected EList introspectEvents() {
 		introspectIfNecessary();
 		if (!isIntrospecting && !isIntrospectingEvents && !hasIntrospectedEvents) {
 			isIntrospectingEvents = true;
 			try {
 				if (isResourceConnected()) {
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					if (bd == null || bd.isIntrospectEvents()) {
 						// bd wants events to be introspected/reflected
 						if (beaninfo != null) {
 							IArrayBeanProxy events =
 								(IArrayBeanProxy) getProxyConstants().getEventSetDescriptorsProxy().invokeCatchThrowableExceptions(
 									beaninfo);
 							if (events != null) {
 								int eventSize = events.getLength();
 								for (int i = 0; i < eventSize; i++)
 									calculateEvent(events.getCatchThrowableException(i));
 							}
 						} else
 							reflectEvents(); // No beaninfo, so use reflection to create events
 					}
 
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
 								int implicit = e.isImplicitlyCreated();
 								if (implicit != EventSetDecorator.NOT_IMPLICIT) {
 									e.setEModelElement(null); // Remove it because it was implicit.
 									 ((InternalEObject) e).eSetProxyURI(BAD_URI);
 									e = null;
 								}
 								if (implicit == EventSetDecorator.IMPLICIT_DECORATOR_AND_FEATURE) {
 									itr.remove(); // The feature was implicit too.
 									 ((InternalEObject) a).eSetProxyURI(BAD_URI);
 									a = null;
 								}
 							}
 						}
 					}
 					hasIntrospectedEvents = true;
 				}
 			} finally {
 				isIntrospectingEvents = false;
 				eventsMap = null; // Clear out the temp lists.
 				eventsRealList = null;
 			}
 		}
 
 		return getJavaClass().getEventsGen();
 	}
 
 	/**
 	 * Fill in the event and its decorator using the eventDesc.
 	 */
 	protected void calculateEvent(IBeanProxy eventDesc) {
 		String name = ((IStringBeanProxy) getProxyConstants().getNameProxy().invokeCatchThrowableExceptions(eventDesc)).stringValue();
 		createEvent(name, eventDesc);
 	}
 
 	/**
 	 * Fill in the event and its decorator using the passed in information.
 	 */
 	public EventSetDecorator createEvent(String name, IBeanProxy eventDesc) {
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
 
 		int implicit = ed == null ? FeatureDecorator.IMPLICIT_DECORATOR : FeatureDecorator.NOT_IMPLICIT;
 		if (event == null) {
 			// We will create a new Event.
 			event = BeaninfoFactory.eINSTANCE.createBeanEvent();
 			getEventsList().add(event);
 			implicit = FeatureDecorator.IMPLICIT_DECORATOR_AND_FEATURE; // Can't have an implicit feature but explicit decorator.
 		}
 
 		// Now fill it in.
 		 ((XMIResource) event.eResource()).setID(event, getJavaClass().getName() + BeaninfoJavaReflectionKeyExtension.EVENT + name);
 		event.setName(name);
 
 		// Now create/fill in the event decorator for it.
 		// If there already is one then we
 		// will use it. This allows merging with beaninfo.		
 		if (ed == null) {
 			ed = BeaninfoFactory.eINSTANCE.createEventSetDecorator();
 			ed.setImplicitlyCreated(implicit);
 			event.getEAnnotations().add(ed);
 		}
 		ed.setDescriptorProxy(eventDesc);
 		ed.setDecoratorProxy(null);
 		return ed;
 	}
 
 	/**
 	 * Reflect the events. This requires going through local public methods and creating an 
 	 * event for the discovered events.
 	 */
 	protected void reflectEvents() {
 		// If we are set to mergeSuperTypeEvents, then we need to get the super events.
 		// This is so that duplicate any from super that we find here. When reflecting we don't
 		// allow discovered duplicates.
 		HashMap supers = new HashMap(50);
 		BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 		if (bd.isMergeSuperEvents()) {
 			JavaClass superType = (JavaClass) getJavaClass().getSupertype();
 			if (superType != null) {
 				Iterator superAllItr = superType.getAllEvents().iterator();
 				while (superAllItr.hasNext()) {
 					JavaEvent se = (JavaEvent) superAllItr.next();
 					supers.put(se.getName(), se);
 				}
 				// Kludge: The above requests for super events could of caused a recycle (in case the super
 				// was stale). Because of this we need to reintrospect to mark ourselves as not stale.
 				introspectIfNecessary();
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
 
 				JavaEvent superEvent = (JavaEvent) supers.get(eventName);
 				if (superEvent != null)
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
 
 		UniqueEList allEvents = new UniqueEList() {
 			protected Object[] newData(int capacity) {
 				return new JavaEvent[capacity];
 			}
 
 			protected boolean useEquals() {
 				return false;
 			}
 		};
 		if (!isIntrospecting && !isDoingAllEvents && isResourceConnected()) {
 			isDoingAllEvents = true;
 			try {
 				EList localEvents = getJavaClass().getEvents();
 				JavaClass superType = getJavaClass().getSupertype();
 				if (superType != null) {
 					// Now we need to merge in the supers.
 					BeanDecorator bd = Utilities.getBeanDecorator(getJavaClass());
 					List supers = superType.getAllEvents();					
 
 					// We will now merge as directed.
 					boolean mergeAll = bd == null || bd.isMergeSuperEvents();
 					if (!mergeAll) {
 						// we don't to want to merge super properties, but we still need super non-properties or explicit ones.
 						int len = supers.size();
 						for (int i = 0; i < len; i++) {
 							JavaEvent e = (JavaEvent) supers.get(i);
 							EventSetDecorator ed = Utilities.getEventSetDecorator(e);
 							if (ed == null || (ed.isImplicitlyCreated() == FeatureDecorator.NOT_IMPLICIT && !ed.isMergeIntrospection()))
 								allEvents.add(e);
 						}
 					} else {
 						// We want to merge all.					
 						if (beaninfo != null) {
 							// BeanInfo has given us the merge list. If the list is empty, then we accept all.
 							IArrayBeanProxy superNames = (IArrayBeanProxy) getProxyConstants().getInheritedEventSetDescriptorsProxy().invokeCatchThrowableExceptions(beaninfo);
 							if (superNames != null) {
 								// We were given a list of names.
 								// Get the names into a set to create a quick lookup.
 								int l = superNames.getLength();
 								HashSet superSet = new HashSet(l);
 								for (int i = 0; i < l; i++) {
 									superSet.add(((IStringBeanProxy) superNames.getCatchThrowableException(i)).stringValue());
 								}
 								
 								// Now walk and add in non-bean properties (and bean events that were explicitly added and not mergeable (i.e. didn't come thru beaninfo)) 
 								// and those specifically called out by BeanInfo.
 								int len = supers.size();
 								for (int i = 0; i < len; i++) {
 									JavaEvent e = (JavaEvent) supers.get(i);
 									EventSetDecorator ed = Utilities.getEventSetDecorator(e);
 									if (ed == null || (ed.isImplicitlyCreated() == FeatureDecorator.NOT_IMPLICIT && !ed.isMergeIntrospection()) || superSet.contains(ed.getName()))
 										allEvents.add(e);
 								}							
 							} else {
 								// BeanInfo called out that all super properties 
 								allEvents.addAll(supers);
 							}
 						} else {
 							// We don't have a BeanInfo telling us how to merge. This means we reflected and have already stripped
 							// out and prevented duplicates by name.
 							allEvents.addAll(supers);
 						}
 					}
 				}
 				allEvents.addAll(localEvents);				
 				superAdapter.setAllEventsCollectionModified(false); // Now built, so reset to not changed.
 			} finally {
 				isDoingAllEvents = false;
 			}
 		}
 
 		allEvents.shrink();
 		return new EcoreEList.UnmodifiableEList(
 			getJavaClass(),
 			JavaRefPackage.eINSTANCE.getJavaClass_AllEvents(),
 			allEvents.size(),
 			allEvents.data());
 
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
 
 		List parms = (List) method.getParameters();
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
 
 		List parms = (List) method.getParameters();
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
 			EventSetDecorator ed = BeaninfoClassAdapter.this.createEvent(name, null);
 			if (ed == null)
 				return false; // Reflection not wanted.
 
 			ed.setAddListenerMethod(addListenerMethod);
 			ed.setRemoveListenerMethod(removeListenerMethod);
 
 			// See if unicast.
 			Iterator itr = addListenerMethod.getJavaExceptions().iterator();
 			while (itr.hasNext()) {
 				if (itr.next() == BeaninfoClassAdapter.this.tooManyExceptionClass) {
 					ed.setUnicast(true);
 					break;
 				}
 			}
 
 			// Set the listener type.
 			List parms = addListenerMethod.getParameters();
 			ed.setListenerType((JavaClass) ((JavaParameter) parms.get(0)).getEType());
 
 			// We'll let listener methods get retrieved dynamically when needed.
 
 			return true;
 		}
 
 	}
 
 	/**
 	 * Mark this factory as the stale factory.
 	 */
 	public void markStaleFactory(ProxyFactoryRegistry stale) {
 		if (staleFactory == null) {
 			// It's not stale so make it stale.
 			hasIntrospected = hasIntrospectedProperties = hasIntrospectedOperations = hasIntrospectedEvents = false;
 			// So that next access will re-introspect
 			defaultBound = null; // So query on next request.
 			staleFactory = new WeakReference(stale);
 			if (beaninfo != null) {
 				beaninfo.getProxyFactoryRegistry().releaseProxy(beaninfo); // Dispose of the beaninfo since we will need to recreate it
 				beaninfo = null;
 			}
 
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
 	
 	private String formLongName(String name, IBeanProxy methDesc) {
 		StringBuffer longName = new StringBuffer(100);
 		longName.append(name); // Feature Name
 		longName.append(':');
 		IMethodProxy mthd = (IMethodProxy) getProxyConstants().getMethodProxy().invokeCatchThrowableExceptions(methDesc);
 		longName.append(mthd.getName()); // Method Name
 		longName.append('(');
 		IBeanTypeProxy[] p = mthd.getParameterTypes();
 		for (int i = 0; i < p.length; i++) {
 			IBeanTypeProxy parm = p[i];
 			if (i>0)
 				longName.append(',');
 			longName.append(parm.getFormalTypeName());
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
