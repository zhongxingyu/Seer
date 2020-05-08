 package com.mymed.controller.core.requesthandler;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
import com.google.gson.JsonParseException;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.reflect.TypeToken;
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.pubsub.PubSubManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.model.data.application.MDataBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.MLogger;
 
 /**
  * Servlet implementation class PubSubRequestHandler
  */
 @MultipartConfig
 @WebServlet("/PublishRequestHandler")
 public class PublishRequestHandler extends AbstractRequestHandler {
 	/* --------------------------------------------------------- */
 	/* Attributes */
 	/* --------------------------------------------------------- */
 	private static final long serialVersionUID = 1L;
 
 	private PubSubManager pubsubManager;
 
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	/**
 	 * @throws ServletException
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public PublishRequestHandler() throws ServletException {
 		super();
 
 		try {
 			pubsubManager = new PubSubManager();
 		} catch (final InternalBackEndException e) {
 			throw new ServletException("PubSubManager is not accessible because: " + e.getMessage());
 		}
 	}
 	
 	/* --------------------------------------------------------- */
 	/* extends AbstractRequestHandler */
 	/* --------------------------------------------------------- */
 	protected Map<String, String> getParameters(final HttpServletRequest request) throws InternalBackEndException {
 		
 		if(!request.getContentType().matches("multipart/form-data.*")){
 			return super.getParameters(request);
 //			throw new InternalBackEndException("PublishRequestHandler should use a multipart request!");
 		}
 		
 		final Map<String, String> parameters = new HashMap<String, String>();
 		try {
 			System.out.println("\nPART size = " + request.getParts().size());
 			for(Part part : request.getParts()){
 				String key = part.getName();
 				Scanner s = new Scanner(part.getInputStream());
 				String value = s.nextLine();    // read filename from stream
 				parameters.put(key, value);
 				System.out.println("\nKEY = " + key);
 				System.out.println("\nVALUE = " + value);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new InternalBackEndException("Error in getting arguments");
 		}
 		
 		if (!parameters.containsKey("code")) {
 			throw new InternalBackEndException("code argument is missing!");
 		}
 		
 		return parameters;
 	}
 
 	/* --------------------------------------------------------- */
 	/* extends HttpServlet */
 	/* --------------------------------------------------------- */
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
 	IOException {
 
 		JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
 		try {
 			final Map<String, String> parameters = getParameters(request);
 			final RequestCode code = requestCodeMap.get(parameters.get("code"));
 
 			switch (code) {
 			case READ:
 			case DELETE:
 			default:
 				throw new InternalBackEndException("PublishRequestHandler(" + code + ") not exist!");
 			}
 
 		} catch (final AbstractMymedException e) {
 			MLogger.getLog().info("Error in doGet operation");
 			MLogger.getDebugLog().debug("Error in doGet operation", e.getCause());
 			message.setStatus(e.getStatus());
 			message.setDescription(e.getMessage());
 		} 
 
 		printJSonResponse(message, response);
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
 			throws ServletException, IOException {
 
 		JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
 		try {
 			final Map<String, String> parameters = getParameters(request);
 			final RequestCode code = requestCodeMap.get(parameters.get("code"));
 			String application, predicates, user, data;
 
 			switch (code) {
 			case CREATE : // PUT
 				message.setMethod("CREATE");
 				if ((application = parameters.get("application")) == null) {
 					throw new InternalBackEndException("missing application argument!");
 				} else if ((predicates = parameters.get("predicate")) == null) {
 					throw new InternalBackEndException("missing predicate argument!");
 				} else if ((user = parameters.get("user")) == null) {
 					throw new InternalBackEndException("missing user argument!");
 				} else if ((data = parameters.get("data")) == null) {
 					throw new InternalBackEndException("missing data argument!");
 				}
 				
 				try {
 					final MUserBean userBean = getGson().fromJson(user, MUserBean.class);
 				
 					final Type dataType = new TypeToken<List<MDataBean>>(){}.getType();
 					final List<MDataBean> dataList = getGson().fromJson(data, dataType);
 					
 					final List<MDataBean> predicatesArray = getGson().fromJson(predicates, dataType);
 
 					// broadcast algorithm
 					int broadcastSize = (int) Math.pow(2, predicatesArray.size());
 					for(int i=1 ; i<broadcastSize ; i++){
 			 			int mask = i;
 			 			String predicate = "";
 			 			int j = 0;
 			 			while(mask > 0){
 			 				if((mask&1) == 1){
 			 					MDataBean element = predicatesArray.get(j);
 			 					predicate += element.getKey() + "%28" + element.getValue() + "%29";
 			 				}
 			 				mask >>= 1;
 			 				j++;
 			 			}
 			 			if(!predicate.equals("")){
 			 				System.out.println("create predicate: " + predicate);
 			 				pubsubManager.create(application, predicate, userBean, dataList);
 			 			}
 			 		}
 
 					message.setDescription("predicate published: " + predicates);
 				} catch (final JsonSyntaxException e) {
 					throw new InternalBackEndException("jSon format is not valid");
				} catch (final JsonParseException e) {
					throw new InternalBackEndException(e.getMessage());
 				}
 				break;
 			default :
 				throw new InternalBackEndException("PublishRequestHandler(" + code + ") not exist!");
 			}
 
 		} catch (final AbstractMymedException e) {
 			MLogger.getLog().info("Error in doPost operation");
 			MLogger.getDebugLog().debug("Error in doPost operation", e.getCause());
 			message.setStatus(e.getStatus());
 			message.setDescription(e.getMessage());
 		} 
 
 		printJSonResponse(message, response);
 	}
 }
