 package org.ankarajug.testinfected.service.ws;
 
 import org.ankarajug.testinfected.domain.CalculationResult;
 
import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 
 /**
  * User: mertcaliskan
  * Date: 2/9/13
  */
 @WebService(name = "calculatorWS")
 public interface CalculatorWebService {
 
    @WebMethod(action = "add")
     CalculationResult add(@WebParam(name = "param1") Integer a, @WebParam(name = "param2") Integer b);
 
    @WebMethod(action = "substract")
     CalculationResult subtract(@WebParam(name = "param1") Integer a, @WebParam(name = "param2") Integer b);
 
    @WebMethod(action = "multiply")
     CalculationResult multiply(@WebParam(name = "param1") Integer a, @WebParam(name = "param2") Integer b);
 
    @WebMethod(action = "divide     ")
     CalculationResult divide(@WebParam(name = "param1") Integer a, @WebParam(name = "param2") Integer b);
 }
