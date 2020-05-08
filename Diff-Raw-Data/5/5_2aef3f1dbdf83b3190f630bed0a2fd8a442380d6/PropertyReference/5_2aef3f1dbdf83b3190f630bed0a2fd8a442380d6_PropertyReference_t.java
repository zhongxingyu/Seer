 /* *****************************************************************************
  * PropertyUtils.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2007-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.js2doc;
 
 import java.util.*;
 import java.util.logging.*;
 import org.openlaszlo.sc.parser.*;
 import org.openlaszlo.js2doc.JS2DocUtils.InternalError;
 import org.w3c.dom.*;
 
 public class PropertyReference {
 
     static private Logger logger = Logger.getLogger("org.openlaszlo.js2doc");
 
     org.w3c.dom.Element propertyOwner;
     String propertyName;
     ConditionalState state;
     
     private org.w3c.dom.Element cachedProperty;
     private org.w3c.dom.Element cachedValue;
 
     public PropertyReference() { 
         this.propertyOwner = null; 
         this.propertyName = null;
         this.state = null;
         this.cachedProperty = null;
         this.cachedValue = null;
     }
     
     public PropertyReference(PropertyReference orig) {
         this.propertyOwner = orig.propertyOwner; 
         this.propertyName = orig.propertyName;
         this.state = orig.state;
         this.cachedProperty = orig.cachedProperty;
         this.cachedValue = orig.cachedValue;
     }
     
     public PropertyReference(org.w3c.dom.Element owner, String propertyName, ConditionalState state) { 
         this.propertyOwner = owner; 
         this.propertyName = propertyName; 
         if (this.propertyName == null) {
             logger.warning("explicit propertyName has null name");
         }
         this.state = state;
         this.cachedProperty = null;
         this.cachedValue = null;
     }
     
     public PropertyReference(org.w3c.dom.Element owner, SimpleNode lvalDesc, ConditionalState state) {
         this();
         resolvePropertyReference(owner, lvalDesc, state);
     }
     
     public boolean isValid() {
         return (this.propertyOwner != null && this.propertyName != null);
     }
     
     public boolean hasOwner() {
         return (this.propertyOwner != null);
     }
     
     public boolean hasProperty() {
         resolveProperty();
         return (this.cachedProperty != null);
     }
     
     public org.w3c.dom.Element getProperty() {
         resolveProperty();
         return this.cachedProperty;
     }
     
     public org.w3c.dom.Element ensureProperty() {
         resolveProperty();
         if (this.isValid() == false)
             return null;
         if (this.cachedProperty == null) {
             this.createProperty();
         }
         return this.cachedProperty;
     }
     
     /** Create new property node, deleting any previous property node if necessary */        
     public org.w3c.dom.Element redefineProperty(String comment) {
         resolveProperty();
         if (this.isValid() == false)
             return null;
         if (this.cachedProperty != null) {
             this.propertyOwner.removeChild(this.cachedProperty);
             // clean member in case createElement throws (unlikely, I know...)
             this.cachedProperty = null;
         }    
 
         this.cachedProperty = propertyOwner.getOwnerDocument().createElement("property");
         this.propertyOwner.appendChild(this.cachedProperty);
         
         this.cachedProperty.setAttribute("name", this.propertyName);
 
         this.setPropertyMetadata(comment);
 
         return this.cachedProperty;
     }
     
     public boolean hasValue() {
         resolveValue();
         return (this.cachedValue != null);
     }
     
     public org.w3c.dom.Element getValue() {
         resolveValue();
         return this.cachedValue;
     }
     
     public org.w3c.dom.Element redefineValue(SimpleNode valueDesc) {
         this.resolveProperty();
         if (this.cachedProperty == null) {
             logger.warning("cannot set value -- no property to bind to");
             return null;
         } else if (this.cachedValue != null) {
             this.cachedProperty.removeChild(this.cachedValue);
             this.cachedValue = null;
         }
             
         this.setPropertyValue(valueDesc);
         
         return this.cachedValue;
     }
     
     public String derivePropertyID() {
         if (this.isValid() == false) {
             logger.warning("propertyName: " + this.propertyName);
             throw new InternalError("Can't derive ID of invalid property", (SimpleNode)null);
         }
         return JS2DocUtils.derivePropertyID(this.propertyOwner, this.propertyName, this.state);
     }
     
     private static void addFunctionParameter(SimpleNode parseNode, org.w3c.dom.Element docNode) {
         if (parseNode instanceof ASTFormalInitializer)
             return;
 
         org.w3c.dom.Element paramNode = docNode.getOwnerDocument().createElement("parameter");
         docNode.appendChild(paramNode);
         
         if (! (parseNode instanceof ASTIdentifier))
             throw new InternalError("FunctionParameter is not an ASTIdentifier", parseNode);
 
         ASTIdentifier param = (ASTIdentifier) parseNode;
         paramNode.setAttribute("name", param.getName());
     }
         
     protected org.w3c.dom.Element setClassValue(SimpleNode parseNode) {
         resolveProperty();
         org.w3c.dom.Element classNode = this.cachedProperty.getOwnerDocument().createElement("class");
         this.cachedProperty.appendChild(classNode);
         
         SimpleNode[] children = parseNode.getChildren();
         Iterator iter = Arrays.asList(children).iterator();
         
         SimpleNode temp = (SimpleNode) iter.next();
         // assert temp instanceof ASTIdentifier && ((ASIdentifier) temp).getName() == "class" or "mixin"
 
         ASTIdentifier nameNode = (ASTIdentifier) iter.next();
         String name = nameNode.getName();
 
         SimpleNode extendsNode = (SimpleNode) iter.next();
         if (extendsNode instanceof ASTIdentifier) {
             ASTIdentifier extendsNameNode = (ASTIdentifier) extendsNode;
             String extendsName = extendsNameNode.getName();
             PropertyReference extendsRef = new PropertyReference(this.propertyOwner, extendsName, this.state);
             if (extendsRef.hasProperty() == false) {
                 extendsRef = new PropertyReference(this.propertyOwner, extendsName, null);
             }
             String extendsID = extendsRef.derivePropertyID();
             classNode.setAttribute("extends", extendsID);
         } else {
             if (! (extendsNode instanceof ASTEmptyExpression))
                 throw new InternalError("unexpected node type parsing extends", extendsNode);
         }
         
         SimpleNode withList = (SimpleNode) iter.next();
         if (withList instanceof ASTMixinsList) {
             String with = "";
             SimpleNode[] withChildren = withList.getChildren();
             Iterator iter2 = Arrays.asList(withChildren).iterator();
             while (iter2.hasNext()) {
                 SimpleNode withNode = (SimpleNode)iter2.next();
                 if (! (withNode instanceof ASTIdentifier)) {
                     throw new InternalError("unexpected node in with list", withNode);
                 }
                 if (with.length() > 0) {
                     with += ",";
                 }
                 with += ((ASTIdentifier)withNode).getName();
             }
 
             if (with.length() > 0) {
                classNode.setAttribute("inherits", with);
             }
         } else {
             if (! (withList instanceof ASTEmptyExpression))
                 throw new InternalError("unexpected node type parsing with", withList);
         }
         
         this.cachedValue = classNode;
         return classNode;
     }
 
     private void setPropertyValueFromNewExpression(ASTIdentifier classNameNode) {
         String className = classNameNode.getName();
         logger.fine("setting property to new " + className);
         if (className.equals("Object")) {
             org.w3c.dom.Element objectNode = this.cachedProperty.getOwnerDocument().createElement("object");
             this.cachedProperty.appendChild(objectNode);
         }
         else if (className.equals("Function")) {
             org.w3c.dom.Element objectNode = this.cachedProperty.getOwnerDocument().createElement("function");
             this.cachedProperty.appendChild(objectNode);
         }
         else {
             org.w3c.dom.Element objectNode = this.cachedProperty.getOwnerDocument().createElement("object");
             this.cachedProperty.appendChild(objectNode);
             objectNode.setAttribute("type", className);
         }
     }
     
     private void setPropertyValue(SimpleNode valueNode) {
         resolveProperty();
         if (valueNode instanceof ASTNewExpression) {
             JS2DocUtils.checkChildrenLowerBounds(valueNode, 1, 1, "visitVariableDeclaration");
             SimpleNode classExprNode = valueNode.getChildren()[0];
             if (classExprNode instanceof ASTIdentifier) {
                 setPropertyValueFromNewExpression((ASTIdentifier) classExprNode);
                 
             } else if (classExprNode instanceof ASTCallExpression) {
                 setPropertyValueFromNewExpression((ASTIdentifier) classExprNode.getChildren()[0]);
             }
         } else if (valueNode instanceof ASTFunctionExpression ||
                    valueNode instanceof ASTFunctionDeclaration) {
             JS2DocUtils.checkChildrenLowerBounds(valueNode, 3, 3, "visitVariableDeclaration");
 
             org.w3c.dom.Element fnNode = this.cachedProperty.getOwnerDocument().createElement("function");
             this.cachedProperty.appendChild(fnNode);
             
             SimpleNode[] children = valueNode.getChildren();
             boolean functionHasName = (children[0] instanceof ASTIdentifier);
             ASTFormalParameterList paramList = 
                 (ASTFormalParameterList) children[functionHasName ? 1 : 0];
             
             SimpleNode[] params = paramList.getChildren();
             for (int i = 0; i < params.length; i++) {
                 addFunctionParameter(params[i], fnNode);
             }
         } else if (valueNode instanceof ASTClassDefinition) {
             this.setClassValue(valueNode);
         } else if (valueNode instanceof ASTObjectLiteral) {
             resolveProperty();
             org.w3c.dom.Element objectNode = this.cachedProperty.getOwnerDocument().createElement("object");
             this.cachedProperty.appendChild(objectNode);
             this.cachedValue = objectNode;
         } else if (valueNode instanceof ASTLiteral) {
             Object value = ((ASTLiteral) valueNode).getValue();
             String valueString = (value == null) ? "null" : value.toString();
             this.cachedProperty.setAttribute("value", valueString);
         } else if (valueNode instanceof ASTIdentifier
                 || valueNode instanceof ASTThisReference
                 || valueNode instanceof ASTPropertyIdentifierReference
                 || valueNode instanceof ASTArrayLiteral
                 || valueNode instanceof ASTConditionalExpression
                 || valueNode instanceof ASTUnaryExpression
                 || valueNode instanceof ASTBinaryExpressionSequence
                 || valueNode instanceof ASTOrExpressionSequence
                 || valueNode instanceof ASTCallExpression
                 || valueNode instanceof ASTAndExpressionSequence) {
             logger.fine("NYI " + valueNode.getClass().getName() + " initializer");
         } else {
             logger.warning("ignoring unknown initializer type: " + valueNode.getClass().getName());
             JS2DocUtils.debugPrintNode(valueNode);
         }
     }
     
     private void setPropertyMetadata(String comment) {
         this.resolveProperty();
         if (this.cachedProperty == null)
             return;
 
         if (comment != null) {
             Comment parsedComment = Comment.extractLastJS2DocFromCommentSequence(comment);
             parsedComment.appendAsXML(this.cachedProperty);
         }
         
         if (this.state != null)
             JS2DocUtils.describeConditionalState(this.state, this.cachedProperty);
         
         String propertyID = this.derivePropertyID();
         this.cachedProperty.setAttribute("id", propertyID);
 
     }
     
     private void resolvePropertyReference(org.w3c.dom.Element owner, SimpleNode lvalDesc, ConditionalState state) {
 
         if (lvalDesc instanceof ASTIdentifier) {
         
             this.propertyOwner = owner;
             this.propertyName = ((ASTIdentifier) lvalDesc).getName();
             this.state = state;
             
             if (this.propertyName == null) {
                 logger.warning("lval identifier propertyName has null name");
             }
             
         } else if (lvalDesc instanceof ASTPropertyIdentifierReference) {
         
             JS2DocUtils.checkChildrenLowerBounds(lvalDesc, 2, 2, "findLVal");
             SimpleNode[] children = lvalDesc.getChildren();
 
             PropertyReference ownerLVal = new PropertyReference(owner, children[0], state);
 
             if (ownerLVal.hasProperty() == true) {
             
                 this.propertyOwner = ownerLVal.getValue();
 
                 if (this.propertyOwner == null) {
                     logger.warning("lvalue has no binding; can't attach property " + propertyName);
                 }
 
                 SimpleNode propertyNameNode = children[1];
                 if (! (propertyNameNode instanceof ASTIdentifier))
                     throw new InternalError("propertyNameNode is not an ASTIdentifier", lvalDesc);
 
                 this.propertyName = ((ASTIdentifier) propertyNameNode).getName();
 
                 if (this.propertyName == null) {
                     logger.warning("lval identifier propertyName has null name");
                 }
                 
                 this.state = null;
                 
             } else {
                 
                 org.w3c.dom.Node parent = owner.getParentNode();
                 if (parent != null && parent instanceof org.w3c.dom.Element)
                     resolvePropertyReference((org.w3c.dom.Element) parent, lvalDesc, state);
             }
             
         } else if (lvalDesc instanceof ASTPropertyValueReference) {
             logger.fine("NYI property value reference");
         } else {
             JS2DocUtils.debugPrintNode(lvalDesc);
             throw new InternalError("Unhandled lval parser type", lvalDesc);
         }
     }
     
     private void resolveProperty() {
         if (this.cachedProperty == null) {
             if (this.isValid()) {
                 String id = derivePropertyID();
                 this.cachedProperty = JS2DocUtils.findFirstChildElementWithAttribute(this.propertyOwner, "property", "id", id);
                 
                 if (this.cachedProperty == null) {
                     String name = this.propertyName;
                     if (name.equals("prototype") || name.equals("setters")) {
                         this.createProperty();
                     }
                     if (this.propertyOwner.getNodeName().equals("js2doc")) {
                         if (name.equals("Object") || name.equals("Function") || name.equals("Array") || name.equals("String")
                             || name.equals("Boolean") || name.equals("Number") || name.equals("Date")) {
                           this.createProperty();
                           this.cachedProperty.setAttribute("topic","JavaScript");
                           this.cachedProperty.setAttribute("subtopic","Intrinsic Classes");
                         }
                         
                         if (name.equals("window") || name.equals("document") || name.equals("_root")) {
                           this.createProperty();
                           this.cachedProperty.setAttribute("topic","JavaScript");
                           this.cachedProperty.setAttribute("subtopic","DOM");
                         }
                     }
                 }
             }
         }
     }
     
     private void createProperty() {
         this.cachedProperty = propertyOwner.getOwnerDocument().createElement("property");
         this.propertyOwner.appendChild(this.cachedProperty);
         this.cachedProperty.setAttribute("name", this.propertyName);
         this.setPropertyMetadata(null);
     }
     
     private void resolveValue() {
         resolveProperty();
         if (this.cachedValue == null) {
             if (this.cachedProperty != null) {
                 org.w3c.dom.Element foundElt = null;
                 org.w3c.dom.NodeList childNodes = this.cachedProperty.getChildNodes();
                 final int n = childNodes.getLength();
                 for (int i=0; i<n; i++) {
                     org.w3c.dom.Node childNode = childNodes.item(i);
                     if (childNode instanceof org.w3c.dom.Element) {
                         String nodeName = childNode.getNodeName();
                         if (nodeName.equals("object") ||
                             nodeName.equals("function") ||
                             nodeName.equals("class") ||
                             nodeName.equals("event")) {
                             foundElt = (org.w3c.dom.Element) childNode;
                             break;
                         }
                     }
                 }
                 if (foundElt == null) {
                     foundElt = this.cachedProperty.getOwnerDocument().createElement("object");
                     this.cachedProperty.appendChild(foundElt);
                 }
                 this.cachedValue = foundElt;
             }
         }
     }
 
     public String toString() {
         StringBuffer sb = new StringBuffer("PropertyReference[");
         sb.append("owner=" + propertyOwner);
         sb.append(", name=" + propertyName);
         sb.append(", state=" + state);
         sb.append(", cachedProp=" + cachedProperty);
         sb.append(", cachedValue=" + cachedValue);
         sb.append("]");
         return sb.toString();
     }
     
 }
