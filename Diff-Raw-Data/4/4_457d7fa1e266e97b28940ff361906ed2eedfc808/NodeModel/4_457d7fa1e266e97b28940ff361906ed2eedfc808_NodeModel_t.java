 /* -*- mode: Java; c-basic-offset: 2; -*- */
 /* ***************************************************************************
  * NodeModel.java
  * ***************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 
 import java.io.*;
 import java.text.ChoiceFormat;
 import java.util.*;
 import java.util.regex.*;
 
 
 import org.openlaszlo.compiler.ViewSchema.ColorFormatException;
 import org.openlaszlo.css.CSSParser;
 import org.openlaszlo.sc.Function;
 import org.openlaszlo.sc.Method;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.sc.CompilerException;
 import org.openlaszlo.sc.CompilerImplementationError;
 import org.openlaszlo.server.*;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.utils.ListFormat;
 import org.openlaszlo.xml.internal.MissingAttributeException;
 import org.openlaszlo.xml.internal.Schema;
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.apache.commons.collections.CollectionUtils;
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.jdom.Text;
 import org.jdom.Content;
 import org.jdom.Namespace;
 
 /** Models a runtime LzNode. */
 public class NodeModel implements Cloneable {
 
     public static final String FONTSTYLE_ATTRIBUTE = "fontstyle";
     public static final String WHEN_IMMEDIATELY = "immediately";
     public static final String WHEN_ONCE = "once";
     public static final String WHEN_ALWAYS = "always";
     public static final String WHEN_PATH = "path";
     public static final String WHEN_STYLE = "style";
     public static final String ALLOCATION_INSTANCE = "instance";
     public static final String ALLOCATION_CLASS = "class";
 
     private static final String SOURCE_LOCATION_ATTRIBUTE_NAME = "__LZsourceLocation";
 
     final ViewSchema schema;
     final Element element;
     String tagName;
     String id = null;
     String localName = null;
     String globalName = null;
     private String nodePath = null;
     LinkedHashMap attrs = new LinkedHashMap();
     List children = new Vector();
     /** A set {eventName: String -> True) of names of event handlers
      * declared with <handler name="xxx"/>. */
     LinkedHashMap delegates = new LinkedHashMap();
     LinkedHashMap classAttrs = new LinkedHashMap();
     LinkedHashMap setters = new LinkedHashMap();
 
     NodeModel     datapath = null;
 
     String passthroughBlock = null;
 
 
     /** [eventName: String, methodName: String, Function] */
     List delegateList = new Vector();
     ClassModel parentClassModel;
     String initstage = null;
     int totalSubnodes = 1;
     final CompilationEnvironment env;
     // Used to freeze the definition for generation
     protected boolean frozen = false;
     final boolean debug;
     // Datapaths and States don't have methods because they "donate"
     // their methods to other instances.  Where we would normally make
     // a method, we make a closure instead
     protected boolean canHaveMethods = true;
 
     public Object clone() {
         NodeModel copy;
         try {
             copy = (NodeModel) super.clone();
         } catch (CloneNotSupportedException e) {
             throw new RuntimeException(e);
         }
         copy.attrs = new LinkedHashMap(copy.attrs);
         copy.delegates = new LinkedHashMap(copy.delegates);
         copy.classAttrs = new LinkedHashMap(copy.classAttrs);
         copy.setters = new LinkedHashMap(copy.setters);
         copy.delegateList = new Vector(copy.delegateList);
         copy.children = new Vector();
         for (Iterator iter = children.iterator(); iter.hasNext(); ) {
             copy.children.add(((NodeModel) iter.next()).clone());
         }
         return copy;
     }
 
     NodeModel(Element element, ViewSchema schema, CompilationEnvironment env) {
         this.element = element;
         this.schema = schema;
         this.env = env;
         this.debug = env.getBooleanProperty(env.DEBUG_PROPERTY);
 
         this.tagName = element.getName();
         // Cache ClassModel for parent
         this.parentClassModel = this.getParentClassModel();
        if (this.parentClassModel == null) {
          throw new CompilationError("invalid superclass value in 'extends' attribute, '" +
                                     element.getAttributeValue("extends") + "'", element);
        }
         if (parentClassModel.isSubclassOf(schema.getClassModel("state")) ||
             parentClassModel.isSubclassOf(schema.getClassModel("datapath"))) {
             this.canHaveMethods = false;
         }
         this.initstage = this.element.getAttributeValue("initstage");
         if (this.initstage != null) {
             this.initstage = this.initstage.intern();
         }
 
         // Get initial node count from superclass
         // TODO: [2003-05-04] Extend this mechanism to cache/model
         // all relevant superclass info
         // TODO: [2003-05-04 ptw] How can we get this info for
         // instances of built-in classes?
         if (this.parentClassModel != null) {
             ElementWithLocationInfo parentElement =
                 (ElementWithLocationInfo) this.parentClassModel.definition;
             // TODO: [2003-05-04 ptw] As above, but a class
             // that extends a built-in class
             if (parentElement != null) {
                 // TODO: [2003-05-04 ptw] An instantiation of
                 // a class that has not been modelled yet --
                 // do enough modelling to get what is needed.
                 if (parentElement.model != null) {
                     this.totalSubnodes =
                         parentElement.model.classSubnodes();
                     if (this.initstage == null) {
                         this.initstage =
                             parentElement.model.initstage;
                     }
                 }
             }
         }
         // Get ID
         this.id = element.getAttributeValue("id");
         this.localName = element.getAttributeValue("name");
         // Get global name, if applicable
         if (CompilerUtils.topLevelDeclaration(element) &&
             (! ("class".equals(tagName) || "interface".equals(tagName) || "mixin".equals(tagName)))) {
           this.globalName = localName;
         }
     }
 
     // Path of node from root.  This follows the same psuedo-xpath
     // system used in LzNode._dbg_name.  It provides a basis for
     // giving anonymous classes unique names.
     private static String computeNodePath(NodeModel node, ViewSchema schema, CompilationEnvironment env) {
         if (node.id != null) {
             return "#" + node.id;
         }
         if (node.globalName != null) {
             return "#" + node.globalName;
         }
         String path = "";
         org.jdom.Parent parentDOMNode = node.element.getParent();
         Element parentElement;
         if (parentDOMNode instanceof org.jdom.Document) {
           parentElement = ((org.jdom.Document)parentDOMNode).getRootElement();
         } else {
           parentElement = (Element)parentDOMNode;
         }
         // Ensure parent modelled
         NodeModel parent = elementAsModel((ElementWithLocationInfo)parentElement, schema, env);
         String pn;
         if ((parentElement == node.element) ||
             // <library> is only permitted at the root and is elided
             // by the compiler
             "library".equals(parentElement.getName())) {
           // Must be at the root, when not linking, create a UID
           // placeholder for root
           if (! "false".equals(env.getProperty(CompilationEnvironment.LINK_PROPERTY))) {
             // linking, there is only one root
             pn = "";
           } else {
             // not linking, use a unique root
             pn = env.getUUID();
           }
         } else {
           pn = computeNodePath(parent, schema, env);
         }
         String nn = node.localName;
         if (nn != null) {
           path = "@" + nn;
         } else {
           String tn = path = node.tagName;
           int count = 0, index = -1;
           for (Iterator iter = parentElement.getChildren(tn, parentElement.getNamespace()).iterator();
                iter.hasNext(); ) {
             Element sibling = (Element) iter.next();
             count++;
             if (index != -1) break;
             if (node.element == sibling) { index = count; }
           }
           if (count > 1) {
             path += "[" + index + "]";
           }
         }
         return pn + "/" + path;
     };
 
     String getNodePath () {
         if (nodePath != null) { return nodePath; }
         return nodePath = computeNodePath(this, schema, env);
     }
 
     private static final String DEPRECATED_METHODS_PROPERTY_FILE = (
         LPS.getMiscDirectory() + File.separator + "lzx-deprecated-methods.properties"
         );
     private static final Properties sDeprecatedMethods = new Properties();
 
     static {
         try {
             InputStream is = new FileInputStream(DEPRECATED_METHODS_PROPERTY_FILE);
             try {
                 sDeprecatedMethods.load(is);
             } finally {
                 is.close();
             }
         } catch (java.io.IOException e) {
             throw new ChainedException(e);
         }
     }
 
     private static final String FLASH7_BUILTINS_PROPERTY_FILE = (
         LPS.getMiscDirectory() + File.separator + "flash7-builtins.properties"
         );
 
     public static final Properties sFlash7Builtins = new Properties();
 
     static {
         try {
 
             InputStream is7 = new FileInputStream(FLASH7_BUILTINS_PROPERTY_FILE);
             try {
                 sFlash7Builtins.load(is7);
             } finally {
                 is7.close();
             }
         } catch (java.io.IOException e) {
             throw new ChainedException(e);
         }
     }
 
   static class BindingExpr {
     String expr;
     BindingExpr(String expr) { this.expr = expr; }
     String getExpr() { return this.expr; }
   }
 
   static class CompiledAttribute {
     String name;
     Schema.Type type;
     private String value;
     boolean constantValue = false;
     String when;
     Element source;
     CompilationEnvironment env;
     String srcloc;
     String bindername;
     String dependenciesname;
     String fallbackexpression;
 
     static org.openlaszlo.sc.Compiler compiler;
 
     org.openlaszlo.sc.Compiler getCompiler() {
       if (compiler != null) { return compiler; }
       return compiler = new org.openlaszlo.sc.Compiler();
     }
 
     public CompiledAttribute (String name, Schema.Type type, String value, String when, Element source, CompilationEnvironment env) {
       this.name = name;
       this.type = type;
       this.value = value;
       // Only approximate.  Current only used for WHEN_STYLE
       this.constantValue = (value != null && value.matches("\\s*['\"]\\S*['\"]\\s*"));
       this.when = when;
       this.source = source;
       this.env = env;
       this.srcloc = CompilerUtils.sourceLocationDirective(source, true);
       if (when.equals(WHEN_PATH) || (when.equals(WHEN_STYLE) && (!constantValue)) || when.equals(WHEN_ONCE) || when.equals(WHEN_ALWAYS)) {
         this.bindername =  env.methodNameGenerator.next();
         if (when.equals(WHEN_ALWAYS)) {
           this.dependenciesname =  env.methodNameGenerator.next();
         }
       }
     }
 
     public void setFallbackExpression(String fallback) {
       this.fallbackexpression = fallback;
     }
 
     public Function getBinderMethod(boolean canHaveMethods) {
       if (! (when.equals(WHEN_PATH) || (when.equals(WHEN_STYLE)) || when.equals(WHEN_ONCE) || when.equals(WHEN_ALWAYS))) {
         return null;
       }
       String installer = "setAttribute";
       String prefix ="";
       String body = "\n#beginAttribute\n" + srcloc + value + CompilerUtils.endSourceLocationDirective + "\n#endAttribute\n";
       String suffix = "";
       String prettyBinderName = name + "='$";
 
       // All constraint methods need ignore args for swf9
       String args="$lzc$ignore";
       if (when.equals(WHEN_ONCE)) {
         // default
         prettyBinderName += "once";
       } else if (when.equals(WHEN_ALWAYS)) {
         // NOTE: [2009-05-18 ptw] Only call the installer if the value
         // will change, to minimize event cascades (data and style
         // binding have to handle this in their installers).  We
         // always call the installer if the target is not inited.
         // This ensures that the value is set correctly (and events
         // propagated) if the constraint is being called to initialize
         // the target
         prefix = "var $lzc$newvalue = " + body + ";\n" +
           "if ($lzc$newvalue !== this[" + ScriptCompiler.quote(name) + "] || (! this.inited)) {\n";
         body = "$lzc$newvalue";
         suffix = "\n}";
       } else if (when.equals(WHEN_PATH)) {
         installer = "dataBindAttribute";
         body = body + ",'" + type + "'";
         prettyBinderName += "path";
       } else if (when.equals(WHEN_STYLE)) {
         // Styles are processed at the same time as constraints.
         // Whether a style actually results in a constraint or not
         // cannot be determined until the style property value is
         // derived (at run time)
         installer = "__LZstyleBindAttribute";
         body = body + ",'" + type + "'";
         if (this.fallbackexpression != null) {
           body += "," + this.fallbackexpression;
         }
         prettyBinderName += "style";
       }
       body = prefix + "this." + installer + "(" +
           ScriptCompiler.quote(name) + "," +
           body + ")" + suffix;
       Function binder;
       prettyBinderName += "{...}'";
       String pragmas = "#pragma " + ScriptCompiler.quote("userFunctionName=" + prettyBinderName);
       // Binders are called by LzDelegate.execute, which passes the
       // value sent by sendEvent, so we have to accept it, but we
       // ignore it
       if (canHaveMethods) {
           // TODO: [2008-07-21 ptw] (LPP-5813) This should really be
           // in the script-compiler back-end
           if (! (env.isAS3())) {
             pragmas += "\n#pragma 'withThis'\n";
           }
           binder = new Method(bindername, args, "", pragmas, body, srcloc, null);
       } else {
           pragmas += "\n#pragma 'withThis'\n";
           binder = new Function(bindername, args, "", pragmas, body, srcloc);
       }
       return binder;
     }
 
     public Function getDependenciesMethod(boolean canHaveMethods) {
       if (! when.equals(WHEN_ALWAYS)) {
         return null;
       }
       String pragmas = "#pragma " + ScriptCompiler.quote("userFunctionName=" + name + " dependencies");
       String body = "";
       try {
         body = "return (" + getCompiler().dependenciesForExpression(srcloc + value) + ")";
       } catch (CompilerException e) {
         env.warn(e, source);
       }
       Function dependencies;
       if (canHaveMethods) {
           // TODO: [2008-07-21 ptw] (LPP-5813) This should really be
           // in the script-compiler back-end
           if (! (env.isAS3())) {
             pragmas += "\n#pragma 'withThis'\n";
           }
           dependencies = new Method(dependenciesname, "", "", pragmas, body, srcloc, null);
       } else {
           pragmas += "\n#pragma 'withThis'\n";
           dependencies = new Function(dependenciesname, "", "", pragmas, body, srcloc);
       }
       return dependencies;
     }
 
     public Object getInitialValue () {
       // A null value indicates an attribute that was declared only
       if (value == null) { return null; }
       // Handle when cases
       // N.B., $path and $style are not really when values, but
       // there you go...
       if (when.equals(WHEN_PATH) || (when.equals(WHEN_STYLE)) || when.equals(WHEN_ONCE) || when.equals(WHEN_ALWAYS)) {
         String kind = "LzOnceExpr";
         String debugDescription = "";
         if (env.getBooleanProperty(env.DEBUG_PROPERTY)) {
           debugDescription = ", " + ScriptCompiler.quote("$" + when + "{" + value + "}");
         }
         if (when.equals(WHEN_ONCE) || when.equals(WHEN_PATH)) {
           // default
         } else if (when.equals(WHEN_STYLE)) {
           // Styles are processed at the same time as constraints.
           // Whether a style actually results in a constraint or not
           // cannot be determined until the style property value is
           // derived (at run time)
           kind = "LzConstraintExpr";
           // Style constraints on constant properties have a special
           // compact mechanism
           if (constantValue) {
             kind = "LzStyleConstraintExpr";
             return new BindingExpr("new " + kind + "(" +
                                    ScriptCompiler.quote(name) + ", " +
                                    value + ", " +
                                    ScriptCompiler.quote("" + type) +
                                    (fallbackexpression != null ? (", " + fallbackexpression) : "")  +
                                    ")");
           }
         } else if (when.equals(WHEN_ALWAYS)) {
           kind = "LzAlwaysExpr";
           // Always constraints have a second value, the dependencies method
           return new BindingExpr("new " + kind + "(" + 
                                  ScriptCompiler.quote(bindername) + ", " +
                                  ScriptCompiler.quote(dependenciesname) + 
                                  debugDescription + ")");
         }
         // Return an initExpr as the 'value' of the attribute
         return new BindingExpr("new " + kind + "(" + ScriptCompiler.quote(bindername) + debugDescription + ")");
       } else if (when.equals(WHEN_IMMEDIATELY)) {
         if (CanvasCompiler.isElement(source) &&
             ("width".equals(name) || "height".equals(name))) {
           // The Canvas compiler depends on seeing width/height
           // unadulterated <sigh />.
           // TODO: [2007-05-05 ptw] (LPP-3949) The
           // #beginAttribute directives for the parser should
           // be added when the attribute is written, not
           // here...
           return value;
         } else {
           return "\n#beginAttribute\n" + srcloc + value + CompilerUtils.endSourceLocationDirective +"\n#endAttribute\n";
         }
       } else {
         throw new CompilationError("invalid when value '" +
                                    when + "'", source);
       }
     }
   }
 
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("{NodeModel class=" + tagName);
         if (!attrs.isEmpty())
             buffer.append(" attrs=" + attrs.keySet());
         if (!delegates.isEmpty())
             buffer.append(" delegates=" + delegates.keySet());
         if (!classAttrs.isEmpty())
             buffer.append(" classAttrs=" + classAttrs.keySet());
         if (!setters.isEmpty())
             buffer.append(" setters=" + setters.keySet());
         if (!delegateList.isEmpty())
             buffer.append(" delegateList=" + delegateList);
         if (!children.isEmpty())
             buffer.append(" children=" + children);
         buffer.append("}");
         return buffer.toString();
     }
 
     List getChildren() {
         return children;
     }
 
     public static boolean isPropertyElement(Element elt) {
         String name = elt.getName();
         return "attribute".equals(name) || "method".equals(name)
           || "handler".equals(name) || "setter".equals(name)
           || "event".equals(name) || "passthrough".equals(name);
     }
 
     /** Returns a name that is used to report this element in warning
      * messages. */
     String getMessageName() {
         return "element " + element.getName();
     }
 
     /**
      * Returns a script that creates a runtime representation of a
      * model.  The format of this representation is specified <a
      * href="../../../../doc/compiler/views.html">here</a>.
      *
      */
     public String asJavascript() {
         try {
             java.io.Writer writer = new java.io.StringWriter();
             ScriptCompiler.writeObject(this.asMap(), writer);
             return writer.toString();
         } catch (java.io.IOException e) {
             throw new ChainedException(e);
         }
     }
 
     /** Returns true iff clickable should default to true. */
     private static boolean computeDefaultClickable(ViewSchema schema,
                                                    Map attrs,
                                                    Map delegates) {
         if ("true".equals(attrs.get("cursor"))) {
             return true;
         }
         for (Iterator iter = attrs.keySet().iterator(); iter.hasNext();) {
             String eventName = (String) iter.next();
             if (schema.isMouseEventAttribute(eventName)) {
               // TODO: [2008-05-05 ptw] See if it really is an event,
               // not just an attribute with a coincidental name?
               return true;
             }
         }
         for (Iterator iter = delegates.keySet().iterator(); iter.hasNext();) {
             String eventName = (String) iter.next();
             if (schema.isMouseEventAttribute(eventName)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns a NodeModel that represents an Element, including the
      * element's children
      *
      * @param elt an element
      * @param schema a schema, used to encode attribute values
      * @param env the CompilationEnvironment
      */
     public static NodeModel elementAsModel(Element elt, ViewSchema schema,
                                            CompilationEnvironment env) {
         return elementAsModelInternal(elt, schema, true, env);
     }
 
     /**
      * Returns a NodeModel that represents an Element, excluding the
      * element's children
      *
      * @param elt an element
      * @param schema a schema, used to encode attribute values
      * @param env the CompilationEnvironment
      */
     public static NodeModel elementOnlyAsModel(Element elt, ViewSchema schema,
                                                CompilationEnvironment env) {
         return elementAsModelInternal(elt, schema, false, env);
     }
 
     /** Returns a NodeModel that represents an Element
      *
      * @param elt an element
      * @param schema a schema, used to encode attribute values
      * @param includeChildren whether or not to include children
      * @param env the CompilationEnvironment
      */
     private static NodeModel elementAsModelInternal(
         Element elt, ViewSchema schema, 
         boolean includeChildren, CompilationEnvironment env)
     {
         NodeModel model = ((ElementWithLocationInfo) elt).model;
         if (model != null) { return model; }
 
         ElementCompiler compiler = Compiler.getElementCompiler(elt, env);
         compiler.preprocess(elt, env);
 
         checkTagDeclared(elt, schema);
         model = new NodeModel(elt, schema, env);
         LinkedHashMap attrs = model.attrs;
         Map delegates = model.delegates;
         model.addAttributes(env);
 
         // This emits a local dataset node, so only process
         // <dataset> tags that are not top level datasets.
         if (elt.getName().equals("dataset")) {
             boolean contentIsLiteralXMLData = true;
             String datafromchild = elt.getAttributeValue("datafromchild");
             String src = elt.getAttributeValue("src");
             String type = elt.getAttributeValue("type");
 
             if ((type != null && ("soap".equals(type) || "http".equals(type)))
                 || (src != null && XMLUtils.isURL(src))
                 || "true".equals(datafromchild)) {
                 contentIsLiteralXMLData = false;
             }
 
             if (contentIsLiteralXMLData) {
               // Default to legacy behavior, treat all children as XML literal data.
               model.addProperty("initialdata", getDatasetContent(elt, env), ALLOCATION_INSTANCE, elt);
               includeChildren = false;
             }
         }
 
         if (includeChildren) {
             model.addChildren(env);
             // If any children are subclasses of <state>, recursively
             // hoist named children up in order to declare them so
             // they can be referenced as vars without a 'this.---" prefix.
             if (!isState(model, schema)) {
               model.addStateChildren(env);
             }
             model.addText();
             if (!attrs.containsKey("clickable")
                 && computeDefaultClickable(schema, attrs, delegates)) {
               model.addProperty("clickable", "true", ALLOCATION_INSTANCE, elt);
             }
         }
 
         // Check that all attributes required by the class or it's superclasses are present
         checkRequiredAttributes(elt, model, schema);
 
         ((ElementWithLocationInfo) elt).model = model;
         return model;
     }
 
   private static void checkRequiredAttributes(Element element, NodeModel model, ViewSchema schema) {
     ClassModel classinfo =  schema.getClassModel(element.getName());
     Map attrs = model.attrs;
 
     CompilationEnvironment env = schema.getCompilationEnvironment();
     // Check that each required attribute has a value supplied by either this instance, class, or ancestor class
     for (Iterator iter = classinfo.requiredAttributes.iterator(); iter.hasNext(); ) {
       String reqAttrName = (String) iter.next();
       boolean supplied = false;
       // check if this node model declares a value
       if (attrs.containsKey(reqAttrName)) {
         supplied = true;
       } else {
         // check if the class or superclass models declares a value
         supplied = attrHasDefaultValue(classinfo, reqAttrName,  NodeModel.ALLOCATION_INSTANCE);
       }
       
       if (!supplied) {
         env.warn(
             new CompilationError("Missing required attribute "+reqAttrName+ " for tag "+element.getName() , element),
             element);
       }
     }
   }
 
   // Return true if a given attribute has a non-null value supplied by the class or ancestor class
   static boolean attrHasDefaultValue(ClassModel classmodel, String attrName, String allocation) {
     Map attrtable = allocation.equals(NodeModel.ALLOCATION_INSTANCE) ?
                          classmodel.attributeSpecs : classmodel.classAttributeSpecs;
     AttributeSpec attr = (AttributeSpec) attrtable.get(attrName);
     if ((attr != null) && attr.defaultValue != null && !attr.defaultValue.equals("null")) {
       return true;
     } else if (classmodel.superclass != null) {
       return(attrHasDefaultValue(classmodel.superclass, attrName, allocation));
     } else {
       return false;
     }
   }
 
 
   static void checkTagDeclared(Element element, ViewSchema schema) {
     ClassModel classinfo =  schema.getClassModel(element.getName());
     if (classinfo == null || classinfo.definition == null) {
       throw new CompilationError("Unknown tag: "+element.getName(), element);
     }
   }
 
     // Calculate how many nodes this object will put on the
     // instantiation queue.
     int totalSubnodes() {
         // A class does not instantiate its subnodes.
         // States override LzNode.thaw to delay subnodes.
         // FIXME [2004-06-3 ows]: This won't work for subclasses
         // of state.
         if (ClassCompiler.isElement(element) ||
             "state".equals(tagName)) {
             return 1;
         }
         // initstage late, defer delay subnodes
         if (this.initstage != null &&
             (this.initstage == "late" ||
              this.initstage == "defer")) {
             return 0;
         }
         return this.totalSubnodes;
     }
 
     // How many nodes will be inherited from this class
     int classSubnodes() {
         if (ClassCompiler.isElement(element)) {
             return this.totalSubnodes;
         }
         return 0;
     }
 
     ClassModel getClassModel() {
         return schema.getClassModel(this.tagName);
     }
 
     /** Gets the ClassModel for this element's parent class.  If this
      * element is a <class> definition, the superclass; otherwise the
      * class of the tag of this element. */
     ClassModel getParentClassModel() {
         String parentName = this.tagName;
         return
             ("class".equals(parentName) || "interface".equals(parentName) || "mixin".equals(parentName)) ?
             schema.getClassModel(element.getAttributeValue("extends", ClassCompiler.DEFAULT_SUPERCLASS_NAME)) :
             schema.getClassModel(parentName);
     }
 
     void setClassName(String name) {
         this.tagName = name;
         this.parentClassModel = getParentClassModel();
     }
 
     ViewSchema.Type getAttributeTypeInfoFromParent(
       Element elt, String attrname, String allocation)
         throws UnknownAttributeException
     {
         Element parent = elt.getParentElement();
         String parentName = parent.getName();
         // If the parent is a class definition, we want to get the
         // attributes of the class that it defines
         if ("class".equals(parentName) || "interface".equals(parentName) || "mixin".equals(parentName)) {
           parentName = parent.getAttributeValue("name");
         }
 
         // TODO: [2008-05-05 ptw] Schema needs to learn about
         // allocation
         assert ALLOCATION_INSTANCE.equals(allocation);
         return schema.getAttributeType(parentName, attrname, allocation);
     }
 
     ViewSchema.Type getAttributeTypeInfoFromParent(
       Element elt, String attrname)
         throws UnknownAttributeException
     {
       return getAttributeTypeInfoFromParent(elt, attrname, ALLOCATION_INSTANCE);
     }
 
     // Should only be called on a <class> or <interface> definition element.
     ViewSchema.Type getAttributeTypeInfoFromSuperclass(
       Element classDefElement, String attrname, String allocation)
         throws UnknownAttributeException
     {
         // TODO: [2008-05-05 ptw] Schema needs to learn about
         // allocation
         assert ALLOCATION_INSTANCE.equals(allocation);
         String superclassname = classDefElement.getAttributeValue("extends", ClassCompiler.DEFAULT_SUPERCLASS_NAME);
         ClassModel superclassModel = schema.getClassModel(superclassname);
 
         if (superclassModel == null) {
             throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Could not find superclass info for class " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-411", new Object[] {superclassname, classDefElement})
 );
         }
 
         // Check if this attribute is defined on the parent class, if
         // so, return that type
         AttributeSpec attr = superclassModel.getAttribute(attrname, allocation);
         if (attr != null) {
             return attr.type;
         }
         // Otherwise, check if it's defined on the "class" element
         // (e.g., 'name', 'extends', or 'with')
         superclassModel = schema.getClassModel("class");
         return superclassModel.getAttributeTypeOrException(attrname, allocation);
     }
 
     ViewSchema.Type getAttributeTypeInfoFromSuperclass(
       Element classDefElement, String attrname)
         throws UnknownAttributeException
     {
       return getAttributeTypeInfoFromSuperclass(classDefElement, attrname, ALLOCATION_INSTANCE);
     }
 
     // Get an attribute value, defaulting to the
     // inherited value, or ultimately the supplied default
     String getAttributeValueDefault(String attribute,
                                     String allocation,
                                     String name,
                                     String defaultValue) {
         // TODO: [2008-05-05 ptw] Schema needs to learn about
         // allocation
         assert ALLOCATION_INSTANCE.equals(allocation);
         // Look for an inherited value
         if (this.parentClassModel != null) {
             AttributeSpec attrSpec =
               this.parentClassModel.getAttribute(attribute, allocation);
             if (attrSpec != null) {
                 Element source = attrSpec.source;
                 if (source != null) {
                     return XMLUtils.getAttributeValue(source, name, defaultValue);
                 }
             }
         }
 
         return defaultValue;
     }
 
     String getAttributeValueDefault(String attribute,
                                     String name,
                                     String defaultValue) {
       return getAttributeValueDefault(attribute, ALLOCATION_INSTANCE, name, defaultValue);
     }
 
     // Not used at present
 //     private static String buildNameBinderBody (String symbol) {
 //         return
 //             "var $lzc$old = $lzc$parent." + symbol + ";\n" +
 //             "if ($lzc$bind) {\n" +
 //             "  if ($debug) {\n" +
 //             "    if ($lzc$old && ($lzc$old !== $lzc$node)) {\n" +
 //             "      Debug.warn('Redefining %w." + symbol + " from %w to %w', \n" +
 //             "        $lzc$parent, $lzc$old, $lzc$node);\n" +
 //             "    }\n" +
 //             "  }\n" +
 //             "  $lzc$parent." + symbol + " = $lzc$node;\n" +
 //             "} else if ($lzc$old === $lzc$node) {\n" +
 //             "  $lzc$parent." + symbol + " = null;\n" +
 //             "}\n";
 //     }
 
     /**
      * Builds the body of a binder method for binding a global value to
      * an instance.  NOTE: [2008-10-21 ptw] In swf9, we shadow these
      * bindings in a table `global` to support the `globalValue` API for
      * looking up global ID's at runtime.
      */
     private static String buildIdBinderBody (String symbol, boolean setId, boolean debug) {
         return
             "#pragma " + ScriptCompiler.quote("userFunctionName=bind #" + symbol) + "\n" +
             "if ($lzc$bind) {\n" +
             (debug ?
              "    if (" + symbol + " && (" + symbol + " !== $lzc$node)) {\n" +
              "      Debug.warn('Redefining #" + symbol + " from %w to %w', \n" +
              "        " + symbol + ", $lzc$node);\n" +
              "    }\n" :
              "") +
             (setId ? ("  $lzc$node.id = " + ScriptCompiler.quote(symbol) + ";\n") : "") +
             "  " + symbol + " = $lzc$node;\n" +
             "  if ($as3) { global[" + ScriptCompiler.quote(symbol) + "] = $lzc$node; }\n" +
             "} else if (" + symbol + " === $lzc$node) {\n" +
             "  " + symbol + " = null;\n" +
             "  if ($as3) { global[" + ScriptCompiler.quote(symbol) + "] = null; }\n" +
             (setId ? ("  $lzc$node.id = null;\n") : "") +
             "}\n";
     }
 
     /**
      * Add the attributes that are specified in the open tag.  These
      * attributes are by definition instance attributes.  If you want
      * class attributes, you have to use the longhand (attribute child
      * node) form.
      */
     void addAttributes(CompilationEnvironment env) {
         // Add source locators, if requested.  Added here because it
         // is not in the schema
         if (env.getBooleanProperty(env.SOURCELOCATOR_PROPERTY)) {
             String location = "document(" + 
                 ScriptCompiler.quote(Parser.getSourceMessagePathname(element)) + 
                 ")" +
                 XMLUtils.getXPathTo(element);
             CompiledAttribute cattr = compileAttribute(
                 element, SOURCE_LOCATION_ATTRIBUTE_NAME,
                 location, ViewSchema.STRING_TYPE,
                 WHEN_IMMEDIATELY);
             addProperty(SOURCE_LOCATION_ATTRIBUTE_NAME, cattr, ALLOCATION_INSTANCE);
         }
 
         // Add file/line information if debugging
         if (debug && !(env.isAS3())) {
           // File/line stored separately for string sharing
           String name = Function.FUNCTION_FILENAME;
           String filename = Parser.getSourceMessagePathname(element);
           CompiledAttribute cattr =
             compileAttribute(element, name, filename, ViewSchema.STRING_TYPE, WHEN_IMMEDIATELY);
           addProperty(name, cattr, ALLOCATION_INSTANCE);
           name = Function.FUNCTION_LINENO;
           Integer lineno = Parser.getSourceLocation(element, Parser.LINENO);
           cattr = compileAttribute(element, name, lineno.toString(), ViewSchema.NUMBER_TYPE, WHEN_IMMEDIATELY);
           addProperty(name, cattr, ALLOCATION_INSTANCE);
         }
 
         ClassModel classModel = getClassModel();
         if (classModel == null) {
           throw new CompilationError("Could not find class definition for tag `" + tagName + "`", element);
         }
         // Encode the attributes
         for (Iterator iter = element.getAttributes().iterator(); iter.hasNext(); ) {
             Attribute attr = (Attribute) iter.next();
             Namespace ns = attr.getNamespace();
             String name = attr.getName();
             String value = element.getAttributeValue(name, ns);
 
             if (name.equals(FONTSTYLE_ATTRIBUTE)) {
                 // "bold italic", "italic bold" -> "bolditalic"
                 value = FontInfo.normalizeStyleString(value, false);
             }
 
             if (name.toLowerCase().equals("defaultplacement")) {
                 if (value != null && value.matches("\\s*['\"]\\S*['\"]\\s*")) {
                     String oldValue = value;
                     // strip off start and ending quotes;
                     value = value.trim();
                     value = value.substring(1, value.length()-1);
                     env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Replacing defaultPlacement=\"" + p[0] + "\" by \"" + p[1] + "\".  For future compatibility" + ", you should make this change to your source code."
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-513", new Object[] {oldValue, value})
                         ,element);
                 }
             }
 
             // Special case for compiling a class, the class
             // attributes are really 'meta' attributes, not
             // attributes of the class -- they will be processed by
             // the ClassModel or ClassCompiler
             boolean isClass = "class".equals(tagName);
             if (isClass || "interface".equals(tagName) || "mixin".equals(tagName)) {
                 // TODO: [2008-03-22 ptw] This should somehow be
                 // derived from the schema, but this does not work, so
                 // we hard-code the meta-attributes here
 //                 if (superclassModel.getAttribute(name) != null) {
                 if ("name".equals(name) || (isClass && ("extends".equals(name) || "with".equals(name)))) {
                     continue;
                 }
             }
 
             // Warn for redefine of a flash builtin
             // TODO: [2006-01-23 ptw] What about colliding with DHTML globals?
             if (("id".equals(name) || "name".equals(name)) &&
                  (value != null &&
                   (env.getRuntime().indexOf("swf") == 0) &&
                   sFlash7Builtins.containsKey(value)))  {
                 env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="You have given the " + p[0] + " an attribute " + p[1] + "=\"" + p[2] + "\", " + "which may overwrite the Flash builtin class named \"" + p[3] + "\"."
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-532", new Object[] {getMessageName(), name, value, value})
                     ,element);
             }
 
             // Check that the view name is a valid javascript identifier
             if (("name".equals(name) || "id".equals(name)) &&
                 (value == null || !ScriptCompiler.isIdentifier(value))) {
                 CompilationError cerr = new CompilationError(
                     "The "+name+" attribute of this node,  "+ "\"" + value + "\", is not a valid javascript identifier " , element);
                 throw(cerr);
             }
 
             Schema.Type type;
             try {
                 if ("class".equals(tagName) || "interface".equals(tagName) || "mixin".equals(tagName)) {
                     // Special case, if we are compiling a "class"
                     // tag, then get the type of attributes from the
                     // superclass.
                     type = getAttributeTypeInfoFromSuperclass(element, name);
                 } else if ("state".equals(tagName)) {
                     // Special case for "state", it can have any attribute
                     // which belongs to the parent.
                     try {
                       type = schema.getAttributeType(element, name, ALLOCATION_INSTANCE);
                     } catch (UnknownAttributeException e) {
                       type = getAttributeTypeInfoFromParent(element, name);
                     }
                 } else {
                     // NOTE: [2007-06-14 ptw] Querying the classModel
                     // directly will NOT work, because the schema
                     // method has some special kludges in it for canvas
                     // width and height!
                     // NOTE: [2008-05-05 ptw] These are instance
                     // attributes by definition
                   type = schema.getAttributeType(element, name, ALLOCATION_INSTANCE);
                 }
 
             } catch (UnknownAttributeException e) {
                 String solution;
                 AttributeSpec alt = schema.findSimilarAttribute(tagName, name);
                 if (alt != null) {
                     String classmessage = "";
                     if (alt.source != null) {
                         classmessage = " on class "+alt.source.getName()+"\"";
                     } else {
                         classmessage = " on class "+getMessageName();
                     }
                     solution = 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="found an unknown attribute named \"" + p[0] + "\" on " + p[1] + ", however there is an attribute named \"" + p[2] + "\"" + p[3] + ", did you mean to use that?"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-616", new Object[] {name, getMessageName(), alt.name, classmessage});
                 } else {
 solution =
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="found an unknown attribute named \"" + p[0] + "\" on " + p[1] + ", check the spelling of this attribute name"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-624", new Object[] {name, getMessageName()});
                 }
                 env.warn(solution, element);
                 type = ViewSchema.EXPRESSION_TYPE;
             }
 
             if (type == schema.EVENT_HANDLER_TYPE) {
                 addHandlerFromAttribute(element, name, value);
             } else {
                 if (type == schema.ID_TYPE) {
                     this.id = value;
                 }
                 String when = this.getAttributeValueDefault(
                     name, "when", WHEN_IMMEDIATELY);
                 try {
                     CompiledAttribute cattr = compileAttribute(
                         element, name, value, type, when);
                     addProperty(name, cattr, ALLOCATION_INSTANCE);
                     // If this attribute is "id", you need to bind the
                     // id
                     if ("id".equals(name)) {
                         String symbol = value;
                         Function idbinder = new Function(
                             "$lzc$node:LzNode, $lzc$bind:Boolean=true",
                             buildIdBinderBody(symbol, true, debug));
                         addProperty("$lzc$bind_id", idbinder, ALLOCATION_INSTANCE);
                     }
                     // Ditto for top-level name "name"
                     if (CompilerUtils.topLevelDeclaration(element) && "name".equals(name)) {
                         String symbol = value;
                         // A top-level name is also an ID, for
                         // hysterical reasons
                         // TODO: [2008-04-10 ptw] We should really
                         // just change the name element to an id
                         // element in any top-level node and be
                         // done with it.
                         Function namebinder = new Function (
                             "$lzc$node:LzNode, $lzc$bind:Boolean=true",
                             buildIdBinderBody(symbol, false, debug));
                         addProperty("$lzc$bind_name", namebinder, ALLOCATION_INSTANCE);
                     }
 
                     // Check if we are aliasing another 'name'
                     // attribute of a sibling
                     if ("name".equals(name)) {
                         Element parent = element.getParentElement();
                         if (parent != null) {
                             for (Iterator iter2 = parent.getChildren().iterator(); iter2.hasNext(); ) {
                                 Element e = (Element) iter2.next();
                                 if (!e.getName().equals("resource") && !e.getName().equals("font")
                                     && e != element && value.equals(e.getAttributeValue("name"))) {
                                     String dup_location =
                                         CompilerUtils.sourceLocationPrettyString(e);
                                     env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes=p[0] + " has the same name=\"" + p[1] + "\" attribute as a sibling element at " + p[2]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-658", new Object[] {getMessageName(), value, dup_location})
                                         ,element);
                                 }
                             }
                         }
                     }
                 } catch (CompilationError e) {
                     env.warn(e);
                 }
             }
         }
     }
 
   void addProperty(String name, Object value, String allocation, Element source) {
     if (frozen) {
       throw new CompilerImplementationError("Attempting to addProperty when NodeModel frozen");
     }
 
     LinkedHashMap attrs;
     if (ALLOCATION_INSTANCE.equals(allocation)) {
       attrs = this.attrs;
     } else if (ALLOCATION_CLASS.equals(allocation)) {
       attrs = this.classAttrs;
     } else {
       throw new CompilationError("Unknown allocation: " + allocation, source);
     }
     // TODO: [2008-05-05 ptw] Make warning say whether it is a
     // class or instance property that is conflicting
     if (attrs.containsKey(name)) {
       env.warn(
                 /* (non-Javadoc)
                  * @i18n.test
                  * @org-mes="an attribute or method named '" + p[0] + "' already is defined on " + p[1]
                  */
                 org.openlaszlo.i18n.LaszloMessages.getMessage(
                   NodeModel.class.getName(),"051018-682", new Object[] {name, getMessageName()})
                 ,source);
     }
     if (value instanceof CompiledAttribute) {
       // Special handling for attribute with binders
       CompiledAttribute cattr = (CompiledAttribute)value;
       // The methods of a datapath constraint are moved to the
       // replicator, so must be compiled as closures
       boolean chm = "datapath".equals(name) ? false : canHaveMethods;
       if (cattr.bindername != null) {
         attrs.put(cattr.bindername, cattr.getBinderMethod(chm));
       }
       if (cattr.dependenciesname != null) {
         attrs.put(cattr.dependenciesname, cattr.getDependenciesMethod(chm));
       }
       attrs.put(name, cattr.getInitialValue());
     } else {
       attrs.put(name, value);
     }
   }
 
   void addProperty(String name, Object value, String allocation) {
     addProperty(name, value, allocation, element);
   }
 
     static String getDatasetContent(Element element, CompilationEnvironment env) {
         return getDatasetContent(element, env, false);
     }
 
     // For a dataset (well really for any Element), writes out the
     // child literal content as an escaped string, which could be used
     // to initialize the dataset at runtime.
     static String getDatasetContent(Element element, CompilationEnvironment env, boolean trimwhitespace) {
         // If type='http' or the path starts with http: or https:,
         // then don't attempt to include the data at compile
         // time. The runtime will have to recognize it as runtime
         // loaded URL data.
             
         if ("http".equals(element.getAttributeValue("type"))) {
             return "null";
         }
 
         boolean nsprefix = false;
         if ("true".equals(element.getAttributeValue("nsprefix"))) {
             nsprefix = true;
         }
 
         Element content = new Element("data");
 
         String src = element.getAttributeValue("src");
         // If 'src' attribute is a URL or null, don't try to expand it now,
         // just return. The LFC will have to interpret it as a runtime
         // loadable data URL.
         if ( (src != null) &&
              (src.startsWith("http:") ||
                src.startsWith("https:")) ) {
             return "null";
         } else if (src != null) {
             // Expands the src file content inline
             File file = env.resolveReference(element);
             try {
                 Element literaldata = new org.jdom.input.SAXBuilder(false)
                     .build(file)
                     .getRootElement();
                 if (literaldata == null) {
                     return "null";
                 }
                 literaldata.detach();
                 // add the expanded file contents as child node
                 content.addContent(literaldata);
             } catch (org.jdom.JDOMException e) {
                 throw new CompilationError(e);
             } catch (IOException e) {
                 throw new CompilationError(e);
             } catch (java.lang.OutOfMemoryError e) {
                 // The runtime gc is necessary in order for subsequent
                 // compiles to succeed.  The System gc is for good
                 // luck.
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="out of memory"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-761")
                         , element);
             }
         } else {
             // no 'src' attribute, use element inline content
             content.addContent(element.cloneContent());
         }
 
         // Serialize the child elements, as the local data
         org.jdom.output.XMLOutputter xmloutputter =
             new org.jdom.output.XMLOutputter();
         // strip the <dataset> wrapper
 
         // If nsprefix is false, remove namespace from elements before
         // serializing, or XMLOutputter puts a "xmlns" attribute on
         // the top element in the serialized string.
 
         if (!nsprefix) {
             removeNamespaces(content);
         }
 
         if (trimwhitespace) {
             trimWhitespace(content);
         }
 
         String body = null;
         if (content != null) {
             body = xmloutputter.outputString(content);
             return ScriptCompiler.quote(body);
         } else {
             return "null";
         }
 
     }
 
     // Recursively null out the namespace on elt and children
     static void removeNamespaces(Element elt) {
         elt.setNamespace(null);
         for (Iterator iter = elt.getChildren().iterator(); iter.hasNext(); ) {
             Element child = (Element) iter.next();
             removeNamespaces(child);
         }
     }
 
     // Recursively trim out the whitespace on text nodes
     static void trimWhitespace(Content elt) {
         if (elt instanceof Text) {
             ((Text) elt).setText(((Text)elt).getTextTrim());
         } else if (elt instanceof Element) {
             for (Iterator iter = ((Element) elt).getContent().iterator(); iter.hasNext(); ) {
                 Content child = (Content) iter.next();
                 trimWhitespace(child);
             }
         }
     }
 
     boolean isDatapathElement(Element child) {
         return (child.getName().equals("datapath"));
     }
 
     /** Warn if named child tag conflicts with a declared attribute in the parent class.
      */
     void checkChildNameConflict(String parentName, Element child, CompilationEnvironment env) {
         String attrName = child.getAttributeValue("name");
         String allocation = child.getAttributeValue("allocation", ALLOCATION_INSTANCE);
 
         if (attrName != null) {
           AttributeSpec attrSpec = schema.getClassAttribute ( parentName, attrName, allocation );
             if (attrSpec != null && attrSpec.type != ViewSchema.NODE_TYPE) {
                 // TODO [2007-09-26 hqm] i18n this
                 env.warn(
                     "Child tag '" + child.getName() +
                     "' with attribute '"+attrName +
                     "' conflicts with attribute named '"+attrName+"' of type '" + attrSpec.type +
                     "' on parent tag '"+element.getName()+"'.",
                     element);
             }
         }
     }
 
 
 
     void addChildren(CompilationEnvironment env) {
         // Encode the children
         for (Iterator iter = element.getChildren().iterator(); iter.hasNext(); ) {
             ElementWithLocationInfo child = (ElementWithLocationInfo) iter.next();
             if (!schema.canContainElement(element.getName(), child.getName())) {
                 // If this element is allowed to contain  HTML content, then
                 // we don't want to warn about encountering an HTML child element.
                 if (!( schema.hasTextContent(element) && schema.isHTMLElement(child))) {
                     env.warn(
                         // TODO [2007-09-26 hqm] i18n this
                         "The tag '" + child.getName() +
                         "' cannot be used as a child of " + element.getName(),
                         element);
                 }
             }
             try {
                 if (child.getName().equals("data")) {
                     checkChildNameConflict(element.getName(), child, env);
                     // literal data
                     addLiteralDataElement(child);
                 } else if (isPropertyElement(child)) {
                     addPropertyElement(child);
                 } else if (schema.isHTMLElement(child)) {
                     ; // ignore; the text compiler wiil handle this
                 } else if (schema.isDocElement(child)) {
                     ; // ignore doc nodes.
                 } else if (isDatapathElement(child)) {
                     checkChildNameConflict(element.getName(), child, env);
                     NodeModel dpnode = elementAsModel(child, schema, env);
                     this.datapath = dpnode;
                 } else {
                     checkChildNameConflict(element.getName(), child, env);
                     NodeModel childModel = elementAsModel(child, schema, env);
                     children.add(childModel);
                     // Declare the child name (if any) as a property
                     // of the node so that references to it from
                     // methods can be resolved at compile-time
                     String childName = child.getAttributeValue("name");
                     if (childName != null) {
                       addProperty(childName, null, ALLOCATION_INSTANCE, child);
                     }
                     totalSubnodes += childModel.totalSubnodes();
                 }
             } catch (CompilationError e) {
                 env.warn(e);
             }
         }
     }
 
   void addStateChildren(CompilationEnvironment env ) {
     // Check for each child, if it is a subclass of <state>.
     // If so, we need to declare any named children as attributes,
     // so the swf9 compiler won't complain about references to them.
     for (Iterator iter = children.iterator(); iter.hasNext(); ) {
       NodeModel childModel = (NodeModel)iter.next();
       if (isState(childModel, schema)) {
         declareNamedChildren(childModel);
       }
     }
   }
 
   /** Is this NodeModel a <state> or subclass of <state> ? */
   static boolean isState(NodeModel model, ViewSchema schema) {
     ClassModel classModel = model.getClassModel();
     boolean isstate = classModel.isSubclassOf(schema.getClassModel("state"));
     return isstate;
   }
 
   /** Hoist named children declarations from a state into the parent.
      
    Take all named children of this NodeModel (including named children
    in the classmodel) and declare them as attributes, so the swf9
    compiler will permit references to them at compile time.
   */
   void declareNamedChildren(NodeModel model) {
     List childnames = collectNamedChildren(model);
     for (Iterator i = childnames.iterator(); i.hasNext(); ) {
       String childname = (String)i.next();
       if (!attrs.containsKey(childname)) {
         addProperty(childname, null, ALLOCATION_INSTANCE);
       }
     }
   }
 
   // Returns a list of names of all named children of this instance and
   // those of its superclasses
   List collectNamedChildren(NodeModel model) {
     List names = new ArrayList();
     // iterate over children, getting named ones. If a child is a <state>, recurse into it to
     // hoist any named children up.
     for (Iterator iter = model.children.iterator(); iter.hasNext(); ) {
       NodeModel child = (NodeModel) iter.next();
       if (isState(child,schema)) {
          List childnames = collectNamedChildren(child);
          // merge in returned list with names
          names.addAll(childnames);
       } else {
         String childNameAttr = child.element.getAttributeValue("name");
         if (childNameAttr != null) {
           names.add(childNameAttr);
         }
       }
     }
     // Then recurse over superclasses, up to <state>
     ClassModel classModel = model.getClassModel();
      if (classModel.hasNodeModel()) {
          List supernames = collectNamedChildren(classModel.nodeModel);
          // merge in returned list with names
          names.addAll(supernames);
        }
      return names;
   }
 
 
 
    void warnIfHasChildren(Element element) {
      if (element.getChildren().size() > 0) {
        CompilationError cerr = new CompilationError(
            "The "+element.getName()+" tag cannot have child tags in this context", element);
        throw(cerr);        
      }
    }
 
     void addPropertyElement(Element element) {
       warnIfHasChildren(element);
       String tagName = element.getName();
       if ("method".equals(tagName)) {
         addMethodElement(element);
       } else if ("handler".equals(tagName)) {
         addHandlerElement(element);
       } else if ("event".equals(tagName)) {
         addEventElement(element);
       } else if ("attribute".equals(tagName)) {
         addAttributeElement(element);
       } else if ("setter".equals(tagName)) {
         addSetterElement(element);
       } else if ("passthrough".equals(tagName)) {
         addPassthroughElement(element);
       }
     }
 
     /** Compile a <passthrough> block for a node.
      *
      * Transforms to a #passthrough compiler pragma in the script output
      */
     void addPassthroughElement(Element element) {
       if (env.isAS3()) {
         passthroughBlock = element.getText();
       } else {
         env.warn("The passthrough tag can only be used in an as3 runtime, "
                  + "perhaps you could put this in a switch tag?",element);
       }
     }
 
     /** Defines an event handler.
      *
      * <handler name="eventname" [method="methodname"]>
      *   [function body]
      * </handler>
      *
      * This can do a compile time check to see if eventname is declared or
      * if there is an attribute FOO such that name="onFOO".
      */
     void addHandlerElement(Element element) {
         schema.checkHandlerAttributes(element, env);
         String method = element.getAttributeValue("method");
         // event name
         String event = element.getAttributeValue("name");
         String args = CompilerUtils.attributeLocationDirective(element, "args") +
             // Handlers get called with one argument, default to
             // ignoring that
             XMLUtils.getAttributeValue(element, "args", "$lzc$ignore");
         if ((event == null || !ScriptCompiler.isIdentifier(event))) {
             env.warn("handler needs a non-null name attribute");
             return;
         }
         String parent_name =
             element.getParentElement().getAttributeValue("id");
         String reference = element.getAttributeValue("reference");
         String body = element.getText();
         if (body.trim().length() == 0) { body = null; }
         // If non-empty body AND method name are specified, flag an error
         // If non-empty body, pack it up as a function definition
         if (body != null) {
             if (method != null) {
                 env.warn("you cannot declare both a 'method' attribute " +
                          "*and* a function body on a handler element",
                          element);
             }
             body = CompilerUtils.attributeLocationDirective(element, "text") + body;
         }
         addHandlerInternal(element, parent_name, event, method, args, body, reference);
     }
 
     /**
      * An event handler defined in the open tag
      */
     void addHandlerFromAttribute(Element element, String event, String body) {
         String parent_name = element.getAttributeValue("id");
         // Handlers get called with one argument, default to ignoring
         // that
         addHandlerInternal(element, parent_name, event, null, "$lzc$ignore", body, null);
     }
 
     /**
      * Adds a handler for `event` to be handled by `method`.  If
      * `body` is non-null, adds the method too.  If `reference` is
      * non-null, adds a method to compute the sender.
      *
      * @devnote: For backwards-compatibility, you are allowed to pass
      * in both a `method` (name) and a `body`.  This supports the old
      * method syntax where you were allowed to specify the event to be
      * handled and the name of the method for the body of the handler.
      */
     void addHandlerInternal(Element element, String parent_name, String event,  String method, String args, String body, String reference) {
         if (body == null && method == null) {
             env.warn("Refusing to compile an empty handler, you should declare the event instead", element);
             return;
         }
 
         if (parent_name == null) {
             parent_name = CompilerUtils.attributeUniqueName(element, "handler");
         }
         // Anonymous handler names have to be unique, so append a
         // gensym; but they also have to be unique across binary
         // libraries, so append parent (class) name
         String unique = "$" + parent_name + "_"  + env.methodNameGenerator.next();
         String referencename = null;
         String srcloc =  CompilerUtils.sourceLocationDirective(element, true);
         // delegates is only used to determine whether to
         // default clickable to true.  Clickable should only
         // default to true if the event handler is attached to
         // this view.
         if (reference == null) {
             delegates.put(event, Boolean.TRUE);
         } else {
             // TODO [2008-05-20 ptw] Replace the $debug code with actual
             // type declarations if/when they are enforced in all run
             // times
             referencename = debug ?
               ("$lzc$" + "handle_" + event + "_reference" + unique) :
               env.methodNameGenerator.next();
             String pragmas = "#pragma " + ScriptCompiler.quote("userFunctionName=get " + reference);
             String refbody = "var $lzc$reference = (" +
                 "#beginAttribute\n" +
                 reference + "\n#endAttribute\n);\n" +
                 (debug ?
                  "if ($lzc$reference is LzEventable) {\n" :
                  "") +
                 "  return $lzc$reference;\n" +
                 (debug ?
                  "} else {\n" +
                  "  Debug.error('Invalid event sender: " + reference + " => %w (for event " + event + ")', $lzc$reference);\n" +
                  "}" :
                  "");
             Function referencefn;
             if (canHaveMethods) {
                 // TODO: [2008-07-21 ptw] (LPP-5813) This should really
                 // be in the script-compiler back-end
                 if (! (env.isAS3())) {
                   pragmas += "\n#pragma 'withThis'\n";
                 }
                 referencefn = new Method(referencename, "", "", pragmas, refbody, srcloc, null);
             } else {
                 pragmas += "\n#pragma 'withThis'\n";
                 referencefn = new Function(referencename, "", "", pragmas, refbody, srcloc);
             }
             // Add reference computation as a method (so it can have
             // 'this' references work)
             addProperty(referencename, referencefn, ALLOCATION_INSTANCE, element);
         }
 
         if (body != null) {
             String pragmas = "#beginContent\n";
             if (method == null) {
                 method = debug ?
                   ("$lzc$" + "handle_" + event + unique) :
                   env.methodNameGenerator.next();
                 pragmas += "#pragma " + ScriptCompiler.quote("userFunctionName=handle " +
                                                              ((reference != null) ? (reference + ".") : "") +
                                                              event) +"\n";
             }
             pragmas += "#pragma 'methodName=" + method + "'\n";
             body = body + "\n#endContent\n";
             Function fndef;
             if (canHaveMethods) {
                 // TODO: [2008-07-21 ptw] (LPP-5813) This should really
                 // be in the script-compiler back-end
                 if (! (env.isAS3())) {
                   pragmas += "#pragma 'withThis'\n";
                 }
                 fndef = new Method(method, args, "", pragmas, body, srcloc, null);
             } else {
                 pragmas += "#pragma 'withThis'\n";
                 fndef = new Function(method, args, "", pragmas, body, srcloc);
             }
             // Add handler as a method
             addProperty(method, fndef, ALLOCATION_INSTANCE, element);
         }
 
         // Put everything into the delegate list
         delegateList.add(ScriptCompiler.quote(event));
         delegateList.add(ScriptCompiler.quote(method));
         if (reference != null) {
             delegateList.add(ScriptCompiler.quote(referencename));
         } else {
             delegateList.add("null");
         }
     }
 
     void addMethodElement(Element element) {
         schema.checkMethodAttributes(element, env);
         String name = element.getAttributeValue("name");
         String event = element.getAttributeValue("event");
         String allocation = XMLUtils.getAttributeValue(element, "allocation", ALLOCATION_INSTANCE);
         String args = CompilerUtils.attributeLocationDirective(element, "args") +
             XMLUtils.getAttributeValue(element, "args", "");
         String body = element.getText();
         String returnType = element.getAttributeValue("returns");
 
         if ((name == null || !ScriptCompiler.isIdentifier(name)) &&
             (event == null || !ScriptCompiler.isIdentifier(event))) {
             env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="method needs a non-null name or event attribute"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-832")
 );
             return;
         }
         if (name != null && sDeprecatedMethods.containsKey(name)) {
             String oldName = name;
             String newName = (String) sDeprecatedMethods.get(name);
             name = newName;
             env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes=p[0] + " is deprecated.  " + "This method will be compiled as <method name='" + p[1] + "' instead.  " + "Please update your sources."
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-846", new Object[] {oldName, newName})
                 ,element);
         }
 
         // TODO: Remove after 4.2
         if (event != null) {
             env.warn("The `event` property of methods is deprecated.  Please update your source to use the `<handler>` tag.",
                      element);
             String parent_name =
                 element.getParentElement().getAttributeValue("id");
             if (parent_name == null) {
                 parent_name =
                     (name == null ?
                      CompilerUtils.attributeUniqueName(element, "handler") :
                      CompilerUtils.attributeUniqueName(element, "name"));
             }
 
             String reference = element.getAttributeValue("reference");
             if (reference != null) {
                 reference = CompilerUtils.attributeLocationDirective(element, "reference") + reference;
             }
             addHandlerInternal(element, parent_name, event, name, args, body, reference);
             return;
         }
 
         addMethodInternal(name, args, returnType, body, element, allocation);
     }
 
   void addMethodInternal(String name, String args, String returnType, String body, Element element, String allocation) {
         ClassModel superclassModel = getParentClassModel();
         // Override will be required if there is an inherited method
         // of the same name
         boolean override =
           // This gets methods from the schema, in particular, the
           // LFC interface
           superclassModel.getAttribute(name, allocation) != null ||
           // This gets methods the compiler has added, in
           // particular, setter methods
           superclassModel.getMergedMethods().containsKey(name) ||
           // And the user may know better than any of us
           "true".equals(element.getAttributeValue("override"));
         boolean isfinal = "true".equals(element.getAttributeValue("final"));
 
         if (!override) {
             // Just check method declarations on regular node.
             // Method declarations inside of class definitions will be already checked elsewhere,
             // in the call from ClassCompiler.updateSchema to schema.addElement
             if ("class".equals(tagName) || "interface".equals(tagName) || "mixin".equals(tagName)) {
                 schema.checkInstanceMethodDeclaration(element, tagName, name, env);
             }
         }
 
         String name_loc =
             (name == null ?
              CompilerUtils.attributeLocationDirective(element, "handler") :
              CompilerUtils.attributeLocationDirective(element, "name"));
         Function fndef;
         String pragmas = "\n#beginContent\n" +
                 "\n#pragma 'methodName=" + name + "'\n";
         body = body + "\n#endContent";
         if (canHaveMethods) {
             String adjectives = "";
             // LPP-8062 script compiler will give an error if you declare 'override' on a static method
             if (override && ALLOCATION_INSTANCE.equals(allocation)) { adjectives += " override"; }
             if (isfinal) { adjectives += " final"; }
             if (ALLOCATION_INSTANCE.equals(allocation) &&
                 // TODO: [2008-07-21 ptw] (LPP-5813) This should really be
                 // in the script-compiler back-end
                 (! (env.isAS3()))) {
               pragmas += "\n#pragma 'withThis'\n";
             }
             fndef = new Method(name, args, returnType, pragmas, body, name_loc, adjectives);
         } else {
             if (ALLOCATION_INSTANCE.equals(allocation)) {
               pragmas += "\n#pragma 'withThis'\n";
             }
             fndef = new Function(name, args, returnType, pragmas, body, name_loc);
         }
         addProperty(name, fndef, allocation, element);
     }
 
 
     // Pattern matcher for '$once{...}' style constraints
     Pattern constraintPat = Pattern.compile("^\\s*\\$(\\w*)\\s*\\{(.*)\\}\\s*");
 
 
     CompiledAttribute compileAttribute(
         Element source, String name,
         String value, Schema.Type type,
         String when)
         {
             String parent_name = source.getAttributeValue("id");
             if (parent_name == null) {
                 parent_name =  CompilerUtils.attributeUniqueName(source, name);
             }
             String canonicalValue = null;
             boolean warnOnDeprecatedConstraints = true;
 
             if (value == null) {
                 throw new RuntimeException(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="value is null in " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-956", new Object[] {source})
 );
             }
 
             Matcher m = constraintPat.matcher(value);
             // value.matches("\\s*['\"]\\S*['\"]\\s*")
             if (value.matches("\\s*\\$\\s*\\(")) {
                 env.warn(
                     "The syntax '$(...)' is not valid, "
                     + "you probably meant to use curly-braces instead '${...}'",
                     source);
             } else if (m.matches()) {
                 // extract out $when{value}
                 when = m.group(1);
                 value = m.group(2);
                 // Expressions override any when value, default is
                 // constraint
                 if (when.equals("")) {
                     when = WHEN_ALWAYS;
                 }
             } else if (type == ViewSchema.XML_LITERAL) {
                 value = "LzDataElement.stringToLzData("+value+")";
             } else if (type == ViewSchema.COLOR_TYPE) {
                 if (when.equals(WHEN_IMMEDIATELY)) {
                     try {
                         value = "0x" +
                             Integer.toHexString(ViewSchema.parseColor(value));
                     } catch (ColorFormatException e) {
                         when = WHEN_ONCE;
                     }
                 }
                 value = "LzColorUtils.convertColor('" + value + "')";
             } else if (type == ViewSchema.CSS_TYPE) {
                 if (when.equals(WHEN_IMMEDIATELY)) {
                     try {
                         Map cssProperties = new CSSParser
                             (new AttributeStream(source, name, value)).Parse();
                         for (Iterator i2 = cssProperties.entrySet().iterator(); i2.hasNext(); ) {
                             Map.Entry entry = (Map.Entry) i2.next();
                             Object mv = entry.getValue();
                             if (mv instanceof String) {
                                 entry.setValue(ScriptCompiler.quote((String) mv));
                             }
                         }
                         canonicalValue = ScriptCompiler.objectAsJavascript(cssProperties);
                     } catch (org.openlaszlo.css.ParseException e) {
                         // Or just set when to WHEN_ONCE and fall
                         // through to TODO?
                         throw new CompilationError(e);
                     } catch (org.openlaszlo.css.TokenMgrError e) {
                         // Or just set when to WHEN_ONCE and fall
                         // through to TODO?
                         throw new CompilationError(e);
                     }
                 }
                 // TODO: [2003-05-02 ptw] Wrap non-constant styles in
                 // runtime parser
             } else if (type == ViewSchema.STRING_TYPE
                        || type == ViewSchema.TOKEN_TYPE
                        || type == ViewSchema.ID_TYPE
                        ) {
                 // Immediate string attributes are auto-quoted
                 if (when.equals(WHEN_IMMEDIATELY)) {
                     value = ScriptCompiler.quote(value);
                 }
             } else if ((type == ViewSchema.EXPRESSION_TYPE) ||
                        (type == ViewSchema.BOOLEAN_TYPE) ||
                        (type == ViewSchema.NODE_TYPE)) {
                 // No change currently, possibly analyze expressions
                 // and default non-constant to when="once" in the
                 // future
             } else if (type == ViewSchema.INHERITABLE_BOOLEAN_TYPE) {
                 // change "inherit" to null and pass true/false through as expression
                 if ("inherit".equals(value)) {
                     value = "null";
                 } else if ("true".equals(value)) {
                     value = "true";
                 } else if ("false".equals(value)) {
                     value = "false";
                 } else {
                     // TODO [hqm 2007-0] i8nalize this message
                     env.warn("attribute '"+name+"' must have the value 'true', 'false', or 'inherit'",
                              element);
                 }
             } else if (type == ViewSchema.NUMBER_TYPE) {
                 // No change currently, possibly analyze expressions
                 // and default non-constant to when="once" in the
                 // future
             } else if (type == ViewSchema.NUMBER_EXPRESSION_TYPE ||
                        type == ViewSchema.SIZE_EXPRESSION_TYPE) {
                 // if it's a number that ends in percent:
                 if (value.trim().endsWith("%")) {
                     String numstr = value.trim();
                     numstr = numstr.substring(0, numstr.length() - 1);
                     try {
                         double scale = new Float(numstr).floatValue() / 100.0;
                         warnOnDeprecatedConstraints = false;
                         String referenceAttribute = name;
                         if ("x".equals(name)) {
                             referenceAttribute = "width";
                         } else if ("y".equals(name)) {
                             referenceAttribute = "height";
                         }
                         value = "immediateparent." + referenceAttribute;
                         if (scale != 1.0) {
                             // This special case doesn't change the
                             // semantics, but it generates shorter (since
                             // the sc doesn't fold constants) and more
                             // debuggable code
                             value += "\n * " + scale;
                         }
                         // fall through to the reference case
                     } catch (NumberFormatException e) {
                         // fall through
                     }
                 }
                 // if it's a literal, treat it the same as a number
                 try {
                     new Float(value); // for effect, to generate the exception
                     when = WHEN_IMMEDIATELY;
                 } catch (NumberFormatException e) {
                     // It's not a constant, unless when has been
                     // specified, default to a constraint
                     if (when.equals(WHEN_IMMEDIATELY)) {
                         if (warnOnDeprecatedConstraints) {
                             env.warn(
                                 "Use " + name + "=\"${" + value + "}\" instead.",
                                 element);
                         }
                         when = WHEN_ALWAYS;
                     }
                 }
             } else if (type == ViewSchema.EVENT_HANDLER_TYPE) {
               // Someone said <attribute name="..." ... /> instead of
               // <event name="..." />
               throw new CompilationError(element, name, 
                                          new Throwable ("'" + name + "' is an event and may not be redeclared as an attribute"));
             } else if (type == ViewSchema.REFERENCE_TYPE) {
                 // type="reference" is defined to imply when="once"
                 // since reference expressions are unlikely to
                 // evaluate correctly at when="immediate" time
                 if (when.equals(WHEN_IMMEDIATELY)) {
                     when = WHEN_ONCE;
                 }
             } else if (type == ViewSchema.METHOD_TYPE) {
                 // methods are emitted elsewhere
             } else {
                 throw new RuntimeException("unknown schema datatype " + type);
             }
 
             if (canonicalValue == null)
                 canonicalValue = value;
 
             return new CompiledAttribute(name, type, canonicalValue, when, source, env);
         }
 
     static final Schema.Type EVENT_TYPE = Schema.newType("LzEvent");
 
     /* Handle the <event> tag
      * example: <event name="onfoobar"/>
     */
     void addEventElement(Element element) {
         schema.checkEventAttributes(element, env);
         String name;
         try {
             name = ElementCompiler.requireIdentifierAttributeValue(element, "name");
         } catch (MissingAttributeException e) {
             throw new CompilationError(
                 /* (non-Javadoc)
                  * @i18n.test
                  * @org-mes="'name' is a required attribute of <" + p[0] + "> and must be a valid identifier"
                  */
                 org.openlaszlo.i18n.LaszloMessages.getMessage(
                     NodeModel.class.getName(),"051018-1157", new Object[] {element.getName()})
                 , element);
         }
 
         // An event is really just an attribute with an implicit
         // default (sentinal) value
         CompiledAttribute cattr =
           new CompiledAttribute(name, EVENT_TYPE, "LzDeclaredEvent", WHEN_IMMEDIATELY, element, env);
         addProperty(name, cattr, ALLOCATION_INSTANCE, element);
     }
 
     void addAttributeElement(Element element) {
         String name;
         try {
             name = ElementCompiler.requireIdentifierAttributeValue(element, "name");
         } catch (MissingAttributeException e) {
             throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="'name' is a required attribute of <" + p[0] + "> and must be a valid identifier"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-1157", new Object[] {element.getName()})
                     , element);
         }
 
         // Check that all attributes on this <attribute> are known names
         schema.checkAttributeAttributes(element, env);
 
         String value = element.getAttributeValue("value");
         String when = element.getAttributeValue("when");
         String typestr = element.getAttributeValue("type");
         String allocation = XMLUtils.getAttributeValue(element, "allocation", ALLOCATION_INSTANCE);
         String style = element.getAttributeValue("style");
         Element parent = element.getParentElement();
         String parent_name = parent.getAttributeValue("id");
 
         if (parent_name == null) {
             parent_name = CompilerUtils.attributeUniqueName(element, name);
         }
 
         // Default when according to parent
         if (when == null) {
             when = this.getAttributeValueDefault(
                 name, "when", WHEN_IMMEDIATELY);
         }
 
         Schema.Type type = null;
         Schema.Type parenttype = null;
         boolean forceOverride = false;
 
         // Class methods are not inherited, hence do not override
         if (ALLOCATION_INSTANCE.equals(allocation)) {
           AttributeSpec parentAttrSpec = schema.getAttributeSpec(parent.getName(), name, allocation);
           forceOverride = parentAttrSpec != null && "false".equals(parentAttrSpec.isfinal);
         }
 
         try {
             if ("class".equals(tagName) || "interface".equals(tagName)) {
               if (allocation.equals(ALLOCATION_INSTANCE)) {
                 parenttype = getAttributeTypeInfoFromSuperclass(parent, name, allocation);
               }
             }  else {
               // TODO: [2008-05-05 ptw] Schema needs to learn about
               // allocation
               assert ALLOCATION_INSTANCE.equals(allocation);
               parenttype = schema.getAttributeType(parent, name, allocation);
             }
         } catch (UnknownAttributeException e) {
             // If attribute type is not defined on parent, leave
             // parenttype null.  The user can define it however they
             // like.
         }
 
         if (typestr == null) {
             // Did user supply an explicit attribute type?
             //  No.  Default to parent type if there is one, else
             // EXPRESSION type.
             if (parenttype == null) {
                 type = ViewSchema.EXPRESSION_TYPE;
             } else {
                 type = parenttype;
             }
         } else {
             // parse attribute type and compare to parent type
             type = schema.getTypeForName(typestr);
             if (type == null) {
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="unknown attribute type: " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-1211", new Object[] {typestr})
 , element);
             }
 
             // If we are trying to declare the attribute with a
             // conflicting type to the parent, throw an error
             if (!forceOverride && parenttype != null && type != parenttype) {
                 env.warn(
                     new CompilationError(
                         element,
                         name,
                         new Throwable(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="In element '" + p[0] + "' attribute '" + p[1] + "' with type '" + p[2] + "' is overriding parent class attribute with same name but different type: " + p[3]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-1227", new Object[] {parent.getName(), name, type.toString(), parenttype.toString()}))
                         )
                     );
             }
         }
 
 
 
         // Warn if we are overidding a method, handler, or other function
         if (!forceOverride &&
             (parenttype == schema.METHOD_TYPE ||
              parenttype == schema.EVENT_HANDLER_TYPE ||
              parenttype == schema.SETTER_TYPE ||
              parenttype == schema.REFERENCE_TYPE)) {
             env.warn( "In element '" + parent.getName() 
                       + "' attribute '" +  name 
                       + "' is overriding parent class attribute which has the same name but type: "
                       + parenttype.toString(),
                       element);
         }
 
         CompiledAttribute cattr;
         // Value may be null if attribute is only declared
         if (value != null) {
             cattr = compileAttribute(element, name, value, type, when);
         } else {
             cattr = new CompiledAttribute(name, type, null, WHEN_IMMEDIATELY, element, env);
         }
 
         if (style != null) {
             // style attributes take precedent over value attributes
             value = "$style{" + ScriptCompiler.quote(style) + "}";
 
             // preserve the original value in case the style match fails...
             Object origvalue = cattr.getInitialValue();
             String origvalueexpression;
             if (origvalue instanceof NodeModel.BindingExpr) {
                 // add dependancy methods
                 if (ALLOCATION_INSTANCE.equals(allocation)) {
                     attrs = this.attrs;
                 } else if (ALLOCATION_CLASS.equals(allocation)) {
                     attrs = this.classAttrs;
                 }
                 if (cattr.bindername != null) {
                     attrs.put(cattr.bindername, cattr.getBinderMethod(false));
                 }
                 if (cattr.dependenciesname != null) {
                     attrs.put(cattr.dependenciesname, cattr.getDependenciesMethod(false));
                 }
 
                 origvalueexpression = ((NodeModel.BindingExpr)origvalue).getExpr();
             } else {
                 origvalueexpression = (String)origvalue;
             }
 
             // replace compiled attr with the new style expression
             cattr = compileAttribute(element, name, value, type, when);
             cattr.setFallbackExpression(origvalueexpression);
         }
         addProperty(name, cattr, allocation, element);
 
         // Add entry for attribute setter function
         String setter = element.getAttributeValue("setter");
         if (setter != null) {
           addSetterFromAttribute(element, name, setter, allocation);
         }
     }
 
     /** Defines a setter
      *
      * <setter name="attr-name" [args="attr-name"]>
      *   [function body]
      * </handler>
      *
      * This defines a setter for the attribute named `attr-name` that
      * will be invoked by setAttribute.
      *
      * TODO [2008-07-25 ptw] Reconcile how this works with storing the
      * state in an attribute of the same name vs. having real
      * setter/getter pairs.
      */
     void addSetterElement(Element element) {
         schema.checkSetterAttributes(element, env);
         String attribute = element.getAttributeValue("name");
         if ((attribute == null || !ScriptCompiler.isIdentifier(attribute))) {
             env.warn("setter needs a non-null name attribute");
             return;
         }
         String args = CompilerUtils.attributeLocationDirective(element, "args") +
             // Setters get called with one argument.  The default is
             // for the argument to have the same name as the attribute
             // this is the setter for
             XMLUtils.getAttributeValue(element, "args", attribute);
         String allocation = XMLUtils.getAttributeValue(element, "allocation", ALLOCATION_INSTANCE);
         String body = element.getText();
         if (body.trim().length() == 0) { body = null; }
         if (body != null) {
           body = CompilerUtils.attributeLocationDirective(element, "text") + body;
         }
         addSetterInternal(element, attribute, args, body, allocation);
     }
 
     /**
      * A setter defined in the open tag
      */
     void addSetterFromAttribute(Element element, String attribute, String body, String allocation) {
       // By default the argument to the setter method is the same name
       // as the attribute it is the setter for
       addSetterInternal(element, attribute, attribute, body, allocation);
     }
 
     /**
      * Adds a setter method for `attribute` with body `body`.
      */
     void addSetterInternal(Element element, String attribute, String args, String body, String allocation) {
       if (body == null) {
         env.warn("Refusing to compile an empty setter", element);
         return;
       }
       // By convention setters are put in the 'lzc' namespace with
       // the name set_<property name> NOTE: LzNode#applyArgs and
       // #setAttribute depend on this convention to find setters
       String settername = "$lzc$" + "set_" + attribute;
       String pragmas = "#pragma " + ScriptCompiler.quote("userFunctionName=set " + attribute) + "\n";
       addMethodInternal(settername, args, "", pragmas + body, element, allocation);
       // This is just for nice error messages
       if (setters.get(attribute) != null) {
         env.warn(
           "a setter for attribute named '"+attribute+
           "' is already defined on "+getMessageName(),
           element);
       }
       setters.put(attribute, ScriptCompiler.quote(settername));
     }
 
     /* Handle a <data> tag.
      * If there is more than one immediate child data node at the top level, signal a warning.
      */
     void addLiteralDataElement(Element element) {
         String name = element.getAttributeValue("name");
 
         if (name == null) {
             name = "initialdata";
         }
 
         boolean trimWhitespace = "true".equals(element.getAttributeValue("trimwhitespace"));
 
         String xmlcontent = getDatasetContent(element, env, trimWhitespace);
 
         Element parent = element.getParentElement();
 
         CompiledAttribute cattr = compileAttribute(element,
                                                    name,
                                                    xmlcontent,
                                                    ViewSchema.XML_LITERAL,
                                                    WHEN_IMMEDIATELY);
 
         addProperty(name, cattr, ALLOCATION_INSTANCE, element);
     }
 
         
 
     boolean hasAttribute(String name) {
         return attrs.containsKey(name);
     }
 
     void removeAttribute(String name) {
         attrs.remove(name);
     }
 
     void setAttribute(String name, Object value) {
         attrs.put(name, value);
     }
 
     boolean hasClassAttribute(String name) {
         return classAttrs.containsKey(name);
     }
 
     void removeClassAttribute(String name) {
         classAttrs.remove(name);
     }
 
     void setClassAttribute(String name, Object value) {
         classAttrs.put(name, value);
     }
 
     void addText() {
         if (schema.hasHTMLContent(element)) {
             String text = TextCompiler.getHTMLContent(element);
             if (text.length() != 0) {
                 if (!attrs.containsKey("text")) {
                   addProperty("text", ScriptCompiler.quote(text), ALLOCATION_INSTANCE);
                 }
             }
         } else if (schema.hasTextContent(element)) {
             String text;
             // The current inputtext component doesn't understand
             // HTML, but we'd like to have some way to enter
             // linebreaks in the source.
             text = TextCompiler.getInputText(element);
             if (text.length() != 0) {
                 if (!attrs.containsKey("text")) {
                   addProperty("text", ScriptCompiler.quote(text), ALLOCATION_INSTANCE);
                 }
             }
         }
     }
 
     void updateAttrs() {
         if (frozen) return;
         if (!delegateList.isEmpty()) {
           addProperty("$delegates", delegateList, ALLOCATION_INSTANCE);
         }
         if (datapath != null) {
           addProperty("$datapath", datapath.asMap(), ALLOCATION_INSTANCE);
           // If we've got an explicit datapath value, we have to null
           // out the "datapath" attribute with the magic
           // LzNode._ignoreAttribute value, so it doesn't get
           // overridden by an inherited value from the class.
           addProperty("datapath", "LzNode._ignoreAttribute", ALLOCATION_INSTANCE);
         }
         // This can only happen once.
         frozen = true;
     }
 
     Map getAttrs() {
       updateAttrs();
       return attrs;
     }
 
     boolean hasMethods() {
         for (Iterator i = attrs.values().iterator(); i.hasNext(); ) {
             if (i.next() instanceof Method) { return true; }
         }
         return false;
     }
 
     Map getClassAttrs() {
       return classAttrs;
     }
 
     Map getSetters() {
         return setters;
     }
 
     Map asMap() {
         if (frozen) {
           throw new CompilerImplementationError("Attempting to asMap when NodeModel frozen");
         }
         updateAttrs();
         assert classAttrs.isEmpty();
         ClassModel classModel = schema.getClassModel(tagName);
         Map map = new LinkedHashMap();
         Map inits = new LinkedHashMap();
         boolean hasMethods = false;
         // Whether we make a class to hold the methods or not,
         // implicit replication relies on non-method properties coming
         // in as instance attributes, so we have to pluck them out
         // here (and turn the attributes into just declarations, by
         // setting their value to null).
         //
         // Node as map just wants to see all the attrs, so clean out
         // the binding markers
         for (Iterator i = attrs.entrySet().iterator(); i.hasNext(); ) {
             Map.Entry entry = (Map.Entry) i.next();
             String key = (String) entry.getKey();
             Object value = entry.getValue();
             if (value instanceof Method) {
               hasMethods = true;
             } else if (! (value instanceof NodeModel.BindingExpr)) {
                 inits.put(key, value);
                 attrs.put(key, null);
             } else {
                 inits.put(key, ((NodeModel.BindingExpr)value).getExpr());
                 attrs.put(key, null);
             }
         }
         if (hasMethods) {
             // If there are methods, make a class (but don't publish it)
             classModel = new ClassModel(tagName, parentClassModel, false, schema, element, env);
             classModel.setNodeModel(this);
             classModel.emitClassDeclaration(env);
         } else {
             // If no class needed, Put children into map
             if (!children.isEmpty()) {
                 map.put("children", childrenMaps());
             }
         }
 
         // Non-method attributes
         if (!inits.isEmpty()) {
             map.put("attrs", inits);
         }
         // Allow forward references
         if (! classModel.isCompiled()) {
           classModel.compile(env);
         }
         if (classModel.anonymous || classModel.builtin || env.tagDefined(tagName)) {
           // The class to instantiate
           map.put("class", classModel.className);
         } else {
           // Non-anonymous classes may be deferred, so we must
           // indirect through the tagname
           map.put("tag", ScriptCompiler.quote(tagName));
         }
 
         return map;
     }
 
     void assignClassRoot(int depth) {
         if (! parentClassModel.isSubclassOf(schema.getClassModel("state"))) { depth++; }
         Integer d = new Integer(depth);
         for (Iterator i = children.iterator(); i.hasNext();) {
             NodeModel child = (NodeModel)i.next();
             child.addProperty("$classrootdepth", d, ALLOCATION_INSTANCE);
             child.assignClassRoot(depth);
         }
     }
 
     List childrenMaps() {
         List childMaps = new Vector(children.size());
         for (Iterator iter = children.iterator(); iter.hasNext(); )
             childMaps.add(((NodeModel) iter.next()).asMap());
 
         // TODO: [2006-09-28 ptw] There must be a better way. See
         // comment in LzNode where $lzc$class_userClassPlacement is
         // inserted in lz regarding the wart this is. You need some
         // way to not set defaultplacement until the class-defined
         // children are instantiated, only the instance-defined
         // children should get default placement. For now this is done
         // by inserting this sentinel in the child nodes...
         if ("class".equals(tagName) && hasAttribute("defaultplacement")) {
             LinkedHashMap dummy = new LinkedHashMap();
             dummy.put("class", "$lzc$class_userClassPlacement");
             dummy.put("attrs", attrs.get("defaultplacement"));
             removeAttribute("defaultplacement");
             childMaps.add(dummy);
         }
 
         return childMaps;
     }
 
     /** Expand eligible instances by replacing the instance by the
      * merge of its class definition with the instance content
      * (attributes and children).  An eligible instance is an instance
      * of a compile-time class, that doesn't contain any merge
      * stoppers.  If the class and the instance contain a member with
      * the same name, this is a merge stopper.  In the future, this
      * restriction may be relaxed, but will probably always include
      * the case where a class and instance have a member with the same
      * name and the instance name calls a superclass method. */
     NodeModel expandClassDefinitions() {
         NodeModel model = this;
         while (true) {
             ClassModel classModel = schema.getClassModel(model.tagName);
             if (classModel == null)
                 break;
             if (classModel.getSuperTagName() == null)
                 break;
             if (!classModel.getInline())
                 break;
             model = classModel.applyClass(model);
             // Iterate to allow for the original classes superclass to
             // be expanded as well.
         }
         // Recurse. Make a copy so we can replace the child list.
         // TODO [2004-0604]: As an optimization, only do this if one
         // of the children changed.
         model = (NodeModel) model.clone();
         for (ListIterator iter = model.children.listIterator();
              iter.hasNext(); ) {
             NodeModel child = (NodeModel) iter.next();
             iter.set(child.expandClassDefinitions());
         }
         return model;
     }
 
     /** Replace members of this with like-named members of source. */
     void updateMembers(NodeModel source) {
         final String OPTIONS_ATTR_NAME = "options";
 
         // Check for duplicate methods.  Collect all the keys that name
         // a Method in both the source and target.
         List sharedMethods = new Vector();
         for (Iterator iter = attrs.keySet().iterator();
              iter.hasNext(); ) {
             String key = (String) iter.next();
             if (attrs.get(key) instanceof Method &&
                 source.attrs.get(key) instanceof Method)
                 sharedMethods.add(key);
         }
         if (!sharedMethods.isEmpty())
             throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Both the class and the instance or subclass define the method" + p[0] + p[1]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-1409", new Object[] {new ChoiceFormat("1# |1<s ").format(sharedMethods.size()), new ListFormat("and").format(sharedMethods)})
                 );
 
         // Check for attributes that have a value in this and
         // a setter in the source.  These can't be merged.
         Collection overriddenAttributes = CollectionUtils.intersection(
             attrs.keySet(), source.setters.keySet());
         if (!overriddenAttributes.isEmpty())
             throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="A class that defines a value can't be inlined against a " + "subclass or instance that defines a setter.  The following " + p[0] + " this condition: " + p[1]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 NodeModel.class.getName(),"051018-1422", new Object[] {new ChoiceFormat("1#attribute violates|1<attributes violate").format(overriddenAttributes.size()), new ListFormat("and").format(overriddenAttributes)})
                 );
 
         // Do the actual merge.
         id = source.id;
         if (source.initstage != null)
             initstage = source.initstage;
         Object options = attrs.get(OPTIONS_ATTR_NAME);
         Object sourceOptions = source.attrs.get(OPTIONS_ATTR_NAME);
         attrs.putAll(source.attrs);
         if (options instanceof Map && sourceOptions instanceof Map) {
 //             System.err.println(options);
 //             System.err.println(sourceOptions);
             Map newOptions = new HashMap((Map) options);
             newOptions.putAll((Map) sourceOptions);
             attrs.put(OPTIONS_ATTR_NAME, newOptions);
         }
         delegates.putAll(source.delegates);
         classAttrs.putAll(source.classAttrs);
         setters.putAll(source.setters);
         delegateList.addAll(source.delegateList);
         // TBD: warn on children that share a name?
         // TBD: update the node count
         children.addAll(source.children);
     }
 }
