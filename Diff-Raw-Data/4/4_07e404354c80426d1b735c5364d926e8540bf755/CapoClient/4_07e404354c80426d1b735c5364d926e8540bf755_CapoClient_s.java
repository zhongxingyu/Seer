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
 package com.delcyon.capo.client;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.SocketException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.KeyStore;
 import java.security.KeyStore.PrivateKeyEntry;
 import java.security.KeyStore.TrustedCertificateEntry;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateFactory;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManagerFactory;
 import javax.xml.bind.DatatypeConverter;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.tanukisoftware.wrapper.WrapperManager;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.Configuration;
 import com.delcyon.capo.Configuration.PREFERENCE;
 import com.delcyon.capo.controller.LocalRequestProcessor;
 import com.delcyon.capo.controller.client.ControllerRequest;
 import com.delcyon.capo.crypto.CertificateRequest;
 import com.delcyon.capo.crypto.CertificateRequest.CertificateRequestType;
 import com.delcyon.capo.datastream.StreamHandler;
 import com.delcyon.capo.datastream.StreamProcessor;
 import com.delcyon.capo.datastream.StreamUtil;
 import com.delcyon.capo.preferences.Preference;
 import com.delcyon.capo.preferences.PreferenceInfo;
 import com.delcyon.capo.preferences.PreferenceInfoHelper;
 import com.delcyon.capo.preferences.PreferenceProvider;
 import com.delcyon.capo.protocol.client.CapoConnection;
 import com.delcyon.capo.protocol.client.Request;
 import com.delcyon.capo.resourcemanager.CapoDataManager;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.remote.RemoteResourceResponseProcessor;
 import com.delcyon.capo.resourcemanager.remote.RemoteResourceResponseProcessor.ThreadedInputStreamReader;
 import com.delcyon.capo.resourcemanager.types.FileResourceType;
 import com.delcyon.capo.tasks.TaskManagerThread;
 import com.delcyon.capo.xml.XPath;
 import com.delcyon.capo.xml.cdom.CNode;
 
 /**
  * @author jeremiah
  *
  */
 @PreferenceProvider(preferences=CapoClient.Preferences.class)
 public class CapoClient extends CapoApplication
 {
 	
 	public enum Preferences implements Preference
 	{
 		
 		@PreferenceInfo(arguments={"boolean"}, defaultValue="true", description="Run The Capo Client as a service [true|false] default is true", longOption="CLIENT_AS_SERVICE", option="CLIENT_AS_SERVICE")
 		CLIENT_AS_SERVICE,
 		@PreferenceInfo(arguments={"clientID"}, defaultValue="capo.client.0", description="ID that this server will use when communicating with servers", longOption="CLIENT_ID", option="CLIENT_ID")
 		CLIENT_ID,
 		@PreferenceInfo(arguments={"keysize"}, defaultValue="1024", description="Encryption key size", longOption="KEY_SIZE", option="KEY_SIZE")
 		KEY_SIZE,
 		@PreferenceInfo(arguments={"months"}, defaultValue="36", description="Number of Months before key expires", longOption="KEY_MONTHS_VALID", option="KEY_MONTHS_VALID")
 		KEY_MONTHS_VALID,
 		@PreferenceInfo(arguments={"interval"}, defaultValue="1000", description="Milliseconds until retry, on connection failure", longOption="CONNECTION_RETRY_INTERVAL", option="CONNECTION_RETRY_INTERVAL")
         CONNECTION_RETRY_INTERVAL,
 		@PreferenceInfo(arguments={"boolean"}, defaultValue="false", description="This will cause the client to ignore any server requests to restart. This should only be used for testing. Defaults to false.", longOption="IGNORE_RESTART_REQUEST", option="IGNORE_RESTART_REQUEST",location=Location.CLIENT)
         IGNORE_RESTART_REQUEST;
 		
 		@Override
 		public String[] getArguments()
 		{
 			return PreferenceInfoHelper.getInfo(this).arguments();
 		}
 
 		@Override
 		public String getDefaultValue()
 		{
 		    return java.util.prefs.Preferences.systemNodeForPackage(CapoApplication.getApplication().getClass()).get(getLongOption(), PreferenceInfoHelper.getInfo(this).defaultValue());			
 		}
 
 		@Override
 		public String getDescription()
 		{
 			return PreferenceInfoHelper.getInfo(this).description();
 		}
 
 		@Override
 		public String getLongOption()
 		{
 			return PreferenceInfoHelper.getInfo(this).longOption();
 		}
 
 		@Override
 		public String getOption()
 		{		
 			return PreferenceInfoHelper.getInfo(this).option();
 		}
 		
 		@Override
 		public Location getLocation() 
 		{
 			return PreferenceInfoHelper.getInfo(this).location();
 		}
 	}
 	
 	private static final String APPLICATION_DIRECTORY_NAME = "client";
 	private static final long MAX_SHUTDOWN_WAIT_TIME = 10000; //10 seconds
 	
 	private HashMap<String, String> idHashMap = new HashMap<String, String>();
    
 	
 	public CapoClient() throws Exception
 	{
 		super();
 	}
 	
 	@Override
 	public Integer start(String[] programArgs)
 	{
 		try
 		{
 			init(programArgs);			
 			startup(programArgs);			
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			return 1;
 		}
 		return null;
 	}
 	
 	/**
 	 * @param programArgs
 	 */
 	public static void main(String[] programArgs)
 	{
 		try
 		{		    		   
 		    WrapperManager.start( new CapoClient(), programArgs );		    
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	protected void init(String[] programArgs) throws Exception
 	{
 	    setApplicationState(ApplicationState.INITIALIZING);
 		setConfiguration(new Configuration(programArgs));
 		if (getConfiguration().hasOption(PREFERENCE.HELP))
 		{
 			getConfiguration().printHelp();
 			System.exit(0);
 		}
 
 		//System.setProperty("javax.net.ssl.keyStore", getConfiguration().getValue(PREFERENCE.KEYSTORE));
 		System.setProperty("javax.net.ssl.keyStorePassword", getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD));
 
 		setDataManager(CapoDataManager.loadDataManager(getConfiguration().getValue(PREFERENCE.RESOURCE_MANAGER)));
 		getDataManager().init();
 		
 		runStartupScript(getConfiguration().getValue(PREFERENCE.STARTUP_SCRIPT));
 		setApplicationState(ApplicationState.INITIALIZED);
 	}
 
 	private void runStartupScript(String startupScriptName) throws Exception
 	{
 		ResourceDescriptor startupScriptFile = getDataManager().getResourceDescriptor(null,startupScriptName);
 		startupScriptFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
 		if (startupScriptFile.getResourceMetaData(null).exists() == false)
 		{
 		    startupScriptFile.performAction(null, Action.CREATE);
 		    startupScriptFile.close(null);
             startupScriptFile.open(null);
 			Document startupDocument = CapoApplication.getDefaultDocument("client_startup.xml");			
 			OutputStream startupFileOutputStream = startupScriptFile.getOutputStream(null);
 			TransformerFactory tFactory = TransformerFactory.newInstance();
 			Transformer transformer = tFactory.newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.transform(new DOMSource(startupDocument), new StreamResult(startupFileOutputStream));			
 			startupFileOutputStream.close();
 		}
 		
 		LocalRequestProcessor localRequestProcessor = new LocalRequestProcessor();
 		localRequestProcessor.process(CapoApplication.getDocumentBuilder().parse(startupScriptFile.getInputStream(null)));
 		startupScriptFile.close(null);
 	}
 	
 	@Override
 	protected void startup(String[] programArgs) throws Exception
 	{
 		start();
 		//keep this thread running until the client thread is ready. 
 		while(getApplicationState().ordinal() < ApplicationState.INITIALIZED.ordinal())
 		{		   
 			Thread.sleep(500);			
 		}		
 	}
 	
 	@Override
 	public void run()
 	{
 		
 		try 
 		{
 			
 		    HashMap<String, String> sessionHashMap = new HashMap<String, String>();
 			//get list of RequestProducers
 			//get list of ServerResponseConsumers
 			//get ordered list of initial requests
 
 
 			CapoConnection capoConnection = new CapoConnection();
 			runUpdateRequest(capoConnection,sessionHashMap);
 			capoConnection.close();
 			if (WrapperManager.isShuttingDown()) //bail out if we're restarting
             {
 			    setApplicationState(ApplicationState.STOPPING);
 			    return;
             }
 			if (hasValidKeystore() == false)
 			{
 				//setup keystore
 				capoConnection = new CapoConnection();
 				setupKeystore(capoConnection);
 				capoConnection.close();
 			}
 			else
 			{
 				loadKeystore();
 			}
 
 			setupSSL();
 
 			//verify identity scripts
 
 			//run identity scripts
 			capoConnection = new CapoConnection();
 			runIdentityRequest(capoConnection,sessionHashMap);
 //			capoConnection.close();
 //
 //			capoConnection = new CapoConnection();
             runTasksUpdateRequest(capoConnection,sessionHashMap);
 //            capoConnection.close();
 //            
 //			capoConnection = new CapoConnection();
 			runDefaultRequest(capoConnection,sessionHashMap);
 			capoConnection.close();
 			
 			TaskManagerThread.startTaskManagerThread();
 			
 			
 		} catch (Exception e)
 		{
 			//if something else is monitoring this client, don't exit on error, leave it to the monitor to do so.
 			if (getExceptionList() != null)
 			{
 				getExceptionList().add(e);				
 			}
 			else //we're on our own here, so just exit.
 			{
				CapoApplication.logger.log(Level.SEVERE, "Exception thrown in main processing loop. Exiting.",e);				
				System.exit(1);
 			}
 		}
 		setApplicationState(ApplicationState.READY);
 	}
 
 	
 	public void runDefaultRequest(CapoConnection capoConnection, HashMap<String, String> sessionHashMap) throws Exception 
 	{
 		ControllerRequest controllerRequest = new ControllerRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
 		//load client variables
 		controllerRequest.loadSystemVariables();
 		runRequest(capoConnection, controllerRequest,sessionHashMap);
 		
 	}
 
 	public void runIdentityRequest(CapoConnection capoConnection, HashMap<String, String> sessionHashMap) throws Exception 
 	{
 		ControllerRequest controllerRequest = new ControllerRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
         controllerRequest.setType("identity");
         controllerRequest.loadSystemVariables();
 		runRequest(capoConnection, controllerRequest,sessionHashMap);
 	}
 
 	public void runUpdateRequest(CapoConnection capoConnection,HashMap<String, String> sessionHashMap) throws Exception
 	{
 		ControllerRequest controllerRequest = new ControllerRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
 		controllerRequest.setType("update");
 		controllerRequest.loadSystemVariables();
 		runRequest(capoConnection, controllerRequest,sessionHashMap);		
 	}
 	
 	public void runTasksUpdateRequest(CapoConnection capoConnection,HashMap<String, String> sessionHashMap) throws Exception
     {
         ControllerRequest controllerRequest = new ControllerRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
         controllerRequest.setType("tasks_update");
         controllerRequest.loadSystemVariables();
         runRequest(capoConnection, controllerRequest,sessionHashMap);
     }
 	
 	public void runRequest(CapoConnection capoConnection, Request request, HashMap<String, String> sessionHashMap) throws Exception
 	{
 	    String initialRequestType = null;
 	    if (request instanceof ControllerRequest)
         {
             initialRequestType = ((ControllerRequest) request).getType();
             if (initialRequestType == null || initialRequestType.isEmpty())
             {
                 initialRequestType = "default";
             }
         }	    
 	    else
 	    {
 	        initialRequestType = request.getClass().getSimpleName();
 	    }
 		//send request
 		try
 		{
 		    CapoApplication.logger.log(Level.INFO, "STARTING "+initialRequestType+" request.");
 			request.send();
 		}
 		catch (SocketException socketException)
 		{
 		    socketException.printStackTrace();
 			//do nothing, let any errors be processed later, since there might be a message in the buffer
 		}
 		boolean isFinished = false;
 		int emptyCount = 0;
 		while(isFinished == false && emptyCount < 20)
 		{	
 			byte[] buffer = getBuffer(capoConnection.getInputStream());
 			if(buffer == null)
 			{
 			    //connection closed
 			    CapoApplication.logger.log(Level.WARNING, "Server Connection closed unexpectedly for "+initialRequestType+" request.");
 			    break;
 			}
 			//figure out the kind of response
 			StreamProcessor streamProcessor = StreamHandler.getStreamProcessor(buffer);
 			if (streamProcessor != null)
 			{
 				streamProcessor.init(sessionHashMap);
 				streamProcessor.processStream(capoConnection.getInputStream(), capoConnection.getOutputStream());
 			}
 			else
 			{
 				//if we have no data, then we are finished, otherwise wait, then try again?
 				if (buffer.length == 0)
 				{
 				    CapoApplication.logger.log(Level.WARNING, "Empty Response from server for "+initialRequestType+" request. Going to wait for more data.");
 				    emptyCount++;
 				}
 				else
 				{
 				    String bufferString = new String(buffer);
 				    if(bufferString.startsWith("FINISHED:"))
 				    {
 				        int count = 0;
 				        while(RemoteResourceResponseProcessor.getThreadedInputStreamReaderHashtable().size() != 0)
 				        {
 				            CapoApplication.logger.log(Level.WARNING, "We shouldn't be done yet!");
 				            Thread.sleep(1000);
 				            count++;
 				            if(count >= 30)
 				            {
 				                CapoApplication.logger.log(Level.SEVERE, "Well, that didn't work, killing all of the connections.");
 				                Enumeration<ThreadedInputStreamReader> threadedInputStreamReaderEnumeration =  RemoteResourceResponseProcessor.getThreadedInputStreamReaderHashtable().elements();
 				                while(threadedInputStreamReaderEnumeration.hasMoreElements())
 				                {
 				                    ThreadedInputStreamReader threadedInputStreamReader = threadedInputStreamReaderEnumeration.nextElement();
 				                    threadedInputStreamReader.close();
 				                }
 				                RemoteResourceResponseProcessor.getThreadedInputStreamReaderHashtable().clear();
 				                break;
 				            }
 				        }
 				        StreamUtil.fullyReadUntilPattern(capoConnection.getInputStream(), false, (byte)0);
 				        CapoApplication.logger.log(Level.INFO, "FINISHED "+initialRequestType+" request.");
 				        isFinished = true;				        
 				    }
 				    else
 				    {
 				        CapoApplication.logger.log(Level.WARNING, "Don't know what to do with '"+bufferString+"', finishing "+initialRequestType+" request.");
 				        isFinished = true;				        
 				    }
 				}
 				
 			}
 		}
 	}
 	
 	
 	private byte[] getBuffer(BufferedInputStream inputStream) throws Exception
 	{
 		int bufferSize = getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE);
 	    byte[] buffer = new byte[bufferSize];
 	    inputStream.mark(bufferSize);
 	    int bytesRead = StreamUtil.fullyReadIntoBufferUntilPattern(inputStream, buffer, (byte)0);
 	    inputStream.reset();
 	    
 	    //truncate the buffer so we can do accurate length checks on it
 	    //totally pointless, but seems like a good idea at the time
 	    if (bytesRead < 0)
 	    {
 	    	return null;
 	    }
 	    else if (bytesRead < bufferSize)
 	    {
 	    	byte[] shortenedBuffer = new byte[bytesRead];
 	    	System.arraycopy(buffer, 0, shortenedBuffer, 0, bytesRead);
 	    	return shortenedBuffer;
 	    }
 	    else
 	    {
 	    	return buffer;
 	    }
 	}
 	
 	@Override
 	public String getApplicationDirectoryName()
 	{
 		return APPLICATION_DIRECTORY_NAME;
 	}
 	
 	public void clearIDMap()
 	{
 		idHashMap.clear();		
 	}
 
 	public void setID(String name, String value)
 	{
 		idHashMap.put(name, value);
 		
 	}
 
 	public HashMap<String, String> getIDMap()
 	{
 		return idHashMap;
 	}
 	
 	
 	private boolean hasValidKeystore() throws Exception
 	{
 		boolean keyStoreIsValid = true;
 		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
 		ResourceDescriptor keystoreFile = getDataManager().getResourceDescriptor(null,getConfiguration().getValue(PREFERENCE.KEYSTORE));
 		keystoreFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
 		if (keystoreFile.getResourceMetaData(null).exists() == false)
 		{
 			return false;
 		}
 		
 		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
 		InputStream keyStoreFileInputStream = keystoreFile.getInputStream(null); 
 		keyStore.load(keyStoreFileInputStream, password);		
 		keyStoreFileInputStream.close();
 		String clientID = getConfiguration().getValue(CapoClient.Preferences.CLIENT_ID);
 		if (keyStore.containsAlias(clientID+".private") == false)
 		{
 			return false;
 		}
 		
 		
 		
 		return keyStoreIsValid;
 	}
 	
 	private void loadKeystore() throws Exception
 	{
 		
 		// load the file
 		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
 		ResourceDescriptor keystoreResourceDescriptor = getDataManager().getResourceDescriptor(null, getConfiguration().getValue(PREFERENCE.KEYSTORE));
 		keystoreResourceDescriptor.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
 		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
 		InputStream keyStoreFileInputStream = keystoreResourceDescriptor.getInputStream(null);
 		keyStore.load(keyStoreFileInputStream, password);
 		keyStoreFileInputStream.close();
 		setKeyStore(keyStore);
 		//System.setProperty("javax.net.ssl.keyStore", keystoreFile.getCanonicalPath());
 		System.setProperty("javax.net.ssl.keyStorePassword", getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD));
 	}
 	
 	
 	private void setupSSL() throws Exception
 	{
 				
 		// set the ssl context to load using our newly created trustmanager which has our keys in it.
 		SSLContext sslContext = SSLContext.getInstance("SSL");
 
 		// initialize a key manager factory with our keystore
 		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         keyManagerFactory.init(getKeyStore(), getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray());
         
      // initialize a trust manager factory with our keystore
         TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());  
         trustManagerFactory.init(getKeyStore());
         
 		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());		
 		setSslSocketFactory(sslContext.getSocketFactory());
 		
 	}
 	
 	
 
 	private void setupKeystore(CapoConnection capoConnection) throws Exception
 	{
 		 
 		// load the file
 		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
 		ResourceDescriptor keystoreFile = getDataManager().getResourceDescriptor(null, getConfiguration().getValue(PREFERENCE.KEYSTORE));
 		keystoreFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
 		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
 		if (keystoreFile.getResourceMetaData(null).exists() == false)
 		{
 			KeyPairGenerator rsakeyPairGenerator = KeyPairGenerator.getInstance("RSA");
             rsakeyPairGenerator.initialize(getConfiguration().getIntValue(Preferences.KEY_SIZE));
             KeyPair rsaKeyPair = rsakeyPairGenerator.generateKeyPair();
 			
             //get certificate from server
             CertificateRequest certificateRequest = new CertificateRequest(capoConnection);
             certificateRequest.setCertificateRequestType(CertificateRequestType.DH);
             certificateRequest.loadDHPhase1();
             certificateRequest.init();
             certificateRequest.send();
             certificateRequest.parseResponse();
             
           //this is where the server assigns our client ID
             String clientID = certificateRequest.getParameter(CertificateRequest.Attributes.CLIENT_ID);
             getConfiguration().setValue(Preferences.CLIENT_ID,clientID);
             
             byte[] certificateEncoding = certificateRequest.getDecryptedPayload();
             
             Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certificateEncoding));
             
             String serverPassword = CapoApplication.getConfiguration().getValue(PREFERENCE.CLIENT_VERIFICATION_PASSWORD);
             if (serverPassword.isEmpty())
             {
             	System.out.println("Enter Password:");
             	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             	serverPassword = br.readLine();
             }
             certificateRequest.setPayload(serverPassword);
             certificateRequest.setParameter(CertificateRequest.Attributes.CLIENT_PUBLIC_KEY, DatatypeConverter.printBase64Binary(rsaKeyPair.getPublic().getEncoded()));
             ((CNode) certificateRequest.getRequestDocument().getDocumentElement()).setNodeName("ClientResponse");
             certificateRequest.resend();
             
             //we need to make sure that we wait for a server response here, so that we don't continue processing until the server has had time to update its keystore.
             Element responseElement = certificateRequest.readResponse().getDocumentElement();
             if (responseElement.hasAttribute("result") == false)
             {
             	throw new Exception("Server did NOT process key request, check logs.");
             }
             else if (responseElement.getAttribute("result").equals("WRONG_PASSWORD"))
             {            	
             	throw new Exception("Wrong Password.");
             }
             else if (responseElement.getAttribute("result").equals("SUCCESS") == false)
             {
             	XPath.dumpNode(responseElement, System.err);
             	throw new Exception("Server did NOT process key request, check logs.");
             }
             
 			keyStore.load(null, password);
 			
 			KeyStore.TrustedCertificateEntry trustedCertificateEntry = new TrustedCertificateEntry(certificate);
 
 			keyStore.setEntry(certificateRequest.getParameter(CertificateRequest.Attributes.SERVER_ID), trustedCertificateEntry,null);
             KeyStore.PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(rsaKeyPair.getPrivate(), new Certificate[]{certificate});
             
             keyStore.setEntry(getConfiguration().getValue(Preferences.CLIENT_ID)+".private", privateKeyEntry,new KeyStore.PasswordProtection(password));
             if (keystoreFile.getResourceMetaData(null).exists() == false)
             {
                 keystoreFile.performAction(null, Action.CREATE);
                 keystoreFile.close(null);
                 keystoreFile.open(null);                
             }
             OutputStream keyStoreFileOutputStream = keystoreFile.getOutputStream(null);
             keyStore.store(keyStoreFileOutputStream, password);
             keyStoreFileOutputStream.close();
             setKeyStore(keyStore);
 		}
 		else
 		{
 			loadKeystore();
 		}
 		
 	}
 
 	public void shutdown() throws Exception
 	{
 	    CapoApplication.logger.log(Level.INFO,"Waiting for processing to finish.");
         while(getApplicationState().ordinal() < ApplicationState.READY.ordinal())
         {
             Thread.sleep(500);
         }
         
 		if (TaskManagerThread.getTaskManagerThread() != null)
 		{
 		    CapoApplication.logger.log(Level.INFO,"Stopping Task Manager");
 			TaskManagerThread.getTaskManagerThread().interrupt();
 			long totalWaitTime = 0;
 			long waitTime = 500;
 			
 			while(TaskManagerThread.getTaskManagerThread().getTaskManagerState() != ApplicationState.STOPPED || totalWaitTime >= MAX_SHUTDOWN_WAIT_TIME)
 			{
 			    try
 			    {			        			        
 			        Thread.sleep(waitTime);
 			        totalWaitTime += waitTime;
 			    }
 			    catch (InterruptedException interruptedException)
 			    {			        
 			        CapoApplication.logger.log(Level.WARNING,"Ignoring InterruptedException");
 			    }
 			}
 			CapoApplication.logger.log(Level.INFO,"Done Stopping Task Manager");			
 		}
 		
 		CapoApplication.logger.log(Level.INFO,"Releaseing Data Manager");	
 		getDataManager().release();
 		setDataManager(null);
 		
 		CapoApplication.logger.log(Level.INFO,"Done.");		
 	}
 	
 	
 }
