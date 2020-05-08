 package org.xillium.data.validation;
 
 import org.xillium.base.Trace;
 import org.xillium.data.DataBinder;
 import org.xillium.data.DataObject;
 import java.lang.reflect.*;
 import java.util.*;
 
 
 /**
  * A dictionary of extended types for data validation.
  */
 public class Dictionary {
     protected static final Map<Class<?>, Map<String, Validator>> _cachedValidators = new HashMap<Class<?>, Map<String, Validator>>();
 
     public static synchronized Validator cache(Class<?> type, String name, Validator validator) {
         Map<String, Validator> validators = _cachedValidators.get(type);
         if (validators == null) {
             validators = new HashMap<String, Validator>();
             _cachedValidators.put(type, validators);
         }
         validators.put(name, validator);
         return validator;
     }
 
     public static synchronized Validator find(Class<?> type, String name) {
         Map<String, Validator> validators = _cachedValidators.get(type);
         if (validators != null) {
             return validators.get(name);
         }
         return null;
     }
 
     //String _namespace;
     Map<String, Validator> _namedValidators = new HashMap<String, Validator>();
     
     /**
      * Constructs a Dictionary associated with the given name space.
      */
     //public Dictionary() {
         //_namespace = namespace;
     //}
 
     /**
      * Adds a set of data type specifications.
      * @param spec - a class that defines data types as member fields
      */
     public Dictionary addTypeSet(Class<?> spec) {
         for (Field field: spec.getDeclaredFields()) {
             if (Modifier.isPublic(field.getModifiers())) {
                 String name = field.getName();
                 try {
                     _namedValidators.put(name, new Validator(name, field.getType(), field));
                 } catch (IllegalArgumentException x) {
                     Trace.g.std.note(Dictionary.class, "Ignored " + name + ": " + x.getMessage());
                 }
             } else {
                 Trace.g.std.note(Dictionary.class, "Ignored non-public field: " + field.getName());
             }
         }
 
         return this;
     }
 
     public Map<String, Validator> getValidators() {
         return _namedValidators;
     }
 
     /**
      * Populates a data object by collecting and validating the named values from a DataBinder.
      *
      * @throws EmptyDataObjectException if a required member is missing and the data object is empty
      * @throws MissingParameterException if a required member is missing but the data object has other members
      * @throws DataValidationException if all other data validation fails
      * @throws SecurityException if the data object is inproperly designed
      */
     public <T extends DataObject> T collect(T data, DataBinder binder) throws SecurityException, DataValidationException {
         return collect(data, binder, null);
     }
 
     private final <T extends DataObject> T collect(T data, DataBinder binder, String prefix) throws SecurityException, DataValidationException {
         int present = 0;
         String absent = null;
 
         for (Field field: data.getClass().getFields()) {
             if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
 
             Class<?> ftype = field.getType();
             String name = field.getName();
             String qualified = prefix != null ? prefix + '.' + name : name;
             Trace.g.std.note(Dictionary.class, "collect(): qualified = " + qualified);
 
             if (ftype.isArray()) {
                 Trace.g.std.note(Dictionary.class, "collect(): field is an array");
                 ArrayList<Object> list = new ArrayList<Object>();
                 Class<?> ctype = ftype.getComponentType();
                 
                 if (DataObject.class.isAssignableFrom(ctype)) {
                     Trace.g.std.note(Dictionary.class, "collect(): DataObject array");
                     for (int index = 0; true; ++index) {
                         try {
                             list.add(collect((DataObject)ctype.newInstance(), binder, qualified + '[' + index + ']'));
                         } catch (EmptyDataObjectException x) {
                             Trace.g.std.note(Dictionary.class, "DataObject array '" + qualified + "': no more elements");
                             break;
                         } catch (InstantiationException x) {
                             throw new ValidationSpecificationException("Impossible to instantiate " + ctype.getName(), x);
                         } catch (IllegalAccessException x) {
                             throw new ValidationSpecificationException("Impossible to instantiate " + ctype.getName(), x);
                         }
                     }
                 } else {
                     Trace.g.std.note(Dictionary.class, "collect(): simple array");
                     for (int index = 0; true; ++index) {
                         String text = binder.get(qualified + '[' + index + ']');
                         if (text != null) {
                             list.add(translate(field, name, text));
                         } else {
                             Trace.g.std.note(Dictionary.class, "Simple array '" + qualified + "': no more elements");
                             break;
                         }
                     }
                 }
                 Trace.g.std.note(Dictionary.class, "collect(): array - get elements " + list.size());
                 if (list.size() > 0) {
                     Trace.g.std.note(Dictionary.class, "Storing array '" + qualified + "' with length " + list.size());
                     try {
                         field.set(data, list.toArray((Object[])Array.newInstance(ctype, list.size())));
                     } catch (IllegalAccessException x) {
                         // should not happen
                         throw new RuntimeException("While setting array field " + field, x);
                     }
                     Trace.g.std.note(Dictionary.class, "Array '" + qualified + "' stored");
                 } else if (field.getAnnotation(required.class) != null) {
                     throw new MissingParameterException(
                         "MissingRequiredParameter{"+name+"}# in " + (prefix != null ? prefix : "") + '(' + data.getClass().getName() + ')'
                     );
                 } else {
                     continue;
                 }
             } else if (DataObject.class.isAssignableFrom(ftype)) {
                 try {
                     field.set(data, collect((DataObject)ftype.newInstance(), binder, qualified));
                 } catch (EmptyDataObjectException x) {
                     if (isRequired(data, field, prefix, name, present)) {
                         absent = name;
                     }
                     continue;
                 } catch (InstantiationException x) {
                     throw new ValidationSpecificationException("Impossible to instantiate " + ftype.getName(), x);
                 } catch (IllegalAccessException x) {
                     throw new RuntimeException("While setting field " + field, x);
                 }
             } else {
                 String text = binder.get(qualified);
                 if (text != null && text.length() > 0) {
                     if (absent != null) {
                         // now report missing required parameters
                         throw new MissingParameterException(
                         "MissingRequiredParameter{"+absent+"}# in " + (prefix != null ? prefix : "") + '('+data.getClass().getName()+')'
                         );
                     } else {
                         try {
                             field.set(data, translate(field, name, text));
                         } catch (IllegalAccessException x) {
                             throw new RuntimeException("While setting field " + field, x);
                         }
                     }
                 } else {
                     Object prefill = null;
                     try {
                         prefill = field.get(data);
                     } catch (Throwable t) {}
                     if (prefill != null) {
                         // re-translate the value, passing it through validation
                        translate(field, name, String.valueOf(prefill));
                    } else if (isRequired(data, field, prefix, name, present)) {
                         absent = name;
                     }
                     continue;
                 }
             }
 
             Trace.g.std.note(Dictionary.class, "Got " + name);
             ++present;
         }
 
         // EmptyDataObjectException should never be thrown for the top-level object (where prefix == null)
         if (present == 0 && prefix != null) {
             throw new EmptyDataObjectException(prefix);
         } else if (prefix == null && absent != null) {
             throw new MissingParameterException("MissingRequiredParameter{"+absent+"}# in ("+data.getClass().getName()+')');
         } else {
             return data;
         }
     }
 
     /*!
      * Translates a text string to a value of the appropriate type for the given field.
      */
     private final Object translate(Field field, String name, String text) throws DataValidationException {
         Object value = null;
 
         try {
             // in-place validator first
             Class<?> type = field.getType();
             if (type.isArray()) {
                 type = type.getComponentType();
             }
 
             Validator inplaceValidator = find(field.getDeclaringClass(), name);
             if (inplaceValidator == null) {
                 Trace.g.std.note(Dictionary.class, "New Validator for type " + type);
                 inplaceValidator = cache(field.getDeclaringClass(), name, new Validator(name, type, field));
             }
             /*
             Trace.g.std.note(Dictionary.class, "New Validator for type " + type);
             Validator inplaceValidator = new Validator(name, type, field);
             */
 
             // validations in extended-type?
             subtype restriction = field.getAnnotation(subtype.class);
             Validator namedValidator = restriction != null ? _namedValidators.get(restriction.value()) : null;
 
             if (namedValidator != null) {
                 inplaceValidator.preValidate(text);
                 value = namedValidator.parse(text);
                 inplaceValidator.postValidate(value);
             } else {
                 value = inplaceValidator.parse(text);
             }
         } catch (IllegalArgumentException x) {
             throw new ValidationSpecificationException(field.getDeclaringClass().getSimpleName() + '.' + name, x);
         }
 
         return value;
     }
 
     /*!
      * Tests whether the specified field is a required field.
      *
      * @param data - the containing data object
      * @param field - the field
      * @param prefix - the current prefix indicating the name of the containing data object
      * @param name - the cached name of the field
      * @param present - the number of data members already having values
      *
      * @throws MissingParameterException if the field is required and there's already another member present (present > 0)
      * @return true if the field is required, false otherwise
      */
     private static final boolean isRequired(DataObject data, Field field, String prefix, String name, int present)
     throws MissingParameterException {
         if (field.getAnnotation(required.class) != null) {
             //if (present == 0) {
             if (present == 0 && prefix != null) { // Brian 3/9/2012
                 // hold the exception report as long as the data object is empty
                 Trace.g.std.note(Dictionary.class, "Hold the exception report on field " + name);
                 return true;
             } else {
                 Trace.g.std.note(Dictionary.class, "Data object already has " + present + " member values");
                 throw new MissingParameterException(
                     "MissingRequiredParameter{"+name+"}# in " + (prefix != null ? prefix : "") + '(' + data.getClass().getName() + ')'
                 );
             }
         } else {
             return false;
         }
     }
 }
