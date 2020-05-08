 package com.sergio.ws;
 
 //PARA FICHEROS
 //import javax.activation.DataHandler;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebParam.Mode;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 
 //PARA FICHEROS
 //@javax.xml.ws.soap.MTOM
@WebService(serviceName = "MockupWS")
 public interface MockupWS {
 
 	@WebMethod(operationName = "ejecutarMetodoWS")
 	@WebResult(name = "resultadoOperacion")
 	public String ejecutarMetodoWS(
 			@WebParam(name = "sessionGuid", mode = Mode.IN) String sessionGuid,
 			@WebParam(name = "parametro01", mode = Mode.IN) String parametro01,
 			@WebParam(name = "parametro02", mode = Mode.IN) String parametro02);
 }
