 package org.jboss.tools.ws.ui.views;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 
 import javax.wsdl.Definition;
 import javax.wsdl.WSDLException;
 
 import org.jboss.tools.ws.ui.JBossWSUIPlugin;
 import org.jboss.tools.ws.ui.utils.TesterWSDLUtils;
 
 public class TestHistoryEntry implements Cloneable{
 
 	private String url;
 	private String action;
 	private String body;
	private String method;
 	private String headers;
 	private String parms;
 	private String[] resultHeadersList;
 	private String resultText;
 	private String wsTech;
 	private String serviceName;
 	private String portName;
 	private String bindingName;
 	private String operationName;
 	private Definition wsdlDef = null;
 	private String[] serviceNSMessage = null;
 	private boolean isSOAP12 = false;
 	
 	public TestHistoryEntry() {
 		// empty
 	}
 	
 	public String getUrl() {
 		return url;
 	}
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	public void setResultHeadersList(String[] resultHeadersList) {
 		this.resultHeadersList = resultHeadersList;
 	}
 
 	public String[] getResultHeadersList() {
 		return resultHeadersList;
 	}
 
 	public String getAction() {
 		return action;
 	}
 	public void setAction(String action) {
 		this.action = action;
 	}
 	public String getBody() {
 		return body;
 	}
 	public void setBody(String body) {
 		this.body = body;
 	}
 	public String getMethod() {
 		return method;
 	}
 	public void setMethod(String method) {
 		this.method = method;
 	}
 	public String getHeaders() {
 		return headers;
 	}
 	public void setHeaders(String headers) {
 		this.headers = headers;
 	}
 	public String getParms() {
 		return parms;
 	}
 	public void setParms(String parms) {
 		this.parms = parms;
 	}
 
 	public void setResultText(String resultText) {
 		this.resultText = resultText;
 	}
 
 	public String getResultText() {
 		return resultText;
 	}
 
 	public void setWsTech(String wsTech) {
 		this.wsTech = wsTech;
 	}
 
 	public String getWsTech() {
 		return wsTech;
 	}
 
 	public String getServiceName() {
 		return serviceName;
 	}
 
 	public void setServiceName(String serviceName) {
 		this.serviceName = serviceName;
 	}
 
 	public String getPortName() {
 		return portName;
 	}
 
 	public void setPortName(String portName) {
 		this.portName = portName;
 	}
 
 	public String getBindingName() {
 		return bindingName;
 	}
 
 	public void setBindingName(String bindingName) {
 		this.bindingName = bindingName;
 	}
 
 	public String getOperationName() {
 		return operationName;
 	}
 
 	public void setOperationName(String operationName) {
 		this.operationName = operationName;
 	}
 
 	public void setWsdlDef(Definition wsdlDef) {
 		this.wsdlDef = wsdlDef;
 	}
 
 	public Definition getWsdlDef() {
 		if (this.wsdlDef == null && this.url != null && this.url.length() > 0) {
 			try {
 				URL tempURL = new URL(this.url);
 				wsdlDef =
 					TesterWSDLUtils.readWSDLURL(tempURL);
 			} catch (MalformedURLException e) {
 				JBossWSUIPlugin.log(e);
 				return null;
 			} catch (WSDLException e) {
 				JBossWSUIPlugin.log(e);
 				return null;
 			}
 		}
 		return wsdlDef;
 	}
 
 	public void setServiceNSMessage(String[] serviceNSMessage) {
 		this.serviceNSMessage = serviceNSMessage;
 	}
 
 	public String[] getServiceNSMessage() {
 		return serviceNSMessage;
 	}
 
 	public void setSOAP12(boolean isSOAP12) {
 		this.isSOAP12 = isSOAP12;
 	}
 
 	public boolean isSOAP12() {
 		return isSOAP12;
 	}
 
 	@Override
 	public String toString() {
 		return "TestHistoryEntry [url=" + url  //$NON-NLS-1$
 				+ ", action=" + action //$NON-NLS-1$
 				+ ", body=" + body  //$NON-NLS-1$
 				+ ", method=" + method  //$NON-NLS-1$
 				+ ", headers=" + headers  //$NON-NLS-1$
 				+ ", parms=" + parms  //$NON-NLS-1$
 				+ ", resultHeadersList=" + Arrays.toString(resultHeadersList)  //$NON-NLS-1$
 				+ ", resultText=" + resultText  //$NON-NLS-1$
 				+ ", wsTech=" + wsTech  //$NON-NLS-1$
 				+ ", serviceName=" + serviceName  //$NON-NLS-1$
 				+ ", portName=" + portName  //$NON-NLS-1$
 				+ ", bindingName=" + bindingName  //$NON-NLS-1$
 				+ ", operationName=" + operationName //$NON-NLS-1$
 				+ ", wsdlDef=" + wsdlDef  //$NON-NLS-1$
 				+ ", serviceNSMessage=" + Arrays.toString(serviceNSMessage)  //$NON-NLS-1$
 				+ ", isSOAP12=" + isSOAP12 //$NON-NLS-1$
 				+ "]"; //$NON-NLS-1$
 	}
 
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
 		TestHistoryEntry newEntry = new TestHistoryEntry();
 		newEntry.setAction(this.getAction());
 		newEntry.setBindingName(this.getBindingName());
 		newEntry.setBody(this.getBody());
 		newEntry.setHeaders(this.getHeaders());
 		newEntry.setMethod(this.getMethod());
 		newEntry.setOperationName(this.getOperationName());
 		newEntry.setParms(this.getParms());
 		newEntry.setPortName(this.getPortName());
 		newEntry.setResultHeadersList(this.getResultHeadersList());
 		newEntry.setResultText(this.getResultText());
 		newEntry.setServiceName(this.getServiceName());
 		newEntry.setServiceNSMessage(this.getServiceNSMessage());
 		newEntry.setSOAP12(this.isSOAP12());
 		newEntry.setUrl(this.getUrl());
 //		if (this.getWsdlDef() != null)
 //			newEntry.setWsdlDef(this.getWsdlDef().);
 		newEntry.setWsTech(this.getWsTech());
 		return newEntry;
 	}
 	
 
 }
