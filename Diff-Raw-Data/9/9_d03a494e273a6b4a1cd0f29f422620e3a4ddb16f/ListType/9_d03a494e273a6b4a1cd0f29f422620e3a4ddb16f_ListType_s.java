 /*
  * Copyright 2013 StuxCrystal
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 
 package net.stuxcrystal.configuration.types;
 
 import net.stuxcrystal.configuration.ConfigurationParser;
 import net.stuxcrystal.configuration.ValueType;
 import net.stuxcrystal.configuration.exceptions.ValueException;
 import net.stuxcrystal.configuration.node.ArrayNode;
 import net.stuxcrystal.configuration.node.MapNode;
 import net.stuxcrystal.configuration.node.Node;
 import net.stuxcrystal.configuration.utils.ReflectionUtil;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Description
  *
  * @author stuxcrystal
  */
 public class ListType implements ValueType<List<?>> {
     @Override
     public boolean isValidType(Object object, Field field, Type cls) throws ReflectiveOperationException {
         return ReflectionUtil.toClass(cls).isAssignableFrom(List.class) && ReflectionUtil.getGenericArguments(cls).length == 1;
     }
 
     @Override
     public List<?> parse(Object object, Field field, ConfigurationParser parser, Type cls, Node<?> value) throws ReflectiveOperationException, ValueException {
         Node<?>[] children = ((Node<Node<?>[]>) value).getData();
         Type type = ReflectionUtil.getGenericArguments(cls)[0];
         List data = new ArrayList(children.length);
 
         for (Node<?> node : children) {
             data.add(parser.parseObject(object, field, type, node));
         }
 
         return data;
     }
 
     @Override
     public Node<?> dump(Object object, Field field, ConfigurationParser parser, Type cls, Object data) throws ReflectiveOperationException, ValueException {
         Type type = ReflectionUtil.getGenericArguments(cls)[0];
 
         Node<?>[] children = new Node<?>[((List<?>) data).size()];
         MapNode parent = new ArrayNode(null);
         for (int i = 0; i < ((List<?>) data).size(); i++) {
             Object obj = ((List<?>) data).get(i);
             Node<?> node = parser.dumpObject(object, field, type, obj);
             node.setParent(parent);
             node.setComments(new String[0]);
             children[i] = node;
         }
         parent.setData(children);
         return parent;
 
     }
 }
