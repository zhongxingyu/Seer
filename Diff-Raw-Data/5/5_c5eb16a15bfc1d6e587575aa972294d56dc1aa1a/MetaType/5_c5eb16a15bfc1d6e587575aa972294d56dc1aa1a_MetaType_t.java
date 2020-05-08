 package org.javapickling.core;
 
 import com.google.common.collect.Maps;
 
 import java.lang.reflect.Array;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * A class which describes types.
  */
 public class MetaType {
 
     private static final String ARRAY_SUFFIX = "[]";
 
     /**
      * An enum for what we consider to be the base types.
      */
     public enum Type {
         NULL(Object.class),
         BOOLEAN(Boolean.class),
         BYTE(Byte.class),
         CHAR(Character.class),
         INT(Integer.class),
         SHORT(Short.class),
         LONG(Long.class),
         FLOAT(Float.class),
         DOUBLE(Double.class),
         ENUM(String.class),
         STRING(String.class),
         MAP(Map.class),
         LIST(List.class),
         SET(Set.class),
         OBJECT(Object.class);
 
         public final Class<?> clazz;
 
         Type(Class<?> clazz) {
             this.clazz = clazz;
         }
 
         public <PF> Pickler<?, PF> pickler(PicklerCore<PF> core, Class<?> clazz) {
             switch (this) {
                 case NULL:      return core.null_p();
                 case BOOLEAN:   return core.boolean_p();
                 case BYTE:      return core.byte_p();
                 case CHAR:      return core.char_p();
                 case INT:       return core.integer_p();
                 case SHORT:     return core.short_p();
                 case LONG:      return core.long_p();
                 case FLOAT:     return core.float_p();
                 case DOUBLE:    return core.double_p();
                case ENUM:
                    // TODO: Remove the use of Type from the following line once we upgrade to Java 7.
                    // It's required for the Java 6 compiler as the type inference is too weak.
                    return core.enum_p(MetaType.<Type>castEnumClass(clazz));
                 case STRING:    return core.string_p();
                 case MAP:       return core.map_p(core.object_p(), core.object_p(), (Class<Map<Object, Object>>)clazz);
                 case LIST:      return core.list_p(core.object_p(), (Class<List<Object>>)clazz);
                 case SET:       return core.set_p(core.object_p(), (Class<Set<Object>>)clazz);
                 case OBJECT:    return core.object_p(clazz);
                 default:        throw new PicklerException("Unexpected Type value - " + name());
             }
         }
     }
 
     private static <T extends Enum<T>> Class<T> castEnumClass(Class<?> clazz) {
         return (Class<T>)clazz;
     }
 
     private static final Map<String, Type> classTypeMap = Maps.newTreeMap();
 
     static {
         register(Boolean.class, Type.BOOLEAN);
         register(Byte.class, Type.BYTE);
         register(Character.class, Type.CHAR);
         register(Integer.class, Type.INT);
         register(Short.class, Type.SHORT);
         register(Long.class, Type.LONG);
         register(Float.class, Type.FLOAT);
         register(Double.class, Type.DOUBLE);
         register(String.class, Type.STRING);
     }
 
     private static void register(Class<?> clazz, Type type) {
         classTypeMap.put(clazz.getName(), type);
     }
 
     public static MetaType ofObject(Object obj) {
 
         if (obj == null)
             return new MetaType((Type.NULL));
 
         Class<?> clazz = obj.getClass();
 
         int arrayDepth = 0;
         while (clazz.isArray()) {
             ++arrayDepth;
             clazz = clazz.getComponentType();
         }
 
         if (clazz.isEnum()) {
             return new MetaType(Type.ENUM, clazz, arrayDepth);
         } else {
             Type type = classTypeMap.get(clazz.getName());
             if (type != null) {
                 return new MetaType(type, arrayDepth);
             } else {
                 return new MetaType(Type.OBJECT, clazz, arrayDepth);
             }
         }
     }
 
     public static MetaType ofName(String name) {
 
         int arrayDepth = 0;
         while (name.endsWith(ARRAY_SUFFIX)) {
             ++arrayDepth;
             name = name.substring(0, name.length() - 2);
         }
 
         final Type type = Type.valueOf(name);
         return new MetaType(type, arrayDepth);
     }
 
     public final Type type;
     public Class<?> clazz;
     public final int arrayDepth;
 
     public MetaType(Type type, Class<?> clazz, int arrayDepth) {
         this.type = type;
         this.clazz = clazz;
         this.arrayDepth = arrayDepth;
     }
 
     public MetaType(Type type, Class<?> clazz) {
         this(type, clazz, 0);
     }
 
     public MetaType(Type type, int arrayDepth) {
         this(type, null, arrayDepth);
     }
 
     public MetaType(Type type) {
         this(type, null, 0);
     }
 
     public <PF> Pickler<Object, PF> pickler(PicklerCore<PF> core) {
         Pickler<?, PF> pickler = type.pickler(core, clazz);
         Class<?> arrClazz = clazz == null ? type.clazz : clazz;
         for (int i = 0; i < arrayDepth; ++i) {
             pickler = core.array_p((Pickler<Object, PF>)pickler, (Class<Object>)arrClazz);
             arrClazz = Array.newInstance(arrClazz, 0).getClass();
         }
 
         return (Pickler<Object, PF>)pickler;
     }
 
     public String name() {
         final StringBuilder sb = new StringBuilder(type.name());
         for (int i = 0; i < arrayDepth; ++i) {
             sb.append(ARRAY_SUFFIX);
         }
         return sb.toString();
     }
 }
