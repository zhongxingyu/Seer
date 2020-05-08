 /*
  * The MIT License (MIT)
  *
  * Copyright (c) 2013 Steven Willems
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package be.idevelop.fiber;
 
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Comparator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import static be.idevelop.fiber.ObjectCreator.OBJECT_CREATOR;
 import static be.idevelop.fiber.ReferenceResolver.REFERENCE_RESOLVER;
 
 public final class ObjectSerializer<T> extends Serializer<T> implements GenericObjectSerializer {
 
     private final SortedSet<Field> fields;
 
     public ObjectSerializer(Class<T> clazz, SerializerConfig config) {
         super(clazz);
 
         this.fields = getFields(clazz, config);
     }
 
     private static SortedSet<Field> getFields(Class<?> clazz, SerializerConfig config) {
         TreeSet<Field> set = new TreeSet<Field>(new FieldComparator());
         for (Field field : clazz.getDeclaredFields()) {
             if (!(field.isSynthetic() || Modifier.isStatic(field.getModifiers()))) {
                 field.setAccessible(true);
                 config.register(field.getType());
                 set.add(field);
             }
         }
         if (clazz.getSuperclass() != null && !Object.class.equals(clazz.getSuperclass())) {
             set.addAll(getFields(clazz.getSuperclass(), config));
         }
         return set;
     }
 
     @SuppressWarnings("unchecked")
     @Override
    public T read(Input input) {
         T t = OBJECT_CREATOR.createNewInstance(getId());
         for (Field field : fields) {
             readField(input, t, field);
         }
         return t;
     }
 
     @Override
     public boolean isImmutable() {
         return false;
     }
 
     @Override
    public void write(Object object, Output output) {
         REFERENCE_RESOLVER.addForSerialize(object, getId(), isImmutable());
         for (Field field : fields) {
             try {
                 output.write(field.get(object));
             } catch (IllegalAccessException e) {
                 throw new IllegalStateException("Could not read value from field " + field.getName() + " for object " + object, e);
             }
         }
     }
 
     private void readField(Input input, T t, Field field) {
         try {
             field.set(t, input.read());
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("Could not set value for field " + field.getName() + " with value " + input.read() + " for object " + getSerializedClass().getName(), e);
         }
     }
 
     private static class FieldComparator implements Comparator<Field>, Serializable {
 
         @Override
         public int compare(Field f1, Field f2) {
             if (!f1.getName().equals(f2.getName())) {
                 return f1.getName().compareTo(f2.getName());
             } else if (!f1.getType().equals(f2.getType())) {
                 return f1.getType().getName().compareTo(f2.getType().getName());
             }
             return 0;
         }
     }
 }
