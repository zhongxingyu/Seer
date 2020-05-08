 /**
  * 
  */
 package net.danielkvasnicka.flower.test.beans;
 
 import net.danielkvasnicka.flower.beans.AbstractWebAccessibleBean;
 import net.danielkvasnicka.flower.response.StreamingResponse;
 import net.danielkvasnicka.flower.response.api.Response;
 
 
 /**
  * @author Daniel Kvasnicka jr.
  *
  */
 public class TestWebAccessibleBean extends AbstractWebAccessibleBean {
 	
 	/**
	 * Shows a primitive text streaming response that prints the 
 	 * value of one of the REST params
 	 * 
 	 * @return
 	 */
 	public Response index() {
 		
 		return new StreamingResponse("text/plain", "x = " + this.parameters.get("x"));
 	}
 
 	/**
	 * XML streaming response, that prints parameter "y", which is not encoded
 	 * in the URL as a REST param and should be sent through POST or GET.
 	 * This is to demo the functionality od merging REST and POST/GET params.
 	 * 
 	 * @return
 	 */
 	public Response hello() {
 		
 		return new StreamingResponse("text/xml", "<a f=\"" + this.parameters.get("y") + "\" />");
 	}
 }
