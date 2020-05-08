 package server;
 
 public class ServerRequest {
 	private int ID;
 	private String requestLine;
 	private RequestType requestType;
 	
 	public ServerRequest(int ID, String requestLine) {
 		this.ID = ID;
 		this.requestLine = requestLine;
 		this.requestType = getType(requestLine);
 	}
 	
 	public int getID() {
 		return ID;
 	}
 	
 	public String getLine() {
 		return requestLine;
 	}
 	
 	public RequestType getType() {
 		return requestType;
 	}
 	
 	/**
 	 * Private helper method that returns the type of request made
 	 * @param input The request made, represented as a string
 	 * @return The request type 
 	 */
 	private RequestType getType(String input) {
 		if (input.startsWith("LOGIN ")) {
 			requestLine = requestLine.substring(6);
 			return RequestType.LOGIN;
 			
 		} else if (input.startsWith("NEWDOC ")){
 			requestLine = requestLine.substring(7);
 			return RequestType.NEWDOC;	
 			
 		} else if (input.startsWith("OPENDOC ")){
 			requestLine = requestLine.substring(8);
 			return RequestType.OPENDOC;
 			
		} else if (input.startsWith("CHANGE|")){
 			requestLine = requestLine.substring(7);
 			return RequestType.CHANGEDOC;
 			
 		} else if (input.startsWith("EXITDOC ")){
 			requestLine = requestLine.substring(8);
 			return RequestType.EXITDOC;
 			
 		} else if (input.startsWith("LOGOUT ")){
 			requestLine = requestLine.substring(7);
 			return RequestType.LOGOUT;			
 		} else {
 			requestLine = "";
 			return RequestType.INVALID_REQUEST;
 		}
 	}
 }
