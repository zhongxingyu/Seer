 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.swip.pivotToMappings;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import javax.jws.WebService;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.xml.bind.annotation.XmlSeeAlso;
 import org.swip.pivotToMappings.controller.Controller;
 import org.swip.pivotToMappings.model.AbstractClass;
 import org.swip.pivotToMappings.model.Class1;
 import org.swip.pivotToMappings.model.Class2;
 import org.swip.pivotToMappings.model.ClassToBeReturned;
 import org.swip.pivotToMappings.model.NestedClassToBeReturned;
 import org.swip.pivotToMappings.model.patterns.mapping.KbElementMapping;
 import org.swip.pivotToMappings.model.patterns.mapping.LiteralElementMapping;
 import org.swip.pivotToMappings.model.patterns.patternElement.ClassPatternElement;
 import org.swip.pivotToMappings.model.patterns.patternElement.LiteralPatternElement;
 import org.swip.pivotToMappings.model.patterns.patternElement.PropertyPatternElement;
 import org.swip.pivotToMappings.model.patterns.subpattern.PatternTriple;
 import org.swip.pivotToMappings.model.patterns.subpattern.SubpatternCollection;
 import org.swip.pivotToMappings.model.query.queryElement.Keyword;
 import org.swip.pivotToMappings.model.query.queryElement.Literal;
 
 /**
  *
  * @author camille
  */
 @WebService(serviceName = "PivotToMappingsWS")
 @XmlSeeAlso({
     Class1.class, Class2.class,
     PropertyPatternElement.class, ClassPatternElement.class, LiteralPatternElement.class,
     PatternTriple.class, SubpatternCollection.class,
     KbElementMapping.class, LiteralElementMapping.class,
     Keyword.class, Literal.class})
 public class PivotToMappingsWS {
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "operation1")
     public ClassToBeReturned operation1(@WebParam(name = "stringParam") String stringParam) {
         ClassToBeReturned result = new ClassToBeReturned();
         result.setString1(stringParam);
         result.setString2(stringParam + "2");
         result.setInt1(1);
         // list
         List<String> strings = new LinkedList<String>();
         for (int i = 1; i <= 10; i++) {
             strings.add(stringParam + "_list_" + i);
         }
         result.setStrings(strings);
         // nested object
         NestedClassToBeReturned nestedObject = new NestedClassToBeReturned();
         nestedObject.setString3(stringParam + "3");
         nestedObject.setInt2(2);
         result.setNestedObject(nestedObject);
         return result;
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "operation2")
     public List<ClassToBeReturned> operation2(@WebParam(name = "parameter") String parameter) {
         List<ClassToBeReturned> resultList = new LinkedList<ClassToBeReturned>();
         for (int j = 101; j <= 105; j++) {
             ClassToBeReturned result = new ClassToBeReturned();
             result.setString1(j + "_" + parameter);
             result.setString2(j + "_" + parameter + "2");
             result.setInt1(j + 1);
             // list
             List<String> strings = new LinkedList<String>();
             for (int i = 1; i <= 3; i++) {
                 strings.add(j + "_" + parameter + "_list_" + i);
             }
             result.setStrings(strings);
             // nested object
             NestedClassToBeReturned nestedObject = new NestedClassToBeReturned();
             nestedObject.setString3(j + "_" + parameter + "3");
             nestedObject.setInt2(j + 2);
             result.setNestedObject(nestedObject);
             resultList.add(result);
         }
         return resultList;
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "operation3")
     public AbstractClass operation3(@WebParam(name = "whichClass") int whichClass, @WebParam(name = "intValue") int intValue) {
         if (whichClass == 1) {
             return new Class1(intValue, intValue + 10);
         } else if (whichClass == 2) {
             return new Class2(intValue, intValue + 20);
         } else {
             return null;
         }
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getPatterns")
     public java.util.List<org.swip.pivotToMappings.model.patterns.Pattern> getPatterns() {
         return Controller.getInstance().getPatterns();
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "generateBestMappings")
     public java.util.List<org.swip.pivotToMappings.model.patterns.mapping.PatternToQueryMapping> generateBestMappings(@WebParam(name = "pivotQueryString") String pivotQueryString, @WebParam(name = "numMappings") int numMappings) {
         List ret = Controller.getInstance().getBestMappings(pivotQueryString, numMappings);
        Collections.reverse(ret);
        return ret;
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getMatchings")
     public java.util.List<org.swip.pivotToMappings.model.patterns.mapping.PatternToQueryMapping> getMatchings(@WebParam(name = "queryString") String pivotQueryString) {
         return null;
     }
 }
