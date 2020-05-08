 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.resourcemanager.remote;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 
 import org.w3c.dom.Document;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.controller.ControlElement;
 import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
 import com.delcyon.capo.protocol.server.AbstractClientRequestProcessor;
 import com.delcyon.capo.protocol.server.ClientRequest;
 import com.delcyon.capo.protocol.server.ClientRequestProcessor;
 import com.delcyon.capo.protocol.server.ClientRequestProcessorSessionManager;
 import com.delcyon.capo.protocol.server.ClientRequestXMLProcessor;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceType;
 import com.delcyon.capo.resourcemanager.ResourceURI;
 import com.delcyon.capo.resourcemanager.remote.RemoteResourceDescriptorMessage.MessageType;
 import com.delcyon.capo.resourcemanager.types.ContentMetaData;
 import com.delcyon.capo.util.XMLSerializer;
 import com.delcyon.capo.xml.XPath;
 import com.delcyon.capo.xml.cdom.CDocument;
 import com.delcyon.capo.xml.cdom.CElement;
 import com.delcyon.capo.xml.cdom.VariableContainer;
 import com.delcyon.capo.xml.dom.ResourceDeclarationElement;
 
 /**
  * @author jeremiah
  *
  */
 public class RemoteResourceDescriptorProxy extends AbstractClientRequestProcessor implements ResourceDescriptor,ClientRequestProcessor
 {
 
 	private ControllerClientRequestProcessor controllerClientRequestProcessor;
	private String lock = new String("lock");
	private String inputStreamLock = new String("inputStreamLock");
	private String outputStreamLock = new String("outputStreamLock");
 	private String sessionID = ClientRequestProcessorSessionManager.generateSessionID();
 	private VariableContainer variableContainer;
 	private InputStream inputStream;
 	private OutputStream outputStream;
 	private ResourceURI resourceURI = null;
 	private ResourceType resourceType;
 	private LifeCycle lifeCycle;
     private boolean isStreamProcessor = false;
 	
 	public RemoteResourceDescriptorProxy(ControllerClientRequestProcessor controllerClientRequestProcessor)
 	{
 		this.controllerClientRequestProcessor = controllerClientRequestProcessor;
 		ClientRequestProcessorSessionManager.registerClientRequestProcessor(this,sessionID);
 	}
 	
 	@Override
 	public void setup(ResourceType resourceType, String resourceURI) throws Exception
 	{
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		this.resourceURI = new ResourceURI(resourceURI);
 		this.resourceType = resourceType;
 		message.setMessageType(MessageType.SETUP);
 		message.setSessionID(sessionID);
 		message.setResourceURI(new ResourceURI(resourceURI));		
 		message = sendResponse(message, MessageType.SETUP,false);
 		this.resourceURI = message.getResourceURI();
 		this.resourceType = message.getResourceType();
 	}
 	
 	@Override
 	public void init(ResourceDeclarationElement declaringResourceElement, VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
 	{
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		this.variableContainer = variableContainer;
 		message.setIterate(iterate);
 		message.setLifeCycle(lifeCycle);
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.INIT,false);			
 	}
 	
 	@Override
 	public State getResourceState() throws Exception
 	{
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
 		message = sendResponse(message, MessageType.GET_RESOURCE_STATE,false);
 		return message.getResourceState();
 	}
 	
 	@Override
 	public void open(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.OPEN,false);		
 	}
 	
 	@Override
 	public ContentMetaData getResourceMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.GET_RESOURCE_METADATA,false);
 		return message.getResourceMetaData();
 	}
 	
 	@Override
 	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.GET_CONTENT_METADATA,false);
 		return message.getContentMetaData();
 	}
 	
 	@Override
     public ContentMetaData getOutputMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
     {
         this.variableContainer = variableContainer;
         RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
         message.setResourceParameters(resourceParameters);      
         message = sendResponse(message, MessageType.GET_OUTPUT_METADATA,false);
         return message.getOutputMetaData();
     }
 	
 	@Override
 	public ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception
 	{
 	    ContentMetaData contentMetaData = getResourceMetaData(null);
         if ( contentMetaData.isContainer() == true)
         {
             List<ContentMetaData> childContentMetaDataList = getResourceMetaData(null).getContainedResources();
             for (ContentMetaData childContentMetaData : childContentMetaDataList)
             {
                 if(childContentMetaData.getResourceURI().getPath().endsWith(relativeURI))
                 {
                     return CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, "remote:"+childContentMetaData.getResourceURI().getResourceURIString());
                 }
             }
             return CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, "remote:"+getResourceURI().getResourceURIString()+"/"+relativeURI);
         }
         else
         {
             return null;
         }
 	}
 	
 	@Override
 	public InputStream getInputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		//unlock any existing threads
 		synchronized (inputStreamLock)
 		{
 			inputStreamLock.notify();
 		}
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);
 		
 		message = sendResponse(message, MessageType.GET_INPUTSTREAM,true);
 		return this.inputStream;
 	}
 	@Override
 	public OutputStream getOutputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		//unlock any existing threads
 		synchronized (outputStreamLock)
 		{
 			outputStreamLock.notify();
 		}
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);
 		
 		message = sendResponse(message, MessageType.GET_OUTPUTSTREAM,true);
 		return this.outputStream;
 	}
 
 	@Override
 	public void addResourceParameters(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.ADD_RESOURCE_PARAMETERS,false);
 		
 	}
 	
 	@Override
 	public boolean isSupportedStreamFormat(StreamType streamType, StreamFormat streamFormat) throws Exception
 	{		
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setStreamType(streamType);
 		message.setStreamFormat(streamFormat);
 		message = sendResponse(message, MessageType.IS_STREAM_SUPPORETED_FORMAT,false);
 		return message.isSupportedStreamFormat();
 	}
 	
 	@Override
 	public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.CLOSE,false);
 		
 		synchronized (inputStreamLock)
 		{
 			this.inputStreamLock.notify();	
 		}
 		synchronized (outputStreamLock)
 		{
 			this.outputStreamLock.notify();	
 		}
 	}
 	
 	@Override
 	public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.RELEASE,false);
 		synchronized (inputStreamLock)
 		{
 			this.inputStreamLock.notify();	
 		}
 		synchronized (outputStreamLock)
 		{
 			this.outputStreamLock.notify();	
 		}
 		ClientRequestProcessorSessionManager.removeClientRequestProcessor(getSessionId());
 	}
 	
 	@Override
 	public void reset(State previousState) throws Exception
 	{		
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setPreviousState(previousState);		
 		message = sendResponse(message, MessageType.RESET,false);		
 	}
 	
 	@Override
 	public void advanceState(State desiredState, VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 	    this.variableContainer = variableContainer;
         RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
         message.setResourceParameters(resourceParameters);
         message.setDesiredState(desiredState);
         message = sendResponse(message, MessageType.ADVANCE_STATE,false);
 	    
 	}
 	
 	@Override
 	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.STEP,false);
 		return message.isStepSuccess();
 	}
 	
 	@Override
 	public CElement readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.READ_XML,false);
 		return message.getXMLElement();
 	}
 	
 	@Override
 	public void writeXML(VariableContainer variableContainer, CElement element, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);
 		message.setXMLElement(element);
 		message = sendResponse(message, MessageType.WRITE_XML,false);
 	}
 	
 	@Override
 	public byte[] readBlock(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.READ_BLOCK,false);
 		return message.getBlock();
 	}
 
 	@Override
 	public void writeBlock(VariableContainer variableContainer, byte[] block, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);
 		message.setBlock(block);
 		message = sendResponse(message, MessageType.WRITE_BLOCK,false);
 		
 	}
 	
 	@Override
 	public void processInput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.PROCESS_INPUT,false);		
 	}
 
 	@Override
 	public void processOutput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);		
 		message = sendResponse(message, MessageType.PROCESS_OUTPUT,false);		
 	}
 	
 	@Override
 	public boolean performAction(VariableContainer variableContainer, Action action, ResourceParameter... resourceParameters) throws Exception
 	{
 		this.variableContainer = variableContainer;
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message.setResourceParameters(resourceParameters);
 		message.setAction(action);
 		message = sendResponse(message, MessageType.PERFORM_ACTION,false);
 		return message.getActionResult();
 	}
 
 	
 	@Override
 	public LifeCycle getLifeCycle() throws Exception
 	{
 		if (this.lifeCycle == null)
 		{
 			RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
 			message = sendResponse(message, MessageType.GET_LIFCYCLE,false);
 			this.lifeCycle = message.getLifeCycle();
 		}
 		return this.lifeCycle;
 	}
 	
 	@Override
 	public State getStreamState(StreamType streamType) throws Exception
 	{		
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
 		message.setStreamType(streamType);
 		message = sendResponse(message, MessageType.GET_STREAM_STATE,false);
 		return message.getStreamState();
 	}
 
 	@Override
 	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
 	{		
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
 		message.setStreamType(streamType);
 		message = sendResponse(message, MessageType.GET_SUPPORTED_STREAM_FORMATS,false);
 		return message.getSupportedStreamFormats();
 	}
 
 	@Override
 	public StreamType[] getSupportedStreamTypes() throws Exception
 	{
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
 		message = sendResponse(message, MessageType.GET_SUPPORTED_STREAM_TYPES,false);
 		return message.getSupportedStreamTypes();
 	}
 
 	@Override
 	public boolean isSupportedAction(Action action) throws Exception
 	{		
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
 		message.setAction(action);
 		message = sendResponse(message, MessageType.IS_SUPPORTED_ACTION,false);
 		return message.getActionResult();
 	}
 
 	@Override
 	public boolean isSupportedStreamType(StreamType streamType) throws Exception
 	{		
 		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
 		message.setStreamType(streamType);
 		message = sendResponse(message, MessageType.IS_SUPPORTED_STREAM_TYPE,false);
 		return message.isSupportedStreamType();
 	}
 	
 	//===================CACHED RESULTS======================
 	@Override
 	public ResourceType getResourceType()
 	{
 		return this.resourceType;
 	}
 
 	@Override
 	public ResourceURI getResourceURI()
 	{
 		return this.resourceURI;
 	}
 
 	
 	public void setResourceURI(ResourceURI resourceURI)
 	{
 		this.resourceURI = resourceURI;
 		
 	}
 
 	/** returns last piece of URI **/
     @Override
     public String getLocalName()
     {
         String[] splitURI = this.resourceURI.getPath().split("/");
         return splitURI[splitURI.length-1];
     }
 	
 	@Override
 	public boolean isRemoteResource()
 	{	
 		return true;
 	}
 	
 	//===================MESSAGING===========================
 	
 	private RemoteResourceDescriptorMessage sendResponse(RemoteResourceDescriptorMessage message, MessageType messageType,boolean needLock) throws Exception
 	{
 	    return sendResponse(message, messageType, needLock, this.controllerClientRequestProcessor.getClientRequestXMLProcessor());
 	}
 	
 	private RemoteResourceDescriptorMessage sendResponse(RemoteResourceDescriptorMessage message, MessageType messageType,boolean needLock,ClientRequestXMLProcessor clientRequestXMLProcessor) throws Exception
 	{
 		message.setSessionID(sessionID);
 		message.setMessageType(messageType);
 		if(message.getResourceURI() == null)
 		{
 		    message.setResourceURI(getResourceURI());
 		}
 		message.prepareResponse();
 		
 		clientRequestXMLProcessor.writeResponse(message);
 		if (needLock)
 		{
 			synchronized (lock)
 			{			    
 				lock.wait(30000);	
 			}			
 		}
 		//wait for a message from the client
 		Document replyDocument = XPath.unwrapDocument(clientRequestXMLProcessor.readNextDocument(),true);
 		
 		message = new RemoteResourceDescriptorMessage(replyDocument);
 		if (message.getMessageType() == MessageType.FAILURE)
 		{
 			if (message.getException() != null)
 			{
 				throw message.getException();
 			}
 		}
 		return message;
 	}
 	
 	@Override
 	public String getSessionId()
 	{
 		return this.sessionID;
 	}
 	
 	@Override
 	public void process(ClientRequest clientRequest) throws Exception
 	{		
 		MessageType messageType = RemoteResourceRequest.getType(clientRequest);
 		
 		//process the request
 		boolean useLock = false;
 		switch (messageType)
 		{
 			case GET_INPUTSTREAM:
 				this.inputStream = clientRequest.getInputStream();
 				useLock = true;
 				break;
 			case GET_OUTPUTSTREAM:				
 				this.outputStream = clientRequest.getOutputStream();
 				useLock = true;
 				break;
 			case GET_VAR_VALUE:
 			    processVarRequest(clientRequest);
 				break;
 			default:
 				throw new UnsupportedOperationException(messageType.toString());
 		}
 		//notify the other thread the the request has been processed
 		if (useLock == true)
 		{
 			synchronized (lock)
 			{
 				lock.notify();			
 			}
 		}
 		//see if we need to wait until a lock is released, so any streams don't get closed
 		switch (messageType)
 		{
 			case GET_INPUTSTREAM:
 				synchronized (inputStreamLock)
 				{
 					this.inputStreamLock.wait();	
 				}				
 				break;
 			case GET_OUTPUTSTREAM:
 				synchronized (outputStreamLock)
 				{				    
 					this.outputStreamLock.wait();	
 				}				
 				break;
             default:
                 break;
 
 		}
 			
 	}
 
 	private void processVarRequest(ClientRequest clientRequest) throws Exception
     {
 	    RemoteResourceDescriptorMessage requestMessage = new RemoteResourceDescriptorMessage(XPath.unwrapDocument(XPath.unwrapDocument(clientRequest.getRequestDocument(), true),true));	    
 
 	    String varName = requestMessage.getVarName();
 	    
 	    RemoteResourceDescriptorMessage replyMessage = new RemoteResourceDescriptorMessage();
 	    replyMessage.setVarName(varName);
 	    if (variableContainer != null && varName != null)
 	    {
 	        replyMessage.setValue(variableContainer.getVarValue(varName));	           
 	    }
 	    replyMessage.setSessionID(sessionID);
 	    replyMessage.setMessageType(MessageType.GET_VAR_VALUE);
         if(replyMessage.getResourceURI() == null)
         {
             replyMessage.setResourceURI(getResourceURI());
         }
         
         CDocument replyDocument = CDocument.buildPath("RemoteResourceVarRequestReply");
         XMLSerializer.export(replyDocument.getDocumentElement(), replyMessage);        
 	    clientRequest.getClientRequestXMLProcessor().getXmlStreamProcessor().writeDocument(replyDocument);
 
     }
 
     @Override
 	public void init(ClientRequestXMLProcessor clientRequestXMLProcessor, String sessionID, HashMap<String, String> sessionHashMap,String requestName) throws Exception
 	{
         RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage(clientRequestXMLProcessor.getDocument());        
         switch (message.getMessageType())
         {
             case GET_INPUTSTREAM:
             case GET_OUTPUTSTREAM:
                 isStreamProcessor  = true;
                 break;
             default:
                 isStreamProcessor  = false;
                 break;
         }
 	}
 
     @Override
     public boolean isStreamProcessor()
     {
         return isStreamProcessor;
     }
     
 	@Override
 	public Document readNextDocument() throws Exception
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	
 }
