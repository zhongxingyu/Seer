 package com.web.server;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.lang.management.ManagementFactory;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.UUID;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLServerSocketFactory;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.digester.xmlrules.DigesterLoader;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.web.messaging.MessagingElem;
 import com.web.messaging.MessagingServer;
 import com.web.messaging.RandomQueueMessagePicker;
 import com.web.messaging.RoundRobinQueueMessagePicker;
 import com.web.messaging.TopicMessagePicker;
 import com.web.services.ATAConfig;
 import com.web.services.ATAServer;
 import com.web.services.ATAConfigClient;
 import com.web.services.ExecutorServiceThread;
 
 /**
  * This class is the implementation of the WebServer which implements the HTTP protocol
  * 
  * @author arun
  *
  */
 public class WebServer extends Thread implements Runnable {
 	static HashMap cache=new HashMap();
 	HashMap urlClassLoaderMap;
 	Logger logger=Logger.getLogger(WebServer.class);
 	Socket socket;
 	int requestPort;
 	String deployDirectory;
 	int shutdownPort;
 	Digester serverdigester;
 	static HashMap sessionObjects=new HashMap();
 	public WebServer(){
 		this.socket=null;
 		this.requestPort=0;
 		this.deployDirectory=null;
 		this.shutdownPort=0;
 		serverdigester=null;
 		
 	}
 	public WebServer(Socket sock,int requestPort,String deployDirectory,int shutdownPort,Digester serverdigester,HashMap urlClassLoaderMap){
 		this.socket=sock;
 		this.requestPort=requestPort;
 		this.deployDirectory=deployDirectory;
 		this.shutdownPort=shutdownPort;
 		this.serverdigester=serverdigester;
 		this.urlClassLoaderMap=urlClassLoaderMap;
 	}
 	
 	/**
 	 * This methos is the implementation of the HTPP request and sends the response.
 	 */
 	public void run(){
 		byte[] response;
 		byte[] content;
 		byte[] uploadData = null;
 		HttpHeaderClient httpHeaderClient = null;
 		InputStream istream = null;
 		OutputStream ostream = null;
 		HttpHeaderServer serverParam=new HttpHeaderServer();
 		StringBuffer buffer=new StringBuffer();
 		String value;
 		char c;
 		String endvalue="\r\n\r\n";
 		String urlFormEncoded;
 		int responseCode;
 		try{
 			////System.out.println("value=");
 			istream=socket.getInputStream();
 			BufferedReader bistr=new BufferedReader(new InputStreamReader(istream,"UTF-8"));
 			//socket.setReceiveBufferSize(10000);
 			//System.out.println("value1=");
 			int availbleStream;
 			int totalBytRead=0;
 			int ch;
 			ByteArrayOutputStream bytout=new ByteArrayOutputStream();
 			ByteArrayOutputStream contentout=new ByteArrayOutputStream();
 			//System.out.println(istream.available());
 			int bytesRead;
 			int endbytIndex=0;
 			int contentbytIndex=0;
 			boolean httpHeaderEndFound=false;
 			byte[] byt;
 			while( (ch= bistr.read()) != -1 ) {
 				bytout.write(ch);
 				if(!httpHeaderEndFound&&(char)ch==endvalue.charAt(endbytIndex)){
 					endbytIndex++;
 					if(endbytIndex==endvalue.length()){
 						byt=bytout.toByteArray();
 						value=new String(ObtainBytes(byt,0,byt.length-4));
 						System.out.println(value);
 						httpHeaderClient=parseHttpHeaders(value);
 						httpHeaderEndFound=true;
 						bytout.close();
 						endbytIndex=0;
 						if(httpHeaderClient.getContentLength()==null)break;
 					}
 				}
 				else{
 					endbytIndex=0;
 				}
 				if(httpHeaderClient!=null&&httpHeaderEndFound){
 					contentout.write(ch);
 					contentbytIndex++;
 					if(httpHeaderClient.getContentLength()!=null&&contentbytIndex>=Integer.parseInt(httpHeaderClient.getContentLength())){
 						break;
 					}
 				}
 				totalBytRead++;
 			}
 			/*while(totalBytRead==0){
 				while((ch = bistr.read())!=-1){
 					System.out.println((char)ch);
 					////System.out.println("availableStream="+availbleStream);
 					bytarrayOutput.write(ch);
 					totalBytRead++;
 				}
 			}*/
 			if(totalBytRead==0){
 				System.out.println("Since byte is 0 sock and istream os closed");
 				//istream.close();
 				socket.close();
 				return;
 			}
 			//istream.read(bt,0,9999999);
 			System.out.println("bytes read");
 			byte[] contentByte=contentout.toByteArray();
 			contentout.close();
 			//System.out.println("String="+new String(bt));
 			/*int index=containbytes(bt,endvalue.getBytes());
 			if(index==-1)index=totalBytRead;
 			value=new String(ObtainBytes(bt,0,index));*/
 			//System.out.println("value2="+value);
 			
 			HashMap<String,HttpCookie> httpCookies=httpHeaderClient.getCookies();
 			HttpSession session=null;
 			if(httpCookies!=null){
 				Iterator<String> cookieNames=httpCookies.keySet().iterator();
 				for(;cookieNames.hasNext();){
 					String cookieName=cookieNames.next();
 					System.out.println(cookieName+" "+httpCookies.get(cookieName).getValue());
 					if(cookieName.equals("SERVERSESSIONID")){
 						session=(HttpSession) sessionObjects.get(httpCookies.get(cookieName).getValue());
 						httpHeaderClient.setSession(session);
 						//break;
 					}
 				}
 			}
 			//System.out.println("Session="+session);
 			if(session==null){
 				HttpCookie cookie=new HttpCookie();
 				cookie.setKey("SERVERSESSIONID");
 				cookie.setValue(UUID.randomUUID().toString());
 				httpCookies.put("SERVERSESSIONID",cookie);
 				session=new HttpSession();
 				sessionObjects.put(cookie.getValue(), session);
 				httpHeaderClient.setSession(session);
 			}
 			if(httpHeaderClient.getContentType()!=null&&
 					httpHeaderClient.getContentType().equals(HttpHeaderParamNames.MULTIPARTFORMDATAVALUE)){
 					////System.out.println(new String(uploadData));
 					HashMap paramMap=new MultipartFormData().parseContent(contentByte,httpHeaderClient);
 					httpHeaderClient.setParameters(paramMap);
 					////logger.info(uploadData);
 			}
 			else if(httpHeaderClient.getContentType()!=null&&httpHeaderClient.getContentType().equals(HttpHeaderParamNames.URLENCODED)){
 					urlFormEncoded=new String(contentByte);
 					HashMap paramMap=parseUrlEncoded(urlFormEncoded);
 					httpHeaderClient.setParameters(paramMap);
 			}
 			////logger.info(serverconfig.getDeploydirectory()+httpHeaderClient.getResourceToObtain());
 			////System.out.println("value3=");
 			
 			////logger.info(new String(bt));
 			serverParam.setContentType("text/html");
 			URLDecoder decoder=new URLDecoder();
 			////System.out.println("content Length= "+socket);
 			responseCode=200;
 			File file=new File(deployDirectory+decoder.decode(httpHeaderClient.getResourceToObtain()));
 			FileContent fileContent=(FileContent) cache.get(httpHeaderClient.getResourceToObtain());
 			if(fileContent!=null&&file.lastModified()==fileContent.getLastModified()){
 				//System.out.println("In cache");
 				content=(byte[]) fileContent.getContent();
 			}
 			else{
     			content=ObtainContentExecutor(deployDirectory,httpHeaderClient.getResourceToObtain(),httpHeaderClient,serverdigester,urlClassLoaderMap);
     			////System.out.println("content Length2= ");
     			if(content==null){
     				//System.out.println("In caching content");
     				content=obtainContent(deployDirectory+decoder.decode(httpHeaderClient.getResourceToObtain()));
     				fileContent=new FileContent();
     				fileContent.setContent(content);
     				fileContent.setFileName(httpHeaderClient.getResourceToObtain());
     				fileContent.setLastModified(file.lastModified());
     				cache.put(httpHeaderClient.getResourceToObtain(),fileContent);
     			}
     			////System.out.println("value4=");
     			
 			}
 			
 			if(content==null){
 				responseCode=404;
 				content=("<html><body><H1>The Request resource "+httpHeaderClient.resourceToObtain+" Not Found</H1><body></html>").getBytes();
 			}
 			////System.out.println("content Length3= ");
 			serverParam.setContentLength(""+(content.length+4));
 			if(httpHeaderClient.getResourceToObtain().endsWith(".ico")){
 				serverParam.setContentType("image/png");
 			}
 			////System.out.println("value5=");
 
 			////System.out.println("content Length4= ");
 			response=formHttpResponseHeader(responseCode,serverParam,content,httpHeaderClient.getCookies());
 			////System.out.println("value6=");
 			ostream=socket.getOutputStream();
 			//logger.info("Response="+new String(response));
 			//System.out.println("value6=");
 			//logger.info("Response="+new String(response));
 			////System.out.println("content "+"Response="+new String(response));
 			ostream.write(response);
 			ostream.flush();
 			ostream.close();
 			socket.close();
 		} 
 		catch (IOException e) {
 			Socket socket;
 			e.printStackTrace();
 			
 			//logger.error(e);
 			try {
 				Thread.sleep(30000);
 				socket = new Socket("localhost",shutdownPort);
 				OutputStream outputStream=socket.getOutputStream();
 				outputStream.write("shutdown WebServer\r\n\r\n".getBytes());
 				outputStream.close();
 			} catch (IOException | InterruptedException e1) {
 				
 				e1.printStackTrace();
 			}					
 			e.printStackTrace();
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	/**
 	 * This method copies the content from start to end
 	 * @param content
 	 * @param start
 	 * @param end
 	 * @return byte[]
 	 * @throws Exception
 	 */
 	private static byte[] ObtainBytes(byte[] content,int start,int end) throws Exception{
 		if(start>=end){
 			throw new Exception("Start byte should be lesser than end byte");
 		}
 		byte[] resultBt=new byte[end-start];
 		for(int count=start;count<end;count++){
 			resultBt[count-start]=content[count];			
 		}
 		return resultBt;
 	}
 	
 	private int containbytes(byte[] byt1,byte[] byt2){
 		int count=0;
 		boolean isEqual=true;
 		for(count=0;count<byt1.length;count++){
 			isEqual=true;
 			for(int secondbytcount=0;secondbytcount<byt2.length;secondbytcount++){
 				if(byt1[count+secondbytcount]!=byt2[secondbytcount]){
 					isEqual=false;
 					break;
 				}
 			}
 			if(isEqual){
 				return count;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * This is the start of the all the services in web server
 	 * @param args
 	 * @throws IOException 
 	 * @throws SAXException 
 	 */
 	public static void main(String[] args) throws IOException, SAXException {
 		
 		
 		HashMap urlClassLoaderMap=new LinkedHashMap();
 		HashMap executorServicesMap=new HashMap();
 		HashMap ataMap=new HashMap<String,ATAConfig>();
 		HashMap messagingClassMap=new HashMap();
 		Digester serverdigester = DigesterLoader.createDigester(new InputSource(new FileInputStream("./config/serverconfig-rules.xml")));
 		ServerConfig serverconfig=(ServerConfig)serverdigester.parse(new InputSource(new FileInputStream("./config/serverconfig.xml")));
 		Digester messagingdigester = DigesterLoader.createDigester(new InputSource(new FileInputStream("./config/messagingconfig-rules.xml")));
 		MessagingElem messagingconfig=(MessagingElem)messagingdigester.parse(new InputSource(new FileInputStream("./config/messaging.xml")));
 		//System.out.println(messagingconfig);
 		////System.out.println(serverconfig.getDeploydirectory());
 		PropertyConfigurator.configure("log4j.properties");
 		/*MemcachedClient cache=new MemcachedClient(
                 new InetSocketAddress("localhost", 1000));*/
 
         // Store a value (async) for one hour
         //c.set("someKey", 36, new String("arun"));
         // Retrieve a value.        
 		
 		ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();
 		
 		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
         ObjectName name = null;
 		try {
 			name = new ObjectName("com.web.server:type=WarDeployer");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		WarDeployer warDeployer=new WarDeployer(serverconfig.getDeploydirectory(),urlClassLoaderMap,executorServicesMap,messagingClassMap,messagingconfig);
 		
 		try {
 			mbs.registerMBean(warDeployer, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		//warDeployer.start();
 		executor.execute(warDeployer);
 		
         ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
 		 
 	    
         serverSocketChannel.bind(new InetSocketAddress("localhost", Integer.parseInt(serverconfig.getPort())),50000);
  
         
         serverSocketChannel.configureBlocking(false);
 		
         
         
         
         
 		byte[] shutdownBt=new byte[50];
 		WebServerRequestProcessor webserverRequestProcessor=new WebServer().new WebServerRequestProcessor(urlClassLoaderMap,serverSocketChannel,serverconfig.getDeploydirectory(),Integer.parseInt(serverconfig.getShutdownport()),1);
 		try {
 			name = new ObjectName("com.web.server:type=WebServerRequestProcessor");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(webserverRequestProcessor, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//webserverRequestProcessor.start();
         executor.execute(webserverRequestProcessor);
 		
 		
         for(int i=0;i<100;i++){
 			WebServerRequestProcessor webserverRequestProcessor1=new WebServer().new WebServerRequestProcessor(urlClassLoaderMap,serverSocketChannel,serverconfig.getDeploydirectory(),Integer.parseInt(serverconfig.getShutdownport()),2);
 			try {
 				name = new ObjectName("com.web.server:type=WebServerRequestProcessor"+(i+1));
 			} catch (MalformedObjectNameException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} 
 			
 			try {
 				mbs.registerMBean(webserverRequestProcessor1, name);
 			} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 					| NotCompliantMBeanException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			
 	        executor.execute(webserverRequestProcessor1);
         }
 		
         ServerSocketChannel serverSocketChannelServices = ServerSocketChannel.open();
 		 
 	    
         serverSocketChannelServices.bind(new InetSocketAddress("localhost", Integer.parseInt(serverconfig.getServicesport())),50000);
  
         
         serverSocketChannelServices.configureBlocking(false);
 
 		ExecutorServiceThread executorService=new ExecutorServiceThread(serverSocketChannelServices,executorServicesMap,Integer.parseInt(serverconfig.getShutdownport()),ataMap,urlClassLoaderMap,serverconfig.getDeploydirectory(),serverconfig.getServicesdirectory(),serverconfig.getEarservicesdirectory(),serverconfig.getNodesport());
 		
 		try {
 			name = new ObjectName("com.web.services:type=ExecutorServiceThread");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(executorService, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//executorService.start();
 		executor.execute(executorService);
 		
 		
 		for(int i=0;i<0;i++){
 			ExecutorServiceThread executorService1=new ExecutorServiceThread(serverSocketChannelServices,executorServicesMap,Integer.parseInt(serverconfig.getShutdownport()),ataMap,urlClassLoaderMap,serverconfig.getDeploydirectory(),serverconfig.getServicesdirectory(),serverconfig.getEarservicesdirectory(),serverconfig.getNodesport());
 			
 			try {
 				name = new ObjectName("com.web.services:type=ExecutorServiceThread"+(i+1));
 			} catch (MalformedObjectNameException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} 
 			
 			try {
 				mbs.registerMBean(executorService1, name);
 			} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 					| NotCompliantMBeanException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			
 			//executor.execute(executorService1);
 		}
 		
 		
 		WebServerHttpsRequestProcessor webserverHttpsRequestProcessor=new WebServer().new WebServerHttpsRequestProcessor(urlClassLoaderMap,Integer.parseInt(serverconfig.getHttpsport()),serverconfig.getDeploydirectory(),Integer.parseInt(serverconfig.getShutdownport()),serverconfig.getHttpscertificatepath(),serverconfig.getHttpscertificatepasscode(),1);
 		try {
 			name = new ObjectName("com.web.server:type=WebServerHttpsRequestProcessor");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(webserverHttpsRequestProcessor, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//webserverRequestProcessor.start();
         executor.execute(webserverHttpsRequestProcessor);
 		
 		
 		/*ATAServer ataServer=new ATAServer(serverconfig.getAtaaddress(),serverconfig.getAtaport(),ataMap);
 		
 		try {
 			name = new ObjectName("com.web.services:type=ATAServer");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(ataServer, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		
 		ataServer.start();*/
 		
 		/*ATAConfigClient ataClient=new ATAConfigClient(serverconfig.getAtaaddress(),serverconfig.getAtaport(),serverconfig.getServicesport(),executorServicesMap);
 		
 		try {
 			name = new ObjectName("com.web.services:type=ATAConfigClient");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(ataClient, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		ataClient.start();*/
 		
 		MessagingServer messageServer=new MessagingServer(serverconfig.getMessageport(),messagingClassMap);
 		
 		try {
 			name = new ObjectName("com.web.messaging:type=MessagingServer");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(messageServer, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		//messageServer.start();
 		executor.execute(messageServer);
 		
 		RandomQueueMessagePicker randomqueuemessagepicker=new RandomQueueMessagePicker(messagingClassMap);
 		
 		try {
 			name = new ObjectName("com.web.messaging:type=RandomQueueMessagePicker");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(randomqueuemessagepicker, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//randomqueuemessagepicker.start();
 		executor.execute(randomqueuemessagepicker);
 		
 		RoundRobinQueueMessagePicker roundrobinqueuemessagepicker=new RoundRobinQueueMessagePicker(messagingClassMap);
 		
 		try {
 			name = new ObjectName("com.web.messaging:type=RoundRobinQueueMessagePicker");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(roundrobinqueuemessagepicker, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//roundrobinqueuemessagepicker.start();
 		executor.execute(roundrobinqueuemessagepicker);
 		
 		TopicMessagePicker topicpicker= new TopicMessagePicker(messagingClassMap);
 		
 		try {
 			name = new ObjectName("com.web.messaging:type=TopicMessagePicker");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(topicpicker, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		//topicpicker.start();
 		executor.execute(topicpicker);
 		
 		try {
 			name = new ObjectName("com.web.server:type=SARDeployer");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		SARDeployer sarDeployer=SARDeployer.newInstance(serverconfig.getDeploydirectory()); 
 		try {
 			mbs.registerMBean(sarDeployer, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			mbs.invoke(name, "startDeployer", null, null);
 		} catch (InstanceNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (ReflectionException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (MBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		JarDeployer jarDeployer=new JarDeployer(serverconfig.getServicesdirectory(), serverconfig.getServiceslibdirectory(),executorServicesMap, urlClassLoaderMap);
 		try {
 			name = new ObjectName("com.web.server:type=JarDeployer");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(jarDeployer, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//jarDeployer.start();
 		executor.execute(jarDeployer);
 		
 		
 		EARDeployer earDeployer=new EARDeployer(serverconfig.getEarservicesdirectory(),serverconfig.getDeploydirectory(),executorServicesMap, urlClassLoaderMap);
 		try {
 			name = new ObjectName("com.web.server:type=EARDeployer");
 		} catch (MalformedObjectNameException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		try {
 			mbs.registerMBean(earDeployer, name);
 		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
 				| NotCompliantMBeanException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//earDeployer.start();
 		executor.execute(earDeployer);
 		
 		
 		try {
 			ServerSocket serverSocket=new ServerSocket(Integer.parseInt(serverconfig.getShutdownport()));
 			while(true){
 				Socket sock=serverSocket.accept();
 				InputStream istream=sock.getInputStream();
 				istream.read(shutdownBt);
 				String shutdownStr=new String(shutdownBt);
 				String[] shutdownToken=shutdownStr.split("\r\n\r\n");
 				//System.out.println(shutdownStr);
 				if(shutdownToken[0].startsWith("shutdown WebServer")){
 					break;
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		//webserverRequestProcessor.stop();
 		//webserverRequestProcessor1.stop();
 		executor.shutdownNow();
 		serverSocketChannel.close();
 		serverSocketChannelServices.close();
 
 		/*warDeployer.stop();
 		executorService.stop();
 		//ataServer.stop();
 		//ataClient.stop();
 		messageServer.stop();
 		randomqueuemessagepicker.stop();
 		roundrobinqueuemessagepicker.stop();
 		topicpicker.stop();*/
 		try {
 			mbs.invoke(new ObjectName("com.web.server:type=SARDeployer"), "destroyDeployer", null, null);
 		} catch (InstanceNotFoundException | MalformedObjectNameException
 				| ReflectionException | MBeanException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//earDeployer.stop();
 		System.exit(0);
 	}
 	/**
 	 * This method parses the encoded url 
 	 * @param urlEncoded
 	 * @return
 	 */
 	public HashMap parseUrlEncoded(String urlEncoded){
 		HashMap ParamValue=new HashMap();
 		URLDecoder urlDecoder=new URLDecoder(); 
 		StringTokenizer paramGroup = new StringTokenizer(urlDecoder.decode(urlEncoded), "&");
 		 
 		   while(paramGroup.hasMoreTokens()){
 	 
 			   StringTokenizer token= new StringTokenizer(paramGroup.nextToken(), "=");
 			   String key="";
 			   String value="";
 			   if(token.hasMoreTokens())key=token.nextToken();
 			   if(token.hasMoreTokens())value=token.nextToken();
 			   ParamValue.put(key, value);
 	 
 		   }
 		   return ParamValue;
 	}
 	
 	/**
 	 * This method obtains the content executor which executes the executor services
 	 * @param deployDirectory
 	 * @param resource
 	 * @param httpHeaderClient
 	 * @param serverdigester
 	 * @return byte[]
 	 */
 	public byte[] ObtainContentExecutor(String deployDirectory,String resource,HttpHeaderClient httpHeaderClient,Digester serverdigester,HashMap urlClassLoaderMap){
 		//System.out.println("In content Executor");
 		String[] resourcepath=resource.split("/");
 		//System.out.println("createDigester1");
 		Method method = null;
 		//System.out.println("createDigester2");
 		////System.out.println();
 		Executors serverconfig;
 		if(resourcepath.length>1)
 		{
 			////System.out.println(resource);
 			
 			try {
 				File file=new File(deployDirectory+"/"+resourcepath[1]+"/WEB-INF/executor-config.xml");
 				synchronized(serverdigester){
 					serverconfig=(Executors)serverdigester.parse(file);
 				}
 				HashMap urlMap=serverconfig.executorMap;
 				//System.out.println("ObtainUrlFromResource1");
 				String urlresource=ObtainUrlFromResource(resourcepath);
 				//logger.info("urlresource"+urlresource);
 				Executor executor=(Executor) urlMap.get(urlresource);
 				WebClassLoader customClassLoader=null;
 				Class customClass=null;
 				//System.out.println("ObtainUrlFromResource2"+executor);
 				synchronized(urlClassLoaderMap){
 					//System.out.println("custom class Loader1"+urlClassLoaderMap);
 					customClassLoader=(WebClassLoader) urlClassLoaderMap.get(deployDirectory+"/"+resourcepath[1]);
 					//System.out.println("custom class Loader2"+customClassLoader);
 				}
 				if(customClassLoader==null){
 					return null;
 				}
 						//System.out.println("CUSTOM CLASS lOADER path"+deployDirectory+"/"+resourcepath[1]);
 					////System.out.println("custom class loader" +customClassLoader);
 					if(executor==null){
 							//System.out.println("url resource"+urlresource);
 						String resourceClass=(String)customClassLoader.getClassMap().get(urlresource);
 							//System.out.println(resourceClass);
 							//System.out.println(customClassLoader.getClassMap());
 						if(resourceClass==null)return null;
 						customClass=customClassLoader.loadClass(resourceClass);
 						if(customClass==null)return null;
 					}
 					else customClass=customClassLoader.loadClass(executor.getExecutorclass());
 					//logger.info(customClass.getClass());
 					/*if(httpHeaderClient.getHttpMethod().trim().toUpperCase().equals("GET")||(executor==null&&customClass!=null)){
 						method= customClass.getDeclaredMethod("doGet", new Class[]{HttpHeaderClient.class});
 					}
 					else if(httpHeaderClient.getHttpMethod().trim().toUpperCase().equals("POST")){
 						method= customClass.getDeclaredMethod("doPost", new Class[]{HttpHeaderClient.class});
 					}*/
 					
 				    //method.setAccessible(false);
 
 					if(executor!=null){
 						ExecutorInterface executorInstance=(ExecutorInterface)customClass.newInstance();
 						Object buffer=null;
 						if(httpHeaderClient.getHttpMethod().trim().toUpperCase().equals("GET")){
 							buffer =executorInstance.doGet(httpHeaderClient);
 						}
 						else if(httpHeaderClient.getHttpMethod().trim().toUpperCase().equals("POST")){
 							buffer =executorInstance.doPost(httpHeaderClient);
 						}
 						if(executor.getResponseResource()!=null){
 							httpHeaderClient.setExecutorBuffer(buffer);
 							//System.out.println("Method:"+httpHeaderClient.getHttpMethod());
 							String resourceClass=(String)customClassLoader.getClassMap().get(executor.getResponseResource().trim());
 							customClass=customClassLoader.loadClass(resourceClass);
 							executorInstance=(ExecutorInterface)customClass.newInstance();
 							buffer = executorInstance.doGet(httpHeaderClient);
 						}
 						return buffer.toString().getBytes();
 					}
 					else {
 						ExecutorInterface executorInstance=(ExecutorInterface)customClass.newInstance();
 						Object buffer =executorInstance.doGet(httpHeaderClient);
 						return buffer.toString().getBytes();
 					}
 					////System.out.println("executor resource 1");
 				    //Object buffer = method.invoke(customClass.newInstance(), new Object[]{httpHeaderClient});
 				    
 			   // //logger.info(buffer.toString());
 				
 			} catch (IOException | SAXException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} /*catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} */catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}	
 		return null;
 	}
 	
 	/**
 	 * This method obtains the url from the resource
 	 * @param resource
 	 * @return
 	 */
 	private String ObtainUrlFromResource(String[] resource){
 		if(resource!=null&&resource.length>2){
 			StringBuffer resourcepath=new StringBuffer();
 			for(int resourcepathcount=2;resourcepathcount<resource.length;resourcepathcount++){
 				resourcepath.append("/");
 				resourcepath.append(resource[resourcepathcount]);
 			}
 			return resourcepath.toString();
 		}
 		return "";
 	}
 	
 	/**
 	 * This method parses the http headers from the Http content
 	 * @param httpheader
 	 * @return
 	 */
 	public HttpHeaderClient parseHttpHeaders(String httpheader){
 		char c=(char)13;
 		String[] header=httpheader.split(""+c);
 		HttpHeaderClient httpClient=new HttpHeaderClient();
 		String httpHeader=header[0];
 		String[] httpHeaderArray=httpHeader.split(" ");
 		httpClient.setHttpMethod(httpHeaderArray[0]);
 		HashMap urlParams=new HashMap();
 		httpClient.setResourceToObtain(ObtainUrlAndParams(httpHeaderArray[1],urlParams));
 		httpClient.setParameters(urlParams);
 		httpClient.setHttpVersion(httpHeaderArray[2]);
 		String tmpHeader;
 		String[] headerParam;
 		String boundary;
 		String boundaryValue;
 		for(int i=1;i<header.length;i++){
 			tmpHeader=header[i];
 			//logger.info(tmpHeader);
 			headerParam=splitHeaderParams(tmpHeader);
 			if(headerParam[0].equals(HttpHeaderParamNames.HOST)){
 				httpClient.setHost(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.ACCEPT)){
 				httpClient.setAcceptResource(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.ACCEPT_ENCODING)){
 				httpClient.setAcceptEncoding(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.ACCEPT_LANGUAGE)){
 				httpClient.setAcceptLanguage(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.CONNECTION)){
 				httpClient.setConnection(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.USERAGENT)){
 				httpClient.setUserAgent(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.CONTENTLENGTH)){
 				httpClient.setContentLength(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.COOKIE)){
 				String[] cookies=headerParam[1].split(";");
 				String[] cookieKeyValue;
 				HttpCookie httpCookie;
 				for(String cookie:cookies){
 					cookieKeyValue=cookie.split("=");
 					httpCookie=new HttpCookie();
 					httpCookie.setKey(cookieKeyValue[0].trim());
 					httpCookie.setValue(cookieKeyValue[1].trim());
 					httpClient.addCookie(httpCookie);
 				}
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.CONTENTTYPE)){
 				String[] headerParamValue=headerParam[1].split(";");
 				httpClient.setContentType(headerParamValue[0].trim());
 				if(headerParamValue!=null&&headerParamValue.length>1){
 					if(httpClient.getContentType().equalsIgnoreCase(HttpHeaderParamNames.MULTIPARTFORMDATAVALUE)){
 						if(headerParamValue[1].contains(HttpHeaderParamNames.BOUNDARY)){
 							int equalToIndexInBoundary=headerParamValue[1].indexOf('=');
 							boundary=headerParamValue[1].substring(0,equalToIndexInBoundary).trim();
 							boundaryValue=headerParamValue[1].substring(equalToIndexInBoundary+1).trim();
 							httpClient.setBoundary(boundaryValue);
 						}
 					}
 					
 				}
 			}
 		}
 		
 		return httpClient;
 	}
 	
 	/**
 	 * This method obtains the url parameters
 	 * @param url
 	 * @param params
 	 * @return string
 	 */
 	public String ObtainUrlAndParams(String url,HashMap params){
 		 
 			URLDecoder decoder=new URLDecoder();
 			url=decoder.decode(url);
 			if (url.indexOf("?") > -1) {
 			   String paramaters = url.substring(url.indexOf("?") + 1);
 			   StringTokenizer paramGroup = new StringTokenizer(paramaters, "&");
 			   
 			   while(paramGroup.hasMoreTokens()){
 		 
 				   StringTokenizer value = new StringTokenizer(paramGroup.nextToken(), "=");
 				   String param=null;
 				   if(value.hasMoreTokens()){
 					   param=value.nextToken();
 				   }
 				   String paramValue=null;
 				   if(value.hasMoreTokens()){
 					   paramValue=value.nextToken();
 				   }
 				   params.put(param,paramValue);
 		 
 			   }
 			}
 			if(url.indexOf("?")!=-1){
 			   return  url.substring(0,url.indexOf("?"));
 			}
 			return url;		   
 	}
 	/**
 	 * This method splits the header params
 	 * @param headerParams
 	 * @return param
 	 */
 	public String[] splitHeaderParams(String headerParams){
 		int indexToSplit=headerParams.indexOf(":");
 		String[] param=new String[2];
 		param[0]=headerParams.substring(0,indexToSplit).trim();
 		param[1]=headerParams.substring(indexToSplit+1).trim();
 		return param;
 	}
 	/**
 	 * This method forms the http response
 	 * @param responseCode
 	 * @param httpHeader
 	 * @param content
 	 * @return
 	 */
 	private byte[] formHttpResponseHeader(int responseCode,HttpHeaderServer httpHeader,byte[] content,HashMap<String,HttpCookie> httpCookies){
 		StringBuffer buffer=new StringBuffer();
 		String colon=": ";
 		String crlf="\r\n";
 		if(responseCode==200){
 			buffer.append("HTTP/1.1 200 OK");
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.DATE);
 			buffer.append(colon);
 			buffer.append(httpHeader.getDate());		
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.SERVER);
 			buffer.append(colon);
 			buffer.append(httpHeader.getServer());		
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.CONTENT_TYPE);
 			buffer.append(colon);
 			buffer.append(httpHeader.getContentType());
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.CONTENT_LENGTH);
 			buffer.append(colon);
 			buffer.append(httpHeader.getContentLength());
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.LAST_MODIFIED);
 			buffer.append(colon);
 			buffer.append(httpHeader.getLastModified());
 			if(httpCookies!=null){
 				Iterator<String> cookieNames=httpCookies.keySet().iterator();
 				HttpCookie httpCookie;
 				for(;cookieNames.hasNext();){
 					String cookieName=cookieNames.next();
 						httpCookie=(HttpCookie) httpCookies.get(cookieName);
 						buffer.append(crlf);
 						buffer.append(HttpHeaderServerParamNames.SETCOOKIE);
 						buffer.append(colon);
 						buffer.append(httpCookie.getKey());
 						buffer.append("=");
 						buffer.append(httpCookie.getValue());
 						if(httpCookie.getExpires()!=null){
 							buffer.append("; ");
 							buffer.append(HttpHeaderServerParamNames.SETCOOKIEEXPIRES);
 							buffer.append("=");
 							buffer.append(httpCookie.getExpires());
 						}
 				}
 			}			
 		}
 		else if(responseCode==404){
 			buffer.append("HTTP/1.1 404 Not Found");
 		}
 		buffer.append(crlf);
 		buffer.append(crlf);
 		byte[] byt1=buffer.toString().getBytes();
 		byte[] byt2=new byte[byt1.length+content.length+crlf.length()*2];
 		/////System.out.println("Header="+new String(byt1));
 		for(int count=0;count<byt1.length;count++){
 			byt2[count]=byt1[count];
 		}
 		for(int count=0;count<content.length;count++){
 			byt2[count+byt1.length]=content[count];
 		}
 		for(int count=0;count<crlf.length()*2;count++){
 			byt2[count+byt1.length+content.length]=(byte)crlf.charAt(count%2);
 		}
 		////System.out.println("Header with content="+new String(byt2));
 		/*try {
 			buffer.append(new String(content, "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		return byt2;
 	}
 	
 	/**
 	 * This methods obtains the stream bytes from the file
 	 * @param filepath
 	 * @return
 	 */
 	private byte[] obtainContent(String filepath){
 		byte[] filebt = null;
 		try {
 			File file=new File(filepath);			
 			FileInputStream stream=new FileInputStream(filepath);
 			filebt=new byte[(int) file.length()];
 			stream.read(filebt,0,(int)file.length());
 			stream.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			filebt=null;
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			filebt=null;
 			e.printStackTrace();
 		}
 		catch(Exception ex){
 			filebt=null;
 			ex.printStackTrace();
 		}
 		return filebt;
 		
 	}
 	
 	public void addURL(URL url,WebClassLoader customClassLoader) throws IOException {
 			try {
 			  customClassLoader.addURL(url);
 		  } catch (Throwable t) {
 		     t.printStackTrace();
 		  }//end try catch
 	}//end method
 	public interface WebServerRequestProcessorMBean{
 			public int getNumberOfRequests();
 	}
 	public interface WebServerHttpsRequestProcessorMBean{
 		public int getNumberOfRequests();
 }
 	
 	class WebServerHttpsRequestProcessor extends Thread implements Runnable,WebServerHttpsRequestProcessorMBean{
 		String deployDirectory;
 		int shutdownPort;
 		HashMap urlClassLoaderMap;
 		int numberOfRequests=0;
 		int instanceNumber;
 		HashMap cache=new HashMap();
 		int requestPort;
 		HashMap sessionObjects=new HashMap();
 		String certstore;
 		String passcode;
 		public WebServerHttpsRequestProcessor(){
 			
 		}
 		public WebServerHttpsRequestProcessor(HashMap urlClassLoaderMap,int requestPort,String deployDirectory,int shutdownPort,String certstore,String passcode,int instanceNumber){
 			this.requestPort=requestPort;
 			this.deployDirectory=deployDirectory;
 			this.shutdownPort=shutdownPort;
 			this.urlClassLoaderMap=urlClassLoaderMap;
 			this.instanceNumber=instanceNumber;
 			this.certstore=certstore;
 			this.passcode=passcode;
 		}
 		public void run(){
 			
 		SSLServerSocket sslServerSocket = null;
 			Socket sock=new Socket();
 			try {
				System.setProperty("javax.net.ssl.keyStore", certstore);
				System.setProperty("javax.net.ssl.keyStorePassword", passcode);
 				SSLServerSocketFactory factory =
 						(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
 					System.out.println("factory created");
 					
 					sslServerSocket = (SSLServerSocket) factory.createServerSocket(requestPort);
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			Digester serverdigester = null;
 			try {
 				serverdigester = DigesterLoader.createDigester(new InputSource(new FileInputStream("./config/executorconfig-rules.xml")));
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			//java.util.concurrent.ExecutorService executor = new java.util.concurrent.ScheduledThreadPoolExecutor(1000);
 			while(true){
 				
 				try{
 					
 
 					
 					sock=sslServerSocket.accept();
 					System.out.println("A new Socket="+sock);
 					//System.out.println("A new Socket1="+sock);
 					WebServer webserver=new WebServer(sock,this.requestPort,this.deployDirectory,this.shutdownPort,serverdigester,urlClassLoaderMap);
 					//System.out.println("starting web request");
 					//executor.execute(webserver);
 					webserver.start();
 					//System.out.println("starting web request1");
 					numberOfRequests++;
 				}
 				catch(Exception ex){
 					try {
 						socket = new Socket("localhost",shutdownPort);
 						OutputStream outputStream=socket.getOutputStream();
 						outputStream.write("shutdown WebServer\r\n\r\n".getBytes());
 						outputStream.close();
 					} catch (IOException e1) {
 						
 						e1.printStackTrace();
 					}	
 				}
 			}
 		}
 		@Override
 		public int getNumberOfRequests() {
 			// TODO Auto-generated method stub
 			return numberOfRequests;
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * This class processes the http request 
 	 * @author arun
 	 *
 	 */
 	class WebServerRequestProcessor extends Thread implements Runnable,WebServerRequestProcessorMBean{
 		int requestPort;
 		String deployDirectory;
 		int shutdownPort;
 		HashMap urlClassLoaderMap;
 		int numberOfRequests=0;
 		int instanceNumber;
 		ServerSocketChannel serverSocketChannel;
 		public WebServerRequestProcessor(){
 			
 		}
 		public WebServerRequestProcessor(HashMap urlClassLoaderMap,ServerSocketChannel serverSocketChannel,String deployDirectory,int shutdownPort,int instanceNumber){
 			this.requestPort=requestPort;
 			this.deployDirectory=deployDirectory;
 			this.shutdownPort=shutdownPort;
 			this.urlClassLoaderMap=urlClassLoaderMap;
 			this.instanceNumber=instanceNumber;
 			this.serverSocketChannel=serverSocketChannel;
 		}
 		
 		public void run(){
 			String CLIENTCHANNELNAME = "clientChannel"+instanceNumber;
 		    String SERVERCHANNELNAME = "serverChannel"+instanceNumber;
 		    String channelType = "channelType"+instanceNumber;
 		    byte[] response;
 			byte[] content;
 			byte[] uploadData = null;
 			HttpHeaderClient httpHeaderClient;
 			InputStream istream = null;
 			OutputStream ostream = null;
 			HttpHeaderServer serverParam=new HttpHeaderServer();
 			String value;
 			char c;
 			String endvalue="\r\n\r\n";
 			String urlFormEncoded;
 			int responseCode;
         	HashMap clientkey=new HashMap();
         	Digester serverdigester = null;
 			try {
 				serverdigester = DigesterLoader.createDigester(new InputSource(new FileInputStream("./config/executorconfig-rules.xml")));
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		    try{
 
 		 
 		        
 		        Selector selector = Selector.open();
 		 
 		        
 		        SelectionKey socketServerSelectionKey = serverSocketChannel.register(selector,
 		                SelectionKey.OP_ACCEPT);
 		        
 		       // logger.info("Instance Number"+instanceNumber);
 		        
 		        Map<String, String> properties = new HashMap<String, String>();
 		        properties.put(channelType, SERVERCHANNELNAME);
 		        socketServerSelectionKey.attach(properties);
 		        for (;;) {
 		        	try{
 		        		
 		        		if (selector.select(10000)== 0)
 			                continue;
 			            Set<SelectionKey> selectedKeys = selector.selectedKeys();
 			            Iterator<SelectionKey> iterator = selectedKeys.iterator();
 			            while (iterator.hasNext()) {
 			                SelectionKey key = iterator.next();
 			                //logger.info("instanceName"+instanceNumber+" ::: "+key);
 			                if (((Map) key.attachment()).get(channelType).equals(
 			                		SERVERCHANNELNAME)) {
 			                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
 			                            .channel();
 			                    SocketChannel clientSocketChannel = serverSocketChannel
 			                            .accept();
 			 
 			                    if (clientSocketChannel != null) {
 			                        // set the client connection to be non blocking
 			                        clientSocketChannel.configureBlocking(false);
 			                        SelectionKey clientKey = clientSocketChannel.register(
 			                                selector, SelectionKey.OP_READ,
 			                                SelectionKey.OP_WRITE);
 			                        Map<String, String> clientproperties = new HashMap<String, String>();
 			                        clientproperties.put(channelType, CLIENTCHANNELNAME);
 			                        clientKey.attach(clientproperties);
 			                        clientKey.interestOps(SelectionKey.OP_READ);
 			                    }
 			 
 			                } else {
 			                    // data is available for read
 			                    // buffer for reading
 			                    ByteBuffer buffer = ByteBuffer.allocate(20);
 			                    //System.out.println("read="+key);;
 			                    SocketChannel clientChannel = (SocketChannel) key.channel();
 			                    int bytesRead = 0;
 			                    ByteArrayOutputStream headerData=new ByteArrayOutputStream();
 			                    int totalBytRead=0;
 			                    if (key.isReadable()) {
 			                        // the channel is non blocking so keep it open till the
 			                        // count is >=0
 			                    	//System.out.println(key);
 			                    	//System.out.println(clientChannel);
 			                        while ((bytesRead = clientChannel.read(buffer)) > 0) {
 			                            buffer.flip();
 			                            //System.out.println(Charset.defaultCharset().decode(
 			                              //      buffer));
 			                            headerData.write(buffer.array(),0,bytesRead);
 			                            buffer.clear();
 			                            totalBytRead+=bytesRead;
 			                        }
 			                        if(totalBytRead==0)continue;
 			                        //System.out.println("totalBytRead"+totalBytRead);
 				                   // System.out.println("ClientChannel="+clientChannel);
 			                        byte[] bt=headerData.toByteArray();
 			                       // System.out.println(new String(bt));
 			                        int index=containbytes(bt,endvalue.getBytes());
 			                       // System.out.println("index="+index);
 			            			if(index==-1)index=totalBytRead;
 			            			value=new String(ObtainBytes(bt,0,index));
 			            			//System.out.println("value2="+value);
 			            			httpHeaderClient=parseHttpHeaders(value);
 			            			//System.out.println(httpHeaderClient.getResourceToObtain());
 			            			HashMap<String,HttpCookie> httpCookies=httpHeaderClient.getCookies();
 			            			HttpSession session=null;
 			            			if(httpCookies!=null){
 			            				Iterator<String> cookieNames=httpCookies.keySet().iterator();
 			            				for(;cookieNames.hasNext();){
 			            					String cookieName=cookieNames.next();
 			            					System.out.println(cookieName+" "+httpCookies.get(cookieName).getValue());
 			            					if(cookieName.equals("SERVERSESSIONID")){
 			            						session=(HttpSession) sessionObjects.get(httpCookies.get(cookieName).getValue());
 			            						httpHeaderClient.setSession(session);
 			            						//break;
 			            					}
 			            				}
 			            			}
 			            			//System.out.println("Session="+session);
 			            			if(session==null){
 			            				HttpCookie cookie=new HttpCookie();
 			            				cookie.setKey("SERVERSESSIONID");
 			            				cookie.setValue(UUID.randomUUID().toString());
 			            				httpCookies.put("SERVERSESSIONID",cookie);
 			            				session=new HttpSession();
 			            				sessionObjects.put(cookie.getValue(), session);
 			            				httpHeaderClient.setSession(session);
 			            			}
 			            			if(httpHeaderClient.getContentType()!=null&&
 			            					httpHeaderClient.getContentType().equals(HttpHeaderParamNames.MULTIPARTFORMDATAVALUE)){
 			            					uploadData=ObtainBytes(bt,index+endvalue.length(),index+endvalue.length()+Integer.parseInt(httpHeaderClient.getContentLength()));
 			            					////System.out.println(new String(uploadData));
 			            					HashMap paramMap=new MultipartFormData().parseContent(uploadData,httpHeaderClient);
 			            					httpHeaderClient.setParameters(paramMap);
 			            					////logger.info(uploadData);
 			            			}
 			            			else if(httpHeaderClient.getContentType()!=null&&httpHeaderClient.getContentType().equals(HttpHeaderParamNames.URLENCODED)){
 			            					urlFormEncoded=new String(ObtainBytes(bt,index+endvalue.length(), index+endvalue.length()+Integer.parseInt(httpHeaderClient.getContentLength())));
 			            					HashMap paramMap=parseUrlEncoded(urlFormEncoded);
 			            					httpHeaderClient.setParameters(paramMap);
 			            			}
 			            			////logger.info(serverconfig.getDeploydirectory()+httpHeaderClient.getResourceToObtain());
 			            			////System.out.println("value3=");
 			            			
 			            			////logger.info(new String(bt));
 			            			serverParam.setContentType("text/html");
 			            			URLDecoder decoder=new URLDecoder();
 			            			////System.out.println("content Length= "+socket);
 			            			responseCode=200;
 			            			File file=new File(deployDirectory+decoder.decode(httpHeaderClient.getResourceToObtain()));
 			            			FileContent fileContent=(FileContent) cache.get(httpHeaderClient.getResourceToObtain());
 			            			if(fileContent!=null&&file.lastModified()==fileContent.getLastModified()){
 			            				//System.out.println("In cache");
 			            				content=(byte[]) fileContent.getContent();
 			            			}
 			            			else{
 				            			content=ObtainContentExecutor(deployDirectory,httpHeaderClient.getResourceToObtain(),httpHeaderClient,serverdigester,urlClassLoaderMap);
 				            			////System.out.println("content Length2= ");
 				            			if(content==null){
 				            				//System.out.println("In caching content");
 				            				content=obtainContent(deployDirectory+decoder.decode(httpHeaderClient.getResourceToObtain()));
 				            				fileContent=new FileContent();
 				            				fileContent.setContent(content);
 				            				fileContent.setFileName(httpHeaderClient.getResourceToObtain());
 				            				fileContent.setLastModified(file.lastModified());
 				            				cache.put(httpHeaderClient.getResourceToObtain(),fileContent);
 				            			}
 				            			////System.out.println("value4=");
 				            			
 			            			}
 			            			
 			            			if(content==null){
 			            				responseCode=404;
 			            				content=("<html><body><H1>The Request resource "+httpHeaderClient.resourceToObtain+" Not Found</H1><body></html>").getBytes();
 			            			}
 			            			////System.out.println("content Length3= ");
 			            			serverParam.setContentLength(""+(content.length+4));
 			            			if(httpHeaderClient.getResourceToObtain().endsWith(".ico")){
 			            				serverParam.setContentType("image/png");
 			            			}
 			            			////System.out.println("value5=");
 	
 			            			////System.out.println("content Length4= ");
 			            			response=formHttpResponseHeader(responseCode,serverParam,content,httpHeaderClient.getCookies());
 			            			////System.out.println("value6=");
 			            			//logger.info("Response="+new String(response));
 			                        
 			                        
 			                        if (bytesRead < 0) {
 			                        	System.out.println("bytesRead as 0");
 			                            // the key is automatically invalidated once the
 			                            // channel is closed
 			                        	key.cancel();
 			                        	clientChannel.close();
 			                        }
 			                        else{
 			                        	clientkey.put(key, response);		                        	
 			                        }
 			                        key.interestOps(SelectionKey.OP_WRITE);
 			                    }
 			                    else if(key.isWritable()){
 			                    	// the channel is non blocking so keep it open till the
 			                        // count is >=0
 			                    	 //System.out.println("write="+key);;
 			                    	response=(byte[]) clientkey.get(key);
 			                    	ByteBuffer responseBuffer=ByteBuffer.wrap(response);
 			                        while (responseBuffer.hasRemaining()) {
 			                        	clientChannel.write(responseBuffer);
 			                        }
 			                        key.cancel();
 			                        //key.interestOps(SelectionKey.OP_READ);
 			                        clientChannel.close();
 			                    }
 			 
 			                }
 			 
 			                // once a key is handled, it needs to be removed
 			                iterator.remove();
 			 
 			            }
 		        	}
 		        	catch(Exception ex){
 		        		logger.error("Error in socket channel", ex);
 			        }
 		        }
 		    }
 		    catch(Exception ex){
 		    	logger.error("Error in socket channel", ex);
 		    }
 		}
 		
 		
 		/**
 		 * This method start the webserver thread for each http 
 		 * request 
 		 */
 		/*public void run(){
 			
 			ServerSocket serverSocket = null;
 			Socket sock=new Socket();
 			try {
 				serverSocket=new ServerSocket(this.requestPort);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			Digester serverdigester = null;
 			try {
 				serverdigester = DigesterLoader.createDigester(new InputSource(new FileInputStream("./config/executorconfig-rules.xml")));
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			java.util.concurrent.ExecutorService executor = new java.util.concurrent.ScheduledThreadPoolExecutor(1000);
 			while(true){
 				
 				try{
 					//System.out.println("A new Socket="+sock);
 					sock=serverSocket.accept();
 					//System.out.println("A new Socket1="+sock);
 					WebServer webserver=new WebServer(sock,this.requestPort,this.deployDirectory,this.shutdownPort,serverdigester,urlClassLoaderMap);
 					//System.out.println("starting web request");
 					executor.execute(webserver);
 					//webserver.start();
 					//System.out.println("starting web request1");
 					numberOfRequests++;
 				}
 				catch(Exception ex){
 					try {
 						socket = new Socket("localhost",shutdownPort);
 						OutputStream outputStream=socket.getOutputStream();
 						outputStream.write("shutdown WebServer\r\n\r\n".getBytes());
 						outputStream.close();
 					} catch (IOException e1) {
 						
 						e1.printStackTrace();
 					}	
 				}
 			}
 		}*/
 		/**
 		 * This method returns the number of http requests.
 		 */
 		@Override
 		public int getNumberOfRequests() {
 			// TODO Auto-generated method stub
 			return numberOfRequests;
 		}
 	}
 }
 
   
