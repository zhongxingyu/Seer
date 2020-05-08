 package com.xebialabs.rest.doclet;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sun.javadoc.ParameterizedType;
 import com.sun.javadoc.SeeTag;
 import com.sun.javadoc.Tag;
 import com.sun.javadoc.Type;
 import com.xebialabs.commons.html.HtmlWriter;
 
 public class RestdocWriter extends HtmlWriter {
 
     public RestdocWriter(PrintWriter writer) {
         super(writer);
     }
 
     protected String asText(Tag[] tags) {
         StringBuilder builder = new StringBuilder();
         for (Tag tag : tags) {
             if (tag instanceof SeeTag) {
                 appendLink(builder, (SeeTag) tag);
             } else if (tag.name().equals("@code")) {
                builder.append(code(tag.text()));
             } else {
                 builder.append(tag.text());
             }
         }
         return builder.toString();
     }
 
     private void appendLink(StringBuilder builder, SeeTag tag) {
         String file = RestDoclet.fileNameFor(tag.referencedClassName());
        String text = tag.label() != null ? tag.label() : tag.text();
 
         if (file != null && FileCatalog.SINGLETON.check(file)) {
             builder.append(link(file, text));
         } else {
             builder.append(bold(text));
         }
     }
 
     protected String asText(Type type) {
         StringBuilder builder = new StringBuilder();
         builder.append(type.simpleTypeName());
 
         String separator = " of ";
         for (Type paramType : getParameterizedTypes(type)) {
             builder.append(separator);
             builder.append(paramType.simpleTypeName());
             separator = ", ";
         }
 
         return builder.toString();
     }
 
     public static String firstWord(Tag tag) {
         return tag.text().split("\\s")[0];
     }
 
     public static String restOfSentence(Tag tag) {
         return tag.text().substring(firstWord(tag).length());
     }
 
     protected String asReference(Type type) {
         List<Type> types = getParameterizedTypes(type);
 
         // Default case: fully qualified name of non-parameterized type.
         if (types.isEmpty()) {
             return type.qualifiedTypeName() + ".html";
         }
 
         // Cook something up for 'List of'
         return types.get(0) + "-" + type.simpleTypeName() + ".html";
     }
 
     public List<Type> getParameterizedTypes(Type type) {
 
         List<Type> types = new ArrayList<Type>();
 
         ParameterizedType paramType = type.asParameterizedType();
         if (paramType != null) {
             for (Type param : paramType.typeArguments()) {
                 types.add(param);
             }
         }
 
         return types;
     }
 
     protected Object renderType(Type type) {
         Object returnTypeText = asText(type);
         String externalFile = asReference(type);
 
         // Add references to catalog
         if (FileCatalog.SINGLETON.check(externalFile)) {
             returnTypeText = link(externalFile, returnTypeText);
         }
         for (Type paramType : getParameterizedTypes(type)) {
             FileCatalog.SINGLETON.check(asReference(paramType));
         }
 
         return returnTypeText;
     }
 }
