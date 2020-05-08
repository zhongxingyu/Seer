 package com.mymed.controller.core.requesthandler;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.reputation.InteractionManager;
 import com.mymed.model.data.reputation.MInteractionBean;
 
 /**
  * Servlet implementation class InteractionRequestHandler
  */
 @WebServlet("/InteractionRequestHandler")
 public class InteractionRequestHandler extends AbstractRequestHandler {
 	/* --------------------------------------------------------- */
 	/* Attributes */
 	/* --------------------------------------------------------- */
 	private static final long serialVersionUID = 1L;
 
 	private InteractionManager interactionManager;
 
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	/**
 	 * @throws ServletException 
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public InteractionRequestHandler() throws ServletException {
 		super();
 		try {
 			this.interactionManager = new InteractionManager();
 		} catch (InternalBackEndException e) {
 			throw new ServletException("InteractionManager is not accessible because: " + e.getMessage());
 		}
 	}
 
 	/* --------------------------------------------------------- */
 	/* extends HttpServlet */
 	/* --------------------------------------------------------- */
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		try {
 			/** Get the parameters */
 			Map<String, String> parameters = getParameters(request);
 
 			/** Get the method code */
 			RequestCode code = requestCodeMap.get(parameters.get("code"));
 
 			/** handle the request */
 			switch (code) {
 			case READ:
 				break;
 			case DELETE:
 				break;
 			default:
 				handleError(new InternalBackEndException("InteractionManager.doGet(" + code + ") not exist!"), response);
 				return;
 			}
 			super.doGet(request, response);
 		} catch (InternalBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 			return;
 		} 
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		try {
 			/** Get the parameters */
 			Map<String, String> parameters = getParameters(request);
 
 			/** Get the method code */
 			RequestCode code = requestCodeMap.get(parameters.get("code"));
 
 			/** handle the request */
 			String application, producer, consumer, start, end, predicate, feedback;
 			switch (code) {
 			case UPDATE:
 				if ((application = parameters.get("application")) == null) {
 					handleError(new InternalBackEndException(
 							"missing application argument!"), response);
 					return;
 				} else if ((producer = parameters.get("producer")) == null) {
 					handleError(new InternalBackEndException(
 							"missing producer argument!"), response);
 					return;
 				} else if ((consumer = parameters.get("consumer")) == null) {
 					handleError(new InternalBackEndException(
 							"missing consumer argument!"), response);
 					return;
 				} else if ((start = parameters.get("start")) == null) {
 					handleError(new InternalBackEndException(
 							"missing start argument!"), response);
 					return;
 				} else if ((end = parameters.get("end")) == null) {
 					handleError(new InternalBackEndException(
 							"missing end argument!"), response);
 					return;
 				} else if ((predicate = parameters.get("predicate")) == null) {
 					handleError(new InternalBackEndException(
 							"missing predicate argument!"), response);
 					return;
 				}
 				
				// verify consumer != producer
				if(consumer.equals(producer)){
					handleError(new InternalBackEndException(
							"cannot create interaction between same users"), response);
					return;
				}
				
 				// CREATE THE INTERACTION
 				MInteractionBean interaction = new MInteractionBean();
 				interaction.setId(application+producer+consumer+predicate);
 				interaction.setApplication(application);
 				interaction.setProducer(producer);
 				interaction.setConsumer(consumer);
 				interaction.setStart(Long.parseLong(start));
 				interaction.setEnd(Long.parseLong(end));
 				
 				// ATOMIC INTERACTION
 				if ((feedback = parameters.get("feedback")) != null) {
 					interaction.setFeedback(Double.parseDouble(feedback));
 				}
 				interactionManager.create(interaction);
 				setResponseText("interaction created!");
 				break;
 			default:
 				handleError(new InternalBackEndException("InteractionManager.doPost(" + code + ") not exist!"), response);
 				return;
 			}
 			super.doPost(request, response);
 		} catch (InternalBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 		} catch (IOBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 		}
 	}
 
 }
