 /**
  * Licensed to jclouds, Inc. (jclouds) under one or more
  * contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  jclouds licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.jclouds.cleanup.output;
 
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.sun.javadoc.AnnotationDesc;
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.FieldDoc;
 import com.sun.javadoc.MethodDoc;
 import com.sun.javadoc.ProgramElementDoc;
 import com.sun.javadoc.Tag;
 import com.sun.javadoc.Type;
 
 public abstract class ClassDocPrinter {
    public static final String[] booleanAccesorNames = {"is", "may", "can", "should", "cannot", "not"};
    protected static final Map<String, String> collectionTypes = ImmutableMap.of("Set", "Sets.newLinkedHashSet", "List", "Lists.newArrayList");
 
    /**
     * Have to code up what you want printed!
     */
    public abstract void write(ClassDoc data, PrintWriter out);
 
    public static final Set<String> DEFAULT_IMPORTS =
          ImmutableSet.of(
                "import static com.google.common.base.Preconditions.checkNotNull;",
                "import java.util.Collections;",
                "import org.jclouds.javax.annotation.Nullable;",
                "import com.google.common.collect.ImmutableCollection;",
                "import com.google.common.collect.ImmutableList;",
                "import com.google.common.collect.ImmutableSet;",
                "import com.google.common.collect.Lists;",
                "import com.google.common.collect.Sets;",
                "import com.google.common.base.Objects;",
                "import com.google.common.base.Objects.ToStringHelper;"
          );
 
    protected final Set<String> imports = Sets.newLinkedHashSet(DEFAULT_IMPORTS);
    protected final Set<ClassDocPrinter> children = Sets.newLinkedHashSet();
 
    protected void addImports(Collection<String> newImports) {
       this.imports.addAll(newImports);
       for (ClassDocPrinter pw : children) {
          pw.addImports(newImports);
       }
    }
 
    protected void addChildren(Collection<ClassDocPrinter> children) {
       this.children.addAll(children);
    }
 
    protected String getAccessorName(FieldDoc var) {
       String fieldName = var.name();
       String fieldNameUC = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
       String getterName = null;
       if (Objects.equal(var.type().simpleTypeName().toLowerCase(), "boolean")) {
          for (String starter : booleanAccesorNames) {
             if (fieldName.startsWith(starter)) {
                getterName = fieldName;
             }
          }
          if (getterName == null) {
             getterName = "is" + fieldNameUC;
          }
       } else {
          getterName = "get" + fieldNameUC;
       }
       return getterName;
    }
 
    protected void writeAnnotation(ProgramElementDoc element, PrintWriter out) {
       for (AnnotationDesc atree : element.annotations()) {
          out.print("@" + atree.annotationType().simpleTypeName());
          if (atree.elementValues().length > 0) {
             out.print("(" + Joiner.on(", ").join(atree.elementValues()) + ")");
          }
          out.println();
       }
    }
 
    protected Set<FieldDoc> instanceFields(ClassDoc classDoc) {
       Set<FieldDoc> result = Sets.newLinkedHashSet();
       for (FieldDoc field : classDoc.fields()) {
          if (!field.isStatic()) {
             result.add(field);
          }
       }
       return result;
    }
 
    protected void writeFieldComment(FieldDoc field, PrintWriter out, Map<String, String> tags) {
       String accessorName = getAccessorName(field);
       ProgramElementDoc accessor = null;
       for (MethodDoc method : field.containingClass().methods()) {
          if (Objects.equal(accessorName, method.name())) {
             accessor = method;
          }
       }
       writeComment(out, null, tags, field, accessor);
    }
 
    protected void writeComment(PrintWriter out, String defaultCommentText, ProgramElementDoc... elements) {
       writeComment(out, defaultCommentText, ImmutableMap.<String, String>of(), elements);
    }
 
    private void writeComment(PrintWriter out, String defaultCommentText, Map<String, String> defaultTags, ProgramElementDoc... elements) {
 
       String commentText = null;
       for (ProgramElementDoc element : elements) {
          if (element != null && element.commentText() != null && !element.commentText().trim().isEmpty()) {
             commentText = element.commentText();
             break;
          }
       }
 
       if (commentText == null) {
          commentText = defaultCommentText;
       }
 
       Map<String, String> tagsToOutput = Maps.newHashMap(defaultTags);
 
       // TODO multimap?
       for (ProgramElementDoc element : elements) {
          if (element != null) {
             for (Tag tag : element.tags()) {
                tagsToOutput.put(tag.name(), tag.text());
             }
          }
       }
 
       if (commentText == null && tagsToOutput.isEmpty()) return;
 
       out.println("/**");
       if (commentText != null) {
          for (String line : commentText.trim().split("\n")) {
             out.println("* " + line);
          }
          if (!tagsToOutput.isEmpty()) {
             out.println("*");
          }
       }
 
       for (Map.Entry<String, String> tag : tagsToOutput.entrySet()) {
          out.println("* " + tag.getKey() + " " + tag.getValue());
       }
 
       out.println("*/");
    }
 
    protected String getUnmodifiableCopyIfCollection(FieldDoc variableTree) {
       return fiddleIfCollection(variableTree, "Collections.unmodifiable%s(%s)");
    }
 
    protected String fiddleIfCollection(FieldDoc var, String format) {
       return fiddleIfCollection(var, format, var.name());
    }
 
    protected boolean extendsSomething(ClassDoc theClass) {
       return (theClass.superclass() != null && !Objects.equal("java.lang.Object", theClass.superclass().qualifiedName()));
    }
 
    protected String properTypeName(FieldDoc field) {
       String fieldType = field.type().simpleTypeName();
       if (field.type().asParameterizedType() != null) {
          fieldType += "<";
          for (Type type : field.type().asParameterizedType().typeArguments()) {
             fieldType += (type.asClassDoc().containingPackage().equals(field.containingPackage()) ? type.simpleTypeName() : type.qualifiedTypeName()) + ", ";
          }
          fieldType = fieldType.substring(0, fieldType.length() - 2);
          fieldType += ">";
       }
 
      if (field.type().isPrimitive() ||
            field.type().asClassDoc().containingPackage().name().equals("java.lang") ||
            field.type().asClassDoc().containingPackage().equals(field.containingClass().containingPackage()) ||
             imports.contains("import " + field.type().qualifiedTypeName() + ";") ||
             imports.contains("import " + field.type().asClassDoc().containingPackage().name() + "*" + ";")) {
          if (field.type().asClassDoc().containingClass() != null) {
             return field.type().asClassDoc().containingClass().simpleTypeName() + "." + field.type().simpleTypeName();
          }
          return field.type().simpleTypeName();
       } else {
          return field.type().qualifiedTypeName();
       }
    }
 
    protected String fiddleIfCollection(FieldDoc var, String format, String def) {
       String fieldName = var.name();
 
       if (collectionTypes.containsKey(var.type().simpleTypeName())) {
          String colTypeName = var.type().simpleTypeName();
          return String.format(format, colTypeName, fieldName);
       }
 
       return def;
    }
 
    protected List<String> getFieldNames(ClassDoc classDoc) {
       return Lists.transform(ImmutableList.copyOf(instanceFields(classDoc)),
             new Function<FieldDoc, String>() {
                public String apply(FieldDoc input) {
                   return input.name();
                }
             });
    }
 }
