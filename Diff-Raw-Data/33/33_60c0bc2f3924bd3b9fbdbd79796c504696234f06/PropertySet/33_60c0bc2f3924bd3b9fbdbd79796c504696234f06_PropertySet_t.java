 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.model2;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.CoreException;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * A <code>PropertySet</code> contains information about a
  * property set.  A property set is a set of properties
  * that either:
  * <UL>
  * <LI>form the base set of properties in a data model object
  *     </LI>
  * <LI>or are the properties added to a data model object
  * by a derived class (such property sets are known as
  * derived property sets)
  *     </LI>
  * <LI>or are the properties added to a data model object by
  * a plug-in (such property sets are know as extension
  * property sets) 
  *     </LI>
  * </UL>
  * The <code>getBasePropertySet</code> and <code>isExtension</code> methods  
  * can be called to determine in which of the above three categories
  * a property set lies.
  * 
  * @see <a href="propertySets.html">Property Set Documentation</a>
  * @see <a href="extendingDatamodel.html#propertySets">Property Set Documentation</a>
  * @author Nigel Westbury
 */
 
 //TODO: do we really need the info to be kept here at all????
 public class PropertySet {
 	
 	private String propertySetId;
 	private IPropertySetInfo propertySetInfo;
 	
 	private Vector properties = new Vector();  // type: PropertyAccessor
 	
 	boolean isExtension;
 	
 	/**
 	 * Used only to store the id passed to the constructor until it 
 	 * can be reconciled to the base or extended PropertySet object
 	 * by init().
 	 */
 	private String baseOrExtendablePropertySetId;
 	
 	// If there is no extension then certain methods that obtain
 	// property values should fetch default values.
 	// We determine the default values by constructing an extension
 	// object using the default constructor.  To avoid constructing
 	// a default extension multiple times, one is cached inside this
 	// object.
 	// This default extension should never be passed outside this package
 	// because plugins have no need for it and can cause chaos if
 	// they alter it.  However it is useful for use inside the
 	// package.
 	private ExtensionObject defaultExtension;  // defined only if an extension property set
 
 	/**
 	 * true if further property sets must be derived from this property set,
 	 * false if property sets cannot be derived from this property set.
 	 */
 	private boolean derivable; // defined only if an extendable property set
 	
 	/**
 	 * Localized text describing the type of object represented
 	 * by this property set.
 	 * <P>
 	 * This field is defined only if an extendable property set 
 	 * and not derivable.
 	 */
 	private String objectDescription;  // defined only if an extendable property set and not derivable
 	
 	private static Map allPropertySetsMap = new HashMap();   // type: String (property set id) to PropertySet
 
 	/**
 	 * Map extendable classes to property sets.
 	 */
 	private static Map classToPropertySetMap = new HashMap();   // type: String (property set id) to PropertySet
 	
 	// TODO clean this up (i.e. remove this)
 	Map extensionPropertySets = null;  // Set only if this is an extendable property set
 
 	// Valid for extendable property sets only
 	PropertySet basePropertySet;
 	
 	// Valid for extension property sets only
 	PropertySet extendablePropertySet;	
 
 	// Valid for extendable property sets only
 	/**
 	 * Set of property sets that are derived from this property set
 	 * (either directly or indirectly) and that are not
 	 * themselves derivable.
 	 */
 	private Vector derivedPropertySets = new Vector();
 
 	// Valid for all property sets except those that are
 	// extendable and must be derived from.
 	private Constructor implementationClassConstructor;
 	
 	// Valid for all property sets except those that are
 	// extendable and must be derived from.
 	private Constructor defaultImplementationClassConstructor;
 	
 	/**
 	 * A list of all properties in this and base property sets that are
 	 * passed to the 're-instantiating' constructor.  The order of properties in
 	 * this vector object is the same as the order in which the
 	 * properties are passed as parameters into the constructor. 
 	 * <P>
 	 * Note that constructors for extendable objects take extra
 	 * parameters at the start before the property parameters.
 	 * There are no elements in this Vector corresponding to 
 	 * these parameters.  Therefore the
 	 * indexes into this Vector will not correspond to the index into
 	 * the parameter list.
 	 * <P>
 	 * This field is undefined for derivable property sets.
 	 */
 	private Vector constructorProperties;
 	
 	/**
 	 * A list of all properties in this and base property sets that are
 	 * passed to the 'new object' constructor.  The 'new object' constructor
 	 * takes parameters only for the properties that are lists, so this
 	 * vector is a list of the list properties.  The order of properties in
 	 * this vector object is the same as the order in which the
 	 * properties are passed as parameters into the constructor. 
 	 * <P>
 	 * Note that constructors for extendable objects take extra
 	 * parameters at the start before the property parameters.
 	 * There are no elements in this Vector corresponding to 
 	 * these parameters.  Therefore the
 	 * indexes into this Vector will not correspond to the index into
 	 * the parameter list.
 	 * <P>
 	 * This field is undefined for derivable property sets.
 	 */
 	private Vector defaultConstructorProperties;
 	
 	private Method theDefaultPropertiesMethod;
 
 	
 	/**
 	 * Loads the property sets.
 	 * All property sets (both base and extensions) are added to the 
 	 * net.sf.jmoney.fields extension point.
 	 */
 	public static void init() {
 		// Load the property set extensions.
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sf.jmoney.fields");
 		IExtension[] extensions = extensionPoint.getExtensions();
 		
 		// TODO: They may be not much point in processing extendable classes before extension
 		// classes.  Eclipse, I believe, will always iterate extension info from a plug-in
 		// before extensions from plug-ins that depend on that plug-in, so we don't have the
 		// problem of the extendable not being processed before the extension.
 		// We do have other problems, however, which have required a second pass thru
 		// the property sets.
 		
 		for (int i = 0; i < extensions.length; i++) {
 			IConfigurationElement[] elements =
 				extensions[i].getConfigurationElements();
 			for (int j = 0; j < elements.length; j++) {
 				if (elements[j].getName().equals("extendable-property-set")) {
 					try {
 						Object listener = elements[j].createExecutableExtension("info-class");
 						if (listener instanceof IPropertySetInfo) {
 							IPropertySetInfo pageListener = (IPropertySetInfo)listener;
 							
 							String fullPropertySetId = extensions[i].getNamespace();
 							String id = elements[j].getAttribute("id");
 							if (id != null && id.length() != 0) {
 								fullPropertySetId = fullPropertySetId + '.' + id;
 							}
 							
 							String basePropertySetId = elements[j].getAttribute("base-property-set");
 							if (basePropertySetId != null && basePropertySetId.length() == 0) {
 								basePropertySetId = null;
 							}
 							registerExtendablePropertySet(fullPropertySetId, basePropertySetId, pageListener);
 						}
 					} catch (CoreException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		
 		for (int i = 0; i < extensions.length; i++) {
 			IConfigurationElement[] elements =
 				extensions[i].getConfigurationElements();
 			for (int j = 0; j < elements.length; j++) {
 				if (elements[j].getName().equals("extension-property-set")) {
 					try {
 						Object listener = elements[j].createExecutableExtension("info-class");
 						if (listener instanceof IPropertySetInfo) {
 							IPropertySetInfo pageListener = (IPropertySetInfo)listener;
 							
 							String fullPropertySetId = extensions[i].getNamespace();
 							String id = elements[j].getAttribute("id");
 							if (id != null && id.length() != 0) {
 								fullPropertySetId = fullPropertySetId + '.' + id;
 							}
 							
 							String extendablePropertySetId = elements[j].getAttribute("extendable-property-set");
 							if (extendablePropertySetId != null) {
 								registerExtensionPropertySet(fullPropertySetId, extendablePropertySetId, pageListener);
 							} else {
 								// TODO plug-in error
 							}
 						}
 					} catch (CoreException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		
 		// Make a second pass thru the property sets.
 		// This finds the base property set or the extendable property set for
 		// each property set.
 		for (Iterator iter = PropertySet.getPropertySetIterator(); iter.hasNext(); ) {
 			PropertySet propertySet = (PropertySet)iter.next();
 			propertySet.initPropertiesPass1();
 		}
 		
 		// Make a third pass thru the property sets.
 		// This finishes initialization that requires going up the chain of base
 		// property sets.  This part of the initialization cannot be done in pass 1
 		// because the base class may not have had its base class resolved until
 		// pass 1 is complete.
 		for (Iterator iter = PropertySet.getPropertySetIterator(); iter.hasNext(); ) {
 			PropertySet propertySet = (PropertySet)iter.next();
 			propertySet.initPropertiesPass2();
 		}
 	}
 	
 	
 	private PropertySet(String propertySetId, boolean isExtension, String baseOrExtendablePropertySetId, IPropertySetInfo propertySetInfo) {
 		this.propertySetId = propertySetId;
 		this.propertySetInfo  = propertySetInfo;
 		this.isExtension = isExtension;
 		this.baseOrExtendablePropertySetId = baseOrExtendablePropertySetId;
 		
 		derivable = false;
 		objectDescription = null;
 		
 		// propertySetInfo will be null if data is found in the datastore
 		// but no plug-in exists for the data.  This can happen if a plug-in
 		// creates an additional set of properties and puts data into those
 		// properties.  The data is saved to the datastore but the plug-in
 		// is then un-installed.  For time being, just bypass
 		// this code.
 		// TODO: implement and test this scenario.
 		if (propertySetInfo != null) {
 			// Set up the list of properties.
 			// This is done by calling the registerExtensionProperties
 			// method of the supplied interface.  We must pass an
 			// IPropertyRegistrar implementation.  registerExtensionProperties
 			// will call back into this interface to register each
 			// property.
 			
 			propertySetInfo.registerProperties(
 					this,
 					new IPropertyRegistrar() {
 						
 						public EnumerationAccessor addEnumeratedValue(PropertyAccessor propertyAccessor, String internalName, String displayName) {
 							// TODO Auto-generated method stub
 							return null;
 						}
 						
 						public EnumerationAccessor addEnumeratedValue(PropertyAccessor propertyAccessor, String internalName, String displayName, Class derivedClass) {
 							// TODO Auto-generated method stub
 							return null;
 						}
 						
 						// TODO change all shortDescription to displayName
 						public PropertyAccessor addProperty(String name, String displayName, double width, IPropertyControlFactory propertyControlFactory, IPropertyDependency propertyDependency) {
 							PropertyAccessor accessor = new PropertyAccessorImpl(PropertySet.this, name, displayName, width, propertyControlFactory, propertyDependency);
 							properties.add(accessor);
 							return accessor;
 						}
 
 						public PropertyAccessor addPropertyList(String name, String displayName, Class listItemClass, IPropertyDependency propertyDependency) {
 							PropertyAccessor accessor = new PropertyAccessorImpl(PropertySet.this, name, displayName, listItemClass, propertyDependency);
 							properties.add(accessor);
 							return accessor;
 						}
 						
 						public PropertyAccessor setDerivableInfo(String name, String displayName) {
 							derivable = true;
 							// TODO Auto-generated method stub
 							return null;
 						}
 						
 						public PropertyAccessor setDerivableInfo() {
 							derivable = true;
 							return null;
 						}
 
 						public void setObjectDescription(String description) {
 							objectDescription = description;
 						}
 
 					}
 			);
 		}
 		
 		// Add to our map that maps ids to PropertySet objects.
 		if (allPropertySetsMap.containsKey(propertySetId)) {
 			throw new MalformedPluginException("More than one property set has an id of " + propertySetId);
 		}
 		allPropertySetsMap.put(propertySetId, this);
 
 		if (!isExtension) {
 			extensionPropertySets = new HashMap();
 		}
 		
 		// Add to the map that maps the extendable classes
 		// to the extendable property sets.
 		// Only final property sets (ones which do not define an enumerated type
 		// to control derived classes) are put in the map.  This is important because
 		// when we are looking for the property set for an instance of an object,
 		// we want to be sure we find only the final property set for that object.
 		if (!isExtension && !derivable) {
 			Class implementationClass = propertySetInfo.getImplementationClass();
 			if (classToPropertySetMap.containsKey(implementationClass)) {
 				throw new MalformedPluginException("More than one property set uses " + implementationClass + " as the Java implementation class.");
 			}
 			classToPropertySetMap.put(implementationClass, this);
 		}
 	}
 
 	
 	/**
 	 * This method is called to perform pass 1 of the initialization process.
 	 * Some parts of the initialization require access to a complete list of
 	 * all the PropertySet objects and therefore cannot be done in the
 	 * PropertySet constructor.
 	 * <P>
 	 * The following are initialized by this method:
 	 * <UL>
 	 * <LI>this.extendablePropertySet</LI>
 	 * <LI>this.basePropertySet</LI>
 	 * <LI>this.defaultExtension</LI>
 	 * <LI>in the base class, derivedPropertySets</LI>
 	 * </UL>
 	 */
 	private void initPropertiesPass1() {
 		if (!isExtension) {
 			if (baseOrExtendablePropertySetId != null) {
 				basePropertySet = (PropertySet)allPropertySetsMap.get(baseOrExtendablePropertySetId);
 				if (basePropertySet == null) {
 					throw new MalformedPluginException("No extendable property set with an id of " + baseOrExtendablePropertySetId + " exists.");
 				}
 				
 				if (basePropertySet.isExtension()) {
 					throw new MalformedPluginException(baseOrExtendablePropertySetId + " is a base property set.  Extension property sets cannot be extended, but " + propertySetId + " is declared as a base of " + baseOrExtendablePropertySetId + ".");
 				}
 				
 				if (!basePropertySet.isDerivable()) {
 					throw new MalformedPluginException(baseOrExtendablePropertySetId + " is a base property for " + propertySetId + ".  However, " + baseOrExtendablePropertySetId + " is not derivable (IPropertyRegistrar.setDerivableInfo not called from the IPropertySetInfo implementation).");
 				}
 			} else {
 				basePropertySet = null;
 			}
 			
 		} else {
 			extendablePropertySet = (PropertySet)allPropertySetsMap.get(baseOrExtendablePropertySetId);
 			if (extendablePropertySet == null) {
 				throw new MalformedPluginException("No extendable property set with an id of " + baseOrExtendablePropertySetId + " exists.");
 			}
 			
 			if (extendablePropertySet.isExtension()) {
 				throw new MalformedPluginException(baseOrExtendablePropertySetId + " is an extension property set.  Extension property sets cannot be extended, but " + propertySetId + " is declared as an extension of " + baseOrExtendablePropertySetId + ".");
 			}
 
 			// We have already checked that all property sets have unique ids, so a
 			// property set with the same id cannot already exist as an extension
 			// to the extendable property set.
 			assert (!extendablePropertySet.extensionPropertySets.containsKey(propertySetId));
 			extendablePropertySet.extensionPropertySets.put(propertySetId, this);
 
 			// Set up the extension that contains the default property values.
 			if (propertySetInfo != null) {
 				try {
 					if (propertySetInfo.getImplementationClass() != null) {
 						defaultExtension = (ExtensionObject)
 						propertySetInfo.getImplementationClass().newInstance();
 						// TODO: plugin error if null is returned
 					}
 				} catch (Exception e) {
 					// TODO: deal with this error
 					// Plug-in error if no default constructor.
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 			} else {
 				defaultExtension = null;
 			}
 		}
 		
 		if (!isExtension && !derivable) {
 			if (objectDescription == null) {
 				throw new MalformedPluginException("IPropertyRegistrar.setObjectDescription is not called from the IPropertyInfo implementation for " + propertySetId + ", nor is the property set derivable or an extension.");
 			}
 			
 			// Add this property set to the list of derived property sets
 			// for this and all the base classes.
 			for (PropertySet base = this; base != null; base = base.getBasePropertySet()) {
 				base.derivedPropertySets.add(this);
 			}
 		} else {
 			if (objectDescription != null) {
 				if (isExtension) {
 					throw new MalformedPluginException("IPropertyRegistrar.setObjectDescription is called from the IPropertyInfo implementation for " + propertySetId + ", but the property set is an extension.");
 				} else {
 					throw new MalformedPluginException("IPropertyRegistrar.setObjectDescription is called from the IPropertyInfo implementation for " + propertySetId + ", but the property set is derivable.");
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * This method is called to complete the initialization of this object.
 	 * Some parts of the initialization require access to a complete list of
 	 * all the PropertySet objects and therefore cannot be done in the
 	 * PropertySet constructor.
 	 * <P>
 	 * The following are initialized by this method:
 	 * <UL>
 	 * <LI>this.constructorParameters</LI>
 	 * <LI>this.defaultConstructorParameters</LI>
 	 * <LI>this.implementationClassConstructor</LI>
 	 * <LI>this.defaultImplementationClassConstructor</LI>
 	 * <LI>indexIntoScalarProperties, for each scalar PropertyAccessor in this property set</LI>
 	 * <LI>indexIntoConstructorParameters, for each PropertyAccessor in this property set</LI>
 	 * </UL>
 	 */
 	private void initPropertiesPass2() {
 		if (isExtension || !derivable) {
 			// Build the list of properties that are passed to
 			// the 'new object' constructor and another list that
 			// are passed to the 're-instantiating' constructor.
 			
 			constructorProperties = new Vector();
 			defaultConstructorProperties = new Vector();
 		}
 		
 		if (!isExtension) {
 			
 			// We need to be able to iterate through property sets
 			// starting at the base property set and continuing through
 			// derived property sets until we get to this property set.
 			// To do this, we first build a list of the base property sets
 			// and we can then iterate through these property sets in
 			// reverse order.
 			Vector basePropertySets = new Vector();
 			for (PropertySet base = getBasePropertySet(); base != null; base = base.getBasePropertySet()) {
 				basePropertySets.add(base);
 			}
 			
 			// Add the appropriate properties from the base classes to
 			// the constructorParameters and defaultConstructorParameters arrays.
 			int parameterIndex = 3;
 			int scalarIndex = 0;
 			
 			for (int i = basePropertySets.size()-1; i >= 0; i--) {
 				PropertySet base = (PropertySet)basePropertySets.get(i);
 
 				for (Iterator iter = base.getPropertyIterator1(); iter.hasNext(); ) {
 					PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 					if (!isDerivable()) {
 						constructorProperties.add(propertyAccessor);
 						if (propertyAccessor.isList()) {
 							defaultConstructorProperties.add(propertyAccessor);
 						}
 					}
 					
 					parameterIndex++;
 				}
 
 				for (Iterator iter = base.getPropertyIterator2(); iter.hasNext(); ) {
 					PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 					if (propertyAccessor.isScalar()) {
 						scalarIndex++;
 					}
 				}
 			}
 			
 			// Process the properties in this property set.
 			for (Iterator iter = getPropertyIterator1(); iter.hasNext(); ) {
 				PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 				propertyAccessor.setIndexIntoConstructorParameters(parameterIndex++);
 				if (!isDerivable()) {
 					constructorProperties.add(propertyAccessor);
 					if (propertyAccessor.isList()) {
 						defaultConstructorProperties.add(propertyAccessor);
 					}
 				}
 			}
 			
 			for (Iterator iter = getPropertyIterator2(); iter.hasNext(); ) {
 				PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 				if (propertyAccessor.isScalar()) {
 					propertyAccessor.setIndexIntoScalarProperties(scalarIndex++);
 				}
 			}
 		} else {
 			// This property set is an extension.
 			int parameterIndex = 0;
 			for (Iterator iter = getPropertyIterator1(); iter.hasNext(); ) {
 				PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 				constructorProperties.add(propertyAccessor);
 				propertyAccessor.setIndexIntoConstructorParameters(parameterIndex++);
 				if (propertyAccessor.isList()) {
 					defaultConstructorProperties.add(propertyAccessor);
 				}
 			}
 		}
 		
 		// Find the full constructor (unless this is a derivable
 		// property set, in which case no constructor is needed).
 
 		// Build the list of types of the constructor parameters.
 		if (isExtension || !derivable) {
 			{
 				int i = 0;
 				int parameterCount = constructorProperties.size();
 				if (!isExtension) {
 					parameterCount += 3;
 				}
 				Class parameters[] = new Class[parameterCount];
 				
 				// In the extendable objects, the first parameters are always:
 				// - the key to this object
 				// - a map of extensions
 				// - the key to the object which is the parent of this object
 				if (!isExtension) {
 					parameters[i++] = IObjectKey.class;
 					parameters[i++] = Map.class;
 					parameters[i++] = IObjectKey.class;
 				}
 				
 				for (Iterator iter = constructorProperties.iterator(); iter.hasNext(); ) {
 					PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 					if (propertyAccessor.isScalar()) {
 						if (ExtendableObject.class.isAssignableFrom(propertyAccessor.getValueClass())) { 		
 							// For extendable objects, we pass not the object
 							// but a key to the object.  This is so that we do not
 							// have to read the object from the database unless it
 							// is necessary.
 							parameters[i] = IObjectKey.class; 
 						} else {
 							// The property type is either a primative (int, boolean etc),
 							// or a non-extendable class (Long, String, Date)
 							// or a non-extendable class added by a plug-in.
 							parameters[i] = propertyAccessor.getValueClass();
 						}
 					} else {
 						parameters[i] = IListManager.class; 
 					}
 					i++;
 				}
 				
 				try {
 					implementationClassConstructor =
 						propertySetInfo.getImplementationClass().getConstructor(parameters);
 				} catch (SecurityException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (NoSuchMethodException e) {
 					String parameterText = "";
 					for (int paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
 						if (paramIndex > 0) {
 							parameterText = parameterText + ", ";
 						}
 						parameterText = parameterText + parameters[paramIndex].getName();
 					}
 					throw new MalformedPluginException("The " + propertySetInfo.getImplementationClass().getName() + " class must have a constructor that takes parameters of types (" + parameterText + ").");
 				}
 			}
 			
 			// Find the 'new object' constructor which sets the default values for
 			// the scalar properties.
 			// Build the list of types of the constructor parameters.
 			{
 				int i = 0;
 				int parameterCount = defaultConstructorProperties.size();
 				if (!isExtension) {
 					parameterCount += 3;
 				}
 				Class parameters[] = new Class[parameterCount];
 				
 				// In the extendable objects, the first parameters are always:
 				// - the key to this object
 				// - a map of extensions
 				// - the key to the object which is the parent of this object
 				if (!isExtension) {
 					parameters[i++] = IObjectKey.class;
 					parameters[i++] = Map.class;
 					parameters[i++] = IObjectKey.class;
 				}
 				
 				for (Iterator iter = defaultConstructorProperties.iterator(); iter.hasNext(); ) {
 					PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 					parameters[i++] = IListManager.class; 
 				}
 				
 				try {
 					defaultImplementationClassConstructor =
 						propertySetInfo.getImplementationClass().getConstructor(parameters);
 				} catch (SecurityException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (NoSuchMethodException e) {
 					String parameterText = "";
 					for (int paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
 						if (paramIndex > 0) {
 							parameterText = parameterText + ", ";
 						}
 						parameterText = parameterText + parameters[paramIndex].getName();
 					}
 					throw new MalformedPluginException("The " + propertySetInfo.getImplementationClass().getName() + " class must have a constructor that takes parameters of types (" + parameterText + ").");
 				}
 			}
 			
 			// Find the getDefaultProperties method.
 			
 			Class parameters[] = new Class[] { };
 			
 			try {
 				theDefaultPropertiesMethod =
 					propertySetInfo.getImplementationClass().getDeclaredMethod("getDefaultProperties", parameters);
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				String parameterText = "";
 				for (int paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
 					if (paramIndex > 0) {
 						parameterText = parameterText + ", ";
 					}
 					if (parameters[paramIndex].isArray()) {
 						parameterText = parameterText + "net.sf.jmoney.model2.ExtensionProperties[]";
 					} else {
 						parameterText = parameterText + parameters[paramIndex].getName();
 					}
 				}
 				throw new MalformedPluginException("The " + propertySetInfo.getImplementationClass().getName() + " class must have a 'getDefaultProperties' method that takes parameters of types (" + parameterText + ").");
 			}
 			
 			// The '8' bit indicates that the method is static.
 			if ((theDefaultPropertiesMethod.getModifiers() & 8) != 8) {
 				throw new MalformedPluginException("The 'getDefaultProperties' method in " + propertySetInfo.getImplementationClass().getName() + " class must be static.");
 			}
 			
 			// The '1' bit indicates that the method is public.
 			// (2 is private, 4 is protected, 1,2 & 4 bits off is default).
 			if ((theDefaultPropertiesMethod.getModifiers() & 7) != 1) {
 				throw new MalformedPluginException("The 'getDefaultProperties' method in " + propertySetInfo.getImplementationClass().getName() + " class must be public.");
 			}
 		} else {
 			// No default properties method is required for derived property sets
 			// so set to null.
 			theDefaultPropertiesMethod = null;
 		}
 		
 		// Complete the initialization of the properties
 		
 		for (Iterator iter = properties.iterator(); iter.hasNext(); ) {
 			PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 			propertyAccessor.initMethods();
 		}
 	}
 
 
 	/**
 	 * @return The number of properties in this property set.
 	 *		This number is the same as the number of elements
 	 * 		enumerated by the iterator returned by getPropertyIterator1.
 	 */
 	private int getPropertyCount() {
 		return properties.size();
 	}
 
 
 	/**
 	 * Returns the list of properties whose values are passed as
 	 * parameters to the 're-instantiating' constructor of the implementation class.
 	 * <P>
 	 * All properties, scalar and list properties, are included
 	 * in this list.  The collection is ordered in the same order
 	 * as the property values are passed to the constructor.
 	 * <P>
 	 * This method may not be called on derivable property sets
 	 * because objects implementing derivable property sets are
 	 * abstract and so no constructor is used by the JMoney framework.
 	 *  
 	 * @return
 	 */
 	public Collection getConstructorProperties() {
 		return constructorProperties;
 	}
 
 
 	/**
 	 * Returns the list of properties whose values are passed as
 	 * parameters to the 'new object' constructor of the implementation class.
 	 * <P>
 	 * Only list properties are included
 	 * in this list.  The collection is ordered in the same order
 	 * as the property values are passed to the constructor.
 	 * <P>
 	 * This method may not be called on derivable property sets
 	 * because objects implementing derivable property sets are
 	 * abstract and so no constructor is used by the JMoney framework.
 	 *  
 	 * @return
 	 */
 	public Collection getDefaultConstructorProperties() {
 		return defaultConstructorProperties;
 	}
 
 
 	/**
 	 * 
 	 * @param propertySetId
 	 * @param basePropertySetId If this property set is derived from
 	 * 			another property set then the id of the base property set,
 	 * 			otherwise null. 
 	 * @param propertySetInfo Null if property set data is found in
 	 * 			the datastore but no plug-in defined a property set
 	 * 			with this id. 
 	 * @return
 	 */
 	static private void registerExtendablePropertySet(String propertySetId, String basePropertySetId, IPropertySetInfo propertySetInfo) {
 		// Objects of this class self-register, so we need
 		// only construct the object.
 		new PropertySet(propertySetId, false, basePropertySetId, propertySetInfo);
 	}
 	
 	/**
 	 * 
 	 * @param propertySetId 
 	 * @param propertySetInfo Null if property set data is found in
 	 * 			the datastore but no plug-in defined a property set
 	 * 			with this id. 
 	 * @return
 	 */
 	static private void registerExtensionPropertySet(String propertySetId, String extendablePropertySetId, IPropertySetInfo propertySetInfo) {
 		// Objects of this class self-register, so we need
 		// only construct the object.
 		new PropertySet(propertySetId, true, extendablePropertySetId, propertySetInfo);
 	}
 	
 	/**
 	 * This method is called when one plug-in wants to access a property
 	 * in another plug-in's property set.  Callers must be able to handle
 	 * the case where the requested property set is not found.  The plug-in
 	 * must catch PropertySetNotFoundException and supply appropriate behavior
 	 * (not an error from the user's perspective).
 	 */
 	static public PropertySet getPropertySet(String propertySetId) throws PropertySetNotFoundException {
 		PropertySet propertySet = (PropertySet)allPropertySetsMap.get(propertySetId);
 		if (propertySet == null) {
 			throw new PropertySetNotFoundException(propertySetId);
 		}
 		return propertySet;
 	}
 
 	
 	/**
 	 * This method will find the PropertySet object, given the class of an
 	 * object.  The class must be an implementation of an extendable property
 	 * set.  (The class may not be an implementation of an extension
 	 * property set).
 	 * <P>
 	 * This method should be called when we have an object, but we do not know
 	 * exactly of what derived class the object is.  By calling this method,
 	 * we can get the actual set of properties for this object.
 	 * For example, if one wants to display the properties for
 	 * a CapitalAccount object, then call this method to get the property
 	 * set for the actual object and you will then see properties for
 	 * this particular object (bank account properties if the object
 	 * is a bank account, credit card account properties if the
 	 * object is a credit card account and so on).
 	 * <P>
 	 * This method is also useful when introspection gives us the interface
 	 * (which extends IExtendableObject) of the object returned by a property 
 	 * getter and we need to obtain the property set that is represented by
 	 * that interface.
 	 */
 	static public PropertySet getPropertySet(Class propertySetClass) {
 		// The classToPropertySetMap contains mappings for final extendable property
 		// sets only, so there should only be a single interface or superclass found
 		// in the object's class hierarchy that is in the map.
 		
 		// Check the superclasses
 		Class thisClass = propertySetClass;
 		do {
 			PropertySet result = (PropertySet)classToPropertySetMap.get(thisClass);
 			if (result != null) {
 				return result;
 			}
 			
 			result = searchInterfaces(thisClass);
 			if (result != null) {
 				return result;
 			}
 			
 			thisClass = thisClass.getSuperclass();
 		} while (!thisClass.equals(Object.class));
 		
 		throw new RuntimeException("No property set found for object of class " + propertySetClass.getName() + ".");
 	}
 
 	private static PropertySet searchInterfaces(Class parentClass) {
 		// Check the interfaces
 		Class interfaces[] = parentClass.getInterfaces();
 		for (int i = 0; i < interfaces.length; i++) {
 			PropertySet result = (PropertySet)classToPropertySetMap.get(interfaces[i]);
 			if (result != null) {
 				return result;
 			}
 
 			result = searchInterfaces(interfaces[i]);
 			if (result != null) {
 				return result;
 			}
 		}
 		
 		return null;
 	}
 
 	
 	/**
 	 * This method is called when loading data from an extension
 	 * in a datastore.
 	 * The datastore will store the property set id and will pass it.
 	 * If no property set with the given id exists in the map then
 	 * we create one.  However there will be no class or informatation
 	 * available about the property set.
 	 */
 	static public PropertySet getPropertySetCreatingIfNecessary(String propertySetId, String extendablePropertySetId) {
 		PropertySet extendablePropertySet = (PropertySet)allPropertySetsMap.get(extendablePropertySetId);
 		if (extendablePropertySet == null) {
 			throw new RuntimeException("No extendable property set with an id of " + extendablePropertySetId + " exists.");
 		}
 		if (extendablePropertySet.isExtension()) {
 			throw new RuntimeException("extension on extension error");
 		}
 		PropertySet key = (PropertySet)extendablePropertySet.extensionPropertySets.get(propertySetId);
 		if (key == null) {
 			key = new PropertySet(propertySetId, true, extendablePropertySetId, null);
 		}
 		return key;
 	}
 	
 	public String toString() {
 		return propertySetId;
 	}
 	
 	
 	/**
 	 * @return The globally unique id of the property set.
 	 */
 	public String getId() {
 		return propertySetId;
 	}
 	
 	/**
 	 * Indicates if the plug-in that had created these properties
 	 * is installed.
 	 * <P>
 	 * The datastore may contain data in properties that had 
 	 * been added by a plug-in but that plug-in may not be installed.
 	 * The plug-in may have been un-installed since the data was
 	 * entered, or the files containing the accounting data may
 	 * have been copied to another computer which does not have
 	 * the plug-in installed.  
 	 * <P>
 	 * JMoney does not drop the data in such circumstances.
 	 * The data is maintained, even though it cannot be
 	 * processed except in a very generic way.
 	 * 
 	 * @see doc on data without plug-in
 	 * @return
 	 */
 	// TODO: more work needed in this area
 	public boolean isExtensionClassKnown() {
 		return propertySetInfo != null;
 	}
 	
 	/**
 	 * Returns the implementation class for this property set.
 	 * 
 	 * The implementation class for a property set is a class that
 	 * implements getters and setters for all the properties in
 	 * the property set.  Implementation classes for property sets
 	 * have a few other rules they must follow too.  For example,
 	 * certain constructors must be provided and they must extend
 	 * either ExtendableObject or ExtensionObject.
 	 * See the documentation on property set implementation classes
 	 * for further information.
 	 *
 	 * @see doc on implemetation classes
 	 * @return the implementation class
 	 */
 	public Class getImplementationClass() {
 		return propertySetInfo.getImplementationClass();
 	}
 	
 	/**
	 * This is used in two situations:
	 * - when data is read from an XML file and there is no value for a
	 * property in the file.  The property from this method is passed
	 * to the constructor.
	 * - when a SQL column is created, the value from this method is
	 * used as the default value.
	 * 
	 * @return an array of default property values, there being a value
	 * 				for each scalar property (but not the list properties)
 	 */
 	public Object[] getDefaultPropertyValues2() {
 		// We do not cache the array returned by this method.
 		// The reason is that the default values may contain a
 		// timestamp or may depend on user options.
 		try {
 			Object [] values = (Object[])theDefaultPropertiesMethod.invoke(null, null);
 			return values;
 		} catch (IllegalAccessException e) {
 			throw new MalformedPluginException("Method '" + theDefaultPropertiesMethod.getName() + "' in '" + getImplementationClass().getName() + "' must be public.");
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public ExtensionObject getDefaultPropertyValues() {
 		return defaultExtension;
 	}
 	
 	/**
 	 * Get the property accessor for a property in an 
 	 * extension property set.
 	 *
 	 * @param name The local name of the property.  This name does not
 	 *          include the dotted prefix.
 	 */
 	public PropertyAccessor getProperty(String name) throws PropertyNotFoundException {
 		for (Iterator propertyIterator = properties.iterator(); propertyIterator.hasNext(); ) {
 			PropertyAccessor propertyAccessor = (PropertyAccessor)propertyIterator.next();
 			if (propertyAccessor.getLocalName().equals(name)) {
 				return propertyAccessor;
 			}
 		}
 		throw new PropertyNotFoundException(propertySetId, name);
 	}
 	
 	/**
 	 * Gets a list of all property sets.  This method is used
 	 * by the Propagator class and also by the datastore plug-ins.
 	 *  
 	 * @return An iterator that will iterate over the full set
 	 * 		of property sets, returning for each the PropertySet
 	 * 		object.
 	 */
 	static public Iterator getPropertySetIterator() {
 		return allPropertySetsMap.values().iterator();
 	}
 	
 	/**
 	 * @return An iterator that iterates over all properties
 	 * 		in this property set, returning, for each property,
 	 * 		the PropertyAccessor object for that property.
 	 */
 	public Iterator getPropertyIterator1() {
 		return properties.iterator();
 	}
 	
 	/**
 	 * Returns an iterator which iterates over all properties
 	 * of the given set of property sets, including both properties in the 
 	 * extendable object and properties in extension property sets.
 	 * <P>
 	 * Properties from base property sets and properties from
 	 * derived property sets are not returned.
 	 * 
 	 * @ return An iterator which iterates over a set of
 	 * 		<code>PropertyAccessor</code> objects.
 	 */
 	// This method may be called on extendable property sets only
 	// (i.e. not on extension property sets).
 	public Iterator getPropertyIterator2() {
 		// Build an array - not efficient but easy and avoids concurrency problems.
 		// TODO: write a proper iterator, or at least cache this vector.
 		Vector fields = new Vector();
 
 		// Properties in this extendable object
 		for (Iterator propertyIterator = getPropertyIterator1(); propertyIterator.hasNext(); ) {
 			PropertyAccessor propertyAccessor = (PropertyAccessor)propertyIterator.next();
 			fields.add(propertyAccessor);
 		}
 
 		// Properties in the extensions
 		for (Iterator propertySetIterator = extensionPropertySets.values().iterator(); propertySetIterator.hasNext(); ) {
 			PropertySet extensionPropertySet = (PropertySet)propertySetIterator.next();
 			
 			for (Iterator propertyIterator = extensionPropertySet.getPropertyIterator1(); propertyIterator.hasNext(); ) {
 				PropertyAccessor propertyAccessor = (PropertyAccessor)propertyIterator.next();
 				fields.add(propertyAccessor);
 			}
 		}
 		
 		return fields.iterator();
 	}
 
 
 	/**
 	 * Returns an iterator which iterates over all properties
 	 * of the given set of property sets, including all
 	 * extension properties, and all base properties including
 	 * all extension properties to the base property sets.
 	 * <P>
 	 * This is the set of properties that can be set against
 	 * an object that implements this property set.
 	 * <P>
 	 * Properties are returned with the properties from the
 	 * base-most class first, then properties from the class
 	 * immediately derived from the base-most class, and so
 	 * on with the properties from this property set being
 	 * last.  This order gives the most intuitive order from
 	 * the user's perspective.  This order also ensures that
 	 * a property in a base class has the same index in the returned order,
 	 * regardless of the actual derived property set.
 	 * 
 	 * @ return An iterator which iterates over a set of
 	 * 		<code>PropertyAccessor</code> objects.
 	 */
 	// This method may be called on extendable property sets only
 	// (i.e. not on extension property sets).
 	public Iterator getPropertyIterator3() {
 		// Build an array - not efficient but easy and avoids concurrency problems.
 		// TODO: write a proper iterator, or at least cache this vector.
 		Vector fields = new Vector();
 
 		// Properties in this and all the base property sets
 		PropertySet extendablePropertySet = this;
 		do {
 			int index= 0;
 			for (Iterator propertyIterator = extendablePropertySet.getPropertyIterator2(); propertyIterator.hasNext(); ) {
 				PropertyAccessor propertyAccessor = (PropertyAccessor)propertyIterator.next();
 				fields.insertElementAt(propertyAccessor, index++);
 			}
 			extendablePropertySet = extendablePropertySet.getBasePropertySet();
 		} while (extendablePropertySet != null);
 
 		return fields.iterator();
 	}
 
 	/**
 	 * Returns an iterator which iterates over all properties
 	 * of the given set of property sets, including both properties in the 
 	 * extendable object and properties in extension property sets.
 	 * <P>
 	 * This method does not have access to an instance of the property set class,
 	 * and so it must return all properties that may be in an
 	 * object including properties that may not be applicable in
 	 * some objects.  We therefore include all properties in
 	 * all base classes, all classes derived from this class, and all
 	 * extensions to any base or derived classes.  (Properties in classes
 	 * or extensions of classes that are derived from a base class of
 	 * the given class are not returned).
 	 * 
 	 * @ return An iterator which iterates over a set of
 	 * 		<code>PropertyAccessor</code> objects.
 	 */
 	// This method may be called on extendable property sets only
 	// (i.e. not on extension property sets).
 	public Iterator getPropertyIterator4() {
 		// Build an array - not efficient but easy and avoids concurrency problems.
 		// TODO: write a proper iterator, or at least cache this vector.
 		Vector fields = new Vector();
 
 		// Properties in this and all the base property sets
 		for (Iterator propertyIterator = getPropertyIterator3(); propertyIterator.hasNext(); ) {
 			PropertyAccessor propertyAccessor = (PropertyAccessor)propertyIterator.next();
 			fields.add(propertyAccessor);
 		}
 		
 		// Properties from derived property sets
 		addPropertiesFromDerivedPropertySets(fields);
 		
 		return fields.iterator();
 	}
 
 	// private helper method.
 	// Method valid for extendable property set only.
 	// TODO: This method is broken.  The derived properties sets are all the non-derivable
 	// property sets, including possibly this property set.
 	void addPropertiesFromDerivedPropertySets(Vector fields) {
 		for (Iterator propertySetIterator = derivedPropertySets.iterator(); propertySetIterator.hasNext(); ) {
 			PropertySet derivedPropertySet = (PropertySet)propertySetIterator.next();
 			
 			for (Iterator propertyIterator = derivedPropertySet.getPropertyIterator2(); propertyIterator.hasNext(); ) {
 				PropertyAccessor propertyAccessor = (PropertyAccessor)propertyIterator.next();
 				fields.add(propertyAccessor);
 			}
 			
 			derivedPropertySet.addPropertiesFromDerivedPropertySets(fields);
 		}
 	}		
 
 	/**
 	 * Gets a list of all property sets that extend the given property
 	 * set.  This method is used by the Propagator class only.
 	 * <P>
 	 * Note:
 	 * This method does not return derived property sets.
 	 * This method does not return property sets that extend any 
 	 * property sets from which this property set is derived.
 	 *  
 	 * @param extendablePropertySetId
 	 * @return An iterator that iterates over the property sets
 	 * 			that extend the given extendable property set,
 	 * 			returning for each property set a String containing
 	 * 			the id of that property set.
 	 */
 	static Iterator getExtensionPropertySetIterator(PropertySet extendablePropertySet) {
 		if (extendablePropertySet.isExtension()) {
 			throw new RuntimeException("cannot iterate extension property sets of an extension");
 		}
 		return extendablePropertySet.extensionPropertySets.values().iterator();
 	}
 	
 	
 	/**
 	 * Gets the accessor for a property given the full name of the property.
 	 *
 	 * This method is used when a column name is persisted in, say, a preferences file.
 	 * This method is also used to get the base properties that are hard coded
 	 * into both the framework and possibly plug-ins.
 	 * 
 	 * @param name The fully qualified property name
 	 */
 	public static PropertyAccessor getPropertyAccessor(String fullPropertyName) throws PropertyNotFoundException {
 		// The local name for properties are the property names that
 		// match the getter and setter method names according to the
 		// Java Bean property patterns.  They cannot therefore contain
 		// a dot.  We are therefore able to separate the property set id
 		// and the local property name by looking for the last dot in
 		// the full property name.
 		
 		int dotLocation = fullPropertyName.lastIndexOf('.');
 		if (dotLocation <= 0) return null;
 		if (dotLocation == fullPropertyName.length() - 1) return null;
 		
 		String propertySetId = fullPropertyName.substring(0, dotLocation);
 		String localName = fullPropertyName.substring(dotLocation + 1);
 		
 		PropertySet propertySet = (PropertySet)allPropertySetsMap.get(propertySetId);
 		if (propertySet == null) {
 			// TODO should we use the different exception for
 			// the case where the property set is not found?
 			throw new PropertyNotFoundException(propertySetId, localName);
 		}
 		
 		PropertyAccessor propertyAccessor = propertySet.getProperty(localName);
 		if (propertyAccessor == null) {
 			throw new PropertyNotFoundException(propertySetId, localName);
 		} 
 		
 		return propertyAccessor;
 	}
 	
 	
 	/**
 	 * Gets the accessor for a property given the local name of the property.
 	 *
 	 * This method searches only this property set and any base
 	 * property sets.  No extensions are searched.
 	 * <P>
 	 * This method is used when a column name is persisted in, say, a file
 	 * and we are keen to keep the data in the file as simple and short as
 	 * possible.  We therefore allow local names only to be specified.
 	 * Local names may not be unique when extensions are included, so we
 	 * must require fully qualified names for extensions.
 	 * 
 	 * @param name The local property name
 	 */
 	public PropertyAccessor getPropertyAccessorGivenLocalNameAndExcludingExtensions(String localPropertyName) throws PropertyNotFoundException {
 		PropertySet thisPropertySet = this;
 		do {
 			try {
 				return thisPropertySet.getProperty(localPropertyName);
 			} catch (PropertyNotFoundException e) {
 			}
 			thisPropertySet = thisPropertySet.getBasePropertySet();
 		} while (thisPropertySet != null);
 		
 		throw new PropertyNotFoundException(propertySetId, localPropertyName);
 	}
 	
 	
 	/**
 	 * @return
 	 */
 	public boolean isExtension() {
 		return isExtension;
 	}
 
 
 	/**
 	 * @return localized text describing the type of object
 	 * 			represented by this property set
 	 */
 	public String getObjectDescription() {
 		if (isExtension() || isDerivable()) {
 			throw new RuntimeException("internal error");
 		}
 		return objectDescription;
 	}
 
 
 	/**
 	 * This method is valid for extendable property sets only.
 	 * Do not call this method on extension property sets.
 	 * 
 	 * @return If this property set is derived from another property
 	 * 			set then the base property set is returned, otherwise
 	 * 			null is returned.
 	 */
 	public PropertySet getBasePropertySet() {
 		if (isExtension) {
 			throw new RuntimeException("getBasePropertySet called for an extension.");
 		}
 		
 		return basePropertySet;
 	}
 	
 	/**
 	 * This method is valid for extension property sets only.
 	 * Do not call this method on extendable property sets.
 	 * 
 	 * @return The property set being extended by this property set.
 	 */
 	public PropertySet getExtendablePropertySet() {
 		if (!isExtension) {
 			throw new RuntimeException("getExtensionPropertySet called for an extendable property set.");
 		}
 		
 		return extendablePropertySet;
 	}	
 
 
 	/**
 	 * This method should be used only by plug-ins that implement
 	 * a datastore.
 	 * 
 	 * @return A newly constructed object, constructed from the given
 	 * 		parameters.  This object may be an ExtendableObject or
 	 * 		may be an ExtensionObject.
 	 */
 	public Object constructImplementationObject(Object [] constructorParameters) {
 		// TODO: tidy up error processing
 		try {
 			return implementationClassConstructor.newInstance(constructorParameters);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		} catch (IllegalAccessException e) {
 			throw new MalformedPluginException("Constructor must be public.");
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 			throw new MalformedPluginException("An exception occured within a constructor in a plug-in.", e);
 		}
 	}
 
 
 	/**
 	 * This method should be used only by plug-ins that implement
 	 * a datastore.
 	 * 
 	 * @return A newly constructed object, constructed from the given
 	 * 		parameters.  This object may be an ExtendableObject or
 	 * 		may be an ExtensionObject.
 	 */
 	public Object constructDefaultImplementationObject(Object [] constructorParameters) {
 		// TODO: tidy up error processing
 		try {
 			return defaultImplementationClassConstructor.newInstance(constructorParameters);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		} catch (IllegalAccessException e) {
 			throw new MalformedPluginException("Constructor must be public.");
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 			throw new MalformedPluginException("An exception occured within a constructor in a plug-in.", e);
 		}
 	}
 
 
 	/**
 	 * @return True if this property set can only be used by
 	 * 			deriving another property set from it, false
 	 * 			if property sets cannot be derived from this
 	 * 			property set.
 	 */
 	public boolean isDerivable() {
 		return derivable;
 	}
 
 
 	/**
 	 * 
 	 * @return an Iterator that iterates over all the property sets
 	 * 				that are derived from this property set and
 	 * 				that are themselves not derivable
 	 */
 	public Iterator getDerivedPropertySetIterator() {
 		return derivedPropertySets.iterator();
 	}
 
 
 }
