 /*
     This file is part of the object-browser.
 
     The object-browser is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     The object-browser is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with the object-browser.  If not, see <http://www.gnu.org/licenses/>.
 
     Copyright 2009 Johannes Rudolph
 */
 
 package net.virtualvoid.android.browser;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class MetaItemFactory {
     static abstract class MappedListMetaItemList<T> implements MetaItemList{
         private List<T> list;
         private CharSequence name;
 
         public MappedListMetaItemList(CharSequence name,List<T> list) {
             this.name = name;
             this.list = list;
         }
 
         protected abstract MetaItem map(T ele);
 
         @Override
         public MetaItem get(int position) {
             return map(list.get(position));
         }
         @Override
         public CharSequence getName() {
             return name;
         }
         @Override
         public int size() {
             return list.size();
         }
     }
     private static MetaItemList fromList(final String name,final List<MetaItem> items){
         return new MetaItemList(){
             @Override
             public MetaItem get(int position) {
                 return items.get(position);
             }
             @Override
             public String getName() {
                 return name;
             }
             @Override
             public int size() {
                 return items.size();
             }
         };
     }
     private static MetaItemList fromItems(final String name,final MetaItem... items){
         return new MetaItemList(){
             @Override
             public MetaItem get(int position) {
                 return items[position];
             }
             @Override
             public String getName() {
                 return name;
             }
             @Override
             public int size() {
                 return items.length;
             }
         };
     }
 
     private static boolean isProperty(Method m){
         return !isStatic(m) // not static
                 && (m.getName().startsWith("get") || m.getName().startsWith("is"))
                 && m.getParameterTypes().length == 0;
     }
     private static boolean isStatic(Member m){
         return (m.getModifiers()&Modifier.STATIC) != 0;
     }
     private static ArrayList<Method> propertyMethodsOf(final Class<?> clazz){
         Method[] ms = clazz.getMethods();
         ArrayList<Method> res = new ArrayList<Method>(ms.length);
         for(Method m:ms)
             if(isProperty(m))
                 res.add(m);
         return res;
     }
     private static MetaItemList propertiesOf(final Class<?> clazz){
         return new MappedListMetaItemList<Method>("Properties",propertyMethodsOf(clazz)){
             @Override
             protected MetaItem map(final Method m) {
                 return new MetaItem(){
                     {
                         m.setAccessible(true);
                     }
                     @Override
                     public Object get(Object parent){
                         try {
                             return m.invoke(parent);
                         } catch (Exception e) {
                             throw new RuntimeException(e);
                         }
                     }
                     @Override
                     public String getName() {
                         return m.getName();
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return m.getReturnType();
                     }
                 };
             }
         };
     }
     private static MetaItemList fieldsOf(Class<?> cur){
         ArrayList<MetaItem> res = new ArrayList<MetaItem>();
 
         while(cur != null){
             for (final Field f:cur.getDeclaredFields())
                 if (!isStatic(f))
                     res.add(new MetaItem(){
                         {
                             f.setAccessible(true);
                         }
                         @Override
                         public Object get(Object o){
                             try {
                                 return f.get(o);
                            } catch (Exception e) {
                                 throw new RuntimeException(e);
                             }
                         }
                         @Override
                         public String getName() {
                             return f.getName();
                         }
                         @Override
                         public Class<?> getReturnType() {
                             return f.getType();
                         }
                     });
             cur = cur.getSuperclass();
         }
 
         return fromList("Fields",res);
     }
 
     private static MetaItemList arrayInformation(final Class<?> clazz){
         return fromItems("Array information"
                 ,new MetaItem(){
                     @Override
                     public Object get(Object o) {
                         return Array.getLength(o);
                     }
                     @Override
                     public CharSequence getName() {
                         return "Length";
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return Integer.class;
                     }
                 }
                 ,new MetaItem(){
                     @Override
                     public Object get(Object o) {
                         return o.getClass().getComponentType();
                     }
                     @Override
                     public CharSequence getName() {
                         return "Component Type";
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return Class.class;
                     }
                 });
     }
     public static ArrayList<MetaItemList> metaItemsFor(Class<?> clazz){
         ArrayList<MetaItemList> res = new ArrayList<MetaItemList>(){
             private static final long serialVersionUID = 1L;
 
             @Override
             public boolean add(MetaItemList list) {
                 if (list.size() > 0)
                     return super.add(list);
                 else
                     return false;
             }
         };
 
         res.add(fieldsOf(clazz));
         res.add(propertiesOf(clazz));
 
         if (clazz.isArray())
             res.add(arrayInformation(clazz));
 
         return res;
     }
 }
