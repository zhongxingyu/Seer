 package org.vaadin.mideaas.frontend;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.*;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.vaadin.mideaas.model.SharedProject;
 import org.vaadin.mideaas.model.UserSettings;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.vaadin.server.ExternalResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Link;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Button.ClickEvent;
 
 import fi.jasoft.qrcode.QRCode;
 
 @SuppressWarnings("serial")
 public class Deployer extends CoapsCaller  {
 
 	public interface DeployListener {
 		public void networkingStarted(String msg);
 		public void networkingFinished(boolean success, String msg);
 		public void networkingCancelled(String msg);
 	}
 	
 	private ExecutorService executor = Executors.newSingleThreadExecutor();
 	private static DefaultHttpClient httpclient = new DefaultHttpClient();
 	private String date;
 	private static String paasApiUrl = "http://130.230.142.89:8080/CF-api/rest/";
 	private String memory;
 	private String deployLocation;
 	private String warLocation;
 	private String appName;
 	private String warName;
 	private CopyOnWriteArrayList<DeployListener> listeners = new CopyOnWriteArrayList<>();
 	private String pathToWar;
 
     Deployer(String pathToWar){
     	this.pathToWar = pathToWar;
     	File file = new File(pathToWar);
    	String warName = file.getName();
    	String warLocation = file.getParentFile().getAbsolutePath();
 		appName = warName.replace(".war", "");
 
         deployLocation = "/home/ubuntu/delpoyedprojects";
         memory = "256";
         Date today = new Date();
         date = new SimpleDateFormat("yyyy-MM-dd").format(today);
     }
         
 	public static ClientResponse findApplications() {
 		return Deployer.findApplications(createClient());
 	}
 
     public static String deleteApplications(){
     	String url = paasApiUrl+"app/delete";
     	HttpDelete delete = new HttpDelete(url);
 		try {
 			CloseableHttpResponse response = httpclient.execute(delete);
         	return response.getStatusLine().getStatusCode()+"\n"+getXML(response);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return e.toString();
 		}
     }
         
 	public static ClientResponse createApplication(String manifest) {
 		return Deployer.createApplication(createClient(), manifest);
 	}
         
 	public static ClientResponse deployApplication(String envId,
 			String appId, String fileName) throws URISyntaxException, FileNotFoundException{
 		try{
 			return Deployer.deployApplication(createClient(), envId, appId, fileName);
 		}catch(FileNotFoundException e){
 			throw e;
 		}
 	}
 		
 	public static ClientResponse startApplication(String appId) {
 		return CoapsCaller.startApplication(createClient(), appId);
 	}
 		
 	public static ClientResponse restartApplication(String appId) {
 		ClientResponse cr = createClient().path("app/" + appId + "/restart")
 				.type(MediaType.APPLICATION_XML).post(ClientResponse.class);
 		return cr;
 	}
 
 	public static ClientResponse stopApplication(String appId) {
 		return CoapsCaller.stopApplication(createClient(), appId);
 	}
 	
     public static String deleteApplication(String appId){    	
     	String deleteurl = paasApiUrl + "app/" + appId + "/delete";
     	HttpDelete delete = new HttpDelete(deleteurl);
 		try {
 			CloseableHttpResponse response = httpclient.execute(delete);
         	return response.getStatusLine().getStatusCode()+"\n"+getXML(response);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return e.toString();
 		}        	
     }
     
     //removes environments
     public static boolean deleteEnvironments(LogView logView){
     	String url = paasApiUrl+"environment";
     	HttpGet get = new HttpGet(url);        	
 		try {
 			logView.newLine("gets environments");
 			CloseableHttpResponse response = httpclient.execute(get);
 			String string = getXML(response);
 			logView.newLine(string);
 			ArrayList<Integer> environmentIDs = getIndexes(string);
 			logView.newLine("parsed:");
 			logView.newLine(environmentIDs.toString());
 			for (int i : environmentIDs){
 				removeEnvironment(i,logView);
 			}
         	return true;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
     }
 
 	public static ClientResponse createEnvironment(String manifest) {
 		return CoapsCaller.createEnvironment(createClient(), manifest);
 	}
     
     private static void removeEnvironment(int i, LogView logView) {
 		logView.newLine("removes: " + i);
     	String url = paasApiUrl+"environment";
     	HttpDelete delete = new HttpDelete(url+"/" + i);
     	CloseableHttpResponse response;
 		try {
 			response = httpclient.execute(delete);
 			String string = response.getStatusLine().getStatusCode()+"\n"+getXML(response);
 			logView.newLine(string);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			logView.newLine(e.getMessage());
 		}					
 	}
 
 	//finds indexes from string
     private static ArrayList<Integer> getIndexes(String string) {
     	ArrayList<Integer> indexes = new ArrayList<Integer>();
     	while(string.contains("<id>")&&string.contains("</id>")){
     		int from = string.indexOf("<id>")+4;
     		int to = string.indexOf("</id>");
     		String id = string.substring(from, to);
     		indexes.add(Integer.parseInt(id));
     		string = string.substring(to+5);
     	}
     	return indexes;
 	}        
 
     private static String createManifest(String appName, String warName,
                     String warLocation, String deployLocation, String date, String memory) {
             String xml = ""+
     		"<?xml version=\"1.0\" encoding=\"UTF8\"?>\n"+
             "<paas_application_manifest name=\"" + appName +"Manifest\">\n"+
             "<description>This manifest describes a " + appName + ".</description>\n"+
             "	<paas_application name=\"" + appName + "\"  environement=\"JavaWebEnv\">\n"+
             "		<description>"+appName+" description.</description>\n"+
             "		<paas_application_version name=\"version1.0\" label=\"1.0\">\n"+
 //                "			<paas_application_deployable name=\""+warName+"\" content_type=\"artifact\" location=\""+deployLocation+"\" multitenancy_level=\"SharedInstance\"/>\n"+
 			"			<paas_application_deployable name=\""+warName+"\" content_type=\"artifact\" location=\""+ warLocation +"\" multitenancy_level=\"SharedInstance\"/>"+
             "			<paas_application_version_instance name=\"Instance1\" initial_state=\"1\" default_instance=\"true\"/>\n"+
             "		</paas_application_version>\n"+
             "	</paas_application>\n"+
             "	<paas_environment name=\"JavaWebEnv\" template=\"TomcatEnvTemp\">\n"+			
             "		<paas_environment_template name=\"TomcatEnvTemp\" memory=\"" + memory + "\">\n"+
             "  		  <description>TomcatServerEnvironmentTemplate</description>\n"+
             "		  <paas_environment_node content_type=\"container\" name=\"tomcat\" version=\"\" provider=\"CF\"/>\n"+
             "		  <paas_environment_node content_type=\"database\" name=\"mysql\" version=\"\" provider=\"CF\"/>\n"+
             "		</paas_environment_template>\n"+
             "	</paas_environment>\n"+
             "</paas_application_manifest>\n";                		
             return xml;
     }
 
     private static String getXML(HttpResponse response) {
             ByteArrayOutputStream outstream = new ByteArrayOutputStream();
             try {
                     response.getEntity().writeTo(outstream);
             } catch (IOException e) {
                     return "IOException";
             }
             byte [] responseBody = outstream.toByteArray();
             String responseBodyString = new String(responseBody);
             return responseBodyString;
     }
     
     static String parseUrl(String response) {
             String seekString = "<uri>";
             int startIndex = response.indexOf(seekString)+seekString.length();
             if (startIndex==-1){return "-1";}
             response = response.substring(startIndex);
             seekString = "</uri>";
             int endIndex = response.indexOf(seekString);
             if (endIndex==-1){return "-1";}
             String uriToService = response.substring(0, endIndex);
             if (!uriToService.startsWith("http")){
         		uriToService = "http://" + uriToService;
         	}
             return uriToService;
     }
 
     //should parse appID number from XML... could also be done with somekind of xmlparser :)
     static String parseAppID(String response) {
             String seekString = "appId=\"";
             int startIndex = response.indexOf(seekString)+seekString.length();
             if (startIndex==-1){return "-1";}
             response = response.substring(startIndex);
             seekString = "\"";
             int endIndex = response.indexOf(seekString)+seekString.length();
             if (endIndex==-1){return "-1";}
             String appId = response.substring(0, endIndex-1);
             return appId;
     }
     
 
 
     //should parse envID number from XML... could also be done with somekind of xmlparser :)
     static String parseEnvID(String response) {
             String seekString = "envId=\"";
             int startIndex = response.indexOf(seekString)+seekString.length();
             if (startIndex==-1){return "-1";}
             response = response.substring(startIndex);
             seekString = "\"";
             int endIndex = response.indexOf(seekString)+seekString.length();
             if (endIndex==-1){return "-1";}
             String envId = response.substring(0, endIndex-1);
             return envId;
     }
 
     private HttpResponse makePostRequest(String urlString, String requestXML,File file) throws IOException{
             
             HttpPost post = new HttpPost(urlString);
 
             //create new post with xml data
             if (requestXML!=null){
                     StringEntity data = new StringEntity(requestXML);
                     data.setContentType("application/xml");
                     post.setEntity(data);
             }
             //make post
             return httpclient.execute(post);
     }
 
     public String formPost(String deployLocation, File file) {
             try {
                     HttpResponse response = makePostRequest(deployLocation,null,file);
                     HttpEntity entity = response.getEntity();
                     String value = EntityUtils.toString(entity);
                     return value;
             } catch (IOException e) {
                     String output = e.toString(); 
                     return output;
             }
             
     }
 
 	public String getManifest(String pathToWar) {
 		// TODO Auto-generated method stub
 		return createManifest(appName,warName, warLocation, deployLocation, date,memory);
 	}
 
 	public static ArrayList<Object[]> createRows(String responseString,final CFAppsView cfAppsView, final LogView logView) {
 		String[] splittedResponse = responseString.split("<application>") ;
 		ArrayList<Object[]> apps = new ArrayList<Object[]>();
 		for (String split:splittedResponse){
 			final String idstring = parse(split,"id");
 			String name = parse(split,"name");
 			String description = parse(split,"description");
 			final String uri = parse(split,"uri");
 			if (idstring!=null){
 				Button button = new Button("show more");
 				button.addClickListener(new Button.ClickListener() {				
 					@Override
 					public void buttonClick(ClickEvent event) {
 						WebResource client = Deployer.createClient();
 						ClientResponse response = Deployer.describeApplication(client, idstring);
 						String responseString = response.getEntity(new GenericType<String>(){});
 				        CFAppView view = new CFAppView(idstring, responseString,cfAppsView,logView);
 				        Window w = new Window("app");
 						w.center();
 						w.setWidth("80%");
 						w.setHeight("80%");
 						w.setContent(view);
 						view.setSizeFull();
 						UI.getCurrent().addWindow(w);
 					}	
 				});
 
 				Integer id = new Integer(Integer.parseInt((String) idstring));
 				Object[] app = new Object[] {id, name, description, uri,button};
 				apps.add(app);					
 			}
 		}
 		return apps;
 	}
 
 	private static String parse(String txt, String tag) {
 		String startTag = "<" + tag + ">";
         int startIndex = txt.indexOf(startTag);
         if (startIndex==-1){return null;}
         startIndex+=+startTag.length();
         txt = txt.substring(startIndex);
         String endTag = "</" + tag + ">";
         int endIndex = txt.indexOf(endTag);
         if (endIndex==-1){return null;}
         return txt.substring(0, endIndex);
 	}
 	
 	public synchronized void addDeployListener(DeployListener li) {
 		listeners.add(li);
 	}
 	
 	public synchronized void removeDeployListener(DeployListener li) {
 		listeners.remove(li);
 	}
 
 	public void deploy(UserSettings settings, SharedProject project, LogView logView,Link link, QRCode qrCode){
 		doDeploy(settings, project, logView,link, qrCode);
 	}
 	
 	private void runAsync(Runnable runnable){
 		synchronized (this) {
 			executor.submit(runnable);
 		}
 	}
 
 	private void doDeploy(UserSettings settings, SharedProject project, final LogView logView,final Link link, final QRCode qrCode) {		
 		final String file;
 		//this is not working yet
 		if (settings.compileGae){
 			File f = new File(project.getProjectDir().getAbsolutePath(), "hellotest.tar.gz");
 			file = f.getAbsolutePath();
 			//package as tar
 			//somehow deploy it :)
 		}else{
 			//deploys war over cf-api
 			file = pathToWar;
 		}
 		Deployer apiClient = new Deployer(file);
 		
 		final String manifest = apiClient.getManifest(file);
 		logView.newLine("Manifest: " + manifest);
 
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				fireNetworkingStarted("deploying application");
 				logView.newLine("create environment");
 				ClientResponse envresponse = Deployer.createEnvironment(manifest);
 				String envresponsestring = envresponse.getEntity(new GenericType<String>(){});
 				logView.newLine("response: " + envresponsestring);
 				String envId = Deployer.parseEnvID(envresponsestring);
 				logView.newLine("create app");
 				ClientResponse appresponse = Deployer.createApplication(manifest);
 				String appresponsestring = appresponse.getEntity(new GenericType<String>(){});
 				logView.newLine("response: " + appresponsestring);
 				String appId = Deployer.parseAppID(appresponsestring);
 				
 				String uriToService="";
 				logView.newLine("deploy app");
 				ClientResponse deployresponse = null;
 				try {
 					deployresponse = Deployer.deployApplication(envId, appId, file);
 				} catch (FileNotFoundException e) {
 					logView.newLine("exception: " + e.getMessage());
 					//return false;				
 				} catch (URISyntaxException e) {
 					// TODO Auto-generated catch block
 					logView.newLine("exception: " + e.getMessage());
 					e.printStackTrace();
 					//return false;
 				}
 				String deployresponsestring = deployresponse.getEntity(new GenericType<String>(){});
 				logView.newLine("response: " + deployresponsestring);
 				uriToService = Deployer.parseUrl(deployresponsestring);
 				logView.newLine("uri: " + uriToService);
 				logView.newLine("start app");
 				ClientResponse startresponse = Deployer.startApplication(appId);
 				String startresponsestring = startresponse.getEntity(new GenericType<String>(){});
 				logView.newLine("response: " + startresponsestring);
 		
 				//TODO show path;
 		        if (uriToService.length()>0){
 		    		link.setResource(new ExternalResource(uriToService));
 		    		link.setVisible(true);
 		    		
 		    		qrCode.setValue(uriToService);
 		    		qrCode.setVisible(true);
 					fireNetworkingFinished(true, "Deploying successed");
 		        }else{
 					fireNetworkingFinished(false, "Deploying failed");
 		        }
 			}
 		};
 		runAsync(runnable);
 	}
 
 	private void fireNetworkingFinished(boolean success, String msg) {
 		// Doesn't need to fire in a different thread because this is always
 		// triggered by a background thread, never a Vaadin UI server visit.
 		for (DeployListener li : listeners) {
 			li.networkingFinished(success,msg);
 		}
 	}
 
 	private void fireNetworkingStarted(String msg) {
 		for (DeployListener li : listeners) {
 			li.networkingStarted(msg);
 		}
 	}
 
 	public void cancel() {
 		synchronized (this) {
 			executor.shutdownNow();
 			executor = Executors.newSingleThreadExecutor();
 		}
 	}	
 }
