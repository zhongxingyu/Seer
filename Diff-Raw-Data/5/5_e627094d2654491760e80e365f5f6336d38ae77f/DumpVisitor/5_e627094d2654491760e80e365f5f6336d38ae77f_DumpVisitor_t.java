 /**
  * Copyright 2013 Huining (Thomas) Feng (tfeng@berkeley.edu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.bacoder.parser.core;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringEscapeUtils;
 
 import com.google.common.base.Strings;
 
 public class DumpVisitor implements Visitor<Node> {
 
   private String indent = "  ";
 
   private int level;
 
   private OutputStream outputStream;
 
   public DumpVisitor(OutputStream outputStream) {
     this.outputStream = outputStream;
   }
 
   public DumpVisitor(OutputStream outputStream, String indent) {
     this.outputStream = outputStream;
     this.indent = indent;
   }
 
   @Override
   public void visitAfter(Node node) {
     level--;
     String s = String.format("%s</%s>\n", Strings.repeat(indent, level),
         node.getClass().getSimpleName());
     try {
       outputStream.write(s.getBytes());
     } catch (IOException e) {
       throw new RuntimeException("Unable to write \"" + s + "\"", e);
     }
   }
 
   @Override
   public void visitBefore(Node node) {
     String tag = String.format("%s<%s sl=\"%d\" sc=\"%d\" el=\"%d\" ec=\"%d\">\n",
         Strings.repeat(indent, level), node.getClass().getSimpleName(), node.getStartLine(),
         node.getStartColumn(), node.getEndLine(), node.getEndColumn());
     try {
       outputStream.write(tag.getBytes());
 
       Field [] fields =  node.getClass().getDeclaredFields();
       for (Field field : fields) {
         if (field.getType().isPrimitive()
             || field.getType().isEnum()
             || String.class.isAssignableFrom(field.getType())) {
           String propertyName = field.getName();
           Object value;
           try {
             value = PropertyUtils.getSimpleProperty(node, propertyName);
             String property = String.format("%s<%s>%s</%s>\n", Strings.repeat(indent, level + 1),
                propertyName, value == null ? "" : StringEscapeUtils.escapeXml(value.toString()),
                propertyName);
             try {
               outputStream.write(property.getBytes());
             } catch (IOException e) {
               throw new RuntimeException("Unable to write \'" + property + "\'", e);
             }
           } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
             // Ignore the field.
           }
         }
       }
     } catch (IOException e) {
       throw new RuntimeException("Unable to write \'" + tag + "\'", e);
     }
     level++;
   }
 
   protected String getIndent() {
     return indent;
   }
 
   protected int getLevel() {
     return level;
   }
 
   protected OutputStream getOutputStream() {
     return outputStream;
   }
 }
