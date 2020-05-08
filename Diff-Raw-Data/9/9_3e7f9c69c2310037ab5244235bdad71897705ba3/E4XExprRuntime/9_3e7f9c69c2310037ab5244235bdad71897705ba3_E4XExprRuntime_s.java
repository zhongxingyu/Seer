 package com.intalio.simpel.expr;
 
 import org.apache.ode.bpel.rtrep.v2.*;
 import org.apache.ode.bpel.rtrep.common.ConfigurationException;
 
 import org.apache.ode.bpel.common.FaultException;
 import com.intalio.simpel.omodel.SimPELExpr;
 import org.apache.ode.utils.DOMUtils;
 import org.apache.ode.utils.xsd.Duration;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Delegator;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.serialize.ScriptableInputStream;
 import org.mozilla.javascript.xmlimpl.XMLLibImpl;
 import org.mozilla.javascript.xml.XMLLib;
 import org.mozilla.javascript.xml.XMLObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import javax.xml.namespace.QName;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.io.ObjectInputStream;
 import java.io.ByteArrayInputStream;
 
 /**
  * @author Matthieu Riou <mriou@apache.org>
  */
 public class E4XExprRuntime implements ExpressionLanguageRuntime {
 
     private static ConcurrentHashMap<String, Scriptable> globalStateCache = new ConcurrentHashMap<String, Scriptable>();
 
     public void initialize(Map map) throws ConfigurationException {
     }
 
     public String evaluateAsString(OExpression oexpr, EvaluationContext evaluationContext) throws FaultException {
         Context cx = ContextFactory.getGlobal().enterContext();
         cx.setOptimizationLevel(-1);
 
         Scriptable parentScope = getScope(cx, oexpr);
         ODEDelegator scope = new ODEDelegator(parentScope, evaluationContext, (SimPELExpr)oexpr, cx);
 
         // First evaluating the assignment
         SimPELExpr expr = (SimPELExpr) oexpr;
         Object res = cx.evaluateString(scope, expr.getExpr(), "<expr>", 0, null);
         if (res instanceof String) return (String) res;
        else throw new FaultException(new QName("e4xEvalFailure"), "Failed to evaluate "
                + expr.getExpr() + " as a string value");
     }
 
     public boolean evaluateAsBoolean(OExpression oexpr, EvaluationContext evaluationContext) throws FaultException {
         // TODO context caching
         Context cx = ContextFactory.getGlobal().enterContext();
         cx.setOptimizationLevel(-1);
 
         Scriptable parentScope = getScope(cx, oexpr);
         ODEDelegator scope = new ODEDelegator(parentScope, evaluationContext, (SimPELExpr)oexpr, cx);
 
         // First evaluating the assignment
         SimPELExpr expr = (SimPELExpr) oexpr;
         Object res = cx.evaluateString(scope, expr.getExpr(), "<expr>", 0, null);
         if (res instanceof Boolean) return (Boolean)res;
         else throw new FaultException(new QName("e4xEvalFailure"), "Failed to evaluate "
                 + expr.getExpr() + " as a boolean value");
     }
 
     public Number evaluateAsNumber(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         return null;
     }
 
     public List evaluate(OExpression oexpr, EvaluationContext evaluationContext) throws FaultException {
         // TODO context caching
         Context cx = ContextFactory.getGlobal().enterContext();
         cx.setOptimizationLevel(-1);
 
         Scriptable parentScope = getScope(cx, oexpr);
         ODEDelegator scope = new ODEDelegator(parentScope, evaluationContext, (SimPELExpr)oexpr, cx);
 
         // First evaluating the assignment
         SimPELExpr expr = (SimPELExpr) oexpr;
         String forged = expr.getExpr();
         if (expr.getLValue() != null)
             forged = expr.getLValue() + " = " + expr.getExpr();
 
         Object res = cx.evaluateString(scope, forged, "<expr>", 0, null);
         // Second extracting the resulting variable value
         if (expr.getLValue() != null) {
             if (scope.getEnv().get(expr.getLVariable()) != null) {
                 res = scope.getEnv().get(expr.getLVariable());
             } else {
                 scope.forceDelegate = true;
                 res = cx.evaluateString(scope, expr.getLVariable(), "<expr>", 0, null);
             }
             OVarType varType = expr.getReferencedVariable(expr.getLVariable()).type;
             // Setting variables runtime type
             if (res instanceof String) varType.underlyingType = OVarType.STRING_TYPE;
             if (res instanceof Number) varType.underlyingType = OVarType.NUMBER_TYPE;
         }
 
         ArrayList<Node> resList = new ArrayList<Node>(1);
         if (res instanceof String || res instanceof Number) {
             Document doc = DOMUtils.newDocument();
             resList.add(doc.createTextNode(res.toString()));
         } else if (res instanceof XMLObject) {
             try {
                 // Only content is copied, need to wrap
                 Document doc = DOMUtils.newDocument();
                 Element wrapper = doc.createElement("assignWrapper");
                 Node resNode = doc.importNode(XMLLibImpl.toDomNode(res), true);
                 wrapper.appendChild(resNode);
                 if (resNode.getNodeType() == Node.ELEMENT_NODE) mergeHeaders((Element) resNode);
                 resList.add(wrapper);
             } catch (IllegalArgumentException e) {
                 // Rhino makes it pretty hard to use it sXML impl, XML and XMLList are package level
                 // classes so I can't test on them but toDomNode doesn't accept XMLList
                 Document doc = DOMUtils.newDocument();
                 resList.add(doc.createTextNode(res.toString()));
             }
         } else if (res instanceof Node) resList.add((Node) res);
         return resList;
     }
 
     public Calendar evaluateAsDate(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         return null;
     }
 
     public Duration evaluateAsDuration(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         String literal = this.evaluateAsString(oExpression, evaluationContext);
         try {
             return new Duration(literal);
         } catch (Exception ex) {
             String errmsg = "Invalid duration: " + literal;
             throw new FaultException(oExpression.getOwner().constants.qnInvalidExpressionValue,errmsg);
         }
     }
 
     public Node evaluateNode(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         return (Node) evaluate(oExpression, evaluationContext).get(0);
     }
 
     private class ODEDelegator extends Delegator  {
         private EvaluationContext _evaluationContext;
         private XMLLib _xmlLib;
         private SimPELExpr _expr;
         private Context _cx;
         private HashMap<String,Object> _env = new HashMap<String,Object>();
         private Scriptable _parentScope;
         public boolean forceDelegate = false;
 
         private ODEDelegator(Scriptable obj, EvaluationContext evaluationContext, SimPELExpr expr, Context cx) {
             super(obj);
             _evaluationContext = evaluationContext;
             _expr = expr;
             _cx = cx;
             _parentScope = obj;
         }
 
         public void setXmlLib(XMLLib _xmlLib) {
             this._xmlLib = _xmlLib;
         }
 
         public Object get(String name, Scriptable start) {
             try {
                 OScope.Variable v = _expr.getReferencedVariable(name);
                 if (v == null || forceDelegate) return super.get(name, start);
 
                 if (_env.get(name) != null) return _env.get(name);
 
                 Node node;
                 try {
                     // This assumes message type with a single part
                     if (v.type instanceof OMessageVarType)
                         node = _evaluationContext.readVariable(v,((OMessageVarType)v.type).parts.values().iterator().next());
                     else
                         node = _evaluationContext.readVariable(v, null);
                 } catch (FaultException e) {
                     if (e.getQName().getLocalPart().equals("uninitializedVariable")) return super.get(name, start);
                     else throw e;
                 }
                 // Simple types
                 // TODO I think the sumple type case never exists anymore (stuff get wrapped), remove this
                 if (node.getNodeValue() != null) {
                     String rawValue = node.getNodeValue();
                     if (v.type.underlyingType == OVarType.SCHEMA_TYPE || v.type.underlyingType == OVarType.STRING_TYPE)
                         return rawValue;
                     if (v.type.underlyingType == OVarType.NUMBER_TYPE) return Double.valueOf(rawValue);
                 } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element nodeElmt = (Element) node;
                     if (DOMUtils.getFirstChildElement(nodeElmt) == null) {
                         String rawValue = nodeElmt.getTextContent();
                         if (v.type.underlyingType == OVarType.NUMBER_TYPE) return Double.valueOf(rawValue);
                         else return rawValue;
                     }
                    else node = DOMUtils.getFirstChildElement((Element)node);
                 }
 
                 // When we're querying on headers, the sub-element is supposed to be right under the
                 // current. To avoid pollution of the main user variable we store it one level up so
                 // we're readjusting here.
                 if (!forceDelegate && (_expr.getExpr().indexOf(".headers") > 0
                         || (_expr.getLValue() != null && _expr.getLValue().indexOf(".headers") > 0)) 
                         && node.getParentNode() != null
                         && node.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
                     Element parent = (Element) node.getParentNode();
                     Element headers = DOMUtils.findChildByName(parent, new QName(null, "headers"));
                     if (headers != null)
                         node.appendChild(node.getOwnerDocument().importNode(headers, true));
                 }
 
                 // Have to remove the xml header otherwise it confuses Rhino
                 String[] xmlArr = DOMUtils.domToString(node).split("\n");
                 // Going back to the evaluation loop to get a Rhino XML object, their XML API just doesn't have any
                 // public method allowing an XML construction.
                 String[] newXmlArr = new String[xmlArr.length - 1];
                 System.arraycopy(xmlArr, 1, newXmlArr, 0, xmlArr.length - 1);
                 Object xmlObj = _cx.evaluateString(start, join(newXmlArr) , "<expr>", 0, null);
                 _env.put(name, xmlObj);
                 return xmlObj;
             } catch (Exception e) {
                 throw new RuntimeException("Error accessing variable " + name + ".", e);
             }
         }
 
         public boolean has(String name, Scriptable start) {
             OScope.Variable v = _expr.getReferencedVariable(name);
             if (v == null) return super.has(name, start);
 
             Node node;
             try {
                 if (v.type instanceof OMessageVarType)
                     node = _evaluationContext.readVariable(v,((OMessageVarType)v.type).parts.values().iterator().next());
                 else
                     node = _evaluationContext.readVariable(v, null);
             } catch (FaultException e) {
                 return false;
             }
             return node != null;
         }
 
         public void put(String name, Scriptable start, Object value) {
             _env.put(name, value);
         }
         
         public HashMap<String, Object> getEnv() {
             return _env;
         }
 
         public Scriptable getObj() {
             return obj;
         }
 
         @Override
         public Scriptable getParentScope() {
             return _parentScope;
         }
     }
 
     public Scriptable getScope(Context cx, OExpression oexpr) {
         Scriptable parentScope;
         if (oexpr.getOwner().globalState != null) {
             parentScope = globalStateCache.get(oexpr.getOwner().getGuid());
             if (parentScope == null) {
                 Scriptable sharedScope = cx.initStandardObjects();
                 try {
                     ObjectInputStream in = new ScriptableInputStream(new ByteArrayInputStream(oexpr.getOwner().globalState), sharedScope);
                     parentScope = (Scriptable) in.readObject();
                     in.close();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 globalStateCache.put(oexpr.getOwner().getGuid(), parentScope);
             }
         } else {
             parentScope = cx.initStandardObjects();
         }
         return parentScope;
     }
 
     // Can someone tell me why I have to implement this? The Java API just sucks.
     public static String join(String[] ss) {
         StringBuffer buffer = new StringBuffer();
         for (String s : ss) buffer.append(s);
         return buffer.toString();
     }
 
     private void mergeHeaders(Element elmt) {
         // As a convenience during E4X assignment, headers is a subnode of the main node. To avoid pollution
         // it's actually stored as a subnode of the parent so we move/merge here.
         NodeList elmtHeadersNL = elmt.getElementsByTagName("headers");
         if (elmtHeadersNL.getLength() > 0) {
             Element elmtHeaders = (Element) elmtHeadersNL.item(0);
             Element parent = (Element) elmt.getParentNode();
             elmt.removeChild(elmtHeaders);
             Element parentHeaders = DOMUtils.getElementByID(parent, "headers");
             if (parentHeaders == null) {
                 parent.appendChild(elmtHeaders);
             } else {
                 NodeList headerChildren = elmtHeaders.getChildNodes();
                 for (int m = 0; m < headerChildren.getLength(); m++) {
                     Node n = headerChildren.item(m);
                     if (n.getNodeType() == Node.ELEMENT_NODE) {
                         n.getParentNode().removeChild(n);
                         parentHeaders.appendChild(n);
                     }
                 }
             }
         }
     }
     
 }
