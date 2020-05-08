 // Copyright (C) 2012, The SAVI Project.
 package ca.savi.camel.service;
 
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 
 import ca.savi.camel.model.GenericOperationRequest;
 import ca.savi.camel.model.GenericOperationResponse;
 import ca.savi.camel.model.GetImageRequest;
 import ca.savi.camel.model.GetImageResponse;
 import ca.savi.camel.model.GetParamRequest;
 import ca.savi.camel.model.GetParamResponse;
 import ca.savi.camel.model.GetRequest;
 import ca.savi.camel.model.GetResponse;
 import ca.savi.camel.model.InitRequest;
 import ca.savi.camel.model.InitResponse;
 import ca.savi.camel.model.ListRequest;
 import ca.savi.camel.model.ListResponse;
 import ca.savi.camel.model.ProgramRequest;
 import ca.savi.camel.model.ProgramResponse;
 import ca.savi.camel.model.RebootRequest;
 import ca.savi.camel.model.RebootResponse;
 import ca.savi.camel.model.ReleaseRequest;
 import ca.savi.camel.model.ReleaseResponse;
 import ca.savi.camel.model.ResetRequest;
 import ca.savi.camel.model.ResetResponse;
 import ca.savi.camel.model.SaveImageRequest;
 import ca.savi.camel.model.SaveImageResponse;
 import ca.savi.camel.model.SetParamRequest;
 import ca.savi.camel.model.SetParamResponse;
 import ca.savi.camel.model.StatusRequest;
 import ca.savi.camel.model.StatusResponse;
 import ca.savi.camel.model.TerminateRequest;
 import ca.savi.camel.model.TerminateResponse;
 
 /**
  * node resource port.
  * @author Eliot J. Kang <eliot@savinetwork.ca>
  * @version 0.1
  */
 @WebService(
     name = "NodeResourceService",
     targetNamespace = "http://camel.savi.ca/wsdl/NodeResource")
 @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
 public interface NodeResourcePortType {
   /**
    * Get a resource.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.GetResponse
    */
   @WebMethod(operationName = "NodeResourceGet")
   @WebResult(name = "getResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public GetResponse nodeResourceGet(@WebParam(name = "getRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") GetRequest inputPart);
 
   /**
    * Release a resource.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.ReleaseResponse
    */
   @WebMethod(operationName = "NodeResourceRelease")
   @WebResult(name = "releaseResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public ReleaseResponse nodeResourceRelease(@WebParam(name = "releaseRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") ReleaseRequest inputPart);
 
   /**
    * Get a status of a resource.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.StatusResponse
    */
   @WebMethod(operationName = "NodeResourceStatus")
   @WebResult(name = "statusResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public StatusResponse nodeResourceStatus(@WebParam(name = "statusRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") StatusRequest inputPart);
 
   /**
    * Program a resource.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.ProgramResponse
    */
   @WebMethod(operationName = "NodeResourceProgram")
   @WebResult(name = "programResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public ProgramResponse nodeResourceProgram(@WebParam(name = "programRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") ProgramRequest inputPart);
 
   /**
    * Initialize a resource.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.InitResponse
    */
   @WebMethod(operationName = "NodeResourceInit")
   @WebResult(name = "initResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public InitResponse nodeResourceInit(@WebParam(name = "initRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") InitRequest inputPart);
 
   /**
    * Save an image
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.SaveImageResponse
    */
   @WebMethod(operationName = "NodeResourceSaveImage")
   @WebResult(name = "saveImageResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public SaveImageResponse nodeResourceSaveImage(@WebParam(
       name = "saveImageRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") SaveImageRequest inputPart);
 
   /**
    * Get an image of a resource.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.GetImageResponse
    */
   @WebMethod(operationName = "NodeResourceGetImage")
   @WebResult(name = "getImageResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public GetImageResponse nodeResourceGetImage(@WebParam(
       name = "getImageRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") GetImageRequest inputPart);
 
   /**
    * Reset a resource.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.ResetResponse
    */
   @WebMethod(operationName = "NodeResourceReset")
   @WebResult(name = "resetResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public ResetResponse nodeResourceReset(@WebParam(name = "resetRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") ResetRequest inputPart);
 
   /**
    * Set a parameter.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.SetParamResponse
    */
   @WebMethod(operationName = "NodeResourceSetParam")
   @WebResult(name = "setParamResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public SetParamResponse nodeResourceSetParam(@WebParam(
       name = "setParamRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") SetParamRequest inputPart);
 
   /**
    * Get a parameter.
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.GetParamResponse
    */
   @WebMethod(operationName = "NodeResourceGetParam")
   @WebResult(name = "getParamResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public GetParamResponse nodeResourceGetParam(@WebParam(
       name = "getParamRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") GetParamRequest inputPart);
 
   /**
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.RebootResponse
    */
   @WebMethod(operationName = "NodeResourceReboot")
   @WebResult(name = "rebootResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public RebootResponse nodeResourceReboot(@WebParam(name = "rebootRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") RebootRequest inputPart);
 
   /**
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.TerminateResponse
    */
   @WebMethod(operationName = "NodeResourceTerminate")
   @WebResult(name = "terminateResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public TerminateResponse nodeResourceTerminate(@WebParam(
       name = "terminateRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") TerminateRequest inputPart);
 
   /**
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.ListResponse
    */
   @WebMethod(operationName = "NodeResourceList")
   @WebResult(name = "listResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public ListResponse nodeResourceList(@WebParam(name = "listRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") ListRequest inputPart);
 
   /**
    * @param inputPart
    * @return returns ca.savi.ws.resourcemanager.GenericOperationResponse
    */
   @WebMethod(operationName = "NodeResourceGenericOperation")
   @WebResult(name = "genericOperationResponse",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "OutputPart")
   public GenericOperationResponse nodeResourceGenericOperation(@WebParam(
       name = "genericOperationRequest",
       targetNamespace = "http://camel.savi.ca/model/NodeResourceSchema",
       partName = "InputPart") GenericOperationRequest inputPart);
 }
