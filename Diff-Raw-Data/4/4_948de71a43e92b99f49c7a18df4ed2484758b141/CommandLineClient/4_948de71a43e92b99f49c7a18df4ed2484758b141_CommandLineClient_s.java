 package org.amplafi.flow.utils;
 
 import static org.amplafi.flow.utils.CommandLineClientOptions.API_KEY;
 import static org.amplafi.flow.utils.CommandLineClientOptions.API_VERSION;
 import static org.amplafi.flow.utils.CommandLineClientOptions.DESCRIBE;
 import static org.amplafi.flow.utils.CommandLineClientOptions.FLOW;
 import static org.amplafi.flow.utils.CommandLineClientOptions.FORMAT;
 import static org.amplafi.flow.utils.CommandLineClientOptions.HOST;
 import static org.amplafi.flow.utils.CommandLineClientOptions.PARAMS;
 import static org.amplafi.flow.utils.CommandLineClientOptions.PORT;
 
 import java.net.URI;
 
 import org.amplafi.flow.definitions.FarReachesServiceInfo;
 import org.amplafi.flow.definitions.FlowRequestDescription;
 import org.amplafi.json.JSONArray;
 import org.amplafi.json.JSONException;
 import org.amplafi.json.JSONObject;
 import org.amplafi.json.JsonConstruct;
 import org.apache.commons.cli.ParseException;
 
 /**
  * A Java CL client used to query FarReaches services. For usage options, please refer to {@link CommandLineClientOptions}.
  * 
  * @author Tyrinslys Valinlore
  * @author Haris Osmanagic
  */
 
 public class CommandLineClient implements Runnable {
 	private static final int INDENTATION_LEVEL = 5;
 
 	private String apiKey;
 	
 	private FarReachesServiceInfo serviceInfo;
 	private FlowRequestDescription flowRequestDescription;
 	
 	private boolean doFormatOutput;
 	
 	public static void main(String[] args) {
 		CommandLineClientOptions cmdOptions = null;
 		
 		try {
 			cmdOptions = new CommandLineClientOptions(args);
 		} catch (ParseException e) {
 			System.err.println("Could not parse passed arguments, message:");
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		String apiKey = cmdOptions.getOptionValue(API_KEY);
 		
 		FarReachesServiceInfo serviceInfo = new FarReachesServiceInfo(
 				cmdOptions.getOptionValue(HOST),
 				cmdOptions.getOptionValue(PORT),
 				cmdOptions.getOptionValue(API_VERSION));
 		
 		FlowRequestDescription flowRequestDescription = new FlowRequestDescription(cmdOptions.getOptionValue(FLOW), 
 				cmdOptions.hasOption(DESCRIBE), cmdOptions.getOptionProperties(PARAMS));
 
 		CommandLineClient client = new CommandLineClient(apiKey, serviceInfo, flowRequestDescription, cmdOptions.hasOption(FORMAT));
 		client.run();
 	}
 
 	public CommandLineClient(String apiKey, FarReachesServiceInfo serviceInfo,
 			FlowRequestDescription flowRequest, boolean doFormatOutput) {
 		
 		this.apiKey = apiKey;
 		this.serviceInfo = serviceInfo;
 		this.flowRequestDescription = flowRequest;
 		this.doFormatOutput = doFormatOutput;
 	}
 
 	private String buildBaseUriString() {
 		String fullUri =  this.serviceInfo.getHost()  
 				+ ":" + this.serviceInfo.getPort() + "/c/"
 				+ this.apiKey 
 				+ "/" + this.serviceInfo.getApiVersion(); 
 		
 		return fullUri;
 	}
 	
 	public void run() {
 		GeneralFlowRequest flowRequest = new GeneralFlowRequest(URI.create(buildBaseUriString()), this.flowRequestDescription.getFlowName(), this.flowRequestDescription.getParameters());;
 		JsonConstruct result = null;
 
 		if (flowRequestDescription.isDescribe() && flowRequestDescription.getFlowName() == null) {
 			result = flowRequest.listFlows();
 		} else if (flowRequestDescription.isDescribe() && flowRequestDescription.getFlowName() != null) {
 			result = flowRequest.describeFlow();
 		} else {
 			result = toJsonConstruct(flowRequest.get());
 		}
 
 		String output = formatIfNeeded(result);
 		System.out.println(output);
 	}
 
 	private String formatIfNeeded(JsonConstruct result) {
 		String output = null;
 		
 		if (doFormatOutput) {
			output = result.toString(INDENTATION_LEVEL);
			// output = result.toString();
 		} else {
 			output = result.toString();
 		}
 		
 		return output;
 	}
 
 	private JsonConstruct toJsonConstruct(String string) {
 		JsonConstruct json = null;
 		
 		try {
 			json = new JSONObject(string);
 		} catch (JSONException e) {
 			json = new JSONArray<String>(string);
 		}
 		
 		return json;
 	}
 
 }
