 /* *****************************************************************************
  * ClassNode.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.io.*;
 import java.util.*;
 import org.apache.log4j.*;
 import org.jdom.Element;
 import org.jdom.output.Format.TextMode;
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.openlaszlo.xml.internal.MissingAttributeException;
 import org.openlaszlo.xml.internal.Schema;
 import org.openlaszlo.xml.internal.Schema.Type;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.css.CSSParser;
 
 /** Compiler for <code>class</code> class elements.
  */
 class ClassCompiler extends ViewCompiler {
     /**
        For a declaration of a class named "foobar"
        
        <pre>&lt;class name="foobar" extends="view"&gt;</pre>
 
        We are going to call
 
        <pre>
        LzInstantiateView(
       {
         name: 'userclass', 
         attrs: {
                 parent: "view", 
                 initobj: {
                              name: "foobar",
                              attrs: {name: "foobar"},
 
        </pre>
      */
     static final String DEFAULT_SUPERCLASS_NAME = "view";
     
     ClassCompiler(CompilationEnvironment env) {
         super(env);
     }
     
     /** Returns true iff this class applies to this element.
      * @param element an element
      * @return see doc
      */
     static boolean isElement(Element element) {
         return element.getName().equals("class");
     }
 
     /** Parse out an XML class definition, add the superclass and
      * attribute types to the schema.
      *
      * <p>
      * For each CLASS element, find child ATTRIBUTE tags, and add them
      * to the schema.
      *
     * Also, any HANDLER tags will be added to the schema as
      * attributes of type "script".
      */
     void updateSchema(Element element, ViewSchema schema, Set visited) {
         Element elt = element;
         
         String classname = elt.getAttributeValue("name");
         String superclass = elt.getAttributeValue("extends");
         
         if (classname == null || !ScriptCompiler.isIdentifier(classname)) {
             CompilationError cerr = new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The classname attribute, \"name\" must be a valid identifier for a class definition"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ClassCompiler.class.getName(),"051018-77")
 , elt);
             throw(cerr);
         }
         
         if (superclass != null && !ScriptCompiler.isIdentifier(superclass)) {
             mEnv.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The value for the 'extends' attribute on a class definition must be a valid identifier"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ClassCompiler.class.getName(),"051018-89")
 , elt);
             superclass = null;
         }
         if (superclass == null) {
             // Default to LzView
             superclass = ClassCompiler.DEFAULT_SUPERCLASS_NAME;
         }
         
         ClassModel superclassinfo = schema.getClassModel(superclass);
         if (superclassinfo == null) {
             throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="undefined superclass " + p[0] + " for class " + p[1]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ClassCompiler.class.getName(),"051018-106", new Object[] {superclass, classname})
 , elt);
         }
         
         // Collect up the attribute defs, if any, of this class
         List attributeDefs = new ArrayList();
         Iterator iterator = element.getContent().iterator();
         
         while (iterator.hasNext()) {
             Object o = iterator.next();
             if (o instanceof Element) {
                 Element child = (Element) o;
                 // Is this an element named ATTRIBUTE which is a
                 // direct child of a CLASS tag?
                 if (child.getName().equals("attribute")) {
                     String attrName;
                     try {
                         attrName = requireIdentifierAttributeValue(child, "name");
                     } catch (MissingAttributeException e) {
                         throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="'name' is a required attribute of <" + p[0] + "> and must be a valid identifier"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ClassCompiler.class.getName(),"051018-131", new Object[] {child.getName()})
 , child);
                     }
                     
                     String attrTypeName = child.getAttributeValue("type");
                     String attrDefault = child.getAttributeValue("default");
                     String attrSetter = child.getAttributeValue("setter");
                     String attrRequired = child.getAttributeValue("required");
                     
                     ViewSchema.Type attrType;
                     if (attrTypeName == null) {
                         // Check if this attribute exists in ancestor classes,
                         // and if so, default to that type.
                         attrType = superclassinfo.getAttributeType(attrName);
                         if (attrType == null) {
                             // The default attribute type
                             attrType = ViewSchema.EXPRESSION_TYPE;
                         }
                     } else {
                         attrType = schema.getTypeForName(attrTypeName);
                     }
                     
                     if (attrType == null) {
                         throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="In class " + p[0] + " type '" + p[1] + "', declared for attribute '" + p[2] + "' is not a known data type."
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ClassCompiler.class.getName(),"051018-160", new Object[] {classname, attrTypeName, attrName})
                                 , element);
                     }
                     
                     AttributeSpec attrSpec = 
                         new AttributeSpec(attrName, attrType, attrDefault,
                                           attrSetter, child);
                     if (attrName.equals("text") && attrTypeName != null) {
                         if ("text".equals(attrTypeName))
                             attrSpec.contentType = attrSpec.TEXT_CONTENT;
                         else if ("html".equals(attrTypeName))
                             attrSpec.contentType = attrSpec.HTML_CONTENT;
                     }
                     attributeDefs.add(attrSpec);
                } else if (child.getName().equals("handler")) {
                     String attrName;
                     try {
                         attrName = requireIdentifierAttributeValue(child, "name");
                     } catch (MissingAttributeException e) {
                         throw new CompilationError(
                             "'name' is a required attribute of <" + child.getName() + "> and must be a valid identifier", child);
                     }
                     
                     ViewSchema.Type attrType = ViewSchema.EVENT_TYPE;
                     AttributeSpec attrSpec = 
                         new AttributeSpec(attrName, attrType, null, null, child);
                     attributeDefs.add(attrSpec);
                 }
             }
         }
         
         // Add this class to the schema
         schema.addElement(element, classname, superclass, attributeDefs);
     }
     
     public void compile(Element elt) {
         String className;
         try {
             className = requireIdentifierAttributeValue(elt, "name");
         } catch (MissingAttributeException e) {
             throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="'name' is a required attribute of <" + p[0] + "> and must be a valid identifier"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ClassCompiler.class.getName(),"051018-193", new Object[] {elt.getName()})
 , elt);
         }
                 
         String extendsName = XMLUtils.getAttributeValue(
             elt, "extends", DEFAULT_SUPERCLASS_NAME);
         
         ViewSchema schema = mEnv.getSchema();
         ClassModel classModel = schema.getClassModel(className);
         
         String linedir = CompilerUtils.sourceLocationDirective(elt, true);
         ViewCompiler.preprocess(elt, mEnv);
         
         FontInfo fontInfo = new FontInfo(mEnv.getCanvas().getFontInfo());
         
         // We compile a class declaration just like a view, and then
         // add attribute declarations and perhaps some other stuff that
         // the runtime wants.
         NodeModel model = NodeModel.elementAsModel(elt, schema, mEnv);
         model = model.expandClassDefinitions();
         model.removeAttribute("name");
         classModel.setNodeModel(model);
         Map viewMap = model.asMap();
         
         // Put in the class name, not "class" 
         viewMap.put("name", ScriptCompiler.quote(className));
         
         // Construct a Javascript statement from the initobj map
         String initobj;
         try {
             java.io.Writer writer = new java.io.StringWriter();
             ScriptCompiler.writeObject(viewMap, writer);
             initobj = writer.toString();
         } catch (java.io.IOException e) {
             throw new ChainedException(e);
         }
         // Generate a call to queue instantiation
         StringBuffer buffer = new StringBuffer();
         buffer.append(VIEW_INSTANTIATION_FNAME + 
                       "({name: 'userclass', attrs: " +
                       "{parent: " +
                       ScriptCompiler.quote(extendsName) +
                       ", initobj: " + initobj +
                       "}}" +
                       ", " + ((ElementWithLocationInfo)elt).model.totalSubnodes() +
                       ");\n");
         if (!classModel.getInline()) {
             ClassModel superclassModel = classModel.getSuperclassModel();
             mEnv.compileScript(buffer.toString(), elt);
         }
         
         // TODO: [12-27-2002 ows] use the log4j API instead of this property
         boolean tracexml =
             mEnv.getProperties().getProperty("trace.xml", "false") == "true";
         if (tracexml) {
             Logger mXMLLogger = Logger.getLogger("trace.xml");
             mXMLLogger.info("compiling class definition:");
             org.jdom.output.XMLOutputter outputter =
                 new org.jdom.output.XMLOutputter();
             outputter.getFormat().setTextMode(TextMode.NORMALIZE);
             mXMLLogger.info(outputter.outputString(elt));
             mXMLLogger.info("compiled to:\n" + buffer.toString() + "\n");
         }
     }
 }
