 package org.codehaus.xfire.aegis.type;
 
 /**
  * The TypeMappingRegistry provides access to the type mappings within XFire.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Feb 18, 2004
  */
 public interface TypeMappingRegistry
 {
    final public static String ROLE = TypeMappingRegistry.class.getName();
 
     /**
      */
     public TypeMapping register(String encodingStyleURI, TypeMapping mapping);
 
     /**
      */
     public void registerDefault(TypeMapping mapping);
 
     /**
      * Gets the registered default <code>TypeMapping</code> instance.
      * This method returns <code>null</code> if there is no registered
      * default TypeMapping in the registry.
      *
      * @return The registered default <code>TypeMapping</code> instance
      *     or <code>null</code>.
      */
     public TypeMapping getDefaultTypeMapping();
 
     /**
      * Returns a list of registered encodingStyle URIs in this
      * <code>TypeMappingRegistry</code> instance.
      *
      * @return Array of the registered encodingStyle URIs
      */
     public String[] getRegisteredEncodingStyleURIs();
 
     /**
      * Returns the registered <code>TypeMapping</code> for the specified
      * encodingStyle URI. If there is no registered <code>TypeMapping</code>
      * for the specified <code>encodingStyleURI</code>, this method
      * returns <code>null</code>.
      *
      * @param encodingStyleURI Encoding style specified as an URI
      * @return TypeMapping for the specified encodingStyleURI or
      *     <code>null</code>
      */
     public TypeMapping getTypeMapping(String encodingStyleURI);
 
     /**
      * Creates a new empty <code>TypeMapping</code> object.
      *
      * @return TypeMapping instance.
      */
     public TypeMapping createTypeMapping(boolean autoTypes);
 
     /**
      * Create a type mapping with the specified encodying style.
      * 
     * @param encodingStyleURI Encoding style specified as an URI
      * @param autoTypes Should this mapping auto-generate types where possible
      * @return TypeMapping instance
      */
    public TypeMapping createTypeMapping(String encodingStyleURI, boolean autoTypes);
 
     /**
      * Unregisters a TypeMapping instance, if present, from the specified
      * encodingStyleURI.
      *
      * @param encodingStyleURI Encoding style specified as an URI
      * @return <code>TypeMapping</code> instance that has been unregistered
      *     or <code>null</code> if there was no TypeMapping
      *     registered for the specified <code>encodingStyleURI</code>
      */
     public TypeMapping unregisterTypeMapping(String encodingStyleURI);
 
     /**
      * Removes a <code>TypeMapping</code> from the TypeMappingRegistry. A
      * <code>TypeMapping</code> is associated with 1 or more
      * encodingStyleURIs. This method unregisters the specified
      * <code>TypeMapping</code> instance from all associated
      * <code>encodingStyleURIs</code> and then removes this
      * TypeMapping instance from the registry.
      *
      * @param mapping TypeMapping to remove
      * @return <code>true</code> if specified <code>TypeMapping</code>
      *     is removed from the TypeMappingRegistry; <code>false</code>
      *     if the specified <code>TypeMapping</code> was not in the
      *     <code>TypeMappingRegistry</code>
      */
     public boolean removeTypeMapping(TypeMapping mapping);
 
     /**
      * Removes all registered TypeMappings and encodingStyleURIs
      * from this TypeMappingRegistry.
      */
     public void clear();
 }
