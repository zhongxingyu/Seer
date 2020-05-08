 /**********************************************************************
 Copyright (c) 2010 Ghais Issa and others. All rights reserved.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 Contributors :
     ...
  ***********************************************************************/
 package org.datanucleus.store.hbase;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.datanucleus.ClassLoaderResolver;
 import org.datanucleus.api.ApiAdapter;
 import org.datanucleus.exceptions.NucleusException;
 import org.datanucleus.metadata.AbstractClassMetaData;
 import org.datanucleus.metadata.AbstractMemberMetaData;
 import org.datanucleus.metadata.MetaDataManager;
 import org.datanucleus.metadata.Relation;
 import org.datanucleus.store.ExecutionContext;
 import org.datanucleus.store.ObjectProvider;
 import org.datanucleus.store.fieldmanager.AbstractFieldManager;
 import org.datanucleus.store.types.sco.backed.Vector;
 
 public class HBaseFetchFieldManager extends AbstractFieldManager
 {
     Result result;
 
     ObjectProvider objectProvider;
 
     public HBaseFetchFieldManager(ObjectProvider objectProvider, Result result)
     {
         this.result = result;
         this.objectProvider = objectProvider;
     }
 
     public boolean fetchBooleanField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
 
         return Bytes.toBoolean(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
     }
 
     public byte fetchByteField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
         return (byte) Bytes.toShort(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
 
     }
 
     public char fetchCharField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
         return Bytes.toChar(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
 
     }
 
     public double fetchDoubleField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
         return Bytes.toDouble(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
 
     }
 
     public float fetchFloatField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
         return Bytes.toFloat(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
     }
 
     public int fetchIntField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
         return Bytes.toInt(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
     }
 
     public long fetchLongField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
         return Bytes.toLong(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
 
     }
 
     public Object fetchObjectField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
 
         ClassLoaderResolver clr = getClassLoaderResolver();
         AbstractMemberMetaData fieldMetaData = getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
 
         // get object
         Object value;
         try
         {
             try
             {
                 byte[] bytes = result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName));
                 if (bytes == null || bytes.length == 0)
                 {
                     return null;
                 }
                 ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInputStream ois = new ObjectInputStream(bis);
                 value = ois.readObject();
                 ois.close();
                 bis.close();
             }
             catch (NullPointerException ex)
             {
                 return null;
             }
         }
         catch (IOException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
         catch (ClassNotFoundException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
 
         // handle relations
         int relationType = fieldMetaData.getRelationType(clr);
 
         switch (relationType)
         {
             case Relation.ONE_TO_ONE_BI :
             case Relation.ONE_TO_ONE_UNI :
             {
 
                 ExecutionContext context = objectProvider.getExecutionContext();
                 Object id = value;
                 String class_name = fieldMetaData.getClassName();
                 value = context.findObject(id, true, false, class_name);
                 break;
             }
             case Relation.ONE_TO_MANY_UNI :
             case Relation.ONE_TO_MANY_BI :
             {
                 ExecutionContext context = objectProvider.getExecutionContext();
                 MetaDataManager mmgr = context.getMetaDataManager();
 
                 if (fieldMetaData.hasCollection())
                 {
 
                     String elementClassName = fieldMetaData.getCollection().getElementType();
 
                     List<Object> mapping = (List<Object>) value;
                     Collection<Object> collection = null;
                     if (TreeSet.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new TreeSet<Object>();
                     }
                     else if (LinkedHashSet.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new LinkedHashSet<Object>();
                     }
                     else if (HashSet.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new HashSet<Object>();
                     }
                     else if (ArrayList.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new ArrayList<Object>();
                     }
                     else if (LinkedList.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new LinkedList<Object>();
                     }
                     else if (Vector.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new java.util.Vector<Object>();
                     }
                     else if (List.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new ArrayList<Object>();
                     }
                     else if (SortedSet.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new TreeSet<Object>();
                     }
                     else if (Set.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         collection = new HashSet<Object>();
                     }
                    else if (Collection.class.isAssignableFrom(fieldMetaData.getType()))
                    {
                        collection = new ArrayList<Object>();
                    }
                     for (Object id : mapping)
                     {
 
                         Object element = context.findObject(id, true, false, elementClassName);
                         collection.add(element);
                     }
                     value = collection;
                 }
 
                 else if (fieldMetaData.hasMap())
                 {
                     // Process all keys, values of the Map that are PC
 
                     String key_elementClassName = fieldMetaData.getMap().getKeyType();
                     String value_elementClassName = fieldMetaData.getMap().getValueType();
 
                     Map<Object, Object> mapping = null;
                     if (HashMap.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         mapping = Utils.newHashMap();
                     }
                     else if (TreeMap.class.isAssignableFrom(fieldMetaData.getType()))
                     {
                         mapping = Utils.newTreehMap();
                     }
                     else
                     {
                         mapping = Utils.newHashMap();
                     }
 
                     Map map = (Map) value;
                     ApiAdapter api = context.getApiAdapter();
 
                     Set keys = map.keySet();
                     Iterator iter = keys.iterator();
                     while (iter.hasNext())
                     {
                         Object mapKey = iter.next();
                         Object key = null;
 
                         if (mapKey instanceof javax.jdo.identity.SingleFieldIdentity)
                         {
                             key = context.findObject(mapKey, true, false, key_elementClassName);
 
                         }
                         else
                         {
                             key = mapKey;
                         }
 
                         Object mapValue = map.get(key);
                         Object key_value = null;
 
                         if (mapValue instanceof javax.jdo.identity.SingleFieldIdentity)
                         {
 
                             key_value = context.findObject(mapValue, true, false, value_elementClassName);
                         }
                         else
                         {
                             key_value = mapValue;
                         }
 
                         mapping.put(key, key_value);
                     }
 
                     value = mapping;
                 }
                 break;
             }
 
             default :
                 break;
         }
         return value;
     }
 
     public short fetchShortField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
         return Bytes.toShort(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
 
     }
 
     public String fetchStringField(int fieldNumber)
     {
         String familyName = Utils.getFamilyName(getClassMetaData(), fieldNumber);
         String columnName = Utils.getQualifierName(getClassMetaData(), fieldNumber);
         try
         {
             return Bytes.toString(result.getValue(Bytes.toBytes(familyName), Bytes.toBytes(columnName)));
         }
         catch (NullPointerException ex)
         {
             return null;
         }
     }
 
     /**
      * @return
      */
     AbstractClassMetaData getClassMetaData()
     {
         return objectProvider.getClassMetaData();
     }
 
     /**
      * @return
      */
     ClassLoaderResolver getClassLoaderResolver()
     {
         return objectProvider.getExecutionContext().getClassLoaderResolver();
     }
 }
