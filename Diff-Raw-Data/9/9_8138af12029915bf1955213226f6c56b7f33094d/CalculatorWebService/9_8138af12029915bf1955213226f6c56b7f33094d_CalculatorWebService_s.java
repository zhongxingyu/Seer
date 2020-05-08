 package org.ankarajug.testinfected.service.ws;
 
 import org.ankarajug.testinfected.domain.CalculationResult;
 
 import javax.jws.WebParam;
 import javax.jws.WebService;
 
 /**
  * User: mertcaliskan
  * Date: 2/9/13
  */
 @WebService(name = "calculatorWS")
 public interface CalculatorWebService {
 
     CalculationResult add(@WebParam(name = "param1") Integer a, @WebParam(name = "param2") Integer b);
 
     CalculationResult subtract(@WebParam(name = "param1") Integer a, @WebParam(name = "param2") Integer b);
 
     CalculationResult multiply(@WebParam(name = "param1") Integer a, @WebParam(name = "param2") Integer b);
 
     CalculationResult divide(@WebParam(name = "param1") Integer a, @WebParam(name = "param2") Integer b);
 }
