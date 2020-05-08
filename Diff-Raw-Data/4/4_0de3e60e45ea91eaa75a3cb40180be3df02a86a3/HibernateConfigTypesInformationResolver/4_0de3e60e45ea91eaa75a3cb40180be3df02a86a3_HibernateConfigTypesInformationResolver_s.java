 package org.cagrid.cacore.sdk4x.cql2.processor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.mapping.Collection;
 import org.hibernate.mapping.OneToMany;
 import org.hibernate.mapping.PersistentClass;
 import org.hibernate.mapping.Property;
 import org.hibernate.mapping.RootClass;
 import org.hibernate.mapping.Subclass;
 import org.hibernate.mapping.ToOne;
 import org.hibernate.mapping.Value;
 
 
 /**
  * Types Information Resolver implementation which leverages 
  * the information in a Hibernate configuration
  * 
  * @author David Ervin
  */
 public class HibernateConfigTypesInformationResolver implements TypesInformationResolver {
 	
 	private static Log LOG = LogFactory.getLog(HibernateConfigTypesInformationResolver.class);
 
     private Configuration configuration = null;
     private Map<String, Boolean> subclasses = null;
     private Map<String, Object> discriminators = null;
     private Map<String, Class<?>> fieldDataTypes = null;
     private Map<String, List<ClassAssociation>> classAssociations = null;
     private Map<String, String> roleNames = null;
     
     public HibernateConfigTypesInformationResolver(Configuration hibernateConfig) {
         this.configuration = hibernateConfig;
         this.subclasses = new HashMap<String, Boolean>();
         this.discriminators = new HashMap<String, Object>();
         this.fieldDataTypes = new HashMap<String, Class<?>>();
         this.classAssociations = new HashMap<String, List<ClassAssociation>>();
         this.roleNames = new HashMap<String, String>();
     }
 
 
     public Object getClassDiscriminatorValue(String classname) throws TypesInformationException {
     	LOG.debug("Getting class discriminator value for " + classname);
         Object identifier = discriminators.get(classname);
         if (identifier == null) {
             PersistentClass clazz = configuration.getClassMapping(classname);
             if (clazz != null) {
                 if (clazz instanceof Subclass) {
                 	Subclass sub = (Subclass) clazz;
                     if (sub.isJoinedSubclass()) {
                     	LOG.debug("\t" + classname + " is a joined subclass");
                         identifier = Integer.valueOf(sub.getSubclassId());
                     } else {
                     	LOG.debug("\t" + classname + " is a named subclass");
                         identifier = getShortClassName(classname);
                     }
                 } else if (clazz instanceof RootClass) {
                 	LOG.debug("\t" + classname + " is a root class");
                     RootClass root = (RootClass) clazz;
                     if (root.getDiscriminator() == null) {
                         identifier = Integer.valueOf(root.getSubclassId());
                     } else {
                         identifier = getShortClassName(classname);
                     }
                 }
             } else {
                 throw new TypesInformationException("Class " + classname + " not found in hibernate configuration");
             }
             discriminators.put(classname, identifier);
         }
         return identifier;
     }
 
 
     public boolean classHasSubclasses(String classname) throws TypesInformationException {
     	LOG.debug("Checking if " + classname + " has subclasses");
         Boolean hasSubclasses = subclasses.get(classname);
         if (hasSubclasses == null) {
         	PersistentClass clazz = findPersistentClass(classname);
             if (clazz != null) {
                 hasSubclasses = Boolean.valueOf(clazz.hasSubclasses());
                 subclasses.put(classname, hasSubclasses);
             } else {
                 throw new TypesInformationException("Class " + classname + " not found in configuration");
             }
         }
         return hasSubclasses.booleanValue();
     }
 
 
     public Class<?> getJavaDataType(String classname, String field) throws TypesInformationException {
     	LOG.debug("Getting java type of " + classname + "." + field);
         String fqName = classname + "." + field;
         Class<?> type = fieldDataTypes.get(fqName);
         if (type == null) {
             PersistentClass clazz = findPersistentClass(classname);
             if (clazz != null) {
                 // TODO: test that this barks up the inheritance tree for properties
                 Property property = clazz.getRecursiveProperty(field);
                 if (property != null) {
                     type = property.getType().getReturnedClass();
                 } else {
                     throw new TypesInformationException("Field " + fqName + " not found in hibernate configuration");
                 }
             } else {
                 throw new TypesInformationException("Class " + classname + " not found in hibernate configuration");
             }
             fieldDataTypes.put(fqName, type);
         }
         return type;
     }
 
 
     public String getEndName(String parentClassname, String childClassname) throws TypesInformationException {
     	LOG.debug("Getting the association end name for " + parentClassname + " to " + childClassname);
         String identifier = getAssociationIdentifier(parentClassname, childClassname);
         String roleName = roleNames.get(identifier);
         if (roleName == null) {
             PersistentClass clazz = findPersistentClass(parentClassname);
             Iterator<?> propertyIter = clazz.getPropertyIterator();
             while (propertyIter.hasNext()) {
                 Property prop = (Property) propertyIter.next();
                 Value value = prop.getValue();
                 String referencedEntity = null;
                 if (value instanceof Collection) {
                 	Value element = ((Collection) value).getElement();
                     if (element instanceof OneToMany) {
                         referencedEntity = ((OneToMany) element).getReferencedEntityName();
                     } else if (element instanceof ToOne) {
                         referencedEntity = ((ToOne) element).getReferencedEntityName();
                     }
                 } else if (value instanceof ToOne) {
                     referencedEntity = ((ToOne) value).getReferencedEntityName();
                 }
                 if (childClassname.equals(referencedEntity)) {
                     if (roleName != null) {
                         // already found one association, so this is ambiguous
                         throw new TypesInformationException("Association from " + parentClassname + " to " 
                             + childClassname + " is ambiguous.  Please specify a valid role name");
                     }
                     roleName = prop.getName();
                 }
             }
         }
         return roleName;
     }
     
     
     public List<ClassAssociation> getAssociationsFromClass(String parentClassname) throws TypesInformationException {
     	LOG.debug("Getting associations from class " + parentClassname);
         List<ClassAssociation> associations = classAssociations.get(parentClassname);
         if (associations == null) {
             associations = new ArrayList<ClassAssociation>();
             PersistentClass clazz = findPersistentClass(parentClassname);
             Iterator<?> propertyIter = clazz.getPropertyIterator();
             while (propertyIter.hasNext()) {
                 Property property = (Property) propertyIter.next();
                 Value value = property.getValue();
                 if (value instanceof ToOne || value instanceof OneToMany) {
                     ClassAssociation assoc = new ClassAssociation(property.getType().getName(), property.getName());
                     associations.add(assoc);
                 }
             }
             classAssociations.put(parentClassname, associations);
         }
         return associations;
     }
 
     
     private String getShortClassName(String className) {
         int dotIndex = className.lastIndexOf('.');
         return className.substring(dotIndex + 1);
     }
     
     
     private String getAssociationIdentifier(String parentClassname, String childClassname) {
         return parentClassname + "-->" + childClassname;
     }
     
     
     private PersistentClass findPersistentClass(String className) {
     	PersistentClass pc = configuration.getClassMapping(className);
     	if (pc == null) {
     		pc = getImplicitClass(className);
     	}
     	return pc;
     }
     
     
     private PersistentClass getImplicitClass(String className) {
     	PersistentClass implicit = null;
     	Iterator classIter = configuration.getClassMappings();
     	while (classIter.hasNext() && implicit == null) {
     		PersistentClass pc = (PersistentClass) classIter.next();
     		if (pc.getSuperclass() != null && pc.getSuperclass().getClassName().equals(className)) {
     			implicit = pc.getSuperclass();
     		}
     	}
     	return implicit;
     }
 }
